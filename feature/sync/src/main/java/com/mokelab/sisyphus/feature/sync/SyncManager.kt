package com.mokelab.sisyphus.feature.sync

import android.content.Context
import com.mokelab.sisyphus.core.database.SisyphusDatabase
import com.mokelab.sisyphus.core.database.entity.CardState
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import com.mokelab.sisyphus.core.database.entity.ExamType
import com.mokelab.sisyphus.core.database.entity.InputOutputType
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import com.mokelab.sisyphus.core.database.entity.ReadingType
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.StudyType
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * 增量同步管理器
 */
class SyncManager(
    private val context: Context,
    private val database: SisyphusDatabase,
    private val syncService: SyncService,
    private val tokenStorage: TokenStorage,
    private val conflictResolver: ConflictResolver
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _syncState = MutableStateFlow<SyncResult?>(null)
    val syncState: StateFlow<SyncResult?> = _syncState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _conflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val conflicts: StateFlow<List<SyncConflict>> = _conflicts.asStateFlow()

    private val deviceId: String by lazy {
        val stored = tokenStorage.getDeviceId()
        if (stored.isNotEmpty()) {
            stored
        } else {
            val newId = UUID.randomUUID().toString()
            tokenStorage.saveDeviceId(newId)
            newId
        }
    }

    /**
     * 执行同步
     */
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (_isSyncing.value) {
            return@withContext SyncResult(success = false, error = "同步正在进行中")
        }

        _isSyncing.value = true
        val conflicts = mutableListOf<SyncConflict>()
        var uploaded = 0
        var downloaded = 0

        try {
            val lastSync = tokenStorage.getLastSyncTimestamp()
            val now = System.currentTimeMillis()

            // 1. 获取本地变更
            val localChanges = getLocalChangesSince(lastSync)

            // 2. 获取远程变更
            val remoteChanges = getRemoteChangesSince(lastSync)

            // 3. 按实体ID合并
            val allEntityIds = (localChanges.keys + remoteChanges.keys).distinct()

            for (entityId in allEntityIds) {
                val local = localChanges[entityId]
                val remote = remoteChanges[entityId]

                when {
                    local != null && remote == null -> {
                        // 只有本地变更，上传
                        uploadEntity(entityId, local)
                        uploaded++
                    }
                    local == null && remote != null -> {
                        // 只有远程变更，下载
                        downloadEntity(entityId, remote)
                        downloaded++
                    }
                    local != null && remote != null -> {
                        // 都有变更，需要合并
                        val merged = conflictResolver.merge(entityId, local, remote)
                        if (merged.hasConflict) {
                            conflicts.add(merged.conflict!!)
                        }
                        saveMergedEntity(entityId, merged.mergedData)
                        uploaded++
                        downloaded++
                    }
                }
            }

            // 4. 更新同步时间戳
            tokenStorage.saveLastSyncTimestamp(now)

            // 5. 更新同步元数据
            updateSyncMetadata(now, allEntityIds.size)

            val result = SyncResult(
                uploaded = uploaded,
                downloaded = downloaded,
                conflicts = conflicts,
                success = true
            )
            _syncState.value = result
            _conflicts.value = conflicts
            result

        } catch (e: Exception) {
            val result = SyncResult(
                uploaded = uploaded,
                downloaded = downloaded,
                conflicts = conflicts,
                success = false,
                error = e.message
            )
            _syncState.value = result
            result
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * 获取本地变更
     */
    private suspend fun getLocalChangesSince(timestamp: Long): Map<String, EntityData> {
        val since = Instant.fromEpochMilliseconds(timestamp)
        val result = mutableMapOf<String, EntityData>()

        // 学科
        database.subjectDao().getModifiedSince(since).forEach { entity ->
            result["subject_${entity.id}"] = entity.toEntityData()
        }

        // 学习记录
        database.studyRecordDao().getModifiedSince(since).forEach { entity ->
            result["study_record_${entity.id}"] = entity.toEntityData()
        }

        // 复习卡片
        database.reviewCardDao().getModifiedSince(since).forEach { entity ->
            result["review_card_${entity.id}"] = entity.toEntityData()
        }

        // 复习历史
        database.reviewHistoryDao().getModifiedSince(since).forEach { entity ->
            result["review_history_${entity.id}"] = entity.toEntityData()
        }

        // 考试记录
        database.examRecordDao().getModifiedSince(since).forEach { entity ->
            result["exam_record_${entity.id}"] = entity.toEntityData()
        }

        // 阅读记录
        database.readingRecordDao().getModifiedSince(since).forEach { entity ->
            result["reading_record_${entity.id}"] = entity.toEntityData()
        }

        return result
    }

    /**
     * 获取远程变更
     */
    private suspend fun getRemoteChangesSince(timestamp: Long): Map<String, EntityData> {
        return try {
            val metadataJson = syncService.downloadData().getOrNull() ?: return emptyMap()
            val metadata = json.decodeFromString<SyncMetadata>(metadataJson)

            metadata.entityVersions
                .filter { it.value > getLocalVersion(it.key) }
                .mapValues { (entityId, _) ->
                    val entityJson = syncService.downloadEntity(entityId).getOrNull()
                    entityJson?.let { json.decodeFromString<EntityData>(it) }
                        ?: EntityData()
                }
                .filterValues { it.fields.isNotEmpty() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 上传实体
     */
    private suspend fun uploadEntity(entityId: String, data: EntityData) {
        val entityJson = json.encodeToString(data)
        syncService.uploadEntity(entityId, entityJson)
    }

    /**
     * 下载实体
     */
    private suspend fun downloadEntity(entityId: String, data: EntityData) {
        saveEntityData(entityId, data)
    }

    /**
     * 保存合并后的实体
     */
    private suspend fun saveMergedEntity(entityId: String, data: EntityData) {
        saveEntityData(entityId, data)
    }

    /**
     * 保存实体数据到数据库
     */
    private suspend fun saveEntityData(entityId: String, data: EntityData) {
        val parts = entityId.split("_", limit = 2)
        if (parts.size != 2) return

        val type = parts[0]
        val id = parts[1].toLongOrNull() ?: return

        when (type) {
            "subject" -> {
                val entity = data.toSubjectEntity(id)
                if (entity != null) {
                    database.subjectDao().insert(entity)
                }
            }
            "study_record" -> {
                val entity = data.toStudyRecordEntity(id)
                if (entity != null) {
                    database.studyRecordDao().insert(entity)
                }
            }
            "review_card" -> {
                val entity = data.toReviewCardEntity(id)
                if (entity != null) {
                    database.reviewCardDao().insert(entity)
                }
            }
            "review_history" -> {
                val entity = data.toReviewHistoryEntity(id)
                if (entity != null) {
                    database.reviewHistoryDao().insert(entity)
                }
            }
            "exam_record" -> {
                val entity = data.toExamRecordEntity(id)
                if (entity != null) {
                    database.examRecordDao().insert(entity)
                }
            }
            "reading_record" -> {
                val entity = data.toReadingRecordEntity(id)
                if (entity != null) {
                    database.readingRecordDao().insert(entity)
                }
            }
        }
    }

    /**
     * 获取本地实体版本
     */
    private suspend fun getLocalVersion(entityId: String): Long {
        val metadataJson = tokenStorage.getSyncMetadata()
        if (metadataJson.isEmpty()) return 0
        return try {
            val metadata = json.decodeFromString<SyncMetadata>(metadataJson)
            metadata.entityVersions[entityId] ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 更新同步元数据
     */
    private suspend fun updateSyncMetadata(timestamp: Long, entityCount: Int) {
        val metadata = SyncMetadata(
            lastSyncTimestamp = timestamp,
            deviceId = deviceId,
            entityVersions = emptyMap()
        )
        tokenStorage.saveSyncMetadata(json.encodeToString(metadata))
    }

    /**
     * 清除冲突
     */
    fun clearConflicts() {
        _conflicts.value = emptyList()
    }
}

// Entity -> EntityData 扩展函数

private fun SubjectEntity.toEntityData(): EntityData {
    val now = updatedAt.toEpochMilliseconds()
    return EntityData(
        fields = mapOf(
            "id" to id.toString(),
            "name" to name,
            "weight" to weight.toString(),
            "isElective" to isElective.toString(),
            "examScoreRatio" to examScoreRatio.toString()
        ),
        fieldTimestamps = mapOf(
            "id" to now,
            "name" to now,
            "weight" to now,
            "isElective" to now,
            "examScoreRatio" to now
        ),
        version = updatedAt.toEpochMilliseconds()
    )
}

private fun StudyRecordEntity.toEntityData(): EntityData {
    val now = updatedAt.toEpochMilliseconds()
    return EntityData(
        fields = mapOf(
            "id" to id.toString(),
            "subjectId" to subjectId.toString(),
            "textbookId" to textbookId?.toString(),
            "chapterId" to chapterId?.toString(),
            "sectionId" to sectionId?.toString(),
            "studyType" to studyType.name,
            "durationMinutes" to durationMinutes.toString(),
            "startTime" to startTime.toEpochMilliseconds().toString(),
            "endTime" to endTime.toEpochMilliseconds().toString(),
            "inputType" to inputType.name,
            "xpEarned" to xpEarned.toString(),
            "note" to note
        ),
        fieldTimestamps = mapOf(
            "id" to now,
            "subjectId" to now,
            "textbookId" to now,
            "chapterId" to now,
            "sectionId" to now,
            "studyType" to now,
            "durationMinutes" to now,
            "startTime" to now,
            "endTime" to now,
            "inputType" to now,
            "xpEarned" to now,
            "note" to now
        ),
        version = updatedAt.toEpochMilliseconds()
    )
}

private fun ReviewCardEntity.toEntityData(): EntityData {
    val now = updatedAt.toEpochMilliseconds()
    return EntityData(
        fields = mapOf(
            "id" to id.toString(),
            "knowledgePointId" to knowledgePointId.toString(),
            "studyRecordId" to studyRecordId?.toString(),
            "stability" to stability.toString(),
            "difficulty" to difficulty.toString(),
            "elapsedDays" to elapsedDays.toString(),
            "scheduledDays" to scheduledDays.toString(),
            "reps" to reps.toString(),
            "lapses" to lapses.toString(),
            "state" to state.name,
            "due" to due.toEpochMilliseconds().toString(),
            "lastReview" to lastReview?.toEpochMilliseconds()?.toString()
        ),
        fieldTimestamps = mapOf(
            "id" to now,
            "knowledgePointId" to now,
            "studyRecordId" to now,
            "stability" to now,
            "difficulty" to now,
            "elapsedDays" to now,
            "scheduledDays" to now,
            "reps" to now,
            "lapses" to now,
            "state" to now,
            "due" to now,
            "lastReview" to now
        ),
        version = updatedAt.toEpochMilliseconds()
    )
}

private fun ReviewHistoryEntity.toEntityData(): EntityData {
    val now = updatedAt.toEpochMilliseconds()
    return EntityData(
        fields = mapOf(
            "id" to id.toString(),
            "cardId" to cardId.toString(),
            "rating" to rating.toString(),
            "stabilityBefore" to stabilityBefore.toString(),
            "stabilityAfter" to stabilityAfter.toString(),
            "difficultyBefore" to difficultyBefore.toString(),
            "difficultyAfter" to difficultyAfter.toString(),
            "intervalBefore" to intervalBefore.toString(),
            "intervalAfter" to intervalAfter.toString(),
            "reviewTime" to reviewTime.toEpochMilliseconds().toString()
        ),
        fieldTimestamps = mapOf(
            "id" to now,
            "cardId" to now,
            "rating" to now,
            "stabilityBefore" to now,
            "stabilityAfter" to now,
            "difficultyBefore" to now,
            "difficultyAfter" to now,
            "intervalBefore" to now,
            "intervalAfter" to now,
            "reviewTime" to now
        ),
        version = updatedAt.toEpochMilliseconds()
    )
}

private fun ExamRecordEntity.toEntityData(): EntityData {
    val now = updatedAt.toEpochMilliseconds()
    return EntityData(
        fields = mapOf(
            "id" to id.toString(),
            "subjectId" to subjectId.toString(),
            "examName" to examName,
            "examType" to examType.name,
            "score" to score.toString(),
            "totalScore" to totalScore.toString(),
            "scoreRate" to scoreRate.toString(),
            "isFullMock" to isFullMock.toString(),
            "examDate" to examDate.toEpochMilliseconds().toString()
        ),
        fieldTimestamps = mapOf(
            "id" to now,
            "subjectId" to now,
            "examName" to now,
            "examType" to now,
            "score" to now,
            "totalScore" to now,
            "scoreRate" to now,
            "isFullMock" to now,
            "examDate" to now
        ),
        version = updatedAt.toEpochMilliseconds()
    )
}

private fun ReadingRecordEntity.toEntityData(): EntityData {
    val now = updatedAt.toEpochMilliseconds()
    return EntityData(
        fields = mapOf(
            "id" to id.toString(),
            "bookName" to bookName,
            "author" to author,
            "readingType" to readingType.name,
            "durationMinutes" to durationMinutes.toString(),
            "startTime" to startTime.toEpochMilliseconds().toString(),
            "endTime" to endTime.toEpochMilliseconds().toString(),
            "note" to note
        ),
        fieldTimestamps = mapOf(
            "id" to now,
            "bookName" to now,
            "author" to now,
            "readingType" to now,
            "durationMinutes" to now,
            "startTime" to now,
            "endTime" to now,
            "note" to now
        ),
        version = updatedAt.toEpochMilliseconds()
    )
}

// EntityData -> Entity 扩展函数

private fun EntityData.toSubjectEntity(id: Long): SubjectEntity? {
    val name = fields["name"] ?: return null
    val createdAt = fields["createdAt"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        ?: Instant.fromEpochMilliseconds(version)
    return SubjectEntity(
        id = id,
        name = name,
        weight = fields["weight"]?.toFloatOrNull() ?: 5.0f,
        isElective = fields["isElective"]?.toBooleanStrictOrNull() ?: false,
        examScoreRatio = fields["examScoreRatio"]?.toFloatOrNull() ?: 0f,
        createdAt = createdAt,
        updatedAt = Instant.fromEpochMilliseconds(version)
    )
}

private fun EntityData.toStudyRecordEntity(id: Long): StudyRecordEntity? {
    val subjectId = fields["subjectId"]?.toLongOrNull() ?: return null
    val studyType = fields["studyType"]?.let { StudyType.valueOf(it) } ?: return null
    val durationMinutes = fields["durationMinutes"]?.toIntOrNull() ?: return null
    val startTime = fields["startTime"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val endTime = fields["endTime"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val inputType = fields["inputType"]?.let { InputOutputType.valueOf(it) } ?: return null
    val createdAt = fields["createdAt"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        ?: Instant.fromEpochMilliseconds(version)
    return StudyRecordEntity(
        id = id,
        subjectId = subjectId,
        textbookId = fields["textbookId"]?.toLongOrNull(),
        chapterId = fields["chapterId"]?.toLongOrNull(),
        sectionId = fields["sectionId"]?.toLongOrNull(),
        studyType = studyType,
        durationMinutes = durationMinutes,
        startTime = startTime,
        endTime = endTime,
        inputType = inputType,
        xpEarned = fields["xpEarned"]?.toFloatOrNull() ?: 0f,
        note = fields["note"],
        createdAt = createdAt,
        updatedAt = Instant.fromEpochMilliseconds(version)
    )
}

private fun EntityData.toReviewCardEntity(id: Long): ReviewCardEntity? {
    val knowledgePointId = fields["knowledgePointId"]?.toLongOrNull() ?: return null
    val state = fields["state"]?.let { CardState.valueOf(it) } ?: return null
    val due = fields["due"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val createdAt = fields["createdAt"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        ?: Instant.fromEpochMilliseconds(version)
    return ReviewCardEntity(
        id = id,
        knowledgePointId = knowledgePointId,
        studyRecordId = fields["studyRecordId"]?.toLongOrNull(),
        stability = fields["stability"]?.toFloatOrNull() ?: 0f,
        difficulty = fields["difficulty"]?.toFloatOrNull() ?: 0f,
        elapsedDays = fields["elapsedDays"]?.toIntOrNull() ?: 0,
        scheduledDays = fields["scheduledDays"]?.toIntOrNull() ?: 0,
        reps = fields["reps"]?.toIntOrNull() ?: 0,
        lapses = fields["lapses"]?.toIntOrNull() ?: 0,
        state = state,
        due = due,
        lastReview = fields["lastReview"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = createdAt,
        updatedAt = Instant.fromEpochMilliseconds(version)
    )
}

private fun EntityData.toReviewHistoryEntity(id: Long): ReviewHistoryEntity? {
    val cardId = fields["cardId"]?.toLongOrNull() ?: return null
    val rating = fields["rating"]?.toIntOrNull() ?: return null
    val reviewTime = fields["reviewTime"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val createdAt = fields["createdAt"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        ?: Instant.fromEpochMilliseconds(version)
    return ReviewHistoryEntity(
        id = id,
        cardId = cardId,
        rating = rating,
        stabilityBefore = fields["stabilityBefore"]?.toFloatOrNull() ?: 0f,
        stabilityAfter = fields["stabilityAfter"]?.toFloatOrNull() ?: 0f,
        difficultyBefore = fields["difficultyBefore"]?.toFloatOrNull() ?: 0f,
        difficultyAfter = fields["difficultyAfter"]?.toFloatOrNull() ?: 0f,
        intervalBefore = fields["intervalBefore"]?.toIntOrNull() ?: 0,
        intervalAfter = fields["intervalAfter"]?.toIntOrNull() ?: 0,
        reviewTime = reviewTime,
        createdAt = createdAt,
        updatedAt = Instant.fromEpochMilliseconds(version)
    )
}

private fun EntityData.toExamRecordEntity(id: Long): ExamRecordEntity? {
    val subjectId = fields["subjectId"]?.toLongOrNull() ?: return null
    val examName = fields["examName"] ?: return null
    val examType = fields["examType"]?.let { ExamType.valueOf(it) } ?: return null
    val score = fields["score"]?.toFloatOrNull() ?: return null
    val totalScore = fields["totalScore"]?.toFloatOrNull() ?: return null
    val scoreRate = fields["scoreRate"]?.toFloatOrNull() ?: return null
    val examDate = fields["examDate"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val createdAt = fields["createdAt"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        ?: Instant.fromEpochMilliseconds(version)
    return ExamRecordEntity(
        id = id,
        subjectId = subjectId,
        examName = examName,
        examType = examType,
        score = score,
        totalScore = totalScore,
        scoreRate = scoreRate,
        isFullMock = fields["isFullMock"]?.toBooleanStrictOrNull() ?: false,
        examDate = examDate,
        createdAt = createdAt,
        updatedAt = Instant.fromEpochMilliseconds(version)
    )
}

private fun EntityData.toReadingRecordEntity(id: Long): ReadingRecordEntity? {
    val bookName = fields["bookName"] ?: return null
    val readingType = fields["readingType"]?.let { ReadingType.valueOf(it) } ?: return null
    val durationMinutes = fields["durationMinutes"]?.toIntOrNull() ?: return null
    val startTime = fields["startTime"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val endTime = fields["endTime"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val createdAt = fields["createdAt"]?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        ?: Instant.fromEpochMilliseconds(version)
    return ReadingRecordEntity(
        id = id,
        bookName = bookName,
        author = fields["author"],
        readingType = readingType,
        durationMinutes = durationMinutes,
        startTime = startTime,
        endTime = endTime,
        note = fields["note"],
        createdAt = createdAt,
        updatedAt = Instant.fromEpochMilliseconds(version)
    )
}

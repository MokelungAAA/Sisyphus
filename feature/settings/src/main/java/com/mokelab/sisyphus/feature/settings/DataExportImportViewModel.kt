package com.mokelab.sisyphus.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.SisyphusDatabase
import com.mokelab.sisyphus.core.database.entity.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportTime: Long,
    val subjects: List<SubjectData>,
    val studyRecords: List<StudyRecordData>,
    val pomodoroSessions: List<PomodoroSessionData>,
    val reviewCards: List<ReviewCardData>,
    val readingRecords: List<ReadingRecordData>,
    val examRecords: List<ExamRecordData>,
    val knowledgePoints: List<KnowledgePointData>
)

@Serializable data class SubjectData(val id: Long, val name: String, val weight: Float, val isElective: Boolean, val examScoreRatio: Float)
@Serializable data class StudyRecordData(val id: Long, val subjectId: Long, val textbookId: Long?, val chapterId: Long?, val sectionId: Long?, val studyType: String, val durationMinutes: Int, val startTime: Long, val endTime: Long, val inputType: String, val xpEarned: Float, val note: String?)
@Serializable data class PomodoroSessionData(val id: Long, val subjectId: Long, val studyRecordId: Long?, val durationMinutes: Int, val actualMinutes: Int, val startTime: Long, val endTime: Long?, val isCompleted: Boolean, val presetType: String)
@Serializable data class ReviewCardData(val id: Long, val knowledgePointId: Long, val studyRecordId: Long?, val stability: Float, val difficulty: Float, val elapsedDays: Int, val scheduledDays: Int, val reps: Int, val lapses: Int, val state: String, val due: Long, val lastReview: Long?)
@Serializable data class ReadingRecordData(val id: Long, val bookName: String, val author: String?, val readingType: String, val durationMinutes: Int, val startTime: Long, val endTime: Long, val note: String?)
@Serializable data class ExamRecordData(val id: Long, val subjectId: Long, val examName: String, val examType: String, val score: Float, val totalScore: Float, val scoreRate: Float, val isFullMock: Boolean, val examDate: Long)
@Serializable data class KnowledgePointData(val id: Long, val sectionId: Long, val name: String, val content: String?, val source: String)

data class ExportImportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class DataExportImportViewModel(
    private val database: SisyphusDatabase,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState: StateFlow<ExportImportUiState> = _uiState.asStateFlow()

    // 为 SettingsScreen 提供直接的 StateFlow 属性
    val isExporting: StateFlow<Boolean> = _uiState.map { it.isExporting }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isImporting: StateFlow<Boolean> = _uiState.map { it.isImporting }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val message: StateFlow<String?> = _uiState.map { it.message }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val errorMessage: StateFlow<String?> = _uiState.map { it.error }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun exportToJson(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null, message = null)
            try {
                val exportData = gatherExportData()
                val jsonString = json.encodeToString(exportData)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "导出成功！数据已保存到所选位置"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "导出失败: ${e.message}"
                )
            }
        }
    }

    fun importFromJson(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, error = null, message = null)
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: throw Exception("无法读取文件")

                val importData = json.decodeFromString<ExportData>(jsonString)
                importDataToDatabase(importData)

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "导入成功！已恢复 ${importData.subjects.size} 个学科，${importData.studyRecords.size} 条学习记录"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "导入失败: ${e.message}"
                )
            }
        }
    }

    fun exportToCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null, message = null)
            try {
                val exportData = gatherExportData()
                val csvString = buildCsvString(exportData)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvString.toByteArray(Charsets.UTF_8))
                }

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "CSV导出成功！数据已保存到所选位置"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "CSV导出失败: ${e.message}"
                )
            }
        }
    }

    private fun buildCsvString(data: ExportData): String {
        val sb = StringBuilder()
        // BOM for Excel UTF-8 compatibility
        sb.append("﻿")

        // 学科表
        sb.appendLine("=== 学科 ===")
        sb.appendLine("ID,名称,权重,选修,考试分数比例")
        data.subjects.forEach {
            sb.appendLine("${it.id},${escapeCsv(it.name)},${it.weight},${it.isElective},${it.examScoreRatio}")
        }
        sb.appendLine()

        // 学习记录表
        sb.appendLine("=== 学习记录 ===")
        sb.appendLine("ID,学科ID,学习类型,时长(分钟),输入类型,XP,备注")
        data.studyRecords.forEach {
            sb.appendLine("${it.id},${it.subjectId},${it.studyType},${it.durationMinutes},${it.inputType},${it.xpEarned},${escapeCsv(it.note ?: "")}")
        }
        sb.appendLine()

        // 番茄钟记录
        sb.appendLine("=== 番茄钟记录 ===")
        sb.appendLine("ID,学科ID,时长(分钟),实际时长(分钟),是否完成,预设类型")
        data.pomodoroSessions.forEach {
            sb.appendLine("${it.id},${it.subjectId},${it.durationMinutes},${it.actualMinutes},${it.isCompleted},${it.presetType}")
        }
        sb.appendLine()

        // 复习卡片
        sb.appendLine("=== 复习卡片 ===")
        sb.appendLine("ID,知识点ID,稳定性,难度,状态,到期日")
        data.reviewCards.forEach {
            sb.appendLine("${it.id},${it.knowledgePointId},${it.stability},${it.difficulty},${it.state},${it.due}")
        }
        sb.appendLine()

        // 阅读记录
        sb.appendLine("=== 阅读记录 ===")
        sb.appendLine("ID,书名,作者,阅读类型,时长(分钟),备注")
        data.readingRecords.forEach {
            sb.appendLine("${it.id},${escapeCsv(it.bookName)},${escapeCsv(it.author ?: "")},${it.readingType},${it.durationMinutes},${escapeCsv(it.note ?: "")}")
        }
        sb.appendLine()

        // 考试记录
        sb.appendLine("=== 考试记录 ===")
        sb.appendLine("ID,学科ID,考试名称,考试类型,分数,总分,得分率,是否全真模拟")
        data.examRecords.forEach {
            sb.appendLine("${it.id},${it.subjectId},${escapeCsv(it.examName)},${it.examType},${it.score},${it.totalScore},${it.scoreRate},${it.isFullMock}")
        }

        return sb.toString()
    }

    private fun escapeCsv(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun gatherExportData(): ExportData {
        val subjectDao = database.subjectDao()
        val studyRecordDao = database.studyRecordDao()
        val pomodoroDao = database.pomodoroSessionDao()
        val reviewCardDao = database.reviewCardDao()
        val readingRecordDao = database.readingRecordDao()
        val examRecordDao = database.examRecordDao()
        val knowledgePointDao = database.knowledgePointDao()

        return ExportData(
            exportTime = Clock.System.now().toEpochMilliseconds(),
            subjects = subjectDao.getAllList().map { SubjectData(it.id, it.name, it.weight, it.isElective, it.examScoreRatio) },
            studyRecords = studyRecordDao.getAllList().map { StudyRecordData(it.id, it.subjectId, it.textbookId, it.chapterId, it.sectionId, it.studyType.name, it.durationMinutes, it.startTime.toEpochMilliseconds(), it.endTime.toEpochMilliseconds(), it.inputType.name, it.xpEarned, it.note) },
            pomodoroSessions = pomodoroDao.getAllList().map { PomodoroSessionData(it.id, it.subjectId, it.studyRecordId, it.durationMinutes, it.actualMinutes, it.startTime.toEpochMilliseconds(), it.endTime?.toEpochMilliseconds(), it.isCompleted, it.presetType.name) },
            reviewCards = reviewCardDao.getAllList().map { ReviewCardData(it.id, it.knowledgePointId, it.studyRecordId, it.stability, it.difficulty, it.elapsedDays, it.scheduledDays, it.reps, it.lapses, it.state.name, it.due.toEpochMilliseconds(), it.lastReview?.toEpochMilliseconds()) },
            readingRecords = readingRecordDao.getAllList().map { ReadingRecordData(it.id, it.bookName, it.author, it.readingType.name, it.durationMinutes, it.startTime.toEpochMilliseconds(), it.endTime.toEpochMilliseconds(), it.note) },
            examRecords = examRecordDao.getAllList().map { ExamRecordData(it.id, it.subjectId, it.examName, it.examType.name, it.score, it.totalScore, it.scoreRate, it.isFullMock, it.examDate.toEpochMilliseconds()) },
            knowledgePoints = knowledgePointDao.getAllList().map { KnowledgePointData(it.id, it.sectionId, it.name, it.content, it.source.name) }
        )
    }

    private suspend fun importDataToDatabase(data: ExportData) {
        val subjectDao = database.subjectDao()
        val studyRecordDao = database.studyRecordDao()
        val pomodoroDao = database.pomodoroSessionDao()
        val reviewCardDao = database.reviewCardDao()
        val readingRecordDao = database.readingRecordDao()
        val examRecordDao = database.examRecordDao()
        val knowledgePointDao = database.knowledgePointDao()
        val now = Clock.System.now()

        data.subjects.forEach {
            subjectDao.insert(SubjectEntity(it.id, it.name, it.weight, it.isElective, it.examScoreRatio, now, now))
        }
        data.knowledgePoints.forEach {
            val source = try { KnowledgePointSource.valueOf(it.source) } catch (_: Exception) { KnowledgePointSource.USER_ADDED }
            knowledgePointDao.insert(KnowledgePointEntity(it.id, it.sectionId, it.name, it.content, source, now, now))
        }
        data.studyRecords.forEach {
            val studyType = try { StudyType.valueOf(it.studyType) } catch (_: Exception) { StudyType.COURSE }
            val inputType = try { InputOutputType.valueOf(it.inputType) } catch (_: Exception) { InputOutputType.INPUT }
            studyRecordDao.insert(StudyRecordEntity(it.id, it.subjectId, it.textbookId, it.chapterId, it.sectionId, studyType, it.durationMinutes, Instant.fromEpochMilliseconds(it.startTime), Instant.fromEpochMilliseconds(it.endTime), inputType, it.xpEarned, it.note, now, now))
        }
        data.pomodoroSessions.forEach {
            val presetType = try { PresetType.valueOf(it.presetType) } catch (_: Exception) { PresetType.CLASSIC }
            pomodoroDao.insert(PomodoroSessionEntity(it.id, it.subjectId, it.studyRecordId, it.durationMinutes, it.actualMinutes, Instant.fromEpochMilliseconds(it.startTime), it.endTime?.let { t -> Instant.fromEpochMilliseconds(t) }, it.isCompleted, presetType, now, now))
        }
        data.readingRecords.forEach {
            val readingType = try { ReadingType.valueOf(it.readingType) } catch (_: Exception) { ReadingType.BOOK }
            readingRecordDao.insert(ReadingRecordEntity(it.id, it.bookName, it.author, readingType, it.durationMinutes, Instant.fromEpochMilliseconds(it.startTime), Instant.fromEpochMilliseconds(it.endTime), it.note, now, now))
        }
        data.examRecords.forEach {
            val examType = try { ExamType.valueOf(it.examType) } catch (_: Exception) { ExamType.MONTHLY }
            examRecordDao.insert(ExamRecordEntity(it.id, it.subjectId, it.examName, examType, it.score, it.totalScore, it.scoreRate, it.isFullMock, Instant.fromEpochMilliseconds(it.examDate), now, now))
        }
        data.reviewCards.forEach {
            val state = try { CardState.valueOf(it.state) } catch (_: Exception) { CardState.NEW }
            reviewCardDao.insert(ReviewCardEntity(it.id, it.knowledgePointId, it.studyRecordId, it.stability, it.difficulty, it.elapsedDays, it.scheduledDays, it.reps, it.lapses, state, Instant.fromEpochMilliseconds(it.due), it.lastReview?.let { t -> Instant.fromEpochMilliseconds(t) }, now, now))
        }
    }
}

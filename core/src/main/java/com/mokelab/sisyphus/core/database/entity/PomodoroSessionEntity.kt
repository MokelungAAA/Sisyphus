package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 预设类型
 */
enum class PresetType {
    CLASSIC,
    CUSTOM
}

/**
 * 番茄钟会话实体
 */
@Entity(
    tableName = "pomodoro_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val studyRecordId: Long? = null,
    val durationMinutes: Int,
    val actualMinutes: Int = 0,
    val startTime: Instant,
    val endTime: Instant? = null,
    val isCompleted: Boolean = false,
    val presetType: PresetType = PresetType.CLASSIC,
    val createdAt: Instant,
)

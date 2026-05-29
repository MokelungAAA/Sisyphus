package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 考试类型
 */
enum class ExamType {
    MONTHLY,     // 月考
    MIDTERM,     // 期中
    FINAL,       // 期末
    MOCK,        // 模拟考
    SIMULATION   // 高考模拟
}

/**
 * 考试记录实体
 */
@Entity(
    tableName = "exam_records",
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
data class ExamRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val examName: String,
    val examType: ExamType,
    val score: Float,
    val totalScore: Float,
    val scoreRate: Float,
    val isFullMock: Boolean = false,
    val examDate: Instant,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

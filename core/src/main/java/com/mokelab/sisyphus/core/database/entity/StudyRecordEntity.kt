package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 学习类型
 */
enum class StudyType {
    COURSE,        // 课程 - 输入
    EXERCISE,      // 刷题 - 输出
    REVIEW,        // 复习 - 输入
    NOTE,          // 笔记 - 输出
    MEMORIZATION,  // 背诵 - 输入
    DICTATION      // 默写 - 输出
}

/**
 * 输入输出类型
 */
enum class InputOutputType {
    INPUT,
    OUTPUT
}

/**
 * 学习记录实体
 */
@Entity(
    tableName = "study_records",
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
data class StudyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val textbookId: Long? = null,
    val chapterId: Long? = null,
    val sectionId: Long? = null,
    val studyType: StudyType,
    val durationMinutes: Int,
    val startTime: Instant,
    val endTime: Instant,
    val inputType: InputOutputType,
    val xpEarned: Float = 0f,
    val note: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

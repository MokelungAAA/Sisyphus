package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 教材类型
 */
enum class TextbookType {
    TUTORIAL,  // 教辅
    COURSE     // 网课
}

/**
 * 教材来源
 */
enum class TextbookSource {
    PHOTO_OCR,      // 拍照OCR
    AI_GENERATED,   // AI生成
    MANUAL          // 手动录入
}

/**
 * 教材实体
 */
@Entity(
    tableName = "textbooks",
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
data class TextbookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val type: TextbookType,
    val source: TextbookSource,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

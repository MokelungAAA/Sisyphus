package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 知识点来源
 */
enum class KnowledgePointSource {
    BUILTIN,        // 内置
    AI_GENERATED,   // AI生成
    USER_ADDED      // 用户添加
}

/**
 * 知识点实体
 */
@Entity(
    tableName = "knowledge_points",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sectionId"])]
)
data class KnowledgePointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sectionId: Long,
    val name: String,
    val content: String? = null,
    val source: KnowledgePointSource,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

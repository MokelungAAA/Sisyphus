package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 章实体
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = TextbookEntity::class,
            parentColumns = ["id"],
            childColumns = ["textbookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["textbookId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val textbookId: Long,
    val name: String,
    val orderIndex: Int,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

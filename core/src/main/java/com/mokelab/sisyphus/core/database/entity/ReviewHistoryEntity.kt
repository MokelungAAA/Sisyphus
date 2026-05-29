package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 复习历史记录实体
 */
@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(
            entity = ReviewCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val rating: Int,  // 0=AGAIN, 1=HARD, 2=GOOD, 3=EASY
    val stabilityBefore: Float,
    val stabilityAfter: Float,
    val difficultyBefore: Float,
    val difficultyAfter: Float,
    val intervalBefore: Int,
    val intervalAfter: Int,
    val reviewTime: Instant,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

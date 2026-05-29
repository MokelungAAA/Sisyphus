package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 卡片状态
 */
enum class CardState {
    NEW,
    LEARNING,
    REVIEW,
    RELEARNING
}

/**
 * 复习卡片实体 (FSRS)
 */
@Entity(
    tableName = "review_cards",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgePointEntity::class,
            parentColumns = ["id"],
            childColumns = ["knowledgePointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["knowledgePointId"])]
)
data class ReviewCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val knowledgePointId: Long,
    val studyRecordId: Long? = null,
    val stability: Float = 0f,
    val difficulty: Float = 0f,
    val elapsedDays: Int = 0,
    val scheduledDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val state: CardState = CardState.NEW,
    val due: Instant,
    val lastReview: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
)

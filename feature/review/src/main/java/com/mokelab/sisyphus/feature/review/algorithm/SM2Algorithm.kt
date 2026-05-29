package com.mokelab.sisyphus.feature.review.algorithm

import com.mokelab.sisyphus.core.database.entity.CardState
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * SM-2 算法实现（用于迁移阶段）
 *
 * 当卡片数 < 50 时使用 SM-2，超过 50 后切换到 FSRS
 */
object SM2Algorithm {

    /**
     * 计算下次复习
     *
     * @param card 当前卡片
     * @param rating 用户评分
     * @param now 当前时间戳（毫秒）
     * @return 更新后的卡片
     */
    fun nextReview(
        card: ReviewCardEntity,
        rating: Rating,
        now: Long
    ): ReviewCardEntity {
        // 新卡片使用默认参数
        val newDifficulty = if (card.difficulty == 0f) {
            (rating.value.toFloat() / 3f) // 简化：0-1映射
        } else {
            card.difficulty
        }

        // 计算间隔
        val interval = calculateInterval(card, rating)

        // 计算稳定性（近似）
        val newStability = max(1f, interval.toFloat())

        // 确定状态
        val newState = FSRSAlgorithm.determineState(card.state, rating)

        return card.copy(
            stability = newStability,
            difficulty = newDifficulty,
            elapsedDays = 0,
            scheduledDays = interval,
            reps = card.reps + 1,
            lapses = if (rating == Rating.AGAIN) card.lapses + 1 else card.lapses,
            state = newState,
            due = kotlinx.datetime.Instant.fromEpochMilliseconds(now + interval * 86400000L),
            lastReview = kotlinx.datetime.Instant.fromEpochMilliseconds(now)
        )
    }

    /**
     * 计算 SM-2 间隔
     */
    private fun calculateInterval(card: ReviewCardEntity, rating: Rating): Int {
        return when (rating) {
            Rating.AGAIN -> 1
            Rating.HARD -> when (card.reps) {
                0 -> 1
                1 -> 3
                else -> (card.scheduledDays * 1.2f).roundToInt()
            }
            Rating.GOOD -> when (card.reps) {
                0 -> 1
                1 -> 3
                2 -> 7
                else -> (card.scheduledDays * 2.5f).roundToInt()
            }
            Rating.EASY -> when (card.reps) {
                0 -> 1
                1 -> 4
                2 -> 10
                else -> (card.scheduledDays * 3.5f).roundToInt()
            }
        }.coerceIn(1, 36500)
    }
}

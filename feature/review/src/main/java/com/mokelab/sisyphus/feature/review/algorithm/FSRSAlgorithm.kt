package com.mokelab.sisyphus.feature.review.algorithm

import com.mokelab.sisyphus.core.database.entity.CardState
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * FSRS (Free Spaced Repetition Scheduler) 纯函数实现
 *
 * 状态机：NEW → LEARNING → REVIEW → RELEARNING
 * 核心公式基于 Jarrett Ye 的 FSRS-4.5/FSRS-5
 */
object FSRSAlgorithm {

    /**
     * 计算下次复习（纯函数）
     *
     * @param card 当前卡片状态
     * @param rating 用户评分
     * @param params FSRS参数
     * @param now 当前时间戳（毫秒）
     * @return 更新后的卡片状态
     */
    fun nextReview(
        card: ReviewCardEntity,
        rating: Rating,
        params: FSRSParams,
        now: Long
    ): ReviewCardEntity {
        val elapsedDays = card.elapsedDays

        // 1. 计算难度变化
        val newDifficulty = calculateDifficulty(card.difficulty, rating, params)

        // 2. 计算稳定性变化
        val newStability = calculateStability(
            stability = card.stability,
            difficulty = newDifficulty,
            elapsedDays = elapsedDays,
            rating = rating,
            params = params
        )

        // 3. 计算下次复习间隔
        val interval = calculateInterval(newStability, params)

        // 4. 确定新状态
        val newState = determineState(card.state, rating)

        // 5. 返回更新后的卡片
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
     * 计算稳定性
     *
     * 公式：S' = S · (1 + factor · decay · difficultyFactor)
     */
    fun calculateStability(
        stability: Float,
        difficulty: Float,
        elapsedDays: Int,
        rating: Rating,
        params: FSRSParams
    ): Float {
        val w = params.w

        // 新卡片初始稳定性
        if (stability == 0f) {
            return calculateInitialStability(rating, params)
        }

        // 遗忘曲线衰减：d = e^(-elapsedDays / (S · w[0]))
        val decay = exp(-elapsedDays.toFloat() / (stability * w[0]))

        // 基于评分的稳定性增长因子
        val factor = when (rating) {
            Rating.AGAIN -> w[1]
            Rating.HARD -> w[2]
            Rating.GOOD -> w[3]
            Rating.EASY -> w[4]
        }

        // 难度调整：1 - D · w[5]
        val difficultyFactor = 1.0f - difficulty * w[5]

        return stability * (1 + factor * decay * difficultyFactor)
    }

    /**
     * 计算初始稳定性
     *
     * 公式：S₀(G) = w[0] · e^(w[1] · G)  （G = 评分等级对应的值）
     */
    fun calculateInitialStability(rating: Rating, params: FSRSParams): Float {
        val w = params.w
        val g = rating.value.toFloat()
        return (w[0] * exp(w[1] * g)).coerceAtLeast(0.01f)
    }

    /**
     * 计算难度
     *
     * 公式：D' = D + delta · (1 - D)
     */
    fun calculateDifficulty(
        difficulty: Float,
        rating: Rating,
        params: FSRSParams
    ): Float {
        val w = params.w

        // 新卡片初始难度
        if (difficulty == 0f) {
            return calculateInitialDifficulty(rating, params)
        }

        val delta = when (rating) {
            Rating.AGAIN -> w[6]
            Rating.HARD -> w[7]
            Rating.GOOD -> 0f
            Rating.EASY -> -w[8]
        }

        return (difficulty + delta * (1 - difficulty)).coerceIn(0f, 1f)
    }

    /**
     * 计算初始难度
     *
     * 公式：D₀(G) = w[4] - (e^(w[5]·G) - 1) · (w[6]·(G-1))
     */
    fun calculateInitialDifficulty(rating: Rating, params: FSRSParams): Float {
        val w = params.w
        val g = rating.value.toFloat()
        val d = w[4] - (exp(w[5] * g) - 1) * (w[6] * (g - 1))
        return d.coerceIn(0f, 1f)
    }

    /**
     * 计算复习间隔
     *
     * 公式：I = S · (1/requestRetention - 1)
     */
    fun calculateInterval(stability: Float, params: FSRSParams): Int {
        if (stability <= 0f) return 1
        val interval = stability * (1f / params.requestRetention - 1f)
        return interval.roundToInt().coerceIn(1, params.maximumInterval)
    }

    /**
     * 确定新状态
     */
    fun determineState(currentState: CardState, rating: Rating): CardState {
        return when (currentState) {
            CardState.NEW -> when (rating) {
                Rating.AGAIN -> CardState.LEARNING
                Rating.HARD -> CardState.LEARNING
                Rating.GOOD -> CardState.LEARNING
                Rating.EASY -> CardState.REVIEW
            }
            CardState.LEARNING -> when (rating) {
                Rating.AGAIN -> CardState.LEARNING
                Rating.HARD -> CardState.LEARNING
                Rating.GOOD -> CardState.REVIEW
                Rating.EASY -> CardState.REVIEW
            }
            CardState.REVIEW -> when (rating) {
                Rating.AGAIN -> CardState.RELEARNING
                Rating.HARD -> CardState.REVIEW
                Rating.GOOD -> CardState.REVIEW
                Rating.EASY -> CardState.REVIEW
            }
            CardState.RELEARNING -> when (rating) {
                Rating.AGAIN -> CardState.RELEARNING
                Rating.HARD -> CardState.RELEARNING
                Rating.GOOD -> CardState.REVIEW
                Rating.EASY -> CardState.REVIEW
            }
        }
    }

    /**
     * 计算当前回忆概率
     *
     * 公式：R(t) = (1 + t/(9·S))^(-1)
     */
    fun calculateRetrievability(stability: Float, elapsedDays: Int): Float {
        if (stability <= 0f) return 0f
        return (1 + elapsedDays.toFloat() / (9 * stability)).pow(-1f)
    }

    /**
     * 创建新卡片
     */
    fun createNewCard(
        knowledgePointId: Long,
        studyRecordId: Long? = null,
        now: Long = System.currentTimeMillis()
    ): ReviewCardEntity {
        return ReviewCardEntity(
            knowledgePointId = knowledgePointId,
            studyRecordId = studyRecordId,
            stability = 0f,
            difficulty = 0f,
            elapsedDays = 0,
            scheduledDays = 0,
            reps = 0,
            lapses = 0,
            state = CardState.NEW,
            due = kotlinx.datetime.Instant.fromEpochMilliseconds(now),
            lastReview = null,
            createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(now)
        )
    }
}

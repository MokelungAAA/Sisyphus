package com.mokelab.sisyphus.feature.review.algorithm

import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity

/**
 * 复习引擎：自动选择 FSRS 或 SM-2
 *
 * 切换阈值：卡片数 >= 50 时使用 FSRS
 */
object ReviewEngine {

    /** 切换阈值 */
    private const val FSRS_THRESHOLD = 50

    /**
     * 判断是否应使用 FSRS
     *
     * @param totalCards 总卡片数
     * @return true = 使用 FSRS，false = 使用 SM-2
     */
    fun shouldUseFSRS(totalCards: Int): Boolean {
        return totalCards >= FSRS_THRESHOLD
    }

    /**
     * 计算下次复习（自动选择算法）
     *
     * @param card 当前卡片
     * @param rating 用户评分
     * @param params FSRS参数
     * @param totalCards 总卡片数
     * @param now 当前时间戳
     * @return 更新后的卡片
     */
    fun nextReview(
        card: ReviewCardEntity,
        rating: Rating,
        params: FSRSParams,
        totalCards: Int,
        now: Long = System.currentTimeMillis()
    ): ReviewCardEntity {
        return if (shouldUseFSRS(totalCards)) {
            FSRSAlgorithm.nextReview(card, rating, params, now)
        } else {
            SM2Algorithm.nextReview(card, rating, now)
        }
    }
}

package com.mokelab.sisyphus.feature.review.algorithm

/**
 * FSRS 4档评分标准
 */
enum class Rating(val value: Int) {
    AGAIN(0),  // 完全忘记
    HARD(1),   // 勉强记住
    GOOD(2),   // 正常回忆
    EASY(3)    // 轻松回忆
}

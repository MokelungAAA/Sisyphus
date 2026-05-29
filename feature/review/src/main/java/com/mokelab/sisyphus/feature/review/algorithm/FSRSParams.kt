package com.mokelab.sisyphus.feature.review.algorithm

/**
 * FSRS 算法参数
 *
 * @param w 17个可学习参数（Anki默认参数作为冷启动）
 * @param requestRetention 目标记忆保持率（默认0.9）
 * @param maximumInterval 最大间隔天数（默认36500天≈100年）
 */
data class FSRSParams(
    val w: FloatArray = floatArrayOf(
        0.4f, 0.6f, 2.4f, 5.8f, 4.93f, 0.94f, 0.86f, 0.01f,
        1.49f, 0.14f, 0.94f, 2.18f, 0.05f, 0.34f, 1.26f, 0.29f, 2.61f
    ),
    val requestRetention: Float = 0.9f,
    val maximumInterval: Int = 36500
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FSRSParams) return false
        return w.contentEquals(other.w) &&
                requestRetention == other.requestRetention &&
                maximumInterval == other.maximumInterval
    }

    override fun hashCode(): Int {
        var result = w.contentHashCode()
        result = 31 * result + requestRetention.hashCode()
        result = 31 * result + maximumInterval
        return result
    }
}

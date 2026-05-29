package com.mokelab.sisyphus.feature.home.algorithm

/**
 * XP 计算配置
 */
data class XPConfig(
    val baseXP: Float = 100f,           // 基础XP
    val levelMultiplier: Float = 1.5f,  // 等级增长系数
    val timeWeight: Float = 0.3f,       // 时长权重
    val qualityWeight: Float = 0.7f,    // 质量权重
    val inputOutputRatio: Float = 0.3f  // 输入占比（输出为0.7）
)

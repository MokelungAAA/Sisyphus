package com.mokelab.sisyphus.feature.home.algorithm

import com.mokelab.sisyphus.core.database.entity.InputOutputType
import kotlin.math.ln

/**
 * XP 等级系统纯函数实现
 *
 * 等级映射固定，经验曲线自动拟合
 */
object XPAlgorithm {

    /** 等级 → 目标得分率映射（固定硬性指标） */
    private val LEVEL_SCORE_MAP = mapOf(
        40 to 0.50f,
        50 to 0.60f,
        60 to 0.70f,
        70 to 0.80f,
        80 to 0.85f,
        90 to 0.90f,
        100 to 0.95f
    )

    /** 等级称号映射（每10级一个） */
    private val LEVEL_TITLE_MAP = mapOf(
        1 to "初学者",
        10 to "学徒",
        20 to "探索者",
        30 to "实践者",
        40 to "进阶者",
        50 to "熟练者",
        60 to "精通者",
        70 to "专家",
        80 to "大师",
        90 to "宗师",
        100 to "传奇"
    )

    /**
     * 计算 XP
     *
     * @param durationMinutes 学习时长（分钟）
     * @param inputType 输入/输出类型
     * @param subjectWeight 学科权重（0-10，映射到0-1）
     * @param userLevel 用户当前等级
     * @param config XP配置
     * @return 获得的XP
     */
    fun calculateXP(
        durationMinutes: Int,
        inputType: InputOutputType,
        subjectWeight: Float,
        userLevel: Int,
        config: XPConfig = XPConfig()
    ): Float {
        // 1. 基础XP（基于等级曲线）
        val baseXP = config.baseXP * (1 + userLevel * config.levelMultiplier / 100)

        // 2. 时长因素（对数增长，避免无限刷时长）
        val timeFactor = ln(durationMinutes + 1f) / ln(60f)

        // 3. 类型质量系数
        val qualityFactor = when (inputType) {
            InputOutputType.INPUT -> 0.6f   // 输入类系数较低
            InputOutputType.OUTPUT -> 1.2f  // 输出类系数较高
        }

        // 4. 学科权重（0-10映射到0.5-1.5）
        val normalizedWeight = 0.5f + subjectWeight / 10f

        // 5. 最终XP
        return baseXP * timeFactor * qualityFactor * normalizedWeight
    }

    /**
     * 经验曲线自动拟合
     *
     * 基于多个数据源拟合用户当前状态
     *
     * @param examScoreRate 考试得分率（最重要）
     * @param dailyStudyMinutes 每日学习时长
     * @param fsrsRetention FSRS记忆保持率
     * @param taskCompletionRate 任务完成率
     * @param mockExamScore 完整模拟卷得分（可选）
     * @return 综合得分率（0-1）
     */
    fun fitXPLevel(
        examScoreRate: Float,
        dailyStudyMinutes: Float,
        fsrsRetention: Float,
        taskCompletionRate: Float,
        mockExamScore: Float? = null
    ): Float {
        val weights = floatArrayOf(0.4f, 0.15f, 0.15f, 0.15f, 0.15f)
        val scores = floatArrayOf(
            examScoreRate,
            normalizeStudyTime(dailyStudyMinutes),
            fsrsRetention,
            taskCompletionRate,
            mockExamScore ?: examScoreRate  // 无模拟卷则用考试得分率
        )

        return scores.zip(weights).sumOf { (score, weight) -> (score * weight).toDouble() }.toFloat()
    }

    /**
     * 归一化学习时长（0-1）
     *
     * 120分钟以上为满分
     */
    private fun normalizeStudyTime(minutes: Float): Float {
        return (minutes / 120f).coerceIn(0f, 1f)
    }

    /**
     * XP曲线调整策略
     *
     * @param currentLevel 当前等级
     * @param targetScoreRate 目标得分率
     * @param actualScoreRate 实际得分率
     * @return 调整后的XP配置
     */
    fun adjustXPCurve(
        currentLevel: Int,
        targetScoreRate: Float,
        actualScoreRate: Float
    ): XPConfig {
        val gap = targetScoreRate - actualScoreRate

        return when {
            gap > 0.1f -> XPConfig(levelMultiplier = 1.2f)  // 实际低于目标，降低升级速度
            gap < -0.1f -> XPConfig(levelMultiplier = 1.8f) // 实际高于目标，加快升级
            else -> XPConfig(levelMultiplier = 1.5f)        // 正常速度
        }
    }

    /**
     * 获取等级对应的目标得分率
     *
     * @param level 等级
     * @return 目标得分率（0-1）
     */
    fun getTargetScoreRate(level: Int): Float {
        // 找到最接近的映射点
        val sortedLevels = LEVEL_SCORE_MAP.keys.sorted()
        return when {
            level >= sortedLevels.last() -> LEVEL_SCORE_MAP[sortedLevels.last()]!!
            level <= sortedLevels.first() -> LEVEL_SCORE_MAP[sortedLevels.first()]!!
            else -> {
                // 线性插值
                val lower = sortedLevels.last { it <= level }
                val upper = sortedLevels.first { it > level }
                val lowerRate = LEVEL_SCORE_MAP[lower]!!
                val upperRate = LEVEL_SCORE_MAP[upper]!!
                val ratio = (level - lower).toFloat() / (upper - lower)
                lowerRate + ratio * (upperRate - lowerRate)
            }
        }
    }

    /**
     * 获取等级称号
     *
     * @param level 等级
     * @return 称号
     */
    fun getTitle(level: Int): String {
        val sortedLevels = LEVEL_TITLE_MAP.keys.sortedDescending()
        for (titleLevel in sortedLevels) {
            if (level >= titleLevel) {
                return LEVEL_TITLE_MAP[titleLevel]!!
            }
        }
        return "初学者"
    }

    /**
     * 判断是否需要强制考试
     *
     * @param level 当前等级
     * @return true = 需要考试
     */
    fun requiresExam(level: Int): Boolean {
        return level > 0 && level % 10 == 0
    }

    /**
     * 计算升级所需XP
     *
     * @param level 目标等级
     * @param config XP配置
     * @return 所需XP
     */
    fun xpForLevel(level: Int, config: XPConfig = XPConfig()): Float {
        return config.baseXP * level * (1 + level * config.levelMultiplier / 200f)
    }
}

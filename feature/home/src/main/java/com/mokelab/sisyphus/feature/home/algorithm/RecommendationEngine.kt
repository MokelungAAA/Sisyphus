package com.mokelab.sisyphus.feature.home.algorithm

import com.mokelab.sisyphus.core.database.entity.InputOutputType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.roundToInt

/**
 * 推荐类型
 */
enum class RecommendationType {
    FSRS_REVIEW,        // FSRS到期复习
    WEAK_SUBJECT,       // 薄弱学科
    NEW_CONTENT,        // 新内容学习
    EXERCISE,           // 练习输出
    MOCK_EXAM           // 模拟考试
}

/**
 * 推荐项目
 */
data class RecommendationItem(
    val type: RecommendationType,
    val subjectId: Long,
    val knowledgePointId: Long? = null,
    val estimatedMinutes: Int,
    val priority: Float,                  // 优先级分数
    val inputOutputType: InputOutputType
)

/**
 * 平衡警告级别
 */
enum class BalanceWarning {
    NONE,    // 正常
    SOFT,    // 2天纯输入
    STRONG   // 连续3天纯输入
}

/**
 * 每日学习数据
 */
data class DailyStudyData(
    val totalMinutes: Int,
    val dayOfWeek: Int  // 1=周一, 7=周日
)

/**
 * 任务完成数据
 */
data class TaskCompletionData(
    val isSystemRecommended: Boolean,
    val completionRate: Float  // 0-1
)

/**
 * 推荐引擎纯函数实现
 *
 * 基于背包问题建模，多约束优化
 */
object RecommendationEngine {

    /**
     * 计算每日时间预算（分钟）
     *
     * 基于同类型日的历史中位数 + 完成率调整
     */
    fun calculateDailyTimeBudget(
        weeklyStudyData: List<DailyStudyData>,
        dayOfWeek: Int,
        taskHistory: List<TaskCompletionData>
    ): Int {
        // 1. 获取同类型日的历史数据
        val sameDayData = weeklyStudyData.filter { it.dayOfWeek == dayOfWeek }
        if (sameDayData.isEmpty()) return 60 // 默认60分钟

        // 2. 计算中位数
        val medianMinutes = sameDayData.map { it.totalMinutes }.median()

        // 3. 任务完成率调整
        val systemTasks = taskHistory.filter { it.isSystemRecommended }
        val completionRate = if (systemTasks.isEmpty()) {
            0.7f // 默认70%
        } else {
            systemTasks.map { it.completionRate }.average().toFloat()
        }

        // 4. 动态调整因子
        val adjustmentFactor = when {
            completionRate > 0.9f -> 1.2f
            completionRate > 0.7f -> 1.0f
            completionRate > 0.5f -> 0.8f
            else -> 0.6f
        }

        return (medianMinutes * adjustmentFactor).roundToInt().coerceAtLeast(15)
    }

    /**
     * 计算复习紧迫度
     *
     * @param dueDue 到期时间戳（毫秒）
     * @param now 当前时间戳（毫秒）
     * @return 紧迫度（0-1，越高越紧迫）
     */
    fun calculateUrgency(dueDue: Long, now: Long = Clock.System.now().toEpochMilliseconds()): Float {
        val daysOverdue = ((now - dueDue) / 86400000f).coerceAtLeast(0f)
        return 1f / (1f + daysOverdue / 7f)
    }

    /**
     * 生成推荐列表
     *
     * @param dailyBudget 每日时间预算（分钟）
     * @param dueCards 到期的复习卡片列表（subjectId, dueTime, estimatedMinutes）
     * @param subjectWeights 学科权重映射
     * @return 推荐列表（最多5个）
     */
    fun generateRecommendations(
        dailyBudget: Int,
        dueCards: List<Triple<Long, Long, Int>>,  // Triple<subjectId, dueTime, estimatedMinutes>
        subjectWeights: Map<Long, Float>,
        now: Long = Clock.System.now().toEpochMilliseconds()
    ): List<RecommendationItem> {
        val recommendations = mutableListOf<RecommendationItem>()

        // 1. FSRS到期卡片（最高优先级）
        val dueItems = dueCards.filter { it.second <= now }.map { card ->
            RecommendationItem(
                type = RecommendationType.FSRS_REVIEW,
                subjectId = card.first,
                estimatedMinutes = card.third.coerceIn(3, 30),
                priority = calculateUrgency(card.second, now),
                inputOutputType = InputOutputType.INPUT
            )
        }
        recommendations.addAll(dueItems)

        // 2. 薄弱学科补充（权重低于平均值的学科）
        val avgWeight = subjectWeights.values.average().toFloat()
        val weakSubjects = subjectWeights.filter { it.value < avgWeight }.map { (id, weight) ->
            RecommendationItem(
                type = RecommendationType.WEAK_SUBJECT,
                subjectId = id,
                estimatedMinutes = 20,
                priority = 0.5f + (avgWeight - weight) / 10f,
                inputOutputType = InputOutputType.INPUT
            )
        }
        recommendations.addAll(weakSubjects)

        // 3. 输入输出平衡调整
        val inputTotal = recommendations
            .filter { it.inputOutputType == InputOutputType.INPUT }
            .sumOf { it.estimatedMinutes }
        val outputNeeded = ((dailyBudget * 0.7f) - (dailyBudget - inputTotal)).toInt()

        if (outputNeeded > 0) {
            // 为薄弱学科添加输出类推荐
            val weakSubjectIds = subjectWeights.filter { it.value < avgWeight }.keys.take(2)
            for (id in weakSubjectIds) {
                recommendations.add(
                    RecommendationItem(
                        type = RecommendationType.EXERCISE,
                        subjectId = id,
                        estimatedMinutes = (outputNeeded / weakSubjectIds.size.coerceAtLeast(1)).coerceIn(10, 30),
                        priority = 0.4f,
                        inputOutputType = InputOutputType.OUTPUT
                    )
                )
            }
        }

        // 4. 背包选择（贪心近似，最多5个）
        return knapsackSelect(recommendations, dailyBudget).take(5)
    }

    /**
     * 背包选择（贪心近似）
     *
     * 按优先级/时间比排序，贪心填充
     */
    fun knapsackSelect(
        items: List<RecommendationItem>,
        capacity: Int
    ): List<RecommendationItem> {
        val sorted = items.sortedByDescending { it.priority / it.estimatedMinutes }

        val selected = mutableListOf<RecommendationItem>()
        var remaining = capacity

        for (item in sorted) {
            if (item.estimatedMinutes <= remaining) {
                selected.add(item)
                remaining -= item.estimatedMinutes
            }
        }
        return selected
    }

    /**
     * 检查输入输出平衡
     *
     * @param recentRecords 最近的记录列表（inputType, dateStr）
     * @param thresholdDays 检查天数
     * @return 平衡警告级别
     */
    fun checkInputOutputBalance(
        recentRecords: List<Pair<InputOutputType, String>>,  // Pair<inputType, dateStr>
        thresholdDays: Int = 3
    ): BalanceWarning {
        val recentByDay = recentRecords
            .groupBy { it.second }
            .toList()
            .sortedByDescending { it.first }
            .take(thresholdDays)

        val inputDays = recentByDay.count { (_, records) ->
            records.all { it.first == InputOutputType.INPUT }
        }

        return when {
            inputDays >= thresholdDays -> BalanceWarning.STRONG
            inputDays >= 2 -> BalanceWarning.SOFT
            else -> BalanceWarning.NONE
        }
    }

    /**
     * 计算中位数
     */
    private fun List<Int>.median(): Int {
        if (isEmpty()) return 0
        val sorted = sorted()
        val mid = size / 2
        return if (size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2
        } else {
            sorted[mid]
        }
    }
}

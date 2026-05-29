package com.mokelab.sisyphus.feature.recommendation

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 推荐引擎 - 贪心近似背包算法
 *
 * 核心逻辑：
 * 1. 时间预算是硬约束（背包容量）
 * 2. FSRS到期复习最高优先级
 * 3. 剩余时间按学科权重和输入输出比例分配
 * 4. 每次推荐3-5个任务
 */
class RecommendationEngine {

    companion object {
        const val MAX_RECOMMENDATIONS = 5
        const val MIN_RECOMMENDATIONS = 3
        const val DEFAULT_DAILY_MINUTES = 120 // 默认每日学习时间
    }

    /**
     * 生成推荐
     */
    fun generateRecommendations(
        fsrsItems: List<RecommendationItem>,
        subjectWeights: List<SubjectWeight>,
        recentActivities: List<ActivityRecord>,
        timeBudget: TimeBudgetConfig = TimeBudgetConfig(DEFAULT_DAILY_MINUTES)
    ): RecommendationResult {
        val selectedItems = mutableListOf<RecommendationItem>()
        var remainingMinutes = timeBudget.dailyMinutes

        // 第1步：填充FSRS到期复习（最高优先级）
        val fsrsBudget = (timeBudget.dailyMinutes * timeBudget.fsrsPercentage).roundToInt()
        val fsrsSelected = selectFsrsItems(fsrsItems, fsrsBudget)
        selectedItems.addAll(fsrsSelected)
        remainingMinutes -= fsrsSelected.sumOf { it.estimatedMinutes }

        // 第2步：计算学科权重和输入输出比例
        val adjustedWeights = adjustWeightsByHistory(subjectWeights, recentActivities)
        val inputBudget = (remainingMinutes * timeBudget.inputPercentage).roundToInt()
        val outputBudget = (remainingMinutes * timeBudget.outputPercentage).roundToInt()

        // 第3步：按权重分配剩余时间
        val remainingItems = fsrsItems.filter { it !in fsrsSelected }
        val weightedItems = applyWeights(remainingItems, adjustedWeights)

        // 第4步：选择输入型活动
        val inputItems = weightedItems
            .filter { it.category == ActivityCategory.INPUT }
            .sortedByDescending { it.priorityScore }
        val selectedInput = selectByBudget(inputItems, inputBudget)
        selectedItems.addAll(selectedInput)

        // 第5步：选择输出型活动
        val outputItems = weightedItems
            .filter { it.category == ActivityCategory.OUTPUT && it !in selectedInput }
            .sortedByDescending { it.priorityScore }
        val selectedOutput = selectByBudget(outputItems, outputBudget)
        selectedItems.addAll(selectedOutput)

        // 第6步：如果还有剩余空间，填充更多任务
        remainingMinutes -= (selectedInput + selectedOutput).sumOf { it.estimatedMinutes }
        if (remainingMinutes > 0 && selectedItems.size < MAX_RECOMMENDATIONS) {
            val filler = weightedItems
                .filter { it !in selectedItems }
                .sortedByDescending { it.priorityScore }
                .take(MAX_RECOMMENDATIONS - selectedItems.size)
            selectedItems.addAll(filler)
        }

        // 计算最终比例
        val totalMinutes = selectedItems.sumOf { it.estimatedMinutes }
        val inputMinutes = selectedItems
            .filter { it.category == ActivityCategory.INPUT }
            .sumOf { it.estimatedMinutes }
        val outputMinutes = selectedItems
            .filter { it.category == ActivityCategory.OUTPUT }
            .sumOf { it.estimatedMinutes }

        return RecommendationResult(
            items = selectedItems.take(MAX_RECOMMENDATIONS),
            totalEstimatedMinutes = totalMinutes,
            inputPercentage = if (totalMinutes > 0) inputMinutes.toFloat() / totalMinutes else 0f,
            outputPercentage = if (totalMinutes > 0) outputMinutes.toFloat() / totalMinutes else 0f,
            timeBudgetMinutes = timeBudget.dailyMinutes
        )
    }

    /**
     * 选择FSRS到期项目
     */
    private fun selectFsrsItems(
        items: List<RecommendationItem>,
        budget: Int
    ): List<RecommendationItem> {
        var remaining = budget
        return items
            .filter { it.type == RecommendationType.FSRS_REVIEW }
            .sortedByDescending { it.urgencyScore }
            .takeWhile { item ->
                if (remaining >= item.estimatedMinutes) {
                    remaining -= item.estimatedMinutes
                    true
                } else {
                    false
                }
            }
    }

    /**
     * 根据历史调整权重
     */
    private fun adjustWeightsByHistory(
        weights: List<SubjectWeight>,
        recentActivities: List<ActivityRecord>
    ): List<SubjectWeight> {
        // 计算各学科最近学习时间
        val recentMinutes = recentActivities
            .groupBy { it.subjectId }
            .mapValues { (_, records) -> records.sumOf { it.minutes }.toLong() }

        return weights.map { weight ->
            val recent = recentMinutes[weight.subjectId] ?: 0L
            val adjustment = when {
                recent > 60L -> 0.8f  // 学习过多，降低权重
                recent < 10L -> 1.2f  // 学习过少，提高权重
                else -> 1.0f
            }
            weight.copy(weight = (weight.weight * adjustment).coerceIn(0f, 10f))
        }
    }

    /**
     * 应用权重计算优先级
     */
    private fun applyWeights(
        items: List<RecommendationItem>,
        weights: List<SubjectWeight>
    ): List<RecommendationItem> {
        val weightMap = weights.associate { it.subjectId to it.weight }

        return items.map { item ->
            val subjectWeight = weightMap[item.subjectId] ?: 5f
            val weightedPriority = item.priorityScore * (subjectWeight / 5f)
            item.copy(priorityScore = weightedPriority)
        }
    }

    /**
     * 按预算选择项目
     */
    private fun selectByBudget(
        items: List<RecommendationItem>,
        budget: Int
    ): List<RecommendationItem> {
        val selected = mutableListOf<RecommendationItem>()
        var remaining = budget

        for (item in items) {
            if (selected.size >= MAX_RECOMMENDATIONS) break
            if (remaining >= item.estimatedMinutes) {
                selected.add(item)
                remaining -= item.estimatedMinutes
            }
        }

        return selected
    }

    /**
     * 计算紧迫度
     * daysOverdue: 超期天数
     */
    fun calculateUrgency(daysOverdue: Int): Float {
        return 1f / (1f + daysOverdue / 7f)
    }

    /**
     * 计算动态时间上限
     */
    fun calculateDynamicLimit(
        medianMinutes: Int,
        completionRate: Float
    ): Int {
        val adjustmentFactor = when {
            completionRate > 0.9f -> 1.2f
            completionRate > 0.7f -> 1.0f
            completionRate > 0.5f -> 0.8f
            else -> 0.6f
        }
        return (medianMinutes * adjustmentFactor).roundToInt()
    }
}

/**
 * 学习活动记录（用于权重调整）
 */
data class ActivityRecord(
    val subjectId: Long,
    val minutes: Int,
    val timestamp: Long
)

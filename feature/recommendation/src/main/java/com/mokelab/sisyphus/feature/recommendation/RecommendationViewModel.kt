package com.mokelab.sisyphus.feature.recommendation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 推荐UI状态
 */
data class RecommendationUiState(
    val recommendations: RecommendationResult? = null,
    val subjectWeights: List<SubjectWeight> = emptyList(),
    val isLoading: Boolean = false,
    val showWeightEditor: Boolean = false,
    val editingWeight: SubjectWeight? = null,
    val timeBudgetMinutes: Int = RecommendationEngine.DEFAULT_DAILY_MINUTES
)

/**
 * 推荐ViewModel
 */
class RecommendationViewModel : ViewModel() {

    private val engine = RecommendationEngine()

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    /**
     * 加载推荐
     */
    fun loadRecommendations(
        fsrsItems: List<RecommendationItem>,
        recentActivities: List<ActivityRecord>
    ) {
        _uiState.update { it.copy(isLoading = true) }

        val weights = _uiState.value.subjectWeights.ifEmpty {
            generateDefaultWeights(fsrsItems)
        }

        val config = TimeBudgetConfig(
            dailyMinutes = _uiState.value.timeBudgetMinutes
        )

        val result = engine.generateRecommendations(
            fsrsItems = fsrsItems,
            subjectWeights = weights,
            recentActivities = recentActivities,
            timeBudget = config
        )

        _uiState.update {
            it.copy(
                recommendations = result,
                subjectWeights = weights,
                isLoading = false
            )
        }
    }

    /**
     * 更新时间预算
     */
    fun updateTimeBudget(minutes: Int) {
        _uiState.update { it.copy(timeBudgetMinutes = minutes) }
    }

    /**
     * 显示权重编辑器
     */
    fun showWeightEditor() {
        _uiState.update { it.copy(showWeightEditor = true) }
    }

    /**
     * 隐藏权重编辑器
     */
    fun hideWeightEditor() {
        _uiState.update { it.copy(showWeightEditor = false, editingWeight = null) }
    }

    /**
     * 编辑学科权重
     */
    fun editWeight(subjectWeight: SubjectWeight) {
        _uiState.update { it.copy(editingWeight = subjectWeight) }
    }

    /**
     * 更新学科权重
     */
    fun updateWeight(subjectId: Long, newWeight: Float) {
        _uiState.update { state ->
            val updated = state.subjectWeights.map {
                if (it.subjectId == subjectId) {
                    it.copy(weight = newWeight.coerceIn(0f, 10f), isManual = true)
                } else {
                    it
                }
            }
            state.copy(subjectWeights = updated)
        }
    }

    /**
     * 自动推算权重（根据学习时间分布）
     */
    fun autoCalculateWeights(recentActivities: List<ActivityRecord>) {
        val totalMinutes = recentActivities.sumOf { it.minutes }.toFloat()
        if (totalMinutes == 0f) return

        val subjectMinutes = recentActivities
            .groupBy { it.subjectId }
            .mapValues { (_, records) -> records.sumOf { it.minutes }.toLong() }

        _uiState.update { state ->
            val updated = state.subjectWeights.map { weight ->
                val minutes = subjectMinutes[weight.subjectId] ?: 0L
                val ratio = minutes.toFloat() / totalMinutes
                val newWeight = (ratio * 10f).coerceIn(0f, 10f)
                weight.copy(weight = newWeight, isManual = false)
            }
            state.copy(subjectWeights = updated)
        }
    }

    /**
     * 生成默认权重
     */
    private fun generateDefaultWeights(items: List<RecommendationItem>): List<SubjectWeight> {
        return items
            .map { it.subjectId to it.subjectName }
            .distinct()
            .map { (id, name) ->
                SubjectWeight(
                    subjectId = id,
                    subjectName = name,
                    weight = 5f,
                    isManual = false,
                    studyMinutes = 0
                )
            }
    }

    /**
     * 获取输入输出比例描述
     */
    fun getBalanceDescription(): String {
        val result = _uiState.value.recommendations ?: return "暂无推荐"
        val inputPct = (result.inputPercentage * 100).roundToInt()
        val outputPct = (result.outputPercentage * 100).roundToInt()

        return when {
            inputPct > 40 -> "输入型偏多（${inputPct}%:${outputPct}%），建议增加练习"
            inputPct < 20 -> "输出型偏多（${inputPct}%:${outputPct}%），建议增加阅读"
            else -> "输入输出平衡（${inputPct}%:${outputPct}%）"
        }
    }

    private fun Float.roundToInt(): Int = (this * 100).toInt()
}

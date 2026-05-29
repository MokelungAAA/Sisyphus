package com.mokelab.sisyphus.feature.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.repository.KnowledgePointRepository
import com.mokelab.sisyphus.core.database.repository.ReviewCardRepository
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
class RecommendationViewModel(
    private val reviewCardRepository: ReviewCardRepository,
    private val studyRecordRepository: StudyRecordRepository,
    private val knowledgePointRepository: KnowledgePointRepository,
    private val subjectRepository: SubjectRepository,
    private val engine: RecommendationEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    init {
        loadFromRepository()
    }

    /**
     * 从Repository加载数据并生成推荐
     */
    private fun loadFromRepository() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 获取所有复习卡片
                val allCards = reviewCardRepository.getAll().first()

                // 获取所有学科（用于获取学科名称）
                val subjects = subjectRepository.getAll().first()
                val subjectMap = subjects.associateBy { it.id }

                // 转换为推荐项
                val fsrsItems = allCards.mapNotNull { card ->
                    val knowledgePoint = knowledgePointRepository.getById(card.knowledgePointId)
                    val subjectId = knowledgePoint?.sectionId?.let { sectionId ->
                        // 需要通过section -> chapter -> textbook -> subject的路径获取subjectId
                        // 这里简化处理，假设knowledgePoint有直接的subjectId
                        // 实际上需要查询数据库获取
                        null // TODO: 实现正确的subjectId获取
                    }

                    if (subjectId != null) {
                        RecommendationItem(
                            itemId = "fsrs_${card.id}",
                            subjectId = subjectId,
                            subjectName = subjectMap[subjectId]?.name ?: "未知学科",
                            knowledgePointId = card.knowledgePointId,
                            knowledgePointName = knowledgePoint?.name ?: "未知知识点",
                            type = RecommendationType.FSRS_REVIEW,
                            title = "复习：${knowledgePoint?.name ?: "未知知识点"}",
                            description = "FSRS间隔重复复习",
                            estimatedMinutes = 5,
                            priority = RecommendationPriority.HIGH,
                            urgencyScore = calculateUrgency(card),
                            inputOutputRatio = ActivityCategory.OUTPUT
                        )
                    } else {
                        null
                    }
                }

                // 获取今日学习活动记录（用于权重计算）
                val todayRecords = studyRecordRepository.getAll().first()
                val recentActivities = todayRecords.map { record ->
                    ActivityRecord(
                        subjectId = record.subjectId,
                        category = if (record.inputType == com.mokelab.sisyphus.core.database.entity.InputOutputType.INPUT) {
                            ActivityCategory.INPUT
                        } else {
                            ActivityCategory.OUTPUT
                        },
                        minutes = record.durationMinutes
                    )
                }

                // 生成默认权重
                val weights = generateDefaultWeights(fsrsItems, subjects.map { it.id to it.name })

                // 更新配置
                val config = TimeBudgetConfig(
                    dailyMinutes = _uiState.value.timeBudgetMinutes
                )

                // 生成推荐
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
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * 计算FSRS卡片紧急度
     */
    private fun calculateUrgency(card: ReviewCardEntity): Float {
        val now = System.currentTimeMillis()
        val dueTime = card.due.toEpochMilliseconds()
        val elapsed = (now - dueTime).toFloat() / (1000 * 60 * 60 * 24) // 天数
        return if (elapsed > 0) (elapsed / 7f).coerceIn(0f, 1f) else 0f
    }

    /**
     * 加载推荐
     */
    fun loadRecommendations(
        fsrsItems: List<RecommendationItem>,
        recentActivities: List<ActivityRecord>
    ) {
        _uiState.update { it.copy(isLoading = true) }

        val weights = _uiState.value.subjectWeights.ifEmpty {
            generateDefaultWeights(fsrsItems, emptyList())
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
    private fun generateDefaultWeights(
        items: List<RecommendationItem>,
        subjects: List<Pair<Long, String>>
    ): List<SubjectWeight> {
        val itemSubjects = items
            .map { it.subjectId to it.subjectName }
            .distinct()

        val allSubjects = (itemSubjects + subjects).distinctBy { it.first }

        return allSubjects.map { (id, name) ->
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

    /**
     * 刷新推荐
     */
    fun refresh() {
        loadFromRepository()
    }
}

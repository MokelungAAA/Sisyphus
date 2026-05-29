package com.mokelab.sisyphus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.dao.ExamRecordDao
import com.mokelab.sisyphus.core.database.dao.SubjectDao
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import com.mokelab.sisyphus.core.database.entity.ExamType
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 考试统计数据点（用于折线图）
 */
data class ScoreTrendPoint(
    val timestamp: Long,
    val scoreRate: Float,
    val label: String,
    val examName: String
)

/**
 * 学科成绩对比数据（用于柱状图）
 */
data class SubjectScoreData(
    val subjectName: String,
    val avgScoreRate: Float,
    val examCount: Int
)

/**
 * 考试统计 UI 状态
 */
data class ExamStatsUiState(
    val isLoading: Boolean = true,
    val allRecords: List<ExamRecordEntity> = emptyList(),
    val subjects: Map<Long, SubjectEntity> = emptyMap(),
    val trendData: List<ScoreTrendPoint> = emptyList(),
    val subjectComparison: List<SubjectScoreData> = emptyList(),
    val selectedSubjectId: Long? = null,
    val selectedExamType: ExamType? = null,
    val avgScoreRate: Float = 0f,
    val totalExams: Int = 0,
    val bestScoreRate: Float = 0f,
    val improvementRate: Float = 0f
)

class ExamStatsViewModel(
    private val examRecordDao: ExamRecordDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamStatsUiState())
    val uiState: StateFlow<ExamStatsUiState> = _uiState.asStateFlow()

    private var allRecords: List<ExamRecordEntity> = emptyList()
    private var subjectsMap: Map<Long, SubjectEntity> = emptyMap()

    init {
        viewModelScope.launch {
            subjectDao.getAll().collect { subjects ->
                subjectsMap = subjects.associateBy { it.id }
                refreshStats()
            }
        }
        viewModelScope.launch {
            examRecordDao.getAll().collect { records ->
                allRecords = records
                refreshStats()
            }
        }
    }

    fun selectSubject(subjectId: Long?) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
        refreshStats()
    }

    fun selectExamType(examType: ExamType?) {
        _uiState.value = _uiState.value.copy(selectedExamType = examType)
        refreshStats()
    }

    private fun refreshStats() {
        val filtered = allRecords.filter { record ->
            val matchSubject = _uiState.value.selectedSubjectId?.let { record.subjectId == it } ?: true
            val matchType = _uiState.value.selectedExamType?.let { record.examType == it } ?: true
            matchSubject && matchType
        }

        val trend = buildTrendData(filtered)
        val comparison = buildSubjectComparison(allRecords) // 始终显示全部学科对比

        val avgRate = if (filtered.isNotEmpty()) filtered.map { it.scoreRate }.average().toFloat() else 0f
        val bestRate = filtered.maxOfOrNull { it.scoreRate } ?: 0f
        val improvement = calculateImprovement(filtered)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            allRecords = filtered,
            subjects = subjectsMap,
            trendData = trend,
            subjectComparison = comparison,
            avgScoreRate = avgRate,
            totalExams = filtered.size,
            bestScoreRate = bestRate,
            improvementRate = improvement
        )
    }

    private fun buildTrendData(records: List<ExamRecordEntity>): List<ScoreTrendPoint> {
        return records
            .sortedBy { it.examDate }
            .map { record ->
                val dt = record.examDate.toLocalDateTime(TimeZone.currentSystemDefault())
                ScoreTrendPoint(
                    timestamp = record.examDate.toEpochMilliseconds(),
                    scoreRate = record.scoreRate,
                    label = "${dt.monthNumber}/${dt.dayOfMonth}",
                    examName = record.examName
                )
            }
    }

    private fun buildSubjectComparison(records: List<ExamRecordEntity>): List<SubjectScoreData> {
        return records
            .groupBy { it.subjectId }
            .map { (subjectId, exams) ->
                val subjectName = subjectsMap[subjectId]?.name ?: "未知学科"
                val avgRate = exams.map { it.scoreRate }.average().toFloat()
                SubjectScoreData(
                    subjectName = subjectName,
                    avgScoreRate = avgRate,
                    examCount = exams.size
                )
            }
            .sortedByDescending { it.avgScoreRate }
    }

    private fun calculateImprovement(records: List<ExamRecordEntity>): Float {
        if (records.size < 2) return 0f
        val sorted = records.sortedBy { it.examDate }
        val recentCount = (sorted.size / 3).coerceAtLeast(1)
        val earlyAvg = sorted.take(recentCount).map { it.scoreRate }.average().toFloat()
        val lateAvg = sorted.takeLast(recentCount).map { it.scoreRate }.average().toFloat()
        return lateAvg - earlyAvg
    }
}

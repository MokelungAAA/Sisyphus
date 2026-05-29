package com.mokelab.sisyphus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.repository.PomodoroSessionRepository
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * 学习统计数据点
 */
data class StudyTrendPoint(
    val date: LocalDate,
    val studyMinutes: Int,
    val pomodoroMinutes: Int
)

/**
 * 学科时间分布数据
 */
data class SubjectTimeData(
    val subjectId: Long,
    val subjectName: String,
    val totalMinutes: Int,
    val percentage: Float
)

/**
 * 学习统计UI状态
 */
data class StudyStatsUiState(
    val isLoading: Boolean = true,
    val weekTrend: List<StudyTrendPoint> = emptyList(),
    val monthTrend: List<StudyTrendPoint> = emptyList(),
    val subjectDistribution: List<SubjectTimeData> = emptyList(),
    val totalStudyMinutes: Int = 0,
    val totalPomodoroMinutes: Int = 0,
    val errorMessage: String? = null
)

/**
 * 学习统计ViewModel
 */
class StudyStatsViewModel(
    private val studyRecordRepository: StudyRecordRepository,
    private val pomodoroSessionRepository: PomodoroSessionRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyStatsUiState())
    val uiState: StateFlow<StudyStatsUiState> = _uiState

    private val timeZone = TimeZone.currentSystemDefault()
    private val today: LocalDate = Clock.System.now().toLocalDateTime(timeZone).date

    init {
        loadStats()
    }

    fun refresh() {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 并行加载数据
            launch { loadStudyRecords() }
            launch { loadPomodoroSessions() }
        }
    }

    private suspend fun loadStudyRecords() {
        studyRecordRepository.getAll()
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "加载学习记录失败: ${e.message}"
                )
            }
            .collect { records ->
                processStudyRecords(records)
            }
    }

    private suspend fun loadPomodoroSessions() {
        pomodoroSessionRepository.getAll()
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "加载番茄钟记录失败: ${e.message}"
                )
            }
            .collect { sessions ->
                processPomodoroSessions(sessions)
            }
    }

    private suspend fun processStudyRecords(records: List<StudyRecordEntity>) {
        // 按日期分组统计学习时长
        val dailyStudyMinutes = records
            .groupBy { record -> instantToLocalDate(record.startTime) }
            .mapValues { (_, dayRecords) ->
                dayRecords.sumOf { it.durationMinutes }
            }

        // 计算最近7天趋势
        val weekTrend = calculateTrend(dailyStudyMinutes, days = 7)

        // 计算最近30天趋势
        val monthTrend = calculateTrend(dailyStudyMinutes, days = 30)

        // 计算各科时间分布
        val subjects = subjectRepository.getAll().first()
        val subjectDistribution = calculateSubjectDistribution(records, subjects)

        // 计算总时长
        val totalStudyMinutes = records.sumOf { it.durationMinutes }

        _uiState.value = _uiState.value.copy(
            weekTrend = weekTrend,
            monthTrend = monthTrend,
            subjectDistribution = subjectDistribution,
            totalStudyMinutes = totalStudyMinutes,
            isLoading = false
        )
    }

    private suspend fun processPomodoroSessions(sessions: List<PomodoroSessionEntity>) {
        val totalPomodoroMinutes = sessions.sumOf { it.durationMinutes }

        // 将番茄钟时间加入趋势
        val dailyPomodoroMinutes = sessions
            .groupBy { session -> instantToLocalDate(session.startTime) }
            .mapValues { (_, daySessions) ->
                daySessions.sumOf { it.actualMinutes }
            }

        // 更新趋势数据，加入番茄钟时间
        val currentWeekTrend = _uiState.value.weekTrend.toMutableList()
        val updatedWeekTrend = currentWeekTrend.map { point ->
            val pomodoroMin = dailyPomodoroMinutes[point.date] ?: 0
            point.copy(pomodoroMinutes = pomodoroMin)
        }

        val currentMonthTrend = _uiState.value.monthTrend.toMutableList()
        val updatedMonthTrend = currentMonthTrend.map { point ->
            val pomodoroMin = dailyPomodoroMinutes[point.date] ?: 0
            point.copy(pomodoroMinutes = pomodoroMin)
        }

        _uiState.value = _uiState.value.copy(
            weekTrend = updatedWeekTrend,
            monthTrend = updatedMonthTrend,
            totalPomodoroMinutes = totalPomodoroMinutes
        )
    }

    private fun instantToLocalDate(instant: Instant): LocalDate {
        return instant.toLocalDateTime(timeZone).date
    }

    private fun calculateTrend(
        dailyMinutes: Map<LocalDate, Int>,
        days: Int
    ): List<StudyTrendPoint> {
        return (0 until days).map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val studyMin = dailyMinutes[date] ?: 0
            StudyTrendPoint(
                date = date,
                studyMinutes = studyMin,
                pomodoroMinutes = 0 // 番茄钟数据会单独更新
            )
        }.reversed()
    }

    private fun calculateSubjectDistribution(
        records: List<StudyRecordEntity>,
        subjects: List<SubjectEntity>
    ): List<SubjectTimeData> {
        val subjectMap = subjects.associateBy { it.id }
        val totalMinutes = records.sumOf { it.durationMinutes }

        if (totalMinutes == 0) return emptyList()

        return records
            .groupBy { it.subjectId }
            .map { (subjectId, subjectRecords) ->
                val subjectName = subjectMap[subjectId]?.name ?: "未知学科"
                val minutes = subjectRecords.sumOf { it.durationMinutes }
                SubjectTimeData(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    totalMinutes = minutes,
                    percentage = minutes.toFloat() / totalMinutes
                )
            }
            .sortedByDescending { data: SubjectTimeData -> data.totalMinutes }
    }
}

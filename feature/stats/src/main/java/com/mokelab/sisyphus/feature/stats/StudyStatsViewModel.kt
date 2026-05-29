package com.mokelab.sisyphus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.*
import com.mokelab.sisyphus.core.database.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * 学习趋势数据点
 */
data class StudyTrendPoint(
    val date: LocalDate,
    val studyMinutes: Int,
    val pomodoroMinutes: Int
)

/**
 * 学科时间分布
 */
data class SubjectTimeData(
    val subjectId: Long,
    val subjectName: String,
    val totalMinutes: Int,
    val percentage: Float
)

/**
 * 学科进度数据
 */
data class SubjectProgressData(
    val subjectId: Long,
    val subjectName: String,
    val totalKnowledgePoints: Int,
    val reviewedCount: Int,
    val masteredCount: Int,
    val progressPercent: Float
)

/**
 * FSRS统计数据
 */
data class FSRSSummary(
    val totalCards: Int,
    val newCards: Int,
    val learningCards: Int,
    val reviewCards: Int,
    val masteredCards: Int,
    val averageStability: Float,
    val averageDifficulty: Float
)

/**
 * 阅读统计数据
 */
data class ReadingStatsSummary(
    val totalMinutes: Int,
    val totalBooks: Int,
    val bookNames: List<String>,
    val recentRecords: List<ReadingRecordEntity>
)

/**
 * 洞察页UI状态
 */
data class StudyStatsUiState(
    val weekTrend: List<StudyTrendPoint> = emptyList(),
    val monthTrend: List<StudyTrendPoint> = emptyList(),
    val subjectDistribution: List<SubjectTimeData> = emptyList(),
    val todayStudyMinutes: Int = 0,
    val todayPomodoroMinutes: Int = 0,
    val weekStudyMinutes: Int = 0,
    val weekPomodoroMinutes: Int = 0,
    val recentRecords: List<StudyRecordEntity> = emptyList(),
    val subjectProgress: List<SubjectProgressData> = emptyList(),
    val fsrsSummary: FSRSSummary = FSRSSummary(0, 0, 0, 0, 0, 0f, 0f),
    val weakKnowledgePoints: List<KnowledgePointEntity> = emptyList(),
    val readingSummary: ReadingStatsSummary = ReadingStatsSummary(0, 0, emptyList(), emptyList()),
    val isLoading: Boolean = true
)

/**
 * 学习统计ViewModel
 */
class StudyStatsViewModel(
    private val studyRecordRepository: StudyRecordRepository,
    private val pomodoroSessionRepository: PomodoroSessionRepository,
    private val subjectRepository: SubjectRepository,
    private val knowledgePointRepository: KnowledgePointRepository,
    private val reviewCardRepository: ReviewCardRepository,
    private val readingRecordRepository: ReadingRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyStatsUiState())
    val uiState: StateFlow<StudyStatsUiState> = _uiState

    private val timeZone = TimeZone.currentSystemDefault()
    private val today: LocalDate = Clock.System.now().toLocalDateTime(timeZone).date

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                studyRecordRepository.getAll(),
                pomodoroSessionRepository.getAll(),
                subjectRepository.getAll(),
                reviewCardRepository.getAll(),
                readingRecordRepository.getAll()
            ) { studyRecords, pomodoroSessions, subjects, reviewCards, readingRecords ->
                val subjectMap = subjects.associateBy { it.id }

                // 计算趋势
                val weekTrend = calculateTrend(studyRecords, pomodoroSessions, 7)
                val monthTrend = calculateTrend(studyRecords, pomodoroSessions, 30)

                // 计算学科分布
                val subjectDistribution = calculateSubjectDistribution(studyRecords, subjectMap)

                // 计算今日/本周数据
                val todayStudy = studyRecords
                    .filter { it.startTime.toLocalDateTime(timeZone).date == today }
                    .sumOf { it.durationMinutes }
                val todayPomodoro = pomodoroSessions
                    .filter { it.startTime.toLocalDateTime(timeZone).date == today }
                    .sumOf { it.actualMinutes }
                val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                val weekStudy = studyRecords
                    .filter { it.startTime.toLocalDateTime(timeZone).date >= weekStart }
                    .sumOf { it.durationMinutes }
                val weekPomodoro = pomodoroSessions
                    .filter { it.startTime.toLocalDateTime(timeZone).date >= weekStart }
                    .sumOf { it.actualMinutes }

                // 最近记录
                val recentRecords = studyRecords.sortedByDescending { it.startTime }.take(10)

                // FSRS统计
                val fsrsSummary = calculateFSRSSummary(reviewCards)

                // 阅读统计
                val readingSummary = calculateReadingSummary(readingRecords)

                StudyStatsUiState(
                    weekTrend = weekTrend,
                    monthTrend = monthTrend,
                    subjectDistribution = subjectDistribution,
                    todayStudyMinutes = todayStudy,
                    todayPomodoroMinutes = todayPomodoro,
                    weekStudyMinutes = weekStudy,
                    weekPomodoroMinutes = weekPomodoro,
                    recentRecords = recentRecords,
                    fsrsSummary = fsrsSummary,
                    readingSummary = readingSummary,
                    isLoading = false
                )
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateTrend(
        studyRecords: List<StudyRecordEntity>,
        pomodoroSessions: List<PomodoroSessionEntity>,
        days: Int
    ): List<StudyTrendPoint> {
        return (0 until days).map { offset ->
            val date = today.minus(offset, DateTimeUnit.DAY)
            val study = studyRecords
                .filter { it.startTime.toLocalDateTime(timeZone).date == date }
                .sumOf { it.durationMinutes }
            val pomodoro = pomodoroSessions
                .filter { it.startTime.toLocalDateTime(timeZone).date == date }
                .sumOf { it.actualMinutes }
            StudyTrendPoint(date, study, pomodoro)
        }.reversed()
    }

    private fun calculateSubjectDistribution(
        studyRecords: List<StudyRecordEntity>,
        subjectMap: Map<Long, SubjectEntity>
    ): List<SubjectTimeData> {
        val totalMinutes = studyRecords.sumOf { it.durationMinutes }
        if (totalMinutes == 0) return emptyList()

        return studyRecords
            .groupBy { it.subjectId }
            .map { (subjectId, records) ->
                val minutes = records.sumOf { it.durationMinutes }
                SubjectTimeData(
                    subjectId = subjectId,
                    subjectName = subjectMap[subjectId]?.name ?: "未知",
                    totalMinutes = minutes,
                    percentage = minutes.toFloat() / totalMinutes
                )
            }
            .sortedByDescending { it.totalMinutes }
    }

    private fun calculateFSRSSummary(reviewCards: List<ReviewCardEntity>): FSRSSummary {
        if (reviewCards.isEmpty()) {
            return FSRSSummary(0, 0, 0, 0, 0, 0f, 0f)
        }
        return FSRSSummary(
            totalCards = reviewCards.size,
            newCards = reviewCards.count { it.state == CardState.NEW },
            learningCards = reviewCards.count { it.state == CardState.LEARNING || it.state == CardState.RELEARNING },
            reviewCards = reviewCards.count { it.state == CardState.REVIEW },
            masteredCards = reviewCards.count { it.state == CardState.REVIEW && it.stability > 30f },
            averageStability = reviewCards.map { it.stability }.average().toFloat(),
            averageDifficulty = reviewCards.map { it.difficulty }.average().toFloat()
        )
    }

    private fun calculateReadingSummary(readingRecords: List<ReadingRecordEntity>): ReadingStatsSummary {
        if (readingRecords.isEmpty()) {
            return ReadingStatsSummary(0, 0, emptyList(), emptyList())
        }
        return ReadingStatsSummary(
            totalMinutes = readingRecords.sumOf { it.durationMinutes },
            totalBooks = readingRecords.map { it.bookName }.distinct().size,
            bookNames = readingRecords.map { it.bookName }.distinct(),
            recentRecords = readingRecords.sortedByDescending { it.startTime }.take(5)
        )
    }
}

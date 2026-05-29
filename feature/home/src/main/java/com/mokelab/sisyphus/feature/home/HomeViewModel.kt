package com.mokelab.sisyphus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.repository.PomodoroSessionRepository
import com.mokelab.sisyphus.core.database.repository.ReadingRecordRepository
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    val totalXP: Int = 0,
    val todayXP: Int = 0,
    val level: Int = 1,
    val title: String = "学习新手",
    val streakDays: Int = 0,
    val subjects: List<SubjectEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val subjectRepository: SubjectRepository,
    private val studyRecordRepository: StudyRecordRepository,
    private val pomodoroSessionRepository: PomodoroSessionRepository,
    private val readingRecordRepository: ReadingRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load subjects
            launch {
                subjectRepository.getAll()
                    .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
                    .collectLatest { subjects ->
                        _uiState.value = _uiState.value.copy(subjects = subjects)
                    }
            }

            // Load study records and calculate XP
            launch {
                studyRecordRepository.getAll()
                    .catch { /* ignore */ }
                    .collectLatest { records ->
                        val totalMinutes = records.sumOf { it.durationMinutes }
                        val todayMinutes = calculateTodayMinutes(records)
                        val level = calculateLevel(totalMinutes)
                        val title = getTitleForLevel(level)
                        _uiState.value = _uiState.value.copy(
                            totalXP = totalMinutes,
                            todayXP = todayMinutes,
                            level = level,
                            title = title
                        )
                    }
            }

            // Calculate streak from all data sources
            launch {
                calculateStreakFromAllSources()
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun calculateStreakFromAllSources() {
        combine(
            studyRecordRepository.getAll(),
            pomodoroSessionRepository.getAll(),
            readingRecordRepository.getAll()
        ) { studyRecords, pomodoroSessions, readingRecords ->
            val timeZone = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val localNow = now.toLocalDateTime(timeZone)

            // 凌晨0-6点算前一天
            val effectiveDate = if (localNow.hour < 6) {
                localNow.date.minus(kotlinx.datetime.DatePeriod(days = 1))
            } else {
                localNow.date
            }

            // Collect all dates with any activity, applying the 00:00-06:00 rule
            val allDates = mutableSetOf<LocalDate>()

            // Study records dates
            studyRecords.forEach { record ->
                val recordLocal = record.startTime.toLocalDateTime(timeZone)
                val recordDate = if (recordLocal.hour < 6) {
                    recordLocal.date.minus(kotlinx.datetime.DatePeriod(days = 1))
                } else {
                    recordLocal.date
                }
                allDates.add(recordDate)
            }

            // Pomodoro sessions dates
            pomodoroSessions.forEach { session ->
                val sessionLocal = session.startTime.toLocalDateTime(timeZone)
                val sessionDate = if (sessionLocal.hour < 6) {
                    sessionLocal.date.minus(kotlinx.datetime.DatePeriod(days = 1))
                } else {
                    sessionLocal.date
                }
                allDates.add(sessionDate)
            }

            // Reading records dates
            readingRecords.forEach { record ->
                val recordLocal = record.createdAt.toLocalDateTime(timeZone)
                val recordDate = if (recordLocal.hour < 6) {
                    recordLocal.date.minus(kotlinx.datetime.DatePeriod(days = 1))
                } else {
                    recordLocal.date
                }
                allDates.add(recordDate)
            }

            // Calculate streak using effective date
            calculateStreak(allDates.toList(), effectiveDate)
        }
        .catch { /* ignore */ }
        .collectLatest { streak ->
            _uiState.value = _uiState.value.copy(streakDays = streak)
        }
    }

    private fun calculateTodayMinutes(records: List<StudyRecordEntity>): Int {
        val now = Clock.System.now()
        val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())

        // 凌晨0-6点算前一天
        val effectiveDate = if (localNow.hour < 6) {
            localNow.date.minus(kotlinx.datetime.DatePeriod(days = 1))
        } else {
            localNow.date
        }

        return records
            .filter { record ->
                val recordLocal = record.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                val recordDate = if (recordLocal.hour < 6) {
                    recordLocal.date.minus(kotlinx.datetime.DatePeriod(days = 1))
                } else {
                    recordLocal.date
                }
                recordDate == effectiveDate
            }
            .sumOf { it.durationMinutes }
    }

    private fun calculateLevel(totalXP: Int): Int {
        return when {
            totalXP < 100 -> 1
            totalXP < 500 -> 2
            totalXP < 1500 -> 3
            totalXP < 3000 -> 4
            totalXP < 5000 -> 5
            totalXP < 8000 -> 6
            totalXP < 12000 -> 7
            totalXP < 18000 -> 8
            totalXP < 25000 -> 9
            else -> 10
        }
    }

    private fun getTitleForLevel(level: Int): String {
        return when (level) {
            1 -> "学习新手"
            2 -> "知识探索者"
            3 -> "勤奋学者"
            4 -> "学习达人"
            5 -> "知识猎手"
            6 -> "学习大师"
            7 -> "知识王者"
            8 -> "学习传奇"
            9 -> "知识之巅"
            else -> "学习之神"
        }
    }

    /**
     * Calculate streak from all data sources.
     * Rules:
     * - Any data counts as valid (study records, pomodoro sessions, reading records)
     * - Missing a day resets streak to 0
     * - 00:00-06:00 counts as previous day
     */
    private fun calculateStreak(dates: List<LocalDate>, today: LocalDate): Int {
        if (dates.isEmpty()) return 0

        // Get unique dates, sorted descending
        val uniqueDates = dates.distinct().sortedDescending()

        if (uniqueDates.isEmpty()) return 0

        // Check if the most recent record is today or yesterday
        val daysSinceLatest = today.toEpochDays() - uniqueDates[0].toEpochDays()
        if (daysSinceLatest > 1) return 0

        // Count consecutive days
        var streak = 1
        for (i in 1 until uniqueDates.size) {
            val diff = uniqueDates[i - 1].toEpochDays() - uniqueDates[i].toEpochDays()
            if (diff == 1) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}

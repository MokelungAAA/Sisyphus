package com.mokelab.sisyphus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.repository.ReadingRecordRepository
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
    val error: String? = null
)

class HomeViewModel(
    private val subjectRepository: SubjectRepository,
    private val studyRecordRepository: StudyRecordRepository,
    private val readingRecordRepository: ReadingRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
        calculateXP()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            subjectRepository.getAll()
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
                .collectLatest { subjects ->
                    _uiState.value = _uiState.value.copy(subjects = subjects)
                }
        }
    }

    private fun calculateXP() {
        viewModelScope.launch {
            studyRecordRepository.getAll()
                .catch { /* ignore */ }
                .collectLatest { records ->
                    val totalMinutes = records.sumOf { it.durationMinutes }
                    val todayMinutes = calculateTodayMinutes(records)
                    val level = calculateLevel(totalMinutes)
                    val title = getTitleForLevel(level)
                    val streak = calculateStreak(records)
                    _uiState.value = _uiState.value.copy(
                        totalXP = totalMinutes,
                        todayXP = todayMinutes,
                        level = level,
                        title = title,
                        streakDays = streak
                    )
                }
        }
    }

    private fun calculateTodayMinutes(records: List<StudyRecordEntity>): Int {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return records
            .filter { record ->
                val recordDate = record.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
                recordDate == today
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

    private fun calculateStreak(records: List<StudyRecordEntity>): Int {
        if (records.isEmpty()) return 0

        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        // Get unique dates with records, sorted descending
        val recordDates = records
            .map { it.startTime.toLocalDateTime(timeZone).date }
            .distinct()
            .sortedDescending()

        if (recordDates.isEmpty()) return 0

        // Check if the most recent record is today or yesterday
        val daysSinceLatest = today.toEpochDays() - recordDates[0].toEpochDays()
        if (daysSinceLatest > 1) return 0

        // Count consecutive days
        var streak = 1
        for (i in 1 until recordDates.size) {
            val diff = recordDates[i - 1].toEpochDays() - recordDates[i].toEpochDays()
            if (diff == 1) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}

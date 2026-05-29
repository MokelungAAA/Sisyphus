package com.mokelab.sisyphus.feature.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import com.mokelab.sisyphus.core.database.entity.PresetType
import com.mokelab.sisyphus.core.database.repository.PomodoroSessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PomodoroUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val completedSessions: Int = 0,
    val currentSubjectId: Long = 0,
    val showSettings: Boolean = false
)

class PomodoroViewModel(
    private val repository: PomodoroSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun setSubject(subjectId: Long) {
        _uiState.value = _uiState.value.copy(currentSubjectId = subjectId)
    }

    fun startTimer(minutes: Int = 25) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            isPaused = false,
            remainingSeconds = minutes * 60,
            totalSeconds = minutes * 60
        )
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }
            completeSession()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isPaused = true)
    }

    fun resumeTimer() {
        _uiState.value = _uiState.value.copy(isPaused = false)
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }
            completeSession()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = _uiState.value.totalSeconds
        )
    }

    private fun completeSession() {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                PomodoroSessionEntity(
                    subjectId = _uiState.value.currentSubjectId,
                    studyRecordId = null,
                    durationMinutes = _uiState.value.totalSeconds / 60,
                    actualMinutes = _uiState.value.totalSeconds / 60,
                    startTime = now,
                    endTime = now,
                    isCompleted = true,
                    presetType = PresetType.CUSTOM,
                    createdAt = now
                )
            )
            _uiState.value = _uiState.value.copy(
                isRunning = false,
                isPaused = false,
                completedSessions = _uiState.value.completedSessions + 1,
                remainingSeconds = _uiState.value.totalSeconds
            )
        }
    }

    fun showSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }

    fun hideSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

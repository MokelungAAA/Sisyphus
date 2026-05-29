package com.mokelab.sisyphus.feature.pomodoro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import com.mokelab.sisyphus.core.database.entity.PresetType
import com.mokelab.sisyphus.core.database.repository.PomodoroSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PomodoroUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val completedSessions: Int = 0,
    val currentSubjectId: Long = 0,
    val currentSubjectName: String = "",
    val selectedPreset: PomodoroPreset = PomodoroPresets.CLASSIC,
    val showSettings: Boolean = false,
    val showPresetSelector: Boolean = false,
    val isBound: Boolean = false,
    val isOnBreak: Boolean = false,
    val showCompletionAnimation: Boolean = false,
    val showGroupCompleteAnimation: Boolean = false
)

class PomodoroViewModel(
    private val repository: PomodoroSessionRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var pomodoroService: PomodoroService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PomodoroService.PomodoroBinder
            pomodoroService = binder.getService()
            isBound = true

            pomodoroService?.setOnCompleteListener {
                onTimerComplete()
            }
            pomodoroService?.setOnGroupCompleteListener {
                onGroupComplete()
            }

            // Sync state from service
            pomodoroService?.let { svc ->
                viewModelScope.launch {
                    svc.state.collect { serviceState ->
                        _uiState.update {
                            it.copy(
                                isRunning = serviceState.isRunning,
                                isPaused = serviceState.isPaused,
                                remainingSeconds = serviceState.remainingSeconds,
                                totalSeconds = serviceState.durationMinutes * 60,
                                completedSessions = serviceState.completedSessions,
                                isOnBreak = serviceState.isOnBreak
                            )
                        }
                    }
                }
            }

            _uiState.update { it.copy(isBound = true) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pomodoroService = null
            isBound = false
            _uiState.update { it.copy(isBound = false) }
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(context, PomodoroService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun setSubject(subjectId: Long, subjectName: String) {
        _uiState.update { it.copy(currentSubjectId = subjectId, currentSubjectName = subjectName) }
    }

    fun selectPreset(preset: PomodoroPreset) {
        _uiState.update {
            it.copy(
                selectedPreset = preset,
                totalSeconds = preset.focusMinutes * 60,
                remainingSeconds = preset.focusMinutes * 60,
                showPresetSelector = false
            )
        }
    }

    fun startTimer() {
        val state = _uiState.value
        pomodoroService?.startTimer(
            subjectId = state.currentSubjectId,
            subjectName = state.currentSubjectName,
            durationMinutes = state.selectedPreset.focusMinutes
        )
    }

    fun pauseTimer() {
        pomodoroService?.pauseTimer()
    }

    fun resumeTimer() {
        pomodoroService?.resumeTimer()
    }

    fun stopTimer() {
        pomodoroService?.stopTimer()
    }

    private fun onTimerComplete() {
        viewModelScope.launch {
            val state = _uiState.value
            val now = Clock.System.now()
            val presetType = when (state.selectedPreset) {
                PomodoroPresets.CLASSIC -> PresetType.CLASSIC
                PomodoroPresets.SHORT -> PresetType.SHORT
                PomodoroPresets.LONG -> PresetType.LONG
                else -> PresetType.CUSTOM
            }

            repository.insert(
                PomodoroSessionEntity(
                    subjectId = state.currentSubjectId,
                    studyRecordId = null,
                    durationMinutes = state.selectedPreset.focusMinutes,
                    actualMinutes = state.selectedPreset.focusMinutes,
                    startTime = now,
                    endTime = now,
                    isCompleted = true,
                    presetType = presetType,
                    createdAt = now
                )
            )

            // Show completion animation
            _uiState.update { it.copy(showCompletionAnimation = true) }
        }
    }

    private fun onGroupComplete() {
        _uiState.update { it.copy(showGroupCompleteAnimation = true) }
    }

    fun dismissCompletionAnimation() {
        _uiState.update { it.copy(showCompletionAnimation = false) }
    }

    fun dismissGroupCompleteAnimation() {
        _uiState.update { it.copy(showGroupCompleteAnimation = false) }
    }

    fun startBreak(isLongBreak: Boolean) {
        pomodoroService?.startBreak(isLongBreak)
    }

    fun showSettings() {
        _uiState.update { it.copy(showSettings = true) }
    }

    fun hideSettings() {
        _uiState.update { it.copy(showSettings = false) }
    }

    fun showPresetSelector() {
        _uiState.update { it.copy(showPresetSelector = true) }
    }

    fun hidePresetSelector() {
        _uiState.update { it.copy(showPresetSelector = false) }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
}
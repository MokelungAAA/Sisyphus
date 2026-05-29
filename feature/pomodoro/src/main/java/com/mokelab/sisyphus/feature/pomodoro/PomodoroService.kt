package com.mokelab.sisyphus.feature.pomodoro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PomodoroState(
    val subjectId: Long = 0,
    val subjectName: String = "",
    val durationMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val completedSessions: Int = 0
)

class PomodoroService : Service() {

    private val binder = PomodoroBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var onComplete: (() -> Unit)? = null

    inner class PomodoroBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    fun setOnCompleteListener(listener: () -> Unit) {
        onComplete = listener
    }

    fun startTimer(subjectId: Long, subjectName: String, durationMinutes: Int) {
        timerJob?.cancel()
        _state.update {
            PomodoroState(
                subjectId = subjectId,
                subjectName = subjectName,
                durationMinutes = durationMinutes,
                remainingSeconds = durationMinutes * 60,
                isRunning = true,
                isPaused = false,
                completedSessions = it.completedSessions
            )
        }

        timerJob = serviceScope.launch {
            while (_state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                updateNotification()
            }
            onTimerComplete()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false, isPaused = true) }
        updateNotification()
    }

    fun resumeTimer() {
        val currentState = _state.value
        if (currentState.isPaused && currentState.remainingSeconds > 0) {
            _state.update { it.copy(isRunning = true, isPaused = false) }

            timerJob = serviceScope.launch {
                while (_state.value.remainingSeconds > 0) {
                    delay(1000)
                    _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                    updateNotification()
                }
                onTimerComplete()
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _state.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                remainingSeconds = it.durationMinutes * 60
            )
        }
        updateNotification()
    }

    private fun onTimerComplete() {
        _state.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                completedSessions = it.completedSessions + 1,
                remainingSeconds = it.durationMinutes * 60
            )
        }
        onComplete?.invoke()
        updateNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄钟",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "番茄钟计时通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val state = _state.value
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        val timeText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

        val statusText = when {
            state.isRunning -> "专注中 - $timeText"
            state.isPaused -> "已暂停 - $timeText"
            else -> "番茄钟就绪"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("番茄钟")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(state.isRunning || state.isPaused)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        const val NOTIFICATION_ID = 1001
    }
}
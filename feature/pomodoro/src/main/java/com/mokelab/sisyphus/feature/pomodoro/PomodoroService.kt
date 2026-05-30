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
import com.mokelab.sisyphus.feature.pomodoro.floating.FloatingWindowManager
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
    val completedSessions: Int = 0,
    val sessionsBeforeLongBreak: Int = 4,
    val isOnBreak: Boolean = false,
    val breakMinutes: Int = 5,
    val longBreakMinutes: Int = 15
)

class PomodoroService : Service() {

    private val binder = PomodoroBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private var floatingWindowManager: FloatingWindowManager? = null

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var onComplete: (() -> Unit)? = null
    private var onGroupComplete: (() -> Unit)? = null

    inner class PomodoroBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        floatingWindowManager = FloatingWindowManager(this)
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

    fun setOnGroupCompleteListener(listener: () -> Unit) {
        onGroupComplete = listener
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
                completedSessions = it.completedSessions,
                sessionsBeforeLongBreak = it.sessionsBeforeLongBreak,
                breakMinutes = it.breakMinutes,
                longBreakMinutes = it.longBreakMinutes
            )
        }
        showFloatingWindow()
        startCountdown()
    }

    fun startBreak(isLongBreak: Boolean) {
        timerJob?.cancel()
        val breakDuration = if (isLongBreak) _state.value.longBreakMinutes else _state.value.breakMinutes
        _state.update {
            it.copy(
                isOnBreak = true,
                isRunning = true,
                isPaused = false,
                remainingSeconds = breakDuration * 60,
                durationMinutes = breakDuration,
                subjectName = if (isLongBreak) "长休息" else "短休息"
            )
        }
        showFloatingWindow()
        startCountdown()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false, isPaused = true) }
        updateFloatingWindow()
        updateNotification()
    }

    fun resumeTimer() {
        val currentState = _state.value
        if (currentState.isPaused && currentState.remainingSeconds > 0) {
            _state.update { it.copy(isRunning = true, isPaused = false) }
            startCountdown()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _state.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isOnBreak = false,
                remainingSeconds = it.durationMinutes * 60
            )
        }
        hideFloatingWindow()
        updateNotification()
    }

    /**
     * 开始倒计时
     * 使用绝对时间计算剩余秒数，避免 delay() 的时间漂移问题
     */
    private fun startCountdown() {
        updateFloatingWindow()
        updateNotification()

        timerJob = serviceScope.launch {
            val startTime = System.currentTimeMillis()
            val totalMillis = _state.value.remainingSeconds * 1000L

            while (true) {
                delay(200) // 每 200ms 更新一次 UI，更流畅
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = ((totalMillis - elapsed) / 1000).toInt()

                if (remaining <= 0) {
                    _state.update { it.copy(remainingSeconds = 0) }
                    updateFloatingWindow()
                    updateNotification()
                    break
                }

                _state.update { it.copy(remainingSeconds = remaining) }
                updateFloatingWindow()
                updateNotification()
            }
            onTimerComplete()
        }
    }

    private fun onTimerComplete() {
        val wasOnBreak = _state.value.isOnBreak
        val previousSessions = _state.value.completedSessions

        if (!wasOnBreak) {
            // Focus session complete
            val newCompletedSessions = previousSessions + 1
            _state.update {
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    isOnBreak = false,
                    completedSessions = newCompletedSessions,
                    remainingSeconds = it.durationMinutes * 60
                )
            }
            onComplete?.invoke()

            // Check if group of 4 is complete
            if (newCompletedSessions % _state.value.sessionsBeforeLongBreak == 0) {
                onGroupComplete?.invoke()
            }
        } else {
            // Break complete
            _state.update {
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    isOnBreak = false,
                    subjectName = "",
                    remainingSeconds = it.durationMinutes * 60
                )
            }
        }

        hideFloatingWindow()
        updateNotification()
    }

    private fun showFloatingWindow() {
        floatingWindowManager?.apply {
            onSingleClick = {
                if (_state.value.isRunning) pauseTimer() else resumeTimer()
            }
            onDoubleClick = { stopTimer() }
            onLongPress = { /* Expand to full screen - future feature */ }
            show()
            updateFloatingWindow()
        }
    }

    private fun hideFloatingWindow() {
        floatingWindowManager?.hide()
    }

    private fun updateFloatingWindow() {
        val s = _state.value
        val totalSeconds = s.durationMinutes * 60
        val elapsed = totalSeconds - s.remainingSeconds
        val progress = if (totalSeconds > 0) elapsed.toFloat() / totalSeconds else 0f
        val remainingMinutes = s.remainingSeconds / 60

        floatingWindowManager?.updateProgress(
            progress = progress,
            remainingMinutes = remainingMinutes,
            isPaused = s.isPaused,
            isRunning = s.isRunning,
            subjectName = s.subjectName
        )
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
        val s = _state.value
        val minutes = s.remainingSeconds / 60
        val seconds = s.remainingSeconds % 60
        val timeText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

        val statusText = when {
            s.isOnBreak && s.isRunning -> "休息中 - $timeText"
            s.isRunning -> "专注中 - ${s.subjectName} - $timeText"
            s.isPaused -> "已暂停 - $timeText"
            else -> "番茄钟就绪"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("番茄钟")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(s.isRunning || s.isPaused)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        timerJob?.cancel()
        floatingWindowManager?.hide()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        const val NOTIFICATION_ID = 1001
    }
}

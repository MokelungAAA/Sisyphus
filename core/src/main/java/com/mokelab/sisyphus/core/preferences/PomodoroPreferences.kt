package com.mokelab.sisyphus.core.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * 番茄钟偏好设置管理
 * 用于持久化番茄钟工作/休息时长
 */
class PomodoroPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    /**
     * 获取工作时长（分钟）
     */
    fun getWorkDuration(): Int {
        return prefs.getInt(KEY_WORK_DURATION, DEFAULT_WORK_DURATION)
    }

    /**
     * 设置工作时长（分钟）
     */
    fun setWorkDuration(minutes: Int) {
        prefs.edit()
            .putInt(KEY_WORK_DURATION, minutes)
            .apply()
    }

    /**
     * 获取休息时长（分钟）
     */
    fun getBreakDuration(): Int {
        return prefs.getInt(KEY_BREAK_DURATION, DEFAULT_BREAK_DURATION)
    }

    /**
     * 设置休息时长（分钟）
     */
    fun setBreakDuration(minutes: Int) {
        prefs.edit()
            .putInt(KEY_BREAK_DURATION, minutes)
            .apply()
    }

    /**
     * 获取长休息时长（分钟）
     */
    fun getLongBreakDuration(): Int {
        return prefs.getInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)
    }

    /**
     * 设置长休息时长（分钟）
     */
    fun setLongBreakDuration(minutes: Int) {
        prefs.edit()
            .putInt(KEY_LONG_BREAK_DURATION, minutes)
            .apply()
    }

    /**
     * 获取长休息前的会话数
     */
    fun getSessionsBeforeLongBreak(): Int {
        return prefs.getInt(KEY_SESSIONS_BEFORE_LONG_BREAK, DEFAULT_SESSIONS_BEFORE_LONG_BREAK)
    }

    /**
     * 设置长休息前的会话数
     */
    fun setSessionsBeforeLongBreak(sessions: Int) {
        prefs.edit()
            .putInt(KEY_SESSIONS_BEFORE_LONG_BREAK, sessions)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "pomodoro_preferences"
        private const val KEY_WORK_DURATION = "work_duration"
        private const val KEY_BREAK_DURATION = "break_duration"
        private const val KEY_LONG_BREAK_DURATION = "long_break_duration"
        private const val KEY_SESSIONS_BEFORE_LONG_BREAK = "sessions_before_long_break"

        private const val DEFAULT_WORK_DURATION = 25
        private const val DEFAULT_BREAK_DURATION = 5
        private const val DEFAULT_LONG_BREAK_DURATION = 15
        private const val DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4
    }
}

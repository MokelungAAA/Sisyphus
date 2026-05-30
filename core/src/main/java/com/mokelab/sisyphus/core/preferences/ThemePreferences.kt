package com.mokelab.sisyphus.core.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主题偏好设置管理
 * 用于持久化深色模式开关状态
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkModeFlow: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    /**
     * 是否启用深色模式
     */
    fun isDarkMode(): Boolean {
        return _isDarkMode.value
    }

    /**
     * 设置深色模式
     */
    fun setDarkMode(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
        _isDarkMode.value = enabled
    }

    /**
     * 切换深色模式
     * @return 切换后的状态
     */
    fun toggleDarkMode(): Boolean {
        val newValue = !isDarkMode()
        setDarkMode(newValue)
        return newValue
    }

    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}

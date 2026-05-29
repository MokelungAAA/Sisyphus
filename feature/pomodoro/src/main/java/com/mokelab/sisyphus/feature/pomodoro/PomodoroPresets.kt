package com.mokelab.sisyphus.feature.pomodoro

data class PomodoroPreset(
    val name: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val longBreakMinutes: Int,
    val sessionsBeforeLongBreak: Int
)

object PomodoroPresets {
    val CLASSIC = PomodoroPreset(
        name = "经典",
        focusMinutes = 25,
        breakMinutes = 5,
        longBreakMinutes = 15,
        sessionsBeforeLongBreak = 4
    )

    val SHORT = PomodoroPreset(
        name = "短时",
        focusMinutes = 15,
        breakMinutes = 3,
        longBreakMinutes = 10,
        sessionsBeforeLongBreak = 4
    )

    val LONG = PomodoroPreset(
        name = "长时",
        focusMinutes = 45,
        breakMinutes = 10,
        longBreakMinutes = 20,
        sessionsBeforeLongBreak = 3
    )

    fun custom(
        focusMinutes: Int,
        breakMinutes: Int,
        longBreakMinutes: Int = 15,
        sessionsBeforeLongBreak: Int = 4
    ) = PomodoroPreset(
        name = "自定义",
        focusMinutes = focusMinutes,
        breakMinutes = breakMinutes,
        longBreakMinutes = longBreakMinutes,
        sessionsBeforeLongBreak = sessionsBeforeLongBreak
    )

    val all = listOf(CLASSIC, SHORT, LONG)
}
package com.moodcalendar.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppTheme(val label: String, val emoji: String) {
    AMBER("琥珀暖", "🟠"),
    TEAL("水鸭绿", "🦆"),
    PINK("哔哩粉", "🩷"),
    ORANGE("活力橙", "🧡")
}

enum class DarkMode(val label: String) {
    SYSTEM("跟随系统"),
    ON("深色"),
    OFF("浅色")
}

class ThemeState {
    var selectedTheme by mutableStateOf(AppTheme.AMBER)
    var darkMode by mutableStateOf(DarkMode.SYSTEM)
}

class ReminderState {
    var enabled by mutableStateOf(false)
    var hour by mutableStateOf(20)
    var minute by mutableStateOf(0)
}

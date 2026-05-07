package com.moodcalendar.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MoodCalendarTheme(
    theme: AppTheme = AppTheme.AMBER,
    darkMode: DarkMode = DarkMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (darkMode) {
        DarkMode.SYSTEM -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }

    val colorScheme = when (theme) {
        AppTheme.AMBER -> if (isDark) AmberTheme.Dark else AmberTheme.Light
        AppTheme.TEAL -> if (isDark) TealTheme.Dark else TealTheme.Light
        AppTheme.PINK -> if (isDark) PinkTheme.Dark else PinkTheme.Light
        AppTheme.ORANGE -> if (isDark) OrangeTheme.Dark else OrangeTheme.Light
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MoodTypography,
        content = content
    )
}

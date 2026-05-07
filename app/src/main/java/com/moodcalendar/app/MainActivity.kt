package com.moodcalendar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moodcalendar.app.ui.screens.MainScreen
import com.moodcalendar.app.ui.screens.SplashScreen
import com.moodcalendar.app.ui.theme.AppTheme
import com.moodcalendar.app.ui.theme.DarkMode
import com.moodcalendar.app.ui.theme.MoodCalendarTheme
import com.moodcalendar.app.ui.theme.ThemeState
import com.moodcalendar.app.ui.theme.ReminderState
import com.moodcalendar.app.data.ReminderHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channel for reminders
        try { ReminderHelper.ensureChannel(this) } catch (_: Exception) {}

        setContent {
            val themeState = remember { ThemeState() }
            var showSplash by remember { mutableStateOf(true) }
            val reminderState = remember {
                ReminderState().also { state ->
                    val (enabled, hour, minute) = ReminderHelper.loadState(this@MainActivity)
                    state.enabled = enabled
                    state.hour = hour
                    state.minute = minute
                }
            }

            MoodCalendarTheme(
                theme = themeState.selectedTheme,
                darkMode = themeState.darkMode
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "splashTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onFinished = { showSplash = false })
                        } else {
                            val viewModel: MoodViewModel = viewModel()
                            val entries by viewModel.allEntries.collectAsState()

                            MainScreen(
                                entries = entries,
                                themeState = themeState,
                                reminderState = reminderState,
                                onSaveEntry = { entry -> viewModel.saveEntry(entry) },
                                onDeleteEntry = { id -> viewModel.deleteEntry(id) }
                            )

                            // Persist reminder & schedule/cancel when state changes
                            val ctx = this@MainActivity
                            LaunchedEffect(reminderState.enabled, reminderState.hour, reminderState.minute) {
                                try {
                                    ReminderHelper.saveState(ctx, reminderState.enabled, reminderState.hour, reminderState.minute)
                                    if (reminderState.enabled) {
                                        ReminderHelper.schedule(ctx, reminderState.hour, reminderState.minute)
                                    } else {
                                        ReminderHelper.cancel(ctx)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

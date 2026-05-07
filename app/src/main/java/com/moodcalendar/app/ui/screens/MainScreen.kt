package com.moodcalendar.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodcalendar.app.data.MoodEntry
import com.moodcalendar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    entries: Map<String, List<MoodEntry>>,
    themeState: ThemeState,
    reminderState: ReminderState,
    onSaveEntry: (MoodEntry) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                        label = { Text("记录心情") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("统计") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> CalendarScreen(
                        entries = entries,
                        themeState = themeState,
                        onSaveEntry = onSaveEntry,
                        onDeleteEntry = onDeleteEntry,
                        onOpenSettings = { showSettings = true }
                    )
                    1 -> StatsPage(
                        entries = entries
                    )
                }
            }
        }

        // Scrim — fade only, no slide
        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200)),
            label = "scrim"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { showSettings = false }
            )
        }
        // Panel — slide from right
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally { it } + fadeIn(tween(250)),
            exit = slideOutHorizontally { it } + fadeOut(tween(200)),
            label = "panel"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.End)
            ) {
                SettingsPanel(
                    themeState = themeState,
                    reminderState = reminderState,
                    onClose = { showSettings = false }
                )
            }
        }
    }
}

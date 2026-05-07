package com.moodcalendar.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodcalendar.app.ui.theme.*
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    themeState: ThemeState,
    reminderState: ReminderState,
    onClose: () -> Unit
) {
    var showThemeSection by remember { mutableStateOf(true) }
    var showReminderSection by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showBatteryHint by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Android 13+ notification permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        reminderState.enabled = granted
        if (granted) {
            showBatteryHint = true
        } else {
            showPermissionRationale = true
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("需要通知权限") },
            text = { Text("请在系统设置中允许「心情日历」发送通知，否则定时提醒无法生效。") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }) { Text("去设置") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) { Text("取消") }
            }
        )
    }

    if (showBatteryHint) {
        AlertDialog(
            onDismissRequest = { showBatteryHint = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("手机设置提醒") },
            text = {
                Text(
                    "部分手机（小米、华为、OPPO、vivo 等）的省电策略会阻止定时提醒生效。\n\n" +
                    "① 电池优化 → 设为「无限制」\n" +
                    "② 自启动 → 允许后台运行\n" +
                    "③ 最近任务 → 锁定应用不被清理\n\n" +
                    "不设置的话，提醒可能被系统杀掉哦。"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryHint = false
                    try {
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (_: Exception) {}
                }) { Text("去电池优化") }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryHint = false }) { Text("知道了") }
            }
        )
    }

    if (showTimePicker) {
        DateTimePicker(
            initialHour = reminderState.hour,
            initialMinute = reminderState.minute,
            onConfirm = { h, m ->
                reminderState.hour = h
                reminderState.minute = m
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    Surface(
        modifier = Modifier.width(300.dp).fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "关闭", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Theme Section ──
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { showThemeSection = !showThemeSection },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Text("主题", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        if (showThemeSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = showThemeSection,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)) + fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = themeState.selectedTheme == theme
                        val themeColor = when (theme) {
                            AppTheme.AMBER -> Color(0xFFD4925A)
                            AppTheme.TEAL -> Color(0xFF3A8A7A)
                            AppTheme.PINK -> Color(0xFFD4386A)
                            AppTheme.ORANGE -> Color(0xFFE07A2F)
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { themeState.selectedTheme = theme },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) themeColor.copy(alpha = 0.12f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(themeColor)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    theme.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = themeColor)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    DarkMode.entries.forEach { mode ->
                        val isSelected = themeState.darkMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { themeState.darkMode = mode },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (mode) {
                                        DarkMode.SYSTEM -> Icons.Default.Settings
                                        DarkMode.ON -> Icons.Default.DarkMode
                                        DarkMode.OFF -> Icons.Default.LightMode
                                    },
                                    contentDescription = null, modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(mode.label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Reminder Section ──
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { showReminderSection = !showReminderSection },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Text("定时提醒", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        if (showReminderSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = showReminderSection,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)) + fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                    // Toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("每日提醒", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = reminderState.enabled,
                            onCheckedChange = { enable ->
                                if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    reminderState.enabled = enable
                                    if (enable) showBatteryHint = true
                                }
                            }
                        )
                    }

                    // Time picker row
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable(enabled = reminderState.enabled) { showTimePicker = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "提醒时间",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (reminderState.enabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = String.format("%02d:%02d", reminderState.hour, reminderState.minute),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ───── Time Picker Dialog ─────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("选择提醒时间", fontWeight = FontWeight.Bold) },
        text = {
            TimePicker(state = timeState)
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(timeState.hour, timeState.minute) },
                shape = RoundedCornerShape(20.dp)
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

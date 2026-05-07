package com.moodcalendar.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodcalendar.app.data.MoodEntry
import com.moodcalendar.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    entries: Map<String, List<MoodEntry>>,
    themeState: ThemeState,
    onSaveEntry: (MoodEntry) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<MoodEntry?>(null) }
    var expandedEntry by remember { mutableStateOf<MoodEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedFilterEmoji by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current
    val hasActiveFilter = searchQuery.isNotBlank() || selectedFilterEmoji != null

    // Filter entries by search text + emoji filter
    val filteredEntries = remember(entries, searchQuery, selectedFilterEmoji) {
        if (!hasActiveFilter) entries
        else entries.mapValues { (_, list) ->
            list.filter { entry ->
                (selectedFilterEmoji == null || entry.emoji == selectedFilterEmoji) &&
                (searchQuery.isBlank() ||
                 entry.note.contains(searchQuery, ignoreCase = true) ||
                 moodSearchNames[entry.emoji]?.contains(searchQuery, ignoreCase = true) == true)
            }
        }.filter { it.value.isNotEmpty() }
    }

    // Flattened search results sorted by time desc
    val searchResults = remember(entries, searchQuery, selectedFilterEmoji) {
        if (!hasActiveFilter) emptyList()
        else entries.entries.flatMap { (dateStr, list) ->
            list.filter { entry ->
                (selectedFilterEmoji == null || entry.emoji == selectedFilterEmoji) &&
                (searchQuery.isBlank() ||
                 entry.note.contains(searchQuery, ignoreCase = true) ||
                 moodSearchNames[entry.emoji]?.contains(searchQuery, ignoreCase = true) == true)
            }.map { dateStr to it }
        }.sortedByDescending { (_, e) -> e.createdAt }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("心情日历", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "设置", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showSearch = !showSearch; if (!showSearch) { searchQuery = ""; selectedFilterEmoji = null } }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Search bar
                AnimatedVisibility(
                    visible = showSearch,
                    enter = expandVertically(animationSpec = spring(dampingRatio = 0.7f)) + fadeIn(),
                    exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
                    label = "searchBar"
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索记录的文字…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; selectedFilterEmoji = null; showSearch = false }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                    )
                }

                // ── Emoji filter grid (2 rows) ──
                if (showSearch) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        moodEmojis.chunked(10).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { emoji ->
                                    val isSelected = selectedFilterEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) (moodColors[emoji] ?: MoodNeutral).copy(alpha = 0.3f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                if (isSelected) 1.5.dp else 0.dp,
                                                (moodColors[emoji] ?: MoodNeutral).copy(alpha = 0.6f),
                                                CircleShape
                                            )
                                            .clickable {
                                                selectedFilterEmoji = if (isSelected) null else emoji
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = rememberSvgPainter(moodEmojiRes[emoji]!!),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                if (hasActiveFilter && searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("没有找到匹配的记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (hasActiveFilter) {
                    // ── Search Results List ──
                    if (searchResults.isNotEmpty()) {
                        Text(
                            "找到 ${searchResults.size} 条记录",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(searchResults, key = { (_, e) -> e.id }) { (dateStr, entry) ->
                                SearchResultItem(
                                    entry = entry,
                                    dateStr = dateStr,
                                    onClick = {
                                        val date = try { LocalDate.parse(dateStr) } catch (_: Exception) { return@SearchResultItem }
                                        currentMonth = YearMonth.from(date)
                                        selectedDate = date
                                        editingEntry = null
                                        searchQuery = ""
                                        showSearch = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // ── Scrollable calendar + preview area with floating button ──
                    Box(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            // Calendar swipe area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(currentMonth) {
                                        val threshold = 120.dp.toPx()
                                        var totalDrag = 0f
                                        detectHorizontalDragGestures(
                                            onDragStart = { totalDrag = 0f },
                                            onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                                            onDragEnd = {
                                                if (totalDrag > threshold) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    currentMonth = currentMonth.minusMonths(1)
                                                } else if (totalDrag < -threshold) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    currentMonth = currentMonth.plusMonths(1)
                                                }
                                            }
                                        )
                                    }
                            ) {
                                Column {
                                    AnimatedContent(
                                        targetState = currentMonth,
                                        transitionSpec = {
                                            val forward = targetState > initialState
                                            (if (forward) slideInHorizontally { w -> w } else slideInHorizontally { w -> -w }) togetherWith
                                            (if (forward) slideOutHorizontally { w -> -w } else slideOutHorizontally { w -> w })
                                        },
                                        label = "monthSwipe"
                                    ) { month ->
                                        Text(
                                            text = "${month.year}年${month.monthValue}月",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    DayHeaders()
                                    Spacer(Modifier.height(2.dp))

                                    CalendarGrid(
                                        yearMonth = currentMonth,
                                        entries = filteredEntries,
                                        onDateClick = { date ->
                                            selectedDate = date
                                            editingEntry = null
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Preview section
                            AnimatedVisibility(
                                visible = selectedDate != null,
                                enter = slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialOffsetY = { it / 2 }) + fadeIn(tween(250)),
                                exit = slideOutVertically(tween(200)) + fadeOut(tween(150)),
                                label = "previewCard"
                            ) {
                                selectedDate?.let { date ->
                                    val key = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    val dayEntries = filteredEntries[key] ?: emptyList()
                                    MoodPreviewSection(
                                        date = date,
                                        entries = dayEntries,
                                        onEntryClick = { expandedEntry = it },
                                        onAddClick = { editingEntry = null; showDialog = true },
                                        onEdit = { editingEntry = it; showDialog = true },
                                        onDelete = { onDeleteEntry(it.id) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                        }

                        // Floating "回到今天" chip overlaying the scrollable content
                        if (selectedDate != null && selectedDate != LocalDate.now()) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        currentMonth = YearMonth.now()
                                        selectedDate = LocalDate.now()
                                        editingEntry = null
                                    }.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("回到今天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Entry dialogs
        if (showDialog && selectedDate != null) {
            val dateKey = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayCount = (filteredEntries[dateKey] ?: emptyList()).let {
                if (editingEntry != null) it.size - 1 else it.size
            }
            MoodEntryDialog(
                date = selectedDate!!,
                existingEntry = editingEntry,
                existingCount = dayCount,
                onDismiss = { showDialog = false; editingEntry = null },
                onSave = { onSaveEntry(it); showDialog = false; editingEntry = null }
            )
        }

        if (expandedEntry != null) {
            ExpandedEntryDialog(
                entry = expandedEntry!!,
                onDismiss = { expandedEntry = null },
                onDelete = {
                    onDeleteEntry(expandedEntry!!.id)
                    expandedEntry = null
                },
                onEdit = {
                    editingEntry = expandedEntry
                    expandedEntry = null
                    showDialog = true
                }
            )
        }
    }
}

// ───── Day Headers ─────

@Composable
private fun DayHeaders() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        dayLabels.forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = if (index >= 5) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ───── Calendar Grid ─────

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    entries: Map<String, List<MoodEntry>>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val startOffset = firstDayOfWeek - 1
    val today = LocalDate.now()
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - startOffset + 1
                    if (day in 1..daysInMonth) {
                        val date = yearMonth.atDay(day)
                        val key = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val dayEntries = entries[key] ?: emptyList()
                        DateCell(
                            day = day,
                            entries = dayEntries,
                            isToday = date == today,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ───── Date Cell ─────

@Composable
private fun DateCell(
    day: Int,
    entries: List<MoodEntry>,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val bgColor = when { isToday -> MaterialTheme.colorScheme.primaryContainer else -> Color.Transparent }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cellScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f).padding(2.dp).clip(shape)
            .background(bgColor, shape)
            .clickable(interactionSource = interactionSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick()
            }
            .graphicsLayer { scaleX = scale; scaleY = scale },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (entries.isNotEmpty()) {
                val showEmojis = entries.take(3)
                Row(horizontalArrangement = Arrangement.spacedBy((-4).dp), verticalAlignment = Alignment.CenterVertically) {
                    showEmojis.forEach { entry ->
                        Box(
                            modifier = Modifier.size(18.dp).clip(CircleShape)
                                .background((entry.emoji.let { moodColors[it] } ?: MoodNeutral).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberSvgPainter(moodEmojiRes[entry.emoji]!!),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (entries.size > 3) Text(
                    text = "+${entries.size - 3}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ───── Mood Greeting ─────

private fun moodGreeting(entries: List<MoodEntry>): String {
    if (entries.isEmpty()) {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 0..5 -> "夜深了🌙 还没睡的话，来记录一下吧"
            in 6..8 -> "早安☀️ 新的一天开始了～"
            in 9..11 -> "上午好～今天有什么想记录的？"
            in 12..13 -> "中午好🌻 吃饭了吗？"
            in 14..17 -> "下午好～来分享一下吧"
            in 18..19 -> "傍晚了🌆 今天过得怎么样？"
            else -> "今天还没有记录，想分享一下吗？"
        }
    }
    val happy = setOf("😊", "😄", "🥳", "🥰"); val sad = setOf("😢", "😞", "🥺")
    val angry = setOf("😡", "😤"); val chill = setOf("😴", "😌", "😶", "😐")
    val h = entries.count { it.emoji in happy }; val s = entries.count { it.emoji in sad }
    val a = entries.count { it.emoji in angry }; val c = entries.count { it.emoji in chill }
    return when {
        h > 0 && s == 0 && a == 0 -> "今天心情不错呀😊 开心最重要！"
        s > 0 && h == 0 -> "抱抱你🤗 不开心的事都会过去的"
        s > 0 && h > 0 -> "心情像过山车🎢 但至少多彩呀"
        a > 0 && h > s -> "虽然有点小生气，但整体还行😤"
        a > 0 -> "消消气，深呼吸～"
        c >= entries.size * 0.6 -> "今天很平静😌 也是一种幸福"
        entries.size >= 3 -> "今天好丰富！记录了${entries.size}条📝"
        else -> "来看看今天的心情记录～"
    }
}

// ───── Mood Preview Section ─────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodPreviewSection(
    date: LocalDate,
    entries: List<MoodEntry>,
    onEntryClick: (MoodEntry) -> Unit,
    onAddClick: () -> Unit,
    onEdit: (MoodEntry) -> Unit,
    onDelete: (MoodEntry) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<MoodEntry?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${date.monthValue}月${date.dayOfMonth}日 ${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("${entries.size}条记录", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AnimatedContent(targetState = entries.hashCode(), transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }, label = "greeting") {
                Text(
                    moodGreeting(entries),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (entries.isEmpty()) {
                FilledIconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Icon(Icons.Default.Add, contentDescription = "添加记录", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary) }
            } else {
                entries.forEach { entry ->
                    key(entry.id) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.EndToStart -> { onEdit(entry); false }
                                SwipeToDismissBoxValue.StartToEnd -> { deleteTarget = entry; false }
                                else -> true
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val dir = dismissState.dismissDirection
                            when (dir) {
                                SwipeToDismissBoxValue.EndToStart -> Box(
                                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Icon(Icons.Default.Edit, "编辑", modifier = Modifier.padding(end = 20.dp), tint = MaterialTheme.colorScheme.primary) }
                                SwipeToDismissBoxValue.StartToEnd -> Box(
                                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.CenterStart
                                ) { Icon(Icons.Default.Delete, "删除", modifier = Modifier.padding(start = 20.dp), tint = MaterialTheme.colorScheme.error) }
                                else -> {}
                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onEntryClick(entry) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            tonalElevation = 0.5.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                        .background((entry.emoji.let { moodColors[it] } ?: MoodNeutral).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) { Image(painter = rememberSvgPainter(moodEmojiRes[entry.emoji]!!), contentDescription = null, modifier = Modifier.size(28.dp)) }
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    if (entry.note.isNotBlank()) Text(entry.note, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    java.time.Instant.ofEpochMilli(entry.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    }
                }
                // Add button when under daily limit
                if (entries.size < 5) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onAddClick() },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text("添加记录", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("删除记录") },
            text = { Text("确定删除这条心情记录？\n\n此操作不可撤销。") },
            confirmButton = { Button(onClick = { onDelete(deleteTarget!!); deleteTarget = null }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } }
        )
    }
}

// ───── Expanded Entry Dialog ─────

@Composable
private fun ExpandedEntryDialog(
    entry: MoodEntry,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val timeText = java.time.Instant.ofEpochMilli(entry.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    val dateText = try { LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)) } catch (_: Exception) { entry.date }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background((entry.emoji.let { moodColors[it] } ?: MoodNeutral).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Image(painter = rememberSvgPainter(moodEmojiRes[entry.emoji]!!), contentDescription = null, modifier = Modifier.size(32.dp)) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(timeText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        },
        text = {
            Column {
                if (entry.note.isNotBlank()) {
                    Text(entry.note, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp)); Text("编辑")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp)); Text("删除")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("删除记录") },
            text = { Text("确定删除这条心情记录？此操作不可撤销。") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } }
        )
    }
}

// ───── Mood Entry Dialog ─────

@Composable
fun MoodEntryDialog(
    date: LocalDate,
    existingEntry: MoodEntry?,
    existingCount: Int = 0,
    onDismiss: () -> Unit,
    onSave: (MoodEntry) -> Unit
) {
    var selectedEmoji by remember { mutableStateOf(existingEntry?.emoji ?: "") }
    var note by remember { mutableStateOf(existingEntry?.note ?: "") }
    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val haptic = LocalHapticFeedback.current
    val isEdit = existingEntry != null
    val atDailyLimit = !isEdit && existingCount >= 5

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "${date.monthValue}月${date.dayOfMonth}日 ${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)}${if (isEdit) " (编辑)" else ""}",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 460.dp)) {
                Text("选择心情", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                moodEmojis.chunked(5).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
                        row.forEach { emoji ->
                            val sel = selectedEmoji == emoji; val mc = moodColors[emoji] ?: MoodNeutral
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                    .background(if (sel) mc.copy(alpha = 0.25f) else Color.Transparent)
                                    .then(if (sel) Modifier.border(2.dp, mc.copy(alpha = 0.6f), CircleShape) else Modifier)
                                    .clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) { Image(painter = rememberSvgPainter(moodEmojiRes[emoji]!!), contentDescription = emoji, modifier = Modifier.size(32.dp)) }
                        }
                    }; Spacer(Modifier.height(8.dp))
                }

                if (atDailyLimit) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(6.dp))
                            Text("每天最多记录5条心情", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = note, onValueChange = { if (it.length <= 200) note = it },
                    label = { Text("今天的感悟") }, placeholder = { Text("记录一下今天的心情…") },
                    supportingText = { Text("${note.length}/200", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(), minLines = 1, maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), unfocusedContainerColor = Color.Transparent)
                )

                Spacer(Modifier.height(12.dp))
            }
        },
        confirmButton = {
            if (selectedEmoji.isNotEmpty() && !atDailyLimit) Button(
                onClick = { onSave(MoodEntry(id = existingEntry?.id ?: 0, date = dateStr, emoji = selectedEmoji, note = note)) },
                shape = RoundedCornerShape(20.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                enabled = !atDailyLimit
            ) { Text(if (isEdit) "更新" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ───── Search Result Item ─────

@Composable
private fun SearchResultItem(
    entry: MoodEntry,
    dateStr: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background((entry.emoji.let { moodColors[it] } ?: MoodNeutral).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Image(painter = rememberSvgPainter(moodEmojiRes[entry.emoji]!!), contentDescription = null, modifier = Modifier.size(28.dp)) }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Date
                Text(
                    text = try {
                        val d = LocalDate.parse(dateStr)
                        "${d.monthValue}月${d.dayOfMonth}日 ${d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)}"
                    } catch (_: Exception) { dateStr },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

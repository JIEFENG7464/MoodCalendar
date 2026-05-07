package com.moodcalendar.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodcalendar.app.data.MoodEntry
import com.moodcalendar.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color
)

// ───── Stats Computation ─────

private fun computePeriodStats(entries: Map<String, List<MoodEntry>>): List<Pair<String, Int>> {
    val allEntries = entries.values.flatten()
    val moodCounts = mutableMapOf<String, Int>()
    for (entry in allEntries) {
        moodCounts[entry.emoji] = (moodCounts[entry.emoji] ?: 0) + 1
    }
    return moodCounts.entries.map { it.key to it.value }.sortedByDescending { it.second }
}

private fun totalChars(entries: Map<String, List<MoodEntry>>): Int =
    entries.values.flatten().sumOf { it.note.length }

private fun totalEntries(entries: Map<String, List<MoodEntry>>): Int =
    entries.values.flatten().size

private fun filterByWeek(entries: Map<String, List<MoodEntry>>, weekOffset: Int): Map<String, List<MoodEntry>> {
    val today = LocalDate.now()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(weekOffset.toLong())
    val sunday = monday.plusDays(6)
    return entries.filter { (dateStr, _) ->
        try {
            val date = LocalDate.parse(dateStr)
            !date.isBefore(monday) && !date.isAfter(sunday)
        } catch (_: Exception) { false }
    }
}

private fun filterByMonth(entries: Map<String, List<MoodEntry>>, monthOffset: Int): Map<String, List<MoodEntry>> {
    val target = YearMonth.now().plusMonths(monthOffset.toLong())
    return entries.filter { (dateStr, _) ->
        try { YearMonth.from(LocalDate.parse(dateStr)) == target } catch (_: Exception) { false }
    }
}

private fun filterByYear(entries: Map<String, List<MoodEntry>>, yearOffset: Int): Map<String, List<MoodEntry>> {
    val target = Year.now().plusYears(yearOffset.toLong())
    return entries.filter { (dateStr, _) ->
        try { Year.from(LocalDate.parse(dateStr)) == target } catch (_: Exception) { false }
    }
}

private fun periodLabel(periodType: Int, weekOffset: Int, monthOffset: Int, yearOffset: Int): String {
    val today = LocalDate.now()
    return when (periodType) {
        0 -> {
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(weekOffset.toLong())
            val sunday = monday.plusDays(6)
            "第${monday.get(java.time.temporal.WeekFields.of(java.util.Locale.CHINA).weekOfYear())}周  (${monday.monthValue}/${monday.dayOfMonth} - ${sunday.monthValue}/${sunday.dayOfMonth})"
        }
        1 -> {
            val ym = YearMonth.now().plusMonths(monthOffset.toLong())
            "${ym.year}年${ym.monthValue}月"
        }
        2 -> {
            val y = Year.now().plusYears(yearOffset.toLong())
            "${y.value}年"
        }
        else -> ""
    }
}

private fun periodGreeting(entries: Map<String, List<MoodEntry>>, periodType: Int): String {
    val count = entries.values.flatten().size
    if (count == 0) return "这个时段还没有记录～"
    val days = entries.size
    val chars = entries.values.flatten().sumOf { it.note.length }
    val moodCounts = computePeriodStats(entries)

    val sb = StringBuilder()
    val periodName = when (periodType) { 0 -> "这周"; 1 -> "这个月"; else -> "今年" }

    sb.append("$periodName 你记录了 $count 篇心情")
    if (chars > 0) sb.append("，写了 $chars 个字")
    sb.append("，覆盖了 $days 天。\n\n")

    if (moodCounts.isNotEmpty()) {
        val (topEmoji, topCount) = moodCounts.first()
        val pct = topCount * 100 / count
        sb.append("出现最多的是「${topEmoji}」($topCount 次, ${pct}%)")
        sb.append(
            when (topEmoji) {
                "😊", "😄", "🥳" -> "，看来过得不错😊"
                "🥰" -> "，心里暖暖的～"
                "😢", "😞", "🥺" -> "，抱抱你🤗"
                "😡", "😤" -> "，有脾气就发出来！"
                "😴", "😌", "😶" -> "，内心平静是好事"
                "😐", "🤔" -> "，思考很多呢"
                else -> ""
            }
        )
    }

    if (periodType != 2 && days >= 3) {
        val avg = count.toFloat() / days
        if (avg > 1.2f) sb.append("\n\n平均每天 $avg 条，很勤快📝")
    }

    return sb.toString()
}

private fun timeSlots(entries: Map<String, List<MoodEntry>>): List<Pair<String, Int>> {
    var m = 0; var a = 0; var e = 0; var n = 0
    for (entry in entries.values.flatten()) {
        val hour = try {
            java.time.Instant.ofEpochMilli(entry.createdAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalTime().hour
        } catch (_: Exception) { 12 }
        when (hour) { in 0..5 -> n++; in 6..11 -> m++; in 12..17 -> a++; else -> e++ }
    }
    return listOf("早晨" to m, "下午" to a, "傍晚" to e, "深夜" to n).filter { it.second > 0 }
}

// ───── StatsPage ─────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsPage(entries: Map<String, List<MoodEntry>>) {
    var periodType by remember { mutableStateOf(0) }
    var weekOffset by remember { mutableStateOf(0) }
    var monthOffset by remember { mutableStateOf(0) }
    var yearOffset by remember { mutableStateOf(0) }

    // Reset offset when switching period type
    LaunchedEffect(periodType) {
        weekOffset = 0; monthOffset = 0; yearOffset = 0
    }

    val periodEntries = remember(entries, periodType, weekOffset, monthOffset, yearOffset) {
        when (periodType) {
            0 -> filterByWeek(entries, weekOffset)
            1 -> filterByMonth(entries, monthOffset)
            2 -> filterByYear(entries, yearOffset)
            else -> entries
        }
    }

    val count = remember(periodEntries) { totalEntries(periodEntries) }
    val stats = remember(periodEntries) { computePeriodStats(periodEntries) }
    val totalC = remember(periodEntries) { totalChars(periodEntries) }
    val slots = remember(periodEntries) { timeSlots(periodEntries) }
    val greeting = remember(periodEntries, periodType) { periodGreeting(periodEntries, periodType) }
    val label = remember(periodType, weekOffset, monthOffset, yearOffset) { periodLabel(periodType, weekOffset, monthOffset, yearOffset) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Period Tabs ──
        TabRow(
            selectedTabIndex = periodType,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("周", "月", "年").forEachIndexed { i, title ->
                Tab(
                    selected = periodType == i,
                    onClick = { periodType = i },
                    text = {
                        Text(
                            title,
                            fontWeight = if (periodType == i) FontWeight.Bold else FontWeight.Normal,
                            color = if (periodType == i) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // ── Period Navigation ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    when (periodType) { 0 -> weekOffset--; 1 -> monthOffset--; 2 -> yearOffset-- }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一个", modifier = Modifier.size(20.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = {
                    when (periodType) { 0 -> weekOffset++; 1 -> monthOffset++; 2 -> yearOffset++ }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一个", modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Content ──
        if (count == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text("该时段没有记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // ── Overview Stats ──
                Column(Modifier.fillMaxWidth()) {
                    StatRow("📝", "$count", "篇记录")
                    StatRow("✍️", "$totalC", "个字")
                    StatRow("📅", "${periodEntries.size}", "天")
                }

                Spacer(Modifier.height(24.dp))

                // ── Mood Distribution ──
                if (stats.isNotEmpty()) {
                    SectionTitle("心情分布")
                    Spacer(Modifier.height(12.dp))
                    MoodPieChart(stats, count)
                    Spacer(Modifier.height(24.dp))
                }

                // ── Time Slots ──
                if (slots.isNotEmpty()) {
                    SectionTitle("记录时段")
                    Spacer(Modifier.height(12.dp))
                    TimeSlotPieChart(slots, count)
                    Spacer(Modifier.height(24.dp))
                }

                // ── Human Text ──
                SectionTitle("给你的话")
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ) {
                    GreetingWithSvgEmoji(greeting)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ───── Greeting with SVG Emoji ─────

@Composable
private fun GreetingWithSvgEmoji(greeting: String) {
    // Extract unique emojis by iterating code points
    val emojiSet = remember(greeting) {
        val set = mutableSetOf<String>()
        var i = 0
        while (i < greeting.length) {
            val cp = Character.codePointAt(greeting, i)
            val s = String(Character.toChars(cp))
            if (s in moodEmojiRes.keys) set.add(s)
            i += Character.charCount(cp)
        }
        set
    }

    val inlineContent = remember(greeting, emojiSet) {
        emojiSet.associateWith { emoji ->
            InlineTextContent(
                Placeholder(18.sp, 18.sp, PlaceholderVerticalAlign.TextCenter)
            ) {
                Image(
                    painter = rememberSvgPainter(moodEmojiRes[emoji]!!),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    val annotatedString = remember(greeting) {
        buildAnnotatedString {
            var i = 0
            while (i < greeting.length) {
                val cp = Character.codePointAt(greeting, i)
                val s = String(Character.toChars(cp))
                if (s in moodEmojiRes.keys) {
                    appendInlineContent(s, " ")
                } else {
                    append(s)
                }
                i += Character.charCount(cp)
            }
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 22.sp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        modifier = Modifier.padding(16.dp)
    )
}

// ───── Section Title ─────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

// ───── Stat Row ─────

@Composable
private fun StatRow(icon: String, value: String, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ───── Mood Pie Chart ─────

@Composable
private fun MoodPieChart(stats: List<Pair<String, Int>>, total: Int) {
    val slices = remember(stats, total) {
        val colorGroups = mutableMapOf<Color, MutableList<Pair<String, Int>>>()
        stats.forEach { (emoji, count) ->
            val color = moodColors[emoji] ?: MoodNeutral
            colorGroups.getOrPut(color) { mutableListOf() }.add(emoji to count)
        }
        colorGroups.map { (color, items) ->
            PieSlice(
                items.joinToString("|") { it.first },
                items.sumOf { it.second }.toFloat(),
                color
            )
        }.sortedByDescending { it.value }
    }

    PieChartWithLegend(
        slices = slices,
        total = total.toFloat()
    ) { slice ->
        val emojis = slice.label.split("|")
        val names = emojis.mapNotNull { moodNames[it] }.distinct()
        val pct = (slice.value / total.toFloat() * 100).toInt()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
                emojis.take(3).forEach { emoji ->
                    val painter = moodEmojiRes[emoji]
                    if (painter != null) {
                        Image(
                            painter = rememberSvgPainter(painter),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${names.firstOrNull() ?: ""}  $pct%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${slice.value.toInt()}次",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}

// ───── Time Slot Pie Chart ─────

@Composable
private fun TimeSlotPieChart(slots: List<Pair<String, Int>>, total: Int) {
    val slotColors = mapOf("早晨" to Color(0xFFFFD54F), "下午" to Color(0xFF64B5F6), "傍晚" to Color(0xFFCE93D8), "深夜" to Color(0xFF37474F))
    val slices = remember(slots, total) {
        slots.map { (label, count) ->
            PieSlice(label, count.toFloat(), slotColors[label] ?: MoodNeutral)
        }
    }

    PieChartWithLegend(
        slices = slices,
        total = total.toFloat()
    ) { slice ->
        val pct = (slice.value / total * 100).toInt()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${slice.label}  $pct%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${slice.value.toInt()}次",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}

// ───── Pie Chart with Legend ─────

@Composable
private fun PieChartWithLegend(
    slices: List<PieSlice>,
    total: Float,
    legendContent: @Composable (PieSlice) -> Unit = {}
) {
    if (slices.isEmpty() || total <= 0f) return

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.minDimension * 0.18f
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = slice.value / total * 360f * animatedProgress.value
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += slice.value / total * 360f
                }
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${total.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "总计",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        slices.forEach { slice ->
            Box(modifier = Modifier.padding(vertical = 2.dp)) {
                legendContent(slice)
            }
        }
    }
}

private val moodNames = mapOf(
    "😊" to "开心", "😄" to "开心", "🥳" to "庆祝",
    "🥰" to "喜爱", "😢" to "难过", "😞" to "低落",
    "🥺" to "委屈", "😡" to "生气", "😤" to "不爽",
    "😴" to "困倦", "😌" to "放松", "😶" to "沉默",
    "😐" to "一般", "🤔" to "思考", "😱" to "震惊",
    "🤯" to "炸裂", "😎" to "得意", "🤗" to "拥抱",
    "😇" to "天使", "😏" to "狡黠"
)

package com.moodcalendar.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

private val quotePool = listOf(
    "此中有真意，欲辨已忘言。" to "陶渊明",
    "人生如逆旅，我亦是行人。" to "苏轼",
    "行到水穷处，坐看云起时。" to "王维",
    "心静即声淡，其间无古今。" to "白居易",
    "且将新火试新茶，诗酒趁年华。" to "苏轼",
    "不要因为走得太远，忘了我们为什么出发。" to "纪伯伦",
    "万物皆有裂痕，那是光照进来的地方。" to "莱昂纳德·科恩",
    "生活不是为了赶路，而是为了感受路。" to "未知",
    "少年与爱永不老去，即便披荆斩棘，丢失怒马鲜衣。" to "莫峻",
    "落日归山海，山海藏深意。" to "未知",
    "总有人间一两风，填我十万八千梦。" to "未知",
    "日出未必意味着光明，太阳也无非是一颗晨星而已。" to "梭罗",
    "山海自有归期，风雨自有相逢。" to "未知",
    "爱自己是终身浪漫的开始。" to "王尔德",
    "世界上只有一种真正的英雄主义，那就是在认清生活的真相后依然热爱生活。" to "罗曼·罗兰",
    "心有猛虎，细嗅蔷薇。" to "西格里夫·萨松",
    "凡是过去，皆为序章。" to "莎士比亚",
    "生活最佳状态：冷冷清清的风风火火。" to "木心",
    "从前的日色变得慢，车、马、邮件都慢，一生只够爱一个人。" to "木心",
    "愿你的世界，星光满载，初心不改。" to "未知",
    "我与旧事归于尽，来年依旧迎花开。" to "未知",
    "且听风吟，静待花开。" to "村上春树",
    "要把所有的夜归还给星河，把所有的春光归还给疏疏篱落。" to "海子",
    "今夜我不关心人类，我只想你。" to "海子",
    "如果你瞄准月亮，即使迷失也是落在星河之间。" to "未知",
)

private fun pickQuote(): Pair<String, String> {
    val picked = quotePool.random()
    val source = when {
        picked.second == "未知" -> picked.second
        else -> picked.second
    }
    return picked.first to source
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val quote = remember { pickQuote() }
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2800)
        onFinished()
    }
    LaunchedEffect(Unit) {
        ready = true
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (ready) 1f else 0f,
        animationSpec = tween(1200),
        label = "alpha"
    )

    val loadingAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
            Spacer(Modifier.height(32.dp))

            Text(
                text = quote.first,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().alpha(textAlpha)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "—— ${quote.second}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = "心情日历",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = loadingAlpha)
            )
        }
    }
}

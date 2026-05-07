package com.moodcalendar.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.PictureDrawable
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.caverock.androidsvg.SVG
import com.moodcalendar.app.R
import com.moodcalendar.app.ui.theme.*

/**
 * All mood emoji text strings for display order
 */
val moodEmojis: List<String> = listOf(
    "😊", "😢", "😡", "😴", "🥰", "😐", "😱", "😎",
    "🤔", "🥺", "😄", "😞", "🤗", "😤", "😌", "😶",
    "🥳", "🤯", "😇", "😏"
)

/**
 * Maps emoji text to OpenMoji SVG raw resource ID
 */
val moodEmojiRes: Map<String, Int> = mapOf(
    "😊" to R.raw.om_1f60a,
    "😢" to R.raw.om_1f622,
    "😡" to R.raw.om_1f621,
    "😴" to R.raw.om_1f634,
    "🥰" to R.raw.om_1f970,
    "😐" to R.raw.om_1f610,
    "😱" to R.raw.om_1f631,
    "😎" to R.raw.om_1f60e,
    "🤔" to R.raw.om_1f914,
    "🥺" to R.raw.om_1f97a,
    "😄" to R.raw.om_1f604,
    "😞" to R.raw.om_1f61e,
    "🤗" to R.raw.om_1f917,
    "😤" to R.raw.om_1f624,
    "😌" to R.raw.om_1f60c,
    "😶" to R.raw.om_1f636,
    "🥳" to R.raw.om_1f973,
    "🤯" to R.raw.om_1f92f,
    "😇" to R.raw.om_1f607,
    "😏" to R.raw.om_1f60f,
)

/**
 * Decorative background tint for each mood emoji
 */
val moodColors: Map<String, Color> = mapOf(
    "😊" to MoodHappy, "😄" to MoodHappy, "🥳" to MoodHappy, "🥰" to MoodLoved,
    "😢" to MoodSad, "😞" to MoodSad, "🥺" to MoodSad,
    "😡" to MoodAngry, "😤" to MoodAngry,
    "😴" to MoodSleepy, "😌" to MoodSleepy, "😶" to MoodSleepy,
    "😐" to MoodNeutral, "🤔" to MoodNeutral,
    "😱" to MoodSurprised, "🤯" to MoodSurprised,
    "😎" to MoodCool, "🤗" to MoodLoved, "😇" to MoodLoved,
    "😏" to MoodNeutral
)

/**
 * Emoji → Chinese name for search filtering
 */
val moodSearchNames: Map<String, String> = mapOf(
    "😊" to "开心", "😄" to "开心大笑", "🥳" to "庆祝",
    "🥰" to "喜爱", "😢" to "难过", "😞" to "低落",
    "🥺" to "委屈", "😡" to "生气", "😤" to "不爽",
    "😴" to "困倦", "😌" to "放松", "😶" to "沉默",
    "😐" to "一般", "🤔" to "思考", "😱" to "震惊",
    "🤯" to "炸裂", "😎" to "得意", "🤗" to "拥抱",
    "😇" to "天使", "😏" to "狡黠"
)

/**
 * Load an SVG from res/raw and return a Compose Painter
 */
@Composable
fun rememberSvgPainter(@RawRes rawRes: Int): Painter {
    val context = LocalContext.current
    return remember(rawRes) {
        val inputStream = context.resources.openRawResource(rawRes)
        val svg = SVG.getFromInputStream(inputStream)
        svg.setDocumentWidth(72f)
        svg.setDocumentHeight(72f)
        svg.setDocumentViewBox(0f, 0f, 72f, 72f)

        val density = context.resources.displayMetrics.density
        val w = (72 * density).toInt()
        val h = (72 * density).toInt()
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(density, density)

        // try renderToCanvas (1.4+), fall back to Picture
        try {
            svg.javaClass.getMethod("renderToCanvas", Canvas::class.java).invoke(svg, canvas)
        } catch (_: NoSuchMethodException) {
            val picture = svg.renderToPicture()
            canvas.drawPicture(picture, RectF(0f, 0f, 72f, 72f))
        }

        BitmapPainter(bitmap.asImageBitmap())
    }
}

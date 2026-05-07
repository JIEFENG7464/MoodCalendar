package com.moodcalendar.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Common Mood Colors (used across all themes) ──

val MoodHappy = Color(0xFFFFD93D)
val MoodSad = Color(0xFF7EC8E3)
val MoodAngry = Color(0xFFFF6B6B)
val MoodSleepy = Color(0xFFB39DDB)
val MoodLoved = Color(0xFFFF8FAB)
val MoodNeutral = Color(0xFFB0BEC5)
val MoodSurprised = Color(0xFFFFB74D)
val MoodCool = Color(0xFF81D4FA)

// ── Amber / Warm (Original) ──

object AmberTheme {
    val Light = lightColorScheme(
        primary = Color(0xFFD4925A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFEDDE),
        onPrimaryContainer = Color(0xFF3D220B),
        secondary = Color(0xFF7DAA92),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDEF0E4),
        onSecondaryContainer = Color(0xFF002114),
        tertiary = Color(0xFFC4898A),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE4E4),
        onTertiaryContainer = Color(0xFF442122),
        background = Color(0xFFFEF8F3),
        onBackground = Color(0xFF1C0F08),
        surface = Color(0xFFFFFCF8),
        onSurface = Color(0xFF1C0F08),
        surfaceVariant = Color(0xFFF0EAE4),
        onSurfaceVariant = Color(0xFF5E4E42),
        outline = Color(0xFFD0C1B6),
        outlineVariant = Color(0xFFE0D3C8),
    )
    val Dark = darkColorScheme(
        primary = Color(0xFFF2B77B),
        onPrimary = Color(0xFF3D220B),
        primaryContainer = Color(0xFF563419),
        onPrimaryContainer = Color(0xFFFFEDDE),
        secondary = Color(0xFFA2D4B7),
        onSecondary = Color(0xFF002114),
        secondaryContainer = Color(0xFF1B3929),
        onSecondaryContainer = Color(0xFFDEF0E4),
        tertiary = Color(0xFFE8B8B8),
        onTertiary = Color(0xFF442122),
        tertiaryContainer = Color(0xFF613637),
        onTertiaryContainer = Color(0xFFFFE4E4),
        background = Color(0xFF14120F),
        onBackground = Color(0xFFEBE1DA),
        surface = Color(0xFF1C1916),
        onSurface = Color(0xFFEBE1DA),
        surfaceVariant = Color(0xFF4E453C),
        onSurfaceVariant = Color(0xFFCBC0B6),
        outline = Color(0xFF958B81),
        outlineVariant = Color(0xFF4E453C),
    )
}

// ── Teal / 水鸭绿 ──

object TealTheme {
    val Light = lightColorScheme(
        primary = Color(0xFF3A8A7A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD4F5EC),
        onPrimaryContainer = Color(0xFF00241D),
        secondary = Color(0xFF4DA6A0),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCEF6F0),
        onSecondaryContainer = Color(0xFF002724),
        tertiary = Color(0xFF6B8FAB),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD1E4F8),
        onTertiaryContainer = Color(0xFF001E30),
        background = Color(0xFFF2FBF8),
        onBackground = Color(0xFF0A1C18),
        surface = Color(0xFFF5FDF9),
        onSurface = Color(0xFF0A1C18),
        surfaceVariant = Color(0xFFE0ECE7),
        onSurfaceVariant = Color(0xFF404E49),
        outline = Color(0xFFBCCDC7),
        outlineVariant = Color(0xFFD1E2DB),
    )
    val Dark = darkColorScheme(
        primary = Color(0xFF72CFBA),
        onPrimary = Color(0xFF00382E),
        primaryContainer = Color(0xFF005143),
        onPrimaryContainer = Color(0xFFD4F5EC),
        secondary = Color(0xFF8DDBD4),
        onSecondary = Color(0xFF003D39),
        secondaryContainer = Color(0xFF1B5652),
        onSecondaryContainer = Color(0xFFCEF6F0),
        tertiary = Color(0xFFAFC9E4),
        onTertiary = Color(0xFF153348),
        tertiaryContainer = Color(0xFF2E4A5F),
        onTertiaryContainer = Color(0xFFD1E4F8),
        background = Color(0xFF0F1F1B),
        onBackground = Color(0xFFD7E5DF),
        surface = Color(0xFF151F1C),
        onSurface = Color(0xFFD7E5DF),
        surfaceVariant = Color(0xFF3F4D48),
        onSurfaceVariant = Color(0xFFBFCECA),
        outline = Color(0xFF8A9C96),
        outlineVariant = Color(0xFF3F4D48),
    )
}

// ── Pink / 哔哩粉 ──

object PinkTheme {
    val Light = lightColorScheme(
        primary = Color(0xFFD4386A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF400016),
        secondary = Color(0xFFB05A7A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E2),
        onSecondaryContainer = Color(0xFF3E172A),
        tertiary = Color(0xFF7C5D9E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEDDEFF),
        onTertiaryContainer = Color(0xFF2D0052),
        background = Color(0xFFFFF8F8),
        onBackground = Color(0xFF1F1417),
        surface = Color(0xFFFFFBFA),
        onSurface = Color(0xFF1F1417),
        surfaceVariant = Color(0xFFFFE4E9),
        onSurfaceVariant = Color(0xFF574348),
        outline = Color(0xFFD9C0C6),
        outlineVariant = Color(0xFFEED5DB),
    )
    val Dark = darkColorScheme(
        primary = Color(0xFFFFB0C8),
        onPrimary = Color(0xFF5E0028),
        primaryContainer = Color(0xFF990043),
        onPrimaryContainer = Color(0xFFFFD9E2),
        secondary = Color(0xFFE8B8CC),
        onSecondary = Color(0xFF462032),
        secondaryContainer = Color(0xFF643649),
        onSecondaryContainer = Color(0xFFFFD9E2),
        tertiary = Color(0xFFD5B9F9),
        onTertiary = Color(0xFF3E006B),
        tertiaryContainer = Color(0xFF5E3582),
        onTertiaryContainer = Color(0xFFEDDEFF),
        background = Color(0xFF1F1417),
        onBackground = Color(0xFFF2DDE1),
        surface = Color(0xFF1F1719),
        onSurface = Color(0xFFF2DDE1),
        surfaceVariant = Color(0xFF574348),
        onSurfaceVariant = Color(0xFFDDC2C8),
        outline = Color(0xFFA48D92),
        outlineVariant = Color(0xFF574348),
    )
}

// ── Orange / 活力橙 ──

object OrangeTheme {
    val Light = lightColorScheme(
        primary = Color(0xFFE07A2F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFEDD6),
        onPrimaryContainer = Color(0xFF3B1D00),
        secondary = Color(0xFFC48A4A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFEED5),
        onSecondaryContainer = Color(0xFF3E2300),
        tertiary = Color(0xFFB35E5E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDAD8),
        onTertiaryContainer = Color(0xFF3E001D),
        background = Color(0xFFFFF9F5),
        onBackground = Color(0xFF201609),
        surface = Color(0xFFFFFCF8),
        onSurface = Color(0xFF201609),
        surfaceVariant = Color(0xFFFFECDD),
        onSurfaceVariant = Color(0xFF5C4A39),
        outline = Color(0xFFDDCBBB),
        outlineVariant = Color(0xFFF2DECE),
    )
    val Dark = darkColorScheme(
        primary = Color(0xFFFFB869),
        onPrimary = Color(0xFF4D2800),
        primaryContainer = Color(0xFF733E00),
        onPrimaryContainer = Color(0xFFFFEDD6),
        secondary = Color(0xFFF5BF81),
        onSecondary = Color(0xFF4D2E00),
        secondaryContainer = Color(0xFF704300),
        onSecondaryContainer = Color(0xFFFFEED5),
        tertiary = Color(0xFFFFB3B0),
        onTertiary = Color(0xFF580033),
        tertiaryContainer = Color(0xFF7F2942),
        onTertiaryContainer = Color(0xFFFFDAD8),
        background = Color(0xFF1C150C),
        onBackground = Color(0xFFF0E1D4),
        surface = Color(0xFF1D1812),
        onSurface = Color(0xFFF0E1D4),
        surfaceVariant = Color(0xFF5C4A39),
        onSurfaceVariant = Color(0xFFD8C8B8),
        outline = Color(0xFFA09081),
        outlineVariant = Color(0xFF5C4A39),
    )
}

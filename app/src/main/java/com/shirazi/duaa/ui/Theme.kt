package com.shirazi.duaa.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===== لوحة الألوان: مخطوط بنّي/زمرّدي/ذهبي =====
val Emerald = Color(0xFF0E5C45)
val EmeraldDeep = Color(0xFF0B3B2E)
val Gold = Color(0xFFB08A2E)
val GoldSoft = Color(0xFFD9C28A)

val PaperBg = Color(0xFFF3EEE2)
val PaperSurface = Color(0xFFFBF8F0)
val PaperSurface2 = Color(0xFFEFE8D8)
val InkColor = Color(0xFF26211A)
val Ink2Color = Color(0xFF6B6353)
val LineColor = Color(0xFFE0D7C2)

val DarkBg = Color(0xFF0C1714)
val DarkSurface = Color(0xFF122019)
val DarkSurface2 = Color(0xFF18291F)
val DarkInk = Color(0xFFEAE4D2)
val DarkInk2 = Color(0xFF9CA792)
val DarkLine = Color(0xFF23362B)
val EmeraldLight = Color(0xFF3FA37F)
val GoldLight = Color(0xFFD4B05E)

val Danger = Color(0xFF9C3A2E)
val DangerD = Color(0xFFD87C6E)
val Good = Color(0xFF1F7A4D)
val GoodD = Color(0xFF5FBE8E)
val Mid = Color(0xFFA87514)
val MidD = Color(0xFFD8A53F)

private val LightScheme = lightColorScheme(
    primary = Emerald, onPrimary = Color.White,
    secondary = Gold, onSecondary = Color.White,
    background = PaperBg, onBackground = InkColor,
    surface = PaperSurface, onSurface = InkColor,
    surfaceVariant = PaperSurface2, onSurfaceVariant = Ink2Color,
    outline = LineColor, error = Danger
)
private val DarkScheme = darkColorScheme(
    primary = EmeraldLight, onPrimary = Color(0xFF06120D),
    secondary = GoldLight, onSecondary = Color(0xFF1A1405),
    background = DarkBg, onBackground = DarkInk,
    surface = DarkSurface, onSurface = DarkInk,
    surfaceVariant = DarkSurface2, onSurfaceVariant = DarkInk2,
    outline = DarkLine, error = DangerD
)

/** ألوان دلالية إضافية تتبع الثيم. */
class DuaaPalette(
    val gold: Color, val goldSoft: Color, val emerald: Color, val emeraldDeep: Color,
    val ink2: Color, val line: Color, val surface2: Color,
    val danger: Color, val good: Color, val mid: Color, val dark: Boolean
)
fun palette(dark: Boolean) = if (dark) DuaaPalette(
    GoldLight, Color(0xFF7A6536), EmeraldLight, EmeraldDeep, DarkInk2, DarkLine, DarkSurface2,
    DangerD, GoodD, MidD, true
) else DuaaPalette(
    Gold, GoldSoft, Emerald, EmeraldDeep, Ink2Color, LineColor, PaperSurface2,
    Danger, Good, Mid, false
)

@Composable
fun DuaaTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

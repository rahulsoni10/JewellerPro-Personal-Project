package com.jeweldesk.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Gold {
    val Main = Color(0xFFF2C443)
    val Light = Color(0xFFFFDE6E)
    val Deep = Color(0xFFD9A82A)
    val Text = Color(0xFFD4A21C)
    val Green = Color(0xFF1E9E5A)
    val Red = Color(0xFFE5484D)
    val Coral = Color(0xFFF06A6A)
    val brush = Brush.horizontalGradient(listOf(Light, Main, Deep))
    val softBrush = Brush.horizontalGradient(listOf(Light.copy(alpha = 0.45f), Deep.copy(alpha = 0.45f)))
}

data class JColors(
    val dark: Boolean,
    val bg: Color,
    val surface: Color,
    val header: Color,
    val headerBorder: Color,
    val border: Color,
    val text: Color,
    val sub: Color,
    val tile: Color,
    val mint: Color,
    val pink: Color,
    val cream: Color,
    val nav: Color,
    val navPill: Color,
    val field: Color,
    val empty: Color,
)

val LightJ = JColors(
    dark = false,
    bg = Color(0xFFFAF9F6),
    surface = Color.White,
    header = Color(0xFFF6EBDD),
    headerBorder = Color(0xFFF1DFB8),
    border = Color(0xFFEBE1CF),
    text = Color(0xFF1C1B19),
    sub = Color(0xFF7A6F62),
    tile = Color(0xFFFFF4D6),
    mint = Color(0xFFEEF4F1),
    pink = Color(0xFFFDF0F1),
    cream = Color(0xFFFFFBF2),
    nav = Color(0xFFF5ECDD),
    navPill = Color(0xFFF8E3B0),
    field = Color(0xFFF7F6F3),
    empty = Color(0xFFCFC5B5),
)

val DarkJ = JColors(
    dark = true,
    bg = Color(0xFF12110F),
    surface = Color(0xFF1C1A17),
    header = Color(0xFF2A241B),
    headerBorder = Color(0xFF4A3D24),
    border = Color(0xFF3A3328),
    text = Color(0xFFF3EEE6),
    sub = Color(0xFFB3A796),
    tile = Color(0xFF3A3020),
    mint = Color(0xFF1E2B26),
    pink = Color(0xFF2E1F22),
    cream = Color(0xFF26221A),
    nav = Color(0xFF221E18),
    navPill = Color(0xFF4A3D24),
    field = Color(0xFF24211D),
    empty = Color(0xFF5A5246),
)

val LocalJ = staticCompositionLocalOf { LightJ }

private val typography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun JewelTheme(dark: Boolean, content: @Composable () -> Unit) {
    val j = if (dark) DarkJ else LightJ
    val scheme = if (dark) darkColorScheme(
        primary = Gold.Main, onPrimary = Color.Black, secondary = Gold.Deep, background = j.bg, surface = j.surface,
        onBackground = j.text, onSurface = j.text, surfaceVariant = j.field, onSurfaceVariant = j.sub,
        outline = j.border, primaryContainer = j.tile, onPrimaryContainer = j.text, surfaceContainerHigh = j.surface,
        surfaceContainer = j.surface, surfaceContainerLow = j.surface, surfaceContainerHighest = j.field, error = Gold.Red,
    ) else lightColorScheme(
        primary = Gold.Main, onPrimary = Color.Black, secondary = Gold.Deep, background = j.bg, surface = j.surface,
        onBackground = j.text, onSurface = j.text, surfaceVariant = j.field, onSurfaceVariant = j.sub,
        outline = j.border, primaryContainer = j.tile, onPrimaryContainer = j.text, surfaceContainerHigh = j.surface,
        surfaceContainer = j.surface, surfaceContainerLow = j.surface, surfaceContainerHighest = j.field, error = Gold.Red,
    )
    CompositionLocalProvider(LocalJ provides j) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}

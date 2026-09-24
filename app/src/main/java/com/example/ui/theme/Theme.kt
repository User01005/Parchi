package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WoodenFrameDark,
    onPrimary = Color.White,
    primaryContainer = WoodenBezel,
    onPrimaryContainer = WoodenFrameDark,
    secondary = PrintPrimaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = SharePrimaryGreen,
    onTertiary = Color.White,
    background = CanvasBackground,
    onBackground = InkBlack,
    surface = ReceiptPaperWhite,
    onSurface = InkBlack,
    surfaceVariant = Color(0xFFF3EEE7),
    onSurfaceVariant = InkMuted,
    outline = ReceiptBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

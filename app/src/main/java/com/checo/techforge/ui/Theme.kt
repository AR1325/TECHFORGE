package com.checo.techforge.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TechForgeColors = darkColorScheme(
    primary = Color(0xFF67E2CD),
    onPrimary = Color(0xFF06231F),
    primaryContainer = Color(0xFF123C37),
    onPrimaryContainer = Color(0xFFB7F7EA),
    secondary = Color(0xFF9BA8FF),
    onSecondary = Color(0xFF10163D),
    tertiary = Color(0xFFFFBE82),
    background = Color(0xFF090E15),
    onBackground = Color(0xFFE8F0F7),
    surface = Color(0xFF101923),
    onSurface = Color(0xFFE8F0F7),
    surfaceVariant = Color(0xFF192635),
    onSurfaceVariant = Color(0xFFAAB9C9),
    outline = Color(0xFF34485A),
    error = Color(0xFFFF8D9B)
)

@Composable
fun TechForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TechForgeColors, typography = MaterialTheme.typography, content = content)
}

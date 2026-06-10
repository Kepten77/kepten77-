package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MinecraftColorScheme = darkColorScheme(
    primary = McGrass,
    secondary = McDirt,
    tertiary = McGold,
    background = McDarkStone,
    surface = McStone,
    onPrimary = McWhite,
    onSecondary = McWhite,
    onTertiary = Color(0xFF1E1E1E),
    onBackground = McWhite,
    onSurface = McWhite
)

val MinecraftShapes = Shapes(
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MinecraftColorScheme,
        typography = Typography,
        shapes = MinecraftShapes,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val greenShades = listOf(
    Color(0xFF5C8E32), // McGrass Base
    Color(0xFF4D7A1E), // Darker green
    Color(0xFF3D6318), // Deep shadow green
    Color(0xFF6BB343), // Vivid light green
    Color(0xFF42681D)  // Moss green
)

private val dirtShades = listOf(
    Color(0xFF866043), // McDirt Base
    Color(0xFF745136), // Dense loam brown
    Color(0xFF5C3C24), // Shadow dirt
    Color(0xFF9E7759), // Lighter dry loam
    Color(0xFF4A311D), // Darkest soil
    Color(0xFF6C4B33)  // Clay dirt
)

// A plain non-observable JVM holder to prevent triggering infinite rebuild/draw recomposition cycles that lead to immediate crashes.
private class BackgroundCache {
    var lastSize: Size = Size.Zero
    var backgroundBitmap: ImageBitmap? = null
}

@Composable
fun MinecraftBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val cache = remember { BackgroundCache() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (size.width <= 0f || size.height <= 0f) return@drawBehind

                if (cache.backgroundBitmap == null || size != cache.lastSize) {
                    val width = size.width.toInt()
                    val height = size.height.toInt()
                    val bitmap = ImageBitmap(width, height)
                    val canvas = Canvas(bitmap)
                    val paint = Paint()

                    val cellSize = with(density) { 18.dp.toPx() }
                    val cols = (size.width / cellSize).toInt() + 1
                    val rows = (size.height / cellSize).toInt() + 1

                    for (r in 0 until rows) {
                        val y = r * cellSize
                        for (c in 0 until cols) {
                            val x = c * cellSize

                            val hash = (r * 31337 + c * 1337) and 0xFFFF
                            val waveOffset = (Math.sin(c.toDouble() * 0.7) * 1.6).toInt()
                            val grassDepth = 4 + waveOffset + (if (c % 3 == 0) 1 else 0)

                            val isGrass = r < grassDepth || (r == grassDepth && (hash % 10 < 4))

                            val color = if (isGrass) {
                                greenShades[hash % greenShades.size]
                            } else {
                                val isPebble = (hash % 120) < 4
                                if (isPebble) {
                                    Color(0xFF737373)
                                } else {
                                    dirtShades[hash % dirtShades.size]
                                }
                            }

                            paint.color = color
                            canvas.drawRect(
                                Rect(x, y, x + cellSize + 1f, y + cellSize + 1f),
                                paint
                            )
                        }
                    }
                    cache.backgroundBitmap = bitmap
                    cache.lastSize = size
                }

                cache.backgroundBitmap?.let { bitmap ->
                    drawImage(bitmap)
                }
            }
    ) {
        // Fullscreen darkened overlay (like in-game GUI style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        // Contents box
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

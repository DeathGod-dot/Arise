package com.example.arise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.arise.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun HexMeshBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val bgColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "meshPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLight) 0.08f else 0.03f,
        targetValue = if (isLight) 0.15f else 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .drawWithCache {
                val r = 28.dp.toPx()
                val dx = 1.5f * r
                val dy = sqrt(3f) * r
                val w = size.width
                val h = size.height

                val meshPath = Path()
                var col = 0
                var x = 0f
                while (x < w + r * 2) {
                    val yOff = if (col % 2 == 1) dy / 2f else 0f
                    var y = yOff - dy
                    while (y < h + dy * 2) {
                        for (i in 0 until 6) {
                            val angle = i * (PI / 3).toFloat()
                            val px = x + r * cos(angle)
                            val py = y + r * sin(angle)
                            if (i == 0) meshPath.moveTo(px, py) else meshPath.lineTo(px, py)
                        }
                        meshPath.close()
                        y += dy
                    }
                    x += dx
                    col++
                }

                onDrawBehind {
                    if (isLight) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE8F5E9), // Soft Mint Green
                                    Color(0xFFFFF7ED)  // Warm Peach Gradient
                                )
                            )
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(w / 2f, h / 2f),
                                radius = w * 0.7f
                            )
                        )
                        drawPath(
                            path = meshPath,
                            color = primaryColor.copy(alpha = pulseAlpha),
                            style = Stroke(width = 0.5.dp.toPx())
                        )
                    } else {
                        drawRect(color = bgColor)
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.06f), Color.Transparent),
                                center = Offset(w / 2f, h / 2f),
                                radius = w * 0.7f
                            )
                        )
                        drawPath(
                            path = meshPath,
                            color = primaryColor.copy(alpha = pulseAlpha),
                            style = Stroke(width = 0.5.dp.toPx())
                        )
                    }
                }
            }
    ) {
        content()
    }
}

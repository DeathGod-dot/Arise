package com.example.arise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.ui.theme.*

@Composable
fun ScanBeamProgressBar(
    current: Int,
    max: Int,
    label: String = "",
    barColor: Color,
    modifier: Modifier = Modifier,
    showTextRow: Boolean = label.isNotEmpty()
) {
    val progress = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progFill"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "scanBeam")
    val beamOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beamOffset"
    )

    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val activeBarColor = if (isLight && (barColor == ArisePrimary || barColor == Color(0xFF00D4FF))) androidx.compose.material3.MaterialTheme.colorScheme.primary else barColor
    val trackBgColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color(0xFF0C141F)

    Column(modifier = modifier) {
        if (showTextRow) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = activeBarColor,
                    style = SystemLabel,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$current / $max",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    style = SystemLabel,
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val w = size.width
            val h = size.height
            val cut = 4.dp.toPx()
            val fillW = (w * animatedProgress).coerceAtLeast(cut * 2)

            // Track
            val trackPath = Path().apply {
                moveTo(cut, 0f)
                lineTo(w, 0f)
                lineTo(w - cut, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = trackPath, color = trackBgColor)
            drawPath(path = trackPath, color = activeBarColor.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))

            if (animatedProgress > 0f) {
                val fillPath = Path().apply {
                    moveTo(cut, 0f)
                    lineTo(fillW, 0f)
                    lineTo(fillW - cut, h)
                    lineTo(0f, h)
                    close()
                }

                // Gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(activeBarColor.copy(alpha = 0.5f), activeBarColor)
                    )
                )

                // Scan-beam
                val beamStartX = fillW * (beamOffset - 0.2f)
                val beamEndX = fillW * (beamOffset + 0.2f)
                val beamBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.7f),
                        activeBarColor.copy(alpha = 0.9f),
                        Color.Transparent
                    ),
                    start = Offset(beamStartX, 0f),
                    end = Offset(beamEndX, h)
                )
                drawPath(path = fillPath, brush = beamBrush)

                // Micro-scanlines
                var scanlineY = 2f
                while (scanlineY < h) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.25f),
                        start = Offset(0f, scanlineY),
                        end = Offset(fillW, scanlineY),
                        strokeWidth = 1f
                    )
                    scanlineY += 4.dp.toPx()
                }

                // Leading edge
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(fillW, 0f),
                    end = Offset(fillW - cut, h),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

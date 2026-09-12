package com.example.arise.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.arise.ui.theme.*

@Composable
fun ECGWaveform(
    modifier: Modifier = Modifier.fillMaxWidth().height(60.dp),
    lineColor: Color = ArisePrimary,
    cycleDurationMs: Int = 1800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ecg")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Dim baseline
        drawLine(lineColor.copy(alpha = 0.1f), Offset(0f, midY), Offset(w, midY), strokeWidth = 0.5.dp.toPx())

        // Build ECG path
        val fullPath = Path().apply {
            moveTo(0f, midY)
            val cycleW = 120.dp.toPx()
            var cx = 0f
            while (cx < w + cycleW) {
                lineTo(cx + cycleW * 0.2f, midY)
                lineTo(cx + cycleW * 0.25f, midY - h * 0.12f)
                lineTo(cx + cycleW * 0.3f, midY)
                lineTo(cx + cycleW * 0.4f, midY)
                lineTo(cx + cycleW * 0.43f, midY + h * 0.1f)
                lineTo(cx + cycleW * 0.48f, midY - h * 0.42f)
                lineTo(cx + cycleW * 0.53f, midY + h * 0.2f)
                lineTo(cx + cycleW * 0.58f, midY)
                lineTo(cx + cycleW * 0.68f, midY - h * 0.15f)
                lineTo(cx + cycleW * 0.78f, midY)
                lineTo(cx + cycleW, midY)
                cx += cycleW
            }
        }

        val pm = PathMeasure()
        pm.setPath(fullPath, false)
        val totalLen = pm.length
        val scanHead = sweepProgress * totalLen
        val trailLen = totalLen * 0.2f

        val segment = Path()
        val startDist = (scanHead - trailLen).coerceAtLeast(0f)
        pm.getSegment(startDist, scanHead, segment)

        // Glow
        drawIntoCanvas { canvas ->
            val glow = Paint().asFrameworkPaint().apply {
                this.color = lineColor.copy(alpha = 0.5f).toArgb()
                this.style = android.graphics.Paint.Style.STROKE
                this.strokeWidth = 4.dp.toPx()
                this.maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawPath(segment.asAndroidPath(), glow)
        }

        // Crisp line
        drawPath(segment, color = lineColor, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        // Leading dot
        val headPos = pm.getPosition(scanHead)
        if (headPos != Offset.Unspecified) {
            drawCircle(Color.White, radius = 3.dp.toPx(), center = headPos)
            drawCircle(lineColor.copy(alpha = 0.4f), radius = 6.dp.toPx(), center = headPos)
        }
    }
}

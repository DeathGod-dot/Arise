package com.example.arise.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.ui.theme.*

@Composable
fun WeeklyChart(
    dataPoints: List<Int>,
    labels: List<String> = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
    modifier: Modifier = Modifier.fillMaxWidth().height(160.dp),
    lineColor: Color = Color.Unspecified
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val activeLineColor = if (lineColor != Color.Unspecified) lineColor else if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.primary else ArisePrimary
    val animatedPoints = dataPoints.mapIndexed { i, value ->
        val animValue by animateFloatAsState(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "point_$i"
        )
        animValue
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dotPulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "dotScale"
    )

    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    val labelTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier.fillMaxWidth()
    ) {
        if (animatedPoints.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val maxVal = (animatedPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)

        val topPadding = 20.dp.toPx()
        val bottomPadding = 22.dp.toPx()
        val sidePadding = 18.dp.toPx()

        val chartW = w - sidePadding * 2
        val chartH = h - topPadding - bottomPadding
        val stepX = if (animatedPoints.size > 1) chartW / (animatedPoints.size - 1) else chartW

        val points = animatedPoints.mapIndexed { i, v ->
            Offset(
                sidePadding + i * stepX,
                topPadding + chartH * (1f - (v / maxVal))
            )
        }

        val baselineY = topPadding + chartH

        // Area fill
        val areaPath = Path().apply {
            moveTo(points.first().x, baselineY)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, baselineY)
            close()
        }
        drawPath(
            areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(activeLineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = topPadding, endY = baselineY
            )
        )

        // Line
        val linePath = Path().apply {
            points.forEachIndexed { i, pt ->
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
        }

        // Line glow
        drawIntoCanvas { canvas ->
            val glow = Paint().asFrameworkPaint().apply {
                this.color = activeLineColor.copy(alpha = 0.4f).toArgb()
                this.style = android.graphics.Paint.Style.STROKE
                this.strokeWidth = 4.dp.toPx()
                this.maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawPath(linePath.asAndroidPath(), glow)
        }

        drawPath(linePath, activeLineColor, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

        // Dots, Floating Value Labels & Day Labels
        points.forEachIndexed { i, pt ->
            val v = dataPoints.getOrNull(i) ?: 0
            val radius = if (i == points.lastIndex) 4.5.dp.toPx() * dotScale else 3.5.dp.toPx()
            drawCircle(activeLineColor, radius, pt)
            drawCircle(Color.White, 1.5.dp.toPx(), pt)

            if (v > 0) {
                val valueText = "+$v"
                val textLayoutResult = textMeasurer.measure(
                    text = valueText,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 8.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = activeLineColor
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = valueText,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 8.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = activeLineColor
                    ),
                    topLeft = Offset((pt.x - textLayoutResult.size.width / 2f).coerceIn(0f, w - textLayoutResult.size.width), pt.y - 14.dp.toPx())
                )
            }

            // Day Label directly below data point
            val label = labels.getOrNull(i) ?: ""
            if (label.isNotEmpty()) {
                val labelLayout = textMeasurer.measure(
                    text = label,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 9.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = labelTextColor
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 9.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = labelTextColor
                    ),
                    topLeft = Offset((pt.x - labelLayout.size.width / 2f).coerceIn(0f, w - labelLayout.size.width), h - 14.dp.toPx())
                )
            }
        }
    }
}

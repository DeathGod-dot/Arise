package com.example.arise.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class StatItem(val name: String, val value: Float, val maxValue: Float = 38f)

@Composable
fun StatRadarChart(
    stats: List<StatItem>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp),
    neonColor: Color = Color.Unspecified,
    gridColor: Color = Color.Unspecified
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val activeNeonColor = if (neonColor != Color.Unspecified) neonColor else if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.primary else Color(0xFF00E5FF)
    val activeGridColor = if (gridColor != Color.Unspecified) gridColor else if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFF2C3847)

    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTrigger = true }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "radarAnim"
    )

    val textMeasurer = rememberTextMeasurer()

    val statMap = stats.associateBy { it.name.uppercase() }
    val orderedStats = listOf(
        statMap["STR"] ?: StatItem("STR", 10f),
        statMap["AGI"] ?: StatItem("AGI", 12f),
        statMap["VIT"] ?: StatItem("VIT", 10f),
        statMap["INT"] ?: StatItem("INT", 8f),
        statMap["SEN"] ?: StatItem("SEN", 11f),
        statMap["MP"]  ?: StatItem("MP", 10f),
    )

    val maxStatVal = (orderedStats.maxOfOrNull { it.value } ?: 10f).coerceAtLeast(10f) * 1.2f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
            val gridAxesCount = 6
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (minOf(size.width, size.height) / 2f) * 0.78f
            val angleStep = (2 * PI / gridAxesCount).toFloat()

            // 1. Concentric Hexagon Grid Lines (3 levels)
            listOf(0.33f, 0.66f, 1f).forEach { level ->
                val r = maxRadius * level
                val hexPath = Path()
                for (i in 0 until gridAxesCount) {
                    val angle = i * angleStep - (PI.toFloat() / 2f)
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                }
                hexPath.close()
                drawPath(
                    path = hexPath,
                    color = activeGridColor.copy(alpha = if (level == 1f) 0.85f else 0.45f),
                    style = Stroke(width = if (level == 1f) 1.5.dp.toPx() else 0.8.dp.toPx())
                )
            }

            // 2. Radial Axis Spokes
            for (i in 0 until gridAxesCount) {
                val angle = i * angleStep - (PI.toFloat() / 2f)
                val x = center.x + maxRadius * cos(angle)
                val y = center.y + maxRadius * sin(angle)
                drawLine(
                    color = activeGridColor.copy(alpha = 0.5f),
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = 0.8.dp.toPx()
                )
            }

            // 3. Stat Data Polygon
            val statPath = Path()
            val points = mutableListOf<Offset>()
            orderedStats.forEachIndexed { i, stat ->
                val fraction = (stat.value / maxStatVal).coerceIn(0f, 1f) * animatedProgress
                val radius = maxRadius * fraction
                val angle = i * angleStep - (PI.toFloat() / 2f)
                val px = center.x + radius * cos(angle)
                val py = center.y + radius * sin(angle)
                points.add(Offset(px, py))
                if (i == 0) statPath.moveTo(px, py) else statPath.lineTo(px, py)
            }
            statPath.close()

            // Translucent Fill
            drawPath(
                path = statPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeNeonColor.copy(alpha = if (isLight) 0.35f else 0.4f),
                        activeNeonColor.copy(alpha = if (isLight) 0.15f else 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                )
            )

            // Crisp Glowing Outline
            drawPath(
                path = statPath,
                color = activeNeonColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Vertex Dots
            points.forEach { pt ->
                drawCircle(color = activeNeonColor, radius = 3.dp.toPx(), center = pt)
                drawCircle(color = Color.White, radius = 1.2.dp.toPx(), center = pt)
            }

            // 4. Draw Attribute Corner Badges & Text directly on Canvas
            val pillBgColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF0F172A).copy(alpha = 0.9f)
            val pillBorderColor = if (isLight) Color(0xFFCBD5E1) else Color(0xFF334155)
            val pillTextColor = if (isLight) Color(0xFF0F172A) else Color(0xFFE2E8F0)

            orderedStats.forEachIndexed { i, stat ->
                val angle = i * angleStep - (PI.toFloat() / 2f)
                val cornerX = center.x + maxRadius * cos(angle)
                val cornerY = center.y + maxRadius * sin(angle)

                val labelText = "${stat.name}: ${stat.value.roundToInt()}"
                val textLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pillTextColor
                    )
                )

                val tw = textLayoutResult.size.width.toFloat()
                val th = textLayoutResult.size.height.toFloat()

                val padX = 6.dp.toPx()
                val padY = 3.dp.toPx()

                val textX = when (i) {
                    0, 3 -> cornerX - tw / 2f
                    1, 2 -> cornerX + 8.dp.toPx()
                    else -> cornerX - tw - 8.dp.toPx()
                }

                val textY = when (i) {
                    0 -> cornerY - th - 6.dp.toPx()
                    3 -> cornerY + 6.dp.toPx()
                    else -> cornerY - th / 2f
                }

                // Theme-aware Pill Background behind corner text
                drawRoundRect(
                    color = pillBgColor,
                    topLeft = Offset(textX - padX, textY - padY),
                    size = Size(tw + padX * 2f, th + padY * 2f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = pillBorderColor,
                    topLeft = Offset(textX - padX, textY - padY),
                    size = Size(tw + padX * 2f, th + padY * 2f),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = 0.8.dp.toPx())
                )

                // Draw Text
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textX, textY)
                )
            }
        }
    }
}

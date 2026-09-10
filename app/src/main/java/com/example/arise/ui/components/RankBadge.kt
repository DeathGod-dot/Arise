package com.example.arise.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.domain.HunterRank
import com.example.arise.ui.theme.*

@Composable
fun RankBadge(
    rank: HunterRank,
    modifier: Modifier = Modifier,
    size: Int = 72
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .drawBehind {
                val center = this.center
                val radius = this.size.minDimension / 2f - 4.dp.toPx()

                // Outer glow
                drawIntoCanvas { canvas ->
                    val glowPaint = Paint().asFrameworkPaint().apply {
                        this.color = rank.color.copy(alpha = 0.5f).toArgb()
                        this.style = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = 3.dp.toPx()
                        this.maskFilter = BlurMaskFilter(8.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawCircle(center.x, center.y, radius, glowPaint)
                }

                // Border circle
                drawCircle(
                    color = rank.color.copy(alpha = 0.7f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Inner faint fill
                drawCircle(
                    color = rank.color.copy(alpha = 0.08f),
                    radius = radius
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.name,
            style = DisplayRank.copy(
                fontSize = (size * 0.45f).sp,
                color = rank.color
            ),
            fontWeight = FontWeight.ExtraBold
        )
    }
}

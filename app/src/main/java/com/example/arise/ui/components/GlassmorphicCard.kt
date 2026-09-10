package com.example.arise.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.arise.ui.theme.*

enum class CardVariant {
    DEFAULT, DANGER, PURPLE
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.DEFAULT,
    crtAnimation: Boolean = false,
    isOpen: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f

    val borderColor = when {
        isLight -> androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        variant == CardVariant.DEFAULT -> AriseGlassBorderActive
        variant == CardVariant.DANGER -> AriseDangerGlassBorder
        else -> AriseSecondaryDim.copy(alpha = 0.4f)
    }
    val fillColor = when {
        isLight -> androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest
        variant == CardVariant.DEFAULT -> AriseGlassWhite
        variant == CardVariant.DANGER -> AriseDangerGlass
        else -> AriseSecondaryDim.copy(alpha = 0.05f)
    }
    val accentColor = when {
        isLight -> androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        variant == CardVariant.DEFAULT -> ArisePrimary.copy(alpha = 0.8f)
        variant == CardVariant.DANGER -> AriseDangerRed.copy(alpha = 0.8f)
        else -> AriseSecondaryDim.copy(alpha = 0.8f)
    }

    val cardContent: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .drawBehind {
                    val cut = 12.dp.toPx()
                    val w = size.width
                    val h = size.height

                    val path = Path().apply {
                        moveTo(cut, 0f)
                        lineTo(w - cut, 0f)
                        lineTo(w, cut)
                        lineTo(w, h - cut)
                        lineTo(w - cut, h)
                        lineTo(cut, h)
                        lineTo(0f, h - cut)
                        lineTo(0f, cut)
                        close()
                    }

                    // Fill
                    drawPath(path = path, color = fillColor)

                    // Outer glow
                    drawIntoCanvas { canvas ->
                        val glowPaint = Paint().asFrameworkPaint().apply {
                            this.color = borderColor.toArgb()
                            this.style = android.graphics.Paint.Style.STROKE
                            this.strokeWidth = 2.dp.toPx()
                            this.maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                        canvas.nativeCanvas.drawPath(path.asAndroidPath(), glowPaint)
                    }

                    // Border
                    drawPath(
                        path = path,
                        color = borderColor,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // L-shaped corner accents
                    val bracketLen = 16.dp.toPx()
                    val bracketW = 2.dp.toPx()
                    // Top-left
                    drawLine(accentColor, Offset(0f, cut), Offset(0f, cut + bracketLen), strokeWidth = bracketW)
                    drawLine(accentColor, Offset(cut, 0f), Offset(cut + bracketLen, 0f), strokeWidth = bracketW)
                    // Top-right
                    drawLine(accentColor, Offset(w, cut), Offset(w, cut + bracketLen), strokeWidth = bracketW)
                    drawLine(accentColor, Offset(w - cut, 0f), Offset(w - cut - bracketLen, 0f), strokeWidth = bracketW)
                    // Bottom-left
                    drawLine(accentColor, Offset(0f, h - cut), Offset(0f, h - cut - bracketLen), strokeWidth = bracketW)
                    drawLine(accentColor, Offset(cut, h), Offset(cut + bracketLen, h), strokeWidth = bracketW)
                    // Bottom-right
                    drawLine(accentColor, Offset(w, h - cut), Offset(w, h - cut - bracketLen), strokeWidth = bracketW)
                    drawLine(accentColor, Offset(w - cut, h), Offset(w - cut - bracketLen, h), strokeWidth = bracketW)
                }
                .padding(16.dp),
            content = content
        )
    }

    if (crtAnimation) {
        CrtTileAnimation(
            isOpen = isOpen,
            beamColor = accentColor
        ) {
            cardContent()
        }
    } else {
        cardContent()
    }
}

package com.example.arise.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.arise.ui.theme.*

enum class ButtonVariant {
    PRIMARY, SECONDARY, GHOST, DANGER
}

@Composable
fun SystemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btnPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val displayAlpha = if (enabled) 1f else pulseAlpha
    val displayText = if (variant == ButtonVariant.GHOST) "[ $text ]" else text

    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f

    val primaryColor = if (isLight) AriseLightPrimary else ArisePrimary
    val onPrimaryColor = if (isLight) AriseLightOnPrimary else AriseBackground
    val disabledTextColor = if (isLight) AriseLightOnSurfaceVariant.copy(alpha = 0.6f) else AriseOnSurfaceVariant
    val disabledBorderColor = if (isLight) AriseLightOutline else AriseOutlineVariant

    val bgColor = when {
        !enabled -> Color.Transparent
        variant == ButtonVariant.PRIMARY -> primaryColor
        else -> Color.Transparent
    }
    val textColor = when {
        !enabled -> disabledTextColor
        variant == ButtonVariant.PRIMARY -> onPrimaryColor
        variant == ButtonVariant.DANGER -> AriseDangerRed
        variant == ButtonVariant.GHOST -> primaryColor
        else -> primaryColor
    }
    val borderColor = when {
        !enabled -> disabledBorderColor
        variant == ButtonVariant.DANGER -> AriseDangerRed.copy(alpha = 0.6f)
        variant == ButtonVariant.SECONDARY -> primaryColor.copy(alpha = 0.5f)
        variant == ButtonVariant.GHOST -> Color.Transparent
        variant == ButtonVariant.PRIMARY -> primaryColor
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .alpha(displayAlpha)
            .drawBehind {
                val w = size.width
                val h = size.height
                val cut = 8.dp.toPx()
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
                drawPath(path = path, color = bgColor)
                if (borderColor != Color.Transparent) {
                    drawPath(path = path, color = borderColor, style = Stroke(width = 1.dp.toPx()))
                }
                if (enabled && variant == ButtonVariant.PRIMARY) {
                    drawIntoCanvas { canvas ->
                        val glow = Paint().asFrameworkPaint().apply {
                            this.color = primaryColor.copy(alpha = 0.3f).toArgb()
                            this.style = android.graphics.Paint.Style.STROKE
                            this.strokeWidth = 2.dp.toPx()
                            this.maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                        canvas.nativeCanvas.drawPath(path.asAndroidPath(), glow)
                    }
                }
            }
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.invoke()
            Text(
                text = displayText.uppercase(),
                style = SystemLabelMedium.copy(color = textColor, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }
}

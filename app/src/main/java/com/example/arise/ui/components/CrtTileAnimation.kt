package com.example.arise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.arise.ui.theme.ArisePrimary

/**
 * Applies a retro CRT TV power-on (opening) and power-off (closing) animation to any tile/card.
 *
 * Opening sequence:
 *   1. Horizontal beam expands from center (scaleX 0.05 -> 1.0, scaleY ~0.03) with bright CRT line flash.
 *   2. Vertical screen opens up (scaleY 0.03 -> 1.0) with smooth fast easing & opacity ramp.
 *
 * Closing sequence:
 *   1. Screen collapses vertically into a thin horizontal line (scaleY 1.0 -> 0.03).
 *   2. Beam collapses horizontally to a center flash dot (scaleX 1.0 -> 0.0) and fades out.
 */
@Composable
fun CrtTileAnimation(
    modifier: Modifier = Modifier,
    isOpen: Boolean = true,
    beamColor: Color = ArisePrimary,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(targetState = isOpen, label = "crt_power")

    // Phase 1: ScaleX (Horizontal CRT beam width)
    val scaleX by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                // Opening: Expand horizontally first (0ms..120ms)
                tween(durationMillis = 160, easing = FastOutSlowInEasing)
            } else {
                // Closing: Collapse horizontally second (100ms..200ms)
                keyframes {
                    durationMillis = 200
                    1.0f at 0
                    1.0f at 90
                    0.0f at 200 using FastOutLinearInEasing
                }
            }
        },
        label = "crt_scaleX"
    ) { open -> if (open) 1f else 0f }

    // Phase 2: ScaleY (Vertical CRT screen height)
    val scaleY by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                // Opening: Expand vertically after horizontal beam (70ms..220ms)
                keyframes {
                    durationMillis = 220
                    0.03f at 0
                    0.06f at 70
                    1.0f at 220 using FastOutSlowInEasing
                }
            } else {
                // Closing: Squish vertically first (0ms..90ms)
                tween(durationMillis = 90, easing = LinearOutSlowInEasing)
            }
        },
        label = "crt_scaleY"
    ) { open -> if (open) 1f else 0.03f }

    // Alpha transition
    val alpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 160)
            } else {
                tween(durationMillis = 180, delayMillis = 20)
            }
        },
        label = "crt_alpha"
    ) { open -> if (open) 1f else 0f }

    // CRT Center Beam Flash Intensity
    val flashIntensity by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                keyframes {
                    durationMillis = 220
                    1.0f at 0
                    0.85f at 70
                    0.0f at 220
                }
            } else {
                keyframes {
                    durationMillis = 200
                    0.0f at 0
                    1.0f at 90
                    0.0f at 200
                }
            }
        },
        label = "crt_flash"
    ) { 0f }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
                this.alpha = alpha.coerceIn(0f, 1f)
                this.transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .drawWithContent {
                drawContent()
                if (flashIntensity > 0.02f) {
                    val centerY = size.height / 2f
                    // Center CRT beam line
                    drawLine(
                        color = Color.White.copy(alpha = flashIntensity),
                        start = Offset(0f, centerY),
                        end = Offset(size.width, centerY),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = beamColor.copy(alpha = flashIntensity * 0.85f),
                        start = Offset(0f, centerY),
                        end = Offset(size.width, centerY),
                        strokeWidth = 7.dp.toPx()
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * A CRT Tile component that can be tapped to toggle power ON (opening) and OFF (closing).
 */
@Composable
fun InteractiveCrtTile(
    modifier: Modifier = Modifier,
    initialOpen: Boolean = true,
    beamColor: Color = ArisePrimary,
    onToggle: ((Boolean) -> Unit)? = null,
    content: @Composable (isOpen: Boolean, toggle: () -> Unit) -> Unit
) {
    var isOpen by remember { mutableStateOf(initialOpen) }

    fun toggle() {
        isOpen = !isOpen
        onToggle?.invoke(isOpen)
    }

    CrtTileAnimation(
        modifier = modifier,
        isOpen = isOpen,
        beamColor = beamColor
    ) {
        content(isOpen) { toggle() }
    }
}

package com.example.arise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun GlitchEntryAnimation(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = LinearEasing),
        label = "entryAlpha"
    )
    val clipFraction by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "clip"
    )
    val offsetX by animateFloatAsState(
        targetValue = if (visible) 0f else (if (index % 2 == 0) 20f else -20f),
        animationSpec = tween(400),
        label = "offsetX"
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .graphicsLayer {
                translationX = offsetX
                clip = true
                scaleY = clipFraction.coerceAtLeast(0.01f)
            }
    ) {
        content()
    }
}

package com.example.arise.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Wraps content with a smooth fade-in + slide-up entrance animation.
 * Use `delayMillis` to create staggered effects for sequential items.
 */
@Composable
fun AnimatedScreenEntrance(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 200,
    initialOffsetY: Int = 0,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}

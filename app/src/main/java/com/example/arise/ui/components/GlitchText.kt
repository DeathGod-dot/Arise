package com.example.arise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.arise.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(
        fontFamily = SoraFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        letterSpacing = 2.sp,
    ),
    isGlitching: Boolean = true,
    baseColor: Color = AriseOnSurface
) {
    var offsetX by remember { mutableIntStateOf(0) }
    var alphaFlicker by remember { mutableFloatStateOf(1f) }
    var chromaticSplit by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isGlitching) {
        if (!isGlitching) return@LaunchedEffect
        while (true) {
            val shouldGlitch = Random.nextFloat() > 0.65f
            if (shouldGlitch) {
                offsetX = Random.nextInt(-6, 7)
                alphaFlicker = Random.nextFloat().coerceIn(0.7f, 1f)
                chromaticSplit = Random.nextInt(2, 6).toFloat()
                delay(Random.nextLong(40, 90))
            } else {
                offsetX = 0
                alphaFlicker = 1f
                chromaticSplit = 0f
                delay(Random.nextLong(200, 800))
            }
        }
    }

    val resolvedColor = when (baseColor) {
        AriseOnSurface -> androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        ArisePrimary -> androidx.compose.material3.MaterialTheme.colorScheme.primary
        else -> baseColor
    }

    Box(modifier = modifier) {
        if (chromaticSplit > 0f) {
            Text(
                text = text,
                style = style.copy(color = resolvedColor.copy(alpha = 0.6f)),
                modifier = Modifier.offset { IntOffset((-chromaticSplit).toInt(), 0) }
            )
            Text(
                text = text,
                style = style.copy(color = Color(0xFFFF0055).copy(alpha = 0.6f)),
                modifier = Modifier.offset { IntOffset(chromaticSplit.toInt(), 0) }
            )
        }
        Text(
            text = text,
            style = style.copy(color = resolvedColor),
            modifier = Modifier
                .offset { IntOffset(offsetX, 0) }
                .alpha(alphaFlicker)
        )
    }
}

package com.example.arise.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.ui.theme.*

@Composable
fun WarningBanner(
    title: String = "PENALTY QUEST WARNING",
    timeFormatted: String = "04:23:11",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warnPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(pulseAlpha),
        variant = CardVariant.DANGER
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filled warning triangle icon
            Box(
                modifier = Modifier
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AriseDangerRed,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = SystemLabel.copy(
                        color = AriseDangerRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )

                val onSurf = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                val onSurfVar = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

                val annotatedMessage = buildAnnotatedString {
                    append("Failure to complete physical objectives within ")
                    withStyle(SpanStyle(color = onSurf, fontWeight = FontWeight.Bold)) {
                        append(timeFormatted)
                    }
                    append(" will result in forced teleportation to the ")
                    withStyle(SpanStyle(color = onSurf, fontWeight = FontWeight.Bold)) {
                        append("Penalty Zone (Desert Survival)")
                    }
                    append(". Survival is not guaranteed.")
                }

                Text(
                    text = annotatedMessage,
                    style = SystemLabel.copy(
                        color = onSurfVar,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

package com.example.arise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.domain.LogIcon
import com.example.arise.ui.theme.*

@Composable
fun SystemLogEntryItem(
    icon: LogIcon,
    message: String,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val iconColor = when (icon) {
        LogIcon.STAT_UP -> AriseTertiary
        LogIcon.QUEST_COMPLETE -> ArisePrimary
        LogIcon.WARNING -> AriseDangerRed
        LogIcon.INFO -> AriseOnSurfaceVariant
    }

    val elapsed = System.currentTimeMillis() - timestamp
    val timeText = when {
        elapsed < 60_000 -> "JUST NOW"
        elapsed < 3_600_000 -> "${elapsed / 60_000} MINS AGO"
        elapsed < 86_400_000 -> "${elapsed / 3_600_000} HOUR AGO"
        else -> "${elapsed / 86_400_000} DAYS AGO"
    }

    val alpha = if (elapsed > 3_600_000) 0.6f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon.symbol,
                color = iconColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Message + timestamp stacked
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = message,
                style = AriseTypography.bodyMedium.copy(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                ),
            )
            Text(
                text = timeText,
                style = SystemLabel.copy(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                )
            )
        }
    }
}

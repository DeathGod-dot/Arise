package com.example.arise.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arise.domain.HunterRank
import com.example.arise.domain.LogIcon
import com.example.arise.ui.components.*
import com.example.arise.ui.components.AnimatedScreenEntrance
import com.example.arise.ui.theme.*
import com.example.arise.viewmodel.StatusViewModel

@Composable
fun StatusScreen(
    onNavigateToQuests: () -> Unit = {},
    viewModel: StatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ARISE wordmark — top-left aligned
        AnimatedScreenEntrance(delayMillis = 0) {
            GlitchText(
                text = "ARISE",
                style = DisplayRank.copy(fontSize = 22.sp, letterSpacing = 4.sp),
                baseColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Hunter Performance Report Card
        AnimatedScreenEntrance(delayMillis = 80) {
            GlitchEntryAnimation(index = 0) {
                HunterReportCard(
                    data = state.reportData
                )
            }
        }

        // Vitals Panel (HP, MP, XP matching screenshot)
        AnimatedScreenEntrance(delayMillis = 160) {
            GlitchEntryAnimation(index = 1) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatusVitalsBar(
                            label = "HP",
                            current = profile.hp,
                            max = profile.maxHp,
                            labelColor = Color(0xFFF87171),
                            barFillColor = Color(0xFFFCA5A5)
                        )
                        StatusVitalsBar(
                            label = "MP",
                            current = profile.mp,
                            max = profile.maxMp,
                            labelColor = Color(0xFF38BDF8),
                            barFillColor = Color(0xFF38BDF8)
                        )
                        StatusVitalsBar(
                            label = "XP",
                            current = profile.xp,
                            max = profile.xpToNextLevel,
                            labelColor = Color(0xFF94A3B8),
                            barFillColor = Color(0xFFE0F2FE)
                        )
                    }
                }
            }
        }

        // Attributes Panel (Matching screenshot)
        AnimatedScreenEntrance(delayMillis = 240) {
            GlitchEntryAnimation(index = 2) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ATTRIBUTES",
                            style = SystemLabel.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        StatRadarChart(
                            stats = state.stats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    }
                }
            }
        }

        // Streak Panel
        AnimatedScreenEntrance(delayMillis = 320) {
            GlitchEntryAnimation(index = 3) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${profile.streakDays}",
                            style = DisplayRank.copy(fontSize = 40.sp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "DAYS",
                            style = DisplayRank.copy(fontSize = 18.sp, letterSpacing = 4.sp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "DAILY QUEST ACTIVE",
                            style = SystemLabel.copy(letterSpacing = 2.sp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // View Quest Log Button
        AnimatedScreenEntrance(delayMillis = 400) {
            SystemButton(
                text = "VIEW QUEST LOG",
                onClick = onNavigateToQuests,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // System Log
        AnimatedScreenEntrance(delayMillis = 480) {
            GlitchEntryAnimation(index = 4) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SYSTEM LOG",
                            style = SystemLabel.copy(letterSpacing = 2.sp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        if (state.logs.isEmpty()) {
                            Text(
                                text = "No system events recorded.",
                                style = SystemLabel,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        } else {
                            state.logs.take(10).forEach { log ->
                                SystemLogEntryItem(
                                    icon = log.icon,
                                    message = log.message,
                                    timestamp = log.timestamp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(72.dp)) // Bottom nav clearance
    }
}


@Composable
private fun StatusVitalsBar(
    label: String,
    current: Int,
    max: Int,
    labelColor: Color,
    barFillColor: Color
) {
    val progress = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "vitalsProgress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = SystemLabel.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )
            )
            Text(
                text = "$current / $max",
                style = SystemLabel.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(barFillColor)
            )
        }
    }
}

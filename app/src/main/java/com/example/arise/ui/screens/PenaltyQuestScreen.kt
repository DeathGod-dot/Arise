package com.example.arise.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.ui.components.*
import com.example.arise.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PenaltyQuestScreen(
    onCompletePenalty: () -> Unit = {}
) {
    // Countdown timer state (starts at 4 hours)
    var remainingSeconds by remember { mutableLongStateOf(14400L) }

    // Suppress back button during penalty; allow return once timer reaches 0
    BackHandler(enabled = true) {
        if (remainingSeconds <= 0L) {
            onCompletePenalty()
        }
    }
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val hours = remainingSeconds / 3600
    val mins = (remainingSeconds % 3600) / 60
    val secs = remainingSeconds % 60

    // Pulse animation for danger elements
    val infiniteTransition = rememberInfiniteTransition(label = "dangerPulse")
    val dangerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "dangerAlpha"
    )

    // HP draining simulation
    val hpPercent by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Restart),
        label = "hpDrain"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        // Hex mesh background with red tint
        HexMeshBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // System Error Header
                GlitchEntryAnimation(index = 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = AriseDangerRed, modifier = Modifier.size(16.dp))
                        GlitchText(
                            text = "[ SYSTEM ERROR: MISSION FAILED ]",
                            style = SystemLabel.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            baseColor = AriseDangerRed
                        )
                        Icon(Icons.Default.Warning, null, tint = AriseDangerRed, modifier = Modifier.size(16.dp))
                    }
                }

                // PENALTY QUEST title
                GlitchEntryAnimation(index = 1) {
                    GlitchText(
                        text = "PENALTY QUEST",
                        style = DisplayRank.copy(fontSize = 36.sp, letterSpacing = 4.sp),
                        baseColor = AriseDangerRed
                    )
                }

                // Survival Timer (hero card)
                GlitchEntryAnimation(index = 2) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth().alpha(dangerAlpha),
                        variant = CardVariant.DANGER
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("SURVIVAL_TIME_REMAINING", style = SystemLabel, color = AriseDangerRed)
                            Text(
                                text = String.format("%02d:%02d:%02d", hours, mins, secs),
                                style = DisplayRank.copy(fontSize = 48.sp),
                                color = AriseDangerRed
                            )
                        }
                    }
                }

                // Survival Protocol
                GlitchEntryAnimation(index = 3) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.DANGER
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SURVIVAL PROTOCOL", style = SystemLabel.copy(fontWeight = FontWeight.Bold), color = AriseDangerRed)
                                Text(
                                    "ACTIVE_OVERRIDE",
                                    style = SystemLabel.copy(fontSize = 9.sp),
                                    color = AriseDangerRed,
                                    modifier = Modifier.alpha(dangerAlpha)
                                )
                            }

                            // 8km Run
                            ScanBeamProgressBar(current = 0, max = 8000, label = "ENDURANCE RUN", barColor = AriseDangerRed)
                            Text("0 / 8,000 METERS", style = SystemLabel, color = AriseOnSurfaceVariant)

                            // Strength Recovery
                            Text("STRENGTH RECOVERY", style = SystemLabel.copy(fontWeight = FontWeight.Bold), color = AriseOnSurface)
                            Text("50 Push-ups — Target: 4 Sets", style = SystemLabel, color = AriseOnSurfaceVariant)
                            Text("30 Pull-ups — Target: 3 Sets", style = SystemLabel, color = AriseOnSurfaceVariant)

                            // Real-World Task
                            Text("REAL-WORLD TASK", style = SystemLabel.copy(fontWeight = FontWeight.Bold), color = AriseOnSurface)
                            Text("System Cleanup — Clean Room/Workspace", style = SystemLabel, color = AriseOnSurfaceVariant)

                            // Mental Focus
                            Text("MENTAL FOCUS", style = SystemLabel.copy(fontWeight = FontWeight.Bold), color = AriseOnSurface)
                            Text("Deep Study Session — Timer: 60:00", style = SystemLabel, color = AriseOnSurfaceVariant)

                            // Social Media Lockout
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth().alpha(dangerAlpha),
                                variant = CardVariant.DANGER
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhonelinkLock, null, tint = AriseDangerRed, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("SOCIAL MEDIA LOCKOUT", style = SystemLabel.copy(fontWeight = FontWeight.Bold), color = AriseDangerRed)
                                        Text("Active for the next 24 hours", style = SystemLabel.copy(fontSize = 10.sp), color = AriseOnSurfaceVariant)
                                    }
                                }
                            }

                            Text(
                                text = "Protocol: Maintain steady pace to avoid detection.",
                                style = SystemLabel.copy(fontSize = 10.sp),
                                color = AriseOnSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Penalty Status Bars
                GlitchEntryAnimation(index = 4) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.DANGER
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("VITALITY (HP)", style = SystemLabel, color = AriseDangerRed)
                                Text("DRAINING", style = SystemLabel.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = AriseDangerRed, modifier = Modifier.alpha(dangerAlpha))
                            }
                            ScanBeamProgressBar(
                                current = (hpPercent * 150).toInt(),
                                max = 150,
                                label = "",
                                barColor = AriseDangerRed
                            )

                            Text("HEAT RESISTANCE", style = SystemLabel, color = AriseOnSurfaceVariant)
                            ScanBeamProgressBar(
                                current = 12,
                                max = 100,
                                label = "",
                                barColor = Color(0xFFFF8800)
                            )
                            Text("12%", style = SystemLabel, color = Color(0xFFFF8800))
                        }
                    }
                }

                // Vitals Monitor
                GlitchEntryAnimation(index = 5) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.DANGER
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("VITALS_MONITOR", style = SystemLabel, color = AriseOnSurfaceVariant)
                            Text("HEART RATE: 145 BPM", style = SystemLabelMedium.copy(fontWeight = FontWeight.Bold), color = AriseDangerRed)
                            ECGWaveform(
                                lineColor = AriseDangerRed,
                                cycleDurationMs = 1200
                            )
                        }
                    }
                }

                // EVACUATE button (always disabled)
                SystemButton(
                    text = "EVACUATE",
                    onClick = { },
                    variant = ButtonVariant.DANGER,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(Icons.Default.Block, null, tint = AriseOnSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                )
                
                // Show SURVIVED message when timer completes
                if (remainingSeconds <= 0L) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.DEFAULT
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "[ PENALTY QUEST: SURVIVED ]",
                                style = DisplayRank.copy(fontSize = 18.sp),
                                color = AriseTertiary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "The System acknowledges your endurance. You may return.",
                                style = SystemLabel.copy(fontSize = 11.sp),
                                color = AriseOnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            SystemButton(
                                text = "RETURN TO SYSTEM",
                                onClick = onCompletePenalty,
                                variant = ButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // System Log Console
                GlitchEntryAnimation(index = 6) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.DANGER
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("SYSTEM_CONSOLE", style = SystemLabel, color = AriseOnSurfaceVariant)
                            // Terminal-style log lines
                            // NOTE: The "permanent account deletion" text is FLAVOR COPY ONLY.
                            // It MUST NEVER be wired to any real destructive action.
                            listOf(
                                "> WARNING: Forced teleportation complete.",
                                "> Location: Penalty Zone — Desert Survival",
                                "> Survival is not guaranteed.",
                                "> Environmental hazards detected.",
                            ).forEach { line ->
                                Text(
                                    text = line,
                                    style = SystemLabel.copy(fontSize = 10.sp),
                                    color = AriseDangerRed.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "> Failure to survive will result in permanent account deletion.",
                                style = SystemLabel.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = AriseDangerRed,
                                modifier = Modifier.alpha(dangerAlpha)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

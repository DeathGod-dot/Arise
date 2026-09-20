package com.example.arise.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arise.ui.components.*
import com.example.arise.ui.components.AnimatedScreenEntrance
import com.example.arise.ui.theme.*
import com.example.arise.viewmodel.GateViewModel

@Composable
fun GateScreen(
    viewModel: GateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onSurf = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val onSurfVar = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ARISE wordmark — top-left
        AnimatedScreenEntrance(delayMillis = 0) {
            GlitchText(
                text = "ARISE",
                style = DisplayRank.copy(fontSize = 22.sp, letterSpacing = 4.sp),
                baseColor = primaryColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Section header
        AnimatedScreenEntrance(delayMillis = 40) {
            Text(
                text = "[ SYSTEM // GROWTH_ANALYTICS ]",
                style = SystemLabel.copy(letterSpacing = 1.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = primaryColor.copy(alpha = 0.8f)
            )
        }

        // Rank Progression Card
        AnimatedScreenEntrance(delayMillis = 80) {
            GlitchEntryAnimation(index = 0) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Current Rank label
                        Text(
                            text = "CURRENT RANK",
                            style = SystemLabel.copy(letterSpacing = 2.sp, fontSize = 10.sp),
                            color = onSurfVar
                        )

                        // Badge + Rank names row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Left: badge + rank name
                            RankBadge(rank = state.currentRank, size = 48)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${state.currentRank.displayName}\nHunter",
                                    style = AriseTypography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 26.sp
                                    ),
                                    color = onSurf
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            // Arrow
                            Text("→", color = onSurfVar, fontSize = 24.sp)

                            Spacer(Modifier.width(12.dp))

                            // Right: next rank
                            Column(horizontalAlignment = Alignment.End) {
                                if (state.nextRank != null) {
                                    Text(
                                        text = "${state.nextRank!!.displayName.split("-")[0]}-",
                                        style = AriseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = onSurfVar
                                    )
                                    Text(
                                        text = "Rank",
                                        style = AriseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = onSurfVar
                                    )
                                } else {
                                    Text(
                                        "MAX",
                                        style = DisplayRank.copy(fontSize = 24.sp),
                                        color = AriseRankS
                                    )
                                }
                            }
                        }

                        // XP Progress label + values
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "XP PROGRESS TO RANK\nUP",
                                style = SystemLabel.copy(letterSpacing = 1.sp, lineHeight = 14.sp),
                                color = onSurfVar
                            )
                            Text(
                                text = "${String.format("%,d", state.xpInRank)} / ${String.format("%,d", state.xpToNextRank)}",
                                style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                                color = onSurf
                            )
                        }

                        // Progress bar
                        ScanBeamProgressBar(
                            current = state.xpInRank.toInt(),
                            max = state.xpToNextRank.toInt(),
                            label = "",
                            barColor = primaryColor
                        )

                        // Percent cleared — right aligned
                        Text(
                            text = String.format("%.1f%% CLEARED", state.rankProgressPercent),
                            style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                            color = onSurfVar,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // XP Acquisition Rate
        AnimatedScreenEntrance(delayMillis = 160) {
            GlitchEntryAnimation(index = 1) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title row with trend badge & week navigation chevrons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "XP Acquisition Rate",
                                    style = AriseTypography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = onSurf,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.shiftWeek(-1) },
                                        enabled = state.weekOffset > -4,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronLeft,
                                            contentDescription = "Previous Week",
                                            tint = if (state.weekOffset > -4) primaryColor else onSurfVar.copy(alpha = 0.3f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = if (state.weekRangeLabel.isNotEmpty()) state.weekRangeLabel else "CURRENT WEEK",
                                        style = SystemLabel.copy(letterSpacing = 1.sp, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )
                                    IconButton(
                                        onClick = { viewModel.shiftWeek(1) },
                                        enabled = state.weekOffset < 0,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next Week",
                                            tint = if (state.weekOffset < 0) primaryColor else onSurfVar.copy(alpha = 0.3f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Trend badge pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val trendColor = if (state.weeklyTrendPercent >= 0) AriseTertiary else AriseDangerRed
                                val trendSign = if (state.weeklyTrendPercent >= 0) "+" else ""
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = trendColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${trendSign}${String.format("%.1f", state.weeklyTrendPercent)}%",
                                    style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                                    color = trendColor
                                )
                            }
                        }

                        Text(
                            text = "WEEKLY YIELD TREND",
                            style = SystemLabel.copy(letterSpacing = 2.sp),
                            color = onSurfVar
                        )

                        var totalDragAmount by remember { mutableFloatStateOf(0f) }

                        // Swipeable Weekly Chart with smooth slide transition
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(state.weekOffset) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDragAmount = 0f },
                                        onDragEnd = {
                                            if (totalDragAmount < -40f && state.weekOffset < 0) {
                                                // Swiped left -> navigate forward to newer week
                                                viewModel.shiftWeek(1)
                                            } else if (totalDragAmount > 40f && state.weekOffset > -4) {
                                                // Swiped right -> navigate back to older week
                                                viewModel.shiftWeek(-1)
                                            }
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDragAmount += dragAmount
                                        }
                                    )
                                }
                        ) {
                            AnimatedContent(
                                targetState = state.weekOffset to state.weeklyXpData,
                                transitionSpec = {
                                    if (targetState.first < initialState.first) {
                                        // Swiped right (going older) -> slide in from left, out to right
                                        (slideInHorizontally { -it } + fadeIn(tween(300))).togetherWith(
                                            slideOutHorizontally { it } + fadeOut(tween(300))
                                        )
                                    } else {
                                        // Swiped left (going newer) -> slide in from right, out to left
                                        (slideInHorizontally { it } + fadeIn(tween(300))).togetherWith(
                                            slideOutHorizontally { -it } + fadeOut(tween(300))
                                        )
                                    }
                                },
                                label = "chartWeekSlide"
                            ) { (_, xpData) ->
                                WeeklyChart(
                                    dataPoints = xpData,
                                    labels = state.weeklyLabels,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Total EXP Gained
        AnimatedScreenEntrance(delayMillis = 240) {
            GlitchEntryAnimation(index = 2) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TOTAL EXP GAINED",
                                style = SystemLabel.copy(letterSpacing = 2.sp),
                                color = onSurfVar
                            )
                            Text(
                                text = String.format("%,d", state.totalXp),
                                style = AriseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = primaryColor
                            )
                            Text(
                                text = "+${String.format("%,d", state.weekXpDelta)} THIS WEEK",
                                style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                                color = AriseTertiary
                            )
                        }
                    }
                }
            }
        }
        
        AnimatedScreenEntrance(delayMillis = 320) {
            GlitchEntryAnimation(index = 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ACTIVE DAYS", style = SystemLabel.copy(letterSpacing = 2.sp), color = onSurfVar)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📅", fontSize = 24.sp)
                                Text("${state.activeDays}", style = AriseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                            }
                        }
                    }
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("CURRENT STREAK", style = SystemLabel.copy(letterSpacing = 2.sp), color = onSurfVar)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🔥", fontSize = 24.sp)
                                Text("${state.currentStreak}", style = AriseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

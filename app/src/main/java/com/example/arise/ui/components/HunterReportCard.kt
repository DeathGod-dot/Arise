package com.example.arise.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.data.HunterProfile
import com.example.arise.domain.HunterRank
import com.example.arise.ui.theme.*

data class ReportCardData(
    val profile: HunterProfile = HunterProfile(),
    val totalClearedQuests: Int = 0,
    val pendingQuestsToday: Int = 0,
    val strengthStatName: String = "STR",
    val strengthStatVal: Int = 10,
    val strengthDescription: String = "High physical muscular force",
    val weaknessStatName: String = "INT",
    val weaknessStatVal: Int = 8,
    val weaknessDescription: String = "Requires cognitive training",
    val recommendation: String = "Complete 2-Hour Study Session to raise INT stat",
    val isSundaySpecial: Boolean = false
)

@Composable
fun HunterReportCard(
    data: ReportCardData,
    modifier: Modifier = Modifier
) {
    val profile = data.profile
    val isSpecial = data.isSundaySpecial
    var isExpanded by remember { mutableStateOf(true) }
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val cs = androidx.compose.material3.MaterialTheme.colorScheme

    // Animated elements
    val infiniteTransition = rememberInfiniteTransition(label = "reportCardAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val scanLinePos by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "scanLine"
    )

    val primaryColor = cs.primary
    val onSurface = cs.onSurface
    val onSurfaceVar = cs.onSurfaceVariant
    val rankColor = profile.rank.color
    val accentColor = if (isSpecial) Color(0xFFFFD700) else primaryColor
    val cardBg = cs.surfaceContainerLowest.copy(alpha = if (isLight) 0.95f else 0.75f)

    // Border gradient
    val borderBrush = Brush.linearGradient(
        colors = if (isSpecial) {
            listOf(Color(0xFFFFD700).copy(alpha = glowAlpha), Color(0xFF38BDF8), Color(0xFFFFD700).copy(alpha = glowAlpha))
        } else {
            listOf(rankColor.copy(alpha = 0.7f * glowAlpha), primaryColor.copy(alpha = 0.5f), rankColor.copy(alpha = 0.4f * glowAlpha))
        }
    )

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .border(width = if (isSpecial) 2.dp else 1.dp, brush = borderBrush, shape = SciFiCutCornerShape())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ═══════════════════════════════════════════
            // SECTION 1: CLASSIFIED HEADER BAR
            // ═══════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isLight) {
                                listOf(cs.primaryContainer.copy(alpha = 0.3f), Color.Transparent, cs.primaryContainer.copy(alpha = 0.15f))
                            } else {
                                listOf(rankColor.copy(alpha = 0.12f), Color.Transparent, primaryColor.copy(alpha = 0.06f))
                            }
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isSpecial) {
                            Icon(Icons.Default.WorkspacePremium, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = if (isSpecial) "★ CLASSIFIED: SUNDAY EVALUATION ★" else "CLASSIFIED: HUNTER DOSSIER",
                            style = SystemLabel.copy(
                                fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                            ),
                            color = if (isSpecial) (if (isLight) Color(0xFFB45309) else Color(0xFFFFD700)) else accentColor
                        )
                    }
                    Text(
                        text = "LVL ${profile.level}",
                        style = SystemLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = if (isLight) AriseLightTertiary else AriseTertiary
                    )
                }
            }

            // Thin divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.5f), Color.Transparent)
                    ))
            )

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ═══════════════════════════════════════════
                // SECTION 2: IDENTITY PANEL (Badge + Info + Stats)
                // ═══════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBg)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rank Badge with animated glow ring
                    Box(contentAlignment = Alignment.Center) {
                        // Outer animated ring
                        Canvas(modifier = Modifier.size(56.dp)) {
                            drawCircle(
                                color = rankColor.copy(alpha = 0.15f * glowAlpha),
                                radius = size.minDimension / 2f
                            )
                            drawCircle(
                                color = rankColor.copy(alpha = 0.6f * glowAlpha),
                                radius = size.minDimension / 2f - 2.dp.toPx(),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                        RankBadge(rank = profile.rank, size = 48)
                    }

                    // Identity details
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = profile.username.uppercase(),
                            style = AriseTypography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp, fontSize = 16.sp
                            ),
                            color = onSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${profile.rank.displayName} Hunter",
                            style = SystemLabel.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = rankColor
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "AGE: ${profile.age}",
                                style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                                color = onSurfaceVar
                            )
                            Text(
                                text = "WT: ${String.format("%.1f", profile.weightKg)}KG",
                                style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                                color = onSurfaceVar
                            )
                        }
                    }

                    // Combat Power Score (visual indicator)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val combatPower = profile.str + profile.agi + profile.vit + profile.int_stat + profile.sen
                        Text(
                            text = "$combatPower",
                            style = DisplayRank.copy(fontSize = 22.sp),
                            color = accentColor
                        )
                        Text(
                            text = "POWER",
                            style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            color = onSurfaceVar
                        )
                    }
                }

                // ═══════════════════════════════════════════
                // SECTION 3: ATTRIBUTE BREAKDOWN (5 bars)
                // ═══════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBg)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ATTRIBUTE ANALYSIS",
                            style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                        Text(
                            text = "LIVE_SCAN",
                            style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                            color = onSurfaceVar.copy(alpha = 0.6f),
                            modifier = Modifier.alpha(glowAlpha)
                        )
                    }

                    val stats = listOf(
                        Triple("STR", profile.str, Color(0xFFEF4444)),
                        Triple("AGI", profile.agi, Color(0xFF3B82F6)),
                        Triple("VIT", profile.vit, Color(0xFF10B981)),
                        Triple("INT", profile.int_stat, Color(0xFFA855F7)),
                        Triple("SEN", profile.sen, Color(0xFFF59E0B))
                    )
                    val maxStat = (stats.maxOfOrNull { it.second } ?: 10).coerceAtLeast(10)

                    stats.forEach { (name, value, color) ->
                        val isHighest = name == data.strengthStatName
                        val isLowest = name == data.weaknessStatName
                        val barColor = when {
                            isLight && isHighest -> color
                            isLight -> color.copy(alpha = 0.7f)
                            isHighest -> color
                            else -> color.copy(alpha = 0.8f)
                        }
                        val barBgColor = if (isLight) cs.outlineVariant.copy(alpha = 0.4f) else Color(0xFF1A1A2E)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stat label
                            Text(
                                text = name,
                                style = SystemLabel.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isHighest || isLowest) FontWeight.ExtraBold else FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = if (isHighest) barColor else if (isLowest) AriseDangerRed else onSurfaceVar,
                                modifier = Modifier.width(30.dp)
                            )

                            // Animated stat bar
                            val progress by animateFloatAsState(
                                targetValue = (value.toFloat() / (maxStat * 1.3f)).coerceIn(0f, 1f),
                                animationSpec = tween(1000, easing = FastOutSlowInEasing),
                                label = "statBar_$name"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(barBgColor)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(barColor.copy(alpha = 0.6f), barColor)
                                            )
                                        )
                                )
                                // Scan beam effect on the bar
                                if (!isLight) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                    ) {
                                        Canvas(modifier = Modifier.matchParentSize()) {
                                            val beamX = size.width * scanLinePos
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.3f),
                                                start = Offset(beamX, 0f),
                                                end = Offset(beamX, size.height),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    }
                                }
                            }

                            // Stat value
                            Text(
                                text = "$value",
                                style = SystemLabel.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = if (isHighest) barColor else onSurface,
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.End
                            )

                            // Highest/Lowest indicator
                            Box(modifier = Modifier.width(14.dp)) {
                                if (isHighest) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = barColor, modifier = Modifier.size(12.dp))
                                } else if (isLowest) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = AriseDangerRed, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════════
                // SECTION 4: MISSION STATUS + RANK PROGRESS
                // ═══════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mission Stats
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardBg)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MISSION LOG",
                            style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            color = onSurfaceVar
                        )

                        // Total Cleared
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(AriseTertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = AriseTertiary, modifier = Modifier.size(12.dp))
                            }
                            Column {
                                Text("${data.totalClearedQuests}", style = SystemLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold), color = AriseTertiary)
                                Text("CLEARED", style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = onSurfaceVar)
                            }
                        }

                        // Pending Today
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isAllDone = data.pendingQuestsToday == 0
                            val pendingColor = if (isAllDone) AriseTertiary else AriseSecondaryDim
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(pendingColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isAllDone) Icons.Default.DoneAll else Icons.Default.HourglassTop,
                                    null, tint = pendingColor, modifier = Modifier.size(12.dp)
                                )
                            }
                            Column {
                                Text(
                                    if (isAllDone) "DONE" else "${data.pendingQuestsToday}",
                                    style = SystemLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
                                    color = pendingColor
                                )
                                Text(
                                    if (isAllDone) "ALL CLEAR" else "PENDING",
                                    style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp),
                                    color = onSurfaceVar
                                )
                            }
                        }

                        // Streak
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔥", fontSize = 10.sp)
                            }
                            Column {
                                Text("${profile.streakDays}", style = SystemLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold), color = Color(0xFFF59E0B))
                                Text("STREAK", style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = onSurfaceVar)
                            }
                        }
                    }

                    // Rank Progress
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardBg)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RANK PROGRESS",
                            style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            color = onSurfaceVar
                        )

                        val nextRank = profile.rank.nextRank
                        val rankProgress = if (nextRank != null) {
                            val xpInCurrentRank = profile.totalXp - profile.rank.xpThreshold
                            val xpNeeded = nextRank.xpThreshold - profile.rank.xpThreshold
                            if (xpNeeded > 0) (xpInCurrentRank.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f) else 1f
                        } else 1f

                        // Circular progress
                        val animatedRankProgress by animateFloatAsState(
                            targetValue = rankProgress,
                            animationSpec = tween(1200, easing = FastOutSlowInEasing),
                            label = "rankProgress"
                        )

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                            Canvas(modifier = Modifier.size(72.dp)) {
                                val strokeWidth = 5.dp.toPx()
                                val sweepAngle = 360f * animatedRankProgress
                                val bgColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1A1A2E)

                                // Background ring
                                drawArc(
                                    color = bgColor,
                                    startAngle = -90f, sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                                )

                                // Progress arc
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(rankColor.copy(alpha = 0.4f), rankColor, rankColor)
                                    ),
                                    startAngle = -90f, sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = profile.rank.name,
                                    style = DisplayRank.copy(fontSize = 18.sp),
                                    color = rankColor
                                )
                                Text(
                                    text = "${(rankProgress * 100).toInt()}%",
                                    style = SystemLabel.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = onSurfaceVar
                                )
                            }
                        }

                        // Next rank label
                        if (nextRank != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = nextRank.color.copy(alpha = 0.7f), modifier = Modifier.size(10.dp))
                                Text(
                                    text = nextRank.displayName,
                                    style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = nextRank.color.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Text(
                                text = "MAX RANK",
                                style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════
                // SECTION 5: SYSTEM ANALYSIS (expandable)
                // ═══════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBg)
                        .border(
                            1.dp,
                            if (isLight) cs.outlineVariant.copy(alpha = 0.5f) else AriseOutlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isExpanded = !isExpanded }
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Analytics, null, tint = accentColor, modifier = Modifier.size(14.dp))
                            Text(
                                text = "TACTICAL ANALYSIS",
                                style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isExpanded) "COLLAPSE" else "EXPAND",
                                style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                                color = primaryColor.copy(alpha = 0.7f)
                            )
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                null, tint = primaryColor.copy(alpha = 0.7f), modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Always visible: Strength & Weakness summary line
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier.size(6.dp).clip(CircleShape)
                                    .background(if (isLight) AriseLightTertiary else AriseTertiary)
                            )
                            Text(
                                "PEAK: ${data.strengthStatName} (${data.strengthStatVal})",
                                style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = if (isLight) AriseLightTertiary else AriseTertiary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier.size(6.dp).clip(CircleShape)
                                    .background(AriseDangerRed.copy(alpha = 0.8f))
                            )
                            Text(
                                "WEAK: ${data.weaknessStatName} (${data.weaknessStatVal})",
                                style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = AriseDangerRed.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Expanded detailed analysis
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Strength Analysis
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isLight) AriseLightTertiary.copy(alpha = 0.06f) else AriseTertiary.copy(alpha = 0.06f)
                                    )
                                    .border(
                                        0.5.dp,
                                        if (isLight) AriseLightTertiary.copy(alpha = 0.2f) else AriseTertiary.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = if (isLight) AriseLightTertiary else AriseTertiary, modifier = Modifier.size(12.dp))
                                    Text(
                                        "DOMINANT ATTRIBUTE",
                                        style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                                        color = if (isLight) AriseLightTertiary else AriseTertiary
                                    )
                                }
                                Text(
                                    data.strengthDescription,
                                    style = SystemLabel.copy(fontSize = 9.sp),
                                    color = onSurfaceVar
                                )
                            }

                            // Weakness Analysis
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AriseDangerRed.copy(alpha = 0.04f))
                                    .border(
                                        0.5.dp,
                                        AriseDangerRed.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = AriseDangerRed.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                                    Text(
                                        "VULNERABILITY DETECTED",
                                        style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                                        color = AriseDangerRed.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    data.weaknessDescription,
                                    style = SystemLabel.copy(fontSize = 9.sp),
                                    color = onSurfaceVar
                                )
                            }

                            // Recommendation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.06f))
                                    .border(0.5.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.GpsFixed, null, tint = accentColor, modifier = Modifier.size(12.dp).padding(top = 1.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "SYSTEM RECOMMENDATION",
                                        style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                                        color = accentColor
                                    )
                                    Text(
                                        data.recommendation,
                                        style = SystemLabel.copy(fontSize = 9.sp),
                                        color = onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════════
                // SECTION 6: SUNDAY SPECIAL COMMENDATION
                // ═══════════════════════════════════════════
                if (isSpecial) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isLight) {
                                        listOf(Color(0xFFFEF3C7), Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                                    } else {
                                        listOf(Color(0xFF3B2D05), Color(0xFF1E1B10), Color(0xFF3B2D05))
                                    }
                                )
                            )
                            .border(
                                1.dp,
                                if (isLight) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFFFFD700).copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "★ PERFECT WEEKLY SYNC COMMENDATION ★",
                                style = SystemLabel.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp),
                                color = if (isLight) Color(0xFFB45309) else Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "\"The System acknowledges your absolute discipline. Rest well today, Hunter.\"",
                                style = SystemLabel.copy(fontSize = 9.sp),
                                color = onSurfaceVar,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom scan line bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val beamWidth = size.width * 0.3f
                    val beamStart = size.width * scanLinePos - beamWidth / 2
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.6f), Color.Transparent),
                            startX = beamStart,
                            endX = beamStart + beamWidth
                        )
                    )
                }
            }
        }
    }
}

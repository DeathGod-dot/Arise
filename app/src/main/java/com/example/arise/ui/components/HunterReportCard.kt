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
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "ripple"
    )

    val primaryColor = cs.primary
    val onSurface = cs.onSurface
    val onSurfaceVar = cs.onSurfaceVariant
    val rankColor = profile.rank.color
    val accentColor = if (isSpecial) Color(0xFFFFD700) else primaryColor
    val cardBg = cs.surfaceContainerLowest.copy(alpha = if (isLight) 0.95f else 0.75f)

    // Border gradient colors (static — animation applied via graphicsLayer)
    val borderColorsSpecial = listOf(Color(0xFFFFD700), Color(0xFF38BDF8), Color(0xFFFFD700))
    val borderColorsNormal = listOf(rankColor.copy(alpha = 0.7f), primaryColor.copy(alpha = 0.5f), rankColor.copy(alpha = 0.4f))
    val borderWidth = if (isSpecial) 2.dp else 1.dp

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { } // Isolate recomposition
            .drawBehind {
                // Draw animated border in draw phase — reads glowAlpha here, not in composition
                val colors = if (isSpecial) {
                    borderColorsSpecial.map { it.copy(alpha = glowAlpha) }
                } else {
                    listOf(
                        borderColorsNormal[0].copy(alpha = 0.7f * glowAlpha),
                        borderColorsNormal[1],
                        borderColorsNormal[2].copy(alpha = 0.4f * glowAlpha)
                    )
                }
                val brush = Brush.linearGradient(colors)
                val strokePx = borderWidth.toPx()
                drawRoundRect(
                    brush = brush,
                    style = Stroke(width = strokePx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )
            }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Animated Grid/Circuit Pattern
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val gridSize = 12.dp.toPx()
                        val gridAlpha = 0.05f * glowAlpha
                        val gridColor = if (isLight) primaryColor.copy(alpha = gridAlpha) else AriseTertiary.copy(alpha = gridAlpha)
                        
                        var x = 0f
                        while (x < size.width) {
                            drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.dp.toPx())
                            x += gridSize
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
                            y += gridSize
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            val userIdHash = profile.username.hashCode().toString(16).uppercase().take(6).padStart(6, '0')
                            Text(
                                text = "ID: HNT-$userIdHash",
                                style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium),
                                color = primaryColor.copy(alpha = 0.8f)
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
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(64.dp)) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(accentColor.copy(alpha = 0.25f * glowAlpha), Color.Transparent)
                                    ),
                                    radius = size.minDimension / 2f
                                )
                            }
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
                    }
                }

                // ── Section Divider ──
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(alpha = 0.25f), Color.Transparent))
                ))

                // ═══════════════════════════════════════════
                // SECTION 3: ATTRIBUTE BREAKDOWN (5 bars)
                // ═══════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
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
                            modifier = Modifier.graphicsLayer { alpha = glowAlpha }
                        )
                    }

                    val stats = listOf(
                        Triple("STR", profile.str, Color(0xFFEF4444)),
                        Triple("AGI", profile.agi, Color(0xFF3B82F6)),
                        Triple("VIT", profile.vit, Color(0xFF10B981)),
                        Triple("INT", profile.int_stat, Color(0xFFA855F7)),
                        Triple("SEN", profile.sen, Color(0xFFF59E0B))
                    )
                    val maxStat = (stats.maxOfOrNull { it.second } ?: 10).coerceAtLeast((profile.rank.statCap / 2).coerceAtLeast(10))
                    val avgStat = stats.map { it.second }.average()

                    stats.forEach { (name, value, color) ->
                        val isHighest = name == data.strengthStatName
                        val isLowest = name == data.weaknessStatName
                        val barColor = when {
                            isLight && isHighest -> color
                            isLight -> color.copy(alpha = 0.7f)
                            isHighest -> color
                            else -> color.copy(alpha = 0.8f)
                        }
                        val barBgColor = if (isLight) cs.outlineVariant.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.07f)

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
                                // Notch marks
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val notchCount = 10
                                    val notchSpacing = size.width / notchCount
                                    for (i in 1 until notchCount) {
                                        val nx = i * notchSpacing
                                        drawLine(
                                            color = onSurfaceVar.copy(alpha = 0.2f),
                                            start = Offset(nx, 0f),
                                            end = Offset(nx, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                }

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
                                
                                if (isHighest) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                            .clip(RoundedCornerShape(4.dp))
                                            .graphicsLayer { alpha = glowAlpha }
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent)
                                                )
                                            )
                                    )
                                }

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
                            Row(
                                modifier = Modifier.width(36.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$value",
                                    style = SystemLabel.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = if (isHighest) barColor else onSurface
                                )
                                if (value > avgStat) {
                                    Text(
                                        text = "▲",
                                        style = SystemLabel.copy(fontSize = 8.sp),
                                        color = if (isHighest) barColor else AriseTertiary,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }

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

                // ── Section Divider ──
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(alpha = 0.25f), Color.Transparent))
                ))

                // ═══════════════════════════════════════════
                // SECTION 4: MISSION STATUS + RANK PROGRESS
                // ═══════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Combat Profile
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "COMBAT PROFILE",
                            style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            color = onSurfaceVar
                        )

                        // Compute stat balance: how evenly distributed the 5 stats are (0-100%)
                        val statValues = listOf(profile.str, profile.agi, profile.vit, profile.int_stat, profile.sen)
                        val minStatVal = statValues.min()
                        val maxStatVal = statValues.max().coerceAtLeast(1)
                        val balance = ((minStatVal.toFloat() / maxStatVal.toFloat()) * 100).toInt()
                        val balanceColor = when {
                            balance >= 80 -> AriseTertiary
                            balance >= 50 -> Color(0xFFF59E0B)
                            else -> AriseDangerRed
                        }

                        // Format total XP
                        val formattedXp = when {
                            profile.totalXp >= 1_000_000 -> String.format("%.1fM", profile.totalXp / 1_000_000f)
                            profile.totalXp >= 1_000 -> String.format("%.1fK", profile.totalXp / 1_000f)
                            else -> "${profile.totalXp}"
                        }

                        // 2x2 Metric Grid — Row 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Total XP
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(formattedXp, style = SystemLabel.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold), color = accentColor)
                                Text("TOTAL XP", style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = onSurfaceVar)
                            }
                            // Quests Cleared
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text("${data.totalClearedQuests}", style = SystemLabel.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold), color = AriseTertiary)
                                Text("CLEARED", style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = onSurfaceVar)
                            }
                        }

                        // 2x2 Metric Grid — Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Level
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text("${profile.level}", style = SystemLabel.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold), color = if (isLight) AriseLightTertiary else AriseTertiary)
                                Text("LEVEL", style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = onSurfaceVar)
                            }
                            // Stat Balance
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text("${balance}%", style = SystemLabel.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold), color = balanceColor)
                                Text("BALANCE", style = SystemLabel.copy(fontSize = 7.sp, letterSpacing = 1.sp), color = onSurfaceVar)
                            }
                        }

                        // Streak row
                        val isHighStreak = profile.streakDays > 7
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isHighStreak) Brush.horizontalGradient(
                                        listOf(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFEF4444).copy(alpha = 0.08f))
                                    ) else Brush.horizontalGradient(
                                        listOf(Color(0xFFF59E0B).copy(alpha = 0.08f), Color.Transparent)
                                    )
                                )
                                .then(
                                    if (isHighStreak) Modifier.border(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("\uD83D\uDD25", fontSize = 12.sp)
                            Text(
                                "${profile.streakDays}",
                                style = SystemLabel.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                "DAY STREAK",
                                style = SystemLabel.copy(fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                                color = onSurfaceVar
                            )
                        }
                    }

                    // Rank Progress
                    // Thin vertical separator
                    Box(modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Brush.verticalGradient(
                            listOf(Color.Transparent, accentColor.copy(alpha = 0.2f), Color.Transparent)
                        ))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
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

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                // Animated ripple
                                drawCircle(
                                    color = rankColor.copy(alpha = (1f - rippleProgress) * 0.3f),
                                    radius = (size.minDimension / 2f) * (0.8f + 0.4f * rippleProgress)
                                )

                                val strokeWidth = 6.dp.toPx()
                                val sweepAngle = 360f * animatedRankProgress
                                val bgColor = if (isLight) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.08f)

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

                // ── Section Divider ──
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(alpha = 0.25f), Color.Transparent))
                ))

                // ═══════════════════════════════════════════
                // SECTION 5: SYSTEM ANALYSIS (expandable)
                // ═══════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            0.5.dp,
                            if (isLight) cs.outlineVariant.copy(alpha = 0.3f) else accentColor.copy(alpha = 0.12f),
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
                    .height(3.dp)
                    .drawBehind {
                        val beamWidth = size.width * 0.3f
                        val beamStart = size.width * scanLinePos - beamWidth / 2
                        
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = accentColor
                                asFrameworkPaint().maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
                            }
                            canvas.drawRect(
                                left = beamStart + beamWidth * 0.2f, 
                                top = 0f, 
                                right = beamStart + beamWidth * 0.8f, 
                                bottom = size.height, 
                                paint = paint
                            )
                        }
                    }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val beamWidth = size.width * 0.3f
                    val beamStart = size.width * scanLinePos - beamWidth / 2
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, accentColor, Color.Transparent),
                            startX = beamStart,
                            endX = beamStart + beamWidth
                        )
                    )
                }
            }
        }
    }
}

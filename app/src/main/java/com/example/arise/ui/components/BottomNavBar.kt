package com.example.arise.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arise.ui.theme.*

enum class NavTab(val label: String, val icon: ImageVector) {
    STATUS("Status", Icons.Default.Person),
    QUESTS("Quests", Icons.AutoMirrored.Filled.List),
    GATE("The Gate", Icons.AutoMirrored.Filled.TrendingUp),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val navBgColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF0E0E12)
    val navBorderPrimary = if (isLight) AriseLightPrimary else ArisePrimary
    val innerShadowColor = if (isLight) Color(0xFFEDE8E0).copy(alpha = 0.8f) else Color(0xFF0A0A0C).copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Main glass container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .drawBehind {
                    // Glass background
                    drawRoundRect(
                        color = navBgColor,
                        cornerRadius = CornerRadius(20.dp.toPx()),
                    )
                    // Subtle glass overlay
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = if (isLight) {
                                listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color(0xFFF7F4EF).copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.95f),
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.02f),
                                )
                            }
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                    )
                    // Top border accent line
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                navBorderPrimary.copy(alpha = 0.4f),
                                Color.Transparent,
                            )
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        size = Size(size.width, 1.5f.dp.toPx()),
                    )
                    // Bottom inner shadow
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                innerShadowColor,
                            ),
                            startY = size.height * 0.6f,
                            endY = size.height,
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                    )
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavTab.entries.forEach { tab ->
                NavTabItem(
                    tab = tab,
                    isSelected = tab == currentTab,
                    isLight = isLight,
                    onSelect = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: NavTab,
    isSelected: Boolean,
    isLight: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = if (isLight) AriseLightPrimary else ArisePrimary
    val unselectedIconColor = if (isLight) AriseLightOnSurfaceVariant.copy(alpha = 0.7f) else AriseOnSurfaceVariant.copy(alpha = 0.6f)
    val unselectedLabelColor = if (isLight) AriseLightOnSurfaceVariant.copy(alpha = 0.7f) else AriseOnSurfaceVariant.copy(alpha = 0.4f)

    // Snappy scale on selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "tab_scale"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 0.dp,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "indicator_width"
    )

    // Fast icon color transition
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else unselectedIconColor,
        animationSpec = tween(150),
        label = "icon_color"
    )

    // Fast label color transition
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else unselectedLabelColor,
        animationSpec = tween(150),
        label = "label_color"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Icon with glow background when selected
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .drawBehind {
                    if (isSelected) {
                        // Soft radial glow behind selected icon
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.25f),
                                    primaryColor.copy(alpha = 0.05f),
                                    Color.Transparent,
                                ),
                                radius = size.maxDimension * 1.2f,
                            )
                        )
                    }
                }
                .padding(4.dp),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
        }

        // Label text
        Text(
            text = tab.label.uppercase(),
            style = SystemLabel.copy(
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
            ),
            color = labelColor,
        )

        // Animated indicator dot/line
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor,
                                primaryColor.copy(alpha = 0.3f),
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
        )
    }
}

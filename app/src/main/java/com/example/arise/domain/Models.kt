package com.example.arise.domain

import androidx.compose.ui.graphics.Color
import com.example.arise.ui.theme.*

enum class HunterRank(
    val displayName: String,
    val xpThreshold: Long,
    val color: Color
) {
    E("E-Rank", 0L, AriseRankE),
    D("D-Rank", 10_000L, AriseRankD),
    C("C-Rank", 40_000L, AriseRankC),
    B("B-Rank", 120_000L, AriseRankB),
    A("A-Rank", 250_000L, AriseRankA),
    S("S-Rank", 500_000L, AriseRankS);

    val nextRank: HunterRank? get() = entries.getOrNull(ordinal + 1)
    val xpToNextRank: Long get() = nextRank?.xpThreshold?.minus(xpThreshold) ?: 0L

    companion object {
        fun fromTotalXp(xp: Long): HunterRank = entries.lastOrNull { xp >= it.xpThreshold } ?: E
    }
}

enum class Attribute {
    STR, AGI, SEN, VIT, INT
}

enum class QuestType(val label: String) {
    PHYSICAL("PHYSICAL"),
    LOGGING("LOGGING"),
    TIMER("TIMER"),
    FOCUS("FOCUS")
}

enum class QuestStatus(val label: String) {
    PENDING("PENDING"),
    ONGOING("ONGOING"),
    LOGGING("LOGGING"),
    CLEARED("CLEARED"),
    FAILED("FAILED")
}

enum class SubObjectiveType {
    REPS,
    NUMERIC_LOG,
    TIMER,
    FOCUS_TIMER
}

enum class LogIcon(val symbol: String) {
    STAT_UP("↑"),
    QUEST_COMPLETE("✓"),
    WARNING("⚠"),
    INFO("ℹ")
}

data class AttributeReward(
    val attribute: Attribute,
    val amount: Int
)

data class RewardResult(
    val xpGained: Int,
    val mpGained: Int,
    val attributeGains: List<AttributeReward>,
    val leveledUp: Boolean,
    val rankedUp: Boolean,
    val newRank: HunterRank?
)

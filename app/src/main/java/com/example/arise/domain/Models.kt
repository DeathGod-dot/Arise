package com.example.arise.domain

import androidx.compose.ui.graphics.Color
import com.example.arise.ui.theme.*

enum class HunterRank(
    val displayName: String,
    val xpThreshold: Long,
    val color: Color,
    val baseHpBonus: Int,
    val baseMpBonus: Int,
    val baseStatBonus: Int,
    val statCap: Int,
    val baseLevelXpCap: Int
) {
    E("E-Rank", 0L, AriseRankE, baseHpBonus = 0, baseMpBonus = 0, baseStatBonus = 0, statCap = 50, baseLevelXpCap = 100),
    D("D-Rank", 10_000L, AriseRankD, baseHpBonus = 150, baseMpBonus = 80, baseStatBonus = 5, statCap = 100, baseLevelXpCap = 300),
    C("C-Rank", 40_000L, AriseRankC, baseHpBonus = 400, baseMpBonus = 200, baseStatBonus = 15, statCap = 250, baseLevelXpCap = 600),
    B("B-Rank", 120_000L, AriseRankB, baseHpBonus = 800, baseMpBonus = 400, baseStatBonus = 30, statCap = 500, baseLevelXpCap = 1200),
    A("A-Rank", 250_000L, AriseRankA, baseHpBonus = 1500, baseMpBonus = 750, baseStatBonus = 50, statCap = 1000, baseLevelXpCap = 2500),
    S("S-Rank", 500_000L, AriseRankS, baseHpBonus = 3000, baseMpBonus = 1500, baseStatBonus = 100, statCap = 9999, baseLevelXpCap = 5000);

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

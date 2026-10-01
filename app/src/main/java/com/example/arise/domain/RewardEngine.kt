package com.example.arise.domain

import com.example.arise.data.HunterProfile

object RewardEngine {

    fun processQuestRewards(
        profile: HunterProfile,
        expReward: Int,
        mpReward: Int,
        attributeRewards: List<AttributeReward>
    ): Pair<HunterProfile, RewardResult> {
        var newXp = (profile.xp + expReward).coerceAtLeast(0)
        var newTotalXp = (profile.totalXp + expReward).coerceAtLeast(0L)
        var leveledUp = false

        // Determine new rank based on updated total XP
        val oldRank = profile.rank
        val newRank = HunterRank.fromTotalXp(newTotalXp)
        val rankedUp = newRank.ordinal > oldRank.ordinal

        // Apply attribute rewards
        var str = profile.str
        var agi = profile.agi
        var sen = profile.sen
        var vit = profile.vit
        var intStat = profile.int_stat

        attributeRewards.forEach { reward ->
            when (reward.attribute) {
                Attribute.STR -> str += reward.amount
                Attribute.AGI -> agi += reward.amount
                Attribute.SEN -> sen += reward.amount
                Attribute.VIT -> vit += reward.amount
                Attribute.INT -> intStat += reward.amount
            }
        }

        // Grant rank ascension stat bonus difference when promoted to upper rank
        if (rankedUp) {
            val statBonusDiff = (newRank.baseStatBonus - oldRank.baseStatBonus).coerceAtLeast(0)
            str += statBonusDiff
            agi += statBonusDiff
            sen += statBonusDiff
            vit += statBonusDiff
            intStat += statBonusDiff
        }

        // Cap stats according to new rank ceiling
        val currentCap = newRank.statCap
        str = str.coerceIn(0, currentCap)
        agi = agi.coerceIn(0, currentCap)
        sen = sen.coerceIn(0, currentCap)
        vit = vit.coerceIn(0, currentCap)
        intStat = intStat.coerceIn(0, currentCap)

        // Calculate dynamic Max HP & Max MP incorporating Rank Ascension Limit Bonuses
        val newMaxHp = 100 + (vit * 15) + newRank.baseHpBonus
        val newMaxMp = 50 + (intStat * 10) + (sen * 5) + newRank.baseMpBonus

        // Check for level-up
        var currentXpToNext = profile.xpToNextLevel.coerceAtLeast(newRank.baseLevelXpCap)
        var levelsGained = 0
        while (newXp >= currentXpToNext) {
            newXp -= currentXpToNext
            currentXpToNext = (currentXpToNext * 1.2f).toInt().coerceAtLeast(currentXpToNext + 1)
            levelsGained++
            leveledUp = true
        }

        // Restore HP & MP fully on level up or rank ascension, or increment MP
        val newHp = if (leveledUp || rankedUp) newMaxHp else profile.hp.coerceAtMost(newMaxHp)
        val newMp = if (leveledUp || rankedUp) newMaxMp else (profile.mp + mpReward).coerceIn(0, newMaxMp)

        val updatedProfile = profile.copy(
            level = profile.level + levelsGained,
            xp = newXp,
            xpToNextLevel = currentXpToNext,
            totalXp = newTotalXp,
            hp = newHp,
            maxHp = newMaxHp,
            mp = newMp,
            maxMp = newMaxMp,
            rank = newRank,
            str = str,
            agi = agi,
            sen = sen,
            vit = vit,
            int_stat = intStat,
        )

        val result = RewardResult(
            xpGained = expReward,
            mpGained = mpReward,
            attributeGains = attributeRewards,
            leveledUp = leveledUp,
            rankedUp = rankedUp,
            newRank = if (rankedUp) newRank else null,
        )

        return updatedProfile to result
    }

    /**
     * Recalculates and enforces rank ascension HP/MP limits, rank tier, and stat caps
     * for a given profile to ensure consistent limits across the application.
     */
    fun recalculateVitals(profile: HunterProfile): HunterProfile {
        val currentRank = HunterRank.fromTotalXp(profile.totalXp)
        val cap = currentRank.statCap

        val str = profile.str.coerceIn(0, cap)
        val agi = profile.agi.coerceIn(0, cap)
        val sen = profile.sen.coerceIn(0, cap)
        val vit = profile.vit.coerceIn(0, cap)
        val intStat = profile.int_stat.coerceIn(0, cap)

        val maxHp = 100 + (vit * 15) + currentRank.baseHpBonus
        val maxMp = 50 + (intStat * 10) + (sen * 5) + currentRank.baseMpBonus
        val xpToNext = profile.xpToNextLevel.coerceAtLeast(currentRank.baseLevelXpCap)

        return profile.copy(
            rank = currentRank,
            str = str,
            agi = agi,
            sen = sen,
            vit = vit,
            int_stat = intStat,
            maxHp = maxHp,
            hp = profile.hp.coerceAtMost(maxHp),
            maxMp = maxMp,
            mp = profile.mp.coerceAtMost(maxMp),
            xpToNextLevel = xpToNext
        )
    }
}

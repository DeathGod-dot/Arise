package com.example.arise.domain

import com.example.arise.data.HunterProfile

object RewardEngine {

    fun processQuestRewards(
        profile: HunterProfile,
        expReward: Int,
        mpReward: Int,
        attributeRewards: List<AttributeReward>
    ): Pair<HunterProfile, RewardResult> {
        var newXp = profile.xp + expReward
        var newTotalXp = profile.totalXp + expReward
        var leveledUp = false

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

        // Calculate dynamic Max HP & Max MP based on Attributes
        val newMaxHp = 100 + (vit * 15)
        val newMaxMp = 50 + (intStat * 10) + (sen * 5)

        // Check for level-up
        var currentXpToNext = profile.xpToNextLevel.coerceAtLeast(100)
        var levelsGained = 0
        while (newXp >= currentXpToNext) {
            newXp -= currentXpToNext
            currentXpToNext = (currentXpToNext * 1.2f).toInt().coerceAtLeast(currentXpToNext + 1)
            levelsGained++
            leveledUp = true
        }

        // Restore HP & MP on level up or add reward MP
        val newHp = if (leveledUp) newMaxHp else profile.hp.coerceAtMost(newMaxHp)
        val newMp = if (leveledUp) newMaxMp else (profile.mp + mpReward).coerceAtMost(newMaxMp)

        // Check for rank-up
        val oldRank = profile.rank
        val newRank = HunterRank.fromTotalXp(newTotalXp)
        val rankedUp = newRank != oldRank

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
}

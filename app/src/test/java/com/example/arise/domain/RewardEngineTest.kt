package com.example.arise.domain

import com.example.arise.data.HunterProfile
import org.junit.Assert.*
import org.junit.Test

class RewardEngineTest {

    @Test
    fun testProcessQuestRewards_increasesXpAndAttributes() {
        val initialProfile = HunterProfile(
            level = 1,
            xp = 0,
            xpToNextLevel = 100,
            totalXp = 0L,
            str = 10,
            vit = 10
        )

        val attributeRewards = listOf(
            AttributeReward(Attribute.STR, 2),
            AttributeReward(Attribute.VIT, 1)
        )

        val (updatedProfile, result) = RewardEngine.processQuestRewards(
            profile = initialProfile,
            expReward = 50,
            mpReward = 10,
            attributeRewards = attributeRewards
        )

        assertEquals(50, updatedProfile.xp)
        assertEquals(50L, updatedProfile.totalXp)
        assertEquals(12, updatedProfile.str)
        assertEquals(11, updatedProfile.vit)
        assertEquals(50, result.xpGained)
        assertEquals(10, result.mpGained)
        assertFalse(result.leveledUp)
    }

    @Test
    fun testProcessQuestRewards_levelsUpWhenXpThresholdReached() {
        val initialProfile = HunterProfile(
            level = 1,
            xp = 80,
            xpToNextLevel = 100,
            totalXp = 80L
        )

        val (updatedProfile, result) = RewardEngine.processQuestRewards(
            profile = initialProfile,
            expReward = 50,
            mpReward = 20,
            attributeRewards = emptyList()
        )

        // 80 + 50 = 130 >= 100 -> level 2, remaining xp = 30, xpToNext = 120
        assertEquals(2, updatedProfile.level)
        assertEquals(30, updatedProfile.xp)
        assertEquals(120, updatedProfile.xpToNextLevel)
        assertEquals(130L, updatedProfile.totalXp)
        assertTrue(result.leveledUp)
    }

    @Test
    fun testProcessQuestRewards_ranksUpWhenTotalXpThresholdReached() {
        val initialProfile = HunterProfile(
            rank = HunterRank.E,
            totalXp = 9_900L,
            xp = 0,
            xpToNextLevel = 1000
        )

        val (updatedProfile, result) = RewardEngine.processQuestRewards(
            profile = initialProfile,
            expReward = 200,
            mpReward = 0,
            attributeRewards = emptyList()
        )

        // Total XP is now 10_100 >= 10_000 (D-Rank threshold)
        assertEquals(HunterRank.D, updatedProfile.rank)
        assertTrue(result.rankedUp)
        assertEquals(HunterRank.D, result.newRank)
    }
}

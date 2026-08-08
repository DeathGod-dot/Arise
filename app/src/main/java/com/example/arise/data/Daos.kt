package com.example.arise.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HunterDao {
    @Query("SELECT * FROM hunter_profile WHERE id = 1")
    fun getProfile(): Flow<HunterProfile?>

    @Query("SELECT * FROM hunter_profile WHERE id = 1")
    suspend fun getProfileOnce(): HunterProfile?

    @Upsert
    suspend fun upsertProfile(profile: HunterProfile)

    @Query("UPDATE hunter_profile SET xp = :xp, totalXp = :totalXp, rank = :rank WHERE id = 1")
    suspend fun updateXpAndRank(xp: Int, totalXp: Long, rank: String)

    @Query("UPDATE hunter_profile SET hp = :hp WHERE id = 1")
    suspend fun updateHp(hp: Int)

    @Query("UPDATE hunter_profile SET mp = :mp WHERE id = 1")
    suspend fun updateMp(mp: Int)

    @Query("UPDATE hunter_profile SET str = :str, agi = :agi, sen = :sen, vit = :vit, int_stat = :intStat WHERE id = 1")
    suspend fun updateAttributes(str: Int, agi: Int, sen: Int, vit: Int, intStat: Int)

    @Query("UPDATE hunter_profile SET streakDays = :days, lastQuestDate = :date WHERE id = 1")
    suspend fun updateStreak(days: Int, date: String)
}

@Dao
interface QuestDao {
    @Transaction
    @Query("SELECT * FROM quests WHERE dayAssigned = :date ORDER BY type")
    fun getQuestsForDay(date: String): Flow<List<QuestWithSubObjectives>>

    @Transaction
    @Query("SELECT * FROM quests WHERE dayAssigned = :date ORDER BY type")
    suspend fun getQuestsForDayOnce(date: String): List<QuestWithSubObjectives>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuest(quest: QuestEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubObjectives(objectives: List<SubObjectiveEntity>)

    @Query("UPDATE quests SET status = :status, completedAt = :completedAt WHERE id = :questId")
    suspend fun updateQuestStatus(questId: String, status: String, completedAt: Long? = null)

    @Query("UPDATE sub_objectives SET current = :current, isComplete = :isComplete WHERE id = :id")
    suspend fun updateSubObjective(id: String, current: Int, isComplete: Boolean)

    @Query("UPDATE sub_objectives SET proofImageUri = :uri WHERE id = :id")
    suspend fun updateProofImage(id: String, uri: String)

    @Query("SELECT COUNT(*) FROM quests WHERE status = 'CLEARED'")
    fun getClearedQuestsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM quests WHERE dayAssigned = :date AND status != 'CLEARED'")
    fun getPendingQuestsCountToday(date: String): Flow<Int>

    @Query("UPDATE quests SET reflectionText = :text WHERE id = :questId")
    suspend fun updateReflection(questId: String, text: String)

    @Query("SELECT COUNT(*) FROM quests WHERE dayAssigned = :date AND status != 'CLEARED'")
    suspend fun getIncompleteQuestCount(date: String): Int

    @Query("DELETE FROM quests WHERE dayAssigned = :date")
    suspend fun deleteQuestsForDay(date: String)

    @Query("SELECT SUM(expReward) FROM quests WHERE dayAssigned = :date AND status = 'CLEARED'")
    suspend fun getClearedXpForDate(date: String): Int?
}

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<SystemLogEntryEntity>>

    @Insert
    suspend fun insertLog(entry: SystemLogEntryEntity)

    @Query("DELETE FROM system_log WHERE timestamp < :before")
    suspend fun deleteOldLogs(before: Long)
}

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM daily_snapshots ORDER BY date DESC LIMIT :limit")
    fun getRecentSnapshots(limit: Int = 7): Flow<List<DailySnapshot>>

    @Upsert
    suspend fun upsertSnapshot(snapshot: DailySnapshot)

    @Query("SELECT COUNT(*) FROM daily_snapshots WHERE isActiveDay = 1")
    fun getActiveDaysCount(): Flow<Int>

    @Query("SELECT SUM(totalXpEarned) FROM daily_snapshots")
    fun getTotalXpAllTime(): Flow<Long?>

    @Query("SELECT * FROM daily_snapshots WHERE date = :date LIMIT 1")
    suspend fun getSnapshotForDate(date: String): DailySnapshot?
    
    @Query("SELECT * FROM daily_snapshots WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getSnapshotsForRange(startDate: String, endDate: String): List<DailySnapshot>
}

@Dao
interface PenaltySessionDao {
    @Query("SELECT * FROM penalty_sessions WHERE isActive = 1 LIMIT 1")
    fun getActivePenaltySession(): Flow<PenaltySessionEntity?>

    @Query("SELECT * FROM penalty_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePenaltySessionOnce(): PenaltySessionEntity?

    @Upsert
    suspend fun upsertPenaltySession(session: PenaltySessionEntity)

    @Query("UPDATE penalty_sessions SET isActive = 0, resolvedAt = :resolvedAt WHERE id = :sessionId")
    suspend fun resolvePenaltySession(sessionId: String, resolvedAt: Long = System.currentTimeMillis())
}

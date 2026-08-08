package com.example.arise.data

import com.example.arise.domain.*
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AriseRepository @Inject constructor(
    private val database: AriseDatabase,
    private val hunterDao: HunterDao,
    private val questDao: QuestDao,
    private val systemLogDao: SystemLogDao,
    private val snapshotDao: SnapshotDao,
    private val penaltySessionDao: PenaltySessionDao,
) {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun <R> withTransaction(block: suspend () -> R): R =
        database.withTransaction(block)

    // --- Penalty Sessions ---
    fun getActivePenaltySession(): Flow<PenaltySessionEntity?> =
        penaltySessionDao.getActivePenaltySession()

    suspend fun getActivePenaltySessionOnce(): PenaltySessionEntity? =
        penaltySessionDao.getActivePenaltySessionOnce()

    suspend fun upsertPenaltySession(session: PenaltySessionEntity) =
        penaltySessionDao.upsertPenaltySession(session)

    suspend fun resolvePenaltySession(sessionId: String) =
        penaltySessionDao.resolvePenaltySession(sessionId)

    // --- Hunter Profile ---
    fun getProfile(): Flow<HunterProfile?> = hunterDao.getProfile()
    suspend fun getProfileOnce(): HunterProfile? = hunterDao.getProfileOnce()
    suspend fun upsertProfile(profile: HunterProfile) = hunterDao.upsertProfile(profile)
    suspend fun updateXpAndRank(xp: Int, totalXp: Long, rank: HunterRank) =
        hunterDao.updateXpAndRank(xp, totalXp, rank.name)
    suspend fun updateHp(hp: Int) = hunterDao.updateHp(hp)
    suspend fun updateMp(mp: Int) = hunterDao.updateMp(mp)
    suspend fun updateAttributes(str: Int, agi: Int, sen: Int, vit: Int, intStat: Int) =
        hunterDao.updateAttributes(str, agi, sen, vit, intStat)
    suspend fun updateStreak(days: Int, date: LocalDate) =
        hunterDao.updateStreak(days, date.format(dateFormat))

    // --- Quests ---
    fun getTodayQuests(): Flow<List<QuestWithSubObjectives>> =
        questDao.getQuestsForDay(LocalDate.now().format(dateFormat))
    suspend fun getTodayQuestsOnce(): List<QuestWithSubObjectives> =
        questDao.getQuestsForDayOnce(LocalDate.now().format(dateFormat))
    suspend fun insertQuest(quest: QuestEntity) = questDao.insertQuest(quest)
    suspend fun insertSubObjectives(objectives: List<SubObjectiveEntity>) =
        questDao.insertSubObjectives(objectives)
    suspend fun updateQuestStatus(questId: String, status: QuestStatus, completedAt: Long? = null) =
        questDao.updateQuestStatus(questId, status.name, completedAt)
    suspend fun updateSubObjective(id: String, current: Int, isComplete: Boolean) =
        questDao.updateSubObjective(id, current, isComplete)
    suspend fun updateProofImage(id: String, uri: String) = questDao.updateProofImage(id, uri)
    suspend fun updateReflection(questId: String, text: String) =
        questDao.updateReflection(questId, text)
    suspend fun getIncompleteQuestCount(date: LocalDate): Int =
        questDao.getIncompleteQuestCount(date.format(dateFormat))
    fun getClearedQuestsCount(): Flow<Int> = questDao.getClearedQuestsCount()
    fun getPendingQuestsCountToday(): Flow<Int> =
        questDao.getPendingQuestsCountToday(LocalDate.now().format(dateFormat))
    suspend fun deleteQuestsForDay(date: String) = questDao.deleteQuestsForDay(date)
    suspend fun getClearedXpForDate(date: String): Int = questDao.getClearedXpForDate(date) ?: 0

    // --- System Log ---
    fun getRecentLogs(limit: Int = 50): Flow<List<SystemLogEntryEntity>> =
        systemLogDao.getRecentLogs(limit)
    suspend fun logEvent(icon: LogIcon, message: String) =
        systemLogDao.insertLog(SystemLogEntryEntity(icon = icon, message = message))
    suspend fun deleteOldLogs(before: Long) = systemLogDao.deleteOldLogs(before)

    // --- Snapshots ---
    fun getRecentSnapshots(limit: Int = 7): Flow<List<DailySnapshot>> =
        snapshotDao.getRecentSnapshots(limit)
    suspend fun upsertSnapshot(snapshot: DailySnapshot) = snapshotDao.upsertSnapshot(snapshot)
    fun getActiveDaysCount(): Flow<Int> = snapshotDao.getActiveDaysCount()
    fun getTotalXpAllTime(): Flow<Long?> = snapshotDao.getTotalXpAllTime()
    suspend fun getSnapshotForDate(date: String): DailySnapshot? = snapshotDao.getSnapshotForDate(date)
    suspend fun getSnapshotsForRange(startDate: String, endDate: String): List<DailySnapshot> = 
        snapshotDao.getSnapshotsForRange(startDate, endDate)
}

package com.example.arise.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arise.data.*
import com.example.arise.domain.*
import com.example.arise.service.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class QuestsUiState(
    val quests: List<QuestWithSubObjectives> = emptyList(),
    val allCompleted: Boolean = false,
    val rewardsClaimed: Boolean = false,
    val penaltyWarningVisible: Boolean = false,
    val penaltyDeadlineMs: Long = 0L,
    val isRestDay: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val repository: AriseRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestsUiState())
    val uiState: StateFlow<QuestsUiState> = _uiState.asStateFlow()

    init {
        loadQuests()
    }

    private fun loadQuests() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val isSunday = today.dayOfWeek == java.time.DayOfWeek.SUNDAY
            val existing = repository.getTodayQuestsOnce()

            // 24-Hour Reset: Check if existing quests are missing or from a previous day
            val needsReset = existing.isEmpty() || existing.any { it.quest.dayAssigned != todayStr }
            if (needsReset) {
                seedDailyQuests()
            }

            // Saturday evening rest day notification (fire once on Saturday after 6 PM)
            if (today.dayOfWeek == java.time.DayOfWeek.SATURDAY) {
                val hour = java.time.LocalTime.now().hour
                if (hour >= 18) {
                    val profile = repository.getProfileOnce()
                    val satKey = "rest_notified_$todayStr"
                    val prefs = context.getSharedPreferences("arise_settings", Context.MODE_PRIVATE)
                    if (!prefs.getBoolean(satKey, false)) {
                        NotificationHelper.showSystemNotification(
                            context,
                            "⚔️ SYSTEM NOTICE: REST DAY PROTOCOL",
                            "Tomorrow is designated Recovery Day. All penalties are suspended. Train if you wish — rewards still apply. Rest well, Hunter."
                        )
                        prefs.edit().putBoolean(satKey, true).apply()
                    }
                }
            }

            // ─── ONE-TIME RETROACTIVE XP FIX ───
            // Deposit rewards for any quests already CLEARED but never credited
            retroactiveRewardFix()

            repository.getTodayQuests().collect { quests ->
                val todayQuests = quests.filter { it.quest.dayAssigned == todayStr }
                val allDone = todayQuests.isNotEmpty() && todayQuests.all { it.quest.status == QuestStatus.CLEARED }
                val hasDeadline = todayQuests.any { it.quest.deadline > 0L }
                val earliestDeadline = todayQuests.filter { it.quest.status != QuestStatus.CLEARED }
                    .minOfOrNull { it.quest.deadline } ?: 0L
                val timeLeft = earliestDeadline - System.currentTimeMillis()
                val showWarning = hasDeadline && timeLeft in 1..7_200_000 // < 2 hours

                val profile = repository.getProfileOnce()
                val claimed = profile?.lastQuestDate == todayStr

                _uiState.update {
                    it.copy(
                        quests = todayQuests,
                        allCompleted = allDone,
                        rewardsClaimed = claimed,
                        penaltyWarningVisible = if (isSunday) false else showWarning,
                        penaltyDeadlineMs = if (isSunday) 0L else earliestDeadline,
                        isRestDay = isSunday,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private suspend fun seedDailyQuests() {
        val today = LocalDate.now()
        val dailyQuests = QuestFactory.createDailyQuests(today)
        repository.withTransaction {
            dailyQuests.forEach { (quest, subs) ->
                repository.insertQuest(quest)
                repository.insertSubObjectives(subs)
            }
        }
        repository.logEvent(LogIcon.INFO, "New daily quests received for ${today.format(DateTimeFormatter.ISO_LOCAL_DATE)}.")
    }

    fun completeSubObjectiveWithReflection(subId: String, questId: String, reflectionText: String) {
        viewModelScope.launch {
            val quests = repository.getTodayQuestsOnce()
            val questData = quests.find { it.quest.id == questId } ?: return@launch
            val sub = questData.subObjectives.find { it.id == subId } ?: return@launch

            repository.updateSubObjective(subId, sub.target, true)
            if (reflectionText.isNotBlank()) {
                repository.updateReflection(questId, reflectionText)
            }
            repository.logEvent(LogIcon.QUEST_COMPLETE, "Completed '${sub.label}'.")
            NotificationHelper.showSubObjectiveCompletedNotification(context, sub.label)
            checkQuestCompletion(questId)
        }
    }

    fun incrementSubObjective(subId: String, questId: String) {
        viewModelScope.launch {
            val quests = repository.getTodayQuestsOnce()
            val questData = quests.find { it.quest.id == questId } ?: return@launch
            if (questData.quest.status == QuestStatus.CLEARED) return@launch
            
            repository.atomicIncrementSubObjective(subId, 1)
            checkQuestCompletion(questId)
        }
    }

    fun updateSubObjectiveValue(subId: String, questId: String, value: Int) {
        viewModelScope.launch {
            val quests = repository.getTodayQuestsOnce()
            val questData = quests.find { it.quest.id == questId } ?: return@launch
            if (questData.quest.status == QuestStatus.CLEARED) return@launch
            val sub = questData.subObjectives.find { it.id == subId } ?: return@launch
            val isComplete = value >= sub.target
            repository.updateSubObjective(subId, value, isComplete)
            checkQuestCompletion(questId)
        }
    }

    fun updateTimerProgress(subId: String, questId: String, elapsedSeconds: Int) {
        viewModelScope.launch {
            val quests = repository.getTodayQuestsOnce()
            val questData = quests.find { it.quest.id == questId } ?: return@launch
            val sub = questData.subObjectives.find { it.id == subId } ?: return@launch
            val newCurrent = elapsedSeconds.coerceIn(0, sub.target)
            // For Study TIMER sub-objectives, completing requires pressing COMPLETE SESSION with Knowledge Log
            val isComplete = if (sub.type == SubObjectiveType.TIMER) false else newCurrent >= sub.target
            repository.updateSubObjective(subId, newCurrent, isComplete)
            checkQuestCompletion(questId)
        }
    }

    fun updateReflection(questId: String, text: String) {
        viewModelScope.launch {
            repository.updateReflection(questId, text)
        }
    }

    private val claimRewardsMutex = kotlinx.coroutines.sync.Mutex()

    fun claimAllRewards() {
        viewModelScope.launch {
            claimRewardsMutex.withLock {
                val today = LocalDate.now()
                val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val profile = repository.getProfileOnce() ?: return@withLock

                // Early exit: already claimed today — prevents double-tap streak inflation
                if (profile.lastQuestDate == todayStr) {
                    _uiState.update { it.copy(rewardsClaimed = true) }
                    return@withLock
                }

                val quests = repository.getTodayQuestsOnce()
                val completedQuests = quests.filter { it.quest.status == QuestStatus.CLEARED }
                if (completedQuests.isEmpty()) return@withLock

                // XP/MP/Stats are already deposited per-quest on completion.
                // claimAllRewards now handles: streak update, daily log acknowledgment, bonus notification.
                val totalExpGained = completedQuests.sumOf { it.quest.expReward }
                val totalMpGained = completedQuests.sumOf { it.quest.mpReward }

                repository.logEvent(LogIcon.QUEST_COMPLETE, "Claimed daily rewards: +$totalExpGained EXP, +$totalMpGained MP.")

                // System Notification for reward claim
                NotificationHelper.showSystemNotification(
                    context,
                    "SYSTEM NOTICE: ALL DAILIES CLEARED!",
                    "All daily quests completed! Total: +$totalExpGained EXP & +$totalMpGained MP deposited to Hunter Profile."
                )

                // Update streak
                val yesterdayStr = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val isMonday = today.dayOfWeek == java.time.DayOfWeek.MONDAY
                val isTuesday = today.dayOfWeek == java.time.DayOfWeek.TUESDAY

                val streak = if (profile.lastQuestDate == yesterdayStr) {
                    // Completed yesterday — extend streak
                    profile.streakDays + 1
                } else if (isMonday && profile.lastQuestDate == today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)) {
                    // Monday + last was Saturday: Sunday rest day preserves streak
                    profile.streakDays + 1
                } else if (isTuesday && profile.lastQuestDate == today.minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE)) {
                    // Tuesday + last was Saturday: Sunday rest day + Monday skip still bridges
                    profile.streakDays + 1
                } else {
                    // Gap too large — reset streak
                    1
                }

                // updateStreak writes both streakDays AND lastQuestDate atomically
                repository.updateStreak(streak, today)

                _uiState.update { it.copy(rewardsClaimed = true) }
            }
        }
    }

    private val questCompletionMutex = kotlinx.coroutines.sync.Mutex()

    private suspend fun checkQuestCompletion(questId: String) {
        questCompletionMutex.withLock {
            val quests = repository.getTodayQuestsOnce()
            val questData = quests.find { it.quest.id == questId } ?: return
            val allSubsDone = questData.subObjectives.all { it.isComplete }
            if (allSubsDone && questData.quest.status != QuestStatus.CLEARED) {
                repository.updateQuestStatus(questId, QuestStatus.CLEARED, System.currentTimeMillis())
                repository.logEvent(LogIcon.QUEST_COMPLETE, "Finished quest '${questData.quest.title}'.")
                NotificationHelper.showQuestCompletedNotification(context, questData.quest.title, questData.quest.expReward, questData.quest.mpReward)

                // ─── IMMEDIATE XP/MP/STAT DEPOSIT ON QUEST CLEAR ───
                depositQuestRewards(questData)
            } else if (!allSubsDone && questData.quest.status != QuestStatus.ONGOING && questData.quest.status != QuestStatus.CLEARED) {
                repository.updateQuestStatus(questId, QuestStatus.ONGOING)
            }
        }
    }

    private suspend fun depositQuestRewards(questData: QuestWithSubObjectives) {
        val quest = questData.quest
        val today = java.time.LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        var rankedUp = false
        var newRankName: String? = null

        repository.withTransaction {
            val profile = repository.getProfileOnce() ?: return@withTransaction
            val (updatedProfile, result) = RewardEngine.processQuestRewards(
                profile,
                quest.expReward,
                quest.mpReward,
                quest.attributeRewards,
            )

            rankedUp = result.rankedUp
            newRankName = result.newRank?.displayName

            repository.upsertProfile(updatedProfile)

            // Log attribute gains
            result.attributeGains.forEach { gain ->
                repository.logEvent(LogIcon.STAT_UP, "${gain.attribute.name} +${gain.amount} from training.")
            }

            if (result.rankedUp && result.newRank != null) {
                repository.logEvent(LogIcon.STAT_UP, "RANK UP! You are now a ${result.newRank.displayName} Hunter!")
            }

            repository.logEvent(LogIcon.QUEST_COMPLETE, "Earned +${quest.expReward} EXP, +${quest.mpReward} MP from '${quest.title}'.")

            val existingSnapshot = repository.getSnapshotForDate(todayStr)
            val prevXp = existingSnapshot?.totalXpEarned ?: 0
            val prevCompleted = existingSnapshot?.questsCompleted ?: 0
            repository.upsertSnapshot(
                DailySnapshot(
                    date = todayStr,
                    totalXpEarned = prevXp + quest.expReward,
                    questsCompleted = prevCompleted + 1,
                    isActiveDay = true,
                    rank = updatedProfile.rank,
                )
            )
        }

        if (rankedUp && newRankName != null) {
            NotificationHelper.showLevelUpNotification(context, newRankName!!)
        }

    }

    /**
     * One-time retroactive fix: Checks for quests that were CLEARED before
     * the per-quest XP deposit was implemented and credits their missing rewards.
     * Compares expected total XP from today's cleared quests against the snapshot.
     */
    private suspend fun retroactiveRewardFix() {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val quests = repository.getTodayQuestsOnce()
        val clearedQuests = quests.filter {
            it.quest.status == QuestStatus.CLEARED && it.quest.dayAssigned == todayStr
        }
        if (clearedQuests.isEmpty()) return

        val expectedTotalXp = clearedQuests.sumOf { it.quest.expReward }
        val snapshot = repository.getSnapshotForDate(todayStr)
        val alreadyCreditedXp = snapshot?.totalXpEarned ?: 0

        // If snapshot already matches expected, rewards were already deposited
        if (alreadyCreditedXp >= expectedTotalXp) return

        // Calculate missing XP and deposit only the difference
        val missingXp = expectedTotalXp - alreadyCreditedXp

        // Find which quests haven't been credited yet
        // Simple approach: deposit all cleared quests that haven't been tracked in snapshot
        val creditedQuestCount = snapshot?.questsCompleted ?: 0
        val uncreditedQuests = clearedQuests.drop(creditedQuestCount)

        if (uncreditedQuests.isEmpty() && missingXp > 0) {
            // Edge case: all quests counted but XP is wrong — credit the raw difference
            val profile = repository.getProfileOnce() ?: return
            val (updatedProfile, result) = RewardEngine.processQuestRewards(
                profile, missingXp, 0, emptyList()
            )
            repository.upsertProfile(updatedProfile)
            repository.upsertSnapshot(
                DailySnapshot(
                    date = todayStr,
                    totalXpEarned = expectedTotalXp,
                    questsCompleted = clearedQuests.size,
                    isActiveDay = true,
                    rank = updatedProfile.rank,
                )
            )
            repository.logEvent(LogIcon.INFO, "Recovered +$missingXp EXP from previously cleared quests.")
            if (result.rankedUp && result.newRank != null) {
                repository.logEvent(LogIcon.STAT_UP, "RANK UP! You are now a ${result.newRank.displayName} Hunter!")
                NotificationHelper.showLevelUpNotification(context, result.newRank.displayName)
            }
        } else {
            // Deposit each uncredited quest's full rewards
            for (questData in uncreditedQuests) {
                depositQuestRewards(questData)
            }
            repository.logEvent(LogIcon.INFO, "Credited rewards for ${uncreditedQuests.size} quest(s).")
        }
    }
}

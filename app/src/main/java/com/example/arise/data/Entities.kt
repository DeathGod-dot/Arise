package com.example.arise.data

import androidx.room.*
import com.example.arise.domain.*

@Entity(tableName = "hunter_profile")
data class HunterProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "HUNTER",
    val age: Int = 21,
    val weightKg: Float = 70.0f,
    val rank: HunterRank = HunterRank.E,
    val level: Int = 1,
    val hp: Int = 150,
    val maxHp: Int = 150,
    val mp: Int = 20,
    val maxMp: Int = 20,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val totalXp: Long = 0L,
    val str: Int = 10,
    val agi: Int = 12,
    val sen: Int = 11,
    val vit: Int = 10,
    val int_stat: Int = 8,
    val streakDays: Int = 0,
    val lastQuestDate: String = "",
    val avatarUri: String? = null,
    val alarmAudioUri: String? = null,
    val alarmToneName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val status: QuestStatus = QuestStatus.PENDING,
    val expReward: Int = 0,
    val mpReward: Int = 0,
    val attributeRewards: List<AttributeReward> = emptyList(),
    val deadline: Long = 0L,
    val isDaily: Boolean = true,
    val dayAssigned: String = "",
    val completedAt: Long? = null,
    val timerDurationSeconds: Long = 0L,
    val reflectionText: String? = null
)

@Entity(
    tableName = "sub_objectives",
    foreignKeys = [ForeignKey(
        entity = QuestEntity::class,
        parentColumns = ["id"],
        childColumns = ["questId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("questId")]
)
data class SubObjectiveEntity(
    @PrimaryKey val id: String,
    val questId: String,
    val label: String,
    val type: SubObjectiveType,
    val current: Int = 0,
    val target: Int = 0,
    val unit: String = "",
    val proofImageUri: String? = null,
    val isComplete: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "system_log")
data class SystemLogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val icon: LogIcon,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_snapshots")
data class DailySnapshot(
    @PrimaryKey val date: String,
    val totalXpEarned: Int = 0,
    val questsCompleted: Int = 0,
    val isActiveDay: Boolean = false,
    val rank: HunterRank = HunterRank.E
)

@Entity(tableName = "penalty_sessions")
data class PenaltySessionEntity(
    @PrimaryKey val id: String,
    val triggeredAt: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 14400L, // 4 hours
    val isActive: Boolean = true,
    val tasksCompleted: Int = 0,
    val totalTasks: Int = 6,
    val resolvedAt: Long? = null
)

// Relation: Quest with its sub-objectives
data class QuestWithSubObjectives(
    @Embedded val quest: QuestEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questId"
    )
    val subObjectives: List<SubObjectiveEntity>
)

package com.example.arise.data

import androidx.room.TypeConverter
import com.example.arise.domain.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromHunterRank(rank: HunterRank): String = rank.name
    @TypeConverter
    fun toHunterRank(value: String): HunterRank = HunterRank.valueOf(value)

    @TypeConverter
    fun fromQuestType(type: QuestType): String = type.name
    @TypeConverter
    fun toQuestType(value: String): QuestType = QuestType.valueOf(value)

    @TypeConverter
    fun fromQuestStatus(status: QuestStatus): String = status.name
    @TypeConverter
    fun toQuestStatus(value: String): QuestStatus = QuestStatus.valueOf(value)

    @TypeConverter
    fun fromSubObjectiveType(type: SubObjectiveType): String = type.name
    @TypeConverter
    fun toSubObjectiveType(value: String): SubObjectiveType = SubObjectiveType.valueOf(value)

    @TypeConverter
    fun fromLogIcon(icon: LogIcon): String = icon.name
    @TypeConverter
    fun toLogIcon(value: String): LogIcon = LogIcon.valueOf(value)

    @TypeConverter
    fun fromAttributeRewardList(rewards: List<AttributeReward>): String {
        return rewards.joinToString(";") { "${it.attribute.name}:${it.amount}" }
    }
    @TypeConverter
    fun toAttributeRewardList(value: String): List<AttributeReward> {
        if (value.isBlank()) return emptyList()
        return value.split(";").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size == 2) {
                try {
                    AttributeReward(Attribute.valueOf(parts[0]), parts[1].toInt())
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }
}

package com.example.arise.domain

import com.example.arise.data.QuestEntity
import com.example.arise.data.SubObjectiveEntity
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

object QuestFactory {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    fun createDailyQuests(date: LocalDate = LocalDate.now()): List<Pair<QuestEntity, List<SubObjectiveEntity>>> {
        val dateStr = date.format(dateFormat)
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return listOf(
            createPhysicalQuest(dateStr, endOfDay),
            createConsumptionLogQuest(dateStr, endOfDay),
            createStudyQuest(dateStr, endOfDay),
            createMeditationQuest(dateStr, endOfDay),
        )
    }

    private fun createPhysicalQuest(dateStr: String, deadline: Long): Pair<QuestEntity, List<SubObjectiveEntity>> {
        val questId = "physical_$dateStr"
        val quest = QuestEntity(
            id = questId,
            title = "Preparation for the Strong",
            description = "Complete the daily physical training regimen to build combat readiness.",
            type = QuestType.PHYSICAL,
            status = QuestStatus.ONGOING,
            expReward = 1000,
            mpReward = 150,
            attributeRewards = listOf(
                AttributeReward(Attribute.STR, 1),
                AttributeReward(Attribute.AGI, 1)
            ),
            deadline = deadline,
            dayAssigned = dateStr,
        )
        val subs = listOf(
            SubObjectiveEntity(id = "${questId}_pushups", questId = questId, label = "Push-ups", type = SubObjectiveType.REPS, target = 100, unit = "REPS", sortOrder = 0),
            SubObjectiveEntity(id = "${questId}_situps", questId = questId, label = "Sit-ups", type = SubObjectiveType.REPS, target = 100, unit = "REPS", sortOrder = 1),
            SubObjectiveEntity(id = "${questId}_squats", questId = questId, label = "Squats", type = SubObjectiveType.REPS, target = 100, unit = "REPS", sortOrder = 2),
            SubObjectiveEntity(id = "${questId}_pullups", questId = questId, label = "Pull-ups", type = SubObjectiveType.REPS, target = 20, unit = "REPS", sortOrder = 3),
            SubObjectiveEntity(id = "${questId}_run", questId = questId, label = "Run", type = SubObjectiveType.REPS, target = 5000, unit = "METERS", sortOrder = 4),
        )
        return quest to subs
    }

    private fun createConsumptionLogQuest(dateStr: String, deadline: Long): Pair<QuestEntity, List<SubObjectiveEntity>> {
        val questId = "consumption_$dateStr"
        val quest = QuestEntity(
            id = questId,
            title = "Daily Consumption Log",
            description = "Log your daily nutritional intake to maintain vitality.",
            type = QuestType.LOGGING,
            status = QuestStatus.LOGGING,
            expReward = 100,
            mpReward = 0,
            attributeRewards = listOf(AttributeReward(Attribute.VIT, 1)),
            deadline = deadline,
            dayAssigned = dateStr,
        )
        val subs = listOf(
            SubObjectiveEntity(id = "${questId}_calories", questId = questId, label = "Calorie Intake", type = SubObjectiveType.NUMERIC_LOG, target = 1, unit = "KCAL", sortOrder = 0),
            SubObjectiveEntity(id = "${questId}_water", questId = questId, label = "Water Intake", type = SubObjectiveType.NUMERIC_LOG, target = 1, unit = "ML", sortOrder = 1),
        )
        return quest to subs
    }

    private fun createStudyQuest(dateStr: String, deadline: Long): Pair<QuestEntity, List<SubObjectiveEntity>> {
        val questId = "study_$dateStr"
        val quest = QuestEntity(
            id = questId,
            title = "Study Session",
            description = "Review system logs and analyze combat data to sharpen intelligence.",
            type = QuestType.TIMER,
            status = QuestStatus.PENDING,
            expReward = 200,
            mpReward = 50,
            attributeRewards = listOf(AttributeReward(Attribute.INT, 1)),
            deadline = deadline,
            dayAssigned = dateStr,
            timerDurationSeconds = 14400, // 4 hours total
        )
        val subs = listOf(
            SubObjectiveEntity(id = "${questId}_review", questId = questId, label = "Reviewing System Logs", type = SubObjectiveType.TIMER, target = 7200, unit = "SECONDS", sortOrder = 0),
            SubObjectiveEntity(id = "${questId}_analysis", questId = questId, label = "Combat Data Analysis", type = SubObjectiveType.TIMER, target = 7200, unit = "SECONDS", sortOrder = 1),
        )
        return quest to subs
    }

    private fun createMeditationQuest(dateStr: String, deadline: Long): Pair<QuestEntity, List<SubObjectiveEntity>> {
        val questId = "meditation_$dateStr"
        val quest = QuestEntity(
            id = questId,
            title = "Meditation",
            description = "Focus your mind and circulate mana to restore inner energy.",
            type = QuestType.FOCUS,
            status = QuestStatus.PENDING,
            expReward = 50,
            mpReward = 300,
            attributeRewards = listOf(AttributeReward(Attribute.SEN, 1)),
            deadline = deadline,
            dayAssigned = dateStr,
            timerDurationSeconds = 600, // 10 minutes
        )
        val subs = listOf(
            SubObjectiveEntity(id = "${questId}_focus", questId = questId, label = "Mana Circulation", type = SubObjectiveType.FOCUS_TIMER, target = 600, unit = "SECONDS", sortOrder = 0),
        )
        return quest to subs
    }
}

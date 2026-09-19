package com.example.arise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arise.data.*
import com.example.arise.domain.*
import com.example.arise.ui.components.HunterReportCard
import com.example.arise.ui.components.ReportCardData
import com.example.arise.ui.components.StatItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatusUiState(
    val profile: HunterProfile = HunterProfile(),
    val stats: List<StatItem> = emptyList(),
    val logs: List<SystemLogEntryEntity> = emptyList(),
    val reportData: ReportCardData = ReportCardData(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val repository: AriseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getProfile(),
                repository.getClearedQuestsCount(),
                repository.getPendingQuestsCountToday()
            ) { profile, clearedCount, pendingCount ->
                val p = profile ?: createDefaultProfile()
                val report = computeReportData(p, clearedCount, pendingCount)
                StatusUiState(
                    profile = p,
                    stats = listOf(
                        StatItem("STR", p.str.toFloat()),
                        StatItem("AGI", p.agi.toFloat()),
                        StatItem("VIT", p.vit.toFloat()),
                        StatItem("INT", p.int_stat.toFloat()),
                        StatItem("SEN", p.sen.toFloat()),
                        StatItem("MP", (p.maxMp / 10f).coerceAtLeast(10f)),
                    ),
                    logs = _uiState.value.logs,
                    reportData = report,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.update { current ->
                    newState.copy(logs = current.logs)
                }
            }
        }

        // System log
        viewModelScope.launch {
            repository.getRecentLogs(50).collect { logs ->
                _uiState.update { it.copy(logs = logs) }
            }
        }
    }

    private fun computeReportData(
        profile: HunterProfile,
        totalCleared: Int,
        pendingToday: Int
    ): ReportCardData {
        val statMap = listOf(
            "STR" to (profile.str to "High physical muscular force & power output"),
            "AGI" to (profile.agi to "Rapid reflexes, speed & movement agility"),
            "VIT" to (profile.vit to "High stamina, endurance & physical resilience"),
            "INT" to (profile.int_stat to "Knowledge retention & cognitive capacity"),
            "SEN" to (profile.sen to "Heightened spatial awareness & instinct")
        )

        val highest = statMap.maxByOrNull { it.second.first } ?: ("STR" to (10 to ""))
        val lowest = statMap.filter { it.first != highest.first }.minByOrNull { it.second.first } ?: highest

        val recommendation = when (lowest.first) {
            "STR" -> "Perform push-ups and physical strength training to raise STR stat"
            "AGI" -> "Log daily run or cardio sessions to boost AGI stat"
            "VIT" -> "Complete squats and core endurance workouts to increase VIT stat"
            "INT" -> "Engage in 2-Hour Study Sessions to raise INT capacity"
            "SEN" -> "Complete 10-Minute Focus Meditation to sharpen SEN stat"
            else -> "Maintain balanced daily quest progression"
        }

        val isSunday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        val isSundaySpecial = isSunday && pendingToday == 0

        return ReportCardData(
            profile = profile,
            totalClearedQuests = totalCleared,
            pendingQuestsToday = pendingToday,
            strengthStatName = highest.first,
            strengthStatVal = highest.second.first,
            strengthDescription = highest.second.second,
            weaknessStatName = lowest.first,
            weaknessStatVal = lowest.second.first,
            weaknessDescription = lowest.second.second,
            recommendation = recommendation,
            isSundaySpecial = isSundaySpecial
        )
    }

    private suspend fun createDefaultProfile(): HunterProfile {
        val profile = HunterProfile()
        repository.upsertProfile(profile)
        repository.logEvent(LogIcon.INFO, "System activated. Hunter status initialized.")
        return profile
    }
}

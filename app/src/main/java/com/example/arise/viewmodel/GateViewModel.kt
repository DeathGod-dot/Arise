package com.example.arise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arise.data.*
import com.example.arise.domain.HunterRank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GateUiState(
    val currentRank: HunterRank = HunterRank.E,
    val nextRank: HunterRank? = HunterRank.D,
    val xpInRank: Long = 0L,
    val xpToNextRank: Long = 10_000L,
    val rankProgressPercent: Float = 0f,
    val weeklyXpData: List<Int> = emptyList(),
    val weeklyLabels: List<String> = emptyList(),
    val weeklyTrendPercent: Float = 0f,
    val totalXp: Long = 0L,
    val weekXpDelta: Int = 0,
    val activeDays: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val weekOffset: Int = 0,
    val weekRangeLabel: String = ""
)

@HiltViewModel
class GateViewModel @Inject constructor(
    private val repository: AriseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GateUiState())
    val uiState: StateFlow<GateUiState> = _uiState.asStateFlow()
    private val _weekOffset = MutableStateFlow(0)

    fun shiftWeek(delta: Int) {
        val newOffset = (_weekOffset.value + delta).coerceIn(-4, 0)
        _weekOffset.value = newOffset
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // BUG-17 Cleanup old logs
            val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            repository.deleteOldLogs(thirtyDaysAgo)
        }

        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                val p = profile ?: return@collect
                val rank = p.rank
                val next = rank.nextRank
                val xpInRank = p.totalXp - rank.xpThreshold
                val xpNeeded = rank.xpToNextRank.coerceAtLeast(1L)
                val progress = if (next != null) (xpInRank.toFloat() / xpNeeded * 100f).coerceIn(0f, 100f) else 100f

                _uiState.update {
                    it.copy(
                        currentRank = rank,
                        nextRank = next,
                        xpInRank = xpInRank,
                        xpToNextRank = xpNeeded,
                        rankProgressPercent = progress,
                        currentStreak = p.streakDays,
                        isLoading = false,
                    )
                }
            }
        }

        viewModelScope.launch {
            _weekOffset.collectLatest { offset ->
                val today = java.time.LocalDate.now().plusWeeks(offset.toLong())
                val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val dayLabels = last7Days.map { it.dayOfWeek.name.take(3) }

                val startDateStr = last7Days.first().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val endDateStr = last7Days.last().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val snapshots = repository.getSnapshotsForRange(startDateStr, endDateStr)

                val snapshotMap = snapshots.associateBy { it.date }
                val xpData = last7Days.map { date ->
                    val dateStr = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                    val snapshotXp = snapshotMap[dateStr]?.totalXpEarned ?: 0
                    if (snapshotXp > 0) {
                        snapshotXp
                    } else {
                        repository.getClearedXpForDate(dateStr)
                    }
                }

                val weekTotal = xpData.sum()
                
                val prevStartDate = today.minusDays(13).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val prevEndDate = today.minusDays(7).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val prevWeekSnapshots = repository.getSnapshotsForRange(prevStartDate, prevEndDate)
                val prevWeekTotal = if (prevWeekSnapshots.isNotEmpty()) {
                    prevWeekSnapshots.sumOf { it.totalXpEarned }
                } else {
                    (13 downTo 7).map { today.minusDays(it.toLong()) }.sumOf { repository.getClearedXpForDate(it.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)) }
                }
                
                val trend = if (prevWeekSnapshots.isEmpty() || prevWeekTotal == 0) {
                    0f
                } else {
                    ((weekTotal - prevWeekTotal).toFloat() / prevWeekTotal * 100f)
                }

                val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d")
                val weekRangeLabel = "${last7Days.first().format(formatter)} - ${last7Days.last().format(formatter)}"

                _uiState.update {
                    it.copy(
                        weeklyXpData = xpData,
                        weeklyLabels = dayLabels,
                        weeklyTrendPercent = trend,
                        weekXpDelta = weekTotal,
                        weekOffset = offset,
                        weekRangeLabel = weekRangeLabel
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getTotalXpAllTime().collect { total ->
                _uiState.update { it.copy(totalXp = total ?: 0L) }
            }
        }

        viewModelScope.launch {
            repository.getActiveDaysCount().collect { days ->
                _uiState.update { it.copy(activeDays = days) }
            }
        }
    }
}

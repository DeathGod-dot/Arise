package com.example.arise.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.arise.data.*
import com.example.arise.domain.*
import com.example.arise.ui.components.*
import com.example.arise.ui.components.AnimatedScreenEntrance
import com.example.arise.ui.theme.*
import com.example.arise.viewmodel.QuestsViewModel

@Composable
fun QuestsScreen(
    viewModel: QuestsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val onSurf = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val onSurfVar = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar / Title with accent diamond line
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "DAILY QUESTS",
                style = DisplayRank.copy(fontSize = 20.sp, letterSpacing = 3.sp),
                color = onSurf
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.width(30.dp), color = primaryColor.copy(alpha = 0.5f))
                Text("◆", color = primaryColor, fontSize = 8.sp)
                HorizontalDivider(modifier = Modifier.width(30.dp), color = primaryColor.copy(alpha = 0.5f))
            }
        }

        // ─── SUNDAY RECOVERY DAY BANNER ─────────────────────────
        if (state.isRestDay) {
            AnimatedScreenEntrance(delayMillis = 0) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.PURPLE
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AriseSecondary.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape)
                                .border(1.dp, AriseSecondary.copy(alpha = 0.4f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Rest Day",
                                tint = AriseSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "SUNDAY RECOVERY DAY",
                                    style = DisplayRank.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                    color = AriseSecondary
                                )
                                Surface(
                                    color = AriseSecondary.copy(alpha = 0.2f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "REST PROTOCOL ACTIVE",
                                        style = SystemLabel.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = AriseSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "All penalties are suspended today. Complete quests for full EXP & MP or use today to rest and recharge.",
                                style = SystemLabel.copy(fontSize = 10.sp, lineHeight = 14.sp),
                                color = onSurfVar
                            )
                        }
                    }
                }
            }
        }

        // Exercise verification dialog state
        var selectedSubForVerify by remember { mutableStateOf<Pair<SubObjectiveEntity, String>?>(null) }

        selectedSubForVerify?.let { (sub, qId) ->
            ExerciseVerificationDialog(
                sub = sub,
                questId = qId,
                viewModel = viewModel,
                onDismiss = { selectedSubForVerify = null }
            )
        }

        // Separate quests by category
        val physicalQuest = state.quests.find { it.quest.type == QuestType.PHYSICAL }
        val loggingQuest = state.quests.find { it.quest.type == QuestType.LOGGING }
        val studyQuest = state.quests.find { it.quest.type == QuestType.TIMER }
        val meditationQuest = state.quests.find { it.quest.type == QuestType.FOCUS }

        // ─── BOX 1: Preparation for the Strong (Physical) ─────────
        physicalQuest?.let { questWithSubs ->
            AnimatedScreenEntrance(delayMillis = 80) {
                GlitchEntryAnimation(index = 0) {
                    SingleQuestCard(questWithSubs, viewModel) {
                        PhysicalSubObjectives(
                            subs = questWithSubs.subObjectives.sortedBy { it.sortOrder },
                            questId = questWithSubs.quest.id,
                            viewModel = viewModel,
                            onVerifyClick = { sub -> selectedSubForVerify = Pair(sub, questWithSubs.quest.id) }
                        )
                    }
                }
            }
        }

        // ─── BOX 2: Daily Consumption Log (Logging) ─────────────
        loggingQuest?.let { questWithSubs ->
            AnimatedScreenEntrance(delayMillis = 160) {
                GlitchEntryAnimation(index = 1) {
                    SingleQuestCard(questWithSubs, viewModel) {
                        LoggingSubObjectives(questWithSubs.subObjectives.sortedBy { it.sortOrder }, questWithSubs.quest.id, viewModel)
                    }
                }
            }
        }

        // ─── BOX 3: Study & Meditation (Combined 3rd Box) ────────
        AnimatedScreenEntrance(delayMillis = 240) {
            GlitchEntryAnimation(index = 2) {
                CombinedStudyAndMeditationCard(
                    studyQuest = studyQuest,
                    meditationQuest = meditationQuest,
                    viewModel = viewModel
                )
            }
        }

        // ─── BOX 4 (RED HIGHLIGHT): Penalty Quest Warning ─────────
        val timeLeft = (state.penaltyDeadlineMs - System.currentTimeMillis()).coerceAtLeast(0L)
        val hours = (timeLeft / 3_600_000).coerceAtLeast(0)
        val mins = ((timeLeft % 3_600_000) / 60_000).coerceAtLeast(0)
        val secs = ((timeLeft % 60_000) / 1_000).coerceAtLeast(0)
        val timeFormatted = String.format("%02d:%02d:%02d", hours, mins, secs)

        if (state.penaltyWarningVisible) {
            AnimatedScreenEntrance(delayMillis = 320) {
                WarningBanner(
                    title = "PENALTY QUEST WARNING",
                    timeFormatted = if (state.penaltyDeadlineMs > 0) timeFormatted else "INACTIVE"
                )
            }
        }

        // ─── BOX 5: Claim All Rewards Action ──────────────────────
        AnimatedScreenEntrance(delayMillis = 400) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SystemButton(
                    text = if (state.rewardsClaimed) "✓ REWARDS CLAIMED" else "CLAIM ALL REWARDS",
                    onClick = { viewModel.claimAllRewards() },
                    variant = if (state.rewardsClaimed) ButtonVariant.GHOST else ButtonVariant.PRIMARY,
                    enabled = state.allCompleted && !state.rewardsClaimed,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!state.allCompleted && !state.rewardsClaimed) {
                    Text(
                        text = "LOCKED: COMPLETE ALL DAILIES",
                        style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                        color = AriseOnSurfaceVariant.copy(alpha = 0.5f)
                    )
                } else if (state.rewardsClaimed) {
                    Text(
                        text = "EXP & MP ADDED TO HUNTER PROFILE",
                        style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                        color = AriseTertiary
                    )
                }
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}

// ─── CARD WRAPPER ───────────────────────────────────────────────

@Composable
private fun SingleQuestCard(
    questWithSubs: QuestWithSubObjectives,
    viewModel: QuestsViewModel,
    content: @Composable ColumnScope.() -> Unit
) {
    val quest = questWithSubs.quest
    val isCleared = quest.status == QuestStatus.CLEARED
    var isPowerOn by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // CRT Tile Container with CRT Opening/Closing power animation
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isCleared) 0.6f else 1f),
            variant = CardVariant.DEFAULT,
            crtAnimation = true,
            isOpen = isPowerOn
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: CRT Power indicator + status pill + rewards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { isPowerOn = !isPowerOn }
                    ) {
                        Text(
                            text = if (isPowerOn) "⏻" else "⏼",
                            fontSize = 11.sp,
                            color = if (isPowerOn) ArisePrimary else AriseOnSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "[ ${quest.status.label} ]",
                            style = SystemLabel.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = when (quest.status) {
                                QuestStatus.CLEARED -> AriseTertiary
                                QuestStatus.ONGOING -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                QuestStatus.LOGGING -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                QuestStatus.PENDING -> androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                QuestStatus.FAILED -> AriseDangerRed
                            }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (quest.expReward > 0) RewardPill("EXP +${quest.expReward}")
                        if (quest.mpReward > 0) RewardPill("MP +${quest.mpReward}")
                        if (quest.expReward == 0 && quest.mpReward == 0) {
                            quest.attributeRewards.forEach { r -> RewardPill("${r.attribute.name} +${r.amount}") }
                        }
                    }
                }

                // Diamond divider
                DiamondDivider()

                // Quest main title
                Text(
                    text = quest.title,
                    style = AriseTypography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.None
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                content()
            }
        }

        // Sleek CRT Offline bar shown when powered down
        if (!isPowerOn) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPowerOn = true }
                    .padding(vertical = 4.dp),
                color = AriseGlassWhite.copy(alpha = 0.05f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⏼", color = AriseOnSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text(
                            text = "${quest.title.uppercase()} [OFFLINE]",
                            style = SystemLabel.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                            color = AriseOnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "TAP TO CRT POWER ON ▶",
                        style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = ArisePrimary
                    )
                }
            }
        }
    }
}

// ─── COMBINED 3RD BOX (STUDY MORNING/EVENING + MEDITATION) ─────

@Composable
private fun CombinedStudyAndMeditationCard(
    studyQuest: QuestWithSubObjectives?,
    meditationQuest: QuestWithSubObjectives?,
    viewModel: QuestsViewModel
) {
    val morningSub = studyQuest?.subObjectives?.find { it.id.contains("review") } ?: studyQuest?.subObjectives?.getOrNull(0)
    val eveningSub = studyQuest?.subObjectives?.find { it.id.contains("analysis") } ?: studyQuest?.subObjectives?.getOrNull(1)
    val meditationSub = meditationQuest?.subObjectives?.firstOrNull()

    val isStudyCleared = studyQuest?.quest?.status == QuestStatus.CLEARED
    val isMeditationCleared = meditationQuest?.quest?.status == QuestStatus.CLEARED
    val allCleared = isStudyCleared && isMeditationCleared

    val expTotal = (studyQuest?.quest?.expReward ?: 200) + (meditationQuest?.quest?.expReward ?: 50)
    val mpTotal = (studyQuest?.quest?.mpReward ?: 50) + (meditationQuest?.quest?.mpReward ?: 300)

    // Strict Engagement Lock: Tracks which sub-objective is engaged in progress (cannot switch until completed!)
    var engagedSubId by remember { mutableStateOf<String?>(null) }
    var runningTimerSubId by remember { mutableStateOf<String?>(null) }

    fun onStartTimer(subId: String) {
        if (engagedSubId == null) {
            engagedSubId = subId
        }
        if (engagedSubId == subId) {
            runningTimerSubId = if (runningTimerSubId == subId) null else subId
        }
    }

    fun onFinishSession(subId: String) {
        if (engagedSubId == subId) {
            engagedSubId = null
        }
        if (runningTimerSubId == subId) {
            runningTimerSubId = null
        }
    }

    // Helper text for locked non-engaged sessions
    val activeEngagedLabel = when (engagedSubId) {
        morningSub?.id -> "MORNING STUDY"
        eveningSub?.id -> "EVENING STUDY"
        meditationSub?.id -> "MEDITATION"
        else -> null
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (allCleared) 0.6f else 1f),
        variant = CardVariant.DEFAULT
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: status pill + combined rewards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (allCleared) "[ CLEARED ]" else if (engagedSubId != null) "[ SESSION IN PROGRESS ]" else "[ PENDING ]",
                    style = SystemLabel.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = if (allCleared) AriseTertiary else if (engagedSubId != null) ArisePrimary else AriseOnSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RewardPill("EXP +$expTotal")
                    RewardPill("MP +$mpTotal")
                }
            }

            DiamondDivider()

            // 1. Study Session (Morning) — 2 Hours
            studyQuest?.quest?.id?.let { qId ->
                StudySessionItem(
                    sectionTitle = "Study Session (Morning)",
                    sub = morningSub,
                    questId = qId,
                    viewModel = viewModel,
                    engagedSubId = engagedSubId,
                    runningTimerSubId = runningTimerSubId,
                    activeEngagedLabel = activeEngagedLabel,
                    onStartTimer = { onStartTimer(it) },
                    onFinishSession = { onFinishSession(it) }
                )
            }

            HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.4f))

            // 2. Study Session (Evening) — 2 Hours
            studyQuest?.quest?.id?.let { qId ->
                StudySessionItem(
                    sectionTitle = "Study Session (Evening)",
                    sub = eveningSub,
                    questId = qId,
                    viewModel = viewModel,
                    engagedSubId = engagedSubId,
                    runningTimerSubId = runningTimerSubId,
                    activeEngagedLabel = activeEngagedLabel,
                    onStartTimer = { onStartTimer(it) },
                    onFinishSession = { onFinishSession(it) }
                )
            }

            HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.4f))

            // 3. Meditation Session — 10 Minutes Focus
            meditationQuest?.quest?.id?.let { qId ->
                MeditationSessionItem(
                    sub = meditationSub,
                    questId = qId,
                    viewModel = viewModel,
                    engagedSubId = engagedSubId,
                    runningTimerSubId = runningTimerSubId,
                    activeEngagedLabel = activeEngagedLabel,
                    onStartTimer = { onStartTimer(it) },
                    onFinishSession = { onFinishSession(it) }
                )
            }
        }
    }
}

// ─── STUDY SESSION ITEM (2-HOUR TIMER) ──────────────────────────

@Composable
private fun StudySessionItem(
    sectionTitle: String,
    sub: SubObjectiveEntity?,
    questId: String,
    viewModel: QuestsViewModel,
    engagedSubId: String?,
    runningTimerSubId: String?,
    activeEngagedLabel: String?,
    onStartTimer: (String) -> Unit,
    onFinishSession: (String) -> Unit
) {
    if (sub == null) return

    val isCompleted = sub.isComplete
    val isRunning = runningTimerSubId == sub.id
    val isEngagedSelf = engagedSubId == sub.id
    val isEngagedOther = engagedSubId != null && !isEngagedSelf

    var remainingSeconds by remember(sub.id, sub.isComplete, sub.current) {
        mutableStateOf(if (isCompleted) 0 else (sub.target - sub.current).coerceAtLeast(0))
    }
    var reflectionText by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activeServiceState by com.example.arise.service.StudyTimerService.activeTimerState.collectAsStateWithLifecycle()

    // Sync UI with background Foreground Service — single source of truth
    LaunchedEffect(activeServiceState) {
        activeServiceState?.let { serviceState ->
            if (serviceState.subId == sub.id) {
                remainingSeconds = (sub.target - serviceState.elapsedSeconds).coerceAtLeast(0)
                // When service reports timer is done
                if (serviceState.elapsedSeconds >= sub.target && isRunning) {
                    onStartTimer(sub.id) // stop running timer state
                    viewModel.updateTimerProgress(sub.id, questId, sub.target)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Title & Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = AriseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isEngagedOther) AriseOnSurface.copy(alpha = 0.5f) else AriseOnSurface
            )

            val h = remainingSeconds / 3600
            val m = (remainingSeconds % 3600) / 60
            val s = remainingSeconds % 60
            Text(
                text = if (isCompleted) "✓ 00:00:00" else String.format("%02d:%02d:%02d", h, m, s),
                style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) AriseTertiary else if (isRunning) ArisePrimary else AriseOnSurfaceVariant
            )
        }

        // Progress bar
        val progressCurrent = if (isCompleted) sub.target else (sub.target - remainingSeconds)
        ScanBeamProgressBar(
            current = progressCurrent,
            max = sub.target,
            label = "",
            barColor = if (isCompleted) AriseTertiary else if (isEngagedOther) AriseOutlineVariant else ArisePrimary,
            modifier = Modifier.height(18.dp),
            showTextRow = false
        )

        if (isCompleted) {
            // Completed Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AriseTertiary, SciFiCutCornerShape())
                    .background(AriseTertiary.copy(alpha = 0.12f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AriseTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$sectionTitle Completed",
                    style = SystemLabel.copy(fontWeight = FontWeight.Bold, color = AriseTertiary, letterSpacing = 1.sp)
                )
            }
        } else {
            val isTimerFinished = remainingSeconds == 0
            val logUnlocked = isTimerFinished && !isEngagedOther

            // Timer Control Buttons (Start / Pause / Reset) — DISABLED if another session is engaged!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SystemButton(
                    text = if (isEngagedOther) "🔒 $activeEngagedLabel IN PROGRESS" else if (isRunning) "⏸ PAUSE TIMER" else "▶ START TIMER",
                    onClick = {
                        if (!isEngagedOther) {
                            val nextRunning = !isRunning
                            onStartTimer(sub.id)
                            if (nextRunning) {
                                val elapsed = sub.target - remainingSeconds
                                com.example.arise.service.StudyTimerService.start(
                                    context, sub.id, questId, sectionTitle, sub.target, elapsed
                                )
                            } else {
                                com.example.arise.service.StudyTimerService.pause(context)
                                val elapsed = sub.target - remainingSeconds
                                viewModel.updateTimerProgress(sub.id, questId, elapsed)
                            }
                        }
                    },
                    enabled = !isEngagedOther,
                    variant = if (isEngagedOther) ButtonVariant.GHOST else if (isRunning) ButtonVariant.SECONDARY else ButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isEngagedOther) {
                Text(
                    text = "🔒 RESTRICTED: COMPLETE $activeEngagedLabel FIRST TO UNLOCK THIS TASK",
                    style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                    color = AriseDangerRed.copy(alpha = 0.8f)
                )
            } else if (logUnlocked) {
                Text(
                    text = "TIMER COMPLETED — KNOWLEDGE LOG UNLOCKED",
                    style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                    color = AriseTertiary
                )
            }

            Spacer(Modifier.height(2.dp))

            // Knowledge Log section
            Text(
                text = if (logUnlocked) "[ KNOWLEDGE LOG ]" else "[ KNOWLEDGE LOG — 🔒 LOCKED ]",
                style = SystemLabel.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = if (logUnlocked) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

            OutlinedTextField(
                value = reflectionText,
                onValueChange = { if (logUnlocked && it.length <= 500) reflectionText = it },
                enabled = logUnlocked,
                readOnly = !logUnlocked,
                modifier = Modifier.fillMaxWidth().height(90.dp),
                textStyle = AriseTypography.bodySmall.copy(color = if (logUnlocked) androidx.compose.material3.MaterialTheme.colorScheme.onSurface else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                maxLines = 4,
                placeholder = {
                    Text(
                        text = if (logUnlocked) "Enter session summary..." else if (isEngagedOther) "🔒 Complete active $activeEngagedLabel session first..." else "🔒 Complete 2-hour timer to unlock Knowledge Log...",
                        style = AriseTypography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                    disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
                    unfocusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    disabledBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                )
            )

            val hLeft = remainingSeconds / 3600
            val mLeft = (remainingSeconds % 3600) / 60
            val sLeft = remainingSeconds % 60
            val timeLeftStr = String.format("%02d:%02d:%02d", hLeft, mLeft, sLeft)

            Text(
                text = if (logUnlocked) "Summary (max 100 words)" else if (isEngagedOther) "🔒 Task locked" else "🔒 Restricted: $timeLeftStr timer remaining",
                style = SystemLabel.copy(fontSize = 10.sp),
                color = if (logUnlocked) AriseOnSurfaceVariant.copy(alpha = 0.5f) else AriseDangerRed.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.End)
            )

            // Complete Session Button — RESTRICTED & DISABLED until 2 hours complete!
            SystemButton(
                text = if (logUnlocked) "COMPLETE ${sectionTitle.uppercase()}" else if (isEngagedOther) "🔒 TASK LOCKED" else "🔒 LOCKED ($timeLeftStr REMAINING)",
                onClick = {
                    if (logUnlocked) {
                        onFinishSession(sub.id)
                        viewModel.completeSubObjectiveWithReflection(sub.id, questId, reflectionText)
                    }
                },
                enabled = logUnlocked,
                variant = if (logUnlocked) ButtonVariant.PRIMARY else ButtonVariant.GHOST,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── MEDITATION SESSION ITEM (10-MINUTE FOCUS TIMER) ───────────

@Composable
private fun MeditationSessionItem(
    sub: SubObjectiveEntity?,
    questId: String,
    viewModel: QuestsViewModel,
    engagedSubId: String?,
    runningTimerSubId: String?,
    activeEngagedLabel: String?,
    onStartTimer: (String) -> Unit,
    onFinishSession: (String) -> Unit
) {
    if (sub == null) return

    val isCompleted = sub.isComplete
    val isRunning = runningTimerSubId == sub.id
    val isEngagedSelf = engagedSubId == sub.id
    val isEngagedOther = engagedSubId != null && !isEngagedSelf

    var remainingSeconds by remember(sub.id, sub.isComplete, sub.current) {
        mutableStateOf(if (isCompleted) 0 else (sub.target - sub.current).coerceAtLeast(0))
    }

    // Active Countdown Timer Coroutine with Database Progress Persistence
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            remainingSeconds--
            val elapsed = sub.target - remainingSeconds
            viewModel.updateTimerProgress(sub.id, questId, elapsed)
        } else if (remainingSeconds == 0 && isRunning) {
            onFinishSession(sub.id)
            viewModel.completeSubObjectiveWithReflection(sub.id, questId, "")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Meditation",
            style = AriseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isEngagedOther) AriseOnSurface.copy(alpha = 0.5f) else AriseOnSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sub.label,
                style = AriseTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isEngagedOther) AriseOnSurfaceVariant.copy(alpha = 0.5f) else AriseOnSurface
            )
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            Text(
                text = if (isCompleted) "✓ 00:00" else String.format("%02d:%02d", m, s),
                style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) AriseTertiary else if (isRunning) AriseSecondaryDim else AriseOnSurfaceVariant
            )
        }

        val progressCurrent = if (isCompleted) sub.target else (sub.target - remainingSeconds)
        ScanBeamProgressBar(
            current = progressCurrent,
            max = sub.target,
            label = "",
            barColor = if (isCompleted) AriseTertiary else if (isEngagedOther) AriseOutlineVariant else AriseSecondaryDim,
            modifier = Modifier.height(18.dp),
            showTextRow = false
        )

        if (isCompleted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AriseTertiary, SciFiCutCornerShape())
                    .background(AriseTertiary.copy(alpha = 0.12f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AriseTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Meditation Focus Completed",
                    style = SystemLabel.copy(fontWeight = FontWeight.Bold, color = AriseTertiary, letterSpacing = 1.sp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SystemButton(
                    text = if (isEngagedOther) "🔒 $activeEngagedLabel IN PROGRESS" else if (isRunning) "⏸ PAUSE FOCUS" else "⏱ [ START FOCUS SESSION ]",
                    onClick = {
                        if (!isEngagedOther) {
                            val nextRunning = !isRunning
                            onStartTimer(sub.id)
                            if (!nextRunning) {
                                val elapsed = sub.target - remainingSeconds
                                viewModel.updateTimerProgress(sub.id, questId, elapsed)
                            }
                        }
                    },
                    enabled = !isEngagedOther,
                    variant = if (isEngagedOther) ButtonVariant.GHOST else ButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            if (isEngagedOther) Icons.Default.Lock else if (isRunning) Icons.Default.Pause else Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isEngagedOther) AriseOnSurfaceVariant.copy(alpha = 0.5f) else AriseSecondaryDim,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (isEngagedOther) {
                Text(
                    text = "🔒 RESTRICTED: COMPLETE $activeEngagedLabel FIRST TO UNLOCK MEDITATION",
                    style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                    color = AriseDangerRed.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── PHYSICAL QUEST SUB-OBJECTIVES ──────────────────────────────

// ─── PHYSICAL QUEST SUB-OBJECTIVES ──────────────────────────────

@Composable
private fun PhysicalSubObjectives(
    subs: List<SubObjectiveEntity>,
    questId: String,
    viewModel: QuestsViewModel,
    onVerifyClick: (SubObjectiveEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        subs.forEach { sub ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sub.label,
                        style = AriseTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatSubValue(sub),
                        style = SystemLabel.copy(fontWeight = FontWeight.Bold),
                        color = if (sub.isComplete) AriseTertiary else androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScanBeamProgressBar(
                        current = sub.current,
                        max = sub.target,
                        label = "",
                        barColor = if (sub.isComplete) AriseTertiary else ArisePrimary,
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp),
                        showTextRow = false
                    )
                    Row(
                        modifier = Modifier
                            .clickable { onVerifyClick(sub) }
                            .border(1.dp, if (sub.isComplete) AriseTertiary else ArisePrimary.copy(alpha = 0.6f), SciFiCutCornerShape())
                            .background(if (sub.isComplete) AriseTertiary.copy(alpha = 0.1f) else ArisePrimary.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (sub.isComplete) Icons.Default.Verified else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (sub.isComplete) AriseTertiary else ArisePrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (sub.isComplete) "VERIFIED" else "VERIFY",
                            style = SystemLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                            color = if (sub.isComplete) AriseTertiary else ArisePrimary
                        )
                    }
                }
            }
        }
    }
}

// ─── EXERCISE VERIFICATION DIALOG (REP & DISTANCE LOGGER) ──────

@Composable
private fun ExerciseVerificationDialog(
    sub: SubObjectiveEntity,
    questId: String,
    viewModel: QuestsViewModel,
    onDismiss: () -> Unit
) {
    var loggedValue by remember(sub.id) { mutableStateOf(sub.current.toString()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    val isDistance = sub.unit.equals("METERS", ignoreCase = true) || sub.label.contains("Run", ignoreCase = true)
    val targetVal = sub.target
    val currentVal = loggedValue.toIntOrNull() ?: sub.current

    val maxLimit = when {
        sub.label.contains("Push", ignoreCase = true) -> 200
        sub.label.contains("Sit", ignoreCase = true) -> 150
        sub.label.contains("Squat", ignoreCase = true) -> 150
        sub.label.contains("Pull", ignoreCase = true) -> 50
        sub.label.contains("Run", ignoreCase = true) -> 8000
        else -> sub.target * 3
    }

    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = SciFiCutCornerShape(),
            color = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFF090B10),
            border = BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = ArisePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "[ VERIFY EXERCISE ]",
                            style = SystemLabel.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ArisePrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                DiamondDivider()

                // Exercise Title & Current vs Target
                Text(
                    text = sub.label.uppercase(),
                    style = AriseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Progress:",
                        style = AriseTypography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isDistance) "${currentVal / 1000f} / ${targetVal / 1000f} KM" else "$currentVal / $targetVal REPS",
                        style = SystemLabel.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = if (currentVal >= targetVal) AriseTertiary else androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }

                ScanBeamProgressBar(
                    current = currentVal.coerceAtMost(targetVal),
                    max = targetVal,
                    label = "",
                    barColor = if (currentVal >= targetVal) AriseTertiary else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(18.dp),
                    showTextRow = false
                )

                // Quick Increment Buttons (Compact pills to prevent +25 text overflow)
                Text(
                    text = if (isDistance) "QUICK ADD DISTANCE:" else "QUICK ADD REPS:",
                    style = SystemLabel.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isDistance) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(500 to "+0.5 KM", 1000 to "+1.0 KM", 2000 to "+2.0 KM", 5000 to "+5.0 KM").forEach { (addMeters, label) ->
                            QuickAddPill(
                                text = label,
                                onClick = {
                                    val newVal = ((loggedValue.toIntOrNull() ?: sub.current) + addMeters).coerceAtMost(maxLimit)
                                    loggedValue = newVal.toString()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1, 5, 10, 25).forEach { addReps ->
                            QuickAddPill(
                                text = "+$addReps",
                                onClick = {
                                    val newVal = ((loggedValue.toIntOrNull() ?: sub.current) + addReps).coerceAtMost(maxLimit)
                                    loggedValue = newVal.toString()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Custom Exact Number Input
                OutlinedTextField(
                    value = loggedValue,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }
                        val parsed = cleaned.toIntOrNull() ?: 0
                        if (parsed <= maxLimit) {
                            loggedValue = cleaned
                        } else {
                            loggedValue = maxLimit.toString()
                        }
                    },
                    label = { Text("Exact Progress (${if (isDistance) "Meters" else "Reps"})", style = SystemLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AriseTypography.bodyMedium.copy(color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedBorderColor = AriseOutlineVariant,
                        focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                )

                if ((loggedValue.toIntOrNull() ?: 0) >= maxLimit) {
                    Text(
                        text = "MAXIMUM SAFE LIMIT REACHED",
                        style = SystemLabel.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                        color = AriseDangerRed
                    )
                }

                // Optional Photo Attachment with Watermark Preview
                Text(
                    text = "OPTIONAL PHOTO PROOF:",
                    style = SystemLabel.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .border(1.dp, AriseTertiary, SciFiCutCornerShape())
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Workout Proof",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = AriseTertiary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "[ SYSTEM VERIFIED PROOF ]",
                                    style = SystemLabel.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AriseTertiary)
                                )
                            }
                            Text(
                                "ARISE SYSTEM",
                                style = SystemLabel.copy(fontSize = 8.sp, color = AriseOnSurfaceVariant)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, AriseOutlineVariant),
                        shape = SciFiCutCornerShape()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AriseOnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("📷 ATTACH WORKOUT PHOTO", style = SystemLabel.copy(color = AriseOnSurfaceVariant))
                    }
                }

                // Submit Button
                SystemButton(
                    text = "LOG EXERCISE PROGRESS",
                    onClick = {
                        val valToLog = (loggedValue.toIntOrNull() ?: sub.current).coerceAtMost(maxLimit)
                        viewModel.updateSubObjectiveValue(sub.id, questId, valToLog)
                        onDismiss()
                    },
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuickAddPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val borderCol = if (isLight) AriseLightOutline else AriseOutlineVariant
    val bgCol = if (isLight) Color(0xFFFFFFFF) else AriseSurfaceContainerLowest
    val textCol = if (isLight) AriseLightPrimary else ArisePrimary

    Box(
        modifier = modifier
            .border(1.dp, borderCol, SciFiCutCornerShape())
            .background(bgCol)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SystemLabel.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = textCol,
            maxLines = 1,
            softWrap = false
        )
    }
}

// ─── LOGGING QUEST SUB-OBJECTIVES ───────────────────────────────

@Composable
private fun LoggingSubObjectives(
    subs: List<SubObjectiveEntity>,
    questId: String,
    viewModel: QuestsViewModel
) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val onSurf = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val onSurfVar = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val tertiaryColor = if (isLight) AriseLightTertiary else AriseTertiary

    val calorieSub = subs.find { it.unit.uppercase() == "KCAL" || it.id.contains("calorie") } ?: subs.getOrNull(0)
    val waterSub = subs.find { it.unit.uppercase() == "ML" || it.id.contains("water") } ?: subs.getOrNull(1)

    var calorieText by remember(calorieSub?.current) {
        mutableStateOf(if ((calorieSub?.current ?: 0) > 0) calorieSub!!.current.toString() else "")
    }
    var waterText by remember(waterSub?.current) {
        mutableStateOf(if ((waterSub?.current ?: 0) > 0) waterSub!!.current.toString() else "")
    }

    var calorieMaxExceeded by remember { mutableStateOf(false) }
    var waterMaxExceeded by remember { mutableStateOf(false) }

    // Helper: digits only, strip leading zeros
    fun filterIntegerInput(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        return digitsOnly.dropWhile { it == '0' }
    }

    val calorieVal = calorieText.toIntOrNull() ?: 0
    val waterVal = waterText.toIntOrNull() ?: 0

    val MIN_CALORIES = 1500
    val MAX_CALORIES = 5000

    val MIN_WATER_ML = 3000
    val MAX_WATER_ML = 5000 // 5 Litres

    val isCalorieValid = calorieVal in MIN_CALORIES..MAX_CALORIES
    val isWaterValid = waterVal in MIN_WATER_ML..MAX_WATER_ML
    val isFormValid = isCalorieValid && isWaterValid

    val isLogged = (calorieSub?.isComplete == true && waterSub?.isComplete == true) || 
                   ((calorieSub?.current ?: 0) >= MIN_CALORIES && (waterSub?.current ?: 0) >= MIN_WATER_ML)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (isLogged) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, tertiaryColor, SciFiCutCornerShape())
                    .background(if (isLight) AriseLightPrimaryContainer.copy(alpha = 0.6f) else AriseTertiary.copy(alpha = 0.12f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = tertiaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "DATA LOGGED SUCCESSFULLY",
                    style = SystemLabel.copy(
                        fontWeight = FontWeight.Bold,
                        color = tertiaryColor,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // Calorie Input Box
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "CALORIE INTAKE (KCAL)",
                style = SystemLabel.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = onSurfVar
            )
            OutlinedTextField(
                value = calorieText,
                onValueChange = { input ->
                    if (!isLogged) {
                        val sanitized = filterIntegerInput(input)
                        val parsed = sanitized.toIntOrNull() ?: 0
                        if (parsed >= MAX_CALORIES) {
                            calorieText = MAX_CALORIES.toString()
                            calorieMaxExceeded = true
                        } else {
                            calorieText = sanitized
                            calorieMaxExceeded = false
                        }
                    }
                },
                readOnly = isLogged,
                modifier = Modifier.fillMaxWidth(),
                textStyle = AriseTypography.bodyLarge.copy(color = if (isLogged) tertiaryColor else onSurf),
                placeholder = {
                    Text(
                        text = "e.g. 2000 (Min 1500 KCAL)",
                        style = AriseTypography.bodyMedium,
                        color = onSurfVar.copy(alpha = 0.4f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                isError = (!isLogged && calorieText.isNotEmpty() && !isCalorieValid) || calorieMaxExceeded,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = if (isLogged) tertiaryColor.copy(alpha = 0.5f) else if ((calorieText.isNotEmpty() && !isCalorieValid) || calorieMaxExceeded) AriseDangerRed else (if (isLight) AriseLightOutline else AriseOutlineVariant),
                    focusedBorderColor = if (isLogged) tertiaryColor else if ((calorieText.isNotEmpty() && !isCalorieValid) || calorieMaxExceeded) AriseDangerRed else primaryColor,
                    errorBorderColor = AriseDangerRed,
                    cursorColor = primaryColor,
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when {
                    calorieMaxExceeded || calorieVal > MAX_CALORIES -> {
                        Text(
                            text = "⚠️ Max limit reached (5000 KCAL)",
                            style = SystemLabel.copy(fontSize = 10.sp, color = AriseDangerRed, fontWeight = FontWeight.Bold)
                        )
                    }
                    calorieText.isNotEmpty() && calorieVal < MIN_CALORIES -> {
                        Text(
                            text = "⚠️ Minimum 1500 KCAL required",
                            style = SystemLabel.copy(fontSize = 10.sp, color = AriseDangerRed)
                        )
                    }
                    else -> {
                        Text(
                            text = "Minimum requirement: 1500 KCAL",
                            style = SystemLabel.copy(fontSize = 9.sp, color = onSurfVar.copy(alpha = 0.6f))
                        )
                    }
                }
                if (isCalorieValid || isLogged) {
                    Text(
                        text = "✓ VALID",
                        style = SystemLabel.copy(fontSize = 9.sp, color = tertiaryColor, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Water Input Box
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "WATER INTAKE (ML)",
                style = SystemLabel.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = onSurfVar
            )
            OutlinedTextField(
                value = waterText,
                onValueChange = { input ->
                    if (!isLogged) {
                        val sanitized = filterIntegerInput(input)
                        val parsed = sanitized.toIntOrNull() ?: 0
                        if (parsed >= MAX_WATER_ML) {
                            waterText = MAX_WATER_ML.toString()
                            waterMaxExceeded = true
                        } else {
                            waterText = sanitized
                            waterMaxExceeded = false
                        }
                    }
                },
                readOnly = isLogged,
                modifier = Modifier.fillMaxWidth(),
                textStyle = AriseTypography.bodyLarge.copy(color = if (isLogged) tertiaryColor else onSurf),
                placeholder = {
                    Text(
                        text = "e.g. 3000 (Min 3000 ML / 3L)",
                        style = AriseTypography.bodyMedium,
                        color = onSurfVar.copy(alpha = 0.4f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                isError = (!isLogged && waterText.isNotEmpty() && !isWaterValid) || waterMaxExceeded,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = if (isLogged) tertiaryColor.copy(alpha = 0.5f) else if ((waterText.isNotEmpty() && !isWaterValid) || waterMaxExceeded) AriseDangerRed else (if (isLight) AriseLightOutline else AriseOutlineVariant),
                    focusedBorderColor = if (isLogged) tertiaryColor else if ((waterText.isNotEmpty() && !isWaterValid) || waterMaxExceeded) AriseDangerRed else primaryColor,
                    errorBorderColor = AriseDangerRed,
                    cursorColor = primaryColor,
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when {
                    waterMaxExceeded || waterVal > MAX_WATER_ML -> {
                        Text(
                            text = "⚠️ Max limit reached (5000 ML / 5L)",
                            style = SystemLabel.copy(fontSize = 10.sp, color = AriseDangerRed, fontWeight = FontWeight.Bold)
                        )
                    }
                    waterText.isNotEmpty() && waterVal < MIN_WATER_ML -> {
                        Text(
                            text = "⚠️ Minimum 3000 ML (3 Litres) required",
                            style = SystemLabel.copy(fontSize = 10.sp, color = AriseDangerRed)
                        )
                    }
                    else -> {
                        Text(
                            text = "Minimum requirement: 3000 ML (3 Litres)",
                            style = SystemLabel.copy(fontSize = 9.sp, color = onSurfVar.copy(alpha = 0.6f))
                        )
                    }
                }
                if (isWaterValid || isLogged) {
                    Text(
                        text = "✓ VALID",
                        style = SystemLabel.copy(fontSize = 9.sp, color = tertiaryColor, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // LOG DATA Button (enabled only when form is valid and not yet logged)
        SystemButton(
            text = if (isLogged) "✓ DATA LOGGED" else "LOG DATA",
            onClick = {
                if (isFormValid && !isLogged) {
                    calorieSub?.let { viewModel.updateSubObjectiveValue(it.id, questId, calorieVal) }
                    waterSub?.let { viewModel.updateSubObjectiveValue(it.id, questId, waterVal) }
                }
            },
            enabled = isFormValid && !isLogged,
            variant = if (isLogged) ButtonVariant.GHOST else ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
            icon = {
                Icon(
                    if (isLogged) Icons.Default.CheckCircle else Icons.Default.EditNote,
                    contentDescription = null,
                    tint = if (isLogged) tertiaryColor else if (isFormValid) primaryColor else onSurfVar.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}

// ─── HELPERS ────────────────────────────────────────────────────

@Composable
private fun DiamondDivider() {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val dividerColor = if (isLight) AriseLightOutline.copy(alpha = 0.6f) else AriseOutlineVariant
    val diamondColor = if (isLight) AriseLightPrimary.copy(alpha = 0.7f) else ArisePrimary.copy(alpha = 0.5f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
        Text(" ◆ ", color = diamondColor, fontSize = 8.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
    }
}

@Composable
private fun RewardPill(text: String) {
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
    val pillBorder = if (isLight) AriseLightOutline else AriseOutlineVariant
    val pillBg = if (isLight) AriseLightPrimaryContainer.copy(alpha = 0.5f) else Color.Transparent
    val pillText = if (isLight) AriseLightPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .background(pillBg, SciFiCutCornerShape())
            .border(1.dp, pillBorder, SciFiCutCornerShape())
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = SystemLabel.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            ),
            color = pillText
        )
    }
}

private fun formatSubValue(sub: SubObjectiveEntity): String {
    return if (sub.unit == "METERS") {
        val currentKm = sub.current / 1000f
        val targetKm = sub.target / 1000f
        String.format("%.1f / %.1f km", currentKm, targetKm)
    } else {
        "${sub.current} / ${sub.target}"
    }
}

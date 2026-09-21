package com.example.arise.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.arise.ui.components.*
import com.example.arise.ui.components.AnimatedScreenEntrance
import com.example.arise.ui.theme.*
import com.example.arise.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAlarmTimePicker by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent read permission so the URI survives app restarts
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistent permissions — URI still works for this session
            }
            viewModel.updateAvatarUri(it.toString())
        }
    }

    // Audio tone picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            var fileName = "Custom Audio Tone"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) {
            }

            viewModel.updateAlarmTone(context, it.toString(), fileName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ARISE wordmark — top-left aligned matching StatusScreen
        AnimatedScreenEntrance(delayMillis = 0) {
            GlitchText(
                text = "ARISE",
                style = DisplayRank.copy(fontSize = 22.sp, letterSpacing = 4.sp),
                baseColor = ArisePrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Profile Section
        AnimatedScreenEntrance(delayMillis = 80) {
            GlitchEntryAnimation(index = 0) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar — tap to pick photo
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AriseSurfaceContainer)
                                .border(2.dp, ArisePrimary.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.avatarUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(state.avatarUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = ArisePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            // Camera badge overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(ArisePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = AriseBackground,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showEditNameDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = state.username,
                                    style = AriseTypography.titleMedium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "SYSTEM ACCESS: GRANTED",
                                style = SystemLabel.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = AriseTertiary
                            )
                        }
                    }
                }
            }
        }

        // Preferences
        AnimatedScreenEntrance(delayMillis = 160) {
            GlitchEntryAnimation(index = 1) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("PREFERENCES", style = SystemLabel, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))

                        SettingsRow(
                            label = "Language",
                            value = state.language,
                            icon = Icons.Default.Language,
                            showChevron = true,
                            onClick = { showLanguageDialog = true }
                        )
                        HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.3f))
                        SettingsRow(
                            label = "Notification Settings",
                            icon = Icons.Default.Notifications,
                            showChevron = true,
                            onClick = { showNotificationDialog = true }
                        )
                        HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Light Neumorphic Theme", style = AriseTypography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = if (state.isLightThemeEnabled) "Soft Mint & Peach active" else "Dark Monarch System (Primary)",
                                        style = SystemLabel.copy(fontSize = 9.sp),
                                        color = if (state.isLightThemeEnabled) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = state.isLightThemeEnabled,
                                onCheckedChange = { viewModel.toggleLightTheme(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    uncheckedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // System Operations
        AnimatedScreenEntrance(delayMillis = 240) {
            GlitchEntryAnimation(index = 2) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("SYSTEM OPERATIONS", style = SystemLabel, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text("System Reminders", style = AriseTypography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                            }
                            Switch(
                                checked = state.systemReminders,
                                onCheckedChange = { viewModel.toggleReminders(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    uncheckedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        }

                        HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.3f))

                        // Daily Alarm
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAlarmTimePicker = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Daily Alarm", style = AriseTypography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = if (state.alarmEnabled) "Scheduled for ${state.alarmTimeDisplay}" else "Tap to set daily alarm time",
                                        fontSize = 10.sp,
                                        color = if (state.alarmEnabled) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (state.alarmEnabled) state.alarmTimeDisplay else "OFF",
                                    style = SystemLabel,
                                    color = if (state.alarmEnabled) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Switch(
                                    checked = state.alarmEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            showAlarmTimePicker = true
                                        } else {
                                            viewModel.toggleAlarm(context, false)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        uncheckedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        uncheckedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.3f))

                        SettingsRow(
                            label = "Privacy & Security",
                            icon = Icons.Default.Security,
                            showChevron = true,
                            onClick = { showPrivacyDialog = true }
                        )
                    }
                }
            }
        }

        // Support
        AnimatedScreenEntrance(delayMillis = 320) {
            GlitchEntryAnimation(index = 3) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("SUPPORT", style = SystemLabel, color = AriseOnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))

                        SettingsRow(
                            label = "Report Issue",
                            icon = Icons.Default.BugReport,
                            showChevron = true,
                            tintColor = AriseDangerRed,
                            onClick = { showReportDialog = true }
                        )
                        HorizontalDivider(color = AriseOutlineVariant.copy(alpha = 0.3f))
                        SettingsRow(
                            label = "About The System",
                            value = state.appVersion,
                            icon = Icons.Default.Info,
                            showChevron = true,
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
        }

        // Log Out Button
        AnimatedScreenEntrance(delayMillis = 400) {
            SystemButton(
                text = "LOG OUT",
                onClick = { showLogoutDialog = true },
                variant = ButtonVariant.DANGER,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(80.dp))
    }

    // --- Interactive Dialogs ---
    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.isLight
    val dialogSurfaceColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFF090B10)
    val dialogTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val dialogMutedColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    // 1. Edit Name Dialog
    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(state.username) }
        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "EDIT HUNTER CODENAME",
                        style = SystemLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ArisePrimary
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        label = { Text("Hunter Codename", color = dialogMutedColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArisePrimary,
                            unfocusedBorderColor = if (isLight) AriseLightOutline else Color(0xFF334155),
                            focusedTextColor = dialogTextColor,
                            unfocusedTextColor = dialogTextColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditNameDialog = false }) {
                            Text("CANCEL", color = dialogMutedColor)
                        }
                        Spacer(Modifier.width(8.dp))
                        SystemButton(
                            text = "SAVE",
                            onClick = {
                                viewModel.updateUsername(tempName)
                                showEditNameDialog = false
                            },
                            variant = ButtonVariant.PRIMARY
                        )
                    }
                }
            }
        }
    }

    // 2. Language Dialog
    if (showLanguageDialog) {
        val languages = listOf("English (US)", "Korean (한국어)", "Japanese (日本語)", "Spanish (Español)")
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SELECT INTERFACE LANGUAGE",
                        style = SystemLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ArisePrimary
                    )
                    languages.forEach { lang ->
                        val isSelected = state.language == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ArisePrimary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, color = if (isSelected) ArisePrimary else dialogTextColor, style = AriseTypography.bodyMedium)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ArisePrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Notification Settings Dialog
    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "NOTIFICATION PROTOCOLS",
                        style = SystemLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ArisePrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Quest Reminders", color = dialogTextColor, style = AriseTypography.bodyMedium)
                        Switch(
                            checked = state.systemReminders,
                            onCheckedChange = { viewModel.toggleReminders(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Feedback Effects", color = dialogTextColor, style = AriseTypography.bodyMedium)
                        Switch(
                            checked = state.soundEffectsEnabled,
                            onCheckedChange = { viewModel.toggleSound(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Vibration Feedback", color = dialogTextColor, style = AriseTypography.bodyMedium)
                        Switch(
                            checked = state.vibrationEnabled,
                            onCheckedChange = { viewModel.toggleVibration(it) }
                        )
                    }
                    SystemButton(
                        text = "CLOSE",
                        onClick = { showNotificationDialog = false },
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // 4. Privacy & Security Dialog
    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PRIVACY & SECURITY INTEGRITY",
                        style = SystemLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ArisePrimary
                    )
                    Text(
                        text = "• All Hunter progress, attributes, and quest logs are encrypted and stored locally in Room SQL DB.\n" +
                                "• System access token: ACTIVE.\n" +
                                "• No telemetry or private user data is transmitted without user consent.",
                        style = AriseTypography.bodyMedium,
                        color = dialogTextColor
                    )
                    SystemButton(
                        text = "ACKNOWLEDGE",
                        onClick = { showPrivacyDialog = false },
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // 5. Report Issue Dialog
    if (showReportDialog) {
        var category by remember { mutableStateOf("Bug Report") }
        var detailsText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showReportDialog = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, AriseDangerRed.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "REPORT SYSTEM ISSUE / FEEDBACK",
                        style = SystemLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = AriseDangerRed
                    )

                    Text("Select Category", style = SystemLabel, color = dialogMutedColor)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Bug Report", "Feature", "Other").forEach { cat ->
                            val isSel = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) AriseDangerRed.copy(alpha = 0.2f) else if (isLight) AriseLightOutline.copy(alpha = 0.1f) else Color(0xFF1E293B))
                                    .border(0.8.dp, if (isSel) AriseDangerRed else if (isLight) AriseLightOutline else Color(0xFF334155), RoundedCornerShape(4.dp))
                                    .clickable { category = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) AriseDangerRed else dialogTextColor
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = detailsText,
                        onValueChange = { detailsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Describe the issue or system feedback...", color = dialogMutedColor.copy(alpha = 0.6f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AriseDangerRed,
                            unfocusedBorderColor = if (isLight) AriseLightOutline else Color(0xFF334155),
                            focusedTextColor = dialogTextColor,
                            unfocusedTextColor = dialogTextColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("CANCEL", color = dialogMutedColor)
                        }
                        Spacer(Modifier.width(8.dp))
                        SystemButton(
                            text = "SUBMIT REPORT",
                            onClick = {
                                if (detailsText.isNotBlank()) {
                                    viewModel.submitReport(category, detailsText)
                                    showReportDialog = false
                                }
                            },
                            variant = ButtonVariant.DANGER
                        )
                    }
                }
            }
        }
    }

    // Report Submitted Dialog Notice
    state.reportSubmittedMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearReportMessage() },
            containerColor = dialogSurfaceColor,
            title = {
                Text("SYSTEM REPORT QUEUED", style = SystemLabel.copy(fontSize = 13.sp, color = ArisePrimary))
            },
            text = {
                Text(msg, color = dialogTextColor, style = AriseTypography.bodyMedium)
            },
            confirmButton = {
                SystemButton(
                    text = "OK",
                    onClick = { viewModel.clearReportMessage() },
                    variant = ButtonVariant.PRIMARY
                )
            }
        )
    }

    // 6. About System Dialog
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ABOUT THE SYSTEM",
                        style = SystemLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ArisePrimary
                    )
                    Text(
                        text = "ARISE — Solo Leveling Habit & Quest System\n" +
                                "Version: ${state.appVersion}\n" +
                                "Architecture: Android Jetpack Compose + Hilt + Room DB\n" +
                                "Status: MONARCH PROTOCOL ACTIVE",
                        style = AriseTypography.bodyMedium,
                        color = dialogTextColor
                    )
                    SystemButton(
                        text = "CLOSE",
                        onClick = { showAboutDialog = false },
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // 7. Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = dialogSurfaceColor,
            title = {
                Text("SYSTEM DEACTIVATION", style = SystemLabel.copy(fontSize = 14.sp, color = AriseDangerRed))
            },
            text = {
                Text(
                    "Are you sure you want to log out / deactivate System access? Your local Hunter progress will remain saved in database.",
                    color = dialogTextColor,
                    style = AriseTypography.bodyMedium
                )
            },
            confirmButton = {
                SystemButton(
                    text = "DEACTIVATE",
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    variant = ButtonVariant.DANGER
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("CANCEL", color = dialogMutedColor)
                }
            }
        )
    }

    // 8. TimePicker Dialog for Daily Training Alarm
    if (showAlarmTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.alarmHour,
            initialMinute = state.alarmMinute
        )
        Dialog(onDismissRequest = { showAlarmTimePicker = false }) {
            Surface(
                color = dialogSurfaceColor,
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, ArisePrimary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SET DAILY ALARM TIME",
                        style = SystemLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ArisePrimary
                    )
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFF0F172A),
                            selectorColor = ArisePrimary,
                            containerColor = dialogSurfaceColor,
                            periodSelectorBorderColor = ArisePrimary,
                            periodSelectorSelectedContainerColor = ArisePrimary.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = ArisePrimary,
                            periodSelectorUnselectedContentColor = dialogTextColor,
                            timeSelectorSelectedContainerColor = ArisePrimary.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = ArisePrimary,
                            timeSelectorUnselectedContainerColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFF1E293B),
                            timeSelectorUnselectedContentColor = dialogTextColor
                        )
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Row: SELECT RINGTONE (Left) & CANCEL (Right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { audioPickerLauncher.launch("audio/*") }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = ArisePrimary, modifier = Modifier.size(16.dp))
                                    Text("SELECT RINGTONE", color = ArisePrimary, style = SystemLabel.copy(fontSize = 11.sp))
                                }
                            }

                            TextButton(onClick = { showAlarmTimePicker = false }) {
                                Text("CANCEL", color = Color(0xFF94A3B8))
                            }
                        }

                        // Bottom Row: Comfortable full-width SET ALARM button
                        SystemButton(
                            text = "SET ALARM",
                            onClick = {
                                viewModel.setAlarmTime(context, timePickerState.hour, timePickerState.minute)
                                showAlarmTimePicker = false
                            },
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showChevron: Boolean = false,
    tintColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit = {}
) {
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val onSurf = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val onSurfVar = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    val effectiveTint = tintColor ?: primaryColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = effectiveTint, modifier = Modifier.size(20.dp))
            Text(label, style = AriseTypography.bodyMedium, color = onSurf)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (value != null) {
                Text(value, style = SystemLabel, color = onSurfVar)
            }
            if (showChevron) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = onSurfVar, modifier = Modifier.size(20.dp))
            }
        }
    }
}

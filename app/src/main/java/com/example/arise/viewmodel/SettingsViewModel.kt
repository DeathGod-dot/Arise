package com.example.arise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arise.data.AriseRepository
import com.example.arise.domain.LogIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.arise.service.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext

data class SettingsUiState(
    val username: String = "HUNTER",
    val avatarUri: String? = null,
    val language: String = "English (US)",
    val isLightThemeEnabled: Boolean = false,
    val systemReminders: Boolean = true,
    val alarmEnabled: Boolean = false,
    val alarmTimeDisplay: String = "08:00 AM",
    val alarmHour: Int = 8,
    val alarmMinute: Int = 0,
    val alarmAudioUri: String? = null,
    val alarmToneName: String = "Default System Tone",
    val soundEffectsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val appVersion: String = "v2.4.9",
    val reportSubmittedMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AriseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("arise_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val appVersionStr = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "v2.4.9"
        } catch (e: Exception) {
            "v2.4.9"
        }
        
        _uiState.update {
            it.copy(
                isLightThemeEnabled = prefs.getBoolean("isLightThemeEnabled", false),
                systemReminders = prefs.getBoolean("systemReminders", true),
                alarmEnabled = prefs.getBoolean("alarmEnabled", false),
                alarmHour = prefs.getInt("alarmHour", 8),
                alarmMinute = prefs.getInt("alarmMinute", 0),
                soundEffectsEnabled = prefs.getBoolean("soundEffectsEnabled", true),
                vibrationEnabled = prefs.getBoolean("vibrationEnabled", true),
                language = prefs.getString("language", "English (US)") ?: "English (US)",
                appVersion = appVersionStr ?: "v1.0.0"
            )
        }

        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                val p = profile ?: return@collect
                _uiState.update {
                    it.copy(
                        username = p.username,
                        avatarUri = p.avatarUri,
                        alarmAudioUri = p.alarmAudioUri,
                        alarmToneName = p.alarmToneName ?: "Default System Tone"
                    )
                }
            }
        }
    }

    fun updateUsername(name: String) {
        if (name.isBlank()) return
        val trimmed = name.trim()
        viewModelScope.launch {
            val profile = repository.getProfileOnce() ?: return@launch
            val updated = profile.copy(username = trimmed)
            repository.upsertProfile(updated)

            // Sync with Firebase User Display Name so it persists across sessions
            try {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(trimmed)
                        .build()
                    firebaseUser.updateProfile(profileUpdates)
                }
            } catch (_: Exception) {
                // Local DB update is primary, ignoring network edge case
            }

            repository.logEvent(LogIcon.INFO, "System Profile Updated: Hunter codename set to '$trimmed'.")
        }
    }

    fun toggleReminders(enabled: Boolean) {
        prefs.edit().putBoolean("systemReminders", enabled).apply()
        _uiState.update { it.copy(systemReminders = enabled) }
        viewModelScope.launch {
            val status = if (enabled) "ACTIVATED" else "DEACTIVATED"
            repository.logEvent(LogIcon.INFO, "System Reminders $status.")
        }
    }

    fun toggleLightTheme(enabled: Boolean) {
        prefs.edit().putBoolean("isLightThemeEnabled", enabled).apply()
        _uiState.update { it.copy(isLightThemeEnabled = enabled) }
        viewModelScope.launch {
            val modeName = if (enabled) "LIGHT NEUMORPHIC" else "DARK MONARCH"
            repository.logEvent(LogIcon.INFO, "System Theme switched to $modeName Mode.")
        }
    }

    fun toggleAlarm(context: Context, enabled: Boolean) {
        prefs.edit().putBoolean("alarmEnabled", enabled).apply()
        _uiState.update { it.copy(alarmEnabled = enabled) }
        if (enabled) {
            val h = _uiState.value.alarmHour
            val m = _uiState.value.alarmMinute
            val uri = _uiState.value.alarmAudioUri
            AlarmScheduler.scheduleDailyAlarm(context, h, m, uri)
            viewModelScope.launch {
                repository.logEvent(LogIcon.INFO, "System Alarm Activated for ${_uiState.value.alarmTimeDisplay}.")
            }
        } else {
            AlarmScheduler.cancelDailyAlarm(context)
            viewModelScope.launch {
                repository.logEvent(LogIcon.INFO, "System Alarm Deactivated.")
            }
        }
    }

    fun setAlarmTime(context: Context, hour: Int, minute: Int) {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when (val h = hour % 12) {
            0 -> 12
            else -> h
        }
        val formattedTime = String.format("%02d:%02d %s", displayHour, minute, amPm)
        val uri = _uiState.value.alarmAudioUri
        prefs.edit()
            .putBoolean("alarmEnabled", true)
            .putInt("alarmHour", hour)
            .putInt("alarmMinute", minute)
            .apply()
        _uiState.update {
            it.copy(
                alarmEnabled = true,
                alarmTimeDisplay = formattedTime,
                alarmHour = hour,
                alarmMinute = minute
            )
        }
        AlarmScheduler.scheduleDailyAlarm(context, hour, minute, uri)
        viewModelScope.launch {
            repository.logEvent(LogIcon.INFO, "System Alarm Scheduled: Set to $formattedTime daily.")
        }
    }

    fun updateAlarmTone(context: Context, uriString: String, toneName: String) {
        _uiState.update {
            it.copy(
                alarmAudioUri = uriString,
                alarmToneName = toneName
            )
        }
        viewModelScope.launch {
            val profile = repository.getProfileOnce() ?: return@launch
            repository.upsertProfile(profile.copy(alarmAudioUri = uriString, alarmToneName = toneName))

            if (_uiState.value.alarmEnabled) {
                val h = _uiState.value.alarmHour
                val m = _uiState.value.alarmMinute
                AlarmScheduler.scheduleDailyAlarm(context, h, m, uriString)
            }
            repository.logEvent(LogIcon.INFO, "System Alarm Tone set to '$toneName'.")
        }
    }

    fun toggleSound(enabled: Boolean) {
        prefs.edit().putBoolean("soundEffectsEnabled", enabled).apply()
        _uiState.update { it.copy(soundEffectsEnabled = enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        prefs.edit().putBoolean("vibrationEnabled", enabled).apply()
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _uiState.update { it.copy(language = lang) }
        viewModelScope.launch {
            repository.logEvent(LogIcon.INFO, "System Interface Language set to '$lang'.")
        }
    }

    fun submitReport(category: String, details: String) {
        viewModelScope.launch {
            val reportId = "R-${System.currentTimeMillis() % 100000}"
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid ?: "anonymous"
            val email = currentUser?.email ?: "anonymous@arise.app"
            val developerEmail = "support@arise.app"

            val reportData = hashMapOf(
                "reportId" to reportId,
                "userId" to uid,
                "userEmail" to email,
                "to" to developerEmail,
                "category" to category,
                "details" to details,
                "appVersion" to _uiState.value.appVersion,
                "deviceModel" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                "androidVersion" to android.os.Build.VERSION.RELEASE,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "message" to hashMapOf(
                    "subject" to "[ARISE REPORT #$reportId] - $category",
                    "text" to """
                        SYSTEM REPORT #$reportId
                        ------------------------------------
                        Reporter Email: $email
                        User ID: $uid
                        Category: $category
                        Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                        Android OS: ${android.os.Build.VERSION.RELEASE}
                        App Version: ${_uiState.value.appVersion}
                        
                        ISSUE DETAILS:
                        $details
                        ------------------------------------
                        Transmitted via ARISE Solo Leveling System
                    """.trimIndent()
                )
            )

            // Submit background document directly to Cloud Firestore system_reports & mail collections
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("system_reports").document(reportId).set(reportData)
                db.collection("mail").add(reportData)
                
                repository.logEvent(
                    LogIcon.INFO,
                    "Report Logged [#$reportId]: Category: '$category' | Target: $developerEmail"
                )
                _uiState.update {
                    it.copy(reportSubmittedMessage = "Report #$reportId transmitted directly to Cloud Databases for $developerEmail.")
                }
            } catch (e: Exception) {
                repository.logEvent(
                    LogIcon.INFO,
                    "Report [#$reportId] saved locally. Cloud sync failed: ${e.message}"
                )
                _uiState.update {
                    it.copy(reportSubmittedMessage = "Report #$reportId saved locally. Cloud sync pending.")
                }
            }
        }
    }

    fun clearReportMessage() {
        _uiState.update { it.copy(reportSubmittedMessage = null) }
    }

    fun updateAvatarUri(uri: String) {
        viewModelScope.launch {
            val profile = repository.getProfileOnce() ?: return@launch
            repository.upsertProfile(profile.copy(avatarUri = uri))
            repository.logEvent(LogIcon.INFO, "Hunter Profile Avatar updated successfully.")
        }
    }
}

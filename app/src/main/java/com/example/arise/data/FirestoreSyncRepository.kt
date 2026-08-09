package com.example.arise.data

import com.example.arise.domain.HunterRank
import com.example.arise.domain.LogIcon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val ariseRepository: AriseRepository
) {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val scope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private var syncJob: kotlinx.coroutines.Job? = null
    private var lastPushedProfile: HunterProfile? = null

    init {
        // Observe auth state changes and trigger sync
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                scope.launch {
                    syncOnLogin(user.uid)
                }
            }
        }
    }

    private suspend fun syncOnLogin(uid: String) {
        try {
            val userDocRef = firestore.collection("users").document(uid).collection("profile").document("hunter_profile")
            val snapshot = userDocRef.get().await()

            if (snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val local = ariseRepository.getProfileOnce() ?: HunterProfile()
                    val remoteProfile = local.copy(
                        username = data["username"] as? String ?: local.username,
                        rank = try { HunterRank.valueOf(data["rank"] as? String ?: local.rank.name) } catch (_: Exception) { local.rank },
                        level = (data["level"] as? Long)?.toInt() ?: local.level,
                        hp = (data["hp"] as? Long)?.toInt() ?: local.hp,
                        maxHp = (data["maxHp"] as? Long)?.toInt() ?: local.maxHp,
                        mp = (data["mp"] as? Long)?.toInt() ?: local.mp,
                        maxMp = (data["maxMp"] as? Long)?.toInt() ?: local.maxMp,
                        xp = (data["xp"] as? Long)?.toInt() ?: local.xp,
                        xpToNextLevel = (data["xpToNextLevel"] as? Long)?.toInt() ?: local.xpToNextLevel,
                        totalXp = data["totalXp"] as? Long ?: local.totalXp,
                        str = (data["str"] as? Long)?.toInt() ?: local.str,
                        agi = (data["agi"] as? Long)?.toInt() ?: local.agi,
                        sen = (data["sen"] as? Long)?.toInt() ?: local.sen,
                        vit = (data["vit"] as? Long)?.toInt() ?: local.vit,
                        int_stat = (data["int_stat"] as? Long)?.toInt() ?: local.int_stat,
                        streakDays = (data["streakDays"] as? Long)?.toInt() ?: local.streakDays,
                        avatarUri = data["avatarUri"] as? String ?: local.avatarUri
                    )
                    lastPushedProfile = remoteProfile
                    ariseRepository.upsertProfile(remoteProfile)
                    ariseRepository.logEvent(LogIcon.INFO, "Cloud Progress Restored: Profile synced from Firestore.")
                }
            } else {
                // First time user — push local profile to Firestore
                val local = ariseRepository.getProfileOnce()
                if (local != null) {
                    pushProfileToCloud(uid, local)
                }
            }

            // Listen to ongoing profile changes in Room and push to Firestore
            observeLocalProfileAndSync(uid)
        } catch (e: Exception) {
            ariseRepository.logEvent(LogIcon.WARNING, "Cloud Sync Warning: ${e.localizedMessage}")
        }
    }

    private fun observeLocalProfileAndSync(uid: String) {
        syncJob?.cancel()
        syncJob = scope.launch {
            ariseRepository.getProfile().collectLatest { profile ->
                if (profile != null && auth.currentUser?.uid == uid && profile != lastPushedProfile) {
                    pushProfileToCloud(uid, profile)
                }
            }
        }
    }

    suspend fun pushProfileToCloud(uid: String, profile: HunterProfile) {
        lastPushedProfile = profile
        try {
            val userDocRef = firestore.collection("users").document(uid).collection("profile").document("hunter_profile")
            val map = mapOf(
                "id" to profile.id,
                "username" to profile.username,
                "rank" to profile.rank.name,
                "level" to profile.level,
                "hp" to profile.hp,
                "maxHp" to profile.maxHp,
                "mp" to profile.mp,
                "maxMp" to profile.maxMp,
                "xp" to profile.xp,
                "xpToNextLevel" to profile.xpToNextLevel,
                "totalXp" to profile.totalXp,
                "str" to profile.str,
                "agi" to profile.agi,
                "sen" to profile.sen,
                "vit" to profile.vit,
                "int_stat" to profile.int_stat,
                "streakDays" to profile.streakDays,
                "lastQuestDate" to profile.lastQuestDate,
                "avatarUri" to (profile.avatarUri ?: ""),
                "lastSyncedAt" to System.currentTimeMillis()
            )
            userDocRef.set(map, SetOptions.merge()).await()
        } catch (_: Exception) {
            // Ignore offline network errors
        }
    }
}

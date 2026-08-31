package com.example.arise.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AriseFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "SYSTEM NOTIFICATION"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "New alert received from System."

        NotificationHelper.showSystemNotification(
            context = applicationContext,
            title = title,
            message = body
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToCloud(token)
    }

    private fun saveTokenToCloud(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            userRef.update("fcmToken", token)
        } catch (_: Exception) {
        }
    }
}

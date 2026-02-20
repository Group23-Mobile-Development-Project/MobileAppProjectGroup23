package com.example.eventplanner.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.eventplanner.utils.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCMService", "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d("FCMService", "Message Notification Title: ${it.title}")
            Log.d("FCMService", "Message Notification Body: ${it.body}")

            NotificationHelper.showNotification(
                this,
                it.title ?: "Event Update",
                it.body ?: "Your event status has been updated"
            )
        }

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCMService", "Message data payload: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: "Event Update"
            val message = remoteMessage.data["message"] ?: "Your event status has been updated"

            NotificationHelper.showNotification(this, title, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Refreshed token: $token")

        // You can send the token to your server here if needed
        // sendRegistrationToServer(token)
    }
}
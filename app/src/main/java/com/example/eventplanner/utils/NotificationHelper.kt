package com.example.eventplanner.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.eventplanner.R

object NotificationHelper {
    private const val CHANNEL_ID = "event_notifications"
    private const val CHANNEL_NAME = "Event Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for event bookings and updates"
    private var notificationId = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Notification channel created successfully")
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Error creating notification channel", e)
            }
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        try {
            // Ensure channel exists for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Increment notification ID to show multiple notifications
            notificationId++

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
                .build()

            notificationManager.notify(notificationId, notification)
            Log.d("NotificationHelper", "Notification shown: $title - $message")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error showing notification", e)
        }
    }
}
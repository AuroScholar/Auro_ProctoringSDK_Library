package com.example.auroproctoringsdk.notification

import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

fun ClearAllNotifications(activity: AppCompatActivity) {
    // Get the notification manager instance
    val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Cancel all pending notifications
    notificationManager.cancelAll()
}

package com.example.auroproctoringsdk.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

fun improvePendingNotificationCount(activity: AppCompatActivity) {
    // Get the notification manager instance
    val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Get the list of active notifications
    val activeNotifications = notificationManager.activeNotifications

    // Count the number of pending notifications
    var pendingNotificationCount = 0
    for (notification in activeNotifications) {
        if (notification.isAppGroup) {
            // Skip group summary notifications
            continue
        }
        if (notification.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            // Skip notifications that are part of a group summary
            continue
        }
        pendingNotificationCount++
    }

    // Print the pending notification count
    println("Pending Notification Count: $pendingNotificationCount")
}

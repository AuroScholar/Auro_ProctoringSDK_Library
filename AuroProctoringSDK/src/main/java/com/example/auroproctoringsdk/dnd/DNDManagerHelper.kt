package com.example.auroproctoringsdk.dnd

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

class DNDManagerHelper(private val context: Context) {

    private val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun checkDNDPolicyAccessAndRequest() {
        if (!mNotificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }
    }

}
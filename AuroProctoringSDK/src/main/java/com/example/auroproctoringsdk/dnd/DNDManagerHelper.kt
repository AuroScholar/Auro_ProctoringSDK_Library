package com.example.auroproctoringsdk.dnd

import android.R
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.startActivity

class DNDManagerHelper(private val context: Context) {

//    ACCESS_NOTIFICATION_POLICY

    fun checkDNDPolicyAccessAndRequest() {

        val mode = Settings.Global.getInt(context.contentResolver, "zen_mode")
        if (mode==0){
            try {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    context.startActivity(intent)
                } else {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }else{
            Log.e("TAG", "checkDNDPolicyAccessAndRequest: error print toast", )
        }


    }

    fun turnOffDndMode(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }


}
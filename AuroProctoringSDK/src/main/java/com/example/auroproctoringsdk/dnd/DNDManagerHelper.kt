package com.example.auroproctoringsdk.dnd

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.auroproctoringsdk.detector.AlertDialogHelper

class DNDManagerHelper(private val context: Context) {

    //    ACCESS_NOTIFICATION_POLICY
    val alertDialogHelper = AlertDialogHelper(context)

    fun checkDNDModeON() {
        enableDoNotDisturb(context)
        /* val mode = Settings.Global.getInt(context.contentResolver, "zen_mode")
         if (mode==0){
             try {
                 val notificationManager =
                     context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                 if (!notificationManager.isNotificationPolicyAccessGranted) {
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
 //            Log.e("TAG", "checkDNDPolicyAccessAndRequest: error print toast", )
         }*/
    }


    fun checkDndPermission(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun enableDoNotDisturb(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
//                gotoPermissionWithAlert(context)
                alertDialogHelper.showAlertDialog()
            } else {
                alertDialogHelper.hideAlertDialog()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                }
            }
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    fun dndAlertDialogue(){
        alertDialogHelper.hideAlertDialog()
    }

    private fun gotoPermissionWithAlert(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage("To enable Do Not Disturb mode, you need to grant permission.")
            .setPositiveButton("Grant Permission") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()
        alertDialog.show()
    }


    /*fun enableDoNotDisturb(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                // Request permission to access Do Not Disturb settings
                // This will open the system settings for the user to grant permission
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            } else {
                // Enable Do Not Disturb mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                }
            }
        } else {
            // Enable Do Not Disturb mode
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }*/

    fun DndModeOff(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE || notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }


}
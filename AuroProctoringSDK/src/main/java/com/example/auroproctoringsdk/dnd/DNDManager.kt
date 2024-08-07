package com.example.auroproctoringsdk.dnd

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.auroproctoringsdk.R
import org.jetbrains.annotations.TestOnly


class DNDManager(private val context: Context) {

    fun checkDNDModeON() {
        enableDoNotDisturb(context)
    }


    fun checkDndPermission(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            notificationManager.isNotificationPolicyAccessGranted
        }
    }
    //dnd done android version 13,14,15

    fun enableDoNotDisturb(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                showAlertDialog()
            } else {
                hideAlertDialog()

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

    fun dndAlertDialogHide() {
        hideAlertDialog()
    }

    // new code
    fun DndModeOff(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (checkDndPermission()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    showAlertDialog()
                } else {
                    hideAlertDialog()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 13
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 14
                        // Adjustments may be needed based on API changes in Android S
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                }
            } else {
                // For versions below Android M, simply set interruption filter to none
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }

    }

    //old code
    fun DndModeOffm(context: Context) {
        /*if (checkDndPermission()){
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE || notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }*/
    }


    var positiveButtonClicked = false

    private val alertDialog: AlertDialog by lazy {
        AlertDialog.Builder(context).setTitle(context.getString(R.string.permission_required))
            .setCancelable(false)
            .setMessage(R.string.to_enable_do_not_disturb_mode_you_need_to_grant_permission)
            .setPositiveButton(R.string.goto_permission) { dialog, _ ->
                if (!positiveButtonClicked) {
                    positiveButtonClicked = true
                    if (!DNDManager(context).checkDndPermission()) {
                        dialog.dismiss()
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    } else {
                        hideAlertDialog()
                    }
                }
            }.create()
    }

    fun checkAndHideAlertDialog(context: Context) {
        if (DNDManager(context).checkDndPermission()) {
            hideAlertDialog()
        }
    }

    fun showAlertDialog() {
        if (context is Activity && !context.isFinishing && !DNDManager(context).checkDndPermission() && !positiveButtonClicked) {
            if (!alertDialog.isShowing) {
                alertDialog.show()
            }
        }


    }

    fun hideAlertDialog() {
        if (context is Activity && !context.isFinishing && alertDialog.isShowing) {
            alertDialog.dismiss()
            alertDialog.hide()
        }
    }

}
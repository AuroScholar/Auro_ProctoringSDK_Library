package com.example.auroproctoringsdk.detector

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.auroproctoringsdk.R
import com.example.auroproctoringsdk.dnd.DNDManagerHelper

class DndPermissionHelper(private val context: Context) {
    var alertDialog: AlertDialog? = null
    var positiveButtonClicked = false

    fun showAlertDialog() {
        if (!DNDManagerHelper(context).checkDndPermission() && !positiveButtonClicked) {
            if (alertDialog == null) {
                createAlertDialog()
            }
            alertDialog?.show()
        }
    }

    fun hideAlertDialog() {
        if (alertDialog?.isShowing == true) {
            alertDialog?.hide()
            alertDialog?.dismiss()
        }
    }
    private fun createAlertDialog() {
        alertDialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.permission_required))
            .setMessage(R.string.to_enable_do_not_disturb_mode_you_need_to_grant_permission)
            .setPositiveButton(R.string.goto_permission) { dialog, _ ->
                if (!positiveButtonClicked) {
                    positiveButtonClicked = true
                    if (!DNDManagerHelper(context).checkDndPermission()) {
                        dialog.dismiss()
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    } else {
                        hideAlertDialog()
                    }
                }
            }
            .create()
    }
}
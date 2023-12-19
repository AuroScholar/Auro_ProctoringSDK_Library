package com.example.auroproctoringsdk.detector

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.auroproctoringsdk.R
import com.example.auroproctoringsdk.dnd.DNDManagerHelper

class DndPermissionHelper(private val context: Context) {
     val alertDialog: AlertDialog = AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.permission_required))
        .setMessage(R.string.to_enable_do_not_disturb_mode_you_need_to_grant_permission)
        .setPositiveButton(R.string.goto_permission) { _, _ ->
            if (!DNDManagerHelper(context).checkDndPermission()){
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }else{
                hideAlertDialog()
            }


        }
//        .setNegativeButton("Cancel", null)
        .create()

    fun showAlertDialog() {
        alertDialog.show()
    }

    fun hideAlertDialog() {
        alertDialog.hide()
        alertDialog.dismiss()
    }
}

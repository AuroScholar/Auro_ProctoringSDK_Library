package com.example.auroproctoringsdk.detector

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.example.auroproctoringsdk.dnd.DNDManagerHelper

class AlertDialogHelper(private val context: Context) {
     val alertDialog: AlertDialog = AlertDialog.Builder(context)
        .setTitle("Permission Required")
        .setMessage("To enable Do Not Disturb mode, you need to grant permission.")
        .setPositiveButton("Grant Permission") { _, _ ->
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

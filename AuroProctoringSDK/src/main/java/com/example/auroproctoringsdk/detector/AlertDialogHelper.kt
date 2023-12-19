package com.example.auroproctoringsdk.detector

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class AlertDialogHelper(private val context: Context) {
    private val alertDialog: AlertDialog = AlertDialog.Builder(context)
        .setTitle("Permission Required")
        .setMessage("To enable Do Not Disturb mode, you need to grant permission.")
        .setPositiveButton("Grant Permission") { _, _ ->
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }
        .setNegativeButton("Cancel", null)
        .create()

    fun showAlertDialog() {
        alertDialog.show()
    }

    fun hideAlertDialog() {
        alertDialog.hide()
        alertDialog.dismiss()
    }
}

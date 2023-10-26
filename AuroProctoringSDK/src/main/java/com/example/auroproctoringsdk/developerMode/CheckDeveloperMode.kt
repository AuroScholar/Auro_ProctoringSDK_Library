package com.example.auroproctoringsdk.developerMode

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class CheckDeveloperMode(val context: Context) {

    fun isDeveloperModeEnabled(): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }

    fun turnOffDeveloperMode( developerModeEnabled: Boolean) {
        if (developerModeEnabled) {
            try {
                AlertDialog.Builder(context)
                    .setTitle("Please Disable Developer Mode")
                    .setMessage("You will not be able to proceed if developer mode is enabled")
                    .setPositiveButton(
                        "Go to Settings",
                        DialogInterface.OnClickListener { dialog, which ->
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            )
                        }).setIcon(android.R.drawable.stat_notify_error)
                    .setCancelable(false).show()
                Settings.Secure.putInt(
                    context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
                )
            } catch (e: SecurityException) {
                // Handle the security exception if necessary
            }
        }
    }
}

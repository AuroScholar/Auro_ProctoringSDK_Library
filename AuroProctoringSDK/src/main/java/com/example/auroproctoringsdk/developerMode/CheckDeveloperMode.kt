package com.example.auroproctoringsdk.developerMode

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class CheckDeveloperMode(val context: Context) {

    fun disableDeveloperMode() {
        try {
            // Disable developer mode
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enableDeveloperMode() {
        try {
            // Enable developer mode
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                1
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkDeveloperMode(): Boolean {
        val developerModeEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1
        return if (developerModeEnabled) {
            disableDeveloperMode()
            true
        } else {
            false
        }
    }

    /*if (developerModeEnabled) {
        // Developer mode is enabled
        // Perform your actions here
    } else {
        // Developer mode is disabled
        // Perform your actions here
    }*/

}

package com.example.auroproctoringsdk.developerMode

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CheckDeveloperMode(val context: Context) {
    private var dialog: AlertDialog? = null
    fun isDeveloperModeEnabled(): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }

    fun turnOffDeveloperMode() {
        Log.e("TAG", "turnOffDeveloperMode: developer status " + isDeveloperModeEnabled())
        if (isDeveloperModeEnabled()) {
            (context as AppCompatActivity).startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }


    }

    fun showDialog() {
        dialog = AlertDialog.Builder(context).apply {
            setTitle("Please Disable Developer Mode")
            setMessage("You will not be able to proceed if developer mode is enabled status ")
            setPositiveButton("Go to Settings") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
            setIcon(android.R.drawable.stat_notify_error)
            setCancelable(false)

        }.create()

        dialog?.show()
    }


}

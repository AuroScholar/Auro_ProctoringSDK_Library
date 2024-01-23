package com.example.auroproctoringsdk.developerMode

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CheckDeveloperMode(val context: Context) {
    private var dialog: AlertDialog? = null
    fun isDeveloperModeEnabled(): Boolean {
        // developerModeOff()
        /*return Settings.Secure.getInt(
            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0*/

        if (Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        ) {
            // Developer Mode is enabled
            // Perform your desired actions here
     // update
/*            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)*/

            // Developer Mode is enabled
            // Perform your desired actions here

            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Check if there is an activity that can handle the intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Handle the case where no activity can handle the intent
                // Display an error message or perform an alternative action
            }

        }


        return Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

    }

    fun turnOffDeveloperMode() { // old code
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

    fun checkAndTurnOffDeveloperMode() {
        val developerModeSettings = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )
        if (developerModeSettings == 1) {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            )
        }
    }

    fun developerModeOff() {

        val developerModeEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

        if (developerModeEnabled) {
            // Developer Mode is enabled
            // Perform your desired actions here

            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        } else {
            // Developer Mode is not enabled
            // Handle the case accordingly
        }


        /* try {
             // Check if the app has the required permission
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                 // Request the WRITE_SETTINGS permission
                 val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                 intent.data = Uri.parse("package:" + context.packageName)
                 context.startActivity(intent)
             } else {
                 // Perform the desired operation that requires WRITE_SETTINGS permission
                 val developerModeSettings = Settings.Secure.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
                 if (developerModeSettings == 1) {
                     Settings.Global.putInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
                 }
             }
         } catch (e: SecurityException) {
             // Handle the SecurityException here
             // For example, show an error message to the user or log the exception
             e.printStackTrace()
         }*/
    }

}

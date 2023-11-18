package com.example.auroproctoringsdk.developerMode

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class CheckDeveloperMode(val context: Context) {
    private var dialog: AlertDialog? = null
    fun isDeveloperModeEnabled(): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }

    fun turnOffDeveloperMode() {
        Log.e("TAG", "turnOffDeveloperMode: developer status " + isDeveloperModeEnabled())

        /*if (isDeveloperModeEnabled()) {
            try {
                AlertDialog.Builder(context)
                    .setTitle("Please Disable Developer Mode")
                    .setMessage("You will not be able to proceed if developer mode is enabled status "+isDeveloperModeEnabled())
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
        }else{

        }*/

        /*
                val dialog = AlertDialog.Builder(context).apply {
                    setTitle("Please Disable Developer Mode")
                    setMessage("You will not be able to proceed if developer mode is enabled ")
                    setPositiveButton("Go to Settings") { _, _ ->
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    }
                    setIcon(android.R.drawable.stat_notify_error)
                    setCancelable(false)
                }.create()
        */

        if (isDeveloperModeEnabled()) {

//            Toast.makeText(context, "is dev -- " + isDeveloperModeEnabled(), Toast.LENGTH_SHORT)
//                .show()
           showDialog()

        } else {
//            Toast.makeText(context, "is dev  -- " + isDeveloperModeEnabled(), Toast.LENGTH_SHORT)
//                .show()
         //   hideDialog()

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

        }.create()
        dialog?.show()
//        try {
//            Settings.Secure.putInt(
//                context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
//            )
//        }catch (e:Exception){
//            e.printStackTrace()
//        }
    }

    fun hideDialog() {
        dialog?.cancel()
        dialog?.dismiss()
    }


}

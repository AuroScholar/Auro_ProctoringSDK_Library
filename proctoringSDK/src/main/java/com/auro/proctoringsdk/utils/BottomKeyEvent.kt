package com.auro.proctoringsdk.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class BottomKeyEvent {
    fun onBackPressHandle(activity: AppCompatActivity) {
        activity.onBackPressedDispatcher.addCallback(
            activity,
            object : OnBackPressedCallback(true) {
                private var doubleBackToExitPressedOnce = false

                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        activity.finish()
                        return
                    }

                    this.doubleBackToExitPressedOnce = true
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { _, _ -> activity.finish() }
                        .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                    val alert = builder.create()
                    alert.show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            })
    }

}

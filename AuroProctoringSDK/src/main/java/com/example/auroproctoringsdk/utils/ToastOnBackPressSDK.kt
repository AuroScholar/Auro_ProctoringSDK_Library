package com.example.auroproctoringsdk.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

object ToastOnBackPressSDK {
    fun init(activity: AppCompatActivity) {
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

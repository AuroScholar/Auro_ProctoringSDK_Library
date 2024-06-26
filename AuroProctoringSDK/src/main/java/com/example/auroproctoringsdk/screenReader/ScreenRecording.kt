package com.example.auroproctoringsdk.screenReader

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager

class ScreenRecording(val context: Context) {

     open fun disableScreenshots() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context as Activity).window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            (context as Activity).window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    open fun enableScreenshots() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context as Activity).window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            (context as Activity).window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

}
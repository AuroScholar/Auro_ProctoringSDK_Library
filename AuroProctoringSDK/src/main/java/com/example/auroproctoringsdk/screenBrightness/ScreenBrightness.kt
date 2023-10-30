package com.example.auroproctoringsdk.screenBrightness

import android.app.Activity
import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate

class ScreenBrightness(private val context: Context) {

    fun setScreenBrightness(isDarkModeOn: Boolean) {
        if (isDarkModeOn) {
            val win: Window = (context as Activity).window
            val winParams = win.attributes
            winParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
            win.attributes = winParams
            val layout: WindowManager.LayoutParams = (context as Activity).window.attributes
            layout.screenBrightness = 0.0f
            layout.alpha = 0.3f
            (context as Activity).window.attributes = layout
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_YES
                )

        } else {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_NO
                )
        }
    }
}

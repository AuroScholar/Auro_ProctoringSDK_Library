package com.example.auroproctoringsdk.screenBrightness

import android.app.Activity
import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class ScreenBrightness(private val context: Context) {

    fun setScreenBrightness(isDarkModeOn: Boolean) {
        try {
            if (isDarkModeOn) {
                val window: Window = (context as AppCompatActivity).window
                val windowParams = window.attributes.apply {
                    screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
                }
                window.attributes = windowParams

                val layout: WindowManager.LayoutParams = context.window.attributes.apply {
                    screenBrightness = 0.0f
                    alpha = 0.3f
                }
                context.window.attributes = layout

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception or log the error message
        }
    }

    fun heightBrightness(context: Context){
        (context as Activity).screenBrightness(1f)
    }
    fun lowBrightness(context: Context){
        (context as Activity).screenBrightness(0f)
    }


}

fun Fragment.screenBrightness(x: Float) = activity?.screenBrightness(x)
fun Activity.screenBrightness(x: Float) = window?.apply {
    attributes = attributes?.apply { screenBrightness = x.coerceIn(-1f..1f) } }

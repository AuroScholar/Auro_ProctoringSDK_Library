package com.example.auroproctoringsdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import com.example.auroproctoringsdk.copypastestop.ClipboardManagerHelper
//import com.example.auroproctoringsdk.screenReader.ScreenShortDetector

/**
 * Application
 *
 * @constructor Create empty Application
 */
class Application : Application() {

    private var controls = Controls()

    companion object {
        var faceDirectionAccuracy: Int = 50 // 10 is Default
        var faceMouthAccuracy: Float = 10.0f//5.0F //3.0 f defalut

    }

    override fun onCreate() {
        super.onCreate()

        if (controls.getControls().isAlert) {

            if (controls.getControls().isScreenshotEnable) {
                // stop screen short and video recording
                registerActivityLifecycle()
            }

            if (!controls.getControls().isCopyPaste) {
                // Stop copy paste option
                ClipboardManagerHelper(this).clearClipboard()
            }
        }


    }


    private fun registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
                )

            }

            override fun onActivityStarted(p0: Activity) {
//                ScreenShortDetector(p0.applicationContext).startListening()

            }

            override fun onActivityResumed(p0: Activity) {

            }

            override fun onActivityPaused(p0: Activity) {

            }

            override fun onActivityStopped(p0: Activity) {

            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

            }

            override fun onActivityDestroyed(p0: Activity) {
//                ScreenShortDetector(p0.applicationContext).stopListening()
            }

        })
    }
}
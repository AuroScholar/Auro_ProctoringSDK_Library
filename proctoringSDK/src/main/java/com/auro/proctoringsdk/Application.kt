package com.auro.proctoringsdk

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import com.auro.proctoringsdk.copypastestop.ClipboardManagerHelper

class Application : Application() {


    companion object {
        var defaultAlert: Boolean = true
//        var deadlyInMilliseconds: Long = 100000
        var deadlyInMilliseconds: Long = 30000
        var faceDirectionAccuracy : Int = 10
        var faceMouthAccuracy : Float = 3.0F
        var surfaceBoardErrorColor : Int = Color.RED
        var surfaceBoardSuccessColor : Int = Color.GREEN
        var surfaceBoardNoColor : Int = Color.TRANSPARENT

    }

    override fun onCreate() {
        super.onCreate()

        if (defaultAlert) {

            // stop screen short and video recording
          registerActivityLifecycle()


            // Stop copy paste option
            ClipboardManagerHelper(this).clearClipboard()
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

            }

        })
    }
}
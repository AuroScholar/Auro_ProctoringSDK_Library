package com.example.auroproctoringsdk

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import com.example.auroproctoringsdk.copypastestop.ClipboardManagerHelper

class SdkAppLevel : Application() {


    companion object {
        var defaultAlert: Boolean = true
        var faceDirectionAccuracy : Int = 30//20//10 //default 10
        var faceMouthAccuracy : Float = 5.0F //3.0 f defalut
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
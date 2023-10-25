package com.example.mytoolbox

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import android.view.WindowManager
import com.example.mytoolbox.utils.ClipboardHelper

class Application : Application() {
    companion object{

        var defaultAlert: Boolean = true
        var deadlyInMilliseconds: Long = 30000

    }
    override fun onCreate() {
        super.onCreate()


        if (defaultAlert){
            // stop screen short and video recording
            registerActivityLifecycle()
            // Stop copy paste option
            ClipboardHelper(this).clearClipboard()
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
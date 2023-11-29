package com.example.auroproctoringsdk.screenBarLock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

// Pinning an app
class PinUnpinApp {
    fun pinApp(context: Context, packageName: String) {
        val componentName = ComponentName(packageName, "")
        val packageManager = context.packageManager

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun unpinApp(context: Context, packageName: String) {
        val componentName = ComponentName(packageName, "")
        val packageManager = context.packageManager

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
            PackageManager.DONT_KILL_APP
        )
    }
}
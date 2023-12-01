package com.example.auroproctoringsdk.appLocker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

/**
 * App lock manager
 *
 * @property context
 * @constructor Create empty App lock manager
 */
class AppLockManager(val context: Context) {

    private val APP_PACKAGES = arrayOf(getPackageName(context))

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminName = getComponentName(context)

    private fun getComponentName(context: Context): ComponentName {
        return ComponentName(context, AppLockManager::class.java)
    }

    private fun getPackageName(context: Context): String {
        return context.applicationContext.packageName
    }

    /**
     * Lock current app
     *
     */
    fun lockCurrentApp() {
        if (dpm.isAdminActive(adminName)) {
            dpm.setLockTaskPackages(adminName,APP_PACKAGES)
        }
    }

    /**
     * Unlock current app
     *
     */
    fun unlockCurrentApp() {
        if (dpm.isAdminActive(adminName)) {
            dpm.setLockTaskPackages(adminName, emptyArray())
        }
    }
}


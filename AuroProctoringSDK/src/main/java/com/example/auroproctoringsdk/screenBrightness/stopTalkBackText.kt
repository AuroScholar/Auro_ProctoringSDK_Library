package com.example.auroproctoringsdk.screenBrightness

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.core.view.ViewCompat

fun stopTalkBackText(context: Context) {
   /* val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (accessibilityManager.isEnabled) {
        View(context).importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }*/
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (accessibilityManager.isEnabled) {
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ""
        )
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            "0"
        )
    }

   /* ViewCompat.setImportantForAccessibility(context.window.decorView,
        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO)*/



}

fun Context.isScreenReaderOn():Boolean{
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (am != null && am.isEnabled) {
        val serviceInfoList =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        if (!serviceInfoList.isEmpty())
            return true
    }
    return false}
private fun isGoogleTalkbackActive(accessibilityManager : AccessibilityManager) : Boolean
{
    val accessibilityServiceInfoList = accessibilityManager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_SPOKEN)
    for (accessibilityServiceInfo in accessibilityServiceInfoList)
    {
        if ("com.google.android.marvin.talkback".equals(accessibilityServiceInfo.resolveInfo.serviceInfo.processName))
        {
            return true
        }
    }
    return false
}

fun turnOffTalkBack(context: Context) {
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (accessibilityManager.isEnabled) {
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ""
        )
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            "0"
        )
    }
}
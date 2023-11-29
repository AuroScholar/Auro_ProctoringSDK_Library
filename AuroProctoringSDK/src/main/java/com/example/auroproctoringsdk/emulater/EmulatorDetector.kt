package com.example.auroproctoringsdk.emulater

import android.os.Build
import java.io.File

class EmulatorDetector {
    fun isEmulatorRun(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") || Build.FINGERPRINT.startsWith(
            "generic"
        ) || Build.FINGERPRINT.startsWith("unknown") || Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains(
            "ranchu"
        ) || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains(
            "Android SDK built for x86"
        ) || Build.MANUFACTURER.contains("Genymotion") || Build.MANUFACTURER.contains("Google") || Build.PRODUCT.contains(
            "sdk_google"
        ) || Build.PRODUCT.contains("google_sdk") || Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains(
            "sdk_x86"
        ) || Build.PRODUCT.contains("vbox86p") || Build.PRODUCT.contains("emulator") || Build.PRODUCT.contains(
            "simulator"
        ) || Build.PRODUCT.contains("Genymotion") || Build.PRODUCT.contains("Bluestacks") ||  Build.MANUFACTURER.equals("BlueStacks", ignoreCase = true))
    }

    fun checkFilesExist(files: Array<String>): Boolean {
        files.forEach {
            val file = File(it)
            if (file.exists()) {
                return true
            }
        }
        return false
    }

    fun isBlueStacks(): Boolean {
        val BLUE_STACKS_FILES = arrayOf(
            "/mnt/windows/BstSharedFolder"
        )
        return checkFilesExist(BLUE_STACKS_FILES)
    }
}

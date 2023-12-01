package com.example.auroproctoringsdk.emulater

import android.os.Build
import java.io.File

class EmulatorDetector {


    //Working fine in BlueStack 5.14.0.1061 P64
    fun isBlueStacks(): Boolean {
        val BLUE_STACKS_FILES = arrayOf(
            "/mnt/windows/BstSharedFolder"
        )
        return checkFilesExist(BLUE_STACKS_FILES)
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

    //v3 code
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
                || "sdk" == Build.PRODUCT
                || "sdk_google" == Build.PRODUCT
                || "vbox86p" == Build.PRODUCT
    }

    fun isEmulatorRunning() : Boolean{
        val isProbablyRunningOnEmulator: Boolean by lazy {
            // Android SDK emulator
            return@lazy ((Build.MANUFACTURER == "Google" && Build.BRAND == "google" &&
                    ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                            && Build.FINGERPRINT.endsWith(":user/release-keys")
                            && Build.PRODUCT.startsWith("sdk_gphone_")
                            && Build.MODEL.startsWith("sdk_gphone_"))
                            //alternative
                            || (Build.FINGERPRINT.startsWith("google/sdk_gphone64_")
                            && (Build.FINGERPRINT.endsWith(":userdebug/dev-keys") || Build.FINGERPRINT.endsWith(":user/release-keys"))
                            && Build.PRODUCT.startsWith("sdk_gphone64_")
                            && Build.MODEL.startsWith("sdk_gphone64_"))))
                    //
                    || Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    //bluestacks
                    || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
                    //bluestacks
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.HOST.startsWith("Build")
                    //MSI App Player
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || Build.PRODUCT == "google_sdk"
                    // another Android SDK emulator check
                    || SystemProperties.getProp("ro.kernel.qemu") == "1")

        }
        return isProbablyRunningOnEmulator
    }




}

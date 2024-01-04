package com.example.auroproctoringsdk.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.auroproctoringsdk.developerMode.CheckDeveloperMode


class ProctoringPermissionRequest(private val activity: Activity) {

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 100
    }

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_NOTIFICATION_POLICY,
    )

    fun requestPermissions() {
        val permissionNeeded = mutableListOf<String>()

        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(activity, permission)
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                permissionNeeded.add(permission)
            }
        }

        if (permissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionNeeded.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun checkPermissionGranted(): Boolean {
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(activity, permission)
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE){

            activity.finish()
//            activity.startActivity(activity.intent)
//            activity.overridePendingTransition(0, 0)
            activity.startActivity(
                Intent(activity.intent).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            )


        }else{
            // permission denied
            this.requestPermissions()
        }
    }

}

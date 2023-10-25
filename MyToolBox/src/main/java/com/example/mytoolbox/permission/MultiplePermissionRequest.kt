package com.example.mytoolbox.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MultiplePermissionRequest(private val activity: Activity) {

    companion object {


        const val PERMISSIONS_REQUEST_CODE = 100
    }

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
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

    fun onReuestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE){
            // permission are granted
        }else{
            // permission denied
            this.requestPermissions()
        }
    }
}

package com.example.mytoolbox

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class MultiplePermissionRequest(private val activity: Activity) {

    private var permissionIndex = 0
    private var permissions: MutableList<String> = arrayListOf(Manifest.permission.CAMERA ,Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val requestCode: Int = 100


    fun requestPermissions() {
        if (permissionIndex < permissions.size) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[permissionIndex])) {
                AlertDialog.Builder(activity)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok") { _, _ ->
                        ActivityCompat.requestPermissions(activity, arrayOf(permissions[permissionIndex]), requestCode)
                    }
                    .setNegativeButton("cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(permissions[permissionIndex]), requestCode)
            }
        } else {
            Toast.makeText(activity, "All permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionIndex++
                requestPermissions()
            } else {
                Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                requestPermissions()
            }
        }
    }

    fun checkPermissions() {
        if (permissionIndex < permissions.size) {
            if (ActivityCompat.checkSelfPermission(activity, permissions[permissionIndex]) == PackageManager.PERMISSION_GRANTED) {
                permissionIndex++
                checkPermissions()
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(permissions[permissionIndex]), requestCode)
            }
        }
    }
}

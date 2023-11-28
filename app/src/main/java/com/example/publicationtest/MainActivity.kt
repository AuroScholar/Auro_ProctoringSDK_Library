package com.example.publicationtest

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding

// OnProctoringResultListener for detector result
class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorResultListener {

    //init permission
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications

        val notificationIds = activeNotifications.map { it.id }

        binding.notificationId.text = notificationIds.toString()
        Log.e("TAG", "onCreate: notification id "+notificationIds )

        binding.notificationId.setOnClickListener {
            clearAll(notificationManager,notificationIds)
        }


        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            binding.mainLayout.initLifecycle(this.lifecycle)
            binding.mainLayout.startProctoring(this)

        } else {
            proctoringPermissionRequest.requestPermissions()
        }

    }
    fun clearAll(notificationManager: NotificationManager, notificationIds: List<Int>) {

        notificationIds.forEach { number ->
            println("Square of notification id ----> $number")
            notificationManager.cancel(number)

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proctoringPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String,
    ) {

    }

    override fun onFaceCount(faceCount: Int) {
        // getting face count
        binding.textView.text = faceCount.toString()
    }

    override fun isRunningDetector(boolean: Boolean?) {
        // detect running status
    }

    override fun onSuccess(faceBounds: Int) {
        // getting face count
    }

    override fun onFailure(exception: Exception) {
        // error on SDK level
    }

    override fun onLipMovementDetection(face: Boolean) {
        // Lips Movement is mouth is open and close
        binding.textView.text.apply {
            if (face) {
                "Open"
            } else {
                "Close"
            }
        }
    }

    override fun onObjectDetection(faceError: ArrayList<String>) {
        // object detection on camera
    }

    override fun onEyeDetectionOnlyOneFace(faceError: String) {
        // eye open and close status
        binding.textView.text = faceError
    }

    override fun onUserWallDistanceDetector(distance: Float) {
        // user pose detection
    }

    override fun onFaceDirectionMovement(faceDirection: String?) {
    }

    override fun captureImage(faceDirection: Bitmap?) {

    }


}

// head status
// perierty base
// Camera , dev , DND
// dialog cross button

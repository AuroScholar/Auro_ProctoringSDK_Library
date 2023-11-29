package com.example.publicationtest

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK.onProctorListener
import android.util.Log
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest

// OnProctoringResultListener for detector result

class MainActivity : AppCompatActivity(), onProctorListener {



    //init permission
    private val binding by lazy { com.example.publicationtest.databinding.ActivityMainBinding.inflate(layoutInflater) }
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications

        val notificationIds = activeNotifications.map { it.id }


        Log.e("TAG", "onCreate: notification id "+notificationIds )





        if (proctoringPermissionRequest.checkPermissionGranted()) {

            binding.mainLayout.observeLifecycle(lifecycle)





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

    override fun isRunningDetector(boolean: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun onVoiceDetected(
        amplitude: Double,
        isNiceDetected: Boolean,
        isRunning: Boolean,
        typeOfVoiceDetected: String
    ) {
        TODO("Not yet implemented")
    }

    override fun onSuccess(faceBounds: Int) {
        TODO("Not yet implemented")
    }

    override fun onFailure(exception: Exception) {
        TODO("Not yet implemented")
    }

    override fun onFaceCount(face: Int) {
        TODO("Not yet implemented")
    }

    override fun onLipMovementDetection(face: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onObjectDetection(face: String) {
        TODO("Not yet implemented")
    }

    override fun onEyeDetectionOnlyOneFace(face: String) {
        TODO("Not yet implemented")
    }

    override fun onUserWallDistanceDetector(distance: Float) {
        TODO("Not yet implemented")
    }

    override fun onFaceDirectionMovement(faceDirection: String?) {
        TODO("Not yet implemented")
    }

    override fun captureImage(faceDirection: Bitmap?) {
        TODO("Not yet implemented")
    }


}



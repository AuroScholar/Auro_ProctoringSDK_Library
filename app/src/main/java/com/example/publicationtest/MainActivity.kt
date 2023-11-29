package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.example.auroproctoringsdk.ProctoringSDK.onProctorListener
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding

// OnProctoringResultListener for detector result
class MainActivity : AppCompatActivity(), onProctorListener {

    //init permission
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        if (proctoringPermissionRequest.checkPermissionGranted()) {
            binding.mainLayout.observeLifecycle(lifecycle)


        } else {
            proctoringPermissionRequest.requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //proctoringPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults)

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



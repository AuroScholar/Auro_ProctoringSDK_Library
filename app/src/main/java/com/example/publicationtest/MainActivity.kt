package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding

// ProctoringSDK.onProctorListener for detector result
class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    //init permission
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)




        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            binding.mainLayout.observeLifecycle(this.lifecycle) // very import for all

            binding.mainLayout.startProctoring(this,null)

        } else {
            //request permission
            proctoringPermissionRequest.requestPermissions()
        }

        binding.btn.setOnClickListener {
            binding.mainLayout.alertOnOff()
        }
    }


    override fun onResume() {
        super.onResume()
       /* if (proctoringPermissionRequest.checkPermissionGranted()) {
            //if permission done then start proctoring // also you can control using ControlModel just add model startProctoring(this,ControlModel)

        }*/

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       /* proctoringPermissionRequest.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )*/

    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String,
    ) {

    }

    override fun onFaceCount(faceCount: Int) {
        // getting face count
        // binding.textView.text = faceCount.toString()
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

    }

    override fun onObjectDetection(face: ArrayList<String>) {
        binding.textView.text = face.toString()

    }

    override fun onEyeDetectionOnlyOneFace(face: String) {

    }

    override fun onUserWallDistanceDetector(distance: Float) {
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

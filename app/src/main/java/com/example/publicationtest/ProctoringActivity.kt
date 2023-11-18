package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auro.proctoringsdk.ProctoringSDK
import com.auro.proctoringsdk.detector.FaceDetector
import com.auro.proctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding

// OnProctoringResultListener for detector result
class ProctoringActivity : AppCompatActivity(), FaceDetector.OnProctoringResultListener{
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
    //init permission
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (proctoringPermissionRequest.checkPermissionGranted()) {

            val proctoringSDK = ProctoringSDK(this, null)
            binding.mainLayout.addView(proctoringSDK)
            proctoringSDK.startProctoring(this)



        }
        else {
            proctoringPermissionRequest.requestPermissions()
        }



    }

    override fun isRunningDetector(boolean: Boolean?) {
        Toast.makeText(this, boolean.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onVoiceDetected(
        amplitude: Double,
        isNiceDetected: Boolean,
        isRunning: Boolean,
        typeOfVoiceDetected: String
    ) {

    }

    override fun onSuccess(faceBounds: Int) {

    }

    override fun onFailure(exception: Exception) {

    }

    override fun onFaceCount(face: Int) {
        Toast.makeText(this, face, Toast.LENGTH_SHORT).show()

    }

    override fun onLipMovementDetection(face: Boolean) {
        Toast.makeText(this, face.toString(), Toast.LENGTH_SHORT).show()

    }

    override fun onObjectDetection(face: String) {
        Toast.makeText(this, face, Toast.LENGTH_SHORT).show()

    }

    override fun onEyeDetectionOnlyOneFace(face: String) {
        Toast.makeText(this, face, Toast.LENGTH_SHORT).show()

    }

    override fun onUserWallDistanceDetector(distance: Float) {

    }

    override fun captureImage(faceDirection: Bitmap?) {
        Toast.makeText(this, faceDirection.toString(), Toast.LENGTH_SHORT).show()

    }
}


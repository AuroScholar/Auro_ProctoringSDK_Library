package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainNewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity1 : AppCompatActivity(), com.example.auroproctoringsdk.detector.testcode1.ProctoringSDKDev.onProctorListener {

    private val binding by lazy { ActivityMainNewBinding.inflate(layoutInflater) }
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (proctoringPermissionRequest.checkPermissionGranted()) {
            proctoringPermissionRequest.requestPermissions()
        }

        binding.camera.observeLifecycle(this.lifecycle)

    }

    override fun onResume() {
        super.onResume()
        val wait = runOnMainAfter(1000){
            binding.camera.startProctoring(this,null)
        }
    }

    fun runOnMainAfter(interval: Long, runnable: () -> Unit): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            delay(interval)
            runnable()
        }
    }

    override fun isRunningDetector(boolean: Boolean?) {

    }

    override fun onVoiceDetected(
        amplitude: Double,
        isNiceDetected: Boolean,
        isRunning: Boolean,
        typeOfVoiceDetected: String,
    ) {

    }

    override fun onSuccess(faceBounds: Int) {

    }

    override fun onFailure(exception: Exception) {

    }

    override fun onFaceCount(face: Int) {

    }

    override fun onLipMovementDetection(face: Boolean) {

    }

    override fun onObjectDetection(face: ArrayList<String>) {

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

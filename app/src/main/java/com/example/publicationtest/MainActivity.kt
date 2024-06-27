package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.model.ControlModel
import com.example.publicationtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.mainLayout.observeLifecycle(this.lifecycle) // very import for all
        binding.mainLayout.startProctoring(this, null)

        binding.btn.setOnClickListener {
            val control = binding.mainLayout.getControl()
            val newControl = control.copy(isScreenshotEnable = !control.isScreenshotEnable)
            if (newControl.isScreenshotEnable){
                binding.btn.text =" Screen Recoding Stop "
            }else{
                binding.btn.text =" Screen Recoding Start "
            }
            binding.mainLayout.updateControl(newControl)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun isRunningDetector(boolean: Boolean?) {
        
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
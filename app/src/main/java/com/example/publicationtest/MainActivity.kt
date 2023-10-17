package com.example.publicationtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mytoolbox.OverLay.FaceDetector.OnFaceDetectionResultListener
import com.example.publicationtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),OnFaceDetectionResultListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.customSurface.startProctoring(this)


    }

    override fun onVoiceDetected(
        amplitude: Double,
        isNiceDetected: Boolean,
        isRunning: Boolean,
        typeOfVoiceDetected: String
    ) {

    }

    override fun onFaceCount(faceCount: String) {

    }


    override fun onSuccess(faceBounds: Int) {
        super.onSuccess(faceBounds)
    }
    override fun onLipMovementDetection(face: Boolean) {
        binding.textView.text = face.toString()
    }

    override fun onObjectDetection(faceError: String) {

    }

    override fun onEyeDetectionOnlyOneFace(faceError: String) {

    }

    override fun onUserWallDistanceDetector(distance: Float) {

    }


}
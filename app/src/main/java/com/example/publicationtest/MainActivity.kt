package com.example.publicationtest

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.example.mytoolbox.ProctoringSDK
import com.example.mytoolbox.proctoring.FaceDetector.OnProctoringResultListener
import com.example.publicationtest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(),OnProctoringResultListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val proctoringSDK = ProctoringSDK(this, null)

        binding.mainLayout.gravity = Gravity.END
        binding.mainLayout.addView(proctoringSDK)

        proctoringSDK.startProctoring(this)


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
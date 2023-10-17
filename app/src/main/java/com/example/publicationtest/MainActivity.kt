package com.example.publicationtest

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import com.example.mytoolbox.CustomSurfaceView
import com.example.mytoolbox.proctoring.FaceDetector.OnFaceDetectionResultListener
import com.example.publicationtest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(),OnFaceDetectionResultListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val customSurfaceView = CustomSurfaceView(this, null)

        binding.mainLayout.gravity = Gravity.END
        binding.mainLayout.addView(customSurfaceView)

        customSurfaceView.startProctoring(this)


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
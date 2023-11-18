//package com.example.publicationtest
//
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.auro.proctoringsdk.ProctoringSDK
//import com.auro.proctoringsdk.detector.FaceDetector
//import com.auro.proctoringsdk.permission.ProctoringPermissionRequest
//import com.example.publicationtest.databinding.ActivityMainBinding
//
//// OnProctoringResultListener for detector result
//class ProctoringActivity : AppCompatActivity(), FaceDetector.OnProctoringResultListener {
//    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
//    //init permission
//    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        Log.e("TAG", "onCreate:  exam camera code ", )
////          val proctoringSDK = ProctoringSDK(this, null)
////        proctoringSDK.startProctoring(this)
////            binding.mainLayout.addView(proctoringSDK)
//        binding.proctview.startProctoring(this)
//    }
//
//    override fun isRunningDetector(boolean: Boolean?) {
//
//    }
//
//    override fun onVoiceDetected(
//        amplitude: Double,
//        isNiceDetected: Boolean,
//        isRunning: Boolean,
//        typeOfVoiceDetected: String
//    ) {
//
//    }
//
//    override fun onSuccess(faceBounds: Int) {
//
//    }
//
//    override fun onFailure(exception: Exception) {
//
//    }
//
//    override fun onFaceCount(face: Int) {
//        Log.e("TAG", "onFaceCount: --> "+face )
//    }
//
//    override fun onLipMovementDetection(face: Boolean) {
//
//    }
//
//    override fun onObjectDetection(face: String) {
//
//    }
//
//    override fun onEyeDetectionOnlyOneFace(face: String) {
//    }
//
//    override fun onUserWallDistanceDetector(distance: Float) {
//    }
//
//    override fun captureImage(faceDirection: Bitmap?) {
//        binding.viewPagerImageList.setImageBitmap(faceDirection)
//    }
//
//}
//

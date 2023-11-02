package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.detector.FaceDetector.OnProctoringResultListener
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding
// OnProctoringResultListener for detector result
class MainActivity : AppCompatActivity(), OnProctoringResultListener {

    //init permission
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        ToastOnBackPressSDK.init(this)

        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            // init Proctoring SDK
            val proctoringSDK = ProctoringSDK(this)

            // add camera output for user alert
            binding.mainLayout.gravity = Gravity.END
            binding.mainLayout.addView(proctoringSDK)

// start proctoring

            proctoringSDK.startProctoring(this)

            proctoringSDK.getCaptureImagesList().observe(this) {
                //            it?.let { updateUi(it) }
            }


            binding.btnToggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    val status = proctoringSDK.startProctoring()
                    binding.textView.text = "Running"
                }else{
                    var status = proctoringSDK.stopProctoring()
                    if (status){
                        binding.textView.text = "Stoped"
                    }
                }
            }


            binding.btnAlert.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    proctoringSDK.defaultAlert()
                }else{
                    proctoringSDK.defaultAlert()
                }
            }



        } else {
            proctoringPermissionRequest.requestPermissions()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proctoringPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String
    ) {
        // detect voice and type of voice

    }

    override fun onFaceCount(faceCount: Int) {
        // getting face count
        binding.textView.text = faceCount.toString()
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
        binding.textView.text.apply {
            if (face) {
                "Open"
            } else {
                "Close"
            }
        }
    }

    override fun onObjectDetection(faceError: String) {
        // object detection on camera
    }

    override fun onEyeDetectionOnlyOneFace(faceError: String) {
        // eye open and close status
        binding.textView.text = faceError
    }

    override fun onUserWallDistanceDetector(distance: Float) {
        // user pose detection
    }

    override fun captureImage(faceDirection: Bitmap?) {

    }


}

// head status
// perierty base
// Camera , dev , DND
// dialog cross button

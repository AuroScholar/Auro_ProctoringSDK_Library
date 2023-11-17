package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.auro.proctoringsdk.ProctoringSDK
import com.auro.proctoringsdk.detector.FaceDetector
import com.auro.proctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding

// OnProctoringResultListener for detector result
class MainActivity : AppCompatActivity(), FaceDetector.OnProctoringResultListener {

    //init permission
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val attrs: AttributeSet = object : AttributeSet {
            override fun getAttributeCount(): Int {
                return 0
            }

            override fun getAttributeName(index: Int): String? {
                return null
            }

            override fun getAttributeValue(index: Int): String? {
                return null
            }

            override fun getAttributeValue(namespace: String, name: String): String? {
                return null
            }

            override fun getPositionDescription(): String? {
                return null
            }

            override fun getAttributeNameResource(index: Int): Int {
                return 0
            }

            override fun getAttributeListValue(
                namespace: String,
                attribute: String,
                options: Array<String>,
                defaultValue: Int
            ): Int {
                return 0
            }

            override fun getAttributeBooleanValue(
                namespace: String,
                attribute: String,
                defaultValue: Boolean
            ): Boolean {
                return false
            }

            override fun getAttributeResourceValue(
                namespace: String,
                attribute: String,
                defaultValue: Int
            ): Int {
                return 0
            }

            override fun getAttributeIntValue(
                namespace: String,
                attribute: String,
                defaultValue: Int
            ): Int {
                return 0
            }

            override fun getAttributeUnsignedIntValue(
                namespace: String,
                attribute: String,
                defaultValue: Int
            ): Int {
                return 0
            }

            override fun getAttributeFloatValue(
                namespace: String,
                attribute: String,
                defaultValue: Float
            ): Float {
                return 0F
            }

            override fun getAttributeListValue(
                index: Int,
                options: Array<String>,
                defaultValue: Int
            ): Int {
                return 0
            }

            override fun getAttributeBooleanValue(index: Int, defaultValue: Boolean): Boolean {
                return false
            }

            override fun getAttributeResourceValue(index: Int, defaultValue: Int): Int {
                return 0
            }

            override fun getAttributeIntValue(index: Int, defaultValue: Int): Int {
                return 0
            }

            override fun getAttributeUnsignedIntValue(index: Int, defaultValue: Int): Int {
                return 0
            }

            override fun getAttributeFloatValue(index: Int, defaultValue: Float): Float {
                return 0F
            }

            override fun getIdAttribute(): String? {
                return null
            }

            override fun getClassAttribute(): String? {
                return null
            }

            override fun getIdAttributeResourceValue(defaultValue: Int): Int {
                return 0
            }

            override fun getStyleAttribute(): Int {
                return 0
            }
        }

//        ToastOnBackPressSDK.init(this)

        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            // init Proctoring SDK
            val proctoringSDK = ProctoringSDK(this, attrs)

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



        }
        else {
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

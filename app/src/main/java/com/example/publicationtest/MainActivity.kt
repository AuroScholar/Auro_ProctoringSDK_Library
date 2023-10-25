package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.example.mytoolbox.ProctoringSDK
import com.example.mytoolbox.permission.ProctroringPermissionRequest
import com.example.mytoolbox.proctoring.FaceDetector.OnProctoringResultListener
import com.example.publicationtest.databinding.ActivityMainBinding

// OnProctoringResultListener for detector result
class MainActivity : AppCompatActivity(), OnProctoringResultListener {

    //init permission
    private var proctoringPermissionRequest = ProctroringPermissionRequest(this)
    // init Proctoring
    private val proctoringSDK = ProctoringSDK(this)

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            // set layout
            binding.mainLayout.gravity = Gravity.END
            binding.mainLayout.addView(proctoringSDK)

            //start proctoring
            proctoringSDK.startProctoring(this)

            proctoringSDK.getCaptureImagesList().observe(this) {
                //            it?.let { updateUi(it) }
            }

        } else {
            proctoringPermissionRequest.requestPermissions()
        }


    }

    private fun updateUi(it: List<Bitmap>) {
        /* val adapter =
             SimpleAdapter.with<Bitmap, ImageViewSimpleAdpterBinding>(R.layout.image_view_simple_adpter) { adapterPosition, model, bindingview ->
                 bindingview.imageView.setImageBitmap(model)
             }
         adapter.addAll(it)
         adapter.notifyDataSetChanged()
         binding.viewPagerImageList.adapter = adapter*/
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proctoringPermissionRequest.onReuestPermissionResult(requestCode,permissions,grantResults)
    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String
    ) {
      // detect voice and type of voice
    }

    override fun onFaceCount(faceCount: Int) {
        // getting face count

    }

    override fun isRunningDetector(boolean: Boolean?) {

    }

    override fun onSuccess(faceBounds: Int) {

    }

    override fun onFailure(exception: Exception) {

    }

    override fun onLipMovementDetection(face: Boolean) {
//        binding.textView.text = face.toString()
    }

    override fun onObjectDetection(faceError: String) {

    }

    override fun onEyeDetectionOnlyOneFace(faceError: String) {

    }

    override fun onUserWallDistanceDetector(distance: Float) {

    }

    override fun onFaceDirectionMovement(faceDirection: String?) {
        binding.textView.text = faceDirection
    }


}
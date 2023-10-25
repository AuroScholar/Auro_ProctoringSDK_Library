package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.example.mytoolbox.ProctoringSDK
import com.example.mytoolbox.permission.MultiplePermissionRequest
import com.example.mytoolbox.proctoring.FaceDetector.OnProctoringResultListener
import com.example.publicationtest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), OnProctoringResultListener {

    private var multiplePermissionRequest: MultiplePermissionRequest = MultiplePermissionRequest(this)


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (multiplePermissionRequest.checkPermissionGranted()) {
            // Permissions already granted
        } else {
            multiplePermissionRequest.requestPermissions()
        }


        val proctoringSDK = ProctoringSDK(this)
        binding.mainLayout.gravity = Gravity.END
        binding.mainLayout.addView(proctoringSDK)

        proctoringSDK.startProctoring(this)

        proctoringSDK.getCaptureImagesList().observe(this) {
            //            it?.let { updateUi(it) }
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
        if (requestCode == MultiplePermissionRequest.PERMISSIONS_REQUEST_CODE){
            // permission are granted
        }else{
            // permission denied
            multiplePermissionRequest.requestPermissions()
        }
    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String
    ) {
        Log.e("TAG", "onVoiceDetected: voice -- > " + amplitude)
    }

    override fun onFaceCount(faceCount: Int) {
//        binding.textView.text = faceCount.toString()

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
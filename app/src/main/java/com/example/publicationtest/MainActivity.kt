package com.example.publicationtest

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding

// ProctoringSDK.onProctorListener for detector result
class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorListener {


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    //init permission
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            binding.mainLayout.observeLifecycle(this.lifecycle)

        } else {
            //request permission
            proctoringPermissionRequest.requestPermissions()
        }

    }


    override fun onResume() {
        super.onResume()
        if (proctoringPermissionRequest.checkPermissionGranted()) {
            //if permission done then start proctoring // also you can control using ControlModel just add model startProctoring(this,ControlModel)
            binding.mainLayout.startProctoring(this,null)
        }

    }

  /*  fun stopTextReading(context: Context) {
        val textViewIds = mutableListOf<Int>()
        val rootView = findRootView(context)
        val views = getAllChildren(rootView)
        for (view in views) {
            if (view is TextView) {
                textViewIds.add(view.id)
                view.text = " No Text Read Any App "
                view.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
        }

    }*/
    /*private fun findRootView(context: Context): View {
        return (context as AppCompatActivity).findViewById<View>(android.R.id.content)
    }
    private fun getAllChildren(v: View): List<View> {
        if (v !is ViewGroup) {
            return listOf(v)
        }

        val result = mutableListOf<View>()
        val group = v as ViewGroup
        val count = group.childCount

        for (i in 0 until count) {
            val child = group.getChildAt(i)
            result.addAll(getAllChildren(child))
        }

        return result
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proctoringPermissionRequest.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String,
    ) {

    }

    override fun onFaceCount(faceCount: Int) {
        // getting face count
        // binding.textView.text = faceCount.toString()
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

    }

    override fun onObjectDetection(face: ArrayList<String>) {
      //  binding.textView.text = face.toString()

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

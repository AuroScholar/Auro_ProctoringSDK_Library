package com.example.publicationtest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.detector.FaceVerification
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ProctoringSDK.onProctorListener for detector result
class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    //init permission
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

      //  binding.mainLayout.observeLifecycle(this.lifecycle) // very import for all

        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {



        } else {
            //request permission
            proctoringPermissionRequest.requestPermissions()
        }

        binding.btn.setOnClickListener {
         //   binding.mainLayout.alertOnOff()

            val bitmp =   getDrawable(R.drawable.demo)?.let { drawableToBitmap(it) }
            val bitmp1 =   getDrawable(R.drawable.demo2)?.let { drawableToBitmap(it) }



            bitmp?.let { bitmp->
                bitmp1?.let {bitmap1 ->


                    val resizedDocImageBitmap = Bitmap.createScaledBitmap(bitmp, bitmp.width, bitmp.height, true)
                    val resizedCameraImageBitmap = Bitmap.createScaledBitmap(bitmap1, bitmp.width,bitmp.height, true)

                    FaceVerification().verifyNow(resizedDocImageBitmap,resizedCameraImageBitmap)
                }
            }

        }
    }

    fun getBitmap(@DrawableRes resId: Int): Bitmap? {
        val drawable = resources.getDrawable(resId)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    private fun Int.nonZero() = if (this <= 0) 1 else this

    override fun onResume() {
        super.onResume()
        /* if (proctoringPermissionRequest.checkPermissionGranted()) {
             //if permission done then start proctoring // also you can control using ControlModel just add model startProctoring(this,ControlModel)

         }*/

        val job = runOnMainAfter(1000) {

            //binding.mainLayout.startProctoring(this, null)

//             ContextCompat.getDrawable(this, R.drawable.demo)

            val drawable = getBitmap(R.drawable.demo)


            drawable?.let {
                /*val bit = it.toBitmap()*/

            }


          /*val bitmp =   getDrawable(R.drawable.demo)?.let { drawableToBitmap(it) }

            bitmp?.let {
                Log.e("CODEPOINT", "start ", )
                FaceVerification(it,it).verifyNow()
            }*/
        }

    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    fun improveCode(drawable: Drawable): Bitmap? {
        return try {
            val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } catch (e: Exception) {
            null
        }
    }


    fun runOnMainAfter(interval: Long, runnable: () -> Unit): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            delay(interval)
            runnable()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /* proctoringPermissionRequest.onRequestPermissionsResult(
             requestCode, permissions, grantResults
         )*/

    }

    override fun onVoiceDetected(
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String,
    ) {

    }

    override fun onFaceCount(faceCount: Int) {
        binding.textView.text = faceCount.toString()
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

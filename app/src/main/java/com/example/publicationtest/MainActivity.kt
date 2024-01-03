package com.example.publicationtest

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.auroproctoringsdk.ProctoringSDK
import com.example.auroproctoringsdk.detector.FaceCompareAwsApi
import com.example.auroproctoringsdk.detector.FaceVerification
import com.example.auroproctoringsdk.permission.ProctoringPermissionRequest
import com.example.publicationtest.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ProctoringSDK.onProctorListener for detector result
class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorListener,
    FaceVerification.FaceVerificationListener, FaceCompareAwsApi.FaceCompareAwsApiListener {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 200
    private var isFirstImageSave = false
    private var isSecondImageSave = false

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    var imageList = ArrayList<String>()

    val faceVerification = FaceCompareAwsApi()

    //init permission
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        faceVerification.setFaceCompareAwsResult(this)

//        faceVerification.setFaceVerificationListener(this)
        //  binding.mainLayout.observeLifecycle(this.lifecycle) // very import for all


        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {


        } else {
            //request permission
            proctoringPermissionRequest.requestPermissions()
        }

        binding.imageView1.setOnClickListener {
            isFirstImageSave = true
            isSecondImageSave = false
//            takePic()
            pickImage()
        }
        binding.imageView2.setOnClickListener {
            isFirstImageSave = false
            isSecondImageSave = true
//            takePic()
            pickImage()
        }

        binding.button2.setOnClickListener {
            checkImage()
            binding.progressbar.visibility = View.VISIBLE
        }

        binding.imageViewReset.setOnClickListener {
            reload()
            imageList.clear()
        }


    }

    fun reload() {
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    private fun takePic() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Camera permission is already granted, start camera intent
            startCameraIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedFileUri: Uri? = data.data
                val filePath: String? = RealFilePath.getPath(this, selectedFileUri)

                if (filePath != null) {
                    // Use the file path as needed
                    Log.d("FilePath---> ", filePath)
                    if (isFirstImageSave) {
                        binding.imageView1.setImageURI(selectedFileUri)
                    }
                    if (isSecondImageSave) {
                        binding.imageView2.setImageURI(selectedFileUri)
                    }

                    imageList.add(filePath)
                } else {
                    // Handle the case where the file path is null
                    Log.e("FilePath", "File path is null")
                }
            } else {
                // Handle the case where the data is null
                Log.e("FilePath", "Data is null")
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, start camera intent
                startCameraIntent()
            } else {
                // Camera permission denied, show a message or handle accordingly
            }
        }
    }

    private fun startCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)


    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        this.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun checkImage() {

        if (imageList.size <= 1) {
            Toast.makeText(this, "Need minimum 2 images path ", Toast.LENGTH_SHORT).show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                faceVerification.faceCompareProcess(imageList)
            }
        }




        val imageUrl = "https://example.com/image.jpg"

//        val drawable = Drawable.createFromUrl(imageUrl)

        /*resources.getDrawable(R.drawable.demo1)?.let { d1 ->

            resources.getDrawable(R.drawable.demo2)?.let { d2 ->


            }

        }*/


        /*val bitmp = getDrawable(R.drawable.demo)?.let { drawableToBitmap(it) }
        val bitmp1 = getDrawable(R.drawable.demo2)?.let { drawableToBitmap(it) }
        */

        val bitmp = drawableToBitmap(binding.imageView1.drawable)
        val bitmp1 = drawableToBitmap(binding.imageView2.drawable)





        bitmp.let { bitmp ->
            bitmp1.let { bitmap1 ->


                val resizedDocImageBitmap =
                    Bitmap.createScaledBitmap(bitmp, bitmp.width, bitmp.height, true)
                val resizedCameraImageBitmap =
                    Bitmap.createScaledBitmap(bitmap1, bitmp.width, bitmp.height, true)

                //faceVerification.verifyNow(resizedDocImageBitmap, resizedCameraImageBitmap)

                //  FaceComparison().startVerify(resizedDocImageBitmap, resizedCameraImageBitmap)

            }
        }
    }

    fun getUriToDrawable(
        context: Context,
        @AnyRes drawableId: Int,
    ): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.resources.getResourcePackageName(drawableId)
                    + '/' + context.resources.getResourceTypeName(drawableId)
                    + '/' + context.resources.getResourceEntryName(drawableId)
        )
    }

    fun getBitmap(@DrawableRes resId: Int): Bitmap? {
        val drawable = resources.getDrawable(resId)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    private fun Int.nonZero() = if (this <= 0) 1 else this

    override fun onResume() {
        super.onResume()/* if (proctoringPermissionRequest.checkPermissionGranted()) {
             //if permission done then start proctoring // also you can control using ControlModel just add model startProctoring(this,ControlModel)

         }*/

        val job = runOnMainAfter(1000) {

            //binding.mainLayout.startProctoring(this, null)

//             ContextCompat.getDrawable(this, R.drawable.demo)

            //   val drawable = getBitmap(R.drawable.demo)


            /* drawable?.let {
                 *//*val bit = it.toBitmap()*//*

            }*/


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
            Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


    fun runOnMainAfter(interval: Long, runnable: () -> Unit): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            delay(interval)
            runnable()
        }
    }

    /* override fun onRequestPermissionsResult(
         requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
     ) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         *//* proctoringPermissionRequest.onRequestPermissionsResult(
             requestCode, permissions, grantResults
         )*//*

    }*/

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

    override fun isFaceVerificationResult(hashMap: Float) {
//        binding.textView.text = hashMap.toList().toString()
        binding.progressbar.visibility = View.INVISIBLE

        /*val leftEye = hashMap["leftEyePercentage"].toString().toInt()
        val rightEye = hashMap["rightEyePercentage"].toString().toInt()
        val nose = hashMap["nosePercentage"].toString().toInt()
        val mouth = hashMap["mouthPercentage"].toString().toInt()

*/
        val finalResult = hashMap/*(leftEye + rightEye + nose + mouth) / 4*/

//        binding.textView.text = finalResult.toString()
        Log.e("TAG", "isFaceVerificationResult: " + finalResult)

        if (finalResult < 30) {
            binding.textView.setBackgroundColor(Color.GREEN)
        } else if (finalResult < 31 && finalResult < 49) {
            binding.textView.setBackgroundColor(Color.GREEN)
        } else if (finalResult < 51 && finalResult < 69) {
            binding.textView.setBackgroundColor(Color.RED)
        } else if (finalResult < 71 && finalResult < 99) {
            binding.textView.setBackgroundColor(Color.BLACK)
        }

        binding.textView.text = finalResult.toString()

    }

    override fun onFaceUnCompareResult(s: String) {

        runOnUiThread {
            binding.progressbar.visibility = View.INVISIBLE
//            binding.textView.text = s
        }
    }

    override fun onFaceResult(s: String) {

        runOnUiThread {
            binding.progressbar.visibility = View.INVISIBLE

            binding.textView.text = s
        }
    }

    override fun onFaceError(s: String) {

        runOnUiThread {
            binding.progressbar.visibility = View.INVISIBLE

            binding.textView.text = s
        }
    }

}

// head status
// perierty base
// Camera , dev , DND
// dialog cross button

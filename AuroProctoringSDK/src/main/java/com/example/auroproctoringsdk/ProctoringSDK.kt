package com.example.auroproctoringsdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Handler
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.detector.FaceDetector
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.LensFacing
import com.example.auroproctoringsdk.dnd.DNDManagerHelper
import com.example.auroproctoringsdk.emulater.EmulatorDetector
import com.example.auroproctoringsdk.screenBarLock.StatusBarLocker
import com.example.auroproctoringsdk.screenBrightness.ScreenBrightness
import com.example.auroproctoringsdk.windowFull.WindowUtils
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.thread


class ProctoringSDK(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private val faceDetector = FaceDetector()
    private var timer: Timer? = null
    private var alertDialog: AlertDialog? = null
    private var dialog: AlertDialog? = null
    private var delayMillis: Long = 30000

    private val handler = Handler()
    var isWaiting = false
    var isAlert = false
    private var proctorListener: onProctorListener? = null

    private val changeWaitingStatus = object : Runnable {
        override fun run() {
            isWaiting = !isWaiting
            handler.postDelayed(this, delayMillis) // Change color every 30 seconds
        }
    }

    init {
        this.surfaceHolder = holder
        this.surfaceHolder?.addCallback(this)
        handler.post(changeWaitingStatus)
        onStart()
    }

    private fun onStart() {
        WindowUtils(context as AppCompatActivity).doNotLockScreen()
        WindowUtils(context as AppCompatActivity).disableMultiWindow()
//        DNDManagerHelper(context as AppCompatActivity).checkDNDPolicyAccessAndRequest()

    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera?.setDisplayOrientation(90)
//            setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT, camera)
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        run {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    takePic()
                }
            }, 0, 30000) // 1 sec

        }

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if (surfaceHolder?.surface == null) {
            return
        }
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
            thread {
                camera?.startPreview()
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        try {
            releaseCamera()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera?) {

        if (data == null) {
            return
        }

        camera?.let {
            // Convert the data to a bitmap
            val parameters = camera.parameters
            val width = parameters.previewSize.width
            val height = parameters.previewSize.height

            Log.e("TAG", "onPreviewFrame: ")
            faceDetector.process(
                Frame(
                    data, 270, Size(width, height), parameters.previewFormat, LensFacing.FRONT
                )
            )


        }

    }

     override fun onWindowFocusChanged(hasFocus: Boolean)
    {
        if (hasFocus) { // hasFocus is true

            StatusBarLocker.setExpandNotificationDrawer(context, false)
        }

        else {

            if (!hasFocus) {

                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("You can't access status bar while playing quiz..!!")
                    .setMessage("Do you want to exit")
                    .setCancelable(false)
                    .setPositiveButton("Exit",
                        DialogInterface.OnClickListener { dialog, which ->  })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialog, which ->  })
                    .setIcon(android.R.drawable.ic_dialog_dialer)
                    .setCancelable(false)
                    .show()
                StatusBarLocker.setExpandNotificationDrawer(context, false)
            }

        }
    }
    fun takePic() {
        camera?.setPreviewCallback(this@ProctoringSDK)
    }

    fun startProctoring(
        listener: onProctorListener,
    ) {
        proctorListener = listener
        isAlert = true
        syncResults()
    }

    fun changeDelay(delayMillis: Long) {
        this.delayMillis = delayMillis
    }

    private fun syncResults() {
        faceDetector.setonFaceDetectionFailureListener(object :
            FaceDetector.OnProctoringResultListener {

            override fun isRunningDetector(boolean: Boolean?) {
                if (isWaiting) {
                    proctorListener?.isRunningDetector(boolean)
                }
                if (EmulatorDetector().isEmulatorRun()){
                    alert(context as AppCompatActivity,"Emulator ","don't use emulator ")
                }
                DNDManagerHelper(context as AppCompatActivity).checkDNDPolicyAccessAndRequest()

            }

            override fun onVoiceDetected(
                amplitude: Double,
                isNiceDetected: Boolean,
                isRunning: Boolean,
                typeOfVoiceDetected: String,
            ) {
                if (isWaiting) {
                    proctorListener?.onVoiceDetected(
                        amplitude, isNiceDetected, isRunning, typeOfVoiceDetected
                    )
                }
            }

            override fun onSuccess(faceBounds: Int) {
                if (isWaiting) {
                    proctorListener?.onSuccess(faceBounds)
                }
            }

            override fun onFailure(exception: Exception) {
                if (isWaiting) {
                    proctorListener?.onFailure(exception)
                }
            }

            override fun onFaceCount(face: Int) {
                if (isWaiting) {
                    proctorListener?.onFaceCount(face)
                }

                if (isAlert) {
                    when (face) {
                        0 -> {
                            ScreenBrightness(context).heightBrightness(context)
                            hideAlert()
                        }

                        1 -> {
                            ScreenBrightness(context).heightBrightness(context)
                            hideAlert()
                        }

                        else -> {
                            ScreenBrightness(context).lowBrightness(context)
                            alert(context as AppCompatActivity, "'Face Count", "Multiple Face")
                        }
                    }
                }

            }

            override fun onLipMovementDetection(islipmovment: Boolean) {
                if (isWaiting) {
                    proctorListener?.onLipMovementDetection(islipmovment)
                }
                if (isAlert) {
                    if (islipmovment) {
                        alert(context as AppCompatActivity, "Lip Movement ", islipmovment.toString())
                    }
                }
            }

            override fun onObjectDetection(face: String) {
                if (isWaiting) {
                    proctorListener?.onObjectDetection(face)

                }
            }

            override fun onEyeDetectionOnlyOneFace(face: String) {
                if (isWaiting) {
                    proctorListener?.onEyeDetectionOnlyOneFace(face)

                }
                if (isAlert) {
                    if (!check(face)){
                        alert(context as AppCompatActivity, "Eye", face)
                    }
                }
            }

            override fun onUserWallDistanceDetector(distance: Float) {
                if (isWaiting) {
                    proctorListener?.onUserWallDistanceDetector(distance)
                }
                if (isAlert) {
                    // defalut alert
                }
            }

            override fun captureImage(faceDirection: Bitmap?) {
                if (isWaiting) {
                    proctorListener?.captureImage(faceDirection)
                }
            }

        })
    }

    private fun check(face: String): Boolean {
        return when (face) {
            "both eyes are open" -> {

                true

            }

            "both eyes are closed" -> {

                false
            }

            "right eye is open" -> {
                false
            }

            "left eye is open" -> {
                false
            }

            else -> {
                false
            }
        }


    }

    private fun releaseCamera() {
        camera?.apply {
            stopPreview()
            setPreviewCallback(null)
            release()
        }
        camera = null
    }


    @SuppressLint("SuspiciousIndentation")
    fun alert(context: AppCompatActivity, title: String?, message: String?) {
        hideAlert()

        if (alertDialog == null) {
            alertDialog = AlertDialog.Builder(context).create()
        }

        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.custom_dialog, null)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnClose = view.findViewById<Button>(R.id.btnClose)


        btnClose.setOnClickListener {
            alertDialog?.hide()
            alertDialog?.dismiss()
            alertDialog = null
        }


        alertDialog?.apply {
            this.setView(view)
            tvTitle.text = ""
            tvMessage.text = ""
            tvTitle.text = title
            tvMessage.text = message
            this.show()
        }


    }

    private fun hideAlert() {
        alertDialog?.let {
            if (it.isShowing) {
                it.hide()
                it.dismiss()
                alertDialog = null
            }
        }
    }

    fun alertDialogForQuit() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Your session is expired..! Please login again")
        builder.setCancelable(true)
        builder.setPositiveButton(
            Html.fromHtml("<font color='#00A1DB'>" + "OK " + "</font>")
        ) { dialog, which ->
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog?.let {
            dialog!!.show()
        }
    }

    interface onProctorListener {

        fun isRunningDetector(boolean: Boolean?)

        fun onVoiceDetected(
            amplitude: Double,
            isNiceDetected: Boolean,
            isRunning: Boolean,
            typeOfVoiceDetected: String,
        )

        fun onSuccess(faceBounds: Int)
        fun onFailure(exception: Exception)
        fun onFaceCount(face: Int)
        fun onLipMovementDetection(face: Boolean)
        fun onObjectDetection(face: String)
        fun onEyeDetectionOnlyOneFace(face: String)
        fun onUserWallDistanceDetector(distance: Float)

        //        fun onFaceDirectionMovement(faceDirection: String?)
        fun captureImage(faceDirection: Bitmap?)

    }

}


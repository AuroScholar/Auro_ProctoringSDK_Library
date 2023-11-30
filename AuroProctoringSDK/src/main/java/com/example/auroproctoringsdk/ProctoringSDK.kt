package com.example.auroproctoringsdk

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.auroproctoringsdk.copypastestop.ClipboardManagerHelper
import com.example.auroproctoringsdk.detector.FaceDetector
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.LensFacing
import com.example.auroproctoringsdk.dnd.DNDManagerHelper
import com.example.auroproctoringsdk.emulater.EmulatorDetector
import com.example.auroproctoringsdk.notification.ClearAllNotifications
import com.example.auroproctoringsdk.screenBarLock.PinUnpinApp
import com.example.auroproctoringsdk.screenBarLock.StatusBarLocker
import com.example.auroproctoringsdk.screenBrightness.ScreenBrightness
import com.example.auroproctoringsdk.screenBrightness.isScreenReaderOn
import com.example.auroproctoringsdk.screenBrightness.stopTalkBackText
import com.example.auroproctoringsdk.utils.CustomAlertDialog
import com.example.auroproctoringsdk.utils.Utils
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
    private var delayMillis: Long = 30000

    private val handler = Handler()
    var isWaiting = false
    var isAlert = false

    companion object {
        var isViewAvailable = false
    }

    private var proctorListener: onProctorListener? = null

    private val changeWaitingStatus = object : Runnable {
        override fun run() {
            isWaiting = !isWaiting
            handler.postDelayed(this, delayMillis) // Change color every 30 seconds
        }
    }
    var alertDialog1 = CustomAlertDialog(context)

    init {
        this.surfaceHolder = holder
        this.surfaceHolder?.addCallback(this)
        handler.post(changeWaitingStatus)
        Utils().getSaveImageInit(context)
//        view.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);


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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) { // hasFocus is true
            StatusBarLocker.setExpandNotificationDrawer(context, false)
        } else {
            if (!hasFocus) {
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
        faceDetector.noticeDetect(context)
        stopTalkBackText(context)
        ViewCompat.setImportantForAccessibility(
            (context as AppCompatActivity).window.decorView,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
        )
    }


    //For Activity
    fun observeLifecycle(lifecycle: Lifecycle) {
        lifeProcess(lifecycle)
    }

    // For Fragment
    fun LifecycleOwner.observeLifecycle() {
        val lifecycle = this.lifecycle
        lifeProcess(lifecycle)
    }

    private fun lifeProcess(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                isViewAvailable = false
                // Code to execute when the fragment or activity is created
                Log.e("RAMU", "onCreate: ")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                StatusBarLocker.statusBarLock(context)

                // Code to execute when the fragment or activity is started
                Log.e("RAMU", "onStart: ")
                isViewAvailable = true
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                /*                CheckDeveloperMode(context).turnOffDeveloperMode()
                                if (!CheckDeveloperMode(context).isDeveloperModeEnabled()){
                                    DNDManagerHelper(context as AppCompatActivity).checkDNDModeON()
                                }*/
                StatusBarLocker.statusBarLock(context)
                ClipboardManagerHelper(context).clearClipboard()
                Log.e("RAMU", "onResume: ")
                DNDManagerHelper(context).checkDNDModeON()
                isViewAvailable = true

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                isViewAvailable = false
                // Code to execute when the fragment or activity is paused
                Log.e("RAMU", "onPause: ")
                DNDManagerHelper(context).DndModeOff(context)
                hideAlert()

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                isViewAvailable = false
                hideAlert()
                Log.e("RAMU", "onStop: ")

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                Log.e("RAMU", "onDestroy: ")

//                Log.e("TAG", "onDestroy: -- result "+Utils(context).removeDir() )
                isViewAvailable = false
                DNDManagerHelper(context as AppCompatActivity).DndModeOff(context)
            }
        })
    }

    fun changeDelay(delayMillis: Long) {
        this.delayMillis = delayMillis
    }

    fun alertOnOff(): Boolean {
        isAlert = !isAlert
        return isAlert
    }

    private fun syncResults() {

        faceDetector.setonFaceDetectionFailureListener(object :
            FaceDetector.OnProctoringResultListener {

            override fun isRunningDetector(boolean: Boolean?) {
                if (isViewAvailable) { // view is ready

                    if (isWaiting) {
                        proctorListener?.isRunningDetector(boolean)
                    }
                    if (EmulatorDetector().isEmulatorRun()) {
                        alert("Emulator ", "don't use emulator ")
                    }

                    ClearAllNotifications(context as AppCompatActivity)
                    Log.e("TAG", "isRunningDetector: onStateChanged: dnd request ")
                    DNDManagerHelper(context as AppCompatActivity).checkDNDModeON()
                    StatusBarLocker.statusBarLock(context)



                }
            }

            override fun onVoiceDetected(
                amplitude: Double,
                isNiceDetected: Boolean,
                isRunning: Boolean,
                typeOfVoiceDetected: String,
            ) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onVoiceDetected(
                            amplitude, isNiceDetected, isRunning, typeOfVoiceDetected
                        )

                        if (isAlert) {
                            if (isNiceDetected) {
                                (context as AppCompatActivity).runOnUiThread {
//                                    alert("HIGH SOUND", typeOfVoiceDetected)
                                }
                            }
                        }
                    }
                }
            }

            override fun onSuccess(faceBounds: Int) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onSuccess(faceBounds)
                    }
                }
            }

            override fun onFailure(exception: Exception) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onFailure(exception)
                    }
                }
            }

            override fun onFaceCount(face: Int) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onFaceCount(face)
                    }

                    if (isAlert) {
                        when (face) {
                            0 -> {
                                ScreenBrightness(context).heightBrightness(context)
                                alert("Face", "Unable to find face ")
                            }

                            1 -> {
                                ScreenBrightness(context).heightBrightness(context)
                                hideAlert()
                            }

                            else -> {
                                ScreenBrightness(context).lowBrightness(context)
                                alert("'Face Count", "Multiple Face")
                            }
                        }
                    }
                }


            }

            override fun onLipMovementDetection(islipmovment: Boolean) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onLipMovementDetection(islipmovment)
                    }
                    if (isAlert) {
                        if (islipmovment) {
                            /*alert(
                                "Lip Movement ",
                                islipmovment.toString()
                            )*/
                        }
                    }
                }

            }

            override fun onObjectDetection(face: String) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onObjectDetection(face)

                    }
                }

            }

            override fun onEyeDetectionOnlyOneFace(face: String) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onEyeDetectionOnlyOneFace(face)

                    }
                    if (isAlert) {
                        if (!check(face) && !face.isNullOrBlank()) {
                            alert("Eye", face)
                        }
                    }
                }

            }

            override fun onUserWallDistanceDetector(distance: Float) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onUserWallDistanceDetector(distance)
                    }
                    if (isAlert) {
                        // defalut alert
                    }
                }

            }

            override fun onFaceDirectionMovement(faceDirection: String?) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onFaceDirectionMovement(faceDirection)
                    }
                    if (isAlert) {
                        if (!checkFaceDirection(faceDirection)) {
                            alert("Face Direction", faceDirection)
                        }
                    }

                }
            }

            override fun captureImage(faceDirection: Bitmap?) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        if (faceDirection != null) {
                            Log.e(
                                "TAG",
                                "captureImage:-->  " + Utils().saveBitmapIntoImageInternalDir(
                                    faceDirection,
                                    context
                                )
                            )
                        }
                        proctorListener?.captureImage(faceDirection)
                    }
                }
            }

        })
    }

    private fun checkFaceDirection(faceDirection: String?): Boolean {
        return when (faceDirection) {
            "moving to right" -> {
                false
            }

            "moving to left" -> {
                false
            }

            "moving up" -> {
                false
            }

            "moving down" -> {
                false
            }

            else -> {
                true
            }

        }
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
    fun alert(title: String?, message: String?) {
        alertDialog1.show(title.toString(), message.toString())
    }

    private fun hideAlert() {
        alertDialog1.hide()
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
        fun onFaceDirectionMovement(faceDirection: String?)
        fun captureImage(faceDirection: Bitmap?)

    }

}


package com.example.auroproctoringsdk

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.auroproctoringsdk.detector.FaceDetector
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.LensFacing
import com.example.auroproctoringsdk.dnd.DNDManagerHelper
import com.example.auroproctoringsdk.emulater.EmulatorDetector
import com.example.auroproctoringsdk.notification.Notifications
import com.example.auroproctoringsdk.screenBarLock.StatusBarLocker
import com.example.auroproctoringsdk.screenBrightness.ScreenBrightness
import com.example.auroproctoringsdk.utils.CustomAlertDialog
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.thread

class ProctoringSDK(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback, Camera.PreviewCallback {

    private var proctorListener: onProctorResultListener? = null
    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private val faceDetector = FaceDetector()
    private var timer: Timer? = null
    private var delayMillis: Long = 30000
    private val handler = Handler()
    private var isWaiting = false
    private var isAlert = false
    private val changeWaitingStatus = object : Runnable {
        override fun run() {
            isWaiting = !isWaiting
            handler.postDelayed(this, delayMillis) // Change color every 30 seconds
        }
    }
    private var alertDialog1 = CustomAlertDialog(context)

    companion object {
        var isViewAvailable = false
    }

    init {
        this.surfaceHolder = holder
        this.surfaceHolder?.addCallback(this)
        handler.post(changeWaitingStatus)
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
        listener: onProctorResultListener,
    ) {
        proctorListener = listener
        isAlert = true

        syncResults()

    }

    fun initLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                isViewAvailable = false
                // Code to execute when the fragment or activity is created
                Log.e("statue", "onCreate: ")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                StatusBarLocker.statusBarLock(context)

                Log.e("statue", "onStart: ")
                isViewAvailable = true
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                DNDManagerHelper(context as AppCompatActivity).dndModeON()
                StatusBarLocker.statusBarLock(context)
                Log.e("statue", "onResume: ")
                isViewAvailable = true

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                isViewAvailable = false
                // Code to execute when the fragment or activity is paused
                Log.e("statue", "onPause: ")
                hideAlert()
                releaseCamera()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                isViewAvailable = false
                hideAlert()
                releaseCamera()
                Log.e("statue", "onStop: ")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                isViewAvailable = false
                Log.e("statue", "onDestroy: ")
                DNDManagerHelper(context as AppCompatActivity).dndModeOff()
            }
        })
    }


    override fun onDetachedFromWindow() {
        hideAlert()
        super.onDetachedFromWindow()
    }

    fun changeDelay(delayMillis: Long) {
        this.delayMillis = delayMillis
    }

    fun alertOnOff(): Boolean {
        isAlert = !isAlert
        return isAlert
    }

    private fun syncResults() {
        faceDetector.setDetectCallBackListner(object :
            FaceDetector.OnProctoringResultListener {
            override fun isRunningDetector(boolean: Boolean?) {
                if (isViewAvailable) { // view is ready

                    if (isWaiting) {
                        proctorListener?.isRunningDetector(boolean)
                    }
                    if (EmulatorDetector().isEmulatorRun()) {
                        alert("Emulator ", "don't use emulator ")
                    }

                    Log.e("TAG", "isRunningDetector: onStateChanged: dnd request ")
                    DNDManagerHelper(context as AppCompatActivity).dndModeON()
                    StatusBarLocker.statusBarLock(context)
                    Notifications().cancelAllNotifications(context)

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
                                hideAlert()
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
                }

            }

            override fun onObjectDetection(face: ArrayList<String>) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onObjectDetection(face)
                    }
                    if (isAlert && !face.isNotEmpty()) {
                        Log.e("TAG", "onObjectDetection: --> "+face.toString() )
//                        alert("Object Found", face.toString())
                    }
                }

            }

            override fun onEyeDetectionOnlyOneFace(face: String) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onEyeDetectionOnlyOneFace(face)

                    }
                    if (isAlert) {
                        if (!eyeOpenCloseStatus(face)) {
                            // alert("Eye", face)
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
                    if (isAlert && !checkFaceDirection(faceDirection)) {
                        alert("Face Direction", faceDirection)
                    }
                }
            }

            override fun captureImage(faceDirection: Bitmap?) {
                if (isViewAvailable) {
                    if (isWaiting) {
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


    private fun eyeOpenCloseStatus(face: String): Boolean {
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
                true
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

    fun alert(title: String?, message: String?) {
        alertDialog1.show(title.toString(), message.toString())
    }

    private fun hideAlert() {
        alertDialog1.hide()
    }

    interface onProctorResultListener {

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
        fun onObjectDetection(face: ArrayList<String>)
        fun onEyeDetectionOnlyOneFace(face: String)
        fun onUserWallDistanceDetector(distance: Float)
        fun onFaceDirectionMovement(faceDirection: String?)
        fun captureImage(faceDirection: Bitmap?)

    }

}

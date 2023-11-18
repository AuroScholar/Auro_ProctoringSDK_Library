package com.example.auroproctoringsdk

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.hardware.Camera
import android.hardware.usb.UsbManager
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.auroproctoringsdk.Application.Companion.deadlyInMilliseconds
import com.example.auroproctoringsdk.Application.Companion.defaultAlert
import com.example.auroproctoringsdk.Application.Companion.surfaceBoardErrorColor
import com.example.auroproctoringsdk.Application.Companion.surfaceBoardNoColor
import com.example.auroproctoringsdk.Application.Companion.surfaceBoardSuccessColor
import com.example.auroproctoringsdk.detector.FaceDetector
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.LensFacing
import com.example.auroproctoringsdk.developerMode.CheckDeveloperMode
import com.example.auroproctoringsdk.dnd.DNDManagerHelper
import com.example.auroproctoringsdk.emulater.EmulatorDetector
import com.example.auroproctoringsdk.screenBrightness.ScreenBrightness
import com.example.auroproctoringsdk.utils.BottomKeyEvent
import com.example.auroproctoringsdk.utils.Utils
import com.example.auroproctoringsdk.voiceDetector.NoiseDetector
import com.example.auroproctoringsdk.windowFull.WindowUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask


class ProctoringSDK(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private val faceDetector = FaceDetector()

    private var imageBitmap: Bitmap? = null
    private var isDetection = true
    private val imgList = mutableListOf<Bitmap>()
    private val captureImageList = MutableLiveData<List<Bitmap>>()
    private var timer: Timer? = null
    private var defaultAlertDialog = false
    private var saveImageIntoFolder = false


    private var surfaceViewBorder = Paint().also {
        it.color = Color.TRANSPARENT
        it.style = Paint.Style.STROKE
        it.strokeWidth = 15f
        it.isAntiAlias = true
        it.isDither = true
        it.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        it.strokeJoin = Paint.Join.ROUND
        it.strokeCap = Paint.Cap.ROUND
    }
    private var backgroundPaint = Paint().also {
        it.color = Color.TRANSPARENT
        it.style = Paint.Style.FILL
        it.isAntiAlias = true
        it.isDither = true
        it.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        it.strokeJoin = Paint.Join.ROUND
        it.strokeCap = Paint.Cap.ROUND
    }

    private var alertDialog: AlertDialog? = null

    private var usbManager = com.example.auroproctoringsdk.usb.UsbReceiver()
    private var statusBarLocker: com.example.auroproctoringsdk.screenBarLock.StatusBarLocker? = null


    init {

        this.layoutParams = ViewGroup.LayoutParams(300, 300)

        this.setPadding(50, 50, 50, 50)

        this.surfaceHolder = holder
        this.surfaceHolder?.addCallback(this)

        this.imgList.clear()

        setWillNotDraw(false)

    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (isDetection) {
                    takePic()
                }
            }
        }, 0, deadlyInMilliseconds) // 1 sec
        //        }, 0, 60000) // 1 mint
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if (surfaceHolder?.surface == null) {
            return
        }
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {

        if (camera != null) {
            camera?.stopPreview()
            camera?.setPreviewCallback(null)
            camera?.release()
            camera = null
        }

        timer?.cancel()
        timer = null
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            (context as Activity).runOnUiThread {
                if (defaultAlert) {
                    surfaceViewBorder.let { borderPaint ->
                        backgroundPaint.let { backgroundPaint ->
                            val borderRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                            it.drawRect(borderRect, borderPaint)
                            val backgroundRect =
                                RectF(5f, 5f, width.toFloat() - 5f, height.toFloat() - 5f)
                            it.drawRect(backgroundRect, backgroundPaint)
                        }
                    }
                }
            }
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
            val yuvImage = YuvImage(data, parameters.previewFormat, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            val lastUpdatedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            out.flush()
            out.close()
            lastUpdatedBitmap?.let {
                imageBitmap = lastUpdatedBitmap
                if (isDetection) {
                    Log.e("TAG", "onPreviewFrame: status "+isDetection )
//                    imgList.add(lastUpdatedBitmap)
//                    captureImageList.value = imgList.toList()
                    Thread{
                        Utils().saveBitmapIntoImageInternalDir(lastUpdatedBitmap,context,saveImageIntoFolder)
                    }.start()
                    faceDetector.process(
                        Frame(
                            data,
                            270,
                            Size(width, height),
                            parameters.previewFormat,
                            LensFacing.FRONT
                        )
                    )


                }

            }

        }

    }


    fun startProctoring(
        onProctoringResultListener: FaceDetector.OnProctoringResultListener,
    ): Boolean {
        isDetection = true
        faceDetector.setonFaceDetectionFailureListener(onProctoringResultListener)
        NoiseDetector().startNoiseDetector((context as Activity), onProctoringResultListener)
        getFaceLiveResult(context as AppCompatActivity)
        getLifeCycle((context as AppCompatActivity).lifecycle, context as AppCompatActivity)
        return isDetection
    }

    private fun showDialog() {
        /*val builder = AlertDialog.Builder(context)
        builder.setTitle("Dialog Title")
        builder.setMessage("Dialog Message")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()

*/
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> (context as AppCompatActivity).finish() }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()



    }

    private val homeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason == "homekey" || reason == "recentapps") {
                    showDialog()
                }
            }
        }
    }

    private fun getLifeCycle(lifecycle: Lifecycle, activity: AppCompatActivity) {

        lifecycle.addObserver(object : LifecycleEventObserver {

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        Log.e("TAG", "onStateChanged: start")
                        if (defaultAlert) {
                            saveImageIntoFolder = true
                            statusBarLocker =
                                com.example.auroproctoringsdk.screenBarLock.StatusBarLocker(
                                    activity
                                )
                            statusBarLocker?.lock()
                        }

                    }

                    Lifecycle.Event.ON_CREATE -> {
                        saveImageIntoFolder = false

                        val homeIntentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                        activity.registerReceiver(homeReceiver, homeIntentFilter)

                        Log.e("TAG", "onStateChanged: create")
                        if (defaultAlert) {
                            /*// developer mode
                            if (!CheckDeveloperMode(context).isDeveloperModeEnabled()) {
                                CheckDeveloperMode(context).hideDialog()
                            } else {
                                CheckDeveloperMode(context).turnOffDeveloperMode()
                            }

                            if (EmulatorDetector().isEmulatorRun()) {
                                alert(activity, "Emulator", "Don't use Emulator")
                            }

                            //DND
                            DNDManagerHelper(context).checkDNDPolicyAccessAndRequest()

                            // lock
                            WindowUtils(activity).doNotLockScreen()

                            // full screen
                            WindowUtils(activity).hideSystemUI()

                            // multi window
                            activity.onMultiWindowModeChanged(false)
                            WindowUtils(activity).disableMultiWindow()

                            //ExpandNotificationDrawer
                            WindowUtils(activity).setExpandNotificationDrawer(context, false)*/

                            //register usb manger
                            val filter = IntentFilter()
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                            activity.registerReceiver(usbManager, filter)



                        }

                    }

                    Lifecycle.Event.ON_RESUME -> {
                        saveImageIntoFolder = false
                        if (defaultAlert) {

                            // developer mode
                            Log.e(
                                "TAG",
                                " onStateChanged: onresume wala status " + CheckDeveloperMode(
                                    context
                                ).isDeveloperModeEnabled()
                            )

                            if (CheckDeveloperMode(context).isDeveloperModeEnabled()) {
                                CheckDeveloperMode(context).turnOffDeveloperMode()
                            } else {
                                CheckDeveloperMode(context).hideDialog()
                            }


                            if (EmulatorDetector().isEmulatorRun()) {
                                alert(activity, "Emulator", "Don't use Emulator")
                            }

                            // lock
                            WindowUtils(activity).doNotLockScreen()
                            //DND
                            DNDManagerHelper(context).checkDNDPolicyAccessAndRequest()
                            // full screen
                            WindowUtils(activity).hideSystemUI()

                            // multi window
                            activity.onMultiWindowModeChanged(false)
                            WindowUtils(activity).disableMultiWindow()

                            //ExpandNotificationDrawer
                            WindowUtils(activity).setExpandNotificationDrawer(context, false)

                            //usb manger
                            val filter = IntentFilter()
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                            activity.registerReceiver(usbManager, filter)
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        Log.e("TAG", "onStateChanged: pause")
                        if (defaultAlert) {
                            activity.unregisterReceiver(usbManager)
                        }
                        saveImageIntoFolder = false

                    }

                    Lifecycle.Event.ON_STOP -> {
                        Log.e("TAG", "onStateChanged: stop")
                        saveImageIntoFolder = false
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        Log.e("TAG", "onStateChanged: destroy")
                        if (defaultAlert) {
                            statusBarLocker?.release()
                        }
                        releaseCameraAndPreview()
                        activity.unregisterReceiver(homeReceiver)

                        saveImageIntoFolder = true

                    }

                    else -> {}

                }

            }

            fun releaseCameraAndPreview() {
                if (camera != null) {
                    camera?.stopPreview()
                    camera?.setPreviewCallback(null)
                    camera?.release()
                    camera = null
                }
            }
        })

        BottomKeyEvent().onBackPressHandle(activity)

    }


    private fun isAppInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == context.packageName) {
                return true
            }
        }
        return false
    }

    private fun showDialog1() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("App in Background")
        builder.setMessage("Please do not leave the app while in use.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }


    fun getCaptureImagesList(): MutableLiveData<List<Bitmap>> {
        return captureImageList
    }


    private fun updateSurfaceViewBoard(open: Boolean?): Boolean {

        if (open != null) {
            if (open) {
                surfaceViewBorder?.color = surfaceBoardErrorColor
                invalidate()
                postInvalidate()
            } else {
                surfaceViewBorder?.color = surfaceBoardSuccessColor
                invalidate()
                postInvalidate()
            }
            return open
        } else {
            surfaceViewBorder?.color = surfaceBoardNoColor
            invalidate()
            postInvalidate()
            return false
        }

    }

    private fun getFaceLiveResult(activity: AppCompatActivity) {
        faceDetector.liveFaceResult().observe(activity) { liveResult ->
            activity.runOnUiThread {
                if (defaultAlert) {
                    if (liveResult.faceCount == 0) {
                        //brightness
                        ScreenBrightness(activity).setScreenBrightness(false)

                        hide()
                        updateSurfaceViewBoard(null)
                        if (defaultAlertDialog) {
                            alert(
                                activity, "Face Count  ", liveResult.faceCount.toString()
                            )
                        }


                    } else if (liveResult.faceCount == 1) {
                        hide()
                        ScreenBrightness(activity).setScreenBrightness(false)
                        // Face Direction check is user see left or right direction
                        if (!liveResult.faceDirection.isNullOrBlank()) {
                            hide()
                            val result = liveResult.faceDirection
                            if (defaultAlertDialog) {
                                alert(activity, "Face Direction", result)
                            }
                        } else {
                            if (!updateSurfaceViewBoard(liveResult.isMouthOen)) { // return close mouth


                            } else {

                            }
                        }

                    } else {
                        ScreenBrightness(activity).setScreenBrightness(defaultAlert)
                        hide()
                        var count = liveResult.faceCount
                        animateRightToLeft(this)
                        if (defaultAlertDialog) {
                            alert(
                                activity, "Face Count  ", count.toString()
                            )
                        }

                    }

                }


            }
        }

    }

    private fun animateRightToLeft(view: View) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val animation = ObjectAnimator.ofFloat(view, "translationX", screenWidth, 0f)
        animation.duration = 10 // Set the duration of the animation (in milliseconds)
        animation.start()
    }

    fun takePic() {
        camera?.setPreviewCallback(this@ProctoringSDK)
    }

    fun proctoringConfig(
        postprocessingElseThis: FaceDetector.OnProctoringResultListener,
        proctoringSDKStart: Boolean,
        delayInMilisecounds: Long?,
        WorningColor: Int?,
        SuccessColor: Int?,
        useDeFaultAlertDialog: Boolean?,
    ) {
        startProctoring()
        startProctoring(postprocessingElseThis)

        useDeFaultAlertDialog?.let {
            useDefaultAlert(useDeFaultAlertDialog)
        }
        WorningColor?.let {
            surfaceBoardErrorColor = WorningColor
        }
        SuccessColor?.let {
            surfaceBoardSuccessColor = SuccessColor

        }
        delayInMilisecounds?.let {
            proctoringWithDealy(delayInMilisecounds)
        }


    }

    fun useDefaultAlert(isDefaultAlert: Boolean): Boolean {
        defaultAlert = isDefaultAlert
        return defaultAlert
    }

    fun proctoringWithDealy(dealInMilliseconds: Long) {
        deadlyInMilliseconds = dealInMilliseconds
    }

    fun startStopDetection(): Boolean {
        isDetection = !isDetection
        return isDetection
    }

    fun stopProctoring() :Boolean{
        saveImageIntoFolder = false
        isDetection = false
        return isDetection

    }

    fun startProctoring() :Boolean{
        isDetection = true
        saveImageIntoFolder = true
        return isDetection
    }

    fun defaultAlert() : Boolean{
        defaultAlertDialog =! defaultAlertDialog

        return defaultAlertDialog
    }

    fun stopCamera() {
        camera?.stopFaceDetection()
        camera?.stopPreview()
        camera?.stopSmoothZoom()
        camera?.release()
    }

    private fun hide() {
        alertDialog?.let {
            if (it.isShowing) {
                it.hide()
                it.dismiss()
                alertDialog = null
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun alert(context: AppCompatActivity, title: String?, message: String?) {
        hide()
        if (alertDialog == null) {
            alertDialog = AlertDialog.Builder(context).create()
        }

        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.custom_dialog, null)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnClose = view.findViewById<Button>(R.id.btnClose)


        btnClose.setOnClickListener {
            hide()
        }


        alertDialog?.apply {
            this.setView(view)
            tvTitle.text = title
            tvMessage.text = message
            this.show()
        }


    }


}


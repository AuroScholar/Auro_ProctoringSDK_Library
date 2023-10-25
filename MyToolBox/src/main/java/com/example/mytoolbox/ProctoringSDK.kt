package com.example.mytoolbox

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
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
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.mytoolbox.Application.Companion.deadlyInMilliseconds
import com.example.mytoolbox.Application.Companion.defaultAlert
import com.example.mytoolbox.proctoring.FaceDetector
import com.example.mytoolbox.proctoring.Frame
import com.example.mytoolbox.proctoring.LensFacing
import com.example.mytoolbox.usb.UsbReceiver
import com.example.mytoolbox.utils.DNDManagerHelper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Method
import java.util.Timer
import java.util.TimerTask

class ProctoringSDK(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private val faceDetector = FaceDetector()

    private var imageBitmap: Bitmap? = null
    private var isDetection = true
    private val imgList = mutableListOf<Bitmap>()
    private val captureImageList = MutableLiveData<List<Bitmap>>()
    private var timer: Timer? = null

    private var surfaceViewBorder: Paint? = null
    private var backgroundPaint: Paint? = null

    private var alertDialog: AlertDialog? = null

    private var usbManager = UsbReceiver()
    private var statusBarLocker: StatusBarLocker? = null
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.e("TAG", "dispatchKeyEvent: ----- > "+event.toString() )
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            val dialog = AlertDialog.Builder(this as Context)
            dialog.setMessage("Dialog message")
            dialog.setPositiveButton("OK") { _, _ -> }
            dialog.show()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val mDisableCursorHandle = false
        if (event?.actionMasked == MotionEvent.ACTION_UP && mDisableCursorHandle) {
            // Hack to prevent keyboard and insertion handle from showing.
            cancelLongPress()
        }
        return super.onTouchEvent(event)
    }


    init {

        this.layoutParams = ViewGroup.LayoutParams(300, 300)
        this.setPadding(50, 50, 50, 50)
        this.surfaceHolder = holder
        this.surfaceHolder?.addCallback(this)
        this.imgList.clear()
        initSurfaceViewBoarder()


    }

    private fun initSurfaceViewBoarder() {
        setWillNotDraw(false)
        this.surfaceViewBorder = Paint()
        surfaceViewBorder?.let {
            it.color = Color.TRANSPARENT
            it.style = Paint.Style.STROKE
            it.strokeWidth = 15f
            it.isAntiAlias = true
            it.isDither = true
            it.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
        }

        this.backgroundPaint = Paint()
        backgroundPaint?.let {
            it.color = Color.TRANSPARENT
            it.style = Paint.Style.FILL
            it.isAntiAlias = true
            it.isDither = true
            it.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
        }

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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            (context as Activity).runOnUiThread {
                if (defaultAlert) {
                    surfaceViewBorder?.let { borderPaint ->
                        backgroundPaint?.let { backgroundPaint ->
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
//                    imgList.add(lastUpdatedBitmap)
//                    captureImageList.value = imgList.toList()
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
        onProctoringResultListener: FaceDetector.OnProctoringResultListener
    ) {
        isDetection = true
        faceDetector.setonFaceDetectionFailureListener(onProctoringResultListener)
        NoiseDetector().startNoiseDetector((context as Activity), onProctoringResultListener)
        getFaceLiveResult(context as AppCompatActivity)
        getLifeCycle((context as AppCompatActivity).lifecycle, context as AppCompatActivity)
    }

    private fun getLifeCycle(lifecycle: Lifecycle, activity: AppCompatActivity) {


        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {

                when (event) {

                    Lifecycle.Event.ON_START -> {
                        if (defaultAlert) {
                            statusBarLocker = StatusBarLocker(activity)
                            statusBarLocker?.lock()
                        }

                    }

                    Lifecycle.Event.ON_CREATE -> {


                        if (defaultAlert) {
                            // DND notification manager
                            DNDManagerHelper(context).apply {
                                this.checkDNDPolicyAccessAndRequest()
                            }
                            // developer mode
                            turnOffDeveloperMode(context, isDeveloperModeEnable(context))

                            if (isEmulatorRun()) {
                                alert(activity, "Emulator", "Don't use Emulator")
                            }
                            // lock
                            doNotLockScreen(activity)
                            // full screen
//                            enableFullScreen(activity)
                            hideSystemUI()

                            // multi window
                            activity.onMultiWindowModeChanged(false)
                            disableMultiWindow(activity)

                            //ExpandNotificationDrawer
                            setExpandNotificationDrawer(context, false)

                            //usb manger
                            val filter = IntentFilter()
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                            activity.registerReceiver(usbManager, filter)
                        }

                    }

                    Lifecycle.Event.ON_RESUME -> {
                        if (defaultAlert) {
                            // developer mode
                            turnOffDeveloperMode(context, isDeveloperModeEnable(context))
                            if (isEmulatorRun()) {
                                alert(activity, "Emulator", "Don't use Emulator")
                            }
                            // lock
                            doNotLockScreen(activity)
                            // full screen
//                            enableFullScreen(activity)
                            hideSystemUI()

                            // multi window
                            activity.onMultiWindowModeChanged(false)
                            disableMultiWindow(activity)

                            //ExpandNotificationDrawer
                            setExpandNotificationDrawer(context, false)

                            //usb manger
                            val filter = IntentFilter()
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                            activity.registerReceiver(usbManager, filter)
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {

                        if (defaultAlert) {
                            activity.unregisterReceiver(usbManager)
                        }

                    }

                    Lifecycle.Event.ON_STOP -> {}
                    Lifecycle.Event.ON_DESTROY -> {
                        if (defaultAlert) {
                            statusBarLocker?.release()
                        }
                        releaseCameraAndPreview()

                    }

                    else -> {}

                }

            }


            private fun disableMultiWindow(activity: AppCompatActivity) {
                activity.let {
                    it.window.setFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    )
                }

            }

            @SuppressLint("WrongConstant")
            private fun setExpandNotificationDrawer(context: Context, expand: Boolean) {
                try {
                    val statusBarService = context.getSystemService("statusbar")
                    val methodName =
                        if (expand) if (Build.VERSION.SDK_INT >= 22) "expandNotificationsPanel" else "expand"
                        else if (Build.VERSION.SDK_INT >= 22) "collapsePanels" else "collapse"
                    val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
                    val method: Method = statusBarManager.getMethod(methodName)
                    method.invoke(statusBarService)
                } catch (e: Exception) {
                    e.printStackTrace()
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

            private fun isEmulatorRun(): Boolean {
                return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") || Build.FINGERPRINT.startsWith(
                    "generic"
                ) || Build.FINGERPRINT.startsWith("unknown") || Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains(
                    "ranchu"
                ) || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains(
                    "Android SDK built for x86"
                ) || Build.MANUFACTURER.contains("Genymotion") || Build.MANUFACTURER.contains("Google") || Build.PRODUCT.contains(
                    "sdk_google"
                ) || Build.PRODUCT.contains("google_sdk") || Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains(
                    "sdk_x86"
                ) || Build.PRODUCT.contains("vbox86p") || Build.PRODUCT.contains("emulator") || Build.PRODUCT.contains(
                    "simulator"
                ) || Build.PRODUCT.contains("Genymotion") || Build.PRODUCT.contains("Bluestacks"))
            }

            private fun hideSystemUI() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                    val controller = activity.window.decorView.windowInsetsController
                    controller?.hide(WindowInsets.Type.ime())
                    controller?.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    controller?.hide(WindowInsets.Type.systemBars())/*                    val flag = WindowInsets.Type.statusBars()
                                        WindowInsets.Type.navigationBars()
                                        WindowInsets.Type.captionBar()
                                        window?.insetsController?.hide(flag)*/
                } else {
                    //noinspection
                    @Suppress("DEPRECATION")
                    // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
                    activity.window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
                }
            }

            private fun isDeveloperModeEnable(context: Context): Boolean {
                return Settings.Secure.getInt(
                    context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
                ) != 0
            }

            private fun turnOffDeveloperMode(context: Context, developerModeEnable: Boolean) {
                if (developerModeEnable) {
                    try {
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Please Disable Developer Mode")
                            .setMessage("You will not proceed if developer mode is enable")
                            .setPositiveButton(
                                "Go to Settings",
                                DialogInterface.OnClickListener { dialog, which ->
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                    )
                                }).setIcon(android.R.drawable.stat_notify_error)
                            .setCancelable(false).show()
                        Settings.Secure.putInt(
                            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
                        )
                    } catch (e: SecurityException) {
                        // Handle the security exception if necessary
                    }
                }
            }

            private fun doNotLockScreen(activity: AppCompatActivity) {
                activity.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        it.setShowWhenLocked(true)
                        it.setTurnScreenOn(true)
                        val keyguardManager =
                            activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        keyguardManager.requestDismissKeyguard(activity, null)
                    } else {
                        activity.window.addFlags(
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        )
                    }
                }

            }

        })

    }


    fun getCaptureImagesList(): MutableLiveData<List<Bitmap>> {
        return captureImageList
    }


    private fun updateSurfaceViewBoard(open: Boolean?): Boolean {
        if (open != null) {
            if (open) {
                surfaceViewBorder?.color = Color.RED
                invalidate()
                postInvalidate()
            } else {
                surfaceViewBorder?.color = Color.GREEN
                invalidate()
                postInvalidate()
            }
            return open
        } else {
            surfaceViewBorder?.color = Color.TRANSPARENT
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
                        hide()
                        updateSurfaceViewBoard(null)
                        alert(
                            activity, "Face Count  ", liveResult.faceCount.toString()
                        )
                    } else if (liveResult.faceCount == 1) {
                        hide()
                        // Face Direction check is user see left or right direction
                        if (!liveResult.faceDirection.isNullOrBlank()) {
                            hide()
                            val result = liveResult.faceDirection
                            alert(activity, "Face Direction", result)
                        } else {
                            if (!updateSurfaceViewBoard(liveResult.isMouthOen)) { // return close mouth


                            } else {

                            }
                        }

                    } else {
                        hide()
                        var count = liveResult.faceCount
                        animateRightToLeft(this)
                        alert(
                            activity, "Face Count  ", count.toString()
                        )
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

    fun startProctoring(
        onProctoringResultListener: FaceDetector.OnProctoringResultListener,
        activity: AppCompatActivity
    ) {
        isDetection = true
        faceDetector.setonFaceDetectionFailureListener(onProctoringResultListener)
        NoiseDetector().startNoiseDetector(activity, onProctoringResultListener)
        getFaceLiveResult(activity)
        getLifeCycle(activity.lifecycle, activity)
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

        alertDialog?.apply {
            this.setView(view)
            tvTitle.text = title
            tvMessage.text = message
            this.show()
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

    fun stopProctoring() {
        isDetection = false
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

    fun stopCamera() {
        camera?.stopFaceDetection()
        camera?.stopPreview()
        camera?.stopSmoothZoom()
        camera?.release()
    }


}
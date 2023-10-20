package com.example.mytoolbox

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.mytoolbox.proctoring.FaceDetector
import com.example.mytoolbox.proctoring.Frame
import com.example.mytoolbox.proctoring.LensFacing
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Method
import java.util.Timer
import java.util.TimerTask

class ProctoringSDK(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private val faceDetector = FaceDetector()
    private var deadlyInMilliseconds: Long = 30000
    private var imageBitmap: Bitmap? = null
    private var isDetection = true
    private val imgList = mutableListOf<Bitmap>()
    private val captureImageList = MutableLiveData<List<Bitmap>>()
    private var timer: Timer? = null

    private var surfaceViewBorder: Paint? = null
    private var backgroundPaint: Paint? = null

    private val alertDialog = AlertDialog.Builder(context).create()
    private var defaultAlert: Boolean = true


    init {
        this.layoutParams = ViewGroup.LayoutParams(300, 300)
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
            it.strokeWidth = 10f
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
        }/* try {
             camera?.stopPreview()
             camera?.takePicture(null, null, null)

         } catch (e: Exception) {
             e.printStackTrace()
         }*/
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null

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

        // Fixing the NullPointerException
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
        usbReceiver(activity.lifecycle, activity)
    }

    private fun usbReceiver(lifecycle: Lifecycle, activity: AppCompatActivity) {

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {

                    Lifecycle.Event.ON_START -> {

                    }

                    Lifecycle.Event.ON_CREATE -> {

                        if (defaultAlert) {
                            // developer mode
                            turnOffDeveloperMode(context, isDeveloperModeEnable(context))
                            if (isEmulatorRun()) {
                                alert(activity, "Emulator", "Don't use Emulator")
                            }
                            // lock
                            doNotLockScreen(activity)

                            // full screen
                            enableFullScreen(activity)
                            hideStatusBar(activity)
                            hideSystemUI(activity)

                            // multi window
                            activity.onMultiWindowModeChanged(false)
                            DisableMultiWindow(activity)

                            //ExpandNotificationDrawer
                            setExpandNotificationDrawer(context, false)
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
                            enableFullScreen(activity)
                            hideStatusBar(activity)
                            hideSystemUI(activity)

                            // multi window
                            activity.onMultiWindowModeChanged(false)
                            DisableMultiWindow(activity)

                            //ExpandNotificationDrawer
                            setExpandNotificationDrawer(context, false)
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {}
                    Lifecycle.Event.ON_STOP -> {}
                    Lifecycle.Event.ON_DESTROY -> {}
                    else -> {}

                }

            }

            private fun DisableMultiWindow(activity: AppCompatActivity) {
                activity.let {
                    it.window?.setFlags(
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    )
                }
                activity.let {
                    it.window?.decorView?.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                            )
                }
            }

            @SuppressLint("WrongConstant")
            private fun setExpandNotificationDrawer(context: Context, expand: Boolean) {
                try {
                    val statusBarService = context.getSystemService("statusbar")
                    val methodName =
                        if (expand)
                            if (Build.VERSION.SDK_INT >= 22) "expandNotificationsPanel" else "expand"
                        else
                            if (Build.VERSION.SDK_INT >= 22) "collapsePanels" else "collapse"
                    val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
                    val method: Method = statusBarManager.getMethod(methodName)
                    method.invoke(statusBarService)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            private fun isEmulatorRun(): Boolean {
                return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.HARDWARE.contains("goldfish")
                        || Build.HARDWARE.contains("ranchu")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")
                        || Build.MANUFACTURER.contains("Genymotion")
                        || Build.MANUFACTURER.contains("Google")
                        || Build.PRODUCT.contains("sdk_google")
                        || Build.PRODUCT.contains("google_sdk")
                        || Build.PRODUCT.contains("sdk")
                        || Build.PRODUCT.contains("sdk_x86")
                        || Build.PRODUCT.contains("vbox86p")
                        || Build.PRODUCT.contains("emulator")
                        || Build.PRODUCT.contains("simulator")
                        || Build.PRODUCT.contains("Genymotion")
                        || Build.PRODUCT.contains("Bluestacks"))
            }

            private fun hideSystemUI(activity: AppCompatActivity) {
                if (activity.supportActionBar != null) {
                    activity.supportActionBar!!.hide()
                }
                activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
            }

            private fun hideStatusBar(activity: Activity) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    activity.window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            )
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }

            private fun enableFullScreen(activity: Activity) {
                // Hide the status bar
                activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                        )
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                // Hide the navigation bar (optional, depending on your use case)
                activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


                // Set the window to full-screen mode
                val window: Window = activity.window
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN

                )
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            }

            private fun isDeveloperModeEnable(context: Context): Boolean {
                return Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                    0
                ) != 0
            }

            private fun turnOffDeveloperMode(context: Context, developerModeEnable: Boolean) {
                if (developerModeEnable) {
                    try {
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Please Disable Developer Mode")
                            .setMessage("You will not proceed if developer mode is enable")
                            .setPositiveButton("Go to Settings",
                                DialogInterface.OnClickListener { dialog, which ->
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                    )
                                })
                            .setIcon(android.R.drawable.stat_notify_error)
                            .setCancelable(false)
                            .show()
                        Settings.Secure.putInt(
                            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
                        )
                    } catch (e: SecurityException) {
                        // Handle the security exception if necessary
                    }
                }
            }

            private fun doNotLockScreen(activity: AppCompatActivity) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }

        })

    }

    fun proctoringWithDealy(dealInMilliseconds: Long) {
        this.deadlyInMilliseconds = dealInMilliseconds
    }

    fun getCaptureImagesList(): MutableLiveData<List<Bitmap>> {
        return captureImageList
    }

    private fun getFaceLiveResult(activity: AppCompatActivity) {
        faceDetector.getLiveFaceResult().observe(activity) { liveResult ->
            activity.runOnUiThread {
                if (defaultAlert) {
                    if (liveResult.faceCount == 0) {
                        updateSurfaceViewBoard(null)
                        alert(
                            activity, "Face Count  ", liveResult.faceCount.toString()
                        )
                    } else if (liveResult.faceCount == 1) {
                        hide()
                        if (updateSurfaceViewBoard(liveResult.isMouthOen)) {

                        } else {

                        }

                    } else {
                        animateRightToLeft(this)
                        alert(
                            activity, "Face Count  ", liveResult.faceCount.toString()
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

    fun useDefaultAlert(isDefaultAlert: Boolean): Boolean {
        defaultAlert = isDefaultAlert
        return defaultAlert
    }

    fun startStopDetection(): Boolean {
        isDetection = !isDetection
        return isDetection
    }

    fun stopProctoring() {
        isDetection = false
    }

    @SuppressLint("SuspiciousIndentation")
    private fun alert(context: AppCompatActivity, title: String?, message: String?) {
        hide()
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.custom_dialog, null)
        /*alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )*/
        alertDialog.setCancelable(false)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        alertDialog.setView(view)
        tvTitle.text = title
        tvMessage.text = message
        alertDialog.show()

    }

    private fun hide() {
        if (alertDialog.isShowing) {
            alertDialog.hide()
        }
    }


}
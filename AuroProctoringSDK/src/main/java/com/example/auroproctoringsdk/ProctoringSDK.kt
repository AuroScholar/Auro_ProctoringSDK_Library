package com.example.auroproctoringsdk

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.auroproctoringsdk.copypastestop.ClipboardManagerHelper
import com.example.auroproctoringsdk.detector.FaceDetector
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.LensFacing
import com.example.auroproctoringsdk.developerMode.CheckDeveloperMode
import com.example.auroproctoringsdk.dnd.DNDManagerHelper
import com.example.auroproctoringsdk.emulater.EmulatorDetector
import com.example.auroproctoringsdk.languageSetup.CurrentLanguage
import com.example.auroproctoringsdk.screenBarLock.StatusBarLocker
import com.example.auroproctoringsdk.screenBrightness.ScreenBrightness
import com.example.auroproctoringsdk.screenReader.StopTextReading
import com.example.auroproctoringsdk.utils.CustomAlertDialog
import com.example.auroproctoringsdk.utils.Utils
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.timerTask


class ProctoringSDK(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback ,
    Camera.PreviewCallback{
    companion object{
        private var isViewAvailable = false
        var isWaiting = false
        var isAlert = false
    }
    private val faceDetector = FaceDetector()
    private var camera: Camera? = null
    private var thread: CustomSurfaceThread? = null
    private var timer: Timer? = null
    // callback
    private var proctorListener: onProctorListener? = null
    //face count

    var faceCountWorring2Times = -1
    //control setup

    private var controls = Controls()
    private val changeWaitingStatus = object : Runnable {
        override fun run() {
            isWaiting = !isWaiting
            controls.getControls().isWaitingDelayInMillis.let {
                handler.postDelayed(
                    this, it/*isWaitingDelayInMillis*/
                )
            }
        }
    }
    var alertDialog1 = CustomAlertDialog(context)


    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        openCamera()
        startPreview()

        thread = CustomSurfaceThread(holder)
        thread?.start()

        startImageCaptureTimer()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Implement surfaceChanged logic here
        if (holder.surface == null) {
            return
        }
        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        /*        camera?.stopPreview()
                camera?.release()
                camera = null*/

        stopPreview()


        var retry = true
        thread?.setRunning(false)
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        stopImageCaptureTimer()
    }

    private fun findFrontCamera(): Int {
        val cameraInfo = Camera.CameraInfo()
        val cameraCount = Camera.getNumberOfCameras()
        for (cameraId in 0 until cameraCount) {
            Camera.getCameraInfo(cameraId, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return cameraId
            }
        }
        return 0
    }

    private fun startImageCaptureTimer() {
     /*   timer = Timer()
        timer?.schedule(timerTask {
            captureImage()
        }, 0, 500) // Capture image every 30 seconds*/

        //real time image create
        run {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    captureImage()
                }
            }, 0, 500) // 1 sec

        }

    }

    private fun stopImageCaptureTimer() {
        timer?.cancel()
        timer = null
    }

    private fun captureImage() {
        // Implement image capture logic here

        camera?.setPreviewCallback(this@ProctoringSDK)
        camera?.setPreviewCallback(Camera.PreviewCallback { data, camera ->
            // Process the preview data
           // val bitmap  = context.getDrawable(R.drawable.testbitmap)?.toBitmap()
            faceDetector.process(
                Frame(
                    data,
                    270,
                    Size(camera.parameters.previewSize.width, camera.parameters.previewSize.height),
                    camera.parameters.previewFormat,
                    LensFacing.FRONT
                )/*,bitmap*/
            )




        })

    }
    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return bitmap
        }

        val width = if (bounds.isEmpty) intrinsicWidth else bounds.width()
        val height = if (bounds.isEmpty) intrinsicHeight else bounds.height()

        return Bitmap.createBitmap(width.nonZero(), height.nonZero(), Bitmap.Config.ARGB_8888).also {
            val canvas = Canvas(it)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }
    private fun Int.nonZero() = if (this <= 0) 1 else this


    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) { // hasFocus is true
            // notification panel closed
            StatusBarLocker.setExpandNotificationDrawer(context, false)
        } else {
            if (!hasFocus) {
                StatusBarLocker.setExpandNotificationDrawer(context, false)
            }
        }
    }

    private fun openCamera() {
        try {
            val cameraId = findFrontCamera()
            camera = Camera.open(cameraId)
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()

            /*    // Open the camera // backup code
                camera = Camera.open()
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                camera?.setDisplayOrientation(90)
    //            setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT, camera)
                camera?.setPreviewDisplay(holder)
                camera?.startPreview()*/


            // Do something with the camera, such as setting up the preview
            // ...

        } catch (e: Exception) {
            // Handle any exceptions that occur while opening the camera
            e.printStackTrace()
        }
    }

    private fun startPreview() {
        try {
            // Check if the camera is already opened
            if (camera == null) {
                // Open the camera if it's not already opened
                openCamera()
            }

            // Start the camera preview
            camera?.startPreview()

        } catch (e: Exception) {
            // Handle any exceptions that occur while starting the preview
            e.printStackTrace()
        }
    }

    private fun stopPreview() {
        try {
            // Stop the camera preview
            camera?.stopPreview()

            // Release the camera
            releaseCamera()

        } catch (e: Exception) {
            // Handle any exceptions that occur while stopping the preview
            e.printStackTrace()
        }
    }

    private fun releaseCamera() {
        timer?.cancel()
        camera?.apply {
            stopPreview()
            setPreviewCallback(null)
            release()
        }
        camera = null
    }


    /**
     * Observe lifecycle
     *
     * @param lifecycle
     *///For Activity
    fun observeLifecycle(lifecycle: Lifecycle) {
        lifeProcess(lifecycle)
    }

    /**
     * Observe lifecycle
     *
     */// For Fragment
    fun LifecycleOwner.observeLifecycle() {
        val lifecycle = this.lifecycle
        lifeProcess(lifecycle)
    }

    private fun lifeProcess(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
               isViewAvailable = true
                // Code to execute when the fragment or activity is created
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                if (controls.getControls().isStatusBarLock) {
                    StatusBarLocker.statusBarLock(context)
                    Log.e("Status", "onStart: ")
                }
               isViewAvailable = true

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                Log.e("RAMU", "onResume: ")
                if (controls.getControls().isDeveloperModeOn) {
                    Log.e("TAG", "onResume: developmer ccode "+CheckDeveloperMode(context).isDeveloperModeEnabled() )
                    CheckDeveloperMode(context).turnOffDeveloperMode()
                    if (CheckDeveloperMode(context).isDeveloperModeEnabled()) {
                        alert("Developer Mode", "Developer Mode off ")
                    }
                }

                if (controls.getControls().isStatusBarLock) { // lock status bar

                    StatusBarLocker.statusBarLock(context)

                }

                if (!controls.getControls().isCopyPaste) { // stop copy past

                    ClipboardManagerHelper(context).clearClipboard()

                }

                if (controls.getControls().isDndStatusOn) { // DND on
                    DNDManagerHelper(context).checkDNDModeON()
                }
                isViewAvailable = true

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                alertDialog1.hideForcefully()
                if (controls.getControls().isDndStatusOn) { // DND off
                    DNDManagerHelper(context).DndModeOff(context)
//                    hideAlert()
                }
                // Code to execute when the fragment or activity is paused
                Log.e("RAMU", "onPause: ")
                isViewAvailable = false
                releaseCamera()


            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                alertDialog1.hideForcefully()
                if (controls.getControls().isDndStatusOn) {

                    DNDManagerHelper(context).DndModeOff(context)
                    hideAlert()
                    Log.e("RAMU", "onStop: ")
                }
                isViewAvailable = false


            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                alertDialog1.hideForcefully()
                Log.e("RAMU", "onDestroy: ")

//                Log.e("TAG", "onDestroy: -- result "+Utils(context).removeDir() )
                if (controls.getControls().isDndStatusOn) { // DND off
                    DNDManagerHelper(context as AppCompatActivity).DndModeOff(context)
                }
                isViewAvailable = false

            }
        })
    }

    /**
     * Alert
     *
     * @param title
     * @param message
     */
    @SuppressLint("SuspiciousIndentation")
    fun alert(title: String?, message: String?) {
        alertDialog1.show(title.toString(), message.toString())
    }

    private fun hideAlert() {
        alertDialog1.hide()
    }

    /**
     * Alert on off
     *
     * @return
     */
    fun alertOnOff(): Boolean {
        isAlert = !isAlert
        controls.updateControl(ControlModel(isAlert = isAlert, isProctoringStart = isAlert))
        return isAlert
    }

    /**
     * Stop proctoring
     *
     */
    fun stopProctoring() {
        Log.e("TAG", "stopProctoring: " + controls.getControls().blockedEmulatorDevicesList)
        controls.updateControl(ControlModel(isAlert = false, isProctoringStart = false))
    }

    fun updateControl(controlModel: ControlModel?): ControlModel {
        return if (controlModel != null) {
            controls.updateControl(controlModel)
            controls.getControls()
        } else {
            controls.getControls()
        }
    }

    fun getControl(): ControlModel = controls.getControls()

    fun startProctoring(
        listener: onProctorListener, controlModel: ControlModel?,
    ) {
        proctorListener = listener

        isAlert = true


        // control update by model class
        if (controlModel != null) {
            controls.updateControl(controlModel)

            if (controls.getControls().isAlert) {
                syncResults()
                faceDetector.noticeDetect(context)
                Utils().getSaveImageInit(context)
            }
        } else {

            // Default controller setup
            controls.updateControl(ControlModel(isAlert = true, isProctoringStart = true))
            if (controls.getControls().isAlert) {
                syncResults()
                faceDetector.noticeDetect(context)
                Utils().getSaveImageInit(context)
            }

        }

        CurrentLanguage().setLocale(
            CurrentLanguage().getCurrentLocalizationLanguageCode(context), context
        )

        if (controls.getControls().isStopScreenRecording) {
            StopTextReading().stopTextReading(context)
            // StopTextReadingFragment().stopTextReading(context)
        }

    }
    private fun syncResults() {
        Log.e("TAG", "startProctoring: syncresult call huaaa  syncResults() ")
        faceDetector.setonFaceDetectionFailureListener(object :
            FaceDetector.OnProctoringResultListener {

            override fun isRunningDetector(boolean: Boolean?) {
                Log.e("TAG", "isRunningDetector: running "+boolean)
                if (ProctoringSDK.isViewAvailable) { // view is ready

                    if (isWaiting) {
                        proctorListener?.isRunningDetector(boolean)
                    }

                    if (controls.getControls().isDeveloperModeOn) {
                        CheckDeveloperMode(context).turnOffDeveloperMode()
                        if (CheckDeveloperMode(context).isDeveloperModeEnabled()) {
                            alert("Developer Mode", "Developer Mode off ")
                        }
                    }

                    if (controls.getControls().isDndStatusOn && !CheckDeveloperMode(context).isDeveloperModeEnabled()) { // DND on
                        DNDManagerHelper(context).checkDNDModeON()
                    }

                    if (controls.getControls().isAlert && controls.getControls().isAlertEmulatorDetector && EmulatorDetector().isEmulatorRunning()) {
                        val emulator =
                            context.getString(R.string.Unable_to_use_mulator_on_the_system_while_taking_quizzes)
                                .split("[:]".toRegex())

                        if (emulator.size == 2){

                            alert(emulator[0], emulator[1])

                        }

                    }
                    if (controls.getControls().isAlert && controls.getControls().isDndStatusOn) {
//                    Log.e("TAG", "isRunningDetector: onStateChanged: dnd request ")
                        DNDManagerHelper(context as AppCompatActivity).checkDNDModeON()
                    }
                    if (controls.getControls().isAlert && controls.getControls().isStatusBarLock) {
                        StatusBarLocker.statusBarLock(context)

                    }

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

                        if (controls.getControls().isAlert && controls.getControls().isAlertVoiceDetection) {
                            if (isNiceDetected) {
                                /*(context as AppCompatActivity).runOnUiThread {
                                    alert("HIGH SOUND", typeOfVoiceDetected)
                                }*/
                            }
                        }
                    }
                }
            }

            override fun onSuccess(faceBounds: Int) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
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

                    if (controls.getControls().isAlert) {
                        when (face) {
                            0 -> {
                                faceCountWorring2Times++
                                if (controls.getControls().isAlertFaceNotFound && faceCountWorring2Times >= 2) {
                                    faceCountWorring2Times = -1
                                    val faceNotFoundException =
                                        context.getString(R.string.face_not_found)
                                            .split("[:]".toRegex())

                                    if (faceNotFoundException.size == 2){

                                        alert(faceNotFoundException[0], faceNotFoundException[1])

                                    }

                                    ScreenBrightness(context).heightBrightness(context)/* val filter = controls.getControls().multipleFaceDetectionError.split(":").toTypedArray()*/

                                }

                            }

                            1 -> {
                                ScreenBrightness(context).heightBrightness(context)
                                hideAlert()
                            }

                            else -> {
                                if (controls.getControls().isAlertMultipleFaceCount) {

                                    ScreenBrightness(context).lowBrightness(context)

                                    val filter = context.getString(R.string.Multiple_face_detection)
                                        .split("[:]".toRegex())

                                    if (filter.size == 2){
                                        alert(
                                            filter[0], filter[1]
                                        )
                                    }


                                }
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
                    if (controls.getControls().isAlert && controls.getControls().isAlertLipMovement) {
                        if (islipmovment) {
                            val lipFilter =
                                context.getString(R.string.Lip_movement_eyeball_tracking)
                                    .split("[:]".toRegex())

                            if (lipFilter.size == 2){
                                alert(lipFilter[0], lipFilter[1])
                            }

                        }
                    }
                }

            }

            override fun onObjectDetection(objectList: ArrayList<String>, size: Int?) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onObjectDetection(objectList)
                    }

                    val data = checkObject(objectList, controls.getControls().isBlockedObjectList)

                    if (controls.getControls().isAlert && controls.getControls().isAlertObjectDetection && !data.isNullOrEmpty()) {
                        val objectAlert =
                            context.getString(R.string.object_not_allowed).split("[:]".toRegex())

                        if (objectAlert.size == 2){
                            alert(objectAlert[0], objectAlert[1])
                        }

                        /*
                             val filter = controls.getControls().lipOrEyeTrackingError.split(":").toTypedArray()
                                                    alert(filter.first(),filter.last())*/
                    }

                    /*   if (size !in listOf(0, 1, null)) {
                           if (controls.getControls().isAlertMultipleFaceCount) {
                               val filter = context.getString(R.string.Multiple_face_detection)
                                   .split("[:]".toRegex())

                               if (filter.size == 2){
                                   alert(
                                       filter[0], filter[1]
                                   )
                               }

                           }
                       }*/

                }

            }

            override fun onEyeDetectionOnlyOneFace(face: String) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onEyeDetectionOnlyOneFace(face)

                    }
                    if (controls.getControls().isAlert && controls.getControls().isAlertEyeDetection) {
                        // eye movement stopped by sir facing quick eye open and close
                        if (!check(face) && !face.isNullOrBlank()) {

                            alert("Eye", face)
                            val filter =
                                context.getString(R.string.Lip_movement_eyeball_tracking).split("[:]".toRegex())

                            if (filter.size == 2) {
                                alert(filter[0], filter[1])
                            }
                        }
                    }
                }

            }

            override fun onUserWallDistanceDetector(distance: Float) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onUserWallDistanceDetector(distance)
                    }
                    if (controls.getControls().isAlert && controls.getControls().isAlertUserWallDistanceDetector) {
                        // defalut alert
                    }
                }

            }

            override fun onFaceDirectionMovement(faceDirection: String?) {
                if (isViewAvailable) {
                    if (isWaiting) {
                        proctorListener?.onFaceDirectionMovement(faceDirection)
                    }

                    if (controls.getControls().isAlert && controls.getControls().isAlertFaceDirectionMovement) {
                        if (!checkFaceDirection(faceDirection) && faceDirection!=null) {
//                            alert("alerts", faceDirection)
                            val headDirection =
                                context.getString(R.string.moving_face_left_or_right_during_assesment)
                                    .split("[:]".toRegex())

                            if (headDirection.size == 2) {
                                alert(headDirection[0], headDirection[1])
                            }

                        }
                    }

                }
            }

            override fun captureImage(faceDirection: Bitmap?) {
                faceDirection?.let {


                    /*val imageList = BitmapUtils().getSaveImaged(context,context.resources.getDrawable(R.drawable.testbitmap).toBitmap(),it)

                    runBlocking(){
                        faceCompareProcess(imageList)
                    }*/




                }

                if (isViewAvailable) {
                    if (isWaiting) {
                        if (faceDirection != null && controls.getControls().isCaptureImage) {/*Log.e(
                                "TAG",
                                "captureImage:-->  " + Utils().saveBitmapIntoImageInternalDir(
                                    faceDirection,
                                    context
                                )
                            )*/

                            proctorListener?.captureImage(faceDirection)
                        }

                        if (controls.getControls().isSaveImageHideFolder) {  // hide image into local folder
                            if (faceDirection != null) {
                                Utils().saveBitmapIntoImageInternalDir(faceDirection, context)
                            }
                        }

                    }
                }

            }

        })

    }
    private fun checkObject(face: ArrayList<String>, blockedDeviceList: List<String>?): String? {
        return blockedDeviceList?.firstOrNull { face.contains(it) }
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

    interface onProctorListener {

        /**
         * Is running detector
         *
         * @param boolean
         */
        fun isRunningDetector(boolean: Boolean?)

        /**
         * On voice detected
         *
         * @param amplitude
         * @param isNiceDetected
         * @param isRunning
         * @param typeOfVoiceDetected
         */
        fun onVoiceDetected(
            amplitude: Double,
            isNiceDetected: Boolean,
            isRunning: Boolean,
            typeOfVoiceDetected: String,
        )

        /**
         * On success
         *
         * @param faceBounds
         */
        fun onSuccess(faceBounds: Int)

        /**
         * On failure
         *
         * @param exception
         */
        fun onFailure(exception: Exception)

        /**
         * On face count
         *
         * @param face
         */
        fun onFaceCount(face: Int)

        /**
         * On lip movement detection
         *
         * @param face
         */
        fun onLipMovementDetection(face: Boolean)

        /**
         * On object detection
         *
         * @param face
         */
        fun onObjectDetection(face: ArrayList<String>)

        /**
         * On eye detection only one face
         *
         * @param face
         */
        fun onEyeDetectionOnlyOneFace(face: String)

        /**
         * On user wall distance detector
         *
         * @param distance
         */
        fun onUserWallDistanceDetector(distance: Float)

        /**
         * On face direction movement
         *
         * @param faceDirection
         */
        fun onFaceDirectionMovement(faceDirection: String?)

        /**
         * Capture image
         *
         * @param faceDirection
         */
        fun captureImage(faceDirection: Bitmap?)

    }
}


class CustomSurfaceThread(private val surfaceHolder: SurfaceHolder) : Thread() {
    private var running = false

    fun setRunning(isRunning: Boolean) {
        running = isRunning
    }

    override fun run() {
        while (running) {
            // Implement drawing logic here

            Log.e("TAG", "run: is camera running or not "+running )

        }
    }
}
package com.example.auroproctoringsdk

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.auroproctoringsdk.copypastestop.ClipboardManagerHelper
import com.example.auroproctoringsdk.detector.FaceDetector
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.LensFacing
import com.example.auroproctoringsdk.developerMode.CheckDeveloperMode
import com.example.auroproctoringsdk.dnd.DNDManager
import com.example.auroproctoringsdk.emulater.EmulatorDetector
import com.example.auroproctoringsdk.languageSetup.CurrentLanguage
import com.example.auroproctoringsdk.model.ControlModel
import com.example.auroproctoringsdk.screenBarLock.StatusBarLocker
import com.example.auroproctoringsdk.screenReader.StopTextReading
import com.example.auroproctoringsdk.utils.CustomAlertDialog
import com.example.auroproctoringsdk.utils.Utils
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class ProctoringSDK(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Camera.PreviewCallback {
    companion object {
        private var isViewAvailable = false
        private var isDNDManagerRequest = false
        var isWaiting = false
        var isAlert = false
    }

    private val faceDetector = FaceDetector()
    private var camera: Camera? = null
    private var thread: CustomSurfaceThread? = null
    private var timer: Timer? = null
    var lastCaptureTime = 0L


    // callback
    private var proctorListener: onProctorListener? = null
    //face count

    var faceCountWorring2Times = -1
    //control setup

    private var controls = Controls()

    var alertDialog1 = CustomAlertDialog(context)
    var timeList = ArrayList<Long>()
    var timeListString = ArrayList<String>()

    fun resultTime(): ArrayList<Long> {
        timeListString.clear()
        val currentTimeMillis = System.currentTimeMillis()
        val nextTenMinutes = currentTimeMillis + (10 * 60 * 1000)
        val tenSecondsInMillis = 10 * 1000
        val numberOfBreakpoints = (nextTenMinutes - currentTimeMillis) / tenSecondsInMillis

        val breakpoints = ArrayList<Long>()
        for (i in 0 until numberOfBreakpoints) {
            breakpoints.add(currentTimeMillis + (i * tenSecondsInMillis))
            timeListString.add(convertIntoTime(currentTimeMillis + (i * tenSecondsInMillis)))

        }
        timeList.addAll(breakpoints)

        return breakpoints
    }
    init {
        timeList.clear()
        holder.addCallback(this)
        resultTime()
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
        //real time image create
        run {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    captureImage()
                }
            }, 0, 1000) // 1 sec

        }

    }

    private fun stopImageCaptureTimer() {
        timer?.cancel()
        timer = null
    }

    fun captureImage() {
        // Implement image capture logic here

        camera?.setPreviewCallback(this@ProctoringSDK)
        camera?.setPreviewCallback(Camera.PreviewCallback { data, camera ->
            faceDetector.process(
                Frame(
                    data,
                    270,
                    Size(camera.parameters.previewSize.width, camera.parameters.previewSize.height),
                    camera.parameters.previewFormat,
                    LensFacing.FRONT
                )
            )
        })

    }

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
                Log.e("RAMU", "onCreate: ")
                // Code to execute when the fragment or activity is created


            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                if (controls.getControls().isStatusBarLock) {
                    StatusBarLocker.statusBarLock(context)
                    Log.e("RAMU", "onStart: ")
                }

                //update code
                if (controls.getControls().isDndStatusOn && isDNDManagerRequest) { // check DND not on
                    if (DNDManager(context).checkDndPermission()) {
                        DNDManager(context).checkDNDModeON()
                    } else {
                        DNDManager(context).enableDoNotDisturb(context)
                    }
                }

                isViewAvailable = true
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                isViewAvailable = true

                Log.e("RAMU", "onResume: ")
                if (controls.getControls().isDeveloperModeOn) {
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

                DNDManager(context).checkAndHideAlertDialog(context)  // if permission is allowed then hide alert


            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                alertDialog1.hideForcefully()
                if (controls.getControls().isDndStatusOn) { // DND off
                    DNDManager(context).dndAlertDialogHide()
                    DNDManager(context).DndModeOff(context)
                    hideAlert()
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
                    DNDManager(context).dndAlertDialogHide()
                    DNDManager(context).DndModeOff(context)
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
                    DNDManager(context).dndAlertDialogHide()
                    DNDManager(context).DndModeOff(context)
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

        Log.e("RAMU", "startProctoring: ")
        //update code
        if (controls.getControls().isDndStatusOn && !isDNDManagerRequest) { // check DND not on
            if (DNDManager(context).checkDndPermission()) {
                DNDManager(context).checkDNDModeON()
            } else {
                DNDManager(context).enableDoNotDisturb(context)
                isDNDManagerRequest = true

            }
        }


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
        }

    }

    fun convertIntoTime(milliseconds: Long): String {
        val currentTime = Date(milliseconds)

        val calendar = Calendar.getInstance()
        calendar.time = currentTime

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        println("Current Time: $hour:$minute:$second")

        // Compare with current time
        val currentTimeMillis = calendar.timeInMillis
        if (milliseconds == currentTimeMillis) {
            println("Milliseconds and current time are equal.")
        } else if (milliseconds > currentTimeMillis) {
            println("Milliseconds is greater than current time.")
        } else {
            println("Milliseconds is less than current time.")
        }
        return "$hour:$minute:$second"
    }


    private fun syncResults() {

        //getTimeInMile(timeList.first())

        var tempListTime = ArrayList<String>()

        tempListTime.clear()
        timeList.forEach { ll ->
            tempListTime.add(convertIntoTime(ll))
        }
        println("MyTime" + tempListTime)

        faceDetector.setonFaceDetectionFailureListener(object :
            FaceDetector.OnProctoringResultListener {

            override fun isRunningDetector(boolean: Boolean?) {


               isWaiting =  isProcessOfWaiting()

                if (isViewAvailable && controls.getControls().isProctoringStart) { // view is ready

                    if (isWaiting) {
                        proctorListener?.isRunningDetector(boolean)
                    }

                    if (controls.getControls().isAlert && controls.getControls().isDeveloperModeOn) {
                        if (CheckDeveloperMode(context).isDeveloperModeEnabled()) {
                            alert("Developer Mode", "Developer Mode off ")
                        }
                    }

                    if (controls.getControls().isDndStatusOn) { // check DND not on
                        if (DNDManager(context).checkDndPermission()) {
                            DNDManager(context).checkDNDModeON()
                        }
                    }


                    if (controls.getControls().isAlert && controls.getControls().isAlertEmulatorDetector && EmulatorDetector().isEmulatorRunning()) {
                        val emulator =
                            context.getString(R.string.Unable_to_use_mulator_on_the_system_while_taking_quizzes)
                                .split("[:]".toRegex())

                        if (emulator.size == 2 && DNDManager(context).checkDndPermission()) {

                            alert(emulator[0], emulator[1])

                        }

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
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onVoiceDetected(
                            amplitude, isNiceDetected, isRunning, typeOfVoiceDetected
                        )

                        if (controls.getControls().isAlert && controls.getControls().isAlertVoiceDetection) {
                            if (isNiceDetected) {/*(context as AppCompatActivity).runOnUiThread {
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
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onFailure(exception)
                    }
                }
            }

            override fun onFaceCount(face: Int) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onFaceCount(face)
                    }

                    if (controls.getControls().isAlert) {
                        when (face) {
                            0 -> {
                                faceCountWorring2Times++

                                Log.e("TAG", "onFaceCount: count print" + faceCountWorring2Times)
                                if (controls.getControls().isAlertFaceNotFound && controls.getControls().isAlert && faceCountWorring2Times >= 2) {


                                    val faceNotFoundException =
                                        context.getString(R.string.face_not_found)
                                            .split("[:]".toRegex())

                                    if (faceNotFoundException.size == 2 && DNDManager(context).checkDndPermission()) {
                                        alert(faceNotFoundException[0], faceNotFoundException[1])
                                        faceCountWorring2Times = -1

                                    }

                                }

                            }

                            1 -> {
                                hideAlert()
                            }

                            else -> {
                                if (controls.getControls().isAlertMultipleFaceCount) {


                                    val filter = context.getString(R.string.Multiple_face_detection)
                                        .split("[:]".toRegex())

                                    if (filter.size == 2 && DNDManager(context).checkDndPermission()) {
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
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onLipMovementDetection(islipmovment)
                    }
                    if (controls.getControls().isAlert && controls.getControls().isAlertLipMovement) {
                        if (islipmovment) {
                            val lipFilter =
                                context.getString(R.string.Lip_movement_eyeball_tracking)
                                    .split("[:]".toRegex())

                            if (lipFilter.size == 2 && DNDManager(context).checkDndPermission()) {
                                alert(lipFilter[0], lipFilter[1])
                            }

                        }
                    }
                }

            }

            override fun onObjectDetection(objectList: ArrayList<String>, size: Int?) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onObjectDetection(objectList)
                    }

                    val data = checkObject(objectList, controls.getControls().isBlockedObjectList)

                    if (controls.getControls().isAlert && controls.getControls().isAlertObjectDetection && !data.isNullOrEmpty()) {
                        val objectAlert =
                            context.getString(R.string.object_not_allowed).split("[:]".toRegex())

                        if (objectAlert.size == 2 && DNDManager(context).checkDndPermission()) {
                            alert(objectAlert[0], objectAlert[1])
                        }


                    }

                }

            }

            override fun onEyeDetectionOnlyOneFace(face: String) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onEyeDetectionOnlyOneFace(face)

                    }
                    if (controls.getControls().isAlert && controls.getControls().isAlertEyeDetection) {
                        // eye movement stopped by sir facing quick eye open and close
                        if (!check(face) && !face.isNullOrBlank()) {

                            alert("Eye", face)
                            val filter = context.getString(R.string.Lip_movement_eyeball_tracking)
                                .split("[:]".toRegex())

                            if (filter.size == 2 && DNDManager(context).checkDndPermission()) {
                                alert(filter[0], filter[1])
                            }
                        }
                    }
                }

            }

            override fun onUserWallDistanceDetector(distance: Float) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onUserWallDistanceDetector(distance)
                    }
                    if (controls.getControls().isAlert && controls.getControls().isAlertUserWallDistanceDetector) {
                        // defalut alert
                    }
                }

            }

            override fun onFaceDirectionMovement(faceDirection: String?) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isWaiting) {
                        proctorListener?.onFaceDirectionMovement(faceDirection)
                    }

                    if (controls.getControls().isAlert && controls.getControls().isAlertFaceDirectionMovement) {
                        if (!checkFaceDirection(faceDirection) && faceDirection != null) {
//                            alert("alerts", faceDirection)
                            val headDirection =
                                context.getString(R.string.moving_face_left_or_right_during_assesment)
                                    .split("[:]".toRegex())

                            if (headDirection.size == 2 && DNDManager(context).checkDndPermission()) {
                                alert(headDirection[0], headDirection[1])
                            }

                        }
                    }

                }
            }

            override fun captureImage(faceDirection: Bitmap?) {

                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    Log.e(
                        "status of image",
                        "captureImage: isWaiting status" + isWaiting + "<  --- " + Date(System.currentTimeMillis())
                    )
                    if (isWaiting) {
                        if (faceDirection != null && controls.getControls().isCaptureImage) {
                           // proctorListener?.captureImage(faceDirection)
                            if (System.currentTimeMillis() - lastCaptureTime > 1000) {
                                proctorListener?.captureImage(faceDirection)
                                lastCaptureTime = System.currentTimeMillis()
                            }
                        }

                        if (controls.getControls().isSaveImageHideFolder) {  // hide image into local folder
                            if (faceDirection != null) {
                                //  Utils().saveBitmapIntoImageInternalDir(faceDirection, context)
                            }
                        }

                    }
                }

            }

        })

    }


    private fun isProcessOfWaiting(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()

        var ct = convertIntoTime(currentTimeMillis)

        return timeListString.contains(ct)
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

            Log.e("TAG", "run: is camera running or not " + running)

        }
    }
}

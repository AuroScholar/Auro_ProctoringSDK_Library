package com.example.auroproctoringsdk

import android.annotation.SuppressLint
import android.app.job.JobScheduler
import android.content.Context
import android.graphics.Bitmap
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

class ProctoringSDK(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback, Camera.PreviewCallback {
    companion object {
        private var isViewAvailable = false
        private var isDNDManagerRequest = false
        var isShareResult = false
        var isAlert = false
    }

    /**
     * Face detector
     * in this class having all AI code
     *
     */
    private val faceDetector = FaceDetector()

    /**
     * Camera
     * [startPreview]
     * [captureImage]
     * [stopPreview]
     * [releaseCamera]
     */
    private var camera: Camera? = null

    /**
     * Thread
     * When Camera is Running
     * then threads start
     */
    private var thread: CustomSurfaceThread? = null

    /**
     * Timer
     * Use to Specifics Duration [time] for Continues Perform same Task repeatedly with give time [Duration]
     *
     */
    private var timer: Timer? = null

    /**
     * [proctorListener] interface is use to send user Face real-time information
     * Proctoring End User every 10 sec wait after AI [FaceDetector] result is Ready.
     * */
    private var proctorListener: onProctorListener? = null


    /**
     * BUG
     * [faceNotFoundWarning]
     * face not found error come multiple times when camera some time generating blurImages , unreadable image
     * Solution
     * before sending result into [proctorListener] user attempt face Not Found Error max 2 times then send into [proctorListener]
     *[faceNotFoundWarning] = max 2 times for more quorate
     */
    var faceNotFoundWarning = -1
    var multipleFaceFoundWarning = -1


    /**
     * Controls [Controls]
     * Help to control the run time Alerts and Permission related task
     *  over handle [startProctoring] and [stopProctoring]
     *  [alertOnOff] [updateControl] [getControl]
     *  all run time alerts handle by [Controls]
     */
    private var controls = Controls()

    /**
     * Alert dialog [CustomAlertDialog]
     * all Run time Alerts in Separate class [CustomAlertDialog]
     */
    var alertDialog = CustomAlertDialog(context)

    init {
        holder.addCallback(this)
        scheduleJob()
    }

    /**
     * continues running during the camera running state is on
     * wait for 10 sec then isShareResult is true for send proctoring info into onProctorListener for final result
     * */
    private fun scheduleJob() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                isShareResult = true
            }
        }
        timer.scheduleAtFixedRate(task, 0, 10000) // every 10 seconds
    }

    /**
     * Camera life Cycle
     *
     * surface Create [surfaceCreated]
     * open checking hardware and detecting camera id [findFrontCamera]
     * start Preview [startPreview]
     * image capture every 5 milis
     * */
    override fun surfaceCreated(holder: SurfaceHolder) {
        openCamera()
        startPreview()

        thread = CustomSurfaceThread(holder)
        thread?.start()

        startImageCaptureTimer()
    }

    /**
     * Surface changed
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     *
     * when user change the Phone Orientation some time
     * camera get stack that camera restart by it self
     *
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Implement surfaceChanged logic here
        if (holder.surface == null) {
            return
        }
        startPreview()
    }

    /**
     * Surface destroyed
     *
     * @param holder
     *  when user exit then ui Detached from view group
     *  surface stop camera hardware
     *  then stop camera threads
     *  stop camera image [captureImage] process
     *
     */
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

    /**
     * always find camera ID
     * check and confirm Open Camera is FrontCamera [findFrontCamera]
     * */
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

    /**
     * take image every 1 sec delay
     * for AI process for analysis the image
     * detect face [FaceDetector]
     * */
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

    /**
     * stop image capture
     * sub methods of [releaseCamera]
     * */
    private fun stopImageCaptureTimer() {
        timer?.cancel()
        timer = null
    }

    /**
     * [captureimage] method to create images data
     * for AI process
     * Implement image capture logic here
     * [faceDetector.process] send camera real time image into [FaceDetector] class
     * [Frame] is Model Class holds camera [data] cameraInfo [camera]
     * Default camera given image rotation is horizontaly so its wrong for Ai [FaceDetector] need 270 Degree
     * */
    fun captureImage() {
//        if (camera!=null){
//            camera?.setPreviewCallback(this@ProctoringSDK)
//            camera?.setPreviewCallback(Camera.PreviewCallback { data, camera ->
//                faceDetector.process(
//                    Frame(
//                        data,
//                        270,
//                        Size(camera.parameters.previewSize.width, camera.parameters.previewSize.height),
//                        camera.parameters.previewFormat,
//                        LensFacing.FRONT
//                    )
//                )
//            })
//        }
        if (camera != null) {
            synchronized(this) {
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

    }
    /**
     * restart camera [restartCamera]
     * Android Version 11 below working fine
     * if user request permission then restart camera
     * */
    fun restartCamera() {
        stopPreview()
        releaseCamera()
        openCamera()
        startPreview()
        thread = CustomSurfaceThread(holder)
        thread?.start()
        startImageCaptureTimer()
    }

    /**
     * lock Notification bar
     * Android Version 11 below working fine
     * when user change on click event out of the window
     * */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            StatusBarLocker.setExpandNotificationDrawer(context, false)
        } else {
            if (!hasFocus) {
                StatusBarLocker.setExpandNotificationDrawer(context, false)
            }
        }
    }

    /**
     * check front camera
     * if exit then start Front Camera ID - 1 [findFrontCamera]
     * set camera output on holder [surfaceCreated]
     *
     * */
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

    /**
     * Check if the camera is already opened [camera][findFrontCamera]
     * then [startPreview] on Surface Holder [surfaceCreated]
     * */
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

    /**
     * stop preview [stopPreview]
     * stop the camera
     * release the camera ID
     *
     * */
    private fun stopPreview() {
        if (camera!=null){
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
    }

    /**
     * timer cancel [timer] capture image every 1 sec wait need to stop
     * camera release [releaseCamera]
     *  [stopPreview]
     *  camera[release]
     * */
     fun releaseCamera() {
        timer?.cancel()
        timer=null
//        if (camera!=null){
//            synchronized(this){
//                try {
//                    camera?.apply {
//                        stopPreview()
//                        setPreviewCallback(null)
//                        release()
//                    }
//                    camera = null
//                }catch (e:Exception){
//                    e.printStackTrace()
//                }
//            }
//        }
        if (camera != null) {
            try {
                camera?.stopPreview()
                camera?.release()
                camera = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    /**
     * Observe lifecycle
     * Activity
     * @param lifecycle
     */

    fun observeLifecycle(lifecycle: Lifecycle) {
        lifeProcess(lifecycle)
    }

    /**
     * Observe lifecycle
     * Fragment
     */
    fun LifecycleOwner.observeLifecycle() {
        val lifecycle = this.lifecycle
        lifeProcess(lifecycle)
    }

    /**
     * Life process [Activity,Fragment]
     * @param lifecycle
     * onCreate()
     * onStart()
     * onResume()
     * onPause()
     * onStop()
     * onDestroy()
     *
     */

    private fun lifeProcess(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                isViewAvailable = true
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {



                if (controls.getControls().isStatusBarLock) {
                    StatusBarLocker.statusBarLock(context)
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
                alertDialog.hideForcefully()
                if (controls.getControls().isDndStatusOn) { // DND off
                    DNDManager(context).dndAlertDialogHide()
                    DNDManager(context).DndModeOff(context)
                    hideAlert()
                }
                // Code to execute when the fragment or activity is paused
                isViewAvailable = false
                releaseCamera()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                alertDialog.hideForcefully()
                if (controls.getControls().isDndStatusOn) {
                    DNDManager(context).dndAlertDialogHide()
                    DNDManager(context).DndModeOff(context)
                    hideAlert()
                }
                isViewAvailable = false
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                alertDialog.hideForcefully()
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
        alertDialog.show(title.toString(), message.toString())
    }

    private fun hideAlert() {
        alertDialog.hide()
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
        controls.updateControl(ControlModel(isAlert = false, isProctoringStart = false))
    }

    /**
     * Proctoring SDK control Update run time
     * */
    fun updateControl(controlModel: ControlModel?): ControlModel {
        return if (controlModel != null) {
            controls.updateControl(controlModel)
            controls.getControls()
        } else {
            controls.getControls()
        }
    }

    /**
     * share last updated control
     * */
    fun getControl(): ControlModel = controls.getControls()

    /**
     * Proctoring SDK start
     * */
    fun startProctoring(
        listener: onProctorListener, controlModel: ControlModel?,
    ) {
        proctorListener = listener
        isAlert = true

        if (controls.getControls().isDndStatusOn && !isDNDManagerRequest) { // check DND not on
            if (DNDManager(context).checkDndPermission()) {
                DNDManager(context).checkDNDModeON()
            } else {
                DNDManager(context).enableDoNotDisturb(context)
                isDNDManagerRequest = true

            }
        }
        /**
         * Update Control
         * [controls] update in run time for change any
         */

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

    /**
     * syncResults method
     *
     * real-time result share into end user using interface onProctoringListener
     * isShareResult true/false handle or manage to sync-result AI interface into onProctoringInterface end user with deal
     *
     * */
    private fun syncResults() {
        faceDetector.setonFaceDetectionFailureListener(object :
            FaceDetector.OnProctoringResultListener {

            /**
             * Is running detector
             *
             * @param boolean
             */
            override fun isRunningDetector(boolean: Boolean?) {

                if (isViewAvailable && controls.getControls().isProctoringStart) { // view is ready

                    if (isShareResult) {
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

            /**
             * On voice detected
             *
             * @param amplitude
             * @param isNiceDetected
             * @param isRunning
             * @param typeOfVoiceDetected
             *
             * when mic detect high sound real-time
             * then alert show
             *
             */
            override fun onVoiceDetected(
                amplitude: Double,
                isNiceDetected: Boolean,
                isRunning: Boolean,
                typeOfVoiceDetected: String,
            ) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isShareResult) {
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

            /**
             * On success
             *
             * @param faceBounds
             * real time [FaceDetector] running status
             *
             */
            override fun onSuccess(faceBounds: Int) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isShareResult) {
                        proctorListener?.onSuccess(faceBounds)
                    }
                }
            }

            /**
             * On failure
             *
             * @param exception
             * [FaceDetector] class getting any error in real-time
             * error come [onFailure]
             * send to [proctorListener] for end user
             */
            override fun onFailure(exception: Exception) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isShareResult) {
                        proctorListener?.onFailure(exception)
                    }
                }
            }

            /**
             * onFace Count
             * this interface get real time face count
             * if isShareResult is True then transfer data into onProctoringListener for end user
             *
             * isShareResult true/false handle by
             *
             * */
            override fun onFaceCount(face: Int) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isShareResult) {
                        proctorListener?.onFaceCount(face)
                    }

                    if (controls.getControls().isAlert) {
                        when (face) {
                            0 -> {
                                faceNotFoundWarning++
                                if (controls.getControls().isAlertFaceNotFound && controls.getControls().isAlert && faceNotFoundWarning >= 2) {
                                    val faceNotFoundException =
                                        context.getString(R.string.face_not_found)
                                            .split("[:]".toRegex())
                                    if (faceNotFoundException.size == 2 && DNDManager(context).checkDndPermission()) {
                                        alert(faceNotFoundException[0], faceNotFoundException[1])
                                        faceNotFoundWarning = -1

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
                                    multipleFaceFoundWarning ++
                                    if (filter.size == 2 && multipleFaceFoundWarning >=2 && DNDManager(context).checkDndPermission()) {
                                        multipleFaceFoundWarning =-1
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
                    if (isShareResult) {
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
                    if (isShareResult) {
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
                    if (isShareResult) {
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
                    if (isShareResult) {
                        proctorListener?.onUserWallDistanceDetector(distance)
                    }
                    if (controls.getControls().isAlert && controls.getControls().isAlertUserWallDistanceDetector) {
                        // defalut alert
                    }
                }

            }

            override fun onFaceDirectionMovement(faceDirection: String?) {
                if (isViewAvailable && controls.getControls().isProctoringStart) {
                    if (isShareResult) {
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
                    if (isShareResult && faceDirection != null && controls.getControls().isCaptureImage) {
                        isShareResult = false
                        proctorListener?.captureImage(faceDirection)
                    }

                }

            }

        })

    }

    /**
     *
     * Detected Objects into camera frame
     * blockedDeviceList Object ( user provided this list )
     *
     * */

    private fun checkObject(face: ArrayList<String>, blockedDeviceList: List<String>?): String? {
        return blockedDeviceList?.firstOrNull { face.contains(it) }
    }

    /**
     *
     * Face Movement into Direction
     * left
     * right
     * up
     * down
     * return
     * */
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

    /**
     * Eye Status result check
     * return
     * */

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

    /**
     * onProctorListener interface to send all Face result into end user
     *
     * */

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

/**
 * CustomSurfaceThread for camera running into a thread
 * */

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
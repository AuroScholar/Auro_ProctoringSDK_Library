package com.example.auroproctoringsdk.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.annotation.GuardedBy
import androidx.lifecycle.MutableLiveData
import com.example.auroproctoringsdk.Application.Companion.faceDirectionAccuracy
import com.example.auroproctoringsdk.Application.Companion.faceMouthAccuracy
import com.example.auroproctoringsdk.Controls
import com.example.auroproctoringsdk.voiceDetector.NoiseDetector
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sqrt

class FaceDetector() {

    private var faceLiveResult = MutableLiveData<FaceDetectorModel>()
    private var controls = Controls()

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).enableTracking()
            .setMinFaceSize(0.20f).build()
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptionsBase.SINGLE_IMAGE_MODE)
            .build()
    )

    private val bitmapFaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )


   private val compareFaces = FaceDetection.getClient(FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build())


    private val objectDetector = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    /** Listener that gets notified when a face detection result is ready. */
    private var onProctoringResultListener: OnProctoringResultListener? = null

    /** [Executor] used to run the face detection on a background thread.  */
    private var faceDetectionExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** Controls access to [isProcessing], since it can be accessed from different threads. */
    private val lock = Object()

    @GuardedBy("lock")
    private var isProcessing = false

    private var oldBitmap: Bitmap? = null

    fun setonFaceDetectionFailureListener(listener: OnProctoringResultListener) {
        onProctoringResultListener = listener
    }

    fun liveFaceResult(): MutableLiveData<FaceDetectorModel> {
        return faceLiveResult
    }

    fun process(frame: Frame/*, bitmap: Bitmap?*/) {
//        oldBitmap = bitmap
        synchronized(lock) {
            if (!isProcessing) {
                isProcessing = true
                onProctoringResultListener?.isRunningDetector(isProcessing)
                faceDetectionExecutor.execute { frame.detectFaces() }
            }
        }
    }

    //    ByteArray
    private fun Frame.detectFaces() {
        val data = data ?: return

        val inputImage = InputImage.fromByteArray(data, size.width, size.height, rotation, format)


       // Log.e(TAG, "detectFaces: live processing "+LivenessDetection().checkLikeness(data, size.width, size.height, rotation, format))


        val faceDetectionTask = faceDetector.process(inputImage)
        val poseDetectionTask = poseDetector.process(inputImage)
        val objectDetectionTask = objectDetector.process(inputImage)


        /*val faces1 = result1.result?.size() ?: 0
        val faces2 = result2.result?.size() ?: 0*/


        Tasks.whenAll(faceDetectionTask, poseDetectionTask, objectDetectionTask)
            .addOnSuccessListener {
                synchronized(lock) {

                    isProcessing = false
                    onProctoringResultListener?.isRunningDetector(isProcessing)

                    val faceResults = faceDetectionTask.result
                    val poseResults = poseDetectionTask.result
                    val objectResults = objectDetectionTask.result

                    var mouthOpen: Boolean = false
                    var eyeOpenStatus: String = ""
                    var calculateUserWallDistance: Float = -0.0F
                    var objectSectionNames: String = ""
                    var faceCount: Int = faceResults.size
                    var faceDirection: String? = null
                    var labelsList: ArrayList<String> = arrayListOf()


                    onProctoringResultListener?.onFaceCount(faceResults.size)


                    onProctoringResultListener?.captureImage(
                        convectionBitmap(this)
                    )

                    //Face Tracking
                    for (face in faceResults) {

                        faceCount = faceResults.size
                        Log.e(TAG, "detectFaces: face Reult " + faceCount)
                        onProctoringResultListener?.onFaceCount(faceResults.size)

                        if (faceResults.size == 1) {

                            eyeOpenStatus = eyeTracking(face)
                            // Eye Tracking
                            onProctoringResultListener?.onEyeDetectionOnlyOneFace(eyeOpenStatus)

                            onProctoringResultListener?.isRunningDetector(isReal(face))

                            //Lip Tracking
                            mouthOpen = detectMouth(face)
                            onProctoringResultListener?.onLipMovementDetection(mouthOpen)

                            //Pose Tracking
                            calculateUserWallDistance = calculateUserWallDistance(poseResults)

                            onProctoringResultListener?.onUserWallDistanceDetector(
                                calculateUserWallDistance
                            )

                            //face direction
                            faceDirection = faceDetection(face)
                            onProctoringResultListener?.onFaceDirectionMovement(faceDirection)


                            //Object Tracking
                            for (label in objectResults) {
                                val text = label.text
                                val confidence = label.confidence
                                labelsList.add(text)
                                Log.e(TAG, "Label:---->    $text, Confidence: $confidence")
                                /*
                                                                E  Label:---->     Desk, Confidence: 0.5930478
                                                                E  Label:---->     Mobile phone, Confidence: 0.8290278
                                                                E  Label:---->     Computer, Confidence: 0.503058
                                */
                            }
                            onProctoringResultListener?.onObjectDetection(labelsList, null)

                        } else {

                            if (faceCount !in listOf(0, 1, null)) {

                                onProctoringResultListener?.onObjectDetection(labelsList, faceCount)

                            }
                        }

                    }

                    //live Result
                    faceLiveResult.postValue(
                        FaceDetectorModel(
                            faceCount, eyeOpenStatus, mouthOpen, objectSectionNames, faceDirection
                        )
                    )
                }

            }.addOnFailureListener { exception ->
                synchronized(lock) {
                    isProcessing = false
                    onProctoringResultListener?.isRunningDetector(isProcessing)
                }
                onError(exception)
            }
    }

    private fun convectionBitmap(frame: Frame): Bitmap {
        val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 100, out)
        val imageBytes = out.toByteArray()
        val lastUpdatedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        out.flush()
        out.close()
        val rotatedBitmap = lastUpdatedBitmap.rotateBitmap(-90F)
        lastUpdatedBitmap.recycle() // Recycle the bitmap to free up memory
        return rotatedBitmap
    }

    fun Bitmap.rotateBitmap(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun faceDetection(face: Face): String? {

        val eulerY = face.headEulerAngleY // Yaw   ------- direction left and right out
        val eulerZ = face.headEulerAngleZ // Roll
        val eulerX = face.headEulerAngleX // Pitch

        if (controls.getControls().faceDirectionAccuracy > 0) {
            //Custom Methods for dynamic accuracy
            if (eulerY > controls.getControls().faceDirectionAccuracy) {
                return "moving to left"
            } else if (eulerY < -controls.getControls().faceDirectionAccuracy) {
                return "moving to right"
            } else if (eulerX > controls.getControls().faceDirectionAccuracy) {
                return "moving up"
            } else if (eulerX < -controls.getControls().faceDirectionAccuracy) {
                return "moving down"
            } else {
                // Face is not moving
                return null
            }

        } else {
            // Default accuracy 50
            if (eulerY > faceDirectionAccuracy) {
                return "moving to left"
            } else if (eulerY < -faceDirectionAccuracy) {
                return "moving to right"
            } else if (eulerX > faceDirectionAccuracy) {
                return "moving up"
            } else if (eulerX < -faceDirectionAccuracy) {
                return "moving down"
            } else {
                // Face is not moving
                return null
            }
        }

    }

    private fun calculateUserWallDistance(pose: Pose): Float {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)?.position
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)?.position

        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            val shoulderMidPoint = PointF(
                (leftShoulder.x + rightShoulder.x) / 2, (leftShoulder.y + rightShoulder.y) / 2
            )
            val hipMidPoint = PointF((leftHip.x + rightHip.x) / 2, (leftHip.y + rightHip.y) / 2)
            return calculateDistance(shoulderMidPoint, hipMidPoint)
        }

        return 0f
    }

    private fun calculateDistance(point1: PointF, point2: PointF): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx.pow(2) + dy.pow(2))
    }


    private fun compareFaces(bitmap1: Bitmap, bitmap2: Bitmap){

        val image1 = InputImage.fromBitmap(bitmap1, 0)
        val image2 = InputImage.fromBitmap(bitmap2, 0)

        val result1 = compareFaces.process(image1)
        val result2 = compareFaces.process(image2)

        Tasks.whenAll(result1, result2).addOnCompleteListener {
            val faces1 = result1.result?.size ?: 0
            val faces2 = result2.result?.size ?: 0

            val similarity = (faces1.toDouble() / faces2.toDouble()) * 100

            Log.e(TAG, "compareFaces: process matching$similarity")

            // Determine the face matching status based on the similarity percentage
            val status = when {
                similarity >= 100 -> "100% Match"
                similarity >= 80 -> "80% Match"
                similarity >= 50 -> "50% Match"
                else -> "No Match"
            }

            // Show the face matching status
            Log.e(TAG, "Face Matching Status: $status")

        }.addOnFailureListener {

        }
    }
    fun isReal(face: Face): Boolean {
        if (face.smilingProbability != null && face.rightEyeOpenProbability != null && face.leftEyeOpenProbability != null) {
            val smileProb = face.smilingProbability
            val rightEyeOpenProb = face.rightEyeOpenProbability
            val leftEyeOpenProb = face.leftEyeOpenProbability
            rightEyeOpenProb?.let {
                leftEyeOpenProb?.let {
                    val isReal =
                        ((smileProb?.plus(rightEyeOpenProb) ?: it) + leftEyeOpenProb) / 3 > 0.5
                    return isReal
                }
            }
            // Do something with the result
        }
        return false
    }

    private fun detectMouth(face: Face): Boolean {

        //Default value
        var initUpperLipBottom = 3
        var initLowerLipTop = 3

        val upperLipBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
        val lowerLipTop = face.getContour(FaceContour.LOWER_LIP_TOP)?.points

        if (controls.getControls().upperLipBottomSize > 0) {
            initLowerLipTop = controls.getControls().lowerLipTopSize
            initUpperLipBottom = controls.getControls().upperLipBottomSize
        }

        // Check if the facial contour points are not null and contain enough points
        if (upperLipBottom != null && lowerLipTop != null && upperLipBottom.size >= initUpperLipBottom && lowerLipTop.size >= initLowerLipTop) {
            // Calculate the average y-coordinate of the upper lip bottom points
            val upperLipBottomY = upperLipBottom.map { it.y }.average()

            // Calculate the average y-coordinate of the lower lip top points
            val lowerLipTopY = lowerLipTop.map { it.y }.average()

            // Define a threshold value to determine if the mouth is open or closed

            //  val threshold = 10.0 // default value
            // incress aquressy <10> decress aquresy
            val threshold = faceMouthAccuracy

            // Check if the difference in y-coordinates exceeds the threshold
            val isMouthOpen = Math.abs(upperLipBottomY - lowerLipTopY) > threshold

            // Print the result
            return isMouthOpen
        } else {
            return false
//            println("Facial contour points not found or insufficient points")
        }
    }

    private fun eyeTracking(face: Face): String {
        val leftEyeOpenProbability = face.leftEyeOpenProbability
        val rightEyeOpenProbability = face.rightEyeOpenProbability
        // Check if both eyes are open
        if (leftEyeOpenProbability != null && rightEyeOpenProbability != null && leftEyeOpenProbability > 0.5 && rightEyeOpenProbability > 0.5) {
            return "both eyes are open"
        } else if (leftEyeOpenProbability != null && rightEyeOpenProbability != null && leftEyeOpenProbability <= 0.2 && rightEyeOpenProbability <= 0.2) {
            return "both eyes are closed"
        } else if (leftEyeOpenProbability != null && leftEyeOpenProbability > 0.5) {
            // Perform desired actions
            return "right eye is open"
        } else if (rightEyeOpenProbability != null && rightEyeOpenProbability > 0.5) {
            // Perform desired actions
            return "left eye is open"
        }

        return ""
    }

    private fun onError(exception: Exception) {
        onProctoringResultListener?.onFailure(exception)
        Log.e(TAG, "An error occurred while running a face detection", exception)
    }

    fun noticeDetect(context: Context?) {
        if (context != null) {
            onProctoringResultListener?.let { NoiseDetector().startNoiseDetector(context, it) }
        }
    }


    interface OnProctoringResultListener {

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
        fun onObjectDetection(face: ArrayList<String>, size: Int?)
        fun onEyeDetectionOnlyOneFace(face: String)
        fun onUserWallDistanceDetector(distance: Float)
        fun onFaceDirectionMovement(faceDirection: String?)
        fun captureImage(faceDirection: Bitmap?)

    }

    companion object {
        private const val TAG = "FaceDetector"
    }
}

data class FaceDetectorModel(
    var faceCount: Int = -1,
    var eyeOpenStatus: String = "",
    var isMouthOen: Boolean = false,
    var objectDectionNames: String = "",
    var faceDirection: String? = null,
)
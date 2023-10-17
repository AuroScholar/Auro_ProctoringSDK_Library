package com.example.mytoolbox.proctoring

import android.graphics.PointF
import android.util.Log
import androidx.annotation.GuardedBy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sqrt

class FaceDetector() {

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).enableTracking()
            .setMinFaceSize(0.20f).build()
    )

    // Create an object detector using the options
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder().setDetectorMode(
            ObjectDetectorOptionsBase.SINGLE_IMAGE_MODE
        ).enableClassification().enableMultipleObjects().enableClassification().build()
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptionsBase.SINGLE_IMAGE_MODE)
            .build()
    )


    /** Listener that gets notified when a face detection result is ready. */
    private var onProctoringResultListener: OnProctoringResultListener? = null

    /** [Executor] used to run the face detection on a background thread.  */
    private var faceDetectionExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** Controls access to [isProcessing], since it can be accessed from different threads. */
    private val lock = Object()

    @GuardedBy("lock")
    private var isProcessing = false

    fun setonFaceDetectionFailureListener(listener: OnProctoringResultListener) {
        onProctoringResultListener = listener
    }

    fun process(frame: Frame) {
        synchronized(lock) {
            if (!isProcessing) {
                isProcessing = true
                faceDetectionExecutor.execute { frame.detectFaces() }
            }
        }
    }

    private fun Frame.detectFaces() {
        val data = data ?: return
        val inputImage = InputImage.fromByteArray(data, size.width, size.height, rotation, format)

        val faceDetectionTask = faceDetector.process(inputImage)
        val poseDetectionTask = poseDetector.process(inputImage)
        val objectDetectionTask = objectDetector.process(inputImage)

        Tasks.whenAll(faceDetectionTask, poseDetectionTask, objectDetectionTask)
            .addOnSuccessListener {
                synchronized(lock) {
                    isProcessing = false

                    val faceResults = faceDetectionTask.result
                    val poseResults = poseDetectionTask.result
                    val objectResults = objectDetectionTask.result

                    //Face Tracking
                    for (face in faceResults) {

                        if (faceResults.size == 1) {
                            // Eye Tracking
                            onProctoringResultListener?.onEyeDetectionOnlyOneFace(eyeTracking(face))

                            //Lip Tracking
                            onProctoringResultListener?.onLipMovementDetection(detectMouth(face))

                            //Pose Tracking
                            onProctoringResultListener?.onUserWallDistanceDetector(
                                calculateUserWallDistance(
                                    poseResults
                                )
                            )
                            //Object Tracking
                            for (detectedObject in objectResults) {
                                val labels = detectedObject.labels
                                for (label in labels) {
                                    onProctoringResultListener?.onObjectDetection(label.text)
                                }
                            }

                        } else {
                           // Face Count
                            onProctoringResultListener?.onFaceCount(faceResults.size.toString())
                        }
                    }


                }

            }.addOnFailureListener { exception ->
                synchronized(lock) {
                    isProcessing = false
                }
                onError(exception)
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


    private fun detectMouth(face: Face): Boolean {
        val upperLipBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
        val lowerLipTop = face.getContour(FaceContour.LOWER_LIP_TOP)?.points

        // Check if the facial contour points are not null and contain enough points
        if (upperLipBottom != null && lowerLipTop != null && upperLipBottom.size >= 3 && lowerLipTop.size >= 3) {
            // Calculate the average y-coordinate of the upper lip bottom points
            val upperLipBottomY = upperLipBottom.map { it.y }.average()

            // Calculate the average y-coordinate of the lower lip top points
            val lowerLipTopY = lowerLipTop.map { it.y }.average()

            // Define a threshold value to determine if the mouth is open or closed

            //  val threshold = 10.0 // default value
            // incress aquressy <10> decress aquresy
            val threshold = 3.0

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


    interface OnProctoringResultListener {
        fun onVoiceDetected(
            amplitude: Double,
            isNiceDetected: Boolean,
            isRunning: Boolean,
            typeOfVoiceDetected: String
        ) {
        }

        fun onSuccess(faceBounds: Int) {}
        fun onFailure(exception: Exception) {}
        fun onFaceCount(face: String)
        fun onLipMovementDetection(face: Boolean)
        fun onObjectDetection(face: String)
        fun onEyeDetectionOnlyOneFace(face: String)
        fun onUserWallDistanceDetector(distance: Float)
    }

    companion object {
        private const val TAG = "FaceDetector"
        private const val MIN_FACE_SIZE = 0.15F
    }
}
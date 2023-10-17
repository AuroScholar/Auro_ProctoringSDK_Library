//package com.example.mytoolbox
//
//import android.content.Context
//import android.graphics.PointF
//import android.util.Log
//import com.google.android.gms.tasks.Tasks
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.Face
//import com.google.mlkit.vision.face.FaceContour
//import com.google.mlkit.vision.face.FaceDetection
//import com.google.mlkit.vision.face.FaceDetectorOptions
//import com.google.mlkit.vision.objects.ObjectDetection
//import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
//import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
//import com.google.mlkit.vision.pose.Pose
//import com.google.mlkit.vision.pose.PoseDetection
//import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
//import com.google.mlkit.vision.pose.PoseLandmark
//import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//open class Proctoring(val context: Context) {
//
//    private var callBack: FaceMovementDetectionListener? = null
//    fun setFaceDetectionListener(faceMovementDetectionListener: FaceMovementDetectionListener) {
//        callBack = faceMovementDetectionListener
//    }
//
//
//    val faceDetector = FaceDetection.getClient(
//        FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
//            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
//            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).enableTracking()
//            .setMinFaceSize(0.20f).build()
//    )
//
//    // Create an object detector using the options
//    val objectDetector = ObjectDetection.getClient(
//        ObjectDetectorOptions.Builder().setDetectorMode(
//            ObjectDetectorOptionsBase.SINGLE_IMAGE_MODE
//        ).enableClassification().enableMultipleObjects().enableClassification().build()
//    )
//
//    val poseDetector = PoseDetection.getClient(PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptionsBase.SINGLE_IMAGE_MODE)
//        .build())
//
//    fun detectBitmap(
//        lastUpdatedBitmap: ByteArray,
//        width: Int,
//        height: Int,
//        previewFormat: Int,
//    ) {
//        val inputImage =
//            InputImage.fromByteArray(lastUpdatedBitmap, width, height, 270, previewFormat)
//
//        /*faceDetector.process(inputImage).addOnSuccessListener {
//            Log.e("TAG", "detectBitmap: status "+it.size)
//
//            callBack?.onFaceCount(it.size.toString())
//
//        }.addOnFailureListener {
//            Log.e("TAG", "detectBitmap: status "+it.message )
//        }.addOnCompleteListener {
//
//        }*/
//
//
//        /*lastUpdatedBitmap.let {
//            val inputImage = InputImage.fromBitmap(
//                lastUpdatedBitmap, 270
//            )
//            Log.e("TAG", "6" + lastUpdatedBitmap.height)
//
//
//            faceDetector.process(inputImage).addOnSuccessListener {
//
//            }.addOnFailureListener {
//                Log.e("TAG", "detectBitmap: face detect " + it.message)
//            }.addOnCompleteListener {
//
//            }
//
//
//            */
//        val faceDetectionTask = faceDetector.process(inputImage)
//        val poseDetectionTask = poseDetector.process(inputImage)
//        val objectDetectionTask = objectDetector.process(inputImage)
//
//        Tasks.whenAll(faceDetectionTask, poseDetectionTask, objectDetectionTask)
//            .addOnSuccessListener {
//                // Handle success
//                val faceResults = faceDetectionTask.result
//                val poseResults = poseDetectionTask.result
//                val objectResults = objectDetectionTask.result
//
//                Log.e("TAG", "detectBitmap: face proccess  ----- > " + faceResults.size)
//
//                callBack?.onFaceCount(faceResults?.size.toString())
//                //Face Tracking
//                if (faceResults != null) {
//                    for (face in faceResults) {
//                        if (faceResults.size == 1) {
//                            // Eye Tracking
//                            callBack?.onEyeDetectionOnlyOneFace(eyeTracking(face))
//                            //Lip Tracking
//                            callBack?.onLipMovementDetection(detectMouthState(face))
//                        }
//                    }
//                }
//                //Pose Tracking
//                callBack?.onUserWallDistanceDetector(calculateUserWallDistance(poseResults))
//
//                //Object Tracking
//                if (objectResults != null) {
//                    for (detectedObject in objectResults) {
//                        val labels = detectedObject.labels
//                        for (label in labels) {
//                            callBack?.onObjectDetection(label.text)
//                        }
//                    }
//                }
//
//
//            }.addOnFailureListener {
//                // Handle failure
//                callBack?.onFaceError(it.message.orEmpty())
//            }.addOnCompleteListener {
//                //                    mediaImage.close()
//            }
//
//    }
//
//
//}
//
//private fun detectMouthState(face: Face): Boolean {
//    val upperLipBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
//    val lowerLipTop = face.getContour(FaceContour.LOWER_LIP_TOP)?.points
//
//    // Check if the facial contour points are not null and contain enough points
//    if (upperLipBottom != null && lowerLipTop != null && upperLipBottom.size >= 3 && lowerLipTop.size >= 3) {
//        // Calculate the average y-coordinate of the upper lip bottom points
//        val upperLipBottomY = upperLipBottom.map { it.y }.average()
//
//        // Calculate the average y-coordinate of the lower lip top points
//        val lowerLipTopY = lowerLipTop.map { it.y }.average()
//
//        // Define a threshold value to determine if the mouth is open or closed
//
//        //  val threshold = 10.0 // default value
//        // incress aquressy <10> decress aquresy
//        val threshold = 3.0
//
//        // Check if the difference in y-coordinates exceeds the threshold
//        val isMouthOpen = Math.abs(upperLipBottomY - lowerLipTopY) > threshold
//
//        // Print the result
//        return isMouthOpen
//    } else {
//        return false
////            println("Facial contour points not found or insufficient points")
//    }
//}
//
//private fun eyeTracking(face: Face): String {
//    val leftEyeOpenProbability = face.leftEyeOpenProbability
//    val rightEyeOpenProbability = face.rightEyeOpenProbability
//    // Check if both eyes are open
//    if (leftEyeOpenProbability != null && rightEyeOpenProbability != null && leftEyeOpenProbability > 0.5 && rightEyeOpenProbability > 0.5) {
//        return "both eyes are open"
//    } else if (leftEyeOpenProbability != null && rightEyeOpenProbability != null && leftEyeOpenProbability <= 0.2 && rightEyeOpenProbability <= 0.2) {
//        return "both eyes are closed"
//    } else if (leftEyeOpenProbability != null && leftEyeOpenProbability > 0.5) {
//        // Perform desired actions
//        return "right eye is open"
//    } else if (rightEyeOpenProbability != null && rightEyeOpenProbability > 0.5) {
//        // Perform desired actions
//        return "left eye is open"
//    }
//
//    return ""
//}
//
//private fun calculateUserWallDistance(pose: Pose): Float {
//    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position
//    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position
//    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)?.position
//    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)?.position
//
//    if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
//        val shoulderMidPoint = PointF(
//            (leftShoulder.x + rightShoulder.x) / 2, (leftShoulder.y + rightShoulder.y) / 2
//        )
//        val hipMidPoint = PointF((leftHip.x + rightHip.x) / 2, (leftHip.y + rightHip.y) / 2)
//        return calculateDistance(shoulderMidPoint, hipMidPoint)
//    }
//
//    return 0f
//}
//
//private fun calculateDistance(point1: PointF, point2: PointF): Float {
//    val dx = point2.x - point1.x
//    val dy = point2.y - point1.y
//    return sqrt(dx.pow(2) + dy.pow(2))
//}
//
//
//interface FaceMovementDetectionListener {
//    fun onVoiceDetected(
//        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String
//    )
//
//    fun onFaceCount(faceCount: String)
//    fun onFaceError(faceError: String)
//
//    fun onLipMovementDetection(faceError: Boolean)
//    fun onObjectDetection(faceError: String)
//
//    fun onEyeDetectionOnlyOneFace(faceError: String)
//    fun onUserWallDistanceDetector(distance: Float)
//}

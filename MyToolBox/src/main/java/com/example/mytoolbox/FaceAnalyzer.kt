//package com.example.mytoolbox
//
//import android.graphics.Bitmap
//import android.graphics.PointF
//import androidx.camera.core.ExperimentalGetImage
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageProxy
//import com.google.android.gms.tasks.Tasks
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.Face
//import com.google.mlkit.vision.face.FaceContour
//import com.google.mlkit.vision.face.FaceDetection
//import com.google.mlkit.vision.face.FaceDetectorOptions
//import com.google.mlkit.vision.face.FaceDetectorOptions.CLASSIFICATION_MODE_ALL
//import com.google.mlkit.vision.face.FaceDetectorOptions.CONTOUR_MODE_ALL
//import com.google.mlkit.vision.face.FaceDetectorOptions.LANDMARK_MODE_ALL
//import com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_FAST
//import com.google.mlkit.vision.objects.ObjectDetection
//import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
//import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
//import com.google.mlkit.vision.pose.Pose
//import com.google.mlkit.vision.pose.PoseDetection
//import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
//import com.google.mlkit.vision.pose.PoseLandmark
//import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
//import kotlin.concurrent.timerTask
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//@ExperimentalGetImage
//class FaceAnalyzer(private val callBack: FaceMovementDetectionListener) : ImageAnalysis.Analyzer {
//
//
//    private val realTimeOpts = FaceDetectorOptions.Builder().setContourMode(CONTOUR_MODE_ALL)
//        .setPerformanceMode(PERFORMANCE_MODE_FAST).setLandmarkMode(LANDMARK_MODE_ALL)
//        .setClassificationMode(CLASSIFICATION_MODE_ALL).enableTracking().setMinFaceSize(0.20f)
//        .build()
//
//    private val faceDetector = FaceDetection.getClient(realTimeOpts)
//
//
//    private val objectDetectionOptions = ObjectDetectorOptions.Builder().setDetectorMode(ObjectDetectorOptionsBase.SINGLE_IMAGE_MODE)
//            .enableClassification().enableMultipleObjects().enableClassification().build()
//
//    // Create an object detector using the options
//    private val objectDetector = ObjectDetection.getClient(objectDetectionOptions)
//
//
//    private val poseOptions =
//        PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptionsBase.SINGLE_IMAGE_MODE)
//            .build()
//
//
//    private val poseDetector = PoseDetection.getClient(poseOptions)
//
//    override fun analyze(imageProxy: ImageProxy) {
//        val mediaImage = imageProxy.image
//
//        mediaImage?.let {
//            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//
//            val bitm :Bitmap? = null
//
//            detectBitmap(bitm,imageProxy.imageInfo.rotationDegrees)
//
//            val faceDetectionTask = faceDetector.process(inputImage)
//            val poseDetectionTask = poseDetector.process(inputImage)
//            val objectDetectionTask = objectDetector.process(inputImage)
//
//
//            Tasks.whenAll(faceDetectionTask, poseDetectionTask, objectDetectionTask)
//                .addOnSuccessListener {
//                    // Handle success
//                    val faceResults = faceDetectionTask.result
//                    val poseResults = poseDetectionTask.result
//                    val objectResults = objectDetectionTask.result
//
//                    callBack.onFaceCount(faceResults.size.toString())
//                    //Face Tracking
//                    for (face in faceResults) {
//                        if (faceResults.size == 1) {
//                            // Eye Tracking
//                            callBack.onEyeDetectionOnlyOneFace(eyeTracking(face))
//                            //Lip Tracking
//                            callBack.onLipMovementDetection(detectMouthState(face))
//                        }
//                    }
//                    //Pose Tracking
//                    callBack.onUserWallDistanceDetector(calculateUserWallDistance(poseResults))
//
//                    //Object Tracking
//                    for (detectedObject in objectResults) {
//                        val labels = detectedObject.labels
//                        for (label in labels) {
//                            callBack.onObjectDetection(label.text)
//                        }
//                    }
//                    /*
//                    Log.e("TAG", "analyze: result face - > " + faceResults.size)
//                    Log.e("TAG", "analyze: result Pose - > " + poseResults.allPoseLandmarks.size)
//                    Log.e("TAG", "analyze: result Object - > " + objectResults.size)
//                     */
//
//                }.addOnFailureListener {
//                    // Handle failure
//                    callBack.onFaceError(it.message.orEmpty())
//                }.addOnCompleteListener {
////                    mediaImage.close()
//                    imageProxy.close()
//                }
//
//
//        }
//    }
//
//   fun detectBitmap(bitmap: Bitmap?, rotationDegrees: Int) {
//        val inputImage = bitmap?.let { InputImage.fromBitmap(it,rotationDegrees) }
//        val faceDetectionTask = inputImage?.let { faceDetector.process(it) }
//        val poseDetectionTask = poseDetector.process(inputImage)
//        val objectDetectionTask = inputImage?.let { objectDetector.process(it) }
//
//
//       Tasks.whenAll(faceDetectionTask, poseDetectionTask, objectDetectionTask)
//            .addOnSuccessListener {
//                // Handle success
//                val faceResults = faceDetectionTask?.result
//                val poseResults = poseDetectionTask.result
//                val objectResults = objectDetectionTask?.result
//
//                callBack.onFaceCount(faceResults?.size.toString())
//                //Face Tracking
//                if (faceResults != null) {
//                    for (face in faceResults) {
//                        if (faceResults.size == 1) {
//                            // Eye Tracking
//                            callBack.onEyeDetectionOnlyOneFace(eyeTracking(face))
//                            //Lip Tracking
//                            callBack.onLipMovementDetection(detectMouthState(face))
//                        }
//                    }
//                }
//                //Pose Tracking
//                callBack.onUserWallDistanceDetector(calculateUserWallDistance(poseResults))
//
//                //Object Tracking
//                if (objectResults != null) {
//                    for (detectedObject in objectResults) {
//                        val labels = detectedObject.labels
//                        for (label in labels) {
//                            callBack.onObjectDetection(label.text)
//                        }
//                    }
//                }
//
//
//            }.addOnFailureListener {
//                // Handle failure
//                callBack.onFaceError(it.message.orEmpty())
//            }.addOnCompleteListener {
////                    mediaImage.close()
//            }
//
//
//    }
//
//    fun detectMouthState(face: Face): Boolean {
//        val upperLipBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
//        val lowerLipTop = face.getContour(FaceContour.LOWER_LIP_TOP)?.points
//
//            // Check if the facial contour points are not null and contain enough points
//        if (upperLipBottom != null && lowerLipTop != null && upperLipBottom.size >= 3 && lowerLipTop.size >= 3) {
//            // Calculate the average y-coordinate of the upper lip bottom points
//            val upperLipBottomY = upperLipBottom.map { it.y }.average()
//
//            // Calculate the average y-coordinate of the lower lip top points
//            val lowerLipTopY = lowerLipTop.map { it.y }.average()
//
//            // Define a threshold value to determine if the mouth is open or closed
//
//            //  val threshold = 10.0 // default value
//            // incress aquressy <10> decress aquresy
//            val threshold = 3.0
//
//            // Check if the difference in y-coordinates exceeds the threshold
//            val isMouthOpen = Math.abs(upperLipBottomY - lowerLipTopY) > threshold
//
//            // Print the result
//            return isMouthOpen
//        } else {
//            return false
////            println("Facial contour points not found or insufficient points")
//        }
//    }
//
//    private fun eyeTracking(face: Face): String {
//        val leftEyeOpenProbability = face.leftEyeOpenProbability
//        val rightEyeOpenProbability = face.rightEyeOpenProbability
//        // Check if both eyes are open
//        if (leftEyeOpenProbability != null && rightEyeOpenProbability != null && leftEyeOpenProbability > 0.5 && rightEyeOpenProbability > 0.5) {
//            return "both eyes are open"
//        } else if (leftEyeOpenProbability != null && rightEyeOpenProbability != null && leftEyeOpenProbability <= 0.2 && rightEyeOpenProbability <= 0.2) {
//            return "both eyes are closed"
//        } else if (leftEyeOpenProbability != null && leftEyeOpenProbability > 0.5) {
//            // Perform desired actions
//            return "right eye is open"
//        } else if (rightEyeOpenProbability != null && rightEyeOpenProbability > 0.5) {
//            // Perform desired actions
//            return "left eye is open"
//        }
//
//        return ""
//    }
//}
//
//// Function to calculate the distance between user and wall using pose data
//fun calculateUserWallDistance(pose: Pose): Float {
//    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position
//    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position
//    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)?.position
//    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)?.position
//
//    if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
//        val shoulderMidPoint =
//            PointF((leftShoulder.x + rightShoulder.x) / 2, (leftShoulder.y + rightShoulder.y) / 2)
//        val hipMidPoint = PointF((leftHip.x + rightHip.x) / 2, (leftHip.y + rightHip.y) / 2)
//        return calculateDistance(shoulderMidPoint, hipMidPoint)
//    }
//
//    return 0f
//}
//
//
//private fun calculateDistance(point1: PointF, point2: PointF): Float {
//    val dx = point2.x - point1.x
//    val dy = point2.y - point1.y
//    return sqrt(dx.pow(2) + dy.pow(2))
//}
//
//interface FaceAnalyzerCallback {
//    fun processFace(faces: List<Face>)
//    fun errorFace(error: String)
//    fun eyeTrackFace(eyeTrack: String)
//    fun lipTrackFace(lipTrack: Boolean)
//    fun objectTrack(lipTrack: String)
//    fun poseDistanceDetector(lipTrack: Float)
//}
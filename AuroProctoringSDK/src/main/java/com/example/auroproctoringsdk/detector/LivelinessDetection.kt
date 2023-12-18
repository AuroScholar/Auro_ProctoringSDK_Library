package com.example.auroproctoringsdk.detector// Import the necessary libraries
import android.graphics.PointF
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.face.Face
import kotlin.math.sqrt
class LivenessDetection() {

    // Set up the face detector options
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    // Create a face detector
    val faceDetector = FaceDetection.getClient(options)

    fun checkLikeness(data: ByteArray, width: Int, height: Int, rotation: Int, format: Int): Boolean {

        val inputImage = InputImage.fromByteArray(data, width, height, rotation, format)

        // Create an input image from a bitmap or file
        val faceDetectorTask = faceDetector.process(inputImage)

        try {
            val faces = Tasks.await(faceDetectorTask)

            for (face in faces) {
                // Perform likeness detection on each face
                val isLive = performLivenessDetection(face)

                if (!isLive) {
                    return false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    // Function to perform liveness detection on a face
    private fun performLivenessDetection(face: Face): Boolean {
        // Get the face landmarks
        val landmarks = face.allLandmarks

        // Check if the left eye and right eye landmarks are detected
        val leftEye = landmarks.getOrNull(FaceLandmark.LEFT_EYE)
        val rightEye = landmarks.getOrNull(FaceLandmark.RIGHT_EYE)

        // Check if both eyes are detected
        if (leftEye != null && rightEye != null) {
            // Calculate the distance between the left and right eye landmarks
            val eyeDistance = livenessCalculateDistance(leftEye.position, rightEye.position)

            // Check if the eye distance is within a certain threshold
            val eyeDistanceThreshold = 50 // Adjust this threshold as needed
            if (eyeDistance < eyeDistanceThreshold) {
                // Eyes are close together, indicating a live face
                return true
            }
        }

        // Default to false if liveness cannot be determined
        return false
    }

    // Function to calculate the distance between two points
    private fun livenessCalculateDistance(point1: PointF, point2: PointF): Float {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return sqrt(dx * dx + dy * dy)
    }
}


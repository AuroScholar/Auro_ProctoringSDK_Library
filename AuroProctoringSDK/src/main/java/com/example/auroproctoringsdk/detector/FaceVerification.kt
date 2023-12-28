package com.example.auroproctoringsdk.detector

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceVerification(private val docImage: InputImage, private val cameraImage: InputImage) {
    // Set up the face detector options
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val faceDetector: FaceDetector = FaceDetection.getClient(options)

    fun verify() {
        val docFaces = faceDetector.process(docImage)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    // Get the face bounding box and landmarks
                    val boundingBox = face.boundingBox
                    val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                    val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                    val nose = face.getLandmark(FaceLandmark.NOSE_BASE)
                    val mouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)

                    // Perform face verification with the camera image
                    verifyFaceWithCameraImage(leftEye, rightEye, nose, mouth)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Log.e("TAG", "Error detecting faces in document image: $exception")
            }
    }

    private fun verifyFaceWithCameraImage(leftEye: FaceLandmark?, rightEye: FaceLandmark?, nose: FaceLandmark?, mouth: FaceLandmark?) {
        val cameraFaces = faceDetector.process(cameraImage)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    // Get the face landmarks
                    val cameraLeftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                    val cameraRightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                    val cameraNose = face.getLandmark(FaceLandmark.NOSE_BASE)
                    val cameraMouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)

                    // Calculate the differences in percentage between the document face and the camera face
                    val leftEyeDiff = calculateDifference(leftEye, cameraLeftEye)
                    val rightEyeDiff = calculateDifference(rightEye, cameraRightEye)
                    val noseDiff = calculateDifference(nose, cameraNose)
                    val mouthDiff = calculateDifference(mouth, cameraMouth)

                    // Perform further verification or comparison based on the differences
                    performVerification(leftEyeDiff, rightEyeDiff, noseDiff, mouthDiff)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Log.e("TAG", "Error detecting faces in camera image: $exception")
            }
    }

    private fun calculateDifference(landmark1: FaceLandmark?, landmark2: FaceLandmark?): Float {
        val distance = Math.sqrt(
            Math.pow(((landmark1?.position?.x ?: 0f) - (landmark2?.position?.x ?: 0f)).toDouble(), 2.0) +
            Math.pow(((landmark1?.position?.y ?: 0f) - (landmark2?.position?.y ?: 0f)).toDouble(), 2.0)
        ).toFloat()

        val percentageDiff = (distance / (docImage.width + docImage.height) * 2) * 100

        return percentageDiff
    }

    private fun performVerification(leftEyeDiff: Float, rightEyeDiff: Float, noseDiff: Float, mouthDiff: Float) {
        Log.e("RESULT", "performVerification: leftEyeDiff"+leftEyeDiff + " rightEyeDiff"+rightEyeDiff + " noseDiff "+noseDiff +" mouthDiff "+mouthDiff )
    }
}

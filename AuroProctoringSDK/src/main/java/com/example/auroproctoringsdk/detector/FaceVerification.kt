package com.example.auroproctoringsdk.detector

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceVerification() {

    /** Listener that gets notified when a face detection result is ready. */
    private var onFaceVerificationListener: FaceVerificationListener? = null
    fun setFaceVerificationListener(listener: FaceVerificationListener) {
        onFaceVerificationListener = listener
    }

    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .setMinFaceSize(0.20f)
            .build()
      /*  FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()*/
    )


    /*private val detector: FaceMeshDetector

    init {
        val optionsBuilder = FaceMeshDetectorOptions.Builder()
        if (FaceMeshDetectorOptions.FACE_MESH == FaceMeshDetectorOptions.BOUNDING_BOX_ONLY) {
            optionsBuilder.setUseCase(FaceMeshDetectorOptions.BOUNDING_BOX_ONLY)
        }
        detector = FaceMeshDetection.getClient(optionsBuilder.build())
    }*/

    /*
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
    */

    fun verifyNow(docImageBitmap: Bitmap, cameraImageBitmap: Bitmap) {
       /* val docImage = InputImage.fromBitmap(docImageBitmap, 0)
        val cameraImage = InputImage.fromBitmap(cameraImageBitmap, 0)

        val docDetectionTask = faceDetector.process(docImage)
        val imageDetectionTask = faceDetector.process(cameraImage)

        Tasks.whenAll(docDetectionTask, imageDetectionTask)
            .addOnSuccessListener {
                val docResults = docDetectionTask.result
                val cameraResults = imageDetectionTask.result

                if (docResults.size == 1 && cameraResults.size == 1) {
                    Log.e(
                        "CODEPOINT",
                        "verifyNow: result ---> " + docResults.size + " " + cameraResults.size
                    )
                    docResultLogic(docResults.first(), cameraResults.first())
//                    val result  = compareFaces(docResults.first(), cameraResults.first())

//                    onFaceVerificationListener?.isFaceVerificationResult(result)

                }
            }
            .addOnFailureListener {
                Log.e("CODEPOINT", "verifyNow: " + it.message)
            }*/

        val task1 = faceDetector.process(InputImage.fromBitmap(docImageBitmap, 0))
        val task2 = faceDetector.process(InputImage.fromBitmap(cameraImageBitmap, 0))

        Tasks.whenAllComplete(task1, task2)
            .addOnSuccessListener {
                val faces1 = task1.result?.getOrNull(0)?.let { it as? Face }
                val faces2 = task2.result?.getOrNull(0)?.let { it as? Face }


                if (faces1 != null && faces2 != null) {
                    val similarity = compareFaces(faces1, faces2)
                    // Use the similarity value for further processing or comparison
                    onFaceVerificationListener?.isFaceVerificationResult(similarity)

                } else {
                    // Handle the case where one or both faces are not detected
                }
            }
            .addOnFailureListener {
                // Handle any errors that occur during face detection
            }
    }


    private fun docResultLogic(face: Face, cameraFace: Face) {
        Log.e("CODEPOINT", "docResultLogic: logic")
        val boundingBox = face.boundingBox
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)
        val mouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)

        cameraResultsLogic(cameraFace, leftEye, rightEye, nose, mouth)
    }

    private fun cameraResultsLogic(
        face: Face,
        leftEye: FaceLandmark?,
        rightEye: FaceLandmark?,
        nose: FaceLandmark?,
        mouth: FaceLandmark?,
    ) {
        val cameraLeftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val cameraRightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val cameraNose = face.getLandmark(FaceLandmark.NOSE_BASE)
        val cameraMouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)

        val leftEyeDiff = calculateDifference(leftEye, cameraLeftEye)
        val rightEyeDiff = calculateDifference(rightEye, cameraRightEye)
        val noseDiff = calculateDifference(nose, cameraNose)
        val mouthDiff = calculateDifference(mouth, cameraMouth)


        val leftEyePercentage = calculatePercentageDifference(leftEye, cameraLeftEye)
        val rightEyePercentage = calculatePercentageDifference(rightEye, cameraRightEye)
        val nosePercentage = calculatePercentageDifference(nose, cameraNose)
        val mouthPercentage = calculatePercentageDifference(mouth, cameraMouth)

        Log.e(
            "Update Result  ---> % ",
            "cameraResultsLogic: leftEyePercentage " + leftEyePercentage + "" +
                    " rightEyePercentage " + rightEyePercentage + " nosePercentage " + nosePercentage + "" +
                    "mouthPercentage " + mouthPercentage
        )

        val hashMap = HashMap<String,String>()
        hashMap.put("leftEyePercentage", leftEyePercentage.toInt().toString())
        hashMap.put("rightEyePercentage", rightEyePercentage.toInt().toString())
        hashMap.put("nosePercentage", nosePercentage.toInt().toString())
        hashMap.put("mouthPercentage", mouthPercentage.toInt().toString())

//        onFaceVerificationListener?.isFaceVerificationResult(hashMap)

    }


    private fun calculateDifference(landmark1: FaceLandmark?, landmark2: FaceLandmark?): Float {
        val distance = Math.sqrt(
            Math.pow(
                ((landmark1?.position?.x ?: 0f) - (landmark2?.position?.x ?: 0f)).toDouble(),
                2.0
            ) +
                    Math.pow(
                        ((landmark1?.position?.y ?: 0f) - (landmark2?.position?.y
                            ?: 0f)).toDouble(),
                        2.0
                    )
        ).toFloat()

        return distance
    }

    private fun calculatePercentageDifference(
        landmark1: FaceLandmark?,
        landmark2: FaceLandmark?,
    ): Float {
        val difference = calculateDifference(landmark1, landmark2)
        val average =
            (calculateDifference(landmark1, null) + calculateDifference(landmark2, null)) / 2
        val percentageDifference = (difference / average) * 100

        return percentageDifference
    }

    interface FaceVerificationListener {

        fun isFaceVerificationResult(hashMap: Float)

    }


}


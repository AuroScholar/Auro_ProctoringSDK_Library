package com.example.auroproctoringsdk
// Import the necessary Firebase Vision API libraries
import android.graphics.Bitmap
import android.util.Log
import com.example.auroproctoringsdk.detector.FaceDetectorModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory

class ImageObjectDetector(bitmap: Bitmap) {

    // Create a FirebaseVisionImage object from the provided image
    val image: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)

    val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(
        FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .build()
    )

    var objectDetectionTask = detector.processImage(image).addOnSuccessListener { detectedObjects ->
        // Handle the detected objects
        for (obj in detectedObjects) {
            val boundingBox = obj.boundingBox
            val trackingId = obj.trackingId
            val category = obj.classificationCategory
            val confidence = obj.classificationConfidence

            Log.e("TAG", ": --- tracking "+category.toString() )
        }
    }.addOnFailureListener {

    }

}
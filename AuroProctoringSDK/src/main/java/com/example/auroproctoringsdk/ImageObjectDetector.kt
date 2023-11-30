package com.example.auroproctoringsdk
// Import the necessary Firebase Vision API libraries
import android.graphics.Bitmap

class ImageObjectDetector(bitmap: Bitmap) {

    // Create a FirebaseVisionImage object from the provided image
   /* val image: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)

    val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(
        FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .build()
    )*/
    fun getObjectName():String{
        var nameString = ""
       /* var objectDetectionTask = detector.processImage(image).addOnSuccessListener { detectedObjects ->
            // Handle the detected objects
            for (obj in detectedObjects) {
                val boundingBox = obj.boundingBox
                val trackingId = obj.trackingId
                val category = obj.classificationCategory
                val confidence = obj.classificationConfidence

                nameString = category.toString()

            }
        }.addOnFailureListener {
            nameString = it.stackTraceToString()
        }*/
        return nameString
    }
    /*var objectDetectionTask = detector.processImage(image).addOnSuccessListener { detectedObjects ->
        // Handle the detected objects
        for (obj in detectedObjects) {
            val boundingBox = obj.boundingBox
            val trackingId = obj.trackingId
            val category = obj.classificationCategory
            val confidence = obj.classificationConfidence


        }
    }.addOnFailureListener {

    }*/

}
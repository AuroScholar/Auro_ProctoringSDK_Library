package com.example.auroproctoringsdk.detector

import android.content.Context
import android.graphics.*
import android.util.Log

val TAG = "ObjectDetectorHelper"
class ObjectDetectorHelper(var context: Context) {
/*
    fun tensoflowLiteObjectdetectionResultList(bitmap: Bitmap): List<DetectionResult> {
        val image = TensorImage.fromBitmap(bitmap)
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.3f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            context,
            "1.tflite",
            options
        )
//            "salad.tflite",

        val assetManager = context.assets
        val fileList = assetManager.list("") ?: emptyArray()

        val isTfliteFileExists = fileList.contains("salad.tflite")

        if (!isTfliteFileExists) {
            // "salad.tflite" exists in the assets folder
            // Add your code here
            return listOf(DetectionResult(RectF(), "Model file not found"))
        }

       */
/* // Check if "salad.tflite" file exists
        val modelFile = File(context.filesDir, "salad.tflite")
        if (!modelFile.exists()) {
            // Handle case when "salad.tflite" file does not exist
            return listOf(DetectionResult(RectF(), "Model file not found"))
        }*//*


        val results = detector.detect(image)

        if (results.isEmpty()) {
            // Handle case when no objects are detected
            return listOf(DetectionResult(RectF(), "No objects found"))
        }

        return results.map {
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"
            debugPrint(results)
            DetectionResult(it.boundingBox, text)
        }
    }
*/

//    private fun debugPrint(results: List<Detection>) {
//        for ((i, obj) in results.withIndex()) {
//            val box = obj.boundingBox
//
//            Log.d(TAG, "Detected object: ${i} ")
//            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")
//
//            for ((j, category) in obj.categories.withIndex()) {
//                Log.d(TAG, "    Label $j: ${category.label}")
//                val confidence: Int = category.score.times(100).toInt()
//                Log.d(TAG, "    Confidence: ${confidence}%")
//            }
//        }
//    }



}
/**
 * DetectionResult
 *      A class to store the visualization info of a detected object.
 */
data class DetectionResult(val boundingBox: RectF, val text: String)

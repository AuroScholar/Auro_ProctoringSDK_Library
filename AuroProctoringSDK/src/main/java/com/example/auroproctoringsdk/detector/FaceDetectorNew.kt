/*
package com.example.auroproctoringsdk.detector

import android.content.Context
import android.graphics.Bitmap
import android.media.FaceDetector.Face
import android.util.SparseArray
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectorNew(private val context: Context) {

    private val faceDetector = FaceDetection.getClient(

        FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()

    )

    fun compareFaces(faces1: SparseArray<Face>, faces2: SparseArray<Face>) {
        for (i in 0 until faces1.size()) {
            val face1 = faces1.valueAt(i)
            for (j in 0 until faces2.size()) {
                val face2 = faces2.valueAt(j)
                // Compare the faces using face1 and face2
                // ...
            }
        }
    }

}*/

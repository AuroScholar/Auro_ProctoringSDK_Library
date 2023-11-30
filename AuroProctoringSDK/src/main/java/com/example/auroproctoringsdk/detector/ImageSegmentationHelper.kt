package com.example.auroproctoringsdk.detector

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Helper class for performing image segmentation using the SubjectSegmentation API.
 * This class encapsulates the functionality for obtaining foreground segmentation results from input images.
 */
object ImageSegmentationHelper {

    // Options for configuring the SubjectSegmenter
    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundConfidenceMask()
        .enableForegroundBitmap()
        .build()

    // SubjectSegmenter instance initialized with the specified options
    private val segmenter = SubjectSegmentation.getClient(options)

    /**
     * Asynchronously processes the given input Bitmap image and retrieves the foreground segmentation result.
     *
     * @param image The input image in Bitmap format to be segmented.
     * @return A suspend function that, when invoked, provides the result Bitmap of the foreground segmentation.
     * @throws Exception if there is an error during the segmentation process.
     */
    suspend fun getResult(image: Bitmap) = suspendCoroutine {
        // Convert the input Bitmap image to InputImage format
        val inputImage = InputImage.fromBitmap(image, 0)

        // Process the input image using the SubjectSegmenter
        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                // Resume the coroutine with the foreground Bitmap result on success
                it.resume(result.foregroundBitmap)
            }
            .addOnFailureListener {e ->
                // Resume the coroutine with an exception in case of failure
                it.resumeWithException(e)
            }
    }
}
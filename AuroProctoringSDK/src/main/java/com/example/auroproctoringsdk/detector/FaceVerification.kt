package com.example.auroproctoringsdk.detector

import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceVerification() {

    /** Listener that gets notified when a face detection result is ready. */
    private var onFaceVerificationListener: FaceVerificationListener? = null
    fun setFaceVerificationListener(listener: FaceVerificationListener) {
        onFaceVerificationListener = listener
    }
    private val registered: HashMap<String, SimilarityClassifier.Recognition> = HashMap<String, SimilarityClassifier.Recognition>() //saved Faces

    var embeedings= ArrayList<FloatArray>()
    var IMAGE_MEAN = 128.0f
    var IMAGE_STD = 128.0f
    var OUTPUT_SIZE = 192 //Output size of model

    private val SELECT_PICTURE = 1

    var detector: FaceDetector? = null
    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .build()

    fun addFace(){

    }

    interface FaceVerificationListener {

        fun isFaceVerificationResult(hashMap: Float)

    }

    interface SimilarityClassifier {
        class Recognition(
            var id: String?,
            /** Display name for the recognition.  */
            var title: String?,
            var distance: Float?,
        ) {
            var extra: Any?

            init {
                id = id
                title = title
                distance = distance
                this.extra = null
            }

            @JvmName("setExtra1")
            fun setExtra(extra: Any?) {
                this.extra = extra
            }

            @JvmName("getExtra1")
            fun getExtra(): Any? {
                return extra
            }

            override fun toString(): String {
                var resultString = ""
                if (id != null) {
                    resultString += "[$id] "
                }
                if (title != null) {
                    resultString += "$title "
                }
                if (distance != null) {
                    resultString += String.format("(%.1f%%) ", distance!! * 100.0f)
                }
                return resultString.trim { it <= ' ' }
            }
        }
    }

}


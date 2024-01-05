package com.example.auroproctoringsdk.detector.testcode1

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.text.InputType
import android.util.Log
import android.util.Pair
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.GuardedBy
import androidx.appcompat.app.AppCompatActivity
import com.example.auroproctoringsdk.detector.Frame
import com.example.auroproctoringsdk.detector.testCode.SimilarityClassifier
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

class FaceCompareTensorFlowLite(val context: Context) {

    /** [Executor] used to run the face detection on a background thread.  */
    private var faceDetectionExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** Controls access to [isProcessing], since it can be accessed from different threads. */
    private val lock = Object()

    @GuardedBy("lock")
    private var isProcessing = false

    var detector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build()
    )

    var embeedings: Array<FloatArray> = emptyArray()
    var tfLite: Interpreter? = null

    var start = true
    var flipX = false
    private val registered: HashMap<String, SimilarityClassifier.Recognition> =
        HashMap<String, SimilarityClassifier.Recognition>() //saved Faces
    var OUTPUT_SIZE = 192 //Output size of model
    var intValues = intArrayOf()
    var inputSize = 112 //Input size for model
    var isModelQuantized = false

    var IMAGE_MEAN = 128.0f
    var IMAGE_STD = 128.0f


    var modelFile = "mobile_face_net.tflite" //model name

    var distance = 1.0f
    var developerMode = false


    init {
       /* //Load model
        try {
            tfLite = loadModelFile(context as Activity, modelFile)?.let { Interpreter(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }*/

      /*  val (fileChannel, startOffset, declaredLength) = triple()

// Load model
        try {
            tfLite = Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength))
        } catch (e: IOException) {
            e.printStackTrace()
        }
*/

    }

    private fun triple(): Triple<FileChannel, Long, Long> {
        val assetManager = context.assets
        val modelPath = "mobile_face_net.tflite" // Replace with the actual model file name
        val fileDescriptor = assetManager.openFd(modelPath)
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return Triple(fileChannel, startOffset, declaredLength)
    }

    private fun insertToSP(jsonMap: HashMap<String, SimilarityClassifier.Recognition>, mode: Int) {
        if (mode == 1) jsonMap.clear()
        else if (mode == 0) jsonMap.putAll(readFromSP())
        val jsonString = Gson().toJson(jsonMap)

        val sharedPreferences = context.getSharedPreferences("HashMap", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("map", jsonString)

        editor.apply()
        Toast.makeText(context, "Recognitions Saved", Toast.LENGTH_SHORT).show()
    }


    fun readFromSP(): HashMap<String, SimilarityClassifier.Recognition> {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("HashMap", Context.MODE_PRIVATE)
        val defValue = Gson().toJson(java.util.HashMap<String, SimilarityClassifier.Recognition>())
        val json = sharedPreferences.getString("map", defValue)


        // System.out.println("Output json"+json.toString());
        val token: TypeToken<java.util.HashMap<String?, SimilarityClassifier.Recognition?>?> =
            object : TypeToken<java.util.HashMap<String?, SimilarityClassifier.Recognition?>?>() {}
        val retrievedMap =
            Gson().fromJson<java.util.HashMap<String, SimilarityClassifier.Recognition>>(
                json, token.type
            )

        for ((key, value) in retrievedMap) {
            val output = Array(1) { FloatArray(OUTPUT_SIZE) }
            var arrayList = value.extra as ArrayList<*>
            arrayList = arrayList[0] as ArrayList<*>
            for (counter in 0 until arrayList.size) {
                output[0][counter] = (arrayList[counter] as Double).toFloat()
            }
            value.extra = output
            //println("Entry output $key ${value.extra}")
        }

        //        System.out.println("OUTPUT"+ Arrays.deepToString(outut));
        Toast.makeText(context, "Recognitions Loaded", Toast.LENGTH_SHORT).show()
        return retrievedMap
    }

    fun addFace() {
        start = false
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter Name")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("ADD") { dialog, which ->
            val result = SimilarityClassifier.Recognition("0", "", -1f)
            result.extra = embeedings
            registered[input.text.toString()] = result
            start = true
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            start = true
            dialog.cancel()
        }

        builder.show()


    }


    private fun convectionBitmap(frame: Frame): Bitmap {
        val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 100, out)
        val imageBytes = out.toByteArray()
        val lastUpdatedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        out.flush()
        out.close()
        return rotateBitmap(lastUpdatedBitmap, -90, false, false)
    }

    fun onActivityResult(bitmap: Bitmap) {
        try {
            val impphoto = InputImage.fromBitmap(bitmap, 0)
            detector.process(impphoto).addOnSuccessListener { faces ->

                if (faces.isNotEmpty()) {

                    val face = faces[0]

                    val frame_bmp1: Bitmap = rotateBitmap(bitmap, 0, false, false)


                    //face_preview.setImageBitmap(frame_bmp1);
                    val boundingBox = RectF(face.boundingBox)

                    val cropped_face: Bitmap = getCropBitmapByCPU(frame_bmp1, boundingBox)

                    val scaled = getResizedBitmap(cropped_face, 112, 112)
                    // face_preview.setImageBitmap(scaled);
                    recognizeImage(scaled)
                    addFace()

                }
            }

        } catch (e: IOException) {
            Log.e("TAG", "liveFace: " + e.message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun recognizeImage(bitmap: Bitmap) {

        // set Face to Preview
        // face_preview.setImageBitmap(bitmap)

        //Create ByteBuffer to store normalized image
        val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)

        imgData.order(ByteOrder.nativeOrder())
        intValues = IntArray(inputSize * inputSize)
        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        imgData.rewind()

        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                } else { // Float model
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }

        //imgData is input to our model
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: HashMap<Int, Any> = HashMap()

        embeedings =
            Array(1) { FloatArray(OUTPUT_SIZE) } //output of model will be stored in this variable

        outputMap.put(0, embeedings)

        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap) //Run model

        var distance_local = Float.MAX_VALUE
        val id = "0"
        val label = "?"

        if (registered.size > 0) {

            val nearest: List<Pair<String, Float>> =
                findNearest(embeedings[0]) //Find 2 closest matching face


            if (nearest.get(0) != null) {

                val name = nearest[0].first //get name and distance of closest matching face
                // label = name;
                distance_local = nearest[0].second


                if (developerMode) {
                    if (distance_local < distance) {
                        Log.e(
                            "TAG",
                            "Nearest: $name\nDist: ${"%.3f".format(distance_local)}\n2nd Nearest: ${nearest[1].first}\nDist: ${
                                "%.3f".format(nearest[1].second)
                            }"
                        )
                        // reco_name.text = "Nearest: $name\nDist: ${"%.3f".format(distance_local)}\n2nd Nearest: ${nearest[1].first}\nDist: ${"%.3f".format(nearest[1].second)}"
                    } else {
                        Log.e(
                            "TAG",
                            "Unknown\nDist: ${"%.3f".format(distance_local)}\nNearest: $name\nDist: ${
                                "%.3f".format(distance_local)
                            }\n2nd Nearest: ${nearest[1].first}\nDist: ${"%.3f".format(nearest[1].second)}"
                        )
                        // reco_name.text = "Unknown\nDist: ${"%.3f".format(distance_local)}\nNearest: $name\nDist: ${"%.3f".format(distance_local)}\n2nd Nearest: ${nearest[1].first}\nDist: ${"%.3f".format(nearest[1].second)}"
                    }
                } else {
                    if (distance_local < distance) {
                        Log.e("RESULT", "recognizeImage: " + name)
                        // reco_name.text = name
                    } else {
                        Log.e("RESULT", "recognizeImage: Unknown")
                        // reco_name.text = "Unknown"
                    }
                }


            }


        }

    }

    private fun findNearest(emb: FloatArray): List<Pair<String, Float>> {
        val neighbourList = mutableListOf<Pair<String, Float>>()
        var ret: Pair<String, Float>? = null
        var prevRet: Pair<String, Float>? = null
        for ((name, recognition) in registered) {
            val knownEmb = (recognition.extra as Array<FloatArray>)[0]
            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = sqrt(distance)
            if (ret == null || distance < ret.second) {
                prevRet = ret
                ret = Pair(name, distance)
            }
        }
        if (prevRet == null) prevRet = ret
        if (ret != null) {
            neighbourList.add(ret)
        }
        if (prevRet != null) {
            neighbourList.add(prevRet)
        }
        return neighbourList
    }


    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    private fun getCropBitmapByCPU(source: Bitmap?, cropRectF: RectF): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            cropRectF.width().toInt(), cropRectF.height().toInt(), Bitmap.Config.ARGB_8888
        )
        val cavas = Canvas(resultBitmap)

        // draw background
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        paint.color = Color.WHITE
        cavas.drawRect(
            RectF(0f, 0f, cropRectF.width(), cropRectF.height()), paint
        )
        val matrix = Matrix()
        matrix.postTranslate(-cropRectF.left, -cropRectF.top)
        cavas.drawBitmap(source!!, matrix, paint)
        if (source != null && !source.isRecycled) {
            source.recycle()
        }
        return resultBitmap
    }


    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean,
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    fun cameraLiveProcess(frame: Frame) {
        synchronized(lock) {
            if (!isProcessing) {
                isProcessing = true
               // faceDetectionExecutor.execute { frame.detectFaces() }
            }
        }
    }

    //    ByteArray
    private fun Frame.detectFaces() {
        val data = data ?: return

        val inputImage = InputImage.fromByteArray(data, size.width, size.height, rotation, format)

        var result: Task<List<Face>> = detector.process(inputImage).addOnSuccessListener { faces ->
            if (faces.size != 0) {
                val face: Face = faces[0]
                val frame_bmp: Bitmap = convectionBitmap(this)
                // Adjust orientation of Face
                val frame_bmp1: Bitmap = rotateBitmap(frame_bmp, 0, false, false)
                // Get bounding box of face
                val boundingBox: RectF = RectF(face.boundingBox)

                // Crop out bounding box from whole Bitmap(image)
                var cropped_face: Bitmap = getCropBitmapByCPU(frame_bmp1, boundingBox)

                if (flipX) cropped_face = rotateBitmap(cropped_face, 0, flipX, false)

                // Scale the acquired Face to 112*112 which is required input for model
                val scaled: Bitmap = getResizedBitmap(cropped_face, 112, 112)

                if (start) {
                    recognizeImage(scaled) // Send scaled bitmap to create face embeddings
                }
            } else {
                if (registered.isEmpty()) {
                    Log.e("TAG", "checkLive: Add Face")
                } else {
                    Log.e("TAG", "checkLive: No Face Detected!")
                }
            }

            isProcessing = true

        }.addOnFailureListener {
            isProcessing = true
        }.addOnCompleteListener {
            isProcessing = true
        }
    }
}

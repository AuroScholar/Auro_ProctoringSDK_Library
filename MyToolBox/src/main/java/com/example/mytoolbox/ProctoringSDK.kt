package com.example.mytoolbox

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.example.mytoolbox.proctoring.FaceDetector
import com.example.mytoolbox.proctoring.Frame
import com.example.mytoolbox.proctoring.LensFacing
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Timer
import java.util.TimerTask


class ProctoringSDK(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private val faceDetector = FaceDetector()
    var dealyInMilliseconds:Long = 30000

    var isDetection = false

    companion object {
        var imageBitmap: Bitmap? = null
    }

    private var timer: Timer? = null

    init {
        surfaceHolder = holder
        surfaceHolder?.addCallback(this)
        this.layoutParams  = ViewGroup.LayoutParams(300,300)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                takePic()
                Log.e("TAG", "run: call take image ")
            }
        }, 0, dealyInMilliseconds) // 1 sec
        //        }, 0, 60000) // 1 mint
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if (surfaceHolder?.surface == null) {
            return
        }/* try {
             camera?.stopPreview()
             camera?.takePicture(null, null, null)

         } catch (e: Exception) {
             e.printStackTrace()
         }*/
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null

        timer?.cancel()
        timer = null
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera?) {

        // Fixing the NullPointerException
        if (data == null) {
            return
        }

        camera?.let {
            // Convert the data to a bitmap
            val parameters = camera.parameters
            val width = parameters.previewSize.width
            val height = parameters.previewSize.height
            val yuvImage = YuvImage(data, parameters.previewFormat, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            val lastUpdatedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            out.flush()
            out.close()
            lastUpdatedBitmap?.let {
                imageBitmap = lastUpdatedBitmap
                if (isDetection) {
                    faceDetector.process(
                        Frame(
                            data,
                            270,
                            Size(width, height),
                            parameters.previewFormat,
                            LensFacing.FRONT
                        )
                    )
                }

            }

        }

    }

    fun takePic() {
        camera?.setPreviewCallback(this@ProctoringSDK)
    }

    fun startProctoring(mainActivity: FaceDetector.OnProctoringResultListener) {
        isDetection = true
        faceDetector.setonFaceDetectionFailureListener(mainActivity)
    }

    fun setProctoringTimeInterval(dealInMilliseconds:Long){
        this.dealyInMilliseconds = dealInMilliseconds
    }

}
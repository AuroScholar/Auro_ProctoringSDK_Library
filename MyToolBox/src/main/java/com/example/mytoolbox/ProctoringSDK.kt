package com.example.mytoolbox

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.mytoolbox.proctoring.FaceDetector
import com.example.mytoolbox.proctoring.FaceDetectorModel
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
    private var deadlyInMilliseconds: Long = 30000
    private var imageBitmap: Bitmap? = null
    private var isDetection = true
    private val imgList = mutableListOf<Bitmap>()
    private val captureImageList = MutableLiveData<List<Bitmap>>()
    private var timer: Timer? = null

    private var borderPaint: Paint? = null
    private var backgroundPaint: Paint? = null

    private var borderColor: Int = Color.GREEN

    val builder = AlertDialog.Builder(context).create()


    init {
        initBoarder()
        dialogStepUp()

        this.layoutParams = ViewGroup.LayoutParams(300, 300)
        this.surfaceHolder = holder
        this.surfaceHolder?.addCallback(this)
        this.imgList.clear()


    }

    private fun dialogStepUp() {

    }

    private fun initBoarder() {
        setWillNotDraw(false)
        this.borderPaint = Paint()
        borderPaint?.let {
            it.color = borderColor
            it.style = Paint.Style.STROKE
            it.strokeWidth = 5f
            it.isAntiAlias = true
            it.isDither = true
            it.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
        }

        this.backgroundPaint = Paint()
        backgroundPaint?.let {
            it.color = Color.TRANSPARENT
            it.style = Paint.Style.FILL
            it.isAntiAlias = true
            it.isDither = true
            it.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
        }

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
                if (isDetection) {
                    takePic()
                    Log.e("TAG", "run: call take image ")
                }
            }
        }, 0, deadlyInMilliseconds) // 1 sec
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            (context as Activity).runOnUiThread {
                borderPaint?.let { borderPaint ->
                    backgroundPaint?.let { backgroundPaint ->
                        val borderRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                        it.drawRect(borderRect, borderPaint)
                        val backgroundRect =
                            RectF(5f, 5f, width.toFloat() - 5f, height.toFloat() - 5f)
                        it.drawRect(backgroundRect, backgroundPaint)
                    }
                }
            }
        }
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
//                    imgList.add(lastUpdatedBitmap)
//                    captureImageList.value = imgList.toList()
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

    fun startProctoring(
        onProctoringResultListener: FaceDetector.OnProctoringResultListener,
        mainActivity: AppCompatActivity
    ) {
        isDetection = true
        faceDetector.setonFaceDetectionFailureListener(onProctoringResultListener)
        getFaceLiveResult(mainActivity)

    }

    fun setProctoringTimeInterval(dealInMilliseconds: Long) {
        this.deadlyInMilliseconds = dealInMilliseconds
    }

    fun getCaptureImagesList(): MutableLiveData<List<Bitmap>> {
        return captureImageList
    }

    fun getFaceLiveResult(activity: AppCompatActivity) {
        faceDetector.getLiveFaceResult().observe(activity) { liveResult ->
            activity.runOnUiThread {

                if (!liveResult.faceCount.equals("") && liveResult.faceCount<=1){

                    if (liveResult.faceCount==1 && liveResult.isMouthOen){
                        alert(activity,"Face Count  ",liveResult.faceCount.toString() + " \n Mouth is Open "+liveResult.isMouthOen)
                    }else{
                        hide()
                    }

                }else{
                   alert(activity,"Face Count ","Found Multiple Faces "+liveResult.faceCount.toString())
                }

            }
        }
    }

    fun startStopDetection(): Boolean {
        isDetection = !isDetection
        return isDetection
    }

    fun stopProctoring(): Boolean {
        return isDetection.apply { false }
    }

     @SuppressLint("SuspiciousIndentation")
     private fun alert(context: AppCompatActivity, title: String?, message: String?) {
         val layoutInflater: LayoutInflater = LayoutInflater.from(context)
         val view = layoutInflater.inflate(R.layout.custom_dialog, null)
             builder.setCancelable(false)
         val button = view.findViewById<Button>(R.id.btn_ok)
         val tvTitle = view.findViewById<TextView>(R.id.tv_title)
         val tvMessage = view.findViewById<TextView>(R.id.tv_message)
         builder.setView(view)
         tvTitle.text = title
         tvMessage.text = message
         button.setOnClickListener {
             builder.dismiss()
         }
         builder.show()
     }
    private fun hide(){
        builder.hide()
    }

}
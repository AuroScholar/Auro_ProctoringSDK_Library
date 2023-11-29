package com.example.auroproctoringsdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class Utils {

    fun saveBitmapIntoImageInternalDir(tempBitmap: Bitmap, context: Context): String = runBlocking {
        var path = ""
        val image = tempBitmap.rotateBitmap(0F)

        if (!getPathDir(context).exists()) {
            getPathDir(context).mkdirs()
        }

        val fileName = "image_${Calendar.getInstance().timeInMillis}.jpg"
        val file = File(getPathDir(context), fileName)


        val outputStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        path = file.absolutePath.toString()

        path
    }

    fun removeFolder(context: Context) {
        if (getPathDir(context).exists()){
            getPathDir(context).deleteRecursively()
        }
    }

    fun getPathDir(context: Context): File {
        val folderName = context.getAppName() ?: "Unknown"
        return File(context.getExternalFilesDir(null), folderName)
    }

    fun getSaveImageInit(context: Context){
        removeFolder(context)
    }


    private fun Bitmap.rotateBitmap(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun Context.getAppName(): String? = applicationInfo.loadLabel(packageManager).toString()
}
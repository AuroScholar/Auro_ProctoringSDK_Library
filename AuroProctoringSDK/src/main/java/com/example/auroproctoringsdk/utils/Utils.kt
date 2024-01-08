package com.example.auroproctoringsdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Calendar

class Utils {
    fun saveBitmapIntoImageInternalDir(tempBitmap: Bitmap, context: Context): String = runBlocking {
        var path = ""
        val image = tempBitmap.rotateBitmap(0F)

        val pathDir = getPathDir(context)
        if (!pathDir.exists()) {
            pathDir.mkdirs() // Create the necessary directories if they don't exist
        }

        val fileName = "image_${Calendar.getInstance().timeInMillis}.jpg"
        val file = File(getPathDir(context), fileName)
        try {
            val outputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            path = file.absolutePath.toString()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            // Recycle the bitmap after it has been used
            image.recycle()
        }
        path
    }

    fun removeFolder(context: Context) {
        if (getPathDir(context).exists()) {
            getPathDir(context).deleteRecursively()
        }
    }

    fun getPathDir(context: Context): File {
        val folderName = context.getAppName() ?: "Unknown"
        return File(context.getExternalFilesDir(null), folderName)
    }

    fun getSaveImageInit(context: Context) {
        removeFolder(context)
    }


    private fun Bitmap.rotateBitmap(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun Context.getAppName(): String? = applicationInfo.loadLabel(packageManager).toString()
}
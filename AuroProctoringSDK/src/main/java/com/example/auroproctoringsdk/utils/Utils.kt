package com.example.auroproctoringsdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar


class Utils() {
    fun saveBitmapIntoImageInternalDir(tempBitmap: Bitmap, context: Context,removeFolder:Boolean): String? {

        var image = tempBitmap.rotateBitmap(270F)

        var folderName = context.getAppName()

        if (folderName.isNullOrBlank()) {
            folderName = "Unknown"
        }

        val directory = File(context.getExternalFilesDir(null), folderName)

        if (removeFolder){
            directory.deleteRecursively()
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = "image_${Calendar.getInstance().time}.jpg"
        val file = File(directory, fileName)
        val outputStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return file.absolutePath
    }

    private fun Bitmap.rotateBitmap(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()


}


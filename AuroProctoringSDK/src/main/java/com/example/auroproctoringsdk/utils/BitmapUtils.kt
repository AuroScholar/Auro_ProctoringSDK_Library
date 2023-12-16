package com.example.auroproctoringsdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BitmapUtils {

    fun getSaveImaged(context: Context, source: Bitmap, target: Bitmap): ArrayList<String> {

        val source = saveBitmapToLocalStorage(context, source, "source")
        val target = saveBitmapToLocalStorage(context, target, "target")

        return arrayListOf(source,target)
    }

    private fun saveBitmapToLocalStorage(context: Context, bitmap: Bitmap, filename: String): String {
        val fileName = "$filename.jpg"
        val directory =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "detection")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

}

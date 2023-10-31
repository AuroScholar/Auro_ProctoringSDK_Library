package com.example.auroproctoringsdk.detector

import android.util.Size

//ByteBuffer
data class Frame(
    @Suppress("ArrayInDataClass") val data: ByteArray?,
    val rotation: Int,
    val size: Size,
    val format: Int,
    val lensFacing: LensFacing
)
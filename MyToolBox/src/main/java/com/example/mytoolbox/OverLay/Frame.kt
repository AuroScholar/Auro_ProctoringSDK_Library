package com.example.mytoolbox.OverLay

import android.util.Size

data class Frame(
    @Suppress("ArrayInDataClass") val data: ByteArray?,
    val rotation: Int,
    val size: Size,
    val format: Int,
    val lensFacing: LensFacing
)
package com.auro.proctoringsdk.copypastestop

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE

class ClipboardManagerHelper(private val context: Context) {

    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    fun clearClipboard() {
        clipboardManager.addPrimaryClipChangedListener {
            try {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
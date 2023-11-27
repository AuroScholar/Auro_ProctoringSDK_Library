package com.example.auroproctoringsdk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CustomAlertDialog(context: Context) {

    private val dialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(context).setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which -> })
        dialog = builder.create()
    }

    fun show(title: String, message: String) {
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.show()
    }

    fun hide() {
        if (dialog.isShowing) {
            dialog.dismiss()
            dialog.hide()
        }
    }
}


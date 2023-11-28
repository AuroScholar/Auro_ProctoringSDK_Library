package com.example.auroproctoringsdk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CustomAlertDialog(context: Context) {


    private val dialog: AlertDialog
    private var isDialogShowing = false

    init {
        val builder = AlertDialog.Builder(context).setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                isDialogShowing = true
            })
        dialog = builder.create()
    }

    fun show(title: String, message: String) {
        if (!isDialogShowing && !message.isNullOrBlank() && !title.isNullOrBlank()) {
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.show()

        }

    }

    fun hide() {
        if (isDialogShowing && !dialog.isShowing) {
            dialog.dismiss()
            dialog.hide()
            isDialogShowing = false
        }
    }
}


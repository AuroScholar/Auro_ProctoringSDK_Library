package com.example.auroproctoringsdk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CustomAlertDialog(context: Context) {

    private val dialog: AlertDialog
    private var isAlertDialog = false

    init {
        val builder = AlertDialog.Builder(context).setCancelable(false)
            .setPositiveButton("Exit", DialogInterface.OnClickListener { dialog, which ->
                isAlertDialog = true
            })

        dialog = builder.create()
    }

    fun show(title: String?, message: String?) {
        if (!isAlertDialog) {
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.show()
        }
    }

    fun hide() {
        if (isAlertDialog) {
            dialog.dismiss()
            dialog.hide()
            isAlertDialog = false
        }
    }

    fun hideForcefully() {
        if (dialog.isShowing) {
            dialog.dismiss()
            dialog.hide()
            isAlertDialog = false
        }
    }
}


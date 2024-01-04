package com.example.auroproctoringsdk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CustomAlertDialog(context: Context) {
    private val dialog: AlertDialog
    private var isOkClicked = false

    init {
        val builder = AlertDialog.Builder(context).setCancelable(false)
            .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                isOkClicked = true
                reset()
                dialog.dismiss()
            })

        dialog = builder.create()
    }

    fun show(title: String?, message: String?) {
        if (!isOkClicked && !dialog.isShowing) {
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.show()
        }
    }

    fun hide() {
        if (isOkClicked) {
            dialog.dismiss()
            isOkClicked = false
        }
    }

    fun hideForcefully() {
        if (dialog.isShowing) {
            dialog.dismiss()
            isOkClicked = false
        }
    }

    fun reset() {
        isOkClicked = false
    }
}


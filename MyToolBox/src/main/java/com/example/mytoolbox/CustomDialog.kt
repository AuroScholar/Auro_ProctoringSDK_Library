package com.example.mytoolbox

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView

class CustomDialog(context: Context) : Dialog(context) {

    private var title: String? = null
    private var message: String? = null
    private var tvTitle: TextView? = null
    private var tvMessage: TextView? = null
    private var btnOk: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog)

        tvTitle = this.findViewById<TextView>(R.id.tv_title)
        tvMessage = this.findViewById<TextView>(R.id.tv_message)
        btnOk = this.findViewById<Button>(R.id.btn_ok)


        tvTitle?.let { tvTitle ->
            tvMessage?.let { tvMessage ->
                tvTitle.text = title
                tvMessage.text = message
            }
        }

        btnOk?.let { btnOk ->
            btnOk.setOnClickListener {
                dismiss()
            }
        }


    }

    fun setTitle(title: String): CustomDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): CustomDialog {
        this.message = message
        return this
    }
}

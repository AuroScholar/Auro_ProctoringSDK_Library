package com.example.auroproctoringsdk.screenReader

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class StopTextReading {
    fun stopTextReading(context: Context) {
        val textViewIds = mutableListOf<Int>()
        val rootView = findRootView(context)
        val views = getAllChildren(rootView)
        for (view in views) {
            if (view is TextView) {
                textViewIds.add(view.id)
              /*  view.text = " No Text Read Any App "*/
                viewAccessNo(view)
            }else if (view is Button){
              /*  view.text = "No Text Read"
                view.hint = "No Text Read"*/
                viewAccessNo(view)
            }else if (view is CheckBox){
                /*view.text = "No Text Read"
                view.hint = "No Text Read"*/
                viewAccessNo(view)
            }else if (view is ToggleButton){
                /*view.text = "No Text Read"
                view.hint = "No Text Read"*/
                viewAccessNo(view)
            }
        }
    }

    private fun viewAccessNo(view: View) {
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
    }

    private fun findRootView(context: Context): View {
        return (context as AppCompatActivity).findViewById<View>(android.R.id.content)
    }

    private fun getAllChildren(v: View): List<View> {
        if (v !is ViewGroup) {
            return listOf(v)
        }

        val result = mutableListOf<View>()
        val group = v as ViewGroup
        val count = group.childCount

        for (i in 0 until count) {
            val child = group.getChildAt(i)
            result.addAll(getAllChildren(child))
        }

        return result
    }
}

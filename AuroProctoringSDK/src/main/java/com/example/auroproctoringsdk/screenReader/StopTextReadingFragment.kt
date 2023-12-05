package com.example.auroproctoringsdk.screenReader

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

class StopTextReadingFragment {

    fun stopTextReading(context: Context/*view: View*/) {
        val textViewIds = mutableListOf<Int>()
        val view  = findRootView(context)
        val views = getAllChildren(view)
        for (view in views) {
            if (view is TextView) {
                textViewIds.add(view.id)
                view.text = "No Read"
                viewAccessNo(view)
            } else if (view is Button) {
                viewAccessNo(view)
            } else if (view is CheckBox) {
                viewAccessNo(view)
            } else if (view is ToggleButton) {
                viewAccessNo(view)
            }
        }
    }

    private fun viewAccessNo(view: View) {
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
    }

    private fun findRootView(context: Context): View {
        return (context as FragmentActivity).findViewById<View>(android.R.id.content)
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

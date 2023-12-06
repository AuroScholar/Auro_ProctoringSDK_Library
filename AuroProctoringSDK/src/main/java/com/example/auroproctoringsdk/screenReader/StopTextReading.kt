package com.example.auroproctoringsdk.screenReader

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class StopTextReading {

    fun stopTextReading(context: Context) {
        val textViewIds = mutableListOf<Int>()
        val rootView = findRootView(context)
        val views = getAllChildren(rootView)
        for (view in views) {
            if (view is TextView) {
                textViewIds.add(view.id)
                view.text = "no read "
                /*  view.text = " No Text Read Any App "*/
                stopTextReading(view)
            } else if (view is Button) {
                /*  view.text = "No Text Read"
                  view.hint = "No Text Read"*/
                stopTextReading(view)
            } else if (view is CheckBox) {
                /*view.text = "No Text Read"
                view.hint = "No Text Read"*/
                stopTextReading(view)
            } else if (view is ToggleButton) {
                /*view.text = "No Text Read"
                view.hint = "No Text Read"*/
                stopTextReading(view)
            }
        }
    }






    private fun stopTextReading(view: View) {
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
    }

    private fun findRootView(context: Context): View {
        if (checkContext(context)) {
            if (context is Activity) {
                // Handle activity-specific logic
                return (context as AppCompatActivity).window.decorView.findViewById(android.R.id.content)

            } else if (context is Fragment) {
                // Handle fragment-specific logic
                return (context as FragmentActivity).window.decorView.findViewById(android.R.id.content)
            }
        }
        return View(context) // Return a default view if the context is not valid
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


    fun checkContext(context: Any): Boolean {
        return context is Activity || context is Fragment
    }
}

//package com.example.auroproctoringsdk.screenReader
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.util.Log
//
//class ScreenShortDetector(private val context: Context) {
//    // not getting screen short detect
//
//    private val receiver: ScreenShortReceiver by lazy { ScreenShortReceiver() }
//
//    fun startListening() {
//        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
//        filter.addAction(Intent.ACTION_SCREEN_OFF)
//        context.registerReceiver(receiver, filter)
//        Log.d("ScreenShortDetector", "Listening for screen short events")
//    }
//
//    fun stopListening() {
//        context.unregisterReceiver(receiver)
//        Log.d("ScreenShortDetector", "Stopped listening for screen short events")
//    }
//}
//
//class ScreenShortReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.EXTRA_RESULT_RECEIVER) {
//            Log.d("ScreenShortReceiver", "Screen short taken")
//            // Perform actions when screen short is detected
//        }
//        if (intent.action == Intent.ACTION_SCREEN_ON) {
//            Log.d("ScreenShortReceiver", "Screen turned on")
//        } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
//            Log.d("ScreenShortReceiver", "Screen turned off")
//        }
//    }
//}

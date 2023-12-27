//package com.example.auroproctoringsdk.dnd
//
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.provider.Settings
//import com.example.auroproctoringsdk.R
//
//class DndPermissionHelper(private val context: Context) {
//
//    var positiveButtonClicked = false
//
//    private val alertDialog : AlertDialog by lazy {
//        AlertDialog.Builder(context)
//            .setTitle(context.getString(R.string.permission_required))
//            .setCancelable(false)
//            .setMessage(R.string.to_enable_do_not_disturb_mode_you_need_to_grant_permission)
//            .setPositiveButton(R.string.goto_permission) { dialog, _ ->
//                if (!positiveButtonClicked) {
//                    positiveButtonClicked = true
//                    if (!DNDManager(context).checkDndPermission()) {
//                        dialog.dismiss()
//                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//                        context.startActivity(intent)
//                    }else{
//                        hideAlertDialog()
//                    }
//                }
//            }
//            .create()
//    }
//
//
//    fun checkAndHideAlertDialog(context: Context) {
//        if (DNDManager(context).checkDndPermission()) {
//            hideAlertDialog()
//        }
//    }
//
//    fun showAlertDialog() {
//        if (!DNDManager(context).checkDndPermission() && !positiveButtonClicked) {
//            if (!alertDialog.isShowing){
//                alertDialog.show()
//            }
//        }
//    }
//
//    fun hideAlertDialog() {
//        if (alertDialog.isShowing) {
//            alertDialog.dismiss()
//            alertDialog.hide()
//        }
//    }
//
//}
package com.example.auroproctoringsdk.screenBarLock;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class StatusBarLocker {
    private Context context;
    private CustomViewGroup view;

    public StatusBarLocker(Context context) {
        this.context = context;
    }

    public void lock() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
            WindowManager manager = getWindowManager();
            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // fix
            } else {
                localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            int result = 0;
            if (resId > 0) {
                result = context.getResources().getDimensionPixelSize(resId);
            } else {
                // Use Fallback size:
                result = 60; // 60px Fallback
            }
            localLayoutParams.height = result;
            localLayoutParams.format = PixelFormat.TRANSPARENT;
            view = new CustomViewGroup(context);
            manager.addView(view, localLayoutParams);
        }

    }

    private WindowManager getWindowManager() {
        return ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
    }

    public void release() {
        if (view != null) {
            getWindowManager().removeView(view);
        }
    }

    public static void askPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                context.startActivity(myIntent);
            }
        }
    }

    private static class CustomViewGroup extends ViewGroup {
        public CustomViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }


    @SuppressLint({"WrongConstant", "PrivateApi"})
    public static void setExpandNotificationDrawer(Context context, boolean expand) {
        try {
            Object statusBarService = context.getSystemService("statusbar");
            String methodName;

            if (expand) {
                methodName = (Build.VERSION.SDK_INT >= 22) ? "expandNotificationsPanel" : "expand";
            } else {
                methodName = (Build.VERSION.SDK_INT >= 22) ? "collapsePanels" : "collapse";
            }

            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method method = statusBarManager.getMethod(methodName);
            method.invoke(statusBarService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void statusBarLock(Context context){
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        Object sbservice = context.getSystemService("statusbar");
        try {
            Class statusbarManager = Class.forName("android.app.StatusBarManager");
            if (currentApiVersion <= 16) {
                Method collapse = statusbarManager.getMethod("collapse");
                collapse.invoke(sbservice);
            } else {
                Method collapse2 = statusbarManager.getMethod("collapsePanels");
                collapse2.invoke(sbservice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

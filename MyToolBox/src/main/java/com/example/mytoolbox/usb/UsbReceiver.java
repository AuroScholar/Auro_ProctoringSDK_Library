package com.example.mytoolbox.usb;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            // A USB device has been attached
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
         //   if (device != null) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null){
                        //call method to set up device communication
                    }
                }
                else {
                    Log.d(TAG, "permission denied for device " + device);
                }
           // }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            // A USB device has been detached
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                // Handle the USB device detachment
            }
        }
    }
}
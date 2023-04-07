package com.example.aeps_sdk

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class PlugInControlReceiver(val context: Activity, val listener: ConnectionLostCallback) :
    BroadcastReceiver() {
    private var mcontext = context
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.v("PlugInControlReceiver", "action: $action")
        if (action!!.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            Toast.makeText(context, "USB Connected", Toast.LENGTH_SHORT).show()
        }else if(action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")){
            Toast.makeText(context, "USB Disconnected", Toast.LENGTH_SHORT).show()
        }

    }
}
package com.example.geoincendios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BroadCast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(BroadCast::class.java.simpleName, "Service Stops!!!")
        context!!.startService(Intent(context,MyService::class.java))
    }

}
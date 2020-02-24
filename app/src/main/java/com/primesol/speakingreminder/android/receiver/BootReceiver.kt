package com.primesol.speakingreminder.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver: BroadcastReceiver() {
    private val TAG = "ttt ${this::class.java.simpleName}"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        if(intent != null && intent.action == "android.intent.action.BOOT_COMPLETED"){
            //recreate all alarms by fetching from db
        }
    }
}
package com.primesol.speakingreminder.android.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.primesol.speakingreminder.android.model.Reminder
import java.lang.Exception

class PopupReminderJobService: JobService() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            if(params != null){
                val reminderStr = params.extras.getString(Reminder.REMINDER)
                val reminder = Gson().fromJson(reminderStr, Reminder::class.java) as Reminder
                startForegroundService(reminder)
            }
        }
        catch (e: Exception){e.printStackTrace()}
        return true
    }

    private fun startForegroundService(reminder: Reminder){
        val intent = Intent(baseContext, PopupReminderService::class.java)
        intent.putExtra(Reminder.REMINDER, reminder)
        startService(intent)
    }
}
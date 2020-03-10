package com.primesol.speakingreminder.android.receiver

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.service.PopupReminderJobService
import com.primesol.speakingreminder.android.service.PopupReminderService
import com.primesol.speakingreminder.android.ui.activity.PopupReminderActivity
import com.primesol.speakingreminder.android.utils.Defaults
import com.primesol.speakingreminder.android.utils.MediaPlayerTon
import java.io.File
import java.util.*


class ReminderReceiver: BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private var context: Context? = null
    private var timer: CountDownTimer? = null

    init {
        initMediaPlayer()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        this.context = context
        if(intent != null && intent.action != null){
            if(intent.action == ACTION_REMINDER_TRIGGERED){
                val reminderId = intent.getIntExtra(Reminder.REMINDER_ID, 0)
                val reminderDB = ReminderDB.getInstance(context!!)
                Thread(Runnable {
                    var reminder: Reminder? = null
                    reminder = reminderDB.reminderDao()?.getReminder(reminderId.toString())

                    Log.d(TAG, "Reminder fetched with id: ${reminder?.id}")
                    startAudio(reminder?.audio!!)
                    showNotification(context, true, reminder)

                    reminder.status = Reminder.Status.STATUS_PASSED.name
                    reminderDB.reminderDao()?.updateReminder(reminder)
                }).start()
            }
            else if(intent.action == ACTION_DISMISS_REMINDER){

                stopAudio()
            }
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showNotification(context: Context, addAction: Boolean, reminder: Reminder) {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, context.packageName) }
        else { NotificationCompat.Builder(context) }

        builder.setContentTitle(reminder.title)
        builder.setContentText("")
        builder.setSmallIcon(R.drawable.ic_dummy_clock)
        if(addAction) {
            val intent = Intent(context, ReminderReceiver::class.java)
            intent.action = ACTION_DISMISS_REMINDER
            intent.putExtra(Defaults.DISMISS, true)
            val pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val action = NotificationCompat.Action.Builder(0, context.getString(R.string.dismiss), pIntent).build()
            builder.addAction(action!!)
        }

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val note = builder.build()
        note.flags = Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        notificationManager.notify(1, note)
    }

    private fun startActivity(context: Context, reminder: Reminder){
        val intent = Intent(context, PopupReminderActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Reminder.REMINDER, reminder)
        context.startActivity(intent)
    }

    private fun startForegroundService(context: Context, reminder: Reminder){
        val intent = Intent(context, PopupReminderService::class.java)
        intent.putExtra(Reminder.REMINDER, reminder)
        context.startService(intent)
    }

    private fun scheduleJob(context: Context, reminder: Reminder){
        val bundle = PersistableBundle()
        bundle.putString(Reminder.REMINDER, Gson().toJson(reminder))

        val componentName = ComponentName(context, PopupReminderJobService::class.java)
        val jobInfo = JobInfo.Builder(0, componentName)
        jobInfo.setMinimumLatency(1*1000)
        jobInfo.setOverrideDeadline(3*1000)
        jobInfo.setExtras(bundle)
        val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo.build())
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////

    companion object{
        private val TAG = "ttt ${this::class.java.simpleName}"
        val ACTION_REMINDER_TRIGGERED = "com.primesol.speakingreminder.android.ACTION_REMINDER_TRIGGERED"
        val ACTION_DISMISS_REMINDER = "com.primesol.speakingreminder.android.ACTION_DISMISS_REMINDER"

        fun setAlarm(context: Context, calendar: Calendar, reminderId: Int){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            intent.action = ACTION_REMINDER_TRIGGERED
            intent.putExtra(Reminder.REMINDER_ID, reminderId)
            val pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
        fun deleteAlarm(context: Context, reminder: Reminder){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            intent.action = ACTION_REMINDER_TRIGGERED
            intent.putExtra(Reminder.REMINDER_ID, reminder.id)
            val pendingIntent = PendingIntent.getBroadcast(context, reminder.id!!, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.cancel(pendingIntent)
            //delete audio file
            try {
                val file = File(reminder.audio)
                if(file.exists()) file.delete()
            }catch (e: java.lang.Exception){e.printStackTrace()}
        }
    }

    private fun startAudio(uri: String){
        try {
            mediaPlayer?.setDataSource(uri)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            startPlayerTimer()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun stopAudio(){
        try {
            val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            stopPlayerTimer()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun initMediaPlayer(){
        mediaPlayer = MediaPlayerTon.getInstance()
        mediaPlayer?.isLooping = true
        mediaPlayer?.setOnPreparedListener {}
        mediaPlayer?.setOnCompletionListener {}
    }

    private fun startPlayerTimer(){
        stopPlayerTimer()
        timer = object: CountDownTimer(15000,1000){
            override fun onFinish() {
                try {
                    Log.d(TAG, "onFinish")
                    stopAudio()
                }catch (e: Exception){e.printStackTrace()}
            }

            override fun onTick(millisUntilFinished: Long) {Log.d(TAG, "onTick")}
        }
        timer?.start()
    }

    private fun stopPlayerTimer(){
        try {
            timer?.cancel()
        }catch (e: Exception){e.printStackTrace()}
    }
}
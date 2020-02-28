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
import java.util.*


class ReminderReceiver: BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null

    init {
        initMediaPlayer()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
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
                    //showNotification(context, reminder!!)
                    //startActivity(context, reminder!!)

//                val pm = context.getSystemService(POWER_SERVICE) as PowerManager
//                if(!pm.isInteractive){
//                    val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG)
//                    wl.acquire(3000)
//                    //startForegroundService(context, reminder)
//                    //scheduleJob(context, reminder!!)
//                    wl.release()
//                }

                }).start()
            }
            else if(intent.action == ACTION_DISMISS_REMINDER){
                val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
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
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun startAudio(uri: String){
        try {
            mediaPlayer?.setDataSource(uri)
            mediaPlayer?.prepare()
            mediaPlayer?.start();
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun stopAudio(){
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun initMediaPlayer(){
        mediaPlayer = MediaPlayerTon.getInstance()
        mediaPlayer?.isLooping = true
        mediaPlayer?.setOnPreparedListener {

        }
        mediaPlayer?.setOnCompletionListener {

        }
    }
}
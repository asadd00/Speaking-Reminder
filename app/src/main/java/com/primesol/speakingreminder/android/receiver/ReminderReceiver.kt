package com.primesol.speakingreminder.android.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.content.Intent
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
import java.util.*


class ReminderReceiver: BroadcastReceiver() {
    private val TAG = "ttt ${this::class.java.simpleName}"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        if(intent != null){
            val reminderId = intent.getIntExtra(Reminder.REMINDER_ID, 0)
            val reminderDB = ReminderDB.getInstance(context!!)
            Thread(Runnable {
                var reminder: Reminder? = null
                reminder = reminderDB.reminderDao()?.getReminder(reminderId.toString())

                Log.d(TAG, "Reminder fetched with id: ${reminder?.id}")
                //showNotification(context, reminder!!)
                //startActivity(context, reminder!!)

                val pm = context.getSystemService(POWER_SERVICE) as PowerManager
                if(!pm.isInteractive){
                    val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG)
                    wl.acquire(3000)
                    startActivity(context, reminder!!)
                    //startForegroundService(context, reminder)
                    //scheduleJob(context, reminder!!)
                    wl.release()
                }

            }).start()
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun showNotification(context: Context, reminder: Reminder){
        val builder = NotificationCompat.Builder(context, context.packageName)
        builder.setContentTitle(reminder.title)
        builder.setSmallIcon(R.drawable.ic_dummy_clock)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
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
        fun setAlarm(context: Context, calendar: Calendar, reminderId: Int){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            intent.putExtra(Reminder.REMINDER_ID, reminderId)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}
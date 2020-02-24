package com.primesol.speakingreminder.android.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.repository.ReminderDB
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
                showNotification(context, reminder!!)

//                val pm = context.getSystemService(POWER_SERVICE) as PowerManager
//                //val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
//                val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or
//                        PowerManager.ON_AFTER_RELEASE, TAG)
//                wl.acquire()
//                Toast.makeText(context, "Reminder ID: $reminderId", Toast.LENGTH_LONG).show()
//                wl.release()

            }).start()
        }
    }

    private fun showNotification(context: Context, reminder: Reminder){
        val builder = NotificationCompat.Builder(context, context.packageName)
        builder.setContentTitle(reminder.title)
        builder.setSmallIcon(R.drawable.ic_dummy_clock)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

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
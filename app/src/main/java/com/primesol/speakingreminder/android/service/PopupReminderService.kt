package com.primesol.speakingreminder.android.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.utils.Defaults
import com.primesol.speakingreminder.android.utils.MediaPlayerTon

class PopupReminderService : Service() {
    private val TAG = "ttt ${this::class.java.simpleName}"

    var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        init(intent)
        return START_STICKY
    }

    private fun init(intent: Intent?){
        initMediaPlayer()
        if(intent != null){
            if(intent.hasExtra(Reminder.REMINDER)){
                val reminder: Reminder = intent.getSerializableExtra(Reminder.REMINDER) as Reminder
                val title = reminder.title

                try {
                    mediaPlayer?.setDataSource(reminder.audio)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start();

                    startForeground(1, getNotification(title, true))
                }
                catch (e: Exception){e.printStackTrace()}
            }
            else if(intent.hasExtra(Defaults.DISMISS)){
                try {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    stopForeground(true)
                    stopSelf()
                }
                catch (e: Exception){e.printStackTrace()}
            }
        }
    }

    private fun initMediaPlayer(){
        mediaPlayer = MediaPlayerTon.getInstance()
        mediaPlayer?.isLooping = true
        mediaPlayer?.setOnPreparedListener {

        }
        mediaPlayer?.setOnCompletionListener {

        }
    }

    private fun getNotification(title: String, addAction: Boolean): Notification{
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(baseContext, packageName)
        } else {
            NotificationCompat.Builder(baseContext)
        }

        builder.setContentTitle(title)
        builder.setContentText("")
        builder.setSmallIcon(R.drawable.ic_dummy_clock)
        if(addAction) {
            val intent = Intent(baseContext, PopupReminderService::class.java)
            intent.putExtra(Defaults.DISMISS, true)
            val pIntent = PendingIntent.getService(baseContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val action = NotificationCompat.Action.Builder(0, getString(R.string.dismiss), pIntent).build()
            builder.addAction(action!!)
        }

        return builder.build()
    }
}

package com.primesol.speakingreminder.android.ui.fragment

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.utils.MediaPlayerTon
import kotlinx.android.synthetic.main.fragment_reminder_details.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReminderDetailsFragment : Fragment() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private var mediaPlayer: MediaPlayer? = null

    override fun onDetach() {
        super.onDetach()
        try {
            MediaPlayerTon.getInstance()?.reset()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        initMediaPlayer()
        if(arguments != null){
            val reminder = arguments?.getSerializable(Reminder.REMINDER) as Reminder
            tvTitle.text = reminder.title
            tvDate.text = reminder.dateTime.substring(0, reminder.dateTime.indexOf('_'))
            tvTime.text = (reminder.dateTime.substring(reminder.dateTime.indexOf('_')+1)).replace('-', ':')
            tvAudio.text = reminder.audio.substring(reminder.audio.lastIndexOf('/')+1)
            tvStatus.text = reminder.status.replace('_', ' ')
            when(reminder.status){
                Reminder.Status.STATUS_ACTIVE.name->ivStatus.setImageResource(R.drawable.circle_green)
                Reminder.Status.STATUS_INACTIVE.name->ivStatus.setImageResource(R.drawable.circle_red)
                Reminder.Status.STATUS_PASSED.name->ivStatus.setImageResource(R.drawable.circle_orange)
            }
            if(reminder.status == Reminder.Status.STATUS_PASSED.name) swStatus.visibility = View.INVISIBLE
            swStatus.isChecked = reminder.status == Reminder.Status.STATUS_ACTIVE.name

            bDelete.setOnClickListener {
                showDeleteConfirmationDialog(reminder)
            }

            swStatus.setOnCheckedChangeListener { _, isChecked ->
                toggleReminderStatus(reminder, isChecked)
            }
            ivPlay.setOnClickListener {
                if (mediaPlayer!!.isPlaying) {
                    stopAudio()
                    ivPlay.setImageResource(R.drawable.ic_play)
                }
                else{
                    startAudio(reminder.audio)
                    ivPlay.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

    private fun toggleReminderStatus(reminder: Reminder, isChecked: Boolean){
        val reminderDB = ReminderDB.getInstance(context!!)

        if(isChecked){
            val date = SimpleDateFormat(getString(R.string.db_date_format)).parse(reminder.dateTime) as Date
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.SECOND, 0)
            reminder.status = Reminder.Status.STATUS_ACTIVE.name
            ReminderReceiver.setAlarm(context!!, calendar, reminder.id!!)
            ivStatus.setImageResource(R.drawable.circle_green)
        }
        else{
            reminder.status = Reminder.Status.STATUS_INACTIVE.name
            ReminderReceiver.removeRegisteredAlarm(context!!, reminder)
            ivStatus.setImageResource(R.drawable.circle_red)
        }

        Thread(Runnable {
            reminderDB.reminderDao()?.updateReminder(reminder)
        }).start()
        tvStatus.text = reminder.status.replace('_', ' ')
    }

    private fun showDeleteConfirmationDialog(reminder: Reminder){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.confirmation)
        builder.setMessage(R.string.m_delete_confirm)
        builder.setCancelable(false)
        builder.setNegativeButton(android.R.string.no){ dialog,_ -> dialog.dismiss() }
        builder.setPositiveButton(android.R.string.yes){ _,_ -> deleteReminder(reminder) }
        builder.create().show()
    }

    private fun deleteReminder(reminder: Reminder){
        val reminderDb: ReminderDB? = ReminderDB.getInstance(context!!)
        Thread(Runnable {
            reminderDb?.reminderDao()?.deleteReminder(reminder)
            ReminderReceiver.removeRegisteredAlarm(context!!, reminder)
            //delete audio file
            try {
                val file = File(reminder.audio)
                if(file.exists()) file.delete()
            }catch (e: java.lang.Exception){e.printStackTrace()}
            view?.findNavController()?.popBackStack()
        }).start()
    }

    //////// player work /////////

    private fun startAudio(uri: String){
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(uri)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun stopAudio(){
        try {
            mediaPlayer?.stop()
            //mediaPlayer?.release()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun initMediaPlayer(){
        mediaPlayer = MediaPlayerTon.getInstance()
        mediaPlayer?.isLooping = false
        mediaPlayer?.setOnPreparedListener {}
        mediaPlayer?.setOnCompletionListener {
            ivPlay.setImageResource(R.drawable.ic_play)
        }
    }
}

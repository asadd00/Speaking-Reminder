package com.primesol.speakingreminder.android.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.utils.MediaPlayerTon
import kotlinx.android.synthetic.main.fragment_add_reminder_details.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddReminderDetailsFragment : Fragment() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var dateFormat: SimpleDateFormat
    private var outputFilePath: String? = null
    private var pickedHour: String? = null
    private var pickedMinute: String? = null
    private var pickedDay: String? = null
    private var pickedMonth: String? = null
    private var pickedYear: String? = null
    private var reminderDb: ReminderDB? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_reminder_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        initMediaPlayer()
        dateFormat = SimpleDateFormat(getString(R.string.db_date_format))
        reminderDb = ReminderDB.getInstance(context!!)

        if(arguments != null){
            outputFilePath = arguments?.getString("audio")
            tvAudio.text = outputFilePath!!.substring(outputFilePath!!.lastIndexOf('/')+1)

            tvSave.setOnClickListener {
                if(pickedDay == null || pickedMonth == null || pickedYear == null
                    || pickedHour == null || pickedMinute == null || outputFilePath == null){
                    showEmptyFieldsDialog()
                    return@setOnClickListener
                }
                saveReminder()
            }
            tvDate.setOnClickListener {
                showDatePickerDialog()
            }
            tvTime.setOnClickListener {
                showTimePickerDialog()
            }
            ivPlay.setOnClickListener {
                if (mediaPlayer!!.isPlaying) {
                    stopAudio()
                    ivPlay.setImageResource(R.drawable.ic_play)
                }
                else{
                    startAudio(outputFilePath!!)
                    ivPlay.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

    private fun saveReminder(){
        Log.d(TAG, "filePath: $outputFilePath")
        Log.d(TAG, "dateTime: $pickedYear-$pickedMonth-$pickedDay -- $pickedHour:$pickedMinute")
        val reminder = Reminder()
        reminder.title = if(TextUtils.isEmpty(etTitle.text.toString())) getString(R.string.untitled)
            else etTitle.text.toString()
        reminder.audio = outputFilePath!!
        reminder.createdAt = dateFormat.format(Date())
        reminder.status = Reminder.Status.STATUS_ACTIVE.name
        reminder.dateTime = String.format(getString(R.string.db_date_format1), pickedYear, pickedMonth, pickedDay, pickedHour, pickedMinute)

        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, (pickedMonth?.toInt()!!)-1)
        cal.set(Calendar.YEAR, (pickedYear?.toInt()!!))
        cal.set(Calendar.DAY_OF_MONTH, (pickedDay?.toInt()!!))
        cal.set(Calendar.HOUR_OF_DAY, (pickedHour?.toInt()!!))
        cal.set(Calendar.MINUTE, (pickedMinute?.toInt()!!))
        cal.set(Calendar.SECOND, 0)

        Thread(Runnable {
            val alreadyReminder = reminderDb?.reminderDao()?.getReminderWithDateTime(reminder.dateTime)
            if(alreadyReminder != null){
                activity?.runOnUiThread { showDuplicateReminderErrorDialog() }
                return@Runnable
            }

            val id: Long? = reminderDb?.reminderDao()?.insertReminder(reminder)
            ReminderReceiver.setAlarm(context!!, cal, id?.toInt()!!)
            activity?.runOnUiThread { showReminderSavedDialog() }
        }).start()
    }

    private fun removeAudio(reminder: Reminder){
        try {
            val file = File(reminder.audio)
            if(file.exists()) file.delete()
        }catch (e: java.lang.Exception){e.printStackTrace()}
    }

    private fun showTimePickerDialog(){
        val cal = Calendar.getInstance()
        val dialog = TimePickerDialog(activity!!,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                pickedHour = hourOfDay.toString(); pickedMinute = minute.toString()
                if(pickedHour?.length == 1) pickedHour = "0${pickedHour}"
                if(pickedMinute?.length == 1) pickedMinute = "0${pickedMinute}"
                tvTime.text = String.format("%s:%s", pickedHour, pickedMinute)
            },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }

    private fun showDatePickerDialog(){
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(activity!!,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                pickedYear = year.toString()
                pickedMonth = (month+1).toString()
                pickedDay = dayOfMonth.toString()

                if(pickedMonth?.length == 1) pickedMonth = "0${pickedMonth}"
                if(pickedDay?.length == 1) pickedDay = "0${pickedDay}"
                tvDate.text = String.format("%s-%s-%s", pickedYear, pickedMonth, pickedDay)
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.show()
    }

    private fun showDuplicateReminderErrorDialog(){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.duplicate_reminder)
        builder.setMessage(R.string.m_duplicate_reminder)
        builder.setCancelable(false)
        builder.setNegativeButton(R.string.dismiss){
                dialog,_ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showEmptyFieldsDialog(){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.error)
        builder.setMessage(R.string.m_fields_error)
        builder.setCancelable(false)
        builder.setNegativeButton(R.string.dismiss){
                dialog,_ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showReminderSavedDialog(){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.success)
        builder.setMessage(R.string.m_reminder_saved)
        builder.setCancelable(false)
        builder.setNegativeButton(R.string.dismiss){
                dialog,_ ->
            findNavController().popBackStack()
        }
        builder.create().show()
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

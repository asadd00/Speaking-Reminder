package com.primesol.speakingreminder.android.ui.activity

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    /////// recorder config start ///////
    private val sampleRates = intArrayOf(44100, 22050, 11025, 8000)
    private var mediaRecorder: MediaRecorder? = null
    /////// recorder config end ///////

    private var isRecording = false
    private val RC_PERMISSIONS = 1001
    private lateinit var dateFormat: SimpleDateFormat
    private var outputFilePath: String? = null
    private var pickedHour: String? = null
    private var pickedMinute: String? = null
    private var pickedDay: String? = null
    private var pickedMonth: String? = null
    private var pickedYear: String? = null
    private var reminderDb: ReminderDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        dateFormat = SimpleDateFormat("ddMMyy-hhmm")
        reminderDb = ReminderDB.getInstance(baseContext)
        bStart.setOnClickListener {
            if (!hasPermissions()) {return@setOnClickListener}

            if(isRecording){
                stopRecording()
                bStart.text = "Start"
            }
            else{
                startRecording()
                bStart.text = "Stop"
            }

            isRecording = !isRecording
        }

        Thread(Runnable {
            val list = reminderDb?.reminderDao()?.getReminderList() as List<Reminder>
            for(rem in list){
                Log.d(TAG, "Rem: ${rem.id} | ${rem.title} | ${rem.audio} | ${rem.createdAt} | ${rem.date} | ${rem.time}")
            }
        }).start()
    }

    private fun startRecording(){
        Log.d(TAG, "startRecording")
        try {
            initRecorder()
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
        Log.d(TAG, "stopRecording")
        mediaRecorder?.stop()
        mediaRecorder?.release()
        showTimePickerDialog()
    }

    private fun initRecorder(){
        val fileParent = getExternalFilesDir(null)?.absolutePath
        outputFilePath = "$fileParent/sr-${dateFormat.format(Date())}.mp3"

        mediaRecorder= MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder?.setOutputFile(outputFilePath)

    }

    private fun hasPermissions(): Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
           return true
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            getPermissions()

            return false
        }

        return true
    }

    fun getPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            RC_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == RC_PERMISSIONS){
            for(perm in grantResults){
                if(perm != PackageManager.PERMISSION_GRANTED) {
                    getPermissions()
                    return
                }
            }
        }
    }

    private fun showTimePickerDialog(){
        val cal = Calendar.getInstance()
        val dialog = TimePickerDialog(this@MainActivity,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                pickedHour = hourOfDay.toString(); pickedMinute = minute.toString()
                if(pickedHour?.length == 1) pickedHour = "0${pickedHour}"
                if(pickedMinute?.length == 1) pickedMinute = "0${pickedMinute}"

                showDatePickerDialog()
            },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }

    private fun showDatePickerDialog(){
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(this@MainActivity,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                pickedYear = year.toString()
                pickedMonth = (month+1).toString()
                pickedDay = dayOfMonth.toString()

                if(pickedMonth?.length == 1) pickedMonth = "0${pickedMonth}"
                if(pickedDay?.length == 1) pickedDay = "0${pickedDay}"

                saveReminder()
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.show()
    }

    private fun saveReminder(){
        Log.d(TAG, "filePath: $outputFilePath")
        Log.d(TAG, "dateTime: $pickedYear-$pickedMonth-$pickedDay -- $pickedHour:$pickedMinute")
        val reminder = Reminder()
        reminder.title = "Room Test"
        reminder.audio = outputFilePath!!
        reminder.createdAt = dateFormat.format(Date())
        reminder.date = String.format("%s-%s-%s", pickedYear, pickedMonth, pickedDay)
        reminder.time = String.format("%s:%s", pickedHour, pickedMinute)

        Thread(Runnable {
            val id: Long? = reminderDb?.reminderDao()?.insertReminder(reminder)

            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, (pickedMonth?.toInt()!!)-1)
            cal.set(Calendar.YEAR, (pickedYear?.toInt()!!))
            cal.set(Calendar.DAY_OF_MONTH, (pickedDay?.toInt()!!))
            cal.set(Calendar.HOUR_OF_DAY, (pickedHour?.toInt()!!))
            cal.set(Calendar.MINUTE, (pickedMinute?.toInt()!!))
            cal.set(Calendar.SECOND, 0)

            ReminderReceiver.setAlarm(baseContext, cal, id?.toInt()!!)

        }).start()
    }
}

package com.primesol.speakingreminder.android

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        dateFormat = SimpleDateFormat("ddMMyy-hhmm")
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
        Log.d(TAG, "outputFilePath: $outputFilePath")

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
    }
}

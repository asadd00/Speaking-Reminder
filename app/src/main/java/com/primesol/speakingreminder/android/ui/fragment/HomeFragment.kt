package com.primesol.speakingreminder.android.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private var mediaRecorder: MediaRecorder? = null

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        dateFormat = SimpleDateFormat(getString(R.string.db_date_format))
        reminderDb = ReminderDB.getInstance(context!!)

        ivReminderList.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.actionToReminderListFragment))

        ivRecord.setOnTouchListener { _, event ->
            if(!hasPermissions()) return@setOnTouchListener true

            if(event.action == MotionEvent.ACTION_DOWN){
                startRecording()
                startRipple()
                bounceInButton(true)
                tvRecord.text = getString(R.string.recording)
            }
            else if(event.action == MotionEvent.ACTION_UP){
                stopRecording()
                stopRipple()
                bounceInButton(false)
                tvRecord.text = getString(R.string.press_hold_to_record)
            }

            isRecording = !isRecording
            return@setOnTouchListener true
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
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            showTimePickerDialog()
        }catch (e: Exception){e.printStackTrace()}
    }

    private fun startRipple(){
        rippleEffect.startRippleAnimation()
    }

    private fun stopRipple(){
        rippleEffect.stopRippleAnimation()
    }

    private fun bounceInButton(isBounceIn: Boolean){
        if(isBounceIn){
            val animation = AnimationUtils.loadAnimation(context, R.anim.bounce_in)
            animation.fillAfter = true
            ivRecord.startAnimation(animation)
        }
        else{
            val animation = AnimationUtils.loadAnimation(context, R.anim.bounce_out)
            animation.fillAfter = true
            ivRecord.startAnimation(animation)
        }
    }


    private fun initRecorder(){
        val fileParent = activity?.getExternalFilesDir(null)?.absolutePath
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
        else if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            getPermissions()

            return false
        }

        return true
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                RC_PERMISSIONS
            )
        }
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
        val dialog = TimePickerDialog(activity!!,
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
        val dialog = DatePickerDialog(activity!!,
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
        reminder.title = getString(R.string.untitled)
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

        }).start()
    }

    private fun showDuplicateReminderErrorDialog(){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.duplicate_reminder)
        builder.setMessage(R.string.m_duplicate_reminder)
        builder.setCancelable(false)
        builder.setNegativeButton(R.string.dismiss){
            dialog,_ ->
            showTimePickerDialog()
            dialog.dismiss()
        }
        builder.create().show()
    }

}

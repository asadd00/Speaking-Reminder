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
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.databinding.FragmentAddReminderDetailsBinding
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.ui.activity.MainActivity
import com.primesol.speakingreminder.android.utils.MediaPlayerTon
import java.io.File
import java.text.SimpleDateFormat
import java.util.*




class AddReminderDetailsFragment : Fragment() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private lateinit var mBinding: FragmentAddReminderDetailsBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var dateFormat: SimpleDateFormat
    private var outputFilePath: String? = null
    private var pickedHour: String? = null
    private var pickedMinute: String? = null
    private var pickedDay: String? = null
    private var pickedMonth: String? = null
    private var pickedYear: String? = null
    private var reminderDb: ReminderDB? = null
    private var isRepeating = false

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
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_reminder_details, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        initMediaPlayer()
        dateFormat = SimpleDateFormat(getString(R.string.db_date_format))
        reminderDb = ReminderDB.getInstance(requireContext())

        if(arguments != null) {
            mBinding.apply {

                outputFilePath = arguments?.getString("audio")
                tvAudio.text = outputFilePath!!.substring(outputFilePath!!.lastIndexOf('/') + 1)

                tvSave.setOnClickListener {
                    if (pickedDay == null || pickedMonth == null || pickedYear == null
                        || pickedHour == null || pickedMinute == null || outputFilePath == null
                    ) {
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
                    if (mediaPlayer?.isPlaying == true) {
                        stopAudio()
                        ivPlay.setImageResource(R.drawable.ic_play)
                    } else {
                        startAudio(outputFilePath ?: "")
                        ivPlay.setImageResource(R.drawable.ic_pause)
                    }
                }

                cbRepeat.setOnCheckedChangeListener { _, isChecked ->
                    isRepeating = isChecked
                    if (isChecked) {
                        tvDate.visibility = View.GONE
                        textView3.visibility = View.GONE
                        imageView3.visibility = View.GONE

                        radioGroup.visibility = View.VISIBLE
                    } else {
                        tvDate.visibility = View.VISIBLE
                        textView3.visibility = View.VISIBLE
                        imageView3.visibility = View.VISIBLE

                        radioGroup.visibility = View.GONE
                    }
                }

                setDataInViews()
            }
        }
    }

    private fun setDataInViews(){
        val cal = Calendar.getInstance()
        pickedDay = String.format("%02d",cal.get(Calendar.DAY_OF_MONTH))
        pickedMonth = String.format("%02d",cal.get(Calendar.MONTH)+1)
        pickedYear = String.format("%s",cal.get(Calendar.YEAR))
        pickedHour = String.format("%02d",cal.get(Calendar.HOUR_OF_DAY))
        pickedMinute = String.format("%02d",cal.get(Calendar.MINUTE))

        mBinding.tvDate.text = String.format("%s-%s-%s", pickedYear, pickedMonth, pickedDay)
        mBinding.tvTime.text = String.format("%s:%s", pickedHour, pickedMinute)
    }

    private fun saveReminder(){
        //Log.d(TAG, "filePath: $outputFilePath")
        //Log.d(TAG, "dateTime: $pickedYear-$pickedMonth-$pickedDay -- $pickedHour:$pickedMinute")
        val reminder = Reminder()
        reminder.title = if(TextUtils.isEmpty(mBinding.etTitle.text.toString())) getString(R.string.untitled)
            else mBinding.etTitle.text.toString()
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
            if(isRepeating)
                ReminderReceiver.setAlarm(requireContext(), cal, id?.toInt()!!, true, getInterval())
            else
                ReminderReceiver.setAlarm(requireContext(), cal, id?.toInt()!!)
            activity?.runOnUiThread { showReminderSavedDialog() }
        }).start()
    }

    private fun getInterval(): Int{
        return when(mBinding.radioGroup.checkedRadioButtonId){
            R.id.rbDaily -> Reminder.INTERVAL_DAILY
            R.id.rbWeekly -> Reminder.INTERVAL_WEEKLY
            R.id.rbMonthly -> Reminder.INTERVAL_MONTHLY
            R.id.rbYearly -> Reminder.INTERVAL_YEARLY
            else -> Reminder.INTERVAL_DAILY
        }
    }

    private fun removeAudio(reminder: Reminder){
        try {
            val file = File(reminder.audio)
            if(file.exists()) file.delete()
        }catch (e: java.lang.Exception){e.printStackTrace()}
    }

    private fun showTimePickerDialog(){
        val cal = Calendar.getInstance()
        val dialog = TimePickerDialog(requireActivity(),
            { _, hourOfDay, minute ->
                pickedHour = hourOfDay.toString(); pickedMinute = minute.toString()
                if(pickedHour?.length == 1) pickedHour = "0${pickedHour}"
                if(pickedMinute?.length == 1) pickedMinute = "0${pickedMinute}"
                mBinding.tvTime.text = String.format("%s:%s", pickedHour, pickedMinute)
            },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }

    private fun showDatePickerDialog(){
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(requireActivity(),
            { _, year, month, dayOfMonth ->
                pickedYear = year.toString()
                pickedMonth = (month+1).toString()
                pickedDay = dayOfMonth.toString()

                if(pickedMonth?.length == 1) pickedMonth = "0${pickedMonth}"
                if(pickedDay?.length == 1) pickedDay = "0${pickedDay}"
                mBinding.tvDate.text = String.format("%s-%s-%s", pickedYear, pickedMonth, pickedDay)
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = cal.timeInMillis
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
        (requireActivity() as MainActivity).isFromSuccessPage = true
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
        try {
            mediaPlayer = MediaPlayerTon.getInstance()
            mediaPlayer?.isLooping = false
            mediaPlayer?.setOnPreparedListener {}
            mediaPlayer?.setOnCompletionListener {
                mBinding.ivPlay.setImageResource(R.drawable.ic_play)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }
}

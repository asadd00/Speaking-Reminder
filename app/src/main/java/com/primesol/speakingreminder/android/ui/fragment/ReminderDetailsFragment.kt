package com.primesol.speakingreminder.android.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import kotlinx.android.synthetic.main.fragment_reminder_details.*

class ReminderDetailsFragment : Fragment() {

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
        if(arguments != null){
            val reminder = arguments?.getSerializable(Reminder.REMINDER) as Reminder
            tvTitle.text = reminder.title
            tvTime.text = reminder.dateTime
            tvStatus.text = "Not implemented"

            bDelete.setOnClickListener {
                val reminderDb: ReminderDB? = ReminderDB.getInstance(context!!)
                Thread(Runnable {
                    reminderDb?.reminderDao()?.deleteReminder(reminder)
                    ReminderReceiver.deleteAlarm(context!!, reminder)
                    view?.findNavController()?.popBackStack()
                }).start()
            }
        }
    }

}

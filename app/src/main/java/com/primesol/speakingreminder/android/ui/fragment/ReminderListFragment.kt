package com.primesol.speakingreminder.android.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.ui.adapter.ReminderAdpater
import kotlinx.android.synthetic.main.fragment_reminder_list.*

class ReminderListFragment : Fragment(), ReminderAdpater.OnListItemClickListener {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private val adapter = ReminderAdpater()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.onListItemClickListener = this
        rvList.adapter = adapter
        fetchDataAndSetList()
    }

    private fun fetchDataAndSetList(){
        val reminderDb: ReminderDB? = ReminderDB.getInstance(context!!)
        Thread(Runnable {
            val list = reminderDb?.reminderDao()?.getReminderList() as ArrayList<Reminder>
//            for(rem in list){
//                Log.d(TAG, "Rem: ${rem.id} | ${rem.title} | ${rem.audio} | ${rem.createdAt} | ${rem.date} | ${rem.time}")
//            }
            activity?.runOnUiThread{ adapter.setData(list) }
        }).start()
    }

    override fun onListItemClick(reminder: Reminder, position: Int) {
        view?.findNavController()?.navigate(R.id.actionToReminderDetailsFragment, bundleOf(Pair(Reminder.REMINDER, reminder)))
    }
}

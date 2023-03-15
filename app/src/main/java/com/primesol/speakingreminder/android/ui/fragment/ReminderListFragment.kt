package com.primesol.speakingreminder.android.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest

import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.databinding.FragmentReminderListBinding
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.ui.adapter.ReminderAdpater
import com.primesol.speakingreminder.android.utils.MediaPlayerTon
import java.io.File

class ReminderListFragment : Fragment(), ReminderAdpater.OnListItemClickListener {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private lateinit var mBinding: FragmentReminderListBinding
    private var adapter: ReminderAdpater? = null

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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reminder_list, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adRequest: AdRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)

        adapter = ReminderAdpater(requireContext())
        adapter?.onListItemClickListener = this
        mBinding.rvList.adapter = adapter
        fetchDataAndSetList()
    }

    private fun fetchDataAndSetList(){
        val reminderDb: ReminderDB? = ReminderDB.getInstance(requireContext())
        Thread(Runnable {
            val list = reminderDb?.reminderDao()?.getReminderList() as ArrayList<Reminder>
//            for(rem in list){
//                Log.d(TAG, "Rem: ${rem.id} | ${rem.title} | ${rem.audio} | ${rem.createdAt} | ${rem.date} | ${rem.time}")
//            }
            activity?.runOnUiThread{
                mBinding.llNoItems.visibility = if(list.size == 0) View.VISIBLE else View.GONE
                adapter?.setData(list)
            }
        }).start()
    }

    override fun onListItemClick(reminder: Reminder, position: Int) {
        view?.findNavController()?.navigate(R.id.actionToReminderDetailsFragment, bundleOf(Pair(Reminder.REMINDER, reminder)))
    }

    override fun onListItemDelete(reminder: Reminder, position: Int) {
        showDeleteConfirmationDialog(reminder, position)
    }

    private fun showDeleteConfirmationDialog(reminder: Reminder, position: Int){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.confirmation)
        builder.setMessage(R.string.m_delete_confirm)
        builder.setCancelable(false)
        builder.setNegativeButton(android.R.string.no){ dialog,_ -> dialog.dismiss() }
        builder.setPositiveButton(android.R.string.yes){ _,_ -> deleteReminder(reminder, position) }
        builder.create().show()
    }

    private fun deleteReminder(reminder: Reminder, position: Int){
        val rem = adapter?.list?.get(position) as Reminder
        val reminderDb: ReminderDB = ReminderDB.getInstance(requireContext())
        Thread(Runnable {
            reminderDb?.reminderDao()?.deleteReminder(rem)
            ReminderReceiver.removeRegisteredAlarm(requireContext(), rem)
            //delete audio file
            try {
                val file = File(reminder.audio)
                if(file.exists()) file.delete()
            }catch (e: java.lang.Exception){e.printStackTrace()}
        }).start()
        adapter?.list?.removeAt(position)
        adapter?.notifyDataSetChanged()
        mBinding.llNoItems.visibility = if(adapter?.list?.size == 0) View.VISIBLE else View.GONE
    }
}

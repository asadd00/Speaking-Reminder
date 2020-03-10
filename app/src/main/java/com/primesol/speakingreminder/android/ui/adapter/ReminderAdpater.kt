package com.primesol.speakingreminder.android.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import com.primesol.speakingreminder.android.receiver.ReminderReceiver
import com.primesol.speakingreminder.android.repository.ReminderDB

class ReminderAdpater(val context: Context): RecyclerView.Adapter<ReminderAdpater.ViewHolder>() {
    private var list = ArrayList<Reminder>()
    var onListItemClickListener: OnListItemClickListener? = null

    fun setData(list: ArrayList<Reminder>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = list[position]
        holder.tvTitle.text = reminder.title
        holder.tvDate.text = reminder.dateTime.substring(0, reminder.dateTime.indexOf('_'))
        holder.tvTime.text = (reminder.dateTime.substring(reminder.dateTime.indexOf('_')+1)).replace('-', ':')
        when(reminder.status){
            Reminder.Status.STATUS_ACTIVE.name->holder.tvStatus.setBackgroundColor(context.resources.getColor(R.color.green))
            Reminder.Status.STATUS_INACTIVE.name->holder.tvStatus.setBackgroundColor(context.resources.getColor(R.color.red))
            Reminder.Status.STATUS_PASSED.name->holder.tvStatus.setBackgroundColor(context.resources.getColor(R.color.orange_light))
        }
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivPlay: ImageView = view.findViewById(R.id.ivPlay)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)

        init {
            view.setOnClickListener {
                if(onListItemClickListener != null) onListItemClickListener?.onListItemClick(list[layoutPosition], layoutPosition)
            }

            ivPlay.setOnClickListener {

            }

            ivDelete.setOnClickListener {
                val reminder = list[layoutPosition]
                ReminderReceiver.deleteAlarm(context, reminder)
                val reminderDb: ReminderDB? = ReminderDB.getInstance(context)
                Thread(Runnable {
                    reminderDb?.reminderDao()?.deleteReminder(reminder)
                    ReminderReceiver.deleteAlarm(context, reminder)
                }).start()
                list.removeAt(layoutPosition)
                notifyDataSetChanged()
            }
        }
    }

    interface OnListItemClickListener{
        fun onListItemClick(reminder: Reminder, position: Int)
    }
}
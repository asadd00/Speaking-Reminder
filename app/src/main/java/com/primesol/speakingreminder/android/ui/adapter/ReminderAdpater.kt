package com.primesol.speakingreminder.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder

class ReminderAdpater: RecyclerView.Adapter<ReminderAdpater.ViewHolder>() {
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
        holder.tvTime.text = reminder.dateTime
        holder.tvStatus.text = "Not implemented"
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)

        init {
            view.setOnClickListener {
                if(onListItemClickListener != null) onListItemClickListener?.onListItemClick(list[layoutPosition], layoutPosition)
            }
        }
    }

    interface OnListItemClickListener{
        fun onListItemClick(reminder: Reminder, position: Int)
    }
}
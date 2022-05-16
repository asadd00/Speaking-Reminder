package com.primesol.speakingreminder.android.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.databinding.ActivityPopupReminderBinding
import com.primesol.speakingreminder.android.model.Reminder


class PopupReminderActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityPopupReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_popup_reminder)
        init()
    }

    private fun init(){
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if(intent != null && intent.hasExtra(Reminder.REMINDER)){
            val reminder: Reminder = intent.getSerializableExtra(Reminder.REMINDER) as Reminder
            mBinding.tvTitle.text = reminder.title
        }

        mBinding.bDismiss.setOnClickListener {
            finish()
        }
    }
}

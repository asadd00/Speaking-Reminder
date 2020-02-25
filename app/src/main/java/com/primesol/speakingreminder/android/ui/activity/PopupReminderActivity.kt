package com.primesol.speakingreminder.android.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.model.Reminder
import kotlinx.android.synthetic.main.activity_popup_reminder.*


class PopupReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_reminder)
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
            tvTitle.text = reminder.title
        }

        bDismiss.setOnClickListener {
            finish()
        }
    }
}

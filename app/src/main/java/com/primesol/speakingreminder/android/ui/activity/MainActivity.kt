package com.primesol.speakingreminder.android.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.primesol.speakingreminder.android.R


class MainActivity : AppCompatActivity() {
    private val TAG = "ttt ${this::class.java.simpleName}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

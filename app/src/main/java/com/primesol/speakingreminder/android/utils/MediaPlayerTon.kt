package com.primesol.speakingreminder.android.utils

import android.media.MediaPlayer

object MediaPlayerTon: MediaPlayer() {
    private var INSTANCE: MediaPlayer? = null
    fun getInstance(): MediaPlayer?{
        if(INSTANCE == null) INSTANCE = MediaPlayer()
        return INSTANCE
    }
}
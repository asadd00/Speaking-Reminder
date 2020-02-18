package com.primesol.speakingreminder.android

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    /////// recorder config start ///////
    private val sampleRates = intArrayOf(44100, 22050, 11025, 8000)
    private var mediaRecorder: MediaRecorder? = null
    /////// recorder config end ///////

    private var isRecording = false
    private val RC_PERMISSIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        bStart.setOnClickListener {
            if (!hasPermissions()) {return@setOnClickListener}

            if(isRecording){
                stopRecording()
                bStart.text = "Start"
            }
            else{
                startRecording()
                bStart.text = "Stop"
            }

            isRecording = !isRecording
        }
    }

    private fun startRecording(){
        Log.d(TAG, "startRecording")
        try {
            initRecorder()
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
        Log.d(TAG, "stopRecording")
        mediaRecorder?.stop()
        mediaRecorder?.release()

    }

    private fun initRecorder(){
        val fileParent = getExternalFilesDir(null)?.absolutePath
        val outputFilePath = "$fileParent/sr.wav"
        Log.d(TAG, "fileParent: $outputFilePath")

        mediaRecorder= MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder?.setOutputFile(outputFilePath)

    }

    private fun hasPermissions(): Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
           return true
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            getPermissions()

            return false
        }

        return true
    }

    fun getPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            RC_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == RC_PERMISSIONS){
            for(perm in grantResults){
                if(perm != PackageManager.PERMISSION_GRANTED) {
                    getPermissions()
                    return
                }
            }
        }
    }
}

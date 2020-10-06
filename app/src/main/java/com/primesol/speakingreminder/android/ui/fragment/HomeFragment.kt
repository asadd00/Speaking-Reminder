package com.primesol.speakingreminder.android.ui.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.content.CursorLoader
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.primesol.speakingreminder.android.R
import com.primesol.speakingreminder.android.repository.ReminderDB
import com.primesol.speakingreminder.android.utils.Methods
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private val TAG = "ttt ${this::class.java.simpleName}"
    private var mediaRecorder: MediaRecorder? = null

    private var isRecording = false
    private val RC_PERMISSIONS = 1001
    private val RC_GET_FILE = 1002
    private lateinit var dateFormat: SimpleDateFormat
    private var outputFilePath: String? = null
    private var reminderDb: ReminderDB? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        dateFormat = SimpleDateFormat(getString(R.string.db_date_format))
        reminderDb = ReminderDB.getInstance(context!!)

        ivReminderList.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.actionToReminderListFragment))

        ivRecord.setOnTouchListener { _, event ->
            if(!hasPermissions()) return@setOnTouchListener true

            if(event.action == MotionEvent.ACTION_DOWN){
                startRecording()
                startRipple()
                bounceInButton(true)
                tvRecord.text = getString(R.string.recording)
            }
            else if(event.action == MotionEvent.ACTION_UP){
                stopRecording()
                stopRipple()
                bounceInButton(false)
                tvRecord.text = getString(R.string.press_hold_to_record)
            }

            isRecording = !isRecording
            return@setOnTouchListener true
        }

        ivSelectFile.setOnClickListener {
            if(!hasPermissions()) return@setOnClickListener
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, RC_GET_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK && requestCode == RC_GET_FILE){
            try {
                val uri = data?.data

                findNavController().navigate(R.id.actionToAddReminderDetailsFragment, bundleOf(
                    Pair("audio", Methods.getGeneralFilePath(uri!!, context!!))
                ))
            }
            catch (e:Exception){
                e.printStackTrace()
            }
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
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            findNavController().navigate(R.id.actionToAddReminderDetailsFragment, bundleOf(
                Pair("audio", outputFilePath)
            ))
        }catch (e: Exception){e.printStackTrace()}
    }

    private fun startRipple(){
        rippleEffect.startRippleAnimation()
    }

    private fun stopRipple(){
        rippleEffect.stopRippleAnimation()
    }

    private fun bounceInButton(isBounceIn: Boolean){
        if(isBounceIn){
            val animation = AnimationUtils.loadAnimation(context, R.anim.bounce_in)
            animation.fillAfter = true
            ivRecord.startAnimation(animation)
        }
        else{
            val animation = AnimationUtils.loadAnimation(context, R.anim.bounce_out)
            animation.fillAfter = true
            ivRecord.startAnimation(animation)
        }
    }

    private fun initRecorder(){
        val fileParent = activity?.getExternalFilesDir(null)?.absolutePath
        outputFilePath = "$fileParent/sr-${dateFormat.format(Date())}-${(0..999).random()}.mp3"

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
        else if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            getPermissions()

            return false
        }

        return true
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                RC_PERMISSIONS
            )
        }
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

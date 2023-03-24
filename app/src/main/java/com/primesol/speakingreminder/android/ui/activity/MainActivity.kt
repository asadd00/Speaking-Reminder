package com.primesol.speakingreminder.android.ui.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.github.javiersantos.appupdater.AppUpdater
import com.google.android.gms.ads.MobileAds
import com.primesol.speakingreminder.android.R


class MainActivity : AppCompatActivity() {
    private val TAG = "ttt ${this::class.java.simpleName}"

    var isFromSuccessPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestNotificationPermission()
        checkForUpdates()
        initAdMob()
    }

    private fun checkForUpdates(){
        val appUpdater = AppUpdater(this)
        appUpdater.start()

    }

    private fun initAdMob(){
        MobileAds.initialize(
            this
        ) {

        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        //do nothing for now
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S || "S" == Build.VERSION.CODENAME) {
            // Android 12 or Android 12 Beta
            if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {

            }
        } else {
            when {
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
                else -> {
                    if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                }
            }
        }
    }
}

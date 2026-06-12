package com.shirazi.duaa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.shirazi.duaa.data.Settings
import com.shirazi.duaa.ui.DuaaApp

class MainActivity : ComponentActivity() {

    private val notifyPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = Settings(this)
        setContent {
            DuaaApp(settings, onRequestNotifyPermission = { requestNotifyPermission() })
        }
    }

    private fun requestNotifyPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

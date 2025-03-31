package com.maverick.clientapp

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.receivers.DeviceAdminReceiver

class BlockScreenActivity : Activity() {

    private lateinit var lockIcon: ImageView
    private lateinit var textMessage: TextView
    private lateinit var btnAceptar: Button
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_screen)

        lockIcon = findViewById(R.id.lockIcon)
        textMessage = findViewById(R.id.textBlockMessage)
        btnAceptar = findViewById(R.id.btnAceptar)
        layout = findViewById(R.id.layoutBlock)

        val anim = AnimationUtils.loadAnimation(this, R.anim.heartbeat)
        lockIcon.startAnimation(anim)

        val modoActivo = intent.getStringExtra("modo") == "activo"

        if (modoActivo) {
            lockIcon.setImageResource(R.drawable.ic_unlock)
            textMessage.text = "Este dispositivo está DESBLOQUEADO.\nPuedes usarlo con normalidad."
            btnAceptar.visibility = View.VISIBLE
            btnAceptar.setOnClickListener { finishAffinity() }

            configurarInterfaz()
            return
        }

        if (!isTaskRoot) finishAffinity()

        configurarModoKiosko()
        configurarInterfaz()
        escucharEstadoEnFirestore()
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .get()
            .addOnSuccessListener { snapshot ->
                val estado = snapshot.documents.firstOrNull()?.getString("estado") ?: "activo"
                if (estado == "activo") desbloquearVisualmente()
            }
    }

    private fun configurarModoKiosko() {
        val policyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)

        if (policyManager.isDeviceOwnerApp(packageName)) {
            policyManager.setLockTaskPackages(adminComponent, arrayOf(packageName))
            startLockTask()
        }
    }

    private fun configurarInterfaz() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
    }

    private fun escucharEstadoEnFirestore() {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) return@addSnapshotListener

                val estado = snapshot.documents[0].getString("estado") ?: "activo"
                if (estado == "activo") {
                    desbloquearVisualmente()
                }
            }
    }

    private fun desbloquearVisualmente() {
        try {
            stopLockTask()
        } catch (_: Exception) {}

        lockIcon.setImageResource(R.drawable.ic_unlock)
        textMessage.text = "Este dispositivo está DESBLOQUEADO.\nPuedes usarlo normalmente."
        btnAceptar.visibility = View.VISIBLE
        btnAceptar.setOnClickListener { finishAffinity() }

        val unlockAnim = AnimationUtils.loadAnimation(this, R.anim.unlock_spin_fade)
        lockIcon.startAnimation(unlockAnim)

        val vibrator = getSystemService(VIBRATOR_SERVICE) as? android.os.Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(200)
        }

        val mediaPlayer = MediaPlayer.create(this, R.raw.unlock_sound)
        mediaPlayer.start()
    }

    override fun onBackPressed() {}
    override fun onUserLeaveHint() {}
}

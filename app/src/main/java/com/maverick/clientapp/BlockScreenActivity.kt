package com.maverick.clientapp

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import com.google.firebase.firestore.FirebaseFirestore

class BlockScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot) {
            finishAffinity() // evita que haya otras actividades debajo
        }

        val mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val mComponentName = ComponentName(this, com.maverick.clientapp.receivers.DeviceAdminReceiver::class.java)

        if (mDevicePolicyManager.isDeviceOwnerApp(packageName)) {
            val packages = arrayOf(packageName)
            mDevicePolicyManager.setLockTaskPackages(mComponentName, packages)
            startLockTask() // Solo ahora sí lo activamos
        }


        // Establece el layout
        setContentView(R.layout.activity_block_screen)

        // Pantalla completa (sin barras ni navegación)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Bloqueo permanente (desactiva Back button)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)
        if (deviceId != null) {
            escucharDesbloqueo(deviceId)
        }

    }
    private fun escucharDesbloqueo(deviceId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null && !snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    val estado = document.getString("estado") ?: "activo"

                    if (estado == "activo") {
                        stopLockTask()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }

                }
            }
    }

    override fun onBackPressed() {
        // No hacer nada
    }

    override fun onUserLeaveHint() {
        // Impide salir al home
    }

}

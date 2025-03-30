package com.maverick.clientapp

/**
 * BlockScreenActivity
 *
 * Esta actividad se muestra cuando el dispositivo ha sido marcado como "bloqueado" en Firestore.
 * Su propósito es restringir completamente el uso del dispositivo, evitando el acceso al sistema,
 * botones, barra de navegación, y cualquier intento de evasión.
 *
 * Características clave:
 * - Se ejecuta en pantalla completa, ocultando barra de estado y navegación.
 * - Inicia el modo kiosko (`startLockTask`) si la app está registrada como Device Owner.
 * - Escucha cambios en Firestore y desbloquea el dispositivo automáticamente si el estado cambia a "activo".
 * - Impide navegación hacia atrás y acceso al sistema (Home, Notificaciones, etc.).
 */


import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.WindowCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.receivers.DeviceAdminReceiver

class BlockScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ PRIMERO se carga el layout
        setContentView(R.layout.activity_block_screen)

        // ✅ Ahora sí puedes acceder a las vistas
        val lockIcon = findViewById<ImageView>(R.id.lockIcon)
        val anim = AnimationUtils.loadAnimation(this, R.anim.heartbeat)
        lockIcon.startAnimation(anim)

        if (!isTaskRoot) {
            finishAffinity()
        }

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
                if (estado == "activo") {
                    desbloquearDispositivo()
                }
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

        // Pantalla completa sin barras
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Ocultar UI del sistema
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
                    desbloquearDispositivo()
                }
            }
    }

    private fun desbloquearDispositivo() {
        try {
            stopLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 🔁 Esperamos brevemente antes de salir, para asegurarnos que el modo kiosko se detuvo bien
        window.decorView.postDelayed({
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0) // Sin animación
        }, 1000) // 1 segundo de espera
    }


    override fun onBackPressed() {
        // Desactiva botón de retroceso
    }

    override fun onUserLeaveHint() {
        // Evita ir al Home
    }
}

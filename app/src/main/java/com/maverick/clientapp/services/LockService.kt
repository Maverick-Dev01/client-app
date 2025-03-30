package com.maverick.clientapp.services

/**
 * LockService
 *
 * Servicio en primer plano (Foreground Service) encargado de verificar el estado del dispositivo
 * en la base de datos de Firestore. Si el estado es "bloqueado", lanza la pantalla de bloqueo
 * (BlockScreenActivity) automáticamente. Está diseñado para ejecutarse al iniciar el dispositivo
 * mediante el BootReceiver.
 *
 * También crea un canal de notificación para cumplir con los requisitos del sistema en Android 8+.
 */

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.BlockScreenActivity
import com.maverick.clientapp.R

class LockService : Service() {

    companion object {
        private const val CHANNEL_ID = "lock_channel_id"
        private const val NOTIFICATION_ID = 1
        private const val PREFS_NAME = "config"
        private const val DEVICE_ID_KEY = "deviceId"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        showForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val deviceId = prefs.getString(DEVICE_ID_KEY, null)

        if (!deviceId.isNullOrEmpty()) {
            val db = FirebaseFirestore.getInstance()

            db.collection("dispositivos")
                .whereEqualTo("imei", deviceId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("❌ Error en escucha Firestore: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val estado = snapshot.documents[0].getString("estado") ?: "activo"
                        println("📡 Estado detectado en escucha: $estado")

                        if (estado == "bloqueado") {
                            println("🔒 Dispositivo bloqueado. Lanzando pantalla de bloqueo.")
                            Handler(mainLooper).post {
                                val lockIntent = Intent(this, BlockScreenActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                startActivity(lockIntent)
                            }
                        }
                    }
                }
        } else {
            println("⚠️ No se encontró deviceId en LockService")
            stopSelf()
        }

        return START_STICKY // Se reinicia automáticamente si el sistema lo mata
    }




    override fun onBind(intent: Intent?): IBinder? = null

    private fun showForegroundNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Verificando estado del dispositivo...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal de bloqueo de dispositivo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}

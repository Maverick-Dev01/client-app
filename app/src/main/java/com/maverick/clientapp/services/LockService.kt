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
                .get()
                .addOnSuccessListener { result ->
                    val document = result.documents.firstOrNull()
                    val estado = document?.getString("estado") ?: "activo"

                    if (estado == "bloqueado") {
                        val lockIntent = Intent(this, BlockScreenActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(lockIntent)
                    }
                    stopSelf()
                }
                .addOnFailureListener {
                    stopSelf()
                }
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
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

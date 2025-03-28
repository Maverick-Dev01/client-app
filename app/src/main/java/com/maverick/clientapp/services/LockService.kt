package com.maverick.clientapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.BlockScreenActivity
import com.maverick.clientapp.R

class LockService : Service() {

    companion object {
        const val CHANNEL_ID = "lock_channel_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Verificando estado del dispositivo...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa un ícono visible
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)

        if (!deviceId.isNullOrEmpty()) {
            val db = FirebaseFirestore.getInstance()

            db.collection("dispositivos")
                .whereEqualTo("imei", deviceId)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val estado = result.documents[0].getString("estado") ?: "activo"
                        if (estado == "bloqueado") {
                            val lockIntent = Intent(this, BlockScreenActivity::class.java)
                            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(lockIntent)
                        }
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Canal de bloqueo de dispositivo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}

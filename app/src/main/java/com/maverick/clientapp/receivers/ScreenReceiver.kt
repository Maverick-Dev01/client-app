package com.maverick.clientapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.BlockScreenActivity

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        println("📺 Broadcast recibido: ${intent?.action}")

        val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .get()
            .addOnSuccessListener { result ->
                val estado = result.documents.firstOrNull()?.getString("estado") ?: "activo"
                println("📡 Estado desde Firestore: $estado")

                if (estado == "bloqueado") {
                    println("🔒 Lanzando pantalla de bloqueo desde ScreenReceiver")

                    val lockIntent = Intent(context, BlockScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(lockIntent)
                }
            }
    }

}

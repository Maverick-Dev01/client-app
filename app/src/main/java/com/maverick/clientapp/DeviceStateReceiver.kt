package com.maverick.clientapp

/**
 * DeviceStateReceiver
 *
 * BroadcastReceiver que responde a una acción personalizada (`com.maverick.clientapp.STATE_CHANGED`).
 * Su objetivo es verificar el estado del dispositivo en Firestore (usando el IMEI local)
 * y, si el estado es "bloqueado", ejecutar un bloqueo inmediato con `DevicePolicyManager.lockNow()`.
 *
 * Este componente puede usarse para invocar un chequeo manual o automatizado desde otros puntos
 * del sistema o de la aplicación.
 */


import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.receivers.DeviceAdminReceiver

class DeviceStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .get()
            .addOnSuccessListener { result ->
                val estado = result.documents.firstOrNull()?.getString("estado")
                if (estado == "bloqueado") {
                    bloquearDispositivo(context)
                }
            }
    }

    private fun bloquearDispositivo(context: Context) {
        val policyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)

        if (policyManager.isDeviceOwnerApp(context.packageName)) {
            policyManager.lockNow()
        }
    }
}

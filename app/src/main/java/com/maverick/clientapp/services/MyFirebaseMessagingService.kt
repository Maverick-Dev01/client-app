package com.maverick.clientapp.services

/**
 * MyFirebaseMessagingService
 *
 * Servicio que extiende FirebaseMessagingService para manejar mensajes push (FCM).
 * Recibe mensajes desde Firebase con información del estado del dispositivo ("activo" o "bloqueado")
 * y el IMEI del dispositivo. Si el mensaje corresponde al dispositivo actual y el estado es "bloqueado",
 * se lanza automáticamente la pantalla de bloqueo (`BlockScreenActivity`) y se impide el uso del dispositivo.
 *
 * También implementa `onNewToken` para manejar cambios en el token de FCM.
 */


import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.maverick.clientapp.BlockScreenActivity
import com.maverick.clientapp.receivers.DeviceAdminReceiver

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val estado = remoteMessage.data["estado"]
        val imei = remoteMessage.data["imei"]

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)

        if (imei == deviceId && estado == "bloqueado") {
            mostrarPantallaBloqueo()
        }
    }

    private fun mostrarPantallaBloqueo() {
        val policyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)

        if (policyManager.isDeviceOwnerApp(packageName)) {
            policyManager.setLockTaskPackages(adminComponent, arrayOf(packageName))
        }

        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Nuevo token FCM generado: $token")
        // Aquí puedes guardar el token en Firestore o usarlo para enviar notificaciones desde el AdminApp
    }
}

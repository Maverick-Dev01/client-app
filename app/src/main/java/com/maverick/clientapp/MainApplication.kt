package com.maverick.clientapp

/**
 * MainApplication
 *
 * Clase personalizada que extiende `Application` y se ejecuta una vez cuando la app es iniciada.
 *
 * Funciones principales:
 * - Inicializa Firebase para que esté disponible en toda la aplicación.
 * - Lanza `LockService` automáticamente si hay un IMEI configurado, para revisar si el dispositivo debe bloquearse.
 * - Solicita y guarda el token FCM en Firestore, asociándolo con el dispositivo registrado (por su IMEI).
 *
 * Este enfoque permite mantener la lógica de arranque y sincronización centralizada.
 */


import android.app.Application
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.maverick.clientapp.services.LockService

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        inicializarFirebase()
        lanzarLockServiceSiEsNecesario()
        sincronizarTokenFCM()
    }

    private fun inicializarFirebase() {
        FirebaseApp.initializeApp(this)
    }

    private fun lanzarLockServiceSiEsNecesario() {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)

        if (!deviceId.isNullOrEmpty()) {
            val intent = Intent(this, LockService::class.java)
            startService(intent)
        }
    }

    private fun sincronizarTokenFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            val token = task.result
            val prefs = getSharedPreferences("config", MODE_PRIVATE)
            val deviceId = prefs.getString("deviceId", null)

            if (!deviceId.isNullOrEmpty()) {
                val db = FirebaseFirestore.getInstance()
                db.collection("dispositivos")
                    .whereEqualTo("imei", deviceId)
                    .get()
                    .addOnSuccessListener { result ->
                        val docRef = result.documents.firstOrNull()?.reference
                        docRef?.update("fcmToken", token)
                    }
            }
        }
    }
}

package com.maverick.clientapp

/**
 * MainActivity
 *
 * Pantalla principal de la aplicación ClientApp que se muestra cuando el dispositivo está en estado "activo".
 * Se encarga de:
 * - Consultar si ya existe un ID de dispositivo guardado en SharedPreferences.
 * - Si no existe, redirige al formulario de registro (`RegisterIdActivity`).
 * - Si existe, muestra la pantalla principal con estado del dispositivo.
 * - Escucha en tiempo real el estado del dispositivo en Firestore (activo o bloqueado).
 * - Si el estado cambia a "bloqueado", redirige inmediatamente a la pantalla de bloqueo (`BlockScreenActivity`).
 *
 * Esta actividad también permite al usuario cambiar manualmente el ID registrado si el dispositivo no es reconocido.
 */


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.clientapp.services.LockService

class MainActivity : AppCompatActivity() {

    private lateinit var textStatus: MaterialTextView
    private lateinit var layoutMain: LinearLayout
    private lateinit var btnCambiarId: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)

        if (deviceId.isNullOrEmpty()) {
            redirigirARegistro()
        } else {
            mostrarPantallaPrincipal()
            escucharEstadoDispositivo(deviceId)
        }

        if (!deviceId.isNullOrEmpty()) {
            val serviceIntent = Intent(this, LockService::class.java)
            startService(serviceIntent)
        }
    }

    private fun redirigirARegistro() {
        val intent = Intent(this, RegisterIdActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun mostrarPantallaPrincipal() {
        setContentView(R.layout.activity_main)
        textStatus = findViewById(R.id.textStatus)
        layoutMain = findViewById(R.id.layoutMain)
        btnCambiarId = findViewById(R.id.btnCambiarId)

        btnCambiarId.setOnClickListener {
            borrarIdYVolverARegistro()
        }
    }

    private fun borrarIdYVolverARegistro() {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        prefs.edit().remove("deviceId").apply()

        val intent = Intent(this, RegisterIdActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun escucharEstadoDispositivo(deviceId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    mostrarEstado("Error de conexión", Color.GRAY, true)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val estado = snapshot.documents[0].getString("estado") ?: "activo"

                    if (estado == "bloqueado") {
                        irABloqueo()
                    } else {
                        mostrarEstado("Dispositivo ACTIVO", Color.parseColor("#4CAF50"), false)
                    }
                } else {
                    mostrarEstado("Dispositivo no encontrado", Color.GRAY, true)
                }
            }
    }

    private fun mostrarEstado(mensaje: String, color: Int, mostrarBoton: Boolean) {
        textStatus.text = mensaje
        layoutMain.setBackgroundColor(color)
        btnCambiarId.visibility = if (mostrarBoton) View.VISIBLE else View.GONE
    }

    private fun irABloqueo() {
        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}

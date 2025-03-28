package com.maverick.clientapp

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textview.MaterialTextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
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
            // Si no hay ID guardado, redirige al formulario de registro
            val intent = Intent(this, LockService::class.java)
            startActivity(intent)
            finish()
        } else {
            // Si ya hay ID, continúa con el flujo normal
            setContentView(R.layout.activity_main)

            textStatus = findViewById(R.id.textStatus)
            layoutMain = findViewById(R.id.layoutMain)
            btnCambiarId = findViewById(R.id.btnCambiarId)


            btnCambiarId.setOnClickListener {
                // Borra el ID guardado y vuelve al formulario
                val prefs = getSharedPreferences("config", MODE_PRIVATE)
                prefs.edit().remove("deviceId").apply()

                val intent = Intent(this, RegisterIdActivity::class.java)
                startActivity(intent)
                finish()
            }
            escucharEstadoDispositivo(deviceId)
        }

    }

    private fun escucharEstadoDispositivo(deviceId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    textStatus.text = "Error de conexión"
                    layoutMain.setBackgroundColor(Color.GRAY)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    val estado = document.getString("estado") ?: "activo"

                    if (estado == "bloqueado") {
                        // 🔴 Ir a pantalla de bloqueo
                        val intent = Intent(this, BlockScreenActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        // 🟢 Mostrar que está activo
                        textStatus.text = "Dispositivo ACTIVO"
                        layoutMain.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                        btnCambiarId.visibility = View.GONE
                    }
                } else {
                    textStatus.text = "Dispositivo no encontrado"
                    layoutMain.setBackgroundColor(Color.GRAY)
                    btnCambiarId.visibility = View.VISIBLE
                }
            }
    }


}

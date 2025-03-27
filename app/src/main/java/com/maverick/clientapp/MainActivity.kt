package com.maverick.clientapp

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textview.MaterialTextView
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings


class MainActivity : AppCompatActivity() {

    private lateinit var textStatus: MaterialTextView
    private lateinit var layoutMain: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textStatus = findViewById(R.id.textStatus)
        layoutMain = findViewById(R.id.layoutMain)

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        escucharEstadoDispositivo()
    }

    private fun escucharEstadoDispositivo() {
        val db = FirebaseFirestore.getInstance()

        val sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        val deviceId = sharedPreferences.getString("deviceId", null)

        if (deviceId == null) {
            textStatus.text = "ID no registrado"
            layoutMain.setBackgroundColor(Color.GRAY)
            return
        }

        db.collection("dispositivos")
            .whereEqualTo("imei", deviceId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    textStatus.text = "Error de conexión"
                    layoutMain.setBackgroundColor(Color.GRAY)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val doc = snapshots.documents.first()
                    val estado = doc.getString("estado") ?: "activo"

                    if (estado == "bloqueado") {
                        textStatus.text = "Dispositivo BLOQUEADO"
                        layoutMain.setBackgroundColor(Color.RED)
                    } else {
                        textStatus.text = "Dispositivo ACTIVO"
                        layoutMain.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                    }
                } else {
                    textStatus.text = "Dispositivo no encontrado"
                    layoutMain.setBackgroundColor(Color.GRAY)
                }
            }
    }


}

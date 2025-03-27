package com.maverick.clientapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterIdActivity : AppCompatActivity() {

    private lateinit var editDeviceId: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_id)

        editDeviceId = findViewById(R.id.editDeviceId)
        btnGuardar = findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val idIngresado = editDeviceId.text.toString().trim()

            if (idIngresado.isNotEmpty()) {
                // 🔸 Guardar el IMEI ingresado en SharedPreferences
                val prefs = getSharedPreferences("config", MODE_PRIVATE)
                prefs.edit().putString("deviceId", idIngresado).apply()

                Toast.makeText(this, "IMEI guardado correctamente", Toast.LENGTH_SHORT).show()

                // 🔸 Iniciar la actividad principal
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Por favor ingresa el IMEI", Toast.LENGTH_SHORT).show()
            }
        }

    }
}

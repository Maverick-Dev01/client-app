package com.maverick.clientapp

/**
 * RegisterIdActivity
 *
 * Actividad encargada de registrar manualmente el identificador del dispositivo (IMEI o ANDROID_ID)
 * que será usado para enlazar el dispositivo con un documento específico en Firestore.
 *
 * Características:
 * - Permite al vendedor ingresar el identificador del dispositivo manualmente.
 * - Almacena el valor en SharedPreferences bajo la clave "deviceId".
 * - Redirige a MainActivity una vez guardado correctamente.
 *
 * Esta actividad solo se muestra una vez al inicio, y es fundamental para que la app
 * pueda luego verificar su estado (activo o bloqueado) en Firestore.
 */


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

        inicializarVista()
        configurarBotonGuardar()
    }

    private fun inicializarVista() {
        editDeviceId = findViewById(R.id.editDeviceId)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun configurarBotonGuardar() {
        btnGuardar.setOnClickListener {
            val deviceId = editDeviceId.text.toString().trim()

            if (deviceId.isNotEmpty()) {
                guardarDeviceId(deviceId)
                Toast.makeText(this, "IMEI guardado correctamente", Toast.LENGTH_SHORT).show()
                irAMainActivity()
            } else {
                Toast.makeText(this, "Por favor ingresa el IMEI", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarDeviceId(id: String) {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        prefs.edit().putString("deviceId", id).apply()
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

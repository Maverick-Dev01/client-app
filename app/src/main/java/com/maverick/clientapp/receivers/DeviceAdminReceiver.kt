package com.maverick.clientapp.receivers

/**
 * DeviceAdminReceiver
 *
 * Clase que extiende `DeviceAdminReceiver` y actúa como administrador del dispositivo.
 * Es requerida para que la aplicación pueda acceder a funcionalidades avanzadas de control,
 * como activar el modo kiosko, evitar la desinstalación o bloquear el dispositivo.
 *
 * Este receiver debe estar registrado en el AndroidManifest y su política definida en un archivo XML.
 * También muestra un mensaje breve (Toast) cuando se activan o desactivan los permisos de administrador.
 */


import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Permiso de administrador activado", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Permiso de administrador desactivado", Toast.LENGTH_SHORT).show()
    }
}

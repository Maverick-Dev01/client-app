package com.maverick.clientapp.receivers
/**
 * BootReceiver
 *
 * BroadcastReceiver que se activa automáticamente cuando el dispositivo se reinicia
 * (`BOOT_COMPLETED`). Su función principal es iniciar el servicio LockService para verificar
 * si el dispositivo debe ser bloqueado, basándose en la información almacenada en Firestore.
 *
 * Este componente es esencial para garantizar que el bloqueo del dispositivo sea persistente,
 * incluso después de reinicios o apagados inesperados.
 *
 * Requisitos:
 * - Permiso RECEIVE_BOOT_COMPLETED en el AndroidManifest.xml
 * - Registro correcto del receiver en el Manifest
 * - La app debe haberse abierto al menos una vez después de la instalación
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.maverick.clientapp.services.LockService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, LockService::class.java)

            // Usamos startForegroundService solo si el dispositivo es Android 8.0 o superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
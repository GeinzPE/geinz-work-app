package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.content.Context
import android.content.Intent
import android.widget.Toast

object constantes_compartir {
    fun compartir_pantalla_completa(
        pantalla: String,
        txt: String,
        context: Context
    ) {
        try {
            val link = "https://geinzworkapp.web.app/share?t=scr&id=$pantalla"
            val texto = "$txt $link"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
            }

            context.startActivity(
                Intent.createChooser(intent, "Compartir con")
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al compartir", Toast.LENGTH_SHORT).show()
        }
    }
}
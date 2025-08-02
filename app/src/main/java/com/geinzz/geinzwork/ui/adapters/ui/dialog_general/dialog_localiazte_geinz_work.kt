package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.geinzz.geinzwork.R

import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import java.nio.file.WatchEvent

@Composable
fun dialog_sin_ubicacion_activa(
    onDismis: () -> Unit,
    abrir_configuracion: () -> Unit,
    dialog_sin_maps: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismis() },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    abrir_configuracion()
                },
                shape = RoundedCornerShape(15)
            ) { Text(text = "Activar Ubicación") }
        },
        dismissButton = { TextButton(onClick = { onDismis() }) { Text(text = "Cerrar") } },
        title = { Text(text = "Ubicación desactivada") },
        text = {
            Column {
                Text("Te recomendamos activar el GPS para que podamos mostrarte la mejor ruta hasta la tienda en Google Maps.")
                spacer_vertical(10.dp)
                Text(
                    text = "Continuar sin activar ubicación",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable { dialog_sin_maps() }
                        .padding(top = 8.dp)
                )
            }
        },
        shape = RoundedCornerShape(10),
        icon = {
            Icon(
                modifier = Modifier.size(25.dp),
                painter = painterResource(R.drawable.google_maps_icono),
                contentDescription = "Google maps",
                tint = Color.Unspecified
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )

    )

}

@Composable
fun dialog_sin_ubi_activa(
    direccion: String,
    referencia: String,
    onDismis: () -> Unit,
    abrir_maps: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismis() },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { abrir_maps() },
                shape = RoundedCornerShape(15)
            ) { Text(text = "Abrir con Google Maps") }
        },
        dismissButton = { TextButton(onClick = { onDismis() }) { Text(text = "Cerrar") } },

        title = { Text(text = "Dirección y referencia") },
        text = {
            Column {
                Text("Usa esta información de manera responsable. El mal uso será reportado.")
                spacer_vertical(10.dp)
                Calle_referencia("Direccion : ", direccion)
                spacer_vertical(10.dp)
                Calle_referencia("Referencia : ", referencia)
                spacer_vertical(10.dp)
            }
        },
        shape = RoundedCornerShape(10),
        icon = {
            Icon(
                modifier = Modifier.size(25.dp),
                painter = painterResource(R.drawable.google_maps_icono),
                contentDescription = "Google maps",
                tint = Color.Unspecified
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = SecureFlagPolicy.SecureOn
        )

    )
}

@Composable
fun Calle_referencia(text1: String, text2: String) {
    val context = LocalContext.current
    Row {
        Row(modifier = Modifier.weight(1f)) {
            Text(text = text1)
            Text(
                text = text2, maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }
        spacer_horizonta(5.dp)
        Icon(
            modifier = Modifier.clickable {
                constantestextos_general.copiarTexto_portapapeles_compouse(text2, context)
            },
            painter = painterResource(R.drawable.baseline_content_copy_24),
            contentDescription = ""
        )
    }
}
@Composable
fun spacer_vertical(altura: Dp) {
    Spacer(modifier = Modifier.height(altura))
}

@Composable
fun spacer_horizonta(ancho: Dp) {
    Spacer(modifier = Modifier.width(ancho))

}

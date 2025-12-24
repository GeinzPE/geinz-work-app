package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.generar_qr_ubi_tinda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line

import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
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
            btn_aceptar_etc_dialog_general(txt_btn="Activar Ubicación") {
                abrir_configuracion()
            }
        },
        dismissButton = {  btn_cerra_etc_dialog_general {
            onDismis()
        } },
        title = {
            FuenteControladaApp{

            Text(
                text = "Ubicación desactivada",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            }
        },
        text = {
            Column {
                FuenteControladaApp{
                Text(
                    "Te recomendamos activar el GPS para que podamos mostrarte la mejor ruta hasta el lugar en Google Maps.",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
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
fun dialog_sin_ubi__rutas(texto:String,onDismis: () -> Unit,abrir_configuracion: () -> Unit){
    AlertDialog(
        onDismissRequest = { onDismis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general(txt_btn="Activar Ubicación") {
                abrir_configuracion()
            }
        },
        dismissButton = {  btn_cerra_etc_dialog_general {
            onDismis()
        } },
        title = {
            FuenteControladaApp{
            Text(
                text = "Ubicación desactivada",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            }
        },
        text = {
            FuenteControladaApp{
            Column {
                Text(
                    texto,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
            }
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
fun dialog_crear_ruta_lugares(onDismis: () -> Unit, crear_ruta: (Boolean) -> Unit) {
    AlertDialog(
        confirmButton = {
            btn_aceptar_etc_dialog_general (txt_btn="Crear ruta"){
                crear_ruta(true)
                onDismis()
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general{ onDismis()}
         },
        onDismissRequest = { onDismis() },
        icon = {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(R.drawable.icon_3d_ruta),
                contentDescription = "",  tint = Color.Unspecified
            )
        },
        title = {
            FuenteControladaApp{
            texto_generico_one_line("Crear ruta", MaterialTheme.typography.titleLarge)
            spacer_vertical(10.dp)
            }
        },

        text = {
            FuenteControladaApp{
            texto_generico_multilinea(
                "Estás a punto de generar una ruta desde tu ubicación actual hasta este destino en el mapa. La ruta te mostrará el recorrido paso a paso para que puedas llegar de manera sencilla. Recuerda que necesitas tener tu ubicación activada para que el sistema pueda calcular el camino correctamente.",
                MaterialTheme.typography.bodyMedium
            )
            }
        },
    )
}

@Composable
fun dialog_qr_tienda(qr: String, nombre_tienda: String, onDismis: () -> Unit) {
    AlertDialog(
        confirmButton = {},
        dismissButton = {
            Button(onClick = { onDismis() }) {
                Text(
                    text = "Cerrar",
                    color = Color.White
                )
            }
        },
        onDismissRequest = { onDismis() },
        icon = {
            Icon(
                modifier = Modifier.size(25.dp),
                painter = painterResource(R.drawable.qr_scaner_icon),
                contentDescription = "Google maps",
                tint = MaterialTheme.colorScheme.onBackground
            )
        },
        title = {
            FuenteControladaApp{

            Text(
                text = "Direccion de la tienda",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            spacer_vertical(10.dp)
            }
        },

        text = {
            FuenteControladaApp{
            generar_qr_ubi_tinda(
                qr,
                "Escanea el QR de la tienda para llegar a $nombre_tienda rápidamente. Recomendamos que actives tu ubicación antes de escanear.",
            )
            }
        },
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
            btn_aceptar_etc_dialog_general ( txt_btn="Abrir con Google Maps"){
                abrir_maps()
            }

        },
        dismissButton = {
            btn_cerra_etc_dialog_general {
                onDismis()
            }
        },

        title = {
            FuenteControladaApp{

            Text(
                text = "Dirección y referencia",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            }
        },
        text = {
            FuenteControladaApp{
            Column {
                Text(
                    "Usa esta información de manera responsable. El mal uso será reportado.",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
                Calle_referencia("Direccion : ", direccion)
                spacer_vertical(10.dp)
                Calle_referencia("Referencia : ", referencia)
                spacer_vertical(10.dp)
            }
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
fun Calle_referencia(texto1: String, texto2: String) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            FuenteControladaApp{
            Text(text = texto1, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = texto2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            }

        }
        spacer_horizonta(5.dp)
        Icon(
            modifier = Modifier.clickable {
                constantestextos_general.copiarTexto_portapapeles_compouse(texto2, context)
            },
            painter = painterResource(R.drawable.baseline_content_copy_24),
            contentDescription = "", tint = MaterialTheme.colorScheme.onBackground
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
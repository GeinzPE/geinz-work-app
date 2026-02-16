package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general.copiarTexto_portapapeles_compouse
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder


val REQUEST_CALL_PHONE = 1

@Composable
fun dialog_llamada_urgencias(
    fusedLocationClient: FusedLocationProviderClient,
    viewmodeSeguridadSalud: viewmode_seguridad_salud,
    lista_numeros: List<String>,
    tipo: String,
    ondimiss: () -> Unit
) {
    val context = LocalContext.current
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by rememberSaveable { mutableStateOf("") }
    val repo_seguridad__salud = repo_seguridad_salud()
    val es_una_emergencia_conctactar by viewmodeSeguridadSalud.es_una_emergegecia.collectAsState()
    var texto_emergencia by remember { mutableStateOf("") }

//    LaunchedEffect(es_una_emergencia_conctactar) {
//
//        if (es_una_emergencia_conctactar) {
//
//            val datosConCallback =
//                repo_seguridad__salud.obtenerUbicacionUsuarioCancelable(fusedLocationClient)
//
//            val datos = datosConCallback.latLng
//
//            Log.d("VER_DISTANCIA", "Lat=${datos.latitude}, Lng=${datos.longitude}")
//
//            repo_seguridad__salud.cancelarUbicacion(
//                fusedLocationClient,
//                datosConCallback.callback
//            )
//
//            // 🔥 Construimos el link real con coordenadas dinámicas
//            val linkUbicacion =
//                "https://www.google.com/maps/dir/?api=1&destination=${datos.latitude},${datos.longitude}"
//
//            texto_emergencia = """
//🚨 EMERGENCIA REAL 🚨
//Me encuentro en esta ubicación:
//$linkUbicacion
//
//Necesito apoyo rápidamente.
//Geinz
//""".trimIndent()
//
//        }
//    }

    AlertDialog(
        onDismissRequest = { ondimiss() },
        confirmButton = {},
        dismissButton = {},
        title = {
            FuenteControladaApp {
                texto_generico_one_line("Números de emergencia")
            }
        },
        text = {
            FuenteControladaApp {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    texto_generico_multilinea(
                        "En caso de emergencia, comunícate de inmediato con los servicios de seguridad y salud.Puedes llamar o ir a whatsApp directamente tocando el ícono de teléfono o copiar el número que necesites.",
                        MaterialTheme.typography.bodyMedium
                    )

                    lista_numeros.forEach { i ->
                        box_llamada_whatsap(
                            i, tipo,
                            click_icon = {
                                if (tipo.equals("whatsapp")) {

                                    CoroutineScope(Dispatchers.Main).launch {

                                        if (es_una_emergencia_conctactar) {

                                            enviarMensajeEmergencia(
                                                fusedLocationClient,
                                                repo_seguridad__salud,
                                                onMensajeListo = { msj ->
                                                    abrir_whattsapp(
                                                        "",
                                                        tipo = "",
                                                        id_tienda = "",
                                                        localidad_tienda = "",
                                                        context = context,
                                                        numero ="937659216",
                                                        mensajePredefinido = msj
                                                    )

                                                })


                                        } else {
                                            // 🔥 Solo abre WhatsApp sin texto
                                            val uri = Uri.parse(
                                                "https://api.whatsapp.com/send?phone=+51937659216"
                                            )

                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        }
                                    }
                                } else if (tipo.equals("llamada")) {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CALL_PHONE
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        call_dialog_permise = true
                                        numero_llamada = i
                                    } else {
                                        makePhoneCall(context, i)
                                    }
                                }
                            },
                            click_copiar = { copiarTexto_portapapeles_compouse(i, context) })

                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        icon = {
            Image(
                painter = painterResource(if (tipo.equals("whatsapp")) R.drawable.whatsapp_icon else R.drawable.llamada_icon),
                contentDescription = "", modifier = Modifier.size(30.dp)
            )
        },

        )
    if (call_dialog_permise) {
        permisos_llamadas(aceptar_permisos = {
            requestCallPermission(context = context, phoneNumber = numero_llamada)
        }, ondimis = {
            call_dialog_permise = false
        })
    }
}

fun enviarMensajeEmergencia(
    fusedLocationClient: FusedLocationProviderClient,
    repo_seguridad__salud: repo_seguridad_salud, // tu repo que maneja ubicación
    onMensajeListo: (String) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            // Obtener ubicación
            val datosConCallback =
                repo_seguridad__salud.obtenerUbicacionUsuarioCancelable(fusedLocationClient)
            val datos = datosConCallback.latLng

            // Cancelar la actualización de ubicación
            repo_seguridad__salud.cancelarUbicacion(fusedLocationClient, datosConCallback.callback)

            // Generar link de Google Maps
            val linkUbicacion =
                "https://www.google.com/maps/dir/?api=1&destination=${datos.latitude},${datos.longitude}"

            // Armar mensaje final
            val mensajeFinal = """
                🚨 EMERGENCIA REAL 🚨
                Me encuentro en esta ubicación:
                $linkUbicacion
                Necesito apoyo rápidamente.
                Geinz
            """.trimIndent()

            // Retornar mensaje
            onMensajeListo(mensajeFinal)
        } catch (e: Exception) {
            e.printStackTrace()
            onMensajeListo("No se pudo obtener la ubicación. ❌")
        }
    }
}

@Composable
fun box_llamada_whatsap(
    numero: String,
    tipo: String,
    click_icon: () -> Unit,
    click_copiar: () -> Unit
) {
    FuenteControladaApp {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            texto_generico_one_line(
                numero,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Row() {
                Image(
                    painter = painterResource(if (tipo.equals("whatsapp")) R.drawable.whatsapp_icon else R.drawable.llamada_icon),
                    contentDescription = "", modifier = Modifier
                        .size(25.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { click_icon() }
                )
                spacer_horizonta(10.dp)
                Image(
                    painter = painterResource(R.drawable.baseline_content_copy_24),
                    contentDescription = "",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { click_copiar() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }

}

fun requestCallPermission(llamar: Boolean = true, context: Context, phoneNumber: String = "") {
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(android.Manifest.permission.CALL_PHONE),
            REQUEST_CALL_PHONE
        )
    } else {
        if (llamar) {
            makePhoneCall(context, phoneNumber)
        }
    }
}

fun makePhoneCall(context: Context, phoneNumber: String) {
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = Uri.parse("tel:$phoneNumber")
    if (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        context.startActivity(callIntent)
    } else {
        requestCallPermission(context = context, phoneNumber = phoneNumber)
    }
}
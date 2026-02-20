package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Layout
import android.util.Log

import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general.copiarTexto_portapapeles_compouse
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder


val REQUEST_CALL_PHONE = 1

@Composable
fun dialog_llamada_urgencias(
    img_entidad:String,
    nombre_entidad_seletc: String,
    launcher_dialog_ubicacion: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    permision_obtener_cordenadas: ManagedActivityResultLauncher<String, Boolean>,
    fusedLocationClient: FusedLocationProviderClient,
    viewmodeSeguridadSalud: viewmode_seguridad_salud,
    lista_numeros: List<String>,
    tipo: String,
    ondimiss: () -> Unit,abrir_dialog_entidad_msj:()-> Unit
) {
    val context = LocalContext.current
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by rememberSaveable { mutableStateOf("") }
    val repo_seguridad__salud = repo_seguridad_salud()
    val es_una_emergencia_conctactar by viewmodeSeguridadSalud.es_una_emergegecia.collectAsState()
    var texto_emergencia by remember { mutableStateOf("") }
    var enviar_msje_con_ubicacion by remember { mutableStateOf(false) }
    var ofuscar_btns_acces by remember { mutableStateOf(false) }
    var msje_general_whatsapp by remember { mutableStateOf("") }



    LaunchedEffect(enviar_msje_con_ubicacion) {
        if (!enviar_msje_con_ubicacion) return@LaunchedEffect

        try {
            ofuscar_btns_acces = true

            val datosConCallback =
                repo_seguridad__salud.obtenerUbicacionUsuarioCancelable(
                    fusedLocationClient
                )

            val datos = datosConCallback.latLng

            if (datos.latitude == 0.0 && datos.longitude == 0.0) {
                enviar_msje_con_ubicacion = false
                Toast.makeText(
                    context,
                    "No se pudo obtener tu ubicación, inténtalo otra vez",
                    Toast.LENGTH_SHORT
                ).show()
                return@LaunchedEffect
            }

            val url_maps =
                "https://www.google.com/maps/dir/?api=1&destination=${datos.latitude},${datos.longitude}"

            msje_general_whatsapp = """
            🚨 EMERGENCIA REAL 🚨
            Me encuentro en esta ubicación:
            $url_maps
            Necesito apoyo rápidamente.
            Geinz
        """.trimIndent()

            repo_seguridad__salud.cancelarUbicacion(
                fusedLocationClient,
                datosConCallback.callback
            )

            ofuscar_btns_acces = false

        } catch (e: Exception) {
            enviar_msje_con_ubicacion = false
            ofuscar_btns_acces = false
        }
    }

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
                    if (tipo.equals("whatsapp")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            TextoSubrayado(
                                "Compartir ubicacion con $nombre_entidad_seletc",
                                modifier = Modifier.weight(1f).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }){
                                    abrir_dialog_entidad_msj()
                                },
                                color_subrallado = MaterialTheme.colorScheme.primary
                            )
                            Switch(
                                checked = enviar_msje_con_ubicacion,
                                onCheckedChange = { value ->
                                    if (value) {

                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {

                                            permision_obtener_cordenadas.launch(
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            )

                                        } else if (!verificarUbiActiva(context)) {

                                            verificarGPS(context, launcher_dialog_ubicacion)

                                        } else {

                                            enviar_msje_con_ubicacion = true
                                        }

                                    } else {
                                        // El usuario quiere desactivar
                                        enviar_msje_con_ubicacion = false
                                    }
                                }, colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.3f
                                    )
                                )
                            )
                        }
                    }

                    texto_generico_multilinea(
                        "En caso de emergencia, comunícate de inmediato con los servicios de seguridad y salud.Puedes llamar o ir a whatsApp directamente tocando el ícono de teléfono o copiar el número que necesites.",
                        MaterialTheme.typography.bodyMedium
                    )
                    if (!ofuscar_btns_acces) {
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
                                                            numero = "937659216",
                                                            mensajePredefinido = msj
                                                        )

                                                    })


                                            } else {
                                                // 🔥 Solo abre WhatsApp sin texto
                                                val texto_a_enviar_diretamte =
                                                    if (enviar_msje_con_ubicacion) {
                                                        msje_general_whatsapp
                                                    } else {
                                                        ""
                                                    }
                                                val textoCodificado = URLEncoder.encode(
                                                    texto_a_enviar_diretamte,
                                                    "UTF-8"
                                                )

                                                Log.d("datos_a_pasr", "$texto_a_enviar_diretamte")
                                                val uri = Uri.parse(
                                                    "https://api.whatsapp.com/send?phone=+51937659216&text=$textoCodificado"
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
                    } else {
                        texto_generico_one_line("obteniendo tu ubicacion...", style = MaterialTheme.typography.bodyMedium)
                    }

                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier.size(45.dp)
            ) {

                Image(
                    painter = painterResource(R.drawable.whatsapp_icon),
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape)
                        .size(30.dp)
                        .align(Alignment.CenterStart)
                )
                if(img_entidad.isNotEmpty()){
                    AsyncImage(
                        model = img_entidad,
                        contentDescription = null,
                        contentScale = ContentScale.Crop, // 🔥 IMPORTANTE
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterEnd)
                    )

                }
            }
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
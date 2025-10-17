package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general.copiarTexto_portapapeles_compouse
import java.net.URLEncoder


val REQUEST_CALL_PHONE = 1

@Composable
fun dialog_llamada_urgencias(lista_numeros: List<String>, tipo: String, ondimiss: () -> Unit) {
    val context = LocalContext.current
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { ondimiss() },
        confirmButton = {},
        dismissButton = {},
        title = { texto_generico_one_line("Números de emergencia") },
        text = {
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
                                val uri = Uri.parse(
                                    "https://api.whatsapp.com/send?phone=${"+51 $i"}&text=${
                                        URLEncoder.encode(
                                            "",
                                            "UTF-8"
                                        )
                                    }"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "no se pudo abrir whatsapp",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
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

@Composable
fun box_llamada_whatsap(
    numero: String,
    tipo: String,
    click_icon: () -> Unit,
    click_copiar: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
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

private fun makePhoneCall(context: Context, phoneNumber: String) {
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
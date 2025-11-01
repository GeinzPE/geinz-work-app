package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dialog_seguridad_salud_algolia
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general.copiarTexto_portapapeles_compouse
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import kotlinx.coroutines.delay

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Composable
fun dialog_salud_seguridad_algolia(
    long: Long,
    item: dialog_seguridad_salud_algolia,
    ondimis: () -> Unit
) {
    Log.d("lsita", "llamada${item.lista_llamada.size} whatsapp ${item.lista_whatsapp.size}")
    var mostrar_lista_numero by remember { mutableStateOf("") }
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }
    var context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(long) {
        isLoading = true
        delay(long.coerceAtLeast(1000))
        isLoading = false
    }

    val colorLlamada by animateColorAsState(
        targetValue = if (mostrar_lista_numero == "llamada")
            MaterialTheme.colorScheme.surfaceVariant
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )

    val colorWhatsapp by animateColorAsState(
        targetValue = if (mostrar_lista_numero == "whatsapp")
            MaterialTheme.colorScheme.surfaceVariant
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )

    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {},
        dismissButton = {},
        title = {},
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.img)
                            .size(40, 40)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(), contentDescription = "Imagen",
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    spacer_horizonta(10.dp)
                    texto_generico_one_line(item.nombre)

                }
                spacer_vertical(20.dp)
                texto_generico_multilinea(
                    "Estos son los contactos de emergencia disponibles. Úsalos únicamente en situaciones urgentes. Mantén la calma",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(20.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    Crossfade(targetState = isLoading, label = "") { loading ->
                        if (loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.lista_llamada.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(53.dp)
                                                .clip(CircleShape)
                                                .background(colorLlamada),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.llamada_icon),
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(45.dp)
                                                    .clickable (indication = null,interactionSource= remember { MutableInteractionSource() }){
                                                        mostrar_lista_numero = "llamada"
                                                    }
                                            )
                                        }
                                    }
                                }
                                if (item.lista_whatsapp.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(53.dp)
                                                .clip(CircleShape)
                                                .background(colorWhatsapp),
                                            contentAlignment = Alignment.Center
                                        ) {

                                        Image(
                                            painter = painterResource(R.drawable.whatsapp_icon),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(45.dp)
                                                .clickable (indication = null,interactionSource= remember { MutableInteractionSource() }){
                                                    mostrar_lista_numero = "whatsapp"
                                                }
                                        )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                spacer_vertical(10.dp)


                if (mostrar_lista_numero == "llamada") {
                    Log.d("lsita", "${item.lista_llamada.size}")
                    spacer_vertical(5.dp)
                    item.lista_llamada.forEach { i ->
                        box_llamada_whatsap(
                            i, mostrar_lista_numero,
                            click_icon = {
                                llamar(context, i, {
                                    call_dialog_permise = true
                                    numero_llamada = i
                                })
                            },
                            click_copiar = { copiarTexto_portapapeles_compouse(i, context) })
                    }
                    spacer_vertical(5.dp)
                } else if (mostrar_lista_numero == "whatsapp") {
                    Log.d("lsita", "${item.lista_whatsapp.size}")
                    spacer_vertical(5.dp)

                    item.lista_whatsapp.forEach { i ->
                        box_llamada_whatsap(
                            i, mostrar_lista_numero,
                            click_icon = {
                                abrir_whattsapp(context, i)
                            },
                            click_copiar = { copiarTexto_portapapeles_compouse(i, context) })
                    }
                    spacer_vertical(5.dp)

                }


            }
        }
    )
    if (call_dialog_permise) {
        permisos_llamadas(aceptar_permisos = {
            requestCallPermission(context = context, phoneNumber = numero_llamada)
        }, ondimis = {
            call_dialog_permise = false
        })
    }


}
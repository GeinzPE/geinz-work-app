package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_close_gris
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import okhttp3.internal.wait

@Composable
fun dialog_servicios_tramite(
    localida: String,
    ondimis: () -> Unit,
    dataclass_lugares_db: dataclass_lugares_db
) {
    var mostar_redes by remember { mutableStateOf(false) }
    val latitud = dataclass_lugares_db.direccion.lat
    val longitud = dataclass_lugares_db.direccion.log
    val direccion = dataclass_lugares_db.direccion.direccion
    val referencia = dataclass_lugares_db.direccion.refencia
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }

    var mostar_dialog_ubicacion by remember { mutableStateOf(false) }
    var mostrarDialog_sin_google_maps by remember { mutableStateOf(false) }
    val numero_llamada by remember { mutableStateOf("") }
    val contex = LocalContext.current

    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {},
        text = {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    btn_close_gris(
                        modifier = Modifier.align(Alignment.TopEnd),
                        size_icon = 15.dp,
                        imageVector = Icons.Default.Close,
                        onClick = { ondimis() })
                }
                spacer_vertical(10.dp)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .data(dataclass_lugares_db.logo_img).build(),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            mostar_redes = !mostar_redes
                        }, contentScale = ContentScale.Crop
                )
                spacer_vertical(12.dp)
                texto_generico_one_line(
                    "${dataclass_lugares_db.lugar_nombre.capitalizeFirst()} - ${localida.capitalizeFirst()}",
                    style = MaterialTheme.typography.titleLarge
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    dataclass_lugares_db.descripcion.capitalizeFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(20.dp)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth()
                        .clickable {
                            constantes_lista_localidades.abrir_google_maps(
                                contex,
                                latitud,
                                longitud
                            ) { dialog ->
                                mostar_dialog_ubicacion = dialog
                            }
                        }
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        texto_generico_one_line(
                            "Crear Ruta",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_horizonta(5.dp)
                        Image(
                            painter = painterResource(R.drawable.localidad_icon_general),
                            contentDescription = "", modifier = Modifier.size(20.dp)
                        )
                    }
                }
                spacer_vertical(20.dp)

                texto_generico_one_line("Redes y contacto")

                spacer_vertical(15.dp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (dataclass_lugares_db.contacto.whatsapp.isNotEmpty()) {
                                dataclass_lugares_db.contacto.whatsapp.map { i ->
                                    Image(
                                        painter = painterResource(R.drawable.whatsapp_icon),
                                        contentDescription = "",
                                        Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                abrir_whattsapp(contex, i)
                                            }
                                    )
                                }
                            }
                            if (dataclass_lugares_db.contacto.telefono.isNotEmpty()) {
                                dataclass_lugares_db.contacto.telefono.map { i ->

                                    Image(
                                        painter = painterResource(R.drawable.llamada_icon),
                                        contentDescription = "",
                                        Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                llamar(contex, i, {
                                                    call_dialog_permise = true
                                                })
                                            }
                                    )
                                }
                            }
                            if (dataclass_lugares_db.contacto.sitio_web.isNotEmpty()) {
                                Image(
                                    painter = painterResource(R.drawable.sitio_web),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(35.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            openWebLink(
                                                contex,
                                                dataclass_lugares_db.contacto.sitio_web
                                            )
                                        },
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }

                        }
                    }
                    item {
                        AnimatedVisibility(mostar_redes, enter = fadeIn(), exit = fadeOut()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                if (dataclass_lugares_db.contacto.ig.isNotEmpty()) {

                                    Image(
                                        painter = painterResource(R.drawable.instagram_icon),
                                        contentDescription = "",
                                        Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                openInstagram(
                                                    contex,
                                                    dataclass_lugares_db.contacto.ig
                                                )
                                            }
                                    )
                                }
                                if (dataclass_lugares_db.contacto.facebook.isNotEmpty()) {
                                    Image(
                                        painter = painterResource(R.drawable.facebook_icon),
                                        contentDescription = "",
                                        Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                openFacebook(
                                                    contex,
                                                    dataclass_lugares_db.contacto.facebook
                                                )
                                            }
                                    )
                                }

                                if (dataclass_lugares_db.contacto.ig.isNotEmpty()) {

                                    Image(
                                        painter = painterResource(R.drawable.tik_tok_icon),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                openTiktok(
                                                    contex,
                                                    dataclass_lugares_db.contacto.tk
                                                )
                                            }
                                    )
                                }

                            }
                        }
                    }
                }
            }
            if (call_dialog_permise) {
                permisos_llamadas(aceptar_permisos = {
                    requestCallPermission(contex, numero_llamada)
                }, ondimis = {
                    call_dialog_permise = false
                })
            }


            if (mostar_dialog_ubicacion) {
                dialog_sin_ubicacion_activa(
                    onDismis = {
                        mostar_dialog_ubicacion = false
                    }, abrir_configuracion = {
                        mostar_dialog_ubicacion = false
                        contex.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    },
                    dialog_sin_maps = {
                        mostar_dialog_ubicacion = false
                        mostrarDialog_sin_google_maps = true
                    }
                )
            }

            if (mostrarDialog_sin_google_maps) {
                dialog_sin_ubi_activa(
                    direccion = direccion,
                    referencia = referencia,
                    onDismis = { mostrarDialog_sin_google_maps = false },
                    abrir_maps = { constantes.abrirGoogleMaps(contex, direccion) })
            }
        }
    )
}
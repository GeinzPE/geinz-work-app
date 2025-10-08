package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

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
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil3.compose.AsyncImage
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
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
    val calle = dataclass_lugares_db.direccion.direccion
    val referencia = dataclass_lugares_db.direccion.refencia

    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {},
        title = { dataclass_lugares_db.lugar_nombre },
        text = {

            Column {
                AsyncImage(
                    model = coil3.request.ImageRequest.Builder(LocalContext.current)
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
                    "Llega facilemte creando una ruta o copiando la direccion y ubicacion",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(20.dp)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth()
                        .clickable {}
                ) {
                    Row(Modifier.padding(10.dp)) {
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
                            Image(
                                painter = painterResource(R.drawable.whatsapp_icon),
                                contentDescription = "",
                                Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                            )
                            Image(
                                painter = painterResource(R.drawable.llamada_icon),
                                contentDescription = "",
                                Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                            )

                            Image(
                                painter = painterResource(R.drawable.sitio_web),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape),
                                colorFilter = ColorFilter.tint(Color.White)
                            )

                        }
                    }
                    item {
                        AnimatedVisibility(mostar_redes, enter = fadeIn(), exit = fadeOut()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.instagram_icon),
                                    contentDescription = "",
                                    Modifier
                                        .size(35.dp)
                                        .clip(CircleShape)
                                )
                                Image(
                                    painter = painterResource(R.drawable.facebook_icon),
                                    contentDescription = "",
                                    Modifier
                                        .size(35.dp)
                                        .clip(CircleShape)
                                )

                                Image(
                                    painter = painterResource(R.drawable.tik_tok_icon),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(35.dp)
                                        .clip(CircleShape),

                                    )

                            }
                        }
                    }
                }


            }
//                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
//                    item {
//                        Image(
//                            painter = painterResource(R.drawable.whatsapp_icon),
//                            contentDescription = "",
//                            Modifier
//                                .size(35.dp)
//                                .clip(CircleShape)
//                        )
//                    }
//                    item {
//                        Image(
//                            painter = painterResource(R.drawable.llamada_icon),
//                            contentDescription = "",
//                            Modifier
//                                .size(35.dp)
//                                .clip(CircleShape)
//                        )
//                    }
//                    item {
//                        Image(
//                            painter = painterResource(R.drawable.sitio_web),
//                            contentDescription = "",
//                            modifier = Modifier
//                                .size(35.dp)
//                                .clip(CircleShape),
//                            colorFilter = ColorFilter.tint(Color.White)
//                        )
//                    }
////                    item {
////                        Image(
////                            painter = painterResource(R.drawable.facebook_icon),
////                            contentDescription = "",
////                            Modifier
////                                .size(35.dp)
////                                .clip(CircleShape)
////                        )
////                    }
////                    item {
////                        Image(
////                            painter = painterResource(R.drawable.instagram_icon),
////                            contentDescription = "",
////                            Modifier
////                                .size(35.dp)
////                                .clip(CircleShape)
////                        )
////                    }
////                    item {
////                        Image(
////                            painter = painterResource(R.drawable.tik_tok_icon),
////                            contentDescription = "",
////                            Modifier
////                                .size(35.dp)
////                                .clip(CircleShape)
////                        )
////                    }
//
//
//                }



        }
    )
}
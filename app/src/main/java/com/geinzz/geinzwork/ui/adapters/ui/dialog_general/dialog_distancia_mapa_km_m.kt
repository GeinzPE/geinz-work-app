package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst

@Composable
fun dialog_distancia_map_km_m(
    id_tienda:String,
    distancia: String,
    HorarioDia_box: HorarioDia_box,
    img: String, nombre: String,
    tick: Long,
    estadoColor: Color,
    ondimis: () -> Unit
) {
    var horario_restante by remember { mutableStateOf("") }

    retornar_color_estado_tienda_Box(
        id_tienda = id_tienda,
        horario_total = HorarioDia_box,
        tick = tick,
        pagado = true,
        color = { color, txt ->
            horario_restante = txt
        }, mostrar_txt = false
    )
//    texto_tiempo_restante(
//        dataclass_map.horario_tienda,
//        dataclass_map.horario_tienda.h_cierre,
//        dataclass_map.horario_tienda.cerrado,
//        dataclass_map.horario_tienda.motivo,
//        tick
//    ) { txt ->
//        horario_restante = txt
//    }

    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general {
                ondimis()
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general {
                ondimis()
            }
        },
        text = {
            FuenteControladaApp {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(img)
                                .size(40, 40)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            texto_generico_one_line(nombre.capitalizeFirst())
                            spacer_horizonta(5.dp)
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(estadoColor)
                            )
                        }
                    }
                    spacer_vertical(10.dp)
                    texto_generico_one_line("Distancia en tiempo real")
                    spacer_vertical(5.dp)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White)) {
                                append("El valor mostrado ")
                            }
                            withStyle(style = SpanStyle(color = estadoColor)) {
                                append("(${distancia})")
                            }
                            withStyle(style = SpanStyle(color = Color.White)) {
                                append(" es una estimación calculada en línea recta y se actualiza al instante con tu GPS. La distancia final puede variar ligeramente debido a las rutas de las calles")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    spacer_vertical(10.dp)
                    texto_generico_one_line("Horaro en tiempo real")
                    spacer_vertical(5.dp)
                    texto_generico_multilinea(
                        "La disponibilidad  se muestra en el momento exacto. ¡Siempre sabrás si la tienda,negocio o lugar está listo para recibirte!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(5.dp)
                    texto_generico_one_line(
                        horario_restante,
                        MaterialTheme.typography.bodyMedium,
                        color = estadoColor
                    )

                }
            }
        }
    )
}
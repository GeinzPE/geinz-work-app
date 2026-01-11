package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.item_metodos_pago
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun dialog_qr_pago_tienda(id_user:String,item_metodos_pago: item_metodos_pago, ondimis: () -> Unit) {
    var mostar_numero_completo by remember { mutableStateOf(false) }
    var galeriaActiva by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {},
        dismissButton = {},
        text = {
            FuenteControladaApp{

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item_metodos_pago.icono_metodo_pago)
                            .size(40, 40)
                            .crossfade(true)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(), contentDescription = "Imagen",
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .clip(CircleShape)
                            ,
                        contentScale = ContentScale.Crop
                    )
                    spacer_horizonta(10.dp)
                    texto_generico_one_line(item_metodos_pago.nombre_metodo_pago)
                }
                spacer_vertical(15.dp)
                texto_generico_multilinea(
                    "⚠\uFE0F Usa esta información solo para pagos legítimos. El uso indebido puede tener consecuencias legales.",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(15.dp)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item_metodos_pago.codigo_qr)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .build(), contentDescription = "Imagen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp)).clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            galeriaActiva = true
                        },
                    contentScale = ContentScale.Crop
                )
                spacer_vertical(15.dp)

                if (item_metodos_pago.titular != "") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        texto_generico_one_line(
                            "Titular : ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_horizonta(5.dp)
                        texto_generico_one_line(
                            formatearNombre(item_metodos_pago.titular),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    spacer_vertical(15.dp)
                }

                if (item_metodos_pago.numero_String != "") {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        texto_generico_one_line(
                            "Numero : ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_horizonta(5.dp)
                        texto_generico_one_line(
                            texto = if (!mostar_numero_completo) {
                                formatearNumeroEnGrupos(
                                    constantes_lista_localidades.ocultarNumero(item_metodos_pago.numero_String)
                                )
                            } else {
                                formatearNumeroEnGrupos(item_metodos_pago.numero_String)
                            },
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                mostar_numero_completo = !mostar_numero_completo
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                    }
                }


            }
            }
        }
    )
    if(galeriaActiva){
        ZoomableGalleryFullScreen(id_user,compartir_promocion(),"",listOf(item_metodos_pago.codigo_qr),0, { galeriaActiva = false })
    }
}

fun formatearNumeroEnGrupos(numero: String, groupSize: Int = 3): String {
    val limpio = numero.replace(" ", "")
    return limpio.chunked(groupSize).joinToString(" ")
}

fun formatearNombre(nombreCompleto: String): String {
    val partes = nombreCompleto.trim().split(" ").filter { it.isNotBlank() }
    if (partes.isEmpty()) return ""

    val primerNombre = partes[0].replaceFirstChar { it.uppercase() }

    if (partes.size == 1) return primerNombre

    val segundoNombre =
        partes.getOrNull(1)?.firstOrNull()?.uppercaseChar()?.toString()?.plus(".") ?: ""
    val primerApellido = partes.getOrNull(2)?.replaceFirstChar { it.uppercase() } ?: ""
    val restoApellidos =
        partes.drop(3).mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString()?.plus(".") }

    val lista = listOfNotNull(primerNombre, segundoNombre, primerApellido) + restoApellidos

    return lista.joinToString(" ")
}


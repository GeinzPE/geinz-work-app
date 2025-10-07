package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import okhttp3.internal.wait

@Composable
fun dialog_servicios_tramite(ondimis: () -> Unit, dataclass_lugares_db: dataclass_lugares_db) {
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
                        .size(180, 180)
                        .data(dataclass_lugares_db.logo_img).build(),
                    contentDescription = "",
                    modifier = Modifier
                        .width(180.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    "Llega facilemte creando una ruta o copiando la direccion y ubicacion",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    texto_generico_one_line(
                        "Direccion",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_horizonta(5.dp)
                    texto_generico_one_line(
                        calle,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(R.drawable.baseline_content_copy_24),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                }
                spacer_vertical(10.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    texto_generico_one_line(
                        "Referencia",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_horizonta(5.dp)
                    texto_generico_one_line(
                        referencia,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(R.drawable.baseline_content_copy_24),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )

                }

            }
        }
    )
}
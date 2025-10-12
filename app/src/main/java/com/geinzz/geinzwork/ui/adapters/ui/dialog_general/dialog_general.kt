package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line


@Composable
fun dialog_sin_pago_tiendas(
    mostrandoCarga_free: Boolean,
    datos_tienda_free: datos_tienda_free,
    ondimis: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            ondimis()
        },
        confirmButton = {},
        dismissButton = {},
        title = {
            texto_generico_one_line(datos_tienda_free.nombre_)
        },
        text = {
            Crossfade(targetState = mostrandoCarga_free, label = "anim_carga") { cargando ->
                if (cargando) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = coil3.request.ImageRequest.Builder(LocalContext.current)
                                    .placeholder(R.drawable.cargando_img_categorias)
                                    .error(R.drawable.cargando_img_categorias)
                                    .data(datos_tienda_free.img)
                                    .build(),
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        spacer_vertical(15.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            texto_generico_one_line(
                                "Dirección",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            spacer_horizonta(5.dp)
                            texto_generico_one_line(
                                datos_tienda_free.ubicacion,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Image(
                                painter = painterResource(R.drawable.baseline_content_copy_24),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.White)
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
                                datos_tienda_free.referencia,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Image(
                                painter = painterResource(R.drawable.baseline_content_copy_24),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }

                        spacer_vertical(10.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            texto_generico_one_line("Horario aproximado")
                            spacer_horizonta(5.dp)
                            texto_generico_one_line(
                                datos_tienda_free.horario_default,
                            )
                        }
                    }
                }
            }
        },
        icon = {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8700F3).copy(alpha = 0.7f),
                                    Color.Transparent
                                ),
                            ),
                            shape = RoundedCornerShape(200.dp)
                        )
                )
                Image(
                    painter = painterResource(R.drawable.logo_geinz_blanco),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp)
                )
            }

        })
}
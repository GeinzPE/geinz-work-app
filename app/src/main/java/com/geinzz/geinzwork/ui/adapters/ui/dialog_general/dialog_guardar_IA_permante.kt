package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.model.content.CircleShape
import androidx.compose.ui.platform.LocalDensity

import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.FondoIAAnimado
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmodel_generaciones_IA
import kotlin.math.roundToInt

@Composable
fun dialog_guardar_IA_permanete(
    viewmodel: viewmodel_generaciones_IA,
    ondimis: () -> Unit,
    id_generacion: String,
    id_tienda: String,
    localidad: String,
    nombre_tienda: String,
    cantidad_monedas: Int, img_guardaro: String, titulo_generacion: String
) {


    var offsetX by remember { mutableStateOf(0f) }
    var maxDrag by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    val progreso = if (maxDrag > 0) (offsetX / maxDrag).coerceIn(0f, 1f) else 0f

    // Animación del color del fondo al llegar al final
    val fondoColor by animateColorAsState(
        targetValue = if (progreso > 0.9f)
            Color(0xFF2E7D32).copy(alpha = 0.7f) // verde semi
        else
            Color.Transparent,
        label = ""
    )

    AlertDialog(
        onDismissRequest = {
            ondimis()
        },
        confirmButton = {},
        dismissButton = {},
        text = {
            FuenteControladaApp {
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    texto_generico_multilinea(
                        "Haz permanente tu generación IA",
                        style = MaterialTheme.typography.titleLarge
                    )

                    texto_generico_multilinea(
                        "Accede a esta generación sin límites. Una vez guardada, podrás reutilizarla cuando lo necesites, solo para ti.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)){
                        texto_generico_one_line(
                            "Desliza para Guardar Permanente",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejorar con IA",
                            tint = Color.White
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)){
                        texto_generico_one_line(
                            "Inversion necesaria",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        texto_generico_one_line(
                            "13",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_horizonta(5.dp)
                        Image(
                            painter = painterResource(R.drawable.icon_monedas_3d),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(CircleShape)
                            .background(
                                lerp(
                                    Color.Transparent,
                                    Color(0xFF2E7D32).copy(alpha = 0.7f),
                                    progreso
                                )
                            )
                            .onSizeChanged { size ->
                                val maxDragPx = with(density) { 52.dp.toPx() }
                                maxDrag = size.width.toFloat() - maxDragPx
                            }
                    ) {
                        // Fondo animado visible solo mientras no se confirme
                        if (progreso < 0.9f) {
                            FondoIAAnimado(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .matchParentSize()
                            )
                        }


                        // Texto de confirmación centrado
                        if (progreso >= 0.9f) {
                            Text(
                                text = "Suelta para guardar",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Imagen draggable
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(img_guardaro)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(52.dp)
                                .offset { IntOffset(offsetX.roundToInt(), 0) }
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f))
                                .align(Alignment.CenterStart)
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (offsetX >= maxDrag * 0.9f) {
                                                offsetX = maxDrag
                                                viewmodel.guardar_como_permanete(
                                    id_generacion = id_generacion,
                                    id_tienda = id_tienda,
                                    localidad = localidad,
                                    nombre_tienda = nombre_tienda,
                                    saldo_tienda = cantidad_monedas
                                )
                                                ondimis() // acción confirmada
                                            } else {
                                                offsetX = 0f // vuelve al inicio
                                            }
                                        }
                                    ) { _, dragAmount ->
                                        offsetX = (offsetX + dragAmount).coerceIn(0f, maxDrag)
                                    }
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(48.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                    ) {
//
//                        // 🔥 Fondo animado visible
//                        FondoIAAnimado(
//                            modifier = Modifier.matchParentSize()
//                        )
//
//                        Button(
//                            onClick = {
//                                viewmodel.guardar_como_permanete(
//                                    id_generacion = id_generacion,
//                                    id_tienda = id_tienda,
//                                    localidad = localidad,
//                                    nombre_tienda = nombre_tienda,
//                                    saldo_tienda = cantidad_monedas
//                                )
//                                ondimis()
//                            },
//                            modifier = Modifier.matchParentSize(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Transparent,
//                                disabledContainerColor = Color.Transparent,
//                                contentColor = Color.White
//                            ),
//                            elevation = ButtonDefaults.buttonElevation(
//                                defaultElevation = 0.dp,
//                                pressedElevation = 0.dp
//                            )
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                            ) {
//                                texto_generico_one_line(
//                                    "Desliza para Guardar Permanente",
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
//                                spacer_horizonta(5.dp)
//                                Icon(
//                                    imageVector = Icons.Default.AutoAwesome,
//                                    contentDescription = "Mejorar con IA",
//                                    tint = Color.White
//                                )
//                                spacer_horizonta(5.dp)
//                                texto_generico_one_line(
//                                    "13",
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
//                                spacer_horizonta(5.dp)
//                                Image(
//                                    painter = painterResource(R.drawable.icon_monedas_3d),
//                                    contentDescription = null,
//                                    modifier = Modifier.size(20.dp)
//                                )
//
//                            }
//                        }
//                    }


                }
            }
        }
    )
}
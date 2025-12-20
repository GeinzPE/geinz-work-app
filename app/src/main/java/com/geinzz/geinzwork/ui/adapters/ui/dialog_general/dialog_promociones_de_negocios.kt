package com.geinzz.geinzwork.ui.adapters.ui.dialog_general


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.EstadoPromocion
import com.geinzz.geinzwork.viewModels.viewmodel_datos_promociones

@Composable
fun dialog_promociones_negocios(
    id_tienda: String,
    localidad: String,
    index: Int,
    onDismiss: () -> Unit
) {
    val viewModel: viewmodel_datos_promociones = viewModel()
    val estado by viewModel.estadoPromocion.collectAsState()

    LaunchedEffect(id_tienda, localidad, index) {
        viewModel.obtener_datos_promociones(id_tienda, localidad, index)
    }

    // 🔹 OVERLAY OSCURO
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() }
    ) {

        when (estado) {

            is EstadoPromocion.Cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            is EstadoPromocion.Exito -> {
                val promo = (estado as EstadoPromocion.Exito).data

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.65f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .clickable(enabled = false) {} // evita cerrar al tocar la imagen
                ) {

                    AsyncImage(
                        model = promo.url_img,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // ❌ BOTÓN CERRAR
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }
            }

            is EstadoPromocion.Vacio -> {
                Text(
                    "No hay promoción",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            is EstadoPromocion.Error -> {
                Text(
                    "Error al cargar",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }

}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_recientes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_datos_promos_noti(
    viewmodel_pantalla: viewmodel_pantallas_recientes,
    id_tienda: String,
    localida: String,
    id_promo: String,
    ondimis: () -> Unit
) {

    val estado_datos by viewmodel_pantalla.estadoPromocion.collectAsState()
    LaunchedEffect(id_tienda, localida, id_promo) {
        viewmodel_pantalla.cargarDatosPromocion(id_tienda, localida, id_promo)
    }
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            BoxWithConstraints {
                val maxHeightSheet = maxHeight * 0.8f
                val maxHeightSheet_empty = maxHeight * 0.4f

                when (estado_datos) {
                    is viewmodel_pantallas_recientes.EstadoDatosPromocion.Error -> {
                        Text((estado_datos as viewmodel_pantallas_recientes.EstadoDatosPromocion.Error).mensaje)
                    }

                    viewmodel_pantallas_recientes.EstadoDatosPromocion.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxHeight * 0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            ShimmerImagenConMarca()
                        }

                    }

                    is viewmodel_pantallas_recientes.EstadoDatosPromocion.Success -> {

                        val datos =
                            (estado_datos as viewmodel_pantallas_recientes.EstadoDatosPromocion.Success).datos
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = maxHeightSheet),

                            ) {

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 10.dp,
                                        end = 10.dp,
                                        top = 10.dp,
                                        bottom = 20.dp
                                    )
                                    .heightIn(max = maxHeightSheet),
                                verticalArrangement = Arrangement.spacedBy(25.dp)
                            ) {
                                item {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(datos.lista_img) { i ->
                                            Box(
                                                modifier = Modifier
                                                    .height(300.dp)
                                                    .width(300.dp)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(i)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .placeholder(R.drawable.cargando_img_categorias)
                                                        .error(R.drawable.cargando_img_categorias)
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .height(300.dp)
                                                        .width(300.dp)
                                                        .clip(RoundedCornerShape(5)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("Categoria")
                                        texto_generico_one_line(datos.categoira)
                                    }
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("compartir")
                                        texto_generico_one_line(datos.compartir.toString())
                                    }
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("contactar")
                                        texto_generico_one_line(datos.contactar.toString())
                                    }
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("id_promocion")
                                        texto_generico_one_line(datos.id_promocion)
                                    }
                                }
                                item {
                                        texto_generico_multilinea(datos.descripcion)
                                }
                                item {
                                        texto_generico_multilinea(datos.titulo)
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("numero de contacto")
                                    texto_generico_one_line(datos.numero)
                                    }
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("fecha iniciada")
                                        texto_generico_one_line(datos.fecha_iniciada.toString())

                                    }
                                }
                                item {
                                    Row() {
                                        texto_generico_one_line("fecha iniciada")
                                        texto_generico_one_line(datos.fecha_terminada.toString())
                                    }
                                }

                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
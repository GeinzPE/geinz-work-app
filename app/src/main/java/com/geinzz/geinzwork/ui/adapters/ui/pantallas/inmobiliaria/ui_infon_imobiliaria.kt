package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewmodel_inmobiliaria

@Composable
fun ui_info_imobiliara(
    viewModel: viewmodel_inmobiliaria,
    id: String,
    localidad: String,
    nombre_user: String
) {


    val estado by viewModel.estado_carga_info_inmuebles.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiar_estado_info()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.cargarDatos(id, localidad)
    }

    when (estado) {

        is viewmodel_inmobiliaria.etado_carga_info_inmuebles.idle -> {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        }

        is viewmodel_inmobiliaria.etado_carga_info_inmuebles.error -> {

            val error = (estado as viewmodel_inmobiliaria.etado_carga_info_inmuebles.error)

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error.txt)
            }

        }

        is viewmodel_inmobiliaria.etado_carga_info_inmuebles.succes -> {

            val datos = (estado as viewmodel_inmobiliaria.etado_carga_info_inmuebles.succes).datos

            LazyColumn() {
                item {
                    Box {

                        GaleriaHorizontalInstagram(
                            datos.listaImg,
                            modifier = Modifier,
                            { },
                            {
                                Log.d("LONG_PRESS", "Long press en la galería")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(Color.Black)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    texto_generico_one_line(datos.nombre)

                    texto_generico_one_line("${datos.distrito} / Lima")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp
                                )
                            )
                    )

                    texto_generico_one_line(
                        "Trato : ${datos.tipoOperacion}",
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    texto_generico_one_line(
                        "Tipo : ${datos.tipoPropiedad}",
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    Column {

                        ListaHorizontal(datos.llissa_lugareS_turistos)

                        ListaHorizontal(datos.cantidad_lugares_seguros)

                        ListaHorizontal(datos.listalugares_cercanos)

                        ListaHorizontal(datos.lista_servicios_sercanos)

                    }

                    val icon_bano = R.drawable.icono_bano
                    val icon_dormitorio = R.drawable.icono_dormitorio
                    val icono_cochera = R.drawable.icono_nochera
                    val icon_regla = R.drawable.icono_regla

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        ItemIcono(icon_regla, "${datos.metros} m²")

                        ItemIcono(
                            icon_dormitorio,
                            "${datos.habitaciones} dorm."
                        )

                        ItemIcono(
                            icon_bano,
                            "${datos.banos} baños."
                        )

                        ItemIcono(
                            icono_cochera,
                            "${datos.estacionamientos} estac."
                        )

                    }
                }


            }

        }

    }
}

@Composable
fun GaleriaHorizontalInstagram(
    imagenes: List<String>,
    modifier: Modifier = Modifier,
    img_clikeble_valor: (Int) -> Unit,
    long_listatener: () -> Int
) {
    val pagerState = rememberPagerState { imagenes.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)

    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            AsyncImage(
                model = imagenes[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            img_clikeble_valor(page)
                        },
                        onLongClick = {
                            long_listatener()
                        }),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.cargando_img_categorias),
                error = painterResource(R.drawable.cargando_img_categorias)
            )
        }

        // Indicador 1/5
        if (imagenes.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 50.dp, end = 8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${imagenes.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun ListaHorizontal(lista: List<lugares_cercanos_>) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        items(lista) { i ->

            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = i.img_String,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Crop
                )

                texto_generico_one_line(
                    "${i.nombre} / ${i.categoira}",
                    color = Color(0xFFB0B0B0),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 5.dp)
                )

            }

        }
    }

}
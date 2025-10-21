package com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.mascara_img
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.Estados_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.open_map_perzonlizado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda.LazyRowConSombras
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import java.nio.file.WatchEvent


@Composable
fun pantalla_lugares_turisticos(
    localidad_selecionada: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    viewModel_cordenadas: viewModel_principal_geinz_work = viewModel(),
    abrir_mapa: (String) -> Unit,
) {
    val _lugares_turisticos by viewmodel_lugares_turisticos._lugares_turisticos.observeAsState(
        emptyList()
    )

    val state_lugares_turisticos by viewmodel_lugares_turisticos.stata_lugares_turisticos.collectAsState()
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var buttom_mapa by remember { mutableStateOf(false) }
    var Box_mostrar_vacio by remember { mutableStateOf(false) }
    var texto_vacio_error by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewmodel_lugares_turisticos.lugares_turisticos(localidad_selecionada)
    }

    LaunchedEffect(_lugares_turisticos) {
        if (_lugares_turisticos.isNotEmpty()) {
            viewmodel_lugares_turisticos.todos_lugares(_lugares_turisticos)
        }
    }
    val listState = rememberLazyListState()

    val showLeftShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val showRightShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < total - 1
        }
    }

    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaLeft"
    )
    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaRight"
    )
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    val state = state_lugares_turisticos
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        when (state) {
            is viewModel_lugares_turisticos.carga_lugares_turisticos.loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is viewModel_lugares_turisticos.carga_lugares_turisticos.succes -> {
                val lista_con_todos = listOf("Todos") + state.lista_categoria
                val lista_original = state.lista_lugares

                val lista_filtrada = remember(subCategoriaSeleccionada, lista_original) {
                    if (subCategoriaSeleccionada == "Todos") lista_original
                    else lista_original.filter { lugar ->
                        lugar.subcategoria_filtrado.any {
                            it.equals(subCategoriaSeleccionada, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        texto_generico_multilinea(
                            "Lugares en ${localidad_selecionada.capitalizeFirst()}",
                            style = MaterialTheme.typography.banerGeinzWork,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }

                    item {
                        texto_generico_multilinea(
                            "Explora los lugares más emblemáticos y atractivos de $localidad_selecionada. Conoce su historia, horarios, recomendaciones y cómo llegar para disfrutar al máximo tu visita.",
                            MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Chips de categorías
                    item {
                        Box(
                            modifier = Modifier
                                .height(45.dp)
                                .fillMaxWidth()
                        ) {
                            LazyRow(
                                state = listState,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(lista_con_todos) { subcategoria ->
                                    val seleccionado = subCategoriaSeleccionada == subcategoria
                                    chisp_filtrado_busqueda(
                                        carta_selecionada = seleccionado,
                                        filtrado = subcategoria.capitalizeFirst(),
                                        btn_visible = false,
                                        clik_card = {
                                            if (!seleccionado) {
                                                subCategoriaSeleccionada = subcategoria
                                                buttom_mapa = subcategoria != "Todos"
                                            }
                                        },
                                        onClick_delete = {}
                                    )
                                }
                            }

                            // Sombras laterales
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(40.dp)
                                        .align(Alignment.CenterStart)
                                        .zIndex(1f)
                                        .alpha(alphaLeft)
                                        .background(Brush.horizontalGradient(colors = shadow_left))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(40.dp)
                                        .align(Alignment.CenterEnd)
                                        .zIndex(1f)
                                        .alpha(alphaRight)
                                        .background(Brush.horizontalGradient(colors = shadow_right))
                                )
                            }
                        }
                    }

                    if (lista_filtrada.isNotEmpty()) {
                        // 🟩 Mostrar lugares
                        items(lista_filtrada) { lugar ->
                            carta_lugares_turisticosa(
                                alto = 200.dp,
                                rounder = 10,
                                lugar = lugar
                            )
                        }
                    } else {
                        // 🟥 Mostrar mensaje vacío centrado pero sin tapar los chips
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.8f) // ocupa el espacio restante visible
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                texto_generico_multilinea(
                                    "No hay lugares disponibles en esta categoría 😕",
                                    style = MaterialTheme.typography.bodyMedium,
                                    Color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }


            is viewModel_lugares_turisticos.carga_lugares_turisticos.error -> {
                texto_generico_multilinea(
                    "Error al cargar lugares turísticos.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
                texto_vacio_error=state.txt
                Box_mostrar_vacio=true
            }

            is viewModel_lugares_turisticos.carga_lugares_turisticos.empty -> {
                texto_vacio_error=state.txt
                Box_mostrar_vacio=true
                texto_generico_multilinea(
                    "No se encontraron lugares turísticos en esta zona.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // 🟦 Botón mapa flotante
        AnimatedVisibility(buttom_mapa) {
            open_map_perzonlizado(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                "turismo",
                abrir_mapa
            )
        }

        // 🟦 Fade inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
                .graphicsLayer { alpha = alphaAnim }
        )

        if(Box_mostrar_vacio){

        }
    }



}

@Composable
fun carta_lugares_turisticosa(alto: Dp, rounder: Int, lugar: lugares_turisticos) {
    var mostrar_dialog by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Box(
        modifier = Modifier
            .width(screenWidth)
            .height(alto)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(lugar.img_principal)
                .size(screenWidth.value.toInt(), alto.value.toInt())
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(screenWidth)
                .height(alto)
                .clip(RoundedCornerShape(rounder))
                .clickable {
                    mostrar_dialog = true
                },

            contentScale = ContentScale.Crop
        )
        mascara_img(rounder, alto, screenWidth)
        texto_generico_one_line(
            lugar.titulo, MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        )
    }
    if (mostrar_dialog) {
        bottom_sheet_lugares_turisticos(lugar, mostrar_dialog,{ mostrar_dialog = false })
    }
}
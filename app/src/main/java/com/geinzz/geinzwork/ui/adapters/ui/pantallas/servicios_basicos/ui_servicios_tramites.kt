package com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.algolia.search.dsl.attributes.DSLSearchableAttributes
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_servicios_tramite
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad.filtrado_texfiel
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite.carga_servicios

@Composable
fun ui_servicio_tramite(localida: String) {
    val viewmode_servicios_tramite: viewmode_servicios_tramite = viewModel()
    val lugares by viewmode_servicios_tramite.lugares.observeAsState(emptyList())
    val lista_servicios = constantes_lista_localidades.lista_fitlrado_servicios_basicos
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var dialog_servicos_tramite by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf(dataclass_lugares_db()) }
    var expandedIndex by remember { mutableStateOf(-1) }
    var lista_mostrar by remember { mutableStateOf<List<dataclass_lugares_db>>(emptyList()) }
    var lista_base_seguridad by rememberSaveable { mutableStateOf(emptyList<dataclass_lugares_db>()) }
    val state_servicios = viewmode_servicios_tramite._state_servicios.collectAsState(carga_servicios.loading).value
    var valor_filtrado by rememberSaveable { mutableStateOf("") }
    var listState = rememberLazyListState()
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

    var yaInicializado by remember { mutableStateOf(false) }

//    LaunchedEffect(lugares) {
//        if (!yaInicializado && lugares.isNotEmpty()) {
//            yaInicializado = true
//            viewmode_servicios_tramite.todos(lugares)
//            lista_base_seguridad = lugares
//        }
//    }
    LaunchedEffect(lugares) {
        if (!yaInicializado && lugares.isNotEmpty()) {
            yaInicializado = true
            lista_base_seguridad = lugares
            viewmode_servicios_tramite.todos(lugares)
        }
    }


    val context=LocalContext.current

    LaunchedEffect(Unit) {
        viewmode_servicios_tramite.obtener_lugares(context,localida)

    }
    LaunchedEffect(valor_filtrado) {
        if (yaInicializado) {
            if (valor_filtrado.isNotEmpty()) {
                viewmode_servicios_tramite.filtrar_nombre_categoria(
                    valor_filtrado,
                    subCategoriaSeleccionada,
                    lista_base_seguridad
                )
                Log.d("actuliazmos_lugares","${ lista_base_seguridad}")
            } else {
                viewmode_servicios_tramite.filtrar_por_categoria(context, subCategoriaSeleccionada)
            }
        }
    }

    LaunchedEffect(subCategoriaSeleccionada) {
        valor_filtrado=""
    }

    LaunchedEffect(lugares, subCategoriaSeleccionada) {
        if (yaInicializado && lugares.isNotEmpty() && subCategoriaSeleccionada != "Todos") {
            viewmode_servicios_tramite.filtrar_por_categoria(context, subCategoriaSeleccionada)
        }else{
            viewmode_servicios_tramite.mostar_lista_completa(subCategoriaSeleccionada)
        }
    }



    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    var progress_bar by remember { mutableStateOf(false) }
    var sin_resultados by remember { mutableStateOf(false) }
    var texto_error_empity by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {

                Column {
                    cabezero_servicios_tramites(localida)
                    spacer_vertical(10.dp)

                    filtrado_texfiel(valor_filtrado) { valor_filtrado = it }

                    spacer_vertical(10.dp)

                    // 🔹 Chips de categorías
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyRow(
                            state = listState,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(constantes_lista_localidades.lista_fitlrado_servicios_basicos) { it ->
                                val catSeleccionada = subCategoriaSeleccionada == it
                                chisp_filtrado_busqueda(catSeleccionada, it, false, {
                                    if (!catSeleccionada) {
                                        subCategoriaSeleccionada = it
                                    }
                                }, {})
                            }
                        }

                        // 👉 sombras izquierda y derecha (decorativas)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(40.dp)
                                .align(Alignment.CenterStart)
                                .zIndex(1f)
                                .alpha(alphaLeft)
                                .background(Brush.horizontalGradient(colors = shadow_left))
                        )

                        // 👉 derecha
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

                    spacer_vertical(10.dp)


                }
            }

            // 🔹 Contenido según el estado del ViewModel
            when (state_servicios) {
                is viewmode_servicios_tramite.carga_servicios.loading -> {
                    Log.d("entramos","cargando")
                    item(span = StaggeredGridItemSpan.FullLine) {
                        progress_bar=true
                        sin_resultados=false

                    }
                }

                is viewmode_servicios_tramite.carga_servicios.succes -> {
                    Log.d("entramos","succes")
                    progress_bar=false
                    sin_resultados=false
                    val lista =
                        (state_servicios as viewmode_servicios_tramite.carga_servicios.succes).items
                    itemsIndexed(lista, key = { _, item -> item.id }) { index, lugar ->
                        carta_servicio_tramites(
                            lugar,
                            index,
                            false
                        ) {
                            seleccionado = lugar
                            dialog_servicos_tramite = true
                        }
                    }
                }

                is viewmode_servicios_tramite.carga_servicios.emoty -> {
                    Log.d("entramos","vacio")
                    progress_bar=false
                    sin_resultados=true
                    val texto =
                        (state_servicios as viewmode_servicios_tramite.carga_servicios.emoty).texto
                    texto_error_empity=texto

                }

                is viewmode_servicios_tramite.carga_servicios.error -> {
                    Log.d("entramos","error")
                    progress_bar=false
                    sin_resultados=true
                    val texto =
                        (state_servicios as viewmode_servicios_tramite.carga_servicios.error).texto
                    texto_error_empity=texto

                }
            }
        }


        val estadoActual = when {
            progress_bar -> "loading"
            sin_resultados -> "empty"
            else -> "none"
        }

        AnimatedContent(
            targetState = estadoActual,
            label = "estado_pantalla",

            ) { estado ->
            centrado_hori_vertical {
                when (estado) {
                    "loading" -> CircularProgressIndicator()
                    "empty" -> Text(
                        texto_error_empity,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                    "none" -> {}
                }
            }
        }

        // 🔹 Diálogo de detalle
        if (dialog_servicos_tramite && seleccionado != null) {
            dialog_servicios_tramite(
                localida,
                ondimis = { dialog_servicos_tramite = false },
                seleccionado!!
            )
        }

        // 🔹 Sombra inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .graphicsLayer { alpha = 0.4f }
        )

    }

}

@Composable
fun centrado_hori_vertical(
    content: @Composable () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 100.dp), // controlas el margen visual
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.3f)) // sube el indicador ~30% del espacio disponible
        content()
        Spacer(modifier = Modifier.weight(0.1f))
    }
}

@Composable
fun carta_servicio_tramites(
    dataclass_lugares_db: dataclass_lugares_db,
    index: Int,
    isExpanded: Boolean,
    click_card: () -> Unit
) {
    val heightOptions = listOf(200.dp, 210.dp)
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]

    val gradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.95f),
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(20.dp))
            .clickable { click_card() }
    ) {
        // 🖼 Imagen principal
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(dataclass_lugares_db.logo_img)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = {
                Image(
                    painter = painterResource(R.drawable.cargando_img_categorias),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )



        AnimatedVisibility(
            visible = isExpanded,
            modifier = Modifier
                .matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clip(RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    texto_generico_one_line("Detalles del servicio")
                    spacer_vertical(5.dp)
                    texto_generico_one_line(dataclass_lugares_db.lugar_nombre.capitalizeFirst())
                }
            }
        }


    }
}

@Composable
fun cabezero_servicios_tramites(localiad: String) {
    Text(text = "servicios esenciales y tramites", fontFamily = baners_geinz_work, fontSize = 30.sp)
    spacer_vertical(5.dp)
    texto_generico_multilinea(
        "Accede al instante a todos los servicios y trámites esenciales de $localiad. Información verificada.",
        MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 10.dp)
    )

}
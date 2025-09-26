package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda


import Item
import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.daclass_filtrado_ui.dataclass_filtrado_ui
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub_lista_cat
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_close_gris
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textos_titulos_geinz_wokr
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.SearchViewModel
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ui_pantalla_busqueda(
    localida_defauld: datos_principales_user,
    viewModelFiltros: viewModel_filtado_tiendas,
    focusRequester: FocusRequester,
    mostrar: () -> Unit,
    ocultar: () -> Unit,
    estado_mostar: Boolean,
    estado_ocultar: Boolean
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val viewModel: SearchViewModel = viewModel()
    val context = LocalContext.current
    val ls_items_ls_cat by viewModel.ls_items_ls_cat.collectAsState()
    val items = ls_items_ls_cat.first   // Lista<Item>
    val categorias = ls_items_ls_cat.second // Lista<String> de categorías

    val categoria_filtrado by viewModelFiltros._subcategoria_filtrado.observeAsState()

    var subcategira_filtrado by rememberSaveable { mutableStateOf("") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val scope = rememberCoroutineScope()
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val horario_por_tienda by viewModelFiltros.estadoTiendas.observeAsState()
    var subir_btn by remember { mutableStateOf(false) }
    var show_bottom_sheeet by remember { mutableStateOf(false) }

    val ultimaLocalidad by data_store_localidad
        .obtener_localidad(context)
        .collectAsState(initial = null)

    var tiendaLocalidadSeleccionada by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ultimaLocalidad) {
        if (ultimaLocalidad != null) {
            tiendaLocalidadSeleccionada = ultimaLocalidad
        }
    }

    var categoria_filtrad by remember { mutableStateOf("") }
    var estadoColor by remember { mutableStateOf(Color.Gray) }
    var id_tienda_selecionada by remember { mutableStateOf("") }
    var firstLaunch by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var expandedFloatingMenuFadeDemo by remember { mutableStateOf(false) }
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    var subcategorias by remember { mutableStateOf<List<String>>(emptyList()) }

    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }

    var cat_sub_seleciondo by remember { mutableStateOf(false) }

    var placeholder by remember { mutableStateOf("A dónde quieres ir?") }

//    LaunchedEffect(tiendaLocalidadSeleccionada, categoria_filtrad, subcategira_filtrado) {
//        Log.d(
//            "LaunchedEffect12313",
//            "🔄 Triggered con localidad=$tiendaLocalidadSeleccionada, categoria=$categoria_filtrad, sub=$subcategira_filtrado, firstLaunch=$firstLaunch"
//        )
//
//        if (firstLaunch) {
//            Log.d("LaunchedEffect12313", "⏳ Primera vez, no filtramos todavía")
//            firstLaunch = false
//            return@LaunchedEffect
//        } else {
//            Log.d("LaunchedEffect12313", "✅ Cambiamos filtros -> llamamos a filtar_sub_cat")
//            viewModel.filtar_sub_cat(
//                tiendaLocalidadSeleccionada ?: "barranca",
//                categoria_filtrad,
//                subcategira_filtrado
//            )
//        }
//    }

    LaunchedEffect(tiendaLocalidadSeleccionada, categoria_filtrad, subcategira_filtrado) {
        if (firstLaunch) {
            firstLaunch = false
            return@LaunchedEffect
        }

        // 🔹 Reset del search
        searchText = TextFieldValue("")

        // 🔹 Placeholder dinámico
        placeholder = if (categoria_filtrad.isNotEmpty() || subcategira_filtrado.isNotEmpty()) {
            "Ingresa el nombre"
        } else {
            "A dónde quieres ir?"
        }

        // 🔹 Llamar solo una vez
        if (categoria_filtrad.isNotEmpty() || subcategira_filtrado.isNotEmpty()) {
            viewModel.filtar_sub_cat(
                tiendaLocalidadSeleccionada ?: "barranca",
                categoria_filtrad,
                subcategira_filtrado
            )
        }
    }

    LaunchedEffect(categoria_filtrad) {
        subcategorias = viewModelFiltros.obtener_lista_sub(categoria_filtrad)
    }

    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                tiendaLocalidadSeleccionada ?: "barranca",
                id_tienda_selecionada
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModelFiltros.obtener_categorias()
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }

//    LaunchedEffect(categoria_filtrad, subcategira_filtrado) {
//        searchText = TextFieldValue("")
//        if (categoria_filtrad.isNotEmpty() || subcategira_filtrado.isNotEmpty()) {
//            placeholder= "Ingresa el nombre"
//            viewModel.filtar_sub_cat(
//                tiendaLocalidadSeleccionada ?: "barranca",
//                categoria_filtrad,
//                subcategira_filtrado
//            )
//        }else{
//            placeholder= "A dónde quieres ir?"
//        }
//    }

    Box() {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    fraces_filtrado(expandedFloatingMenuFadeDemo)
                    spacer_vertical(10.dp)

                    TexfielFiltrado(placeholder, focusRequester, searchText) { it ->
                        searchText = TextFieldValue(
                            text = it,
                            selection = TextRange(it.length)
                        )

                        if (it.isNotEmpty() && it.length >= 2) {
                            ocultar()
                            if (!cat_sub_seleciondo) {
                                viewModel.ls_items_ls_cat_fun(
                                    false,
                                    tiendaLocalidadSeleccionada ?: "barranca",
                                    null,
                                    null,
                                    searchText.text
                                )
                            } else {
                                viewModel.ls_items_ls_cat_fun(
                                    true,
                                    tiendaLocalidadSeleccionada ?: "barranca",
                                    categoria_filtrad,
                                    subcategira_filtrado,
                                    searchText.text
                                )
                            }
                            subir_btn = false
                        } else {
                            subir_btn = true
                            mostrar()
                        }
                    }


                    spacer_vertical(5.dp)

                    filtrado_chips(
                        viewModel,
                        searchText = searchText.text,
                        lista_filtrado = categorias,
                        lista_subcategoria = subcategorias,
                        categoria_selecionada = categoria_filtrad,
                        categoria_selecionada_fun = { filtrado_Select ->
                            categoria_filtrad = filtrado_Select
                        },
                        subcategoria_selecionada = subcategira_filtrado,
                        subcateogira_selecionada_fun = { filtrado_subcategoria_select ->
                            subcategira_filtrado = filtrado_subcategoria_select
                        }, cat_sub_select = { hay_selecccion ->
                            cat_sub_seleciondo = hay_selecccion
                        })
                    spacer_vertical(5.dp)
                }
            }


            itemsIndexed(items) { index, item ->
                ramdoBox(
                    horario_por_tienda,
                    item,
                    index,
                    { id, localidad, color ->
                        estadoColor = color
                        tiendaLocalidadSeleccionada = localidad
                        id_tienda_selecionada = id
                        viewModelFiltros.obtenerHorarioPorTienda_activa(localidad, id)
                        show_bottom_sheeet = true
                    },
                    abrir_gogle_map = { lat, log ->
                        dialog_Crear_ruta = true
                        latitud = lat
                        longitud = log
                    }
                )
            }
        }



        if (dialog_Crear_ruta) {
            dialog_crear_ruta_lugares({ dialog_Crear_ruta = false }, { crear_ruta ->
                dialog_Crear_ruta = false
                if (crear_ruta && verificarUbiActiva(context)) {
                    constantes_lista_localidades.abrir_google_maps(
                        context, latitud, longitud,
                    ) { dialogo ->
                        validacion_mostrar_dialog_ubi_off = dialogo
                    }
                } else {
                    validacion_mostrar_dialog_ubi_off = true
                }
            })
        }

        if (validacion_mostrar_dialog_ubi_off) {
            dialog_sin_ubi__rutas(
                { validacion_mostrar_dialog_ubi_off = false },
                {
                    validacion_mostrar_dialog_ubi_off = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
        }

        if (show_bottom_sheeet) {
            bottom_sheet_tiendas_filtradas(
                estadoColor,
                viewModelFiltros,
                dataclass_tienda_seleccionada, show_bottom_sheeet
            ) {
                show_bottom_sheeet = false
            }
        }

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
        FloatingBubble(
            viewModelFiltros = viewModelFiltros,
            categoria_filtrado = categoria_filtrado,
            subir_btn = subir_btn,
            expanded = expandedFloatingMenuFadeDemo,
            onClick = {
                scope.launch {
                    if (!estado_mostar) {
                        expandedFloatingMenuFadeDemo = !expandedFloatingMenuFadeDemo
                    } else {
                        ocultar()
                        delay(400)
                        expandedFloatingMenuFadeDemo = !expandedFloatingMenuFadeDemo

                    }
                }
            },
            expanded_fun = { expandedFloatingMenuFadeDemo = false },
            localidad_selecionada = tiendaLocalidadSeleccionada ?: "barranca",
            localidad_filtrado = { localidad ->
                tiendaLocalidadSeleccionada = localidad
//                scope.launch {
//                    viewModel.search(
//                        query = searchText.text,
//                        subcategoria_selecionada = subcategoria_selecionada,
//                        localidad = localidad
//                    )
//                }
            },
            categoria_filtrad,
            categoria_Selecionada = { categoria ->
                categoria_filtrad = categoria
            },
            subcategira_filtrado,
            subcategoria_selecionada = { subcategoria_select ->
                subcategira_filtrado = subcategoria_select

            })
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FloatingBubble(
    viewModelFiltros: viewModel_filtado_tiendas,
    categoria_filtrado: List<dataclass_cat_sub_lista_cat>?,
    subir_btn: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    expanded_fun: () -> Unit,
    localidad_selecionada: String,
    localidad_filtrado: (String) -> Unit,
    categoria_filtrad: String,
    categoria_Selecionada: (String) -> Unit,
    subcategira_filtrado: String,
    subcategoria_selecionada: (String) -> Unit
) {
    Log.d("minitosvalor", subir_btn.toString())
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val bubbleSizePx = with(density) { 60.dp.toPx() }
    val paddingPx = with(density) { 16.dp.toPx() }
    var categorias_filtrado_res by remember {
        mutableStateOf<List<dataclass_cat_sub_lista_cat>>(
            emptyList()
        )
    }
    val subctegorias by viewModelFiltros._obtener_subacategoria.observeAsState()

    var subcategoira_filtrado_res by remember { mutableStateOf<List<String>>(emptyList()) }
    var filtros by remember { mutableStateOf(dataclass_filtrado_ui()) }


    val icono: Int? = if (!expanded) {
        R.drawable.icono_filtrado_webp
    } else {
        null
    }

    LaunchedEffect(categoria_filtrado) {
        categoria_filtrado?.let {
            Log.d("categoria_filtrado", it.toString())
            categorias_filtrado_res = it
        }
    }
    LaunchedEffect(categoria_filtrad) {
        viewModelFiltros.obtener_subcategoiras(categoria_filtrad)
    }
    LaunchedEffect(subctegorias) {
        val listaSoloSubcategorias = subctegorias
            ?.flatMap { it.subcategorias }

        listaSoloSubcategorias?.let {
            Log.d("categoria_filtrado", it.toString())
            subcategoira_filtrado_res = it
        }
    }

    var expandedIndex by remember { mutableStateOf(-1) }

    val weightBox1 by animateFloatAsState(
        targetValue = when (expandedIndex) {
            0 -> 0.7f
            1 -> 0.3f
            else -> 0.5f
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    val weightBox2 by animateFloatAsState(
        targetValue = when (expandedIndex) {
            0 -> 0.3f
            1 -> 0.7f
            else -> 0.5f
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )


    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        val offsetX = remember { Animatable(screenWidth - bubbleSizePx - paddingPx) }
        val offsetY = remember { Animatable(screenHeight - bubbleSizePx - paddingPx) }


        val maxY = if (subir_btn) {
            screenHeight - bubbleSizePx - paddingPx - with(density) { 80.dp.toPx() }
        } else {
            screenHeight - bubbleSizePx - paddingPx
        }

        LaunchedEffect(subir_btn) {
            if (offsetY.value > maxY) {
                offsetY.animateTo(
                    maxY,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        }

        val animatedColor by animateColorAsState(
            targetValue = if (!expanded) MaterialTheme.colorScheme.primary else Color.Transparent,
            animationSpec = tween(600, easing = FastOutSlowInEasing),
            label = "colorAnim"
        )

        var color_categoria by remember { mutableStateOf(false) }
        var color_localidad by remember { mutableStateOf(false) }
        var color_subcategoria by remember { mutableStateOf(false) }
        val lista_localidades = constantes_lista_localidades.lista_localidad
        val icono_expandido = if (expandedIndex == 0) {
            Icons.Default.ExpandLess
        } else {
            Icons.Default.ExpandMore
        }
        val icono_expandido2 = if (expandedIndex == 1) {
            Icons.Default.ExpandMore
        } else {
            Icons.Default.ExpandLess
        }
        val mostrarChipCategoria = remember { mutableStateOf(filtros.categoria.isNotEmpty()) }

        val mostrarChipsubcategoria = remember { mutableStateOf(filtros.subcategoria.isNotEmpty()) }
        if (categoria_filtrad.isNotEmpty()) {
            mostrarChipCategoria.value = true
        } else {
            mostrarChipCategoria.value = false
        }

        if (subcategira_filtrado.isNotEmpty()) {
            mostrarChipsubcategoria.value = true
        } else {
            mostrarChipsubcategoria.value = false
        }

        val backgroundColor_categorias by animateColorAsState(
            targetValue = if (!color_categoria)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant,
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            ), label = ""
        )

        val startTopColor_categorias by animateColorAsState(
            targetValue = if (!color_categoria) shadow_top_filtrado_v1[0] else shadow_top_filtrado_v2[0],
            animationSpec = tween(500), label = ""
        )
        val endTopColor_categorias by animateColorAsState(
            targetValue = if (!color_categoria) shadow_top_filtrado_v1[1] else shadow_top_filtrado_v2[1],
            animationSpec = tween(500), label = ""
        )

        val startBottomColor_categorias by animateColorAsState(
            targetValue = if (!color_categoria) shadow_botonm_filtrado_v1[0] else shadow_botonm_filtrado_v2[0],
            animationSpec = tween(500), label = ""
        )
        val endBottomColor_categorias by animateColorAsState(
            targetValue = if (!color_categoria) shadow_botonm_filtrado_v1[1] else shadow_botonm_filtrado_v2[1],
            animationSpec = tween(500), label = ""
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                .size(60.dp)
                .clip(CircleShape)
                .background(animatedColor)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = (offsetX.value + dragAmount.x)
                                .coerceIn(paddingPx, screenWidth - bubbleSizePx - paddingPx)
                            val newY = (offsetY.value + dragAmount.y)
                                .coerceIn(paddingPx, maxY)
                            scope.launch {
                                offsetX.snapTo(newX)
                                offsetY.snapTo(newY)
                            }
                        },
                        onDragEnd = {
                            val middle = screenWidth / 2
                            val targetX = if (offsetX.value < middle) {
                                paddingPx
                            } else {
                                screenWidth - bubbleSizePx - paddingPx
                            }
                            scope.launch {
                                offsetX.animateTo(
                                    targetX,
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = icono,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                label = "crossfadeIcon"
            ) { currentIcon ->
                currentIcon?.let {
                    Image(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(it),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        expanded_fun()
                    }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 25.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.25f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {}
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(15.dp)
                    ) {
                        Column {
                            texto_generico_one_line(
                                "Filtra tu búsqueda",
                                MaterialTheme.typography.headlineSmall
                            )
                            spacer_vertical(10.dp)
                            texto_generico_multilinea(
                                "Elige una categoría y explora desde playas y museos hasta tiendas y restaurantes locales.",
                                MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                            spacer_vertical(15.dp)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,


                                ) {
                                filtros.localidad.let {
                                    item {
                                        AnimatedVisibility(
                                            it.isNotEmpty(),
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            chisp_filtrado_busqueda(
                                                color_localidad,
                                                it,
                                                false,
                                                clik_card = {
                                                    color_localidad = !color_localidad
                                                    color_categoria = false
                                                    color_subcategoria = false
                                                },
                                                onClick_delete = {
                                                    color_localidad = false
                                                    filtros = filtros.copy(localidad = "")
                                                })
                                        }
                                    }
                                }
                                filtros.categoria.let { categoria ->
                                    item {
                                        AnimatedVisibility(
                                            visible = mostrarChipCategoria.value,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            chisp_filtrado_busqueda(
                                                color_categoria,
                                                categoria_filtrad.ifEmpty { categoria },
                                                clik_card = {
                                                    color_categoria = !color_categoria
                                                    color_localidad = false
                                                    color_subcategoria = false
                                                },
                                                onClick_delete = {
                                                    color_subcategoria = false
                                                    color_categoria = false
                                                    categoria_Selecionada("")
                                                    subcategoria_selecionada("")
                                                    mostrarChipCategoria.value = false
                                                    mostrarChipsubcategoria.value = false


                                                }
                                            )
                                        }
                                    }
                                }
                                filtros.subcategoria.let {
                                    item {
                                        AnimatedVisibility(
                                            mostrarChipsubcategoria.value,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            chisp_filtrado_busqueda(
                                                color_subcategoria,
                                                subcategira_filtrado.ifEmpty { it },
                                                clik_card = {
                                                    color_subcategoria = !color_subcategoria
                                                    color_localidad = false
                                                    color_categoria = false
                                                },
                                                onClick_delete = {
                                                    subcategoria_selecionada("")
                                                    color_subcategoria = false
                                                    mostrarChipsubcategoria.value = false
                                                })
                                        }

                                    }
                                }
                            }

                        }
                        btn_close_gris(
                            modifier = Modifier.align(Alignment.TopEnd),
                            Icons.Default.Close,
                            onClick = {
                                expanded_fun()
                            }
                        )
                    }
                    spacer_vertical(10.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.8f), // 90%
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(.5f)   // ocupa 50% del ancho
                                .clip(RoundedCornerShape(30.dp))
                                .background(backgroundColor_categorias)
                                .padding(8.dp)
                                .fillMaxHeight()
                        ) {
                            val listState = rememberLazyListState()
                            val showTopShadow by remember {
                                derivedStateOf { listState.firstVisibleItemScrollOffset > 0 }
                            }
                            val showBottomShadow by remember {
                                derivedStateOf {
                                    val lastVisible =
                                        listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                    val totalItems = listState.layoutInfo.totalItemsCount
                                    lastVisible != null && lastVisible < totalItems - 1
                                }
                            }
                            val selectedCategoria = categoria_filtrad

                            this@Row.AnimatedVisibility(true) {
                                Crossfade(
                                    targetState = categorias_filtrado_res.isNotEmpty(),
                                    label = ""
                                ) { tieneCategorias ->
                                    if (tieneCategorias) {
                                        LazyColumn(
                                            state = listState,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            item {
                                                texto_generico_one_line(
                                                    "Categoria",
                                                    MaterialTheme.typography.titleMedium
                                                )
                                                spacer_vertical(5.dp)
                                            }

                                            items(categorias_filtrado_res) { i ->
                                                val isSelected =
                                                    selectedCategoria.equals(
                                                        i.nombre_cat,
                                                        ignoreCase = true
                                                    )

                                                AnimatedFabItem(
                                                    text = simplificarCategoria(i.nombre_cat),
                                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.primary,
                                                    visible = expanded
                                                ) {
                                                    categoria_Selecionada(i.nombre_cat)
                                                    mostrarChipCategoria.value = true
                                                    mostrarChipsubcategoria.value = false
                                                    filtros = filtros.copy(categoria = i.nombre_cat)
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Cargando categorías",
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                spacer_vertical(15.dp)
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(35.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            this@Row.AnimatedVisibility(
                                showTopShadow,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.TopCenter)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    startTopColor_categorias,
                                                    endTopColor_categorias
                                                ),
                                                startY = 0f,
                                                endY = 200f
                                            )
                                        )
                                )
                            }
                            this@Row.AnimatedVisibility(
                                showBottomShadow, enter = fadeIn(), exit = fadeOut(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.BottomCenter)
                            ) {

                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    startBottomColor_categorias,
                                                    endBottomColor_categorias
                                                ),
                                                startY = 0f,
                                                endY = 200f
                                            )
                                        )
                                )
                            }


                        }


                        // Columna derecha: dos cuadros apilados verticalmente
                        Column(
                            modifier = Modifier
                                .weight(.5f) // ocupa 50% del ancho
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // ------- LOCALIDAD -------
                            val listStateLocalidad = rememberLazyListState()
                            val showTopShadowLocalidad by remember {
                                derivedStateOf { listStateLocalidad.firstVisibleItemScrollOffset > 0 }
                            }
                            val showBottomShadowLocalidad by remember {
                                derivedStateOf {
                                    val lastVisible =
                                        listStateLocalidad.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                    val totalItems = listStateLocalidad.layoutInfo.totalItemsCount
                                    lastVisible != null && lastVisible < totalItems - 1
                                }
                            }
                            val backgroundColor_localidad by animateColorAsState(
                                targetValue = if (!color_localidad)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = tween(
                                    durationMillis = 500, // velocidad (medio segundo)
                                    easing = LinearOutSlowInEasing
                                ), label = ""
                            )
                            val startTopColor_localidad by animateColorAsState(
                                targetValue = if (!color_localidad) shadow_top_filtrado_v1[0] else shadow_top_filtrado_v2[0],
                                animationSpec = tween(500), label = ""
                            )
                            val endTopColor_localidad by animateColorAsState(
                                targetValue = if (!color_localidad) shadow_top_filtrado_v1[1] else shadow_top_filtrado_v2[1],
                                animationSpec = tween(500), label = ""
                            )
                            val startBottomColor_localidad by animateColorAsState(
                                targetValue = if (!color_localidad) shadow_botonm_filtrado_v1[0] else shadow_botonm_filtrado_v2[0],
                                animationSpec = tween(500), label = ""
                            )
                            val endBottomColor_localidad by animateColorAsState(
                                targetValue = if (!color_localidad) shadow_botonm_filtrado_v1[1] else shadow_botonm_filtrado_v2[1],
                                animationSpec = tween(500), label = ""
                            )

                            BoxWithConstraints(
                                modifier = Modifier
                                    .weight(weightBox1)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(backgroundColor_localidad)
                                    .padding(8.dp)
                            ) {
                                LazyColumn(
                                    state = listStateLocalidad,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        texto_generico_one_line(
                                            "Localidad",
                                            MaterialTheme.typography.titleMedium
                                        )
                                        spacer_vertical(5.dp)
                                    }

                                    items(lista_localidades) { i ->
                                        val colorSeleccionado =
                                            if (localidad_selecionada.equals(i, ignoreCase = true))
                                                Color.Black
                                            else
                                                MaterialTheme.colorScheme.primary

                                        AnimatedFabItem(
                                            text = i.capitalizeFirst(),
                                            color = colorSeleccionado,
                                            visible = expanded
                                        ) {
                                            localidad_filtrado(i)
                                            filtros = filtros.copy(localidad = i)
                                        }
                                        filtros = filtros.copy(localidad = localidad_selecionada)
                                    }
                                }

                                this@Row.AnimatedVisibility(
                                    showTopShadowLocalidad,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        startTopColor_localidad,
                                                        endTopColor_localidad
                                                    ), startY = 0f,
                                                    endY = 200f
                                                )
                                            )
                                    )
                                }

                                this@Row.AnimatedVisibility(
                                    showBottomShadowLocalidad,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        startBottomColor_localidad,
                                                        endBottomColor_localidad
                                                    ),
                                                    startY = 0f,
                                                    endY = 200f
                                                )
                                            )
                                    )
                                }

                                btn_close_gris(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    icono_expandido,
                                    onClick = {
                                        expandedIndex = if (expandedIndex == 0) -1 else 0
                                    }
                                )
                            }

                            // ------- SUBCATEGORÍAS -------
                            val listStateSub = rememberLazyListState()
                            val showTopShadowSub by remember {
                                derivedStateOf { listStateSub.firstVisibleItemScrollOffset > 0 }
                            }
                            val showBottomShadowSub by remember {
                                derivedStateOf {
                                    val lastVisible =
                                        listStateSub.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                    val totalItems = listStateSub.layoutInfo.totalItemsCount
                                    lastVisible != null && lastVisible < totalItems - 1
                                }
                            }
                            val backgroundColor by animateColorAsState(
                                targetValue = if (!color_subcategoria)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = tween(
                                    durationMillis = 500, // velocidad (medio segundo)
                                    easing = LinearOutSlowInEasing
                                ), label = ""
                            )
                            val startTopColor by animateColorAsState(
                                targetValue = if (!color_subcategoria) shadow_top_filtrado_v1[0] else shadow_top_filtrado_v2[0],
                                animationSpec = tween(500), label = ""
                            )
                            val endTopColor by animateColorAsState(
                                targetValue = if (!color_subcategoria) shadow_top_filtrado_v1[1] else shadow_top_filtrado_v2[1],
                                animationSpec = tween(500), label = ""
                            )
                            val startBottomColor by animateColorAsState(
                                targetValue = if (!color_subcategoria) shadow_botonm_filtrado_v1[0] else shadow_botonm_filtrado_v2[0],
                                animationSpec = tween(500), label = ""
                            )
                            val endBottomColor by animateColorAsState(
                                targetValue = if (!color_subcategoria) shadow_botonm_filtrado_v1[1] else shadow_botonm_filtrado_v2[1],
                                animationSpec = tween(500), label = ""
                            )
                            val subcategoria_select = subcategira_filtrado

                            BoxWithConstraints(
                                modifier = Modifier
                                    .weight(weightBox2)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(backgroundColor)
                                    .padding(8.dp)
                            ) {
                                this@Row.AnimatedVisibility(true) {
                                    Crossfade(
                                        targetState = subcategoira_filtrado_res.isNotEmpty(),
                                        label = ""
                                    ) { tiene_categria ->
                                        if (tiene_categria) {
                                            LazyColumn(
                                                state = listStateSub,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                contentPadding = PaddingValues(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                ),
                                                modifier = Modifier.fillMaxSize()

                                            ) {
                                                item {
                                                    texto_generico_one_line(
                                                        "Subcategoría",
                                                        MaterialTheme.typography.titleMedium
                                                    )
                                                    spacer_vertical(5.dp)
                                                }

                                                items(subcategoira_filtrado_res) { sub ->
                                                    val isSelected =
                                                        subcategoria_select.equals(
                                                            sub,
                                                            ignoreCase = true
                                                        )

                                                    AnimatedFabItem(
                                                        text = sub.capitalizeFirst(),
                                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.primary,
                                                        visible = expanded
                                                    ) {
                                                        subcategoria_selecionada(sub)
                                                        filtros = filtros.copy(subcategoria = sub)
                                                        mostrarChipsubcategoria.value = true
                                                    }
                                                }

                                            }
                                        } else {
                                            this@Column.AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(),
                                                exit = fadeOut()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Selecciona una categoría",
                                                        textAlign = TextAlign.Center,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )

                                                }
                                            }
                                        }
                                    }
                                }

                                this@Row.AnimatedVisibility(
                                    showTopShadowSub,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(startTopColor, endTopColor),
                                                    startY = 0f,
                                                    endY = 200f
                                                )
                                            )
                                    )
                                }

                                this@Row.AnimatedVisibility(
                                    showBottomShadowSub,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        startBottomColor,
                                                        endBottomColor
                                                    ),
                                                    startY = 0f,
                                                    endY = 200f
                                                )
                                            )
                                    )
                                }
                                btn_close_gris(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    icono_expandido2,
                                    onClick = {
                                        expandedIndex = if (expandedIndex == 1) -1 else 1
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun filtrado_chips(
    viewModel: SearchViewModel,
    searchText: String,
    lista_filtrado: List<String>,
    lista_subcategoria: List<String>,
    categoria_selecionada: String,
    categoria_selecionada_fun: (String) -> Unit,
    subcategoria_selecionada: String,
    subcateogira_selecionada_fun: (String) -> Unit,
    cat_sub_select: (Boolean) -> Unit
) {
    // ✅ Unicidad
    val categoriasUnicas = lista_filtrado.distinct()
    val subcategoriasUnicas = lista_subcategoria.distinct()

    val hayCategoria = categoria_selecionada.isNotEmpty()
    val haySubcategoria = subcategoria_selecionada.isNotEmpty()
    val haySeleccion = hayCategoria || haySubcategoria

    cat_sub_select(haySeleccion)

    // ✅ Si hay categoría seleccionada, solo dejamos esa en la lista
    val categoriasFiltradas = if (hayCategoria) {
        listOf(categoria_selecionada)
    } else {
        categoriasUnicas
    }

    if (searchText.isNotEmpty() && !haySeleccion) {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ✅ Mostrar categorías únicas
            items(categoriasFiltradas) { cat ->
                val catSeleccionada = categoria_selecionada == cat
                chisp_filtrado_busqueda(
                    carta_selecionada = catSeleccionada,
                    filtrado = cat,
                    btn_visible = true,
                    clik_card = { categoria_selecionada_fun(cat) },
                    onClick_delete = { categoria_selecionada_fun("")
                        viewModel.clearResults()
                    }
                )
            }

            // ✅ Mostrar subcategorías únicas
            items(subcategoriasUnicas) { sub ->
                val subSeleccionada = subcategoria_selecionada == sub
                chisp_filtrado_busqueda(
                    carta_selecionada = subSeleccionada,
                    filtrado = sub.capitalizeFirst(),
                    btn_visible = true,
                    clik_card = { subcateogira_selecionada_fun(sub) },
                    onClick_delete = { subcateogira_selecionada_fun("") }
                )
            }
        }
    } else if (searchText.isEmpty() && !haySeleccion) {
        texto_generico_one_line("mostramos chips clásicos")
    } else {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categoriasFiltradas) { cat ->
                val catSeleccionada = categoria_selecionada == cat
                chisp_filtrado_busqueda(
                    carta_selecionada = catSeleccionada,
                    filtrado = cat,
                    btn_visible = true,
                    clik_card = { categoria_selecionada_fun(cat) },
                    onClick_delete = { categoria_selecionada_fun("")
                        viewModel.clearResults()}
                )
            }

            // ✅ Mostrar subcategorías
            items(subcategoriasUnicas) { sub ->
                val subSeleccionada = subcategoria_selecionada == sub
                chisp_filtrado_busqueda(
                    carta_selecionada = subSeleccionada,
                    filtrado = sub.capitalizeFirst(),
                    btn_visible = true,
                    clik_card = { subcateogira_selecionada_fun(sub) },
                    onClick_delete = { subcateogira_selecionada_fun("") }
                )
            }
        }
    }
}


@Composable
fun fraces_filtrado(expandedFloatingMenuFadeDemo: Boolean) {
    val fraces = constantes_lista_localidades.lista_frases_busqueda
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(expandedFloatingMenuFadeDemo) {
        if (!expandedFloatingMenuFadeDemo) {
            while (true) {
                delay(4000L)
                index = (index + 1) % fraces.size
            }
        }
    }
    Crossfade(fraces[index], label = "fraces") { txt ->
        texto_generico_one_line(
            texto = txt,
            MaterialTheme.typography.busquedaGeinzWork
        )
    }
}


@Composable
fun TexfielFiltrado(
    placeholder: String,
    focusRequester: FocusRequester,
    texto: TextFieldValue,
    onvalueChage: (String) -> Unit
) {
    var icono_borrar by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = texto,
        onValueChange = { newValue: TextFieldValue ->
            icono_borrar = newValue.text.isNotBlank()
            onvalueChage(newValue.text)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "buscar",
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (icono_borrar) {
                IconButton(onClick = {
                    onvalueChage("")
                    icono_borrar = false
                }) {
                    Icon(
                        painter = painterResource(R.drawable.vector_eliminar_texto_texfiel),
                        contentDescription = "borrar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        shape = RoundedCornerShape(50)
    )

    Spacer(modifier = Modifier.height(8.dp))

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}


@Composable
fun ramdoBox(
    estado_tienda: Map<String, Boolean>?,
    i: Item,
    index: Int,
    listener_carta: (String, String, Color) -> Unit,
    abrir_gogle_map: (Double, Double) -> Unit
) {
    val heightOptions = listOf(300.dp, 350.dp)
    val estado_tienda_filter = estado_tienda?.get(i.id_tienda) == true
    Log.d("estado_tienda", estado_tienda_filter.toString())
    var Estado_color = if (estado_tienda_filter) Color.Green else Color.Red
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]
    val iconCategoria = constantes_lista_localidades.getCategoriaIcon(i.categoria)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.7f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(i.img)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { listener_carta(i.id_tienda, i.lugar, Estado_color) },
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF262626) // este es tu negro exacto
                                ),
                                startY = 0f,
                                endY = 200f
                            )
                        )
                )

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp, bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = i.nombre,
                        fontFamily = textos_titulos_geinz_wokr,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        onClick = { abrir_gogle_map(i.latitud, i.longitud) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "centrar",
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.localidad_icon_general),
                        contentDescription = "",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 5.dp)
                    )
                    texto_generico_one_line(
                        i.lugar.capitalizeFirst(),
                        MaterialTheme.typography.bodyMedium
                    )
                }
                spacer_vertical(5.dp)
                texto_generico_one_line(
                    "$iconCategoria ${i.categoria}",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                tags_subcateogiras(i.lista)
            }
        }
    }
}

@Composable
fun AnimatedFabItem(
    text: String,
    color: Color,
    visible: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 500),
        label = "buttonColorAnim"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
    ) {
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(containerColor = animatedColor)
        ) {
            Text(
                text,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}





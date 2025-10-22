package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.daclass_filtrado_ui.dataclass_filtrado_ui
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub_lista_cat
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_close_gris
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.cat_sub_seguirar_salud
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.geohashing
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerUbicacion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.SearchViewModel
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope", "MissingPermission")
@Composable
fun FloatingBubble(
    cerca_de_ti_enable: Boolean,
    geohashin: String?,
    color_categoria: Boolean,
    color_localidad: Boolean,
    color_subcategoria: Boolean,
    color_salud_seguridad: Boolean,
    seguidad_salud: String,
    viewModel: SearchViewModel,
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
    subcategoria_selecionada: (String) -> Unit,
    seguridad_salud_selec: (String) -> Unit,
    click_carta_localidad: () -> Unit,
    click_carta_localidad_delete: () -> Unit,
    click_carta_categoria: () -> Unit,
    click_carta_categoria_delete: () -> Unit,
    click_carta_seguridad: () -> Unit,
    click_carta_seguridad_delete: () -> Unit,
    click_carta_subcategoria: () -> Unit,
    click_carta_subcategoria_delete: () -> Unit,
    click_salud_general: () -> Unit,
    tiene_categorias: () -> Unit,
    filtrado_cerca_de_ti: (Float, String) -> Unit,
    fun_cerca_de_ti_enable: (Boolean) -> Unit
) {
    Log.d("minitosvalor", subir_btn.toString())
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

    var mostar_carga_subcategorias by remember { mutableStateOf(false) }


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
        Log.d("selecian odasmo", subctegorias.toString())
        mostar_carga_subcategorias = true
        val listaSoloSubcategorias = subctegorias?.flatMap { it.subcategorias }
        listaSoloSubcategorias?.let {
            Log.d("categoria_filtrado", it.toString())
            subcategoira_filtrado_res = it
        }
        delay(500)
        mostar_carga_subcategorias = false
    }

    val state_subcategoria: viewModel_filtado_tiendas.carga_subcategorias = when {
        mostar_carga_subcategorias -> viewModel_filtado_tiendas.carga_subcategorias.Loading
        subcategoira_filtrado_res.isNotEmpty() -> viewModel_filtado_tiendas.carga_subcategorias.loaded(
            subcategoira_filtrado_res
        )

        else -> viewModel_filtado_tiendas.carga_subcategorias.Empty
    }


    var expandedIndex by remember { mutableStateOf(-1) }

    val weightBox1 by animateFloatAsState(
        targetValue = when (expandedIndex) {
            0 -> 0.26f
            1 -> 0.3f
            else -> 0.3f
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

    val animatedColor by animateColorAsState(
        targetValue = if (!expanded) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "colorAnim"
    )
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

    val mostrar_chip_salud_seguridad =
        remember { mutableStateOf(filtros.salud_seguridad.isNotEmpty()) }

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

    if (seguidad_salud.isNotEmpty()) {
        mostrar_chip_salud_seguridad.value = true
    } else {
        mostrar_chip_salud_seguridad.value = false

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

    val backgrpound_salud_seguridad by animateColorAsState(
        targetValue = if (!color_salud_seguridad)
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
        val screenHeight12 = LocalConfiguration.current.screenHeightDp.dp

        LaunchedEffect(subir_btn) {
            if (offsetY.value > maxY) {
                offsetY.animateTo(
                    maxY,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        }

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
        val screenHeight123 = LocalConfiguration.current.screenHeightDp.dp
        val topPadding = screenHeight123 * 0.1f

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 25.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = topPadding), // ocupa toda la pantalla
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(15.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        val (_, dy) = dragAmount
                                        if (dy > 0) {
                                            expanded_fun()
                                        }
                                    }
                                }
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
                                    // Localidad
                                    if (filtros.localidad.isNotEmpty()) {
                                        item {
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(),
                                                exit = fadeOut()
                                            ) {
                                                chisp_filtrado_busqueda(
                                                    color_localidad,
                                                    filtros.localidad,
                                                    false,
                                                    clik_card = {
                                                        click_carta_localidad()
                                                    },
                                                    onClick_delete = {
                                                        click_carta_localidad_delete()
                                                        filtros = filtros.copy(localidad = "")
                                                    }
                                                )
                                            }
                                        }
                                    }

// Categoria
                                    if (mostrarChipCategoria.value) {
                                        item {
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(),
                                                exit = fadeOut()
                                            ) {
                                                chisp_filtrado_busqueda(
                                                    color_categoria,
                                                    categoria_filtrad.ifEmpty { filtros.categoria },
                                                    clik_card = {
                                                        click_carta_categoria()

                                                    },
                                                    onClick_delete = {
                                                        click_carta_categoria_delete()
                                                        categoria_Selecionada("")
                                                        subcategoria_selecionada("")
                                                        mostrarChipCategoria.value = false
                                                        mostrarChipsubcategoria.value = false
                                                        viewModel.clearResults()
                                                    }
                                                )
                                            }
                                        }
                                    }

// Subcategoria
                                    if (mostrarChipsubcategoria.value) {
                                        item {
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(),
                                                exit = fadeOut()
                                            ) {
                                                chisp_filtrado_busqueda(
                                                    color_subcategoria,
                                                    subcategira_filtrado.ifEmpty { filtros.subcategoria },
                                                    clik_card = {
                                                        click_carta_subcategoria()

                                                    },
                                                    onClick_delete = {
                                                        click_carta_subcategoria_delete()
                                                        subcategoria_selecionada("")
                                                        mostrarChipsubcategoria.value = false
                                                    }
                                                )
                                            }
                                        }
                                    }


                                    if (mostrar_chip_salud_seguridad.value) {
                                        item {
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(),
                                                exit = fadeOut()
                                            ) {
                                                chisp_filtrado_busqueda(
                                                    color_salud_seguridad,
                                                    seguidad_salud.ifEmpty { filtros.salud_seguridad },
                                                    clik_card = {
                                                        click_carta_seguridad()
                                                    },
                                                    onClick_delete = {
                                                        Log.d("elimoasno_valo", "dealte")
                                                        seguridad_salud_selec("")
                                                        categoria_Selecionada("")
                                                        subcategoria_selecionada("")
                                                        click_carta_seguridad_delete()
                                                        mostrarChipsubcategoria.value = false
                                                        mostrarChipCategoria.value = false
                                                        mostrar_chip_salud_seguridad.value = false
                                                    }
                                                )
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
                    }

                    item {
                        val childScrollState = rememberLazyListState()
                        val nestedScrollConnection = remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    // Devuelve cero vertical si el hijo puede moverse en esa dirección
                                    return when {
                                        available.y < 0 && childScrollState.firstVisibleItemIndex + childScrollState.firstVisibleItemScrollOffset <
                                                childScrollState.layoutInfo.totalItemsCount -> Offset(
                                            0f,
                                            available.y
                                        )

                                        available.y > 0 && (childScrollState.firstVisibleItemIndex > 0 ||
                                                childScrollState.firstVisibleItemScrollOffset > 0) -> Offset(
                                            0f,
                                            available.y
                                        )

                                        else -> Offset.Zero
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight12 * 0.80f) // 80% de la pantalla
                        ) {

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(.50f)   // ocupa 50% del ancho
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(backgroundColor_categorias)
                                            .padding(8.dp)
                                            .weight(.4f)
                                    ) {
                                        val listState = rememberLazyListState()
                                        val showTopShadow by remember {
                                            derivedStateOf { listState.firstVisibleItemScrollOffset > 0 }
                                        }
                                        val showBottomShadow by remember {
                                            derivedStateOf {
                                                val lastVisible =
                                                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                                val totalItems =
                                                    listState.layoutInfo.totalItemsCount
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

                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .nestedScroll(nestedScrollConnection)
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
                                                                subcategoria_selecionada("")
                                                                seguridad_salud_selec("")
                                                                mostrar_chip_salud_seguridad.value =
                                                                    false
                                                                mostrarChipCategoria.value = true
                                                                mostrarChipsubcategoria.value =
                                                                    false
                                                                mostar_carga_subcategorias = true
                                                                tiene_categorias()

                                                                filtros =
                                                                    filtros.copy(categoria = i.nombre_cat)
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
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(
                                                                startTopColor_categorias,
                                                                endTopColor_categorias
                                                            ),

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
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(
                                                                startBottomColor_categorias,
                                                                endBottomColor_categorias
                                                            ),

                                                            )
                                                    )
                                            )
                                        }
                                    }
                                    spacer_vertical(10.dp)
                                    var expandedIndex by remember { mutableStateOf(-1) }
                                    val weigh_lugares_inters by animateFloatAsState(
                                        targetValue = when (expandedIndex) {
                                            0 -> 0.20f
                                            1 -> 0.25f
                                            else -> 0.13f
                                        },
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    )
                                    apartado_lugares_interes(
                                        color_salud_seguridad,
                                        expandedIndex = expandedIndex,
                                        texto = "Seguridad y salud",
                                        lista_subcategoria = cat_sub_seguirar_salud,
                                        enColumna = true,
                                        modifier = Modifier
                                            .weight(weigh_lugares_inters)
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(backgrpound_salud_seguridad),
                                        expandir_clik = {
                                            expandedIndex = if (expandedIndex == 0) -1 else 0
                                        },
                                        cat_sub_selection = seguidad_salud,
                                        cat_sub_clik = { i ->
                                            seguridad_salud_selec(i)
                                            filtros = filtros.copy(salud_seguridad = i)
                                            mostrar_chip_salud_seguridad.value = true
                                            mostrarChipsubcategoria.value = false
                                            mostrarChipCategoria.value = false
                                            click_salud_general()
                                            categoria_Selecionada("")
                                            subcategoria_selecionada("")
                                        })
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
                                            val totalItems =
                                                listStateLocalidad.layoutInfo.totalItemsCount
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
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .nestedScroll(nestedScrollConnection)
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
                                                    if (localidad_selecionada.equals(
                                                            i,
                                                            ignoreCase = true
                                                        )
                                                    )
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
                                                filtros =
                                                    filtros.copy(localidad = localidad_selecionada)
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
                                                            ),
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
                                                when (state_subcategoria) {
                                                    is viewModel_filtado_tiendas.carga_subcategorias.Loading -> {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(5.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(
                                                                text = "Cargando subcategorías",
                                                                textAlign = TextAlign.Center,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = MaterialTheme.colorScheme.onBackground
                                                            )
                                                            spacer_vertical(15.dp)
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(
                                                                    35.dp
                                                                )
                                                            )
                                                        }
                                                    }

                                                    is viewModel_filtado_tiendas.carga_subcategorias.loaded -> {
                                                        if (tiene_categria) {
                                                            LazyColumn(
                                                                state = listStateSub,
                                                                verticalArrangement = Arrangement.spacedBy(
                                                                    8.dp
                                                                ),
                                                                contentPadding = PaddingValues(
                                                                    horizontal = 16.dp,
                                                                    vertical = 8.dp
                                                                ),
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .nestedScroll(
                                                                        nestedScrollConnection
                                                                    )
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
                                                                        text = sub.replaceFirstChar { it.uppercase() },
                                                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.primary,
                                                                        visible = expanded
                                                                    ) {
                                                                        subcategoria_selecionada(sub)
                                                                        filtros =
                                                                            filtros.copy(
                                                                                subcategoria = sub
                                                                            )
                                                                        mostrarChipsubcategoria.value =
                                                                            true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    is viewModel_filtado_tiendas.carga_subcategorias.Empty -> {
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
                                                            colors = listOf(
                                                                startTopColor,
                                                                endTopColor
                                                            ),

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
                        spacer_vertical(10.dp)
                    }

                    item {
                        val context = LocalContext.current
                        val scope = rememberCoroutineScope()


                        val radioGuardado by data_store_localidad.get_radio_user(context)
                            .collectAsState(initial = 1f)

                        // 👇 este estado se actualiza automáticamente cuando cambia el valor guardado
                        var radioActual by remember { mutableStateOf(1f) }

                        // Cuando el valor de DataStore cambia, actualizamos el radioActual (solo si el usuario no está moviendo el slider)
                        LaunchedEffect(radioGuardado) {
                            radioActual = radioGuardado
                        }

                        var enable_cerca by remember { mutableStateOf(false) }
                        enable_cerca =
                            categoria_filtrad.isNotEmpty() || subcategira_filtrado.isNotEmpty()


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(15.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    texto_generico_one_line(
                                        "Cerca de ti",
                                        MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = cerca_de_ti_enable,
                                        onCheckedChange = { fun_cerca_de_ti_enable(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White
                                        )
                                    )
                                }

                                spacer_vertical(10.dp)

                                texto_generico_multilinea(
                                    "Explora lugares, tiendas y servicios que están cerca de tu ubicación. Encuentra lo que necesitas sin perder tiempo.",
                                    MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 20.dp)
                                )

                                spacer_vertical(15.dp)
                                if (cerca_de_ti_enable) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp), contentAlignment = Alignment.Center
                                    ) {
                                        AnimatedContent(
                                            targetState = enable_cerca,
                                            transitionSpec = {
                                                fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                                            },
                                            label = "animacion_cerca"
                                        ) { habilitado ->
                                            if (habilitado) {
                                                Slider(
                                                    enabled = enable_cerca,
                                                    value = radioActual,
                                                    onValueChange = {
                                                        radioActual = it.roundToInt().toFloat()
                                                    },
                                                    valueRange = 1f..10f,
                                                    steps = 8,
                                                    onValueChangeFinished = {
                                                        scope.launch {
                                                            data_store_localidad.guardar_radio_user(context, radioActual)
                                                        }
                                                        if (geohashin != null) {
                                                            filtrado_cerca_de_ti(radioActual, geohashin!!)
                                                        } else {
                                                            Log.d("Ubicacion", "❌ Aún no se ha obtenido la ubicación")
                                                        }
                                                    },
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = MaterialTheme.colorScheme.primary,
                                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                        activeTickColor = MaterialTheme.colorScheme.primary,
                                                        inactiveTickColor = Color.Gray
                                                    ),
                                                    thumb = {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(25.dp)
                                                                .clip(CircleShape)
                                                                .background(Color.White),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            texto_generico_one_line(
                                                                radioActual.toInt().toString(),
                                                                color = Color.Black,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(40.dp)
                                                )
                                            } else {
                                                texto_generico_one_line(
                                                    "Selecciona una categoría",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }

                                    }
                                }

                            }
                        }
                    }


                }


            }


        }
    }
}
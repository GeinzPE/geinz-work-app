package com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.geinzz.geinzwork.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.EstadoFiltrosUi
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
//import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.selec_class_estados_carga
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.existencia_dato
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.open_map_perzonlizado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.shadow_bottom_pantallas_generales
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_qr_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_pago_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.firebaseAuth
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.centrado_hori_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularTiempoRestante
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.strat_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Pantalla_filtrado_tiendas(
    viewModelFiltros: viewModel_filtado_tiendas,
    categoria: String,
    localida: String,
    nombre_user: String,
    navigation_regresar: () -> Unit,
    abrir_mapa: (String, String) -> Unit,
    iniciar_normal: () -> Unit,
    con_google: () -> Unit,
    crear_cuenta: () -> Unit,
) {
    val subcategoriaObjs by viewModelFiltros._subcategoiraList.observeAsState(emptyList())
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState(emptyList())
    val estadoTiendaFree by viewModelFiltros._datos_tienda_sin_pago.observeAsState(
        viewModel_filtado_tiendas.carga_tiendas_sin_pago.loading_tiendas_free
    )
    val state_filtrado_tiendas =
        viewModelFiltros._Tiendas_filtradas_por_categoria.collectAsState().value
//    val estado_tiendas by viewModelFiltros.estadoTiendas.observeAsState()
    val lista_filtrada_tiendas by viewModelFiltros.listaTiendasGuardadas.observeAsState(emptyList())

    var primeraCargaCompletada by rememberSaveable { mutableStateOf(false) }
    var mostrandoCargaGlobal by remember { mutableStateOf(true) }

    val estadoFiltrosUi = EstadoFiltrosUi(
        subcategorias = subcategoriaObjs,
        tiendasFiltradas = emptyList()
    )

    var showBottomSheet by remember { mutableStateOf(false) }
    var visible_texfiel by rememberSaveable { mutableStateOf(false) }
    var estadoColor by remember { mutableStateOf(Color.Gray) }
    var existe by remember { mutableStateOf(false) }
    var texto_filtrado by rememberSaveable { mutableStateOf("") }
    var id_tienda_selecionada by remember { mutableStateOf("") }
    var categoria_seleccionda by rememberSaveable { mutableStateOf("") }

    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var dataclass_datos_tienda_free by remember { mutableStateOf(datos_tienda_free()) }

    var lista_subcategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var subCategoriaSeleccionada by rememberSaveable { mutableStateOf("Todos") }

    var btn_mostrar_mapa by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()

    var listaMostrar by remember { mutableStateOf<List<tiendas_por_categoria>>(emptyList()) }
    var bottom_sheet_iniciar_seccion by remember { mutableStateOf(false) }
    var bottom_shet_tienda by remember { mutableStateOf(false) }
    var dialog_tienda_no_pagada by remember { mutableStateOf(false) }
    var tienda_pagada by remember { mutableStateOf(false) }
    var sin_resultados by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error_empity by remember { mutableStateOf(false) }
    var texto_error_empity by remember { mutableStateOf("") }

    var lista_base_seguridad by remember { mutableStateOf(emptyList<tiendas_por_categoria>()) }

    val lista_datos_tiendas by viewModelFiltros._datos__tiendas.observeAsState(emptyList())
    var mostrandoCarga_free by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        primeraCargaCompletada = false
        mostrandoCargaGlobal = true

    }

    LaunchedEffect(texto_filtrado) {
        viewModelFiltros.obtener_filtrado_nombre(
            texto_filtrado,
            subCategoriaSeleccionada,
            lista_base_seguridad
        )

    }
    LaunchedEffect(lista_datos_tiendas) {
        lista_base_seguridad = lista_datos_tiendas
        viewModelFiltros.tiendas_iniciales(lista_datos_tiendas)

    }

    LaunchedEffect(estadoFiltrosUi.subcategorias) {
        val subcategorias: List<String> = estadoFiltrosUi.subcategorias.flatMap { it.subcategorias }
        lista_subcategorias = subcategorias
    }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(localida, id_tienda_selecionada)
        }
    }
    LaunchedEffect(dialog_tienda_no_pagada) {
        if (dialog_tienda_no_pagada) {
            mostrandoCarga_free = true
            viewModelFiltros.obtener_tienda_no_pagada(localida, id_tienda_selecionada)
        }
    }

    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }
    LaunchedEffect(estadoTiendaFree) {
        val estado = estadoTiendaFree  // ✅ Smart cast habilitado
        when (estado) {
            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.loading_tiendas_free -> {
                mostrandoCarga_free = true // 🌀 Empieza a cargar
            }

            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.succes_tiendas_free -> {
                mostrandoCarga_free = false // ✅ Deja de cargar
                dataclass_datos_tienda_free = estado.item
                dialog_tienda_no_pagada = true // 👉 Abre el diálogo
            }

            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.error_tiendas_free -> {
                mostrandoCarga_free = false // ❌ Error, deja de cargar
            }

            else -> Unit
        }
    }

    LaunchedEffect(categoria) {
        Log.d("se_cambio", categoria)
        subCategoriaSeleccionada = "Todos"
        viewModelFiltros.obtener_subcategorias(categoria)
        viewModelFiltros.obtener_tiendas_filtradas(localida, categoria)


    }

    LaunchedEffect(subCategoriaSeleccionada) {
        viewModelFiltros.filtrar_por_subcategoria(subCategoriaSeleccionada)
        texto_filtrado = ""
        btn_mostrar_mapa = when (val estado = state_filtrado_tiendas) {
            is viewModel_filtado_tiendas.carga_tiendas.succes -> {
                // Mostrar mapa solo si NO es "Todos"
                !subCategoriaSeleccionada.equals("Todos", ignoreCase = true)
            }

            else -> false
        }
    }

//    LaunchedEffect(lista_filtrada_tiendas) {
//        viewModelFiltros.actualizarListaFiltrada(lista_filtrada_tiendas)
//    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()

        val bubbleSizePx = with(density) { 60.dp.toPx() }

        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val paddingPx = with(LocalDensity.current) { 16.dp.toPx() }

        val offsetX = remember { Animatable(screenWidth - bubbleSizePx - paddingPx) }
        val offsetY = remember { Animatable(screenHeight - bubbleSizePx - paddingPx) }
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 10.dp)
        ) {
            item { encabezado_chis_categorias() }
            stickyHeader() {
                ColumnContenedorComun {
                    chips_filtrado(
                        listState,
                        subCategoriaSeleccionada,
                        lista_subcategorias,
                        { expandir ->
                            visible_texfiel = expandir
                        },
                        { categoria_selecionada ->
                            categoria_seleccionda = categoria_selecionada
                            subCategoriaSeleccionada = categoria_seleccionda
                        })
                    Text_fiel_filtrado(existe, visible_texfiel, texto_filtrado) {
                        texto_filtrado = it
                    }
                }
            }


            when (state_filtrado_tiendas) {
                is viewModel_filtado_tiendas.carga_tiendas.loading -> {

                    if (primeraCargaCompletada) {
                        mostrandoCargaGlobal = false // loader pequeño
                        isLoading = true
                    } else {
                        mostrandoCargaGlobal = true // overlay global
                        // 🔹 Corrutina que mantiene visible el overlay 4s solo la primera vez
                        scope.launch {
                            delay(4000L)
                            mostrandoCargaGlobal = false
                            primeraCargaCompletada = true
                        }
                    }
                    error_empity = false
                }

                is viewModel_filtado_tiendas.carga_tiendas.error -> {

                    val texto =
                        (state_filtrado_tiendas as viewModel_filtado_tiendas.carga_tiendas.error).texto
                    error_empity = true
                    texto_error_empity = texto
//                    mostrandoCargaGlobal = false
                    primeraCargaCompletada = true
                    isLoading = false
                }

                is viewModel_filtado_tiendas.carga_tiendas.succes -> {
                    val lista =
                        (state_filtrado_tiendas as viewModel_filtado_tiendas.carga_tiendas.succes).items
                    primeraCargaCompletada = true
                    isLoading = false
                    error_empity = false

//
                    val listaOrdenada = lista.sortedWith(
                        compareByDescending<tiendas_por_categoria> { it.pagado }
                            .thenByDescending { it.estaAbierto }
                    )



                    items(listaOrdenada, key = { tienda -> tienda.id_tienda }) { tienda ->
                        item_tiendas(
                            viewModelFiltros,
                            tienda,
                            tienda.horario_dia, tienda.estaAbierto,
                            { id_tienda, listener, estado_color, pagado ->
                                tienda_pagada = pagado
                                estadoColor = estado_color
                                showBottomSheet = listener
                                id_tienda_selecionada = id_tienda
                            })
                    }
                }

                is viewModel_filtado_tiendas.carga_tiendas.empty -> {
                    val texto =
                        (state_filtrado_tiendas as viewModel_filtado_tiendas.carga_tiendas.empty).texto
                    isLoading = false
                    error_empity = true
//                    mostrandoCargaGlobal = false
                    primeraCargaCompletada = true
                    texto_error_empity = texto
                }
            }


        }
        if (btn_mostrar_mapa) {
            open_map_perzonlizado(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                    .zIndex(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()

                                val newX = (offsetX.value + dragAmount.x)
                                    .coerceIn(
                                        paddingPx,
                                        screenWidth - bubbleSizePx - paddingPx
                                    )
                                val newY = (offsetY.value + dragAmount.y)
                                    .coerceIn(
                                        paddingPx,
                                        screenHeight - bubbleSizePx - paddingPx
                                    )

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
                tipo = "tiendas",
                abrir_mapa = { tipo ->
                    abrir_mapa(tipo, localida)
                }
            )
        }

        if (mostrandoCargaGlobal && !primeraCargaCompletada) {
            pantalla_carga_login()
        }

        AnimatedContent(
            targetState = when {
                isLoading -> "loading"
                sin_resultados -> "empty"
                error_empity -> "error"
                else -> "none"
            },
            label = "estado_carga"
        ) { estado ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                centrado_hori_vertical {
                    when (estado) {
                        "loading" -> CircularProgressIndicator()
                        "empty" -> texto_generico_one_line(
                            texto_error_empity,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        "error" -> texto_generico_one_line(
                            texto_error_empity,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        shadow_bottom_pantallas_generales(Modifier.align(Alignment.BottomCenter))
    }


    if (showBottomSheet && firebaseAuth.currentUser != null) {
        if (tienda_pagada) {
            bottom_shet_tienda = true
        } else {
            dialog_tienda_no_pagada = true
            bottom_shet_tienda = false
        }
        bottom_sheet_iniciar_seccion = false
    } else if (showBottomSheet && firebaseAuth.currentUser == null) {
        bottom_shet_tienda = false
        bottom_sheet_iniciar_seccion = true
    }

    if (dialog_tienda_no_pagada) {
        bottom_shet_tienda = false
        showBottomSheet = false
        dialog_sin_pago_tiendas(
            mostrandoCarga_free,
            dataclass_datos_tienda_free,
            ondimis = { dialog_tienda_no_pagada = false })
    }

    if (bottom_shet_tienda) {
        bottom_sheet_tiendas_filtradas(
            estadoColor,
            viewModelFiltros,
            dataclass_tienda_seleccionada, bottom_shet_tienda
        ) {
            bottom_shet_tienda = false
            showBottomSheet = false
        }
    }

    if (bottom_sheet_iniciar_seccion) {
        bottom_sheet_registrate(
            ondimis = {
                bottom_sheet_iniciar_seccion = false
                showBottomSheet = false
            },
            iniciar_seccion_normal = {
                showBottomSheet = false
                iniciar_normal()
                bottom_sheet_iniciar_seccion
            },
            continuar_con_google = {
                showBottomSheet = false
                con_google()
                bottom_sheet_iniciar_seccion
            },
            crear_cuenta_geinz = {
                showBottomSheet = false
                crear_cuenta()
                bottom_sheet_iniciar_seccion
            })
    }
}


@Composable
fun TiempoRestanteCierre(
    horario_total: horario_tienda,
    hCierre: String,
    cerrado: Boolean,
    motivo: String,
    pagado: Boolean,
    tick: Long,
    color: (Color) -> Unit
) {

Log.d("horario_total",horario_total.toString())
    val resultado by remember(horario_total, hCierre, cerrado, motivo, tick) {
        derivedStateOf { calcularTiempoRestante(horario_total, hCierre, cerrado, motivo) }
    }
    if (pagado) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(resultado.color)
            )
            spacer_horizonta(5.dp)
            Text(
                text = resultado.texto.capitalizeFirst(),
                color = resultado.color,
                style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            color(resultado.color)
        }
    } else {
        Text(
            text = "Consultar al negocio",
            color = Color(0xFFA5A5A5),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun encabezado_chis_categorias() {

    titulos_genericos_one_line(
        "Busca tus tiendas favoritas", MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    )
    spacer_vertical(5.dp)
    texto_generico_multilinea(
        "Filtra entre nuestras categorías o busca directamente por el nombre de esa tienda que tanto te gusta. ¡Explorar nunca fue tan fácil y rápido!",
        MaterialTheme.typography.bodyMedium
    )
    spacer_vertical(5.dp)
}

@Composable
fun chips_filtrado(
    listState: LazyListState,
    sub_categoria_selecionada: String?,
    lista_subcategorias: List<String>,
    expandir_carta: (Boolean) -> Unit,
    selecionado: (String) -> Unit
) {
    val lista_con_todos = listOf("Todos") + lista_subcategorias
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp), contentAlignment = Alignment.Center
    ) {
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
        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            items(lista_con_todos) { subcategorias ->
                val selecionado = sub_categoria_selecionada == subcategorias
                chisp_filtrado_busqueda(selecionado, subcategorias, false, clik_card = {
                    if (!selecionado) {
                        expandir_carta(true)
                        if (subcategorias == "Todos") {
                            expandir_carta(false)
                            selecionado("Todos")
                        } else {
                            selecionado(subcategorias)
                        }
                    }
                }, {})
            }
        }
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

//            FilterChip(
//                colors = FilterChipDefaults.filterChipColors(
//                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                    selectedLabelColor = Color.White,
//                    labelColor = Color.White
//                ),
//                modifier = Modifier.padding(horizontal = 4.dp),
//                selected = selecionado,
//                border = if (selecionado) null else BorderStroke(
//                    1.dp,
//                    MaterialTheme.colorScheme.onBackground
//                ),
//                onClick = {
//                    if (!selecionado) {
//                        expandir_carta(true)
//                        if (subcategorias == "Todos") {
//                            expandir_carta(false)
//                            selecionado("Todos")
//                        } else {
//                            selecionado(subcategorias)
//                        }
//                    }
//                },
//                label = {
//                    Text(
//                        text = subcategorias,
//                        color = if (selecionado) Color.White else MaterialTheme.colorScheme.onBackground
//                    )
//                },
//                shape = RoundedCornerShape(40)
//            )
}

@Composable
fun Text_fiel_filtrado(
    existe_texto: Boolean,
    visible_texfiel: Boolean,
    texto_filtrado_txt: String,
    texto_filtrado: (String) -> Unit,
) {
    var icono_busqeuda by remember { mutableStateOf(R.drawable.buscar_icon) }

    AnimatedVisibility(
        visible = visible_texfiel,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column() {
            custom_texFiel(
                value = texto_filtrado_txt,
                onValueChange = {
                    texto_filtrado(it)
                    if (it.isNotBlank()) {
                        icono_busqeuda = R.drawable.vector_eliminar_texto_texfiel
                    } else {
                        icono_busqeuda = R.drawable.buscar_icon
                    }
                },
                labelText = "Ingresa el nombre de la tienda",
                placeholderText = "Ingresa el nombre",
                trailingIcon = {
                    if (icono_busqeuda == R.drawable.vector_eliminar_texto_texfiel) {
                        IconButton(onClick = {
                            texto_filtrado("")
                            icono_busqeuda = R.drawable.buscar_icon
                        }) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = icono_busqeuda),
                                contentDescription = "Eliminar texto"
                            )
                        }
                    } else {
                        androidx.compose.material3.Icon(
                            painter = painterResource(id = icono_busqeuda),
                            contentDescription = "Buscar por subcategoría"
                        )
                    }
                },
                isError = false,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(),
            )
            AnimatedVisibility(existe_texto) {
                existencia_dato()
            }

        }

    }
}


@Composable
fun item_tiendas(
    viewModelFiltros: viewModel_filtado_tiendas,
    item_tiendas: tiendas_por_categoria,
    horario_tienda: horario_tienda,
    abierto_cerrado: Boolean,
    listener_botom_sheet: (id_tienda: String, showBottomSheet: Boolean, estado_color: Color, Boolean) -> Unit
) {
    val tick by viewModelFiltros.tick.collectAsState()
    var detalles_tienda by remember { mutableStateOf(false) }
    var estadoColor by remember { mutableStateOf(Color.Red) }

    var showDialog by remember { mutableStateOf(false) }

    val generador_qr = remember(item_tiendas.latitud, item_tiendas.longitud) {
        generar_qr_cordenadas_tienda.codificarCoordenadas(
            item_tiendas.latitud, item_tiendas.longitud
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showDialog = true
                    },
                    onTap = {
                        listener_botom_sheet(
                            item_tiendas.id_tienda,
                            true,
                            estadoColor,
                            item_tiendas.pagado
                        )
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {

            Row(
                modifier = Modifier.padding(7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item_tiendas.logo_tienda,
                    contentDescription = "Imagen local",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(80.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(15))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Nombre_estado_tienda(item_tiendas.nombre_tienda)
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Direccion :", item_tiendas.direccion)
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Referencia : ", item_tiendas.referencia)
                    Spacer(modifier = Modifier.height(5.dp))
                    tags_subcateogiras(
                        item_tiendas.lista_subcategoiras,
                        brush_start = Brush.horizontalGradient(colors = strat_subcategoria_shadow),
                        brush_end = Brush.horizontalGradient(colors = end_subcategoria_shadow)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    TiempoRestanteCierre(
                        horario_tienda,
                        horario_tienda.h_cierre,
                        horario_tienda.cerrado,
                        horario_tienda.motivo,
                        item_tiendas.pagado,
                        tick
                    ){color->
                        estadoColor=color
                    }

                }
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Btn_Expandir_card { expandir -> detalles_tienda = expandir }
                }
            }
            AnimatedVisibility(visible = detalles_tienda) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Descripcion : ${item_tiendas.descripcion}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )


                }
            }
        }
    }

    if (showDialog) {
        dialog_qr_tienda(
            qr = generador_qr,
            nombre_tienda = item_tiendas.nombre_tienda,
            onDismis = { showDialog = false }
        )
    }

}


@Composable
fun Nombre_estado_tienda(
    nombre_tienda: String,
) {
    texto_generico_one_line(nombre_tienda)
    Spacer(modifier = Modifier.width(10.dp))
}


@Composable
fun Caracteristicas_tiendas(caracteristica: String, texto: String) {
    Row() {
        Text(
            text = caracteristica,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = texto,
            modifier = Modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun Btn_Expandir_card(expandir_carta: (Boolean) -> Unit) {
    var expandida_carta by remember { mutableStateOf(false) }
    FloatingActionButton(
        modifier = Modifier
            .padding(5.dp)
            .size(30.dp)
            .clip(CircleShape),
        onClick = {
            expandida_carta = !expandida_carta
            expandir_carta(expandida_carta)
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Image(
            modifier = Modifier.size(15.dp),
            painter = painterResource(
                constantes_lista_localidades.cambiar_icono_exapndible(
                    expandida_carta
                )
            ),
            contentDescription = "",
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}
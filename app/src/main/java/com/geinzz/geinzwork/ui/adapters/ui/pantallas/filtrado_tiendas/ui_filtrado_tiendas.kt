package com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import com.geinzz.geinzwork.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
//import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.selec_class_estados_carga
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_listener_fv_externo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.existencia_dato
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.open_map_perzonlizado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.shadow_bottom_pantallas_generales
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_eliminar_favoritos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_qr_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_pago_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_ayudanos_a_creccer
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.firebaseAuth
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.centrado_hori_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.mostrar_iconos_pagos
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.strat_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.viewModels.viewModel_favoritos
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas.carga_tiendas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import kotlin.text.isNotEmpty


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Pantalla_filtrado_tiendas(
    id_tienda:String,
    verificar_intener: Boolean,
    viewmodelFavoritos: viewModel_favoritos,
    viewModelFiltros: viewModel_filtado_tiendas,
    categoria: String,
    localida: String,
    nombre_user: String,
    navigation_regresar: () -> Unit,
    abrir_mapa: (String, String) -> Unit,
    iniciar_normal: () -> Unit,
    con_google: () -> Unit,
    crear_cuenta: () -> Unit,
    navController: NavHostController,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
Log.d("cvateogireaopasda","$categoria")
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState(emptyList())
    val estadoTiendaFree by viewModelFiltros._datos_tienda_sin_pago.observeAsState(
        viewModel_filtado_tiendas.carga_tiendas_sin_pago.loading_tiendas_free
    )
    val state_filtrado_tiendas =
        viewModelFiltros._Tiendas_filtradas_por_categoria.collectAsState(carga_tiendas.loading).value

    val categoria_filtrado = viewModelFiltros._subcategoria_lis.collectAsState(emptyList())

    var mostrandoCargaGlobal by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }


    var estadoColor by remember { mutableStateOf(Color.Gray) }
    var existe by remember { mutableStateOf(false) }
    var id_tienda_selecionada by remember {
        mutableStateOf(if (id_tienda.isNotEmpty()) id_tienda else "")
    }
    var categoria_seleccionda by rememberSaveable { mutableStateOf("") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var dataclass_datos_tienda_free by remember { mutableStateOf(datos_tienda_free()) }
    var lista_subcategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var bottom_sheet_iniciar_seccion by remember { mutableStateOf(false) }
    var bottom_shet_tienda by remember { mutableStateOf(false) }
    var dialog_tienda_no_pagada by remember { mutableStateOf(false) }
    var tienda_pagada by remember { mutableStateOf(false) }
    var sin_resultados by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error_empity by remember { mutableStateOf(false) }

    var mostar_bottom_sheet_ayuda_geinz by remember { mutableStateOf(false) }
    val ultimaLocalidad by data_store_localidad.obtener_localidad(context)
        .collectAsState(initial = null)
    var texto_error_empity by remember { mutableStateOf("") }
    var lista_base_seguridad by remember { mutableStateOf(emptyList<tiendas_por_categoria>()) }
    val lista_datos_tiendas by viewModelFiltros._datos__tiendas.observeAsState(emptyList())
    var mostrandoCarga_free by remember { mutableStateOf(false) }
    var yaInicializado by remember { mutableStateOf(false) }
    val subCategoriaSeleccionada by viewModelFiltros.subcategoriaFiltrado.collectAsState()
    val texto_filtrado by viewModelFiltros.txtNombreFiltrado.collectAsState()
    val btn_mostrar_mapa by remember(subCategoriaSeleccionada, state_filtrado_tiendas) {
        derivedStateOf {
            val hayTiendas = state_filtrado_tiendas is carga_tiendas.succes &&
                    (state_filtrado_tiendas as carga_tiendas.succes).items.isNotEmpty()

            subCategoriaSeleccionada != "Todos" && hayTiendas
        }
    }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    var id_respado_user by remember { mutableStateOf("") }
//
    var texto_falta_registra by remember { mutableStateOf("") }

    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            id_respado_user = uid_respald_user
            Log.d("UID_DataStore", "✅ Recuperado UID válido desde DataStore: $id_respado_user")
        } else {
            id_respado_user = ""
        }
    }

    var visibleTextField by remember { mutableStateOf(false) }
    var habiaTiendasAntes by remember { mutableStateOf(false) }

    LaunchedEffect(subCategoriaSeleccionada, state_filtrado_tiendas) {
        val tiendasActuales = if (state_filtrado_tiendas is carga_tiendas.succes)
            (state_filtrado_tiendas as carga_tiendas.succes).items
        else
            emptyList()

        val hayTiendas = tiendasActuales.isNotEmpty()

        delay(150)

        visibleTextField = subCategoriaSeleccionada != "Todos" && (hayTiendas || habiaTiendasAntes)

        habiaTiendasAntes = hayTiendas || habiaTiendasAntes
    }


    var primeraVez by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        mostrandoCargaGlobal = true

        // Si NO hay filtros activos → cargamos toda la lista desde cero
        if (subCategoriaSeleccionada == "Todos" && texto_filtrado.isEmpty()) {
            viewModelFiltros.obtener_tiendas_filtradas(localida, categoria)
        } else {
            // Si hay filtros → aplicamos filtrado
            viewModelFiltros.aplicarFiltrosAlRegresar()
        }

        // Siempre actualizamos las subcategorías
        viewModelFiltros.get_subcategorias_sola(categoria)
    }

    BackHandler {
        navigation_regresar()
    }
    LaunchedEffect(texto_filtrado) {
        if (texto_filtrado.isNotEmpty()) {
            Log.d("texto_filtrado", texto_filtrado)
            // Solo filtrar si hay texto
            viewModelFiltros.obtener_filtrado_nombre(
                texto_filtrado,
                subCategoriaSeleccionada,
                viewModelFiltros._lista_base_seguridad.value
            )
        } else {
            // Si se borró el texto, volver al estado anterior
            viewModelFiltros.aplicarFiltrosAlRegresar()
        }

        viewModelFiltros.actualizarNombre(texto_filtrado)
    }

    LaunchedEffect(categoria_filtrado.value) {
        if (categoria_filtrado.value.isNotEmpty()) {
            lista_subcategorias = categoria_filtrado.value
        }
    }
    LaunchedEffect(lista_datos_tiendas) {
        Log.w("ingresadmo", "saldieo de mapa")

        if (!yaInicializado && lista_datos_tiendas.isNotEmpty()) {
            yaInicializado = true
            lista_base_seguridad = lista_datos_tiendas
            viewModelFiltros.tiendas_iniciales(lista_datos_tiendas)

        }
    }
    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }
    LaunchedEffect(subCategoriaSeleccionada) {
        if (primeraVez) {
            primeraVez = false
            return@LaunchedEffect
        }
        viewModelFiltros.actualizarNombre("")

        if (yaInicializado && lista_datos_tiendas.isNotEmpty() && subCategoriaSeleccionada != "Todos") {
            viewModelFiltros.filtrar_por_subcategoria(subCategoriaSeleccionada, lista_datos_tiendas)
            viewModelFiltros.actualizarsubcategoria_filtrado(subCategoriaSeleccionada)
        } else {
            viewModelFiltros.lista_completa_inicial(subCategoriaSeleccionada)

        }
    }
    LaunchedEffect(estadoTiendaFree) {
        val estado = estadoTiendaFree
        when (estado) {
            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.loading_tiendas_free -> {
                mostrandoCarga_free = true
            }

            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.succes_tiendas_free -> {
                mostrandoCarga_free = false
                dataclass_datos_tienda_free = estado.item
                dialog_tienda_no_pagada = true
            }

            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.error_tiendas_free -> {
                mostrandoCarga_free = false
            }

            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.empty_tiendas_free -> {
                dialog_tienda_no_pagada = false
            }


            else -> Unit
        }
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
    val horarios by viewModelFiltros.horariosTiendas_real.collectAsState()

    LaunchedEffect(Unit) {
        viewModelFiltros.iniciarEscucha(
            localidad = localida,
            categoria = categoria
        )
    }

    LaunchedEffect(id_tienda) {
        Log.d("LaunchedEffect_ID", "ID recibido: $id_tienda")

        if (id_tienda.isNotEmpty()) {
            Log.d("LaunchedEffect_ID", "ID no vacío, mostrando bottom sheet")
            try {
                delay(5000L)
                showBottomSheet=true
                bottom_shet_tienda=true
            } catch (e: Exception) {
                Log.e("LaunchedEffect_ID", "Error obteniendo datos del lugar turístico", e)
            }
        } else {
            Log.d("LaunchedEffect_ID", "ID vacío, no se hace nada")
        }
    }


    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
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
//                            visible_texfiel = expandir
                        },
                        { categoria_selecionada ->
                            categoria_seleccionda = categoria_selecionada
//                            subCategoriaSeleccionada = categoria_seleccionda
                            viewModelFiltros.actualizarsubcategoria_filtrado(categoria_seleccionda)
                        })
                    Text_fiel_filtrado(existe, visibleTextField, texto_filtrado) {
                        viewModelFiltros.actualizarNombre(it)
//                        texto_filtrado = it
                    }
                }
            }


            when (state_filtrado_tiendas) {
                is carga_tiendas.loading -> {
                    Log.d("entramos", "loading")
                    mostrandoCargaGlobal = true
                    error_empity = false
                    isLoading = true
                }

                is carga_tiendas.error -> {
                    Log.d("entramos", "error")
                    val texto = (state_filtrado_tiendas as carga_tiendas.error).texto
                    error_empity = true
                    texto_error_empity = texto
                    scope.launch {
                        delay(4000)
                        mostrandoCargaGlobal = false

                    }
                    isLoading = false
                }

                is carga_tiendas.succes -> {
                    Log.d("entramos", "sucecs")
                    val lista = (state_filtrado_tiendas as carga_tiendas.succes).items

                    if (lista.isNotEmpty()) {
                        scope.launch {
                            delay(4000)
                            mostrandoCargaGlobal = false
                        }

                        isLoading = false
                        error_empity = false
                    }


                    val listaOrdenada = lista.sortedWith(
                        compareByDescending<tiendas_por_categoria> { it.pagado }
                            .thenByDescending { it.estaAbierto }
                    )

                    items(listaOrdenada, key = { tienda -> tienda.id_tienda }) { tienda ->

                        val horarioDeEstaTienda = horarios[tienda.id_tienda] ?: HorarioDia_box()

                        item_tiendas(
                            horario_box1 = horarioDeEstaTienda,
                            horario_box = tienda.horario_tienda_box,
                            verificar_interner = verificar_intener,
                            localidad_user = localida,
                            id_user = uid_respald_user,
                            viewModelFiltros = viewModelFiltros,
                            item_tiendas = tienda,
                            abierto_cerrado = tienda.estaAbierto,
                            listener_botom_sheet = { id_tienda, listener, estado_color, pagado ->
                                if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {
                                    estadoColor = estado_color
                                    id_tienda_selecionada = id_tienda
                                    if (pagado) {
                                        bottom_shet_tienda = true
                                        showBottomSheet = listener
                                    } else {
                                        dialog_tienda_no_pagada = true
                                    }
                                } else {
                                    bottom_sheet_iniciar_seccion = true
                                    texto_falta_registra =
                                        "Regístrate para ver los detalles completos y las funciones exclusivas"

                                }
                            }, dialog_sin_registrao = {
                                bottom_sheet_iniciar_seccion = true
                                texto_falta_registra = "Regístrate para agregar a tus favoritos"
                            })
                    }
                }
                is carga_tiendas.empty -> {
                    Log.d("entramos", "vacio")
                    val texto =
                        (state_filtrado_tiendas as carga_tiendas.empty).texto
                    scope.launch {
                        delay(4000)
                        mostrandoCargaGlobal = false
                    }

                    isLoading = false
                    error_empity = true
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

        if (mostrandoCargaGlobal) {
            Log.d("entramos", "global sii")
            AnimatedVisibility(
                visible = mostrandoCargaGlobal,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    pantalla_carga_login(false)
                }
            }
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
                        "loading" ->{}
                        "empty" -> {
                            Column() {
                                texto_generico_one_line(
                                    texto_error_empity,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(10.dp)


                            }
                        }

                        "error" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                texto_generico_one_line(
                                    texto_error_empity,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(10.dp)

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            mostar_bottom_sheet_ayuda_geinz = true
                                        }
                                ) {
                                    texto_generico_one_line(
                                        "¿Conoces alguno?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )


                                }
                            }

                        }
                    }
                }
            }
        }
        if(!mostrandoCargaGlobal){
        shadow_bottom_pantallas_generales(Modifier.align(Alignment.BottomCenter))
        }
    }




    if (mostar_bottom_sheet_ayuda_geinz) {
        bottom_sheet_ayudanos_a_creccer(
            verificar_intener,
            ultimaLocalidad ?: "barranca",
            { mostar_bottom_sheet_ayuda_geinz = false }, viewModelFiltros
        )
    }
    if (dialog_tienda_no_pagada) {
        bottom_shet_tienda = false
        showBottomSheet = false
        dialog_sin_pago_tiendas(
            mostrandoCarga_free = mostrandoCarga_free,
            datos_tienda_free = dataclass_datos_tienda_free,
            ondimis = {
                dialog_tienda_no_pagada = false
                viewModelFiltros.resetear_estado_sin_pago()
            })
    }

    AnimatedVisibility(visible = bottom_shet_tienda) {
        bottom_sheet_tiendas_filtradas(
            verificar_intener,
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
            crear_cuenta_geinz = {
                showBottomSheet = false
                crear_cuenta()
                bottom_sheet_iniciar_seccion
            },
            texto_bottom_Sheet = texto_falta_registra
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
    selecionado: (String) -> Unit,
    color_left: List<Color> = shadow_left,
    color_right: List<Color> = shadow_right,
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
                .background(Brush.horizontalGradient(colors = color_left))
        )

        // 👉 derecha
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .alpha(alphaRight)
                .background(Brush.horizontalGradient(colors = color_right))
        )
    }

}

@Composable
fun Text_fiel_filtrado(
    existe_texto: Boolean,
    visible_texfiel: Boolean,
    texto_filtrado_txt: String,
    texto_filtrado: (String) -> Unit,
) {
    var icono_busqeuda by remember { mutableStateOf(R.drawable.buscar_icon) }

    LaunchedEffect(texto_filtrado_txt) {
        icono_busqeuda = if (texto_filtrado_txt.isNotBlank()) {
            R.drawable.vector_eliminar_texto_texfiel
        } else {
            R.drawable.buscar_icon
        }
    }
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
                    icono_busqeuda = if (it.isNotBlank()) {
                        R.drawable.vector_eliminar_texto_texfiel
                    } else {
                        R.drawable.buscar_icon
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun item_tiendas(
    horario_box1: HorarioDia_box,
    horario_box: HorarioAtencion_box,
    verificar_interner: Boolean,
    localidad_user: String,
    id_user: String,
    viewModelFiltros: viewModel_filtado_tiendas,
    item_tiendas: tiendas_por_categoria,
    abierto_cerrado: Boolean,
    listener_botom_sheet: (id_tienda: String, showBottomSheet: Boolean, estado_color: Color, Boolean) -> Unit,
    dialog_sin_registrao: () -> Unit
) {
    // --- Estado local instantáneo ---
    var favoritoEstado by remember { mutableStateOf(false) }
    LaunchedEffect(item_tiendas.id_tienda, horario_box) {
        viewModelFiltros.cast_horario_atencion_horario_tienda_box(horario_box)
    }
    // --- Escuchar el Flow para sincronizar si viene desde otro lado ---
    val mapa by viewModelFiltros.favoritos.collectAsState()
    LaunchedEffect(mapa, item_tiendas.id_tienda) {
        favoritoEstado = mapa[item_tiendas.id_tienda] ?: favoritoEstado
    }
    LaunchedEffect(item_tiendas.id_tienda) {
        if (id_user.isNotEmpty()) {
            viewModelFiltros.verificar_existe_favoritoMap(id_user, item_tiendas.id_tienda)
        }
    }
    val tick by viewModelFiltros.tick.collectAsState()
    var detalles_tienda by remember { mutableStateOf(false) }
    var estadoColor by remember { mutableStateOf(Color.Red) }

    var showDialog by remember { mutableStateOf(false) }

    val generador_qr = remember(item_tiendas.latitud, item_tiendas.longitud) {
        generar_qr_cordenadas_tienda.codificarCoordenadas_url(
            item_tiendas.latitud, item_tiendas.longitud,item_tiendas.id_tienda
        )
    }

    val targetHeight =
        if (!detalles_tienda && item_tiendas.metodos_pago_tienda != modelo_pagos_tienda()) 90.dp else 110.dp

    val altoImgAnimado by animateDpAsState(
        targetValue = targetHeight
    )
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
    var estado_fv_btn by remember { mutableStateOf(false) }
    var nuevo_Estadp_btn_fv by remember { mutableStateOf(false) }
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

                Column {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item_tiendas.logo_tienda)

                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        contentDescription = "Imagen local",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(80.dp)
                            .height(altoImgAnimado)
                            .clip(RoundedCornerShape(15))
                    )
                    spacer_vertical(5.dp)
                    AnimatedVisibility(!detalles_tienda && item_tiendas.metodos_pago_tienda != modelo_pagos_tienda()) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(25.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            campos_de_pago(listState, item_tiendas.metodos_pago_tienda)
                            // 👈 izquierda
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .align(Alignment.CenterStart)
                                    .zIndex(1f)
                                    .alpha(alphaLeft)
                                    .background(Brush.horizontalGradient(colors = strat_subcategoria_shadow))
                            )

                            // 👉 derecha
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)

                                    .align(Alignment.CenterEnd)
                                    .zIndex(1f)
                                    .alpha(alphaRight)
                                    .background(
                                        Brush.horizontalGradient(colors = end_subcategoria_shadow)
                                    )
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Nombre_estado_tienda(item_tiendas.nombre_tienda.capitalizeFirst())
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Direccion :", item_tiendas.direccion)
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Referencia :", item_tiendas.referencia)
                    Spacer(modifier = Modifier.height(5.dp))
                    tags_subcateogiras(
                        item_tiendas.lista_subcategoiras,
                        brush_start = Brush.horizontalGradient(colors = strat_subcategoria_shadow),
                        brush_end = Brush.horizontalGradient(colors = end_subcategoria_shadow)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    retornar_color_estado_tienda_Box(
                        id_tienda = item_tiendas.id_tienda,
                        horario_total = horario_box1,
                        tick = tick,
                        pagado = item_tiendas.pagado,
                        color = { color, txt ->
                            estadoColor = color
                        })
                }
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(
                            item_tiendas.pagado && verificar_interner,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            btn_listener_fv_externo(
                                select = favoritoEstado,
                                modifier = Modifier.padding(bottom = 10.dp),
                                listener = { nuevoEstado ->
                                    nuevo_Estadp_btn_fv = nuevoEstado
                                    if (id_user.isNotEmpty()) {
                                        if (nuevoEstado) {
                                            viewModelFiltros.guardar_tienda_favorita_por_id(
                                                localidad_user,
                                                id_user,
                                                item_tiendas.id_tienda
                                            )
                                            favoritoEstado = nuevo_Estadp_btn_fv
                                        } else {

                                            estado_fv_btn = true

                                        }

                                    } else {
                                        dialog_sin_registrao()
                                    }
                                },
                                size_btn = 30.dp,
                                size_icon = 15.dp
                            )
                        }

                        Btn_Expandir_card { expandir -> detalles_tienda = expandir }
                    }
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
                    spacer_vertical(10.dp)

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
    if (estado_fv_btn) {
        dialog_eliminar_favoritos(
            viewModelFiltros = viewModelFiltros,
            item_tiendas.localidad_tienda,
            id_user = id_user,
            id_tienda = item_tiendas.id_tienda,
            nombre_tienda = item_tiendas.nombre_tienda,
            ondimis = { estado_fv_btn = false }, aceptado = {
                nuevo_Estadp_btn_fv = favoritoEstado
            })

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

@Composable
fun campos_de_pago(
    listState: LazyListState,
    metodosPagoTienda: modelo_pagos_tienda,
    width_complete: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier.then(
            if (width_complete) Modifier.fillMaxWidth()
            else Modifier.width(80.dp)
        )
    ) {
        val lista_metodos_pagos = mostrar_iconos_pagos(metodosPagoTienda)
            .filter { it.enable }
        items(lista_metodos_pagos, key = { it.nombre_metodo }) { i ->

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(i.img)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

        }
        item {
            if (metodosPagoTienda.efectivo.enable) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)) // Verde tipo "dinero"
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Efectivo",
                        tint = Color.White, // Ícono blanco
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }


}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda


import Item
import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.zIndex
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
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ImagenesSuperpuestasCollage
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_close_gris
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textos_titulos_geinz_wokr
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.cat_sub_seguirar_salud
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.categorias_defaul
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.SearchViewModel
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ui_pantalla_busqueda(
    localida_defauld: datos_principales_user,
    focusRequester: FocusRequester,

    ocultar: () -> Unit,
    estado_mostar: Boolean,

    iniciar_seccion_normal:()-> Unit,
    crear_cuenta_geinz:()-> Unit
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val viewModel: SearchViewModel = viewModel()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val lista_encontrada_items by viewModel.lista_encontrada.collectAsState()
    val items: List<Item>
    val categorias: List<String>
    when (state) {
        is SearchViewModel.List_items_result.succes -> {
            val succes = state as SearchViewModel.List_items_result.succes
            items = succes.items
            categorias = succes.categoira
        }

        else -> {
            items = emptyList()
            categorias = emptyList()
        }
    }

    val categoria_filtrado by viewModelFiltros._subcategoria_filtrado.observeAsState()
    var subcategira_filtrado by rememberSaveable { mutableStateOf("") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var salud_seguirdad by remember { mutableStateOf("") }
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

    var localidad_Anterior_select by remember { mutableStateOf(tiendaLocalidadSeleccionada) }

    var categoria_filtrad by remember { mutableStateOf("") }
    Log.d("camibamos", "${categoria_filtrad} ${localidad_Anterior_select}")
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

    var color_categoria by remember { mutableStateOf(false) }
    var color_localidad by remember { mutableStateOf(false) }
    var color_subcategoria by remember { mutableStateOf(false) }
    var color_salud_seguirdad by remember { mutableStateOf(false) }
    var mostrar_centrado_visible by remember { mutableStateOf(true) }

    var localidad_tienda_seklecioanda by remember { mutableStateOf("") }

    var placeholder by remember { mutableStateOf("A dónde quieres ir?") }

    var previousLocalidad by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(
        tiendaLocalidadSeleccionada,
        categoria_filtrad,
        subcategira_filtrado,
        salud_seguirdad
    ) {
        Log.d("_cabiamos_localida", tiendaLocalidadSeleccionada ?: "1123")

        val localidadActual = tiendaLocalidadSeleccionada

        if (localidadActual != previousLocalidad && (categoria_filtrad.isEmpty() || subcategira_filtrado.isEmpty() || salud_seguirdad.isEmpty())) {
            Log.d(
                "_cambio_localidad",
                "Cambiamos de ${previousLocalidad ?: "ninguna"} a $localidadActual"
            )
            viewModel.clearResults()
            mostrar_centrado_visible = true
            previousLocalidad = localidadActual
            searchText = TextFieldValue("")
            categoria_filtrad = ""
            subcategira_filtrado = ""
            salud_seguirdad = ""
            return@LaunchedEffect
        }
        if (firstLaunch) {
            firstLaunch = false
            return@LaunchedEffect
        }

        // 🔹 Reset del search
        searchText = TextFieldValue("")

        // 🔹 Placeholder dinámico
        placeholder = if (
            categoria_filtrad.isNotEmpty() ||
            subcategira_filtrado.isNotEmpty() ||
            salud_seguirdad.isNotEmpty()
        ) {
            "Ingresa el nombre"
        } else {
            "A dónde quieres ir?"
        }

        // 🔹 Caso especial: si hay salud/seguridad, tomarlo como categoría
        val categoriaFinal = if (salud_seguirdad.isNotEmpty()) {
            salud_seguirdad
        } else {
            categoria_filtrad
        }

        // 🔹 Llamar solo una vez si hay categoría/subcategoría
        if (categoriaFinal.isNotEmpty() || subcategira_filtrado.isNotEmpty()) {
            Log.d("buscamosen", "entramos_condiocn")
            viewModel.filtar_sub_cat(
                tiendaLocalidadSeleccionada ?: "barranca",
                categoriaFinal,
                subcategira_filtrado
            )
        }
    }



    LaunchedEffect(lista_encontrada_items) {
        if (lista_encontrada_items.isNotEmpty()) {
            Log.d("valor_encontrado", "${lista_encontrada_items.toString()}")
        }
    }

    LaunchedEffect(categoria_filtrad) {
        subcategorias = viewModelFiltros.obtener_lista_sub(categoria_filtrad)
    }

    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localidad_tienda_seklecioanda ?: "barranca",
                id_tienda_selecionada
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModelFiltros.obtener_categorias()
        viewModelFiltros.obtener_cat_lugares()
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }
    LaunchedEffect(cat_sub_seleciondo) {
        if (cat_sub_seleciondo) {
            mostrar_centrado_visible = false
            Log.d("BusquedaFlow", "Texto <2 pero cat_sub=true -> centrado oculto")

        } else {
            mostrar_centrado_visible = true

        }
    }
    Log.d("categoria_filtradcategoria_filtrad", categoria_filtrad)
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

                    TexfielFiltrado(
                        cat_sub_seleciondo,
                        placeholder,
                        focusRequester,
                        searchText,
                        { it ->
                            searchText = TextFieldValue(
                                text = it,
                                selection = TextRange(it.length)
                            )
                            if (it.isNotEmpty() && it.length >= 2) {

                                mostrar_centrado_visible = false
                                if (!cat_sub_seleciondo) {
                                    viewModel.ls_items_ls_cat_fun(
                                        false,
                                        tiendaLocalidadSeleccionada ?: "barranca",
                                        null,
                                        null,
                                        searchText.text,
                                        it
                                    )
                                } else {

                                    viewModel.ls_items_ls_cat_fun(
                                        true,
                                        tiendaLocalidadSeleccionada ?: "barranca",
                                        categoria_filtrad.ifEmpty { salud_seguirdad },
                                        subcategira_filtrado,
                                        searchText.text, it
                                    )
                                }
                            } else {
                                // 📝 Si no hay texto suficiente (<2)
                                if (cat_sub_seleciondo) {

                                    mostrar_centrado_visible = false
                                    viewModel.ls_items_ls_cat_fun(
                                        true,
                                        tiendaLocalidadSeleccionada ?: "barranca",
                                        categoria_filtrad.ifEmpty { salud_seguirdad },
                                        subcategira_filtrado,
                                        "",
                                        it // 🔥 búsqueda vacía
                                    )

                                } else {
                                    // 👉 No hay cat/sub seleccionado → limpio
                                    mostrar_centrado_visible = true
                                    viewModel.clearResults()
                                }
                            }


                        },
                        listener_borrar_texto = {
                            viewModel.clearResults()
                        })

                    spacer_vertical(5.dp)

                    filtrado_chips(
                        viewModel,
                        searchText = searchText.text,
                        lista_filtrado = categorias,
                        salud_seguirdad = salud_seguirdad,
                        lista_subcategoria = subcategorias,
                        categoria_selecionada = categoria_filtrad,
                        categoria_selecionada_fun = { filtrado_Select ->
                            categoria_filtrad = filtrado_Select
                        },
                        subcategoria_selecionada = subcategira_filtrado,
                        subcateogira_selecionada_fun = { filtrado_subcategoria_select ->
                            subcategira_filtrado = filtrado_subcategoria_select
                        },
                        cat_sub_select = { hay_selecccion ->
                            cat_sub_seleciondo = hay_selecccion
                        },
                        seguridad_salud_selec = { saud_select ->
                            salud_seguirdad = saud_select
                        },
                        descolorar_carta_segu = {
                            color_salud_seguirdad = false
                            color_categoria = false
                            color_subcategoria = false
                        },
                        descolorar_carta_cat = {
                            color_salud_seguirdad = false
                            color_categoria = false
                            color_subcategoria = false
                        },
                        descolorar_carta_sub = {
                            color_salud_seguirdad = false
                            color_categoria = false
                            color_subcategoria = false
                        })

                    spacer_vertical(5.dp)
                }
            }


            itemsIndexed(items) { index, item ->
                ramdoBox(
                    firebaseAuth = firebaseAuth,
                    estado_tienda = horario_por_tienda,
                    i = item,
                    index = index,
                    listener_carta = { id, localidad, color ->
                        estadoColor = color
                        localidad_tienda_seklecioanda = localidad
                        id_tienda_selecionada = id
                        viewModelFiltros.obtenerHorarioPorTienda_activa(localidad, id)
                        show_bottom_sheeet = true
                    },
                    abrir_gogle_map = { lat, log ->
                        dialog_Crear_ruta = true
                        latitud = lat
                        longitud = log
                    }, iniciar_seccion_normal = {iniciar_seccion_normal()}, crear_cuenta_geinz = {crear_cuenta_geinz()}
                )
            }
        }

        if (state is SearchViewModel.List_items_result.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center

            ) {
                CircularProgressIndicator()
            }
        }

        when (state) {
            is SearchViewModel.List_items_result.Empty -> {
                texto_generico_one_line(
                    if (searchText.text.isNotEmpty()) {
                        "No se encontraron resultados con \"${searchText.text}\""
                    } else {
                       "No se encontraron resultados"
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                    color = Color.Gray
                )
            }

            is SearchViewModel.List_items_result.Cleared -> {}
            is SearchViewModel.List_items_result.error -> {
                val errorState = state as SearchViewModel.List_items_result.error
                Text(
                    "${errorState.msje}",
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 10.dp)

                )
            }

            else -> {}
        }
        AnimatedVisibility(
            mostrar_centrado_visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            ImagenesSuperpuestasCollage(localida_defauld.nombre)
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
                "Te recomendamos activar el GPS para que podamos mostrarte la mejor ruta hasta el lugar en Google Maps.",
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
            color_categoria = color_categoria,
            color_localidad = color_localidad,
            color_subcategoria = color_subcategoria,
            color_salud_seguridad = color_salud_seguirdad,
            seguidad_salud = salud_seguirdad,
            viewModel = viewModel,
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
//                localidad_Anterior_select=localidad
//                searchText = TextFieldValue("")
            },
            categoria_filtrad,
            categoria_Selecionada = { categoria ->
                categoria_filtrad = categoria
            },
            subcategira_filtrado,
            subcategoria_selecionada = { subcategoria_select ->
                subcategira_filtrado = subcategoria_select
            },
            seguridad_salud_selec = { select ->
                salud_seguirdad = select
            },
            click_carta_localidad = {
                color_localidad = !color_localidad
                color_categoria = false
                color_subcategoria = false
                color_salud_seguirdad = false
            },
            click_carta_localidad_delete = {
                color_localidad = false
            },
            click_carta_categoria = {
                color_categoria = !color_categoria
                color_localidad = false
                color_subcategoria = false
            },
            click_carta_categoria_delete = {
                color_subcategoria = false
                color_categoria = false
            },
            click_carta_seguridad = {
                color_salud_seguirdad = !color_salud_seguirdad
                color_localidad = false
                color_categoria = false
            },
            click_carta_seguridad_delete = {
                color_salud_seguirdad = false
            },
            click_carta_subcategoria = {
                color_subcategoria = !color_subcategoria
                color_localidad = false
                color_categoria = false
            },
            click_carta_subcategoria_delete = {
                Log.d("elminados_", "coloreelimado")
                color_subcategoria = false
            },
            click_salud_general = {
                color_subcategoria = false
                color_categoria = false
                color_localidad = false
            },
            tiene_categorias = {
                color_salud_seguirdad = false
                color_subcategoria = false
            })
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FloatingBubble(
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
    tiene_categorias: () -> Unit
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
                        .fillMaxHeight(0.85f), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .fillMaxHeight(0.22f)
                            .heightIn(min = 150.dp)
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
                                                    seguridad_salud_selec("")
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // 90%
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
                                    .weight(.7f)
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
                                                        subcategoria_selecionada("")
                                                        seguridad_salud_selec("")
                                                        mostrar_chip_salud_seguridad.value = false
                                                        mostrarChipCategoria.value = true
                                                        mostrarChipsubcategoria.value = false
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
                                    0 -> 0.5f
                                    1 -> 0.3f
                                    else -> 0.3f
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
                                    .background(backgrpound_salud_seguridad), expandir_clik = {
                                    expandedIndex = if (expandedIndex == 0) -1 else 0
                                }, cat_sub_selection = seguidad_salud,
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
                                                                text = sub.replaceFirstChar { it.uppercase() },
                                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.primary,
                                                                visible = expanded
                                                            ) {
                                                                subcategoria_selecionada(sub)
                                                                filtros =
                                                                    filtros.copy(subcategoria = sub)
                                                                mostrarChipsubcategoria.value = true
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
                                                    colors = listOf(startTopColor, endTopColor),

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
    salud_seguirdad: String,
    lista_subcategoria: List<String>,
    categoria_selecionada: String,
    categoria_selecionada_fun: (String) -> Unit,
    subcategoria_selecionada: String,
    subcateogira_selecionada_fun: (String) -> Unit,
    cat_sub_select: (Boolean) -> Unit,
    seguridad_salud_selec: (String) -> Unit,
    descolorar_carta_segu: () -> Unit,
    descolorar_carta_cat: () -> Unit, descolorar_carta_sub: () -> Unit
) {
    // ✅ Unicidad
    val categoriasUnicas = lista_filtrado.distinct()
    val subcategoriasUnicas = lista_subcategoria.distinct()

    val hayCategoria = categoria_selecionada.isNotEmpty() && categoria_selecionada.length >= 2
    val haySubcategoria =
        subcategoria_selecionada.isNotEmpty() && subcategoria_selecionada.length >= 2
    val salud_seguirdad_valor = salud_seguirdad.isNotEmpty() && salud_seguirdad.length >= 2
    val haySeleccion = hayCategoria || haySubcategoria || salud_seguirdad_valor
    var mostrar_texto by remember { mutableStateOf(false) }

    cat_sub_select(haySeleccion)

    // ✅ Caso especial: si hay salud/seguridad, mostrar solo eso

    if (salud_seguirdad.isNotEmpty()) {
        LazyRow() {
            item {
                chisp_filtrado_busqueda(
                    carta_selecionada = salud_seguirdad_valor,
                    filtrado = salud_seguirdad,
                    btn_visible = true,
                    clik_card = { seguridad_salud_selec(salud_seguirdad) },
                    onClick_delete = {
                        categoria_selecionada_fun("")
                        subcateogira_selecionada_fun("")
                        seguridad_salud_selec("")
                        viewModel.clearResults()
                        descolorar_carta_segu()
                    }
                )
            }
        }
    } else {
        // ✅ Flujo normal si salud_seguirdad está vacío
        val categoriasFiltradas = if (hayCategoria) {
            listOf(categoria_selecionada)
        } else {
            categoriasUnicas
        }

        if (searchText.length >= 2 && !haySeleccion) {
            Log.d("entramos_Seach", "1")
            LazyRowConSombras() {
                // ✅ Mostrar categorías únicas
                items(categoriasFiltradas) { cat ->
                    val catSeleccionada = categoria_selecionada == cat
                    chisp_filtrado_busqueda(
                        carta_selecionada = catSeleccionada,
                        filtrado = cat,
                        btn_visible = true,
                        clik_card = { categoria_selecionada_fun(cat) },
                        onClick_delete = {
                            categoria_selecionada_fun("")
                            subcateogira_selecionada_fun("")
                            viewModel.clearResults()
                            descolorar_carta_cat()
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
                        onClick_delete = {
                            subcateogira_selecionada_fun("")
                            descolorar_carta_sub()
                        }
                    )
                }
            }
        } else if (searchText.length < 2 && !haySeleccion) {
            Log.d("entramos_Seach", "2")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LazyRowConSombras {
                    items(categorias_defaul) { i ->
                        chisp_filtrado_busqueda(
                            carta_selecionada = false,
                            filtrado = simplificarCategoria(i),
                            btn_visible = false,
                            clik_card = { categoria_selecionada_fun(i) },
                            onClick_delete = {
                                categoria_selecionada_fun("")
                                subcateogira_selecionada_fun("")
                                viewModel.clearResults()
                                descolorar_carta_cat()
                            }
                        )
                    }
                }
                spacer_vertical(10.dp)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Flecha arriba",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            mostrar_texto = !mostrar_texto
                        }
                )

                spacer_vertical(5.dp)
                AnimatedVisibility(mostrar_texto) {
                    texto_generico_one_line("Seleciona una categoria para empezar")
                }

            }


        } else {
            Log.d("entramos_Seach", "3")
            LazyRowConSombras() {
                items(categoriasFiltradas) { cat ->
                    val catSeleccionada = categoria_selecionada == cat
                    chisp_filtrado_busqueda(
                        carta_selecionada = catSeleccionada,
                        filtrado = cat,
                        btn_visible = true,
                        clik_card = { categoria_selecionada_fun(cat) },
                        onClick_delete = {
                            categoria_selecionada_fun("")
                            subcateogira_selecionada_fun("")
                            viewModel.clearResults()
                            descolorar_carta_cat()
                        }
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
                        onClick_delete = {
                            subcateogira_selecionada_fun("")
                            descolorar_carta_sub()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun LazyRowConSombras(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
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

    // 🔥 animar alpha, no crear/destruir Box
    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaLeft"
    )
    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaRight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )

        // 👈 izquierda
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
    cat_sub_seleciondo: Boolean,
    placeholder: String,
    focusRequester: FocusRequester,
    texto: TextFieldValue,
    onvalueChage: (String) -> Unit,
    listener_borrar_texto: () -> Unit,
) {
    var icono_borrar by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    if (texto.text.isEmpty()) {
        icono_borrar = false
    }
    OutlinedTextField(
        value = texto,
        onValueChange = { newValue: TextFieldValue ->
            Log.d("falta_señal", newValue.text)
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
                    if (cat_sub_seleciondo) {
                        Log.d("seleccion", "existe")
                        onvalueChage("")
                    } else {
                        Log.d("seleccion", " no existe")
                        onvalueChage("")
                        listener_borrar_texto()
                    }
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
    firebaseAuth: FirebaseAuth,
    estado_tienda: Map<String, Boolean>?,
    i: Item,
    index: Int,
    listener_carta: (String, String, Color) -> Unit,
    abrir_gogle_map: (Double, Double) -> Unit,
    iniciar_seccion_normal:()-> Unit,
    crear_cuenta_geinz:()-> Unit,
) {
    val heightOptions = listOf(300.dp, 350.dp)
    val estado_tienda_filter = estado_tienda?.get(i.id_tienda) == true
    Log.d("estado_tienda", estado_tienda_filter.toString())
    var Estado_color = if (estado_tienda_filter) Color.Green else Color.Red
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]
    val iconCategoria = constantes_lista_localidades.getCategoriaIcon(i.categoria)
    var mostra_dialog_login by remember { mutableStateOf(false) }
    var texto_bottom_sheet_dialog_login by remember { mutableStateOf("") }
    val context = LocalContext.current
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
                        .clickable {
                            if (firebaseAuth.currentUser != null) {
                                if (i.categoria == "turismo") {
                                    Toast.makeText(
                                        context,
                                        "mostramos_dialog_turismo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    listener_carta(i.id_tienda, i.lugar, Estado_color)
                                }
                            } else {
                                texto_bottom_sheet_dialog_login="¡Regístrate para ver todos los detalles y disfrutar la experiencia completa!"
                                mostra_dialog_login=true
                            }

                        },
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
                                    Color(0xFF262626)
                                ),

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
                        onClick = {
                            if (firebaseAuth.currentUser != null) {
                                abrir_gogle_map(i.latitud, i.longitud)
                            } else {
                                texto_bottom_sheet_dialog_login="Crea tu ruta registrándote ahora"
                                mostra_dialog_login=true
                            }
                        },
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
                tags_subcateogiras(
                    i.lista,
                    brush_start = Brush.horizontalGradient(colors = shadow_top_filtrado_v1),
                    brush_end = Brush.horizontalGradient(colors = shadow_botonm_filtrado_v1)
                )
            }
        }
    }
    if (mostra_dialog_login) {
        bottom_sheet_registrate(
            ondimis = { mostra_dialog_login = false },
            iniciar_seccion_normal = {iniciar_seccion_normal()},
            crear_cuenta_geinz = {crear_cuenta_geinz()},
            texto_bottom_Sheet = texto_bottom_sheet_dialog_login
        )
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun apartado_lugares_interes(
    clikeado: Boolean,
    expandedIndex: Int,
    texto: String,
    lista_subcategoria: List<String>,
    enColumna: Boolean,
    modifier: Modifier = Modifier,
    expandir_clik: () -> Unit,
    cat_sub_selection: String,
    cat_sub_clik: (String) -> Unit
) {
    val icono_expandido = if (expandedIndex == 0) {
        Icons.Default.ExpandMore
    } else {
        Icons.Default.ExpandLess
    }

    val listState = if (enColumna) rememberLazyListState() else rememberLazyListState()

    // sombreado arriba/abajo (solo aplica en columnas)
    val showTopShadow by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 0 }
    }
    val showBottomShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < totalItems - 1
        }
    }

    var color_subcategoria by remember { mutableStateOf(false) }

    val startTopColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_top_filtrado_v1[0] else shadow_top_filtrado_v2[0],
        animationSpec = tween(500), label = ""
    )
    val endTopColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_top_filtrado_v1[1] else shadow_top_filtrado_v2[1],
        animationSpec = tween(500), label = ""
    )
    val startBottomColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_botonm_filtrado_v1[0] else shadow_botonm_filtrado_v2[0],
        animationSpec = tween(500), label = ""
    )
    val endBottomColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_botonm_filtrado_v1[1] else shadow_botonm_filtrado_v2[1],
        animationSpec = tween(500), label = ""
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        if (enColumna) {
            // LazyColumn
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    texto_generico_multilinea(
                        texto.capitalizeFirst(),
                        MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                    spacer_vertical(5.dp)
                }
                items(lista_subcategoria) { i ->
                    val seleccionado = if (cat_sub_selection.equals(
                            i,
                            ignoreCase = true
                        )
                    ) Color.Black else MaterialTheme.colorScheme.primary
                    AnimatedFabItem(
                        i,
                        seleccionado,
                        true,
                        onClick = {
                            cat_sub_clik(i)
                        })
                }
            }

            // sombras solo para columna
            AnimatedVisibility(
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
                                colors = listOf(startTopColor, endTopColor),

                                )
                        )
                )
            }

            AnimatedVisibility(
                showBottomShadow,
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
        }

        btn_close_gris(
            modifier = Modifier.align(Alignment.TopEnd),
            icono_expandido,
            onClick = { expandir_clik() }
        )
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
                text.capitalizeFirst(),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


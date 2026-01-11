package com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_compartir.compartir_pantalla_completa
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_servicios_tramite
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_pago_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_ayudanos_a_creccer
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad.filtrado_texfiel
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite.carga_servicios
import com.google.firebase.database.core.Context

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ui_servicio_tramite(verificar_intener: Boolean, localida: String,iduser:String) {
    val viewmodel_filtrado: viewModel_filtado_tiendas = viewModel()
    val viewmode_servicios_tramite: viewmode_servicios_tramite = viewModel()
    val lugares by viewmode_servicios_tramite.lugares.observeAsState(emptyList())
    val carga_pantalla_completa by viewmode_servicios_tramite.mostrar_carga_turistico.collectAsState()
    val lista_servicios = constantes_lista_localidades.lista_fitlrado_servicios_basicos
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var dialog_servicos_tramite by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf(dataclass_lugares_db()) }
    var id_tienda_select by remember { mutableStateOf("") }
    var localidad_tienda by remember { mutableStateOf("") }
    var pagado_tienda by remember { mutableStateOf(false) }
    var mostrar_diaogo_general by remember { mutableStateOf(false) }
    var motrar_dialog_tienda_Select by remember { mutableStateOf(false) }
    var mostrar_dialog_tienda_no_pagada by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var dataclass_datos_tienda_free by remember { mutableStateOf(datos_tienda_free()) }
    val datosTienda by viewmodel_filtrado._datos_tienda.observeAsState()
    var expandedIndex by remember { mutableStateOf(-1) }
    var lista_mostrar by remember { mutableStateOf<List<dataclass_lugares_db>>(emptyList()) }
    var mostrandoCarga_free by remember { mutableStateOf(false) }
    var lista_base_seguridad by rememberSaveable { mutableStateOf(emptyList<dataclass_lugares_db>()) }
    val state_servicios =
        viewmode_servicios_tramite._state_servicios.collectAsState(carga_servicios.loading).value
    val estadoTiendaFree by viewmodel_filtrado._datos_tienda_sin_pago.observeAsState(
        viewModel_filtado_tiendas.carga_tiendas_sin_pago.loading_tiendas_free
    )
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

    var mostar_bottom_sheet_ayuda_geinz by remember { mutableStateOf(false) }
    var yaInicializado by remember { mutableStateOf(false) }

    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada = datosTienda!!.first()
        }
    }

    LaunchedEffect(motrar_dialog_tienda_Select) {
        if (motrar_dialog_tienda_Select) {
            viewmodel_filtrado.obtener_campos_tiendas_por_id(
                localida ?: "barranca", id_tienda_select
            )
        }
    }
    LaunchedEffect(lugares) {
        if (!yaInicializado && lugares.isNotEmpty()) {
            yaInicializado = true
            lista_base_seguridad = lugares
            viewmode_servicios_tramite.todos(lugares)
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
                mostrar_dialog_tienda_no_pagada = true // 👉 Abre el diálogo
            }

            is viewModel_filtado_tiendas.carga_tiendas_sin_pago.error_tiendas_free -> {
                mostrandoCarga_free = false // ❌ Error, deja de cargar
            }

            else -> Unit
        }
    }


    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewmode_servicios_tramite.obtener_lugares(context, localida)

    }
    LaunchedEffect(valor_filtrado) {
        if (yaInicializado) {
            if (valor_filtrado.isNotEmpty()) {
                viewmode_servicios_tramite.filtrar_nombre_categoria(
                    valor_filtrado,
                    subCategoriaSeleccionada,
                    lista_base_seguridad
                )
                Log.d("actuliazmos_lugares", "${lista_base_seguridad}")
            } else {
                viewmode_servicios_tramite.filtrar_por_categoria(context, subCategoriaSeleccionada)
            }
        }
    }

    LaunchedEffect(mostrar_dialog_tienda_no_pagada) {
        if (mostrar_dialog_tienda_no_pagada) {
            viewmodel_filtrado.obtener_tienda_no_pagada(localida, id_tienda_select)
        }
    }

    LaunchedEffect(subCategoriaSeleccionada) {
        valor_filtrado = ""
    }

    LaunchedEffect(lugares, subCategoriaSeleccionada) {
        if (yaInicializado && lugares.isNotEmpty() && subCategoriaSeleccionada != "Todos") {
            viewmode_servicios_tramite.filtrar_por_categoria(context, subCategoriaSeleccionada)
        } else {
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
                    Log.d("entramos", "cargando")
                    item(span = StaggeredGridItemSpan.FullLine) {
                        progress_bar = true
                        sin_resultados = false

                    }
                }

                is viewmode_servicios_tramite.carga_servicios.succes -> {
                    Log.d("entramos", "succes")
                    progress_bar = false
                    sin_resultados = false
                    val lista =
                        (state_servicios as viewmode_servicios_tramite.carga_servicios.succes).items
                    itemsIndexed(lista, key = { _, item -> item.id }) { index, lugar ->
                        carta_servicio_tramites(
                            dataclass_lugares_db = lugar,
                            index = index,
                            isExpanded = false, click_card = {
                                seleccionado = lugar
                                dialog_servicos_tramite = true
                            }, click_car_gas_agua = { id, localidad, pagado ->
                                id_tienda_select = id
                                mostrar_diaogo_general = true
                                localidad_tienda = localidad
                                pagado_tienda = pagado
                                if (pagado) {
                                    motrar_dialog_tienda_Select = true
                                } else {
                                    mostrar_dialog_tienda_no_pagada = true
                                }
                            })
                    }
                }

                is viewmode_servicios_tramite.carga_servicios.emoty -> {
                    Log.d("entramos", "vacio")
                    progress_bar = false
                    sin_resultados = true
                    val texto =
                        (state_servicios as viewmode_servicios_tramite.carga_servicios.emoty).texto
                    texto_error_empity = texto

                }

                is viewmode_servicios_tramite.carga_servicios.error -> {
                    Log.d("entramos", "error")
                    progress_bar = false
                    sin_resultados = true
                    val texto =
                        (state_servicios as viewmode_servicios_tramite.carga_servicios.error).texto
                    texto_error_empity = texto

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
                    "loading" -> {}
                    "empty" ->
                        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                texto_error_empity,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            spacer_vertical(5.dp)
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


                    "none" -> {}
                }
            }
        }

        if(mostar_bottom_sheet_ayuda_geinz){
            bottom_sheet_ayudanos_a_creccer(iduser,verificar_intener,localida?:"barranca",
                { mostar_bottom_sheet_ayuda_geinz = false },viewmodel_filtrado)
        }
        if (motrar_dialog_tienda_Select) {
            bottom_sheet_tiendas_filtradas(
                verificar_intener,
                viewmodel_filtrado,
                dataclass_tienda_seleccionada,
                motrar_dialog_tienda_Select
            ) {
                motrar_dialog_tienda_Select = false
            }
        }

        if (mostrar_dialog_tienda_no_pagada) {
            dialog_sin_pago_tiendas(
                mostrandoCarga_free,
                dataclass_datos_tienda_free,
                ondimis = { mostrar_dialog_tienda_no_pagada = false })
        }


        // 🔹 Diálogo de detalle
        if (dialog_servicos_tramite && seleccionado != null) {
            dialog_servicios_tramite(iduser,
                localida,
                ondimis = { dialog_servicos_tramite = false },
                seleccionado!!
            )
        }

        if (carga_pantalla_completa) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(5f),
                contentAlignment = Alignment.Center
            ) {
                pantalla_carga_login(false)
            }
        }
        AnimatedVisibility(
            !carga_pantalla_completa,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
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
        }
        // 🔹 Sombra inferior
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(40.dp)
//                .align(Alignment.BottomCenter)
//                .background(
//                    Brush.verticalGradient(
//                        colors = listOf(Color.Transparent, Color.Black)
//                    )
//                )
//                .graphicsLayer { alpha = 0.4f }
//        )

    }

}

@Composable
fun centrado_hori_vertical(
    content: @Composable () -> Unit
) {
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
    click_card: () -> Unit,
    click_car_gas_agua: (id: String, localida: String, pagado: Boolean) -> Unit
) {
    val heightOptions = listOf(200.dp, 210.dp)
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                val categoriasProhibidas = listOf("gas", "agua de mesa")
                if (!dataclass_lugares_db.categoria.any { it in categoriasProhibidas }) {
                    click_card()
                } else {
                    click_car_gas_agua(
                        dataclass_lugares_db.id,
                        dataclass_lugares_db.lugar_nombre,
                        dataclass_lugares_db.pagado
                    )
                }


            }
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
            loading = {
                Image(
                    painter = painterResource(R.drawable.cargando_img_categorias),
                    contentDescription = "Cargando...",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            },
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
    val context=LocalContext.current
    Row(horizontalArrangement = Arrangement.Center , verticalAlignment = Alignment.CenterVertically){
    Text(text = "servicios esenciales y tramites", fontFamily = baners_geinz_work, fontSize = 30.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier,
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.5f)).clickable{
                        compartir_pantalla_completa("seyt","Explora los lugares disponibles en Geinz para tus servicios y trámites.",context)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.comparir_icon), modifier = Modifier.size(16.dp), contentDescription = null)
            }
        }
    }
    spacer_vertical(5.dp)
    texto_generico_multilinea(
        "Accede al instante a todos los servicios y trámites esenciales de $localiad. Información verificada.",
        MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 10.dp)
    )

}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.nuevos_negocios

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_listener_fv_externo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_eliminar_favoritos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_qr_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.firebaseAuth
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Btn_Expandir_card
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Caracteristicas_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Nombre_estado_tienda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Text_fiel_filtrado
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.campos_de_pago
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.chips_filtrado
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.pasar_teindas_nuevas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.strat_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.viewModels.viewModel_favoritos
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_novedades_tiendas
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun nuevos_negocios(
    verificar_inter: Boolean,
    localida_select: String,
    crear_cuenta: () -> Unit,
    iniciar_normal: () -> Unit
) {

    val viewmodel_novedades_teinda: viewmodel_novedades_tiendas = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val context = LocalContext.current

    val estado = viewmodel_novedades_teinda.obtener_datos_tienda.collectAsState().value
    val listState = rememberLazyListState()

    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var categoria_seleccionda by rememberSaveable { mutableStateOf("") }
    var lista_subcategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val horarios by viewModelFiltros.horariosTiendas_real.collectAsState()
    var bottom_sheet_iniciar_seccion by remember { mutableStateOf(false) }
    var texto_falta_registra by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var id_tienda_selecionada by remember { mutableStateOf("") }
    var bottom_shet_tienda by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState(emptyList())

    // --- CONTROL MANUAL DEL LOADING ---
    var mostrarLoading by remember { mutableStateOf(true) }

    // Al iniciar: activar loading
    LaunchedEffect(Unit) {
        mostrarLoading = true
        viewmodel_novedades_teinda.obtener_datos_nuevos_tiendas(localida_select)
        viewModelFiltros.iniciarEscucha(
            localidad = localida_select,
            categoria = "todos"
        )
    }
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(localida_select, id_tienda_selecionada)
        }
    }
    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }

    // Cuando estado pase a SUCCESS → mantener loading 1.5s más
    LaunchedEffect(estado) {
        if (estado is viewmodel_novedades_tiendas.carga_datos_tienda.succes) {
            delay(3000)     // ⏳ <- aquí se controla el tiempo extra
            mostrarLoading = false
        }
    }

    // UI completa
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            texto_generico_multilinea(
                "Lo nuevo en tu ciudad",
                style = MaterialTheme.typography.banerGeinzWork,
                modifier = Modifier.padding(end = 20.dp)
            )
            spacer_vertical(5.dp)
            texto_generico_multilinea(
                "Aquí verás los negocios que acaban de unirse a Geinz. Estarán destacados apenas 14 días, así que descúbrelos hoy mismo, guárdalos y vuelve a ellos cuando quieras",
                style = MaterialTheme.typography.bodyMedium
            )


            when (estado) {
                is viewmodel_novedades_tiendas.carga_datos_tienda.empty -> {
                    Text("Sin datos...", color = Color.Gray)
                }

                is viewmodel_novedades_tiendas.carga_datos_tienda.error -> {
                    Text("Error al cargar datos.", color = Color.Red)
                }

                is viewmodel_novedades_tiendas.carga_datos_tienda.succes -> {
                    val lista = remember(estado.datos) { estado.datos }
                    val categorias = estado.categorias
                    lista_subcategorias = categorias

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        stickyHeader {
                            ColumnContenedorComun {
                                chips_filtrado(
                                    listState = listState,
                                    sub_categoria_selecionada = subCategoriaSeleccionada,
                                    lista_subcategorias = lista_subcategorias,
                                    expandir_carta = {},
                                    selecionado = { categoria ->
                                        categoria_seleccionda = categoria
                                        subCategoriaSeleccionada = categoria_seleccionda
                                        viewmodel_novedades_teinda.filtrarPorCategoria(
                                            categoria_seleccionda
                                        )
                                    }
                                )
                            }
                        }

                        items(
                            items = lista,
                            key = { it.id_tienda }
                        ) { tienda ->
                            val horarioDeEstaTienda = horarios[tienda.id_tienda] ?: HorarioDia_box()

                            item_tiendas_registradas(
                                horario_box1 = horarioDeEstaTienda,
                                horario_box = tienda.horario_atencion,
                                verificar_inter,
                                localidad_user = "barranca",
                                id_user = uid_respald_user,
                                viewModelFiltros = viewModelFiltros,
                                item_tiendas = tienda,
                                listener_botom_sheet = { id_tienda, listener, estado_color, pagado ->
                                    if (firebaseAuth.currentUser != null || uid_respald_user.isNotEmpty()) {
                                        id_tienda_selecionada = id_tienda

                                        bottom_shet_tienda = true
                                        showBottomSheet = listener

                                    } else {
                                        bottom_sheet_iniciar_seccion = true
                                        texto_falta_registra =
                                            "Regístrate para ver los detalles completos y las funciones exclusivas"

                                    }
                                },
                                {
                                    bottom_sheet_iniciar_seccion = true
                                    texto_falta_registra = "Regístrate para agregar a tus favoritos"
                                })
                        }
                    }

                    if (bottom_shet_tienda) {
                        bottom_sheet_tiendas_filtradas(
                            verificar_inter,
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

                else -> Unit
            }
        }

        // --- LOADING ENCIMA DE TODO ---
        if (mostrarLoading) {
            carga_inicial() // ⚡ sigue mostrándose incluso si ya llegó SUCCESS
        }
    }
}


@Composable
fun carga_inicial() {
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun item_tiendas_registradas(
    horario_box1: HorarioDia_box,
    horario_box: HorarioAtencion_box,
    verificar_interner: Boolean,
    localidad_user: String,
    id_user: String,
    viewModelFiltros: viewModel_filtado_tiendas,
    item_tiendas: dataclass_novedades_geinz,
    listener_botom_sheet: (id_tienda: String, showBottomSheet: Boolean, estado_color: Color, Boolean) -> Unit,
    dialog_sin_registrao: () -> Unit
) {
    val img = remember(item_tiendas.logo_img) { item_tiendas.logo_img }

    var estadoColor by remember { mutableStateOf(Color.Gray) }
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

    var estado_fv_btn by remember { mutableStateOf(false) }
    var nuevo_Estadp_btn_fv by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                listener_botom_sheet(
                    item_tiendas.id_tienda,
                    true,
                    estadoColor,
                    true
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
                            .data(img)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        contentDescription = "Imagen local",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(80.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(15))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    texto_generico_one_line(item_tiendas.nombre_tienda.capitalizeFirst())
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Direccion :", item_tiendas.direccion)
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Categoria :", item_tiendas.categoria)
                    Spacer(modifier = Modifier.height(5.dp))
                    tags_subcateogiras(
                        item_tiendas.lista_subcateogira,
                        brush_start = Brush.horizontalGradient(colors = strat_subcategoria_shadow),
                        brush_end = Brush.horizontalGradient(colors = end_subcategoria_shadow)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
//
                    retornar_color_estado_tienda_Box(
                        id_tienda = item_tiendas.id_tienda,
                        horario_total = horario_box1,
                        tick = tick,
                        pagado = true,
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
                            true && verificar_interner,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            btn_listener_fv_externo(
                                favoritoEstado,
                                Modifier.padding(bottom = 10.dp),
                                { nuevoEstado ->
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
                                30.dp,
                                15.dp
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

    if (estado_fv_btn) {
        dialog_eliminar_favoritos(
            viewModelFiltros = viewModelFiltros,
            localidad_tienda = item_tiendas.localidad_tienda,
            id_user = id_user,
            id_tienda = item_tiendas.id_tienda,
            nombre_tienda = item_tiendas.nombre_tienda,
            ondimis = { estado_fv_btn = false }, aceptado = {
                nuevo_Estadp_btn_fv = favoritoEstado
            })

    }

}

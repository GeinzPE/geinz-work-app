package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general


import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.img_con_texto
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.DatosResponse
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_para_filtrado_manual
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_close_gris
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_filtrados_promos_y_ofertas(
    filtrado_ia: Boolean,
    datos_filtrado: DatosResponse?,
    comodidad_selet: Set<String>, metodo_pago: Set<String>,
    rango_precio: String?,
    viewModel: viewmodel_promos_cercanas,
    onClose: () -> Unit, onAutocompletar: (String) -> Unit, limpiar_textos: () -> Unit,
) {
    var expandido by remember { mutableStateOf(true) }
    Log.d("datos_padaso", "$filtrado_ia $datos_filtrado")

    var verTodos by remember { mutableStateOf(false) }
    val obtener_cateogiras by viewModel.obtener_categorias.collectAsState()
    Log.d("obtener_cateogiras", "$obtener_cateogiras")

    val categoriaSeleccionada by viewModel.categoria_seleccionada.collectAsState()
    val subcategoriasSeleccionadas by viewModel.subcategoria_seleccionada.collectAsState()
    val rango_precio by viewModel.rangoPrecioSeleccionado.collectAsState()
    val comodidad_selet by viewModel.comodidadesSeleccionadas.collectAsState()
    val metodo_pago by viewModel.metodosPagoSeleccionados.collectAsState()
    val listaData by viewModel.listaResultados.collectAsState()
    // ── 1. CATEGORÍAS — siempre visible ─────────────────────
    val terminos_nlp by viewModel.terminos_nlp.collectAsState()
    val terminos_nlp_seleccionados by viewModel.terminos_nlp_seleccionados.collectAsState()
    val categoriaInicial = remember { categoriaSeleccionada }
    val subcategoriasIniciales = remember { subcategoriasSeleccionadas.toSet() }
    val rangoInicial = remember { rango_precio }
    val comodidadesIniciales = remember { comodidad_selet.toSet() }
    val pagosIniciales = remember { metodo_pago.toSet() }
    val terminosIniciales = remember { terminos_nlp_seleccionados.toSet() }
    val texto_ser_guardado by viewModel.texto_usser_buscado.collectAsState()
    val subcategorias_obtenidas = obtener_cateogiras
        .firstOrNull { it.categoria == categoriaSeleccionada }
        ?.subcategoria
        ?: emptyList()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lista_filtrados_pagos = remember {
        listOf(
            img_con_texto(R.drawable.yape_logo, "yape"),
            img_con_texto(R.drawable.logo_plin, "plin"),
            img_con_texto(R.drawable.visa_logo, "visa"),
            img_con_texto(R.drawable.master_car_logo, "mastercard"),
            img_con_texto(R.drawable.efectivo_logo, "efectivo"),
            img_con_texto(R.drawable.logo_agora, "agora"),
        )
    }

    val rango_precios = remember {
        listOf(
            "0 - 10", "10 - 20", "20 - 30", "30 - 50", "50 - 80",
            "80 - 120", "120 - 200", "200 - 350", "350 - 500",
            "500 - 1000", "1000 - 2500", "2500 - 5000",
            "5000 - 10000", "Más de 10000"
        )
    }

    val precioDicho = datos_filtrado?.precio_max
    val metodos_pago_params = datos_filtrado?.metodos_pago
    val comodidadesParams = datos_filtrado?.comodidades

    LaunchedEffect(metodos_pago_params) {
        metodos_pago_params?.let { lista ->
            viewModel.limpiarMetodosPago()
            viewModel.setPagosDesdeLista(lista)
        }
    }

    var autoComodidadesAplicado by remember { mutableStateOf(false) }

    LaunchedEffect(comodidadesParams) {
        if (!autoComodidadesAplicado) {
            comodidadesParams?.let { lista ->
                val normalizados = lista.map { it.lowercase().trim() }
                viewModel.limpiar_comodidad()
                viewModel.setComodidadesDesdeLista(normalizados)
                autoComodidadesAplicado = true
            }
        }
    }

    LaunchedEffect(precioDicho) {
        val rangoAuto = obtenerRangoDesdePrecio(precioDicho)
        rangoAuto?.let { viewModel.setearRangoPrecioDesdeNLP(it) }
    }

    val lista_comodidades = remember {
        listOf(
            img_con_texto(R.drawable.icon_wifi, "wifi"),
            img_con_texto(R.drawable.icon_zona_expandida, "zona_expandida"),
            img_con_texto(R.drawable.icon_servicios_higenicos, "servicios_higienicos"),
            img_con_texto(R.drawable.icon_seguridad, "camaras_de_seguridad"),
            img_con_texto(R.drawable.icon_sala_de_espera, "sala_de_espera"),
            img_con_texto(R.drawable.icon_sala_para_ninos, "sala_juegos"),
            img_con_texto(R.drawable.icon_mesa_para_ninos, "mesa_para_ninos"),
            img_con_texto(R.drawable.icon_estacionamiento, "estacionamiento"),
            img_con_texto(R.drawable.icon_enchufa, "enchufe"),
            img_con_texto(R.drawable.icon_aire_acondicionado, "aire_acondicionado"),
            img_con_texto(R.drawable.icon_ingreso_animales, "ingreso_mascotas"),
        )
    }

    val huboCambio = categoriaSeleccionada != categoriaInicial ||
            subcategoriasSeleccionadas.toSet() != subcategoriasIniciales ||
            rango_precio != rangoInicial ||
            comodidad_selet.toSet() != comodidadesIniciales ||
            metodo_pago.toSet() != pagosIniciales ||
            terminos_nlp_seleccionados.toSet() != terminosIniciales
    val limite = 10
    val subcategoriasVisibles = if (verTodos) subcategorias_obtenidas
    else subcategorias_obtenidas.take(limite)

    // 🔑 condición central: hay categoría seleccionada y no es "Todos"
    val categoriaElegida = categoriaSeleccionada.isNotEmpty() && categoriaSeleccionada != "Todos"

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 10.dp)
                    .animateContentSize()
            ) {

                // ── HEADER ──────────────────────────────────────────────
                item {
                    Text(
                        text = "Busca a tu manera",
                        style = MaterialTheme.typography.banerGeinzWork,
                        color = Color.White,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    spacer_vertical(7.dp)
                    texto_generico_multilinea(
                        "Busca promociones y ofertas solo para ti en todo Barranca",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(10.dp)
                }


                item {
                    if (filtrado_ia && terminos_nlp.isNotEmpty()) {
                        // 👈 MODO NLP: solo chips de términos, sin categorías ni subcategorías
                        texto_generico_one_line("Filtrando por tu búsqueda")
                        spacer_vertical(5.dp)
                        texto_generico_multilinea(
                            "Deselecciona lo que no quieres buscar",
                            style = MaterialTheme.typography.bodySmall
                        )
                        spacer_vertical(10.dp)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            terminos_nlp.forEach { termino ->
                                val seleccionado = terminos_nlp_seleccionados.contains(termino)
                                chisp_filtrado_busqueda(
                                    carta_selecionada = seleccionado,
                                    filtrado = termino.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = { viewModel.toggleTerminoNlp(termino) },
                                    onClick_delete = {}
                                )
                            }
                        }
                    } else {
                        // 👈 MODO NORMAL: categorías del sistema
                        texto_generico_one_line("Selecciona tu categoría")
                        spacer_vertical(10.dp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(obtener_cateogiras) { categoria ->
                                val seleccionado = categoriaSeleccionada == categoria.categoria
                                chisp_filtrado_busqueda(
                                    carta_selecionada = seleccionado,
                                    filtrado = categoria.categoria.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = { viewModel.toggleCategoria(categoria.categoria) },
                                    onClick_delete = {}
                                )
                            }
                        }
                    }
                }

// 👈 ocultar subcategorías en modo NLP
                item {
                    AnimatedVisibility(
                        visible = categoriaElegida && !filtrado_ia, // 👈 solo modo normal
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        // subcategorías igual que antes...
                    }
                }

                // ── 2. SUBCATEGORÍAS — aparece al elegir categoría ───────
                item {
                    AnimatedVisibility(
                        visible = categoriaElegida && !filtrado_ia,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            spacer_vertical(10.dp)
                            texto_generico_one_line("Subcategorías")
                            spacer_vertical(10.dp)

                            if (!verTodos) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(subcategoriasVisibles) { subcategoria ->
                                        val seleccionado =
                                            subcategoriasSeleccionadas.contains(subcategoria)
                                        chisp_filtrado_busqueda(
                                            carta_selecionada = seleccionado,
                                            filtrado = subcategoria.capitalizeFirst(),
                                            btn_visible = false,
                                            clik_card = { viewModel.toggle_subcategoria(subcategoria) },
                                            onClick_delete = {}
                                        )
                                    }
                                    if (subcategorias_obtenidas.size > limite) {
                                        item {
                                            chisp_filtrado_busqueda(
                                                carta_selecionada = false,
                                                filtrado = "Ver todos",
                                                btn_visible = false,
                                                clik_card = { verTodos = true },
                                                onClick_delete = {}
                                            )
                                        }
                                    }
                                }
                            } else {
                                FlowRow(
                                    maxItemsInEachRow = 3,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    subcategorias_obtenidas.forEach { subcategoria ->
                                        val seleccionado =
                                            subcategoriasSeleccionadas.contains(subcategoria)
                                        chisp_filtrado_busqueda(
                                            carta_selecionada = seleccionado,
                                            filtrado = subcategoria.capitalizeFirst(),
                                            btn_visible = false,
                                            clik_card = { viewModel.toggle_subcategoria(subcategoria) },
                                            onClick_delete = {}
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── BÚSQUEDA GUARDADA — solo si hay texto ────────────────
                item {
                    AnimatedVisibility(
                        visible = texto_ser_guardado.isNotEmpty() && categoriaElegida,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_geinz_500x500),
                                        contentDescription = "Logo IA",
                                        modifier = Modifier
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {}
                                            .size(35.dp)
                                    )
                                    spacer_horizonta(5.dp)
                                    Text(
                                        text = "Geinz",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Repetir búsqueda",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            onAutocompletar(texto_ser_guardado)
                                        }
                                    )
                                }
                                spacer_vertical(8.dp)
                                texto_generico_multilinea(
                                    texto_ser_guardado.capitalizeFirst(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(10.dp)
                            }
                        }
                    }
                }

                // ── RESULTADOS IA ────────────────────────────────────────
                if (listaData.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = categoriaElegida,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                texto_generico_one_line("Resultados de tu búsqueda")
                                spacer_vertical(10.dp)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(listaData) { i ->
                                        chisp_filtrado_busqueda_resultados_busqueda(
                                            "resultado",
                                            carta_selecionada = false,
                                            filtrado = i.capitalizeFirst(),
                                            btn_visible = true,
                                            clik_card = {},
                                            onClick_delete = { viewModel.eliminarItem(i) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        spacer_vertical(2.dp)
                        texto_generico_one_line("Rangos de precio")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(rango_precios) { subcategoria ->
                                val seleccionado = rango_precio == subcategoria
                                chisp_filtrado_busqueda_resultados_busqueda(
                                    "precio",
                                    carta_selecionada = seleccionado,
                                    filtrado = subcategoria.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = { viewModel.setearRangoPrecioDesdeNLP(subcategoria) },
                                    onClick_delete = {}
                                )
                            }
                        }
                        spacer_vertical(6.dp)
                        texto_generico_one_line("Métodos de pago")
                        RadioCheckingMetodos(
                            lista = lista_filtrados_pagos,
                            seleccionados = metodo_pago,
                            onToggle = { texto -> viewModel.toggleMetodoPago(texto) }
                        )
                        spacer_vertical(6.dp)
                        texto_generico_one_line("Comodidades")
                        spacer_vertical(10.dp)
                        RadioCheckingMetodos(
                            lista = lista_comodidades,
                            seleccionados = comodidad_selet,
                            onToggle = { texto -> viewModel.togleRango_select(texto) }
                        )
                        spacer_vertical(10.dp)
                    }
                }

                val hayFiltrosActivos = categoriaSeleccionada.isNotEmpty() ||
                        subcategoriasSeleccionadas.isNotEmpty() || // ✅ agregar
                        !rango_precio.isNullOrEmpty() ||
                        comodidad_selet.isNotEmpty() ||
                        metodo_pago.isNotEmpty() ||
                        terminos_nlp.isNotEmpty() // 👈

                if (hayFiltrosActivos) {
                    item {
                        OutlinedButton(
                            onClick = {
                                viewModel.limpiarCategoria()
                                viewModel.limpiarSubcategorias()
                                viewModel.limpiarRangoPrecio()
                                viewModel.limpiarMetodosPago()
                                viewModel.limpiar_comodidad()
                                viewModel.limpiarTerminosNlp() // 👈
                                viewModel.obtener_promociones_2da("barranca", "", null)
                                onClose()
                                limpiar_textos()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            texto_generico_one_line("Todas las promos")
                        }
                    }
                }
                val mostrarBotonAplicar = huboCambio && (
                        if (filtrado_ia) {
                            terminos_nlp_seleccionados.isNotEmpty() ||
                                    !rango_precio.isNullOrEmpty() ||
                                    comodidad_selet.isNotEmpty() ||
                                    metodo_pago.isNotEmpty()
                        } else {
                            categoriaSeleccionada.isNotEmpty() ||
                                    subcategoriasSeleccionadas.isNotEmpty() ||
                                    !rango_precio.isNullOrEmpty() ||
                                    comodidad_selet.isNotEmpty() ||
                                    metodo_pago.isNotEmpty()
                        }
                        )
                if (mostrarBotonAplicar) {
                    item {
                        Button(
                            onClick = {
                                val data =
                                    if (filtrado_ia && terminos_nlp_seleccionados.isNotEmpty()) {
                                        // 👈 modo NLP
                                        datos_para_filtrado_manual(
                                            categoria = terminos_nlp_seleccionados.joinToString(","),
                                            subcategorias = terminos_nlp_seleccionados,
                                            rango_precio = rango_precio,
                                            pagos = metodo_pago.toList(),
                                            comodidades = comodidad_selet.toList(),
                                            localidad = "barranca"
                                        )
                                    } else {
                                        // 👈 modo manual normal
                                        datos_para_filtrado_manual(
                                            categoria = categoriaSeleccionada,
                                            subcategorias = subcategoriasSeleccionadas.toList(),
                                            rango_precio = rango_precio,
                                            pagos = metodo_pago.toList(),
                                            comodidades = comodidad_selet.toList(),
                                            localidad = "barranca"
                                        )
                                    }
                                viewModel.busqueda_manual_filtrado(data)
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            texto_generico_one_line("Aplicar Filtros")
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_filtrados_general_promos_ofertas(
    filtrado_ia: Boolean,
    datos_filtrado: DatosResponse?,
    viewModel: viewmodel_promos_cercanas,
    onClose: () -> Unit, onAutocompletar: (String) -> Unit, limpiar_textos: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val limite = 10
    val lista_filtrados_pagos = remember {
        listOf(
            img_con_texto(R.drawable.yape_logo, "yape"),
            img_con_texto(R.drawable.logo_plin, "plin"),
            img_con_texto(R.drawable.visa_logo, "visa"),
            img_con_texto(R.drawable.master_car_logo, "mastercard"),
            img_con_texto(R.drawable.efectivo_logo, "efectivo"),
            img_con_texto(R.drawable.logo_agora, "agora"),
        )
    }

    val lista_comodidades = remember {
        listOf(
            img_con_texto(R.drawable.icon_wifi, "wifi"),
            img_con_texto(R.drawable.icon_zona_expandida, "zona_expandida"),
            img_con_texto(R.drawable.icon_servicios_higenicos, "servicios_higienicos"),
            img_con_texto(R.drawable.icon_seguridad, "camaras_de_seguridad"),
            img_con_texto(R.drawable.icon_sala_de_espera, "sala_de_espera"),
            img_con_texto(R.drawable.icon_sala_para_ninos, "sala_juegos"),
            img_con_texto(R.drawable.icon_mesa_para_ninos, "mesa_para_ninos"),
            img_con_texto(R.drawable.icon_estacionamiento, "estacionamiento"),
            img_con_texto(R.drawable.icon_enchufa, "enchufe"),
            img_con_texto(R.drawable.icon_aire_acondicionado, "aire_acondicionado"),
            img_con_texto(R.drawable.icon_ingreso_animales, "ingreso_mascotas"),
        )
    }

    val rango_precios = remember {
        listOf(
            "0 - 10", "10 - 20", "20 - 30", "30 - 50", "50 - 80",
            "80 - 120", "120 - 200", "200 - 350", "350 - 500",
            "500 - 1000", "1000 - 2500", "2500 - 5000",
            "5000 - 10000", "Más de 10000"
        )
    }

    val obtener_cateogiras by viewModel.obtener_categorias.collectAsState()
    var verTodos by remember { mutableStateOf(false) }
    val terminos_nlp by viewModel.terminos_nlp.collectAsState()
    val terminos_nlp_seleccionados by viewModel.terminos_nlp_seleccionados.collectAsState()
    val obtener_cateogiras_filtro_generales by viewModel.obtener_categorias.collectAsState()
    val categoriaSeleccionada by viewModel.categoria_seleccionada.collectAsState()
    val rango_precio by viewModel.rangoPrecioSeleccionado.collectAsState()
    val metodo_pago by viewModel.metodosPagoSeleccionados.collectAsState()
    val comodidad_selet by viewModel.comodidadesSeleccionadas.collectAsState()
    val subcategoriasSeleccionadas by viewModel.subcategoria_seleccionada.collectAsState()
    val categoriaElegida = categoriaSeleccionada.isNotEmpty() && categoriaSeleccionada != "Todos"
    val subcategorias_obtenidas = obtener_cateogiras
        .firstOrNull { it.categoria == categoriaSeleccionada }
        ?.subcategoria
        ?: emptyList()
    val subcategoriasVisibles = if (verTodos) subcategorias_obtenidas
    else subcategorias_obtenidas.take(limite)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .animateContentSize()
            ) {

                // ── HEADER ──────────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(start = 5.dp, end = 5.dp, top = 28.dp, bottom = 20.dp)
                    ) {
                        Column {
                            // Pill indicador superior
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                    .align(Alignment.CenterHorizontally)
                            )
                            spacer_vertical(16.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Filtros",
                                        style = MaterialTheme.typography.banerGeinzWork,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 28.sp,
                                    )
                                    spacer_vertical(2.dp)
                                    texto_generico_multilinea(
                                        "Encuentra lo que buscas",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                // Botón limpiar todo — icono + texto compacto
                                OutlinedButton(
                                    onClick = {
                                        viewModel.limpiarCategoria()
                                        viewModel.limpiarSubcategorias()
                                        viewModel.limpiarRangoPrecio()
                                        viewModel.limpiarMetodosPago()
                                        viewModel.limpiar_comodidad()
                                        viewModel.limpiarTerminosNlp()
                                        viewModel.obtener_promociones_2da("barranca", "", null)
                                        onClose()
                                        limpiar_textos()
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterAltOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "Limpiar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    // Divider sutil
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )
                }

                // ── SECCIÓN: CATEGORÍAS o NLP ────────────────────────────────
                item {
                    FiltroSeccion(
                        numero = "01",
                        titulo = if (filtrado_ia && terminos_nlp.isNotEmpty())
                            "Tu búsqueda" else "Categoría",
                        subtitulo = if (filtrado_ia && terminos_nlp.isNotEmpty())
                            "Deselecciona lo que no quieres" else "¿Qué tipo de promo buscas?"
                    ) {
                        if (filtrado_ia && terminos_nlp.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                terminos_nlp.forEach { termino ->
                                    val seleccionado = terminos_nlp_seleccionados.contains(termino)
                                    chisp_filtrado_busqueda(
                                        carta_selecionada = seleccionado,
                                        filtrado = termino.capitalizeFirst(),
                                        btn_visible = false,
                                        clik_card = { viewModel.toggleTerminoNlp(termino) },
                                        onClick_delete = {}
                                    )
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(obtener_cateogiras_filtro_generales) { categoria ->
                                    val seleccionado = categoriaSeleccionada == categoria.categoria
                                    chisp_filtrado_busqueda(
                                        carta_selecionada = seleccionado,
                                        filtrado = categoria.categoria.capitalizeFirst(),
                                        btn_visible = false,
                                        clik_card = { viewModel.toggleCategoria(categoria.categoria) },
                                        onClick_delete = {}
                                    )
                                }
                            }
                        }
                    }
                }

                // ── SECCIÓN: SUBCATEGORÍAS ───────────────────────────────────
                item {
                    val mostrarExtras = categoriaElegida ||
                            (filtrado_ia && terminos_nlp_seleccionados.isNotEmpty())

                    AnimatedVisibility(
                        visible = mostrarExtras,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        FiltroSeccion(
                            numero = "02",
                            titulo = "Subcategoría",
                            subtitulo = "Afina tu búsqueda"
                        ) {
                            if (!verTodos) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(subcategoriasVisibles) { subcategoria ->
                                        val seleccionado =
                                            subcategoriasSeleccionadas.contains(subcategoria)
                                        chisp_filtrado_busqueda(
                                            carta_selecionada = seleccionado,
                                            filtrado = subcategoria.capitalizeFirst(),
                                            btn_visible = false,
                                            clik_card = { viewModel.toggle_subcategoria(subcategoria) },
                                            onClick_delete = {}
                                        )
                                    }
                                    if (subcategorias_obtenidas.size > limite) {
                                        item {
                                            chisp_filtrado_busqueda(
                                                carta_selecionada = false,
                                                filtrado = "Ver todos",
                                                btn_visible = false,
                                                clik_card = { verTodos = true },
                                                onClick_delete = {}
                                            )
                                        }
                                    }
                                }
                            } else {
                                FlowRow(
                                    maxItemsInEachRow = 3,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    subcategorias_obtenidas.forEach { subcategoria ->
                                        val seleccionado =
                                            subcategoriasSeleccionadas.contains(subcategoria)
                                        chisp_filtrado_busqueda(
                                            carta_selecionada = seleccionado,
                                            filtrado = subcategoria.capitalizeFirst(),
                                            btn_visible = false,
                                            clik_card = { viewModel.toggle_subcategoria(subcategoria) },
                                            onClick_delete = {}
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── SECCIÓN: RANGO DE PRECIO ─────────────────────────────────
                item {
                    val mostrarExtras = categoriaElegida ||
                            (filtrado_ia && terminos_nlp_seleccionados.isNotEmpty())

                    AnimatedVisibility(
                        visible = mostrarExtras,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        FiltroSeccion(
                            numero = "03",
                            titulo = "Rango de precio",
                            subtitulo = "Precio en soles (S/)"
                        ) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(rango_precios) { item ->
                                    val seleccionado = rango_precio == item
                                    chisp_filtrado_busqueda_resultados_busqueda(
                                        "precio",
                                        carta_selecionada = seleccionado,
                                        filtrado = item.capitalizeFirst(),
                                        btn_visible = false,
                                        clik_card = { viewModel.setearRangoPrecioDesdeNLP(item) },
                                        onClick_delete = {}
                                    )
                                }
                            }
                        }
                    }
                }

                // ── SECCIÓN: PAGOS + COMODIDADES (grid 2 col) ────────────────
                item {
                    val mostrarExtras = categoriaElegida ||
                            (filtrado_ia && terminos_nlp_seleccionados.isNotEmpty())

                    AnimatedVisibility(
                        visible = mostrarExtras,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            // Número de sección
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NumeroBadge("04")
                                Column {
                                    texto_generico_one_line("Más filtros")
                                    Text(
                                        text = "Pagos y comodidades",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Métodos de pago
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        desing_chips_texto_filtrado(
                                            "Métodos de pago",
                                            lista_filtrados_pagos,
                                            metodo_pago
                                        ) { txt -> viewModel.toggleMetodoPago(txt) }
                                    }
                                }
                                // Comodidades
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        desing_chips_texto_filtrado(
                                            "Comodidades",
                                            lista_comodidades,
                                            comodidad_selet
                                        ) { txt -> viewModel.togleRango_select(txt) }
                                    }
                                }
                            }
                            spacer_vertical(8.dp)
                        }
                    }
                }

                // ── BOTÓN APLICAR ────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Button(
                            onClick = {
                                val data = if (filtrado_ia && terminos_nlp_seleccionados.isNotEmpty()) {
                                    datos_para_filtrado_manual(
                                        categoria = terminos_nlp_seleccionados.joinToString(","),
                                        subcategorias = terminos_nlp_seleccionados,
                                        rango_precio = rango_precio,
                                        pagos = metodo_pago.toList(),
                                        comodidades = comodidad_selet.toList(),
                                        localidad = "barranca"
                                    )
                                } else {
                                    datos_para_filtrado_manual(
                                        categoria = categoriaSeleccionada,
                                        subcategorias = subcategoriasSeleccionadas.toList(),
                                        rango_precio = rango_precio,
                                        pagos = metodo_pago.toList(),
                                        comodidades = comodidad_selet.toList(),
                                        localidad = "barranca"
                                    )
                                }
                                viewModel.busqueda_manual_filtrado(data)
                                onClose()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text = "Aplicar filtros",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── COMPOSABLES DE APOYO ─────────────────────────────────────────────────────

/**
 * Sección con número badge, título, subtítulo y contenido.
 * Se usa para cada bloque de filtro con separador visual consistente.
 */
@Composable
private fun FiltroSeccion(
    numero: String,
    titulo: String,
    subtitulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NumeroBadge(numero)
            Column {
                texto_generico_one_line(titulo)
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp)) {
            Column { contenido() }
        }
        spacer_vertical(8.dp)
    }
}

/**
 * Badge numérico pequeño — indica el orden de cada sección de filtro.
 */
@Composable
private fun NumeroBadge(numero: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numero,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_filtrar_desde_tienda(
    nombre_tienda: String,
    lista_filtrado_negocio: List<String>,
    rangos_disponibles: List<String>,
    pagos_tienda: List<String>,           // 🔥 agregado
    comodidades_tienda: List<String>,     // 🔥 agregado
    id_tienda: String,
    viewModel: viewmodel_promos_cercanas,
    onClose: () -> Unit,
    onAplicarFiltro: (String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val subcategoriasSeleccionadas by viewModel.subcategoria_seleccionada.collectAsState()
    val rango_precio by viewModel.rangoPrecioSeleccionado.collectAsState()
    val metodo_pago by viewModel.metodosPagoSeleccionados.collectAsState()       // 🔥 agregado
    val comodidad_selet by viewModel.comodidadesSeleccionadas.collectAsState()   // 🔥 agregado

    // 🔥 Guardar estado inicial al abrir el sheet
    val subcategoriasIniciales = remember { subcategoriasSeleccionadas.toSet() }
    val rangoPrecioInicial = remember { rango_precio }
    val pagosIniciales = remember { metodo_pago.toSet() }           // 🔥 agregado
    val comodidadesIniciales = remember { comodidad_selet.toSet() } // 🔥 agregado

    // 🔥 Hubo cambio si el estado actual difiere del inicial
    val huboCambio = subcategoriasSeleccionadas.toSet() != subcategoriasIniciales ||
            rango_precio != rangoPrecioInicial ||
            metodo_pago != pagosIniciales ||           // 🔥 agregado
            comodidad_selet != comodidadesIniciales    // 🔥 agregado

    var verTodos by remember { mutableStateOf(false) }
    val limite = 10
    val subcategoriasVisibles = if (verTodos) lista_filtrado_negocio
    else lista_filtrado_negocio.take(limite)

    // 🔥 agregado
    val lista_filtrados_pagos = remember {
        listOf(
            img_con_texto(R.drawable.yape_logo, "yape"),
            img_con_texto(R.drawable.logo_plin, "plin"),
            img_con_texto(R.drawable.visa_logo, "visa"),
            img_con_texto(R.drawable.master_car_logo, "mastercard"),
            img_con_texto(R.drawable.efectivo_logo, "efectivo"),
            img_con_texto(R.drawable.logo_agora, "agora"),
        )
    }
    val lista_comodidades = remember {
        listOf(
            img_con_texto(R.drawable.icon_wifi, "wifi"),
            img_con_texto(R.drawable.icon_zona_expandida, "zona_expandida"),
            img_con_texto(R.drawable.icon_servicios_higenicos, "servicios_higienicos"),
            img_con_texto(R.drawable.icon_seguridad, "camaras_seguridad"),
            img_con_texto(R.drawable.icon_sala_de_espera, "sala_espera"),
            img_con_texto(R.drawable.icon_sala_para_ninos, "sala_juegos"),
            img_con_texto(R.drawable.icon_mesa_para_ninos, "mesa_para_ninos"),
            img_con_texto(R.drawable.icon_estacionamiento, "estacionamiento"),
            img_con_texto(R.drawable.icon_enchufa, "enchufe"),
            img_con_texto(R.drawable.icon_aire_acondicionado, "aire_acondicionado"),
            img_con_texto(R.drawable.icon_ingreso_animales, "ingreso_con_mascotas"),
        )
    }

    val pagos_disponibles_tienda = remember(pagos_tienda) {
        lista_filtrados_pagos.filter { it.texto in pagos_tienda }.also {
            Log.d("FILTRO_TIENDA", "pagos_tienda recibidos: $pagos_tienda")
            Log.d("FILTRO_TIENDA", "pagos filtrados: ${it.map { p -> p.texto }}")
        }
    }
    val comodidades_disponibles_tienda = remember(comodidades_tienda) {
        lista_comodidades.filter { it.texto in comodidades_tienda }.also {
            Log.d("FILTRO_TIENDA", "comodidades_tienda recibidas: $comodidades_tienda")
            Log.d("FILTRO_TIENDA", "comodidades filtradas: ${it.map { c -> c.texto }}")
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 10.dp)
                    .animateContentSize()
            ) {

                item {
                    Text(
                        text = "Busca a tu manera",
                        style = MaterialTheme.typography.banerGeinzWork,
                        color = Color.White,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    spacer_vertical(7.dp)
                    texto_generico_multilinea(
                        "Encuentra lo que buscas en $nombre_tienda",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(10.dp)
                }

                // 🔥 CARD INFO NEGOCIO: PAGOS + COMODIDADES
                item {
                    AnimatedVisibility(
                        visible = pagos_disponibles_tienda.isNotEmpty() ||
                                comodidades_disponibles_tienda.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {

                                // ── PAGOS ────────────────────────────────
                                if (pagos_disponibles_tienda.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            texto_generico_one_line(
                                                "Métodos de pago",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(pagos_disponibles_tienda) { pago ->
                                                Surface(
                                                    shape = RoundedCornerShape(50.dp),
                                                    color = MaterialTheme.colorScheme.background,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        Color.Gray.copy(alpha = 0.3f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(
                                                            horizontal = 12.dp,
                                                            vertical = 6.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            6.dp
                                                        )
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = pago.img),
                                                            contentDescription = pago.texto,
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                        )
                                                        texto_generico_one_line(
                                                            pago.texto.capitalizeFirst(),
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (pagos_disponibles_tienda.isNotEmpty() &&
                                    comodidades_disponibles_tienda.isNotEmpty()
                                ) {
                                    Divider(color = Color.Gray.copy(alpha = 0.15f))
                                }

                                // ── COMODIDADES ──────────────────────────
                                if (comodidades_disponibles_tienda.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Widgets,
                                                contentDescription = null,
                                                tint = Color(0xFF2196F3),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            texto_generico_one_line(
                                                "Comodidades disponibles",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(comodidades_disponibles_tienda) { comodidad ->
                                                Surface(
                                                    shape = RoundedCornerShape(50.dp),
                                                    color = MaterialTheme.colorScheme.background,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        Color.Gray.copy(alpha = 0.3f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(
                                                            horizontal = 12.dp,
                                                            vertical = 6.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            6.dp
                                                        )
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = comodidad.img),
                                                            contentDescription = comodidad.texto,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        texto_generico_one_line(
                                                            comodidad.texto.replace("_", " ")
                                                                .capitalizeFirst(),
                                                            style = MaterialTheme.typography.bodySmall
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

                item {
                    texto_generico_one_line("Selecciona múltiples filtros")
                    spacer_vertical(10.dp)

                    if (!verTodos) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                val todosSeleccionado = subcategoriasSeleccionadas.isEmpty() &&
                                        rango_precio.isNullOrEmpty()
                                chisp_filtrado_busqueda(
                                    carta_selecionada = todosSeleccionado,
                                    filtrado = "Todos",
                                    btn_visible = false,
                                    clik_card = {
                                        viewModel.limpiarSubcategorias()
                                        viewModel.limpiarRangoPrecio()
                                    },
                                    onClick_delete = {}
                                )
                            }
                            items(subcategoriasVisibles) { tag ->
                                val seleccionado = subcategoriasSeleccionadas.contains(tag)
                                chisp_filtrado_busqueda(
                                    carta_selecionada = seleccionado,
                                    filtrado = tag.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = { viewModel.toggle_subcategoria(tag) },
                                    onClick_delete = {}
                                )
                            }
                            if (lista_filtrado_negocio.size > limite) {
                                item {
                                    chisp_filtrado_busqueda(
                                        carta_selecionada = false,
                                        filtrado = "Ver todos",
                                        btn_visible = false,
                                        clik_card = { verTodos = true },
                                        onClick_delete = {}
                                    )
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            maxItemsInEachRow = 3,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val todosSeleccionado = subcategoriasSeleccionadas.isEmpty() &&
                                    rango_precio.isNullOrEmpty()
                            chisp_filtrado_busqueda(
                                carta_selecionada = todosSeleccionado,
                                filtrado = "Todos",
                                btn_visible = false,
                                clik_card = {
                                    viewModel.limpiarSubcategorias()
                                    viewModel.limpiarRangoPrecio()
                                },
                                onClick_delete = {}
                            )
                            lista_filtrado_negocio.forEach { tag ->
                                val seleccionado = subcategoriasSeleccionadas.contains(tag)
                                chisp_filtrado_busqueda(
                                    carta_selecionada = seleccionado,
                                    filtrado = tag.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = { viewModel.toggle_subcategoria(tag) },
                                    onClick_delete = {}
                                )
                            }
                            chisp_filtrado_busqueda(
                                carta_selecionada = false,
                                filtrado = "Ver menos",
                                btn_visible = false,
                                clik_card = { verTodos = false },
                                onClick_delete = {}
                            )
                        }
                    }
                }

                item {
                    spacer_vertical(10.dp)
                    texto_generico_one_line("Rangos de precio(s) disponibles")
                    spacer_vertical(10.dp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(rangos_disponibles) { rango ->
                            val seleccionado = rango_precio == rango
                            chisp_filtrado_busqueda_resultados_busqueda(
                                "precio",
                                carta_selecionada = seleccionado,
                                filtrado = rango.capitalizeFirst(),
                                btn_visible = false,
                                clik_card = { viewModel.setearRangoPrecioDesdeNLP(rango) },
                                onClick_delete = {}
                            )
                        }
                    }
                    spacer_vertical(10.dp)
                }

                // 🔥 Botón solo visible cuando hubo un cambio real
                item {
                    AnimatedVisibility(visible = huboCambio) {
                        Button(
                            onClick = {
                                val partes = mutableListOf<String>()
                                if (subcategoriasSeleccionadas.isNotEmpty()) {
                                    partes.add(" ${subcategoriasSeleccionadas.joinToString(", ")}")
                                }
                                if (!rango_precio.isNullOrEmpty()) {
                                    partes.add(" S/ $rango_precio")
                                }
                                if (metodo_pago.isNotEmpty()) {                              // 🔥
                                    partes.add("💳 ${metodo_pago.joinToString(", ")}")       // 🔥
                                }                                                             // 🔥
                                if (comodidad_selet.isNotEmpty()) {                          // 🔥
                                    partes.add("🛋️ ${comodidad_selet.joinToString(", ")}") // 🔥
                                }                                                             // 🔥
                                val mensaje = if (partes.isEmpty()) {
                                    "Mostrando todas las promos de $nombre_tienda"
                                } else {
                                    "Buscando: ${partes.joinToString(" · ")}"
                                }
                                onAplicarFiltro(mensaje)
                                viewModel.filtrar_promos_de_tienda(id_tienda)
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            texto_generico_one_line("Aplicar filtros de $nombre_tienda")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadioCheckingMetodos(
    lista: List<img_con_texto>,
    seleccionados: Set<String>,
    onToggle: (String) -> Unit
) {
    val colorMatrix = remember {
        ColorMatrix().apply { setToSaturation(0f) }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(lista) { item ->

            val seleccionado = seleccionados.contains(item.texto)

            Surface(
                onClick = { onToggle(item.texto) },
                shape = RoundedCornerShape(50),
                color = if (seleccionado)
                    Color.White
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {

                    // 🔹 Imagen
                    Image(
                        painter = painterResource(id = item.img),
                        contentDescription = item.texto,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        colorFilter = if (seleccionado) {
                            null
                        } else {
                            ColorFilter.colorMatrix(colorMatrix)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 🔹 Texto
                    Text(
                        text = item.texto.capitalizeFirst(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (seleccionado)
                            Color.Black // 🔥 texto blanco cuando fondo es morado
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun chisp_filtrado_busqueda_resultados_busqueda(
    tipo: String,
    carta_selecionada: Boolean,
    filtrado: String,
    btn_visible: Boolean = true,
    clik_card: () -> Unit,
    onClick_delete: () -> Unit,
    color_invertido: Boolean = false,
    alto: Dp = 45.dp,
) {

    val color_chips by animateColorAsState(
        targetValue = if (!carta_selecionada) if (tipo == "precio") {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.primary
        }
        else Color.White, animationSpec = tween(
            durationMillis = 500, easing = LinearOutSlowInEasing
        ), label = ""
    )

    val color_invertido_chips by animateColorAsState(
        targetValue = if (!carta_selecionada) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            if (tipo == "precio") {
                Color.Gray
            } else {
                MaterialTheme.colorScheme.primary
            }
        }
    )


    val color_text = if (!carta_selecionada) Color.White else Color.Black
    val color_text_ivnertido =
        if (color_invertido && !carta_selecionada) Color.Black else Color.White

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (!color_invertido) color_chips else color_invertido_chips)
            .height(alto)
            .padding(horizontal = 15.dp, vertical = 10.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { clik_card() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        texto_generico_one_line(
            filtrado.capitalizeFirst(),
            color = if (!color_invertido) color_text else color_text_ivnertido,
            style = MaterialTheme.typography.bodyMedium
        )

        if (btn_visible) {
            spacer_horizonta(7.dp)
            btn_close_gris(
                imageVector = Icons.Default.Close,
                onClick = { onClick_delete() },
                size_container = 20.dp,
                size_icon = 15.dp,
                tint_icon = if (!carta_selecionada) Color.White else Color.Black
            )
        }


    }

}

fun obtenerRangoDesdePrecio(precio: Int?): String? {
    if (precio == null) return null

    return when (precio) {
        in 0..10 -> "0 - 10"
        in 11..20 -> "10 - 20"
        in 21..30 -> "20 - 30"
        in 31..50 -> "30 - 50"
        in 51..80 -> "50 - 80"
        in 81..120 -> "80 - 120"
        in 121..200 -> "120 - 200"
        in 201..350 -> "200 - 350"
        in 351..500 -> "350 - 500"
        in 501..1000 -> "500 - 1000"
        in 1001..2500 -> "1000 - 2500"
        in 2501..5000 -> "2500 - 5000"
        in 5001..10000 -> "5000 - 10000"
        else -> "Más de 10000"
    }
}

@Composable
fun desing_chips_texto_filtrado(
    texto_principal: String,
    lista: List<img_con_texto>,
    seleccionados: Set<String>,
    selecionado: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
    texto_generico_one_line(texto_principal)
    RadioCheckingMetodos(
        lista = lista,
        seleccionados = seleccionados,
        onToggle = { texto ->
            selecionado (texto) }
    )
    }
}
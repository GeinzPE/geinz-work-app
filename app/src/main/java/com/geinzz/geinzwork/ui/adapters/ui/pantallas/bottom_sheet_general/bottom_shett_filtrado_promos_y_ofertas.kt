package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_filtrados_promos_y_ofertas(
    comodidad_selet: Set<String>, metodo_pago: Set<String>,
    rango_precio: String?,
    viewModel: viewmodel_promos_cercanas,
    onClose: () -> Unit
) {

    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    val categorias by viewModel._categoriasDisponibles.collectAsState()

    val obtener_datos_respuesta_gemini by viewModel.respuesta_gemini.collectAsState()
    var listaData by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(obtener_datos_respuesta_gemini) {

        listaData = when (val estado = obtener_datos_respuesta_gemini) {

            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.succes -> {

                estado.items?.run {
                    buildList {
                        principal?.let(::add)
                        addAll(atributos)
                    }
                } ?: emptyList()
            }

            else -> emptyList()
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
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
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 10.dp)
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
                        "Busca promociones y ofertas solo apra ti en todo barranca",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(10.dp)
                }
//                item {
//                    val subcategorias = listOf("Todos") + promos
//                        .flatMap { categorias }
//                        .distinct()
//                    texto_generico_one_line("Categoria")
//                    LazyRow(
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                contentPadding = PaddingValues(horizontal = 10.dp)
//                            ) {
//                                items(subcategorias) { subcategoria ->
//                                    val seleccionado = subCategoriaSeleccionada == subcategoria
//                                    chisp_filtrado_busqueda(
//                                        carta_selecionada = seleccionado,
//                                        filtrado = subcategoria.capitalizeFirst(),
//                                        btn_visible = false,
//                                        clik_card = {
//                                            subCategoriaSeleccionada = subcategoria
//                                            tiendaSeleccionada = null
//                                            if (subcategoria != "Todos") {
//
//                                            }
//                                        },
//                                        onClick_delete = {}
//                                    )
//                                }
//                            }
//                }
                if (listaData.isNotEmpty()) {
                    item {
                        texto_generico_one_line("Resultados de tu busqueda")
                        spacer_vertical(10.dp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(listaData) { i ->
                                val seleccionado = rango_precio == i
                                chisp_filtrado_busqueda(
                                    carta_selecionada = false,
                                    filtrado = i.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = {

                                    },
                                    onClick_delete = {}
                                )
                            }
                        }
                    }
                }

                item {
                    texto_generico_one_line("Rangos de Precio")
                    spacer_vertical(10.dp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(rango_precios) { subcategoria ->
                            val seleccionado = rango_precio == subcategoria
                            chisp_filtrado_busqueda(
                                carta_selecionada = seleccionado,
                                filtrado = subcategoria.capitalizeFirst(),
                                btn_visible = true,
                                clik_card = {
                                    viewModel.setearRangoPrecioDesdeNLP(subcategoria)
                                },
                                onClick_delete = {}
                            )
                        }
                    }
                    spacer_vertical(10.dp)
                }

                item {
                    texto_generico_one_line("Metodos de pago")
                    spacer_vertical(10.dp)
                    RadioCheckingMetodos(
                        lista = lista_filtrados_pagos,
                        seleccionados = metodo_pago,
                        onToggle = { texto ->
                            viewModel.toggleMetodoPago(texto)
                        }
                    )
                    spacer_vertical(10.dp)
                }

                item {
                    texto_generico_one_line("Comodidades")
                    spacer_vertical(10.dp)
                    RadioCheckingMetodos(
                        lista = lista_comodidades,
                        seleccionados = comodidad_selet,
                        onToggle = { texto ->
                            viewModel.togleRango_select(texto)
                        }
                    )
                    spacer_vertical(10.dp)
//                    LazyRow(
//                        horizontalArrangement = Arrangement.spacedBy(10.dp),
//                        contentPadding = PaddingValues(horizontal = 10.dp)
//                    ) {
//                        items(lista_comodidades) { subcategoria ->
//
////                            val seleccionado = comodidad_selet.contains(subcategoria)
//
//                            chisp_filtrado_busqueda(
//                                carta_selecionada = false,
//                                filtrado = subcategoria.capitalizeFirst(),
//                                btn_visible = false,
//                                clik_card = {
////                                    viewModel.togleRango_select(subcategoria)
//                                },
//                                onClick_delete = {}
//                            )
//                        }
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
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.TiendasCercanasFiltrada
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_turismo_google_mpa
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.flyTo
import kotlinx.coroutines.launch
import kotlin.math.roundToInt



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun bottom_sheet_mapa(
    lista_categiras_filtrado_tiendas_Cercanas: List<String>,
    viewmodelMapa: viewModel_lugares_turisticos,
    estado: TiendasCercanasFiltrada,
    seleccionadoId: String,
    lat_user: Double,
    log_user: Double,
    mapboxMap: com.mapbox.maps.MapboxMap,
    tipo: String,
    lista_filtrada_turismo: List<lugares_cercanos>,
    lista: List<tiendas_por_categoria>,
    onclose: () -> Unit,
    selecionado_id: (String?) -> Unit,
    datos_selecionado_retornar: (dataclass_map) -> Unit
) {

    var estadoFiltro by remember {
        mutableStateOf(
            TiendasCercanasFiltrada(
                estado.categoriaFiltrada,
                estado.radioFiltrado,
                lista_categiras_filtrado_tiendas_Cercanas,
                estado.listaCompleta,
                estado.lugar_lat,
                estado.lugar_lng
            )
        )
    }
    Log.d(
        "estadolistapasble",
        """
    ================== ESTADO FILTRO ==================
    🗂 Lista categorías   : ${estadoFiltro.listaCategorias.size}
      ====================================================
    """.trimIndent()
    )



    ModalBottomSheet(
        onDismissRequest = { onclose() },
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth()
        ) {
            FuenteControladaApp {
                when (tipo) {
                    "turismo" -> {
                        listado_items(
                            lista_categiras_filtrado_tiendas_Cercanas,
                            viewmodelMapa = viewmodelMapa,
                            teindas_cercanas_fitrada = estadoFiltro,
                            tipo = "turismo",
                            seleccionadoId = seleccionadoId,
                            mapboxMap = mapboxMap,
                            lista = lista_filtrada_turismo,
                            getId = { it.id_tienda },
                            getLat = { it.latitud },
                            getLng = { it.longitud },
                            getLogo = { it.logo_tienda },
                            getNombre = { it.nombre_tienda },
                            getDescripcion = { it.descripcion },
                            selecionado = { id ->
                                selecionado_id(id.id_tienda)
                                datos_selecionado_retornar(
                                    dataclass_map(
                                        img = id.logo_tienda,
                                        nombre = id.nombre_tienda,
                                        tag = id.lista_subcategoiras,
                                        my_latitud = id.latitud,
                                        my_longitud = log_user,
                                        latitud = id.latitud,
                                        longitud = id.longitud,
                                        id = id.id_tienda,
                                        categoria = "",
                                        direccion = id.direccion,
                                        referencia = id.referencia,
                                        contacto_tienda = id.contacto_tienda,
                                        metodos_pago_tienda = id.metodos_pago_tienda,
                                        horario_box = id.horario_box
                                    )
                                )
                            }, estados_guardado = { categoria, radio ->
                                estadoFiltro = estadoFiltro.copy(
                                    categoriaFiltrada = categoria,
                                    radioFiltrado = radio
                                )
                            }
                        )

                    }

                    "tiendas" -> {
                        listado_items(
                            lista_categiras_filtrado_tiendas_Cercanas,
                            viewmodelMapa = viewmodelMapa,
                            teindas_cercanas_fitrada = TiendasCercanasFiltrada(),
                            tipo = "tiendas",
                            seleccionadoId = seleccionadoId,
                            mapboxMap = mapboxMap,
                            lista = lista,
                            getId = { it.id_tienda },
                            getLat = { it.latitud },
                            getLng = { it.longitud },
                            getLogo = { it.logo_tienda },
                            getNombre = { it.nombre_tienda },
                            getDescripcion = { it.descripcion }, selecionado = { id ->
                                selecionado_id(id.id_tienda)
                                datos_selecionado_retornar(
                                    dataclass_map(
                                        img = id.logo_tienda,
                                        nombre = id.nombre_tienda,
                                        tag = id.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = id.latitud,
                                        longitud = id.longitud,
                                        id = id.id_tienda,
                                        categoria = "",
                                        direccion = id.direccion,
                                        referencia = id.referencia,
                                        contacto_tienda = id.contacto_tienda,
                                        metodos_pago_tienda = id.metodos_pago_tienda,
                                        horario_box = id.horario_tienda_box
                                    )
                                )
                            }
                        ) { _, _ -> }
                    }

                    else -> {}
                }

            }
        }

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> listado_items(
    lista_categiras_filtrado_tiendas_Cercanas: List<String>,
    viewmodelMapa: viewModel_lugares_turisticos,
    teindas_cercanas_fitrada: TiendasCercanasFiltrada,
    tipo: String,
    seleccionadoId: String,
    mapboxMap: com.mapbox.maps.MapboxMap,
    lista: List<T>,
    getId: (T) -> String,
    getLat: (T) -> Double,
    getLng: (T) -> Double,
    getLogo: (T) -> String,
    getNombre: (T) -> String,
    getDescripcion: (T) -> String,
    selecionado: (T) -> Unit,
    estados_guardado: (categoira: String, filtrado: Double) -> Unit
) {
    val scope = rememberCoroutineScope()
    var sub_categoria_selecionada by rememberSaveable {
        mutableStateOf(teindas_cercanas_fitrada.categoriaFiltrada)
    }
    var mostrar_filtrado by rememberSaveable {
        mutableStateOf(false)
    }
    var nuevo_rango_busqueda by rememberSaveable {
        mutableStateOf(teindas_cercanas_fitrada.radioFiltrado.toFloat())
    }
    LaunchedEffect(sub_categoria_selecionada) {
        viewmodelMapa.filtrar_por_subcategoria(
            lista_categiras_filtrado_tiendas_Cercanas,
            sub_categoria_selecionada,
            teindas_cercanas_fitrada.lugar_lat, teindas_cercanas_fitrada.lugar_lng,
            nuevo_rango_busqueda
        )
        estados_guardado(sub_categoria_selecionada, nuevo_rango_busqueda.toDouble())
        viewmodelMapa.actualizarCategoria(sub_categoria_selecionada)
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            val header = if (tipo == "turismo") {
                stringResource(id = R.string.MAPA_HEADER_TXT_TURISMO)
            } else {
                stringResource(id = R.string.MAPA_HEADER_TXT_TIENDAS)
            }

            val texto = if (tipo == "turismo") {
                stringResource(id = R.string.MAPA_DESC_TXT_TURISMO)
            } else {
                stringResource(id = R.string.MAPA_DESC_TXT_TIENDAS)
            }
            Column {
                Text(
                    text = header,
                    fontFamily = baners_geinz_work,
                    fontSize = 24.sp
                )
                spacer_vertical(10.dp)
                if (tipo == "turismo") {
                    // 🔹 Texto clickeable (ajustar los filtros)
                    val partes = texto.split("ajustar los filtros")

                    val annotatedText = buildAnnotatedString {
                        append(partes.firstOrNull() ?: texto)

                        pushStringAnnotation(tag = "filtros", annotation = "abrir_filtros")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,

                                )
                        ) {
                            append("ajustar los filtros")
                        }
                        pop()

                        if (partes.size > 1) {
                            append(partes[1])
                        }
                    }

                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations("filtros", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    if (annotation.item == "abrir_filtros") {
                                        mostrar_filtrado = !mostrar_filtrado
                                        Log.d("Mapa", "Abrir filtros clickeado")
                                        // aquí puedes poner abrirBottomSheetFiltros()
                                    }
                                }
                        }
                    )
                } else {
                    // 🔹 Texto normal
                    texto_generico_multilinea(
                        texto,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                spacer_vertical(10.dp)
                AnimatedVisibility(mostrar_filtrado) {
                    if (teindas_cercanas_fitrada != TiendasCercanasFiltrada()) {

                        Column {
                            texto_generico_multilinea(
                                "Rango aproximado de búsqueda: ${
                                    constantes_lista_localidades.formatRadioFromSlider(
                                        nuevo_rango_busqueda
                                    )
                                }",
                                MaterialTheme.typography.bodyMedium
                            )
                            spacer_vertical(10.dp)

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                val listaConTodos = lista_categiras_filtrado_tiendas_Cercanas
                                items(listaConTodos) { i ->
                                    val selecionado = sub_categoria_selecionada == i
                                    chisp_filtrado_busqueda(
                                        selecionado,
                                        simplificarCategoria(i),
                                        false,
                                        clik_card = {
                                            if (!selecionado) {
                                                if (i == "Todos") {
                                                    sub_categoria_selecionada = "Todos"
                                                } else {
                                                    sub_categoria_selecionada = i
                                                }
                                            }
                                        },
                                        onClick_delete = {},
                                        true,
                                        40.dp
                                    )

                                }
                            }

                            spacer_vertical(10.dp)
                            Slider(
                                value = nuevo_rango_busqueda,
                                onValueChange = {
                                    nuevo_rango_busqueda = it.roundToInt().toFloat()
                                },
                                valueRange = 1f..10f,
                                steps = 8,
                                onValueChangeFinished = {
                                    viewmodelMapa.filtrar_por_subcategoria(
                                        lista_categiras_filtrado_tiendas_Cercanas,
                                        sub_categoria_selecionada,
                                        teindas_cercanas_fitrada.lugar_lat,
                                        teindas_cercanas_fitrada.lugar_lng,
                                        nuevo_rango_busqueda
                                    )
                                    viewmodelMapa.actualizarRadio(nuevo_rango_busqueda.toDouble())
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,       // 🔹 Color del "thumb" o bolita que se mueve cuando arrastras el slider
                                    activeTrackColor = MaterialTheme.colorScheme.primary, // 🔹 Color de la línea activa del slider (la parte a la izquierda del thumb)
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.2f
                                    ), // 🔹 Color de la línea inactiva (parte a la derecha del thumb)
                                    activeTickColor = MaterialTheme.colorScheme.primary,  // 🔹 Color de las marcas de pasos (ticks) que ya están "alcanzadas" por el thumb
                                    inactiveTickColor = Color.Gray                        // 🔹 Color de las marcas de pasos que aún no se alcanzaron
                                ),
                                thumb = {
                                    // Nuestra bolita blanca sin borde negro
                                    Box(
                                        modifier = Modifier
                                            .size(25.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        texto_generico_one_line(
                                            nuevo_rango_busqueda.toInt()
                                                .toString(),
                                            color = Color.Black,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            )


                        }


                    }
                }

            }

        }
        if (lista.size != 0) {
            itemsIndexed(lista, key = { _, item -> getId(item) }) { index, item ->
                Log.d("tamodasdalista", "${lista.size}")
                carta_turismo_google_mpa(
                    index,
                    getId(item),
                    getLat(item),
                    getLng(item),
                    getLogo(item),
                    getNombre(item),
                    getDescripcion(item),
                    seleccionado = (seleccionadoId == getId(item))
                ) { id, lat, log ->
                    val nuevaUbicacion = LatLng(lat, log)

                    selecionado(item)
                    scope.launch {
                        mapboxMap.flyTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(log, lat))
                                .zoom(16.0)
                                .build()
                        )
//                        cameraPositionState.animate(
//                            CameraUpdateFactory.newLatLngZoom(nuevaUbicacion, 16f),
//                            1000
//                        )
                    }
                }
            }
        } else {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 500.dp), // Ajusta el alto que quieras
                    contentAlignment = Alignment.Center
                ) {
                    texto_generico_one_line(
                        texto = "No se encontraron resultados en este rango",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }




        }

    }

}

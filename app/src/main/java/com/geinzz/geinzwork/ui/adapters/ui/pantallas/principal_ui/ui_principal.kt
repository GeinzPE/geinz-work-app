package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_resultado_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.cargando_progess_mas_texto
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.estados_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.existencia_dato
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.floatin_actionButton
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_activos_encontrados
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_shet_patrocinadores
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cargando_categorias
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.amarillo30
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.localizate_geinz.abrirRutaEnGoogleMaps
import com.geinzz.geinzwork.utils.localizate_geinz.normalizarTexto
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.mapNotNull
import kotlin.collections.orEmpty
import kotlin.text.contains

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExplorarTiendas(
    localidadUser: String,
    nombreUser: String,
    viewModel: viewModel_localizate_geinz,
    clik_img: (categoria: String, localidad: String, nombre_user: String) -> Unit
) {
    val lista = remember { mutableStateListOf<encontradas_por_categoria>() }
    var texto_filtrado by rememberSaveable { mutableStateOf("") }
    val composision by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cargando_categorias))
    val lista_filtrada = remember { mutableStateListOf<encontradas_por_categoria>().apply { addAll(lista) } }
    val cargando = remember { mutableStateOf(true) }
    val encontrados_activos_tiendass by viewModel.encontrados_activos_tiendas.observeAsState()
    val lista_localidades = constantes_lista_localidades.lista
    val cartaExpandida = remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    var mostrar_fab by remember { mutableStateOf(false) }
    mostrar_fab = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 10
    val coroutineScope = rememberCoroutineScope()
    val localidadSeleccionada = remember { mutableStateOf("") }

    val primeraVez = remember { mutableStateOf(true) }
    var fraces_localidad by remember { mutableStateOf(listOf("Espere un momento...")) }

    // Primera carga
    LaunchedEffect(Unit) {
        if (localidadSeleccionada.value.isEmpty()) {
            localidadSeleccionada.value = localidadUser
            fraces_localidad = viewModel.obtenerFrasesCarga(localidadUser, nombreUser)
        }
    }

    // Cambio de localidad
    LaunchedEffect(localidadSeleccionada.value) {
        if (primeraVez.value) {
            primeraVez.value = false
        } else {
            cargando.value = true
            cartaExpandida.value = null
            viewModel.T_obtener_registrados_activos(localidadSeleccionada.value)
            fraces_localidad = viewModel.obtenerFrasesCarga(localidadSeleccionada.value, nombreUser)
            mostrar_fab = false
        }
    }

    // Respuesta de las tiendas
    LaunchedEffect(encontrados_activos_tiendass) {
        encontrados_activos_tiendass?.let { listaNueva ->
            lista.clear()
            lista.addAll(listaNueva)
            texto_filtrado = ""
            cargando.value = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedVisibility(mostrar_fab) {
                floatin_actionButton(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    R.drawable.arrow_subir_vector,
                    null
                ) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index = 0)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Crossfade(targetState = cargando.value) { isCargando ->
            if (isCargando && fraces_localidad.isNotEmpty()) {
                cargando_categorias(
                    composision,
                    localidadSeleccionada.value, // ✅ ahora usa la seleccionada
                    5.dp,
                    fraces_localidad
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    state = listState
                ) {
                    item {
                        cabezero_activity(localidadSeleccionada.value) // ✅ lo mismo aquí
                    }
                    stickyHeader {
                        ColumnContenedorComun {
                            FiltradosChipsLocalidades(
                                lista_localidades,
                                localidadSeleccionada.value
                            ) { nuevaLocalidad ->
                                localidadSeleccionada.value = nuevaLocalidad
                            }
                            filtrado_texto(
                                viewModel,
                                texto_filtrado,
                                lista,
                                { texto_filtrado = it },
                                { nuevaLista, _ ->
                                    lista_filtrada.clear()
                                    lista_filtrada.addAll(nuevaLista)
                                    if (texto_filtrado.length > 2) cartaExpandida.value = null
                                }
                            )
                        }
                    }
                    val listaParaMostrar =
                        if (texto_filtrado.length > 2) lista_filtrada else lista
                    items(
                        listaParaMostrar,
                        key = { it.categoria ?: it.hashCode().toString() }
                    ) { item ->
                        cartas_categorias(
                            nombreUser,
                            item,
                            cartaExpandida,
                            localidadSeleccionada.value,
                            viewModel,
                            clik_img
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun cabezero_activity(localidad_registrado: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
    ) {
        titulos_genericos_one_line(
            "Ubicate $localidad_registrado", MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
        )
        spacer_vertical(5.dp)
        texto_generico_multilinea(
            "Explora las diferentes categorías de tiendas\n" +
                    "registradas en Geinz Work y ubícate fácilmente en $localidad_registrado",
            MaterialTheme.typography.bodyMedium
        )

    }
}

@ExperimentalMaterial3Api
@Composable
fun FiltradosChipsLocalidades(
    lista_localidades: List<dataclass_localidad_escudos>,
    localidadSeleccionada: String,
    onLocalidadSeleccionada: (String) -> Unit
) {
    LazyRow(modifier = Modifier.padding(top = 5.dp)) {
        items(lista_localidades) { localidad ->
            val isSelected =
                localidadSeleccionada.equals(localidad.nombre_localidad, ignoreCase = true)
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = null,
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onLocalidadSeleccionada(localidad.nombre_localidad.toString())
                    }
                },

                label = {
                    Text(
                        text = localidad.nombre_localidad.toString(),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                },
                trailingIcon = {
                    localidad.escudo_img?.let { imgResId ->
                        Image(
                            painter = painterResource(id = imgResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(40)

            )
        }
    }
}


@Composable
fun filtrado_texto(
    viewModel: viewModel_localizate_geinz,
    texto: String,
    lista_cargada_filstrado: List<encontradas_por_categoria>,
    texto_filtrado: (String) -> Unit,
    busquedaAction: (List<encontradas_por_categoria>, Boolean) -> Unit
) {
    var lastInputTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    var is_error by remember { mutableStateOf(false) }
    var icono_busqeuda by remember { mutableStateOf(R.drawable.buscar_icon) }
    val sugerencias: List<dataclass_resultado_filtrado> = lista_cargada_filstrado
        .flatMap { catSub ->
            catSub.subcateogiras.orEmpty().mapNotNull { subcat ->
                if (subcat.contains(texto, ignoreCase = true) && texto.isNotBlank()) {
                    dataclass_resultado_filtrado(catSub.categoria.toString(), subcat)
                } else null
            }
        }
    is_error = expanded && sugerencias.isEmpty()
    LaunchedEffect(lastInputTime) {
        delay(2000L)
        val tiempoInactivo = System.currentTimeMillis() - lastInputTime
        if (tiempoInactivo >= 5000L) {
            focusManager.clearFocus()
            expanded = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            custom_texFiel(
                value = texto,
                onValueChange = {
                    texto_filtrado(it)
                    expanded = it.isNotBlank()
                    lastInputTime = System.currentTimeMillis()
                    if (it.isNotBlank()) {
                        icono_busqeuda = R.drawable.vector_eliminar_texto_texfiel
                        busquedaAction(
                            viewModel.obtenerResultados(it, lista_cargada_filstrado),
                            true
                        )
                    } else {
                        icono_busqeuda = R.drawable.buscar_icon
                    }
                },
                labelText = "Ingrese la subcategoría a buscar",
                placeholderText = "Ingrese la subcategoría a buscar",
                trailingIcon = {
                    if (icono_busqeuda == R.drawable.vector_eliminar_texto_texfiel) {
                        IconButton(onClick = {
                            texto_filtrado("")
                            busquedaAction(emptyList(), false)
                            expanded = false
                            icono_busqeuda = R.drawable.buscar_icon
                        }) {
                            Icon(
                                painter = painterResource(id = icono_busqeuda),
                                contentDescription = "Eliminar texto"
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = icono_busqeuda),
                            contentDescription = "Buscar por subcategoría"
                        )
                    }
                },
                isError = is_error,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        busquedaAction(
                            viewModel.obtenerResultados(texto, lista_cargada_filstrado),
                            true
                        )
                        expanded = false
                        focusManager.clearFocus()
                    }
                ),
            )
            if (is_error) {
                existencia_dato()
            }
        }


    }
}

@Composable
fun cartas_categorias(
    nombre_user: String,
    item: encontradas_por_categoria,
    cartaExpandida: MutableState<String?>,
    Localidad_selecionada: String,
    viewModel: viewModel_localizate_geinz,
    clik_img: (categoria: String, localidad: String, nombre: String) -> Unit
) {
    val categoriaKey = item.categoria?.let { normalizarTexto(it) } ?: return
    val expandido = cartaExpandida.value == categoriaKey
    val categoriaYaLlamada = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(expandido) {
        if (expandido) {
            categoriaYaLlamada.value = categoriaKey
            viewModel.T_patrocinadas(localidad = Localidad_selecionada, categoria = categoriaKey)
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.img_subcategorias)
                        .crossfade(true)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.sin_item_carrito)
                        .build(),
                    contentDescription = "Imagen de la tienda",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            clik_img(item.categoria, Localidad_selecionada, nombre_user)
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {

                    activos_y_registrados(
                        item.cantidad_registradas ?: 0, item.activas ?: 0,
                        modifier = Modifier
                    )
                    floatin_actionButton(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .align(Alignment.BottomEnd),
                        constantes_lista_localidades.cambiar_icono_exapndible(expandido),
                        onClick = {
                            cartaExpandida.value = if (expandido) null else categoriaKey
                        }
                    )
                }
            }
            obtener_patrocinados(item, expandido, viewModel, Localidad_selecionada)
        }
    }
}


@Composable
fun obtener_patrocinados(
    item: encontradas_por_categoria,
    expandido: Boolean,
    viewModel: viewModel_localizate_geinz, localidad: String
) {
    val listaPatrocinados = remember { mutableStateListOf<tiendas_filtradas>() }
    val tiendas_patrocinadas_por_categoria by viewModel.T_patrocinadas_por_categoria.observeAsState()
    LaunchedEffect(tiendas_patrocinadas_por_categoria, expandido) {
        if (expandido && tiendas_patrocinadas_por_categoria != null) {
            val inicio = System.currentTimeMillis()
            listaPatrocinados.clear()
            tiendas_patrocinadas_por_categoria!!.forEach {
                listaPatrocinados.add(
                    tiendas_filtradas(
                        logo_tienda = it.logo_tienda,
                        img_tienda = it.img_tienda,
                        nombre_tienda = it.nombre_tienda,
                        direccion = it.direccion,
                        referencia = it.referencia,
                        longitud = it.longitud,
                        descripcion = it.descripcion,
                        id_tienda = it.id_tienda,
                        latitud = it.latitud,
                        lista_subcategoiras = it.lista_subcategoiras,
                        whatsapp = it.whatsapp,
                        numero_whatsapp = it.numero_whatsapp,
                        tiktok = it.tiktok,
                        nombre_tiktok = it.nombre_tiktok,
                        sitio_web = it.sitio_web,
                        url_sitio_web = it.url_sitio_web,
                        instagram = it.instagram,
                        nombre_user_ig = it.nombre_user_ig,
                        facebook = it.facebook,
                        nombre_user_fb = it.nombre_user_fb,
                    )
                )
            }
            val tiempoTranscurrido = System.currentTimeMillis() - inicio
            val tiempoRestante = 1000 - tiempoTranscurrido
            if (tiempoRestante > 0) delay(tiempoRestante)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        AnimatedVisibility(visible = expandido) {
            Column(modifier = Modifier.fillMaxWidth()) {
                spacer_vertical(10.dp)
                texto_generico_one_line(
                    "Subcategorias encontradas",
                    MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )

                Box(modifier = Modifier.padding(10.dp)) {
                    tags_subcateogiras(item.subcateogiras)
                }

                ListaTiendasPatrocinadas(
                    viewModel = viewModel,
                    tiendas = listaPatrocinados,
                    categoria = item.categoria.toString(),
                    localidad = localidad
                )
            }
        }
    }

}


@Composable
fun ListaTiendasPatrocinadas(
    viewModel: viewModel_localizate_geinz,
    tiendas: List<tiendas_filtradas>, categoria: String, localidad: String
) {
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val estado_tiendas by viewModelFiltros.estadoTiendas.observeAsState()
    var estadoColorSeleccionado by remember { mutableStateOf(Color.Gray) }


    tiendas.forEach { i ->
        viewModelFiltros.obtenerHorarioPorTienda_activa(
            localidad,
            i.id_tienda
        )
    }

    val isLoading by viewModel.loading
    val context = LocalContext.current
    val mostrarDialogo = remember { mutableStateOf(false) }
    val mostrarDialog_sin_google_maps = remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var show_boottom_sheet_dialog by remember { mutableStateOf(false) }
    var tiendaSeleccionada by remember { mutableStateOf(tiendas_filtradas()) }

    if (mostrarDialogo.value) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                mostrarDialogo.value = false
            },
            abrir_configuracion = {
                mostrarDialogo.value = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                mostrarDialogo.value = false
                mostrarDialog_sin_google_maps.value = true
            })
    }
    if (mostrarDialog_sin_google_maps.value) {
        dialog_sin_ubi_activa(
            direccion, referencia, onDismis = { mostrarDialog_sin_google_maps.value = false },
            abrir_maps = { constantes.abrirGoogleMaps(context, direccion) })

    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                cargando_progess_mas_texto("Buscando tiendas patrocinadas")
            }

        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (tiendas.isNotEmpty()) {
                    texto_generico_one_line(
                        "Tiendas Patrocinadas",
                        MaterialTheme.typography.titleSmall, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    spacer_vertical(10.dp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        items(tiendas) { tienda ->
                            val estaAbierto = estado_tiendas?.get(tienda.id_tienda) == true
                            val estadoTexto = if (estaAbierto) "Abierto" else "Cerrado"
                            val estadoColor = if (estaAbierto) Color.Green else Color.Red
                            patrocinadores(
                                item = tienda,
                                estadoColor,
                                estadoTexto,
                                { latitud, longitud ->
                                    if (verificarUbiActiva(context)) {
                                        abrirRutaEnGoogleMaps(context, latitud, longitud)
                                    } else {
                                        mostrarDialogo.value = true
                                    }
                                    direccion = tienda.direccion ?: ""
                                    referencia = tienda.referencia ?: ""
                                }, { tiendas_filtradas, showBottomSheet ->
                                    estadoColorSeleccionado = estadoColor
                                    tiendaSeleccionada = tiendas_filtradas
                                    show_boottom_sheet_dialog = showBottomSheet
                                }
                            )
                        }
                    }

                    if (show_boottom_sheet_dialog) {
                        bottom_shet_patrocinadores(
                            estadoColorSeleccionado,
                            viewModelFiltros,
                            categoria,
                            localidad,
                            tiendaSeleccionada
                        ) { show_boottom_sheet_dialog = false }
                    }

                    spacer_vertical(10.dp)
                }
            }
        }
    }
}


@Composable
fun patrocinadores(
    item: tiendas_filtradas,
    estadoColor: Color,
    estadoTexto: String,
    abrir_maps: (Double, Double) -> Unit,
    listener_bottom_sheet: (item: tiendas_filtradas, mostrar: Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(220.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        AsyncImage(
            model = item.logo_tienda,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clickable {
                    listener_bottom_sheet(item, true)
                }
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)),
            placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
            error = painterResource(id = R.drawable.qr_yape)
        )
        spacer_vertical(5.dp)
        Row(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(.5f)) {
                texto_generico_one_line(item.nombre_tienda, MaterialTheme.typography.bodySmall)
                spacer_vertical(5.dp)
                estados_tiendas(estadoTexto, estadoColor)
            }

            Spacer(modifier = Modifier.width(3.dp))
            floatin_actionButton(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape), R.drawable.localidad_icon_general, null, onClick = {
                    abrir_maps(item.latitud, item.longitud)
                }
            )
        }
    }
}


@Composable
fun activos_y_registrados(
    cantidad_registrados: Int,
    cantidad_activos: Int,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(30),
    ) {
        Row(
            modifier = modifier
                .background(amarillo30)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            texto_activos_encontrados(
                modifier,
                "Tiendas Registradas : $cantidad_registrados",
                2.dp,
                Color.Black
            )
            texto_activos_encontrados(modifier, "Activos : $cantidad_activos", 5.dp, Color.Green)

        }
    }
}



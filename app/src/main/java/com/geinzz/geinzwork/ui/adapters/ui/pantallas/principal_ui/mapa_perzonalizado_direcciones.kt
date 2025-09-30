package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.algolia.search.dsl.ranking.DSLCustomRanking
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_mapa
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.actualizarUbicacion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.bitmapDescriptorFromDrawable
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isGpsActivo
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.lista_redes_tiendas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarDistanciaFormateada
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

@Composable
fun pantalla_mapa_perzonalizado(
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    tipo: String,
    localidad: String
) {
    Box() {
        MyGoogle_maps(
            tipo,
            viewmodel_lugares_turisticos,
            viewModel_filtrado_tiendas,
            viewmode_segurirdad_Salud,
            localidad
        )
    }

}

@SuppressLint("MissingPermission")
@Composable
fun MyGoogle_maps(
    tipo: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    localidad: String
) {
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coordenadas by viewmode_segurirdad_Salud.coordenadasSeleccionadas.observeAsState()
    var latitud_luga_seg by remember { mutableStateOf(0.0) }
    var long_luga_seg by remember { mutableStateOf(0.0) }
    var currentIndex by remember { mutableStateOf(0) }


    coordenadas?.let { (lat, lon) ->
        latitud_luga_seg = lat
        long_luga_seg = lon
    }
    val lista_filtrada_turismo by viewmodel_lugares_turisticos.listaFiltrada.collectAsState()
    val lista_filtrada_tiendas by viewModel_filtrado_tiendas.listaFiltrada.collectAsState()
    val horario_por_tienda by viewModel_filtrado_tiendas.estadoTiendas.observeAsState()
    val datosTienda by viewModel_filtrado_tiendas._datos_tienda.observeAsState(emptyList())


    var lister_marker by remember { mutableStateOf(dataclass_map()) }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    var show_botoom_sheet by remember { mutableStateOf(true) }
    var show_dialog_datos_lugares by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var log_user by remember { mutableStateOf(0.0) }
    var lat_user by remember { mutableStateOf(0.0) }

    val defaultLocation_barranca = LatLng(-10.8500, -77.7500)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation_barranca, 12f)
    }

    var boxVisible by remember { mutableStateOf(true) }

    var id_lugar_tienda_select by remember { mutableStateOf("") }
    var localidad_tienda_lugar_Select by remember { mutableStateOf(localidad) }
    var show_bottom_sheet_datos_tienda_lugares by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }


    LaunchedEffect(id_lugar_tienda_select) {
        viewModel_filtrado_tiendas.obtener_campos_tiendas_por_id(
            localidad_tienda_lugar_Select,
            id_lugar_tienda_select
        )
    }
    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }

    if (show_bottom_sheet_datos_tienda_lugares) {
        bottom_sheet_tiendas_filtradas(
            Color.Red,
            viewModel_filtrado_tiendas,
            dataclass_tienda_seleccionada, show_bottom_sheet_datos_tienda_lugares
        ) {
            show_bottom_sheet_datos_tienda_lugares = false
        }
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
            { validacion_mostrar_dialog_ubi_off = false },
            {
                validacion_mostrar_dialog_ubi_off = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                show_dialog_datos_lugares = false
            })
    }

    Box() {
        Column(modifier = Modifier.fillMaxSize()) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false
                )
            ) {

                when (tipo) {
                    "turismo" -> {
                        lista_filtrada_turismo.forEach { lugar ->
                            Log.d(
                                "obtenoemos_la_tog",
                                "user = ${lat_user} ${log_user}tusirmo= ${lugar.latitud} ${lugar.longitud}"
                            )
                            Marker(
                                state = MarkerState(LatLng(lugar.latitud, lugar.longitud)),
                                title = lugar.titulo,
                                icon =  MarkerIcon(context, seleccionadoId == lugar.id_lugar_turistico),
                                onClick = {
                                    lister_marker = dataclass_map(
                                        lugar.id_lugar_turistico,
                                        lugar.titulo,
                                        lugar.subcategoria_filtrado,
                                        lat_user,
                                        log_user,
                                        lugar.latitud,
                                        lugar.longitud,
                                        lugar.id_lugar_turistico,
                                        "",
                                        lugar.direcccion,
                                        lugar.referencia
                                    )
                                    seleccionadoId = lugar.id_lugar_turistico
                                    show_dialog_datos_lugares = true
                                    true
                                }
                            )
                        }
                    }

                    "tiendas" -> {
                        lista_filtrada_tiendas.forEach { tienda ->
                            Log.d(
                                "obtenoemos_la_tog",
                                " user = ${lat_user} ${log_user} teinda=${tienda.latitud} ${tienda.longitud}"
                            )
                            Marker(
                                state = MarkerState(LatLng(tienda.latitud, tienda.longitud)),
                                title = tienda.nombre_tienda,
                                icon =  MarkerIcon(context, seleccionadoId == tienda.id_tienda),
                                onClick = {
                                    lister_marker = dataclass_map(
                                        tienda.logo_tienda,
                                        tienda.nombre_tienda,
                                        tienda.lista_subcategoiras,
                                        lat_user,
                                        log_user,
                                        tienda.latitud,
                                        tienda.longitud,
                                        tienda.id_tienda,
                                        "",
                                        tienda.direccion,
                                        tienda.referencia,
                                    )


                                    seleccionadoId = tienda.id_tienda
                                    viewModel_filtrado_tiendas.obtenerHorarioPorTienda_activa(
                                        "barranca",
                                        tienda.id_tienda
                                    )
                                    show_dialog_datos_lugares = true
                                    true
                                }
                            )
                        }
                    }

                    "seguridad" -> {
                        Marker(
                            state = MarkerState(LatLng(latitud_luga_seg, long_luga_seg)),
                            title = "lugar seguro ajaj",
//                            icon = markerColor


//                            onClick = {
//                                lister_marker = dataclass_map(
//                                    tienda.logo_tienda,
//                                    tienda.nombre_tienda,
//                                    tienda.lista_subcategoiras,
//                                    lat_user,
//                                    log_user,
//                                    tienda.latitud,
//                                    tienda.longitud,
//                                    tienda.id_tienda,
//                                    "",
//                                    tienda.direccion,
//                                    tienda.referencia,
//                                )
//
//
//                                seleccionadoId = tienda.id_tienda
//                                viewModel_filtrado_tiendas.obtenerHorarioPorTienda_activa(
//                                    "barranca",
//                                    tienda.id_tienda
//                                )
//                                show_dialog_datos_lugares = true
//                                true
//                            }
                        )
                    }

                    else -> {}
                }

                MapEffect {
                    try {
                        val success = it.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                        )
                        if (!success) Log.e("Maps", "Error aplicando estilo del mapa")
                    } catch (e: Exception) {
                        Log.e("Maps", "Archivo de estilo no encontrado.", e)
                    }
                }
            }

        }

        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            onClick = {
                if (verificarUbiActiva(context)) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val userLatLng = LatLng(it.latitude, it.longitude)
                            log_user = it.longitude
                            lat_user = it.latitude
                            Log.d("obtenoemos_la_tog", " userprimario = ${log_user} ${lat_user}")
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(userLatLng, 16f),
                                    1000
                                )
                            }
                        }
                    }
                } else {
                    validacion_mostrar_dialog_ubi_off = true
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación"
            )
        }
        AnimatedVisibility(
            visible = (!show_botoom_sheet && !boxVisible) || !show_dialog_datos_lugares,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                onClick = {
                    if (lista_filtrada_tiendas.isNotEmpty()) {
                        currentIndex = (currentIndex + 1) % lista_filtrada_tiendas.size
                        val tienda = lista_filtrada_tiendas[currentIndex]

                        // actualizar marcador seleccionado
                        lister_marker = dataclass_map(
                            tienda.logo_tienda,
                            tienda.nombre_tienda,
                            tienda.lista_subcategoiras,
                            lat_user,
                            log_user,
                            tienda.latitud,
                            tienda.longitud,
                            tienda.id_tienda,
                            "",
                            tienda.direccion,
                            tienda.referencia,
                        )

                        seleccionadoId = tienda.id_tienda
                        show_dialog_datos_lugares = true

                        // mover cámara
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(tienda.latitud, tienda.longitud), 16f
                                ),
                                1000
                            )
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mi ubicación"
                )
            }
        }

        if (show_botoom_sheet) {
            bottom_sheet_mapa(
                lat_user,
                log_user,
                cameraPositionState,
                tipo,
                lista_filtrada_turismo,
                lista_filtrada_tiendas,
                onclose = {
                    show_botoom_sheet = false
                },
                selecionado_id = { seleccionadoIds ->
                    seleccionadoId = seleccionadoIds
                }, datos_selecionado_retornar = { datos ->
                    lister_marker = datos
                    show_dialog_datos_lugares = true
                })
        }
//        AnimatedVisibility(
//            visible = (!show_botoom_sheet && !boxVisible) || !show_dialog_datos_lugares,
//            enter = fadeIn(),
//            exit = fadeOut(),
//            modifier = Modifier.align(Alignment.BottomCenter)
//        ) {
//            Box(
//                modifier = Modifier
//                    .height(25.dp)
//                    .width(70.dp)
//                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
//                    .background(
//                        MaterialTheme.colorScheme.primary
//                    )
//                    .clickable { show_botoom_sheet = true },
//                contentAlignment = Alignment.Center
//
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.KeyboardArrowUp,
//                    contentDescription = "Flecha arriba", modifier = Modifier.size(20.dp)
//                )
//            }
//
//        }

        AnimatedVisibility(
            visible = show_dialog_datos_lugares,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            dialogo_lugar_tienda(show_botoom_sheet,show_dialog_datos_lugares,horario_por_tienda, lister_marker, seleccionadoId, cerra_dialog = {
                show_dialog_datos_lugares = false
            }, limpiar = {
                seleccionadoId = ""
            }, crear_ruta = { lat, log ->
                latitud = lat
                longitud = log
                dialog_Crear_ruta = true
            }, actualizar = {
                actualizarUbicacion(context, fusedLocationClient) { lat, log ->
                    lat_user = lat
                    log_user = log
                    lister_marker = lister_marker.copy(
                        my_latitud = lat,
                        my_longitud = log
                    )
                }
            }, boxVisible, onBoxVisibleChange = { boxVisible = it }, centrar_camara = { lat, log ->
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(lat, log), 18f),
                        1000
                    )
                }
            }, retornar_id_select = { id_tienda_lugar ->
                id_lugar_tienda_select = id_tienda_lugar
                show_bottom_sheet_datos_tienda_lugares = true
            }, onclick_iconos = { datos ->

                when (datos.nombre_red) {
                    "llamar" -> {
                        // Lógica para llamada
                    }

                    "whatsapp" -> {
                        // Lógica para abrir whatsapp
                    }

                    "tiktok" -> {
                        // Lógica para abrir TikTok
                    }

                    "facebook" -> {
                        // Abrir facebook
                    }

                    "instagram" -> {
                        // Abrir instagram
                    }

                }
            }, mostrar_lista = {
                show_botoom_sheet = true
            })
        }
    }
}
@Composable
fun MarkerIcon(
    context: Context,
    seleccionado: Boolean
): BitmapDescriptor {
    val drawableId = if (seleccionado) {
        R.drawable.pin_select_webp
    } else {
        R.drawable.pin_deselect_webp
    }

    val size = if (seleccionado) 120 else 100

    return bitmapDescriptorFromDrawable(context, drawableId, size, size)
}

@Composable
fun dialogo_lugar_tienda(
    show_botoom_sheet: Boolean,
    show_dialog_datos_lugares: Boolean,
    horario_por_tienda: Map<String, Boolean>?,
    dataclass_map: dataclass_map,
    seleccionadoId: String?,
    cerra_dialog: () -> Unit,
    limpiar: () -> Unit,
    crear_ruta: (lat: Double, log: Double) -> Unit,
    actualizar: () -> Unit,
    boxVisible: Boolean,
    onBoxVisibleChange: (Boolean) -> Unit,
    centrar_camara: (Double, Double) -> Unit,
    retornar_id_select: (String) -> Unit,
    onclick_iconos: (constantes_lista_localidades.data_redes_tiendas) -> Unit,
    mostrar_lista:()-> Unit
) {
    val estado_tienda_filter = horario_por_tienda?.get(seleccionadoId) == true
    val color = if (estado_tienda_filter) Color.Green else Color.Red
    val estado_texto = if (estado_tienda_filter) "Abierto" else "Cerrado"

    Log.d("boxVisibleboxVisible", boxVisible.toString())
    val context = LocalContext.current
    val gpsActivo by rememberGpsActivo(context)
    val distancia = verificarDistanciaFormateada(
        dataclass_map.my_latitud,
        dataclass_map.my_longitud,
        dataclass_map.latitud,
        dataclass_map.longitud
    )

    val listate = rememberLazyListState()
    val showLeftShadow by remember {
        derivedStateOf { listate.firstVisibleItemIndex > 0 || listate.firstVisibleItemScrollOffset > 0 }
    }
    val showRightShadow by remember {
        derivedStateOf {
            val lastVisible = listate.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listate.layoutInfo.totalItemsCount
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


    LaunchedEffect(gpsActivo) {
        if (gpsActivo && (dataclass_map.my_latitud == 0.0 || dataclass_map.my_longitud == 0.0)) {
            actualizar()
        }
    }
    val cornerShape = if (boxVisible) {
        RoundedCornerShape(
            topEnd = 0.dp,
            bottomEnd = 0.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        )
    }
    val cornerShap2 = if (!boxVisible) {
        RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomEnd = 10.dp,
            bottomStart = 10.dp
        )
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val targetWidth = if (boxVisible) 0.33f else 0.38f
    val animatedWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp)

    ) {
        Row(
            modifier = Modifier
                .height(screenHeight * 0.19f)
                .clip(cornerShap2)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .zIndex(1f)
                    .fillMaxWidth(animatedWidth)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(dataclass_map.img)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.sin_item_carrito)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(cornerShape)
                        .clickable { onBoxVisibleChange(!boxVisible) }
                        .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < 0) {
                                Log.d("GESTO", "mostramos lista")
                                mostrar_lista()
                            }
                            else if (dragAmount > 0) {
                                // 👉 Se movió hacia abajo
                                Log.d("GESTO", "Swipe Down detectado")
                            }
                        }
                    },
                    contentScale = ContentScale.Crop
                )

                this@Row.AnimatedVisibility(
                    visible = !boxVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                cerra_dialog()
                                limpiar()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(35.dp)
                                .background(Color.Black.copy(alpha = 0.25f))
                                .blur(12.dp),
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                this@Row.AnimatedVisibility(
                    !boxVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 1f)
                                    ),
                                    startY = 0f,
                                    endY = 200f
                                )
                            )
                    )
                }

            }


            AnimatedVisibility(
                visible = boxVisible,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it },
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                        .background(Color.Black)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            centrar_camara(
                                dataclass_map.latitud,
                                dataclass_map.longitud
                            )

                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 5.dp)
                        ) {
                            texto_generico_one_line(
                                dataclass_map.nombre,
                                MaterialTheme.typography.titleLarge
                            )
                            spacer_vertical(10.dp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                texto_generico_one_line(
                                    estado_texto,
                                    MaterialTheme.typography.bodyMedium
                                )
                                spacer_horizonta(5.dp)
                                Box(
                                    Modifier
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(14.dp)
                                )
                                spacer_horizonta(10.dp)
                                if (dataclass_map.my_latitud == 0.0 || dataclass_map.my_longitud == 0.0) {
                                    if (gpsActivo) texto_generico_one_line("Obteniendo ubicación...")
                                    else texto_generico_one_line("")
                                } else {
                                    texto_generico_one_line(
                                        "A $distancia",
                                        MaterialTheme.typography.bodyMedium
                                    )
                                }

                            }
                            spacer_vertical(10.dp)
                            texto_generico_one_line(
                                dataclass_map.direccion,
                                MaterialTheme.typography.bodyMedium
                            )
                            spacer_vertical(10.dp)
                            texto_generico_one_line(
                                dataclass_map.referencia,
                                MaterialTheme.typography.bodyMedium
                            )
                            spacer_vertical(10.dp)
                            tags_subcateogiras(dataclass_map.tag)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 7.dp)
                        ) {
                            // Botón cerrar
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                onClick = {
                                    cerra_dialog()
                                    limpiar()
                                },
                                modifier = Modifier
                                    .size(35.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                            Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                                // Botón ruta
                                FloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White,
                                    onClick = {
                                        retornar_id_select(dataclass_map.id)
                                    },
                                    modifier = Modifier
                                        .size(35.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = "centrar")
                                }
                                spacer_vertical(7.dp)
                                // Botón ruta
                                FloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White,
                                    onClick = {
                                        crear_ruta(
                                            dataclass_map.latitud,
                                            dataclass_map.longitud
                                        )
                                    },
                                    modifier = Modifier
                                        .size(35.dp)
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = "Ir")
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            !boxVisible,
            modifier = Modifier
                .fillMaxWidth(animatedWidth)
                .zIndex(-1f)
        ) {
            Box(
                modifier = Modifier
                    .height(45.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                    )
            ) {
                LazyRow(
                    state = listate,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp),
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    items(lista_redes_tiendas) { i ->
                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(i.icono),
                                contentDescription = i.nombre_red,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
//                                        crear_ruta(
//                                            dataclass_map.latitud,
//                                            dataclass_map.longitud
//                                        )
                                        onclick_iconos(i)
                                    }
                            )
                        }
                    }
                }
                // 👈 izquierda
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(40.dp)
                        .align(Alignment.CenterStart)
                        .zIndex(1f)
                        .alpha(alphaLeft)
                        .clip(RoundedCornerShape(bottomStart = 10.dp))
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
                        .clip(RoundedCornerShape(bottomEnd = 10.dp))
                        .background(Brush.horizontalGradient(colors = shadow_right))
                )

            }
        }
    }

}


@Composable
fun rememberGpsActivo(context: Context): State<Boolean> {
    val gpsActivo = remember { mutableStateOf(isGpsActivo(context)) }

    // Cada vez que la pantalla vuelve a primer plano, actualiza
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                gpsActivo.value = isGpsActivo(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return gpsActivo
}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.heightIn
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
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permisos_llamadas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.requestCallPermission
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_mapa
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.TiempoRestanteCierre
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.retornar_color_estado_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.actualizarUbicacion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.bitmapDescriptorFromDrawable
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.data_redes_tiendas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isGpsActivo
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
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
    var color_referencia by remember { mutableStateOf(Color.Red) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coordenadas by viewmode_segurirdad_Salud.coordenadasSeleccionadas.observeAsState()
    var latitud_luga_seg by remember { mutableStateOf(0.0) }
    var long_luga_seg by remember { mutableStateOf(0.0) }
    val lista_filtrada_turismo by viewmodel_lugares_turisticos.listaFiltrada.collectAsState()
    val lista_filtrada_tiendas by viewModel_filtrado_tiendas.listaTiendasGuardadas.observeAsState(
        emptyList()
    )
    val horario_por_tienda by viewModel_filtrado_tiendas.estadoTiendas.observeAsState()
    val datosTienda by viewModel_filtrado_tiendas._datos_tienda.observeAsState(emptyList())
    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    var currentIndex =
        lista_filtrada_tiendas.indexOfFirst { data -> data.id_tienda == seleccionadoId }


    coordenadas?.let { (lat, lon) ->
        latitud_luga_seg = lat
        long_luga_seg = lon
    }

    var lister_marker by remember { mutableStateOf(dataclass_map()) }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var show_botoom_sheet by remember { mutableStateOf(true) }
    var show_dialog_datos_lugares by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var log_user by remember { mutableStateOf(0.0) }
    var lat_user by remember { mutableStateOf(0.0) }

    val defaultLocation_barranca = LatLng(-10.751480371828691, -77.76088112286742)
    val defaultLocation_paramonga = LatLng(-10.678480703018984, -77.81957068618482)
    val defaultLocation_supe = LatLng(-10.795610086889571, -77.71618154413743)
    val defaultLocation_puerto_supe = LatLng(-10.796606548738318, -77.74082770132752)
    val defaultLocation_pativilca = LatLng(-10.696153944234334, -77.77668811678933)

    val cameraPositionState = rememberCameraPositionState {
        val localidad_default = when (localidad) {
            "barranca" -> {
                defaultLocation_barranca
            }

            "paramonga" -> {
                defaultLocation_paramonga
            }

            "pativilca" -> {
                defaultLocation_pativilca
            }

            "supe" -> {
                defaultLocation_supe
            }

            "puerto_supe" -> {
                defaultLocation_puerto_supe
            }

            else -> {
                defaultLocation_barranca
            }
        }
        position = CameraPosition.fromLatLngZoom(localidad_default, 15f)
    }


    var boxVisible by remember { mutableStateOf(true) }
    var mostar_bottom_sheet by remember { mutableStateOf(false) }
    var id_lugar_tienda_select by remember { mutableStateOf("") }
    var localidad_tienda_lugar_Select by remember { mutableStateOf(localidad) }
    var show_bottom_sheet_datos_tienda_lugares by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }


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
            color_referencia,
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
            "Para una mejor experiencia y poder mostrar tu ubicación actual en el mapa, por favor habilita la función de ubicación en tu dispositivo. Esto te permitirá ubicarte de manera más rápida y conocer la proximidad a tu destino.",
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
                    isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false)
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
                                icon = MarkerIcon(
                                    context,
                                    seleccionadoId == lugar.id_lugar_turistico
                                ),
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
                                icon = MarkerIcon(context, seleccionadoId == tienda.id_tienda),
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
                                        tienda.horario_dia, tienda.contacto_tienda
                                    )

                                    seleccionadoId = tienda.id_tienda

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
                mostar_bottom_sheet = true
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
        Log.d("(!show_botoom_sheet && !boxVisible)", "${!show_botoom_sheet} ${!boxVisible}")
        AnimatedVisibility(
            visible = (!show_botoom_sheet && !boxVisible) && show_dialog_datos_lugares,
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
                        Log.d("currentIndex", currentIndex.toString())
                        if (currentIndex != -1) {
                            val siguiente = (currentIndex + 1) % lista_filtrada_tiendas.size
                            val tienda = lista_filtrada_tiendas[siguiente]
                            Log.d("currentIndex", "$siguiente")
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
                                tienda.horario_dia, tienda.contacto_tienda
                            )
                            Log.d("ecnotramos","${tienda.contacto_tienda}")

                            seleccionadoId = tienda.id_tienda
                            show_dialog_datos_lugares = true

                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(tienda.latitud, tienda.longitud), 16f
                                    ),
                                    1000
                                )
                            }
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
                seleccionadoId = seleccionadoId?:"",
                lat_user = lat_user,
                log_user = log_user,
                cameraPositionState = cameraPositionState,
                tipo = tipo,
                lista_filtrada_turismo = lista_filtrada_turismo,
                lista = lista_filtrada_tiendas,
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

        AnimatedVisibility(
            visible = show_dialog_datos_lugares,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            dialogo_lugar_tienda(
                viewModel_filtrado_tiendas,
                dataclass_map = lister_marker,
                cerra_dialog = {
                    show_dialog_datos_lugares = false
                    mostar_bottom_sheet = true
                },
                limpiar = {
                    seleccionadoId = ""
                },
                crear_ruta = { lat, log ->
                    latitud = lat
                    longitud = log
                    dialog_Crear_ruta = true
                },
                actualizar = {
                    actualizarUbicacion(context, fusedLocationClient) { lat, log ->
                        lat_user = lat
                        log_user = log
                        lister_marker = lister_marker.copy(
                            my_latitud = lat,
                            my_longitud = log
                        )
                    }
                },
                boxVisible,
                onBoxVisibleChange = {
                    Log.d("visible", it.toString())
                    boxVisible = it
                },
                centrar_camara = { lat, log ->
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(lat, log), 18f),
                            1000
                        )
                    }
                },
                retornar_id_select = { id_tienda_lugar, color ->
                    color_referencia = color
                    id_lugar_tienda_select = id_tienda_lugar
                    show_bottom_sheet_datos_tienda_lugares = true
                },
                onclick_iconos = { datos ->
                    when (datos.nombre_red) {
                        "llamar" -> {
                            llamar(context, datos.valor, {
                                call_dialog_permise = true
                                numero_llamada = datos.valor
                            })
                        }

                        "whatsapp" -> {
                            abrir_whattsapp(context, datos.valor)
                        }

                        "tiktok" -> {
                            openTiktok(
                                context,
                                datos.valor
                            )
                        }

                        "facebook" -> {
                            Log.d("    datos.valor","${datos.valor}")
                            openFacebook(
                                context,
                                datos.valor
                            )
                        }

                        "instagram" -> {
                            Log.d("    datos.valor","${datos.valor}")
                            openInstagram(
                                context,
                                datos.valor
                            )
                        }

                        "Web" -> {
                            openWebLink(
                                context, datos.valor
                            )
                        }

                    }
                },
                mostrar_lista = {
                    show_botoom_sheet = true
                }, move_derecha = {
                    if (lista_filtrada_tiendas.isNotEmpty()) {
                        if (currentIndex != -1) {
                            // Mover al elemento anterior (hacia la derecha)
                            val anterior =
                                if (currentIndex - 1 < 0) lista_filtrada_tiendas.lastIndex else currentIndex - 1
                            val tienda = lista_filtrada_tiendas[anterior]

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
                                tienda.horario_dia, tienda.contacto_tienda
                            )

                            seleccionadoId = tienda.id_tienda
                            currentIndex = anterior // 🔹 Actualiza el índice

                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(tienda.latitud, tienda.longitud), 16f
                                    ),
                                    1000
                                )
                            }
                        }
                    }
                },

                move_izquierda = {
                    if (lista_filtrada_tiendas.isNotEmpty()) {
                        if (currentIndex != -1) {
                            // Mover al siguiente elemento (hacia la izquierda)
                            val siguiente = (currentIndex + 1) % lista_filtrada_tiendas.size
                            val tienda = lista_filtrada_tiendas[siguiente]

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
                                tienda.horario_dia, tienda.contacto_tienda
                            )

                            seleccionadoId = tienda.id_tienda
                            currentIndex = siguiente // 🔹 Actualiza el índice

                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(tienda.latitud, tienda.longitud), 16f
                                    ),
                                    1000
                                )
                            }
                        }
                    }
                }
            )
        }
        AnimatedVisibility(
            visible = (mostar_bottom_sheet && !show_dialog_datos_lugares) || seleccionadoId.isNullOrEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .height(25.dp)
                    .width(100.dp)
                    .clickable {
                        show_botoom_sheet = true
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp, // ícono de flecha hacia arriba
                    contentDescription = "Abrir",
                    tint = Color.White, // color del ícono
                    modifier = Modifier.size(20.dp) // tamaño del ícono
                )
            }
        }
    }
    if (call_dialog_permise) {
        permisos_llamadas(aceptar_permisos = {
            requestCallPermission( context =context, phoneNumber = numero_llamada)
        }, ondimis = {
            call_dialog_permise = false
        })
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
    viewModelFiltros: viewModel_filtado_tiendas,
    dataclass_map: dataclass_map,
    cerra_dialog: () -> Unit,
    limpiar: () -> Unit,
    crear_ruta: (lat: Double, log: Double) -> Unit,
    actualizar: () -> Unit,
    boxVisible: Boolean,
    onBoxVisibleChange: (Boolean) -> Unit,
    centrar_camara: (Double, Double) -> Unit,
    retornar_id_select: (String, Color) -> Unit,
    onclick_iconos: (data_redes_tiendas) -> Unit,
    mostrar_lista: () -> Unit,
    move_izquierda: () -> Unit,
    move_derecha: () -> Unit
) {
    val tick by viewModelFiltros.tick.collectAsState()

    var estadoColor by remember { mutableStateOf(Color.Red) }
    Log.d("llamoasalafun", "si")

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
    val cardHeight = (screenHeight * 0.22f).coerceIn(200.dp, 320.dp)
    val animatedWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )
    var totalDx by remember { mutableStateOf(0f) }
    var totalDY by remember { mutableStateOf(0f) }
    var validad_horario by remember { mutableStateOf(false) }

    val lista_redes_tiendas = listOf(
        data_redes_tiendas(
            enable = dataclass_map.contacto_tienda.llamada.estado,
            icono = R.drawable.llamada_icon,
            nombre_red = "llamar",
            valor = dataclass_map.contacto_tienda.llamada.numero
        ),
        data_redes_tiendas(
            enable = dataclass_map.contacto_tienda.whatsapp.estado,
            icono = R.drawable.whatsapp_icon,
            nombre_red = "whatsapp",
            valor = dataclass_map.contacto_tienda.whatsapp.numero
        ),
        data_redes_tiendas(
            enable = dataclass_map.contacto_tienda.tiktok.estado,
            icono = R.drawable.tik_tok_icon,
            nombre_red = "tiktok",
            valor = dataclass_map.contacto_tienda.tiktok.url
        ),
        data_redes_tiendas(
            enable = dataclass_map.contacto_tienda.facebook.estado,
            icono = R.drawable.facebook_icon,
            nombre_red = "facebook",
            valor = dataclass_map.contacto_tienda.facebook.url
        ),
        data_redes_tiendas(
            enable = dataclass_map.contacto_tienda.instagram.estado,
            icono = R.drawable.instagram_icon,
            nombre_red = "instagram",
            valor = dataclass_map.contacto_tienda.instagram.url
        ),
        data_redes_tiendas(
            enable = dataclass_map.contacto_tienda.sitio_web.estado,
            icono = R.drawable.sitio_web,
            nombre_red = "web",
            valor = dataclass_map.contacto_tienda.sitio_web.url
        )
    )
    Log.d("campos_enviados", lista_redes_tiendas.toString())


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .heightIn(
                    min = 120.dp, // más compacto
                    max = 180.dp  // altura máxima para que no se coma toda la pantalla
                )
                .animateContentSize()
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
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(cornerShape)
                        .clickable { onBoxVisibleChange(!boxVisible) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { _, dragAmount ->
                                    val (dx, dy) = dragAmount
                                    totalDx += dx
                                    totalDY += dy
                                },
                                onDragEnd = {
                                    when {
                                        totalDx > 80 -> {
                                            move_derecha()
                                            validad_horario = true
                                        }

                                        totalDx < -80 -> {
                                            move_izquierda()
                                            validad_horario = true
                                        }

                                        totalDY > 80 -> {
//                                            move_abajo()
                                        }

                                        totalDY < -80 -> {
                                            mostrar_lista()
                                        }
                                    }
                                    totalDx = 0f
                                    totalDY = 0f
                                }
                            )
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < 0) {
                                    Log.d("GESTO", "mostramos lista")
                                    mostrar_lista()
                                } else if (dragAmount > 0) {
                                    Log.d("GESTO", "Swipe Down detectado")
                                }
                            }
                        },
                    contentScale = ContentScale.Crop
                )
                if (gpsActivo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(1f)
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                    ) {

                        if (dataclass_map.my_latitud == 0.0 || dataclass_map.my_longitud == 0.0) {

                        } else {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                texto_generico_one_line(
                                    "A $distancia",
                                    MaterialTheme.typography.bodyMedium,
                                )
                                spacer_horizonta(5.dp)
                                if (!boxVisible) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(estadoColor)
                                    )
                                }
                            }
                        }
                    }
                }




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
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { _, dragAmount ->
                                    val (dx, dy) = dragAmount
                                    totalDx += dx
                                    totalDY += dy
                                },
                                onDragEnd = {
                                    when {
                                        totalDx > 80 -> {
                                            move_derecha()
                                            validad_horario = true

                                        }

                                        totalDx < -80 -> {
                                            move_izquierda()
                                            validad_horario = true
                                        }

                                        totalDY > 80 -> {
//                                            move_abajo()
                                        }

                                        totalDY < -80 -> {
                                            mostrar_lista()
                                        }
                                    }
                                    totalDx = 0f
                                    totalDY = 0f
                                }
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
                                TiempoRestanteCierre(
                                    dataclass_map.horario_tienda,
                                    dataclass_map.horario_tienda.h_cierre,
                                    dataclass_map.horario_tienda.cerrado,
                                    dataclass_map.horario_tienda.motivo,
                                    true,
                                    tick
                                ) { color ->
                                    estadoColor = color
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
                            tags_subcateogiras(
                                dataclass_map.tag,
                                brush_start = Brush.horizontalGradient(colors = shadow_left),
                                brush_end = Brush.horizontalGradient(colors = shadow_right)
                            )
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
                                        retornar_id_select(dataclass_map.id, estadoColor)
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
                    items(lista_redes_tiendas.filter { it.enable }) { i ->
                        Log.d("lsitaeclicalda",lista_redes_tiendas.toString())
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
        if (validad_horario) {
            retornar_color_estado_tienda(
                dataclass_map.horario_tienda,
                dataclass_map.horario_tienda.h_cierre,
                dataclass_map.horario_tienda.cerrado,
                dataclass_map.horario_tienda.motivo,
                tick
            ) { color ->
                estadoColor = color
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
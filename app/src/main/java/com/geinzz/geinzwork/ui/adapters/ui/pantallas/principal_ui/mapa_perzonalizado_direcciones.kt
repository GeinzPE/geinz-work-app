package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_mapa
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.actualizarUbicacion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isGpsActivo
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarDistanciaFormateada
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun pantalla_mapa_perzonalizado(
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    tipo: String,
) {
    Box() {
        MyGoogle_maps(tipo, viewmodel_lugares_turisticos, viewModel_filtrado_tiendas)
    }

}

@SuppressLint("MissingPermission")
@Composable
fun MyGoogle_maps(
    tipo: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
) {
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val lista_filtrada_turismo by viewmodel_lugares_turisticos.listaFiltrada.collectAsState()
    val lista_filtrada_tiendas by viewModel_filtrado_tiendas.listaFiltrada.collectAsState()
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
                    myLocationButtonEnabled = false
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
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    if (seleccionadoId == lugar.id_lugar_turistico) BitmapDescriptorFactory.HUE_BLUE
                                    else BitmapDescriptorFactory.HUE_RED
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
                                        lugar.id_lugar_turistico
                                    )
                                    show_dialog_datos_lugares = true

                                    seleccionadoId = lugar.id_lugar_turistico
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
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    if (seleccionadoId == tienda.id_tienda) BitmapDescriptorFactory.HUE_BLUE
                                    else BitmapDescriptorFactory.HUE_RED
                                ),

                                onClick = {
                                    lister_marker = dataclass_map(
                                        tienda.logo_tienda,
                                        tienda.nombre_tienda,
                                        tienda.lista_subcategoiras,
                                        lat_user,
                                        log_user,
                                        tienda.latitud,
                                        tienda.longitud,
                                        tienda.id_tienda
                                    )
                                    show_dialog_datos_lugares = true

                                    seleccionadoId = tienda.id_tienda
                                    true
                                }
                            )
                        }
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
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación"
            )
        }
        if (show_botoom_sheet) {
            bottom_sheet_mapa(
                cameraPositionState,
                tipo,
                lista_filtrada_turismo,
                lista_filtrada_tiendas,
                onclose = {
                    show_botoom_sheet = false
                },
                selecionado_id = { seleccionadoIds ->
                    seleccionadoId = seleccionadoIds
                })
        }
        AnimatedVisibility(
            visible = !show_botoom_sheet,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = { show_botoom_sheet = true },
                modifier = Modifier
                    .padding(bottom = 20.dp)
            ) {
                texto_generico_one_line("Ver lista")
            }
        }

        AnimatedVisibility(
            visible = show_dialog_datos_lugares,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            dialogo_lugar_tienda(lister_marker, seleccionadoId, cerra_dialog = {
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
            })
        }


    }
}


@Composable
fun dialogo_lugar_tienda(
    dataclass_map: dataclass_map,
    seleccionadoId: String?,
    cerra_dialog: () -> Unit,
    limpiar: () -> Unit,
    crear_ruta: (lat: Double, log: Double) -> Unit,
    actualizar: () -> Unit
) {
    val context = LocalContext.current
    val gpsActivo by rememberGpsActivo(context)
    val distancia = verificarDistanciaFormateada(
        dataclass_map.my_latitud,
        dataclass_map.my_longitud,
        dataclass_map.latitud,
        dataclass_map.longitud
    )

    LaunchedEffect(gpsActivo) {
        if (gpsActivo && (dataclass_map.my_latitud == 0.0 || dataclass_map.my_longitud == 0.0)) {
            actualizar()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(20.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.background)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(dataclass_map.img)
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .weight(1f)
                .clip(RoundedCornerShape(10)),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .weight(2f)
                .height(200.dp)
                .padding(10.dp)
        ) {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                onClick = {
                    cerra_dialog()
                    limpiar()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .size(35.dp)
                    .align(Alignment.TopEnd)

            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    modifier = Modifier.size(25.dp),
                    contentDescription = "Cerrar"
                )
            }
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
                    .clip(CircleShape)
                    .size(35.dp)
                    .align(Alignment.BottomEnd)

            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    modifier = Modifier.size(25.dp),
                    contentDescription = "Cerrar"
                )
            }
            Column {
                texto_generico_one_line(dataclass_map.nombre)
                Row {
                    if (dataclass_map.my_latitud == 0.0 || dataclass_map.my_longitud == 0.0) {
                        if (gpsActivo) {
                            texto_generico_one_line("Obteniendo ubicación...")
                        } else {
                            texto_generico_one_line("La ubicación está desactivada")
                        }
                    } else {
                        texto_generico_one_line("A $distancia")
                    }
                }


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


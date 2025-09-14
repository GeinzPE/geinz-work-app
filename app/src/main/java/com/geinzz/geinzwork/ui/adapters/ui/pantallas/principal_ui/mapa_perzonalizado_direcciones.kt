package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_mapa
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
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
import kotlinx.coroutines.tasks.await

@Composable
fun pantalla_mapa_perzonalizado(
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
    tipo: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
) {
    Box() {
        MyGoogle_maps(tipo, viewmodel_lugares_turisticos, viewModel_filtrado_tiendas)
    }

}

@Composable
fun MyGoogle_maps(
    tipo: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
) {
    val lista_filtrada by viewmodel_lugares_turisticos.listaFiltrada.collectAsState()


    val lista_filtrada_tiendas by viewModel_filtrado_tiendas.listaFiltrada.collectAsState()
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var dialogo_ubi_Activa by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    var show_botoom_sheet by remember { mutableStateOf(true) }
    var show_dialog_datos_lugares by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    Log.d("obtenoemos_tienda", lista_filtrada_tiendas.toString())

//    if (dialog_Crear_ruta) {
//        dialog_crear_ruta_lugares({ dialog_Crear_ruta = false }, { crear_ruta ->
//            dialog_Crear_ruta = false
//            if (crear_ruta && verificarUbiActiva(context)) {
//                constantes_lista_localidades.abrir_google_maps(
//                    context, latitud, longitud,
//                ) { dialogo ->
//                    dialogo_ubi_Activa = dialogo
//                }
//            } else {
//                dialogo_ubi_Activa = true
//            }
//        })
//    }

    val defaultLocation = LatLng(-10.8500, -77.7500) // coordenadas de Barranca
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }
    if (validacion_mostrar_dialog_ubi_off) {
        dialog_sin_ubi__rutas(
            { validacion_mostrar_dialog_ubi_off = false },
            {
                validacion_mostrar_dialog_ubi_off = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            })
    }

    Box() {
        Column(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true // muestra el punto azul si hay permisos
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false // ocultamos el botón nativo
                )

            ) {

//                lista_filtrada.forEach { lugar ->
//                    Marker(
//                        state = MarkerState(LatLng(lugar.latitud, lugar.longitud)),
//                        title = lugar.titulo,
//                        icon = BitmapDescriptorFactory.defaultMarker(
//                            if (seleccionadoId == lugar.id_lugar_turistico) BitmapDescriptorFactory.HUE_BLUE
//                            else BitmapDescriptorFactory.HUE_RED
//                        ),
//                        onClick = {
//                            dialog_Crear_ruta = true
//                            seleccionadoId = lugar.id_lugar_turistico
//                            true
//                        }
//                    )
//                }

                lista_filtrada_tiendas.forEach { tienda ->
                    Marker(
                        state = MarkerState(LatLng(tienda.latitud, tienda.longitud)),
                        title = tienda.nombre_tienda,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (seleccionadoId == tienda.id_tienda) BitmapDescriptorFactory.HUE_BLUE
                            else BitmapDescriptorFactory.HUE_RED
                        ),

                        onClick = {
                            show_dialog_datos_lugares = true
//                            dialog_Crear_ruta = true
                            seleccionadoId = tienda.id_tienda
                            true
                        }
                    )
                }

//                _lugares_turisticos.forEach { tienda ->
//                    Marker(
//                        state = MarkerState(LatLng(tienda.latitud, tienda.longitud)),
//                        title = tienda.titulo,
//                        onClick = {
//                            true
//                        }
//                    )
//                }

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
        // Botón flotante personalizado
        FloatingActionButton(
            onClick = {
                if (verificarUbiActiva(context)) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val userLatLng = LatLng(it.latitude, it.longitude)
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
        LaunchedEffect(Unit) {
//        viewModel_cordenadas.lugares_turisticos("barranca")
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
        }
        if (show_botoom_sheet) {
            bottom_sheet_mapa(cameraPositionState, lista_filtrada_tiendas, onclose = {
                show_botoom_sheet = false
            }, selecionado_id = { seleccionadoIds ->
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
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 } // empieza desde 1/3 de la pantalla (≈30%)
                    ),
            exit = fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(
                        targetOffsetY = { it / 3 }
                    ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            dialogo_lugar_tienda(seleccionadoId, cerra_dialog = {
                show_dialog_datos_lugares = false
            }, limpiar = {
                seleccionadoId = ""
            })
        }


    }
}

@Composable
fun dialogo_lugar_tienda(seleccionadoId: String?, cerra_dialog: () -> Unit, limpiar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(20.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.background)

    ) {
        Button(onClick = {
            cerra_dialog()
            limpiar()
        }) {
            texto_generico_one_line("cerrar")

        }
        texto_generico_one_line(seleccionadoId.toString())
    }
}


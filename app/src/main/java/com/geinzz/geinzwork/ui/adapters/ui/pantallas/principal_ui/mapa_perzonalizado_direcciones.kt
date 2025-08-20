package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_turismo_google_mpa
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun pantalla_mapa_perzonalizado(
    tipo: String,
    viewModel_cordenadas: viewModel_principal_geinz_work = viewModel(),
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
) {
    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MyGoogle_maps(tipo,viewmodel_lugares_turisticos,viewModel_cordenadas)
        }
    }
}

@Composable
fun MyGoogle_maps(
    tipo: String, viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    viewModel_cordenadas: viewModel_principal_geinz_work = viewModel(),
) {

//    val _lugares_turisticos by viewModel_cordenadas._lugares_turisticos.observeAsState(emptyList())
    val lista_filtrada by viewmodel_lugares_turisticos.listaFiltrada.collectAsState()
    Log.d("obteneoms_listA_fitlrado", lista_filtrada.toString())
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var dialogo_ubi_Activa by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
//    val cordenadas by viewModel_cordenadas._obtener_datos_tienda.observeAsState(emptyList())
//    val datosTienda by viewModel_cordenadas._datos_tienda.observeAsState(emptyList())
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var id_tienda = remember { mutableStateOf("") }
    var seleccionadoId by remember { mutableStateOf<String?>(null) }


    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true // Esto activa el círculo azul y la flecha
            )
        )
    }

    if (dialog_Crear_ruta) {
        dialog_crear_ruta_lugares({ dialog_Crear_ruta = false }, { crear_ruta ->
            dialog_Crear_ruta = false
            if (crear_ruta && verificarUbiActiva(context)) {
                constantes_lista_localidades.abrir_google_maps(
                    context, latitud, longitud,
                ) { dialogo ->
                    dialogo_ubi_Activa = dialogo
                }
            } else {
                dialogo_ubi_Activa = true
            }
        })
    }

    if (dialogo_ubi_Activa) {
        dialog_sin_ubi__rutas({ dialogo_ubi_Activa = false }, {
            dialogo_ubi_Activa = false
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        })
    }

    val defaultLocation = LatLng(-10.8500, -77.7500) // coordenadas de Barranca
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(0.7f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
            ) {


                lista_filtrada.forEach { lugar ->
                    Marker(
                        state = MarkerState(LatLng(lugar.latitud, lugar.longitud)),
                        title = lugar.titulo,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (seleccionadoId == lugar.id_lugar_turistico) BitmapDescriptorFactory.HUE_BLUE
                            else BitmapDescriptorFactory.HUE_RED
                        ),
                        onClick = {
                            dialog_Crear_ruta = true
                            seleccionadoId = lugar.id_lugar_turistico
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .align(alignment = Alignment.BottomCenter)
            )
        }


        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    texto_generico_one_line(
                        "Lugares turisticos de barranca",
                        MaterialTheme.typography.titleLarge
                    )
                    spacer_vertical(5.dp)
                    texto_generico_multilinea(
                        "Selecciona tu lugar turístico favorito y ubícate fácilmente en el mapa. También puedes crear tu propia ruta directa con solo un botón.",
                        MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(5.dp)
                }
                items(lista_filtrada) { lugar ->
                    carta_turismo_google_mpa(
                        lugar,
                        seleccionado = (seleccionadoId == lugar.id_lugar_turistico)
                    ) { id, lat, log ->
                        val nuevaUbicacion = LatLng(lat, log)
                        seleccionadoId = id
                        latitud = lat
                        longitud = log
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(nuevaUbicacion, 16f),
                                1000
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel_cordenadas.lugares_turisticos("barranca")
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


//    LaunchedEffect(datosTienda) {
//        if (datosTienda.isNotEmpty()) {
//            Log.d("obtenoemos_datos_tienda", datosTienda.toString())
//            dataclass_tienda_seleccionada = datosTienda.first()
//        }
//    }
//
//    LaunchedEffect(show_bottom_sheeet) {
//        if (show_bottom_sheeet) {
//            viewModel_cordenadas.obtener_campos_tiendas_por_id("barranca", id_tienda.value)
//        }
//    }
//
//
//    LaunchedEffect(Unit) {
//        viewModel_cordenadas.obtener_tiendas_registradas("barranca")
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            val location =
//                fusedLocationClient.lastLocation.await()
//            location?.let {
//                val userLatLng = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
//                cameraPositionState.animate(
//                    update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
//                )
//            }
//        }
//    }

//    if (show_bottom_sheeet) {
//        bottom_sheet_tiendas_filtradas(
//            Color.Red,
//            viewModel_cordenadas,
//            dataclass_tienda_seleccionada, show_bottom_sheeet
//        ) {
//            show_bottom_sheeet = false
//        }
//    }
}

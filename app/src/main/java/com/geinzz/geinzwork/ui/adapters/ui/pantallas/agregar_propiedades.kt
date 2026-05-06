package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_propiedades
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import com.geinzz.geinzwork.model.repo_agregar_inmubles
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.google.android.gms.location.LocationServices
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck


@SuppressLint("MissingPermission")
@Composable
fun agregar_propiedades() {
    val lat_inicia = -10.749213848512397
    val lng_inicial = -77.76144964752426
    var obtener_datos: viewmodel_agregar_propiedades = viewModel()
    val context = LocalContext.current

    var alto by remember { mutableStateOf("") }
    var ancho by remember { mutableStateOf("") }

    var lat_txt by remember { mutableStateOf("") }
    var lng_txt by remember { mutableStateOf("") }

    var cordenadas_lat by remember { mutableStateOf(lat_inicia) }
    var cordenadas_lng by remember { mutableStateOf(lng_inicial) }

    var localidad_defaul by remember { mutableStateOf("barranca") }
    var lugares_nombres by remember { mutableStateOf<List<String>>(emptyList()) }
    var resultado_medicion_terreno by remember { mutableStateOf(0.0) }

    var titulo_geneado_variable by remember { mutableStateOf("") }
    var descripcion_generada_varible by remember { mutableStateOf("") }

    var tipo_terreno by remember { mutableStateOf("") }
    var localidad_terreno by remember { mutableStateOf("") }
    var tipo_operacion by remember { mutableStateOf("") }
    val lista_modelo_negocio = listOf("casa", "hotel", "terreno vacio", "edificio")
    val lista_localidades = listOf("barranca", "supe", "puerto supe", "paramonga","pativila")

    val lista_tipo_operacion = listOf("venta", "alquiler")

    // 👇 nuevos estados precio
    var precio_txt by remember { mutableStateOf("") }
    var refenencia by remember { mutableStateOf("") }
    var moneda_seleccionada by remember { mutableStateOf("dolares") } // USD o PEN
    var precio_por_m2 by remember { mutableStateOf(0.0) }

    val titulo_generado by obtener_datos.titulo.collectAsState()
    val texto_generado by obtener_datos.descripcion.collectAsState()
    val direccion by obtener_datos.nombre_calle.collectAsState()
    val scope = rememberCoroutineScope()

    val cargada_data by obtener_datos.estadoAgregar.collectAsState()

    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(alto, ancho) {
        if (alto.isNotEmpty() && ancho.isNotEmpty()) {
            resultado_medicion_terreno = verificar_metraje_propiedad(alto.toDouble(), ancho.toDouble())
        }
    }

    // 👇 calcular precio por m2 automáticamente
    LaunchedEffect(precio_txt, resultado_medicion_terreno) {
        val precio = precio_txt.toDoubleOrNull() ?: 0.0
        precio_por_m2 = if (resultado_medicion_terreno > 0.0 && precio > 0.0) {
            precio / resultado_medicion_terreno
        } else 0.0
    }

    LaunchedEffect(titulo_generado, texto_generado) {
        titulo_geneado_variable = titulo_generado
        descripcion_generada_varible = texto_generado
    }
    var tocandoMapa by remember { mutableStateOf(false) }
    fun limpiarCampos() {
        alto = ""
        ancho = ""
        lat_txt = ""
        lng_txt = ""
        cordenadas_lat = lat_inicia
        cordenadas_lng = lng_inicial
        precio_txt = ""
        refenencia = ""
        moneda_seleccionada = "dolares"
        tipo_terreno = ""
        localidad_terreno = ""
        tipo_operacion = ""
        resultado_medicion_terreno = 0.0
        precio_por_m2 = 0.0
        titulo_geneado_variable = ""
        descripcion_generada_varible = ""
        lugares_nombres = emptyList()
        managerLauncher.value?.deleteAll() // limpia el pin del mapa
        obtener_datos.limpiarCamposViewModel()  // limpia el ViewModel
    }

    LaunchedEffect(cargada_data) {
        if (cargada_data == viewmodel_agregar_propiedades.agregar_lugares.sucecs) {
            limpiarCampos()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
//        when(cargada_data){
//            viewmodel_agregar_propiedades.agregar_lugares.error ->{
//
//            }
//            viewmodel_agregar_propiedades.agregar_lugares.loading -> {
//
//            }
//            viewmodel_agregar_propiedades.agregar_lugares.sucecs ->{
//
//            }
//        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), userScrollEnabled = !tocandoMapa) {


            item {
                ExpandDropDown(lista_localidades, false, "", "localidad") { modelo ->
                    localidad_terreno = modelo
                }
            }

            item {
                ExpandDropDown(lista_modelo_negocio, false, "", "tipo de terreno") { modelo ->
                    tipo_terreno = modelo
                }
            }

            item {
                ExpandDropDown(lista_tipo_operacion, false, "", "tipo de operacion") { modelo ->
                    tipo_operacion = modelo
                }
            }

            item {
                texto_generico_one_line("metraje")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = alto,
                        onValueChange = { alto = it },
                        label = { texto_generico_one_line("Largo") },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = ancho,
                        onValueChange = { ancho = it },
                        label = { texto_generico_one_line("Ancho") },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 👇 item de precio + moneda + precio por m2
            item {
                texto_generico_one_line("precio")

                // chips de moneda
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    listOf("dolares", "soles").forEach { moneda ->
                        val seleccionado = moneda_seleccionada == moneda
                        Box(
                            modifier = Modifier
                                .clickable { moneda_seleccionada = moneda }
                                .background(
                                    color = if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (moneda == "dolares") "💵 dolares" else "🇵🇪 soles",
                                color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = precio_txt,
                    onValueChange = { precio_txt = it },
                    label = { texto_generico_one_line("Precio (${moneda_seleccionada})") },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

            }

            item {
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    tocandoMapa = event.changes.any { it.pressed }
                                }
                            }
                        }
                ) {
                    MapboxMap(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        MapStyle("mapbox://styles/benjaminlopez/cmm9c0hlt003901s54utw9p30")
                        MapEffect(Unit) { mapView ->
                            mapView.getMapboxMap().getStyle { style ->
                                val mapboxMap = mapView.getMapboxMap()
                                mapViewState.value = mapView
                                mapboxMapInstance = mapboxMap
                                managerLauncher.value = mapView.annotations.createPointAnnotationManager()

                                // ── Pin de ubicación del usuario ──
                                mapView.location.updateSettings {
                                    enabled = true
                                    pulsingEnabled = true
                                    puckBearingEnabled = true
                                    puckBearing = PuckBearing.HEADING
                                    locationPuck = createDefault2DPuck(withBearing = true)
                                }

                                // 👇 3. Dentro del MapEffect, después de configurar el location puck
                                mapView.location.updateSettings {
                                    enabled = true
                                    pulsingEnabled = true
                                    puckBearingEnabled = true
                                    puckBearing = PuckBearing.HEADING
                                    locationPuck = createDefault2DPuck(withBearing = true)
                                }

// 👇 Centrar cámara en ubicación del usuario al cargar
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        val punto = Point.fromLngLat(it.longitude, it.latitude)
                                        mapboxMap.easeTo(
                                            CameraOptions.Builder()
                                                .center(punto)
                                                .zoom(15.0)
                                                .build(),
                                            MapAnimationOptions.mapAnimationOptions {
                                                duration(800)
                                            }
                                        )
                                        // 👇 también inicializa las coordenadas con la ubicación real
                                        cordenadas_lat = it.latitude
                                        cordenadas_lng = it.longitude
                                        lat_txt = it.latitude.toString()
                                        lng_txt = it.longitude.toString()
                                    }
                                }
                                // 👇 fallback si lastLocation es null
                                val positionListener = OnIndicatorPositionChangedListener { point ->
                                    if (cordenadas_lat == lat_inicia) { // solo si aún no se movió
                                        mapboxMap.easeTo(
                                            CameraOptions.Builder()
                                                .center(point)
                                                .zoom(15.0)
                                                .build()
                                        )
                                        cordenadas_lat = point.latitude()
                                        cordenadas_lng = point.longitude()
                                        lat_txt = point.latitude().toString()
                                        lng_txt = point.longitude().toString()
                                    }
                                }
                                mapView.location.addOnIndicatorPositionChangedListener(positionListener)
                                // ── Click en el mapa → actualizar lat/lng ──
                                mapboxMap.addOnMapClickListener { point ->
                                    val lat = point.latitude()
                                    val lng = point.longitude()
                                    Log.d("MAP_CLICK", "Lat: $lat, Lng: $lng")

                                    lat_txt = lat.toString()
                                    lng_txt = lng.toString()
                                    cordenadas_lat = lat
                                    cordenadas_lng = lng

                                    // 👇 limpiar pin anterior y colocar uno nuevo
                                    managerLauncher.value?.deleteAll()

                                    val bitmap = crearBitmapPin()  // función de abajo
                                    val imageId = "pin_seleccionado"

                                    mapboxMap.getStyle { style ->
                                        style.removeStyleImage(imageId)
                                        style.addImage(imageId, bitmap)

                                        managerLauncher.value?.create(
                                            PointAnnotationOptions()
                                                .withPoint(point)
                                                .withIconImage(imageId)
                                                .withIconAnchor(IconAnchor.BOTTOM)
                                                .withIconSize(1.2)
                                        )
                                    }

                                    false
                                }
                            }
                        }
                    }
                }
            }

            item {
                texto_generico_one_line("coordenadas")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = lat_txt,
                        onValueChange = {
                            lat_txt = it
                            it.toDoubleOrNull()?.let { v -> cordenadas_lat = v }
                        },
                        label = { texto_generico_one_line("Lat") },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = lng_txt,
                        onValueChange = {
                            lng_txt = it
                            it.toDoubleOrNull()?.let { v -> cordenadas_lng = v }
                        },
                        label = { texto_generico_one_line("Lng") },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                    OutlinedTextField(
                        value = refenencia,
                        onValueChange = { refenencia = it },
                        label = { texto_generico_one_line("referencia") },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )


                    if (precio_por_m2 > 0.0) {
                        texto_generico_one_line(
                            "Precio por m²: ${moneda_seleccionada} ${"%.2f".format(precio_por_m2)}/m²"
                        )
                    }


                spacer_vertical(20.dp)
                if (resultado_medicion_terreno != 0.0) {
                    texto_generico_multilinea("El terreno tiene un área de ${"%.2f".format(resultado_medicion_terreno)} m²")
                }
                spacer_vertical(20.dp)
                if (direccion.isNotEmpty()) {
                    texto_generico_multilinea("ubicado en: $direccion")
                }
                spacer_vertical(20.dp)
                if (lugares_nombres.isNotEmpty()) {
                    texto_generico_multilinea(lugares_nombres.joinToString(", "))
                }
                spacer_vertical(20.dp)
            }



            item {
                Button(onClick = {
                    scope.launch {
                        lugares_nombres = obtener_datos.obtener_lugares_cercanos(
                            cordenadas_lat, cordenadas_lng, localidad_defaul
                        )
                        obtener_datos.buscar_nombre_calle(cordenadas_lat, cordenadas_lng)
                    }
                }) {
                    texto_generico_multilinea("obtener datos")
                }
            }

            item {
                Button(onClick = {
                    scope.launch {
                        obtener_datos.generar_titulo_para_Casa(
                            tipo_realizado = tipo_terreno,
                            tipo_operacion = tipo_operacion,
                            nombre_Calle = direccion,
                            localidad = localidad_defaul,
                            lista_lugares = lugares_nombres
                        )
                    }
                }) {
                    texto_generico_one_line("obtener título y descripción")
                }
            }

            item {
                texto_generico_one_line("título")
                texto_generico_multilinea(titulo_geneado_variable)
            }

            item {
                texto_generico_one_line("descripción")
                texto_generico_multilinea(descripcion_generada_varible)
            }

            // 👇 botón guardar — log con todos los datos listos para DB
            item {
                Button(
                    onClick = {
                        obtener_datos.obtener_datos_aloglia()
//                        obtener_datos.agregar_noramilazicon()
//                        Log.d("GUARDAR_PROPIEDAD", "=============================")
//                        Log.d("GUARDAR_PROPIEDAD", "tipo_terreno: $tipo_terreno")
//                        Log.d("GUARDAR_PROPIEDAD", "tipo_operacion: $tipo_operacion")
//                        Log.d("GUARDAR_PROPIEDAD", "largo: $alto")
//                        Log.d("GUARDAR_PROPIEDAD", "ancho: $ancho")
//                        Log.d("GUARDAR_PROPIEDAD", "area_m2: ${"%.2f".format(resultado_medicion_terreno)}")
//                        Log.d("GUARDAR_PROPIEDAD", "precio: $precio_txt")
//                        Log.d("GUARDAR_PROPIEDAD", "moneda: $moneda_seleccionada")
//                        Log.d("GUARDAR_PROPIEDAD", "precio_por_m2: ${"%.2f".format(precio_por_m2)}")
//                        Log.d("GUARDAR_PROPIEDAD", "latitud: $cordenadas_lat")
//                        Log.d("GUARDAR_PROPIEDAD", "longitud: $cordenadas_lng")
//                        Log.d("GUARDAR_PROPIEDAD", "direccion: $direccion")
//                        Log.d("GUARDAR_PROPIEDAD", "localidad: $localidad_defaul")
//                        Log.d("GUARDAR_PROPIEDAD", "lugares_cercanos: ${lugares_nombres.joinToString(", ")}")
//                        Log.d("GUARDAR_PROPIEDAD", "titulo: $titulo_geneado_variable")
//                        Log.d("GUARDAR_PROPIEDAD", "descripcion: $descripcion_generada_varible")
//                        Log.d("GUARDAR_PROPIEDAD", "=============================")
//
//                        val anchoInt = ancho.toIntOrNull() ?: 0
//                        val fondoInt = alto.toIntOrNull() ?: 0
//                        val metrosInt = resultado_medicion_terreno.toInt()
//                        val precioInt = precio_txt.toIntOrNull() ?: 0
//
//                        val data = repo_agregar_inmubles.agregar_inmubles_datos(
//                            ancho = anchoInt,
//                            banos = "0",
//                            ciudad = localidad_terreno,
//                            descripcion = descripcion_generada_varible,
//                            direccion = direccion,
//                            distrito = localidad_terreno,
//                            divisa = moneda_seleccionada,
//                            estacionamiento = "0",
//                            fondo = fondoInt,
//                            habitaciones = "0",
//                            lat = cordenadas_lat,
//                            lng = cordenadas_lng,
//                            metros = metrosInt,
//                            nombre = titulo_geneado_variable,
//                            precio = precioInt,
//                            referencia = refenencia,
//                            tipoOperacion = tipo_operacion,
//                            tipoPropiedad = tipo_terreno
//                        )
//
//                        scope.launch {
//                            obtener_datos.agregar_lugar(data)
//                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    texto_generico_one_line("💾 Guardar propiedad")
                }
            }
        }
    }
}

fun verificar_metraje_propiedad(ancho: Double, largo: Double): Double {
    return ancho * largo
}

fun crearBitmapPin(): Bitmap {
    val size = 60
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#7C3AED")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)

    return bitmap
}
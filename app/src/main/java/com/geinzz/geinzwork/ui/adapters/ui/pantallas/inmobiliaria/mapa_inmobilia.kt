package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.R.attr.duration
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.data.model.datos_viewmodel_inmobiliara
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.loadBitmapFromUrl
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.toCircularBitmap
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_inmobiliara
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.ScaleBarPlugin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.ModifierLocalModifierNode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.geinzz.geinzwork.BuildConfig.MAPBOX_ACCESS_TOKEN
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.EstadoMapa
import com.geinzz.geinzwork.data.model.categorias_diltrado_mapa_inmobiliara
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dialog_seguridad_salud_algolia
import com.geinzz.geinzwork.data.model.localizate_geinz.iconos_creaciones_rutas
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.data.model.obj_pasado_clikeado_mapa
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_salud_seguridad_algolia
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_servicios_tramite
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.mapbox.geojson.LineString
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatearDistanciaDouble
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularDistanciaMetros

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mapa_inmobilia(
    id_user: String,
    viewModelLugares: viewModel_lugares_turisticos,
    viewmodelMapa: viewmodel_mapa_personalizado,
    viewModelFiltros: viewModel_filtado_tiendas,
    verificar_inter: Boolean,
    viewmodel_mapa_inmobilia: viewmodel_mapa_inmobiliara,
    iniciar_seccion: () -> Unit,
    crear_cuenta: () -> Unit
) {
    val viewmode_servicios_tramite: viewmode_servicios_tramite = viewModel()
    var ulr_esilo by remember { mutableStateOf("") }
    var estilo_mapa_mapbox by remember { mutableStateOf(false) }
    val contex = LocalContext.current
    var chipSeleccionado by remember { mutableStateOf("Principal") }
    val datos_obtener_mapa by viewmodel_mapa_inmobilia.datosInmueble.collectAsState()
    var confuracion_seleccionda by remember { mutableStateOf("Mapa nocturno") }
    var pitch_selecciondo by remember { mutableStateOf("2D") }
    var mostrar_ocultar_immagen by remember { mutableStateOf(true) }
    var lista_seleccionada by remember { mutableStateOf(obj_pasado_clikeado_mapa()) }
    val seguirUbicacion = remember { mutableStateOf(false) }
    val ruta_ref = remember { mutableStateOf<List<Point>>(emptyList()) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()

    var mostar_dialog_lugare_Cercanos by remember { mutableStateOf(false) }
    var mostra_lugar_seguro_dialog by remember { mutableStateOf(false) }
    var mostrar_lugares_hogares by remember { mutableStateOf(false) }

    var id_negocio_lugar_previwe by remember { mutableStateOf("") }
    var localida_negocio_lugar_preview by remember { mutableStateOf("") }
    var nombre_negocio_select_preview by remember { mutableStateOf("") }
    var img_negocio_preview by remember { mutableStateOf("") }
    val bottomSheetVisible by viewmodelMapa.estadoBottomSheet.collectAsState()
    val datos_numeros_salud_seguridad by viewModelFiltros.instance_salud_seguridad.collectAsState()
    var seleccionado_posible by remember { mutableStateOf<String?>(null) }
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }

// ── Estado de ruta ─────────────────────────────────────────
    var puntos_ruta_activa by remember { mutableStateOf<List<Point>>(emptyList()) }
    var distancia_ruta_metros by remember { mutableStateOf(0) }
    var velocidad_actual by remember { mutableStateOf(0f) }
    var perfil_creacion_ruta_seleccionada by remember { mutableStateOf("") }
    var icono_creacion_ruta_seleccionada by remember {
        mutableStateOf(Icons.Default.Place)
    }
    var distancia_al_destino by remember { mutableStateOf(0f) }


    LaunchedEffect(perfil_creacion_ruta_seleccionada) {
        icono_creacion_ruta_seleccionada = when (perfil_creacion_ruta_seleccionada) {
            "driving" -> {
                Icons.Default.DirectionsCar
            }

            "walking" -> {
                Icons.Default.DirectionsWalk
            }

            "cycling" -> {
                Icons.Default.DirectionsBike
            }

            else -> {
                Icons.Default.DirectionsCar
            }
        }
    }
    val lista_iconos_ruta = remember {
        listOf(
            iconos_creaciones_rutas("driving", Icons.Default.DirectionsCar),
            iconos_creaciones_rutas("walking", Icons.Default.DirectionsWalk),
            iconos_creaciones_rutas("cycling", Icons.Default.DirectionsBike)
        )
    }
    val ruta_creada_state = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rutaCreadaRef = remember { mutableStateOf(false) }
    var ruta_cargando by remember { mutableStateOf(false) }
    var ruta_creada by ruta_creada_state
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
            confirmValueChange = { nuevoValor ->
                when (nuevoValor) {
                    SheetValue.Expanded -> !ruta_creada  // bloqueado con ruta
                    SheetValue.PartiallyExpanded -> true                  // siempre permitido
                    SheetValue.Hidden -> true                  // siempre permitido
                    else -> true
                }
            }
        )
    )

    var lat_user by remember { mutableStateOf(0.0) }
    var lng_user by remember { mutableStateOf(0.0) }
    var lat_lugar_seleccionado by remember { mutableStateOf(0.0) }
    var lng_lugar_seleccionado by remember { mutableStateOf(0.0) }


    LaunchedEffect(ruta_creada) {
        if (ruta_creada) {
            pitch_selecciondo = "3D"
            // ← Colapsar el sheet cuando se crea la ruta
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        } else {
            pitch_selecciondo = "2D"
        }
    }

//    val lista_configuracion = listOf(
//        "Mapa de dia", "Mapa nocturno"
//    )
//    val lista_2d_3d = listOf(
//        "3D", "2D"
//    )
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada = datosTienda!!.first()
//            viewModelFiltros.cast_horario_atencion_horario_tienda(datosTienda!!.first().horario_atencion)
        }
    }
    LaunchedEffect(mostar_dialog_lugare_Cercanos) {
        if (mostar_dialog_lugare_Cercanos) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localida_negocio_lugar_preview,
                id_negocio_lugar_previwe
            )
        }
    }
    LaunchedEffect(mostra_lugar_seguro_dialog) {
        if (mostra_lugar_seguro_dialog) {
            viewModelFiltros.obtener_numeros_seguridad_salud(
                localida_negocio_lugar_preview,
                id_negocio_lugar_previwe
            )
        }
    }

    LaunchedEffect(confuracion_seleccionda) {
        ulr_esilo = when (confuracion_seleccionda) {
            "Mapa de dia" -> {
                "mapbox://styles/benjaminlopez/cmm99ygby002w01s50jvt9r1h"
            }

            "Mapa nocturno" -> {
                "mapbox://styles/benjaminlopez/cmm9c0hlt003901s54utw9p30"
            }

            else -> {
                ""
            }
        }
    }

    LaunchedEffect(estilo_mapa_mapbox) {
        ulr_esilo = if (estilo_mapa_mapbox) {
            "mapbox://styles/benjaminlopez/cmm99ygby002w01s50jvt9r1h"
        } else {
            "mapbox://styles/benjaminlopez/cmm9c0hlt003901s54utw9p30"
        }
    }
    LaunchedEffect(chipSeleccionado) {
        mapboxMapInstance?.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(datos_obtener_mapa.longitud, datos_obtener_mapa.latitud))
                .zoom(14.0)
                .build(),
            MapAnimationOptions.mapAnimationOptions {
                duration(800)
            }
        )
    }
    LaunchedEffect(chipSeleccionado, datos_obtener_mapa, EstadoMapa.managerSecundario.value) {

        val lista = when (chipSeleccionado) {
            "Lugares seguros" -> obj_pasado_clikeado_mapa(
                "lugar_seguro",
                datos_obtener_mapa.cantidad_lugares_seguros
            )

            "Lugares cercanos" -> obj_pasado_clikeado_mapa(
                "lugar_cercanos",
                datos_obtener_mapa.cantidad_lugares_cercanos
            )

            "Lugares turísticos" -> obj_pasado_clikeado_mapa(
                "lugar_turistico",
                datos_obtener_mapa.cantidad_lugares_turisticos
            )

            "Lugares para el hogar" -> obj_pasado_clikeado_mapa(
                "lugar_servicios",
                datos_obtener_mapa.cantidad_lugares_para_el_hogar
            )

            else -> obj_pasado_clikeado_mapa()
        }
        lista_seleccionada = lista
        setear_puntos_clikeados(
            lista = lista, onPuntoClick = { id, lat, lng, img, nombre ->
                seleccionado_posible = id
                img_negocio_preview = img
                nombre_negocio_select_preview = nombre
                mapboxMapInstance?.easeTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(lng, lat))
                        .zoom(16.0)
                        .build(),
                    MapAnimationOptions.mapAnimationOptions {
                        duration(800)
                    }
                )
            }

        )
    }


    val categorias = listOf(
        categorias_diltrado_mapa_inmobiliara("Principal", 0),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares seguros",
            datos_obtener_mapa.cantidad_lugares_seguros.size
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares cercanos",
            datos_obtener_mapa.cantidad_lugares_cercanos.size
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares turísticos",
            datos_obtener_mapa.cantidad_lugares_turisticos.size
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares para el hogar",
            datos_obtener_mapa.cantidad_lugares_para_el_hogar.size
        )
    )


    // ✅ Escucha AMBOS: datos + manager listo
    LaunchedEffect(datos_obtener_mapa, managerLauncher.value, mapboxMapInstance) {
        val launcherManager = managerLauncher.value ?: return@LaunchedEffect
        val mapboxMap = mapboxMapInstance ?: return@LaunchedEffect

        val lat = datos_obtener_mapa.latitud
        val lng = datos_obtener_mapa.longitud

        if (lat == 0.0 && lng == 0.0) return@LaunchedEffect
        if (datos_obtener_mapa.lista_img.isEmpty()) return@LaunchedEffect

        val punto = Point.fromLngLat(lng, lat)
        launcherManager.deleteAll()

        val imageId = "launcher_icon"
        val bitmap =
            loadBitmapFromUrl(datos_obtener_mapa.lista_img.first(), contex).toCircularBitmap(130)

        mapboxMap.getStyle { style ->
            style.removeStyleImage(imageId)
            style.addImage(imageId, bitmap)

            launcherManager.create(
                PointAnnotationOptions()
                    .withPoint(punto)
                    .withIconImage(imageId)
                    .withIconAnchor(IconAnchor.BOTTOM)
                    .withIconSize(1.0)
            )

            // ✅ Centra cámara en el punto
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(punto)
                    .zoom(14.0)
                    .build()
            )
        }
    }


    LaunchedEffect(pitch_selecciondo, mapboxMapInstance) {
        val mapboxMap = mapboxMapInstance ?: return@LaunchedEffect

        val pitch = when (pitch_selecciondo) {
            "3D" -> 60.0
            "2D" -> 0.0
            else -> return@LaunchedEffect
        }

        mapboxMap.easeTo(
            CameraOptions.Builder()
                .pitch(pitch)
                .build(),
            MapAnimationOptions.mapAnimationOptions {
                duration(600)
            }
        )
    }

    val styleLoadedListener = remember {
        mutableStateOf<com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener?>(null)
    }
    // ── Redibujar ruta cuando cambia el estilo del mapa ──
    LaunchedEffect(ulr_esilo) {
        if (ulr_esilo.isEmpty()) return@LaunchedEffect
        val map = mapboxMapInstance ?: return@LaunchedEffect

        // Remover listener anterior si existe
        styleLoadedListener.value?.let { map.removeOnStyleLoadedListener(it) }

        // Crear nuevo listener
        val listener = com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener {
            if (puntos_ruta_activa.isNotEmpty()) {
                dibujarRutaEnMapa(map, puntos_ruta_activa)
            }
        }

        styleLoadedListener.value = listener
        map.addOnStyleLoadedListener(listener)
    }


// ── Vincula con ruta_creada_state ────────────────────────
    LaunchedEffect(ruta_creada_state.value) {
        rutaCreadaRef.value = ruta_creada_state.value
    }


// Agrega estas variables junto a tus otros remember en mapa_inmobilia

    var tipo_ruta by remember { mutableStateOf("") }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(contex) }
    val fabColor by animateColorAsState(
        targetValue = if (seguirUbicacion.value) MaterialTheme.colorScheme.primary else Color(
            0xFF9C7BFF
        ), animationSpec = tween(
            durationMillis = 300 // 0.3 segundos, suave pero rápido
        )
    )
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 80.dp, // 👈 TIRITA SIEMPRE VISIBLE
        sheetDragHandle = null,
        sheetContainerColor = Color.Black,
        sheetContent = {


            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // 👈 altura siempre automática al contenido
                    .navigationBarsPadding()
            ) {
                AnimatedVisibility(
                    visible = !mostrar_ocultar_immagen,
                    enter = fadeIn(tween(300)) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(90.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            val expandido =
                                scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

                            Crossfade(
                                targetState = expandido,
                                animationSpec = tween(90),
                                label = "crossfade_header"
                            ) { estaExpandido ->
                                if (estaExpandido) {
                                    // ── Expandido: solo texto ──
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Todo lo que rodea tu próximo terreno",
                                            fontSize = 20.sp,
                                            fontFamily = baners_geinz_work,
                                        )
                                        texto_generico_one_line(
                                            texto = "Conoce los lugares cercanos y toma una mejor decisión",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                } else {
                                    if (ruta_creada) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        ) {
                                            estilo_botons_circulares(
                                                color = Color.White,
                                                iconoTint = Color.Black,
                                                icon = Icons.Default.Close,
                                                onclick = {
                                                    ruta_creada = false
                                                    puntos_ruta_activa = emptyList()
                                                    distancia_ruta_metros = 0
                                                    mapboxMapInstance?.let {
                                                        limpiarRutaEnMapa(
                                                            it
                                                        )
                                                    }
                                                })
                                            Spacer(modifier = Modifier.weight(1f))
                                            Column() {
                                                texto_generico_one_line(distancia_al_destino.toString(), style = MaterialTheme.typography.titleLarge)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Icon(
                                                        icono_creacion_ruta_seleccionada,
                                                        contentDescription = "Mi ubicación",
                                                        tint = Color.Gray
                                                    )

                                                    texto_generico_one_line("1:40h", style = MaterialTheme.typography.labelSmall)

                                                }
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Column(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .border(2.dp, Color(0xFF7C3AED), CircleShape),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "${velocidad_actual.toInt()}",
                                                    color = Color.Black,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "km/h",
                                                    color = Color(0xFF7C3AED),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }

                                        }
                                    } else {
                                        // ── Colapsado: imagen + nombre ──
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            item {
                                                AsyncImage(
                                                    model = img_negocio_preview,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )

                                            }
                                            item {
                                                Text(
                                                    text = nombre_negocio_select_preview,
                                                    fontSize = 14.sp,
                                                    fontFamily = baners_geinz_work,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.width(100.dp) // 👈 toma el espacio disponible y cede a los botones
                                                )
                                            }
                                            item {
                                                desing_creacion_ruta(
                                                    puntos_para_la_ruta = puntos_ruta_activa,
                                                    distancia = distancia_ruta_metros,
                                                    velocidad = velocidad_actual,
                                                    context = contex,
                                                    lista = lista_iconos_ruta,
                                                    img_tienda = img_negocio_preview,
                                                    seleccionado = { perfil, icono ->
                                                        // 1️⃣ Obtener ubicación actual
                                                        perfil_creacion_ruta_seleccionada=perfil
                                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                            location?.let {
                                                                lat_user = it.latitude
                                                                lng_user = it.longitude

                                                                // 2️⃣ Buscar el destino seleccionado
                                                                val destino =
                                                                    lista_seleccionada.datos
                                                                        .firstOrNull { d -> d.id == seleccionado_posible }
                                                                        ?: return@let

                                                                // 3️⃣ Calcular y dibujar ruta
                                                                scope.launch {
                                                                    ruta_cargando = true
                                                                    ruta_creada = false
                                                                    val resultado = obtenerRuta(
                                                                        lat_user, lng_user,
                                                                        destino.lat, destino.lng,
                                                                        perfil
                                                                    )
                                                                    lng_lugar_seleccionado =
                                                                        destino.lng
                                                                    lat_lugar_seleccionado =
                                                                        destino.lat
                                                                    resultado?.let { (puntos, metros) ->
                                                                        puntos_ruta_activa = puntos
                                                                        distancia_ruta_metros =
                                                                            metros.toInt()

                                                                        // 4️⃣ Dibujar en el mapa
                                                                        mapboxMapInstance?.let { map ->
                                                                            dibujarRutaEnMapa(
                                                                                map,
                                                                                puntos
                                                                            )

                                                                            // 5️⃣ Centrar cámara entre origen y destino
                                                                            val latMedio =
                                                                                (lat_user + destino.lat) / 2
                                                                            val lngMedio =
                                                                                (lng_user + destino.lng) / 2
                                                                            map.easeTo(
                                                                                CameraOptions.Builder()
                                                                                    .center(
                                                                                        Point.fromLngLat(
                                                                                            lngMedio,
                                                                                            latMedio
                                                                                        )
                                                                                    )
                                                                                    .zoom(16.5)
                                                                                    .build(),
                                                                                MapAnimationOptions.mapAnimationOptions {
                                                                                    duration(
                                                                                        900
                                                                                    )
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                    ruta_creada =
                                                                        true   // ← ruta lista
                                                                }
                                                                ruta_cargando = false
                                                            }
                                                        }
                                                    },
                                                    cancelacion_ruta = {
                                                        // Limpiar estado y mapa
                                                        ruta_creada = false
                                                        puntos_ruta_activa = emptyList()
                                                        distancia_ruta_metros = 0
                                                        mapboxMapInstance?.let {
                                                            limpiarRutaEnMapa(
                                                                it
                                                            )
                                                        }
                                                    },
                                                    ocultar_dialog_ = {
                                                        // Colapsar bottom sheet al activar ruta
                                                        scope.launch {
                                                            scaffoldState.bottomSheetState.partialExpand()
                                                        }
                                                    },
                                                    mostrar_campo = {
                                                        // Abrir detalle del lugar seleccionado
                                                        val destino = lista_seleccionada.datos
                                                            .firstOrNull { it.id == seleccionado_posible }
                                                            ?: return@desing_creacion_ruta

                                                        when (lista_seleccionada.tipo) {
                                                            "lugar_cercanos" -> mostar_dialog_lugare_Cercanos =
                                                                true

                                                            "lugar_seguro" -> mostra_lugar_seguro_dialog =
                                                                true

                                                            "lugar_turistico" -> viewmodelMapa.setBottomSheetVisible(
                                                                true
                                                            )

                                                            "lugar_servicios" -> mostrar_lugares_hogares =
                                                                true
                                                        }
                                                        id_negocio_lugar_previwe = destino.id
                                                        localida_negocio_lugar_preview =
                                                            destino.localidad
                                                    },
                                                    mostar_dialog_no_ubi_activa = {
                                                        validacion_mostrar_dialog_ubi_off = true
                                                    }
                                                )
                                            }
                                            item {
                                                box_datos_botones_faciles(
                                                    onclick = {
                                                        val lista = lista_seleccionada.datos
                                                        if (lista.isEmpty()) return@box_datos_botones_faciles

                                                        val indexActual =
                                                            lista.indexOfFirst { it.id == seleccionado_posible }
                                                        val siguiente =
                                                            lista.getOrNull(indexActual + 1)
                                                                ?: lista.first() // vuelve al primero si llega al final

                                                        seleccionado_posible = siguiente.id
                                                        EstadoMapa.seleccionarPinPorId(siguiente.id)
                                                        img_negocio_preview = siguiente.img_String
                                                        nombre_negocio_select_preview =
                                                            siguiente.nombre
                                                        mapboxMapInstance?.easeTo(
                                                            CameraOptions.Builder()
                                                                .center(
                                                                    Point.fromLngLat(
                                                                        siguiente.lng,
                                                                        siguiente.lat
                                                                    )
                                                                )
                                                                .zoom(16.0)
                                                                .build(),
                                                            MapAnimationOptions.mapAnimationOptions {
                                                                duration(
                                                                    800
                                                                )
                                                            }
                                                        )
                                                    },
                                                    icono = Icons.Default.ArrowRight
                                                )
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        if (!ruta_creada) {
                            img_container(
                                lista_seleccionada = lista_seleccionada,
                                seleccionado_posible,
                                lugar_clikeado = { id, lat, lng, img, nombre ->
                                    seleccionado_posible = id
                                    EstadoMapa.seleccionarPinPorId(id)
                                    img_negocio_preview = img
                                    nombre_negocio_select_preview = nombre
                                    mapboxMapInstance?.easeTo(
                                        CameraOptions.Builder()
                                            .center(Point.fromLngLat(lng, lat))
                                            .zoom(16.0)
                                            .build(),
                                        MapAnimationOptions.mapAnimationOptions {
                                            duration(800)
                                        }
                                    )
                                },
                                ver_mas_ = { tipo, id, localidad, img, nombre ->
                                    Log.d("tipo_clikeado ", "$tipo $id $localidad")
                                    when (tipo) {
                                        "lugar_seguro" -> {
                                            mostra_lugar_seguro_dialog = true
                                            id_negocio_lugar_previwe = id
                                            localida_negocio_lugar_preview = localidad
                                            nombre_negocio_select_preview = nombre
                                            img_negocio_preview = img
                                        }

                                        "lugar_cercanos" -> {
                                            mostar_dialog_lugare_Cercanos = true
                                            id_negocio_lugar_previwe = id
                                            localida_negocio_lugar_preview = localidad

                                        }

                                        "lugar_turistico" -> {
                                            viewmodelMapa.setBottomSheetVisible(true)
                                            id_negocio_lugar_previwe = id
                                            localida_negocio_lugar_preview = localidad

                                        }

                                        "lugar_servicios" -> {
                                            mostrar_lugares_hogares = true
                                            id_negocio_lugar_previwe = id
                                            localida_negocio_lugar_preview = localidad

                                        }
                                    }

                                })
                        }
                    }
                }
                if (!ruta_creada) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListaChips(
                            modifier = Modifier.weight(1f),
                            categorias = categorias,
                            seleccionado = chipSeleccionado,
                            onSeleccionar = {
                                chipSeleccionado = it
                                if (it == "Principal") {
                                    mostrar_ocultar_immagen = true
                                } else {
                                    mostrar_ocultar_immagen = false
                                    if (!ruta_creada_state.value) {   // ← bloqueado si hay ruta
                                        scope.launch {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

            }


        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapboxMap(modifier = Modifier.fillMaxSize(), scaleBar = {}, compass = {}) {
                MapStyle(ulr_esilo)
                MapEffect(Unit) { mapView ->
                    mapView.getMapboxMap().getStyle { style ->
                        val mapboxMap = mapView.getMapboxMap()
                        mapViewState.value = mapView
                        mapboxMapInstance = mapboxMap

                        managerLauncher.value = mapView.annotations.createPointAnnotationManager()
                        managerLauncher.value?.addClickListener { annotation ->

                            val data = annotation.getData()?.asJsonObject

                            val id = data?.get("id")?.asString
                            val lat = data?.get("lat")?.asDouble
                            val lng = data?.get("lng")?.asDouble

                            Log.d("MARKER_CLICK", "ID: $id")
                            Log.d("MARKER_CLICK", "Lat: $lat, Lng: $lng")

                            true
                        }
                        // ✅ ESTO FALTABA — sin esto setear_puntos_clikeados siempre retorna
                        EstadoMapa.managerSecundario.value =
                            mapView.annotations.createPointAnnotationManager()
                        EstadoMapa.mapboxMapGlobal.value = mapboxMap
                        EstadoMapa.contextoGlobal = contex

                        mapView.getPlugin<ScaleBarPlugin>(Plugin.MAPBOX_SCALEBAR_PLUGIN_ID)?.enabled =
                            false
                        mapView.location?.apply {
                            enabled = true
                            pulsingEnabled = true
                        }
                        mapView.location.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                            showAccuracyRing = true
                            puckBearingEnabled = true
                            puckBearing = com.mapbox.maps.plugin.PuckBearing.HEADING
                            locationPuck =
                                com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck(
                                    withBearing = true
                                )
                        }
                        mapView.location.addOnIndicatorPositionChangedListener { point ->
                            lat_user = point.latitude()
                            lng_user = point.longitude()

                            if (lat_lugar_seleccionado != 0.0) {
                                distancia_al_destino = calcularDistanciaMetros(
                                    lat_user, lng_user,
                                    lat_lugar_seleccionado, lng_lugar_seleccionado
                                )
                                Log.d("distancia_realtime", "📍 ${distancia_al_destino.toInt()} metros")
                            }
                        }

                        mapboxMap.addOnMoveListener(object : OnMoveListener {
                            override fun onMoveBegin(detector: MoveGestureDetector) {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                }
                            }

                            override fun onMove(detector: MoveGestureDetector): Boolean = false
                            override fun onMoveEnd(detector: MoveGestureDetector) {}
                        })
                        mapboxMap.addOnMapClickListener { point ->

                            val lat = point.latitude()
                            val lng = point.longitude()

                            Log.d("MAP_CLICK", "Lat: $lat, Lng: $lng")

                            true
                        }
                    }
                }

            }
            FloatingActionButton(
                modifier = Modifier.padding(10.dp),
                containerColor = fabColor,
                contentColor = Color.White,
                onClick = {

                }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }
            FabMenuAjustes(
                confuracion_seleccionda = confuracion_seleccionda,
                onToggleDayNight = {
                    confuracion_seleccionda =
                        if (confuracion_seleccionda == "Mapa de dia") "Mapa nocturno" else "Mapa de dia"
                },
                pitch_selecciondo = pitch_selecciondo,
                onToggle3D = {
                    pitch_selecciondo = if (pitch_selecciondo == "2D") "3D" else "2D"
                }, modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopEnd)

            )
            AnimatedVisibility(
                visible = mostrar_ocultar_immagen,
                enter = fadeIn(tween(300)) + slideInVertically { it },
                exit = fadeOut(tween(200)) + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 85.dp)
            ) {
                estilo_carta_visual_inmueble(
                    modifier = Modifier,
                    datos_obtener_mapa
                )
            }

            if (mostra_lugar_seguro_dialog) {
                val (llamada, whatsapp, long) = datos_numeros_salud_seguridad
                dialog_salud_seguridad_algolia(
                    "",
                    long,
                    dialog_seguridad_salud_algolia(
                        whatsapp,
                        llamada,
                        nombre_negocio_select_preview,
                        img_negocio_preview
                    ),
                    ondimis = { mostra_lugar_seguro_dialog = false })
            }
            if (bottomSheetVisible) {
                bottom_sheet_lugares_turisticos(
                    localida_negocio_lugar_preview,
                    verificar_inter,
                    viewmodelMap = viewmodelMapa,
                    viewmodel_lugares_turisticos = viewModelLugares,
                    visible = true,
                    onClose = {
                        viewmodelMapa.setBottomSheetVisible(false)
                    },
                    ver_mapa = {
//                        abrir_mapa(
//                            "turismo",
//                            img_turismo,
//                            lat,
//                            lng
//                        )
                    }, iniciar_seccion = { iniciar_seccion() }, crear_cuenta = { crear_cuenta() },
                    id_negocio_lugar_previwe
                )
            }
            if (mostar_dialog_lugare_Cercanos) {
                bottom_sheet_tiendas_filtradas(
                    verificar_inter,
                    viewModelFiltros,
                    dataclass_tienda_seleccionada, mostar_dialog_lugare_Cercanos
                ) {
                    mostar_dialog_lugare_Cercanos = false
                }
            }
            if (mostrar_lugares_hogares) {
                dialog_servicios_tramite(
                    viewmode_servicios_tramite,
                    id_negocio_lugar_previwe,
                    localida_negocio_lugar_preview,
                    id_user,
                    localida_negocio_lugar_preview,
                    ondimis = { mostrar_lugares_hogares = false },
                )
            }
        }
    }


}

@Composable
fun ListaChips(
    modifier: Modifier,
    categorias: List<categorias_diltrado_mapa_inmobiliara>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        items(categorias) { categoria ->
            ChipEstilo(
                texto = categoria.nombre,
                cantidad = categoria.cantidad,
                estaSeleccionado = categoria.nombre == seleccionado,
                onClick = { onSeleccionar(categoria.nombre) }
            )
        }
    }
}

@Composable
fun ListaChips_configuraciones(
    modifier: Modifier,
    categorias: List<String>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        items(categorias) { categoria ->
            ChipEstilo(
                texto = categoria,
                cantidad = 0,
                estaSeleccionado = categoria == seleccionado,
                onClick = { onSeleccionar(categoria) }
            )
        }
    }
}

@Composable
fun ChipEstilo(
    texto: String,
    cantidad: Int,
    estaSeleccionado: Boolean = false,
    onClick: () -> Unit = {}
) {
    val cargando by EstadoMapa.cargandoPuntos
    val mostrarProgreso = estaSeleccionado && cargando

    // Animación de aparición suave de los iconos cuando termina
    val alphaIconos by animateFloatAsState(
        targetValue = if (estaSeleccionado && !cargando) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "alpha_iconos"
    )

    val fondo = if (estaSeleccionado) {
        Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF7C3AED)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF2D1B69), Color(0xFF3D2080)))
    }

    Box(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(50.dp))
            .then(
                if (estaSeleccionado)
                    Modifier.border(1.5.dp, Color(0xFFB17BFF), RoundedCornerShape(50.dp))
                else Modifier
            )
            .background(brush = fondo)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── Texto del chip ────────────────────
            val texto_final = if (cantidad == 0) texto else "$texto ($cantidad)"
            Text(
                text = texto_final,
                color = if (estaSeleccionado) Color.White else Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.3.sp
            )

            // ── Progress o check animado ──────────
            AnimatedVisibility(
                visible = estaSeleccionado,
                enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                exit = fadeOut(tween(200)) + scaleOut(tween(200))
            ) {
                if (mostrarProgreso) {
                    // Cargando → spinner
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    // Listo → check con fade suave
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = alphaIconos),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun img_container(
    lista_seleccionada: obj_pasado_clikeado_mapa,
    seleccionado: String?,
    lugar_clikeado: (id: String, lat: Double, lng: Double, img: String, nombre: String) -> Unit,
    ver_mas_: (tipo: String, id: String, localidad: String, img: String, nombre: String) -> Unit
) {

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(lista_seleccionada.datos) { datos ->
            val estaSeleccionado = seleccionado == datos.id

            val anchoAnimado by animateDpAsState(
                targetValue = if (estaSeleccionado) 118.dp else 100.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "ancho_${datos.nombre}"
            )
            val altoAnimado by animateDpAsState(
                targetValue = if (estaSeleccionado) 140.dp else 120.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "alto_${datos.nombre}"
            )

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .animateItem(
                        placementSpec = tween(
                            durationMillis = 350,
                            easing = FastOutSlowInEasing
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.width(anchoAnimado),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(datos.img_String)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .width(anchoAnimado)
                                .height(altoAnimado)
                                .then(
                                    if (estaSeleccionado)
                                        Modifier.border(
                                            2.dp,
                                            Color(0xFF7C3AED),
                                            RoundedCornerShape(15.dp)
                                        )
                                    else Modifier
                                )
                                .clickable {

                                    lugar_clikeado(
                                        datos.id,
                                        datos.lat,
                                        datos.lng,
                                        datos.img_String,
                                        datos.nombre
                                    )
                                },
//                                    placeholder = painterResource(com.geinzz.geinzwork.R.drawable.cargando_img_categorias),
//                            error = painterResource(R.drawable.cargando_img_categorias)
                        )

                        Box(
                            modifier = Modifier
                                .padding(5.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.85f))
                                .align(Alignment.BottomCenter)
                        ) {
                            texto_generico_one_line(
                                "A:${formatearDistanciaDouble(datos.distanciaKm)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 9.dp)
                            )
                        }
                        // Badge seleccionado
                        this@Column.AnimatedVisibility(
                            visible = estaSeleccionado,
                            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
                            modifier = Modifier
                                .padding(6.dp)
                        ) {
                            Row(modifier = Modifier.align(Alignment.TopStart)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7C3AED))

                                        .clickable {
                                            ver_mas_(
                                                lista_seleccionada.tipo,
                                                datos.id,
                                                datos.localidad,
                                                datos.img_String,
                                                datos.nombre
                                            )
                                        },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7C3AED)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }


                        }
                    }
                    texto_generico_one_line(
                        datos.nombre,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun estilo_carta_visual_inmueble(modifier: Modifier, datos: datos_viewmodel_inmobiliara) {
    val pagerState = rememberPagerState(pageCount = { datos.lista_img.size.coerceAtLeast(1) })
    var expandido by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val mitadPantalla = (configuration.screenHeightDp / 2).dp
    val alturaAnimada by animateDpAsState(
        targetValue = if (expandido) mitadPantalla else 260.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "altura"
    )
    val redondeoAnimado by animateDpAsState(
        // ✅ siempre mantiene el redondeo
        targetValue = 16.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "redondeo"
    )
    val iconoRotacion by animateFloatAsState(
        targetValue = if (expandido) 45f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotacion"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)          // ✅ padding fijo siempre
            .clip(RoundedCornerShape(redondeoAnimado))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(alturaAnimada)  // ✅ crece hasta mitad pantalla
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(datos.lista_img.getOrNull(page))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
//                        placeholder = painterResource(com.geinzz.geinzwork.R.drawable.cargando_img_categorias),
//                error = painterResource(R.drawable.cargando_img_categorias)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.68f))
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { expandido = !expandido },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expandido) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                contentDescription = if (expandido) "Reducir" else "Agrandar",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = iconoRotacion }
            )
        }

        if (datos.lista_img.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 52.dp)
            ) {
                repeat(datos.lista_img.size) { index ->
                    val tamaño by animateDpAsState(
                        targetValue = if (pagerState.currentPage == index) 8.dp else 5.dp,
                        animationSpec = tween(200),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(tamaño)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconoDato(icon = Icons.Rounded.KingBed, texto = datos.habitaciones.ifEmpty { "—" })
                IconoDato(icon = Icons.Rounded.Bathtub, texto = datos.banos.ifEmpty { "—" })
                IconoDato(
                    icon = Icons.Rounded.SquareFoot,
                    texto = if (datos.metros > 0) "${datos.metros.toInt()} m²" else "—"
                )
                if (datos.ancho > 0 && datos.fondo > 0) {
                    IconoDato(
                        icon = Icons.Rounded.Straighten,
                        texto = "${datos.ancho}×${datos.fondo}"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$ ${datos.precio.toLong()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            offset = Offset(0f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
            texto_generico_one_line(
                datos.nombre.capitalizeFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun IconoDato(icon: ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    // sombra sutil al icono
                    shadowElevation = 4f
                }
        )
        Text(
            text = texto,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.55f),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f
                )
            )
        )
    }
}


// ── Setear puntos clickeados en el mapa ───────
fun setear_puntos_clikeados(
    lista: obj_pasado_clikeado_mapa,
    onPuntoClick: (id: String, lat: Double, lng: Double, img: String, nombre: String) -> Unit
) {
    val manager = EstadoMapa.managerSecundario.value ?: return
    val mapboxMap = EstadoMapa.mapboxMapGlobal.value ?: return
    val contexto = EstadoMapa.contextoGlobal ?: return

    manager.deleteAll()
    EstadoMapa.idPuntoSeleccionado.value = null  // 👈 reset al limpiar

    if (lista.datos.isEmpty()) {
        EstadoMapa.cargandoPuntos.value = false
        return
    }

    manager.clickListeners.clear()
    manager.addClickListener { annotation ->
        val data = annotation.getData()?.asJsonObject ?: return@addClickListener false

        val id = data.get("id")?.asString ?: "null"
        val lat = data.get("lat")?.asDouble ?: 0.0
        val lng = data.get("lng")?.asDouble ?: 0.0
        val img = data.get("img")?.asString ?: ""
        val nombre = data.get("nombre")?.asString ?: ""

        // ✅ Resetear tamaño de TODOS los marcadores
        manager.annotations.forEach { it.iconSize = 0.8 }

        // ✅ Agrandar solo el seleccionado
        annotation.iconSize = 1.3
        manager.update(annotation)  // 👈 forzar redibujado

        EstadoMapa.idPuntoSeleccionado.value = id  // 👈 guardar seleccionado

        onPuntoClick(id, lat, lng, img, nombre)
        true
    }

    EstadoMapa.cargandoPuntos.value = true

    mapboxMap.getStyle { style ->
        MainScope().launch {
            var completados = 0
            val total = lista.datos.size

            lista.datos.forEachIndexed { index, lugar ->
                val punto = Point.fromLngLat(lugar.lng, lugar.lat)
                val imageId = "lugar_icon_${lista.tipo}_$index"

                val data = JsonObject().apply {
                    addProperty("id", lugar.id)
                    addProperty("lat", lugar.lat)
                    addProperty("lng", lugar.lng)
                    addProperty("img", lugar.img_String)
                    addProperty("nombre", lugar.nombre)
                }

                try {
                    val bitmap = loadBitmapFromUrl(lugar.img_String, contexto).toCircularBitmap(100)
                    try {
                        style.removeStyleImage(imageId)
                    } catch (_: Exception) {
                    }
                    style.addImage(imageId, bitmap)
                } catch (e: Exception) {
                    val bitmapFallback = crearCirculoFallback(contexto)
                    try {
                        style.removeStyleImage(imageId)
                    } catch (_: Exception) {
                    }
                    style.addImage(imageId, bitmapFallback)
                }

                manager.create(
                    PointAnnotationOptions()
                        .withPoint(punto)
                        .withIconImage(imageId)
                        .withIconAnchor(IconAnchor.CENTER)
                        .withIconSize(0.8)  // 👈 tamaño normal
                        .withData(data)
                )

                completados++
                if (completados == total) {
                    EstadoMapa.cargandoPuntos.value = false
                }
            }
        }
    }
}

fun crearCirculoFallback(contexto: Context): Bitmap {
    val size = 80
    val bitmap =
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#7C3AED")
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bitmap
}

@Composable
fun FabMenuAjustes(
    modifier: Modifier,
    confuracion_seleccionda: String,
    onToggleDayNight: () -> Unit,
    pitch_selecciondo: String,
    onToggle3D: () -> Unit,
) {
    var expandido by remember { mutableStateOf(false) }

    val rotacionTuerca by animateFloatAsState(
        targetValue = if (expandido) 45f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "tuerca"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Botón principal (tuerca) ──────────────
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { expandido = !expandido },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Ajustes",
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { rotationZ = rotacionTuerca }
            )
        }

        // ── Items que aparecen debajo ─────────────
        AnimatedVisibility(
            visible = expandido,
            enter = fadeIn(tween(200)) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(220), shrinkTowards = Alignment.Top)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Sol / Luna ──
                val delayDia = 40
                FabItem(
                    icon = if (confuracion_seleccionda == "Mapa de dia")
                        Icons.Rounded.WbSunny else Icons.Rounded.NightlightRound,
                    iconTint = if (confuracion_seleccionda == "Mapa de dia")
                        Color(0xFFFFA500) else Color(0xFF9F5FFA),
                    borderColor = if (confuracion_seleccionda == "Mapa de dia")
                        Color(0xFFFFA500) else Color(0xFF7C3AED),
                    onClick = onToggleDayNight,
                    enterDelay = delayDia
                )

                // ── 2D / 3D ──
                FabItem(
                    icon = if (pitch_selecciondo == "3D")
                        Icons.Rounded.ViewInAr else Icons.Rounded.Map,
                    iconTint = Color(0xFF9F5FFA),
                    borderColor = Color(0xFF7C3AED),
                    isActive = pitch_selecciondo == "3D",
                    onClick = onToggle3D,
                    enterDelay = 90
                )
            }
        }
    }
}

@Composable
fun FabItem(
    icon: ImageVector,
    iconTint: Color,
    borderColor: Color,
    isActive: Boolean = false,
    onClick: () -> Unit,
    enterDelay: Int = 0
) {
    val escala by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "escala_fab"
    )
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                Color.White
            )
            .border(1.5.dp, borderColor, CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .graphicsLayer { scaleX = escala; scaleY = escala },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun box_datos_botones_faciles(onclick: () -> Unit, icono: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.primary
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onclick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ✅ Fuera de la función — singleton, se crea una sola vez
private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .build()

suspend fun obtenerRuta(
    originLat: Double, originLng: Double, destLat: Double, destLng: Double, profile: String
): Pair<List<Point>, Double>? {

    val url = "https://api.mapbox.com/directions/v5/mapbox/$profile/" +
            "$originLng,$originLat;$destLng,$destLat" +
            "?geometries=geojson&overview=full&steps=true&access_token=$MAPBOX_ACCESS_TOKEN"

    Log.d("DESVIO_DEBUG", "🌐 URL: $url")

    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()

            Log.d("DESVIO_DEBUG", "HTTP ${response.code} — body: ${body?.take(300)}")

            if (response.isSuccessful && body != null) {
                val json = JSONObject(body)
                val routes = json.getJSONArray("routes")

                Log.d("DESVIO_DEBUG", "Rutas en respuesta: ${routes.length()}")

                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val distanciaMetros = route.getDouble("distance")
                    val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")

                    val points = mutableListOf<Point>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)))
                    }

                    return@withContext Pair(points, distanciaMetros) // ← aquí el cambio
                } else {
                    Log.e("DESVIO_DEBUG", "❌ routes vacío")
                    null
                }
            } else {
                Log.e("DESVIO_DEBUG", "❌ HTTP error ${response.code}: ${body?.take(200)}")
                null
            }
        } catch (e: Exception) {
            Log.e("DESVIO_DEBUG", "💥 Excepción: ${e.javaClass.simpleName} — ${e.message}")
            null
        }
    }
}

fun dibujarRutaEnMapa(
    mapboxMap: MapboxMap,
    puntos: List<Point>
) {
    mapboxMap.getStyle { style ->
        // ── Limpia capa y source previos ──────────────
        try {
            style.removeStyleLayer("route_layer")
        } catch (_: Exception) {
        }
        try {
            style.removeStyleSource("route_source")
        } catch (_: Exception) {
        }

        if (puntos.isEmpty()) return@getStyle

        // ── Source con la línea ───────────────────────
        val featureCollection = FeatureCollection.fromFeature(
            Feature.fromGeometry(LineString.fromLngLats(puntos))
        )
        style.addSource(
            GeoJsonSource.Builder("route_source")
                .featureCollection(featureCollection)
                .build()
        )

        // ── Layer con estilo de línea ─────────────────
        style.addLayerBelow(
            com.mapbox.maps.extension.style.layers.generated.lineLayer(
                "route_layer", "route_source"
            ) {
                lineColor("#2563EB")
                lineOpacity(0.35)
                lineWidth(18.0)
                lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.ROUND)
                lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.ROUND)
                lineOpacity(0.9)
            },
            "road-label"   // se inserta debajo de las etiquetas de calle
        )
    }
}

fun limpiarRutaEnMapa(mapboxMap: MapboxMap) {
    mapboxMap.getStyle { style ->
        try {
            style.removeStyleLayer("route_layer")
        } catch (_: Exception) {
        }
        try {
            style.removeStyleSource("route_source")
        } catch (_: Exception) {
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun desing_creacion_ruta(
    puntos_para_la_ruta: List<Point>,
    distancia: Int,
    velocidad: Float,
    context: Context,
    lista: List<iconos_creaciones_rutas>,
    img_tienda: String,
    seleccionado: (String, ImageVector) -> Unit,
    cancelacion_ruta: () -> Unit,
    ocultar_dialog_: () -> Unit,
    mostrar_campo: () -> Unit,
    mostar_dialog_no_ubi_activa: () -> Unit
) {
    var seleccionadoActual by remember { mutableStateOf<String?>(null) }

    val listaVisible = if (seleccionadoActual == null) lista
    else lista.filter { it.tipo == seleccionadoActual }

    val distanciaKm = distancia / 1000.0

    // ── Velocímetro (solo cuando hay ruta activa) ──────
//    AnimatedVisibility(
//        visible = puntos_para_la_ruta.isNotEmpty() && seleccionadoActual != null,
//        enter = fadeIn(tween(300)) + scaleIn(tween(300)),
//        exit = fadeOut(tween(200)) + scaleOut(tween(200))
//    ) {
//        Column(
//            modifier = Modifier
//                .size(44.dp)
//                .clip(CircleShape)
//                .background(Color.White)
//                .border(2.dp, Color(0xFF7C3AED), CircleShape),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "${velocidad.toInt()}",
//                color = Color.Black,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.Bold
//            )
//            Text(
//                text = "km/h",
//                color = Color(0xFF7C3AED),
//                style = MaterialTheme.typography.labelSmall
//            )
//        }
//    }

    // ── Botones de tipo de ruta ────────────────────────
    listaVisible.forEach { item ->
        val deshabilitado = item.tipo == "walking" && distanciaKm > 20.0
        val estaActivo = seleccionadoActual == item.tipo

        val colorFondo by animateColorAsState(
            targetValue = when {
                deshabilitado -> Color.Gray
                estaActivo -> Color(0xFF5B21B6)   // más oscuro = activo
                else -> Color(0xFF7C3AED)
            },
            animationSpec = tween(250),
            label = "fondo_${item.tipo}"
        )


        Box(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(colorFondo)
                .then(
                    if (estaActivo)
                        Modifier.border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    else Modifier
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (deshabilitado) return@clickable
                    if (verificarUbiActiva(context)) {
                        if (seleccionadoActual == item.tipo) {
                            seleccionadoActual = null
                            cancelacion_ruta()
                        } else {
                            seleccionadoActual = item.tipo
                            seleccionado(item.tipo, item.icono)
                            ocultar_dialog_()
                        }
                    } else {
                        mostar_dialog_no_ubi_activa()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icono,
                contentDescription = item.tipo,
                tint = if (deshabilitado) Color.White.copy(alpha = 0.35f) else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }

}


@Composable
fun estilo_botons_circulares(
    color: Color,
    iconoTint: Color,
    icon: ImageVector,
    onclick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(
                color
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onclick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconoTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
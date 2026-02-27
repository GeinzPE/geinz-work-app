package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import androidx.compose.animation.AnimatedVisibility
import com.mapbox.maps.plugin.annotation.annotations
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permisos_llamadas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.requestCallPermission
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_mapa
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.MarkerIcon
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.dialogo_lugar_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.actualizarUbicacion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isLocationEnabled
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.generated.fillExtrusionLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Polygon
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.properties.generated.CirclePitchAlignment
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.scalebar.ScaleBarPlugin
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.text.get

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMapDark(
    img_lugare_dircto: String?, lat_lugar_directo: Double?, lng_lugar_directo: Double?,
    viewmodelMapa: viewmodel_mapa_personalizado,
    localidad: String, id_user: String, tipo: String,
    verificar_intener: Boolean, viewmodel_lugares_turisticos: viewModel_lugares_turisticos,
    viewModel_filtrado_tiendas: viewModel_filtado_tiendas,
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")

        }
    }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    val pointAnnotationManager = remember { mutableStateOf<PointAnnotationManager?>(null) }


    val radio by viewmodel_lugares_turisticos.estado_radio_filtrada.collectAsState()

    val coordenadas by viewmode_segurirdad_Salud.coordenadasSeleccionadas.observeAsState()
    var latitud_luga_seg by remember { mutableStateOf(0.0) }
    var long_luga_seg by remember { mutableStateOf(0.0) }
    val lista_filtrada_tiendas by viewModel_filtrado_tiendas.listaTiendasGuardadas.observeAsState(
        emptyList()
    )
    val lista_tiendas_cecanas_turismo by viewmodel_lugares_turisticos.listaTiendasGuardadas
        .collectAsState()


    val lista_categiras_filtrado_tiendas_Cercanas by viewmodel_lugares_turisticos.lista_categoira_filtradas.collectAsState()
    val imageCache = remember { mutableMapOf<String, Bitmap>() }
    val estado by viewmodel_lugares_turisticos.estadoFiltrado.collectAsState()

    val datosTienda by viewModel_filtrado_tiendas._datos_tienda.observeAsState(emptyList())
    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    var currentIndex =
        lista_filtrada_tiendas.indexOfFirst { data -> data.id_tienda == seleccionadoId }

    var currentIndex_turismo =
        lista_tiendas_cecanas_turismo.indexOfFirst { data -> data.id_tienda == seleccionadoId }

    coordenadas?.let { (lat, lon) ->
        latitud_luga_seg = lat
        long_luga_seg = lon
    }
    val tick by viewModel_filtrado_tiendas.tick.collectAsState()
    val annotationsById = remember { mutableMapOf<String, PointAnnotation>() }
    var selectedAnnotation by remember { mutableStateOf<PointAnnotation?>(null) }

    var lister_marker by remember { mutableStateOf(dataclass_map()) }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var id_tienda by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf(0.0) }
    var show_botoom_sheet by remember { mutableStateOf(true) }
    var show_dialog_datos_lugares by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var log_user by remember { mutableStateOf(0.0) }
    var lat_user by remember { mutableStateOf(0.0) }
    var boxVisible by remember { mutableStateOf(true) }
    var mostar_bottom_sheet by remember { mutableStateOf(false) }
    var id_lugar_tienda_select by remember { mutableStateOf("") }
    var localidad_tienda_lugar_Select by remember { mutableStateOf(localidad) }
    var show_bottom_sheet_datos_tienda_lugares by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var primeravez by remember { mutableStateOf(true) }
    val seguirUbicacion = remember { mutableStateOf(false) }
    val animatingMap = remember { mutableStateOf(false) }
    val horarios by viewModel_filtrado_tiendas.horariosTiendas_real.collectAsState()

    fun animarTamano(
        annotation: PointAnnotation,
        desde: Double,
        hasta: Double
    ) {
        val animator = ValueAnimator.ofFloat(desde.toFloat(), hasta.toFloat())
        animator.duration = 250 // duración en milisegundos

        animator.addUpdateListener { animation ->
            val valor = animation.animatedValue as Float
            annotation.iconSize = valor.toDouble()
            pointAnnotationManager.value?.update(annotation)
        }

        animator.start()
    }

    fun seleccionarMarkerPorId(id: String) {

        val nuevo = annotationsById[id] ?: return

        selectedAnnotation?.let { anterior ->
            animarTamano(anterior, anterior.iconSize ?: 1.3, 0.9)
            anterior.iconOpacity = 0.6
            pointAnnotationManager.value?.update(anterior)
        }

        animarTamano(nuevo, nuevo.iconSize ?: 0.9, 1.3)
        nuevo.iconOpacity = 1.0
        pointAnnotationManager.value?.update(nuevo)

        selectedAnnotation = nuevo
    }

    LaunchedEffect(Unit) {
        while (true) {
            isLocationEnabled = isLocationEnabled(context)
            delay(5000L)
        }
    }
    if (isLocationEnabled) {
        viewmodelMapa.actualziar_estado(true)

    } else {
        viewmodelMapa.actualziar_estado(false)
    }

    LaunchedEffect(lister_marker.id) {

        if (lister_marker.id.isBlank()) return@LaunchedEffect

        Log.d("id_tienda_cambiada", "${lister_marker.id} ${lister_marker.horario_box}")

        viewModel_filtrado_tiendas.calcularHorarioParaTienda(
            lister_marker.id,
            lister_marker.horario_box
        )
    }
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



    viewModel_filtrado_tiendas.repo_filtrado.escucharHorarioDeTiendaUnica(
        idTiendaBuscada = lister_marker.id,
        localidad = "barranca"
    )


    val snackbarHostState = remember { SnackbarHostState() }
    val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }

    var primeraCargaCamara by remember { mutableStateOf(true) }

    val defaultLocation_barranca = Point.fromLngLat(-77.76088112286742, -10.751480371828691)
    val defaultLocation_paramonga = Point.fromLngLat(-77.81957068618482, -10.678480703018984)
    val defaultLocation_supe = Point.fromLngLat(-77.71618154413743, -10.795610086889571)
    val defaultLocation_puerto_supe = Point.fromLngLat(-77.74082770132752, -10.796606548738318)
    val defaultLocation_pativilca = Point.fromLngLat(-77.77668811678933, -10.696153944234334)
    val localidad_default = when (localidad) {
        "barranca" -> defaultLocation_barranca
        "paramonga" -> defaultLocation_paramonga
        "pativilca" -> defaultLocation_pativilca
        "supe" -> defaultLocation_supe
        "puerto_supe" -> defaultLocation_puerto_supe
        else -> defaultLocation_barranca
    }
    val coroutineScope = rememberCoroutineScope()
    var styleReady by remember { mutableStateOf(false) }

    LaunchedEffect(radio, lat_lugar_directo, lng_lugar_directo, styleReady) {

        if (!styleReady) return@LaunchedEffect
        if (lat_lugar_directo == null || lng_lugar_directo == null) return@LaunchedEffect

        val punto = Point.fromLngLat(lng_lugar_directo, lat_lugar_directo)
        val radioEnMetros = radio * 100.0

        mapboxMapInstance?.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("launcher_circle_source")

            source?.featureCollection(
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(
                        createCirclePolygon(punto, radioEnMetros)
                    )
                )
            )
        }
    }


    LaunchedEffect(lista_tiendas_cecanas_turismo, pointAnnotationManager.value) {

        val manager = pointAnnotationManager.value ?: return@LaunchedEffect
        val mapboxMap = mapboxMapInstance ?: return@LaunchedEffect
        if (lista_tiendas_cecanas_turismo.isEmpty()) return@LaunchedEffect

        manager.deleteAll()
        annotationsById.clear()
        selectedAnnotation = null

        val results = coroutineScope {
            lista_tiendas_cecanas_turismo.map { tienda ->
                async(Dispatchers.IO) {

                    val imageUrl = tienda.logo_tienda
                    val imageId = "marker-${tienda.id_tienda}"

                    var bitmap = imageCache[imageUrl]

                    if (bitmap == null) {
                        bitmap = loadBitmapFromUrl(imageUrl, context)
                            .toCircularBitmap(sizePx = 120)
                        imageCache[imageUrl] = bitmap
                    }

                    Triple(tienda, imageId, bitmap)
                }
            }.awaitAll()
        }

        mapboxMap.getStyle { style ->

            for ((tienda, imageId, bitmap) in results) {

                if (style.getStyleImage(imageId) == null) {
                    style.addImage(imageId, bitmap)
                }

                val option = PointAnnotationOptions()
                    .withPoint(
                        Point.fromLngLat(
                            tienda.longitud,
                            tienda.latitud
                        )
                    )
                    .withIconImage(imageId)
                    .withIconSize(0.9)
                    .withIconOpacity(0.6)

                val annotation = manager.create(option)

                annotationsById[tienda.id_tienda] = annotation
            }
            if (seleccionadoId == null && lista_tiendas_cecanas_turismo.isNotEmpty()) {
                seleccionadoId = lista_tiendas_cecanas_turismo.first().id_tienda
            }
        }
    }

    LaunchedEffect(seleccionadoId) {

        seleccionadoId?.let { id ->
            seleccionarMarkerPorId(id)
        }
    }

    LaunchedEffect(
        img_lugare_dircto,
        lat_lugar_directo,
        lng_lugar_directo,
        managerLauncher.value
    ) {

        if (img_lugare_dircto == null ||
            lat_lugar_directo == null ||
            lng_lugar_directo == null
        ) return@LaunchedEffect

        val launcherManager = managerLauncher.value ?: return@LaunchedEffect
        val mapboxMap = mapboxMapInstance ?: return@LaunchedEffect

        val punto = Point.fromLngLat(
            lng_lugar_directo,
            lat_lugar_directo
        )

        launcherManager.deleteAll()

        val imageId = "launcher_icon"

        // ✅ AQUI SÍ puedes llamar suspend
        val bitmap = loadBitmapFromUrl(img_lugare_dircto, context)
            .toCircularBitmap(130)

        // 🔥 Ahora solo usas style
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
        }
    }
    LaunchedEffect(
        lat_lugar_directo,
        lng_lugar_directo,
        mapboxMapInstance,
        styleReady
    ) {

        if (!styleReady) return@LaunchedEffect
        val mapboxMap = mapboxMapInstance ?: return@LaunchedEffect

        val punto = if (lat_lugar_directo != null && lng_lugar_directo != null) {
            Point.fromLngLat(lng_lugar_directo, lat_lugar_directo)
        } else {
            localidad_default
        }

        if (primeraCargaCamara) {

            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(punto)
                    .zoom(16.0)
                    .build()
            )

            primeraCargaCamara = false
        } else {

            mapboxMap.flyTo(
                CameraOptions.Builder()
                    .center(punto)
                    .zoom(16.0)
                    .build(),
                MapAnimationOptions.Builder()
                    .duration(1500)
                    .build()
            )
        }
    }


    val fabColor by animateColorAsState(
        targetValue = if (seguirUbicacion.value) MaterialTheme.colorScheme.primary else Color(
            0xFF9C7BFF
        ),
        animationSpec = tween(
            durationMillis = 300 // 0.3 segundos, suave pero rápido
        )
    )
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val mapView = mapViewState.value

    LaunchedEffect(seguirUbicacion.value) {
        if (seguirUbicacion.value) {
            mapboxMapInstance?.setCamera(
                CameraOptions.Builder()
                    .zoom(16.0)
                    .build()
            )
        }
        val mensaje = if (seguirUbicacion.value) {
            "Seguimiento automático activado"

        } else {
            "Seguimiento automático desactivado"
        }

        snackbarHostState.showSnackbar(
            message = mensaje,
            duration = SnackbarDuration.Short
        )
    }
    val seguirActual by rememberUpdatedState(seguirUbicacion.value)

    DisposableEffect(mapView) {

        if (mapView == null) return@DisposableEffect onDispose { }

        val locationPlugin = mapView.location
        val gesturesPlugin = mapView.gestures
        val mapboxMap = mapView.getMapboxMap()

        val locationListener = OnIndicatorPositionChangedListener { point ->
            if (show_dialog_datos_lugares) {
                lat_user = point.latitude()
                log_user = point.longitude()
            }

            if (!seguirActual) return@OnIndicatorPositionChangedListener

            val puntoUsuario = Point.fromLngLat(
                point.longitude(),
                point.latitude()
            )

            mapboxMap.easeTo(
                CameraOptions.Builder()
                    .center(puntoUsuario)
                    .build(),
                MapAnimationOptions.Builder()
                    .duration(500)
                    .build()
            )
        }
        val moveListener = object : OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                if (seguirUbicacion.value) {
                    seguirUbicacion.value = false
                }
            }

            override fun onMove(detector: MoveGestureDetector): Boolean = false
            override fun onMoveEnd(detector: MoveGestureDetector) {}
        }

        locationPlugin.addOnIndicatorPositionChangedListener(locationListener)
        gesturesPlugin.addOnMoveListener(moveListener)

        onDispose {
            locationPlugin.removeOnIndicatorPositionChangedListener(locationListener)
            gesturesPlugin.removeOnMoveListener(moveListener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        MapboxMap(modifier = Modifier.fillMaxSize(), scaleBar = { }) {

            MapStyle("mapbox://styles/benjaminlopez/cmm3v8k35002c01s8fgun4mas")
            MapEffect(Unit) { mapView ->

                val locationPlugin = mapView.location
                val mapboxMap = mapView.getMapboxMap()
                val gesturesPlugin = mapView.gestures
                mapViewState.value = mapView
                mapboxMapInstance = mapboxMap

                mapView.getPlugin<ScaleBarPlugin>(
                    Plugin.MAPBOX_SCALEBAR_PLUGIN_ID
                )?.enabled = false

                mapView.location?.apply {
                    enabled = true
                    pulsingEnabled = true
                }

                if (managerLauncher.value == null) {
                    managerLauncher.value =
                        mapView.annotations.createPointAnnotationManager()
                }

                if (pointAnnotationManager.value == null) {

                    pointAnnotationManager.value =
                        mapView.annotations.createPointAnnotationManager()

                    pointAnnotationManager.value?.addClickListener { annotation ->

                        val tienda = lista_tiendas_cecanas_turismo
                            .find { "marker-${it.id_tienda}" == annotation.iconImage }

                        tienda?.let {

                            lister_marker = dataclass_map(
                                img = it.logo_tienda,
                                nombre = it.nombre_tienda,
                                tag = it.lista_subcategoiras,
                                my_latitud = lat_user,
                                my_longitud = log_user,
                                latitud = it.latitud,
                                longitud = it.longitud,
                                id = it.id_tienda,
                                categoria = "",
                                direccion = it.direccion,
                                referencia = it.referencia,
                                contacto_tienda = it.contacto_tienda,
                                metodos_pago_tienda = it.metodos_pago_tienda,
                                horario_box = it.horario_box,
                                localidad = it.localidad_tienda
                            )

                            seleccionadoId = it.id_tienda
                            show_dialog_datos_lugares = true
                        }

                        true
                    }
                }


                // Crear source del círculo SOLO UNA VEZ
                mapboxMap.getStyle { style ->
                    if (style.getSource("launcher_circle_source") == null) {

                        style.addSource(
                            geoJsonSource("launcher_circle_source") {
                                featureCollection(
                                    FeatureCollection.fromFeatures(emptyArray())
                                )
                            }
                        )

                        style.addLayer(
                            fillLayer("launcher_circle_layer", "launcher_circle_source") {

                                fillOpacity(0.2)
                            }
                        )
                        styleReady = true // ✅ aquí sí
                    }
                }


            }

        }
        FloatingActionButton(
            modifier = Modifier.padding(10.dp),
            containerColor = fabColor,
            contentColor = Color.White,
            onClick = {
                if (verificarUbiActiva(context)) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val userPoint = Point.fromLngLat(it.longitude, it.latitude)
                            coroutineScope.launch {
                                mapboxMapInstance?.easeTo(
                                    CameraOptions.Builder()
                                        .center(userPoint)
                                        .zoom(16.0)
                                        .build(),
                                    MapAnimationOptions.Builder()
                                        .duration(800)
                                        .build()
                                )
                                seguirUbicacion.value = true
                                animatingMap.value = false
                            }
                        }
                    }
                } else {
                    validacion_mostrar_dialog_ubi_off = true
                }
            }
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
        }

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
                    when (tipo) {
                        "turismo" -> {
                            if (lista_tiendas_cecanas_turismo.isNotEmpty()) {
                                // 🔹 Recalcular índice del seleccionado
                                currentIndex_turismo =
                                    lista_tiendas_cecanas_turismo.indexOfFirst { it.id_tienda == seleccionadoId }

                                Log.d("currentIndex", currentIndex_turismo.toString())

                                // 🔹 Validar índice y calcular siguiente
                                val siguienteIndex =
                                    if (currentIndex_turismo in lista_tiendas_cecanas_turismo.indices) {
                                        (currentIndex_turismo + 1) % lista_tiendas_cecanas_turismo.size
                                    } else {
                                        0 // si no hay seleccionado, empezar por el primero
                                    }

                                val tienda = lista_tiendas_cecanas_turismo.getOrNull(siguienteIndex)

                                tienda?.let {
                                    Log.d("currentIndex", "$siguienteIndex")
                                    lister_marker = dataclass_map(
                                        img = it.logo_tienda,
                                        nombre = it.nombre_tienda,
                                        tag = it.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = it.latitud,
                                        longitud = it.longitud,
                                        id = it.id_tienda,
                                        categoria = "",
                                        direccion = it.direccion,
                                        referencia = it.referencia,
                                        contacto_tienda = it.contacto_tienda,
                                        metodos_pago_tienda = it.metodos_pago_tienda,
                                        horario_box = it.horario_box,
                                        localidad = it.localidad_tienda
                                    )

                                    seleccionadoId = it.id_tienda
                                    show_dialog_datos_lugares = true

                                    val location_turismo = Point.fromLngLat(it.longitud, it.latitud)

                                    coroutineScope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder()
                                                .center(location_turismo)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
                                }
                            }
                        }

                        "tiendas" -> {
                            if (lista_filtrada_tiendas.isNotEmpty()) {
                                // 🔹 Recalcular índice del seleccionado
                                currentIndex =
                                    lista_filtrada_tiendas.indexOfFirst { it.id_tienda == seleccionadoId }

                                Log.d("currentIndex", currentIndex.toString())

                                // 🔹 Validar índice
                                val siguienteIndex =
                                    if (currentIndex in lista_filtrada_tiendas.indices) {
                                        (currentIndex + 1) % lista_filtrada_tiendas.size
                                    } else {
                                        0 // si no hay seleccionado, empieza por el primero
                                    }

                                val tienda = lista_filtrada_tiendas.getOrNull(siguienteIndex)

                                tienda?.let {
                                    Log.d("currentIndex", "$siguienteIndex")
                                    lister_marker = dataclass_map(
                                        img = it.logo_tienda,
                                        nombre = it.nombre_tienda,
                                        tag = it.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = it.latitud,
                                        longitud = it.longitud,
                                        id = it.id_tienda,
                                        categoria = "",
                                        direccion = it.direccion,
                                        referencia = it.referencia,
                                        contacto_tienda = it.contacto_tienda,
                                        metodos_pago_tienda = it.metodos_pago_tienda,
                                        horario_box = it.horario_tienda_box,
                                        localidad = it.localidad_tienda
                                    )

                                    seleccionadoId = it.id_tienda
                                    show_dialog_datos_lugares = true

                                    val location_tienda = Point.fromLngLat(it.longitud, it.latitud)

                                    coroutineScope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder()
                                                .center(location_tienda)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
                                }
                            }
                        }

                        else -> {}
                    }

                },
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mi ubicación"
                )
            }
        }

        if (show_botoom_sheet && mapboxMapInstance != null) {
            bottom_sheet_mapa(
                lista_categiras_filtrado_tiendas_Cercanas,
                viewmodel_lugares_turisticos,
                estado,
                seleccionadoId = seleccionadoId ?: "",
                lat_user = lat_user,
                log_user = log_user,
                mapboxMap = mapboxMapInstance!!,
                tipo = tipo,
                lista_filtrada_turismo = lista_tiendas_cecanas_turismo,
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
                horario_box1 = horarios[lister_marker.id] ?: HorarioDia_box(),
                viewmodelMapa = viewmodelMapa,
                lat_user = lat_user, log_user = log_user,
                time = tick,
                dataclass_map = lister_marker,
                cerra_dialog = {
                    show_dialog_datos_lugares = false
                    mostar_bottom_sheet = true
                },
                limpiar = {
                    seleccionadoId = ""
                },
                crear_ruta = { id, lat, log ->
                    latitud = lat
                    longitud = log
                    id_tienda = id
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
                boxVisible = boxVisible,
                onBoxVisibleChange = {
                    Log.d("visible", it.toString())
                    boxVisible = it
                },
                centrar_camara = { lat, log ->
                    scope.launch {
                        val location = Point.fromLngLat(log, lat)

                        coroutineScope.launch {
                            mapboxMapInstance?.flyTo(
                                CameraOptions.Builder()
                                    .center(location)
                                    .zoom(16.0)
                                    .build(),

                                )
                        }
                    }
                },
                retornar_id_select = { id_tienda_lugar, color ->
                    id_lugar_tienda_select = id_tienda_lugar
                    show_bottom_sheet_datos_tienda_lugares = true
                },
                onclick_iconos = { id, datos ->
                    when (datos.nombre_red) {
                        "llamar" -> {
                            llamar(id_user, "tienda", id, localidad, context, datos.valor, {
                                call_dialog_permise = true
                                numero_llamada = datos.valor
                            })
                        }

                        "whatsapp" -> {
                            abrir_whattsapp(id_user, "tienda", id, localidad, context, datos.valor)
                        }

                        "tiktok" -> {
                            openTiktok(
                                "Tienda",
                                context = context,
                                username = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad, id_user
                            )
                        }

                        "facebook" -> {
                            Log.d("    datos.valor", "${datos.valor}")
                            openFacebook(
                                "Tienda",
                                context = context,
                                pageUrl = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad, id_user
                            )
                        }

                        "instagram" -> {
                            Log.d("    datos.valor", "${datos.valor}")
                            openInstagram(
                                "Tienda",
                                context = context,
                                url = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad, id_user
                            )
                        }

                        "Web" -> {
                            openWebLink(
                                context = context,
                                url = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad, id_user
                            )
                        }

                    }
                },
                mostrar_lista = {
                    show_botoom_sheet = true
                },
                move_derecha = {
                    when (tipo) {
                        "turismo" -> {
                            if (lista_tiendas_cecanas_turismo.isNotEmpty()) {
                                if (currentIndex_turismo != -1) {
                                    // Mover al elemento anterior (hacia la derecha)
                                    val anterior =
                                        if (currentIndex_turismo - 1 < 0) lista_tiendas_cecanas_turismo.lastIndex else currentIndex_turismo - 1
                                    val tienda = lista_tiendas_cecanas_turismo[anterior]

                                    lister_marker = dataclass_map(
                                        img = tienda.logo_tienda,
                                        nombre = tienda.nombre_tienda,
                                        tag = tienda.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = tienda.latitud,
                                        longitud = tienda.longitud,
                                        id = tienda.id_tienda,
                                        categoria = "",
                                        direccion = tienda.direccion,
                                        referencia = tienda.referencia,
                                        contacto_tienda = tienda.contacto_tienda,
                                        metodos_pago_tienda = tienda.metodos_pago_tienda,
                                        horario_box = tienda.horario_box,
                                        localidad = tienda.localidad_tienda
                                    )

                                    seleccionadoId = tienda.id_tienda
                                    currentIndex_turismo = anterior // 🔹 Actualiza el índice
                                    val location = Point.fromLngLat(tienda.longitud, tienda.latitud)

                                    coroutineScope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder()
                                                .center(location)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
//                                    scope.launch {
//                                        cameraPositionState.animate(
//                                            CameraUpdateFactory.newLatLngZoom(
//                                                LatLng(tienda.latitud, tienda.longitud), 16f
//                                            ),
//                                            1000
//                                        )
//                                    }
                                }
                            }
                        }

                        "tiendas" -> {

                            if (lista_filtrada_tiendas.isNotEmpty()) {
                                if (currentIndex != -1) {
                                    // Mover al elemento anterior (hacia la derecha)
                                    val anterior =
                                        if (currentIndex - 1 < 0) lista_filtrada_tiendas.lastIndex else currentIndex - 1
                                    val tienda = lista_filtrada_tiendas[anterior]

                                    lister_marker = dataclass_map(
                                        img = tienda.logo_tienda,
                                        nombre = tienda.nombre_tienda,
                                        tag = tienda.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = tienda.latitud,
                                        longitud = tienda.longitud,
                                        id = tienda.id_tienda,
                                        categoria = "",
                                        direccion = tienda.direccion,
                                        referencia = tienda.referencia,
                                        contacto_tienda = tienda.contacto_tienda,
                                        metodos_pago_tienda = tienda.metodos_pago_tienda,
                                        horario_box = tienda.horario_tienda_box,
                                        localidad = tienda.localidad_tienda
                                    )

                                    seleccionadoId = tienda.id_tienda
                                    currentIndex = anterior // 🔹 Actualiza el índice
                                    val location = Point.fromLngLat(tienda.longitud, tienda.latitud)

                                    coroutineScope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder()
                                                .center(location)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
//                                    scope.launch {
//                                        cameraPositionState.animate(
//                                            CameraUpdateFactory.newLatLngZoom(
//                                                LatLng(tienda.latitud, tienda.longitud), 16f
//                                            ),
//                                            1000
//                                        )
//                                    }
                                }
                            }
                        }

                        else -> {}
                    }

                },
                move_izquierda = {
                    when (tipo) {
                        "turismo" -> {
                            if (lista_tiendas_cecanas_turismo.isNotEmpty()) {
                                if (currentIndex_turismo != -1) {
                                    // Mover al siguiente elemento (hacia la izquierda)
                                    val siguiente =
                                        (currentIndex_turismo + 1) % lista_tiendas_cecanas_turismo.size
                                    val tienda = lista_tiendas_cecanas_turismo[siguiente]
                                    lister_marker = dataclass_map(
                                        img = tienda.logo_tienda,
                                        nombre = tienda.nombre_tienda,
                                        tag = tienda.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = tienda.latitud,
                                        longitud = tienda.longitud,
                                        id = tienda.id_tienda,
                                        categoria = "",
                                        direccion = tienda.direccion,
                                        referencia = tienda.referencia,
                                        contacto_tienda = tienda.contacto_tienda,
                                        metodos_pago_tienda = tienda.metodos_pago_tienda,
                                        horario_box = tienda.horario_box,
                                        localidad = tienda.localidad_tienda
                                    )

                                    seleccionadoId = tienda.id_tienda
                                    currentIndex_turismo = siguiente // 🔹 Actualiza el índice
                                    val location = Point.fromLngLat(tienda.longitud, tienda.latitud)

                                    coroutineScope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder()
                                                .center(location)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
//                                    scope.launch {
//                                        cameraPositionState.animate(
//                                            CameraUpdateFactory.newLatLngZoom(
//                                                LatLng(tienda.latitud, tienda.longitud), 16f
//                                            ),
//                                            1000
//                                        )
//                                    }
                                }
                            }
                        }

                        "tiendas" -> {
                            if (lista_filtrada_tiendas.isNotEmpty()) {
                                if (currentIndex != -1) {
                                    // Mover al siguiente elemento (hacia la izquierda)
                                    val siguiente = (currentIndex + 1) % lista_filtrada_tiendas.size
                                    val tienda = lista_filtrada_tiendas[siguiente]

                                    lister_marker = dataclass_map(
                                        img = tienda.logo_tienda,
                                        nombre = tienda.nombre_tienda,
                                        tag = tienda.lista_subcategoiras,
                                        my_latitud = lat_user,
                                        my_longitud = log_user,
                                        latitud = tienda.latitud,
                                        longitud = tienda.longitud,
                                        id = tienda.id_tienda,
                                        categoria = "",
                                        direccion = tienda.direccion,
                                        referencia = tienda.referencia,
                                        contacto_tienda = tienda.contacto_tienda,
                                        metodos_pago_tienda = tienda.metodos_pago_tienda,
                                        horario_box = tienda.horario_tienda_box,
                                        localidad = tienda.localidad_tienda
                                    )

                                    seleccionadoId = tienda.id_tienda
                                    currentIndex = siguiente // 🔹 Actualiza el índice
                                    val location = Point.fromLngLat(tienda.longitud, tienda.latitud)

                                    coroutineScope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder()
                                                .center(location)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
//                                    scope.launch {
//                                        cameraPositionState.animate(
//                                            CameraUpdateFactory.newLatLngZoom(
//                                                LatLng(tienda.latitud, tienda.longitud), 16f
//                                            ),
//                                            1000
//                                        )
//                                    }
                                }
                            }
                        }

                        else -> {}
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


        if (show_bottom_sheet_datos_tienda_lugares) {
            bottom_sheet_tiendas_filtradas(
                verificar_intener,
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
                        id_user,
                        "tienda", id_tienda, localidad,
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
                "Para una mejor experiencia y " +
                        "poder mostrar tu ubicación actual en el mapa, por favor habilita la función de ubicación en tu dispositivo. Esto te permitirá ubicarte de manera más rápida y conocer la proximidad a tu destino.",
                { validacion_mostrar_dialog_ubi_off = false },
                {
                    validacion_mostrar_dialog_ubi_off = false
                    verificarGPS(context, launcher)
                    show_dialog_datos_lugares = false
                })
        }
        if (call_dialog_permise) {
            permisos_llamadas(aceptar_permisos = {
                requestCallPermission(context = context, phoneNumber = numero_llamada)
            }, ondimis = {
                call_dialog_permise = false
            })
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }


}

// 🔹 Descargar bitmap desde URL
suspend fun loadBitmapFromUrl(url: String, context: Context): Bitmap = withContext(Dispatchers.IO) {
    // URL de respaldo
    val fallbackUrl =
        "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0"

    // Validar URL: si está vacía o no tiene protocolo, usar fallback
    val validUrl = if (url.isBlank() || !url.startsWith("http")) fallbackUrl else url

    try {
        val stream = URL(validUrl).openStream()
        BitmapFactory.decodeStream(stream)
    } catch (e: Exception) {
        e.printStackTrace()
        // Si falla por alguna razón, cargar un recurso local de la app
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.logo_geinz_500x500 // tu placeholder local en drawable
        )
    }
}

// 🔹 Redimensionar y recortar bitmap como círculo
fun Bitmap.toCircularBitmap(sizePx: Int): Bitmap {
    val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = Rect(0, 0, sizePx, sizePx)
    val rectF = RectF(rect)

    val shader = BitmapShader(
        Bitmap.createScaledBitmap(this, sizePx, sizePx, true),
        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
    )
    paint.shader = shader
    canvas.drawOval(rectF, paint)
    return output
}

fun createCirclePolygon(center: Point, radiusMeters: Double, points: Int = 64): Polygon {
    val coordinates = mutableListOf<Point>()
    val earthRadius = 6371000.0
    val lat = Math.toRadians(center.latitude())
    val lon = Math.toRadians(center.longitude())

    for (i in 0..points) {
        val angle = 2 * Math.PI * i / points
        val dx = radiusMeters * Math.cos(angle)
        val dy = radiusMeters * Math.sin(angle)

        val deltaLat = dy / earthRadius
        val deltaLon = dx / (earthRadius * Math.cos(lat))

        val point = Point.fromLngLat(
            Math.toDegrees(lon + deltaLon),
            Math.toDegrees(lat + deltaLat)
        )
        coordinates.add(point)
    }

    return Polygon.fromLngLats(listOf(coordinates))
}


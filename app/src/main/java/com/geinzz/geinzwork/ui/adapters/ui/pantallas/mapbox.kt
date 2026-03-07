package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.graphics.*
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import androidx.compose.animation.AnimatedVisibility
import com.mapbox.maps.plugin.annotation.annotations
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.BuildConfig.MAPBOX_ACCESS_TOKEN
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.iconos_creaciones_rutas
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
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
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularDistanciaMetros
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatearDistancia
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
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.addLayerAt
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.CirclePitchAlignment
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
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
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.scalebar.ScaleBarPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.text.get
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.ui.node.Ref
import androidx.core.content.ContextCompat

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
    val listaIconosRutas = listOf(
        iconos_creaciones_rutas("walking", Icons.Default.DirectionsWalk),
        iconos_creaciones_rutas("cycling", Icons.Default.DirectionsBike),
        iconos_creaciones_rutas("driving", Icons.Default.DirectionsCar)
    )
    var yaSeAnuncioLlegada by remember { mutableStateOf(false) }
    val rutaRef = remember { mutableStateOf<List<Point>>(emptyList()) }
    var rutaCompleta by rutaRef
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    val pointAnnotationManager = remember { mutableStateOf<PointAnnotationManager?>(null) }
    var estilo_mapa_mapbox by remember { mutableStateOf(false) }
    var ulr_esilo by remember { mutableStateOf("") }
    LaunchedEffect(estilo_mapa_mapbox) {
        if (estilo_mapa_mapbox) {
            ulr_esilo = "mapbox://styles/benjaminlopez/cmm99ygby002w01s50jvt9r1h"
        } else {
            ulr_esilo = "mapbox://styles/benjaminlopez/cmm9c0hlt003901s54utw9p30"
        }
    }
    var showRecenterButton by remember { mutableStateOf(false) }


    val radio by viewmodel_lugares_turisticos.estado_radio_filtrada.collectAsState()

    val coordenadas by viewmode_segurirdad_Salud.coordenadasSeleccionadas.observeAsState()
    var latitud_luga_seg by remember { mutableStateOf(0.0) }
    var long_luga_seg by remember { mutableStateOf(0.0) }
    val lista_filtrada_tiendas by viewModel_filtrado_tiendas.listaTiendasGuardadas.observeAsState(
        emptyList()
    )
    val lista_tiendas_cecanas_turismo by viewmodel_lugares_turisticos.listaTiendasGuardadas.collectAsState()
    val datos_cloud_TTs by viewmodelMapa.datosCloudTts.collectAsState()
    LaunchedEffect(datos_cloud_TTs) {
        Log.d("datos_cloud_TTs", "$datos_cloud_TTs")
        if (datos_cloud_TTs.isNotEmpty()) {
            viewmodelMapa.reproducirMP3(context, datos_cloud_TTs)
            viewmodelMapa.limpiarAudio()
        }
    }

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
    var latitud_lugar by remember { mutableStateOf(0.0) }
    var longitud_lugar by remember { mutableStateOf(0.0) }
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
    var modo3D by remember { mutableStateOf(false) }
    var tipo_crearcion_ruta_creado by remember { mutableStateOf("") }
    val estaRecalculando = remember { mutableStateOf(false) }
    val ultimoRecalculo = remember { longArrayOf(0L) }
    fun animarTamano(
        annotation: PointAnnotation, desde: Double, hasta: Double
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
            animarTamano(anterior, anterior.iconSize ?: 1.0, 0.9)
            anterior.iconOpacity = 0.6
            pointAnnotationManager.value?.update(anterior)
        }

        animarTamano(nuevo, nuevo.iconSize ?: 0.9, 1.0)
        nuevo.iconOpacity = 1.0
        pointAnnotationManager.value?.update(nuevo)

        selectedAnnotation = nuevo
    }


    fun cambiarModoMapa() {

        val pitchValue = if (modo3D) 45.0 else 0.0

        mapboxMapInstance?.easeTo(
            CameraOptions.Builder().pitch(pitchValue).build(),
            MapAnimationOptions.Builder().duration(600).build()
        )
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

    // Cada vez que cambia tu ubicación

    LaunchedEffect(lister_marker.id) {

        if (lister_marker.id.isBlank()) return@LaunchedEffect

        Log.d("id_tienda_cambiada", "${lister_marker.id} ${lister_marker.horario_box}")

        viewModel_filtrado_tiendas.calcularHorarioParaTienda(
            lister_marker.id, lister_marker.horario_box
        )
    }
    LaunchedEffect(id_lugar_tienda_select) {
        viewModel_filtrado_tiendas.obtener_campos_tiendas_por_id(
            localidad_tienda_lugar_Select, id_lugar_tienda_select
        )
    }
    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }



    viewModel_filtrado_tiendas.repo_filtrado.escucharHorarioDeTiendaUnica(
        idTiendaBuscada = lister_marker.id, localidad = "barranca"
    )


    val snackbarHostState = remember { SnackbarHostState() }
    val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }

    var primeraCargaCamara by remember { mutableStateOf(true) }
// 1️⃣ Agrega esta variable junto a tus otros remember
    var ultimoBearing by remember { mutableStateOf(0.0) }
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
    var styleReady by remember { mutableStateOf(false) }
    var mostar_ocultar_carta by remember { mutableStateOf(true) }
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
    LaunchedEffect(radio, estado) {
        show_dialog_datos_lugares = false
        seleccionadoId = null
        viewmodelMapa.limpiarAudio()
        yaSeAnuncioLlegada = false


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
                        bitmap = loadBitmapFromUrl(imageUrl, context).toCircularBitmap(sizePx = 120)
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

                val option = PointAnnotationOptions().withPoint(
                    Point.fromLngLat(
                        tienda.longitud, tienda.latitud
                    )
                ).withIconImage(imageId).withIconSize(0.9).withIconOpacity(0.6)

                val annotation = manager.create(option)

                annotationsById[tienda.id_tienda] = annotation
            }

        }
    }
    LaunchedEffect(seleccionadoId) {

        Log.d("MAP_DEBUG", "📌 seleccionadoId actual: '$seleccionadoId'")

        val id = seleccionadoId

        if (id.isNullOrBlank()) {

            Log.d("MAP_DEBUG", "❌ seleccionadoId está null o vacío → borrando ruta")

            viewmodelMapa.limpiarAudio()
            yaSeAnuncioLlegada = false

            mapboxMapInstance?.getStyle { style ->

                val source = style.getSourceAs<GeoJsonSource>("route_source")

                if (source != null) {
                    source.featureCollection(
                        FeatureCollection.fromFeatures(emptyArray())
                    )
                    Log.d("MAP_DEBUG", "✅ Ruta eliminada")
                } else {
                    Log.e("MAP_DEBUG", "⚠️ route_source no existe")
                }
            }

            return@LaunchedEffect
        }

        Log.d("MAP_DEBUG", "🎯 seleccionadoId válido → seleccionando marker: $id")
        yaSeAnuncioLlegada = false
        viewmodelMapa.limpiarAudio()
        seleccionarMarkerPorId(id)
    }
    LaunchedEffect(
        img_lugare_dircto, lat_lugar_directo, lng_lugar_directo, managerLauncher.value
    ) {
        if (img_lugare_dircto == null || lat_lugar_directo == null || lng_lugar_directo == null) return@LaunchedEffect

        val launcherManager = managerLauncher.value ?: return@LaunchedEffect
        val mapboxMap = mapboxMapInstance ?: return@LaunchedEffect

        val punto = Point.fromLngLat(
            lng_lugar_directo, lat_lugar_directo
        )
        latitud_lugar = lat_lugar_directo
        longitud_lugar = lng_lugar_directo
        launcherManager.deleteAll()

        val imageId = "launcher_icon"

        // ✅ AQUI SÍ puedes llamar suspend
        val bitmap = loadBitmapFromUrl(img_lugare_dircto, context).toCircularBitmap(130)

        // 🔥 Ahora solo usas style
        mapboxMap.getStyle { style ->

            style.removeStyleImage(imageId)
            style.addImage(imageId, bitmap)

            launcherManager.create(
                PointAnnotationOptions().withPoint(punto).withIconImage(imageId)
                    .withIconAnchor(IconAnchor.BOTTOM).withIconSize(1.0)
            )
        }
    }
    LaunchedEffect(
        lat_lugar_directo, lng_lugar_directo, mapboxMapInstance, styleReady
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
                CameraOptions.Builder().center(punto).zoom(16.0).build()
            )

            primeraCargaCamara = false
        } else {

            mapboxMap.flyTo(
                CameraOptions.Builder().center(punto).zoom(16.0).build(),
                MapAnimationOptions.Builder().duration(1500).build()
            )
        }
    }


    val fabColor by animateColorAsState(
        targetValue = if (seguirUbicacion.value) MaterialTheme.colorScheme.primary else Color(
            0xFF9C7BFF
        ), animationSpec = tween(
            durationMillis = 300 // 0.3 segundos, suave pero rápido
        )
    )
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val mapView = mapViewState.value
    var ultimaLat: Double? = null
    var ultimaLng: Double? = null
    var yaSeAnuncio50metros by remember { mutableStateOf(false) }
    LaunchedEffect(seguirUbicacion.value) {

        if (seguirUbicacion.value) {

            ultimaLat = lat_user

            ultimaLng = log_user

            if (ultimaLat != null && ultimaLng != null) {

                val puntoUsuario = Point.fromLngLat(
                    ultimaLng, ultimaLat
                )
                val distacia_camaara_creada = if (rutaCompleta.isNotEmpty()) {
                    18.0
                } else {
                    16.0
                }
                mapboxMapInstance?.flyTo(
                    cameraOptions = CameraOptions.Builder().center(puntoUsuario)
                        .zoom(distacia_camaara_creada).build(),
                    animationOptions = MapAnimationOptions.Builder().duration(1000).build()
                )
            }
        }

        val mensaje = if (seguirUbicacion.value) {
            "Seguimiento automático activado"
        } else {
            "Seguimiento automático desactivado"
        }

        scope.launch {
            snackbarHostState.showSnackbar(
                message = mensaje, duration = SnackbarDuration.Short
            )
        }
    }

    DisposableEffect(mapView) {

        if (mapView == null) return@DisposableEffect onDispose { }

        val locationPlugin = mapView.location
        val gesturesPlugin = mapView.gestures
        val mapboxMap = mapView.getMapboxMap()

        mapboxMapInstance = mapboxMap

        val locationListener = OnIndicatorPositionChangedListener { point ->
            val lat = point.latitude()
            val lng = point.longitude()

            if (lat == 0.0 && lng == 0.0) return@OnIndicatorPositionChangedListener

            val distancia = FloatArray(1)
            Location.distanceBetween(lat_user, log_user, lat, lng, distancia)
            if (distancia[0] < 1f && lat_user != 0.0) return@OnIndicatorPositionChangedListener

            lat_user = lat
            log_user = lng

            if (!seguirUbicacion.value) return@OnIndicatorPositionChangedListener

            val puntoUsuario = Point.fromLngLat(lng, lat)
            val rutaActual = rutaRef.value

            if (rutaActual.isNotEmpty()) {
                val siguiente =
                    rutaActual.firstOrNull() ?: return@OnIndicatorPositionChangedListener
                val bearingNuevo = calculateBearing(Point.fromLngLat(lng, lat), siguiente)
                val bearingCorregido = (bearingNuevo + 180.0) % 360.0

                // ✅ Umbral más alto = menos micro-rotaciones
                val diferenciaBearing = Math.abs(bearingCorregido - ultimoBearing)
                val bearingFinal = if (diferenciaBearing > 15.0 && diferenciaBearing < 345.0) {
                    ultimoBearing = bearingCorregido
                    bearingCorregido
                } else {
                    ultimoBearing
                }

                mapboxMap.easeTo(
                    CameraOptions.Builder()
                        .center(puntoUsuario)
                        .zoom(18.0)
                        .bearing(bearingFinal)
//                        .pitch(45.0)
                        .build(),
                    MapAnimationOptions.Builder()
                        .duration(300) // ✅ Reducido: termina antes del próximo update GPS
                        .build()
                )
            } else {
                mapboxMap.easeTo(
                    CameraOptions.Builder()
                        .center(puntoUsuario)
                        .zoom(16.0)
                        .bearing(0.0)
                        .pitch(0.0)
                        .build(),
                    MapAnimationOptions.Builder()
                        .duration(300) // ✅ Reducido también
                        .build()
                )
            }
        }
        val moveListener = object : OnMoveListener {

            override fun onMoveBegin(detector: MoveGestureDetector) {
                if (detector.pointersCount > 0) {
                    seguirUbicacion.value = false
                }

                val lat = lat_lugar_directo ?: return  // ✅ sale silenciosamente
                val lng = lng_lugar_directo ?: return

                val mapboxMap = mapboxMapInstance ?: return
                val cameraState = mapboxMap.cameraState

                val bounds = mapboxMap.coordinateBoundsForCamera(
                    CameraOptions.Builder().center(cameraState.center).zoom(cameraState.zoom)
                        .bearing(cameraState.bearing).pitch(cameraState.pitch).build()
                )

                val markerPoint = Point.fromLngLat(lng, lat)
                val markerVisible =
                    markerPoint.latitude() in bounds.southwest.latitude()..bounds.northeast.latitude() && markerPoint.longitude() in bounds.southwest.longitude()..bounds.northeast.longitude()

                showRecenterButton = !markerVisible
            }

            override fun onMove(detector: MoveGestureDetector): Boolean = false
            override fun onMoveEnd(detector: MoveGestureDetector) {
                val mapboxMap = mapboxMapInstance ?: return
                val cameraState = mapboxMap.cameraState

                val bounds = mapboxMap.coordinateBoundsForCamera(
                    CameraOptions.Builder().center(cameraState.center).zoom(cameraState.zoom)
                        .bearing(cameraState.bearing).pitch(cameraState.pitch).build()
                )

                val markerPoint = Point.fromLngLat(lng_lugar_directo!!, lat_lugar_directo!!)
                val markerVisible =
                    markerPoint.latitude() in bounds.southwest.latitude()..bounds.northeast.latitude() && markerPoint.longitude() in bounds.southwest.longitude()..bounds.northeast.longitude()

                showRecenterButton = !markerVisible
            }

        }

        locationPlugin.addOnIndicatorPositionChangedListener(locationListener)

        gesturesPlugin.addOnMoveListener(moveListener)

        onDispose {
            locationPlugin.removeOnIndicatorPositionChangedListener(locationListener)
            gesturesPlugin.removeOnMoveListener(moveListener)
        }
    }

    LaunchedEffect(tipo_crearcion_ruta_creado, seleccionadoId) {

        if (tipo_crearcion_ruta_creado.isBlank()) return@LaunchedEffect
        if (seleccionadoId.isNullOrBlank()) return@LaunchedEffect

        val ruta = obtenerRuta(
            lat_user,
            log_user,
            lister_marker.latitud,
            lister_marker.longitud,
            tipo_crearcion_ruta_creado
        )

        ruta?.let { puntos ->

            rutaCompleta = puntos

            mapboxMapInstance?.getStyle { style ->
                style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                    FeatureCollection.fromFeature(
                        Feature.fromGeometry(
                            LineString.fromLngLats(puntos)
                        )
                    )
                )
            }

            // 🔹 Ajustar cámara para que toda la ruta se vea
            if (puntos.isNotEmpty()) {
                modo3D = true

                // 🔑 Calcular dirección desde tu posición hacia el primer punto de la ruta
                val tuPosicion = Point.fromLngLat(log_user, lat_user)
                val primerPunto = puntos.first()

                // Bearing = ángulo hacia donde apunta la ruta
                val bearing = calculateBearing(tuPosicion, primerPunto)
                val bearingCorregido = (bearing + 180.0) % 360.0  // ✅ mismo fix


                // 🎯 Cámara detrás tuyo, mirando hacia adelante — igual que Google Maps navegando
                mapboxMapInstance?.flyTo(
                    cameraOptions = CameraOptions.Builder()
                        .center(tuPosicion)        // centrada en TI, no en la ruta
                        .zoom(18.0)                // zoom cercano como navegación
                        .bearing(bearingCorregido)          // rotada hacia donde vas
                        .pitch(45.0)               // inclinación fuerte tipo GPS
                        .build(),
                    animationOptions = MapAnimationOptions.Builder().duration(2000).build()
                )
            }
        }
    }

    LaunchedEffect(lat_user, log_user) {
        if (rutaCompleta.isEmpty() || seleccionadoId.isNullOrBlank()) return@LaunchedEffect

        // 🔴 BLOQUEO — si ya está recalculando, no hacer nada
        if (estaRecalculando.value) {
            Log.d("DESVIO_DEBUG", "⏳ Ya recalculando, ignorando update GPS")
            return@LaunchedEffect
        }

        val distanciaMetros = calcularDistanciaMetros(
            lat_user, log_user,
            lister_marker.latitud, lister_marker.longitud
        )

        if (distanciaMetros <= 10f) {
            mostar_ocultar_carta = true
            show_dialog_datos_lugares = true

            if (!yaSeAnuncioLlegada) {
                yaSeAnuncioLlegada = true
                viewmodelMapa.crear_texto__para_tts("Benjamin llegaste a ${lister_marker.nombre} mira a tu alrededor")
                vibrarTelefono(context)
                tipo_crearcion_ruta_creado = ""
                rutaCompleta = emptyList()
                mapboxMapInstance?.getStyle { style ->
                    style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                        FeatureCollection.fromFeatures(emptyArray())
                    )
                }
            }
            return@LaunchedEffect
        }

        if (distanciaMetros <= 50f) {
            mostar_ocultar_carta = true
            show_dialog_datos_lugares = true

            if (!yaSeAnuncio50metros) {
                yaSeAnuncio50metros = true
                viewmodelMapa.crear_texto__para_tts("Benjamin estas por llegar a ${lister_marker.nombre} a menos de 50 metros")
                vibrarTelefono(context)
            }
            return@LaunchedEffect
        }
        val miUbicacion = Point.fromLngLat(log_user, lat_user)
        var mejorIndice = 0
        var mejorDistancia = Float.MAX_VALUE

        for (i in 0 until rutaCompleta.size - 1) {
            val snap =
                obtenerPuntoMasCercanoEnSegmento(miUbicacion, rutaCompleta[i], rutaCompleta[i + 1])
            val dist = FloatArray(1)
            Location.distanceBetween(lat_user, log_user, snap.latitude(), snap.longitude(), dist)
            if (dist[0] < mejorDistancia) {
                mejorDistancia = dist[0]
                mejorIndice = i
            }
        }

        Log.d("DESVIO_DEBUG", "mejorDistancia = $mejorDistancia metros")

        val ahora = System.currentTimeMillis()

        if (mejorDistancia > 50f && tipo_crearcion_ruta_creado.isNotBlank()) {
            if (ahora - ultimoRecalculo[0] > 8_000L) {

                estaRecalculando.value = true
                ultimoRecalculo[0] = ahora

                Log.d("DESVIO_DEBUG", "🔄 Recalculando ruta...")

                // ✅ Usar recalculoScope en vez de await directo
                scope.launch {
                    try {
                        val nuevaRuta = obtenerRuta(
                            lat_user, log_user,
                            lister_marker.latitud, lister_marker.longitud,
                            tipo_crearcion_ruta_creado
                        )

                        if (nuevaRuta != null && nuevaRuta.isNotEmpty()) {
                            Log.d(
                                "DESVIO_DEBUG",
                                "✅ Nueva ruta con ${nuevaRuta.size} puntos — dibujando"
                            )
                            rutaCompleta = nuevaRuta
                            mapboxMapInstance?.getStyle { style ->
                                style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                                    FeatureCollection.fromFeature(
                                        Feature.fromGeometry(LineString.fromLngLats(nuevaRuta))
                                    )
                                )
                            }
                        } else {
                            Log.e("DESVIO_DEBUG", "❌ obtenerRuta() devolvió null")
                        }
                    } catch (e: Exception) {
                        Log.e("DESVIO_DEBUG", "💥 EXCEPCIÓN: ${e.message}", e)
                    } finally {
                        estaRecalculando.value = false
                        Log.d("DESVIO_DEBUG", "🔓 Desbloqueado")
                    }
                }
            }
            return@LaunchedEffect
        }
        // Sin desvío — recortar ruta normalmente
        if (mejorIndice >= rutaCompleta.size - 1) return@LaunchedEffect

        val snap = obtenerPuntoMasCercanoEnSegmento(
            miUbicacion, rutaCompleta[mejorIndice], rutaCompleta[mejorIndice + 1]
        )

        val listaVisual = mutableListOf(snap)
        listaVisual.addAll(rutaCompleta.drop(mejorIndice + 1))

        mapboxMapInstance?.getStyle { style ->
            style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(LineString.fromLngLats(listaVisual))
                )
            )
        }

        if (mejorIndice > 0) {
            rutaCompleta = rutaCompleta.drop(mejorIndice)
        }

        if (!seguirUbicacion.value) {
            val location = Point.fromLngLat(log_user, lat_user)
            mapboxMapInstance?.easeTo(
                CameraOptions.Builder().center(location).zoom(16.0).build(),
                MapAnimationOptions.Builder().duration(500).build()
            )
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {

        MapboxMap(modifier = Modifier.fillMaxSize(), scaleBar = { }) {

            MapStyle(ulr_esilo)

            MapEffect(Unit) { mapView ->
                mapView.getMapboxMap().getStyle { style ->

                    style.styleLayers.forEach {
                        Log.d("LAYERS_DEBUG", "Layer id = ${it.id}")
                    }
                }
                val mapboxMap = mapView.getMapboxMap()
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
                    managerLauncher.value = mapView.annotations.createPointAnnotationManager()
                }

                if (pointAnnotationManager.value == null) {

                    pointAnnotationManager.value =
                        mapView.annotations.createPointAnnotationManager()

                    pointAnnotationManager.value?.addClickListener { annotation ->

                        val tienda =
                            lista_tiendas_cecanas_turismo.find { "marker-${it.id_tienda}" == annotation.iconImage }

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
                            })

                        style.addLayerBelow(
                            fillLayer("launcher_circle_layer", "launcher_circle_source") {
//                                fillColor("#2196F3")
                                fillOpacity(0.2)
                            }, "road-label"
                        )
                        styleReady = true // ✅ aquí sí
                    }
                    if (style.getSource("route_source") == null) {

                        style.addSource(
                            geoJsonSource("route_source") {
                                featureCollection(
                                    FeatureCollection.fromFeatures(emptyArray())
                                )
                            })
                        if (style.styleLayerExists("route_layer")) {
                            style.removeStyleLayer("route_layer")
                        }
                        style.addLayerBelow(
                            lineLayer("route_layer", "route_source") {
                                lineColor("#4285F4")
                                lineWidth(12.0)
                                lineCap(LineCap.ROUND)
                                lineJoin(LineJoin.ROUND)
                            }, "road-label"
                        )
                    }

// ✅ REDIBUJAR LA RUTA SI EXISTE
                    if (rutaCompleta.isNotEmpty()) {
                        style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(
                                    LineString.fromLngLats(rutaCompleta)
                                )
                            )
                        )
                        Log.d("MAP_DEBUG", "🔵 Ruta redibujada tras cambio de estilo")
                    }
                }


            }

            MapEffect(ulr_esilo) { mapView ->

                Log.d("MAP_DEBUG", "🔥 MapEffect(ulr_esilo) EJECUTADO")

                val mapboxMap = mapView.getMapboxMap()

                mapboxMap.getStyle { style ->

                    Log.d("MAP_DEBUG", "🎨 Style cargado")

                    if (style.getSource("launcher_circle_source") == null) {
                        style.addSource(
                            geoJsonSource("launcher_circle_source") {
                                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
                            })
                        style.addLayerBelow(
                            fillLayer("launcher_circle_layer", "launcher_circle_source") {
                                fillOpacity(0.2)
                            }, "road-label"
                        )
                    }

                    // 🔵 REDIBUJAR CÍRCULO
                    if (lat_lugar_directo != null && lng_lugar_directo != null) {
                        val punto = Point.fromLngLat(lng_lugar_directo, lat_lugar_directo)
                        val radioEnMetros = radio * 100.0
                        style.getSourceAs<GeoJsonSource>("launcher_circle_source")
                            ?.featureCollection(
                                FeatureCollection.fromFeature(
                                    Feature.fromGeometry(createCirclePolygon(punto, radioEnMetros))
                                )
                            )
                        Log.d("MAP_DEBUG", "🔵 Círculo redibujado tras cambio de estilo")
                    }

                    if (style.getSource("route_source") == null) {
                        style.addSource(
                            geoJsonSource("route_source") {
                                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
                            })
                    }
                    if (style.styleLayerExists("route_layer")) {
                        style.removeStyleLayer("route_layer")
                    }
                    style.addLayerBelow(
                        lineLayer("route_layer", "route_source") {
                            lineColor("#4285F4")
                            lineWidth(12.0)
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                        }, "road-label"
                    )

                    // ✅ REDIBUJAR RUTA (lo que faltaba)
                    if (rutaCompleta.isNotEmpty()) {
                        style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(LineString.fromLngLats(rutaCompleta))
                            )
                        )
                        Log.d("MAP_DEBUG", "🟢 Ruta redibujada tras cambio de estilo día/noche")
                    }
                }
            }

        }


        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.animateContentSize()
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                FloatingActionButton(
                    modifier = Modifier.padding(10.dp),
                    containerColor = fabColor,
                    contentColor = Color.White,
                    onClick = {
                        if (verificarUbiActiva(context)) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val userPoint = Point.fromLngLat(it.longitude, it.latitude)
                                    scope.launch {
                                        mapboxMapInstance?.easeTo(
                                            CameraOptions.Builder().center(userPoint).zoom(16.0)
                                                .build(),
                                            MapAnimationOptions.Builder().duration(800).build()
                                        )
                                        seguirUbicacion.value = true
                                        animatingMap.value = false
                                    }
                                }
                            }
                        } else {
                            validacion_mostrar_dialog_ubi_off = true
                        }
                    }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }

                // 🔹 Botones de rutas
                if (show_dialog_datos_lugares) {
                    desing_creacion_ruta(
                        context,
                        lista = listaIconosRutas,
                        img_tienda = lister_marker.img,
                        seleccionado = { select ->

                            Log.d("tipo_creacion_ruta", "creacion de ruta de $select")
                            tipo_crearcion_ruta_creado = select
                            seguirUbicacion.value = true


                        },
                        cancelacion_ruta = {
                            mostar_ocultar_carta = true
                            show_dialog_datos_lugares = true
                            tipo_crearcion_ruta_creado = ""
                            rutaCompleta = emptyList()
                            mapboxMapInstance?.getStyle { style ->
                                style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                                    FeatureCollection.fromFeatures(emptyArray())
                                )
                            }
                        },
                        ocultar_dialog_ = {
                            mostar_ocultar_carta = false
                        }, mostrar_campo = {
                            mostar_ocultar_carta = !mostar_ocultar_carta
                            show_dialog_datos_lugares = true
                        }, {
                            validacion_mostrar_dialog_ubi_off = true
                        })
                }


            }

            // 🔹 Columna derecha (siempre fija)
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                estilo_visual_btns(null, null, if (modo3D) "2D" else "3D", "text") {
                    modo3D = !modo3D
                    cambiarModoMapa()
                }

                estilo_visual_btns(
                    null,
                    if (estilo_mapa_mapbox) Icons.Default.NightlightRound else Icons.Default.WbSunny,
                    null,
                    "icono"
                ) {
                    estilo_mapa_mapbox = !estilo_mapa_mapbox
                }

                if (img_lugare_dircto != null && showRecenterButton) {

                    estilo_visual_btns(
                        img_lugare_dircto, null, null, "img"
                    ) {

                        if (mapView == null) return@estilo_visual_btns
                        val mapboxMap = mapView.getMapboxMap()

                        if (longitud_lugar != 0.0 && latitud_lugar != 0.0) {
                            val puntoUsuario = Point.fromLngLat(longitud_lugar, latitud_lugar)

                            mapboxMap.flyTo(
                                CameraOptions.Builder().center(puntoUsuario).zoom(16.0).build(),
                                MapAnimationOptions.Builder().duration(2000).build()
                            )
                        }

                        showRecenterButton = false
                    }
                }
            }
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

                                    scope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder().center(location_turismo)
                                                .zoom(16.0).build()
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

                                    scope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder().center(location_tienda)
                                                .zoom(16.0).build()
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
                },
                datos_selecionado_retornar = { datos ->
                    lister_marker = datos
                    show_dialog_datos_lugares = true
                })
        }

        AnimatedVisibility(
            visible = show_dialog_datos_lugares && mostar_ocultar_carta,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {

            dialogo_lugar_tienda(
                horario_box1 = horarios[lister_marker.id] ?: HorarioDia_box(),
                viewmodelMapa = viewmodelMapa,
                lat_user = lat_user,
                log_user = log_user,
                time = tick,
                dataclass_map = lister_marker,
                cerra_dialog = {
                    selectedAnnotation?.let { anterior ->
                        animarTamano(anterior, anterior.iconSize ?: 1.0, 0.9)
                        anterior.iconOpacity = 0.6
                        pointAnnotationManager.value?.update(anterior)
                    }
                    selectedAnnotation = null
                    show_dialog_datos_lugares = false
                    mostar_bottom_sheet = true
                    tipo_crearcion_ruta_creado = ""
                    rutaCompleta = emptyList()
                    mapboxMapInstance?.getStyle { style ->
                        style.getSourceAs<GeoJsonSource>("route_source")?.featureCollection(
                            FeatureCollection.fromFeatures(emptyArray())
                        )
                    }
                },
                limpiar = {
                    selectedAnnotation?.let { anterior ->
                        animarTamano(anterior, anterior.iconSize ?: 1.0, 0.9)
                        anterior.iconOpacity = 0.6
                        pointAnnotationManager.value?.update(anterior)
                    }
                    selectedAnnotation = null
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
                            my_latitud = lat, my_longitud = log
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

                        scope.launch {
                            mapboxMapInstance?.flyTo(
                                CameraOptions.Builder().center(location).zoom(16.0).build(),

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
                                localidad_tienda = localidad,
                                id_user
                            )
                        }

                        "facebook" -> {
                            Log.d("    datos.valor", "${datos.valor}")
                            openFacebook(
                                "Tienda",
                                context = context,
                                pageUrl = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad,
                                id_user
                            )
                        }

                        "instagram" -> {
                            Log.d("    datos.valor", "${datos.valor}")
                            openInstagram(
                                "Tienda",
                                context = context,
                                url = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad,
                                id_user
                            )
                        }

                        "Web" -> {
                            openWebLink(
                                context = context,
                                url = datos.valor,
                                id_tienda = id,
                                localidad_tienda = localidad,
                                id_user
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

                                    scope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder().center(location).zoom(16.0)
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
                                    currentIndex = anterior
                                    val location = Point.fromLngLat(tienda.longitud, tienda.latitud)

                                    scope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder().center(location).zoom(16.0)
                                                .build()
                                        )
                                    }
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

                                    scope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder().center(location).zoom(16.0)
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

                                    scope.launch {
                                        mapboxMapInstance?.flyTo(
                                            CameraOptions.Builder().center(location).zoom(16.0)
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

                })
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
                    contentDescription = "Abrir", tint = Color.White, // color del ícono
                    modifier = Modifier.size(20.dp) // tamaño del ícono
                )
            }
        }


        if (show_bottom_sheet_datos_tienda_lugares) {
            bottom_sheet_tiendas_filtradas(
                verificar_intener,
                viewModel_filtrado_tiendas,
                dataclass_tienda_seleccionada,
                show_bottom_sheet_datos_tienda_lugares
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
                "Para una mejor experiencia y " + "poder mostrar tu ubicación actual en el mapa, por favor habilita la función de ubicación en tu dispositivo. Esto te permitirá ubicarte de manera más rápida y conocer la proximidad a tu destino.",
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
            context.resources, R.drawable.logo_geinz_500x500 // tu placeholder local en drawable
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
        Shader.TileMode.CLAMP,
        Shader.TileMode.CLAMP
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
            Math.toDegrees(lon + deltaLon), Math.toDegrees(lat + deltaLat)
        )
        coordinates.add(point)
    }

    return Polygon.fromLngLats(listOf(coordinates))
}

// ✅ Fuera de la función — singleton, se crea una sola vez
private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .build()

suspend fun obtenerRuta(
    originLat: Double, originLng: Double, destLat: Double, destLng: Double, profile: String
): List<Point>? {

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
                    val coordinates = routes
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                    val points = mutableListOf<Point>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)))
                    }

                    Log.d("DESVIO_DEBUG", "✅ Puntos extraídos: ${points.size}")
                    points
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

fun obtenerPuntoMasCercanoEnSegmento(p: Point, a: Point, b: Point): Point {
    val atp = doubleArrayOf(p.longitude() - a.longitude(), p.latitude() - a.latitude())
    val atb = doubleArrayOf(b.longitude() - a.longitude(), b.latitude() - a.latitude())

    val dot = atp[0] * atb[0] + atp[1] * atb[1]
    val lenSq = atb[0] * atb[0] + atb[1] * atb[1]

    var param = if (lenSq != 0.0) dot / lenSq else -1.0

    param = when {
        param < 0 -> 0.0
        param > 1 -> 1.0
        else -> param
    }

    return Point.fromLngLat(
        a.longitude() + param * atb[0], a.latitude() + param * atb[1]
    )
}

@Composable
fun estilo_visual_btns(
    img: String?,
    icon: ImageVector?,
    txt: String?,
    tipo: String,
    listener: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable {
                listener()
            }, contentAlignment = Alignment.Center
    ) {
        when (tipo) {
            "img" -> {
                if (img != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(img)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias).build(),
                        contentDescription = "Mi ubicación",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            "icono" -> {
                if (icon != null) {
                    Icon(icon, contentDescription = "Mi ubicación")
                }
            }

            "text" -> {
                if (txt != null) {
                    texto_generico_one_line(
                        txt, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

    }
}

@Composable
fun desing_creacion_ruta(
    context: Context,
    lista: List<iconos_creaciones_rutas>,
    img_tienda: String,
    seleccionado: (String) -> Unit,
    cancelacion_ruta: () -> Unit,
    ocultar_dialog_: () -> Unit, mostrar_campo: () -> Unit, mostar_dialog_no_ubi_activa: () -> Unit
) {

    var seleccionadoActual by remember { mutableStateOf<String?>(null) }

    val listaVisible = if (seleccionadoActual == null) {
        lista
    } else {
        lista.filter { it.tipo == seleccionadoActual }
    }

    listaVisible.forEach { i ->
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    if (verificarUbiActiva(context)) {

                        if (seleccionadoActual == i.tipo) {
                            seleccionadoActual = null
                            cancelacion_ruta()
                        } else {
                            seleccionadoActual = i.tipo
                            seleccionado(i.tipo)
                            ocultar_dialog_()
                        }
                    } else {
                        mostar_dialog_no_ubi_activa()
                    }
                }, contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = i.icono, contentDescription = i.tipo
            )
        }
    }
    if (!seleccionadoActual.isNullOrEmpty()) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(img_tienda)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias).build(),
            contentDescription = "Mi ubicación",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    mostrar_campo()
                },
            contentScale = ContentScale.Crop
        )
    }

}


fun calculateBearing(from: Point, to: Point): Double {
    val lat1 = Math.toRadians(from.latitude())
    val lon1 = Math.toRadians(from.longitude())
    val lat2 = Math.toRadians(to.latitude())
    val lon2 = Math.toRadians(to.longitude())
    val dLon = lon2 - lon1
    val y = Math.sin(dLon) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
    return Math.toDegrees(Math.atan2(y, x))
}


fun vibrarTelefono(context: Context) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE)
        != PackageManager.PERMISSION_GRANTED
    ) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(300)
        }
    }
}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.R.attr.duration
import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.geinzz.geinzwork.data.model.localizate_geinz.obj_cuando_creas_rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.FabMenuAjustes
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.ListaChips
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.box_datos_botones_faciles
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.desing_creacion_ruta
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.dibujarRutaEnMapa
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.estilo_botons_circulares
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.estilo_carta_visual_inmueble
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.filtro_chips_categoria_Seleccioanda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.img_container
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.limpiarRutaEnMapa
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularDistanciaMetros
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatearDistancia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import kotlin.math.roundToInt

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
    var subcategoria_seleccionada by remember { mutableStateOf("") }
    val lista_categoria_lugares_seguros by viewmodel_mapa_inmobilia.categorias_mas_lista_lugares_cercanos_seguros.collectAsState()
    val lista_categoria_lugares_cercanos by viewmodel_mapa_inmobilia.categorias_mas_lista_lugares_cercanos.collectAsState()
    val lista_categoria_lugares_turisticos by viewmodel_mapa_inmobilia.categorias_mas_lista_lugares_cercanos_turisticos.collectAsState()
    val lista_categoria_lugares_servicios_hogar by viewmodel_mapa_inmobilia.categorias_mas_lista_lugares_cercanos_hogar.collectAsState()
    var select_filtrado_sub by remember { mutableStateOf("") }
    val estadoRuta by viewmodel_mapa_inmobilia.estadoRuta.collectAsState()
    var confuracion_seleccionda by remember { mutableStateOf("Mapa nocturno") }
    var pitch_selecciondo by remember { mutableStateOf("2D") }
    var mostrar_ocultar_immagen by remember { mutableStateOf(true) }
    var lista_seleccionada by remember { mutableStateOf(obj_pasado_clikeado_mapa()) }
    val seguirUbicacion = remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()

    var mostar_dialog_lugare_Cercanos by remember { mutableStateOf(false) }
    var mostra_lugar_seguro_dialog by remember { mutableStateOf(false) }
    var mostrar_lugares_hogares by remember { mutableStateOf(false) }

    var id_negocio_lugar_previwe by remember { mutableStateOf("") }
    var localida_negocio_lugar_preview by remember { mutableStateOf("") }
    var nombre_negocio_select_preview by remember { mutableStateOf("") }
    var distancia_terreno by remember { mutableStateOf("") }
    var img_negocio_preview by remember { mutableStateOf("") }
    val bottomSheetVisible by viewmodelMapa.estadoBottomSheet.collectAsState()
    val datos_numeros_salud_seguridad by viewModelFiltros.instance_salud_seguridad.collectAsState()
    var seleccionado_posible by remember { mutableStateOf<String?>(null) }
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }
var lista_subcategoria_selecciondad by remember { mutableStateOf<List<String>>(emptyList()) }
// ── Estado de ruta ─────────────────────────────────────────
    var puntos_ruta_activa by remember { mutableStateOf<List<Point>>(emptyList()) }
    var distancia_ruta_metros by remember { mutableStateOf(0) }
    var velocidad_actual by remember { mutableStateOf(0f) }
    var perfil_creacion_ruta_seleccionada by remember { mutableStateOf("") }
    var icono_creacion_ruta_seleccionada by remember { mutableStateOf(obj_cuando_creas_rutas()) }
    var distancia_al_destino by remember { mutableStateOf(0f) }
    var duracion_ruta_segundos by remember { mutableStateOf(0.0) }
    var tiempo_de_ruta_llega_string by remember { mutableStateOf("") }
    var hora_llegada_string by remember { mutableStateOf("") }

    LaunchedEffect(perfil_creacion_ruta_seleccionada) {
        icono_creacion_ruta_seleccionada = when (perfil_creacion_ruta_seleccionada) {
            "driving" -> {
                obj_cuando_creas_rutas("driving", Icons.Default.DirectionsCar, 0)

            }

            "walking" -> {
                obj_cuando_creas_rutas("walking", Icons.Default.DirectionsWalk, 0)

            }

            "cycling" -> {
                obj_cuando_creas_rutas("cycling", Icons.Default.DirectionsBike, 0)
            }

            else -> {
                obj_cuando_creas_rutas()
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
    var ultimaLat by remember { mutableStateOf(0.0) }
    var ultimaLng by remember { mutableStateOf(0.0) }
    var ultimoTiempo by remember { mutableStateOf(0L) }
    val velocidadBuffer = remember { ArrayDeque<Float>() }
    var ultimoBearing by remember { mutableStateOf(0.0) }
    val estaRecalculando = remember { mutableStateOf(false) }
    val ultimoRecalculo = remember { longArrayOf(0L) }
    var mostrar_carga_datos_prores by remember { mutableStateOf(true) }
    var seguimiento_automatico by remember { mutableStateOf(true) }
    val cerca_del_destino by remember(distancia_al_destino) {
        derivedStateOf { distancia_al_destino in 1f..100f }
    }
    // Agrega esto junto a los demás estados
    var chip_cambio_contador by remember { mutableStateOf(0) }

    var nueva_busqueda by remember { mutableStateOf(0f) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")
        }
    }
    val colorBottomSheet by animateColorAsState(
        targetValue = when {
            !ruta_creada -> Color.Black
            cerca_del_destino -> Color(0xFF166534)
            else -> Color.Black
        },
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "color_bottom_sheet"
    )

    DisposableEffect(Unit) {
        onDispose {
            viewmodel_mapa_inmobilia.limpiarEstadoRuta()
        }
    }

    LaunchedEffect(ruta_creada) {
        if (ruta_creada) {

            pitch_selecciondo = "3D"

            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }

        } else {
            pitch_selecciondo = "2D"
        }
    }
    LaunchedEffect(ruta_creada, lat_user) {
        if (ruta_creada && lat_user != 0.0 && lat_lugar_seleccionado != 0.0) {

            distancia_al_destino = calcularDistanciaMetros(
                lat_user, lng_user,
                lat_lugar_seleccionado, lng_lugar_seleccionado
            )

            Log.d("distancia_inicial", "📍 ${distancia_al_destino.toInt()} metros")
        }
    }

    LaunchedEffect(distancia_al_destino, velocidad_actual, duracion_ruta_segundos) {
        if (!ruta_creada) return@LaunchedEffect
        if (distancia_al_destino <= 0f) return@LaunchedEffect

        val segundosRestantes = when {
            // Usuario moviéndose → velocidad real
            velocidad_actual > 2f -> {
                val dist = if (distancia_ruta_metros > 0) distancia_ruta_metros.toFloat()
                else distancia_al_destino
                (dist / (velocidad_actual / 3.6f)).toInt()
            }
            // API ya respondió → usar duración oficial
            duracion_ruta_segundos > 0.0 -> duracion_ruta_segundos.toInt()
            // ✅ API aún no llegó → estimar con función auxiliar
            else -> viewmodel_mapa_inmobilia.calcularSegundosEstimados(
                distancia_al_destino,
                perfil_creacion_ruta_seleccionada
            )
        }

        if (segundosRestantes <= 0) return@LaunchedEffect  // ✅ nunca mostrar "Ya llegaste" de inicio

        val horas = segundosRestantes / 3600
        val minutos = (segundosRestantes % 3600) / 60
        val segundos = segundosRestantes % 60

        tiempo_de_ruta_llega_string = when {
            horas > 0 && minutos > 0 -> "${horas}h ${minutos}min"
            horas > 0 -> "${horas}h"
            minutos > 0 -> "${minutos}min"
            segundos > 0 -> "${segundos}seg"
            else -> "Menos de 1 min"
        }

        val ahora = java.util.Calendar.getInstance()
        ahora.add(java.util.Calendar.SECOND, segundosRestantes)
        val hora = ahora.get(java.util.Calendar.HOUR_OF_DAY)
        val min = ahora.get(java.util.Calendar.MINUTE)
        val amPm = if (hora < 12) "AM" else "PM"
        val hora12 = when {
            hora == 0 -> 12
            hora > 12 -> hora - 12
            else -> hora
        }
        hora_llegada_string = String.format("%02d:%02d %s", hora12, min, amPm)
    }

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
    // 👇 Reemplaza este LaunchedEffect existente
    LaunchedEffect(
        chipSeleccionado,
        subcategoria_seleccionada,
        datos_obtener_mapa,
        EstadoMapa.managerSecundario.value,
        lista_categoria_lugares_seguros,
        lista_categoria_lugares_cercanos,
        lista_categoria_lugares_turisticos,
        lista_categoria_lugares_servicios_hogar
    ) {
        Log.d("MAPA_FILTER", "==============================")
        Log.d("MAPA_FILTER", "chip: $chipSeleccionado")
        Log.d("MAPA_FILTER", "subcategoria: '$subcategoria_seleccionada'")

        // ✅ Si es una categoria con subcategorias, esperar a que llegue la subcategoria
        val requiereSubcategoria = chipSeleccionado != "Principal"
        if (requiereSubcategoria && subcategoria_seleccionada.isBlank()) {
            Log.d("MAPA_FILTER", "⏳ Esperando subcategoría, no se setean puntos aún")
            return@LaunchedEffect
        }

        val listaCompleta = when (chipSeleccionado) {
            "Lugares seguros" -> obj_pasado_clikeado_mapa(
                "lugar_seguro",
                lista_categoria_lugares_seguros.lista_data
            )

            "Lugares cercanos" -> obj_pasado_clikeado_mapa(
                "lugar_cercanos",
                lista_categoria_lugares_cercanos.lista_data
            )

            "Lugares turísticos" -> obj_pasado_clikeado_mapa(
                "lugar_turistico",
                lista_categoria_lugares_turisticos.lista_data
            )

            "Lugares para el hogar" -> obj_pasado_clikeado_mapa(
                "lugar_servicios",
                lista_categoria_lugares_servicios_hogar.lista_data
            )

            else -> obj_pasado_clikeado_mapa()
        }

        Log.d("MAPA_FILTER", "listaCompleta.datos.size: ${listaCompleta.datos.size}")

        val listaFinal = if (subcategoria_seleccionada.isNotBlank()) {
            val filtrados = listaCompleta.datos.filter { it.categoira == subcategoria_seleccionada }
            Log.d(
                "MAPA_FILTER",
                "Filtrando por '$subcategoria_seleccionada' → ${filtrados.size} resultados"
            )
            if (filtrados.isEmpty()) {
                Log.w("MAPA_FILTER", "⚠️ FILTRO VACÍO")
                Log.w(
                    "MAPA_FILTER",
                    "Categorías disponibles: ${listaCompleta.datos.map { it.categoira }.distinct()}"
                )
            }
            listaCompleta.copy(datos = filtrados)
        } else {
            listaCompleta
        }

        Log.d("MAPA_FILTER", "listaFinal enviada al mapa: ${listaFinal.datos.size} puntos")
        Log.d("MAPA_FILTER", "==============================")

        lista_seleccionada = listaFinal

        viewmodel_mapa_inmobilia.setear_puntos_clikeados(
            lista = listaFinal,
            onPuntoClick = { id, lat, lng, img, nombre, distancia ->
                val distanciaTexto = formatearDistanciaDouble(distancia)
                if (seleccionado_posible == id) {
                    seleccionado_posible = null
                    EstadoMapa.seleccionarPinPorId("")
                    img_negocio_preview = ""
                    nombre_negocio_select_preview = ""
                    distancia_terreno = ""
                } else {
                    seleccionado_posible = id
                    img_negocio_preview = img
                    nombre_negocio_select_preview = nombre
                    distancia_terreno = distanciaTexto
                    mapboxMapInstance?.easeTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(lng, lat))
                            .zoom(16.0)
                            .build(),
                        MapAnimationOptions.mapAnimationOptions { duration(800) }
                    )
                }
            }
        )
    }

    val categorias = listOf(
        categorias_diltrado_mapa_inmobiliara(
            nombre = "Principal",
            cantidad = 0,
            categoria = emptyList()
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares seguros",
            lista_categoria_lugares_seguros.lista_data.size,
            lista_categoria_lugares_seguros.lista_categoira,
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares cercanos",
            lista_categoria_lugares_cercanos.lista_data.size,
            lista_categoria_lugares_cercanos.lista_categoira
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares turísticos",
            lista_categoria_lugares_turisticos.lista_data.size,
            lista_categoria_lugares_turisticos.lista_categoira
        ),
        categorias_diltrado_mapa_inmobiliara(
            "Lugares para el hogar",
            lista_categoria_lugares_servicios_hogar.lista_data.size,
            lista_categoria_lugares_servicios_hogar.lista_categoira
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

// ✅ Agrega este LaunchedEffect junto a los demás
    LaunchedEffect(estadoRuta) {
        val exitosa = estadoRuta ?: return@LaunchedEffect

        puntos_ruta_activa = exitosa.puntos
        distancia_ruta_metros = exitosa.distanciaMetros.toInt()
        duracion_ruta_segundos = exitosa.duracionSegundos

        mapboxMapInstance?.let { map ->
            dibujarRutaEnMapa(map, exitosa.puntos)

            // Calcular bearing (rumbo) del usuario hacia el destino
            val bearingHaciaDestino = viewmodel_mapa_inmobilia.calcularBearing(
                lat_user, lng_user,
                lat_lugar_seleccionado, lng_lugar_seleccionado
            )

            map.easeTo(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(lng_user, lat_user)) // cámara sobre el usuario
//                    .zoom(17.5)
                    .bearing(bearingHaciaDestino) // orientado hacia el destino
                    .build(),
                MapAnimationOptions.mapAnimationOptions { duration(1000) }
            )
        }
        ruta_creada = true
        ruta_cargando = false
    }

    LaunchedEffect(lat_user, lng_user) {
        if (puntos_ruta_activa.isEmpty()) return@LaunchedEffect
        if (!ruta_creada) return@LaunchedEffect

        val miUbicacion = Point.fromLngLat(lng_user, lat_user)

        // Encontrar segmento más cercano
        var mejorIndice = 0
        var mejorDistancia = Float.MAX_VALUE

        for (i in 0 until puntos_ruta_activa.size - 1) {
            val snap = viewmodel_mapa_inmobilia.obtenerPuntoMasCercanoEnSegmento(
                miUbicacion,
                puntos_ruta_activa[i],
                puntos_ruta_activa[i + 1]
            )
            val dist = calcularDistanciaMetros(
                lat_user, lng_user,
                snap.latitude(), snap.longitude()
            )
            if (dist < mejorDistancia) {
                mejorDistancia = dist
                mejorIndice = i
            }
        }

        // ── Recalcular si se desvió ──────────────────────────────
        val ahora = System.currentTimeMillis()
        if (mejorDistancia > 10f && perfil_creacion_ruta_seleccionada.isNotBlank()) {
            if (!estaRecalculando.value && ahora - ultimoRecalculo[0] > 8_000L) {
                estaRecalculando.value = true
                ultimoRecalculo[0] = ahora
                viewmodel_mapa_inmobilia.crear_ruta(
                    lat_user, lng_user,
                    lat_lugar_seleccionado, lng_lugar_seleccionado,
                    perfil_creacion_ruta_seleccionada
                )
                estaRecalculando.value = false
            }
            return@LaunchedEffect
        }

        // ── Recortar ruta visualmente ────────────────────────────
        if (mejorIndice >= puntos_ruta_activa.size - 1) return@LaunchedEffect

        val puntoSnap = viewmodel_mapa_inmobilia.obtenerPuntoMasCercanoEnSegmento(
            miUbicacion,
            puntos_ruta_activa[mejorIndice],
            puntos_ruta_activa[mejorIndice + 1]
        )

        val listaVisual = mutableListOf(puntoSnap)
        listaVisual.addAll(puntos_ruta_activa.drop(mejorIndice + 1))

        // Actualizar línea en el mapa
        mapboxMapInstance?.getStyle { style ->
            style.getSourceAs<GeoJsonSource>("route_source")
                ?.featureCollection(
                    FeatureCollection.fromFeature(
                        Feature.fromGeometry(LineString.fromLngLats(listaVisual))
                    )
                )
        }

        // Recortar lista interna
        if (mejorIndice > 0) {
            puntos_ruta_activa = puntos_ruta_activa.drop(mejorIndice)
        }
    }

    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(contex) }
    val fabColor by animateColorAsState(
        targetValue = if (seguirUbicacion.value) MaterialTheme.colorScheme.primary else Color(
            0xFF9C7BFF
        ), animationSpec = tween(
            durationMillis = 300 // 0.3 segundos, suave pero rápido
        )
    )

    val estaExpandido = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
    val estaColapsado = scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded
    val estaOculto = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 90.dp, // 👈 TIRITA SIEMPRE VISIBLE
        sheetDragHandle = null,
        sheetContainerColor = colorBottomSheet,
        sheetContent = {


            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
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
                                .padding(start = 10.dp, end = 10.dp, top = 8.dp)
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
                                            text = "Todo lo que rodea tu próxima inversion",
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
                                        //tablero de ruta creada
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
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val distanciaTexto =
                                                    formatearDistancia(distancia_al_destino)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Icon(
                                                        icono_creacion_ruta_seleccionada.icono,
                                                        contentDescription = "Mi ubicación",
                                                        tint = Color.Gray
                                                    )

                                                    texto_generico_one_line(
                                                        tiempo_de_ruta_llega_string,
                                                        style = MaterialTheme.typography.titleLarge
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {

                                                    texto_generico_one_line(
                                                        distanciaTexto,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                    texto_generico_one_line(
                                                        "/"
                                                    )
                                                    texto_generico_one_line(
                                                        hora_llegada_string,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )


                                                }
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Column(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
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
                                                    color = Color.Black,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }

                                        }
                                    } else {
                                        // tablero sin ruta creada

                                        if (seleccionado_posible.isNullOrEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(17.dp)
                                            ) {

                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Text(
                                                        text = "Lugares cercanos a tu proxima inversion",
                                                        fontSize = 17.sp,
                                                        fontFamily = baners_geinz_work,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    texto_generico_one_line(
                                                        "Conoce los lugares y toma una mejor desicion",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                                estilo_botons_circulares(
                                                    color = Color.White,
                                                    iconoTint = MaterialTheme.colorScheme.primary,
                                                    icon = Icons.Default.ArrowDropUp,
                                                    onclick = {
                                                        scope.launch {
                                                            scaffoldState.bottomSheetState.expand()
                                                        }
                                                    })
                                            }
                                        } else {

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
                                                    Column(modifier = Modifier.width(150.dp)) {// 👈 toma el espacio disponible y cede a los botones) {
                                                        Text(
                                                            text = nombre_negocio_select_preview,
                                                            fontSize = 14.sp,
                                                            fontFamily = baners_geinz_work,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        texto_generico_one_line(
                                                            "A $distancia_terreno de tu inversion ",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.Gray
                                                        )
                                                    }

                                                }
                                                item {
                                                    desing_creacion_ruta(
                                                        distancia = distancia_ruta_metros,
                                                        context = contex,
                                                        lista = lista_iconos_ruta,
                                                        seleccionado = { perfil, icono ->
                                                            // 1️⃣ Obtener ubicación actual
                                                            perfil_creacion_ruta_seleccionada =
                                                                perfil
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
                                                                        viewmodel_mapa_inmobilia.crear_ruta(
                                                                            lat_user,
                                                                            lng_user,
                                                                            destino.lat,
                                                                            destino.lng,
                                                                            perfil
                                                                        )
                                                                        lng_lugar_seleccionado =
                                                                            destino.lng
                                                                        lat_lugar_seleccionado =
                                                                            destino.lat
                                                                        ruta_creada =
                                                                            true   // ← ruta lista
                                                                    }
                                                                    ruta_cargando = false
                                                                }
                                                            }
                                                        },
                                                        cancelacion_ruta = {
                                                            // Limpiar estado y mapa
                                                            viewmodel_mapa_inmobilia.limpiarEstadoRuta()
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
                                                            distancia_terreno =
                                                                formatearDistanciaDouble(siguiente.distanciaKm)

                                                            EstadoMapa.seleccionarPinPorId(siguiente.id)
                                                            img_negocio_preview =
                                                                siguiente.img_String
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
                        }
                        if (!ruta_creada) {
                            if (!mostrar_carga_datos_prores) {
                                Box(
                                    modifier = Modifier
                                        .height(140.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(
                                            15.dp,
                                            Alignment.CenterHorizontally
                                        )
                                    ) {
                                        texto_generico_one_line(
                                            "Cargando lo mejor para ti",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        CircularProgressIndicator()
                                    }
                                }
                            } else {
                                img_container(
                                    lista_seleccionada = lista_seleccionada,
                                    seleccionado = seleccionado_posible,
                                    lugar_clikeado = { id, lat, lng, img, nombre, distancia ->
                                        if (seleccionado_posible == id) {
                                            seleccionado_posible = null
                                            EstadoMapa.seleccionarPinPorId("")
                                            img_negocio_preview = ""
                                            nombre_negocio_select_preview = ""
                                            distancia_terreno = ""
                                        } else {
                                            distancia_terreno = formatearDistanciaDouble(distancia)
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
                                        }
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
                                    }

                                )
                                filtro_chips_categoria_Seleccioanda(
                                    seleccioando = subcategoria_seleccionada,
                                    categoria = lista_subcategoria_selecciondad,
                                    cateogira_selecto = { categoriaSeleccionada ->
                                        subcategoria_seleccionada = categoriaSeleccionada
                                        when (chipSeleccionado) {
                                            "Lugares seguros" -> viewmodel_mapa_inmobilia.aplicarFiltroSeguros(
                                                categoriaSeleccionada
                                            )

                                            "Lugares cercanos" -> viewmodel_mapa_inmobilia.aplicarFiltrolugares_cercanos(
                                                categoriaSeleccionada
                                            )

                                            "Lugares turísticos" -> viewmodel_mapa_inmobilia.aplicarFiltroTuristicos(
                                                categoriaSeleccionada
                                            )

                                            "Lugares para el hogar" -> viewmodel_mapa_inmobilia.aplicarFiltroHogar(
                                                categoriaSeleccionada
                                            )
                                        }
                                    })
                            }
                        }
                    }
                }
                if (!ruta_creada) {
                        ListaChips(
                            subateoria = subcategoria_seleccionada,
                            categorias = categorias,
                            seleccionado = chipSeleccionado,
                            onSeleccionar = {it,lista->
                                lista_subcategoria_selecciondad = lista
                                seleccionado_posible = null
                                mostrar_carga_datos_prores = false
                                chipSeleccionado = it
                                subcategoria_seleccionada = ""
                                chip_cambio_contador++
                                if (it == "Principal") {
                                    mostrar_ocultar_immagen = true
                                } else {
                                    mostrar_ocultar_immagen = false
                                    if (!ruta_creada_state.value) {
                                        scope.launch {
                                            // ✅ Pequeño delay para que los cambios de estado no interrumpan la animación
                                            kotlinx.coroutines.delay(50)
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                }
                            }, todos_cargados = {
                                mostrar_carga_datos_prores = true
                            }
                        )

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
//                        mapboxMap.setBounds(
//                            com.mapbox.maps.CameraBoundsOptions.Builder()
//                                .maxZoom(19.0)   // 👈 zoom máximo
//                                .maxPitch(65.0)  // 👈 pitch máximo que el usuario puede mover
//                                .build()
//                        )

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
                        // Dentro del MapEffect, reemplaza el listener de posición
                        mapView.location.addOnIndicatorPositionChangedListener { point ->
                            val lat = point.latitude()
                            val lng = point.longitude()
                            lat_user = lat
                            lng_user = lng

                            val ahora = System.currentTimeMillis()

                            if (ultimaLat != 0.0 && ultimoTiempo != 0L) {
                                val tiempoSegundos = (ahora - ultimoTiempo) / 1000f

                                if (tiempoSegundos >= 1f) {
                                    val metros =
                                        calcularDistanciaMetros(ultimaLat, ultimaLng, lat, lng)

                                    // ✅ Siempre actualizar posición y tiempo
                                    ultimaLat = lat
                                    ultimaLng = lng
                                    ultimoTiempo = ahora

                                    if (metros < 1.5f) {
                                        // ✅ Sin movimiento → bajar gradualmente hasta 0
                                        velocidadBuffer.clear() // limpiar buffer
                                        velocidad_actual = (velocidad_actual * 0.4f)
                                            .coerceAtMost(200f)
                                        if (velocidad_actual < 1f) velocidad_actual = 0f

                                    } else {
                                        val velocidadRaw = (metros / tiempoSegundos) * 3.6f

                                        if (velocidadRaw <= 200f) {
                                            velocidadBuffer.addLast(velocidadRaw)
                                            if (velocidadBuffer.size > 4) velocidadBuffer.removeFirst()

                                            // ✅ Promedio del buffer = velocidad suavizada
                                            velocidad_actual = velocidadBuffer.average().toFloat()
                                        }
                                    }
                                }
                            } else {
                                // Primera posición
                                ultimaLat = lat
                                ultimaLng = lng
                                ultimoTiempo = ahora
                                velocidad_actual = 0f
                            }
                        }
                        mapboxMap.addOnMoveListener(object : OnMoveListener {
                            override fun onMoveBegin(detector: MoveGestureDetector) {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                }
                                if (rutaCreadaRef.value) {
                                    seguimiento_automatico = false
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
            AnimatedVisibility(visible = !ruta_creada, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(
                    modifier = Modifier.padding(10.dp),
                    containerColor = fabColor,
                    contentColor = Color.White,
                    onClick = {
                        if (verificarUbiActiva(contex)) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val user_point = Point.fromLngLat(it.longitude, it.latitude)
                                    scope.launch {
                                        seguimiento_automatico = true
                                        mapboxMapInstance?.easeTo(
                                            CameraOptions.Builder()
                                                .center(Point.fromLngLat(lng_user, lat_user))
                                                .build(),
                                            MapAnimationOptions.mapAnimationOptions { duration(600) }
                                        )
                                    }
                                }
                            }
                        } else {
                            validacion_mostrar_dialog_ubi_off = true
                        }

                    }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }
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
                    ver_mapa = {}, iniciar_seccion = { iniciar_seccion() }, crear_cuenta = { crear_cuenta() },
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
            if (validacion_mostrar_dialog_ubi_off) {
                dialog_sin_ubi__rutas(
                    "Para una mejor experiencia y " + "poder mostrar tu ubicación actual en el mapa, por favor habilita la función de ubicación en tu dispositivo. Esto te permitirá ubicarte de manera más rápida y conocer la proximidad a tu destino.",
                    { validacion_mostrar_dialog_ubi_off = false },
                    {
                        validacion_mostrar_dialog_ubi_off = false
                        verificarGPS(contex, launcher)
                    })
            }
            AnimatedVisibility(
                visible = ruta_creada,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 15.dp, bottom = 95.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(fabColor)
                        .clickable() {
                            seguimiento_automatico = true
                            mapboxMapInstance?.easeTo(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(lng_user, lat_user))
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions { duration(600) }
                            )
                        }) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = Color.White, modifier = Modifier.padding(15.dp)
                    )
                }
            }

        }
    }
}

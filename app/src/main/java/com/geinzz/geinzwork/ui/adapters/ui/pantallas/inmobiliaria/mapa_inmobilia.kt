package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.R.attr.duration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.sp
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.EstadoMapa
import com.geinzz.geinzwork.data.model.categorias_diltrado_mapa_inmobiliara
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatearDistanciaDouble
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mapa_inmobilia(viewmodel_mapa_inmobilia: viewmodel_mapa_inmobiliara) {
    var ulr_esilo by remember { mutableStateOf("") }
    var estilo_mapa_mapbox by remember { mutableStateOf(false) }
    val contex = LocalContext.current
    var chipSeleccionado by remember { mutableStateOf("Principal") }
    val datos_obtener_mapa by viewmodel_mapa_inmobilia.datosInmueble.collectAsState()
    var confuracion_seleccionda by remember { mutableStateOf("Mapa nocturno") }
    var pitch_selecciondo by remember { mutableStateOf("2D") }
    var mostrar_ocultar_immagen by remember { mutableStateOf(true) }
    var lista_seleccionada by remember { mutableStateOf<List<lugares_cercanos_>>(emptyList()) }
    val seguirUbicacion = remember { mutableStateOf(false) }
//    val lista_configuracion = listOf(
//        "Mapa de dia", "Mapa nocturno"
//    )
//    val lista_2d_3d = listOf(
//        "3D", "2D"
//    )

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
    LaunchedEffect(chipSeleccionado, datos_obtener_mapa, EstadoMapa.managerSecundario.value) {
        val manager = EstadoMapa.managerSecundario.value ?: return@LaunchedEffect

        val lista: List<lugares_cercanos_> = when (chipSeleccionado) {
            "Lugares seguros" -> datos_obtener_mapa.cantidad_lugares_seguros
            "Lugares cercanos" -> datos_obtener_mapa.cantidad_lugares_cercanos
            "Lugares turísticos" -> datos_obtener_mapa.cantidad_lugares_turisticos
            "Lugares para el hogar" -> datos_obtener_mapa.cantidad_lugares_para_el_hogar
            else -> emptyList()
        }
        lista_seleccionada = lista
        setear_puntos_clikeados(lista)
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

    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }
    // ── En mapa_inmobilia, agrega este estado ─────
    var chipsExpandido by remember { mutableStateOf(false) }
    val iconoFlechaRotacion by animateFloatAsState(
        targetValue = if (chipsExpandido) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "flecha"
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

    val scaffoldState = rememberBottomSheetScaffoldState()

    val scope=rememberCoroutineScope()
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
                            modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 10.dp)
                        ) {

                            Text(
                                text = "Todo lo que rodea tu próximo terreno",
                                fontSize = 20.sp,
                                fontFamily = baners_geinz_work,
                            )

                            texto_generico_one_line(
                                "Conoce los lugares cercanos y toma una mejor decisión",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            img_container(lista_seleccionada, { lat, lng ->
                                mapboxMapInstance?.easeTo(
                                    CameraOptions.Builder()
                                        .center(Point.fromLngLat(lng, lat))
                                        .zoom(16.0)
                                        .build(),
                                    MapAnimationOptions.mapAnimationOptions {
                                        duration(800)
                                    }
                                )
                            })
                        }
                    }

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
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand() // 👈 sube el sheet
                                    }

                                }
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
    lista_datos: List<lugares_cercanos_>,
    lugar_clikeado: (lat: Double, lng: Double) -> Unit
) {
    var seleccionado by remember { mutableStateOf<lugares_cercanos_?>(null) }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(lista_datos) { datos ->
            val estaSeleccionado = seleccionado == datos

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
                                    seleccionado = datos
                                    lugar_clikeado(datos.lat, datos.lng)
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
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
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
fun setear_puntos_clikeados(lista: List<lugares_cercanos_>) {
    val manager = EstadoMapa.managerSecundario.value ?: return
    val mapboxMap = EstadoMapa.mapboxMapGlobal.value ?: return
    val contexto = EstadoMapa.contextoGlobal ?: return

    manager.deleteAll()

    if (lista.isEmpty()) {
        EstadoMapa.cargandoPuntos.value = false
        return
    }

    // ✅ Inicia carga
    EstadoMapa.cargandoPuntos.value = true

    mapboxMap.getStyle { style ->
        kotlinx.coroutines.MainScope().launch {
            var completados = 0
            val total = lista.size

            lista.forEachIndexed { index, lugar ->
                val punto = Point.fromLngLat(lugar.lng, lugar.lat)
                val imageId = "lugar_icon_$index"

                try {
                    val bitmap = loadBitmapFromUrl(lugar.img_String, contexto).toCircularBitmap(100)
                    style.removeStyleImage(imageId)
                    style.addImage(imageId, bitmap)
                    manager.create(
                        PointAnnotationOptions()
                            .withPoint(punto)
                            .withIconImage(imageId)
                            .withIconAnchor(IconAnchor.CENTER)
                            .withIconSize(0.8)
                    )
                } catch (e: Exception) {
                    val bitmapFallback = crearCirculoFallback(contexto)
                    style.removeStyleImage(imageId)
                    style.addImage(imageId, bitmapFallback)
                    manager.create(
                        PointAnnotationOptions()
                            .withPoint(punto)
                            .withIconImage(imageId)
                            .withIconAnchor(IconAnchor.CENTER)
                            .withIconSize(0.8)
                    )
                }

                completados++
                // ✅ Cuando terminaron todos → apaga el loading
                if (completados == total) {
                    EstadoMapa.cargandoPuntos.value = false
                }
            }
        }
    }
}

fun crearCirculoFallback(contexto: android.content.Context): android.graphics.Bitmap {
    val size = 80
    val bitmap =
        android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
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
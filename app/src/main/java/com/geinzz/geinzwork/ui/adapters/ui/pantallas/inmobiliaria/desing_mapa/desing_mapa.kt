package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.Bathtub
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.KingBed
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.geinzz.geinzwork.data.model.EstadoMapa
import com.geinzz.geinzwork.data.model.categorias_diltrado_mapa_inmobiliara
import com.geinzz.geinzwork.data.model.datos_viewmodel_inmobiliara
import com.geinzz.geinzwork.data.model.localizate_geinz.iconos_creaciones_rutas
import com.geinzz.geinzwork.data.model.obj_pasado_clikeado_mapa
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatearDistanciaDouble
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.mapbox.geojson.Point
import kotlin.text.ifEmpty


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
            .padding(10.dp)
            .clip(RoundedCornerShape(redondeoAnimado))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(alturaAnimada)
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
fun img_container(
    lista_seleccionada: obj_pasado_clikeado_mapa,
    seleccionado: String?,
    lugar_clikeado: (id: String, lat: Double, lng: Double, img: String, nombre: String,distancia: Double) -> Unit,
    ver_mas_: (tipo: String, id: String, localidad: String, img: String, nombre: String) -> Unit
) {

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
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

            Box(
                modifier = Modifier.
                    padding(bottom = 10.dp)
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
                                .height(140.dp)
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
                                        datos.nombre,datos.distanciaKm
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
fun ChipEstilo(
    texto: String,
    cantidad: Int,
    estaSeleccionado: Boolean = false,
    todos_cargados: () -> Unit,
    onClick: () -> Unit = {}
) {
    val cargando by EstadoMapa.cargandoPuntos
    val mostrarProgreso = estaSeleccionado && cargando

    // ✅ Solo se dispara cuando termina de cargar (cargando pasa de true → false)
    LaunchedEffect(estaSeleccionado, cargando) {
        if (estaSeleccionado && !cargando) {
            todos_cargados()
        }
    }

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
            val texto_final = if (cantidad == 0) texto else "$texto ($cantidad)"
            Text(
                text = texto_final,
                color = if (estaSeleccionado) Color.White else Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.3.sp
            )

            AnimatedVisibility(
                visible = estaSeleccionado,
                enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                exit = fadeOut(tween(200)) + scaleOut(tween(200))
            ) {
                if (mostrarProgreso) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    // ✅ Ya NO se llama todos_cargados() aquí
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
fun ListaChips(
    categorias: List<categorias_diltrado_mapa_inmobiliara>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    todos_cargados: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 12.dp , end = 12.dp , top = 5.dp , bottom =5.dp)
    ) {
        items(categorias) { categoria ->
            ChipEstilo(
                texto = categoria.nombre,
                cantidad = categoria.cantidad,
                estaSeleccionado = categoria.nombre == seleccionado,
                {
                    todos_cargados()
                },
                onClick = { onSeleccionar(categoria.nombre)
                   }
            )
        }
    }
}

package com.geinzz.geinzwork.ui.adapters.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.compartir_contacto_pulicaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoExpandibleSuave
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.compartirLugarFirebaseHosttiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.parseDiasHorasRestantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomState

import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.compose.zoom.zoomable
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.valentinilk.shimmer.shimmer
import org.checkerframework.framework.qual.ConditionalPostconditionAnnotation
import java.net.URLEncoder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CollageGoogleMapsStyle(
    id_user: String,
    aspectRatio: Float = 1.4f,
    with: Dp = 250.dp,
    imagenes: List<String>,
    modifier: Modifier = Modifier
) {
    if (imagenes.isEmpty()) return

    val grupos = imagenes.chunked(3)
    var galeriaActiva by remember { mutableStateOf(false) }
    var indiceInicial by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxWidth()) {

        // --- Collage principal ---
        LazyRow(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .wrapContentHeight(),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 8.dp),
        ) {
            items(grupos) { grupo ->
                GrupoCollageGoogle(
                    aspectRatio = aspectRatio,
                    baseWidth = with,
                    imagenes = grupo,
                    onClickImagen = { url ->
                        indiceInicial = imagenes.indexOf(url)
                        galeriaActiva = true
                    }
                )
            }
        }

        if (galeriaActiva) {
            ZoomableGalleryFullScreen(
                id_user,
                compartir_promocion(),
                "",
                imagenes,
                indiceInicial,
                { galeriaActiva = false })
        }
//        // --- Galería tipo Instagram (fullscreen con animación) ---
//        AnimatedVisibility(
//            visible = galeriaActiva,
//            enter = fadeIn(),
//            exit = fadeOut()
//        ) {
//            GaleriaInstagram(
//                imagenes = imagenes,
//                indiceInicial = indiceInicial,
//                onClose = { galeriaActiva = false }
//            )
//        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CollageGoogleMapsStyle_sin_scroll(
    id_user: String,
    categoria: String,
    it: compartir_promocion,
    tag: String,
    aspectRatio: Float = 1.4f,
    width: Dp = 280.dp,
    imagenes: List<String>, // solo URLs antes
    modifier: Modifier = Modifier
) {
    if (imagenes.isEmpty()) return

    var galeriaActiva by remember { mutableStateOf(false) }
    var indiceInicial by remember { mutableStateOf(0) }

    // Dividimos las imágenes en grupos de 3 para el collage
    val gruposConIndice = imagenes.chunked(3)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .wrapContentWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        gruposConIndice.forEachIndexed { grupoIndex, grupo ->
            GrupoCollageGoogle_sin_scrool(
                categoria,
                tag = tag,
                aspectRatio = aspectRatio,
                baseWidth = width,
                imagenes = grupo,
                onClickImagen = { indiceEnGrupo, _ ->
                    // Calculamos el índice global
                    indiceInicial = (grupoIndex * 3) + indiceEnGrupo
                    galeriaActiva = true
                }
            )
        }
    }

    // Galería zoomable fullscreen
    if (galeriaActiva) {
        ZoomableGalleryFullScreen(
            id_user,
            it = it,
            tag = tag,
            imagenes = imagenes,
            startIndex = indiceInicial
        ) {
            galeriaActiva = false
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CollageGoogleMapsStyle_sin_scroll_promociones(
    id_user: String,
    it: compartir_promocion,
    tag: String,
    aspectRatio: Float = 1.4f,
    width: Dp = 280.dp,
    imagenes: Map<String, String>, // ID -> URL
    modifier: Modifier = Modifier
) {
    Log.d("datoscometaeir", "$imagenes")
    val listaImagenes = imagenes.map { it.key to it.value } // List<Pair<ID, URL>>

    if (listaImagenes.isEmpty()) return

    var galeriaActiva by remember { mutableStateOf(false) }
    var idPromocionSeleccionada by remember { mutableStateOf(listaImagenes.first().first) }

// Dividimos en grupos de 3
    val gruposConIndice = listaImagenes.chunked(3)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .wrapContentWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        gruposConIndice.forEach { grupo ->
            GrupoCollageGoogle_sin_scrool_promociones(
                tag = tag,
                aspectRatio = aspectRatio,
                baseWidth = width,
                imagenes = grupo,
                onClickImagen = { idPromocion, _ ->
                    idPromocionSeleccionada = idPromocion
                    galeriaActiva = true
                }
            )
        }
    }

    if (galeriaActiva) {
        val startIndex = listaImagenes.indexOfFirst { it.first == idPromocionSeleccionada }
            .coerceAtLeast(0)

        ZoomableGalleryFullScreen_para_promociones(
            id_user,
            id_promocion = idPromocionSeleccionada,
            it = it,
            tag = tag,
            imagenes = listaImagenes,
            startIndex = startIndex
        ) {
            galeriaActiva = false
        }
    }
}


@Composable
fun GrupoCollageGoogle_sin_scrool(
    categoria: String,
    tag: String,
    aspectRatio: Float,
    baseWidth: Dp,
    imagenes: List<String>, // <-- original solo URLs
    onClickImagen: (Int, String) -> Unit
) {
    val anchoReal = when (imagenes.size) {
        1 -> baseWidth * 0.5f
        2 -> baseWidth * 0.75f
        else -> baseWidth
    }

    Row(
        modifier = Modifier
            .width(anchoReal)
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(12.dp))
    ) {

        ImagenCollage(
            categoria,
            tag,
            url = imagenes.getOrNull(0),
            modifier = Modifier
                .weight(if (imagenes.size == 1) 1.1f else 2f)
                .fillMaxSize(),
            listener_img = {
                onClickImagen(0, tag)
            }
        )

        if (imagenes.size > 1) {
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight()
                    .padding(start = 7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                ImagenCollage(
                    categoria,
                    tag,
                    url = imagenes.getOrNull(1),
                    modifier = Modifier.weight(1f),
                    listener_img = {
                        onClickImagen(1, tag)
                    }
                )

                if (imagenes.size > 2) {
                    ImagenCollage(
                        categoria,
                        tag,
                        url = imagenes.getOrNull(2),
                        modifier = Modifier.weight(1f),
                        listener_img = {
                            onClickImagen(2, tag)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun GrupoCollageGoogle_sin_scrool_promociones(
    tag: String,
    aspectRatio: Float,
    baseWidth: Dp,
    imagenes: List<Pair<String, String>>, // <-- ahora es ID -> URL
    onClickImagen: (String, String) -> Unit // ID y tag
) {
    val anchoReal = when (imagenes.size) {
        1 -> baseWidth * 0.5f
        2 -> baseWidth * 0.75f
        else -> baseWidth
    }

    Row(
        modifier = Modifier
            .width(anchoReal)
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(12.dp))
    ) {

        ImagenCollage(
            tag = tag,
            url = imagenes.getOrNull(0)?.second,
            modifier = Modifier
                .weight(if (imagenes.size == 1) 1.1f else 2f)
                .fillMaxSize(),
            listener_img = {
                onClickImagen(imagenes[0].first, tag) // pasamos el ID
            }
        )

        if (imagenes.size > 1) {
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight()
                    .padding(start = 7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                ImagenCollage(
                    tag = tag,
                    url = imagenes.getOrNull(1)?.second,
                    modifier = Modifier.weight(1f),
                    listener_img = {
                        onClickImagen(imagenes[1].first, tag)
                    }
                )

                if (imagenes.size > 2) {
                    ImagenCollage(
                        tag = tag,
                        url = imagenes.getOrNull(2)?.second,
                        modifier = Modifier.weight(1f),
                        listener_img = {
                            onClickImagen(imagenes[2].first, tag)
                        }
                    )
                }
            }
        }
    }
}


// ✅ Grupo de 3 imágenes dentro del collage
@Composable
fun GrupoCollageGoogle(
    aspectRatio: Float,
    baseWidth: Dp,
    imagenes: List<String>,
    onClickImagen: (String) -> Unit
) {
    val anchoReal = when (imagenes.size) {
        1 -> baseWidth * 0.5f
        2 -> baseWidth * 0.75f
        else -> baseWidth
    }

    Row(
        modifier = Modifier
            .width(anchoReal) // 👈 ancho dinámico
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(12.dp))
    ) {

        ImagenCollage(
            url = imagenes.getOrNull(0),
            modifier = Modifier
                .weight(if (imagenes.size == 1) 1f else 2f)
                .fillMaxHeight(),
            listener_img = { url -> onClickImagen(url) }
        )

        if (imagenes.size > 1) {
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight()
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ImagenCollage(
                    url = imagenes.getOrNull(1),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    listener_img = { url -> onClickImagen(url) }
                )

                if (imagenes.size > 2) {
                    ImagenCollage(
                        url = imagenes.getOrNull(2),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        listener_img = { url -> onClickImagen(url) }
                    )
                }
            }
        }
    }
}


// ✅ Imagen individual dentro del collage
@Composable
fun ImagenCollage(
    tipo: String = "",
    tag: String = "",
    url: String?,
    modifier: Modifier = Modifier,
    listener_img: (String) -> Unit
) {
    if (url == null) return
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { listener_img(url) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (tag.isNotEmpty()) {
            when (tag) {
                "ambiente" -> {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line("✨")
                        }
                    }
                }

                "productos" -> {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line(emojiPorCategoria(tipo))
                        }
                    }
                }

                "promociones" -> {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line("\uD83D\uDD25")
                        }
                    }
                }
            }
        }
    }
}

fun emojiPorCategoria(categoria: String): String {
    return when (categoria.lowercase()) {

        "bancos y servicios financieros" -> "🏦"
        "belleza" -> "💄"
        "comida y restaurantes" -> "🍽️"
        "deporte y bienestar" -> "🏋️‍♂️"
        "educacion y librerias" -> "📚"
        "entretenimiento y recreacion" -> "🎮"
        "grifos y estaciones" -> "⛽"
        "hogar" -> "🏠"
        "hogar y ferreteria" -> "🛠️"
        "hospedaje y entretenimiento nocturno" -> "🏨"
        "imagen y publicidad" -> "📸"
        "jardineria y plantas" -> "🌱"
        "lavanderias y tintorerias" -> "🧺"
        "mascotas y animales" -> "🐾"
        "mecanica y autoservicios" -> "🚗"
        "moda y estilo" -> "👗"
        "salud y farmacias" -> "💊"
        "servicios de encomienda y envios" -> "📦"
        "servicios tecnicos y reparaciones" -> "🔧"
        "supermercado minimarkets y bodegas" -> "🛒"
        "tecnologia y electronica" -> "💻"
        "transporte y terminales" -> "🚌"
        "turismo" -> "🗿"

        else -> "🏷️" // emoji por defecto
    }
}

// ✅ Galería fullscreen tipo Instagram
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GaleriaInstagram(
    imagenes: List<String>,
    indiceInicial: Int = 0,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = indiceInicial,
        pageCount = { imagenes.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagenes[page])
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Botón cerrar (✕)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable { onClose() }
        ) {
            Text(
                text = "✕",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ZoomableGalleryFullScreen(
    id_user: String,
    it: compartir_promocion,
    tag: String = "",
    imagenes: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (imagenes.isEmpty()) return
    val context = LocalContext.current

    // ====================
    // Accompanist PagerState
    // ====================
    val pagerState = com.google.accompanist.pager.rememberPagerState(initialPage = startIndex)
    var allowScroll by remember { mutableStateOf(true) }
    val zoomableState = rememberZoomableState()
    var indice_cruzado by remember { mutableStateOf(startIndex) }
    val localidad_pasada = when (it.localidad) {
        "barranca" -> "ba"
        "paramonga" -> "par"
        "pativilca" -> "pat"
        "supe" -> "su"
        "puerto supe" -> "pue"
        else -> it.localidad
    }
    // Observamos cambios de página para actualizar indice_cruzado
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                indice_cruzado = page
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ====================
                // Horizontal Pager Accompanist
                // ====================
                com.google.accompanist.pager.HorizontalPager(
                    state = pagerState,
                    count = imagenes.size,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = allowScroll
                ) { page ->
                    ZoomImage(
                        painter = rememberAsyncImagePainter(imagenes[page]),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .zoomable(zoomableState),
                        contentScale = ContentScale.Fit
                    )
                }

                // Botón cerrar
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }

                // Botón WhatsApp para promociones
                if (tag.equals("promociones", ignoreCase = true)) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        btn_compartir(
                            color = Color(0xFF178A3F),
                            icono = R.drawable.whatsapp_icon,
                            txt_icono = "Me interesa",
                            modifier = Modifier.weight(1f),
                            clikeable = {
                                abrir_whattsapp(
                                    id_user,
                                    "promocion",
                                    "",
                                    "",
                                    context = context,
                                    it.numero_tienda,
                                    "Hola, quiero esta oferta que vi en su perfil en Geinz: " +
                                            "https://geinzworkapp.web.app/share?" +
                                            "t=p" +
                                            "&id=${it.id_tienda}" +
                                            "&l=${localidad_pasada}" +
                                            "&c=${it.categoria}" +
                                            "&i=${indice_cruzado}"
                                )
                            })

                        btn_compartir(
                            color = Color(0xFF8700F3),
                            icono = R.drawable.compartir_icon_unico_blanco,
                            txt_icono = "Compartir",
                            modifier = Modifier.weight(1f),
                            clikeable = {
                                compartir_hosting_promo(
                                    id_user,
                                    it.nombre_tienda,
                                    it.categoria,
                                    context,
                                    it.localidad,
                                    it.id_tienda, indice_cruzado.toString()
                                )

                            })
                    }
                }

            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ZoomableGalleryFullScreen_para_promociones(
    id_user: String,
    id_promocion: String,
    it: compartir_promocion,
    tag: String = "",
    imagenes: List<Pair<String, String>>, // Pair<url, idPromocion>
    startIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (imagenes.isEmpty()) return

    val context = LocalContext.current

    // ====================
    // Accompanist PagerState
    // ====================
    val pagerState = com.google.accompanist.pager.rememberPagerState(initialPage = startIndex)
    var allowScroll by remember { mutableStateOf(true) }
    val zoomableState = rememberZoomableState()

    // ID de la promoción actual
    var indice_cruzado by remember { mutableStateOf(id_promocion) }

    val localidad_pasada = when (it.localidad.lowercase()) {
        "barranca" -> "ba"
        "paramonga" -> "par"
        "pativilca" -> "pat"
        "supe" -> "su"
        "puerto supe" -> "pue"
        else -> it.localidad
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                indice_cruzado = imagenes.getOrNull(page)?.first ?: id_promocion
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ====================
                // Horizontal Pager Accompanist
                // ====================
                com.google.accompanist.pager.HorizontalPager(
                    state = pagerState,
                    count = imagenes.size,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = allowScroll
                ) { page ->
                    val (idPromocion, url) = imagenes[page]
                    ZoomImage(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .zoomable(zoomableState),
                        contentScale = ContentScale.Fit
                    )

                }

                // Botón cerrar
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }

                // Botones WhatsApp y Compartir
                if (tag.equals("promociones", ignoreCase = true)) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        btn_compartir(
                            color = Color(0xFF178A3F),
                            icono = R.drawable.whatsapp_icon,
                            txt_icono = "Me interesa",
                            modifier = Modifier.weight(1f),
                            clikeable = {
                                abrir_whattsapp(
                                    id_user,
                                    "promocion",
                                    "",
                                    "",
                                    context = context,
                                    it.numero_tienda,
                                    "Hola, quiero esta oferta que vi en su perfil en Geinz: " +
                                            "https://geinzworkapp.web.app/share?" +
                                            "t=p" +
                                            "&id=${it.id_tienda}" +
                                            "&l=${localidad_pasada}" +
                                            "&c=${it.categoria}" +
                                            "&i=${indice_cruzado}"
                                )
                            }
                        )

                        btn_compartir(
                            color = Color(0xFF8700F3),
                            icono = R.drawable.compartir_icon_unico_blanco,
                            txt_icono = "Compartir",
                            modifier = Modifier.weight(1f),
                            clikeable = {
                                compartir_hosting_promo(
                                    id_user,
                                    it.nombre_tienda,
                                    it.categoria,
                                    context,
                                    it.localidad,
                                    it.id_tienda,
                                    indice_cruzado
                                )
                            }
                        )
                    }
                }

            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ZoomableGalleryFullScreen_promociones(
    estadisticas: EstadisticasPromo?,
    i: compartir_contacto_pulicaciones,
    titulo: String, txt: String,
    imagenes: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    clikc_compartir: (id: String, categoria: String, localidad: String, id_tienda: String) -> Unit,
    click_contacto_directo: (id: String, numero: String, localidad: String, id_tienda: String, categoria: String) -> Unit,
    abrir_prefil: (String) -> Unit,
) {
    val (valorRestante, tipo) = parseDiasHorasRestantes(i.dias_restantes)
    var layoutReady by remember { mutableStateOf(false) }

    val backgroundColor = when {
        tipo == "dias" -> when {
            valorRestante > 5 -> Color(0xFF15BB1A) // Verde
            valorRestante in 2..5 -> Color(0xFFFF9900) // Naranja
            valorRestante == 1 -> Color(0xFFEC1707) // Rojo
            else -> Color.Gray
        }

        tipo == "horas" -> when {
            valorRestante > 12 -> Color(0xFF15BB1A)
            valorRestante in 6..12 -> Color(0xFFFF9900)
            valorRestante in 1..5 -> Color(0xFFEC1707)
            else -> Color.Gray
        }

        else -> Color.Gray
    }
    val color_bottom = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.01f),
        Color.Black.copy(alpha = 0.25f),
        Color.Black.copy(alpha = 0.40f),
        Color.Black.copy(alpha = 0.65f), // arriba suave
        Color.Black.copy(alpha = 0.85f),  // abajo oscuro
        Color.Black.copy(alpha = 1f)
    )
    val color_top = listOf(
        Color.Black.copy(alpha = 1f),
        Color.Black.copy(alpha = 0.85f),
        Color.Black.copy(alpha = 0.65f),
        Color.Black.copy(alpha = 0.40f),
        Color.Black.copy(alpha = 0.25f),
        Color.Black.copy(alpha = 0.01f),
        Color.Transparent,
    )
    if (imagenes.isEmpty()) return
    val context = LocalContext.current

    val pagerState = com.google.accompanist.pager.rememberPagerState(initialPage = startIndex)
    var allowScroll by remember { mutableStateOf(true) }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = 25.dp, bottom = 10.dp)) {

        com.google.accompanist.pager.HorizontalPager(
            state = pagerState,
            count = imagenes.size,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (layoutReady) 1f else 0f)
                .onGloballyPositioned {
                    layoutReady = true
                },
            userScrollEnabled = allowScroll
        ) { page ->
  val zoomState = rememberZoomState()

            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagenes[page])
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build()
            )
            val state = painter.state
            ZoomImage(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
                contentScale = ContentScale.Fit
            )

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = color_top
                    )
                )
                .height(100.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                )

                // Texto centrado REAL
                if (imagenes.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} de ${imagenes.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Botón cerrar (X)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.End)
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = color_bottom
                        )
                    )
            )

            Column(modifier = Modifier.padding(10.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(
                            context
                        )
                            .data(i.logo_img)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                abrir_prefil(i.iod_tienda)
                            },
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = i.nombre_tienda.capitalizeFirst(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                spacer_vertical(10.dp)
                Text(
                    text = titulo.capitalizeFirst(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                spacer_vertical(5.dp)
                TextoExpandibleSuave(
                    texto = txt,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                spacer_vertical(5.dp)
                if (imagenes.size > 1) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PegasooPagerIndicator(
                            pageCount = imagenes.size,
                            currentPage = pagerState.currentPage,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        )
                    }
                }

                spacer_vertical(5.dp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${i.dias_restantes}",
                        fontSize = 12.sp,
                        color = backgroundColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!i.dias_restantes.equals("Expirado")) {
                        BotonCompartirReddit(
                            icon = R.drawable.icono_whatsapp_blanco_tasns,
                            descripcion = "contactados",
                            contador = "${estadisticas?.whatsapp ?: 0}",
                            onClick = {
                                click_contacto_directo(
                                    i.id_promocion,
                                    i.numero_contacto,
                                    i.localidad_tineda,
                                    i.iod_tienda, i.categoria
                                )
                            })
                        BotonCompartirReddit(
                            icon = R.drawable.comparir_icon,
                            descripcion = "compartidos",
                            contador = "${estadisticas?.compartidos ?: 0}",
                            onClick = {
                                clikc_compartir(
                                    i.id_promocion,
                                    i.categoria,
                                    i.localidad_tineda,
                                    i.iod_tienda
                                )
                            })
                    }
                }
            }
        }
    }

}


@Composable
fun BotonCompartirReddit(
    icon: Int,
    descripcion: String,
    contador: String = "4.4k",
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .background(Color.Black)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = descripcion,
            modifier = Modifier.size(16.dp)
        )
        AnimatedVisibility(!contador.equals("0")) {
            Text(
                text = contador,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}


@Composable
fun PegasooPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.35f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage

            val width by animateDpAsState(
                targetValue = if (isSelected) 18.dp else 6.dp,
                animationSpec = tween(durationMillis = 250),
                label = ""
            )

            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 250),
                label = ""
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun compartir_hosting_promo(
    id_user: String,
    nombre_tienda: String,
    categoria: String,
    context: Context,
    localidad_tienda: String,
    id_tienda: String,
    indice_cruazado: String
) {
    try {
        val localidad_pasada = when (localidad_tienda) {
            "barranca" -> "ba"
            "paramonga" -> "par"
            "pativilca" -> "pat"
            "supe" -> "su"
            "puerto supe" -> "pue"
            else -> localidad_tienda
        }
        val repo_erese_socio = repo_eres_socio()
        // Construimos el link de la Cloud Function

        val link =
            "https://geinzworkapp.web.app/share?" +
                    "t=p" +
                    "&id=${URLEncoder.encode(id_tienda, "UTF-8")}" +
                    "&l=$localidad_pasada" +
                    "&c=${URLEncoder.encode(categoria, "UTF-8")}" +
                    "&i=$indice_cruazado"


        val texto = "Mira lo que encontre en $nombre_tienda 👀🔥 \n$link"


        // Intent simple de compartir
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }

        // Abrimos el chooser para que el usuario seleccione la app
        context.startActivity(
            Intent.createChooser(intent, "Compartir con")
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        )
        repo_erese_socio.agregar_contador(
            "compartidos",
            id_tienda,
            localidad_tienda, id_user
        )
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir el lugar", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun btn_compartir(
    color: Color,
    icono: Int,
    txt_icono: String,
    modifier: Modifier,
    clikeable: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(color)
            .clickable {
                clikeable()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(icono),
                contentDescription = "",
                modifier = Modifier
                    .size(35.dp)
                    .padding(vertical = 5.dp)
            )
            texto_generico_one_line(
                txt_icono,
                modifier = Modifier.padding(vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GaleriaZoomablePanpf(
    imagenes: List<String>,
    startIndex: Int = 0
) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { imagenes.size })

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val zoomableState = rememberZoomableState() // <-- ZoomableState correcto

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ZoomImage(
                painter = rememberAsyncImagePainter(imagenes[page]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomableState),   // <-- usa zoomable, no zoom
                contentScale = ContentScale.Fit
            )
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ZoomableImagePagerItem(
    imageUrl: String,
    onZoomChange: (Float) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()

        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        val gestureModifier = Modifier.pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                val newScale = (scale * zoom).coerceIn(1f, 4f)
                onZoomChange(newScale)

                if (newScale > 1f) {
                    val extraWidth = (maxWidth * (newScale - 1)) / 2
                    val extraHeight = (maxHeight * (newScale - 1)) / 2
                    offsetX = (offsetX + pan.x).coerceIn(-extraWidth, extraWidth)
                    offsetY = (offsetY + pan.y).coerceIn(-extraHeight, extraHeight)
                } else {
                    offsetX = 0f
                    offsetY = 0f
                }

                scale = newScale
            }
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .then(gestureModifier)
        )
    }
}

@Composable
fun ShimmerImagenConMarca() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shimmer()
            .background(Color(0xFF1C1C1C)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.logo_geinz_500x500),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cargando",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

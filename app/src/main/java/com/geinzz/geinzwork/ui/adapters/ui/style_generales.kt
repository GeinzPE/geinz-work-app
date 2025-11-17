package com.geinzz.geinzwork.ui.adapters.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.compose.zoom.zoomable
import com.google.accompanist.pager.rememberPagerState

@Composable
fun CollageGoogleMapsStyle(
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
                    with = with,
                    imagenes = grupo,
                    onClickImagen = { url ->
                        indiceInicial = imagenes.indexOf(url)
                        galeriaActiva = true
                    }
                )
            }
        }

        if(galeriaActiva){
            ZoomableGalleryFullScreen(imagenes,indiceInicial, { galeriaActiva = false })
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

// ✅ Grupo de 3 imágenes dentro del collage
@Composable
fun GrupoCollageGoogle(
    aspectRatio: Float,
    with: Dp,
    imagenes: List<String>,
    onClickImagen: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .width(with)
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(12.dp))
    ) {
        ImagenCollage(
            url = imagenes.getOrNull(0),
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            listener_img = { url -> onClickImagen(url) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ImagenCollage(
                url = imagenes.getOrNull(1),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                listener_img = { url -> onClickImagen(url) }
            )
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

// ✅ Imagen individual dentro del collage
@Composable
fun ImagenCollage(
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
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ZoomableGalleryFullScreen(
    imagenes: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (imagenes.isEmpty()) return

    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { imagenes.size })
    var allowScroll by remember { mutableStateOf(true) }
    val zoomableState = rememberZoomableState() // <-- ZoomableState correcto

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Pager principal
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds(),
                    userScrollEnabled = allowScroll

                ) { page ->
                    ZoomImage(
                        painter = rememberAsyncImagePainter(imagenes[page]),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .zoomable(zoomableState),   // <-- usa zoomable, no zoom
                        contentScale = ContentScale.Fit
                    )
//                    ZoomableImagePagerItem(
//                        imageUrl = imagenes[page],
//                        onZoomChange = { zoom ->
//                            // Cuando el zoom vuelve al normal, reactiva scroll horizontal
//                            allowScroll = zoom <= 1.02f
//                        }
//                    )
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
            }
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

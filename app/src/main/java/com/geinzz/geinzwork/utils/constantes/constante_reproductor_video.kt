package com.geinzz.geinzwork.utils.constantes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object constantes_reprodutor_video {
    @Composable
    fun VideoPlayerWithControls(
        volumen_arriba: Boolean,
        url: String,
        isVisible: Boolean
    ) {
        val context = LocalContext.current

        var isPlaying by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }
        var progress by remember { mutableStateOf(0f) }
        var currentTime by remember { mutableStateOf(0L) }
        var durationTime by remember { mutableStateOf(0L) }
        var isEnded by remember { mutableStateOf(false) }
        var isMuted by remember { mutableStateOf(true) } // 🔇 inicia muteado

        var showForward by remember { mutableStateOf(false) }
        var showRewind by remember { mutableStateOf(false) }

        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()
                volume = 0f // 🔇 muteado desde el inicio

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isLoading = state == Player.STATE_BUFFERING
                        if (state == Player.STATE_ENDED) {
                            isEnded = true
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                })
            }
        }

        // 🔁 Cuando termina, replay automático muteado
        LaunchedEffect(isEnded) {
            if (isEnded) {
                delay(300) // pequeña pausa antes de reiniciar
                isMuted = true
                exoPlayer.volume = 0f
                exoPlayer.seekTo(0)
                exoPlayer.play()
                isEnded = false
            }
        }

        val coroutineScope = rememberCoroutineScope()

        fun seekForward() {
            exoPlayer.seekTo((exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration))
            showForward = true
            coroutineScope.launch { delay(800); showForward = false }
        }

        fun seekRewind() {
            exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0))
            showRewind = true
            coroutineScope.launch { delay(800); showRewind = false }
        }

        LaunchedEffect(isVisible) {
            if (isVisible) exoPlayer.play() else exoPlayer.pause()
        }

        LaunchedEffect(Unit) {
            while (true) {
                val duration = exoPlayer.duration
                val current = exoPlayer.currentPosition

                delay(300)
            }
        }

        DisposableEffect(Unit) {
            onDispose { exoPlayer.release() }
        }

        var boxWidth by remember { mutableStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged { boxWidth = it.width }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            if (boxWidth > 0) {
                                if (offset.x > boxWidth / 2) seekForward()
                                else seekRewind()
                            }
                        },
                        onTap = {
                            // tap normal solo play/pause, el replay es automático
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }
                    )
                }
        ) {
            // 🎬 Video
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        useController = false
                    }
                }
            )

            // 🔄 Loader
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            // ▶ Icono play
            if (!isPlaying && !isLoading) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(70.dp)
                )
            }

            // 🔇 Botón Mute
            val posicion = if (!volumen_arriba) Alignment.TopEnd else Alignment.BottomEnd
            val padding = if (!volumen_arriba) {
                PaddingValues(top = 8.dp, end = 5.dp)
            } else {
                PaddingValues(bottom = 50.dp, end = 5.dp)
            }

            Box(
                modifier = Modifier
                    .align(posicion)
                    .padding(padding)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        isMuted = !isMuted
                        exoPlayer.volume = if (isMuted) 0f else 1f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }


            // ⏪ Overlay rewind
            AnimatedVisibility(
                visible = showRewind,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FastRewind,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("10 seg", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // ⏩ Overlay forward
            AnimatedVisibility(
                visible = showForward,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FastForward,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("10 seg", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    @Composable
    fun GaleriaHorizontalInstagram_mas_video_info_inmobiliaria(
        alturaAnimada: Dp,
        isFullscreen: Boolean,
        imagenes: List<String>,
        videoUrl: String?,
        modifier: Modifier = Modifier,
        img_clikeble_valor: (Int) -> Unit,
        long_listatener: () -> Int,
        es_completo: (Boolean) -> Unit
    ) {
        val totalItems = imagenes.size + if (videoUrl != null) 1 else 0
        val pagerState = rememberPagerState { totalItems }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(alturaAnimada)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->

                val isLastItem = videoUrl != null && page == totalItems - 1
                val isVisible = pagerState.currentPage == page

                if (isLastItem) {
                    VideoPlayerWithControls(
                        volumen_arriba = false,
                        url = videoUrl!!,
                        isVisible = isVisible
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = imagenes[page],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { img_clikeble_valor(page) },
                                    onLongClick = { long_listatener() }
                                ),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.cargando_img_categorias),
                            error = painterResource(R.drawable.cargando_img_categorias)
                        )
                    }
                }
            }

            // ✅ Botón fullscreen FUERA del pager — se crea una sola vez, siempre en TopStart
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp, start = 8.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { es_completo(!isFullscreen) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Indicador 1/5
            if (totalItems > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 50.dp, end = 8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/$totalItems",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }


    @Composable
    fun GaleriaHorizontalInstagram_mas_video_ui_inmobiliaria(
        link_video: String,
        imagenes: List<String>,
        modifier: Modifier = Modifier,
        img_clikeble_valor: (Int) -> Unit,
        long_listatener: () -> Int
    ) {
        val totalPages = imagenes.size + 1 // 🎬 +1 para el video al final
        val pagerState = rememberPagerState { totalPages }
        val isOnVideoPage = pagerState.currentPage == imagenes.size // última página

        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                if (page == imagenes.size) {
                    // 🎬 Última página = Video
                    VideoPlayerWithControls(
                        true,
                        url = link_video,
                        isVisible = isOnVideoPage
                    )
                } else {
                    // 🖼 Páginas normales = Imágenes
                    AsyncImage(
                        model = imagenes[page],
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { img_clikeble_valor(page) },
                                onLongClick = { long_listatener() }
                            ),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.cargando_img_categorias),
                        error = painterResource(R.drawable.cargando_img_categorias)
                    )
                }
            }

            // Indicador 1/5 — muestra ▶ cuando es la página del video
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (isOnVideoPage) {
                    // 🎬 Ícono video en última página
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${pagerState.currentPage + 1}/$totalPages",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    // 🖼 Contador normal en imágenes
                    Text(
                        text = "${pagerState.currentPage + 1}/$totalPages",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

    @Composable
    fun GaleriaHorizontalInstagram_promociones_solo_imagen(
        imagenes: List<String>,
        modifier: Modifier = Modifier,
        img_clikeble_valor: (Int) -> Unit,
        long_listatener: () -> Int
    ) {
        val pagerState = rememberPagerState { imagenes.size }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                AsyncImage(
                    model = imagenes[page],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            indication = null, // opcional (sin ripple)
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                img_clikeble_valor(page)
                            },
                            onLongClick = {
                                long_listatener()
                            }),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.cargando_img_categorias),
                    error = painterResource(R.drawable.cargando_img_categorias)
                )
            }

            // Indicador 1/5
            if (imagenes.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${imagenes.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}


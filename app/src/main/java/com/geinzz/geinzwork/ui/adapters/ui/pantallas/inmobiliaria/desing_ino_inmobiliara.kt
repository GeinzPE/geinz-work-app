package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.completeta_info_inmuebles
import com.geinzz.geinzwork.data.model.ia_inmobiliara_tts
import com.geinzz.geinzwork.data.model.lista_lugaers_totales
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.data.model.perfiles_negocios
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TypewriterTexto
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.viewModels.tts_stt.tts_stt
import com.geinzz.geinzwork.viewModels.viewmodel_inmobiliaria
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Composable
fun ListaHorizontal(
    tipo_lanzado: String,
    titulo: String,
    texto: String,
    lista: List<lugares_cercanos_>,
    clik_carta: (id: String, localida: String, img: String, nombre: String, lat: Double, lng: Double) -> Unit
) {

    val anchos_definidos = when (tipo_lanzado) {
        "salud" -> {
            150.dp
        }

        "cercanos" -> {
            150.dp
        }

        "turisticos" -> {
            300.dp
        }

        "servicos" -> {
            150.dp
        }

        else -> {
            150.dp
        }
    }
    val altos_definidos = when (tipo_lanzado) {
        "salud" -> {
            150.dp
        }

        "cercanos" -> {
            150.dp
        }

        "turisticos" -> {
            200.dp
        }

        "servicos" -> {
            150.dp
        }

        else -> {
            150.dp
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        texto_generico_one_line(titulo)
        texto_generico_multilinea(texto, style = MaterialTheme.typography.labelSmall)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(lista) { i ->
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

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .height(altos_definidos)
                            .width(anchos_definidos)
                    ) {
                        AsyncImage(
                            model = i.img_String,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .height(altos_definidos)
                                .width(anchos_definidos)
                                .clickable {
                                    clik_carta(
                                        i.id,
                                        i.localidad,
                                        i.img_String,
                                        i.nombre,
                                        i.lat,
                                        i.lng
                                    )
                                },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.cargando_img_categorias),
                            error = painterResource(R.drawable.cargando_img_categorias)
                        )
                        Column(
                            modifier = Modifier

                                .align(Alignment.BottomCenter)

                        ) {
                            val metros = (i.distanciaKm * 1000).toInt()
                            val texto = if (metros >= 1000) "1km" else "${metros}m"
                            texto_generico_one_line(
                                "A solo ${texto}",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            spacer_vertical(10.dp)
                        }


                    }


                }

            }
        }
    }

}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun seleccion_tipo_persona(
    i: perfiles_negocios,
    seleccionado: Boolean,
    click: (String, nombre: String) -> Unit
) {
    val context = LocalContext.current
    var reproduciendo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var exoPlayerState by remember { mutableStateOf<ExoPlayer?>(null) }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (seleccionado) 0f else 0.55f,
        animationSpec = tween(600),
        label = "overlay"
    )
    val gradientAlpha by animateFloatAsState(
        targetValue = if (seleccionado) 1f else 0f,
        animationSpec = tween(600),
        label = "gradient"
    )
    val videoAlpha by animateFloatAsState(
        targetValue = if (reproduciendo) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "videoAlpha"
    )

    LaunchedEffect(seleccionado) {
        if (!seleccionado) {
            reproduciendo = false
            exoPlayerState?.pause()
            exoPlayerState?.seekTo(0)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayerState?.stop()
            exoPlayerState?.release()
            exoPlayerState = null
        }
    }

    val player = exoPlayerState

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .width(100.dp)
            .height(140.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                click(i.txt, i.nombre_personas)
                scope.launch {
                    val videoRes = when (i.txt) {
                        "Inversionista" -> R.raw.pablo_asistente_video
                        "Familiar"      -> R.raw.kaori_asistente_video
                        "Solitario"     -> R.raw.luis_asistente_video
                        else            -> R.raw.pablo_asistente_video
                    }

                    val activePlayer = if (exoPlayerState == null) {
                        val newPlayer = ExoPlayer.Builder(context).build().apply {
                            val uri = Uri.parse("android.resource://${context.packageName}/$videoRes")
                            setMediaItem(MediaItem.fromUri(uri))
                            prepare()
                            playWhenReady = false
                        }

                        newPlayer.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_ENDED) {
                                    // ✅ 1. Primero pausar — evita que renderice el frame 0
                                    newPlayer.pause()
                                    // ✅ 2. Fade-out del video ANTES de hacer seek
                                    reproduciendo = false
                                    // ✅ 3. Seek al inicio DESPUÉS del fade-out (700ms del tween)
                                    scope.launch {
                                        delay(750) // esperar que videoAlpha llegue a 0
                                        newPlayer.seekTo(0)
                                    }
                                }
                            }
                        })

                        exoPlayerState = newPlayer
                        newPlayer
                    } else {
                        exoPlayerState!!
                    }

                    activePlayer.seekTo(0)
                    activePlayer.play()
                    reproduciendo = true
                }
            }
    ) {
        Image(
            painter = painterResource(i.imagen),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null
        )

        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { view ->
                    view.player = player
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .alpha(videoAlpha)
            )
        }

        if (videoAlpha < 0.99f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha * (1f - videoAlpha)))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f * gradientAlpha)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = i.txt, color = Color.White, style = MaterialTheme.typography.labelSmall)
            Text(text = i.nombre_personas, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun GeminiBlobBackground_contexto(
    persona:String,
    expandido: Boolean,
    viewmodel_tts: tts_stt,
    respuesta_gemini_para_tts: viewmodel_inmobiliaria.estado_carga_respuesta_con_IA,
    filtro_seleccionado: String,
    desespandir: (Boolean) -> Unit,
) {
    var respuesta_IA_cargada by remember { mutableStateOf(false) }

    val nombreAMostrar = when (filtro_seleccionado) {
        "Inversionista" -> "Pablo"
        "Familiar" -> "Kaori"
        "Solitario" -> "Luis"
        else -> "Cliente"
    }
    val imagen_perfil_asistente =
        when (filtro_seleccionado) {
            "Inversionista" -> R.drawable.pablo_asistente
            "Familiar" -> R.drawable.naomi_asistente
            "Solitario" -> R.drawable.luis_asistente
            else -> R.drawable.naomi_asistente
        }
    AnimatedVisibility(filtro_seleccionado.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(400, easing = EaseInOutCubic))
                .then(
                    if (expandido) Modifier.wrapContentHeight()
                    else Modifier.height(170.dp)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    desespandir(!expandido)
//                    expandido = !expandido
                }
                .clipToBounds()
        ) {

            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .then(
                        if (!expandido) Modifier.heightIn(max = 170.dp) else Modifier
                    )
            ) {

                when (respuesta_gemini_para_tts) {
                    is viewmodel_inmobiliaria.estado_carga_respuesta_con_IA.error -> {}
                    viewmodel_inmobiliaria.estado_carga_respuesta_con_IA.idle -> {}
                    viewmodel_inmobiliaria.estado_carga_respuesta_con_IA.loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line("$persona esta generando lo mejor respuesta para ti" , color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    is viewmodel_inmobiliaria.estado_carga_respuesta_con_IA.succes -> {
                        var respuesta_gemini =
                            (respuesta_gemini_para_tts as viewmodel_inmobiliaria.estado_carga_respuesta_con_IA.succes).texto

                        // ✅ Guarda qué texto ya fue enviado al TTS
                        var textoYaReproducido by remember { mutableStateOf("") }
                        LaunchedEffect(respuesta_gemini) {
                            Log.d("respuesta_gemini", "${respuesta_gemini}")
                            if (respuesta_gemini.isNotEmpty() && respuesta_gemini != textoYaReproducido) {
                                textoYaReproducido = respuesta_gemini
                                val tipo_voz = when (filtro_seleccionado) {

                                    "Inversionista" -> {
                                        "es-US-Polyglot-1"
                                    }

                                    "Familiar" -> {
                                        "es-US-News-F"
                                    }

                                    "Solitario" -> {
                                        "es-US-Neural2-B"
                                    }

                                    else -> {
                                        "es-US-News-F"
                                    }
                                }
                                viewmodel_tts.crear_texto__para_tts(respuesta_gemini, tipo_voz)
                            }

                        }
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = imagen_perfil_asistente),
                                    contentDescription = "Logo IA",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {

                                        }
                                        .size(40.dp)
                                )
                                spacer_horizonta(5.dp)

                                Text(
                                    text = "$nombreAMostrar asistente de ventas",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = if (expandido) "▲ ver menos" else "▼ ver más",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                )
                            }
                            TypewriterTexto(respuesta_gemini)
                        }


                    }
                }

            }


        }
    }
}

@Composable
fun GeminiBlobBackground(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF8B5CF6), // purple
        Color(0xFF3B82F6), // blue
        Color(0xFF06B6D4), // cyan
        Color(0xFFEC4899), // pink
    )
) {
    data class Blob(
        val xAnim: Animatable<Float, AnimationVector1D>,
        val yAnim: Animatable<Float, AnimationVector1D>,
        val scaleAnim: Animatable<Float, AnimationVector1D>,
        val color: Color
    )

    val blobs = remember {
        colors.mapIndexed { i, color ->
            Blob(
                xAnim = Animatable(0.2f + (i * 0.2f)),
                yAnim = Animatable(0.2f + (i * 0.15f)),
                scaleAnim = Animatable(0.6f),
                color = color
            )
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            blobs.forEach { blob ->
                launch {
                    while (isPlaying) {
                        val tx = (0.1f + Math.random() * 0.8f).toFloat()
                        val ty = (0.1f + Math.random() * 0.8f).toFloat()
                        val ts = (0.5f + Math.random() * 0.6f).toFloat()
                        val dur = (1800 + Math.random() * 1400).toInt()

                        launch {
                            blob.xAnim.animateTo(tx, tween(dur, easing = EaseInOutCubic))
                        }
                        launch {
                            blob.yAnim.animateTo(ty, tween(dur, easing = EaseInOutCubic))
                        }
                        blob.scaleAnim.animateTo(ts, tween(dur, easing = EaseInOutCubic))
                    }
                }
            }
        } else {
            // vuelven a posición inicial suavemente
            blobs.forEachIndexed { i, blob ->
                launch {
                    blob.xAnim.animateTo(0.2f + (i * 0.2f), tween(1200, easing = EaseInOutCubic))
                    blob.yAnim.animateTo(0.2f + (i * 0.15f), tween(1200, easing = EaseInOutCubic))
                    blob.scaleAnim.animateTo(0.6f, tween(1200, easing = EaseInOutCubic))
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val blobRadius = size.minDimension * 0.75f

        blobs.forEach { blob ->
            val cx = blob.xAnim.value * size.width
            val cy = blob.yAnim.value * size.height
            val radius = blobRadius * blob.scaleAnim.value
            val c = blob.color

            // capa principal — color en el centro, se desvanece hacia los bordes
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to c.copy(alpha = 0.30f),
                        0.4f to c.copy(alpha = 0.20f),
                        0.7f to c.copy(alpha = 0.08f),
                        1.0f to c.copy(alpha = 0.0f)
                    ),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )

            // halo exterior más grande y muy suave
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to c.copy(alpha = 0.10f),
                        0.5f to c.copy(alpha = 0.05f),
                        1.0f to c.copy(alpha = 0.0f)
                    ),
                    center = Offset(cx, cy),
                    radius = radius * 1.6f
                ),
                radius = radius * 1.6f,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun desing_style_circular(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                color
            )
    ) {
        texto_generico_one_line(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun filtar_datos(
    viewModel: viewmodel_inmobiliaria,
    radioEnKm: Double,
    datos: completeta_info_inmuebles,
    nombre_user: String,
    lista_lugares_cercanos_filtrada: lista_lugaers_totales,
    tipo_select: String
) {

    viewModel.filtrar_por_radio_Cercania(radioEnKm)
    val seguros_filtrados =
        viewModel.lista_lugares_seguros_filtrada.value
    val cercanos_filtrados =
        viewModel.lista_lugares_cercanos_filtrada.value
    val turisticos_filtrados =
        viewModel.lista_lugares_turisticos_filtrada.value

    val obj = ia_inmobiliara_tts(
        cantidad_lugares_seguros = seguros_filtrados.size,   // ← filtrado
        cantidad_lugares_encontrado = cercanos_filtrados.size,  // ← filtrado
        cantidad_lugares_turisticos = turisticos_filtrados.size,// ← filtrado
        metros_cuadrados = datos.metros.toString(),
        tipo = datos.tipoPropiedad,
        estado = datos.tipoOperacion,
        nombre_user = nombre_user,
        lista_lugares_cercanos = lista_lugares_cercanos_filtrada.listalugares_cercanos,
        lista_lugares_seguros = lista_lugares_cercanos_filtrada.lista_servicios_sercanos,
        lista_lugares_turisticos = lista_lugares_cercanos_filtrada.llissa_lugareS_turistos,
        tipo_seleccionado = tipo_select,
        calle_ubicada = datos.direccion,
    )
    viewModel.respuesta_gemini_(obj, tipo_select)

}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.completeta_info_inmuebles
import com.geinzz.geinzwork.data.model.ia_inmobiliara_tts
import com.geinzz.geinzwork.data.model.lista_lugaers_totales
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TypewriterTexto
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.tts_stt.tts_stt
import com.geinzz.geinzwork.viewModels.viewmodel_inmobiliaria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ui_info_imobiliara(
    viewModel: viewmodel_inmobiliaria,
    id: String,
    localidad: String,
    nombre_user: String
) {
    val context = LocalContext.current
    val viewmodel_tts: tts_stt = viewModel()
    var nueva_busqueda by remember { mutableStateOf(10.0f) }
    val estado by viewModel.estado_carga_info_inmuebles.collectAsState()

    // Reemplaza el acceso directo a datos.xxx por los StateFlow filtrados
    val seguros by viewModel.lista_lugares_seguros_filtrada.collectAsState()
    val cercanos by viewModel.lista_lugares_cercanos_filtrada.collectAsState()
    val turisticos by viewModel.lista_lugares_turisticos_filtrada.collectAsState()
    val servicios by viewModel.lista_lugares_servicios_filtrada.collectAsState()


    var datos_Estados_succes by remember { mutableStateOf(completeta_info_inmuebles()) }
    var filtro_seleccionado by remember { mutableStateOf("") }

    val lista_lugares_cercanos_filtrada by viewModel.lugares_filtrados.collectAsState()

    val respuesta_gemini by viewModel.respuesta_IA.collectAsState()

    val datos_cloud_TTs by viewmodel_tts.datosCloudTts.collectAsState()
    var respues_parseo_gemini by remember { mutableStateOf("") }


    val isPlaying by viewmodel_tts.isPlaying.collectAsState()

    val lista_perfil = listOf("inversionista", "familiar", "solitario")
    val scope = rememberCoroutineScope()


    LaunchedEffect(datos_cloud_TTs) {
        Log.d("datos_cloud_TTs", "$datos_cloud_TTs")
        if (datos_cloud_TTs.isNotEmpty()) {
            viewmodel_tts.reproducirMP3(context, datos_cloud_TTs)
            viewmodel_tts.limpiarAudio()
        }
    }

    LaunchedEffect(respuesta_gemini) {
        Log.d("respuesta_gemini", "${respuesta_gemini}")
        if (respuesta_gemini.isNotEmpty()) {
            respues_parseo_gemini = respuesta_gemini
            val tipo_voz = when (filtro_seleccionado) {

                "inversionista" -> {
                    "es-US-Polyglot-1"
                }

                "familiar" -> {
                    "es-US-News-F"
                }

                "solitario" -> {
                    "es-US-Neural2-B"
                }

                else -> {
                    "es-US-News-F"
                }
            }
            viewmodel_tts.crear_texto__para_tts(respuesta_gemini, tipo_voz)
        }

    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiar_estado_info()
            viewModel.limpiar_listas()
            viewmodel_tts.detenerAudio()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.cargarDatos(id, localidad)
    }

    LaunchedEffect(datos_Estados_succes) {
        viewModel.guardar_datosListas(
            datos_Estados_succes.cantidad_lugares_seguros,
            datos_Estados_succes.listalugares_cercanos,
            datos_Estados_succes.llissa_lugareS_turistos,
            datos_Estados_succes.lista_servicios_sercanos
        )
    }


    LaunchedEffect(filtro_seleccionado, cercanos) {
        val lista_general = viewModel.obtener_negocios_para_perfil(
            filtro_seleccionado,
            cercanos,
            seguros,
            turisticos
        )
        Log.d("lsitaobtenid", "$lista_general")
    }

    when (estado) {

        is viewmodel_inmobiliaria.etado_carga_info_inmuebles.idle -> {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        }

        is viewmodel_inmobiliaria.etado_carga_info_inmuebles.error -> {

            val error = (estado as viewmodel_inmobiliaria.etado_carga_info_inmuebles.error)

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error.txt)
            }

        }

        is viewmodel_inmobiliaria.etado_carga_info_inmuebles.succes -> {

            var datos = (estado as viewmodel_inmobiliaria.etado_carga_info_inmuebles.succes).datos
            datos_Estados_succes = datos

            LazyColumn() {
                item {
                    Box {

                        GaleriaHorizontalInstagram(
                            datos.listaImg,
                            modifier = Modifier,
                            { },
                            {
                                Log.d("LONG_PRESS", "Long press en la galería")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(Color.Black)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    texto_generico_one_line(datos.nombre)

                    texto_generico_one_line("${datos.distrito} / Lima")

                    texto_generico_one_line("selecciona tu perfil de persona")
                    texto_generico_one_line("Deja que la ia de geinz te ayude a ubicarte mas rapdio")

                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(lista_perfil) { i ->

                            seleccion_tipo_persona(
                                tipo = i,
                                seleccionado = filtro_seleccionado == i
                            ) { tipo_select ->

                                filtro_seleccionado = tipo_select
                                nueva_busqueda = 5.0f
                                val radioEnKm = nueva_busqueda.toDouble() / 10.0

                                scope.launch {
                                    filtar_datos(
                                        viewModel,
                                        radioEnKm,
                                        datos,
                                        nombre_user,
                                        lista_lugares_cercanos_filtrada,
                                        tipo_select
                                    )
                                }
                            }
                        }
                    }


                    GeminiBlobBackground_contexto(
                        filtro_seleccionado,
                        isPlaying = isPlaying,
                        texto = respues_parseo_gemini,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )



                    texto_generico_one_line(
                        "Trato : ${datos.tipoOperacion}",
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    texto_generico_one_line(
                        "Tipo : ${datos.tipoPropiedad}",
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    Column(Modifier.animateContentSize()) {
                        Slider(
                            value = nueva_busqueda,
                            onValueChange = { nueva_busqueda = it.roundToInt().toFloat() },
                            valueRange = 1f..10f,
                            steps = 8,
                            onValueChangeFinished = {
                                val radioEnKm = nueva_busqueda.toDouble() / 10.0
                                viewModel.filtrar_por_radio_Cercania(radioEnKm)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,       // 🔹 Color del "thumb" o bolita que se mueve cuando arrastras el slider
                                activeTrackColor = MaterialTheme.colorScheme.primary, // 🔹 Color de la línea activa del slider (la parte a la izquierda del thumb)
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.2f
                                ),
                                activeTickColor = MaterialTheme.colorScheme.primary,  // 🔹 Color de las marcas de pasos (ticks) que ya están "alcanzadas" por el thumb
                                inactiveTickColor = Color.Gray                        // 🔹 Color de las marcas de pasos que aún no se alcanzaron
                            ),
                            thumb = {
                                // Nuestra bolita blanca sin borde negro
                                Box(
                                    modifier = Modifier
                                        .size(25.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val metros = (nueva_busqueda * 100).toInt() // 1=100m, 10=1000m
                                    val texto = if (metros >= 1000) "1km" else "${metros}m"
                                    texto_generico_one_line(
                                        texto,
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        )

                        ListaHorizontal(seguros)

                        ListaHorizontal(cercanos)

                        ListaHorizontal(turisticos)

                        ListaHorizontal(servicios)

                    }

                    val icon_bano = R.drawable.icono_bano
                    val icon_dormitorio = R.drawable.icono_dormitorio
                    val icono_cochera = R.drawable.icono_nochera
                    val icon_regla = R.drawable.icono_regla

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        ItemIcono(icon_regla, "${datos.metros} m²")

                        ItemIcono(
                            icon_dormitorio,
                            "${datos.habitaciones} dorm."
                        )

                        ItemIcono(
                            icon_bano,
                            "${datos.banos} baños."
                        )

                        ItemIcono(
                            icono_cochera,
                            "${datos.estacionamientos} estac."
                        )

                    }
                }


            }

        }

    }
}

@Composable
fun GaleriaHorizontalInstagram(
    imagenes: List<String>,
    modifier: Modifier = Modifier,
    img_clikeble_valor: (Int) -> Unit,
    long_listatener: () -> Int
) {
    val pagerState = rememberPagerState { imagenes.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)

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
                        indication = null,
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
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 50.dp, end = 8.dp)
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

@Composable
fun ListaHorizontal(lista: List<lugares_cercanos_>) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            texto_generico_one_line("encontrados ${lista.size}")
        }
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

                Row(verticalAlignment = Alignment.CenterVertically) {

                    AsyncImage(
                        model = i.img_String,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Crop
                    )

                    val distanciaTexto = when {
                        i.distanciaKm < 1.0 -> "${(i.distanciaKm * 1000).toInt()}m"
                        else -> "${"%.1f".format(i.distanciaKm)}km"
                    }

                    texto_generico_one_line(
                        distanciaTexto,
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                }
            }

        }
    }

}


fun normalizarNombre(nombre: String): String {
    return nombre
        .lowercase()
        .replace(Regex("[^a-záéíóúñ ]"), "")
        .split(" ")
        .first()
}

@Composable
fun seleccion_tipo_persona(
    tipo: String,
    seleccionado: Boolean,
    click: (String) -> Unit
) {

    val containerColor by animateColorAsState(
        targetValue = if (seleccionado)
            Color.White
        else
            MaterialTheme.colorScheme.primary,
        label = "btnColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (seleccionado)
            Color.Black
        else
            Color.White,
        label = "textColor"
    )

    Button(
        onClick = { click(tipo) },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(50.dp)
    ) {
        texto_generico_one_line(
            tipo,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun GeminiBlobBackground_contexto(
    filtro_seleccionado: String,
    isPlaying: Boolean,
    texto: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF7D49EE),
        Color(0xFF2354A6),
        Color(0xFF046070),
        Color(0xFF9A175C),
    )
) {

    var expandido by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .animateContentSize(animationSpec = tween(400, easing = EaseInOutCubic))
            .then(
                if (expandido) Modifier.wrapContentHeight()
                else Modifier.height(200.dp)
            )
            .clickable { expandido = !expandido }
            .clipToBounds() // ← corta todo lo que desborde los 200dp
    ) {
        GeminiBlobBackground(
            isPlaying = isPlaying,
            colors = colors,
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .matchParentSize()
        )

        Column(
            modifier = Modifier
                .padding(12.dp)
                .then(
                    if (!expandido) Modifier.heightIn(max = 200.dp) else Modifier
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_geinz_500x500),
                    contentDescription = "Logo IA",
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {

                        }
                        .size(35.dp)
                )
                spacer_horizonta(5.dp)
                val nombreAMostrar = when (filtro_seleccionado) {
                    "inversionista" -> "Pablo"
                    "familiar" -> "Naomi"
                    "solitario" -> "Luis"
                    else -> "Cliente" // valor por defecto si no coincide
                }
//                Column() {

                    Text(
                        text = "$nombreAMostrar asistente de ventas",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
//                    Text(
//                        text = "House capital Group",
//                        fontSize = 13.sp, // ojo, en Text se usa sp para el tamaño, no dp
//                        color = Color.Gray
//                    )
//                }
                Text(
                    text = if (expandido) "▲ ver menos" else "▼ ver más",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
            TypewriterTexto(texto)


        }

        if (!expandido) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 80f
                        )
                    )
            )
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
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria


//import android.graphics.Point
import android.os.Build
import androidx.media3.common.MediaItem
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.BuildConfig
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.completeta_info_inmuebles
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dialog_seguridad_salud_algolia
import com.geinzz.geinzwork.data.model.ia_inmobiliara_tts
import com.geinzz.geinzwork.data.model.lista_lugaers_totales
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TypewriterTexto
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_salud_seguridad_algolia
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_servicios_tramite
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.formatearNumero
import com.geinzz.geinzwork.ui.adapters.ui.principal.texFiel_fake
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.house_capital_whatsap
import com.geinzz.geinzwork.viewModels.tts_stt.tts_stt
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite
import com.geinzz.geinzwork.viewModels.viewmodel_inmobiliaria
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.google.android.gms.maps.MapView
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Properties
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.geinzz.geinzwork.data.model.datos_compartidos_lugares_cercacnos
import com.geinzz.geinzwork.data.model.datos_viewmodel_inmobiliara
import com.geinzz.geinzwork.data.model.perfiles_negocios
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca_logo
import com.geinzz.geinzwork.utils.constantes.constantes_reprodutor_video.GaleriaHorizontalInstagram_mas_video_info_inmobiliaria
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_inmobiliara
import kotlin.String

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ui_info_imobiliara(
    viewmodel_mapa_inmobilia: viewmodel_mapa_inmobiliara,
    viewmodelMapa: viewmodel_mapa_personalizado,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos,
    verificar_inter: Boolean,
    viewModel: viewmodel_inmobiliaria,
    id: String,
    localidad: String,
    nombre_user: String, iniciar_seccion: () -> Unit, crear_cuenta: () -> Unit,
    abrir_mapa: (tipo: String, img: String, lat: Double, lng: Double) -> Unit,
    abrir_mapa_ver_cercanos: () -> Unit
) {
    val context = LocalContext.current
    val viewmode_servicios_tramite: viewmode_servicios_tramite = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()


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
    val datos_numeros_salud_seguridad by viewModelFiltros.instance_salud_seguridad.collectAsState()


    val datos_cloud_TTs by viewmodel_tts.datosCloudTts.collectAsState()

    val icon_bano = R.drawable.icono_bano
    val icon_dormitorio = R.drawable.icono_dormitorio
    val icono_cochera = R.drawable.icono_nochera
    val icon_regla = R.drawable.icono_regla
    val icono_profuncidad = R.drawable.flecha_vertical
    val icon_ancho = R.drawable.flecha_orizontal
    val isPlaying by viewmodel_tts.isPlaying.collectAsState()


    val respuesta_gemini_para_tts by viewModel.respuesta_IA_datos.collectAsState()

    var nombre_seguridad_salud by remember { mutableStateOf("") }
    var img_seguirdad_salud by remember { mutableStateOf("") }
    val lista_perfil = listOf(
        perfiles_negocios("Inversionista", R.drawable.pablo_asistente, "Pablo"),
        perfiles_negocios("Familiar", R.drawable.naomi_asistente, "Kaori"),
        perfiles_negocios("Solitario", R.drawable.luis_asistente, "Luis")
    )
    val scope = rememberCoroutineScope()
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()

    var show_bottom_sheeet by remember { mutableStateOf(false) }
    val bottomSheetVisible by viewmodelMapa.estadoBottomSheet.collectAsState()

    var localidad_tienda_seleccionada by remember { mutableStateOf("") }
    var id_tienda_selecionada by remember { mutableStateOf("") }

    var dialog_servicos_tramite by remember { mutableStateOf(false) }
    var id_tienda_select by remember { mutableStateOf("") }
    var localidad_tienda by remember { mutableStateOf("") }
    var iduser by remember { mutableStateOf("") }
    var localida by remember { mutableStateOf("") }
    var aler_dialog_contacto by remember { mutableStateOf(false) }
    var localidad_seguridad_salud by remember { mutableStateOf("") }
    var id_seguridad_salud by remember { mutableStateOf("") }

    var img_turismo by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    val listState = rememberLazyListState()
    val stickyHeaderIndex = 1
    var toastShown by remember { mutableStateOf(false) }
    val paddingAnim by animateDpAsState(
        targetValue = if (toastShown) 5.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
//    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
//        if (listState.firstVisibleItemIndex >= stickyHeaderIndex && !toastShown) {
//            toastShown = true
//        } else if (listState.firstVisibleItemIndex < stickyHeaderIndex) {
//            toastShown = false
//        }
//    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada = datosTienda!!.first()
//            viewModelFiltros.cast_horario_atencion_horario_tienda(datosTienda!!.first().horario_atencion)
        }
    }


    LaunchedEffect(datos_cloud_TTs) {
        Log.d("datos_cloud_TTs", "$datos_cloud_TTs")
        if (datos_cloud_TTs.isNotEmpty()) {
            viewmodel_tts.reproducirMP3(context, datos_cloud_TTs)
            viewmodel_tts.limpiarAudio()
        }
    }


    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localidad_tienda_seleccionada,
                id_tienda_selecionada
            )
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

    LaunchedEffect(aler_dialog_contacto) {
        if (aler_dialog_contacto) {
            viewModelFiltros.obtener_numeros_seguridad_salud(
                localidad_seguridad_salud,
                id_seguridad_salud
            )
        }
    }

    val mostrarColumna by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex >= 3 // item index 3 = el 4to item
        }

    }

    var isFullscreen by rememberSaveable { mutableStateOf(false) } // 🔲 estado pantalla completa
    val altura by animateDpAsState(
        targetValue = if (isFullscreen) LocalConfiguration.current.screenHeightDp.dp else 550.dp,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "altura_galeria"
    )
    val cargando_data = estado is viewmodel_inmobiliaria.etado_carga_info_inmuebles.loading
            || estado == viewmodel_inmobiliaria.etado_carga_info_inmuebles.idle

    Box(modifier = Modifier.fillMaxSize()) {
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
                if (!cargando_data) {

                    var datos =
                        (estado as viewmodel_inmobiliaria.etado_carga_info_inmuebles.succes).datos
                    datos_Estados_succes = datos

                    LazyColumn(
                        modifier = Modifier
                            .padding(5.dp)
                            .animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        state = listState,
                    ) {
                        item(key = "galeria") {
                            Box {
                                GaleriaHorizontalInstagram_mas_video_info_inmobiliaria(
                                    altura,
                                    isFullscreen,
                                    imagenes = datos.listaImg,
                                    videoUrl = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/videos_prueva%2FDownload.mp4?alt=media&token=0bf1f262-a5e2-46aa-b807-7c82c11333e4",
                                    modifier = Modifier,
                                    img_clikeble_valor = { },
                                    long_listatener = {
                                        Log.d("LONG_PRESS", "Long press en la galería")
                                    },
                                    es_completo = { completo ->
                                        isFullscreen = completo
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                        .background(Color.Black)
                                        .align(Alignment.BottomCenter)
                                )
                            }
                        }

                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(15.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Row() {
                                    Text(
                                        datos.nombre.capitalizeFirst(),
                                        fontFamily = baners_geinz_work,
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                viewModel.compartir_link_tienda(
                                                    context,
                                                    datos.distrito,
                                                    datos.id,
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    spacer_horizonta(5.dp)
                                }

                                texto_generico_multilinea(
                                    datos.descripcion.capitalizeFirst(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                LazyRow(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    item {
                                        desing_style_circular(
                                            "Ubicado en ${datos.distrito} / Lima".capitalizeFirst(),
                                            Color(0xFF065D07).copy(alpha = 0.25f)
                                        )
                                    }

                                    item {
                                        desing_style_circular(
                                            "Trato : ${datos.tipoOperacion.capitalizeFirst()}",
                                            Color(0xFF0738BD).copy(alpha = 0.25f)
                                        )
                                    }

                                    item {
                                        desing_style_circular(
                                            "Tipo : ${datos.tipoPropiedad.capitalizeFirst()}",
                                            Color(0xFF980606).copy(alpha = 0.25f)
                                        )
                                    }

                                    item {
                                        desing_style_circular(
                                            "Divisa principal: ${datos.divisa.capitalizeFirst()}",
                                            Color(0xFF9D3704).copy(alpha = 0.25f)
                                        )
                                    }
                                }

                                texto_generico_one_line(
                                    "Precio en soles o dolares",
                                    color = Color(0xFFB0B0B0),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val tipoCambio = 3.75 // soles por dólar

                                    val (textoPrimario, textoSecundario) = when (datos.divisa) {
                                        "dolares" -> {
                                            val enSoles = datos.precio * tipoCambio
                                            "$${formatearNumero_double(datos.precio)}" to "S/${
                                                formatearNumero_double(
                                                    enSoles
                                                )
                                            }"
                                        }

                                        "soles" -> {
                                            val enDolares = datos.precio / tipoCambio
                                            "S/${formatearNumero_double(datos.precio)}" to "$${
                                                formatearNumero_double(
                                                    enDolares
                                                )
                                            }"
                                        }

                                        else -> formatearNumero_double(datos.precio) to ""
                                    }

                                    desing_style_circular(textoPrimario, Color(0xFFB69615))

                                    if (textoSecundario.isNotEmpty()) {
                                        texto_generico_one_line(
                                            "o",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        desing_style_circular(textoSecundario, Color(0xFFB69615))
                                    }
                                }
                                spacer_vertical(1.dp)

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    ItemIcono(icon_regla, "${datos.metros} m²")

                                    ItemIcono(
                                        icono_profuncidad,
                                        "${datos.fondo} m."

                                    )

                                    ItemIcono(
                                        icon_ancho,
                                        "${datos.ancho} m."
                                    )

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
                                spacer_vertical(10.dp)

                            }

                        }

                        item {
                            val colors: List<Color> = listOf(
                                Color(0xFF7D49EE),
                                Color(0xFF2354A6),
                                Color(0xFF046070),
                                Color(0xFF9A175C),
                            )
                            var expandido_var by rememberSaveable { mutableStateOf(true) }

                            Box() {
                                GeminiBlobBackground(
                                    isPlaying = isPlaying,
                                    colors = colors,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(15.dp))
                                        .matchParentSize()
                                )
                                Column(
                                    modifier = Modifier
                                        .animateContentSize()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {


                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Selecciona tu perfil",
                                            fontFamily = baners_geinz_work,
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 10.dp)
                                                .size(24.dp)
                                                .graphicsLayer {
                                                    compositingStrategy =
                                                        CompositingStrategy.Offscreen
                                                }
                                                .drawWithCache {
                                                    val brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFFFFD700),
                                                            Color(0xFFFF00FF),
                                                            Color(0xFF00E0BA)
                                                        )
                                                    )
                                                    onDrawWithContent {
                                                        drawContent()
                                                        drawRect(
                                                            brush = brush,
                                                            blendMode = BlendMode.SrcAtop
                                                        )
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                    }
                                    texto_generico_one_line(
                                        "Permite perzonalizar tu experiencia con asistentes de Geinz",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    LazyRow(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                                    ) {
                                        items(lista_perfil) { i ->
                                            seleccion_tipo_persona(
                                                i = i,
                                                seleccionado = filtro_seleccionado == i.txt
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
                                        expandido = expandido_var,
                                        viewmodel_tts = viewmodel_tts,
                                        respuesta_gemini_para_tts = respuesta_gemini_para_tts,
                                        filtro_seleccionado = filtro_seleccionado,
                                        desespandir = { expandido ->
                                            expandido_var = expandido
                                        }
                                    )
                                }
//                            Text(
//                                text = if (expandido) "▲ ver menos" else "▼ ver más",
//                                style = MaterialTheme.typography.labelSmall,
//                                color = Color.White.copy(alpha = 0.6f),
//                                modifier = Modifier
//                                    .padding(top = 8.dp)
//                            )
                                if (!expandido_var) {
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

                            spacer_vertical(10.dp)


                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .animateContentSize()
                                    .padding(
                                        start = paddingAnim,
                                        end = paddingAnim,
                                        bottom = (paddingAnim - 5.dp).coerceAtLeast(0.dp)
                                    ), verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Text(
                                    "Descubre que hay cerca",
                                    fontFamily = baners_geinz_work,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )

//                                AnimatedVisibility(!toastShown) {
                                texto_generico_multilinea(
                                    "Explora los servicios, comercios y atractivos cercanos al inmueble para tomar la mejor decisión de compra",
                                    style = MaterialTheme.typography.labelSmall
                                )
//                                }

                                Slider(
                                    value = nueva_busqueda,
                                    onValueChange = {
                                        nueva_busqueda = it.roundToInt().toFloat()
                                    },
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
                                            val metros =
                                                (nueva_busqueda).toInt()
                                            val texto =
                                                if (metros >= 1000) "1km" else "${metros}m"
                                            texto_generico_one_line(
                                                texto = "${metros}",
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                )

                                val metros = (nueva_busqueda * 100).toInt()
                                val texto = if (metros >= 1000) "1km" else "${metros}m"

                                texto_generico_one_line(
                                    "Buscando en un radio de $texto",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(9.dp)

                            }
                        }
                        item {
                            Column(
                                Modifier.animateContentSize(),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                if (seguros.isNotEmpty()) {
                                    ListaHorizontal(
                                        tipo_lanzado = "salud",
                                        titulo = "Lugares seguros",
                                        texto = "Encuentra hospitales, clínicas y puntos de atención cercanos al inmueble para tu tranquilidad y seguridad en cualquier momento.",
                                        lista = seguros,
                                        clik_carta = { id_, localida, img, nombre, lat, lng ->
                                            aler_dialog_contacto = true
                                            localidad_seguridad_salud = localida
                                            id_seguridad_salud = id_
                                            nombre_seguridad_salud = nombre
                                            img_seguirdad_salud = img
                                        })
                                }
                                if (cercanos.isNotEmpty()) {
                                    ListaHorizontal(
                                        tipo_lanzado = "cercanos",
                                        titulo = "Lugares cercanos",
                                        texto = "Descubre tiendas, restaurantes y servicios ubicados a pocos minutos de tu futura propiedad y disfruta de todo lo que tienes alrededor",
                                        lista = cercanos,
                                        clik_carta = { id_, localida, img, nombre, lat, lng ->
                                            localidad_tienda_seleccionada = localida
                                            id_tienda_selecionada = id_
                                            show_bottom_sheeet = true
                                        })
                                }
                                if (turisticos.isNotEmpty()) {
                                    ListaHorizontal(
                                        tipo_lanzado = "turisticos",
                                        titulo = "Lugares turistics",
                                        texto = "Accede fácilmente a servicios esenciales como mantenimiento, técnicos y soluciones para el hogar cerca de tu inmueble.",
                                        lista = turisticos,
                                        clik_carta = { id_, localida, img, nombre, lat_, lng_ ->
                                            img_turismo = img
                                            lat = lat_
                                            lng = lng_
                                            localidad_tienda_seleccionada = localida
                                            id_tienda_selecionada = id_
                                            viewmodelMapa.setBottomSheetVisible(true)
                                        })
                                }
                                if (servicios.isNotEmpty()) {
                                    ListaHorizontal(
                                        tipo_lanzado = "servicos",
                                        titulo = "Servicios para el hogar",
                                        texto = "Mira los negocios que tienes cerca del inmuble recorre los mejores lugares de barranca",
                                        lista = servicios,
                                        clik_carta = { id_, localida_params, img, nombre, lat, lng ->
                                            dialog_servicos_tramite = true
                                            id_tienda_select = id_
                                            localidad_tienda = localida_params
                                            iduser = ""
                                            localida = localida_params
                                        })
                                }

                            }
                            spacer_vertical(10.dp)
                        }

                        item {
                            MapPreview(datos.listaImg.first(), datos.lat, datos.lng, {})
                            spacer_vertical(80.dp)
                        }
                    }
                }

            }

            viewmodel_inmobiliaria.etado_carga_info_inmuebles.loading -> {

            }
        }

        if (cargando_data) {
            ShimmerImagenConMarca_logo()
        }

        AnimatedVisibility(show_bottom_sheeet) {
            bottom_sheet_tiendas_filtradas(
                verificar_inter,
                viewModelFiltros,
                dataclass_tienda_seleccionada, show_bottom_sheeet
            ) {
                show_bottom_sheeet = false
            }
        }
        AnimatedVisibility(bottomSheetVisible) {
            bottom_sheet_lugares_turisticos(
                localidad_tienda_seleccionada,
                verificar_inter,
                viewmodelMap = viewmodelMapa,
                viewmodel_lugares_turisticos = viewmodel_lugares_turisticos,
                visible = true,
                onClose = {
                    viewmodelMapa.setBottomSheetVisible(false)
                },
                ver_mapa = {
                    abrir_mapa(
                        "turismo",
                        img_turismo,
                        lat,
                        lng
                    )
                }, iniciar_seccion = { iniciar_seccion() }, crear_cuenta = { crear_cuenta() },
                id_tienda_selecionada
            )
        }

        AnimatedVisibility(dialog_servicos_tramite) {
            dialog_servicios_tramite(
                viewmode_servicios_tramite, id_tienda_select, localidad_tienda, iduser,
                localida,
                ondimis = { dialog_servicos_tramite = false },
            )
        }

        AnimatedVisibility(aler_dialog_contacto) {
            val (llamada, whatsapp, long) = datos_numeros_salud_seguridad
            dialog_salud_seguridad_algolia(
                "",
                long,
                dialog_seguridad_salud_algolia(
                    whatsapp,
                    llamada,
                    nombre_seguridad_salud,
                    img_seguirdad_salud
                ),
                ondimis = { aler_dialog_contacto = false })
        }

        AnimatedVisibility(
            visible = mostrarColumna,
            enter = slideInVertically(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                initialOffsetY = { it }   // empieza desde abajo (fuera de pantalla)
            ),
            exit = slideOutVertically(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                targetOffsetY = { it }    // sale hacia abajo
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)

        ) {
            Column(
                modifier = Modifier

                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    btns(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4A0085),
                        icono = R.drawable.google_maps_icono,
                        text = "Ir a ver", clikeado = {
                            val instancia_datos = datos_viewmodel_inmobiliara(
                                id = datos_Estados_succes.id,
                                lista_img = datos_Estados_succes.listaImg,
                                localidad = datos_Estados_succes.distrito,
                                nombre = datos_Estados_succes.nombre,
                                latitud = datos_Estados_succes.lat,
                                longitud = datos_Estados_succes.lng,
                                ancho = datos_Estados_succes.ancho,
                                fondo = datos_Estados_succes.fondo,
                                precio = datos_Estados_succes.precio,
                                banos = datos_Estados_succes.banos,
                                metros = datos_Estados_succes.metros,
                                habitaciones = datos_Estados_succes.habitaciones,
                                cantidad_lugares_seguros =datos_Estados_succes.cantidad_lugares_seguros,
                                cantidad_lugares_cercanos =datos_Estados_succes.listalugares_cercanos ,
                                cantidad_lugares_turisticos =datos_Estados_succes.llissa_lugareS_turistos,
                                cantidad_lugares_para_el_hogar =datos_Estados_succes.lista_servicios_sercanos

                            )
                            abrir_mapa_ver_cercanos()
                            viewmodel_mapa_inmobilia.agregar_datos_para_pasa_mapa(instancia_datos)
                        }
                    )
                    btns(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF29A71A),
                        icono = R.drawable.whatsapp_icon,
                        text = "WhatsApp", {
                            house_capital_whatsap(
                                context,
                                "+1 (555) 167-1924",
                                "Hola quiero mas informacion sobre " +
                                        "https://geinzworkapp.web.app/share?t=in&id=${datos_Estados_succes.id}" +
                                        "&l=${datos_Estados_succes.distrito}" +
                                        "&p=${filtro_seleccionado}"
                            )
                        }
                    )
                }
            }
        }
    }

}

@Composable
fun GaleriaHorizontalInstagram(
    x0: List<String>,
    x1: String,
    modifier: Modifier.Companion,
    x3: () -> Unit,
    x4: () -> Int
) {
    TODO("Not yet implemented")
}

@Composable
fun MapPreview(
    img_inmueble: String,
    lat: Double,
    lon: Double,
    onClick: () -> Unit
) {

    val apiKey = BuildConfig.MAPBOX_ACCESS_TOKEN

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = "https://api.mapbox.com/styles/v1/benjaminlopez/cmm9c0hlt003901s54utw9p30/static/" +
                    // Pin rojo pequeño
                    "pin-s+ff0000($lon,$lat)/" +
                    // Centro del mapa
                    "$lon,$lat,18,0,45/" +
                    // Tamaño de la imagen
                    "1200x600" +
                    "?access_token=$apiKey",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.cargando_img_categorias),
            error = painterResource(R.drawable.cargando_img_categorias)
        )
    }

}


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


//                    val distanciaTexto = when {
//                        i.distanciaKm < 1.0 -> "${(i.distanciaKm * 1000).toInt()}m"
//                        else -> "${"%.1f".format(i.distanciaKm)}km"
//                    }

//                    texto_generico_one_line(
//                        distanciaTexto,
//                        color = Color(0xFFB0B0B0),
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.padding(start = 5.dp)
//                    )

                    }


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
    i: perfiles_negocios,
    seleccionado: Boolean,
    click: (String) -> Unit
) {
    val overlayAlpha by animateFloatAsState(
        targetValue = if (seleccionado) 0f else 0.55f,
        animationSpec = tween(300),
        label = "overlay"
    )

    // 🌑 Gradiente inferior animado — aparece cuando está seleccionado
    val gradientAlpha by animateFloatAsState(
        targetValue = if (seleccionado) 1f else 0f,
        animationSpec = tween(300),
        label = "gradient"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .width(100.dp)
            .height(120.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { click(i.txt) }
    ) {
        // 🖼 Imagen base
        Image(
            painter = painterResource(i.imagen),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null
        )

        // 🌑 Overlay negro — oscuro cuando no seleccionado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
        )

        // 🎨 Gradiente inferior — aparece al seleccionar para proteger el texto
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

        // 📝 Texto
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = i.txt,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = i.nombre_personas,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun GeminiBlobBackground_contexto(
    expandido: Boolean,
    viewmodel_tts: tts_stt,
    respuesta_gemini_para_tts: viewmodel_inmobiliaria.estado_carga_respuesta_con_IA,
    filtro_seleccionado: String,
    desespandir: (Boolean) -> Unit,
) {

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
                            CircularProgressIndicator()
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
fun iconos_de_filtrado(cantidad: String, icono: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(icono),
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
        texto_generico_one_line(cantidad, style = MaterialTheme.typography.bodyMedium)
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

// Para Double (precio que viene del modelo)
fun formatearNumero_double(numero: Double): String {
    return if (numero % 1.0 == 0.0) {
        "%,d".format(numero.toLong())   // 150000.0 → "150,000"
    } else {
        "%,.2f".format(numero)           // 150000.75 → "150,000.75"
    }
}


package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
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
import kotlinx.coroutines.launch

import kotlin.math.roundToInt
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.data.model.datos_viewmodel_inmobiliara
import com.geinzz.geinzwork.data.model.perfiles_negocios
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa.MapPreview_solo_imagen
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
    var datos_Estados_succes by remember { mutableStateOf(completeta_info_inmuebles()) }
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
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


            )

            abrir_mapa_ver_cercanos()
            viewmodel_mapa_inmobilia.agregar_datos_para_pasa_mapa(instancia_datos)
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }
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


    var filtro_seleccionado by remember { mutableStateOf("") }
    var nombre_personaje_seleccionado by remember { mutableStateOf("") }

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
    var toastShown by remember { mutableStateOf(false) }
    val paddingAnim by animateDpAsState(
        targetValue = if (toastShown) 5.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada = datosTienda!!.first()
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
    val cargando_data = estado is viewmodel_inmobiliaria.etado_carga_info_inmuebles.loading
            || estado == viewmodel_inmobiliaria.etado_carga_info_inmuebles.idle

    val mostrarColumna by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex >= 3
        }
    }

    var isFullscreen by rememberSaveable { mutableStateOf(false) } // 🔲 estado pantalla completa
    val altura by animateDpAsState(
        targetValue = if (isFullscreen) LocalConfiguration.current.screenHeightDp.dp else 550.dp,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "altura_galeria"
    )


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

                        item(key = "datos") {
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

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    item { ItemIcono(icon_regla, "${datos.metros} m²") }
                                    item {
                                        ItemIcono(
                                            icono_profuncidad,
                                            "${datos.fondo} m."

                                        )
                                    }
                                    item {

                                        ItemIcono(
                                            icon_ancho,
                                            "${datos.ancho} m."
                                        )
                                    }
                                    item {

                                        ItemIcono(
                                            icon_dormitorio,
                                            "${datos.habitaciones} dorm."
                                        )
                                    }
                                    item {
                                        ItemIcono(
                                            icon_bano,
                                            "${datos.banos} baños."
                                        )
                                    }
                                    item {
                                        ItemIcono(
                                            icono_cochera,
                                            "${datos.estacionamientos} estac."
                                        )
                                    }
                                }
                                spacer_vertical(10.dp)

                            }

                        }

                        item(key = "perfiles") {
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

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        lista_perfil.forEach { i ->
                                            key(i.txt) {
                                            seleccion_tipo_persona(
                                                i = i,
                                                seleccionado = filtro_seleccionado == i.txt
                                            ) { tipo_select,nombre ->
                                                filtro_seleccionado = tipo_select
                                                nombre_personaje_seleccionado=nombre
                                                viewmodel_tts.detenerAudio()
                                                nueva_busqueda = 5.0f
                                                val radioEnKm = nueva_busqueda.toDouble() / 10.0
                                                scope.launch {
                                                    filtar_datos(
                                                        viewModel = viewModel,
                                                        radioEnKm = radioEnKm,
                                                        datos = datos,
                                                        nombre_user = nombre_user,
                                                        lista_lugares_cercanos_filtrada = lista_lugares_cercanos_filtrada,
                                                        tipo_select = tipo_select
                                                    )
                                                }
                                            }
                                            }
                                        }
                                    }
                                    GeminiBlobBackground_contexto(
                                        nombre_personaje_seleccionado,
                                        expandido = expandido_var,
                                        viewmodel_tts = viewmodel_tts,
                                        respuesta_gemini_para_tts = respuesta_gemini_para_tts,
                                        filtro_seleccionado = filtro_seleccionado,
                                        desespandir = { expandido ->
                                            expandido_var = expandido
                                        }
                                    )
                                }
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

                        item(key = "slider") {
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
                        item (key = "listas") {
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

                        item (key = "mapa"){
                            MapPreview_solo_imagen( datos.lat, datos.lng, {})
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
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
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
                                )

                                abrir_mapa_ver_cercanos()
                                viewmodel_mapa_inmobilia.guardar_listas_datos_lugares_Seguros(datos_Estados_succes.cantidad_lugares_seguros)
                                viewmodel_mapa_inmobilia.guardar_lista_datos_lugares_cercanos(datos_Estados_succes.listalugares_cercanos)
                                viewmodel_mapa_inmobilia.guardar_lista_datos_lugares_turisticos(datos_Estados_succes.llissa_lugareS_turistos)
                                viewmodel_mapa_inmobilia.guardar_lista_datos_lugares_servicio_hogar(datos_Estados_succes.lista_servicios_sercanos)
                                viewmodel_mapa_inmobilia.agregar_datos_para_pasa_mapa(instancia_datos)
                            } else {
                                permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
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








fun normalizarNombre(nombre: String): String {
    return nombre
        .lowercase()
        .replace(Regex("[^a-záéíóúñ ]"), "")
        .split(" ")
        .first()
}



// Para Double (precio que viene del modelo)
fun formatearNumero_double(numero: Double): String {
    return if (numero % 1.0 == 0.0) {
        "%,d".format(numero.toLong())   // 150000.0 → "150,000"
    } else {
        "%,.2f".format(numero)           // 150000.75 → "150,000.75"
    }
}


package com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_compartir.compartir_pantalla_completa
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_llamada_urgencias
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permisos_llamadas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.requestCallPermission
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_alerta_llamada
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.centrado_hori_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud.carga_seguidad
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.pow


import androidx.compose.runtime.State
import androidx.compose.ui.focus.FocusManager
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TypewriterClickableText
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_compartir_ubicacion_con_entidad_salud
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.enviarMensajeEmergencia
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.makePhoneCall


private val REQUEST_CALL_PHONE = 1
sealed class EstadoEnvioEmergencia {
    object Idle : EstadoEnvioEmergencia()
    object VerificandoPermisos : EstadoEnvioEmergencia()
    object PidiendoPermiso : EstadoEnvioEmergencia()         // esperando callback del launcher
    object ActivandoGPS : EstadoEnvioEmergencia()            // esperando que el usuario active GPS
    object ObteniendoUbicacion : EstadoEnvioEmergencia()     // spinner visible
    data class Listo(val mensaje: String) : EstadoEnvioEmergencia()  // puede enviarse
    data class Error(val texto: String) : EstadoEnvioEmergencia()
}
@Composable
fun ui_salud_seguirdad(
    is_conect: Boolean,
    nombre_user: String,
    id_user: String,
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    localida: String,
    abrir_mapa: (latitud: Double, longitud: Double) -> Unit
) {


    var mostrar_busqueda_por_NL by remember { mutableStateOf(false) }
    var mostar_dialog_permiso_llamada by remember { mutableStateOf(false) }
    var numero_dialogo_permiso_llamda by remember { mutableStateOf("") }
    val lista_filtrado = listOf<String>("Todos", "salud", "seguridad")
    val lista_seguridad_salud by viewmode_segurirdad_Salud._datos_lugares.observeAsState(emptyList())
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lista_mostrar by rememberSaveable { mutableStateOf<List<dataclass_seguridad>>(emptyList()) }
    var lista_base_seguridad by rememberSaveable { mutableStateOf(emptyList<dataclass_seguridad>()) }
    var valor_filtrado by rememberSaveable { mutableStateOf("") }
    var chip_selecionado by rememberSaveable { mutableStateOf("Todos") }
    val state_seguridad =
        viewmode_segurirdad_Salud.state_lista_filtradad.collectAsState(carga_seguidad.loading).value
    val mostrar_carga_salud_seguridad by viewmode_segurirdad_Salud.mostrar_carga_salud_seguridad.collectAsState()
    val datos_cloud_TTs by viewmode_segurirdad_Salud.datosCloudTts.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var error_empity by remember { mutableStateOf(false) }
    var texto_error_empity by remember { mutableStateOf("") }
    var yaInicializado by remember { mutableStateOf(false) }
    val dialogo_contacto by viewmode_segurirdad_Salud.callDialogPermise.collectAsState()
    val autoseleccion_filtrado by viewmode_segurirdad_Salud.categoira_filtrado_realziado.collectAsState()
    val autoseleccion_filtrado_solo_texto by viewmode_segurirdad_Salud.categoira_solo_texto_realizado.collectAsState()

    val focusManager = LocalFocusManager.current
    LaunchedEffect(is_conect) {
        mostrar_busqueda_por_NL = is_conect
        if (!is_conect) {
            viewmode_segurirdad_Salud.cambiar_estado_Sin_internet()
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            viewmode_segurirdad_Salud.limpiarEstado()


        }
    }


    LaunchedEffect(autoseleccion_filtrado) {
        if (autoseleccion_filtrado.isNotEmpty()) {
            chip_selecionado = autoseleccion_filtrado
        }
    }

    LaunchedEffect(chip_selecionado) {
        if (!autoseleccion_filtrado_solo_texto) {
            if (yaInicializado && lista_seguridad_salud.isNotEmpty() && chip_selecionado != "Todos") {
                viewmode_segurirdad_Salud.filtrar_lugares(chip_selecionado)
            } else {
                viewmode_segurirdad_Salud.lista_base_completa(chip_selecionado)
            }
        }
    }

    LaunchedEffect(datos_cloud_TTs) {
        if (datos_cloud_TTs.isNotEmpty()) {
            viewmode_segurirdad_Salud.reproducirMP3(context, datos_cloud_TTs)
            viewmode_segurirdad_Salud.limpiarAudio()
        }
    }
    LaunchedEffect(valor_filtrado) {
//        viewmode_segurirdad_Salud.filtrar_nombre_categoria(
//            nombre = valor_filtrado,
//            categoria = chip_selecionado,
//            lista = lista_base_seguridad
//        )


    }


    // Llama servicios iniciales
    LaunchedEffect(Unit) {
        viewmode_segurirdad_Salud.nombre_user(nombre_user)
        viewmode_segurirdad_Salud.obtener_servicios(localida, context)

        viewmode_segurirdad_Salud.controlarEntrenamiento(context)
    }

    LaunchedEffect(lista_seguridad_salud) {
        if (!yaInicializado && lista_seguridad_salud.isNotEmpty()) {
            yaInicializado = true
            lista_base_seguridad = lista_seguridad_salud
            viewmode_segurirdad_Salud.lugares_iniciales(lista_seguridad_salud)
            // 🔥 EXTRAER SOLO LOS NOMBRES


        }
    }

    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    var sin_resultados by remember { mutableStateOf(false) }
    var toastShown by remember { mutableStateOf(false) }

    val stickyHeaderIndex = 1
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex >= stickyHeaderIndex && !toastShown) {
            toastShown = true
        } else if (listState.firstVisibleItemIndex < stickyHeaderIndex) {
            toastShown = false
        }
    }
    var bottom_sheet_llamda by remember { mutableStateOf(false) }
    val paddingAnim by animateDpAsState(
        targetValue = if (toastShown) 10.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    var tienePermisoLlamada by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // ✅ Launcher que reacciona cuando el usuario acepta o rechaza el permiso
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        tienePermisoLlamada = isGranted
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")

        }
    }

    // Launcher para pedir permiso
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d("GPS", "✅ El usuario activó el GPS")
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    texto_generico_multilinea(
                        "Salud y Seguridad Pública",
                        style = MaterialTheme.typography.banerGeinzWork,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .weight(1f)
                    )

                    Box(
                        modifier = Modifier,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.5f))
                                .clickable {
                                    compartir_pantalla_completa(
                                        "nemg",
                                        "Encuentra los números de emergencia de forma rápida y segura cuando más lo necesites.",
                                        context
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.comparir_icon),
                                modifier = Modifier.size(16.dp),
                                contentDescription = null
                            )
                        }
                    }

                }
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "Tu bienestar es primero, localiza hospitales, comisarías, bomberos y servicios de ayuda cuando los necesites.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            stickyHeader {
                ColumnContenedorComun(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth()
                            .padding(
                                start = paddingAnim,
                                end = paddingAnim,
                                bottom = (paddingAnim - 5.dp).coerceAtLeast(0.dp)
                            )
                    ) {
                        if (mostrar_busqueda_por_NL) {
                            FiltradoTextField(
                                focusManager,
                                permisoLauncher,
                                launcher,
                                fusedLocationClient,
                                viewmodelSeguridadSalud = viewmode_segurirdad_Salud,
                                onValueChange = {
                                    valor_filtrado = it
                                    viewmode_segurirdad_Salud.preguntar_gemini(
                                        valor_filtrado,
                                        context,
                                        fusedLocationClient, launcher, permisoLauncher
                                    )
                                },
                                regresarListaCompleta = {
                                    viewmode_segurirdad_Salud.retornar_lista_comppleta()
                                    chip_selecionado = "Todos"
                                }, { numero ->
                                    mostar_dialog_permiso_llamada = true
                                    numero_dialogo_permiso_llamda = numero
                                })
                            spacer_vertical(10.dp)
                        }
                        chips_filtrado(
                            tienePermisoLlamada1 = tienePermisoLlamada,
                            context = context,
                            selecionado_chip = chip_selecionado,
                            lista_filtrado = lista_filtrado,
                            selecionado_fun = { i ->
                                chip_selecionado = i
                            },
                            select_alerta = {
                                bottom_sheet_llamda = true
                            })
                    }
                }
            }
            when (state_seguridad) {
                is viewmode_seguridad_salud.carga_seguidad.loading -> {
                    isLoading = true
                    error_empity = false
                }

                is viewmode_seguridad_salud.carga_seguidad.succes -> {
                    val lista =
                        (state_seguridad as viewmode_seguridad_salud.carga_seguidad.succes).list
                    items(lista) { i ->
                        isLoading = false
                        error_empity = false
                        Box(modifier = Modifier.padding(8.dp).animateItem(
                            placementSpec = tween(
                                durationMillis = 350,
                                easing = FastOutSlowInEasing
                            )
                        )) {
                            carta_salud_cuidad(
                                id_user = id_user,
                                viewmode_segurirdad_Salud = viewmode_segurirdad_Salud,
                                i = i,
                                fusedLocationClient = fusedLocationClient,
                                abrir_mapa = { la, lo ->
                                    viewmode_segurirdad_Salud.setCoordenadas(la, lo)
                                    abrir_mapa(la, lo)
                                }, permisoLauncher = permisoLauncher
                            )
                        }
                    }
                }

                is viewmode_seguridad_salud.carga_seguidad.empity -> {
                    val texto =
                        (state_seguridad as viewmode_seguridad_salud.carga_seguidad.empity).texto
                    isLoading = false
                    error_empity = true
                    texto_error_empity = texto
                }

                is viewmode_seguridad_salud.carga_seguidad.error -> {
                    val texto =
                        (state_seguridad as viewmode_seguridad_salud.carga_seguidad.error).texto
                    isLoading = false
                    error_empity = true
                    texto_error_empity = texto

                }
            }
        }

        if (mostrar_carga_salud_seguridad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(5f),
                contentAlignment = Alignment.Center
            ) {
                pantalla_carga_login(false)
            }
        }

        AnimatedContent(
            targetState = when {
                isLoading -> "loading"
                sin_resultados -> "empty"
                error_empity -> "error"
                else -> "none"
            },
            label = "estado_carga"
        ) { estado ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                centrado_hori_vertical {
                    when (estado) {
                        "loading" -> {
//                            CircularProgressIndicator()
                        }

                        "empty" -> texto_generico_one_line(
                            texto_error_empity,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        "error" -> texto_generico_one_line(
                            texto_error_empity,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            !mostrar_carga_salud_seguridad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                        )
                    )
                    .graphicsLayer { alpha = alphaAnim }
            )
        }

        if (bottom_sheet_llamda) {
            bottom_sheet_alerta_llamada(
                ondimis = { bottom_sheet_llamda = false },
                mostrar_permiso = {
                    requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                })
        }

        val numeroALlamar = when {
            dialogo_contacto.first -> dialogo_contacto.second
            mostar_dialog_permiso_llamada -> numero_dialogo_permiso_llamda
            else -> null
        }

        numeroALlamar?.let { numero ->
            permisos_llamadas(
                aceptar_permisos = {
                    requestCallPermission(context = context, phoneNumber = numero)
                },
                ondimis = {
                    viewmode_segurirdad_Salud.cambiar_estado_valor_calldialog()
                }
            )
        }

    }
}

@Composable
fun chips_filtrado(
    tienePermisoLlamada1: Boolean,
    context: Context,
    selecionado_chip: String,
    lista_filtrado: List<String>,
    selecionado_fun: (String) -> Unit,
    select_alerta: () -> Unit,
) {


    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(lista_filtrado) { i ->
            val selecionado = selecionado_chip == i
            chisp_filtrado_busqueda(
                carta_selecionada = selecionado,
                filtrado = i.capitalizeFirst(),
                btn_visible = false,
                clik_card = {
                    selecionado_fun(i)
                }, onClick_delete = {})

        }
        if (!tienePermisoLlamada1) {
            item {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .height(45.dp)
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            select_alerta()

                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    texto_generico_one_line("Alerta", style = MaterialTheme.typography.bodyMedium)
                    spacer_horizonta(5.dp)
                    Image(
                        painter = painterResource(R.drawable.icono_alerta_3d_webp),
                        contentDescription = "alerta",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }


}

@Composable
fun FiltradoTextField(
    focusManager: FocusManager,
    permisoLauncher: ManagedActivityResultLauncher<String, Boolean>,
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    fusedLocationClient: FusedLocationProviderClient,
    viewmodelSeguridadSalud: viewmode_seguridad_salud,
    onValueChange: (String) -> Unit,
    regresarListaCompleta: () -> Unit,
    call_dialog_permise: (String) -> Unit
) {


    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var numero_llamada by rememberSaveable { mutableStateOf("") }
    var expandido by remember { mutableStateOf(true) }
    val repo = repo_seguridad_salud()

    var mostar_micro = viewmodelSeguridadSalud._mostrar_micro.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var estadoMic by remember { mutableStateOf(viewmode_seguridad_salud.EstadoMic.IDLE) }
    var ocultar_borrado by remember { mutableStateOf(false) }

    var textoBuscar by remember { mutableStateOf("") }
    var grabando by remember { mutableStateOf(false) }
    var cargandoVoz by remember { mutableStateOf(false) }
    var huboVoz by remember { mutableStateOf(false) }

    var amplitudes by remember { mutableStateOf(listOf(0f, 0f, 0f)) }
    var audioData by remember { mutableStateOf(ByteArray(0)) }

    val SILENCE_THRESHOLD = 0.02f
    val SILENCE_TIME_MS = 1200L
    var silencioInicio by remember { mutableStateOf<Long?>(null) }

    val titulo_mostrado_IA by viewmodelSeguridadSalud.titulo_mostrado.collectAsState()
    val descripcion_mostrado_IA by viewmodelSeguridadSalud.texto_mostrado.collectAsState()
    var mostrar_respuesIA by remember { mutableStateOf(false) }
    var detener_grabacion_btn by remember { mutableStateOf(false) }

    val estadoBusqueda by viewmodelSeguridadSalud.estadoBusqueda
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            estadoMic = viewmode_seguridad_salud.EstadoMic.GRABANDO
        } else {
            Toast.makeText(context, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
            estadoMic = viewmode_seguridad_salud.EstadoMic.IDLE
        }
    }


    LaunchedEffect(descripcion_mostrado_IA) {
        Log.d("titutofitra", "$titulo_mostrado_IA $descripcion_mostrado_IA")

        mostrar_respuesIA = descripcion_mostrado_IA.isNotBlank()
    }

    LaunchedEffect(estadoMic) {
        if (estadoMic != viewmode_seguridad_salud.EstadoMic.GRABANDO) return@LaunchedEffect

        grabando = true
        huboVoz = false
        audioData = ByteArray(0)
        silencioInicio = null

        withContext(Dispatchers.IO) {
            val sampleRate = 16000
            val minBuffer = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AUDIO", "AudioRecord no inicializado")
                estadoMic = viewmode_seguridad_salud.EstadoMic.IDLE
                return@withContext
            }

            val buffer = ByteArray(minBuffer)
            val stream = ByteArrayOutputStream()

            try {
                recorder.startRecording()

                while (grabando && estadoMic == viewmode_seguridad_salud.EstadoMic.GRABANDO) {
                    val read = recorder.read(buffer, 0, buffer.size)

                    if (read > 0) {

                        stream.write(buffer, 0, read)

                        var sum = 0.0
                        var samples = 0

                        for (i in 0 until read step 2) {
                            val low = buffer[i].toInt() and 0xFF
                            val high = buffer[i + 1].toInt()
                            val sample = (high shl 8) or low
                            sum += sample * sample
                            samples++
                        }

                        val rms = kotlin.math.sqrt(sum / samples).toFloat() / 32768f

                        val visualAmp =
                            (rms * 28f).coerceIn(0f, 0.85f).pow(0.7f)

                        withContext(Dispatchers.Main) {
                            amplitudes = amplitudes
                                .map { it * 0.85f }
                                .mapIndexed { i, old ->
                                    val factor = listOf(1f, 0.85f, 0.7f)[i]
                                    max(old, visualAmp * factor)
                                }
                        }

                        // 🔥 DETECCIÓN DE SILENCIO REAL
                        if (rms > SILENCE_THRESHOLD) {
                            huboVoz = true
                            silencioInicio = null
                        } else {
                            if (silencioInicio == null) {
                                silencioInicio = System.currentTimeMillis()
                            } else {
                                val silencioActual =
                                    System.currentTimeMillis() - silencioInicio!!

                                if (silencioActual > SILENCE_TIME_MS) {

                                    // 🔥 Si nunca habló → cancelar
                                    if (!huboVoz) {
                                        withContext(Dispatchers.Main) {
                                            estadoMic = viewmode_seguridad_salud.EstadoMic.IDLE
                                            Toast.makeText(
                                                context,
                                                "No se detectó voz",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        // 🔥 Si habló y luego silencio → enviar
                                        withContext(Dispatchers.Main) {
                                            estadoMic = viewmode_seguridad_salud.EstadoMic.ENVIANDO
                                            cargandoVoz = true
                                            scope.launch(Dispatchers.IO) {
                                                var intentos = 0
                                                while (audioData.isEmpty() && intentos < 40) {
                                                    delay(50)
                                                    intentos++
                                                }

                                                if (audioData.isEmpty()) {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Error grabando audio",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        estadoMic =
                                                            viewmode_seguridad_salud.EstadoMic.IDLE
                                                    }
                                                    return@launch
                                                }
                                                try {
                                                    val texto =
                                                        viewmodelSeguridadSalud.tranformar_texto_a_voz(
                                                            audioData
                                                        )
                                                    withContext(Dispatchers.Main) {
                                                        textoBuscar = texto
                                                        onValueChange(texto)
                                                        estadoMic =
                                                            viewmode_seguridad_salud.EstadoMic.IDLE
                                                    }
                                                } finally {
                                                    withContext(Dispatchers.Main) {
                                                        cargandoVoz = false
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    grabando = false
                                }
                            }
                        }
                    }
                    delay(16)
                }
            } finally {
                recorder.stop()
                recorder.release()
                audioData = stream.toByteArray()
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 16.dp)

    ) {

        OutlinedTextField(
            value = textoBuscar,
            onValueChange = {
                textoBuscar = it
                if (it.isEmpty()) {
                    regresarListaCompleta()
                    viewmodelSeguridadSalud.cabiar_valor_mostara_micro(true)
                    ocultar_borrado = false
                    mostrar_respuesIA = false
                } else {
                    viewmodelSeguridadSalud.cabiar_valor_mostara_micro(false)
                    ocultar_borrado = true
                }
            },
            placeholder = {
                texto_generico_one_line(
                    "Dime tu emergencia",
                    style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        viewmodelSeguridadSalud.cabiar_valor_mostara_micro(false)
                        if(textoBuscar.isNotEmpty()){
                            ocultar_borrado = true
                        }
                    }else{

                    }
                },
            shape = RoundedCornerShape(50),
            leadingIcon = {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (mostar_micro.value) {
                        Crossfade(targetState = estadoMic) { estado ->
                            when (estado) {
                                viewmode_seguridad_salud.EstadoMic.IDLE -> {
                                    detener_grabacion_btn = false
                                    IconButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            if (ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED
                                            ) {
                                                estadoMic =
                                                    viewmode_seguridad_salud.EstadoMic.GRABANDO
                                            } else {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }

                                        }
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = "Hablar")
                                    }
                                }

                                viewmode_seguridad_salud.EstadoMic.GRABANDO -> {
                                    detener_grabacion_btn = true
                                    MicVisualizerGoogle(
                                        amplitudes = amplitudes,
                                        modifier = Modifier.clickable {
                                            if (!huboVoz) {
                                                Toast.makeText(
                                                    context,
                                                    "No se detectó voz",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                estadoMic = viewmode_seguridad_salud.EstadoMic.IDLE
                                                return@clickable
                                            }

                                            // ✅ Hubo voz → enviar
                                            estadoMic = viewmode_seguridad_salud.EstadoMic.ENVIANDO
                                            cargandoVoz = true

                                            scope.launch(Dispatchers.IO) {
                                                var intentos = 0
                                                while (audioData.isEmpty() && intentos < 40) {
                                                    delay(50)
                                                    intentos++
                                                }

                                                if (audioData.isEmpty()) {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Error grabando audio",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        estadoMic =
                                                            viewmode_seguridad_salud.EstadoMic.IDLE
                                                    }
                                                    return@launch
                                                }
                                                try {
                                                    val texto =
                                                        viewmodelSeguridadSalud.tranformar_texto_a_voz(
                                                            audioData
                                                        )
                                                    withContext(Dispatchers.Main) {
                                                        textoBuscar = texto
                                                        onValueChange(texto)
                                                        estadoMic =
                                                            viewmode_seguridad_salud.EstadoMic.IDLE
                                                    }
                                                } finally {
                                                    withContext(Dispatchers.Main) {
                                                        cargandoVoz = false
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                viewmode_seguridad_salud.EstadoMic.ENVIANDO -> {
                                    detener_grabacion_btn = false
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                else -> {}
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(targetState = estadoBusqueda) { estado ->
                                when (estado) {
                                    viewmode_seguridad_salud.Estado_busqueda.IDLE -> {
                                        IconButton(

                                            onClick = {
                                                if (textoBuscar.isNotEmpty()) {

                                                    focusManager.clearFocus()
                                                    // Cambiar a CARGANDO para mostrar ProgressBar
                                                    viewmodelSeguridadSalud.cambiar_Estado_carga()

                                                    // Ejecutar búsqueda en background
                                                    scope.launch(Dispatchers.IO) {
                                                        try {
                                                            viewmodelSeguridadSalud.preguntar_gemini(
                                                                textoBuscar,
                                                                context,
                                                                fusedLocationClient,
                                                                launcher,
                                                                permisoLauncher
                                                            )
                                                        } finally {
                                                            // Una vez terminado, volver a IDLE en el hilo principal
                                                            withContext(Dispatchers.Main) {
//                                                            estadoBusqueda = viewmode_seguridad_salud.Estado_busqueda.IDLE
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Escribe o di algo",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "Buscar"
                                            )
                                        }
                                    }

                                    viewmode_seguridad_salud.Estado_busqueda.BUSCANDO -> {
                                        // Si quieres, aquí podrías mostrar otra animación mientras se procesa algo
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }

                                    viewmode_seguridad_salud.Estado_busqueda.CARGANDO -> {
                                        // Mostrar ProgressBar mientras se ejecuta la búsqueda
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            },
            trailingIcon = {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (ocultar_borrado) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                textoBuscar = ""

                                viewmodelSeguridadSalud.cabiar_valor_mostara_micro(true)
                                mostrar_respuesIA = false
                                ocultar_borrado = false
                                regresarListaCompleta()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Hablar", tint = Color.Gray
                            )
                        }
                    }

                    if (detener_grabacion_btn) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()

                                // 🔥 SOLO romper el loop
                                grabando = false
                                estadoMic = viewmode_seguridad_salud.EstadoMic.IDLE

                                // 🔥 Limpiar estados visuales
                                audioData = ByteArray(0)
                                amplitudes = listOf(0f, 0f, 0f)
                                huboVoz = false
                                detener_grabacion_btn = false
                                cargandoVoz = false

                                Toast.makeText(
                                    context,
                                    "Grabación cancelada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Detener",
                                tint = Color.Gray
                            )
                        }
                    }

                }
            }
        )

        if (mostrar_respuesIA) {
            spacer_vertical(10.dp)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_geinz_500x500),
                        contentDescription = "Logo IA",
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                viewmodelSeguridadSalud.cloudTTS(descripcion_mostrado_IA)
                            }
                            .size(35.dp)
                    )
                    spacer_horizonta(5.dp)
                    Text(
                        text = "Geinz",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { expandido = !expandido }
                    ) {
                        Icon(
                            imageVector = if (expandido)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir"
                        )
                    }
                }

                // 🔹 Contenido expandible
                if (expandido) {
                    TypewriterClickableText(
                        viewmodelSeguridadSalud,
                        descripcion_mostrado_IA, onClickRuta = { lat, lng ->
                            constantes_lista_localidades.abrir_google_maps(
                                "", "emergencia", "", "",
                                context = context,
                                lat, lng
                            ) { mostrar_dialog ->
//                                dialogo_activar_ubicacion = mostrar_dialog
                            }
                        },
                        onClickTelefono = { numero, tipo ->
                            if (tipo == "LLAMADA") {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CALL_PHONE
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    call_dialog_permise(numero)

                                } else {
                                    makePhoneCall(context, numero)
                                }
                            } else if (tipo == "WHATSAPP") {
                                enviarMensajeEmergencia(
                                    fusedLocationClient,
                                    repo,
                                    onMensajeListo = { msj ->
                                        abrir_whattsapp(
                                            "",
                                            tipo = "",
                                            id_tienda = "",
                                            localidad_tienda = "",
                                            context = context,
                                            numero = "937659216",
                                            mensajePredefinido = msj
                                        )
                                    })
                            }
                        })
                }
            }


        }

    }
}


@Composable
fun MicVisualizerGoogle(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF4285F4), // Azul
        Color(0xFFEA4335), // Rojo
        Color(0xFFFBBC05)  // Amarillo
    )

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFF313131)), // gris suave Google
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            amplitudes.take(3).forEachIndexed { index, amp ->

                val animatedHeight by animateFloatAsState(
                    targetValue = amp.coerceIn(0.1f, 1f),
                    animationSpec = tween(
                        durationMillis = 120,
                        easing = FastOutSlowInEasing
                    ),
                    label = "micBar"
                )

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(15.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedHeight)
                            .align(Alignment.BottomCenter)
                            .background(
                                colors[index % colors.size],
                                RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun carta_salud_cuidad(
    id_user: String,
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    i: dataclass_seguridad,
    fusedLocationClient: FusedLocationProviderClient,
    abrir_mapa: (latitud: Double, longitud: Double) -> Unit,
    permisoLauncher: ManagedActivityResultLauncher<String, Boolean>

) {
    val context = LocalContext.current
    var dialogo_activar_ubicacion by rememberSaveable { mutableStateOf(false) }

    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var dialogo_contacto by remember { mutableStateOf(false) }
    var lista_numero by remember { mutableStateOf(listOf<String>()) }
    var icono_dialogo by remember { mutableStateOf("") }
    var dialog_sin_lat_log by remember { mutableStateOf(false) }
    var mostar_dialog_convesacional_entidad by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")

        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surface)

    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(i.img_ref)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .size(300, 100).placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.padding(start = 10.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
        ) {
            texto_generico_one_line(
                i.nombre_,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            spacer_vertical(5.dp)
            if (i.direccion.isNotEmpty()) {
                texto_generico_one_line(
                    i.direccion, color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
            }
            texto_generico_one_line(
                viewmode_segurirdad_Salud.horario_atencion(i.nombre_),
                color = Color.White, style = MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                if (i.numero_llamada.isNotEmpty()) {
                    BtnCirculares(R.drawable.llamada_icon) {
                        dialogo_contacto = true
                        lista_numero = i.numero_llamada
                        icono_dialogo = "llamada"

                    }
                }
                if (i.numero_whatsapp.isNotEmpty()) {
                    BtnCirculares(R.drawable.whatsapp_icon) {
                        dialogo_contacto = true
                        lista_numero = i.numero_whatsapp
                        icono_dialogo = "whatsapp"

                    }
                }
                if (i.latidud != 0.0 || i.longitud != 0.0) {
                    BtnCirculares(
                        R.drawable.vector_ruta_icon,
                        fondo = MaterialTheme.colorScheme.primary
                    ) {
                        constantes_lista_localidades.abrir_google_maps(
                            id_user, "emergencia", "", "",
                            context = context,
                            i.latidud, i.longitud
                        ) { mostrar_dialog ->
                            dialogo_activar_ubicacion = mostrar_dialog
                        }
                    }
                }
            }
        }
    }

    if (dialogo_activar_ubicacion) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                dialogo_activar_ubicacion = false
            },
            abrir_configuracion = {
                dialogo_activar_ubicacion = false
                verificarGPS(context, launcher)
//                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                dialog_sin_lat_log = true
                dialogo_activar_ubicacion = false
            }
        )
    }
    if (dialog_sin_lat_log) {
        dialog_sin_ubi_activa(
            direccion = i.direccion,
            referencia = i.referencia,
            onDismis = { dialog_sin_lat_log = false },
            abrir_maps = { constantes.abrirGoogleMaps(context, i.direccion) })
    }
    if (dialogo_contacto) {
        dialog_llamada_urgencias(
            i.img_ref,
            i.nombre_,
            launcher_dialog_ubicacion = launcher,
            permision_obtener_cordenadas = permisoLauncher,
            fusedLocationClient = fusedLocationClient,
            viewmodeSeguridadSalud = viewmode_segurirdad_Salud,
            lista_numeros = lista_numero,
            tipo = icono_dialogo
        , {
                dialogo_contacto = false
            },{
                mostar_dialog_convesacional_entidad=true
            })
    }

    if(mostar_dialog_convesacional_entidad){
        dialog_compartir_ubicacion_con_entidad_salud(
            ondismis = {mostar_dialog_convesacional_entidad=false},
            entidad = i.nombre_,
            img_entidad = i.img_ref
        )
    }
}


@Composable
fun BtnCirculares(
    icono: Any,
    fondo: Color = Color.Transparent,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    tint: Color = Color.White,
    listener: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fondo)
            .clickable { listener() },
        contentAlignment = Alignment.Center
    ) {
        when (icono) {
            is Int -> Image(
                painter = painterResource(id = icono),
                contentDescription = null,
            )

            is ImageVector -> Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .padding(5.dp),
                tint = tint
            )
        }
    }
}

@Composable
fun rememberInternetState(): State<Boolean> {

    val context = LocalContext.current
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val internetState = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                internetState.value = true
            }

            override fun onLost(network: Network) {
                internetState.value = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return internetState
}
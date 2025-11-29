package com.geinzz.geinzwork.ui.adapters.ui.principal


import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.localidad_Selecionada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.rutas_turismo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulo_referenciales_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.esAniversarioHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerAniversarioLocalidad
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.core.content.ContextCompat
import coil3.request.CachePolicy
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.widget_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad.guarar_dialogo_notifi
import com.geinzz.geinzwork.data_store.data_store_localidad.sendNotificacion
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.baner_registra_tu_negocio
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.baner_servicios_basicos_
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.baner_widget_tienda_geinz_baner

import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permiso_primario_notifi
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_ayudanos_a_creccer
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.verificar_version
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.guarar_token_user
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_carga_img_general
import com.geinzz.geinzwork.viewmodel_carga_img_generalFactory
import com.google.firebase.messaging.FirebaseMessaging

private lateinit var firebaseAuth: FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal(
    verificar_intener: Boolean,
    datos_principales_user: datos_principales_user,
    categorias: (localidad: String, nombre_user: String) -> Unit,
    clikear_cartas: (String, String, String) -> Unit,
    ver_lugares: (String) -> Unit,
    listner_busqueda: () -> Unit,
    listener_seguridad: (String) -> Unit,
    listner_sevicios_tramites: (String) -> Unit,
    abrir_guardar_datos: () -> Unit,
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ud_tienda_shader by data_store_localidad.get_id_socio(context).collectAsState(initial = "")

    val vm_fotos_salud: viewmodel_carga_img_general = viewModel(
        factory = viewmodel_carga_img_generalFactory.Factory(context)
    )
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    val viewModel_filtado_tiendas: viewModel_filtado_tiendas = viewModel()
    val stateCat by viewModel_cordenadas._state_cat.observeAsState()
    val _categorias_tiendas by viewModel_cordenadas._sub_cat_tiendas.observeAsState(emptyList())
    val _obtener_filtrado_localidades by viewModel_cordenadas._lista_filtrado_localidades.observeAsState(
        emptyList()
    )
    var mostar_bottom_sheet_ayuda_geinz by remember { mutableStateOf(false) }
    val actulizacionE_stado_play by viewModel_cordenadas.estado_version_PS.collectAsState()
    val urls by vm_fotos_salud.urlsCarga.collectAsState()
    val urls_turistico by vm_fotos_salud.urlsCarga_turistico.collectAsState()
    val urlAleatoria = rememberSaveable(urls.hashCode()) {
        urls.randomOrNull() ?: ""
    }

    var datos_tienda by remember { mutableStateOf(widget_tienda()) }

    val estados_carga_widget by vm_fotos_salud.estado_carga_widget_tienda.collectAsState()

    LaunchedEffect(estados_carga_widget) {
        datos_tienda = estados_carga_widget
    }

    val url_turistico_aleatoria = rememberSaveable(urls_turistico.hashCode()) {
        urls_turistico.randomOrNull() ?: ""
    }
    var mostrar_widget_tienda by remember { mutableStateOf(false) }

//    LaunchedEffect(Unit) {
////        viewModel_cordenadas.obtener_subcategorias(true)
////        viewModel_cordenadas.obtner_filtrado_localidades()
//    }
    LaunchedEffect(Unit) {
        viewModel_cordenadas.verificar_vesion_actulizacion(context)
    }
    val ultimaLocalidad by data_store_localidad
        .obtener_localidad(context)
        .collectAsState(initial = null)

    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    val localidad_defaul = ultimaLocalidad ?: "barranca"
    val localidadSeleccionada = rememberSaveable { mutableStateOf("barranca") }

    val stickyHeaderIndex = 1
    var toastShown by remember { mutableStateOf(false) }


    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex >= stickyHeaderIndex && !toastShown) {
            toastShown = true
        } else if (listState.firstVisibleItemIndex < stickyHeaderIndex) {
            toastShown = false
        }
    }
    var esAniversarioHoy by rememberSaveable(localidad_defaul) {
        mutableStateOf(esAniversarioHoy(localidad_defaul))
    }
    val dialgo_notificacion by data_store_localidad.getNotificacion(context)
        .collectAsState(initial = false)
    val dialogo_notifi_ret by data_store_localidad.get_dialog_notifi(context)
        .collectAsState(initial = false)

    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    var id_respado_user by remember { mutableStateOf("") }

    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            id_respado_user = uid_respald_user
            Log.d("UID_DataStore", "✅ Recuperado UID válido desde DataStore: $id_respado_user")
        } else {
            id_respado_user = ""
        }
    }

    val tick by viewModel_filtado_tiendas.tick.collectAsState()

    var bloques_hoy by remember { mutableStateOf<List<HorarioBloque>>(emptyList()) }

    LaunchedEffect(ud_tienda_shader,estados_carga_widget) {
        Log.d("ejecutamro_Carga_neuvaem","nueva_carga_datos")
        if (ud_tienda_shader != "") {
            mostrar_widget_tienda = true
            bloques_hoy= constantes_horas.obtenerBloquesDeHoy(datos_tienda.dia_hoy,datos_tienda.horario_tiendaMap)
            viewModel_filtado_tiendas.calcularHorarioParaTienda(
                ud_tienda_shader,
                datos_tienda.horario_tiendaMap
            )
        } else {
            mostrar_widget_tienda = false

        }

    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("dialgo_notificacion", "Permiso concedido")
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        guarar_token_user(uid, token)
                    } else {
                        Log.e("FCM1231312", "Usuario no registrado, no se guardará token")
                    }
                }
            }
        } else {
            Log.d("dialgo_notificacion", "Permiso denegado")

        }
    }

    LaunchedEffect(firebaseAuth.currentUser, dialgo_notificacion) {
        if ((firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) && dialgo_notificacion) {
            Log.d("dialgo_notificacion", "si hay user registrado y si hay si de permiso")
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val uid = firebaseAuth.currentUser?.uid ?: uid_respald_user ?: ""
                    if (uid != null) {
                        guarar_token_user(uid, token)
                    } else {
                        Log.e("FCM1231312", "Usuario no registrado, no se guardará token")
                    }
                }
            }

        } else {
            Log.d("dialgo_notificacion", "no hay registrado y no aparecio el perimos")

        }
    }

    if (!dialogo_notifi_ret) {
        permiso_primario_notifi(
            clik_si = {
                scope.launch {
                    sendNotificacion(context, true)
                    guarar_dialogo_notifi(context, true)
                }
                Log.d("clikeamos", "si")

            },
            clik_no = {
                scope.launch {
                    sendNotificacion(context, false)
                    guarar_dialogo_notifi(context, true)
                }
                Log.d("clikeamos", "no")
            },
            ondimis = {
                scope.launch {
                    guarar_dialogo_notifi(context, true)
                }
                Log.d("clikeamos", "ocultamos")
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 700.dp) // 🔥 este controla el ancho real
                .fillMaxHeight()
                .padding(start = 12.dp, end = 12.dp, top = 10.dp)
        ) {
            item {
                nombre_texto_img_perfil(
                    abrir_guardar_datos,
                    actulizacionE_stado_play,
                    datos_principales_user.nombre,
                    datos_principales_user.img_perfil
                )
            }
            stickyHeader() {
                ColumnContenedorComun {
                    texFiel_fake(listner_busqueda, toastShown)
                }
            }
            item {
                spacer_vertical(10.dp)
                filtrado_localidades(
                    esAniversarioHoy,
                    localidad_defaul, _obtener_filtrado_localidades, { localidad_selecionada ->
                        localidadSeleccionada.value = localidad_selecionada
                    }, { esAniversario ->
                        Log.d("esAniversarioaaaa", esAniversario.toString())
                        if (esAniversarioHoy != esAniversario) {
                            esAniversarioHoy = esAniversario
                        }
                    })



                spacer_vertical(20.dp)
            }

            item {
                spacer_vertical(10.dp)
                apartado_explora_cat(
                    stateCat,
                    _categorias_tiendas,
                    localidad_defaul,
                    datos_principales_user.nombre,
                    { nombre, localidad ->
                        categorias(localidad, nombre)
                    }, { categoria, localidad, nombre ->
                        clikear_cartas(categoria, localidad, nombre)
                    })

                spacer_vertical(20.dp)
            }

            item {
                if (mostrar_widget_tienda) {
                    spacer_vertical(10.dp)
                    baner_widget_tienda_geinz_baner(
                        datos_tienda,
                        viewModel_filtado_tiendas.horariosTiendas.collectAsState().value[datos_tienda.id_tienda]
                            ?: HorarioDia_box(),
                        constantes_horas.calcularHorasDiaLegible( viewModel_filtado_tiendas.horariosTiendas.collectAsState().value[datos_tienda.id_tienda]
                            ?: HorarioDia_box(),),bloques_hoy,
                        tick
                    )
                    spacer_vertical(20.dp)
                }
            }
            item {
                if (!mostrar_widget_tienda) {
                    spacer_vertical(10.dp)
                    baner_servicios_basicos_ { listner_sevicios_tramites(localidad_defaul) }
                    spacer_vertical(20.dp)
                }
            }
            item {
                rutas_turismo(
                    url_turistico_aleatoria ?: "",
                    "ver lugares",
                    "Descubre lugares en ${localidad_defaul}"

                ) {
                    ver_lugares(localidad_defaul)
                }
                spacer_vertical(30.dp)
            }

            item {
                if (mostrar_widget_tienda) {
                    baner_servicios_basicos_ { listner_sevicios_tramites(localidad_defaul) }
                    spacer_vertical(10.dp)
                }
            }

            item {
                spacer_vertical(10.dp)

                rutas_turismo(
                    urlAleatoria ?: "",
                    "Contactar",
                    "Salud y seguridad Pública"

                ) {
                    listener_seguridad(localidad_defaul)
                }

//                seguridad(
//                    imgActual,
//                    "Contactar",
//                    "Salud y seguridad Pública"
//                ) { listener_seguridad(localidad_defaul) }

                spacer_vertical(20.dp)
            }

            item {
                spacer_vertical(10.dp)
                baner_registra_tu_negocio {
                    mostar_bottom_sheet_ayuda_geinz = true
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
                .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
        )
        if (mostar_bottom_sheet_ayuda_geinz) {
            bottom_sheet_ayudanos_a_creccer(
                verificar_intener, ultimaLocalidad ?: "barranca",
                { mostar_bottom_sheet_ayuda_geinz = false }, viewModel_filtado_tiendas
            )
        }
    }

}


@Composable
fun apartado_explora_cat(
    stateCat: viewModel_principal_geinz_work.carga_categorias?,
    categorias_tienda: List<dataclass_cat_sub>,
    localidad_selecionada: String?,
    nombre_user: String,
    categorias1: (String, String) -> Unit,
    clikear_cartas: (String, String, String) -> Unit,
) {
    val localidad_defaul = localidad_selecionada ?: "barranca"
    Log.d("obtemloms_lista", categorias_tienda.toString())
    spacer_vertical(10.dp)

    Column {
        titulo_referenciales_geinz_work(
            "Explora ${localidad_defaul.capitalizeFirst()}",
            "Ver todos"
        ) { categorias1(nombre_user, localidad_defaul) }
        spacer_vertical(10.dp)
        Crossfade(targetState = stateCat, label = "crossfadeCategorias") { state ->
            when (state) {
                is viewModel_principal_geinz_work.carga_categorias.Loading -> {
                    carga_progres_categoria(130.dp, 190.dp)
                }

                is viewModel_principal_geinz_work.carga_categorias.succes -> {
                    cartas_filtrado(
                        nombre_user,
                        localidad_defaul,
                        categorias_tienda
                    ) { categoria, localidad, nombre ->
                        Log.d("localdiasdadas", "$categoria, $localidad ,$nombre")
                        clikear_cartas(categoria, localidad, nombre)
                    }
                }

                is viewModel_principal_geinz_work.carga_categorias.error -> {
                    Text(
                        text = "Error al cargar categorías",
                        color = Color.Red
                    )
                }

                else -> {
                    // Estado inicial (null)
                }
            }
        }

    }
}

@Composable
fun cartas_filtrado(
    nombre_user: String?,
    localidad_defaul: String,
    lista: List<dataclass_cat_sub>,
    carta_clikeada: (String, String, String) -> Unit
) {
    val alturaFija = 190.dp

    var cartaSeleccionada by remember { mutableStateOf<String?>(null) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { i ->
            val seleccionada = cartaSeleccionada == i.nombre

            val anchoAnimado by animateDpAsState(
                targetValue = if (seleccionada) 200.dp else 130.dp,
                label = "anchoCarta"
            )
            val fontSizeAnimada by animateFloatAsState(
                targetValue = if (seleccionada) 20f else 18f,
                label = "fontSizeAnimada"
            )

            Box(
                modifier = Modifier
                    .width(anchoAnimado)
                    .height(alturaFija)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        cartaSeleccionada = if (seleccionada) null else i.nombre
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(i.lista_img)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        contentDescription = "Imagen de la tienda",
                        modifier = Modifier.matchParentSize(), // ocupa todo el Box
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(anchoAnimado)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x33000000),
                                    Color(0x66000000),
                                    Color(0xDD000000)
                                )
                            )
                        )
                )

                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(5.dp)
                ) {
                    Text(
                        text = simplificarCategoria(i.nombre).capitalizeFirst(),
                        fontFamily = baners_geinz_work,
                        fontSize = fontSizeAnimada.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = if (seleccionada) TextOverflow.Clip else TextOverflow.Ellipsis
                    )


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${i.lista_subcategorias.size} categorías",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Label,
                            contentDescription = "Categorías",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }

                    spacer_vertical(15.dp)
                }

                AnimatedVisibility(
                    visible = seleccionada,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Button(
                        onClick = {
                            carta_clikeada(
                                i.nombre.toString(),
                                localidad_defaul,
                                nombre_user.toString().capitalizeFirst()
                            )
                        },
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Explorar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun texto_encimado_cartas(
    aniversario: Boolean,
    defecto_selecionado: Boolean,
    modifier: Modifier,
    titulo: String,
    descripcion: String,
) {
    Row(modifier = modifier.padding(horizontal = 12.dp)) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                texto_generico_multilinea(
                    titulo,
                    MaterialTheme.typography.banerGeinzWork,
                )

                if (defecto_selecionado) {
                    localidad_Selecionada()
                }
            }

            spacer_vertical(5.dp)

            Crossfade(
                targetState = descripcion,
                animationSpec = tween(durationMillis = 500)
            ) { textoAnimado ->
                texto_generico_one_line(
                    textoAnimado.capitalizeFirst(),
                    MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                )
            }

            spacer_vertical(10.dp)

            AnimatedContent(
                targetState = defecto_selecionado && aniversario,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600, delayMillis = 200)) togetherWith
                            fadeOut(animationSpec = tween(400))
                },
                label = "textoAniversario"
            ) { isVisible ->
                if (isVisible) {
                    texto_generico_one_line(
                        obtenerAniversarioLocalidad(titulo),
                        MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }


        }
    }

}

@Composable
fun texFiel_fake(listner_busqueda: () -> Unit, toastShown: Boolean) {

    val paddingAnim by animateDpAsState(
        targetValue = if (toastShown) 10.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = paddingAnim,
                end = paddingAnim,
                bottom = (paddingAnim - 5.dp).coerceAtLeast(0.dp)
            )
            .height(60.dp)
            .border(1.dp, Color(0xFF75707A), RoundedCornerShape(60))
            .clip(RoundedCornerShape(60))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                listner_busqueda()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(60.dp)
                .padding(start = 20.dp, end = 20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color(0xFF75707A)),
            )
            spacer_horizonta(10.dp)
            Text(
                "A dónde quieres llegar?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun filtrado_localidades(
    aniversario: Boolean,
    ultimaLocalidad: String,
    lista_localidades: List<localidades_filtrado>,
    nombre_localidad_selecionado: (String) -> Unit,
    clikeable: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetii))
    var localidad_defecto by rememberSaveable { mutableStateOf(ultimaLocalidad) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp > 600

//    var index by remember { mutableStateOf(0) }
    LaunchedEffect(ultimaLocalidad) {
        ultimaLocalidad.let { seleccionada ->
            localidad_defecto = seleccionada

            clikeable(false)

            val aniversarioHoy = esAniversarioHoy(seleccionada)
            Log.d("ANIVERSARIO", "Localidad: $seleccionada → $aniversarioHoy")

            clikeable(aniversarioHoy)

//            index = lista_localidades.indexOfFirst {
//                it.nombre.equals(seleccionada, ignoreCase = true)
//            }.coerceAtLeast(0)
//            if (index >= 0) {
//
//            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    if (lista_localidades.isNotEmpty()) {
        val index = lista_localidades.indexOfFirst {
            it.nombre.equals(ultimaLocalidad, ignoreCase = true)
        }.coerceAtLeast(0)

        val carouselState = rememberCarouselState(
            initialItem = index,
            itemCount = { lista_localidades.size }
        )
        Log.d("indexindex", index.toString())
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = screenWidth * 0.8f,
            itemSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) { index ->
            val item = lista_localidades[index]
            val randomImg = remember(item.lista_img) { item.lista_img.randomOrNull() }
            Box(
                modifier = Modifier
                    .fillMaxWidth() // 👈 más pequeño en tablets
                    .aspectRatio(1f)  // 👈 menos alto en tablets
                    .maskClip(RoundedCornerShape(20.dp))
                    .clickable {
                        scope.launch {
                            data_store_localidad.guardar_localida(context, item.nombre)
                            val newIndex =
                                lista_localidades.indexOfFirst { it.nombre == item.nombre }
                            if (newIndex >= 0) {
//                                listState.animateScrollToItem(newIndex)
                            }
                        }
                        localidad_defecto = item.nombre

                        nombre_localidad_selecionado(item.nombre)

                    }

            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(randomImg)

                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                    contentDescription = item.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .maskClip(RoundedCornerShape(20.dp)),


                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomCenter
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x66000000),
                                    Color(0xEE000000)
                                )
                            )
                        )
                )
                if (localidad_defecto.equals(item.nombre, ignoreCase = true) && aniversario) {
                    Log.d("aniversario", aniversario.toString())
                    LottieAnimation(
                        composition,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter)
                    )
                }

                val titulo = if (localidad_defecto.equals(
                        item.nombre,
                        ignoreCase = true
                    )
                ) {
                    "Estás aquí 👋"
                } else {
                    "Explorar"
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    texto_encimado_cartas(
                        aniversario,
                        localidad_defecto.equals(item.nombre, ignoreCase = true),
                        modifier = Modifier,
                        item.nombre.capitalizeFirst(),
                        titulo,
                    )
                    Spacer(modifier = Modifier.weight(.2f))
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }


    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun nombre_texto_img_perfil(
    abrir_guardar_datos: () -> Unit,
    actulizacionE_stado_play: Triple<String, Boolean, String>,
    nombre_user: String,
    img_url: String = ""
) {
    val contex = LocalContext.current
    var mostrar_bottom_sheet_new_version by remember { mutableStateOf(false) }
    val fraces = constantes_lista_localidades.lista_fraces_inicio
    var index by remember { mutableStateOf(0) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.saludo_user))

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }
    Row() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 10.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(15.dp)
                        .padding(bottom = 3.dp)
                )
                spacer_horizonta(5.dp)
                AnimatedContent(
                    targetState = nombre_user,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    }
                ) { nombre ->
                    texto_generico_one_line(
                        texto = constantes_lista_localidades.saludo_user_principal(nombre),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            abrir_guardar_datos()
                        },

                        )
                }
            }
            spacer_vertical(15.dp)
            Crossfade(targetState = fraces[index], label = "fraces") { txt ->
                AutoResizeOneLineText(
                    text = txt,
                    style = MaterialTheme.typography.busquedaGeinzWork
                )
            }
        }

        Box(modifier = Modifier
            .size(43.dp)
            .padding(end = 5.dp)) {


            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img_url)
                    .size(40)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.logo_geinz_500x500)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        if (actulizacionE_stado_play.second) {
                            mostrar_bottom_sheet_new_version = true
                        }
                    }, alignment = Alignment.BottomStart,
                contentScale = ContentScale.Crop
            )
            this@Row.AnimatedVisibility(
                actulizacionE_stado_play.second,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .align(Alignment.TopEnd)
                )
            }

        }
        spacer_vertical(5.dp)
    }
    if (mostrar_bottom_sheet_new_version) {
        verificar_version(
            context = contex,
            nueva_version = actulizacionE_stado_play.first,
            cambiosrealizados = actulizacionE_stado_play.third,
            ondimis = { mostrar_bottom_sheet_new_version = false })
    }

}

@Composable
fun AutoResizeOneLineText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 12.sp,
    maxTextSize: TextUnit = 32.sp
) {
    BoxWithConstraints(
        modifier = modifier.height(40.dp) // altura fija según tu diseño
    ) {
        val density = LocalDensity.current
        val scaledSize = with(density) {
            (maxWidth.toPx() / (text.length * 0.6f) / density.density)
                .coerceIn(minTextSize.value, maxTextSize.value)
                .sp
        }

        Text(
            text = text,
            style = style.copy(fontSize = scaledSize),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false
        )
    }
}


@Composable
fun carga_progres_categoria(anchoAnimado: Dp, alturaFija: Dp) {
    val cantidad_items = 5
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cantidad_items) {
            Box(
                modifier = Modifier
                    .width(anchoAnimado)
                    .height(alturaFija)
                    .clip(RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.geinzz.geinzwork.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.data.model.localizate_geinz.botom_shet_turismobtn
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_cecanas_km
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda

import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permisos_llamadas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.requestCallPermission
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.verificar_hora_abierta_ykm
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.firebaseAuth
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.retornar_color_estado_tienda
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.calcularDistanciaKm
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp_bottom_sheet_dialog
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.data_redes_tiendas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.lista_turismo_bottom_sheet
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_lugares_turisticos(
    viewmodelMap: viewmodel_mapa_personalizado,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos,
    datos: lugares_turisticos,
    visible: Boolean,
    onClose: () -> Unit,
    ver_mapa: (List<lugares_cercanos>) -> Unit,
    iniciar_seccion: () -> Unit,
    crear_cuenta: () -> Unit
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val viewmodel_filtrado: viewModel_filtado_tiendas = viewModel()
    val viewmodel_turismo = viewmodel_lugares_turisticos
    var id_tienda by remember { mutableStateOf("") }
    var localida_tienda by remember { mutableStateOf("") }
    var color_estado_tienda by remember { mutableStateOf(Color.Gray) }
    var mostrar_bottom_datos by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewmodel_filtrado._datos_tienda.observeAsState()
    val state_tiendas_cercanas by viewmodel_turismo.state_carga_tiendas_cercanas.collectAsState()
    val tick by viewmodel_filtrado.tick.collectAsState()
    val lista_general_completa by viewmodel_turismo._lista_general_completa.collectAsState()

    var nueva_busqueda by remember { mutableFloatStateOf(0f) }
    var buscar_nuevamente by remember { mutableStateOf(false) }
    var radioAnterior by remember { mutableStateOf(1.0) }
    var lista_subacteogorias by remember { mutableStateOf(emptyList<String>()) }
    var subcategoriatienda_select by remember { mutableStateOf("Todos") }
    var bottom_sheet_iniciar_seccion by remember { mutableStateOf(false) }
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

    LaunchedEffect(mostrar_bottom_datos) {
        if (mostrar_bottom_datos) {
            viewmodel_filtrado.obtener_campos_tiendas_por_id(
                localida_tienda ?: "barranca", id_tienda
            )
        }
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada = datosTienda!!.first()
        }
    }

    LaunchedEffect(Unit) {
        viewmodel_turismo.limpiar_tiendas_cercanas()
        viewmodel_turismo.obtener_tiendas_cercanas(datos.latitud, datos.longitud, 1.0, "barranca")
        viewmodelMap.setObjetoSeleccionado(datos)
    }

    LaunchedEffect(lista_general_completa) {
        if (lista_general_completa.isNotEmpty()) {
            viewmodel_turismo.mostrar_listas_completas(datos.latitud, datos.longitud)
        } else {
            Log.d("mostrar_listas", "🚫 No se llama aún, lista vacía inicial")
        }
    }

    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(visible) {
        if (visible) {
            cargando = true
            delay(2000)
            cargando = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = {},
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material.CircularProgressIndicator()
                }
            } else {
                AnimatedVisibility(visible = true) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 20.dp)
                    ) {
                        card_img_container(
                            viewmodel_turismo,
                            firebaseAuth1 = firebaseAuth,
                            datos = datos,
                            tick = tick,
                            lista_items = state_tiendas_cercanas,
                            clik_card = { id, localidad, color ->
                                if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {
                                    mostrar_bottom_datos = true
                                    id_tienda = id
                                    localida_tienda = localidad
                                    color_estado_tienda = color
                                } else {
                                    bottom_sheet_iniciar_seccion = true
                                }
                            },
                            buscar_nuevas_tiendas = { radio ->
                                if (radio != radioAnterior) {
                                    radioAnterior = radio
                                }
                            }, lista_base = { lista_baseparams, lista_sub ->
                                lista_subacteogorias = lista_sub
                            }, subcategoria_seleciondafun = { i ->
                                subcategoriatienda_select = i
                                viewmodel_turismo.filtrar_por_subcategoria(
                                    lista_subacteogorias,
                                    i,
                                    datos.latitud,
                                    datos.longitud, nueva_busqueda
                                )
                            }, nuevo_rango_km = { rango ->
                                nueva_busqueda = rango
                                viewmodel_turismo.filtrar_por_subcategoria(
                                    lista_subacteogorias,
                                    subcategoriatienda_select,
                                    datos.latitud,
                                    datos.longitud, rango
                                )
                                Log.d("Rangonuevo", nueva_busqueda.toString())
                            }, ver_mapa = { lugares_cercanos ->
                                ver_mapa(lugares_cercanos)
                            }, { bottom_sheet_iniciar_seccion = true })
                    }
                }
            }
        }
    }


    if (bottom_sheet_iniciar_seccion) {
        bottom_sheet_registrate(
            ondimis = {
                bottom_sheet_iniciar_seccion = false
            },
            iniciar_seccion_normal = {
                iniciar_seccion()
                bottom_sheet_iniciar_seccion = false
            },
            crear_cuenta_geinz = {
                crear_cuenta()
                bottom_sheet_iniciar_seccion = false
            },
            texto_bottom_Sheet = "Regístrate para ver los detalles completos y las funciones exclusivas"
        )
    }

    if (mostrar_bottom_datos) {
        bottom_sheet_tiendas_filtradas(
            viewmodel_filtrado,
            dataclass_tienda_seleccionada,
            mostrar_bottom_datos
        ) {
            mostrar_bottom_datos = false
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun card_img_container(
    viewmodel_turismo: viewModel_lugares_turisticos,
    firebaseAuth1: FirebaseAuth,
    datos: lugares_turisticos,
    tick: Long,
    lista_items: viewModel_lugares_turisticos.carga_tienda_cercanos,
    clik_card: (String, String, Color) -> Unit,
    buscar_nuevas_tiendas: (Double) -> Unit,
    lista_base: (List<lugares_cercanos>, List<String>) -> Unit,
    subcategoria_seleciondafun: (String) -> Unit,
    nuevo_rango_km: (Float) -> Unit,
    ver_mapa: (List<lugares_cercanos>) -> Unit,
    mostrar_login_seccion_bottom_sheet: () -> Unit
) {

    val firebaseAuth = FirebaseAuth.getInstance()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var call_dialog_permise by remember { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }
    var lat_tienda by remember { mutableStateOf(0.0) }
    var long_tienda by remember { mutableStateOf(0.0) }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    var mostrar_slider by remember { mutableStateOf(false) }
    var mostar_filtrado_categorias by remember { mutableStateOf(false) }
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    var nueva_busqueda by remember { mutableStateOf(1.0f) }
    var sub_categoria_selecionada by remember { mutableStateOf("Todos") }
//    val nueva_busqueda by viewmodel_turismo.estado_categoria_filtrada.collectAsState()
//    val sub_categoria_selecionada by viewmodel_turismo.estado_radio_filtrada.collectAsState()
//
    var lista_string_filtrado_tiendas by remember { mutableStateOf(emptyList<String>()) }

    var lugares_turisticos_filtrados by remember { mutableStateOf(emptyList<lugares_cercanos>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    var scope = rememberCoroutineScope()

    LaunchedEffect(sub_categoria_selecionada) {
        viewmodel_turismo.actualizarCategoria(sub_categoria_selecionada)
    }

    // Launcher para pedir permiso
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (lugares_turisticos_filtrados.isNotEmpty()) {
                ver_mapa(lugares_turisticos_filtrados)
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "No se encontraron lugares cercanos",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            spacer_vertical(10.dp)
            Text(
                text = datos.titulo,
                fontFamily = baners_geinz_work,
                fontSize = 30.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            texto_generico_multilinea(
                datos.descripcion,
                MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(10.dp)
            )
            abierto_flag("Abierto las 24h")
            spacer_vertical(10.dp)
            CollageGoogleMapsStyle(aspectRatio = 1.1f, with = 360.dp, imagenes = datos.lista_img)
            spacer_vertical(10.dp)
            chips_filtrado(lista_turismo_bottom_sheet) { i ->
                when (i) {
                    "Ir al lugar" -> {
                        lat_tienda = datos.latitud
                        long_tienda = datos.longitud
                        dialog_Crear_ruta = true
                    }

                    "ver en mapa" -> {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Funcionalidad en desarrollo. Disponible próximamente",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }

                    "compartir" -> {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Funcionalidad en desarrollo. Disponible próximamente",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
            spacer_vertical(10.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(horizontal = 10.dp)
            ) {
                spacer_vertical(10.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    texto_generico_multilinea(
                        "Lugares que no puedes perderte",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    texto_generico_one_line(
                        "ver en mapa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                if (lugares_turisticos_filtrados.isNotEmpty()) {
                                    if (firebaseAuth.currentUser != null) {
                                        ver_mapa(lugares_turisticos_filtrados)
                                        viewmodel_turismo.actualizarRadio(nueva_busqueda.toDouble())
                                        viewmodel_turismo.actualizar_lat_lugar(datos.latitud)
                                        viewmodel_turismo.actualizar_lng_lugar(datos.longitud)
                                    } else {
                                        mostrar_login_seccion_bottom_sheet()
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No se encontraron lugares cercanos",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            } else {
                                permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                        }
                    )
                }

                spacer_vertical(10.dp)

                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("Descubre sitios cercanos a ${datos.titulo} ")
                    }

                    pushStringAnnotation(tag = "filtrar", annotation = "filtrar")
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("filtra")
                    }
                    pop()

                    withStyle(style = SpanStyle(color = Color.White)) {
                        append(" y disfruta lo mejor a menos de ")
                    }

                    pushStringAnnotation(
                        tag = "RADIO",
                        annotation = nueva_busqueda.toInt().toString()
                    )
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("${nueva_busqueda.toInt()} Km")
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    onClick = { offset ->

                        annotatedText.getStringAnnotations(
                            tag = "filtrar",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { _ ->
                                println("Se hizo clic en filtrar")
                                mostrar_slider = false
                                mostar_filtrado_categorias = !mostar_filtrado_categorias
                            }

                        annotatedText.getStringAnnotations(
                            tag = "RADIO",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { annotation ->
                                mostar_filtrado_categorias = false
                                mostrar_slider = !mostrar_slider

                            }
                    }
                )



                spacer_vertical(10.dp)
                Box() {
                    this@Column.AnimatedVisibility(
                        mostrar_slider,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column() {
                            Slider(
                                value = nueva_busqueda,
                                onValueChange = { nueva_busqueda = it.roundToInt().toFloat() },
                                valueRange = 1f..10f,
                                steps = 8,
                                onValueChangeFinished = {
                                    buscar_nuevas_tiendas(nueva_busqueda.toDouble())
                                    nuevo_rango_km(nueva_busqueda)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,       // 🔹 Color del "thumb" o bolita que se mueve cuando arrastras el slider
                                    activeTrackColor = MaterialTheme.colorScheme.primary, // 🔹 Color de la línea activa del slider (la parte a la izquierda del thumb)
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.2f
                                    ), // 🔹 Color de la línea inactiva (parte a la derecha del thumb)
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
                                        texto_generico_one_line(
                                            nueva_busqueda.toInt().toString(),
                                            color = Color.Black,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            )


                        }
                    }
                    this@Column.AnimatedVisibility(
                        mostar_filtrado_categorias,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            items(lista_string_filtrado_tiendas) { i ->
                                val selecionado = sub_categoria_selecionada == i
                                chisp_filtrado_busqueda(
                                    selecionado,
                                    simplificarCategoria(i),
                                    false,
                                    clik_card = {
                                        coroutineScope.launch {
                                            if (!selecionado) {
                                                if (i == "Todos") {
                                                    sub_categoria_selecionada = "Todos"
                                                    subcategoria_seleciondafun("Todos")
                                                    listState.scrollToItem(0)
                                                } else {
                                                    sub_categoria_selecionada = i
                                                    subcategoria_seleciondafun(i)
                                                    listState.scrollToItem(0)
                                                }
                                            }
                                        }
                                    },
                                    onClick_delete = {},
                                    true,
                                    40.dp
                                )

                            }
                        }
                    }
                }

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp), contentAlignment = Alignment.Center
            ) {
                when (val state = lista_items) {
                    is viewModel_lugares_turisticos.carga_tienda_cercanos.loading -> {
                        CircularProgressIndicator()
                    }

                    is viewModel_lugares_turisticos.carga_tienda_cercanos.empty -> {
                        Text(
                            state.txt,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }

                    is viewModel_lugares_turisticos.carga_tienda_cercanos.error -> {
                        Text(
                            state.texto,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }

                    is viewModel_lugares_turisticos.carga_tienda_cercanos.succes -> {
                        lista_base(state.lista_lugares, state.lista_categorias)
                        lugares_turisticos_filtrados = state.lista_lugares
                        viewmodel_turismo.actualizarCategorias(state.lista_categorias)
                        viewmodel_turismo.actualizarListaCompleta(state.lista_completa_lugares)
                        Log.d("listassssssss", "${state.lista_lugares} ${state.lista_categorias}")
                        val lista_subcat = listOf("Todos") + state.lista_categorias
                        lista_string_filtrado_tiendas = lista_subcat
                        Column {
                            val listaOrdenada = state.lista_lugares.sortedWith(
                                compareByDescending { it.esta_abierto }
                            )

                            spacer_vertical(10.dp)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                state = listState,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 8.dp)
                            ) {
                                items(listaOrdenada, key = { it.id_tienda }) { item ->
                                    item_cercanos(
                                        firebaseAuth,
                                        expanded = expandedItemId == item.id_tienda,
                                        tick,
                                        datos = datos,
                                        item = item,
                                        onExpand = { id ->
                                            expandedItemId = if (expandedItemId == id) null else id
                                        },
                                        clik_card = { id, localidad, color ->
                                            coroutineScope.launch {
                                                clik_card(id, localidad, color)
                                            }
                                        },
                                        clik_icono = { i ->
                                            when (i.nombre_red) {
                                                "llamar" -> {
                                                    llamar(context, i.valor, {
                                                        call_dialog_permise = true
                                                        numero_llamada = i.valor
                                                    })
                                                }

                                                "whatsapp" -> {
                                                    abrir_whattsapp(context, i.valor)
                                                }

                                                "tiktok" -> {
                                                    openTiktok(
                                                        context, i.valor
                                                    )
                                                }

                                                "facebook" -> {
                                                    openFacebook(
                                                        context, i.valor
                                                    )
                                                }

                                                "instagram" -> {
                                                    openInstagram(
                                                        context, i.valor
                                                    )
                                                }

                                                "Web" -> {
                                                    openWebLink(
                                                        context, i.valor
                                                    )
                                                }

                                            }
                                        },
                                        click_crear_ruta = { lat, log ->
                                            lat_tienda = lat
                                            long_tienda = log
                                            dialog_Crear_ruta = true
                                        }, mostrar_dialog_registro = {
                                            mostrar_login_seccion_bottom_sheet()
                                        })
                                }
                            }
                        }
                    }
                }
            }



            Log.d("obtemos_cordenads", "${datos.latitud}, ${datos.longitud}")

        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
    if (dialog_Crear_ruta) {
        dialog_crear_ruta_lugares({ dialog_Crear_ruta = false }, { crear_ruta ->
            dialog_Crear_ruta = false
            if (crear_ruta && verificarUbiActiva(context)) {
                constantes_lista_localidades.abrir_google_maps(
                    context, lat_tienda, long_tienda,
                ) { dialogo ->

                }
            } else {
                validacion_mostrar_dialog_ubi_off = true
            }
        })
    }
    if (validacion_mostrar_dialog_ubi_off) {
        dialog_sin_ubi__rutas(
            "Para una mejor experiencia y poder mostrar tu ubicación actual en el mapa, por favor habilita la función de ubicación en tu dispositivo.",
            { validacion_mostrar_dialog_ubi_off = false },
            {
                validacion_mostrar_dialog_ubi_off = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            })
    }
    if (call_dialog_permise) {
        permisos_llamadas(aceptar_permisos = {
            requestCallPermission(context = context, phoneNumber = numero_llamada)
        }, ondimis = {
            call_dialog_permise = false
        })
    }
}

@Composable
fun item_cercanos(
    firebaseAuth: FirebaseAuth,
    expanded: Boolean,
    tick: Long,
    datos: lugares_turisticos,
    item: lugares_cercanos,
    onExpand: (String) -> Unit,
    clik_card: (String, String, Color) -> Unit,
    clik_icono: (data_redes_tiendas) -> Unit,
    mostrar_dialog_registro: () -> Unit,
    click_crear_ruta: (lat: Double, long: Double) -> Unit
) {

    var estado_color by remember { mutableStateOf(Color.Gray) }
    var mostar_dialog_km by remember { mutableStateOf(false) }
    var datosdialog_km by remember { mutableStateOf(tiendas_cecanas_km()) }
    val animatedWidth by animateDpAsState(
        targetValue = if (expanded) 200.dp else 160.dp, label = "widthAnim"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (expanded) 270.dp else 160.dp, label = "heightAnim"
    )
    val widthImg by animateDpAsState(
        targetValue = if (expanded) 130.dp else 160.dp, label = "heightAnim"
    )

    Column {
        Box(
            modifier = Modifier
                .width(animatedWidth)
                .height(animatedHeight)
                .animateContentSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (expanded) 16.dp else 0.dp,
                        bottomEnd = if (expanded) 16.dp else 0.dp
                    )
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        clik_card(item.id_tienda, "barranca", estado_color)
                    }) {
                Box(
                    modifier = Modifier
                        .width(widthImg)
                        .height(animatedHeight)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.logo_tienda).placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias).build(),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (expanded) 16.dp else 20.dp,
                                    bottomEnd = if (expanded) 16.dp else 20.dp
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onExpand(item.id_tienda)
                            }
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop

                    )
                    val distanciaKm = calcularDistanciaKm(
                        datos.latitud, datos.longitud, item.latitud, item.longitud
                    )
                    spacer_horizonta(5.dp)
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                mostar_dialog_km = true
                                datosdialog_km = tiendas_cecanas_km(
                                    img_tienda = item.logo_tienda,
                                    nombre_tienda = item.nombre_tienda,
                                    kl = "%.2f km".format(distanciaKm),
                                    nombre_lugar = datos.titulo,
                                    color = estado_color,
                                    horario_total = item.horario_dia,
                                    hora_cierre = item.horario_dia.h_cierre,
                                    cerrado = item.horario_dia.cerrado,
                                    motivo = item.horario_dia.motivo,
                                    tick = tick
                                )
                            }
                            .align(Alignment.BottomCenter)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(10.dp)
                                .animateContentSize()
                        ) {
                            retornar_color_estado_tienda(
                                horario_total = item.horario_dia,
                                hCierre = item.horario_dia.h_cierre,
                                cerrado = item.horario_dia.cerrado,
                                motivo = item.horario_dia.motivo,
                                tick = tick
                            ) { color ->
                                estado_color = color
                            }
                            texto_generico_one_line(
                                "A: %.2f km".format(distanciaKm),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            spacer_horizonta(5.dp)
                            AnimatedVisibility(
                                visible = !expanded, enter = fadeIn(), exit = fadeOut()
                            ) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(estado_color)
                                )
                            }
                        }

                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
                ) {
                    val lista_redes_tiendas = listOf(
                        data_redes_tiendas(
                            enable = item.contacto_tienda.llamada.estado,
                            icono = R.drawable.llamada_icon,
                            nombre_red = "llamar",
                            valor = item.contacto_tienda.llamada.numero
                        ), data_redes_tiendas(
                            enable = item.contacto_tienda.whatsapp.estado,
                            icono = R.drawable.whatsapp_icon,
                            nombre_red = "whatsapp",
                            valor = item.contacto_tienda.whatsapp.numero
                        ), data_redes_tiendas(
                            enable = item.contacto_tienda.tiktok.estado,
                            icono = R.drawable.tik_tok_icon,
                            nombre_red = "tiktok",
                            valor = item.contacto_tienda.tiktok.url
                        ), data_redes_tiendas(
                            enable = item.contacto_tienda.facebook.estado,
                            icono = R.drawable.facebook_icon,
                            nombre_red = "facebook",
                            valor = item.contacto_tienda.facebook.url
                        ), data_redes_tiendas(
                            enable = item.contacto_tienda.instagram.estado,
                            icono = R.drawable.instagram_icon,
                            nombre_red = "instagram",
                            valor = item.contacto_tienda.instagram.url
                        ), data_redes_tiendas(
                            enable = item.contacto_tienda.sitio_web.estado,
                            icono = R.drawable.sitio_web,
                            nombre_red = "web",
                            valor = item.contacto_tienda.sitio_web.url
                        )
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)

                    ) {
                        items(lista_redes_tiendas.filter { it.enable }) { i ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(i.icono),
                                    contentDescription = i.nombre_red,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)

                                        .clickable {
                                            if (firebaseAuth.currentUser != null) {
                                                clik_icono(i)
                                            } else {
                                                mostrar_dialog_registro()
                                            }
                                        })
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        click_crear_ruta(item.latitud, item.longitud)
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar, // ejemplo: ícono de “+”
                                    contentDescription = "Agregar red", tint = Color.White
                                )
                            }
                        }
                    }


                }
            }
        }
        AnimatedVisibility(
            !expanded,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.zIndex(-1f)
        ) {
            Column(
                modifier = Modifier
                    .width(widthImg)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { clik_card(item.id_tienda, "barranca", estado_color) },
            ) {
                spacer_vertical(7.dp)
                texto_generico_one_line(
                    item.nombre_tienda, MaterialTheme.typography.titleMedium
                )
                spacer_vertical(7.dp)
                texto_generico_one_line(
                    item.categoria, MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(7.dp)
                tags_subcateogiras(
                    item.lista_subcategoiras,
                    brush_start = Brush.horizontalGradient(colors = shadow_top_filtrado_v1),
                    brush_end = Brush.horizontalGradient(colors = shadow_botonm_filtrado_v1)
                )
                spacer_vertical(7.dp)
            }
        }
    }
    if (mostar_dialog_km) {
        verificar_hora_abierta_ykm(datosdialog_km, { mostar_dialog_km = false })
    }
}


@Composable
fun chips_filtrado(list: List<botom_shet_turismobtn>, item_clikeado: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(
            start = 7.dp, end = 7.dp, top = 8.dp, bottom = 8.dp
        ),
    ) {
        items(list) { i ->
            val enable_color =
                if (i.enable) MaterialTheme.colorScheme.primary else Color(0xFF4F4F4F)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(enable_color)
                    .height(45.dp)
                    .padding(horizontal = 15.dp, vertical = 10.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        item_clikeado(i.txt)
                    }, verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = i.icono,
                    contentDescription = i.txt,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )

                spacer_horizonta(8.dp)

                texto_generico_one_line(
                    i.txt.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

    }
}


@Composable
fun abierto_flag(texto: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .clip(CircleShape)

            .background(Color(0xFF43A047))
        // Verde intermedio, más natural
    ) {
        texto_generico_one_line(
            texto.capitalizeFirst(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color.White
        )
    }
}
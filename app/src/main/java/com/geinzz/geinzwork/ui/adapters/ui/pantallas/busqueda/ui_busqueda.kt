package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda


import Item
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dialog_seguridad_salud_algolia
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ImagenesSuperpuestasCollage
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_close_gris
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_salud_seguridad_algolia
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialogo_cabiar_rango_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_ayudanos_a_creccer
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textos_titulos_geinz_wokr
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularTiempoRestante
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.categorias_defaul
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.geohashing
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isGPSEnabled
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerUbicacionEnTiempoReal
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.SearchViewModel
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmodel_floating_filtrado
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ui_pantalla_busqueda(
    verificar_intener: Boolean,
    viewmodelMap: viewmodel_mapa_personalizado,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos,
    localida_defauld: datos_principales_user,
    focusRequester: FocusRequester,
    ocultar: () -> Unit,
    estado_mostar: Boolean,
    iniciar_seccion_normal: () -> Unit,
    crear_cuenta_geinz: () -> Unit,
    abrir_mapa: (String) -> Unit,
    iniciar_seccion: () -> Unit,
    crear_cuenta: () -> Unit
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    var primeraVezCercaDeTi by rememberSaveable { mutableStateOf(true) }
    var mostrar_dialog_cambiar_radio by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val viewmodel_floating_filtrado: viewmodel_floating_filtrado = viewModel()
    val cerca_de_ti_enable =
        viewmodel_floating_filtrado.cerca_de_ti_enable.collectAsState(initial = false)

    val viewModel: SearchViewModel = viewModel()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val items: List<Item>
    val categorias: List<String>
    when (state) {
        is SearchViewModel.ListItemsResult.Success -> {

            val succes = state as SearchViewModel.ListItemsResult.Success
            items = succes.items
            categorias = succes.categorias
            Log.d("items_result", "${items.size}")
        }

        else -> {
            items = emptyList()
            categorias = emptyList()
        }
    }

    val categoria_filtrado by viewModelFiltros._subcategoria_filtrado.observeAsState()
    var subcategira_filtrado by rememberSaveable { mutableStateOf("") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var salud_seguirdad by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val horario_por_tienda by viewModelFiltros.estadoTiendas.observeAsState()
    val datos_numeros_salud_seguridad by viewModelFiltros.instance_salud_seguridad.collectAsState()
    val datos_lugares_turisticos by viewModelFiltros.instance_lugar_turistico.collectAsState()

    var subir_btn by remember { mutableStateOf(false) }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    val tick by viewModelFiltros.tick.collectAsState()
    val ultimaLocalidad by data_store_localidad.obtener_localidad(context)
        .collectAsState(initial = null)

    var tiendaLocalidadSeleccionada by remember { mutableStateOf<String?>(null) }
    val localida_filtrado_guardado by viewModel.localida_filtrado_guardado.collectAsState()
    LaunchedEffect(ultimaLocalidad) {
        if (ultimaLocalidad != null) {
            // Si la localidad del DataStore es diferente a la del ViewModel
            if (ultimaLocalidad != localida_filtrado_guardado && localida_filtrado_guardado.isNotEmpty()) {
                Log.d("user", "Cambio viene del ViewModel (no limpiar filtros)")
                tiendaLocalidadSeleccionada = localida_filtrado_guardado
            } else {
                Log.d("user", "Cambio viene del usuario o sigue igual (actualizar sin limpiar)")
                tiendaLocalidadSeleccionada = ultimaLocalidad
            }
        }
    }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    var localidad_Anterior_select by remember { mutableStateOf(tiendaLocalidadSeleccionada) }
    var categoria_filtrad by remember { mutableStateOf("") }
    Log.d("camibamos", "${categoria_filtrad} ${localidad_Anterior_select}")
    var id_tienda_selecionada by remember { mutableStateOf("") }
    var firstLaunch by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var expandedFloatingMenuFadeDemo by remember { mutableStateOf(false) }
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    var subcategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    var cat_sub_seleciondo by remember { mutableStateOf(false) }

    var color_categoria by remember { mutableStateOf(false) }
    var color_localidad by remember { mutableStateOf(false) }
    var color_subcategoria by remember { mutableStateOf(false) }
    var color_salud_seguirdad by remember { mutableStateOf(false) }
    var mostrar_centrado_visible by remember { mutableStateOf(true) }

    var localidad_tienda_seklecioanda by remember { mutableStateOf("") }

    var placeholder by remember { mutableStateOf("A dónde quieres ir?") }

    var previousLocalidad by remember { mutableStateOf<String?>(null) }
    var aler_dialog_contacto by remember { mutableStateOf(false) }
    var nombre_seguridad_salud by remember { mutableStateOf("") }
    var localidad_seguridad_salud by remember { mutableStateOf("") }
    var id_seguridad_salud by remember { mutableStateOf("") }
    var img_seguirdad_salud by remember { mutableStateOf("") }

    var id_lugar_turistico_select by remember { mutableStateOf("") }
    var localdad_llugar_turistico by remember { mutableStateOf("") }
    var bottom_sheet_turismo by remember { mutableStateOf(false) }
    var lat_user by remember { mutableStateOf<Double?>(null) }
    var log_user by remember { mutableStateOf<Double?>(null) }
    var hash_user by remember { mutableStateOf<String?>(null) }

    val radioGuardado by data_store_localidad.get_radio_user(context)
        .collectAsState(initial = 1f)
    // 👇 este estado se actualiza automáticamente cuando cambia el valor guardado
    var radioActual by remember { mutableStateOf(1f) }
    var radio_cambiado by remember { mutableStateOf(1f) }


    val categoria_retorno_viewmodel by viewModel.categoria_retorno.collectAsState()
    val subcategoria_retorno_viewmodel by viewModel.subcategoria.collectAsState()
    val txt_filtrado_guardado by viewModel.txt_filtrado_guardado.collectAsState()


    var cambioDesdeViewModel by remember { mutableStateOf(false) }
    var mostar_bottom_sheet_ayuda_geinz by remember { mutableStateOf(false) }
    var id_tienda_crear_ruta by remember { mutableStateOf("") }
    var localidad_tienda_crear_ruta by remember { mutableStateOf("") }

    LaunchedEffect(localida_filtrado_guardado) {
        if (localida_filtrado_guardado.isNotEmpty()) {
            cambioDesdeViewModel = true
            tiendaLocalidadSeleccionada = localida_filtrado_guardado
            Log.d("sync", "Localidad forzada desde ViewModel: $localida_filtrado_guardado")
        }
    }

    LaunchedEffect(
        categoria_retorno_viewmodel,
        subcategoria_retorno_viewmodel,
        txt_filtrado_guardado, localida_filtrado_guardado
    ) {
        if (categoria_retorno_viewmodel.isNotEmpty() || subcategoria_retorno_viewmodel.isNotEmpty() || txt_filtrado_guardado.isNotEmpty()) {
            categoria_filtrad = categoria_retorno_viewmodel
            subcategira_filtrado = subcategoria_retorno_viewmodel
            searchText = TextFieldValue(txt_filtrado_guardado)
//            tiendaLocalidadSeleccionada=localida_filtrado_guardado
            Log.d("valorr", txt_filtrado_guardado)
            Log.d("valorr", "${searchText.text}")
        }
    }

    // Cuando el valor de DataStore cambia, actualizamos el radioActual (solo si el usuario no está moviendo el slider)
    LaunchedEffect(radioGuardado) {
        radioActual = radioGuardado
    }
    var hasing_user_user_filtrado by remember { mutableStateOf("") }
    val estadoGPS by viewmodel_floating_filtrado.gpsActivo.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")
            obtenerUbicacionEnTiempoReal(estadoGPS, context, { lat, lng ->
                Log.d("lat_log_user", "$lat $lng")
                hash_user = geohashing(lat, lng)
                val hora = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                scope.launch {
                    data_store_localidad.guardar_hasgin_lat_lon_user(context, hash_user ?: "", hora)
                    data_store_localidad.guardar_lat_log_user(context, lat, lng)
                }
            }, {})
//            cerca_de_ti_enable = true
            viewmodel_floating_filtrado.save_cerca_de_ti(true)
        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")
//            cerca_de_ti_enable = false
            viewmodel_floating_filtrado.save_cerca_de_ti(false)
        }
    }
    LaunchedEffect(radio_cambiado) {
        searchText = TextFieldValue("")

    }


    LaunchedEffect(cerca_de_ti_enable.value) {
        Log.d("FiltroRadioEffect", "🚀 LaunchedEffect disparado")
        Log.d("FiltroRadioEffect", "cerca_de_ti_enable = $cerca_de_ti_enable")
        Log.d("FiltroRadioEffect", "categoria_filtrad = $categoria_filtrad")
        Log.d("FiltroRadioEffect", "subcategira_filtrado = $subcategira_filtrado")
        if (cerca_de_ti_enable.value) {
            searchText = TextFieldValue("")
            Log.d("FiltroRadioEffect", "Switch Cerca de Ti ACTIVADO")
            obtenerUbicacionEnTiempoReal(estadoGPS, context, { lat, lng ->
                Log.d("lat_log_user", "$lat $lng")
                hash_user = geohashing(lat, lng)
                val hora = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                scope.launch {
                    data_store_localidad.guardar_hasgin_lat_lon_user(context, hash_user ?: "", hora)
                    data_store_localidad.guardar_lat_log_user(context, lat, lng)
                }
            }, {})
            if (categoria_filtrad.isNotEmpty() || subcategira_filtrado.isNotEmpty()) {
                Log.d(
                    "FiltroRadioEffect",
                    "Hay categorías o subcategorías seleccionadas, filtrando por radio..."
                )
                viewModel.filtrar_por_radio(
                    radioActual,
                    context,
                    categoria_filtrad,
                    subcategira_filtrado,
                    cerca_de_ti_enable.value,
                    hash_user
                )
            } else {
                Log.d(
                    "FiltroRadioEffect",
                    "No hay categorías ni subcategorías seleccionadas, no se filtra aún"
                )
            }
        } else {
            Log.d("FiltroRadioEffect", "Switch Cerca de Ti DESACTIVADO")
            if (categoria_filtrad.isEmpty() && subcategira_filtrado.isEmpty()) {
                Log.d(
                    "FiltroRadioEffect",
                    "No hay filtros seleccionados, limpiando todos los resultados"
                )
                Log.d(
                    "clearResults",
                    "borramos dentro del launcher efect"
                )
                viewModel.clearResults()
            } else {
                Log.d(
                    "FiltroRadioEffect",
                    "Hay filtros de categoría/subcategoría, restaurando lista original"
                )
                viewModel.restaurarListaOriginal(categoria_filtrad, subcategira_filtrado)
            }
        }

        Log.d("FiltroRadioEffect", "✅ LaunchedEffect finalizado")
    }


    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            verificarGPS(context, launcher)
//            Toast.makeText(context, "Verificamos que el gps este activo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(
        tiendaLocalidadSeleccionada,
        categoria_filtrad,
        subcategira_filtrado,
        salud_seguirdad
    ) {
        val localidadActual = tiendaLocalidadSeleccionada

        val tieneDatosGuardados = localida_filtrado_guardado.isNotEmpty() ||
                categoria_retorno_viewmodel.isNotEmpty() ||
                subcategoria_retorno_viewmodel.isNotEmpty() ||
                txt_filtrado_guardado.isNotEmpty()

        Log.d("categoria_filtrad", "$categoria_filtrad $subcategira_filtrado")
        Log.d("localidadActual", "$localidadActual")

        if (cambioDesdeViewModel) {
            Log.d("sync", "Ignorando limpieza porque el cambio vino del ViewModel")
            previousLocalidad = localidadActual
            cambioDesdeViewModel = false
            return@LaunchedEffect
        }
        if (salud_seguirdad.isNotEmpty()) {
//            Log.d(
//                "clearResults",
//                "si seguirdad es vacia"
//            )
//            viewModel.clearResults()
//            salud_seguirdad = ""
//            Log.d("_cabiamos_localida", "$salud_seguirdad,$categoria_filtrad,$subcategira_filtrado")
//            cerca_de_ti_enable = false
            viewmodel_floating_filtrado.save_cerca_de_ti(false)
        }
        if (localidadActual != previousLocalidad && (categoria_filtrad.isEmpty() || subcategira_filtrado.isEmpty())) {
            Log.d(
                "clearResults",
                "si cateogira esta vacia y sub igual"
            )
            viewModel.clearResults()
            mostrar_centrado_visible = true
            previousLocalidad = localidadActual

            searchText = TextFieldValue("")
            categoria_filtrad = ""
            subcategira_filtrado = ""
            salud_seguirdad = ""

            return@LaunchedEffect
        }

        if (localidadActual != previousLocalidad) {
            searchText = TextFieldValue("")
            categoria_filtrad = ""
            subcategira_filtrado = ""
            salud_seguirdad = ""
        }

        if (firstLaunch) {
            firstLaunch = false
            return@LaunchedEffect
        }


        searchText = TextFieldValue("")


        // 🔹 Placeholder dinámico
        placeholder = if (
            categoria_filtrad.isNotEmpty() ||
            subcategira_filtrado.isNotEmpty() ||
            salud_seguirdad.isNotEmpty()
        ) {
            "Ingresa el nombre"
        } else {
            "A dónde quieres ir?"
        }

        // 🔹 Caso especial: si hay salud/seguridad, tomarlo como categoría
        val categoriaFinal = if (salud_seguirdad.isNotEmpty()) {
            salud_seguirdad
        } else {
            categoria_filtrad
        }

        // 🔹 Llamar solo una vez si hay categoría/subcategoría
        if (categoriaFinal.isNotEmpty() || subcategira_filtrado.isNotEmpty()) {
            Log.d("buscamosen", "entramos_condiocn")
            viewModel.filtrarSubCat(
                radioActual,
                context,
                hash_user,
                cerca_de_ti_enable.value,
                tiendaLocalidadSeleccionada ?: "barranca",
                categoriaFinal,
                subcategira_filtrado
            )
        } else {
            Log.d(
                "clearResults",
                "si categoira final y sub categoira filtrado esta vacio otra vez"
            )
            viewModel.clearResults()
        }
    }

    LaunchedEffect(aler_dialog_contacto) {
        if (aler_dialog_contacto) {
            viewModelFiltros.obtener_numeros_seguridad_salud(
                localidad_seguridad_salud,
                id_seguridad_salud
            )
        }
    }
    val ultima_cordenada_actualziada by data_store_localidad.obtener_hashing_user(context)
        .collectAsState(initial = null)
    LaunchedEffect(ultima_cordenada_actualziada, cerca_de_ti_enable.value) {
        if (ultima_cordenada_actualziada != null && cerca_de_ti_enable.value) {
            Log.d("cambiamos_hasuser", "📍 Nueva coordenada: $ultima_cordenada_actualziada")
            viewModel.filtrar_por_radio(
                radioActual,
                context,
                categoria_filtrad,
                subcategira_filtrado,
                cerca_de_ti_enable.value,
                ultima_cordenada_actualziada
            )
        } else {
            Log.d("cambiamos_hasuser", "⚠️ No hay coordenada o cerca_de_ti_enable = false")
//         viewmodel_floating_filtrado.limpiar_valor_save_cerca_de_ti()
        }
    }


    LaunchedEffect(categoria_filtrad) {
        subcategorias = viewModelFiltros.obtener_lista_sub(categoria_filtrad)
    }

    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localidad_tienda_seklecioanda,
                id_tienda_selecionada
            )
        }
    }

    LaunchedEffect(bottom_sheet_turismo) {
        if (bottom_sheet_turismo) {
            viewModelFiltros.obtener_datos_lugares_turisticos(
                id_lugar_turistico_select,
                localdad_llugar_turistico
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModelFiltros.obtener_categorias()
        viewModelFiltros.obtener_cat_lugares()
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada = datosTienda!!.first()
            viewModelFiltros.cast_horario_atencion_horario_tienda(datosTienda!!.first().horario_atencion)
        }
    }
    LaunchedEffect(cat_sub_seleciondo) {
        if (cat_sub_seleciondo) {
            mostrar_centrado_visible = false
            Log.d("BusquedaFlow", "Texto <2 pero cat_sub=true -> centrado oculto")

        } else {
            mostrar_centrado_visible = true

        }
    }

    Box() {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    fraces_filtrado(expandedFloatingMenuFadeDemo)
                    spacer_vertical(10.dp)
                    TexfielFiltrado(
                        cat_sub_seleciondo,
                        placeholder,
                        focusRequester,
                        searchText,
                        { it ->
                            searchText = TextFieldValue(
                                text = it,
                                selection = TextRange(it.length)
                            )
                            if (it.isNotEmpty() && it.length >= 2) {
                                Log.d("entramos123123", "1mayor")
                                mostrar_centrado_visible = false
                                if (!cat_sub_seleciondo) {
                                    Log.d("entramos123123", "1menor")
                                    viewModel.buscarItems(
                                        radioActual,
                                        context,
                                        cerca_de_ti_enable.value,
                                        hash_user,
                                        false,
                                        tiendaLocalidadSeleccionada ?: "barranca",
                                        null,
                                        null,
                                        searchText.text,

                                        )
                                } else {
                                    Log.d("entramos123123", "2menor")
                                    viewModel.buscarItems(
                                        radioActual,
                                        context,
                                        cerca_de_ti_enable.value,
                                        hash_user,
                                        true,
                                        tiendaLocalidadSeleccionada ?: "barranca",
                                        categoria_filtrad.ifEmpty { salud_seguirdad },
                                        subcategira_filtrado,
                                        searchText.text,
                                    )
                                }
                            } else {
                                // 📝 Si no hay texto suficiente (<2)
                                Log.d("entramos123123", "2")
                                if (cat_sub_seleciondo) {

                                    mostrar_centrado_visible = false
                                    viewModel.buscarItems(
                                        radioActual,
                                        context,
                                        cerca_de_ti_enable.value,
                                        hash_user,
                                        true,
                                        tiendaLocalidadSeleccionada ?: "barranca",
                                        categoria_filtrad.ifEmpty { salud_seguirdad },
                                        subcategira_filtrado,
                                        "",
                                        // 🔥 búsqueda vacía
                                    )

                                } else {
                                    // 👉 No hay cat/sub seleccionado → limpio
                                    Log.d("entramos123123", "limpiamoscartas en si ")
                                    mostrar_centrado_visible = true
                                    Log.d(
                                        "clearResults",
                                        "no hay cat ni sub se limpia"
                                    )
                                    viewModel.clearResults()
                                }
                            }


                        },
                        listener_borrar_texto = {
                            viewModel.clearResults()
                            Log.d(
                                "clearResults",
                                "caundo borramos el texto completo con el boton de borrar"
                            )
                        })

                    spacer_vertical(5.dp)

                    filtrado_chips(
                        viewModel = viewModel,
                        searchText = searchText.text,
                        lista_filtrado = categorias,
                        salud_seguirdad = salud_seguirdad,
                        lista_subcategoria = subcategorias,
                        categoria_selecionada = categoria_filtrad,
                        categoria_selecionada_fun = { filtrado_Select ->
                            categoria_filtrad = filtrado_Select
                        },
                        subcategoria_selecionada = subcategira_filtrado,
                        subcateogira_selecionada_fun = { filtrado_subcategoria_select ->
                            subcategira_filtrado = filtrado_subcategoria_select
                        },
                        cat_sub_select = { hay_selecccion ->
                            cat_sub_seleciondo = hay_selecccion
                        },
                        seguridad_salud_selec = { saud_select ->
                            salud_seguirdad = saud_select
                        },
                        descolorar_carta_segu = {
                            color_salud_seguirdad = false
                            color_categoria = false
                            color_subcategoria = false
                        },
                        descolorar_carta_cat = {
                            color_salud_seguirdad = false
                            color_categoria = false
                            color_subcategoria = false
                        },
                        descolorar_carta_sub = {
                            color_salud_seguirdad = false
                            color_categoria = false
                            color_subcategoria = false
                        })

                    spacer_vertical(5.dp)
                }
            }
            itemsIndexed(items) { index, item ->
                ramdoBox(
                    viewModelFiltros,
                    tick,
                    aler_dialog_contacto = aler_dialog_contacto,
                    firebaseAuth = firebaseAuth,
                    estado_tienda = horario_por_tienda,
                    i = item,
                    index = index,
                    listener_carta = { id, localidad, color ->
                        Log.d("coorrr1213213132", "$color")
                        localidad_tienda_seklecioanda = localidad
                        id_tienda_selecionada = id

//                        viewModelFiltros.obtenerHorarioPorTienda_activa(localidad, id)
                        show_bottom_sheeet = true
                    }, listner_carta_turismo = { id, localidad ->
                        Log.d("id_tiendasdada","$id $localidad")
                        id_lugar_turistico_select = id
                        localdad_llugar_turistico = localidad
                        bottom_sheet_turismo = true
                    },
                    abrir_gogle_map = { lat, log,id_tienda,localidad ->
                        dialog_Crear_ruta = true
                        id_tienda_crear_ruta=id_tienda
                        localidad_tienda_crear_ruta=localidad
                        latitud = lat
                        longitud = log
                    },
                    iniciar_seccion_normal = { iniciar_seccion_normal() },
                    crear_cuenta_geinz = { crear_cuenta_geinz() },
                    aler_dialog_contacto_fun = { lugar, nombre, img, id ->
                        aler_dialog_contacto = true
                        localidad_seguridad_salud = lugar
                        nombre_seguridad_salud = nombre
                        img_seguirdad_salud = img
                        id_seguridad_salud = id
                    }
                )
            }
        }

        if (state is SearchViewModel.ListItemsResult.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center

            ) {
                CircularProgressIndicator()
            }
        }

        when (state) {
            is SearchViewModel.ListItemsResult.Empty -> {
                val mensaje = (state as SearchViewModel.ListItemsResult.Empty).mensaje
                val radioTexto = Regex("""\d+\s*Km""").find(mensaje)?.value ?: ""
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (searchText.text.isNotEmpty()) {
                        texto_generico_one_line(
                            "No se encontraron resultados con ${searchText.text}",
                            modifier = Modifier,
                            color = Color.Gray
                        )
                    } else {
                        val annotatedText = buildAnnotatedString {
                            val before = mensaje.substringBefore(radioTexto)
                            val after = mensaje.substringAfter(radioTexto, "")

                            append(before)

                            if (radioTexto.isNotEmpty()) {
                                pushStringAnnotation(tag = "radio", annotation = radioTexto)
                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(radioTexto)
                                }
                                pop()
                            }

                            append(after)
                        }
                        ClickableText(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { offset ->
                                annotatedText
                                    .getStringAnnotations(
                                        tag = "radio",
                                        start = offset,
                                        end = offset
                                    )
                                    .firstOrNull()
                                    ?.let {

                                        mostrar_dialog_cambiar_radio = true
                                    }
                            }
                        )
                        spacer_vertical(10.dp)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    mostar_bottom_sheet_ayuda_geinz = true
                                }
                        ) {
                            texto_generico_one_line(
                                "¿Conoces alguno?",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            is SearchViewModel.ListItemsResult.Cleared -> {}
            is SearchViewModel.ListItemsResult.Error -> {
                val errorState = state as SearchViewModel.ListItemsResult.Error
                Text(
                    "${errorState.mensaje}",
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 10.dp)

                )
            }

            else -> {}
        }
        AnimatedVisibility(
            mostrar_centrado_visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            ImagenesSuperpuestasCollage(localida_defauld.nombre)
        }




        if (aler_dialog_contacto) {
            val (llamada, whatsapp, long) = datos_numeros_salud_seguridad
            dialog_salud_seguridad_algolia(
                long,
                dialog_seguridad_salud_algolia(
                    whatsapp,
                    llamada,
                    nombre_seguridad_salud,
                    img_seguirdad_salud
                ),
                ondimis = { aler_dialog_contacto = false })
        }


        if (dialog_Crear_ruta) {
            dialog_crear_ruta_lugares(
                onDismis = { dialog_Crear_ruta = false },
                crear_ruta = { crear_ruta ->
                    dialog_Crear_ruta = false
                    if (crear_ruta && verificarUbiActiva(context)) {
                        constantes_lista_localidades.abrir_google_maps("tienda",id_tienda_crear_ruta,localidad_tienda_crear_ruta,
                            context, latitud, longitud,
                        ) { dialogo ->
                            validacion_mostrar_dialog_ubi_off = dialogo
                        }
                    } else {
                        validacion_mostrar_dialog_ubi_off = true
                    }
                })
        }

        if (validacion_mostrar_dialog_ubi_off) {
            dialog_sin_ubi__rutas(
                "Te recomendamos activar el GPS para que podamos mostrarte la mejor ruta hasta el lugar en Google Maps.",
                { validacion_mostrar_dialog_ubi_off = false },
                {
                    validacion_mostrar_dialog_ubi_off = false
                    verificarGPS(context, launcher)

//                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
        }

        if (show_bottom_sheeet) {
            bottom_sheet_tiendas_filtradas(
                verificar_intener,
                viewModelFiltros,
                dataclass_tienda_seleccionada, show_bottom_sheeet
            ) {
                show_bottom_sheeet = false
            }
        }

        if (bottom_sheet_turismo) {
            bottom_sheet_lugares_turisticos(verificar_intener,
                viewmodelMap = viewmodelMap,
                viewmodel_lugares_turisticos = viewmodel_lugares_turisticos,
                datos = datos_lugares_turisticos,
                visible = bottom_sheet_turismo,
                onClose = { bottom_sheet_turismo = false },
                ver_mapa = { lista_lugares_cecanos ->
                    abrir_mapa("turismo")
                    val datos_cambio_pantalla = SearchViewModel.cambio_pantalla(
                        categoria_filtrad,
                        subcategira_filtrado,
                        tiendaLocalidadSeleccionada ?: "barranca",
                        searchText.text,
                    )
                    viewModel.guardar_datos_cambi_pantalla(datos_cambio_pantalla)
                },
                iniciar_seccion = { iniciar_seccion() },
                crear_cuenta = { crear_cuenta() })
        }

        if (mostrar_dialog_cambiar_radio) {
            dialogo_cabiar_rango_busqueda(
                viewmodel_floating_filtrado,
                geohashin = hash_user,
                context = context,
                ondimis = { mostrar_dialog_cambiar_radio = !mostrar_dialog_cambiar_radio },
                ondimis_aceptar = { radio, hasing_user ->
                    Log.d("logemos", "${radio}")
                    viewModel.filtrar_por_radio(
                        radio,
                        context,
                        categoria_filtrad,
                        subcategira_filtrado,
                        cerca_de_ti_enable.value,
                        hasing_user
                    )
//                    hash_user=hasing_user
                    radio_cambiado = radio
                },
                cancelar_dialog_filtrado_cerncano = {
                    viewmodel_floating_filtrado.save_cerca_de_ti(!cerca_de_ti_enable.value)
//                    cerca_de_ti_enable = !cerca_de_ti_enable.value
                },
                localidad_busqueda_general = tiendaLocalidadSeleccionada ?: "barranca",
                listner_localidad_busqueda = {
                    scope.launch {
                        if (!estado_mostar) {
                            expandedFloatingMenuFadeDemo = !expandedFloatingMenuFadeDemo
                        } else {
                            ocultar()
                            delay(400)
                            expandedFloatingMenuFadeDemo = !expandedFloatingMenuFadeDemo
                        }
                    }
                    color_localidad = !color_localidad
                    color_categoria = false
                    color_subcategoria = false
                    color_salud_seguirdad = false
                }, { hasing ->
                    hash_user = hasing
                })
        }

        if (mostar_bottom_sheet_ayuda_geinz) {
            bottom_sheet_ayudanos_a_creccer(verificar_intener,ultimaLocalidad?:"barranca",
                { mostar_bottom_sheet_ayuda_geinz = false },viewModelFiltros)
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
                .graphicsLayer { alpha = alphaAnim }
        )

        FloatingBubble(
            uid_respald_user,
            primeraVezCercaDeTi = primeraVezCercaDeTi,
            viewmodel_floating_filtrado = viewmodel_floating_filtrado,
            cerca_de_ti_enable = cerca_de_ti_enable.value,
            geohashin = hash_user,
            color_categoria = color_categoria,
            color_localidad = color_localidad,
            color_subcategoria = color_subcategoria,
            color_salud_seguridad = color_salud_seguirdad,
            seguidad_salud = salud_seguirdad,
            viewModel = viewModel,
            viewModelFiltros = viewModelFiltros,
            categoria_filtrado = categoria_filtrado,
            subir_btn = subir_btn,
            expanded = expandedFloatingMenuFadeDemo,
            onClick = {
                scope.launch {
                    if (!estado_mostar) {
                        expandedFloatingMenuFadeDemo = !expandedFloatingMenuFadeDemo
                    } else {
                        ocultar()
                        delay(400)
                        expandedFloatingMenuFadeDemo = !expandedFloatingMenuFadeDemo
                    }
                }
            },
            expanded_fun = {
                expandedFloatingMenuFadeDemo = false
                color_localidad = false
                color_categoria = false
                color_subcategoria = false
                color_salud_seguirdad = false
            },
            localidad_selecionada = tiendaLocalidadSeleccionada ?: "barranca",
            localidad_filtrado = { localidad ->
                tiendaLocalidadSeleccionada = localidad

            },
            categoria_filtrad = categoria_filtrad,
            categoria_Selecionada = { categoria ->
                Log.d("catgoria", "se cambio categoria")
                categoria_filtrad = categoria
                viewModel.clearResults()
            },
            subcategira_filtrado = subcategira_filtrado,
            subcategoria_selecionada = { subcategoria_select ->
                subcategira_filtrado = subcategoria_select
            },
            seguridad_salud_selec = { select ->
                if (select == "seguridad" || select == "salud") {
                    viewmodel_floating_filtrado.limpiar_valor_save_cerca_de_ti()
                }
                salud_seguirdad = select
                Log.d("salid_se", select)
                viewModel.clearResults()

            },
            click_carta_localidad = {
                color_localidad = !color_localidad
                color_categoria = false
                color_subcategoria = false
                color_salud_seguirdad = false
            },
            click_carta_localidad_delete = {
                color_localidad = false
            },
            click_carta_categoria = {
                color_categoria = !color_categoria
                color_localidad = false
                color_subcategoria = false
            },
            click_carta_categoria_delete = {
                color_subcategoria = false
                color_categoria = false
            },
            click_carta_seguridad = {
                color_salud_seguirdad = !color_salud_seguirdad
                color_localidad = false
                color_categoria = false

            },
            click_carta_seguridad_delete = {
                color_salud_seguirdad = false
                Log.d("salid_se", "eliminadomosfiltrado")

            },
            click_carta_subcategoria = {
                color_subcategoria = !color_subcategoria
                color_localidad = false
                color_categoria = false
            },
            click_carta_subcategoria_delete = {
                Log.d("elminados_", "coloreelimado")
                color_subcategoria = false
            },
            click_salud_general = {
                color_subcategoria = false
                color_categoria = false
                color_localidad = false
            },
            tiene_categorias = {
                color_salud_seguirdad = false
                color_subcategoria = false
            }, filtrado_cerca_de_ti = { radio, hasing_user ->
                Log.d("logemo13131232s", "${radio} $hasing_user")
                viewModel.filtrar_por_radio(
                    radio,
                    context,
                    categoria_filtrad,
                    subcategira_filtrado,
                    cerca_de_ti_enable.value,
                    hasing_user
                )
                radio_cambiado = radio
                hasing_user_user_filtrado = hasing_user
            }, fun_cerca_de_ti_enable = { it ->
                try {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (isGPSEnabled(context)) {
                            viewmodel_floating_filtrado.save_cerca_de_ti(it)
                            //                            cerca_de_ti_enable = it
                        } else {
                            verificarGPS(context, launcher)
                        }
                    } else {
                        permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } catch (e: SecurityException) {
                    Log.e("Ubicacion", "Error de seguridad al verificar permisos: ${e.message}")
                    Toast.makeText(context, "Error de permiso de ubicación", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    Log.e("Ubicacion", "Error inesperado: ${e.message}")
                }

            }, fun_nuevo_geohasing_actualizado = { it ->
                Log.d("nuevohasgin", it)
                hash_user = it
            }, fun_abrir_dialog_filtrado_radio = {
                mostrar_dialog_cambiar_radio = true
            }, fun_primeraVezCercaDeTi = { it ->
                primeraVezCercaDeTi = it
            },iniciar_seccion,crear_cuenta)
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun filtrado_chips(
    viewModel: SearchViewModel,
    searchText: String,
    lista_filtrado: List<String>,
    salud_seguirdad: String,
    lista_subcategoria: List<String>,
    categoria_selecionada: String,
    categoria_selecionada_fun: (String) -> Unit,
    subcategoria_selecionada: String,
    subcateogira_selecionada_fun: (String) -> Unit,
    cat_sub_select: (Boolean) -> Unit,
    seguridad_salud_selec: (String) -> Unit,
    descolorar_carta_segu: () -> Unit,
    descolorar_carta_cat: () -> Unit, descolorar_carta_sub: () -> Unit
) {
    // ✅ Unicidad
    val categoriasUnicas = lista_filtrado.distinct()
    val subcategoriasUnicas = lista_subcategoria.distinct()

    val hayCategoria = categoria_selecionada.isNotEmpty() && categoria_selecionada.length >= 2
    val haySubcategoria =
        subcategoria_selecionada.isNotEmpty() && subcategoria_selecionada.length >= 2
    val salud_seguirdad_valor = salud_seguirdad.isNotEmpty() && salud_seguirdad.length >= 2
    val haySeleccion = hayCategoria || haySubcategoria || salud_seguirdad_valor
    var mostrar_texto by remember { mutableStateOf(false) }

    cat_sub_select(haySeleccion)

    // ✅ Caso especial: si hay salud/seguridad, mostrar solo eso

    if (salud_seguirdad.isNotEmpty()) {
        LazyRow() {
            item {
                chisp_filtrado_busqueda(
                    carta_selecionada = salud_seguirdad_valor,
                    filtrado = salud_seguirdad,
                    btn_visible = true,
                    clik_card = {
                        seguridad_salud_selec(salud_seguirdad)
                        Log.d("salud_segudad", salud_seguirdad)
                    },
                    onClick_delete = {
                        categoria_selecionada_fun("")
                        subcateogira_selecionada_fun("")
                        seguridad_salud_selec("")
//                        viewModel.limpiar_lista_datos_original_cat()
                        viewModel.clearResults()
                        Log.d(
                            "clearResults",
                            "caundo borramos el de salud o seguridad solo chip"
                        )
                        descolorar_carta_segu()
                    }
                )
            }
        }
    } else {
        // ✅ Flujo normal si salud_seguirdad está vacío
        val categoriasFiltradas = if (hayCategoria) {
            listOf(categoria_selecionada)
        } else {
            categoriasUnicas
        }

        if (searchText.length >= 2 && !haySeleccion) {
            Log.d("entramos_Seach", "1")
            LazyRowConSombras() {
                // ✅ Mostrar categorías únicas
                items(categoriasFiltradas) { cat ->
                    val catSeleccionada = categoria_selecionada == cat
                    chisp_filtrado_busqueda(
                        carta_selecionada = catSeleccionada,
                        filtrado = cat,
                        btn_visible = true,
                        clik_card = {
                            when (cat) {
                                "salud" -> {
                                    seguridad_salud_selec("salud")
                                }

                                "seguridad" -> {
                                    seguridad_salud_selec("seguridad")
                                }

                                else -> {
                                    categoria_selecionada_fun(cat)
                                }
                            }
                            Log.d("select", cat)
                        },
                        onClick_delete = {
                            categoria_selecionada_fun("")
                            subcateogira_selecionada_fun("")
                            viewModel.clearResults()
                            Log.d(
                                "clearResults",
                                "caundo borramos alguna categoria selecioanda"
                            )
                            descolorar_carta_cat()
//                            viewModel.limpiar_lista_datos_original_cat()
                        }
                    )
                }

                // ✅ Mostrar subcategorías únicas
                items(subcategoriasUnicas) { sub ->
                    val subSeleccionada = subcategoria_selecionada == sub
                    chisp_filtrado_busqueda(
                        carta_selecionada = subSeleccionada,
                        filtrado = sub.capitalizeFirst(),
                        btn_visible = true,
                        clik_card = { subcateogira_selecionada_fun(sub) },
                        onClick_delete = {
                            subcateogira_selecionada_fun("")
                            descolorar_carta_sub()
                        }
                    )
                }
            }
        } else if (searchText.length < 2 && !haySeleccion) {
            Log.d("entramos_Seach", "2")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val categoriasAleatorias = remember(categorias_defaul) {
                    categorias_defaul.shuffled().take(15)
                }
                LazyRowConSombras {
                    items(categoriasAleatorias) { i ->
                        chisp_filtrado_busqueda(
                            carta_selecionada = false,
                            filtrado = simplificarCategoria(i),
                            btn_visible = false,
                            clik_card = { categoria_selecionada_fun(i) },
                            onClick_delete = {
                                categoria_selecionada_fun("")
                                subcateogira_selecionada_fun("")
                                viewModel.clearResults()
                                Log.d(
                                    "clearResults",
                                    "en los  chips 2 cunado borramos alguna categoria"
                                )
//                                viewModel.limpiar_lista_datos_original_cat()
                                descolorar_carta_cat()
                            }
                        )
                    }
                }
                spacer_vertical(10.dp)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Flecha arriba",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            mostrar_texto = !mostrar_texto
                        }
                )

                spacer_vertical(5.dp)
                AnimatedVisibility(mostrar_texto) {
                    texto_generico_one_line("Selecciona una categoria para empezar")
                }

            }


        } else {
            Log.d("entramos_Seach", "3")
            LazyRowConSombras() {
                items(categoriasFiltradas) { cat ->
                    val catSeleccionada = categoria_selecionada == cat
                    chisp_filtrado_busqueda(
                        carta_selecionada = catSeleccionada,
                        filtrado = cat,
                        btn_visible = true,
                        clik_card = { categoria_selecionada_fun(cat) },
                        onClick_delete = {
                            Log.d("eliminado", "eliminasdos_fitrlado1")
                            categoria_selecionada_fun("")
                            subcateogira_selecionada_fun("")
                            Log.d(
                                "clearResults",
                                "seararch 3 caundo borramops otra cat selecianoda"
                            )
                            viewModel.clearResults()
//                            viewModel.limpiar_lista_datos_original_cat()
                            descolorar_carta_cat()
                        }
                    )
                }

                // ✅ Mostrar subcategorías
                items(subcategoriasUnicas) { sub ->
                    val subSeleccionada = subcategoria_selecionada == sub
                    chisp_filtrado_busqueda(
                        carta_selecionada = subSeleccionada,
                        filtrado = sub.capitalizeFirst(),
                        btn_visible = true,
                        clik_card = { subcateogira_selecionada_fun(sub) },
                        onClick_delete = {
                            Log.d("eliminado", "eliminasdos_fitrlado2")
//viewModel.limpiar_lista_datos_original_sub()
                            subcateogira_selecionada_fun("")
                            descolorar_carta_sub()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun LazyRowConSombras(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()

    val showLeftShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val showRightShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < total - 1
        }
    }

    // 🔥 animar alpha, no crear/destruir Box
    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaLeft"
    )
    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaRight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
//            contentPadding=PaddingValues(horizontal = 10.dp),
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )

        // 👈 izquierda
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterStart)
                .zIndex(1f)
                .alpha(alphaLeft)
                .background(Brush.horizontalGradient(colors = shadow_left))
        )

        // 👉 derecha
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .alpha(alphaRight)
                .background(Brush.horizontalGradient(colors = shadow_right))
        )
    }
}


@Composable
fun fraces_filtrado(expandedFloatingMenuFadeDemo: Boolean) {
    val fraces = constantes_lista_localidades.lista_frases_busqueda
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(expandedFloatingMenuFadeDemo) {
        if (!expandedFloatingMenuFadeDemo) {
            while (true) {
                delay(4000L)
                index = (index + 1) % fraces.size
            }
        }
    }
    Crossfade(fraces[index], label = "fraces") { txt ->
        texto_generico_one_line(
            texto = txt,
            MaterialTheme.typography.busquedaGeinzWork
        )
    }
}


@Composable
fun TexfielFiltrado(
    cat_sub_seleciondo: Boolean,
    placeholder: String,
    focusRequester: FocusRequester,
    texto: TextFieldValue,
    onvalueChage: (String) -> Unit,
    listener_borrar_texto: () -> Unit,
) {
    var icono_borrar by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    if (texto.text.isEmpty()) {
        icono_borrar = false
    }
    OutlinedTextField(
        value = texto,
        onValueChange = { newValue: TextFieldValue ->
            Log.d("falta_señal", newValue.text)
            icono_borrar = newValue.text.isNotBlank()
            onvalueChage(newValue.text)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "buscar",
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (icono_borrar) {
                IconButton(onClick = {
                    if (cat_sub_seleciondo) {
                        Log.d("seleccion", "existe")
                        onvalueChage("")
                    } else {
                        Log.d("seleccion", " no existe")
                        onvalueChage("")
                        listener_borrar_texto()
                    }
                    icono_borrar = false

                }) {
                    Icon(
                        painter = painterResource(R.drawable.vector_eliminar_texto_texfiel),
                        contentDescription = "borrar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        shape = RoundedCornerShape(50)
    )

    Spacer(modifier = Modifier.height(8.dp))

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}


@Composable
fun ramdoBox(
    viewModelFiltros: viewModel_filtado_tiendas,
    tick: Long,
    aler_dialog_contacto: Boolean,
    firebaseAuth: FirebaseAuth,
    estado_tienda: Map<String, Boolean>?,
    i: Item,
    index: Int,
    listener_carta: (String, String, Color) -> Unit,
    listner_carta_turismo: (String, String) -> Unit,
    abrir_gogle_map: (Double, Double,String,String) -> Unit,
    iniciar_seccion_normal: () -> Unit,
    crear_cuenta_geinz: () -> Unit,
    aler_dialog_contacto_fun: (lugar: String, nombre: String, img: String, id: String) -> Unit
) {
    val color_princial = MaterialTheme.colorScheme.surface
    val heightOptions = listOf(300.dp, 350.dp)
    val color_estado_tienda by viewModelFiltros.color_estado_tienda.collectAsState()
    var estadoColor by remember { mutableStateOf(color_princial) }
    val resultado by remember(
        color_estado_tienda,
        color_estado_tienda.h_cierre,
        color_estado_tienda.cerrado,
        color_estado_tienda.motivo,
        tick
    ) {
        derivedStateOf {
            calcularTiempoRestante(
                color_estado_tienda,
                color_estado_tienda.h_cierre,
                color_estado_tienda.cerrado,
                color_estado_tienda.motivo
            )
        }
    }

    viewModelFiltros.setear_color(resultado.color)

    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]
    val iconCategoria = constantes_lista_localidades.getCategoriaIcon(i.categoria)
    var mostra_dialog_login by remember { mutableStateOf(false) }
    var texto_bottom_sheet_dialog_login by remember { mutableStateOf("") }
    val context = LocalContext.current
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
//    var aler_dialog_contacto by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.7f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(i.img)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            when {
                                i.categoria == "seguridad" || i.categoria == "salud" -> {
                                    // Caso 1: seguridad o salud
                                    aler_dialog_contacto_fun(i.lugar, i.nombre, i.img, i.id_tienda)
                                }

                                firebaseAuth.currentUser != null || id_respado_user.isNotEmpty() -> {
                                    // Caso 2: usuario registrado
                                    when (i.categoria) {
                                        "turismo" -> {
                                            listner_carta_turismo(i.id_tienda, i.lugar)
                                        }

                                        else -> {
                                            listener_carta(i.id_tienda, i.lugar, estadoColor)
                                        }
                                    }
                                }

                                else -> {
                                    // Caso 3: usuario NO registrado
                                    texto_bottom_sheet_dialog_login =
                                        "¡Regístrate para ver todos los detalles y disfrutar la experiencia completa!"
                                    mostra_dialog_login = true
                                }
                            }
                        },

                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(

                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF262626)
                                ),

                                )
                        )
                )

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp, bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = i.nombre.capitalizeFirst(),
                        fontFamily = textos_titulos_geinz_wokr,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    val coordenadasValidas = i.latitud != 0.0 && i.longitud != 0.0
                    if (coordenadasValidas) {
                        FloatingActionButton(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            onClick = {


                                when {
                                    i.categoria == "seguridad" || i.categoria == "salud" -> {
                                        // Categoría seguridad o salud
                                        if (coordenadasValidas) {
                                            abrir_gogle_map(i.latitud, i.longitud,i.id_tienda,i.lugar)
                                        }
                                        // Si no hay coordenadas, no hace nada
                                    }

                                    firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()-> {
                                        // Categoría diferente y usuario registrado
                                        if (coordenadasValidas) {
                                            abrir_gogle_map(i.latitud, i.longitud,i.id_tienda,i.lugar)
                                        }
                                        // Si no hay coordenadas, no hace nada
                                    }

                                    else -> {
                                        // Categoría diferente y usuario NO registrado
                                        texto_bottom_sheet_dialog_login =
                                            "Crea tu ruta registrándote ahora"
                                        mostra_dialog_login = true
                                    }
                                }
                            },

                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "centrar",
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    }
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.localidad_icon_general),
                        contentDescription = "",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 5.dp)
                    )
                    texto_generico_one_line(
                        i.lugar.capitalizeFirst(),
                        MaterialTheme.typography.bodyMedium
                    )
                }
                spacer_vertical(5.dp)
                texto_generico_one_line(
                    "$iconCategoria ${i.categoria.capitalizeFirst()}",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(7.dp)
                tags_subcateogiras(
                    i.lista,
                    brush_start = Brush.horizontalGradient(colors = shadow_top_filtrado_v1),
                    brush_end = Brush.horizontalGradient(colors = shadow_botonm_filtrado_v1)
                )
            }
        }
    }
    if (mostra_dialog_login) {
        bottom_sheet_registrate(
            ondimis = { mostra_dialog_login = false },
            iniciar_seccion_normal = { iniciar_seccion_normal() },
            crear_cuenta_geinz = { crear_cuenta_geinz() },
            texto_bottom_Sheet = texto_bottom_sheet_dialog_login
        )
    }


}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun apartado_lugares_interes(
    clikeado: Boolean,
    expandedIndex: Int,
    texto: String,
    lista_subcategoria: List<String>,
    enColumna: Boolean,
    modifier: Modifier = Modifier,
    expandir_clik: () -> Unit,
    cat_sub_selection: String,
    cat_sub_clik: (String) -> Unit
) {
    val icono_expandido = if (expandedIndex == 0) {
        Icons.Default.ExpandMore
    } else {
        Icons.Default.ExpandLess
    }

    val listState = if (enColumna) rememberLazyListState() else rememberLazyListState()

    // sombreado arriba/abajo (solo aplica en columnas)
    val showTopShadow by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 0 }
    }
    val showBottomShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < totalItems - 1
        }
    }

    var color_subcategoria by remember { mutableStateOf(false) }

    val startTopColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_top_filtrado_v1[0] else shadow_top_filtrado_v2[0],
        animationSpec = tween(500), label = ""
    )
    val endTopColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_top_filtrado_v1[1] else shadow_top_filtrado_v2[1],
        animationSpec = tween(500), label = ""
    )
    val startBottomColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_botonm_filtrado_v1[0] else shadow_botonm_filtrado_v2[0],
        animationSpec = tween(500), label = ""
    )
    val endBottomColor by animateColorAsState(
        targetValue = if (!clikeado) shadow_botonm_filtrado_v1[1] else shadow_botonm_filtrado_v2[1],
        animationSpec = tween(500), label = ""
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        if (enColumna) {
            // LazyColumn
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    texto_generico_multilinea(
                        texto.capitalizeFirst(),
                        MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                    spacer_vertical(5.dp)
                }
                items(lista_subcategoria) { i ->
                    val seleccionado = if (cat_sub_selection.equals(
                            i,
                            ignoreCase = true
                        )
                    ) Color.Black else MaterialTheme.colorScheme.primary
                    AnimatedFabItem(
                        i,
                        seleccionado,
                        true,
                        onClick = {
                            cat_sub_clik(i)
                        })
                }
            }

            // sombras solo para columna
            AnimatedVisibility(
                showTopShadow,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(startTopColor, endTopColor),

                                )
                        )
                )
            }

            AnimatedVisibility(
                showBottomShadow,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    startBottomColor,
                                    endBottomColor
                                ),

                                )
                        )
                )
            }
        }

        btn_close_gris(
            modifier = Modifier.align(Alignment.TopEnd),
            icono_expandido,
            onClick = { expandir_clik() }
        )
    }
}


@Composable
fun AnimatedFabItem(
    text: String,
    color: Color,
    visible: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 500),
        label = "buttonColorAnim"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
    ) {
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(containerColor = animatedColor)
        ) {
            Text(
                text.capitalizeFirst(),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
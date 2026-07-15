package com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.key
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ZoomableGalleryFullScreenVerticalPager
import com.geinzz.geinzwork.ui.adapters.promoEstaExpirada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_filtrados_general_promos_ofertas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_filtrar_desde_tienda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_todas_las_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.FiltroSnackbarActivo
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.snackbar_tienda_con_logo
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.constantes_reprodutor_video.GaleriaHorizontalInstagram_promociones_solo_imagen
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_datos_promociones
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Offset
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween


@Composable
fun ui_promos_cerca_de_ti(
    flag_identificador: String,
    activar_promo_params: String,
    localidad: String,
    ids: List<String>? = null,
    verificar_intener: Boolean,
    iniciar_seccion: () -> Unit,
    crear_cuenta: () -> Unit,
    onBack: () -> Unit
) {
    Log.d("ids_entraantes", "$ids")

    var ids_obtenidos_promociones by remember { mutableStateOf(ids) }

    // ===== Contexto, Auth y ViewModels =====
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val uid_respald_user by data_store_localidad
        .get_uid_user(context)
        .collectAsState(initial = firebaseAuth.uid.orEmpty())

    val viewModel: viewmodel_promos_cercanas = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val viewmodel_repo_datos_promo: viewmodel_datos_promociones = viewModel()

// ===== Estados provenientes de viewModel (promos / carga) =====
    val estado by viewModel.estadoPromos.collectAsState()
    val estadoTienda by viewModel.estado_Carga_tienda_select.collectAsState()
    val respuesta_gemini_NLP by viewModel.respuesta_gemini.collectAsState()
    val lista_resultados_gemini by viewModel.listaResultados.collectAsState()
    val hayMasPaginas by viewModel.hayMasPaginas.collectAsState()
    val cargandoPagina by viewModel.cargandoPagina.collectAsState()
    val filtro_sin_resultados by viewModel.filtro_tienda_sin_resultados.collectAsState()
    val tiendasConMasDeUnaPromo by viewModel.tiendas_con_mas_de_una_promo.collectAsState()
    val esPrimeraCarga by viewModel.esPrimeraCarga.collectAsState()
    val resultado_open_ia = viewModel.resultado
    val modoBusquedaIA = viewModel.modoBusquedaIA


// ===== Estados de filtros provenientes de viewModel =====
    val categoriaSeleccionada by viewModel.categoria_seleccionada.collectAsState()
    val subcategoriasSeleccionadas by viewModel.subcategoria_seleccionada.collectAsState()
    val rango_precio by viewModel.rangoPrecioSeleccionado.collectAsState()
    val comodidad_selet by viewModel.comodidadesSeleccionadas.collectAsState()
    val metodo_pago by viewModel.metodosPagoSeleccionados.collectAsState()

// ===== Otros datos provenientes de viewModels =====
    val datos_promo_parametros by viewmodel_repo_datos_promo.datos_promocion_parametro.collectAsState()

// ===== Estados locales: filtros seleccionados/confirmados (UI) =====
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var tags_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    var rangos_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    var pagos_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    var comodidades_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    var filtrosDesdeBottomSheetGeneral by remember { mutableStateOf(false) }
    var filtrandoDesdeTienda by remember { mutableStateOf(false) }
    var filtroTiendaAplicado by remember { mutableStateOf(false) }

    val hayFiltros =
        categoriaSeleccionada.isNotEmpty() ||
                subcategoriasSeleccionadas.isNotEmpty() ||
                rango_precio?.isNotEmpty() == true ||
                comodidad_selet.isNotEmpty() ||
                metodo_pago.isNotEmpty() ||
                lista_resultados_gemini.isNotEmpty()

// ===== Estados locales: tiendas seleccionadas =====
    var tiendaSeleccionada by remember { mutableStateOf<String?>(null) }
    var tienda_seleccionada_clik_baner by remember { mutableStateOf<String?>(null) }
    var tienda_anterior by remember { mutableStateOf<String?>(null) }
    var nombre_tienda_anterior by remember { mutableStateOf("") }
    var nombre_tienda_seleccionada by remember { mutableStateOf("") }
    var nombre_tienda_mostrando by remember { mutableStateOf("") }
    var id_tienda_select by remember { mutableStateOf("") }
    var esperandoConfirmacionTienda by remember { mutableStateOf(false) }
    var estado_caundo_busca_tienda by remember { mutableStateOf(false) }

    val tiendasOrdenadas by remember(tiendasConMasDeUnaPromo, tiendaSeleccionada) {
        derivedStateOf {
            if (tiendaSeleccionada == null) {
                tiendasConMasDeUnaPromo
            } else {
                val seleccionada = tiendasConMasDeUnaPromo.find { it.id == tiendaSeleccionada }
                val resto = tiendasConMasDeUnaPromo.filter { it.id != tiendaSeleccionada }
                if (seleccionada != null) listOf(seleccionada) + resto
                else tiendasConMasDeUnaPromo
            }
        }
    }

// ===== Estados locales: promociones =====
    var promoSeleccionada_unica by remember {
        mutableStateOf<dataclass_promociones_cerca_de_ti?>(
            null
        )
    }
    var promoExpirada by remember { mutableStateOf(false) }
    var estadisticasAgregadas by remember { mutableStateOf(false) }
    var promos by remember { mutableStateOf<List<obj_completo>>(emptyList()) }
    var promosFiltradas by remember { mutableStateOf<List<obj_completo>>(emptyList()) }

// ===== Estados locales: UI - visibilidad de diálogos / bottom sheets / zoom =====
    var mostrar_zoom_img by remember { mutableStateOf(false) }
    var mostrar_bottom_shet_registrate by remember { mutableStateOf(false) }
    var mostrar_todas_tiendas by remember { mutableStateOf(false) }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var mostar_bottom_sheet_datos by remember { mutableStateOf(false) }
    var index_galeria_img by remember { mutableStateOf(0) }

// ===== Estados locales: búsqueda IA (Gemini) / carga de tienda =====
    var mostrar_carga_Respuesta_gemini by remember { mutableStateOf(false) }
    var mostrar_lupa_busqueda by remember { mutableStateOf(true) }
    var loadingSnackbarShown by remember { mutableStateOf(false) }
    var loadingTiendaShown by remember { mutableStateOf(false) }
    var segundosRestantes by remember { mutableIntStateOf(10) }


    // ===== Estado del micrófono / Whisper (ahora a nivel de composable) =====
    val estadoMicrofono by viewModel.estadoMicrofono.collectAsState()
    val estaGrabando = estadoMicrofono is viewmodel_promos_cercanas.EstadoMicrofono.Grabando
    val estaProcesando = estadoMicrofono is viewmodel_promos_cercanas.EstadoMicrofono.Procesando
    val estaOcupado = estaGrabando || estaProcesando || mostrar_carga_Respuesta_gemini
// ===== Estados locales: Snackbar =====
    var mostrar_snackbar_tienda by remember { mutableStateOf(false) }
    var snackbar_logo_tienda by remember { mutableStateOf("") }
    var snackbar_total_promos by remember { mutableStateOf(0) }
    var snackbar_nombre_tienda_snap by remember { mutableStateOf("") }
    var texto_snackbar by remember { mutableStateOf("") }
    var snackbarMostrado by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

// ===== Otros: búsqueda, navegación, scope =====
    var valor_a_buscar by remember { mutableStateOf("") }
    var valor_a_buscar_respaldo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()


    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val anchoPantallaPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val altoPantallaPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val tamanoBurbujaPx = with(density) { 56.dp.toPx() }
    val margenBurbujaPx = with(density) { 12.dp.toPx() }
// 👈 se eliminó la línea de margenInferiorPx
    val offsetXBurbuja = remember {
        Animatable(anchoPantallaPx - tamanoBurbujaPx - margenBurbujaPx)
    }
    val offsetYBurbuja = remember {
        Animatable(altoPantallaPx - tamanoBurbujaPx - margenBurbujaPx)   // 👈 cambiado de margenInferiorPx a margenBurbujaPx
    }
    val scopeBurbuja = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    BackHandler { onBack() }

// ── LaunchedEffects ──────────────────────────────────────

    LaunchedEffect(activar_promo_params) {
        estadisticasAgregadas = false
        snackbarMostrado = false
        if (activar_promo_params.isEmpty()) {
            promoExpirada = false
            return@LaunchedEffect
        }
        viewmodel_repo_datos_promo.obtener_datos_promociones_por_paramtros(
            localidad,
            activar_promo_params
        )
    }

    LaunchedEffect(filtro_sin_resultados) {
        if (filtro_sin_resultados) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "No hay promos con ese filtro en esta tienda",
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.resetear_filtro_sin_resultados()
        }
    }

    LaunchedEffect(datos_promo_parametros) {
        if (activar_promo_params.isEmpty()) return@LaunchedEffect
        val idPromo = datos_promo_parametros.informacion_publcacion.id_promocion
        if (idPromo.isEmpty()) return@LaunchedEffect
        if (datos_promo_parametros.estado_publicacion.equals("pausado", ignoreCase = true)) {
            promoExpirada = true
            texto_snackbar = "Esta publicación no está disponible en este momento."
            return@LaunchedEffect
        }
        if (promoEstaExpirada(datos_promo_parametros.fecha_fin)) {
            promoExpirada = true
            return@LaunchedEffect
        }

        promoExpirada = false
        tienda_seleccionada_clik_baner = datos_promo_parametros.informacion_publcacion.id_tienda
        mostrar_zoom_img = true
        promoSeleccionada_unica = datos_promo_parametros

        if (!estadisticasAgregadas) {
            viewModel.agregar_estadisticas_publicacion(
                "click",
                activar_promo_params,
                localidad,
                uid_respald_user
            )
            viewModel.agregar_estadisticas_publicacion(
                "vistas",
                activar_promo_params,
                localidad,
                uid_respald_user
            )
            estadisticasAgregadas = true
        }
    }

    LaunchedEffect(estado, promoExpirada) {
        if (estado is viewmodel_promos_cercanas.estado_carga_promociones.succes &&
            promoExpirada && !snackbarMostrado
        ) {
            snackbarMostrado = true
            snackbarHostState.showSnackbar(
                message = texto_snackbar,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(localidad) {
        if (!ids_obtenidos_promociones.isNullOrEmpty()) {
            viewModel.obtener_promos_por_ids_directos(ids_obtenidos_promociones!!, localidad)
        } else {
            viewModel.obtener_promociones_2da("barranca", "", null)
        }
    }

    LaunchedEffect(respuesta_gemini_NLP) {
        when (respuesta_gemini_NLP) {
            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.loading -> {
                mostrar_carga_Respuesta_gemini = true
                mostrar_lupa_busqueda = false
                if (!loadingSnackbarShown) {
                    loadingSnackbarShown = true
                    launch {
                        snackbarHostState.showSnackbar(
                            message = "Buscando resultados...",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }
            }

            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.succes -> {
                mostrar_carga_Respuesta_gemini = false
                mostrar_lupa_busqueda = true
                loadingSnackbarShown = false
                focusManager.clearFocus(force = true)   // 👈 nuevo
                keyboardController?.hide()               // 👈 nuevo
                val cantidad =
                    (respuesta_gemini_NLP as viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.succes).cantidad
                val datos_respuesta =
                    (respuesta_gemini_NLP as viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.succes).items
                val msje =
                    if (cantidad > 0) "Tengo $cantidad resultados para tu búsqueda" else "Lo siento, no encontré nada para ti"
                promosFiltradas = datos_respuesta
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = msje,
                    actionLabel = if (cantidad > 0) "Ver" else null,
                    duration = if (cantidad > 0) SnackbarDuration.Indefinite else SnackbarDuration.Short
                )
                viewModel.resetear_respuesta_de_gemini()
                if (result == SnackbarResult.ActionPerformed) {
                    promos = promosFiltradas
                }
            }

            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.empty -> {
                mostrar_carga_Respuesta_gemini = false
                mostrar_lupa_busqueda = true
                loadingSnackbarShown = false
                focusManager.clearFocus(force = true)   // 👈 nuevo
                keyboardController?.hide()               // 👈 nuevo
                val mensaje =
                    (respuesta_gemini_NLP as viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.empty).text_vacio
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = if (mensaje.isNotEmpty()) mensaje else "Lo siento, no encontré nada para ti",
                    duration = SnackbarDuration.Short
                )
                viewModel.resetear_respuesta_de_gemini()
            }

            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.error -> {
                mostrar_carga_Respuesta_gemini = false
                mostrar_lupa_busqueda = true
                loadingSnackbarShown = false
                focusManager.clearFocus(force = true)   // 👈 nuevo
                keyboardController?.hide()               // 👈 nuevo
                snackbarHostState.currentSnackbarData?.dismiss()
            }

            else -> {
                mostrar_carga_Respuesta_gemini = false
                mostrar_lupa_busqueda = true
                loadingSnackbarShown = false
            }
        }
    }
    LaunchedEffect(estado) {
        if (estado is viewmodel_promos_cercanas.estado_carga_promociones.succes) {
            Log.d("BUG_TIENDA", "📦 LaunchedEffect(estado) succes — esperandoConfirmacionTienda=$esperandoConfirmacionTienda, filtrandoDesdeTienda=$filtrandoDesdeTienda")
            if (esperandoConfirmacionTienda && !filtrandoDesdeTienda) {
                Log.d("BUG_TIENDA", "🚫 IGNORANDO actualización de promos (esperando confirmación de tienda)")
                return@LaunchedEffect
            }

            val nuevos = (estado as viewmodel_promos_cercanas.estado_carga_promociones.succes).items
            promos = nuevos.distinctBy {
                it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
            }
            Log.d("BUG_TIENDA", "✅ promos actualizado — total=${promos.size}")
            filtrandoDesdeTienda = false
        }
    }

    LaunchedEffect(estadoTienda) {
        when (estadoTienda) {
            is viewmodel_promos_cercanas.estado_carga_tienda_Seleccionada.loading -> {
                estado_caundo_busca_tienda = true
                if (!loadingTiendaShown) {
                    loadingTiendaShown = true
                    launch {
                        snackbarHostState.showSnackbar(
                            message = "Buscando en la tienda...",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }
            }

            is viewmodel_promos_cercanas.estado_carga_tienda_Seleccionada.succes -> {
                estado_caundo_busca_tienda = false
                loadingTiendaShown = false
                val data =
                    estadoTienda as viewmodel_promos_cercanas.estado_carga_tienda_Seleccionada.succes
                pagos_tienda_confirmada = data.pagos
                comodidades_tienda_confirmada = data.comodidades
                promosFiltradas = data.items
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbar_total_promos = data.total
                snackbar_nombre_tienda_snap = nombre_tienda_seleccionada
                snackbar_logo_tienda =
                    tiendasConMasDeUnaPromo.find { it.id == tiendaSeleccionada }?.logo_img ?: ""
                esperandoConfirmacionTienda = true
                mostrar_snackbar_tienda = true
            }

            is viewmodel_promos_cercanas.estado_carga_tienda_Seleccionada.error -> {
                estado_caundo_busca_tienda = false
                loadingTiendaShown = false
                snackbarHostState.currentSnackbarData?.dismiss()
            }

            else -> {
                estado_caundo_busca_tienda = false
                loadingTiendaShown = false
            }
        }
    }
    // ── UI ───────────────────────────────────────────────────
    val titulo =
        if (ids_obtenidos_promociones.isNullOrEmpty())
            "Promociones y ofertas cerca de ti"
        else
            "Promociones seleccionadas para ti"

    val descripcion =
        if (ids_obtenidos_promociones.isNullOrEmpty())
            "Descubre descuentos, promociones especiales y ofertas exclusivas de negocios cercanos."
        else
            "Estas son las promociones que Daniel seleccionó según tu búsqueda. Si deseas explorar todas las promociones disponibles, presiona «Ver todas las promociones»."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (estado) {

            viewmodel_promos_cercanas.estado_carga_promociones.loading -> {
                if (esPrimeraCarga) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }

            is viewmodel_promos_cercanas.estado_carga_promociones.empty -> {
                Text(
                    text = (estado as viewmodel_promos_cercanas.estado_carga_promociones.empty).txt,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is viewmodel_promos_cercanas.estado_carga_promociones.error -> {
                Text(
                    text = (estado as viewmodel_promos_cercanas.estado_carga_promociones.error).txt,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }

            is viewmodel_promos_cercanas.estado_carga_promociones.succes -> {

                Box(modifier = Modifier.fillMaxSize()) {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .animateContentSize(),
                    ) {

                        // ── Título ──
                        item {
                            Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                                texto_generico_multilinea(
                                    titulo,
                                    style = MaterialTheme.typography.banerGeinzWork
                                )
                                spacer_vertical(5.dp)
                                texto_generico_multilinea(
                                    descripcion, style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // ── Campo de búsqueda ──
                        if (ids_obtenidos_promociones.isNullOrEmpty() &&
                            !hayFiltros && !filtroTiendaAplicado && tiendaSeleccionada == null
                        ) {
                            item {

                                var mediaRecorder by remember {
                                    mutableStateOf<MediaRecorder?>(
                                        null
                                    )
                                }
                                var archivoAudio by remember { mutableStateOf<File?>(null) }

                                val launcher = rememberLauncherForActivityResult(
                                    ActivityResultContracts.RequestPermission()
                                ) { granted ->
                                    if (granted) {
                                        // 👇 nuevo: soltamos el foco y ocultamos el teclado ANTES de grabar
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()

                                        val file = File(
                                            context.cacheDir,
                                            "audio_${System.currentTimeMillis()}.m4a"
                                        )
                                        archivoAudio = file
                                        @Suppress("DEPRECATION")
                                        val recorder = MediaRecorder().apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                            setOutputFile(file.absolutePath)
                                            prepare()
                                            start()
                                        }
                                        mediaRecorder = recorder
                                        segundosRestantes = 10
                                        viewModel.iniciar_grabacion()
                                    }
                                }

                                LaunchedEffect(estaGrabando) {
                                    if (estaGrabando) {
                                        segundosRestantes = 10
                                        while (segundosRestantes > 0) {
                                            delay(1000L)
                                            segundosRestantes--
                                        }
                                        try {
                                            mediaRecorder?.stop()
                                            mediaRecorder?.release()
                                            mediaRecorder = null
                                        } catch (e: Exception) {
                                            mediaRecorder = null
                                            viewModel.resetear_microfono()
                                            return@LaunchedEffect
                                        }
                                        archivoAudio?.let { file ->
                                            viewModel.enviar_audio_a_whisper(file) { texto ->
                                                valor_a_buscar = texto.take(500)
                                                // 👇 nuevo: aseguramos que el teclado no aparezca al llenar el campo
                                                focusManager.clearFocus(force = true)
                                                keyboardController?.hide()
                                            }
                                        }
                                    } else {
                                        segundosRestantes = 10
                                    }
                                }

                                DisposableEffect(Unit) {
                                    onDispose {
                                        mediaRecorder?.apply {
                                            try {
                                                stop()
                                            } catch (e: Exception) {
                                            }
                                            release()
                                        }
                                        mediaRecorder = null
                                        viewModel.resetear_microfono()
                                    }
                                }

                                val infiniteTransition =
                                    rememberInfiniteTransition(label = "mic_pulse")
                                val pulseScale by infiniteTransition.animateFloat(
                                    initialValue = 1f, targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "pulse_scale"
                                )
                                val pulseAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f, targetValue = 0.7f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "pulse_alpha"
                                )

                                LoadingOutlinedField(loading = mostrar_carga_Respuesta_gemini) {
                                    OutlinedTextField(
                                        value = valor_a_buscar,
                                        onValueChange = {
                                            if (!estaOcupado && it.length <= 500) valor_a_buscar =
                                                it
                                        },
                                        readOnly = estaOcupado,
                                        placeholder = {
                                            AnimatedContent(
                                                targetState = when {
                                                    estaGrabando -> "grabando"
                                                    estaProcesando -> "procesando"
                                                    mostrar_carga_Respuesta_gemini -> "buscando"
                                                    else -> "idle"
                                                },
                                                label = "placeholder_anim"
                                            ) { est ->
                                                when (est) {
                                                    "grabando" -> Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            6.dp
                                                        )
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .scale(pulseScale)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    Color(0xFFEC1707).copy(
                                                                        alpha = pulseAlpha
                                                                    )
                                                                )
                                                        )
                                                        Text(
                                                            "Escuchando...",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color(0xFFEC1707)
                                                        )
                                                        Text(
                                                            text = "${segundosRestantes}s",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (segundosRestantes <= 3) Color(
                                                                0xFFEC1707
                                                            ) else Color(0xFFAAAAAA)
                                                        )
                                                    }

                                                    "procesando" -> Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            6.dp
                                                        )
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(
                                                                12.dp
                                                            ),
                                                            strokeWidth = 1.5.dp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "Procesando audio...",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.Gray
                                                        )
                                                    }

                                                    "buscando" -> Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            6.dp
                                                        )
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(
                                                                12.dp
                                                            ),
                                                            strokeWidth = 1.5.dp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "Buscando resultados...",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.Gray
                                                        )
                                                    }

                                                    else -> Text(
                                                        "¿Qué buscas?",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester),
                                        shape = RoundedCornerShape(50),
                                        trailingIcon = {
                                            when {
                                                estaGrabando -> {
                                                    IconButton(onClick = {
                                                        try {
                                                            mediaRecorder?.stop()
                                                            mediaRecorder?.release()
                                                            mediaRecorder = null
                                                        } catch (e: Exception) {
                                                            mediaRecorder = null
                                                            viewModel.resetear_microfono()
                                                            return@IconButton
                                                        }
                                                        archivoAudio?.let { file ->
                                                            viewModel.enviar_audio_a_whisper(
                                                                file
                                                            ) { texto ->
                                                                valor_a_buscar = texto.take(500)
                                                                // 👇 nuevo: misma protección aquí también
                                                                focusManager.clearFocus(force = true)
                                                                keyboardController?.hide()
                                                            }
                                                        }
                                                    }) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .scale(pulseScale)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFFEC1707))
                                                        )
                                                    }
                                                }

                                                estaProcesando || mostrar_carga_Respuesta_gemini -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier
                                                            .padding(12.dp)
                                                            .size(22.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.4f
                                                        )
                                                    )
                                                }

                                                valor_a_buscar.isNotEmpty() -> {
                                                    IconButton(
                                                        onClick = {
                                                            if (mostrar_lupa_busqueda && !estaOcupado) {
                                                                focusManager.clearFocus(force = true)
                                                                keyboardController?.hide()
                                                                viewModel.procesar_nlp_open_ia(
                                                                    valor_a_buscar
                                                                )
                                                            }
                                                        },
                                                        enabled = !estaOcupado
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Search,
                                                            contentDescription = "Buscar",
                                                            tint = if (estaOcupado) Color.Gray.copy(
                                                                alpha = 0.4f
                                                            ) else Color.Gray
                                                        )
                                                    }
                                                }

                                                else -> {
                                                    IconButton(onClick = {
                                                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Mic,
                                                            contentDescription = "Micrófono",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            disabledBorderColor = Color.Transparent
                                        ),
                                    )
                                }

                                AnimatedVisibility(visible = valor_a_buscar.isNotEmpty() && !estaOcupado) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "${valor_a_buscar.length}/250",
                                            fontSize = 10.sp,
                                            color = if (valor_a_buscar.length >= 250) Color(
                                                0xFFEC1707
                                            ) else Color.Gray,
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 16.dp, top = 5.dp)
                                        )
                                    }
                                }
                            }// 👈 nuevo: envuelve TODO, incluido el AnimatedVisibility


                        }

                        // ── Header tiendas + filtros ──
                        if (ids_obtenidos_promociones.isNullOrEmpty()) {   // 👈 nuevo: envuelve TODO, incluido el AnimatedVisibility
                            item {
                                AnimatedVisibility(
                                    visible = !estaOcupado,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    val itemFiltros = tiendas_con_mas_de_una_promo(
                                        id = "FILTROS_GENERALES",
                                        nombre_tienda = "Buscar",
                                        logo_img = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
                                        categoira = ""
                                    )
                                    LazyRow(
                                        modifier = Modifier
                                            .animateContentSize()
                                            .padding(top = 5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        item {
                                            estilo_ig_header(
                                                cargando = false,
                                                i = itemFiltros, seleccionada = false,
                                                img_clikeada = {
                                                    if (!estaOcupado) mostar_bottom_sheet_datos =
                                                        true
                                                }
                                            )
                                        }

                                        items(tiendasOrdenadas.take(4), key = { it.id }) { tienda ->
                                            Box(
                                                modifier = Modifier.animateItem(
                                                    placementSpec = tween(
                                                        durationMillis = 400,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                )
                                            ) {
                                                estilo_ig_header(
                                                    cargando = estado_caundo_busca_tienda,
                                                    i = tienda,
                                                    seleccionada = tienda.id == tiendaSeleccionada,
                                                    img_clikeada = { id ->
                                                        if (estaOcupado || estado_caundo_busca_tienda) return@estilo_ig_header   // 👈 AGREGAR estado_caundo_busca_tienda
                                                        Log.d("BUG_TIENDA", "🔵 CLICK en id=$id | tiendaSeleccionada=$tiendaSeleccionada | esperandoConfirmacionTienda=$esperandoConfirmacionTienda | tienda_anterior=$tienda_anterior")

                                                        if (tiendaSeleccionada == id) {
                                                            if (esperandoConfirmacionTienda) {
                                                                Log.d("BUG_TIENDA", "⚠️ Rama CANCELAR (esperandoConfirmacionTienda=true) — revirtiendo a tienda_anterior=$tienda_anterior")
                                                                mostrar_snackbar_tienda = false
                                                                esperandoConfirmacionTienda = false
                                                                tiendaSeleccionada = tienda_anterior
                                                                nombre_tienda_seleccionada = nombre_tienda_anterior
                                                                nombre_tienda_mostrando = nombre_tienda_anterior
                                                                if (tienda_anterior != null) {
                                                                    filtroTiendaAplicado = true
                                                                } else {
                                                                    filtroTiendaAplicado = false
                                                                    tags_tienda_confirmada = emptyList()
                                                                    rangos_tienda_confirmada = emptyList()
                                                                    pagos_tienda_confirmada = emptyList()
                                                                    comodidades_tienda_confirmada = emptyList()
                                                                    viewModel.limpiarSubcategorias()
                                                                    viewModel.limpiarRangoPrecio()
                                                                    viewModel.limpiarMetodosPago()
                                                                    viewModel.limpiar_comodidad()
                                                                    val textoRestaurado = viewModel.restaurar_busqueda_nlp_si_existe()
                                                                    if (textoRestaurado.isNotEmpty()) {
                                                                        valor_a_buscar = textoRestaurado
                                                                        valor_a_buscar_respaldo = ""
                                                                    } else {
                                                                        Log.d("BUG_TIENDA", "🟢 Llamando obtener_promociones_2da(null) desde rama CANCELAR")
                                                                        viewModel.obtener_promociones_2da(localidad, "", null)
                                                                    }
                                                                }
                                                            } else {
                                                                Log.d("BUG_TIENDA", "✅ Rama DESELECCIÓN REAL (esperandoConfirmacionTienda=false) — reseteando todo")
                                                                tiendaSeleccionada = null
                                                                filtrosDesdeBottomSheetGeneral = false
                                                                nombre_tienda_seleccionada = ""
                                                                nombre_tienda_mostrando = ""
                                                                filtroTiendaAplicado = false
                                                                tags_tienda_confirmada = emptyList()
                                                                rangos_tienda_confirmada = emptyList()
                                                                pagos_tienda_confirmada = emptyList()
                                                                comodidades_tienda_confirmada = emptyList()
                                                                viewModel.limpiarSubcategorias()
                                                                viewModel.limpiarRangoPrecio()
                                                                viewModel.limpiarMetodosPago()
                                                                viewModel.limpiar_comodidad()
                                                                Log.d("BUG_TIENDA", "🟢 Llamando obtener_promociones_2da(null) desde rama DESELECCIÓN REAL")
                                                                viewModel.obtener_promociones_2da(localidad, "", null)
                                                            }
                                                        } else {
                                                            Log.d("BUG_TIENDA", "➡️ Seleccionando NUEVA tienda id=$id (anterior=$tiendaSeleccionada)")

                                                            // 🔥 FIX: solo actualizamos tienda_anterior si la tienda actual YA estaba confirmada.
                                                            // Si esperandoConfirmacionTienda=true, tiendaSeleccionada es una selección pendiente
                                                            // (nunca confirmada) y no debe pisar tienda_anterior.
                                                            if (!esperandoConfirmacionTienda) {
                                                                tienda_anterior = tiendaSeleccionada
                                                                nombre_tienda_anterior = nombre_tienda_seleccionada
                                                            }

                                                            tiendaSeleccionada = id
                                                            filtroTiendaAplicado = false
                                                            filtrosDesdeBottomSheetGeneral = false
                                                            nombre_tienda_seleccionada = tienda.nombre_tienda
                                                            esperandoConfirmacionTienda = true
                                                            pagos_tienda_confirmada = emptyList()
                                                            comodidades_tienda_confirmada = emptyList()
                                                            viewModel.limpiarSubcategorias()
                                                            viewModel.limpiarRangoPrecio()
                                                            viewModel.limpiarMetodosPago()
                                                            viewModel.limpiar_comodidad()
                                                            viewModel.obtener_promociones_2da(localidad, "", tiendaSeleccionada)
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        if (tiendasOrdenadas.size > 4) {
                                            item {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.width(80.dp)
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .size(70.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .clickable(
                                                                indication = null,
                                                                interactionSource = remember { MutableInteractionSource() }
                                                            ) { mostrar_todas_tiendas = true }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MoreHoriz,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    texto_generico_one_line(
                                                        "Ver todas",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // ── Banner "ahora viendo tienda" ──
                        item {
                            AnimatedVisibility(
                                visible = tiendaSeleccionada != null &&
                                        nombre_tienda_mostrando.isNotEmpty() &&
                                        nombre_tienda_mostrando != nombre_tienda_seleccionada,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                        .background(
                                            color = Color(0xFF1E1E1E),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF3A3A3A),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = Color(0xFFAAAAAA),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    texto_generico_one_line(
                                        texto = "Ahora estás viendo contenido de $nombre_tienda_mostrando",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                            }
                        }

                        // ── Lista de promos ──
                        items(
                            items = promos,
                            key = { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                        ) { item ->
                            Box(
                                modifier = Modifier.animateItem(
                                    placementSpec = tween(
                                        durationMillis = 350,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            ) {
                                carta_promocion_geinz(
                                    promos_ids=ids_obtenidos_promociones.isNullOrEmpty(),
                                    i = item.dataclass_promociones_cerca_de_ti,
                                    img_clikeble = { id_promo, listaimg, select, id_tienda ->
                                        if (uid_respald_user.isNotEmpty()) {
                                            tienda_seleccionada_clik_baner = id_tienda
                                            promoSeleccionada_unica =
                                                item.dataclass_promociones_cerca_de_ti
                                            mostrar_zoom_img = true
                                            index_galeria_img = select



                                            viewModel.agregar_estadisticas_publicacion(
                                                "click",
                                                id_promo,
                                                localidad,
                                                uid_respald_user
                                            )
                                        } else {
                                            mostrar_bottom_shet_registrate = true
                                        }
                                    },
                                    share_promo = { id_tienda, id, categoria ->
                                        compartir_hosting_promo(
                                            viewmodelPromosCercanas = viewModel,
                                            msje = item.dataclass_promociones_cerca_de_ti.texto_msje_whatsapp.compartir.msje_predermindo,
                                            id_user = uid_respald_user,
                                            context = context,
                                            localidad_tienda = localidad,
                                            idpromo = id,
                                        )
                                        viewModel.agregar_estadisticas_publicacion(
                                            "compartidos",
                                            id,
                                            localidad,
                                            uid_respald_user
                                        )
                                    },
                                    whatsap_promo = { id, id_tienda, _ ->
                                        if (uid_respald_user.isNotEmpty()) {
                                            abrir_whattsapp(
                                                uid_respald_user, "promocion", "", "", context,
                                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.numero,
                                                "${item.dataclass_promociones_cerca_de_ti.texto_msje_whatsapp.whatsapp.msje_predermindo}" +
                                                        "https://geinztech.com/api/share?t=prms&l=$localidad&pi=$id"
                                            )
                                            viewModel.agregar_estadisticas_publicacion(
                                                "whatsapp",
                                                id,
                                                localidad,
                                                uid_respald_user
                                            )
                                        } else {
                                            mostrar_bottom_shet_registrate = true
                                        }
                                    },
                                    mostrar_perfil = { id, id_promo ->
                                        if (uid_respald_user.isNotEmpty()) {
                                            viewModel.agregar_estadisticas_publicacion(
                                                "click_perfil",
                                                id_promo,
                                                localidad,
                                                uid_respald_user
                                            )
                                            show_bottom_sheeet = true
                                            id_tienda_select = id
                                        } else {
                                            mostrar_bottom_shet_registrate = true
                                        }
                                    },
                                    onVerTodas = if (tiendaSeleccionada == null) {
                                        {
                                            val idTiendaDeLaPromo =
                                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_tienda
                                            val nombreTiendaDeLaPromo =
                                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.nombre_tienda
                                            tienda_anterior = tiendaSeleccionada
                                            nombre_tienda_anterior = nombre_tienda_seleccionada
                                            tiendaSeleccionada = idTiendaDeLaPromo
                                            nombre_tienda_seleccionada = nombreTiendaDeLaPromo
                                            filtroTiendaAplicado = false
                                            esperandoConfirmacionTienda = true
                                            pagos_tienda_confirmada = emptyList()
                                            comodidades_tienda_confirmada = emptyList()
                                            viewModel.limpiarCategoria()
                                            viewModel.limpiarSubcategorias()
                                            viewModel.limpiarRangoPrecio()
                                            viewModel.limpiarMetodosPago()
                                            viewModel.limpiar_comodidad()
                                            promosFiltradas = emptyList()
                                            valor_a_buscar_respaldo = valor_a_buscar
                                            valor_a_buscar = ""
                                            viewModel.obtener_promociones_2da(
                                                localidad,
                                                "",
                                                idTiendaDeLaPromo,
                                                valor_a_buscar_respaldo
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // ── Paginación ──
                        item {
                            if (hayMasPaginas) {
                                LaunchedEffect(promos.size) {
                                    if (!cargandoPagina) {
                                        if (viewModel.modoBusquedaIA) {
                                            viewModel.cargarSiguientePaginaPorIds()
                                        } else {
                                            viewModel.cargarSiguientePagina(
                                                localidad,
                                                "",
                                                tiendaSeleccionada
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cargandoPagina) CircularProgressIndicator(
                                        modifier = Modifier.size(
                                            28.dp
                                        )
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Ya viste todas las promos 🎉",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        FiltroSnackbarActivo(
                            categoriaSeleccionada = if (filtrosDesdeBottomSheetGeneral && tiendaSeleccionada == null) categoriaSeleccionada else "",
                            subcategoriasSeleccionadas = if (filtrosDesdeBottomSheetGeneral && tiendaSeleccionada == null) subcategoriasSeleccionadas.toList() else emptyList(),
                            rangoPrecio = if (filtrosDesdeBottomSheetGeneral && tiendaSeleccionada == null) rango_precio else null,
                            metodosPago = if (filtrosDesdeBottomSheetGeneral && tiendaSeleccionada == null) metodo_pago else emptySet(),
                            comodidades = if (filtrosDesdeBottomSheetGeneral && tiendaSeleccionada == null) comodidad_selet else emptySet(),
                            onEditarFiltros = {
                                if (!estaProcesando) mostar_bottom_sheet_datos = true
                            },
                            onLimpiarTodo = {
                                if (estaProcesando) return@FiltroSnackbarActivo
                                filtrosDesdeBottomSheetGeneral = false
                                viewModel.limpiarCategoria()
                                viewModel.limpiarSubcategorias()
                                viewModel.limpiarRangoPrecio()
                                viewModel.limpiarMetodosPago()
                                viewModel.limpiar_comodidad()
                                viewModel.limpiarTerminosNlp()
                                viewModel.obtener_promociones_2da("barranca", "", null)
                                valor_a_buscar = ""
                            },
                            onQuitarCategoria = { viewModel.limpiarCategoria(); viewModel.limpiarSubcategorias() },
                            onQuitarSubcategoria = { sub -> viewModel.toggle_subcategoria(sub) },
                            onQuitarRango = { viewModel.setearRangoPrecioDesdeNLP(null) },
                            onQuitarPago = { pago -> viewModel.toggleMetodoPago(pago) },
                            onQuitarComodidad = { comod -> viewModel.togleRango_select(comod) },
                        )

                        AnimatedVisibility(
                            visible = mostrar_snackbar_tienda,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it },
                            modifier = Modifier.zIndex(10f)
                        ) {
                            snackbar_tienda_con_logo(
                                logo_url = snackbar_logo_tienda,
                                total_promos = snackbar_total_promos,
                                onVer = {
                                    Log.d("BUG_TIENDA", "✔️ CONFIRMADO con 'Ver' — tienda=$snackbar_nombre_tienda_snap | seteando esperandoConfirmacionTienda=false")
                                    mostrar_snackbar_tienda = false
                                    promos = promosFiltradas
                                    esperandoConfirmacionTienda = false
                                    filtroTiendaAplicado = true
                                    nombre_tienda_mostrando = snackbar_nombre_tienda_snap
                                    valor_a_buscar = ""

                                    viewModel.limpiarCategoria()
                                    viewModel.limpiarSubcategorias()
                                    viewModel.limpiarRangoPrecio()
                                    viewModel.limpiarMetodosPago()
                                    viewModel.limpiar_comodidad()
                                    viewModel.limpiarTerminosNlp()
                                    filtrosDesdeBottomSheetGeneral = false

                                    tags_tienda_confirmada = promosFiltradas
                                        .flatMap { it.dataclass_promociones_cerca_de_ti.terminos_clave }
                                        .distinct().filter { it.isNotEmpty() }
                                    rangos_tienda_confirmada = promosFiltradas
                                        .map { it.dataclass_promociones_cerca_de_ti.rango }
                                        .filter { it.isNotEmpty() }.distinct()
                                },
                                onDismiss = {
                                    Log.d("BUG_TIENDA", "❌ DISMISS snackbar — revirtiendo a tienda_anterior=$tienda_anterior")
                                    mostrar_snackbar_tienda = false
                                    esperandoConfirmacionTienda = false
                                    tiendaSeleccionada = tienda_anterior
                                    nombre_tienda_seleccionada = nombre_tienda_anterior
                                    nombre_tienda_mostrando = nombre_tienda_anterior
                                    if (tienda_anterior != null) {
                                        filtroTiendaAplicado = true
                                    } else {
                                        filtroTiendaAplicado = false
                                        tags_tienda_confirmada = emptyList()
                                        rangos_tienda_confirmada = emptyList()
                                        pagos_tienda_confirmada = emptyList()
                                        comodidades_tienda_confirmada = emptyList()
                                        viewModel.limpiarSubcategorias()
                                        viewModel.limpiarRangoPrecio()
                                        viewModel.limpiarMetodosPago()
                                        viewModel.limpiar_comodidad()
                                        valor_a_buscar = viewModel.restaurar_busqueda_nlp_si_existe()
                                        valor_a_buscar_respaldo = ""
                                    }
                                }
                            )
                        }
                    }

                    // ── Botón flotante arrastrable: "ver todas las promos" ──
                    if (!ids_obtenidos_promociones.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        offsetXBurbuja.value.roundToInt(),
                                        offsetYBurbuja.value.roundToInt()
                                    )
                                }
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            scopeBurbuja.launch {
                                                offsetXBurbuja.snapTo(
                                                    (offsetXBurbuja.value + dragAmount.x)
                                                        .coerceIn(
                                                            0f,
                                                            anchoPantallaPx - tamanoBurbujaPx
                                                        )
                                                )
                                                offsetYBurbuja.snapTo(
                                                    (offsetYBurbuja.value + dragAmount.y)
                                                        .coerceIn(
                                                            0f,
                                                            altoPantallaPx - tamanoBurbujaPx
                                                        )
                                                )
                                            }
                                        },
                                        onDragEnd = {
                                            val centroBurbuja =
                                                offsetXBurbuja.value + tamanoBurbujaPx / 2
                                            val destinoX =
                                                if (centroBurbuja < anchoPantallaPx / 2) {
                                                    0f + margenBurbujaPx
                                                } else {
                                                    anchoPantallaPx - tamanoBurbujaPx - margenBurbujaPx
                                                }

                                            // 👇 se queda en la altura donde lo soltaste, no vuelve abajo
                                            val destinoY = offsetYBurbuja.value.coerceIn(
                                                margenBurbujaPx,
                                                altoPantallaPx - tamanoBurbujaPx - margenBurbujaPx
                                            )

                                            scopeBurbuja.launch {
                                                offsetXBurbuja.animateTo(
                                                    targetValue = destinoX,
                                                    animationSpec = tween(durationMillis = 250)
                                                )
                                            }
                                            scopeBurbuja.launch {
                                                offsetYBurbuja.animateTo(
                                                    targetValue = destinoY,
                                                    animationSpec = tween(durationMillis = 250)
                                                )
                                            }
                                        }
                                    )
                                }
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    ids_obtenidos_promociones = null
                                    tiendaSeleccionada = null
                                    filtroTiendaAplicado = false
                                    filtrosDesdeBottomSheetGeneral = false
                                    valor_a_buscar = ""

                                    viewModel.limpiarCategoria()
                                    viewModel.limpiarSubcategorias()
                                    viewModel.limpiarRangoPrecio()
                                    viewModel.limpiarMetodosPago()
                                    viewModel.limpiar_comodidad()
                                    viewModel.limpiarTerminosNlp()
                                    viewModel.resetearModoBusquedaIA()
                                    viewModel.resetear_respuesta_de_gemini()
                                    viewModel.limpiar_estado_promos_completo()
                                    viewModel.obtener_promociones_2da(localidad, "", null)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Ver todas las promos",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                }


                // ── Bottom sheets ──

                if (mostar_bottom_sheet_datos) {
                    if (tiendaSeleccionada != null) {
                        if (tags_tienda_confirmada.isEmpty()) {
                            Toast.makeText(
                                context,
                                "El negocio no cuenta con filtros",
                                Toast.LENGTH_SHORT
                            ).show()
                            mostar_bottom_sheet_datos = false
                        } else {
                            bottom_sheet_filtrar_desde_tienda(
                                nombre_tienda = nombre_tienda_mostrando,
                                lista_filtrado_negocio = tags_tienda_confirmada,
                                rangos_disponibles = rangos_tienda_confirmada,
                                pagos_tienda = pagos_tienda_confirmada,
                                comodidades_tienda = comodidades_tienda_confirmada,
                                id_tienda = tiendaSeleccionada ?: "",
                                viewModel = viewModel,
                                onClose = { mostar_bottom_sheet_datos = false },
                                onAplicarFiltro = { mensaje ->
                                    filtrosDesdeBottomSheetGeneral = false
                                    filtrandoDesdeTienda = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = mensaje,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        bottom_sheet_filtrados_general_promos_ofertas(
                            filtrado_ia = modoBusquedaIA,
                            datos_filtrado = resultado_open_ia,
                            viewModel = viewModel,
                            onClose = {
                                filtrosDesdeBottomSheetGeneral = true
                                mostar_bottom_sheet_datos = false
                            },
                            onAutocompletar = { txt ->
                                valor_a_buscar = txt
                                mostar_bottom_sheet_datos = false
                            },
                            limpiar_textos = { valor_a_buscar = "" },
                        )
                    }
                }

                if (mostrar_zoom_img) {
                    key(tienda_seleccionada_clik_baner) {
                        ZoomableGalleryFullScreenVerticalPager(
                            promociones_de_una_tienda = tienda_seleccionada_clik_baner ?: "",
                            es_la_misma_tienda_o_no = tienda_seleccionada_clik_baner != null,
                            tiendaSeleccionada1 = tiendaSeleccionada,
                            categoria_select_filtro = subCategoriaSeleccionada,
                            id_user = uid_respald_user,
                            viewModel = viewModel,
                            localidad_general = localidad,
                            promoSeleccionada = promoSeleccionada_unica!!,
                            indeximg_seleccionado = index_galeria_img,
                            onDismiss = {
                                mostrar_zoom_img = false
                                tienda_seleccionada_clik_baner = null
                            }, falta_registro_para_whatsapp = {
                                mostrar_bottom_shet_registrate=true
                            }
                        )
                    }
                    return
                }

                if (mostrar_todas_tiendas) {
                    bottom_sheet_todas_las_tiendas(
                        tiendas = tiendasConMasDeUnaPromo,
                        tiendaSeleccionada = tiendaSeleccionada,
                        onTiendaClick = { tienda ->
                            tiendaSeleccionada = tienda.id
                            nombre_tienda_seleccionada = tienda.nombre_tienda
                            filtrosDesdeBottomSheetGeneral = false
                            esperandoConfirmacionTienda = true
                            pagos_tienda_confirmada = emptyList()
                            comodidades_tienda_confirmada = emptyList()
                            viewModel.limpiarSubcategorias()
                            viewModel.limpiarRangoPrecio()
                            viewModel.limpiarMetodosPago()
                            viewModel.limpiar_comodidad()
                            viewModel.obtener_promociones_2da(localidad, "", tienda.id)
                        },
                        onClose = { mostrar_todas_tiendas = false }
                    )
                }

                if (mostrar_bottom_shet_registrate) {
                    bottom_sheet_registrate(
                        ondimis = { mostrar_bottom_shet_registrate = false },
                        iniciar_seccion_normal = {
                            iniciar_seccion()
                            mostrar_bottom_shet_registrate = false
                        },
                        crear_cuenta_geinz = {
                            crear_cuenta()
                            mostrar_bottom_shet_registrate = false
                        },
                        texto_bottom_Sheet = "Inicia sesión ver mas detalles de esta promo o oferta"
                    )
                }

                if (show_bottom_sheeet) {
                    bottom_sheet_tiendas_filtradas(
                        id_tienda_select,
                        localidad,
                        verificar_intener,
                        viewModelFiltros,
                        show_bottom_sheeet
                    ) {
                        show_bottom_sheeet = false
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}


@Composable
fun carta_promocion_geinz(
    promos_ids: Boolean,
    i: dataclass_promociones_cerca_de_ti,
    img_clikeble: (id: String, lista: List<String>, Int, id_tienda: String) -> Unit,
    share_promo: (String, String, String) -> Unit,
    whatsap_promo: (String, id_tienda: String, categoira: String) -> Unit,
    mostrar_perfil: (String, id_promo: String) -> Unit,
    onVerTodas: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var diasRestantes by remember(i.informacion_publcacion.id_promocion) {
        mutableStateOf(
            constantes_datos_expirados_fechas_publicaciones
                .tiempoRestante(i.fecha_fin)
        )
    }
    val (valorRestante, tipo) = parseDiasHorasRestantes(i.dias_restantes)
    Log.d("dias_restantes_obenidos", "${i.dias_restantes}")
    val backgroundColor = when {
        tipo == "dias" -> when {
            valorRestante > 5 -> Color(0xFF15BB1A) // Verde
            valorRestante in 2..5 -> Color(0xFFFF9900) // Naranja
            valorRestante == 1 -> Color(0xFFEC1707) // Rojo
            else -> Color.Gray
        }

        tipo == "horas" -> when {
            valorRestante > 12 -> Color(0xFF15BB1A)
            valorRestante in 6..12 -> Color(0xFFFF9900)
            valorRestante in 1..5 -> Color(0xFFEC1707)
            else -> Color.Gray
        }

        else -> Color.Gray
    }
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))

        ) {
            GaleriaHorizontalInstagram_promociones_solo_imagen(
                i.precio,
                pagos = i.pagos,
                imagenes = i.img.lista_img,
                modifier = Modifier.fillMaxSize(), img_clikeble_valor = { select ->
                    img_clikeble(
                        i.informacion_publcacion.id_promocion,
                        i.img.lista_img,
                        select,
                        i.informacion_publcacion.id_tienda
                    )
                }, long_listatener = {
                    Log.d("LONG_PRESS", "Long press en la galería")
                })


//            texto_generico_one_line("provavilidad de $porcentajeMatch", color = Color.Black)
        }

        Row(
            modifier = Modifier
                .padding(start = 4.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Logo de la tienda
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(i.img.logo_img)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        mostrar_perfil(
                            i.informacion_publcacion.id_tienda,
                            i.informacion_publcacion.id_promocion
                        )
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = i.informacion_publcacion.nombre_tienda.capitalizeFirst(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (i.informacion_publcacion.titulo.isNotEmpty()) {
                    Text(
                        text = i.informacion_publcacion.titulo.capitalizeFirst(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$diasRestantes",
                        fontSize = 12.sp,
                        color = backgroundColor
                    )
                    if (onVerTodas != null) {
                        Text(
                            text = "·",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = if (promos_ids) {
                                "Ver todas las promos"
                            } else {
                                "Ver promos completas"
                            },
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (promos_ids) {

                                    onVerTodas()
                                } else {
                                    img_clikeble(
                                        i.informacion_publcacion.id_promocion,
                                        i.img.lista_img,
                                        1       ,
                                        i.informacion_publcacion.id_tienda
                                    )
                                }
                            }
                        )
                    }
                }
            }

            spacer_horizonta(10.dp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (i.informacion_publcacion.compartir) {
                    Icon(
                        painterResource(R.drawable.comparir_icon),
                        contentDescription = "Compartir",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {

                                val expirada = promoEstaExpirada(i.fecha_fin)

                                if (expirada) {
                                    Toast.makeText(
                                        context,
                                        "La publicación ya caducó",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@clickable
                                }

                                // ✅ Solo si sigue activa
                                share_promo(
                                    i.informacion_publcacion.id_tienda,
                                    i.informacion_publcacion.id_promocion,
                                    i.informacion_publcacion.categoria
                                )
                            }
                    )
                }


                if (i.informacion_publcacion.contactar) {
                    Icon(
                        painterResource(R.drawable.whatsapp_icon),
                        contentDescription = "WhatsApp",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                val expirada = promoEstaExpirada(i.fecha_fin)

                                if (expirada) {
                                    Toast.makeText(
                                        context,
                                        "La publicación ya caducó",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@clickable
                                }

                                whatsap_promo(
                                    i.informacion_publcacion.id_promocion,
                                    i.informacion_publcacion.id_tienda,
                                    i.informacion_publcacion.categoria
                                )
                            },
                        tint = Color.Unspecified
                    )
                }
            }
        }

    }
}


fun parseDiasHorasRestantes(diasRestantesStr: String): Pair<Int, String> {
    val regex = """(\d+)\s*(día|días|hora|horas)""".toRegex()
    val match = regex.find(diasRestantesStr)
    return if (match != null) {
        val valor = match.groupValues[1].toIntOrNull() ?: 0
        val tipo = if (match.groupValues[2].startsWith("día")) "dias" else "horas"
        valor to tipo
    } else {
        0 to "dias"
    }
}


fun compartir_hosting_promo(
    viewmodelPromosCercanas: viewmodel_promos_cercanas,
    msje: String,
    id_user: String,
    context: Context,
    localidad_tienda: String,
    idpromo: String,
) {
    Log.d("menjsame", "$msje")
    try {
        val localidad_pasada = when (localidad_tienda) {
            "barranca" -> "ba"
            "paramonga" -> "par"
            "pativilca" -> "pat"
            "supe" -> "su"
            "puerto supe" -> "pue"
            else -> localidad_tienda
        }
        val link =
            "https://geinztech.com/api/share?" +
                    "t=prms" +
                    "&l=$localidad_pasada" +
                    "&pi=$idpromo"


        val texto = "$msje \n$link"

        // Intent simple de compartir
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }

        // Abrimos el chooser para que el usuario seleccione la app
        context.startActivity(
            Intent.createChooser(intent, "Compartir con")
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        )

        viewmodelPromosCercanas.agregar_estadisticas_publicacion(
            "compartidos",
            idpromo,
            localidad_tienda, id_user
        )
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir el lugar", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun estilo_ig_header(
    cargando: Boolean,
    i: tiendas_con_mas_de_una_promo,
    seleccionada: Boolean,
    img_clikeada: (String) -> Unit
) {
    // Animaciones suaves
    val alphaAnim by animateFloatAsState(
        targetValue = if (seleccionada) 1f else 0f,
        animationSpec = tween(300),
        label = ""
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (seleccionada) 1.12f else 1f,
        animationSpec = tween(300),
        label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(76.dp)
        ) {

            if (cargando && seleccionada) {
                // 🔄 Ring tipo loading SOLO para el seleccionado
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(76.dp)
                )
            } else if (seleccionada) {
                // 🔹 Ring morado cuando está seleccionado
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(scaleAnim)
                        .alpha(alphaAnim)
                        .border(
                            width = 3.dp,
                            color = Color(0xFF7B2CBF),
                            shape = CircleShape
                        )
                )
            }

            // 🔹 Imagen centrada
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(i.logo_img)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp) // 👈 más pequeño que el ring
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        img_clikeada(i.id)
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        texto_generico_one_line(
            i.nombre_tienda,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


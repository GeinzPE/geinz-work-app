package com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.key
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.geinzz.geinzwork.data.model.localizate_geinz.img_con_texto
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data.model.metodos_pagos_agregados_publiaciones
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ZoomableGalleryFullScreenVerticalPager
import com.geinzz.geinzwork.ui.adapters.promoEstaExpirada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_filtrados_promos_y_ofertas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_filtrar_desde_tienda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_todas_las_tiendas
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


@Composable
fun ui_promos_cerca_de_ti(
    flag_identificador: String,
    activar_promo_params: String,
    localidad: String,
    verificar_intener: Boolean,
    iniciar_seccion: () -> Unit,
    crear_cuenta: () -> Unit, onBack: () -> Unit
) {
    Log.d("flag_psada", "$flag_identificador")
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    Log.d("daots", "$activar_promo_params  $localidad $")
    val uid_respald_user by data_store_localidad
        .get_uid_user(context)
        .collectAsState(initial = firebaseAuth.uid.orEmpty())

    val viewModel: viewmodel_promos_cercanas = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()

    val estado by viewModel.estadoPromos.collectAsState()
    val estadoTienda by viewModel.estado_Carga_tienda_select.collectAsState()

    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }

    val respuesta_gemini_NLP by viewModel.respuesta_gemini.collectAsState()


    var tienda_seleccionada_ccon_mas_de_una_promo by remember { mutableStateOf(false) }
    var tags_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    var rangos_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }

    var mostrar_zoom_img by remember { mutableStateOf(false) }
    var lista_img by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var mostrar_snackbar_tienda by remember { mutableStateOf(false) }
    var snackbar_logo_tienda by remember { mutableStateOf("") }
    var snackbar_total_promos by remember { mutableStateOf(0) }
    var snackbar_nombre_tienda_snap by remember { mutableStateOf("") }

    var mostrar_bottom_shet_registrate by remember { mutableStateOf(false) }
    val categoriaSeleccionada by viewModel.categoria_seleccionada.collectAsState()
    val subcategoriasSeleccionadas by viewModel.subcategoria_seleccionada.collectAsState()
    val rango_precio by viewModel.rangoPrecioSeleccionado.collectAsState()
    val comodidad_selet by viewModel.comodidadesSeleccionadas.collectAsState()
    val metodo_pago by viewModel.metodosPagoSeleccionados.collectAsState()

    val lista_resultados_gemini by viewModel.listaResultados.collectAsState()
    var limpiar_campo_de_busqueda by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var mostrar_todas_tiendas by remember { mutableStateOf(false) }
    var tienda_anterior by remember { mutableStateOf<String?>(null) }
    var nombre_tienda_anterior by remember { mutableStateOf("") }
    var pagos_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    var comodidades_tienda_confirmada by remember { mutableStateOf<List<String>>(emptyList()) }
    val lista_filtrados_pagos = remember {
        listOf(
            img_con_texto(R.drawable.yape_logo, "yape"),
            img_con_texto(R.drawable.logo_plin, "plin"),
            img_con_texto(R.drawable.visa_logo, "visa"),
            img_con_texto(R.drawable.master_car_logo, "mastercard"),
            img_con_texto(R.drawable.efectivo_logo, "efectivo"),
            img_con_texto(R.drawable.logo_agora, "agora"),
        )
    }
    val lista_comodidades = remember {
        listOf(
            img_con_texto(R.drawable.icon_wifi, "wifi"),
            img_con_texto(R.drawable.icon_zona_expandida, "zona_expandida"),
            img_con_texto(R.drawable.icon_servicios_higenicos, "servicios_higienicos"),
            img_con_texto(R.drawable.icon_seguridad, "camaras_seguridad"),
            img_con_texto(R.drawable.icon_sala_de_espera, "sala_espera"),
            img_con_texto(R.drawable.icon_sala_para_ninos, "sala_juegos"),
            img_con_texto(R.drawable.icon_mesa_para_ninos, "mesa_para_ninos"),
            img_con_texto(R.drawable.icon_estacionamiento, "estacionamiento"),
            img_con_texto(R.drawable.icon_enchufa, "enchufe"),
            img_con_texto(R.drawable.icon_aire_acondicionado, "aire_acondicionado"),
            img_con_texto(R.drawable.icon_ingreso_animales, "ingreso_con_mascotas"),
        )
    }

    val listaSeleccionada = remember(metodo_pago) {
        lista_filtrados_pagos.filter { item ->
            item.texto in metodo_pago
        }
    }
    val lista_comodidades_Select = remember(comodidad_selet) {
        lista_comodidades.filter { item ->
            item.texto in comodidad_selet
        }
    }
    var index_galeria_img by remember { mutableStateOf(0) }
    var titulo_poromo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var id_tienda_select by remember { mutableStateOf("") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
//    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val viewmodel_repo_datos_promo: viewmodel_datos_promociones = viewModel()
    val datos_promo_parametros by viewmodel_repo_datos_promo.datos_promocion_parametro.collectAsState()
    var tiendaSeleccionada by remember { mutableStateOf<String?>(null) }

    var tienda_seleccionada_clik_baner by remember { mutableStateOf<String?>(null) }
    var nombre_tienda by remember { mutableStateOf("") }
    var img_tienda by remember { mutableStateOf("") }
    var dias_restantes by remember { mutableStateOf("") }
    var promoSeleccionada by remember { mutableStateOf<obj_completo?>(null) }

    val hayMasPaginas by viewModel.hayMasPaginas.collectAsState()
    val cargandoPagina by viewModel.cargandoPagina.collectAsState()
    val filtro_sin_resultados by viewModel.filtro_tienda_sin_resultados.collectAsState()




    var promoSeleccionada_unica by remember {
        mutableStateOf<dataclass_promociones_cerca_de_ti?>(
            null
        )
    }
    var valor_a_buscar by remember { mutableStateOf("") }

    var indexImagenSeleccionada by remember { mutableStateOf(0) }
    var filtrandoDesdeTienda by remember { mutableStateOf(false) } // 🔥 nueva bandera

    // Dentro de tu Composable
    var estadisticasAgregadas by remember { mutableStateOf(false) }

    BackHandler {
        Log.d("NAV_BACK", "Back desde ui_promos_cerca_de_ti")
        onBack()   // 🔥 avisa al NavController
    }


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var yaIntentoCargar by remember { mutableStateOf(false) }
    var datosCargados by remember { mutableStateOf(false) }

    var promoExpirada by remember { mutableStateOf(false) }
    var texto_snackbar by remember { mutableStateOf("") }
    var snackbarMostrado by remember { mutableStateOf(false) }

    var terminoNLP by remember { mutableStateOf<String?>(null) }
    var atributosNLP by remember { mutableStateOf<List<String?>>(emptyList()) }


    val porcentajes by viewModel.porcentajesMatch.collectAsState()

    var cargaFinalizada by remember { mutableStateOf(false) }
    val tiendasConMasDeUnaPromo by viewModel.tiendas_con_mas_de_una_promo.collectAsState()
    var nombre_tienda_seleccionada by remember { mutableStateOf("") }
    var esperandoConfirmacionTienda by remember { mutableStateOf(false) }
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
    var mostar_bottom_sheet_datos by remember { mutableStateOf(false) }
    var filtroTiendaAplicado by remember { mutableStateOf(false) }
    var nombre_tienda_mostrando by remember { mutableStateOf("") }
    LaunchedEffect(activar_promo_params) {

        Log.d("PROMO_FLOW", "▶ LaunchedEffect activar_promo_params = '$activar_promo_params'")

        // reset
        cargaFinalizada = false
        estadisticasAgregadas = false
        snackbarMostrado = false

        if (activar_promo_params.isEmpty()) {
            Log.w("PROMO_FLOW", "⚠ activar_promo_params VACÍO → no se consulta Firestore")
            cargaFinalizada = true
            promoExpirada = false
            return@LaunchedEffect
        }

        Log.d("PROMO_FLOW", "📡 Consultando Firestore con id = $activar_promo_params")

        viewmodel_repo_datos_promo.obtener_datos_promociones_por_paramtros(
            localidad,
            activar_promo_params
        )
    }
    var mostrar_carga_Respuesta_gemini by remember { mutableStateOf(false) }
    var mostrar_lupa_busqueda by remember { mutableStateOf(true) }

    LaunchedEffect(filtro_sin_resultados) {
        if (filtro_sin_resultados) {
//            Toast.makeText(
//                context,
//                "No hay promos con ese filtro en esta tienda",
//                Toast.LENGTH_SHORT
//            ).show()
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

        Log.d("PROMO_FLOW", "▶ LaunchedEffect datos_promo_parametros")

        if (activar_promo_params.isEmpty()) {
            Log.w(
                "PROMO_FLOW",
                "⛔ activar_promo_params vacío"
            )
            return@LaunchedEffect
        }

        val idPromo = datos_promo_parametros.informacion_publcacion.id_promocion

        Log.d("PROMO_FLOW", "🆔 idPromo recibido = '$idPromo'")

        // ⏳ TODAVÍA CARGANDO
        if (idPromo.isEmpty()) {

            Log.d(
                "PROMO_FLOW",
                "⏳ Esperando datos reales de Firestore..."
            )

            return@LaunchedEffect
        }

        cargaFinalizada = true

        val estado_publicaicones =
            datos_promo_parametros.estado_publicacion

        if (estado_publicaicones.equals("pausado", ignoreCase = true)) {

            Log.e("PROMO_FLOW", "⛔ publicación pausada")

            promoExpirada = true

            texto_snackbar =
                "Esta publicación no está disponible en este momento."

            return@LaunchedEffect
        }

        // ⏱️ VALIDAR EXPIRACIÓN
        val expirada =
            promoEstaExpirada(datos_promo_parametros.fecha_fin)

        Log.d(
            "PROMO_FLOW",
            "⏱ Fecha fin = ${datos_promo_parametros.fecha_fin} | Expirada = $expirada"
        )

        if (expirada) {

            Log.e("PROMO_FLOW", "⛔ Promo expirada")

            promoExpirada = true

            return@LaunchedEffect
        }

        // ✅ TODO OK
        Log.d("PROMO_FLOW", "✅ Promo válida → mostrar UI")

        promoExpirada = false
        tienda_seleccionada_clik_baner = datos_promo_parametros
            .informacion_publcacion
            .id_tienda  // ← el campo donde guardas el id de la tienda

        mostrar_zoom_img = true
        promoSeleccionada_unica = datos_promo_parametros

        // 📊 estadísticas
        if (!estadisticasAgregadas) {

            Log.d("PROMO_FLOW", "📊 Agregando estadísticas")

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

        } else {

            Log.d(
                "PROMO_FLOW",
                "ℹ estadísticas ya agregadas"
            )
        }
    }

    LaunchedEffect(estado, promoExpirada) {
        if (
            estado is viewmodel_promos_cercanas.estado_carga_promociones.succes &&
            promoExpirada &&
            !snackbarMostrado
        ) {
            snackbarMostrado = true
            snackbarHostState.showSnackbar(
                message = texto_snackbar,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(localidad) {
        viewModel.obtener_promociones_2da("barranca", "", null)
    }

    var primeraVez by remember { mutableStateOf(true) }

    LaunchedEffect(
        lista_resultados_gemini,
        subCategoriaSeleccionada,
        comodidad_selet,
        metodo_pago,
        rango_precio
    ) {
        Log.d("FILTRO_DEBUG", "lista_resultados_gemini vacío: ${lista_resultados_gemini.isEmpty()}")
        Log.d("FILTRO_DEBUG", "comodidad_selet vacío: ${comodidad_selet.isEmpty()}")
        Log.d("FILTRO_DEBUG", "metodo_pago vacío: ${metodo_pago.isEmpty()}")
        Log.d("FILTRO_DEBUG", "rango_precio es null: ${rango_precio == null}")

        if (primeraVez) {
            primeraVez = false
            return@LaunchedEffect
        }

        val sinFiltros =
            lista_resultados_gemini.isEmpty() &&
                    comodidad_selet.isEmpty() &&
                    metodo_pago.isEmpty()

        if (sinFiltros) {
            Log.d("FILTRO_DEBUG", "SIN FILTROS → cargando todo")
            viewModel.retornar_lista_nuevamente()
            return@LaunchedEffect
        }

        Log.d("FILTRO_DEBUG", "Con filtros → llamando filtrarPromociones")

//        viewModel.filtrarPromociones(
//            subCategoriaSeleccionada,
//            terminoNLP,
//            atributosNLP
//        )
    }

    LaunchedEffect(limpiar_campo_de_busqueda) {
        if (limpiar_campo_de_busqueda) {
            valor_a_buscar = ""
        }
    }
    val esPrimeraCarga by viewModel.esPrimeraCarga.collectAsState()

    var promos by remember { mutableStateOf<List<obj_completo>>(emptyList()) }
    var promosFiltradas by remember { mutableStateOf<List<obj_completo>>(emptyList()) }

    var estado_caundo_busca_tienda by remember { mutableStateOf(false) }
    var loadingSnackbarShown by remember { mutableStateOf(false) }
    val hayFiltros =
        categoriaSeleccionada.isNotEmpty() ||
                subcategoriasSeleccionadas.isNotEmpty() ||
                rango_precio?.isNotEmpty() == true ||
                comodidad_selet?.isNotEmpty() == true ||
                metodo_pago?.isNotEmpty() == true ||
                lista_resultados_gemini.isNotEmpty()
    LaunchedEffect(respuesta_gemini_NLP) {

        when (respuesta_gemini_NLP) {

            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.loading -> {
                mostrar_carga_Respuesta_gemini = true
                mostrar_lupa_busqueda = false

                if (!loadingSnackbarShown) {
                    loadingSnackbarShown = true

                    // 🔥 lanzar en otra corrutina (NO bloquear)
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

                val cantidad = (respuesta_gemini_NLP as viewmodel_promos_cercanas
                .estado_Carga_respuesta_gemini.succes).cantidad
                val datos_respuesta = (respuesta_gemini_NLP as viewmodel_promos_cercanas
                .estado_Carga_respuesta_gemini.succes).items
                Log.d("datos_respuesta", datos_respuesta.toString())
                val msje = if (cantidad > 0) {
                    "Tengo $cantidad resultados para tu búsqueda"
                } else {
                    "Lo siento, no encontré nada para ti"
                }
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

                val mensaje = (respuesta_gemini_NLP as viewmodel_promos_cercanas
                .estado_Carga_respuesta_gemini.empty).text_vacio

                snackbarHostState.currentSnackbarData?.dismiss()

                snackbarHostState.showSnackbar(
                    message = if (mensaje.isNotEmpty())
                        mensaje
                    else
                        "Lo siento, no encontré nada para ti",
                    duration = SnackbarDuration.Short
                )

//                promos = emptyList()

                viewModel.resetear_respuesta_de_gemini()
            }

            is viewmodel_promos_cercanas.estado_Carga_respuesta_gemini.error -> {
                mostrar_carga_Respuesta_gemini = false
                mostrar_lupa_busqueda = true
                loadingSnackbarShown = false

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
            // 🔥 Permitir si viene de filtro interno de tienda
            if (esperandoConfirmacionTienda && !filtrandoDesdeTienda) return@LaunchedEffect

            val nuevos = (estado as viewmodel_promos_cercanas.estado_carga_promociones.succes).items
            promos = nuevos.distinctBy {
                it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
            }
            filtrandoDesdeTienda = false // 🔥 reset
        }
    }
    var loadingTiendaShown by remember { mutableStateOf(false) }
    val resultado_open_ia = viewModel.resultado
    val modoBusquedaIA = viewModel.modoBusquedaIA
    val tags_de_promos_filtradas by remember {
        derivedStateOf {
            if (tiendaSeleccionada != null) {
                promosFiltradas
                    .flatMap { it.dataclass_promociones_cerca_de_ti.terminos_clave }
                    .distinct()
                    .filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }
    }
    val rangos_de_promos_filtradas by remember {
        derivedStateOf {
            if (tiendaSeleccionada != null) {
                promosFiltradas
                    .map { it.dataclass_promociones_cerca_de_ti.rango }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .also {
                        Log.d("RANGOS_DEBUG", "🏪 Rangos de promosFiltradas (tienda '$tiendaSeleccionada'): $it")
                        Log.d("RANGOS_DEBUG", "📦 promosFiltradas.size = ${promosFiltradas.size}")
                    }
            } else {
                Log.d("RANGOS_DEBUG", "⚠️ Sin tienda → vacío")
                emptyList()
            }
        }
    }

    var segundosRestantes by remember { mutableIntStateOf(10) }
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

                val data = estadoTienda as viewmodel_promos_cercanas
                .estado_carga_tienda_Seleccionada.succes
                pagos_tienda_confirmada = data.pagos
                comodidades_tienda_confirmada = data.comodidades
                promosFiltradas = data.items
                snackbarHostState.currentSnackbarData?.dismiss()

                // 🔥 Preparar datos del snackbar custom
                snackbar_total_promos = data.total
                snackbar_nombre_tienda_snap = nombre_tienda_seleccionada
                snackbar_logo_tienda = tiendasConMasDeUnaPromo
                    .find { it.id == tiendaSeleccionada }?.logo_img ?: ""
                esperandoConfirmacionTienda = true
                mostrar_snackbar_tienda = true
            }

            is viewmodel_promos_cercanas.estado_carga_tienda_Seleccionada.error -> {
                estado_caundo_busca_tienda = false
                loadingTiendaShown = false

                // ❌ cerrar snackbar si falla
                snackbarHostState.currentSnackbarData?.dismiss()
            }

            else -> {
                estado_caundo_busca_tienda = false
                loadingTiendaShown = false
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        when (estado) {

            // ---------- LOADING ----------
            viewmodel_promos_cercanas.estado_carga_promociones.loading -> {
                if (esPrimeraCarga) {
                    // Primera vez → CircularProgressIndicator normal
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }

            }

            // ---------- EMPTY ----------
            is viewmodel_promos_cercanas.estado_carga_promociones.empty -> {
                val txt =
                    (estado as viewmodel_promos_cercanas.estado_carga_promociones.empty).txt

                Text(
                    text = txt,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ---------- ERROR ----------
            is viewmodel_promos_cercanas.estado_carga_promociones.error -> {
                val txt =
                    (estado as viewmodel_promos_cercanas.estado_carga_promociones.error).txt

                Text(
                    text = txt,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ---------- SUCCESS ----------
            is viewmodel_promos_cercanas.estado_carga_promociones.succes -> {

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .animateContentSize(),
                    ) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                                texto_generico_multilinea(
                                    "Promos y ofertas cerca de ti",
                                    style = MaterialTheme.typography.banerGeinzWork
                                )
                                spacer_vertical(5.dp)
                                texto_generico_multilinea(
                                    "Descubre descuentos, promociones especiales y ofertas exclusivas de negocios cercanos.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                            }
                        }

                        item {
                            if (!hayFiltros && !filtroTiendaAplicado && tiendaSeleccionada == null) {

                                val estadoMicrofono by viewModel.estadoMicrofono.collectAsState()
                                var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
                                var archivoAudio by remember { mutableStateOf<File?>(null) }
                                val estaGrabando = estadoMicrofono is viewmodel_promos_cercanas.EstadoMicrofono.Grabando
                                val estaProcesando = estadoMicrofono is viewmodel_promos_cercanas.EstadoMicrofono.Procesando
                                val estaOcupado = estaGrabando || estaProcesando || mostrar_carga_Respuesta_gemini

                                val launcher = rememberLauncherForActivityResult(
                                    ActivityResultContracts.RequestPermission()
                                ) { granted ->
                                    if (granted) {
                                        val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
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
                                        segundosRestantes = 10  // 🔥 reset
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
                                        // ⏱️ Tiempo agotado → parar y enviar automáticamente
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
                                            }
                                        }
                                    } else {
                                        segundosRestantes = 10  // 🔥 reset cuando para
                                    }
                                }
                                DisposableEffect(Unit) {
                                    onDispose {
                                        mediaRecorder?.apply {
                                            try { stop() } catch (e: Exception) { }
                                            release()
                                        }
                                        mediaRecorder = null
                                        viewModel.resetear_microfono()
                                    }
                                }

                                // pulso animado para el micrófono
                                val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                                val pulseScale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulse_scale"
                                )
                                val pulseAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 0.7f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulse_alpha"
                                )

                                LoadingOutlinedField(loading = mostrar_carga_Respuesta_gemini) {
                                    OutlinedTextField(
                                        value = valor_a_buscar,
                                        onValueChange = {
                                            // 🔥 bloquear escritura cuando está ocupado
                                            if (!estaOcupado && it.length <= 500) valor_a_buscar = it
                                        },
                                        readOnly = estaOcupado, // 🔥 bloquea teclado
                                        placeholder = {
                                            AnimatedContent(
                                                targetState = when {
                                                    estaGrabando -> "grabando"
                                                    estaProcesando -> "procesando"
                                                    mostrar_carga_Respuesta_gemini -> "buscando"
                                                    else -> "idle"
                                                },
                                                label = "placeholder_anim"
                                            ) { estado ->
                                                when (estado) {
                                                    "grabando" -> Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .scale(pulseScale)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFEC1707).copy(alpha = pulseAlpha))
                                                        )
                                                        Text(
                                                            "Escuchando...",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color(0xFFEC1707)
                                                        )
                                                        // 🔥 Contador regresivo
                                                        Text(
                                                            text = "${segundosRestantes}s",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (segundosRestantes <= 3) Color(0xFFEC1707) else Color(0xFFAAAAAA)
                                                        )
                                                    }
                                                    "procesando" -> Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(12.dp),
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
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(12.dp),
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
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(50),
                                        trailingIcon = {
                                            when {
                                                // 🔴 GRABANDO → cuadrado rojo pulsante
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
                                                            viewModel.enviar_audio_a_whisper(file) { texto ->
                                                                valor_a_buscar = texto.take(500)
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

                                                // ⏳ PROCESANDO o BUSCANDO → spinner bloqueado
                                                estaProcesando || mostrar_carga_Respuesta_gemini -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier
                                                            .padding(12.dp)
                                                            .size(22.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                    )
                                                }

                                                // 🔍 HAY TEXTO → lupa
                                                valor_a_buscar.isNotEmpty() -> {
                                                    IconButton(onClick = {
                                                        if (mostrar_lupa_busqueda) {
                                                            viewModel.procesar_nlp_open_ia(valor_a_buscar)
                                                        }
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Search,
                                                            contentDescription = "Buscar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                }

                                                // 🎙️ VACÍO → micrófono
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
                                            color = if (valor_a_buscar.length >= 250) Color(0xFFEC1707) else Color.Gray,
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 16.dp , top = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                        item {
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

                                // 🔥 Nuevo item de filtros generales
                                item {

                                    // 🔹 Logueamos que no hay tienda seleccionada
                                    Log.d(
                                        "DEBUG_FILTROS",
                                        "No hay tienda seleccionada, mostrando FILTROS_GENERALES"
                                    )

                                    val condicion =
                                        (rango_precio != null && rango_precio != "Sin precio") ||
                                                listaSeleccionada.isNotEmpty() ||
                                                lista_comodidades_Select.isNotEmpty() ||
                                                lista_resultados_gemini.isNotEmpty()

                                    // 🔹 Logueamos el estado de cada filtro
                                    Log.d("DEBUG_FILTROS", "Rango precio activo: ${rango_precio}")
                                    Log.d(
                                        "DEBUG_FILTROS",
                                        "Métodos seleccionados: ${listaSeleccionada.isNotEmpty()}"
                                    )
                                    Log.d(
                                        "DEBUG_FILTROS",
                                        "Comodidades seleccionadas: ${lista_comodidades_Select.isNotEmpty()}"
                                    )
                                    Log.d(
                                        "DEBUG_FILTROS",
                                        "Resultados Gemini: ${lista_resultados_gemini.isNotEmpty()}"
                                    )
                                    Log.d("DEBUG_FILTROS", "Condición general: $condicion")

                                    estilo_ig_header(
                                        false,
                                        "FILTROS_GENERALES",
                                        condicion,
                                        i = itemFiltros,
                                        seleccionada = false,
                                        img_clikeada = { id ->

                                            Log.d(
                                                "DEBUG_FILTROS",
                                                "Click en FILTROS_GENERALES, id: $id"
                                            )
                                            mostar_bottom_sheet_datos = true
                                        }
                                    )

                                }

                                if (rango_precio != null && !rango_precio.equals("Sin precio")) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(28.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .height(99.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Spacer(modifier = Modifier.height(7.dp))
                                                rango_precio?.let { i ->
                                                    estilo_para_metodo_de_pago(i, { des ->
                                                        viewModel.setearRangoPrecioDesdeNLP(null)
                                                    })
                                                }
                                            }
                                        }
                                    }
                                }

                                // 🔹 Si hay métodos seleccionados → mostrar esos
                                if (listaSeleccionada.isNotEmpty()) {
                                    item {

                                        Surface(
                                            shape = RoundedCornerShape(28.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {

                                            Column(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .height(99.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // 🔹 Chips
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    listaSeleccionada.forEach { item ->
                                                        estilo_chips_circular_Select(
                                                            item,
                                                            { select -> },
                                                            { texto ->
                                                                viewModel.toggleMetodoPago(texto)
                                                            })
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // 🔹 Texto
                                                texto_generico_one_line(
                                                    texto = "Métodos de pago",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }


                                if (lista_comodidades_Select.isNotEmpty()) {
                                    item {

                                        Surface(
                                            shape = RoundedCornerShape(28.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {

                                            Column(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .height(99.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // 🔹 Chips
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    lista_comodidades_Select.forEach { item ->
                                                        estilo_chips_circular_Select(
                                                            item,
                                                            { select -> },
                                                            { texto ->
                                                                viewModel.togleRango_select(texto)
                                                            })
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // 🔹 Texto
                                                texto_generico_one_line(
                                                    texto = "Comodidades",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }

                                if (listaSeleccionada.isEmpty() &&
                                    lista_comodidades_Select.isEmpty() &&
                                    (rango_precio == null || rango_precio == "Sin precio") &&
                                    lista_resultados_gemini.isEmpty()
                                ) {
                                    items(tiendasOrdenadas.take(4), key = { it.id }) { tienda ->
                                        Box(
                                            modifier = Modifier.animateItem(
                                                placementSpec = tween(
                                                    durationMillis = 400,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        ){
                                        estilo_ig_header(
                                            estado_caundo_busca_tienda,
                                            "",
                                            false,
                                            i = tienda,
                                            seleccionada = tienda.id == tiendaSeleccionada,
                                            img_clikeada = { id ->
                                                if (tiendaSeleccionada == id) {
                                                    tiendaSeleccionada = null
                                                    nombre_tienda_seleccionada = ""
                                                    nombre_tienda_mostrando = ""
                                                    esperandoConfirmacionTienda = false
                                                    filtroTiendaAplicado = false
                                                    tags_tienda_confirmada = emptyList()
                                                    rangos_tienda_confirmada = emptyList()
                                                    pagos_tienda_confirmada = emptyList()        // 🔥
                                                    comodidades_tienda_confirmada = emptyList()  // 🔥
                                                    viewModel.limpiarSubcategorias()
                                                    viewModel.limpiarRangoPrecio()
                                                    viewModel.limpiarMetodosPago()               // 🔥
                                                    viewModel.limpiar_comodidad()                // 🔥
                                                    viewModel.obtener_promociones_2da(localidad, "", null)
                                                } else {
                                                    tienda_anterior = tiendaSeleccionada
                                                    nombre_tienda_anterior = nombre_tienda_seleccionada

                                                    tiendaSeleccionada = id
                                                    filtroTiendaAplicado = false
                                                    nombre_tienda_seleccionada = tienda.nombre_tienda
                                                    esperandoConfirmacionTienda = false
//                                                    esperandoConfirmacionTienda = true
                                                    pagos_tienda_confirmada = emptyList()        // 🔥
                                                    comodidades_tienda_confirmada = emptyList()  // 🔥
                                                    viewModel.limpiarSubcategorias()
                                                    viewModel.limpiarRangoPrecio()
                                                    viewModel.limpiarMetodosPago()               // 🔥
                                                    viewModel.limpiar_comodidad()                // 🔥
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
                                                        ) {
                                                            mostrar_todas_tiendas = true
                                                        }
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            imageVector = Icons.Default.MoreHoriz,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(22.dp)
                                                        )

                                                    }
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
                        items(
                            items = promos,
                            key = { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                        ) { item ->
                            val idPromo = item.dataclass_promociones_cerca_de_ti
                                .informacion_publcacion.id_promocion

                            Log.d("pagospromops", "${item.dataclass_promociones_cerca_de_ti.pagos}")
                            val porcentajeMatch = porcentajes[idPromo] ?: 0
                            val index = promos.indexOfFirst {
                                it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion ==
                                        item.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
                            }
                            Box(
                                modifier = Modifier.animateItem(
                                    placementSpec = tween(
                                        durationMillis = 350,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            ) {
                                carta_promocion_geinz(
                                    porcentajeMatch,
                                    i = item.dataclass_promociones_cerca_de_ti,
                                    img_clikeble = { id_promo, listaimg, select, id_tienda ->
                                        if (uid_respald_user.isNotEmpty()) {
                                            Log.d(
                                                "mostramosooom",
                                                "$id_promo ${listaimg.size} $select"
                                            )
                                            tienda_seleccionada_clik_baner = id_tienda
                                            promoSeleccionada = item
                                            // ✅ Usar el index calculado
                                            promoSeleccionada_unica =
                                                item.dataclass_promociones_cerca_de_ti
                                            indexImagenSeleccionada = select
                                            mostrar_zoom_img = true
                                            lista_img = listaimg
                                            index_galeria_img = select
                                            titulo_poromo =
                                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.titulo
                                            descripcion =
                                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.descripcion
                                            nombre_tienda =
                                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.nombre_tienda
                                            img_tienda =
                                                item.dataclass_promociones_cerca_de_ti.img.logo_img
                                            dias_restantes =
                                                item.dataclass_promociones_cerca_de_ti.dias_restantes

                                            viewModel.agregar_estadisticas_publicacion(
                                                "click",
                                                id_promo,
                                                localidad, uid_respald_user
                                            )

                                        } else {
                                            mostrar_bottom_shet_registrate = true

                                        }
                                    },
                                    share_promo = { id_tienda, id, categoria ->
                                        compartir_hosting_promo(
                                            viewModel,
                                            item.dataclass_promociones_cerca_de_ti.texto_msje_whatsapp.compartir.msje_predermindo,
                                            uid_respald_user,
                                            id_tienda,
                                            context,
                                            localidad,
                                            id,
                                            categoria
                                        )
                                        viewModel.agregar_estadisticas_publicacion(
                                            "compartidos",
                                            id,
                                            localidad, uid_respald_user
                                        )
                                    },
                                    whatsap_promo = { id, id_tienda, categoira ->
                                        if (uid_respald_user.isNotEmpty()) {
                                            abrir_whattsapp(
                                                uid_respald_user,
                                                "promocion",
                                                "",
                                                "",
                                                context,
                                                item.dataclass_promociones_cerca_de_ti
                                                    .informacion_publcacion.numero,
                                                "${item.dataclass_promociones_cerca_de_ti.texto_msje_whatsapp.whatsapp.msje_predermindo}" +
                                                        "https://geinzworkapp.web.app/api/share?" +
                                                        "t=prms" +
                                                        "&l=$localidad" +
                                                        "&pi=$id"

                                            )
                                            viewModel.agregar_estadisticas_publicacion(
                                                "whatsapp",
                                                id,
                                                localidad, uid_respald_user
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
                                                localidad, uid_respald_user
                                            )
                                            show_bottom_sheeet = true
                                            id_tienda_select = id
                                        } else {
                                            mostrar_bottom_shet_registrate = true
                                        }
                                    },onVerTodas = if (tiendaSeleccionada == null) {
                                        {
                                            val idTiendaDeLaPromo = item.dataclass_promociones_cerca_de_ti
                                                .informacion_publcacion.id_tienda
                                            val nombreTiendaDeLaPromo = item.dataclass_promociones_cerca_de_ti
                                                .informacion_publcacion.nombre_tienda

                                            tienda_anterior = tiendaSeleccionada
                                            nombre_tienda_anterior = nombre_tienda_seleccionada

                                            tiendaSeleccionada = idTiendaDeLaPromo
                                            nombre_tienda_seleccionada = nombreTiendaDeLaPromo
                                            filtroTiendaAplicado = false
                                            esperandoConfirmacionTienda = true  // ✅ CAMBIO: true para bloquear update automático
                                            pagos_tienda_confirmada = emptyList()
                                            comodidades_tienda_confirmada = emptyList()

                                            viewModel.limpiarCategoria()
                                            viewModel.limpiarSubcategorias()
                                            viewModel.limpiarRangoPrecio()
                                            viewModel.limpiarMetodosPago()
                                            viewModel.limpiar_comodidad()
                                            viewModel.limpiarTerminosNlp()
                                            viewModel.resetearModoBusquedaIA()
                                            viewModel.resetear_respuesta_de_gemini()
                                            promosFiltradas = emptyList()
                                            valor_a_buscar = ""
                                            limpiar_campo_de_busqueda = true
                                            viewModel.obtener_promociones_2da(localidad, "", idTiendaDeLaPromo)
                                        }
                                    } else null
                                )
                            }


                        }
                        // 🔹 Trigger de carga al llegar al último item
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
                                    if (cargandoPagina) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                    }
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
                    AnimatedVisibility(
                        visible = mostrar_snackbar_tienda,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(10f)
                    ) {
                        snackbar_tienda_con_logo(
                            logo_url = snackbar_logo_tienda,
                            total_promos = snackbar_total_promos,
                            onVer = {
                                mostrar_snackbar_tienda = false
                                promos = promosFiltradas
                                filtroTiendaAplicado = true
                                nombre_tienda_mostrando = snackbar_nombre_tienda_snap
                                valor_a_buscar = ""                // 🔥
                                tags_tienda_confirmada = promosFiltradas
                                    .flatMap { it.dataclass_promociones_cerca_de_ti.terminos_clave }
                                    .distinct().filter { it.isNotEmpty() }
                                rangos_tienda_confirmada = promosFiltradas
                                    .map { it.dataclass_promociones_cerca_de_ti.rango }
                                    .filter { it.isNotEmpty() }.distinct()
                            },
                            onDismiss = {
                                mostrar_snackbar_tienda = false
                                esperandoConfirmacionTienda = false

                                tiendaSeleccionada = tienda_anterior
                                nombre_tienda_seleccionada = nombre_tienda_anterior
                                nombre_tienda_mostrando = nombre_tienda_anterior

                                if (tienda_anterior != null) {
                                    filtroTiendaAplicado = true
                                    // 🔥 promos ya tienen los datos correctos de tienda_anterior
                                    // NO llamar viewModel, NO tocar promos
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
                                    viewModel.obtener_promociones_2da(localidad, "", null)
                                }
                            }
                        )
                    }
                    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))

                }

                if (mostar_bottom_sheet_datos) {
                    Log.d("SHEET_DEBUG", "🔴 mostar_bottom_sheet_datos = true")
                    Log.d("SHEET_DEBUG", "🏪 tiendaSeleccionada = $tiendaSeleccionada")
                    Log.d("SHEET_DEBUG", "🏷️ tags_de_promos_filtradas = $tags_de_promos_filtradas")

                    if (tiendaSeleccionada != null) {
                        if (tags_tienda_confirmada.isEmpty()) {
                            Log.d("SHEET_DEBUG", "⚠️ Sin tags → Toast")
                            Toast.makeText(context, "El negocio no cuenta con filtros", Toast.LENGTH_SHORT).show()
                            mostar_bottom_sheet_datos = false
                        } else {
                            Log.d("SHEET_DEBUG", "✅ Entrando a bottom_sheet_filtrar_desde_tienda")
                            bottom_sheet_filtrar_desde_tienda(
                                nombre_tienda = nombre_tienda_mostrando,
                                lista_filtrado_negocio = tags_tienda_confirmada,
                                rangos_disponibles = rangos_tienda_confirmada,
                                pagos_tienda = pagos_tienda_confirmada,           // 🔥 falta
                                comodidades_tienda = comodidades_tienda_confirmada, // 🔥 falta
                                id_tienda = tiendaSeleccionada ?: "",
                                viewModel = viewModel,
                                onClose = { mostar_bottom_sheet_datos = false },
                                onAplicarFiltro = { mensaje ->
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
                        Log.d("SHEET_DEBUG", "✅ Entrando a bottom_sheet_filtrados_promos_y_ofertas")
                        bottom_sheet_filtrados_promos_y_ofertas(

                            filtrado_ia = modoBusquedaIA,
                            datos_filtrado = resultado_open_ia,
                            comodidad_selet = comodidad_selet,
                            metodo_pago = metodo_pago,
                            rango_precio = rango_precio,
                            viewModel = viewModel,
                            onClose = { mostar_bottom_sheet_datos = false },
                            onAutocompletar = { txt ->
                                valor_a_buscar = txt
                                mostar_bottom_sheet_datos = false
                            },{
                                valor_a_buscar = ""
                            }
                        )
                    }
                }
//                if (mostrar_zoom_img && promoSeleccionada != null) {
                if (mostrar_zoom_img) {
                    key(tienda_seleccionada_clik_baner) { // 🔹 fuerza recreación al cambiar tienda
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
                                promoSeleccionada = null
                                tienda_seleccionada_clik_baner = null
                            },
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
                            esperandoConfirmacionTienda = false
                            pagos_tienda_confirmada = emptyList()        // 🔥
                            comodidades_tienda_confirmada = emptyList()  // 🔥
                            viewModel.limpiarSubcategorias()
                            viewModel.limpiarRangoPrecio()
                            viewModel.limpiarMetodosPago()               // 🔥
                            viewModel.limpiar_comodidad()                // 🔥
                            viewModel.obtener_promociones_2da(localidad, "", tienda.id)
                        },
                        onClose = { mostrar_todas_tiendas = false }
                    )
                }
                if (mostrar_bottom_shet_registrate) {
                    bottom_sheet_registrate(
                        ondimis = {
                            mostrar_bottom_shet_registrate = false
                        },
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
//                        dataclass_tienda_seleccionada,
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
    porcentajeMatch: Int,
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
                i.pagos,
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
                        Text(text = "·", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "Ver todas las promos",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onVerTodas() }
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
    // Ejemplos de strings que podrías tener: "3 días restantes" o "5 horas restantes"
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
    id_tienda: String,
    context: Context,
    localidad_tienda: String,
    idpromo: String,
    categoria: String,
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
            "https://geinzworkapp.web.app/api/share?" +
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
    termino_condicion: String,
    condicon: Boolean,
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

@Composable
fun estilo_para_metodo_de_pago(rango_select: String, deseleccionar: (String) -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 80.dp)
    ) {
        Box(
            modifier = Modifier
                .height(55.dp)
                .widthIn(min = 55.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    deseleccionar(rango_select)
                }, contentAlignment = Alignment.Center
        ) {
            texto_generico_one_line(
                rango_select,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        texto_generico_one_line(
            "Precio seleccionado",
            style = MaterialTheme.typography.bodySmall
        )

    }

}

@Composable
fun estilo_chips_circular_Select(
    datos: img_con_texto,
    onClickImagen: (img_con_texto) -> Unit,
    onClickEliminar: (String) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {

        Box(
            modifier = Modifier.size(65.dp),
            contentAlignment = Alignment.Center
        ) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(datos.img)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onClickEliminar(datos.texto)
                    }
            )

        }
    }
}

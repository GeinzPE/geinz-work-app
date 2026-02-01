package com.geinzz.geinzwork.ui.adapters

import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.compartir_contacto_pulicaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.tiempoRestante
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen_promociones
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.compartir_hosting_promo
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableGalleryFullScreenVerticalPager(
    tiendaSeleccionada1: String?,
    categoria_select_filtro: String,
    id_user: String,
    viewModel: viewmodel_promos_cercanas,
    localidad_general: String,
    promoSeleccionada: dataclass_promociones_cerca_de_ti,
    indeximg_seleccionado: Int,
    onDismiss: () -> Unit,
) {
    Log.d("lodisias_restantes_publica","${promoSeleccionada.fecha_fin}")

    val context = LocalContext.current
    val listaPromos by viewModel.promosCargadas.collectAsState()


    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    var idTiendaSelect by remember { mutableStateOf("") }

    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    var tiendaSeleccionada by remember { mutableStateOf(modelo_tienda()) }

    // ---------------- FEED ----------------
    var feedInicializado by remember { mutableStateOf(false) }
    var feedVisible by remember {
        mutableStateOf<List<dataclass_promociones_cerca_de_ti>>(emptyList())
    }
    var dias_restantes by remember() {mutableStateOf("") }


    // Inicializar feed (promo seleccionada primero)
    LaunchedEffect(listaPromos) {
        if (!feedInicializado && listaPromos.isNotEmpty()) {
            val resto = listaPromos
                .filter {
                    it.informacion_publcacion.id_promocion !=
                            promoSeleccionada.informacion_publcacion.id_promocion
                }
                .shuffled()

            feedVisible = listOf(promoSeleccionada) + resto
            feedInicializado = true
        }
    }

    // ---------------- PAGER ----------------
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { feedVisible.size }
    )

    // ---------------- CARGA INICIAL ----------------
    LaunchedEffect(Unit) {
        viewModel.cargarSiguienteBloque(localidad_general,categoria_select_filtro,tiendaSeleccionada1)
    }

    // ---------------- BOTTOM SHEET ----------------
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localidad_general,
                idTiendaSelect
            )
        }
    }

    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            tiendaSeleccionada = datosTienda!!.first()
        }
    }

    // ---------------- SCROLL INFINITO ----------------
    var solicitandoBloque by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        if (
            pagerState.currentPage >= feedVisible.size - 2 &&
            !solicitandoBloque
        ) {
            solicitandoBloque = true
            viewModel.cargarSiguienteBloque(localidad_general,categoria_select_filtro,tiendaSeleccionada1)
        }
    }

    // Agregar nuevas promos sin duplicar
    LaunchedEffect(listaPromos) {
        if (solicitandoBloque) {
            val existentes = feedVisible
                .map { it.informacion_publcacion.id_promocion }
                .toSet()

            val nuevas = listaPromos.filter {
                it.informacion_publcacion.id_promocion !in existentes
            }

            if (nuevas.isNotEmpty()) {
                feedVisible = feedVisible + nuevas
            }

            solicitandoBloque = false
        }
    }

    // ---------------- REGISTRO DE VISTAS ----------------
    var viewJob by remember { mutableStateOf<Job?>(null) }
    val vistasRegistradas = remember { mutableSetOf<String>() }

    LaunchedEffect(pagerState.currentPage) {
        viewJob?.cancel()

        if (feedVisible.isEmpty()) return@LaunchedEffect

        val promoActual = feedVisible[pagerState.currentPage]
        val idPromo = promoActual.informacion_publcacion.id_promocion

        viewJob = scope.launch {
            delay(1500)

            val sigueVisible =
                pagerState.currentPage == feedVisible.indexOf(promoActual)

            if (sigueVisible && !vistasRegistradas.contains(idPromo)) {
                vistasRegistradas.add(idPromo)

                viewModel.agregar_estadisticas_publicacion(
                    tipo = "vistas",
                    id_promo = idPromo,
                    localidad = localidad_general, id_user
                )
            }
        }
    }
    var mostrarLoader by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        dias_restantes=   constantes_datos_expirados_fechas_publicaciones.tiempoRestante(
            promoSeleccionada.fecha_fin
        )
        delay(3000) // ⏱️ 3 segundos
        mostrarLoader = false
    }
    // ---------------- UI ----------------
    Box(modifier = Modifier.fillMaxSize()) {

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AnimatedVisibility(
                    visible = !mostrarLoader,
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->

                        val promo = feedVisible[index]

                        val diasRestantes by produceState(
                            initialValue = "",
                            key1 = promo.informacion_publcacion.id_promocion
                        ) {
                            while (true) {
                                value = tiempoRestante(promo.fecha_fin)
                                delay(30_000) // cada 30s
                            }
                        }



                        val datos = compartir_contacto_pulicaciones(
                            promo.informacion_publcacion.id_promocion,
                            iod_tienda = promo.informacion_publcacion.id_tienda,
                            localidad_tineda = localidad_general,
                            categoria = promo.informacion_publcacion.categoria,
                            numero_contacto = promo.informacion_publcacion.numero,
                            dias_restantes = diasRestantes,
                            logo_img = promo.img.logo_img,
                            nombre_tienda = promo.informacion_publcacion.nombre_tienda
                        )
                        LaunchedEffect(promo.informacion_publcacion.id_promocion) {
                            viewModel.cargarStats(
                                localidad_general,
                                promo.informacion_publcacion.id_promocion
                            )
                        }

                        val stats = viewModel.statsCache[promo.informacion_publcacion.id_promocion]

                        ZoomableGalleryFullScreen_promociones(
                            stats,
                            i = datos,
                            titulo = promo.informacion_publcacion.titulo,
                            txt = promo.informacion_publcacion.descripcion,
                            imagenes = promo.img.lista_img,
                            startIndex = if (index == 0) indeximg_seleccionado else 0,
                            onDismiss = {
                                onDismiss()
                                viewModel.resetPromos()
                            },
                            clikc_compartir = { idPromo, categoria, localidad, idTienda ->
                                val expirada = promoEstaExpirada(promo.fecha_fin)
                                if (expirada) {
                                    Toast.makeText(
                                        context,
                                        "La publicación ya caducó",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@ZoomableGalleryFullScreen_promociones
                                }


                                compartir_hosting_promo(
                                    viewModel,
                                    promo.texto_msje_whatsapp.compartir.msje_predermindo,
                                    id_user,
                                    idTienda,
                                    context,
                                    localidad,
                                    idPromo,
                                    categoria
                                )

                                viewModel.agregar_estadisticas_publicacion(
                                    "compartidos",
                                    idPromo,
                                    localidad, id_user,
                                )

                            },
                            click_contacto_directo = { id, numero, localidad, id_tienda,categoira ->
                                val expirada = promoEstaExpirada(promo.fecha_fin)
                                if (expirada) {
                                    Toast.makeText(
                                        context,
                                        "La publicación ya caducó",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@ZoomableGalleryFullScreen_promociones
                                }

                                abrir_whattsapp(
                                    id_user,
                                    "promocion",
                                    "",
                                    "",
                                    context,
                                    numero,
                                    "${promo.texto_msje_whatsapp.whatsapp.msje_predermindo}" +
                                            "https://geinzworkapp.web.app/share?" +
                                            "t=prms" +
                                            "&l=$localidad" +
                                            "&pi=$id"
                                )
                                viewModel.agregar_estadisticas_publicacion(
                                    "whatsapp",
                                    id,
                                    localidad, id_user,
                                )


                            },
                            abrir_prefil = { idTienda ->

                                idTiendaSelect = idTienda
                                showBottomSheet = true

                            }
                        )
                    }

                }
                // ---------- LOADER OVERLAY ----------
                AnimatedVisibility(
                    visible = mostrarLoader,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ShimmerImagenConMarca()
                }
            }
        }


        if (showBottomSheet) {
            bottom_sheet_tiendas_filtradas(
                true,
                viewModelFiltros,
                tiendaSeleccionada,
                showBottomSheet
            ) {
                showBottomSheet = false
            }
        }
    }
}

fun promoEstaExpirada(fechaFin: Timestamp): Boolean {
    return constantes_datos_expirados_fechas_publicaciones
        .tiempoRestante(fechaFin) == "Expirado"
}


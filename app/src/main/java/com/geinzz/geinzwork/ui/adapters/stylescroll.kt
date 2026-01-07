package com.geinzz.geinzwork.ui.adapters

import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen_promociones
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.compartir_hosting_promo
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableGalleryFullScreenVerticalPager(
    viewModel: viewmodel_promos_cercanas,
    localidad_general: String,
    promoSeleccionada: dataclass_promociones_cerca_de_ti,
    indeximg_seleccionado: Int,
    onDismiss: () -> Unit
) {
    val context= LocalContext.current
    val listaPromos by viewModel.promosCargadas.collectAsState()
    val threshold = 2
    val scope = rememberCoroutineScope()
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }


    // Feed completo sin duplicados
    var feedVisible by remember { mutableStateOf<List<dataclass_promociones_cerca_de_ti>>(emptyList()) }
    var id_tienda_select by remember { mutableStateOf("") }
    // Función para agregar nuevas promos sin duplicados
    fun agregarNuevasPromos(nuevas: List<dataclass_promociones_cerca_de_ti>) {
        val existentesIds = feedVisible.map { it.informacion_publcacion.id_promocion }.toSet()
        val filtradas = nuevas.filter { it.informacion_publcacion.id_promocion !in existentesIds }
        if (filtradas.isNotEmpty()) {
            feedVisible = feedVisible + filtradas
        }
    }

    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }
    // 1️⃣ Cargar bloques iniciales
    LaunchedEffect(Unit) {
        viewModel.cargarSiguienteBloque(localidad_general)
    }

    LaunchedEffect(listaPromos, promoSeleccionada) {
        if (listaPromos.isNotEmpty()) {
            val resto =
                listaPromos.filter { it.informacion_publcacion.id_promocion != promoSeleccionada.informacion_publcacion.id_promocion }
                    .shuffled() // 🔹 orden aleatorio cada vez
            feedVisible = listOf(promoSeleccionada) + resto
        }
    }
    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localidad_general,
                id_tienda_select
            )
        }
    }

    var viewJob by remember { mutableStateOf<Job?>(null) }
    val vistasRegistradas = remember { mutableSetOf<String>() }


    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { feedVisible.size }
    )

    RegistrarVistaConDelay(pagerState,feedVisible,localidad_general,viewModel)

    // 4️⃣ Scroll infinito
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage >= feedVisible.size - threshold) {
            // Filtrar nuevas promos comparando id_promocion
            val feedIds = feedVisible.map { it.informacion_publcacion.id_promocion }.toSet()
            val nuevas = listaPromos.filter { it.informacion_publcacion.id_promocion !in feedIds }
            agregarNuevasPromos(nuevas)

            if (pagerState.currentPage >= listaPromos.size - threshold) {
                viewModel.cargarSiguienteBloque(localidad_general)
            }
        }
    }





    LaunchedEffect(pagerState.currentPage) {

        viewJob?.cancel()

        if (feedVisible.isEmpty()) return@LaunchedEffect

        val promoActual = feedVisible[pagerState.currentPage]
        val idPublicacion = promoActual.informacion_publcacion.id_promocion

        viewJob = scope.launch {
            delay(3000) // ⏱ 3 segundos reales

            val sigueVisible =
                pagerState.currentPage == feedVisible.indexOf(promoActual)

            val noRegistrada =
                !vistasRegistradas.contains(idPublicacion)

            if (sigueVisible && noRegistrada) {
                vistasRegistradas.add(idPublicacion)

                viewModel.agregar_estadisticas_publicacion(
                    tipo = "vistas",
                    id_promo = idPublicacion,
                    localidad = localidad_general
                )
            }
        }
    }



    Box(modifier = Modifier.fillMaxSize()){
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    val promo = feedVisible[index]
                    val datos = compartir_contacto_pulicaciones(
                        promo.informacion_publcacion.id_promocion,
                        iod_tienda = promo.informacion_publcacion.id_tienda,
                        localidad_tineda = "barranca",
                        categoria = promo.informacion_publcacion.categoria,
                        numero_contacto = promo.informacion_publcacion.numero,
                        dias_restantes = promo.dias_restantes,
                        logo_img = promo.img.logo_img,
                        nombre_tienda = promo.informacion_publcacion.nombre_tienda
                    )
                    ZoomableGalleryFullScreen_promociones(
                        i = datos,
                        titulo = promo.informacion_publcacion.titulo,
                        txt = promo.informacion_publcacion.descripcion,
                        imagenes = promo.img.lista_img,
                        startIndex = indeximg_seleccionado,
                        onDismiss = {
                            onDismiss()
                            viewModel.resetPromos()
                        }, clikc_compartir = { id_promo, categoria, localidad,id_tienda ->
                            compartir_hosting_promo(id_tienda,context, localidad, id_promo,categoria)
                            viewModel.agregar_estadisticas_publicacion(
                                "compartidos",
                                id_promo,
                                localidad
                            )
                        }, click_contacto_directo = { id, numero, localidad ,id_tienda->
                            abrir_whattsapp(
                                "promocion",
                                "",
                                "",
                                context,
                                numero,
                                "Hola, quiero esta oferta que vi Geinz: " +
                                        "https://geinzworkapp.web.app/share?" +
                                        "t=prof&cl=pro" +
                                        "&id=${URLEncoder.encode(id, "UTF-8")}" +
                                        "&l=$localidad"
                            )
                            viewModel.agregar_estadisticas_publicacion(
                                "whatsapp",
                                id,
                                localidad
                            )
                        },{id->
                            show_bottom_sheeet = true
                            id_tienda_select = id
                        },
                    )
                }
            }
        }
        if (show_bottom_sheeet) {
            bottom_sheet_tiendas_filtradas(
                true,
                viewModelFiltros,
                dataclass_tienda_seleccionada, show_bottom_sheeet
            ) {
                show_bottom_sheeet = false
            }
        }
    }

}


@Composable
fun RegistrarVistaConDelay(
    pagerState: PagerState,
    feed: List<dataclass_promociones_cerca_de_ti>,
    localidad: String,
    viewModel: viewmodel_promos_cercanas
) {
    val scope = rememberCoroutineScope()
    var viewJob by remember { mutableStateOf<Job?>(null) }
    val vistasRegistradas = remember { mutableSetOf<String>() }

    LaunchedEffect(pagerState.currentPage) {
        viewJob?.cancel()

        if (feed.isEmpty()) return@LaunchedEffect

        val promo = feed[pagerState.currentPage]
        val id = promo.informacion_publcacion.id_promocion

        viewJob = scope.launch {
            delay(3000)

            val sigueVisible =
                pagerState.currentPage == feed.indexOf(promo)

            if (sigueVisible && vistasRegistradas.add(id)) {
                viewModel.agregar_estadisticas_publicacion(
                    "vistas", id, localidad
                )
            }
        }
    }
}



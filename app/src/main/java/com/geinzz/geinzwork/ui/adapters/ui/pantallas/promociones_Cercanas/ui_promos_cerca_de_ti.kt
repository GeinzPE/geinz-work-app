package com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.data.model.localizate_geinz.img_con_texto
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ZoomableGalleryFullScreenVerticalPager
import com.geinzz.geinzwork.ui.adapters.promoEstaExpirada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_filtrados_promos_y_ofertas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_datos_promociones
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
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
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }

    val respuesta_gemini_NLP by viewModel.respuesta_gemini.collectAsState()

    var mostar_respuesta_gemini by remember(respuesta_gemini_NLP) {
        mutableStateOf(respuesta_gemini_NLP != null)
    }

    var tienda_seleccionada_ccon_mas_de_una_promo by remember { mutableStateOf(false) }


    var mostrar_zoom_img by remember { mutableStateOf(false) }
    var lista_img by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var mostrar_bottom_shet_registrate by remember { mutableStateOf(false) }
    val comodidad_selet by viewModel.comodidadesSeleccionadas.collectAsState()
    val metodo_pago by viewModel.metodosPagoSeleccionados.collectAsState()
    val rango_precio by viewModel.rangoPrecioSeleccionado.collectAsState()
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
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val categorias by viewModel._categoriasDisponibles.collectAsState()
    val viewmodel_repo_datos_promo: viewmodel_datos_promociones = viewModel()
    val datos_promo_parametros by viewmodel_repo_datos_promo.datos_promocion_parametro.collectAsState()
    var tiendaSeleccionada by remember { mutableStateOf<String?>(null) }
    var nombre_tienda by remember { mutableStateOf("") }
    var img_tienda by remember { mutableStateOf("") }
    var dias_restantes by remember { mutableStateOf("") }
    var promoSeleccionada by remember { mutableStateOf<obj_completo?>(null) }


    var promoSeleccionada_unica by remember {
        mutableStateOf<dataclass_promociones_cerca_de_ti?>(
            null
        )
    }
    var valor_a_buscar by remember { mutableStateOf("") }

    var indexImagenSeleccionada by remember { mutableStateOf(0) }

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
    var mostar_bottom_sheet_datos by remember { mutableStateOf(false) }
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

    LaunchedEffect(respuesta_gemini_NLP) {
        respuesta_gemini_NLP?.let {
            val respuesta = RespuestaGemini(
                principal = it.principal,
                atributos = it.atributos,
                precio = it.precio,
                metodo_pago = it.metodo_pago,
                comodidades = it.comodidades
            )
            terminoNLP = respuesta.principal
            atributosNLP = respuesta.atributos
            viewModel.setComodidadesDesdeLista(respuesta.comodidades)
            viewModel.setPagosDesdeLista(respuesta.metodo_pago)
//            viewModel.fiiltrar_por_termino_y_atributos(respuesta.principal,respuesta.atributos)
            val rangoPrecio = respuesta.precio?.let { precio ->
                when (precio) {
                    in 0.0..10.0 -> "0 - 10"
                    in 10.01..20.0 -> "10 - 20"
                    in 20.01..30.0 -> "20 - 30"
                    in 30.01..50.0 -> "30 - 50"
                    in 50.01..80.0 -> "50 - 80"
                    in 80.01..120.0 -> "80 - 120"
                    in 120.01..200.0 -> "120 - 200"
                    in 200.01..350.0 -> "200 - 350"
                    in 350.01..500.0 -> "350 - 500"
                    in 500.01..1000.0 -> "500 - 1000"
                    in 1000.01..2500.0 -> "1000 - 2500"
                    in 2500.01..5000.0 -> "2500 - 5000"
                    else -> "Sin rango"
                }
            } ?: "Sin precio" // si precio es null
            viewModel.setearRangoPrecioDesdeNLP(rangoPrecio)

            Log.d("datos_entrantes", "$respuesta")
        }
    }




    LaunchedEffect(datos_promo_parametros) {

        Log.d("PROMO_FLOW", "▶ LaunchedEffect datos_promo_parametros")

        if (activar_promo_params.isEmpty()) {
            Log.w(
                "PROMO_FLOW",
                "⛔ Se ignora datos_promo_parametros porque activar_promo_params está vacío"
            )
            return@LaunchedEffect
        }

        cargaFinalizada = true

        val idPromo =
            datos_promo_parametros.informacion_publcacion.id_promocion
        val estado_publicaicones = datos_promo_parametros.estado_publicacion

        if (estado_publicaicones.equals("pausado", ignoreCase = true)) {
            promoExpirada = true
            texto_snackbar =
                "Esta publicación no está disponible en este momento. Inténtalo más tarde."
            return@LaunchedEffect
        }

        Log.d("PROMO_FLOW", "🆔 idPromo recibido = '$idPromo'")

        // ❌ NO EXISTE
        if (idPromo.isEmpty()) {
            Log.e("PROMO_FLOW", "❌ idPromo vacío → promo NO existe")
            promoExpirada = true
            texto_snackbar = "Este contenido ya no está disponible"

            return@LaunchedEffect
        }

        // ⏱️ VALIDAR EXPIRACIÓN
        val expirada = promoEstaExpirada(datos_promo_parametros.fecha_fin)

        Log.d(
            "PROMO_FLOW",
            "⏱ Fecha fin = ${datos_promo_parametros.fecha_fin} | Expirada = $expirada"
        )

        if (expirada) {
            Log.e("PROMO_FLOW", "⛔ Promo EXPIRADA")
            promoExpirada = true
            return@LaunchedEffect
        }

        // ✅ EXISTE y NO está expirada → UI
        Log.d("PROMO_FLOW", "✅ Promo válida → mostrar UI")

        promoExpirada = false
        mostrar_zoom_img = true
        promoSeleccionada_unica = datos_promo_parametros

        // 📊 ESTADÍSTICAS (SOLO UNA VEZ)
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
            Log.d("PROMO_FLOW", "ℹ Estadísticas ya agregadas")
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
        viewModel.obtener_promociones("barranca", "Todos")
    }
    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localidad,
                id_tienda_select
            )
        }
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }
    var primeraVez by remember { mutableStateOf(true) }

    LaunchedEffect(
        subCategoriaSeleccionada,
        comodidad_selet,
        metodo_pago,
        rango_precio
    ) {
        if (primeraVez) {
            primeraVez = false
            return@LaunchedEffect
        }
        viewModel.filtrarPromociones(subCategoriaSeleccionada, terminoNLP, atributosNLP)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        when (estado) {

            // ---------- LOADING ----------
            viewmodel_promos_cercanas.estado_carga_promociones.loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
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

                val promos =
                    (estado as viewmodel_promos_cercanas.estado_carga_promociones.succes).items
                val tiendasConMasDeUnaPromo: List<tiendas_con_mas_de_una_promo> = promos
                    .flatMap { it.lista_tiendas_con_mas_promo }
                    .distinctBy { it.id } // eliminamos duplicados por id

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(vertical = 5.dp)
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


                        val subcategorias = listOf("Todos") + promos
                            .flatMap { categorias }
                            .distinct()

                        item {
                            Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                                OutlinedTextField(
                                    value = valor_a_buscar,
                                    onValueChange = {
                                        valor_a_buscar = it
                                    },
                                    placeholder = {
                                        texto_generico_one_line(
                                            "¿Qué buscas?",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(50),
//                                    leadingIcon = {
//                                        IconButton(
//                                            onClick = {
//                                            }
//                                        ) {
//                                            Icon(
//                                                imageVector = Icons.Default.Clear,
//                                                contentDescription = "Hablar", tint = Color.Gray
//                                            )
//                                        }
//                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                viewModel.procesar_NLP(
                                                    valor_a_buscar,
                                                    subCategoriaSeleccionada
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Hablar", tint = Color.Gray
                                            )
                                        }
                                    }
                                )
                                respuesta_gemini_NLP?.let { respuesta ->
                                    texto_generico_multilinea(respuesta.toString())
                                }
                            }
                        }

                        item {

                            val itemFiltros = tiendas_con_mas_de_una_promo(
                                id = "FILTROS_GENERALES",
                                nombre_tienda = "Filtros",
                                logo_img = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0"
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // 🔥 Nuevo item de filtros generales
                                item {
                                    estilo_ig_header(
                                        i = itemFiltros,
                                        seleccionada = tiendaSeleccionada == "FILTROS_GENERALES",
                                        img_clikeada = { id ->
                                            mostar_bottom_sheet_datos = true
                                        }
                                    )
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

                                if (listaSeleccionada.isEmpty() && lista_comodidades_Select.isEmpty()) {

                                    // 🔹 Si no hay filtros → mostrar tiendas
                                    items(tiendasConMasDeUnaPromo) { tienda ->
                                        estilo_ig_header(
                                            i = tienda,
                                            seleccionada = tienda.id == tiendaSeleccionada,
                                            img_clikeada = { id ->
                                                tiendaSeleccionada = id
                                                subCategoriaSeleccionada = "Todos"
                                                viewModel.filtrar_promociones_por_id(id)
                                            }
                                        )
                                    }
                                }
                            }

                        }

//                        item {
//                            LazyRow(
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                contentPadding = PaddingValues(horizontal = 10.dp)
//                            ) {
//                                items(subcategorias) { subcategoria ->
//                                    val seleccionado = subCategoriaSeleccionada == subcategoria
//                                    chisp_filtrado_busqueda(
//                                        carta_selecionada = seleccionado,
//                                        filtrado = subcategoria.capitalizeFirst(),
//                                        btn_visible = false,
//                                        clik_card = {
//                                            subCategoriaSeleccionada = subcategoria
//                                            tiendaSeleccionada = null
//                                            if (subcategoria != "Todos") {
//
//                                            }
//                                        },
//                                        onClick_delete = {}
//                                    )
//                                }
//                            }
//                        }

//                        item {
//                            LazyRow(
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                contentPadding = PaddingValues(horizontal = 10.dp)
//                            ) {
//                                items(lista_filtrados_pagos) { subcategoria ->
//
//                                    val seleccionado = metodo_pago.contains(subcategoria)
//
//                                    chisp_filtrado_busqueda(
//                                        carta_selecionada = seleccionado,
//                                        filtrado = subcategoria.capitalizeFirst(),
//                                        btn_visible = false,
//                                        clik_card = {
//                                            viewModel.toggleMetodoPago(subcategoria)
//                                        },
//                                        onClick_delete = {}
//                                    )
//                                }
//                            }
//                        }
//
//                        item {
//                            LazyRow(
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                contentPadding = PaddingValues(horizontal = 10.dp)
//                            ) {
//                                items(rango_precios) { subcategoria ->
//                                    val seleccionado = rango_precio == subcategoria
//                                    chisp_filtrado_busqueda(
//                                        carta_selecionada = seleccionado,
//                                        filtrado = subcategoria.capitalizeFirst(),
//                                        btn_visible = false,
//                                        clik_card = {
//                                            viewModel.setearRangoPrecioDesdeNLP(subcategoria)
//                                        },
//                                        onClick_delete = {}
//                                    )
//                                }
//                            }
//                        }
//
//
//
//                        item {
//                            LazyRow(
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                contentPadding = PaddingValues(horizontal = 10.dp)
//                            ) {
//                                items(lista_comodidades) { subcategoria ->
//
//                                    val seleccionado = comodidad_selet.contains(subcategoria)
//
//                                    chisp_filtrado_busqueda(
//                                        carta_selecionada = seleccionado,
//                                        filtrado = subcategoria.capitalizeFirst(),
//                                        btn_visible = false,
//                                        clik_card = {
//                                            viewModel.togleRango_select(subcategoria)
//                                        },
//                                        onClick_delete = {}
//                                    )
//                                }
//                            }
//                        }


                        items(
                            items = promos,
                            key = { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion + it.hashCode() }
                        ) { item ->
                            val idPromo = item.dataclass_promociones_cerca_de_ti
                                .informacion_publcacion.id_promocion

                            val porcentajeMatch = porcentajes[idPromo] ?: 0
                            val index = promos.indexOfFirst {
                                it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion ==
                                        item.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
                            }


                            carta_promocion_geinz(
                                porcentajeMatch,
                                i = item.dataclass_promociones_cerca_de_ti,
                                img_clikeble = { id_promo, listaimg, select ->
                                    if (uid_respald_user.isNotEmpty()) {
                                        Log.d(
                                            "mostramosooom",
                                            "$id_promo ${listaimg.size} $select"
                                        )
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
                                                    "https://geinzworkapp.web.app/share?" +
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
                                }
                            )

                        }

                    }
                    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))

                }

                if (mostar_bottom_sheet_datos) {
                    bottom_sheet_filtrados_promos_y_ofertas(
                        comodidad_selet,
                        metodo_pago,
                        rango_precio,
                        viewModel,
                        {
                            mostar_bottom_sheet_datos = false
                        })
                }

//                if (mostrar_zoom_img && promoSeleccionada != null) {
                if (mostrar_zoom_img) {
                    ZoomableGalleryFullScreenVerticalPager(
                        tiendaSeleccionada1 = tiendaSeleccionada,
                        categoria_select_filtro = subCategoriaSeleccionada,
                        id_user = uid_respald_user,
                        viewModel = viewModel,
                        localidad_general = localidad,
                        promoSeleccionada = promoSeleccionada_unica!!,
                        indeximg_seleccionado = index_galeria_img,
                        onDismiss = { mostrar_zoom_img = false; promoSeleccionada = null },
                    )
                    return
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
                        verificar_intener,
                        viewModelFiltros,
                        dataclass_tienda_seleccionada, show_bottom_sheeet
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
    img_clikeble: (id: String, lista: List<String>, Int) -> Unit,
    share_promo: (String, String, String) -> Unit,
    whatsap_promo: (String, id_tienda: String, categoira: String) -> Unit,
    mostrar_perfil: (String, id_promo: String) -> Unit
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
            GaleriaHorizontalInstagram(
                imagenes = i.img.lista_img,
                modifier = Modifier.fillMaxSize(), img_clikeble_valor = { select ->
                    img_clikeble(i.informacion_publcacion.id_promocion, i.img.lista_img, select)
                }, long_listatener = {
                    Log.d("LONG_PRESS", "Long press en la galería")
                })
            texto_generico_one_line("provavilidad de $porcentajeMatch", color = Color.Black)
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

                Text(
                    text = "${diasRestantes}",
                    fontSize = 12.sp,
                    color = backgroundColor
                )
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

@Composable
fun GaleriaHorizontalInstagram(
    imagenes: List<String>,
    modifier: Modifier = Modifier,
    img_clikeble_valor: (Int) -> Unit,
    long_listatener: () -> Int
) {
    val pagerState = rememberPagerState { imagenes.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            AsyncImage(
                model = imagenes[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        indication = null, // opcional (sin ripple)
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            img_clikeble_valor(page)
                        },
                        onLongClick = {
                            long_listatener()
                        }),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.cargando_img_categorias),
                error = painterResource(R.drawable.cargando_img_categorias)
            )
        }

        // Indicador 1/5
        if (imagenes.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${imagenes.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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
        val repo_erese_socio = repo_eres_socio()


        val link =
            "https://geinzworkapp.web.app/share?" +
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

            // 🔹 Ring IG morado
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
                    .border(
                        width = 3.dp,
                        color = Color(0xFF7B2CBF),
                        shape = CircleShape
                    )
            )

            // 🔹 Imagen
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(i.logo_img)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
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

            // 🔹 Imagen circular
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



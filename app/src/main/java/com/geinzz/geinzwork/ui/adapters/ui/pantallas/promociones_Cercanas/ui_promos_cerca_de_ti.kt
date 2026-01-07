package com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ZoomableGalleryFullScreenVerticalPager
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen_promociones
import com.geinzz.geinzwork.ui.adapters.ui.btn_compartir
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textosTituloGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ui_promos_cerca_de_ti(localidad: String, verificar_intener: Boolean) {
    val context = LocalContext.current
    val viewModel: viewmodel_promos_cercanas = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()

    val estado by viewModel.estadoPromos.collectAsState()
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var mostrar_zoom_img by remember { mutableStateOf(false) }
    var lista_img by remember {
        mutableStateOf<List<String>>(emptyList())
    }


    var index_galeria_img by remember { mutableStateOf(0) }
    var titulo_poromo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var id_tienda_select by remember { mutableStateOf("") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val categorias by viewModel._categoriasDisponibles.collectAsState()
    var tiendaSeleccionada by remember { mutableStateOf<String?>(null) }
    var nombre_tienda by remember { mutableStateOf("") }
    var img_tienda by remember { mutableStateOf("") }
    var dias_restantes by remember { mutableStateOf("") }

    var promoSeleccionada by remember { mutableStateOf<obj_completo?>(null) }

    var promoSeleccionada_unica by remember { mutableStateOf<dataclass_promociones_cerca_de_ti?>(null) }

    var indexImagenSeleccionada by remember { mutableStateOf(0) }


    LaunchedEffect(localidad) {
        viewModel.obtener_promociones("barranca")
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

    LaunchedEffect(subCategoriaSeleccionada) {
        if (primeraVez) {
            primeraVez = false
            return@LaunchedEffect
        }
        viewModel.filtrar_promociones(subCategoriaSeleccionada)
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
                    .flatMap { it.lista_tiendas_con_mas_promo } // sacamos todas las listas de cada promo
                    .distinctBy { it.id } // eliminamos duplicados por id

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
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            items(subcategorias) { subcategoria ->
                                val seleccionado = subCategoriaSeleccionada == subcategoria
                                chisp_filtrado_busqueda(
                                    carta_selecionada = seleccionado,
                                    filtrado = subcategoria.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = {
                                        subCategoriaSeleccionada = subcategoria
                                        if (subcategoria != "Todos") {
                                            tiendaSeleccionada = null
                                        }
                                    },
                                    onClick_delete = {}
                                )
                            }
                        }
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
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

                    items(
                        items = promos,
                        key = { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                    ) { item ->

                        val index = promos.indexOfFirst { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion ==
                                item.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }


                        carta_promocion_geinz(
                            i = item.dataclass_promociones_cerca_de_ti,
                            img_clikeble = { id_promo, listaimg, select ->
                                Log.d("mostramosooom", "$id_promo ${listaimg.size} $select")
                                promoSeleccionada = item
                                // ✅ Usar el index calculado
                                promoSeleccionada_unica=item.dataclass_promociones_cerca_de_ti
                                indexImagenSeleccionada = select
                                mostrar_zoom_img = true
                                lista_img = listaimg
                                index_galeria_img = select
                                titulo_poromo = item.dataclass_promociones_cerca_de_ti.informacion_publcacion.titulo
                                descripcion = item.dataclass_promociones_cerca_de_ti.informacion_publcacion.descripcion
                                nombre_tienda = item.dataclass_promociones_cerca_de_ti.informacion_publcacion.nombre_tienda
                                img_tienda = item.dataclass_promociones_cerca_de_ti.img.logo_img
                                dias_restantes = item.dataclass_promociones_cerca_de_ti.dias_restantes

                                viewModel.agregar_estadisticas_publicacion(
                                    "vistas",
                                    id_promo,
                                    localidad
                                )
                            },
                            share_promo = { id_tienda,id,categoria ->
                                compartir_hosting_promo(id_tienda,context, localidad, id,categoria)
                                viewModel.agregar_estadisticas_publicacion(
                                    "compartidos",
                                    id,
                                    localidad
                                )
                            },
                            whatsap_promo = { id ->
                                abrir_whattsapp(
                                    "promocion",
                                    "",
                                    "",
                                    context,
                                    item.dataclass_promociones_cerca_de_ti
                                        .informacion_publcacion.numero,
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

                            },
                            mostrar_perfil = { id ->
                                show_bottom_sheeet = true
                                id_tienda_select = id
                            }
                        )

                    }

                }
                if (mostrar_zoom_img && promoSeleccionada != null) {
                    ZoomableGalleryFullScreenVerticalPager(
                        viewModel = viewModel,
                        localidad_general = localidad,
                        promoSeleccionada = promoSeleccionada_unica!!,
                        index_galeria_img,
                        onDismiss = { mostrar_zoom_img = false; promoSeleccionada = null }
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
    }
}


@Composable
fun carta_promocion_geinz(
    i: dataclass_promociones_cerca_de_ti,
    img_clikeble: (id: String, lista: List<String>, Int) -> Unit,
    share_promo: (String,String,String) -> Unit,
    whatsap_promo: (String) -> Unit, mostrar_perfil: (String) -> Unit
) {
    val (valorRestante, tipo) = parseDiasHorasRestantes(i.dias_restantes)

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
                        mostrar_perfil(i.informacion_publcacion.id_tienda)
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
                    text = "${i.dias_restantes}",
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
                                whatsap_promo(i.informacion_publcacion.id_promocion)
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
    id_tienda: String,
    context: Context,
    localidad_tienda: String,
    idpromo: String,
    categoria: String,
) {
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
                    "t=prn" +
                    "&id=$id_tienda" +
                    "&l=$localidad_pasada" +
                    "&c=${
                        URLEncoder.encode(categoria, "UTF-8")
                    }" + "&pi=$idpromo"


        val texto = "Mira esta promo en Geinz ❤\uFE0F\u200D\uD83D\uDD25 \n$link"

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
//        repo_erese_socio.agregar_contador(
//            "compartidos",
//            id_tienda,
//            localidad_tienda
//        )
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





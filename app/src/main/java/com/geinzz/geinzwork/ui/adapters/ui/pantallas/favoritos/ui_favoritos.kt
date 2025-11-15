package com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos


import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ImagenConInclinacion
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda.LazyRowConSombras
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.TiempoRestanteCierre
import com.geinzz.geinzwork.ui.adapters.ui.principal.AutoResizeOneLineText
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textos_titulos_geinz_wokr
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.categorias_defaul
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.viewModel_favoritos
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun iu_favoritos(
    viewModelFiltros: viewModel_filtado_tiendas,
    viewmodelFavoritos: viewModel_favoritos,
    datos_principales_user: datos_principales_user,
    empty_select_chip: (String, String, String) -> Unit
) {

    val contex = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val lista_fb_size by viewmodelFavoritos.lista_fv.collectAsState()
    var imagenActiva by remember { mutableStateOf<Int?>(null) }
    val ultimaLocalidad by data_store_localidad.obtener_localidad(contex)
        .collectAsState(initial = null)
    val tick by viewModelFiltros.tick.collectAsState()

    val listaImg = listOf(
        R.drawable.f1,
        R.drawable.f2,
        R.drawable.f4
    )

    val uid_respald_user by data_store_localidad.get_uid_user(contex).collectAsState(initial = "")
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()

    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""

    var lista_subcategorias by remember { mutableStateOf(listOf<String>()) }
    var lista_datos by remember { mutableStateOf(listOf<favoritos_guardados>()) }
    var cat_selecionada by remember { mutableStateOf("Todos") }
    var mostarsin_continuar by remember { mutableStateOf(false) }
    var mostar_succes by remember { mutableStateOf(false) }
    var bottomhseet_tienda by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var id_tienda_select by remember { mutableStateOf("") }
    var localida_tienda_select by remember { mutableStateOf("") }
    LaunchedEffect(key1 = id_user) {
        viewmodelFavoritos.obtener_favoritos(id_user)
    }
    LaunchedEffect(bottomhseet_tienda) {

        if (bottomhseet_tienda) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                localida_tienda_select,
                id_tienda_select,
            )
        }
    }

    LaunchedEffect(datosTienda) {
        Log.d("id_tienda_select123", "$datosTienda")
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }
    when (lista_fb_size) {
        viewModel_favoritos.state_fv.empty -> {
            mostar_succes = false
            mostarsin_continuar = true

        }

        is viewModel_favoritos.state_fv.error -> {
            mostar_succes = false
            mostarsin_continuar = true
        }

        viewModel_favoritos.state_fv.loading -> {
            mostar_succes = false
            mostarsin_continuar = false
            Box(modifier = Modifier.fillMaxSize()) {
                texto_generico_one_line("cargando tus guardados")
            }
        }

        is viewModel_favoritos.state_fv.succes -> {
            val listaFavoritos = (lista_fb_size as viewModel_favoritos.state_fv.succes).item
            val listaCategorias =
                (lista_fb_size as viewModel_favoritos.state_fv.succes).lista_categoria
            if (listaFavoritos.isNotEmpty() || listaCategorias.isNotEmpty()) {
                lista_subcategorias = listaCategorias
                lista_datos = listaFavoritos
                mostar_succes = true
                mostarsin_continuar = false
            }
        }
    }

    Crossfade(
        targetState = if (mostarsin_continuar) "empty" else if (mostar_succes) "success" else "none",
        animationSpec = tween(500)
    ) { state ->
        when(state) {

        "empty" -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Crossfade(targetState = imagenActiva, animationSpec = tween(500)) { index ->
                        if (index != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = listaImg[index]),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }


                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center

                ) {
//            TextoConIconoFinal("Aun no cuentas con favoritos")
                    Text(
                        "Aun no cuentas con favoritos",
                        fontFamily = baners_geinz_work,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                    spacer_vertical(5.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.corazon_canva_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                        )
                        spacer_horizonta(5.dp)
                        Image(
                            painter = painterResource(R.drawable.estrella_3d_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                    spacer_vertical(15.dp)
                    Text(
                        modifier = Modifier.padding(horizontal = 30.dp),
                        text = "Guarda tus negocios y lugares favoritos en GEINZ y encuéntralos al instante. Ahorra tiempo, evita búsquedas y ten todo a un toque.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )

                    spacer_vertical(20.dp)
                    ChipsCategorias(
                        categorias = categorias_defaul,
                        imagenActiva = imagenActiva, select = { cat ->
                            empty_select_chip(
                                datos_principales_user.nombre,
                                cat,
                                ultimaLocalidad ?: "barranca"
                            )
                        }

                    )
                    spacer_vertical(10.dp)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // --- Foto 1 (Izquierda) ---
                        ImagenConInclinacion(
                            drawableResId = R.drawable.f1,
                            anguloRotacion = -8f,
                            desplazamientoX = -70.dp,
                            desplazamientoY = 20.dp,
                            factorTamaño = 0.33f,
                            { imagenActiva = if (imagenActiva == 0) null else 0 }, imagenActiva == 0
                        )

                        ImagenConInclinacion(
                            drawableResId = R.drawable.f2,
                            anguloRotacion = 3f,
                            desplazamientoX = 0.dp,
                            desplazamientoY = 0.dp,
                            factorTamaño = 0.33f,
                            { imagenActiva = if (imagenActiva == 1) null else 1 },
                            imagenActiva == 1
                        )

                        // --- Foto 3 (Derecha) ---
                        ImagenConInclinacion(
                            drawableResId = R.drawable.f4,
                            anguloRotacion = 7f,
                            desplazamientoX = 70.dp,
                            desplazamientoY = 40.dp,
                            factorTamaño = 0.33f,
                            { imagenActiva = if (imagenActiva == 2) null else 2 },
                            imagenActiva == 2
                        )
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
                )
            }
        }
        "success" -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        fraces_cambio(datos_principales_user.nombre)
                        val lista_mas_todos = listOf("Todos") + lista_subcategorias
                        texto_generico_multilinea(
                            "Aquí tienes tus favoritos, esos lugares y productos que elegiste guardar. Cada uno tiene algo especial para ti y cuando quieras volver estarán aquí esperándote, listos para acompañarte otra vez.",
                            MaterialTheme.typography.bodyMedium
                        )
                        LazyRowConSombras() {
                            items(lista_mas_todos) { cat ->
                                val catSeleccionada = cat_selecionada == cat
                                chisp_filtrado_busqueda(
                                    carta_selecionada = catSeleccionada,
                                    filtrado = cat,
                                    btn_visible = false,
                                    clik_card = { cat_selecionada = cat },
                                    onClick_delete = {
                                    }
                                )
                            }
                        }
                    }
                }
                itemsIndexed(lista_datos) { index, item ->
                    Log.d("safdSADFGJSAIUGHAsuorg", item.horario.toString())
                    val heightOptions = listOf(300.dp, 350.dp)
                    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]
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
                                        .data(item.img_tienda)
                                        .placeholder(R.drawable.cargando_img_categorias)
                                        .error(R.drawable.cargando_img_categorias)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            bottomhseet_tienda = true
                                            id_tienda_select = item.id_tienda_lugar
                                            localida_tienda_select = item.localida_tienda
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
                                        text = item.nombre_lugar_tienda,
                                        fontFamily = textos_titulos_geinz_wokr,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 17.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    val coordenadasValidas = item.lat != 0.0 && item.lng != 0.0
                                    if (coordenadasValidas) {
                                        FloatingActionButton(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White,
                                            onClick = {
//                                            when {
//                                                i.categoria == "seguridad" || i.categoria == "salud" -> {
//                                                    // Categoría seguridad o salud
//                                                    if (coordenadasValidas) {
//                                                        abrir_gogle_map(i.latitud, i.longitud)
//                                                    }
//                                                    // Si no hay coordenadas, no hace nada
//                                                }
//
//                                                firebaseAuth.currentUser != null -> {
//                                                    // Categoría diferente y usuario registrado
//                                                    if (coordenadasValidas) {
//                                                        abrir_gogle_map(i.latitud, i.longitud)
//                                                    }
//                                                    // Si no hay coordenadas, no hace nada
//                                                }
//
//                                                else -> {
//                                                    // Categoría diferente y usuario NO registrado
//                                                    texto_bottom_sheet_dialog_login =
//                                                        "Crea tu ruta registrándote ahora"
//                                                    mostra_dialog_login = true
//                                                }
//                                            }
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
                                        item.localida_tienda.capitalizeFirst(),
                                        MaterialTheme.typography.bodyMedium
                                    )
                                }
                                spacer_vertical(5.dp)
                                TiempoRestanteCierre(
                                    horario_total = item.horario,
                                    hCierre = item.horario.h_cierre,
                                    cerrado = item.horario.cerrado,
                                    motivo = item.horario.motivo,
                                    pagado = true,
                                    max_line = 1, tick = tick
                                ) { color ->
                                    viewModelFiltros.setear_color(color)
                                }
                                spacer_vertical(5.dp)
                                val iconCategoria =
                                    constantes_lista_localidades.getCategoriaIcon(item.categoria)
                                texto_generico_one_line(
                                    "$iconCategoria ${item.categoria}",
                                    MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(5.dp)
                                tags_subcateogiras(
                                    item.tag_sub,
                                    brush_start = Brush.horizontalGradient(colors = shadow_top_filtrado_v1),
                                    brush_end = Brush.horizontalGradient(colors = shadow_botonm_filtrado_v1)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
        }


    if (bottomhseet_tienda) {
        bottom_sheet_tiendas_filtradas(
            viewModelFiltros,
            dataclass_tienda_seleccionada, bottomhseet_tienda
        ) {
            bottomhseet_tienda = false
        }
    }

}

//@Composable
//fun TextoConIconoFinal(texto: String) {
//
//    val iconId = "icono_final"
//
//    val annotated = buildAnnotatedString {
//        append(texto + " ")
//        appendInlineContent(iconId)
//    }
//
//    val inlineContent = mapOf(
//        iconId to InlineTextContent(
//            Placeholder(
//                width = 30.sp,   // 🔥 Icono más grande y visible
//                height = 30.sp,
//                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
//            )
//        ) {
//            Row() {
//                Image(
//                    painter = painterResource(R.drawable.corazon_canva_icon),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxSize()   // ⬅️ Ajusta a EXACTO el tamaño del placeholder
//                )
//                spacer_horizonta(5.dp)
//                Image(
//                    painter = painterResource(R.drawable.estrella_3d_icon),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxSize()   // ⬅️ Ajusta a EXACTO el tamaño del placeholder
//                )
//            }
//
//        }
//    )
//
//    Text(
//        text = annotated,
//        inlineContent = inlineContent,
//        fontFamily = baners_geinz_work,
//        fontSize = 25.sp,
//        textAlign = TextAlign.Center,
//        modifier = Modifier.padding(horizontal = 30.dp)
//    )
//}


@Composable
fun fraces_cambio(nombre_user: String) {
    val fraces = constantes_lista_localidades.lista_fraces_favoritos
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }
    Crossfade(fraces[index], label = "fraces") { txt ->
        AutoResizeOneLineText(
            text = txt,
            style = MaterialTheme.typography.busquedaGeinzWork
        )
    }
}

fun <T> dividirEnFilas(lista: List<T>, filas: Int): List<List<T>> {
    val size = lista.size
    val elementosPorFila = (size + filas - 1) / filas
    return lista.chunked(elementosPorFila)
}

@Composable
fun ChipsCategorias(categorias: List<String>, imagenActiva: Int?, select: (String) -> Unit) {

    val filas = dividirEnFilas(categorias, 3)
    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // 🔹 centrado horizontal
            modifier = Modifier.fillMaxWidth()

        ) {
            filas.forEach { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    fila.forEach { categoria ->
                        ChipCategoria(titulo = categoria, { cat ->
                            select(categoria)
                        })
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = imagenActiva == null,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            // Sombra izquierda
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .align(Alignment.CenterStart)
                    .zIndex(1f)
                    .background(Brush.horizontalGradient(colors = shadow_left))
            )
        }
        AnimatedVisibility(
            visible = imagenActiva == null,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {

            // Sombra derecha
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)

                    .zIndex(1f)
                    .background(Brush.horizontalGradient(colors = shadow_right))
            )
        }


    }

}

@Composable
fun ChipCategoria(titulo: String, select: (String) -> Unit) {
    val iconCategoria = constantes_lista_localidades.getCategoriaIcon(titulo)
    val cata_simplificada = simplificarCategoria(titulo)
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                select(titulo)
            }
    ) {
        Text(
            text = "$iconCategoria $cata_simplificada",
            fontSize = 14.sp,
            color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )


    }
}


package com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos


import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
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
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
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

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val lista_fb_size by viewmodelFavoritos.lista_fv.collectAsState()
    var imagenActiva by remember { mutableStateOf<Int?>(null) }
    val ultimaLocalidad by data_store_localidad.obtener_localidad(context)
        .collectAsState(initial = null)
    val tick by viewModelFiltros.tick.collectAsState()

    val listaImg = listOf(
        R.drawable.f1,
        R.drawable.f2,
        R.drawable.f4
    )

    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()

    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""

    var lista_subcategorias by remember { mutableStateOf(listOf<String>()) }
    var lista_localidad_filtrado by remember { mutableStateOf(listOf<String>()) }
    var lista_datos by remember { mutableStateOf(listOf<favoritos_guardados>()) }
    var cat_selecionada by remember { mutableStateOf("Todos") }
    var mostarsin_continuar by remember { mutableStateOf(false) }
    var mostar_succes by remember { mutableStateOf(false) }
    var bottomhseet_tienda by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var id_tienda_select by remember { mutableStateOf("") }
    var localida_tienda_select by remember { mutableStateOf("") }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    val localidad_storage_user by data_store_localidad.obtener_localidad(context).collectAsState(initial = null)
    var localidad_select by remember { mutableStateOf(localidad_storage_user?:"barranca") }
    var click_categoria by remember { mutableStateOf(true) }
    var click_localida by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")
        }
    }
    LaunchedEffect(key1 = id_user) {
        viewmodelFavoritos.obtener_favoritos(id_user)
    }

    LaunchedEffect(cat_selecionada,lista_datos) {
        if(cat_selecionada.isNotEmpty()){
            Log.d("cat13123","no vacio")
            viewmodelFavoritos.filtrar_categoira(cat_selecionada)
        }
    }

    var sizeAnterior by remember { mutableStateOf(0) }

    LaunchedEffect(lista_datos) {
        val categoriaActual = cat_selecionada

        // No hacer nada si ya está en "Todos"
        if (categoriaActual == "Todos") return@LaunchedEffect

        Log.d("FAV_DEBUG", "lista_datos filtrada size = ${lista_datos.size}")

        // Verificar si en la lista original sigue existiendo esa categoría
        val existeCategoriaEnListaOriginal =
            lista_subcategorias.any { cat ->
                cat.equals(categoriaActual, ignoreCase = true)
            }


        Log.d("FAV_DEBUG", "¿Existe categoria '$categoriaActual' en lista original?: $existeCategoriaEnListaOriginal")

        if (!existeCategoriaEnListaOriginal) {
            Log.d("FAV_DEBUG", "La categoría '$categoriaActual' ya no existe. Cambiando a 'Todos'...")

            cat_selecionada = "Todos"
            viewmodelFavoritos.filtrar_categoira("Todos")
        }
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
            val listalocalidad =
                (lista_fb_size as viewModel_favoritos.state_fv.succes).localidad_list
            if (listaFavoritos.isNotEmpty() || listaCategorias.isNotEmpty()) {
                lista_subcategorias = listaCategorias
                lista_datos = listaFavoritos
                lista_localidad_filtrado = listalocalidad
                mostar_succes = true
                mostarsin_continuar = false
            }
        }
    }

    Crossfade(
        targetState = if (mostarsin_continuar) "empty" else if (mostar_succes) "success" else "none",
        animationSpec = tween(500)
    ) { state ->
        when (state) {

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
                                { imagenActiva = if (imagenActiva == 0) null else 0 },
                                imagenActiva == 0
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
                Box {
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

                                TextoFavoritosConFiltros(
                                    localidad = "Barranca",
                                    categoria = cat_selecionada,
                                    onClickLocalidad = {
                                        click_categoria = false
                                        click_localida = true
                                    },
                                    onClickCategoria = {
                                        click_categoria = true
                                        click_localida = false
                                    })
                                spacer_vertical(7.dp)
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)) {
                                    this@Column.AnimatedVisibility(
                                        click_categoria,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {


                                            LazyRowConSombras {
                                                items(lista_mas_todos) { cat ->
                                                    val catSeleccionada = cat_selecionada == cat

                                                    chisp_filtrado_busqueda(
                                                        carta_selecionada = catSeleccionada,
                                                        filtrado = cat,
                                                        btn_visible = false,
                                                        clik_card = { cat_selecionada = cat },
                                                        onClick_delete = {}
                                                    )
                                                }
                                            }




                                    }
                                    this@Column.AnimatedVisibility(
                                        click_localida,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        LazyRowConSombras() {
                                            items(lista_localidad_filtrado) { cat ->
                                                val catSeleccionada = localidad_select == cat
                                                chisp_filtrado_busqueda(
                                                    carta_selecionada = catSeleccionada,
                                                    filtrado = cat,
                                                    btn_visible = false,
                                                    clik_card = { localidad_select = cat },
                                                    onClick_delete = {
                                                    }
                                                )
                                            }
                                        }

                                    }
                                }
                                spacer_vertical(10.dp)
                            }
                        }

                        itemsIndexed(lista_datos) { index, item ->
                            Log.d("safdSADFGJSAIUGHAsuorg", item.horario.toString())
                            val heightOptions = listOf(300.dp, 350.dp)
                            val boxHeight =
                                if (index % 2 == 0) heightOptions[0] else heightOptions[1]

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
                                            model = ImageRequest.Builder(context)
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
                                            val coordenadasValidas =
                                                item.lat != 0.0 && item.lng != 0.0
                                            if (coordenadasValidas) {
                                                FloatingActionButton(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = Color.White,
                                                    onClick = {
                                                        dialog_Crear_ruta = true
                                                        lat = item.lat
                                                        lng = item.lng


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
                                        ) {}
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

    if (dialog_Crear_ruta) {
        dialog_crear_ruta_lugares({ dialog_Crear_ruta = false }, { crear_ruta ->
            dialog_Crear_ruta = false
            if (crear_ruta && verificarUbiActiva(context)) {
                constantes_lista_localidades.abrir_google_maps(
                    context, lat, lng,
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
            })
    }

}

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

@Composable
fun TextoFavoritosConFiltros(
    localidad: String,
    categoria: String,
    onClickLocalidad: () -> Unit,
    onClickCategoria: () -> Unit
) {
    val texto = buildAnnotatedString {

        append("Aquí tienes tus favoritos, aquello que guardaste porque es importante para ti. ")
        append("Lo que ves en este momento corresponde a tus favoritos de ")

        // 👉 LOCALIDAD clickeable
        pushStringAnnotation(tag = "LOCALIDAD", annotation = localidad)
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(localidad)
        }
        pop()

        append(", dentro de la categoría de ")

        // 👉 CATEGORÍA clickeable
        pushStringAnnotation(tag = "CATEGORIA", annotation = categoria)
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(categoria)
        }
        pop()

        append(".")
    }

    ClickableText(
        text = texto,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onBackground
        )
    ) { offset ->
        texto.getStringAnnotations("LOCALIDAD", start = offset, end = offset)
            .firstOrNull()?.let { onClickLocalidad() }

        texto.getStringAnnotations("CATEGORIA", start = offset, end = offset)
            .firstOrNull()?.let { onClickCategoria() }
    }
}


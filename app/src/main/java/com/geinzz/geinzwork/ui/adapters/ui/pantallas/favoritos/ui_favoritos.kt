package com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos


import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_listener_fv_externo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_crear_ruta_lugares
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_eliminar_favoritos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda.LazyRowConSombras
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.TiempoRestanteCierre
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.principal.AutoResizeOneLineText
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.categorias_defaul
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
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
    verificar_intener: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    viewmodelFavoritos: viewModel_favoritos,
    datos_principales_user: datos_principales_user,
    empty_select_chip: (String, String, String) -> Unit,
    mostar_butom_var: () -> Unit,
    ocultar_buttom_var: () -> Unit
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
    var lista_subcategorias by remember { mutableStateOf(listOf<String>()) }
    var lista_localidad_filtrado by remember { mutableStateOf(listOf<String>()) }
    var lista_datos by remember { mutableStateOf(listOf<favoritos_guardados>()) }
    var cat_selecionada by remember { mutableStateOf("Todos") }
    var mostarsin_continuar by remember { mutableStateOf(false) }
    var mostar_succes by remember { mutableStateOf(false) }
    var mostrar_loading by remember { mutableStateOf(false) }
    var bottomhseet_tienda by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var id_tienda_select by remember { mutableStateOf("") }
    var localida_tienda_select by remember { mutableStateOf("") }
    var dialog_Crear_ruta by remember { mutableStateOf(false) }
    var validacion_mostrar_dialog_ubi_off by remember { mutableStateOf(false) }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    val localidad_storage_user by data_store_localidad.obtener_localidad(context)
        .collectAsState(initial = null)
    var localidad_select by remember { mutableStateOf(localidad_storage_user ?: "barranca") }
    var click_categoria by remember { mutableStateOf(true) }
    var click_localida by remember { mutableStateOf(false) }
    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")
        }
    }
    var ocultar_sombnra by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = id_user) {
        viewmodelFavoritos.obtener_favoritos(id_user)
    }

    LaunchedEffect(cat_selecionada, lista_datos) {
        if (cat_selecionada.isNotEmpty()) {
            Log.d("cat13123", "no vacio")
            viewmodelFavoritos.filtrar_categoira(cat_selecionada)
        }
    }

    var sizeAnterior by remember { mutableStateOf(0) }

    LaunchedEffect(lista_datos) {

        // ---- 🆕 Detectar si ya no queda ningún favorito en total ----
        if (lista_datos.isEmpty()) {
            Log.d(
                "FAV_DEBUG",
                "Se eliminaron todos los favoritos. Limpiando listas del ViewModel..."
            )
            viewmodelFavoritos.limpiar_listas_favoritos()   // <--- aquí llamas tu función
            cat_selecionada = "Todos"
            return@LaunchedEffect
        }
        // ------------------------------------------------------------

        val categoriaActual = cat_selecionada

        Log.d("FAV_DEBUG", "lista_datos filtrada size = ${lista_datos.size}")
        if (categoriaActual == "Todos") return@LaunchedEffect

        // Verificar si la categoría sigue existiendo
        val existeCategoriaEnListaOriginal =
            lista_subcategorias.any { cat ->
                cat.equals(categoriaActual, ignoreCase = true)
            }

        Log.d("FAV_DEBUG", "¿Existe categoría '$categoriaActual'?: $existeCategoriaEnListaOriginal")

        if (!existeCategoriaEnListaOriginal) {
            Log.d(
                "FAV_DEBUG",
                "La categoría '$categoriaActual' ya no existe. Cambiando a 'Todos'..."
            )

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
            mostrar_loading = false
            mostar_succes = false
            mostarsin_continuar = true

        }

        is viewModel_favoritos.state_fv.error -> {
            mostrar_loading = false
            mostar_succes = false
            mostarsin_continuar = true
        }

        viewModel_favoritos.state_fv.loading -> {
            mostrar_loading = true
            mostar_succes = false
            mostarsin_continuar = false

        }

        is viewModel_favoritos.state_fv.succes -> {
            mostrar_loading = false
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
    val listState = rememberLazyListState()

    val isAtEnd by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible == totalItems - 1 && totalItems > 0
        }
    }

    LaunchedEffect(isAtEnd) {
        if (isAtEnd) {
            ocultar_sombnra = true
            // Aquí llamas tu función:
            // cargarMásDatos()
        } else {
            ocultar_sombnra = false
        }
    }

    LaunchedEffect(mostrar_loading) {
        if (mostrar_loading) {
            delay(3000)
            if (!mostrar_loading) return@LaunchedEffect
            mostar_succes = true
            mostrar_loading = false
            mostarsin_continuar = false
        }
    }



    Crossfade(
        targetState = if (mostarsin_continuar) "empty" else if (mostar_succes) "success" else if (mostrar_loading) "loading" else "none",
        animationSpec = tween(500)
    ) { state ->
        var fv_por_fuera by remember { mutableStateOf(true) }
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
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier,
                    ) {
                        item {
                            Column(
                                modifier = Modifier.padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = 15.dp
                                )
                            ) {
                                fraces_cambio(datos_principales_user.nombre)
                                val lista_mas_todos = listOf("Todos") + lista_subcategorias
                                spacer_vertical(10.dp)
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
                                spacer_vertical(15.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp)
                                ) {
                                    this@Column.AnimatedVisibility(
                                        click_categoria,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        LazyRowConSombras() {

                                            items(lista_mas_todos) { cat ->
                                                val iconCategoria = constantes_lista_localidades.getCategoriaIcon(cat)

                                                val catSeleccionada = cat_selecionada == cat

                                                chisp_filtrado_busqueda(
                                                    carta_selecionada = catSeleccionada,
                                                    filtrado = "$iconCategoria $cat",
                                                    btn_visible = false,
                                                    clik_card = { cat_selecionada = cat },
                                                    onClick_delete = {},
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
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                                spacer_vertical(15.dp)
                            }
                        }
                        items(lista_datos) { item ->
                            carta_desing_fv(
                                verificar_intener,
                                fv_por_fuera,
                                viewModelFiltros,
                                id_user,
                                context,
                                item,
                                tick
                            ) { id_tienda, localida ->
                                bottomhseet_tienda = true
                                id_tienda_select = id_tienda
                                localida_tienda_select = localida
                            }
                        }
                    }

                    AnimatedVisibility(
                        !ocultar_sombnra,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
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

            "loading" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        strokeWidth = 5.dp
                    )
                }
            }

            else -> {
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
        }
    }


    if (bottomhseet_tienda) {
        bottom_sheet_tiendas_filtradas(
            verificar_intener,
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

@Composable
fun carta_desing_fv(
    verificar_internet: Boolean,
    fv_bool: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    id_user: String,
    context: Context,
    item: favoritos_guardados,
    tick: Long, clik_card: (id_tienda: String, localida: String) -> Unit
) {
    var mostrar_dialog_eliminar by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 20.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        clik_card(item.id_tienda_lugar, item.localida_tienda)
//                bottomhseet_tienda = true
//                id_tienda_select = item.id_tienda_lugar
//                localida_tienda_select = item.localida_tienda
                    },
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)   // altura exacta de la sombra
                    .clip(RoundedCornerShape(10.dp))
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xAA000000) // negro sutil abajo
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

        }
        spacer_vertical(10.dp)

        Row() {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    texto_generico_one_line(
                        item.localida_tienda.capitalizeFirst(),
                        MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                    )
                }
                spacer_vertical(5.dp)
                Text(
                    text = item.nombre_lugar_tienda.capitalizeFirst(),
                    maxLines = 1,
                    fontSize = 20.sp,
                    fontFamily = baners_geinz_work
                )
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
                val iconCategoria = constantes_lista_localidades.getCategoriaIcon(item.categoria)
                texto_generico_one_line(
                    "$iconCategoria ${item.categoria}",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
            }

            AnimatedVisibility(verificar_internet, enter = fadeIn(), exit = fadeOut()) {
                btn_listener_fv_externo(
                    fv_bool,
                    Modifier.padding(top = 5.dp, start = 5.dp, end = 5.dp), {
                        mostrar_dialog_eliminar = true
//                    viewModelFiltros.eliminar_tienda_favorita(id_user, item.id_tienda_lugar)
                    }
                )
            }
        }
    }
    spacer_vertical(10.dp)

    if (mostrar_dialog_eliminar) {
        dialog_eliminar_favoritos(
            viewModelFiltros = viewModelFiltros,
            id_user = id_user,
            id_tienda = item.id_tienda_lugar,
            nombre_tienda = item.nombre_lugar_tienda,
            ondimis = { mostrar_dialog_eliminar = false }, aceptado = {})
    }
}


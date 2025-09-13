package com.geinzz.geinzwork.ui.adapters.ui.principal


import android.graphics.BitmapFactory
import android.graphics.Shader
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
//import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_filtrado_localidades
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.localidad_Selecionada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.rutas_turismo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.seguridad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulo_referenciales_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.extractPaletteColors
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.getScaledBitmap
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private lateinit var firebaseAuth: FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal(
    viewmodel: viewModel_localizate_geinz,
    categorias: (localidad: String, nombre_user: String) -> Unit,
    clikear_cartas: (String, String, String) -> Unit,
    ver_lugares: () -> Unit,
    listner_busqueda: () -> Unit,
    navController: NavController,
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    val _categorias_tiendas by viewModel_cordenadas._sub_cat_tiendas.observeAsState(emptyList())
    val datos_user by viewModel_cordenadas.userData.observeAsState(null)
    val _obtener_filtrado_localidades by viewModel_cordenadas._lista_filtrado_localidades.observeAsState(
        emptyList()
    )
    val encontrados_activos_tiendass by viewmodel.encontrados_activos_tiendas.observeAsState()

    Log.d("itemadadasd", _obtener_filtrado_localidades.toString())
    var nombre_user: String by rememberSaveable { mutableStateOf("") }
    var img_perfil by rememberSaveable { mutableStateOf("") }

    if (firebaseAuth.currentUser != null) {
        nombre_user = datos_user?.nombre ?: "Usuario"
        img_perfil = datos_user?.img_perfil ?: ""
    } else {
        nombre_user = "Usuario"
        img_perfil = ""
    }


    LaunchedEffect(Unit) {
        viewModel_cordenadas.obtener_subcategorias(true)
        viewModel_cordenadas.obtner_filtrado_localidades()
        viewModel_cordenadas.obtener_datos_user_registrado(firebaseAuth.uid.toString())
    }
    val ultimaLocalidad by data_store_localidad
        .obtener_localidad(context)
        .collectAsState(initial = null)

    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    val localidad_defaul = ultimaLocalidad ?: "barranca"
    val localidadSeleccionada = rememberSaveable { mutableStateOf("barranca") }

    val stickyHeaderIndex = 1
    var toastShown by remember { mutableStateOf(false) }

    val paletteCache = remember { mutableMapOf<Int, List<Color>>() }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex >= stickyHeaderIndex && !toastShown) {
            toastShown = true
        } else if (listState.firstVisibleItemIndex < stickyHeaderIndex) {
            toastShown = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 12.dp, end = 12.dp, top = 10.dp)
    ) {

        LazyColumn(
            state = listState,
        ) {
            item {
                nombre_texto_img_perfil(nombre_user, img_perfil)
            }
            stickyHeader() {
                ColumnContenedorComun {
                    texFiel_fake(listner_busqueda, toastShown)
                }
            }
            item {
                spacer_vertical(10.dp)
                filtrado_localidades(
                    localidad_defaul, _obtener_filtrado_localidades
                ) { localidad_selecionada ->
                    localidadSeleccionada.value = localidad_selecionada
                }
                spacer_vertical(35.dp)
            }
            item {
                spacer_vertical(10.dp)
                apartado_explora_cat(
                    _categorias_tiendas,
                    localidad_defaul,
                    nombre_user,
                    { nombre, localidad ->
                        categorias(localidad, nombre)
                    }, { categoria, localidad, nombre ->
                        clikear_cartas(categoria, localidad, nombre)
                    })
                spacer_vertical(20.dp)
            }


            item {
                rutas_turismo(
                    "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0159.00_00_00_00.Imagen%20fija002.webp?alt=media&token=c4c60311-1293-4731-b2e4-c51265c15860",
                    "ver eventos",
                    "Descubre todo ${localidad_defaul}"

                ) {}
                spacer_vertical(30.dp)
            }

            item {
                spacer_vertical(10.dp)
                val imgActual by rememberSaveable {
                    mutableStateOf(constantes_lista_localidades.lista_img_seguridad.random())
                }
                seguridad(
                    imgActual,
                    "ver rutas",
                    "Salud y seguridad cuidadana"
                ) { ver_lugares() }

                spacer_vertical(20.dp)
            }
            item {
                rutas_turismo(
                    "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0593.webp?alt=media&token=8e770a68-dfad-4ae1-8d20-c9133e2f4a49",
                    "ver Lugares",
                    "Mira los eventos proximos de ${localidad_defaul}"
                ) { ver_lugares() }
                spacer_vertical(20.dp)
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
                .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
        )
    }

}


//@Composable
//fun TopGradientBlurred(
//    modifier: Modifier = Modifier,
//
//) {
//    Box(
//        modifier = modifier
//
//            .blur(30.dp) // Mantiene un desenfoque medio para que se note
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        // Los colores se basan en tu color principal, pero con más opacidad y profundidad.
//                        Color(0xFF8700F3).copy(alpha = 0.8f), // Tu color base, más visible
//                        Color(0xFF5C00E6).copy(alpha = 0.6f),  // Un púrpura más oscuro
//                        Color(0xFF140428).copy(alpha = 0.5f)  // Un tono muy oscuro para el fondo
//                    )
//                )
//            )
//    ) {
//        // La capa negra superpuesta ayuda a oscurecer y suavizar el efecto.
//        Box(
//            Modifier
//                .matchParentSize()
//                .background(Color.Black.copy(alpha = 0.2f))
//        )
//    }
//}
@Composable
fun AlbumBackgroundBlurOptimized(albumRes: Int, heightDp: Dp = 300.dp) {
    val context = LocalContext.current
    val paletteCache = remember { mutableMapOf<Int, List<Color>>() }
    var colors by remember { mutableStateOf(listOf(Color.Black, Color.DarkGray)) }

    // Solo calculamos si no está en cache
    LaunchedEffect(albumRes) {
        paletteCache[albumRes]?.let {
            colors = it
        } ?: run {
            val bitmap = getScaledBitmap(context, albumRes)
            extractPaletteColors(bitmap) { extracted ->
                colors = extracted.map { Color(it) } // <- convertimos Int a Color
                paletteCache[albumRes] = colors
            }
        }
    }

    // Fondo con gradiente + blur
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .background(Brush.verticalGradient(colors))
            .blur(80.dp)
    )
}


@Composable
fun apartado_explora_cat(
    categorias_tienda: List<dataclass_cat_sub>,
    localidad_selecionada: String?,
    nombre_user: String,
    categorias1: (String, String) -> Unit,
    clikear_cartas: (String, String, String) -> Unit,
) {
    val localidad_defaul = localidad_selecionada ?: "barranca"
    Log.d("obtemloms_lista", categorias_tienda.toString())
    val categoriasPrincipales = categorias_tienda
    spacer_vertical(10.dp)

    Column {
        titulo_referenciales_geinz_work(
            "Explora ${localidad_defaul.capitalizeFirst()}",
            "Ver todos"
        ) { categorias1(nombre_user, localidad_defaul) }
        spacer_vertical(10.dp)
        cartas_filtrado(
            nombre_user,
            localidad_defaul,
            categorias_tienda
        ) { categoria, localidad, nombre ->
            Log.d("localdiasdadas", "$categoria, $localidad ,$nombre")
            clikear_cartas(categoria, localidad, nombre)
        }
//        cartas_explorar_tienda(localidad_defaul, categorias_tienda)
//        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//            items(
//                items = categoriasPrincipales,
//                key = { it.nombre.toString() }
//            ) { categorias ->
//                apartado_categorias_tiendas(
//                    categorias.lista_img,
//                    categorias.nombre.toString(), nombre_user, localidad_selecionada,
//                    categorias.lista_subcategorias,
//                    300.dp,
//                    200.dp,
//                    5, { categoria, localidad, nombre ->
//                        clikear_cartas(categoria, localidad, nombre)
//                    })
//            }
//        }
    }
}

@Composable
fun cartas_filtrado(
    nombre_user: String?,
    localidad_defaul: String,
    lista: List<dataclass_cat_sub>,
    carta_clikeada: (String, String, String) -> Unit
) {
    var cartaSeleccionada by remember { mutableStateOf<String?>(null) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { i ->
            val seleccionada = cartaSeleccionada == i.nombre

            val anchoAnimado by animateDpAsState(
                targetValue = if (seleccionada) 200.dp else 130.dp,
                label = "anchoCarta"
            )
            val fontSizeAnimada by animateFloatAsState(
                targetValue = if (seleccionada) 20f else 17f, // tamaño expandido vs normal
                label = "fontSizeAnimada"
            )



            Box(
                modifier = Modifier
                    .width(anchoAnimado)
                    .height(190.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        cartaSeleccionada = if (seleccionada) null else i.nombre
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(i.lista_img)
                        .crossfade(true)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.sin_item_carrito)
                        .build(),
                    contentDescription = "Imagen de la tienda",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(anchoAnimado)
                        .height(190.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(anchoAnimado)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x33000000),
                                    Color(0x66000000),
                                    Color(0xDD000000)
                                )
                            )
                        )
                )

                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(5.dp)
                ) {
                    Text(
                        text = simplificarCategoria(i.nombre.toString()).capitalizeFirst(),
                        fontFamily = baners_geinz_work,
                        fontSize = fontSizeAnimada.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = if (seleccionada) TextOverflow.Clip else TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        texto_generico_one_line(
                            localidad_defaul.capitalizeFirst(),
                            MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Ubicación",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${i.lista_subcategorias.size} categorías",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Label,
                            contentDescription = "Categorías",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }


                    spacer_vertical(5.dp)
                }

                // El botón siempre existe, pero animamos su visibilidad
                AnimatedVisibility(
                    visible = seleccionada,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                ) {
                    Button(
                        onClick = {  carta_clikeada(i.nombre.toString(), localidad_defaul, nombre_user.toString()) },
                        modifier = Modifier.size(32.dp), // cuadrado pequeño
                        contentPadding = PaddingValues(0.dp) // quita relleno interno
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, // o Search, Explore, etc
                            contentDescription = "Explorar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun apartado_categorias_tiendas(
    img: String,
    nombre_categoria: String,
    localidad: String, nombre_user: String?,
    lista_subcateogiras: List<String>,
    ancho: Dp,
    alto: Dp,
    rounder: Int,
    carta_clikeada: (String, String, String) -> Unit
) {

    Column(
        modifier = Modifier
            .width(ancho)
            .clip(RoundedCornerShape(rounder))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .width(ancho)
                .height(alto)
                .clickable { carta_clikeada(nombre_categoria, localidad, nombre_user ?: "User") }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img)
                    .size(ancho.value.toInt(), alto.value.toInt())
                    .crossfade(true)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .width(ancho)
                    .height(alto)
                    .clip(RoundedCornerShape(rounder)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(rounder))
                    .fillMaxWidth()
                    .height(alto * 0.6f) // cubre 60% desde abajo
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,       // arriba: totalmente transparente
                                Color(0x33000000),       // negro suave arriba
                                Color(0x66000000),       // negro semi-transparente
                                Color(0xAA000000),       // negro más intenso
                                Color(0xEE000000)        // negro casi total abajo para resaltar el texto
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

//            mascara_img(rounder, alto, ancho, Modifier.align(Alignment.BottomStart))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
            ) {
                Column(modifier = Modifier.padding(vertical = 0.dp, horizontal = 10.dp)) {
                    texto_categorias(nombre_categoria)
                    spacer_vertical(10.dp)
                    tags_subcateogiras(lista_subcateogiras)

                }


            }

        }


    }
}

@Composable
fun texto_categorias(
    nombre_categoria: String,

    ) {
    texto_generico_one_line(
        nombre_categoria.uppercase(), MaterialTheme.typography.titleSmall,
        Modifier

            .padding(end = 10.dp)
    )
}


@Composable
fun texto_encimado_cartas(
    defecto_selecionado: Boolean,
    modifier: Modifier,
    titulo: String,
    descripcion: String,
) {
    Row(modifier = modifier.padding(start = 20.dp, end = 20.dp, bottom = 40.dp)) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animamos el salto del texto de arriba
                val offsetY by animateDpAsState(
                    targetValue = if (defecto_selecionado) (-1).dp else 0.dp, // pequeño salto hacia arriba
                    animationSpec = tween(durationMillis = 300),
                    label = "offsetY"
                )

                texto_generico_multilinea(
                    titulo,
                    MaterialTheme.typography.banerGeinzWork,
                    modifier = Modifier.offset(y = offsetY) // aplicamos el salto suave
                )

                if (defecto_selecionado) {
                    localidad_Selecionada()
                }
            }

            spacer_vertical(5.dp)

            // Aquí NO tocamos nada
            Crossfade(
                targetState = descripcion,
                animationSpec = tween(durationMillis = 500)
            ) { textoAnimado ->
                texto_generico_one_line(
                    textoAnimado.capitalizeFirst(),
                    MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                )
            }
        }
    }
}


@Composable
fun texFiel_fake(listner_busqueda: () -> Unit, toastShown: Boolean) {

    // Animar el padding horizontal
    val paddingAnim by animateDpAsState(
        targetValue = if (toastShown) 10.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = paddingAnim,
                end = paddingAnim,
                bottom = (paddingAnim - 5.dp).coerceAtLeast(0.dp)
            )
            .height(60.dp)
            .border(1.dp, Color(0xFF75707A), RoundedCornerShape(60))
            .clip(RoundedCornerShape(60))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                listner_busqueda()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(60.dp)
                .padding(start = 20.dp, end = 20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color(0xFF75707A)),
            )
            spacer_horizonta(10.dp)
            Text(
                "A dónde quieres llegar?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

//@Composable
//fun filtrado_localidades(
//    ultimaLocalidad: String?,
//    lista_localidades: List<localidades_filtrado>,
//    nombre_localidad_selecionado: (String) -> Unit
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val listState = rememberLazyListState()
//
//    // selección (igual que antes, la cargamos desde DataStore)
//    var localidad_defecto by remember { mutableStateOf<String?>(null) }
//
//    // guardamos aquí sólo el orden como List<String> (nombres). rememberSaveable lo soporta.
//    var ordenGuardado by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
//
//    // estado visible de la lista (orden dinámico)
//    var listaLocalidadesState by remember { mutableStateOf(lista_localidades) }
//
//    // Inicialización: cargar selección y/o reconstruir orden desde ordenGuardado
//    LaunchedEffect(Unit) {
//        // cargar selección guardada en DataStore (si aún no la tenemos)
//        if (localidad_defecto == null) {
//            val guardada = data_store_localidad.obtener_localidad(context) as? String
//            localidad_defecto = guardada ?: ultimaLocalidad
//        }
//
//        if (ordenGuardado.isNotEmpty()) {
//            // reconstruir la lista en base a los nombres guardados
//            val reconstruida = ordenGuardado.mapNotNull { nombre ->
//                lista_localidades.find { it.nombre.equals(nombre, ignoreCase = true) }
//            }
//            // añadir cualquier elemento nuevo que no estuviera en el orden guardado
//            val restantes = lista_localidades.filterNot { l ->
//                reconstruida.any { it.nombre.equals(l.nombre, ignoreCase = true) }
//            }
//            listaLocalidadesState = reconstruida + restantes
//        } else {
//            // si no hay orden guardado, aplicar la selección (si existe) como primera posición
//            localidad_defecto?.let { sel ->
//                val seleccionada = lista_localidades.find { it.nombre.equals(sel, ignoreCase = true) }
//                val resto = lista_localidades.filterNot { it.nombre.equals(sel, ignoreCase = true) }
//                listaLocalidadesState = listOfNotNull(seleccionada) + resto
//            }
//        }
//
//        // opcional: asegurarnos que el primer item esté visible
//        if (listaLocalidadesState.isNotEmpty()) {
//            listState.scrollToItem(0)
//        }
//    }
//
//    Column {
//        Spacer(modifier = Modifier.height(10.dp))
//        LazyRow(
//            state = listState,
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(listaLocalidadesState, key = { it.nombre }) { item ->
//                carta_filtrado_localidades(
//                    defecto_selecionado = localidad_defecto.equals(item.nombre, ignoreCase = true),
//                    item.nombre,
//                    item.lista_img,
//                    5,
//                    320.dp,
//                    300.dp
//                ) { nombre_localidad ->
//                    // cuando el usuario selecciona una localidad:
//                    localidad_defecto = nombre_localidad
//                    nombre_localidad_selecionado(nombre_localidad)
//
//                    // reordenar la lista para que la seleccionada quede al inicio
//                    val seleccionada = lista_localidades.find { it.nombre.equals(nombre_localidad, ignoreCase = true) }
//                    val resto = lista_localidades.filterNot { it.nombre.equals(nombre_localidad, ignoreCase = true) }
//                    listaLocalidadesState = listOfNotNull(seleccionada) + resto
//
//                    // Guardar el nuevo orden (solo nombres) para que rememberSaveable lo restaure
//                    ordenGuardado = listaLocalidadesState.map { it.nombre }
//
//                    scope.launch {
//                        // guardar selección persistente en tu DataStore (como ya tenías)
//                        data_store_localidad.guardar_localida(context, nombre_localidad)
//                        listState.animateScrollToItem(0)
//                    }
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun filtrado_localidades(
    ultimaLocalidad: String,
    lista_localidades: List<localidades_filtrado>,
    nombre_localidad_selecionado: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()


    var localidad_defecto by rememberSaveable { mutableStateOf(ultimaLocalidad) }



    LaunchedEffect(ultimaLocalidad) {
        ultimaLocalidad?.let { seleccionada ->
            localidad_defecto = seleccionada
            val index = lista_localidades.indexOfFirst {
                it.nombre.equals(seleccionada, ignoreCase = true)
            }
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }


    Spacer(modifier = Modifier.height(10.dp))
    if (lista_localidades.isNotEmpty()) {
        val carouselState = rememberCarouselState {
            lista_localidades.size
        }
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = 300.dp,
            itemSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) { index ->
            val item = lista_localidades[index]
            val randomImg = remember(item.lista_img) { item.lista_img.randomOrNull() }

            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(320.dp)
                    .maskClip(RoundedCornerShape(20.dp))
                    .clickable {
                        nombre_localidad_selecionado(item.nombre)
                        scope.launch {
                            data_store_localidad.guardar_localida(context, item.nombre)
                            val newIndex =
                                lista_localidades.indexOfFirst { it.nombre == item.nombre }
                            if (newIndex >= 0) {
                                listState.animateScrollToItem(newIndex)
                            }
                        }
                    }
            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(randomImg)
                            .crossfade(true)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                    contentDescription = item.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .maskClip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x66000000),
                                    Color(0xEE000000)
                                )
                            )
                        )
                )

                val titulo = if (localidad_defecto.equals(
                        item.nombre,
                        ignoreCase = true
                    )
                ) "Estás aquí 👋" else "Explorar"
                texto_encimado_cartas(
                    localidad_defecto.equals(item.nombre, ignoreCase = true),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    item.nombre.capitalizeFirst(),
                    titulo,
                )
            }


        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }


    }


//@Composable
//fun filtrado_localidades(
//    ultimaLocalidad: String?,
//    lista_localidades: List<localidades_filtrado>,
//    nombre_localidad_selecionado: (String) -> Unit
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    var localidad_defecto by remember { mutableStateOf(ultimaLocalidad) }
//    var colorFondo by remember { mutableStateOf(Color.Black) }
//    var colorTopBar by remember { mutableStateOf(Color.Black) }
//
//
//
//    LaunchedEffect(ultimaLocalidad) {
//        localidad_defecto = ultimaLocalidad
//    }
//
//    Column {
//        LazyRow(
//            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
//        ) {
//            items(lista_localidades, key = { it.nombre }) { items ->
//
//                carta_filtrado_localidades(
//                    defecto_selecionado = localidad_defecto.equals(items.nombre, ignoreCase = true),
//                    items.nombre,
//                    items.lista_img,
//                    5,
//                    320.dp,
//                    300.dp
//                ) { nombre_localidad ->
//
//                    localidad_defecto = nombre_localidad
//                    nombre_localidad_selecionado(nombre_localidad)
//
//                    scope.launch {
//                        data_store_localidad.guardar_localida(context, nombre_localidad)
//
//                        val imageUrl = items.lista_img.firstOrNull() ?: return@launch
//                        try {
//                            val loader = ImageLoader(context)
//                            val request = ImageRequest.Builder(context)
//                                .data(imageUrl)
//                                .allowHardware(false) // necesario para Palette
//                                .build()
//
//                            val result = loader.execute(request)
//                            val bitmap: Bitmap? = result.drawable?.toBitmap()
//                            bitmap?.let { bmp ->
//                                val palette = Palette.from(bmp).generate()
//                                colorFondo = Color(palette.getVibrantColor(Color.Gray.toArgb()))
//                                colorTopBar = Color(palette.getDarkVibrantColor(Color.Black.toArgb()))
//                            }
//
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
}

fun String.capitalizeFirst(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

@Composable
fun nombre_texto_img_perfil(nombre_user: String, img_url: String = "") {
    val fraces = constantes_lista_localidades.lista_fraces_inicio
    var index by remember { mutableStateOf(0) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.saludo_user))

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever // se repite infinito
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }
    Box() {
        Row() {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 10.dp, top = 10.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(15.dp)
                            .padding(bottom = 3.dp)
                    )
                    spacer_horizonta(5.dp)
                    texto_generico_one_line(
                        texto = constantes_lista_localidades.saludo_user_principal(nombre_user),
                        MaterialTheme.typography.bodyMedium
                    )
                }
                spacer_vertical(15.dp)
                Crossfade(targetState = fraces[index], label = "fraces") { txt ->
                    texto_generico_one_line(
                        texto = txt,
                        MaterialTheme.typography.busquedaGeinzWork
                    )
                }
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img_url)
                    .size(40)
                    .crossfade(true)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.img_perfil)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            spacer_vertical(5.dp)
        }
    }
}

package com.geinzz.geinzwork.ui.adapters.ui.principal


import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.capitalizeFirst
//import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_filtrado_localidades
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.localidad_Selecionada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.rutas_turismo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.seguridad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulo_referenciales_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.esAniversarioHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.extractPaletteColors
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.getScaledBitmap
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerAniversarioLocalidad
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarAniversarioLocalidad
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import androidx.compose.ui.text.TextStyle
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.eliminar_menios_comida


private lateinit var firebaseAuth: FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal(
    datos_principales_user: datos_principales_user,
    categorias: (localidad: String, nombre_user: String) -> Unit,
    clikear_cartas: (String, String, String) -> Unit,
    ver_lugares: (String) -> Unit,
    listner_busqueda: () -> Unit,
    listener_seguridad: (String) -> Unit
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    val _categorias_tiendas by viewModel_cordenadas._sub_cat_tiendas.observeAsState(emptyList())
    val _obtener_filtrado_localidades by viewModel_cordenadas._lista_filtrado_localidades.observeAsState(
        emptyList()
    )

    LaunchedEffect(Unit) {
        viewModel_cordenadas.obtener_subcategorias(true)
        viewModel_cordenadas.obtner_filtrado_localidades()
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
    var aniversario by remember { mutableStateOf(false) }



    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex >= stickyHeaderIndex && !toastShown) {
            toastShown = true
        } else if (listState.firstVisibleItemIndex < stickyHeaderIndex) {
            toastShown = false
        }
    }
    var esAniversarioHoy by rememberSaveable(localidad_defaul) {
        mutableStateOf(esAniversarioHoy(localidad_defaul))
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
                nombre_texto_img_perfil(
                    datos_principales_user.nombre,
                    datos_principales_user.img_perfil
                )
            }
            stickyHeader() {
                ColumnContenedorComun {
                    texFiel_fake(listner_busqueda, toastShown)
                }
            }
            item {
                spacer_vertical(10.dp)
                filtrado_localidades(
                    esAniversarioHoy,
                    localidad_defaul, _obtener_filtrado_localidades, { localidad_selecionada ->
                        localidadSeleccionada.value = localidad_selecionada
                    }, { esAniversario ->
                        Log.d("esAniversarioaaaa", esAniversario.toString())
                        if (esAniversarioHoy != esAniversario) {
                            esAniversarioHoy = esAniversario
                        }
                    })
                spacer_vertical(25.dp)
            }

            item {
                spacer_vertical(10.dp)
                apartado_explora_cat(
                    _categorias_tiendas,
                    localidad_defaul,
                    datos_principales_user.nombre,
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
                    "ver lugares",
                    "Descubre lugares en ${localidad_defaul}"

                ) {
//                    ver_lugares(localidad_defaul)
                    eliminar_menios_comida()
                }
                spacer_vertical(30.dp)
            }
            item {
                spacer_vertical(10.dp)
                val imgActual by rememberSaveable {
                    mutableStateOf(constantes_lista_localidades.lista_img_seguridad.random())
                }
                seguridad(
                    imgActual,
                    "Contactar",
                    "Salud y seguridad cuidadana"
                ) { listener_seguridad(localidad_defaul) }

                spacer_vertical(20.dp)
            }
            item {
                rutas_turismo(
                    "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0593.webp?alt=media&token=8e770a68-dfad-4ae1-8d20-c9133e2f4a49",
                    "ver eventos",
                    "Mira los eventos proximos de ${localidad_defaul}"
                ) { ver_lugares(localidad_defaul) }
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
    }
}

@Composable
fun cartas_filtrado(
    nombre_user: String?,
    localidad_defaul: String,
    lista: List<dataclass_cat_sub>,
    carta_clikeada: (String, String, String) -> Unit
) {
    val alturaFija = 190.dp

    var cartaSeleccionada by remember { mutableStateOf<String?>(null) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { i ->
            val seleccionada = cartaSeleccionada == i.nombre

            val anchoAnimado by animateDpAsState(
                targetValue = if (seleccionada) 200.dp else 130.dp,
                label = "anchoCarta"
            )
            val fontSizeAnimada by animateFloatAsState(
                targetValue = if (seleccionada) 20f else 18f,
                label = "fontSizeAnimada"
            )

            Box(
                modifier = Modifier
                    .width(anchoAnimado) // ancho animado (130.dp a 200.dp)
                    .height(alturaFija)  // altura fija
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        cartaSeleccionada = if (seleccionada) null else i.nombre
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(i.lista_img)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.sin_item_carrito)
                            .build(),
                        contentDescription = "Imagen de la tienda",
                        modifier = Modifier.matchParentSize(), // ocupa todo el Box
                        contentScale = ContentScale.Crop
                    )
                }

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

                    spacer_vertical(15.dp)
                }

                AnimatedVisibility(
                    visible = seleccionada,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Button(
                        onClick = {
                            carta_clikeada(
                                i.nombre.toString(),
                                localidad_defaul,
                                nombre_user.toString()
                            )
                        },
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
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
fun texto_encimado_cartas(
    aniversario: Boolean,
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
                texto_generico_multilinea(
                    titulo,
                    MaterialTheme.typography.banerGeinzWork,
                )

                if (defecto_selecionado) {
                    localidad_Selecionada()
                }
            }

            spacer_vertical(5.dp)

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

            spacer_vertical(10.dp)

            AnimatedContent(
                targetState = defecto_selecionado && aniversario,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600, delayMillis = 200)) togetherWith
                            fadeOut(animationSpec = tween(400))
                },
                label = "textoAniversario"
            ) { isVisible ->
                if (isVisible) {
                    texto_generico_one_line(
                        obtenerAniversarioLocalidad(titulo),
                        MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }


        }
    }

}

@Composable
fun texFiel_fake(listner_busqueda: () -> Unit, toastShown: Boolean) {

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun filtrado_localidades(
    aniversario: Boolean,
    ultimaLocalidad: String,
    lista_localidades: List<localidades_filtrado>,
    nombre_localidad_selecionado: (String) -> Unit,
    clikeable: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetii))
    var localidad_defecto by rememberSaveable { mutableStateOf(ultimaLocalidad) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

//    var index by remember { mutableStateOf(0) }
    LaunchedEffect(ultimaLocalidad) {
        ultimaLocalidad.let { seleccionada ->
            localidad_defecto = seleccionada

            clikeable(false)

            val aniversarioHoy = esAniversarioHoy(seleccionada)
            Log.d("ANIVERSARIO", "Localidad: $seleccionada → $aniversarioHoy")

            clikeable(aniversarioHoy)

//            index = lista_localidades.indexOfFirst {
//                it.nombre.equals(seleccionada, ignoreCase = true)
//            }.coerceAtLeast(0)
//            if (index >= 0) {
//
//            }
        }
    }



    Spacer(modifier = Modifier.height(10.dp))
    if (lista_localidades.isNotEmpty()) {
        val index = lista_localidades.indexOfFirst {
            it.nombre.equals(ultimaLocalidad, ignoreCase = true)
        }.coerceAtLeast(0)

        val carouselState = rememberCarouselState(
            initialItem = index,
            itemCount = { lista_localidades.size }
        )
        Log.d("indexindex",index.toString())
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = screenWidth * 0.8f,
            itemSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) { index ->
            val item = lista_localidades[index]
            val randomImg = remember(item.lista_img) { item.lista_img.randomOrNull() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .maskClip(RoundedCornerShape(20.dp))
                    .clickable {
                        scope.launch {
                            data_store_localidad.guardar_localida(context, item.nombre)
                            val newIndex =
                                lista_localidades.indexOfFirst { it.nombre == item.nombre }
                            if (newIndex >= 0) {
//                                listState.animateScrollToItem(newIndex)
                            }
                        }
                        localidad_defecto = item.nombre

                        nombre_localidad_selecionado(item.nombre)

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
                if (localidad_defecto.equals(item.nombre, ignoreCase = true) && aniversario) {
                    Log.d("aniversario", aniversario.toString())
                    LottieAnimation(
                        composition,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter)
                    )
                }

                val titulo = if (localidad_defecto.equals(
                        item.nombre,
                        ignoreCase = true
                    )
                ) {
                    "Estás aquí 👋"
                } else {
                    "Explorar"
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    texto_encimado_cartas(
                        aniversario,
                        localidad_defecto.equals(item.nombre, ignoreCase = true),
                        modifier = Modifier,
                        item.nombre.capitalizeFirst(),
                        titulo,
                    )
                }


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
                    AutoResizeOneLineText(
                        text = txt,
                        style = MaterialTheme.typography.busquedaGeinzWork
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

@Composable
fun AutoResizeOneLineText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 12.sp,
    maxTextSize: TextUnit = 32.sp
) {
    BoxWithConstraints(
        modifier = modifier.height(40.dp) // altura fija según tu diseño
    ) {
        val density = LocalDensity.current
        val scaledSize = with(density) {
            (maxWidth.toPx() / (text.length * 0.6f) / density.density)
                .coerceIn(minTextSize.value, maxTextSize.value)
                .sp
        }

        Text(
            text = text,
            style = style.copy(fontSize = scaledSize),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false
        )
    }
}


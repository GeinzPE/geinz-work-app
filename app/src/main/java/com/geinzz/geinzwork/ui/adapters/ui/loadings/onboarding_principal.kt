package com.geinzz.geinzwork.ui.adapters.ui.loadings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.onboarding.dataclass_onboarding
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingPrincipal() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val lista_colores_degradado_bottom = constantes_lista_localidades.lista_color_degradado_bottom
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (page) {
                0 -> pantalla1(lista_colores_degradado_top) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 1,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }
                }

                1 -> pantalla2(lista_colores_degradado_bottom)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla1(lista_colores_degradado: List<Color>, onNext: () -> Unit) {
    val lista_localidades = constantes_lista_localidades.lista_img_localidades_nombre
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % lista_localidades.size
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) with
                        fadeOut(animationSpec = tween(1500))
            }
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(lista_localidades[index].img)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        fondo_osucro(lista_colocares = lista_colores_degradado)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {

            spacer_vertical(20.dp)
            Box(modifier = Modifier.fillMaxWidth(0.7f)) {
                texto_generico_multilinea(
                    "Bienvenido a geinz".uppercase(), MaterialTheme.typography.busquedaGeinzWork
                )
            }
            spacer_vertical(15.dp)
            texto_generico_multilinea(
                "Explora Barranca, Supe, Puerto Supe, Pativilca y Paramonga. Descubre tiendas, lugares turísticos y los eventos más próximos en cada localidad. Mantente al día con todo lo que sucede cerca de ti y encuentra fácilmente los sitios que quieres visitar",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(20.dp)
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    galeria_img(lista_localidades, currentImageIndex) { index ->
                        currentImageIndex = index
                    }
                }
                Box(Modifier.padding(horizontal = 20.dp)) {
                    CelularAnimacion(modifier = Modifier.align(Alignment.BottomCenter), {
                        onNext()
                    }, orientation = Orientation.Vertical)
                }
            }
            spacer_vertical(30.dp)
        }
        CartaLocalizacion(
            lugar = lista_localidades[currentImageIndex].nombre_lugar,
            localida = lista_localidades[currentImageIndex].nombre_localidad, true
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)

        ) {
            AsyncImage(
                model = R.drawable.logo_geinz_blanco,
                contentDescription = null,
                modifier = Modifier.size(55.dp),
                contentScale = ContentScale.Inside
            )
        }

    }
}

@Composable
fun CartaLocalizacion(
    lugar: String,
    localida: String,
    icon: Boolean
) {
    Column(modifier = Modifier.padding(10.dp)) {
        Text(
            text = lugar.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
        spacer_vertical(5.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = localida.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
            spacer_horizonta(5.dp)
            if (icon) {
                Image(
                    painter = painterResource(R.drawable.location_drawable),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}


@Composable
fun fondo_osucro(scale: Float = 1f, lista_colocares: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//            }
            .blur(40.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = lista_colocares,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    )
}


@Composable
fun FondoOscuroAlto(listaColores: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // dale más altura para suavizar el corte
            .background(
                brush = Brush.verticalGradient(
                    colors = listaColores,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .blur(80.dp) // aumenta el blur después
    )
}


@Composable
fun pantalla2(lista_colores_degradaro: List<Color>) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (page) {
                0 -> pantalla3(pagerState = pagerState, page = page, lista_colores_degradaro) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 1,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }
                }

                1 -> pantalla4(pagerState = pagerState, page = page, lista_colores_degradaro){
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 2,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }
                }
                2 -> pantalla5(pagerState = pagerState, page = page, lista_colores_degradaro){
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 3,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }
                }
                3 -> pantalla6(pagerState = pagerState, page = page, lista_colores_degradaro){

                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla3(
    pagerState: PagerState,
    page: Int,
    lista_colores_degradado: List<Color>,
    onNext: () -> Unit
) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    val lista_sub_pantallas = constantes_lista_localidades.fracespantalla1
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == page) {
            while (true) {
                // Zoom hacia 1.15f
                scaleAnim.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Zoom de regreso a 1f
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Cambia la imagen solo cuando vuelve a su tamaño normal
                currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
            }
        } else {
            scaleAnim.snapTo(1f)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) with fadeOut(animationSpec = tween(1500))
            }
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(lista_sub_pantallas[index].img)
                    .crossfade(false)
                    .build(),
                contentDescription = null,

                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    spacer_vertical(20.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(IntrinsicSize.Min)
                    ) {
                        Crossfade(
                            targetState = currentImageIndex,
                            animationSpec = tween(1500)
                        ) { index ->
                            texto_generico_multilinea(
                                lista_sub_pantallas[index].titulo.uppercase(),
                                MaterialTheme.typography.busquedaGeinzWork
                            )
                        }
                    }

                    spacer_vertical(15.dp)
                    Crossfade(
                        targetState = currentImageIndex,
                        animationSpec = tween(1500)
                    ) { index ->
                        texto_generico_multilinea(
                            lista_sub_pantallas[index].texto,
                            MaterialTheme.typography.bodyMedium
                        )
                    }


                }
                Box(Modifier.padding(end = 20.dp, start = 10.dp, bottom = 20.dp)) {
                    CelularAnimacion(
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        ), {
                            onNext()
                        }, orientation = Orientation.Horizontal
                    )

                }
            }

        }

        FondoOscuroAlto(lista_colores_degradado)
        CartaLocalizacion(
            lugar = "Explora a tu manera",
            localida = "Geinz", false
        )

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla4(    pagerState: PagerState,
                  page: Int,
                  lista_colores_degradado: List<Color>,
                  onNext: () -> Unit) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    val lista_sub_pantallas = constantes_lista_localidades.fracespantalla1
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == page) {
            while (true) {
                // Zoom hacia 1.15f
                scaleAnim.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Zoom de regreso a 1f
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Cambia la imagen solo cuando vuelve a su tamaño normal
                currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
            }
        } else {
            scaleAnim.snapTo(1f)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) with fadeOut(animationSpec = tween(1500))
            }
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(lista_sub_pantallas[index].img)
                    .crossfade(false)
                    .build(),
                contentDescription = null,

                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    spacer_vertical(20.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(IntrinsicSize.Min)
                    ) {
                        Crossfade(
                            targetState = currentImageIndex,
                            animationSpec = tween(1500)
                        ) { index ->
                            texto_generico_multilinea(
                                lista_sub_pantallas[index].titulo.uppercase(),
                                MaterialTheme.typography.busquedaGeinzWork
                            )
                        }
                    }

                    spacer_vertical(15.dp)
                    Crossfade(
                        targetState = currentImageIndex,
                        animationSpec = tween(1500)
                    ) { index ->
                        texto_generico_multilinea(
                            lista_sub_pantallas[index].texto,
                            MaterialTheme.typography.bodyMedium
                        )
                    }


                }
                Box(Modifier.padding(end = 20.dp, start = 10.dp, bottom = 20.dp)) {
                    CelularAnimacion(
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        ), {
                            onNext()
                        }, orientation = Orientation.Horizontal
                    )

                }
            }

        }

        FondoOscuroAlto(lista_colores_degradado)
        CartaLocalizacion(
            lugar = "Explora a tu manera",
            localida = "Geinz", false
        )

    }
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla5(    pagerState: PagerState,
                  page: Int,
                  lista_colores_degradado: List<Color>,
                  onNext: () -> Unit) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    val lista_sub_pantallas = constantes_lista_localidades.fracespantalla1
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == page) {
            while (true) {
                // Zoom hacia 1.15f
                scaleAnim.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Zoom de regreso a 1f
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Cambia la imagen solo cuando vuelve a su tamaño normal
                currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
            }
        } else {
            scaleAnim.snapTo(1f)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) with fadeOut(animationSpec = tween(1500))
            }
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(lista_sub_pantallas[index].img)
                    .crossfade(false)
                    .build(),
                contentDescription = null,

                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    spacer_vertical(20.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(IntrinsicSize.Min)
                    ) {
                        Crossfade(
                            targetState = currentImageIndex,
                            animationSpec = tween(1500)
                        ) { index ->
                            texto_generico_multilinea(
                                lista_sub_pantallas[index].titulo.uppercase(),
                                MaterialTheme.typography.busquedaGeinzWork
                            )
                        }
                    }

                    spacer_vertical(15.dp)
                    Crossfade(
                        targetState = currentImageIndex,
                        animationSpec = tween(1500)
                    ) { index ->
                        texto_generico_multilinea(
                            lista_sub_pantallas[index].texto,
                            MaterialTheme.typography.bodyMedium
                        )
                    }


                }
                Box(Modifier.padding(end = 20.dp, start = 10.dp, bottom = 20.dp)) {
                    CelularAnimacion(
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        ), {
                            onNext()
                        }, orientation = Orientation.Horizontal
                    )

                }
            }

        }

        FondoOscuroAlto(lista_colores_degradado)
        CartaLocalizacion(
            lugar = "Explora a tu manera",
            localida = "Geinz", false
        )

    }
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla6(    pagerState: PagerState,
                  page: Int,
                  lista_colores_degradado: List<Color>,
                  onNext: () -> Unit) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    val lista_sub_pantallas = constantes_lista_localidades.fracespantalla1
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == page) {
            while (true) {
                // Zoom hacia 1.15f
                scaleAnim.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Zoom de regreso a 1f
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(2500, easing = LinearEasing)
                )
                // Cambia la imagen solo cuando vuelve a su tamaño normal
                currentImageIndex = (currentImageIndex + 1) % lista_sub_pantallas.size
            }
        } else {
            scaleAnim.snapTo(1f)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) with fadeOut(animationSpec = tween(1500))
            }
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(lista_sub_pantallas[index].img)
                    .crossfade(false)
                    .build(),
                contentDescription = null,

                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    spacer_vertical(20.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(IntrinsicSize.Min)
                    ) {
                        Crossfade(
                            targetState = currentImageIndex,
                            animationSpec = tween(1500)
                        ) { index ->
                            texto_generico_multilinea(
                                lista_sub_pantallas[index].titulo.uppercase(),
                                MaterialTheme.typography.busquedaGeinzWork
                            )
                        }
                    }

                    spacer_vertical(15.dp)
                    Crossfade(
                        targetState = currentImageIndex,
                        animationSpec = tween(1500)
                    ) { index ->
                        texto_generico_multilinea(
                            lista_sub_pantallas[index].texto,
                            MaterialTheme.typography.bodyMedium
                        )
                    }


                }
                Box(Modifier.padding(end = 20.dp, start = 10.dp, bottom = 20.dp)) {
                    CelularAnimacion(
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        ), {
                            onNext()
                        }, orientation = Orientation.Horizontal
                    )

                }
            }

        }

        FondoOscuroAlto(lista_colores_degradado)
        CartaLocalizacion(
            lugar = "Explora a tu manera",
            localida = "Geinz", false
        )

    }
}

@Composable
fun CameraZoomSobreImagen(scale: Float) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = R.drawable.f1,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

    }
}

@Composable
fun CelularAnimacion(
    modifier: Modifier = Modifier,
    onclick: () -> Unit,
    orientation: Orientation = Orientation.Vertical
) {
    val cellHeight = 60.dp
    val cellWidth = 40.dp
    val dotSize = 5.dp


    val maxOffset = if (orientation == Orientation.Vertical) 15f else 6f

    Box(
        modifier = modifier
            .height(cellHeight)
            .width(cellWidth)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onclick() }
            .background(Color.Transparent)
            .border(1.dp, color = Color.White, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = maxOffset,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    easing = LinearOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .then(
                    if (orientation == Orientation.Vertical) {
                        Modifier.offset(y = offset.dp)
                    } else {
                        Modifier.offset(x = offset.dp)
                    }
                )
                .background(Color.White, shape = CircleShape)
        )
    }
}


@Composable
fun galeria_img(
    lista_img: List<dataclass_onboarding>,
    imgSeleccionada: Int,
    img_selecionada: (Int) -> Unit
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(lista_img) { index, img ->
            carta_img_preview(img, index == imgSeleccionada) {
                img_selecionada(index)
            }
        }
    }
}

@Composable
fun carta_img_preview(
    img: dataclass_onboarding,
    isSelected: Boolean,
    img_selecionada: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(60.dp)
                .width(60.dp)
                .clip(RoundedCornerShape(10))
                .clickable {
                    img_selecionada()
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img.img)
                    .size(60, 60)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            this@Column.AnimatedVisibility(
                visible = !isSelected,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }
        }


        Text(
            text = img.nombre_localidad.uppercase(),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier
                .padding(top = 6.dp)
                .width(60.dp),
            textAlign = TextAlign.Center
        )
    }
}





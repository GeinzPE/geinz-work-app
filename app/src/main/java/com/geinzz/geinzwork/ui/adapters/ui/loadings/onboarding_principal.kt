package com.geinzz.geinzwork.ui.adapters.ui.loadings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.onboarding.dataclass_onboarding
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ImagenConInclinacion
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.fracespantalla11
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.fracespantalla12
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.fracespantalla13
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingPrincipal(onFinish: () -> Unit) {
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

                1 -> pantalla2(lista_colores_degradado_bottom) {
                    onFinish()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla1(
    lista_colores_degradado: List<Color>,
    onNext: () -> Unit
) {
    val lista_localidades = constantes_lista_localidades.lista_img_localidades_nombre
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }

    // ⏱️ Control automático de cambio de imagen (sin while true)
    LaunchedEffect(currentImageIndex) {
        delay(5000)
        currentImageIndex = (currentImageIndex + 1) % lista_localidades.size
    }

    // ✅ Pre-cargamos imágenes correctamente dentro de un @Composable remember
    val imagePainters = remember {
        // No se puede usar painterResource aquí porque no es composable
        // Así que devolvemos solo los IDs y los usamos dentro de AnimatedContent
        lista_localidades.map { it.img }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ✨ Animación de transición más rápida y fluida
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) with fadeOut(animationSpec = tween(600))
            },
            label = "fade"
        ) { index ->

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePainters[index])
                    .crossfade(true) // Transición más suave y rápida
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

        }

        // Fondo degradado (no debe reanimarse)
        fondo_osucro(lista_colocares = lista_colores_degradado)

        // Contenido textual
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            spacer_vertical(20.dp)
            Box(modifier = Modifier.fillMaxWidth(0.7f)) {
                texto_generico_multilinea(
                    "Bienvenido a Geinz".uppercase(),
                    MaterialTheme.typography.busquedaGeinzWork,
                    Color = Color.White
                )
            }
            spacer_vertical(15.dp)
            texto_generico_multilinea(
                "Explora Barranca, Supe, Puerto Supe, Pativilca y Paramonga. Descubre tiendas, lugares turísticos y eventos. Mantente al día con todo lo que sucede cerca de ti.",
                MaterialTheme.typography.bodyMedium,
                Color = Color.White
            )
            spacer_vertical(20.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    galeria_img(lista_localidades, currentImageIndex) { index ->
                        currentImageIndex = index
                    }
                }
                Box(Modifier.padding(horizontal = 20.dp)) {
                    CelularAnimacion(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onclick = onNext,
                        orientation = Orientation.Vertical
                    )
                }
            }
            spacer_vertical(30.dp)
        }

        CartaLocalizacion(
            lugar = lista_localidades[currentImageIndex].nombre_lugar,
            localida = lista_localidades[currentImageIndex].nombre_localidad,
            true
        )

        Box(
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_geinz_blanco),
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
            style = MaterialTheme.typography.titleSmall,
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
                    painter = rememberAsyncImagePainter(R.drawable.location_drawable),
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
fun pantalla2(lista_colores_degradaro: List<Color>, onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        pageSpacing = 0.dp,
        key = { it }
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (page) {
                0 -> pantalla3(lista_colores_degradaro) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 1,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = LinearEasing
                            )
                        )
                    }
                }

                1 -> pantalla4(lista_colores_degradaro) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 2,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = LinearEasing
                            )
                        )
                    }
                }

                2 -> pantalla5(lista_colores_degradaro) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = 3,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = LinearEasing
                            )
                        )
                    }
                }

                3 -> pantalla6 {
                    onFinish()
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla3(
    lista_colores_degradado: List<Color>,
    onNext: () -> Unit
) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top
    Box(modifier = Modifier.fillMaxSize()) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(fracespantalla11.img)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp)
        ) {
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
                        texto_generico_multilinea(
                            fracespantalla11.titulo.uppercase(),
                            MaterialTheme.typography.busquedaGeinzWork, Color = Color.White
                        )
                    }

                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        fracespantalla11.texto,
                        MaterialTheme.typography.bodyMedium, Color = Color.White
                    )
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
            lugar = "Tu camino más fácil",
            localida = "Geinz", false
        )

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla4(
    lista_colores_degradado: List<Color>,
    onNext: () -> Unit
) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    Box(modifier = Modifier.fillMaxSize()) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(fracespantalla12.img)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp)
        ) {
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
                        texto_generico_multilinea(
                            fracespantalla12.titulo.uppercase(),
                            MaterialTheme.typography.busquedaGeinzWork, Color = Color.White
                        )
                    }

                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        fracespantalla12.texto,
                        MaterialTheme.typography.bodyMedium, Color = Color.White
                    )

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
            lugar = "Explora tu zona",
            localida = "Geinz", false
        )

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla5(
    lista_colores_degradado: List<Color>,
    onNext: () -> Unit
) {
    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top

    Box(modifier = Modifier.fillMaxSize()) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(fracespantalla13.img)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        fondo_osucro(lista_colocares = lista_colores_degradado_top)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp)
        ) {
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
                        texto_generico_multilinea(
                            fracespantalla13.titulo.uppercase(),
                            MaterialTheme.typography.busquedaGeinzWork, Color = Color.White
                        )
                    }

                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        fracespantalla13.texto,
                        MaterialTheme.typography.bodyMedium, Color = Color.White
                    )

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
            lugar = "Rutas rápidas",
            localida = "Geinz", false
        )

    }

}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla6(
    onNext: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Escala animada (de 0.95 a 1.05, muy sutil)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,  // más pequeño al inicio
        targetValue = 1.1f,   // se expande un poco más
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800, // más lenta = más natural
                easing = FastOutSlowInEasing // respiración más orgánica
            ),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Box(
            modifier = Modifier
                .size(310.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8700F3).copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                    ),
                    shape = RoundedCornerShape(200.dp)
                )
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(R.drawable.logo_geinz_blanco),
                contentDescription = "logo",
                modifier = Modifier.size(60.dp)
            )

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
                    desplazamientoY = 20.dp,null,{},true
                )

                // --- Foto 2 (Centro, la protagonista) ---
                ImagenConInclinacion(
                    drawableResId = R.drawable.f5,
                    anguloRotacion = 3f,
                    desplazamientoX = 0.dp,
                    desplazamientoY = 0.dp,null,{},true
                )

                // --- Foto 3 (Derecha) ---
                ImagenConInclinacion(
                    drawableResId = R.drawable.f4,
                    anguloRotacion = 7f,
                    desplazamientoX = 70.dp,
                    desplazamientoY = 40.dp,null,{},true
                )
            }
            spacer_vertical(30.dp)
            Text(
                text = "Empezemos a explorar juntos",
                fontFamily = baners_geinz_work,
                fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.White
            )
            spacer_vertical(10.dp)
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        onNext()
                    },
                contentAlignment = Alignment.Center
            ) {
                texto_generico_one_line(
                    "Empezar",
                    MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 20.dp, horizontal = 30.dp)
                )

            }
        }



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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        itemsIndexed(
            items = lista_img,
            key = { _, item -> item.nombre_localidad } // Evita recomposiciones innecesarias
        ) { index, img ->
            carta_img_preview(
                img = img,
                isSelected = index == imgSeleccionada,
                img_selecionada = { img_selecionada(index) }
            )
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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.animateContentSize(animationSpec = tween(200))
    ) {
        // 🧠 Usamos AsyncImage con crossfade para mejor rendimiento
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10))
                .clickable { img_selecionada() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img.img)
                    .crossfade(true) // Transición más suave y rápida
                    .size(120) // optimiza caché interna
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 🎭 Sombra rápida en lugar de AnimatedVisibility (más fluido)
            if (!isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }
        }

        // 🏷️ Texto optimizado
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



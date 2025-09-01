package com.geinzz.geinzwork.ui.adapters.ui.loadings

import android.R.attr.translationX
import android.text.Layout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.floatin_actionButton
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

@Composable
fun OnboardingPrincipal() {
    val pagerState = rememberPagerState(pageCount = { 2 }) // 2 pantallas
    val scope = rememberCoroutineScope()

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (page) {
                0 -> pantalla1 {
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

                1 -> pantalla2()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun pantalla1(onNext: () -> Unit) {
    val listaImg = constantes_lista_localidades.lista_img_local
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % listaImg.size
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
            Image(
                painter = painterResource(id = listaImg[index]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(4.dp),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .blur(40.dp)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.55f)
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        )
        Column( modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomStart)
            .padding(10.dp) ) {
            spacer_vertical(20.dp)
            Box(modifier = Modifier.fillMaxWidth(0.7f)){
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
            Box(modifier = Modifier.fillMaxWidth()){
                CelularAnimacion(modifier = Modifier.align(Alignment.BottomCenter)){
                    onNext()
                }
            }
            spacer_vertical(30.dp)
        }

    }
}

@Composable
fun pantalla2() {
    val pagerState = rememberPagerState(pageCount = { 4 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (page) {
                0 -> pantalla3()
                1 -> pantalla4()
                2 -> pantalla5()
                3 -> pantalla6()
            }
        }
    }
}


@Composable
fun pantalla3() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraZoomSobreImagen()

        // 🔹 Texto sobre la imagen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            texto_generico_multilinea("hola1")
        }
    }
}

@Composable
fun pantalla4() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraZoomSobreImagen()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            texto_generico_multilinea("hola2")
        }
    }
}

@Composable
fun pantalla5() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraZoomSobreImagen()

        // 🔹 Texto sobre la imagen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            texto_generico_multilinea("hola3")
        }
    }
}

@Composable
fun pantalla6() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraZoomSobreImagen()

        // 🔹 Texto sobre la imagen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            texto_generico_multilinea("hola4")
        }
    }
}


@Composable
fun CameraZoomSobreImagen() {
    val infiniteTransition = rememberInfiniteTransition()

    // 👇 escala animada (zoom in y zoom out)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,       // tamaño normal
        targetValue = 1.3f,      // hasta 30% de zoom
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing), // duración 10s
            repeatMode = RepeatMode.Reverse                  // zoom in -> zoom out
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.f4),
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
fun CelularAnimacion(modifier: Modifier = Modifier,onclick:()-> Unit) {
    val cellHeight = 60.dp
    val cellWidth = 40.dp
    val dotSize = 5.dp
    val maxOffset = 15f // ajusta para que no llegue al borde

    Box(
        modifier = modifier
            .height(cellHeight)
            .width(cellWidth)
            .clip(RoundedCornerShape(10.dp))
            .clickable{onclick()}
            .background(Color.Transparent)
            .border(1.dp, color = Color.White, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = maxOffset,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500, // más rápido y fluido
                    easing = LinearOutSlowInEasing // suaviza el movimiento
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = offsetY.dp)
                .background(Color.White, shape = CircleShape)
        )
    }
}





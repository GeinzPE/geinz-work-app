package com.geinzz.geinzwork.ui.adapters.ui.loadings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.fondo_oscuro5_s
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay


@Composable
fun pantalla_carga_login() {
    val iimg_random = constantes_lista_localidades.lista_img_carga.random()
    val lista_fraces_ramdo = constantes_lista_localidades.frasesCarga.random()

    val lista_colores_degradado_top = constantes_lista_localidades.lista_color_degradado_top
    val lista_colores_degradado_bottom = constantes_lista_localidades.lista_color_degradado_bottom
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = fondo_oscuro5_s,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = fondo_oscuro5_s,
            darkIcons = false
        )
    }
    Scaffold { innerPading ->
        Box(modifier = Modifier.padding(innerPading)) {
            fondo_img(iimg_random.img)
            fondo_osucro(lista_colocares = lista_colores_degradado_top)
            Box(modifier = Modifier.align(Alignment.BottomStart)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    CargandoPalabra()
                    spacer_vertical(10.dp)
                    texto_generico_one_line(lista_fraces_ramdo)
                }
            }
            FondoOscuroAlto(lista_colores_degradado_bottom)
            CartaLocalizacion(
                lugar = iimg_random.nombre_lugar,
                localida = iimg_random.nombre_localidad, true
            )
        }
//    Column(modifier = Modifier.padding(innerPading).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
//        respiracion_logo_profesional()
//
//        texto_generico_one_line("espere un momento")
//    }
    }
}

@Composable
fun respiracion_logo_profesional() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo breathing")


    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )


    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Image(
        painter = painterResource(R.drawable.logo_geinz_500x500),
        contentDescription = "Logo Geinz",
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha,
                translationY = offsetY
            )
    )
}

@Composable
fun CargandoPalabra() {

    var puntos by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            puntos = (puntos + 1) % 4
        }
    }

    Text(
        text = "Cargando" + ".".repeat(puntos),
        style = MaterialTheme.typography.busquedaGeinzWork,
        color = Color.White
    )


}

@Composable
fun fondo_img(randomImg: Int) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(randomImg)
            .crossfade(false)
            .placeholder(R.drawable.cargando_img_categorias)
            .error(R.drawable.sin_item_carrito)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}


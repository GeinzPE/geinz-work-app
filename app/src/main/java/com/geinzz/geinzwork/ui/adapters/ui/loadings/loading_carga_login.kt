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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.fondo_oscuro5_s
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay


@Composable
fun pantalla_carga_login(inner_pading:Boolean) {
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
        Box(modifier = Modifier.padding(if(inner_pading)innerPading else PaddingValues(0.dp))) {
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

    }
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
        fontFamily = baners_geinz_work,
        fontSize = 35.sp,
        color = Color.White
    )


}

@Composable
fun fondo_img(randomImg: Int) {
    Image(
        painter = painterResource(randomImg),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}


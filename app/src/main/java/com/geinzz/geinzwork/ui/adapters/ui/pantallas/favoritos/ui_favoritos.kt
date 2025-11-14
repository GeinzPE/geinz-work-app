package com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos


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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ImagenConInclinacion
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.principal.AutoResizeOneLineText
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.categorias_defaul
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.viewModel_favoritos
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun iu_favoritos(
    viewmodelFavoritos: viewModel_favoritos,
    datos_principales_user: datos_principales_user
) {

    val lista_fb_size by viewmodelFavoritos.lista_fv.collectAsState()
    var imagenActiva by remember { mutableStateOf<Int?>(null) }

    val chipEstaActivo = imagenActiva != null

    val animatedChipColor by animateColorAsState(
        targetValue = if (!chipEstaActivo) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (!chipEstaActivo) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300)
    )
    val listaImg = listOf(
        R.drawable.f1,
        R.drawable.f2,
        R.drawable.f4
    )
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
            ChipsCategorias(categorias_defaul, animatedChipColor, animatedTextColor, imagenActiva)
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
fun ChipsCategorias(categorias: List<String>, color: Color, color_txt: Color, imagenActiva: Int?) {

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
                        ChipCategoria(titulo = simplificarCategoria(categoria), color, color_txt)
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = imagenActiva == null,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)), modifier = Modifier.align(Alignment.CenterStart)
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
            exit = fadeOut(animationSpec = tween(500)), modifier = Modifier.align(Alignment.CenterEnd)
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
fun ChipCategoria(titulo: String, color: Color, color_txt: Color) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 14.sp,
            color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )

    }
}


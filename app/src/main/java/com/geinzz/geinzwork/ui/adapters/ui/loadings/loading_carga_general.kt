package com.geinzz.geinzwork.ui.adapters.ui.loadings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun cargando_categorias(
    composision: LottieComposition?,
    value: String,
    PB: Dp,
    lista_fraces: List<String>
) {
    var fraseIndex by remember { mutableStateOf(0) }
    var frases by remember { mutableStateOf(lista_fraces) }
    var fraseActual by remember { mutableStateOf(frases[0]) }

    LaunchedEffect(value) {
        fraseIndex = 0
        while (true) {
            fraseActual = frases[fraseIndex]
            fraseIndex = (fraseIndex + 1) % frases.size
            delay(2500L)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (texto_centrado, loti_animation) = createRefs()
            AnimatedContent(
                targetState = fraseActual,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "frase_animada"
            ) { texto ->
                Text(
                    text = texto,
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 30.dp)
                        .constrainAs(texto_centrado) {
                            top.linkTo(loti_animation.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }

            LottieAnimation(
                composition = composision,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.padding(top =PB)
                    .size(400.dp)
                    .constrainAs(loti_animation) {}
            )
        }
    }
}
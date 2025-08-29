package com.geinzz.geinzwork.ui.adapters.ui.loadings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line


@Composable
fun pantalla_carga_login() {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        respiracion_logo()

        texto_generico_one_line("espere un momento")
    }
}

@Composable
fun respiracion_logo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Image(
        painter = painterResource(R.drawable.logo_geinz_500x500),
        contentDescription = "",
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
    )
}
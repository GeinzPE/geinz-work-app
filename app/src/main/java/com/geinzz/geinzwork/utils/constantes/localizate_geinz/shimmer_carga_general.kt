package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.rememberShimmer

object shimmer_carga_general {
    @Composable
    fun shimmer(): Shimmer {
        return rememberShimmer(
            shimmerBounds = ShimmerBounds.View,
            theme = ShimmerTheme(
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1300, easing = LinearEasing)
                ),
                shaderColors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ),
                shaderColorStops = listOf(0.0f, 0.5f, 1.0f),
                shimmerWidth = 200.dp,
                rotation = 20f,
                blendMode = BlendMode.SrcOver
            )
        )
    }
}
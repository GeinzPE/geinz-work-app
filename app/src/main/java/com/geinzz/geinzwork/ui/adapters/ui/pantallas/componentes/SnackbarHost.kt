package com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier) {

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->

        // Solo movimiento vertical hacia ABAJO
        var offsetY by remember { mutableStateOf(0f) }


        val animatedOffsetY by animateFloatAsState(
            targetValue = offsetY,
            label = "snackbar_offset_y"
        )

        val threshold = 80f

        LaunchedEffect(offsetY) {
            if (offsetY > threshold) {
                hostState.currentSnackbarData?.dismiss()
            }
        }

        Snackbar(
            snackbarData = data,
            modifier = Modifier
                .offset { IntOffset(0, animatedOffsetY.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        // Solo permitir arrastrar HACIA ABAJO
                        val newOffset = offsetY + dragAmount.y

                        if (newOffset > 0) { // solo hacia abajo
                            offsetY = newOffset
                        }
                    }
                }
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(50.dp)),
            shape = RoundedCornerShape(50.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        )
    }
}

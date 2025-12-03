package com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
        modifier = modifier.padding(bottom = 20.dp)
    ) { data ->

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

        // Reinicia offset cuando aparece un nuevo snackbar
        LaunchedEffect(data) {
            offsetY = 0f
        }

        AnimatedVisibility(
            visible = data != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {

            Snackbar(
                snackbarData = data,
                modifier = Modifier
                    .offset { IntOffset(0, animatedOffsetY.toInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetY + dragAmount.y
                            if (newOffset > 0) offsetY = newOffset
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
}


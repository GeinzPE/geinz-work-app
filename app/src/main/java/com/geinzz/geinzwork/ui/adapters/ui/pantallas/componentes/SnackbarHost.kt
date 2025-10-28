package com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SnackbarHost(SnackbarHostState: SnackbarHostState,modifier: Modifier){
    SnackbarHost(
        hostState = SnackbarHostState,
        // *** CLAVE: Alinea el Host al centro inferior del Box ***
        modifier = modifier
            .padding(16.dp)
    ) { data ->

        Snackbar(
            snackbarData = data,
            // 1. Define la forma redondeada
            shape = RoundedCornerShape(50), // Puedes ajustar el radio (ej. 4.dp, 12.dp)
            // 2. Opcional: Dale un fondo diferente
            containerColor = Color.White,
            // 3. Opcional: Ajusta el color del texto del mensaje
            contentColor = Color.Black,


            )
    }

}
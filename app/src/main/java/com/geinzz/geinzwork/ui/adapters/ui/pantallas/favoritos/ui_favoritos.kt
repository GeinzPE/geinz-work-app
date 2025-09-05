package com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line

@Composable
fun iu_favoritos() {
    Box(modifier = Modifier.fillMaxSize()) {
        texto_generico_one_line("favoritos", modifier = Modifier.align(Alignment.Center))
    }
}
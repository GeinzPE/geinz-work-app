package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label

@Composable
fun ui_pantalla_busqueda() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            texfiel_filtrado()
        }
    }

}

@Composable
fun chips_filtrado(){

}

@Composable
fun texfiel_filtrado() {
    OutlinedTextField(
        value = "",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = {},
        placeholder = { retornar_pleaceholder_label(" A dónde quieres llegar?") },
        label = { retornar_pleaceholder_label(" A dónde quieres llegar?") },
        leadingIcon = {
            Image(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "", colorFilter = ColorFilter.tint(Color.White)
            )
        }, shape = RoundedCornerShape(50)
    )
}


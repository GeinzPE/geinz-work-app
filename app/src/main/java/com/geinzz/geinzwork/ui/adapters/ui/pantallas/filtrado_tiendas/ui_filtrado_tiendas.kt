package com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import com.geinzz.geinzwork.R
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun chips_categorias() {
    val listaCaeogiars = listOf("chifa", "polleria", "pizzeria", "Hambugeseria")

    LazyColumn() {

        item {
            Text("Busca tus tiendas favoritas")
            Text("filtra por tus cateogiras y el nombre de tus tiendas favoritas")
        }
        item {
            LazyRow() {
                items(listaCaeogiars) { categorias ->
                    FilterChip(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        selected = true,
                        onClick = {
                        },
                        label = {
                            Text(text = categorias.toString())
                        },
                        shape = RoundedCornerShape(50)
                    )

                }
            }
        }

        item {
            OutlinedTextField(
                value = "",
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                onValueChange = {},
                label = { Text("Ingresa el nombre de la tienda") },
                placeholder = { Text("Ingresa el nomber") }
            )
        }

        item {
                encontradas_activas("Tiendas activas", "10", R.drawable.icon_tienda_icon_general)
            Spacer(modifier = Modifier.height(20.dp))
                encontradas_activas("Tiendas registradas", "20", R.drawable.icon_tiendas)

        }

    }

}

@Composable
fun encontradas_activas(texto1: String, texto2: String, @DrawableRes icono: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(painter = painterResource(icono), contentDescription = "", modifier = Modifier.size(20.dp))
        Text(texto1)
        Text(texto2)
    }
}
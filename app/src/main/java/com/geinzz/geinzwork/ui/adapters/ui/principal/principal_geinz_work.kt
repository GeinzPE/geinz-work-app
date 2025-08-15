package com.geinzz.geinzwork.ui.adapters.ui.principal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal() {
    val lista_localidades = constantes_lista_localidades.lista
    val localidadSeleccionada = rememberSaveable { mutableStateOf("") }

    Scaffold() { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(10.dp)
        ) {
            item {
                nombre_texto_img_perfil()
            }

                stickyHeader(){
                    ColumnContenedorComun {
                        texfiel_filtrado()
                        FiltradosChipsLocalidades(
                            lista_localidades,
                            localidadSeleccionada.value
                        ) { nuevaLocalidad -> }
                    }


                }

            item {
                apartado_turismo()
            }
            item {
                apartado_explora_cat()
            }
            item {
                rutas_turismo()
            }
            item { recomendado_por_vistitantes() }
        }
    }

}


@Composable
fun apartado_explora_cat() {
    spacer_vertical(10.dp)
    Column {
        texto_generico_one_line("Explora", MaterialTheme.typography.titleLarge)
        spacer_vertical(5.dp)
        cartas_turismo(5, 150.dp, 300.dp)
    }
}

@Composable
fun apartado_turismo() {
    spacer_vertical(10.dp)

    Column {
        texto_generico_one_line("Lugares turisticos", MaterialTheme.typography.titleLarge)
        spacer_vertical(5.dp)
        cartas_turismo(5, 230.dp, 200.dp)
    }
    spacer_vertical(10.dp)

}

@Composable
fun rutas_turismo() {
    spacer_vertical(10.dp)
    Box() {
        Image(
            painter = painterResource(R.drawable.cargar_foto_500x500),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(5)),
            contentDescription = "",
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5))
                .background(Color.Black.copy(alpha = 0.5f))
                .fillMaxWidth()
                .height(400.dp)
        ) {

        }

    }
}

@Composable
fun recomendado_por_vistitantes() {
    spacer_vertical(20.dp)
    texto_generico_one_line("Recomendado por vistitantes", MaterialTheme.typography.titleLarge)
    spacer_vertical(5.dp)
    Column {
        cartas_turismo(5, 205.dp, 320.dp)
    }

}


@Composable
fun cartas_turismo(rounder: Int, alto: Dp, ancho: Dp) {
    Box() {
        Image(
            painter = painterResource(R.drawable.cargar_foto_500x500),
            modifier = Modifier
                .width(ancho)
                .height(alto)
                .clip(RoundedCornerShape(rounder)),
            contentDescription = "",
        )
        mascara_img(rounder, alto, ancho)

    }
}


@Composable
fun mascara_img(rounder: Int, alto: Dp, ancho: Dp) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(rounder))
            .background(Color.Black.copy(alpha = 0.5f))
            .width(ancho)
            .height(alto)
    ) {

    }
}

@Composable
fun texfiel_filtrado() {
    spacer_vertical(10.dp)
    OutlinedTextField(
        value = "",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = {},
        placeholder = { retornar_pleaceholder_label(" A donde quieres llegar") },
        label = { retornar_pleaceholder_label(" A donde quieres llegar") },
        leadingIcon = {
            Image(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = ""
            )
        }, shape = RoundedCornerShape(50)
    )
    spacer_vertical(10.dp)

}


@ExperimentalMaterial3Api
@Composable
fun FiltradosChipsLocalidades(
    lista_localidades: List<dataclass_localidad_escudos>,
    localidadSeleccionada: String,
    onLocalidadSeleccionada: (String) -> Unit
) {
    LazyRow(modifier = Modifier.padding(top = 5.dp)) {
        items(lista_localidades) { localidad ->
            val isSelected =
                localidadSeleccionada.equals(localidad.nombre_localidad, ignoreCase = true)
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = null,
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onLocalidadSeleccionada(localidad.nombre_localidad.toString())
                    }
                },

                label = {
                    Text(
                        text = localidad.nombre_localidad.toString(),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                },
                trailingIcon = {
                    localidad.escudo_img?.let { imgResId ->
                        Image(
                            painter = painterResource(id = imgResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(40)

            )
        }
    }
}


//@Preview(showBackground = true)
@Composable
fun nombre_texto_img_perfil(nombre_user: String = "Benjamin lopez", img_url: String = "") {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            texto_generico_one_line(
                texto = "Hola $nombre_user",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            texto_generico_one_line(
                texto = "A donde quieres llegar ?",
                MaterialTheme.typography.headlineSmall
            )
        }
        Image(
            painter = painterResource(R.drawable.cargar_foto_500x500),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentDescription = ""
        )
//        AsyncImage(
//            model = "",
//            contentDescription = "Imagen de la tienda",
//            contentScale = ContentScale.Crop,
//            error = painterResource(R.drawable.qr_yape),
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp)
//                .clip(RoundedCornerShape(50.dp)),
//
//            )

    }


}
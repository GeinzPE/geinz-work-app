package com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.mascara_img
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.Estados_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.open_map_perzonlizado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import java.nio.file.WatchEvent


@Composable
fun pantalla_lugares_turisticos(
    localidad_selecionada: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos = viewModel(),
    viewModel_cordenadas: viewModel_principal_geinz_work = viewModel(),
    abrir_mapa: (String) -> Unit,
) {
    val _lugares_turisticos by viewModel_cordenadas._lugares_turisticos.observeAsState(emptyList())

    val filtrado_lugares_turisticos by viewmodel_lugares_turisticos._categorias_filtrados.observeAsState(
        emptyList()
    )
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var buttom_mapa by remember { mutableStateOf(false) }
    val lista_con_todos = listOf("Todos") + filtrado_lugares_turisticos
    var listaMostrar by remember { mutableStateOf<List<lugares_turisticos>>(emptyList()) }


    var lista_base_turisticos by remember { mutableStateOf(emptyList<lugares_turisticos>()) }

    val estado_fitlrado_datos = Estados_lugares_turisticos(
        subcategorias = filtrado_lugares_turisticos,
        lista_filtrada = _lugares_turisticos
    )

    LaunchedEffect(Unit) {
        viewModel_cordenadas.lugares_turisticos(localidad_selecionada)
        viewmodel_lugares_turisticos.obtener_categorias()
    }

    LaunchedEffect(_lugares_turisticos) {
        if (_lugares_turisticos.isNotEmpty()) {
            viewmodel_lugares_turisticos.todos_lugares(_lugares_turisticos)
            listaMostrar = if (subCategoriaSeleccionada == "Todos") {
                _lugares_turisticos
            } else {
                _lugares_turisticos.filter {
                    it.subcategoria_filtrado.contains(
                        subCategoriaSeleccionada
                    )
                }
            }
        }
    }

    // Cuando cambie la subcategoría
    LaunchedEffect(subCategoriaSeleccionada) {
        viewmodel_lugares_turisticos.filtrar_por_subcategoria(subCategoriaSeleccionada)
        // Actualizamos la variable local para LazyColumn
        listaMostrar = viewmodel_lugares_turisticos.listaFiltrada.value
    }


// Cuando cambies de chip
//    LaunchedEffect(subCategoriaSeleccionada) {
//        val lugares_filtrados=viewmodel_lugares_turisticos.filtrar_por_subcategoria(subCategoriaSeleccionada)
//        lista_base_turisticos=lugares_filtrados
//
//    }

//    LaunchedEffect(_lugares_turisticos) {
//
//    }
//
//    LaunchedEffect(subCategoriaSeleccionada) {

//        Log.d("lugares_fitlrado",lista_base_turisticos.toString())
//    }
//
//    LaunchedEffect(subCategoriaSeleccionada, _lugares_turisticos) {
//        viewmodel_lugares_turisticos.lista_filtrada_por_subcategoira(subCategoriaSeleccionada, _lugares_turisticos)
//    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { texto_generico_one_line("Lugares turisticos de $localidad_selecionada") }
            item {
                texto_generico_multilinea("descriocion de los lugares")
            }
            item {
                LazyRow() {
                    items(lista_con_todos) { subcategorias ->
                        val selecionado = subCategoriaSeleccionada == subcategorias
                        FilterChip(
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = Color.White,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp),
                            selected = selecionado,
                            border = if (selecionado) null else BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground
                            ),
                            onClick = {
                                if (!selecionado) {
                                    if (subcategorias == "Todos") {
                                        subCategoriaSeleccionada = "Todos"
                                        buttom_mapa = false
                                    } else {
                                        buttom_mapa = true
                                        subCategoriaSeleccionada = subcategorias
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = subcategorias,
                                    color = if (selecionado) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            },
                            shape = RoundedCornerShape(40)
                        )
                    }
                }
            }
            items(listaMostrar) { lugares ->
                carta_lugares_turisticosa(200.dp, 10, lugares)
            }
        }
        AnimatedVisibility(buttom_mapa) {
            open_map_perzonlizado(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),"turismo",
                abrir_mapa
            )
        }
    }

}

@Composable
fun carta_lugares_turisticosa(alto: Dp, rounder: Int, lugar: lugares_turisticos) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Box(
        modifier = Modifier
            .width(screenWidth)
            .height(alto)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(lugar.img_ref)
                .size(screenWidth.value.toInt(), alto.value.toInt())
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(screenWidth)
                .height(alto)
                .clip(RoundedCornerShape(rounder)),
//                .clickable {
//                    listener(true, lugar)
//                },
            contentScale = ContentScale.Crop
        )
        mascara_img(rounder, alto, screenWidth)
        texto_generico_one_line(
            lugar.titulo, MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        )
    }
}

package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda

import Item
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ShadowTagsCategoriassEnd
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ShadowTagsCategoriasstart
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ui_pantalla_busqueda(
    isvisble_buttomvar: Boolean,
    focusRequester: FocusRequester,
    ocultar: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val viewModel: SearchViewModel = viewModel()
    val results by viewModel.results.collectAsState()
    var subcategoria_selecionada by rememberSaveable { mutableStateOf("Todos") }
    val scope = rememberCoroutineScope()
    val lista_filtrado = constantes_lista_localidades.chips_filtrado_busqueda

    Log.d("categoira_Selecoad", subcategoria_selecionada)
    Box() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            item {
                fraces_filtrado()
                spacer_vertical(10.dp)
            }
            item {
                TexfielFiltrado(focusRequester, searchText) { it ->
                    searchText = it
                    if (it.isEmpty()) {
                        viewModel.clearResults()
                    } else {
                        scope.launch { viewModel.search(it) }
                    }
                }
            }
            item {
                filtrado_chips(lista_filtrado, subcategoria_selecionada) { filtrado_Select ->
                    subcategoria_selecionada = filtrado_Select
                }
                spacer_vertical(10.dp)
            }
            // Resultados como items
            items(results) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    carta_filtrado(item)
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(item.img)
//                        .size(200,200)
//                        .crossfade(true)
//                        .placeholder(R.drawable.cargando_img_categorias)
//                        .error(R.drawable.cargando_img_categorias)
//                        .build(),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .width(200.dp)
//                        .height(200.dp)
//                        .clip(RoundedCornerShape(12)),
//                    contentScale = ContentScale.Crop
//                )
//                Text(
//                    text = item.nombre,
//                    color = Color.White,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                )
                }
            }
        }
        val offsetY by animateDpAsState(
            targetValue = if (isvisble_buttomvar) (-10).dp else 0.dp, // súbelo cuando la BottomBar esté
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            ),
            label = "buttonOffset"
        )

        Button(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = offsetY), // animación independiente
            onClick = { ocultar() }
        ) {
            texto_generico_multilinea("ocultar")
        }

    }

}

@Composable
fun fraces_filtrado() {
    val fraces = constantes_lista_localidades.lista_frases_busqueda
    var index by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }

    Crossfade(fraces[index], label = "fraces") { txt ->
        texto_generico_one_line(
            texto = txt,
            MaterialTheme.typography.busquedaGeinzWork
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun filtrado_chips(
    lista_filtrado: List<String>,
    subcategoria_selecionada: String,
    cateogira_selecionada: (String) -> Unit
) {
    LazyRow {
        items(lista_filtrado) { item ->
            val cat_selecionada = subcategoria_selecionada == item
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                selected = cat_selecionada,
                onClick = { cateogira_selecionada(item) },
                label = {
                    Text(
                        text = item,
                    )
                },
                shape = RoundedCornerShape(40)
            )

        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun TexfielFiltrado(focusRequester: FocusRequester, texto: String, onvalueChage: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = texto,
        onValueChange = { newText ->
            onvalueChage(newText)
        },

        placeholder = {
            Text(
                text = "A dónde quieres llegar?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        },
        label = {
            texto_generico_one_line(
                "A dónde quieres llegar?",
                MaterialTheme.typography.bodyMedium
            )
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        shape = RoundedCornerShape(50)
    )

    Spacer(modifier = Modifier.height(8.dp))
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
fun carta_filtrado(item: Item) {
    Row() {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.img)
                .size(100, 100)
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(12)),
            contentScale = ContentScale.Crop
        )
        spacer_horizonta(5.dp)
        Column {
            texto_generico_one_line(item.nombre.uppercase(), MaterialTheme.typography.titleLarge)
            spacer_vertical(5.dp)
            texto_generico_one_line(
                "Localidad : ${item.lugar}",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(5.dp)
            texto_generico_one_line(
                "categoria : ${item.categoria}",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(5.dp)
            Box(
                modifier = Modifier
                    .height(25.dp)
                    .zIndex(0f)
                    .padding(end = 30.dp)
            ) {
                tags_subcateogiras(item.lista)
                if (item.lista.size > 3) {
                    ShadowTagsCategoriasstart(
                        Modifier
                            .align(Alignment.BottomStart)
                            .zIndex(1f)
                    )
                    ShadowTagsCategoriassEnd(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .zIndex(1f)
                    )
                }
            }
        }
    }

}



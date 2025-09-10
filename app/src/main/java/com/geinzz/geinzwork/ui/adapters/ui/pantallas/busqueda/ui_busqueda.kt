package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda


import Item
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ShadowTagsCategoriassEnd
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ShadowTagsCategoriasstart
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.SearchViewModel
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ui_pantalla_busqueda(
    viewModelFiltros: viewModel_filtado_tiendas,
    focusRequester: FocusRequester,
    mostrar: () -> Unit,
    ocultar: () -> Unit
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val viewModel: SearchViewModel = viewModel()
    val results by viewModel.results.collectAsState()
    var subcategoria_selecionada by rememberSaveable { mutableStateOf("Todos") }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val scope = rememberCoroutineScope()
    val lista_filtrado = constantes_lista_localidades.chips_filtrado_busqueda
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val horario_por_tienda by viewModelFiltros.estadoTiendas.observeAsState()
    var mosatar_autcompletado by remember { mutableStateOf(false) }
    var localidad_Selecionadad_filtrado by remember { mutableStateOf("") }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var tienda_localida_selecioanda by remember { mutableStateOf("") }
    var estadoColor by remember { mutableStateOf(Color.Gray) }
    var id_tienda_selecionada by remember { mutableStateOf("") }

    var firstLaunch by remember { mutableStateOf(true) }




    LaunchedEffect(subcategoria_selecionada) {
        if (firstLaunch) {
            firstLaunch = false
        } else {
            Log.d("cambiado", subcategoria_selecionada)
            scope.launch { viewModel.search(searchText.text, subcategoria_selecionada,tienda_localida_selecioanda) }
        }
    }

    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                tienda_localida_selecioanda,
                id_tienda_selecionada
            )

        }
    }

    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }

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
                    searchText = TextFieldValue(
                        text = it,
                        selection = TextRange(it.length) // 👈 cursor al final del texto
                    )
                    if (it.isNotEmpty()) {
                        when {
                            it.startsWith("barr", ignoreCase = true) -> {
                                mosatar_autcompletado = true
                            }

                            it.startsWith("sup", ignoreCase = true) -> {
                                mosatar_autcompletado = true
                            }

                            it.startsWith("pati", ignoreCase = true) -> {
                                mosatar_autcompletado = true
                            }

                            it.startsWith("puer", ignoreCase = true) -> {
                                mosatar_autcompletado = true
                            }

                            it.startsWith("param", ignoreCase = true) -> {
                                mosatar_autcompletado = true
                            }

                            else -> {
                                mosatar_autcompletado = false
                                localidad_Selecionadad_filtrado = ""
                            }
                        }
                        ocultar()
                        scope.launch {
                            viewModel.search(
                                it,
                                subcategoria_selecionada,
                                tienda_localida_selecioanda
                            )
                        }
                    } else {
                        mosatar_autcompletado = false
                        mostrar()
                        viewModel.clearResults()
                    }
                }
                spacer_vertical(5.dp)
            }
//            item {
//                AnimatedVisibility(
//                    visible = mosatar_autcompletado,
//                    enter = slideInVertically { fullHeight -> -fullHeight } + fadeIn(),
//                    exit = slideOutVertically { fullHeight -> -fullHeight } + fadeOut()
//                ) {
//                    autcomplet_localidad(localidad_Selecionadad_filtrado) { cat_selecionado ->
//                        localidad_Selecionadad_filtrado = cat_selecionado
//                        searchText = TextFieldValue(
//                            text = cat_selecionado,
//                            selection = TextRange(cat_selecionado.length)
//                        )
//                        scope.launch { viewModel.search(cat_selecionado, subcategoria_selecionada) }
//
//                    }
//                }
//                spacer_vertical(5.dp)
//
//            }
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
                    carta_filtrado(horario_por_tienda, item) { id, localidad, color ->
                        Log.d("elcolores", color.toString())
                        estadoColor = color
                        tienda_localida_selecioanda = localidad
                        id_tienda_selecionada = id
                        viewModelFiltros.obtenerHorarioPorTienda_activa(localidad, id)
                        show_bottom_sheeet = true
                    }
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
        if (show_bottom_sheeet) {
            bottom_sheet_tiendas_filtradas(
                estadoColor,
                viewModelFiltros,
                dataclass_tienda_seleccionada, show_bottom_sheeet
            ) {
                show_bottom_sheeet = false
            }
        }
        FloatingMenuFadeDemo(tienda_localida_selecioanda) { localidad ->
            tienda_localida_selecioanda = localidad
            scope.launch {
                viewModel.search(
                    query = searchText.text, // puede estar vacío
                    subcategoria_selecionada = subcategoria_selecionada,
                    localidad = localidad
                )
            }
        }

    }
}
//
//@Composable
//fun autcomplet_localidad(localidad_selecionadad: String, cateogira_selecionada: (String) -> Unit) {
//    LazyRow {
//        items(constantes_lista_localidades.lista_localidad) { it ->
//            val cat_selecionada = localidad_selecionadad == it
//            FilterChip(
//                modifier = Modifier.padding(horizontal = 4.dp),
//                colors = FilterChipDefaults.filterChipColors(
//                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                    selectedLabelColor = Color.White,
//                    containerColor = MaterialTheme.colorScheme.surface
//                ), selected = cat_selecionada,
//                onClick = { cateogira_selecionada(it) }, label = {
//                    Text(
//                        text = it,
//                    )
//                },
//                shape = RoundedCornerShape(40)
//            )
//
//        }
//    }
//}

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


@Composable
fun TexfielFiltrado(
    focusRequester: FocusRequester,
    texto: TextFieldValue,
    onvalueChage: (String) -> Unit
) {
    var icono_borrar by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = texto,
        onValueChange = { newValue: TextFieldValue ->
            icono_borrar = newValue.text.isNotBlank()
            onvalueChage(newValue.text)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "buscar",
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (icono_borrar) {
                IconButton(onClick = {
                    onvalueChage("") // 👈 limpiar texto
                    icono_borrar = false
                }) {
                    Icon(
                        painter = painterResource(R.drawable.vector_eliminar_texto_texfiel),
                        contentDescription = "borrar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "A dónde quieres llegar?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
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
fun carta_filtrado(
    estado_tienda: Map<String, Boolean>?,
    item: Item,
    listener: (String, String, Color) -> Unit
) {

    val iconCategoria = constantes_lista_localidades.getCategoriaIcon(item.categoria)
    val estado_tienda_filter = estado_tienda?.get(item.id_tienda) == true
    Log.d("estado_tienda", estado_tienda_filter.toString())
    var Estado_color = if (estado_tienda_filter) Color.Green else Color.Red
    Row(modifier = Modifier.clickable { listener(item.id_tienda, item.lugar, Estado_color) }) {

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
        Column() {
            texto_generico_one_line(item.nombre.uppercase(), MaterialTheme.typography.titleLarge)
            spacer_vertical(5.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.localidad_icon_general),
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 5.dp)
                )
                texto_generico_one_line(
                    item.lugar,
                    MaterialTheme.typography.bodyMedium
                )
            }
            spacer_vertical(5.dp)
            texto_generico_one_line(
                "$iconCategoria ${item.categoria}",
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

@Composable
fun FloatingMenuFadeDemo(localidad_selecionada: String, localidad_filtrado: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val lista_localidades = constantes_lista_localidades.lista_localidad

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        indication = null, // 🚫 sin ripple
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        expanded = false
                    }
            )
        }

        // 📌 Botones secundarios con fade + scale
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        ) {
            lista_localidades.forEach { i ->

                val color_selecionado = if (localidad_selecionada == i) {
                    Color.Black
                } else {
                    Color.Blue
                }

                AnimatedFabItem(
                    i,
                    color_selecionado,
                    expanded
                ) { localidad_filtrado(i)
                    expanded=false}
            }

        }

        val cornerRadius by animateDpAsState(
            targetValue = if (expanded) 12.dp else 50.dp,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            label = "cornerRadius"
        )
        val icono = if (expanded) {
            R.drawable.cerrar_selecion_x_vector
        } else {
            R.drawable.icono_filtrado_webp
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            interactionSource = remember { MutableInteractionSource() },
        ) {
            Crossfade(
                targetState = icono,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "crossfadeIcon"
            ) { currentIcon ->
                Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(currentIcon),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }


    }
}

@Composable
fun AnimatedFabItem(
    text: String,
    color: Color,
    visible: Boolean,
    onClick: () -> Unit
) {
    // Animación suave del color
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 500), // duración de la transición
        label = "buttonColorAnim"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
    ) {
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(containerColor = animatedColor)
        ) {
            Text(
                text,
                color = Color.White
            )
        }
    }
}




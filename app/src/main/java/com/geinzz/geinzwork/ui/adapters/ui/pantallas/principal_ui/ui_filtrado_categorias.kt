package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_resultado_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ShadowBottomPantallas
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.existencia_dato
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.floatin_actionButton
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.cargando_categorias
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.localizate_geinz.normalizarTexto
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.mapNotNull
import kotlin.collections.orEmpty
import kotlin.text.contains

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExplorarTiendas(
    localidadUser: String,
    nombreUser: String,
    viewModel:viewModel_localizate_geinz,
    clik_img: (categoria: String, localidad: String, nombre_user: String) -> Unit
) {
    val viewModel: viewModel_localizate_geinz = viewModel()
    Log.d("viewmode",viewModel.toString())
    val lista = remember { mutableStateListOf<encontradas_por_categoria>() }
    var texto_filtrado by rememberSaveable { mutableStateOf("") }
    val composision by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cargando_categorias))
    val lista_filtrada =
        remember { mutableStateListOf<encontradas_por_categoria>().apply { addAll(lista) } }
    val cargando = remember { mutableStateOf(true) }
    val encontrados_activos_tiendass by viewModel.encontrados_activos_tiendas.observeAsState()
    val lista_localidades = constantes_lista_localidades.lista
    val cartaExpandida = remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    var mostrar_fab by remember { mutableStateOf(false) }
    mostrar_fab = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 10
    val coroutineScope = rememberCoroutineScope()
    val localidadSeleccionada = rememberSaveable { mutableStateOf("") }

    val primeraVez = remember { mutableStateOf(true) }
    var fraces_localidad by remember { mutableStateOf(listOf("Espere un momento...")) }

    // Primera carga
    LaunchedEffect(Unit) {
        if (localidadSeleccionada.value.isEmpty()) {
            localidadSeleccionada.value = localidadUser
            fraces_localidad = viewModel.obtenerFrasesCarga(localidadUser, nombreUser)
        }
    }

    // Cambio de localidad
    LaunchedEffect(localidadSeleccionada.value) {
        if (primeraVez.value) {
            primeraVez.value = false
        } else {
            cargando.value = true
            cartaExpandida.value = null
            fraces_localidad = viewModel.obtenerFrasesCarga(localidadSeleccionada.value, nombreUser)
            mostrar_fab = false
        }
    }

    LaunchedEffect(encontrados_activos_tiendass) {
        encontrados_activos_tiendass?.let { listaNueva ->
            lista.clear()
            lista.addAll(listaNueva)
            texto_filtrado = ""
            cargando.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = cargando.value) { isCargando ->
            val listaParaMostrar =
                if (texto_filtrado.length > 2) lista_filtrada else lista
            if (isCargando && fraces_localidad.isNotEmpty()) {
                cargando_categorias(
                    composision,
                    localidadSeleccionada.value, // ✅ ahora usa la seleccionada
                    5.dp,
                    fraces_localidad
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Column {
                            cabezero_activity(localidadSeleccionada.value)
                            FiltradosChipsLocalidades(
                                lista_localidades,
                                localidadSeleccionada.value
                            ) { nuevaLocalidad ->
                                localidadSeleccionada.value = nuevaLocalidad
                            }
                            filtrado_texto(
                                viewModel,
                                texto_filtrado,
                                lista,
                                { texto_filtrado = it },
                                { nuevaLista, _ ->
                                    lista_filtrada.clear()
                                    lista_filtrada.addAll(nuevaLista)
                                    if (texto_filtrado.length > 2) cartaExpandida.value = null
                                }
                            )


                        }
                    }

                    itemsIndexed(listaParaMostrar, key = { _, item -> item.categoria?:""}) { index, item ->
                        cartas_categorias(
                            nombreUser,
                            item,
                            index,
                            localidadSeleccionada.value,
                            {categoria,localidad, nombre ->
                                clik_img(categoria,localidad,nombre)
                            },
                        )
                    }
                }

            }
        }

        ShadowBottomPantallas(listState, modifier = Modifier.align(Alignment.BottomCenter))
        AnimatedVisibility(
            mostrar_fab, enter = fadeIn(), exit = fadeOut(), modifier = Modifier
                .align(
                    Alignment.BottomStart
                )
                .padding(10.dp)
        ) {
            floatin_actionButton(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                R.drawable.arrow_subir_vector,
                null
            ) {
                coroutineScope.launch {
                    listState.animateScrollToItem(index = 0)
                }
            }
        }
    }


}


@Composable
fun cabezero_activity(localidad_registrado: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp),
    ) {
        titulos_genericos_one_line(
            "Ubicate $localidad_registrado", MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 10.dp),
        )
        spacer_vertical(5.dp)
        texto_generico_multilinea(
            "Explora las diferentes categorías de tiendas registradas en Geinz Work y ubícate fácilmente en $localidad_registrado",
            MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 10.dp)
        )

    }
}

@ExperimentalMaterial3Api
@Composable
fun FiltradosChipsLocalidades(
    lista_localidades: List<dataclass_localidad_escudos>,
    localidadSeleccionada: String,
    onLocalidadSeleccionada: (String) -> Unit
) {

    LazyRow(
        modifier = Modifier.padding(top = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(lista_localidades) { localidad ->
            val isSelected =
                localidadSeleccionada.equals(localidad.nombre_localidad, ignoreCase = true)
            val color_chips by animateColorAsState(
                targetValue = if (!isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.White,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = LinearOutSlowInEasing
                ), label = ""
            )
            val color_text = if (!isSelected) Color.White else Color.Black
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color_chips)
                    .height(45.dp)
                    .padding(horizontal = 15.dp, vertical = 10.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        if (!isSelected) {
                            onLocalidadSeleccionada(localidad.nombre_localidad.toString())
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                texto_generico_one_line(
                    localidad.nombre_localidad.toString().capitalizeFirst(),
                    color = color_text, style = MaterialTheme.typography.bodyMedium
                )
                spacer_horizonta(5.dp)
                localidad.escudo_img?.let { imgResId ->
                    Image(
                        painter = painterResource(id = imgResId),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

        }
    }
}


@Composable
fun filtrado_texto(
    viewModel: viewModel_localizate_geinz,
    texto: String,
    lista_cargada_filstrado: List<encontradas_por_categoria>,
    texto_filtrado: (String) -> Unit,
    busquedaAction: (List<encontradas_por_categoria>, Boolean) -> Unit
) {
    var lastInputTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    var is_error by remember { mutableStateOf(false) }
    var icono_busqeuda by remember { mutableStateOf(R.drawable.buscar_icon) }
    val sugerencias: List<dataclass_resultado_filtrado> = lista_cargada_filstrado
        .flatMap { catSub ->
            catSub.subcateogiras.orEmpty().mapNotNull { subcat ->
                if (subcat.contains(texto, ignoreCase = true) && texto.isNotBlank()) {
                    dataclass_resultado_filtrado(catSub.categoria.toString(), subcat)
                } else null
            }
        }
    is_error = expanded && sugerencias.isEmpty()
    LaunchedEffect(lastInputTime) {
        delay(2000L)
        val tiempoInactivo = System.currentTimeMillis() - lastInputTime
        if (tiempoInactivo >= 5000L) {
            focusManager.clearFocus()
            expanded = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            custom_texFiel(
                value = texto,
                onValueChange = {
                    texto_filtrado(it)
                    expanded = it.isNotBlank()
                    lastInputTime = System.currentTimeMillis()
                    if (it.isNotBlank()) {
                        icono_busqeuda = R.drawable.vector_eliminar_texto_texfiel
                        busquedaAction(
                            viewModel.obtenerResultados(it, lista_cargada_filstrado),
                            true
                        )
                    } else {
                        icono_busqeuda = R.drawable.buscar_icon
                    }
                },
                labelText = "Ingrese la subcategoría a buscar",
                placeholderText = "Ingrese la subcategoría a buscar",
                trailingIcon = {
                    if (icono_busqeuda == R.drawable.vector_eliminar_texto_texfiel) {
                        IconButton(onClick = {
                            texto_filtrado("")
                            busquedaAction(emptyList(), false)
                            expanded = false
                            icono_busqeuda = R.drawable.buscar_icon
                        }) {
                            Icon(
                                painter = painterResource(id = icono_busqeuda),
                                contentDescription = "Eliminar texto"
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = icono_busqeuda),
                            contentDescription = "Buscar por subcategoría"
                        )
                    }
                },
                isError = is_error,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        busquedaAction(
                            viewModel.obtenerResultados(texto, lista_cargada_filstrado),
                            true
                        )
                        expanded = false
                        focusManager.clearFocus()
                    }
                ),
            )
            if (is_error) {
                existencia_dato()
            }
        }


    }
}

@Composable
fun cartas_categorias(
    nombre_user: String,
    item: encontradas_por_categoria,
    index: Int,
    Localidad_selecionada: String,
    clik_img: (categoria: String, localidad: String, nombre: String) -> Unit
) {
    val categoriaKey = item.categoria?.let { normalizarTexto(it) } ?: return
    val heightOptions = listOf(300.dp, 350.dp)
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]
    val gradient = remember {
        Brush.verticalGradient(
            colors  = listOf(
                 Color.Transparent,
                Color.Black.copy(alpha = 0.55f),
                 Color.Black.copy(alpha = 1f)
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .padding(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.7f)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.img_subcategorias)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Imagen",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth() .clickable {
                        clik_img(item.categoria, Localidad_selecionada, nombre_user)
                    },
                    loading = {
                        // Opcional: shimmer o progress mientras carga real
                    },
                    error = {
                        Image(
                            painter = painterResource(R.drawable.cargando_img_categorias),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize() // 👈 ocupa todo el contenedor
                        )                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            gradient
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = item.categoria.capitalizeFirst(),
                        modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(modifier = Modifier.padding(5.dp)) {
                        tags_subcateogiras(
                            item.subcateogiras,
                            brush_start = Brush.horizontalGradient(colors = shadow_left),
                            brush_end = Brush.horizontalGradient(colors = shadow_right)
                        )
                    }
                }
            }
        }
    }
}
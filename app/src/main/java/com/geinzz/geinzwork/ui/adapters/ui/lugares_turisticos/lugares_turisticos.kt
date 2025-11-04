package com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.strat_subcategoria_shadow
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado


@Composable
fun pantalla_lugares_turisticos(
    viewmodelMapa: viewmodel_mapa_personalizado,
    localidad_selecionada: String,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos,
    abrir_mapa: (String) -> Unit,
    crear_cuenta: () -> Unit,
    navigation_regresar:()-> Unit,
    iniciar_seccion: () -> Unit
) {
    val _lugares_turisticos by viewmodel_lugares_turisticos._lugares_turisticos.observeAsState(
        emptyList()
    )
    val _lista_completa_lugares_turisticos by viewmodel_lugares_turisticos._lista_completa_lugares_turisticos.collectAsState()
    val state_lugares_turisticos by viewmodel_lugares_turisticos.stata_lugares_turisticos.collectAsState()
    val mostra_pantalla_carga by viewmodel_lugares_turisticos.mostrar_carga_turistico.collectAsState()
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var buttom_mapa by remember { mutableStateOf(false) }
    var Box_mostrar_vacio by remember { mutableStateOf(false) }
    var texto_vacio_error by remember { mutableStateOf("") }
    val context=LocalContext.current

    BackHandler {
        navigation_regresar()
    }

LaunchedEffect(mostra_pantalla_carga) {
    Log.d("mostra_pantalla_carga" ,"${mostra_pantalla_carga.toString()}")
}

    LaunchedEffect(Unit) {
        viewmodel_lugares_turisticos.resetearEstado()
        viewmodel_lugares_turisticos.lugares_turisticos(localidad_selecionada,context)
    }

    LaunchedEffect(subCategoriaSeleccionada) {
        if (_lista_completa_lugares_turisticos.isNotEmpty()) {
            viewmodel_lugares_turisticos.filtrar_lugares_turisticos(subCategoriaSeleccionada)
        }
    }

    LaunchedEffect(_lugares_turisticos) {
        if (_lugares_turisticos.isNotEmpty()) {
            viewmodel_lugares_turisticos.todos_lugares(_lugares_turisticos)
        }
    }
    val listState = rememberLazyListState()

    val showLeftShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val showRightShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < total - 1
        }
    }

    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaLeft"
    )
    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400), label = "alphaRight"
    )
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    val state = state_lugares_turisticos

    var lista_categoria by remember { mutableStateOf(listOf<String>()) }

    var mostar_error by remember { mutableStateOf(false) }
    var mostrar_texto_error by remember { mutableStateOf("") }


    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
//                    texto_generico_multilinea(
//                        "Lugares en ${localidad_selecionada.capitalizeFirst()}",
//                        style = MaterialTheme.typography.banerGeinzWork,
//                        modifier = Modifier.padding(end = 20.dp)
//                    )
                    Text(text =   "Lugares en ${localidad_selecionada.capitalizeFirst()}", fontFamily = baners_geinz_work, fontSize = 30.sp)

                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        "Explora los lugares más emblemáticos y atractivos de $localidad_selecionada. Conoce su historia, horarios, recomendaciones y cómo llegar para disfrutar al máximo tu visita.",
                        MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(13.dp)
                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .fillMaxWidth()
                    ) {
                        LazyRow(
                            state = listState,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(lista_categoria) { subcategoria ->
                                val seleccionado = subCategoriaSeleccionada == subcategoria
                                chisp_filtrado_busqueda(
                                    carta_selecionada = seleccionado,
                                    filtrado = subcategoria.capitalizeFirst(),
                                    btn_visible = false,
                                    clik_card = {
                                        if (!seleccionado) {
                                            subCategoriaSeleccionada = subcategoria
                                            buttom_mapa = subcategoria != "Todos"
                                        }
                                    },
                                    onClick_delete = {}
                                )
                            }
                        }

                        // Sombras laterales
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(40.dp)
                                    .align(Alignment.CenterStart)
                                    .zIndex(1f)
                                    .alpha(alphaLeft)
                                    .background(Brush.horizontalGradient(colors = shadow_left))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(40.dp)
                                    .align(Alignment.CenterEnd)
                                    .zIndex(1f)
                                    .alpha(alphaRight)
                                    .background(Brush.horizontalGradient(colors = shadow_right))
                            )
                        }
                    }
                    spacer_vertical(10.dp)
                }
            }

            when (state) {
                viewModel_lugares_turisticos.carga_lugares_turisticos.loading -> {
                    Log.d("adadasda123123","loading")

                    mostar_error = false
                }

                is viewModel_lugares_turisticos.carga_lugares_turisticos.empty -> {
                    Log.d("adadasda123123","empty")
                    mostar_error = true
                    mostrar_texto_error = state.txt
                }

                is viewModel_lugares_turisticos.carga_lugares_turisticos.error -> {
                    Log.d("adadasda123123","error")

                    mostar_error = true
                    mostrar_texto_error = state.txt
                }

                is viewModel_lugares_turisticos.carga_lugares_turisticos.succes -> {
                    Log.d("adadasda123123","succes")

                    mostar_error = false
                    lista_categoria = listOf("Todos") + state.lista_categoria
                    itemsIndexed(state.lista_lugares) { index, item ->
                        carta_turismo(
                            mostra_pantalla_carga,
                            viewmodelMapa,
                            viewmodel_lugares_turisticos,
                            index,
                            item.img_principal,
                            item,
                            { tipo ->
                                abrir_mapa(tipo)
                            }, { crear_cuenta() }, { iniciar_seccion() })
                    }
                }

                viewModel_lugares_turisticos.carga_lugares_turisticos.idle -> {
                    Log.d("adadasda123123","idle")
                }
            }
        }
        if (mostra_pantalla_carga) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(5f),
                contentAlignment = Alignment.Center
            ) {
                pantalla_carga_login(false)
            }
        }
        if (mostar_error) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(    text = mostrar_texto_error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 30.dp))

            }
        }

        AnimatedVisibility(
            !mostra_pantalla_carga,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                        )
                    )
                    .graphicsLayer { alpha = alphaAnim }
            )
        }


    }
}

@Composable
fun carta_turismo(
    mostra_pantalla_carga: Boolean,
    viewmodelMap: viewmodel_mapa_personalizado,
    viewmodel_lugares_turisticos: viewModel_lugares_turisticos,
    index: Int,
    img: String,
    lugar: lugares_turisticos,
    abrir_mapa: (String) -> Unit,
    crear_cuenta: () -> Unit,
    iniciar_seccion: () -> Unit
) {
    val bottomSheetVisible by viewmodelMap.estadoBottomSheet.collectAsState()
    val lugarSeleccionado by viewmodelMap.objetoSeleccionado.collectAsState()

    val heightOptions = listOf(250.dp, 280.dp)
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.7f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(img)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            // Solo abre el sheet de este lugar
                            viewmodelMap.setObjetoSeleccionado(lugar)
                            viewmodelMap.setBottomSheetVisible(true)
                        },
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp) // Alto del degradado
                        .align(Alignment.BottomCenter) // Posiciona abajo
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF262626)
                                )
                            )
                        )
                )
            }
            Column (Modifier.padding(top = 10.dp, start = 5.dp, end = 5.dp)){
                texto_generico_one_line(lugar.titulo.capitalizeFirst())
                spacer_vertical(10.dp)
                tags_subcateogiras(
                    lugar.subcategoria_filtrado,
                    brush_start = Brush.horizontalGradient(colors = strat_subcategoria_shadow),
                    brush_end = Brush.horizontalGradient(colors = end_subcategoria_shadow),
                    modifier = Modifier.padding(end = 10.dp)
                )
                spacer_vertical(10.dp)

            }
        }
    }

    if ((bottomSheetVisible && lugarSeleccionado == lugar) && !mostra_pantalla_carga) {
        bottom_sheet_lugares_turisticos(
            viewmodelMap = viewmodelMap,
            viewmodel_lugares_turisticos = viewmodel_lugares_turisticos,
            datos = lugar,
            visible = true,
            onClose = {
                viewmodelMap.setBottomSheetVisible(false)
            },
            ver_mapa = {
                abrir_mapa("turismo")
            }, iniciar_seccion = { iniciar_seccion() }, crear_cuenta = { crear_cuenta() }
        )
    }
}



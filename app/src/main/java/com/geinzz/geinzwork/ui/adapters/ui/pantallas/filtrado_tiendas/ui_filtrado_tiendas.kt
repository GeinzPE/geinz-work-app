package com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import com.geinzz.geinzwork.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.EstadoFiltrosUi
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cargando_categorias
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.retornar_pleaceholder_label
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay


@Composable
fun Pantalla_filtrado_tiendas(
    categoria: String,
    localida: String,
    nombre_user: String, navigation_regresar: () -> Unit,
) {
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val subcategoriaObjs by viewModelFiltros._subcategoiraList.observeAsState(emptyList())
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState(emptyList())
    val horario_tienda by viewModelFiltros._horario_tienda.observeAsState(emptyList())
    val tiendasFiltradas by viewModelFiltros._tiendas_filtradas_por_categoria.observeAsState(
        emptyList()
    )

    val estadoFiltrosUi = EstadoFiltrosUi(
        subcategorias = subcategoriaObjs,
        tiendasFiltradas = tiendasFiltradas
    )

    var showBottomSheet by remember { mutableStateOf(false) }
    var visible_texfiel by remember { mutableStateOf(false) }
    var cargando = remember { mutableStateOf(true) }


    var texto_filtrado by rememberSaveable { mutableStateOf("") }
    var id_tienda_selecionada by remember { mutableStateOf("") }
    var categoria_seleccionda by remember { mutableStateOf("") }

    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var lista_subcategorias by remember { mutableStateOf<List<String>>(emptyList()) }

    val composision by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.carga_tiendas_filtradas))

    var categoria_anterior by remember { mutableStateOf("") }
    var listaMostrar by remember { mutableStateOf<List<tiendas_por_categoria>>(emptyList()) }
    var listaBaseSubcategoria by remember { mutableStateOf(emptyList<tiendas_por_categoria>()) }


    LaunchedEffect(categoria_seleccionda) {
        if (categoria_seleccionda.isNotBlank() && categoria_seleccionda != categoria_anterior) {
            val tiendas_filtradas = viewModelFiltros.filtrar_por_subcategoria(categoria_seleccionda)
            listaBaseSubcategoria = tiendas_filtradas
            listaMostrar = tiendas_filtradas
            categoria_anterior = categoria_seleccionda
        }
    }

    LaunchedEffect(texto_filtrado) {
        listaMostrar = if (texto_filtrado.isBlank()) {
            listaBaseSubcategoria
        } else {
            viewModelFiltros.filtrar_por_nombre_en_lista(texto_filtrado, listaBaseSubcategoria)
        }
    }

    LaunchedEffect(estadoFiltrosUi.subcategorias) {
        val subcategorias: List<String> = estadoFiltrosUi.subcategorias.flatMap { it.subcategorias }
        lista_subcategorias = subcategorias

    }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(localida, id_tienda_selecionada)
        }
    }

    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            Log.d("DatosObtenidos", datosTienda.toString())
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }

    LaunchedEffect(Unit) {
        cargando.value = true
        viewModelFiltros.obtener_subcategorias(categoria)
        viewModelFiltros.obtener_tiendas_filtradas(localida, categoria)
        delay(6000)
        cargando.value = false
    }

    LaunchedEffect(tiendasFiltradas) {
        if (tiendasFiltradas.isNotEmpty()) {
            Log.d("llamos_tiendas_por", tiendasFiltradas.toString())
            viewModelFiltros.tiendas_iniciales(tiendasFiltradas)
            listaMostrar = tiendasFiltradas
            delay(6000)
            cargando.value = false
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Crossfade(targetState = cargando.value, label = "Cargando transición") { isCargando ->
            if (isCargando) {
                cargando_categorias(
                    composision,
                    localida,
                    30.dp,
                    viewModelFiltros.fraces_loadin(localida, nombre_user, categoria)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 10.dp)
                ) {
                    item { encabezado_chis_categorias() }

                    item {
                        chips_filtrado(lista_subcategorias, { expandir ->
                            visible_texfiel = expandir
                        }, { categoria_selecionada ->
                            categoria_seleccionda = categoria_selecionada
                        })
                    }

                    item {
                        Text_fiel_filtrado(visible_texfiel, texto_filtrado) {
                            texto_filtrado = it
                        }
                    }

                    items(listaMostrar) { tienda ->
                        item_tiendas(tienda) { id_tienda, listener ->
                            showBottomSheet = listener
                            id_tienda_selecionada = id_tienda
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            bottom_sheet_tiendas_filtradas(
                viewModelFiltros,
                dataclass_tienda_seleccionada
            ) {
                showBottomSheet = false
            }
        }

    }
}

@Composable
fun encabezado_chis_categorias() {
    Text(
        "Busca tus tiendas favoritas",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    spacer_vertical(5.dp)
    Text(
        "Filtra entre nuestras categorías o busca directamente por el nombre de esa tienda que tanto te gusta. ¡Explorar nunca fue tan fácil y rápido!",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    spacer_vertical(5.dp)
}

@Composable
fun chips_filtrado(
    lista_subcategorias: List<String>,
    expandir_carta: (Boolean) -> Unit,
    selecionado: (String) -> Unit
) {
    var sub_categoria_selecionada by remember { mutableStateOf<String?>(null) }
    LazyRow() {
        items(lista_subcategorias) { subcategorias ->
            val selecionado = sub_categoria_selecionada == subcategorias

            FilterChip(
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = Color.White,
                    labelColor = Color.White
                ),
                modifier = Modifier.padding(horizontal = 4.dp),
                selected = selecionado,
                border = if (selecionado) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground   ),
                onClick = {
                    sub_categoria_selecionada = if (selecionado) null else subcategorias
                    expandir_carta(true)
                    selecionado(sub_categoria_selecionada.toString())
                },
                label = {
                    Text(text = subcategorias, color = if (selecionado) Color.White else MaterialTheme.colorScheme.onBackground)
                },
                shape = RoundedCornerShape(40)
            )

        }
    }
}

@Composable
fun Text_fiel_filtrado(
    visible_texfiel: Boolean,
    texto_filtrado_txt: String,
    texto_filtrado: (String) -> Unit,
) {
    var icono_busqeuda by remember { mutableStateOf(R.drawable.buscar_icon) }

    AnimatedVisibility(
        visible = visible_texfiel,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        custom_texFiel(
            value = texto_filtrado_txt,
            onValueChange = {
                texto_filtrado(it)
                if (it.isNotBlank()) {
                    icono_busqeuda = R.drawable.vector_eliminar_texto_texfiel
                } else {
                    icono_busqeuda = R.drawable.buscar_icon
                }
            },
            labelText = "Ingresa el nombre de la tienda",
            placeholderText = "Ingresa el nombre",
            trailingIcon = {
                if (icono_busqeuda == R.drawable.vector_eliminar_texto_texfiel) {
                    IconButton(onClick = {
                        texto_filtrado("")
                        icono_busqeuda = R.drawable.buscar_icon
                    }) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(id = icono_busqeuda),
                            contentDescription = "Eliminar texto"
                        )
                    }
                } else {
                    androidx.compose.material3.Icon(
                        painter = painterResource(id = icono_busqeuda),
                        contentDescription = "Buscar por subcategoría"
                    )
                }
            },
            isError = false,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(),
        )

    }
}


@Composable
fun item_tiendas(
    item_tiendas: tiendas_por_categoria,
    listener_botom_sheet: (id_tienda: String, showBottomSheet: Boolean) -> Unit
) {
    var detalles_tienda by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .clickable { listener_botom_sheet(item_tiendas.id_tienda, true) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 7.dp, vertical = 7.dp)
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item_tiendas.logo_tienda,
                    contentDescription = "Imagen local",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .weight(.8f)
                        .clip(RoundedCornerShape(15)),
                    placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
                    error = painterResource(id = R.drawable.qr_yape)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .weight(3f)
                ) {
                    Nombre_estado_tienda(item_tiendas.nombre_tienda)
                    spacer_vertical(5.dp)
                    Caracteristicas_tiendas(
                        "Direccion :", item_tiendas.direccion
                    )
                    spacer_vertical(5.dp)
                    Caracteristicas_tiendas("Referencia : ", item_tiendas.referencia)
                    spacer_vertical(5.dp)
                }
                spacer_horizonta(5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Btn_Expandir_card { expandir -> detalles_tienda = expandir }
                }
            }
            AnimatedVisibility(visible = detalles_tienda) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize() // 👈 Esto hace la animación de altura
                ) {
                    Text(
                        text = "Descripcion : ${item_tiendas.descripcion}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Subcategorias a la cual pertenece ${item_tiendas.lista_subcategoiras}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                }
            }


        }


    }
}

@Composable
fun Nombre_estado_tienda(nombre_tiendas: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = nombre_tiendas,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.guardados_icon),
                contentDescription = ""
            )
        }

        Spacer(modifier = Modifier.width(10.dp))


    }
}

@Composable
fun Caracteristicas_tiendas(caracteristica: String, texto: String) {
    Row() {
        Text(text = caracteristica, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = texto,
            modifier = Modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun Btn_Expandir_card(expandir_carta: (Boolean) -> Unit) {
    var expandida_carta by remember { mutableStateOf(false) }
    val icono_cambiado =
        if (expandida_carta) R.drawable.ocultar_abajo else R.drawable.ocultar_arriva
    FloatingActionButton(
        modifier = Modifier
            .padding(5.dp)
            .size(30.dp)
            .clip(CircleShape),
        onClick = {
            expandida_carta = !expandida_carta
            expandir_carta(expandida_carta)
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Image(
            modifier = Modifier.size(15.dp),
            painter = painterResource(icono_cambiado),
            contentDescription = "",
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}
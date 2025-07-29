package com.geinzz.geinzwork.ui.adapters.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import com.geinzz.geinzwork.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.AsyncImage
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_resultado_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import java.text.Normalizer
import kotlin.collections.forEach



class localizate_geinz_wokr_ui : ComponentActivity() {
    private val viewModel by viewModels<viewModel_localizate_geinz>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeinzWorkTheme {
                val localidad_user = intent.getStringExtra("filtrado_localidad")?.lowercase() ?: "barranca"
                val lista = remember { mutableStateListOf<encontradas_por_categoria>() }
                var texto_filtrado by rememberSaveable { mutableStateOf("") }
                val composision by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cargando_categorias))
                val datosCategorias = remember { mutableStateMapOf<String, Triple<Int, Int, String>>() }
                val lista_filtrada = remember { mutableStateListOf<encontradas_por_categoria>().apply { addAll(lista) } }
                val cargando = remember { mutableStateOf(true) }

                val encontrados_activos_tiendass by viewModel.encontrados_activos_tiendas.observeAsState()
                val lista_localidades = constantes_lista_localidades.lista

                var localidadAnterior by remember { mutableStateOf("") }
                val localidadSeleccionada = rememberSaveable { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    if (localidadSeleccionada.value.isEmpty()) {
                        localidadSeleccionada.value = localidad_user
                    }
                }

                LaunchedEffect(localidadSeleccionada.value) {
                    if (localidadSeleccionada.value != localidadAnterior) {
                        cargando.value = true
                        localidadAnterior = localidadSeleccionada.value
                        viewModel.obtener_horario_tiendas(localidadSeleccionada.value)
                    }
                }

                LaunchedEffect(encontrados_activos_tiendass) {
                    encontrados_activos_tiendass?.let { listaNueva ->
                        datosCategorias.clear()
                        lista.clear()
                        lista.addAll(listaNueva)
                        listaNueva.forEach { item ->
                            datosCategorias[normalizarTexto(item.categoria ?: "Desconocido")] =
                                Triple(
                                    item.cantidad_registradas ?: 0,
                                    item.activas ?: 0,
                                    item.categoria ?: "Desconocido"
                                )
                        }
                        cargando.value = false
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(targetState = cargando.value) { isCargando ->
                        if (isCargando) {
                            cargando_categorias(composision, localidadSeleccionada.value)
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                item {
                                    cabezero_activity(localidad_user)
                                }

//                                stickyHeader {
//                                    Column(
//                                        modifier = Modifier
//                                            .background(MaterialTheme.colorScheme.background)
//                                            .padding(top = 8.dp, bottom = 8.dp)
//                                    ) {
//                                        FiltradosChipsLocalidades(
//                                            lista_localidades,
//                                            localidadSeleccionada.value
//                                        ) { nuevaLocalidad -> localidadSeleccionada.value = nuevaLocalidad }
//
//                                        filtrado_texto(
//                                            texto_filtrado,
//                                            lista,
//                                            { texto_filtrado = it },
//                                            { nuevaLista, _ ->
//                                                lista_filtrada.clear()
//                                                lista_filtrada.addAll(nuevaLista)
//                                                Log.d("sugerencias", nuevaLista.toString())
//                                            }
//                                        )
//                                    }
//                                }

                                val listaParaMostrar = if (texto_filtrado.length > 2) lista_filtrada else lista

                                items(listaParaMostrar, key = { it.categoria ?: it.hashCode().toString() }) { item ->
                                    MostrarSugerencias(item, datosCategorias)
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FloatingOverlayButtonsRefinado() {
    var menuOpen by remember { mutableStateOf(false) }

    // Transición del fondo
    val transition = updateTransition(targetState = menuOpen, label = "menuTransition")
    val backgroundAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 400) },
        label = "alphaAnim"
    ) { isOpen -> if (isOpen) 0.9f else 0f }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo oscuro suave
        if (backgroundAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backgroundAlpha))
                    .clickable { menuOpen = false }
            )
        }

        // Botones animados flotantes
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = menuOpen,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = { /* Acción 1 */ },
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = "Nota")
                }
            }

            AnimatedVisibility(
                visible = menuOpen,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = { /* Acción 2 */ },
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = "Lista")
                }
            }
        }

        // FAB principal (botón +)
        FloatingActionButton(
            onClick = { menuOpen = !menuOpen },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (menuOpen) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (menuOpen) "Cerrar" else "Abrir"
            )
        }
    }
}



@Composable
fun cargando_categorias(composision: LottieComposition?, value: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (texto, loti_animation) = createRefs()
            Text(
                text = "Cargando tiendas de $value",
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(texto) {
                        top.linkTo(loti_animation.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                textAlign = TextAlign.Center
            )
            LottieAnimation(
                composition = composision,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(400.dp)
                    .constrainAs(loti_animation) {}
            )
        }

    }
}

@Composable
fun cabezero_activity(localidad_registrado: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ubicate $localidad_registrado",
            fontSize = 25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center

        )
        Text(
            modifier = Modifier.padding(vertical = 0.dp),
            text = "Explora las diferentes categorías de tiendas\n" +
                    "registradas en Geinz Work y ubícate fácilmente en $localidad_registrado"
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
    LazyRow(modifier = Modifier.padding(top = 5.dp)) {
        items(lista_localidades) { localidad ->
            val isSelected = localidadSeleccionada.equals(localidad.nombre_localidad, ignoreCase = true)
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onLocalidadSeleccionada(localidad.nombre_localidad.toString())
                    }
                },
                label = {
                    Text(text = localidad.nombre_localidad.toString())
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
                shape = RoundedCornerShape(50)
            )
        }
    }
}


@Composable
fun filtrado_texto(
    texto: String,
    lista_cargada_filstrado: List<encontradas_por_categoria>,
    texto_filtrado: (String) -> Unit,
    busquedaAction: (List<encontradas_por_categoria>, Boolean) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var is_error by rememberSaveable { mutableStateOf(false) }
    var icono_busqeuda by rememberSaveable { mutableStateOf(R.drawable.buscar_icon) }
    val sugerencias: List<dataclass_resultado_filtrado> = lista_cargada_filstrado
        .flatMap { catSub ->
            catSub.subcateogiras.orEmpty().mapNotNull { subcat ->
                if (subcat.contains(texto, ignoreCase = true) && texto.isNotBlank()) {
                    dataclass_resultado_filtrado(catSub.categoria.toString(), subcat)
                } else null
            }
        }
    Log.d("obtener_sugeerncias",sugerencias.toString())


    is_error = expanded && sugerencias.isEmpty()
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = texto,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                onValueChange = {
                    texto_filtrado(it)
                    expanded = it.isNotBlank()
                    if (it.isNotBlank()) {
                        icono_busqeuda = R.drawable.vector_eliminar_texto_texfiel
                        busquedaAction(obtenerResultados(it, lista_cargada_filstrado), true)

                    } else {
                        icono_busqeuda = R.drawable.buscar_icon
                    }
                },
                label = {
                    retornar_pleaceholder_label(
                        texto = "Ingrese la subcategoría a buscar",
                        modifier = Modifier
                    )
                },
                placeholder = {
                    retornar_pleaceholder_label(
                        texto = "Ingrese la subcategoría a buscar",
                        modifier = Modifier
                    )
                },
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
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        busquedaAction(obtenerResultados(texto, lista_cargada_filstrado), true)
                        expanded = false
                        focusManager.clearFocus()
                    }
                ),
                isError = is_error,
            )

            if (is_error) {
                Text("No hay coincidencias", color = Color.Red)
            }
        }


    }
}


@Composable
fun MostrarSugerencias(
    item: encontradas_por_categoria,
    datosCategorias: Map<String, Triple<Int, Int, String>>
) {
    val categoriaKey = item.categoria?.let { normalizarTexto(it) } ?: return
    val triple = remember(categoriaKey, datosCategorias) {
        datosCategorias.entries.find {
            normalizarTexto(it.key) == categoriaKey
        }?.value
    }

    val cantidadRegistradas = triple?.first ?: 0
    val cantidadActivas = triple?.second ?: 0

    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .clickable { expandido = !expandido }
            .animateContentSize()
            .defaultMinSize(minHeight = 200.dp),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/IMG_CategoriasGeneral%2FcategoriasTienda%2FCuidado%20personal%20(1).png?alt=media&token=4f491879-c2bd-46b9-a197-82652b3bcdde", // tu URL o imagen de categoría
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
                    error = painterResource(id = R.drawable.sin_qr_icon)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val subcatsUnidos = remember(item.subcateogiras) {
                item.subcateogiras?.joinToString(", ") ?: ""
            }
            Text(
                text = subcatsUnidos,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (expandido) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tiendas Registradas $cantidadRegistradas",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tiendas activas $cantidadActivas",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Green
            )
            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}


fun obtenerResultados(
    texto: String,
    lista: List<encontradas_por_categoria>
): List<encontradas_por_categoria> = lista.filter { catSub ->
    catSub.subcateogiras?.any {
        it.contains(texto, ignoreCase = true)
    } == true
}

@Composable
fun retornar_pleaceholder_label(texto: String, modifier: Modifier) {
    Text(texto)
}

fun normalizarTexto(texto: String): String {
    val textoSinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return textoSinTildes.lowercase().trim()
}



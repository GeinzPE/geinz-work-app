package com.geinzz.geinzwork.ui.adapters.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import com.geinzz.geinzwork.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.VerticalAlign
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub

import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_resultado_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.model.modelo_agregar_cat_sub_localizate
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import java.nio.file.WatchEvent
import java.text.Normalizer

class localizate_geinz_wokr_ui : ComponentActivity() {
    val modelo_cat_sub = modelo_agregar_cat_sub_localizate()
    private val viewModel by viewModels<viewModel_localizate_geinz>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val nombre = intent.getStringExtra("filtrado_localidad") ?: "Barranca"
        setContent {
            GeinzWorkTheme {
                val lista = remember { mutableStateListOf<encontradas_por_categoria>() }
                var texto_filtrado by rememberSaveable { mutableStateOf("") }
                val composision by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.carga_loading))
                val datosCategorias =
                    remember { mutableStateMapOf<String, Triple<Int, Int, String>>() }
                val lista_filtrada =
                    remember { mutableStateListOf<encontradas_por_categoria>().apply { addAll(lista) } }
                val cargando = remember { mutableStateOf(true) }
                val localidadSeleccionada = remember { mutableStateOf("") }
                val encontrados_activos_tiendas by viewModel.encontrados_activos_tiendas.observeAsState()
                val lista_localidades = constantes_lista_localidades.lista
                LaunchedEffect(localidadSeleccionada.value) {
                    cargando.value = true
                    viewModel.obtener_horario_tiendas(localidadSeleccionada.value)
                }
                LaunchedEffect(encontrados_activos_tiendas) {
                    encontrados_activos_tiendas?.let {
                        lista.clear()
                        lista.addAll(it)
                        cargando.value = false
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(vertical = 7.dp, horizontal = 7.dp)
                    ) {
                        item {
                            cabezero_activity(nombre)
                        }
                        item {
                            FiltradosChipsLocalidades(
                                lista_localidades,
                                nombre,
                                viewModel,
                                { registrado, activos, categoria ->
                                    datosCategorias[normalizarTexto(categoria)] =
                                        Triple(registrado, activos, categoria)
                                    Log.d(
                                        "tiendas_obnenidos_por_cateogira",
                                        "registrados:$registrado , activos:$activos, categoria:$categoria $localidadSeleccionada"
                                    )
                                },
                                { localidda ->
                                    localidadSeleccionada.value = localidda
                                }
                            )
                        }

                        // Mostrar animación mientras carga
                        if (cargando.value) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Green),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LottieAnimation(
                                        composition = composision,
                                        iterations = LottieConstants.IterateForever,
                                        modifier = Modifier
                                            .size(200.dp)
                                            .background(Color.Red),
                                    )
                                }
                            }
                        } else {
                            // Mostrar buscador cuando no está cargando
                            item {
                                filtrado_texto(
                                    texto_filtrado,
                                    lista,
                                    { texto_filtrado = it },
                                    { nuevaLista, activar ->
                                        lista_filtrada.clear()
                                        lista_filtrada.addAll(nuevaLista)
                                        Log.d("sugerencias", nuevaLista.toString())
                                    }
                                )
                            }

                            val listaParaMostrar =
                                if (texto_filtrado.length > 2) lista_filtrada else lista

                            items(listaParaMostrar.chunked(2)) { fila ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    fila.forEach { item ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            MostrarSugerencias(item, datosCategorias)
                                        }
                                    }
                                    if (fila.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
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
fun cabezero_activity(localidad_registrado: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center, // centra vertical
        horizontalAlignment = Alignment.CenterHorizontally // centra horizontal
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
    localidad_registrado: String,
    viewmodel_localizate: viewModel_localizate_geinz,
    calback: (Int, Int, categoria: String) -> Unit, localidad_selecionada: (String) -> Unit
) {
    var localidadSeleccionada by remember { mutableStateOf(localidad_registrado) }
    LaunchedEffect(localidadSeleccionada) {
        localidad_selecionada(localidadSeleccionada)
    }
    val encontrados_activos_tiendas by viewmodel_localizate.encontrados_activos_tiendas.observeAsState()

    encontrados_activos_tiendas?.forEach { i ->
        calback(i.cantidad_registradas ?: 0, i.activas ?: 0, i.categoria ?: "Desconocido")
    }
    LazyRow(
        modifier = Modifier.padding(top = 5.dp)
    ) {
        items(lista_localidades) { localidad ->
            val isSelected =
                localidadSeleccionada.equals(localidad.nombre_localidad, ignoreCase = true)
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                selected = isSelected,
                onClick = {
                    localidadSeleccionada = localidad.nombre_localidad.toString()
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
                }
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

    is_error = expanded && sugerencias.isEmpty()
    val focusManager = LocalFocusManager.current
    Box(modifier = Modifier.fillMaxWidth()  .padding(vertical = 5.dp)) {
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
    Log.d("SUGERENCIAS", "Categoría: ${item.categoria}")
    Log.d("SUGERENCIAS", "Subcategorías: ${item.subcateogiras}")
    Log.d("SUGERENCIAS", "activa: ${item.activas}")
    Log.d("SUGERENCIAS", "Total Tiendas: ${item.cantidad_registradas}")
    val categoriaKey = item.categoria?.let { normalizarTexto(it) } ?: return
    val triple = datosCategorias.entries.find {
        normalizarTexto(it.key) == categoriaKey
    }?.value
    val cantidadRegistradas = triple?.first ?: 0
    val cantidadActivas = triple?.second ?: 0

    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expandido = !expandido }
            .animateContentSize()
            .defaultMinSize(minHeight = 250.dp),
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

            val subcatsUnidos = item.subcateogiras?.joinToString(", ") ?: ""

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

            Text(
                text = "Tiendas activas $cantidadActivas",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Green
            )
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



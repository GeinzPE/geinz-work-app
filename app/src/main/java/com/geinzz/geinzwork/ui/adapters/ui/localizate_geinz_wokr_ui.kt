package com.geinzz.geinzwork.ui.adapters.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.geinzz.geinzwork.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import coil3.compose.AsyncImage
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_resultado_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.registradas_activas_cat_img
import com.geinzz.geinzwork.data.model.localizate_geinz.tienda_patrocinada
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.amarillo30
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import kotlinx.coroutines.delay
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
                val localidad_user =
                    intent.getStringExtra("filtrado_localidad")?.lowercase() ?: "barranca"
                val nombre_user = intent.getStringExtra("nombre_user") ?: ""
                val lista = remember { mutableStateListOf<encontradas_por_categoria>() }
                var texto_filtrado by rememberSaveable { mutableStateOf("") }
                val composision by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cargando_categorias))
                val datosCategorias =
                    remember { mutableStateMapOf<String, registradas_activas_cat_img>() }
                val lista_filtrada =
                    remember { mutableStateListOf<encontradas_por_categoria>().apply { addAll(lista) } }
                val cargando = remember { mutableStateOf(true) }

                val encontrados_activos_tiendass by viewModel.encontrados_activos_tiendas.observeAsState()

                val lista_localidades = constantes_lista_localidades.lista

                var localidadAnterior by remember { mutableStateOf("") }
                val localidadSeleccionada = rememberSaveable { mutableStateOf("") }
                val cartaExpandida = remember { mutableStateOf<String?>(null) }


                LaunchedEffect(Unit) {
                    if (localidadSeleccionada.value.isEmpty()) {
                        localidadSeleccionada.value = localidad_user
                    }
                }

                LaunchedEffect(localidadSeleccionada.value) {
                    if (localidadSeleccionada.value != localidadAnterior) {
                        cargando.value = true
                        localidadAnterior = localidadSeleccionada.value
                        cartaExpandida.value = null
                        viewModel.T_obtener_registrados_activos(localidadSeleccionada.value)
                    }
                }



                LaunchedEffect(encontrados_activos_tiendass) {
                    encontrados_activos_tiendass?.let { listaNueva ->
                        datosCategorias.clear()
                        lista.clear()
                        lista.addAll(listaNueva)
                        listaNueva.forEach { item ->
                            datosCategorias[(normalizarTexto(item.categoria ?: ""))] =
                                registradas_activas_cat_img(
                                    item.cantidad_registradas ?: 0,
                                    item.activas ?: 0,
                                    item.categoria ?: "Desconocido",
                                    item.img_subcategorias
                                )
                        }
                        texto_filtrado = ""
                        cargando.value = false
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(targetState = cargando.value) { isCargando ->
                        if (isCargando) {
                            cargando_categorias(
                                composision,
                                localidadSeleccionada.value,
                                nombre_user
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                item {
                                    cabezero_activity(localidad_user)
                                }

                                stickyHeader {
                                    Column(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    bottomStart = 16.dp,
                                                    bottomEnd = 16.dp
                                                )
                                            )
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(8.dp)
                                    ) {
                                        FiltradosChipsLocalidades(
                                            lista_localidades,
                                            localidadSeleccionada.value
                                        ) { nuevaLocalidad ->
                                            localidadSeleccionada.value = nuevaLocalidad
                                        }

                                        filtrado_texto(
                                            texto_filtrado,
                                            lista,
                                            { texto_filtrado = it },
                                            { nuevaLista, _ ->
                                                lista_filtrada.clear()
                                                lista_filtrada.addAll(nuevaLista)
                                                Log.d("sugerencias", nuevaLista.toString())
                                            }
                                        )
                                    }
                                }

                                val listaParaMostrar =
                                    if (texto_filtrado.length > 2) lista_filtrada else lista

                                items(
                                    listaParaMostrar,
                                    key = { it.categoria ?: it.hashCode().toString() }) { item ->
                                    cartas_categorias(
                                        item,
                                        datosCategorias,
                                        cartaExpandida,
                                        localidadSeleccionada.value,
                                        viewModel,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun cargando_categorias(composision: LottieComposition?, value: String, nombre_user: String) {
    var fraseIndex by remember { mutableStateOf(0) }
    var frases by remember {
        mutableStateOf(
            listOf(
                "Espere un momento...",
                "Cargando tienda...",
                "Buscamos lo mejor para ti $nombre_user"
            )
        )
    }
    var fraseActual by remember { mutableStateOf(frases[0]) }

    LaunchedEffect(value) {
        frases = listOf(
            "Espere un momento...",
            "Cargando tiendas de $value...",
            "Buscamos lo mejor para ti $nombre_user"
        )
        fraseIndex = 0
    }

    LaunchedEffect(value) {
        frases = listOf(
            "Espere un momento...",
            "Cargando tiendas de $value...",
            "Buscamos lo mejor para ti $nombre_user"
        )
        fraseIndex = 0

        while (true) {
            fraseActual = frases[fraseIndex]
            fraseIndex = (fraseIndex + 1) % frases.size
            delay(2500L)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (texto_centrado, loti_animation) = createRefs()

            AnimatedContent(
                targetState = fraseActual,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "frase_animada"
            ) { texto ->
                Text(
                    text = texto,
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .constrainAs(texto_centrado) {
                            top.linkTo(loti_animation.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }

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
            .fillMaxSize()
            .padding(horizontal = 8.dp),
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
            val isSelected =
                localidadSeleccionada.equals(localidad.nombre_localidad, ignoreCase = true)
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
fun cartas_categorias(
    item: encontradas_por_categoria,
    datosCategorias: SnapshotStateMap<String, registradas_activas_cat_img>,
    cartaExpandida: MutableState<String?>,
    Localidad_selecionada: String,
    viewModel: viewModel_localizate_geinz,
) {
    val tiendas_patrocinadas_por_categoria by viewModel.T_patrocinadas_por_categoria.observeAsState()

    val listaPatrocinados = remember { mutableStateListOf<tienda_patrocinada>() }
    val cargandoPatrocinados = remember { mutableStateOf(true) }
    val categoriaKey = item.categoria?.let { normalizarTexto(it) } ?: return
    val datos = remember(categoriaKey, datosCategorias) {
        datosCategorias.entries.find {
            normalizarTexto(it.key) == categoriaKey
        }?.value
    }

    val cantidadRegistradas = datos?.cantidad_registradas ?: 0
    val cantidadActivas = datos?.catidad_activas ?: 0
    val nombreSubcategoria = datos?.subcategoria ?: "Desconocido"
    val imagenes = datos?.img_subcategorias ?: emptyList()

    val listState = rememberLazyListState()
    val expandido = cartaExpandida.value == categoriaKey

    var index = 0
    LaunchedEffect(expandido) {
        if (expandido) {
            while (true) {
                delay(3000)
                index = (index + 1) % imagenes.size
                listState.animateScrollToItem(index)
            }
        } else {
            index = 0
            listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(key1 = expandido, key2 = cartaExpandida.value) {
        if (expandido) {
            Log.d("expandimos_carta", cartaExpandida.value.toString())
            viewModel.T_patrocinadas(Localidad_selecionada, cartaExpandida.value ?: "")
        }
    }

    LaunchedEffect(key1 = tiendas_patrocinadas_por_categoria) {
        cargandoPatrocinados.value = true
        val inicio = System.currentTimeMillis()
        listaPatrocinados.clear()
        tiendas_patrocinadas_por_categoria?.let { lista ->
            if (lista.isNotEmpty()) {
                lista.forEach {
                    listaPatrocinados.add(
                        tienda_patrocinada(
                            it.categoria_tienda,
                            it.id_tienda,
                            it.img_tienda,
                            it.nombre, it.latitud, it.longitud, it.direccion, it.referencia
                        )
                    )
                }
            }
        }

        val tiempoTranscurrido = System.currentTimeMillis() - inicio
        val tiempoRestante = 1000 - tiempoTranscurrido
        if (tiempoRestante > 0) delay(tiempoRestante)
        cargandoPatrocinados.value = false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(imagenes.size) { i ->
                        AsyncImage(
                            model = imagenes[i],
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                            placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
                            error = painterResource(id = R.drawable.sin_qr_icon)
                        )
                    }
                }
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    val (activos_encontrados, size_carta) = createRefs()

                    activos_y_registrados(
                        cantidadRegistradas, cantidadActivas,
                        modifier = Modifier.constrainAs(activos_encontrados) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                    )

                    size_carta_categoria(
                        modifier = Modifier.constrainAs(size_carta) {
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }, expandido, onClick = {
                            cartaExpandida.value = if (expandido) null else categoriaKey
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                AnimatedVisibility(visible = expandido) {
                    val subcatsUnidos = remember(item.subcateogiras) {
                        item.subcateogiras?.joinToString(", ") ?: ""
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {

                        spacer_vertical(10.dp)
                        Text(
                            text = "Subcategorias encontradas",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            fontSize = 15.sp
                        )
                        spacer_vertical(5.dp)
                        Text(
                            text = subcatsUnidos,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis
                        )
                        spacer_vertical(10.dp)
                        ListaTiendasPatrocinadas(viewModel = viewModel, tiendas = listaPatrocinados)
                    }
                }
            }
        }
    }
}

@Composable
fun spacer_vertical(altura: Dp) {
    Spacer(modifier = Modifier.height(altura))
}

@Composable
fun spacer_horizonta(ancho: Dp) {
    Spacer(modifier = Modifier.width(ancho))

}


@Composable
fun ListaTiendasPatrocinadas(
    viewModel: viewModel_localizate_geinz,
    tiendas: List<tienda_patrocinada>
) {
    val isLoading by viewModel.loading
    val context = LocalContext.current
    val mostrarDialogo = remember { mutableStateOf(false) }
    val mostrarDialog_sin_google_maps = remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }


    if (mostrarDialogo.value) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                mostrarDialogo.value = false
            },
            abrir_configuracion = {
                mostrarDialogo.value = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                mostrarDialogo.value = false
                mostrarDialog_sin_google_maps.value = true
            })
    }
    if (mostrarDialog_sin_google_maps.value) {
        dialog_sin_ubi_activa(
            direccion, referencia, onDismis = { mostrarDialog_sin_google_maps.value = false },
            abrir_maps = { constantes.abrirGoogleMaps(context,direccion)})

    }



    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(text = "Buscando tiendas patrocinadas")
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (tiendas.isNotEmpty()) {
                    Text(
                        text = "Tiendas Patrocinadas",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        fontSize = 15.sp
                    )
                    spacer_vertical(10.dp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        items(tiendas) { tienda ->
                            patrocinadores(item = tienda) { latitud, longitud ->
                                Log.d("hicimos_clik_ne","${tienda.nombre}")
                                if (verificarUbiActiva(context)) {
                                    abrirRutaEnGoogleMaps(context, latitud, longitud)
                                } else {
                                    mostrarDialogo.value = true
                                }
                                    direccion = tienda.direccion ?: ""
                                    referencia = tienda.referencia ?: ""
                            }
                        }
                    }

                    spacer_vertical(10.dp)
                }
            }
        }
    }
}


fun abrirRutaEnGoogleMaps(context: Context, latitud: Double, longitud: Double) {
    val uri = Uri.parse("google.navigation:q=$latitud,$longitud&mode=d")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Google Maps no está instalado. Por favor, instálalo desde Play Store.",
            Toast.LENGTH_LONG
        ).show()
    }
}


@Composable
fun patrocinadores(item: tienda_patrocinada, abrir_maps: (Double, Double) -> Unit) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(215.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0F7FA)
        )
    ) {
        AsyncImage(
            model = item.img_tienda.toString(),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)),
            placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
            error = painterResource(id = R.drawable.qr_yape)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.nombre.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(.5f),
                maxLines = 1,
                color = Color.Black,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(3.dp))
            FloatingActionButton(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                onClick = {
                    abrir_maps(item.latitud!!.toDouble(), item.longitud!!.toDouble())
                },
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.localidad_icon_general),
                    contentDescription = "Icono"
                )
            }
        }
    }
}

@Composable
fun dialog_sin_ubicacion_activa() {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {},
                shape = RoundedCornerShape(15)
            ) { Text(text = "Activar Ubicación") }
        },
        dismissButton = { TextButton(onClick = {}) { Text(text = "Cerrar") } },
        title = { Text(text = "Ubicación desactivada") },
        text = {
            Column {
                Text("Te recomendamos activar el GPS para que podamos mostrarte la mejor ruta hasta la tienda en Google Maps.")
                spacer_vertical(10.dp)
                Text(
                    text = "Continuar sin activar ubicación",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable { }
                        .padding(top = 8.dp)
                )
            }
        },
        shape = RoundedCornerShape(10),
        icon = {
            Icon(
                modifier = Modifier.size(25.dp),
                painter = painterResource(R.drawable.google_maps_icono),
                contentDescription = "Google maps",
                tint = Color.Unspecified
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )

    )

}


@Composable
fun activos_y_registrados(
    cantidad_registrados: Int,
    cantidad_activos: Int,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = modifier
                .background(amarillo30)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                "Activos $cantidad_activos",
                modifier = modifier.padding(horizontal = 2.dp),
                fontSize = 15.sp,
                color = Color.Green
            )
            Text(
                "Encontrados $cantidad_registrados",
                fontSize = 15.sp,
                modifier = modifier.padding(horizontal = 5.dp), color = Color.Black
            )
        }

    }
}

@Composable
fun size_carta_categoria(modifier: Modifier, expanddido: Boolean, onClick: () -> Unit) {
    FloatingActionButton(
        modifier = modifier
            .size(35.dp)
            .clip(CircleShape),
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        )
    ) {
        val icono_cambiante = if (expanddido) {
            R.drawable.ocultar_abajo
        } else {
            R.drawable.ocultar_arriva
        }
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = icono_cambiante),
            contentDescription = "Icono"
        )
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


fun verificarUbiActiva(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}






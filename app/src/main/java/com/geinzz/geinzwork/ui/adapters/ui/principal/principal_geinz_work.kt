package com.geinzz.geinzwork.ui.adapters.ui.principal

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.mascara_img
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.rutas_turismo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulo_referenciales_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal(categorias: () -> Unit, ver_lugares:()-> Unit) {
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    val _lugares_turisticos by viewModel_cordenadas._lugares_turisticos.observeAsState(emptyList())
    val _categorias_tiendas by viewModel_cordenadas._sub_cat_tiendas.observeAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel_cordenadas.lugares_turisticos("barranca")
        viewModel_cordenadas.obtener_subcategorias()
    }

    val lista_localidades = constantes_lista_localidades.lista
    val localidadSeleccionada = rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = { ScannerButton() }, floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(10.dp)
        ) {
            item {
                nombre_texto_img_perfil()

            }

            stickyHeader() {
                ColumnContenedorComun {
                    texfiel_filtrado()
                    FiltradosChipsLocalidades(
                        lista_localidades,
                        localidadSeleccionada.value
                    ) { nuevaLocalidad -> }
                }
            }

            item {
                spacer_vertical(10.dp)
                apartado_turismo(_lugares_turisticos,ver_lugares)
                spacer_vertical(10.dp)
            }

            item {
                rutas_turismo(
                    "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0159.00_00_00_00.Imagen%20fija002.webp?alt=media&token=c4c60311-1293-4731-b2e4-c51265c15860",
                    "ver rutas",
                    "descubre barranca"
                ) {}
                spacer_vertical(20.dp)
            }

            item {
                apartado_explora_cat(_categorias_tiendas, categorias)
                spacer_vertical(10.dp)
            }

            item {
                rutas_turismo(
                    "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0593.webp?alt=media&token=8e770a68-dfad-4ae1-8d20-c9133e2f4a49",
                    "ver eventos",
                    "Eventos proximos"
                ) {}
            }
//            item { recomendado_por_vistitantes() }
        }
    }

}


@Composable
fun ScannerButton() {
    val context = LocalContext.current

    val startScanner = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result -> handleScanResult(context, result?.contents) }
    )

    FloatingActionButton(
        onClick = { startScanner.launch(ScanOptions()) },
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        val painter = painterResource(id = R.drawable.qr_scaner_icon)
        Image(
            painter = painter,
            contentDescription = "Escanear QR",
            modifier = Modifier.size(28.dp)
        )
    }
}


private fun handleScanResult(context: Context, contenidoEscaneado: String?) {
    if (contenidoEscaneado.isNullOrEmpty()) {
        Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        return
    }

    Log.d("obtenoemos_resultado", contenidoEscaneado)

    try {
        if (contenidoEscaneado.startsWith("Tienda|")) {
            val base64Coordenadas = contenidoEscaneado.removePrefix("Tienda|")
            val (lat, lng) = generar_qr_cordenadas_tienda.decodificarCoordenadas(base64Coordenadas)
            constantes_lista_localidades.abrir_google_maps(context, lat, lng) { dialogo ->
                if (dialogo) Toast.makeText(
                    context,
                    "Activa tu ubicación primero",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Aquí podrías llamar al ViewModel si fuera necesario
            Log.d("Scanner", "Otro tipo de QR: $contenidoEscaneado")
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al decodificar coordenadas", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

@Composable
fun apartado_explora_cat(categorias_tienda: List<dataclass_cat_sub>, categorias1: () -> Unit) {
    Log.d("obtemloms_lista", categorias_tienda.toString())
    val categoriasPrincipales = remember {
        categorias_tienda.shuffled().take(5)
    }
    spacer_vertical(10.dp)
    Column {
        titulo_referenciales_geinz_work("Explora categorias", "Ver todos") { categorias1() }
        spacer_vertical(10.dp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                items = categoriasPrincipales,
                key = { it.nombre.toString() }
            ) { categorias ->
                apartado_categorias_tiendas(
                    categorias.lista_img,
                    categorias.nombre.toString(),
                    categorias.lista_subcategorias,
                    300.dp,
                    150.dp,
                    5
                )
            }
        }

    }
}


@Composable
fun apartado_categorias_tiendas(
    img: String,
    nombre_categoria: String,
    lista_subcateogiras: List<String>,
    ancho: Dp,
    alto: Dp,
    rounder: Int
) {
    var expandir_subcategorias by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(ancho)
            .clip(RoundedCornerShape(rounder))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .width(ancho)
                .height(alto)

        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img)
                    .size(ancho.value.toInt(), alto.value.toInt())
                    .crossfade(true)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .width(ancho)
                    .height(alto)
                    .clip(RoundedCornerShape(rounder)),
                contentScale = ContentScale.Crop
            )

            mascara_img(rounder, alto, ancho)

            texto_categorias(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                nombre_categoria,
                expandir_subcategorias
            ) { expandir_subcategorias = !expandir_subcategorias }
        }

        AnimatedVisibility(expandir_subcategorias) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    tags_subcateogiras(lista_subcateogiras)
                }
            }
        }
    }
}

@Composable
fun texto_categorias(
    modifier: Modifier,
    nombre_categoria: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        texto_generico_one_line(
            nombre_categoria.uppercase(), MaterialTheme.typography.titleSmall,
            Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )
        FloatingActionButton(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape),
            onClick = { onClickExpand() },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(
                    constantes_lista_localidades.cambiar_icono_exapndible(
                        expandido
                    )
                ),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun apartado_turismo(lugares: List<lugares_turisticos>,ver_lugares:()-> Unit) {
    val lugaresSeleccionados = rememberSaveable(lugares.hashCode()) {
        lugares.shuffled().take(5)
    }
    var bottom_sheet_cartas by remember { mutableStateOf(false) }

    var datos_lugares by remember { mutableStateOf(lugares_turisticos()) }

    Column {
        titulo_referenciales_geinz_work("Lugares turísticos", "Ver lugares") {ver_lugares()}
        spacer_vertical(10.dp)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lugaresSeleccionados) { lugar ->
                cartas_turismo(
                    lugar,
                    5,
                    300.dp,
                    300.dp
                ) { bottom_sheet_listener, datos_selecionado ->
                    bottom_sheet_cartas = bottom_sheet_listener
                    datos_lugares = datos_selecionado
                }
            }
        }
    }
    if (bottom_sheet_cartas) {
        bottom_sheet_lugares_turisticos(datos_lugares) { bottom_sheet_cartas = false }
    }

}


@Composable
fun cartas_turismo(
    lugar: lugares_turisticos,
    rounder: Int,
    alto: Dp,
    ancho: Dp,
    listener: (Boolean, lugares_turisticos) -> Unit
) {
    Box(
        modifier = Modifier
            .width(ancho)
            .height(alto)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(lugar.img_ref)
                .size(ancho.value.toInt(), alto.value.toInt())
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(ancho)
                .height(alto)
                .clip(RoundedCornerShape(rounder))
                .clickable {
                    listener(true, lugar)
                },
            contentScale = ContentScale.Crop
        )

        mascara_img(rounder, alto, ancho)
        texto_encimado_cartas(
            modifier = Modifier.align(Alignment.BottomStart),
            lugar.titulo,
            "ir y conocer".uppercase(),
        )

    }
}


@Composable
fun texto_encimado_cartas(
    modifier: Modifier,
    titulo: String,
    descripcion: String,
) {
    Row(modifier = modifier.padding(start = 20.dp, end = 20.dp, bottom = 40.dp)) {
        Column {
            texto_generico_multilinea(
                titulo,
                MaterialTheme.typography.titleMedium
            )
            spacer_vertical(5.dp)
            TextoSubrayado(
                descripcion,
                MaterialTheme.typography.bodySmall, modifier = Modifier
            )
        }

    }

}





@Composable
fun texfiel_filtrado() {
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
                .padding(bottom = 10.dp, top = 10.dp)
        ) {
            texto_generico_one_line(
                texto = "Hola $nombre_user",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            texto_generico_one_line(
                texto = "Encuentra tu lugar",
                MaterialTheme.typography.busquedaGeinzWork
            )
        }
        Image(
            painter = painterResource(R.drawable.cargar_foto_500x500),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentDescription = ""
        )

    }


}


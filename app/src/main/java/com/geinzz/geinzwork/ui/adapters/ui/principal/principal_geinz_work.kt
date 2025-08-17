package com.geinzz.geinzwork.ui.adapters.ui.principal

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.floatin_actionButton
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal() {
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    val _lugares_turisticos by viewModel_cordenadas._lugares_turisticos.observeAsState(emptyList())
    val _categorias_tiendas by viewModel_cordenadas._sub_cat_tiendas.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel_cordenadas.lugares_turisticos("barranca")
        viewModel_cordenadas.obtener_subcategorias()
    }

    val lista_localidades = constantes_lista_localidades.lista
    val localidadSeleccionada = rememberSaveable { mutableStateOf("") }

    Scaffold() { innerPadding ->
//        MyGoogle_maps()
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
                apartado_turismo(_lugares_turisticos)
            }
            item {
                apartado_explora_cat(_categorias_tiendas)
            }
            item {
                rutas_turismo()
            }
//            item { recomendado_por_vistitantes() }
        }
    }

}


@Composable
fun apartado_explora_cat(categorias_tienda: List<dataclass_cat_sub>) {
    val categoiras_principales = remember(categorias_tienda) {
        categorias_tienda.shuffled().take(5)
    }
    spacer_vertical(10.dp)
    Column {
        texto_generico_one_line("Explora establecimientos", MaterialTheme.typography.titleLarge)
        spacer_vertical(10.dp)
        LazyRow ( horizontalArrangement = Arrangement.spacedBy(8.dp)){
            items(categoiras_principales) { categorias_tienda ->
                apartado_categorias_tiendas(
                    categorias_tienda.lista_img,
                    categorias_tienda.nombre.toString(),
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
    ancho: Dp,
    alto: Dp,
    rounder: Int
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
        texto_categorias(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),nombre_categoria,false){}

    }
}

@Composable
fun texto_categorias(modifier: Modifier, nombre_categoria: String, expandido: Boolean, onClickExpand:()-> Unit) {
    Row (modifier, verticalAlignment = Alignment.CenterVertically){
    texto_generico_one_line(nombre_categoria.uppercase(), MaterialTheme.typography.titleMedium,
        Modifier.weight(1f).padding(end = 10.dp))
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
        )   {
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
fun apartado_turismo(lugares: List<lugares_turisticos>) {
    val lugaresSeleccionados = remember(lugares) {
        lugares.shuffled().take(5)
    }

    Column {
        texto_generico_one_line(
            "Lugares turísticos",
            MaterialTheme.typography.titleLarge
        )
        spacer_vertical(5.dp)

        LazyRow {
            items(lugaresSeleccionados) { lugar ->
                cartas_turismo(
                    lugar.titulo.orEmpty(),
                    "Ver mas",
                    lugar.img_ref.orEmpty(),
                    5,
                    300.dp,
                    300.dp
                ) {}
                spacer_horizonta(8.dp)
            }
        }
    }
}


//@Preview(showBackground = true)
@Composable
fun rutas_turismo() {
    spacer_vertical(10.dp)
    Box() {
        Image(
            painter = painterResource(R.drawable.cargar_foto_500x500),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(5)),
            contentDescription = "",
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5))
                .background(Color.Black.copy(alpha = 0.5f))
                .fillMaxWidth()
                .height(400.dp)
        ) {
        }
        texto_encimado(modifier = Modifier.align(Alignment.BottomStart), "barranca")
    }
}

//@Composable
//fun recomendado_por_vistitantes() {
//    spacer_vertical(20.dp)
//    texto_generico_one_line("Recomendado por vistitantes", MaterialTheme.typography.titleLarge)
//    spacer_vertical(5.dp)
//    Column {
//        cartas_turismo(5, 205.dp, 320.dp)
//    }
//
//}


@Composable
fun cartas_turismo(
    titulo: String,
    texto: String,
    img: String,
    rounder: Int,
    alto: Dp,
    ancho: Dp,
    listener: () -> Unit
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
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(ancho)
                .height(alto)
                .clip(RoundedCornerShape(rounder)),
            contentScale = ContentScale.Crop
        )

        mascara_img(rounder, alto, ancho)
        texto_encimado_cartas(
            modifier = Modifier.align(Alignment.BottomStart),
            titulo,
            texto,
            listener
        )

    }
}

@Composable
fun texto_encimado_cartas(
    modifier: Modifier,
    titulo: String,
    descripcion: String,
    listener: () -> Unit
) {
    Row(modifier = modifier.padding(start = 10.dp, end = 20.dp, bottom = 30.dp)) {
        Column {
            texto_generico_multilinea(
                titulo,
                MaterialTheme.typography.titleLarge
            )
            spacer_vertical(5.dp)
            TextoSubrayado(
                descripcion,
                MaterialTheme.typography.bodySmall,
            )
        }

    }

}


@Composable
fun texto_encimado(modifier: Modifier, localidad: String) {
    Column(modifier = modifier.padding(10.dp)) {
        texto_generico_multilinea(
            "Encuentra Rutas de turismo en $localidad",
            MaterialTheme.typography.headlineSmall
        )
        spacer_vertical(10.dp)
        Button(onClick = {}, modifier = Modifier.clip(RoundedCornerShape(50))) {
            texto_generico_one_line("Ver rutas")
        }
    }

}

@Composable
fun mascara_img(rounder: Int, alto: Dp, ancho: Dp) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(rounder))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black
                    )
                )
            )
            .width(ancho)
            .height(alto)
    )
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
        ) {
            texto_generico_one_line(
                texto = "Hola $nombre_user",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            texto_generico_one_line(
                texto = "A donde quieres llegar ?",
                MaterialTheme.typography.headlineSmall
            )
        }
        Image(
            painter = painterResource(R.drawable.cargar_foto_500x500),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentDescription = ""
        )
//        AsyncImage(
//            model = "",
//            contentDescription = "Imagen de la tienda",
//            contentScale = ContentScale.Crop,
//            error = painterResource(R.drawable.qr_yape),
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp)
//                .clip(RoundedCornerShape(50.dp)),
//
//            )

    }


}

@Composable
fun MyGoogle_maps() {
    val viewModel_cordenadas: viewModel_filtado_tiendas = viewModel()
    val cordenadas by viewModel_cordenadas._obtener_datos_tienda.observeAsState(emptyList())
    val datosTienda by viewModel_cordenadas._datos_tienda.observeAsState(emptyList())
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var id_tienda = remember { mutableStateOf("") }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true // Esto activa el círculo azul y la flecha
            )
        )
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            com.google.android.gms.maps.model.LatLng(
                -10.747981,
                -77.754218
            ), 15f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = properties,
    ) {
        cordenadas.forEach { tienda ->
            Marker(
                state = MarkerState(
                    com.google.android.gms.maps.model.LatLng(
                        tienda.lat,
                        tienda.log
                    )
                ),
                title = "${tienda.nombre_tienda}",
                snippet = "Dirección: ${tienda.direccion}\nReferencia: ${tienda.referencia}",
                onClick = {
                    id_tienda.value = tienda.id_tienda
                    Log.d("MarkerClick", "Marcador ${tienda.id_tienda} tocado")
                    show_bottom_sheeet = true
                    false
                }
            )
        }
    }
    LaunchedEffect(datosTienda) {
        if (datosTienda.isNotEmpty()) {
            Log.d("obtenoemos_datos_tienda", datosTienda.toString())
            dataclass_tienda_seleccionada = datosTienda.first()
        }
    }

    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModel_cordenadas.obtener_campos_tiendas_por_id("barranca", id_tienda.value)
        }
    }


    LaunchedEffect(Unit) {
        viewModel_cordenadas.obtener_tiendas_registradas("barranca")
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location =
                fusedLocationClient.lastLocation.await()
            location?.let {
                val userLatLng = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                )
            }
        }
    }
    if (show_bottom_sheeet) {
        bottom_sheet_tiendas_filtradas(
            Color.Red,
            viewModel_cordenadas,
            dataclass_tienda_seleccionada, show_bottom_sheeet
        ) {
            show_bottom_sheeet = false
        }
    }
}

package com.geinzz.geinzwork.ui.adapters.ui.principal

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_filtrado_localidades
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.localidad_Selecionada
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.mascara_img
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.rutas_turismo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulo_referenciales_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textosTituloGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


private lateinit var firebaseAuth: FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pantalla_principal(
    categorias: (localidad: String, nombre_user: String) -> Unit,
    ver_lugares: () -> Unit,
    navController: NavController
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    val _categorias_tiendas by viewModel_cordenadas._sub_cat_tiendas.observeAsState(emptyList())
    val datos_user by viewModel_cordenadas.userData.observeAsState(null)
    val _obtener_filtrado_localidades by viewModel_cordenadas._lista_filtrado_localidades.observeAsState(
        emptyList()
    )
    var nombre_user: String by rememberSaveable { mutableStateOf("") }
    var img_perfil by rememberSaveable { mutableStateOf("") }

    if (firebaseAuth.currentUser != null) {
        nombre_user = datos_user?.nombre ?: "Usuario"
        img_perfil = datos_user?.img_perfil ?: ""
    } else {
        nombre_user = "Usuario"

        img_perfil = ""
    }


    LaunchedEffect(Unit) {
        viewModel_cordenadas.lugares_turisticos("barranca")
        viewModel_cordenadas.obtener_subcategorias()
        viewModel_cordenadas.obtner_filtrado_localidades()
        viewModel_cordenadas.obtener_datos_user_registrado(firebaseAuth.uid.toString())
    }

    val ultimaLocalidad by data_store_localidad
        .obtener_localidad(context)
        .map { valor ->
            if (valor.isNullOrEmpty()) "barranca" else valor
        }
        .collectAsState(initial = "barranca")


    val localidadSeleccionada = remember { mutableStateOf("barranca") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
    ) {
        item {
            nombre_texto_img_perfil(nombre_user, img_perfil)
        }
        stickyHeader() {
            ColumnContenedorComun {
                texfiel_filtrado()
            }
        }
        item {
            filtrado_localidades(
                ultimaLocalidad, _obtener_filtrado_localidades
            ) { localidad_selecionada ->
                localidadSeleccionada.value = localidad_selecionada
            }
            spacer_vertical(10.dp)
        }
        item {
            apartado_explora_cat(
                _categorias_tiendas,
                ultimaLocalidad,
                nombre_user,
            ) { nombre, localidad ->
                categorias(localidad, nombre)
            }
            spacer_vertical(10.dp)
        }

        item {
            rutas_turismo(
                "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0159.00_00_00_00.Imagen%20fija002.webp?alt=media&token=c4c60311-1293-4731-b2e4-c51265c15860",
                "ver rutas",
                "descubre ${ultimaLocalidad}"
            ) { ver_lugares() }
            spacer_vertical(10.dp)
        }

        item {
            rutas_turismo(
                "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/geinz_work_turismo%2Fbarranca%2Flugares_turisticos%2FDJI_0593.webp?alt=media&token=8e770a68-dfad-4ae1-8d20-c9133e2f4a49",
                "ver eventos",
                "Eventos proximos de ${ultimaLocalidad}"
            ) {}
            spacer_vertical(20.dp)
        }
    }
}

@Composable
fun apartado_explora_cat(
    categorias_tienda: List<dataclass_cat_sub>,
    localidad_selecionada: String?,
    nombre_user: String,
    categorias1: (String, String) -> Unit,

    ) {
    Log.d("obtemloms_lista", categorias_tienda.toString())
    val categoriasPrincipales = remember {
        categorias_tienda.shuffled().take(5)
    }
    spacer_vertical(10.dp)
    Column {
        titulo_referenciales_geinz_work(
            "Explora $localidad_selecionada",
            "Ver todos"
        ) { categorias1(nombre_user, localidad_selecionada ?: "barranca") }
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
                    200.dp,
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Column () {
                    texto_categorias(nombre_categoria)
                    spacer_vertical(10.dp)
                    tags_subcateogiras(lista_subcateogiras)

                }

            }


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
//    modifier: Modifier,
    nombre_categoria: String,

) {
//    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        texto_generico_one_line(
            nombre_categoria.uppercase(), MaterialTheme.typography.titleSmall,
            Modifier
//                .weight(1f)
                .padding(end = 10.dp)
        )
//        FloatingActionButton(
//            modifier = Modifier
//                .size(30.dp)
//                .clip(CircleShape),
//            onClick = { onClickExpand() },
//            elevation = FloatingActionButtonDefaults.elevation(
//                defaultElevation = 6.dp,
//                pressedElevation = 10.dp
//            ),
//            containerColor = MaterialTheme.colorScheme.primary,
//        ) {
//            Image(
//                modifier = Modifier.size(20.dp),
//                painter = painterResource(
//                    constantes_lista_localidades.cambiar_icono_exapndible(
//                        expandido
//                    )
//                ),
//                contentDescription = "",
//                colorFilter = ColorFilter.tint(Color.White)
//            )
//        }
//    }
}


@Composable
fun texto_encimado_cartas(
    defecto_selecionado: Boolean,
    modifier: Modifier,
    titulo: String,
    descripcion: String,
) {
    Row(modifier = modifier.padding(start = 20.dp, end = 20.dp, bottom = 40.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                texto_generico_multilinea(
                    titulo,
                    MaterialTheme.typography.titleMedium
                )
                spacer_horizonta(5.dp)
                AnimatedVisibility(
                    defecto_selecionado, enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    localidad_Selecionada()
                }
            }
            spacer_vertical(5.dp)
            Crossfade(
                targetState = descripcion,
                animationSpec = tween(durationMillis = 500) // O la duración que prefieras
            ) { textoAnimado ->
                TextoSubrayado(
                    textoAnimado.uppercase(), // Usamos el texto animado y lo convertimos a mayúsculas
                    MaterialTheme.typography.bodySmall, modifier = Modifier
                )
            }
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


@Composable
fun filtrado_localidades(
    ultimaLocalidad: String?,
    lista_localidades: List<localidades_filtrado>,
    nombre_localidad_selecionado: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var localidad_defecto by remember { mutableStateOf(ultimaLocalidad) }

    LaunchedEffect(ultimaLocalidad) {
        localidad_defecto = ultimaLocalidad
    }



    Column {
        spacer_vertical(10.dp)
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista_localidades, key = { it.nombre }) { items ->
                carta_filtrado_localidades(
                    defecto_selecionado = localidad_defecto.equals(items.nombre, ignoreCase = true),
                    items.nombre,
                    items.lista_img,
                    5,
                    300.dp,
                    300.dp
                ) { nombre_localidad ->
                    localidad_defecto = nombre_localidad
                    nombre_localidad_selecionado(nombre_localidad)
                    scope.launch {
                        data_store_localidad.guardar_localida(context, nombre_localidad)
                    }
                }
            }
        }
    }
}


@Composable
fun nombre_texto_img_perfil(nombre_user: String, img_url: String = "") {
    val fraces = constantes_lista_localidades.lista_fraces_inicio
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 10.dp, top = 10.dp)
        ) {
            texto_generico_one_line(
                texto = constantes_lista_localidades.saludo_user_principal(nombre_user),
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            Crossfade(targetState = fraces[index], label = "fraces") { txt ->
                texto_generico_one_line(
                    texto = txt,
                    MaterialTheme.typography.busquedaGeinzWork
                )
            }
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img_url)
                .size(40)
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}




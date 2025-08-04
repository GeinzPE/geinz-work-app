package com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas

import android.content.Intent
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import android.util.Log
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Icon
import com.geinzz.geinzwork.R
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.abrirRutaEnGoogleMaps

import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.verificarUbiActiva
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.amarillo30
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale


@Composable
fun Pantalla_filtrado_tiendas(
    categoria: String,
    localida: String, navigation_regresar: () -> Unit,
) {

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        var texto_filtrado by rememberSaveable { mutableStateOf("") }
        val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
        val tiendas_filtradas by viewModelFiltros._tiendas_filtradas.observeAsState(emptyList())
        val datos_tiendas by viewModelFiltros._datos_tienda.observeAsState(emptyList())
        val horario_tienda by viewModelFiltros._horario_tienda.observeAsState(emptyList())
        var showBottomSheet by remember { mutableStateOf(false) }
        var id_tienda_selecionada by remember { mutableStateOf("") }
        var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }

        LaunchedEffect(id_tienda_selecionada, datos_tiendas) {
            viewModelFiltros.obtener_horario_por_tienda(localida, id_tienda_selecionada)
            Log.d("el_horario_de_alat", horario_tienda.toString())
            Log.d("obtenemos_iud_iten", id_tienda_selecionada)
            viewModelFiltros.obtener_campos_tiendas_por_id(localida, id_tienda_selecionada)
            Log.d("datos_teinda", datos_tiendas.toString())
            datos_tiendas.find { it.id_tienda == id_tienda_selecionada }?.let {
                dataclass_tienda_seleccionada = it
            }
        }


        LaunchedEffect(localida, categoria) {
            viewModelFiltros.obtener_tiendas_filtradas(localida, categoria)
        }
        LaunchedEffect(tiendas_filtradas) {
            Log.d("tiendas_encontradas", tiendas_filtradas.toString())
        }
        LazyColumn() {
            item {
                chips_categorias(
                    texto_filtrado,
                    categoria,
                    localida,
                    innerPadding,
                    { texto_filtrado = it },
                    { navigation_regresar })

            }
            items(tiendas_filtradas) { tiendas ->
                item_tiendas(tiendas) { id_tienda, listener ->
                    showBottomSheet = listener
                    id_tienda_selecionada = id_tienda
                }
            }
        }
        if (showBottomSheet) {
            bottom_sheet_tiendas(
                viewModelFiltros,
                dataclass_tienda_seleccionada,
            ) {
                showBottomSheet = false
            }
        }

    }
}


@Composable
fun chips_categorias(
    texto_filtrado_txt: String,
    categoria: String,
    localida: String,
    innerPadding: PaddingValues,
    texto_filtrado: (String) -> Unit,
    navigation_regresar: () -> Unit
) {
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val subcategoriaObjs by viewModelFiltros._subcategoiraList.observeAsState(emptyList())
    viewModelFiltros.obtener_subcategorias(categoria)
    val subcategorias: List<String> = subcategoriaObjs.flatMap { it.subcategorias }

    var sub_categoria_selecionada by remember { mutableStateOf<String?>(null) }
    var visible_texfiel by remember { mutableStateOf(false) }
    var cargando_progress by remember { mutableStateOf(false) }
    LaunchedEffect(texto_filtrado_txt) {
        if (texto_filtrado_txt.isNotEmpty()) {
            cargando_progress = true
            delay(1000) // Simula llamada a servidor
            cargando_progress = false
        }

    }

    Box(modifier = Modifier.padding(innerPadding)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {

            item {
                Text(" $categoria $localida")

            }
            item {
                Text("Busca tus tiendas favoritas")
                Text("filtra por tus cateogiras y el nombre de tus tiendas favoritas")
            }
            item {
                LazyRow() {
                    items(subcategorias) { subcategorias ->
                        val selecionado = sub_categoria_selecionada == subcategorias
                        FilterChip(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            selected = selecionado,
                            onClick = {
                                sub_categoria_selecionada = if (selecionado) null else subcategorias
                                visible_texfiel = true
                            },
                            label = {
                                Text(text = subcategorias.toString())
                            },
                            shape = RoundedCornerShape(50)
                        )

                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = visible_texfiel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = texto_filtrado_txt,
                        onValueChange = {
                            texto_filtrado(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("Ingresa el nombre de la tienda") },
                        placeholder = { Text("Ingresa el nombre") },
                        trailingIcon = {
                            if (cargando_progress) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(4.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                    )
                }
            }

            item {
                encontradas_activas("Tiendas activas", "10", R.drawable.icon_tienda_icon_general)
                Spacer(modifier = Modifier.height(20.dp))
                encontradas_activas("Tiendas registradas", "20", R.drawable.icon_tiendas)

//                Button(onClick = {agregar_tiendas("barranca",lista_agregar_tiendas_brca)}) {Text("clikear")}
            }

        }
    }
}

@Composable
fun encontradas_activas(texto1: String, texto2: String, @DrawableRes icono: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(icono),
            contentDescription = "",
            modifier = Modifier.size(20.dp)
        )
        Text(texto1)
        Text(texto2)
    }
}

@Composable
fun item_tiendas(
    item_tiendas: tiendas_filtradas,
    listener_botom_sheet: (id_tienda: String, showBottomSheet: Boolean) -> Unit
) {
    var detalles_tienda by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .clickable {
                listener_botom_sheet(item_tiendas.id_tienda, true)
            },
        colors = CardDefaults.cardColors(
            containerColor = amarillo30
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 7.dp, vertical = 7.dp)
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                AsyncImage(
                    model = item_tiendas.img_tiendas,
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
                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas(
                        "Direccion :", item_tiendas.direccion
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                    Caracteristicas_tiendas("Referencia : ", item_tiendas.referencia)
                    Spacer(modifier = Modifier.height(5.dp))
//                    kilometros_cerca()
                }
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
//                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Subcategorias a la cual pertenece ${item_tiendas.lista_subcategoiras.toString()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
//                        style = MaterialTheme.typography.bodySmall,
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
                modifier = Modifier
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
        Text(text = caracteristica)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = texto, modifier = Modifier, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun kilometros_cerca() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color = amarillo30)
    ) {
        Row(modifier = Modifier.padding(horizontal = 5.dp)) {
            Text(text = "Distancia cerca : ")
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "1Kl", modifier = Modifier, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

}

@Composable
fun Abrir_google_maps() {
    FloatingActionButton(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape), onClick = {}, elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 10.dp,
            pressedElevation = 10.dp
        )
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.localidad_icon_general),
            contentDescription = ""
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
            .size(30.dp)
            .clip(CircleShape),
        onClick = {
            expandida_carta = !expandida_carta  // alternar estado
            expandir_carta(expandida_carta)
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 10.dp,
            pressedElevation = 10.dp
        )
    ) {
        Image(
            modifier = Modifier.size(15.dp),
            painter = painterResource(icono_cambiado),
            contentDescription = ""
        )
    }

}


fun agregar_tiendas(localidad: String, listadatos: List<modelo_tienda>) {
    val db = FirebaseFirestore.getInstance().collection("Tiendas").document(localidad)
        .collection(localidad)

    listadatos.forEach { i ->
        val id = i.id_tienda

        db.document(id).set(i)
            .addOnSuccessListener {
                Log.d("Firestore", "Tienda agregada correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al agregar la tienda ", e)
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_tiendas(
    viewModelFiltros: viewModel_filtado_tiendas,
    tiendas_filtradas: modelo_tienda,
    onClose: () -> Unit
) {
    var expandir_descripcion by rememberSaveable { mutableStateOf(false) }
    var expander_caracterisiticas by rememberSaveable { mutableStateOf(false) }
    var expander_horario by rememberSaveable { mutableStateOf(false) }
    val direccion = tiendas_filtradas.ubicacion["dirección"]?.toString() ?: ""
    val referencia = tiendas_filtradas.ubicacion["referencia"]?.toString() ?: ""
    Surface {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            dragHandle = null
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.80f)
                    .padding(10.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.LightGray)
                        )
                    }
                }
                item {
                    cabezero_tiendas(tiendas_filtradas)
                    spacer_vertical(20.dp)
                }
                item {
                    Text(
                        text = "Acerca de la tienda",
                        modifier = Modifier,
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Normal
                    )
                    spacer_vertical(10.dp)
                }
                item {
                    Expandible_descripcion_tienda(
                        tiendas_filtradas,
                        expander_caracterisiticas
                    ) { expander_caracterisiticas = !expander_caracterisiticas }
                    spacer_vertical(10.dp)
                }
                item {


                    if (direccion.isNotBlank() || referencia.isNotBlank()) {
                        val fisica_virtual =
                            if (tiendas_filtradas.modelo_negocio) "Fisica" else "Virtual"

                        Column(modifier = Modifier.animateContentSize()) {
                            Expandible_direccion_ref(
                                direccion,
                                referencia,
                                fisica_virtual,
                                expandir_descripcion
                            ) {
                                expandir_descripcion = !expandir_descripcion
                            }
                        }
                    }

                    spacer_vertical(10.dp)
                }
                item {
                    Expandible_horario_atencion(
                        tiendas_filtradas.localidad,
                        tiendas_filtradas.id_tienda,
                        expander_horario,
                        viewModelFiltros
                    ) { expander_horario = !expander_horario }
                    spacer_vertical(10.dp)
                }

            }
        }
    }
}

//@Preview
@Composable
fun cabezero_tiendas(tiendas_filtradas: modelo_tienda) {
    val context = LocalContext.current
    val mostrarDialogo = remember { mutableStateOf(false) }
    val mostrarDialog_sin_google_maps = remember { mutableStateOf(false) }
    val direccion = tiendas_filtradas.ubicacion["dirección"]?.toString() ?: ""
    val referencia = tiendas_filtradas.ubicacion["referencia"]?.toString() ?: ""
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
            }
        )
    }
    if (mostrarDialog_sin_google_maps.value) {
        dialog_sin_ubi_activa(
            direccion, referencia, onDismis = { mostrarDialog_sin_google_maps.value = false },
            abrir_maps = { constantes.abrirGoogleMaps(context, direccion) })

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = "https://via.placeholder.com/300",
            contentDescription = "Imagen de la tienda",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
            error = painterResource(id = R.drawable.qr_yape),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Column(modifier = Modifier.weight(1f)) {
                val iconId = "icon"

                val annotatedText = buildAnnotatedString {
                    append(tiendas_filtradas.nombre_tienda)
                    append(" ")
                    appendInlineContent(iconId, "[icon]")
                }

                val inlineContent = mapOf(
                    iconId to InlineTextContent(
                        Placeholder(
                            width = 20.sp,
                            height = 20.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_tienda_icon_general),
                            contentDescription = "Icono tienda",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )

                Text(
                    text = annotatedText,
                    inlineContent = inlineContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    style = TextStyle(fontSize = 16.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = tiendas_filtradas.categoria_tienda,
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(40f))
                        .background(amarillo30)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cerca de ti")
                    Spacer(modifier = Modifier.width(5.dp))
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.localidad_icon_general),
                        contentDescription = ""
                    )
                }

            }
            spacer_horizonta(10.dp)
            val latitud =
                tiendas_filtradas.ubicacion["latitud"]?.toString()?.toDoubleOrNull() ?: 0.0
            val longitud =
                tiendas_filtradas.ubicacion["longitud"]?.toString()?.toDoubleOrNull() ?: 0.0
            FloatingActionButton(
                onClick = { abrir_google_maps(context, latitud, longitud) { dialogo ->
                    mostrarDialogo.value=dialogo
                    if(mostrarDialogo.value){
                        abrir_google_maps(context, latitud, longitud) { dialogo ->
                            mostrarDialogo.value = dialogo
                        }
                    }
                } },
                modifier = Modifier.size(40.dp),
                containerColor = Color(0xFFFFC107),
                contentColor = Color.White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.localidad_icon_general),
                    contentDescription = "Localidad",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun abrir_google_maps(
    context: android.content.Context,
    latitud: Double,
    longitud: Double,
    mostrar_dialog: (Boolean) -> Unit
) {
    if (verificarUbiActiva(context)) {
        abrirRutaEnGoogleMaps(context, latitud, longitud)
    } else {
        mostrar_dialog(true)
    }
}


@Composable
fun Expandible_descripcion_tienda(
    item_tiendas: modelo_tienda,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = amarillo30
        )
    ) {
        Column() {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp)
            ) {

                val (texto, btn) = createRefs()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .constrainAs(texto) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.descripcion_tienda_vector),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descripcion de tienda")
                }
                FloatingActionButton(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .constrainAs(btn) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    onClick = { onClickExpand() },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    )
                ) {

                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ocultar_abajo), contentDescription = ""
                    )
                }
            }
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(10.dp)
                ) {
                    Text(
                        text = item_tiendas.descripcion,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

}

@Composable
fun Expandible_direccion_ref(
    direccion: String,
    referencia: String,
    fisica_virtual: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = amarillo30
        )
    ) {
        Column {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp)
            ) {
                val (texto, btn) = createRefs()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.constrainAs(texto) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.location_drawable),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dirección y referencia",
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
                FloatingActionButton(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .constrainAs(btn) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    onClick = { onClickExpand() },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    )
                ) {
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ocultar_abajo),
                        contentDescription = ""
                    )
                }
            }

            AnimatedVisibility(
                visible = expandido,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ) // Importante para no tocar bordes directamente
                ) {
                    Text(text = "Dirección: $direccion")
                    Text(text = "Referencia: $referencia")
                    Text(text = "Tipo de tienda: $fisica_virtual")
                }
            }
        }
    }
}


@Composable
fun Expandible_horario_atencion(
    idTienda: String?,
    id_tienda: String,
    expandido: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    onClickExpand: () -> Unit
) {
    val horario_tienda = viewModelFiltros._horario_tienda.observeAsState(emptyList())
    var cargado by remember { mutableStateOf(false) }

    LaunchedEffect(expandido) {
        if (expandido && !cargado) {
            viewModelFiltros.obtener_horario_por_tienda(idTienda!!, id_tienda)
            cargado = true
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = amarillo30
        )
    ) {
        Column() {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp)
            ) {
                val (texto, btn) = createRefs()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .constrainAs(texto) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.horario_tienda_vector),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Horario de atención",
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
                FloatingActionButton(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .constrainAs(btn) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    onClick = { onClickExpand() },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    )
                ) {
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ocultar_abajo), contentDescription = ""
                    )
                }
            }
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {

                    horario_tienda.value.forEach { i ->
                        val esDiaActual = obtenerDiaActualEnEspañol() == i.dia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val horario_abierto_cerrado =
                                if (i.h_apertura.isNotEmpty() && i.h_cierre.isNotEmpty()) "${i.h_apertura} am a ${i.h_cierre} pm " else "Cerrando"
                            Text(
                                text = "${i.dia} : $horario_abierto_cerrado",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .weight(1f),
                                overflow = TextOverflow.Ellipsis, color = Color.Black
                            )
                            if (esDiaActual) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.guardados_icon),
                                    contentDescription = ""
                                )
                            }

                            spacer_horizonta(15.dp)
                        }
                    }


                }
            }
        }

    }

}

fun obtenerDiaActualEnEspañol(): String {
    val locale = Locale("es", "ES")
    val calendar = Calendar.getInstance()
    val diaSemana = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)
    return diaSemana?.lowercase() ?: ""
}


//@Composable
//fun Expandible_generar_qr() {
//    ExtendedFloatingActionButton(
//        onClick = {}, shape = RoundedCornerShape(20.dp)
//    ) {
//        Text("Generar QR")
//        spacer_horizonta(10.dp)
//        Icon(painter = painterResource(R.drawable.qr_scaner_icon), contentDescription = "")
//
//    }
//}




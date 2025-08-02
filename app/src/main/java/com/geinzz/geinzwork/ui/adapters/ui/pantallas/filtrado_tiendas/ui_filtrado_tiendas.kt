package com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.amarillo30
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay


@Composable
fun Pantalla_filtrado_tiendas(
    categoria: String,
    localida: String, navigation_regresar: () -> Unit,
) {

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        var texto_filtrado by rememberSaveable { mutableStateOf("") }
        val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
        val tiendas_filtradas by viewModelFiltros._tiendas_filtradas.observeAsState(emptyList())
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
                item_tiendas(tiendas)


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
fun item_tiendas(item_tiendas: tiendas_filtradas) {
    var detalles_tiend by remember { mutableStateOf(false) }
    val local_context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .clickable {
                Toast.makeText(
                    local_context,
                    item_tiendas.nombre_tienda,
                    Toast.LENGTH_SHORT
                ).show()
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
                    Btn_Expandir_card { expandir -> detalles_tiend = expandir }
                }
            }
            AnimatedVisibility(visible = detalles_tiend) {
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

//        Row(
//            modifier = Modifier
//                .clip(RoundedCornerShape(40f))
//                .background(amarillo30)
//                .padding(horizontal = 5.dp, vertical = 2.dp)
//                .weight(1f),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text("Cerca de ti")
//            Spacer(modifier = Modifier.width(5.dp))
//            Image(
//                modifier = Modifier.size(20.dp),
//                painter = painterResource(R.drawable.localidad_icon_general),
//                contentDescription = ""
//            )
//        }
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

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun bottom_sheet_tiendas() {
    Surface {
        ModalBottomSheet(
            onDismissRequest = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                item {
                    cabezero_tiendas()
                    spacer_vertical(10.dp)
                }

                item {
                    Acerca_tienda()
                    spacer_vertical(10.dp)
                }

                item {
                    horario_atencion()
                    spacer_vertical(10.dp)
                }
            }
        }
    }
}

@Composable
fun cabezero_tiendas() {
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        AsyncImage(
            model = "https://via.placeholder.com/300", // imagen temporal
            contentDescription = "Imagen de la tienda",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
            error = painterResource(id = R.drawable.qr_yape),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Verdulería Marcos",
                )
                Text(
                    text = "Categoría: Verdulería",
                )
            }

            Icon(
                painter = painterResource(R.drawable.icon_tienda_icon_general),
                contentDescription = "Icono tienda",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )

            FloatingActionButton(
                onClick = { /* Acción */ },
                modifier = Modifier.size(36.dp),
                containerColor = Color(0xFFFFC107), // reemplaza amarillo30 si no está
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

@Composable
fun Acerca_tienda() {
    Text(text = "Acerca de la tienda")
    Text(text = "calle localizada : urb san mateo mz i lote 4")
    Text(text = "referencia : cerna a la loza san mteo")
    Text(text = "Distancia cercana : 4ML")
    Text(text = "Tipo de tienda :Fisica")
}

@Composable
fun horario_atencion() {
    Card(
        modifier = Modifier
            .height(40.dp),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = amarillo30,
        ),

    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth(),) {
            val (texto,btn)=createRefs()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp).constrainAs(texto){

                }
            ) {
                Text(
                    text = "Horario de atención",
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.horario_tienda_vector),
                    contentDescription = ""
                )
            }
            FloatingActionButton(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape).constrainAs(btn){
                        end.linkTo(parent.end)
                    },
                onClick = {},
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



    }

}




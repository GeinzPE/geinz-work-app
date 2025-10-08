package com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.algolia.search.dsl.attributes.DSLSearchableAttributes
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_servicios_tramite
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite

@Composable
fun ui_servicio_tramite(localida: String) {
    val viewmode_servicios_tramite: viewmode_servicios_tramite = viewModel()
    val lugares = viewmode_servicios_tramite.lugares.observeAsState(emptyList())
    val lista_servicios = constantes_lista_localidades.lista_fitlrado_servicios_basicos
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var dialog_servicos_tramite by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf(dataclass_lugares_db()) }
    var expandedIndex by remember { mutableStateOf(-1) }
    var lista_mostrar by remember { mutableStateOf<List<dataclass_lugares_db>>(emptyList()) }
    LaunchedEffect(lugares.value) {
        viewmode_servicios_tramite.todos(lugares.value)
    }
    LaunchedEffect(localida) {
        viewmode_servicios_tramite.obtener_lugares(localida)
    }

    LaunchedEffect(lugares.value, subCategoriaSeleccionada) {
        viewmode_servicios_tramite.todos(lugares.value)
        if (subCategoriaSeleccionada == "Todos") {
            lista_mostrar = lugares.value
        } else {
            viewmode_servicios_tramite.filtrar_por_categoria(subCategoriaSeleccionada)
            lista_mostrar = viewmode_servicios_tramite.listaFiltrada.value
        }
        Log.d("lista_value", subCategoriaSeleccionada)
    }


    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Column() {
                cabezero_servicios_tramites(localida)
                spacer_vertical(10.dp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(lista_servicios) { it ->
                        val catSeleccionada = subCategoriaSeleccionada == it
                        Log.d("asnfaBNFIKBNFIDNASUIF","$subCategoriaSeleccionada == $it")
                        chisp_filtrado_busqueda(catSeleccionada, it, false, {
                            if (!catSeleccionada) {
                                if (it == "Todos") {
                                    subCategoriaSeleccionada = "Todos"
                                }else{
                                    subCategoriaSeleccionada=it
                                }
                            }

                        }, {})
                    }
                }
                spacer_vertical(10.dp)
            }
        }
        itemsIndexed(lista_mostrar, key = { _, item -> item.id }) { index, lugar ->
            carta_servicio_tramites(
                lugar,
                index,
                isExpanded = expandedIndex == index
            ) {
                seleccionado = lugar
//                expandedIndex = if (expandedIndex == index) -1 else index
                dialog_servicos_tramite = true
            }
        }
    }
    if (dialog_servicos_tramite) {
        dialog_servicios_tramite(ondimis = { dialog_servicos_tramite = false }, seleccionado)
    }

}

@Composable
fun carta_servicio_tramites(
    dataclass_lugares_db: dataclass_lugares_db,
    index: Int,
    isExpanded: Boolean,
    click_card: () -> Unit
) {
    val heightOptions = listOf(200.dp, 210.dp)
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]

    val gradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.95f),
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(20.dp))
            .clickable { click_card() }
    ) {
        // 🖼 Imagen principal
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(dataclass_lugares_db.logo_img)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = {
                Image(
                    painter = painterResource(R.drawable.cargando_img_categorias),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )

//        // 🌈 Degradado inferior
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter)
//                .background(gradient)
//        ) {
//            Column(modifier = Modifier.padding(8.dp)) {
//                texto_generico_one_line(dataclass_lugares_db.lugar_nombre.capitalizeFirst())
//            }
//        }

        // 🟢 Overlay cuando está expandido (no cambia la altura)
        AnimatedVisibility(
            visible = isExpanded,
            modifier = Modifier
                .matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clip(RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    texto_generico_one_line("Detalles del servicio")
                    spacer_vertical(5.dp)
                    texto_generico_one_line(dataclass_lugares_db.lugar_nombre.capitalizeFirst())
                }
            }
        }

//        Box(
//            modifier = Modifier   .padding(7.dp)
//                .size(35.dp)
//                .clip(CircleShape)
//                .background(MaterialTheme.colorScheme.primary)
//                .padding(9.dp)
//                .align(Alignment.BottomEnd),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.Visibility ,
//                contentDescription = "Ver u ocultar",
//                tint = Color.White
//            )
//        }
    }
}

@Composable
fun cabezero_servicios_tramites(localiad: String) {
    Text(text = "servicios esenciales y tramites", fontFamily = baners_geinz_work, fontSize = 25.sp)
    spacer_vertical(5.dp)
    texto_generico_multilinea(
        "Accede al instante a todos los servicios y trámites esenciales de $localiad. Información verificada.",
        MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 10.dp)
    )

}
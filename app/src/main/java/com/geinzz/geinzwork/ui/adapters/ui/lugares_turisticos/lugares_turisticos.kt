package com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos

import android.widget.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos_maps
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.mascara_img
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.principal.texto_encimado_cartas
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work

@Composable
fun pantalla_lugares_turisticos(abrir_mapa: (String) -> Unit) {
    val contex = LocalContext.current
    val viewModel_cordenadas: viewModel_principal_geinz_work = viewModel()
    var permiso_location_aceptado by remember { mutableStateOf(false) }
    val _lugares_turisticos by viewModel_cordenadas._lugares_turisticos.observeAsState(emptyList())
    LaunchedEffect(Unit) {
        viewModel_cordenadas.lugares_turisticos("barranca")
    }
    val permisos =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { graden ->
            if (graden) {
                permiso_location_aceptado = true
            } else {
                permiso_location_aceptado = false
            }
        }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(_lugares_turisticos) { lugares ->
                    carta_lugares_turisticosa(200.dp, 10, lugares)
                }
            }  open_map_perzonlizado(abrir_mapa)

        }
    }
}

@Composable
fun carta_lugares_turisticosa(alto: Dp, rounder: Int, lugar: lugares_turisticos) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Box(
        modifier = Modifier
            .width(screenWidth)
            .height(alto)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(lugar.img_ref)
                .size(screenWidth.value.toInt(), alto.value.toInt())
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(screenWidth)
                .height(alto)
                .clip(RoundedCornerShape(rounder)),
//                .clickable {
//                    listener(true, lugar)
//                },
            contentScale = ContentScale.Crop
        )

        mascara_img(rounder, alto, screenWidth)
        texto_generico_one_line(lugar.titulo)
    }
}


@Composable
fun open_map_perzonlizado(abrir_mapa: (String) -> Unit) {
    androidx.compose.material3.Button(onClick = { abrir_mapa("turismo") }) { Text("ver mapa") }
}

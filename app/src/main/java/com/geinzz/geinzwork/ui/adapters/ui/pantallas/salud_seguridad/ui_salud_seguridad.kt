package com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud


@Composable
fun ui_salud_seguirdad(localida: String) {
    val viewmodel_seguridad_salud: viewmode_seguridad_salud = viewModel()
    val lista_seguridad_salud by viewmodel_seguridad_salud._datos_lugares.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewmodel_seguridad_salud.obtener_servicios(localida)
    }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn {
            items(lista_seguridad_salud) { i ->
                Box(modifier = Modifier.padding(8.dp)) {
                    carta_salud_cuidad(i)
                }
            }
        }

    }
}

@Composable
fun carta_salud_cuidad(i: dataclass_seguridad) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surface)
            .padding(5.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(i.img_ref)
                .size(300, 100).placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(5.dp)) {
            texto_generico_one_line(
                i.nombre_,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            spacer_vertical(5.dp)
            texto_generico_one_line("direccion : ${i.direccion}", color = Color.White)
            spacer_vertical(5.dp)
            texto_generico_one_line("Abierto", color = Color.White)
            spacer_vertical(10.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                BtnCirculares(R.drawable.llamada_icon, fondo = MaterialTheme.colorScheme.primary)
                BtnCirculares(R.drawable.whatsapp_icon)
                BtnCirculares(R.drawable.vector_ruta_icon, fondo = MaterialTheme.colorScheme.primary)
                BtnCirculares(Icons.Default.Map, fondo = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
@Composable
fun BtnCirculares(
    icono: Any, // puede ser Int (drawable) o ImageVector
    fondo: Color = Color.Transparent,
    size: Dp = 32.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        when (icono) {
            is Int -> Image(
                painter = painterResource(id = icono),
                contentDescription = null,
            )

            is ImageVector -> Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = tint
            )
        }
    }
}

package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.geinzz.geinzwork.R
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.chips_filtrado
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_lugares_turisticos(
    datos: lugares_turisticos,
    onClose: () -> Unit,
) {
    Surface {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background

        ) {
            card_img_container(datos)
        }
    }
}


@Composable
fun card_img_container(datos: lugares_turisticos) {
    var dialogo_ubi_enable by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {

        spacer_vertical(10.dp)
        CollageGoogleMapsStyle(aspectRatio = 1.1f, with = 310.dp, imagenes = datos.lista_img)
        spacer_vertical(10.dp)
        val lista_datos=listOf("Crear ruta","compartir","guardar","ver en mapa")
        chips_filtrado(lista_datos)
        spacer_vertical(10.dp)
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically){

                Text(text = datos.titulo, fontFamily = baners_geinz_work, fontSize = 25.sp)
                spacer_horizonta(10.dp)
                abierto_flag("Abierto las 24h")
            }
            spacer_vertical(10.dp)

            texto_generico_multilinea(
                datos.descripcion,
                MaterialTheme.typography.bodyMedium,
            )
        }
        spacer_vertical(10.dp)


//        buttom_open_map("Crear ruta") {
//            constantes_lista_localidades.abrir_google_maps(
//                context,
//                datos.latitud,
//                datos.longitud
//            ) { dialogo ->
//                dialogo_ubi_enable = dialogo
//            }
//        }
        Log.d("obtemos_cordenads", "${datos.latitud}, ${datos.longitud}")
//        buttom_open_map("Mostrar mapa") {map_personalizado(datos.titulo,datos.latitud,datos.longitud)}
    }
}

@Composable
fun chips_filtrado(list: List<String>){
    LazyRow (horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, contentPadding = PaddingValues(
        start = 7.dp,   // 👈 margen al inicio
        end = 7.dp,     // 👈 margen al final
        top = 8.dp,
        bottom = 8.dp
    ),){
        items(list){i->
            Box( modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .height(45.dp)
                .padding(horizontal = 15.dp, vertical = 10.dp), contentAlignment = Alignment.Center)
                {
                    texto_generico_one_line(i.capitalizeFirst(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }


@Composable
fun abierto_flag(texto:String){
    Box(modifier = Modifier.clip(CircleShape).background(Color(0xFF43A047)) // Verde intermedio, más natural
    ){
    texto_generico_one_line(texto.capitalizeFirst(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color.White)
    }
}

@Composable
fun img_principal(img_principal: String) {
    var estadolistener by remember { mutableStateOf(false) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(img_principal)
            .crossfade(true)
            .placeholder(R.drawable.cargando_img_categorias)
            .error(R.drawable.sin_item_carrito)
            .build(),
        contentDescription = "",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { estadolistener = !estadolistener },
    )
    AnimatedVisibility(estadolistener) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(10.dp)
        ) {
//            galeria_img()
        }
    }
}

@Composable
fun galeria_img(lista_img: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(lista_img) { img ->
            card_img(img, 100.dp, 100.dp, 10)
        }
    }
}

@Composable
fun card_img(img: String, alto: Dp, ancho: Dp, rounder: Int) {
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
}


@Composable
fun texto_Descripcion_ref(titulo: String, texto: String) {
    texto_generico_multilinea(titulo, MaterialTheme.typography.titleLarge)
    spacer_vertical(10.dp)
    texto_generico_multilinea(texto, MaterialTheme.typography.bodyMedium)

}


@Composable
fun buttom_open_map(
    texto_button: String,
    clik_listener: () -> Unit,
) {

    ExtendedFloatingActionButton(onClick = {
        clik_listener()
    }) {
        Icon(
            painter = painterResource(id = R.drawable.localidad_icon_general),
            contentDescription = "",
            modifier = Modifier.size(25.dp),
            tint = Color.Unspecified
        )
        texto_generico_one_line(texto_button)
    }
}


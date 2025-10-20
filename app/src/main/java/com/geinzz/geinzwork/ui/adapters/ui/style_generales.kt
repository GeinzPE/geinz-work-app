package com.geinzz.geinzwork.ui.adapters.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.ZoomableImageDialogFullScreen
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.ZoomIconButton

@Composable
fun CollageGoogleMapsStyle(
    aspectRatio: Float=1.4f,
    with: Dp=250.dp,
    imagenes: List<String>,
    modifier: Modifier = Modifier
) {
    if (imagenes.isEmpty()) return

    val grupos = imagenes.chunked(3)

    LazyRow(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .wrapContentHeight(),
        contentPadding = PaddingValues(
            start = 7.dp,   // 👈 margen al inicio
            end = 7.dp,     // 👈 margen al final
            top = 8.dp,
            bottom = 8.dp
        ),

    ) {
        items(grupos) { grupo ->
            GrupoCollageGoogle(aspectRatio,with,grupo)
        }
    }

}

@Composable
fun GrupoCollageGoogle(aspectRatio: Float,
                       with: Dp,imagenes: List<String>) {
    Row(
        modifier = Modifier
            .width(with)
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        // Imagen grande
        ImagenCollage(

            url = imagenes.getOrNull(0),
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        )

        // Dos pequeñas en columna
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ImagenCollage(

                url = imagenes.getOrNull(1),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            ImagenCollage(

                url = imagenes.getOrNull(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ImagenCollage(url: String?, modifier: Modifier = Modifier) {
    var expandir_img by remember { mutableStateOf(false) }
    if (expandir_img) {
        ZoomableImageDialogFullScreen(
            imageUrl = url?:"",
            onDismiss = { expandir_img = false }
        )
    }
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)

    ) {
        if (url != null && url.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                ZoomIconButton({expandir_img=true})
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)

            )
        }
    }
}
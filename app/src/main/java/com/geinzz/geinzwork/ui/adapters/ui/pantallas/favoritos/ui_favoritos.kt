package com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.principal.AutoResizeOneLineText
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.categorias_defaul
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.simplificarCategoria
import com.geinzz.geinzwork.viewModels.viewModel_favoritos
import kotlinx.coroutines.delay

@Composable
fun iu_favoritos(
    viewmodelFavoritos: viewModel_favoritos,
    datos_principales_user: datos_principales_user
) {

    val lista_fb_size by viewmodelFavoritos.lista_fv.collectAsState()


    LaunchedEffect(lista_fb_size) {
    }

//    fraces_cambio(datos_principales_user.nombre)


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 30.dp),
            text = "Aun no cuentas con favoritos",
            fontFamily = baners_geinz_work,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
        )
        spacer_vertical(20.dp)
        ChipsCategorias(categorias_defaul)
    }


}

@Composable
fun fraces_cambio(nombre_user: String) {
    val fraces = constantes_lista_localidades.lista_fraces_favoritos
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }
    Crossfade(fraces[index], label = "fraces") { txt ->
        AutoResizeOneLineText(
            text = txt,
            style = MaterialTheme.typography.busquedaGeinzWork
        )
    }
}

fun <T> dividirEnFilas(lista: List<T>, filas: Int): List<List<T>> {
    val size = lista.size
    val elementosPorFila = (size + filas - 1) / filas
    return lista.chunked(elementosPorFila)
}

@Composable
fun ChipsCategorias(categorias: List<String>) {

    val filas = dividirEnFilas(categorias, 3)
    Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // 🔹 centrado horizontal
            modifier = Modifier.fillMaxWidth()

        ) {
            filas.forEach { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    fila.forEach { categoria ->
                        ChipCategoria(titulo = simplificarCategoria(categoria))
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(60.dp)
                .align(Alignment.CenterStart)
                .zIndex(1f)
                .background(Brush.horizontalGradient(colors = shadow_left))
        )

        // 👉 derecha
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(60.dp)
                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .background(Brush.horizontalGradient(colors = shadow_right))
        )
    }

}

@Composable
fun ChipCategoria(titulo: String) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 14.sp,
            color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}


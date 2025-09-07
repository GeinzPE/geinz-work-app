package com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive




@Composable
fun ui_pantalla_busqueda() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        item {
            fraces_filtrado()
            spacer_vertical(10.dp)
        }
        item {
            TexfielFiltrado()
        }
    }

}
@Composable
fun filtrado_categorias(){

}
@Composable
fun fraces_filtrado(){
    val fraces= constantes_lista_localidades.lista_frases_busqueda
    var index by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        while (true){
            delay(4000L)
            index=(index+1)% fraces.size
        }
    }

    Crossfade(fraces[index], label = "fraces") { txt->
        texto_generico_one_line(
            texto = txt,
            MaterialTheme.typography.busquedaGeinzWork
        )
    }
}



@Composable
fun TexfielFiltrado(viewModel: SearchViewModel) {
    var searchText by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState()

    OutlinedTextField(
        value = searchText,
        onValueChange = { newText ->
            searchText = newText
            viewModel.search(newText) // Busca mientras escribes
        },
        placeholder = { Text("A dónde quieres llegar?") },
        label = { Text("A dónde quieres llegar?") },
        leadingIcon = { /* tu ícono */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50)
    )

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        items(results) { item ->
            Text(item.nombre)
        }
    }
}



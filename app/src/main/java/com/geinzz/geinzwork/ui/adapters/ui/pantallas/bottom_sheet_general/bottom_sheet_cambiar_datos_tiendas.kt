package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.model.repo_agregar_datos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDownconvalor_inicial

import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.chips_categorias
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.chips_categoriasconvalor_inicial
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_datos
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_cambiar_datos_tiendas(ondimis: () -> Unit) {

    val context = LocalContext.current

    // =============== STATES ===============
    var id_tienda_cambio by remember { mutableStateOf("") }

    var lista_categorias by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_subcategorias_full by rememberSaveable { mutableStateOf(listOf<List<String>>()) }
    var lista_subcategoria by rememberSaveable { mutableStateOf(listOf<String>()) }

    val viewmodel_instance: viewmodel_agregar_datos = viewModel()
    var localidad by remember { mutableStateOf("barranca") }

    var cat_select by rememberSaveable { mutableStateOf("") }
    var subcategorias_select by rememberSaveable { mutableStateOf(listOf<String>()) }

    val cat_sub_tienda = viewmodel_instance.obtener_cat_sub_tienda.collectAsState()

    var mosatar_btncambio_cat by remember { mutableStateOf(false) }

    var nombre_tienda by remember { mutableStateOf("") }
    var pertenece_algolia by remember { mutableStateOf(false) }
    var pertenece_nuevo by remember { mutableStateOf(false) }
    // =============== CARGAR CATEGORÍAS DEL SISTEMA ===============
    LaunchedEffect(Unit) {
        val (cats, subs_full) = repo_agregar_datos(context).obtener_categorias()
        lista_categorias = cats
        lista_subcategorias_full = subs_full
    }

    // =============== CARGA DATOS DE TIENDA DESDE VIEWMODEL ===============
    LaunchedEffect(cat_sub_tienda.value) {
        val datos = cat_sub_tienda.value

        if (datos.cat.isNotEmpty()) {
            cat_select = datos.cat
            subcategorias_select = datos.lista_sub
            nombre_tienda = datos.nombre_lugar
            pertenece_algolia = datos.pertenerce_algolia
            pertenece_nuevo = datos.esta_nuevo


            // Actualiza la lista visible de subcategorías
            val index = lista_categorias.indexOf(datos.cat)
            if (index != -1) {
                lista_subcategoria = lista_subcategorias_full[index]
            }
        }
    }
    fun limpiarCampos() {
        id_tienda_cambio = ""
        cat_select = ""
        subcategorias_select = emptyList()
        lista_subcategoria = emptyList()
        pertenece_algolia = false
        pertenece_nuevo = false
        mosatar_btncambio_cat = false
    }


    // =============== UI ===============
    ModalBottomSheet(
        onDismissRequest = {
            viewmodel_instance.limpiarCatSubTienda()   // <- RESETEA LiveData/StateFlow
            limpiarCampos()                            // <- RESETA UI
            ondimis()                                   // <- CIERRA HOJA
        }
        ,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {

            Column(modifier = Modifier.padding(16.dp)) {

                texto_generico_one_line(nombre_tienda)
                // ============================================
                //        TEXTFIELD + ÍCONO PEGAR
                // ============================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)) {
                        MyOutlinedTextField(
                            value = id_tienda_cambio,
                            onValueChange = {
                                id_tienda_cambio = it

                                if (it.length == 20) {
                                    Log.d("llegamos", "llegamos")
                                    viewmodel_instance.obtener_cat_sub_tienda(
                                        id_tienda = it,
                                        localidad_tienda = localidad,
                                        context = context
                                    )
                                    mosatar_btncambio_cat = true
                                }
                            },
                            labelText = "ID tienda",
                            placeholderText = "Escribe el ID exacto"
                        )
                    }

                    Image(
                        painter = painterResource(R.drawable.pegar_portapales_webp),
                        contentDescription = "Copiar del portapapeles",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = clipboard.primaryClip
                                val item = clipData?.getItemAt(0)
                                val text = item?.text?.toString() ?: ""

                                val cleaned = text.trim().replace("\n", "").replace("\r", "")

                                id_tienda_cambio = cleaned

                                if (cleaned.length == 20) {
                                    viewmodel_instance.obtener_cat_sub_tienda(
                                        id_tienda = cleaned,
                                        localidad_tienda = localidad,
                                        context = context
                                    )
                                    mosatar_btncambio_cat = true
                                }
                            }

                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


                // ============================================
                //           SELECTOR DE CATEGORÍA
                // ============================================
                ExpandDropDownconvalor_inicial(
                    lista_categorias,
                    false,
                    "",
                    "Categoría",
                    valorInicial = cat_select,   // <- Ahora sí aparecerá la categoría actual
                ) { catseleccionado ->
                    cat_select = catseleccionado
                    val index = lista_categorias.indexOf(catseleccionado)
                    if (index != -1) {
                        lista_subcategoria = lista_subcategorias_full[index]
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))


                // ============================================
                //           CHIPS SUBCATEGORÍAS
                // ============================================
                chips_categoriasconvalor_inicial(
                    lista_subcategoria,
                    subcategorias_select
                ) { seleccion ->
                    subcategorias_select = seleccion
                }
                if (mosatar_btncambio_cat) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        texto_generico_one_line("búsqueda avanzada")

                        Spacer(modifier = Modifier.weight(1f))

                        Switch(
                            checked = pertenece_algolia,
                            onCheckedChange = { pertenece_algolia = it }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        texto_generico_one_line("nuevos negocios")

                        Spacer(modifier = Modifier.weight(1f))

                        Switch(
                            checked = pertenece_nuevo,
                            onCheckedChange = { pertenece_nuevo = it }
                        )
                    }

                    Button(onClick = {
                        viewmodel_instance.guaradr_cat_sub_nueva(
                            pertenece_algolia,pertenece_nuevo,
                            id_tienda = id_tienda_cambio,
                            localidad_tienda = "barranca",
                            context = context,
                            cat = cat_select,
                            sub = subcategorias_select
                        ) { ok ->
                            if (ok) {
                                ondimis()
                                Toast.makeText(
                                    context,
                                    "Guardado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                limpiarCampos()
                            } else {
                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }) {
                        texto_generico_one_line("guardar cambios")
                    }
                }
            }
        }
    }
}

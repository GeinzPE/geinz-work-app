package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.text.Layout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_shet_filtrado_inmubles(ondismis: () -> Unit) {


    val lista_tipo_operacion = listOf("compra", "alquiler", "proyecto", "temporal", "traspaso")
    val lista_tipo_inmueble =
        listOf("departamente", "casa", "terreno/lote", "local", "oficia", "edificio", "hotel")
    val antiguedad = listOf("en construcción", "a estrenar", "hasta 5 años", "hasta 10 años")


    var filtrado_desde by remember { mutableStateOf("") }
    var filtrado_hasta by remember { mutableStateOf("") }

    var filtrado_metros_desde by remember { mutableStateOf("") }
    var filtrado_metros_hasta by remember { mutableStateOf("") }

    var seleccionado by remember { mutableStateOf(false) }

    var tipo_operacion_Seleccionada by remember { mutableStateOf(false) }

    val tipo_operacion by animateColorAsState(
        targetValue = if (tipo_operacion_Seleccionada)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.background, animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )

    ModalBottomSheet(
        onDismissRequest = {
            ondismis()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            LazyColumn(
                modifier = Modifier
                    .animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                item {
                    texto_generico_multilinea(
                        "Encuentra lo que buscas en segundos", modifier = Modifier.clickable {
                            tipo_operacion_Seleccionada = !tipo_operacion_Seleccionada
                        },
                        style = MaterialTheme.typography.banerGeinzWork
                    )
                }
                item {
                    if (seleccionado) {
                        texto_generico_one_line("tus selecciones dadas")
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(tipo_operacion)
                            .padding(horizontal = 5.dp, vertical = 10.dp)
                    ) {

                        texto_generico_one_line("tipo de operacion".capitalizeFirst())
                        chips_de_filtrado(lista_tipo_operacion)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(tipo_operacion)
                            .padding(horizontal = 5.dp, vertical = 10.dp)
                    ) {

                        texto_generico_one_line("tipo de hinmueble".capitalizeFirst())
                        chips_de_filtrado(lista_tipo_inmueble)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(tipo_operacion)
                            .padding(horizontal = 5.dp, vertical = 10.dp)
                    ) {

                        texto_generico_one_line("divisa".capitalizeFirst())
                        spacer_vertical(8.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            boton_desing("soles")
                            boton_desing("dolares")
                        }
                    }
                }
                item {
                    val precioDesdeNum = filtrado_desde.toLongOrNull()
                    val precioHastaNum = filtrado_hasta.toLongOrNull()
                    val precioError = when {
                        filtrado_desde.isNotEmpty() && precioDesdeNum == null -> "Precio \"desde\" inválido"
                        filtrado_hasta.isNotEmpty() && precioHastaNum == null -> "Precio \"hasta\" inválido"
                        precioDesdeNum != null && precioHastaNum != null && precioDesdeNum >= precioHastaNum ->
                            "El precio inicial debe ser menor al final"
                        else -> null
                    }

                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(tipo_operacion)
                            .padding(horizontal = 5.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        texto_generico_one_line("Precio")

                        if (precioError != null) {
                            texto_generico_multilinea(precioError, Color = MaterialTheme.colorScheme.error)
                        } else if (precioDesdeNum != null && precioHastaNum != null) {
                            texto_generico_multilinea(
                                "Buscando entre ${formatearNumero(filtrado_desde)} y ${formatearNumero(filtrado_hasta)}"
                            )
                        } else if (precioDesdeNum != null) {
                            texto_generico_multilinea("Precio mínimo: ${formatearNumero(filtrado_desde)}")
                        } else if (precioHastaNum != null) {
                            texto_generico_multilinea("Precio máximo: ${formatearNumero(filtrado_hasta)}")
                        }

                        texflied_verificado(
                            desde = filtrado_desde,
                            hasta = filtrado_hasta,
                            isError = precioError != null,
                            desdes_fun = { filtrado_desde = it },
                            hasta_fun = { filtrado_hasta = it }
                        )
                    }
                }
                item {
                    val metrosDesdeNum = filtrado_metros_desde.toLongOrNull()
                    val metrosHastaNum = filtrado_metros_hasta.toLongOrNull()
                    val metrosError = when {
                        filtrado_metros_desde.isNotEmpty() && metrosDesdeNum == null -> "Metros \"desde\" inválido"
                        filtrado_metros_hasta.isNotEmpty() && metrosHastaNum == null -> "Metros \"hasta\" inválido"
                        metrosDesdeNum != null && metrosHastaNum != null && metrosDesdeNum >= metrosHastaNum ->
                            "Los metros iniciales deben ser menores a los finales"
                        else -> null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(tipo_operacion)
                            .padding(horizontal = 5.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        texto_generico_one_line("Metros cuadrados")

                        if (metrosError != null) {
                            texto_generico_multilinea(metrosError, Color = MaterialTheme.colorScheme.error)
                        } else if (metrosDesdeNum != null && metrosHastaNum != null) {
                            texto_generico_multilinea(
                                "Buscando entre ${formatearNumero(filtrado_metros_desde)} m² y ${formatearNumero(filtrado_metros_hasta)} m²"
                            )
                        } else if (metrosDesdeNum != null) {
                            texto_generico_multilinea("Mínimo: ${formatearNumero(filtrado_metros_desde)} m²")
                        } else if (metrosHastaNum != null) {
                            texto_generico_multilinea("Máximo: ${formatearNumero(filtrado_metros_hasta)} m²")
                        }

                        texflied_verificado(
                            desde = filtrado_metros_desde,
                            hasta = filtrado_metros_hasta,
                            isError = metrosError != null,
                            desdes_fun = { filtrado_metros_desde = it },
                            hasta_fun = { filtrado_metros_hasta = it }
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(tipo_operacion)
                            .padding(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        texto_generico_one_line("antiguedad".capitalizeFirst())
                        chips_de_filtrado(antiguedad)

                    }
                }


            }
        }
    }
}


@Composable
fun texflied_verificado(
    desde: String,
    hasta: String,
    isError: Boolean = false,
    desdes_fun: (String) -> Unit,
    hasta_fun: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = desde,
            onValueChange = { it ->
                val soloNumeros = it.filter { it.isDigit() }
                desdes_fun(soloNumeros)
            },
            label = { texto_generico_one_line("Desde") },
            shape = RoundedCornerShape(20.dp),
            isError = isError,
            placeholder = {
                texto_generico_one_line(
                    "nombre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = Color.Gray,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )
        spacer_vertical(5.dp)
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = hasta,
            onValueChange = { it ->
                val soloNumeros = it.filter { it.isDigit() }

                hasta_fun(soloNumeros)
            },
            label = { texto_generico_one_line("Hasta") },
            shape = RoundedCornerShape(20.dp),
            isError = isError,
            placeholder = {
                texto_generico_one_line(
                    "nombre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = Color.Gray,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )
    }
}

@Composable
fun boton_desing(txt: String) {
    spacer_vertical(10.dp)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 13.dp, vertical = 10.dp)
    ) {
        texto_generico_one_line(txt.capitalizeFirst(), style = MaterialTheme.typography.bodyMedium)

    }
}

@Composable
fun chips_de_filtrado(lista_texto: List<String>) {
    spacer_vertical(10.dp)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(lista_texto) { i ->
            boton_desing(i)
        }
    }
}

fun formatearNumero(numero: String): String {
    return numero
        .toLongOrNull()
        ?.let { "%,d".format(it) }
        ?: numero
}

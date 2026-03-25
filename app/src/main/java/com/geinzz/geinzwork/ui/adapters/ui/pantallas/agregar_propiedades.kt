package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ubicacion
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_datos
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_propiedades
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun agregar_propiedades() {
    val lat_inicia = -10.749213848512397
    val lng_inicial = -77.76144964752426
    var obtener_datos: viewmodel_agregar_propiedades = viewModel()
    val viewmodel_agregar_datos: viewmodel_agregar_datos = viewModel()
    val context = LocalContext.current
    var alto by remember { mutableStateOf("") }
    var ancho by remember { mutableStateOf("") }

    var lat_txt by remember { mutableStateOf("") }
    var lng_txt by remember { mutableStateOf("") }

    var cordenadas_lat by remember { mutableStateOf(lat_inicia) }
    var cordenadas_lng by remember { mutableStateOf(lng_inicial) }

    var localidad_defaul by remember { mutableStateOf("barranca") }
    var lugares_nombres by remember { mutableStateOf<List<String>>(emptyList()) }
    var resultado_medicion_terreno by remember { mutableStateOf(0.0) }

    var titulo_geneado_variable by remember { mutableStateOf("") }
    var descripcion_generada_varible by remember { mutableStateOf("") }


    var tipo_terreno by remember { mutableStateOf("") }
    var tipo_operacion by remember { mutableStateOf("") }
    val lista_modelo_negocio = listOf("casa", "hotel", "terreno vacio", "edificio")
    val lista_tipo_operacion = listOf("venta", "aquiler")


    val titulo_generado by obtener_datos.titulo.collectAsState()
    val texto_generado by obtener_datos.descripcion.collectAsState()

    val direccion by obtener_datos.nombre_calle.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(alto, ancho) {
        if (alto.isNotEmpty() && ancho.isNotEmpty()) {
            val resultado = verificar_metraje_propiedad(alto.toDouble(), ancho.toDouble())
            resultado_medicion_terreno = resultado

        }
    }
    LaunchedEffect(
        titulo_generado, texto_generado
    ) {
        titulo_geneado_variable = titulo_generado
        descripcion_generada_varible = texto_generado
    }

    LaunchedEffect(lugares_nombres) {
        Log.d("nombre_lugares_cecanos ", "$lugares_nombres ")


    }


    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {


            item {
                ExpandDropDown(
                    lista_modelo_negocio,
                    false,
                    "",
                    "tipo de terreno",
                ) { modelo ->
                    tipo_terreno = modelo

                }
            }
            item {
                ExpandDropDown(
                    lista_tipo_operacion,
                    false,
                    "",
                    "tipo de operacion",
                ) { modelo ->
                    tipo_operacion = modelo

                }
            }


            item {
                texto_generico_one_line("metraje")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = alto,
                        onValueChange = { it ->
                            alto = it
                        },
                        label = { texto_generico_one_line("Largo") },
                        shape = RoundedCornerShape(20.dp),
                        placeholder = {
                            texto_generico_one_line(
                                "Alto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f),
                    )


                    OutlinedTextField(
                        value = ancho,
                        onValueChange = { it ->
                            ancho = it
                        },
                        label = { texto_generico_one_line("Ancho") },
                        shape = RoundedCornerShape(20.dp),
                        placeholder = {
                            texto_generico_one_line(
                                "Ancho",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f),
                    )


                }

            }

            item {
                texto_generico_one_line("cordenadas")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = lat_txt,
                        onValueChange = { it ->
                            lat_txt = it
                        },
                        label = { texto_generico_one_line("Lat") },
                        shape = RoundedCornerShape(20.dp),
                        placeholder = {
                            texto_generico_one_line(
                                "Latitud",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f),
                    )


                    OutlinedTextField(
                        value = lng_txt,
                        onValueChange = { it ->
                            lng_txt = it
                        },
                        label = { texto_generico_one_line("Lng") },
                        shape = RoundedCornerShape(20.dp),
                        placeholder = {
                            texto_generico_one_line(
                                "Longitud",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f),
                    )


                }
                texto_generico_one_line("tamaño propiedad")
                if (resultado_medicion_terreno != 0.0) {
                    val texto =
                        "El terreno tiene un área de ${"%.2f".format(resultado_medicion_terreno)} m2"
                    texto_generico_one_line(texto)
                }

                if (direccion.isNotEmpty()) {
                    val texto =
                        "ubicado en : ${direccion}"
                    texto_generico_one_line(texto)
                }

                if (lugares_nombres.isNotEmpty()) {
                    val resultado = lugares_nombres.joinToString(", ")
                    texto_generico_one_line(resultado)


                }
            }

            item {
                Button(onClick = {
                    scope.launch {
                        lugares_nombres =
                            obtener_datos.obtener_lugares_cercanos(
                                cordenadas_lat,
                                cordenadas_lng,
                                localidad_defaul
                            )
                            obtener_datos.buscar_nombre_calle(
                                cordenadas_lat,
                                cordenadas_lng,
                            )
                        Log.d("nombre_lugares_cecanos ", "$lugares_nombres $direccion")
                    }
                }) {
                    texto_generico_one_line("obtenerdatos")
                }
            }

            item {
                Button(onClick = {
                    scope.launch {
                        obtener_datos.generar_titulo_para_Casa(
                            tipo_realizado = tipo_terreno,
                            tipo_operacion = tipo_operacion,
                            nombre_Calle = direccion,
                            localidad = localidad_defaul,
                            lista_lugares = lugares_nombres
                        )
                    }
                }) {
                    texto_generico_one_line("obtener_titulo_descripcion")
                }
            }

            item {
                texto_generico_one_line("titulo")

                texto_generico_multilinea(titulo_geneado_variable)
            }
            item {
                texto_generico_one_line("descrpcion")

                texto_generico_multilinea(descripcion_generada_varible)
            }

        }
    }
}

fun verificar_metraje_propiedad(ancho: Double, largo: Double): Double {
    return ancho * largo
}

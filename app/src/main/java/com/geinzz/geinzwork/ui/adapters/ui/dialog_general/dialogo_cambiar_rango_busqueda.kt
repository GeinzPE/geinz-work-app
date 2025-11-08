package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.geohashing
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerZonaActual
import com.geinzz.geinzwork.viewModels.viewmodel_floating_filtrado
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dialogo_cabiar_rango_busqueda(
    viewmodel_floating_filtrado: viewmodel_floating_filtrado,
    geohashin: String?,
    context: Context,
    ondimis: () -> Unit,
    ondimis_aceptar: (Float, String) -> Unit,
    cancelar_dialog_filtrado_cerncano: () -> Unit,
    localidad_busqueda_general: String,
    listner_localidad_busqueda: () -> Unit,
    nuevo_hashin:(String)-> Unit
) {
    val scope = rememberCoroutineScope()
    val radioGuardado by data_store_localidad.get_radio_user(context)
        .collectAsState(initial = 1f)
    var radioActual by remember { mutableStateOf(1f) }
    LaunchedEffect(radioGuardado) {
        radioActual = radioGuardado
    }
    val ultima_cordenada_actualziada by data_store_localidad.get_hora_hashin_user(
        context
    ).collectAsState(initial = null)

    var ultima_hora_actualziada by remember { mutableStateOf("") }
    val scate_carga_cordenadas_nuevas by viewmodel_floating_filtrado.carga_cordenadas_nuevas.collectAsState()


    LaunchedEffect(ultima_cordenada_actualziada) {
        Log.d("ultima_hora_actualziada", ultima_cordenada_actualziada.toString())
        ultima_hora_actualziada = ultima_cordenada_actualziada ?: ""
    }
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general {
                scope.launch {
                    // Guarda el nuevo rango
                    data_store_localidad.guardar_radio_user(context, radioActual)

                    // Si hay ubicación disponible, aplica el cambio
                    geohashin?.let {
                        ondimis_aceptar(radioActual, it)
                    } ?: Log.d("Ubicacion", "❌ Aún no se ha obtenido la ubicación")

                    // Cierra el diálogo al final
                    ondimis()
                }
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general { ondimis() }
        },
        text = {
            FuenteControladaApp{
            Column() {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        texto_generico_one_line(
                            "Cerca de ti",
                            MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            modifier = Modifier
                                .scale(0.8f)
                                .padding(end = 20.dp),
                            checked = true,
                            onCheckedChange = {
                                cancelar_dialog_filtrado_cerncano()
                                ondimis()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                spacer_vertical(15.dp)
                texto_generico_multilinea(
                    "Puedes ajustar el rango para encontrar resultados más cerca o más lejos de tu ubicación actual.",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(15.dp)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when (scate_carga_cordenadas_nuevas) {
                            is viewmodel_floating_filtrado.carga_cordenadas.error -> {
                                val error =
                                    (scate_carga_cordenadas_nuevas as viewmodel_floating_filtrado.carga_cordenadas.error).txt
                                texto_generico_one_line(
                                    error,
                                    MaterialTheme.typography.bodyMedium
                                )
                            }

                            is viewmodel_floating_filtrado.carga_cordenadas.inicial -> {
                                // 🔹 Mostrar la última hora guardada sin actualizar nada
                                texto_generico_one_line(
                                    "Ubicación actualizada",
                                    MaterialTheme.typography.bodyMedium
                                )
                                TextoSubrayado(
                                    "$ultima_hora_actualziada",
                                    MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.clickable {
                                        viewmodel_floating_filtrado.obtener_nuevas_cordenadas(context)
                                    },
                                    color_subrallado = MaterialTheme.colorScheme.primary
                                )
                            }

                            is viewmodel_floating_filtrado.carga_cordenadas.loading -> {
                                // 🔸 Mientras se actualiza: ocultamos textos y mostramos loader
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(25.dp)
                                            .padding(start = 6.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            is viewmodel_floating_filtrado.carga_cordenadas.succes -> {
                                val geohasing_variable =
                                    (scate_carga_cordenadas_nuevas as viewmodel_floating_filtrado.carga_cordenadas.succes).hashin_user
                                nuevo_hashin(geohasing_variable)

                                // 🔹 Mostrar texto y hora actualizada después del éxito
                                texto_generico_one_line(
                                    "Ubicación actualizada  ",
                                    MaterialTheme.typography.bodyMedium
                                )
                                TextoSubrayado(
                                    (scate_carga_cordenadas_nuevas as viewmodel_floating_filtrado.carga_cordenadas.succes).hora,
                                    MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.clickable {
                                        viewmodel_floating_filtrado.obtener_nuevas_cordenadas(context)
                                    },
                                    color_subrallado = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                spacer_vertical(15.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    texto_generico_one_line(
                        "Localidad de busqueda  ",
                        MaterialTheme.typography.bodyMedium
                    )
                    TextoSubrayado(
                        "${localidad_busqueda_general.capitalizeFirst()}",
                        MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            listner_localidad_busqueda()
                            ondimis()
                        },
                        color_subrallado = MaterialTheme.colorScheme.primary
                    )
                }


                spacer_vertical(15.dp)
                texto_generico_multilinea(
                    "Rango aproximado de búsqueda: ${radioActual} km",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)

                Slider(
                    enabled = true,
                    value = radioActual,
                    onValueChange = {
                        radioActual = it.roundToInt().toFloat()
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    onValueChangeFinished = {},
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.2f
                        ),
                        activeTickColor = MaterialTheme.colorScheme.primary,
                        inactiveTickColor = Color.Gray
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(25.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line(
                                radioActual.toInt().toString(),
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }
            }
        },
    )
}
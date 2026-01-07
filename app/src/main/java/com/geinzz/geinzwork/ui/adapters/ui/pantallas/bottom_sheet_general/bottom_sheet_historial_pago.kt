package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.R

import com.geinzz.geinzwork.data.model.historial_financiero
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.geinzz.geinzwork.viewModels.viewmodel_recargas

import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_historial_pago(
    id_tienda: String,
    nombretienda: String,
    localidad: String,
    saldo_actual: String,
    ondimis: () -> Unit
) {

    val viewmodel_monedas: viewmodel_recargas = viewModel()
    val state by viewmodel_monedas.stateHistorial.collectAsState()

    val lsita_fitlrado_opciones = listOf(
        "Todos",
        "Hoy",
        "Esta semana",
        "Este mes",
        "Generacion con IA",
        "Notificaciones",
        "Publicaciones",
        "Recargas"
    )
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }

    LaunchedEffect(id_tienda) {
        viewmodel_monedas.obtner_historial_pagos_tienda(id_tienda, localidad)
    }

    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            BoxWithConstraints {
                val maxHeightSheet = maxHeight * 0.8f
                val maxHeightSheet_empty = maxHeight * 0.4f

                when (state) {
                    is viewmodel_recargas.state_historial_financiero.Idle -> {}

                    is viewmodel_recargas.state_historial_financiero.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxHeight * 0.5f), // 👈 50% visual
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is viewmodel_recargas.state_historial_financiero.Success -> {
                        val lista =
                            (state as viewmodel_recargas.state_historial_financiero.Success).lista
                        val listaFiltrada =
                            remember(lista, subCategoriaSeleccionada) {
                                viewmodel_monedas.filtrarHistorial(
                                    lista = lista,
                                    filtro = subCategoriaSeleccionada
                                )
                            }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = maxHeightSheet),

                            ) {

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 10.dp,
                                        end = 10.dp,
                                        top = 10.dp,
                                        bottom = 20.dp
                                    )
                                    .heightIn(max = maxHeightSheet),
                                verticalArrangement = Arrangement.spacedBy(25.dp)
                            ) {
                                item {
                                    Text(
                                        fontFamily = baners_geinz_work,
                                        text = "Historial",
                                        color = Color.White,
                                        fontSize = 25.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    spacer_vertical(5.dp)

                                    texto_generico_multilinea(
                                        "Hola $nombretienda, en este apartado podrás ver el detalle de la inversión realizada en Geinz para el crecimiento de tu negocio.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                item {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                        )
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                texto_generico_one_line(
                                                    "${
                                                        calcularTotalMonedasDescuento(
                                                            listaFiltrada
                                                        )
                                                    }"
                                                )
                                                spacer_horizonta(5.dp)
                                                Image(
                                                    painter = painterResource(R.drawable.icon_monedas_3d),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            texto_generico_one_line(
                                                "Gastados",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                texto_generico_one_line("${saldo_actual}")
                                                spacer_horizonta(5.dp)
                                                Image(
                                                    painter = painterResource(R.drawable.icon_monedas_3d),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            texto_generico_one_line(
                                                "Saldo",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            texto_generico_one_line(
                                                "S/ ${
                                                    calcularTotalSolesDescuento(
                                                        listaFiltrada
                                                    )
                                                }"
                                            )
                                            texto_generico_one_line(
                                                "Gastados",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
//                                PieChartGastoMonedasVsSoles(lista)

                                }
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            10.dp
                                        )
                                    ) {

                                        items(lsita_fitlrado_opciones) { subcategoria ->
                                            val seleccionado =
                                                subCategoriaSeleccionada == subcategoria

                                            chisp_filtrado_busqueda(
                                                carta_selecionada = seleccionado,
                                                filtrado = subcategoria.capitalizeFirst(),
                                                btn_visible = false,
                                                clik_card = {
                                                    subCategoriaSeleccionada =
                                                        subcategoria
                                                },
                                                onClick_delete = {}
                                            )
                                        }
                                    }
                                }

                                items(listaFiltrada) { item ->
                                    item_historial_pagos(item)
                                }
                                item {
                                    if (listaFiltrada.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = maxHeightSheet_empty),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            texto_generico_one_line(
                                                "Aún no hay registros para este período.",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }


                        }
                    }

                    is viewmodel_recargas.state_historial_financiero.Error -> {
                        Text((state as viewmodel_recargas.state_historial_financiero.Error).mensaje)
                    }
                }
            }
        }
    }
}

@Composable
fun item_historial_pagos(i: historial_financiero) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2B2B2B))
            .padding(12.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                expanded = !expanded
            },
        verticalArrangement = Arrangement.spacedBy(6.dp)

    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            val esPaquete = i.tipo_transaccion.startsWith("PAQUETE", ignoreCase = true)

            val (iconoMaterial, colorIcono) = when {
                i.tipo_transaccion.contains("GEN IA", ignoreCase = true) -> {
                    Icons.Default.AutoAwesome to Color(0xFF9B59B6) // morado IA
                }

                i.tipo_transaccion.contains("PUBLICIDAD", ignoreCase = true) -> {
                    Icons.Default.Campaign to Color(0xFFF39C12) // naranja publicidad
                }

                i.tipo_transaccion.startsWith("ENVIO", ignoreCase = true) -> {
                    Icons.Default.Notifications to Color(0xFF3498DB) // azul notificación
                }

                else -> {
                    Icons.Default.Info to Color.Gray
                }
            }



            if (esPaquete) {
                // 🔹 TU LOGO (drawable)
                Image(
                    painter = painterResource(id = R.drawable.logo_geinz_500x500),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // 🔹 MATERIAL ICON
                Icon(
                    imageVector = iconoMaterial,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.size(19.dp)
                )
            }
            spacer_horizonta(5.dp)
            if (expanded) {
                texto_generico_multilinea(
                    texto = i.tipo_transaccion,
                    modifier = Modifier.weight(1f)
                )
            } else {
                texto_generico_one_line(
                    texto = i.tipo_transaccion,
                    modifier = Modifier.weight(1f)
                )
            }
            spacer_horizonta(10.dp)
            Box(
                modifier = Modifier
                    .size(18.dp) // un poco más grande para que entre el ícono
                    .clip(CircleShape)
                    .background(
                        if (i.tipo_realziado == "recarga")
                            Color(0xFF2ECC71) // verde ganancia
                        else
                            Color(0xFFE74C3C) // rojo descuento
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =
                        if (i.tipo_realziado == "recarga")
                            Icons.Default.ArrowUpward
                        else
                            Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }


        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            texto_generico_one_line(
                if (i.tipo_realziado == "recarga") "Monto recargado : ${i.monedas}" else "Monto gastado : ${i.monedas}",
                style = MaterialTheme.typography.bodyMedium
            )
            spacer_horizonta(5.dp)
            Image(
                painter = painterResource(R.drawable.icon_monedas_3d),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

        }
        texto_generico_one_line(
            "Realizado el : ${formatearFechaLarga(i.fecha).capitalizeFirst()}",
            style = MaterialTheme.typography.bodyMedium
        )


        if (expanded) {
            texto_generico_one_line(
                "Hora realizada: ${convertirHoraAmPm(i.hora)}",
                style = MaterialTheme.typography.bodyMedium
            )
            texto_generico_one_line(
                "Realizado por : ${i.nombre_tienda}",
                style = MaterialTheme.typography.bodyMedium
            )
            texto_generico_one_line(
                "Gasto en soles : S/${i.precio_soles}",
                style = MaterialTheme.typography.bodyMedium
            )
            texto_generico_one_line(
                "ID de operacion: ${i.id_transaccion}",
                style = MaterialTheme.typography.bodyMedium,
            )

            texto_generico_one_line(
                "Estado: ${i.estodo}",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (i.tipo_realziado.equals("descuento")) {
                texto_generico_one_line(
                    "Monto restante: ${i.monto_restante}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                texto_generico_one_line(
                    "Monto anterior: ${i.monto_restante}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

    }

}
//@Composable
//fun PieChartGastoMonedasVsSoles(
//    lista: List<historial_financiero>
//) {
//    val totalMonedas = calcularTotalMonedasDescuento(lista).toFloat()
//    val totalSoles = calcularTotalSolesDescuento(lista).toFloat()
//
//    // Evita crash si todo es 0
//    if (totalMonedas == 0f && totalSoles == 0f) return
//
//    val pieColors = listOf(
//        Color(0xFFFF9800) , // Soles
//        Color(0xFF4CAF50), // Monedas
//    )
//
//    val style = PieChartDefaults.style(
//        donutPercentage = 45f,
//        borderWidth = 4f,
//        borderColor = Color.White,
//        pieColors = pieColors
//    )
//
//    val dataSet = listOf(
//        totalMonedas,
//        totalSoles
//    ).toChartDataSet(
//        title = "Distribución de gastos",
//        labels = listOf("Monedas", "Soles"),
//        postfix = ""
//    )
//
//    PieChart(
//        dataSet = dataSet,
//        style = style,
//
//    )
//}


fun calcularTotalMonedasDescuento(
    lista: List<historial_financiero>
): Int {
    return lista
        .filter { it.tipo_realziado.equals("descuento", ignoreCase = true) }
        .sumOf { it.monedas.toInt() ?: 0 }
}

fun calcularTotalSolesDescuento(
    lista: List<historial_financiero>
): Double {
    return lista
        .filter { it.tipo_realziado.equals("descuento", ignoreCase = true) }
        .sumOf { it.precio_soles.toDoubleOrNull() ?: 0.0 }
}


fun convertirHoraAmPm(hora24: String): String {
    return try {
        val formato24 = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        val formato12 = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")

        val hora = java.time.LocalTime.parse(hora24, formato24)
        hora.format(formato12)
    } catch (e: Exception) {
        hora24
    }
}

fun formatearFechaLarga(fecha: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val outputFormatter = DateTimeFormatter.ofPattern(
            "EEEE dd 'de' MMMM 'del' yyyy",
            Locale("es", "ES")
        )

        val localDate = LocalDate.parse(fecha, inputFormatter)
        localDate.format(outputFormatter)

    } catch (e: Exception) {
        fecha // fallback por si algo falla
    }
}

fun obtenerLineaMonedas(lista: List<historial_financiero>): List<Float> {
    var total = 0f
    return lista.filter {
        it.tipo_realziado.equals("descuento", true)
    }.map {
        total += it.monedas.toFloat() ?: 0f
        total
    }
}

fun obtenerLineaSoles(lista: List<historial_financiero>): List<Float> {
    var total = 0f
    return lista.filter {
        it.tipo_realziado.equals("descuento", true)
    }.map {
        total += it.precio_soles.toFloatOrNull() ?: 0f
        total
    }
}


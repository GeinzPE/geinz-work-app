package com.geinzz.geinzwork.ui.adapters.ui.dialog_general


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.geinzz.geinzwork.data.model.EstadisticaAccion
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.EstadisticasTikTokCuadro_dentro
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Charts Android (Dautovic Haris)

import androidx.compose.ui.viewinterop.AndroidView

data class BuyerPersona(
    val genero: String,
    val edadRango: String,
    val localidad: String,
    val diaMasActivo: String,
    val totalInteracciones: Int,
    val recomendacion: String
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficosPromosMPAndroidChart(
    estadistica: EstadisticaAccion,
) {

    // 🎨 COLORES
    val AZUL = AndroidColor.parseColor("#4F8EF7")
    val ROSADO = AndroidColor.parseColor("#E0528C")
    val ROJO = AndroidColor.parseColor("#E53935")
    val VERDE = AndroidColor.parseColor("#66BB6A")
    val AMARILLO = AndroidColor.parseColor("#FBC02D")
    val CELESTE = AndroidColor.parseColor("#29B6F6")
    val MORADO = AndroidColor.parseColor("#AB47BC")


    val verde = AndroidColor.parseColor("#66BB6A")
    val amarillo = AndroidColor.parseColor("#FBC02D")
    val celeste = AndroidColor.parseColor("#29B6F6")
    val morado = AndroidColor.parseColor("#AB47BC")
    val rojo = AndroidColor.parseColor("#E53935")


    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {

        /* ===================== PIE - GÉNERO (%) ===================== */
        if (estadistica.genero.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // -------------------- Título y descripción
                texto_generico_one_line("Rendimiento por género")
                texto_generico_multilinea(
                    "Descubre si tu publicación conecta más con hombres, mujeres o público diverso para ajustar tu mensaje.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // -------------------- PieChart de género
                val colors123 = listOf(AZUL, ROSADO, ROJO, VERDE)
                val AZUL = ComposeColor(0xFF4F8EF7)
                val ROSADO = ComposeColor(0xFFE0528C)
                val ROJO = ComposeColor(0xFFE53935)
                val VERDE = ComposeColor(0xFF66BB6A)
                val colores: List<ComposeColor> = listOf(AZUL, ROSADO, ROJO, VERDE)
                val generoData = mapOf(
                    "Masculino" to (estadistica.genero.entries
                        .firstOrNull {
                            it.key.equals(
                                "Masculino",
                                ignoreCase = true
                            )
                        }?.value?.total?.toFloat() ?: 0f),
                    "Femenino" to (estadistica.genero.entries
                        .firstOrNull {
                            it.key.equals(
                                "Femenino",
                                ignoreCase = true
                            )
                        }?.value?.total?.toFloat() ?: 0f),
                    "Otros" to (estadistica.genero.entries
                        .firstOrNull {
                            it.key.equals(
                                "Otros",
                                ignoreCase = true
                            )
                        }?.value?.total?.toFloat() ?: 0f)
                )

                // -------------------- Mini-leyenda con colores y porcentajes

                // -------------------- PieChart
                AndroidView(
                    factory = { context ->
                        val pieChart = PieChart(context)
                        pieChart.apply {
                            setBackgroundColor(AndroidColor.parseColor("#2B2B2B"))
                            description.isEnabled = false
                            setUsePercentValues(true)
                            holeRadius = 55f
                            setHoleColor(AndroidColor.parseColor("#2B2B2B"))
                            setEntryLabelColor(AndroidColor.WHITE)
                            setEntryLabelTextSize(12f)
                            legend.isEnabled = false
                            setExtraOffsets(5f, 10f, 5f, 10f)

                            val entries = generoData.map { PieEntry(it.value, it.key) }
                            val dataSet = PieDataSet(entries, "").apply {
                                colors = colors123
                                valueTextColor = AndroidColor.WHITE
                                valueTextSize = 12f
                            }

                            val pieData = PieData(dataSet)
                            pieData.setValueFormatter(PercentFormatter(pieChart))
                            data = pieData

                            animateY(800)
                        }
                        pieChart
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                val totalGeneral = generoData.values.sum()
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    generoData.entries.forEachIndexed { index, entry ->
                        val porcentaje =
                            if (totalGeneral == 0f) 0 else ((entry.value * 100) / totalGeneral).roundToInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Color al costado
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = colores.getOrElse(index) { ComposeColor.Gray },
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "${entry.key}: $porcentaje%",
                                color = ComposeColor.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(4.dp))
                // -------------------- Mini-cuadros estilo TikTok
                val generoMax = generoData.maxByOrNull { it.value }?.key ?: "-"
                val totalMax = generoData.maxByOrNull { it.value }?.value ?: 0f
                val porcentajeMax =
                    if (totalGeneral == 0f) 0 else ((totalMax * 100) / totalGeneral).roundToInt()

                val generoMin = generoData.minByOrNull { it.value }?.key ?: "-"
                val totalMin = generoData.minByOrNull { it.value }?.value ?: 0f
                val porcentajeMin =
                    if (totalGeneral == 0f) 0 else ((totalMin * 100) / totalGeneral).roundToInt()


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadisticasTikTokCuadro_dentro(
                        modifier = Modifier.weight(1f),
                        titulo = "$generoMax ($porcentajeMax%)",
                        valor = totalMax.toInt().toString(),
                        subtitulo = "Género más activo",
                        iconoAscendente = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    EstadisticasTikTokCuadro_dentro(
                        modifier = Modifier.weight(1f),
                        titulo = "$generoMin ($porcentajeMin%)",
                        valor = totalMin.toInt().toString(),
                        subtitulo = "Género menos activo",
                        iconoAscendente = false
                    )
                }
            }
        }


        /* ===================== PIE - LOCALIDAD (%) ===================== */
        if (estadistica.localidad.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                val localidadData = estadistica.localidad.mapValues { it.value.total.toFloat() }
                val totalGeneral = localidadData.values.sum()

                // -------------------- COLORES (igual que ejemplo de género)
                val VERDE = ComposeColor(0xFF66BB6A)
                val AMARILLO = ComposeColor(0xFFFBC02D)
                val CELESTE = ComposeColor(0xFF29B6F6)
                val MORADO = ComposeColor(0xFFAB47BC)
                val ROJO = ComposeColor(0xFFE53935)
                val colores: List<ComposeColor> = listOf(VERDE, AMARILLO, CELESTE, MORADO, ROJO)
                val coloresLocalidad = listOf(verde, amarillo, celeste, morado, rojo)

                // -------------------- Título y descripción
                texto_generico_one_line("Impacto por ubicación")
                texto_generico_multilinea(
                    "Identifica las zonas de Barranca y alrededores donde tu anuncio tiene mayor relevancia y alcance.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // -------------------- PieChart sin leyenda
                AndroidView(
                    factory = { context ->
                        val pieChart = PieChart(context)
                        pieChart.apply {
                            setBackgroundColor(AndroidColor.parseColor("#2B2B2B"))
                            description.isEnabled = false
                            setUsePercentValues(true)
                            holeRadius = 55f
                            setHoleColor(AndroidColor.parseColor("#2B2B2B"))
                            setEntryLabelColor(AndroidColor.WHITE)
                            setEntryLabelTextSize(12f)

                            // 🔥 desactivar leyenda integrada
                            legend.isEnabled = false

                            setExtraOffsets(5f, 10f, 5f, 10f)

                            val entries = localidadData.map { PieEntry(it.value, it.key) }
                            val dataSet = PieDataSet(entries, "").apply {
                                colors = coloresLocalidad
                                valueTextColor = AndroidColor.WHITE
                                valueTextSize = 12f
                            }

                            val pieData = PieData(dataSet)
                            pieData.setValueFormatter(PercentFormatter(pieChart))
                            data = pieData

                            animateY(800)
                        }
                        pieChart
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                // -------------------- Mini-leyenda con colores y porcentajes
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(localidadData.entries.toList()) { index, entry ->
                        val porcentaje =
                            if (totalGeneral == 0f) 0 else ((entry.value * 100) / totalGeneral).roundToInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = colores.getOrElse(index) { ComposeColor.Gray },
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "${entry.key}: $porcentaje%",
                                color = ComposeColor.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // -------------------- Mini-cuadros estilo TikTok
                val localidadMax = localidadData.maxByOrNull { it.value }?.key ?: "-"
                val totalMax = localidadData.maxByOrNull { it.value }?.value ?: 0f
                val porcentajeMax =
                    if (totalGeneral == 0f) 0 else ((totalMax * 100) / totalGeneral).roundToInt()

                val localidadMin = localidadData.minByOrNull { it.value }?.key ?: "-"
                val totalMin = localidadData.minByOrNull { it.value }?.value ?: 0f
                val porcentajeMin =
                    if (totalGeneral == 0f) 0 else ((totalMin * 100) / totalGeneral).roundToInt()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadisticasTikTokCuadro_dentro(
                        modifier = Modifier.weight(1f),
                        titulo = "$localidadMax ($porcentajeMax%)",
                        valor = totalMax.toInt().toString(),
                        subtitulo = "Mayor impacto",
                        iconoAscendente = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    EstadisticasTikTokCuadro_dentro(
                        modifier = Modifier.weight(1f),
                        titulo = "$localidadMin ($porcentajeMin%)",
                        valor = totalMin.toInt().toString(),
                        subtitulo = "Menor impacto",
                        iconoAscendente = false
                    )
                }
            }
        }


        /* ===================== LINE - POR DÍA ===================== */
        if (estadistica.por_dia.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val porDiaOrdenado = estadistica.por_dia.toList().sortedBy { it.first }
                val etiquetas = porDiaOrdenado.map { it.first.substring(5) } // mm-dd

                // ---------------- Título y descripción
                texto_generico_one_line("Actividad en el tiempo")
                texto_generico_multilinea(
                    "Monitorea los picos de interacción diarios y descubre qué días de la semana son los más efectivos para tu negocio.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // ---------------- Mini-cuadros con flechitas
                val totales = porDiaOrdenado.map { it.second.total }
                val promedio = totales.average()
                val maximo = totales.maxOrNull() ?: 0
                val minimo = totales.minOrNull() ?: 0

                // Cambios respecto al día anterior
                val cambios = totales.mapIndexed { index, value ->
                    if (index == 0) 0 else value - totales[index - 1]
                }

                // ---------------- Gráfico de línea
                AndroidView(
                    factory = { context ->
                        LineChart(context).apply {

                            setBackgroundColor(AndroidColor.parseColor("#2B2B2B"))
                            description.isEnabled = false
                            legend.isEnabled = false

                            axisRight.isEnabled = false
                            axisLeft.textColor = AndroidColor.WHITE
                            axisLeft.axisMinimum = 0f

                            xAxis.apply {
                                granularity = 1f
                                textColor = AndroidColor.WHITE
                                valueFormatter = IndexAxisValueFormatter(etiquetas)
                                yOffset = 10f
                            }

                            val entries = porDiaOrdenado.mapIndexed { index, pair ->
                                Entry(index.toFloat(), pair.second.total.toFloat())
                            }

                            val dataSet = LineDataSet(entries, "").apply {
                                color = CELESTE
                                circleRadius = 5f
                                valueTextColor = AndroidColor.WHITE
                                valueTextSize = 12f
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                            }

                            data = LineData(dataSet)
                            setExtraOffsets(10f, 10f, 10f, 30f)
                            animateY(800)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Último día
                        EstadisticasTikTokCuadro_dentro(
                            modifier = Modifier.weight(1f),
                            titulo = "Último día",
                            valor = totales.last().toString(),
                            subtitulo = "Día: ${etiquetas.last()}",
                            iconoAscendente = cambios.last() >= 0
                        )

// Promedio
                        EstadisticasTikTokCuadro_dentro(
                            modifier = Modifier.weight(1f),
                            titulo = "Promedio",
                            valor = promedio.toInt().toString(),
                            subtitulo = "Comparado con promedio",
                            iconoAscendente = totales.last() >= promedio
                        )

                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val indexMax = totales.indexOf(maximo)
                        EstadisticasTikTokCuadro_dentro(
                            modifier = Modifier.weight(1f),
                            titulo = "Máximo",
                            valor = maximo.toString(),
                            subtitulo = "Día: ${etiquetas[indexMax]}",
                            iconoAscendente = cambios[indexMax] >= 0
                        )

// Mínimo
                        val indexMin = totales.indexOf(minimo)
                        EstadisticasTikTokCuadro_dentro(
                            modifier = Modifier.weight(1f),
                            titulo = "Mínimo",
                            valor = minimo.toString(),
                            subtitulo = "Día: ${etiquetas[indexMin]}",
                            iconoAscendente = cambios[indexMin] >= 0
                        )
                    }
                }
            }
        }


        /* ===================== HORIZONTAL BAR - EDAD ===================== */
        if (estadistica.edad.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                val edadesOrdenadas = estadistica.edad.toList().sortedBy { it.first }
                val etiquetasEdad = edadesOrdenadas.map { it.first }

                // -------------------- Título y descripción
                texto_generico_one_line("Segmentación por edad")
                texto_generico_multilinea(
                    "Conoce el rango de edad de las personas que interactúan con tu contenido para optimizar tus próximas ofertas.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // -------------------- Gráfico Horizontal
                AndroidView(
                    factory = { context ->
                        HorizontalBarChart(context).apply {

                            setBackgroundColor(AndroidColor.parseColor("#2B2B2B"))
                            description.isEnabled = false
                            legend.isEnabled = false

                            axisLeft.isEnabled = false
                            axisRight.axisMinimum = 0f
                            axisRight.textColor = AndroidColor.WHITE

                            xAxis.apply {
                                granularity = 1f
                                textColor = AndroidColor.WHITE
                                valueFormatter = IndexAxisValueFormatter(etiquetasEdad)
                            }

                            val entries = edadesOrdenadas.mapIndexed { index, pair ->
                                BarEntry(index.toFloat(), pair.second.total.toFloat())
                            }

                            val dataSet = BarDataSet(entries, "Edades").apply {
                                colors = listOf(VERDE, AMARILLO, CELESTE, MORADO)
                                valueTextColor = AndroidColor.WHITE
                                valueTextSize = 12f
                                setDrawValues(true)
                                valueFormatter = object :
                                    com.github.mikephil.charting.formatter.ValueFormatter() {
                                    override fun getBarLabel(barEntry: BarEntry?): String {
                                        return barEntry?.y?.toInt().toString()
                                    }
                                }
                            }

                            data = BarData(dataSet).apply {
                                barWidth = 0.6f
                            }

                            animateX(800)
                            setExtraOffsets(10f, 10f, 10f, 20f)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                // -------------------- Totales y porcentajes para mini-cuadros
                val totalGeneral = edadesOrdenadas.sumOf { it.second.total }

                val rangoDominante = edadesOrdenadas.maxByOrNull { it.second.total }?.first ?: "-"
                val totalDominante =
                    edadesOrdenadas.maxByOrNull { it.second.total }?.second?.total ?: 0
                val porcentajeDominante =
                    (totalDominante * 100 / totalGeneral.toFloat()).roundToInt()

                val rangoMenor = edadesOrdenadas.minByOrNull { it.second.total }?.first ?: "-"
                val totalMenor = edadesOrdenadas.minByOrNull { it.second.total }?.second?.total ?: 0
                val porcentajeMenor = (totalMenor * 100 / totalGeneral.toFloat()).roundToInt()

                // -------------------- Mini-cuadros estilo TikTok
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Más activo
                    EstadisticasTikTokCuadro_dentro(
                        modifier = Modifier.weight(1f),
                        titulo = "$rangoDominante ($porcentajeDominante%)",
                        valor = totalDominante.toString(),
                        subtitulo = "Rango más activo",
                        iconoAscendente = true

                    )

                    // Menos activo
                    EstadisticasTikTokCuadro_dentro(
                        modifier = Modifier.weight(1f),
                        titulo = "$rangoMenor ($porcentajeMenor%)",
                        valor = totalMenor.toString(),
                        subtitulo = "Rango menos activo",
                        iconoAscendente = false

                    )
                }
            }
        }



    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun generarBuyerPersona(
    estadistica: EstadisticaAccion
): BuyerPersona {

    // ---------------- GÉNERO DOMINANTE
    val generoDominante = estadistica.genero
        .maxByOrNull { it.value.total }
        ?.key ?: "Desconocido"

    // ---------------- EDAD DOMINANTE
    val edadDominante = estadistica.edad
        .maxByOrNull { it.value.total }
        ?.key ?: "N/A"

    // ---------------- LOCALIDAD DOMINANTE
    val localidadDominante = estadistica.localidad
        .maxByOrNull { it.value.total }
        ?.key ?: "N/A"

    // ---------------- DÍA MÁS ACTIVO
    val fechaTop = estadistica.por_dia
        .maxByOrNull { it.value.total }
        ?.key

    val diaMasActivo = fechaTop?.let {
        obtenerDiaSemana(it)
    } ?: "N/A"
    // ---------------- TOTAL INTERACCIONES
    val totalInteracciones = estadistica.genero.values.sumOf { it.total }

    // ---------------- RECOMENDACIÓN INTELIGENTE
    val recomendacion = buildString {
        append("Publica para $generoDominante ")
        append("de $edadDominante en $localidadDominante. ")
        append("El mejor día para publicar es $diaMasActivo. ")
        append("Usa promociones visuales y llamadas a la acción claras.")
    }

    return BuyerPersona(
        genero = generoDominante,
        edadRango = edadDominante,
        localidad = localidadDominante,
        diaMasActivo = diaMasActivo,
        totalInteracciones = totalInteracciones,
        recomendacion = recomendacion
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BuyerPersonaCard(persona: BuyerPersona) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ComposeColor(0xFF1E1E1E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "🧠 Perfil predominante",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ComposeColor.White
        )
        texto_generico_one_line(
            "👤 Género: ${persona.genero}",
            style = MaterialTheme.typography.bodyMedium
        )
        texto_generico_one_line(
            "🎂 Edad: ${persona.edadRango}",
            style = MaterialTheme.typography.bodyMedium
        )
        texto_generico_one_line(
            "📍 Zona: ${persona.localidad}",
            style = MaterialTheme.typography.bodyMedium
        )
        texto_generico_one_line(
            "📅 Día top: ${persona.diaMasActivo}",
            style = MaterialTheme.typography.bodyMedium
        )

        Divider(color = ComposeColor.White.copy(alpha = 0.2f))

        Text(
            text = "💡 Recomendación",
            fontWeight = FontWeight.SemiBold,
            color = ComposeColor.White
        )

        Text(
            text = persona.recomendacion,
            color = ComposeColor(0xFFB0B0B0),
            fontSize = 14.sp
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun obtenerDiaSemana(fecha: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(fecha, formatter)

    return date.dayOfWeek.getDisplayName(
        java.time.format.TextStyle.FULL,
        Locale("es", "ES")
    )
}




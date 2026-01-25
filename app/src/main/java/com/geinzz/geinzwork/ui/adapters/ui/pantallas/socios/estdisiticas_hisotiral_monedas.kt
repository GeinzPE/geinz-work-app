package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

// Android
import android.widget.Space
import android.graphics.Color as AndroidColor

// Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.RoundedCornerShape

// MPAndroidChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

object EstadisticasHistorialMonedas {

    @Composable
    fun GraficoBarrasVerticalInteractivo(
        conteoPorTipo: Map<String, Int>,
        modifier: Modifier = Modifier.fillMaxWidth()
    ) {
        val tipos = conteoPorTipo.keys.toList()
        val valores = conteoPorTipo.values.toList()

        val coloresVivos = listOf(
            Color(0xFF4CAF50), // Verde
            Color(0xFFFFC107), // Ámbar
            Color(0xFF03A9F4), // Celeste
            Color(0xFF9C27B0), // Morado
            Color(0xFFE53935), // Rojo
            Color(0xFFFF5722), // Naranja
            Color(0xFF1E88E5), // Azul fuerte
            Color(0xFFFF4081), // Rosado

            Color(0xFF00BCD4), // Cian
            Color(0xFF8BC34A), // Verde lima
            Color(0xFF673AB7), // Violeta oscuro
            Color(0xFF009688), // Verde azulado
            Color(0xFFFF9800), // Naranja claro
            Color(0xFFCDDC39), // Lima amarilla
            Color(0xFF3F51B5), // Indigo
            Color(0xFF795548), // Marrón moderno

            Color(0xFF607D8B), // Azul gris
            Color(0xFFD81B60), // Fucsia
            Color(0xFF26A69A), // Verde menta
            Color(0xFFEF5350)  // Rojo suave
        )


        val selectedIndex = remember { mutableStateOf(-1) }

        Column(modifier = modifier) {

            // 🔹 PIE CHART
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        setBackgroundColor(AndroidColor.parseColor("#2B2B2B"))
                        description.isEnabled = false
                        legend.isEnabled = false

                        setUsePercentValues(true)
                        setDrawEntryLabels(false)
                        isRotationEnabled = true
                        isHighlightPerTapEnabled = true

                        holeRadius = 55f
                        transparentCircleRadius = 60f
                        setHoleColor(AndroidColor.parseColor("#2B2B2B"))

                        val entries = valores.mapIndexed { index, value ->
                            PieEntry(value.toFloat(), tipos[index])
                        }

                        val dataSet = PieDataSet(entries, "").apply {
                            setColors(coloresVivos.map {
                                AndroidColor.argb(255, it.redInt, it.greenInt, it.blueInt)
                            })
                            sliceSpace = 3f
                            selectionShift = 8f
                            valueTextColor = AndroidColor.WHITE
                            valueTextSize = 14f

                            valueFormatter = object : ValueFormatter() {
                                override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                                    return "${value.toInt()}%"
                                }
                            }
                        }

                        data = PieData(dataSet)

                        // 🔹 SINCRONIZA selección con la leyenda
                        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                selectedIndex.value = h?.x?.toInt() ?: -1
                            }

                            override fun onNothingSelected() {
                                selectedIndex.value = -1
                            }
                        })

                        animateY(900)
                        invalidate()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 10.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tipos.chunked(2).forEachIndexed { rowIndex, fila ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        fila.forEachIndexed { colIndex, tipo ->
                            val index = rowIndex * 2 + colIndex
                            val activo = selectedIndex.value == -1 || selectedIndex.value == index

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp) // 🔹 ALTURA FIJA
                                    .background(
                                        color = Color.White.copy(alpha = if (activo) 0.08f else 0.03f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            color = coloresVivos.getOrElse(index) { Color.Gray }
                                                .copy(alpha = if (activo) 1f else 0.3f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )

                                Text(
                                    text = tipo,
                                    color = coloresVivos.getOrElse(index) { Color.White }
                                        .copy(alpha = if (activo) 1f else 0.3f),
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    lineHeight = 16.sp, // 🔹 CONTROL VISUAL
                                    textAlign = TextAlign.Start
                                )
                            }
                        }

                        // 🔹 Rellena si queda una sola card
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }


        }
    }

    // 🔹 Helpers Color
    val Color.redInt get() = (red * 255).toInt()
    val Color.greenInt get() = (green * 255).toInt()
    val Color.blueInt get() = (blue * 255).toInt()
}

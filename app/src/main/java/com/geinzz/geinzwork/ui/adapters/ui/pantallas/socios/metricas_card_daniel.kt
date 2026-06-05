package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.daniel_metricas.HistorialHotItem
import com.geinzz.geinzwork.data.model.daniel_metricas.MetricasResumen
import com.geinzz.geinzwork.viewModels.viewmodel_metricas_daniel
import java.text.SimpleDateFormat
import java.util.*

// ─── COLORES ──────────────────────────────────────────────────────────────────

private val MBotSurface2      = Color(0xFF1A1A1A)
private val MBotSurface3      = Color(0xFF252525)
private val MBotBorder        = Color(0xFF2A2A2A)
private val MBotGreen         = Color(0xFF22B05B)
private val MBotBlue          = Color(0xFF3B82F6)
private val MBotAmber         = Color(0xFFF59E0B)
private val MBotPurple        = Color(0xFF8B5CF6)
private val MBotRed           = Color(0xFFEF4444)
private val MBotTextPrimary   = Color(0xFFFFFFFF)
private val MBotTextSecondary = Color(0xFFE0E0E0)
private val MBotTextMuted     = Color(0xFF888888)

// ─── FILTROS ──────────────────────────────────────────────────────────────────

private enum class FiltroHistorial(val label: String) {
    TODO("Todos"),
    HOY("Hoy"),
    SEMANA("7 días"),
    MES("30 días")
}

// ─── CARD PRINCIPAL ───────────────────────────────────────────────────────────

@Composable
fun MetricasDanielCard(
    id_tienda          : String,
    precio_por_contacto: Int,     // soles por contacto directo
    precio_por_click   : Int,     // soles por click de WhatsApp
    precio_por_moneda  : Double,  // soles que vale 1 crédito
    modifier           : Modifier = Modifier
) {
    val viewmodel: viewmodel_metricas_daniel = viewModel()
    val estado by viewmodel.estado.collectAsState()
    var expandido by remember { mutableStateOf(false) }

    LaunchedEffect(id_tienda) { viewmodel.cargarMetricas(id_tienda) }

    Card(
        modifier = modifier.fillMaxWidth().padding(top = 14.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MBotSurface2),
        border   = BorderStroke(0.5.dp, MBotBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { expandido = !expandido },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MBotPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BarChart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Métricas", color = MBotTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Clicks · Publicidad · Rentabilidad", color = MBotTextMuted, fontSize = 11.sp)
                }
                if (expandido) {
                    IconButton(
                        onClick  = { viewmodel.recargar(id_tienda) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Recargar", tint = MBotTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Icon(
                    imageVector        = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = MBotTextMuted,
                    modifier           = Modifier.size(20.dp)
                )
            }

            // ── Cuerpo expandible ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = expandido,
                enter   = expandVertically(tween(300), Alignment.Top) + fadeIn(tween(260)),
                exit    = shrinkVertically(tween(260), Alignment.Top) + fadeOut(tween(200))
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MBotSurface3, thickness = 0.5.dp)
                    Spacer(Modifier.height(14.dp))

                    when (val s = estado) {
                        is viewmodel_metricas_daniel.EstadoMetricas.Loading ->
                            MetricasLoadingState()
                        is viewmodel_metricas_daniel.EstadoMetricas.Error ->
                            MetricasErrorState(s.mensaje) { viewmodel.recargar(id_tienda) }
                        is viewmodel_metricas_daniel.EstadoMetricas.Success -> {
                            val d = s.data
                            val sinDatos = d.clicks_whatsapp_hoy == 0 &&
                                    d.clicks_whatsapp_semana == 0 &&
                                    d.total_publicidad_enviada == 0 &&
                                    d.historial_reciente.isEmpty()
                            if (sinDatos) MetricasEmptyState()
                            else MetricasContenido(
                                data                = d,
                                precio_por_click    = precio_por_click,
                                precio_por_contacto = precio_por_contacto,
                                precio_por_moneda   = precio_por_moneda
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

// ─── CONTENIDO PRINCIPAL ──────────────────────────────────────────────────────

@Composable
private fun MetricasContenido(
    data               : MetricasResumen,
    precio_por_click   : Int,
    precio_por_contacto: Int,
    precio_por_moneda  : Double
) {
    // ✅ Créditos calculados desde cantidades × precio (no desde monedasGastadas)
    val creditosClicksHoy      = data.clicks_whatsapp_hoy    * precio_por_click
    val creditosClicksSemana   = data.clicks_whatsapp_semana * precio_por_click
    val creditosEnviadosHoy    = data.enviados_hoy           * precio_por_contacto
    val creditosEnviadosSemana = data.enviados_semana        * precio_por_contacto

    // ✅ Soles = créditos × precio_por_moneda (solo referencia visual)
    val solesClicksHoy      = creditosClicksHoy      * precio_por_moneda
    val solesClicksSemana   = creditosClicksSemana   * precio_por_moneda
    val solesEnviadosHoy    = creditosEnviadosHoy    * precio_por_moneda
    val solesEnviadosSemana = creditosEnviadosSemana * precio_por_moneda

    // ── BLOQUE 1 — CONTACTO DIRECTO ───────────────────────────────────────
    SeccionHeader(
        titulo    = "CONTACTO DIRECTO - WhatsApp",
        subtitulo = "$precio_por_click créd/click"
    )
    Spacer(Modifier.height(10.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "${data.clicks_whatsapp_hoy}",    "Clicks\nhoy",    MBotBlue)
        KpiCard(Modifier.weight(1f), "${data.clicks_whatsapp_semana}", "Clicks\n7 días", MBotBlue)
    }
    Spacer(Modifier.height(8.dp))
    // ✅ Créditos = clicks × precio_por_click
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "$creditosClicksHoy créd.",    "Créditos\nhoy",    MBotAmber)
        KpiCard(Modifier.weight(1f), "$creditosClicksSemana créd.", "Créditos\n7 días", MBotAmber)
    }
    Spacer(Modifier.height(8.dp))
    // Soles solo como referencia
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "≈S/ ${"%.2f".format(solesClicksHoy)}",    "Equiv.\nhoy",    MBotTextMuted)
        KpiCard(Modifier.weight(1f), "≈S/ ${"%.2f".format(solesClicksSemana)}", "Equiv.\n7 días", MBotTextMuted)
    }

    Spacer(Modifier.height(8.dp))
    TotalHistoricoRow(
        label         = "Total histórico — contacto directo",
        totalClicks   = data.total_clicks_historico,
        totalCreditos = (data.total_clicks_historico * precio_por_click).toLong(),
        totalSoles    = data.total_clicks_historico * precio_por_click * precio_por_moneda,
        colorClicks   = MBotBlue,
        colorCreditos = MBotAmber
    )

    Spacer(Modifier.height(14.dp))
    HorizontalDivider(color = MBotSurface3, thickness = 0.5.dp)
    Spacer(Modifier.height(14.dp))

    // ── BLOQUE 2 — PUBLICIDAD ENVIADA ─────────────────────────────────────
    SeccionHeader(
        titulo    = "PUBLICIDAD ENVIADA - Plantillas",
        subtitulo = "$precio_por_contacto créd/plantilla"
    )
    Spacer(Modifier.height(10.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "${data.enviados_hoy}",    "Enviados\nhoy",    MBotGreen)
        KpiCard(Modifier.weight(1f), "${data.enviados_semana}", "Enviados\n7 días", MBotGreen)
    }
    Spacer(Modifier.height(8.dp))
    // ✅ Créditos = enviados × precio_por_contacto
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "$creditosEnviadosHoy créd.",    "Créditos\nhoy",    MBotAmber)
        KpiCard(Modifier.weight(1f), "$creditosEnviadosSemana créd.", "Créditos\n7 días", MBotAmber)
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "≈S/ ${"%.2f".format(solesEnviadosHoy)}",    "Equiv.\nhoy",    MBotTextMuted)
        KpiCard(Modifier.weight(1f), "≈S/ ${"%.2f".format(solesEnviadosSemana)}", "Equiv.\n7 días", MBotTextMuted)
    }

    Spacer(Modifier.height(8.dp))
    TotalHistoricoRow(
        label         = "Total histórico — publicidad",
        totalClicks   = data.total_publicidad_enviada,
        totalCreditos = (data.total_publicidad_enviada * precio_por_contacto).toLong(),
        totalSoles    = data.total_publicidad_enviada * precio_por_contacto * precio_por_moneda,
        colorClicks   = MBotGreen,
        colorCreditos = MBotAmber,
        labelClicks   = "envíos"
    )

    // ── Insight rentabilidad ──────────────────────────────────────────────
    if (data.clicks_whatsapp_semana > 0 && data.enviados_semana > 0) {
        Spacer(Modifier.height(10.dp))

        val clicksPorEnvio   = data.clicks_whatsapp_semana.toFloat() / data.enviados_semana
        val costoPorClick    = if (data.clicks_whatsapp_semana > 0)
            solesClicksSemana / data.clicks_whatsapp_semana else 0.0
        val roi              = if (solesClicksSemana > 0)
            ((data.clicks_whatsapp_semana * precio_por_click * precio_por_moneda - solesClicksSemana)
                    / solesClicksSemana * 100) else 0.0
        val esRentable       = clicksPorEnvio >= 1f

        val colorInsight = if (esRentable) MBotGreen else MBotAmber

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorInsight.copy(alpha = 0.07f))
                .border(0.5.dp, colorInsight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = if (esRentable) Icons.Default.TrendingUp else Icons.Default.TrendingFlat,
                    contentDescription = null,
                    tint               = colorInsight,
                    modifier           = Modifier.size(18.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text       = if (esRentable) "Buena respuesta ✓" else "Respuesta baja",
                        color      = colorInsight,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text     = "${"%.1f".format(clicksPorEnvio)} clicks por plantilla · $creditosClicksSemana créd. en clicks · $creditosEnviadosSemana créd. en envíos",
                        color    = MBotTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text     = "≈ S/ ${"%.2f".format(solesEnviadosSemana)} invertido en publicidad → ${"%.2f".format(solesClicksSemana)} equiv. en clicks",
                        color    = MBotTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // ── BLOQUE 3 — HISTORIAL ──────────────────────────────────────────────
    if (data.historial_reciente.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MBotSurface3, thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))
        HistorialFiltrable(
            items             = data.historial_reciente,
            precio_por_moneda = precio_por_moneda
        )
    }
}

// ─── SECCIÓN HEADER ───────────────────────────────────────────────────────────

@Composable
private fun SeccionHeader(titulo: String, subtitulo: String) {
    Column {
        Text(titulo,    color = MBotTextMuted,                   fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
        Text(subtitulo, color = MBotTextMuted.copy(alpha = 0.6f), fontSize = 10.sp)
    }
}

// ─── FILA TOTAL HISTÓRICO ─────────────────────────────────────────────────────

@Composable
private fun TotalHistoricoRow(
    label        : String,
    totalClicks  : Int,
    totalCreditos: Long,
    totalSoles   : Double,
    colorClicks  : Color,
    colorCreditos: Color,
    labelClicks  : String = "clicks"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF141414))
            .border(0.5.dp, MBotBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MBotTextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total clicks / envíos
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(colorClicks))
                    Text("$totalClicks $labelClicks", color = colorClicks, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                // Total créditos
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(colorCreditos))
                    Text("$totalCreditos créd.", color = colorCreditos, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                // Total soles
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(MBotRed))
                    Text("S/ ${"%.2f".format(totalSoles)}", color = MBotRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (totalClicks > 0) {
                Text(
                    "≈ ${"%.2f".format(totalSoles / totalClicks)} soles por ${ if (labelClicks == "envíos") "envío" else "click" }",
                    color    = MBotTextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ─── HISTORIAL FILTRABLE ──────────────────────────────────────────────────────

@Composable
private fun HistorialFiltrable(
    items            : List<HistorialHotItem>,
    precio_por_moneda: Double
) {
    var filtroActivo by remember { mutableStateOf(FiltroHistorial.HOY) }

    val itemsFiltrados = remember(filtroActivo, items) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        val inicioDiaHoy = cal.timeInMillis
        val ahora        = System.currentTimeMillis()
        val unaSemana    = 7L  * 86_400_000L
        val unMes        = 30L * 86_400_000L

        when (filtroActivo) {
            FiltroHistorial.TODO   -> items
            FiltroHistorial.HOY    -> items.filter { it.timestamp != null && it.timestamp.time >= inicioDiaHoy }
            FiltroHistorial.SEMANA -> items.filter { it.timestamp != null && (ahora - it.timestamp.time) <= unaSemana }
            FiltroHistorial.MES    -> items.filter { it.timestamp != null && (ahora - it.timestamp.time) <= unMes }
        }
    }

    val totalMonedasFiltro = itemsFiltrados.sumOf { it.monedas_descontadas }
    val totalSolesFiltro   = totalMonedasFiltro * precio_por_moneda

    // Header
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("ÚLTIMOS ENVÍOS", color = MBotTextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
        Text("${itemsFiltrados.size} registros", color = MBotTextMuted, fontSize = 10.sp)
    }
    Spacer(Modifier.height(8.dp))

    // Chips filtro
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FiltroHistorial.entries.forEach { filtro ->
            val activo = filtro == filtroActivo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activo) MBotPurple else Color(0xFF141414))
                    .border(0.5.dp, if (activo) MBotPurple else MBotBorder, RoundedCornerShape(20.dp))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { filtroActivo = filtro }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = filtro.label,
                    color      = if (activo) Color.White else MBotTextMuted,
                    fontSize   = 11.sp,
                    fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    // Mini resumen del período con soles incluidos
    if (itemsFiltrados.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniResumen(Modifier.weight(1f), "${itemsFiltrados.size}",                                "envíos",          MBotGreen)
            MiniResumen(Modifier.weight(1f), "$totalMonedasFiltro",                                   "créditos",        MBotAmber)
            MiniResumen(Modifier.weight(1f), "S/ ${"%.2f".format(totalSolesFiltro)}",                 "costo real",      MBotRed)
        }
    }

    Spacer(Modifier.height(10.dp))

    if (itemsFiltrados.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF141414))
                .border(0.5.dp, MBotBorder, RoundedCornerShape(10.dp))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin envíos en este período", color = MBotTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            state               = rememberLazyListState(),
            modifier            = Modifier.fillMaxWidth().heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(itemsFiltrados, key = { it.id }) { item ->
                HistorialItem(item = item, precio_por_moneda = precio_por_moneda)
            }
            if (itemsFiltrados.size > 8) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text("${itemsFiltrados.size} registros · desliza para ver más", color = MBotTextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ─── MINI RESUMEN ─────────────────────────────────────────────────────────────

@Composable
private fun MiniResumen(modifier: Modifier, valor: String, label: String, color: Color) {
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141414))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, color = color,         fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(label, color = MBotTextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

// ─── KPI CARD ─────────────────────────────────────────────────────────────────

@Composable
private fun KpiCard(modifier: Modifier, valor: String, etiqueta: String, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141414))
            .border(0.5.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor,    color = color,         fontSize = 20.sp, fontWeight = FontWeight.Bold,   textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(etiqueta, color = MBotTextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

// ─── HISTORIAL ITEM ───────────────────────────────────────────────────────────

@Composable
private fun HistorialItem(item: HistorialHotItem, precio_por_moneda: Double) {
    val fmt        = SimpleDateFormat("dd MMM · HH:mm", Locale("es"))
    val costoSoles = item.monedas_descontadas * precio_por_moneda

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141414))
            .border(0.5.dp, MBotBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ícono con créditos descontados
        Box(
            modifier         = Modifier.size(36.dp).clip(CircleShape).background(MBotAmber.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("−${item.monedas_descontadas}", color = MBotAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        // Tipo y fecha
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = item.tipo.replace("_", " ").replaceFirstChar { it.uppercase() },
                color      = MBotTextSecondary,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text     = item.timestamp?.let { fmt.format(it) } ?: "—",
                color    = MBotTextMuted,
                fontSize = 10.sp
            )
            // Costo en soles de este envío
            Text(
                text     = "S/ ${"%.2f".format(costoSoles)}",
                color    = MBotRed.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Créditos restantes
        Column(horizontalAlignment = Alignment.End) {
            Text("${item.creditos_despues}", color = MBotGreen,    fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("créd. rest.",              color = MBotTextMuted, fontSize = 10.sp)
        }
    }
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────

@Composable
private fun MetricasEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141414))
            .border(0.5.dp, MBotBorder, RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(MBotPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.BarChart, null, tint = MBotPurple.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
        }
        Text("Aún no tienes métricas", color = MBotTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            "En cuanto Daniel recomiende tu negocio, verás aquí clicks, envíos y créditos.",
            color = MBotTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 17.sp
        )
    }
}

@Composable
private fun MetricasLoadingState() {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(color = MBotPurple, modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
            Text("Cargando métricas…", color = MBotTextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MetricasErrorState(mensaje: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A0A0A))
            .border(0.5.dp, Color(0xFF3A1A1A), RoundedCornerShape(10.dp))
            .padding(14.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onRetry() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("No se pudieron cargar las métricas", color = Color(0xFFFF6B6B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(mensaje, color = MBotTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text("Toca para reintentar", color = MBotPurple, fontSize = 11.sp)
    }
}
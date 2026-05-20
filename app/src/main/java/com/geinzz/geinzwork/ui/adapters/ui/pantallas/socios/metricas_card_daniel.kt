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
    id_tienda: String,
    modifier: Modifier = Modifier
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
                            else MetricasContenido(d)
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
private fun MetricasContenido(data: MetricasResumen) {

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 1 — CONTACTO DIRECTO (clicks a WhatsApp)
    // ════════════════════════════════════════════════════════════════════════
    SeccionHeader(
        titulo   = "CONTACTO DIRECTO - WhatsApp",
        subtitulo = "Clicks al botón de WhatsApp"
    )
    Spacer(Modifier.height(10.dp))

    // Hoy vs semana
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "${data.clicks_whatsapp_hoy}",    "Clicks\nhoy",      MBotBlue)
        KpiCard(Modifier.weight(1f), "${data.clicks_whatsapp_semana}", "Clicks\n7 días",   MBotBlue)
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "${data.monedas_gastadas_hoy}",    "Créditos\nhoy",    MBotAmber)
        KpiCard(Modifier.weight(1f), "${data.monedas_gastadas_semana}", "Créditos\n7 días", MBotAmber)
    }

    Spacer(Modifier.height(8.dp))

    // ── Total histórico contacto directo ──────────────────────────────────
    TotalHistoricoRow(
        label       = "Total histórico — contacto directo",
        totalClicks = data.total_clicks_historico,        // suma de todos los días
        totalCreditos = data.total_monedas_contacto_historico,
        colorClicks   = MBotBlue,
        colorCreditos = MBotAmber
    )

    Spacer(Modifier.height(14.dp))
    HorizontalDivider(color = MBotSurface3, thickness = 0.5.dp)
    Spacer(Modifier.height(14.dp))

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 2 — PUBLICIDAD ENVIADA (historial_hot)
    // ════════════════════════════════════════════════════════════════════════
    SeccionHeader(
        titulo    = "PUBLICIDAD ENVIADA - Plantillas",
        subtitulo = "Plantillas recomendadas por Daniel"
    )
    Spacer(Modifier.height(10.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(Modifier.weight(1f), "${data.total_publicidad_enviada}", "Plantillas\nenviadas", MBotGreen)
        KpiCard(Modifier.weight(1f), "${data.monedas_en_publicidad}",   "Créditos\ngastados",   MBotAmber)
    }

    Spacer(Modifier.height(8.dp))

    // ── Total histórico publicidad ─────────────────────────────────────────
    TotalHistoricoRow(
        label         = "Total histórico — publicidad",
        totalClicks   = data.total_publicidad_enviada,
        totalCreditos = data.monedas_en_publicidad,
        colorClicks   = MBotGreen,
        colorCreditos = MBotAmber,
        labelClicks   = "envíos"
    )

    // ── Insight rentabilidad ──────────────────────────────────────────────
    if (data.clicks_whatsapp_semana > 0 && data.total_publicidad_enviada > 0) {
        Spacer(Modifier.height(10.dp))

        val clicksPorEnvio = data.clicks_whatsapp_semana.toFloat() / data.total_publicidad_enviada
        val costoPorClick  = data.monedas_gastadas_semana.toFloat() / data.clicks_whatsapp_semana
        val esRentable     = clicksPorEnvio >= 2f

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
                        text       = if (esRentable) "Está siendo rentable ✓" else "Rentabilidad baja",
                        color      = colorInsight,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text     = "%.1f clicks por plantilla · %.2f créd/click".format(clicksPorEnvio, costoPorClick),
                        color    = MBotTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 3 — HISTORIAL FILTRABLE
    // ════════════════════════════════════════════════════════════════════════
    if (data.historial_reciente.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MBotSurface3, thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))
        HistorialFiltrable(items = data.historial_reciente)
    }
}

// ─── SECCIÓN HEADER CON ÍCONO ─────────────────────────────────────────────────

@Composable
private fun SeccionHeader(
    titulo:    String,
    subtitulo: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            Text(titulo,    color = MBotTextMuted,     fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            Text(subtitulo, color = MBotTextMuted.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

// ─── FILA TOTAL HISTÓRICO ─────────────────────────────────────────────────────

@Composable
private fun TotalHistoricoRow(
    label:         String,
    totalClicks:   Int,
    totalCreditos: Long,
    colorClicks:   Color,
    colorCreditos: Color,
    labelClicks:   String = "clicks"
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total clicks / envíos
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colorClicks)
                    )
                    Text(
                        text       = "$totalClicks $labelClicks",
                        color      = colorClicks,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Total créditos
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colorCreditos)
                    )
                    Text(
                        text       = "$totalCreditos créditos",
                        color      = colorCreditos,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Costo promedio si aplica
                if (totalClicks > 0) {
                    val prom = totalCreditos.toFloat() / totalClicks
                    Text(
                        text  = "≈ ${"%.1f".format(prom)} c/u",
                        color = MBotTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ─── HISTORIAL FILTRABLE ──────────────────────────────────────────────────────

@Composable
private fun HistorialFiltrable(items: List<HistorialHotItem>) {

    var filtroActivo by remember { mutableStateOf(FiltroHistorial.TODO) }

    val ahora     = System.currentTimeMillis()
    val unDia     = 86_400_000L
    val unaSemana = 7  * unDia
    val unMes     = 30 * unDia

    val itemsFiltrados = remember(filtroActivo, items) {
        when (filtroActivo) {
            FiltroHistorial.TODO   -> items
            FiltroHistorial.HOY    -> items.filter { it.timestamp != null && (ahora - it.timestamp.time) <= unDia }
            FiltroHistorial.SEMANA -> items.filter { it.timestamp != null && (ahora - it.timestamp.time) <= unaSemana }
            FiltroHistorial.MES    -> items.filter { it.timestamp != null && (ahora - it.timestamp.time) <= unMes }
        }
    }

    // ── Totales del filtro activo ─────────────────────────────────────────
    val totalMonedasFiltro = itemsFiltrados.sumOf { it.monedas_descontadas }

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
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        filtroActivo = filtro
                    }
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

    // ── Mini resumen del período filtrado ─────────────────────────────────
    if (itemsFiltrados.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniResumen(
                modifier  = Modifier.weight(1f),
                valor     = "${itemsFiltrados.size}",
                label     = "envíos",
                color     = MBotGreen
            )
            MiniResumen(
                modifier  = Modifier.weight(1f),
                valor     = "$totalMonedasFiltro",
                label     = "créditos gastados",
                color     = MBotAmber
            )
            MiniResumen(
                modifier  = Modifier.weight(1f),
                valor     = if (itemsFiltrados.isNotEmpty())
                    "≈${"%.1f".format(totalMonedasFiltro.toFloat() / itemsFiltrados.size)}"
                else "—",
                label     = "créd/envío",
                color     = MBotTextMuted
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    // Lista o empty
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
                HistorialItem(item = item)
            }
            if (itemsFiltrados.size > 8) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${itemsFiltrados.size} registros · desliza para ver más",
                            color = MBotTextMuted, fontSize = 10.sp
                        )
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
        Text(valor, color = color,         fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
            Text(valor,    color = color,         fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(etiqueta, color = MBotTextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

// ─── HISTORIAL ITEM ───────────────────────────────────────────────────────────

@Composable
private fun HistorialItem(item: HistorialHotItem) {
    val fmt = SimpleDateFormat("dd MMM · HH:mm", Locale("es"))
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
        Box(
            modifier         = Modifier.size(32.dp).clip(CircleShape).background(MBotAmber.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("−${item.monedas_descontadas}", color = MBotAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
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
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${item.creditos_despues}", color = MBotGreen,    fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("créditos rest.",           color = MBotTextMuted, fontSize = 10.sp)
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
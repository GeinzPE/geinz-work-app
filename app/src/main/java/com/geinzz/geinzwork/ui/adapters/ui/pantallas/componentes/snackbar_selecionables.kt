package com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
/**
 * Snackbar persistente y colapsable que muestra los filtros activos.
 *
 * Comportamiento:
 * - Aparece desde abajo con animación cuando hay al menos 1 filtro activo.
 * - Colapsado: muestra "N filtros activos" + chips inline de los primeros 2.
 * - Expandido: muestra cada grupo de filtro (Categoría, Precio, Pagos, Comodidades)
 *   con chips individuales eliminables + botón "Editar filtros" → abre el bottom sheet.
 * - Desaparece automáticamente cuando no hay ningún filtro activo.
 */
// ── Mapas de recursos para logos ─────────────────────────────────────────────
// ── Mapas de recursos para logos ─────────────────────────────────────────────
private val logosPago = mapOf(
    "yape"        to R.drawable.yape_logo,
    "plin"        to R.drawable.logo_plin,
    "visa"        to R.drawable.visa_logo,
    "mastercard"  to R.drawable.master_car_logo,
    "efectivo"    to R.drawable.efectivo_logo,
    "agora"       to R.drawable.logo_agora,
)

private val logosComodidad = mapOf(
    "wifi"                to R.drawable.icon_wifi,
    "zona_expandida"      to R.drawable.icon_zona_expandida,
    "servicios_higienicos" to  R.drawable.icon_servicios_higenicos,
    "camaras_seguridad"   to R.drawable.icon_seguridad,
    "sala_espera"         to R.drawable.icon_sala_de_espera,
    "sala_juegos"         to R.drawable.icon_sala_para_ninos,
    "mesa_para_ninos"     to R.drawable.icon_mesa_para_ninos,
    "estacionamiento"     to R.drawable.icon_estacionamiento,
    "enchufe"             to R.drawable.icon_enchufa,
    "aire_acondicionado"  to R.drawable.icon_aire_acondicionado,
    "ingreso_con_mascotas" to R.drawable.icon_ingreso_animales,
    )
// ── Paleta dark-first ─────────────────────────────────────────────────────────
// Un solo tono base con distintas opacidades para cada tipo de filtro.
// Evita los colores de pastel brillante que se ven mal en dark mode.
private data class ChipColors(val bg: Color, val border: Color, val text: Color)

@Composable
private fun chipColorsFor(tipo: String): ChipColors = when (tipo) {
    "precio"           -> ChipColors(
        bg     = Color(0xFF0D2137),
        border = Color(0xFF1E5080),
        text   = Color(0xFF5EB8FF)
    )
    "pago"             -> ChipColors(
        bg     = Color(0xFF0D1F0D),
        border = Color(0xFF2A6B2A),
        text   = Color(0xFF5FD65F)
    )
    "categoria", "sub" -> ChipColors(
        bg     = Color(0xFF1A0D2E),
        border = Color(0xFF5A3080),
        text   = Color(0xFFB380FF)
    )
    "comodidad"        -> ChipColors(
        bg     = Color(0xFF1F1100),
        border = Color(0xFF7A4A00),
        text   = Color(0xFFFFAA33)
    )
    else               -> ChipColors(
        bg     = Color(0xFF1A1A1A),
        border = Color(0xFF333333),
        text   = Color(0xFF999999)
    )
}

// ── Composable principal ──────────────────────────────────────────────────────
@Composable
fun FiltroSnackbarActivo(
    categoriaSeleccionada: String,
    subcategoriasSeleccionadas: List<String>,
    rangoPrecio: String?,
    metodosPago: Set<String>,
    comodidades: Set<String>,
    onEditarFiltros: () -> Unit,
    onLimpiarTodo: () -> Unit,
    onQuitarCategoria: () -> Unit,
    onQuitarSubcategoria: (String) -> Unit,
    onQuitarRango: () -> Unit,
    onQuitarPago: (String) -> Unit,
    onQuitarComodidad: (String) -> Unit,
) {
    var expandido by remember { mutableStateOf(false) }

    val hayCategoria   = categoriaSeleccionada.isNotEmpty() && categoriaSeleccionada != "Todos"
    val hayPrecio      = !rangoPrecio.isNullOrEmpty() && rangoPrecio != "Sin precio"
    val hayPagos       = metodosPago.isNotEmpty()
    val hayComodidades = comodidades.isNotEmpty()
    val haySubs        = subcategoriasSeleccionadas.isNotEmpty()

    data class ChipResumen(val texto: String, val tipo: String, val resDrawable: Int? = null)

    val resumen = buildList {
        if (hayCategoria) add(ChipResumen(categoriaSeleccionada.capitalizeFirst(), "categoria"))
        subcategoriasSeleccionadas.forEach { add(ChipResumen(it.capitalizeFirst(), "sub")) }
        if (hayPrecio) add(ChipResumen("S/ $rangoPrecio", "precio"))
        metodosPago.forEach  { add(ChipResumen(it.capitalizeFirst(), "pago",      logosPago[it])) }
        comodidades.forEach  { add(ChipResumen(it.capitalizeFirst(), "comodidad", logosComodidad[it])) }
    }

    val hayFiltros = resumen.isNotEmpty()

    LaunchedEffect(hayFiltros) { if (!hayFiltros) expandido = false }

    AnimatedVisibility(
        visible = hayFiltros,
        enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it },
        exit  = fadeOut(tween(180)) + slideOutVertically(tween(220)) { it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { if (!expandido) expandido = true },
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF111111),
            border = BorderStroke(0.5.dp, Color(0xFF2A2A2A))
        ) {
            Column(modifier = Modifier.animateContentSize(tween(300))) {

                // ── Fila siempre visible ──────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dot pulsante
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5EB8FF))
                    )

                    Text(
                        text = if (resumen.size == 1) "1 filtro activo"
                        else "${resumen.size} filtros activos",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Medium
                    )

                    // Chips inline primeros 2
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        resumen.take(2).forEach { chip ->
                            FiltroChipMini(
                                texto      = chip.texto,
                                tipo       = chip.tipo,
                                resDrawable = chip.resDrawable
                            )
                        }
                        if (resumen.size > 2) {
                            FiltroChipMini("+${resumen.size - 2}", "neutro")
                        }
                    }

                    // Chevron
                    IconButton(
                        onClick = { expandido = !expandido },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (expandido) Icons.Default.KeyboardArrowDown
                            else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF666666)
                        )
                    }
                }

                // ── Cuerpo expandido ──────────────────────────────────────
                if (expandido) {
                    HorizontalDivider(color = Color(0xFF222222), thickness = 0.5.dp)
                    Column(
                        modifier = Modifier.padding(
                            start = 14.dp, end = 14.dp,
                            top = 12.dp, bottom = 14.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        if (hayCategoria) {
                            FiltroGrupoExpandido("Categoría") {
                                FiltroChipEliminable(
                                    texto       = categoriaSeleccionada.capitalizeFirst(),
                                    tipo        = "categoria",
                                    resDrawable = null,
                                    onEliminar  = { onEditarFiltros() },  // 🔥 abre bottom sheet
                                    solo_visual = true                     // 🔥 sin X
                                )
                            }
                        }

                        if (haySubs) {
                            FiltroGrupoExpandido("Subcategorías") {
                                subcategoriasSeleccionadas.forEach { sub ->
                                    FiltroChipEliminable(
                                        texto       = sub.capitalizeFirst(),
                                        tipo        = "sub",
                                        resDrawable = null,
                                        onEliminar  = { onEditarFiltros() },  // 🔥 abre bottom sheet
                                        solo_visual = true                     // 🔥 oculta la X
                                    )
                                }
                            }
                        }

                        if (hayPrecio) {
                            FiltroGrupoExpandido("Rango de precio") {
                                FiltroChipEliminable(
                                    texto       = "S/ $rangoPrecio",
                                    tipo        = "precio",
                                    resDrawable = null,
                                    onEliminar  = { onEditarFiltros() },
                                    solo_visual = true
                                )
                            }
                        }
                        if (hayPagos) {
                            FiltroGrupoExpandido("Métodos de pago") {
                                metodosPago.forEach { pago ->
                                    FiltroChipEliminable(
                                        texto       = pago.capitalizeFirst(),
                                        tipo        = "pago",
                                        resDrawable = logosPago[pago],
                                        onEliminar  = { onEditarFiltros() },
                                        solo_visual = true
                                    )
                                }
                            }
                        }


                        if (hayComodidades) {
                            FiltroGrupoExpandido("Comodidades") {
                                comodidades.forEach { comod ->
                                    FiltroChipEliminable(
                                        texto       = comod.capitalizeFirst(),
                                        tipo        = "comodidad",
                                        resDrawable = logosComodidad[comod],
                                        onEliminar  = { onEditarFiltros() },
                                        solo_visual = true
                                    )
                                }
                            }
                        }

                        // Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onEditarFiltros() },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.5.dp, Color(0xFF333333)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFF999999)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "Editar filtros",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF999999)
                                )
                            }
                            TextButton(
                                onClick = { onLimpiarTodo() },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Limpiar todo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Chip mini (fila colapsada) ────────────────────────────────────────────────
@Composable
private fun FiltroChipMini(
    texto: String,
    tipo: String,
    resDrawable: Int? = null
) {
    val colors = chipColorsFor(tipo)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.bg,
        border = BorderStroke(0.5.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (resDrawable != null) {
                Image(
                    painter = painterResource(id = resDrawable),
                    contentDescription = null,
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Grupo expandido con título ────────────────────────────────────────────────
@Composable
private fun FiltroGrupoExpandido(
    titulo: String,
    content: @Composable FlowRowScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF444444),
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

// ── Chip eliminable (cuerpo expandido) ───────────────────────────────────────
@Composable
private fun FiltroChipEliminable(
    texto: String,
    tipo: String,
    resDrawable: Int?,
    onEliminar: () -> Unit,
    solo_visual: Boolean = false   // 🔥 nuevo
) {
    val colors = chipColorsFor(tipo)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.bg,
        border = BorderStroke(0.5.dp, colors.border),
        modifier = if (solo_visual) Modifier.clickable { onEliminar() } else Modifier  // 🔥 click en todo el chip
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (resDrawable != null) {
                Image(
                    painter = painterResource(id = resDrawable),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = colors.text
            )
            // 🔥 Solo muestra la X si NO es solo_visual
            if (!solo_visual) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colors.text.copy(alpha = 0.10f))
                        .clickable { onEliminar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar $texto",
                        modifier = Modifier.size(9.dp),
                        tint = colors.text.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_todas_las_tiendas(
    tiendas: List<tiendas_con_mas_de_una_promo>,
    tiendaSeleccionada: String?,
    onTiendaClick: (tiendas_con_mas_de_una_promo) -> Unit,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 🔥 categorías únicas
    val categorias = remember(tiendas) {
        listOf("Todas") + tiendas
            .map { it.categoira }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    var categoriaFiltro by remember { mutableStateOf("Todas") }

    // 🔥 tiendas filtradas por categoría + seleccionada primero
    val tiendasMostradas by remember(tiendas, tiendaSeleccionada, categoriaFiltro) {
        derivedStateOf {
            val filtradas = if (categoriaFiltro == "Todas") tiendas
            else tiendas.filter { it.categoira == categoriaFiltro }

            val seleccionada = filtradas.find { it.id == tiendaSeleccionada }
            val resto = filtradas.filter { it.id != tiendaSeleccionada }
            if (seleccionada != null) listOf(seleccionada) + resto else filtradas
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 20.dp)
            ) {
                // ── Título ──────────────────────────────────────────
                Text(
                    text = "Tiendas cerca de ti",
                    style = MaterialTheme.typography.banerGeinzWork,
                    color = Color.White,
                    fontSize = 25.sp
                )
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "Explora y filtra por negocio o categoría",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(14.dp)

                // ── Chips de categoría ───────────────────────────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categorias) { cat ->
                        val seleccionado = categoriaFiltro == cat
                        chisp_filtrado_busqueda(
                            carta_selecionada = seleccionado,
                            filtrado = cat.capitalizeFirst(),
                            btn_visible = false,
                            clik_card = { categoriaFiltro = cat },
                            onClick_delete = {}
                        )
                    }
                }

                spacer_vertical(16.dp)

                // ── Grid de tiendas ──────────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    items(
                        items = tiendasMostradas,
                        key = { it.id }
                    ) { tienda ->
                        val seleccionada = tienda.id == tiendaSeleccionada

                        val scaleAnim by animateFloatAsState(
                            targetValue = if (seleccionada) 1.12f else 1f,
                            animationSpec = tween(300),
                            label = ""
                        )
                        val alphaAnim by animateFloatAsState(
                            targetValue = if (seleccionada) 1f else 0f,
                            animationSpec = tween(300),
                            label = ""
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                                .width(70.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(top = 10.dp).size(66.dp)
                            ) {
                                // 🔹 Ring seleccionado
                                if (seleccionada) {
                                    Box(
                                        modifier = Modifier
                                            .size(66.dp)
                                            .scale(scaleAnim)
                                            .alpha(alphaAnim)
                                            .border(
                                                width = 3.dp,
                                                color = Color(0xFF7B2CBF),
                                                shape = CircleShape
                                            )
                                    )
                                }

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(tienda.logo_img)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            onTiendaClick(tienda)
                                            onClose()
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            texto_generico_one_line(
                                tienda.nombre_tienda,
                                style = MaterialTheme.typography.bodySmall,

                            )

                            if (tienda.categoira.isNotEmpty()) {
                                texto_generico_one_line(
                                    tienda.categoira.capitalizeFirst(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF7B2CBF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
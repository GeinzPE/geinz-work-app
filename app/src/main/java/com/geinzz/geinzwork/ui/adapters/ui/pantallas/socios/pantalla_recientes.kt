package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.publicaciones_notificaciones_geinz
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_datos_promos_noti
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_recientes
import com.valentinilk.shimmer.shimmer

@Composable
fun PantallaRecientes(
    id_tienda: String,
    localidad_tienda: String,
    viewModelPantallasRecientes: viewmodel_pantallas_recientes = viewModel(),
    cargando: (Boolean) -> Unit
) {

    // Observamos el estado del ViewModel
    val estadoPromoNoti by viewModelPantallasRecientes.estadoPromoNoti.collectAsState()

    // Lanzamos la carga de datos al inicio
    LaunchedEffect(Unit) {
        viewModelPantallasRecientes.obtner_noti_promo(id_tienda, localidad_tienda)
        viewModelPantallasRecientes.obtener_estadotiempo_real_promociones(id_tienda, localidad_tienda)
    }
    val lsita_fitlrado_opciones = listOf(
        "Todos",
        "Promociones o ofertas",
        "Notificaciones",
        "Vencidos",
        "Activos",
        "Por vencer",
        "En pausa"
    )
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }
    var bottom_sheet_datos_competos by remember { mutableStateOf(false) }
    var id_promo_select by remember { mutableStateOf("") }
    // UI principal

    Crossfade(targetState = estadoPromoNoti) { curren_state ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (curren_state) {
                is viewmodel_pantallas_recientes.EstadoPromoNoti.Cargando -> {
                    ShimmerImagenConMarca()
                }

                is viewmodel_pantallas_recientes.EstadoPromoNoti.Success -> {
                    val lista =
                        curren_state.lista
                    val listaOrdenada = ordenarPorVence(lista)
                    val listaFiltrada = when (subCategoriaSeleccionada) {

                        "Todos" -> listaOrdenada

                        "Promociones o ofertas" ->
                            listaOrdenada.filter { it.tipo == "promoción" }

                        "Notificaciones" ->
                            listaOrdenada.filter { it.tipo == "notificación" }

                        "Vencidos" ->
                            listaOrdenada.filter { viewModelPantallasRecientes.esVencido(it.vence) }

                        "Activos" ->
                            listaOrdenada.filter { viewModelPantallasRecientes.esActivo(it.vence) }

                        "Por vencer" ->
                            listaOrdenada.filter { viewModelPantallasRecientes.esPorVencer(it.vence) }

                        else -> listaOrdenada
                    }

                    val listState = rememberLazyListState()
                    val targetAlpha = if (listState.canScrollForward) 1f else 0f
                    val alphaAnim by animateFloatAsState(
                        targetValue = targetAlpha,
                        animationSpec = tween(durationMillis = 500)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    fontFamily = baners_geinz_work,
                                    text = "Tus Publicaciones y Notificaciones",
                                    color = Color.White,
                                    fontSize = 25.sp
                                )
                                spacer_vertical(10.dp)
                                texto_generico_multilinea(
                                    "Aquí puedes ver todas tus promociones activas y las notificaciones que has enviado a tus seguidores.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                spacer_vertical(10.dp)
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

                            item {
                                spacer_vertical(10.dp)
                            }
                            if (listaFiltrada.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxHeight()
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        texto_generico_one_line(
                                            "Aún no hay registros en este filtro",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                items(
                                    items = listaFiltrada,
                                    key = { item ->
                                        "${item.id}_${item.tipo}_${item.realizado}"
                                    }
                                ) { item ->
                                    Box(
                                        modifier = Modifier.animateItem(
                                            placementSpec = tween(
                                                durationMillis = 350,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    ) {
                                        item_recientes(localidad_tienda,id_tienda,viewModelPantallasRecientes,item = item, item_clikeado = { id_promo ->
                                            bottom_sheet_datos_competos = true
                                            id_promo_select = id_promo
                                        }, cambiar_a_pausar = {nuevo_estado,id_promo->
                                                viewModelPantallasRecientes.cambiar_estado_promociones(id_tienda,localidad_tienda,id_promo,nuevo_estado)
                                        })
                                    }
                                }
                            }
                            item {
                                spacer_vertical(30.dp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black
                                        )
                                    )
                                )
                                .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
                        )
                    }
                }

                is viewmodel_pantallas_recientes.EstadoPromoNoti.Vacío -> {

                    Text(
                        text = "No hay notificaciones ni promociones",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                is viewmodel_pantallas_recientes.EstadoPromoNoti.Error -> {
                    val mensaje =
                        curren_state.mensaje
                    Text(
                        text = "Error: $mensaje",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }


            }
            if (bottom_sheet_datos_competos) {
                bottom_sheet_datos_promos_noti(
                    viewModelPantallasRecientes,
                    id_tienda,
                    localidad_tienda,
                    id_promo_select,
                    {
                        bottom_sheet_datos_competos = false
                    })
            }
        }

    }
}

fun ordenarPorVence(lista: List<publicaciones_notificaciones_geinz>): List<publicaciones_notificaciones_geinz> {
    return lista.sortedByDescending { item ->
        val vence = item.vence.trim() // "12 dias" o "9 horas"
        val parts = vence.split(" ")
        if (parts.size != 2) return@sortedByDescending 0 // fallback

        val cantidad = parts[0].toIntOrNull() ?: 0
        val unidad = parts[1].lowercase()

        when {
            unidad.startsWith("dia") -> cantidad * 24 // convertir días a horas
            unidad.startsWith("hora") -> cantidad    // ya está en horas
            else -> 0
        }
    }
}


@Composable
fun item_recientes(
    localidad_tienda: String,id_tienda: String,
    viewModel: viewmodel_pantallas_recientes,
    item: publicaciones_notificaciones_geinz,
    item_clikeado: (String) -> Unit,
    cambiar_a_pausar: (tipo: String, id_promo: String) -> Unit
) {
    val diasRestantes = item.vence
        .substringBefore(" ") // toma solo el número antes del primer espacio
        .toLongOrNull() ?: 0L


    val estados by viewModel.estado_promociones.collectAsState()
    val estadoSwitch = estados[item.id] ?: item.estado_publicacion


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2B2B2B))
            .clickable {
                item_clikeado(item.id)
            }
    ) {

        AsyncImage(
            model = item.img_principal,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(120.dp)
                .width(100.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)),
            colorFilter = if (item.vence == "0 dias") {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) // Blanco y negro
            } else {
                null
            },
            placeholder = painterResource(R.drawable.cargando_img_categorias),
            error = painterResource(R.drawable.cargando_img_categorias)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp)
                .align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                texto_generico_one_line(
                                    item.tipo.capitalizeFirst(),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Image(
                                    painter = painterResource(
                                        if (item.tipo.equals("notificación")) {
                                            R.drawable.campana_3d_webp
                                        } else if (item.tipo.equals(
                                                "promoción"
                                            )
                                        ) {
                                            R.drawable.promocio_iconn
                                        } else {
                                            R.drawable.logo_geinz_500x500
                                        }
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            spacer_vertical(4.dp)
                            texto_generico_one_line(
                                item.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                        }

                        if (item.tipo.equals("promoción") && item.vence != "0 dias") {
                            Switch(
                                checked = estadoSwitch == "activo",
                                onCheckedChange = { isChecked ->
                                    val nuevoEstado = if (isChecked) "activo" else "pausado"
                                    cambiar_a_pausar(nuevoEstado, item.id)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        texto_generico_one_line(
                            "Tipo : ${item.estado.capitalizeFirst()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Image(
                            painter = painterResource(
                                if (item.estado.equals("dias")) {
                                    R.drawable.por_dias_icon_3d
                                } else if (item.estado.equals("horas")) {
                                    R.drawable.reloj_icon_hora_3d
                                } else if (item.estado.equals("Enviado")) {
                                    R.drawable.check_enviado_3d_icon
                                } else {
                                    R.drawable.logo_geinz_500x500

                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                }

            }
            texto_generico_one_line(
                "Realizado: ${item.realizado}",
                style = MaterialTheme.typography.bodyMedium
            )


            if (item.tipo.equals("promoción")) {
                texto_generico_one_line(
                    "Vence en: ${item.vence}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorPorVencimiento(item.vence)
                )
            } else {
                texto_generico_one_line(
                    "Notificacion enviada correctamente",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }


        }
    }
}

fun colorPorVencimiento(vence: String): Color {
    return when {
        vence.contains("dias") -> {
            // extraemos el número de días
            val dias = vence.substringBefore(" ").toLongOrNull() ?: 0L
            when {
                dias > 10 -> Color(0xFF4CAF50) // verde
                dias in 2..10 -> Color(0xFF9C27B0) // morado
                dias <= 1 -> Color(0xFFF44336)
                dias.toInt() == 0 -> Color.Gray// rojo
                else -> Color.Gray
            }
        }

        vence.contains("horas") || vence.contains("minuto") -> {
            Color(0xFFF44336) // menos de 1 día → rojo
        }

        else -> Color.Gray
    }
}

@Composable
fun ShimmerImagenConMarca(texto:String="GEINZ") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shimmer()
            .background(Color(0xFF1C1C1C)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = texto,
                color = Color.White.copy(alpha = 0.8f), // un poco más visible
                fontSize = 50.sp,                        // más grande
                fontWeight = FontWeight.Bold
            )
        }
    }
}




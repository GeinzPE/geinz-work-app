package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.geinzz.geinzwork.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.data.model.localizate_geinz.botom_shet_turismobtn
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_cecanas_km
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.repo_lugares_turisticos

import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.verificar_hora_abierta_ykm
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.retornar_color_estado_tienda
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.calcularDistanciaKm
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.data_redes_tiendas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.lista_turismo_bottom_sheet
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_lugares_turisticos(
    datos: lugares_turisticos,
    onClose: () -> Unit,
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val viewmodel_filtrado: viewModel_filtado_tiendas = viewModel()
    var id_tienda by remember { mutableStateOf("") }
    var localida_tienda by remember { mutableStateOf("") }
    var color_estado_tienda by remember { mutableStateOf(Color.Gray) }
    var mostrar_bottom_datos by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewmodel_filtrado._datos_tienda.observeAsState()
    val tick by viewmodel_filtrado.tick.collectAsState()
    LaunchedEffect(mostrar_bottom_datos) {
        if (mostrar_bottom_datos) {
            viewmodel_filtrado.obtener_campos_tiendas_por_id(
                localida_tienda ?: "barranca",
                id_tienda
            )
        }
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }

    Surface {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            val rep = repo_lugares_turisticos()
            var lista by remember { mutableStateOf<List<lugares_cercanos>>(emptyList()) }


            LaunchedEffect(Unit) {
                rep.obtenerTiendasCercanas(datos.latitud, datos.longitud, 1.0, "barranca") { i ->
                    lista = i
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                card_img_container(
                    firebaseAuth,
                    datos, tick,
                    lista,
                    { id, localidad, color ->
                        mostrar_bottom_datos = true
                        id_tienda = id
                        localida_tienda = localidad
                        color_estado_tienda = color
                    })
            }
        }
    }
    if (mostrar_bottom_datos) {
        bottom_sheet_tiendas_filtradas(
            color_estado_tienda,
            viewmodel_filtrado,
            dataclass_tienda_seleccionada, mostrar_bottom_datos
        ) {
            mostrar_bottom_datos = false
        }
    }
}


@Composable
fun card_img_container(
    firebaseAuth1: FirebaseAuth,
    datos: lugares_turisticos,
    tick: Long,
    lista_items: List<lugares_cercanos>,
    clik_card: (String, String, Color) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        spacer_vertical(10.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(text = datos.titulo, fontFamily = baners_geinz_work, fontSize = 25.sp)
            spacer_horizonta(10.dp)
            abierto_flag("Abierto las 24h")
        }
        spacer_vertical(10.dp)

        texto_generico_multilinea(
            datos.descripcion,
            MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(10.dp)
        )
        spacer_vertical(10.dp)
        CollageGoogleMapsStyle(aspectRatio = 1.1f, with = 310.dp, imagenes = datos.lista_img)
        spacer_vertical(10.dp)
        val lista_datos = listOf("Ir al lugar", "ver en mapa", "compartir", "guardar")
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        chips_filtrado(lista_turismo_bottom_sheet)
        spacer_vertical(10.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            spacer_vertical(10.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                texto_generico_multilinea(
                    "Lugares que no puedes perderte",
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)
                )
                texto_generico_one_line(
                    "ver en mapa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            spacer_vertical(10.dp)
            texto_generico_multilinea(
                "Descubre sitios cercanos a ${datos.titulo} y disfruta lo mejor a menos de 3 km.",
                style = MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
        }
        var expandedItemId by remember { mutableStateOf<String?>(null) }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp),
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(
                start = 7.dp,
                end = 7.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        ) {
            items(lista_items, key = { it.id_tienda }) { item ->
                Box(
                    modifier = Modifier.animateContentSize()
                ) {
                    item_cercanos(
                        expanded = expandedItemId == item.id_tienda,
                        tick,
                        datos = datos,
                        item = item,
                        onExpand = { id ->
                            expandedItemId = if (expandedItemId == id) null else id
                        },
                        clik_card = { id, localidad, color ->
                            coroutineScope.launch {
                                val index = lista_items.indexOf(item)
                                listState.animateScrollToItem(index)
                                clik_card(id, localidad, color)
                            }
                        })
                }
            }
        }

        Log.d("obtemos_cordenads", "${datos.latitud}, ${datos.longitud}")
    }
}

@Composable
fun item_cercanos(
    expanded: Boolean,
    tick: Long,
    datos: lugares_turisticos,
    item: lugares_cercanos,
    onExpand: (String) -> Unit,
    clik_card: (String, String, Color) -> Unit
) {

    var estado_color by remember { mutableStateOf(Color.Gray) }
    var mostar_dialog_km by remember { mutableStateOf(false) }
    var datosdialog_km by remember { mutableStateOf(tiendas_cecanas_km()) }
//    val tick by viewModelFiltros.tick.collectAsState()
    val animatedWidth by animateDpAsState(
        targetValue = if (expanded) 200.dp else 160.dp,
        label = "widthAnim"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (expanded) 270.dp else 160.dp,
        label = "heightAnim"
    )
    val widthImg by animateDpAsState(
        targetValue = if (expanded) 130.dp else 160.dp,
        label = "heightAnim"
    )

    Column {
        Box(
            modifier = Modifier
                .width(animatedWidth)
                .height(animatedHeight)
                .animateContentSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (expanded) 16.dp else 0.dp,
                        bottomEnd = if (expanded) 16.dp else 0.dp
                    )
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        clik_card(item.id_tienda, "barranca", estado_color)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(widthImg)
                        .height(animatedHeight)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.logo_tienda)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (expanded) 16.dp else 20.dp,
                                    bottomEnd = if (expanded) 16.dp else 20.dp
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onExpand(item.id_tienda)
                            }
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop

                    )
                    val distanciaKm = calcularDistanciaKm(
                        datos.latitud,
                        datos.longitud,
                        item.latitud,
                        item.longitud
                    )
                    spacer_horizonta(5.dp)
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                mostar_dialog_km = true
                                datosdialog_km = tiendas_cecanas_km(
                                    img_tienda = item.logo_tienda,
                                    nombre_tienda = item.nombre_tienda,
                                    kl = "%.2f km".format(distanciaKm),
                                    nombre_lugar = datos.titulo,
                                    color = estado_color, horario_total = item.horario_dia,
                                    hora_cierre = item.horario_dia.h_cierre,
                                    cerrado = item.horario_dia.cerrado,
                                    motivo = item.horario_dia.motivo,
                                    tick = tick
                                )
                            }
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(10.dp)
                                .animateContentSize()
                        ) {
                            retornar_color_estado_tienda(
                                horario_total = item.horario_dia,
                                hCierre = item.horario_dia.h_cierre,
                                cerrado = item.horario_dia.cerrado,
                                motivo = item.horario_dia.motivo, tick = tick
                            ) { color ->
                                estado_color = color
                            }
                            texto_generico_one_line(
                                "A: %.2f km".format(distanciaKm),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            spacer_horizonta(5.dp)
                            AnimatedVisibility(
                                visible = !expanded,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(estado_color)
                                )
                            }
                        }

                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
                ) {
                    val lista_redes_tiendas = listOf(
                        data_redes_tiendas(
                            enable = item.contacto_tienda.llamada.estado,
                            icono = R.drawable.llamada_icon,
                            nombre_red = "llamar",
                            valor = item.contacto_tienda.llamada.numero
                        ),
                        data_redes_tiendas(
                            enable = item.contacto_tienda.whatsapp.estado,
                            icono = R.drawable.whatsapp_icon,
                            nombre_red = "whatsapp",
                            valor = item.contacto_tienda.whatsapp.numero
                        ),
                        data_redes_tiendas(
                            enable = item.contacto_tienda.tiktok.estado,
                            icono = R.drawable.tik_tok_icon,
                            nombre_red = "tiktok",
                            valor = item.contacto_tienda.tiktok.url
                        ),
                        data_redes_tiendas(
                            enable = item.contacto_tienda.facebook.estado,
                            icono = R.drawable.facebook_icon,
                            nombre_red = "facebook",
                            valor = item.contacto_tienda.facebook.url
                        ),
                        data_redes_tiendas(
                            enable = item.contacto_tienda.instagram.estado,
                            icono = R.drawable.instagram_icon,
                            nombre_red = "instagram",
                            valor = item.contacto_tienda.instagram.url
                        ),
                        data_redes_tiendas(
                            enable = item.contacto_tienda.sitio_web.estado,
                            icono = R.drawable.sitio_web,
                            nombre_red = "web",
                            valor = item.contacto_tienda.sitio_web.url
                        )
                    )
                    LazyColumn(

                            modifier = Modifier
                                .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 15.dp ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)

                    ) {
                        items(lista_redes_tiendas.filter { it.enable }) { i ->
                            Log.d("lsitaeclicalda", lista_redes_tiendas.toString())
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(i.icono),
                                    contentDescription = i.nombre_red,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)

                                        .clickable {
//                                                onclick_iconos(i)
                                        }
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        // acción personalizada (por ejemplo abrir ajustes)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar, // ejemplo: ícono de “+”
                                    contentDescription = "Agregar red",
                                    tint = Color.White
                                )
                            }
                        }
                    }



                }
            }
        }
        AnimatedVisibility(!expanded,

            enter = slideInVertically(
                // Entra desde arriba
                initialOffsetY = { -it }
            ) + fadeIn(),
            exit = slideOutVertically(
                // Sale hacia arriba
                targetOffsetY = { -it }
            ) + fadeOut(), modifier = Modifier.zIndex(-1f)) {
            Column(
                modifier = Modifier
                    .width(widthImg)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { clik_card(item.id_tienda, "barranca", estado_color) },
            ) {
                spacer_vertical(7.dp)
                texto_generico_one_line(
                    item.nombre_tienda,
                    MaterialTheme.typography.titleMedium
                )
                spacer_vertical(7.dp)
                texto_generico_one_line(
                    item.categoria,
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(7.dp)
                tags_subcateogiras(
                    item.lista_subcategoiras,
                    brush_start = Brush.horizontalGradient(colors = shadow_top_filtrado_v1),
                    brush_end = Brush.horizontalGradient(colors = shadow_botonm_filtrado_v1)
                )
                spacer_vertical(7.dp)
            }
        }
    }
    if (mostar_dialog_km) {
        verificar_hora_abierta_ykm(datosdialog_km, { mostar_dialog_km = false })
    }
}


@Composable
fun chips_filtrado(list: List<botom_shet_turismobtn>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(
            start = 7.dp,
            end = 7.dp,
            top = 8.dp,
            bottom = 8.dp
        ),
    ) {
        items(list) { i ->
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .height(45.dp)
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = i.icono,
                    contentDescription = i.txt,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )

             spacer_horizonta(8.dp)

                texto_generico_one_line(
                    i.txt.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

    }
}


@Composable
fun abierto_flag(texto: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFF43A047)) // Verde intermedio, más natural
    ) {
        texto_generico_one_line(
            texto.capitalizeFirst(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color.White
        )
    }
}

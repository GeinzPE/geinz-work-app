package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.EstadisticaAccion
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.ui.adapters.ui.BotonCompartirReddit
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.BuyerPersonaCard
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.GraficosPromosMPAndroidChart
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.generarBuyerPersona
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_recientes


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_datos_notificacion(
    viewmodel_pantalla: viewmodel_pantallas_recientes,
    id_tienda: String,
    localida: String,
    id_promo: String,
    ondimis: () -> Unit
) {
    var estadisticaSeleccionadaTipo by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val estado_datos by viewmodel_pantalla.estado_notificacion.collectAsState()
    var estadisticaSeleccionada by remember {
        mutableStateOf<EstadisticaAccion?>(null)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(id_tienda, localida, id_promo) {
        viewmodel_pantalla.cargar_datos_notificacion(id_tienda, localida, id_promo)
    }
    var listaImgContainer by remember {
        mutableStateOf<List<String>>(emptyList())
    }


    var indexSelect by remember { mutableIntStateOf(0) }

    var mostrarDialogozoom by remember { mutableStateOf(false) }



    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    )
    {
        FuenteControladaApp {
            when (estado_datos) {
                is viewmodel_pantallas_recientes.EstadoDatosNotificacion.Error -> {
                    Text((estado_datos as viewmodel_pantallas_recientes.EstadoDatosNotificacion.Error).mensaje)
                }

                viewmodel_pantallas_recientes.EstadoDatosNotificacion.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ShimmerImagenConMarca()
                    }

                }

                is viewmodel_pantallas_recientes.EstadoDatosNotificacion.Success -> {
                    val datos =
                        (estado_datos as viewmodel_pantallas_recientes.EstadoDatosNotificacion.Success).datos
                    val estadisticas = datos.EventoEstadisticas

                    Log.d("esataidisatanoticviaon", "$estadisticas")

//                    val totalClick = estadisticas?.click?.total ?: 0
//                    val totalVistas = estadisticas?.vistas?.total ?: 0
//                    val totalCompartidos = estadisticas?.compartidos?.total ?: 0
//                    val totalWhatsapp = estadisticas?.whatsapp?.total ?: 0
//                    val color_estado = when (datos.estado) {
//                        "activo" -> Color.Green
//                        "pausa" -> Color.Gray
//                        "vencido" -> Color.Red
//                        else -> {
//                            Color.Red
//                        }
//                    }
//                    val icono_horas_dias = when (datos.horas_o_fecha) {
//                        "dias" -> R.drawable.por_dias_icon_3d
//                        "horas" -> R.drawable.reloj_icon_hora_3d
//                        else -> {
//                            R.drawable.logo_geinz_500x500
//                        }
//                    }
//                    listaImgContainer = datos.lista_img
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()

                    ) {

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()

                                .padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = 10.dp,
                                    bottom = 20.dp
                                )
                                .nestedScroll(rememberNestedScrollInteropConnection()),
                            verticalArrangement = Arrangement.spacedBy(25.dp)
                        ) {

                            item {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(datos.datos_de_notificacion.img_notifiacion)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .placeholder(R.drawable.cargando_img_categorias)
                                        .error(R.drawable.cargando_img_categorias)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(250.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(5))
                                        .clickable {
//                                           indexSelect =
//                                               index   // 👈 AQUÍ TIENES EL ÍNDICE
//                                           mostrarDialogozoom = true
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }

                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    texto_generico_multilinea(
                                        datos.datos_de_notificacion.titulo_notificacion,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.weight(1f)
                                    )

                                }
                                spacer_vertical(10.dp)
                                texto_generico_multilinea(
                                    datos.datos_de_notificacion.texto_notificacion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    texto_generico_one_line(
                                        "Datos de la notificacion",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Prioridad",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.parametros_notificacion.prioridad_notificacion.capitalizeFirst(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Formato",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.parametros_notificacion.tipo_notificacion.capitalizeFirst(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Tipo",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.parametros_notificacion.tipo_precio.capitalizeFirst(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }


                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Enviado a",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            "Seguidores",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }



                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "id de notificacion",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        TextoCopiable(datos.parametros_notificacion.id_noti)
                                    }

                                    if (datos.parametros_notificacion.tipo_precio != "informativas") {
                                        Column(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(
                                                    start = 10.dp,
                                                    end = 10.dp,
                                                    bottom = 10.dp
                                                ), verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(R.drawable.whatsapp_icon),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                spacer_horizonta(5.dp)
                                                texto_generico_one_line(
                                                    "Contacto directo",
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Switch(
                                                    modifier = Modifier.scale(0.8f),
                                                    checked = true,
                                                    onCheckedChange = {
                                                    }, colors = SwitchDefaults.colors(
                                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.5f
                                                        ),
                                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(
                                                            alpha = 0.3f
                                                        )
                                                    )
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                texto_generico_one_line(
                                                    "Numero de contacto",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                texto_generico_one_line(
                                                    "+51 ${datos.datos_de_notificacion.numero_contacto}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            spacer_vertical(5.dp)
                                            texto_generico_one_line(
                                                "Mensaje de envio",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            texto_generico_multilinea(
                                                datos.parametros_notificacion.mensaje_predeterminado,
                                                style = MaterialTheme.typography.bodySmall
                                            )


                                        }
                                    }

                                }
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    texto_generico_one_line(
                                        "Horas y fechas",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        texto_generico_one_line(
                                            "Estado",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            "Enviado",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Enviado el ",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.fecha_enviada,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(6.dp)
                                        Image(
                                            painter = painterResource(R.drawable.por_dias_icon_3d),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
//                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                        texto_generico_one_line(
//                                            "Iniciado",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                        Spacer(modifier = Modifier.weight(1f))
//                                        texto_generico_one_line(
//                                            datos.fecha_iniciada.toString(),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Enviados",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.parametros_notificacion.enviados.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Fallidos",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.parametros_notificacion.fallidos.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
//                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                        texto_generico_one_line(
//                                            "Tiempo transcurrido",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                        Spacer(modifier = Modifier.weight(1f))
//                                        texto_generico_one_line(
//                                            datos.tiempo_transcurrido.toString(),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Inversion",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.parametros_notificacion.total_gastado,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(6.dp)
                                        Image(
                                            painter = painterResource(R.drawable.icon_monedas_3d),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                }
                            }

                            item {
                                // 🔹 Bloque de cuadros estadísticos

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Métricas de audiencia",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    if (datos.parametros_notificacion.tipo_precio != "informativas") {



                                        // 🔹 Fila superior
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {

                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "click") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo = "click"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.click?.copy(tipo = "click")
//                                                    }
                                                    },
                                                titulo = "Enviados",
                                                valor = "1k",
                                                subtitulo = "Notificaciones entregadas",
                                                icono = R.drawable.icon_enviados_blanco,
                                                color = if (estadisticaSeleccionadaTipo == "click")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                            // Clicks
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "click") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo = "click"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.click?.copy(tipo = "click")
//                                                    }
                                                    },
                                                titulo = "Abiertos",
                                                valor = "1k",
                                                subtitulo = "Usuarios que abrieron",
                                                icono = R.drawable.clikc_drawable,
                                                color = if (estadisticaSeleccionadaTipo == "click")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                            // Vistas

                                        }

                                        // 🔹 Fila inferior
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Compartidos
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "compartidos") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo =
//                                                            "compartidos"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.compartidos?.copy(tipo = "compartidos")
//                                                    }
                                                    },
                                                titulo = "Atención prolongada",
                                                valor = "1k",
                                                subtitulo = "Tiempo de apertura elevado",
                                                icono = R.drawable.icon_reloj_blanco,
                                                color = if (estadisticaSeleccionadaTipo == "compartidos")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                            // WhatsApp
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "whatsapp") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo = "whatsapp"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.whatsapp?.copy(tipo = "whatsapp")
//                                                    }
                                                    },
                                                titulo = "Vistas 6s+",
                                                valor = "1k",
                                                subtitulo = "Atención mayor a 6 segundos",
                                                icono = R.drawable.visibility_preview,
                                                color = if (estadisticaSeleccionadaTipo == "whatsapp")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )


                                        }


                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Compartidos
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "compartidos") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo =
//                                                            "compartidos"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.compartidos?.copy(tipo = "compartidos")
//                                                    }
                                                    },
                                                titulo = "Ver perfil",
                                                valor = "1k",
                                                subtitulo = "Visitas al perfil",
                                                icono = R.drawable.persona_drawable,
                                                color = if (estadisticaSeleccionadaTipo == "compartidos")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                            // WhatsApp
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "whatsapp") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo = "whatsapp"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.whatsapp?.copy(tipo = "whatsapp")
//                                                    }
                                                    },
                                                titulo = "Whatsapp",
                                                valor = "1k",
                                                subtitulo = "Conversaciones iniciadas",
                                                icono = R.drawable.icono_whatsapp_blanco_tasns,
                                                color = if (estadisticaSeleccionadaTipo == "whatsapp")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                        }
                                    } else {
                                        EstadisticasTikTokCuadro(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "click") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo = "click"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.click?.copy(tipo = "click")
//                                                    }
                                                },
                                            titulo = "Enviados",
                                            valor = "1k",
                                            subtitulo = "Notificaciones entregadas",
                                            icono = R.drawable.icon_enviados_blanco,
                                            color = if (estadisticaSeleccionadaTipo == "click")
                                                MaterialTheme.colorScheme.surfaceVariant
                                            else Color(0xFF1C1C1E)
                                        )

                                        EstadisticasTikTokCuadro(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
//                                                    if (estadisticaSeleccionadaTipo == "click") {
//                                                        estadisticaSeleccionadaTipo = null
//                                                        estadisticaSeleccionada = null
//                                                    } else {
//                                                        estadisticaSeleccionadaTipo = "click"
//                                                        estadisticaSeleccionada =
//                                                            estadisticas?.click?.copy(tipo = "click")
//                                                    }
                                                },
                                            titulo = "Abiertos",
                                            valor = "1k",
                                            subtitulo = "Usuarios que abrieron",
                                            icono = R.drawable.clikc_drawable,
                                            color = if (estadisticaSeleccionadaTipo == "click")
                                                MaterialTheme.colorScheme.surfaceVariant
                                            else Color(0xFF1C1C1E)
                                        )
                                    }
                                }


                            }


                            item {

                                estadisticaSeleccionada?.let { estadistica ->
                                    key(estadistica.tipo) {

                                        val buyPersonData = remember(estadistica) {
                                            generarBuyerPersona(estadistica)
                                        }

                                        BuyerPersonaCard(buyPersonData)

                                        spacer_vertical(10.dp)

                                        GraficosPromosMPAndroidChart(estadistica)
                                    }
                                }


                            }

                        }
                        if (mostrarDialogozoom) {
                            ZoomableGalleryFullScreen(
                                "",
                                compartir_promocion(),
                                imagenes = listaImgContainer,
                                startIndex = indexSelect,
                                onDismiss = { mostrarDialogozoom = false }
                            )
                        }
                    }
                }

                else -> {}
            }

        }
    }
}
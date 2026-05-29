package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.R.attr.description
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_recientes
import com.github.mikephil.charting.charts.BarChart

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.github.dautovicharis.charts.style.StackedBarChartDefaults


import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil3.Image
import com.geinzz.geinzwork.data.model.EstadisticaAccion
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ui.BotonCompartirReddit
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.BuyerPersonaCard
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.GraficosPromosMPAndroidChart
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.generarBuyerPersona
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_promos_cercanas

import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_datos_promos_noti(
    viewmodel_pantalla: viewmodel_pantallas_recientes,
    id_tienda: String,
    localida: String,
    id_promo: String,
    ondimis: () -> Unit
) {
    var estadisticaSeleccionadaTipo by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val estado_datos by viewmodel_pantalla.estadoPromocion.collectAsState()
    var estadisticaSeleccionada by remember {
        mutableStateOf<EstadisticaAccion?>(null)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(id_tienda, localida, id_promo) {
        viewmodel_pantalla.cargarDatosPromocion(id_tienda, localida, id_promo)
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
                is viewmodel_pantallas_recientes.EstadoDatosPromocion.Error -> {
                    Text((estado_datos as viewmodel_pantallas_recientes.EstadoDatosPromocion.Error).mensaje)
                }

                viewmodel_pantallas_recientes.EstadoDatosPromocion.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ShimmerImagenConMarca()
                    }

                }

                is viewmodel_pantallas_recientes.EstadoDatosPromocion.Success -> {
                    val datos =
                        (estado_datos as viewmodel_pantallas_recientes.EstadoDatosPromocion.Success).datos
                    val estadisticas = datos.estadisticas
                    val metodosPagoIcons = listOfNotNull(
                        if (datos.metodos_pagos.yape) R.drawable.yape_logo else null,
                        if (datos.metodos_pagos.plin) R.drawable.logo_plin else null,
                        if (datos.metodos_pagos.agora) R.drawable.logo_agora else null,
                        if (datos.metodos_pagos.efectivo) R.drawable.efectivo_logo else null,
                        if (datos.metodos_pagos.visa) R.drawable.visa_logo else null,
                        if (datos.metodos_pagos.mastercard) R.drawable.master_car_logo else null
                    )

                    val comodidadesIcons = listOfNotNull(
                        if (datos.servicios_comoidades.zonaExpandida) R.drawable.icon_zona_expandida else null,
                        if (datos.servicios_comoidades.wifi) R.drawable.icon_wifi else null,
                        if (datos.servicios_comoidades.serviciosHigienicos) R.drawable.icon_servicios_higenicos else null,
                        if (datos.servicios_comoidades.camarasSeguridad) R.drawable.icon_seguridad else null,
                        if (datos.servicios_comoidades.salaEspera) R.drawable.icon_sala_de_espera else null,
                        if (datos.servicios_comoidades.salaJuegos) R.drawable.icon_sala_para_ninos else null,
                        if (datos.servicios_comoidades.mesaParaNinos) R.drawable.icon_mesa_para_ninos else null,
                        if (datos.servicios_comoidades.ingresoConMascotas) R.drawable.icon_ingreso_animales else null,
                        if (datos.servicios_comoidades.estacionamiento) R.drawable.icon_estacionamiento else null,
                        if (datos.servicios_comoidades.enchufe) R.drawable.icon_enchufa else null,
                        if (datos.servicios_comoidades.aireAcondicionado) R.drawable.icon_aire_acondicionado else null
                    )


                    val totalClick = estadisticas?.click?.total ?: 0
                    val totalVistas = estadisticas?.vistas?.total ?: 0
                    val totalCompartidos = estadisticas?.compartidos?.total ?: 0
                    val totalWhatsapp = estadisticas?.whatsapp?.total ?: 0
                    val color_estado = when (datos.estado) {
                        "activo" -> Color.Green
                        "pausado" -> Color.Yellow
                        "expirada" -> Color.Red
                        else -> {
                            Color.Red
                        }
                    }
                    val icono_horas_dias = when (datos.horas_o_fecha) {
                        "dias" -> R.drawable.por_dias_icon_3d
                        "horas" -> R.drawable.reloj_icon_hora_3d
                        else -> {
                            R.drawable.logo_geinz_500x500
                        }
                    }
                    listaImgContainer = datos.lista_img
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
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    itemsIndexed(
                                        items = datos.lista_img,
                                        key = { index, item -> "$item-$index" }
                                    ) { index, img ->

                                        Box(
                                            modifier = Modifier
                                                .height(330.dp)
                                                .width(300.dp)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(img)
                                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                                    .diskCachePolicy(CachePolicy.ENABLED)
                                                    .placeholder(R.drawable.cargando_img_categorias)
                                                    .error(R.drawable.cargando_img_categorias)
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .height(350.dp)
                                                    .width(300.dp)
                                                    .clip(RoundedCornerShape(5))
                                                    .clickable {
                                                        indexSelect =
                                                            index   // 👈 AQUÍ TIENES EL ÍNDICE
                                                        mostrarDialogozoom = true
                                                    },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }

                            }

                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    texto_generico_multilinea(
                                        datos.titulo,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    BotonCompartirReddit(
                                        icon = R.drawable.comparir_icon,
                                        descripcion = "compartidos",
                                        contador = "compartir",
                                        onClick = {
                                            compartir_hosting_promo(
                                                datos.mensaje_predeterminado.compartir.msje_predermindo,
                                                context = context,
                                                localida,
                                                id_promo
                                            )
                                        })
                                }
                                spacer_vertical(10.dp)
                                texto_generico_multilinea(
                                    datos.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    texto_generico_one_line(
                                        "Datos de la publicacion",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Categoria",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.categoira,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }


                                    if (datos.horaio_publicacion.isNotEmpty()) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            texto_generico_one_line(
                                                "Horario de publicacion",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            texto_generico_one_line(
                                                datos.horaio_publicacion,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                    if (datos.precio_publicacion.isNotEmpty()) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            texto_generico_one_line(
                                                "Precio de la publicacion",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            texto_generico_one_line(
                                                " S/${datos.precio_publicacion}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    if (datos.precio_publicacion.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            texto_generico_one_line(
                                                "Rango de precio",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            texto_generico_one_line(
                                                datos.rango_publicacion,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }



                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Pagos",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        metodosPagoIcons.forEach { icon ->
                                            Image(
                                                painter = painterResource(icon),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .clip(CircleShape)
                                                    .padding(start = 6.dp)
                                            )
                                        }
                                    }


                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Comodidades",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        comodidadesIcons.forEach { icon ->
                                            Image(
                                                painter = painterResource(icon),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .clip(CircleShape)
                                                    .padding(start = 6.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Publicado para ",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            "Todos",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "id promocion",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        TextoCopiable(datos.id_promocion)
                                    }

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
                                                checked = datos.compartir,
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
                                        if (datos.compartir) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                texto_generico_one_line(
                                                    "Numero de contacto",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                texto_generico_one_line(
                                                    "+51 ${datos.numero}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            spacer_vertical(5.dp)
                                            texto_generico_one_line(
                                                "Mensaje de envio",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            texto_generico_multilinea(
                                                datos.mensaje_predeterminado.compartir.msje_predermindo,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                    }


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
                                                painter = painterResource(R.drawable.comparir_icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            spacer_horizonta(5.dp)
                                            texto_generico_one_line(
                                                "Compartir",

                                                )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Switch(
                                                modifier = Modifier.scale(0.8f),
                                                checked = datos.contactar,
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
                                        if (datos.contactar) {
                                            texto_generico_one_line(
                                                "Mensaje de envio",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            texto_generico_multilinea(
                                                datos.mensaje_predeterminado.whatsapp.msje_predermindo,
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
                                            datos.estado.capitalizeFirst(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(6.dp)
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(color_estado)
                                                .size(15.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Seleccion por",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.horas_o_fecha.capitalizeFirst(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(6.dp)
                                        Image(
                                            painter = painterResource(icono_horas_dias),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Iniciado",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.fecha_iniciada.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Finaliza",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.fecha_terminada.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Duracion completa",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.duracion_total.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Tiempo transcurrido",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.tiempo_transcurrido.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        texto_generico_one_line(
                                            "Inversion",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        texto_generico_one_line(
                                            datos.costo_total.toInt().toString(),
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
                            if (
                                totalClick != 0 ||
                                totalVistas != 0 ||
                                totalCompartidos != 0 ||
                                totalWhatsapp != 0
                            ) {

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

                                        // 🔹 Fila superior
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Clicks
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        if (estadisticaSeleccionadaTipo == "click") {
                                                            estadisticaSeleccionadaTipo = null
                                                            estadisticaSeleccionada = null
                                                        } else {
                                                            estadisticaSeleccionadaTipo = "click"
                                                            estadisticaSeleccionada =
                                                                estadisticas?.click?.copy(tipo = "click")
                                                        }
                                                    },
                                                titulo = "Clicks",
                                                valor = totalClick.toString(),
                                                subtitulo = "Usuarios que tocaron",
                                                icono = R.drawable.clikc_drawable,
                                                color = if (estadisticaSeleccionadaTipo == "click")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                            // Vistas
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        if (estadisticaSeleccionadaTipo == "vistas") {
                                                            estadisticaSeleccionadaTipo = null
                                                            estadisticaSeleccionada = null
                                                        } else {
                                                            estadisticaSeleccionadaTipo = "vistas"
                                                            estadisticaSeleccionada =
                                                                estadisticas?.vistas?.copy(tipo = "vistas")
                                                        }
                                                    },
                                                titulo = "Vistas",
                                                valor = totalVistas.toString(),
                                                subtitulo = "Alcance total",
                                                icono = R.drawable.visibility_preview,
                                                color = if (estadisticaSeleccionadaTipo == "vistas")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )
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
                                                        if (estadisticaSeleccionadaTipo == "compartidos") {
                                                            estadisticaSeleccionadaTipo = null
                                                            estadisticaSeleccionada = null
                                                        } else {
                                                            estadisticaSeleccionadaTipo =
                                                                "compartidos"
                                                            estadisticaSeleccionada =
                                                                estadisticas?.compartidos?.copy(tipo = "compartidos")
                                                        }
                                                    },
                                                titulo = "Compartidos",
                                                valor = totalCompartidos.toString(),
                                                subtitulo = "Veces compartido",
                                                icono = R.drawable.comparir_icon,
                                                color = if (estadisticaSeleccionadaTipo == "compartidos")
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else Color(0xFF1C1C1E)
                                            )

                                            // WhatsApp
                                            EstadisticasTikTokCuadro(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        if (estadisticaSeleccionadaTipo == "whatsapp") {
                                                            estadisticaSeleccionadaTipo = null
                                                            estadisticaSeleccionada = null
                                                        } else {
                                                            estadisticaSeleccionadaTipo = "whatsapp"
                                                            estadisticaSeleccionada =
                                                                estadisticas?.whatsapp?.copy(tipo = "whatsapp")
                                                        }
                                                    },
                                                titulo = "WhatsApp",
                                                valor = totalWhatsapp.toString(),
                                                subtitulo = "Contactos iniciados",
                                                icono = R.drawable.icono_whatsapp_blanco_tasns,
                                                color = if (estadisticaSeleccionadaTipo == "whatsapp")
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


@Composable
fun EstadisticasTikTokCuadro(
    color_estadisticas: Boolean = false,
    modifier: Modifier = Modifier,
    titulo: String = "Vistas",
    valor: String = "1.9K",
    subtitulo: String = "Interacciones",
    icono: Int = R.drawable.visibility_preview,
    color: Color = Color(0xFF1C1C1E)
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color) // 👈 color dinámico
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(icono),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (!color_estadisticas) Color.White else Color.Unspecified
                    ),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = titulo,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }

            Text(
                text = valor,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitulo,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EstadisticasTikTokCuadro_dentro(

    modifier: Modifier = Modifier,
    titulo: String = "Vistas",
    valor: String = "1.9K",
    subtitulo: String = "Interacciones",
    iconoAscendente: Boolean = true, // true = arriba, false = abajo
    color: Color = Color(0xFF1C1C1E)
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔹 Icono de tendencia tipo Material
                Icon(
                    imageVector = if (iconoAscendente) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (iconoAscendente) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = titulo,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }

            Text(
                text = valor,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitulo,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}


fun compartir_hosting_promo(
    msje: String,
    context: Context,
    localidad_tienda: String,
    idpromo: String,
) {
    Log.d("menjsame", "$msje")
    try {
        val localidad_pasada = when (localidad_tienda) {
            "barranca" -> "ba"
            "paramonga" -> "par"
            "pativilca" -> "pat"
            "supe" -> "su"
            "puerto supe" -> "pue"
            else -> localidad_tienda
        }
        val link =
            "https://geinzworkapp.web.app/api/share?" +
                    "t=prms" +
                    "&l=$localidad_pasada" +
                    "&pi=$idpromo"


        val texto = "$msje \n$link"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }


        context.startActivity(
            Intent.createChooser(intent, "Compartir con")
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        )

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir el lugar", Toast.LENGTH_SHORT).show()
    }
}




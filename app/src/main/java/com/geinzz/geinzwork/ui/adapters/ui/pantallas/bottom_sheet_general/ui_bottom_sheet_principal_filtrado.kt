package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme

import androidx.compose.material.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.abrirRutaEnGoogleMaps
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.verificarUbiActiva
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.amarillo30
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_tiendas_filtradas(
    viewModelFiltros: viewModel_filtado_tiendas,
    tiendas_filtradas: modelo_tienda,
    onClose: () -> Unit
) {
    var expandir_descripcion by rememberSaveable { mutableStateOf(false) }
    var expander_caracterisiticas by rememberSaveable { mutableStateOf(false) }
    var expander_horario by rememberSaveable { mutableStateOf(false) }
    val direccion = tiendas_filtradas.ubicacion["dirección"]?.toString() ?: ""
    val referencia = tiendas_filtradas.ubicacion["referencia"]?.toString() ?: ""
    val longitud = (tiendas_filtradas.ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
    val latitud = (tiendas_filtradas.ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0
    Surface {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            dragHandle = null
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.80f)
                    .padding(10.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.LightGray)
                        )
                    }
                }
                item {
                    cabezero_tiendas(
                        direccion,
                        referencia,
                        tiendas_filtradas.categoria_tienda,
                        tiendas_filtradas.nombre_tienda,
                        latitud,
                        longitud,tiendas_filtradas.img_perfil
                    )
                    spacer_vertical(20.dp)
                }
                item {
                    text_expandible_wrapp(
                        "Acerca de la tienda",
                        MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Acerca de la tienda",
                        modifier = Modifier,
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Normal
                    )
                    spacer_vertical(10.dp)
                }
                item {
                    Expandible_descripcion_tienda(
                        tiendas_filtradas.descripcion,
                        expander_caracterisiticas
                    ) { expander_caracterisiticas = !expander_caracterisiticas }
                    spacer_vertical(10.dp)
                }
                item {


                    if (direccion.isNotBlank() || referencia.isNotBlank()) {
                        val fisica_virtual =
                            if (tiendas_filtradas.modelo_negocio) "Fisica" else "Virtual"

                        Column(modifier = Modifier.animateContentSize()) {
                            Expandible_direccion_ref(
                                direccion,
                                referencia,
                                fisica_virtual,
                                expandir_descripcion
                            ) {
                                expandir_descripcion = !expandir_descripcion
                            }
                        }
                    }

                    spacer_vertical(10.dp)
                }
                item {
                    Expandible_horario_atencion(
                        tiendas_filtradas.localidad,
                        tiendas_filtradas.id_tienda,
                        expander_horario,
                        viewModelFiltros
                    ) { expander_horario = !expander_horario }
                    spacer_vertical(10.dp)
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_shet_patrocinadores(
    viewModelFiltros: viewModel_filtado_tiendas,
    categoritienda: String,
    localidad_tienda: String,
    tiendas_filtradas: tiendas_filtradas,
    onClose: () -> Unit
) {
    var expandir_descripcion by rememberSaveable { mutableStateOf(false) }
    var expander_caracterisiticas by rememberSaveable { mutableStateOf(false) }
    var expander_horario by rememberSaveable { mutableStateOf(false) }
    Surface {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            dragHandle = null, containerColor = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.80f)
                    .padding(10.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.LightGray)
                        )
                    }
                }
                item {
                    cabezero_tiendas(
                        tiendas_filtradas.direccion, tiendas_filtradas.referencia, categoritienda,
                        tiendas_filtradas.nombre_tienda, tiendas_filtradas.latitud,
                        tiendas_filtradas.longitud,tiendas_filtradas.img_tiendas
                    )
                    spacer_vertical(20.dp)
                }
                item {
                    text_expandible_wrapp(
                        "Acerca de la tienda",
                        MaterialTheme.typography.titleLarge
                    )
                    spacer_vertical(10.dp)
                }
                item {
                    Expandible_descripcion_tienda(
                        tiendas_filtradas.descripcion,
                        expander_caracterisiticas
                    ) { expander_caracterisiticas = !expander_caracterisiticas }
                    spacer_vertical(10.dp)
                }
                item {


                    if (tiendas_filtradas.direccion.isNotBlank() || tiendas_filtradas.referencia.isNotBlank()) {

                        Column(modifier = Modifier.animateContentSize()) {
                            Expandible_direccion_ref(
                                tiendas_filtradas.direccion,
                                tiendas_filtradas.referencia,
                                "Fisica",
                                expandir_descripcion
                            ) {
                                expandir_descripcion = !expandir_descripcion
                            }
                        }
                    }

                    spacer_vertical(10.dp)
                }
                item {
                    Expandible_horario_atencion(
                        localidad_tienda,
                        tiendas_filtradas.id_tienda,
                        expander_horario,
                        viewModelFiltros
                    ) { expander_horario = !expander_horario }
                    spacer_vertical(10.dp)
                }

            }
        }
    }
}

@Composable
fun cabezero_tiendas(
    direccion: String,
    referencia: String,
    categoritienda: String,
    nombre_tienda: String, latitud: Double, longitud: Double, img_tienda_perfil: String
) {
    val context = LocalContext.current
    val mostrarDialogo = remember { mutableStateOf(false) }
    val mostrarDialog_sin_google_maps = remember { mutableStateOf(false) }
    if (mostrarDialogo.value) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                mostrarDialogo.value = false
            },
            abrir_configuracion = {
                mostrarDialogo.value = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                mostrarDialogo.value = false
                mostrarDialog_sin_google_maps.value = true
            }
        )
    }
    if (mostrarDialog_sin_google_maps.value) {
        dialog_sin_ubi_activa(
            direccion, referencia, onDismis = { mostrarDialog_sin_google_maps.value = false },
            abrir_maps = { constantes.abrirGoogleMaps(context, direccion) })

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = img_tienda_perfil,
            contentDescription = "Imagen de la tienda",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.qr_geinz_sin_fondo),
            error = painterResource(id = R.drawable.qr_yape),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Column(modifier = Modifier.weight(1f)) {
                val iconId = "icon"
                val annotatedText = buildAnnotatedString {
                    append(nombre_tienda)
                    append(" ")
                    appendInlineContent(iconId, "[icon]")
                }

                val inlineContent = mapOf(
                    iconId to InlineTextContent(
                        Placeholder(
                            width = 20.sp,
                            height = 20.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_tienda_icon_general),
                            contentDescription = "Icono tienda",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )

                Text(
                    text = annotatedText,
                    inlineContent = inlineContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                text_expandible_wrapp(
                    "Categoria : $categoritienda",
                    MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
            spacer_horizonta(10.dp)
            FloatingActionButton(
                onClick = {
                    abrir_google_maps(context, latitud, longitud) { dialogo ->
                        mostrarDialogo.value = dialogo
                        if (mostrarDialogo.value) {
                            abrir_google_maps(context, latitud, longitud) { dialogo ->
                                mostrarDialogo.value = dialogo
                            }
                        }
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Image(
                    painter = painterResource(R.drawable.localidad_icon_general),
                    contentDescription = "Localidad",
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}

fun abrir_google_maps(
    context: android.content.Context,
    latitud: Double,
    longitud: Double,
    mostrar_dialog: (Boolean) -> Unit
) {
    if (verificarUbiActiva(context)) {
        abrirRutaEnGoogleMaps(context, latitud, longitud)
    } else {
        mostrar_dialog(true)
    }
}


@Composable
fun tienda_cercana() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(40f))
            .background(amarillo30)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text("Cerca de ti")
        Spacer(modifier = Modifier.width(5.dp))
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.localidad_icon_general),
            contentDescription = ""
        )
    }
}

@Composable
fun Expandible_descripcion_tienda(
    descipcion_tienda: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column() {
            expandibles_wrapp(
                "Descripcion de tienda",
                R.drawable.descripcion_tienda_vector,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(10.dp)
                ) {
                    texto_expandido_wrapp_sin_max_line(descipcion_tienda)
                }
            }
        }
    }

}

@Composable
fun Expandible_direccion_ref(
    direccion: String,
    referencia: String,
    fisica_virtual: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            expandibles_wrapp(
                "Dirección y referencia",
                R.drawable.location_drawable,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(
                visible = expandido
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                ) {
                    text_expandible_wrapp("Dirección : $direccion")
                    spacer_vertical(10.dp)
                    text_expandible_wrapp("Referencia : $referencia")
                    spacer_vertical(10.dp)
                    text_expandible_wrapp("Tipo de tienda : $fisica_virtual")
                    spacer_vertical(10.dp)
                }
            }
        }
    }
}

@Composable
fun text_expandible_wrapp(
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxlines: Int = 1
) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        style = style,
        maxLines = maxlines,
        overflow = TextOverflow.Ellipsis

    )
}

@Composable
fun texto_expandido_wrapp_sin_max_line(
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        style = style,
        overflow = TextOverflow.Ellipsis

    )
}


@Composable
fun Expandible_horario_atencion(
    localidad_tienda: String?,
    id_tienda: String,
    expandido: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    onClickExpand: () -> Unit
) {
    val horario_tienda = viewModelFiltros._horario_tienda.observeAsState(emptyList())
    var cargado by remember { mutableStateOf(false) }

    LaunchedEffect(expandido) {
        if (expandido && !cargado) {
            viewModelFiltros.obtener_horario_por_tienda(localidad_tienda!!, id_tienda)
            cargado = true
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column() {
            expandibles_wrapp(
                "Horario de atención",
                R.drawable.horario_tienda_vector,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {

                    horario_tienda.value.forEach { i ->
                        val esDiaActual = obtenerDiaActualEnEspañol() == i.dia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val horario_abierto_cerrado =
                                if (i.h_apertura.isNotEmpty() && i.h_cierre.isNotEmpty()) "${i.h_apertura} am a ${i.h_cierre} pm " else "Cerrando"
                            Text(
                                text = "${i.dia.uppercase()} : $horario_abierto_cerrado",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (esDiaActual) {
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.guardados_icon),
                                    contentDescription = ""
                                )
                            }

                            spacer_horizonta(15.dp)
                        }
                    }


                }
            }
        }

    }

}

@Composable
fun expandibles_wrapp(
    texto_params: String,
    icon: Int,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp)
    ) {
        val (texto, btn) = createRefs()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto_params,
                fontSize = 15.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
        }

        val icono_exandido = if (expandido) {
            R.drawable.ocultar_abajo
        } else {
            R.drawable.ocultar_arriva
        }
        FloatingActionButton(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .constrainAs(btn) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            onClick = { onClickExpand() },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            containerColor = MaterialTheme.colorScheme.primary,
        )
        {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(icono_exandido),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }

}

fun obtenerDiaActualEnEspañol(): String {
    val locale = Locale("es", "ES")
    val calendar = Calendar.getInstance()
    val diaSemana = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)
    return diaSemana?.lowercase() ?: ""
}

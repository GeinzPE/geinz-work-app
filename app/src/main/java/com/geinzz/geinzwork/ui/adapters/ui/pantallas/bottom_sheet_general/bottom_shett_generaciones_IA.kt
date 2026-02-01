package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.IconoIA
import com.geinzz.geinzwork.data.model.datos_para_generacion_dialog_historial_IA

import com.geinzz.geinzwork.data.model.lista_genereracione
import com.geinzz.geinzwork.data.model.obt_item_gen_IA
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.timestampAFechaLegible
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dailog_generaciones_IA_versiones
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.parseDiasHorasRestantes
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.FondoIAAnimado
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_generaciones_IA
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar
import com.geinzz.geinzwork.viewModels.viewmodel_recargas
import com.google.firebase.Timestamp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ui_bottom_sheet_generaciones_IA(
    i:datos_para_generacion_dialog_historial_IA,
    id_tienda: String,
    localidad: String,
    ondismis: () -> Unit,
    nombre_tienda: String,
    usar_todas: (String, String, String, String, String) -> Unit,
    usar_titulo_descripcion: (String, String, String) -> Unit,
    usar_wsap: (String, String) -> Unit,
    usar_compartir: (String, String) -> Unit

) {
    val viewmodelGeneracionesIa: viewmodel_generaciones_IA = viewModel()
    val viewmodel_pantallas_promocionar:viewmodel_pantallas_promocionar=viewModel()
    val estaod_generaciones_IA by viewmodelGeneracionesIa.estado_generaciones_IA.collectAsState()
    var mostar_dialog_mejorar_versiones by remember { mutableStateOf(false) }
    var titulo_dialog_mejorar_version by remember { mutableStateOf("") }
    var texto_dialog_mejorar_version by remember { mutableStateOf("") }
    var tipo_seleccionado_promo_noti by remember { mutableStateOf("") }
    var subCategoriaSeleccionada by remember { mutableStateOf("Todos") }

    val lsita_fitlrado_opciones = listOf(
        "Todos",
        "Generaciones de promociones",
        "Generaciones de notificaciones",
        "Por vencer",
        )



    LaunchedEffect(id_tienda, localidad) {
        viewmodelGeneracionesIa.obtner_generaciones_IA(localidad, id_tienda)
    }
    ModalBottomSheet(
        onDismissRequest = { ondismis() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    )
    {
        FuenteControladaApp {
            BoxWithConstraints {
                val maxHeightSheet_empty = maxHeight * 0.4f
                when (estaod_generaciones_IA) {
                    viewmodel_generaciones_IA.EstadoGeneracionesIA.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxHeight * 0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            ShimmerImagenConMarca()
                        }
                    }

                    is viewmodel_generaciones_IA.EstadoGeneracionesIA.Success -> {
                        val lista =
                            (estaod_generaciones_IA as viewmodel_generaciones_IA.EstadoGeneracionesIA.Success).data

                        val lista_filtrada = when (subCategoriaSeleccionada) {
                            "Todos" -> {
                                lista
                            }

                            "Generaciones de promociones" -> {
                                lista.filter { it.tipo_realizado == "publicacion" }
                            }

                            "Generaciones de notificaciones" -> {
                                lista.filter { it.tipo_realizado == "notificacion" }
                            }

                            "Por vencer" -> {
                                val diasLimite = 1

                                lista.filter { item ->
                                    val tiempo = item.fin?.let {
                                        constantes_datos_expirados_fechas_publicaciones.tiempoRestante(
                                            it
                                        )
                                    } ?: return@filter false

                                    when {
                                        tiempo == "Expirado" -> false

                                        // 🔥 entra si son minutos
                                        tiempo.contains("mto", ignoreCase = true) ||
                                                tiempo.contains("min", ignoreCase = true) -> true

                                        // 🔥 entra si son horas
                                        tiempo.contains("hora", ignoreCase = true) -> true

                                        // 🔥 entra si son días <= limite
                                        tiempo.contains("día", ignoreCase = true) -> {
                                            val dias = tiempo.filter { it.isDigit() }
                                                .toIntOrNull() ?: return@filter false
                                            dias in 0..diasLimite
                                        }

                                        else -> false
                                    }
                                }
                            }


                            else -> {
                                lista
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = rememberLazyListState(), // 👈 importante
                                verticalArrangement = Arrangement.spacedBy(15.dp),
                                modifier = Modifier.fillMaxSize(), // 👈 ESTO ES LA SOLUCIÓN
                                contentPadding = PaddingValues(10.dp) // 👈 padding correcto
                            ) {
                                item {
                                    Text(
                                        fontFamily = baners_geinz_work,
                                        text = "Historial de IA",
                                        color = Color.White,
                                        fontSize = 25.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    spacer_vertical(5.dp)

                                    texto_generico_multilinea(
                                        "Hola $nombre_tienda, en este apartado podrás ver el historial de generaciones de IA que has realizado para impulsar el crecimiento de tu negocio. Recuerda que cada generación tiene una vigencia de 30 días desde su creación.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                item {
                                    spacer_vertical(7.dp)
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
                                    spacer_vertical(7.dp)
                                }

                                if (lista_filtrada.isEmpty()) {
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
                                        items = lista_filtrada,
                                        key = { it.id_promo_noti_cread }) { i ->
                                        val datos = obt_item_gen_IA(
                                            img_ = i.img_container,
                                            titulo_gen_IA = i.nombre_generacion,
                                            vencimiento = i.fin,
                                            inicio = i.inicio,
                                            tipo = i.tipo_realizado,
                                            generacion_wsap = i.datos_generaciones.generacion_wsap,
                                            generacion_compartida = i.datos_generaciones.generacion_compartir,
                                            generacion_origini = lista_genereracione(
                                                titulo = i.datos_generaciones.titulo_original,
                                                descripcion = i.datos_generaciones.descripcion_original,
                                                tipo = i.datos_generaciones.tipo_generacion_IA,
                                            ),
                                            lista_generaciones = i.datos_generaciones.generaciones
                                        )
                                        Box(
                                            modifier = Modifier.animateItem(
                                                placementSpec = tween(
                                                    durationMillis = 350,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        ) {
                                            item_generaciones_con_IA(
                                                i.datos_generaciones.titulo_seleccionado_gen_IA,
                                                i.datos_generaciones.descripcion_seleccionada_ge_IA,
                                                i = datos,
                                                usar_todas = { titulo, descripcion, whatsapp, compartir, tipo ->
                                                    usar_todas(
                                                        titulo,
                                                        descripcion,
                                                        whatsapp,
                                                        compartir,
                                                        tipo
                                                    )
                                                },
                                                usar_titulo_descripcion = { titulo, descripcion, tipo ->
                                                    usar_titulo_descripcion(titulo, descripcion, tipo)
                                                },
                                                whatsapp = { mejse, tipo ->
                                                    usar_wsap(mejse, tipo)
                                                },
                                                compartir = { mejse, tipo ->
                                                    usar_compartir(mejse, tipo)
                                                }, { titulo, texto,tipo ->
                                                    mostar_dialog_mejorar_versiones = true
                                                    titulo_dialog_mejorar_version = titulo
                                                    texto_dialog_mejorar_version = texto
                                                    tipo_seleccionado_promo_noti=tipo
                                                }
                                            )
                                        }


                                        spacer_vertical(10.dp)
                                    }

                                }

                            }


                            if (mostar_dialog_mejorar_versiones) {
                                dailog_generaciones_IA_versiones(
                                    i,
                                    viewmodel_pantallas_promocionar,
                                    titulo_dialog_mejorar_version,
                                    texto_dialog_mejorar_version,tipo_seleccionado_promo_noti,
                                    { mostar_dialog_mejorar_versiones = false })
                            }
                        }


                    }

                    is viewmodel_generaciones_IA.EstadoGeneracionesIA.Empty -> {}
                    is viewmodel_generaciones_IA.EstadoGeneracionesIA.Error -> {
                        Text((estaod_generaciones_IA as viewmodel_generaciones_IA.EstadoGeneracionesIA.Error).message)
                    }

                    else -> {}
                }
            }

        }
    }
}


@Composable
fun item_generaciones_con_IA(
    tituo_seleccionado: String, descrpcion_seleccionada: String,
    i: obt_item_gen_IA, usar_todas: (String, String, String, String, String) -> Unit,
    usar_titulo_descripcion: (String, String, String) -> Unit,
    whatsapp: (String, String) -> Unit,
    compartir: (String, String) -> Unit,
    mostrar_dialog_mejorar_vesiones: (String, String,String) -> Unit
) {
    val contex = LocalContext.current
    var tituloSeleccionado by remember { mutableStateOf(tituo_seleccionado) }
    var descripcionSeleccionada by remember { mutableStateOf(descrpcion_seleccionada) }


    var mostar_apartado_completo by remember { mutableStateOf(false) }
    val listaConOriginal = remember(i) {
        listOf(i.generacion_origini) + i.lista_generaciones
    }
    val tiempo = i.vencimiento?.let {
        constantes_datos_expirados_fechas_publicaciones.tiempoRestante(
            it
        )
    } ?: "Expirado"

    var diasRestantes by remember(i.vencimiento) {
        mutableStateOf(
            constantes_datos_expirados_fechas_publicaciones
                .tiempoRestante(i.vencimiento)
        )
    }
    val (valorRestante, tipo) = parseDiasHorasRestantes(tiempo)
    val backgroundColor = when {
        tipo == "dias" -> when {
            valorRestante > 5 -> Color(0xFF15BB1A) // Verde
            valorRestante in 2..5 -> Color(0xFFFF9900) // Naranja
            valorRestante == 1 -> Color(0xFFEC1707) // Rojo
            else -> Color.Gray
        }

        tipo == "horas" -> when {
            valorRestante > 12 -> Color(0xFF15BB1A)
            valorRestante in 6..12 -> Color(0xFFFF9900)
            valorRestante in 1..5 -> Color(0xFFEC1707)
            else -> Color.Gray
        }

        else -> Color.Gray
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier
                .padding(end = 10.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    mostar_apartado_completo = !mostar_apartado_completo
                }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(i.img_)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(100.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(5))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                    },
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                val icono_promo_noti = if (i.tipo.equals("notificacion")) {
                    R.drawable.campana_3d_webp
                } else {
                    R.drawable.promocio_iconn
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Mejorar con IA",
                        tint = Color.White, modifier = Modifier.size(20.dp)
                    )
                    texto_generico_one_line(
                        i.titulo_gen_IA.capitalizeFirst(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "${diasRestantes}",
                    fontSize = 12.sp,
                    color = backgroundColor
                )
                texto_generico_one_line(
                    "Realizado: ${timestampAFechaLegible(i.inicio)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    texto_generico_one_line(
                        i.tipo.capitalizeFirst(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Image(
                        painter = painterResource(icono_promo_noti),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        AnimatedVisibility(mostar_apartado_completo) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                spacer_vertical(5.dp)
                texto_generico_one_line("Generacion de titulo y descripcion")

                if (i.tipo.equals("publicacion")) {

                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(listaConOriginal) { item ->
                            val estaSeleccionado =
                                item.titulo == tituloSeleccionado &&
                                        item.descripcion == descripcionSeleccionada
                            generaciones_IA(
                                "generaciones",
                                titulo_select = tituloSeleccionado,
                                descripcion_selet = descripcionSeleccionada,
                                selecionado = estaSeleccionado,
                                width_auto = true,
                                titulo = item.titulo,
                                descripcion = item.descripcion,
                                tipo = item.tipo,
                                seleccionado = { titulo, descripcion ->
                                    tituloSeleccionado = titulo
                                    descripcionSeleccionada = descripcion
                                }, {}, { titulo, texto,tipo ->
                                    mostrar_dialog_mejorar_vesiones(titulo, texto,i.tipo)
                                }
                            )
                        }
                    }
                } else {

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        generaciones_IA(
                            "generaciones",
                            titulo_select = "",
                            descripcion_selet = "",
                            selecionado = false,
                            width_auto = true,
                            titulo = i.generacion_origini.titulo,
                            descripcion = i.generacion_origini.descripcion,
                            tipo = "ORIGINAL",
                            seleccionado = { titulo, descripcion ->
                            }, {}, { titulo, texto,tipo->
                                mostrar_dialog_mejorar_vesiones(titulo, texto,i.tipo)
                            }
                        )
                        generaciones_IA(
                            "generaciones",
                            titulo_select = tituloSeleccionado,
                            descripcion_selet = descripcionSeleccionada,
                            selecionado = true,
                            width_auto = true,
                            titulo = tituo_seleccionado,
                            descripcion = descripcionSeleccionada,
                            tipo = "GENERADO",
                            seleccionado = { titulo, descripcion ->

                            }, {

                            }, { titulo, texto,tipo->
                                mostrar_dialog_mejorar_vesiones(titulo, texto,i.tipo)
                            }
                        )
                    }
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Mejorar con IA",
                        tint = Color.Blue, modifier = Modifier.size(20.dp)
                    )
                    texto_generico_one_line("Nuevas generaciones")
                }





                if (i.generacion_wsap.isNotEmpty()) {
                    spacer_vertical(7.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejorar con IA",
                            tint = Color.Blue, modifier = Modifier.size(20.dp)
                        )
                        Image(
                            painter = painterResource(R.drawable.whatsapp_icon),
                            modifier = Modifier.size(20.dp),
                            contentDescription = ""
                        )
                        texto_generico_one_line("Generacion de contacto por whatsapp")
                    }
                    generaciones_IA(
                        "copiado",
                        tituloSeleccionado,
                        descripcionSeleccionada,
                        false,
                        false,
                        "",
                        i.generacion_wsap,
                        "",
                        { _, _ -> }, {
                            constantestextos_general.copiarTexto_portapapeles_compouse(
                                i.generacion_wsap, contex
                            )
                        }, { _, _ ,_-> })

                }


                if (i.generacion_compartida.isNotEmpty()) {
                    spacer_vertical(7.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejorar con IA",
                            tint = Color.Blue, modifier = Modifier.size(20.dp)
                        )
                        Image(
                            painter = painterResource(R.drawable.compartir_icon_rojo),
                            modifier = Modifier.size(20.dp),
                            contentDescription = ""
                        )
                        texto_generico_one_line("Generacion de compartidos")
                    }

                    generaciones_IA(
                        "copiado",
                        tituloSeleccionado,
                        descripcionSeleccionada,
                        false,
                        false,
                        "",
                        i.generacion_compartida,
                        "",
                        { _, _ -> }, {
                            constantestextos_general.copiarTexto_portapapeles_compouse(
                                i.generacion_compartida, contex
                            )
                        }, { _, _ ,_-> })
                }

                spacer_vertical(7.dp)
                TresBotonesPrimarios(
                    usar_todas_bool =
                        tituloSeleccionado.isNotEmpty() &&
                                descripcionSeleccionada.isNotEmpty() &&
                                i.generacion_wsap.isNotEmpty() &&
                                i.generacion_compartida.isNotEmpty(),

                    usarTodas = {
                        usar_todas(
                            tituloSeleccionado,
                            descripcionSeleccionada,
                            i.generacion_wsap,
                            i.generacion_compartida,
                            i.tipo
                        )
                    },

                    titulo_descripcion =
                        tituloSeleccionado.isNotEmpty() &&
                                descripcionSeleccionada.isNotEmpty(),

                    usarTituloDescripcion = {
                        usar_titulo_descripcion(
                            tituloSeleccionado,
                            descripcionSeleccionada, i.tipo
                        )
                    },

                    whattsap = i.generacion_wsap.isNotEmpty(),

                    usarWhatsapp = {
                        whatsapp(i.generacion_wsap, i.tipo)
                    },

                    compartir = i.generacion_compartida.isNotEmpty(),

                    usarCompartir = {
                        compartir(i.generacion_compartida, i.tipo)
                    }
                )


                spacer_vertical(5.dp)

            }


        }
    }
}

@Composable
fun generaciones_IA(
    tipo_clikeable: String,
    titulo_select: String, descripcion_selet: String,
    selecionado: Boolean,
    width_auto: Boolean,
    titulo: String,
    descripcion: String,
    tipo: String, seleccionado: (titulo: String, descripcion: String) -> Unit,
    copiar_texto: () -> Unit,
    mostrar_dialog_mejorar_generacion: (String, String,String) -> Unit
) {

    val titulo_finla = if (tipo.equals("notificacion")) titulo_select else titulo
    val texto_final = if (tipo.equals("notificacion")) descripcion_selet else descripcion


    Box(
        modifier = Modifier
            .then(
                if (width_auto) Modifier.width(200.dp)
                else Modifier.fillMaxWidth()
            )
            .clip(RoundedCornerShape(9.dp))
    ) {

        // 🌈 Fondo animado SOLO cuando está seleccionado
        if (selecionado) {
            FondoIAAnimado(
                modifier = Modifier.matchParentSize()
            )
        }

        val soloTitulo = descripcion.isNotEmpty() && tipo.isEmpty() && titulo.isEmpty()

        Column(
            modifier = Modifier
                // Fondo normal SOLO si no está seleccionado
                .then(
                    if (!soloTitulo && !selecionado)
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    else Modifier
                )

                .then(
                    if (!soloTitulo && !selecionado)
                        Modifier.clickable {
                            seleccionado(titulo, descripcion)
                        }
                    else Modifier
                )
                .then(
                    if (!soloTitulo)
                        Modifier.padding(horizontal = 5.dp, vertical = 10.dp)
                    else Modifier
                )
                .then(
                    if (!soloTitulo)
                        Modifier.height(110.dp)
                    else Modifier
                ),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (tipo.isNotEmpty()) {

                    texto_generico_one_line(
                        tipo.capitalizeFirst(),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!tipo.equals("publicacion")) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejorar con IA",
                            tint = Color.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(R.drawable.visibility_preview),
                        contentDescription = "ver",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                mostrar_dialog_mejorar_generacion(titulo_finla, texto_final,tipo)
                            },
                        colorFilter = ColorFilter.tint(Color.White)
                    )


                }
            }

            if (titulo_finla.isNotEmpty()) {
                texto_generico_one_line(
                    titulo_finla,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (texto_final.isNotEmpty()) {
                text_expandible_wrapp(
                    texto = texto_final,
                    maxlines = 3,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.then(
                        if (tipo_clikeable.equals("copiado"))
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                copiar_texto()
                            }
                        else Modifier
                    ),
                )
            }


        }
    }
}


@Composable
fun TresBotonesPrimarios(
    usar_todas_bool: Boolean,
    usarTodas: () -> Unit,
    titulo_descripcion: Boolean,
    usarTituloDescripcion: () -> Unit,
    whattsap: Boolean,
    usarWhatsapp: () -> Unit,
    compartir: Boolean,
    usarCompartir: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (usar_todas_bool) {

            BotonIA(
                "Usar todas",
                usarTodas,
                IconoIA.Vector(Icons.Default.SelectAll)
            )
        }

        if (titulo_descripcion) {

            BotonIA(
                "Usar título y descripción",
                usarTituloDescripcion,
                IconoIA.Vector(Icons.Default.AutoAwesome)
            )
        }
        if (whattsap) {

            BotonIA(
                "Usar generación de WhatsApp",
                usarWhatsapp,
                IconoIA.Drawable(R.drawable.whatsapp_icon)
            )
        }
        if (compartir) {
            BotonIA(
                "Usar generación de compartir",
                usarCompartir,
                IconoIA.Drawable(R.drawable.compartir_icon_rojo)
            )
        }

    }
}


@Composable
fun BotonIA(
    texto: String,
    onClick: () -> Unit,
    icono: IconoIA? = null
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            texto_generico_one_line(
                texto,
                style = MaterialTheme.typography.bodyMedium
            )

            when (icono) {
                is IconoIA.Drawable -> {
                    Image(
                        painter = painterResource(icono.resId),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                is IconoIA.Vector -> {
                    Icon(
                        imageVector = icono.imageVector,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                null -> Unit
            }
        }
    }
}


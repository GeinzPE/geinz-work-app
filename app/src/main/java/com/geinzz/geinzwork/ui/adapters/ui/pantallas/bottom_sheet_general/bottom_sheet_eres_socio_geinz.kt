package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.datos_grafico
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.Cartas_expandibles
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.expandibles_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.expandibles_wrapp_socio_geinzz
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_mostar_leyendas_graficos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.HorarioSemanal
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.HorarioSemanal123
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_datos
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.ktor.client.content.LocalFileContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun eres_socio_geinz(nombre_user: String, ondimis: () -> Unit) {

    val context = LocalContext.current
    var id_registrado by remember { mutableStateOf("") }
    val labels = listOf("Vistas", "Guardados", "Clics")
    val labels2 = listOf("Facebook", "Instagram", "TikTok", "sitio web")
    val labels3 = listOf("Llamada", "Whatsapp", "Rutas")
    val viewmodel: viewmodel_eres_socio = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val viewmodel_agregar_datos: viewmodel_agregar_datos = viewModel()
    val state_socio = viewmodel.state_eres_socio.collectAsState()
    val _tick by viewModelFiltros.tick.collectAsState()

    var mostar_interes by remember { mutableStateOf(false) }
    var mostrar_convesion by remember { mutableStateOf(false) }
    var mostrar_trafico_externo by remember { mutableStateOf(false) }
    val uid_respald_user by data_store_localidad
        .get_id_socio(context)
        .collectAsState(initial = "")

    var dialog_mostar_leyendas_graficos by remember { mutableStateOf(false) }
    var titulo_leyenda_dialog by remember { mutableStateOf("") }
    var txt_leyenda by remember { mutableStateOf("") }
    var icono_mostar_leyendas_graficos by remember { mutableStateOf(0) }
    var id_tienda by remember { mutableStateOf("") }
    var horarioMap by remember { mutableStateOf(HorarioAtencion_box()) }
    LaunchedEffect(id_tienda) {
        viewModelFiltros.calcularHorarioParaTienda(id_tienda, horarioMap)
    }
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        FuenteControladaApp {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 10.dp)
            ) {
                LazyColumn() {
                    item {

                        if (uid_respald_user.isEmpty()) {

                            Column(
                                Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                texto_generico_one_line(
                                    "¿Eres socio de Geinz?",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                spacer_vertical(10.dp)

                                texto_generico_multilinea(
                                    "Ingresa tu ID y descubre el impacto real de tu negocio. " +
                                            "Conoce cuántas personas visitaron tu perfil, cuántos lo guardaron como favorito " +
                                            "y actualiza tu horario en solo segundos.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(10.dp)

                                MyOutlinedTextField(
                                    value = id_registrado,
                                    onValueChange = { id_registrado = it },
                                    labelText = "Pega tu ID",
                                    placeholderText = "Pega tu ID"
                                )

                                spacer_vertical(10.dp)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            viewmodel.verificar_seccion(context, id_registrado)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Acceder",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 12.dp
                                        )
                                    )
                                }

                                // ------------------------------------------------------------------
                                //                  ESTADOS CUANDO EL USUARIO DA CLICK
                                // ------------------------------------------------------------------

                                spacer_vertical(20.dp)

                                when (val state = state_socio.value) {

                                    is viewmodel_eres_socio.carga_acces_socio.loading -> {
                                        texto_generico_one_line("Verificando ID…")
                                    }

                                    is viewmodel_eres_socio.carga_acces_socio.error -> {
                                        texto_generico_multilinea(
                                            "⚠️ ${state.txt}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    is viewmodel_eres_socio.carga_acces_socio.succes -> {
                                        texto_generico_one_line(
                                            "¡Acceso concedido!",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                        } else {


                            LaunchedEffect(uid_respald_user) {
                                viewmodel.verificar_seccion(context, uid_respald_user)
                            }

                            Box(
                                Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {

                                when (val state = state_socio.value) {

                                    is viewmodel_eres_socio.carga_acces_socio.loading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator()
                                                spacer_vertical(8.dp)
                                                texto_generico_one_line("Cargando, espere un momento...")
                                            }
                                        }
                                    }

                                    is viewmodel_eres_socio.carga_acces_socio.error -> {
                                        texto_generico_multilinea(
                                            "⚠️ ${state.txt}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    is viewmodel_eres_socio.carga_acces_socio.succes -> {
                                        val datos = state.datos
                                        id_tienda = datos.id_tienda
                                        horarioMap = datos.horario_tiendaMap
                                        val values by remember(datos.id_tienda) {
                                            mutableStateOf(
                                                listOf(
                                                    datos.total_vista.toFloat(),
                                                    datos.total_guardados.toFloat(),
                                                    datos.clic.toFloat()
                                                )
                                            )
                                        }
                                        val values2 by remember(datos.id_tienda) {
                                            mutableStateOf(
                                                listOf(
                                                    datos.fb.toFloat(),
                                                    datos.ig.toFloat(),
                                                    datos.tk.toFloat(),
                                                    datos.stweb.toFloat()
                                                )
                                            )
                                        }
                                        val values3 by remember(datos.id_tienda) {
                                            mutableStateOf(
                                                listOf(
                                                    datos.llamada.toFloat(),
                                                    datos.wsap.toFloat(),
                                                    datos.ruta.toFloat()
                                                )
                                            )
                                        }


                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(top = 20.dp)
                                        ) {
                                            texto_generico_one_line(
                                                "Bienvenido a GEINZ PANEL",
                                                style = MaterialTheme.typography.titleLarge
                                            )

                                            spacer_vertical(10.dp)

                                            texto_generico_multilinea(
                                                "Hola $nombre_user, aquí puedes ver la información principal de ${datos.nombre}.  Accede a las estadísticas de vistas, guardados y clics, y actualiza el horario de tu tienda de forma rápida y sencilla.",
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            spacer_vertical(10.dp)

                                            Column(
                                                Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(datos.img_tienda)
                                                        .placeholder(R.drawable.cargando_img_categorias)
                                                        .error(R.drawable.cargando_img_categorias)
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .height(200.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                texto_generico_one_line(datos.nombre.capitalizeFirst(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 5.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    texto_generico_one_line("Horario de hoy :", style = MaterialTheme.typography.bodyMedium,modifier = Modifier.padding(start = 5.dp,bottom = 10.dp))
                                                    retornar_color_estado_tienda_Box(
                                                        "",
                                                        viewModelFiltros.horariosTiendas.collectAsState().value[id_tienda]
                                                            ?: HorarioDia_box(),
                                                        _tick,
                                                        true,
                                                        { color, txt -> }
                                                    )
                                                }
                                                HorarioSemanal123(datos.horario_tiendaMap)
                                            }



                                            spacer_vertical(10.dp)

                                            Cartas_expandibles(
                                                modifier = Modifier.padding(
                                                    vertical = 10.dp
                                                )
                                            ) {
                                                Column() {
                                                    val lsita_datos1 = listOf(
                                                        datos_grafico(
                                                            R.drawable.vizualizacion_icon_3d,
                                                            "Vistas",
                                                            datos.total_vista.toString()
                                                        ),
                                                        datos_grafico(
                                                            R.drawable.corazon_gracias,
                                                            "Guardados",
                                                            datos.total_guardados.toString()
                                                        ),
                                                        datos_grafico(
                                                            R.drawable.click_icon3d,
                                                            "clics",
                                                            datos.clic.toString()
                                                        )
                                                    )
                                                    expandibles_wrapp_socio_geinzz(
                                                        lsita_datos1,
                                                        "El interés real muestra cuántas personas se detienen a ver tu perfil por más de 6 segundos. Esta métrica refleja la atención genuina que tu negocio genera dentro de la plataforma",
                                                        texto_params = "Interés real",
                                                        expandido = mostar_interes,
                                                        onClickExpand = {
                                                            mostar_interes = !mostar_interes
                                                        }
                                                    )
                                                    AnimatedVisibility(visible = mostar_interes) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .animateContentSize(),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            LazyRow(
                                                                contentPadding = PaddingValues(
                                                                    horizontal = 10.dp
                                                                ),
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    10.dp
                                                                )
                                                            ) {
                                                                itemsIndexed(values) { index, value ->
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.Center,
                                                                        modifier = Modifier.clickable {
                                                                            when (labels[index]) {

                                                                            }

                                                                        }
                                                                    ) {
                                                                        Box(
                                                                            Modifier
                                                                                .size(12.dp)
                                                                                .background(
                                                                                    color = listOf(
                                                                                        Color(
                                                                                            0xFFFF6B6B
                                                                                        ),
                                                                                        Color(
                                                                                            0xFF4ECDC4
                                                                                        ),
                                                                                        Color(
                                                                                            0xFF4EFF00
                                                                                        ),
                                                                                    )[index],
                                                                                    shape = CircleShape
                                                                                )
                                                                        )

                                                                        spacer_horizonta(8.dp)

                                                                        texto_generico_one_line("${labels[index]}: ${value.toInt()}",MaterialTheme.typography.bodyMedium)
                                                                    }
                                                                }
                                                            }
                                                            spacer_vertical(10.dp)
                                                            PieChart(
                                                                dataSet = values.toChartDataSet(
                                                                    labels = labels,
                                                                    title = "",
                                                                    postfix = ""     // nada
                                                                ),
                                                                style = PieChartDefaults.style(
                                                                    donutPercentage = 40f,
                                                                    pieColors = listOf(
                                                                        Color(0xFFFF6B6B),
                                                                        Color(0xFF4ECDC4),
                                                                        Color(0xFF4EFF00)
                                                                    )
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Cartas_expandibles(
                                                modifier = Modifier.padding(
                                                    vertical = 10.dp
                                                )
                                            ) {
                                                val lsita_datos2 = listOf(
                                                    datos_grafico(
                                                        R.drawable.facebook_icon,
                                                        "Facebook",
                                                        datos.fb.toString()
                                                    ),
                                                    datos_grafico(
                                                        R.drawable.instagram_icon,
                                                        "Instagra",
                                                        datos.ig.toString()
                                                    ),
                                                    datos_grafico(
                                                        R.drawable.tik_tok_icon,
                                                        "Tik tok",
                                                        datos.tk.toString()
                                                    ), datos_grafico(
                                                        R.drawable.web_icon,
                                                        "Sitio web",
                                                        datos.stweb.toString()
                                                    )
                                                )
                                                Column() {
                                                    expandibles_wrapp_socio_geinzz(

                                                        lsita_datos2,
                                                        "Este indicador muestra cuántas personas hicieron clic en tus perfiles de redes sociales o en tu sitio web después de ver tu página. Refleja el nivel de intención que tiene el usuario de saber más sobre tu negocio y avanzar hacia un contacto directo",
                                                        texto_params = "Convesion",
                                                        expandido = mostrar_convesion,
                                                        onClickExpand = {
                                                            mostrar_convesion = !mostrar_convesion
                                                        }
                                                    )
                                                    AnimatedVisibility(visible = mostrar_convesion) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .animateContentSize(),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            LazyRow(
                                                                contentPadding = PaddingValues(
                                                                    horizontal = 10.dp
                                                                ),
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    10.dp
                                                                )
                                                            ) {
                                                                itemsIndexed(values2) { index, value ->
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.Center,
                                                                        modifier = Modifier.clickable {
                                                                            when (labels2[index]) {
                                                                                "Vistas" -> {
                                                                                    dialog_mostar_leyendas_graficos =
                                                                                        true
                                                                                    titulo_leyenda_dialog =
                                                                                        "Vistas"
                                                                                    txt_leyenda =
                                                                                        "Las vistas se registran cuando un usuario permanece viendo tu perfil durante más de 6 segundos. Representan el interés real que genera tu negocio."
                                                                                    icono_mostar_leyendas_graficos =
                                                                                        R.drawable.vizualizacion_icon_3d
                                                                                }

                                                                                "Guardados" -> {
                                                                                    dialog_mostar_leyendas_graficos =
                                                                                        true
                                                                                    titulo_leyenda_dialog =
                                                                                        "Guardados"
                                                                                    txt_leyenda =
                                                                                        "Los guardados indican cuántos usuarios añadieron a ${datos.nombre} a su lista de favoritos. Es una métrica que refleja cuánta gente quiere volver a encontrar tu tienda rápidamente."
                                                                                    icono_mostar_leyendas_graficos =
                                                                                        R.drawable.corazon_gracias
                                                                                }

                                                                                "Clics" -> {
                                                                                    dialog_mostar_leyendas_graficos =
                                                                                        true
                                                                                    titulo_leyenda_dialog =
                                                                                        "clics"
                                                                                    txt_leyenda =
                                                                                        "Los clics representan cuántos usuarios tocaron tu negocio y abrieron directamente el perfil de la tienda o negocio. Miden la intención inmediata de conocer más sobre ti."
                                                                                    icono_mostar_leyendas_graficos =
                                                                                        R.drawable.click_icon3d
                                                                                }
                                                                            }

                                                                        }
                                                                    ) {
                                                                        Box(
                                                                            Modifier
                                                                                .size(12.dp)
                                                                                .background(
                                                                                    color = listOf(
                                                                                        Color(
                                                                                            0xFF1877F2
                                                                                        ),
                                                                                        Color(
                                                                                            0xFFE1306C
                                                                                        ),
                                                                                        Color(
                                                                                            0xFF69C9D0
                                                                                        ),
                                                                                        Color(
                                                                                            0xFF6366F1
                                                                                        ),
                                                                                    )[index],
                                                                                    shape = CircleShape
                                                                                )
                                                                        )

                                                                        spacer_horizonta(8.dp)

                                                                        texto_generico_one_line("${labels2[index]}: ${value.toInt()}",MaterialTheme.typography.bodyMedium)
                                                                    }
                                                                }
                                                            }
                                                            spacer_vertical(10.dp)
                                                            PieChart(
                                                                dataSet = values2.toChartDataSet(
                                                                    labels = labels2,
                                                                    title = "",
                                                                    postfix = ""     // nada
                                                                ),
                                                                style = PieChartDefaults.style(
                                                                    donutPercentage = 40f,
                                                                    pieColors = listOf(
                                                                        Color(0xFF1877F2),
                                                                        Color(0xFFE1306C),
                                                                        Color(0xFF69C9D0),
                                                                        Color(0xFF6366F1),
                                                                    )
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Cartas_expandibles(
                                                modifier = Modifier.padding(
                                                    vertical = 10.dp
                                                )
                                            ) {
                                                val lsita_datos3 = listOf(
                                                    datos_grafico(
                                                        R.drawable.llamada_icon,
                                                        "Llamada",
                                                        datos.llamada.toString()
                                                    ),
                                                    datos_grafico(
                                                        R.drawable.whatsapp_icon,
                                                        "Whatsapp",
                                                        datos.wsap.toString()
                                                    ),
                                                    datos_grafico(
                                                        R.drawable.icon_3d_ruta,
                                                        "Rutas",
                                                        datos.ruta.toString()
                                                    )
                                                )
                                                Column() {
                                                    expandibles_wrapp_socio_geinzz(
                                                        lsita_datos3,
                                                        "Mide cuántas personas usaron accesos externos como WhatsApp, llamadas o enlaces directos para comunicarse contigo fuera de la plataforma.",
                                                        texto_params = "Tráfico externo",
                                                        expandido = mostrar_trafico_externo,
                                                        onClickExpand = {
                                                            mostrar_trafico_externo =
                                                                !mostrar_trafico_externo
                                                        }
                                                    )
                                                    AnimatedVisibility(visible = mostrar_trafico_externo) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .animateContentSize(),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            spacer_vertical(20.dp)
                                                            spacer_vertical(5.dp)
                                                            LazyRow(
                                                                contentPadding = PaddingValues(
                                                                    horizontal = 10.dp
                                                                ),
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    10.dp
                                                                )
                                                            ) {
                                                                itemsIndexed(values3) { index, value ->
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.Center,
                                                                        modifier = Modifier.clickable {
                                                                            when (labels3[index]) {


                                                                            }

                                                                        }
                                                                    ) {
                                                                        Box(
                                                                            Modifier
                                                                                .size(12.dp)
                                                                                .background(
                                                                                    color = listOf(
                                                                                        Color(
                                                                                            0xFF18C5A4
                                                                                        ),
                                                                                        Color(
                                                                                            0xFF25D366
                                                                                        ),
                                                                                        Color(
                                                                                            0xFF6A0DAD
                                                                                        )
                                                                                    )[index],
                                                                                    shape = CircleShape
                                                                                )
                                                                        )

                                                                        spacer_horizonta(8.dp)

                                                                        texto_generico_one_line("${labels3[index]}: ${value.toInt()}",MaterialTheme.typography.bodyMedium)
                                                                    }
                                                                }
                                                            }
                                                            spacer_vertical(10.dp)
                                                            PieChart(
                                                                dataSet = values3.toChartDataSet(
                                                                    labels = labels3,
                                                                    title = "",
                                                                    postfix = ""     // nada
                                                                ),
                                                                style = PieChartDefaults.style(
                                                                    donutPercentage = 40f,
                                                                    pieColors = listOf(
                                                                        Color(0xFF18C5A4),
                                                                        Color(0xFF25D366),
                                                                        Color(0xFF6A0DAD)
                                                                    )
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (dialog_mostar_leyendas_graficos) {
                dialog_mostar_leyendas_graficos(
                    icono_mostar_leyendas_graficos,
                    titulo_leyenda_dialog,
                    txt_leyenda,
                    { dialog_mostar_leyendas_graficos = false })
            }
        }


    }
}



package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.util.Base64

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.EstadoUI
import com.geinzz.geinzwork.data.model.IconoIA
import com.geinzz.geinzwork.data.model.NotificacionIA_dialog
import com.geinzz.geinzwork.data.model.OpcionPromocionIA
import com.geinzz.geinzwork.data.model.datos_generaciones_sin_publicaicones
import com.geinzz.geinzwork.data.model.datos_para_generacion_dialog_historial_IA
import com.geinzz.geinzwork.data.model.dialog_generaciones_IA_promo_noti
import com.geinzz.geinzwork.data.model.lista_genereracione
import com.geinzz.geinzwork.data.model.nuevas_generaciones_con_IA
import com.geinzz.geinzwork.data.model.obt_item_gen_IA
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.timestampAFechaLegible
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoSubrayado
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line_Expandible
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dailog_generaciones_IA_versiones
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_guardar_IA_permanete
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.parseDiasHorasRestantes
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.FondoIAAnimado
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_generaciones_IA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import org.json.JSONArray
import kotlin.text.trim

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ui_bottom_sheet_generaciones_IA(
    i_datos_parametros: datos_para_generacion_dialog_historial_IA,
    ondismis: () -> Unit,
    nombre_tienda: String,
    usar_todas: (String, String, String, String, String, String, i: datos_generaciones_sin_publicaicones) -> Unit,
    usar_titulo_descripcion: (String, String, String, String, i: datos_generaciones_sin_publicaicones) -> Unit,
    usar_wsap: (String, String, String) -> Unit,
    usar_compartir: (String, String, String) -> Unit
) {
    val contex = LocalContext.current
    val viewmodelGeneracionesIa: viewmodel_generaciones_IA = viewModel()
    val estaod_generaciones_IA by viewmodelGeneracionesIa.estado_generaciones_IA.collectAsState()
    var mostar_dialog_mejorar_versiones by remember { mutableStateOf(false) }
    var titulo_dialog_mejorar_version by remember { mutableStateOf("") }
    var texto_dialog_mejorar_version by remember { mutableStateOf("") }
    var tipo_seleccionado_promo_noti by remember { mutableStateOf("") }
    var id_seleccionado_gen by remember { mutableStateOf("") }

    var subCategoriaSeleccionada by remember { mutableStateOf("Generaciones de promociones") }
    var filtroProfundo by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var resultadoIA by remember {
        mutableStateOf<dialog_generaciones_IA_promo_noti?>(null)
    }
    var nueva_generacion_notificaciones by remember { mutableStateOf(NotificacionIA_dialog()) }

    val scope = rememberCoroutineScope()
    var mostar_dialog_guardar_permanente by remember { mutableStateOf(false) }
    var id_seleccionado_para_ilimitado by remember { mutableStateOf("") }
    var img_seleccionado_para_ilimitado by remember { mutableStateOf("") }
    var titulo_seleccionado_para_ilimitado by remember { mutableStateOf("") }
    val estado_textos_promociones_generadas by viewmodelGeneracionesIa.estado_promociones_ia.collectAsState()
    val estado_textos_notificaciones_generad by viewmodelGeneracionesIa.estado_notificaion_con_ia_corta.collectAsState()
    val datos_cloud_TTs by viewmodelGeneracionesIa.datosCloudTts.collectAsState()
    val texto_cloudsTTS by viewmodelGeneracionesIa.textoCloudTts.collectAsState()
    var procesar_busqueda_box by remember { mutableStateOf<String?>(null) }
    var filtroTerminos by remember { mutableStateOf("") }
    var precio_data by remember { mutableStateOf<String?>(null) }
    var tiempo_data by remember { mutableStateOf<String?>(null) }
    var prioridad_data by remember { mutableStateOf<String?>(null) }
    var tipo_data by remember { mutableStateOf<String?>(null) }


    val buscar_por_nombre = remember { mutableStateOf("") }

    var grabando by remember { mutableStateOf(false) }
    var cargandoVoz by remember { mutableStateOf(false) }
    var cargandoIA by remember { mutableStateOf(false) }

    var audioData by remember { mutableStateOf(ByteArray(0)) }
    var resultadoIA_nuleable by remember { mutableStateOf<String?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Se necesita permiso de micrófono",

                    duration = SnackbarDuration.Short
                )
            }
    }
    var mostrar_texto_por_IA by remember { mutableStateOf(false) }
    var mensaje_de_la_IA by remember { mutableStateOf("") }

    LaunchedEffect(datos_cloud_TTs) {
        if (datos_cloud_TTs.isNotEmpty()) {
            // Aquí ya puedes reproducir el audio
            viewmodelGeneracionesIa.reproducirMP3(contex, datos_cloud_TTs)
        }
    }
    LaunchedEffect(
        subCategoriaSeleccionada,
        viewmodelGeneracionesIa.filtroTerminos,
        viewmodelGeneracionesIa.tipo_data,
        viewmodelGeneracionesIa.prioridad_data,
        viewmodelGeneracionesIa.tiempo_data,
        viewmodelGeneracionesIa.dias_restantes,
        viewmodelGeneracionesIa.filtroProfundo
    ) {
        viewmodelGeneracionesIa.cambiar_filtrado(subCategoriaSeleccionada)
    }
    val listaMostrada = viewmodelGeneracionesIa.listaFiltrada
    LaunchedEffect(estado_textos_promociones_generadas) {
        if (estado_textos_promociones_generadas is viewmodel_generaciones_IA.EstadoIA_dialog_centrado.Success) {
            resultadoIA =
                (estado_textos_promociones_generadas as viewmodel_generaciones_IA.EstadoIA_dialog_centrado.Success).generacion
            Log.d("desde_otro_sitio_Verinado", "$resultadoIA")
            resultadoIA?.let { resultado ->
                viewmodelGeneracionesIa.agregar_nueva_generacion_remasterizada(
                    titulo_dialog_mejorar_version, texto_dialog_mejorar_version,
                    i_datos_parametros.id_tienda,
                    i_datos_parametros.localidad_tienda,
                    resultado.titulo,
                    resultado.descripcion,
                    resultado.id_promo_noti_gen
                )
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "La nueva generación se creó correctamente.",

                    duration = SnackbarDuration.Short
                )
            }
            viewmodelGeneracionesIa.limpiar_Estado_nueva_generacion()
        } else if (estado_textos_notificaciones_generad is viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Error) {
            val mejse_error =
                (estado_textos_notificaciones_generad as viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Error).mensaje
            scope.launch {

                snackbarHostState.showSnackbar(
                    message = mejse_error,

                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(estado_textos_notificaciones_generad) {
        if (estado_textos_notificaciones_generad is viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Success) {
            nueva_generacion_notificaciones =
                (estado_textos_notificaciones_generad as viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Success).txt_descripcion
            Log.d(
                "desde_otro_sitio_Verinado",
                "${nueva_generacion_notificaciones.id_promo_noti_gen}"
            )

            Log.d("desde_otro_sitio_Verinado", "${nueva_generacion_notificaciones.titulo}")
            Log.d(
                "desde_otro_sitio_Verinado",
                "${nueva_generacion_notificaciones.id_promo_noti_gen}"
            )

            viewmodelGeneracionesIa.agregar_nueva_generacion_remasterizada(
                titulo_dialog_mejorar_version, texto_dialog_mejorar_version,
                i_datos_parametros.id_tienda,
                i_datos_parametros.localidad_tienda,
                nueva_generacion_notificaciones.titulo,
                nueva_generacion_notificaciones.descripcion,
                nueva_generacion_notificaciones.id_promo_noti_gen
            )

            scope.launch {
                // Llama al SnackbarHostState para mostrar el mensaje
                snackbarHostState.showSnackbar(
                    message = "La nueva generación se creó correctamente.",

                    duration = SnackbarDuration.Short
                )
            }
            viewmodelGeneracionesIa.resetear_Estado_notificacion_enviadad()
        } else if (estado_textos_notificaciones_generad is viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Error) {
            val mejse_error =
                (estado_textos_notificaciones_generad as viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Error).mensaje

            scope.launch {
                snackbarHostState.showSnackbar(
                    message = mejse_error,

                    duration = SnackbarDuration.Short
                )
            }
        }
    }


    val lsita_fitlrado_opciones = listOf(
        "Todos",
        "Permanentes",
        "Generaciones de promociones",
        "Generaciones no publicadas (promociones)",
        "Generaciones de notificaciones",
        "Generaciones no publicadas (notificaciones)",
        "Por vencer",
    )

    val listaEstadosUI = listOf(
        EstadoUI(9, "Notificación (promoción seleccionada)", Color(0xFF1262C5)),
        EstadoUI(7, "Notificación creada de 0", Color(0xFF12B974)),
        EstadoUI(5, "Notificación informativa", Color(0xFFC90000))
    )


    LaunchedEffect(i_datos_parametros.id_tienda, i_datos_parametros.localidad_tienda) {
        viewmodelGeneracionesIa.obtner_generaciones_IA(
            i_datos_parametros.localidad_tienda,
            i_datos_parametros.id_tienda
        )
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
                        viewmodelGeneracionesIa.cargarLista(lista)
//                        val lista_filtrada = when (subCategoriaSeleccionada) {
//                            "Todos" -> {
//                                lista
//                            }
//
//                            "Permanentes" -> {
//                                lista.filter { it.fin == null }
//                            }
//
//                            "Generaciones no publicadas (promociones)" -> {
//                                lista.filter { it.tipo_realizado == "generacion_publicacion_sin_pulicar" }
//                            }
//
//                            "Generaciones no publicadas (notificaciones)" -> {
//                                lista.filter { it.tipo_realizado == "notificacion_sin_publicar" }
//                            }
//
//                            "Generaciones de promociones" -> {
//                                lista.filter { it.tipo_realizado == "publicacion" }
//                            }
//
//                            "Generaciones de notificaciones" -> {
//                                lista.filter { it.tipo_realizado == "notificacion" }
//                            }
//
//                            "Por vencer" -> {
//                                val diasLimite = 1
//
//                                lista.filter { item ->
//                                    val tiempo = item.fin?.let {
//                                        constantes_datos_expirados_fechas_publicaciones.tiempoRestante(
//                                            it
//                                        )
//                                    } ?: return@filter false
//
//                                    when {
//                                        tiempo == "Expirado" -> false
//
//                                        // 🔥 entra si son minutos
//                                        tiempo.contains("mto", ignoreCase = true) ||
//                                                tiempo.contains("min", ignoreCase = true) -> true
//
//                                        // 🔥 entra si son horas
//                                        tiempo.contains("hora", ignoreCase = true) -> true
//
//                                        // 🔥 entra si son días <= limite
//                                        tiempo.contains("día", ignoreCase = true) -> {
//                                            val dias = tiempo.filter { it.isDigit() }
//                                                .toIntOrNull() ?: return@filter false
//                                            dias in 0..diasLimite
//                                        }
//
//                                        else -> false
//                                    }
//                                }
//                            }
//
//
//                            else -> {
//                                lista
//                            }
//                        }
//                        val listaFinal = remember(
//                            lista_filtrada,
//                            subCategoriaSeleccionada,
//                            filtroProfundo
//                        ) {
//                            if (
//                                subCategoriaSeleccionada == "Generaciones no publicadas (notificaciones)" &&
//                                filtroProfundo != null
//                            ) {
//                                lista_filtrada.filter {
//                                    it.id_promo_noti_cread.length == filtroProfundo
//                                }
//                            } else {
//                                lista_filtrada
//                            }
//                        }

//                        val listaMostrada_final = remember(
//                            listaFinal,
//                            filtroTerminos,
//                            tipo_data,
//                            prioridad_data,
//                            tiempo_data,
//                            precio_data,
//                            subCategoriaSeleccionada
//                        ) {
//                            Log.d("FILTRADO", "Lista inicial tamaño: ${listaFinal.size}")
//                            Log.d(
//                                "FILTRADO",
//                                "Filtros: filtroTerminos='$filtroTerminos', tipo_data=$tipo_data, prioridad_data=$prioridad_data, tiempo_data=$tiempo_data, precio_data=$precio_data, subCategoriaSeleccionada=$subCategoriaSeleccionada"
//                            )
//
//                            // Si no hay filtros activos o subcategoria no es "Todos", devolvemos lista final directamente
//                            if (subCategoriaSeleccionada != "Todos" &&
//                                filtroTerminos.isBlank() &&
//                                tipo_data == null &&
//                                prioridad_data == null &&
//                                tiempo_data == null &&
//                                precio_data == null
//                            ) {
//                                Log.d("FILTRADO", "No se aplicaron filtros, lista final tamaño=${listaFinal.size}")
//                                listaFinal
//                            } else {
//                                var listaFiltrada = listaFinal
//
//                                // 🔹 FILTRO POR TIPO IA
//                                val tiposValidos = listOf(
//                                    "publicacion",
//                                    "notificacion",
//                                    "generacion_no_publicada",
//                                    "notificacion_no_publicada",
//                                    "generacion_publicacion_sin_publicar", // si manejas promociones
//                                    "notificacion_sin_publicar"           // si manejas promociones
//                                )
//
//                                tipo_data?.let { tipo ->
//                                    val tipoSeguro = tipo.lowercase().takeIf { it in tiposValidos }
//                                    tipoSeguro?.let { t ->
//                                        listaFiltrada = listaFiltrada.filter {
//                                            it.tipo_realizado.equals(t, ignoreCase = true)
//                                        }
//                                        Log.d("FILTRADO", "Después filtro tipo_data='$t' tamaño=${listaFiltrada.size}")
//                                    } ?: Log.d("FILTRADO", "tipo_data='$tipo' no es válido, no se filtró")
//                                }
//                                // 🔹 FILTRO POR TIEMPO
//                                tiempo_data?.let { tiempo ->
//                                    listaFiltrada = listaFiltrada.filter {
//                                        viewmodelGeneracionesIa.coincideConTiempo(it.inicio, tiempo)
//                                    }
//                                    Log.d("FILTRADO", "Después filtro tiempo_data='$tiempo' tamaño=${listaFiltrada.size}")
//                                }
//
//                                // 🔹 FILTRO POR PRIORIDAD
//                                prioridad_data?.let { prioridad ->
//                                    listaFiltrada = listaFiltrada.filter { it.fin == null }
//                                    Log.d("FILTRADO", "Después filtro prioridad_data='$prioridad' tamaño=${listaFiltrada.size}")
//                                }
//
//                                // 🔹 FILTRO POR TÉRMINOS
//                                if (filtroTerminos.isNotBlank()) {
//                                    val terminos = filtroTerminos.lowercase().split(" ").filter { it.isNotBlank() }
//
//                                    listaFiltrada = listaFiltrada.filter { item ->
//                                        val titulos = listOfNotNull(
//                                            item.datos_generaciones.titulo_seleccionado_gen_IA,
//                                            item.nombre_generacion,
//                                            item.datos_generaciones.titulo_original,
//                                            item.nuevas_generaciones.titulo_nuevo
//                                        ).joinToString(" ").lowercase()
//
//                                        // Evitar lista vacía
//                                        if (titulos.isEmpty()) return@filter false
//
//                                        // Coincidencia: basta con que un término esté presente
//                                        terminos.any { termino -> titulos.contains(termino) }
//                                    }
//                                    Log.d("FILTRADO", "Después filtro terminos='$filtroTerminos' tamaño=${listaFiltrada.size}")
//                                }
//
//                                Log.d("FILTRADO", "Lista final filtrada tamaño=${listaFiltrada.size}")
//                                listaFiltrada
//                            }
//                        }


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
                                if (subCategoriaSeleccionada == "Todos") {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {

                                            // 🔤 TextField
                                            MySearchTextField(
                                                value = buscar_por_nombre.value,
                                                onValueChange = { buscar_por_nombre.value = it },
                                                label = "Busca por palabra",
                                                placeholder = "Escribe o habla",
                                                isError = false,
                                                textoError = ""
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                                                // 🎤 BOTÓN HABLAR
                                                Button(
                                                    onClick = {
                                                        if (ContextCompat.checkSelfPermission(
                                                                contex,
                                                                Manifest.permission.RECORD_AUDIO
                                                            ) != PackageManager.PERMISSION_GRANTED
                                                        ) {
                                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                            return@Button
                                                        }

                                                        if (!grabando && !cargandoVoz) {
                                                            grabando = true
                                                            audioData = ByteArray(0)

                                                            scope.launch(Dispatchers.IO) {
                                                                val minBuffer =
                                                                    AudioRecord.getMinBufferSize(
                                                                        16000,
                                                                        AudioFormat.CHANNEL_IN_MONO,
                                                                        AudioFormat.ENCODING_PCM_16BIT
                                                                    )

                                                                val recorder = AudioRecord(
                                                                    MediaRecorder.AudioSource.MIC,
                                                                    16000,
                                                                    AudioFormat.CHANNEL_IN_MONO,
                                                                    AudioFormat.ENCODING_PCM_16BIT,
                                                                    minBuffer
                                                                )

                                                                val buffer = ByteArray(minBuffer)
                                                                val stream = ByteArrayOutputStream()

                                                                recorder.startRecording()
                                                                while (grabando) {
                                                                    val read = recorder.read(
                                                                        buffer,
                                                                        0,
                                                                        buffer.size
                                                                    )
                                                                    if (read > 0) stream.write(
                                                                        buffer,
                                                                        0,
                                                                        read
                                                                    )
                                                                }
                                                                recorder.stop()
                                                                recorder.release()
                                                                audioData = stream.toByteArray()
                                                            }

                                                        } else if (grabando) {
                                                            grabando = false
                                                            cargandoVoz = true

                                                            scope.launch(Dispatchers.IO) {
                                                                while (audioData.isEmpty()) delay(50)

                                                                try {
                                                                    val texto =
                                                                        viewmodelGeneracionesIa.tranformar_texto_a_voz(
                                                                            audioData
                                                                        )
                                                                    withContext(Dispatchers.Main) {
                                                                        if (texto.isNotEmpty()) {
                                                                            buscar_por_nombre.value =
                                                                                texto
                                                                            viewmodelGeneracionesIa.textoReconocido =
                                                                                texto
                                                                        } else {
                                                                            snackbarHostState.showSnackbar(
                                                                                message = "No se reconoció voz",
                                                                                duration = SnackbarDuration.Short
                                                                            )
                                                                        }
                                                                        cargandoVoz = false
                                                                    }
                                                                } catch (e: Exception) {
                                                                    withContext(Dispatchers.Main) {
                                                                        cargandoVoz = false
                                                                        snackbarHostState.showSnackbar(
                                                                            message = "Error al enviar audio",
                                                                            duration = SnackbarDuration.Short
                                                                        )
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    if (cargandoVoz) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(20.dp),
                                                            strokeWidth = 2.dp,
                                                            color = Color.White
                                                        )
                                                    } else {
                                                        Text(
                                                            if (grabando) "Detener" else "Hablar 🎤",
                                                            color = Color.White
                                                        )
                                                    }
                                                }

                                                // 🔍 BOTÓN BUSCAR (IA)
                                                Button(
                                                    onClick = {
                                                        if (buscar_por_nombre.value.isBlank()) return@Button

                                                        cargandoIA = true
                                                        resultadoIA_nuleable = null
                                                        mostrar_texto_por_IA = false
                                                        mensaje_de_la_IA = ""

                                                        scope.launch {
                                                            // Esperamos a que se complete la búsqueda
                                                            val resultado =
                                                                viewmodelGeneracionesIa.procesar_busqueda_con_IA(
                                                                    buscar_por_nombre.value
                                                                )
                                                            resultadoIA_nuleable = resultado

                                                            // 🔹 Procesar JSON
                                                            if (!resultado.isNullOrEmpty()) {
                                                                val jsonString =
                                                                    resultado.replace("```json", "")
                                                                        .replace("```", "").trim()
                                                                try {
                                                                    val json =
                                                                        JSONObject(jsonString)
                                                                    val terminosArray =
                                                                        json.optJSONArray("terminos")
                                                                            ?: JSONArray()
                                                                    val tiempo =
                                                                        json.optStringOrNull("tiempo")
                                                                    val precio =
                                                                        json.optStringOrNull("precio")
                                                                    val prioridad =
                                                                        json.optStringOrNull("prioridad")
                                                                    val tipo =
                                                                        json.optStringOrNull("tipo")

                                                                    val terminosList =
                                                                        List(terminosArray.length()) { i ->
                                                                            terminosArray.optString(
                                                                                i
                                                                            )
                                                                        }

                                                                    val mensaje =
                                                                        viewmodelGeneracionesIa.generarMensajeVoz(
                                                                            nombre_negocio = i_datos_parametros.nombre_tienda,
                                                                            cantidad = viewmodelGeneracionesIa.obtenerCantidadFiltrada(),
                                                                            terminos = terminosList,
                                                                            tiempo = tiempo,
                                                                            precio = precio,
                                                                            prioridad = prioridad,
                                                                            tipo = tipo
                                                                        )

                                                                    // 🔹 Reproducir TTS
                                                                    mensaje?.let {
                                                                        viewmodelGeneracionesIa.cloudTTS(
                                                                            it
                                                                        )
                                                                        mostrar_texto_por_IA = true
                                                                        mensaje_de_la_IA = it
                                                                    }

                                                                } catch (e: JSONException) {
                                                                    Log.e(
                                                                        "JSON",
                                                                        "Error al parsear JSON: ${e.message}"
                                                                    )
                                                                }
                                                            } else {
                                                                Log.w(
                                                                    "IA",
                                                                    "resultadoIA es null o vacío, no se procesará"
                                                                )
                                                            }

                                                            cargandoIA = false
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    if (cargandoIA) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(20.dp),
                                                            strokeWidth = 2.dp,
                                                            color = Color.White
                                                        )
                                                    } else {
                                                        Icon(
                                                            Icons.Default.Search,
                                                            contentDescription = null
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Buscar", color = Color.White)
                                                    }
                                                }
                                            }

                                            // 📄 RESULTADO JSON
                                            if (resultadoIA_nuleable != null) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                try {
                                                    val jsonString = resultadoIA_nuleable
                                                        ?.replace("```json", "")
                                                        ?.replace("```", "")
                                                        ?.trim()
                                                        ?: "{}" // Por si es null

                                                    val json = JSONObject(jsonString)

                                                    val terminosArray =
                                                        json.optJSONArray("terminos") ?: JSONArray()
                                                    val dias_restantes: Int? = json.opt("dias_restantes")
                                                        ?.takeIf { it != JSONObject.NULL } // ignora null de JSON
                                                        ?.toString()                        // pasa a String temporalmente
                                                        ?.toIntOrNull()                     // convierte a Int? seguro

                                                    val tiempo = json.opt("tiempo")
                                                        ?.takeIf { it != JSONObject.NULL }
                                                        ?.toString()
                                                    val prioridad = json.opt("prioridad")
                                                        ?.takeIf { it != JSONObject.NULL }
                                                        ?.toString()
                                                    val tipo = json.opt("tipo")
                                                        ?.takeIf { it != JSONObject.NULL }
                                                        ?.toString()


                                                    val textoBusqueda = buildString {
                                                        for (i in 0 until terminosArray.length()) {
                                                            append(terminosArray.getString(i))
                                                            if (i < terminosArray.length() - 1) append(
                                                                " "
                                                            )
                                                        }
                                                    }

                                                    viewmodelGeneracionesIa.filtroTerminos = textoBusqueda
                                                    viewmodelGeneracionesIa.dias_restantes= dias_restantes
                                                    viewmodelGeneracionesIa.tiempo_data = tiempo
                                                    viewmodelGeneracionesIa.tipo_data = tipo
                                                    viewmodelGeneracionesIa.prioridad_data = prioridad
                                                    viewmodelGeneracionesIa.actualizarListaFiltrada()
                                                } catch (e: JSONException) {
                                                    Log.e("IA_PARSE", "Error parseando JSON IA", e)
                                                }

                                                Text(
                                                    text = "Resultado IA (JSON)",
                                                    style = MaterialTheme.typography.titleSmall
                                                )

//                                                if (mostrar_texto_por_IA) {
//                                                    TypewriterText(mensaje_de_la_IA)
//                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color(0xFF121212),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(12.dp)
                                                ) {
                                                    Text(
                                                        text = resultadoIA_nuleable!!,
                                                        color = Color(0xFF00E676),
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }

//                                        CloudSpeechSearchWithIA(viewmodelGeneracionesIa,size_lista_ya_filtrada,i_datos_parametros.nombre_tienda) { res ->
//                                            Log.d("respuesta_IA", "$res")
//                                            try {
//                                                val json = JSONObject(res)
//
//
//                                                val terminosArray = json.optJSONArray("terminos") ?: JSONArray()
//
//                                                var precio = if (json.isNull("precio")) null else json.optString("precio")
//                                                var tiempo = if (json.isNull("tiempo")) null else json.optString("tiempo")
//                                                var prioridad = if (json.isNull("prioridad")) null else json.optString("prioridad")
//                                                var tipo = if (json.isNull("tipo")) null else json.optString("tipo")
//
//                                                val textoBusqueda = buildString {
//                                                    for (i in 0 until terminosArray.length()) {
//                                                        append(terminosArray.getString(i))
//                                                        if (i < terminosArray.length() - 1) append(
//                                                            " "
//                                                        )
//                                                    }
//                                                }
//
//                                                filtroTerminos = textoBusqueda
//                                                precio_data = precio
//                                                tiempo_data= tiempo
//                                                tipo_data= tipo
//                                                prioridad_data= prioridad
//                                            } catch (e: Exception) {
//                                                Log.e("IA_PARSE", "Error parseando JSON IA", e)
//                                            }
//                                        }
                                    }
                                }
                                item {
                                    if (
                                        subCategoriaSeleccionada == "Generaciones no publicadas (notificaciones)"
                                    ) {
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(listaEstadosUI) { i ->
                                                EstadoChip(
                                                    i,
                                                    viewmodelGeneracionesIa.filtroProfundo == i.valor
                                                ) { valorSeleccionado ->
                                                    viewmodelGeneracionesIa.filtroProfundo =
                                                        valorSeleccionado
                                                }
                                            }
                                        }
                                    }
                                }

                                // 🟡 Caso: lista vacía
                                if (listaMostrada.isEmpty()) {
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
                                }

                                // 🟢 Caso: lista con datos
                                else {
                                    itemsIndexed(
                                        items = listaMostrada,
                                        key = { _, item -> item.id_promo_noti_cread }
                                    ) { _, i ->

                                        val datos = obt_item_gen_IA(
                                            id_generacion = i.id_promo_noti_cread,
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
                                                i.nuevas_generaciones,
                                                tituo_seleccionado = i.datos_generaciones.titulo_seleccionado_gen_IA,
                                                descrpcion_seleccionada = i.datos_generaciones.descripcion_seleccionada_ge_IA,
                                                i = datos,

                                                usar_todas = { titulo, descripcion, whatsapp, compartir, tipo, id_generacion, datos_generaciones_sin_publicaicones ->
                                                    usar_todas(
                                                        titulo,
                                                        descripcion,
                                                        whatsapp,
                                                        compartir,
                                                        tipo,
                                                        id_generacion,
                                                        datos_generaciones_sin_publicaicones
                                                    )
                                                },

                                                usar_titulo_descripcion = { titulo, descripcion, tipo, id, id_generacion, datos_generaciones_sin_publicaicones ->
                                                    usar_titulo_descripcion(
                                                        titulo,
                                                        descripcion,
                                                        tipo,
                                                        id_generacion,
                                                        datos_generaciones_sin_publicaicones
                                                    )
                                                },

                                                whatsapp = { mensaje, tipo, id ->
                                                    usar_wsap(mensaje, tipo, id)
                                                },

                                                compartir = { mensaje, tipo, id ->
                                                    usar_compartir(mensaje, tipo, id)
                                                },

                                                mostrar_dialog_mejorar_vesiones = { titulo, texto, tipo, id_ ->
                                                    id_seleccionado_gen = id_
                                                    mostar_dialog_mejorar_versiones = true
                                                    titulo_dialog_mejorar_version = titulo
                                                    texto_dialog_mejorar_version = texto
                                                    tipo_seleccionado_promo_noti = tipo
                                                },

                                                usar_nueva_generacion_generada = { titulo, texto, id, tipo ->
                                                    usar_titulo_descripcion(
                                                        titulo,
                                                        texto,
                                                        tipo,
                                                        id,
                                                        datos_generaciones_sin_publicaicones(
                                                            lista_obciones = null,
                                                            titulo_original = null,
                                                            descripcion_original = null,
                                                            titulo_seleccionado = null,
                                                            descripcion_seleccionada = null
                                                        )
                                                    )
                                                }, guardado_permanente = { id_gen, img, titulo ->
                                                    if (i_datos_parametros.monedas_tienda < 13) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Saldo insuficiente: necesitas 13 monedas para guardar esta generación",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    } else {
                                                        mostar_dialog_guardar_permanente = true
                                                        id_seleccionado_para_ilimitado = id_gen
                                                        img_seleccionado_para_ilimitado = img
                                                        titulo_seleccionado_para_ilimitado = titulo
                                                    }


                                                }
                                            )
                                        }


                                    }
                                }

                                item {
                                    spacer_vertical(10.dp)
                                }

                            }

                            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))


                            if (mostar_dialog_mejorar_versiones) {
                                dailog_generaciones_IA_versiones(
                                    id_seleccionado_gen,
                                    i_datos_parametros,
                                    viewmodelGeneracionesIa,
                                    titulo_dialog_mejorar_version,
                                    texto_dialog_mejorar_version, tipo_seleccionado_promo_noti,
                                    { mostar_dialog_mejorar_versiones = false })
                            }

                            if (mostar_dialog_guardar_permanente) {
                                dialog_guardar_IA_permanete(
                                    viewmodel = viewmodelGeneracionesIa,
                                    ondimis = { mostar_dialog_guardar_permanente = false },
                                    id_generacion = id_seleccionado_para_ilimitado,
                                    id_tienda = i_datos_parametros.id_tienda,
                                    localidad = i_datos_parametros.localidad_tienda,
                                    nombre_tienda = i_datos_parametros.nombre_tienda,
                                    cantidad_monedas = i_datos_parametros.monedas_tienda,
                                    img_seleccionado_para_ilimitado,
                                    titulo_seleccionado_para_ilimitado
                                )
                            }
                        }


                    }

                    is viewmodel_generaciones_IA.EstadoGeneracionesIA.Empty -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line("No se encontraron generaciones realizadas")
                        }
                    }

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
    nueva_generacion: nuevas_generaciones_con_IA,
    tituo_seleccionado: String,
    descrpcion_seleccionada: String,
    i: obt_item_gen_IA,
    usar_todas: (String, String, String, String, String, String, i: datos_generaciones_sin_publicaicones) -> Unit,
    usar_titulo_descripcion: (String, String, String, String, String, i: datos_generaciones_sin_publicaicones) -> Unit,
    whatsapp: (String, String, String) -> Unit,
    compartir: (String, String, String) -> Unit,
    mostrar_dialog_mejorar_vesiones: (String, String, String, String) -> Unit,
    usar_nueva_generacion_generada: (String, String, String, String) -> Unit,
    guardado_permanente: (id_gen: String, img: String, titulo: String) -> Unit,
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

    val hayAlgunaAccionDisponible =
        (tituloSeleccionado.isNotEmpty() && descripcionSeleccionada.isNotEmpty()) ||
                i.generacion_wsap.isNotEmpty() ||
                i.generacion_compartida.isNotEmpty()


    val diasRestantes = remember(i.vencimiento) {
        if (i.vencimiento == null) {
            "Guardado permanente"
        } else {
            constantes_datos_expirados_fechas_publicaciones
                .tiempoRestante(i.vencimiento)
        }
    }


    val (valorRestante, tipo) = parseDiasHorasRestantes(tiempo)
    val backgroundColor = when {
        tipo == "dias" -> when {
            valorRestante > 5 -> Color(0xFF13C218) // Verde
            valorRestante in 2..5 -> Color(0xFFCB7C03) // Naranja
            valorRestante == 1 -> Color(0xFFF11505) // Rojo
            else -> Color(0xFFFFE600)
        }

        tipo == "horas" -> when {
            valorRestante > 12 -> Color(0xFF15BB1A)
            valorRestante in 6..12 -> Color(0xFFFF9900)
            valorRestante in 1..5 -> Color(0xFFEC1707)
            else -> Color(0xFFFFE600)
        }

        else -> Color.Gray
    }
    val icono_promo_noti = if (i.tipo == "notificacion") {
        R.drawable.campana_3d_webp
    } else if (i.tipo == "publicacion") {
        R.drawable.promocio_iconn
    } else if (i.tipo == "generacion_publicacion_sin_pulicar") {
        R.drawable.logo_geinz_500x500
    } else {
        R.drawable.logo_geinz_500x500
    }

    val estadoUI = when {
        i.id_generacion.length == 9 -> EstadoUI(
            9,
            "Notificación (promoción seleccionada)",
            Color(0xFF1262C5)
        )

        i.id_generacion.length == 7 -> EstadoUI(
            7,
            "Notificación creada de 0 (promoción sin seleccionar)",
            Color(0xFF12B974)
        )

        i.id_generacion.length == 5 -> EstadoUI(
            5,
            "Notificación informativa",
            Color(0xFFC90000)
        )

        i.vencimiento == null -> EstadoUI(
            99,
            "Notificación permanente",
            Color(0xFFFFE300)
        )

        else -> EstadoUI(
            0,
            "Estado desconocido",
            Color.White
        )
    }




    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val anchoAnimado by animateDpAsState(
            targetValue = if (mostar_apartado_completo) LocalConfiguration.current.screenWidthDp.dp else 100.dp,
            animationSpec = tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing
            ),
            label = "anchoImagen"
        )

        val altoAnimado by animateDpAsState(
            targetValue = if (mostar_apartado_completo) 150.dp else 100.dp,
            animationSpec = tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing
            ),
            label = "altoImagen"
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    mostar_apartado_completo = !mostar_apartado_completo
                }) {
            Box() {

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
                        .width(anchoAnimado)
                        .height(altoAnimado)
                        .clip(RoundedCornerShape(5.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            mostar_apartado_completo = !mostar_apartado_completo
                        },
                    contentScale = ContentScale.Crop
                )
                if (
                    i.vencimiento != null &&
                    i.tipo != "generacion_publicacion_sin_pulicar" &&
                    i.tipo != "notificacion_sin_publicar"
                ) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .align(Alignment.BottomEnd)
                            .clickable {


                                guardado_permanente(i.id_generacion, i.img_, i.titulo_gen_IA)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.guardar_generacionia),
                            contentDescription = "Guardar permanente",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }


            }

            AnimatedVisibility(
                !mostar_apartado_completo,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.height(100.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(
                        start = 9.dp,
                        top = 5.dp,
                        bottom = 5.dp,
                        end = 10.dp
                    )
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejorar con IA",
                            tint = estadoUI.color, modifier = Modifier.size(20.dp)
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
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Image(
                            painter = painterResource(icono_promo_noti),
                            contentDescription = "",
                            modifier = Modifier
                                .requiredSize(22.dp)
                                .padding(start = 5.dp)
                        )
                    }

                }

            }
        }
        AnimatedVisibility(mostar_apartado_completo) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                spacer_vertical(5.dp)
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {

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
                        "Realizado : ${timestampAFechaLegible(i.inicio)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        texto_generico_one_line(
                            "Tipo : ${i.tipo.capitalizeFirst()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Image(
                            painter = painterResource(icono_promo_noti),
                            contentDescription = "",
                            modifier = Modifier.size(20.dp)
                        )
                    }


                    if (estadoUI.texto != "Estado desconocido" && estadoUI.texto != "Notificación permanente") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(50.dp)
                                )
                                .background(estadoUI.color)
                                .padding(10.dp)
                        ) {


                            texto_generico_one_line(
                                "Estado :",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            texto_generico_multilinea(
                                estadoUI.texto,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    spacer_vertical(5.dp)

                    texto_generico_one_line("Generacion de titulo y descripcion")


                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        items(listaConOriginal) { item ->

                            val original_tipo_ =
                                if (item.titulo == i.generacion_origini.titulo) "ORIGINAL" else item.tipo
                            val estaSeleccionado =
                                (item.titulo == tituloSeleccionado &&
                                        item.descripcion == descripcionSeleccionada)

                            generaciones_IA(
                                "generaciones",
                                titulo_select = tituloSeleccionado,
                                descripcion_selet = descripcionSeleccionada,
                                selecionado = estaSeleccionado,
                                width_auto = true,
                                titulo = item.titulo,
                                descripcion = item.descripcion,
                                tipo = original_tipo_,
                                seleccionado = { titulo, descripcion ->
                                    tituloSeleccionado = titulo
                                    descripcionSeleccionada = descripcion
                                }, {}, { titulo, texto, tipo ->
                                    mostrar_dialog_mejorar_vesiones(
                                        titulo,
                                        texto,
                                        i.tipo,
                                        i.id_generacion
                                    )
                                }
                            )
                        }
                    }

                    AnimatedVisibility(nueva_generacion.titulo_nuevo.isNotEmpty() && nueva_generacion.descripcion_nueva.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            spacer_vertical(10.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Mejorar con IA",
                                    tint = Color.Blue, modifier = Modifier.size(20.dp)
                                )
                                texto_generico_one_line("Nueva generacion")
                            }

                            texto_generico_one_line_Expandible(
                                "Realizado el : ${nueva_generacion.fecha_nueva_generacion}",
                                style = MaterialTheme.typography.bodySmall,
                                expandir = mostar_apartado_completo
                            )

                            TextoSubrayado(
                                "Marcar original",
                                MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    tituloSeleccionado = nueva_generacion.titulo_anterior
                                    descripcionSeleccionada = nueva_generacion.descripcion_anteriror
                                },
                                color_subrallado = MaterialTheme.colorScheme.primary
                            )


                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 5.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                texto_generico_multilinea(
                                    nueva_generacion.titulo_nuevo,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                texto_generico_multilinea(
                                    nueva_generacion.descripcion_nueva,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                BotonIA(
                                    texto = "Usar esta generación",
                                    onClick = {
                                        usar_nueva_generacion_generada(
                                            nueva_generacion.titulo_nuevo,
                                            nueva_generacion.descripcion_nueva, i.id_generacion,
                                            i.tipo,
                                        )
                                    },
                                    icono = IconoIA.Vector(Icons.Default.AutoAwesome)
                                )
                            }


                        }
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
                            }, { _, _, _ -> })

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
                            }, { _, _, _ -> })
                    }




                    if (hayAlgunaAccionDisponible) {

                        spacer_vertical(7.dp)

                        TresBotonesPrimarios(
                            usar_todas_bool =
                                tituloSeleccionado.isNotEmpty() &&
                                        descripcionSeleccionada.isNotEmpty() &&
                                        i.generacion_wsap.isNotEmpty() &&
                                        i.generacion_compartida.isNotEmpty(),

                            usarTodas = {
                                val datos_genearcion_anterior =
                                    datos_generaciones_sin_publicaicones(
                                        lista_obciones = i.lista_generaciones.mapNotNull { item ->
                                            val tipoEnum = try {
                                                repo_pantallas_promocionar.TipoGeneracionIA.valueOf(
                                                    item.tipo
                                                )
                                            } catch (e: IllegalArgumentException) {
                                                null
                                            }

                                            tipoEnum?.let {
                                                OpcionPromocionIA(
                                                    tipoIA = it,
                                                    titulo = item.titulo,
                                                    descripcion = item.descripcion
                                                )
                                            }
                                        },
                                        titulo_original = i.generacion_origini.titulo,
                                        descripcion_original = i.generacion_origini.descripcion,
                                        titulo_seleccionado = tituloSeleccionado,
                                        descripcion_seleccionada = descripcionSeleccionada
                                    )

                                usar_todas(
                                    tituloSeleccionado,
                                    descripcionSeleccionada,
                                    i.generacion_wsap,
                                    i.generacion_compartida,
                                    i.tipo,
                                    i.id_generacion,
                                    datos_genearcion_anterior
                                )
                            },

                            titulo_descripcion =
                                tituloSeleccionado.isNotEmpty() &&
                                        descripcionSeleccionada.isNotEmpty(),

                            usarTituloDescripcion = {
                                val datos_genearcion_anterior =
                                    datos_generaciones_sin_publicaicones(
                                        lista_obciones = i.lista_generaciones.mapNotNull { item ->
                                            val tipoEnum = try {
                                                repo_pantallas_promocionar.TipoGeneracionIA.valueOf(
                                                    item.tipo
                                                )
                                            } catch (e: IllegalArgumentException) {
                                                null
                                            }

                                            tipoEnum?.let {
                                                OpcionPromocionIA(
                                                    tipoIA = it,
                                                    titulo = item.titulo,
                                                    descripcion = item.descripcion
                                                )
                                            }
                                        },
                                        titulo_original = i.generacion_origini.titulo,
                                        descripcion_original = i.generacion_origini.descripcion,
                                        titulo_seleccionado = tituloSeleccionado,
                                        descripcion_seleccionada = descripcionSeleccionada
                                    )

                                usar_titulo_descripcion(
                                    tituloSeleccionado,
                                    descripcionSeleccionada,
                                    i.tipo,
                                    i.id_generacion,
                                    i.id_generacion,
                                    datos_genearcion_anterior
                                )
                            },

                            whattsap = i.generacion_wsap.isNotEmpty(),

                            usarWhatsapp = {
                                whatsapp(i.generacion_wsap, i.tipo, i.id_generacion)
                            },

                            compartir = i.generacion_compartida.isNotEmpty(),

                            usarCompartir = {
                                compartir(i.generacion_compartida, i.tipo, i.id_generacion)
                            }
                        )
                    }

                    spacer_vertical(5.dp)

                }


            }
        }
    }
}

//fun stringToTipoGeneracionIA(tipo: String): repo_pantallas_promocionar.TipoGeneracionIA? {
//    return try {
//        repo_pantallas_promocionar.TipoGeneracionIA.valueOf(tipo) // Esto convierte "VENTA" -> TipoGeneracionIA.VENTA
//    } catch (e: IllegalArgumentException) {
//        null // En caso no exista el enum
//    }
//}


@Composable
fun generaciones_IA(
    tipo_clikeable: String,
    titulo_select: String,
    descripcion_selet: String,
    selecionado: Boolean,
    width_auto: Boolean,
    titulo: String,
    descripcion: String,
    tipo: String,
    seleccionado: (titulo: String, descripcion: String) -> Unit,
    copiar_texto: () -> Unit,
    mostrar_dialog_mejorar_generacion: (String, String, String) -> Unit
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
                                mostrar_dialog_mejorar_generacion(
                                    titulo_finla,
                                    texto_final,
                                    tipo
                                )
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
    usarCompartir: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .horizontalScroll(rememberScrollState()),
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


@Composable
fun EstadoChip(
    estado: EstadoUI,
    seleccionado: Boolean,
    onSeleccion: (Int?) -> Unit
) {
    val backgroundColor =
        if (seleccionado) estado.color
        else estado.color.copy(alpha = 0.12f)

    val textColor =
        if (seleccionado) Color.White
        else estado.color

    val dotColor =
        if (seleccionado) Color.White
        else estado.color

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable {
            // 🔥 toggle
            onSeleccion(
                if (seleccionado) null else estado.valor
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = estado.texto,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                maxLines = 1
            )
        }
    }
}


//@Composable
//fun CloudSpeechSearchWithIA(
//    viewmodelGeneracionesIa: viewmodel_generaciones_IA,
//    size_lista: Int,
//    nombre_negocio: String,
//    resultadoIA_exports: (String?) -> Unit
//) {
//
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val buscar_por_nombre = remember { mutableStateOf("") }
//
//    var grabando by remember { mutableStateOf(false) }
//    var cargandoVoz by remember { mutableStateOf(false) }
//    var cargandoIA by remember { mutableStateOf(false) }
//
//    var audioData by remember { mutableStateOf(ByteArray(0)) }
//    var resultadoIA by remember { mutableStateOf<String?>(null) }
//
//    val micPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (!isGranted) {
//            Toast.makeText(context, "Se necesita permiso de micrófono", Toast.LENGTH_SHORT).show()
//        }
//    }
//    var mostrar_texto_por_IA by remember { mutableStateOf(false) }
//    var mensaje_de_la_IA by remember { mutableStateOf("") }
////
////    val tts = rememberTextToSpeech()
//
//}


@Composable
fun MySearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Buscar",
    placeholder: String = "Escribe o habla",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    textoError: String = "",
    maxLines: Int = 7 // máximo de líneas que puede crecer
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                texto_generico_multilinea(
                    label,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            placeholder = {
                texto_generico_multilinea(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            isError = isError,
            singleLine = false, // permite multilinea
            maxLines = maxLines,
            shape = RoundedCornerShape(16.dp), // bordes redondeados
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Borrar texto"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        if (isError && textoError.isNotEmpty()) {
            Text(
                text = textoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun JSONObject.optStringOrNull(key: String): String? {
    return if (isNull(key)) null else optString(key)
}




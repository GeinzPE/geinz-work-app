package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.agregar_promociones
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.fechas_promociones
import com.geinzz.geinzwork.data.model.img_contaier
import com.geinzz.geinzwork.data.model.informacion_container
import com.geinzz.geinzwork.data.model.items_pantallas_promociones
import com.geinzz.geinzwork.data.model.ubicacaion_container
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.DatePickerExample
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.DatePickerExample_promociones
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.calcularDiasEntreFechas
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.SelectorFotosReview
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.lanzarCrop
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.generarPromptOptimizado
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.opciones_localida
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_carga_ucrop_img
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.data.model.OpcionPromocionIA
import com.geinzz.geinzwork.data.model.datos_fecha_hora_tipo
import com.geinzz.geinzwork.data.model.datos_publicaciones_realizadas
import com.geinzz.geinzwork.data.model.fechas_horas_promociones
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.nombre_precio_notificaciones
import com.geinzz.geinzwork.data.model.obj_contador_notificaciones
import com.geinzz.geinzwork.data.model.obj_parametros_notificacion
import com.geinzz.geinzwork.data.model.obj_suspend_notificacion
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen_nueve
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown_precio_nombre_notificaciones
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown_select_params
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraFin
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerTimestampFecha
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerTimestampHoraFin
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerTimestampHoraInicio
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar
import com.geinzz.geinzwork.viewModels.viewmodel_recargas
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun pantalla_promocionar(
    viewmodel_pantalla_promocionar: viewmodel_pantallas_promocionar,
    viewmodel_socios: viewmodel_eres_socio,
    i: items_pantallas_promociones,
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val imagenes = remember { mutableStateListOf<ImagenReview>() }
    val viewmodel_recargas: viewmodel_recargas = viewModel()
    val monedas_tienda by viewmodel_recargas.saldo.collectAsState()
    val maxFotos = 5
    val maxFotos_notifi = 1
    var nombre_publicacion by rememberSaveable { mutableStateOf("") }
    var descripcion_publicacion by rememberSaveable { mutableStateOf("") }
    var version_nombre_publicacion_original by rememberSaveable { mutableStateOf("") }
    var descripcion_publicacion_original by rememberSaveable { mutableStateOf("") }
    var titulo_notificacion by rememberSaveable { mutableStateOf("") }
    var descripcion_notificacion by rememberSaveable { mutableStateOf("") }
    var id_publicacion_selecionada by rememberSaveable { mutableStateOf("") }
    var mostar_img_zoom by remember { mutableStateOf(false) }
    var imagenZoomSeleccionada by remember { mutableStateOf<String?>(null) }
    var contacto_directo by rememberSaveable { mutableStateOf(false) }
    var compartir by rememberSaveable { mutableStateOf(false) }
//    var ubicacion by rememberSaveable { mutableStateOf(false) }
//    var exclusivo by rememberSaveable { mutableStateOf(false) }
    var numero_publicaicon by rememberSaveable { mutableStateOf(i.numero_contacto_tienda) }
    var hora_escrita by remember { mutableStateOf("1") }
    var total_monedas_por_hora by rememberSaveable { mutableStateOf("") }
    var errorfecha by rememberSaveable { mutableStateOf(false) }
    var fecha_inicio by rememberSaveable { mutableStateOf("") }
    var fecha_fin by rememberSaveable { mutableStateOf("") }
    var diasEntre by remember { mutableStateOf<Int?>(null) }
    var dias_restantes_pr by remember { mutableStateOf(0) }
    val formato_notificacion_nombre_precio = listOf(
        nombre_precio_notificaciones("Basico", 5),
        nombre_precio_notificaciones("Avanzado", 15),
        nombre_precio_notificaciones("Primiun", 25)
    )
    val lista_tipo_promocion =
        listOf(nombre_precio_notificaciones("horas", 3), nombre_precio_notificaciones("dias", 30))
    var seleccion by remember { mutableStateOf(lista_tipo_promocion[0]) }

    val tipo_notificacion_precio_nombre = listOf(
        nombre_precio_notificaciones("informativas", 5),
        nombre_precio_notificaciones("promociones y ofertas", 10),
    )

    val prioridad_notificacion_precio_nombre = listOf(
        nombre_precio_notificaciones("high", 20),
        nombre_precio_notificaciones("normal", 10),
        nombre_precio_notificaciones("low", 5)
    )

    var precio_formato by remember { mutableStateOf(0) }
    var precio_tipo_notificacion by remember { mutableStateOf(0) }
    var precio_prioridad_notificacion by remember { mutableStateOf(0) }

    var prioridad_selec by remember { mutableStateOf("") }
    var tipo_notificacion_seleccionada by remember { mutableStateOf("") }
    var tipo_notificacion_params_seleccionada by remember { mutableStateOf("") }
    var opcionElegida by remember { mutableStateOf<OpcionPromocionIA?>(null) }
    var listaOpcionesIA by remember {
        mutableStateOf<List<OpcionPromocionIA>>(emptyList())
    }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")

    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""

    val cantidad_seguidores by viewmodel_socios.seguidores_obtenidos.collectAsState()

    val state_envio_notificaciones by viewmodel_pantalla_promocionar.estado_envio_notificaciones.collectAsState()
    val state_validacion_notificacion by viewmodel_pantalla_promocionar.estadoValidacion.collectAsState()
    val croppedUri = constantes_carga_ucrop_img.croppedUri

    val publicaicones_realizadas by viewmodel_socios.lista_publicaciones.collectAsState()
    var mostrar_btn_mejorar_IA by rememberSaveable { mutableStateOf(false) }

    val estado_textos_notificaciones_generadas by viewmodel_pantalla_promocionar.estado_promociones_ia.collectAsState()
    val estado_textos_notificacion_corta_generada by viewmodel_pantalla_promocionar.estado_notificaion_con_ia_corta.collectAsState()

    var monedas_costo_publicidad by remember { mutableStateOf("") }

    val state by viewmodel_socios.subidaPromoState.collectAsState()
    val scope = rememberCoroutineScope()

    var error_mostrado_numero_contacto by remember { mutableStateOf(false) }
    var error_horas_escritas by remember { mutableStateOf(false) }

    var error_titulo_publicacion by remember { mutableStateOf(false) }
    LaunchedEffect(titulo_notificacion, descripcion_notificacion) {
        if ((titulo_notificacion + descripcion_notificacion).length < 5) return@LaunchedEffect

        delay(1000)
        viewmodel_pantalla_promocionar
            .validarTexto(titulo_notificacion, descripcion_notificacion)
    }
    LaunchedEffect(i.id_tienda, i.localidad_tienda) {
        viewmodel_recargas.obtner_saldo_actual_reactivo(i.id_tienda, i.localidad_tienda)
    }

    LaunchedEffect(hora_escrita) {
        if (hora_escrita.isNotEmpty()) {
            if (hora_escrita.toInt() != 0) {
                monedas_costo_publicidad = cobroMonedas("horas", hora_escrita.toInt()).toString()
            }
        }
    }

    LaunchedEffect(estado_textos_notificaciones_generadas) {
        if (estado_textos_notificaciones_generadas is viewmodel_pantallas_promocionar.EstadoIA.Success) {
            listaOpcionesIA =
                (estado_textos_notificaciones_generadas as viewmodel_pantallas_promocionar.EstadoIA.Success).lista
        }
    }

    LaunchedEffect(estado_textos_notificacion_corta_generada) {
        if (estado_textos_notificacion_corta_generada is viewmodel_pantallas_promocionar.EstadoIA_notifi_corta.Success) {
            titulo_notificacion =
                (estado_textos_notificacion_corta_generada as viewmodel_pantallas_promocionar.EstadoIA_notifi_corta.Success).txt_descripcion.titulo
            descripcion_notificacion =
                (estado_textos_notificacion_corta_generada as viewmodel_pantallas_promocionar.EstadoIA_notifi_corta.Success).txt_descripcion.descripcion

        }
    }
    LaunchedEffect(state_envio_notificaciones) {
        if (state_envio_notificaciones.isNotEmpty()) {
            Toast.makeText(context, state_envio_notificaciones, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(i.id_tienda) {
        viewmodel_socios.obtener_lista_seguidores(i.localidad_tienda, i.id_tienda)
        viewmodel_socios.obtner_publicaciones_subidas(i.id_tienda, i.localidad_tienda)
    }
    LaunchedEffect(croppedUri) {
        croppedUri?.let { uriFinal ->

            // ✅ Buscar cuál imagen se estaba editando
            // Si quieres reemplazar la que estaba seleccionada
            val indexEditando = imagenes.indexOfFirst { it.isEditing } // ejemplo de flag

            if (indexEditando != -1) {
                // reemplazar la imagen editada
                imagenes[indexEditando] = imagenes[indexEditando].copy(
                    uri = uriFinal,
                    url = null,
                    isEditing = false
                )
            } else {
                // o agregar nueva si es upload directo
                imagenes.add(ImagenReview(uri = uriFinal, url = null))
            }

            // Limpiar el cropBus
            constantes_carga_ucrop_img.croppedUri = null
        }
    }


    LaunchedEffect(state) {
        when (state) {
            viewmodel_eres_socio.SubidaPromoState.Success -> {
                Toast
                    .makeText(
                        context,
                        "✅ Promoción subida correctamente",
                        Toast.LENGTH_SHORT
                    )
                    .show()
                viewmodel_pantalla_promocionar.cambiar_Estado_reciente(true)
                val historial = historial_descuento(
                    "descuento",
                    fecha = obtenerFechaActual(),
                    hora = obtenerHoraActual(),
                    id_recarga = viewmodel_recargas.generarIdRecarga(),
                    localidad_tienda = i.localidad_tienda,
                    id_tienda = i.id_tienda,
                    nombre_tienda = i.nombre_tienda,
                    monto_descuento = monedas_costo_publicidad,
                    tipo = if (seleccion.tipo.equals("horas")) "Publicidad por ${hora_escrita} horas" else "Publicidad por ${dias_restantes_pr} dias ",
                    precio_soles = viewmodel_recargas.calcular_precio_soles(monedas_costo_publicidad)
                        .toString(), estado = "Aceptado", monto_restante = monedas_tienda-monedas_costo_publicidad.toInt()
                )
                viewmodel_recargas.restar_puntos_recarga(
                    historial,
                    monedas_costo_publicidad,
                    i.id_tienda,
                    i.localidad_tienda
                )

            }

            is viewmodel_eres_socio.SubidaPromoState.Error -> {
                Toast
                    .makeText(
                        context,
                        (state as viewmodel_eres_socio.SubidaPromoState.Error).msg,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }

            else -> {}
        }

    }


    LaunchedEffect(fecha_inicio, fecha_fin) {
        Log.d("sdiagbofsahng", "$fecha_inicio $fecha_fin")
        if (fecha_inicio.isNotEmpty() && fecha_fin.isNotEmpty()) {
            diasEntre = calcularDiasEntreFechas(fecha_inicio, fecha_fin)
        } else {
            diasEntre = null
        }
    }

    val MIN_TITULO = 5
    val MAX_TITULO = 80

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        // Solo agregamos hasta el máximo permitido
        val nuevasImagenes = uris.take(maxFotos - imagenes.size).map { uri ->
            ImagenReview(
                uri = uri,
                url = null
            )
        }
        imagenes.addAll(nuevasImagenes)
    }
    val id_socio by data_store_localidad.get_id_socio(context).collectAsState(initial = "")
    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    var precio_por_notificacion_general by remember { mutableStateOf(0) }
    val botonHabilitado by derivedStateOf {
        val horas = hora_escrita.toLongOrNull() ?: 0L // si está vacío o inválido, lo considera 0
        nombre_publicacion.isNotEmpty() &&
                descripcion_publicacion.isNotEmpty() &&
                ((seleccion.tipo == "horas" && horas > 0L) ||
                        (seleccion.tipo == "dias" && fecha_fin.isNotEmpty()))
    }



    LaunchedEffect(precio_tipo_notificacion, precio_formato, precio_prioridad_notificacion) {
        precio_por_notificacion_general = viewmodel_pantalla_promocionar.calcularCostoNotificacion(
            cantidad_seguidores.size,
            precio_tipo_notificacion,
            precio_formato,
            precio_prioridad_notificacion
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    fontFamily = baners_geinz_work,
                    text = "Bienvenido a GEINZ ADS",
                    color = Color.White, fontSize = 25.sp
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    "Diseña promociones de forma fácil y rápida para tus clientes, o notifica a tus seguidores sobre ofertas exclusivas pensadas solo para ellos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    texto_generico_one_line("saldo ${i.saldo}")
                    Image(
                        painter = painterResource(R.drawable.icon_monedas_3d),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                spacer_vertical(10.dp)
            }

            item {
                texto_generico_multilinea(
                    "Agrega hasta 5 imágenes para destacar tu promoción.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                SelectorFotos(
                    imagenes = imagenes,
                    maxFotos = maxFotos,
                    onAddClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemove = { uri ->
                        imagenes.remove(uri)
                    }, mostar_zoom_img = { img ->
                        val imageModel = img.uri ?: img.url
                        mostar_img_zoom = true
                        imagenZoomSeleccionada =
                            img.uri?.toString() ?: img.url
                    }, editar_img = { img ->
                        when {
                            img.uri != null -> {

                                val index = imagenes.indexOf(img)
                                if (index != -1) {
                                    imagenes[index] =
                                        imagenes[index].copy(isEditing = true)
                                    lanzarCrop(img.uri, context)
                                }
                            }
                        }
                    }
                )
            }

            item {
                MyOutlinedTextField(
                    value = nombre_publicacion,
                    onValueChange = { input ->

                        if (input.length <= MAX_TITULO) {
                            nombre_publicacion = input
                            version_nombre_publicacion_original = input
                        }

                        error_titulo_publicacion =
                            nombre_publicacion.isNotEmpty() &&
                                    nombre_publicacion.length < MIN_TITULO
                    },
                    labelText = "Título de la publicación",
                    placeholderText = "Título de la publicación",
                    isError = error_titulo_publicacion,
                    texto_error = "El título debe tener al menos $MIN_TITULO caracteres"
                )
            }

            item {
                OutlinedTextField(
                    value = descripcion_publicacion,
                    onValueChange = {
                        descripcion_publicacion = it
                        descripcion_publicacion_original = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    label = { retornar_pleaceholder_label("descripción de publicación") },
                    placeholder = { retornar_pleaceholder_label("descripción de publicación") },
                    singleLine = false,
                    maxLines = 10,
                    minLines = 7,
                )


            }

            item {
                if (nombre_publicacion.isNotEmpty() && descripcion_publicacion.isNotEmpty()) {

                    val cargando =
                        estado_textos_notificaciones_generadas is viewmodel_pantallas_promocionar.EstadoIA.Loading

                    Button(
                        onClick = {
                            if (!cargando) {
                                viewmodel_pantalla_promocionar.mejorar_texto_con_promo_IA(
                                    monedas_tienda,
                                    localidad_tienda = i.localidad_tienda,
                                    id_tienda = i.id_tienda,
                                    nombre_tienda = i.nombre_tienda,
                                    tituloUsuario = nombre_publicacion,
                                    descripcionUsuario = descripcion_publicacion,
                                    nombreTienda = i.nombre_tienda,
                                    localidad = i.localidad_tienda,
                                    diasRestantes = dias_restantes_pr
                                )
                            }
                        },
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                texto_generico_one_line("Generando contenido…")
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                texto_generico_one_line("Mejorar con IA")
                                spacer_horizonta(5.dp)
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Mejorar con IA",
                                    tint = Color.White
                                )

                            }
                        }
                    }
                }
            }

            item {
                SelectorOpcionesPromocionIA(
                    OpcionPromocionIA(
                        version_nombre_publicacion_original,
                        descripcion_publicacion_original
                    ),
                    opciones = listaOpcionesIA
                ) { seleccion ->
                    nombre_publicacion = seleccion.titulo
                    descripcion_publicacion = seleccion.descripcion

                }
            }

            item {
                texto_generico_one_line("parametros de la publicacion")
                txt_publicaciones(
                    contacto_directo,
                    { it -> contacto_directo = it },
                    "contacto directo"
                )
                if (contacto_directo) {
                    MyOutlinedTextField(
                        value = numero_publicaicon,
                        onValueChange = { input ->


                            if (input.length <= 9 && input.all { it.isDigit() }) {
                                numero_publicaicon = input
                            }

                            error_mostrado_numero_contacto =
                                numero_publicaicon.isNotEmpty() && numero_publicaicon.length != 9
                        },
                        texto_error = "El número debe tener 9 dígitos",
                        isError = error_mostrado_numero_contacto,
                        labelText = "Número de contacto",
                        placeholderText = "Número de contacto",
                        keyboardType = KeyboardType.Number
                    )


                }
                txt_publicaciones(compartir, { it -> compartir = it }, "compartir")
//                txt_publicaciones(ubicacion, { it -> ubicacion = it }, "ubicacion")
//                txt_publicaciones(exclusivo, { it -> exclusivo = it }, "exclusivo")
            }

            item {
                texto_generico_one_line("Selecciona el plazo de tu publicacion")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(lista_tipo_promocion) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = item == seleccion,
                                onClick = {
                                    seleccion = item
                                    monedas_costo_publicidad = ""
                                    dias_restantes_pr = 0
                                    hora_escrita = "0"
                                    fecha_inicio = ""
                                    fecha_fin = ""
                                }
                            )
                            Text(text = "${item.tipo}")
                        }
                    }
                }
                if (seleccion.tipo.equals("horas")) {
                    texto_generico_one_line("indica las horas que este tu publicacion activa")
                    Row() {
                        texto_generico_one_line("costo por hora 3 monedas")
                    }
                    texto_generico_one_line("fecha de inicio ${obtenerFechaActual()}")
                    texto_generico_one_line("fecha de fin ${obtenerFechaActual()}")
                    MyOutlinedTextField(
                        value = hora_escrita,
                        onValueChange = { input ->
                            // Limitar input a solo números
                            var sanitizedInput = input.filter { it.isDigit() }

                            // Limitar máximo 3 caracteres
                            if (sanitizedInput.length > 3) {
                                sanitizedInput = sanitizedInput.take(3)
                            }

                            hora_escrita = sanitizedInput

                            // Convertir a Int seguro
                            val numero = sanitizedInput.toIntOrNull() ?: 0
                            error_horas_escritas = numero <= 0
                        },
                        texto_error = "El 0 no está permitido",
                        isError = error_horas_escritas,
                        labelText = "Ingresa las horas de tu publicación",
                        placeholderText = "Ingresa las horas de tu publicación",
                        keyboardType = KeyboardType.Number
                    )


                    if (hora_escrita.isNotEmpty()) {
                        texto_generico_multilinea("Total de monedas por $hora_escrita h =$monedas_costo_publicidad")
                    }


                } else if (seleccion.tipo.equals("dias")) {
                    texto_generico_one_line("indica los dias")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DateButton(
                                titulo = "Inicio",
                                error_fecha = errorfecha,
                                campo_error = "El campo es obligatorio",
                                selectedDate = fecha_inicio,
                                fecha = { fecha_obtenida ->
                                    fecha_inicio = obtenerFechaActual()
                                })
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            DateButton(
                                titulo = "Fin",
                                error_fecha = errorfecha,
                                campo_error = "El campo es obligatorio", selectedDate = fecha_fin,
                                fecha = { fecha_obtenida ->
                                    fecha_fin = fecha_obtenida

                                })
                        }
                    }
                    diasEntre?.let { dias ->
                        val texto = if (dias == 0) {
                            "el minimo de dias es 1"
                        } else {
                            "Duración: $dias días"
                        }
                        val color =
                            if (dias == 0) {
                                Color.Red
                            } else {
                                Color.Gray
                            }
                        Text(
                            text = texto,
                            fontSize = 14.sp,
                            color = color
                        )
                        dias_restantes_pr = dias

                    }
                    if (dias_restantes_pr != 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            monedas_costo_publicidad =
                                cobroMonedas("dias", dias_restantes_pr).toString()
                            texto_generico_one_line(monedas_costo_publicidad)
                            Image(
                                painter = painterResource(R.drawable.icon_monedas_3d),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }

            }

            item {
                val numero_campo = if (contacto_directo) {
                    i.numero_contacto_tienda
                } else {
                    ""
                }

                if (botonHabilitado) {
                    Button(onClick = {
                        val datos_publicacion = agregar_promociones(
                            formato_fecha_hora = seleccion.tipo,
                            exclusivo = false,
                            img_container = img_contaier(),
                            informacion = informacion_container(
                                categoria = i.categoira_tienda,
                                descripcion = descripcion_publicacion,
                                id_promocion = generarIdFirebase(),
                                id_tienda = id_socio,
                                nombre_tienda = i.nombre_tienda,
                                titulo = nombre_publicacion,
                                numero = numero_campo,
                                compartir = compartir,
                                contactar = contacto_directo
                            ),
                            ubicacion = ubicacaion_container(),
                            datos_hora_fecha = datos_fecha_hora_tipo(
                                horas = fechas_horas_promociones(
                                    hora_inicio = if (seleccion.tipo == "horas") obtenerHoraActual() else "",
                                    hora_fin = if (seleccion.tipo == "horas") obtenerHoraFin(
                                        hora_escrita.toInt()
                                    ) else "",
                                    activo = if (seleccion.tipo == "horas") true else false,
                                    timestamp_inicio = if (seleccion.tipo == "horas") obtenerTimestampHoraInicio() else 0L,
                                    timestamp_fin = if (seleccion.tipo == "horas") obtenerTimestampHoraFin(
                                        hora_escrita.toInt()
                                    ) else 0L
                                ),
                                dias = fechas_promociones(
                                    fecha_inicio = if (seleccion.tipo == "dias") fecha_inicio else "",
                                    fecha_fin = if (seleccion.tipo == "dias") fecha_fin else "",
                                    activo = if (seleccion.tipo == "dias") true else false,
                                    timestamp_inicio = if (seleccion.tipo == "dias") obtenerTimestampFecha(
                                        fecha_inicio
                                    ) else 0L,
                                    timestamp_fin = if (seleccion.tipo == "dias") obtenerTimestampFecha(
                                        fecha_fin
                                    ) else 0L
                                )
                            ),
                        )
                        viewmodel_socios.crear_promociones(
                            datos_publicacion,
                            localidad = i.localidad_tienda
                        )
                        viewmodel_socios.subir_img_firestore_promociones(
                            img_tienda = i.img_tienda,
                            localidad = i.localidad_tienda,
                            context = context,
                            imagenes = imagenes,
                            idSocio = id_socio,
                            idPromo = datos_publicacion.informacion.id_promocion
                        )

                    }) {
                        texto_generico_one_line("guardar")
                    }
                }
            }

            item {
                spacer_vertical(50.dp)
                texto_generico_one_line("Notifica a tus seguidores")
                texto_generico_multilinea(
                    "Envía notificaciones sobre tus promociones y novedades para que tus seguidores sean los primeros en enterarse."
                )
                texto_generico_one_line("Cantidad de seguidores ${cantidad_seguidores.size}")
            }


            if (cantidad_seguidores.size >= 10) {
                item {
                    texto_generico_one_line("Notifica tus publicaciones subidas")

                    if (publicaicones_realizadas.isNotEmpty()) {
                        LazyRow() {
                            items(publicaicones_realizadas) { i ->
                                Log.d("pulbiaicaones", publicaicones_realizadas.size.toString())
                                item_publicaiones_realizadas(i) { titulo, descripcion, id ->
                                    titulo_notificacion = titulo
                                    descripcion_notificacion = descripcion

                                    val tipo = "promociones y ofertas"
                                    val precio = tipo_notificacion_precio_nombre
                                        .firstOrNull { it.tipo == tipo }
                                        ?.precio ?: 0

                                    tipo_notificacion_params_seleccionada = tipo
                                    precio_tipo_notificacion = precio

                                    mostrar_btn_mejorar_IA = true
                                    id_publicacion_selecionada = id
                                }

                            }
                        }

                    }

                }
                item {
                    MyOutlinedTextField(
                        value = titulo_notificacion,
                        onValueChange = {
                            titulo_notificacion = it
                        },
                        labelText = "Titulo de notificacion",
                        placeholderText = "Titulo de notificacion"
                    )

                    OutlinedTextField(
                        value = descripcion_notificacion,
                        onValueChange = {
                            descripcion_notificacion = it

                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        label = { retornar_pleaceholder_label("texto de notificacion") },
                        placeholder = { retornar_pleaceholder_label("texto de notificacion") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = false,
                        maxLines = 2,
                        minLines = 1,
                        isError = false,
                        supportingText = {

                        }
                    )
                    AnimatedVisibility(
                        tipo_notificacion_seleccionada.equals("Primiun"), modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        SelectorFotos(
                            imagenes = imagenes,
                            maxFotos = maxFotos_notifi,
                            onAddClick = {
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemove = { uri ->
                                imagenes.remove(uri)
                            }, mostar_zoom_img = { img ->
                                val imageModel = img.uri ?: img.url
                                mostar_img_zoom = true
                                imagenZoomSeleccionada =
                                    img.uri?.toString() ?: img.url
                            }, editar_img = { img ->
                                when {
                                    img.uri != null -> {

                                        val index = imagenes.indexOf(img)
                                        if (index != -1) {
                                            imagenes[index] =
                                                imagenes[index].copy(isEditing = true)
                                            lanzarCrop(img.uri, context)
                                        }
                                    }
                                }
                            }
                        )
                    }

                    ExpandDropDown_precio_nombre_notificaciones(
                        prioridad_notificacion_precio_nombre,
                        false,
                        "selecciona tu prioridad",
                        "selecciona tu prioridad"
                    ) { prioridad, precio ->
                        prioridad_selec = prioridad
                        precio_prioridad_notificacion = precio

                    }


                    ExpandDropDown_precio_nombre_notificaciones(
                        formato_notificacion_nombre_precio,
                        false,
                        "selecciona tu formato de notificacion",
                        "selecciona tu formato de notificacion"
                    ) { plan, precio ->
                        tipo_notificacion_seleccionada = plan
                        precio_formato = precio
                    }

                    ExpandDropDown_select_params(
                        tipo_notificacion_params_seleccionada,
                        tipo_notificacion_precio_nombre,
                        false,
                        "selecciona tu tipo de notificacion",
                        "selecciona tu tipo de notificacion"
                    ) { tipo, precio ->
                        Log.d("precioestableico", "$precio")
                        tipo_notificacion_params_seleccionada = tipo
                        precio_tipo_notificacion = precio
                    }
                    texto_generico_one_line("precio por notificaicon $precio_por_notificacion_general")

                    if (mostrar_btn_mejorar_IA) {
                        val cargando =
                            estado_textos_notificacion_corta_generada is viewmodel_pantallas_promocionar.EstadoIA_notifi_corta.Loading

                        Button(
                            onClick = {
                                if (!cargando) {
                                    viewmodel_pantalla_promocionar.mejorar_mejorar_notificacion_con_IA_corta(
                                        monedas_tienda,
                                        localidad_tienda = i.localidad_tienda,
                                        id_tienda = i.id_tienda,
                                        nombre_tienda = i.nombre_tienda,
                                        titulo_publicacion = titulo_notificacion,
                                        descripcion = descripcion_notificacion
                                    )
                                }
                            },
                            enabled = !cargando
                        ) {
                            if (cargando) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    texto_generico_one_line("Generando contenido…")
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    texto_generico_one_line("Mejorar con IA")
                                    spacer_horizonta(5.dp)
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Mejorar con IA",
                                        tint = Color.White
                                    )

                                }
                            }
                        }
                    }

                    if (tipo_notificacion_seleccionada.isNotEmpty()) {
                        val texto = when (tipo_notificacion_seleccionada) {
                            "Basico" -> {
                                "Se mostrará únicamente el título, la descripción y el logo de tu negocio."
                            }

                            "Avanzado" -> {
                                "Se mostrará el título, la descripción y una imagen promocional."
                            }

                            "Primiun" -> {
                                "Se mostrará el título, la descripción, una imagen promocional y el logo de tu negocio."
                            }

                            else -> {
                                ""
                            }
                        }

                        if (texto.isNotEmpty()) {
                            texto_generico_multilinea(texto)
                        }
                    }

                    when (state_validacion_notificacion) {
                        is viewmodel_pantallas_promocionar.EstadoValidacionNotificacion.Idle -> {}
                        is viewmodel_pantallas_promocionar.EstadoValidacionNotificacion.Permitida -> {

                            Button(onClick = {
                                scope.launch {
                                    enviar_notificacion_lista_dispo(
                                        "",
                                        id_tienda = "",
                                        localidad = "",
                                        categora_tienda = "",
                                        "",
                                        id_users = listOf(id_user),
                                        titulo = titulo_notificacion,
                                        txt = descripcion_notificacion,
                                        logo_tienda = i.img_tienda,
                                        tipo_notificacion = tipo_notificacion_seleccionada,
                                        url_img = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/walpaper_geinz%2Fturisticos%2Fimg11.webp?alt=media&token=1151dd65-8a6b-497d-a452-a8d948859422",
                                        prioridad = prioridad_selec
                                    )
                                }
                            }) {
                                texto_generico_one_line("Ver vista previa de notificación")
                            }

                            Button(onClick = {
                                val id_creado_tipo =
                                    if (id_publicacion_selecionada.isNotEmpty()) generarIdImagen_nueve() else generarIdImagen()

                                val obj = obj_contador_notificaciones(
                                    id_tienda = id_socio,
                                    localida = i.localidad_tienda,
                                    categoria = i.categoira_tienda,
                                    idnotificacion = id_creado_tipo,
                                    fecha_enviada = obtenerFechaActual(),
                                    precio_envio = 50,
                                    parametros_notificacion = obj_parametros_notificacion(
                                        titulo_notificacion = titulo_notificacion,
                                        texto_notificacion = descripcion_notificacion,
                                        logo_notificacion = i.img_tienda,
                                        img_notifiacion = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/walpaper_geinz%2Fturisticos%2Fimg11.webp?alt=media&token=1151dd65-8a6b-497d-a452-a8d948859422",
                                        priorida_notificacion = prioridad_selec,
                                        tipo_notificacion = tipo_notificacion_seleccionada,
                                        notificacion_publicidad = id_publicacion_selecionada.isEmpty(),
                                        id_publicacion_anuncio = id_publicacion_selecionada
                                    ),
                                    suspendido = obj_suspend_notificacion(),
                                    tipo_notificacion = tipo_notificacion_params_seleccionada,
                                    i.nombre_tienda,
                                    i.numero_contacto_tienda,
                                    i.categoira_tienda
                                )
                                viewmodel_pantalla_promocionar.enviar_notificacion(
                                    monedas_tienda,
                                    i.localidad_tienda,
                                    i.nombre_tienda,
                                    i.id_tienda,
                                    precio_por_notificacion_general.toString(),
                                    cantidad_seguidores,
                                    obj
                                )
                            }) {
                                texto_generico_one_line("notificar a tus ${cantidad_seguidores.size}seguidores")
                            }
                        }

                        is viewmodel_pantallas_promocionar.EstadoValidacionNotificacion.Bloqueada -> {
                            Text(
                                text = (state_validacion_notificacion as viewmodel_pantallas_promocionar.EstadoValidacionNotificacion.Bloqueada).mensaje,
                                color = Color.Red
                            )
                        }

                    }

                }
            } else {
                item {
                    texto_generico_one_line("te falta cumplir los requisitos para notificar a tus seguidores")
                }
            }

            item {
                spacer_vertical(30.dp)
            }


        }
        if (mostar_img_zoom) {
            imagenZoomSeleccionada?.let { imagenString ->
                ZoomableGalleryFullScreen(
                    compartir_promocion(),
                    imagenes = listOf(imagenString), // 👈 List<String>
                    startIndex = 0,
                    onDismiss = { mostar_img_zoom = false }
                )
            }
        }
        AnimatedVisibility(
            state == viewmodel_eres_socio.SubidaPromoState.Loading,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                texto_generico_one_line("subieindo promo")
                CircularProgressIndicator()
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

fun generarIdFirebase(): String {
    return FirebaseFirestore.getInstance()
        .collection("temp")
        .document()
        .id
}


@Composable
fun txt_publicaciones(valor: Boolean, retorno: (Boolean) -> Unit, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        texto_generico_one_line(titulo, modifier = Modifier.weight(1f))
        Switch(
            checked = valor,
            onCheckedChange = {
                retorno(it)
            }, colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SelectorFotos(
    imagenes: SnapshotStateList<ImagenReview>,
    maxFotos: Int,
    onAddClick: () -> Unit,
    onRemove: (ImagenReview) -> Unit,
    mostar_zoom_img: (ImagenReview) -> Unit,
    editar_img: (ImagenReview) -> Unit
) {
    var remover by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(
            items = imagenes,
            key = { it.uri?.toString() ?: it.url ?: "" }
        ) { img ->

            val visibleState = remember {
                MutableTransitionState(true)
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.95f),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f)
            ) {

                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {

                    AsyncImage(
                        model = img.uri ?: img.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.cargando_img_categorias),
                        error = painterResource(R.drawable.cargando_img_categorias)
                    )

                    // ❌ ELIMINAR (SUAVE)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(25.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .padding(6.dp)
                            .clickable {
                                visibleState.targetState = false
                                scope.launch {
                                    delay(200)
                                    onRemove(img)
                                }

                            }
                    )

                    // 🔍 VER
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .size(25.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .padding(6.dp)
                            .clickable {
                                mostar_zoom_img(img)
                            }
                    )
                    if (img.uri != null) {
                        // ✏ EDITAR
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(25.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .padding(6.dp)
                                .clickable {
                                    editar_img(img)
                                }
                        )
                    }
                }
            }
        }

        // ➕ AGREGAR FOTO
        if (imagenes.size < maxFotos) {
            item(key = "camera") {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateButton(
    titulo: String,
    error_fecha: Boolean,
    campo_error: String,
    selectedDate: String,       // Valor desde afuera
    fecha: (String) -> Unit     // Callback para actualizar
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // 📌 Si es "Inicio" y no hay fecha, fijar hoy automáticamente
    LaunchedEffect(titulo, selectedDate) {
        if (titulo.lowercase() == "inicio" && selectedDate.isEmpty()) {
            val hoy = LocalDate.now()
            fecha(hoy.format(formatter))
        }
    }

    // 📅 DatePicker solo se usa cuando NO es "Inicio"
    if (titulo.lowercase() != "inicio") {
        DatePickerExample_promociones(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onDateSelected = { fechaSeleccionada ->
                fecha(fechaSeleccionada.format(formatter)) // Actualiza desde afuera
            }
        )
    }

    Column {
        OutlinedTextField(
            value = selectedDate, // Solo usamos el parámetro
            onValueChange = {},
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            placeholder = { Text(titulo) },
            singleLine = true,
            readOnly = true,
            enabled = true,
            leadingIcon = {
                IconButton(
                    onClick = {
                        if (titulo.lowercase() != "inicio") {
                            showDialog = true
                        }
                    },
                    enabled = titulo.lowercase() != "inicio"
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        tint = if (titulo.lowercase() == "inicio") Color.Gray
                        else MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = if (error_fecha) Color.Red else MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (error_fecha) Color.Red else MaterialTheme.colorScheme.primary
            ),
            isError = error_fecha,
            shape = RoundedCornerShape(50)
        )

        AnimatedVisibility(error_fecha) {
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                retornar_pleaceholder_label(campo_error, Color.Red)
            }
        }
    }
}


fun acortarDescripcionNotificacion(
    textoLargo: String,
    maxCaracteres: Int = 50
): String {
    val limpio = textoLargo
        .replace("\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    return if (limpio.length <= maxCaracteres) {
        limpio
    } else {
        limpio.substring(0, maxCaracteres).trimEnd() + "…"
    }
}


//suspend fun generarPromocionesConIA(
//    tituloUsuario: String,
//    descripcionUsuario: String,
//    nombreTienda: String,
//    localidad: String,
//    diasRestantes: Int
//): List<OpcionPromocionIA> {
//
//    return try {
//        val model = Firebase.ai(
//            backend = GenerativeBackend.googleAI()
//        ).generativeModel("gemini-2.5-flash")
//
//        val prompt = generarPromptPromocionProduccion(
//            tituloUsuario = tituloUsuario,
//            descripcionUsuario = descripcionUsuario,
//            nombreTienda = nombreTienda,
//            localidad = localidad,
//            diasRestantes = diasRestantes
//        )
//
//        val result = model.generateContent(prompt)
//        val texto = result.text ?: return emptyList()
//
//        parsearOpcionesIA(texto)
//
//    } catch (e: Exception) {
//        Log.e("IA", "Error IA promociones: ${e.message}")
//        emptyList()
//    }
//}
//


@Composable
fun SelectorOpcionesPromocionIA(
    original: OpcionPromocionIA,
    opciones: List<OpcionPromocionIA>,
    onOpcionSeleccionada: (OpcionPromocionIA) -> Unit
) {
    // 🚫 No mostrar nada hasta que la IA termine
    if (opciones.isEmpty()) return

    var seleccionIndex by rememberSaveable { mutableIntStateOf(0) }

    // ✅ Ahora sí: original + IA
    val listaFinal = remember(original, opciones) {
        listOf(original) + opciones
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(listaFinal) { index, opcion ->

            val esOriginal = index == 0
            val seleccionado = seleccionIndex == index

            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(250.dp)
                    .clickable {
                        seleccionIndex = index
                        onOpcionSeleccionada(opcion)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        seleccionado -> MaterialTheme.colorScheme.primaryContainer
                        esOriginal -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (seleccionado)
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                else null
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    Text(
                        text = if (esOriginal) "VERSIÓN ORIGINAL" else "SUGERENCIA IA",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esOriginal)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = opcion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = opcion.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


fun cobroMonedas(tipo: String, dias_horas: Int): Int {
    Log.d("hrgdfjbvsdfigbisd", "$tipo $dias_horas")
    return when (tipo.lowercase()) {
        "dias" -> dias_horas * 30
        "horas" -> dias_horas * 3
        else -> throw IllegalArgumentException("Tipo inválido: $tipo")
    }
}


@Composable
fun item_publicaiones_realizadas(
    i: datos_publicaciones_realizadas,
    clikeado: (String, String, String) -> Unit
) {
    Column() {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(i.img)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias).build(),
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
                .width(150.dp)
                .clickable { clikeado(i.titulo, i.descripcion, i.id) },
            contentScale = ContentScale.Crop
        )

        texto_generico_one_line("publicado el ${i.fecha_publicado}")
        texto_generico_one_line(
            if (i.activo) {
                "Activo"
            } else {
                "vencido"
            }
        )
    }
}





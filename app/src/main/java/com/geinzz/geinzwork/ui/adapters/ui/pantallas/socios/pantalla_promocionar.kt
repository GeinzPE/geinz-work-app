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
import androidx.compose.animation.core.MutableTransitionState
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
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.data.model.datos_publicaciones_realizadas
import com.geinzz.geinzwork.data.model.obj_contador_notificaciones
import com.geinzz.geinzwork.data.model.obj_parametros_notificacion
import com.geinzz.geinzwork.data.model.obj_suspend_notificacion
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen_nueve
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown_select_params
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.google.firebase.auth.FirebaseAuth


data class NotificacionIA(
    val titulo: String,
    val descripcion: String
)

data class OpcionPromocionIA(
    val titulo: String,
    val descripcion: String
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun pantalla_promocionar(
    viewmodel_socios: viewmodel_eres_socio,
    i: items_pantallas_promociones
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val imagenes = remember { mutableStateListOf<ImagenReview>() }
    val maxFotos = 5
    val maxFotos_notifi = 1
    var nombre_publicacion by rememberSaveable { mutableStateOf("") }
    var descripcion_publicacion by rememberSaveable { mutableStateOf("") }
    var version_nombre_publicacion_original by rememberSaveable { mutableStateOf("") }
    var descripcion_publicacion_original by rememberSaveable { mutableStateOf("") }
    var titulo_notificacion by rememberSaveable { mutableStateOf("") }
    var descripcion_notificacion by rememberSaveable { mutableStateOf("") }
    var id_publicacion_selecionada by rememberSaveable { mutableStateOf("")}
    var mostar_img_zoom by remember { mutableStateOf(false) }
    var imagenZoomSeleccionada by remember { mutableStateOf<String?>(null) }
    var contacto_directo by rememberSaveable { mutableStateOf(false) }
    var compartir by rememberSaveable { mutableStateOf(false) }
    var ubicacion by rememberSaveable { mutableStateOf(false) }
    var exclusivo by rememberSaveable { mutableStateOf(false) }
    var numero_publicaicon by rememberSaveable { mutableStateOf(i.numero_contacto_tienda) }
    var errorfecha by rememberSaveable { mutableStateOf(false) }
    var fecha_inicio by rememberSaveable { mutableStateOf("") }
    var fecha_fin by rememberSaveable { mutableStateOf("") }
    var diasEntre by remember { mutableStateOf<Int?>(null) }
    var dias_restantes_pr by remember { mutableStateOf(0) }
    val prioridad = listOf("high", "normal", "low")
    val tipo_notificacion = listOf("Basico", "Avanzado", "Primiun")
    val tipo_notificacion_params = listOf("informativas", "promociones y ofertas")
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

    val state_envio_notificaciones by viewmodel_socios.estado_envio_notificaciones.collectAsState()
    var resultado_validacion_notificacion by remember {
        mutableStateOf(
            ResultadoValidacion(
                estado = EstadoNotificacion.PERMITIDA,
                mensaje = ""
            )
        )
    }
    val croppedUri = constantes_carga_ucrop_img.croppedUri

    val publicaicones_realizadas by viewmodel_socios.lista_publicaciones.collectAsState()
    var mostrar_btn_mejorar_IA by rememberSaveable { mutableStateOf(false) }


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
    val state by viewmodel_socios.subidaPromoState.collectAsState()
    val scope = rememberCoroutineScope()

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
        if (fecha_inicio.isNotEmpty() && fecha_fin.isNotEmpty()) {
            diasEntre = calcularDiasEntreFechas(fecha_inicio, fecha_fin)
        } else {
            diasEntre = null
        }
    }

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
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                texto_generico_one_line(
                    "Crea ofertas y promociones",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            item {
                texto_generico_multilinea(
                    "Diseña promociones de forma fácil y rápida para tus clientes, o notifica a tus seguidores sobre ofertas exclusivas pensadas solo para ellos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    texto_generico_one_line("saldo ${i.saldo}")
                    Image(
                        painter = painterResource(R.drawable.icon_monedas_3d),
                        contentDescription = null,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

            item {
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
                    onValueChange = {
                        nombre_publicacion = it
                        version_nombre_publicacion_original = it
                    },
                    labelText = "Titulo de la publicaion",
                    placeholderText = "Titulo de la publicaion"
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
                    label = { retornar_pleaceholder_label("descripcion de publicacion") },
                    placeholder = { retornar_pleaceholder_label("descripcion de publicacion") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = false,
                    maxLines = 10,
                    minLines = 7,
                    isError = false,
                    supportingText = {

                    }
                )
            }
            item {
                if (nombre_publicacion.isNotEmpty() && descripcion_publicacion.isNotEmpty()) {
                    Button(onClick = {
                        scope.launch {
                            val listaOpciones = generarPromocionesConIA(
                                nombre_publicacion,
                                descripcion_publicacion,
                                i.nombre_tienda,
                                i.localidad_tienda,
                                dias_restantes_pr
                            )
                            listaOpcionesIA = listaOpciones
                        }
                    }) {
                        texto_generico_one_line("mejorar texto con IA")
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
                        onValueChange = { numero_publicaicon = it },
                        labelText = "Numero de contacto",
                        placeholderText = "Numero de contacto"
                    )
                }
                txt_publicaciones(compartir, { it -> compartir = it }, "compartir")
                txt_publicaciones(ubicacion, { it -> ubicacion = it }, "ubicacion")
                txt_publicaciones(exclusivo, { it -> exclusivo = it }, "exclusivo")
            }

            item {
                texto_generico_one_line("indica caundo quieres finalizar tu promocion")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DateButton(
                            "Inicio",
                            errorfecha,
                            "El campo es obligatorio",
                            { fecha_obtenida ->
                                fecha_inicio = fecha_obtenida
                            })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        DateButton("Fin", errorfecha, "El campo es obligatorio", { fecha_obtenida ->
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
                        texto_generico_one_line(cobroMonedas(dias_restantes_pr).toString())
                        Image(
                            painter = painterResource(R.drawable.icon_monedas_3d),
                            contentDescription = null,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }


            }

            item {
                val ubicontainer = if (ubicacion) {
                    i.ubicacion
                } else {
                    ubicacaion_container()
                }
                val numero_campo = if (contacto_directo) {
                    i.numero_contacto_tienda
                } else {
                    ""
                }
                val datos_publicacion = agregar_promociones(
                    exclusivo = exclusivo,
                    fechas = fechas_promociones(
                        inicio = fecha_inicio,
                        fin = fecha_fin,
                        activo = true
                    ),
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
                    ubicacion = ubicontainer
                )
                if (nombre_publicacion.isNotEmpty() && descripcion_publicacion.isNotEmpty()) {
                    Button(onClick = {
                        viewmodel_socios.crear_promociones(
                            datos_publicacion,
                            localidad = i.localidad_tienda
                        )
                        viewmodel_socios.subir_img_firestore_promociones(
                            i.img_tienda,
                            i.localidad_tienda,
                            context,
                            imagenes,
                            id_socio,
                            datos_publicacion.informacion.id_promocion
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


            if (cantidad_seguidores.size == 10) {

                item {
                    texto_generico_one_line("Notifica tus publicaciones subidas")

                    if (publicaicones_realizadas.isNotEmpty()) {
                        LazyRow() {
                            items(publicaicones_realizadas) { i ->
                                item_publicaiones_realizadas(i) { titutlo, descripcion,id ->
                                    titulo_notificacion = titutlo
                                    descripcion_notificacion = descripcion
                                    tipo_notificacion_params_seleccionada = "promociones y ofertas"
                                    mostrar_btn_mejorar_IA = true
                                    id_publicacion_selecionada =id
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
                            resultado_validacion_notificacion = ResultadoValidacion(
                                estado = EstadoNotificacion.PERMITIDA,
                                mensaje = "",
                                palabraDetectada = null
                            )
                        },
                        labelText = "Titulo de notificacion",
                        placeholderText = "Titulo de notificacion"
                    )

                    OutlinedTextField(
                        value = descripcion_notificacion,
                        onValueChange = {
                            descripcion_notificacion = it
                            resultado_validacion_notificacion = ResultadoValidacion(
                                estado = EstadoNotificacion.PERMITIDA,
                                mensaje = "",
                                palabraDetectada = null
                            )
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

                    ExpandDropDown(
                        prioridad,
                        false,
                        "selecciona tu prioridad",
                        "selecciona tu prioridad"
                    ) { prioridad ->
                        prioridad_selec = prioridad
                    }


                    ExpandDropDown(
                        tipo_notificacion,
                        false,
                        "selecciona tu plan de notificacion",
                        "selecciona tu plan de notificacion"
                    ) { plan ->
                        tipo_notificacion_seleccionada = plan
                    }

                    ExpandDropDown_select_params(
                        tipo_notificacion_params_seleccionada,
                        tipo_notificacion_params,
                        false,
                        "selecciona tu tipo de notificacion",
                        "selecciona tu tipo de notificacion"
                    ) { tipo ->
                        tipo_notificacion_params_seleccionada = tipo
                    }

                    if (mostrar_btn_mejorar_IA) {
                        Button(onClick = {
                            crear_notificacion_conIA_corta(
                                scope,
                                titulo_notificacion,
                                acortarDescripcionNotificacion(descripcion_notificacion)
                            ) { NotificacionIA ->
                                titulo_notificacion=NotificacionIA.titulo
                                descripcion_notificacion=NotificacionIA.descripcion
                            }

                        }) {
                            texto_generico_one_line("mejorar contenido  con IA")
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
                    if (tipo_notificacion_seleccionada.isNotEmpty() &&
                        titulo_notificacion.isNotEmpty() &&
                        descripcion_notificacion.isNotEmpty()
                    ) {

                        Button(onClick = {
                            // Validar de nuevo sin resetear el estado inmediatamente
                            resultado_validacion_notificacion =
                                validarNotificacion(titulo_notificacion, descripcion_notificacion)
                        }) {
                            texto_generico_one_line("Realizar validación de notificación")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (resultado_validacion_notificacion.estado) {

                            EstadoNotificacion.PERMITIDA -> {
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
                                    val id_creado_tipo = if(id_publicacion_selecionada.isNotEmpty()) generarIdImagen_nueve() else      generarIdImagen()


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
                                        tipo_notificacion = tipo_notificacion_params_seleccionada,i.nombre_tienda,i.numero_contacto_tienda,i.categoira_tienda
                                    )
                                    viewmodel_socios.enviar_notificacion(cantidad_seguidores, obj)

                                }) {
                                    texto_generico_one_line("notificar a tus seguidores 50 monedas")
                                }
                            }


                            EstadoNotificacion.ADVERTENCIA -> {
                                Toast.makeText(
                                    context,
                                    "Advertencia: cambia la palabra \"${resultado_validacion_notificacion.palabraDetectada}\"",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // NO resetees aquí
                            }

                            EstadoNotificacion.BLOQUEADA -> {
                                Toast.makeText(
                                    context,
                                    "Tu notificación será bloqueada. Cambia la palabra \"${resultado_validacion_notificacion.palabraDetectada}\"",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // NO resetees aquí
                            }
                        }
                    }

                    if (nombre_publicacion.isNotEmpty() && descripcion_publicacion.isNotEmpty()) {
                        Button(onClick = {
                            crear_notificacion_conIA(
                                scope,
                                tituloPublicacion = nombre_publicacion,
                                descCorta = acortarDescripcionNotificacion(descripcion_publicacion),
                                nombreTienda = i.nombre_tienda,
                                localidad = "barranca", dias_restantes_pr
                            ) { es ->
                                titulo_notificacion = es.titulo
                                descripcion_notificacion = es.descripcion
                            }


                        }) {
                            texto_generico_one_line("generar Contenido con IA")
                        }
                    }


                }
            } else {
                item {
                    texto_generico_one_line("te falta cumplir los requisitos para notificar a tus seguidores")
                }
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
    fecha: (String) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf("") }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // 📌 Si es "Inicio", fijar fecha de hoy automáticamente
    LaunchedEffect(titulo) {
        if (titulo.lowercase() == "inicio") {
            val hoy = LocalDate.now()
            selectedDate = hoy.format(formatter)
            fecha(selectedDate)
        }
    }

    // 📅 DatePicker solo se usa cuando NO es inicio
    if (titulo.lowercase() != "inicio") {
        DatePickerExample_promociones(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onDateSelected = { fechaSeleccionada ->
                selectedDate = fechaSeleccionada.format(formatter)
                fecha(selectedDate)
            }
        )
    }

    Column {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            placeholder = { Text(titulo) },
            singleLine = true,
            readOnly = true,
            enabled = true,

            // ⛔ Bloquea el icono si es "Inicio"
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
                        tint = if (titulo.lowercase() == "inicio")
                            Color.Gray
                        else
                            MaterialTheme.colorScheme.primary
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


fun crear_notificacion_conIA_corta(
    scope: CoroutineScope,
    tituloPublicacion: String,
    descCorta: String,
    onResultado: (NotificacionIA) -> Unit
) {
    scope.launch {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        try {
            val prompt = generarPromptNotificacionselecionada(
                tituloPublicacion,
                descCorta,
            )

            val inicio = System.currentTimeMillis()
            val result = model.generateContent(prompt)
            val textoGenerado = result.text ?: ""
            val fin = System.currentTimeMillis()

            Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
            Log.d("Gemini", "Resultado:\n$textoGenerado")

            // 🔥 PARSEAR RESPUESTA
            val notificacion = parsearRespuestaGemini(textoGenerado)

            // 🔁 RETORNAR RESULTADO
            onResultado(notificacion)

        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
        }
    }
}

fun crear_notificacion_conIA(
    scope: CoroutineScope,
    tituloPublicacion: String,
    descCorta: String,
    nombreTienda: String,
    localidad: String,
    diasRestantes: Int,
    onResultado: (NotificacionIA) -> Unit
) {
    scope.launch {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        try {
            val prompt = generarPromptNotificacionOptimizado(
                tituloPublicacion,
                descCorta,
                nombreTienda,
                localidad,
                diasRestantes
            )

            val inicio = System.currentTimeMillis()
            val result = model.generateContent(prompt)
            val textoGenerado = result.text ?: ""
            val fin = System.currentTimeMillis()

            Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
            Log.d("Gemini", "Resultado:\n$textoGenerado")

            // 🔥 PARSEAR RESPUESTA
            val notificacion = parsearRespuestaGemini(textoGenerado)

            // 🔁 RETORNAR RESULTADO
            onResultado(notificacion)

        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
        }
    }
}


fun parsearRespuestaGemini(texto: String): NotificacionIA {
    var titulo = ""
    var descripcion = ""

    texto.lines().forEach { linea ->
        when {
            linea.startsWith("T:") ->
                titulo = linea.removePrefix("T:").trim()

            linea.startsWith("D:") ->
                descripcion = linea.removePrefix("D:").trim()
        }
    }

    return NotificacionIA(
        titulo = titulo,
        descripcion = descripcion
    )
}

fun generarPromptNotificacionOptimizado(
    tituloPublicacion: String,
    descCorta: String, // ≤60 chars
    nombreTienda: String,
    localidad: String,
    diasRestantes: Int
): String {

    return """
Genera un título (≤40) y una descripción (≤90) para notificación.
No inventes datos. Español neutro.
Usa MÁXIMO 1 emoji SOLO en el título. Sin emojis en la descripción.
Texto claro, directo y comercial. Incluye CTA breve.

Datos:
t:$tituloPublicacion
d:$descCorta
n:$nombreTienda
l:$localidad
r:$diasRestantes

Urgencia:
r=1 -> "Último día"
r<=3 -> "Últimos días"
r>3 -> "Por tiempo limitado"

Salida EXACTA:
T: texto
D: texto
""".trimIndent()
}

fun generarPromptNotificacionselecionada(
    tituloPublicacion: String,
    descCorta: String, // ≤60 chars


): String {

    return """
Genera un título (≤40) y una descripción (≤90) para notificación.
No inventes datos. Español neutro.
Usa MÁXIMO 1 emoji SOLO en el título. Sin emojis en la descripción.
Texto claro, directo y comercial. Incluye CTA breve.

Datos:
t:$tituloPublicacion
d:$descCorta


Urgencia:
r=1 -> "Último día"
r<=3 -> "Últimos días"
r>3 -> "Por tiempo limitado"

Salida EXACTA:
T: texto
D: texto
""".trimIndent()
}


fun acortarDescripcionNotificacion(
    textoLargo: String,
    maxCaracteres: Int = 90
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


suspend fun generarPromocionesConIA(
    tituloUsuario: String,
    descripcionUsuario: String,
    nombreTienda: String,
    localidad: String,
    diasRestantes: Int
): List<OpcionPromocionIA> {

    return try {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val prompt = generarPromptPromocionProduccion(
            tituloUsuario = tituloUsuario,
            descripcionUsuario = descripcionUsuario,
            nombreTienda = nombreTienda,
            localidad = localidad,
            diasRestantes = diasRestantes
        )

        val result = model.generateContent(prompt)
        val texto = result.text ?: return emptyList()

        parsearOpcionesIA(texto)

    } catch (e: Exception) {
        Log.e("IA", "Error IA promociones: ${e.message}")
        emptyList()
    }
}

fun generarPromptPromocionProduccion(
    tituloUsuario: String,
    descripcionUsuario: String,
    nombreTienda: String,
    localidad: String,
    diasRestantes: Int
): String {

    return """
Mejora el título y la descripción de una promoción usando SOLO la información dada.
No inventes datos ni precios.
Genera EXACTAMENTE 3 opciones distintas.

Reglas:
- Título ≤60 caracteres
- Descripción 50–70 palabras
- Español claro y comercial
- Sin emojis
- Texto profesional y directo

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad
duracion:$diasRestantes dias

Salida EXACTA:
Opcion 1:
T:
D:

Opcion 2:
T:
D:

Opcion 3:
T:
D:
""".trimIndent()
}


fun parsearOpcionesIA(texto: String): List<OpcionPromocionIA> {

    val opciones = mutableListOf<OpcionPromocionIA>()

    val bloques = texto.split("Opcion")
        .map { it.trim() }
        .filter { it.startsWith("1") || it.startsWith("2") || it.startsWith("3") }

    for (bloque in bloques) {

        val titulo = Regex("T:\\s*(.*)")
            .find(bloque)
            ?.groupValues
            ?.get(1)
            ?.trim()
            ?: continue

        val descripcion = Regex("D:\\s*([\\s\\S]*)")
            .find(bloque)
            ?.groupValues
            ?.get(1)
            ?.trim()
            ?: continue

        opciones.add(
            OpcionPromocionIA(
                titulo = titulo,
                descripcion = descripcion
            )
        )
    }

    return opciones
}


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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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


fun cobroMonedas(dias: Int): Int {
    val monedasPorDia = 30
    return dias * monedasPorDia
}

enum class EstadoNotificacion {
    PERMITIDA,
    ADVERTENCIA,
    BLOQUEADA
}

data class ResultadoValidacion(
    val estado: EstadoNotificacion,
    val mensaje: String,
    val palabraDetectada: String? = null
)

fun normalizarTexto(texto: String): String {
    return texto.lowercase()
        .replace("@", "a")
        .replace("3", "e")
        .replace("4", "a")
        .replace("1", "i")
        .replace("!", "i")
        .replace("\\$", "s")
        .replace("0", "o")
        .replace("[^a-z ]".toRegex(), "") // eliminar cualquier otro carácter especial
}

val palabrasBloqueadas = listOf(
    "sexo",
    "porno",
    "escort",
    "droga",
    "arma",
    "casino",
    "apuesta",
    "elecciones",
    "política",
    "voten por",
    "alcalde",
    "presidente",
    "viva",
    "inbox",
    "privado",
    "dm",
    "háblame por whatsapp",
    "whatsapp al",
    "escríbeme al",
    "manda dm",
    "contacto directo",
    "huevón",
    "huevona",
    "cojudo",
    "cojuda",
    "pendejo",
    "pendeja",
    "mierda",
    "concha",
    "conchesumadre",
    "culiao",
    "culiada",
    "imbécil",
    "idiota",
    "estúpido",
    "estúpida",
    "malparido",
    "malparida",
    "carajo",
    "puta",
    "puto",
    "puta madre",
    "coño",
    "concha e tu madre",
    "hijo de puta",
    "marico",
    "marica",
    "mierda",
    "maldito",
    "cabrón",
    "cabróna",
    "pendejo",
    "pendeja"
)

fun validarNotificacion(titulo: String, descripcion: String): ResultadoValidacion {
    val textoNormalizado = normalizarTexto("$titulo $descripcion")

    palabrasBloqueadas.forEach { palabra ->
        val palabraNormalizada = normalizarTexto(palabra)
        if (textoNormalizado.contains(palabraNormalizada)) {
            return ResultadoValidacion(
                estado = EstadoNotificacion.BLOQUEADA,
                mensaje = "La palabra \"${palabra}\" no está permitida en notificaciones.",
                palabraDetectada = palabra
            )
        }
    }

    return ResultadoValidacion(
        estado = EstadoNotificacion.PERMITIDA,
        mensaje = "Notificación válida.",
        palabraDetectada = null
    )

}

@Composable
fun item_publicaiones_realizadas(
    i: datos_publicaciones_realizadas,
    clikeado: (String, String,String) -> Unit
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
                .clickable { clikeado(i.titulo, i.descripcion,i.id) },
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





package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.annotation.SuppressLint


import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_normas_de_verificacion
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_verificacion_proceso
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_verificada_automatico
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textos_titulos_geinz_wokr
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_review
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


private lateinit var firebaseAuth: FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_review(
    verificar_intener: Boolean,
    datos_principales_user: datos_principales_user,
    viewmodel: viewmodel_review,
    data_class_review: data_class_review,
    ondimis: () -> Unit,
    clik_envio: (Int, String, lista: List<ImagenReview>) -> Unit,
    crear_cuenta: () -> Unit, iniciar_seccion: () -> Unit,
    mostar_snacbar: () -> Unit
) {

    val context = LocalContext.current
    firebaseAuth = FirebaseAuth.getInstance()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    val _datos_TL_review = viewmodel._datos_TL_review.observeAsState()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val datos_tienda_review_state = viewmodel.datos_tienda_review_flow.collectAsState()
    val _verificar_review_exsit = viewmodel._verificar_review_exsit.observeAsState()
    val _review_send = viewmodel._review_send.observeAsState(initial = false)
    var texto by remember { mutableStateOf("") }
    var ratingValue by remember { mutableStateOf(0) }
    var fecha_registrada by remember { mutableStateOf("") }
    var listaImgObtenidas by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var clicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (clicked) 1.05f else 1f)
    var five_estrellas by remember { mutableStateOf(false) }
    var dialog_verificaca_automatico by remember { mutableStateOf(false) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetii))
    var showAnimation by remember { mutableStateOf(five_estrellas) }
    val caracteresMinimos = 60
    val caracteresMaximos = 1500
    val tieneError = texto.length in 1 until caracteresMinimos
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val tick by viewModelFiltros.tick.collectAsState()
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    var id_respado_user by remember { mutableStateOf("") }
    val maxFotos = 5
    val imagenes = remember { mutableStateListOf<ImagenReview>() }
    var mostar_img_zoom by remember { mutableStateOf(false) }


    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            id_respado_user = uid_respald_user
            Log.d("UID_DataStore", "✅ Recuperado UID válido desde DataStore: $id_respado_user")
        } else {
            id_respado_user = ""
        }
    }
    var color by remember { mutableStateOf(Color.Gray) }
    LaunchedEffect(Unit) {
        viewmodel.set_datos_TL_review(data_class_review)
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }
    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                data_class_review.localida_lugar,
                data_class_review.id_tienda_lugar
            )
        }
    }
    // cerrar bottomsheet al enviar review
    LaunchedEffect(_review_send.value) {
        if (_review_send.value) {
            ondimis()
            viewmodel.resetar_valor_review()
        }
    }

    // verificar si ya existe review cuando hay usuario
    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            viewmodel.verificar_review_existente(uid_respald_user, data_class_review)
        }
    }

    // cargar datos existentes en los estados
    LaunchedEffect(_verificar_review_exsit.value) {
        imagenes.clear()
        texto = _verificar_review_exsit.value?.descripcion ?: ""
        ratingValue = (_verificar_review_exsit.value?.calificacion ?: 1) as Int
        fecha_registrada = _verificar_review_exsit.value?.fecha_realizada ?: ""
        _verificar_review_exsit.value?.lista_img?.forEach { url ->
            imagenes.add(
                ImagenReview(
                    url = url,
                    uri = null
                )
            )
        }

    }

    LaunchedEffect(five_estrellas) {
        if (five_estrellas) {
            showAnimation = true
            delay(5000)
            showAnimation = false
        } else {
            showAnimation = false
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

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxSheetHeight = screenHeight * 0.8f
    var imagenZoomSeleccionada by remember {
        mutableStateOf<String?>(null)
    }

    if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = {
                imagenes.clear()
                texto = ""
                ratingValue = 0
                fecha_registrada = ""
                ondimis()
                viewmodel.limpiar_estado()
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .imePadding()
            ) {
                Crossfade(
                    targetState = datos_tienda_review_state.value,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                ) { state ->
                    when (state) {
                        is viewmodel_review.datos_tienda_review.laoding -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(LocalConfiguration.current.screenHeightDp.dp * 0.3f), // 30% de la altura de pantalla
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is viewmodel_review.datos_tienda_review.succes -> {
                            val datos = state.item

                            FuenteControladaApp {
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(
                                            start = 10.dp,
                                            end = 10.dp,
                                            top = 20.dp,
                                            bottom = 30.dp
                                        )
                                        .imePadding()
                                        .fillMaxSize()

                                ) {
                                    item {
                                        Text(
                                            text = "Cuéntanos tu experiencia",
                                            fontFamily = textos_titulos_geinz_wokr,
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(end = 20.dp)
                                        )
                                        spacer_vertical(10.dp)
                                        val annotatedText = buildAnnotatedString {
                                            append("Hola ${datos_principales_user.nombre}, tu opinión nos ayuda a mejorar. Deja tu reseña sobre ")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append(datos.nombre.toString())
                                            }

                                            append(". Será ")

                                            // Parte clickeable y subrayada
                                            pushStringAnnotation(
                                                tag = "VERIFICADA",
                                                annotation = "verificada"
                                            )
                                            withStyle(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append("verificada automáticamente")
                                            }
                                            pop()

                                            append(". ¡Gracias por confiar en Geinz!")
                                        }

                                        ClickableText(
                                            text = annotatedText,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                            onClick = { offset ->
                                                annotatedText.getStringAnnotations(
                                                    tag = "VERIFICADA",
                                                    start = offset,
                                                    end = offset
                                                )
                                                    .firstOrNull()?.let {
                                                        dialog_verificaca_automatico = true
//

                                                    }
                                            }
                                        )

                                        if (fecha_registrada != "") {
                                            texto_generico_one_line(
                                                "Fecha de reseña publicada : ${fecha_registrada}",
                                                modifier = Modifier.padding(
                                                    top = 10.dp,
                                                    end = 7.dp
                                                ),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        spacer_vertical(20.dp)
                                    }
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        ) {
                                            // Imagen
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(
                                                        datos.imagen
                                                            ?: R.drawable.cargando_img_categorias
                                                    )
                                                    .placeholder(R.drawable.cargando_img_categorias)
                                                    .error(R.drawable.cargando_img_categorias)
                                                    .build(),
                                                contentDescription = "Imagen de la tienda",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .graphicsLayer {
                                                        scaleX = scale
                                                        scaleY = scale
                                                    }
                                                    .shadow(
                                                        elevation = 24.dp,
                                                        ambientColor = Color.White.copy(alpha = 0.8f),
                                                        spotColor = Color.White.copy(alpha = 0.6f)
                                                    )
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() }) {
                                                        show_bottom_sheeet = true
                                                    }
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                            )

                                            this@ModalBottomSheet.AnimatedVisibility(
                                                showAnimation,
                                                enter = fadeIn(),
                                                exit = fadeOut(),
                                                modifier = Modifier
                                                    .matchParentSize()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color.Black.copy(alpha = 0.70f))
                                                )
                                            }
                                            this@ModalBottomSheet.AnimatedVisibility(
                                                showAnimation,
                                                enter = fadeIn(),
                                                exit = fadeOut(),
                                            ) {
                                                LottieAnimation(
                                                    composition,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .align(Alignment.TopCenter)
                                                )
                                            }
                                            this@ModalBottomSheet.AnimatedVisibility(
                                                showAnimation,
                                                enter = fadeIn(),
                                                exit = fadeOut(),
                                                modifier = Modifier.align(Alignment.Center)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(
                                                            datos.imagen
                                                                ?: R.drawable.cargando_img_categorias
                                                        )
                                                        .placeholder(R.drawable.cargando_img_categorias)
                                                        .error(R.drawable.cargando_img_categorias)
                                                        .build(),
                                                    contentDescription = "Imagen de la tienda",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .width(100.dp)
                                                        .height(100.dp)
                                                )
                                            }


                                        }

                                        spacer_vertical(15.dp)
                                    }
                                    item {

                                        FullStarRating(
                                            starSize = 35.dp,
                                            onRatingChanged = { newRating ->
                                                five_estrellas = newRating == 5
                                                ratingValue = newRating
                                            },
                                            initialRating = ratingValue,
                                        )
                                        spacer_vertical(20.dp)


                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = texto,
                                                onValueChange = {
                                                    if (it.length <= caracteresMaximos) {
                                                        texto = it
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                label = { retornar_pleaceholder_label("Déjanos tu opinión") },
                                                placeholder = { retornar_pleaceholder_label("Déjanos tu opinión") },
                                                textStyle = MaterialTheme.typography.bodyMedium,
                                                singleLine = false,
                                                maxLines = 10,
                                                minLines = 6,
                                                isError = tieneError,
                                                supportingText = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        if (tieneError) {
                                                            Text(
                                                                text = "Debe tener al menos $caracteresMinimos caracteres",
                                                                color = MaterialTheme.colorScheme.error,
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        } else {
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }

                                                        Text(
                                                            text = "${texto.length}/$caracteresMaximos",
                                                            color = if (texto.length > caracteresMaximos - 50)
                                                                MaterialTheme.colorScheme.error
                                                            else
                                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            )

                                        }
                                        spacer_vertical(15.dp)
                                    }
                                    item {
                                        texto_generico_multilinea(
                                            "¡Muestra lo mejor de tu experiencia!",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_vertical(10.dp)
                                        SelectorFotosReview(
                                            imagenes = imagenes,
                                            maxFotos = maxFotos,
                                            onAddClick = {
                                                picker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            onRemove = { uri ->
                                                imagenes.remove(uri)
                                            }, mostar_zoom_img = {img->
                                                val imageModel = img.uri ?: img.url
                                                mostar_img_zoom=true
                                                imagenZoomSeleccionada = img.uri?.toString() ?: img.url                                            })

                                    }
                                    item {
                                        spacer_vertical(15.dp)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(CircleShape)
                                                .background(
                                                    MaterialTheme.colorScheme.primary
                                                )
                                                .clickable {
                                                    if (ratingValue == 0 || texto.isEmpty()) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Ups, parece que faltan algunos campos por completar.",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    } else if (tieneError) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Por favor, escribe una reseña más completa (mínimo 60 caracteres)",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }

                                                    } else {
                                                        ondimis()
                                                        clik_envio(ratingValue, texto, imagenes)
                                                        viewmodel.limpiar_estado()
                                                    }
                                                }, contentAlignment = Alignment.Center
                                        ) {
                                            texto_generico_one_line(
                                                "Enviar reseña",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(vertical = 15.dp)
                                            )
                                        }

                                    }


                                }
                            }
                        }

                        is viewmodel_review.datos_tienda_review.error -> {
                            val mensaje =
                                (datos_tienda_review_state.value as viewmodel_review.datos_tienda_review.error).txt
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                FuenteControladaApp {
                                    Text(
                                        text = mensaje,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        else -> {}
                    }
                }

                SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
            }

        }
    } else {
        bottom_sheet_registrate(
            ondimis = {
                ondimis()
            },
            iniciar_seccion_normal = {
                iniciar_seccion()
                ondimis()
            },
            crear_cuenta_geinz = {
                crear_cuenta()
                ondimis()
            },
            texto_bottom_Sheet = "Inicia sesión para compartir tu experiencia"
        )
    }
    if (show_bottom_sheeet) {
        bottom_sheet_tiendas_filtradas(
            verificar_intener,
            viewModelFiltros,
            dataclass_tienda_seleccionada, show_bottom_sheeet
        ) {
            show_bottom_sheeet = false
        }
    }

    if (dialog_verificaca_automatico) {
        dialog_verificada_automatico { dialog_verificaca_automatico = false }
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

}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_Sheet_seguro(
    verificar_intener: Boolean,
    esta_o_no_lugar: Boolean,
    datos_principales_user: datos_principales_user,
    viewmodel: viewmodel_review,
    data_class_review: data_class_review,
    ondimis: () -> Unit,
    clik_envio: (Int, String, Location?) -> Unit,
    crear_cuenta: () -> Unit,
    iniciar_seccion: () -> Unit
) {

    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var ubicacionPrevia by remember { mutableStateOf<Location?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val datos_tienda_review_state = viewmodel.datos_tienda_review_flow.collectAsState()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val _verificar_review_exsit = viewmodel._verificar_review_exsit.observeAsState()
    val _review_send = viewmodel._review_send.observeAsState(initial = false)
    var texto by remember { mutableStateOf("") }
    var ratingValue by remember { mutableStateOf(0) }
    var fecha_registrada by remember { mutableStateOf("") }
    var five_estrellas by remember { mutableStateOf(false) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetii))
    var showAnimation by remember { mutableStateOf(five_estrellas) }
    val caracteresMinimos = 60
    val caracteresMaximos = 1500
    val tieneError = texto.length in 1 until caracteresMinimos
    val snackbarHostState = remember { SnackbarHostState() }
    var dialog_proceso_verificacion by remember { mutableStateOf(false) }
    var dialog_normas_de_verificaion by remember { mutableStateOf(false) }
    var show_bottom_sheeet by remember { mutableStateOf(false) }
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    val datosTienda by viewModelFiltros._datos_tienda.observeAsState()
    val tick by viewModelFiltros.tick.collectAsState()
    var color by remember { mutableStateOf(Color.Gray) }
    val scope = rememberCoroutineScope()
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    var id_respado_user by remember { mutableStateOf("") }

    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            id_respado_user = uid_respald_user
            Log.d("UID_DataStore", "✅ Recuperado UID válido desde DataStore: $id_respado_user")
        } else {
            id_respado_user = ""
        }
    }
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            ubicacionPrevia = location
            Log.d("ReviewUbicacion", "Ubicación prefetch -> $location")
        }
    }
    LaunchedEffect(datosTienda) {
        if (!datosTienda.isNullOrEmpty()) {
            dataclass_tienda_seleccionada =
                datosTienda!!.first()
        }
    }
    LaunchedEffect(show_bottom_sheeet) {
        if (show_bottom_sheeet) {
            viewModelFiltros.obtener_campos_tiendas_por_id(
                data_class_review.localida_lugar,
                data_class_review.id_tienda_lugar
            )
        }
    }
    LaunchedEffect(data_class_review) {
        viewmodel.set_datos_TL_review(data_class_review)
    }

    // cerrar bottomsheet al enviar review
    LaunchedEffect(_review_send.value) {
        if (_review_send.value) {
            ondimis()
            viewmodel.resetar_valor_review()
        }
    }
    // verificar si ya existe review cuando hay usuario
    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {

            viewmodel.verificar_review_existente(uid_respald_user, data_class_review)
        }
    }

    // cargar datos existentes en los estados
    LaunchedEffect(_verificar_review_exsit.value) {
        texto = _verificar_review_exsit.value?.descripcion ?: ""
        ratingValue = (_verificar_review_exsit.value?.calificacion ?: 1) as Int
        fecha_registrada = _verificar_review_exsit.value?.fecha_realizada ?: ""

    }

    LaunchedEffect(five_estrellas) {
        if (five_estrellas) {
            showAnimation = true
            delay(5000)
            showAnimation = false
        } else {
            showAnimation = false
        }
    }
    if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {

        ModalBottomSheet(
            onDismissRequest = {
                ondimis()
                viewmodel.limpiar_estado()
            },
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            containerColor = MaterialTheme.colorScheme.background
        ) {

            FuenteControladaApp {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    Crossfade(
                        targetState = datos_tienda_review_state.value,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    ) { state ->
                        when (state) {
                            is viewmodel_review.datos_tienda_review.error -> {
                                val mensaje =
                                    (datos_tienda_review_state.value as viewmodel_review.datos_tienda_review.error).txt
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FuenteControladaApp {
                                        Text(
                                            text = mensaje,
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            is viewmodel_review.datos_tienda_review.laoding -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(LocalConfiguration.current.screenHeightDp.dp * 0.3f), // 30% de la altura de pantalla
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is viewmodel_review.datos_tienda_review.succes -> {
                                val datos = state.item

                                Column(
                                    modifier = Modifier
                                        .padding(
                                            start = 10.dp,
                                            end = 10.dp,
                                            top = 20.dp,
                                            bottom = 30.dp
                                        )
                                        .verticalScroll(rememberScrollState())
                                        .imePadding()
                                ) {
                                    Text(
                                        text = "Cuéntanos tu experiencia",
                                        fontFamily = textos_titulos_geinz_wokr, fontSize = 30.sp,
                                        modifier = Modifier.padding(end = 20.dp)
                                    )
                                    spacer_vertical(10.dp)
                                    if (!esta_o_no_lugar) {
                                        val annotatedText = buildAnnotatedString {
                                            append("Hola ${datos_principales_user.nombre}, tu opinión es muy valiosa para nosotros. Deja tu reseña sobre ")

                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append(datos.nombre.toString())
                                            }

                                            append(". Tu reseña pasará por un ")

                                            pushStringAnnotation(
                                                tag = "VERIFICADA",
                                                annotation = "verificada"
                                            )
                                            withStyle(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append("proceso de verificación")
                                            }
                                            pop()

                                            append(" para confirmar que cumple con las ")

                                            pushStringAnnotation(
                                                tag = "NORMAS",
                                                annotation = "normas"
                                            )
                                            withStyle(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append("normas")
                                            }
                                            pop()

                                            append(" de reseñas de Geinz. Tu reseña se publicará una vez que completes este proceso. ¡Gracias por compartir tu experiencia con Geinz!")
                                        }

                                        ClickableText(
                                            text = annotatedText,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                            onClick = { offset ->
                                                annotatedText.getStringAnnotations(
                                                    start = offset,
                                                    end = offset
                                                )
                                                    .firstOrNull()?.let { annotation ->
                                                        when (annotation.tag) {
                                                            "VERIFICADA" -> {
                                                                dialog_proceso_verificacion = true
                                                            }

                                                            "NORMAS" -> {
                                                                dialog_normas_de_verificaion = true
                                                            }
                                                        }
                                                    }
                                            }
                                        )
                                    } else {
                                        val annotatedText = buildAnnotatedString {
                                            append("Hola ${datos_principales_user.nombre}, tu opinión nos ayuda a mejorar. Deja tu reseña sobre ")

                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append(datos.nombre.toString())
                                            }
                                        }
                                        Text(
                                            "$annotatedText. ¡Gracias por confiar en Geinz!",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    if (fecha_registrada != "") {
                                        texto_generico_one_line(
                                            "Fecha de reseña publicada : ${fecha_registrada}",
                                            modifier = Modifier.padding(top = 10.dp, end = 7.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    spacer_vertical(20.dp)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        // Imagen
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(
                                                    datos.imagen
                                                        ?: R.drawable.cargando_img_categorias
                                                )
                                                .placeholder(R.drawable.cargando_img_categorias)
                                                .error(R.drawable.cargando_img_categorias)
                                                .build(),
                                            contentDescription = "Imagen de la tienda",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }) {
                                                    show_bottom_sheeet = true
                                                }
                                        )

                                        this@Column.AnimatedVisibility(
                                            showAnimation,
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                            modifier = Modifier
                                                .matchParentSize()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color.Black.copy(alpha = 0.70f))
                                            )
                                        }
                                        this@Column.AnimatedVisibility(
                                            showAnimation,
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                        ) {
                                            LottieAnimation(
                                                composition,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .align(Alignment.TopCenter)
                                            )
                                        }
                                        this@Column.AnimatedVisibility(
                                            showAnimation,
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                            modifier = Modifier.align(Alignment.Center)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(
                                                        datos.imagen
                                                            ?: R.drawable.cargando_img_categorias
                                                    )
                                                    .placeholder(R.drawable.cargando_img_categorias)
                                                    .error(R.drawable.cargando_img_categorias)
                                                    .build(),
                                                contentDescription = "Imagen de la tienda",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .width(100.dp)
                                                    .height(100.dp)
                                            )
                                        }

                                    }
                                    spacer_vertical(15.dp)
                                    FullStarRating(
                                        starSize = 30.dp,
                                        onRatingChanged = { newRating ->
                                            five_estrellas = newRating == 5
                                            ratingValue = newRating
                                        },
                                        initialRating = ratingValue,
                                    )
                                    spacer_vertical(20.dp)
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = texto,
                                            onValueChange = {
                                                if (it.length <= caracteresMaximos) {
                                                    texto = it
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            label = { retornar_pleaceholder_label("Déjanos tu opinión") },
                                            placeholder = { retornar_pleaceholder_label("Déjanos tu opinión") },
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            singleLine = false,
                                            maxLines = 10,
                                            minLines = 7,
                                            isError = tieneError,
                                            supportingText = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    if (tieneError) {
                                                        Text(
                                                            text = "Debe tener al menos $caracteresMinimos caracteres",
                                                            color = MaterialTheme.colorScheme.error,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }

                                                    Text(
                                                        text = "${texto.length}/$caracteresMaximos",
                                                        color = if (texto.length > caracteresMaximos - 50)
                                                            MaterialTheme.colorScheme.error
                                                        else
                                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        )
                                    }

                                    spacer_vertical(15.dp)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary
                                            )
                                            .clickable {
                                                if (ratingValue == 0 || texto.isEmpty()) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Ups, parece que faltan algunos campos por completar.",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } else if (tieneError) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Por favor, escribe una reseña más completa (mínimo 60 caracteres)",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } else {
                                                    ondimis()

                                                    clik_envio(ratingValue, texto, ubicacionPrevia)
                                                    viewmodel.limpiar_estado()
                                                }
                                            }, contentAlignment = Alignment.Center
                                    ) {
                                        texto_generico_one_line(
                                            "Enviar reseña",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(vertical = 15.dp)
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    } else {
        bottom_sheet_registrate(
            ondimis = {
                ondimis()
            },
            iniciar_seccion_normal = {
                iniciar_seccion()
                ondimis()
            },
            crear_cuenta_geinz = {
                crear_cuenta()
                ondimis()
            },
            texto_bottom_Sheet = "Inicia sesión para compartir tu experiencia"
        )
    }

    if (dialog_normas_de_verificaion) {
        dialog_normas_de_verificacion { dialog_normas_de_verificaion = false }
    }
    if (dialog_proceso_verificacion) {
        dialog_verificacion_proceso { dialog_proceso_verificacion = false }
    }

    if (show_bottom_sheeet) {
        bottom_sheet_tiendas_filtradas(
            verificar_intener,
            viewModelFiltros,
            dataclass_tienda_seleccionada, show_bottom_sheeet
        ) {
            show_bottom_sheeet = false
        }
    }

}

@Composable
fun FullStarRating(
    modifier: Modifier = Modifier,
    starSize: Dp = 15.dp,
    maxStars: Int = 5,
    initialRating: Int = 0,
    onRatingChanged: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(initialRating) }

    LaunchedEffect(initialRating) {
        rating = initialRating
    }

    fun ratingText(rating: Int): String = when (rating) {
        1 -> "Muy malo 😡"
        2 -> "Malo 😞"
        3 -> "Regular 😐"
        4 -> "Bueno 🙂"
        5 -> "Excelente 🤩"
        else -> ""
    }
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(starSize)
                .onSizeChanged { rowSize = it }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x
                        val starWidth = rowSize.width / maxStars
                        val newRating = ((x / starWidth).toInt() + 1)
                            .coerceIn(0, maxStars)

                        onRatingChanged(newRating)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxStars) {
                Box(
                    modifier = Modifier
                        .weight(1f)               // 👈 ocupa todo el ancho
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= rating)
                            Color.Yellow
                        else
                            Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(starSize)
                    )
                }
            }
        }

        // Box para mostrar el texto debajo de las estrellas
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            texto_generico_one_line(ratingText(rating))
        }
    }
}

@Composable
fun SelectorFotosReview(
    imagenes: SnapshotStateList<ImagenReview>,
    maxFotos: Int,
    onAddClick: () -> Unit,
    onRemove: (ImagenReview) -> Unit,
    mostar_zoom_img:(ImagenReview)-> Unit
) {
    var remover by remember { mutableStateOf(false) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
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
                        .size(120.dp)
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
                                onRemove(img)

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
                            .clickable { }
                    )
                }
            }
        }

        // ➕ AGREGAR FOTO
        if (imagenes.size < maxFotos) {
            item(key = "camera") {
                Box(
                    modifier = Modifier
                        .size(120.dp)
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







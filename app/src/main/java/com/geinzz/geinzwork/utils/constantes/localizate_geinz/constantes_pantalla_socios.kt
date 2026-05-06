package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.SubcomposeAsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.BoxImagen
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.PlaceholderInterno
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.esUriLocal
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.esUrlRemota
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.guardarCambiosImagenes
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.guardarImagenesEnFirestore
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.guardarImagenesEnFirestore_promociones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenParaWhatsappDB
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenWebPSinRecorte
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.utils.constantes.constantes.Variables.idTienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import kotlinx.coroutines.launch
import kotlin.collections.getOrNull

object constantes_pantalla_socios {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun BtnSoporte(tipo: String, context: Context, id_user: String) {

        val paddingInicial = 5.dp   // ⬅ padding solo para posición inicial

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val density = LocalDensity.current
            val scope = rememberCoroutineScope()

            val bubbleSizePx = with(density) { 60.dp.toPx() }
            val paddingInicialPx = with(density) { paddingInicial.toPx() }

            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()

            // Límites REALES (pegado total permitido)
            val minX = 0f
            val minY = 0f
            val maxX = screenWidth - bubbleSizePx
            val maxY = screenHeight - bubbleSizePx

            // Posición inicial con padding
            val offsetX = remember { Animatable(maxX - paddingInicialPx) }
            val offsetY = remember { Animatable(maxY - paddingInicialPx) }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            abrir_whattsapp(
                                id_user,
                                "",
                                "",
                                "",
                                context = context,
                                "958120920",
                                if (tipo == "Soporte")
                                    "Hola, deseo comunicarme con el soporte de Geinz. Mi ID de usuario es: $id_user"
                                else
                                    "Hola, tengo problemas para ingresar a mi cuenta de tienda. Mi ID de usuario es: $id_user"
                            )
                        }
                        .pointerInput(Unit) {

                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()

                                    val newX = (offsetX.value + dragAmount.x)
                                        .coerceIn(minX, maxX)

                                    val newY = (offsetY.value + dragAmount.y)
                                        .coerceIn(minY, maxY)

                                    scope.launch {
                                        offsetX.snapTo(newX)
                                        offsetY.snapTo(newY)
                                    }
                                },

                                onDragEnd = {
                                    val middle = screenWidth / 2
                                    val targetX = if (offsetX.value < middle) minX else maxX

                                    scope.launch {
                                        offsetX.animateTo(
                                            targetX,
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        painter = painterResource(R.drawable.soporte_icon),
                        contentDescription = "",
                        modifier = Modifier.size(25.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }


    @Composable
    fun carga_inicial() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(5f),
            contentAlignment = Alignment.Center
        ) {
            pantalla_carga_login(false)
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @Composable
    fun BoxFotosTipos(
        id_user: String,
        tipo: String,
        id_tienda: String,
        urlsDesdeDb: List<String>, max: Int,
    ) {
        val contxt = LocalContext.current

        var mostrarDialogozoom by remember { mutableStateOf(false) }
        var valor_img_completa by remember { mutableStateOf("") }
        val eliminadas = remember { mutableStateListOf<Int>() }

        val imagenesOriginales = remember {
            List(max) { index -> urlsDesdeDb.getOrNull(index) }
        }

        val imagenes = remember {
            mutableStateListOf<String?>().apply {
                addAll(imagenesOriginales)
                repeat(max - size) { add(null) }
            }
        }

        val hayCambios by remember {
            derivedStateOf {
                eliminadas.isNotEmpty() ||
                        imagenes.any { esUriLocal(it) }
            }
        }


        var indexSeleccionado by remember { mutableStateOf<Int?>(null) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                indexSeleccionado?.let { index ->
                    imagenes[index] = it.toString()
                    eliminadas.remove(index)
                }
            }
        }

        // --- LazyRow state y shadows en tiempo real ---
        val listState = rememberLazyListState()
        var showLeftShadow by remember { mutableStateOf(false) }
        var showRightShadow by remember { mutableStateOf(false) }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (firstIndex, scrollOffset) ->
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    val totalItems = listState.layoutInfo.totalItemsCount

                    showLeftShadow = firstIndex > 0 || scrollOffset > 0
                    showRightShadow = visibleItems.lastOrNull()?.index != null &&
                            visibleItems.lastOrNull()?.index!! < totalItems - 1
                }
        }

        val alphaLeft by animateFloatAsState(
            targetValue = if (showLeftShadow) 1f else 0f,
            animationSpec = tween(400)
        )
        val alphaRight by animateFloatAsState(
            targetValue = if (showRightShadow) 1f else 0f,
            animationSpec = tween(400)
        )
        var guardando by remember { mutableStateOf(false) }


        // ----------------------------------------------
        Column() {
            Box(
                modifier = Modifier
                    .animateContentSize()
                    .height(100.dp)
            ) {

                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(imagenes) { index, valor ->
                        BoxImagen(
                            valor = valor,
                            estaEliminada = eliminadas.contains(index),
                            onClick = {
                                indexSeleccionado = index
                                launcher.launch("image/*")
                            },
                            onCancelarOEliminar = {

                                // 🔁 SI ESTÁ ELIMINADA → RESTABLECER
                                if (eliminadas.contains(index)) {
                                    eliminadas.remove(index)
                                    imagenes[index] = imagenesOriginales[index]
                                    return@BoxImagen
                                }

                                when {
                                    esUriLocal(valor) -> {
                                        imagenes[index] = imagenesOriginales[index]
                                    }

                                    esUrlRemota(valor) -> {
                                        eliminadas.add(index)
                                        imagenes[index] = null
                                    }
                                }
                            }, onExpandir = {
                                mostrarDialogozoom = true
                                valor_img_completa = valor ?: ""
                            }

                        )
                    }
                }
                // Sombra izquierda
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)

                        .align(Alignment.CenterStart)
                        .zIndex(1f)
                        .alpha(alphaLeft)
                        .background(Brush.horizontalGradient(colors = shadow_top_filtrado_v2))
                )

                // Sombra derecha
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)
                        .align(Alignment.CenterEnd)
                        .zIndex(1f)
                        .alpha(alphaRight)
                        .background(Brush.horizontalGradient(colors = shadow_botonm_filtrado_v2))
                )
            }

            AnimatedVisibility(
                visible = hayCambios,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = !guardando) {

                                guardando = true
                                guardarCambiosImagenes(
                                    tipo = tipo,
                                    context = contxt,
                                    imagenes = imagenes,
                                    eliminadas = eliminadas,
                                    imagenesOriginales = imagenesOriginales,
                                    idTienda = id_tienda,
                                    "barranca"
                                ) { completo ->
                                    val urlsFinales = completo.filterNotNull()
                                    guardarImagenesEnFirestore(
                                        localidad = "barranca",
                                        idTienda = id_tienda,
                                        tipo = tipo,
                                        urls = urlsFinales,
                                        onSuccess = {
                                            Log.d(
                                                "agregardo_firebae",
                                                "🔥 Firestore actualizado ($tipo)"
                                            )

                                        },
                                        onError = {
                                            Log.e("agregardo_firebae", "❌ Error Firestore", it)

                                        }
                                    )


                                    guardando = false
                                    eliminadas.clear()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            if (guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = if (guardando) "Guardando…" else "Guardar cambios",
                                color = Color.White
                            )
                        }
                    }
                }
            }

        }
        if (mostrarDialogozoom) {
            ZoomableGalleryFullScreen(
                id_user,
                compartir_promocion(),
                imagenes = listOf(valor_img_completa),
                startIndex = 0,
                onDismiss = { mostrarDialogozoom = false }
            )

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun BoxTipo_promociones(
        id_user: String,
        tipo: String,
        id_tienda: String,
        urlsDesdeDb: Map<String, String>,
        max: Int
    ) {
        val context = LocalContext.current

        var mostrarDialogozoom by remember { mutableStateOf(false) }
        var valor_img_completa by remember { mutableStateOf<Pair<String, String?>?>(null) }
        val eliminadas = remember { mutableStateListOf<String>() } // IDs eliminadas

        // Generamos map completo con max slots
        val imagenes = remember {
            mutableStateMapOf<String, String?>().apply {
                for (i in 0 until max) {
                    val keyExistente = urlsDesdeDb.keys.elementAtOrNull(i)
                    val id = keyExistente ?: generarIdImagen() // 🔹 generar ID único si no hay
                    put(id, urlsDesdeDb[keyExistente]) // si no hay URL queda null
                }
            }
        }

        val hayCambios by remember {
            derivedStateOf {
                eliminadas.isNotEmpty() || imagenes.any { esUriLocal(it.value) }
            }
        }

        var idSeleccionado by remember { mutableStateOf<String?>(null) }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    idSeleccionado?.let { id ->
                        imagenes[id] = it.toString()
                        eliminadas.remove(id)
                    }
                }
            }

        val listState = rememberLazyListState()
        var guardando by remember { mutableStateOf(false) }

        Column {
            Box(modifier = Modifier
                .animateContentSize()
                .height(100.dp)) {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(imagenes.toList()) { (id, url) ->
                        BoxImagen(
                            valor = url,
                            estaEliminada = eliminadas.contains(id),
                            onClick = {
                                idSeleccionado = id
                                launcher.launch("image/*")
                            },
                            onCancelarOEliminar = {
                                if (eliminadas.contains(id)) {
                                    eliminadas.remove(id)
                                    imagenes[id] = urlsDesdeDb[id]
                                    return@BoxImagen
                                }
                                when {
                                    esUriLocal(url) -> {
                                        imagenes[id] = urlsDesdeDb[id]
                                    }

                                    esUrlRemota(url) -> {
                                        eliminadas.add(id)
                                        imagenes[id] = null
                                    }
                                }
                            },
                            onExpandir = {
                                mostrarDialogozoom = true
                                valor_img_completa = id to url
                            }
                        )
                    }
                }
            }
            spacer_vertical(10.dp)
            AnimatedVisibility(visible = hayCambios) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = !guardando) {
                            guardando = true

                            // Guardamos cambios con Map<ID, URL?>
                            constantes_subir_img_panel_tienda.guardandoCambiosPromociones(
                                tipo = tipo,
                                context = context,
                                imagenes = imagenes.toMap(),
                                eliminadas = eliminadas.toList(),
                                idTienda = id_tienda,
                                localidad = "barranca"
                            ) { mapFinal, _ ->
                                eliminadas.clear()
                                // Guardamos en Firestore
                                guardarImagenesEnFirestore_promociones(
                                    localidad = "barranca",
                                    idTienda = id_tienda,
                                    tipo = "promociones",
                                    fotos = mapFinal
                                )
                                guardando = false
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (guardando) "Guardando…" else "Guardar cambios",
                        color = Color.White
                    )
                }
            }
        }

        if (mostrarDialogozoom && valor_img_completa != null) {
            ZoomableGalleryFullScreen(
                id_user,
                compartir_promocion(),
                imagenes = listOf(valor_img_completa!!.second ?: ""), // URL
                startIndex = 0,
                onDismiss = { mostrarDialogozoom = false }
            )
        }
    }

    fun agregarImagenParaBot(
        localidad_tienda: String,
        id_tienda: String,
        uri: Uri?,
        context: Context,
        checkFinish: () -> Unit
    ) {

        val storage = FirebaseStorage.getInstance().reference

        if (uri != null) {

            val bytes = procesarImagenParaWhatsappDB(context, uri)

            val nombreArchivo = "bot_${System.currentTimeMillis()}.jpg"

            val ref = storage.child("tiendas/$id_tienda/imagenes/para_whatsapp/$nombreArchivo")

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            ref.putBytes(bytes, metadata)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Error subiendo imagen")
                    }
                    ref.downloadUrl
                }
                .addOnSuccessListener { downloadUrl ->

                    Log.d(TAG, "✅ Imagen subida: $downloadUrl")

                    val firestoreRef = FirebaseFirestore.getInstance()
                        .collection("lugares")
                        .document(id_tienda)

                    // 🔥 GUARDAR EN MAPA img_tienda → imagen_bot
                    val updateData = mapOf(
                        "imagen_bot" to downloadUrl.toString()
                    )

                    firestoreRef.update(updateData)
                        .addOnSuccessListener {
                            Log.d(TAG, "✅ URL guardada en Firestore")
                            checkFinish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Error guardando en Firestore", e)
                            checkFinish()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Error subiendo imagen", e)
                    checkFinish()
                }
        } else {
            Log.e(TAG, "❌ URI es null")
            checkFinish()
        }
    }

    @Composable
    fun Box_para_imagen_general_de_Bot_whatsapp(
        imagen_subida_correctamente: Boolean,
        subiendo_imagen: Boolean,
        imagenInicial: String?,
        onImagenChange: (Uri?) -> Unit,
        usuario_borro_los_cambios: () -> Unit
    ) {

        // 🔥 estado visual sincronizado con backend
        var imagenActual by remember(imagenInicial) { mutableStateOf(imagenInicial) }

        // 🔥 historial
        val historial = remember { mutableStateListOf<String?>() }

        // 🔥 limpiar historial cuando se sube correctamente
        LaunchedEffect(imagen_subida_correctamente) {
            if (imagen_subida_correctamente) {
                historial.clear()
            }
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {
                historial.add(imagenActual)
                imagenActual = it.toString()

                onImagenChange(it)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {

            SubcomposeAsyncImage(
                model = imagenActual ?: "",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        enabled = !subiendo_imagen,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        launcher.launch("image/*")
                    },
                contentScale = ContentScale.Crop,
                loading = { PlaceholderInterno() },
                error = { PlaceholderInterno() }
            )

            // 🔥 BOTÓN RETROCEDER
            if (!imagen_subida_correctamente && !subiendo_imagen && historial.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable {
                            val anterior = historial.removeLast()
                            imagenActual = anterior

                            onImagenChange(anterior?.let { Uri.parse(it) })
                            usuario_borro_los_cambios()
                        }
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    Text("↩", color = Color.White)
                }
            }

            // 🔥 OVERLAY MIENTRAS SUBE
            if (subiendo_imagen) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    @Composable
    fun estadisticas_aplicables(
        mostrar_qr_externo: Boolean,
        List_float: List<Float>,
        ListString: List<String>,
        colores_lista: List<Color>, contenido_clikeado: (String) -> Unit
    ) {
        AnimatedVisibility(
            visible = mostrar_qr_externo
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {
                    itemsIndexed(
                        List_float
                    ) { index, value ->
                        if (value.toInt() != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    val datos = ListString[index]
                                    contenido_clikeado(datos)


                                }
                            ) {
                                Box(
                                    Modifier
                                        .size(
                                            12.dp
                                        )
                                        .background(
                                            color = colores_lista[index],
                                            shape = CircleShape
                                        )
                                )

                                spacer_horizonta(
                                    8.dp
                                )

                                texto_generico_one_line(
                                    "${ListString[index]}: ${value.toInt()}",
                                    MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                spacer_vertical(
                    10.dp
                )
                PieChart(
                    dataSet = List_float.toChartDataSet(
                        labels = ListString,
                        title = "",
                        postfix = ""
                    ),
                    style = PieChartDefaults.style(
                        donutPercentage = 40f,
                        pieColors = colores_lista
                    )
                )
            }
        }
    }
}
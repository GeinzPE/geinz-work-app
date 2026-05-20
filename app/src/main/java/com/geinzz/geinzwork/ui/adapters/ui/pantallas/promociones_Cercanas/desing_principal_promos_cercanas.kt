package com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex

import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.carta_promociones_geinz_vista_previa
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.compartir_contacto_pulicaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.tiempoRestante
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.promoEstaExpirada
import com.geinzz.geinzwork.ui.adapters.ui.BotonCompartirReddit
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoExpandibleSuave
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.PegasooPagerIndicator
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen_promociones
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import kotlinx.coroutines.delay

@Composable
fun desing_principal_promos_cerncas(
    compartir: Boolean, contacto_directo: Boolean,
    onDismiss: () -> Unit,
    list: List<Uri>,
    texto_promo: String,
    titulo_promo: String,
    i: compartir_contacto_pulicaciones
) {
    Log.d("parmotrospoados", "$i $list")
    var mostrarLoader by remember { mutableStateOf(true) }

    var feedVisible by remember {
        mutableStateOf<List<dataclass_promociones_cerca_de_ti>>(emptyList())
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { feedVisible.size }
    )

    LaunchedEffect(Unit) {

        delay(3000) // ⏱️ 3 segundos
        mostrarLoader = false
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AnimatedVisibility(
                    visible = !mostrarLoader,
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {

                    ZoomableGalleryFullScreen_promociones_vista_previa(
                        compartir, contacto_directo,
                        i = i,
                        titulo = titulo_promo,
                        txt = texto_promo,
                        imagenes = list,
                        startIndex = 0,
                        onDismiss = {
                            onDismiss()
                        },

                        )


                }
                // ---------- LOADER OVERLAY ----------
                AnimatedVisibility(
                    visible = mostrarLoader,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ShimmerImagenConMarca()
                }
            }
        }


    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableGalleryFullScreen_promociones_vista_previa(
    compartir: Boolean, contacto_directo: Boolean,
    i: compartir_contacto_pulicaciones,
    titulo: String, txt: String,
    imagenes: List<Uri>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,

    ) {
    val (valorRestante, tipo) = parseDiasHorasRestantes(i.dias_restantes)
    var layoutReady by remember { mutableStateOf(false) }

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
    val color_bottom = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.01f),
        Color.Black.copy(alpha = 0.25f),
        Color.Black.copy(alpha = 0.40f),
        Color.Black.copy(alpha = 0.65f), // arriba suave
        Color.Black.copy(alpha = 0.85f),  // abajo oscuro
        Color.Black.copy(alpha = 1f)
    )
    val color_top = listOf(
        Color.Black.copy(alpha = 1f),
        Color.Black.copy(alpha = 0.85f),
        Color.Black.copy(alpha = 0.65f),
        Color.Black.copy(alpha = 0.40f),
        Color.Black.copy(alpha = 0.25f),
        Color.Black.copy(alpha = 0.01f),
        Color.Transparent,
    )
    if (imagenes.isEmpty()) return
    val context = LocalContext.current

    val pagerState = com.google.accompanist.pager.rememberPagerState(initialPage = startIndex)
    var allowScroll by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 25.dp, bottom = 10.dp)
    ) {


        com.google.accompanist.pager.HorizontalPager(
            state = pagerState,
            count = imagenes.size,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (layoutReady) 1f else 0f)
                .onGloballyPositioned {
                    layoutReady = true
                },
            userScrollEnabled = allowScroll
        ) { page ->

            val zoomState = rememberZoomState()
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagenes[page])
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build()
            )
            val state = painter.state
            ZoomImage(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
                contentScale = ContentScale.Fit
            )

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = color_top
                    )
                )
                .height(100.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                )

                // Texto centrado REAL
                if (imagenes.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} de ${imagenes.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Botón cerrar (X)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.End)
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = color_bottom
                        )
                    )
            )

            Column(modifier = Modifier.padding(10.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(
                            context
                        )
                            .data(i.logo_img)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {

                            },
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = i.nombre_tienda.capitalizeFirst(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                spacer_vertical(10.dp)
                Text(
                    text = titulo.capitalizeFirst(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                spacer_vertical(5.dp)
                TextoExpandibleSuave(
                    texto = txt,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                spacer_vertical(5.dp)
                if (imagenes.size > 1) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PegasooPagerIndicator(
                            pageCount = imagenes.size,
                            currentPage = pagerState.currentPage,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        )
                    }
                }

                spacer_vertical(5.dp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${i.dias_restantes}",
                        fontSize = 12.sp,
                        color = backgroundColor
                    )
                    spacer_horizonta(10.dp)
                    Spacer(modifier = Modifier.weight(1f))
                    if (contacto_directo) {
                        BotonCompartirReddit(
                            icon = R.drawable.icono_whatsapp_blanco_tasns,
                            descripcion = "contactados",
                            contador = "100",
                            onClick = {

                            })
                    }
                    if (compartir) {
                        BotonCompartirReddit(
                            icon = R.drawable.comparir_icon,
                            descripcion = "compartidos",
                            contador = "100",
                            onClick = {

                            })
                    }
                }
            }
        }
    }

}


@Composable
fun DialogVistaPreviaPromocion(
    show: Boolean,
    i: carta_promociones_geinz_vista_previa,
    onDismiss: () -> Unit
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {

            // 🟢 CONTENEDOR DEL DIALOG (SIN PADDING)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // ← opcional, quítalo si lo quieres full
                shape = RoundedCornerShape(20.dp),
                color = Color.Black
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                )
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onDismiss()
                            }
                    ) {
                        GaleriaHorizontalInstagram_promos_cercanas(
                            imagenes = i.lista_img_uri,
                            modifier = Modifier.fillMaxSize(),
                            img_clikeble_valor = {}
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(i.logo_img)
                                .placeholder(R.drawable.cargando_img_categorias)
                                .error(R.drawable.cargando_img_categorias)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = i.nombre_tienda.capitalizeFirst(),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )

                            if (i.titulo_publicacion.isNotEmpty()) {
                                Text(
                                    text = i.titulo_publicacion.capitalizeFirst(),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.LightGray
                                )
                            }

                            Text(
                                text = "${i.dias_restantes}",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            if (i.compartir) {
                                Icon(
                                    painterResource(R.drawable.comparir_icon),
                                    contentDescription = "Compartir",
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.White
                                )
                            }

                            if (i.contactar) {
                                Icon(
                                    painterResource(R.drawable.whatsapp_icon),
                                    contentDescription = "WhatsApp",
                                    modifier = Modifier.size(30.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GaleriaHorizontalInstagram_promos_cercanas(
    imagenes: List<Uri>,
    modifier: Modifier = Modifier,
    img_clikeble_valor: (Int) -> Unit,

    ) {
    val pagerState = rememberPagerState { imagenes.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            AsyncImage(
                model = imagenes[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        indication = null, // opcional (sin ripple)
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            img_clikeble_valor(page)
                        },
                        onLongClick = {

                        }),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.cargando_img_categorias),
                error = painterResource(R.drawable.cargando_img_categorias)
            )
        }
//
//        if (imagenes.size > 1) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(10.dp)
//                    .zIndex(1f) // 🔥 ESTO ES LO QUE FALTABA
//                    .background(
//                        Color.Black.copy(alpha = 0.65f),
//                        RoundedCornerShape(12.dp)
//                    )
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            ) {
//                Text(
//                    text = "${pagerState.currentPage + 1}/${imagenes.size}",
//                    color = Color.White,
//                    style = MaterialTheme.typography.labelSmall
//                )
//            }
//        }

    }
}

@Composable
fun LoadingOutlinedField(
    loading: Boolean,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_border")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .border(
                width = if (loading) 2.dp else 1.dp,
                color = if (loading)
                    primaryColor.copy(alpha = alpha)
                else
                    Color.LightGray,
                shape = RoundedCornerShape(50)
            )
    ) {
        content()
    }
}
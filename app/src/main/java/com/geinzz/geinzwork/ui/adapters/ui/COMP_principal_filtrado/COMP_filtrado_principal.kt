package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado


import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.principal.texto_encimado_cartas
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textosTituloGeinzWork


@Composable
fun custom_texFiel(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    placeholderText: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        label = { retornar_pleaceholder_label(labelText) },
        placeholder = { retornar_pleaceholder_label(placeholderText) },
        trailingIcon = trailingIcon,
        textStyle = MaterialTheme.typography.bodyMedium,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun existencia_dato() {
    Text(
        "No hay coincidencias ingrese otra palabra",
        color = Color.Red,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(5.dp)
    )
}


@Composable
fun estados_tiendas(estado: String, color_estado: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = estado, color = color_estado, style = MaterialTheme.typography.bodyMedium)
        spacer_horizonta(5.dp)
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color_estado)
        )
    }
}

@Composable
fun tags_subcateogiras(lista_tags: List<String>) {
    LazyRow(modifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures { change, dragAmount ->
            change.consume()
        }
    }, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        items(lista_tags) { cap ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = cap,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

    }
}

@Composable
fun cargando_progess_mas_texto(text: String) {

    Row(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
fun ColumnContenedorComun(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}


@Composable
fun texto_activos_encontrados(modifier: Modifier, texto: String, p_horizontal: Dp, color: Color) {
    Text(
        text = texto,
        modifier = modifier.padding(p_horizontal),
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}


@Composable
fun floatin_actionButton(
    modifier: Modifier,
    drawable: Int,
    colorFilter: ColorFilter? = ColorFilter.tint(Color.White),
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = {
            onClick()
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        containerColor = MaterialTheme.colorScheme.primary,

        ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = drawable),
            contentDescription = "Icono", colorFilter = colorFilter
        )
    }
}

@Composable
fun retornar_pleaceholder_label(texto: String, color: Color? = null) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = color ?: LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    )
}

@Composable
fun texto_generico_one_line(
    texto: String,
    style: TextStyle = MaterialTheme.typography.titleMedium, modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun texto_generico_multilinea(
    texto: String,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    modifier: Modifier = Modifier,
    Color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = texto,
        modifier = modifier,
        style = style,
        color = Color,
    )
}


@Composable
fun titulos_genericos_one_line(
    texto: String,
    style: TextStyle = MaterialTheme.typography.titleMedium, modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun Cartas_expandibles(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
        ) {
            content()
        }
    }

}

@Composable
fun expandibles_wrapp(
    texto_params: String,
    iconRes: Int? = null,           // para R.drawable
    iconVector: ImageVector? = null, // para Material Icons
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
            modifier = Modifier.constrainAs(texto) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) {
            when {
                iconRes != null -> {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                iconVector != null -> {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = texto_params,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
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
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(
                    constantes_lista_localidades.cambiar_icono_exapndible(expandido)
                ),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
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
fun generar_qr_ubi_tinda(
    bottom_text: String,
    content: String,
    sizeDp: Int = 200,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberSaveable(content) {
        val dimension = 512
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, dimension, dimension)
        val bmp = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        for (x in 0 until dimension) {
            for (y in 0 until dimension) {
                val color =
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                bmp.setPixel(x, y, color)
            }
        }
        bmp
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(30f)),
            contentScale = ContentScale.Fit

        )
        spacer_vertical(10.dp)
        texto_generico_multilinea(
            bottom_text,
            MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun btn_clasico_shap_50f(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }, modifier = Modifier, shape = RoundedCornerShape(40)) {
        Text(
            text,
            color = Color.White, style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun TextoSubrayado(
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier
) {
    Text(
        text = texto,
        modifier = modifier,
        textDecoration = TextDecoration.Underline,
        color = MaterialTheme.colorScheme.onBackground,
        style = style,
    )
}

@Composable
fun rutas_turismo(
    img_baner: String,
    texto_button: String,
    texto_baner: String,
    clik_button: () -> Unit
) {
    spacer_vertical(10.dp)
    Box() {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img_baner)
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(5)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5))
                .background(Color.Black.copy(alpha = 0.5f))
                .fillMaxWidth()
                .height(400.dp)
        ) {
        }
        texto_encimado(
            modifier = Modifier.align(Alignment.BottomStart),
            texto_button,
            texto_baner
        ) { clik_button() }
    }
}


@Composable
fun texto_encimado(
    modifier: Modifier,
    texto_button: String,
    texto_apartado: String,
    onClick: () -> Unit
) {
    Column(modifier = modifier.padding(10.dp)) {
        texto_generico_multilinea(
            texto_apartado.uppercase(),
            MaterialTheme.typography.banerGeinzWork
        )
        spacer_vertical(10.dp)
        androidx.compose.material3.Button(
            onClick = { onClick() },
            modifier = Modifier.clip(RoundedCornerShape(50))
        ) {
            texto_generico_one_line(texto_button, MaterialTheme.typography.titleSmall)
        }
    }

}

@Composable
fun titulo_referenciales_geinz_work(texto: String, texto_subrallado: String, listener: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        texto_generico_one_line(
            texto,
            MaterialTheme.typography.textosTituloGeinzWork,
            modifier = Modifier.weight(1f)
        )
        TextoSubrayado(
            texto_subrallado,
            MaterialTheme.typography.bodySmall,
            modifier = Modifier.clickable { listener() })

    }

}

@Composable
fun mascara_img(rounder: Int, alto: Dp, ancho: Dp) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(rounder))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black
                    )
                )
            )
            .width(ancho)
            .height(alto)
    )
}


@Composable
fun carta_turismo_google_mpa(
    id_lugar: String,
    latitud: Double,
    longitud: Double,
    img_ref: String,
    titulo: String,
    datos_descripcion: String,
    seleccionado: Boolean,
    onClick: (id: String, lat: Double, log: Double) -> Unit,
) {
    val targetColor = if (seleccionado) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(id_lugar, latitud, longitud)
            },
        colors = CardDefaults.cardColors(
            containerColor = animatedColor
        )
    ) {
        Row {
            img_carta_google_maps(img_ref)
            datos_lugares_google_maps(titulo, datos_descripcion)
        }
    }
}

@Composable
fun img_carta_google_maps(img: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(img)
            .size(80, 80)
            .crossfade(true)
            .placeholder(R.drawable.cargando_img_categorias)
            .error(R.drawable.sin_item_carrito)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(10)),
//                .clickable {
//                    listener(true, lugar)
//                },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun datos_lugares_google_maps(texto: String, descripcion: String) {
    Column(modifier = Modifier.padding(start = 10.dp, end = 20.dp, top = 5.dp, bottom = 5.dp)) {
        texto_generico_one_line(texto = texto, MaterialTheme.typography.titleLarge)
        spacer_vertical(5.dp)
        text_expandible_wrapp(descripcion, maxlines = 2)
    }
}


@Composable
fun carta_filtrado_localidades(
    defecto_selecionado: Boolean,
    nombre_localidad: String,
    img: List<String>,
    rounder: Int,
    alto: Dp,
    ancho: Dp,
    listener: (String) -> Unit
) {
    val randomImg = remember(img) { img.randomOrNull() }
    val borderColor by animateColorAsState(
        if (defecto_selecionado) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )
    Box(
        modifier = Modifier
            .width(ancho)
            .height(alto)
            .clip(RoundedCornerShape(rounder))
            .clickable {
                listener(nombre_localidad)
            }
//            .border(
//                width = 1.dp,
//                color = borderColor,
//                shape = RoundedCornerShape(rounder)
//            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(randomImg)
                .size(ancho.value.toInt(), alto.value.toInt())
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.sin_item_carrito)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(ancho)
                .height(alto)
                .clip(RoundedCornerShape(rounder)),
            contentScale = ContentScale.Crop
        )
        mascara_img(rounder, alto, ancho)
        val titulo = if (defecto_selecionado) {
            "Estás aquí 👋"
        } else {
            "explorar"
        }
        texto_encimado_cartas(
            defecto_selecionado,
            modifier = Modifier.align(Alignment.BottomStart),
            nombre_localidad.uppercase(),
            titulo.uppercase(),
        )


    }
}

@Composable
fun localidad_Selecionada(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cargando_categorias))

    Box(
        modifier = modifier
            .height(25.dp), // altura constante
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = 3,
            modifier = modifier
                .size(30.dp)
                .offset(y = (-5).dp) // sube 2dp
        )

    }
}


@Composable
fun open_map_perzonlizado(modifier: Modifier, tipo: String, abrir_mapa: (String) -> Unit) {
    val context = LocalContext.current
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            abrir_mapa(tipo)
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    androidx.compose.material3.Button(modifier = modifier, onClick = {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            abrir_mapa(tipo)
        } else {
            permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }) {
        texto_generico_one_line("Ver en mapa")
    }
}





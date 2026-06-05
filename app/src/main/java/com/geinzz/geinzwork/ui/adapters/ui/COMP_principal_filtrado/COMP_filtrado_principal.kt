package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado


import android.Manifest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.PreciosApp
import com.geinzz.geinzwork.data.model.datos_grafico
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.datos_tienda_fechas
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.widget_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.Descuentos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_cantidad_slado_geinz
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_renovar_plan
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.eres_socio_geinz
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.item_metodos_de_pago
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.FondoIAAnimado
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.textosTituloGeinzWork
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.HorarioSemanal123
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.abreviarNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.construirBloques
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.motivos
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerDiasYColor
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.onSwitchChange
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_shadow_bottom_sheet_default
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_subcategoria_shadow
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.start_shadow_bottom_sheet_default
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import com.geinzz.geinzwork.viewModels.viewmodel_recargas
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder



@Composable
fun custom_texFiel(
    rounder: Int = 50,
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
        shape = RoundedCornerShape(rounder),
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
            unfocusedBorderColor = Color(0xFF75707A),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun custom_textField_150(modifier: Modifier= Modifier,
    mostrar_contado_palabras: Boolean=true,
    rounder: Int = 50,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    placeholderText: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {

    val maxLength = 200
    val isOverLimit = value.length > maxLength

    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(rounder),
            label = { retornar_pleaceholder_label(labelText) },
            placeholder = { retornar_pleaceholder_label(placeholderText) },
            trailingIcon = trailingIcon,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = Color(0xFF75707A),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
if(mostrar_contado_palabras){

        // 📊 contador de caracteres
        Text(
            text = "${value.length}/$maxLength",
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 12.dp, top = 2.dp),
            color = if (isOverLimit) Color.Red else Color.Gray,
            style = MaterialTheme.typography.labelSmall
        )
}
    }
}

@Composable
fun custom_textField_readonly(
    modifier: Modifier = Modifier,
    rounder: Int = 50,
    value: String,
    labelText: String,
    placeholderText: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(rounder),
        label = { retornar_pleaceholder_label(labelText) },
        placeholder = { retornar_pleaceholder_label(placeholderText) },
        textStyle = MaterialTheme.typography.bodyMedium,
        readOnly = true,
        enabled = false, // 🔥 desactiva teclado y foco completamente
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onBackground,
            disabledBorderColor = Color(0xFF75707A),
            disabledLabelColor = MaterialTheme.colorScheme.onBackground,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onBackground,
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
fun tags_subcateogiras(
    lista_tags: List<String>, modifier: Modifier = Modifier, brush_start: Brush, brush_end: Brush
) {
    val listState = rememberLazyListState()

    val showLeftShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val showRightShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < total - 1
        }
    }
    // 🔥 animar alpha, no crear/destruir Box
    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400),
        label = "alphaLeft"
    )
    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400),
        label = "alphaRight"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(25.dp), contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = listState,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(lista_tags) { cap ->

                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = cap.capitalizeFirst(),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        // 👈 izquierda
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .clip(CircleShape)
                .align(Alignment.CenterStart)
                .zIndex(1f)
                .alpha(alphaLeft)
//                .background(Brush.horizontalGradient(colors = shadow_left))
                .background(brush_start)
        )

        // 👉 derecha
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .clip(CircleShape)
                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .alpha(alphaRight)
//                .background(Brush.horizontalGradient(colors = shadow_right))
                .background(brush_end)
        )
    }
}

@Composable
fun ShadowTagsCategoriasstart(modifier: Modifier) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(25.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 1f),   // negro sólido
                        Color.Black.copy(alpha = 0.6f), // intermedio
                        Color.Black.copy(alpha = 0.3f), // más suave
                        Color.Transparent               // totalmente transparente
                    )
                )
            )
    )
}

@Composable
fun ShadowTagsCategoriassEnd(modifier: Modifier) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(25.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,               // centro transparente
                        Color.Black.copy(alpha = 0.3f), // más suave
                        Color.Black.copy(alpha = 0.6f), // intermedio
                        Color.Black.copy(alpha = 1f)    // negro sólido
                    )
                )
            )
    )
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
            modifier = Modifier.size(20.dp), strokeWidth = 2.dp
        )
    }
}

@Composable
fun  ColumnContenedorComun(
    modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp, bottom = 10.dp),
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
            defaultElevation = 6.dp, pressedElevation = 10.dp
        ),
        containerColor = MaterialTheme.colorScheme.primary,

        ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = drawable),
            contentDescription = "Icono",
            colorFilter = colorFilter
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
    style: TextStyle = MaterialTheme.typography.titleMedium,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    FuenteControladaApp {
        Text(
            text = texto,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun texto_generico_one_line_Expandible(
    texto: String,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    expandir: Boolean = false
) {
    Log.d("expanire","$expandir")
    FuenteControladaApp {
        Text(
            text = texto,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = if (expandir) Int.MAX_VALUE else 1,
            overflow = if (expandir) TextOverflow.Clip else TextOverflow.Ellipsis
        )
    }
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
fun Cartas_expandibles(color: Color =MaterialTheme.colorScheme.surface,
                       modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
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
    texto_params: String, iconRes: Int? = null,           // para R.drawable
    iconVector: ImageVector? = null, // para Material Icons
    expandido: Boolean, onClickExpand: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp)
    ) {
        val (texto, btn) = createRefs()

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.constrainAs(texto) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }) {
            when {
                iconRes != null -> {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onBackground,
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
                defaultElevation = 6.dp, pressedElevation = 10.dp
            ),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Image(
                modifier = Modifier.size(20.dp), painter = painterResource(
                    constantes_lista_localidades.cambiar_icono_exapndible(expandido)
                ), contentDescription = "", colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}







@Composable
fun campos_datos_graficos(item: datos_grafico) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape),
            painter = painterResource(item.img_),
            contentDescription = "Google maps",
        )

        texto_generico_one_line(
            "${item.label}", MaterialTheme.typography.bodyMedium
        )
        texto_generico_one_line(
            ": ${item.cantidad}", MaterialTheme.typography.bodyMedium
        )


    }


}


@Composable
fun text_expandible_wrapp(
    modifier: Modifier = Modifier,
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxlines: Int = 1
) {
    Text(
        modifier = modifier,
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        style = style,
        maxLines = maxlines,
        overflow = TextOverflow.Ellipsis

    )
}

@Composable
fun TextoExpandibleSuave(
    texto: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = 2
) {
    var expanded by remember { mutableStateOf(false) }
    var canExpand by remember { mutableStateOf(false) }

    Column(
        modifier = modifier

    ) {

        Text(
            text = texto,
            style = style,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout ->
                // 🔒 solo lo calculamos una vez
                if (!expanded) {
                    canExpand = layout.hasVisualOverflow
                }
            },  modifier = Modifier
                .padding(top = 6.dp)
                .clickable (indication = null, interactionSource = remember { MutableInteractionSource() }){ expanded = !expanded },
        )

    }
}




@Composable
fun btn_clasico_shap_50f(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }, modifier = Modifier, shape = RoundedCornerShape(40)) {
        Text(
            text, color = Color.White, style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun TextoSubrayado(
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color_subrallado: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = texto,
        modifier = modifier,
        textDecoration = TextDecoration.Underline,
        color = color_subrallado,
        style = style
    )
}

@Composable
fun rutas_turismo(
    img_baner: String, texto_button: String, texto_baner: String, clik_button: () -> Unit,eliminarerr:(String)-> Unit
) {
    spacer_vertical(10.dp)
    Box() {
        ImagenSuave(
            url = img_baner,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(5.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { clik_button() },
            eliminarerr = {img->
                eliminarerr(img)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp * 0.6f)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33000000),
                            Color(0x66000000),
                            Color(0xDD000000)
                        )
                    )
                )
        )
        texto_encimado(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            texto_button,
            texto_baner
        ) { clik_button() }
    }
}


@Composable
fun ImagenSuave(
    url: String,
    modifier: Modifier = Modifier,
    eliminarerr: (String) -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    Box(modifier = modifier) {

        // Placeholder
        if (!isLoaded) {
            Image(
                painter = painterResource(R.drawable.cargando_img_categorias),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Imagen real
        AsyncImage(
            model = url,
            contentDescription = null,
            onSuccess = { isLoaded = true },
            onError = {
                eliminarerr(url)
            },
            modifier = Modifier
                .matchParentSize()
                .alpha(alphaAnim),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun seguridad(
    drawable: Int, texto_button: String, texto_baner: String, clik_button: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(5))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(drawable)
                .memoryCachePolicy(CachePolicy.ENABLED).diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true).placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias).build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { clik_button() },
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp * 0.6f)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,          // arriba: totalmente transparente
                            Color(0x33000000),          // negro muy suave
                            Color(0x66000000),          // negro semi-transparente
                            Color(0xDD000000)           // negro más oscuro abajo
                        )
                    )
                )
        )


        // Texto encima de la máscara
        texto_encimado(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            texto_button,
            texto_baner
        ) { clik_button() }
    }
}


@Composable
fun texto_encimado(
    modifier: Modifier, texto_button: String, texto_apartado: String, onClick: () -> Unit
) {
    Column(modifier = modifier.padding(start = 10.dp, end = 70.dp, bottom = 10.dp)) {
        texto_generico_multilinea(
            texto_apartado, MaterialTheme.typography.banerGeinzWork
        )
        spacer_vertical(10.dp)
        Button(
            onClick = { onClick() },
            modifier = Modifier.clip(RoundedCornerShape(50)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
            )
        ) {
            texto_generico_one_line(
                texto_button, MaterialTheme.typography.titleSmall, color = Color(0xFF8700F3)
            )
        }
    }

}

@Composable
fun titulo_referenciales_geinz_work(texto: String, texto_subrallado: String, listener: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
//        texto_generico_one_line(
//            texto, MaterialTheme.typography.textosTituloGeinzWork, modifier = Modifier.weight(1f)
//        )
        Text(
            texto,
            fontFamily = baners_geinz_work,
            color = Color.White,
            fontSize = 17.sp,
            modifier = Modifier.weight(1f)
        )
        TextoSubrayado(
            texto = texto_subrallado,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.clickable { listener() })

    }

}

@Composable
fun mascara_img(rounder: Int, alto: Dp, ancho: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(rounder))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,                   // negro sólido abajo
                        Color.Black.copy(alpha = 0.7f),// transición
                        Color.Transparent              // transparente arriba
                    ), startY = Float.POSITIVE_INFINITY, // fuerza el gradiente desde abajo
                    endY = 0f
                )
            )
            .width(ancho)
            .height(alto)
    )
}


@Composable
fun carta_turismo_google_mpa(
    index: Int,
    id_lugar: String,
    latitud: Double,
    longitud: Double,
    img_ref: String,
    titulo: String,
    datos_descripcion: String,
    seleccionado: Boolean,
    onClick: (id: String, lat: Double, log: Double) -> Unit,
) {
    val heightOptions = listOf(200.dp, 250.dp)
    val boxHeight = if (index % 2 == 0) heightOptions[0] else heightOptions[1]
    var mostrar_overlay by remember { mutableStateOf(false) }

    if (seleccionado) {
        mostrar_overlay = false
    } else {
        mostrar_overlay = true
    }

//    val animatedColor by animateColorAsState(
//        targetValue = targetColor,
//        animationSpec = tween(durationMillis = 500)
//    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(10))
            .clickable {
                onClick(id_lugar, latitud, longitud)
            },

        ) {
        img_carta_google_maps(img_ref)
        AnimatedVisibility(
            !mostrar_overlay, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(
                Alignment.BottomCenter
            )
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .background(brush = Brush.verticalGradient(end_subcategoria_shadow))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    datos_lugares_google_maps(titulo, datos_descripcion)
                }
            }
        }
        AnimatedVisibility(mostrar_overlay, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeight)
                    .clip(RoundedCornerShape(10))
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {}
        }

    }
}

@Composable
fun img_carta_google_maps(img: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(img)
            .placeholder(R.drawable.cargando_img_categorias)
            .error(R.drawable.cargando_img_categorias).build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
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
        texto_generico_one_line(
            texto = texto,
            MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        spacer_vertical(5.dp)
//        text_expandible_wrapp(descripcion, maxlines = 2)
    }
}


//@Composable
//fun carta_filtrado_localidades(
//    defecto_selecionado: Boolean,
//    nombre_localidad: String,
//    img: List<String>,
//    rounder: Int,
//    alto: Dp,
//    ancho: Dp,
//    listener: (String) -> Unit
//) {
//    val randomImg = remember(img) { img.randomOrNull() }
//    val borderColor by animateColorAsState(
//        if (defecto_selecionado) MaterialTheme.colorScheme.primary else Color.Transparent,
//        animationSpec = tween(durationMillis = 300)
//    )
//
//    // El Box principal no necesita recorte
//    Box(
//        modifier = Modifier
//            .width(ancho)
//            .height(alto)
//            .clickable { listener(nombre_localidad) }
//    ) {
//        // 👇 Recorta la imagen
//        AsyncImage(
//            model = randomImg ?: R.drawable.sin_item_carrito,
//            contentDescription = nombre_localidad,
//            modifier = Modifier
//                .fillMaxSize()
//                .maskClip(MaterialTheme.shapes.extraLarge),
//            contentScale = ContentScale.Crop
//        )
//
//        // 👇 Recorta el degradado
//        Box(
//            modifier = Modifier
//                .matchParentSize()
//                .clip(RoundedCornerShape(rounder.dp))
//                .background(
//                    Brush.verticalGradient(
//                        listOf(
//                            Color.Transparent,
//                            Color(0x66000000),
//                            Color(0xEE000000)
//                        )
//                    )
//                )
//        )
//
//        // Texto encima
//        val titulo = if (defecto_selecionado) "Estás aquí 👋" else "Explorar"
//        texto_encimado_cartas(
//            defecto_selecionado,
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .padding(12.dp),
//            nombre_localidad.uppercase(),
//            titulo.uppercase(),
//        )
//    }
//}


@Composable
fun localidad_Selecionada(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cargando_categorias))

    Box(
        modifier = modifier.height(25.dp), // altura constante
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
fun open_map_perzonlizado(
    modifier: Modifier = Modifier, tipo: String, abrir_mapa: (String) -> Unit
) {
    val context = LocalContext.current

    // Launcher para pedir permiso
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            abrir_mapa(tipo)
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = modifier.size(56.dp), // 👈 igual al tamaño estándar FAB
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 6.dp // 👈 igual que FAB con sombra
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        abrir_mapa(tipo)
                    } else {
                        permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }, contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = "Mapa",
                tint = Color.White,
                modifier = Modifier.size(24.dp) // 👈 tamaño estándar del ícono en FAB
            )
        }
    }
}


//@Composable
//fun cartas_explorar_tienda(localidad_selecionadad: String, datos: List<dataclass_cat_sub>) {
//    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//        items(datos) { it ->
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .clip(RoundedCornerShape(10))
//                    .background(MaterialTheme.colorScheme.surface)
//                    .width(300.dp)
//                    .height(85.dp)
//            ) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(it.lista_img)
//                        .size(80, 85)
//                        .crossfade(true)
//                        .placeholder(R.drawable.cargando_img_categorias)
//                        .error(R.drawable.cargando_img_categorias)
//                        .build(),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .width(60.dp)
//                        .height(85.dp)
//                        .clip(RoundedCornerShape(10)),
//                    contentScale = ContentScale.Crop
//                )
//                spacer_horizonta(10.dp)
//                Column() {
//                    texto_generico_one_line(
//                        texto = it.nombre.toString().capitalizeFirst(),
//                        MaterialTheme.typography.titleLarge
//                    )
//                    spacer_vertical(5.dp)
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Image(
//                            painter = painterResource(R.drawable.localidad_icon_general),
//                            modifier = Modifier.size(20.dp),
//                            contentDescription = ""
//                        )
//                        spacer_horizonta(5.dp)
//                        texto_generico_one_line(
//                            texto = localidad_selecionadad.capitalizeFirst(),
//                            MaterialTheme.typography.bodySmall
//                        )
//                    }
//                    spacer_vertical(5.dp)
//                    tags_subcateogiras(
//                        it.lista_subcategorias,
//                        modifier = Modifier.padding(end = 10.dp)
//                    )
//                }
//            }
//        }
//    }
//
//}
//
//@Composable
//fun btn_cerrado_overlay(onClick: () -> Unit) {
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .clickable(
//                indication = null,
//                interactionSource = remember { MutableInteractionSource() }) { onClick() }
//            .clip(CircleShape)
//            .background(MaterialTheme.colorScheme.surface)
//            .padding(horizontal = 20.dp, vertical = 5.dp)
//    ) {
//        Icon(
//            imageVector = Icons.Default.ExpandMore,
//            contentDescription = "Cerrar",
//            tint = Color.White,
//            modifier = Modifier
//                .size(40.dp)
//        )
//    }
//}

@Composable
fun btn_close_gris(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    size_container: Dp = 35.dp,
    size_icon: Dp = 12.dp,
    tint_icon: Color = Color.White,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size_container)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) {
                Log.d("realizaste", "click")
                onClick()
            }, contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .blur(12.dp)
        )
        // Icono
        Icon(
            imageVector = imageVector,
            contentDescription = "Cerrar",
            tint = tint_icon,
            modifier = Modifier.size(size_icon)
        )
    }
}


@Composable
fun chisp_filtrado_busqueda(
    carta_selecionada: Boolean,
    filtrado: String,
    btn_visible: Boolean = true,
    clik_card: () -> Unit,
    onClick_delete: () -> Unit,
    color_invertido: Boolean = false,
    alto: Dp = 45.dp,
) {
    val color_chips by animateColorAsState(
        targetValue = if (!carta_selecionada) MaterialTheme.colorScheme.primary
        else Color.White, animationSpec = tween(
            durationMillis = 500, easing = LinearOutSlowInEasing
        ), label = ""
    )
    val color_invertido_chips by animateColorAsState(
        targetValue = if (!carta_selecionada) {
            Color.White
        } else {
            MaterialTheme.colorScheme.primary
        }
    )


    val color_text = if (!carta_selecionada) Color.White else Color.Black
    val color_text_ivnertido =
        if (color_invertido && !carta_selecionada) Color.Black else Color.White

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (!color_invertido) color_chips else color_invertido_chips)
            .height(alto)
            .padding(horizontal = 15.dp, vertical = 10.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { clik_card() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        texto_generico_one_line(
            filtrado.capitalizeFirst(),
            color = if (!color_invertido) color_text else color_text_ivnertido,
            style = MaterialTheme.typography.bodyMedium
        )
        if (btn_visible) {
            if (carta_selecionada) {
                spacer_horizonta(7.dp)
                btn_close_gris(
                    imageVector = Icons.Default.Close,
                    onClick = { onClick_delete() },
                    size_container = 20.dp,
                    size_icon = 15.dp,
                    tint_icon = if (!carta_selecionada) Color.White else Color.Black
                )
            }
        }

    }

}

@Composable
fun chisp_filtrado_busqueda_con_la_IA(
    carta_selecionada: Boolean,
    filtrado: String,
    btn_visible: Boolean = true,
    clik_card: () -> Unit,
    onClick_delete: () -> Unit,
    alto: Dp = 45.dp,
) {

    val surfaceColor = MaterialTheme.colorScheme.surface

    // 🎨 Fondo base animado (siempre existe)
    val colorBase by animateColorAsState(
        targetValue = if (carta_selecionada)
            surfaceColor.copy(alpha = 0.6f) // base debajo del fondo IA
        else
            surfaceColor, // 👈 chips normales
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        ),
        label = ""
    )

    val colorText = Color.White

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .height(alto)
            .background(colorBase)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                clik_card()
            }
    ) {

        // 🤖 Fondo IA animado SOLO cuando está seleccionado
        if (carta_selecionada) {
            FondoIAAnimado(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        }

        // 👉 Contenido del chip
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            texto_generico_one_line(
                filtrado.capitalizeFirst(),
                color = colorText,
                style = MaterialTheme.typography.bodyMedium
            )

            if (btn_visible && carta_selecionada) {
                spacer_horizonta(7.dp)
                btn_close_gris(
                    imageVector = Icons.Default.Close,
                    onClick = onClick_delete,
                    size_container = 20.dp,
                    size_icon = 15.dp,
                    tint_icon = colorText
                )
            }
        }
    }
}





@Composable
fun ImagenesSuperpuestasCollage(nombre_usuario: String, modifier: Modifier = Modifier) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(text = "Hola benjamin \uD83D\uDC4B", fontSize = 25.sp, fontFamily = baners_geinz_work)
        Box(
            modifier = modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8700F3).copy(alpha = 0.7f), Color.Transparent
                            ),
                        ), shape = RoundedCornerShape(200.dp)
                    )
            )

            // --- Foto 1 (Izquierda) ---
            ImagenConInclinacion(
                drawableResId = R.drawable.f5,
                anguloRotacion = -8f,
                desplazamientoX = -70.dp,
                desplazamientoY = 20.dp,
                null,
                {},
                true
            )

            // --- Foto 2 (Centro, la protagonista) ---
            ImagenConInclinacion(
                drawableResId = R.drawable.f2,
                anguloRotacion = 3f,
                desplazamientoX = 0.dp,
                desplazamientoY = 0.dp,
                null,
                {},
                true
            )

            // --- Foto 3 (Derecha) ---
            ImagenConInclinacion(
                drawableResId = R.drawable.f4,
                anguloRotacion = 7f,
                desplazamientoX = 70.dp,
                desplazamientoY = 40.dp,
                null,
                {},
                true
            )
        }
        fracescambiantes(nombre_usuario)
    }
}

@Composable
fun fracescambiantes(nombre_user: String) {
    // Construimos la lista incluyendo el saludo
    val fraces = listOf(
        "👋 Hola $nombre_user",
    ) + constantes_lista_localidades.lista_fraces_filtado

    var index by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            index = (index + 1) % fraces.size
        }
    }

    AnimatedContent(
        targetState = fraces[index], transitionSpec = {
            fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
        }, label = "frases"
    ) { txt ->
        Text(
            text = txt,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.animateContentSize()
        )
    }
}

//@Composable
//fun ImagenConInclinacion(
//    drawableResId: Int,
//    anguloRotacion: Float,
//    desplazamientoX: Dp = 0.dp,
//    desplazamientoY: Dp = 0.dp
//) {
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//
//    // 🔹 Calcula un tamaño proporcional al ancho, pero con límite
//    val tamaño = (screenWidth * 0.35f).coerceIn(100.dp, 160.dp)
//    // -> en celulares será ~130dp, en tablets nunca pasa de 160dp
//
//    Box(
//        modifier = Modifier
//            .size(tamaño) // 👈 controla el tamaño real
//            .offset(x = desplazamientoX, y = desplazamientoY)
//            .rotate(anguloRotacion)
//            .clip(RoundedCornerShape(12.dp))
//    ) {
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(drawableResId)
//                .placeholder(R.drawable.cargando_img_categorias)
//                .error(R.drawable.cargando_img_categorias)
//                .crossfade(false)
//                .build(),
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//}
@Composable
fun ImagenConInclinacion(
    drawableResId: Int,
    anguloRotacion: Float,
    desplazamientoX: Dp = 0.dp,
    desplazamientoY: Dp = 0.dp,
    factorTamaño: Float? = null,
    clikeable: (Boolean) -> Unit,
    mostrarMascara: Boolean = false  // 👈 tamaño opcional y responsivo
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // 🔹 Tamaño automático si no pasas nada (35% del ancho)
    val tamañoDefault = (screenWidth * 0.35f).coerceIn(100.dp, 160.dp)

    // 🔹 Si envías un factor, lo calculamos igual que el default
    val tamañoFinal = if (factorTamaño != null) {
        (screenWidth * factorTamaño).coerceIn(80.dp, 200.dp)
    } else {
        tamañoDefault
    }

    Box(
        modifier = Modifier
            .size(tamañoFinal)
            .offset(x = desplazamientoX, y = desplazamientoY)
            .rotate(anguloRotacion)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(drawableResId)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias).crossfade(false).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    clikeable(!mostrarMascara)
                })
        AnimatedVisibility(!mostrarMascara, enter = fadeIn(), exit = fadeOut()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)) // 👈 máscara elegante
            )
        }
    }
}


@Composable
fun ShadowBottomPantallas(listState: LazyListState, modifier: Modifier = Modifier) {
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha, animationSpec = tween(durationMillis = 500)
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent, Color.Black
                    )
                )
            )
            .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
    )
}


@Composable
fun baner_servicios_basicos_(texto1:String,descripcion:String,img:Int,listener_servicios: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1A1A))
            .fillMaxWidth()
            .defaultMinSize(minHeight = 180.dp)
            .clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }) {
                listener_servicios()
            }) {
        Row(
            modifier = Modifier.matchParentSize() // 🔹 el Row ocupa todo el Box
        ) {
            // === COLUMNA DE TEXTO Y BOTÓN ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = texto1,
                        color = Color.White,
                        fontFamily = baners_geinz_work,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )


                    spacer_vertical(10.dp)

                    texto_generico_multilinea(
                        descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        Color = Color.White
                    )
                }

                spacer_vertical(10.dp)
                // === BOTÓN FLECHA ===
                Box(
                    modifier = Modifier
                        .size(40.dp) // 🔹 tamaño fijo y respetado
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.25f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            listener_servicios()
                        }
                        .align(Alignment.Start), // evita que se estire horizontalmente
                    contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Ir",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                spacer_vertical(10.dp)
            }

            // === IMAGEN ===
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .memoryCachePolicy(CachePolicy.ENABLED).diskCachePolicy(CachePolicy.ENABLED)
                        .data(img).build(),

                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradiente para transición
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1A1A1A), Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

//
//@Composable
//fun baner_widget_tienda_geinz(
//    viewModel_filtado_tiendas: viewModel_filtado_tiendas,
//    item: widget_tienda,
//) {
//    val (dias, color) = obtenerDiasYColor(item.fecha_termino)
//
//    val tick by viewModel_filtado_tiendas.tick.collectAsState()
//
//    LaunchedEffect(item.id_tienda) {
//        viewModel_filtado_tiendas.calcularHorarioParaTienda(item.id_tienda,item.horario_tiendaMap)
//    }
//    val horario_hoy=viewModel_filtado_tiendas.horariosTiendas.collectAsState().value[item.id_tienda] ?: HorarioDia_box()
//
//    val hora_de_trabajo= constantes_horas.calcularHorasDiaLegible(horario_hoy)
//
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(20.dp))
//            .background(Color(0xFF1A1A1A))
//            .fillMaxWidth()
//            .defaultMinSize(minHeight = 180.dp)
//            .clickable(
//                indication = null,
//                interactionSource = remember { MutableInteractionSource() }
//            ) {
//                // Acción al hacer clic (opcional)
//            }
//    ) {
//        Row(modifier = Modifier.matchParentSize()) {
//
//            // Columna con texto e información
//            Column(
//                modifier = Modifier
//                    .weight(2f)
//                    .fillMaxHeight()
//                    .padding(12.dp)
//            ) {
//                texto_generico_one_line(item.nombre_tienda, style = MaterialTheme.typography.titleLarge)
//
//
//                texto_generico_one_line("Horas de trabajo $hora_de_trabajo")
//
//                    // Mostramos el horario calculado desde el ViewModel
//                    retornar_color_estado_tienda_Box(
//                        "",horario_hoy,
//                        tick,
//                        true,
//                        { color, txt -> /* callback si lo necesitas */ },
//                        true
//                    )
//
//
//                texto_generico_one_line(
//                    "Renovación: $dias días a renovar",
//                    color = color, style = MaterialTheme.typography.bodyMedium
//                )
//            }
//
//            // Imagen de la tienda con gradiente
//            Box(
//                modifier = Modifier
//                    .weight(1.5f)
//                    .fillMaxHeight()
//            ) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .memoryCachePolicy(CachePolicy.ENABLED)
//                        .diskCachePolicy(CachePolicy.ENABLED)
//                        .data(item.img_tienda)
//                        .build(),
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .width(80.dp)
//                        .align(Alignment.CenterStart)
//                        .background(
//                            Brush.horizontalGradient(
//                                colors = listOf(Color(0xFF1A1A1A), Color.Transparent)
//                            )
//                        )
//                )
//            }
//        }
//    }
//}


@Composable
fun baner_widget_tienda_geinz_baner(
    precio_moneda: Double,
    cargar_precio_activacione: PreciosApp?,
    viewmodel_recargas:viewmodel_recargas,
    switchActivo: Boolean,
    motivo_cierre: String,
    context: Context,
    isConnected: Boolean,
    viewmodel: viewmodel_eres_socio,
    item: widget_tienda,
    horario_hoy: HorarioDia_box,
    horas_de_trabajo: String,
    bloques_hoy: List<HorarioBloque>,
    tick: Long,
    swtch_motivocieere_activo_desactivado: (Boolean) -> Unit,
    retornar_motivo_cierre_vacio: (String) -> Unit,
    sin_activar_horario: () -> Unit,
    sin_acceso_motivo_cierre: () -> Unit,
    sin_acceso_horario: () -> Unit,
    mostar_panel_geinz: () -> Unit, sin_internet_al_renovar: () -> Unit
) {

    val fechaFin by item.fecha_fin_panel.collectAsState()
    val (dias, color) = obtenerDiasYColor(fechaFin)
    var color_estado by remember { mutableStateOf(Color(0XFF535252)) }
    var txt_estado_teinda by remember { mutableStateOf("") }

    var mostar_Bottom_shet_editar_horario by remember { mutableStateOf(false) }
    var datos_bottom_Shhet_editar_horario by remember() { mutableStateOf(datos_tienda()) }

    LaunchedEffect(item.id_tienda, item.horario_tiendaMap) {
        datos_bottom_Shhet_editar_horario = datos_tienda(
            nombre = item.nombre_tienda,
            id_tienda = item.id_tienda,
            horario_tiendaMap = item.horario_tiendaMap
        )
    }

    retornar_color_estado_tienda_Box(
        "", horario_hoy, tick, true, { color, txt ->
            color_estado = color
            txt_estado_teinda = txt
        }, false
    )

    val mostrarSelectorMotivo = switchActivo && motivo_cierre.isEmpty()
    val mostrarDatosCerrado = !switchActivo || motivo_cierre.isNotEmpty()

    var ya_esta_cerrado by remember { mutableStateOf(false) }
    var motivoSeleccionado by remember { mutableStateOf<String?>(null) }

    val bloqueManana = bloques_hoy.getOrNull(0)
    val bloqueTarde = bloques_hoy.getOrNull(1)

    val hAperturaAM = remember { mutableStateOf(bloqueManana?.h_apertura ?: "") }
    val hCierreAM = remember { mutableStateOf(bloqueManana?.h_cierre ?: "") }
    val hAperturaPM = remember { mutableStateOf(bloqueTarde?.h_apertura ?: "") }
    val hCierrePM = remember { mutableStateOf(bloqueTarde?.h_cierre ?: "") }

    var por_removar by remember { mutableStateOf(false) }

    val lista_descuentos = listOf(

        Descuentos(
            meses = "20 días",
            icono_descuento = null,
            descuento_off = "",
            precio_anterior = "",
            procentaje_ahorro = "",
            porcentaje_int = 0, meses_agregados = "20 días"
        ),
        Descuentos(
            meses = "1 mes",
            icono_descuento = Icons.Filled.LocalFireDepartment,
            descuento_off = "-5%off",
            precio_anterior =(cargar_precio_activacione?.planesActivacion["1_mes"] ?: 0).toString(),
            procentaje_ahorro = "5%",
            porcentaje_int = 5, "1 mes"
        ),

        Descuentos(
            meses = "2 meses",
            icono_descuento = Icons.Filled.LocalFireDepartment,
            descuento_off = "-10%off",
            precio_anterior = (cargar_precio_activacione?.planesActivacion["2_meses"] ?: 0).toString(),
            procentaje_ahorro = "10%",
            porcentaje_int = 10, "2 mes"
        ),

        Descuentos(
            meses = "3 meses",
            icono_descuento = Icons.Filled.LocalFireDepartment,
            descuento_off = "-20%off",
            precio_anterior = (cargar_precio_activacione?.planesActivacion["3_meses"] ?: 0).toString(),
            procentaje_ahorro = "20%",
            porcentaje_int = 20, "3 mes"
        ),

        Descuentos(
            meses = "4 meses",
            icono_descuento = Icons.Filled.LocalFireDepartment,
            descuento_off = "-30%off",
            precio_anterior = (cargar_precio_activacione?.planesActivacion["4_meses"] ?: 0).toString(),
            procentaje_ahorro = "30%",
            porcentaje_int = 30, "4 mes"
        )
    )


    val puntosSeguros = item.total_puntos.toLongOrNull() ?: 0L
// 🔥 Esto mantiene sincronizado el estado cuando Firebase cambia
    LaunchedEffect(item.horario_tiendaMap) {
        val bloqueMananaActual = bloques_hoy.getOrNull(0)
        val bloqueTardeActual = bloques_hoy.getOrNull(1)

        hAperturaAM.value = bloqueMananaActual?.h_apertura ?: ""
        hCierreAM.value = bloqueMananaActual?.h_cierre ?: ""
        hAperturaPM.value = bloqueTardeActual?.h_apertura ?: ""
        hCierrePM.value = bloqueTardeActual?.h_cierre ?: ""
    }


    val scrollState = rememberScrollState()
    var tiempoRestante by remember { mutableStateOf(20) }
    LaunchedEffect(key1 = mostrarSelectorMotivo) {
        if (mostrarSelectorMotivo) {
            tiempoRestante = 20
            while (tiempoRestante > 0 && switchActivo) {
                delay(1000L)
                tiempoRestante--
            }
            if (tiempoRestante == 0) swtch_motivocieere_activo_desactivado(false)

            if (tiempoRestante == 0 && ya_esta_cerrado) {
                retornar_motivo_cierre_vacio(horario_hoy.motivo)

                swtch_motivocieere_activo_desactivado(true)
                ya_esta_cerrado = false
            }
        }
    }
    LaunchedEffect(motivoSeleccionado) {
        if (!motivoSeleccionado.isNullOrEmpty()) {
            Log.d("Lllego_a", "El user ya seleccionó uno")
            val bloque = construirBloques(
                hAperturaAM.value, hCierreAM.value, hAperturaPM.value, hCierrePM.value
            )
            viewmodel.cambiar_cerrado(
                item.id_tienda, item.dia_hoy, motivoSeleccionado ?: "cierre", bloque
            )
            motivoSeleccionado = ""
            return@LaunchedEffect
        }
    }

    val showLeftShadow by remember {
        derivedStateOf {
            scrollState.value > 0
        }
    }

    val showRightShadow by remember {
        derivedStateOf {
            scrollState.value < scrollState.maxValue
        }
    }

    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400),
        label = "alphaLeft"
    )

    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400),
        label = "alphaRight"
    )

    var mostarr_dialog_saldo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        // ======================
        //      COLUMNA IZQUIERDA
        // ======================
        Column(
            modifier = Modifier
                .weight(1.7f)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF1A1A1A))
                .padding(10.dp)
                .animateContentSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                texto_generico_one_line(
                    item.nombre_tienda.capitalizeFirst(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = !switchActivo,
                    onCheckedChange = {
                        if (dias.toInt() != 0) {
                            swtch_motivocieere_activo_desactivado(!it)

                            if (it) {
                                val bloque = construirBloques(
                                    hAperturaAM.value,
                                    hCierreAM.value,
                                    hAperturaPM.value,
                                    hCierrePM.value
                                )
                                viewmodel.cambiar_abierto(
                                    item.id_tienda, item.dia_hoy, bloque
                                )
                            }
                        } else {
                            sin_activar_horario()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                )
            }
            Box {
                this@Column.AnimatedVisibility(mostrarDatosCerrado) {
                    Column {
                        // UI de la tienda abierta o cerrada con motivo
                        if (horas_de_trabajo == "0h 0m") {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                texto_generico_one_line(
                                    "Motivo de cierre: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = motivo_cierre,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.Underline,
                                        color = color_estado
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        if (dias.toInt() != 0) {
                                            retornar_motivo_cierre_vacio("")
//                                            motivo_cierre = ""
                                            motivo_cierre.isEmpty()
                                            ya_esta_cerrado = true
                                        } else {
                                            sin_acceso_motivo_cierre()
                                        }

                                    })

                            }
                        } else {
                            texto_generico_one_line(
                                "Horas de trabajo $horas_de_trabajo",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        spacer_vertical(8.dp)
                        // Estado / dias a renovar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            texto_generico_one_line(
                                "Estado: ", style = MaterialTheme.typography.bodyMedium
                            )
                            if (dias.toInt() != 0) {
                                texto_generico_one_line(
                                    "$dias días a renovar",
                                    color = color,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = "Por renovar",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.Underline,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        if (isConnected) {
                                            por_removar = true
                                        } else {
                                            sin_internet_al_renovar()
                                        }
                                    }
                                )
                            }
                        }
                        spacer_vertical(8.dp)
                        // Horarios
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                                .animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    if (dias.toInt() != 0) {
                                        mostar_Bottom_shet_editar_horario = true
                                    } else {
                                        sin_acceso_horario()
                                    }
                                }) {
                                texto_generico_one_line("Hoy ${item.dia_hoy}")
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color_estado)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }

                            bloques_hoy.forEach { bloque ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    texto_generico_one_line(
                                        constantes_horas.convertir24a12(bloque.h_apertura),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    texto_generico_one_line("a")
                                    texto_generico_one_line(
                                        constantes_horas.convertir24a12(bloque.h_cierre),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                this@Column.AnimatedVisibility(mostrarSelectorMotivo) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .animateContentSize()
                    ) {
                        texto_generico_one_line(
                            "Selecciona tu motivo en $tiempoRestante s",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_vertical(15.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp), contentAlignment = Alignment.Center
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                motivos.forEach { motivo ->
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (motivoSeleccionado == motivo) Color.White
                                                else MaterialTheme.colorScheme.primary
                                            )
                                            .clickable {
                                                // Alterna la selección: si ya está seleccionado, deselecciona
                                                motivoSeleccionado =
                                                    if (motivoSeleccionado == motivo) null else motivo
                                            }
                                            .padding(5.dp)) {
                                        texto_generico_one_line(
                                            motivo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (motivoSeleccionado == motivo) Color.Black else Color.White,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }

                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(40.dp)
                                    .align(Alignment.CenterStart)
                                    .zIndex(1f)
                                    .alpha(alphaLeft)
                                    .background(Brush.horizontalGradient(colors = start_shadow_bottom_sheet_default))
                            )

                            // 👉 derecha
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(40.dp)
                                    .align(Alignment.CenterEnd)
                                    .zIndex(1f)
                                    .alpha(alphaRight)
                                    .background(Brush.run { horizontalGradient(colors = end_shadow_bottom_sheet_default) })
                            )
                        }

                    }
                }
            }
        }
        spacer_vertical(8.dp)


        // ======================
        //      COLUMNA DERECHA
        // ======================
        // ======================
//      COLUMNA DERECHA
// ======================
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .data(item.img_tienda)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { mostar_panel_geinz() }
                )

                // Botón de compartir encima
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.60f))
                        .align(Alignment.TopEnd)
                        .clickable {
                            compartir_link_tienda(
                                context = context,
                                localidad = item.localidad_tienda,
                                id = item.id_tienda,
                                categoria = item.categoira_tienda,
                                nombre_tienda = item.nombre_tienda
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Parte inferior con datos - ocupa 60% del espacio del Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f) // 60% del alto
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(10.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    texto_generico_one_line("Saldo : ")

                    if (item.total_puntos == "0") {
                        Text(
                            text = "Obtener saldo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Box(
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                mostarr_dialog_saldo = true
                            }
                        ) {
                            texto_generico_one_line(
                                "${abreviarNumero(puntosSeguros)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.icon_monedas_3d),
                        contentDescription = "Icono",
                        modifier = Modifier.size(15.dp),
                        tint = Color.Unspecified
                    )
                }

                texto_generico_one_line("Atención")
                TextoExpandibleEnLinea(
                    txt_estado_teinda.capitalizeFirst(),
                    color_estado,
                    color_estado
                )
            }
        }

    }
    if (mostarr_dialog_saldo) {
        dialog_cantidad_slado_geinz(
            { mostarr_dialog_saldo = !mostarr_dialog_saldo },
            item.total_puntos
        )
    }
    if (por_removar) {
        dialog_renovar_plan(
            precio_moneda = precio_moneda,
            lista_descuentos = lista_descuentos,
            saldo_disponible = puntosSeguros,
            ondimis = { por_removar = !por_removar },
            comprar = { total_cancelar, meses_agregados ->
                viewmodel.descontar_puntos(precio_moneda,viewmodel_recargas,
                    item.total_puntos.toInt(),item.nombre_tienda,
                    "barranca",
                    item.id_tienda,
                    total_cancelar.toInt(),
                    meses_agregados
                )
            })
    }
    if (mostar_Bottom_shet_editar_horario) {
        eres_socio_geinz(
            true,
            tick,
            { mostar_Bottom_shet_editar_horario = false },
            datos_bottom_Shhet_editar_horario
        )
    }

}

fun compartir_link_tienda(
    context: Context,
    localidad: String,
    id: String,
    categoria: String,
    nombre_tienda: String
) {
    // Construimos el link de la Cloud Function
    val link = "https://geinzworkapp.web.app/api/share?" +
            "t=ti" +
            "&id=${URLEncoder.encode(id, "UTF-8")}" +
            "&l=${URLEncoder.encode(localidad, "UTF-8")}" +
            "&c=${URLEncoder.encode(categoria, "UTF-8")}"

    val texto = "¡Mira $nombre_tienda en Geinz! 🔥\n$link"

    // Intent simple ya sin imágenes, porque la preview la maneja Firebase Hosting
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }

    context.startActivity(Intent.createChooser(intent, "Compartir con"))
}


@Composable
fun baner_registra_tu_negocio(
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    conexion: Boolean,
    listener_registra_tu_negocio: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1A1A))
            .fillMaxWidth()
            .defaultMinSize(minHeight = 180.dp)
            .clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }) {
                listener_registra_tu_negocio()

            }) {
        Row(
            modifier = Modifier.matchParentSize() // 🔹 el Row ocupa todo el Box
        ) {
            // === COLUMNA DE TEXTO Y BOTÓN ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "¿Quieres ser parte de Geinz?",
                        color = Color.White,
                        fontFamily = baners_geinz_work,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    spacer_vertical(10.dp)

                    texto_generico_multilinea(
                        "Llega a más clientes potenciales y aumenta tu presencia digital.",
                        style = MaterialTheme.typography.bodyMedium,
                        Color = Color.White
                    )
                }

                spacer_vertical(10.dp)
                // === BOTÓN FLECHA ===
                Box(
                    modifier = Modifier
                        .size(40.dp) // 🔹 tamaño fijo y respetado
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.25f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            listener_registra_tu_negocio()
                        }
                        .align(Alignment.Start), // evita que se estire horizontalmente
                    contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Ir",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                spacer_vertical(10.dp)
            }

            // === IMAGEN ===
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .memoryCachePolicy(CachePolicy.ENABLED).diskCachePolicy(CachePolicy.ENABLED)
                        .data(R.drawable.geinz_baner).build(),

                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradiente para transición
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1A1A1A), Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun shadow_bottom_pantallas_generales(modifier: Modifier) {
    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha, animationSpec = tween(durationMillis = 500)
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent, Color.Black
                    )
                )
            )
            .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
    )
}

@Composable
fun TextoExpandibleEnLinea(
    texto: String,
    color_principla: Color = Color.White,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    var expandido by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth() // 🔹 ocupa todo el ancho disponible
            .animateContentSize() // 🔹 animación suave
            .padding(end = 15.dp)
    ) {
        Text(
            buildAnnotatedString {
                // Texto principal (blanco)
                withStyle(
                    style = SpanStyle(color = color_principla)
                ) {
                    append(texto)
                }

                append(" ")

                withStyle(
                    style = SpanStyle(
                        color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                ) {}
            },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expandido) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }) {
                expandido = !expandido
            })
    }
}

@Composable
fun btn_listener_fv_externo(
    select: Boolean,
    modifier: Modifier,
    listener: (Boolean) -> Unit,
    size_btn: Dp = 35.dp,
    size_icon: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(size_btn)
            .background(Color.White)
            .clickable {
                listener(!select)
            },
        contentAlignment = Alignment.Center,
    ) {

        Crossfade(targetState = select, label = "") { seleccionado ->
            val icono = if (!seleccionado) {
                R.drawable.corazon_icon_negro_border
            } else {
                R.drawable.icon_borde_corazon_completo
            }

            Image(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(size_icon)
            )
        }
    }
}
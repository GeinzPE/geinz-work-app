package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.Cartas_expandibles
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.cargando_progess_mas_texto
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.expandibles_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.generar_qr_ubi_tinda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permisos_llamadas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.requestCallPermission
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.amarillo30
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_publicaciones_general_user_tiendas.obtenerDiaActualEnEspañol
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.ZoomIconButton
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.end_shadow_bottom_sheet_default
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.start_shadow_bottom_sheet_default
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda.retornar_id_Tienda_lugar
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun bottom_sheet_tiendas_filtradas(
    estadoColor: Color,
    viewModelFiltros: viewModel_filtado_tiendas,
    tiendas_filtradas: modelo_tienda,
    visible: Boolean,
    onClose: () -> Unit
) {
    var expandir_descripcion by rememberSaveable { mutableStateOf(false) }
    var expander_caracterisiticas by rememberSaveable { mutableStateOf(false) }
    var expander_contacto by rememberSaveable { mutableStateOf(false) }
    var expander_horario by rememberSaveable { mutableStateOf(false) }
    var expander_qr_tienda by rememberSaveable { mutableStateOf(false) }

    val direccion = tiendas_filtradas.ubicacion["dirección"]?.toString() ?: ""
    val referencia = tiendas_filtradas.ubicacion["referencia"]?.toString() ?: ""
    val longitud = (tiendas_filtradas.ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
    val latitud = (tiendas_filtradas.ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0



    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(visible) {
        if (visible) {
            cargando = true
            delay(2000)
            cargando = false
        }
    }

    if (!visible) return


    ModalBottomSheet(
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null
    ) {
        FuenteControladaApp {
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AnimatedVisibility(visible = true) { // Aquí sí animamos solo el contenido
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)

                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 12.dp , start = 10.dp , end = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.LightGray)
                                )
                            }
                        }
                        item {
                            cabezero_tiendas(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                estadoColor,
                                direccion,
                                referencia,
                                tiendas_filtradas.categoria_tienda,
                                tiendas_filtradas.nombre_tienda,
                                latitud,
                                longitud,
                                tiendas_filtradas.img_perfil,
                                tiendas_filtradas.lista_img,
                                tiendas_filtradas.subcategoria
                            )
                            spacer_vertical(20.dp)
                        }
                        item {
                            text_expandible_wrapp(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                "Acerca de la tienda",
                                MaterialTheme.typography.titleLarge,
                            )
                            spacer_vertical(10.dp)
                        }
                        item {
                            Expandible_descripcion_tienda(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                tiendas_filtradas.descripcion,
                                expander_caracterisiticas
                            ) { expander_caracterisiticas = !expander_caracterisiticas }
                            spacer_vertical(10.dp)
                        }
                        item {
                            if (direccion.isNotBlank() || referencia.isNotBlank()) {
                                val fisica_virtual =
                                    if (tiendas_filtradas.modelo_negocio) "Fisica" else "Virtual"

                                Column(modifier = Modifier.animateContentSize()) {
                                    Expandible_direccion_ref(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        direccion,
                                        referencia,
                                        fisica_virtual,
                                        expandir_descripcion
                                    ) {
                                        expandir_descripcion = !expandir_descripcion
                                    }
                                }
                            }
                            spacer_vertical(10.dp)
                        }
                        item {
                            Expandible_horario_atencion(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                tiendas_filtradas.horario_atencion,
                                estadoColor,
                                tiendas_filtradas.localidad,
                                tiendas_filtradas.id_tienda,
                                expander_horario,
                                viewModelFiltros
                            ) { expander_horario = !expander_horario }
                            spacer_vertical(10.dp)
                        }
                        item {
                            Expandible_Metodo_contacto(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                expander_contacto,
                                tiendas_filtradas.metodo_contacto_tienda
                            ) { expander_contacto = !expander_contacto }
                            spacer_vertical(10.dp)
                        }

                        item {
                            Expandible_qr_tienda(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                tiendas_filtradas.id_tienda,
                                latitud, longitud,
                                expander_qr_tienda
                            ) { expander_qr_tienda = !expander_qr_tienda }
                            spacer_vertical(10.dp)
                        }
                    }
                }
            }
        }
    }


}



@Composable
fun cabezero_tiendas(
    modifier: Modifier= Modifier,
    estadoColor: Color,
    direccion: String,
    referencia: String,
    categoritienda: String,
    nombre_tienda: String,
    latitud: Double,
    longitud: Double,
    img_tienda_perfil: String,
    lista_img: List<String>,
    lista_tags: List<String>
) {
    val context = LocalContext.current
    val mostrarDialogo = rememberSaveable { mutableStateOf(false) }
    val mostrarDialog_sin_google_maps = rememberSaveable { mutableStateOf(false) }
    var expdir_img by remember { mutableStateOf(false) }
    var mostrarDialogozoom by remember { mutableStateOf(false) }

    if (mostrarDialogozoom) {
        ZoomableImageDialogFullScreen(
            imageUrl = img_tienda_perfil,
            onDismiss = { mostrarDialogozoom = false }
        )
    }
    var expandir_img by remember { mutableStateOf(false) }


    if (mostrarDialogo.value) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                mostrarDialogo.value = false
            },
            abrir_configuracion = {
                mostrarDialogo.value = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                mostrarDialogo.value = false
                mostrarDialog_sin_google_maps.value = true
            }
        )
    }

    if (mostrarDialog_sin_google_maps.value) {
        dialog_sin_ubi_activa(
            direccion, referencia, onDismis = { mostrarDialog_sin_google_maps.value = false },
            abrir_maps = { constantes.abrirGoogleMaps(context, direccion) })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),

            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1D1B20)

            )
        ) {
            perfil_img_zooom(
                modifier,
                img_tienda_perfil,
                { expdir_img = !expdir_img },
                { mostrarDialogozoom = true })

            AnimatedVisibility(expdir_img, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                CollageGoogleMapsStyle(imagenes=lista_img)
//                LazyRow(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 10.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    contentPadding = PaddingValues(horizontal = 4.dp)
//                ) {
//                    items(lista_img.size) { img ->
////                        val img_unidad = lista_img[img]
//
//                    }
//                }
            }
        }

        spacer_vertical(10.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()

        ) {
            Box(modifier = Modifier.weight(1f)) {
                perfil_cabezero(nombre_tienda, estadoColor, categoritienda, lista_tags)
            }
            spacer_horizonta(10.dp)
            abrir_google_maps(context, latitud, longitud) { dialog_ ->
                mostrarDialogo.value = dialog_
            }
        }
    }
}


@Composable
fun perfil_img_zooom(
    modifier: Modifier= Modifier,
    img_tienda_perfil: String,
    expandido: () -> Unit,
    mostrarDialogozoom: () -> Unit
) {
    Box(modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img_tienda_perfil)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias).build(),
            contentDescription = "Imagen de la tienda",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { expandido() },
            onState = { state ->
            }
        )
        val painterState = rememberAsyncImagePainter(model = img_tienda_perfil).state
        if (painterState is AsyncImagePainter.State.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ZoomIconButton(mostrarDialogozoom)
        }
    }

}

@Composable
fun perfil_cabezero(
    nombre_tienda: String,
    estadoColor: Color,
    categoritienda: String,
    lista_tags: List<String>
) {
    Column {
        val iconId = "icon"
        val annotatedText = buildAnnotatedString {
            append(nombre_tienda.uppercase())
            append(" ")
            appendInlineContent(iconId, "[icon]")
        }

        val inlineContent = mapOf(
            iconId to InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(50))
                        .background(estadoColor)
                )
            }
        )

        Text(
            text = annotatedText,
            inlineContent = inlineContent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        text_expandible_wrapp(
            texto = "Categoria : $categoritienda",
            style = MaterialTheme.typography.bodyMedium
        )
        spacer_vertical(10.dp)

        tags_subcateogiras(
            lista_tags,
            brush_start = Brush.horizontalGradient(colors = start_shadow_bottom_sheet_default),
            brush_end = Brush.horizontalGradient(colors = end_shadow_bottom_sheet_default)
        )
    }

}

@Composable
fun abrir_google_maps(
    context: Context,
    latitud: Double,
    longitud: Double,
    mostrarDialogo: (Boolean) -> Unit
) {
    FloatingActionButton(
        onClick = {
            constantes_lista_localidades.abrir_google_maps(
                context,
                latitud,
                longitud
            ) { dialogo ->
                mostrarDialogo(dialogo)
            }
        },
        modifier = Modifier.size(40.dp),
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Image(
            painter = painterResource(R.drawable.localidad_icon_general),
            contentDescription = "Localidad",
            modifier = Modifier.size(35.dp)
        )
    }
}


@Composable
fun Expandible_descripcion_tienda(
    modifier: Modifier= Modifier,
    descipcion_tienda: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Cartas_expandibles (modifier= modifier){
        Column() {
            expandibles_wrapp(
                "Descripcion",
                iconRes = R.drawable.descripcion_tienda_vector,
                null,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(10.dp)
                ) {
                    texto_expandido_wrapp_sin_max_line(descipcion_tienda)
                }
            }
        }
    }

}

@Composable
fun Expandible_direccion_ref(
    modifier: Modifier= Modifier,
    direccion: String,
    referencia: String,
    fisica_virtual: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Cartas_expandibles(modifier = modifier) {
        Column {
            expandibles_wrapp(
                "Dirección y referencia",
                iconRes = R.drawable.location_drawable,
                null,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(
                visible = expandido
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                ) {
                    text_expandible_wrapp(texto="Dirección : $direccion")
                    spacer_vertical(10.dp)
                    text_expandible_wrapp(texto="Referencia : $referencia")
                    spacer_vertical(10.dp)
                    text_expandible_wrapp(texto="Tipo de tienda : $fisica_virtual")
                    spacer_vertical(10.dp)
                }
            }
        }
    }
}


@Composable
fun Expandible_Metodo_contacto(
    modifier: Modifier= Modifier,
    expandido: Boolean,
    metodos_contactos: metodo_contacto_tienda,
    onClickExpand: () -> Unit
) {
    val context=LocalContext.current
    var call_dialog_permise by remember { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }
    Cartas_expandibles (modifier=modifier){
        Column {
            expandibles_wrapp(
                "Metodos de contacto",
                iconRes = R.drawable.baseline_call_24,
                null,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                ) {
                    if (metodos_contactos.whatsapp.estado) {
                        item_metodo_contacto(
                            R.drawable.whatsapp_icon,
                            constantes_lista_localidades.ocultarNumero(metodos_contactos.whatsapp.numero)
                        ){
                            abrir_whattsapp(context, metodos_contactos.whatsapp.numero)
                        }
                    }
                    if (metodos_contactos.llamada.estado) {
                        item_metodo_contacto(
                            R.drawable.llamada_icon,
                            constantes_lista_localidades.ocultarNumero(metodos_contactos.llamada.numero)
                        ){

                            llamar(context, metodos_contactos.llamada.numero, {
                                call_dialog_permise = true
                                numero_llamada = metodos_contactos.llamada.numero
                            })
//                            call_dialog_permise = true
//                            numero_llamada = metodos_contactos.llamada.numero
                        }
                    }
                    if (metodos_contactos.tiktok.estado) {
                        item_metodo_contacto(
                            R.drawable.tik_tok_icon,
                            metodos_contactos.tiktok.nombre
                        ){
                            openTiktok(context,metodos_contactos.tiktok.url)
                        }
                    }
                    if (metodos_contactos.sitio_web.estado) {
                        item_metodo_contacto(
                            R.drawable.web_icon,
                            metodos_contactos.sitio_web.nombre
                        ){
                            openWebLink(context,metodos_contactos.sitio_web.url)
                        }
                    }
                    if (metodos_contactos.instagram.estado) {
                        item_metodo_contacto(
                            R.drawable.instagram_icon,
                            metodos_contactos.instagram.nombre
                        ){
                            openInstagram(context,metodos_contactos.instagram.url)
                        }
                    }
                    if (metodos_contactos.facebook.estado) {
                        item_metodo_contacto(
                            R.drawable.facebook_icon,
                            metodos_contactos.facebook.nombre
                        ){
                            openFacebook(context, metodos_contactos.facebook.url)
                        }
                    }
                }
            }

        }
    }
    if (call_dialog_permise) {
        permisos_llamadas(aceptar_permisos = {
            requestCallPermission(context = context, phoneNumber = numero_llamada)
        }, ondimis = {
            call_dialog_permise = false
        })
    }
}


@Composable
fun Expandible_qr_tienda(
    modifier: Modifier= Modifier,
    id_tienda: String,
    latitud: Double,
    longitud: Double,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
//    val generador_qr = remember(latitud, longitud) {
//        generar_qr_cordenadas_tienda.codificarCoordenadas(
//            latitud, longitud
//        )
//    }

    val generar_qr_tienda_id = remember(id_tienda, latitud, longitud) {
        retornar_id_Tienda_lugar(id_tienda, latitud, longitud)
    }
    Cartas_expandibles (modifier = modifier){
        Column {
            expandibles_wrapp(
                "QR de Tienda",
                iconRes = R.drawable.qr_scaner_icon,
                null,
                expandido,
                onClickExpand
            )
        }
        AnimatedVisibility(visible = expandido) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            ) {
                generar_qr_ubi_tinda(
                    "Escanea el código QR desde Geinz work para obtener la ruta hacia esta tienda. Google maps te guiará con indicaciones paso a paso para que llegues fácilmente",
                    generar_qr_tienda_id
                )
            }
        }
    }
}

@Composable
fun item_metodo_contacto(icono_red: Int, texto: String,click_icon:()-> Unit) {
    var context = LocalContext.current
    spacer_vertical(5.dp)
    Row(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(icono_red),
                modifier = Modifier
                    .size(30.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        click_icon()
                    },
                contentDescription = ""
            )
            spacer_horizonta(10.dp)
            Text(
                text = texto,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        spacer_horizonta(20.dp)
        Image(
            painter = painterResource(R.drawable.baseline_content_copy_24),
            modifier = Modifier
                .size(25.dp)
                .clickable {
                    constantestextos_general.copiarTexto_portapapeles_compouse(texto, context)
                },
            contentDescription = "",
            colorFilter = ColorFilter.tint(Color.White)

        )
    }
    spacer_vertical(10.dp)

}


@Composable
fun texto_expandido_wrapp_sin_max_line(
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        style = style,
        overflow = TextOverflow.Ellipsis

    )
}


@Composable
fun Expandible_horario_atencion(
    modifier: Modifier= Modifier,
    horario_atencion: HorarioAtencion,
    estadoColor: Color,
    localidad_tienda: String?,
    id_tienda: String,
    expandido: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    onClickExpand: () -> Unit
) {
//    val horarioTienda by viewModelFiltros.horarioTienda.observeAsState(null)
//    val horarioRecordado = remember(horarioTienda) { horarioTienda }


    var cargado by remember { mutableStateOf(false) }

    LaunchedEffect(expandido) {
        if (expandido && !cargado) {
            viewModelFiltros.obtenerHorarioPorTienda(localidad_tienda!!, id_tienda)
            cargado = true
        }
    }
    Cartas_expandibles(modifier = modifier) {
        Column() {
            expandibles_wrapp(
                "Horario de atención",
                iconRes = R.drawable.horario_tienda_vector,
                null,
                expandido,
                onClickExpand
            )
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    MostrarHorarioTienda(horario_atencion, estadoColor)
                }
            }
        }
    }
}

@Composable
fun MostrarHorarioTienda(
    horarioTienda: HorarioAtencion,
    estadoColor: Color,
) {
    val diaActual = obtenerDiaActualEnEspañol().lowercase()

    // Convertimos el objeto a lista de pares (día, horario)
    val listaHorarios = listOf(
        "lunes" to horarioTienda.lunes,
        "martes" to horarioTienda.martes,
        "miércoles" to horarioTienda.miercoles,
        "jueves" to horarioTienda.jueves,
        "viernes" to horarioTienda.viernes,
        "sábado" to horarioTienda.sabado,
        "domingo" to horarioTienda.domingo
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        listaHorarios.forEach { (dia, horario) ->
            val esDiaActual = dia == diaActual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "${dia.replaceFirstChar { it.uppercase() }} : ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                )

                val aperturaAMPM = formatearHoraAMPM(horario.h_apertura)
                val cierreAMPM = formatearHoraAMPM(horario.h_cierre)

                val textoHorario = when {
                    horario.cerrado -> if (horario.motivo.isEmpty()) {
                        "Cerrado"
                    } else {
                        horario.motivo.capitalizeFirst()
                    }

                    horario.h_apertura == "00:00" && horario.h_cierre == "23:59" -> "Abierto las 24h"
                    else -> "$aperturaAMPM - $cierreAMPM"
                }
                spacer_horizonta(7.dp)

                Text(
                    text = textoHorario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                if (esDiaActual) {

                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(estadoColor)
                    )
                }
            }

        }
    }
}

fun formatearHoraAMPM(hora24: String): String {
    return try {
        val formato24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formato12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val fecha = formato24.parse(hora24)
        formato12.format(fecha ?: return "")
    } catch (e: Exception) {
        ""
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ZoomableImageDialogFullScreen(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            color = Color.Black
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val maxWidthPx = constraints.maxWidth.toFloat()
                val maxHeightPx = constraints.maxHeight.toFloat()

                val coroutineScope = rememberCoroutineScope()

                var scale by remember { mutableStateOf(1f) }
                val offsetX = remember { Animatable(0f) }
                val offsetY = remember { Animatable(0f) }

                val gestureModifier = Modifier.pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                        scale = newScale

                        val maxX = (maxWidthPx * (scale - 1)) / 2
                        val maxY = (maxHeightPx * (scale - 1)) / 2

                        coroutineScope.launch {
                            offsetX.snapTo((offsetX.value + pan.x).coerceIn(-maxX, maxX))
                            offsetY.snapTo((offsetY.value + pan.y).coerceIn(-maxY, maxY))
                        }
                    }
                }

                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX.value,
                            translationY = offsetY.value
                        )
                        .then(gestureModifier)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun tienda_cercana() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(40f))
            .background(amarillo30)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text("Cerca de ti")
        Spacer(modifier = Modifier.width(5.dp))
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.localidad_icon_general),
            contentDescription = ""
        )
    }
}




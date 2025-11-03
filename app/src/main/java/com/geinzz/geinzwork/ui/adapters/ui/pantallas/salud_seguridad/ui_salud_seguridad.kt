package com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_llamada_urgencias
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.requestCallPermission
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_alerta_llamada
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.centrado_hori_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud.carga_seguidad

private val REQUEST_CALL_PHONE = 1

@Composable
fun ui_salud_seguirdad(
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    localida: String,
    abrir_mapa: (latitud: Double, longitud: Double) -> Unit
) {


    val lista_filtrado = listOf<String>("Todos", "salud", "seguridad")
    val lista_seguridad_salud by viewmode_segurirdad_Salud._datos_lugares.observeAsState(emptyList())
    val context = LocalContext.current

    var lista_mostrar by rememberSaveable { mutableStateOf<List<dataclass_seguridad>>(emptyList()) }
    var lista_base_seguridad by rememberSaveable { mutableStateOf(emptyList<dataclass_seguridad>()) }
    var valor_filtrado by rememberSaveable { mutableStateOf("") }
    var chip_selecionado by rememberSaveable { mutableStateOf("Todos") }
    val state_seguridad = viewmode_segurirdad_Salud.state_lista_filtradad.collectAsState(carga_seguidad.loading).value
    val mostrar_carga_salud_seguridad by viewmode_segurirdad_Salud.mostrar_carga_salud_seguridad.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var error_empity by remember { mutableStateOf(false) }
    var texto_error_empity by remember { mutableStateOf("") }
    var yaInicializado by remember { mutableStateOf(false) }

    LaunchedEffect(chip_selecionado) {
        if (yaInicializado && lista_seguridad_salud.isNotEmpty() && chip_selecionado != "Todos") {
            viewmode_segurirdad_Salud.filtrar_lugares(chip_selecionado)
        } else {
            viewmode_segurirdad_Salud.lista_base_completa(chip_selecionado)
        }
    }
    LaunchedEffect(valor_filtrado) {
        viewmode_segurirdad_Salud.filtrar_nombre_categoria(
            nombre = valor_filtrado,
            categoria = chip_selecionado,
            lista = lista_base_seguridad
        )
    }

    // Llama servicios iniciales
    LaunchedEffect(Unit) {
        viewmode_segurirdad_Salud.obtener_servicios(localida,context)
    }

    LaunchedEffect(lista_seguridad_salud) {
        if (!yaInicializado && lista_seguridad_salud.isNotEmpty()) {
            yaInicializado = true
            lista_base_seguridad = lista_seguridad_salud
            viewmode_segurirdad_Salud.lugares_iniciales(lista_seguridad_salud)
        }
    }

    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    var sin_resultados by remember { mutableStateOf(false) }
    var toastShown by remember { mutableStateOf(false) }

    val stickyHeaderIndex = 1
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex >= stickyHeaderIndex && !toastShown) {
            toastShown = true
        } else if (listState.firstVisibleItemIndex < stickyHeaderIndex) {
            toastShown = false
        }
    }
    var bottom_sheet_llamda by remember { mutableStateOf(false) }
    val paddingAnim by animateDpAsState(
        targetValue = if (toastShown) 10.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    var tienePermisoLlamada by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // ✅ Launcher que reacciona cuando el usuario acepta o rechaza el permiso
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        tienePermisoLlamada = isGranted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                texto_generico_multilinea(
                    "Salud y Seguridad Pública",
                    style = MaterialTheme.typography.banerGeinzWork,
                    modifier = Modifier.padding(end = 20.dp)
                )
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "Tu bienestar es primero, localiza hospitales, comisarías, bomberos y servicios de ayuda cuando los necesites.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            stickyHeader {
                ColumnContenedorComun(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = paddingAnim,
                                end = paddingAnim,
                                bottom = (paddingAnim - 5.dp).coerceAtLeast(0.dp)
                            )
                    ) {
                        filtrado_texfiel(valor_filtrado) { valor_filtrado = it }
                        spacer_vertical(10.dp)
                        chips_filtrado(tienePermisoLlamada,context, chip_selecionado, lista_filtrado, { i ->
                            chip_selecionado = i
                        }, {
                            bottom_sheet_llamda = true
                        })
                    }
                }
            }
            when (state_seguridad) {
                is viewmode_seguridad_salud.carga_seguidad.loading -> {
                    isLoading = true
                    error_empity = false
                }

                is viewmode_seguridad_salud.carga_seguidad.succes -> {
                    val lista =
                        (state_seguridad as viewmode_seguridad_salud.carga_seguidad.succes).list
                    items(lista) { i ->
                        isLoading = false
                        error_empity = false
                        Box(modifier = Modifier.padding(8.dp)) {
                            carta_salud_cuidad(
                                viewmode_segurirdad_Salud,
                                i,
                                abrir_mapa = { la, lo ->
                                    viewmode_segurirdad_Salud.setCoordenadas(la, lo)
                                    abrir_mapa(la, lo)
                                })
                        }
                    }
                }

                is viewmode_seguridad_salud.carga_seguidad.empity -> {
                    val texto =
                        (state_seguridad as viewmode_seguridad_salud.carga_seguidad.empity).texto
                    isLoading = false
                    error_empity = true
                    texto_error_empity = texto
                }

                is viewmode_seguridad_salud.carga_seguidad.error -> {
                    val texto =
                        (state_seguridad as viewmode_seguridad_salud.carga_seguidad.error).texto
                    isLoading = false
                    error_empity = true
                    texto_error_empity = texto

                }
            }
        }

        if (mostrar_carga_salud_seguridad) {
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

        AnimatedContent(
            targetState = when {
                isLoading -> "loading"
                sin_resultados -> "empty"
                error_empity -> "error"
                else -> "none"
            },
            label = "estado_carga"
        ) { estado ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                centrado_hori_vertical {
                    when (estado) {
                        "loading" -> {
//                            CircularProgressIndicator()
                        }
                        "empty" -> texto_generico_one_line(
                            texto_error_empity,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        "error" -> texto_generico_one_line(
                            texto_error_empity,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            !mostrar_carga_salud_seguridad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                        )
                    )
                    .graphicsLayer { alpha = alphaAnim }
            )
        }

        if (bottom_sheet_llamda) {
            bottom_sheet_alerta_llamada(
                ondimis = { bottom_sheet_llamda = false },
                mostrar_permiso = {
                    requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                })
        }

    }
}

@Composable
fun chips_filtrado(
    tienePermisoLlamada1: Boolean,
    context: Context,
    selecionado_chip: String,
    lista_filtrado: List<String>,
    selecionado_fun: (String) -> Unit,
    select_alerta: () -> Unit,
) {

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(lista_filtrado) { i ->
            val selecionado = selecionado_chip == i
            chisp_filtrado_busqueda(
                carta_selecionada = selecionado,
                filtrado = i.capitalizeFirst(),
                btn_visible = false,
                clik_card = {
                    selecionado_fun(i)
                }, onClick_delete = {})

        }
        if (!tienePermisoLlamada1) {
            item {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .height(45.dp)
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            select_alerta()

                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    texto_generico_one_line("Alerta", style = MaterialTheme.typography.bodyMedium)
                    spacer_horizonta(5.dp)
                    Image(
                        painter = painterResource(R.drawable.icono_alerta_3d_webp),
                        contentDescription = "alerta",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }


}

@Composable
fun filtrado_texfiel(texto: String, onValueChange: (String) -> Unit) {
    var icono_borrar by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = texto,
        onValueChange = { it ->
            icono_borrar = it.isNotBlank()
            onValueChange(it)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.buscar_icon),
                contentDescription = "buscar",
                modifier = Modifier.size(18.dp)
            )
        }, trailingIcon = {
            if (icono_borrar) {
                IconButton(onClick = {
                    onValueChange("")
                    icono_borrar = false
                }) {
                    Icon(
                        painter = painterResource(R.drawable.vector_eliminar_texto_texfiel),
                        contentDescription = "borrar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }, placeholder = {
            Text(
                text = "Que buscas?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }, modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(50)
    )
}

@Composable
fun carta_salud_cuidad(
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    i: dataclass_seguridad,
    abrir_mapa: (latitud: Double, longitud: Double) -> Unit
) {
    val context = LocalContext.current
    var dialogo_activar_ubicacion by rememberSaveable { mutableStateOf(false) }

    var call_dialog_permise by rememberSaveable { mutableStateOf(false) }
    var dialogo_contacto by remember { mutableStateOf(false) }
    var lista_numero by remember { mutableStateOf(listOf<String>()) }
    var icono_dialogo by remember { mutableStateOf("") }
    var dialog_sin_lat_log by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surface)

    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(i.img_ref)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .size(300, 100).placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.padding(start = 10.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
        ) {
            texto_generico_one_line(
                i.nombre_,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            spacer_vertical(5.dp)
            if (i.direccion.isNotEmpty()) {
                texto_generico_one_line(
                    i.direccion, color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
            }
            texto_generico_one_line(
                viewmode_segurirdad_Salud.horario_atencion(i.nombre_),
                color = Color.White, style = MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                if (i.numero_llamada.isNotEmpty()) {
                    BtnCirculares(R.drawable.llamada_icon) {
                        dialogo_contacto = true
                        lista_numero = i.numero_llamada
                        icono_dialogo = "llamada"

                    }
                }
                if (i.numero_whatsapp.isNotEmpty()) {
                    BtnCirculares(R.drawable.whatsapp_icon) {
                        dialogo_contacto = true
                        lista_numero = i.numero_whatsapp
                        icono_dialogo = "whatsapp"

                    }
                }
                if (i.latidud != 0.0 || i.longitud != 0.0) {
                    BtnCirculares(
                        R.drawable.vector_ruta_icon,
                        fondo = MaterialTheme.colorScheme.primary
                    ) {
                        constantes_lista_localidades.abrir_google_maps(
                            context = context,
                            i.latidud, i.longitud
                        ) { mostrar_dialog ->
                            dialogo_activar_ubicacion = mostrar_dialog
                        }
                    }
                }
            }
        }
    }

    if (dialogo_activar_ubicacion) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                dialogo_activar_ubicacion = false
            },
            abrir_configuracion = {
                dialogo_activar_ubicacion = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                dialog_sin_lat_log=true
                dialogo_activar_ubicacion = false
            }
        )
    }
    if(dialog_sin_lat_log){
        dialog_sin_ubi_activa(
            direccion = i.direccion,
            referencia = i.referencia,
            onDismis = { dialog_sin_lat_log = false },
            abrir_maps = { constantes.abrirGoogleMaps(context, i.direccion) })
    }
    if (dialogo_contacto) {
        dialog_llamada_urgencias(lista_numero, icono_dialogo) {
            dialogo_contacto = false
        }
    }
}

@Composable
fun BtnCirculares(
    icono: Any,
    fondo: Color = Color.Transparent,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    tint: Color = Color.White,
    listener: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fondo)
            .clickable { listener() },
        contentAlignment = Alignment.Center
    ) {
        when (icono) {
            is Int -> Image(
                painter = painterResource(id = icono),
                contentDescription = null,
            )

            is ImageVector -> Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .padding(5.dp),
                tint = tint
            )
        }
    }
}
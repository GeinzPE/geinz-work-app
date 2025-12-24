package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.cambiar_datos_pago_contacto
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.datos_grafico
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.datos_tienda_fechas
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.servicio_comodidad
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.data_store.data_store_localidad.set_id_socio
import com.geinzz.geinzwork.data_store.data_store_localidad.set_localidad_tienda_soscio
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandible_wrap_socio_atrubitos
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_contacto_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_geinzz
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_geinzz_datos_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_metodos_pago_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandido_wrap_socio_atributos
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.BoxImagen
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.esUriLocal
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.esUrlRemota
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.guardarCambiosImagenes
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.guardarImagenesEnFirestore
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.subir_foto_perfil_algolia_normal
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.subir_storage_perfil_img
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.Cartas_expandibles
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.expandibles_wrapp_socio_geinzz_horario_atencion
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_mostar_leyendas_graficos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialogo_cerrar_seccion_teinda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.opciones_localida
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.DiaHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerDiasYColor
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import com.google.firebase.firestore.FirebaseFirestore
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun login_socios(isConnected: Boolean) {
    val verificar_id by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var id_registrado by remember { mutableStateOf("") }
    val labels = listOf("Vistas", "Guardados", "Clics", "Compartidos")
    val labels2 = listOf("Facebook", "Instagram", "TikTok", "Sitio web")
    val labels3 = listOf("Llamada", "Whatsapp", "Rutas")
    val labels4 = listOf("QR review publico", "QR review privado", "QR creacion de ruta","QR vistas de perfil")
    val viewmodel: viewmodel_eres_socio = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val state_socio = viewmodel.state_eres_socio.collectAsState()
    val _tick by viewModelFiltros.tick.collectAsState()
    var mostar_interes by remember { mutableStateOf(false) }
    var mostrar_convesion by remember { mutableStateOf(false) }
    var mostrar_datos_teinda by remember { mutableStateOf(false) }
    var mostrar_redes_tienda by remember { mutableStateOf(false) }
    var mostar_socio_atributos by remember { mutableStateOf(false) }
    var mostar_metodos_pago_tienda by remember { mutableStateOf(false) }
    var mostar_atributos_negocios by remember { mutableStateOf(false) }
    var mostar_horario_teinda by remember { mutableStateOf(false) }
    var mostrar_trafico_externo by remember { mutableStateOf(false) }
    var mostrar_qr_externo by remember { mutableStateOf(false) }
    var dialog_mostar_leyendas_graficos by remember { mutableStateOf(false) }
    var titulo_leyenda_dialog by remember { mutableStateOf("") }
    var txt_leyenda by remember { mutableStateOf("") }
    var icono_mostar_leyendas_graficos by remember { mutableStateOf(0) }
    var id_tienda by remember { mutableStateOf("") }
    var listaPropietarios by remember { mutableStateOf(emptyList<String>()) }
    var metodos_pago by remember { mutableStateOf(modelo_pagos_tienda()) }
    var metodo_contact by remember { mutableStateOf(metodo_contacto_tienda()) }

    val existe_id_vinculado_tienda by remember { mutableStateOf("") }
    var localidad_tienda by remember { mutableStateOf("barranca") }
    var fecha_ingreso by remember { mutableStateOf("") }
    var fecha_termino by remember { mutableStateOf("") }
    var horarioMap by remember { mutableStateOf(HorarioAtencion_box()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostar_horario__bool by remember { mutableStateOf(false) }
    val verificarSeccion by viewmodel.verificarSeccion.collectAsState()
    var mostar_progeres_var_en_btn by remember { mutableStateOf(false) }
    val cargando by viewmodel.cargandoIdSocio.collectAsState()
    val idSocio by viewmodel.idSocio.collectAsState()
    var cerrar_Seccion_cuenta_tienda by remember { mutableStateOf(false) }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    var localidad_seleciondad by remember { mutableStateOf("") }
    val localidad_tienda_select_ by data_store_localidad.get_localidad_tienda_socio(context)
        .collectAsState(initial = "")

    val estaVinculado = listaPropietarios.contains(uid_respald_user)
    var ingresar_correo by remember { mutableStateOf(false) }
    var correo_electronico_cuenta_user by remember { mutableStateOf("") }


    var fotos_ambiente by remember {mutableStateOf(false)}

    var fotos_producto by remember {mutableStateOf(false)}

    var fotos_promociones by remember {mutableStateOf(false)}

    var fotosAmbientales by remember { mutableStateOf<List<String>>(emptyList()) }

    var fotosServicios by remember { mutableStateOf<List<String>>(emptyList()) }

    var fotosPromociones by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var logoOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var logoActual by rememberSaveable { mutableStateOf<String?>(null) }
    var hayCambiosLogo by remember { mutableStateOf(false) }
    var guardandoLogo by remember { mutableStateOf(false) }


    var valor_img_completa by remember { mutableStateOf("") }
    var mostrarDialogozoom by remember { mutableStateOf(false) }
    var lista_servicios_comodidades by remember {
        mutableStateOf<List<servicio_comodidad>>(emptyList())
    }

    LaunchedEffect(Unit) {
        viewmodel.cargarIdSocio(context)
    }

    LaunchedEffect(id_tienda, horarioMap) {
        viewModelFiltros.calcularHorarioParaTienda(id_tienda, horarioMap)
    }
    LaunchedEffect(id_tienda) {
        if (id_tienda != "") {
            viewmodel.carga_img_tipo("ambientales", id_tienda)
            viewmodel.carga_img_tipo("servicios_productos", id_tienda)
        }
    }

    LaunchedEffect(localidad_seleciondad) {
        if (localidad_seleciondad != "") {
            set_localidad_tienda_soscio(context, localidad_seleciondad.lowercase())
        }
    }
    LaunchedEffect(verificarSeccion) {
        val (existe, msje, idConfirmado) = verificarSeccion

        when {
            existe && !idConfirmado.isNullOrEmpty() -> {
                mostar_progeres_var_en_btn = true

                delay(2000)

                // Guardar en SharedPreferences / DataStore
                scope.launch {
                    set_id_socio(context, idConfirmado)
                }


                mostar_progeres_var_en_btn = false
            }

            !existe && msje.isNotEmpty() -> {
                mostar_progeres_var_en_btn = true
                delay(1500)
                mostar_progeres_var_en_btn = false

                snackbarHostState.showSnackbar(
                    message = msje,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            logoActual = it.toString()   // ✅ preview inmediata
            hayCambiosLogo = true        // ✅ muestra botones
        }
    }



    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Crossfade(
            targetState = when {
                cargando -> "cargando"
                idSocio.isEmpty() -> "formulario"
                else -> "contenido"
            }
        ) { screen ->
            when (screen) {
                "cargando" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .zIndex(10f),
                        contentAlignment = Alignment.Center
                    ) {
                        carga_inicial()
                    }
                }

                "formulario" -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(R.drawable.fondo_img_parte_geinz)
                                    .placeholder(R.drawable.cargando_img_categorias)
                                    .error(R.drawable.cargando_img_categorias).build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.70f),
                                                Color.Black.copy(alpha = 0.70f),
                                                Color.Black.copy(alpha = 0.70f),
                                                Color.Black.copy(alpha = 0.70f)
                                            )
                                        )
                                    )
                            )

                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp), contentAlignment = Alignment.Center
                        ) {
                            Column(
                                Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                texto_generico_one_line(
                                    "¿Eres socio de Geinz?",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                spacer_vertical(10.dp)
                                texto_generico_multilinea(
                                    "Ingresa tu ID y descubre el impacto real de tu negocio. " +
                                            "Conoce cuántas personas visitaron tu perfil, cuántos lo guardaron como favorito " +
                                            "y actualiza tu horario en solo segundos.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                spacer_vertical(10.dp)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    texto_generico_one_line("Selecciona tu localidad")
                                    spacer_vertical(12.dp)
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(opciones_localida) { localidad ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (localidad_seleciondad == localidad) Color.White
                                                        else MaterialTheme.colorScheme.primary
                                                    )
                                                    .clickable {
                                                        localidad_seleciondad = localidad
                                                    }
                                                    .padding(5.dp)
                                            ) {
                                                texto_generico_one_line(
                                                    localidad,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (localidad_seleciondad == localidad) Color.Black else Color.White,
                                                    modifier = Modifier.padding ( horizontal = 5.dp, vertical = 4.dp )
                                                )
                                            }
                                        }
                                    }
                                }
                                spacer_vertical(10.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    this@Column.AnimatedVisibility(
                                        !ingresar_correo,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                            ) {
                                                MyOutlinedTextField(
                                                    value = id_registrado,
                                                    onValueChange = { id_registrado = it },
                                                    labelText = "Pega tu ID",
                                                    placeholderText = "Pega tu ID"
                                                )
                                            }
                                            spacer_horizonta(2.dp)
                                            constantes_expandibles_generales.pegar_porta_papeles (clipboard,context,{ it->
                                                id_registrado = it
                                            })
                                        }
                                    }

                                    this@Column.AnimatedVisibility(
                                        ingresar_correo,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        MyOutlinedTextField(
                                            value = correo_electronico_cuenta_user,
                                            onValueChange = { correo_electronico_cuenta_user = it },
                                            labelText = "Ingresa tu correo electronico",
                                            placeholderText = "Ingresa tu correo electronico",
                                            keyboardType = KeyboardType.Email
                                        )

                                    }

                                }
                                spacer_vertical(5.dp)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (!ingresar_correo) "Ingresar con cuenta de usuario (solo vinculados)" else "Ingresar con ID ",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.Underline,
                                            color = Color.White
                                        ),
                                        modifier = Modifier
                                            .padding(start = 5.dp)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }) {
                                                ingresar_correo = !ingresar_correo
                                                correo_electronico_cuenta_user = ""
                                                id_registrado = ""
                                            }
                                    )
                                }
                                spacer_vertical(15.dp)
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f) // Para que solo ocupe parte del Row
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                if (!isConnected) {
                                                    // Sin conexión
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "No cuentas con conexión a internet para iniciar sesión",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } else {
                                                    if (ingresar_correo) {
                                                        // Flujo por correo
                                                        if (correo_electronico_cuenta_user.isEmpty()) {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Ingresa tu correo electrónico",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        } else if (localidad_seleciondad.isEmpty()) {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Selecciona la localidad de la tienda",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        } else {
                                                            viewmodel.verificar_existencia_tienda(
                                                                uid_respald_user,
                                                                ingresar_correo,
                                                                correo_electronico_cuenta_user,
                                                                id_registrado,
                                                                localidad_seleciondad
                                                            )
                                                        }
                                                    } else {
                                                        if (id_registrado.isEmpty()) {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Ingresa tu id de tienda",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        } else if (localidad_seleciondad.isEmpty()) {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Selecciona la localidad de la tienda",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        } else {
                                                            viewmodel.verificar_existencia_tienda(
                                                                uid_respald_user,
                                                                ingresar_correo,
                                                                correo_electronico_cuenta_user,
                                                                id_registrado,
                                                                localidad_seleciondad
                                                            )
                                                        }

                                                    }
                                                }

                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            texto_generico_one_line(
                                                if (mostar_progeres_var_en_btn) {
                                                    "Verificando"
                                                } else {
                                                    "Acceder"
                                                },

                                                modifier = Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 12.dp
                                                )
                                            )
                                            if (mostar_progeres_var_en_btn) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                        }
                        BtnSoporte("problemas", context, uid_respald_user)
                    }
                }

                "contenido" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {

                                    when (val state = state_socio.value) {

                                        is viewmodel_eres_socio.carga_acces_socio.loading -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }

                                        is viewmodel_eres_socio.carga_acces_socio.error -> {}

                                        is viewmodel_eres_socio.carga_acces_socio.succes -> {
                                            val datos = state.datos
                                            id_tienda = datos.id_tienda
                                            horarioMap = datos.horario_tiendaMap
                                            localidad_tienda = datos.localidad_tienda
                                            fecha_termino = datos.fecha_termino
                                            listaPropietarios = datos.lista_ids_propietarios
                                            fotosAmbientales =
                                                datos.obtener_img_tiendas.lista_ambiernte
                                            fotosServicios =
                                                datos.obtener_img_tiendas.lista_productos
                                            fotosPromociones =
                                                datos.obtener_img_tiendas.lista_promociones
                                            metodos_pago = datos.metodos_pago
                                            metodo_contact = datos.metodo_contacto_tienda
                                            lista_servicios_comodidades=datos.servicios_comodidades

                                            LaunchedEffect(datos.id_tienda) {
                                                logoOriginal = datos.obtener_img_tiendas.logo_tienda
                                                logoActual = datos.obtener_img_tiendas.logo_tienda
                                                hayCambiosLogo = false
                                            }


                                            var values by remember { mutableStateOf(listOf<Float>()) }

                                            var values2 by remember { mutableStateOf(listOf<Float>()) }

                                            var values3 by remember { mutableStateOf(listOf<Float>()) }

                                            var values4 by remember { mutableStateOf(listOf<Float>()) }

                                            var nombre_negocio by remember { mutableStateOf(datos.nombre) }
                                            var descripcion_negocio by remember { mutableStateOf(datos.descripcion) }
                                            var nombre_negocio_actual by remember { mutableStateOf(datos.nombre) }
                                            var descripcion_actual by remember { mutableStateOf(datos.descripcion) }

                                            LaunchedEffect(datos) {
                                                values2 = listOf(
                                                    datos.fb.toFloat(),
                                                    datos.ig.toFloat(),
                                                    datos.tk.toFloat(),
                                                    datos.stweb.toFloat()
                                                )

                                                values = listOf(
                                                    datos.total_vista.toFloat(),
                                                    datos.total_guardados.toFloat(),
                                                    datos.clic.toFloat(),
                                                    datos.compartidos.toFloat()
                                                )
                                                values3 = listOf(
                                                    datos.llamada.toFloat(),
                                                    datos.wsap.toFloat(),
                                                    datos.ruta.toFloat()
                                                )

                                                values4 = listOf(
                                                    datos.perfil_qr.toFloat(),
                                                    datos.review_c_qr.toFloat(),
                                                    datos.review_qr.toFloat(),
                                                    datos.crear_ruta_qr.toFloat()
                                                )
                                            }

                                            val (dias, color) = obtenerDiasYColor(fecha_termino)
                                            val datos_fechas = datos_tienda_fechas(
                                                datos.id_tienda,
                                                datos.fecha_ingreso,
                                                datos.fecha_termino,
                                                dias.toString(),
                                                color,
                                                datos.saldo_disponible_tienda.toString() ?: "0",
                                            )

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(top = 20.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 16.dp,
                                                            vertical = 10.dp
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Título centrado
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f), // Ocupa todo el espacio disponible
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        texto_generico_one_line(
                                                            "Bienvenido a GEINZ PANEL",
                                                            style = MaterialTheme.typography.titleLarge,
                                                            color = Color.White
                                                        )
                                                    }

                                                    // Ícono de logout alineado a la derecha
                                                    Icon(
                                                        imageVector = Icons.Default.ExitToApp,
                                                        contentDescription = "Cerrar sesión",
                                                        tint = Color.White,
                                                        modifier = Modifier.clickable {
                                                            cerrar_Seccion_cuenta_tienda = true
                                                        }
                                                    )
                                                }


                                                spacer_vertical(10.dp)

                                                texto_generico_multilinea(
                                                    "Hola, aquí puedes ver la información principal de ${datos.nombre}.  Accede a las estadísticas de vistas, guardados y clics, y actualiza el horario de tu tienda de forma rápida y sencilla.",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                spacer_vertical(10.dp)

                                                Column(
                                                    Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .animateContentSize(),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .height(170.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .clickable {
                                                                mostar_horario__bool =
                                                                    !mostar_horario__bool
                                                            }
                                                    ) {
                                                        AsyncImage(
                                                            model = ImageRequest.Builder(context)
                                                                .data(logoActual)
                                                                .placeholder(R.drawable.cargando_img_categorias)
                                                                .error(R.drawable.cargando_img_categorias)
                                                                .build(),
                                                            contentDescription = null,
                                                            modifier = Modifier.matchParentSize(),
                                                            contentScale = ContentScale.Crop
                                                        )

                                                        Icon(
                                                            imageVector = Icons.Default.OpenInFull,
                                                            contentDescription = "Expandir imagen",
                                                            tint = Color.White,
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .padding(6.dp)
                                                                .size(30.dp)
                                                                .zIndex(10f)
                                                                .background(
                                                                    Color.Black.copy(alpha = 0.6f),
                                                                    CircleShape
                                                                )
                                                                .clickable {
                                                                    valor_img_completa =
                                                                        logoActual ?: ""
                                                                    mostrarDialogozoom = true
                                                                }
                                                                .padding(4.dp)
                                                        )

                                                        if (mostar_horario__bool) {
                                                            Icon(
                                                                imageVector = if (hayCambiosLogo) Icons.Default.Undo else Icons.Default.Edit, // o ZoomOutMap
                                                                contentDescription = "Expandir imagen",
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .padding(6.dp)
                                                                    .size(30.dp)
                                                                    .background(
                                                                        Color.Black.copy(alpha = 0.6f),
                                                                        CircleShape
                                                                    )
                                                                    .clickable {
                                                                        if (hayCambiosLogo) {
                                                                            logoActual =
                                                                                logoOriginal
                                                                            hayCambiosLogo = false
                                                                        } else {

                                                                            launcher.launch("image/*")
                                                                        }
                                                                    }
                                                                    .padding(4.dp)
                                                            )
                                                        }

                                                        this@Column.AnimatedVisibility(
                                                            !mostar_horario__bool,
                                                            modifier = Modifier
                                                                .matchParentSize()
                                                        ) {
                                                            Box(modifier = Modifier
                                                                    .matchParentSize()
                                                                    .background(Color(0x99000000)))
                                                        }
                                                    }

                                                    this@Column.AnimatedVisibility(
                                                        visible = hayCambiosLogo,
                                                        enter = fadeIn(),
                                                        exit = fadeOut()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(
                                                                    top = 10.dp,
                                                                    start = 10.dp,
                                                                    end = 10.dp
                                                                ),
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                10.dp
                                                            )
                                                        ) {
                                                            Button(
                                                                modifier = Modifier.weight(1f),
                                                                enabled = !guardandoLogo && logoActual != null && logoActual != logoOriginal,
                                                                onClick = {
                                                                    guardandoLogo = true
                                                                    subir_storage_perfil_img(
                                                                        context = context,
                                                                        idTienda = id_tienda,
                                                                        valor = logoActual!!,
                                                                        onSuccess = { urlFinal ->
                                                                            scope.launch {
                                                                                val subido = subir_foto_perfil_algolia_normal(
                                                                                    id_tienda,
                                                                                    urlFinal
                                                                                )

                                                                                guardandoLogo = false

                                                                                if (subido) {
                                                                                    logoOriginal = urlFinal
                                                                                    logoActual = urlFinal
                                                                                    hayCambiosLogo = false
                                                                                }
                                                                            }
                                                                        },
                                                                        onError = {
                                                                            guardandoLogo = false
                                                                        }
                                                                    )
                                                                }
                                                            ) {
                                                                if (guardandoLogo) {
                                                                    CircularProgressIndicator(
                                                                        modifier = Modifier.size(18.dp),
                                                                        strokeWidth = 2.dp,
                                                                        color = Color.White
                                                                    )
                                                                } else {
                                                                    texto_generico_one_line("Guardar cambios ")
                                                                }
                                                            }

                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .then(
                                                                if (!mostar_horario__bool) Modifier.height(
                                                                    30.dp
                                                                ) else Modifier
                                                            )
                                                            .animateContentSize(),
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        this@Column.AnimatedVisibility(
                                                            visible = !mostar_horario__bool,
                                                            enter = fadeIn(),
                                                            exit = fadeOut()
                                                        ) {
                                                            Text(
                                                                text = datos.nombre.capitalizeFirst(),
                                                                fontFamily = baners_geinz_work,
                                                                fontSize = 20.sp,
                                                                modifier = Modifier.padding(
                                                                    start = 10.dp,
                                                                    end = 7.dp
                                                                )
                                                            )
                                                        }
                                                        this@Column.AnimatedVisibility(
                                                            visible = mostar_horario__bool,
                                                            enter = fadeIn(),
                                                            exit = fadeOut(), modifier = Modifier.padding(horizontal = 10.dp)
                                                        ) {
                                                            Column {

                                                                custom_texFiel(
                                                                    value = nombre_negocio,
                                                                    onValueChange = { nombre_negocio = it },
                                                                    labelText = "Nombre del negocio",
                                                                    placeholderText = "Nombre del negocio"
                                                                )

                                                                AnimatedVisibility (nombre_negocio !=nombre_negocio_actual) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clip(CircleShape)
                                                                            .background(MaterialTheme.colorScheme.primary)
                                                                            .padding(vertical = 10.dp)
                                                                            .clickable(
                                                                                indication = null,
                                                                                interactionSource = remember { MutableInteractionSource() }
                                                                            ) {
                                                                             viewmodel.cambiar_nombre_descripcion(datos.localidad_tienda,datos.id_tienda,"nombre_tienda",nombre_negocio)
                                                                                nombre_negocio_actual=nombre_negocio
                                                                                scope.launch {
                                                                                    snackbarHostState.showSnackbar(
                                                                                        message = "Cambios guardados correctamente",
                                                                                        duration = SnackbarDuration.Short
                                                                                    )
                                                                                }
                                                                            },

                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        texto_generico_one_line(
                                                                            "Guardar cambios",
                                                                            style = MaterialTheme.typography.bodyMedium
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }


                                                    Crossfade(
                                                        targetState = mostar_horario__bool,
                                                        label = ""
                                                    ) { estado ->
                                                        if (estado) {
                                                            Column(
                                                                modifier = Modifier.padding(
                                                                    start = 10.dp,
                                                                    end = 10.dp,
                                                                    bottom = 20.dp
                                                                ).animateContentSize(
                                                                    animationSpec = tween(
                                                                        durationMillis = 400,
                                                                        easing = FastOutSlowInEasing
                                                                    )
                                                                ),

                                                                verticalArrangement = Arrangement.spacedBy(
                                                                    7.dp
                                                                )
                                                            ) {
                                                                Column {

                                                                    custom_texFiel(
                                                                        rounder = 10,
                                                                        value = descripcion_negocio,
                                                                        onValueChange = { descripcion_negocio = it },
                                                                        labelText = "Descripción del negocio",
                                                                        placeholderText = "Descripción del negocio"
                                                                    )

                                                                    AnimatedVisibility (descripcion_negocio != descripcion_actual) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .clip(CircleShape)
                                                                                .background(MaterialTheme.colorScheme.primary)
                                                                                .padding(vertical = 10.dp)
                                                                                .clickable(
                                                                                    indication = null,
                                                                                    interactionSource = remember { MutableInteractionSource() }
                                                                                ) {
                                                                                    viewmodel.cambiar_nombre_descripcion(datos.localidad_tienda,datos.id_tienda,"descripcion",descripcion_negocio)
                                                                                    descripcion_actual = descripcion_negocio
                                                                                    scope.launch {
                                                                                        snackbarHostState.showSnackbar(
                                                                                            message = "Cambios guardados correctamente",
                                                                                            duration = SnackbarDuration.Short
                                                                                        )
                                                                                    }
                                                                                },
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            texto_generico_one_line(
                                                                                "Guardar cambios",
                                                                                style = MaterialTheme.typography.bodyMedium
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                spacer_vertical(20.dp)
                                                                Column(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                        .fillMaxWidth(),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(40.dp)
                                                                            .clickable {
                                                                                fotos_ambiente =
                                                                                    !fotos_ambiente
                                                                            }
                                                                            .padding(horizontal = 16.dp),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        texto_generico_one_line("Fotos de ambiente \uD83C\uDF06")
                                                                    }

                                                                    AnimatedVisibility(
                                                                        fotos_ambiente
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .padding(8.dp)
                                                                        ) {
                                                                            BoxFotosTipos(
                                                                                "ambientales",
                                                                                id_tienda,
                                                                                fotosAmbientales, 6
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                spacer_vertical(10.dp)

                                                                Column(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                        .fillMaxWidth(),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(40.dp)
                                                                            .clickable {
                                                                                fotos_producto =
                                                                                    !fotos_producto
                                                                            }
                                                                            .padding(horizontal = 16.dp),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        texto_generico_one_line("Fotos de productos o servicios 👣")
                                                                    }

                                                                    AnimatedVisibility(
                                                                        fotos_producto
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .padding(8.dp)
                                                                        ) {
                                                                            BoxFotosTipos(
                                                                                "servicios_productos",
                                                                                id_tienda,
                                                                                fotosServicios, 6
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                spacer_vertical(10.dp)

                                                                Column(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                        .fillMaxWidth(),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {

                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(40.dp)
                                                                            .clickable {
                                                                                fotos_promociones =
                                                                                    !fotos_promociones
                                                                            }
                                                                            .padding(horizontal = 16.dp),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        texto_generico_one_line("Promociones recientes \u200B\u200B❤\uFE0F\u200D\uD83D\uDD25\u200B")
                                                                    }

                                                                    AnimatedVisibility(
                                                                        fotos_promociones
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .padding(8.dp)
                                                                        ) {
                                                                            BoxFotosTipos(
                                                                                "promociones",
                                                                                id_tienda,
                                                                                fotosPromociones, 3
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
//                                                            custom_texFiel(
//                                                                value =datos.descripcion,
//                                                                onValueChange = { /* tu lógica */ },
//                                                                labelText = "Descripcion del negocio",
//                                                                placeholderText = "Descripcion del negocio"
//                                                            )
                                                            text_expandible_wrapp(
                                                                modifier = Modifier.padding(
                                                                    start = 10.dp,
                                                                    end = 10.dp,
                                                                    bottom = 20.dp
                                                                ),
                                                                texto = datos.descripcion,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                maxlines = 3

                                                            )
                                                        }
                                                    }

                                                }




                                                spacer_vertical(10.dp)

                                                Cartas_expandibles(
                                                    modifier = Modifier.padding(
                                                        vertical = 10.dp
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .animateContentSize() // ← Animación suave
                                                    ) {
                                                        expandibles_wrapp_socio_geinzz_horario_atencion(
                                                            tick = _tick,
                                                            viewModelFiltros = viewModelFiltros,
                                                            dia = DiaHoy(),
                                                            isConnected = isConnected,
                                                            viewmodel = viewmodel,
                                                            expandido = mostar_horario_teinda,
                                                            datos = datos,
                                                            onClickExpand = {
                                                                mostar_horario_teinda =
                                                                    !mostar_horario_teinda
                                                            }, sin_conexion = {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "No puedes realizar cambios sin conexion a internet",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }, {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Por favor, completa todos los campos antes de actualizar el horario.",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }, { msje ->
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = msje,
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            })
                                                    }
                                                }

                                                Cartas_expandibles(
                                                    modifier = Modifier.padding(
                                                        vertical = 10.dp
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .animateContentSize() // ← Animación suave
                                                    ) {
                                                        expandibles_wrapp_socio_geinzz_datos_tienda(
                                                            viewModelFiltros = viewmodel,
                                                            context = context,
                                                            expandido = mostrar_datos_teinda,
                                                            datos_tienda_fechas = datos_fechas
                                                        ) {
                                                            mostrar_datos_teinda =
                                                                !mostrar_datos_teinda
                                                        }
                                                    }
                                                }
                                                Cartas_expandibles(
                                                    modifier = Modifier.padding(
                                                        vertical = 10.dp
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .animateContentSize() // ← Animación suave
                                                    ) {
                                                        expandibles_wrapp_socio_contacto_tienda(
                                                            viewModelFiltros = viewmodel,
                                                            context = context,
                                                            expandido = mostrar_redes_tienda,
                                                            it = metodo_contact,
                                                            datos_tienda = cambiar_datos_pago_contacto(
                                                                id_tienda,
                                                                datos.localidad_tienda
                                                            ),
                                                            onClickExpand = {
                                                                mostrar_redes_tienda =
                                                                    !mostrar_redes_tienda
                                                            },
                                                            cambios_guardados = {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Cambios guardados correctamente",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            })
                                                    }
                                                }

                                                Cartas_expandibles(
                                                    modifier = Modifier.padding(
                                                        vertical = 10.dp
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .animateContentSize() // ← Animación suave
                                                    ) {
                                                        expandibles_wrapp_socio_metodos_pago_tienda(
                                                            viewModelFiltros = viewmodel,
                                                            context = context,
                                                            expandido = mostar_metodos_pago_tienda,
                                                            metodos_pago = metodos_pago,
                                                            datos_tienda = cambiar_datos_pago_contacto(
                                                                id_tienda,
                                                                datos.localidad_tienda
                                                            ), onClickExpand = {
                                                                mostar_metodos_pago_tienda =
                                                                    !mostar_metodos_pago_tienda
                                                            }, guardar_dado_datos = {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Cambios guardados correctamente",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            })
                                                    }
                                                }

                                                Cartas_expandibles(
                                                    modifier = Modifier.padding(
                                                        vertical = 10.dp
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .animateContentSize() // ← Animación suave
                                                    ) {
                                                        expandible_wrap_socio_atrubitos(
                                                            datos.id_tienda,datos.localidad_tienda,
                                                            lista_servicios_comodidades,
                                                            viewModelFiltros = viewmodel,
                                                            expandido = mostar_atributos_negocios,{
                                                                mostar_atributos_negocios =
                                                                    !mostar_atributos_negocios
                                                            },{
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Cambios guardados correctamente",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }
                                                      )
                                                    }
                                                }

                                                Cartas_expandibles(
                                                    modifier = Modifier.padding(
                                                        vertical = 10.dp
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .animateContentSize() // ← Animación suave
                                                    ) {
                                                        expandido_wrap_socio_atributos(
                                                            id_tienda = datos.id_tienda,
                                                            localida = datos.localidad_tienda,
                                                            aforo_max = datos.aforo.toString(),
                                                            viewModelFiltros = viewmodel,
                                                            expandido = mostar_socio_atributos,
                                                            onClickExpand = {mostar_socio_atributos=!mostar_socio_atributos},
                                                            guardar_dado_datos = {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Cambios guardados correctamente",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            })
                                                    }
                                                }


                                                val lsita_datos1 = listOf(
                                                    datos_grafico(
                                                        enable = datos.total_vista != 0,
                                                        img_ = R.drawable.vizualizacion_icon_3d,
                                                        label = "Vistas de perfil",
                                                        cantidad = datos.total_vista.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.total_guardados != 0,
                                                        img_ = R.drawable.corazon_gracias,
                                                        label = "Guardados de perfil",
                                                        cantidad = datos.total_guardados.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.clic != 0,
                                                        img_ = R.drawable.click_icon3d,
                                                        label = "Clics en perfil",
                                                        cantidad = datos.clic.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.compartidos != 0,
                                                        img_ = R.drawable.compartir_icon_vector,
                                                        label = "Compartidos",
                                                        cantidad = datos.compartidos.toString()
                                                    )
                                                )
                                                AnimatedVisibility(lsita_datos1.any { it.enable }) {
                                                    Cartas_expandibles(
                                                        modifier = Modifier.padding(
                                                            vertical = 10.dp
                                                        )
                                                    ) {
                                                        Column() {
                                                            val lsita_datos1filtrada =
                                                                lsita_datos1.filter { it.enable }
                                                            expandibles_wrapp_socio_geinzz(
                                                                lsita_datos1filtrada,
                                                                "El interés real muestra cuántas personas se detienen a ver tu perfil por más de 6 segundos. Esta métrica refleja la atención genuina que tu negocio genera dentro de la plataforma",
                                                                texto_params = "Interés real",
                                                                expandido = mostar_interes,
                                                                onClickExpand = {
                                                                    mostar_interes = !mostar_interes
                                                                }
                                                            )
                                                        }
                                                        AnimatedVisibility(visible = mostar_interes) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .animateContentSize(),
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {
                                                                LazyRow(
                                                                    contentPadding = PaddingValues(
                                                                        horizontal = 15.dp
                                                                    ),
                                                                    horizontalArrangement = Arrangement.spacedBy(
                                                                        10.dp
                                                                    )
                                                                ) {
                                                                    itemsIndexed(values) { index, value ->
                                                                        if (value.toInt() != 0) {
                                                                            Row(
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.Center,
                                                                                modifier = Modifier.clickable(
                                                                                    indication = null,
                                                                                    interactionSource = remember { MutableInteractionSource() }) {
                                                                                    when (labels[index]) {
                                                                                        "Vistas" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "Vistas"
                                                                                            txt_leyenda =
                                                                                                "Las vistas se registran cuando un usuario permanece viendo tu perfil durante más de 6 segundos. Representan el interés real que genera tu negocio."
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.vizualizacion_icon_3d
                                                                                        }

                                                                                        "Guardados" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "Guardados"
                                                                                            txt_leyenda =
                                                                                                "Los guardados indican cuántos usuarios añadieron a ${datos.nombre} a su lista de favoritos. Es una métrica que refleja cuánta gente quiere volver a encontrar tu tienda rápidamente."
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.corazon_gracias
                                                                                        }

                                                                                        "Clics" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "clics"
                                                                                            txt_leyenda =
                                                                                                "Los clics representan cuántos usuarios tocaron tu negocio y abrieron directamente el perfil de la tienda o negocio. Miden la intención inmediata de conocer más sobre ti."
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.click_icon3d
                                                                                        }

                                                                                        "Compartidos" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "Compartidos"
                                                                                            txt_leyenda =
                                                                                                "Los compartidos representan cuántos usuarios compartieron directamente el perfil de la tienda o negocio. Haciendo que mas personas te conozcan"
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.compartir_icon_vector
                                                                                        }
                                                                                    }

                                                                                }
                                                                            ) {
                                                                                Box(
                                                                                    Modifier
                                                                                        .size(12.dp)
                                                                                        .background(
                                                                                            color = listOf(
                                                                                                Color(
                                                                                                    0xFFFF6B6B
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF4ECDC4
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF4EFF00
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF0037FF
                                                                                                ),
                                                                                            )[index],
                                                                                            shape = CircleShape
                                                                                        )
                                                                                )

                                                                                spacer_horizonta(8.dp)

                                                                                texto_generico_one_line(
                                                                                    "${labels[index]}: ${value.toInt()}",
                                                                                    MaterialTheme.typography.bodyMedium
                                                                                )
                                                                            }

                                                                        }
                                                                    }
                                                                }
                                                                spacer_vertical(10.dp)
                                                                PieChart(
                                                                    dataSet = values.toChartDataSet(
                                                                        labels = labels,
                                                                        title = "",
                                                                        postfix = ""     // nada
                                                                    ),
                                                                    style = PieChartDefaults.style(
                                                                        donutPercentage = 40f,
                                                                        pieColors = listOf(
                                                                            Color(0xFFFF6B6B),
                                                                            Color(0xFF4ECDC4),
                                                                            Color(0xFF4EFF00),
                                                                            Color(0xFF0037FF)
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                val lsita_datos2 = listOf(
                                                    datos_grafico(
                                                        enable = datos.fb != 0,
                                                        img_ = R.drawable.facebook_icon,
                                                        label = "Facebook",
                                                        cantidad = datos.fb.toString()
                                                    ), datos_grafico(
                                                        enable = datos.ig != 0,
                                                        img_ = R.drawable.instagram_icon,
                                                        label = "Instagram",
                                                        cantidad = datos.ig.toString()
                                                    ), datos_grafico(
                                                        enable = datos.tk != 0,
                                                        img_ = R.drawable.tik_tok_icon,
                                                        label = "Tik tok",
                                                        cantidad = datos.tk.toString()
                                                    ), datos_grafico(
                                                        enable = datos.stweb != 0,
                                                        R.drawable.web_icon,
                                                        "Sitio web",
                                                        datos.stweb.toString()
                                                    )
                                                )
                                                AnimatedVisibility(lsita_datos2.any { it.enable }) {
                                                    Cartas_expandibles(
                                                        modifier = Modifier.padding(
                                                            vertical = 10.dp
                                                        )
                                                    ) {
                                                        Column() {
                                                            val lsita_datos1filtrada =
                                                                lsita_datos2.filter { it.enable }
                                                            expandibles_wrapp_socio_geinzz(
                                                                lsita_datos1filtrada,
                                                                "Este indicador muestra cuántas personas hicieron clic en tus perfiles de redes sociales o en tu sitio web después de ver tu página. Refleja el nivel de intención que tiene el usuario de saber más sobre tu negocio y avanzar hacia un contacto directo",
                                                                texto_params = "Convesion",
                                                                expandido = mostrar_convesion,
                                                                onClickExpand = {
                                                                    mostrar_convesion =
                                                                        !mostrar_convesion
                                                                }
                                                            )
                                                        }
                                                        AnimatedVisibility(visible = mostrar_convesion) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .animateContentSize(),
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {
                                                                LazyRow(
                                                                    contentPadding = PaddingValues(
                                                                        horizontal = 10.dp
                                                                    ),
                                                                    horizontalArrangement = Arrangement.spacedBy(
                                                                        10.dp
                                                                    )
                                                                ) {
                                                                    itemsIndexed(values2) { index, value ->
                                                                        if (value.toInt() != 0) {
                                                                            Row(
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.Center,

                                                                                modifier = Modifier
                                                                                    .padding(
                                                                                        horizontal = 10.dp
                                                                                    )
                                                                                    .clickable(
                                                                                        indication = null,
                                                                                        interactionSource = remember { MutableInteractionSource() }) {
                                                                                        when (labels2[index]) {
                                                                                            "Facebook" -> {
                                                                                                dialog_mostar_leyendas_graficos =
                                                                                                    true
                                                                                                titulo_leyenda_dialog =
                                                                                                    "Facebook"
                                                                                                txt_leyenda =
                                                                                                    "Este valor representa cuántos usuarios hicieron clic en el botón de Facebook y fueron redirigidos al perfil oficial de la tienda. Es una métrica clave para medir el interés directo y la intención de conocer más sobre tu negocio."
                                                                                                icono_mostar_leyendas_graficos =
                                                                                                    R.drawable.facebook_icon
                                                                                            }

                                                                                            "Instagram" -> {
                                                                                                dialog_mostar_leyendas_graficos =
                                                                                                    true
                                                                                                titulo_leyenda_dialog =
                                                                                                    "Instagram"
                                                                                                txt_leyenda =
                                                                                                    "Aquí se muestra la cantidad de usuarios que tocaron el botón de Instagram y visitaron el perfil de la tienda. Estos clics indican una intención clara de explorar tu contenido, productos y publicaciones recientes."
                                                                                                icono_mostar_leyendas_graficos =
                                                                                                    R.drawable.instagram_icon
                                                                                            }

                                                                                            "TikTok" -> {
                                                                                                dialog_mostar_leyendas_graficos =
                                                                                                    true
                                                                                                titulo_leyenda_dialog =
                                                                                                    "TikTok"
                                                                                                txt_leyenda =
                                                                                                    "Este número refleja cuántas personas hicieron clic en el botón de TikTok para ver directamente el contenido de la tienda. Es una medida de la atracción y curiosidad que genera tu negocio en esta plataforma."
                                                                                                icono_mostar_leyendas_graficos =
                                                                                                    R.drawable.tik_tok_icon
                                                                                            }

                                                                                            "Sitio web" -> {
                                                                                                dialog_mostar_leyendas_graficos =
                                                                                                    true
                                                                                                titulo_leyenda_dialog =
                                                                                                    "Sitio web"
                                                                                                txt_leyenda =
                                                                                                    "Indica cuántos usuarios ingresaron a tu página web desde la app. Cada clic muestra el interés por obtener más información, ver tu catálogo completo o contactar directamente con tu negocio."
                                                                                                icono_mostar_leyendas_graficos =
                                                                                                    R.drawable.web_icon
                                                                                            }

                                                                                        }

                                                                                    }
                                                                            ) {
                                                                                Box(
                                                                                    Modifier
                                                                                        .size(12.dp)
                                                                                        .background(
                                                                                            color = listOf(
                                                                                                Color(
                                                                                                    0xFF1877F2
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFFE1306C
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF69C9D0
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF6366F1
                                                                                                ),
                                                                                            )[index],
                                                                                            shape = CircleShape
                                                                                        )
                                                                                )

                                                                                spacer_horizonta(8.dp)

                                                                                texto_generico_one_line(
                                                                                    "${labels2[index]}: ${value.toInt()}",
                                                                                    MaterialTheme.typography.bodyMedium
                                                                                )
                                                                            }

                                                                        }
                                                                    }
                                                                }
                                                                spacer_vertical(10.dp)
                                                                PieChart(
                                                                    dataSet = values2.toChartDataSet(
                                                                        labels = labels2,
                                                                        title = "",
                                                                        postfix = ""     // nada
                                                                    ),
                                                                    style = PieChartDefaults.style(
                                                                        donutPercentage = 40f,
                                                                        pieColors = listOf(
                                                                            Color(0xFF1877F2),
                                                                            Color(0xFFE1306C),
                                                                            Color(0xFF69C9D0),
                                                                            Color(0xFF6366F1),
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                val lsita_datos3 = listOf(
                                                    datos_grafico(
                                                        enable = datos.llamada != 0,
                                                        img_ = R.drawable.llamada_icon,
                                                        label = "Llamada",
                                                        cantidad = datos.llamada.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.wsap != 0,
                                                        img_ = R.drawable.whatsapp_icon,
                                                        label = "Whatsapp",
                                                        cantidad = datos.wsap.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.ruta != 0,
                                                        img_ = R.drawable.icon_3d_ruta,
                                                        label = "Rutas",
                                                        cantidad = datos.ruta.toString()
                                                    )
                                                )
                                                AnimatedVisibility(lsita_datos3.any { it.enable }) {
                                                    Cartas_expandibles(
                                                        modifier = Modifier.padding(
                                                            vertical = 10.dp
                                                        )
                                                    ) {
                                                        Column() {
                                                            val lsita_datos1filtrada =
                                                                lsita_datos3.filter { it.enable }
                                                            expandibles_wrapp_socio_geinzz(
                                                                lsita_datos1filtrada,
                                                                "Mide cuántas personas usaron accesos externos como WhatsApp, llamadas o enlaces directos para comunicarse contigo fuera de la plataforma.",
                                                                texto_params = "Tráfico externo",
                                                                expandido = mostrar_trafico_externo,
                                                                onClickExpand = {
                                                                    mostrar_trafico_externo =
                                                                        !mostrar_trafico_externo
                                                                }
                                                            )
                                                        }

                                                        AnimatedVisibility(visible = mostrar_trafico_externo) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .animateContentSize(),
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {
                                                                LazyRow(
                                                                    contentPadding = PaddingValues(
                                                                        horizontal = 10.dp
                                                                    ),
                                                                    horizontalArrangement = Arrangement.spacedBy(
                                                                        10.dp
                                                                    )
                                                                ) {
                                                                    itemsIndexed(values3) { index, value ->
                                                                        if (value.toInt() != 0) {
                                                                            Row(
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.Center,
                                                                                modifier = Modifier.clickable(
                                                                                    indication = null,
                                                                                    interactionSource = remember { MutableInteractionSource() }) {
                                                                                    when (labels3[index]) {
                                                                                        "Llamada" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "Llamada"
                                                                                            txt_leyenda =
                                                                                                "Este valor muestra cuántos usuarios tocaron el botón de llamada para comunicarse directamente con la tienda. Cada clic representa una intención clara de consultar precios, disponibilidad o realizar una compra inmediata."
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.llamada_icon
                                                                                        }

                                                                                        "Whatsapp" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "WhatsApp"
                                                                                            txt_leyenda =
                                                                                                "Indica cuántas personas hicieron clic en el botón de WhatsApp para chatear con la tienda. Es una métrica muy importante, ya que refleja la intención directa de solicitar información, hacer pedidos o coordinar una reserva."
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.whatsapp_icon
                                                                                        }

                                                                                        "Rutas" -> {
                                                                                            dialog_mostar_leyendas_graficos =
                                                                                                true
                                                                                            titulo_leyenda_dialog =
                                                                                                "Cómo llegar"
                                                                                            txt_leyenda =
                                                                                                "Este número muestra cuántos usuarios presionaron el botón de rutas para abrir el mapa y obtener indicaciones hacia la tienda. Cada clic demuestra un alto interés en visitar físicamente el negocio."
                                                                                            icono_mostar_leyendas_graficos =
                                                                                                R.drawable.icon_3d_ruta
                                                                                        }

                                                                                    }

                                                                                }
                                                                            ) {
                                                                                Box(
                                                                                    Modifier
                                                                                        .size(12.dp)
                                                                                        .background(
                                                                                            color = listOf(
                                                                                                Color(
                                                                                                    0xFF18C5A4
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF25D366
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF6A0DAD
                                                                                                )
                                                                                            )[index],
                                                                                            shape = CircleShape
                                                                                        )
                                                                                )

                                                                                spacer_horizonta(8.dp)

                                                                                texto_generico_one_line(
                                                                                    "${labels3[index]}: ${value.toInt()}",
                                                                                    MaterialTheme.typography.bodyMedium
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                spacer_vertical(10.dp)
                                                                PieChart(
                                                                    dataSet = values3.toChartDataSet(
                                                                        labels = labels3,
                                                                        title = "",
                                                                        postfix = ""     // nada
                                                                    ),
                                                                    style = PieChartDefaults.style(
                                                                        donutPercentage = 40f,
                                                                        pieColors = listOf(
                                                                            Color(0xFF18C5A4),
                                                                            Color(0xFF25D366),
                                                                            Color(0xFF6A0DAD)
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                val lsita_datos4 = listOf(
                                                    datos_grafico(
                                                        enable = datos.review_qr != 0,
                                                        img_ = R.drawable.review_qr,
                                                        label = "QR review publico",
                                                        cantidad = datos.review_qr.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.review_c_qr != 0,
                                                        img_ = R.drawable.review_c_qr,
                                                        label = "QR review privado",
                                                        cantidad = datos.review_c_qr.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.ruta != 0,
                                                        img_ = R.drawable.crear_ruta_qr,
                                                        label = "QR crear ruta",
                                                        cantidad = datos.ruta.toString()
                                                    ),
                                                    datos_grafico(
                                                        enable = datos.perfil_qr != 0,
                                                        img_ = R.drawable.perfil_qr,
                                                        label = "QR vista de perfil",
                                                        cantidad = datos.perfil_qr.toString()
                                                    )
                                                )
                                                AnimatedVisibility(lsita_datos4.any { it.enable }) {
                                                    Cartas_expandibles(
                                                        modifier = Modifier.padding(
                                                            vertical = 10.dp
                                                        )
                                                    ) {
                                                        Column() {
                                                            val lsita_datos1filtrada =
                                                                lsita_datos4.filter { it.enable }
                                                            expandibles_wrapp_socio_geinzz(
                                                                lsita_datos1filtrada,
                                                                "Mide cuántas personas interactúan con tu tienda a través de los códigos QR. Cada QR permite conocer cuántas veces fue escaneado y qué tipo de interacción generó, ayudándote a analizar el alcance y efectividad de cada punto de acceso.",
                                                                texto_params = "Actividad de códigos QR",
                                                                expandido = mostrar_qr_externo,
                                                                onClickExpand = {
                                                                    mostrar_qr_externo =
                                                                        !mostrar_qr_externo
                                                                }
                                                            )
                                                        }

                                                        AnimatedVisibility(visible = mostrar_qr_externo) {
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
                                                                    itemsIndexed(values4) { index, value ->
                                                                        if (value.toInt() != 0) {
                                                                            Row(
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.Center,
                                                                                modifier = Modifier.clickable(
                                                                                    indication = null,
                                                                                    interactionSource = remember { MutableInteractionSource() }) {
                                                                                    when (labels4[index]) {
                                                                                        "QR review privado" -> {
                                                                                            dialog_mostar_leyendas_graficos = true
                                                                                            titulo_leyenda_dialog = "QR review privado"
                                                                                            txt_leyenda =
                                                                                                "Este valor indica cuántos usuarios escanearon el QR de reseñas que se encuentra dentro de la aplicación, específicamente en el perfil de la tienda. Representa la interacción de usuarios que ya están navegando en la plataforma y deciden dejar o consultar una reseña."
                                                                                            icono_mostar_leyendas_graficos = R.drawable.review_c_qr
                                                                                        }

                                                                                        "QR review publico" -> {
                                                                                            dialog_mostar_leyendas_graficos = true
                                                                                            titulo_leyenda_dialog = "QR review publico"
                                                                                            txt_leyenda =
                                                                                                "Este valor muestra cuántas personas escanearon el QR de reseñas que el negocio tiene de forma física en su local. Cada escaneo representa a un usuario externo que interactúa directamente con el negocio para dejar o ver opiniones."
                                                                                            icono_mostar_leyendas_graficos = R.drawable.review_qr
                                                                                        }

                                                                                        "QR creacion de ruta" -> {
                                                                                            dialog_mostar_leyendas_graficos = true
                                                                                            titulo_leyenda_dialog = "QR creacion de ruta"
                                                                                            txt_leyenda =
                                                                                                "Este valor indica cuántos usuarios escanearon el QR de creación de rutas. Al hacerlo, se abre automáticamente el mapa con la ruta hacia el local, lo que demuestra una clara intención de visitar físicamente el negocio."
                                                                                            icono_mostar_leyendas_graficos = R.drawable.crear_ruta_qr
                                                                                        }

                                                                                        "QR vistas de perfil" -> {
                                                                                            dialog_mostar_leyendas_graficos = true
                                                                                            titulo_leyenda_dialog = "QR vistas de perfil"
                                                                                            txt_leyenda =
                                                                                                "Este valor muestra cuántos usuarios escanearon el QR del perfil del negocio. Al escanearlo, el usuario ingresa directamente al perfil de la tienda dentro de la aplicación para ver su información, servicios y contenido."
                                                                                            icono_mostar_leyendas_graficos = R.drawable.perfil_qr
                                                                                        }


                                                                                    }

                                                                                }
                                                                            ) {
                                                                                Box(
                                                                                    Modifier
                                                                                        .size(12.dp)
                                                                                        .background(
                                                                                            color = listOf(
                                                                                                Color(
                                                                                                    0xFFFADC7B
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFFDE3B41
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF565656
                                                                                                ),
                                                                                                Color(
                                                                                                    0xFF7FACE8
                                                                                                        )
                                                                                            )[index],
                                                                                            shape = CircleShape
                                                                                        )
                                                                                )

                                                                                spacer_horizonta(8.dp)

                                                                                texto_generico_one_line(
                                                                                    "${labels4[index]}: ${value.toInt()}",
                                                                                    MaterialTheme.typography.bodyMedium
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                spacer_vertical(10.dp)
                                                                PieChart(
                                                                    dataSet = values4.toChartDataSet(
                                                                        labels = labels4,
                                                                        title = "",
                                                                        postfix = ""
                                                                    ),
                                                                    style = PieChartDefaults.style(
                                                                        donutPercentage = 40f,
                                                                        pieColors = listOf(
                                                                            Color(0xFFFADC7B),
                                                                            Color(0xFFDE3B41),
                                                                            Color(0xFF565656),
                                                                            Color(0xFF7FACE8)
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                spacer_vertical(10.dp)


                                                if (!estaVinculado && isConnected) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            5.dp
                                                        ),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(20.dp))
                                                            .background(
                                                                MaterialTheme.colorScheme.surface
                                                            )
                                                            .padding(10.dp)
                                                    ) {
                                                        texto_generico_one_line(
                                                            "Vincula tu cuenta",
                                                            style = MaterialTheme.typography.titleLarge
                                                        )
                                                        texto_generico_multilinea(
                                                            "Vincula tu tienda con tu cuenta Geinz y gestiona todo desde tus dispositivos. Cada tienda puede asociar hasta 3 dispositivos por cuenta.",
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                        spacer_vertical(5.dp)
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(CircleShape)
                                                                .background(MaterialTheme.colorScheme.primary)
                                                                .clickable() {
                                                                    viewModelFiltros.vincular_cuenta(
                                                                        uid_respald_user,
                                                                        idSocio,
                                                                        localidad_tienda_select_
                                                                            ?: "barranca"
                                                                    )
                                                                    scope.launch {
                                                                        snackbarHostState.showSnackbar(
                                                                            message = "Cuenta vinculada correctamente",
                                                                            duration = SnackbarDuration.Short
                                                                        )
                                                                    }

                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            texto_generico_one_line(
                                                                "Vincular cuenta ahora",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                modifier = Modifier.padding(
                                                                    horizontal = 10.dp,
                                                                    vertical = 10.dp
                                                                )
                                                            )
                                                        }
                                                    }

                                                }
                                            }

                                        }

                                        else -> {}
                                    }
                                }

                                if (mostrarDialogozoom) {
                                    ZoomableGalleryFullScreen(
                                        compartir_promocion(),
                                        imagenes = listOf(valor_img_completa),
                                        startIndex = 0,
                                        onDismiss = { mostrarDialogozoom = false }
                                    )

                                }

                                if (dialog_mostar_leyendas_graficos) {
                                    dialog_mostar_leyendas_graficos(
                                        icono_mostar_leyendas_graficos,
                                        titulo_leyenda_dialog,
                                        txt_leyenda,
                                        { dialog_mostar_leyendas_graficos = false })
                                }

                            }
                        }
                        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                        BtnSoporte("Soporte", context, uid_respald_user)
                    }
                }

            }
        }

        if (cerrar_Seccion_cuenta_tienda) {
            dialogo_cerrar_seccion_teinda(
                txt = "¿Estás seguro de que deseas cerrar sesión de tu cuenta de tienda? Al hacerlo, tu dispositivo se desvinculará completamente de la tienda, incluyendo la eliminación del ID de tienda asociado a tu cuenta. Tendrás que volver a iniciar sesión y vincular nuevamente tu dispositivo para acceder otra vez.",
                ondimis = {
                    cerrar_Seccion_cuenta_tienda = !cerrar_Seccion_cuenta_tienda
                },
                cerrar_seccion = {
                    scope.launch {
                        id_registrado = ""
                        viewmodel.cambiar_estado_Seccion()
                        data_store_localidad.delete_id_socio(context)
                    }
                    viewModelFiltros.eliminarvincualcion_cuenta_tienda(
                        uid_respald_user,
                        idSocio,
                        localidad_tienda_select_
                            ?: "barranca"
                    )
                }
            )
        }
    }
}


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
            compartir_promocion(),
            imagenes = listOf(valor_img_completa),
            startIndex = 0,
            onDismiss = { mostrarDialogozoom = false }
        )

    }

}

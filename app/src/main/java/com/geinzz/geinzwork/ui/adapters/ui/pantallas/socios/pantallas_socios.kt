package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R

import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.data_store.data_store_localidad.set_id_socio
import com.geinzz.geinzwork.data_store.data_store_localidad.set_localidad_tienda_soscio
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialogo_cerrar_seccion_teinda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.opciones_localida
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Scaffold
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.BtnSoporte
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.carga_inicial

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.material.Badge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.geinzz.geinzwork.data.model.DatosPublicidadIA
import com.geinzz.geinzwork.data.model.datos_generaciones_sin_publicaicones
import com.geinzz.geinzwork.data.model.items_pantallas_promociones
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.firebaseAuth
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_recientes
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun login_socios(isConnected: Boolean, tipo_: String = "", navController: NavHostController,localidad_user:String) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var id_registrado by remember { mutableStateOf("") }
    val viewmodel: viewmodel_eres_socio = viewModel()
    val viewmodel_pantalla_reciente: viewmodel_pantallas_recientes = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val state_socio = viewmodel.state_eres_socio.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val verificarSeccion by viewmodel.verificarSeccion.collectAsState()
    var mostar_progeres_var_en_btn by remember { mutableStateOf(false) }
    val cargando by viewmodel.cargandoIdSocio.collectAsState()
    val idSocio by viewmodel.idSocio.collectAsState()
    var cerrar_Seccion_cuenta_tienda by remember { mutableStateOf(false) }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""
    var localidad_seleciondad by remember { mutableStateOf("") }
    val localidad_tienda_select_ by data_store_localidad.get_localidad_tienda_socio(context)
        .collectAsState(initial = "")
    var ingresar_correo by remember { mutableStateOf(false) }
    var correo_electronico_cuenta_user by remember { mutableStateOf("") }
    var item_pantalla_promociones by remember { mutableStateOf(items_pantallas_promociones()
    ) }
    var pantallaSeleccionada by remember { mutableStateOf("Inicio") }
    val viewmodel_pantalla_promocionar: viewmodel_pantallas_promocionar = viewModel()
    val mostarr_bundel_recientes by viewmodel_pantalla_promocionar.estado_envio_recientes.collectAsState()

    var mostrar_bundle_desbloqueo by remember { mutableStateOf(false) }
    var mostrar_bundle_recargas by remember { mutableStateOf(false) }
    var campoBloqueante by mutableStateOf<viewmodel_pantallas_promocionar.CampoPendiente?>(null)


    var nombre_tienda by remember { mutableStateOf("") }
    var localidad_tienda by remember { mutableStateOf("") }
    var id_tienda by remember { mutableStateOf("") }
    var moneda_total_tienda by remember { mutableStateOf(0) }
    var cargandoState by remember { mutableStateOf(false) }
    LaunchedEffect(tipo_) {
        if (tipo_.isNotEmpty() && tipo_.equals("envio")) {
            mostrar_bundle_desbloqueo = true
        } else if (tipo_.equals("recargas")) {
            mostrar_bundle_recargas = true
        }
    }
    LaunchedEffect(Unit) {
        viewmodel.cargarIdSocio(context)
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

    var mostrarDialogoSalir by remember { mutableStateOf(false) }
    var pantallaDestino by remember { mutableStateOf("") }


    fun intentarCambiarPantalla(nuevaPantalla: String) {
        if (
            pantallaSeleccionada == "Promocionar" &&
            viewmodel_pantalla_promocionar.hayCambiosSinGuardar()
        ) {
            pantallaDestino = nuevaPantalla
            campoBloqueante =
                viewmodel_pantalla_promocionar.obtenerCampoModificado()
            mostrarDialogoSalir = true
        } else {
            pantallaSeleccionada = nuevaPantalla
        }
    }


    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        BackHandler {
            if (
                pantallaSeleccionada == "Promocionar" &&
                viewmodel_pantalla_promocionar.hayCambiosSinGuardar()
            ) {
                pantallaDestino = "Inicio"
                campoBloqueante =
                    viewmodel_pantalla_promocionar.obtenerCampoModificado()
                mostrarDialogoSalir = true

            } else if (pantallaSeleccionada != "Inicio") {
                pantallaSeleccionada = "Inicio"

            } else {
                navController.navigate("pantalla_principal") {
                    popUpTo("pantalla_principal") { inclusive = true }
                }
            }
        }

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
                                                    modifier = Modifier.padding(
                                                        horizontal = 5.dp,
                                                        vertical = 4.dp
                                                    )
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
                                            constantes_expandibles_generales.pegar_porta_papeles(
                                                clipboard,
                                                context,
                                                { it ->
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
                                                                id_user,
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
                                                                id_user,
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
                        BtnSoporte("problemas", context, id_user)
                    }
                }

                "contenido" -> {
                    val pantallaGeinz = remember(state_socio.value) {
                        // Memoriza cada pantalla para que no se reconstruya completa al volver
                        @Composable {
                            when (val state = state_socio.value) {
                                is viewmodel_eres_socio.carga_acces_socio.loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is viewmodel_eres_socio.carga_acces_socio.error -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Error al cargar", color = Color.Red)
                                    }
                                }

                                is viewmodel_eres_socio.carga_acces_socio.succes -> {


                                    LaunchedEffect(state.datos) {

                                        item_pantalla_promociones = items_pantallas_promociones(
                                            state.datos.categoira_tienda,
                                            state.datos.nombre,
                                            state.datos.localidad_tienda,
                                            state.datos.obtener_img_tiendas.logo_tienda,
                                            state.datos.metodo_contacto_tienda.whatsapp.numero,
                                            state.datos.subcategorias_tienda,
                                            state.datos.ubicacion,
                                            state.datos.saldo_disponible_tienda,
                                            state.datos.id_tienda,
                                            state.datos.metodos_pago,
                                            state.datos.servicios_comodidades

                                        )
                                    }
                                    nombre_tienda = state.datos.nombre
                                    localidad_tienda = state.datos.localidad_tienda
                                    id_tienda = state.datos.id_tienda
                                    moneda_total_tienda =
                                        state.datos.saldo_disponible_tienda.toInt()
                                    pantalla_carga_socios(
                                        fecha_finalizado_flow = viewmodel.fecha_finalizar_panel_real_time,
                                        id_user = id_user,
                                        datos = state.datos,
                                        isConnected = isConnected,
                                        id_registrado = { valor ->
                                            id_registrado = valor
                                        },
                                        navegarcrear_pùblicidad_titulo_descripcion = { titulo, descrpcion, tipo, id_generacion, datos_generaciones_sin_publicaicones ->
                                            pantallaSeleccionada = "Promocionar"
                                            val datos = DatosPublicidadIA(
                                                titulo = titulo,
                                                descripcion = descrpcion,
                                                whatsapp = "",
                                                compartir = "",
                                                tipo_redirigido = tipo,
                                                id_generacion_sin_publicar = id_generacion,
                                                datos_generaciones = datos_generaciones_sin_publicaicones
                                            )
                                            viewmodel.setear_datos_datos_publicada_IA(datos)
                                        },
                                        navegarcrear_pùblicidad_wsap = { mejse, tipo, id_generacion ->
                                            pantallaSeleccionada = "Promocionar"
                                            val datos = DatosPublicidadIA(
                                                titulo = "",
                                                descripcion = "",
                                                whatsapp = mejse,
                                                compartir = "",
                                                tipo_redirigido = tipo,
                                                id_generacion_sin_publicar = id_generacion,
                                                datos_generaciones = datos_generaciones_sin_publicaicones(),
                                            )
                                            viewmodel.setear_datos_datos_publicada_IA(datos)
                                        },
                                        navegarcrear_pùblicidad_todas = { titulo, descripcion, wsap, compartir, tipo, id_generacion, datos_generaciones_sin_publicaicones ->
                                            pantallaSeleccionada = "Promocionar"
                                            val datos = DatosPublicidadIA(
                                                titulo = titulo,
                                                descripcion = descripcion,
                                                whatsapp = wsap,
                                                compartir = compartir,
                                                tipo_redirigido = tipo,
                                                id_generacion_sin_publicar = id_generacion,
                                                datos_generaciones = datos_generaciones_sin_publicaicones
                                            )
                                            viewmodel.setear_datos_datos_publicada_IA(datos)
                                        },
                                        navegarcrear_pùblicidad_compartiro = { msje, tipo, id_generacion ->
                                            pantallaSeleccionada = "Promocionar"
                                            val datos = DatosPublicidadIA(
                                                titulo = "",
                                                descripcion = "",
                                                whatsapp = "",
                                                compartir = msje,
                                                tipo_redirigido = tipo,
                                                id_generacion_sin_publicar = id_generacion,
                                                datos_generaciones = datos_generaciones_sin_publicaicones()
                                            )
                                            viewmodel.setear_datos_datos_publicada_IA(datos)
                                        },ocultar_button_bar={
                                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                                        },mostrar_buttom_bar={
                                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                    Scaffold(
                        bottomBar = {
                            AnimatedVisibility(
                                visible = !cargandoState, // si NO está cargando, se muestra
                                enter = slideInVertically(
                                    initialOffsetY = { it }, // entra desde abajo
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { it }, // sale hacia abajo
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            ) {

                                BottomNavigation(
                                    backgroundColor = Color.Black,
                                    elevation = 8.dp
                                ) {
                                    BottomNavigation(
                                        backgroundColor = Color.Black,
                                        elevation = 8.dp
                                    ) {
                                        BottomNavigationItem(
                                            icon = {
                                                Image(
                                                    painter = painterResource(R.drawable.home_seleccionado),
                                                    contentDescription = null,
                                                    colorFilter = if (pantallaSeleccionada == "Inicio")
                                                        ColorFilter.tint(Color.White) else ColorFilter.tint(
                                                        Color.Gray
                                                    ),
                                                    modifier = Modifier
                                                        .size(25.dp)
                                                        .padding(bottom = 4.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = "Inicio",
                                                    fontFamily = FontFamily.Default,
                                                    color = if (pantallaSeleccionada == "Inicio") Color.White else Color.Gray,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis// fuerza que sea normal, no bold
                                                )
                                            },
                                            selected = pantallaSeleccionada == "Inicio",
                                            onClick = {
//                                                pantallaSeleccionada = "Inicio"
                                                intentarCambiarPantalla("Inicio")
                                            },
                                            alwaysShowLabel = true,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        BottomNavigationItem(
                                            icon = {
                                                if (mostrar_bundle_desbloqueo) {
                                                    BadgedBox(
                                                        badge = {
                                                            Text(
                                                                text = "🔥",
                                                                fontSize = 17.sp
                                                            )

                                                        }

                                                    ) {
                                                        Image(
                                                            painter = painterResource(R.drawable.promocionar),
                                                            contentDescription = null,
                                                            colorFilter = if (pantallaSeleccionada == "Promocionar") ColorFilter.tint(
                                                                Color.White
                                                            ) else ColorFilter.tint(Color.Gray),
                                                            modifier = Modifier.size(27.dp)
                                                        )
                                                    }

                                                } else {
                                                    Image(
                                                        painter = painterResource(R.drawable.promocionar),
                                                        contentDescription = null,
                                                        colorFilter = if (pantallaSeleccionada == "Promocionar") ColorFilter.tint(
                                                            Color.White
                                                        ) else ColorFilter.tint(Color.Gray),
                                                        modifier = Modifier.size(27.dp)
                                                    )
                                                }


                                            },
                                            label = {
                                                Text(
                                                    text = "Promocionar",
                                                    fontFamily = FontFamily.Default,
                                                    color = if (pantallaSeleccionada == "Promocionar") Color.White else Color.Gray,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis // fuerza que sea normal, no bold
                                                )
                                            },
                                            selected = pantallaSeleccionada == "Promocionar",
                                            onClick = {
                                                intentarCambiarPantalla("Promocionar")
//                                                pantallaSeleccionada = "Promocionar"
                                                mostrar_bundle_desbloqueo = false
                                            },
                                            alwaysShowLabel = true,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        BottomNavigationItem(
                                            icon = {
                                                if (mostarr_bundel_recientes) {
                                                    BadgedBox(
                                                        badge = {
                                                            Badge(
                                                                backgroundColor = Color.Red,
                                                            ) {
                                                                Text(text = "1", fontSize = 12.sp)
                                                            }
                                                        }
                                                    ) {
                                                        Image(
                                                            painter = painterResource(R.drawable.historial),
                                                            contentDescription = null,
                                                            colorFilter = if (pantallaSeleccionada == "Publicaciones") ColorFilter.tint(
                                                                Color.White
                                                            ) else ColorFilter.tint(Color.Gray),
                                                            modifier = Modifier.size(30.dp)
                                                        )
                                                    }
                                                } else {
                                                    Image(
                                                        painter = painterResource(R.drawable.historial),
                                                        contentDescription = null,
                                                        colorFilter = if (pantallaSeleccionada == "Publicaciones") ColorFilter.tint(
                                                            Color.White
                                                        ) else ColorFilter.tint(Color.Gray),
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }

                                            },
                                            label = {
                                                Text(
                                                    text = "Recientes",
                                                    fontFamily = FontFamily.Default,
                                                    color = if (pantallaSeleccionada == "Publicaciones") Color.White else Color.Gray,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            selected = pantallaSeleccionada == "Publicaciones",
                                            onClick = {
                                                intentarCambiarPantalla("Publicaciones")
//                                                pantallaSeleccionada = "Publicaciones"
                                                viewmodel_pantalla_promocionar.cambiar_Estado_reciente(
                                                    false
                                                )
                                            },
                                            alwaysShowLabel = true,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        BottomNavigationItem(
                                            icon = {
                                                if (mostrar_bundle_recargas) {
                                                    BadgedBox(
                                                        badge = {
                                                            Text(
                                                                text = "\uD83D\uDCB0",
                                                                fontSize = 17.sp
                                                            )
                                                        }

                                                    ) {
                                                        Image(
                                                            painter = painterResource(R.drawable.icon_monedas_3d),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(30.dp)
                                                        )
                                                    }

                                                } else {
                                                    Image(
                                                        painter = painterResource(R.drawable.icon_monedas_3d),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }

                                            },
                                            label = {
                                                Text(
                                                    text = "Recargas",
                                                    fontFamily = FontFamily.Default,
                                                    color = if (pantallaSeleccionada == "Recargas") Color.White else Color.Gray,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            selected = pantallaSeleccionada == "Recargas",
                                            onClick = {
                                                intentarCambiarPantalla("Recargas")
//                                                pantallaSeleccionada = "Recargas"
                                                mostrar_bundle_recargas = false
                                            },
                                            alwaysShowLabel = true,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )


                                    }
                                }
                            }

                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = paddingValues.calculateBottomPadding()
                                )
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Crossfade(targetState = pantallaSeleccionada) { target ->
                                when (target) {
                                    "Inicio" -> {
                                        pantallaGeinz()
                                    }

                                    "Promocionar" -> {
                                        pantalla_promocionar(
                                            viewmodel_pantalla_promocionar = viewmodel_pantalla_promocionar,
                                            viewmodel_socios = viewmodel,
                                            i = item_pantalla_promociones, ocultar_buttom_bar = {
                                            }, mostrar_buttom_bar = {
                                            })
                                    }

                                    "Publicaciones" -> {
                                        PantallaRecientes(
                                            id_tienda,
                                            localidad_tienda,
                                            viewmodel_pantalla_reciente, { cargando ->
                                                cargandoState = cargando
                                            })
                                    }

                                    "Recargas" -> {
                                        pantala_recarga(
                                            localidad_user,
                                            viewmodel_paramo = viewmodel,
                                            nombre_tienda = nombre_tienda,
                                            localida_tienda = localidad_tienda,
                                            id_tienda = id_tienda,
                                            monedas_user = moneda_total_tienda,
                                            cargando = { cargando ->
                                                cargandoState = cargando
                                            })

                                    }
                                }
                            }
                        }
                    }

                }

            }
        }

        if (mostrarDialogoSalir) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSalir = false },
                title = {
                    texto_generico_one_line(
                        "Descartar cambios",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    texto_generico_multilinea(
                        "Tienes campos llenos. ¿Deseas descartarlos?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                },
                confirmButton = {
                    TextButton(onClick = {
                        viewmodel_pantalla_promocionar.descartarCambios()
                        pantallaSeleccionada = pantallaDestino
                        mostrarDialogoSalir = false
                    }) {
                        Text("Sí")
                        viewmodel.limpiar_datos_pasados_notificaciones_con_IA()
//                        viewmodel_pantalla_promocionar.resetear_Estado_notificacion_gnerado_ia()
                        viewmodel.limpiarId()
                        viewmodel.limpiar_datos_pasados_publcada_IA()
//                        viewmodel_pantalla_promocionar.limpiar_resutlados_ia_promo()
//                        viewmodel_pantalla_promocionar.reseteo_compartir()
//                        viewmodel_pantalla_promocionar.reseteo_wshap_promocion()
                        viewmodel_pantalla_promocionar.resetearEstadosNotificacion()
                        viewmodel_pantalla_promocionar.resetearEstadosPublicacion()
                    }
                },

                dismissButton = {
                    TextButton(onClick = { mostrarDialogoSalir = false }) {
                        Text("No")
                    }
                }
            )
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



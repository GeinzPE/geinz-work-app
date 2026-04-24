package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.OpcionPromocionIA
import com.geinzz.geinzwork.data.model.cambiar_datos_pago_contacto
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.datos_generaciones_sin_publicaicones
import com.geinzz.geinzwork.data.model.datos_grafico
import com.geinzz.geinzwork.data.model.datos_para_generacion_dialog_historial_IA
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.datos_tienda_fechas
import com.geinzz.geinzwork.data.model.lista_genereracione
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.servicio_comodidad
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constante_abrir_navegador.openCustomTab
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandible_wrap_socio_atrubitos
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_contacto_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_geinzz
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_geinzz_datos_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_geinzz_horario_atencion
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandibles_wrapp_socio_metodos_pago_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.expandido_wrap_socio_atributos
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.subir_foto_perfil_algolia_normal
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.subir_storage_perfil_img
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.Cartas_expandibles
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_textField_150
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_mostar_leyendas_graficos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialogo_cerrar_seccion_teinda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_centro_de_Ayudas_pra_geinz
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_historial_pago
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.ui_bottom_sheet_generaciones_IA
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.DiaHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerDiasYColor
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.BoxFotosTipos
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.BoxTipo_promociones
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.Box_para_imagen_general_de_Bot_whatsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.agregarImagenParaBot
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios.estadisticas_aplicables
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import com.geinzz.geinzwork.viewModels.viewmodel_generaciones_IA
import com.geinzz.geinzwork.viewModels.viewmodel_recargas
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun pantalla_carga_socios(
    fecha_finalizado_flow: StateFlow<String>,
    id_user: String,
    datos: datos_tienda,
    isConnected: Boolean,
    id_registrado: (String) -> Unit,
    navegarcrear_pùblicidad_todas: (String, String, String, String, String, String?, i: datos_generaciones_sin_publicaicones) -> Unit,
    navegarcrear_pùblicidad_titulo_descripcion: (String, String, String, String?, i: datos_generaciones_sin_publicaicones) -> Unit,
    navegarcrear_pùblicidad_wsap: (String, String, String?) -> Unit,
    navegarcrear_pùblicidad_compartiro: (String, String, String?) -> Unit,
    ocultar_button_bar:()-> Unit,
    mostrar_buttom_bar:()-> Unit
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val viewmodel: viewmodel_eres_socio = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    val viewmodel_recargas: viewmodel_recargas = viewModel()
    val viewmodel_generacones_IA: viewmodel_generaciones_IA = viewModel()
    val labels = listOf("Vistas", "Guardados", "Clics", "Compartidos")
    val labels2 = listOf("Facebook", "Instagram", "TikTok", "Sitio web")
    val labels3 = listOf("Llamada", "Whatsapp", "Rutas")
    val labels4 = listOf(
        "QR review publico",
        "QR review privado",
        "QR creacion de ruta",
        "QR vistas de perfil"
    )
    var fecha_termino by remember { mutableStateOf("") }
    var fotosAmbientales by remember { mutableStateOf<List<String>>(emptyList()) }
    var fotosServicios by remember { mutableStateOf<List<String>>(emptyList()) }
    var fotos_ambiente by remember { mutableStateOf(false) }
    var fotos_producto by remember { mutableStateOf(false) }
    var fotos_promociones by remember { mutableStateOf(false) }
    var fotosPromociones by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var cambiar_estados_bot_whattsapp by remember { mutableStateOf(false) }

    var metodos_pago by remember { mutableStateOf(modelo_pagos_tienda()) }
    var metodo_contact by remember { mutableStateOf(metodo_contacto_tienda()) }
    var lista_servicios_comodidades by remember {
        mutableStateOf<List<servicio_comodidad>>(emptyList())
    }
    val context = LocalContext.current
    var valor_img_completa by remember { mutableStateOf("") }
    var mostrarDialogozoom by remember { mutableStateOf(false) }
    var logoOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var logoActual by rememberSaveable { mutableStateOf<String?>(null) }
    var hayCambiosLogo by remember { mutableStateOf(false) }
    var id_tienda by remember { mutableStateOf("") }
    val _tick by viewModelFiltros.tick.collectAsState()
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val esta_vincualdo by viewmodel.esta_vinculado.collectAsState()
    val fecha_tienda_fin by fecha_finalizado_flow.collectAsState()
    var subiendoImagen by remember { mutableStateOf(false) }
    var imagen_subida_correctamente by remember { mutableStateOf(false) }
    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            viewmodel.verificar_cuenta_vinculada(
                uid_respald_user,
                datos.id_tienda,
                datos.localidad_tienda
            )
        }
    }

    val datos = datos
    id_tienda = datos.id_tienda
    fecha_termino = fecha_tienda_fin
    fotosAmbientales =
        datos.obtener_img_tiendas.lista_ambiernte
    fotosServicios =
        datos.obtener_img_tiendas.lista_productos
    fotosPromociones =
        datos.obtener_img_tiendas.lista_promociones
    metodos_pago = datos.metodos_pago
    metodo_contact = datos.metodo_contacto_tienda
    lista_servicios_comodidades =
        datos.servicios_comodidades

    LaunchedEffect(datos.id_tienda) {
        logoOriginal = datos.obtener_img_tiendas.logo_tienda
        logoActual = datos.obtener_img_tiendas.logo_tienda
        hayCambiosLogo = false
    }
    LaunchedEffect(id_tienda) {
        if (id_tienda != "") {
            viewmodel.carga_img_tipo("ambientales", id_tienda)
            viewmodel.carga_img_tipo("servicios_productos", id_tienda)
        }
    }

    var values by remember { mutableStateOf(listOf<Float>()) }

    var values2 by remember { mutableStateOf(listOf<Float>()) }

    var values3 by remember { mutableStateOf(listOf<Float>()) }

    var values4 by remember { mutableStateOf(listOf<Float>()) }

    var nombre_negocio by remember { mutableStateOf(datos.nombre) }
    var mostar_horario__bool by remember { mutableStateOf(false) }
    var mostrar_datos_teinda by remember { mutableStateOf(false) }
    var cerrar_Seccion_cuenta_tienda by remember { mutableStateOf(false) }
    var mostrar_redes_tienda by remember { mutableStateOf(false) }
    var mostar_socio_atributos by remember { mutableStateOf(false) }
    var mostar_metodos_pago_tienda by remember { mutableStateOf(false) }
    var mostar_atributos_negocios by remember { mutableStateOf(false) }
    var mostar_horario_teinda by remember { mutableStateOf(false) }
    var mostrar_trafico_externo by remember { mutableStateOf(false) }
    var mostrar_qr_externo by remember { mutableStateOf(false) }
    var expandido by remember { mutableStateOf(false) }
    var mostar_interes by remember { mutableStateOf(false) }
    var dialog_mostar_leyendas_graficos by remember { mutableStateOf(false) }
    var titulo_leyenda_dialog by remember { mutableStateOf("") }
    var txt_leyenda by remember { mutableStateOf("") }
    var icono_mostar_leyendas_graficos by remember { mutableStateOf(0) }
    var mostrar_convesion by remember { mutableStateOf(false) }
    val obj_creado_para_descripcion_para_whtsapp by remember { mutableStateOf("") }
    var cambiar_imagen_para_el_bot_whatsapp by remember { mutableStateOf(false) }
    var uri_para_bot_whatsapp: Uri? = null
    val estado_descripcion_generadad_whatsapp by viewmodel_generacones_IA.estado_carga_generaciones_desk_whatsapp.collectAsState()
// Verificamos si el estado actual es "Loading" o "Cargando"
    val estaCargandoIA =
        estado_descripcion_generadad_whatsapp is viewmodel_generaciones_IA.Estado_generacion_IA_whsatp.loading
// Nota: Ajusta ".loading" al nombre exacto que uses en tu sealed class
    var mostrar_btn_guardar_chatbot_IA by remember { mutableStateOf(false) }

    val estado_subido_para_whatsapp_bot by viewmodel.estado_subido_desc_para_bot.collectAsState()

    var descripcion_negocio by remember {
        mutableStateOf(datos.descripcion)
    }

    var descripcion_chat_bot by remember {
        mutableStateOf(
            datos.descripcion_chat_bot_whatsapp ?: ""
        )
    }

    LaunchedEffect(estado_descripcion_generadad_whatsapp) {
        when (estado_descripcion_generadad_whatsapp) {

            is viewmodel_generaciones_IA.Estado_generacion_IA_whsatp.succes -> {
                val data =
                    (estado_descripcion_generadad_whatsapp as viewmodel_generaciones_IA.Estado_generacion_IA_whsatp.succes).txt

                if (!data.isNullOrEmpty()) {
                    descripcion_chat_bot = data
                    mostrar_btn_guardar_chatbot_IA = true
                }
            }

            else -> Unit
        }
    }


    var nombre_negocio_actual by remember {
        mutableStateOf(
            datos.nombre
        )
    }
    var descripcion_actual by remember {
        mutableStateOf(
            datos.descripcion
        )
    }
    val (dias, color) = obtenerDiasYColor(fecha_termino)
    val datos_fechas = datos_tienda_fechas(
        datos.id_tienda,
        datos.fecha_ingreso,
        fecha_tienda_fin,
        dias.toString(),
        color,
        datos.saldo_disponible_tienda.toString() ?: "0",
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            logoActual = it.toString()   // ✅ preview inmediata
            hayCambiosLogo = true        // ✅ muestra botones
        }
    }
    var listaPropietarios by remember { mutableStateOf(emptyList<String>()) }
    val localidad_tienda_select_ by data_store_localidad.get_localidad_tienda_socio(context)
        .collectAsState(initial = "")
    var horarioMap by remember { mutableStateOf(HorarioAtencion_box()) }
    horarioMap = datos.horario_tiendaMap
    val estaVinculado = listaPropietarios.contains(uid_respald_user)
    val idSocio by viewmodel.idSocio.collectAsState()
    val scope = rememberCoroutineScope()
    var guardandoLogo by remember { mutableStateOf(false) }

    var mostra_bottom_sheet_historial by remember { mutableStateOf(false) }

    var mostarr_botom_sheet_legal_ayuda_geinz by remember { mutableStateOf(false) }
    var mostrar_webview_terminos_condiciones by remember { mutableStateOf(false) }
    var mostrar_webview_politicas_devoluciones by remember { mutableStateOf(false) }
    var mostrar_webview_libro_recalmaciones by remember { mutableStateOf(false) }
    var mostra_bottom_sheet_historial_de_gen_IA by remember { mutableStateOf(false) }

    LaunchedEffect (mostrar_webview_politicas_devoluciones) {
        Log.d("cambiamos_btn_devolcuinos","$mostrar_webview_politicas_devoluciones")
    }

    LaunchedEffect (mostra_bottom_sheet_historial_de_gen_IA) {
        Log.d("cambiamos_btn_terminos_condiciones","$mostra_bottom_sheet_historial_de_gen_IA")
    }
    val elTextoCambio by remember(descripcion_chat_bot) {
        derivedStateOf {
            descripcion_chat_bot != (datos.descripcion_chat_bot_whatsapp ?: "")
        }
    }
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
            datos.review_qr.toFloat(),
            datos.review_c_qr.toFloat(),
            datos.crear_ruta_qr.toFloat(),
            datos.perfil_qr.toFloat(),
        )
    }
    LaunchedEffect(id_tienda, horarioMap) {
        viewModelFiltros.calcularHorarioParaTienda(id_tienda, horarioMap)
    }
    LaunchedEffect(estado_subido_para_whatsapp_bot) {
        if (estado_subido_para_whatsapp_bot) {

            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Se actualizó la descripción correctamente",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewmodel.resetear_valor_estado_whatsapp_subido_y_gemini()
            viewmodel_generacones_IA.resetear_valor_generacion_desk_whatsapp()

            println("Saliste de la pantalla: Estado reseteado a false")
        }
    }
    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item {
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
                                cerrar_Seccion_cuenta_tienda =
                                    true
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
                        verticalArrangement = Arrangement.spacedBy(
                            10.dp
                        )
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
                                model = ImageRequest.Builder(
                                    context
                                )
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
                                        Color.Black.copy(
                                            alpha = 0.6f
                                        ),
                                        CircleShape
                                    )
                                    .clickable {
                                        valor_img_completa =
                                            logoActual ?: ""
                                        mostrarDialogozoom =
                                            true
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
                                            Color.Black.copy(
                                                alpha = 0.6f
                                            ),
                                            CircleShape
                                        )
                                        .clickable {
                                            if (hayCambiosLogo) {
                                                logoActual =
                                                    logoOriginal
                                                hayCambiosLogo =
                                                    false
                                            } else {

                                                launcher.launch(
                                                    "image/*"
                                                )
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
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Color(
                                                0x99000000
                                            )
                                        )
                                )
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
                                    modifier = Modifier.weight(
                                        1f
                                    ),
                                    enabled = !guardandoLogo && logoActual != null && logoActual != logoOriginal,
                                    onClick = {
                                        guardandoLogo = true
                                        subir_storage_perfil_img(
                                            context = context,
                                            idTienda = id_tienda,
                                            valor = logoActual!!,
                                            onSuccess = { urlFinal ->
                                                scope.launch {
                                                    val subido =
                                                        subir_foto_perfil_algolia_normal(
                                                            id_tienda,
                                                            urlFinal
                                                        )

                                                    guardandoLogo =
                                                        false

                                                    if (subido) {
                                                        logoOriginal =
                                                            urlFinal
                                                        logoActual =
                                                            urlFinal
                                                        hayCambiosLogo =
                                                            false
                                                    }
                                                }
                                            },
                                            onError = {
                                                guardandoLogo =
                                                    false
                                            }
                                        )
                                    }
                                ) {
                                    if (guardandoLogo) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(
                                                18.dp
                                            ),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        texto_generico_one_line(
                                            "Guardar cambios "
                                        )
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
                                exit = fadeOut(),
                                modifier = Modifier.padding(
                                    horizontal = 10.dp
                                )
                            ) {
                                Column {

                                    custom_texFiel(
                                        value = nombre_negocio,
                                        onValueChange = {
                                            nombre_negocio =
                                                it
                                        },
                                        labelText = "Nombre del negocio",
                                        placeholderText = "Nombre del negocio"
                                    )

                                    AnimatedVisibility(
                                        nombre_negocio != nombre_negocio_actual
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(
                                                    CircleShape
                                                )
                                                .background(
                                                    MaterialTheme.colorScheme.primary
                                                )
                                                .padding(
                                                    vertical = 10.dp
                                                )
                                                .clickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ) {
                                                    viewmodel.cambiar_nombre_descripcion(
                                                        datos.localidad_tienda,
                                                        datos.id_tienda,
                                                        "nombre_tienda",
                                                        nombre_negocio
                                                    )
                                                    nombre_negocio_actual =
                                                        nombre_negocio
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
                                    modifier = Modifier
                                        .padding(
                                            start = 10.dp,
                                            end = 10.dp,
                                            bottom = 20.dp
                                        )
                                        .animateContentSize(
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
                                            onValueChange = {
                                                descripcion_negocio =
                                                    it
                                            },
                                            labelText = "Descripción del negocio",
                                            placeholderText = "Descripción del negocio"
                                        )



                                        AnimatedVisibility(
                                            descripcion_negocio != descripcion_actual
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(
                                                        CircleShape
                                                    )
                                                    .background(
                                                        MaterialTheme.colorScheme.primary
                                                    )
                                                    .padding(
                                                        vertical = 10.dp
                                                    )
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() }
                                                    ) {
                                                        viewmodel.cambiar_nombre_descripcion(
                                                            datos.localidad_tienda,
                                                            datos.id_tienda,
                                                            "descripcion",
                                                            descripcion_negocio
                                                        )
                                                        descripcion_actual =
                                                            descripcion_negocio
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


                                        spacer_vertical(20.dp)

                                        Column(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        10.dp
                                                    )
                                                )
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                                    .clickable {
                                                        cambiar_estados_bot_whattsapp =
                                                            !cambiar_estados_bot_whattsapp
                                                    }
                                                    .padding(
                                                        horizontal = 16.dp
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    texto_generico_one_line(
                                                        "Asistente de whatsApp"
                                                    )
                                                    Image(
                                                        painter = painterResource(R.drawable.whatsapp_icon),
                                                        modifier = Modifier.size(25.dp),
                                                        contentDescription = null
                                                    )
                                                }
                                            }

                                            AnimatedVisibility(
                                                cambiar_estados_bot_whattsapp,
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                                ) {
                                                    texto_generico_multilinea(
                                                        "Optimiza tu perfil para la IA: Mejora la información de tu negocio para que nuestro asistente de WhatsApp te recomiende con prioridad y atraiga más clientes.",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    custom_textField_150(
                                                        rounder = 10,
                                                        value = descripcion_chat_bot,
                                                        onValueChange = {
                                                            descripcion_chat_bot = it
                                                        },
                                                        labelText = "Descripción SEO para WhatsApp",
                                                        placeholderText = "Descripción SEO para WhatsApp"
                                                    )
                                                    if ((mostrar_btn_guardar_chatbot_IA || elTextoCambio) && !estado_subido_para_whatsapp_bot) {
                                                        Button(
                                                            onClick = {
                                                                viewmodel.guadardar_descripcion_whattsapp_bot(
                                                                    id_tienda,
                                                                    datos.localidad_tienda,
                                                                    descripcion_chat_bot
                                                                )
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = CircleShape
                                                        ) {
                                                            texto_generico_one_line(
                                                                "Guardar cambios",
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                        spacer_vertical(10.dp)
                                                    }
                                                    boton_generador_por_IA(
                                                        cargando = estaCargandoIA,
                                                        onclick = {
                                                            val data_para_ia =
                                                                viewmodel.prepararInputParaIA(
                                                                    datos.subcategorias_tienda,
                                                                    metodos_pago,
                                                                    datos.servicios_comodidades
                                                                )
                                                            Log.d("data_para_ia", data_para_ia)
                                                            viewmodel_generacones_IA.obtener_descripcion_generada_con_datos(
                                                                data_para_ia,datos.localidad_tienda,datos.nombre,datos.id_tienda,"30",datos.saldo_disponible_tienda.toInt()
                                                            )
                                                        },
                                                        texto_button = "generar con IA",
                                                        cantidad_monedas = "30"
                                                    )

                                                    spacer_vertical(15.dp)
                                                    texto_generico_multilinea(
                                                        "Agrega una imagen para que el asistente de Geinz pueda mostrar tu negocio de forma más atractiva.",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    spacer_vertical(10.dp)

                                                    Box_para_imagen_general_de_Bot_whatsapp(
                                                        imagen_subida_correctamente,
                                                        subiendoImagen,
                                                        imagenInicial = datos.obtener_img_tiendas.logo_whatsapp_bot,
                                                        onImagenChange = { uri ->
                                                            if (uri != null) {
                                                                cambiar_imagen_para_el_bot_whatsapp =
                                                                    true
                                                                uri_para_bot_whatsapp=uri
                                                            }
                                                        },
                                                        usuario_borro_los_cambios = {
                                                            cambiar_imagen_para_el_bot_whatsapp =
                                                                false
                                                        })

                                                    if (cambiar_imagen_para_el_bot_whatsapp) {
                                                        Button(
                                                            onClick = {
                                                                subiendoImagen = true
                                                                agregarImagenParaBot(
                                                                    datos.localidad_tienda,
                                                                    datos.id_tienda,
                                                                    uri_para_bot_whatsapp,
                                                                    context
                                                                ) {
                                                                    subiendoImagen = false
                                                                    cambiar_imagen_para_el_bot_whatsapp = false

                                                                    imagen_subida_correctamente=true
                                                                    scope.launch {
                                                                        snackbarHostState.showSnackbar(
                                                                            message = "Imagen subida correctamente",
                                                                            duration = SnackbarDuration.Short
                                                                        )
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = CircleShape,
                                                            enabled = !subiendoImagen // 🔥 desactiva mientras sube
                                                        ) {

                                                            if (subiendoImagen) {
                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    texto_generico_one_line(
                                                                        "Subiendo...",
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                }

                                                            } else {
                                                                texto_generico_one_line(
                                                                    "Guardar cambios",
                                                                    style = MaterialTheme.typography.bodyMedium
                                                                )
                                                            }
                                                        }
                                                    }


                                                }
                                            }
                                        }

                                    }

                                    spacer_vertical(10.dp)
                                    Column(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    10.dp
                                                )
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
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
                                                .padding(
                                                    horizontal = 16.dp
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            texto_generico_one_line(
                                                "Fotos de ambiente \uD83C\uDF06"
                                            )
                                        }

                                        AnimatedVisibility(
                                            fotos_ambiente
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        8.dp
                                                    )
                                            ) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    BoxFotosTipos(
                                                        id_user,
                                                        "ambientales",
                                                        id_tienda,
                                                        fotosAmbientales,
                                                        6
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    spacer_vertical(10.dp)

                                    Column(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    10.dp
                                                )
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
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
                                                .padding(
                                                    horizontal = 16.dp
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            texto_generico_one_line(
                                                "Fotos de productos o servicios 👣"
                                            )
                                        }

                                        AnimatedVisibility(
                                            fotos_producto
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        8.dp
                                                    )
                                            ) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    BoxFotosTipos(
                                                        id_user,
                                                        "servicios_productos",
                                                        id_tienda,
                                                        fotosServicios,
                                                        6
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    spacer_vertical(10.dp)

                                    Column(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    10.dp
                                                )
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
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
                                                .padding(
                                                    horizontal = 16.dp
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            texto_generico_one_line(
                                                "Promociones recientes \u200B\u200B❤\uFE0F\u200D\uD83D\uDD25\u200B"
                                            )
                                        }

                                        AnimatedVisibility(
                                            fotos_promociones
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        8.dp
                                                    )
                                            ) {
                                                BoxTipo_promociones(
                                                    id_user,
                                                    "promociones",
                                                    id_tienda,
                                                    fotosPromociones,
                                                    3
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
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
                                nombre_negocio, viewmodel_recargas = viewmodel_recargas,
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
                                id_user,
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
                                datos.id_tienda,
                                datos.localidad_tienda,
                                lista_servicios_comodidades,
                                viewModelFiltros = viewmodel,
                                expandido = mostar_atributos_negocios,
                                {
                                    mostar_atributos_negocios =
                                        !mostar_atributos_negocios
                                },
                                {
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
                                onClickExpand = {
                                    mostar_socio_atributos =
                                        !mostar_socio_atributos
                                },
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
                    spacer_vertical(10.dp)
                    AnimatedContent(
                        targetState = expandido,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(10.dp)
                            )
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) { estado ->
                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        expandido =
                                            !expandido
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Estadisticas completas \uD83D\uDCC8",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(
                                            vertical = 10.dp
                                        ),
                                    )
                                }
                            }

                        } else {
                            Column() {

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Estadisticas completas \uD83D\uDCC8",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .padding(
                                                vertical = 10.dp
                                            )
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }) {
                                                expandido =
                                                    !expandido
                                            },
                                    )

                                }
                                texto_generico_multilinea(
                                    "Analiza las estadísticas de tu perfil, mejora tus promociones y conecta mejor con tus clientes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(
                                        vertical = 5.dp,
                                        horizontal = 10.dp
                                    )
                                )

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
                                AnimatedVisibility(
                                    lsita_datos1.any { it.enable }) {
                                    Cartas_expandibles(

                                        modifier = Modifier.padding(
                                            vertical = 10.dp,
                                            horizontal = 5.dp
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
                                                    mostar_interes =
                                                        !mostar_interes
                                                }
                                            )
                                        }
                                        val lista_colores1 =
                                            listOf(
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
                                            )
                                        estadisticas_aplicables(
                                            mostrar_qr_externo = mostar_interes,
                                            List_float = values,
                                            ListString = labels,
                                            colores_lista = lista_colores1,
                                            contenido_clikeado = { select ->
                                                when (select) {
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
                                            })

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
                                AnimatedVisibility(
                                    lsita_datos2.any { it.enable }) {
                                    Cartas_expandibles(

                                        modifier = Modifier.padding(
                                            vertical = 10.dp,
                                            horizontal = 5.dp
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
                                        val lista_colores2 =
                                            listOf(
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
                                            )
                                        estadisticas_aplicables(
                                            mostrar_qr_externo = mostrar_convesion,
                                            List_float = values2,
                                            ListString = labels2,
                                            colores_lista = lista_colores2,
                                            contenido_clikeado = { select ->
                                                when (select) {
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
                                        )
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
                                AnimatedVisibility(
                                    lsita_datos3.any { it.enable }) {
                                    Cartas_expandibles(

                                        modifier = Modifier.padding(
                                            vertical = 10.dp,
                                            horizontal = 5.dp
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
                                        val lista_colores3 =
                                            listOf(
                                                Color(
                                                    0xFF18C5A4
                                                ),
                                                Color(
                                                    0xFF25D366
                                                ),
                                                Color(
                                                    0xFF6A0DAD
                                                )
                                            )
                                        estadisticas_aplicables(
                                            mostrar_qr_externo = mostrar_trafico_externo,
                                            List_float = values3,
                                            ListString = labels3,
                                            colores_lista = lista_colores3,
                                            contenido_clikeado = { select ->
                                                when (select) {
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
                                        )
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
                                        cantidad = datos.crear_ruta_qr.toString()
                                    ),
                                    datos_grafico(
                                        enable = datos.perfil_qr != 0,
                                        img_ = R.drawable.perfil_qr,
                                        label = "QR vista de perfil",
                                        cantidad = datos.perfil_qr.toString()
                                    )
                                )
                                AnimatedVisibility(
                                    lsita_datos4.any { it.enable }) {
                                    Cartas_expandibles(
                                        modifier = Modifier.padding(
                                            vertical = 10.dp,
                                            horizontal = 5.dp
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
                                        val lista_colores4 =
                                            listOf(
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
                                            )
                                        estadisticas_aplicables(
                                            mostrar_qr_externo = mostrar_qr_externo,
                                            List_float = values4,
                                            ListString = labels4,
                                            colores_lista = lista_colores4,
                                            contenido_clikeado = { select ->
                                                when (select) {
                                                    "QR review privado" -> {
                                                        dialog_mostar_leyendas_graficos =
                                                            true
                                                        titulo_leyenda_dialog =
                                                            "QR review privado"
                                                        txt_leyenda =
                                                            "Este valor indica cuántos usuarios escanearon el QR de reseñas que se encuentra dentro de la aplicación, específicamente en el perfil de la tienda. Representa la interacción de usuarios que ya están navegando en la plataforma y deciden dejar o consultar una reseña."
                                                        icono_mostar_leyendas_graficos =
                                                            R.drawable.review_c_qr
                                                    }

                                                    "QR review publico" -> {
                                                        dialog_mostar_leyendas_graficos =
                                                            true
                                                        titulo_leyenda_dialog =
                                                            "QR review publico"
                                                        txt_leyenda =
                                                            "Este valor muestra cuántas personas escanearon el QR de reseñas que el negocio tiene de forma física en su local. Cada escaneo representa a un usuario externo que interactúa directamente con el negocio para dejar o ver opiniones."
                                                        icono_mostar_leyendas_graficos =
                                                            R.drawable.review_qr
                                                    }

                                                    "QR creacion de ruta" -> {
                                                        dialog_mostar_leyendas_graficos =
                                                            true
                                                        titulo_leyenda_dialog =
                                                            "QR creacion de ruta"
                                                        txt_leyenda =
                                                            "Este valor indica cuántos usuarios escanearon el QR de creación de rutas. Al hacerlo, se abre automáticamente el mapa con la ruta hacia el local, lo que demuestra una clara intención de visitar físicamente el negocio."
                                                        icono_mostar_leyendas_graficos =
                                                            R.drawable.crear_ruta_qr
                                                    }

                                                    "QR vistas de perfil" -> {
                                                        dialog_mostar_leyendas_graficos =
                                                            true
                                                        titulo_leyenda_dialog =
                                                            "QR vistas de perfil"
                                                        txt_leyenda =
                                                            "Este valor muestra cuántos usuarios escanearon el QR del perfil del negocio. Al escanearlo, el usuario ingresa directamente al perfil de la tienda dentro de la aplicación para ver su información, servicios y contenido."
                                                        icono_mostar_leyendas_graficos =
                                                            R.drawable.perfil_qr
                                                    }
                                                }
                                            }
                                        )


                                    }
                                }

                                spacer_vertical(10.dp)
                            }
                        }
                    }
                    spacer_vertical(10.dp)

                    Button(
                        onClick = {
                            mostra_bottom_sheet_historial = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        texto_generico_one_line(
                            "\uD83D\uDCCA Historial de recarga y compra",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    spacer_vertical(10.dp)
                    Button(
                        onClick = {
                            mostarr_botom_sheet_legal_ayuda_geinz = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        texto_generico_one_line(
                            "Centro Legal y Ayuda",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }


//                    spacer_vertical(10.dp)
//                       Button(
//                        onClick = {
//                            mostrar_webview_terminos_condiciones =true
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        shape = CircleShape,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        )
//                    ) {
//                        texto_generico_one_line(
//                            "\uD83D\uDCDC\uD83D\uDC99 Términos y condiciones",
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(vertical = 6.dp)
//                        )
//                    }
//                    spacer_vertical(10.dp)
//                    Button(
//                        onClick = {
//                            mostrar_webview_politicas_devoluciones=true
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        shape = CircleShape,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        )
//                    ) {
//                        texto_generico_one_line(
//                            "\uD83D\uDCC4 Politica de cambios y devoluciones",
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(vertical = 6.dp)
//                        )
//                    }
//
//                    spacer_vertical(10.dp)
//                    Button(
//                        onClick = {
//                            mostrar_webview_libro_recalmaciones=true
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        shape = CircleShape,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        )
//                    ) {
//                        texto_generico_one_line(
//                            "\uD83D\uDCD8 Libro de Reclamaciones",
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(vertical = 6.dp)
//                        )
//                    }
                    spacer_vertical(20.dp)



//                    spacer_vertical(10.dp)
//
//                    Button(
//                        onClick = {
//                            mostra_bottom_sheet_historial_de_gen_IA = true
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        shape = CircleShape,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        )
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(5.dp)
//                        ) {
//
//                            texto_generico_one_line(
//                                "Historial de generaciones con IA",
//                                style = MaterialTheme.typography.bodyMedium,
//                                modifier = Modifier.padding(vertical = 6.dp)
//                            )
//                            Icon(
//                                imageVector = Icons.Default.AutoAwesome,
//                                contentDescription = "IA", tint = Color.White
//                            )
//                        }
//                    }

                    spacer_vertical(20.dp)

                    if (!esta_vincualdo && isConnected) {
                        spacer_vertical(20.dp)
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
                                    .background(
                                        MaterialTheme.colorScheme.primary
                                    )
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
                    spacer_vertical(30.dp)
                }

            }
        }

        if(mostarr_botom_sheet_legal_ayuda_geinz){
            bottom_sheet_centro_de_Ayudas_pra_geinz({
                mostarr_botom_sheet_legal_ayuda_geinz=false
            },{url->
                openCustomTab(context, url)
            })
        }

//        when {
//            mostrar_webview_terminos_condiciones -> {
//                openCustomTab(context, "https://geinzwork.web.app/terminos_y_condiciones.html")
//                mostrar_webview_terminos_condiciones = false
//            }
//            mostrar_webview_politicas_devoluciones -> {
//                openCustomTab(context, "https://geinzwork.web.app/politicas_devoluciones.html")
//                mostrar_webview_politicas_devoluciones=false
//            }
//            mostrar_webview_libro_recalmaciones->{
//                openCustomTab(context,"https://geinzwork.firebaseapp.com/libro_reclamaciones.html")
//                mostrar_webview_libro_recalmaciones=false
//            }
//        }


        if (mostra_bottom_sheet_historial) {
            bottom_sheet_historial_pago(
                datos.id_tienda,
                datos.nombre,
                datos.localidad_tienda,
                datos.saldo_disponible_tienda.toString(),
                { mostra_bottom_sheet_historial = false })
        }

        if (mostra_bottom_sheet_historial_de_gen_IA) {
            val datos_tienda_params = datos_para_generacion_dialog_historial_IA(
                nombre_tienda = datos.nombre,
                monedas_tienda = datos.saldo_disponible_tienda.toInt(),
                localidad_tienda = datos.localidad_tienda,
                id_tienda = datos.id_tienda
            )
            ui_bottom_sheet_generaciones_IA(
                datos_tienda_params,
                { mostra_bottom_sheet_historial_de_gen_IA = false },
                datos.nombre,
                usar_todas = { titulo, descripcion, wsap, compartir, tipo, id_generacion, datos_generaciones_sin_publicaicones ->
                    Log.d("tipo_pbntenido1", "$tipo")
                    if (tipo == "generacion_publicacion_sin_pulicar" || tipo == "notificacion_sin_publicar") {
                        navegarcrear_pùblicidad_todas(
                            titulo,
                            descripcion,
                            wsap,
                            compartir,
                            tipo,
                            id_generacion,
                            datos_generaciones_sin_publicaicones
                        )
                    } else {
                        navegarcrear_pùblicidad_todas(
                            titulo,
                            descripcion,
                            wsap,
                            compartir,
                            tipo,
                            null,
                            datos_generaciones_sin_publicaicones()
                        )
                    }
                },
                usar_titulo_descripcion = { titulo, descrpcion, tipo, id_generacion, datos_generaciones_sin_publicaicones ->
                    Log.d("tipo_pbntenido2", "$tipo")
                    if (tipo == "generacion_publicacion_sin_pulicar" || tipo == "notificacion_sin_publicar") {
                        navegarcrear_pùblicidad_titulo_descripcion(
                            titulo,
                            descrpcion,
                            tipo,
                            id_generacion,
                            datos_generaciones_sin_publicaicones
                        )
                    } else {
                        navegarcrear_pùblicidad_titulo_descripcion(
                            titulo,
                            descrpcion,
                            tipo,
                            null,
                            datos_generaciones_sin_publicaicones()
                        )
                    }
                },
                usar_wsap = { msje, tipo, id_generacion ->
                    Log.d("tipo_pbntenido3", "$tipo")
                    if (tipo == "generacion_publicacion_sin_pulicar" || tipo == "notificacion_sin_publicar") {
                        navegarcrear_pùblicidad_wsap(msje, tipo, id_generacion)

                    } else {
                        navegarcrear_pùblicidad_wsap(msje, tipo, null)

                    }
                },
                usar_compartir = { msje, tipo, id_generacion ->
                    Log.d("tipo_pbntenido4", "$tipo")
                    if (tipo == "generacion_publicacion_sin_pulicar" || tipo == "notificacion_sin_publicar") {
                        navegarcrear_pùblicidad_compartiro(msje, tipo, id_generacion)

                    } else {

                        navegarcrear_pùblicidad_compartiro(msje, tipo, null)
                    }
                }
            )
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
        if (dialog_mostar_leyendas_graficos) {
            dialog_mostar_leyendas_graficos(
                icono_mostar_leyendas_graficos,
                titulo_leyenda_dialog,
                txt_leyenda,
                { dialog_mostar_leyendas_graficos = false })
        }
        if (cerrar_Seccion_cuenta_tienda) {
            dialogo_cerrar_seccion_teinda(
                txt = "¿Estás seguro de que deseas cerrar sesión de tu cuenta de tienda? Al hacerlo, tu dispositivo se desvinculará completamente de la tienda, incluyendo la eliminación del ID de tienda asociado a tu cuenta. Tendrás que volver a iniciar sesión y vincular nuevamente tu dispositivo para acceder otra vez.",
                ondimis = {
                    cerrar_Seccion_cuenta_tienda = !cerrar_Seccion_cuenta_tienda
                },
                cerrar_seccion = {
                    scope.launch {
                        id_registrado("")
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


        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
                .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
        )
    }


}


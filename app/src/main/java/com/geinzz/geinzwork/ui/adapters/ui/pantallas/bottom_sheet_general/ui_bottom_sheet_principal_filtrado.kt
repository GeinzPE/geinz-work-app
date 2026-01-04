package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.ServicioComodidadUI
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.item_metodos_pago
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data.model.obtener_img_tiendas
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openFacebook
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openInstagram
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openTiktok
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.openWebLink
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.Cartas_expandibles
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoExpandibleEnLinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.expandibles_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.generarQrBitmapAltaCalidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.generar_qr_ubi_tinda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle_sin_scroll
import com.geinzz.geinzwork.ui.adapters.ui.CollageGoogleMapsStyle_sin_scroll_promociones
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_administrar_perfil
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_eliminar_favoritos
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_qr_pago_tienda
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
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.convertir_timesTAmp_fecha
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.ZoomIconButton
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatDistancia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isGPSEnabled
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.llamar
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerUbicacionEnTiempoReal
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left

import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda.retornar_id_Tienda_lugar
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.Unit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun bottom_sheet_tiendas_filtradas(
    verificar_intener: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    tiendas_filtradas: modelo_tienda,
    visible: Boolean,
    iconos_cosas_clikeables: Boolean = true,
    onClose: () -> Unit,
) {
    Log.d("datoasadasda", tiendas_filtradas.toString())
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val repo_socio = repo_eres_socio()
    var expandir_descripcion by rememberSaveable { mutableStateOf(false) }
    var expander_caracterisiticas by rememberSaveable { mutableStateOf(false) }
    var expander_contacto by rememberSaveable { mutableStateOf(false) }
    var expander_horario by rememberSaveable { mutableStateOf(false) }
    var expander_comidades_aforo by remember { mutableStateOf(false) }
    var expander_qr_tienda by rememberSaveable { mutableStateOf(false) }
    var expander_metodos_pagos by rememberSaveable { mutableStateOf(false) }
    val direccion = tiendas_filtradas.ubicacion["dirección"]?.toString() ?: ""
    val referencia = tiendas_filtradas.ubicacion["referencia"]?.toString() ?: ""
    val longitud = (tiendas_filtradas.ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
    val latitud = (tiendas_filtradas.ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0

    val horarios by viewModelFiltros
        .horariosTiendas_real_completo
        .collectAsState(initial = HorarioAtencion_box())

    val color_Estado_flow by viewModelFiltros.color_estado_tienda_flow.collectAsState()
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val verificarfavorito by viewModelFiltros.existe_favorito.collectAsState()
    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""
    var cargando by remember { mutableStateOf(true) }

    var guardar_icon by remember { mutableStateOf(false) }
    var mostar_eliminar_guardado_dialog by remember { mutableStateOf(false) }

    var icono_select_fv by remember { mutableStateOf(false) }

    var triggerAnimacion by remember { mutableStateOf(false) }
    LaunchedEffect(verificarfavorito) {
        guardar_icon = verificarfavorito
    }
    var ultimoProcesado by rememberSaveable { mutableStateOf("") }
    var inicioPerfil by rememberSaveable { mutableStateOf(0L) }
    Log.d("pasamoceorndeasd", "$latitud,$longitud ")
    var mostar_dialog_verificar_perfil by remember { mutableStateOf(false) }
    LaunchedEffect(tiendas_filtradas.id_tienda) {

        Log.d(
            "horarideirecotblotshee",
            "${tiendas_filtradas.id_tienda} ${tiendas_filtradas.localidad}"
        )
        val nuevoId = tiendas_filtradas.id_tienda

//        viewModelFiltros.cast_horario_atencion_horario_tienda(tiendas_filtradas.horario_atencion)
        viewModelFiltros.cast_horario_atencion_horario_tienda_box(tiendas_filtradas.horario_tienda_box)
        viewModelFiltros.verificar_existe_favorito(id_user, nuevoId)

    }

    var ultimoIdProcesado by rememberSaveable { mutableStateOf("") }

// Este efecto NO debe hacer nada con el ID
    LaunchedEffect(visible, tiendas_filtradas) {
        if (visible) {
            cargando = true
            val start = System.currentTimeMillis()

            while (cargando) {
                val datosListos =
                    tiendas_filtradas.id_tienda.isNotBlank() ||
                            tiendas_filtradas.ubicacion.isNotEmpty()

                val paso = System.currentTimeMillis() - start

                if (paso >= 1500 && (datosListos || paso >= 2500)) {
                    cargando = false
                    break
                }
                delay(80)
            }
        } else {
            cargando = false
        }
    }

// 👇 AHORA SÍ: solo cuando ya cargó + visible
    LaunchedEffect(visible, cargando) {
        if (!visible || cargando) return@LaunchedEffect

        val nuevoId = tiendas_filtradas.id_tienda
        if (nuevoId.isBlank()) return@LaunchedEffect

        if (nuevoId != ultimoIdProcesado) {
            viewModelFiltros.repo_filtrado.escucharHorarioCompletoDeTiendaUnica(
                idTiendaBuscada = nuevoId,
                localidad = tiendas_filtradas.localidad ?: "barranca"
            )
            repo_socio.agregar_contador(
                "clic",
                nuevoId,
                tiendas_filtradas.localidad ?: "barranca"
            )
            Log.d("CLIC", "Clic REAL: $nuevoId")
        }

        ultimoIdProcesado = nuevoId
        inicioPerfil = System.currentTimeMillis()

        delay(6000)

        if (ultimoIdProcesado == nuevoId) {

            repo_socio.agregar_contador(
                "vistas",
                nuevoId,
                tiendas_filtradas.localidad ?: "barranca"
            )
            Log.d("VISTA", "Vista REAL: $nuevoId")


        }
    }


    var distanciaUsuarioTienda by remember { mutableStateOf<Float?>(null) }
    var gpsJobId by remember { mutableStateOf(0) }   // 🔥 ID para ignorar callbacks viejos

    val gps_enable = isGPSEnabled(context)

    LaunchedEffect(gps_enable, latitud, longitud) {

        // 1. Nueva operación → incrementa ID
        val currentId = ++gpsJobId

        // 2. Limpia distancia inmediatamente
        distanciaUsuarioTienda = null

        if (gps_enable) {
            try {
                obtenerUbicacionEnTiempoReal(
                    true,
                    context,
                    { lat_user, lng_user ->

                        // 3. IGNORA si este callback pertenece a un cálculo viejo
                        if (currentId != gpsJobId) return@obtenerUbicacionEnTiempoReal

                        val resultados = FloatArray(1)
                        Location.distanceBetween(
                            lat_user,
                            lng_user,
                            latitud,
                            longitud,
                            resultados
                        )

                        distanciaUsuarioTienda = resultados[0]
                    },
                    {
                        if (currentId == gpsJobId) {
                            distanciaUsuarioTienda = null
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("UBICACION_ERROR", "Error al obtener ubicación: ${e.message}")
            }
        }
    }
var qr_generado_tienda by remember { mutableStateOf("") }
    LaunchedEffect(tiendas_filtradas.id_tienda, latitud, longitud) {
        qr_generado_tienda=retornar_id_Tienda_lugar(tiendas_filtradas.id_tienda, latitud, longitud)

    }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qr_generado_tienda) {
        if (qr_generado_tienda.isBlank()) return@LaunchedEffect
        if (qrBitmap != null) return@LaunchedEffect

        qrBitmap = withContext(Dispatchers.Default) {
            generarQrBitmapAltaCalidad(context, qr_generado_tienda)
        }
    }

//    val generar_qr_tienda_id = remember(tiendas_filtradas.id_tienda, latitud, longitud) {
//    }



    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
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
                AnimatedVisibility(visible = true) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)

                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 10.dp,
                                        bottom = 12.dp,
                                        start = 10.dp,
                                        end = 10.dp
                                    ),
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
                                tiendas_filtradas.metodo_contacto_tienda.whatsapp.numero,
                                distanciaUsuarioTienda = distanciaUsuarioTienda,
                                nombreTienda = tiendas_filtradas.nombre_tienda,
                                logo_tienda_img = tiendas_filtradas.img_perfil,
                                iconos_cosas_clikeables = iconos_cosas_clikeables,
                                localidad = tiendas_filtradas.localidad ?: "barranca",
                                id_tienda = tiendas_filtradas.id_tienda,
                                verificar_intener = verificar_intener,
                                triggerAnimacion = triggerAnimacion,
                                guardar_icon = guardar_icon,
                                viewModel_filtado_tiendas = viewModelFiltros,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                estadoColor = color_Estado_flow,
                                direccion = direccion,
                                referencia = referencia,
                                categoritienda = tiendas_filtradas.categoria_tienda,
                                nombre_tienda = tiendas_filtradas.nombre_tienda,
                                latitud = latitud,
                                longitud = longitud,
                                img_tienda_perfil = tiendas_filtradas.img_perfil,
                                lista_img = tiendas_filtradas.lista_img_tienda,
                                lista_tags = tiendas_filtradas.subcategoria,
                                guaradar_select = { i ->
                                    icono_select_fv = i
                                    val datos_guardar = favoritos_guardados(
                                        img_tienda = tiendas_filtradas.img_perfil,
                                        id_tienda_lugar = tiendas_filtradas.id_tienda,
                                        nombre_lugar_tienda = tiendas_filtradas.nombre_tienda,
                                        categoria = tiendas_filtradas.categoria_tienda,
                                        timesLap = "",
                                        lat = latitud,
                                        lng = longitud,
                                        localida_tienda = tiendas_filtradas.localidad ?: "",
                                        horario_tienda_box = tiendas_filtradas.horario_tienda_box
                                    )
                                    if (id_user.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Regístrate para guardar favoritos ❤️",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@cabezero_tiendas
                                    }

                                    if (i) {
                                        viewModelFiltros.guardar_tienda_favorita(
                                            id_user,
                                            datos_guardar
                                        )
                                        triggerAnimacion = true
                                        guardar_icon = i

                                    } else {
                                        mostar_eliminar_guardado_dialog = true
                                    }

                                },
                                resetear_estado_loo = { triggerAnimacion = false }
                            )
                            spacer_vertical(20.dp)
                        }
                        item {
                            text_expandible_wrapp(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                "Acerca del lugar",
                                MaterialTheme.typography.titleLarge,
                            )
                            spacer_vertical(10.dp)
                        }
                        item {
                            if(tiendas_filtradas.comodidades.isNotEmpty()){
                            expandible_comidades_aforo(
                                tiendas_filtradas.nombre_tienda,
                                tiendas_filtradas.comodidades,
                                tiendas_filtradas.aforo,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                expandido = expander_comidades_aforo,
                            ) { expander_comidades_aforo = !expander_comidades_aforo }
                            spacer_vertical(10.dp)
                            }
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
                                        context,
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
                                horario_atencion = horarios,
                                estadoColor = color_Estado_flow,
                                localidad_tienda = tiendas_filtradas.localidad,
                                id_tienda = tiendas_filtradas.id_tienda,
                                expandido = expander_horario,
                                viewModelFiltros = viewModelFiltros
                            ) { expander_horario = !expander_horario }
                            spacer_vertical(10.dp)
                        }
                        item {
                            Expandible_Metodo_contacto(
                                id_tienda = tiendas_filtradas.id_tienda,
                                localidad_tienda = tiendas_filtradas.localidad ?: "barranca",
                                iconos_cosas_clikeables = iconos_cosas_clikeables,
                                context = context,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                expandido = expander_contacto,
                                metodos_contactos = tiendas_filtradas.metodo_contacto_tienda
                            ) { expander_contacto = !expander_contacto }
                            spacer_vertical(10.dp)
                        }

                        item {
                            val tieneMetodos = with(tiendas_filtradas.metodos_pago_tienda) {
                                yape.enable || plin.enable || agora.enable || efectivo.enable || visa_mastercard.enable
                            }

                            if (tieneMetodos) {
                                item_metodos_de_pago(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    metodos_pago = tiendas_filtradas,
                                    expandido = expander_metodos_pagos,
                                    onClickExpand = {
                                        expander_metodos_pagos = !expander_metodos_pagos
                                    }
                                )
                                spacer_vertical(10.dp)
                            }
                        }

                        item {
                            Expandible_qr_tienda(
                                qrBitmap,
                                generar_qr_tienda_id = qr_generado_tienda,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                expandido = expander_qr_tienda
                            ) { expander_qr_tienda = !expander_qr_tienda }

                            spacer_vertical(10.dp)
                        }

                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(horizontal = 10.dp)
//                                    .clip(CircleShape)
//                                    .background(Color.White)
//                                    .height(40.dp)
//                                    .clickable {
//                                        mostar_dialog_verificar_perfil = true
//                                    }, contentAlignment = Alignment.Center
//                            ) {
//                                Row(
//                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Image(
//                                        painter = painterResource(R.drawable.icono_varificado_geinz),
//                                        contentDescription = "",
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                    texto_generico_one_line(
//                                        "Verificar y Administrar Perfil",
//                                        MaterialTheme.typography.bodyMedium, color = Color.Black
//                                    )
//                                }
//                            }
                        }

                        item {
                            spacer_vertical(20.dp)
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                texto_generico_one_line(
                                    "Última actualización: ${
                                        convertir_timesTAmp_fecha(
                                            tiendas_filtradas.timestamp
                                        )
                                    }", style = MaterialTheme.typography.bodyMedium
                                )

                            }
                        }
                        item {
                            spacer_vertical(20.dp)

                        }

                    }
                }
            }
            if (mostar_eliminar_guardado_dialog) {
                dialog_eliminar_favoritos(
                    viewModelFiltros = viewModelFiltros,
                    localidad_tienda = tiendas_filtradas.localidad ?: "barranca",
                    id_user = id_user,
                    id_tienda = tiendas_filtradas.id_tienda,
                    nombre_tienda = tiendas_filtradas.nombre_tienda,
                    ondimis = { mostar_eliminar_guardado_dialog = false }, aceptado = {
                        triggerAnimacion = false
                        guardar_icon = icono_select_fv
                    })
            }

            if (mostar_dialog_verificar_perfil) {
                dialog_administrar_perfil(
                    ondimis = { mostar_dialog_verificar_perfil = false },
                    contex = context,
                    id_tienda = tiendas_filtradas.id_tienda,
                    nombre_tienda = tiendas_filtradas.nombre_tienda
                )
            }
        }
    }


}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun cabezero_tiendas(
    numero_tienda:String,
    distanciaUsuarioTienda: Float?,
    nombreTienda: String, logo_tienda_img: String,
    iconos_cosas_clikeables: Boolean,
    localidad: String,
    id_tienda: String,
    verificar_intener: Boolean,
    triggerAnimacion: Boolean,
    guardar_icon: Boolean,
    viewModel_filtado_tiendas: viewModel_filtado_tiendas,
    modifier: Modifier = Modifier,
    estadoColor: Color,
    direccion: String,
    referencia: String,
    categoritienda: String,
    nombre_tienda: String,
    latitud: Double,
    longitud: Double,
    img_tienda_perfil: String,
    lista_img: obtener_img_tiendas,
    lista_tags: List<String>, guaradar_select: (Boolean) -> Unit, resetear_estado_loo: () -> Unit
) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")

        }
    }
    val context = LocalContext.current
    val mostrarDialogo = rememberSaveable { mutableStateOf(false) }
    val mostrarDialog_sin_google_maps = rememberSaveable { mutableStateOf(false) }
    var expdir_img by remember { mutableStateOf(false) }
    var mostrarDialogozoom by remember { mutableStateOf(false) }



    if (mostrarDialogozoom) {
        ZoomableGalleryFullScreen(
            compartir_promocion(),
            imagenes = listOf(img_tienda_perfil),
            startIndex = 0,
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
                verificarGPS(context, launcher)
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
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {


                // 🔹 CARD PERFIL
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    perfil_img_zooom(
                        categoritienda, localidad, id_tienda, nombreTienda,
                        distanciaUsuarioTienda = distanciaUsuarioTienda,
                        triggerAnimacion = triggerAnimacion,
                        modifier = Modifier.fillMaxWidth(),
                        img_tienda_perfil = img_tienda_perfil,
                        expandido = { expdir_img = !expdir_img },
                        mostrarDialogozoom = { mostrarDialogozoom = true },
                        resetear_estado_lott = resetear_estado_loo
                    )
                }


                // 🔹 COLLAGE (ocupa lo que necesite)
                if (lista_img.lista_ambiernte.isNotEmpty()) {
                    CollageGoogleMapsStyle_sin_scroll(
                        categoritienda,
                        compartir_promocion(),
                        "ambiente",
                        1.1f,
                        imagenes = lista_img.lista_ambiernte
                    )
                }


                       // 🔹 COLLAGE (ocupa lo que necesite)
                if (lista_img.lista_productos.isNotEmpty()) {
                    CollageGoogleMapsStyle_sin_scroll(
                        categoritienda,
                        compartir_promocion(),
                        "productos",
                        1.1f,
                        imagenes = lista_img.lista_productos
                    )
                }


                // 🔹 COLLAGE (ocupa lo que necesite)
                if (lista_img.lista_promociones.isNotEmpty()) {
                    CollageGoogleMapsStyle_sin_scroll_promociones(
                        compartir_promocion(nombre_tienda,id_tienda,localidad, URLEncoder.encode(categoritienda, "UTF-8"),numero_tienda),
                        "promociones",
                        1.1f,
                        imagenes = lista_img.lista_promociones
                    )
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
                perfil_cabezero(
                    distanciaUsuarioTienda = distanciaUsuarioTienda,
                    lat = latitud, lng = longitud,
                    iconos_cosas_clikeables = iconos_cosas_clikeables,
                    localida = localidad,
                    id_tienda = id_tienda,
                    viewModelFiltros = viewModel_filtado_tiendas,
                    nombre_tienda = nombre_tienda,
                    estadoColor = estadoColor,
                    categoritienda = categoritienda,
                    lista_tags = lista_tags
                )
            }
            spacer_horizonta(15.dp)
            abrir_google_maps(
                iconos_cosas_clikeables = iconos_cosas_clikeables,
                id_tienda = id_tienda,
                localidad = localidad,
                verificar_intener = verificar_intener,
                guardar_icon = guardar_icon,
                context = context,
                latitud = latitud,
                longitud = longitud,
                mostrarDialogo = { dialog_ ->
                    mostrarDialogo.value = dialog_
                },
                guaradar_select = { guaradar_select(!guardar_icon) })
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun perfil_img_zooom(
    categoria: String, localidad: String, id_tienda: String, nombre_tienda: String,
    distanciaUsuarioTienda: Float?,
    triggerAnimacion: Boolean,
    modifier: Modifier = Modifier,
    img_tienda_perfil: String,
    expandido: () -> Unit,
    mostrarDialogozoom: () -> Unit,
    resetear_estado_lott: () -> Unit
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bandai_dokkan))
    var showAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(triggerAnimacion) {
        Log.d("entramos", triggerAnimacion.toString())
        if (triggerAnimacion) {
            showAnimation = true
            delay(4000)
            showAnimation = false
            resetear_estado_lott()
        } else {
            showAnimation = false
        }
    }
    val gps_enable = isGPSEnabled(context)
    Box(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img_tienda_perfil)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias).build(),
                contentDescription = "Imagen de la tienda",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { mostrarDialogozoom() },
                onState = { state ->
                }
            )
            AnimatedVisibility(
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
            AnimatedVisibility(
                showAnimation,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LottieAnimation(
                    composition,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .align(Alignment.TopCenter)
                )
            }

            AnimatedVisibility(
                gps_enable,
                modifier = Modifier
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.60f))
                    .align(Alignment.BottomStart)
            ) {
                distanciaUsuarioTienda?.let { distancia ->
                    Text(
                        text = "Aproximadamente a ${formatDistancia(distancia)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White, modifier = Modifier.padding(7.dp)
                    )
                }

            }

        }
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
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        compartirLugarFirebaseHosttiendas(
                            categoria,
                            context,
                            localidad,
                            id_tienda,
                            img_tienda_perfil,
                            nombre_tienda
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.compartir_icon_unico_blanco),
                    contentDescription = "compartir",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

}

@Composable
fun perfil_cabezero(
    distanciaUsuarioTienda: Float?,
    lat: Double, lng: Double, // Lat/lng de la tienda
    iconos_cosas_clikeables: Boolean,
    localida: String,
    id_tienda: String,
    viewModelFiltros: viewModel_filtado_tiendas,
    nombre_tienda: String,
    estadoColor: Color,
    categoritienda: String,
    lista_tags: List<String>
) {
    val context = LocalContext.current

    Log.d("tienda:tienda:tienda:tienda:tienda:", "$id_tienda $localida")
    val horarios by viewModelFiltros.horariosTiendas_real.collectAsState()

    LaunchedEffect(id_tienda, localida) {
        if (id_tienda.isNotBlank() && localida.isNotBlank()) {
            viewModelFiltros.repo_filtrado.escucharHorarioDeTiendaUnica(
                idTiendaBuscada = id_tienda,
                localidad = localida
            )
        }
    }

    val tick by viewModelFiltros.tick.collectAsState()
//    val gps_enable = isGPSEnabled(context)
    Column {
        Text(
            text = nombre_tienda.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        spacer_vertical(5.dp)


        retornar_color_estado_tienda_Box(
            id_tienda = id_tienda,
            horario_total = horarios[id_tienda] ?: HorarioDia_box(),
            tick = tick,
            pagado = true,
            color = { color, txt ->
                viewModelFiltros.setear_color(color)
            })
        spacer_vertical(5.dp)
//    TextoCopiable(id_tienda)
        text_expandible_wrapp(
            texto = "${categoritienda.capitalizeFirst()}",
            style = MaterialTheme.typography.bodyMedium
        )
        spacer_vertical(10.dp)
        tags_subcateogiras(
            lista_tags,
            brush_start = Brush.horizontalGradient(colors = shadow_left),
            brush_end = Brush.horizontalGradient(colors = shadow_right),
            modifier = Modifier.padding(end = 40.dp)
        )
    }
}


@Composable
fun TextoCopiable(id_tienda: String) {
    val context = LocalContext.current

    Text(
        text = id_tienda,
        fontSize = 16.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.clickable {
            // Obtener el ClipboardManager
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ID Tienda", id_tienda)
            clipboard.setPrimaryClip(clip)

            // Opcional: mensaje corto de confirmación
            Toast.makeText(context, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun abrir_google_maps(
    iconos_cosas_clikeables: Boolean,
    id_tienda: String,
    localidad: String,
    verificar_intener: Boolean,
    guardar_icon: Boolean,
    context: Context,
    latitud: Double,
    longitud: Double,
    mostrarDialogo: (Boolean) -> Unit,
    guaradar_select: () -> Unit
) {


    val color_guardar_fondo by animateColorAsState(
        targetValue = if (!guardar_icon) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = ""
    )

    Row() {

        FloatingActionButton(
            onClick = {
                constantes_lista_localidades.abrir_google_maps(
                    "tienda", id_tienda, localidad,
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

        spacer_horizonta(10.dp)

        val icono_entrega = if (!guardar_icon) {
            R.drawable.icon_corazon_borde
        } else {
            R.drawable.icon_borde_corazon_completo
        }
        AnimatedVisibility(verificar_intener, enter = fadeIn(), exit = fadeOut()) {
            FloatingActionButton(
                onClick = {
                    if (iconos_cosas_clikeables) {
                        guaradar_select()
                    } else {
                        Toast.makeText(
                            context,
                            "solo puedes guardar tiendas reales",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = color_guardar_fondo,
            ) {
                Image(
                    painter = painterResource(icono_entrega),
                    modifier = Modifier.size(22.dp),
                    contentDescription = "Favorito",
                )
            }
        }
        spacer_horizonta(10.dp)
//        AnimatedVisibility(verificar_intener, enter = fadeIn(), exit = fadeOut()) {
//            FloatingActionButton(
//                onClick = {
//                    compartirLugarFirebaseHosttiendas(
//                        categoria,
//                        context,
//                        localidad,
//                        id_tienda,
//                        img_tienda_perfil,
//                        nombre_tienda
//                    )
//                },
//                modifier = Modifier.size(40.dp),
//                containerColor = MaterialTheme.colorScheme.primary,
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.compartir_icon_vector),
//                    modifier = Modifier.size(22.dp),
//                    contentDescription = "Favorito",
//                )
//            }
//        }

    }
}


@Composable
fun Expandible_descripcion_tienda(
    modifier: Modifier = Modifier,
    descipcion_tienda: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Cartas_expandibles(modifier = modifier) {
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
    context: Context,
    modifier: Modifier = Modifier,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextoExpandibleEnLinea(
                            texto = "Dirección: ${direccion.capitalizeFirst()}",
                        )
                    }
                    spacer_vertical(10.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextoExpandibleEnLinea(
                            texto = "Referencia : ${referencia.capitalizeFirst()}",
                        )

                    }
//                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
//                    text_expandible_wrapp(texto = "Referencia : ${referencia.capitalizeFirst()}", modifier = Modifier.weight(1f))
//                        spacer_horizonta(10.dp)
//                        Image(
//                            painter = painterResource(
//                                R.drawable.baseline_content_copy_24
//                            ), contentDescription = "", modifier = Modifier.size(22.dp).padding(end = 5.dp).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }){
//                                constantestextos_general.copiarTexto_portapapeles_compouse(referencia, context)
//                            }
//                        )
//
//                    }
                    spacer_vertical(15.dp)
                    text_expandible_wrapp(texto = "Tipo de tienda : $fisica_virtual")
                    spacer_vertical(10.dp)
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Expandible_Metodo_contacto(
    id_tienda: String,
    localidad_tienda: String,
    iconos_cosas_clikeables: Boolean,
    context: Context,
    modifier: Modifier = Modifier,
    expandido: Boolean,
    metodos_contactos: metodo_contacto_tienda,
    onClickExpand: () -> Unit
) {
//    val context = LocalContext.current
    var call_dialog_permise by remember { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }
    Cartas_expandibles(modifier = modifier) {
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
                            tipo = "whatsapp",
                            clikeable_estado = iconos_cosas_clikeables,
                            icono_red = R.drawable.whatsapp_icon,
                            texto = metodos_contactos.whatsapp.numero
                        ) {
                            abrir_whattsapp(
                                "tienda", id_tienda,
                                localidad_tienda, context, metodos_contactos.whatsapp.numero
                            )
                        }
                    }
                    if (metodos_contactos.llamada.estado) {
                        item_metodo_contacto(
                            "llamada",
                            clikeable_estado = iconos_cosas_clikeables,
                            icono_red = R.drawable.llamada_icon,
                            texto = metodos_contactos.llamada.numero
                        ) {
                            llamar(
                                "tienda", id_tienda,
                                localidad_tienda, context, metodos_contactos.llamada.numero, {
                                    call_dialog_permise = true
                                    numero_llamada = metodos_contactos.llamada.numero
                                })
                        }
                    }
                    if (metodos_contactos.tiktok.estado) {
                        item_metodo_contacto(
                            "tk",
                            clikeable_estado = iconos_cosas_clikeables,
                            icono_red = R.drawable.tik_tok_icon,
                            texto = metodos_contactos.tiktok.nombre
                        ) {
                            openTiktok(
                                "Tienda",
                                context, metodos_contactos.tiktok.url, id_tienda,
                                localidad_tienda
                            )
                        }
                    }
                    if (metodos_contactos.sitio_web.estado) {
                        item_metodo_contacto(
                            "web",
                            clikeable_estado = iconos_cosas_clikeables,
                            icono_red = R.drawable.web_icon,
                            texto = metodos_contactos.sitio_web.nombre
                        ) {
                            openWebLink(
                                context, metodos_contactos.sitio_web.url, id_tienda,
                                localidad_tienda
                            )
                        }
                    }
                    if (metodos_contactos.instagram.estado) {
                        item_metodo_contacto(
                            "ig",
                            clikeable_estado = iconos_cosas_clikeables,
                            icono_red = R.drawable.instagram_icon,
                            texto = metodos_contactos.instagram.nombre
                        ) {
                            openInstagram(
                                "Tienda",
                                context,
                                metodos_contactos.instagram.url,
                                id_tienda,
                                localidad_tienda
                            )
                        }
                    }
                    if (metodos_contactos.facebook.estado) {
                        item_metodo_contacto(
                            "fb",
                            clikeable_estado = iconos_cosas_clikeables,
                            icono_red = R.drawable.facebook_icon,
                            texto = metodos_contactos.facebook.nombre
                        ) {
                            openFacebook(
                                "Tienda",
                                context, metodos_contactos.facebook.url, id_tienda,
                                localidad_tienda
                            )
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
    qrBitmap: Bitmap?,
    generar_qr_tienda_id: String,
    modifier: Modifier = Modifier,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    val context = LocalContext.current

//    // 🔥 ESTADO FUERA DEL AnimatedVisibility
//    var qrBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
//
//    // 🔥 Se genera UNA sola vez cuando el ID es válido
//    LaunchedEffect(generar_qr_tienda_id) {
//        if (generar_qr_tienda_id.isBlank()) return@LaunchedEffect
//        if (qrBitmap != null) return@LaunchedEffect
//
//        qrBitmap = withContext(Dispatchers.Default) {
//            generarQrBitmapAltaCalidad(context, generar_qr_tienda_id)
//        }
//    }

    Cartas_expandibles(modifier = modifier) {
        Column {
            expandibles_wrapp(
                texto_params = "Cuéntanos tu experiencia",
                iconRes = null,
                iconVector = Icons.Filled.Star,
                expandido = expandido,
                onClickExpand = onClickExpand
            )
        }

        // 👇 AnimatedVisibility SOLO controla UI
        AnimatedVisibility(visible = expandido) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                texto_generico_multilinea(
                    "¡Tu opinión cuenta! Escanea este código con Geinz y deja tu reseña sobre tu experiencia. Geinz verificará tu ubicación para confirmar que estuviste aquí y mantener reseñas auténticas.",
                    MaterialTheme.typography.bodyMedium,
                    Color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}




@Composable
fun item_metodos_de_pago(
    modifier: Modifier = Modifier, metodos_pago: modelo_tienda, expandido: Boolean,
    onClickExpand: () -> Unit
) {
    var mostrar_dialog_pagos by remember { mutableStateOf(false) }
    var metodoPagoSeleccionado by remember { mutableStateOf(item_metodos_pago()) }

    Cartas_expandibles(modifier = modifier) {
        Column {
            expandibles_wrapp(
                "Metodos de pago",
                iconRes = null,
                Icons.Default.Payment,
                expandido,
                onClickExpand
            )
        }
        AnimatedVisibility(visible = expandido) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (metodos_pago.metodos_pago_tienda.yape.enable) {
                    item {
                        car_metodos_de_pago(
                            img = R.drawable.yape_logo,
                            nombre = "Yape"
                        ) {
                            if (metodos_pago.metodos_pago_tienda.yape.nombre.isNotEmpty() || metodos_pago.metodos_pago_tienda.yape.numero.isNotEmpty()) {

                                mostrar_dialog_pagos = true
                                metodoPagoSeleccionado = item_metodos_pago(
                                    metodos_pago.metodos_pago_tienda.yape.qr,
                                    metodos_pago.metodos_pago_tienda.yape.numero,
                                    R.drawable.yape_logo,
                                    "Yape",
                                    metodos_pago.metodos_pago_tienda.yape.nombre,

                                    )
                            }
                        }
                    }
                }

                if (metodos_pago.metodos_pago_tienda.plin.enable) {
                    item {
                        car_metodos_de_pago(
                            img = R.drawable.logo_plin,
                            nombre = "Plin"
                        ) {
                            if (metodos_pago.metodos_pago_tienda.plin.nombre.isNotEmpty() || metodos_pago.metodos_pago_tienda.plin.numero.isNotEmpty()) {
                                mostrar_dialog_pagos = true
                                metodoPagoSeleccionado = item_metodos_pago(
                                    metodos_pago.metodos_pago_tienda.plin.qr,
                                    metodos_pago.metodos_pago_tienda.plin.numero,
                                    R.drawable.logo_plin,
                                    "Plin",
                                    metodos_pago.metodos_pago_tienda.plin.nombre,

                                    )
                            }
                        }
                    }
                }

                if (metodos_pago.metodos_pago_tienda.agora.enable) {
                    item {
                        car_metodos_de_pago(
                            img = R.drawable.logo_agora,
                            nombre = "Agora"
                        ) { }
                    }
                }

                if (metodos_pago.metodos_pago_tienda.efectivo.enable) {
                    item {
                        car_metodos_de_pago(
                            img = 0,
                            nombre = "Efectivo"
                        ) { }
                    }
                }

                if (metodos_pago.metodos_pago_tienda.visa_mastercard.enable) {
                    item {
                        car_metodos_de_pago(
                            img = R.drawable.visa_logo,
                            nombre = "Visa"
                        ) { }
                    }

                    item {
                        car_metodos_de_pago(
                            img = R.drawable.master_car_logo,
                            nombre = "Mastercard"
                        ) { }
                    }
                }


            }
        }

    }
    if (mostrar_dialog_pagos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog_qr_pago_tienda(
                metodoPagoSeleccionado,
                { mostrar_dialog_pagos = !mostrar_dialog_pagos })
        }
    }
}

@Composable
fun car_metodos_de_pago(img: Int, nombre: String, listener: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))

            .padding(10.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                listener()
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            if (img == 0) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)) // Verde tipo "dinero"
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Efectivo",
                        tint = Color.White, // Ícono blanco
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(img)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .build(), contentDescription = "Imagen",
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            texto_generico_one_line(nombre, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun item_metodo_contacto(
    tipo:String,
    clikeable_estado: Boolean,
    icono_red: Int,
    texto: String,
    click_icon: () -> Unit
) {
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
                    .clip(CircleShape)
                    .size(30.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        if (clikeable_estado) {
                            click_icon()
                        } else {
                            Toast.makeText(context, "Solo es prueva", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentDescription = ""
            )
            spacer_horizonta(10.dp)
            Text(
                text = if(tipo.equals("whatsapp") || tipo.equals("llamada")){constantes_lista_localidades.ocultarNumero(texto)}else{
                    texto
                },
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
    modifier: Modifier = Modifier,
    horario_atencion: HorarioAtencion_box,
    estadoColor: Color,
    localidad_tienda: String?,
    id_tienda: String,
    expandido: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    onClickExpand: () -> Unit
) {
    Log.d("horario_atencion","$horario_atencion")
//    val horarioTienda by viewModelFiltros.horarioTienda.observeAsState(null)
//    val horarioRecordado = remember(horarioTienda) { horarioTienda }


    var cargado by remember { mutableStateOf(false) }

//    LaunchedEffect(expandido) {
//        if (expandido && !cargado) {
//            viewModelFiltros.obtenerHorarioPorTienda(localidad_tienda!!, id_tienda)
//            cargado = true
//        }
//    }
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
                    texto_generico_multilinea(
                        "El horario mostrado corresponde al horario continuo .Si se maneja turnos divididos —por ejemplo, mañana y tarde—, estos se reflejarán correctamente en el horario actualizado en tiempo real.",
                        style = MaterialTheme.typography.bodyMedium,
                        Modifier.padding(horizontal = 10.dp)
                    )
                    spacer_vertical(5.dp)
                    MostrarHorarioTienda(horario_atencion, estadoColor)
                }
            }
        }
    }
}

@Composable
fun expandible_comidades_aforo(
    nombre_luga: String,
    listaComodidades: List<ServicioComodidadUI>,
    aforo: Number,
    modifier: Modifier = Modifier,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    Cartas_expandibles(modifier = modifier) {
        Column {
            expandibles_wrapp(
                "Comodidades y aforo",
                iconRes = null,
                  Icons.Filled.Wifi,
                expandido,
                onClickExpand
            )

            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {

                    texto_generico_multilinea(
                        "Comodidades que $nombre_luga para ti",
                        style = MaterialTheme.typography.bodyMedium,
                        Modifier.padding(horizontal = 10.dp)
                    )

                    spacer_vertical(15.dp)

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            listaComodidades.filter { it.activo }
                        ) { comodidad ->
                            campos_atributos(
                                tipo = comodidad.nombre,
                                icono = comodidad.icono
                            )
                        }
                    }
                    spacer_vertical(15.dp)

                    AforoCard(aforo.toInt())
                }
            }
        }
    }
}

@Composable
fun AforoCard(
    capacidadMax: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier= Modifier.padding(10.dp)) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2), RoundedCornerShape(8.dp)) // azul con bordes redondeados
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "AFORO",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtítulo
        Text(
            text = "CAPACIDAD MÁXIMA",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Valor
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$capacidadMax personas",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
    }
    }
}



@Composable
fun campos_atributos(
    tipo: String,
    icono: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icono),
            contentDescription = tipo,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
        )

        texto_generico_one_line(
            tipo.capitalizeFirst(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
fun MostrarHorarioTienda(
    horarioTienda: HorarioAtencion_box,
    estadoColor: Color,
) {
    val diaActual = obtenerDiaActualEnEspañol().lowercase()

    val listaHorarios = listOf(
        "lunes" to horarioTienda.lunes,
        "martes" to horarioTienda.martes,
        "miércoles" to horarioTienda.miércoles,
        "jueves" to horarioTienda.jueves,
        "viernes" to horarioTienda.viernes,
        "sábado" to horarioTienda.sábado,
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

                val textoHorario = when {
                    horario.cerrado -> if (horario.motivo.isEmpty()) {
                        "Cerrado"
                    } else {
                        horario.motivo.capitalizeFirst()
                    }

                    horario.bloques.isEmpty() -> "Cerrado"

                    else -> {
                        val texto = unificarBloques(horario.bloques)
                        if (horario.bloques.size == 1 &&
                            horario.bloques.first().h_apertura == "00:00" &&
                            horario.bloques.first().h_cierre == "23:59"
                        ) "Abierto las 24h" else texto
                    }
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

fun unificarBloques(bloques: List<HorarioBloque>): String {
    if (bloques.isEmpty()) return "Sin horario"

    // Ordenar por hora de apertura por seguridad
    val ordenados = bloques.sortedBy { it.h_apertura }

    val primeraApertura = ordenados.first().h_apertura
    val ultimaCierre = ordenados.last().h_cierre

    val aperturaAMPM = formatearHoraAMPM(primeraApertura)
    val cierreAMPM = formatearHoraAMPM(ultimaCierre)

    return "$aperturaAMPM - $cierreAMPM"
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


@RequiresApi(Build.VERSION_CODES.O)
fun compartirLugarFirebaseHosttiendas(
    categoria: String,
    context: Context,
    localidad_tienda: String,
    id_tienda: String,
    img: String,
    nombre_tienda: String
) {
    try {
        val repo_erese_socio = repo_eres_socio()
        // Construimos el link de la Cloud Function
        val link = "https://geinzworkapp.web.app/share?" +
                "t=ti" +
                "&id=${URLEncoder.encode(id_tienda, "UTF-8")}" +
                "&l=${URLEncoder.encode(localidad_tienda, "UTF-8")}" +
                "&c=${URLEncoder.encode(categoria, "UTF-8")}"

        val texto = "¡Mira $nombre_tienda en Geinz! 🔥\n$link"


        // Intent simple de compartir
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }

        // Abrimos el chooser para que el usuario seleccione la app
        context.startActivity(
            Intent.createChooser(intent, "Compartir con")
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        )
        repo_erese_socio.agregar_contador(
            "compartidos",
            id_tienda,
            localidad_tienda
        )
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir el lugar", Toast.LENGTH_SHORT).show()
    }
}




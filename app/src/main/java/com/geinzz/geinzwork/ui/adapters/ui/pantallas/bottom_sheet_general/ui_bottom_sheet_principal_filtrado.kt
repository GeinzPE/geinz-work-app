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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import com.geinzz.geinzwork.data.model.dataclass_novedades.promociones_de_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.ServicioComodidadUI
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.item_metodos_pago
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.promocion_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data.model.obtener_img_tiendas
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.get_alias_tienda.obtenerAliasTienda
import com.geinzz.geinzwork.model.LocalNavController
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
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.Unit

// ─────────────────────────────────────────────────────────────────────────
// TOKENS DE DISEÑO LOCALES (solo estética, no tocan lógica ni nombres)
// ─────────────────────────────────────────────────────────────────────────
private val SeccionCornerRadius = 22.dp
private val SeccionPaddingH = 16.dp
private val SeccionSpacing = 14.dp

@Composable
private fun ContenedorSeccion(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SeccionCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun bottom_sheet_tiendas_filtradas(
    id_tienda: String,
    localidad_tienda: String,
    verificar_intener: Boolean,
    viewModelFiltros: viewModel_filtado_tiendas,
    visible: Boolean,
    iconos_cosas_clikeables: Boolean = true,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val repo_socio = repo_eres_socio()
    val navController = LocalNavController.current
    var expandir_descripcion by rememberSaveable { mutableStateOf(false) }
    var expander_caracterisiticas by rememberSaveable { mutableStateOf(false) }
    var expander_contacto by rememberSaveable { mutableStateOf(false) }
    var expander_horario by rememberSaveable { mutableStateOf(false) }
    var expander_comidades_aforo by remember { mutableStateOf(false) }
    var expander_qr_tienda by rememberSaveable { mutableStateOf(false) }
    var expander_metodos_pagos by rememberSaveable { mutableStateOf(false) }


    val horarios by viewModelFiltros
        .horariosTiendas_real_completo
        .collectAsState(initial = HorarioAtencion_box())

    val color_Estado_flow by viewModelFiltros.color_estado_tienda_flow.collectAsState()
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val verificarfavorito by viewModelFiltros.existe_favorito.collectAsState()
    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""


    var guardar_icon by remember { mutableStateOf(false) }
    var mostar_eliminar_guardado_dialog by remember { mutableStateOf(false) }

    var icono_select_fv by remember { mutableStateOf(false) }

    var triggerAnimacion by remember { mutableStateOf(false) }
    LaunchedEffect(verificarfavorito) {
        guardar_icon = verificarfavorito
    }
    var ultimoProcesado by rememberSaveable { mutableStateOf("") }
    var inicioPerfil by rememberSaveable { mutableStateOf(0L) }
    var mostar_dialog_verificar_perfil by remember { mutableStateOf(false) }
    val datos_tienda by viewModelFiltros.datos_tienda_especifica.collectAsState()
    LaunchedEffect(datos_tienda) {
        Log.d("BOTTOM_SHEET_STATE", "estado actual: $datos_tienda")
    }
    var listo by remember { mutableStateOf(false) }

// 2. justo después de la línea tiendas_filtradas
    val tiendas_filtradas =
        (datos_tienda as? viewModel_filtado_tiendas.obtner_Datos_tiendas_espesifica.succes)?.data
            ?: modelo_tienda()
    val datosCorrespondenAlIdSolicitado = tiendas_filtradas.id_tienda == id_tienda
    val mostrarContenido = listo && datosCorrespondenAlIdSolicitado
    val direccion = tiendas_filtradas.ubicacion["dirección"]?.toString() ?: ""
    val referencia = tiendas_filtradas.ubicacion["referencia"]?.toString() ?: ""
    val longitud = (tiendas_filtradas.ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
    val latitud = (tiendas_filtradas.ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0
    val zona = (tiendas_filtradas.ubicacion["zona"] as? String) ?: ""
    val cargando = datos_tienda is viewModel_filtado_tiendas.obtner_Datos_tiendas_espesifica.loading


    var ultimoIdProcesado by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(id_tienda, localidad_tienda) {
        listo = false                    // 👈 reset inmediato, antes de pedir datos
        ultimoIdProcesado = ""           // 👈 fuerza reprocesar aunque el id se repita luego
        viewModelFiltros.obtener_datos_tienda_por_id(id_tienda, localidad_tienda)
    }

    LaunchedEffect(visible, cargando, id_user, id_tienda) {
        if (!visible || cargando) {
            listo = false
            return@LaunchedEffect
        }

        val nuevoId = tiendas_filtradas.id_tienda
        if (nuevoId.isBlank()) return@LaunchedEffect
        if (nuevoId != id_tienda) return@LaunchedEffect
        // Espera que el horario y favorito carguen
        viewModelFiltros.cast_horario_atencion_horario_tienda_box(tiendas_filtradas.horario_tienda_box)
        viewModelFiltros.verificar_existe_favorito(id_user, nuevoId)

        if (nuevoId != ultimoIdProcesado) {
            viewModelFiltros.repo_filtrado.escucharHorarioCompletoDeTiendaUnica(
                idTiendaBuscada = nuevoId,
                localidad = tiendas_filtradas.localidad ?: "barranca"
            )
            if (id_user.isNotBlank()) {
                repo_socio.agregar_contador(
                    "clic",
                    nuevoId,
                    tiendas_filtradas.localidad ?: "barranca",
                    id_user
                )
            }
        }

        ultimoIdProcesado = nuevoId
        inicioPerfil = System.currentTimeMillis()

        // ✅ Pequeño delay para que el horario/favorito lleguen del flow
        delay(400)
        listo = true  // ← recién ahora se muestra el contenido

        delay(5600)
        if (ultimoIdProcesado == nuevoId && id_user.isNotBlank()) {
            repo_socio.agregar_contador(
                "vistas",
                nuevoId,
                tiendas_filtradas.localidad ?: "barranca",
                id_user
            )
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
        qr_generado_tienda =
            retornar_id_Tienda_lugar(tiendas_filtradas.id_tienda, latitud, longitud)

    }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qr_generado_tienda) {
        if (qr_generado_tienda.isBlank()) return@LaunchedEffect
        if (qrBitmap != null) return@LaunchedEffect

        qrBitmap = withContext(Dispatchers.Default) {
            generarQrBitmapAltaCalidad(context, qr_generado_tienda)
        }
    }

    LaunchedEffect(tiendas_filtradas.id_tienda, tiendas_filtradas.localidad) {
        if (tiendas_filtradas.id_tienda.isNotBlank()) {
            viewModelFiltros.obtener_promociones_tienda(
                tiendas_filtradas.id_tienda,
                tiendas_filtradas.localidad ?: "barranca"
            )
        }
    }


    LaunchedEffect(tiendas_filtradas.id_tienda, tiendas_filtradas.localidad) {
        if (tiendas_filtradas.id_tienda.isNotBlank()) {
            viewModelFiltros.obtener_promociones_tienda(
                tiendas_filtradas.id_tienda,
                tiendas_filtradas.localidad ?: "barranca"
            )
        }
    }

    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = { onClose() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            if (!mostrarContenido) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        spacer_vertical(10.dp)
                        Text(
                            "Cargando tienda…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // ── DRAG HANDLE ────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.LightGray.copy(alpha = 0.7f))
                            )
                        }
                    }

                    // ── HEADER: galería + nombre + acciones ────────
                    item {
                        cabezero_tiendas(
                            id_user,
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
                            modifier = Modifier.padding(horizontal = SeccionPaddingH),
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
                        spacer_vertical(22.dp)
                    }

                    // ── SECCIÓN NO EXPANDIBLE: datos rápidos ───────
                    // (dirección corta + zona) siempre visibles, sin acordeón
                    if (direccion.isNotBlank() || zona.isNotBlank()) {
                        item {
                            FranjaDatosRapidos(
                                modifier = Modifier.padding(horizontal = SeccionPaddingH),
                                direccion = direccion,
                                zona = zona,
                                estadoColor = color_Estado_flow
                            )
                            spacer_vertical(SeccionSpacing)
                        }
                    }

                    // ── PROMOCIONES ACTIVAS ────────────────────────
                    item {
                        val estado_promociones by viewModelFiltros.promociones_tienda.collectAsState()
                        SeccionPromocionesTienda(
                            estadoPromociones = estado_promociones,
                            onVerTodasClick = { id, data ->
                                navController.navigate(
                                    promociones_de_tienda(
                                        id_tienda = id,
                                        parametros = data,
                                        localidad = tiendas_filtradas.localidad ?: "barranca"
                                    )
                                )
                            }
                        )
                    }

                    item {
                        text_expandible_wrapp(
                            modifier = Modifier.padding(
                                horizontal = SeccionPaddingH,
                                vertical = 2.dp
                            ),
                            "Acerca del lugar",
                            MaterialTheme.typography.titleLarge,
                        )
                        spacer_vertical(10.dp)
                    }
                    item {
                        if (tiendas_filtradas.comodidades.isNotEmpty()) {
                            expandible_comidades_aforo(
                                tiendas_filtradas.nombre_tienda,
                                tiendas_filtradas.comodidades,
                                tiendas_filtradas.aforo,
                                modifier = Modifier.padding(horizontal = SeccionPaddingH),
                                expandido = expander_comidades_aforo,
                            ) { expander_comidades_aforo = !expander_comidades_aforo }
                            spacer_vertical(SeccionSpacing)
                        }
                    }
                    item {
                        Expandible_descripcion_tienda(
                            modifier = Modifier.padding(horizontal = SeccionPaddingH),
                            tiendas_filtradas.descripcion,
                            expander_caracterisiticas
                        ) { expander_caracterisiticas = !expander_caracterisiticas }
                        spacer_vertical(SeccionSpacing)
                    }
                    item {
                        if (direccion.isNotBlank() || referencia.isNotBlank()) {
                            val fisica_virtual =
                                if (tiendas_filtradas.modelo_negocio) "Fisica" else "Virtual"

                            Column(modifier = Modifier.animateContentSize()) {
                                Expandible_direccion_ref(
                                    zona,
                                    context,
                                    modifier = Modifier.padding(horizontal = SeccionPaddingH),
                                    direccion,
                                    referencia,
                                    fisica_virtual,
                                    expandir_descripcion
                                ) {
                                    expandir_descripcion = !expandir_descripcion
                                }
                            }
                        }
                        spacer_vertical(SeccionSpacing)
                    }
                    item {
                        Expandible_horario_atencion(
                            modifier = Modifier.padding(horizontal = SeccionPaddingH),
                            horario_atencion = horarios,
                            estadoColor = color_Estado_flow,
                            localidad_tienda = tiendas_filtradas.localidad,
                            id_tienda = tiendas_filtradas.id_tienda,
                            expandido = expander_horario,
                            viewModelFiltros = viewModelFiltros
                        ) { expander_horario = !expander_horario }
                        spacer_vertical(SeccionSpacing)
                    }
                    item {
                        Expandible_Metodo_contacto(
                            id_user,
                            id_tienda = tiendas_filtradas.id_tienda,
                            localidad_tienda = tiendas_filtradas.localidad ?: "barranca",
                            iconos_cosas_clikeables = iconos_cosas_clikeables,
                            context = context,
                            modifier = Modifier.padding(horizontal = SeccionPaddingH),
                            expandido = expander_contacto,
                            metodos_contactos = tiendas_filtradas.metodo_contacto_tienda
                        ) { expander_contacto = !expander_contacto }
                        spacer_vertical(SeccionSpacing)
                    }
                    item {
                        val tieneMetodos = with(tiendas_filtradas.metodos_pago_tienda) {
                            yape.enable || plin.enable || agora.enable || efectivo.enable || visa_mastercard.enable
                        }

                        if (tieneMetodos) {
                            item_metodos_de_pago(
                                id_user,
                                modifier = Modifier.padding(horizontal = SeccionPaddingH),
                                metodos_pago = tiendas_filtradas,
                                expandido = expander_metodos_pagos,
                                onClickExpand = {
                                    expander_metodos_pagos = !expander_metodos_pagos
                                }
                            )
                            spacer_vertical(SeccionSpacing)
                        }
                    }
                    item {
                        Expandible_qr_tienda(
                            qrBitmap,
                            generar_qr_tienda_id = qr_generado_tienda,
                            modifier = Modifier.padding(horizontal = SeccionPaddingH),
                            expandido = expander_qr_tienda
                        ) { expander_qr_tienda = !expander_qr_tienda }

                        spacer_vertical(SeccionSpacing)
                    }
                    item {
                        // (sin cambios de lógica: espacio reservado tal como el original)
                    }

                    item {
                        spacer_vertical(24.dp)
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
                    id_user,
                    ondimis = { mostar_dialog_verificar_perfil = false },
                    contex = context,
                    id_tienda = tiendas_filtradas.id_tienda,
                    nombre_tienda = tiendas_filtradas.nombre_tienda
                )
            }
        }
    }


}

/**
 * Franja de datos rápidos — NO expandible, siempre visible.
 * Solo lectura de datos ya calculados (direccion, zona, estadoColor).
 * No introduce estado nuevo ni altera la lógica del resto del sheet.
 */
@Composable
private fun FranjaDatosRapidos(
    modifier: Modifier = Modifier,
    direccion: String,
    zona: String,
    estadoColor: Color
) {
    ContenedorSeccion(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(estadoColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = estadoColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            spacer_horizonta(12.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (direccion.isNotBlank()) direccion.capitalizeFirst() else "Ubicación no especificada",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (zona.isNotBlank()) {
                    spacer_vertical(2.dp)
                    Text(
                        text = "Zona: $zona",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(estadoColor)
            )
        }
    }
}


@Composable
fun cabezero_tiendas(
    id_user: String,
    numero_tienda: String,
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
            id_user,
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
                .padding(horizontal = SeccionPaddingH),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {


            // 🔹 CARD PERFIL
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.12f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                perfil_img_zooom(
                    id_user,
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
                    id_user = id_user,
                    categoria = categoritienda,
                    it = compartir_promocion(),
                    tag = "ambiente",
                    aspectRatio = 1.1f,
                    imagenes = lista_img.lista_ambiernte
                )
            }


            // 🔹 COLLAGE (ocupa lo que necesite)
            if (lista_img.lista_productos.isNotEmpty()) {
                CollageGoogleMapsStyle_sin_scroll(
                    id_user = id_user,
                    categoria = categoritienda,
                    it = compartir_promocion(),
                    tag = "productos",
                    aspectRatio = 1.1f,
                    imagenes = lista_img.lista_productos
                )
            }


            // 🔹 COLLAGE (ocupa lo que necesite)
            if (lista_img.lista_promociones.isNotEmpty()) {
                CollageGoogleMapsStyle_sin_scroll_promociones(
                    id_user = id_user,
                    it = compartir_promocion(
                        nombre_tienda,
                        id_tienda,
                        localidad,
                        URLEncoder.encode(categoritienda, "UTF-8"),
                        numero_tienda
                    ),
                    tag = "promociones",
                    aspectRatio = 1.1f,
                    imagenes = lista_img.lista_promociones
                )
            }


        }




        spacer_vertical(16.dp)

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
                id_user,
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


@Composable
fun perfil_img_zooom(
    id_user: String,
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
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { mostrarDialogozoom() },
                onState = { state ->
                }
            )

            // Scrim inferior sutil para legibilidad de la chip de distancia
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                            startY = 260f
                        )
                    )
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
                        .clip(RoundedCornerShape(24.dp))
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
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .align(Alignment.BottomStart)
            ) {
                distanciaUsuarioTienda?.let { distancia ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "A ${formatDistancia(distancia)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
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
                    .padding(10.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clickable {
                        compartirLugarFirebaseHosttiendas(
                            id_user,
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
                    modifier = Modifier.size(17.dp)
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
    Column {
        Text(
            text = nombre_tienda.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        spacer_vertical(6.dp)

        // Chip de estado (misma lógica de color, solo empaquetado visual)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(estadoColor.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            retornar_color_estado_tienda_Box(
                id_tienda = id_tienda,
                horario_total = horarios[id_tienda] ?: HorarioDia_box(),
                tick = tick,
                pagado = true,
                color = { color, txt ->
                    viewModelFiltros.setear_color(color)
                })
        }
        spacer_vertical(8.dp)

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            text_expandible_wrapp(
                texto = categoritienda.capitalizeFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }
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
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            // Obtener el ClipboardManager
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ID Tienda", id_tienda)
            clipboard.setPrimaryClip(clip)

            // Opcional: mensaje corto de confirmación
            Toast.makeText(context, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }
    )
}


@Composable
fun abrir_google_maps(
    iduser: String,
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
                    iduser,
                    "tienda", id_tienda, localidad,
                    context,
                    latitud,
                    longitud
                ) { dialogo ->
                    mostrarDialogo(dialogo)
                }
            },
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, CircleShape),
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.localidad_icon_general),
                contentDescription = "Localidad",
                modifier = Modifier.size(34.dp)
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
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape),
                containerColor = color_guardar_fondo,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Image(
                    painter = painterResource(icono_entrega),
                    modifier = Modifier.size(21.dp),
                    contentDescription = "Favorito",
                )
            }
        }
        spacer_horizonta(10.dp)

    }
}


@Composable
fun Expandible_descripcion_tienda(
    modifier: Modifier = Modifier,
    descipcion_tienda: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    ContenedorSeccion(modifier = modifier) {
        Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
            Column {
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
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        texto_expandido_wrapp_sin_max_line(descipcion_tienda)
                        spacer_vertical(4.dp)
                    }
                }
            }
        }
    }

}

@Composable
fun Expandible_direccion_ref(
    zona: String,
    context: Context,
    modifier: Modifier = Modifier,
    direccion: String,
    referencia: String,
    fisica_virtual: String,
    expandido: Boolean,
    onClickExpand: () -> Unit
) {
    ContenedorSeccion(modifier = modifier) {
        Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
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
                                horizontal = 14.dp,
                                vertical = 10.dp
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TextoExpandibleEnLinea(
                                texto = "Dirección: ${direccion.capitalizeFirst()}",
                            )
                        }
                        spacer_vertical(10.dp)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.4f
                            )
                        )
                        spacer_vertical(10.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TextoExpandibleEnLinea(
                                texto = "Referencia : ${referencia.capitalizeFirst()}",
                            )

                        }
                        spacer_vertical(14.dp)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            text_expandible_wrapp(texto = "Tipo de tienda : $fisica_virtual")
                        }

                        spacer_vertical(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append("Zona aproximada según el cuadrante comercial de Geinz. ")

                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(zona)
                                    }
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        }

                        spacer_vertical(6.dp)
                    }
                }
            }
        }
    }
}


@Composable
fun Expandible_Metodo_contacto(
    iduser: String,
    id_tienda: String,
    localidad_tienda: String,
    iconos_cosas_clikeables: Boolean,
    context: Context,
    modifier: Modifier = Modifier,
    expandido: Boolean,
    metodos_contactos: metodo_contacto_tienda,
    onClickExpand: () -> Unit
) {
    var call_dialog_permise by remember { mutableStateOf(false) }
    var numero_llamada by remember { mutableStateOf("") }
    ContenedorSeccion(modifier = modifier) {
        Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
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
                                horizontal = 14.dp,
                                vertical = 6.dp
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
                                    iduser,
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
                                    iduser,
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
                                    localidad_tienda, iduser
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
                                    localidad_tienda, iduser
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
                                    localidad_tienda, iduser
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
                                    localidad_tienda, iduser
                                )
                            }
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

    ContenedorSeccion(modifier = modifier) {
        Column {
            Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
                Column {
                    expandibles_wrapp(
                        texto_params = "Cuéntanos tu experiencia",
                        iconRes = null,
                        iconVector = Icons.Filled.Star,
                        expandido = expandido,
                        onClickExpand = onClickExpand
                    )
                }
            }

            // 👇 AnimatedVisibility SOLO controla UI
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    qrBitmap?.let {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White)
                                .padding(14.dp)
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "QR",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    texto_generico_multilinea(
                        "¡Tu opinión cuenta! Escanea este código con Geinz y deja tu reseña sobre tu experiencia. Geinz verificará tu ubicación para confirmar que estuviste aquí y mantener reseñas auténticas.",
                        MaterialTheme.typography.bodyMedium,
                        Color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun item_metodos_de_pago(
    iduser: String,
    modifier: Modifier = Modifier, metodos_pago: modelo_tienda, expandido: Boolean,
    onClickExpand: () -> Unit
) {
    var mostrar_dialog_pagos by remember { mutableStateOf(false) }
    var metodoPagoSeleccionado by remember { mutableStateOf(item_metodos_pago()) }

    ContenedorSeccion(modifier = modifier) {
        Column {
            Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
                Column {
                    expandibles_wrapp(
                        "Metodos de pago",
                        iconRes = null,
                        Icons.Default.Payment,
                        expandido,
                        onClickExpand
                    )
                }
            }
            AnimatedVisibility(visible = expandido) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    }
    if (mostrar_dialog_pagos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog_qr_pago_tienda(
                iduser,
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
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                listener()
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
    tipo: String,
    clikeable_estado: Boolean,
    icono_red: Int,
    texto: String,
    click_icon: () -> Unit
) {
    var context = LocalContext.current
    spacer_vertical(4.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icono_red),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(24.dp)
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
            }
            spacer_horizonta(10.dp)
            Text(
                text = if (tipo.equals("whatsapp") || tipo.equals("llamada")) {
                    constantes_lista_localidades.ocultarNumero(texto)
                } else {
                    texto
                },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        spacer_horizonta(14.dp)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    constantestextos_general.copiarTexto_portapapeles_compouse(texto, context)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.baseline_content_copy_24),
                modifier = Modifier.size(18.dp),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
    spacer_vertical(8.dp)

}


@Composable
fun texto_expandido_wrapp_sin_max_line(
    texto: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        style = style.copy(lineHeight = style.fontSize * 1.4f),
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
    Log.d("horario_atencion", "$horario_atencion")

    var cargado by remember { mutableStateOf(false) }

    ContenedorSeccion(modifier = modifier) {
        Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
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
                            .padding(bottom = 8.dp)
                    ) {
                        texto_generico_multilinea(
                            "El horario mostrado corresponde al horario continuo .Si se maneja turnos divididos —por ejemplo, mañana y tarde—, estos se reflejarán correctamente en el horario actualizado en tiempo real.",
                            style = MaterialTheme.typography.bodySmall,
                            Modifier.padding(horizontal = 14.dp)
                        )
                        spacer_vertical(8.dp)
                        MostrarHorarioTienda(horario_atencion, estadoColor)
                    }
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
    ContenedorSeccion(modifier = modifier) {
        Cartas_expandibles(modifier = Modifier.fillMaxWidth()) {
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
                            style = MaterialTheme.typography.bodySmall,
                            Modifier.padding(horizontal = 14.dp)
                        )

                        spacer_vertical(12.dp)

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                        spacer_vertical(14.dp)

                        AforoCard(aforo.toInt())
                    }
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
    Box(modifier = Modifier.padding(14.dp)) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1976D2), Color(0xFF1565C0))
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AFORO",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CAPACIDAD MÁXIMA",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$capacidadMax personas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.SemiBold
                    )
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Image(
            painter = painterResource(id = icono),
            contentDescription = tipo,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
        )
        spacer_vertical(6.dp)
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
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        listaHorarios.forEach { (dia, horario) ->
            val esDiaActual = dia == diaActual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (esDiaActual) estadoColor.copy(alpha = 0.10f) else Color.Transparent
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${dia.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (esDiaActual) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.width(90.dp)
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

                Text(
                    text = textoHorario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                if (esDiaActual) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(estadoColor)
                    )
                }
            }
            spacer_vertical(2.dp)
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


fun compartirLugarFirebaseHosttiendas(
    iduser: String,
    categoria: String,
    context: Context,
    localidad_tienda: String,
    id_tienda: String,
    img: String,
    nombre_tienda: String
) {

    try {

        val repo_erese_socio = repo_eres_socio()

        CoroutineScope(Dispatchers.IO).launch {

            val alias = obtenerAliasTienda(id_tienda, localidad_tienda)

            withContext(Dispatchers.Main) {

                val link = "https://geinztech.com/perfil/$alias"

                val texto = "¡Mira $nombre_tienda en Geinz! 🔥\n$link"

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, texto)
                }

                context.startActivity(
                    Intent.createChooser(intent, "Compartir con")
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                )

                repo_erese_socio.agregar_contador(
                    "compartidos",
                    id_tienda,
                    localidad_tienda,
                    iduser
                )
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir el lugar", Toast.LENGTH_SHORT).show()
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PROMOCIONES ACTIVAS — sección NO expandible, carrusel elegante
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun SeccionPromocionesTienda(
    estadoPromociones: viewModel_filtado_tiendas.carga_promociones,
    onVerTodasClick: (id: String, data: String) -> Unit
) {
    val context = LocalContext.current

    when (estadoPromociones) {

        is viewModel_filtado_tiendas.carga_promociones.loading -> {
            Column {
                text_expandible_wrapp(
                    modifier = Modifier.padding(
                        horizontal = SeccionPaddingH,
                        vertical = 2.dp
                    ),
                    "Promociones activas 🔥",
                    MaterialTheme.typography.titleLarge
                )

                spacer_vertical(10.dp)

                LazyRow(
                    contentPadding = PaddingValues(horizontal = SeccionPaddingH),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) {
                        card_promocion_loading()
                    }
                }

                spacer_vertical(SeccionSpacing)
            }
        }

        is viewModel_filtado_tiendas.carga_promociones.succes -> {

            val promos = estadoPromociones.items

            if (promos.isNotEmpty()) {

                val idTienda = promos.first().id_tienda

                Column {

                    text_expandible_wrapp(
                        modifier = Modifier.padding(
                            horizontal = SeccionPaddingH,
                            vertical = 2.dp
                        ),
                        "Promociones activas 🔥",
                        MaterialTheme.typography.titleLarge
                    )

                    spacer_vertical(10.dp)

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = SeccionPaddingH),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(promos.take(4)) { promo ->
                            card_promocion_tienda(
                                promo = promo,
                                context = context
                            )
                        }

                        if (promos.size > 4) {
                            item {
                                card_ver_todas_promos(
                                    onClick = {
                                        onVerTodasClick(
                                            idTienda,
                                            "promociones_tienda"
                                        )
                                    }
                                )
                            }
                        }
                    }

                    spacer_vertical(SeccionSpacing)
                }
            }
        }

        is viewModel_filtado_tiendas.carga_promociones.empty -> Unit

        is viewModel_filtado_tiendas.carga_promociones.error -> Unit
    }
}

@Composable
private fun card_promocion_tienda(promo: promocion_tienda, context: Context) {
    Box(
        modifier = Modifier
            .width(168.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(promo.lista_img)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = promo.titulo,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)),
                        startY = 70f
                    )
                )
        )

        Text(
            text = promo.titulo,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 54.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (promo.contactar_activo && promo.numero.isNotBlank()) {
                Image(
                    painter = painterResource(R.drawable.whatsapp_icon),
                    contentDescription = "WhatsApp",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { abrir_whatsapp_promocion(context, promo) },
                )

            }

            if (promo.compartir_activo) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable { compartir_promocion_tienda(context, promo) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.compartir_icon_unico_blanco),
                        contentDescription = "Compartir",
                        modifier = Modifier.size(15.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
private fun card_promocion_loading() {
    val transition = rememberInfiniteTransition(label = "shimmer_promo")
    val alpha by transition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmerAlpha"
    )

    Box(
        modifier = Modifier
            .width(168.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.Gray.copy(alpha = alpha * 0.4f),
                        Color.Gray.copy(alpha = alpha),
                        Color.Gray.copy(alpha = alpha * 0.4f)
                    )
                )
            )
    )
}

@Composable
private fun card_ver_todas_promos(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(168.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            }
            spacer_vertical(10.dp)
            Text(
                "Ver todas\nlas promos",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun localidad_a_codigo(localidad: String): String = when (localidad.lowercase()) {
    "barranca" -> "ba"
    else -> localidad.take(2).lowercase()
}

private fun abrir_whatsapp_promocion(context: Context, promo: promocion_tienda) {
    try {
        val link =
            "https://geinztech.com/api/share?t=prms&l=${localidad_a_codigo("barranca")}&pi=${promo.id_promocion}"
        val mensaje = "${promo.msje_whatsapp} $link"
        val numero = promo.numero.filter { it.isDigit() }
        val url = "https://api.whatsapp.com/send?phone=51$numero&text=${
            URLEncoder.encode(
                mensaje,
                "UTF-8"
            )
        }"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun compartir_promocion_tienda(context: Context, promo: promocion_tienda) {
    try {
        val link =
            "https://geinztech.com/api/share?t=prms&l=${localidad_a_codigo("barranca")}&pi=${promo.id_promocion}"
        val texto = "${promo.msje_compartir} $link"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartir con").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir la promoción", Toast.LENGTH_SHORT).show()
    }
}
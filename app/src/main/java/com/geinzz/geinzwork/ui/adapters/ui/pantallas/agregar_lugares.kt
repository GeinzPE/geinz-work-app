package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.Manifest

import android.util.Base64
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon



import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.BuildConfig
import com.geinzz.geinzwork.data.model.data_class_tienda_geinz
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.data.model.img_tienda
import com.geinzz.geinzwork.data.model.ingreso_date
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_numero
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_red
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.ref_ubi
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_metodo_individual
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.model.repo_agregar_datos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_cambiar_datos_tiendas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.abrirTimePicker
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.fechaActual
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.fechaUnaSemanaDespues
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.generarIdSeguro
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.timeStampNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.lista_localidad
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.gson.GsonBuilder
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.SecureRandom

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.border

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.data.model.data_class_turismo
import com.geinzz.geinzwork.data.model.img_turismo
import com.geinzz.geinzwork.data.model.ubicacion_turismo
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.animation.easeTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object GeofencingManager {

    // =========================
    // POLÍGONOS DE BARRANCA
    // =========================

    private val zonaSur = listOf(
        Pair(-77.7586301, -10.7478916),
        Pair(-77.7645550, -10.7503904),
        Pair(-77.7700057, -10.7526507),
        Pair(-77.77688094997941, -10.74238012141042),
        Pair(-77.7717891, -10.7378640),
        Pair(-77.7564977, -10.7367506),
        Pair(-77.7542475, -10.7412584),
        Pair(-77.7602514, -10.7442700),
        Pair(-77.7586301, -10.7478916)
    )

    private val sonaPlayera = listOf(
        Pair(-77.7679053252519, -10.757941967817928),
        Pair(-77.7628337, -10.7559458),
        Pair(-77.7613565, -10.7567195),
        Pair(-77.75655838530102, -10.766302143389424),
        Pair(-77.7620085, -10.7703684),
        Pair(-77.7679053252519, -10.757941967817928)
    )

    private val sonaCentricaPanamericana = listOf(
        Pair(-77.76019035538333, -10.744316585480519),
        Pair(-77.7532785, -10.7406811),
        Pair(-77.7493425, -10.7537332),
        Pair(-77.7558306, -10.7540662),
        Pair(-77.76019035538333, -10.744316585480519)
    )

    private val panamericanaNorte = listOf(
        Pair(-77.7532395, -10.7407583),
        Pair(-77.74811386801767, -10.738775530699925),
        Pair(-77.7430896, -10.7504602),
        Pair(-77.7463849, -10.7523272),
        Pair(-77.7493232, -10.7537259),
        Pair(-77.7532395, -10.7407583)
    )

    private val zonaCentro = listOf(
        Pair(-77.76538442901771, -10.750732084787856),
        Pair(-77.7586254, -10.7478992),
        Pair(-77.7558059, -10.7540882),
        Pair(-77.7594191, -10.7559450),
        Pair(-77.7612170, -10.7568464),
        Pair(-77.76314511416147, -10.755942593844523),
        Pair(-77.76538442901771, -10.750732084787856)
    )

    private val entrePlayaSurCentro = listOf(
        Pair(-77.76999855079166, -10.752634205967098),
        Pair(-77.7653824, -10.7507547),
        Pair(-77.7631347, -10.7559814),
        Pair(-77.7650495, -10.7567880),
        Pair(-77.7680039, -10.7580106),
        Pair(-77.76999855079166, -10.752634205967098)
    )

    private val zonaNorte = listOf(
        Pair(-77.7613271, -10.7568066),
        Pair(-77.7594360, -10.7559620),
        Pair(-77.7558203, -10.7541111),
        Pair(-77.75085405255727, -10.753849853980455),
        Pair(-77.74711030635795, -10.762560781158172),
        Pair(-77.75658461692613, -10.76649065099977),
        Pair(-77.7613271, -10.7568066)
    )

    /**
     * Ray Casting Algorithm
     */
    fun esPuntoEnPoligono(
        latitud: Double,
        longitud: Double,
        poligono: List<Pair<Double, Double>>
    ): Boolean {

        var dentro = false
        var j = poligono.size - 1

        for (i in poligono.indices) {

            val xi = poligono[i].first
            val yi = poligono[i].second

            val xj = poligono[j].first
            val yj = poligono[j].second

            val intersecta =
                ((yi > latitud) != (yj > latitud)) &&
                        (longitud < (xj - xi) * (latitud - yi) / (yj - yi) + xi)

            if (intersecta) {
                dentro = !dentro
            }

            j = i
        }

        return dentro
    }

    fun obtenerNombreZona(
        latitud: Double,
        longitud: Double
    ): String {

        return when {

            esPuntoEnPoligono(latitud, longitud, zonaSur) ->
                "Barranca - Entrada y salida zona sur"

            esPuntoEnPoligono(latitud, longitud, sonaPlayera) ->
                "Barranca - Zona playera"

            esPuntoEnPoligono(latitud, longitud, sonaCentricaPanamericana) ->
                "Barranca - Zona céntrica"

            esPuntoEnPoligono(latitud, longitud, panamericanaNorte) ->
                "Barranca - Salida y entrada, Panamericana Norte"

            esPuntoEnPoligono(latitud, longitud, zonaCentro) ->
                "Barranca - Zona céntrica"

            esPuntoEnPoligono(latitud, longitud, entrePlayaSurCentro) ->
                "Barranca - Entre zona playera, salida sur y zona céntrica"

            esPuntoEnPoligono(latitud, longitud, zonaNorte) ->
                "Barranca - Salida y entrada zona norte"
            else -> ""
        }
    }
}
// ─── Retrofit para geocodificación inversa Mapbox ──────────────────────────
private interface MapboxGeocodingApi {
    @GET("geocoding/v5/mapbox.places/{longitude},{latitude}.json")
    suspend fun reverseGeocode(
        @retrofit2.http.Path("longitude") longitude: Double,
        @retrofit2.http.Path("latitude") latitude: Double,
        @Query("access_token") token: String,
        @Query("language") language: String = "es",
        @Query("limit") limit: Int = 1
    ): MapboxGeocodingResponse
}

data class MapboxGeocodingResponse(
    val features: List<MapboxFeature>
)

data class MapboxFeature(
    val place_name: String
)

private val geocodingApi: MapboxGeocodingApi by lazy {
    Retrofit.Builder()
        .baseUrl("https://api.mapbox.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MapboxGeocodingApi::class.java)
}

// ─── Composable principal ──────────────────────────────────────────────────

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun datos_teindas() {
    var guardando_turismo by remember { mutableStateOf(false) }
    var modoActual by rememberSaveable { mutableStateOf(0) } // 0 = Tienda, 1 = Turismo

    val lista_metood_pago = listOf("Yape", "Plin", "Efectivo", "Agora", "visa/Mastercard", "SIP")
    val lista_medood_contacto = listOf("whatsapp", "telefono", "tiktok", "facebook", "instagram", "sitio web")
    val lista_modelo_negocio = listOf("Fisico", "virtual")
    val lista_pagado = listOf("Premiun", "Free")

    val viewmodel_agregar_datos: viewmodel_agregar_datos = viewModel()
    val context = LocalContext.current
    val horario_atencion = viewmodel_agregar_datos.obtenerHorarioAtencion()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    val repo_agregar_datos = repo_agregar_datos(context)

    // ── Coordenadas y mapa ──────────────────────────────────────────────
    var lat_ by rememberSaveable { mutableStateOf(0.0) }
    var lng_ by rememberSaveable { mutableStateOf(0.0) }
    var contadorClicks by rememberSaveable { mutableStateOf(0) }
    var mostar_geo by rememberSaveable { mutableStateOf(false) }
    var mostrar_mapa by rememberSaveable { mutableStateOf(false) }

    // ── Pagos ────────────────────────────────────────────────────────────
    var yape_select by rememberSaveable { mutableStateOf(false) }
    var plin_select by rememberSaveable { mutableStateOf(false) }
    var Efectivo2 by rememberSaveable { mutableStateOf(false) }
    var Agora2 by rememberSaveable { mutableStateOf(false) }
    var visa2 by rememberSaveable { mutableStateOf(false) }
    var sip2 by rememberSaveable { mutableStateOf(false) }

    // ── Contacto ─────────────────────────────────────────────────────────
    var tk2 by rememberSaveable { mutableStateOf(false) }
    var fb2 by rememberSaveable { mutableStateOf(false) }
    var ig2 by rememberSaveable { mutableStateOf(false) }
    var ws2 by rememberSaveable { mutableStateOf(false) }
    var tlf2 by rememberSaveable { mutableStateOf(false) }
    var stw2 by rememberSaveable { mutableStateOf(false) }
    var modelo_negocio by rememberSaveable { mutableStateOf(false) }
    var pagado by rememberSaveable { mutableStateOf(false) }
    var pedir_ayuda_ia by rememberSaveable { mutableStateOf(false) }
    var mostar_progrs_var_IA by remember { mutableStateOf(false) }

    // ── Textos ────────────────────────────────────────────────────────────
    var direccion by rememberSaveable { mutableStateOf("") }
    var latitud by rememberSaveable { mutableStateOf("") }
    var longitud by rememberSaveable { mutableStateOf("") }
    var referencia by rememberSaveable { mutableStateOf("") }

    var numero_yape by rememberSaveable { mutableStateOf("") }
    var titular_yape by rememberSaveable { mutableStateOf("") }
    var numero_plin by rememberSaveable { mutableStateOf("") }
    var titular_plin by rememberSaveable { mutableStateOf("") }
    var numero_sip by rememberSaveable { mutableStateOf("") }
    var titular_sip by rememberSaveable { mutableStateOf("") }

    var user_tk by rememberSaveable { mutableStateOf("") }
    var user_fb by rememberSaveable { mutableStateOf("") }
    var user_ig by rememberSaveable { mutableStateOf("") }
    var numero_whatsap by rememberSaveable { mutableStateOf("") }
    var numero_telefono by rememberSaveable { mutableStateOf("") }
    var sitio_web by rememberSaveable { mutableStateOf("") }
    var localidad by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var texto_nombre_lugar by rememberSaveable { mutableStateOf("") }
    var txt_descipcion by rememberSaveable { mutableStateOf("") }
    var valor_geohashin by rememberSaveable { mutableStateOf("") }

    var subcategoarias_selet by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_subcategoria by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_categorias by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_subcategorias_full by rememberSaveable { mutableStateOf(listOf<List<String>>()) }

    val lista_notificaion_select = listOf("turistico", "nuevos_negocios", "numeros_salud_seguridad", "tramites")
    var tipo_notificacion_select by remember { mutableStateOf("") }
    var cambiar_cat_sub by remember { mutableStateOf(false) }
    var tocandoMapa by remember { mutableStateOf(false) }

    // ── Foto tienda: solo URI en estado, base64 se genera al guardar ─────
    var foto_perfil_uri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // ── Launchers tienda ──────────────────────────────────────────────────
    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { foto_perfil_uri = it }
    }

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri -> foto_perfil_uri = uri }
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = crearUriCamaraMediaStore(context)
            cameraUri = uri
            launcherCamara.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val permisoGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launcherGaleria.launch("image/*")
        else Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
    }

    // ── Carga de categorías ──────────────────────────────────────────────
    LaunchedEffect(Unit) {
        val (d1, d2) = repo_agregar_datos.obtener_categorias()
        lista_categorias = d1
        lista_subcategorias_full = d2
    }

    // ── IA ───────────────────────────────────────────────────────────────
    LaunchedEffect(pedir_ayuda_ia) {
        if (pedir_ayuda_ia) {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash")
            try {
                val subcategoriaUnica = subcategoarias_selet.firstOrNull() ?: ""
                val prompt = generarPromptOptimizado(
                    texto_nombre_lugar, categoria, contadorClicks, subcategoriaUnica
                )
                val result = model.generateContent(prompt)
                txt_descipcion = result.text ?: ""
            } catch (e: Exception) {
                Log.e("Gemini", "Error al generar descripción: ${e.message}")
            } finally {
                pedir_ayuda_ia = false
                mostar_progrs_var_IA = false
            }
        }
    }

    // ── Geocodificación inversa cuando cambian coordenadas ───────────────
    LaunchedEffect(latitud, longitud) {
        if (latitud.isNotEmpty() && longitud.isNotEmpty()) {
            mostar_geo = true
            try {
                val response = geocodingApi.reverseGeocode(
                    longitude = lng_,
                    latitude = lat_,
                    token = BuildConfig.MAPBOX_ACCESS_TOKEN
                )
                direccion = response.features.firstOrNull()?.place_name ?: ""
            } catch (e: Exception) {
                Log.e("Mapbox", "Error geocoding: ${e.message}")
            }
        }
    }

    if (cambiar_cat_sub) {
        bottom_sheet_cambiar_datos_tiendas { cambiar_cat_sub = false }
    }

    // ── Estado exclusivo de Turismo ──────────────────────────────────────
    var titulo_turistico by rememberSaveable { mutableStateOf("") }
    var descripcion_turistica by rememberSaveable { mutableStateOf("") }
    var categorias_turisticas_select by rememberSaveable { mutableStateOf(listOf<String>()) }

    // ── Fotos turismo: SOLO URIs en estado, sin base64 ───────────────────
    var foto_principal_uri by remember { mutableStateOf<Uri?>(null) }
    var lista_imgs_uri by remember { mutableStateOf(listOf<Uri>()) }
    var cameraUri_turismo by remember { mutableStateOf<Uri?>(null) }
    var foto_turismo_target by remember { mutableStateOf("principal") } // "principal" | "lista"

    val lista_categorias_turisticas = listOf(
        "playa", "monumento", "mirador", "plaza", "parque", "iglesia",
        "historico", "recreativo", "arqueologico", "museo", "cultura", "fortaleza"
    )

    // ── Launchers turismo: solo guardan URI ──────────────────────────────
    val launcherGaleria_turismo = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (foto_turismo_target == "principal") foto_principal_uri = it
            else lista_imgs_uri = lista_imgs_uri + it
        }
    }

    val launcherCamara_turismo = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri_turismo?.let { uri ->
                if (foto_turismo_target == "principal") foto_principal_uri = uri
                else lista_imgs_uri = lista_imgs_uri + uri
            }
        }
    }

    val permisoCamara_turismo = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = crearUriCamaraMediaStore(context)
            cameraUri_turismo = uri
            launcherCamara_turismo.launch(uri)
        }
    }

    val permisoGaleria_turismo = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launcherGaleria_turismo.launch("image/*")
    }

    // ════════════════════════════════════════════════════════════════════
    // UI
    // ════════════════════════════════════════════════════════════════════
    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(
            selectedTabIndex = modoActual,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = modoActual == 0, onClick = { modoActual = 0 }, text = { Text("🏪 Tienda") })
            Tab(selected = modoActual == 1, onClick = { modoActual = 1 }, text = { Text("🗺️ Turismo") })
        }

        // ════════════════════════════════════════════════════════════════
        // TAB TIENDA
        // ════════════════════════════════════════════════════════════════
        if (modoActual == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = !tocandoMapa
            ) {
                item { SectionHeader("🖼️ Foto de perfil") }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (foto_perfil_uri != null) {
                                AsyncImage(
                                    model = foto_perfil_uri,
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    else Manifest.permission.READ_EXTERNAL_STORAGE
                                    if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED)
                                        launcherGaleria.launch("image/*")
                                    else permisoGaleria.launch(permiso)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("🖼️ Galería") }

                            Button(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        val uri = crearUriCamaraMediaStore(context)
                                        cameraUri = uri
                                        launcherCamara.launch(uri)
                                    } else {
                                        permisoCamara.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("📷 Cámara", color = Color.White) }
                        }
                    }
                }

                item { spacer_vertical(8.dp) }
                item { SectionHeader("📋 Información básica") }

                item {
                    OutlinedTextField(
                        value = texto_nombre_lugar,
                        onValueChange = { texto_nombre_lugar = it },
                        label = { texto_generico_one_line("Nombre del lugar") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            texto_generico_one_line(
                                "Nombre del lugar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        colors = camposColores()
                    )
                }

                item {
                    ExpandDropDown(lista_categorias, false, "", "Categoría") { seleccionado ->
                        categoria = seleccionado
                        val index = lista_categorias.indexOf(seleccionado)
                        if (index != -1) lista_subcategoria = lista_subcategorias_full[index]
                    }
                }

                item {
                    if (categoria.isNotEmpty()) {
                        chips_categorias(lista_subcategoria) { lista ->
                            subcategoarias_selet = lista
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = txt_descipcion,
                        onValueChange = { txt_descipcion = it },
                        label = { texto_generico_one_line("Descripción") },
                        placeholder = {
                            texto_generico_one_line(
                                "Escribe una descripción atractiva...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = camposColores(),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 8,
                        singleLine = false
                    )
                }

                item {
                    if (texto_nombre_lugar.length > 3 && categoria.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    contadorClicks++
                                    pedir_ayuda_ia = true
                                    mostar_progrs_var_IA = true
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                texto_generico_one_line(
                                    if (mostar_progrs_var_IA) "Generando..." else "✨ Generar con IA",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (mostar_progrs_var_IA) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        trackColor = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    ExpandDropDown(lista_localidad, false, "", "Localidad") { sub ->
                        localidad = sub
                    }
                }

                item {
                    ExpandDropDown(lista_modelo_negocio, false, "", "Modelo de negocio") { modelo ->
                        modelo_negocio = modelo == "Fisico"
                    }
                }

                item {
                    ExpandDropDown(lista_pagado, false, "", "Plan") { modelo ->
                        pagado = modelo == "Premiun"
                    }
                }

                item { SectionHeader("📍 Ubicación") }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            obtenerUbicacionConfiable(fusedLocationClient) { latLng, accuracy ->
                                                lat_ = latLng.latitude
                                                lng_ = latLng.longitude
                                                latitud = latLng.latitude.toString()
                                                longitud = latLng.longitude.toString()
                                                Log.d("GPS", "Precisión: ${accuracy}m")
                                            }
                                        } else {
                                            Toast.makeText(context, "Activa el permiso de ubicación", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("📡 GPS", color = Color.White, fontSize = 13.sp) }

                                Button(
                                    onClick = { mostrar_mapa = !mostrar_mapa },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (mostrar_mapa) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        if (mostrar_mapa) "🗺️ Ocultar mapa" else "🗺️ Abrir mapa",
                                        color = Color.White, fontSize = 13.sp
                                    )
                                }
                            }

                            AnimatedVisibility(visible = mostrar_mapa) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Toca el mapa para seleccionar la ubicación",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .shadow(2.dp, RoundedCornerShape(16.dp))
                                    ) {
                                        MapboxMapViewWithLocation(
                                            modifier = Modifier.fillMaxWidth(),
                                            onMapClick = { lat, lng ->
                                                lat_ = lat; lng_ = lng
                                                latitud = lat.toString()
                                                longitud = lng.toString()
                                            },
                                            onLocationUpdate = { lat, lng ->
                                                if (lat_ == 0.0 && lng_ == 0.0) {
                                                    lat_ = lat; lng_ = lng
                                                    latitud = lat.toString()
                                                    longitud = lng.toString()
                                                }
                                            },
                                            onTouchChange = { data -> tocandoMapa = data }
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.weight(1f),
                                    value = latitud, onValueChange = {},
                                    shape = RoundedCornerShape(16.dp),
                                    label = { texto_generico_one_line("Latitud") },
                                    colors = camposColores(), readOnly = true
                                )
                                OutlinedTextField(
                                    modifier = Modifier.weight(1f),
                                    value = longitud, onValueChange = {},
                                    shape = RoundedCornerShape(16.dp),
                                    label = { texto_generico_one_line("Longitud") },
                                    colors = camposColores(), readOnly = true
                                )
                            }

                            OutlinedTextField(
                                value = direccion, onValueChange = { direccion = it },
                                label = { texto_generico_one_line("Dirección") },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = camposColores()
                            )

                            OutlinedTextField(
                                value = referencia, onValueChange = { referencia = it },
                                label = { texto_generico_one_line("Referencia") },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = camposColores()
                            )

                            if (mostar_geo) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Geohash:", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    valor_geohashin = constantes_lista_localidades.geohashing(lat_, lng_)
                                    Text(valor_geohashin, style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item { SectionHeader("💳 Métodos de pago") }

                item {
                    ChipsCategoriasCheck(lista_metood_pago) { seleccionados ->
                        yape_select = "Yape" in seleccionados
                        plin_select = "Plin" in seleccionados
                        Efectivo2 = "Efectivo" in seleccionados
                        Agora2 = "Agora" in seleccionados
                        visa2 = "visa/Mastercard" in seleccionados
                        sip2 = "SIP" in seleccionados
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (yape_select) {
                            valor_txt_contacto("yape", numero_yape) { numero_yape = it }
                            OutlinedTextField(
                                value = titular_yape, onValueChange = { titular_yape = it },
                                label = { texto_generico_one_line("Titular de Yape") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(), colors = camposColores()
                            )
                        }
                        if (plin_select) {
                            valor_txt_contacto("plin", numero_plin) { numero_plin = it }
                            OutlinedTextField(
                                value = titular_plin, onValueChange = { titular_plin = it },
                                label = { texto_generico_one_line("Titular de Plin") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(), colors = camposColores()
                            )
                        }
                        if (sip2) {
                            valor_txt_contacto("sip", numero_sip) { numero_sip = it }
                            OutlinedTextField(
                                value = titular_sip, onValueChange = { titular_sip = it },
                                label = { texto_generico_one_line("Titular de SIP") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(), colors = camposColores()
                            )
                        }
                    }
                }

                item { SectionHeader("📞 Métodos de contacto") }

                item {
                    ChipsCategoriasCheck(lista_medood_contacto) { seleccionados ->
                        tk2 = "tiktok" in seleccionados
                        fb2 = "facebook" in seleccionados
                        ig2 = "instagram" in seleccionados
                        ws2 = "whatsapp" in seleccionados
                        tlf2 = "telefono" in seleccionados
                        stw2 = "sitio web" in seleccionados
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (tk2) valor_txt_contacto("tiktok", user_tk) { user_tk = it }
                        if (fb2) valor_txt_contacto("facebook", user_fb) { user_fb = it }
                        if (ig2) valor_txt_contacto("instagram", user_ig) { user_ig = it }
                        if (ws2) valor_txt_contacto("whatsapp", numero_whatsap) { numero_whatsap = it }
                        if (tlf2) valor_txt_contacto("telefono", numero_telefono) { numero_telefono = it }
                        if (stw2) valor_txt_contacto("sitio web", sitio_web) { sitio_web = it }
                    }
                }

                item { SectionHeader("🕐 Horario de atención") }
                item { HorarioSemanal(viewmodel_agregar_datos) }

                item { SectionHeader("⚙️ Acciones") }

                item {
                    var guardando_tienda by remember { mutableStateOf(false) }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (!guardando_tienda) {
                                    guardando_tienda = true
                                    val repo = repo_agregar_datos(context)
                                    val id_tienda = generarIdSeguro()

                                    scope.launch(Dispatchers.IO) {
                                        // ✅ Base64 solo al momento de guardar, en background
                                        val base64Logo = foto_perfil_uri?.let { uriToBase64(context, it) } ?: ""

                                        withContext(Dispatchers.Main) {
                                            val datos_enviar = data_class_tienda_geinz(
                                                base64Logo,
                                                categoria_tienda = categoria,
                                                descripcion = txt_descipcion,
                                                geogash = valor_geohashin,
                                                id_tienda = id_tienda,
                                                localida_tienda = localidad.lowercase(),
                                                modelo_negocio = modelo_negocio,
                                                nombre_tienda = texto_nombre_lugar,
                                                pagado = pagado,
                                                subcategoria = subcategoarias_selet,
                                                ubicacion = ref_ubi(
                                                    latitud = lat_,
                                                    longitud = lng_,
                                                    referencia = referencia,
                                                    dirección = direccion,
                                                ),
                                                metodo_pago = modelo_pagos_tienda(
                                                    visa_mastercard = modelo_metodo_individual(numero = "", qr = "", nombre = "", enable = visa2),
                                                    agora = modelo_metodo_individual(numero = "", qr = "", nombre = "", enable = Agora2),
                                                    efectivo = modelo_metodo_individual(numero = "", qr = "", nombre = "", enable = Efectivo2),
                                                    plin = modelo_metodo_individual(numero = numero_plin, qr = "", nombre = titular_plin, enable = plin_select),
                                                    yape = modelo_metodo_individual(numero = numero_yape, qr = "", nombre = titular_yape, enable = yape_select),
                                                ),
                                                metodo_contacto = metodo_contacto_tienda(
                                                    whatsapp = contacto_numero(estado = ws2, numero = numero_whatsap),
                                                    llamada = contacto_numero(estado = tlf2, numero = numero_telefono),
                                                    facebook = contacto_red(estado = fb2, nombre = user_fb, url = ""),
                                                    instagram = contacto_red(estado = ig2, nombre = user_ig, url = ""),
                                                    tiktok = contacto_red(estado = tk2, nombre = user_tk, url = ""),
                                                    sitio_web = contacto_red(estado = stw2, nombre = sitio_web, url = ""),
                                                ),
                                                fechas = ingreso_date(
                                                    hora_ingreso = constantes_horas.horaActual(),
                                                    fecha_ingreso = fechaActual(),
                                                    fecha_fin = fechaActual()
                                                ),
                                                timeSlamp = timeStampNumero(),
                                                horario_atencion = HorarioAtencion_box(
                                                    lunes = horario_atencion.lunes,
                                                    martes = horario_atencion.martes,
                                                    miércoles = horario_atencion.miércoles,
                                                    jueves = horario_atencion.jueves,
                                                    viernes = horario_atencion.viernes,
                                                    sábado = horario_atencion.sábado,
                                                    domingo = horario_atencion.domingo,
                                                ),
                                                lista_img = img_tienda()
                                            )

                                            repo.agraegar_datos_db_2(datos_enviar)

                                            if (foto_perfil_uri != null) {
                                                repo.subirLogoTienda(
                                                    context = context,
                                                    uri = foto_perfil_uri!!,
                                                    id_tienda = datos_enviar.id_tienda,
                                                    localidad = datos_enviar.localida_tienda,
                                                    onComplete = { exito ->
                                                        guardando_tienda = false
                                                        Toast.makeText(
                                                            context,
                                                            if (exito) "✅ Tienda guardada correctamente"
                                                            else "❌ Error al subir la imagen",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                )
                                            } else {
                                                guardando_tienda = false
                                                Toast.makeText(context, "✅ Tienda guardada correctamente", Toast.LENGTH_LONG).show()
                                            }

                                            val gson = GsonBuilder().setPrettyPrinting().create()
                                            Log.d("datos_enviamor", gson.toJson(datos_enviar))
                                        }
                                    }
                                }
                            },
                            enabled = !guardando_tienda,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (guardando_tienda) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                    texto_generico_one_line("Guardando...")
                                }
                            } else {
                                texto_generico_one_line("Enviar")
                            }
                        }

                        Button(
                            onClick = { cambiar_cat_sub = true },
                            shape = RoundedCornerShape(12.dp)
                        ) { texto_generico_one_line("Cambiar cat/sub") }
                    }
                }

                item {
                    ExpandDropDown(lista_notificaion_select, false, "Tipo de notificación", "Tipo de notificación") { tipo ->
                        tipo_notificacion_select = tipo
                    }
                }

                item {
                    Button(
                        onClick = {
                            val titulo = when (tipo_notificacion_select) {
                                "turistico" -> "🗺️ 🌄 Descubre nuevos lugares turísticos en Geinz 🌅"
                                "nuevos_negocios" -> "🚀 Nuevos lugares llegaron a Geinz ❤️"
                                "numeros_salud_seguridad" -> "🚨 Ante cualquier emergencia, comunícate con salud y seguridad 🚑 ❤️"
                                "tramites" -> "🧾 Encuentra lugares de servicios y comunidad "
                                else -> "📍 Nuevos lugares disponibles en Geinz"
                            }
                            val texto = when (tipo_notificacion_select) {
                                "turistico" -> "Explora destinos, atractivos y espacios únicos que se acaban de sumar. Encuentra tu próxima visita aquí 👀"
                                "nuevos_negocios" -> "Conoce los nuevos negocios que se unieron a Geinz y apoya a los emprendedores de tu zona 🏪✨"
                                "numeros_salud_seguridad" -> "Ten a la mano los contactos de emergencia, salud y seguridad cuando más los necesites ⛑️"
                                "tramites" -> "Ubica fácilmente dónde realizar trámites, servicios y gestiones cerca de ti 🏢"
                                else -> "Descubre nuevos lugares y servicios disponibles en Geinz 📍"
                            }
                            val imagen_url = when (tipo_notificacion_select) {
                                "turistico" -> "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/compartir_pantallas%2Fturistico_horizontal.webp?alt=media&token=aef7f5b9-a7e3-48bd-b419-8b0799d8a29b"
                                "nuevos_negocios" -> "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/compartir_pantallas%2Fsocios_horizontal.webp?alt=media&token=1e5e44be-5d56-4b0a-89f8-7f44e2532db1"
                                "numeros_salud_seguridad" -> "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/compartir_pantallas%2Fseguridad_horizontal.webp?alt=media&token=3d8c1853-6ad9-44ba-a0c7-092c2a1d8e49"
                                "tramites" -> "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/compartir_pantallas%2Fservicios_tramites_horizontal.webp?alt=media&token=7a203c44-405b-4527-842a-3cd87194fed8"
                                else -> "Descubre nuevos lugares y servicios disponibles en Geinz 📍"
                            }
                            val tipo_categoria_screen = when (tipo_notificacion_select) {
                                "turistico" -> "lgtr"
                                "nuevos_negocios" -> "nvng"
                                "numeros_salud_seguridad" -> "nemg"
                                "tramites" -> "seyt"
                                else -> "Descubre nuevos lugares y servicios disponibles en Geinz 📍"
                            }
                            scope.launch {
                                enviar_notificacion_lista_dispo(
                                    "", "", "", tipo_categoria_screen,
                                    tipo_notificacion_params = "screen",
                                    id_users = listOf("SEky161hPTf7SyjvfxNlkLRNd7f2"),
                                    titulo = titulo,
                                    txt = texto,
                                    logo_tienda = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
                                    tipo_notificacion = "Premium",
                                    url_img = imagen_url,
                                    prioridad = "high"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { texto_generico_one_line("Notificar a usuarios sobre apartados") }
                }

                item { spacer_vertical(32.dp) }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // TAB TURISMO
        // ════════════════════════════════════════════════════════════════
        if (modoActual == 1) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = !tocandoMapa
            ) {
                item { SectionHeader("🖼️ Foto principal") }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (foto_principal_uri != null) {
                                AsyncImage(
                                    model = foto_principal_uri,
                                    contentDescription = "Foto principal",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Landscape,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    foto_turismo_target = "principal"
                                    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    else Manifest.permission.READ_EXTERNAL_STORAGE
                                    if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED)
                                        launcherGaleria_turismo.launch("image/*")
                                    else permisoGaleria_turismo.launch(permiso)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("🖼️ Galería") }

                            Button(
                                onClick = {
                                    foto_turismo_target = "principal"
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        val uri = crearUriCamaraMediaStore(context)
                                        cameraUri_turismo = uri
                                        launcherCamara_turismo.launch(uri)
                                    } else permisoCamara_turismo.launch(Manifest.permission.CAMERA)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("📷 Cámara", color = Color.White) }
                        }
                    }
                }

                item { SectionHeader("📸 Galería del lugar") }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (lista_imgs_uri.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(lista_imgs_uri.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = lista_imgs_uri[index],
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        // ✅ Eliminar solo de lista_imgs_uri, sin base64
                                        IconButton(
                                            onClick = {
                                                lista_imgs_uri = lista_imgs_uri.toMutableList()
                                                    .also { it.removeAt(index) }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    foto_turismo_target = "lista"
                                    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    else Manifest.permission.READ_EXTERNAL_STORAGE
                                    if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED)
                                        launcherGaleria_turismo.launch("image/*")
                                    else permisoGaleria_turismo.launch(permiso)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("➕ Galería") }

                            Button(
                                onClick = {
                                    foto_turismo_target = "lista"
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        val uri = crearUriCamaraMediaStore(context)
                                        cameraUri_turismo = uri
                                        launcherCamara_turismo.launch(uri)
                                    } else permisoCamara_turismo.launch(Manifest.permission.CAMERA)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("➕ Cámara", color = Color.White) }
                        }
                    }
                }

                item { SectionHeader("📋 Información del lugar") }

                item {
                    OutlinedTextField(
                        value = titulo_turistico,
                        onValueChange = { titulo_turistico = it },
                        label = { texto_generico_one_line("Título del lugar") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            texto_generico_one_line(
                                "Ej: Cristo Redentor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        colors = camposColores()
                    )
                }

                item {
                    Text(
                        "Categorías del lugar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    chips_categorias(lista_categorias_turisticas) { lista ->
                        categorias_turisticas_select = lista
                    }
                }

                item {
                    OutlinedTextField(
                        value = descripcion_turistica,
                        onValueChange = { descripcion_turistica = it },
                        label = { texto_generico_one_line("Descripción") },
                        placeholder = {
                            texto_generico_one_line(
                                "Describe el lugar turístico...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = camposColores(),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 8,
                        singleLine = false
                    )
                }

                item { SectionHeader("📍 Ubicación") }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            obtenerUbicacionConfiable(fusedLocationClient) { latLng, _ ->
                                                lat_ = latLng.latitude; lng_ = latLng.longitude
                                                latitud = latLng.latitude.toString()
                                                longitud = latLng.longitude.toString()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("📡 GPS", color = Color.White, fontSize = 13.sp) }

                                Button(
                                    onClick = { mostrar_mapa = !mostrar_mapa },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (mostrar_mapa) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        if (mostrar_mapa) "🗺️ Ocultar mapa" else "🗺️ Abrir mapa",
                                        color = Color.White, fontSize = 13.sp
                                    )
                                }
                            }

                            AnimatedVisibility(visible = mostrar_mapa) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Toca el mapa para seleccionar la ubicación",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    ) {
                                        MapboxMapViewWithLocation(
                                            modifier = Modifier.fillMaxWidth(),
                                            onMapClick = { lat, lng ->
                                                lat_ = lat; lng_ = lng
                                                latitud = lat.toString()
                                                longitud = lng.toString()
                                            },
                                            onLocationUpdate = { lat, lng ->
                                                if (lat_ == 0.0 && lng_ == 0.0) {
                                                    lat_ = lat; lng_ = lng
                                                    latitud = lat.toString()
                                                    longitud = lng.toString()
                                                }
                                            },
                                            onTouchChange = { data -> tocandoMapa = data }
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.weight(1f),
                                    value = latitud, onValueChange = {},
                                    shape = RoundedCornerShape(16.dp),
                                    label = { texto_generico_one_line("Latitud") },
                                    colors = camposColores(), readOnly = true
                                )
                                OutlinedTextField(
                                    modifier = Modifier.weight(1f),
                                    value = longitud, onValueChange = {},
                                    shape = RoundedCornerShape(16.dp),
                                    label = { texto_generico_one_line("Longitud") },
                                    colors = camposColores(), readOnly = true
                                )
                            }

                            OutlinedTextField(
                                value = direccion, onValueChange = { direccion = it },
                                label = { texto_generico_one_line("Dirección") },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = camposColores()
                            )

                            if (mostar_geo) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Geohash:", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    valor_geohashin = constantes_lista_localidades.geohashing(lat_, lng_)
                                    Text(valor_geohashin, style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item { SectionHeader("⚙️ Acciones") }

                item {
                    Button(
                        onClick = {
                            if (!guardando_turismo) {
                                guardando_turismo = true
                                val id_generado = generarIdSeguro()

                                val datos_turismo = data_class_turismo(
                                    titulo = titulo_turistico,
                                    descripcion = descripcion_turistica,
                                    categoria = categorias_turisticas_select,
                                    geohash = valor_geohashin,
                                    id = id_generado,
                                    img = img_turismo(principal = "", lista_img = emptyList()),
                                    ubicacion = ubicacion_turismo(
                                        direccion = direccion,
                                        latitud = lat_,
                                        longitud = lng_
                                    )
                                )

                                val onResult: (Boolean) -> Unit = { exito ->
                                    guardando_turismo = false
                                    Toast.makeText(
                                        context,
                                        if (exito) "✅ Lugar turístico guardado correctamente"
                                        else "❌ Error al guardar, intenta de nuevo",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                // ✅ Pasa URIs directamente al repo, que convierte internamente
                                if (foto_principal_uri != null) {
                                    repo_agregar_datos.subirImagenesTuristico(
                                        context = context,
                                        uriFotoPrincipal = foto_principal_uri!!,
                                        listaUrisExtra = lista_imgs_uri,
                                        id_lugar = id_generado,
                                        localidad = "barranca",
                                        datosTurismo = datos_turismo,
                                        onComplete = onResult
                                    )
                                } else {
                                    repo_agregar_datos.guardarTuristicoEnFirestore(
                                        db = FirebaseFirestore.getInstance(),
                                        id_lugar = id_generado,
                                        localidad = "barranca",
                                        datos = datos_turismo,
                                        urlPrincipal = "",
                                        urlsExtra = emptyList(),
                                        onComplete = onResult
                                    )
                                }
                            }
                        },
                        enabled = !guardando_turismo,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (guardando_turismo) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                texto_generico_one_line("Guardando...")
                            }
                        } else {
                            texto_generico_one_line("💾 Guardar lugar turístico")
                        }
                    }
                }

                item { spacer_vertical(32.dp) }
            }
        }
    }
}

// ─── Composable: Mapa Mapbox ───────────────────────────────────────────────


// ─── Composable auxiliar: encabezado de sección ────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

// ─── Helper colores de campos ──────────────────────────────────────────────

@Composable
fun camposColores() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedPlaceholderColor = Color.Gray,
    unfocusedPlaceholderColor = Color.Gray,
)

// ─── Horario semanal (sin cambios) ─────────────────────────────────────────

@Composable
fun HorarioSemanal(viewmodel_agregar_datos: viewmodel_agregar_datos) {
    val context = LocalContext.current
    val dias = listOf(
        "Lunes", "Martes", "Miércoles",
        "Jueves", "Viernes", "Sábado", "Domingo"
    )
    val mapaHoras = viewmodel_agregar_datos.mapaHoras

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dias.forEach { dia ->
            val item = mapaHoras[dia]!!

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            texto_generico_one_line(dia)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (item.cerrado.value) "Abierto" else "Cerrado")
                            spacer_horizonta(5.dp)
                            Switch(
                                checked = item.cerrado.value,
                                onCheckedChange = { value ->
                                    item.cerrado.value = value
                                    if (!value) {
                                        item.h1AM.value = ""
                                        item.h2AM.value = ""
                                        item.h1PM.value = ""
                                        item.h2PM.value = ""
                                        item.solo_horario.value = false
                                    }
                                }
                            )
                        }
                    }

                    AnimatedVisibility(item.cerrado.value) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item.solo_horario.value,
                                    onCheckedChange = { item.solo_horario.value = it }
                                )
                                Text(
                                    if (item.solo_horario.value) "Trabajo de corrido"
                                    else "Trabajo con descanso"
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                campoHora(
                                    valor = item.h1AM.value,
                                    etiqueta = "Apertura AM",
                                    onHoraSeleccionada = { item.h1AM.value = it },
                                    abrirTimePicker = { valorActual, onSelect ->
                                        abrirTimePicker(context, valorActual, onSelect)
                                    }
                                )
                                texto_generico_one_line(" a ")
                                campoHora(
                                    valor = item.h2AM.value,
                                    etiqueta = "Cierre AM",
                                    onHoraSeleccionada = { item.h2AM.value = it },
                                    abrirTimePicker = { valorActual, onSelect ->
                                        abrirTimePicker(context, valorActual, onSelect)
                                    }
                                )
                            }

                            AnimatedVisibility(!item.solo_horario.value) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    campoHora(
                                        valor = item.h1PM.value,
                                        etiqueta = "Apertura PM",
                                        onHoraSeleccionada = { item.h1PM.value = it },
                                        abrirTimePicker = { valorActual, onSelect ->
                                            abrirTimePicker(context, valorActual, onSelect)
                                        }
                                    )
                                    texto_generico_one_line(" a ")
                                    campoHora(
                                        valor = item.h2PM.value,
                                        etiqueta = "Cierre PM",
                                        onHoraSeleccionada = { item.h2PM.value = it },
                                        abrirTimePicker = { valorActual, onSelect ->
                                            abrirTimePicker(context, valorActual, onSelect)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── campoHora (sin cambios) ───────────────────────────────────────────────

@Composable
fun RowScope.campoHora(
    valor: String,
    etiqueta: String,
    onHoraSeleccionada: (String) -> Unit,
    abrirTimePicker: (String, (String) -> Unit) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                abrirTimePicker(valor, onHoraSeleccionada)
            }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { texto_generico_one_line(etiqueta) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─── valor_txt_contacto (sin cambios + SIP) ────────────────────────────────

@Composable
fun valor_txt_contacto(
    tipo: String,
    valor: String,
    valor_retorno: (String) -> Unit
) {
    val txt = remember(tipo) {
        when (tipo.lowercase()) {
            "whatsapp", "telefono", "yape", "plin", "sip" -> "Número de $tipo"
            "sitio web" -> "Nombre del sitio web"
            else -> "Usuario de $tipo"
        }
    }

    val keyboardType = remember(tipo) {
        if (tipo.equals("whatsapp", ignoreCase = true) ||
            tipo.equals("telefono", ignoreCase = true) ||
            tipo.equals("yape", ignoreCase = true) ||
            tipo.equals("plin", ignoreCase = true) ||
            tipo.equals("sip", ignoreCase = true)         // ← NUEVO SIP
        ) KeyboardType.Phone
        else KeyboardType.Text
    }

    OutlinedTextField(
        value = valor,
        onValueChange = { valor_retorno(it) },
        label = { texto_generico_one_line(txt) },
        placeholder = {
            texto_generico_one_line(txt, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = camposColores()
    )
}

// ─── ChipsCategoriasCheck (sin cambios) ───────────────────────────────────

@Composable
fun ChipsCategoriasCheck(
    lista: List<String>,
    lista_select: (List<String>) -> Unit
) {
    var seleccionados by rememberSaveable { mutableStateOf(listOf<String>()) }
    spacer_vertical(5.dp)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista) { item ->
            val isSelected = item in seleccionados
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        seleccionados = if (isSelected) seleccionados - item else seleccionados + item
                        lista_select(seleccionados)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            seleccionados = if (isSelected) seleccionados - item else seleccionados + item
                            lista_select(seleccionados)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = Color.White,
                            checkmarkColor = Color.White
                        )
                    )
                    texto_generico_one_line(
                        texto = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
    spacer_vertical(3.dp)
}

// ─── chips_categorias (sin cambios) ───────────────────────────────────────

@Composable
fun chips_categorias(
    lista: List<String>,
    lista_select: (List<String>) -> Unit
) {
    var seleccionados by rememberSaveable { mutableStateOf(listOf<String>()) }
    spacer_vertical(10.dp)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista) { item ->
            val isSelected = seleccionados.contains(item)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        seleccionados = if (isSelected) seleccionados - item else seleccionados + item
                        lista_select(seleccionados)
                    },
                contentAlignment = Alignment.Center
            ) {
                texto_generico_one_line(
                    item,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) Color.Black else Color.White
                )
            }
        }
    }
    spacer_vertical(10.dp)
}

// ─── chips_categoriasconvalor_inicial (sin cambios) ───────────────────────

@Composable
fun chips_categoriasconvalor_inicial(
    lista: List<String>,
    valorInicial: List<String>,
    lista_select: (List<String>) -> Unit
) {
    var seleccionados by rememberSaveable { mutableStateOf(valorInicial) }
    LaunchedEffect(valorInicial) { seleccionados = valorInicial }
    spacer_vertical(10.dp)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista) { item ->
            val isSelected = seleccionados.contains(item)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        seleccionados = if (isSelected) seleccionados - item else seleccionados + item
                        lista_select(seleccionados)
                    },
                contentAlignment = Alignment.Center
            ) {
                texto_generico_one_line(
                    item,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) Color.Black else Color.White
                )
            }
        }
    }
    spacer_vertical(10.dp)
}

// ─── generarPromptOptimizado (sin cambios) ────────────────────────────────

fun generarPromptOptimizado(
    nombre: String,
    categoria: String,
    intentos: Int,
    subcategoria: String
): String {
    val reglasDeFormato =
        "Breve, no más de 6 líneas, sin puntos ni saltos de línea. Usa solo emojis inspiradores (✨🌟💡⚡📦🏆) y NUNCA uses corazones."
    val contexto = if (subcategoria.isNotBlank()) {
        "Tienda '$nombre', Categoría '$categoria', Subcategoría '$subcategoria'."
    } else {
        "Tienda '$nombre', Categoría '$categoria'."
    }
    val tonoInstruccion = when (intentos) {
        1 -> "Tono: Cálido, elegante, convincente. Debe 'enamorar', transmitir confianza y explicar la oferta."
        2 -> "Tono: Creativo, moderno, fluido, atractivo. Muestra lo que hace la tienda de forma cautivadora."
        3 -> "Tono: Emocional, profundo, profesional, memorable. Debe transmitir cercanía y destacar la esencia de la tienda."
        else -> "Tono: Artístico, poético, motivador, único y auténtico. Transmite encanto y diferenciación."
    }
    return """
        Instrucción: Genera una descripción inspiradora.
        Reglas: $reglasDeFormato
        Contexto: $contexto
        Tono: $tonoInstruccion
        Salida: SOLO el texto final.
    """.trimIndent()
}

// ─── obtenerUbicacionConfiable (sin cambios) ──────────────────────────────

@SuppressLint("MissingPermission")
fun obtenerUbicacionConfiable(
    fusedLocationClient: FusedLocationProviderClient,
    onUbicacionObtenida: (LatLng, Float) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onUbicacionObtenida(
                LatLng(location.latitude, location.longitude),
                location.accuracy
            )
        }
    }

    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500)
        .setMinUpdateDistanceMeters(0f)
        .setMaxUpdates(3)
        .build()

    var mejorLocation: Location? = null

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (loc in result.locations) {
                if (mejorLocation == null || loc.accuracy < mejorLocation!!.accuracy) {
                    mejorLocation = loc
                }
            }
            mejorLocation?.let {
                onUbicacionObtenida(LatLng(it.latitude, it.longitude), it.accuracy)
                if (it.accuracy <= 10f) fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
}

// ─── HorasDia (sin cambios) ───────────────────────────────────────────────

data class HorasDia(
    var h1AM: MutableState<String> = mutableStateOf(""),
    var h2AM: MutableState<String> = mutableStateOf(""),
    var h1PM: MutableState<String> = mutableStateOf(""),
    var h2PM: MutableState<String> = mutableStateOf(""),
    var cerrado: MutableState<Boolean> = mutableStateOf(false),
    var solo_horario: MutableState<Boolean> = mutableStateOf(false)
)




// ─── Composable principal del mapa con permisos, ubicación y toque ────────

@OptIn(MapboxExperimental::class)
@Composable
fun MapboxMapViewWithLocation(
    modifier: Modifier = Modifier,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    onLocationUpdate: (lat: Double, lng: Double) -> Unit = { _, _ -> },
    onTouchChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estado de permisos
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!hasLocationPermission) {
            Toast.makeText(context, "Permiso de ubicación necesario", Toast.LENGTH_LONG).show()
        }
    }

    if (!hasLocationPermission) {
        Box(
            modifier = modifier.fillMaxWidth().height(280.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }) {
                Text("Activar ubicación")
            }
        }
        return
    }

    // Estado del mapa y anotaciones
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    // 📍 Almacenar la última ubicación conocida del usuario (para el botón de recentrar)
    var userLocation by remember { mutableStateOf<Point?>(null) }

    Box(modifier = modifier) {
        // ──────────────────────────────────────────────────────────────
        // Mapa
        // ──────────────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            onTouchChange(event.changes.any { it.pressed })
                        }
                    }
                },
            factory = { ctx ->
                MapView(ctx).apply {
                    mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->

                        // 1️⃣ Configurar ubicación (puck)
                        val locationComponent = this.location
                        locationComponent.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                            puckBearingEnabled = true
                            puckBearing = PuckBearing.HEADING
                            locationPuck = createDefault2DPuck(withBearing = true)
                        }

                        // 2️⃣ Centrar en ubicación actual si está disponible
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val punto = Point.fromLngLat(it.longitude, it.latitude)
                                    userLocation = punto   // guardar para recentrar
                                    mapboxMap.setCamera(
                                        CameraOptions.Builder()
                                            .center(punto)
                                            .zoom(15.0)
                                            .build()
                                    )
                                    onLocationUpdate(it.latitude, it.longitude)
                                }
                            }
                        }

                        // 3️⃣ Configurar gestor de anotaciones
                        annotationManager = annotations.createPointAnnotationManager()

                        // 4️⃣ Agregar imagen de pin personalizada (usando Color de Compose)
                        val bitmap = crearBitmapPin(context, Color(0xFFFF5722))
                        style.addImage("pin_icon", bitmap)

                        // 5️⃣ Click en el mapa: colocar pin y actualizar coordenadas
                        mapboxMap.addOnMapClickListener { point ->
                            val lat = point.latitude()
                            val lng = point.longitude()

                            // Eliminar pin anterior
                            annotationManager?.deleteAll()

                            // Crear nuevo pin
                            annotationManager?.create(
                                PointAnnotationOptions()
                                    .withPoint(point)
                                    .withIconImage("pin_icon")
                                    .withIconAnchor(IconAnchor.BOTTOM)
                                    .withIconSize(1.2)
                            )

                            // Notificar al exterior
                            onMapClick(lat, lng)

                            true
                        }
                    }
                    mapViewRef = this
                }
            },
            update = { /* no es necesario actualizar nada aquí */ }
        )

        // ──────────────────────────────────────────────────────────────
        // Botón flotante de recentrar (esquina inferior derecha)
        // ──────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                val punto = userLocation
                if (punto != null) {
                    mapViewRef?.mapboxMap?.easeTo(
                        CameraOptions.Builder()
                            .center(punto)
                            .zoom(15.0)
                            .build()
                    )
                } else {
                    // Si aún no tenemos ubicación, intentamos obtenerla ahora
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val newPoint = Point.fromLngLat(it.longitude, it.latitude)
                                userLocation = newPoint
                                mapViewRef?.mapboxMap?.easeTo(
                                    CameraOptions.Builder()
                                        .center(newPoint)
                                        .zoom(15.0)
                                        .build()
                                )
                                onLocationUpdate(it.latitude, it.longitude)
                            } ?: run {
                                Toast.makeText(context, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar en mi ubicación"
            )
        }
    }

    // Opcional: actualizar ubicación en tiempo real (para mantener userLocation actualizado)
    DisposableEffect(Unit) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    userLocation = Point.fromLngLat(it.longitude, it.latitude)
                    onLocationUpdate(it.latitude, it.longitude)
                }
            }
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        if (hasLocationPermission) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}


fun crearBitmapPin(context: Context, color: Color = Color.Red): Bitmap {
    val colorInt = color.toArgb()   // Convierte Color de Compose a Int

    val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = colorInt
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(40f, 40f, 38f, paint)

    // Borde blanco usando Color.White de Compose
    val borderPaint = Paint().apply {
        this.color = Color.White.toArgb()   // Convertido a Int
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawCircle(40f, 40f, 35f, borderPaint)

    return bitmap
}
fun uriToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()
    val salida = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, salida)
    return Base64.encodeToString(salida.toByteArray(), Base64.DEFAULT)
}

// Reemplaza crearUriCamara con esto
fun crearUriCamaraMediaStore(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "foto_tmp_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GeinzTemp")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("No se pudo crear URI para cámara")
}
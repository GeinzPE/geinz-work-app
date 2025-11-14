package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import Item
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.widget.Toast
import androidx.palette.graphics.Palette
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.botom_shet_turismobtn
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_numero
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_red
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_Dia
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.onboarding.dataclass_onboarding
import com.geinzz.geinzwork.data.model.localizate_geinz.onboarding.dataclass_pantalla1
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.REQUEST_CALL_PHONE
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_lugares_turisticos
import com.geinzz.geinzwork.utils.localizate_geinz.abrirRutaEnGoogleMaps
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.os.Looper
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_metodo_individual
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch


object constantes_lista_localidades {
    val lista = listOf(
        dataclass_localidad_escudos("Barranca".lowercase(), R.drawable.escudo_barranca),
        dataclass_localidad_escudos("Paramonga".lowercase(), R.drawable.escudo_paramonga),
        dataclass_localidad_escudos("Supe".lowercase(), R.drawable.escudo_supe),
        dataclass_localidad_escudos("Pativilca".lowercase(), R.drawable.escudo_pativilca)
    )
    val lista_turismo_bottom_sheet = listOf(
        botom_shet_turismobtn("Ir al lugar", Icons.Filled.Place, true),
        botom_shet_turismobtn("ver en mapa", Icons.Filled.Map, false),
        botom_shet_turismobtn("compartir", Icons.Filled.Share, false)
    )
    val cat_sub_seguirar_salud = listOf("seguridad", "salud")
    val categorias_defaul = listOf(
        "comida y restaurantes",
        "bancos y servicios financieros",
        "belleza",
        "deporte y bienestar",
        "educacion y librerias",
        "entretenimiento y recreacion",
        "grifos y estaciones",
        "hogar",
        "hogar y ferreteria",
        "hospedaje y entretenimiento nocturno",
        "jardineria y plantas",
        "lavanderias y tintorerias",
        "mascotas y animales",
        "mecanica y autoservicios",
        "minimarkets y bodegas",
        "moda y estilo",
        "salud y farmacias",
        "servicios de encomienda y envios",
        "servicios tecnicos y reparaciones",
        "tecnologia y electronica",
        "transporte y terminales",
        "turismo"
    )

    val dias_sema =
        listOf(
            "lunes", "martes", "miércoles",
            "jueves", "viernes", "sábado", "domingo"
        )

    val lista_localidad = listOf("Barranca", "Supe", "paramonga", "pativilca", "Puerto supe")

    val lista_fraces_filtado = listOf(
        "¡Qué bueno verte por aquí!",
        "¿Qué deseas buscar hoy?",
        "Encuentra lo que necesitas",
        "Fácil",
        "Rápido",
        "Preciso"
    )

    fun obtenerMetodoContacto(
        metodo: String,
        data: Map<String, Any>
    ): Pair<Boolean, String> {
        val metodoData = data[metodo] as? Map<String, Any> ?: emptyMap()
        val estado = metodoData["estado"] as? Boolean ?: false
        val nombre =
            metodoData["nombre_buscador"] as? String ?: metodoData["numero"] as? String ?: ""
        return estado to nombre
    }

    fun ocultarNumero(numero: String): String {
        return if (numero.length >= 9) {
            val primerosTres = numero.take(3)
            val ocultos = "*".repeat(numero.length - 3)
            "$primerosTres$ocultos"
        } else {
            numero
        }
    }

    fun getCategoriaIcon(categoria: String): String {
        return when (categoria.lowercase()) {
            "bancos y servicios financieros" -> "🏦"
            "belleza" -> "\uD83D\uDC88"
            "comida y restaurantes" -> "🍽️"
            "deporte y bienestar" -> "🏋️"
            "educacion y librerias" -> "📚"
            "entretenimiento y recreacion" -> "🎭"
            "grifos y estaciones" -> "⛽"
            "hogar y ferreteria" -> "🛠️"
            "hospedaje y entretenimiento nocturno" -> "🏨"
            "jardineria y plantas" -> "🌱"
            "lavanderias y tintorerias" -> "👕"
            "mascotas y animales" -> "🐾"
            "mecanica y autoservicios" -> "🔧"
            "minimarkets y bodegas" -> "🛒"
            "moda y estilo" -> "\uD83D\uDC55"
            "salud y farmacias" -> "💊"
            "servicios de encomienda y envios" -> "📦"
            "servicios tecnicos y reparaciones" -> "🔌"
            "supermercados y tiendas grandes" -> "🏬"
            "tecnologia y electronica" -> "💻"
            "transporte y terminales" -> "🚌"
            "hogar" -> "🏨"
            "turismo"->"\uD83C\uDDF5\uD83C\uDDEA"
            else -> "🏷️" // genérico
        }
    }


    fun verificarSiEstaAbierto(lista_horarios_por_tienda: List<horario_Dia>): Boolean {
        return try {
            val diaActualConTilde =
                SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).lowercase()
            val diaActual = diaActualConTilde
            Log.d("HORARIO_CHECK", "Día actual: $diaActual")

            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val ahora = formato.parse(formato.format(Date())) ?: return false
            Log.d("HORARIO_CHECK", "Hora actual: ${formato.format(ahora)}")

            lista_horarios_por_tienda.forEach { i ->
                val dia = i.dia!!.lowercase()
                Log.d("HORARIO_CHECK", "Evaluando día: $dia")
                if (dia == diaActual) {
                    val apertura = formato.parse(i.h_apertura)
                    val cierre = formato.parse(i.h_cierre)
                    Log.d(
                        "HORARIO_CHECK",
                        "Horario -> Apertura: ${i.h_apertura}, Cierre: ${i.h_cierre}"
                    )

                    if (apertura == null || cierre == null) {
                        Log.w("HORARIO_CHECK", "Horario inválido, se omite este día.")
                        return@forEach
                    }

                    val estaAbierto = if (cierre.after(apertura)) {
                        // Ejemplo: 08:00 - 18:00
                        ahora in apertura..cierre
                    } else {
                        // Ejemplo: 22:00 - 02:00 (día siguiente)
                        ahora.after(apertura) || ahora.before(cierre)
                    }

                    Log.d("HORARIO_CHECK", "¿Está abierto hoy? $estaAbierto")

                    if (estaAbierto) return true
                }
            }

            false // Ningún horario coincide y está activo
        } catch (e: Exception) {
            Log.e("verificarSiEstaAbierto", "Error al verificar horario", e)
            false
        }
    }

    fun verificarSiEstaAbiertoHoy(horarioHoy: horario_tienda): Boolean {
        return try {
            if (horarioHoy == null) {
                Log.w("HORARIO_CHECK", "Horario recibido es NULL")
                return false
            }

            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val ahora = formato.parse(formato.format(Date())) ?: return false

            Log.d(
                "HORARIO_CHECK",
                "Horario recibido -> Apertura: ${horarioHoy.h_apertura}, Cierre: ${horarioHoy.h_cierre}"
            )
            Log.d("HORARIO_CHECK", "Hora actual: ${formato.format(ahora)}")

            val apertura = formato.parse(horarioHoy.h_apertura)
            val cierre = formato.parse(horarioHoy.h_cierre)

            if (apertura == null || cierre == null) {
                Log.w("HORARIO_CHECK", "Horario inválido (apertura o cierre nulos)")
                return false
            }

            val estaAbierto = if (cierre.after(apertura)) {
                // Ejemplo normal: 08:00 - 18:00
                ahora in apertura..cierre
            } else {
                // Ejemplo cruce de medianoche: 22:00 - 02:00
                ahora.after(apertura) || ahora.before(cierre)
            }

            Log.d("HORARIO_CHECK", "¿Está abierto hoy? $estaAbierto")
            estaAbierto
        } catch (e: Exception) {
            Log.e("HORARIO_CHECK", "Error al verificar horario", e)
            false
        }
    }


    fun quitarTildes(texto: String): String {
        val normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    fun abrir_google_maps(
        context: Context,
        latitud: Double,
        longitud: Double,
        mostrar_dialog: (Boolean) -> Unit
    ) {
        Log.d("lateitudes", "${latitud} ${longitud}")
        if (verificarUbiActiva(context)) {
            abrirRutaEnGoogleMaps(context, latitud, longitud)
        } else {
            mostrar_dialog(true)
        }
    }

    @Composable
    fun ZoomIconButton(mostrarDialogozoom: () -> Unit) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { mostrarDialogozoom() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.verctor_zoom_white),
                contentDescription = "Ocultar",
                modifier = Modifier.size(18.dp)
            )
        }
    }


    fun cambiar_icono_exapndible(expandido: Boolean): Int {

        return if (expandido) {
            R.drawable.ocultar_abajo
        } else {
            R.drawable.ocultar_arriva
        }
    }

    val lista_img_local = listOf(
        R.drawable.f1,
        R.drawable.f2,
        R.drawable.f4,
        R.drawable.f5,
        R.drawable.f6,
        R.drawable.f7,
        R.drawable.f8
    )


    val lista_img_localidades_local = listOf(
        R.drawable.f2,
        R.drawable.f4,
        R.drawable.f5,
        R.drawable.f7,
        R.drawable.f8,
    )
    val lista_img_localidades_nombre = listOf(
        dataclass_onboarding(R.drawable.f2, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f4, "Puerto ", "Playa de puerto supe"),
        dataclass_onboarding(R.drawable.f5, "pativilca ", "Paza de armas de pativilca"),
        dataclass_onboarding(R.drawable.f7, "Supe ", "Casa de las brujas supe"),
        dataclass_onboarding(R.drawable.f8, "Paramonga ", "Plaza de armas de paramonga"),
    )

    val lista_img_carga = listOf(
        dataclass_onboarding(R.drawable.f1, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f2, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f3, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f4, "Puerto ", "Playa de puerto supe"),
        dataclass_onboarding(R.drawable.f5, "pativilca ", "Paza de armas de pativilca"),
        dataclass_onboarding(R.drawable.f7, "Supe ", "Casa de las brujas supe"),
        dataclass_onboarding(R.drawable.f8, "Paramonga ", "Plaza de armas de paramonga"),
    )

    val lista_frases_busqueda = listOf(
        "¿Qué buscas?",
        "Descubre algo",
        "Explora tu ciudad",
        "¿Qué descubrir?",
        "Busca tiendas",
        "Todo en un lugar",
        "¿Qué explorar?"
    )


    val lista_fraces_favoritos = listOf(
        "Solo para ti",
        "Aquí empieza lo tuyo",
        "Tu selección perfecta", "Lo que amas aquí",
        "Tu lista, tu mundo", "Tu espacio especial", "Tu toque personal"
    )

    val lista_fitlrado_servicios_basicos = listOf(
        "Todos",
        "agua",
        "gas",
        "luz",
        "cable",
        "agua de mesa",
        "internet",
        "telefonia movil",
        "tramites"
    )

    val lista_color_degradado_bottom = listOf(
        Color.Black.copy(alpha = 1f),   // arriba: oscuro
        Color.Black.copy(alpha = 0.85f), // muy oscuro
        Color.Black.copy(alpha = 0.6f),  // intermedio
        Color.Black.copy(alpha = 0.3f),// casi transparente
        Color.Transparent
    )
    val lista_color_degradado_top = listOf(
        Color.Transparent,
        // inicio
        Color.Black.copy(alpha = 0.2f),        // un poco oscuro
        Color.Black.copy(alpha = 0.5f),        // intermedio
        Color.Black.copy(alpha = 0.85f),       // ya casi negro
        Color.Black.copy(alpha = 0.95f),       // más negro aún
        Color.Black.copy(alpha = 1f)
    )

    val frasesCarga = listOf(
        "Bienvenido, tu espacio te espera.",
        "Relájate, estamos preparando todo para ti.",
        "Cada lugar tiene su historia, la tuya empieza aquí.",
        "Explora y disfruta de lo que tu ciudad ofrece.",
        "Tu tiempo es valioso, lo cuidamos por ti.",
        "Descubre algo nuevo en cada esquina.",
        "Pequeños momentos, grandes experiencias.",
        "Cerca de ti, todo lo que necesitas.",
        "Bienvenido de vuelta, te hemos extrañado.",
        "Tu próxima aventura comienza aquí.",
        "Calles, luces y lugares para descubrir.",
        "Tu ciudad tiene secretos, vamos a encontrarlos.",
        "Cada paso cuenta, explora sin prisa.",
        "Lo que buscas, lo encuentras cerca.",
        "Entre tiendas y locales, todo tiene su encanto.",
        "Sabores, colores y momentos por descubrir.",
        "Aquí, tu rutina se convierte en experiencia.",
        "Cada esquina guarda algo especial para ti.",
        "Siente la ciudad, vive cada momento.",
        "La aventura urbana empieza en tu pantalla."
    )


    val lista_frases_login = listOf(
        "Bienvenido a Geinz, tu espacio ideal",
        "Explora y descubre lo que te gusta",
        "Encuentra tu próximo destino favorito",
        "Tu aventura con Geinz comienza hoy",
        "Momentos únicos, solo con Geinz",
        "Todo lo que buscas, en un solo lugar",
        "Haz que cada día cuente con Geinz",
        "Descubre nuevas experiencias con Geinz"
    )


    val lista_fraces_inicio = listOf(
        "¿Listo para empezar?",
        "¿Qué planes tienes?",
        "¿A dónde quieres ir?",
    )

    val fracespantalla1 = listOf(
        dataclass_pantalla1(
            "Tu camino más fácil",
            "Encuentra rápido las tiendas y servicios que necesitas cerca de ti. Todo en un solo lugar, para que tu día sea más simple.",
            R.drawable.p1_1
        ),
        dataclass_pantalla1(
            "Explora tu zona",
            "Descubre restaurantes, tiendas y servicios en tu ciudad. Aprovecha promociones exclusivas y conoce lo que tienes alrededor.",
            R.drawable.p1_2
        ),
        dataclass_pantalla1(
            "Rutas rápidas",
            "Sigue rutas directas y seguras para llegar más rápido a tu destino. Encuentra siempre lo que buscas sin complicaciones.",
            R.drawable.p1_3
        )
    )


    val fracespantalla11 = dataclass_pantalla1(
        "Tu camino más fácil",
        "Encuentra rápido las tiendas y servicios que necesitas cerca de ti. Todo en un solo lugar, para que tu día sea más simple.",
        R.drawable.f4


    )

    @Composable
    fun FuenteControladaApp_bottom_sheet_dialog(content: @Composable () -> Unit) {
        val currentDensity = LocalDensity.current

        CompositionLocalProvider(
            LocalDensity provides Density(
                currentDensity.density,
                fontScale = 1f // 👈 Bloquea el cambio del tamaño de texto
            )
        ) {
            content()
        }
    }


    @Composable
    fun FuenteControladaApp(content: @Composable () -> Unit) {
        val currentDensity = LocalDensity.current

        CompositionLocalProvider(
            LocalDensity provides Density(
                currentDensity.density,
                currentDensity.fontScale.coerceIn(0.85f, 1.1f)
            )
        ) {
            content()
        }
    }


//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun CarouselExample_MultiBrowse() {
//        data class CarouselItem(
//            val id: Int,
//            @DrawableRes val imageResId: Int,
//            val contentDescription: String
//        )
//
//        val items = remember {
////            listOf(
////                CarouselItem(0, R.drawable.cupcake, "cupcake"),
////                CarouselItem(1, R.drawable.donut, "donut"),
////                CarouselItem(2, R.drawable.eclair, "eclair"),
////                CarouselItem(3, R.drawable.froyo, "froyo"),
////                CarouselItem(4, R.drawable.gingerbread, "gingerbread"),
////            )
//        }
//
//        HorizontalMultiBrowseCarousel(
//            state = rememberCarouselState { items.count() },
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentHeight()
//                .padding(top = 16.dp, bottom = 16.dp),
//            preferredItemWidth = 186.dp,
//            itemSpacing = 8.dp,
//            contentPadding = PaddingValues(horizontal = 16.dp)
//        ) { i ->
//            val item = items[i]
//            Image(
//                modifier = Modifier
//                    .height(205.dp)
//                    .maskClip(MaterialTheme.shapes.extraLarge),
//                painter = painterResource(id = item.imageResId),
//                contentDescription = item.contentDescription,
//                contentScale = ContentScale.Crop
//            )
//        }
//    }

    fun saludo_user_principal(nombre: String): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (saludo, emojiExtra) = when (hora) {
            in 6..11 -> "Hola, buenos días" to "🌞"
            in 12..18 -> "Hola, buenas tardes" to "\uD83C\uDF24\uFE0F"
            else -> "Hola, buenas noches" to "🌙"
        }

        return "$saludo $nombre $emojiExtra"
    }


    fun esGmailValido(correo: String): Boolean {
        val regex = Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$")
        return regex.matches(correo)
    }

    fun convertirHora24a12(hora24: String): String {
        val formato24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formato12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = formato24.parse(hora24)
        return formato12.format(date!!)
    }

    fun estaDentroDeTienda(
        userLat: Double,
        userLng: Double,
        tiendaLat: Double,
        tiendaLng: Double,
        radioMetros: Float = 50f
    ): Pair<Float, Boolean> {
        val userLocation = Location("").apply {
            latitude = userLat
            longitude = userLng
        }

        val tiendaLocation = Location("").apply {
            latitude = tiendaLat
            longitude = tiendaLng
        }

        val distancia = userLocation.distanceTo(tiendaLocation) // en metros
        val dentro = distancia <= radioMetros

        Log.d("validacion_tienda", "$userLat $userLng $tiendaLat $tiendaLng")
        if (!dentro) {
            Log.d(
                "validacion_tienda",
                "Fuera del rango: distancia real = ${"%.2f".format(distancia)} m (radio = $radioMetros m)"
            )
        } else {
            Log.d(
                "validacion_tienda",
                "Dentro del rango: distancia real = ${"%.2f".format(distancia)} m"
            )
        }

        return Pair(distancia, dentro)
    }

    val lista_img_seguridad = listOf(
        R.drawable.barranca_comisaria,
        R.drawable.bomberos_brca,
        R.drawable.samu_brca,
        R.drawable.bomberos_brca, R.drawable.supe_brca,
        R.drawable.hospital_brca
    )

    fun guarar_token_user(user: String, token: String) {

        val marca = Build.MANUFACTURER
        val modelo = Build.MODEL
        val nombreDispositivo = "$marca-$modelo"
        val ref =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("users").collection("tokens")
                .document(user)

        val hashMap = hashMapOf<String, Any>(
            nombreDispositivo to token
        )
        val toknes = hashMapOf<String, Any>(
            "tokens" to hashMap
        )

        ref.set(toknes, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FCM1231312", "Token guardado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("FCM1231312", "Error guardando token", e)
            }
    }

    fun eliminarTokenDispositivo(user: String) {
        val marca = Build.MANUFACTURER
        val modelo = Build.MODEL
        val nombreDispositivo = "$marca-$modelo"

        val ref = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("tokens")
            .document(user)

        // Borramos solo la clave correspondiente a este dispositivo
        val updates = hashMapOf<String, Any>(
            "tokens.$nombreDispositivo" to FieldValue.delete()
        )

        ref.update(updates)
            .addOnSuccessListener {
                Log.d("FCM1231312", "Token del dispositivo eliminado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("FCM1231312", "Error eliminando token del dispositivo", e)
            }
    }

    data class data_redes_tiendas(
        val enable: Boolean,
        val icono: Int,
        val nombre_red: String,
        val valor: String
    )

    //    val lista_redes_tiendas = listOf(
//        data_redes_tiendas(icono = R.drawable.llamada_icon, "llamar"),
//        data_redes_tiendas(icono = R.drawable.whatsapp_icon, "whatsapp"),
//        data_redes_tiendas(icono = R.drawable.tik_tok_icon, "tiktok"),
//        data_redes_tiendas(icono = R.drawable.facebook_icon, "facebook"),
//        data_redes_tiendas(icono = R.drawable.instagram_icon, "instragram"),
//        data_redes_tiendas(icono =R.drawable.sitio_web,"web")
//        )
    data class ContactoItem(
        val nombre_red: String,
        val icono: Int,
        val valor: String
    )


    fun bitmapDescriptorFromDrawable(
        context: Context,
        resId: Int,
        width: Int,
        height: Int
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, resId)!!
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


//    val lista_constantes = listOf(
//        // Barranca
//        seguridad_salud_publica(
//            nombre = "Hospital de Barranca",
//            tipo = "salud",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.753746104394681,
//                longitud = -77.76360404577927,
//                referencia = "Frente a la Plaza de Armas",
//                direccion = "Av. Nicolás de Piérola 210–224"
//            ),
//            numero_contacto = listOf("012352241", "012352075", "012352156")
//        ),
//
//        seguridad_salud_publica(
//            nombre = "DIPINCRI Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.741253339348441,
//                longitud = -77.76520851349262,
//                referencia = "Frente a la Municipalidad",
//                direccion = "Jr. Grau s/n, Barranca"
//            ),
//            numero_contacto = listOf("012352350") // ejemplo de contacto oficial, ajustable
//        ),
//
//        seguridad_salud_publica(
//            nombre = "Puestos de Salud Barranca",
//            tipo = "salud",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = 0.0,
//                longitud = 0.0,
//                referencia = "Sectores: Buenavista, Chiu Chiu, Purmacana, Potao",
//                direccion = "Varios sectores"
//            ),
//            numero_contacto = listOf("012352075")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.750510578838727,
//                longitud = -77.76504890345076,
//                referencia = "",
//                direccion = "Calle Independencia s/n"
//            ),
//            numero_contacto = listOf("012354905", "012354906")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría de Carreteras Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.753936505455542,
//                longitud = -77.75880483155137,
//                referencia = "",
//                direccion = "Calle José Gálvez 490"
//            ),
//            numero_contacto = listOf("012352302")
//        ),
//        seguridad_salud_publica(
//            nombre = "Serenazgo Municipal Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.741717151744634,
//                longitud = -77.76324690256669,
//                referencia = "",
//                direccion = ""
//            ),
//            numero_contacto = listOf("900872784")
//        ),
//        seguridad_salud_publica(
//            nombre = "SAMU Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.75215146325845,
//                longitud = -77.76316724577927,
//                referencia = "",
//                direccion = ""
//            ),
//            numero_contacto = listOf("948162002")
//        ),
//        seguridad_salud_publica(
//            nombre = "Compañía de Bomberos Voluntarios Barranca N° 73",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.754105881347417,
//                longitud = -77.76056763413719,
//                referencia = "",
//                direccion = "Plaza de Armas s/n"
//            ),
//            numero_contacto = listOf("012352333")
//        ),
//
//        // Paramonga
//        seguridad_salud_publica(
//            nombre = "Centro de Salud Paramonga José Luis Flores Mallqui",
//            tipo = "salud",
//            img = "",
//            localidad = "paramonga",
//            datos_ubi = ref_ubi(
//                latitud = -10.679865987621321,
//                longitud = -77.8163874169446,
//                referencia = "Frente a la Plaza de Armas",
//                direccion = "Urb. 7 de Junio, Calle Francisco Vidal s/n"
//            ),
//            numero_contacto = listOf("012360738")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Paramonga",
//            tipo = "seguridad",
//            img = "",
//            localidad = "paramonga",
//            datos_ubi = ref_ubi(
//                latitud = -10.673341757309588,
//                longitud = -77.81932444948157,
//                referencia = "",
//                direccion = "Av. Central N° 275"
//            ),
//            numero_contacto = listOf("2360082", "2362854")
//        ),
//
//        seguridad_salud_publica(
//            nombre = "Compañía de Bomberos Voluntarios Salvadora Paramonga N° 81",
//            tipo = "seguridad",
//            img = "",
//            localidad = "paramonga",
//            datos_ubi = ref_ubi(
//                latitud = -10.673108494600752,
//                longitud = -77.82089603101512,
//                referencia = "",
//                direccion = "Av. Central 131"
//            ),
//            numero_contacto = listOf("012360329")
//        ),
//
//        // Supe
//        seguridad_salud_publica(
//            nombre = "Hospital de Supe Laura Esther Rodríguez Dulanto",
//            tipo = "salud",
//            img = "",
//            localidad = "supe",
//            datos_ubi = ref_ubi(
//                latitud = -10.796884035820383,
//                longitud = -77.71720251256687,
//                referencia = "",
//                direccion = "Jr. Alfonso Ugarte 350"
//            ),
//            numero_contacto = listOf("930954779", "991335459")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Supe Pueblo",
//            tipo = "seguridad",
//            img = "",
//            localidad = "supe",
//            datos_ubi = ref_ubi(
//                latitud = -10.796013479381218,
//                longitud = -77.71522440097667,
//                referencia = "",
//                direccion = "Jr. Sucre 350"
//            ),
//            numero_contacto = listOf("2364304")
//        ),
//
//
//        // Pativilca
//        seguridad_salud_publica(
//            nombre = "Centro de Salud Pativilca",
//            tipo = "salud",
//            img = "",
//            localidad = "pativilca",
//            datos_ubi = ref_ubi(
//                latitud = -10.696178344630095,
//                longitud = -77.77991647824568,
//                referencia = "",
//                direccion = "Jr. Simón Bolívar 125"
//            ),
//            numero_contacto = listOf("2363406")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Pativilca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "pativilca",
//            datos_ubi = ref_ubi(
//                latitud = -10.69593001890776,
//                longitud = -77.77828829745266,
//                referencia = "",
//                direccion = "Jr. Simón Bolívar 117"
//            ),
//            numero_contacto = listOf("2363406")
//        ),
//        seguridad_salud_publica(
//            nombre = "Bomberos Pativilca N° 91",
//            tipo = "seguridad",
//            img = "",
//            localidad = "pativilca",
//            datos_ubi = ref_ubi(
//                latitud = -10.693173894803182,
//                longitud = -77.78186228182595,
//                referencia = "",
//                direccion = "Av. San Martin 295"
//            ),
//            numero_contacto = listOf("012360329")
//        ),
//
//
//        // Supe Puerto
//        seguridad_salud_publica(
//            nombre = "Puesto de salud nueva victoria",
//            tipo = "salud",
//            img = "",
//            localidad = "puerto supe",
//            datos_ubi = ref_ubi(
//                latitud = -10.798229224689468,
//                longitud = -77.7395314515867,
//                referencia = "",
//                direccion = "C. San Pedro 201, Supe Puerto 15162"
//            ),
//            numero_contacto = listOf("2364008")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Supe Puerto",
//            tipo = "seguridad",
//            img = "",
//            localidad = "puerto supe",
//            datos_ubi = ref_ubi(
//                latitud = -10.797019308906867,
//                longitud = -77.74180885176115,
//                referencia = "",
//                direccion = "Jr. Callao 501"
//            ),
//            numero_contacto = listOf("2364008")
//        ),
//
//        )


//    val listaCategorias = listOf(
//        dataclass_cat_sub(
//            "belleza", listOf(
//                "peluquerias",
//                "barberias",
//                "spas",
//                "salones de unas",
//                "centros esteticos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "comida y restaurantes", listOf(
//                "pollerias",
//                "chifas",
//                "pizzerias",
//                "cevicherias",
//                "restaurantes criollos",
//                "comida rapida",
//                "pastelerias",
//                "cafeterias",
//                "heladerias"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "educacion y librerias", listOf(
//                "institutos educativos",
//                "universidades privadas",
//                "colegios privados",
//                "librerias"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "hogar y ferreteria", listOf(
//                "mueblerias",
//                "vidrierias",
//                "ferreterias",
//                "tiendas de decoracion",
//                "tiendas de electrodomesticos",
//                "colchoneras",
//                "tiendas de iluminacion"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "moda y estilo", listOf(
//                "tiendas de ropa",
//                "tiendas de calzado",
//                "ropa deportiva",
//                "accesorios de moda",
//                "boutiques"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "salud y farmacias", listOf(
//                "boticas",
//                "farmacias",
//                "consultorios medicos",
//                "laboratorios clinicos",
//                "opticas",
//                "consultorios dentales"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "mascotas y animales", listOf(
//                "veterinarias",
//                "tiendas para mascotas",
//                "alimentos para mascotas",
//                "accesorios para mascotas",
//                "banos y peluqueria canina"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "supermercados y tiendas grandes", listOf(
//                "supermercados",
//                "mayoristas",
//                "tiendas por departamento",
//                "mercados centrales"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "minimarkets y bodegas", listOf(
//                "minimarkets",
//                "bodegas",
//                "licorerias",
//                "abarrotes",
//                "distribuidoras de agua y gas"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "transporte y terminales", listOf(
//                "paraderos de moto",
//                "paraderos de combi",
//                "terminales terrestres",
//                "agencias de transporte",
//                "cooperativas de transporte"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "viajes y turismo", listOf(
//                "agencias de viaje",
//                "paquetes turisticos",
//                "turismo local",
//                "boletos terrestres y aereos"
//            ), emptyList()
//        ),
//
//        dataclass_cat_sub(
//            "jardineria y plantas", listOf(
//                "viveros",
//                "tiendas de plantas",
//                "tiendas de abonos y fertilizantes",
//                "control de plagas",
//                "productos para jardin"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "grifos y estaciones", listOf(
//                "grifos",
//                "estaciones de servicio",
//                "venta de gasolina",
//                "venta de gas vehicular",
//                "lubricentros"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "bancos y servicios financieros", listOf(
//                "bancos",
//                "cajas municipales",
//                "cooperativas",
//                "casas de cambio",
//                "agencias financieras"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "servicios tecnicos y reparaciones", listOf(
//                "reparacion de celulares",
//                "reparacion de laptops y computadoras",
//                "reparacion de televisores",
//                "reparacion de refrigeradoras",
//                "reparacion de licuadoras",
//                "reparacion de electrodomesticos",
//                "servicios de lavado de electrodomesticos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "mecanica y autoservicios", listOf(
//                "mecanica de motos",
//                "mecanica de autos",
//                "repuestos para motos",
//                "repuestos para autos",
//                "talleres de motos",
//                "talleres de autos",
//                "lavado de vehiculos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "entretenimiento y recreacion", listOf(
//                "billares",
//                "casas de apuestas",
//                "salas de videojuegos",
//                "cabinas de internet"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "tecnologia y electronica", listOf(
//                "venta de celulares",
//                "venta de accesorios para celulares",
//                "venta de computadoras",
//                "venta de partes y perifericos",
//                "tiendas de electronica menor"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "fotografia e impresion", listOf(
//                "cabinas fotograficas",
//                "fotografias para dni y carnet",
//                "servicios de copias",
//                "plastificados y escaneos",
//                "imprentas"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "deporte y bienestar", listOf(
//                "gimnasios",
//                "centros de yoga",
//                "centros fitness",
//                "centros de pilates"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "utiles y papelerias", listOf(
//                "tiendas de utiles escolares",
//                "papelerias",
//                "venta de materiales de oficina",
//                "venta de cuadernos y lapiceros"
//            ), emptyList()
//        ),
//
//        dataclass_cat_sub(
//            "lavanderias y tintorerias", listOf(
//                "lavanderias",
//                "tintorerias",
//                "lavado en seco",
//                "planchado de ropa"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "servicios de encomienda y envios", listOf(
//                "agencias de encomienda",
//                "courier local",
//                "envios nacionales",
//                "servicios de delivery",
//                "paqueteria"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "funerarias", listOf(
//                "servicios funerarios",
//                "velatorios",
//                "venta de ataudes",
//                "traslados funerarios",
//                "cremacion"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "hospedaje y entretenimiento nocturno", listOf(
//                "hoteles",
//                "hostales",
//                "hospedajes",
//                "discotecas",
//                "bares",
//                "karaokes",
//                "salones de eventos",
//                "moteles"
//            ), emptyList()
//        )
//    )

    fun simplificarCategoria(nombre: String): String {
        return when (nombre.lowercase()) {
            "bancos y servicios financieros" -> "Bancos"
            "belleza" -> "Belleza"
            "comida y restaurantes" -> "Comida"
            "deporte y bienestar" -> "Deporte"
            "educacion y librerias" -> "Educación"
            "entretenimiento y recreacion" -> "Entretenimiento"
            "grifos y estaciones" -> "Grifos"
            "hogar y ferreteria" -> "Hogar y decoración"
            "hospedaje y entretenimiento nocturno" -> "Hospedaje"
            "jardineria y plantas" -> "Jardinería"
            "lavanderias y tintorerias" -> "Lavandería"
            "mascotas y animales" -> "Mascotas"
            "mecanica y autoservicios" -> "Mecánica"
            "minimarkets y bodegas" -> "Minimarkets"
            "moda y estilo" -> "Moda"
            "salud y farmacias" -> "Salud"
            "servicios de encomienda y envios" -> "Encomiendas"
            "servicios tecnicos y reparaciones" -> "Servicios técnicos"
            "supermercados y tiendas grandes" -> "Supermercados"
            "tecnologia y electronica" -> "Tecnología"
            "transporte y terminales" -> "Transporte"
            else -> nombre // fallback: devuelve el mismo si no está en la lista
        }
    }


    fun extractPaletteColors(bitmap: Bitmap, onColorsReady: (List<Int>) -> Unit) {
        Palette.from(bitmap).generate { palette ->
            val colors = listOfNotNull(
                palette?.darkMutedSwatch?.rgb,
                palette?.darkVibrantSwatch?.rgb
            )

            onColorsReady(colors)
        }
    }

    fun getScaledBitmap(context: Context, resId: Int, size: Int = 100): Bitmap {
        val original = BitmapFactory.decodeResource(context.resources, resId)
        return Bitmap.createScaledBitmap(original, size, size, true)
    }


    fun verificarDistanciaFormateada(
        myLat: Double,
        myLng: Double,
        latitud: Double,
        longitud: Double
    ): String {

        Log.d("obtenoemos_la_tog22", " user = ${myLat} ${myLng} teinda = ${latitud} ${longitud}")
        val resultado = FloatArray(1)
        Location.distanceBetween(myLat, myLng, latitud, longitud, resultado)
        val distancia = resultado[0]

        return if (distancia < 1000) {
            "${distancia.toInt()} m" // metros enteros
        } else {
            String.format("%.1f km", distancia / 1000f) // 1 decimal en km
        }
    }

    fun isGpsActivo(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun actualizarUbicacion(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onUpdate: (Double, Double) -> Unit
    ) {
        if (verificarUbiActiva(context)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val lat = it.latitude
                    val log = it.longitude
                    onUpdate(lat, log)
                }
            }
        } else {
            Toast.makeText(context, "Activa tu ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    fun verificarAniversarioLocalidad(dia: Int, mes: Int): Boolean {
        val today = Calendar.getInstance()
        val diaHoy = today.get(Calendar.DAY_OF_MONTH)
        val mesHoy = today.get(Calendar.MONTH) + 1
        Log.d(
            "Aniversario",
            "Hoy es: $diaHoy/$mesHoy, Fecha a verificar: $dia/$mes, Es aniversario? ${diaHoy == dia && mesHoy == mes}"
        )

        return diaHoy == dia && mesHoy == mes
    }

    fun obtenerAniversarioLocalidad(localidad: String): String {
        // Año de fundación (ejemplo real aproximado, cambia si tienes la fecha exacta)
        val aniversarios = mapOf(
            "Barranca" to Pair(1984, "5 de octubre"),        // Provincia creada oficialmente
            "Supe" to Pair(1874, "6 de noviembre"),          // Año aproximado de distrito
            "Paramonga" to Pair(1936, "22 de octubre"),      // Creación distrito
            "Pativilca" to Pair(1871, "2 de enero"),         // Creación distrito
            "Puerto Supe" to Pair(1915, "6 de diciembre")    // Creación distrito
        )

        val data = aniversarios[localidad]
        return if (data != null) {
            val (anioFundacion, fecha) = data
            val anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val edad = anioActual - anioFundacion
            "Feliz ${edad} aniversario $localidad 🎉"
        } else {
            "No tengo registrada la fecha de aniversario de $localidad"
        }
    }

    fun String.capitalizeFirst(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    fun esAniversarioHoy(localidad: String): Boolean {
        val aniversarios = mapOf(
            "barranca" to Pair(1984, "5 de octubre"),
            "supe" to Pair(1874, "6 de noviembre"),
            "paramonga" to Pair(1936, "22 de octubre"),
            "pativilca" to Pair(1871, "2 de enero"),
            "puerto supe" to Pair(1915, "6 de diciembre")
        )

        val data = aniversarios[localidad]
        if (data == null) {
            Log.d("ANIVERSARIO", "No hay datos para $localidad")
            return false
        }

        val (_, fecha) = data
        Log.d("ANIVERSARIO", "Fecha registrada: $fecha")

        val partes = fecha.split(" de ")
        if (partes.size != 2) {
            Log.d("ANIVERSARIO", "Formato de fecha inválido para $localidad → $fecha")
            return false
        }

        val dia = partes[0].toIntOrNull()
        if (dia == null) {
            Log.d("ANIVERSARIO", "Día inválido en fecha: $fecha")
            return false
        }

        val mesTexto = partes[1].lowercase()
        Log.d("ANIVERSARIO", "Día: $dia, Mes texto: $mesTexto")

        val meses = mapOf(
            "enero" to java.util.Calendar.JANUARY,
            "febrero" to java.util.Calendar.FEBRUARY,
            "marzo" to java.util.Calendar.MARCH,
            "abril" to java.util.Calendar.APRIL,
            "mayo" to java.util.Calendar.MAY,
            "junio" to java.util.Calendar.JUNE,
            "julio" to java.util.Calendar.JULY,
            "agosto" to java.util.Calendar.AUGUST,
            "septiembre" to java.util.Calendar.SEPTEMBER,
            "octubre" to java.util.Calendar.OCTOBER,
            "noviembre" to java.util.Calendar.NOVEMBER,
            "diciembre" to java.util.Calendar.DECEMBER
        )

        val mes = meses[mesTexto]
        if (mes == null) {
            Log.d("ANIVERSARIO", "Mes inválido: $mesTexto")
            return false
        }

        val hoy = java.util.Calendar.getInstance()
        val diaHoy = hoy.get(java.util.Calendar.DAY_OF_MONTH)
        val mesHoy = hoy.get(java.util.Calendar.MONTH)

        Log.d("ANIVERSARIO", "Hoy es $diaHoy de ${mesHoy + 1} | Aniversario: $dia de ${mes + 1}")

        val resultado = diaHoy == dia && mesHoy == mes
        Log.d("ANIVERSARIO", "¿Es aniversario hoy? $resultado")

        return resultado
    }

    data class lugares_turisticos2(
        val titulo: String = "",
        val descripcion: String = "",
        val img_ref: String = "",
        val direcccion: String = "",
        val referencia: String = "",
        val latitud: Double = 0.0,
        val longitud: Double = 0.0,
        val subcategoria_filtrado: List<String> = emptyList(),
        val localida: String
    )


    val datos_ubicacionesreales = listOf(
        lugares_turisticos2(
            titulo = "Playa Chorrillos",
            descripcion = "Una de las playas más visitadas de Barranca, ideal para disfrutar del sol y el mar, con una vibra relajada y familiar. Es un punto de encuentro popular para locales y turistas.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.760706064882067,
            longitud = -77.764686746073,
            subcategoria_filtrado = listOf("playa"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Playa Miraflores",
            descripcion = "Forma parte del circuito de playas de Barranca, conocida por su ambiente tranquilo y sus aguas aptas para nadar. Es un lugar perfecto para relajarse y contemplar el paisaje costero.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.762725766858797,
            longitud = -77.76265770994893,
            subcategoria_filtrado = listOf("playa"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Playa Puerto Chico",
            descripcion = "Conocida por ser un balneario pintoresco y una de las playas más accesibles de la zona. Es un lugar clave para quienes buscan disfrutar de la brisa marina y pasear por la orilla.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.766447182644843,
            longitud = -77.76168315070542,
            subcategoria_filtrado = listOf("playa"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Playa Colorado",
            descripcion = "Una playa singular que destaca por sus formaciones rocosas y su arena de tonos rojizos. Es un destino ideal para quienes buscan paisajes únicos y una experiencia de playa diferente.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.773308198625925,
            longitud = -77.75903246437092,
            subcategoria_filtrado = listOf("playa"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Playa Bandurria",
            descripcion = "Ubicada en la Bahía de Pativilca, esta playa es famosa por su belleza natural y la cercanía al sitio arqueológico del mismo nombre. Es un lugar con historia y un entorno impresionante.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.775926198586697,
            longitud = -77.75567452103537,
            subcategoria_filtrado = listOf("playa"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Playa Atarraya",
            descripcion = "Conocida por su tranquilidad y su ambiente de pescadores. Es un lugar ideal para observar las actividades locales y disfrutar de una vista relajante del océano Pacífico.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.778789841646699,
            longitud = -77.75391347123461,
            subcategoria_filtrado = listOf("playa"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Cristo Redentor",
            descripcion = "Una imponente estatua de Cristo que se erige en el cerro Colorado. Ofrece una vista panorámica espectacular de toda la bahía de Barranca, siendo un punto de interés tanto religioso como turístico.",
            img_ref = "",
            direcccion = "Cerro Colorado",
            referencia = "Monumento",
            latitud = -10.7699116708496,
            longitud = -77.76427946867258,
            subcategoria_filtrado = listOf("monumento", "religioso", "mirador"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Plaza de Armas",
            descripcion = "El corazón de Barranca, un espacio público vibrante rodeado de edificios historicos y palmeras. Es el punto de encuentro principal de la ciudad, con jardines cuidados y un ambiente acogedor.",
            img_ref = "",
            direcccion = "Centro de Barranca",
            referencia = "Plaza",
            latitud = -10.754119848818947,
            longitud = -77.76084684873094,
            subcategoria_filtrado = listOf("plaza", "parque"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Iglesia San Idelfonso",
            descripcion = "Una iglesia historica en el centro de Barranca, conocida por su arquitectura y su importancia cultural. Es un lugar de paz y reflexión que forma parte del patrimonio de la ciudad.",
            img_ref = "",
            direcccion = "",
            referencia = "Iglesia",
            latitud = -10.754403149844096,
            longitud = -77.76109118438252,
            subcategoria_filtrado = listOf("iglesia", "religioso", "historico"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Mirador Grau",
            descripcion = "Un mirador popular que ofrece una de las mejores vistas de la ciudad y el puerto. Es un lugar perfecto para tomar fotografías y apreciar la geografía de la zona desde las alturas.",
            img_ref = "",
            direcccion = "",
            referencia = "Mirador",
            latitud = -10.757691664011752,
            longitud = -77.7673856100165,
            subcategoria_filtrado = listOf("mirador", "recreativo"),
            localida = "barranca"
        ),
        lugares_turisticos2(
            titulo = "Caral",
            descripcion = "La Ciudad Sagrada de Caral es la civilización más antigua de América. Es un impresionante sitio arqueologico con pirámides, templos y plazas, que demuestra el alto grado de desarrollo de la sociedad de Caral-Supe.",
            img_ref = "",
            direcccion = "",
            referencia = "Sitio arqueologico",
            latitud = -10.892064926519373,
            longitud = -77.52363743208294,
            subcategoria_filtrado = listOf("arqueologico", "historico"),
            localida = "supe"
        ),
        lugares_turisticos2(
            titulo = "Peñico",
            descripcion = "Una comunidad o lugar de referencia en Supe. Es un punto de interés que se menciona en la conversación y que puede tener relevancia local para los residentes.",
            img_ref = "",
            direcccion = "",
            referencia = "",
            latitud = -10.899061373112445,
            longitud = -77.3781547554546,
            subcategoria_filtrado = emptyList(),
            localida = "supe"
        ),
        lugares_turisticos2(
            titulo = "Sitio arqueologico Miraya",
            descripcion = "Un importante complejo arqueologico que complementa la historia de la civilización Caral-Supe. Es un lugar clave para entender la distribución y la organización de los asentamientos prehispánicos en la zona.",
            img_ref = "",
            direcccion = "",
            referencia = "Sitio arqueologico",
            latitud = -10.88237044894816,
            longitud = -77.53973149505958,
            subcategoria_filtrado = listOf("arqueologico", "historico"),
            localida = "supe"
        ),
        lugares_turisticos2(
            titulo = "Museo Comunitario de Supe",
            descripcion = "Un museo local que alberga importantes hallazgos arqueologicos de la región de Supe, incluyendo piezas de la civilización Caral. Es un excelente lugar para aprender sobre la historia y la cultura del Valle de Supe.",
            img_ref = "",
            direcccion = "",
            referencia = "Museo",
            latitud = -10.79630741006499,
            longitud = -77.71642201050005,
            subcategoria_filtrado = listOf("museo", "cultura"),
            localida = "supe"
        ),
        lugares_turisticos2(
            titulo = "Plaza de armas de Supe",
            descripcion = "El corazón de Pativilca, un espacio público tradicional que es el centro de la vida social y cultural del pueblo. Es ideal para una pausa y para conocer el ambiente local.",
            img_ref = "",
            direcccion = "",
            referencia = "Plaza",
            latitud = -10.79571832269379,
            longitud = -77.71640528516001,
            subcategoria_filtrado = listOf("plaza", "parque"),
            localida = "supe"
        ),
        lugares_turisticos2(
            titulo = "Plaza de armas de Pativilca",
            descripcion = "El corazón de Pativilca, un espacio público tradicional que es el centro de la vida social y cultural del pueblo. Es ideal para una pausa y para conocer el ambiente local.",
            img_ref = "",
            direcccion = "Centro de Pativilca",
            referencia = "Plaza",
            latitud = -10.696393939697877,
            longitud = -77.7797801953667,
            subcategoria_filtrado = listOf("plaza", "parque"),
            localida = "pativilca"
        ),
        lugares_turisticos2(
            titulo = "Museo Bolivariano",
            descripcion = "Dedicado a la memoria de Simón Bolívar, este museo se encuentra en la casa donde residió el Libertador. Contiene objetos y documentos historicos relacionados con su estadía y la independencia del Perú.",
            img_ref = "",
            direcccion = "",
            referencia = "Museo",
            latitud = -10.695440708535772,
            longitud = -77.78065684226628,
            subcategoria_filtrado = listOf("museo", "historico"),
            localida = "pativilca"
        ),
        lugares_turisticos2(
            titulo = "Plaza de Armas de Paramonga",
            descripcion = "El centro de la vida en Paramonga, una plaza bien cuidada que sirve como punto de referencia y de encuentro. Es un lugar de descanso y de observación de la vida cotidiana en el distrito.",
            img_ref = "",
            direcccion = "Centro de Paramonga",
            referencia = "Plaza",
            latitud = -10.674397201524314,
            longitud = -77.8185777381935,
            subcategoria_filtrado = listOf("plaza", "parque"),
            localida = "paramonga"
        ),
        lugares_turisticos2(
            titulo = "Fortaleza de Paramonga",
            descripcion = "Una impresionante fortaleza de adobe, construida por la cultura Chimú y posteriormente ocupada por los Incas. Su estructura piramidal y su ubicación estratégica la convierten en uno de los atractivos arqueologicos más importantes de la zona.",
            img_ref = "",
            direcccion = "",
            referencia = "Fortaleza, arqueologico",
            latitud = -10.653336709267442,
            longitud = -77.84137903741973,
            subcategoria_filtrado = listOf("fortaleza", "arqueologico", "historico"),
            localida = "paramonga"
        ),
        lugares_turisticos2(
            titulo = "La casa de las brujas",
            descripcion = "Un lugar misterioso y conocido por las leyendas locales. Aunque no se conoce su historia precisa, es un punto de referencia que despierta la curiosidad y forma parte del folclore de Paramonga.",
            img_ref = "",
            direcccion = "",
            referencia = "Leyenda",
            latitud = -10.673073750951527,
            longitud = -77.81965564235402,
            subcategoria_filtrado = listOf("leyenda", "turismo"),
            localida = "paramonga"
        ),
        lugares_turisticos2(
            titulo = "Playa La Isla",
            descripcion = "Una playa pintoresca en el puerto de Supe, conocida por sus aguas tranquilas y su ambiente de caleta. Es un lugar idílico para disfrutar de la pesca, el paisaje costero y la gastronomía local.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.760706064882067,
            longitud = -77.764686746073,
            subcategoria_filtrado = listOf("playa"),
            localida = "puerto supe"
        ),
        lugares_turisticos2(
            titulo = "Playa El Faraón",
            descripcion = "Esta playa, también conocida como 'La Isla', es un destino popular en Puerto Supe. Su nombre evoca misterio y su belleza natural la convierte en un lugar favorito para los visitantes de la zona.",
            img_ref = "",
            direcccion = "",
            referencia = "Playa",
            latitud = -10.81187622414177,
            longitud = -77.75175163336938,
            subcategoria_filtrado = listOf("playa"),
            localida = "puerto supe"
        ),
        lugares_turisticos2(
            titulo = "El Áspero",
            descripcion = "Considerado como una 'Ciudad Pesquera de Caral', es un sitio arqueologico crucial para entender la conexión entre la costa y el interior en la época de la civilización Caral-Supe. Aquí se encuentran vestigios de pirámides y viviendas antiguas.",
            img_ref = "",
            direcccion = "",
            referencia = "Sitio arqueologico",
            latitud = -10.814415381147068,
            longitud = -77.74162389966081,
            subcategoria_filtrado = listOf("arqueologico", "historico"),
            localida = "puerto supe"
        )
    )

//
//

    fun eliminar_menios_comida() {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("lugares")

// Obtenemos todos los documentos que NO sean de categoria "comida y restaurantes"
        ref.whereNotEqualTo("categoria", "comida y restaurantes")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    ref.document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Documento eliminado: ${document.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al eliminar documento: ${document.id}", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener documentos", e)
            }

    }
//    fun agregar_lugares_turisticos2(lugaresTuristicos: lugares_turisticos2) {
//        val db = FirebaseFirestore.getInstance().collection("Tiendas")
//            .document(lugaresTuristicos.localida).collection("lugares_turisticos")
//
//        val hasmap_ubicacion = hashMapOf<String, Any>(
//            "direccion" to lugaresTuristicos.direcccion,
//            "latitud" to lugaresTuristicos.latitud,
//            "longitud" to lugaresTuristicos.longitud
//        )
//        val hashMap = hashMapOf<String, Any>(
//            "categoria" to lugaresTuristicos.subcategoria_filtrado,
//            "descripcion" to lugaresTuristicos.descripcion,
//            "img" to lugaresTuristicos.img_ref,
//            "titulo" to lugaresTuristicos.titulo,
//            "ubicacion" to hasmap_ubicacion
//        )
//
//        db.add(hashMap).addOnSuccessListener { res ->
//            val id_creado = res.id
//            val hasmapUpdate = hashMapOf<String, Any>(
//                "id" to id_creado
//            )
//            db.document(id_creado).update(hasmapUpdate).addOnSuccessListener {
//                val ItemPasadp = Item(
//                    lugaresTuristicos.titulo,
//                    lugaresTuristicos.localida,
//                    id_creado,
//                    "turismo", lugaresTuristicos.img_ref, lugaresTuristicos.subcategoria_filtrado
//
//                )
//                agregar_lugares_turisticos(ItemPasadp,lugaresTuristicos.latitud,lugaresTuristicos.longitud)
//            }
//
//        }.addOnFailureListener { e ->
//            Log.d("error_subir_datos", "error")
//        }
//
//    }

    //

//    fun obtener_seguridad(onResult: (List<Item>) -> Unit) {
//        val db = FirebaseFirestore.getInstance()
//            .collection("Tiendas")
//            .document("salud_seguridad")
//            .collection("barranca")
//
//        db.get().addOnSuccessListener { res ->
//            val dattt = res.map { datos ->
//                val data = datos.data
//                val categoria = data?.get("categoria") as? String ?: ""
//                val id = data?.get("id") as? String ?: ""
//                val img = data?.get("img") as? String ?: ""
//                val lugar = data?.get("lugar") as? String ?: ""
//                val nombre = data?.get("nombre") as? String ?: ""
//                val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
//                val latitud = (ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0
//                val longitud = (ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
//
//                Item(
//                    nombre = nombre,
//                    lugar = lugar,
//                    id_tienda = id,
//                    categoria = categoria,
//                    img = img,
//                    lista = listOf(categoria),
//                    latitud = latitud,
//                    longitud = longitud
//                )
//            }
//
//            onResult(dattt)
//        }.addOnFailureListener {
//            onResult(emptyList())
//        }
//    }


    fun agregar_lugares_turisticos(Item: Item) {
        val db = FirebaseFirestore.getInstance().collection("lugares").document(Item.id_tienda)
        val hasmap_ubicacion = hashMapOf<String, Any>(
            "latitud" to Item.latitud,
            "longitud" to Item.longitud
        )
        val hashMap = hashMapOf<String, Any>(
            "categoria" to Item.categoria,
            "id_tienda" to Item.id_tienda,
            "img" to Item.img,
            "lugar" to Item.lugar,
            "nombre" to Item.nombre,
            "tag" to Item.lista,
            "ubicacion" to hasmap_ubicacion
        )
        db.set(hashMap).addOnSuccessListener { res ->
            Log.d("creado_correcto", "${Item.id_tienda} creado correctamente :)")
        }.addOnFailureListener { e ->
            Log.d("error_subir_datos", "error")
        }
    }

    val shadow_top_filtrado_v1 = listOf(
        Color(0xFF262626),
        Color.Transparent,
    )
    val shadow_botonm_filtrado_v1 = listOf(
        Color.Transparent,
        Color(0xFF262626),
    )

    val shadow_top_filtrado_v2 = listOf(
        Color(0XFF535252),
        Color.Transparent,
    )
    val shadow_botonm_filtrado_v2 = listOf(
        Color.Transparent,
        Color(0XFF535252),
    )

    val start_shadow_bottom_sheet_default = listOf(
        Color(0XFF1D1B20),
        Color.Transparent,
    )
    val end_shadow_bottom_sheet_default = listOf(
        Color.Transparent,
        Color(0XFF1D1B20)
    )

    val end_subcategoria_shadow = listOf(
        Color.Transparent,
        Color(0xFF262626)
    )
    val strat_subcategoria_shadow = listOf(
        Color(0xFF262626),
        Color.Transparent
    )

    val shadow_left = listOf(
        Color(0xFF000000),
        Color.Transparent,
    )

    // Sombra derecha (de transparente a oscuro)
    val shadow_right = listOf(
        Color.Transparent,
        Color(0xFF000000),
    )

    fun abrir_whattsapp(
        context: Context,
        numero: String,
        mensajePredefinido: String = "¡Hola! Vengo de Geinz y me gustaría hacer una consulta. ¿Me pueden atender?"
    ) {
        // 2. Codificar el mensaje para que sea seguro en la URL.
        val mensajeCodificado = URLEncoder.encode(mensajePredefinido, "UTF-8")
        val uri = Uri.parse(
            "https://api.whatsapp.com/send?phone=${"+51$numero"}&text=$mensajeCodificado"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "no se pudo abrir whatsapp",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }


    fun llamar(context: Context, numero: String, open_dialog: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            open_dialog()
        } else {
            makePhoneCall(context, numero)
        }
    }

    private fun requestCallPermission(context: Context, phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
        } else {
            makePhoneCall(context, phoneNumber)
        }
    }

    private fun makePhoneCall(context: Context, phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            context.startActivity(callIntent)
        } else {
            requestCallPermission(context, phoneNumber)
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    data class TiempoRestanteResult(
        val texto: String,
        val color: Color
    )

    fun calcularTiempoRestante(
        horario_total: horario_tienda,
        hCierre: String,
        cerrado: Boolean,
        motivo: String
    ): TiempoRestanteResult {
        return try {
            if (hCierre.isBlank()) return TiempoRestanteResult("", Color.Gray)

            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val ahora = Calendar.getInstance()

            // 🔹 Obtener hora de apertura y cierre de hoy
            val horaApertura = Calendar.getInstance()
            val horaCierre = Calendar.getInstance()

            val parsedApertura = formato.parse(horario_total.h_apertura)
            val parsedCierre = formato.parse(hCierre)

            if (parsedApertura == null || parsedCierre == null)
                return TiempoRestanteResult("", Color.Gray)

            horaApertura.time = parsedApertura
            horaCierre.time = parsedCierre

            horaApertura.set(Calendar.YEAR, ahora.get(Calendar.YEAR))
            horaApertura.set(Calendar.MONTH, ahora.get(Calendar.MONTH))
            horaApertura.set(Calendar.DAY_OF_MONTH, ahora.get(Calendar.DAY_OF_MONTH))
            horaCierre.set(Calendar.YEAR, ahora.get(Calendar.YEAR))
            horaCierre.set(Calendar.MONTH, ahora.get(Calendar.MONTH))
            horaCierre.set(Calendar.DAY_OF_MONTH, ahora.get(Calendar.DAY_OF_MONTH))

            val ahoraMillis = ahora.timeInMillis

            // ⚠️ Nuevo manejo de días cerrados
            if (cerrado) {
                // Si tiene motivo → mostrarlo directamente
                if (motivo.isNotBlank()) {
                    return TiempoRestanteResult(motivo, Color(0xFFF4C524))
                } else {
                    // Si NO tiene motivo → mostrar la próxima apertura
                    val diaProx = horario_total.dia_prox_apertura
                    val horaProx = horario_total.hora_prox_apertura

                    if (diaProx.isNotBlank() && horaProx.isNotBlank()) {
                        val formatoEntrada = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val formatoSalida = SimpleDateFormat("hh:mm a", Locale.getDefault())

                        val horaFormateada = try {
                            val parsedProx = formatoEntrada.parse(horaProx)
                            formatoSalida.format(parsedProx!!)
                        } catch (e: Exception) {
                            horaProx
                        }

                        return TiempoRestanteResult(
                            "Abre $diaProx a las $horaFormateada",
                            Color.Red
                        )
                    } else {
                        return TiempoRestanteResult("Cerrado", Color.Red)
                    }
                }
            }

            // 🧭 Si no está cerrado por configuración → flujo normal
            return when {
                // 🕗 Antes de abrir hoy
                ahoraMillis < horaApertura.timeInMillis -> {
                    val formatoSalida = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val horaFormateada = formatoSalida.format(horaApertura.time)
                    TiempoRestanteResult(
                        texto = "Abre hoy a las $horaFormateada",
                        color = Color.Red
                    )
                }

                // 🟢 Durante el horario de atención
                ahoraMillis in horaApertura.timeInMillis..horaCierre.timeInMillis -> {
                    val diffMillis = horaCierre.timeInMillis - ahoraMillis
                    val horas = TimeUnit.MILLISECONDS.toHours(diffMillis)
                    val minutos = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60

                    val texto = when {
                        horas > 0 && minutos > 0 -> "Cierra en ${horas}h ${minutos}m"
                        horas > 0 -> "Cierra en ${horas}h"
                        minutos > 0 -> "Cierra en ${minutos}m"
                        else -> "Cerrando"
                    }

                    val color = when {
                        horas >= 1 -> Color.Green
                        minutos in 15..59 -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    }

                    TiempoRestanteResult(texto, color)
                }

                // 🔴 Ya cerró hoy → mostrar el próximo día de apertura
                else -> {
                    val diaProx = horario_total.dia_prox_apertura
                    val horaProx = horario_total.hora_prox_apertura

                    if (diaProx.isNotBlank() && horaProx.isNotBlank()) {
                        val formatoEntrada = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val formatoSalida = SimpleDateFormat("hh:mm a", Locale.getDefault())

                        val horaFormateada = try {
                            val parsedProx = formatoEntrada.parse(horaProx)
                            formatoSalida.format(parsedProx!!)
                        } catch (e: Exception) {
                            horaProx
                        }

                        TiempoRestanteResult("Abre $diaProx a las $horaFormateada", Color.Red)
                    } else {
                        TiempoRestanteResult("Cerrado", Color.Red)
                    }
                }
            }
        } catch (e: Exception) {
            TiempoRestanteResult("", Color.Gray)
        }
    }

    fun Map<String, Any>?.toMetodoContacto(): metodo_contacto_tienda {
        fun Map<String, Any>?.toNumero() = contacto_numero(
            estado = this?.get("estado") as? Boolean ?: false,
            numero = this?.get("numero") as? String ?: ""
        )

        fun Map<String, Any>?.toRed() = contacto_red(
            estado = this?.get("estado") as? Boolean ?: false,
            nombre = this?.get("nombre") as? String ?: "",
            url = this?.get("url") as? String ?: ""
        )



        return metodo_contacto_tienda(
            whatsapp = (this?.get("whatsapp") as? Map<String, Any>).toNumero(),
            llamada = (this?.get("llamada") as? Map<String, Any>).toNumero(),
            facebook = (this?.get("facebook") as? Map<String, Any>).toRed(),
            instagram = (this?.get("instagram") as? Map<String, Any>).toRed(),
            tiktok = (this?.get("tiktok") as? Map<String, Any>).toRed(),
            sitio_web = (this?.get("sitio_web") as? Map<String, Any>).toRed()
        )
    }

    fun Map<String, Any>?.to_metodo_pago(): modelo_pagos_tienda {
        if (this == null) return modelo_pagos_tienda()
        fun getMetodo(key: String): modelo_metodo_individual {
            val metodo = this[key] as? Map<*, *> ?: return modelo_metodo_individual()
            return modelo_metodo_individual(
                numero = metodo["numero"] as? String ?: "",
                qr = metodo["qr"] as? String ?: "",
                enable = metodo["enable"] as? Boolean ?: false,
                nombre = metodo["nombre"] as? String ?: ""
            )
        }
        return modelo_pagos_tienda(
            visa_mastercard = getMetodo("Visa/Mastercard"),
            agora = getMetodo("agora"),
            efectivo = getMetodo("efectivo"),
            plin = getMetodo("plin"),
            yape = getMetodo("yape")
        )

    }

    data class cordenasdas(val lat: Double, val longitud: Double)

    val lista_cordenadas = listOf(
        cordenasdas(-10.892064926519373, -77.52363743208294),
        cordenasdas(-10.814415381147068, -77.74162389966081),
        cordenasdas(-10.760706064882067, -77.764686746073),
        cordenasdas(-10.653336709267442, -77.84137903741973),
        cordenasdas(-10.695440708535772, -77.78065684226628),
        cordenasdas(-10.7699116708496, -77.76427946867258)
    )

    suspend fun subir_cordenas_algolioa() {
        val db = FirebaseFirestore.getInstance()
            .collection("lugares")
            .whereEqualTo("categoria", "turismo")
            .get()
            .await()

        val firestore = FirebaseFirestore.getInstance() // instancia única

        for (datos in db.documents) {
            val data = datos.data
            val ubicacion = data?.get("ubicacion") as? Map<String, Any>
            val lat = ubicacion?.get("latitud") as? Number ?: 0
            val longitud = ubicacion?.get("longitud") as? Number ?: 0
            val geohash = geohashing(lat.toDouble(), longitud.toDouble())
            val idTienda = data?.get("id_tienda") as? String ?: continue

            val hashMap = hashMapOf<String, Any>(
                "geohash" to geohash
            )

            // Actualiza solo el campo geohash sin borrar otros datos
            firestore.collection("lugares").document(idTienda).update(hashMap).await()
        }
    }


    fun geohashing(lat: Double, lon: Double): String {
        Log.d("generar_geo", "$lat $lon")
        return GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lon))
    }

    fun obtenerUbicacion(context: Context, onLocation: (LatLng) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1 // solo queremos 1 actualización
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    onLocation(LatLng(it.latitude, it.longitude))
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun filtrar_por_radio_interno(
        radio: Float,
        hasing_user: String,
        lista_base: List<Item>
    ): List<Item> {
        Log.d("llamaointer", "funfiltradainteran")
        val precision = when {
            radio <= 0.1 -> 8
            radio <= 0.3 -> 7
            radio <= 1 -> 6
            radio <= 5 -> 5
            else -> 4
        }
        val prefijo = hasing_user.take(precision)

        return lista_base.filter { it.geohasing.startsWith(prefijo) }
    }

    fun verificarGPS(
        context: Context,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L
        ).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)

        client.checkLocationSettings(builder.build())
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    launcher.launch(intentSenderRequest)
                } else {
                    Log.e("GPS", "Error al verificar ajustes de ubicación: ${exception.message}")
                }
            }
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionEnTiempoReal(
        gps_activo: Boolean,
        context: Context,
        onLocation: (Double, Double) -> Unit,
        onTimeout: () -> Unit // Callback para cuando se supera el tiempo máximo
    ) {
        if (!gps_activo) {
            Log.w("UBICACION_TIEMPO_REAL", "⚠️ GPS inactivo, cancelando solicitud inmediatamente.")
            onTimeout()
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        var ubicacionObtenida = false // 🔹 bandera de control

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // cada cuánto se intenta actualizar
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                if (!ubicacionObtenida) { // 🔹 solo si no se obtuvo antes
                    ubicacionObtenida = true
                    Log.d(
                        "UBICACION_TIEMPO_REAL",
                        "✅ Ubicación obtenida: ${location.latitude}, ${location.longitude}"
                    )
                    onLocation(location.latitude, location.longitude)
                    fusedClient.removeLocationUpdates(this)
                }
            }
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // 🔹 Temporizador de 15 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            if (!ubicacionObtenida) {
                Log.w(
                    "UBICACION_TIEMPO_REAL",
                    "⚠️ Tiempo de espera superado (15s), cancelando solicitud."
                )
                fusedClient.removeLocationUpdates(locationCallback)
                onTimeout()
            }
        }, 15000L)
    }


//    @SuppressLint("MissingPermission")
//    fun obtenerUbicacionEnTiempoReal(
//        context: Context,
//        onLocation: (Double, Double) -> Unit
//    ) {
//        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
//
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            5000L
//        ).setMinUpdateDistanceMeters(1f) // que se actualice aunque te muevas poco
//            .build()
//
//        val callback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                val location = result.locations.lastOrNull()
//                if (location != null && location.time + 1000 < System.currentTimeMillis()) {
//                    // aceptamos solo ubicaciones recientes
//                    onLocation(location.latitude, location.longitude)
//                    fusedClient.removeLocationUpdates(this)
//                }
//            }
//        }
//
//        fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
//    }


    @SuppressLint("MissingPermission")
    fun obtenerUbicacionReal(
        context: Context,
        onLocation: (Double, Double) -> Unit,
        onTimeout: (() -> Unit)? = null // callback opcional si pasa el tiempo
    ) {
        Log.d("OBTENER_UBICACION", "Iniciando obtenerUbicacionReal()...")

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateDistanceMeters(1f)
            .build()

        var locationRecibida = false

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                Log.d("OBTENER_UBICACION", "onLocationResult() llamado")
                val location = result.lastLocation

                if (location != null) {
                    val diff = System.currentTimeMillis() - location.time
                    Log.d(
                        "OBTENER_UBICACION",
                        "Coordenadas → lat=${location.latitude}, lng=${location.longitude}, diff=$diff"
                    )

                    if (diff < 3000 && !locationRecibida) {
                        locationRecibida = true
                        Log.d("OBTENER_UBICACION", "✅ Ubicación válida, devolviendo resultado.")
                        onLocation(location.latitude, location.longitude)
                        fusedClient.removeLocationUpdates(this)
                        Log.d("OBTENER_UBICACION", "🚫 Se detuvo la escucha de actualizaciones.")
                    } else {
                        Log.w("OBTENER_UBICACION", "⚠️ Ubicación descartada o ya recibida.")
                    }
                } else {
                    Log.e("OBTENER_UBICACION", "❌ result.lastLocation == null (sin datos)")
                }
            }
        }

        // Inicia el listener
        Log.d("OBTENER_UBICACION", "Solicitando actualizaciones de ubicación...")
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // ⏱️ Timeout de 15 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            if (!locationRecibida) {
                Log.e(
                    "OBTENER_UBICACION",
                    "⏰ Tiempo de espera agotado (15s). Cancelando solicitud."
                )
                fusedClient.removeLocationUpdates(callback)
                onTimeout?.invoke()
            }
        }, 15000L)
    }


    fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    data class Zona(
        val nombre: String,
        val latMin: Double,
        val latMax: Double,
        val lonMin: Double,
        val lonMax: Double
    )

    fun obtenerZonaActual(lat: Double, lon: Double): String {
        val zonas = listOf(
            // 🟢 Supe
            Zona(
                "Supe",
                latMin = -10.819538261079375,
                latMax = -10.786853029158221,
                lonMin = -77.72571490375917,
                lonMax = -77.69103646347199
            ),

            // 🟣 Puerto Supe
            Zona(
                "Puerto Supe",
                latMin = -10.82069349776264,
                latMax = -10.782293146257883,
                lonMin = -77.76089119930481,
                lonMax = -77.72195131770866
            ),

            // 🔵 Barranca
            Zona(
                "Barranca",
                latMin = -10.782337175402573,
                latMax = -10.72179267853299,
                lonMin = -77.7902832245304,
                lonMax = -77.74226095544132
            ),

            // 🟠 Pativilca
            Zona(
                "Pativilca",
                latMin = -10.701136877458813,
                latMax = -10.67908010034809,
                lonMin = -77.79371612285841,
                lonMax = -77.76034972738653
            ),

            // 🔴 Paramonga
            Zona(
                "Paramonga",
                latMin = -10.686996630651555,
                latMax = -10.66270517917747,
                lonMin = -77.83882787730298,
                lonMax = -77.79363802091774
            )
        )

        for (z in zonas) {
            if (lat in z.latMin..z.latMax && lon in z.lonMin..z.lonMax) {
                return z.nombre
            }
        }

        return "Fuera de zona"
    }


    data class metodos_pago_tiendas(
        val enable: Boolean,
        val img: Int = 0,
        val nombre_metodo: String
    )

    fun mostrar_iconos_pagos(i: modelo_pagos_tienda): List<metodos_pago_tiendas> {
        val lista = listOf(
            metodos_pago_tiendas(i.yape.enable, R.drawable.yape_logo, "yape"),
            metodos_pago_tiendas(i.plin.enable, R.drawable.logo_plin, "plin"),
            metodos_pago_tiendas(i.agora.enable, R.drawable.logo_agora, "agora"),
            metodos_pago_tiendas(
                i.visa_mastercard.enable,
                R.drawable.master_car_logo,
                "mastercard"
            ),
            metodos_pago_tiendas(i.visa_mastercard.enable, R.drawable.visa_logo, "visa"),


            )
        return lista

    }

//    @Composable
//    fun rememberMyLocationVisibility(context: Context): State<Boolean> {
//        val isLocationVisible = remember { mutableStateOf(false) }
//
//        // Verificar al inicio
//        LaunchedEffect(Unit) {
//            isLocationVisible.value = isLocationEnabled(context)
//        }
//
//        // Detectar cambios (cuando el usuario activa/desactiva GPS)
//        DisposableEffect(Unit) {
//            val receiver = object : BroadcastReceiver() {
//                override fun onReceive(ctx: Context?, intent: Intent?) {
//                    if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
//                        isLocationVisible.value = isLocationEnabled(context)
//                    }
//                }
//            }
//
//            val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
//            context.registerReceiver(receiver, filter)
//
//            onDispose { context.unregisterReceiver(receiver) }
//        }
//
//        return isLocationVisible
//    }

    // Función auxiliar
    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


}
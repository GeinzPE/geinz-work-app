package com.geinzz.geinzwork.model

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.geinzz.geinzwork.data.model.dataclass_seguridad.FrasePendiente
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.herramientas_geinz.constantes.construirPromptNLP
import com.geinzz.geinzwork.herramientas_geinz.constantes.procesaro_por_vos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.math.*
import kotlin.system.measureTimeMillis

class repo_seguridad_salud {
    val db = FirebaseFirestore.getInstance()

    data class UbicacionResult(val latLng: LatLng, val callback: LocationCallback)

    private val client = OkHttpClient()

    suspend fun obtener_servicios_salud(localdad: String): List<dataclass_seguridad> {
        val lista = mutableListOf<dataclass_seguridad>()
        try {
            Log.d("DEBUG_SERVICIOS", "Consultando datos de: $localdad")

            val ref = db.collection("Tiendas")
                .document("salud_seguridad")
                .collection(localdad)
                .get()
                .await()

            Log.d("DEBUG_SERVICIOS", "Documentos obtenidos: ${ref.size()}")

            for (datos in ref) {
                val data = datos.data
                Log.d("DEBUG_SERVICIOS", "Documento ID: ${datos.id} → Data: $data")

                val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
                val numero_contacto =
                    data?.get("numeros_contactos") as? Map<String, Any> ?: emptyMap()

                val latitud = (ubicacion["latitud"] as? Number ?: 0).toDouble()
                val referencia = (ubicacion["referencia"] as? String ?: "")
                val longitud = (ubicacion["longitud"] as? Number ?: 0).toDouble()
                val categoria = data?.get("categoria") as? String ?: ""

                val llamada = numero_contacto["llamada"] as? List<String> ?: emptyList()
                val whatsapp = numero_contacto["whatsapp"] as? List<String> ?: emptyList()

                val tag_eventos_emerge =
                    data?.get("tag_eventos_emerge") as? List<String> ?: emptyList()
                val tag_no_urgentes = data?.get("tag_no_urgentes") as? List<String> ?: emptyList()
                val key_alias = data?.get("alias_key") as? List<String> ?: emptyList()

                val servicios = dataclass_seguridad(
                    nombre_ = data?.get("nombre") as? String ?: "",
                    direccion = ubicacion["direccion"] as? String ?: "",
                    numero_llamada = llamada,
                    numero_whatsapp = whatsapp,
                    img_ref = data?.get("img") as? String ?: "",
                    latidud = latitud,
                    longitud = longitud,
                    referencia = referencia,
                    categoria = categoria,
                    etiqutas_emergencias = tag_eventos_emerge,
                    etiquetas_no_urgente = tag_no_urgentes,
                    key_alias = key_alias
                )

                Log.d("DEBUG_SERVICIOS", "Objeto creado: $servicios")

                lista.add(servicios)
            }

            Log.d("DEBUG_SERVICIOS", "Lista final: $lista")
        } catch (e: Exception) {
            Log.e("DEBUG_SERVICIOS", "Error al obtener servicios de $localdad", e)
        }

        return lista
    }


    suspend fun obtener_datos_servicio_salud(localidad: String, id: String): dataclass_seguridad {
        return try {
            val ref = db.collection("Tiendas")
                .document("salud_seguridad")
                .collection(localidad)
                .document(id)
                .get()
                .await()

            if (!ref.exists()) return dataclass_seguridad()

            val data = ref.data ?: return dataclass_seguridad()
            val ubicacion = data["ubicacion"] as? Map<*, *> ?: emptyMap<String, Any>()
            val contacto = data["numeros_contactos"] as? Map<*, *> ?: emptyMap<String, Any>()

            dataclass_seguridad(
                nombre_          = data["nombre"] as? String ?: "",
                direccion        = ubicacion["direccion"] as? String ?: "",
                latidud          = (ubicacion["latitud"] as? Number ?: 0).toDouble(),
                longitud         = (ubicacion["longitud"] as? Number ?: 0).toDouble(),
                referencia       = ubicacion["referencia"] as? String ?: "",
                categoria        = data["categoria"] as? String ?: "",
                numero_llamada   = contacto["llamada"] as? List<String> ?: emptyList(),
                numero_whatsapp  = contacto["whatsapp"] as? List<String> ?: emptyList(),
                img_ref          = data["img"] as? String ?: "",
                etiqutas_emergencias  = data["tag_eventos_emerge"] as? List<String> ?: emptyList(),
                etiquetas_no_urgente  = data["tag_no_urgentes"] as? List<String> ?: emptyList(),
                key_alias        = data["alias_key"] as? List<String> ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("obtener_datos_servicio_salud", "❌ Error: ${e.message}")
            dataclass_seguridad()
        }
    }

    fun atencion_24h(i: String): String {
        return when (i) {
            "Divpol Barranca" -> "Atencion 24horas (física)"
            "Comisaría PNP Barranca" -> "Atencion 24horas (física)"
            "Diprincri Barranca" -> "Atencion 24horas (física)"
            "Bomberos Voluntarios Barranca" -> "Atencion 24horas (física)"
            "SAMU Barranca" -> "Servicio 24horas "
            "Hospital de Barranca" -> "Atencion 24horas (física)"
            "Serenazgo Municipal Barranca" -> "Operativo 24horas (patrullaje)"
            else -> "Atención (08:00 - 14:00)"
        }

    }


    suspend fun get_numeros(
        localidad: String,
        id_selecionado: String
    ): Triple<List<String>, List<String>, Long> { // 🔹 Ahora devuelve también el tiempo (en ms)
        var lista_llamada: List<String> = emptyList()
        var lista_whatsapp: List<String> = emptyList()
        var tiempoCarga: Long = 0L

        try {
            tiempoCarga = measureTimeMillis {
                val ref = db.collection("Tiendas")
                    .document("salud_seguridad")
                    .collection(localidad)
                    .document(id_selecionado)
                    .get()
                    .await()

                if (ref.exists()) {
                    val data = ref.data
                    val numeros_contactos =
                        data?.get("numeros_contactos") as? Map<String, Any> ?: emptyMap()
                    lista_whatsapp = numeros_contactos["whatsapp"] as? List<String> ?: emptyList()
                    lista_llamada = numeros_contactos["llamada"] as? List<String> ?: emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Triple(lista_llamada, lista_whatsapp, tiempoCarga)
    }


    suspend fun extraerConGemini(textoUsuario: String): String? {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val prompt = construirPromptNLP(textoUsuario)

        val result = model.generateContent(prompt)

        return result.text

    }

    fun generarMensajeVoz(
        nombre_negocio: String,
        cantidad: Int,
        terminos: List<String>,
        tiempo: String?,
        precio: String?, prioridad: String?, tipo: String?
    ): String? {
        return try {
            procesaro_por_vos(nombre_negocio, cantidad, terminos, tiempo, precio, prioridad, tipo)
        } catch (e: Exception) {
            Log.e("IA_VOZ", "Error generando mensaje de voz", e)
            null
        }
    }

    suspend fun cloudTTS(texto: String): ByteArray =
        withContext(Dispatchers.IO) {

            val json = """
        {
          "texto": "$texto"
        }
        """.trimIndent()

            val body = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://us-central1-geinzworkapp.cloudfunctions.net/textToSpeechIA")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Error TTS: ${response.code}")
            }

            response.body?.bytes()
                ?: throw IOException("Audio vacío")
        }


    fun calcularDistanciaBonita(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val radioTierra = 6371000.0 // metros

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2).pow(2.0) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distancia = radioTierra * c // en metros

        return when {
            distancia < 1000 -> "${distancia.roundToInt()} m" // menos de 1 km → metros exactos
            distancia < 10000 -> "${(distancia / 1000).roundToDecimal(2)} km" // 1 km a 10 km → 2 decimales
            else -> "${(distancia / 1000).roundToDecimal(1)} km" // más de 10 km → 1 decimal
        }
    }

    fun distanciaEnMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371000.0
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2).pow(2.0) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radioTierra * c
    }


    fun Double.roundToDecimal(decimales: Int): Double {
        val factor = 10.0.pow(decimales)
        return (this * factor).roundToInt() / factor
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionUsuario(fusedLocationClient: FusedLocationProviderClient): LatLng {
        return suspendCancellableCoroutine { cont ->
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.locations.minByOrNull { it.accuracy }
                    if (loc != null) {
                        cont.resume(LatLng(loc.latitude, loc.longitude)) {}
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMaxUpdates(5)
                .build()

            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

            // Timeout de 10 seg
            Handler(Looper.getMainLooper()).postDelayed({
                if (!cont.isCompleted) {
                    cont.resume(LatLng(0.0, 0.0)) {}
                    fusedLocationClient.removeLocationUpdates(callback)
                }
            }, 10000)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionUsuarioCancelable(
        fusedLocationClient: FusedLocationProviderClient
    ): UbicacionResult = suspendCancellableCoroutine { cont ->
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.locations.minByOrNull { it.accuracy }
                if (loc != null) {
                    cont.resume(UbicacionResult(LatLng(loc.latitude, loc.longitude), this)) {}
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMaxUpdates(5)
            .build()

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        Handler(Looper.getMainLooper()).postDelayed({
            if (!cont.isCompleted) {
                cont.resume(UbicacionResult(LatLng(0.0, 0.0), callback)) {}
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }, 10000)
    }

    fun cancelarUbicacion(
        fusedLocationClient: FusedLocationProviderClient,
        callback: LocationCallback
    ) {
        fusedLocationClient.removeLocationUpdates(callback)
        Log.d("VER_DISTANCIA", "❌ Cancelada la obtención de ubicación")
    }

    suspend fun guardar_lista_entrenamiento(lista: List<FrasePendiente>) {

        val ref = db.collection("Entrenamiento_seguridad")

        lista.forEach { frase ->
            ref.add(frase)
        }
    }


}
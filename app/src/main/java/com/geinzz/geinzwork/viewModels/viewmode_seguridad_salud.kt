package com.geinzz.geinzwork.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.os.Handler

import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_seguridad.EntidadNLP
import com.geinzz.geinzwork.data.model.dataclass_seguridad.RespuestaNLP
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.REQUEST_CALL_PHONE
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isInternetAvailable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class viewmode_seguridad_salud : ViewModel() {


    val instancia = repo_seguridad_salud()
    private val datos_lugares = MutableLiveData<List<dataclass_seguridad>>()
    val _datos_lugares: LiveData<List<dataclass_seguridad>> get() = datos_lugares

    private val _coordenadasSeleccionadas = MutableLiveData<Pair<Double, Double>?>()

    private val _listaFiltrada = MutableStateFlow<List<dataclass_seguridad>>(emptyList())
    val lista_filtrada: StateFlow<List<dataclass_seguridad>> = _listaFiltrada

    private val _state_lista_filtrada = MutableStateFlow<carga_seguidad>(carga_seguidad.loading)
    val state_lista_filtradad: StateFlow<carga_seguidad> = _state_lista_filtrada

    val coordenadasSeleccionadas: LiveData<Pair<Double, Double>?> = _coordenadasSeleccionadas

    private val _mostrar_carga_salud_seguridad = MutableStateFlow(false)
    val mostrar_carga_salud_seguridad = _mostrar_carga_salud_seguridad.asStateFlow()
    private val _lista_entidades = MutableStateFlow<List<EntidadNLP>>(emptyList())
    val lista_general_original_inmutable = MutableStateFlow<List<dataclass_seguridad>>(emptyList())
    var todos_lugares = mutableListOf<dataclass_seguridad>()
        private set
    var estadoBusqueda = mutableStateOf(Estado_busqueda.IDLE)
        private set

    var titulo_mostrado = MutableStateFlow("")
    val texto_mostrado = MutableStateFlow("")
    val mostar_crear_ruta_btn = MutableStateFlow(false)
    var mostrar_whatsapp_ = MutableStateFlow(false)
    var mostrar_llamada = MutableStateFlow(false)
    var lista_whattsapp = MutableStateFlow<List<String>>(emptyList())
    var lista_llamda = MutableStateFlow<List<String>>(emptyList())

    var categoira_filtrado_realziado = MutableStateFlow("")
    var categoira_solo_texto_realizado = MutableStateFlow(false)

    private val _datosCloudTts = MutableStateFlow(ByteArray(0))
    val datosCloudTts: StateFlow<ByteArray> = _datosCloudTts

    private val _ubicacionUsuario = MutableStateFlow<String>("")


    fun cambiar_Estado_carga() {
        estadoBusqueda.value = Estado_busqueda.CARGANDO
    }

    fun retornar_lista_comppleta() {
        _state_lista_filtrada.value = carga_seguidad.succes(lista_general_original_inmutable.value)
    }

    fun obtener_servicios(localidad: String, context: Context) {
        viewModelScope.launch {
            _mostrar_carga_salud_seguridad.value = true
            _state_lista_filtrada.value = carga_seguidad.loading
            delay(2000)
            try {
                if (!isInternetAvailable(context)) {
                    _mostrar_carga_salud_seguridad.value = false
                    _state_lista_filtrada.value = carga_seguidad.error("Sin conexión a internet 😕")
                    return@launch
                }
                val respuesta = instancia.obtener_servicios_salud(localidad)
                datos_lugares.value = respuesta
                lista_general_original_inmutable.value = respuesta
                if (respuesta.isNotEmpty()) {
                    _mostrar_carga_salud_seguridad.value = false
                    _state_lista_filtrada.value = carga_seguidad.succes(respuesta)
                    generarEntidadesNLP(respuesta)

                } else {
                    delay(300)
                    _mostrar_carga_salud_seguridad.value = false
                    _state_lista_filtrada.value =
                        carga_seguidad.empity("No se encontraron resultados en $localidad")
                }
            } catch (e: Exception) {
                _mostrar_carga_salud_seguridad.value = false
                datos_lugares.value = emptyList()
                _state_lista_filtrada.value = carga_seguidad.error("Error al cargar los datos")

            }
        }
    }

    fun setCoordenadas(lat: Double, lon: Double) {
        _coordenadasSeleccionadas.value = lat to lon
    }

    fun lugares_iniciales(lista: List<dataclass_seguridad>) {
        todos_lugares.clear()
        todos_lugares.addAll(lista)
    }

    fun horario_atencion(nombre: String): String {
        return instancia.atencion_24h(nombre)
    }

    fun filtrar_lugares(
        categoria: String,
    ) {
        viewModelScope.launch {
            _state_lista_filtrada.value = carga_seguidad.loading
            try {
                val resultado = if (categoria == "Todos") {
                    todos_lugares
                } else {
                    todos_lugares.filter {
                        it.categoria.lowercase().contains(categoria.lowercase())
                    }
                }

                if (resultado.isNotEmpty()) {
                    _state_lista_filtrada.value = carga_seguidad.succes(resultado)
                } else {
                    _state_lista_filtrada.value =
                        carga_seguidad.empity("No se encontraron resutlados")
                }
            } catch (e: Exception) {
                _state_lista_filtrada.value = carga_seguidad.empity("No se encontraron resutlados")

            }
        }
    }

    fun lista_base_completa(categorias: String) {
        viewModelScope.launch {
            if (categorias == "Todos" && todos_lugares.isNotEmpty()) {
                _state_lista_filtrada.value = carga_seguidad.succes(todos_lugares)
                return@launch
            }
        }
    }

    fun filtrar_nombre_categoria(
        nombre: String,
        categoria: String,
        lista: List<dataclass_seguridad>
    ) {
        viewModelScope.launch {
            try {
                _state_lista_filtrada.value = carga_seguidad.loading
                val res = lista.filter { item ->
                    val textoCoincide = item.nombre_.contains(nombre, ignoreCase = true)
                    val categoriaCoincide = categoria == "Todos" || item.categoria == categoria
                    textoCoincide && categoriaCoincide
                }
                if (res.isNotEmpty()) {
                    _state_lista_filtrada.value = carga_seguidad.succes(res)

                } else {
                    _state_lista_filtrada.value =
                        carga_seguidad.empity("No se encontraron resultados")

                }
            } catch (e: Exception) {
                _state_lista_filtrada.value = carga_seguidad.error("Error al filtrar datos")

            }
        }
    }


    fun preguntar_gemini(
        texto: String,
        context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        viewModelScope.launch {
            try {
                val respues = instancia.extraerConGemini(texto)
                Log.d("GEMINI", respues ?: "respuesta nula")
                procesarRespuestaGemini(respues ?: "", context, texto, fusedLocationClient)
            } catch (e: Exception) {
                Log.d("GEMINI", "$e")
            }
        }

    }

    fun limpiarRespuestaGemini(texto: String): String {
        return texto
            .replace("```json", "")
            .replace("```", "")
            .trim()
    }

    fun parsearRespuesta(json: String): RespuestaNLP? {
        return try {
            Gson().fromJson(json, RespuestaNLP::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun procesarRespuestaGemini(
        raw: String,
        context: Context,
        texto_origina: String,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        if (raw != "") {

            val limpio = limpiarRespuestaGemini(raw)
            val respuesta = parsearRespuesta(limpio)

            if (respuesta == null) {
                Log.e("NLP", "JSON inválido")
                return
            }
            clasificarAccionDebug(respuesta, context, texto_origina, fusedLocationClient)
        }
    }


    fun clasificarAccionDebug(
        r: RespuestaNLP,
        context: Context,
        texto_origina: String,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        Log.d("NLP_DEBUG2", "Respuesta completa → $r")
        Log.d("NLP_DEBUG2", "Acción → ${r.a}")
        Log.d("NLP_DEBUG2", "Término → ${r.t}")
        Log.d("NLP_DEBUG2", "Confianza → ${r.c}")
        Log.d("NLP_DEBUG2", "$texto_origina")

        // Resolvemos el término usando la lista de entidades del ViewModel

        val esEmergenciaTexto = esEmergencia(texto_origina)

        val entidad = resolverEntidad(r.t, _lista_entidades.value)
        if (entidad != null) {
            Log.d("NLP_DEBUG2", "Entidad resuelta → ${entidad.key} (alias: ${entidad.alias})")
        } else {
            Log.d("NLP_DEBUG2", "Entidad no encontrada para '${r.t}'")
        }


        // Ahora hacemos debug de la acción
        when (r.a) {
            "asistencia" -> Log.d("NLP_DEBUG2", "Detectado: ASISTENCIA / EMERGENCIA")
            "info" -> {
                Log.d("NLP_DEBUG2", "Detectado: INFO sobre '${entidad?.key ?: r.t}'")
                retornarInformacion(
                    lista_general_original_inmutable.value,
                    entidad?.key ?: r.t,
                    texto_origina
                )
                estadoBusqueda.value = Estado_busqueda.IDLE

            }

            "ruta" -> {
                Log.d("NLP_DEBUG2", "Detectado: RUTA hacia '${entidad?.key ?: r.t}'")
                cloudTTS("Creando ruta hacia ${entidad?.key ?: r.t}")
                retornar_cordenadas(
                    lista_general_original_inmutable.value,
                    entidad?.key ?: r.t
                )?.let { (lat, lng) ->
                    // Aquí lat y lng están disponibles
                    println("Latitud: $lat, Longitud: $lng")
                    constantes_lista_localidades.abrir_google_maps(
                        "", "emergencia", "", "",
                        context = context,
                        lat, lng
                    ) {
                    }
                } ?: run {
                    // Si no se encontró la entidad
                    println("No se encontraron coordenadas")
                }
                estadoBusqueda.value = Estado_busqueda.IDLE
            }

            "llamar" -> {
                // 🔥 EMERGENCIA detectada aunque NO haya entidad
                if (esEmergenciaTexto && entidad == null) {
                    val mensaje = numerosPorEmergencia(texto_origina)
                    Log.d("NLP_DEBUG2", "Emergencia detectada por similitud")
                    cloudTTS(mensaje)
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    return
                }

                // 📞 Llamada normal
                if (entidad != null) {
                    val texto = generarTextoLLamada(
                        context,
                        lista_general_original_inmutable.value,
                        entidad.key
                    )
                    cloudTTS(texto)
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    return
                }


                cloudTTS("¿A quién deseas llamar?")
                estadoBusqueda.value = Estado_busqueda.IDLE
            }

            "whatsapp" -> {
                val numero = retornar_numero_whatsapp(
                    lista_general_original_inmutable.value,
                    entidad?.key ?: r.t
                )
                numero?.let {
                    println("Número seleccionado: $it")
                    abrir_whattsapp(context, it, "")
                    estadoBusqueda.value = Estado_busqueda.IDLE
                } ?: run {
                    println("No se encontró número de WhatsApp")
                    estadoBusqueda.value = Estado_busqueda.IDLE
                }

                Log.d("NLP_DEBUG2", "Detectado: WHATSAPP a '${entidad?.key ?: r.t}'")
//                val texto=generarTextoContacto(lista_general_original_inmutable.value,entidad?.key ?: r.t)
                cloudTTS("Abriendo Whattsapp")
                estadoBusqueda.value = Estado_busqueda.IDLE
            }

            "dar_numero" -> {

                if (esEmergenciaTexto && entidad == null) {
                    val mensaje = numerosPorEmergencia(texto_origina)
                    cloudTTS(mensaje)
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    return
                }

                if (entidad != null) {
                    val numero = retornar_numero_whatsapp(
                        lista_general_original_inmutable.value,
                        entidad.key
                    )
                    numero?.let {
                        cloudTTS("claro el número de ${entidad.key} es $it")
                        titulo_mostrado.value = "Aqui tienes tus resultados"
                        texto_mostrado.value = "claro el número de ${entidad.key} es $it"
                    } ?: cloudTTS("No se encontró el número")
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    return
                }

                cloudTTS("¿De qué institución necesitas el número?")

                texto_mostrado.value = "¿De qué institución necesitas el número?"
                estadoBusqueda.value = Estado_busqueda.IDLE
            }

            "buscar" -> {
                val busqNormalizado = normalizar(r.t)
                Log.d("NLP_DEBUG2", "Detectado: BUSCAR '${r.t}'")
                val resultadosFiltrados = filtrarEntidadesPorBusquedaNLP(
                    lista_general_original_inmutable.value,
                    _lista_entidades.value,
                    r.t
                )
                if (resultadosFiltrados.isNotEmpty()) {
                    _state_lista_filtrada.value = carga_seguidad.succes(resultadosFiltrados)
                    cloudTTS("Resultados filtrados para ${entidad?.key ?: r.t}")
                    texto_mostrado.value = "Resultados filtrados para ${entidad?.key ?: r.t}"
                } else {
                    _state_lista_filtrada.value =
                        carga_seguidad.empity("No se encontraron resultados para ${r.t}")
                    cloudTTS("No se encontraron resultados para ${r.t}")
                    texto_mostrado.value = "No se encontraron resultados para ${r.t} :("
                }

                if (busqNormalizado == "seguridad" || busqNormalizado == "salud") {
                    categoira_filtrado_realziado.value = busqNormalizado
                    categoira_solo_texto_realizado.value = false
                } else {
                    categoira_solo_texto_realizado.value = true
                    categoira_filtrado_realziado.value = "Todos"
                }

                estadoBusqueda.value = Estado_busqueda.IDLE
            }

            "distancia" -> {
                Log.d("NLP_DEBUG2", "distancia  '${entidad?.key ?: r.t}'")
                viewModelScope.launch {
                    val distancia = ver_distancia_lugar(
                        fusedLocationClient,
                        lista_general_original_inmutable.value,
                        r.t
                    )
                    texto_mostrado.value = "${distancia}"
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    cloudTTS("${distancia} ")
                }
            }

            "desconocido" ->  {
                cloudTTS("Losiento no entendi lo que me trataste de decir")
                texto_mostrado.value = "Losiento no entendi lo que me trataste de decir"
            }


            else -> {
                Log.w("NLP_DEBUG2", "Acción no soportada: ${r.a}")
                cloudTTS("No pude encontrar exactamente lo que buscabas, pero mantén la calma. Recuerda que Geinz siempre está contigo ante cualquier emergencia.")
                texto_mostrado.value =
                    "No pude encontrar exactamente lo que buscabas, pero mantén la calma. Recuerda que Geinz siempre está contigo ante cualquier emergencia."

                estadoBusqueda.value = Estado_busqueda.IDLE
            }
        }
    }

    fun mensajeFallbackEmergencia(): String {
        return "Tranquilo. Si estás en una emergencia, mantén la calma. Puedes decirme por ejemplo: llama a la policía, samu ,hospital o bomberos."
    }


    fun numerosPorEmergencia(texto: String): String {
        val t = texto.lowercase()
        return when {
            listOf("fuego", "incendio").any { it in t } ->
                "Llama al 116 Bomberos"

            listOf("dolor", "se cayó", "me siento mal", "sangre").any { it in t } ->
                "Llama al 106 SAMU"

            listOf("robo", "asalt", "me siguen").any { it in t } ->
                "Llama al 105 Policía"

            else ->
                "Llama al 105 Policía, 116 Bomberos o 106 SAMU"
        }
    }


    fun formatearNumerosConAlYNumeros(numeros: List<String>): String {
        return when (numeros.size) {
            0 -> "No disponible"
            1 -> "al ${numeros[0]}"
            else -> numeros.dropLast(1).joinToString(", ") { "al $it" } + " o al ${numeros.last()}"
        }
    }


    fun generarTextoContacto(lista: List<dataclass_seguridad>, nombreBuscado: String): String {
        val entidad = lista.firstOrNull {
            normalizar(it.nombre_).contains(normalizar(nombreBuscado))
        } ?: return "No se encontró información para '$nombreBuscado'"

        val whatsapps = formatearNumerosConAlYNumeros(entidad.numero_whatsapp)

        val plantillas = listOf(
            "Hola, puedes comunicarte con ${entidad.nombre_} $whatsapps vía WhatsApp",
            "Si deseas, contacta a ${entidad.nombre_} $whatsapps en WhatsApp",
            "Para hablar con ${entidad.nombre_}, escribe $whatsapps en WhatsApp",
            "Necesitas algo de ${entidad.nombre_}? WhatsApp $whatsapps",
            "Conecta con ${entidad.nombre_} $whatsapps en WhatsApp"
        )

        return plantillas.random()
    }

    fun filtrarEntidadesPorBusquedaNLP(
        entidades1: List<dataclass_seguridad>,  // lista original con datos
        entidades: List<EntidadNLP>,            // lista de key + alias
        textoBusqueda: String
    ): List<dataclass_seguridad> {

        val busqNormalizado = normalizar(textoBusqueda)

        // 🔥 CASO ESPECIAL: seguridad o salud
        if (busqNormalizado == "seguridad" || busqNormalizado == "salud") {
            return entidades1.filter { dat ->
                normalizar(dat.categoria) == busqNormalizado
            }
        }

        // -----------------------------------------
        // 🔹 CASO NORMAL (por nombre)
        // -----------------------------------------

        val entidadesCoincidentes = entidades.filter { entidad ->
            val keyNormalizada = normalizar(entidad.key)
            val aliasNormalizados = entidad.alias.map { normalizar(it) }

            keyNormalizada.contains(busqNormalizado) ||
                    aliasNormalizados.any { it.contains(busqNormalizado) }
        }

        val keysCoincidentes = entidadesCoincidentes.map { it.key }

        return entidades1.filter { dat ->
            keysCoincidentes.any {
                dat.nombre_.contains(it, ignoreCase = true)
            }
        }
    }


    fun generarTextoLLamada(context: Context,lista: List<dataclass_seguridad>, nombreBuscado: String): String {
        Log.d("GENERAR_CONTACTO", "Buscando entidad para: '$nombreBuscado'")

//        val entidad = lista.firstOrNull {
//            it.nombre_.contains(nombreBuscado, ignoreCase = true).also { encontrado ->
//                Log.d("GENERAR_CONTACTO", "Revisando '${it.nombre_}', coincidencia: $encontrado")
//            }
//        } ?: return "No se encontró información para '$nombreBuscado'".also {
//            Log.d("GENERAR_CONTACTO", it)
//        }
        val entidad = lista.firstOrNull {
            normalizar(it.nombre_).contains(normalizar(nombreBuscado))
        } ?: return "No se encontró información para '$nombreBuscado'"

        Log.d("GENERAR_CONTACTO", "Entidad encontrada: ${entidad.nombre_}")

        val llamadas = formatearNumerosConAlYNumeros(entidad.numero_llamada)
        Log.d("GENERAR_CONTACTO", "Números de llamada formateados:${formatearParaTTS(llamadas)}")
        var mensaje =""
//        val plantillas = listOf(
//            "Hola, puedes llamar a ${entidad.nombre_} ${formatearParaTTS(llamadas)}",
//            "Si quieres contactar a ${entidad.nombre_}, llama ${formatearParaTTS(llamadas)}",
//            "Necesitas hablar con ${entidad.nombre_}? ${formatearParaTTS(llamadas)}",
//            "Para comunicarte con ${entidad.nombre_}, usa ${formatearParaTTS(llamadas)}",
//            "Marca a ${entidad.nombre_} ${formatearParaTTS(llamadas)}"
//        )
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mensaje="Neecsitas activar el permiso"
        } else {
            mensaje="LLamando"
            makePhoneCall(context, entidad.numero_llamada.first())
        }

        Log.d("GENERAR_CONTACTO", "Mensaje generado: $mensaje")
        return mensaje
    }

    fun retornar_cordenadas(
        lista: List<dataclass_seguridad>,
        nombreBuscado: String
    ): Pair<Double, Double>? {
        val entidad = lista.firstOrNull {
            it.nombre_.contains(nombreBuscado, ignoreCase = true).also { encontrado ->
                Log.d("GENERAR_CONTACTO", "Revisando '${it.nombre_}', coincidencia: $encontrado")
            }
        }

        return if (entidad != null) {
            val latitud = entidad.latidud
            val longitud = entidad.longitud
            Log.d("GENERAR_CONTACTO", "Latitud: $latitud, Longitud: $longitud")
            Pair(latitud, longitud)
        } else {
            Log.d("GENERAR_CONTACTO", "No se encontró información para '$nombreBuscado'")
            null
        }
    }

    fun retornar_numero_whatsapp(lista: List<dataclass_seguridad>, nombreBuscado: String): String? {
        val entidad = lista.firstOrNull {
            it.nombre_.contains(nombreBuscado, ignoreCase = true).also { encontrado ->
                Log.d("GENERAR_CONTACTO", "Revisando '${it.nombre_}', coincidencia: $encontrado")
            }
        }

        return if (entidad != null && entidad.numero_whatsapp.isNotEmpty()) {
            val numeroAleatorio = entidad.numero_whatsapp.random()
            Log.d(
                "GENERAR_CONTACTO",
                "Número WhatsApp elegido: ${formatearParaTTS(numeroAleatorio)}"
            )
            numeroAleatorio
        } else {
            Log.d("GENERAR_CONTACTO", "No se encontró número de WhatsApp para '$nombreBuscado'")
            null
        }
    }

    fun detectarTipoInfo(texto: String): String {
        val t = texto.lowercase().trim()

        return when {
            listOf(
                "dónde",
                "direccion",
                "ubicación",
                "lugar",
                "queda",
                "dirección",
                "calle",
                "referencia"
            ).any { it in t } -> "direccion"

            listOf(
                "teléfono",
                "llamar",
                "contacto",
                "comunicar",
                "número"
            ).any { it in t } -> "telefono"

            listOf(
                "horario",
                "hora",
                "atención",
                "abierto",
                "abre",
                "cierra"
            ).any { it in t } -> "horario"

            listOf(
                "qué es",
                "información",
                "descripción",
                "quién",
                "servicio"
            ).any { it in t } -> "descripcion"

            else -> "general"
        }
    }


    fun retornarInformacion(
        lista: List<dataclass_seguridad>,
        nombreBuscado: String,
        textoUsuario: String
    ) {
        // Buscamos la entidad en la lista original
        val entidad = lista.firstOrNull {
            it.nombre_.contains(nombreBuscado, ignoreCase = true)
        }

        if (entidad == null) {
            cloudTTS("No se encontró información para '$nombreBuscado'")
            return
        }

        // Detectamos qué tipo de info quiere el usuario
        val tipoInfo = detectarTipoInfo(textoUsuario)

        // Construimos el mensaje según el tipo
        val mensaje = when (tipoInfo) {
            "direccion" -> "La dirección de ${entidad.nombre_} es: ${entidad.direccion ?: "no disponible"} y alguna referencia es: ${entidad.referencia ?: "no disponible"}"
            "telefono" -> "Puedes comunicarte al teléfono ${entidad.numero_llamada.joinToString(", ")}"
            "horario" -> "El horario de atención de ${entidad.nombre_} es: ${
                horario_atencion(
                    entidad.nombre_
                )
            }"

            else -> "Aquí te damos información sobre ${entidad.nombre_}"
        }

        cloudTTS(mensaje)
    }


    fun normalizar(texto: String): String {
        val sinAcentos = texto
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
        return sinAcentos.lowercase().trim()
    }

    fun formatearParaTTS(numero: String): String {
        val soloDigitos = numero.filter { it.isDigit() }

        // Separamos cada dígito con un espacio para que TTS los lea individualmente
        val formateado = soloDigitos.toCharArray().joinToString(" ")

        Log.d("FORMATEAR_TTS", "Número formateado para TTS: $formateado")
        return formateado
    }


    fun resolverEntidad(t: String, entidades: List<EntidadNLP>): EntidadNLP? {
        val tNormalizado = normalizar(t)

        return entidades.firstOrNull { entidad ->
            val keyNormalizada = normalizar(entidad.key)
            val aliasNormalizados = entidad.alias.map { normalizar(it) }

            keyNormalizada.contains(tNormalizado) || aliasNormalizados.any {
                it.contains(
                    tNormalizado
                )
            }
        }
    }


    suspend fun tranformar_texto_a_voz(audioData: ByteArray): String {
        return withContext(Dispatchers.IO) {
            val base64 = Base64.encodeToString(audioData, Base64.NO_WRAP)
            val json = JSONObject().put("audio", base64).toString()
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://us-central1-geinzworkapp.cloudfunctions.net/recognizeSpeech")
                .post(body)
                .build()
            val response = OkHttpClient().newCall(request).execute()
            val recognized = JSONObject(response.body?.string().orEmpty())
                .optString("text", "")
                .trim()
            recognized
        }
    }

    fun reproducirMP3(context: Context, audioBytes: ByteArray) {
        try {
            // Verificar que haya datos
            if (audioBytes.isEmpty()) {
                Log.e("TTS", "Audio vacío, no se puede reproducir")
                return
            }

            // Crear archivo temporal
            val tempFile = File.createTempFile("tts_", ".mp3", context.cacheDir)
            tempFile.writeBytes(audioBytes)

            // Configurar MediaPlayer
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            // Liberar y borrar archivo cuando termine
            mediaPlayer.setOnCompletionListener {
                it.release()
                tempFile.delete()
            }

            // También liberar si hay error
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("TTS", "Error en MediaPlayer: $what / $extra")
                mp.release()
                tempFile.delete()
                true
            }

        } catch (e: Exception) {
            Log.e("TTS", "Error reproduciendo MP3", e)
        }
    }

    fun cloudTTS(texto: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _datosCloudTts.value = instancia.cloudTTS(texto)
            } catch (e: Exception) {
                Log.e("CloudTTS", "Error de text to speech", e)
            }
        }
    }

    fun generarEntidadesNLP(lista: List<dataclass_seguridad>) {
        val entidadesGeneradas = lista.map { item ->
            Log.d("NLP_DEBUG", "Procesando: ${item.nombre_}")

            val limpio = normalizarNombre(item.nombre_)
            var alias = generarAliasPorTipo(limpio).toMutableList()

            // 🔥 Añadir key como alias si no hay ninguno
            if (alias.isEmpty()) alias.add(limpio)

            Log.d("NLP_DEBUG", "limpio=$limpio alias=$alias")

            EntidadNLP(limpio, alias)
        }

        _lista_entidades.value = entidadesGeneradas
    }


    fun normalizarNombre(nombre: String): String {
        return nombre
            .lowercase()
            .replace("barranca", "")
            .replace("divpol", "divpol")
            .replace("dipol", "dipol")
            .replace("  ", " ")
            .trim()
    }

    fun generarAliasPorTipo(nombre: String): List<String> {
        return when {
            nombre.contains("samu") ->
                listOf("samu", "ambulancia")

            nombre.contains("serenazgo") ->
                listOf("serenazgo", "seren")

            nombre.contains("bomberos") ->
                listOf("bomberos", "fuego")

            nombre.contains("hospital") ->
                listOf("hospital", "clinica")

            nombre.contains("comisaria") ->
                listOf("comisaria", "policia")

            nombre.contains("dipol") || nombre.contains("divpol") ->
                listOf("dipol", "policia")

            nombre.contains("depincri") ->
                listOf("depincri", "investigacion")

            else -> emptyList()
        }
    }

    val palabrasEmergencia = listOf(

        // 🔥 INCENDIOS / EXPLOSIONES
        "fuego", "incendio", "explosión", "exploto", "humo", "quemándose", "se quema",
        "gas", "fuga de gas", "olor a gas",

        // 🚑 SALUD / ACCIDENTES
        "me siento mal", "me desmayé", "me desmaye", "me caí", "se cayó", "se ha caído",
        "dolor", "dolor fuerte", "dolor intenso", "sangre", "sangrando", "hemorragia",
        "no respira", "no puede respirar", "me falta el aire", "ahogo", "ahogando",
        "ataque", "ataque al corazón", "infarto", "derrame", "convulsión", "convulsionando",
        "fiebre alta", "inconsciente", "perdió el conocimiento",
        "fractura", "hueso roto", "accidente",

        // 🚓 DELITOS / PELIGRO
        "auxilio", "ayuda", "socorro",
        "me siguen", "me están siguiendo", "me persiguen",
        "robo", "asalto", "asaltaron", "me robaron", "me están robando",
        "se metieron a mi casa", "allanamiento",
        "arma", "armado", "disparo", "balazo", "tiroteo",
        "matar", "amenaza", "peligro", "violencia", "agresión",
        "secuestro", "rapto",

        // 🚗 ACCIDENTES VIALES
        "choque", "accidente de tránsito", "atropello", "atropellaron",
        "volcadura", "choqué", "me chocaron",

        // 🌊 DESASTRES / RIESGO
        "inundación", "inundado", "huaico",
        "terremoto", "sismo", "temblor",
        "derrumbe", "deslizamiento",
        "colapso", "se cayó el techo",

        // ⚠️ GENÉRICO
        "emergencia", "urgente", "grave", "pasa algo malo"
    )


    fun esEmergencia(texto: String): Boolean {
        val textoLimpio = texto.lowercase()

        val tokens = textoLimpio
            .replace(Regex("[^a-záéíóúñ ]"), "")
            .split("\\s+".toRegex())
            .filter { it.length > 2 }

        Log.d("EMERGENCIA_DEBUG", "Tokens detectados: $tokens")

        for (palabraEmergencia in palabrasEmergencia) {
            val peTokens = palabraEmergencia.split(" ")

            // 🔥 Coincidencia directa por palabra o frase
            if (peTokens.all { token ->
                    tokens.any { it == token || it.contains(token) }
                }
            ) {
                Log.d(
                    "EMERGENCIA_DEBUG",
                    "🚨 MATCH emergencia → '$palabraEmergencia'"
                )
                return true
            }
        }

        Log.d("EMERGENCIA_DEBUG", "❌ No se detectó emergencia")
        return false
    }


    fun similitudTexto(texto: String, palabra: String): Float {
        if (texto.contains(palabra)) return 1f

        val palabrasTexto = texto.split(" ")
        var coincidencias = 0

        for (p in palabrasTexto) {
            if (palabra.contains(p) || p.contains(palabra)) {
                coincidencias++
            }
        }

        return coincidencias.toFloat() / palabra.split(" ").size.coerceAtLeast(1)
    }

    suspend fun ver_distancia_lugar(
        fusedLocationClient: FusedLocationProviderClient,
        value: List<dataclass_seguridad>,
        t: String
    ): String {
        Log.d("VER_DISTANCIA", "🔍 Buscando coincidencia para: '$t' en la lista de lugares")

        // Buscamos el lugar que coincida
        val lugar = value.firstOrNull { it.nombre_.contains(t, ignoreCase = true) }
        if (lugar == null) {
            Log.d("VER_DISTANCIA", "❌ No se encontró ningún lugar que coincida con '$t'.")
            return "Lo siento, no pude calcular la distancia en este momento 😔"
        }

        val lat_lugar = lugar.latidud
        val lng_lugar = lugar.longitud

        // Validamos que el lugar tenga coordenadas válidas
        if (lat_lugar == 0.0 || lng_lugar == 0.0) {
            Log.d("VER_DISTANCIA", "⚠️ La entidad '${lugar.nombre_}' no tiene ubicación física registrada.")
            return "Esta entidad '${lugar.nombre_}' no cuenta con ubicación física, solo se puede acceder directamente."
        }

        Log.d("VER_DISTANCIA", "✅ Lugar encontrado: ${lugar.nombre_}")
        Log.d("VER_DISTANCIA", "   Lat: $lat_lugar, Lng: $lng_lugar")

        return try {
            Log.d("VER_DISTANCIA", "🌐 Obteniendo ubicación del usuario...")

            // Obtenemos la ubicación junto con el callback
            val datosConCallback = instancia.obtenerUbicacionUsuarioCancelable(fusedLocationClient)
            val datos = datosConCallback.latLng

            Log.d("VER_DISTANCIA", "📍 Ubicación del usuario obtenida")
            Log.d("VER_DISTANCIA", "   Lat=${datos.latitude}, Lng=${datos.longitude}")

            // Cancelamos la obtención de ubicación para no seguir recibiendo updates
            instancia.cancelarUbicacion(fusedLocationClient, datosConCallback.callback)
            Log.d("VER_DISTANCIA", "❌ Cancelada la obtención de ubicación")

            // Validamos ubicación
            if (datos.latitude == 0.0 || datos.longitude == 0.0) {
                Log.d("VER_DISTANCIA", "⚠️ No se pudo obtener ubicación física del usuario")
                return "No se pudo obtener tu ubicación actual para calcular la distancia 😔"
            }

            // Distancia pura en metros
            val recorrido_puro = instancia.distanciaEnMetros(
                datos.latitude,
                datos.longitude,
                lat_lugar,
                lng_lugar
            )

            // Distancia bonita
            val distanciaBonita = instancia.calcularDistanciaBonita(
                datos.latitude,
                datos.longitude,
                lat_lugar,
                lng_lugar
            )
            Log.d("VER_DISTANCIA", "📏 Distancia calculada: $distanciaBonita")

            // Estimación de tiempo
            val tiempoPie = estimarTiempo(recorrido_puro, 1.4)
            val tiempoBici = estimarTiempo(recorrido_puro, 3.0)
            val tiempoAuto = estimarTiempo(recorrido_puro, 8.3)

            Log.d("VER_DISTANCIA", "⏱ Tiempo estimado a pie: $tiempoPie")
            Log.d("VER_DISTANCIA", "⏱ Tiempo estimado en bici: $tiempoBici")
            Log.d("VER_DISTANCIA", "⏱ Tiempo estimado en auto: $tiempoAuto")

            // Texto final
            "Estás aproximadamente a $distanciaBonita ($tiempoPie a pie, $tiempoBici en bici, $tiempoAuto en auto, dependiendo del tráfico) de ${lugar.nombre_}"

        } catch (e: Exception) {
            Log.d("VER_DISTANCIA", "⚠️ Error al obtener ubicación o calcular distancia: $e")
            "Lo siento, tuve un error al calcular la distancia 😔"
        }

    }


    fun estimarTiempo(distanciaMetros: Double, velocidadMS: Double): String {
        val tiempoSeg = distanciaMetros / velocidadMS
        val horas = (tiempoSeg / 3600).toInt()
        val minutos = ((tiempoSeg % 3600) / 60).toInt()
        return when {
            horas > 0 -> "${horas}horas ${minutos}minutos"
            minutos > 0 -> "${minutos} minutos"
            else -> "<1 minutos"
        }
    }
    fun requestCallPermission(llamar: Boolean = true, context: Context, phoneNumber: String = "") {
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
            if (llamar) {
                makePhoneCall(context, phoneNumber)
            }
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
            requestCallPermission(context = context, phoneNumber = phoneNumber)
        }
    }


    enum class EstadoMic {
        IDLE,        // mic normal
        GRABANDO,    // visualizador
        ENVIANDO     // progress
    }

    enum class Estado_busqueda {
        IDLE,
        BUSCANDO,
        CARGANDO
    }

    sealed class carga_seguidad {
        data class empity(val texto: String) : carga_seguidad()
        data class succes(val list: List<dataclass_seguridad>) : carga_seguidad()
        data class error(val texto: String) : carga_seguidad()
        object loading : carga_seguidad()
    }


}
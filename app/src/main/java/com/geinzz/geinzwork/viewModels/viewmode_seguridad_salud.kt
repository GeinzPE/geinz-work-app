package com.geinzz.geinzwork.viewModels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest

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
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.google.android.gms.location.FusedLocationProviderClient
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

    var nombre_user =MutableStateFlow("")

    fun nombre_user (nombre:String){
        nombre_user.value=nombre
    }


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
    val callDialogPermise = MutableStateFlow(Pair(false, ""))

    val _mostrar_micro = MutableStateFlow(true)


    fun cabiar_valor_mostara_micro(valor: Boolean) {
        _mostrar_micro.value = valor
    }


    fun limpiarEstado() {

        titulo_mostrado.value = ""
        texto_mostrado.value = ""

        estadoBusqueda.value = Estado_busqueda.IDLE
        _datosCloudTts.value = ByteArray(0)

        callDialogPermise.value = Pair(false, "")

        categoira_filtrado_realziado.value = "Todos"
        categoira_solo_texto_realizado.value = false
    }

    fun cambiar_estado_valor_calldialog() {
        callDialogPermise.value = Pair(false, "")
    }


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
        fusedLocationClient: FusedLocationProviderClient,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        permisoLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        viewModelScope.launch {
            try {
                val respues = instancia.extraerConGemini(texto)
                Log.d("GEMINI", respues ?: "respuesta nula")
                procesarRespuestaGemini(
                    respues ?: "",
                    context,
                    texto,
                    fusedLocationClient,
                    launcher, permisoLauncher
                )
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
        fusedLocationClient: FusedLocationProviderClient,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        permisoLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        if (raw != "") {

            val limpio = limpiarRespuestaGemini(raw)
            val respuesta = parsearRespuesta(limpio)

            if (respuesta == null) {
                Log.e("NLP", "JSON inválido")
                return
            }
            clasificarAccionDebug(
                respuesta,
                context,
                texto_origina,
                launcher,
                fusedLocationClient,
                permisoLauncher
            )
        }
    }


    fun clasificarAccionDebug(
        r: RespuestaNLP,
        context: Context,
        texto_origina: String,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        fusedLocationClient: FusedLocationProviderClient,
        permisoLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        Log.d("NLP_DEBUG2", "Respuesta completa → $r")
        Log.d("NLP_DEBUG2", "Acción → ${r.a}")
        Log.d("NLP_DEBUG2", "Término → ${r.t}")
        Log.d("NLP_DEBUG2", "Confianza → ${r.c}")
        Log.d("NLP_DEBUG2", "es_salud_o_seguridad → ${r.g}")
        Log.d("NLP_DEBUG2", "$texto_origina")

        // Resolvemos el término usando la lista de entidades del ViewModel

//        val esEmergenciaTexto = esEmergencia(texto_origina)

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
                val respuesta = retornarInformacion(
                    lista_general_original_inmutable.value,
                    entidad?.key ?: r.t,
                    texto_origina
                )
                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
                verificarGPS(context, launcher)
                cloudTTS(respuesta)
                texto_mostrado.value = respuesta
            }

            "ruta" -> {
                Log.d("NLP_DEBUG2", "Detectado: RUTA hacia '${entidad?.key ?: r.t}'")

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
                    ) { sin_permisos ->
                        if (sin_permisos) {
                            verificarGPS(context, launcher)
                            cloudTTS("Activa tu ubicacion para poder crearte una ruta hacia ${entidad?.key ?: r.t}")
                            titulo_mostrado.value = "Activa tus permisos"
                            texto_mostrado.value =
                                "Activa tu ubicacion para poder crearte una ruta hacia ${entidad?.key ?: r.t}"
                        } else {
                            cloudTTS("Creando ruta hacia ${entidad?.key ?: r.t}")
                            titulo_mostrado.value = "Creando ruta"
                            texto_mostrado.value = "Creando ruta hacia ${entidad?.key ?: r.t}"
                        }
                    }
                } ?: run {
                    // Si no se encontró la entidad
                    cloudTTS("Losiento no entendi lo que me quiziste decir")
                }
                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
            }

            "llamar" -> {

                // 📞 Llamada normal
                if (entidad != null) {
                    val texto = generarTextoLLamada(
                        context,
                        lista_general_original_inmutable.value,
                        entidad.key, { numero ->
                            callDialogPermise.value = Pair(true, numero)
                        }
                    )
                    titulo_mostrado.value = "Aqui tienes tus resultados"
                    texto_mostrado.value = "Llamando a ${entidad.key} "
                    cloudTTS(texto)
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    _mostrar_micro.value = true
                    return
                }


                cloudTTS("¿A quién deseas llamar?")
                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
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
                    _mostrar_micro.value = true
                } ?: run {
                    println("No se encontró número de WhatsApp")

                    cloudTTS("No se encontró número de WhatsApp ${entidad?.key ?: r.t} ")
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    _mostrar_micro.value = true
                }

                cloudTTS("Abriendo Whattsapp")
                titulo_mostrado.value = "Abriendo Whattsapp"
                texto_mostrado.value = "Abriendo Whattsapp"
                Log.d("NLP_DEBUG2", "Detectado: WHATSAPP a '${entidad?.key ?: r.t}'")
//                val texto=generarTextoContacto(lista_general_original_inmutable.value,entidad?.key ?: r.t)
                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
            }

            "dar_numero" -> {

                if (entidad != null) {
                    val numero = retornar_numero_whatsapp(
                        lista_general_original_inmutable.value,
                        entidad.key
                    )
                    numero?.let {
                        cloudTTS("claro el número de ${entidad.key} es ${formatearParaTTS(it)}")
                        titulo_mostrado.value = "Aqui tienes tus resultados"
                        texto_mostrado.value = "claro el número de ${entidad.key} es $it"
                    } ?: cloudTTS("No se encontró el número")
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    _mostrar_micro.value = true
                    return
                }

                cloudTTS("¿De qué institución necesitas el número?")

                texto_mostrado.value = "¿De qué institución necesitas el número?"
                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
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
                _mostrar_micro.value = true
            }

            "distancia" -> {
                Log.d("NLP_DEBUG2", "distancia  '${entidad?.key ?: r.t}'")
                viewModelScope.launch {

                    // 1️⃣ Verificar permiso de ubicación
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        texto_mostrado.value =
                            "Para calcular la distancia necesito acceder a tu ubicación actual 📍"

                        cloudTTS(
                            "Necesito permiso de ubicación para calcular la distancia hacia ${entidad?.key ?: r.t}"
                        )

                        permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        estadoBusqueda.value = Estado_busqueda.IDLE
                        _mostrar_micro.value = true
                        return@launch
                    }

                    // 2️⃣ Verificar si el GPS está activado
                    if (!verificarUbiActiva(context)) {

                        texto_mostrado.value =
                            "Tu ubicación está desactivada. Actívala para poder calcular la distancia 📡"

                        cloudTTS(
                            "Activa tu ubicación para calcular la distancia hacia ${entidad?.key ?: r.t}"
                        )

                        verificarGPS(context, launcher)
                        estadoBusqueda.value = Estado_busqueda.IDLE
                        _mostrar_micro.value = true
                        return@launch
                    }

                    // 3️⃣ Todo correcto → calcular distancia
                    val distancia = ver_distancia_lugar(
                        fusedLocationClient,
                        lista_general_original_inmutable.value,
                        r.t
                    )

                    texto_mostrado.value = distancia
                    estadoBusqueda.value = Estado_busqueda.IDLE
                    _mostrar_micro.value = true
                    cloudTTS(distancia)
                }
            }

            "desconocido" -> {

                if (r.g == "otro") {

                    cloudTTS("No entendí tu solicitud. ¿Podrías repetirlo?")
                    texto_mostrado.value = "No entendí tu solicitud."

                } else {

                    val (mensaje, listaResultado) =
                        resolverPorTexto(
                            texto_origina,
                            lista_general_original_inmutable.value,
                            r.g
                        )

                    cloudTTS(mensaje)
                    titulo_mostrado.value = mensaje
                    texto_mostrado.value = mensaje
                    listaResultado?.let {
                    _state_lista_filtrada.value = carga_seguidad.succes(it)
                    }

                }

                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
            }


            else -> {
                Log.w("NLP_DEBUG2", "Acción no soportada: ${r.a}")
                cloudTTS("No pude encontrar exactamente lo que buscabas, pero mantén la calma. Recuerda que Geinz siempre está contigo ante cualquier emergencia.")
                texto_mostrado.value =
                    "No pude encontrar exactamente lo que buscabas, pero mantén la calma. Recuerda que Geinz siempre está contigo ante cualquier emergencia."

                estadoBusqueda.value = Estado_busqueda.IDLE
                _mostrar_micro.value = true
            }
        }
    }

    fun tieneSimilitud(
        token: String,
        etiqueta: String,
        minCoincidencias: Int = 4
    ): Boolean {

        val t = token.lowercase()
        val e = etiqueta.lowercase()

        // Match directo completo
        if (t.contains(e) || e.contains(t)) return true

        if (e.length >= minCoincidencias) {
            val raizEtiqueta = e.take(minCoincidencias)

            // Detecta raíz dentro del token
            if (t.contains(raizEtiqueta)) return true
        }

        if (t.length >= minCoincidencias) {
            val raizToken = t.take(minCoincidencias)

            if (e.contains(raizToken)) return true
        }

        return false
    }

    fun resolverPorTexto(
        texto: String,
        value: List<dataclass_seguridad>,
        g: String
    ): Pair<String, List<dataclass_seguridad>?> {

        Log.d("NLP_FLOW", "==============================")
        Log.d("NLP_FLOW", "Texto original: $texto")
        Log.d("NLP_FLOW", "Categoría solicitada: $g")
        Log.d("NLP_FLOW", "Total lista original: ${value.size}")

        val listaFiltrada = value.filter {
            it.categoria.equals(g, ignoreCase = true)
        }

        Log.d("NLP_FLOW", "Total listaFiltrada por categoria: ${listaFiltrada.size}")

        if (listaFiltrada.isEmpty()) {
            Log.d("NLP_FLOW", "❌ No hay entidades con esa categoría")
            return Pair("Mantén la calma", null)
        }

        val textoLimpio = texto.lowercase()
            .replace(Regex("[^a-záéíóúñ ]"), "")

        val tokens = textoLimpio
            .split("\\s+".toRegex())
            .filter { it.length > 2 }

        Log.d("NLP_FLOW", "Texto limpio: $textoLimpio")
        Log.d("NLP_FLOW", "Tokens detectados: $tokens")

        var mejorEntidad: dataclass_seguridad? = null
        var maxCoincidencias = 0

        for (entidad in listaFiltrada) {

            var coincidencias = 0

            Log.d("MATCH_DEBUG", "----- Analizando entidad: ${entidad.nombre_} -----")

            for (etiqueta in entidad.etiquetas_categorias) {

                for (token in tokens) {

                    Log.d(
                        "MATCH_DEBUG",
                        "Comparando token='$token' con etiqueta='$etiqueta'"
                    )

                    if (tieneSimilitud(token, etiqueta, 4)) {
                        coincidencias++
                        Log.d(
                            "MATCH_DEBUG",
                            "✔ MATCH en ${entidad.nombre_} con etiqueta '$etiqueta'"
                        )
                        break
                    }
                }
            }

            Log.d(
                "MATCH_RESULT",
                "Entidad: ${entidad.nombre_} | Coincidencias: $coincidencias"
            )

            if (coincidencias > maxCoincidencias) {
                maxCoincidencias = coincidencias
                mejorEntidad = entidad
            }
        }

        Log.d("NLP_DEBUG_RESUMEN", """
        Texto: $texto
        Categoria: $g
        Tokens: $tokens
        ListaFiltradaSize: ${listaFiltrada.size}
        MejorEntidad: ${mejorEntidad?.nombre_}
        MaxCoincidencias: $maxCoincidencias
    """.trimIndent())

        return if (mejorEntidad != null && maxCoincidencias > 0) {

            Log.d("NLP_DEBUG_FINAL", "✔ ENTIDAD SELECCIONADA: ${mejorEntidad.nombre_}")
            Log.d("NLP_DEBUG_FINAL", "✔ Coincidencias finales: $maxCoincidencias")

            val mensaje = obtenerMensajeCalmaUI(mejorEntidad.nombre_, value)

            val resultadoLista = listOf(mejorEntidad)

            Log.d("NLP_DEBUG_FINAL", "✔ Lista retornada size: ${resultadoLista.size}")

            Pair(mensaje, resultadoLista)

        } else {

            Log.d("NLP_DEBUG_FINAL", "❌ No hubo entidad válida")
            Log.d("NLP_DEBUG_FINAL", "maxCoincidencias: $maxCoincidencias")

            Pair(
                "Todo estará bien. necesito algo mas de informacion de tu caso",
                null
            )
        }
    }




    fun obtenerMensajeCalmaUI(
        nombreEntidad: String,
        value: List<dataclass_seguridad>
    ): String {

        val entidad = value.firstOrNull {
            it.nombre_.contains(nombreEntidad, ignoreCase = true)
        }

        val numerosLlamada = entidad?.numero_llamada ?: emptyList()
        val numerosWhatsapp = entidad?.numero_whatsapp ?: emptyList()

        val bloqueContacto = buildString {

            if (numerosLlamada.isNotEmpty()) {

                append(" Llama al ")

                append(
                    numerosLlamada.joinToString(" o al ")
                )

                append(".")
            }

            if (numerosWhatsapp.isNotEmpty()) {

                if (numerosLlamada.isNotEmpty()) {
                    append(" También puedes escribir por WhatsApp al ")
                } else {
                    append(" Escríbeles por WhatsApp al ")
                }

                append(
                    numerosWhatsapp.joinToString(" o al ")
                )

                append(".")
            }

            if (numerosLlamada.isEmpty() && numerosWhatsapp.isEmpty()) {
                append(".")
            }
        }


        val mensajesBase = listOf(
            "${nombre_user.value}, estoy aquí contigo. Mantén la calma y contacta a $nombreEntidad.$bloqueContacto",

            "Respira despacio, ${nombre_user.value}. La ayuda está cerca con $nombreEntidad.$bloqueContacto",

            "${nombre_user.value}, no estás solo. $nombreEntidad puede asistirte ahora mismo.$bloqueContacto",

            "Mantén la serenidad, ${nombre_user.value}. $nombreEntidad está para ayudarte.$bloqueContacto",

            "Tranquilo, ${nombre_user.value}. Estás haciendo lo correcto. Contacta a $nombreEntidad.$bloqueContacto",

            "${nombre_user.value}, la ayuda está disponible. $nombreEntidad puede intervenir.$bloqueContacto",

            "Respira profundo, ${nombre_user.value}. $nombreEntidad puede atender esta situación.$bloqueContacto",

            "Estoy contigo, ${nombre_user.value}. Busca apoyo en $nombreEntidad.$bloqueContacto",

            "${nombre_user.value}, no entres en pánico. $nombreEntidad puede ayudarte.$bloqueContacto",

            "${nombre_user.value}, la asistencia está cerca. Contacta a $nombreEntidad.$bloqueContacto"
        )


        return mensajesBase.random()
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


    fun generarTextoLLamada(
        context: Context,
        lista: List<dataclass_seguridad>,
        nombreBuscado: String,
        activar_permiso_llamada: (numero: String) -> Unit
    ): String {
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
        var mensaje = ""
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mensaje = "Por favor activa el permiso para realizar las llamadas"
            activar_permiso_llamada(entidad.numero_llamada.first())
        } else {
            mensaje = "LLamando a $nombreBuscado"
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
    ): String {

        // Buscamos la entidad
        val entidad = lista.firstOrNull {
            it.nombre_.contains(nombreBuscado, ignoreCase = true)
        }

        if (entidad == null) {
            return "No se encontró información para '$nombreBuscado'."
        }

        // Detectamos qué tipo de info quiere el usuario
        val tipoInfo = detectarTipoInfo(textoUsuario)

        // Construimos el mensaje
        return when (tipoInfo) {

            "direccion" -> {
                val direccion = entidad.direccion ?: "no disponible"
                val referencia = entidad.referencia ?: "no disponible"
                "La dirección de ${entidad.nombre_} es: $direccion. Referencia: $referencia."
            }

            "telefono" -> {
                if (entidad.numero_llamada.isNotEmpty()) {
                    "Puedes comunicarte con ${entidad.nombre_} al ${
                        entidad.numero_llamada.joinToString(
                            ", "
                        )
                    }."
                } else {
                    "El teléfono de ${entidad.nombre_} no está disponible."
                }
            }

            "horario" -> {
                "El horario de atención de ${entidad.nombre_} es: ${
                    horario_atencion(entidad.nombre_)
                }."
            }

            else -> {
                "Aquí tienes información sobre ${entidad.nombre_}."
            }
        }
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

        val mapa = mapOf(
            '0' to "cero",
            '1' to "uno",
            '2' to "dos",
            '3' to "tres",
            '4' to "cuatro",
            '5' to "cinco",
            '6' to "seis",
            '7' to "siete",
            '8' to "ocho",
            '9' to "nueve"
        )

        val soloDigitos = numero.filter { it.isDigit() }

        val formateado = soloDigitos
            .mapNotNull { mapa[it] }
            .joinToString(" ")

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
            Log.d(
                "VER_DISTANCIA",
                "⚠️ La entidad '${lugar.nombre_}' no tiene ubicación física registrada."
            )
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
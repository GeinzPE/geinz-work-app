package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.NotificacionIA
import com.geinzz.geinzwork.data.model.NotificacionIA_dialog
import com.geinzz.geinzwork.data.model.PreciosApp
import com.geinzz.geinzwork.data.model.datos_gen_IA_Tiendas
import com.geinzz.geinzwork.data.model.dialog_generaciones_IA_promo_noti
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.procesaro_por_vos
import com.geinzz.geinzwork.model.repo_generaciones_IA
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar.EstadoIA
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar.EstadoIA_notifi_corta
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Objects

class viewmodel_generaciones_IA : ViewModel() {
    val insta_repo = repo_generaciones_IA()
    val viewmodel_recargas = viewmodel_recargas()


    private val _estado_generaciones_IA =
        MutableStateFlow<EstadoGeneracionesIA>(EstadoGeneracionesIA.Idle)

    val estado_generaciones_IA: StateFlow<EstadoGeneracionesIA> =
        _estado_generaciones_IA

    private val _estado_carga_generacion_desk_whatsap =
        MutableStateFlow<Estado_generacion_IA_whsatp>(Estado_generacion_IA_whsatp.Idle)

    val estado_carga_generaciones_desk_whatsapp: StateFlow<Estado_generacion_IA_whsatp> =
        _estado_carga_generacion_desk_whatsap


    private val _estado_promociones_ia =
        MutableStateFlow<EstadoIA_dialog_centrado>(EstadoIA_dialog_centrado.Idle)

    val estado_promociones_ia: StateFlow<EstadoIA_dialog_centrado> =
        _estado_promociones_ia

    private val _estado_notificacion_con_ia_corta =
        MutableStateFlow<EstadoIA_dialog_centrado_notificaciones>(
            EstadoIA_dialog_centrado_notificaciones.Idle
        )

    val estado_notificaion_con_ia_corta: StateFlow<EstadoIA_dialog_centrado_notificaciones> =
        _estado_notificacion_con_ia_corta


    private val _datosCloudTts = MutableStateFlow(ByteArray(0))
    val datosCloudTts: StateFlow<ByteArray> = _datosCloudTts

    private val _textoCloudTts = MutableStateFlow<String?>(null)
    val textoCloudTts = _textoCloudTts.asStateFlow()

    var textoReconocido by mutableStateOf("")


    var listaCompleta by mutableStateOf<List<datos_gen_IA_Tiendas>>(emptyList())
    var subCategoriaSeleccionada by mutableStateOf("Todos")
    var filtroProfundo by mutableStateOf<Int?>(null)
    var filtroTerminos by mutableStateOf("")
    var tipo_data by mutableStateOf<String?>(null)
    var prioridad_data by mutableStateOf<String?>(null)
    var tiempo_data by mutableStateOf<String?>(null)
    var dias_restantes by mutableStateOf<Int?>(null)


    init {
        obtener_todos_precios()
    }
    // Lista filtrada que la UI va a observar
    var listaFiltrada by mutableStateOf<List<datos_gen_IA_Tiendas>>(emptyList())
        private set

    fun cargarLista(datos: List<datos_gen_IA_Tiendas>) {
        listaCompleta = datos
        actualizarListaFiltrada() // recalcula lista filtrada
    }

    fun actualizarListaFiltrada() {
        listaFiltrada = aplicarFiltros()
    }

    fun cambiar_filtrado(filtro: String) {
        subCategoriaSeleccionada = filtro
        actualizarListaFiltrada() // recalcula lista filtrada antes de consultar tamaño
    }

    fun obtenerCantidadFiltrada(): Int {
        return listaFiltrada.size
    }


    private val _preciosState = MutableStateFlow<PreciosApp?>(null)
    val preciosState: StateFlow<PreciosApp?> = _preciosState
    fun obtener_todos_precios() {
        viewModelScope.launch {

            try {
                val resultado = insta_repo.obtener_precios_generales()
                _preciosState.value = resultado
            } catch (e: Exception) {
                Log.d("error", "$e")
            } finally {

            }
        }
    }

    fun obtener_descripcion_generada_con_datos(
        data: String,
        localidad_tienda: String,
        nombre_tienda: String,
        id_tienda: String,
        total_cobrar: String,
        saldo_tienda: Int
    ) {
        viewModelScope.launch {
            _estado_carga_generacion_desk_whatsap.value = Estado_generacion_IA_whsatp.loading
            try {
                val texto_generado = insta_repo.generar_descripcion_con_IA_whatsapp_bot(data)
                if (texto_generado.isNotEmpty()) {
                    _estado_carga_generacion_desk_whatsap.value =
                        Estado_generacion_IA_whsatp.succes(texto_generado)
                    val historial = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = total_cobrar,
                        tipo = "Gen IA Asistente para whatsapp",
                        precio_soles = constantes_cobro_monedas
                            .calcular_precio_soles(total_cobrar)
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - total_cobrar.toInt()
                    )
                    viewmodel_recargas.restar_puntos_recarga(
                        historial,
                        total_cobrar,
                        id_tienda,
                        localidad_tienda
                    )
                } else {
                    _estado_carga_generacion_desk_whatsap.value =
                        Estado_generacion_IA_whsatp.empty(texto_generado)
                }

            } catch (e: Exception) {
                Log.d("error_obtener_Descripcion", "$e")
            }
        }
    }

    fun resetear_valor_generacion_desk_whatsapp() {
        _estado_carga_generacion_desk_whatsap.value = Estado_generacion_IA_whsatp.Idle

    }


    private fun aplicarFiltros(): List<datos_gen_IA_Tiendas> {
        // Partimos de la lista madre
        var listaFinal = listaCompleta

        // 1️⃣ Filtrado por subcategoría
        listaFinal = when (subCategoriaSeleccionada) {
            "Todos" -> listaFinal
            "Permanentes" -> listaFinal.filter { it.fin == null }
            "Generaciones no publicadas (promociones)" ->
                listaFinal.filter { it.tipo_realizado == "generacion_publicacion_sin_pulicar" }

            "Generaciones no publicadas (notificaciones)" ->
                listaFinal.filter { it.tipo_realizado == "notificacion_sin_publicar" }

            "Generaciones de promociones" ->
                listaFinal.filter { it.tipo_realizado == "publicacion" }

            "Generaciones de notificaciones" ->
                listaFinal.filter { it.tipo_realizado == "notificacion" }

            "Por vencer" -> {
                val diasLimite = 1
                listaFinal.filter { item ->
                    val tiempo = item.fin?.let {
                        constantes_datos_expirados_fechas_publicaciones.tiempoRestante(it)
                    } ?: return@filter false

                    when {
                        tiempo == "Expirado" -> false
                        tiempo.contains("mto", ignoreCase = true) ||
                                tiempo.contains("min", ignoreCase = true) -> true

                        tiempo.contains("hora", ignoreCase = true) -> true
                        tiempo.contains("día", ignoreCase = true) -> {
                            val dias =
                                tiempo.filter { it.isDigit() }.toIntOrNull() ?: return@filter false
                            dias in 0..diasLimite
                        }

                        else -> false
                    }
                }
            }

            else -> listaFinal
        }

        // 2️⃣ Filtrado profundo (por longitud de id)
        if (subCategoriaSeleccionada == "Generaciones no publicadas (notificaciones)" && filtroProfundo != null) {
            listaFinal = listaFinal.filter { it.id_promo_noti_cread.length == filtroProfundo }
        }

        // 3️⃣ FILTRO POR TIPO IA
        tipo_data?.let { tipo ->
            val tiposValidos = listOf(
                "publicacion",
                "notificacion",
                "generacion_publicacion_sin_pulicar",
                "notificacion_sin_publicar"
            )
            val tipoSeguro = tipo.lowercase().takeIf { it in tiposValidos }
            tipoSeguro?.let { t ->
                listaFinal = listaFinal.filter { it.tipo_realizado.equals(t, ignoreCase = true) }
            }
        }

        // 4️⃣ FILTRO POR TIEMPO
        tiempo_data?.let { tiempo ->
            listaFinal = listaFinal.filter { coincideConTiempo(it.fecha_normal, tiempo) }
        }

        // 5️⃣ FILTRO POR PRIORIDAD
        prioridad_data?.let {
            listaFinal = listaFinal.filter { it.fin == null }
        }


        val filtroDias: Int? = dias_restantes

        Log.d("FiltroDias", "Valor recibido dias_restantes (filtro): $filtroDias")

        val listaFiltrada = filtroDias?.let { maxDias ->
            Log.d("FiltroDias", "Aplicando filtro: dias <= $maxDias")

            listaFinal.filter { item ->
                val dias = calcularDiasRestantes(item.fin)

                Log.d(
                    "FiltroDias",
                    "Item ${item.id_promo_noti_cread} | fin=${item.fin} | dias_calculados=$dias"
                )

                val pasa = dias != null && dias <= maxDias

                Log.d(
                    "FiltroDias",
                    "Item ${item.id_promo_noti_cread} | pasa_filtro=$pasa"
                )

                pasa
            }
        } ?: run {
            Log.d("FiltroDias", "filtroDias es null → se devuelve lista completa")
            listaFinal
        }

        Log.d("FiltroDias", "Total items antes: ${listaFinal.size}")
        Log.d("FiltroDias", "Total items después: ${listaFiltrada.size}")


        // 6️⃣ FILTRO POR TÉRMINOS
        if (filtroTerminos.isNotBlank()) {
            val terminos = filtroTerminos.lowercase().split(" ").filter { it.isNotBlank() }
            listaFinal = listaFinal.filter { item ->
                val terminosUsuario = terminos.map { it.lowercase() }
                val terminosGeneracion = item.terminos.map { it.lowercase() }
                terminosUsuario.any { termino ->
                    terminosGeneracion.any { it.contains(termino) }
                }
            }
        }

        return listaFinal
    }

    fun calcularDiasRestantes(fechaFin: Timestamp?): Int? {
        if (fechaFin == null) return null
        val ahora = Calendar.getInstance().timeInMillis
        val fin = fechaFin.toDate().time
        val diff = fin - ahora
        return if (diff >= 0) (diff / (1000 * 60 * 60 * 24)).toInt() else 0
    }

    fun obtner_generaciones_IA(localida: String, id_tienda: String) {
        viewModelScope.launch {

            insta_repo
                .obtener_generaciones_IA_realtime(id_tienda, localida)
                .onStart {
                    _estado_generaciones_IA.value = EstadoGeneracionesIA.Loading
                }
                .catch {
                    _estado_generaciones_IA.value =
                        EstadoGeneracionesIA.Error("Error al obtener generaciones")
                }
                .collect { lista ->

                    _estado_generaciones_IA.value =
                        if (lista.isNotEmpty()) {
                            EstadoGeneracionesIA.Success(lista)
                        } else {
                            EstadoGeneracionesIA.Empty("No se encontraron generaciones")
                        }
                }
        }
    }

    fun agregar_nueva_generacion_remasterizada(
        titulo_anterior: String,
        descripcion_anteriro: String,
        id_tienda: String,
        localidad: String,
        titulo_nuevo: String,
        texto_nuevo: String,
        id_generacion: String
    ) {
        viewModelScope.launch {
            try {
                insta_repo.agregar_nuevas_generaciones(
                    titulo_anterior, descripcion_anteriro,
                    id_tienda,
                    localidad,
                    titulo_nuevo,
                    texto_nuevo,
                    id_generacion
                )
            } catch (
                e: Exception
            ) {
                Log.d("agregamos_campos", "$e")
            }
        }
    }

    fun mejorar_texto_con_promo_IA(
        id_promo_noti_gen: String,
        tipo_generacion: repo_pantallas_promocionar.TipoGeneracionIA,
        saldo_tienda: Int,
        localidad_tienda: String,
        id_tienda: String,
        nombre_tienda: String,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
        total_cobrar: String,
        titulo_generacion_historial: String
    ) {
        viewModelScope.launch {

            _estado_promociones_ia.value = EstadoIA_dialog_centrado.Loading

            try {

                if (saldo_tienda < 30) {
                    _estado_promociones_ia.value =
                        EstadoIA_dialog_centrado.Error("Saldo insuficiente")
                    return@launch
                }

                val resultado = withTimeout(15_000) {
                    insta_repo.generar_promocion_con_IA(
                        id_promo_noti_gen = id_promo_noti_gen,
                        tipo_generacion = tipo_generacion,
                        tituloUsuario = tituloUsuario,
                        descripcionUsuario = descripcionUsuario,
                        nombreTienda = nombreTienda,
                        localidad = localidad
                    )
                }

                if (resultado != null) {

                    _estado_promociones_ia.value =
                        EstadoIA_dialog_centrado.Success(resultado)

                    val historial = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = total_cobrar,
                        tipo = titulo_generacion_historial,
                        precio_soles = constantes_cobro_monedas
                            .calcular_precio_soles(total_cobrar)
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - total_cobrar.toInt()
                    )

                    viewmodel_recargas.restar_puntos_recarga(
                        historial,
                        total_cobrar,
                        id_tienda,
                        localidad_tienda
                    )


                } else {
                    _estado_promociones_ia.value =
                        EstadoIA_dialog_centrado.Error("No se pudo generar contenido")
                }

            } catch (e: TimeoutCancellationException) {


                _estado_promociones_ia.value =
                    EstadoIA_dialog_centrado.Error("La IA tardó demasiado. Intenta otra vez.")

            } catch (e: Exception) {

                _estado_promociones_ia.value =
                    EstadoIA_dialog_centrado.Error("Error al generar con IA")
            }
        }
    }


    fun limpiar_Estado_nueva_generacion() {
        _estado_promociones_ia.value = EstadoIA_dialog_centrado.Idle
    }


    fun mejorar_mejorar_notificacion_con_IA_corta(
        id_notificacion_promo: String,
        tipo_select_IA: String,
        tipoSeleccionado: repo_pantallas_promocionar.TipoGeneracionIA,
        saldo_tienda: Int,
        localidad_tienda: String,
        id_tienda: String,
        nombre_tienda: String,
        titulo_publicacion: String,
        descripcion: String
    ) {
        viewModelScope.launch {

            _estado_notificacion_con_ia_corta.value =
                EstadoIA_dialog_centrado_notificaciones.Loading

            try {
                if (saldo_tienda < 20) {
                    _estado_notificacion_con_ia_corta.value =
                        EstadoIA_dialog_centrado_notificaciones.Error("saldo insuficiente")
                    return@launch
                }


                val notificacionIA = withTimeout(15_000) {
                    insta_repo.crear_notificacion_conIA_corta(
                        id_notificacion_promo,
                        titulo_publicacion,
                        descripcion,
                        tipoSeleccionado
                    )
                }

                _estado_notificacion_con_ia_corta.value =
                    EstadoIA_dialog_centrado_notificaciones.Success(notificacionIA)

                if (
                    notificacionIA.titulo.isNotEmpty() &&
                    notificacionIA.descripcion.isNotEmpty()
                ) {
                    val historial_descuento = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = "15",
                        tipo = tipo_select_IA,
                        precio_soles = constantes_cobro_monedas
                            .calcular_precio_soles("15")
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - 15
                    )

                    viewmodel_recargas.restar_puntos_recarga(
                        historial_descuento,
                        "15",
                        id_tienda,
                        localidad_tienda
                    )
                }

            } catch (e: TimeoutCancellationException) {
                _estado_notificacion_con_ia_corta.value =
                    EstadoIA_dialog_centrado_notificaciones.Error(
                        "La generación tardó demasiado, intenta otra vez"
                    )

            } catch (e: Exception) {
                _estado_notificacion_con_ia_corta.value =
                    EstadoIA_dialog_centrado_notificaciones.Error(
                        e.message ?: "Error al generar la notificación con IA"
                    )
            }
        }
    }


    fun resetear_Estado_notificacion_enviadad() {
        _estado_notificacion_con_ia_corta.value = EstadoIA_dialog_centrado_notificaciones.Idle
    }

    fun guardar_como_permanete(
        id_generacion: String,
        id_tienda: String,
        localidad: String,
        nombre_tienda: String,
        saldo_tienda: Int
    ) {
        viewModelScope.launch {
            try {
                val guardado = insta_repo.guardar_como_permanente(
                    id_generacion,
                    id_tienda,
                    localidad
                )
                if (!guardado) return@launch
                val historial_descuento = historial_descuento(
                    tipo_transaccion = "descuento",
                    fecha = obtenerFechaActual(),
                    hora = obtenerHoraActual(),
                    id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                    localidad_tienda = localidad,
                    id_tienda = id_tienda,
                    nombre_tienda = nombre_tienda,
                    monto_descuento = "13",
                    tipo = "Guardado permanente de generacion IA",
                    precio_soles = constantes_cobro_monedas
                        .calcular_precio_soles("13")
                        .toString(),
                    estado = "Aceptado",
                    monto_restante = saldo_tienda - 13
                )

                viewmodel_recargas.restar_puntos_recarga(
                    historial_descuento,
                    "13",
                    id_tienda,
                    localidad
                )
            } catch (e: Exception) {
                Log.d("error_guardado", "error al guardar la generacion")
            }
        }

    }


    fun cloudTTS(texto: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _datosCloudTts.value = insta_repo.cloudTTS(texto)
            } catch (e: Exception) {
                Log.e("CloudTTS", "Error de text to speech", e)
            }
        }
    }

    suspend fun procesar_busqueda_con_IA(textoUser: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                insta_repo.procesarBusquedaConIA(textoUser)
            } catch (e: Exception) {
                Log.e("BusquedaIA", "Error al procesar búsqueda", e)
                ""
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun coincideConTiempo(fecha: LocalDate, tiempo: String): Boolean {
        val hoy = LocalDate.now()
        val tiempoClean = tiempo.lowercase().trim()

        return when {
            // Hoy
            tiempoClean == "hoy" -> fecha.isEqual(hoy)

            // Ayer
            tiempoClean == "ayer" -> fecha.isEqual(hoy.minusDays(1))

            // Hace X días (soporta "día" y "días")
            tiempoClean.startsWith("hace ") && (tiempoClean.endsWith(" día") || tiempoClean.endsWith(
                " días"
            )) -> {
                val diasStr = tiempoClean
                    .removePrefix("hace ")
                    .removeSuffix(" días")
                    .removeSuffix(" día")
                    .trim()
                val dias = diasStr.toLongOrNull()
                dias?.let { fecha.isEqual(hoy.minusDays(it)) } ?: false
            }

            // Esta semana
            tiempoClean == "esta semana" -> {
                val inicioSemana = hoy.with(DayOfWeek.MONDAY)
                val finSemana = hoy.with(DayOfWeek.SUNDAY)
                !fecha.isBefore(inicioSemana) && !fecha.isAfter(finSemana)
            }

            // Este mes
            tiempoClean == "este mes" -> fecha.month == hoy.month && fecha.year == hoy.year

            // Este año
            tiempoClean == "este año" -> fecha.year == hoy.year

            // Intentar parsear dd/MM/yyyy o yyyy-MM-dd
            else -> runCatching {
                val formatos = listOf(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
                formatos.any { fmt ->
                    runCatching { LocalDate.parse(tiempoClean, fmt) }.getOrNull()
                        ?.let { fecha.isEqual(it) } ?: false
                }
            }.getOrDefault(false)
        }.also { resultado ->
            Log.d("FiltroTiempo", "fecha=$fecha, tiempo='$tiempo', hoy=$hoy, resultado=$resultado")
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun Timestamp.toLocalDate(): LocalDate =
        this.toDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    sealed class EstadoIA_dialog_centrado {
        object Idle : EstadoIA_dialog_centrado()
        object Loading : EstadoIA_dialog_centrado()
        data class Success(val generacion: dialog_generaciones_IA_promo_noti) :
            EstadoIA_dialog_centrado()

        data class Error(val mensaje: String) : EstadoIA_dialog_centrado()
    }


    sealed class EstadoIA_dialog_centrado_notificaciones {
        object Idle : EstadoIA_dialog_centrado_notificaciones()
        object Loading : EstadoIA_dialog_centrado_notificaciones()
        data class Success(val txt_descripcion: NotificacionIA_dialog) :
            EstadoIA_dialog_centrado_notificaciones()

        data class Error(val mensaje: String) : EstadoIA_dialog_centrado_notificaciones()
    }


    sealed class Estado_generacion_IA_whsatp {
        object Idle : Estado_generacion_IA_whsatp()
        object loading : Estado_generacion_IA_whsatp()
        data class succes(val txt: String) : Estado_generacion_IA_whsatp()
        data class error(val error: String) : Estado_generacion_IA_whsatp()
        data class empty(val txt: String) : Estado_generacion_IA_whsatp()
    }


    sealed class EstadoGeneracionesIA {
        object Idle : EstadoGeneracionesIA()
        object Loading : EstadoGeneracionesIA()
        data class Success(
            val data: List<datos_gen_IA_Tiendas>
        ) : EstadoGeneracionesIA()

        data class Error(
            val message: String
        ) : EstadoGeneracionesIA()

        data class Empty(
            val message: String
        ) : EstadoGeneracionesIA()
    }


}
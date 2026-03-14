package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.completeta_info_inmuebles
import com.geinzz.geinzwork.data.model.dataclass_geinz_inmobiliaria_principal
import com.geinzz.geinzwork.data.model.ia_inmobiliara_tts
import com.geinzz.geinzwork.data.model.lista_lugaers_totales
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.model.repo_inmobiliaria
import com.geinzz.geinzwork.model.tts_stt.repo_tts_stt
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.normalizarNombre
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder

class viewmodel_inmobiliaria : ViewModel() {
    val instarepo = repo_inmobiliaria()


    private val _estado_carga_inmubles_principales =
        MutableStateFlow<estado_carga_principal_immubles>(estado_carga_principal_immubles.idle)

    val estado_carga_inmuebles_principales: StateFlow<estado_carga_principal_immubles> =
        _estado_carga_inmubles_principales

    private val _respuesta_IA = MutableStateFlow("")

    private val _datosCloudTts = MutableStateFlow(ByteArray(0))
    val datosCloudTts: StateFlow<ByteArray> = _datosCloudTts


    val respuesta_IA: StateFlow<String> = _respuesta_IA.asStateFlow()


    private val _lugares_filtrados = MutableStateFlow(lista_lugaers_totales())

    val lugares_filtrados: StateFlow<lista_lugaers_totales> =
        _lugares_filtrados.asStateFlow()


    val lista_lugares_seguros: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())

    val lista_lugares_cercanos: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())

    val lista_lugares_turisticos: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())

    val lista_lugares_servicios: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())


    val lista_lugares_seguros_filtrada: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())

    val lista_lugares_cercanos_filtrada: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())

    val lista_lugares_turisticos_filtrada: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())

    val lista_lugares_servicios_filtrada: MutableStateFlow<List<lugares_cercanos_>> =
        MutableStateFlow(emptyList())


    fun guardar_datosListas(
        seguros: List<lugares_cercanos_>,
        cercanos: List<lugares_cercanos_>,
        turisticos: List<lugares_cercanos_>,
        servicios: List<lugares_cercanos_>
    ) {
        lista_lugares_seguros.value = seguros
        lista_lugares_cercanos.value = cercanos
        lista_lugares_turisticos.value = turisticos
        lista_lugares_servicios.value = servicios
        lista_lugares_seguros_filtrada.value = seguros
        lista_lugares_cercanos_filtrada.value = cercanos
        lista_lugares_turisticos_filtrada.value = turisticos
        lista_lugares_servicios_filtrada.value = servicios
    }

    fun limpiar_listas() {
        lista_lugares_seguros.value = emptyList()
        lista_lugares_cercanos.value = emptyList()
        lista_lugares_turisticos.value = emptyList()
        lista_lugares_servicios.value = emptyList()
        lista_lugares_seguros_filtrada.value = emptyList()
        lista_lugares_cercanos_filtrada.value = emptyList()
        lista_lugares_turisticos_filtrada.value = emptyList()
        lista_lugares_servicios_filtrada.value = emptyList()
    }

    private val _estado_carga_info_inmuebles =
        MutableStateFlow<etado_carga_info_inmuebles>(etado_carga_info_inmuebles.idle)

    val estado_carga_info_inmuebles: StateFlow<etado_carga_info_inmuebles> =
        _estado_carga_info_inmuebles

    private var lastDocument: DocumentSnapshot? = null

    // Agregar en el ViewModel
    fun limpiar_estado_info() {
        _estado_carga_info_inmuebles.value = etado_carga_info_inmuebles.idle
    }

    fun obtener_inmubles_dados(localidad_select: String, cargarMas: Boolean = false) {

        if (!cargarMas &&
            _estado_carga_inmubles_principales.value is estado_carga_principal_immubles.succes
        ) {
            return
        }

        viewModelScope.launch {

            if (!cargarMas) {
                _estado_carga_inmubles_principales.value =
                    estado_carga_principal_immubles.loading
            }

            try {

                val resultado = instarepo.obtener_inmuebles(localidad_select, lastDocument)

                val lista = resultado.first
                lastDocument = resultado.second

                if (lista.isNotEmpty()) {

                    if (cargarMas &&
                        _estado_carga_inmubles_principales.value is estado_carga_principal_immubles.succes
                    ) {

                        val actual =
                            (_estado_carga_inmubles_principales.value as estado_carga_principal_immubles.succes).lista_inmuebles

                        _estado_carga_inmubles_principales.value =
                            estado_carga_principal_immubles.succes(actual + lista)

                    } else {

                        _estado_carga_inmubles_principales.value =
                            estado_carga_principal_immubles.succes(lista)

                    }

                } else {

                    if (!cargarMas) {
                        _estado_carga_inmubles_principales.value =
                            estado_carga_principal_immubles.empty("No se encontraron datos")
                    }

                }

            } catch (e: Exception) {
                Log.d("error_inmubles", "error al obtener los inmuebles $e")
            }
        }
    }

    fun cargarDatos(id: String, localidad: String) {

        viewModelScope.launch {

            try {

                val datos = instarepo.obtner_datos_completos_del_inmueble(
                    id,
                    localidad
                )

                _estado_carga_info_inmuebles.value =
                    etado_carga_info_inmuebles.succes(datos)

            } catch (e: Exception) {

                _estado_carga_info_inmuebles.value =
                    etado_carga_info_inmuebles.error(e.message ?: "error")

            }

        }

    }


    fun obtener_negocios_para_perfil(
        tipo: String,
        lista_negocios: List<lugares_cercanos_>,
        lista_seguros: List<lugares_cercanos_>,
        lista_turisticos: List<lugares_cercanos_>,
    ) {

        val categorias = when (tipo) {

            "inversionista" -> listOf(
                "bancos y servicios financieros",
                "supermercado minimarkets y bodegas",
                "transporte y terminales",
                "turismo",
                "hospedaje y entretenimiento nocturno"
            )

            "familiar" -> listOf(
                "salud y farmacias",
                "educacion y librerias",
                "supermercado minimarkets y bodegas",
                "deporte y bienestar",
                "transporte y terminales"
            )

            "solitario" -> listOf(
                "comida y restaurantes",
                "entretenimiento y recreacion",
                "moda y estilo",
                "tecnologia y electronica",
                "belleza"
            )

            else -> emptyList()
        }

        val usados = mutableSetOf<String>()

        val negocios = mutableListOf<String>()
        val seguros = mutableListOf<String>()
        val turisticos = mutableListOf<String>()

        // 🔹 NEGOCIOS (máx 10)
        categorias.forEach { categoria ->

            val lugares = lista_negocios
                .filter { it.distanciaKm <= 0.5 }
                .filter { it.categoira == categoria }
                .sortedBy { it.distanciaKm }

            for (lugar in lugares) {

                val clave = normalizarNombre(lugar.nombre)

                if (!usados.contains(clave)) {
                    negocios.add(lugar.nombre)
                    usados.add(clave)
                }

                if (negocios.size >= 10) break
            }
        }

        // 🔹 SEGUROS (máx 4)
        lista_seguros
            .filter { it.distanciaKm <= 0.5 }
            .sortedBy { it.distanciaKm }
            .take(4)
            .forEach {

                val clave = normalizarNombre(it.nombre)

                if (!usados.contains(clave)) {
                    seguros.add(it.nombre)
                    usados.add(clave)
                }
            }

        // 🔹 TURISMO (máx 4)
        lista_turisticos
            .filter { it.distanciaKm <= 0.5 }
            .sortedBy { it.distanciaKm }
            .take(4)
            .forEach {

                val clave = normalizarNombre(it.nombre)

                if (!usados.contains(clave)) {
                    turisticos.add(it.nombre)
                    usados.add(clave)
                }
            }

        _lugares_filtrados.value =
            lista_lugaers_totales(negocios.take(4), turisticos.take(3), seguros.take(3))


        Log.d("IA_LUGARES", _lugares_filtrados.toString())
    }


    fun filtrar_por_radio_Cercania(radio: Double) {
        viewModelScope.launch {
            try {
//                Log.d("FILTRO_RADIO", "========================================")
//                Log.d("FILTRO_RADIO", "Radio recibido: $radio km")
//                Log.d("FILTRO_RADIO", "========================================")

                // --- SEGUROS ---
                val seguros_originales = lista_lugares_seguros.value
                val seguros_filtrados = seguros_originales.filter { it.distanciaKm <= radio }
//                Log.d("FILTRO_SEGUROS", "Originales: ${seguros_originales.size}")
                seguros_originales.forEach {
                    Log.d(
                        "FILTRO_SEGUROS",
                        "  > ${it.nombre} | dist: ${it.distanciaKm} km | pasa: ${it.distanciaKm <= radio}"
                    )
                }
//                Log.d("FILTRO_SEGUROS", "Filtrados: ${seguros_filtrados.size}")
                lista_lugares_seguros_filtrada.value = seguros_filtrados

                // --- CERCANOS ---
                val cercanos_originales = lista_lugares_cercanos.value
                val cercanos_filtrados = cercanos_originales.filter { it.distanciaKm <= radio }
//                Log.d("FILTRO_CERCANOS", "Originales: ${cercanos_originales.size}")
                cercanos_originales.forEach {
                    Log.d(
                        "FILTRO_CERCANOS",
                        "  > ${it.nombre} | dist: ${it.distanciaKm} km | pasa: ${it.distanciaKm <= radio}"
                    )
                }
//                Log.d("FILTRO_CERCANOS", "Filtrados: ${cercanos_filtrados.size}")
                lista_lugares_cercanos_filtrada.value = cercanos_filtrados

                // --- TURISTICOS ---
                val turisticos_originales = lista_lugares_turisticos.value
                val turisticos_filtrados = turisticos_originales.filter { it.distanciaKm <= radio }
//                Log.d("FILTRO_TURISTICOS", "Originales: ${turisticos_originales.size}")
                turisticos_originales.forEach {
                    Log.d(
                        "FILTRO_TURISTICOS",
                        "  > ${it.nombre} | dist: ${it.distanciaKm} km | pasa: ${it.distanciaKm <= radio}"
                    )
                }
//                Log.d("FILTRO_TURISTICOS", "Filtrados: ${turisticos_filtrados.size}")
                lista_lugares_turisticos_filtrada.value = turisticos_filtrados

                // --- SERVICIOS ---
                val servicios_originales = lista_lugares_servicios.value
                val servicios_filtrados = servicios_originales.filter { it.distanciaKm <= radio }
//                Log.d("FILTRO_SERVICIOS", "Originales: ${servicios_originales.size}")
                servicios_originales.forEach {
                    Log.d(
                        "FILTRO_SERVICIOS",
                        "  > ${it.nombre} | dist: ${it.distanciaKm} km | pasa: ${it.distanciaKm <= radio}"
                    )
                }
//                Log.d("FILTRO_SERVICIOS", "Filtrados: ${servicios_filtrados.size}")
                lista_lugares_servicios_filtrada.value = servicios_filtrados

//                Log.d("FILTRO_RADIO", "========================================")
//                Log.d("FILTRO_RADIO", "RESUMEN -> seguros:${seguros_filtrados.size} | cercanos:${cercanos_filtrados.size} | turisticos:${turisticos_filtrados.size} | servicios:${servicios_filtrados.size}")
//                Log.d("FILTRO_RADIO", "========================================")

            } catch (e: Exception) {
                Log.e("FILTRO_RADIO", "ERROR al filtrar: ${e.message}", e)
            }
        }
    }


    fun respuesta_gemini_(
        i: ia_inmobiliara_tts,
        perfil_selet: String
    ) {
        viewModelScope.launch {
            try {
                _respuesta_IA.value = instarepo.generacion_texto_por_IA(i, perfil_selet)
            } catch (e: Exception) {
                Log.d("error", "ocurrio un error al veriicar")
            }
        }

    }

    //    fun agregar_geo(){
//        viewModelScope.launch {
//            try {
//                instarepo.agregar_geohasgin_turistico()
//
//            }catch (e: Exception){
//
//            }
//        }
//    }


    fun compartir_link_tienda(
        context: Context,
        localidad: String,
        id: String,
    ) {
        // Construimos el link de la Cloud Function
        val link = "https://geinzworkapp.web.app/share?" +
                "t=in" +
                "&id=${URLEncoder.encode(id, "UTF-8")}" +
                "&l=${URLEncoder.encode(localidad, "UTF-8")}"


        // Intent simple ya sin imágenes, porque la preview la maneja Firebase Hosting
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir con"))
    }

    sealed class etado_carga_info_inmuebles {
        data class succes(val datos: completeta_info_inmuebles) : etado_carga_info_inmuebles()
        data class error(val txt: String = "error") : etado_carga_info_inmuebles()
        object idle : etado_carga_info_inmuebles()
    }


    sealed class estado_carga_principal_immubles {
        data class succes(val lista_inmuebles: List<dataclass_geinz_inmobiliaria_principal>) :
            estado_carga_principal_immubles()

        data class empty(val texto: String) : estado_carga_principal_immubles()
        data class error(val texto: String) : estado_carga_principal_immubles()
        object idle : estado_carga_principal_immubles()
        object loading : estado_carga_principal_immubles()
    }

}
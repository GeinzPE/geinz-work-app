package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.EstadoMapa
import com.geinzz.geinzwork.data.model.datos_viewmodel_inmobiliara
import com.geinzz.geinzwork.data.model.localizate_geinz.Exitosa
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.data.model.ob_categoria_mas_lista_lugares_cercanos
import com.geinzz.geinzwork.data.model.obj_pasado_clikeado_mapa
import com.geinzz.geinzwork.model.repo_inmobiliaria
import com.geinzz.geinzwork.model.repo_mapa_inmobiliara
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.loadBitmapFromUrl
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.toCircularBitmap
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_mapa_inmobiliara : ViewModel() {

    private val instance_repo_mapa_inmobiliara = repo_mapa_inmobiliara()

    private val instance_repo= repo_inmobiliaria()

    private val _cargandoDatosMapa = MutableStateFlow(true)
    val cargandoDatosMapa: StateFlow<Boolean> = _cargandoDatosMapa


    private val guardar_datos_inmuble = MutableStateFlow(datos_viewmodel_inmobiliara())

    val datosInmueble: StateFlow<datos_viewmodel_inmobiliara> = guardar_datos_inmuble.asStateFlow()


    private val _categorias_mas_lista_lugares_cercanos_seguros =
        MutableStateFlow(ob_categoria_mas_lista_lugares_cercanos())

    val categorias_mas_lista_lugares_cercanos_seguros: StateFlow<ob_categoria_mas_lista_lugares_cercanos> =
        _categorias_mas_lista_lugares_cercanos_seguros


    private val _categorias_mas_lista_lugares_cercanos =
        MutableStateFlow(ob_categoria_mas_lista_lugares_cercanos())

    val categorias_mas_lista_lugares_cercanos: StateFlow<ob_categoria_mas_lista_lugares_cercanos> =
        _categorias_mas_lista_lugares_cercanos


    private val _categorias_mas_lista_lugares_cercanos_turisticos =
        MutableStateFlow(ob_categoria_mas_lista_lugares_cercanos())

    val categorias_mas_lista_lugares_cercanos_turisticos: StateFlow<ob_categoria_mas_lista_lugares_cercanos> =
        _categorias_mas_lista_lugares_cercanos_turisticos


    private val _categorias_mas_lista_lugares_cercanos_hoga =
        MutableStateFlow(ob_categoria_mas_lista_lugares_cercanos())

    val categorias_mas_lista_lugares_cercanos_hogar: StateFlow<ob_categoria_mas_lista_lugares_cercanos> =
        _categorias_mas_lista_lugares_cercanos_hoga



    private val _lugares_Seguros_encontrados=MutableStateFlow<List<lugares_cercanos_>> (emptyList())
    val lugares_Seguros_econtrados: StateFlow<List<lugares_cercanos_>> = _lugares_Seguros_encontrados

    private val _lugares_cercanos_encontrados=MutableStateFlow<List<lugares_cercanos_>> (emptyList())
    val lugares_cercanos_econtrados: StateFlow<List<lugares_cercanos_>> = _lugares_cercanos_encontrados

    private val _lugares_turisticos_encontrados=MutableStateFlow<List<lugares_cercanos_>> (emptyList())
    val lugares_turistico_econtrados: StateFlow<List<lugares_cercanos_>> = _lugares_turisticos_encontrados

    private val _lugares_esenciales_encontrados=MutableStateFlow<List<lugares_cercanos_>> (emptyList())
    val lugares_esenciales_econtrados: StateFlow<List<lugares_cercanos_>> = _lugares_esenciales_encontrados

    private val _estadoRuta = MutableStateFlow<Exitosa?>(null)
    val estadoRuta: StateFlow<Exitosa?> = _estadoRuta.asStateFlow()




    init {
        viewModelScope.launch {
            _lugares_Seguros_encontrados.collect { lista ->
                if (lista.isNotEmpty()) guardar_listas_datos_lugares_Seguros(lista)
            }
        }
        viewModelScope.launch {
            _lugares_cercanos_encontrados.collect { lista ->
                if (lista.isNotEmpty()) guardar_lista_datos_lugares_cercanos(lista)
            }
        }
        viewModelScope.launch {
            _lugares_turisticos_encontrados.collect { lista ->
                if (lista.isNotEmpty()) guardar_lista_datos_lugares_turisticos(lista)
            }
        }
        viewModelScope.launch {
            _lugares_esenciales_encontrados.collect { lista ->
                if (lista.isNotEmpty()) guardar_lista_datos_lugares_servicio_hogar(lista)
            }
        }
    }
    fun limpiarEstadoRuta() {
        _estadoRuta.value = null
        _cargandoDatosMapa.value = true
    }

    fun agregar_datos_para_pasa_mapa(datos: datos_viewmodel_inmobiliara) {
        guardar_datos_inmuble.value = datos

    }
    fun marcar_mapa_listo() {
        _cargandoDatosMapa.value = false
    }
    // 👇 Listas ORIGINALES (nunca se tocan)
    private var _lista_original_seguros = listOf<lugares_cercanos_>()
    private var _lista_original_cercanos = listOf<lugares_cercanos_>()
    private var _lista_original_turisticos = listOf<lugares_cercanos_>()
    private var _lista_original_hogar = listOf<lugares_cercanos_>()

    fun obtener_lista_original_seguros() = _lista_original_seguros
    fun obtener_lista_original_cercanos() = _lista_original_cercanos
    fun obtener_lista_original_turisticos() = _lista_original_turisticos
    fun obtener_lista_original_hogar() = _lista_original_hogar
    // Al guardar, siempre salva también la original
    fun guardar_listas_datos_lugares_Seguros(lista: List<lugares_cercanos_>) {
        _lista_original_seguros = lista // 👈 guardar original
        val categorias = lista.map { it.categoira }.distinct()
        _categorias_mas_lista_lugares_cercanos_seguros.value =
            ob_categoria_mas_lista_lugares_cercanos(lista, categorias)
    }

    fun guardar_lista_datos_lugares_cercanos(lista: List<lugares_cercanos_>) {
        _lista_original_cercanos = lista
        val categorias = lista.map { it.categoira }.distinct()
        _categorias_mas_lista_lugares_cercanos.value =
            ob_categoria_mas_lista_lugares_cercanos(lista, categorias)
    }

    fun guardar_lista_datos_lugares_turisticos(lista: List<lugares_cercanos_>) {
        _lista_original_turisticos = lista
        val categorias = lista.map { it.categoira }.distinct()
        _categorias_mas_lista_lugares_cercanos_turisticos.value =
            ob_categoria_mas_lista_lugares_cercanos(lista, categorias)
    }

    fun guardar_lista_datos_lugares_servicio_hogar(lista: List<lugares_cercanos_>) {
        _lista_original_hogar = lista
        val categorias = lista.map { it.categoira }.distinct()
        _categorias_mas_lista_lugares_cercanos_hoga.value =
            ob_categoria_mas_lista_lugares_cercanos(lista, categorias)
    }

    // 👇 Filtros siempre desde la original
    fun aplicarFiltroSeguros(categoria: String) {
        val filtrados = filtrarPorCategoria(_lista_original_seguros, categoria)
        _categorias_mas_lista_lugares_cercanos_seguros.value =
            ob_categoria_mas_lista_lugares_cercanos(
                lista_data = filtrados,
                lista_categoira = _lista_original_seguros.map { it.categoira }.distinct()
            )
    }

    fun aplicarFiltrolugares_cercanos(categoria: String) {
        val filtrados = filtrarPorCategoria(_lista_original_cercanos, categoria)
        _categorias_mas_lista_lugares_cercanos.value =
            ob_categoria_mas_lista_lugares_cercanos(
                lista_data = filtrados,
                lista_categoira = _lista_original_cercanos.map { it.categoira }.distinct()
            )
    }

    fun aplicarFiltroTuristicos(categoria: String) {
        val filtrados = filtrarPorCategoria(_lista_original_turisticos, categoria)
        _categorias_mas_lista_lugares_cercanos_turisticos.value =
            ob_categoria_mas_lista_lugares_cercanos(
                lista_data = filtrados,
                lista_categoira = _lista_original_turisticos.map { it.categoira }.distinct()
            )
    }

    fun aplicarFiltroHogar(categoria: String) {
        val filtrados = filtrarPorCategoria(_lista_original_hogar, categoria)
        _categorias_mas_lista_lugares_cercanos_hoga.value =
            ob_categoria_mas_lista_lugares_cercanos(
                lista_data = filtrados,
                lista_categoira = _lista_original_hogar.map { it.categoira }.distinct()
            )
    }

    fun obtner_lugarres_Seguros(lat: Double, lng: Double, localidad: String, radiusKm: Double = 1.0){
        viewModelScope.launch {
            try {
                val datos_lista= instance_repo.obtner_lugares_seguros_cerca(lat,lng,localidad,radiusKm)
                if(datos_lista.isNotEmpty()){
                    _lugares_Seguros_encontrados.value=datos_lista

                }else{
                    _lugares_Seguros_encontrados.value=emptyList()

                }

            }catch (e: Exception){
                _lugares_Seguros_encontrados.value=emptyList()

            }
        }
    }
    fun obtner_lugarres_cercanos(lat: Double, lng: Double, localidad: String, radiusKm: Double = 1.0){
        viewModelScope.launch {
            try {
                val datos_lista= instance_repo.obtener_cantidad_lugares_cercanos(lat,lng,localidad,radiusKm)
                if(datos_lista.isNotEmpty()){
                    _lugares_cercanos_encontrados.value=datos_lista

                }else{
                    _lugares_cercanos_encontrados.value=emptyList()

                }

            }catch (e: Exception){
                _lugares_cercanos_encontrados.value=emptyList()

            }
        }
    }
    fun obtner_lugarres_turisticos(lat: Double, lng: Double, localidad: String, radiusKm: Double = 1.0){
        viewModelScope.launch {
            try {
                val datos_lista= instance_repo.obtner_lugares_seguros_cerca_turismo(lat,lng,localidad,radiusKm)
                if(datos_lista.isNotEmpty()){
                    _lugares_turisticos_encontrados.value=datos_lista

                }else{
                    _lugares_turisticos_encontrados.value=emptyList()

                }

            }catch (e: Exception){
                _lugares_turisticos_encontrados.value=emptyList()

            }
        }
    }
    fun obtner_lugarres_servicios_para_el_hogar(lat: Double, lng: Double, localidad: String, radiusKm: Double = 1.0){
        viewModelScope.launch {
            try {
                val datos_lista= instance_repo.obtener_servicios_esenciales(lat,lng,localidad,radiusKm)
                if(datos_lista.isNotEmpty()){
                    _lugares_esenciales_encontrados.value=datos_lista

                }else{
                    _lugares_esenciales_encontrados.value=emptyList()

                }

            }catch (e: Exception){
                _lugares_esenciales_encontrados.value=emptyList()

            }
        }
    }

    fun filtrarPorCategoria(
        lista: List<lugares_cercanos_>,
        categoriaSeleccionada: String
    ): List<lugares_cercanos_> {

        if (categoriaSeleccionada.isEmpty()) return lista

        return lista.filter { lugar ->
            lugar.categoira.equals(categoriaSeleccionada, ignoreCase = true)
        }
    }

    fun crear_ruta(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        profile: String
    ) {
        viewModelScope.launch {
            try {

                _estadoRuta.value = null // 👈 resetear antes de nueva ruta
                val resultado = instance_repo_mapa_inmobiliara.obtenerRuta(
                    originLat, originLng, destLat, destLng, profile
                )
                _estadoRuta.value = resultado
            } catch (e: Exception) {
                Log.e("RUTA_VM", "💥 ${e.message}")
                _estadoRuta.value = null
            }
        }
    }


    fun setear_puntos_clikeados(
        lista: obj_pasado_clikeado_mapa,
        onPuntoClick: (id: String, lat: Double, lng: Double, img: String, nombre: String, distancia: Double) -> Unit
    ) {
        EstadoMapa.cargandoPuntos.value = true
        val manager = EstadoMapa.managerSecundario.value ?: return
        val mapboxMap = EstadoMapa.mapboxMapGlobal.value ?: return
        val contexto = EstadoMapa.contextoGlobal ?: return

        manager.deleteAll()
        EstadoMapa.idPuntoSeleccionado.value = null  // 👈 reset al limpiar

        if (lista.datos.isEmpty()) {
            EstadoMapa.cargandoPuntos.value = false
            return
        }

        manager.clickListeners.clear()
        manager.addClickListener { annotation ->
            val data = annotation.getData()?.asJsonObject ?: return@addClickListener false

            val id = data.get("id")?.asString ?: "null"
            val lat = data.get("lat")?.asDouble ?: 0.0
            val lng = data.get("lng")?.asDouble ?: 0.0
            val img = data.get("img")?.asString ?: ""
            val nombre = data.get("nombre")?.asString ?: ""
            val distanciaKm = data.get("distanciaKm")?.asDouble ?: 0.0

            // ✅ Resetear tamaño de TODOS los marcadores
            manager.annotations.forEach { it.iconSize = 0.8 }

            // ✅ Agrandar solo el seleccionado
            annotation.iconSize = 1.3
            manager.update(annotation)  // 👈 forzar redibujado

            EstadoMapa.idPuntoSeleccionado.value = id  // 👈 guardar seleccionado

            onPuntoClick(id, lat, lng, img, nombre, distanciaKm)
            true
        }

        EstadoMapa.cargandoPuntos.value = true

        mapboxMap.getStyle { style ->
            MainScope().launch {
                var completados = 0
                val total = lista.datos.size

                lista.datos.forEachIndexed { index, lugar ->
                    val punto = Point.fromLngLat(lugar.lng, lugar.lat)
                    val imageId = "lugar_icon_${lista.tipo}_$index"

                    val data = JsonObject().apply {
                        addProperty("id", lugar.id)
                        addProperty("lat", lugar.lat)
                        addProperty("lng", lugar.lng)
                        addProperty("img", lugar.img_String)
                        addProperty("nombre", lugar.nombre)
                        addProperty("distanciaKm", lugar.distanciaKm)
                    }
                    Log.d("DISTANCIA_SAVE", "id=${lugar.id} | distanciaKm=${lugar.distanciaKm}")

                    try {
                        val bitmap =
                            loadBitmapFromUrl(lugar.img_String, contexto).toCircularBitmap(100)
                        try {
                            style.removeStyleImage(imageId)
                        } catch (_: Exception) {
                        }
                        style.addImage(imageId, bitmap)
                    } catch (e: Exception) {
                        val bitmapFallback = crearCirculoFallback(contexto)
                        try {
                            style.removeStyleImage(imageId)
                        } catch (_: Exception) {
                        }
                        style.addImage(imageId, bitmapFallback)
                    }

                    manager.create(
                        PointAnnotationOptions()
                            .withPoint(punto)
                            .withIconImage(imageId)
                            .withIconAnchor(IconAnchor.CENTER)
                            .withIconSize(0.8)  // 👈 tamaño normal
                            .withData(data)
                    )

                    completados++
                    if (completados == total) {
                        EstadoMapa.cargandoPuntos.value = false
                    }
                }
            }
        }
        EstadoMapa.cargandoPuntos.value = false
    }


    fun crearCirculoFallback(contexto: Context): Bitmap {
        val size = 80
        val bitmap =
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#7C3AED")
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        return bitmap
    }

    fun calcularBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLng = Math.toRadians(lng2 - lng1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val x = Math.sin(dLng) * Math.cos(lat2Rad)
        val y = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng)
        return (Math.toDegrees(Math.atan2(x, y)) + 360) % 360
    }


    fun obtenerPuntoMasCercanoEnSegmento(p: Point, a: Point, b: Point): Point {
        val atp = doubleArrayOf(p.longitude() - a.longitude(), p.latitude() - a.latitude())
        val atb = doubleArrayOf(b.longitude() - a.longitude(), b.latitude() - a.latitude())

        val dot = atp[0] * atb[0] + atp[1] * atb[1]
        val lenSq = atb[0] * atb[0] + atb[1] * atb[1]

        var param = if (lenSq != 0.0) dot / lenSq else -1.0

        param = when {
            param < 0 -> 0.0
            param > 1 -> 1.0
            else -> param
        }

        return Point.fromLngLat(
            a.longitude() + param * atb[0], a.latitude() + param * atb[1]
        )
    }


    // ── Función auxiliar fuera del composable ──────────────────
    fun calcularSegundosEstimados(
        distanciaMetros: Float,
        perfil: String
    ): Int {
        if (distanciaMetros <= 0f) return 0
        val velKmh = when (perfil) {
            "driving" -> 25f
            "walking" -> 3.5f
            "cycling" -> 12f
            else -> 20f
        }
        return (distanciaMetros / (velKmh / 3.6f)).toInt()
    }
}
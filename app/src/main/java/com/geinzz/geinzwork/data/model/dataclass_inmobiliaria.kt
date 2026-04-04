package com.geinzz.geinzwork.data.model

import androidx.compose.runtime.mutableStateOf
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager

data class dataclass_geinz_inmobiliaria_principal(
    val id: String = "",
    val lista_img: List<String> = emptyList(),
    val nombre_inmobiliara: String = "",
    val descripcion: String = "",
    val precio_String: Double = 0.0,
    val localidad: String = "",
    val tipo_propieda: String = "",
    val cantidad_banos: String = "",
    val metros_cuadrados: Double = 0.0,
    val cantidad_dormitrios: String = "",
    val cantidad_cochera: String = "",
    val trato: String = "",
    val medida_fondo: Int = 0,
    val medida_frente: Int = 0
)


data class completeta_info_inmuebles(
    val listaImg: List<String> = emptyList(),
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val ciudad: String = "",
    val banos: String = "",
    val metros: Double = 0.0,
    val habitaciones: String = "",
    val estacionamientos: String = "",
    val direccion: String = "",
    val distrito: String = "",
    val referencia: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val id: String = "",
    val tipoOperacion: String = "",
    val tipoPropiedad: String = "",
    val divisa: String = "",
    val ancho: Int = 0,
    val fondo: Int = 0,
    val cantidad_lugares_seguros: List<lugares_cercanos_> = emptyList(),
    val listalugares_cercanos: List<lugares_cercanos_> = emptyList(),
    val llissa_lugareS_turistos: List<lugares_cercanos_> = emptyList(),
    val lista_servicios_sercanos: List<lugares_cercanos_> = emptyList()

)

data class lista_lugaers_totales(
    val listalugares_cercanos: List<String> = emptyList(), //cercano
    val llissa_lugareS_turistos: List<String> = emptyList(), //seguro
    val lista_servicios_sercanos: List<String> = emptyList() //turistico
)


data class lugares_cercanos_(
    val nombre: String = "",
    val categoira: String = "",
    val img_String: String = "",
    val distanciaKm: Double = 0.0,
    val id: String, val localidad: String = "",
    val lat: Double = 0.0, val lng: Double = 0.0
)


data class ob_categoria_mas_lista_lugares_cercanos (val lista_data:List<lugares_cercanos_> =emptyList(),val lista_categoira: List<String> = emptyList())

data class obj_pasado_clikeado_mapa(
    val tipo: String = "",
    val datos: List<lugares_cercanos_> = emptyList()
)

object EstadoMapa {
    val managerSecundario = mutableStateOf<PointAnnotationManager?>(null)
    val mapboxMapGlobal = mutableStateOf<MapboxMap?>(null)
    var contextoGlobal: android.content.Context? = null
    val cargandoPuntos = mutableStateOf(false)
    var mapViewGlobal: MapView? = null
    var idPuntoSeleccionado = mutableStateOf<String?>(null)

    fun seleccionarPinPorId(id: String) {
        val manager = managerSecundario.value ?: return
        manager.annotations.forEach { annotation ->
            val data = annotation.getData()?.asJsonObject ?: return@forEach
            val annotationId = data.get("id")?.asString ?: return@forEach

            annotation.iconSize = if (annotationId == id) 1.3 else 0.8
        }
        // actualizar todos de una sola vez
        manager.update(manager.annotations)
        idPuntoSeleccionado.value = id
    }
    // ✅ Job para cancelar carga anterior
    var jobCarga: kotlinx.coroutines.Job? = null
    var onClickPunto: ((tipo: String, id: String, localidad: String, img: String, nombre: String, lat: Double, lng: Double) -> Unit)? = null

}

data class perfiles_negocios(val txt: String, val imagen: Int, val nombre_personas: String)

data class datos_geolocalizables(
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val refencia: String
)


data class ia_inmobiliara_tts(
    val cantidad_lugares_seguros: Int,
    val cantidad_lugares_encontrado: Int,
    val cantidad_lugares_turisticos: Int,
    val metros_cuadrados: String,
    val tipo: String,
    val estado: String,
    val nombre_user: String,
    val lista_lugares_cercanos: List<String>,
    val lista_lugares_seguros: List<String>,
    val lista_lugares_turisticos: List<String>,
    val tipo_seleccionado: String,
    val calle_ubicada: String
)

data class datos_viewmodel_inmobiliara(
    val id: String = "",
    val lista_img: List<String> = emptyList(),
    val localidad: String = "",
    val nombre: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val ancho: Int = 0,
    val fondo: Int = 0,
    val precio: Double = 0.0,
    val banos: String = "",
    val metros: Double = 0.0,
    val habitaciones: String = "",

)


data class datos_compartidos_lugares_cercacnos(
    val img: String, val lat: Double, val lng: Double, val id: String
)


data class categorias_diltrado_mapa_inmobiliara(
    val nombre: String, val cantidad: Int,val categoria: List<String>
)

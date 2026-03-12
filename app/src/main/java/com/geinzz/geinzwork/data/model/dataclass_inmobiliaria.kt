package com.geinzz.geinzwork.data.model

import android.R
import kotlinx.serialization.Serializable


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
    val trato: String = ""
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

    val cantidad_lugares_seguros: List<lugares_cercanos_> =emptyList(),
    val listalugares_cercanos: List<lugares_cercanos_> =emptyList(),
    val llissa_lugareS_turistos: List<lugares_cercanos_> =emptyList()
)


data class lugares_cercanos_(val img_String:String,val nombre:String,val categoira:String,val subcategoria: String)

data class datos_geolocalizables(
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val refencia: String
)



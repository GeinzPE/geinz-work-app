package com.geinzz.geinzwork.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue


data class dataclass_repo_agregar_datos(
    var nombre_lugar: String,
    val lat: Double,
    val long: Double,
    val numero_telefono: Int
)

@Parcelize
data class dataclass_lugares_db(
    var descripcion: String = "",
    var categoria: List<String> = emptyList(),
    var direccion: direccion_lugar = direccion_lugar(),
    val horario_atencion: @RawValue Map<String, Any> = emptyMap(),
    val id: String = "",
    val lugar_nombre: String = "",
    var logo_img: String = "",
    val contacto: contacto_lugares_gratis = contacto_lugares_gratis(),
    val pagado: Boolean = false,

    ) : Parcelable

@Parcelize
data class contacto_lugares_gratis(
    val facebook: String = "",
    val ig: String = "",
    val sitio_web: String = "",
    val telefono: List<String> = emptyList(),
    val tk: String = "",
    val whatsapp: List<String> = emptyList(),
) : Parcelable

@Parcelize
data class direccion_lugar(
    val lat: Double = 0.0,
    val log: Double = 0.0,
    val direccion: String = "",
    val refencia: String = ""
) : Parcelable


data class datos_cambiar_cat_sub(
    val nombre_lugar: String,
    val pertenerce_algolia: Boolean,
    val esta_nuevo: Boolean,
    val cat: String,
    val lista_sub: List<String>
)
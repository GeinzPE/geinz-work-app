package com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class dataclass_principal_geinz_work_lugares_turistico(
    val nombre_lugar: String,
    val descripcion_lugar: String
)

data class recomedado(
    val nombre_recomendado: String,
    val texto_recomendao: String,
    val estrellas_recomendad: Int,
    val latitud_: Double,
    val longitud: Double,
    val ref: String,
    val direccion: String
)

data class dataclass_cat_sub(
    val nombre: String?,
    val lista_subcategorias: List<String>?,
    val lista_img: String = ""
)

@Parcelize
data class lugares_turisticos(
    val id_lugar_turistico: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val img_ref: String = "",
    val direcccion: String = "",
    val referencia: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val subcategoria_filtrado: List<String> = emptyList()
) : Parcelable

data class localidades_filtrado(
    val nombre: String = "",
    val lista_img: List<String> = emptyList(),
)

data class Estados_lugares_turisticos(
    val subcategorias: List<String> = emptyList(),
    val lista_filtrada: List<lugares_turisticos> = emptyList()
)

data class datos_principales_user(
    val nombre: String="Usuario",
    val img_perfil: String="",
    val localida: String="barranca"
)
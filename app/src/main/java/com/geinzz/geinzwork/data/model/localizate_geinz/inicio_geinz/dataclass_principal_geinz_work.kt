package com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz

import android.R
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//data class dataclass_principal_geinz_work_lugares_turistico(
//    val nombre_lugar: String,
//    val descripcion_lugar: String
//)
//
//data class recomedado(
//    val nombre_recomendado: String,
//    val texto_recomendao: String,
//    val estrellas_recomendad: Int,
//    val latitud_: Double,
//    val longitud: Double,
//    val ref: String,
//    val direccion: String
//)
//
//data class dataclass_cat_sub(
//    val nombre: String?,
//    val lista_subcategorias: List<String>?,
//    val lista_img: String = ""
//)

@Parcelize
data class lugares_turisticos(
    val id_lugar_turistico: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lista_img: List<String>,
    val img_principal: String,
    val direcccion: String = "",
    val referencia: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val subcategoria_filtrado: List<String> = emptyList()
) : Parcelable


@Parcelize
data class tiendas_mapa(
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
    val dia_aniversariop: Number,
    val mes_aniversario: Number
)

data class Estados_lugares_turisticos(
    val subcategorias: List<String> = emptyList(),
    val lista_filtrada: List<lugares_turisticos> = emptyList()
)

data class datos_principales_user(
    val nombre: String = "Usuario",
    val img_perfil: String = "",
    val localida: String = "barranca"
)

data class ref_ubi(
    val latitud: Double,
    val longitud: Double,
    val referencia: String,
    val direccion: String
)

data class seguridad_salud_publica(
    val nombre: String,
    val tipo: String,
    val img: String,
    val localidad: String,
    val datos_ubi: ref_ubi,
    val numero_contacto: List<String>,
)

data class datos_tienda_free(
    val nombre_: String="",
    val img: String="",
    val ubicacion: String="",
    val referencia: String="",
    val horario_default: String=""
)
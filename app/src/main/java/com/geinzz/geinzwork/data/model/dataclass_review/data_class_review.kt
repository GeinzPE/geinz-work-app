package com.geinzz.geinzwork.data.model.dataclass_review

import android.os.Parcelable
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import kotlinx.android.parcel.Parcelize

data class data_class_review(val id_tienda_lugar: String = "", val localida_lugar: String = "")
@Parcelize
data class data_class_resultado_tienda_lugar(
    val id: String = "",
    val nombre: String = "",
    val imagen: String = "",
    val localidad: String = "",
    val esta_Abierto: Boolean,
    val datos_horario_actual : horario_tienda
): Parcelable

data class datos_review(
    val id_usuario: String="",
    val cantidad_Strar: Int=0,
    val descripcion_review: String="",
    val verificado_presencial: Boolean=false,
    val id_tienda_lugar: String="",
    val localidad_tienda: String="",
    val hora:String="",
    val fecha: String="",
    )


data class datos_review_existenet(
    val calificacion: Number=0,
    val descripcion:String="",
    val fecha_realizada:String=""
)

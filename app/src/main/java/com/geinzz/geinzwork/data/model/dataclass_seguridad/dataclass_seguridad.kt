package com.geinzz.geinzwork.data.model.dataclass_seguridad

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class dataclass_seguridad(
    val nombre_: String = "",
    val direccion: String = "",
    val numero_llamada: List<String>,
    val numero_whatsapp:  List<String>,
    val latidud: Double=0.0,
    val longitud: Double=0.0,
    val referencia: String="",
    val img_ref:String="",
    val categoria:String="",
    val etiqutas_emergencias: List<String>,
    val etiquetas_no_urgente: List<String>,
    val key_alias: List<String>
): Parcelable

data class dialog_seguridad_salud_algolia(
    val lista_whatsapp: List<String>,
    val lista_llamada: List<String>,
    val nombre:String,
    val img: String
)
data class EntidadNLP(
    val key: String,        // "samu"
    val alias: List<String> // ["samu", "ambulancia", "emergencia medica"]
)
data class RespuestaNLP(
    val a: String, // acción
    val t: String, // término
    val c: String,  // confianza
    val g:String,
)

data class FrasePendiente(
    val texto: String,
    val accion :String,
    val termino:String,
    val salud_o_sec:String,
    val categoriazacion:String
)


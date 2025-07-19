package com.geinzz.geinzwork.model

data class dataClass_ubicacion_user(
    val log: String,
    val lat: String,
    val direccion: String,
    val id: String,
    val referencia: String,
    val nombreC:String?
){
    override fun toString(): String {
        return "Referencia : $nombreC"
    }
}

package com.geinzz.geinzwork.data.model.localizate_geinz.cuenta_user

data class cuenta_user(
    val nombre: String = "",
    val apellido: String = "",
    val cod_pais: String = "",
    val correo: String = "",
    val fecha_nac: String = "",
    val fecha_registrada: String = "",
    val genero: String = "",
    val localidad: String = "",
    val nacionalidad_nac: String = "",
    val nombre_user: String = "",
    val img_perfil: String = "",
    val contacto: contacto_cuenta_user? = null)

data class contacto_cuenta_user(
    val cod_telefonico: String,
    val pais_telefono: String,
    val numero_telf: Number
)
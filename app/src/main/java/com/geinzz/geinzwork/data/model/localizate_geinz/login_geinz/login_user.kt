package com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz

data class login_user(
    val nombre: String = "",
    val apellido: String = "",
    val nombre_user: String = "",
    val correo: String = "",
    val numero_celular: Int = 0,
    val cod_telefeno: String = "",
    val nacionalidad_numero: String = "",
    val genero: String = "",
    val localidad: String = "",
    val fecha_nac: String = "",
    val password: String = "",
    val nacionalidad_nacimiento: String="",
    val cod_pais: String=""
)

data class login_google(
    val nombre: String = "",
    val apellido: String = "",
    val nombre_user: String = "",
    val correo: String = "",
    val id: String = "",
    val numero_celular: Int = 0,
    val cod_telefeno: String = "",
    val nacionalidad_numero: String = "",
    val genero: String = "",
    val localidad: String = "",
    val fecha_nac: String = "",
    val nacionalidad_nacimiento: String="",
    val cod_pais: String="",

)


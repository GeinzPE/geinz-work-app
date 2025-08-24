package com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz

data class login_user(
    val nombre: String = "",
    val apellido: String = "",
    val nombre_user: String = "",
    val correo: String = "",
    val numero_celular: Int = 0,
    val genero: String = "",
    val cod_pais: String = "",
    val localidad: String = "",
    val fecha_nac: String = "",
    val password: String=""
)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import kotlinx.serialization.Serializable


@Serializable
object principal

@Serializable
object lugares_turisticos

@Serializable
data class crear_cuenta_geinz(val tipo_completado: String)




@Serializable
data class screen_filtrado(val categoria: String, val localidad: String, val nombre_user: String)

@Serializable
data class mostrar_tiendas(val nombre_user: String, val localidad: String)

@Serializable
data class map_perzonalizado(val tipo: String)





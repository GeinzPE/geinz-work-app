package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.R
import kotlinx.serialization.Serializable


@Serializable
object principal

@Serializable
data class lugares_turisticos(val localidad:String)

@Serializable
data class crear_cuenta_geinz(val tipo_completado: String)

@Serializable
data class screen_filtrado(val categoria: String, val localidad: String, val nombre_user: String)

@Serializable
object login_scios

@Serializable
data class mostrar_tiendas(val nombre_user: String, val localidad: String)

@Serializable
data class map_perzonalizado(val tipo: String,val localidad: String)

@Serializable
data class ui_salud_seguridad(val localidad: String)


@Serializable
object ui_agregar_lugares

@Serializable
data class ui_servicios_tramites(val localidad: String)


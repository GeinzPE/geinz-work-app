package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import kotlinx.serialization.Serializable


@Serializable
object principal

@Serializable
data class  screen_filtrado(val categoria: String,val localidad: String,val nombre_user: String)

@Serializable
object pantalla_principal




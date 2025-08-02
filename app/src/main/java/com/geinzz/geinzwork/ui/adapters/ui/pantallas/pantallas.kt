package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import kotlinx.serialization.Serializable


@Serializable
object principal

@Serializable
data class  screen_filtrado(val categoria: String,val localidad: String)

@Serializable
data object floating_action_button


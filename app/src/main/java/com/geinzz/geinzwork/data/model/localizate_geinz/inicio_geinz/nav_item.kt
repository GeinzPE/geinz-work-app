package com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class nav_item(val nombre_item: String, val icon: ImageVector)

sealed class Items_menu(
    val titulo: String,
    val icono:ImageVector,
    val ruta: String
){
    object pantalla1: Items_menu("Inicio",Icons.Default.Home,"pantalla_principal")
    object pantalla2: Items_menu("Buscar",Icons.Filled.Search,"buscar")
    object pantalla3: Items_menu("Favoritos",Icons.Default.Favorite,"favoritos")
    object pantalla4: Items_menu("Cuenta",Icons.Default.Person,"login_principal")

}

sealed class UiAction {
    data class AbrirPerfil(
        val idTienda: String,
        val localidad: String
    ) : UiAction()

    data class ReviewPublica(
        val idTienda: String,
        val localidad: String
    ) : UiAction()

    data class ReviewPrivada(
        val idTienda: String,
        val localidad: String,
        val lat: Double,
        val lng: Double
    ) : UiAction()

    data class Ruta(
        val id_tienda:String,
        val lat: Double,
        val lng: Double
    ) : UiAction()
}



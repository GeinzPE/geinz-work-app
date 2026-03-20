package com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.geinzz.geinzwork.R

data class nav_item(val nombre_item: String,  val icono_seleccionado: Int,  val icono_deseleccionado:Int)

sealed class Items_menu(
    val titulo: String,
    val icono_seleccionado: Int,
    val icono_deseleccionado:Int,
    val ruta: String
){
    object pantalla1: Items_menu("Inicio",R.drawable.home_seleccionado,R.drawable.home_deseleccionado,"pantalla_principal")
    object pantalla2: Items_menu("Buscar",R.drawable.busqueda_seleccionada,R.drawable.busqueda_deseleccionada,"buscar")
    object pantalla3: Items_menu("Favoritos",R.drawable.favorito_selecionado,R.drawable.favorito_deseleccionado,"favoritos")
    object pantalla4: Items_menu("Cuenta",R.drawable.usuario_selecionado,R.drawable.usuario_deseleccionado,"login_principal")

}



sealed class UiAction {

    data class Abrir_pantalla_promos_cecanas(val id_promocion:String, val localida_tienda:String): UiAction()


    data class abrir_pantalla_inmobiliara(val id_propiedad:String ,val localdiad_pripiedad:String):
        UiAction()

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


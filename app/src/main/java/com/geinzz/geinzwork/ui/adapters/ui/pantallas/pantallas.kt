package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.R
import com.geinzz.geinzwork.data.model.dataclass_geinz_inmobiliaria_principal
import kotlinx.serialization.Serializable


@Serializable
object principal

@Serializable
data class lugares_turisticos(val localidad: String)

@Serializable
data class crear_cuenta_geinz(val tipo_completado: String)

@Serializable
data class screen_filtrado(val categoria: String, val localidad: String, val nombre_user: String)


@Serializable
data class promociones_y_ofertas(
    val localidad: String,
    val id_promo: String = "",
    val ids: List<String>? = null  // ← nullable, default null
)
@Serializable
object login_scios

@Serializable
object abrir_mapa_inmobiliara
@Serializable
data class mostrar_tiendas(val nombre_user: String, val localidad: String)

@Serializable
data class map_perzonalizado(
    val tipo: String,
    val localidad: String,
    val nombre: String?,
    val latitud: Double?,
    val lng: Double?
)

@Serializable
data class ui_salud_seguridad(val localidad: String)


@Serializable
object ui_agregar_lugares


@Serializable
object agregar_pripiedads{}

@Serializable
data class ui_servicios_tramites(val localidad: String, val alias:String)

@Serializable
data class nuevos_negocios_geinz(val localidad: String)


@Serializable
object map_box

@Serializable
data class geinz_inmobiliaria(val localidad_selec: String)


@Serializable
data class datos_completros_inmobiliaria(
    val id: String,
    val localidad: String,
    val nombre_user: String
)
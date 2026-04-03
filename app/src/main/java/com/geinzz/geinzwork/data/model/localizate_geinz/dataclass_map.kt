package com.geinzz.geinzwork.data.model.localizate_geinz

import com.mapbox.geojson.Point
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda

data class dataclass_map(
    val img: String = "",
    val nombre: String = "",
    val tag: List<String> = emptyList(),
    val my_latitud: Double = 0.0,
    val my_longitud: Double = 0.0,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val id: String = "",
    val categoria: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val horario_tienda: horario_tienda = horario_tienda(),
    val contacto_tienda: metodo_contacto_tienda = metodo_contacto_tienda(),
    val metodos_pago_tienda: modelo_pagos_tienda = modelo_pagos_tienda(),
    val horario_box: HorarioAtencion_box = HorarioAtencion_box(),
    val localidad: String = "",
)


data class iconos_creaciones_rutas(val tipo: String, val icono: ImageVector)

data class obj_cuando_creas_rutas(
    val tipo_creado: String = "",
    val icono: ImageVector = Icons.Default.Place,
    val kmxH: Int = 0
)

//sealed class EstadoRuta {
//    object Idle : EstadoRuta()
//    object Cargando : EstadoRuta()
//    object Error : EstadoRuta()
//    data class Exitosa(
//        val puntos: List<Point>,
//        val distanciaMetros: Double,
//        val duracionSegundos: Double
//    ) : EstadoRuta()
//}

data class Exitosa(
    val puntos:  List<Point>,
    val distanciaMetros: Double,
    val duracionSegundos: Double
)
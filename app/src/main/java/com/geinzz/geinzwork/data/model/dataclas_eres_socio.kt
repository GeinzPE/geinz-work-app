package com.geinzz.geinzwork.data.model

import androidx.compose.ui.graphics.Color
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box

data class datos_tienda(
    val id_tienda: String = "",
    val nombre: String = "",
    val img_tienda: String = "",
    val horario_tiendaMap: HorarioAtencion_box = HorarioAtencion_box(),
    val total_vista: Number = 0,
    val total_guardados: Number = 0,
    val clic: Number = 0,
    val fb: Number = 0,
    val ig: Number = 0,
    val tk: Number = 0,
    val stweb: Number = 0,
    val wsap: Number = 0,
    val llamada: Number = 0,
    val ruta: Number = 0,
    val localidad_tienda: String="",
    val fecha_ingreso: String="",
    val fecha_termino: String="",
    val descripcion: String="",
)

data class widget_tienda(
    val total_puntos :String ="",
    val dia_hoy:String="",
    val id_tienda: String="",
    val nombre_tienda: String="",
    val img_tienda: String="",
    val horario_tiendaMap: HorarioAtencion_box = HorarioAtencion_box(),val fecha_termino:String=""
)

data class datos_grafico(
    val enable: Boolean,
    val img_: Int,
    val label: String,
    val cantidad: String
)

data class datos_tienda_fechas(
    val id_tienda: String,
    val fecha_ingreso: String,
    val fecha_termino: String,
    val dias_restantes: String,
    val color: Color
)
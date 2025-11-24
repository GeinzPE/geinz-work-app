package com.geinzz.geinzwork.data.model

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
    val ruta: Number = 0
)

data class datos_grafico(val img_:Int,val label:String,val cantidad:String)
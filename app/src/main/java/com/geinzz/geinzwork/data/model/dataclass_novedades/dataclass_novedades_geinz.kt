package com.geinzz.geinzwork.data.model.dataclass_novedades

import androidx.compose.runtime.Immutable
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda

@Immutable
data class dataclass_novedades_geinz(
    val categoria: String,
    val direccion: String,
    val horario_atencion: HorarioAtencion_box,
    val id_tienda: String,
    val logo_img: String,

    val nombre_tienda:  String,
    val lista_subcateogira: List<String>,
    val descripcion:String,
    val localidad_tienda:String,
    val fecha: Map<String, Any>,

)

package com.geinzz.geinzwork.data.model.dataclass_novedades

import androidx.compose.runtime.Immutable
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda

@Immutable
data class dataclass_novedades_geinz(
    val nombre_tienda: String,
    val localidad_tienda: String,
    val id_tienda: String,
    val categoria: String,
    val logo_img: String,
    val direccion: String,
    val lista_subcateogira: List<String>,
    val horario_atencion: HorarioAtencion_box,
    val descripcion: String,
    val fecha: Map<String, Any>,
)


@Immutable
data class dataclass_novedades_geinz_activar_descativar_aloglia(
    val nombre_tienda: String,
    val localidad_tienda: String,
    val id_tienda: String,
    val categoria: String,
    val logo_img: String,
    val direccion: String,
    val lista_subcateogira: List<String>,
    val horario_atencion: HorarioAtencion_box,
    val descripcion: String,
    val fecha: Map<String, Any>,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohasing: String
)

data class nuevas_teindas_dias(
    val categoria: String,
    val direccion: String,
    val horario_atencion: HorarioAtencion_box,
    val id_tienda: String,
    val logo_img: String,
    val nombre_tienda: String,
    val descripcion: String,
    val lista_subcateogira: List<String>,
    val localidad_tienda: String,
    val fecha: Map<String, Any>,
)

data class compartir_promocion(
    val id_tienda: String ="",
    val localidad: String="",
    val categoria: String="",
)




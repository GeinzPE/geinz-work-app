package com.geinzz.geinzwork.data.model

import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box

data class datos_tienda(
    val id_tienda: String ="",
    val nombre: String ="",
    val img_tienda: String ="",
    val horario_tiendaMap:  HorarioAtencion_box = HorarioAtencion_box()
)
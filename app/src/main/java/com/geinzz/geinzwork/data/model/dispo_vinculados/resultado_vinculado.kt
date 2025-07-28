package com.geinzz.geinzwork.data.model.dispo_vinculados

import com.geinzz.geinzwork.model.dataclass_dispo_vinculados

data class resultado_vinculado(
    val primarioExiste: Boolean,
    val cerrarSeccionMismoDispo: Boolean,
    val soyPrimario: Boolean,
    val esDispositivoActual: Boolean,
    val esPrimarioActual: Boolean,
    val nombre_dispo: String,
    val id_dispo: String
)

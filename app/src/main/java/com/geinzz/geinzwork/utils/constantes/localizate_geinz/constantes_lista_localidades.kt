package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.R

object constantes_lista_localidades {
    val lista = listOf(
        dataclass_localidad_escudos("Barranca".lowercase(), R.drawable.escudo_barranca),
        dataclass_localidad_escudos("Paramonga".lowercase(), R.drawable.escudo_paramonga),
        dataclass_localidad_escudos("Supe".lowercase(), R.drawable.escudo_supe),
        dataclass_localidad_escudos("Pativilca".lowercase(), R.drawable.escudo_pativilca)
    )
    val dias_sema =
        listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")
}
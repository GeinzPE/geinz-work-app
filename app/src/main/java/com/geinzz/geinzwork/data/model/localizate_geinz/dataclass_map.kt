package com.geinzz.geinzwork.data.model.localizate_geinz

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
    val horario_tienda: horario_tienda =horario_tienda(),
    val contacto_tienda: metodo_contacto_tienda= metodo_contacto_tienda()

)

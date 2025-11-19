package com.geinzz.geinzwork.data.model

import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.ref_ubi
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda

data class data_class_tienda_geinz(
    val categoria_tienda: String,
    val descripcion: String,
    val geogash: String,
    val id_tienda: String,
    val localida_tienda: String,
    val modelo_negocio: Boolean,
    val nombre_tienda: String,
    val pagado: Boolean,
    val subcategoria: List<String>,
    val ubicacion: ref_ubi=ref_ubi(),
    val metodo_pago:modelo_pagos_tienda =modelo_pagos_tienda(),
    val metodo_contacto:metodo_contacto_tienda= metodo_contacto_tienda()
)
data class DatosTienda(
    val idTienda: String,
    val localidad: String,
    val categoria: String,
    val subcategorias: List<String>,
    val modeloNegocio: Boolean,
    val pagado: Boolean,
    val nombreLugar: String,
    val descripcion: String
)
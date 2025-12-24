package com.geinzz.geinzwork.data.model

import androidx.compose.ui.graphics.Color
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades

data class datos_tienda(
    val id_tienda: String = "",
    val nombre: String = "",
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
    val perfil_qr : Number=0,
    val review_c_qr : Number=0,
    val review_qr : Number=0,
    val crear_ruta_qr : Number=0,
    val localidad_tienda: String = "",
    val fecha_ingreso: String = "",
    val fecha_termino: String = "",
    val descripcion: String = "",
    val lista_ids_propietarios: List<String> = emptyList(),
    val saldo_disponible_tienda: Number = 0,
    val compartidos: Number = 0,
    val obtener_img_tiendas:obtener_img_tiendas =obtener_img_tiendas(),
    val metodos_pago :modelo_pagos_tienda = modelo_pagos_tienda(),
    val metodo_contacto_tienda:metodo_contacto_tienda= metodo_contacto_tienda(),
    val servicios_comodidades: List<servicio_comodidad> =  emptyList(),
    val aforo: Number=0,
)

data class widget_tienda(
    val total_puntos: String = "",
    val dia_hoy: String = "",
    val id_tienda: String = "",
    val nombre_tienda: String = "",
    val img_tienda: String = "",
    val horario_tiendaMap: HorarioAtencion_box = HorarioAtencion_box(),
    val fecha_termino: String = "",
    val localidad_tienda: String = "",
    val categoira_tienda: String = ""
)


data class obtener_img_tiendas(
    val logo_tienda: String = "",
    val lista_ambiernte: List<String> = emptyList(),
    val lista_productos: List<String> = emptyList(),
    val lista_promociones: List<String> = emptyList()
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
    val color: Color,
    val saldo_cuenta_tienda: String
)

data class servicio_comodidad(
    val nombre: String,
    val estado: Boolean
)


data class cambiar_datos_pago_contacto(val id_tienda:String,val localida:String)
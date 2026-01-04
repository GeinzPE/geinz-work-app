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
    val perfil_qr: Number = 0,
    val review_c_qr: Number = 0,
    val review_qr: Number = 0,
    val crear_ruta_qr: Number = 0,
    val localidad_tienda: String = "",
    val fecha_ingreso: String = "",
    val fecha_termino: String = "",
    val descripcion: String = "",
    val lista_ids_propietarios: List<String> = emptyList(),
    val saldo_disponible_tienda: Number = 0,
    val compartidos: Number = 0,
    val obtener_img_tiendas: obtener_img_tiendas = obtener_img_tiendas(),
    val metodos_pago: modelo_pagos_tienda = modelo_pagos_tienda(),
    val metodo_contacto_tienda: metodo_contacto_tienda = metodo_contacto_tienda(),
    val servicios_comodidades: List<servicio_comodidad> = emptyList(),
    val aforo: Number = 0,
    val categoira_tienda: String = "",
    val subcategorias_tienda: List<String> = emptyList(),
    val ubicacion: ubicacaion_container = ubicacaion_container()
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
    val lista_promociones: Map<String, String> = emptyMap()
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

data class agregar_promociones(
    val exclusivo: Boolean,
    val fechas: fechas_promociones,
    val img_container: img_contaier,
    val informacion: informacion_container,
    val ubicacion: ubicacaion_container
)

data class fechas_promociones(val inicio: String, val fin: String, val activo: Boolean)

data class img_contaier(val lista_img: List<String> = emptyList(), val logo_img: String = "")

data class informacion_container(
    val categoria: String,
    val descripcion: String,
    val id_promocion: String,
    val id_tienda: String,
    val nombre_tienda: String,
    val titulo: String,
    val numero: String,
    val compartir: Boolean,
    val contactar: Boolean
)

data class ubicacaion_container(
    val direccion: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val referencia: String = ""
)


data class cambiar_datos_pago_contacto(val id_tienda: String, val localida: String)

data class items_pantallas_promociones(
    val categoira_tienda: String = "",
    val nombre_tienda: String = "",
    val localidad_tienda: String = "",
    val img_tienda: String = "",
    val numero_contacto_tienda: String = "",
    val subcategorias_tienda: List<String> = emptyList(),
    val ubicacion: ubicacaion_container = ubicacaion_container(),
    val saldo: Number = 0,
    val id_tienda: String = ""
)

data class obj_contador_notificaciones(
    val id_tienda: String,
    val localida: String,
    val categoria: String,
    val idnotificacion: String,
    val fecha_enviada: String,
    val precio_envio: Number,
    val parametros_notificacion: obj_parametros_notificacion,
    val suspendido: obj_suspend_notificacion,
    val tipo_notificacion: String,
    val nombre_tienda: String,
    val numero_contacto_tienda: String,
    val categoira_tienda: String,
)

data class obj_suspend_notificacion(
    val suspendido: Boolean = false,
    val descrpcion_suspencion: String = ""
)


data class obj_parametros_notificacion(
    val titulo_notificacion: String,
    val texto_notificacion: String,
    val logo_notificacion: String,
    val img_notifiacion: String,
    val priorida_notificacion: String,
    val tipo_notificacion: String,
    val notificacion_publicidad: Boolean,
    val id_publicacion_anuncio: String,
)

data class datos_publicaciones_realizadas(
    val titulo: String,
    val descripcion: String,
    val activo: Boolean,
    val fecha_publicado: String,
    val id: String,
    val img: String
)
package com.geinzz.geinzwork.data.model

import androidx.compose.ui.graphics.Color
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    val localidad_tienda: String = "",
    val categoira_tienda: String = "",
    val fecha_fin_panel: StateFlow<String> = MutableStateFlow("")
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
    val estado: String = "",
    val exclusivo: Boolean,
    val img_container: img_contaier,
    val informacion: informacion_container,
    val ubicacion: ubicacaion_container,
    val datos_hora_fecha: datos_fecha_hora_tipo,
    val formato_fecha_hora: String,
    val mensaje_predeterminado: msjes_predeteminados_generales
)

data class datos_fecha_hora_tipo(
    val horas: fechas_horas_promociones,
    val dias: fechas_promociones
)

data class fechas_horas_promociones(
    val hora_inicio: String = "",
    val hora_fin: String = "",
    val activo: Boolean = false,
    val timestamp_inicio: Long = 0L,
    val timestamp_fin: Long = 0L
)

data class fechas_promociones(
    val fecha_inicio: String = "",
    val fecha_fin: String = "",
    val activo: Boolean = false,
    val timestamp_inicio: Long = 0L,
    val timestamp_fin: Long = 0L
)

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

data class msjes_predeteminados_generales(
    val compartir: mensaje_predeterminado = mensaje_predeterminado(),
    val whatsapp: mensaje_predeterminado = mensaje_predeterminado()
)

data class mensaje_predeterminado(
    val msje_predermindo: String = "", val activo_o_no: Boolean = false
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
    val id_img_storage: String,
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
    val vence_en: String,
    val id: String,
    val img: String
)

data class datos_recarga(
    val accesos: List<String>,
    val descripcion: String,
    val monedas: String,
    val monedas_agregadas: String,
    val monedas_inicial: String,
    val nombre_plan: String,
    val precio_soles: String
)

data class historial_recargas(
    val tipo_transaccion: String,
    val fecha: String, val hora: String,
    val id_recarga: String,
    val localidad_tienda: String,
    val id_tienda: String,
    val nombre_tienda: String,
    val tipo: String,
    val monto: String,
    val precio_soles: String,
    val yape: Boolean,
    val plin: Boolean, val estado: String, val monto_posterior: Int
)

data class historial_descuento(
    val tipo_transaccion: String,
    val fecha: String,
    val hora: String,
    val id_recarga: String,
    val localidad_tienda: String,
    val id_tienda: String,
    val nombre_tienda: String,
    val monto_descuento: String,
    val tipo: String,
    val precio_soles: String, val estado: String, val monto_restante: Int
)


data class recargar_monedas_tienda(
    val id_tienda: String,
    val localidad_tienda: String,
    val cantidad: String
)

data class NotificacionIA(
    val titulo: String,
    val descripcion: String
)

data class OpcionPromocionIA(
    val titulo: String,
    val descripcion: String
)

data class nombre_precio_notificaciones(val tipo: String, val precio: Int)


data class historial_financiero(
    val id_transaccion: String,
    val monedas: Number,
    val hora: String,
    val fecha: String,
    val nombre_tienda: String,
    val precio_soles: String,
    val tipo_realziado: String,//recarga o descuento,
    val tipo_transaccion: String, //generacion por ia o publicdad
    val estodo: String,
    val monto_restante: Number
)


data class EstadisticasPromo(
    val vistas: Int = 0,
    val compartidos: Int = 0,
    val whatsapp: Int = 0
)

data class publicaciones_notificaciones_geinz(
    val id: String,
    val img_principal: String,
    val nombre: String,
    val tipo: String,
    val estado: String,
    val realizado: String,
    val vence: String,
    val total_gastado: String,
    val estado_publicacion:String,
)

data class obtener_datos_promociones(
    val lista_img: List<String>,
    val categoira: String,
    val compartir: Boolean,
    val contactar: Boolean,
    val id_promocion: String,
    val descripcion: String,
    val titulo: String,
    val numero: String,
    val fecha_iniciada: Long,
    val fecha_terminada: Long
)

data class DatosDemograficosUsuario(
    val localidad: String,
    val nacionalidad: String,
    val genero: String,
    val edad: Int,
)

data class EstadoNotificaciones(
    val restantes: Int,
    val fechaFin: String? = null
)



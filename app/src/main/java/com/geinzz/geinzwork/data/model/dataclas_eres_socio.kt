package com.geinzz.geinzwork.data.model

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime

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
    val descripcion_chat_bot_whatsapp:String ="",
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
    val lista_promociones: Map<String, String> = emptyMap(),
    val logo_whatsapp_bot : String =""
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
    val mensaje_predeterminado: msjes_predeteminados_generales,
    val generaciones_con_ia: generaciones_con_ia,
    val precio_publicacion: precio_rango_publicacion,
    val horario_deseado: horario_deseado,
    val metodos_pagos:metodos_pagos_agregados_publiaciones,
    val servicios_comoidades:ComodidadesAgregadas
)

data class metodos_pagos_agregados_publiaciones(
    val yape: Boolean=false,
    val plin: Boolean=false,
    val agora: Boolean=false,
    val efectivo: Boolean=false,
    val visa: Boolean=false,
    val mastercard: Boolean=false
)


data class ComodidadesAgregadas(
    val zonaExpandida: Boolean = false,
    val wifi: Boolean = false,
    val serviciosHigienicos: Boolean = false,
    val camarasSeguridad: Boolean = false,
    val salaEspera: Boolean = false,
    val salaJuegos: Boolean = false,
    val mesaParaNinos: Boolean = false,
    val ingresoConMascotas: Boolean = false,
    val estacionamiento: Boolean = false,
    val enchufe: Boolean = false,
    val aireAcondicionado: Boolean = false
)


data class generaciones_con_ia(
    val titulo_original: String,
    val descripcion_original: String,
    val lista_generaciones: List<OpcionPromocionIA>,
    val generacion_selecionada: contenido_publicidad,
    val generacion_wsap: String,
    val generacion_compartir: String
)

data class generacion_primarios(
    val titulo_original: String,
    val descripcion_original: String,
    val lista_generaciones: List<OpcionPromocionIA>,
)

data class generaciones_con_ia_notificaciones(
    val titulo_original: String,
    val descripcion_original: String,
    val generacion_selecionada: contenido_publicidad,
    val generacion_wsap: String,
)

data class generaciones_con_ia_notificaciones_solo_generaciones(
    val titulo_original: String,
    val descripcion_original: String,
    val generacion_selecionada: contenido_publicidad,

    )

data class contenido_publicidad(val titulo: String, val descripcion: String)

data class datos_fecha_hora_tipo(
    val horas: fechas_horas_promociones,
    val dias: fechas_promociones
)

data class fechas_horas_promociones(
    val hora_inicio: String = "",
    val hora_fin: String = "",
    val activo: Boolean = false,
    val timestamp_inicio: Timestamp = Timestamp.now(),
    val timestamp_fin: Timestamp = Timestamp.now()
)

data class fechas_promociones(
    val fecha_inicio: String = "",
    val fecha_fin: String = "",
    val activo: Boolean = false,
    val timestamp_inicio: Timestamp = Timestamp.now(),
    val timestamp_fin: Timestamp = Timestamp.now()
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
    val contactar: Boolean,

    )

data class horario_deseado(val seleccion: String, val horario: String)
data class precio_rango_publicacion(val precio: String, val rango: String)

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
    val id_tienda: String = "",
    val metodosPago: modelo_pagos_tienda = modelo_pagos_tienda(),
    val serviciosComodidades: List<servicio_comodidad> = emptyList(),
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
    val fecha_caducidad: Timestamp,
    val generaciones_con_ia_notificaciones: generaciones_con_ia_notificaciones
)

data class obj_suspend_notificacion(
    val suspendido: Boolean = false,
    val descrpcion_suspencion: String = ""
)


data class obj_parametros_notificacion(
    val titulo_notificacion: String,
    val texto_notificacion: String,
    val logo_notificacion: String,
    val img_notifiacion: String = "",
    val priorida_notificacion: String,
    val tipo_notificacion: String,
    val notificacion_publicidad: Boolean,
    val id_publicacion_anuncio: String,
    val mensaje_programado_whatsap: String
)

data class datos_publicaciones_realizadas(
    val titulo: String,
    val descripcion: String,
    val vence_en: String,
    val id: String,
    val img: String,
    val texto_whatsapp: String,
    val timestamp_fin: Timestamp
)

data class datos_recarga(
    val id_plan_select: String,
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
    val tipo: repo_pantallas_promocionar.TipoGeneracionIA,
    val titulo: String,
    val descripcion: String
)

data class NotificacionIA_dialog(
    val id_promo_noti_gen: String = "",
    val titulo: String = "",
    val descripcion: String = ""
)


data class dialog_generaciones_IA_promo_noti(
    val id_promo_noti_gen: String,
    val titulo: String,
    val descripcion: String
)

data class OpcionPromocionIA(
    val tipoIA: repo_pantallas_promocionar.TipoGeneracionIA? = null,
    val titulo: String,
    val descripcion: String
)

data class nombre_precio_notificaciones(val tipo: String, val precio: Int)

data class GeneracionIA(
    val tipo: repo_pantallas_promocionar.TipoGeneracionIA,
    val beneficios: List<String>
)

data class tipo_de_genearcion_para_imagen(
    val tipo:String,
    val realiazar_promp: String)


data class historial_financiero(
    val id_transaccion: String,
    val monedas: Number,
    val hora: String,
    val fecha: String,
    val nombre_tienda: String,
    val precio_soles: String,
    val tipo_realziado: String,
    val tipo_transaccion: String,
    val estodo: String,
    val monto_restante: Number,
    val dateTime: LocalDateTime // 🔥 Campo extra para ordenar
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
    val estado_publicacion: String, val fechaOrden: Long
)

data class TiempoPromo(
    val duracion: String,
    val transcurrido: String
)

data class CostoPromo(
    val total: Double,
    val consumido: Double
)

data class obtener_datos_promociones(
    val estado: String,
    val horas_o_fecha: String,
    val lista_img: List<String>,
    val categoira: String,
    val compartir: Boolean,
    val contactar: Boolean,
    val id_promocion: String,
    val descripcion: String,
    val titulo: String,
    val numero: String,
    val fecha_iniciada: String,
    val fecha_terminada: String,
    val duracion_total: String = "",      // 🔥 ej: "10 días" / "5 horas"
    val tiempo_transcurrido: String = "", // 🔥 ej: "3 días" / "2 horas"
    val costo_total: Double = 0.0,        // 🔥 inversión total
    val costo_consumido: Double = 0.0,    // 🔥 ya cobrado
    val estadisticas: EstadisticasPromoGenerales?,
    val mensaje_predeterminado: msjes_predeteminados_generales,
    val rango_publicacion: String,
    val precio_publicacion: String,
    val horaio_publicacion: String,
    val metodos_pagos:metodos_pagos_agregados_publiaciones,
    val servicios_comoidades:ComodidadesAgregadas
)


data class EstadisticasPromoGenerales(
    val click: EstadisticaAccion? = null,
    val vistas: EstadisticaAccion? = null,
    val compartidos: EstadisticaAccion? = null,
    val whatsapp: EstadisticaAccion? = null
)

data class EstadisticaAccion(
    val tipo: String = "", // 👈 CLAVE
    val total: Int = 0,
    val edad: Map<String, TotalEstadistica> = emptyMap(),
    val genero: Map<String, TotalEstadistica> = emptyMap(),
    val localidad: Map<String, TotalEstadistica> = emptyMap(),
    val por_dia: Map<String, TotalEstadistica> = emptyMap()
)

data class TotalEstadistica(
    val total: Int = 0
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


data class notificaciones(
    val fecha_enviada: String,
    val datos_de_notificacion: datos_de_notificacion,
    val parametros_notificacion: parametros_notificacion,
    val EventoEstadisticas: EventosNotificacion
)

data class datos_de_notificacion(
    val categoira: String,
    val img_id_storage: String,
    val id_tienda: String,
    val img_notifiacion: String,
    val localidad: String,
    val logo_notificacion: String,
    val numero_contacto: String,
    val texto_notificacion: String,
    val titulo_notificacion: String,

    )

data class parametros_notificacion(
    val id_noti: String,
    val id_promo_anuncio: String,
    val notificacion_nuevo: Boolean,
    val prioridad_notificacion: String,
    val tipo_notificacion: String,
    val tipo_precio: String,
    val total_gastado: String,
    val enviados: String,
    val fallidos: String,
    val mensaje_predeterminado: String
)


//// 📌 Estadísticas generales de un evento (clic, vista, cerrar, etc.)
//data class EventoEstadisticas(
//    val total: Long = 0,                 // Total acumulado
//    val porDia: Map<String, Long> = mapOf(), // Total por día: "2026-01-16" -> 2
//    val valores: Map<String, Long> = mapOf() // Valores numéricos asociados (ej. tiempo en segundos)
//)

// 📌 Alcance único de usuarios
data class AlcanceUsuarios(
    val total: Long = 0,                 // Total de usuarios únicos
    val usuarios: Map<String, Long> = mapOf() // uid -> timestamp o 1
)

// 📌 Estadísticas demográficas
data class DemografiaEvento(
    val localidad: Map<String, Long> = mapOf(), // ej: "barranca" -> 20
    val genero: Map<String, Long> = mapOf(),    // ej: "masculino" -> 15
    val edad: Map<String, Long> = mapOf()       // ej: "18-25" -> 10
)

// 📌 Estadísticas completas de un evento
data class EstadisticasEvento(
    val total: Long = 0,                        // Total general
    val porDia: Map<String, Long> = mapOf(),    // Total por día
    val valores: Map<String, Long> = mapOf(),   // Valores (tiempo en segundos)
    val alcanceUsuarios: AlcanceUsuarios = AlcanceUsuarios(),
    val demografia: DemografiaEvento = DemografiaEvento()
)


// 📌 Estadísticas completas de una promoción
data class EstadisticasPromocion(
    val idPromo: String,
    val eventos: Map<String, EstadisticasEvento> = mapOf() // "VISTA", "CLICK_ANUNCIO", "CERRAR_ANUNCIO"...
)


data class TiempoPorDia(
    val total_segundos: Long = 0,
    val eventos: Int = 0
)


data class EventosNotificacion(
    val cerrar_anuncio: CerrarAnuncioEstadisticas = CerrarAnuncioEstadisticas(),
    val click: EventoEstadisticas = EventoEstadisticas(),
    val click_anuncio: EventoEstadisticas = EventoEstadisticas(),
    val click_perfil: EventoEstadisticas = EventoEstadisticas(),
    val click_whatsapp: EventoEstadisticas = EventoEstadisticas(),
    val vista: EventoEstadisticas = EventoEstadisticas()
)


data class CerrarAnuncioEstadisticas(
    val total: Int = 0,
    val porDia: Map<String, TiempoPorDia> = emptyMap(),
    val promedio_segundos: Double = 0.0
)


data class EventoEstadisticas(
    val total: Long = 0L,
    val porDia: Map<String, Long> = emptyMap(),
    val edad: Map<String, Long> = emptyMap(),
    val genero: Map<String, Long> = emptyMap(),
    val localidad: Map<String, Long> = emptyMap()
)


data class Res_precios(
    val preciosDetectados: List<Double> = emptyList(),
    val precioFinal: Double? = null,     // null si hay más de uno
    val rango: String? = null // null si hay más de uno
)

data class pantalla_horarios(val nombre: String, val texto: String, val horario_mostrado: String)

data class carta_promociones_geinz_vista_previa(
    val lista_img_uri: List<Uri> = emptyList(),
    val logo_img: String = "",
    val nombre_tienda: String = "",
    val titulo_publicacion: String = "",
    val dias_restantes: String = "",
    val compartir: Boolean = false,
    val contactar: Boolean = false
)


data class datos_gen_IA_Tiendas(
    val inicio: Timestamp,
    val fin: Timestamp?,
    val id_promo_noti_cread: String,
    val img_container: String,
    val nombre_generacion: String,
    val tipo_realizado: String,
    val datos_generaciones: datos_generaciones_IA,
    val nuevas_generaciones: nuevas_generaciones_con_IA,
    val terminos: List<String>,
    val fecha_normal: LocalDate
)

data class datos_generaciones_IA(
    val titulo_original: String,
    val descripcion_original: String,
    val tipo_generacion_IA: String,
    val generacion_wsap: String,
    val generacion_compartir: String,
    val generaciones: List<lista_genereracione>,
    val titulo_seleccionado_gen_IA: String,
    val descripcion_seleccionada_ge_IA: String
)

data class obt_item_gen_IA(
    val id_generacion: String,
    val img_: String,
    val titulo_gen_IA: String,
    val vencimiento: Timestamp?,
    val inicio: Timestamp,
    val tipo: String,
    val generacion_wsap: String,
    val generacion_compartida: String,
    val generacion_origini: lista_genereracione,
    val lista_generaciones: List<lista_genereracione>
)

data class lista_genereracione(
    val tipo: String = "Original",
    val titulo: String,
    val descripcion: String
)


data class DatosPublicidadIA(
    val titulo: String = "",
    val descripcion: String = "",
    val whatsapp: String = "",
    val compartir: String = "",
    val tipo_redirigido: String = "",
    val id_generacion_sin_publicar: String? = null,
    val datos_generaciones: datos_generaciones_sin_publicaicones = datos_generaciones_sin_publicaicones()
)

data class datos_notificacion(
    val titulo_original: String = "",
    val descripcion_original: String = "",
    val titulo_select: String = "",
    val descripcion_select: String = "",
    val id_generacion_sin_publicar: String = ""
)


data class nuevas_notificaciones(
    val titulo: String = "",
    val descripcion: String = "",
    val tipo_redirigido: String = "",
    val id_generacion_sin_publicar: String = "",
    val datos_generaciones: datos_generaciones_sin_publicaicones = datos_generaciones_sin_publicaicones()
)

data class datos_generaciones_sin_publicaicones(
    val lista_obciones: List<OpcionPromocionIA>? = null,
    val titulo_original: String? = null,
    val descripcion_original: String? = null,
    val titulo_seleccionado: String? = null,
    val descripcion_seleccionada: String? = null
)


sealed class IconoIA {
    data class Drawable(@DrawableRes val resId: Int) : IconoIA()
    data class Vector(val imageVector: ImageVector) : IconoIA()
}


data class nuevas_generaciones_con_IA(
    val titulo_nuevo: String,
    val descripcion_nueva: String,
    val fecha_nueva_generacion: String,
    val titulo_anterior: String,
    val descripcion_anteriror: String
)

data class datos_para_generacion_dialog_historial_IA(
    val nombre_tienda: String,
    val monedas_tienda: Int,
    val localidad_tienda: String,
    val id_tienda: String
)

data class EstadoUI(
    val valor: Int,   // 👈 ESTE es el número que quieres retornar
    val texto: String,
    val color: Color
)


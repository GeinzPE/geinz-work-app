package com.geinzz.geinzwork.data.model.daniel_metricas

import java.util.Date

// ─── MODELOS ──────────────────────────────────────────────────────────────────

data class EstadisticaDiaria(
    val fecha           : String = "",
    val clicks_whatsapp : Int    = 0,  // "clicks" en Firestore
    val monedas_clicks  : Int    = 0,  // "monedasGastadas" en Firestore
    val enviados_publi  : Int    = 0   // "enviados" en Firestore
)

data class HistorialHotItem(
    val id: String = "",
    val creditos_antes: Long = 0,
    val creditos_despues: Long = 0,
    val monedas_descontadas: Long = 0,
    val timestamp: Date? = null,
    val tipo: String = "",
    val token_id: String = ""
)

data class InteraccionDirectaItem(
    val id: String = "",
    val createdAt: Date? = null,
    val estado: String = "",
    val fin: Date? = null,
    val inicio: Date? = null,
    val historial_id: String = "",
    val monedas: Long = 0,
    val usado: Boolean = false
)

data class MetricasResumen(
    // Clicks WhatsApp
    val clicks_whatsapp_hoy     : Int    = 0,
    val monedas_gastadas_hoy    : Int    = 0,
    val clicks_whatsapp_semana  : Int    = 0,
    val monedas_gastadas_semana : Int    = 0,

    // Publicidad
    val total_publicidad_enviada: Int    = 0,
    val monedas_en_publicidad   : Long   = 0L,

    // Para gráfica
    val historial_reciente      : List<HistorialHotItem> = emptyList() ,// ← agregar esto
    val total_clicks_historico           : Int  = 0,   // suma histórica de clicks WhatsApp
    val total_monedas_contacto_historico : Long = 0L,
)
package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.daniel_metricas.EstadisticaDiaria
import com.geinzz.geinzwork.data.model.daniel_metricas.HistorialHotItem
import com.geinzz.geinzwork.data.model.daniel_metricas.InteraccionDirectaItem
import com.geinzz.geinzwork.data.model.daniel_metricas.MetricasResumen
import com.geinzz.geinzwork.data.model.precios_bot
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class repo_metricas_daniel_wsap {

    private val db_sec: FirebaseFirestore by lazy {
        FirebaseSecundario.getFirestore()
    }



    /**
     * Estadísticas de UN día específico
     * Firestore: /creditos_tienda/{id_tienda}/estadisticas/{yyyy-MM-dd}
     *
     * clicks          → cuántas veces el usuario tocó el botón de WhatsApp
     * monedasGastadas → cuántas monedas se descontaron por esos clicks
     * enviados        → cuántas plantillas (publicidad) se enviaron ese día
     */
    suspend fun obtenerEstadisticasDia(id_tienda: String, fecha: String): Result<EstadisticaDiaria> {
        return try {
            val doc = db_sec.collection("creditos_tienda")
                .document(id_tienda)
                .collection("estadisticas")
                .document(fecha)
                .get()
                .await()

            val data = EstadisticaDiaria(
                fecha           = fecha,
                clicks_whatsapp = (doc.getLong("clicks")          ?: 0).toInt(),
                monedas_clicks  = (doc.getLong("monedasGastadas") ?: 0).toInt(),
                enviados_publi  = (doc.getLong("enviados")        ?: 0).toInt()
            )
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Estadísticas de los últimos N días (para gráfica o resumen)
     */
    suspend fun obtenerEstadisticasUltimosDias(
        id_tienda: String,
        dias: Int = 7
    ): Result<List<EstadisticaDiaria>> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")) // ✅ también el calendar
        val estadisticas = mutableListOf<EstadisticaDiaria>()

        return try {
            repeat(dias) {
                val fecha  = formatter.format(calendar.time)
                val result = obtenerEstadisticasDia(id_tienda, fecha)
                estadisticas.add(result.getOrNull() ?: EstadisticaDiaria(fecha = fecha))
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            Result.success(estadisticas.reversed()) // orden cronológico: más antiguo → hoy
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Historial de publicidad enviada
     * Firestore: /creditos_tienda/{id_tienda}/historial_hot/
     *
     * Sirve para saber cuánto gastó el negocio en publicidad (plantillas)
     * monedas_descontadas → costo de cada envío
     * tipo                → "recomendacion_asistente" u otro
     * timestamp           → cuándo se hizo
     */
    suspend fun obtenerHistorialPublicidad(
        id_tienda: String,
        limite: Int = 50
    ): Result<List<HistorialHotItem>> {
        return try {
            val snapshot = db_sec.collection("creditos_tienda")
                .document(id_tienda)
                .collection("historial_bot_envios")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()

            val lista = snapshot.documents.map { doc ->
                HistorialHotItem(
                    id                  = doc.id,
                    creditos_antes      = doc.getLong("saldo_antes")      ?: 0,
                    creditos_despues    = doc.getLong("saldo_despues")    ?: 0,
                    monedas_descontadas = doc.getLong("monedas_descontadas") ?: 0,
                    timestamp           = doc.getTimestamp("timestamp")?.toDate(),
                    tipo                = doc.getString("tipo")              ?: "",
                    token_id            = doc.getString("token_id")          ?: ""
                )
            }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RESUMEN FINAL — lo que le muestra al usuario
     *
     * Métricas útiles:
     *  • clicks_whatsapp_hoy      → cuántas personas tocaron WhatsApp hoy
     *  • monedas_gastadas_hoy     → cuánto costaron esos clicks hoy
     *  • clicks_whatsapp_semana   → total clicks últimos 7 días
     *  • monedas_gastadas_semana  → total monedas en clicks últimos 7 días
     *  • total_publicidad_enviada → cuántas plantillas se mandaron (historial_hot)
     *  • monedas_en_publicidad    → cuánto se gastó en total en publicidad
     *  • costo_promedio_publi     → promedio de monedas por plantilla enviada
     */
    suspend fun obtenerResumenMetricas(id_tienda: String): Result<MetricasResumen> {
        return try {
            val fmtUTC = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val hoy = fmtUTC.format(Date())

            val estadHoy    = obtenerEstadisticasDia(id_tienda, hoy).getOrNull()
                ?: EstadisticaDiaria(fecha = hoy)
            val estadSemana = obtenerEstadisticasUltimosDias(id_tienda, 30).getOrNull()
                ?: emptyList()
            val historial   = obtenerHistorialPublicidad(id_tienda, limite = 200).getOrNull()
                ?: emptyList()

            val ultimos7          = estadSemana.takeLast(7)
            val clicksSemana      = ultimos7.sumOf { it.clicks_whatsapp }
            val monedasSemana     = ultimos7.sumOf { it.monedas_clicks }
            // ✅ enviados de los últimos 7 días desde estadisticas
            val enviadosSemana    = ultimos7.sumOf { it.enviados_publi }
            val totalMonPubli     = historial.sumOf { it.monedas_descontadas }

            val resumen = MetricasResumen(
                clicks_whatsapp_hoy      = estadHoy.clicks_whatsapp,
                monedas_gastadas_hoy     = estadHoy.monedas_clicks,
                clicks_whatsapp_semana   = clicksSemana,
                monedas_gastadas_semana  = monedasSemana,

                // ✅ enviados desde el doc de estadisticas, no del historial
                enviados_hoy             = estadHoy.enviados_publi,
                enviados_semana          = enviadosSemana,

                total_publicidad_enviada = historial.size,
                monedas_en_publicidad    = totalMonPubli,
                historial_reciente       = historial,
                total_clicks_historico           = estadSemana.sumOf { it.clicks_whatsapp },
                total_monedas_contacto_historico = estadSemana.sumOf { it.monedas_clicks }.toLong(),
            )
            Result.success(resumen)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }}
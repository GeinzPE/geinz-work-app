package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.obtener_datos_promociones
import com.geinzz.geinzwork.data.model.publicaciones_notificaciones_geinz
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.emptyMap
import kotlin.collections.get
import kotlin.math.ceil
import kotlin.math.floor

class repo_pantalla_recientes {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_notificacion_publicaciones(
        id_tienda: String,
        localidad: String
    ): List<publicaciones_notificaciones_geinz> = try {
        val tiendaRef = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)

        coroutineScope {
            val promocionesDeferred =
                async { tiendaRef.collection("promociones_geinz").get().await() }
            val notificacionesDeferred =
                async { tiendaRef.collection("notificaciones_enviadas").get().await() }

            val promocionesSnapshot = promocionesDeferred.await()
            val notiSnapshot = notificacionesDeferred.await()

            val ahora = System.currentTimeMillis()

            val listaPromos = promocionesSnapshot.documents.map { doc ->
                val img_container = doc.get("img_container") as? Map<String, Any> ?: emptyMap()
                val lista = img_container.get("lista_img") as? List<String> ?: emptyList()
                val informacion = doc.get("informacion") as? Map<String, Any> ?: emptyMap()
                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""
                val hora_fecha_general =
                    doc.get("datos_hora_fecha") as? Map<String, Any> ?: emptyMap()
                val horasMap = hora_fecha_general["horas"] as? Map<String, Any> ?: emptyMap()
                val diasMap = hora_fecha_general["dias"] as? Map<String, Any> ?: emptyMap()

                val hora_inicio = horasMap["hora_inicio"] as? String ?: ""
                val hora_fin = horasMap["hora_fin"] as? String ?: ""
                val timestamp_inicio = (horasMap["timestamp_inicio"] as? Number)?.toLong() ?: 0L
                val dia_inicio = diasMap["fecha_inicio"] as? String ?: ""
                val dia_fin = diasMap["fecha_fin"] as? String ?: ""

                val timestampFin = when (tipo_hora_dias) {
                    "horas" -> (horasMap["timestamp_fin"] as? Number)?.toLong() ?: 0L
                    "dias" -> (diasMap["timestamp_fin"] as? Number)?.toLong() ?: 0L
                    else -> 0L
                }
                val timestampFinMs =
                    if (timestampFin < 1_000_000_000_000L) timestampFin * 1000 else timestampFin
                val (valorRestante, tipo) = parseDiasHorasRestantes(tiempoRestante(timestampFinMs))
                publicaciones_notificaciones_geinz(
                    id = informacion["id_promocion"] as? String ?: "",
                    img_principal = lista.firstOrNull() ?: "",
                    nombre = informacion["titulo"] as? String ?: "",
                    tipo = "promoción",
                    estado = tipo_hora_dias,
                    realizado = if (tipo_hora_dias == "horas") timestampAFechaSolo(timestamp_inicio) else dia_inicio,
                    vence = "${valorRestante} $tipo",
                    total_gastado = ""
                )
            }

            val listaNoti = notiSnapshot.documents.map { doc ->
                val params_noti = doc.get("params_noti") as? Map<String, Any> ?: emptyMap()
                val params_notificacion = doc.get("params_notificacion") as? Map<String, Any> ?: emptyMap()
                publicaciones_notificaciones_geinz(
                    id = params_notificacion["id_noti"] as? String ?: "",
                    img_principal = params_noti["img_notificacion"] as? String ?: "",
                    nombre = params_noti["titulo_notificacion"] as? String ?: "",
                    tipo = "notificación",
                    estado = "Enviado",
                    realizado = doc.getString("fecha_envio") ?: "",
                    vence = "",
                    total_gastado = ""
                )
            }

            // 🔹 Combinar y ordenar: activos primero, expirados al final
            val combinada = listaPromos + listaNoti
            val activos = combinada.filter { it.vence != "0 dias" }
            val expirados = combinada.filter { it.vence == "0 dias" }

            activos + expirados
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }



    suspend fun obtenerDatosPromocion(
        id_tienda: String,
        localidad: String,
        id_promo: String
    ): obtener_datos_promociones? {
        return try {
            val docSnap = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .collection("promociones_geinz")
                .document(id_promo)
                .get()
                .await()

            if (!docSnap.exists()) return null

            val data = docSnap.data ?: emptyMap<String, Any>()

            val tipo_hora_dias = data["tipo_hora_dias"] as? String ?: ""

            val img_container = data["img_container"] as? Map<String, Any> ?: emptyMap()
            val lista_img = img_container["lista_img"] as? List<String> ?: emptyList()

            val datos_hora_fecha = data["datos_hora_fecha"] as? Map<String, Any> ?: emptyMap()
            val diasMap = datos_hora_fecha["dias"] as? Map<String, Any> ?: emptyMap()
            val horasMap = datos_hora_fecha["horas"] as? Map<String, Any> ?: emptyMap()

            val informacion = data["informacion"] as? Map<String, Any> ?: emptyMap()
            val categoria   = informacion["categoria"] as? String ?: ""
            val compartir   = informacion["compartir"] as? Boolean ?: false
            val contactar   = informacion["contactar"] as? Boolean ?: false
            val idPromocion = informacion["id_promocion"] as? String ?: ""
            val titulo      = informacion["titulo"] as? String ?: ""
            val descripcion = informacion["descripcion"] as? String ?: ""
            val numero      = informacion["numero"] as? String ?: ""

            val fechaInicio: Long = if (tipo_hora_dias == "horas") {
                (horasMap["timestamp_inicio"] as? Number)?.toLong() ?: 0L
            } else {
                (diasMap["timestamp_inicio"] as? Number)?.toLong() ?: 0L
            }

            val fechaFin: Long = if (tipo_hora_dias == "horas") {
                (horasMap["timestamp_fin"] as? Number)?.toLong() ?: 0L
            } else {
                (diasMap["timestamp_fin"] as? Number)?.toLong() ?: 0L
            }

            obtener_datos_promociones(
                lista_img = lista_img,
                categoira = categoria,
                compartir = compartir,
                contactar = contactar,
                id_promocion = idPromocion,
                descripcion = descripcion,
                titulo = titulo,
                numero = numero,
                fecha_iniciada = fechaInicio,
                fecha_terminada = fechaFin
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    fun timestampAFechaSolo(timestampMillis: Long): String {
        val date = Date(timestampMillis)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // solo fecha
        format.timeZone = TimeZone.getDefault() // hora local
        return format.format(date)
    }

    fun parseDiasHorasRestantes(diasRestantesStr: String): Pair<Int, String> {
        // Ejemplos de strings que podrías tener: "3 días restantes" o "5 horas restantes"
        val regex = """(\d+)\s*(día|días|hora|horas)""".toRegex()
        val match = regex.find(diasRestantesStr)
        return if (match != null) {
            val valor = match.groupValues[1].toIntOrNull() ?: 0
            val tipo = if (match.groupValues[2].startsWith("día")) "dias" else "horas"
            valor to tipo
        } else {
            0 to "dias"
        }
    }




    fun tiempoRestante(timestampFin: Long): String {
        val ahoraMs = System.currentTimeMillis()
        val diffMs = timestampFin - ahoraMs

        if (diffMs <= 0) return "Expirado"

        val totalHoras = diffMs.toDouble() / (1000 * 60 * 60)
        val totalMinutos = diffMs.toDouble() / (1000 * 60)

        return if (totalHoras >= 24) {
            // Mostrar días completos restantes
            val dias = ceil(totalHoras / 24).toLong()  // +1 implícito para el día actual
            "$dias ${if (dias == 1L) "día" else "días"} restantes"
        } else {
            // Mostrar horas y minutos restantes
            val horas = floor(totalHoras).toLong()
            val minutos = floor(totalMinutos % 60).toLong()
            when {
                horas > 0 && minutos > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} y $minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                horas > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} restantes"
                minutos > 0 -> "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                else -> "Menos de un minuto restante"
            }
        }
    }



}
package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.CerrarAnuncioEstadisticas
import com.geinzz.geinzwork.data.model.ComodidadesAgregadas
import com.geinzz.geinzwork.data.model.EstadisticaAccion
import com.geinzz.geinzwork.data.model.EstadisticasEvento
import com.geinzz.geinzwork.data.model.EstadisticasPromoGenerales
import com.geinzz.geinzwork.data.model.EventoEstadisticas
import com.geinzz.geinzwork.data.model.EventosNotificacion
import com.geinzz.geinzwork.data.model.TiempoPorDia
import com.geinzz.geinzwork.data.model.TotalEstadistica
import com.geinzz.geinzwork.data.model.datos_de_notificacion
import com.geinzz.geinzwork.data.model.mensaje_predeterminado
import com.geinzz.geinzwork.data.model.metodos_pagos_agregados_publiaciones
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.geinzz.geinzwork.data.model.notificaciones
import com.geinzz.geinzwork.data.model.obtener_datos_promociones
import com.geinzz.geinzwork.data.model.parametros_notificacion
import com.geinzz.geinzwork.data.model.publicaciones_notificaciones_geinz
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.calcularCostoPromo
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.calcularTiempoPromo
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.formatoFechaHora
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.obtenerEstadoFinal
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.tiempoRestante
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.emptyMap

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

                val estado_publicacion = doc.get("estado") as? String ?: ""
                val hora_inicio = horasMap["hora_inicio"] as? String ?: ""
                val hora_fin = horasMap["hora_fin"] as? String ?: ""
                val dia_inicio = diasMap["fecha_inicio"] as? String ?: ""
                val dia_fin = diasMap["fecha_fin"] as? String ?: ""
                val timestamp_inicio = (horasMap["timestamp_inicio"] as? Timestamp)
                val id_promo = informacion["id_promocion"] as? String ?: ""
                val timestampFin = when (tipo_hora_dias) {
                    "horas" -> (horasMap["timestamp_fin"] as? Timestamp)
                    "dias" -> (diasMap["timestamp_fin"] as? Timestamp)
                    else -> null
                }


//                val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
                val tiempo = timestampFin?.let {
                    tiempoRestante(
                        it
                    )
                } ?: "Expirado"

                val fechaOrden = timestampFin?.toDate()?.time ?: 0L

//                val (valorRestante, tipo) = parseDiasHorasRestantes(tiempo)

                publicaciones_notificaciones_geinz(
                    id = id_promo,
                    img_principal = lista.firstOrNull() ?: "",
                    nombre = informacion["titulo"] as? String ?: "",
                    tipo = "promoción",
                    estado = tipo_hora_dias,
                    realizado = if (tipo_hora_dias == "horas")
                        timestamp_inicio?.let { timestampAFechaSolo(it) } ?: ""
                    else
                        dia_inicio,
                    vence = tiempo,
                    total_gastado = "",
                    estado_publicacion = estado_publicacion, fechaOrden
                )
            }

            val listaNoti = notiSnapshot.documents.map { doc ->
                val params_noti =
                    doc.get("datos_de_notificacion") as? Map<String, Any> ?: emptyMap()
                val params_notificacion =
                    doc.get("params_notificacion") as? Map<String, Any> ?: emptyMap()
                publicaciones_notificaciones_geinz(
                    id = params_notificacion["id_noti"] as? String ?: "",
                    img_principal = params_noti["img_notificacion"] as? String ?: "",
                    nombre = params_noti["titulo_notificacion"] as? String ?: "",
                    tipo = "notificación",
                    estado = "Enviado",
                    realizado = doc.getString("fecha_envio") ?: "",
                    vence = "",
                    total_gastado = "", "", 0L
                )
            }

            // 🔹 Combinar y ordenar: activos primero, expirados al final
            val combinada = listaPromos + listaNoti
            val activos = combinada
                .filter { it.vence != "Expirado" }
                .sortedByDescending { it.fechaOrden } // 🔥 HOY → ATRÁS

            val expirados = combinada
                .filter { it.vence == "Expirado" }
                .sortedByDescending { it.fechaOrden }

            activos + expirados
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    fun escucharEstadosTodasPromociones(
        idTienda: String,
        localidad: String,
        onCambio: (Map<String, String>) -> Unit
    ): ListenerRegistration {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(idTienda)
            .collection("promociones_geinz")

        return ref.addSnapshotListener { snapshots, error ->
            if (error != null || snapshots == null) {
                onCambio(emptyMap())
                return@addSnapshotListener
            }

            val estados = snapshots.documents.associate { doc ->
                doc.id to (doc.getString("estado") ?: "")
            }

            onCambio(estados)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
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

            val data = docSnap.data ?: return null
            val precio_publicacio = data["precio_publicacion"] as? String ?: ""
            val rango_establecido = data["rango_establecido"] as? String ?: ""
            val horario_publicacion = data["horario_publicacion"] as? String ?: ""
            val tipoHoraDias = data["tipo_hora_dias"] as? String ?: ""

            val estado = data["estado"] as? String ?: ""
            val enPausa = estado.equals("pausado", ignoreCase = true)

            val listaImg = (data["img_container"] as? Map<*, *>)
                ?.get("lista_img") as? List<String> ?: emptyList()

            val datosHoraFecha = data["datos_hora_fecha"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val diasMap = datosHoraFecha["dias"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val horasMap = datosHoraFecha["horas"] as? Map<*, *> ?: emptyMap<Any, Any>()

            val info = data["informacion"] as? Map<*, *> ?: emptyMap<Any, Any>()

            val categoria = info["categoria"] as? String ?: ""
            val compartir = info["compartir"] as? Boolean ?: false
            val contactar = info["contactar"] as? Boolean ?: false
            val idPromocion = info["id_promocion"] as? String ?: ""
            val titulo = info["titulo"] as? String ?: ""
            val descripcion = info["descripcion"] as? String ?: ""
            val numero = info["numero"] as? String ?: ""

            val metodos_pagos =data["pagos"]as? Map<*, *> ?: emptyMap<Any, Any>()

            val comodidades =data["comodidades"]as? Map<*, *> ?: emptyMap<Any, Any>()

            val fechaInicioTs = ((if (tipoHoraDias == "horas") horasMap else diasMap)
                ["timestamp_inicio"] as? Timestamp) ?: Timestamp.now()

            val fechaFinTs = ((if (tipoHoraDias == "horas") horasMap else diasMap)
                ["timestamp_fin"] as? Timestamp) ?: Timestamp.now()

            // 🔥 TRANSFORMACIÓN AQUÍ
            val fechaInicio = fechaInicioTs.formatoFechaHora()
            val fechaFin = fechaFinTs.formatoFechaHora()

            // ---------------- MENSAJES PREDETERMINADOS ----------------
            val mensajeMap =
                data["mensaje_predeterminado"] as? Map<*, *> ?: emptyMap<Any, Any>()

            val compartirMap =
                mensajeMap["compartir"] as? Map<*, *> ?: emptyMap<Any, Any>()

            val whatsappMap =
                mensajeMap["whatsapp"] as? Map<*, *> ?: emptyMap<Any, Any>()


            val mensajesPredeterminados = msjes_predeteminados_generales(
                compartir = mensaje_predeterminado(
                    msje_predermindo = compartirMap["msje_predermindo"] as? String ?: "",
                    activo_o_no = compartirMap["activo_o_no"] as? Boolean ?: false
                ),
                whatsapp = mensaje_predeterminado(
                    msje_predermindo = whatsappMap["msje_predermindo"] as? String ?: "",
                    activo_o_no = whatsappMap["activo_o_no"] as? Boolean ?: false
                )
            )


            val obtener_metodos_pagos=metodos_pagos_agregados_publiaciones(
                yape = metodos_pagos["yape"] as? Boolean?:false,
                plin = metodos_pagos["plin"] as? Boolean?:false,
                agora = metodos_pagos["yape"] as? Boolean?:false,
                efectivo = metodos_pagos["efectivo"] as? Boolean?:false,
                visa = metodos_pagos["visa"] as? Boolean?:false,
                mastercard = metodos_pagos["mastercard"] as? Boolean?:false
            )

            val obtener_comodidades=ComodidadesAgregadas(
                zonaExpandida =  comodidades["zona_expandida"] as? Boolean?:false,
                wifi =  comodidades["true"] as? Boolean?:false,
                serviciosHigienicos =  comodidades["servicios_higienicos"] as? Boolean?:false,
                camarasSeguridad =  comodidades["camaras_seguridad"] as? Boolean?:false,
                salaEspera =  comodidades["sala_espera"] as? Boolean?:false,
                salaJuegos =  comodidades["sala_juegos"] as? Boolean?:false,
                mesaParaNinos =  comodidades["mesa_para_ninos"] as? Boolean?:false,
                ingresoConMascotas =  comodidades["ingreso_con_mascotas"] as? Boolean?:false,
                estacionamiento =  comodidades["estacionamiento"] as? Boolean?:false,
                enchufe =  comodidades["enchufe"] as? Boolean?:false,
                aireAcondicionado =  comodidades["aire_acondicionado"] as? Boolean?:false,
            )

            val tiempoPromo = calcularTiempoPromo(
                inicio = fechaInicioTs,
                fin = fechaFinTs,
                tipo = tipoHoraDias
            )
            val costoPromo = calcularCostoPromo(
                inicio = fechaInicioTs,
                fin = fechaFinTs,
                tipo = tipoHoraDias
            )
            val estadisticas =
                obtener_estadisticas_promocion(estado, id_tienda, id_promo, localidad)

            // ---------------- RETURN FINAL ----------------
            obtener_datos_promociones(
                estado = obtenerEstadoFinal(fechaFinTs, enPausa),
                horas_o_fecha = tipoHoraDias,
                lista_img = listaImg,
                categoira = categoria,
                compartir = compartir,
                contactar = contactar,
                id_promocion = idPromocion,
                descripcion = descripcion,
                titulo = titulo,
                numero = numero,
                fecha_iniciada = fechaInicio,
                fecha_terminada = fechaFin,
                duracion_total = tiempoPromo.duracion,
                tiempo_transcurrido = tiempoPromo.transcurrido,
                costo_total = costoPromo.total,
                costo_consumido = costoPromo.consumido,
                estadisticas = estadisticas,
                mensaje_predeterminado = mensajesPredeterminados,
                rango_establecido,
                precio_publicacio,
                horario_publicacion,obtener_metodos_pagos,obtener_comodidades
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun obtener_estadisticas_promocion(
        tipo: String,
        id_tienda: String,
        id_promo: String,
        localidad: String
    ): EstadisticasPromoGenerales {

        val estadisticasRef = when (tipo) {

            "activo", "pausado" -> db.collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .document(id_promo)
                .collection("estadisticas")

            "expirada" -> db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .collection("promociones_geinz")
                .document(id_promo)
                .collection("estadisticas")

            else -> return EstadisticasPromoGenerales()
        }

        val acciones = listOf("click", "vistas", "compartidos", "whatsapp")

        val resultados = acciones.associateWith { accion ->
            val snap = estadisticasRef.document(accion).get().await()
            if (snap.exists()) obtenerAccion(snap.reference, accion) else null
        }

        return EstadisticasPromoGenerales(
            click = resultados["click"],
            vistas = resultados["vistas"],
            compartidos = resultados["compartidos"],
            whatsapp = resultados["whatsapp"]
        )
    }


    private suspend fun leerEventoNormal(doc: DocumentSnapshot): EventoEstadisticas {

        suspend fun leer(nombre: String): Map<String, Long> =
            doc.reference.collection(nombre).get().await()
                .associate { it.id to (it.getLong("total") ?: 0L) }

        return EventoEstadisticas(
            total = doc.getLong("total") ?: 0L,
            porDia = leer("por_dia"),
            edad = leer("edad"),
            genero = leer("genero"),
            localidad = leer("localidad")
        )
    }


    suspend fun leerCerrarAnuncio(docRef: DocumentReference): CerrarAnuncioEstadisticas {
        Log.d("NotiDebug", "====== INICIO leerCerrarAnuncio ======")
        Log.d("NotiDebug", "Doc path (usuarios): ${docRef.path}/usuarios")

        // Obtenemos todos los usuarios (documentos dentro de la colección "usuarios")
        val usuariosSnap = docRef.collection("usuarios").get().await()
        Log.d("NotiDebug", "Usuarios encontrados: ${usuariosSnap.size()}")

        val porDia = mutableMapOf<String, TiempoPorDia>()
        var totalEventos = 0
        var totalSegundos = 0L

        // Iteramos cada usuario
        for (usuarioDoc in usuariosSnap.documents) {
            val uid = usuarioDoc.id
            Log.d("NotiDebug", "→ Usuario UID: $uid")
            Log.d("NotiDebug", "Usuario path: ${usuarioDoc.reference.path}")

            // Aquí ignoramos si el documento padre "existe" o no, y vamos directo a la colección "por_dia"
            val porDiaSnap = usuarioDoc.reference.collection("por_dia").get().await()
            Log.d("NotiDebug", "   por_dia docs: ${porDiaSnap.size()}")

            // Iteramos cada día
            for (diaDoc in porDiaSnap.documents) {
                val fecha = diaDoc.id
                val segundos = diaDoc.getLong("totalSegundos") ?: 0L

                Log.d("NotiDebug", "      Fecha: $fecha | totalSegundos: $segundos")

                val actual = porDia[fecha]

                porDia[fecha] = if (actual == null) {
                    TiempoPorDia(
                        total_segundos = segundos,
                        eventos = 1
                    )
                } else {
                    actual.copy(
                        total_segundos = actual.total_segundos + segundos,
                        eventos = actual.eventos + 1
                    )
                }

                totalEventos++
                totalSegundos += segundos
            }
        }

        val promedio = if (totalEventos > 0)
            totalSegundos.toDouble() / totalEventos
        else 0.0

        Log.d("NotiDebug", "====== RESULTADO FINAL ======")
        Log.d("NotiDebug", "Total eventos: $totalEventos")
        Log.d("NotiDebug", "Total segundos: $totalSegundos")
        Log.d("NotiDebug", "Promedio: $promedio")
        Log.d("NotiDebug", "Por día: $porDia")

        return CerrarAnuncioEstadisticas(
            total = totalEventos,
            porDia = porDia,
            promedio_segundos = promedio
        )
    }


    suspend fun obtenerNotificacionCompleta(
        localidadTienda: String,
        idTienda: String,
        idPromo: String
    ): notificaciones? {
        val db = FirebaseFirestore.getInstance()

        try {
            val promoRef = db.collection("Tiendas")
                .document(localidadTienda)
                .collection(localidadTienda)
                .document(idTienda)
                .collection("notificaciones_enviadas")
                .document(idPromo)

            // 🔹 Obtener datos de la notificación
            val promoSnap = promoRef.get().await()
            if (!promoSnap.exists()) {
                Log.d(
                    "NotiDebug",
                    "No existe la notificación: $idPromo en tienda: $idTienda, localidad: $localidadTienda"
                )
                return null
            }

            val datosMap = promoSnap.data ?: mapOf()
            val datos_notifion =
                datosMap["datos_de_notificacion"] as? Map<String, Any> ?: emptyMap()
            Log.d("NotiDebug", "Datos brutos de la notificación: $datosMap")

            val datosNoti = datos_de_notificacion(
                categoira = datos_notifion["categoria_tienda"] as? String ?: "",
                img_id_storage = datos_notifion["id_img_storage"] as? String ?: "",
                id_tienda = datos_notifion["id_tienda"] as? String ?: "",
                img_notifiacion = datos_notifion["img_notificacion"] as? String ?: "",
                localidad = datos_notifion["localidad"] as? String ?: "",
                logo_notificacion = datos_notifion["logo_notificacion"] as? String ?: "",
                numero_contacto = datos_notifion["numero_contacto"] as? String ?: "",
                texto_notificacion = datos_notifion["texto_notificacion"] as? String ?: "",
                titulo_notificacion = datos_notifion["titulo_notificacion"] as? String ?: "",

                )
            Log.d("NotiDebug", "Datos de notificación parseados: $datosNoti")

            val paramsMap = datosMap["params_notificacion"] as? Map<String, Any> ?: emptyMap()
            val res_noti = datosMap["resultado_notificacion"] as? Map<String, Any> ?: emptyMap()
            Log.d("NotiDebug", "Parametros de notificación crudos: $paramsMap")

            val parametrosNoti = parametros_notificacion(
                id_noti = paramsMap["id_noti"] as? String ?: "",
                id_promo_anuncio = paramsMap["id_publicacion_anuncio"] as? String ?: "",
                notificacion_nuevo = paramsMap["notificacion_nueva"] as? Boolean ?: false,
                prioridad_notificacion = paramsMap["priorida_notificacion"] as? String ?: "",
                tipo_notificacion = paramsMap["tipo_notificacion"] as? String ?: "",
                tipo_precio = paramsMap["tipo_clikeable"] as? String ?: "",
                total_gastado = (paramsMap["total_gastado"] as? Number)?.toString() ?: "0",
                enviados = (res_noti["enviados"] as? Number)?.toString() ?: "0",
                fallidos = (res_noti["fallido"] as? Number)?.toString() ?: "0",
                mensaje_predeterminado = paramsMap["msje_predeterminado"]?.toString() ?: ""
            )
            Log.d("NotiDebug", "Parametros de notificación parseados: $parametrosNoti")

            // 🔹 Obtener estadísticas de todos los eventos
            val eventosSnap = promoRef.collection("eventos").get().await()
            Log.d(
                "NotiDebug",
                "Documentos de eventos obtenidos: ${eventosSnap.documents.map { it.id }}"
            )

            var cerrarAnuncio = CerrarAnuncioEstadisticas()
            var click = EventoEstadisticas()
            var clickAnuncio = EventoEstadisticas()
            var clickPerfil = EventoEstadisticas()
            var clickWhatsapp = EventoEstadisticas()
            var vista = EventoEstadisticas()

            for (doc in eventosSnap.documents) {
                when (doc.id.lowercase()) {
                    "cerrar_anuncio" -> cerrarAnuncio = leerCerrarAnuncio(doc.reference)
                    "click" -> click = leerEventoNormal(doc)
                    "click_anuncio" -> clickAnuncio = leerEventoNormal(doc)
                    "click_perfil" -> clickPerfil = leerEventoNormal(doc)
                    "click_whatsapp" -> clickWhatsapp = leerEventoNormal(doc)
                    "vista", "vistas" -> vista = leerEventoNormal(doc)
                }
            }


            val eventosNotificacion = EventosNotificacion(
                cerrar_anuncio = cerrarAnuncio,
                click = click,
                click_anuncio = clickAnuncio,
                click_perfil = clickPerfil,
                click_whatsapp = clickWhatsapp,
                vista = vista
            )

            Log.d("NotiDebug", "Evento final de estadísticas: $eventosNotificacion")

            // 🔹 Fecha de envío
            val fechaEnvio = datosMap["fecha_envio"] as? String ?: ""
            Log.d("NotiDebug", "Fecha de envío: $fechaEnvio")

            return notificaciones(
                fecha_enviada = fechaEnvio,
                datos_de_notificacion = datosNoti,
                parametros_notificacion = parametrosNoti,
                EventoEstadisticas = eventosNotificacion
            )

        } catch (e: Exception) {
            Log.e("NotiDebug", "Error al obtener notificación completa", e)
            return null
        }
    }


    private suspend fun obtenerAccion(
        accionRef: DocumentReference,
        tipo: String
    ): EstadisticaAccion {

        val accionSnap = accionRef.get().await()
        val total = accionSnap.getLong("total")?.toInt() ?: 0

        suspend fun obtenerMapa(sub: String): Map<String, TotalEstadistica> {
            return accionRef.collection(sub)
                .get()
                .await()
                .documents
                .associate { doc ->
                    doc.id to TotalEstadistica(
                        total = doc.getLong("total")?.toInt() ?: 0
                    )
                }
        }

        return EstadisticaAccion(
            tipo = tipo, // 👈 AQUÍ
            total = total,
            edad = obtenerMapa("edad"),
            genero = obtenerMapa("genero"),
            localidad = obtenerMapa("localidad"),
            por_dia = obtenerMapa("por_dia")
        )
    }


    fun timestampAFechaSolo(timestamp: Timestamp): String {
        val date = Date(timestamp.seconds * 1000)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
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


    fun cambiar_estado_publicacion(
        id_tienda: String,
        localidad: String,
        id_promo: String,
        estado_cambiado: String
    ) {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection("promos_ofertas")
            .document(id_promo)

        val ref2 = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("promociones_geinz")
            .document(id_promo)

        val hashMap = hashMapOf<String, Any>(
            "estado" to estado_cambiado // ✅ usamos la variable, no la cadena literal
        )

        // Actualizamos ambas colecciones
        ref.update(hashMap)
            .addOnSuccessListener { Log.d("Firestore", "Estado actualizado en promos_ofertas") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error: ${e.message}") }

        ref2.update(hashMap)
            .addOnSuccessListener { Log.d("Firestore", "Estado actualizado en promociones_geinz") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error: ${e.message}") }
    }


}
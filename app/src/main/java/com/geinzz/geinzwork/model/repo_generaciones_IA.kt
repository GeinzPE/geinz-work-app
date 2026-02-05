package com.geinzz.geinzwork.model

import android.annotation.SuppressLint
import android.util.Log
import com.geinzz.geinzwork.data.model.NotificacionIA
import com.geinzz.geinzwork.data.model.NotificacionIA_dialog
import com.geinzz.geinzwork.data.model.datos_gen_IA_Tiendas
import com.geinzz.geinzwork.data.model.datos_generaciones_IA
import com.geinzz.geinzwork.data.model.dialog_generaciones_IA_promo_noti
import com.geinzz.geinzwork.data.model.lista_genereracione
import com.geinzz.geinzwork.data.model.nuevas_generaciones_con_IA
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.timestampToFechaHora
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromoAtencion_solo_una_generacion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromoInformativo_solo_una_generacion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromoVenta_solo_una_generacion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionAtencion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionCita
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionNovedad
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionOperativa
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionReposicion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionServicios
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionUrgencia
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionVenta
import com.geinzz.geinzwork.model.repo_pantallas_promocionar.TipoGeneracionIA
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.acortarDescripcionNotificacion
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class repo_generaciones_IA {

    private val db = FirebaseFirestore.getInstance()


    @SuppressLint("SuspiciousIndentation")
    fun obtener_generaciones_IA_realtime(
        id_tienda: String,
        localidad: String
    ): Flow<List<datos_gen_IA_Tiendas>> = callbackFlow {

        val listener = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("gen_con_IA_historial")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val listaFinal = mutableListOf<datos_gen_IA_Tiendas>()

                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue

                    val inicio = data["fecha"] as? Timestamp ?: continue
                    val fin = data["caudidad"] as? Timestamp ?: continue

                    // ⏱️ validar expiración
                    val tiempo =
                        constantes_datos_expirados_fechas_publicaciones.tiempoRestante(fin)

                    if (tiempo == "Expirado") continue

                    val genIA = data["generacions_con_IA"] as? Map<*, *> ?: emptyMap<Any, Any>()

                    val seleccion =
                        genIA["generacion_selecionada"] as? Map<*, *> ?: emptyMap<Any, Any>()

                    val listaGeneracionesRaw =
                        genIA["lista_generaciones"] as? List<Map<String, Any>> ?: emptyList()

                    val listaGeneraciones = listaGeneracionesRaw.map {
                        lista_genereracione(
                            tipo = it["tipoIA"] as? String ?: "",
                            titulo = it["titulo"] as? String ?: "",
                            descripcion = it["descripcion"] as? String ?: ""
                        )
                    }

                    val nuevas_generaciones =
                        genIA["nuevas_generaciones"] as? Map<*, *> ?: emptyMap<Any, Any>()
                    val titulo_nueva_generacion = nuevas_generaciones["titulo"] as? String ?: ""
                    val descripcion = nuevas_generaciones["descripcion"] as? String ?: ""
                    val titulo_anterior = nuevas_generaciones["titulo_anterior"] as? String ?: ""
                    val descripcion_anterior = nuevas_generaciones["descripcion_anterior"] as? String ?: ""
                    val fecha_nueva_generacion =
                        nuevas_generaciones["fecha_nueva_generacion"] as? Timestamp ?: Timestamp.now()

                  val fecha_hora_realizado=  timestampToFechaHora(fecha_nueva_generacion)
                    listaFinal.add(
                        datos_gen_IA_Tiendas(
                            inicio = inicio,
                            fin = fin,
                            id_promo_noti_cread = data["id_promo_o_noti"] as? String ?: "",
                            img_container = data["img_container"] as? String ?: "",
                            nombre_generacion = data["nombre_generacion"] as? String ?: "",
                            tipo_realizado = data["tipo"] as? String ?: "",
                            datos_generaciones = datos_generaciones_IA(
                                titulo_original = genIA["titulo_original"] as? String ?: "",
                                descripcion_original = genIA["descripcion_original"] as? String
                                    ?: "",
                                tipo_generacion_IA = data["tipo"] as? String ?: "",
                                generacion_wsap = genIA["generacion_wsap"] as? String ?: "",
                                generacion_compartir = genIA["generacion_compartir"] as? String
                                    ?: "",
                                generaciones = listaGeneraciones,
                                titulo_seleccionado_gen_IA =
                                    seleccion["titulo"] as? String ?: "",
                                descripcion_seleccionada_ge_IA =
                                    seleccion["descripcion"] as? String ?: ""
                            ), nuevas_generaciones = nuevas_generaciones_con_IA(
                                titulo_nuevo = titulo_nueva_generacion,
                                descripcion_nueva = descripcion, fecha_hora_realizado,titulo_anterior,descripcion_anterior
                            )
                        )
                    )
                }

                trySend(listaFinal.sortedByDescending { it.fin.seconds })
            }

        awaitClose { listener.remove() }
    }


    fun generarPromptSegunTipoUnaGeneracion(
        tipo: TipoGeneracionIA,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return when (tipo) {

            TipoGeneracionIA.VENTA ->
                generarPromptPromoVenta_solo_una_generacion(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            TipoGeneracionIA.ATENCION ->
                generarPromptPromoAtencion_solo_una_generacion(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            TipoGeneracionIA.INFORMATIVO,
            TipoGeneracionIA.URGENCIA,
            TipoGeneracionIA.NOVEDAD,
            TipoGeneracionIA.OPERATIVA,
            TipoGeneracionIA.REPOSICION,
            TipoGeneracionIA.CITAS,
            TipoGeneracionIA.SERVICIOS ->
                generarPromptPromoInformativo_solo_una_generacion(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )
        }
    }


    suspend fun generar_promocion_con_IA(
        id_promo_noti_gen: String,
        tipo_generacion: TipoGeneracionIA,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
    ): dialog_generaciones_IA_promo_noti? {

        return try {
            val model = Firebase.ai(
                backend = GenerativeBackend.googleAI()
            ).generativeModel("gemini-2.5-flash")

            val prompt = generarPromptSegunTipoUnaGeneracion(
                tipo = tipo_generacion,
                tituloUsuario = tituloUsuario,
                descripcionUsuario = descripcionUsuario,
                nombreTienda = nombreTienda,
                localidad = localidad
            )

            val result = model.generateContent(prompt)
            val texto = result.text ?: return null

            parsearRespuestaIA(id_promo_noti_gen, texto)

        } catch (e: Exception) {
            Log.e("IA", "Error IA promociones: ${e.message}")
            null
        }
    }


    fun parsearRespuestaIA(
        id_promo_noti_gen: String,
        texto: String
    ): dialog_generaciones_IA_promo_noti? {

        val titulo = Regex("T:\\s*(.*)")
            .find(texto)
            ?.groupValues
            ?.get(1)
            ?.trim()

        val descripcion = Regex("D:\\s*([\\s\\S]*)")
            .find(texto)
            ?.groupValues
            ?.get(1)
            ?.trim()

        if (titulo.isNullOrBlank() || descripcion.isNullOrBlank()) {
            return null
        }

        return dialog_generaciones_IA_promo_noti(
            id_promo_noti_gen,
            titulo = titulo,
            descripcion = descripcion
        )
    }


    suspend fun agregar_nuevas_generaciones(
        titulo_anterior:String,descripcion_anterior:String,
        id_tienda: String,
        localidad: String,
        titulo_nuevo: String,
        texto_nuevo: String,
        id_generacion: String
    ) {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("gen_con_IA_historial")
            .document(id_generacion)


        val nuevasGeneraciones = hashMapOf(
            "titulo" to titulo_nuevo,
            "descripcion" to texto_nuevo,
            "fecha_nueva_generacion" to Timestamp.now(),
            "titulo_anterior" to titulo_anterior,
            "descripcion_anterior" to descripcion_anterior
        )

        ref.update(
            mapOf(
                "generacions_con_IA.nuevas_generaciones" to nuevasGeneraciones,
            )
        ).await()
    }



    suspend fun crear_notificacion_conIA_corta(
        id_notificacion_promo: String,
        tituloPublicacion: String,
        descCorta: String,
        tipoGeneracion: TipoGeneracionIA
    ): NotificacionIA_dialog {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val descripcion_acortada = acortarDescripcionNotificacion(descCorta)

        try {
            val prompt = when (tipoGeneracion) {
                TipoGeneracionIA.VENTA -> promptNotificacionVenta(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.ATENCION -> promptNotificacionAtencion(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.URGENCIA -> promptNotificacionUrgencia(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.NOVEDAD -> promptNotificacionNovedad(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.INFORMATIVO -> promptNotificacionAtencion(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.OPERATIVA -> promptNotificacionOperativa(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.REPOSICION -> promptNotificacionReposicion(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.CITAS -> promptNotificacionCita(
                    tituloPublicacion,
                    descripcion_acortada
                )
                TipoGeneracionIA.SERVICIOS -> promptNotificacionServicios(
                    tituloPublicacion,
                    descripcion_acortada
                )
            }

            val inicio = System.currentTimeMillis()
            val result = model.generateContent(prompt)
            val textoGenerado = result.text ?: ""
            val fin = System.currentTimeMillis()

            Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
            Log.d("Gemini", "Resultado:\n$textoGenerado")

            val notificacion = parsearRespuestaGemini(
                textoGenerado,
                tipoGeneracion
            )

            return NotificacionIA_dialog(
                id_promo_noti_gen = id_notificacion_promo,
                titulo = notificacion.titulo,
                descripcion = notificacion.descripcion
            )

        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
            throw e // 🔥 importante para que el ViewModel lo capture
        }
    }


    fun parsearRespuestaGemini(texto: String, tipoGeneracion: TipoGeneracionIA): NotificacionIA {
        var titulo = ""
        var descripcion = ""

        texto.lines().forEach { linea ->
            when {
                linea.startsWith("T:") -> titulo = linea.removePrefix("T:").trim()

                linea.startsWith("D:") -> descripcion = linea.removePrefix("D:").trim()
            }
        }

        return NotificacionIA(tipoGeneracion,
            titulo = titulo, descripcion = descripcion
        )
    }


}
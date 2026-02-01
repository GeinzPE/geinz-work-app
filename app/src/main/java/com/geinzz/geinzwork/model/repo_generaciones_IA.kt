package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.datos_gen_IA_Tiendas
import com.geinzz.geinzwork.data.model.datos_generaciones_IA
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.historial_recargas
import com.geinzz.geinzwork.data.model.lista_genereracione
import com.geinzz.geinzwork.data.model.nuevas_generaciones_con_IA
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas.generarIdRecarga
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class repo_generaciones_IA {

    private val db = FirebaseFirestore.getInstance()


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
                            )
                        )
                    )
                }

                // 📅 ordenar por fecha
                trySend(listaFinal.sortedByDescending { it.fin.seconds })
            }

        awaitClose { listener.remove() }
    }


}
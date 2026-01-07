package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.historial_financiero
import com.geinzz.geinzwork.data.model.historial_recargas
import com.geinzz.geinzwork.data.model.recargar_monedas_tienda
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class repo_recargas {

    val db = FirebaseFirestore.getInstance()


    suspend fun guardarHistorial(i: historial_recargas): Boolean {
        return try {
            val ref = db.collection("Tiendas")
                .document(i.localidad_tienda)
                .collection(i.localidad_tienda)
                .document(i.id_tienda)
                .collection("historial_financiero")
                .document(i.id_recarga)

            val hashMap = mapOf(
                "id_transaccion" to i.id_recarga,
                "tipo_transacción" to i.tipo_transaccion,
                "hora_fecha" to mapOf(
                    "fecha" to i.fecha,
                    "hora" to i.hora
                ),
                "metodo_pago" to mapOf(
                    "yape" to i.yape,
                    "plin" to i.plin
                ),
                "datos_tienda" to mapOf(
                    "nombre_tienda" to i.nombre_tienda,
                    "id_tienda" to i.id_tienda,
                    "localidad_tienda" to i.localidad_tienda
                ),
                "datos_recarga" to mapOf(
                    "tipo_paquete" to i.tipo,
                    "monto_aumentado" to i.monto.toInt(),
                    "precio_soles" to i.precio_soles,
                    "estado" to i.estado,
                    "monto_anterior" to i.monto_posterior
                ), "timestamp" to FieldValue.serverTimestamp()

            )

            ref.set(hashMap, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    fun obtner_saldo_reactivo(
        id_tienda: String,
        localidad: String
    ): Flow<Int> = callbackFlow {

        val listener = db
            .collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val puntos = snapshot.get("puntos_tienda") as? Number
                    trySend(puntos?.toInt() ?: 0)
                } else {
                    trySend(0)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun guardar_historial_descuento(i: historial_descuento): Boolean {
        return try {
            val ref = db.collection("Tiendas")
                .document(i.localidad_tienda)
                .collection(i.localidad_tienda)
                .document(i.id_tienda)
                .collection("historial_financiero")
                .document(i.id_recarga)

            val hashMap = mapOf(
                "id_transaccion" to i.id_recarga,
                "tipo_transacción" to i.tipo_transaccion,
                "hora_fecha" to mapOf(
                    "fecha" to i.fecha,
                    "hora" to i.hora
                ),
                "datos_tienda" to mapOf(
                    "nombre_tienda" to i.nombre_tienda,
                    "id_tienda" to i.id_tienda,
                    "localidad_tienda" to i.localidad_tienda
                ),
                "datos_recarga" to mapOf(
                    "tipo_paquete" to i.tipo,
                    "monto_descontado" to i.monto_descuento.toInt(),
                    "precio_soles" to i.precio_soles,
                    "estado" to i.estado,
                    "monto_restante" to i.monto_restante
                ),
                "timestamp" to FieldValue.serverTimestamp()
            )

            ref.set(hashMap, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun recargar_monedas(i: recargar_monedas_tienda): Boolean {
        return try {
            val ref = db.collection("Tiendas")
                .document(i.localidad_tienda)
                .collection(i.localidad_tienda)
                .document(i.id_tienda)


            val actualizarPuntos = mapOf(
                "puntos_tienda" to FieldValue.increment(i.cantidad.toLong())
            )

            ref.update(actualizarPuntos).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun descontar_puntos_uso(
        monto_descontar: String,
        id_tienda: String,
        localidad: String
    ): Boolean {
        return try {
            val ref = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)


            val actualizarPuntos = mapOf(
                "puntos_tienda" to FieldValue.increment(-monto_descontar.toLong())
            )
            ref.update(actualizarPuntos).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun obtner_historial(
        id_tienda: String,
        localidad: String
    ): List<historial_financiero> {

        return try {
            val snapshot = db
                .collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .collection("historial_financiero")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->

                val data = doc.data ?: return@mapNotNull null

                val datosRecarga = data["datos_recarga"] as? Map<*, *>
                val datosTienda = data["datos_tienda"] as? Map<*, *>
                val horaFecha = data["hora_fecha"] as? Map<*, *>
                val id_trascacion =data["id_transaccion"] as? String?:""
                val tipo_transacion=data["tipo_transacción"] as? String ?: ""

                historial_financiero(
                    id_transaccion =id_trascacion,
                    monedas = datosRecarga?.get(if(tipo_transacion.equals("descuento"))"monto_descontado" else "monto_aumentado") as? Number?:0,
                    hora = horaFecha?.get("hora") as? String ?: "",
                    fecha = horaFecha?.get("fecha") as? String ?: "",
                    nombre_tienda = datosTienda?.get("nombre_tienda") as? String ?: "",
                    precio_soles = datosRecarga?.get("precio_soles") as? String ?: "",
                    tipo_realziado = tipo_transacion,
                    tipo_transaccion = datosRecarga?.get("tipo_paquete") as? String ?: "",
                    estodo = datosRecarga?.get("estado") as? String?:"",
                    monto_restante=datosRecarga?.get(if(tipo_transacion.equals("descuento"))"monto_restante" else "monto_anterior") as? Number?:0,
                )
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

}
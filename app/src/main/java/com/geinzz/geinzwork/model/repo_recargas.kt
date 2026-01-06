package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.historial_recargas
import com.geinzz.geinzwork.data.model.recargar_monedas_tienda
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
                    "monto_aumentado" to i.monto,
                    "precio_soles" to i.precio_soles
                ), "timestamp" to FieldValue.serverTimestamp()
            )

            ref.set(hashMap, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun guardar_historial_descuento(i: historial_descuento): Boolean{
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
                    "monto_descontado" to i.monto_descuento,
                    "precio_soles" to i.precio_soles
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

    suspend fun descontar_puntos_uso(monto_descontar:String,id_tienda:String,localidad:String): Boolean{
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
}
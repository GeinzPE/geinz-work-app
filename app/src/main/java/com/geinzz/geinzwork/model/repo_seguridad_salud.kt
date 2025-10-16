package com.geinzz.geinzwork.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_seguridad_salud {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_servicios_salud(localdad: String): List<dataclass_seguridad> {
        val lista = mutableListOf<dataclass_seguridad>()
        try {
            Log.d("DEBUG_SERVICIOS", "Consultando datos de: $localdad")

            val ref = db.collection("Tiendas")
                .document("salud_seguridad")
                .collection(localdad)
                .get()
                .await()

            Log.d("DEBUG_SERVICIOS", "Documentos obtenidos: ${ref.size()}")

            for (datos in ref) {
                val data = datos.data
                Log.d("DEBUG_SERVICIOS", "Documento ID: ${datos.id} → Data: $data")

                val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
                val numero_contacto = data?.get("numeros_contactos") as? Map<String, Any> ?: emptyMap()

                val latitud = (ubicacion["latitud"] as? Number ?: 0).toDouble()
                val referencia = (ubicacion["referencia"] as? String ?: "")
                val longitud = (ubicacion["longitud"] as? Number ?: 0).toDouble()
                val categoria = data?.get("categoria") as? String ?: ""

                val llamada = numero_contacto["llamada"] as? List<String> ?: emptyList()
                val whatsapp = numero_contacto["whatsapp"] as? List<String> ?: emptyList()

                val servicios = dataclass_seguridad(
                    nombre_ = data?.get("nombre") as? String ?: "",
                    direccion = ubicacion["direccion"] as? String ?: "",
                    numero_llamada = llamada,
                    numero_whatsapp = whatsapp,
                    img_ref = data?.get("img") as? String ?: "",
                    latidud = latitud,
                    longitud = longitud,
                    referencia=referencia,
                    categoria = categoria
                )

                Log.d("DEBUG_SERVICIOS", "Objeto creado: $servicios")

                lista.add(servicios)
            }

            Log.d("DEBUG_SERVICIOS", "Lista final: $lista")
        } catch (e: Exception) {
            Log.e("DEBUG_SERVICIOS", "Error al obtener servicios de $localdad", e)
        }

        return lista
    }


    fun atencion_24h(i:String): String {

            return when (i) {
                "Divpol Barranca" -> "Atencion 24h (física)"
                "Comisaría PNP Barranca" -> "Atencion 24h (física)"
                "Diprincri Barranca" -> "Atencion 24h (física)"
                "Bomberos Voluntarios Barranca" -> "Atencion 24h (física)"
                "SAMU Barranca" -> "Servicio 24h "
                "Hospital de Barranca"->"Atencion 24h (física)"
                "Serenazgo Municipal Barranca" -> "Operativo 24h (patrullaje)"
                else -> "Atención (08:00 - 14:00)"
            }

    }

}
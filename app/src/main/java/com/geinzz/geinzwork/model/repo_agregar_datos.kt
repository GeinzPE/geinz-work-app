package com.geinzz.geinzwork.model

import android.content.Context
import android.util.Log
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.data.model.direccion_lugar
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.google.firebase.firestore.FirebaseFirestore

class repo_agregar_datos(context: Context) {
    private val db: FirebaseFirestore

    init {
        // Inicializa Firebase secundario una sola vez
        FirebaseSecundario.inicializar(context)
        db = FirebaseSecundario.getFirestore()
    }

    fun agregar_datos(dataclass_repo_agregar_datos: dataclass_repo_agregar_datos) {
        val hashMap = hashMapOf<String, Any>(
            "numero" to dataclass_repo_agregar_datos.numero_telefono,
            "lugar_nobre" to dataclass_repo_agregar_datos.nombre_lugar,
            "lat" to dataclass_repo_agregar_datos.lat,
            "log" to dataclass_repo_agregar_datos.long
        )
        db.collection("datos_lugares").add(hashMap).addOnSuccessListener { documentReference ->
            val idGenerado = documentReference.id
            documentReference.update("id", idGenerado)
                .addOnSuccessListener {
                    Log.d("FirebaseRepo", "Documento agregado y ID actualizado correctamente.")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseRepo", "Error al actualizar el ID: ${e.message}")
                }
        }
    }

    fun pasar_datos() {
        var datos_compeltos = mutableListOf<dataclass_lugares_db>()

        db.collection("datos_lugares").get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val categoira = data?.get("categoria") as? List<String> ?: emptyList()
                val id = data?.get("id") as? String ?: ""
                val img = data?.get("img") as? String ?: ""
                val nombre_lugar = data?.get("lugar_nobre") as? String ?: ""
                val lat = data?.get("lat") as? Number ?: 0
                val log = data?.get("log") as? Number ?: 0
                val lugar = dataclass_lugares_db(
                    categoria = categoira,
                    direccion = direccion_lugar(lat = lat.toDouble(), log = log.toDouble()),
                    horario_atencion = emptyMap(),
                    id = id,
                    lugar_nombre = nombre_lugar,
                    logo_img = img, pagado = data["pagado"] as? Boolean ?:false
                )
                datos_compeltos.add(lugar)
            }
            for (dataclass_lugares_db in datos_compeltos) {

                val hashMap_horario = hashMapOf(
                    "lunes" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
                    "martes" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
                    "miercoles" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
                    "jueves" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
                    "viernes" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
                    "sabado" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
                    "domingo" to hashMapOf("h_abierto" to "", "h_cerrado" to "")
                )
                val direccion = hashMapOf(
                    "direccion" to dataclass_lugares_db.direccion.direccion,
                    "lat" to dataclass_lugares_db.direccion.lat,
                    "log" to dataclass_lugares_db.direccion.log,
                    "referencia" to dataclass_lugares_db.direccion.refencia,
                )
                val numero_atencion = hashMapOf(
                    "whatsapp" to listOf<String>(),
                    "telefono" to listOf<String>()
                )
                val hasmap_datos_lugare = hashMapOf(
                    "direccion" to direccion,
                    "categoria" to dataclass_lugares_db.categoria,
                    "horario_atencion" to hashMap_horario,
                    "id" to dataclass_lugares_db.id,
                    "lugar_nombre" to dataclass_lugares_db.lugar_nombre,
                    "img_logo" to dataclass_lugares_db.logo_img,
                    "contacto" to numero_atencion
                )

                FirebaseFirestore.getInstance().collection("Tiendas").document("servicios_basicos")
                    .collection("barranca").document(dataclass_lugares_db.id)
                    .set(hasmap_datos_lugare)
                    .addOnSuccessListener { res ->
                        Log.d("datos_pasado", "oket")
                    }.addOnFailureListener { e ->
                        Log.d("datos_pasado", "err")
                    }
            }
        }.addOnFailureListener { e->
            Log.d("datos_pasado", "err")
        }


    }
}
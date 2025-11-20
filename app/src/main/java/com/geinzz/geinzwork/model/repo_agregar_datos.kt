package com.geinzz.geinzwork.model

import android.content.Context
import android.util.Log
import com.geinzz.geinzwork.data.model.data_class_tienda_geinz
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.data.model.direccion_lugar
import com.geinzz.geinzwork.data.model.img_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

    fun agraegar_datos_db_2(data_class_tienda_geinz:data_class_tienda_geinz){
        val hasmap=hashMapOf<String, Any>(
            "categoria_tienda" to data_class_tienda_geinz.categoria_tienda,
            "descripcion" to data_class_tienda_geinz.descripcion,
            "geohash" to data_class_tienda_geinz.geogash,
            "horario_atencion" to data_class_tienda_geinz.horario_atencion,
            "id_tienda" to data_class_tienda_geinz.id_tienda,
            "localidad" to data_class_tienda_geinz.localida_tienda,
            "metodo_contacto" to data_class_tienda_geinz.metodo_contacto,
            "metodos_pago" to data_class_tienda_geinz.metodo_pago,
            "modelo_negocio" to data_class_tienda_geinz.modelo_negocio,
            "nombre_tienda" to data_class_tienda_geinz.nombre_tienda,
            "pagado" to data_class_tienda_geinz.pagado,
            "subcategoria" to data_class_tienda_geinz.subcategoria,
            "ubicacion" to data_class_tienda_geinz.ubicacion,
            "img_tienda" to img_tienda()
        )
        db.collection("datos_lugares").document(data_class_tienda_geinz.id_tienda).set(hasmap).addOnSuccessListener { documentReference ->
               Log.d("datos_agregados","correcto")
        }.addOnFailureListener {
            Log.d("datos_agregados","malo")
        }
    }



//    fun pasar_datos() {
//        var datos_compeltos = mutableListOf<dataclass_lugares_db>()
//
//        db.collection("datos_lugares").get().addOnSuccessListener { res ->
//            for (datos in res) {
//                val data = datos.data
//                val categoira = data?.get("categoria") as? List<String> ?: emptyList()
//                val id = data?.get("id") as? String ?: ""
//                val img = data?.get("img") as? String ?: ""
//                val nombre_lugar = data?.get("lugar_nobre") as? String ?: ""
//                val lat = data?.get("lat") as? Number ?: 0
//                val log = data?.get("log") as? Number ?: 0
//                val lugar = dataclass_lugares_db(
//                    categoria = categoira,
//                    direccion = direccion_lugar(lat = lat.toDouble(), log = log.toDouble()),
//                    horario_atencion = emptyMap(),
//                    id = id,
//                    lugar_nombre = nombre_lugar,
//                    logo_img = img, pagado = data["pagado"] as? Boolean ?:false
//                )
//                datos_compeltos.add(lugar)
//            }
//            for (dataclass_lugares_db in datos_compeltos) {
//
//                val hashMap_horario = hashMapOf(
//                    "lunes" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
//                    "martes" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
//                    "miercoles" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
//                    "jueves" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
//                    "viernes" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
//                    "sabado" to hashMapOf("h_abierto" to "", "h_cerrado" to ""),
//                    "domingo" to hashMapOf("h_abierto" to "", "h_cerrado" to "")
//                )
//                val direccion = hashMapOf(
//                    "direccion" to dataclass_lugares_db.direccion.direccion,
//                    "lat" to dataclass_lugares_db.direccion.lat,
//                    "log" to dataclass_lugares_db.direccion.log,
//                    "referencia" to dataclass_lugares_db.direccion.refencia,
//                )
//                val numero_atencion = hashMapOf(
//                    "whatsapp" to listOf<String>(),
//                    "telefono" to listOf<String>()
//                )
//                val hasmap_datos_lugare = hashMapOf(
//                    "direccion" to direccion,
//                    "categoria" to dataclass_lugares_db.categoria,
//                    "horario_atencion" to hashMap_horario,
//                    "id" to dataclass_lugares_db.id,
//                    "lugar_nombre" to dataclass_lugares_db.lugar_nombre,
//                    "img_logo" to dataclass_lugares_db.logo_img,
//                    "contacto" to numero_atencion
//                )
//
//                FirebaseFirestore.getInstance().collection("Tiendas").document("servicios_basicos")
//                    .collection("barranca").document(dataclass_lugares_db.id)
//                    .set(hasmap_datos_lugare)
//                    .addOnSuccessListener { res ->
//                        Log.d("datos_pasado", "oket")
//                    }.addOnFailureListener { e ->
//                        Log.d("datos_pasado", "err")
//                    }
//            }
//        }.addOnFailureListener { e->
//            Log.d("datos_pasado", "err")
//        }
//
//
//    }


    suspend fun obtener_categorias(): Pair<List<String>, List<List<String>>> {
        Log.d("docuemtos","obtenmimosad")
        val listaDocs = mutableListOf<String>()           // IDs de documentos
        val listaSubcategorias = mutableListOf<List<String>>() // Lista de listas internas

        val ref = FirebaseFirestore.getInstance().collection("Tiendas")
            .document("categorias")
            .collection("categorias")
            .get()
            .await()

        for (doc in ref.documents) {
          Log.d("docuemtos","${doc.id}")
            listaDocs.add(doc.id)

            // 2. Obtener la lista interna "subcategorias"
            val sub = doc.get("subcategorias") as? List<String> ?: emptyList()

            listaSubcategorias.add(sub)
        }

        return Pair(listaDocs, listaSubcategorias)
    }




}
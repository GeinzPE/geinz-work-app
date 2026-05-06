package com.geinzz.geinzwork.model
import android.util.Log
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_promp_NLP_depromo_y_oferta
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_promp_para_Descripcion
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_prompt_para_titulo_casa
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.Normalizer
import kotlin.io.normalize


class repo_agregar_inmubles {

    data class agregar_inmubles_datos(
        val ancho:Int=0,
        val banos:String ="",
        val ciudad:String="barranca",
        val descripcion :String="",
        val direccion:String="",
        val distrito:String ="barranca",
        val divisa:String="",
        val estacionamiento:String="0",
        val fondo: Int=0,
        val habitaciones:String="0",
        val id:String="",
        val lat: Double=0.0,
        val lng:Double=0.0,
        val metros:Int=0,
        val nombre:String="",
        val precio:Int=0,
        val referencia: String="",
        val tipoOperacion :String="",
        val tipoPropiedad: String=""
    )

    val db= FirebaseFirestore.getInstance()
    suspend fun generar_titulo_propiedad(
        tipo_realizado: String,
        tipo_operacion: String,
        nombre_Calle: String,
        localidad: String
    ): String? {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")
        val prompt = construir_prompt_para_titulo_casa(tipo_realizado, tipo_operacion,nombre_Calle,localidad)
        val result = model.generateContent(prompt)
        return result.text
    }

    suspend fun generar_descripcion(titulo: String, lista_lugares: List<String>): String? {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")
        val prompt = construir_promp_para_Descripcion(titulo, lista_lugares)
        val result = model.generateContent(prompt)
        return result.text
    }

    suspend fun agregar_inmuebles(data: agregar_inmubles_datos) {
        withContext(Dispatchers.IO) {
            try {
                val ref = db.collection("Tiendas")
                    .document("barranca")
                    .collection("geinz_inmobiliaria")

                val docRef = ref.document() // 🔥 genera ID automático

                val datos = agregar_inmubles_datos(
                    ancho = data.ancho,
                    banos = data.banos,
                    ciudad = data.ciudad,
                    descripcion = data.descripcion,
                    direccion = data.direccion,
                    distrito = data.distrito,
                    divisa = data.divisa,
                    estacionamiento = data.estacionamiento,
                    fondo = data.fondo,
                    habitaciones = data.habitaciones,
                    id = docRef.id, // ✅ ID generado
                    lat = data.lat,
                    lng = data.lng,
                    metros = data.metros,
                    nombre = data.nombre,
                    precio = data.precio,
                    referencia = data.referencia,
                    tipoOperacion = data.tipoOperacion,
                    tipoPropiedad = data.tipoPropiedad
                )

                docRef.set(datos).await() // 🔥 guarda en Firestore

                Log.d("Firestore", "Inmueble agregado correctamente")

            } catch (e: Exception) {
                Log.e("Firestore", "Error al agregar inmueble: ${e.message}")
            }
        }
    }


    suspend fun normalizarSubcategorias() {
        val ref = db.collection("Tiendas")
            .document("barranca")
            .collection("barranca")
            .get()
            .await()

        for (doc in ref) {

            val subcategorias = doc.get("subcategoria") as? List<String> ?: emptyList()


            val normalizadas = subcategorias.map { sub ->
                Normalizer.normalize(sub.lowercase(), Normalizer.Form.NFD)
                    .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                    .trim()
            }
            doc.reference.update(
                "subcategoria", normalizadas
            ).await()

            println("✅ ${subcategorias} -> ${normalizadas}")
        }
    }
    fun normalizar(texto: String): String {
        return Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "") // quita tildes
            .replace(Regex("[^\\w\\s]"), "") // quita puntos, comas, símbolos
            .trim()
    }
    fun generarKeywords(nombre: String): List<String> {
        val limpio = normalizar(nombre)

        val palabras = limpio.split(" ")
            .filter { it.isNotBlank() }
            .filter { it.length > 2 } // 🔥 evita palabras basura tipo "de", "la"

        val combinaciones = mutableListOf<String>()

        // 🔹 palabras individuales
        combinaciones.addAll(palabras)

        // 🔹 combinaciones (tipo "gran chifa")
        for (i in 0 until palabras.size - 1) {
            combinaciones.add("${palabras[i]} ${palabras[i + 1]}")
        }

        return combinaciones.distinct()
    }

    suspend fun agregar_datos_algolia() {
        try {
            val snapshot = db
                .collection("Tiendas")
                .document("barranca")
                .collection("barranca")
                .get()
                .await()

            for (doc in snapshot.documents) {

                val descripcionSeo = doc.getString("descripcion_seo") ?: ""
                val descripcionNormal = doc.getString("descripcion") ?: ""
                val idTienda = doc.id // 👈 mejor usar el ID real del doc

                val imgTienda = doc.get("img_tienda") as? Map<*, *>
                val imagenBot = imgTienda?.get("imagen_bot") as? String ?: ""

                // 🔥 PRIORIDAD
                val descripcionFinal = if (descripcionSeo.isNotEmpty()) {
                    descripcionSeo
                } else {
                    descripcionNormal
                }

                val refAlgolia = db.collection("lugares").document(idTienda)

                val data = mutableMapOf<String, Any>(
                    "descripcion" to descripcionFinal
                )

                // 👇 solo agregar imagen si existe
                if (imagenBot.isNotEmpty()) {
                    data["imagen_bot"] = imagenBot
                }

                // 🔥 MERGE (NO BORRA NADA)
                refAlgolia.set(data, SetOptions.merge()).await()

                Log.d("ALGOLIA_SYNC", "Actualizado: $idTienda")
            }

        } catch (e: Exception) {
            Log.e("ERROR", e.message.toString())
        }
    }
}
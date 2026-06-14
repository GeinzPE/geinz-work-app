package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object get_alias_tienda {

    val db = FirebaseFirestore.getInstance()

    suspend fun obtenerAliasTienda(
        id_tienda: String,
        localidad: String
    ): String? {

        val localidadCompleta = when (localidad.lowercase().trim()) {
            "ba", "barranca" -> "barranca"
            "par", "paramonga" -> "paramonga"
            "pat", "pativilca" -> "pativilca"
            "su", "supe" -> "supe"
            "pue", "puerto supe" -> "puerto supe"
            else -> localidad
        }

        return try {
            Log.d("ALIAS_DEBUG", "Buscando → id: $id_tienda | localidad: $localidadCompleta")

            val document = db.collection("Tiendas")
                .document(localidadCompleta)
                .collection(localidadCompleta)
                .document(id_tienda)
                .get()
                .await()

            Log.d("ALIAS_DEBUG", "Doc existe: ${document.exists()}")
            Log.d("ALIAS_DEBUG", "Campos: ${document.data}")
            Log.d("ALIAS_DEBUG", "alias_key: ${document.getString("alias_key")}")

            document.getString("alias_key")

        } catch (e: Exception) {
            Log.e("ALIAS_DEBUG", "Error: ${e.message}", e)
            null
        }
    }


    fun resolverAlias(
        alias: String,
        contex: Context,
        onResult: (id: String, localidad: String, categoria: String) -> Unit
    ) {

        db.collection("alias_tiendas")
            .document(alias)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val id = document.getString("id") ?: ""
                    val localidad = document.getString("localidad") ?: ""
                    val categoria = document.getString("categoria") ?: ""

                    Log.d(
                        "DeepLinkDebug",
                        "ALIAS RESUELTO -> id=$id, loc=$localidad, cat=$categoria"
                    )
                    onResult(id, localidad, categoria)
                } else {
                    Log.w("DeepLinkDebug", "Alias no encontrado: $alias")
                    Toast.makeText(contex, "Perfil no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("DeepLinkDebug", "Error resolviendo alias", e)
            }
    }

    fun resolver_Alias_de_turismo(
        alias_String: String,
        contex: Context,
        onResult: (id: String, localidad: String, categoria: String) -> Unit,
    ) {
        db.collection("alias_turismo")
            .document(alias_String)
            .get().addOnSuccessListener() { docu ->
                if (docu.exists()) {
                    val id = docu.get("id") as? String ?: ""
                    val categoria = docu.get("categoria") as String ?: ""
                    val localidad = docu.get("localidad") as? String ?: ""
                    onResult(id, localidad, categoria)
                } else {
                    Toast.makeText(contex, "lugar no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
//    private fun navegarATienda(id: String, localidad: String, categoria: String) {
//        fun enc(value: String) = java.net.URLEncoder.encode(value, "UTF-8")
//
//        val ruta = "mostrar_tiendas/${enc(localidad)}/${enc(id)}/${enc(categoria)}"
//
//        Log.d("DeepLinkDebug", "NAVEGANDO -> $ruta")
//        navController.navigate(ruta) {
//            launchSingleTop = true
//            popUpTo(navController.graph.startDestinationId) {
//                inclusive = false
//            }
//        }
//    }
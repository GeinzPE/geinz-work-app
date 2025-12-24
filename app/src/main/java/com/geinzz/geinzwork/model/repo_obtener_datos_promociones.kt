package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.dataclass_promos.promociones_tiendas_negocios
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_obtener_datos_promociones {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtner_datos_promocion(
        id_tienda: String,
        localidad: String,
        index: Int
    ): promociones_tiendas_negocios {

        val ref = db
            .collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .get()
            .await()

        if (!ref.exists()) {
            return promociones_tiendas_negocios(
                id_tienda = id_tienda,
                nombre_tienda = "",
                url_img = "",
                numero_contacto_teinda = "", "", "",""
            )
        }

        val datos = ref.data ?: emptyMap()

        // 🔹 nombre tienda
        val nombre_tienda = datos["nombre_tienda"] as? String ?: ""

        // 🔹 whatsapp
        val metodo_contacto = datos["metodo_contacto"] as? Map<*, *>
        val wsap_numero = metodo_contacto?.get("whatsapp") as? Map<*, *>
        val numero_contacto = wsap_numero?.get("numero")?.toString() ?: ""


        // 🔹 imágenes
        val img_tienda = datos["img_tienda"] as? Map<String, Any>
        val logo_tienda = img_tienda?.get("logo_tienda") as? String ?: ""
        val localidad = datos?.get("localidad") as? String ?: ""

        val lista_img = img_tienda?.get("lista_img") as? Map<String, Any>
        val promociones_lista = lista_img?.get("promociones") as? List<String> ?: emptyList()

        val categoria=datos["categoria_tienda"] as? String?:""

        // 🔹 imagen por índice (seguro)
        val img_principal_por_index =
            if (index in promociones_lista.indices) promociones_lista[index] else ""

        return promociones_tiendas_negocios(
            id_tienda = id_tienda,
            nombre_tienda = nombre_tienda,
            url_img = img_principal_por_index,
            numero_contacto_teinda = numero_contacto, img_logo_tienda = logo_tienda, localidad = localidad,categoria
        )
    }

}
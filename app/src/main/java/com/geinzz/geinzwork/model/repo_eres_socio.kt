package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_eres_socio {
   private val db = FirebaseFirestore.getInstance()

    suspend fun obtener_datos_tienda(id_tienda: String): datos_tienda {
        val ref = db.collection("Tiendas")
            .document("barranca")
            .collection("barranca")
            .document(id_tienda)
            .get()
            .await()

        if (!ref.exists()) {
            return datos_tienda(
                nombre = "",
                img_tienda = "",
                horario_tiendaMap = HorarioAtencion_box()
            )
        }

        val data = ref.data ?: emptyMap<String, Any>()

        val nombre_tienda = data["nombre_tienda"] as? String ?: ""
        val img_tienda=data["img_tienda"] as? Map<String, Any> ?: emptyMap()
        val logo = img_tienda.get("logo_tienda") as? String ?:""

        val horario_atencion = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
        val hoarario_mapBox=horario_atencion.to_horario_atencion_box_dia()
        val id_tienda =data["id_tienda"] as? String ?:""

        return datos_tienda(
            id_tienda,
            nombre = nombre_tienda,
            img_tienda = logo,
            horario_tiendaMap = hoarario_mapBox
        )
    }


}
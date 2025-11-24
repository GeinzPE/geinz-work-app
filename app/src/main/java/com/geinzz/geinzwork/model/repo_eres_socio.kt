package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_eres_socio {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtener_datos_tienda(id_tienda: String): datos_tienda {
        val collectionPadre = db.collection("Tiendas")
            .document("barranca")
            .collection("barranca")
            .document(id_tienda)

        val ref = collectionPadre
            .get()
            .await()

        if (!ref.exists()) {
            return datos_tienda()
        }

        val data = ref.data ?: emptyMap<String, Any>()

        val nombre_tienda = data["nombre_tienda"] as? String ?: ""
        val img_tienda = data["img_tienda"] as? Map<String, Any> ?: emptyMap()
        val logo = img_tienda.get("logo_tienda") as? String ?: ""

        val horario_atencion = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
        val hoarario_mapBox = horario_atencion.to_horario_atencion_box_dia()
        val id_tienda = data["id_tienda"] as? String ?: ""
        val estadisticasRef = collectionPadre
            .collection("estadisticas")

        val vistas = estadisticasRef.document("vistas").get().await()
        val guardados = estadisticasRef.document("guardados").get().await()
        val clic = estadisticasRef.document("clic").get().await()

        val fb = estadisticasRef.document("facebook").get().await()
        val ig = estadisticasRef.document("instagram").get().await()
        val tk = estadisticasRef.document("tiktok").get().await()
        val stweb = estadisticasRef.document("tiktok").get().await()

        val wsap = estadisticasRef.document("tiktok").get().await()
        val llamada = estadisticasRef.document("llamada").get().await()
        val ruta = estadisticasRef.document("ruta").get().await()

        val totalVistas = vistas?.get("total") as? Number ?: 0
        val totalGuardados = guardados?.get("total") as? Number ?: 0
        val totalclic = clic?.get("total") as? Number ?: 0

        val fbtotal = fb?.get("total") as? Number ?: 0
        val igtotal = ig?.get("total") as? Number ?: 0
        val tktotal = tk?.get("total") as? Number ?: 0
        val stwebtotal = stweb?.get("total") as? Number ?: 0
        val wsaptotal = wsap?.get("total") as? Number ?: 0
        val llamadatotal = llamada?.get("total") as? Number ?: 0
        val rutatotal = ruta?.get("total") as? Number ?: 0


        return datos_tienda(
            id_tienda = id_tienda,
            nombre = nombre_tienda,
            img_tienda = logo,
            horario_tiendaMap = hoarario_mapBox,
            total_vista = totalVistas,
            total_guardados = totalGuardados,
            clic = totalclic,
            fb = fbtotal,
            ig = igtotal,
            tk = tktotal,
            stweb = stwebtotal,
            wsap = wsaptotal,
            llamada = llamadatotal,
            ruta = rutatotal
        )
    }


}
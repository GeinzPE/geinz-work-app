package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box

import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.obtener_tiendas_lat_log_id
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem.calcularProximaApertura
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem.obtenerHorarioDeHoy
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem.obtenerHorarioDeHoy_BOX
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularTiempoRestante_box
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.convertirABox
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.estaAbiertoHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.mapearHorarioDia_box2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.Boolean
import kotlin.Number

class repo_filtrado_tiendas {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_subcategorias_tiendas(categorias: String): List<filtrado_tiendas_cat_sub> {
        Log.d("obtenos_", categorias.toString())
        val lista_cat_subcategoria = mutableListOf<filtrado_tiendas_cat_sub>()
        val subcategorias_ref =
            db.collection("Tiendas").document("categorias").collection("categorias")
                .document(categorias).get().await()


        if (subcategorias_ref.exists()) {
            val data = subcategorias_ref.data
            val subcategories = data?.get("subcategorias") as? List<String> ?: emptyList()
            Log.d("obtenos_", subcategories.toString())
            lista_cat_subcategoria.add(filtrado_tiendas_cat_sub(categorias, subcategories))
        }
        return lista_cat_subcategoria
    }


    fun obtener_estado_horario_tienda(horarioAtencion: HorarioAtencion): horario_tienda {
        Log.d("!qefaFsgsAGasgASFDA", horarioAtencion.toString())
        val dias =
            listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
        val calendar = Calendar.getInstance()
        val diaActual = dias[calendar.get(Calendar.DAY_OF_WEEK) - 1]

        val horarioDia = when (diaActual) {
            "lunes" -> horarioAtencion.lunes
            "martes" -> horarioAtencion.martes
            "miércoles" -> horarioAtencion.miercoles
            "jueves" -> horarioAtencion.jueves
            "viernes" -> horarioAtencion.viernes
            "sábado" -> horarioAtencion.sabado
            "domingo" -> horarioAtencion.domingo
            else -> HorarioDia()
        }
        return horario_tienda(
            horarioDia.h_apertura,
            horarioDia.h_cierre,
            horarioDia.cerrado,
            horarioDia.motivo
        )
    }


    fun obtener_estado_horario_tienda_Box(horarioAtencion: HorarioAtencion_box): HorarioDia_box {
        Log.d("HORARIO_BOX", "---- INICIO obtener_estado_horario_tienda_Box ----")

        val dias = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
        val calendar = Calendar.getInstance()
        val diaActual = dias[calendar.get(Calendar.DAY_OF_WEEK) - 1]

        Log.d("HORARIO_BOX", "Día detectado: $diaActual")

        // Convertimos el horario semanal en un Map<String, HorarioDia_bloques>
        val horarioSemanal = mapOf(
            "lunes" to horarioAtencion.lunes,
            "martes" to horarioAtencion.martes,
            "miércoles" to horarioAtencion.miércoles,
            "jueves" to horarioAtencion.jueves,
            "viernes" to horarioAtencion.viernes,
            "sábado" to horarioAtencion.sábado,
            "domingo" to horarioAtencion.domingo
        )

        val diaBloques: HorarioDia_bloques = horarioSemanal[diaActual] ?: HorarioDia_bloques()

        Log.d("HORARIO_BOX", "Datos obtenidos: $diaBloques")

        // Convertimos a HorarioDia_box usando la función mejorada
        val resultado = diaBloques.toHorarioDiaBox(diaActual, horarioSemanal)

        Log.d("HORARIO_BOX", "Resultado mapeado: $resultado")
        Log.d("HORARIO_BOX", "---- FIN obtener_estado_horario_tienda_Box ----")

        return resultado
    }


    fun HorarioDia_bloques.toHorarioDiaBox(
        diaActual: String,
        horarioSemanal: Map<String, HorarioDia_bloques>
    ): HorarioDia_box {

        val debeCalcularProx = this.cerrado || yaPasoElHorario(bloques)

        val (diaProx, horaProx) = if (debeCalcularProx) {
            calcularProximaApertura(diaActual, horarioSemanal)
        } else {
            "" to ""
        }


        return HorarioDia_box(
            bloques = this.bloques,
            cerrado = this.cerrado,
            motivo = this.motivo,
            dia_prox_apertura = diaProx,
            hora_prox_apertura = horaProx
        )
    }

    fun yaPasoElHorario(bloques: List<HorarioBloque>): Boolean {
        if (bloques.isEmpty()) return true

        val ultimo = bloques.last()

        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaCierre = formatter.parse(ultimo.h_cierre)
        val ahora = Calendar.getInstance().time

        return ahora.after(horaCierre)
    }


    suspend fun obtenerSubcategorias(categoria: String): List<String> {
        Log.d("categoriacategoria", categoria)
        return try {
            val snapshot = db.collection("Tiendas")
                .document("categorias")
                .collection("categorias")
                .document(categoria)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.get("subcategorias") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener subcategorías de $categoria", e)
            emptyList()
        }
    }


    suspend fun obtenerTiendasFiltradas(
        localidad: String,
        categoria: String
    ): List<tiendas_por_categoria> {
        Log.d("localida", "$localidad $categoria")
        val lista_tiendas_filtradas = mutableListOf<tiendas_por_categoria>()
        try {
            val tiendas = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .whereEqualTo("categoria_tienda", categoria)
                .get()
                .await()

            tiendas.forEach { i ->
                val subcategorias_list = i.get("subcategoria") as? List<String> ?: emptyList()
                val ubicacion = i.get("ubicacion") as? Map<String, Any>
                val direccion = ubicacion?.get("dirección") as? String ?: ""
                val referencia = ubicacion?.get("referencia") as? String ?: ""
                val latitud = ubicacion?.get("latitud") as? Number ?: 0
                val longitud = ubicacion?.get("longitud") as? Number ?: 0
                val descripcion = i.get("descripcion") as? String ?: ""
                val localidad = i.get("localidad") as? String ?: ""
                val id_tienda = i.get("id_tienda") as? String ?: ""
                val map_img_tienda = i.get("img_tienda") as? Map<String, Any> ?: emptyMap()
                val logo_tienda = map_img_tienda.get("logo_tienda") as? String ?: ""
                val pagado = i.get("pagado") as? Boolean ?: false
                val horario = i.get("horario_atencion") as? Map<String, Any> ?: emptyMap()
                val metodos_contacto = i.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
                val metodo_pago = i.get("metodos_pago") as? Map<String, Any> ?: emptyMap()
                val contacto_obs = metodos_contacto.toMetodoContacto()
                val metodo_pago_tienda = metodo_pago.to_metodo_pago()
                val horario_tienda_box = horario.to_horario_atencion_box_dia()
                val horarioHoyBloques = obtenerHorarioDeHoy_BOX(horario_tienda_box)
                val horarioHoyBox = convertirABox(horarioHoyBloques)
                val estaAbierto = estaAbiertoHoy(horarioHoyBox)

                lista_tiendas_filtradas.add(
                    tiendas_por_categoria(
                        localidad,
                        nombre_tienda = i.get("nombre_tienda") as? String ?: "",
                        direccion = direccion,
                        referencia = referencia,
                        logo_tienda = logo_tienda,
                        latitud = latitud.toDouble(),
                        longitud = longitud.toDouble(),
                        lista_subcategoiras = subcategorias_list,
                        descripcion = descripcion,
                        id_tienda = id_tienda,
                        pagado = pagado,
                        estaAbierto = estaAbierto, contacto_tienda = contacto_obs,
                        metodos_pago_tienda = metodo_pago_tienda, horario_tienda_box
                    )
                )

            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener tiendas filtradas", e)
        }
        return lista_tiendas_filtradas
    }


    suspend fun obtenner_campos_tiendas_espesifica(
        localidad: String,
        id_tienda: String
    ): List<modelo_tienda> {
        val lista_modelo_tienda = mutableListOf<modelo_tienda>()

        // Obtener todas las tiendas de la localidad
        val tiendasSnapshot = db.collection("Tiendas").document(localidad)
            .collection(localidad).get().await()

        // Buscar el documento específico por id_tienda
        val tiendaDoc = tiendasSnapshot.documents.find { it.id == id_tienda }

        if (tiendaDoc != null && tiendaDoc.exists()) {
            val data = tiendaDoc.data
            val map_img = data?.get("img_tienda") as? Map<String, Any> ?: emptyMap()
            val horarioMap = data?.get("horario_atencion") as? Map<String, Any> ?: emptyMap()
            val metodos_contacto = data?.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
            val metodo_pago = data?.get("metodos_pago") as? Map<String, Any> ?: emptyMap()
            val metodo_pago_tienda = metodo_pago.to_metodo_pago()
            val horario_atencion_Box = horarioMap.to_horario_atencion_box_dia()
            Log.d("viendo_contacto", metodos_contacto.toString())

            // Función auxiliar para mapear un día
            fun mapearDia(diaMap: Map<String, Any>?): HorarioDia {
                if (diaMap == null) return HorarioDia()
                return HorarioDia(
                    cerrado = diaMap["cerrado"] as? Boolean ?: false,
                    h_apertura = diaMap["h_apertura"] as? String ?: "",
                    h_cierre = diaMap["h_cierre"] as? String ?: "",
                    motivo = diaMap["motivo"] as? String ?: ""
                )
            }

            val contacto_obs = metodos_contacto.toMetodoContacto()
            Log.d("metodo_contacot", contacto_obs.toString())


            val horarioTienda = HorarioAtencion(
                lunes = mapearDia(horarioMap["lunes"] as? Map<String, Any>),
                martes = mapearDia(horarioMap["martes"] as? Map<String, Any>),
                miercoles = mapearDia(horarioMap["miércoles"] as? Map<String, Any>),
                jueves = mapearDia(horarioMap["jueves"] as? Map<String, Any>),
                viernes = mapearDia(horarioMap["viernes"] as? Map<String, Any>),
                sabado = mapearDia(horarioMap["sábado"] as? Map<String, Any>),
                domingo = mapearDia(horarioMap["domingo"] as? Map<String, Any>)
            )

            val tiendaModelo = modelo_tienda(
                categoria_tienda = data?.get("categoria_tienda") as? String ?: "",
                descripcion = data?.get("descripcion") as? String ?: "",
                id_tienda = data?.get("id_tienda") as? String ?: "",
                img_perfil = map_img["logo_tienda"] as? String ?: "",
                lista_img = map_img["lista_img"] as? List<String> ?: emptyList(),
                localidad = data?.get("localidad") as? String ?: "",
                modelo_negocio = data?.get("modelo_negocio") as? Boolean ?: false,
                nombre_tienda = data?.get("nombre_tienda") as? String ?: "",
                subcategoria = data?.get("subcategoria") as? List<String> ?: emptyList(),
                ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap(),
                pagado = data?.get("pagado") as? Boolean ?: false,
                metodo_contacto_tienda = contacto_obs,
                horario_atencion = horarioTienda,
                metodos_pago_tienda = metodo_pago_tienda, horario_tienda_box = horario_atencion_Box
            )

            lista_modelo_tienda.add(tiendaModelo)
        }

        return lista_modelo_tienda
    }


    suspend fun obtener_campos_tienda_free(
        localida: String,
        id_tienda: String
    ): datos_tienda_free {
        val data =
            db.collection("Tiendas").document(localida).collection(localida).document(id_tienda)
                .get().await()
        val map_img = data?.get("img_tienda") as? Map<String, Any> ?: emptyMap()
        val map_ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
        val dirección = map_ubicacion.get("dirección") as? String ?: ""
        val referencia = map_ubicacion.get("referencia") as? String ?: ""
        val logo_tienda = map_img.get("logo_tienda") as? String ?: ""
        return datos_tienda_free(
            nombre_ = data.get("nombre_tienda") as? String ?: "",
            img = logo_tienda,
            ubicacion = dirección,
            referencia = referencia,
            horario_default = "no disponible"
        )
    }

//    suspend fun obtenerHorarioPorTienda(idTienda: String, localidad: String): horario_Dia? {
//        val diasSemana = listOf(
//            "domingo", "lunes", "martes", "miércoles",
//            "jueves", "viernes", "sábado"
//        )
//
//        // Obtener día actual
//        val hoyIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
//        val diaHoy = diasSemana[hoyIndex]
//
//        val tiendaSnapshot = db.collection("Tiendas")
//            .document(localidad)
//            .collection(localidad)
//            .document(idTienda)
//            .collection("horario_atencion")
//            .document("horario_atencion")
//            .get()
//            .await()
//
//        if (!tiendaSnapshot.exists()) return null
//
//        val data = tiendaSnapshot.data ?: return null
//        val infoDia = data[diaHoy] as? Map<*, *> ?: return null
//
//        return horario_Dia(
//            dia = diaHoy,
//            h_apertura = infoDia["h_apertura"] as? String ?: "",
//            h_cierre =
//                infoDia["h_cierre"] as? String ?: ""
//        )
//    }


//    suspend fun obtenerHorarioPorTienda2(idTienda: String, localidad: String): HorarioTienda? {
//        val listaDias = listOf(
//            "lunes", "martes", "miércoles",
//            "jueves", "viernes", "sábado", "domingo"
//        )
//
//        val tiendaSnapshot = db.collection("Tiendas")
//            .document(localidad)
//            .collection(localidad)
//            .document(idTienda)
//            .collection("horario_atencion")
//            .document("horario_atencion")
//            .get()
//            .await()
//
//        if (!tiendaSnapshot.exists()) return null
//
//        val data = tiendaSnapshot.data ?: emptyMap<String, Any>()
//        val listaHorarios = listaDias.map { dia ->
//            val infoDia = data[dia] as? Map<*, *>
//            horario_Dia(
//                dia = dia,
//                h_apertura = infoDia?.get("h_apertura") as? String ?: "",
//                h_cierre = infoDia?.get("h_cierre") as? String ?: ""
//
//
//            )
//        }
//
//        return HorarioTienda(idTienda, listaHorarios)
//    }


//    suspend fun obtener_tiendas_por_subcateogira(G
//        subcategoria: String,
//        localidad: String
//    ): List<tiendas_por_categoria> {
//        val lista_por_subcateogira = mutableListOf<tiendas_por_categoria>()
//        val datos_tienda = db.collection("Tiendas").document(localidad).collection(localidad)
//            .whereArrayContains("subcategoria", subcategoria).get().await()
//        for (document in datos_tienda.documents) {
//            val data = document.data
//            val ubicacion = data?.get("ubicacion") as? Map<String, Any>
//            val map_img_tienda = data?.get("img_tienda") as? Map<String, Any> ?: emptyMap()
//            val latitud = ubicacion?.get("latitud") as? Number ?: 0
//            val longitud = ubicacion?.get("longitud") as? Number ?: 0
//            lista_por_subcateogira.add(
//                tiendas_por_categoria(
//                    nombre_tienda = data?.get("nombre_tienda") as? String ?: "",
//                    direccion = ubicacion?.get("dirección") as? String ?: "",
//                    referencia = ubicacion?.get("referencia") as? String ?: "",
//                    latitud = latitud.toDouble(),
//                    longitud = longitud.toDouble(),
//                    logo_tienda = map_img_tienda.get("logo_tienda") as? String ?: "",
//                    lista_subcategoiras = data?.get("subcategoria") as? List<String> ?: emptyList(),
//                    descripcion = data?.get("descripcion") as? String ?: "",
//                    id_tienda = data?.get("id_tienda") as? String ?: ""
//                )
//            )
//
//        }
//        return lista_por_subcateogira
//    }


    suspend fun obtener_tienas_filtradas(
        localidad: String,
    ): List<obtener_tiendas_lat_log_id> {
        val lista_lat_log = mutableListOf<obtener_tiendas_lat_log_id>()
        val ref = db.collection("Tiendas").document(localidad).collection(localidad).get().await()
        for (datos in ref) {
            val data = datos.data
            val id_tienda = data?.get("id_tienda") as? String ?: ""
            val nombre_tienda = data.get("nombre_tienda") as? String ?: ""
            val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
            val lat = ubicacion.get("latitud") as? Number ?: 0
            val log = ubicacion.get("longitud") as? Number ?: 0
            val direccion = ubicacion.get("dirección") as? String ?: ""
            val referencia = ubicacion.get("referencia") as? String ?: ""
            val dataclass = obtener_tiendas_lat_log_id(
                lat.toDouble(),
                log.toDouble(),
                id_tienda,
                direccion,
                referencia,
                nombre_tienda
            )
            lista_lat_log.add(dataclass)
        }
        return lista_lat_log
    }


    suspend fun guardar_tienda_favorito(id_user: String, item: favoritos_guardados) {
        val ref = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users").collection("users").document(id_user)
            .collection("favoritos").document(item.id_tienda_lugar)

        val timestampLocal = System.currentTimeMillis()

        val data = mapOf(
            "id_tienda_lugar" to item.id_tienda_lugar,
            "nombre_lugar_tienda" to item.nombre_lugar_tienda,
            "categoria" to item.categoria,
            "timesLap_local" to timestampLocal.toString(),
            "horario" to item.horario_tienda_box,
            "latitud" to item.lat,
            "longitud" to item.lng,
            "img_tienda_lugar" to item.img_tienda,
            "localidad_lugar_tienda" to item.localida_tienda

        )

        try {
            ref.set(data).await()
            Log.d("FAVORITOS", "Guardado correctamente")
            guardar_user_para_tienda_db(id_user, item.id_tienda_lugar, item.localida_tienda)
        } catch (e: Exception) {
            Log.e("FAVORITOS", "Error al guardar: ${e.message}")
            throw e
        }

    }

    fun guardar_user_para_tienda_db(id_user: String, id_tienda: String, localidad: String) {
        val ref = db.collection("Tiendas")
            .document(localidad).collection(localidad).document(id_tienda)
            .collection("seguidores").document(id_user)

        ref.set(mapOf<String, Any>())  // documento vacío
    }

    fun eliminar_uer_tienda_fv(id_user: String, id_tienda: String, localidad: String) {
        val ref = db.collection("Tiendas")
            .document(localidad).collection(localidad).document(id_tienda)
            .collection("seguidores").document(id_user).delete()
            .addOnSuccessListener { Log.d("terminado", "correctamente") }
    }


    suspend fun obtener_datos_tienda_id(localidad: String, id_tienda: String): favoritos_guardados {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .get()
            .await()

        return if (ref.exists()) {
            val data = ref.data ?: emptyMap<String, Any>()

            val imgMap = data["img_tienda"] as? Map<String, Any> ?: emptyMap()
            val metodoPagoMap = data["metodos_pago"] as? Map<String, Any> ?: emptyMap()
            val ubicacionMap = data["ubicacion"] as? Map<String, Any> ?: emptyMap()
            val horarioMap = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
//            val metodo_pago_tienda = metodoPagoMap.to_metodo_pago()
//            val horario_atencio =horarioMap.to_horario_atencion()
            val horario_atencion_box = horarioMap.to_horario_atencion_box_dia()
            favoritos_guardados(
                img_tienda = imgMap["logo_tienda"] as? String ?: "",
                id_tienda_lugar = data["id_tienda"] as? String ?: "",
                nombre_lugar_tienda = data["nombre_tienda"] as? String ?: "",
//                tag_sub = (data["subcategoria"] as? List<String>) ?: emptyList(),
                categoria = data["categoria_tienda"] as? String ?: "",
                timesLap = "", // si quieres, calcula el lapso
//                horario_tienda = horario_atencio, // aquí puedes mapearlo si tienes los datos
//                metodos_pago = metodo_pago_tienda,
                lat = (ubicacionMap["latitud"] as? Double) ?: 0.0,
                lng = (ubicacionMap["longitud"] as? Double) ?: 0.0,
                localida_tienda = data["localidad"] as? String ?: "",
                estaAbierto = false,
//                horario = horario_tienda() ,
                horario_tienda_box = horario_atencion_box
            )
        } else {
            favoritos_guardados(
                localida_tienda = localidad
            )
        }
    }

    suspend fun eliminar_tienda_favorito(id_user: String, id_tienda: String) {
        val ref = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users").collection("users").document(id_user)
            .collection("favoritos").document(id_tienda)

        ref.delete().await()
    }

    suspend fun verificar_favorito(id_user: String, id_tienda: String): Boolean {
        val ref = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("users")
            .document(id_user)
            .collection("favoritos")
            .document(id_tienda)

        val doc = ref.get().await()
        return doc.exists()
    }

}
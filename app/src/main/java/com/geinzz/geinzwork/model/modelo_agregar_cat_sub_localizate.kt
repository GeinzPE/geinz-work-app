package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_horarios_atencion_tiendas
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria

import com.geinzz.geinzwork.data.model.localizate_geinz.estadoTienda
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class modelo_agregar_cat_sub_localizate {
    val db = FirebaseFirestore.getInstance()
//    fun agregar_categorias(lista: List<dataclass_cat_sub>) {
//        val Cat_sub_tiendas =
//            db.collection("Tiendas").document("categorias").collection("categorias")
//        lista.forEach { i ->
//            val hasmap = hashMapOf<String, Any>(
//                "subcategorias" to i.lista_subcategorias
//            )
//            Cat_sub_tiendas.document(i.nombre.toString()).set(hasmap).addOnSuccessListener { res ->
//                Log.d("correcto", "se agregaron toda las categorias correcteamnte")
//            }.addOnFailureListener { e ->
//                Log.d("correcto", "ocurrio un arrero al gregar toda las categorias")
//
//            }
//
//        }
//    }


    suspend fun obtener_tiendas_categorias_activas_registradas(filtrado_localidad: String): List<encontradas_por_categoria> {
        val lista_activos_registrados_categoria = mutableListOf<encontradas_por_categoria>()
        val lista = obtener_categorias_subcategorias()
        lista.forEach { i ->
            val nombre_categoria = i.nombre.toString()
            val activos_por_localidad = obtenerTiendas_registradas_activas_por_categoria(
                filtrado_localidad,
                nombre_categoria,i.lista_subcategorias
            )
            activos_por_localidad.forEach { i ->
                val datos =
                    encontradas_por_categoria(i.cantidad_registradas, i.activas, i.categoria,i.subcateogiras)
                lista_activos_registrados_categoria.add(datos)
            }
        }
        return lista_activos_registrados_categoria
    }

    suspend fun obtener_categorias_subcategorias(): List<dataclass_cat_sub> {
        val lista = mutableListOf<dataclass_cat_sub>()
        val categoriasRef = db.collection("Tiendas").document("categorias").collection("categorias")
        val snapshot = categoriasRef.get().await()
        lista.clear()
        for (cate in snapshot.documents) {
            val subcategoriasDoc = categoriasRef.document(cate.id).get().await()
            if (subcategoriasDoc.exists()) {
                val data = subcategoriasDoc.data
                val subcategorias = data?.get("subcategorias") as? List<String>

                val datos = dataclass_cat_sub(cate.id.lowercase(), subcategorias)
                lista.add(datos)

            }
        }
        return lista
    }

    suspend fun obtenerTiendas_registradas_activas_por_categoria(
        categoria_filtrada_localidad: String,
        categoria_filtrada: String,
        listaSubcategorias: List<String>?
    ): List<encontradas_por_categoria> {
        val lista_encotrado = mutableListOf<encontradas_por_categoria>()
        val categoria = categoria_filtrada_localidad.lowercase()
        val collectionTiendas = db
            .collection("Tiendas")
            .document(categoria)
            .collection(categoria)

        val snapshot = collectionTiendas.get().await()

        val coincidenciasCategoria = snapshot.filter { doc ->
            doc.getString("categoria_tienda") == categoria_filtrada
        }
        val cantidadRegistradas = coincidenciasCategoria.size
        var cantidadActivas = 0

        for (datos in coincidenciasCategoria) {
            val id_tienda = datos.getString("id_tienda") ?: continue
            val horarioSnapshot = collectionTiendas.document(id_tienda)
                .collection("horario_atencio")
                .document("horario_atencion")
                .get()
                .await()
            val dias_sema = constantes_lista_localidades.dias_sema
            val lista_horario_por_tienda = mutableListOf<horario_tienda>()
            for (dias in dias_sema) {
                val diaMap = horarioSnapshot.get(dias) as? Map<*, *>
                val h_apertura = diaMap?.get("h_apertura") as? String ?: "Sin horario"
                val h_cierre = diaMap?.get("h_cierre") as? String ?: "Sin horario"
                val datos = horario_tienda(id_tienda, dias, h_apertura, h_cierre)
                lista_horario_por_tienda.add(datos)
            }
            Log.d("temonos_teindas", lista_horario_por_tienda.toString())
            val tienda_activa = verificarSiEstaAbierto(lista_horario_por_tienda)
            if (tienda_activa) {
                cantidadActivas++
            }

        }
        // Agregar solo una vez por categoría
        val resultado = encontradas_por_categoria(
            cantidad_registradas = cantidadRegistradas,
            activas = cantidadActivas,
            categoria = categoria_filtrada,listaSubcategorias!!)
        lista_encotrado.add(resultado)


        return lista_encotrado
    }

    suspend fun obtenerCantidadTiendasPorLocalidad(categoria_filtrada_localidad: String): Int {
        val categoria = categoria_filtrada_localidad.lowercase()
        val collectionTiendas = db
            .collection("Tiendas")
            .document(categoria)
            .collection(categoria)
        val snapshot = collectionTiendas.get().await()
        return snapshot.size()
    }

//    suspend fun obtenerTiendas_registradas_activas(
//        categoria_filtrada_localidad: String,
//        lista_categorias: List<dataclass_cat_sub>
//    ): List<encontradas_por_categoria> {
//        val lista_encotrado = mutableListOf<encontradas_por_categoria>()
//        val categoria = categoria_filtrada_localidad.lowercase()
//        val collectionTiendas = db
//            .collection("Tiendas")
//            .document(categoria)
//            .collection(categoria)
//
//        val snapshot = collectionTiendas.get().await()
//
//        lista_categorias.forEach { cat ->
//            val coincidenciasCategoria = snapshot.filter { doc ->
//                doc.getString("categoria_tienda") == cat.nombre
//            }
//            val cantidadRegistradas = coincidenciasCategoria.size
//            var cantidadActivas = 0
//
//            for (datos in coincidenciasCategoria) {
//                val id_tienda = datos.getString("id_tienda") ?: continue
//                val horarioSnapshot = collectionTiendas.document(id_tienda)
//                    .collection("horario_atencio")
//                    .document("horario_atencion")
//                    .get()
//                    .await()
//                val dias_sema =
//                    listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")
//                val lista_horario_por_tienda = mutableListOf<horario_tienda>()
//                for (dias in dias_sema) {
//                    val diaMap = horarioSnapshot.get(dias) as? Map<*, *>
//                    val h_apertura = diaMap?.get("h_apertura") as? String ?: "Sin horario"
//                    val h_cierre = diaMap?.get("h_cierre") as? String ?: "Sin horario"
//                    val datos = horario_tienda(id_tienda, dias, h_apertura, h_cierre)
//                    lista_horario_por_tienda.add(datos)
//                }
//                Log.d("temonos_teindas", lista_horario_por_tienda.toString())
//                val tienda_activa = verificarSiEstaAbierto(lista_horario_por_tienda)
//                if (tienda_activa) {
//                    cantidadActivas++
//                }
//
//            }
//            // Agregar solo una vez por categoría
//            val resultado = encontradas_por_categoria(
//                cantidad_registradas = cantidadRegistradas,
//                activas = cantidadActivas,
//                categoria = cat.nombre
//            )
//            lista_encotrado.add(resultado)
//        }
//
//        return lista_encotrado
//    }



    suspend fun obtener_verificar_horario_tiendas(tiendas_encontradas: List<dataclass_horarios_atencion_tiendas>) {
        tiendas_encontradas.forEach { i ->
            val dias_sema =
                listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")
            val collectionTiendas = db
                .collection("Tiendas")
                .document(i.localidad_tienda.toString())
                .collection(i.localidad_tienda.toString())
            val snapshot = collectionTiendas.get().await()
        }
    }

//    suspend fun verificar_horario_tienda_activa(categoria_filtrada_localidad: String): List<dataclass_horarios_atencion_tiendas> {
//        val lista_horarios = mutableListOf<dataclass_horarios_atencion_tiendas>()
//        val categoria = categoria_filtrada_localidad.lowercase()
//        val collectionTiendas = db
//            .collection("Tiendas")
//            .document(categoria)
//            .collection(categoria)
//        val snapshot = collectionTiendas.get().await()
//        for (datos in snapshot) {
//            val id_tienda = datos.getString("id_tienda") ?: continue
//            val categoria_tienda = datos.getString("categoria_tienda") ?: continue
//            val subategorias = datos?.get("subategorias") as? List<String> ?: emptyList()
//            val horario_tiendas =
//                collectionTiendas.document(id_tienda).collection("horario_atencio")
//                    .document("horario_atencion").get().await()
//            if (horario_tiendas.exists()) {
//                val horario_por_dia = mutableMapOf<String, Pair<String, String>>()
//                for (dias in dias_sema) {
//                    val diaMap = horario_tiendas.get(dias) as? Map<*, *>
//                    val h_apertura = diaMap?.get("h_apertura") as? String ?: "Sin horario"
//                    val h_cierre = diaMap?.get("h_cierre") as? String ?: "Sin horario"
//
//                    horario_por_dia[dias] = h_apertura to h_cierre
//                }
//                subategorias.map { i ->
//                    horario_por_dia.forEach { (dia, horario) ->
//                        val lista = dataclass_horarios_atencion_tiendas(
//                            id_tienda,
//                            categoria_filtrada_localidad,
//                            dia,
//                            horario.first,
//                            horario.second, categoria_tienda, i
//                        )
//                        lista_horarios.add(lista)
//                    }
//                }
//
//                lista_horarios.forEachIndexed { index, horario ->
//                    Log.d("horario_tienda", "[$index] $horario")
//                }
//            }
//        }
//        return lista_horarios
//    }

//    suspend fun obtener_horarios_tiendas(doc: DocumentSnapshot, subategorias: List<String>) {
//        val dias_sema =
//            listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")
//        if (doc.exists()) {
//            val horario_por_dia = mutableMapOf<String, Pair<String, String>>()
//            for (dias in dias_sema) {
//                val diaMap = doc.get(dias) as? Map<*, *>
//                val h_apertura = diaMap?.get("h_apertura") as? String ?: "Sin horario"
//                val h_cierre = diaMap?.get("h_cierre") as? String ?: "Sin horario"
//
//                horario_por_dia[dias] = h_apertura to h_cierre
//            }
//            subategorias.map { i ->
//                horario_por_dia.forEach { (dia, horario) ->
//                    val lista = dataclass_horarios_atencion_tiendas(
//                        id_tienda,
//                        categoria_filtrada_localidad,
//                        dia,
//                        horario.first,
//                        horario.second, categoria_tienda, i
//                    )
//                    lista_horarios.add(lista)
//                }
//            }
//
//            lista_horarios.forEachIndexed { index, horario ->
//                Log.d("horario_tienda", "[$index] $horario")
//            }
//        }
//    }

    fun verificarSiEstaAbierto(lista_horarios_por_tienda: List<horario_tienda>): Boolean {
        return try {
            val diaActualConTilde =
                SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).lowercase()
            val diaActual = quitarTildes(diaActualConTilde)
            Log.d("HORARIO_CHECK", "Día actual: $diaActual")

            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val ahora = formato.parse(formato.format(Date())) ?: return false
            Log.d("HORARIO_CHECK", "Hora actual: ${formato.format(ahora)}")

            lista_horarios_por_tienda.forEach { i ->
                val dia = i.dia!!.lowercase()
                val diaSinTilde = quitarTildes(dia)
                Log.d("HORARIO_CHECK", "Evaluando día: $diaSinTilde")

                if (diaSinTilde == diaActual) {
                    val apertura = formato.parse(i.h_apertura)
                    val cierre = formato.parse(i.h_cierre)
                    Log.d(
                        "HORARIO_CHECK",
                        "Horario -> Apertura: ${i.h_apertura}, Cierre: ${i.h_cierre}"
                    )

                    if (apertura == null || cierre == null) {
                        Log.w("HORARIO_CHECK", "Horario inválido, se omite este día.")
                        return@forEach
                    }

                    val estaAbierto = if (cierre.after(apertura)) {
                        // Ejemplo: 08:00 - 18:00
                        ahora in apertura..cierre
                    } else {
                        // Ejemplo: 22:00 - 02:00 (día siguiente)
                        ahora.after(apertura) || ahora.before(cierre)
                    }

                    Log.d("HORARIO_CHECK", "¿Está abierto hoy? $estaAbierto")

                    if (estaAbierto) return true
                }
            }

            false // Ningún horario coincide y está activo
        } catch (e: Exception) {
            Log.e("verificarSiEstaAbierto", "Error al verificar horario", e)
            false
        }
    }


    fun verificar_activos_desactivos(lista_horarios_tiendas: List<dataclass_horarios_atencion_tiendas>): List<estadoTienda> {
        val estados_tiendas = mutableListOf<estadoTienda>()
        val diaActualConTilde =
            SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).lowercase()
        val diaActual = quitarTildes(diaActualConTilde)
        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val ahora = formatoHora.parse(horaActual)

        lista_horarios_tiendas.forEach { i ->
            val id = i.id_tienda
            val apertura = i.h_apertura
            val cierre = i.h_cierre
            val dia = i.dia
            val localidad_tienda = i.localidad_tienda
            val categoria = i.categoria_tienda
            val subcategoria = i.subcategoria

            if (dia == diaActual && apertura != "sin horario" && cierre != "sin horario") {
                try {
                    val horaApertura = formatoHora.parse(apertura)
                    val horaCierre = formatoHora.parse(cierre)

                    val estaAbierto = if (horaCierre.after(horaApertura)) {
                        // Caso normal: mismo día
                        val dentroHorario = ahora in horaApertura..horaCierre
                        Log.d("DEBUG_HORARIO", "Horario normal (mismo día)")
                        Log.d("DEBUG_HORARIO", "Hora apertura: $horaApertura")
                        Log.d("DEBUG_HORARIO", "Hora cierre: $horaCierre")
                        Log.d("DEBUG_HORARIO", "Hora actual: $ahora")
                        Log.d("DEBUG_HORARIO", "¿Está abierto?: $dentroHorario")
                        dentroHorario
                    } else {
                        // Caso especial: cierre al día siguiente (madrugada)
                        val dentroHorario = ahora.after(horaApertura) || ahora.before(horaCierre)
                        Log.d("DEBUG_HORARIO", "Horario de madrugada (cierra al día siguiente)")
                        Log.d("DEBUG_HORARIO", "Hora apertura: $horaApertura")
                        Log.d("DEBUG_HORARIO", "Hora cierre: $horaCierre")
                        Log.d("DEBUG_HORARIO", "Hora actual: $ahora")
                        Log.d("DEBUG_HORARIO", "¿Está abierto?: $dentroHorario")
                        dentroHorario
                    }


                    if (estaAbierto) {
                        val estado_tienda = estadoTienda(
                            id_tienda = id,
                            localidad_tienda = localidad_tienda,
                            abierto_cerrado = estaAbierto,
                            categoria,
                            subcategoria,
                            ""
                        )
                        estados_tiendas.add(estado_tienda)
                        Log.d("obtenoes_teindas_solo_Aviertas", estado_tienda.toString())
                    }

                } catch (e: Exception) {
                    Log.e(
                        "ErrorHorario",
                        "Formato de hora inválido en tienda $id: $apertura a $cierre"
                    )
                }
            }
        }
        return estados_tiendas
    }

    fun quitarTildes(texto: String): String {
        val normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

//    fun formatearTiempoRestante(horaApertura: Date, horaCierre: Date): String {
//        val ahora = Date()
//
//        // Ajustamos si el cierre es al día siguiente
//        val horaCierreReal = if (horaCierre.before(horaApertura)) {
//            Calendar.getInstance().apply {
//                time = horaCierre
//                add(Calendar.DATE, 1)
//            }.time
//        } else {
//            horaCierre
//        }
//
//        val diferenciaMillis = horaCierreReal.time - ahora.time
//
//        if (diferenciaMillis <= 0) return "Ya cerró"
//
//        val minutos = diferenciaMillis / (1000 * 60)
//        val horas = minutos / 60
//        val dias = horas / 24
//
//        return when {
//            dias > 0 -> "Cierra en $dias día${if (dias > 1) "s" else ""}"
//            horas > 0 -> "Cierra en $horas hora${if (horas > 1) "s" else ""}"
//            minutos > 0 -> "Cierra en $minutos minuto${if (minutos > 1) "s" else ""}"
//            else -> "Ya cerró"
//        }
//    }

}
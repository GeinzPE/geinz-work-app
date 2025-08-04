package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_horarios_atencion_tiendas
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria

import com.geinzz.geinzwork.data.model.localizate_geinz.estadoTienda
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.tienda_patrocinada
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class repo_agregar_cat_sub_localizate {

    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_tiendas_categorias_activas_registradas(filtrado_localidad: String): List<encontradas_por_categoria> {
        val lista_activos_registrados_categoria = mutableListOf<encontradas_por_categoria>()
        val lista = obtener_categorias_subcategorias()
        lista.forEach { i ->
            val nombre_categoria = i.nombre.toString()
            val activos_por_localidad = obtenerTiendas_registradas_activas_por_categoria(
                filtrado_localidad,
                nombre_categoria, i.lista_subcategorias, i.lista_img
            )
            activos_por_localidad.forEach { i ->
                val datos =
                    encontradas_por_categoria(
                        i.cantidad_registradas,
                        i.activas,
                        i.categoria,
                        i.subcateogiras, i.img_subcategorias
                    )
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
                val img_data = data?.get("img_categoria") as? String? ?: ""
                val datos = dataclass_cat_sub(cate.id.lowercase(), subcategorias, img_data)
                lista.add(datos)

            }
        }
        return lista
    }

    suspend fun obtenerTiendas_registradas_activas_por_categoria(
        categoria_filtrada_localidad: String,
        categoria_filtrada: String,
        listaSubcategorias: List<String>?,
        listaImg: String
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
            categoria = categoria_filtrada, listaSubcategorias!!, listaImg
        )
        lista_encotrado.add(resultado)


        return lista_encotrado
    }


    suspend fun obtenerTiendasPatrocinadas(
        localidadSeleccionada: String,
        categoriaSeleccionada: String
    ): List<tienda_patrocinada> {
        return try {
            val snapshot = db.collection("Tiendas")
                .document(localidadSeleccionada)
                .collection("patrocinadas")
                .whereEqualTo("categoria", categoriaSeleccionada)
                .get()
                .await()

            snapshot.mapNotNull { doc ->
                val categoria = doc.getString("categoria") ?: return@mapNotNull null
                val idTienda = doc.getString("id_tienda") ?: return@mapNotNull null
                val imgPerfil = doc.getString("img_perfil") ?: return@mapNotNull null
                val nombre = doc.getString("nombre") ?: return@mapNotNull null
                val coordenadaMap = doc.get("ubicacion") as? Map<*, *>
                val latitud = coordenadaMap?.get("latitud") as? Number ?: 0
                val longitud = coordenadaMap?.get("longitud") as? Number ?: 0
                val direccion = coordenadaMap?.get("direccion") as? String ?: ""
                val referencia = coordenadaMap?.get("referencia") as? String ?: ""
                tienda_patrocinada(
                    categoria_tienda = categoria,
                    id_tienda = idTienda,
                    img_tienda = imgPerfil,
                    nombre = nombre,
                    latitud = latitud,
                    longitud = longitud,
                    direccion = direccion,
                    referencia = referencia
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


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


    fun quitarTildes(texto: String): String {
        val normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    suspend fun obtener_subcategorias(categoria_selecionada: String): List<String> {

        val subcategorias = db.collection("Tiendas").document("categorias").collection("categorias")
            .document(categoria_selecionada).get().await()
        return if (subcategorias.exists()) {
             subcategorias.get("subcategorias") as? List<String> ?: emptyList()
        }else{
            emptyList()
        }
    }
}
package com.geinzz.geinzwork.model

import android.util.Log
import androidx.compose.material.AlertDialog
import com.geinzz.geinzwork.data.model.data_model_inicio_fr_
import com.geinzz.geinzwork.data.model.data_model_trabajador_scanner
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.utils.constantes.constantes.constantesSubcategoriaszonasTiendas
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

class repo_info {

    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_img_firestore(): List<data_model_inicio_fr_> {
        val lista = mutableListOf<data_model_inicio_fr_>()
        val db_img = db.collection(Variables.anuncios)
            .document(Variables.anunciosprimarios_cortos).collection(Variables.anuncios)
            .get().await()

        for (document in db_img) {
            val data = document.data
            val URLimg = data?.get(Variables.URLimg) as? String ?: ""
            val titulo = data?.get(Variables.titulo) as? String ?: ""
            val id = data?.get(Variables.id) as? String ?: ""
            val descripcion = data?.get(Variables.descripcion) as? String ?: ""
            val lista_data_model =
                data_model_inicio_fr_(
                    titulo = titulo,
                    descripcion = descripcion,
                    img = URLimg,
                    id = id
                )
            lista.add(lista_data_model)
        }
        return lista
    }

    suspend fun obtenerTrabajosCat(): List<dataClassCategoriasInicio> {
        val trabajos = mutableListOf<dataClassCategoriasInicio>()
        val collection = db.collection(Variables.categoriasDB).document(Variables.categoriasTrabajo)
            .get().await()
        if (collection.exists()) {
            val categorias = collection.get(Variables.categoriasDB) as? ArrayList<String>
            categorias?.let { cat ->
                for (categorias in cat) {
                    val url = obtenerImagenesCategorias(categorias)
                    trabajos.add(
                        dataClassCategoriasInicio(
                            cateogiria = categorias,
                            imgResId = url ?: ""
                        )
                    )
                }
            }
        }
        return trabajos
    }

    suspend fun obtenerImagenesCategorias(categoria: String): String? =
        suspendCancellableCoroutine { continuation ->
            constantesSubcategoriaszonasTiendas.obtenerImagenesCategorias(
                Variables.IMG_CategoriasGeneral,
                Variables.categroriasTrabajadores,
                categoria,
                onSuccess = { urlImg ->
                    continuation.resume(urlImg) {}
                },
                onFailure = { error ->
                    continuation.resume(null) {}
                }
            )
        }

    suspend fun obtener_res_scanner(result: String): List<data_model_trabajador_scanner> {
        val lista_datos_trabajador = mutableListOf<data_model_trabajador_scanner>()
        val trabajador = db.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(result).get().await()
        if (trabajador.exists()) {
            val data = trabajador.data
            val id = data?.get(Variables.id) as? String ?: ""
            if (id == result) {
                val nombreUser = data?.get(Variables.nombre) as? String ?: ""
                val nacionalidad = data?.get(Variables.nacionalidad) as? String ?: ""
                val categoria = data?.get(Variables.categoriaTrabajo) as? String ?: ""
                val img = data?.get(Variables.imagenPerfil) as? String ?: ""
                val data = data_model_trabajador_scanner(
                    nombre = nombreUser,
                    nacionalidad = nacionalidad,
                    categoria = categoria,
                    img = img,
                )
                lista_datos_trabajador.add(data)
            } else {
                Result.success(null)
            }
        } else {
            Result.success(null)
        }
        return lista_datos_trabajador

    }

    suspend fun obtener_mejores_trabajadores(filtrado_shader: String,tiempo:(Long)-> Unit): List<dataClassTrabajosd> {
        val tiempoInicio = System.currentTimeMillis()

        val lista_trabajadores = mutableListOf<dataClassTrabajosd>()
        val userCollections = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .get().await()

        val filtrados = userCollections.documents.mapNotNull { documentSnapshot ->
            val user = documentSnapshot.data ?: return@mapNotNull null
            val estrellas = user["estrellas"] as? String
            val estrellasInt = estrellas?.toIntOrNull()
            if (estrellasInt != null && estrellasInt > 40) {
                val usuario = dataClassTrabajosd(
                    id = user["id"] as? String,
                    apellido = user["apellido"] as? String,
                    c1 = user["caracteristica1"] as? String,
                    c2 = user["caracteristica2"] as? String,
                    c3 = user["caracteristica3"] as? String,
                    categoria = user["categoriaTrabajo"] as? String,
                    fecha_N = user["fechaNac"] as? String,
                    genero = user["genero"] as? String,
                    horarioam = user["horario1"] as? String,
                    horariopm = user["horario2"] as? String,
                    nacionalidad = user["nacionalidad"] as? String,
                    nombre = user["nombre"] as? String,
                    start = user["estrellas"] as? String,
                    tipoT = user["tipoTrabajo"] as? String,
                    localidad = user["localidad"] as? String,
                    codigo = user["codigo_pais"] as? String,
                    numero = user["numero"] as? String,
                    imgpriamria = user["imagenPerfil"] as? String,
                    activado = user["activado"] as? String,
                    edadActual = user["EdadActual"] as? String,
                    verificados = user["verificado"] as? Boolean
                )
                if (filtrado_shader == "General" || usuario.localidad == filtrado_shader) usuario else null
            } else {
                null
            }
        }

        lista_trabajadores.addAll(filtrados)

        val tiempoFin = System.currentTimeMillis()
        val duracion = tiempoFin - tiempoInicio
        tiempo(duracion)

        return lista_trabajadores
    }

    suspend fun obtner_trabajadores_por_categorias(
        filtro: String,
        categoria_filtado: String,tiempo: (Long) -> Unit
    ): List<dataClassTrabajosd> {
        val tiempoInicio = System.currentTimeMillis()
        val result = mutableListOf<dataClassTrabajosd>()
        val consulta = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .get().await()
        for (document in consulta.documents) {
            val userData = document.data ?: continue
            val usuario = dataClassTrabajosd(
                id = userData["id"] as? String,
                apellido = userData["apellido"] as? String,
                c1 = userData["caracteristica1"] as? String,
                c2 = userData["caracteristica2"] as? String,
                c3 = userData["caracteristica3"] as? String,
                categoria = userData["categoriaTrabajo"] as? String,
                fecha_N = userData["fechaNac"] as? String,
                genero = userData["genero"] as? String,
                horarioam = userData["horario1"] as? String,
                horariopm = userData["horario2"] as? String,
                nacionalidad = userData["nacionalidad"] as? String,
                nombre = userData["nombre"] as? String,
                start = userData["estrellas"] as? String,
                tipoT = userData["tipoTrabajo"] as? String,
                localidad = userData["localidad"] as? String,
                codigo = userData["codigo_pais"] as? String,
                numero = userData["numero"] as? String,
                imgpriamria = userData["imagenPerfil"] as? String,
                activado = userData["activado"] as? String,
                edadActual = userData["EdadActual"] as? String,
                verificados = userData["verificado"] as? Boolean
            )
            if ((filtro == "General" || usuario.localidad == filtro) && categoria_filtado == usuario.categoria) {
                Log.d("obtenos_el_fitraoda","$filtro $categoria_filtado==${usuario.categoria}")
                usuario
                result.add(usuario)
            } else null
            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio
            tiempo(duracion)
        }
        return result
    }
}
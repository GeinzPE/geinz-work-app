package com.geinzz.geinzwork.model

import Item
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.data_class_tienda_geinz
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz
import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz_activar_descativar_aloglia
import com.geinzz.geinzwork.data.model.dataclass_novedades.nuevas_teindas_dias
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.data.model.datos_cambiar_cat_sub
import com.geinzz.geinzwork.data.model.direccion_lugar
import com.geinzz.geinzwork.data.model.img_tienda
import com.geinzz.geinzwork.data.model.ingreso_date
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.GeofencingManager
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.get

class repo_agregar_datos(context: Context) {
    private val db: FirebaseFirestore

    val db2 = FirebaseFirestore.getInstance()

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

    fun agraegar_datos_db_2(data_class_tienda_geinz: data_class_tienda_geinz) {
        val hasmap = hashMapOf<String, Any>(
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
            "img_tienda" to img_tienda(),
            "fechas" to data_class_tienda_geinz.fechas,
            "timeSlamp" to data_class_tienda_geinz.timeSlamp,
            "puntos_tienda" to data_class_tienda_geinz.puntos_tienda
        )
        db2.collection("Tiendas").document(data_class_tienda_geinz.localida_tienda)
            .collection(data_class_tienda_geinz.localida_tienda)
            .document(data_class_tienda_geinz.id_tienda).set(hasmap).addOnSuccessListener { e ->
                Log.d("datos_agregados", "correcto")
                val datos = Item(
                    nombre = data_class_tienda_geinz.nombre_tienda,
                    lugar = data_class_tienda_geinz.localida_tienda,
                    id_tienda = data_class_tienda_geinz.id_tienda,
                    categoria = data_class_tienda_geinz.categoria_tienda,
                    img = data_class_tienda_geinz.lista_img.logo_tienda,
                    lista = data_class_tienda_geinz.subcategoria,
                    latitud = data_class_tienda_geinz.ubicacion.latitud,
                    longitud = data_class_tienda_geinz.ubicacion.longitud,
                    geohasing = data_class_tienda_geinz.geogash
                )
                agregar_datos_alglia(datos)

                val nuevas_teindas_dias = nuevas_teindas_dias(
                    categoria = data_class_tienda_geinz.categoria_tienda,
                    direccion = data_class_tienda_geinz.ubicacion.dirección,
                    horario_atencion = data_class_tienda_geinz.horario_atencion,
                    id_tienda = data_class_tienda_geinz.id_tienda,
                    logo_img = data_class_tienda_geinz.lista_img.logo_tienda,
                    nombre_tienda = data_class_tienda_geinz.nombre_tienda,
                    descripcion = data_class_tienda_geinz.descripcion,
                    lista_subcateogira = data_class_tienda_geinz.subcategoria,
                    localidad_tienda = data_class_tienda_geinz.localida_tienda, fecha = emptyMap()
                )
                agregar_por_14_dias_a_nuevos(nuevas_teindas_dias)
            }.addOnFailureListener {
                Log.d("datos_agregados", "malo")
            }
    }

    fun agregar_datos_alglia(data_class_tienda_geinz: Item) {
        val db = FirebaseFirestore.getInstance().collection("lugares")
            .document(data_class_tienda_geinz.id_tienda)
        val hasmap_ubicacion = hashMapOf<String, Any>(
            "latitud" to data_class_tienda_geinz.latitud,
            "longitud" to data_class_tienda_geinz.longitud
        )
        val hashMap = hashMapOf<String, Any>(
            "categoria" to data_class_tienda_geinz.categoria,
            "id_tienda" to data_class_tienda_geinz.id_tienda,
            "img" to "",
            "lugar" to data_class_tienda_geinz.lugar,
            "nombre" to data_class_tienda_geinz.nombre,
            "tag" to data_class_tienda_geinz.lista,
            "ubicacion" to hasmap_ubicacion,
            "geohash" to data_class_tienda_geinz.geohasing
        )
        db.set(hashMap).addOnSuccessListener { res ->
            Log.d("creado_correcto", "${data_class_tienda_geinz.id_tienda} creado correctamente :)")
        }.addOnFailureListener { e ->
            Log.d("error_subir_datos", "error")
        }

    }


    fun agregar_por_14_dias_a_nuevos(data_class_tienda_geinz: nuevas_teindas_dias) {

        val hoy = LocalDate.now()
        val fechaFin = hoy.plusDays(14)

        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val mapaFechas = mapOf(
            "fecha_inicio" to hoy.format(formato),
            "fecha_fin" to fechaFin.format(formato)
        )

        val hasmap = dataclass_novedades_geinz(
            categoria = data_class_tienda_geinz.categoria,
            direccion = data_class_tienda_geinz.direccion,
            horario_atencion = data_class_tienda_geinz.horario_atencion,
            id_tienda = data_class_tienda_geinz.id_tienda,
            logo_img = data_class_tienda_geinz.logo_img,
            nombre_tienda = data_class_tienda_geinz.nombre_tienda,
            descripcion = data_class_tienda_geinz.descripcion,
            lista_subcateogira = data_class_tienda_geinz.lista_subcateogira,
            localidad_tienda = data_class_tienda_geinz.localidad_tienda,
            fecha = mapaFechas
        )

        db.collection("Tiendas")
            .document(data_class_tienda_geinz.localidad_tienda)
            .collection("nuevos_lugares")
            .document(data_class_tienda_geinz.id_tienda)
            .set(hasmap)
            .addOnSuccessListener { Log.d("datos_agregados", "correcto") }
            .addOnFailureListener { Log.d("datos_agregados", "malo") }
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
        Log.d("docuemtos", "obtenmimosad")
        val listaDocs = mutableListOf<String>()           // IDs de documentos
        val listaSubcategorias = mutableListOf<List<String>>() // Lista de listas internas

        val ref = FirebaseFirestore.getInstance().collection("Tiendas")
            .document("categorias")
            .collection("categorias")
            .get()
            .await()

        for (doc in ref.documents) {
            Log.d("docuemtos", "${doc.id}")
            listaDocs.add(doc.id)

            // 2. Obtener la lista interna "subcategorias"
            val sub = doc.get("subcategorias") as? List<String> ?: emptyList()

            listaSubcategorias.add(sub)
        }

        return Pair(listaDocs, listaSubcategorias)
    }


    suspend fun obtener_cat_sub_tienda(
        id_tienda: String,
        localidad: String
    ): datos_cambiar_cat_sub {

        // --- 1. TIENDA PRINCIPAL ---
        val normal = db2.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .get()
            .await()

        // --- 2. NUEVOS LUGARES ---
        val nuevoslugares = db2.collection("Tiendas")
            .document(localidad)
            .collection("nuevos_lugares")
            .document(id_tienda)
            .get()
            .await()

        // --- 3. ALGO EN "lugares" (Algolia / indexación) ---
        val algoliaDoc = db2.collection("lugares")
            .document(id_tienda)
            .get()
            .await()

        // ==========================================
        // SI NO EXISTE EN TIENDAS (NORMAL)
        // ==========================================
        if (!normal.exists()) {
            return datos_cambiar_cat_sub(
                nombre_lugar = "",
                pertenerce_algolia = algoliaDoc.exists(),
                esta_nuevo = nuevoslugares.exists(),
                cat = "",
                lista_sub = emptyList()
            )
        }

        // ==========================================
        // SI EXISTE: OBTENER DATOS
        // ==========================================
        val data = normal.data ?: emptyMap<String, Any>()

        val nombre = data["nombre_tienda"] as? String ?: ""
        val categoria = data["categoria_tienda"] as? String ?: ""
        val subcategoria = data["subcategoria"] as? List<String> ?: emptyList()

        return datos_cambiar_cat_sub(
            nombre_lugar = nombre,
            pertenerce_algolia = algoliaDoc.exists(),
            esta_nuevo = nuevoslugares.exists(),
            cat = categoria,
            lista_sub = subcategoria
        )
    }


    suspend fun guardar_datos_tienda(
        algolia: Boolean,
        nuevo: Boolean,
        id_tienda: String,
        localidad: String,
        cat: String,
        subcat: List<String>,
        onResult: (Boolean) -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()

        Log.d("GUARDAR_TIENDA", "==== INICIO guardar_datos_tienda ====")
        Log.d("GUARDAR_TIENDA", "algolia = $algolia")
        Log.d("GUARDAR_TIENDA", "nuevo = $nuevo")
        Log.d("GUARDAR_TIENDA", "id_tienda = $id_tienda")
        Log.d("GUARDAR_TIENDA", "localidad = $localidad")
        Log.d("GUARDAR_TIENDA", "cat = $cat")
        Log.d("GUARDAR_TIENDA", "subcat = $subcat")

        val hashMapTiendas = mapOf(
            "categoria_tienda" to cat,
            "subcategoria" to subcat
        )

        val hashMapLugares = mapOf(
            "categoria" to cat,
            "tag" to subcat
        )

        Log.d("GUARDAR_TIENDA", "hashMapTiendas = $hashMapTiendas")
        Log.d("GUARDAR_TIENDA", "hashMapLugares = $hashMapLugares")

        try {

            // ============================
            //   UPDATE 1: Tiendas
            // ============================
            Log.d("GUARDAR_TIENDA", "↪ Actualizando TIENDAS...")
            db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .update(hashMapTiendas)
                .await()
            Log.d("GUARDAR_TIENDA", "✔ TIENDAS actualizado correctamente")


            // ============================
            //   UPDATE 2: Lugares
            // ============================
            if(!algolia){
            Log.d("GUARDAR_TIENDA", "↪ Actualizando LUGARES...")
            db.collection("lugares")
                .document(id_tienda)
                .update(hashMapLugares)
                .await()
            Log.d("GUARDAR_TIENDA", "✔ LUGARES actualizado correctamente")
            }


            // ============================
            //   ELIMINAR si NO pertenece a Algolia
            // ============================
            if (!algolia) {
                Log.d("GUARDAR_TIENDA", "↪ NO pertenece a algolia → eliminando de lugares")

                db.collection("lugares")
                    .document(id_tienda)
                    .delete()
                    .await()

                Log.d("GUARDAR_TIENDA", "🗑 Eliminado de lugares")
            } else {

                Log.d("GUARDAR_TIENDA", "↪ SI pertenece a algolia → obteniendo datos tienda")

                val datos_tienda_Espesifica = obtenerTiendaPorId(localidad, id_tienda)
                Log.d("GUARDAR_TIENDA", "Datos tienda obtenidos: $datos_tienda_Espesifica")

                val datos = Item(
                    datos_tienda_Espesifica.nombre_tienda,
                    datos_tienda_Espesifica.localidad_tienda,
                    datos_tienda_Espesifica.id_tienda,
                    datos_tienda_Espesifica.categoria,
                    datos_tienda_Espesifica.logo_img,
                    datos_tienda_Espesifica.lista_subcateogira,
                    datos_tienda_Espesifica.latitud,
                    datos_tienda_Espesifica.longitud,
                    datos_tienda_Espesifica.geohasing
                )

                Log.d("GUARDAR_TIENDA", "Datos enviados a algolia: $datos")
                agregar_datos_alglia(datos)
            }


            // ============================
            //   ELIMINAR si NO es tienda nueva
            // ============================
            if (!nuevo) {
                Log.d("GUARDAR_TIENDA", "↪ NO es tienda nueva → eliminando de nuevos_lugares")

                db.collection("Tiendas")
                    .document(localidad)
                    .collection("nuevos_lugares")
                    .document(id_tienda)
                    .delete()
                    .await()

                Log.d("GUARDAR_TIENDA", "🗑 Eliminado de nuevos_lugares")
            } else {

                Log.d("GUARDAR_TIENDA", "↪ SI es tienda nueva → obteniendo datos tienda")

                val datos_tienda_Espesifica = obtenerTiendaPorId(localidad, id_tienda)
                Log.d("GUARDAR_TIENDA", "Datos tienda nueva: $datos_tienda_Espesifica")

                val nuevas_teindas_dias = nuevas_teindas_dias(
                    categoria = datos_tienda_Espesifica.categoria,
                    direccion = datos_tienda_Espesifica.direccion,
                    horario_atencion = datos_tienda_Espesifica.horario_atencion,
                    id_tienda = datos_tienda_Espesifica.id_tienda,
                    logo_img = datos_tienda_Espesifica.logo_img,
                    nombre_tienda = datos_tienda_Espesifica.nombre_tienda,
                    descripcion = datos_tienda_Espesifica.descripcion,
                    lista_subcateogira = datos_tienda_Espesifica.lista_subcateogira,
                    localidad_tienda = datos_tienda_Espesifica.localidad_tienda,
                    fecha = emptyMap()
                )

                Log.d("GUARDAR_TIENDA", "Datos enviados a nuevos_lugares: $nuevas_teindas_dias")

                agregar_por_14_dias_a_nuevos(nuevas_teindas_dias)
            }

            Log.d("GUARDAR_TIENDA", "==== FIN SIN ERRORES ====")
            onResult(true)

        } catch (e: Exception) {
            Log.e("GUARDAR_TIENDA", "❌ ERROR EN guardar_datos_tienda", e)
            onResult(false)
        }
    }




    suspend fun obtenerTiendaPorId(
        localidad: String,
        id_tienda: String
    ): dataclass_novedades_geinz_activar_descativar_aloglia {

        Log.d("OBTENER_TIENDA", "==== INICIO obtenerTiendaPorId ====")
        Log.d("OBTENER_TIENDA", "localidad = $localidad")
        Log.d("OBTENER_TIENDA", "id_tienda = $id_tienda")

        val doc = db2.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .get()
            .await()

        if (!doc.exists()) {
            Log.w("OBTENER_TIENDA", "⚠ Documento NO existe")
            return dataclass_novedades_geinz_activar_descativar_aloglia(
                nombre_tienda = "",
                localidad_tienda = "",
                id_tienda = "",
                categoria = "",
                logo_img = "",
                direccion = "",
                lista_subcateogira = emptyList(),
                horario_atencion = HorarioAtencion_box(),
                descripcion = "",
                fecha = emptyMap(),
                geohasing = ""
            )
        }

        val data = doc.data
        if (data == null) {
            Log.w("OBTENER_TIENDA", "⚠ Documento existe pero NO tiene datos")
            return dataclass_novedades_geinz_activar_descativar_aloglia(
                nombre_tienda = "",
                localidad_tienda = "",
                id_tienda = "",
                categoria = "",
                logo_img = "",
                direccion = "",
                lista_subcateogira = emptyList(),
                horario_atencion = HorarioAtencion_box(),
                descripcion = "",
                fecha = emptyMap(),
                geohasing = ""
            )
        }

        Log.d("OBTENER_TIENDA", "Documento obtenido: $data")

        val map_img = data["img_tienda"] as? Map<String, Any> ?: emptyMap()
        val horarioMap = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
        val horario_atencion_Box = horarioMap.to_horario_atencion_box_dia()
        val ubicacion = data["ubicacion"] as? Map<String, Any> ?: emptyMap()
        val lat = (ubicacion["latitud"] as? Double) ?: 0.0
        val lon = (ubicacion["longitud"] as? Double) ?: 0.0

        Log.d("OBTENER_TIENDA", "map_img = $map_img")
        Log.d("OBTENER_TIENDA", "horarioMap = $horarioMap")
        Log.d("OBTENER_TIENDA", "horario_atencion_Box = $horario_atencion_Box")
        Log.d("OBTENER_TIENDA", "ubicacion = $ubicacion")
        Log.d("OBTENER_TIENDA", "latitud = $lat, longitud = $lon")

        val resultado = dataclass_novedades_geinz_activar_descativar_aloglia(
            nombre_tienda = data["nombre_tienda"] as? String ?: "",
            localidad_tienda = data["localidad"] as? String ?: "",
            id_tienda = data["id_tienda"] as? String ?: "",
            categoria = data["categoria_tienda"] as? String ?: "",
            logo_img = map_img["logo_tienda"] as? String ?: "",
            direccion = data["descripcion"] as? String ?: "",
            lista_subcateogira = data["subcategoria"] as? List<String> ?: emptyList(),
            horario_atencion = horario_atencion_Box,
            descripcion = data["descripcion"] as? String ?: "",
            fecha = data["fechas"] as? Map<String, Any> ?: emptyMap(),
            latitud = lat,
            longitud = lon,
            geohasing = data["geohash"] as? String ?: ""
        )

        Log.d("OBTENER_TIENDA", "Resultado final: $resultado")
        Log.d("OBTENER_TIENDA", "==== FIN obtenerTiendaPorId ====")

        return resultado
    }



    fun agregar_zonas() {

        Log.d("ZONAS", "🚀 Iniciando actualización de zonas")

        FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document("barranca")
            .collection("barranca")
            .get()
            .addOnSuccessListener { documentos ->

                Log.d("ZONAS", "📦 Total tiendas encontradas: ${documentos.size()}")

                for (documento in documentos) {

                    val nombreTienda =
                        documento.getString("nombre_tienda") ?: "SIN_NOMBRE"

                    val ubicacion =
                        documento.get("ubicacion") as? Map<*, *>

                    if (ubicacion == null) {
                        Log.w(
                            "ZONAS",
                            "⚠️ Tienda: $nombreTienda (${documento.id}) no tiene ubicación"
                        )
                        continue
                    }

                    val latitud =
                        (ubicacion["latitud"] as? Number)?.toDouble()

                    val longitud =
                        (ubicacion["longitud"] as? Number)?.toDouble()

                    if (latitud == null || longitud == null) {

                        Log.w(
                            "ZONAS",
                            """
                        ⚠️ Coordenadas faltantes
                        🏪 Tienda: $nombreTienda
                        🆔 ID: ${documento.id}
                        📍 Latitud: $latitud
                        📍 Longitud: $longitud
                        """.trimIndent()
                        )

                        continue
                    }

                    val zona = GeofencingManager.obtenerNombreZona(
                        latitud = latitud,
                        longitud = longitud
                    )

                    Log.d(
                        "ZONAS",
                        """
                    🏪 Tienda: $nombreTienda
                    🆔 ID: ${documento.id}
                    📍 Latitud: $latitud
                    📍 Longitud: $longitud
                    🗺️ Zona detectada: $zona
                    """.trimIndent()
                    )

                    documento.reference
                        .update("ubicacion.zona", zona)
                        .addOnSuccessListener {

                            Log.d(
                                "ZONAS",
                                """
                            ✅ Zona guardada correctamente
                            🏪 Tienda: $nombreTienda
                            🆔 ID: ${documento.id}
                            🗺️ Zona: $zona
                            """.trimIndent()
                            )
                        }
                        .addOnFailureListener { e ->

                            Log.e(
                                "ZONAS",
                                """
                            ❌ Error guardando zona
                            🏪 Tienda: $nombreTienda
                            🆔 ID: ${documento.id}
                            🗺️ Zona: $zona
                            """.trimIndent(),
                                e
                            )
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(
                    "ZONAS",
                    "❌ Error obteniendo tiendas",
                    e
                )
            }
    }



}
package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.DatosDemograficosUsuario
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.DatosResponse
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.TextoRequest
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_envidiadosbody_algolia
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.estadisticas_publiccaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.img_content
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.informacion_publcacion
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.data.model.mensaje_predeterminado
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.tiempoRestante
import com.geinzz.geinzwork.herramientas_geinz.constantes.construirPromptNLP
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_promp_NLP_depromo_y_oferta
import com.geinzz.geinzwork.retrofit.objet_retrofit
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.floor

class repo_promos_cercanas {
    val db = FirebaseFirestore.getInstance()
    private var randomInicioGlobal: Double = Math.random()

    suspend fun extraer_con_gemini(texto_user:String,categoria_select:String): String?{
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")
        val prompt= construir_promp_NLP_depromo_y_oferta(texto_user,categoria_select)
        val result = model.generateContent(prompt)
        return result.text
    }

    suspend fun obtener_respuesta_open_ia(texto:String): DatosResponse{
        return objet_retrofit.api.extraerDatos(TextoRequest(texto))
    }

    suspend fun send_get_resul_algoalia(data: DatosResponse): ResAlgoliaFiltrado {
        return objet_retrofit.api.Consultar_algolia(data)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_promos(
        tipo_seleccionado: String,
        localidad: String,
        tiendaSeleccionada1: String?,
    ): List<obj_completo> {

        Log.d("PROMOS_DEBUG", "▶ obtener_promos | tipo=$tipo_seleccionado | localidad=$localidad")

        return try {
            val snapshot = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .get()
                .await()

            Log.d("PROMOS_DEBUG", "📦 Total docs Firestore: ${snapshot.size()}")

            // 🔹 Agrupar promos por tienda
            val promosPorTienda = snapshot.documents.mapNotNull { doc ->
                val infoMap = doc.get("informacion") as? Map<*, *> ?: return@mapNotNull null
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()

                val idTienda = infoMap["id_tienda"] as? String ?: return@mapNotNull null
                val nombreTienda = infoMap["nombre_tienda"] as? String ?: ""
                val logo = imgMap["logo_img"] as? String ?: ""

                Triple(idTienda, nombreTienda, logo)
            }.groupBy { it.first }

            Log.d("PROMOS_DEBUG", "🏪 Tiendas encontradas: ${promosPorTienda.size}")

            val listaTiendasConMasDeUnaPromo = promosPorTienda
                .filter { it.value.size > 1 }
                .map { (idTienda, promos) ->
                    val p = promos.first()
                    tiendas_con_mas_de_una_promo(
                        id = idTienda,
                        nombre_tienda = p.second,
                        logo_img = p.third
                    )
                }

            snapshot.documents.mapNotNull { doc ->

                Log.d("PROMO_ITEM", "──────────────")
                Log.d("PROMO_ITEM", "📄 Promo ID: ${doc.id}")

                val estado = doc.getString("estado") ?: "expirado"
                Log.d("PROMO_ITEM", "estado=$estado")

                if (estado != "activo") {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA: no activa")
                    return@mapNotNull null
                }

                val infoMap = doc.get("informacion") as? Map<*, *> ?: run {
                    Log.d("PROMO_ITEM", "⛔ sin informacion")
                    return@mapNotNull null
                }

                val categoria_params = infoMap["categoria"] as? String ?: ""
                Log.d("PROMO_ITEM", "categoria=$categoria_params")

                if (tipo_seleccionado != "Todos" && categoria_params != tipo_seleccionado) {
                    Log.d(
                        "PROMO_ITEM",
                        "⛔ DESCARTADA: filtro categoria (seleccion=$tipo_seleccionado)"
                    )
                    return@mapNotNull null
                }

                val idTiendaInfo = infoMap["id_tienda"] as? String ?: ""
                if (tiendaSeleccionada1 != null && idTiendaInfo != tiendaSeleccionada1) {
                    Log.d(
                        "PROMO_ITEM",
                        "⛔ DESCARTADA: filtro por tienda (seleccionada=$tiendaSeleccionada1)"
                    )
                    return@mapNotNull null
                }

                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""
                Log.d("PROMO_ITEM", "tipo_hora_dias=$tipo_hora_dias")

                val datos_hora_fecha =
                    doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
                val horasMap = datos_hora_fecha["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
                val diasMap = datos_hora_fecha["dias"] as? Map<*, *> ?: emptyMap<String, Any>()
                val horario_de_publicacion =doc.get("horario_publicacion") as? String?:""
                val horarioActual = verificar_horairo_cel_para_publicidad().trim()
                val horarioPublicacion = horario_de_publicacion.trim()

                if (
                    horarioPublicacion.isNotEmpty() &&
                    horarioPublicacion != "todo_dia" &&
                    horarioPublicacion != horarioActual
                ) {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA por horario: $horarioPublicacion != $horarioActual")
                    return@mapNotNull null
                }



                val timestampFin = when (tipo_hora_dias) {
                    "horas" -> horasMap["timestamp_fin"] as? Timestamp
                    "dias" -> diasMap["timestamp_fin"] as? Timestamp
                    else -> null
                }

                Log.d("PROMO_ITEM", "timestampFin=$timestampFin")

                val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
                Log.d("PROMO_ITEM", "tiempoRestante=$tiempo")

                if (tiempo == "Expirado") {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA: expirada")
                    return@mapNotNull null
                }

                Log.d("PROMO_ITEM", "✅ PROMO VÁLIDA")
                val terminos_clave=doc.get("terminos_clave") as? List<String> ?: emptyList()
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val mensaje_predeterminado =
                    doc.get("mensaje_predeterminado") as? Map<*, *> ?: emptyMap<String, Any>()
                val compartir =
                    mensaje_predeterminado["compartir"] as? Map<*, *> ?: emptyMap<String, Any>()
                val whatsapp =
                    mensaje_predeterminado["whatsapp"] as? Map<*, *> ?: emptyMap<String, Any>()

                val comodidades_filtro =doc.get("comodidades") as? Map<String, Boolean> ?:emptyMap()
                val pagos =doc.get("pagos") as? Map<String, Boolean> ?:emptyMap()
                val rango_precio =doc.get("rango_establecido") as?String?:""
                val precio =doc.get("precio_publicacion") as?String?:""

                val msje_compartir = compartir["msje_predermindo"] as? String ?: ""
                val msje_whatsapp = whatsapp["msje_predermindo"] as? String ?: ""


                val informacion = informacion_publcacion(
                    descripcion = infoMap["descripcion"] as? String ?: "",
                    numero = infoMap["numero"] as? String ?: "",
                    titulo = infoMap["titulo"] as? String ?: "",
                    nombre_tienda = infoMap["nombre_tienda"] as? String ?: "",
                    id_promocion = doc.id,
                    id_tienda = infoMap["id_tienda"] as? String ?: "",
                    categoria = categoria_params,
                    compartir = infoMap["compartir"] as? Boolean ?: false,
                    contactar = infoMap["contactar"] as? Boolean ?: false,
                    msjes_predeteminados_generales = msjes_predeteminados_generales(
                        compartir = mensaje_predeterminado(
                            msje_predermindo = msje_compartir,
                            activo_o_no = true
                        ),
                        whatsapp = mensaje_predeterminado(
                            msje_predermindo = msje_whatsapp,
                            activo_o_no = true
                        )
                    )
                )

                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = tiempo,
                    estadisticas = estadisticas_publiccaciones(),
                    texto_msje_whatsapp = informacion.msjes_predeteminados_generales,
                    timestampFin ?: Timestamp.now(),
                    estado,comodidades_filtro,pagos,rango_precio,precio,terminos_clave
                )

                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_filtrado = listOfNotNull(informacion.categoria),
                    lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                )
            }.shuffled()

        } catch (e: Exception) {
            Log.e("ERROR_PROMO", "❌ Error al obtener promociones", e)
            emptyList()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_promos_paginado(
        tienda_seleccionada: String?,
        tipo_seleccionado: String,
        localidad: String,
        tiendaSeleccionada1: String?,
        ultimoDocumento: DocumentSnapshot?,  // 🔥 cursor de paginación
        limite: Int = 5
    ): Pair<List<obj_completo>, DocumentSnapshot?> {  // 🔥 devuelve también el nuevo cursor

        return try {

            if (tienda_seleccionada != null) {

                var query = db
                    .collection("Tiendas")
                    .document(localidad)
                    .collection("promos_ofertas")
                    .whereEqualTo("estado", "activo")
                    .whereEqualTo("informacion.id_tienda", tienda_seleccionada)
                    .orderBy("random")
                    .limit(limite.toLong())

                if (ultimoDocumento != null) {
                    query = query.startAfter(ultimoDocumento)
                }

                val snapshot = query.get().await()

                val documentosFinales = snapshot.documents

                if (documentosFinales.isEmpty()) {
                    return Pair(emptyList(), null)
                }

                // 👉 procesas documentosFinales igual que ya hace
            }
            // 🔹 Construir query con paginación
            var query = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .whereEqualTo("estado", "activo")
                .orderBy("random")
                .startAt(randomInicioGlobal)
                .limit((limite * 8).toLong()) // pedimos más para compensar los que se filtran por expiración/horario

            // 🔹 Si hay cursor → continuamos desde ahí
            if (ultimoDocumento != null) {
                query = query.startAfter(ultimoDocumento)
            }


            val snapshot = query.get().await()

            var documentosFinales = snapshot.documents

            if (snapshot.size() < limite) {

                val query2 = db
                    .collection("Tiendas")
                    .document(localidad)
                    .collection("promos_ofertas")
                    .whereEqualTo("estado", "activo")
                    .orderBy("random")
                    .endBefore(randomInicioGlobal)
                    .limit((limite * 5).toLong())

                val snapshot2 = query2.get().await()

                documentosFinales = (snapshot.documents + snapshot2.documents)
                    .distinctBy { it.id } // 🔥 evitar duplicados
            }

            Log.d("PROMOS_DEBUG", "📦 Docs finales: ${documentosFinales.size}")

            if (documentosFinales.isEmpty()) {
                return Pair(emptyList(), null)
            }

            // 🔹 Para las tiendas con más de una promo necesitamos un query separado (solo IDs)
            val snapshotCompleto = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .whereEqualTo("estado", "activo")
                .get()
                .await()

            val promosPorTienda = snapshotCompleto.documents.mapNotNull { doc ->
                val infoMap = doc.get("informacion") as? Map<*, *> ?: return@mapNotNull null
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val idTienda = infoMap["id_tienda"] as? String ?: return@mapNotNull null
                val nombreTienda = infoMap["nombre_tienda"] as? String ?: ""
                val logo = imgMap["logo_img"] as? String ?: ""
                Triple(idTienda, nombreTienda, logo)
            }.groupBy { it.first }

            val listaTiendasConMasDeUnaPromo = promosPorTienda
                .filter { it.value.size > 1 }
                .map { (idTienda, promos) ->
                    val p = promos.first()
                    tiendas_con_mas_de_una_promo(id = idTienda, nombre_tienda = p.second, logo_img = p.third)
                }

            var ultimoDocValido: DocumentSnapshot? = null
            val resultado = mutableListOf<obj_completo>()

            for (doc in documentosFinales) {
                val infoMap = doc.get("informacion") as? Map<*, *> ?: continue
                val categoria_params = infoMap["categoria"] as? String ?: ""

                if (tipo_seleccionado != "Todos" && categoria_params != tipo_seleccionado) continue

                val idTiendaInfo = infoMap["id_tienda"] as? String ?: ""
                if (tiendaSeleccionada1 != null && idTiendaInfo != tiendaSeleccionada1) continue

                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""
                val datos_hora_fecha = doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
                val horasMap = datos_hora_fecha["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
                val diasMap = datos_hora_fecha["dias"] as? Map<*, *> ?: emptyMap<String, Any>()

                val horario_de_publicacion = doc.get("horario_publicacion") as? String ?: ""
                val horarioActual = verificar_horairo_cel_para_publicidad().trim()
                if (horario_de_publicacion.isNotEmpty() &&
                    horario_de_publicacion != "todo_dia" &&
                    horario_de_publicacion.trim() != horarioActual) continue

                val timestampFin = when (tipo_hora_dias) {
                    "horas" -> horasMap["timestamp_fin"] as? Timestamp
                    "dias"  -> diasMap["timestamp_fin"] as? Timestamp
                    else    -> null
                }

                val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
                if (tiempo == "Expirado") continue

                // ✅ Doc válido → guardamos como nuevo cursor
                ultimoDocValido = doc

                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val mensaje_predeterminado = doc.get("mensaje_predeterminado") as? Map<*, *> ?: emptyMap<String, Any>()
                val compartir = mensaje_predeterminado["compartir"] as? Map<*, *> ?: emptyMap<String, Any>()
                val whatsapp = mensaje_predeterminado["whatsapp"] as? Map<*, *> ?: emptyMap<String, Any>()
                val terminos_clave = doc.get("terminos_clave") as? List<String> ?: emptyList()
                val comodidades_filtro = doc.get("comodidades") as? Map<String, Boolean> ?: emptyMap()
                val pagos = doc.get("pagos") as? Map<String, Boolean> ?: emptyMap()
                val rango_precio = doc.get("rango_establecido") as? String ?: ""
                val precio = doc.get("precio_publicacion") as? String ?: ""

                val informacion = informacion_publcacion(
                    descripcion = infoMap["descripcion"] as? String ?: "",
                    numero = infoMap["numero"] as? String ?: "",
                    titulo = infoMap["titulo"] as? String ?: "",
                    nombre_tienda = infoMap["nombre_tienda"] as? String ?: "",
                    id_promocion = doc.id,
                    id_tienda = idTiendaInfo,
                    categoria = categoria_params,
                    compartir = infoMap["compartir"] as? Boolean ?: false,
                    contactar = infoMap["contactar"] as? Boolean ?: false,
                    msjes_predeteminados_generales = msjes_predeteminados_generales(
                        compartir = mensaje_predeterminado(msje_predermindo = compartir["msje_predermindo"] as? String ?: "", activo_o_no = true),
                        whatsapp  = mensaje_predeterminado(msje_predermindo = whatsapp["msje_predermindo"] as? String ?: "", activo_o_no = true)
                    )
                )

                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = tiempo,
                    estadisticas = estadisticas_publiccaciones(),
                    texto_msje_whatsapp = informacion.msjes_predeteminados_generales,
                    timestampFin ?: Timestamp.now(),
                    doc.getString("estado") ?: "",
                    comodidades_filtro, pagos, rango_precio, precio, terminos_clave
                )

                resultado.add(
                    obj_completo(
                        dataclass_promociones_cerca_de_ti = promo,
                        lista_filtrado = listOfNotNull(categoria_params),
                        lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                    )
                )

                // 🔹 Ya tenemos suficientes válidos → parar
                if (resultado.size >= limite) break
            }

            Pair(resultado, ultimoDocValido)

        } catch (e: Exception) {
            Log.e("ERROR_PROMO", "❌ Error al obtener promociones", e)
            Pair(emptyList(), null)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_promos_paginado2(
        tienda_seleccionada: String?,
        tipo_seleccionado: String,
        localidad: String,
        ultimoDocumento: DocumentSnapshot?,
        limite: Int = 5
    ): Pair<List<obj_completo>, DocumentSnapshot?> {

        return try {

            val baseRef = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")

            // 🔹 Obtener tiendas con más de una promo
            val snapshotCompleto = baseRef
                .whereEqualTo("estado", "activo")
                .get()
                .await()

            val promosPorTienda = snapshotCompleto.documents.mapNotNull { doc ->
                val infoMap = doc.get("informacion") as? Map<*, *> ?: return@mapNotNull null
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()

                val idTienda = infoMap["id_tienda"] as? String ?: return@mapNotNull null
                val nombreTienda = infoMap["nombre_tienda"] as? String ?: ""
                val logo = imgMap["logo_img"] as? String ?: ""

                Triple(idTienda, nombreTienda, logo)
            }.groupBy { it.first }

            val listaTiendasConMasDeUnaPromo = promosPorTienda
                .filter { it.value.size > 1 }
                .map { (idTienda, promos) ->
                    val p = promos.first()
                    tiendas_con_mas_de_una_promo(
                        id = idTienda,
                        nombre_tienda = p.second,
                        logo_img = p.third
                    )
                }

            Log.d("PROMOS_DEBUG", "🏪 Tiendas con +1 promo: ${listaTiendasConMasDeUnaPromo.size}")

            // 🔥 CASO 1: TIENDA SELECCIONADA
            if (tienda_seleccionada != null) {

                Log.d("PROMOS_DEBUG", "🎯 MODO TIENDA: $tienda_seleccionada")

                var query = baseRef
                    .whereEqualTo("estado", "activo")
                    .whereEqualTo("informacion.id_tienda", tienda_seleccionada)
                    .orderBy("random")
                    .limit(limite.toLong())

                if (ultimoDocumento != null) {
                    query = query.startAfter(ultimoDocumento)
                }

                val snapshot = query.get().await()

                Log.d("PROMOS_DEBUG", "📦 Docs tienda: ${snapshot.size()}")

                if (snapshot.isEmpty) {
                    return Pair(emptyList(), null)
                }

                return procesarDocs(
                    snapshot.documents,
                    limite,
                    listaTiendasConMasDeUnaPromo
                )
            }

            // 🔥 CASO 2: RANDOM
            Log.d("PROMOS_DEBUG", "🎲 MODO RANDOM")

            var query = baseRef
                .whereEqualTo("estado", "activo")
                .orderBy("random")
                .startAt(randomInicioGlobal)
                .limit((limite * 8).toLong())

            if (ultimoDocumento != null) {
                query = query.startAfter(ultimoDocumento)
            }

            val snapshot = query.get().await()

            var documentosFinales = snapshot.documents

            // 🔁 fallback circular
            if (snapshot.size() < limite) {
                Log.d("PROMOS_DEBUG", "🔁 Activando fallback")

                val query2 = baseRef
                    .whereEqualTo("estado", "activo")
                    .orderBy("random")
                    .endBefore(randomInicioGlobal)
                    .limit((limite * 5).toLong())

                val snapshot2 = query2.get().await()

                documentosFinales = (snapshot.documents + snapshot2.documents)
                    .distinctBy { it.id }
            }

            Log.d("PROMOS_DEBUG", "📦 Docs finales: ${documentosFinales.size}")

            if (documentosFinales.isEmpty()) {
                return Pair(emptyList(), null)
            }

            return procesarDocs(
                documentosFinales,
                limite,
                listaTiendasConMasDeUnaPromo
            )

        } catch (e: Exception) {
            Log.e("ERROR_PROMO", "❌ Error al obtener promociones", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun procesarDocs(
        docs: List<DocumentSnapshot>,
        limite: Int,
        listaTiendasConMasDeUnaPromo: List<tiendas_con_mas_de_una_promo>
    ): Pair<List<obj_completo>, DocumentSnapshot?> {

        var ultimoDocValido: DocumentSnapshot? = null
        val resultado = mutableListOf<obj_completo>()

        for (doc in docs) {

            val infoMap = doc.get("informacion") as? Map<*, *> ?: continue
            val categoria = infoMap["categoria"] as? String ?: ""

            val tipoHora = doc.get("tipo_hora_dias") as? String ?: ""
            val datos = doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
            val horas = datos["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
            val dias = datos["dias"] as? Map<*, *> ?: emptyMap<String, Any>()

            val timestampFin = when (tipoHora) {
                "horas" -> horas["timestamp_fin"] as? Timestamp
                "dias"  -> dias["timestamp_fin"] as? Timestamp
                else    -> null
            }

            val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
            if (tiempo == "Expirado") continue

            ultimoDocValido = doc

            val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()

            val informacion = informacion_publcacion(
                descripcion = infoMap["descripcion"] as? String ?: "",
                numero = infoMap["numero"] as? String ?: "",
                titulo = infoMap["titulo"] as? String ?: "",
                nombre_tienda = infoMap["nombre_tienda"] as? String ?: "",
                id_promocion = doc.id,
                id_tienda = infoMap["id_tienda"] as? String ?: "",
                categoria = categoria,
                compartir = infoMap["compartir"] as? Boolean ?: false,
                contactar = infoMap["contactar"] as? Boolean ?: false,
                msjes_predeteminados_generales = msjes_predeteminados_generales()
            )

            val img = img_content(
                logo_img = imgMap["logo_img"] as? String ?: "",
                lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
            )

            val promo = dataclass_promociones_cerca_de_ti(
                informacion_publcacion = informacion,
                img = img,
                exclussivo = doc.getBoolean("exclusivo") ?: false,
                dias_restantes = tiempo,
                estadisticas = estadisticas_publiccaciones(),
                texto_msje_whatsapp = informacion.msjes_predeteminados_generales,
                timestampFin ?: Timestamp.now(),
                doc.getString("estado") ?: "",
                emptyMap(),
                emptyMap(),
                "",
                "",
                emptyList()
            )

            resultado.add(
                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_filtrado = listOf(categoria),
                    lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                )
            )

            if (resultado.size >= limite) break
        }

        Log.d("PROMOS_DEBUG", "✅ Resultado final: ${resultado.size}")

        return Pair(resultado, ultimoDocValido)
    }

    suspend fun obtener_categorias_firebase(): List<String> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document("categorias")
            .collection("categorias")
            .get()
            .await()

        return snapshot.documents.map { it.id }
    }

    suspend fun obtener_subcategorias(categoria: String): List<String> {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("Tiendas")
                .document("categorias")
                .collection("categorias")
                .document(categoria)
                .get()
                .await()

            snapshot.get("subcategorias") as? List<String> ?: emptyList()

        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtener_estadisticas(
        localidad: String,
        id_promo: String
    ): EstadisticasPromo {

        val statsRef = db
            .collection("Tiendas")
            .document(localidad)
            .collection("promos_ofertas")
            .document(id_promo)
            .collection("estadisticas")

        val compartidos = statsRef.document("compartidos")
            .get().await()
            .getLong("total")?.toInt() ?: 0

        val whatsapp = statsRef.document("whatsapp")
            .get().await()
            .getLong("total")?.toInt() ?: 0

        return EstadisticasPromo(
            vistas = 0,
            compartidos = compartidos,
            whatsapp = whatsapp
        )
    }


    fun verificar_horairo_cel():String{
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val horario = when (hora) {
            in 6..11 -> "Mañana"
            in 12..18 -> "Tarde"
            else -> "Noche"
        }
        return horario
    }

    fun verificar_horairo_cel_para_publicidad():String{
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val horario = when (hora) {
            in 6..11 -> "manana"
            in 12..18 -> "tarde"
            else -> "noche"
        }
        return horario
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun agregar_contador_estadisticas_publicacion(
        tipoEvento: String,
        id_promo: String,
        localidad: String,
        idUser: String
    ) {
        val db = FirebaseFirestore.getInstance()

        val estadisticaBase = db
            .collection("Tiendas").document(localidad)
            .collection("promos_ofertas").document(id_promo)
            .collection("estadisticas")
            .document(tipoEvento)

        val hoy = LocalDate.now().toString()

        // 1️⃣ TOTAL (todos los eventos)
        incrementar(estadisticaBase)

        // 2️⃣ POR DÍA (todos los eventos)
        incrementar(
            estadisticaBase
                .collection("por_dia")
                .document(hoy)
        )

        // 3️⃣ ALCANCE (solo vistas)
        if (tipoEvento == "vista") {
            registrarAlcanceUnico(
                estadisticaBase = estadisticaBase,
                idUser = idUser
            )
        }

        // 4️⃣ DEMOGRAFÍA (todo menos compartir)
        if (tipoEvento != "compartir") {
            obtenerDatosUsuario(idUser) { datos ->
                if (datos == null) return@obtenerDatosUsuario

                if (datos.localidad.isNotEmpty()) {
                    incrementar(
                        estadisticaBase
                            .collection("localidad")
                            .document(datos.localidad)
                    )
                }

                if (datos.genero.isNotEmpty()) {
                    incrementar(
                        estadisticaBase
                            .collection("genero")
                            .document(datos.genero.lowercase())
                    )
                }

                incrementar(
                    estadisticaBase
                        .collection("edad")
                        .document(obtenerRangoEdad(datos.edad))
                )
            }
        }
    }


    fun registrarAlcanceUnico(
        estadisticaBase: DocumentReference,
        idUser: String
    ) {
        val alcanceUserRef = estadisticaBase
            .collection("alcance_users")
            .document(idUser)

        alcanceUserRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                alcanceUserRef.set(
                    mapOf("ts" to FieldValue.serverTimestamp())
                )

                incrementar(
                    estadisticaBase
                        .collection("alcance")
                        .document("total")
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerDatosUsuario(
        idUser: String,
        onResult: (DatosDemograficosUsuario?) -> Unit
    ) {
        db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("users")
            .document(idUser)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val data = snapshot.data ?: run {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val fechaNacString = data["fecha_nac"] as? String

                val edad = try {
                    fechaNacString?.let {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val fechaNac = LocalDate.parse(it, formatter)
                        Period.between(fechaNac, LocalDate.now()).years
                    } ?: 0
                } catch (e: Exception) {
                    0 // 🔒 nunca crashea
                }

                onResult(
                    DatosDemograficosUsuario(
                        localidad = data["localida"] as? String ?: "",
                        nacionalidad = data["nacionalidad_nacimiento"] as? String ?: "",
                        genero = data["genero"] as? String ?: "",
                        edad = edad
                    )
                )
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun incrementar(ref: DocumentReference) {
        ref.update("total", FieldValue.increment(1))
            .addOnFailureListener {
                ref.set(mapOf("total" to 1))
            }
    }


    fun obtenerRangoEdad(edad: Int): String {
        return when (edad) {
            in 18..25 -> "18-25"
            in 26..35 -> "26-35"
            in 36..45 -> "36-45"
            else -> "otro"
        }
    }

}
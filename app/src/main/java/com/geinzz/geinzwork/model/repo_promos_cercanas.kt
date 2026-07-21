package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.DatosDemograficosUsuario
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.DatosResponse
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.IdScore
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado_manual
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.TextoRequest
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_envidiadosbody_algolia
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_para_filtrado_manual
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.estadisticas_publiccaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.filtrado_feed_promociones
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
import com.google.firebase.firestore.FieldPath
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


    suspend fun obtener_respuesta_open_ia(texto:String): DatosResponse{
        return objet_retrofit.api.extraerDatos(TextoRequest(texto))
    }

    suspend fun send_get_resul_algoalia(data: DatosResponse): ResAlgoliaFiltrado {
        return objet_retrofit.api.Consultar_algolia(data)
    }

    suspend fun send_params_filter_manual(data:datos_para_filtrado_manual): ResAlgoliaFiltrado {
        return objet_retrofit.api.construir_filtrado_manual(data)
    }


    suspend fun tag_existe_en_promos_activas(
        tag: String,
        localidad: String,
        rango: String? = null,
        pagos: List<String> = emptyList(),
        comodidades: List<String> = emptyList()
    ): Boolean {
        return try {
            val snapshot = db.collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .whereEqualTo("estado", "activo")
                .whereArrayContains("terminos_clave", tag)
                .get()
                .await()

            if (snapshot.isEmpty) return false

            // 🔥 de las promos que sí tienen el tag, verificamos si ALGUNA
            // también cumple rango/pagos/comodidades (si el usuario los seleccionó)
            snapshot.documents.any { doc ->
                val cumpleRango = rango.isNullOrEmpty() || doc.getString("rango_establecido") == rango

                val docPagos = doc.get("pagos") as? List<*> ?: emptyList<Any>()
                val cumplePago = pagos.isEmpty() || pagos.any { docPagos.contains(it) }

                val docComodidades = doc.get("comodidades") as? List<*> ?: emptyList<Any>()
                val cumpleComodidad = comodidades.isEmpty() || comodidades.any { docComodidades.contains(it) }

                cumpleRango && cumplePago && cumpleComodidad
            }
        } catch (e: Exception) {
            Log.e("CACHE_FILTRADO", "❌ Error verificando tag: ${e.message}")
            true // fail-safe: ante error, no borrar
        }
    }
    suspend fun eliminar_tag_obsoleto_cache_filtrado(
        categoria: String,
        tag: String,
        localidad: String = "barranca"
    ) {
        try {
            db.collection("Tiendas")
                .document(localidad)
                .collection("cache_filtrado")
                .document("filtrado")
                .update(categoria, FieldValue.arrayRemove(tag))
                .await()
            Log.d("CACHE_FILTRADO", "🗑️ Tag '$tag' eliminado de '$categoria'")
        } catch (e: Exception) {
            Log.e("CACHE_FILTRADO", "❌ Error eliminando tag: ${e.message}")
        }
    }

    suspend fun obtener_promos(
        tipo_seleccionado: String,
        localidad: String,
        tiendaSeleccionada1: String,
    ): List<obj_completo> {

        Log.d("PROMOS_DEBUG", "════════════════════════════════════")
        Log.d("PROMOS_DEBUG", "▶ INICIO obtener_promos")
        Log.d("PROMOS_DEBUG", "tipo_seleccionado=$tipo_seleccionado")
        Log.d("PROMOS_DEBUG", "localidad=$localidad")
        Log.d("PROMOS_DEBUG", "tiendaSeleccionada1=$tiendaSeleccionada1")

        return try {

            Log.d("PROMOS_DEBUG", "📡 Consultando Firestore...")

            val snapshot = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .get()
                .await()

            Log.d("PROMOS_DEBUG", "📦 Total docs Firestore: ${snapshot.size()}")

            val promosPorTienda = snapshot.documents.mapNotNull { doc ->

                Log.d("PROMOS_DEBUG", "🔍 Analizando tienda promo doc=${doc.id}")

                val infoMap = doc.get("informacion") as? Map<*, *> ?: run {
                    Log.d("PROMOS_DEBUG", "⛔ infoMap null en ${doc.id}")
                    return@mapNotNull null
                }

                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()

                val idTienda = infoMap["id_tienda"] as? String ?: run {
                    Log.d("PROMOS_DEBUG", "⛔ id_tienda null en ${doc.id}")
                    return@mapNotNull null
                }

                val nombreTienda = infoMap["nombre_tienda"] as? String ?: ""
                val logo = imgMap["logo_img"] as? String ?: ""
                val categoria = infoMap["categoria"] as? String ?: ""
                Log.d(
                    "PROMOS_DEBUG",
                    "🏪 Tienda detectada -> id=$idTienda | nombre=$nombreTienda"
                )

                listOf(idTienda, nombreTienda, logo, categoria)

            }.groupBy { it[0] }


            Log.d("PROMOS_DEBUG", "🏪 Total tiendas agrupadas: ${promosPorTienda.size}")

            val listaTiendasConMasDeUnaPromo = promosPorTienda
                .filter {
                    it.value.isNotEmpty()
                }
                .map { (idTienda, promos) ->

                    Log.d(
                        "PROMOS_DEBUG",
                        "✅ Tienda con múltiples promos -> $idTienda"
                    )

                    val p = promos.first()

                    tiendas_con_mas_de_una_promo(
                        id = idTienda,
                        nombre_tienda = p[1] as String,
                        logo_img = p[2] as String,
                        categoira = p[3] as String   // ← categoría real
                    )
                }

            Log.d(
                "PROMOS_DEBUG",
                "📋 listaTiendasConMasDeUnaPromo size=${listaTiendasConMasDeUnaPromo.size}"
            )

            val resultado = snapshot.documents.mapNotNull { doc ->

                Log.d("PROMO_ITEM", "════════════════════════════")
                Log.d("PROMO_ITEM", "📄 Promo ID: ${doc.id}")

                val estado = doc.getString("estado") ?: "expirado"

                Log.d("PROMO_ITEM", "estado=$estado")

                if (estado != "activo") {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA: estado no activo")
                    return@mapNotNull null
                }

                val infoMap = doc.get("informacion") as? Map<*, *> ?: run {
                    Log.d("PROMO_ITEM", "⛔ informacion null")
                    return@mapNotNull null
                }

                Log.d("PROMO_ITEM", "ℹ️ informacion encontrada")

                // 🔹 Filtro por tienda
                val idTiendaInfo = infoMap["id_tienda"] as? String ?: ""

                Log.d(
                    "PROMO_ITEM",
                    "🏪 idTiendaInfo=$idTiendaInfo | tiendaSeleccionada1=$tiendaSeleccionada1"
                )

                if (idTiendaInfo != tiendaSeleccionada1) {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA por tienda")
                    return@mapNotNull null
                }

                val categoria_params = infoMap["categoria"] as? String ?: ""

                Log.d(
                    "PROMO_ITEM",
                    "📂 categoria promo=$categoria_params | filtro=$tipo_seleccionado"
                )

                if (tipo_seleccionado != "Todos" && categoria_params != tipo_seleccionado) {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA por categoría")
                    return@mapNotNull null
                }

                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""

                Log.d("PROMO_ITEM", "🕒 tipo_hora_dias=$tipo_hora_dias")

                val datos_hora_fecha =
                    doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()

                val timestampFin = datos_hora_fecha.get("timestamp_fin") as? Timestamp

                Log.d("PROMO_ITEM", "📅 timestampFin=$timestampFin")

                val horario_de_publicacion =
                    doc.get("horario_publicacion") as? String ?: ""

                val horarioActual = verificar_horairo_cel_para_publicidad().trim()
                val horarioPublicacion = horario_de_publicacion.trim()

                Log.d(
                    "PROMO_ITEM",
                    "🕓 horarioPublicacion=$horarioPublicacion | horarioActual=$horarioActual"
                )

                if (
                    horarioPublicacion.isNotEmpty() &&
                    horarioPublicacion != "todo_dia" &&
                    horarioPublicacion != horarioActual
                ) {

                    Log.d(
                        "PROMO_ITEM",
                        "⛔ DESCARTADA por horario: $horarioPublicacion != $horarioActual"
                    )

                    return@mapNotNull null
                }

                val tiempo = timestampFin?.let {

                    Log.d("PROMO_ITEM", "⏳ Calculando tiempo restante...")

                    tiempoRestante(it)

                } ?: "Expirado"

                Log.d("PROMO_ITEM", "⌛ tiempoRestante=$tiempo")

                if (tiempo == "Expirado") {
                    Log.d("PROMO_ITEM", "⛔ DESCARTADA: promo expirada")
                    return@mapNotNull null
                }

                Log.d("PROMO_ITEM", "✅ PROMO VÁLIDA")

                val terminos_clave =
                    doc.get("terminos_clave") as? List<String> ?: emptyList()

                Log.d(
                    "PROMO_ITEM",
                    "🏷️ terminos_clave size=${terminos_clave.size}"
                )

                val imgMap =
                    doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()

                Log.d(
                    "PROMO_ITEM",
                    "🖼️ imgMap keys=${imgMap.keys}"
                )

                val mensaje_predeterminado =
                    doc.get("mensaje_predeterminado") as? Map<*, *> ?: emptyMap<String, Any>()

                val compartir =
                    mensaje_predeterminado["compartir"] as? Map<*, *> ?: emptyMap<String, Any>()

                val whatsapp =
                    mensaje_predeterminado["whatsapp"] as? Map<*, *> ?: emptyMap<String, Any>()

                val comodidades_filtro =
                    doc.get("comodidades") as? List<String> ?: emptyList()

                val pagos =
                    doc.get("pagos") as? List<String> ?: emptyList()

                val rango_precio =
                    doc.get("rango_establecido") as? String ?: ""

                val precio =
                    doc.get("precio_publicacion") as? String ?: ""

                Log.d(
                    "PROMO_ITEM",
                    "💰 precio=$precio | rango_precio=$rango_precio"
                )

                Log.d(
                    "PROMO_ITEM",
                    "💳 pagos=${pagos.joinToString()}"
                )

                Log.d(
                    "PROMO_ITEM",
                    "🛋️ comodidades=${comodidades_filtro.joinToString()}"
                )

                val msje_compartir =
                    compartir["msje_predermindo"] as? String ?: ""

                val msje_whatsapp =
                    whatsapp["msje_predermindo"] as? String ?: ""

                Log.d(
                    "PROMO_ITEM",
                    "📨 msje_compartir length=${msje_compartir}"
                )

                Log.d(
                    "PROMO_ITEM",
                    "📲 msje_whatsapp length=${msje_whatsapp}"
                )

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

                Log.d(
                    "PROMO_ITEM",
                    "📝 informacion creada -> titulo=${informacion.titulo}"
                )

                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                Log.d(
                    "PROMO_ITEM",
                    "🖼️ lista_img size=${img.lista_img.size}"
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = tiempo,
                    estadisticas = estadisticas_publiccaciones(),
                    texto_msje_whatsapp = informacion.msjes_predeteminados_generales,
                    timestampFin ?: Timestamp.now(),
                    estado,
                    comodidades_filtro,
                    pagos,
                    rango_precio,
                    precio,
                    terminos_clave
                )

                Log.d(
                    "PROMO_ITEM",
                    "🎉 Promo creada correctamente -> ${informacion.id_promocion}"
                )

                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                )
            }.shuffled()

            Log.d(
                "PROMOS_DEBUG",
                "✅ TOTAL PROMOS RETORNADAS: ${resultado.size}"
            )

            resultado

        } catch (e: Exception) {

            Log.e("ERROR_PROMO", "❌ Error al obtener promociones", e)

            emptyList()
        }
    }

    suspend fun obtener_todas_promos_de_tienda(
        tienda_seleccionada: String,
        localidad: String
    ): Triple<List<obj_completo>, List<String>, List<String>> {
        return try {
            // 🔥 Consultas en paralelo
            val snapshotPromosDeferred = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .whereEqualTo("estado", "activo")
                .whereEqualTo("informacion.id_tienda", tienda_seleccionada)
                .get()

            val snapshotTiendaDeferred = db
                .collection("Tiendas")
                .document(localidad)
                .collection("barranca")
                .document(tienda_seleccionada)
                .get()

            val snapshotPromos = snapshotPromosDeferred.await()
            val docTienda = snapshotTiendaDeferred.await()

            val (resultado, _) = procesarDocs(
                docs = snapshotPromos.documents,
                limite = snapshotPromos.size(),
                listaTiendasConMasDeUnaPromo = emptyList()
            )

            // ── PAGOS desde doc real de la tienda ────────────────────
            val metodos_pago_map = docTienda.get("metodos_pago") as? Map<*, *> ?: emptyMap<String, Any>()
            val pagos = metodos_pago_map
                .filterValues { valor ->
                    val mapa = valor as? Map<*, *> ?: return@filterValues false
                    mapa["enable"] as? Boolean == true
                }
                .keys
                .filterIsInstance<String>()
                .flatMap { key ->
                    when (key) {
                        "visa_mastercard" -> listOf("visa", "mastercard")
                        else -> listOf(key)
                    }
                }

            // ── COMODIDADES desde doc real de la tienda ──────────────
            val servicios_list = docTienda.get("servicios_comodidades") as? List<*> ?: emptyList<Any>()
            val comodidades = servicios_list
                .filterIsInstance<Map<*, *>>()
                .flatMap { mapa ->
                    mapa.entries
                        .filter { it.value as? Boolean == true }
                        .map { it.key.toString().lowercase().trim().replace(" ", "_") }
                }

            Log.d("TIENDA_DATOS", "✅ pagos: $pagos")
            Log.d("TIENDA_DATOS", "✅ comodidades: $comodidades")

            Triple(resultado, pagos, comodidades)

        } catch (e: Exception) {
            Log.e("TIENDA_PROMOS", "❌ Error: $e")
            Triple(emptyList(), emptyList(), emptyList())
        }
    }
    suspend fun obtener_promos_paginado2(
        es_primera_carga: Boolean,
        tienda_seleccionada: String?,
        localidad: String,
        ultimoDocumento: DocumentSnapshot?,
        limite: Int = 5
    ): Pair<List<obj_completo>, DocumentSnapshot?> {

        return try {

            val baseRef = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")

            val listaTiendasConMasDeUnaPromo =
                if (es_primera_carga) {
                    val snapshotCompleto = baseRef
                        .whereEqualTo("estado", "activo")
                        .get()
                        .await()

                    snapshotCompleto.documents
                        .mapNotNull { doc ->
                            val infoMap = doc.get("informacion") as? Map<*, *> ?: return@mapNotNull null
                            val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                            val idTienda = infoMap["id_tienda"] as? String ?: return@mapNotNull null

                            // ✅ FIX: verificar que la promo NO esté expirada
                            val datos = doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
                            val timestampFin = datos["timestamp_fin"] as? Timestamp
                            val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
                            if (tiempo == "Expirado") return@mapNotNull null  // ← esta línea es la clave

                            val nombreTienda = infoMap["nombre_tienda"] as? String ?: ""
                            val logo = imgMap["logo_img"] as? String ?: ""
                            val categoria = infoMap["categoria"] as? String ?: ""
                            listOf(idTienda, nombreTienda, logo, categoria)
                        }
                        .groupBy { it[0] }
                        .filter { it.value.isNotEmpty() }
                        .map { (idTienda, promos) ->
                            val p = promos.random()
                            tiendas_con_mas_de_una_promo(
                                id = idTienda,
                                nombre_tienda = p[1] as String,
                                logo_img = p[2] as String,
                                categoira = p[3] as String
                            )
                        }
                        .shuffled()

                } else {
                    emptyList()
                }
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

            val datos = doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
            val pagos =doc.get("pagos")as? List<String>?: emptyList()
            val timestampFin=datos.get("timestamp_fin") as? Timestamp

            val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
            if (tiempo == "Expirado") continue

            ultimoDocValido = doc

            val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
            val mensaje_predeterminado=doc.get("mensaje_predeterminado") as? Map<*,*>?:emptyMap<String, Any>()

            val compartir=mensaje_predeterminado.get("compartir") as? Map<*,*>?:emptyMap<String, Any>()

            val whatsapp=mensaje_predeterminado.get("whatsapp") as? Map<*,*>?:emptyMap<String, Any>()
            val terminos_clave = doc.get("terminos_clave") as? List<String> ?: emptyList()

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

                msjes_predeteminados_generales = msjes_predeteminados_generales(
                    compartir = mensaje_predeterminado(
                        msje_predermindo = compartir.get("msje_predermindo") as? String?:"",
                        activo_o_no = compartir.get("activo_o_no") as? Boolean?:false
                    ),
                    whatsapp = mensaje_predeterminado(
                        msje_predermindo = whatsapp.get("msje_predermindo") as? String?:"",
                        activo_o_no =whatsapp.get("activo_o_no") as? Boolean?:false
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
                fecha_fin = timestampFin ?: Timestamp.now(),
                estado_publicacion = doc.getString("estado") ?: "",
                comodidades = emptyList(),
                pagos = pagos,
                rango = doc.getString("rango_establecido") ?: "",
                precio = doc.getString("precio_publicacion") ?: "",
                terminos_clave = terminos_clave
            )

            resultado.add(
                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                )
            )

            if (resultado.size >= limite) break
        }

        Log.d("PROMOS_DEBUG", "✅ Resultado final: ${resultado.size}")

        return Pair(resultado, ultimoDocValido)
    }

    fun procesarDocsSoloPromos(
        docs: List<DocumentSnapshot>,
        limite: Int
    ): Pair<List<obj_completo>, DocumentSnapshot?> {

        var ultimoDocValido: DocumentSnapshot? = null
        val resultado = mutableListOf<obj_completo>()

        for (doc in docs) {

            val infoMap = doc.get("informacion") as? Map<*, *> ?: continue
            val categoria = infoMap["categoria"] as? String ?: ""

            val datos = doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
            val timestampFin=datos.get("timestamp_fin") as? Timestamp

            val tiempo = timestampFin?.let { tiempoRestante(it) } ?: "Expirado"
            if (tiempo == "Expirado") continue

            ultimoDocValido = doc

            val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
            val pagos =doc.get("pagos")as? List<String>?: emptyList()

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
                fecha_fin = timestampFin ?: Timestamp.now(),
                estado_publicacion = doc.getString("estado") ?: "",
                comodidades = emptyList(),
                pagos =pagos,
                rango = "",
                precio = "",
                terminos_clave = emptyList()
            )

            resultado.add(
                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_tiendas_con_mas_promo = emptyList() // 🔥 vacío
                )
            )

            if (resultado.size >= limite) break
        }

        return Pair(resultado, ultimoDocValido)
    }

    suspend fun obtenerPromosPorIdsProcesadas_123(
        ids: List<String>,
        limite: Int,
        localidad: String = "barranca"  // ← agrega esto
    ): List<obj_completo> {

        val snapshot = db.collection("Tiendas")
            .document(localidad)  // ← usa localidad en vez de hardcoded
            .collection("promos_ofertas")
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .await()

        val (procesadas, _) = procesarDocsSoloPromos(
            docs = snapshot.documents,
            limite = limite
        )

        return procesadas
    }

    suspend fun obtenerPromosPorIdsProcesadas(
        ids: List<String>,
        limite: Int
    ): List<obj_completo> {

        val snapshot = db.collection("Tiendas").document("barranca").collection("promos_ofertas")
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .await()

        val (procesadas, _) = procesarDocsSoloPromos(
            docs = snapshot.documents,
            limite = limite
        )

        return procesadas
    }

    suspend fun obtener_categorias_firebase(): List<filtrado_feed_promociones> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document("barranca")
            .collection("cache_filtrado")
            .document("filtrado")
            .get()
            .await()

        val data = snapshot.data ?: return emptyList()

        return data.map { (categoria, lista) ->

            val subcategorias = (lista as? List<*>)
                ?.filterIsInstance<String>()
                ?.map { it.lowercase().trim() }
                ?.toSet() // 🔥 elimina duplicados
                ?.toList()
                ?.sorted() // opcional (ordenado)
                ?: emptyList()

            filtrado_feed_promociones(
                categoria = categoria,
                subcategoria = subcategorias
            )
        }
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

    fun verificar_horairo_cel_para_publicidad():String{
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val horario = when (hora) {
            in 6..11 -> "manana"
            in 12..18 -> "tarde"
            else -> "noche"
        }
        return horario
    }

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
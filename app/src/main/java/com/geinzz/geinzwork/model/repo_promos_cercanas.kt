package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.DatosDemograficosUsuario
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.estadisticas_publiccaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.img_content
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.informacion_publcacion
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.data.model.mensaje_predeterminado
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.tiempoRestante
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

class repo_promos_cercanas {
    val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_promos(
        tipo_seleccionado: String,
        localidad: String
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

                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""
                Log.d("PROMO_ITEM", "tipo_hora_dias=$tipo_hora_dias")

                val datos_hora_fecha =
                    doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
                val horasMap = datos_hora_fecha["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
                val diasMap = datos_hora_fecha["dias"] as? Map<*, *> ?: emptyMap<String, Any>()

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

                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val mensaje_predeterminado =
                    doc.get("mensaje_predeterminado") as? Map<*, *> ?: emptyMap<String, Any>()
                val compartir =
                    mensaje_predeterminado["compartir"] as? Map<*, *> ?: emptyMap<String, Any>()
                val whatsapp =
                    mensaje_predeterminado["whatsapp"] as? Map<*, *> ?: emptyMap<String, Any>()

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
                    estado
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
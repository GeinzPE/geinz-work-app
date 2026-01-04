package com.geinzz.geinzwork.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.identity.util.UUID
import com.geinzz.geinzwork.data.model.agregar_promociones
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.datos_publicaciones_realizadas
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.fechas_promociones
import com.geinzz.geinzwork.data.model.obj_contador_notificaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.normalizar
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenWebPSinRecorte
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaConDias
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.timeStampNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoservicios_comodidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_img_usert
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_ubicacion_container
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

@RequiresApi(Build.VERSION_CODES.O)

class repo_eres_socio {
    private val db = FirebaseFirestore.getInstance()

    fun escuchar_datos_tienda(
        localidad_tienda: String,
        id_tienda: String,
        resultado: (datos_tienda) -> Unit,
        error: (Exception) -> Unit,
    ): ListenerRegistration {

        val ref = db.collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        return ref.addSnapshotListener { snapshot, e ->
            if (e != null) {
                error(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {

                val data = snapshot.data ?: emptyMap<String, Any>()

                val nombre_tienda = data["nombre_tienda"] as? String ?: ""
                val img_tienda = data["img_tienda"] as? Map<String, Any> ?: emptyMap()

                val horario_atencion = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
                val horarioMap = horario_atencion.to_horario_atencion_box_dia()

                val localidadTienda = data["localidad"] as? String ?: ""

                val fechas = data["fechas"] as? Map<String, Any> ?: emptyMap()
                val fecha_ingreso = fechas["fecha_ingreso"] as? String ?: ""
                val fecha_termino = fechas["fecha_fin"] as? String ?: ""

                val descripcion = data["descripcion"] as? String ?: ""
                val propietario_id = data["propietario_id"] as? List<String> ?: emptyList()
                val saldo_tienda = data["puntos_tienda"] as? Number ?: 0
                val metodo_pago = data?.get("metodos_pago") as? Map<String, Any> ?: emptyMap()
                val metodos_contacto =
                    data?.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
                val metodos_servicios =
                    data?.get("servicios_comodidades") as? List<Map<String, Any>>
                val aforo_maximo = data?.get("aforo_max") as? Number ?: 0
                val categoria_teinda = data?.get("categoria_tienda") as? String ?: ""
                val subcategoria = data?.get("subcategoria") as? List<String> ?: emptyList()

                val servicios_comodidades = metodos_servicios.toMetodoservicios_comodidades()
                val contacto_obs = metodos_contacto.toMetodoContacto()
                val metodo_pago_tienda = metodo_pago.to_metodo_pago()
                val img_generales = img_tienda.to_img_usert()

                val ubicacion = data?.get("ubicacion") as? Map<String, Any>
                val ubi_container = ubicacion.to_ubicacion_container()


                // 🔥 ESCUCHAR ESTADISTICAS EN TIEMPO REAL
                ref.collection("estadisticas")
                    .addSnapshotListener { statsSnap, statsError ->

                        if (statsError != null) {
                            error(statsError)
                            return@addSnapshotListener
                        }

                        if (statsSnap != null) {

                            fun obtenerTotal(nombreDoc: String): Int {
                                val d = statsSnap.documents.find { it.id == nombreDoc }
                                return (d?.get("total") as? Number)?.toInt() ?: 0
                            }

                            val totalVistas = obtenerTotal("vistas")
                            val totalGuardados = obtenerTotal("guardados")
                            val totalClic = obtenerTotal("clic")

                            val fb = obtenerTotal("facebook")
                            val ig = obtenerTotal("instagram")
                            val tk = obtenerTotal("tiktok")
                            val stweb = obtenerTotal("sitio_web")
                            val wsap = obtenerTotal("whatsapp")
                            val llamada = obtenerTotal("llamada")
                            val ruta = obtenerTotal("ruta")
                            val compartidos = obtenerTotal("compartidos")

                            val perfil_qr = obtenerTotal("perfil_qr")
                            val review_c_qr = obtenerTotal("review_c_qr")
                            val review_qr = obtenerTotal("review_qr")
                            val crear_ruta_qr = obtenerTotal("crear_ruta_qr")

                            // ✔️ AQUÍ ESTABA EL ERROR → faltaba poner el nombre del último parámetro
                            resultado(
                                datos_tienda(
                                    id_tienda = id_tienda,
                                    nombre = nombre_tienda,
                                    horario_tiendaMap = horarioMap,
                                    total_vista = totalVistas,
                                    total_guardados = totalGuardados,
                                    clic = totalClic,
                                    fb = fb,
                                    ig = ig,
                                    tk = tk,
                                    stweb = stweb,
                                    wsap = wsap,
                                    llamada = llamada,
                                    ruta = ruta,
                                    perfil_qr = perfil_qr,
                                    review_c_qr = review_c_qr,
                                    review_qr = review_qr,
                                    crear_ruta_qr = crear_ruta_qr,
                                    localidad_tienda = localidadTienda,
                                    fecha_ingreso = fecha_ingreso,
                                    fecha_termino = fecha_termino,
                                    descripcion = descripcion,
                                    lista_ids_propietarios = propietario_id,
                                    saldo_disponible_tienda = saldo_tienda,
                                    compartidos = compartidos,
                                    obtener_img_tiendas = img_generales,
                                    metodos_pago = metodo_pago_tienda,
                                    metodo_contacto_tienda = contacto_obs,
                                    servicios_comodidades = servicios_comodidades,
                                    aforo = aforo_maximo,
                                    categoira_tienda = categoria_teinda,
                                    subcategorias_tienda = subcategoria,
                                    ubicacion = ubi_container
                                )
                            )
                        }
                    }
            }
        }
    }


    suspend fun guardar_horario_cerrado(
        id_tienda: String,
        dia: String,
        motivo: String,
        bloques: List<Map<String, String>>
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document("barranca")
            .collection("barranca").document(id_tienda)

        val dataDia = mapOf(
            "bloques" to bloques,
            "cerrado" to true,
            "motivo" to motivo
        )

        val updates = mapOf(
            "timeSlamp" to timeStampNumero(),
            "horario_atencion.${dia.lowercase()}" to dataDia
        )

        try {
            db.update(updates).await()
            Log.d("DB", "Horario de $dia actualizado correctamente (CERRADO).")
        } catch (e: Exception) {
            Log.e("DB", "Error al actualizar horario cerrado", e)
        }
    }


    suspend fun guardar_horario_atencion_abierto(
        id_tienda: String, dia: String, bloques: List<Map<String, String>>
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document("barranca")
            .collection("barranca").document(id_tienda)

        val dataDia = mapOf(
            "bloques" to bloques, "cerrado" to false, "motivo" to ""
        )

        val updates = mapOf(
            "timeSlamp" to timeStampNumero(), "horario_atencion.${dia.lowercase()}" to dataDia
        )

        try {
            db.update(updates).await()
            Log.d("DB", "Horario de $dia actualizado correctamente (CERRADO).")
        } catch (e: Exception) {
            Log.e("DB", "Error al actualizar horario cerrado", e)
        }

    }


    fun agregar_contador(tipo: String, id_tienda: String, localida_tienda: String) {
        Log.d("agregar", "$tipo $localida_tienda $id_tienda")
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document(localida_tienda)
            .collection(localida_tienda).document(id_tienda)
            .collection("estadisticas").document(tipo)

        db.update("total", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("CONTADOR", "Contador actualizado correctamente")
            }
            .addOnFailureListener { e ->
                db.set(mapOf("total" to 1))
            }
    }


    fun restar_contador(tipo: String, localida_tienda: String, id_tienda: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document(localida_tienda)
            .collection(localida_tienda).document(id_tienda)
            .collection("estadisticas").document(tipo)

        db.update("total", FieldValue.increment(-1))
            .addOnSuccessListener {
                Log.d("CONTADOR", "Contador decrementado correctamente")
            }
            .addOnFailureListener { e ->
                // Si no existe el doc, lo creamos como 0 (o 1 si prefieres)
                db.set(mapOf("total" to 0))
            }
    }


    fun verificar_existencia_tienda(
        id_user: String,
        ingresa_correo: Boolean,
        correo_tienda: String,
        id_tienda: String,
        localidad_tienda: String,
        resultado: (Boolean, String, String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        Log.d("verificar_existencia", "========== INICIO VERIFICACIÓN ==========")

        // ===========================================
        // FUNCIÓN AUXILIAR PARA VALIDAR CUPOS
        // ===========================================
        fun validarCupos(idTienda: String) {
            val refTienda = db.collection("Tiendas")
                .document(localidad_tienda.lowercase())
                .collection(localidad_tienda.lowercase())
                .document(idTienda)

            refTienda.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        resultado(false, "El id no existe", null)
                        return@addOnSuccessListener
                    }

                    val administradores =
                        snapshot.get("propietario_id") as? List<String> ?: emptyList()

                    if (administradores.size < 3) {
                        resultado(true, "", idTienda)
                    } else {
                        resultado(false, "Ya tienes 3 dispositivos vinculados", null)
                    }
                }
                .addOnFailureListener {
                    resultado(false, "Error al verificar la tienda", null)
                }
        }

        // ======================================================
        //  OPCIÓN 1: VERIFICACIÓN POR CORREO (NO VALIDAR CUPOS)
        // ======================================================
        if (ingresa_correo) {
            db.collection("Trabajadores_Usuarios_Drivers")
                .document("users")
                .collection("users")
                .document(id_user)
                .get()
                .addOnSuccessListener { res ->
                    if (!res.exists()) {
                        resultado(false, "Usuario no encontrado", null)
                        return@addOnSuccessListener
                    }

                    val correo_user = res.getString("correo")
                    val id_tienda_propietario = res.getString("id_tienda_propietario")

                    if (correo_user != correo_tienda) {
                        resultado(false, "El correo ingresado no pertenece a este perfil", null)
                        return@addOnSuccessListener
                    }

                    if (id_tienda_propietario.isNullOrEmpty()) {
                        resultado(false, "No cuentas con una tienda vinculada", null)
                        return@addOnSuccessListener
                    }

                    // 👉 CORREO ES CORRECTO Y TIENE TIENDA → PERMITIR SIN VALIDAR CUPOS
                    resultado(true, "", id_tienda_propietario)
                }
                .addOnFailureListener {
                    resultado(false, "Error al verificar el correo", null)
                }

            return
        }

        // ======================================================
        //  OPCIÓN 2: VERIFICAR POR ID (AQUÍ SÍ VALIDAR CUPOS)
        // ======================================================
        if (localidad_tienda.isEmpty() || id_tienda.isEmpty()) {
            resultado(false, "Localidad o ID inválidos", null)
            return
        }

        validarCupos(id_tienda)
    }

    fun restar_puntos(
        localidad_tienda: String,
        id_tienda: String,
        puntos_restar: Int,
        mes_agregado_cantidad: String
    ) {

        val refTienda = db.collection("Tiendas")
            .document(localidad_tienda.lowercase())
            .collection(localidad_tienda.lowercase())
            .document(id_tienda)

        refTienda.get().addOnSuccessListener { res ->
            if (res.exists()) {

                val puntosActuales = (res.getLong("puntos_tienda") ?: 0).toInt()
                val nuevosPuntos = (puntosActuales - puntos_restar).coerceAtLeast(0)

                // NUEVA FECHA FIN (desde hoy)
                val nuevaFechaFin = sumarTiempoDesdeHoy(mes_agregado_cantidad)

                val updates = hashMapOf<String, Any>(
                    "puntos_tienda" to nuevosPuntos,
                    "fechas.fecha_fin" to nuevaFechaFin    // <-- SOLO ESTO SE ACTUALIZA DEL MAPA
                )

                refTienda.update(updates)
                    .addOnSuccessListener {
                        Log.d("Puntos", "Puntos y fecha actualizados")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Puntos", "Error al actualizar", e)
                    }
            }

        }.addOnFailureListener {
            Log.e("Puntos", "Error al obtener puntos", it)
        }
    }


    fun sumarTiempoDesdeHoy(texto: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val hoy = LocalDate.now()

        return try {
            val partes = texto.trim().split(" ")
            val cantidad = partes[0].toInt()
            val unidad = partes[1].lowercase()

            val nuevaFecha = when {
                unidad.contains("día") -> hoy.plusDays(cantidad.toLong())
                unidad.contains("semana") -> hoy.plusWeeks(cantidad.toLong())
                unidad.contains("mes") -> hoy.plusMonths(cantidad.toLong())
                else -> hoy
            }

            nuevaFecha.format(formatter)

        } catch (e: Exception) {
            hoy.format(formatter)
        }
    }

    fun obtner_img_stoprage_cambios(
        tipo: String,
        idTienda: String,
        onResult: (List<String>) -> Unit
    ) {
        val TAG = "STORAGE_IMGS"

        Log.d(TAG, "📂 Buscando imágenes")
        Log.d(TAG, "➡️ tienda: $idTienda")
        Log.d(TAG, "➡️ tipo: $tipo")

        val storage = FirebaseStorage.getInstance()
        val carpetaRef = storage.reference
            .child("tiendas/$idTienda/imagenes/$tipo")

        carpetaRef.listAll()
            .addOnSuccessListener { result ->

                Log.d(TAG, "📸 Archivos encontrados: ${result.items.size}")

                if (result.items.isEmpty()) {
                    Log.d(TAG, "⚠️ No hay imágenes")
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val urls = mutableListOf<String>()
                var procesadas = 0

                result.items.forEach { fileRef ->

                    Log.d("obtenemos_img", "⬇️ Obteniendo URL de: ${fileRef.name}")

                    fileRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            urls.add(uri.toString())
                            Log.d("obtenemos_img", "✅ URL OK: $uri")
                        }
                        .addOnFailureListener { e ->
                            Log.e("obtenemos_img", "❌ Error en ${fileRef.name}", e)
                        }
                        .addOnCompleteListener {
                            procesadas++

                            if (procesadas == result.items.size) {

                                // 🔥 Ordenar por slot_X.webp
                                val ordenadas = urls.sortedBy {
                                    Regex("slot_(\\d+)").find(it)
                                        ?.groupValues
                                        ?.get(1)
                                        ?.toInt() ?: 0
                                }

                                Log.d("obtenemos_img", "📦 URLs finales (${ordenadas.size}):")
                                ordenadas.forEach {
                                    Log.d("obtenemos_img", it)
                                }

                                onResult(ordenadas)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("obtenemos_img", "❌ Error al listar carpeta", e)
                onResult(emptyList())
            }
    }

    suspend fun cambiar_pagos_tienda(
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String,
        valor_cambiado: Boolean
    ) {
        val ref = db
            .collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        ref.update(
            "metodos_pago.$metodo_pago.enable",
            valor_cambiado
        ).await()
    }

    suspend fun cambiar_contacto_redes(
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String,
        valor_cambiado: Boolean
    ) {
        val ref = db
            .collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        ref.update(
            "metodo_contacto.$metodo_pago.estado",
            valor_cambiado
        ).await()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun cambiar_NT_yape_plin(
        context: Context,
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String,
        titular: String,
        numero_cambiado: String,
        uri: Uri // 👈 NUNCA NULL
    ) {

        val ref = db
            .collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        var qrFinalUrl: String? = null

        when {
            // 🆕 NUEVA IMAGEN → SUBIR A STORAGE
            uri.scheme == "content" -> {

                val bytes = procesarImagenWebPSinRecorte(context, uri)

                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("tiendas/$id_tienda/pagos/$metodo_pago.webp")

                storageRef.putBytes(bytes).await()
                qrFinalUrl = storageRef.downloadUrl.await().toString()
            }

            // ❌ ELIMINADO
            uri == Uri.EMPTY -> {
                qrFinalUrl = null
            }

            // 🔁 YA ES URL (NO SE SUBE)
            uri.scheme == "http" || uri.scheme == "https" -> {
                qrFinalUrl = uri.toString()
            }
        }

        // ───── MAPA BASE (SIEMPRE) ─────
        val updates = mutableMapOf<String, Any>(
            "metodos_pago.$metodo_pago.nombre" to titular,
            "metodos_pago.$metodo_pago.numero" to numero_cambiado
        )

        // ───── QR ─────
        if (qrFinalUrl != null) {
            // ✅ guardar / reemplazar QR
            updates["metodos_pago.$metodo_pago.qr"] = qrFinalUrl
        } else {
            // ❌ eliminar QR
            updates["metodos_pago.$metodo_pago.qr"] = FieldValue.delete()
        }

        ref.update(updates).await()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun subirImagenesAFirebase(
        context: Context,
        imagenes: List<ImagenReview>,
        idSocio: String,
        idPromo: String
    ): List<String> {

        val storageRef = FirebaseStorage.getInstance().reference
        val urls = mutableListOf<String>()

        imagenes.forEachIndexed { index, img ->

            val uri = img.uri ?: return@forEachIndexed

            // 🔥 procesas la imagen en WebP / alta calidad
            val bytes = procesarImagenWebPSinRecorte(context, uri)

            // 📛 img1.webp, img2.webp, img3.webp...
            val nombreImg = "img${index + 1}.webp"

            val ref = storageRef.child(
                "tiendas/$idSocio/imagenes/promociones_geinz/$idPromo/$nombreImg"
            )

            // ⬆️ Subir bytes
            ref.putBytes(bytes).await()

            // 🔗 URL pública
            val downloadUrl = ref.downloadUrl.await()
            urls.add(downloadUrl.toString())
        }

        return urls
    }


    suspend fun guardarImagenesEnFirestore_promociones(
        id_tienda: String,
        logo_tienda: String,
        localidad: String,
        idPromo: String,
        urls: List<String>
    ) {
        val db = FirebaseFirestore.getInstance()

        val ref = db
            .collection("Tiendas")
            .document(localidad)
            .collection("promos_ofertas")
            .document(idPromo)
        val ref2 = db.collection("Tiendas").document(localidad).collection(localidad)
            .document(id_tienda).collection("promociones_geinz")
            .document(idPromo)


        val imgContainer = mapOf(
            "lista_img" to urls,
            "logo_img" to logo_tienda
        )

        val data = mapOf(
            "img_container" to imgContainer
        )
        ref2.set(data, SetOptions.merge()).await()
        ref.set(data, SetOptions.merge()).await()
    }

    suspend fun cambiar_atributos_tiendas(
        id_tienda: String,
        localidad_tienda: String,
        nombreAtributo: String,
        nuevoEstado: Boolean
    ) {
        val ref = db
            .collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        val snapshot = ref.get().await()

        val listaActual = snapshot.get("servicios_comodidades")
                as? List<Map<String, Any>>
            ?: emptyList()

        val listaActualizada = mutableListOf<Map<String, Any>>()

        var encontrado = false

        for (item in listaActual) {
            val key = item.keys.firstOrNull() ?: ""

            if (normalizar(key) == normalizar(nombreAtributo)) {
                // 🔁 Existe → reemplazar el map
                listaActualizada.add(
                    mapOf(nombreAtributo to nuevoEstado)
                )
                encontrado = true
            } else {
                listaActualizada.add(item)
            }
        }

        // ➕ No existía → agregar nuevo map
        if (!encontrado) {
            listaActualizada.add(
                mapOf(nombreAtributo to nuevoEstado)
            )
        }

        // 💾 Guardar array completo
        ref.update("servicios_comodidades", listaActualizada).await()
    }


    suspend fun cambiar_NT_metodo_contacto(
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String,
        titular: String,
        valor: String
    ) {
        val ref = db
            .collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        val data = if (metodo_pago.equals("whatsapp", true) ||
            metodo_pago.equals("llamada", true)
        ) {
            mapOf(
                "metodo_contacto.$metodo_pago.numero" to valor
            )
        } else {
            mapOf(
                "metodo_contacto.$metodo_pago.nombre" to titular,
                "metodo_contacto.$metodo_pago.url" to valor
            )
        }

        ref.update(data).await()
    }

    suspend fun cambiar_nombre_descripcion(
        localidad_tienda: String,
        id_tienda: String,
        tipo: String, // nombre del campo a cambiar
        cambio: String // nuevo valor
    ) {
        try {
            val ref = db
                .collection("Tiendas")
                .document(localidad_tienda)
                .collection(localidad_tienda)
                .document(id_tienda)

            // Actualiza dinámicamente el campo
            ref.update(tipo, cambio).await()

            println("Campo '$tipo' actualizado correctamente a '$cambio'.")
        } catch (e: Exception) {
            println("Error al actualizar el campo: ${e.message}")
        }
    }

    suspend fun guardar_aforo(
        numero: String,
        id_tienda: String,
        localidad: String
    ) {
        val aforo = numero.toIntOrNull() ?: return

        try {
            val ref = db
                .collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)

            ref.set(
                mapOf("aforo_max" to aforo),
                SetOptions.merge()
            ).await()

        } catch (e: Exception) {
            println("Error al actualizar aforo: ${e.message}")
        }
    }


    suspend fun cambiar(
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String, // yape | plin
        titular: String,
        numero_cambiado: String
    ) {
        val ref = db
            .collection("Tiendas")
            .document(localidad_tienda)
            .collection(localidad_tienda)
            .document(id_tienda)

        ref.update(
            mapOf(
                "metodos_pago.$metodo_pago.nombre" to titular,
                "metodos_pago.$metodo_pago.numero" to numero_cambiado
            )
        ).await()
    }

    suspend fun crear_promocion(
        i: agregar_promociones,
        localidad: String
    ): Result<Unit> {
        return try {
            val ref = db.collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .document(i.informacion.id_promocion)

            val ref2 = db.collection("Tiendas").document(localidad).collection(localidad)
                .document(i.informacion.id_tienda).collection("promociones_geinz")
                .document(i.informacion.id_promocion)

            val hasmap = hashMapOf<String, Any>(
                "fechas" to i.fechas
            )

            val hashMap = hashMapOf<String, Any>(
                "exclusivo" to i.exclusivo,
                "fechas" to i.fechas,
                "img_container" to i.img_container,
                "informacion" to i.informacion,
                "ubicacion" to i.ubicacion
            )

            ref.set(hashMap, SetOptions.merge()).await()
            ref2.set(hashMap, SetOptions.merge()).await()

            Result.success(Unit) // ✅ TERMINÓ BIEN
        } catch (e: Exception) {
            Result.failure(e)    // ❌ FALLÓ
        }
    }

    suspend fun agregarContadorNotificacion(
        usuarios: List<String>,
        i: obj_contador_notificaciones
    ) {
        try {
            enviar_notificacion_lista_dispo(
                i.idnotificacion,
                i.id_tienda, i.localida, i.categoria,
                tipo_notificacion_params = i.tipo_notificacion,
                id_users = usuarios,
                titulo = i.parametros_notificacion.titulo_notificacion,
                txt = i.parametros_notificacion.texto_notificacion,
                logo_tienda = i.parametros_notificacion.logo_notificacion,
                tipo_notificacion = i.parametros_notificacion.tipo_notificacion,
                url_img = i.parametros_notificacion.img_notifiacion,
                prioridad = i.parametros_notificacion.priorida_notificacion
            )
//            Log.d("NOTI", "Enviados: ${resultado.enviadosCorrectos}")
//            Log.d("NOTI", "Fallidos: ${resultado.enviadosFallidos}")
            actualizarEstadoNotificaciones(i)
            val ref = db.collection("Tiendas")
                .document(i.localida)
                .collection(i.localida)
                .document(i.id_tienda)
                .collection("notificaciones_enviadas")
                .document(i.idnotificacion)


            // Transformamos los objetos anidados en Map para Firebase
            val paramsMap = hashMapOf(
                "id_tienda" to i.id_tienda,
                "localidad" to i.localida,
                "titulo_notificacion" to i.parametros_notificacion.titulo_notificacion,
                "texto_notificacion" to i.parametros_notificacion.texto_notificacion,
                "logo_notificacion" to i.parametros_notificacion.logo_notificacion,
                "img_notificacion" to i.parametros_notificacion.img_notifiacion,
                "nombre_tienda" to i.nombre_tienda,
                "numero_contacto" to i.numero_contacto_tienda,
                "categoria_tienda" to i.categoira_tienda
            )

            val params_notificacion =hashMapOf(
                "id_noti" to i.idnotificacion,
                "id_publicacion_anuncio" to i.parametros_notificacion.id_publicacion_anuncio,
                "priorida_notificacion" to i.parametros_notificacion.priorida_notificacion,
                "tipo_notificacion" to i.parametros_notificacion.tipo_notificacion,
                "tipo_clikeable" to i.tipo_notificacion,
                "notificacion_nueva" to i.parametros_notificacion.notificacion_publicidad,
                "total_gastado" to i.precio_envio,
                )

            val suspendidoMap = hashMapOf(
                "suspendido" to i.suspendido.suspendido,
                "descripcion_suspencion" to i.suspendido.descrpcion_suspencion
            )

            val hashMap = hashMapOf<String, Any>(
                "fecha_envio" to i.fecha_enviada,
                "params_noti" to paramsMap,
                "observacion" to suspendidoMap,
                "params_notificacion" to params_notificacion
            )

            // Usando await() para que sea verdaderamente suspend
            ref.set(hashMap).await()
            Log.d("FIREBASE_NOTI", "✅ Notificación guardada correctamente")

        } catch (e: Exception) {
            Log.e("FIREBASE_NOTI", "❌ Error al guardar notificación", e)
        }
    }

    suspend fun obtenerSeguidoresTienda(localidad: String, idTienda: String): List<String> {
        val db = FirebaseFirestore.getInstance()

        return try {
            val ref = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(idTienda)
                .collection("seguidores")

            val snapshot = ref.get().await()


            snapshot.documents.map { it.id }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun actualizarEstadoNotificaciones(i: obj_contador_notificaciones) {
        val estadoRef = db.collection("Tiendas")
            .document(i.localida)
            .collection(i.localida)
            .document(i.id_tienda)
            .collection("estado_notificaciones")
            .document("estado")

        val snapshot = estadoRef.get().await()

        if (!snapshot.exists()) {
            // 👉 Primera notificación
            val estado = hashMapOf(
                "fecha_inicio" to i.fecha_enviada,
                "fecha_fin" to obtenerFechaConDias(i.fecha_enviada, 7),
                "contador" to 1,
                "maximo" to 3,
                "promocion_nueva" to true   // 👈 CLAVE
            )
            estadoRef.set(estado).await()

        } else {
            // 👉 Ya existe → incrementar y marcar nuevo
            estadoRef.update(
                mapOf(
                    "contador" to FieldValue.increment(1),
                    "promocion_nueva" to true   // 👈 CLAVE
                )
            ).await()
        }
    }

    suspend fun verificar_envio_notificaciones(
        localidad: String, id_tienda: String
    ): Boolean {

        val estadoRef = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("estado_notificaciones")
            .document("estado")

        val snapshot = estadoRef.get().await()


        if (!snapshot.exists()) return true

        val contador = snapshot.getLong("contador") ?: 0
        val maximo = snapshot.getLong("maximo") ?: 0

        return contador < maximo
    }

    suspend fun obtener_lista_tokens(id_user: String): List<Pair<String, String>> {
        val listaTokens = mutableListOf<Pair<String, String>>()

        val snapshot = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("tokens")
            .document(id_user)
            .get()
            .await()

        if (!snapshot.exists()) {
            Log.d("TOKENS", "❌ No existe documento para este usuario")
            return emptyList()
        }

        val mapaTokens =
            (snapshot.data?.get("tokens") as? Map<String, String>) ?: emptyMap()

        // dispositivo = nombre, token = token FCM
        mapaTokens.forEach { (dispositivo, token) ->
            listaTokens.add(dispositivo to token)
        }

        return listaTokens
    }

    suspend fun obtener_publicaciones_tiendas(
        localidad: String,
        id_tienda: String
    ): List<datos_publicaciones_realizadas> {

        val resultado = mutableListOf<datos_publicaciones_realizadas>()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hoy = dateFormat.parse(dateFormat.format(Date())) ?: Date()

        val snapshot = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("promociones_geinz")
            .get()
            .await()

        for (doc in snapshot.documents) {

            val informacion = doc.get("informacion") as? Map<*, *> ?: continue
            val fechas = doc.get("fechas") as? Map<*, *> ?: continue
            val imgContainer = doc.get("img_container") as? Map<*, *>

            val titulo = informacion["titulo"] as? String ?: ""
            val descripcion = informacion["descripcion"] as? String ?: ""
            val id = informacion["id_promocion"] as? String ?: doc.id

            val inicioStr = fechas["inicio"] as? String
            val finStr = fechas["fin"] as? String

            val inicio = inicioStr?.let { dateFormat.parse(it) }
            val fin = finStr?.let { dateFormat.parse(it) }

            val activo = if (inicio != null && fin != null) {
                !hoy.before(inicio) && !hoy.after(fin)
            } else {
                false
            }

            val listaImg = imgContainer?.get("lista_img") as? List<*>
            val img = listaImg?.getOrNull(0) as? String ?: ""
            resultado.add(
                datos_publicaciones_realizadas(
                    titulo = titulo,
                    descripcion = descripcion,
                    activo = activo,
                    fecha_publicado = inicioStr ?: "",
                    id = id,
                    img = img
                )
            )
        }

        return resultado
    }




}
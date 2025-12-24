package com.geinzz.geinzwork.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.normalizar
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenWebPSinRecorte
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.timeStampNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoservicios_comodidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_img_usert
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                val logo = img_tienda["logo_tienda"] as? String ?: ""


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
                val metodos_servicios=data?.get("servicios_comodidades")as? List<Map<String, Any>>
                val aforo_maximo =data?.get("aforo_max") as? Number ?:0
                val servicios_comodidades=metodos_servicios.toMetodoservicios_comodidades()
                val contacto_obs = metodos_contacto.toMetodoContacto()
                val metodo_pago_tienda = metodo_pago.to_metodo_pago()
                val img_generales = img_tienda.to_img_usert()
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
                                    aforo = aforo_maximo
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
            metodo_pago.equals("llamada", true)) {
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


}
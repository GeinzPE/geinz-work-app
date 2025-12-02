package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.timeStampNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

                            // ✔️ AQUÍ ESTABA EL ERROR → faltaba poner el nombre del último parámetro
                            resultado(
                                datos_tienda(
                                    id_tienda = id_tienda,
                                    nombre = nombre_tienda,
                                    img_tienda = logo,
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
                                    localidad_tienda = localidadTienda,
                                    fecha_ingreso = fecha_ingreso,
                                    fecha_termino = fecha_termino,
                                    descripcion = descripcion, propietario_id
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
                val nuevaFechaFin = sumarMesesDesdeHoy(mes_agregado_cantidad.toInt())

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


    fun sumarMesesDesdeHoy(meses: Int): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val hoy = LocalDate.now()                     // ← FECHA ACTUAL
            val nuevaFecha = hoy.plusMonths(meses.toLong()) // ← SUMA LOS MESES
            nuevaFecha.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }


}
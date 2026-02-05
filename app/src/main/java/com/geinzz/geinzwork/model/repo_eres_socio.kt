package com.geinzz.geinzwork.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.DatosDemograficosUsuario
import com.geinzz.geinzwork.data.model.DatosPublicidadIA
import com.geinzz.geinzwork.data.model.agregar_promociones
import com.geinzz.geinzwork.data.model.contenido_publicidad
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.datos_notificacion
import com.geinzz.geinzwork.data.model.datos_publicaciones_realizadas
import com.geinzz.geinzwork.data.model.datos_recarga
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.generacion_primarios
import com.geinzz.geinzwork.data.model.generaciones_con_ia
import com.geinzz.geinzwork.data.model.generaciones_con_ia_notificaciones
import com.geinzz.geinzwork.data.model.generaciones_con_ia_notificaciones_solo_generaciones
import com.geinzz.geinzwork.data.model.nuevas_notificaciones
import com.geinzz.geinzwork.data.model.obj_contador_notificaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones.timestampEn30Dias
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_expandibles_generales.normalizar
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenWebPSinRecorte
import com.geinzz.geinzwork.herramientas_geinz.constantes.generarPromptNombreGeneracionIA
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.acortarDescripcionNotificacion
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaConDias
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.timeStampNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoservicios_comodidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_img_usert
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_ubicacion_container
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend


import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.get
import kotlin.math.ceil
import kotlin.math.floor


@RequiresApi(Build.VERSION_CODES.O)

class repo_eres_socio {
    private val db = FirebaseFirestore.getInstance()

    private var genIAOriginal: generaciones_con_ia? = null

    private val db_sec: FirebaseFirestore by lazy {
        FirebaseSecundario.getFirestore()
    }

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


    fun fechaFinTiendaPanel(
        idTienda: String,
        localidad: String,
        onCambio: (String) -> Unit
    ): ListenerRegistration {
        // Obtenemos la referencia al documento
        val docRef = db.collection("Tiendas")
            .document(localidad)
            .collection("tiendas_servicios_geinz_activos")
            .document(idTienda)

        // Agregamos un listener para tiempo real
        return docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Manejo de error
                Log.e("Firestore", "Error escuchando cambios: ${error.message}")
                onCambio("")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val fechaFin = snapshot.get("panel_admin") as? Map<String, Any> ?: emptyMap()
                val fecha_fin_retunt = fechaFin.get("fecha_fin") as? String ?: ""
                onCambio(fecha_fin_retunt)
            } else {
                onCambio("") // No existe el documento
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


    fun agregar_contador(
        tipo: String,
        id_tienda: String,
        localida_tienda: String,
        id_user: String
    ) {

        val estadisticaRef = FirebaseFirestore.getInstance()
            .collection("Tiendas").document(localida_tienda)
            .collection(localida_tienda).document(id_tienda)
            .collection("estadisticas")
            .document(tipo)

        // 🔹 TOTAL (lo que ya tienes)
        incrementar(estadisticaRef)

        // 🔹 DATOS DEL USUARIO
        obtenerDatosUsuario(id_user) { datos ->
            if (datos == null) return@obtenerDatosUsuario

            // LOCALIDAD
            if (datos.localidad.isNotEmpty()) {
                incrementar(
                    estadisticaRef
                        .collection("localida")
                        .document(datos.localidad)
                )
            }

            // GENERO
            if (datos.genero.isNotEmpty()) {
                incrementar(
                    estadisticaRef
                        .collection("genero")
                        .document(datos.genero.lowercase())
                )
            }

            // EDAD
            incrementar(
                estadisticaRef
                    .collection("edad")
                    .document(obtenerRangoEdad(datos.edad))
            )
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


    fun agregar_contador_estadistica_noti(
        tipo: String,
        id_tienda: String,
        localida_tienda: String,
        id_notificacion: String,
        id_user: String
    ) {
        val estadisticaRef = FirebaseFirestore.getInstance()
            .collection("Tiendas").document(localida_tienda)
            .collection(localida_tienda).document(id_tienda)
            .collection("notificaciones_enviadas").document(id_notificacion)
            .collection("estadisticas")
            .document(tipo)

        // 🔹 TOTAL
        incrementar(estadisticaRef)

        // 🔹 DATOS DEL USUARIO
        obtenerDatosUsuario(id_user) { datos ->
            if (datos == null) return@obtenerDatosUsuario

            // LOCALIDAD
            if (datos.localidad.isNotBlank()) {
                incrementar(
                    estadisticaRef
                        .collection("localida")
                        .document(datos.localidad.lowercase())
                )
            }

            // GÉNERO
            if (datos.genero.isNotBlank()) {
                incrementar(
                    estadisticaRef
                        .collection("genero")
                        .document(datos.genero.lowercase())
                )
            }

            // EDAD (por rango)
            incrementar(
                estadisticaRef
                    .collection("edad")
                    .document(obtenerRangoEdad(datos.edad))
            )
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


    fun verificarVinculadoRealtime(
        idUser: String,
        idTienda: String,
        localidad: String,
        onResult: (Boolean) -> Unit
    ) {
        val docRef = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(idTienda)

        // Listener en tiempo real
        docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Hubo un error en la escucha
                onResult(false)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                val listaPropietarios = data?.get("propietario_id") as? List<*>
                val estaVinculado = listaPropietarios?.any { it.toString() == idUser } ?: false
                onResult(estaVinculado)
            } else {
                onResult(false)
            }
        }
    }


//   suspend fun restar_puntos(
//        localidad_tienda: String,
//        id_tienda: String,
//        puntos_restar: Int,
//        mes_agregado_cantidad: String
//    ) {
//        val refTienda = db.collection("Tiendas")
//            .document(localidad_tienda.lowercase())
//            .collection(localidad_tienda.lowercase())
//            .document(id_tienda)
//
//        refTienda.get().addOnSuccessListener { res ->
//            if (res.exists()) {
//
//                val puntosActuales = (res.getLong("puntos_tienda") ?: 0).toInt()
//                val nuevosPuntos = (puntosActuales - puntos_restar).coerceAtLeast(0)
//
//                // NUEVA FECHA FIN (desde hoy)
//                val nuevaFechaFin = sumarTiempoDesdeHoy(mes_agregado_cantidad)
//
//                val updates = hashMapOf<String, Any>(
//                    "puntos_tienda" to nuevosPuntos,
//                )
//
//                refTienda.update(updates)
//                    .addOnSuccessListener {
//                        Log.d("Puntos", "Puntos y fecha actualizados")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e("Puntos", "Error al actualizar", e)
//                    }
//            }
//
//        }.addOnFailureListener {
//            Log.e("Puntos", "Error al obtener puntos", it)
//        }
//
//
//        // 1️⃣ Obtener propietario
//        val idPropietarioSnap = db.collection("Tiendas")
//            .document(localidad_tienda.lowercase())
//            .collection(localidad_tienda.lowercase())
//            .document(id_tienda)
//            .get()
//            .await()
//
//        val propietario_id: List<String> = if (idPropietarioSnap.exists()) {
//            val data = idPropietarioSnap.data
//            data?.get("propietario_id") as? List<String> ?: emptyList()
//        } else {
//            emptyList()
//        }
//
//        // 2️⃣ Referencia a las notificaciones
//        val estadoRef = db.collection("Tiendas")
//            .document(localidad_tienda.lowercase())
//            .collection("tiendas_servicios_geinz_activos")
//            .document(id_tienda)
//
//        val snapshot = estadoRef.get().await()
//        val notificaciones = snapshot.get("panel_admin") as? Map<*, *>
//        val timestampFinExiste = notificaciones?.containsKey("timestamp_fin") == true
//        val propietarioExiste = snapshot.contains("propietario_id")
//
//        if (!snapshot.exists() || !timestampFinExiste) {
//            // 🔥 NUEVO CICLO DE NOTIFICACIONES
//            val fechaFinString = sumarTiempoDesdeHoy(mes_agregado_cantidad)
//
//            val panel_admin_map = hashMapOf(
//                "fecha_fin" to fechaFinString,
//                "timestamp_fin" to stringFechaATimestamp(fechaFinString)
//            )
//
//            // 🔹 Crear mapa a guardar
//            val mapaCompleto = hashMapOf<String, Any>(
//                "panel_admin" to panel_admin_map
//            )
//
//            // 🔹 Solo agregar propietario_id si no existe
//            if (!propietarioExiste) {
//                mapaCompleto["propietario_id"] = propietario_id
//            }
//
//            // 🔹 Guardar en Firestore con merge
//            estadoRef.set(mapaCompleto, SetOptions.merge()).await()
//
//        } else {
//        }
//    }


    suspend fun restar_puntos(
        localidadTienda: String,
        idTienda: String,
        puntosRestar: Int,
        mesAgregadoCantidad: String
    ) {
        val localidadLower = localidadTienda.lowercase()
        Log.d("Puntos", "==== INICIO restar_puntos ====")
        Log.d(
            "Puntos",
            "Tienda: $idTienda, Localidad: $localidadLower, Puntos a restar: $puntosRestar"
        )

        try {
            // 1️⃣ Referencia a la tienda principal
            val refTienda = db.collection("Tiendas")
                .document(localidadLower)
                .collection(localidadLower)
                .document(idTienda)

            val tiendaSnap = refTienda.get().await()
            if (!tiendaSnap.exists()) {
                Log.e("Puntos", "La tienda no existe")
                return
            }

            // 🔹 Calcular y actualizar puntos
            val puntosActuales = (tiendaSnap.getLong("puntos_tienda") ?: 0).toInt()
            val nuevosPuntos = (puntosActuales - puntosRestar).coerceAtLeast(0)
            refTienda.update("puntos_tienda", nuevosPuntos).await()
            Log.d("Puntos", "Puntos actualizados: $puntosActuales → $nuevosPuntos")

            // 🔹 Obtener propietario_id
            val propietarioId: List<String> =
                tiendaSnap.get("propietario_id") as? List<String> ?: emptyList()
            Log.d("Puntos", "Propietario_id: $propietarioId")

            // 2️⃣ Subcolección "tiendas_servicios_geinz_activos"
            val estadoRef = db.collection("Tiendas")
                .document(localidadLower)
                .collection("tiendas_servicios_geinz_activos")
                .document(idTienda)

            val estadoSnap = estadoRef.get().await()
            val propietarioExiste = estadoSnap.contains("propietario_id")
            val panelAdmin = estadoSnap.get("panel_admin") as? Map<*, *>
            val timestampFinExiste = panelAdmin?.containsKey("timestamp_fin") == true

            val nuevaFechaFin = sumarTiempoDesdeHoy(mesAgregadoCantidad)
            Log.d("Puntos", "Nueva fecha_fin calculada: $nuevaFechaFin")

            // 🔹 Crear mapa panel_admin
            val panelAdminMap = hashMapOf(
                "fecha_fin" to nuevaFechaFin,
                "timestamp_fin" to stringFechaATimestamp(nuevaFechaFin)
            )

            // 🔹 Mapa completo a guardar
            val mapaCompleto = hashMapOf<String, Any>(
                "panel_admin" to panelAdminMap
            )

            if (!propietarioExiste && propietarioId.isNotEmpty()) {
                mapaCompleto["propietario_id"] = propietarioId
                Log.d("Puntos", "Agregando propietario_id al documento")
            } else {
                Log.d("Puntos", "Propietario_id ya existe, no se agregará")
            }

            // 🔹 Guardar en Firestore con merge (crea doc si no existía)
            estadoRef.set(mapaCompleto, SetOptions.merge()).await()
            Log.d("Puntos", "Panel_admin y propietario_id actualizados correctamente")

            Log.d("Puntos", "==== FIN restar_puntos ====")
        } catch (e: Exception) {
            Log.e("Puntos", "Error en restar_puntos: ${e.message}", e)
        }
    }


    fun sumarTiempoDesdeHoy(texto: String): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
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
    ): Result<Unit> {
        return try {
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subirImagenesConReintento(
        intentos: Int = 3,
        bloque: suspend () -> List<String>
    ): List<String> {
        repeat(intentos - 1) {
            try {
                return bloque()
            } catch (_: Exception) {
            }
        }
        return bloque() // último intento
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
        datos_si_paso_IA: DatosPublicidadIA,
        tuvi_nueva_genearcion: Boolean,
        lista_img_subida: List<String>,
        i: agregar_promociones,
        localidad: String
    ): Result<Unit> {
        return try {
            val tieneDatos = datos_si_paso_IA.let { datos ->
                datos.titulo.isNotBlank() ||
                        datos.descripcion.isNotBlank() ||
                        datos.whatsapp.isNotBlank() ||
                        datos.compartir.isNotBlank() ||
                        datos.tipo_redirigido.isNotBlank() ||
                        datos.id_generacion_sin_publicar != null ||
                        datos.datos_generaciones.let { gen ->
                            gen.lista_obciones?.isNotEmpty() == true ||
                                    !gen.titulo_original.isNullOrBlank() ||
                                    !gen.descripcion_original.isNullOrBlank() ||
                                    !gen.titulo_seleccionado.isNullOrBlank() ||
                                    !gen.descripcion_seleccionada.isNullOrBlank()
                        }}

            val ref = db.collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .document(i.informacion.id_promocion)

            val ref2 = db.collection("Tiendas").document(localidad).collection(localidad)
                .document(i.informacion.id_tienda).collection("promociones_geinz")
                .document(i.informacion.id_promocion)

            val hashMap = hashMapOf<String, Any>(
                "estado" to i.estado,
                "tipo_hora_dias" to i.formato_fecha_hora,
                "datos_hora_fecha" to i.datos_hora_fecha,
                "informacion" to i.informacion,
                "ubicacion" to i.ubicacion,
                "mensaje_predeterminado" to i.mensaje_predeterminado,
                "horario_publicacion" to i.horario_deseado.seleccion,
                "precio_publicacion" to i.precio_publicacion.precio,
                "rango_establecido" to i.precio_publicacion.rango
            )

            // 🤖 SOLO SI EXISTE IA
            if (tuvi_nueva_genearcion) {
                val gen_con_IA = db.collection("Tiendas").document(localidad).collection(localidad)
                    .document(i.informacion.id_tienda).collection("gen_con_IA_historial")
                    .document(i.informacion.id_promocion)
                val hashmpa_gen_con_IA = hashMapOf<String, Any>(
                    "fecha" to Timestamp.now(),
                    "img_container" to lista_img_subida.first(),
                    "caudidad" to timestampEn30Dias(30),
                    "id_promo_o_noti" to i.informacion.id_promocion,
                    "tipo" to "publicacion",
                    "generacions_con_IA" to i.generaciones_con_ia
                )
                val descripcionAcortada = acortarDescripcionNotificacion(
                    i.informacion.descripcion
                )

                val nombreGeneracion = crear_notificacion_conIA_corta(
                    i.informacion.titulo,
                    descripcionAcortada
                )
                nombreGeneracion.let {
                    hashmpa_gen_con_IA["nombre_generacion"] = it
                }

                gen_con_IA.set(hashmpa_gen_con_IA, SetOptions.merge()).await()
            }else if(tieneDatos && !datos_si_paso_IA.id_generacion_sin_publicar.isNullOrBlank()) {
                val gen_con_IA = db.collection("Tiendas").document(localidad).collection(localidad)
                    .document(i.informacion.id_tienda).collection("gen_con_IA_historial")
                    .document(datos_si_paso_IA.id_generacion_sin_publicar)
                val hashmpa_gen_con_IA = hashMapOf<String, Any>(
                    "fecha" to Timestamp.now(),
                    "img_container" to lista_img_subida.first(),
                    "caudidad" to timestampEn30Dias(30),
                    "tipo" to "publicacion",

                )

                gen_con_IA.set(hashmpa_gen_con_IA, SetOptions.merge()).await()
            }

            ref.set(hashMap, SetOptions.merge()).await()
            ref2.set(hashMap, SetOptions.merge()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CREAR_PROMO", "Error al crear promoción", e)
            Result.failure(e)
        }
    }


    suspend fun guaradar_generacion_normal_por_7_dias(
        id_generacion:String,
        localidad: String,
        id_tienda: String,
        i: generacion_primarios,
        tipo: String
    ) {
        val gen_con_IA = db.collection("Tiendas").document(localidad).collection(localidad)
            .document(id_tienda).collection("gen_con_IA_historial")
            .document(id_generacion)
        val hashmpa_gen_con_IA = hashMapOf<String, Any>(
            "fecha" to Timestamp.now(),
            "img_container" to "",
            "caudidad" to timestampEn30Dias(7),
            "id_promo_o_noti" to id_generacion,
            "tipo" to tipo,
            "generacions_con_IA" to i
        )
        val descripcionAcortada = acortarDescripcionNotificacion(
            i.descripcion_original
        )

        val nombreGeneracion = crear_notificacion_conIA_corta(
            i.titulo_original,
            descripcionAcortada
        )
        nombreGeneracion.let {
            hashmpa_gen_con_IA["nombre_generacion"] = it
        }

        gen_con_IA.set(hashmpa_gen_con_IA, SetOptions.merge()).await()
    }

    suspend fun guaradar_generacion_normal_por_7_dias_notificaciones(
        id_generacion:String,
        localidad: String,
        id_tienda: String,
        i: generacion_primarios,
    ) {

        val gen_con_IA = db.collection("Tiendas").document(localidad).collection(localidad)
            .document(id_tienda).collection("gen_con_IA_historial")
            .document(id_generacion)

        val historialIAData = mutableMapOf<String, Any>(
            "fecha" to Timestamp.now(),
            "caudidad" to timestampEn30Dias(7),
            "id_promo_o_noti" to id_generacion,
            "tipo" to "notificacion_sin_publicar",
            "generacions_con_IA" to i
        )
        val descripcionAcortada = acortarDescripcionNotificacion(
            i.titulo_original
        )

        val nombreGeneracion = crear_notificacion_conIA_corta(
            i.descripcion_original,
            descripcionAcortada
        )
        nombreGeneracion.let {
            historialIAData["nombre_generacion"] = it
        }

        gen_con_IA.set(historialIAData, SetOptions.merge()).await()
    }




    suspend fun crear_notificacion_conIA_corta(
        tituloPublicacion: String,
        descCorta: String
    ): String {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val descripcionAcortada = acortarDescripcionNotificacion(descCorta)

        return try {
            val prompt = generarPromptNombreGeneracionIA(
                tituloPublicacion,
                descripcionAcortada
            )

            val result = model.generateContent(prompt)

            result.text
                ?.trim()
                ?.lines()?.firstOrNull()
                ?.removePrefix("Nombre:")
                ?.removePrefix("Nombre :")
                ?.trim()
                ?.take(40)
                ?: "Generación IA"


        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
            "Generación IA"
        }
    }


    suspend fun agregarContadorNotificacion(
        usuarios: List<String>,
        i: obj_contador_notificaciones
    ) {
        try {

            // 🔒 VALIDACIONES BÁSICAS (NO IA)
            require(i.idnotificacion.isNotBlank())
            require(i.id_tienda.isNotBlank())
            require(i.localida.isNotBlank())

            val resultado = enviar_notificacion_lista_dispo(
                id_promo = i.idnotificacion,
                id_tienda = i.id_tienda,
                localidad = i.localida,
                categora_tienda = i.categoria,
                tipo_notificacion_params = i.tipo_notificacion,
                id_users = usuarios,
                titulo = i.parametros_notificacion.titulo_notificacion,
                txt = i.parametros_notificacion.texto_notificacion,
                logo_tienda = i.parametros_notificacion.logo_notificacion,
                tipo_notificacion = i.parametros_notificacion.tipo_notificacion,
                url_img = i.parametros_notificacion.img_notifiacion,
                prioridad = i.parametros_notificacion.priorida_notificacion
            )

            Log.d("NOTI", "Enviados: ${resultado.enviadosCorrectos}")
            Log.d("NOTI", "Fallidos: ${resultado.enviadosFallidos}")

            actualizarEstadoNotificaciones(i)

            val ref = db.collection("Tiendas")
                .document(i.localida)
                .collection(i.localida)
                .document(i.id_tienda)
                .collection("notificaciones_enviadas")
                .document(i.idnotificacion)

            val rootRef = db.collection("Tiendas").document(i.localida)

            val historialIARef = rootRef
                .collection(i.localida)
                .document(i.id_tienda)
                .collection("gen_con_IA_historial")
                .document(i.idnotificacion)

            // ------------------------------------------------------------------
            // ⬇️ NO SE TOCA: MAPS ORIGINALES (tal como pediste)
            // ------------------------------------------------------------------

            val paramsMap = hashMapOf(
                "id_tienda" to i.id_tienda,
                "localidad" to i.localida,
                "titulo_notificacion" to i.parametros_notificacion.titulo_notificacion,
                "texto_notificacion" to i.parametros_notificacion.texto_notificacion,
                "logo_notificacion" to i.parametros_notificacion.logo_notificacion,
                "img_notificacion" to i.parametros_notificacion.img_notifiacion,
                "nombre_tienda" to i.nombre_tienda,
                "numero_contacto" to i.numero_contacto_tienda,
                "categoria_tienda" to i.categoira_tienda,
                "id_img_storage" to i.id_img_storage
            )

            val params_notificacion = hashMapOf(
                "id_noti" to i.idnotificacion,
                "id_publicacion_anuncio" to i.parametros_notificacion.id_publicacion_anuncio,
                "priorida_notificacion" to i.parametros_notificacion.priorida_notificacion,
                "tipo_notificacion" to i.parametros_notificacion.tipo_notificacion,
                "tipo_clikeable" to i.tipo_notificacion,
                "notificacion_nueva" to i.parametros_notificacion.notificacion_publicidad,
                "total_gastado" to i.precio_envio,
                "msje_predeterminado" to i.parametros_notificacion.mensaje_programado_whatsap
            )

            val resultado_notificacion = hashMapOf(
                "enviados" to resultado.enviadosCorrectos,
                "fallido" to resultado.enviadosFallidos
            )

            val suspendidoMap = hashMapOf(
                "suspendido" to i.suspendido.suspendido,
                "descripcion_suspencion" to i.suspendido.descrpcion_suspencion
            )

            val hashMap = hashMapOf<String, Any>(
                "fecha_envio" to i.fecha_enviada,
                "datos_de_notificacion" to paramsMap,
                "observacion" to suspendidoMap,
                "resultado_notificacion" to resultado_notificacion,
                "params_notificacion" to params_notificacion,
                "fecha_caducidad" to i.fecha_caducidad
            )

            // ------------------------------------------------------------------
            // 🤖 IA OPCIONAL (CORRECTO)
            // ------------------------------------------------------------------
            if (i.generaciones_con_ia_notificaciones.titulo_original.isNotEmpty()) {
                val generacionSeleccionada =
                    i.generaciones_con_ia_notificaciones?.generacion_selecionada

                val nombreGeneracion: String? =
                    if (
                        generacionSeleccionada != null &&
                        generacionSeleccionada.titulo.isNotBlank() &&
                        generacionSeleccionada.descripcion.isNotBlank()
                    ) {
                        val descripcionAcortada = acortarDescripcionNotificacion(
                            generacionSeleccionada.descripcion
                        )

                        crear_notificacion_conIA_corta(
                            generacionSeleccionada.titulo,
                            descripcionAcortada
                        )
                    } else {
                        null
                    }

                val historialIAData = mutableMapOf<String, Any>(
                    "fecha" to Timestamp.now(),
                    "img_container" to i.parametros_notificacion.img_notifiacion,
                    "caudidad" to timestampEn30Dias(30),
                    "id_promo_o_noti" to i.idnotificacion,
                    "tipo" to "notificacion",
                    "generacions_con_IA" to i.generaciones_con_ia_notificaciones
                )

                nombreGeneracion?.let {
                    historialIAData["nombre_generacion"] = it
                }
                historialIARef.set(historialIAData, SetOptions.merge()).await()
            }

            // ------------------------------------------------------------------
            // ✅ GUARDADO FINAL
            // ------------------------------------------------------------------

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

    fun stringFechaATimestamp(fecha: String): Timestamp {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.isLenient = false
        val date = formato.parse(fecha)!!
        return Timestamp(date)
    }


    suspend fun actualizarEstadoNotificaciones(i: obj_contador_notificaciones) {

        // 1️⃣ Obtener propietario
        val idPropietarioSnap = db.collection("Tiendas")
            .document(i.localida)
            .collection(i.localida)
            .document(i.id_tienda)
            .get()
            .await()

        val propietario_id: List<String> = if (idPropietarioSnap.exists()) {
            val data = idPropietarioSnap.data
            data?.get("propietario_id") as? List<String> ?: emptyList()
        } else {
            emptyList()
        }

        // 2️⃣ Referencia a las notificaciones
        val estadoRef = db.collection("Tiendas")
            .document(i.localida)
            .collection("tiendas_servicios_geinz_activos")
            .document(i.id_tienda)

        val snapshot = estadoRef.get().await()
        val notificaciones = snapshot.get("notificaciones") as? Map<*, *>
        val timestampFinExiste = notificaciones?.containsKey("timestamp_fin") == true
        val propietarioExiste = snapshot.contains("propietario_id")

        if (!snapshot.exists() || !timestampFinExiste) {
            // 🔥 NUEVO CICLO DE NOTIFICACIONES
            val fechaFinString = obtenerFechaConDias(i.fecha_enviada, 7)

            val mapaNotificaciones = hashMapOf(
                "fecha_inicio" to i.fecha_enviada,
                "fecha_fin" to fechaFinString,
                "contador" to 1,
                "maximo" to 3,
                "promocion_nueva" to true,
                "timestamp_fin" to stringFechaATimestamp(fechaFinString)
            )

            // 🔹 Crear mapa a guardar
            val mapaCompleto = hashMapOf<String, Any>(
                "notificaciones" to mapaNotificaciones
            )

            // 🔹 Solo agregar propietario_id si no existe
            if (!propietarioExiste) {
                mapaCompleto["propietario_id"] = propietario_id
            }

            // 🔹 Guardar en Firestore con merge
            estadoRef.set(mapaCompleto, SetOptions.merge()).await()

        } else {
            // 👉 MISMO CICLO → SOLO INCREMENTA
            estadoRef.update(
                "notificaciones.contador",
                FieldValue.increment(1)
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

        val snapshot = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("promociones_geinz")
            .get()
            .await()

        Log.d("PUB_TEST", "Total docs: ${snapshot.size()}")

        for (doc in snapshot.documents) {

            Log.d("PUB_TEST", "DOC ${doc.id} => ${doc.data}")

            val informacion = doc.get("informacion") as? Map<String, Any>

            val imgContainer = doc.get("img_container") as? Map<String, Any>

            val titulo = informacion?.get("titulo") as? String ?: ""
            val descripcion = informacion?.get("descripcion") as? String ?: ""
            val id = informacion?.get("id_promocion") as? String ?: doc.id

            val datos_hora_fecha =
                doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
            val horas = datos_hora_fecha["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
            val fechas = datos_hora_fecha["dias"] as? Map<*, *> ?: emptyMap<String, Any>()
            val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""

            val datosHoraFecha = doc["datos_hora_fecha"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val diasMap = datosHoraFecha["dias"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val horasMap = datosHoraFecha["horas"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val menjsa_predeterminado_whatsap =
                doc.get("mensaje_predeterminado") as? Map<String, Any>
            val wsap_msej = menjsa_predeterminado_whatsap?.get("whatsapp") as? Map<String, Any>
            val wsap = wsap_msej?.get("msje_predermindo") as? String ?: ""
            // 🔹 Obtenemos el timestamp final según tipo (horas o días)

            val timestampFin = when (tipo_hora_dias) {
                "horas" -> (horasMap["timestamp_fin"] as? Timestamp)
                "dias" -> (diasMap["timestamp_fin"] as? Timestamp)
                else -> null
            }


            val tiempo = timestampFin?.let {
                constantes_datos_expirados_fechas_publicaciones.tiempoRestante(
                    it
                )
            } ?: "Expirado"


            val (valorRestante, tipo) = parseDiasHorasRestantes(tiempo)

            val listaImg = imgContainer?.get("lista_img") as? List<*>
            val img = listaImg?.firstOrNull() as? String ?: ""
            if (!tiempo.equals("Expirado")) {
                resultado.add(
                    datos_publicaciones_realizadas(
                        titulo = titulo,
                        descripcion = descripcion,
                        vence_en = tiempo,
                        id = id,
                        img = img, wsap, timestampFin ?: Timestamp.now()
                    )
                )
            }


        }

        return resultado
    }


    suspend fun obtenerPaquetesBasicos(): List<datos_recarga> {
        // 1️⃣ Obtener snapshot de Firestore
        val snapshot = db_sec
            .collection("precios_planes_geinz")
            .get()
            .await()

        val lista = mutableListOf<datos_recarga>()

        // 2️⃣ Recorrer documentos y parsear
        for (doc in snapshot) {
            val data = doc.data

            val accesos = data["accesos"] as? List<String> ?: emptyList()
            val descripcion = data["descripcion"] as? String ?: ""
            val nombre = data["nombre"] as? String ?: ""

            val monedas = (data["monedas"] as? Number)?.toString() ?: "0"
            val monedas_agregadas = (data["monedas_agregadas"] as? Number)?.toString() ?: "0"
            val monedas_inicial = (data["monedas_inicial"] as? Number)?.toString() ?: "0"
            val precio_soles = (data["precio_soles"] as? Number)?.toString() ?: "0"

            lista.add(
                datos_recarga(
                    accesos = accesos,
                    descripcion = descripcion,
                    monedas = monedas,
                    monedas_agregadas = monedas_agregadas,
                    monedas_inicial,
                    nombre_plan = nombre,
                    precio_soles = precio_soles,
                )
            )
        }

        // 3️⃣ Ordenar la lista según nombre_plan
        val listaOrdenada = lista.sortedBy {
            when (it.nombre_plan.uppercase()) {
                "PAQUETE BASICO" -> 1
                "PAQUETE AVANZADO \uD83D\uDD25" -> 2
                "PAQUETE PREMIUM \uD83D\uDC8E" -> 3
                "PAQUETE BUSINESS \uD83D\uDC51" -> 4
                else -> 99
            }
        }

        // 4️⃣ Log para debug
        listaOrdenada.forEach { plan ->
            Log.d(
                "ListaPlanes",
                "${plan.nombre_plan} - ${plan.monedas} monedas - S/${plan.precio_soles}"
            )
        }

        // 5️⃣ Retornar lista ordenada
        return listaOrdenada
    }


    fun parseDiasHorasRestantes(diasRestantesStr: String): Pair<Int, String> {
        // Ejemplos de strings que podrías tener: "3 días restantes" o "5 horas restantes"
        val regex = """(\d+)\s*(día|días|hora|horas)""".toRegex()
        val match = regex.find(diasRestantesStr)
        return if (match != null) {
            val valor = match.groupValues[1].toIntOrNull() ?: 0
            val tipo = if (match.groupValues[2].startsWith("día")) "dias" else "horas"
            valor to tipo
        } else {
            0 to "dias"
        }
    }


    fun obtenerFechaFinDosDias(): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 2) // sumamos 2 días
        return Timestamp(cal.time)
    }

    fun tiempoRestante(timestampFin: Long): String {
        val ahoraMs = System.currentTimeMillis()
        val diffMs = timestampFin - ahoraMs

        if (diffMs <= 0) return "Expirado"

        val totalHoras = diffMs.toDouble() / (1000 * 60 * 60)
        val totalMinutos = diffMs.toDouble() / (1000 * 60)

        return if (totalHoras >= 24) {
            // Mostrar días completos restantes
            val dias = ceil(totalHoras / 24).toLong()  // +1 implícito para el día actual
            "$dias ${if (dias == 1L) "día" else "días"}"
        } else {
            // Mostrar horas y minutos restantes
            val horas = floor(totalHoras).toLong()
            val minutos = floor(totalMinutos % 60).toLong()
            when {
                horas > 0 && minutos > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} y $minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                horas > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} restantes"
                minutos > 0 -> "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                else -> "Menos de un minuto restante"
            }
        }
    }


}
package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.DatosPublicidadIA
import com.geinzz.geinzwork.data.model.PreciosApp
import com.geinzz.geinzwork.data.model.agregar_promociones
import com.geinzz.geinzwork.data.model.data_whatsapp_info
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.datos_generaciones_sin_publicaicones
import com.geinzz.geinzwork.data.model.datos_notificacion
import com.geinzz.geinzwork.data.model.datos_publicaciones_realizadas
import com.geinzz.geinzwork.data.model.datos_recarga

import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.generacion_primarios
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.nuevas_notificaciones
import com.geinzz.geinzwork.data.model.servicio_comodidad
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen_cinco
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.generarIdImagen_nueve
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenParaWhatsappDB
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.model.repo_generaciones_IA
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.generarIdFirebase
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class viewmodel_eres_socio : ViewModel() {


    val instace_repo = repo_eres_socio()


    var promocionId by mutableStateOf("")
        private set


    var notificacion_ID by mutableStateOf("")
        private set


    // El privado es el que puedes modificar dentro del ViewModel
    private val _estado_subido_desc_para_bot = MutableStateFlow(false)

    // El público es el que observas desde el Composable (UI)
    val estado_subido_desc_para_bot: StateFlow<Boolean> = _estado_subido_desc_para_bot.asStateFlow()
    // Mutable


    val _tiene_nueva_generacion = MutableStateFlow(false)


    val datos_publicidad_IA_params = MutableStateFlow(DatosPublicidadIA())

    val datos_notificaciones = MutableStateFlow(datos_notificacion())

    private val _preciosState = MutableStateFlow<PreciosApp?>(null)
    val preciosState: StateFlow<PreciosApp?> = _preciosState

    private val _estado_imagen_bot = MutableStateFlow("")
    val estado_imagen_bot: StateFlow<String> = _estado_imagen_bot


    private val _estado_activacion_daniel_pro = MutableStateFlow(false)
    val estado_activacion_daniel_pro: StateFlow<Boolean> = _estado_activacion_daniel_pro



    init {
        obtener_todos_precios()
    }

    fun obtener_todos_precios() {
        viewModelScope.launch {
            try {
                val resultado = instace_repo.obtener_precios_generales()
                _preciosState.value = resultado
            } catch (e: Exception) {
                Log.d("error", "adasdadasdadasd$e")
            } finally {

            }
        }
    }

    fun limpiar_estado(){
        _estado_activacion_daniel_pro.value=false
    }

    fun activar_plan_pro(
        id_tienda: String,
        localidad: String,
        saldo_actual: Int
    ) {
        viewModelScope.launch {
            try {

                _estado_activacion_daniel_pro.value = false // loading/reset

                val ok = instace_repo.crear_copia_saldo_ativar_bot(
                    id_tienda,
                    localidad,
                    saldo_actual
                )

                _estado_activacion_daniel_pro.value = ok

                Log.d("PLAN_PRO_VM", "Resultado PRO: $ok")

            } catch (e: Exception) {
                _estado_activacion_daniel_pro.value = false
                Log.d("errorbot", "$e")
            }
        }
    }
    fun activar_plan_free_boot(
        id_tienda: String,
        localidad: String
    ) {
        viewModelScope.launch {
            try {

                _estado_activacion_daniel_pro.value = false

                val ok = instace_repo.activar_plan_free_bot(
                    id_tienda,
                    localidad
                )

                _estado_activacion_daniel_pro.value = ok

                Log.d("PLAN_FREE_VM", "Resultado FREE: $ok")

            } catch (e: Exception) {
                _estado_activacion_daniel_pro.value = false
                Log.d("errorbot", "$e")
            }
        }
    }

    fun obtener_imange_bot(id_tienda: String) {

        viewModelScope.launch {

            try {

                val imagen =
                    instace_repo.obtener_imange_bot(id_tienda)

                _estado_imagen_bot.value = imagen

            } catch (e: Exception) {

                Log.d("error_imagen_bot", "$e")

                _estado_imagen_bot.value = ""

            }
        }
    }

    private val _obtener_estado_msje_whataspp_bot =
        MutableStateFlow(data_whatsapp_info())

    val obtener_estado_msje_whataspp_bot:
            StateFlow<data_whatsapp_info> =
        _obtener_estado_msje_whataspp_bot

    fun obtener_descripcion_Seo_bot(id_tienda: String) {

        viewModelScope.launch {

            try {

                val res =
                    instace_repo.obtener_descripcion_Seo_whatsapp(id_tienda)

                Log.d("res", "$res")

                _obtener_estado_msje_whataspp_bot.value = res

            } catch (e: Exception) {

                Log.d("error", "adasdadasdadasd$e")

                _obtener_estado_msje_whataspp_bot.value =
                    data_whatsapp_info()

            }
        }
    }


    fun cambiar_numero_msje_algolia(data: String, tipo: String) {
        viewModelScope.launch {
            try {

            } catch (e: Exception) {

            }
        }
    }

    // StateFlow privado (mutable)
    private val _estado_Carga_subacategoria =
        MutableStateFlow<Estado_carga_subcategoiras>(Estado_carga_subcategoiras.idle)

    // StateFlow público (solo lectura)
    val estado_Carga_subacategoria: StateFlow<Estado_carga_subcategoiras> =
        _estado_Carga_subacategoria

    fun setear_datos_notificacion_publicada_IA(i: datos_notificacion) {
        datos_notificaciones.value = i
    }

    fun resetear_valor_estado_whatsapp_subido_y_gemini() {
        _estado_subido_desc_para_bot.value = false
    }


    fun setear_datos_datos_publicada_IA(i: DatosPublicidadIA) {
        datos_publicidad_IA_params.value = i
    }


    fun limpiar_datos_pasados_publcada_IA() {
        val data = DatosPublicidadIA(
            titulo = "",
            descripcion = "",
            whatsapp = "",
            compartir = "",
            tipo_redirigido = "",
            id_generacion_sin_publicar = null, datos_generaciones_sin_publicaicones()
        )
        datos_publicidad_IA_params.value = data
    }

    fun obtener_subcategorias(cat: String) {
        viewModelScope.launch {
            try {
                val subcateogiras = instace_repo.obtener_subcateogiras(cat)
                if (subcateogiras.isNotEmpty()) {
                    _estado_Carga_subacategoria.value =
                        Estado_carga_subcategoiras.succes(subcateogiras)
                } else {
                    _estado_Carga_subacategoria.value = Estado_carga_subcategoiras.empty
                }
            } catch (e: Exception) {
                Log.d("error_obtener", "$e")
                _estado_Carga_subacategoria.value =
                    Estado_carga_subcategoiras.error("Hubo un error al obtner los resultados")
            }
        }
    }

    fun guardar_cambios_subcateogira(
        idTienda: String,
        localidad: String,
        lista: List<String>
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_cambios_subcategoria_negocio(idTienda, localidad, lista)
            } catch (e: Exception) {
                Log.d("eror_actualziar", "error al actualziar los campos $e")
            }
        }
    }


    fun guarda_lat_lng(lat: Double, lng: Double, id_tienda: String, localidad: String) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_lat_lng(lat, lng, id_tienda, localidad)
            } catch (e: Exception) {
                Log.d("error_al_guardar", "error cordenasd $e")
            }
        }
    }

    fun actualiza_direccion_de_tienda(
        idTienda: String,
        localidad: String,
        dato: String, tipo: String
    ) {
        viewModelScope.launch {
            try {
                when (tipo) {
                    "dir" -> {
                        instace_repo.guardar_direccion_negocio(idTienda, localidad, dato)
                    }

                    "ref" -> {
                        instace_repo.guardar_referencia_negocio(idTienda, localidad, dato)
                    }
                }
            } catch (e: Exception) {
                Log.d("eror_actualziar", "error al actualziar la direccion y referncia $e")

            }
        }

    }


    fun limpiar_datos_pasados_notificaciones_con_IA() {
        datos_notificaciones.value = datos_notificacion()
    }


    fun guadardar_descripcion_whattsapp_bot(
        id_tienda: String,
        localidad: String,
        texto: String,
        tipo: String
    ) {

        viewModelScope.launch {

            Log.d(
                "dadadadd",
                "$id_tienda , $localidad , $texto , $tipo"
            )

            try {

                _estado_subido_desc_para_bot.value =
                    instace_repo.agregar_numero_mesje(
                        id_tienda = id_tienda,
                        data = texto,
                        tipo = tipo
                    )

            } catch (e: Exception) {

                Log.d("error_data", "$e")

                _estado_subido_desc_para_bot.value = false
            }
        }
    }


    fun generarIdNotificacion(
        idForzado: String?,
        idPublicacion: String,
        tipo: String
    ) {

        notificacion_ID = when {
            !idForzado.isNullOrBlank() -> {
                Log.d("ID_NOTI", "Usando ID FORZADO: $idForzado")
                idForzado
            }

            idPublicacion.isNotBlank() -> {
                val id = generarIdImagen_nueve()
                Log.d("ID_NOTI", "Generado por publicación (9 dígitos): $id")
                id
            }

            tipo == "informativas" -> {
                val id = generarIdImagen_cinco()
                Log.d("ID_NOTI", "Generado por tipo informativa (5 dígitos): $id")
                id
            }

            else -> {
                val id = generarIdImagen()
                Log.d("ID_NOTI", "Generado normal (7 dígitos): $id")
                id
            }
        }

        Log.d(
            "ID_NOTI_FINAL",
            "ID_FINAL=$notificacion_ID | forzado=$idForzado | publicacion=$idPublicacion | tipo=$tipo"
        )


    }


    fun iniciarPromo(idExterno: String?) {
        promocionId = if (!idExterno.isNullOrBlank()) {
            idExterno
        } else {
            generarIdFirebase()
        }

        Log.d("PROMO_ID", "Promo iniciada con ID: $promocionId")
    }

    fun limpiar_id_selecionadanotificacion() {
        notificacion_ID = ""
    }

    fun limpiarId() {
        promocionId = ""
        _tiene_nueva_generacion.value = false
    }


    fun verificar_si_tiene_nueva_generacion(valor: Boolean) {
        _tiene_nueva_generacion.value = valor
    }


    private val _state_eres_socio = MutableStateFlow<carga_acces_socio>(carga_acces_socio.idle)
    val state_eres_socio: StateFlow<carga_acces_socio> = _state_eres_socio

    private val _seguidores_obtenidos =
        MutableStateFlow<EstadoSeguidores>(EstadoSeguidores.Cargando)
    val seguidores_obtenidos: StateFlow<EstadoSeguidores> = _seguidores_obtenidos


    private val _fecha_finalizar_panel_real_time =
        MutableStateFlow("") // inicializamos con string vacío
    val fecha_finalizar_panel_real_time = _fecha_finalizar_panel_real_time.asStateFlow()


    private val _lista_publicaciones =
        MutableStateFlow<List<datos_publicaciones_realizadas>>(emptyList())
    val lista_publicaciones: StateFlow<List<datos_publicaciones_realizadas>> = _lista_publicaciones

    private val _estadoPaquetes =
        MutableStateFlow<CargaPaquetesPago>(CargaPaquetesPago.Loading)

    val estadoPaquetes: StateFlow<CargaPaquetesPago> = _estadoPaquetes


    private val _imgAmbientales = MutableStateFlow<List<String>>(emptyList())
    val imgAmbientales: StateFlow<List<String>> = _imgAmbientales

    private val _imgServicios = MutableStateFlow<List<String>>(emptyList())
    val imgServicios: StateFlow<List<String>> = _imgServicios

    private val _imgPromociones = MutableStateFlow<List<String>>(emptyList())
    val imgPromociones: StateFlow<List<String>> = _imgPromociones


    private val _state_cerrar = MutableStateFlow<Boolean>(false)
    val state_cerrar: StateFlow<Boolean> = _state_cerrar

    private val _state_abierto = MutableStateFlow<Boolean>(false)
    val state_abierto: StateFlow<Boolean> = _state_abierto

    private var listenerDatosTienda: ListenerRegistration? = null

    private val _verificar_seccion_tienda =
        MutableStateFlow<Triple<Boolean, String, String?>>(Triple(false, "", null))
    val verificarSeccion = _verificar_seccion_tienda.asStateFlow()


    private val _idSocio = MutableStateFlow("")
    val idSocio = _idSocio.asStateFlow()

    private val _cargandoIdSocio = MutableStateFlow(true)
    val cargandoIdSocio = _cargandoIdSocio.asStateFlow()

    private val _subidaPromoState =
        MutableStateFlow<SubidaPromoState>(SubidaPromoState.Idle)

    val subidaPromoState = _subidaPromoState.asStateFlow()


//    private val _estado_envio_notificaciones = MutableStateFlow("")
//    val estado_envio_notificaciones = _estado_envio_notificaciones.asStateFlow()

//    private val _estado_envio_recientes = MutableStateFlow(false)
//    val estado_envio_recientes = _estado_envio_recientes.asStateFlow()
//

    private val _esta_vinculado = MutableStateFlow(false)
    val esta_vinculado = _esta_vinculado.asStateFlow()

//    fun cambiar_Estado_reciente(estado: Boolean) {
//        _estado_envio_recientes.value = estado
//    }


    fun cargarIdSocio(context: Context) {
        _cargandoIdSocio.value = true
        Log.d("CargarIdSocio", "Inicio de la función. _cargandoIdSocio = true")

        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            Log.d("CargarIdSocio", "Marca de tiempo de inicio: $inicio")
            viewModelScope.launch {
                combine(
                    data_store_localidad.get_localidad_tienda_socio(context),
                    data_store_localidad.get_id_socio(context)
                ) { localidad, idSocio -> Pair(localidad, idSocio) }
                    .collect { (localidad, idSocio) ->

                        Log.d("CargarIdSocio", "Valor recibido de DataStore: $idSocio")
                        _idSocio.value = idSocio

                        if (idSocio.isNotEmpty()) {
                            Log.d("CargarIdSocio", "Valor no vacío, se llama a verificar_seccion")
                            verificar_seccion(context, idSocio, localidad)
                        } else {
                            Log.d("CargarIdSocio", "Valor vacío, no se verifica sección")
                        }

                        val tiempoTranscurrido = System.currentTimeMillis() - inicio
                        val faltante = 4000 - tiempoTranscurrido

                        if (faltante > 0) {
                            delay(faltante)
                        }

                        _cargandoIdSocio.value = false
                    }
            }
        }
    }

    /**
     * Para resetear el estado de la pantalla antes de logear
     */
//    fun resetearEstado() {
//        _cargandoIdSocio.value = false
//    }


    fun descontar_puntos(
        viewmodel_recargas: viewmodel_recargas,
        saldo_tienda: Int,
        nombre_tienda: String,
        localidad_tienda: String,
        id_tienda: String,
        puntos_descuento: Int,
        meses_agregados: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.restar_puntos(
                    localidad_tienda,
                    id_tienda,
                    puntos_descuento,
                    meses_agregados
                )
                val historial_descuento = historial_descuento(
                    tipo_transaccion = "descuento",
                    fecha = obtenerFechaActual(),
                    hora = obtenerHoraActual(),
                    id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                    localidad_tienda = localidad_tienda,
                    id_tienda = id_tienda,
                    nombre_tienda = nombre_tienda,
                    monto_descuento = puntos_descuento.toString(),
                    tipo = "Panel activo por $meses_agregados",
                    precio_soles = constantes_cobro_monedas.calcular_precio_soles(puntos_descuento.toString())
                        .toString(),
                    estado = "Aceptado",
                    monto_restante = saldo_tienda - puntos_descuento.toInt()
                )
                viewmodel_recargas.restar_puntos_recarga(
                    historial_descuento,
                    "0",
                    id_tienda,
                    localidad_tienda
                )

            } catch (e: Exception) {
                Log.d("Error_canjear", "error al cambiar el cange")
            }
        }
    }


//    fun enviar_notificacion(id_user: String) {
//        Log.d("envimaorns_ntoi","notifioneviada")
//        viewModelScope.launch {
//            try {
//                enviar_notificacion_lista_dispo(
//                    id_promo = "",
//                    id_tienda = "",
//                    localidad = "",
//                    categora_tienda = "",
//                    tipo_notificacion_params = "screen",
//                    id_users = listOf(id_user),
//                    titulo = "\uD83C\uDFAF ¡A punto de quedarte sin monedas!",
//                    txt = "Te quedan pocas monedas. ¡No dejes que tu alcance se detenga! \uD83D\uDD14✨",
//                    logo_tienda = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
//                    tipo_notificacion = "Basico",
//                    url_img = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/walpaper_geinz%2Fturisticos%2Fimg11.webp?alt=media&token=1151dd65-8a6b-497d-a452-a8d948859422",
//                    prioridad = "high"
//
//                )
//            } catch (e: Exception) {
//
//            }
//        }
//    }

    fun verificar_existencia_tienda(
        id_user: String,
        ingresa_correo: Boolean,
        correo_tienda: String,
        id_tienda: String,
        localidad_tienda: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.verificar_existencia_tienda(
                    id_user,
                    ingresa_correo,
                    correo_tienda,
                    id_tienda,
                    localidad_tienda
                ) { existe, msje, idConfirmado ->
                    _verificar_seccion_tienda.value = Triple(existe, msje, idConfirmado)
                }
            } catch (e: Exception) {
                _verificar_seccion_tienda.value = Triple(false, "Error al verificar tu id", null)
            }
        }
    }


    fun verificar_cuenta_vinculada(id_user: String, id_tienda: String, localidad: String) {
        viewModelScope.launch {
            try {
                instace_repo.verificarVinculadoRealtime(
                    id_user,
                    id_tienda,
                    localidad,
                    { vinculado ->
                        Log.d("valor_resultad", "$vinculado  $id_user $id_tienda  $localidad")
                        _esta_vinculado.value = vinculado
                    })
            } catch (e: Exception) {
                _esta_vinculado.value = false
                Log.d("error_iniciar", "error al realizar los cambios")
            }
        }

    }

    fun cambiar_estado_Seccion() {
        _verificar_seccion_tienda.value = Triple(false, "", null)
    }

    fun verificar_seccion(context: Context, id_tienda: String, localidad_tienda: String) {

        listenerDatosTienda?.remove()

        _state_eres_socio.value = carga_acces_socio.loading

        listenerDatosTienda = instace_repo.escuchar_datos_tienda(
            localidad_tienda,
            id_tienda,
            resultado = { datos ->
                viewModelScope.launch {
                    if (datos.nombre.isNotEmpty()) {
                        _state_eres_socio.value = carga_acces_socio.succes(datos)
                        obtener_fecha_fin_en_tiempo_real(id_tienda, localidad_tienda)
                    } else {
                        _state_eres_socio.value = carga_acces_socio.error("No se encontraron datos")
                    }
                }
            },
            error = { e ->
                viewModelScope.launch {

                    _state_eres_socio.value =
                        carga_acces_socio.error("Error: ${e.message}")
                }
            }
        )
    }


    fun obtener_fecha_fin_en_tiempo_real(id_tienda: String, localidad: String) {
        viewModelScope.launch {
            try {
                instace_repo.fechaFinTiendaPanel(id_tienda, localidad, { res ->
                    if (res.isNotEmpty()) {
                        _fecha_finalizar_panel_real_time.value = res
                        Log.d("fechas_otbenideosa", res)
                    }
                })
            } catch (e: Exception) {
                Log.d("error", "error al obtner la fecha")
            }
        }
    }

    fun cambiar_cerrado(
        id_tienda: String,
        dia: String,
        motivo: String,
        bloques: List<Map<String, String>>
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_horario_cerrado(id_tienda, dia, motivo, bloques)
                _state_cerrar.value = true
            } catch (e: Exception) {
                _state_cerrar.value = false
                Log.d("error", "$e")

            }
        }

    }

    fun cambiar_abierto(
        id_tienda: String,
        dia: String,
        bloques: List<Map<String, String>>
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_horario_atencion_abierto(id_tienda, dia, bloques)
                _state_abierto.value = true
            } catch (e: Exception) {
                _state_abierto.value = false
                Log.d("error", "$e")

            }

        }
    }


    fun carga_img_tipo(tipo: String, idTienda: String) {
        viewModelScope.launch {
            instace_repo.obtner_img_stoprage_cambios(tipo, idTienda) { lista ->
                when (tipo) {
                    "ambientales" -> _imgAmbientales.value = lista
                    "servicios_productos" -> _imgServicios.value = lista
                    "promociones" -> _imgPromociones.value = lista
                }
            }
        }
    }

    fun cambiar_metodos_pago_tienda(
        tipo: String,
        id_tienda: String, localidad_tienda: String, metodo_pago: String,
        valor_cambiado: Boolean
    ) {
        viewModelScope.launch {
            try {
                when (tipo) {
                    "pago" -> {
                        instace_repo.cambiar_pagos_tienda(
                            id_tienda,
                            localidad_tienda,
                            metodo_pago,
                            valor_cambiado
                        )
                    }

                    "contacto" -> {
                        instace_repo.cambiar_contacto_redes(
                            id_tienda,
                            localidad_tienda,
                            metodo_pago,
                            valor_cambiado
                        )
                    }

                }
            } catch (e: Exception) {
                Log.d("datos cambiado", "$e")
            }
        }
    }

    fun cambiar_contacto_redes(
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String,
        titular: String,
        valor: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.cambiar_NT_metodo_contacto(
                    id_tienda,
                    localidad_tienda,
                    metodo_pago,
                    titular,
                    valor
                )
            } catch (e: Exception) {
                Log.d("datos cambiado", "$e")
            }
        }
    }

    fun cambiar_nombre_descripcion(
        localidad_tienda: String,
        id_tienda: String,
        tipo: String,
        cambio: String,
    ) {
        viewModelScope.launch {
            try {
                instace_repo.cambiar_nombre_descripcion(localidad_tienda, id_tienda, tipo, cambio)
            } catch (e: Exception) {
                Log.d("datos cambiado", "$e")
            }
        }
    }

    fun guardar_aforo(
        numero: String,
        id_tienda: String,
        localidad_tienda: String,
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_aforo(numero, id_tienda, localidad_tienda)
            } catch (e: Exception) {
                Log.d("datos cambiado", "$e")
            }
        }
    }


    fun cambiar_titular_yape_plin(
        context: Context,
        uri: Uri,
        id_tienda: String,
        localidad_tienda: String,
        metodo_pago: String,
        titular: String,
        numero_cambiado: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.cambiar_NT_yape_plin(
                    context,
                    id_tienda,
                    localidad_tienda,
                    metodo_pago,
                    titular,
                    numero_cambiado,
                    uri
                )
            } catch (e: Exception) {
                Log.d("datos cambiado", "$e")
            }
        }
    }


    //    @RequiresApi(Build.VERSION_CODES.R)
//    fun subir_img_firestore_promociones(
//        i: agregar_promociones,
//        img_tienda: String,
//        localidad: String,
//        context: Context,
//        imagenes: List<ImagenReview>,
//        idSocio: String,
//        idPromo: String
//    ) {
//        viewModelScope.launch {
//            _subidaPromoState.value = SubidaPromoState.Loading
//
//            try {
//                // 1️⃣ Subir imágenes
//                val urls = instace_repo.subirImagenesAFirebase(
//                    context = context,
//                    imagenes = imagenes,
//                    idSocio = idSocio,
//                    idPromo = idPromo
//                )
//
//                // 2️⃣ Guardar URLs en Firestore
//              val res_completado=  instace_repo.guardarImagenesEnFirestore_promociones(
//                    idSocio,
//                    img_tienda,
//                    localidad = localidad,
//                    idPromo = idPromo,
//                    urls = urls
//                )
//
//                if (res_completado.isSuccess) {
//                _subidaPromoState.value = SubidaPromoState.Success
//                    crear_promociones(i,localidad)
//                } else {
//                    _subidaPromoState.value = SubidaPromoState.Error("ocurrio un error en el proceso")
//                }
//                // ✅ TODO OK
//
//
//            } catch (e: Exception) {
//                Log.e("error_agregado", "Error al subir imágenes", e)
//                _subidaPromoState.value =
//                    SubidaPromoState.Error("Error al subir la promoción")
//            }
//        }
//    }
    fun subir_img_firestore_promociones(
        i: agregar_promociones,
        img_tienda: String,
        localidad: String,
        context: Context,
        imagenes: List<ImagenReview>,
        idSocio: String,
        idPromo: String
    ) {
        viewModelScope.launch {
            _subidaPromoState.value = SubidaPromoState.Loading

            try {
                // 🔒 1. Validación obligatoria
                if (imagenes.isEmpty()) {
                    _subidaPromoState.value =
                        SubidaPromoState.Error("Debes subir al menos una imagen")
                    return@launch
                }

                // 🔄 2. Subir imágenes CON REINTENTO
                val (urls, img_bot) = instace_repo.subirImagenesConReintento(intentos = 3) {
                    instace_repo.subirImagenesAFirebase(
                        context = context,
                        imagenes = imagenes,
                        idSocio = idSocio,
                        idPromo = idPromo
                    )
                }


                // 🔒 3. Verificación estricta
                if (urls.size != imagenes.size) {
                    _subidaPromoState.value =
                        SubidaPromoState.Error("No se pudieron subir todas las imágenes")
                    return@launch
                }

                // 💾 4. Guardar URLs en Firestore
                val resCompletado =
                    instace_repo.guardarImagenesEnFirestore_promociones(
                        id_tienda = idSocio,
                        logo_tienda = img_tienda,
                        localidad = localidad,
                        idPromo = idPromo,
                        urls = urls
                    )
                if (!resCompletado.isSuccess) {
                    _subidaPromoState.value =
                        SubidaPromoState.Error("Error al guardar las imágenes")
                    return@launch
                } else {
                    // ✅ 5. Crear promoción SOLO si TODO salió bien
                    val resPromo = crear_promociones(img_bot, urls, i, localidad)

                    if (!resPromo.isSuccess) {
                        _subidaPromoState.value =
                            SubidaPromoState.Error("Las imágenes se subieron, pero la promoción no se creó")
                        return@launch
                    }

                    _subidaPromoState.value = SubidaPromoState.Success
                }


            } catch (e: Exception) {
                Log.e("SUBIDA_PROMO", "Error crítico al subir imágenes", e)
                _subidaPromoState.value =
                    SubidaPromoState.Error("No se pudo subir la promoción. Inténtalo nuevamente.")
            }
        }
    }


    fun resetear_Estado_promo_subida() {
        _subidaPromoState.value = SubidaPromoState.Idle
    }


    fun cambiar_atrubitos(
        id_tienda: String,
        localidad_tienda: String, nombre_atributo: String, nombre_estado: Boolean,
    ) {
        viewModelScope.launch {
            try {
                instace_repo.cambiar_atributos_tiendas(
                    id_tienda,
                    localidad_tienda,
                    nombre_atributo,
                    nombre_estado,
                )
            } catch (e: Exception) {
                Log.d("datos cambiado", "$e")
            }
        }
    }

    private val db = FirebaseFirestore.getInstance()


    suspend fun crear_promociones(
        img_bot: String?,
        lista_img_subida: List<String>,
        i: agregar_promociones,
        localidad: String
    ): Result<Unit> {
        val tieneNueva = _tiene_nueva_generacion.value
        val datos_si_paso_IA = datos_publicidad_IA_params.value
        return instace_repo.crear_promocion(
            img_bot,
            datos_si_paso_IA,
            tieneNueva,
            lista_img_subida,
            i,
            localidad
        )
    }


    fun agregar_generacions_obligatorias_subidas(
        localidad: String,
        id_tienda: String,
        i: generacion_primarios,
        tipo: String, id_generacion: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guaradar_generacion_normal_por_7_dias(
                    id_generacion,
                    localidad,
                    id_tienda,
                    i,
                    tipo
                )
            } catch (e: Exception) {
                Log.d("Error_al_agregar", "$e")
            }
        }
    }

    fun agregar_generacions_obligatorias_subidas_notificaciones(
        localidad: String,
        id_tienda: String,
        i: generacion_primarios,
        id_generacion: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guaradar_generacion_normal_por_7_dias_notificaciones(
                    id_generacion,
                    localidad,
                    id_tienda,
                    i,

                    )
            } catch (e: Exception) {
                Log.d("Error_al_agregar", "$e")
            }
        }
    }


    fun actualizarWhatsapp(
        id_tienda: String,
        localidad: String,
        idGeneracion: String,
        mensaje: String
    ) {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("gen_con_IA_historial")
            .document(idGeneracion)

        val hashMap = hashMapOf<String, Any>(
            "generacions_con_IA" to hashMapOf(
                "generacion_wsap" to mensaje
            )
        )

        ref.set(
            hashMap,
            SetOptions.merge()
        )
    }

    fun actualizarCompartir(
        id_tienda: String,
        localidad: String,
        idGeneracion: String,
        mensaje: String
    ) {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("gen_con_IA_historial")
            .document(idGeneracion)

        val hashMap = hashMapOf<String, Any>(
            "generacions_con_IA" to hashMapOf(
                "generacion_compartir" to mensaje
            )
        )

        ref.set(
            hashMap,
            SetOptions.merge()
        )

    }


    fun obtener_lista_seguidores(localidad: String, id_tienda: String) {
        viewModelScope.launch {
            _seguidores_obtenidos.value = EstadoSeguidores.Cargando
            try {
                val lista = instace_repo.obtenerSeguidoresTienda(localidad, id_tienda)

                _seguidores_obtenidos.value = when {
                    lista.isEmpty() -> EstadoSeguidores.Vacio
                    lista.size < 10 -> EstadoSeguidores.NoCumpleMinimo(lista.size)
                    else -> EstadoSeguidores.Exito(lista)
                }

            } catch (e: Exception) {
                Log.d("error_envio_noti", "error al obtener los seguidores", e)
                _seguidores_obtenidos.value =
                    EstadoSeguidores.Error(e.message ?: "Error desconocido")
            }
        }
    }


    fun obtner_publicaciones_subidas(id_tienda: String, localidad: String) {
        viewModelScope.launch {
            try {
                val listaPublicaicones =
                    instace_repo.obtener_publicaciones_tiendas(localidad, id_tienda)
                if (listaPublicaicones.isNotEmpty()) {
                    _lista_publicaciones.value = listaPublicaicones
                } else {
                    _lista_publicaciones.value = emptyList()
                }
            } catch (e: Exception) {
                Log.d("error_publicaiones", "error al obtener las pblicaicoens")
            }
        }
    }


    fun obtener_precios_paquetes() {
        viewModelScope.launch {
            _estadoPaquetes.value = CargaPaquetesPago.Loading
            try {
                val lista = instace_repo.obtenerPaquetesBasicos()
                _estadoPaquetes.value = CargaPaquetesPago.Success(lista)
            } catch (e: Exception) {
                _estadoPaquetes.value =
                    CargaPaquetesPago.Error(e.message ?: "Error al obtener los planes123")
            }
        }
    }

    fun prepararInputParaIA(
        listaSubcategorias: List<String>,
        pagos: modelo_pagos_tienda,
        comodidades: List<servicio_comodidad>
    ): String {
        val elementos = mutableListOf<String>()

        // 1. Subcategorías
        elementos.addAll(listaSubcategorias)

        // 2. Pagos (Evaluación individual para evitar que se pisen)
        if (pagos.yape.enable) elementos.add("Yape")
        if (pagos.plin.enable) elementos.add("Plin")
        if (pagos.efectivo.enable) elementos.add("Efectivo")
        if (pagos.visa_mastercard.enable) elementos.add("Tarjeta")
        if (pagos.agora.enable) elementos.add("Agora")

        // 3. Comodidades
        val comodidadesActivas = comodidades
            .filter { it.estado }
            .map { it.nombre }
        elementos.addAll(comodidadesActivas)

        // 4. Resultado limpio
        return elementos
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
    }


    sealed class CargaPaquetesPago {
        object Loading : CargaPaquetesPago()
        data class Success(val datos: List<datos_recarga>) : CargaPaquetesPago()
        data class Error(val txt: String) : CargaPaquetesPago()
    }


    sealed class carga_acces_socio {
        object idle : carga_acces_socio()
        data class succes(val datos: datos_tienda) : carga_acces_socio()
        data class error(val txt: String) : carga_acces_socio()
        object loading : carga_acces_socio()
    }

    sealed class SubidaPromoState {
        object Idle : SubidaPromoState()
        object Loading : SubidaPromoState()
        object Success : SubidaPromoState()
        data class Error(val msg: String) : SubidaPromoState()
    }


    sealed class EstadoEnvioNotificacion {
        object Loading : EstadoEnvioNotificacion()
        data class enviado(val enviadotxt: String) : EstadoEnvioNotificacion()
        object LimiteAlcanzado : EstadoEnvioNotificacion()
        data class Error(val mensaje: String) : EstadoEnvioNotificacion()
    }


    sealed class EstadoSeguidores {
        object Cargando : EstadoSeguidores()
        data class Exito(val seguidores: List<String>) : EstadoSeguidores()
        object Vacio : EstadoSeguidores()
        data class Error(val mensaje: String) : EstadoSeguidores()
        data class NoCumpleMinimo(val cantidad: Int) :
            EstadoSeguidores() // nuevo estado si hay menos de 10 seguidores
    }

    sealed class Estado_carga_subcategoiras {
        object cagando : Estado_carga_subcategoiras()
        data class succes(val lista_datos: List<String>) : Estado_carga_subcategoiras()
        object empty : Estado_carga_subcategoiras()
        data class error(val error: String) : Estado_carga_subcategoiras()
        object idle : Estado_carga_subcategoiras()
    }

    companion object


}
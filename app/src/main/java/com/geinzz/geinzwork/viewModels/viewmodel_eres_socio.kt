package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.agregar_promociones
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.datos_publicaciones_realizadas
import com.geinzz.geinzwork.data.model.datos_recarga

import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.obj_contador_notificaciones
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.model.repo_recargas
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class viewmodel_eres_socio : ViewModel() {

    val instace_repo = repo_eres_socio()
    val insta_repo = repo_recargas()

    private val _state_eres_socio = MutableStateFlow<carga_acces_socio>(carga_acces_socio.idle)
    val state_eres_socio: StateFlow<carga_acces_socio> = _state_eres_socio

    private val _seguidores_obtenidos = MutableStateFlow<List<String>>(emptyList())
    val seguidores_obtenidos: StateFlow<List<String>> = _seguidores_obtenidos



    private val _fecha_finalizar_panel_real_time = MutableStateFlow("") // inicializamos con string vacío
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

    fun generarIdRecarga(): String {
        return UUID.randomUUID().toString()
    }


    fun descontar_puntos(
        viewmodel_recargas:viewmodel_recargas,
        saldo_tienda:Int,
        nombre_tienda:String,
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
                        obtener_fecha_fin_en_tiempo_real(id_tienda,localidad_tienda)
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


    fun obtener_fecha_fin_en_tiempo_real(id_tienda: String,localidad: String){
        viewModelScope.launch {
            try {
                instace_repo.fechaFinTiendaPanel(id_tienda,localidad,{res->
                    if(res.isNotEmpty()){
                        _fecha_finalizar_panel_real_time.value=res
                        Log.d("fechas_otbenideosa",res)
                    }
                })
            }catch (e: Exception){
                Log.d("error","error al obtner la fecha")
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

    @RequiresApi(Build.VERSION_CODES.R)
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


    @RequiresApi(Build.VERSION_CODES.R)
    fun subir_img_firestore_promociones(
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
                // 1️⃣ Subir imágenes
                val urls = instace_repo.subirImagenesAFirebase(
                    context = context,
                    imagenes = imagenes,
                    idSocio = idSocio,
                    idPromo = idPromo
                )

                // 2️⃣ Guardar URLs en Firestore
                instace_repo.guardarImagenesEnFirestore_promociones(
                    idSocio,
                    img_tienda,
                    localidad = localidad,
                    idPromo = idPromo,
                    urls = urls
                )

                // ✅ TODO OK
                _subidaPromoState.value = SubidaPromoState.Success


            } catch (e: Exception) {
                Log.e("error_agregado", "Error al subir imágenes", e)
                _subidaPromoState.value =
                    SubidaPromoState.Error("Error al subir la promoción")
            }
        }
    }

    fun resetear_Estado_promo_subida(){
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


    fun crear_promociones(
        i: agregar_promociones,
        localidad: String
    ) {
        viewModelScope.launch {
            try {
                val resultado = instace_repo.crear_promocion(i, localidad)

            } catch (e: Exception) {
                Log.d("error", "error al crear la publicacion")
            }
        }
    }


    fun obtener_lista_seguidores(localidad: String, id_tienda: String) {
        viewModelScope.launch {
            try {
                _seguidores_obtenidos.value =
                    instace_repo.obtenerSeguidoresTienda(localidad, id_tienda)
            } catch (e: Exception) {
                Log.d("error_envio_noti", "error al obtener los seguidores")
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


}
package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.NotificacionIA
import com.geinzz.geinzwork.data.model.OpcionPromocionIA

import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.obj_contador_notificaciones
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import com.geinzz.geinzwork.model.repo_recargas

import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_pantallas_promocionar : ViewModel() {
    val palabrasBloqueadas = listOf(
        "sexo",
        "porno",
        "escort",
        "droga",
        "arma",
        "casino",
        "apuesta",
        "elecciones",
        "política",
        "voten por",
        "alcalde",
        "presidente",
        "viva",
        "inbox",
        "privado",
        "dm",
        "háblame por whatsapp",
        "whatsapp al",
        "escríbeme al",
        "manda dm",
        "contacto directo",
        "huevón",
        "huevona",
        "cojudo",
        "cojuda",
        "pendejo",
        "pendeja",
        "mierda",
        "concha",
        "conchesumadre",
        "culiao",
        "culiada",
        "imbécil",
        "idiota",
        "estúpido",
        "estúpida",
        "malparido",
        "malparida",
        "carajo",
        "puta",
        "puto",
        "puta madre",
        "coño",
        "concha e tu madre",
        "hijo de puta",
        "marico",
        "marica",
        "mierda",
        "maldito",
        "cabrón",
        "cabróna",
        "pendejo",
        "pendeja"
    )
    val insta_repo = repo_pantallas_promocionar()
    val viewmodel_recargas = viewmodel_recargas()

    private val _estadoImagen = MutableStateFlow<ImagenEstado>(ImagenEstado.Idle)
    val estadoImagen: StateFlow<ImagenEstado> = _estadoImagen

    @RequiresApi(Build.VERSION_CODES.O)
    val insta_repo_eres_socio = repo_eres_socio()
    private val palabrasBloqueadasNormalizadas: List<Pair<String, Regex>> =
        palabrasBloqueadas.map { palabra ->
            val normalizada = normalizarTexto(palabra)
            val regex = Regex("\\b$normalizada\\b")
            palabra to regex
        }
    private val _estado_promociones_ia =
        MutableStateFlow<EstadoIA>(EstadoIA.Idle)
    val estado_promociones_ia: StateFlow<EstadoIA> =
        _estado_promociones_ia


    private val _estado_notificacion_con_ia_corta =
        MutableStateFlow<EstadoIA_notifi_corta>(EstadoIA_notifi_corta.Idle)

    val estado_notificaion_con_ia_corta: StateFlow<EstadoIA_notifi_corta> =
        _estado_notificacion_con_ia_corta


    private val _estado_texto_whatsap_con_ia =
        MutableStateFlow<ESstado_ia_msje_whatsap>(ESstado_ia_msje_whatsap.Idle)

    val estado_texto_whatsap_con_ia: StateFlow<ESstado_ia_msje_whatsap> =
        _estado_texto_whatsap_con_ia

    private val _estado_texto_compartir_con_ia =
        MutableStateFlow<ESstado_ia_msje_compartir>(ESstado_ia_msje_compartir.Idle)

    val estado_texto_compatir_con_ia: StateFlow<ESstado_ia_msje_compartir> =
        _estado_texto_compartir_con_ia

    private val _estado_envio_notificaciones = MutableStateFlow("")
    val estado_envio_notificaciones = _estado_envio_notificaciones.asStateFlow()

    private val _estado_envio_recientes = MutableStateFlow(false)
    val estado_envio_recientes = _estado_envio_recientes.asStateFlow()

    fun cambiar_Estado_reciente(estado: Boolean) {
        _estado_envio_recientes.value = estado
    }

    private val _estadoValidacion =
        MutableStateFlow<EstadoValidacionNotificacion>(
            EstadoValidacionNotificacion.Idle
        )

    val estadoValidacion: StateFlow<EstadoValidacionNotificacion> =
        _estadoValidacion


    fun mejorar_texto_con_promo_IA(
        saldo_tienda: Int,
        localidad_tienda: String,
        id_tienda: String,
        nombre_tienda: String,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
        diasRestantes: Int
    ) {
        viewModelScope.launch {
            _estado_promociones_ia.value = EstadoIA.Loading

            try {
                if (saldo_tienda < 30) {
                    _estado_promociones_ia.value = EstadoIA.Error("Saldo insuficiente")
                    return@launch
                }
                val lista = insta_repo.generar_promociones_con_IA(
                    tituloUsuario,
                    descripcionUsuario,
                    nombreTienda,
                    localidad,
                    diasRestantes
                )

                if (lista.isNotEmpty()) {
                    _estado_promociones_ia.value = EstadoIA.Success(lista)
                    val historial_descuento = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = viewmodel_recargas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = "30",
                        tipo = "Gen IA (Promociones X3)",
                        precio_soles = viewmodel_recargas.calcular_precio_soles("30")
                            .toString(), estado = "Aceptado", monto_restante = saldo_tienda - 30
                    )
                    viewmodel_recargas.restar_puntos_recarga(
                        historial_descuento,
                        "30",
                        id_tienda,
                        localidad_tienda
                    )
                } else {
                    _estado_promociones_ia.value =
                        EstadoIA.Error("No se pudo generar contenido")
                }

            } catch (e: Exception) {
                _estado_promociones_ia.value =
                    EstadoIA.Error("Error al generar con IA")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun enviar_notificacion(
        saldo_tienda: Int,
        localidad_tienda: String,
        nombre_tienda: String,
        id_tienda: String,
        descontar_monedas: String,
        usuarios: List<String>,
        i: obj_contador_notificaciones
    ) {
        viewModelScope.launch {
            try {
                if (saldo_tienda < descontar_monedas.toInt()) {
                    _estado_envio_notificaciones.value = "saldo insuficiente"
                    return@launch
                }
                val estado_notificacion =
                    insta_repo_eres_socio.verificar_envio_notificaciones(i.localida, i.id_tienda)
                if (estado_notificacion) {
                    insta_repo_eres_socio.agregarContadorNotificacion(usuarios, i)
                    _estado_envio_notificaciones.value = "Notificaciones enviadas correctamente"
                    _estado_envio_recientes.value = true
                    val historial_descuento = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = viewmodel_recargas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = descontar_monedas,
                        tipo = "Envio de notificaciones a ${usuarios.size} seguidores (Actual)",
                        precio_soles = viewmodel_recargas.calcular_precio_soles(descontar_monedas)
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - descontar_monedas.toInt()
                    )
                    viewmodel_recargas.restar_puntos_recarga(
                        historial_descuento,
                        descontar_monedas,
                        id_tienda,
                        localidad_tienda
                    )
                } else {
                    _estado_envio_notificaciones.value =
                        "superaste el maximo de notificaciones semanales"
                }
            } catch (e: Exception) {
                _estado_envio_notificaciones.value =
                    "error al enviar las notificaciones"
                Log.d("error_envio_noti", "error al enviar las notificaciones")
            }
        }
    }


    fun mejorar_mejorar_notificacion_con_IA_corta(
        saldo_tienda: Int,
        localidad_tienda: String, id_tienda: String, nombre_tienda: String,
        titulo_publicacion: String,
        descripcion: String
    ) {
        Log.d("titulo_publicacion", "$titulo_publicacion $descripcion")
        viewModelScope.launch {

            _estado_notificacion_con_ia_corta.value =
                EstadoIA_notifi_corta.Loading

            try {
                if (saldo_tienda < 20) {
                    _estado_notificacion_con_ia_corta.value =
                        EstadoIA_notifi_corta.Error("saldo insuficiente")
                    return@launch
                }
                insta_repo.crear_notificacion_conIA_corta(
                    titulo_publicacion,
                    descripcion
                ) { notificacionIA ->
                    _estado_notificacion_con_ia_corta.value =
                        EstadoIA_notifi_corta.Success(notificacionIA)
                    if (notificacionIA.titulo.isNotEmpty() && notificacionIA.descripcion.isNotEmpty()) {
                        val historial_descuento = historial_descuento(
                            tipo_transaccion = "descuento",
                            fecha = obtenerFechaActual(),
                            hora = obtenerHoraActual(),
                            id_recarga = viewmodel_recargas.generarIdRecarga(),
                            localidad_tienda = localidad_tienda,
                            id_tienda = id_tienda,
                            nombre_tienda = nombre_tienda,
                            monto_descuento = "20",
                            tipo = "Gen IA (Notificacion - promo seleccionada)",
                            precio_soles = viewmodel_recargas.calcular_precio_soles("20")
                                .toString(), estado = "Aceptado", monto_restante = saldo_tienda - 20
                        )
                        viewmodel_recargas.restar_puntos_recarga(
                            historial_descuento,
                            "20",
                            id_tienda,
                            localidad_tienda
                        )
                    }
                }

            } catch (e: Exception) {
                _estado_notificacion_con_ia_corta.value =
                    EstadoIA_notifi_corta.Error(
                        e.message ?: "Error al generar la notificación con IA"
                    )
            }
        }
    }


    fun mejorar_texto_perzonalizado_whatsapp(
        saldo_tienda: Int,
        localidad_tienda: String,
        id_tienda: String,
        nombre_tienda: String,
        titulo_publicacion: String,
        descripcion: String
    ) {
        viewModelScope.launch {
            _estado_texto_whatsap_con_ia.value =
                ESstado_ia_msje_whatsap.Loading
            if (saldo_tienda < 10) {
                _estado_texto_whatsap_con_ia.value =
                    ESstado_ia_msje_whatsap.Error("Saldo insuficiente")
                return@launch
            }

            try {
                insta_repo.mejorar_texto_perzonalizado_whatsapp(
                    titulo_publicacion,
                    descripcion
                ) { notificacionIA ->

                    if (notificacionIA.isBlank()) {
                        _estado_texto_whatsap_con_ia.value =
                            ESstado_ia_msje_whatsap.Error("No se pudo generar el mensaje")
                        return@mejorar_texto_perzonalizado_whatsapp
                    }

                    _estado_texto_whatsap_con_ia.value =
                        ESstado_ia_msje_whatsap.Success(notificacionIA)

                    val historial = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = viewmodel_recargas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = "10",
                        tipo = "Gen IA (Mensaje WhatsApp personalizado)",
                        precio_soles = viewmodel_recargas
                            .calcular_precio_soles("10")
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - 10
                    )

                    viewmodel_recargas.restar_puntos_recarga(
                        historial,
                        "10",
                        id_tienda,
                        localidad_tienda
                    )
                }
            } catch (e: Exception) {
                _estado_texto_whatsap_con_ia.value =
                    ESstado_ia_msje_whatsap.Error(
                        e.message ?: "Error al generar el mensaje"
                    )
            }
        }
    }



    fun mejorar_texto_perzonalizado_compatir(
        saldo_tienda: Int,
        localidad_tienda: String,
        id_tienda: String,
        nombre_tienda: String,
        titulo_publicacion: String,
        descripcion: String
    ) {
        viewModelScope.launch {
            _estado_texto_compartir_con_ia.value =
                ESstado_ia_msje_compartir.Loading

            if (saldo_tienda < 10) {
                _estado_texto_compartir_con_ia.value =
                    ESstado_ia_msje_compartir.Error("Saldo insuficiente")
                return@launch
            }

            try {
                insta_repo.mejorar_texto_perzonalizado_compartir(
                    titulo_publicacion,
                    descripcion
                ) { notificacionIA ->

                    if (notificacionIA.isBlank()) {
                        _estado_texto_compartir_con_ia.value =
                            ESstado_ia_msje_compartir.Error("No se pudo generar el mensaje")
                        return@mejorar_texto_perzonalizado_compartir
                    }

                    _estado_texto_compartir_con_ia.value =
                        ESstado_ia_msje_compartir.Success(notificacionIA)

                    val historial = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = viewmodel_recargas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = "10",
                        tipo = "Gen IA (Mensaje WhatsApp personalizado)",
                        precio_soles = viewmodel_recargas
                            .calcular_precio_soles("10")
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - 10
                    )

                    viewmodel_recargas.restar_puntos_recarga(
                        historial,
                        "10",
                        id_tienda,
                        localidad_tienda
                    )
                }
            } catch (e: Exception) {
                _estado_texto_compartir_con_ia.value =
                    ESstado_ia_msje_compartir.Error(
                        e.message ?: "Error al generar el mensaje"
                    )
            }
        }
    }


    fun calcularBloques(seguidores: Int): Int {
        if (seguidores < 10) return 0
        return Math.ceil(seguidores / 10.0).toInt()
    }


    fun calcularCostoNotificacion(
        seguidores: Int,
        costoTipo: Int,
        costoPrioridad: Int,
        costoFormato: Int
    ): Int {
        val bloques = calcularBloques(seguidores)
        if (bloques == 0) return 0

        val costoPorBloque = costoTipo + costoPrioridad + costoFormato
        return bloques * costoPorBloque
    }


    fun validarTexto(titulo: String, descripcion: String) {
        val resultado = valida_notificacion(titulo, descripcion)
        _estadoValidacion.value = resultado
    }


    fun valida_notificacion(
        titulo_notificacion: String,
        descripcion_notificacion: String
    ): EstadoValidacionNotificacion {

        val textoOriginal = "$titulo_notificacion $descripcion_notificacion"
        val textoNormalizado = normalizarTexto(textoOriginal)

        Log.d("VALIDA_NOTIF", "Texto original: $textoOriginal")
        Log.d("VALIDA_NOTIF", "Texto normalizado: $textoNormalizado")

        palabrasBloqueadasNormalizadas.forEach { (original, regex) ->
            if (regex.containsMatchIn(textoNormalizado)) {

                Log.w(
                    "VALIDA_NOTIF",
                    "🚫 BLOQUEADA | palabra detectada='$original'"
                )

                return EstadoValidacionNotificacion.Bloqueada(
                    palabraDetectada = original,
                    mensaje = "La palabra \"$original\" no está permitida en notificaciones."
                )
            }
        }

        Log.d("VALIDA_NOTIF", "✅ PERMITIDA | sin palabras bloqueadas")

        return EstadoValidacionNotificacion.Permitida
    }


    fun normalizarTexto(texto: String): String {
        return texto.lowercase()
            .replace("@", "a")
            .replace("3", "e")
            .replace("4", "a")
            .replace("1", "i")
            .replace("!", "i")
            .replace("$", "s")
            .replace("0", "o")
            .replace(Regex("[^a-z ]"), "")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    fun subirImgTemporal(context: Context, uri: Uri, idTemporal: String, idTienda: String) {
        viewModelScope.launch {
            _estadoImagen.value = ImagenEstado.Cargando

            try {
                val resultado: Result<String> =
                    insta_repo.subirImgNotificacionTemporal(context, uri, idTemporal, idTienda)

                resultado.onSuccess { url ->
                    _estadoImagen.value = ImagenEstado.Exito(url = url, idTemporal = idTemporal)
                }.onFailure { e ->
                    _estadoImagen.value = ImagenEstado.Error(e.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _estadoImagen.value = ImagenEstado.Error(e.message ?: "Excepción inesperada")
                Log.e("FirebaseUpload", "Excepción: ${e.message}")
            }
        }
    }

    fun eliminarImagen(idTienda: String, idTemporal: String) {
        Log.d("FirebaseDelete", "$idTienda $idTemporal")
        viewModelScope.launch {
            try {
                _estadoImagen.value = ImagenEstado.Idle
                insta_repo.eliminarImgTemporal(idTienda, idTemporal)
                // ✅ Aquí reseteamos el estado para que Compose deje de mostrar la imagen

            } catch (e: Exception) {
                Log.e("FirebaseDelete", "Error eliminando imagen temporal: ${e.message}")
                // Opcional: cambiar el estado a Error si quieres mostrar mensaje
                _estadoImagen.value = ImagenEstado.Error("No se pudo eliminar la imagen")
            }
        }
    }


    fun resetImagen() {
        _estadoImagen.value = ImagenEstado.Idle
    }


    sealed class ImagenEstado {
        object Idle : ImagenEstado()
        object Cargando : ImagenEstado()
        data class Exito(val url: String, val idTemporal: String) : ImagenEstado()
        data class Error(val mensaje: String) : ImagenEstado()
    }


    sealed class EstadoValidacionNotificacion {
        object Idle : EstadoValidacionNotificacion()
        object Permitida : EstadoValidacionNotificacion()
        data class Bloqueada(
            val palabraDetectada: String,
            val mensaje: String
        ) : EstadoValidacionNotificacion()
    }


    sealed class EstadoIA_notifi_corta {
        object Idle : EstadoIA_notifi_corta()
        object Loading : EstadoIA_notifi_corta()
        data class Success(val txt_descripcion: NotificacionIA) : EstadoIA_notifi_corta()
        data class Error(val mensaje: String) : EstadoIA_notifi_corta()
    }

    sealed class EstadoIA {
        object Idle : EstadoIA()
        object Loading : EstadoIA()
        data class Success(val lista: List<OpcionPromocionIA>) : EstadoIA()
        data class Error(val mensaje: String) : EstadoIA()
    }

    sealed class ESstado_ia_msje_whatsap {
        object Idle : ESstado_ia_msje_whatsap()
        object Loading : ESstado_ia_msje_whatsap()
        data class Success(val txt_descripcion: String) : ESstado_ia_msje_whatsap()
        data class Error(val mensaje: String) : ESstado_ia_msje_whatsap()
    }

    sealed class ESstado_ia_msje_compartir {
        object Idle : ESstado_ia_msje_compartir()
        object Loading : ESstado_ia_msje_compartir()
        data class Success(val txt_descripcion: String) : ESstado_ia_msje_compartir()
        data class Error(val mensaje: String) : ESstado_ia_msje_compartir()
    }


}
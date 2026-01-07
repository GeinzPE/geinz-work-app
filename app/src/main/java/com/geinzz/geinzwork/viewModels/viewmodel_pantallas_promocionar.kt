package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

}
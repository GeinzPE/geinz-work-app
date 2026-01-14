package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.dataclass_promos.promociones_tiendas_negocios
import com.geinzz.geinzwork.model.repo_obtener_datos_promociones
import com.geinzz.geinzwork.utils.constantes.constantes.constantes.db
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewmodel_datos_promociones : ViewModel() {

    private val repo = repo_obtener_datos_promociones()

    private val _estadoPromocion =
        MutableStateFlow<EstadoPromocion>(EstadoPromocion.Vacio)

    val estadoPromocion: StateFlow<EstadoPromocion> =
        _estadoPromocion

    // MutableStateFlow privado (solo se modifica dentro del ViewModel)
    private val _datos_promocion_parametro =
        MutableStateFlow<dataclass_promociones_cerca_de_ti>(dataclass_promociones_cerca_de_ti())

    // StateFlow público inmutable (solo lectura desde afuera)
    val datos_promocion_parametro: StateFlow<dataclass_promociones_cerca_de_ti> =
        _datos_promocion_parametro



    private val _estado_promociones = MutableStateFlow<Map<String, String>>(emptyMap())
    val estadoPromos: StateFlow<Map<String, String>> = _estado_promociones

    fun escuchar_estado_promociones_activa(localidad: String) {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection("promos_ofertas")

        ref.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            val nuevosEstados = mutableMapOf<String, String>()
            snapshot.documents.forEach { doc ->
                val idPromo = doc.id
                val estado = doc.getString("estado") ?: "inactivo"
                nuevosEstados[idPromo] = estado
            }

            _estado_promociones.value = nuevosEstados
        }
    }


    fun obtener_datos_promociones(
        id_tienda: String,
        localidad: String,
        index: String
    ) {
        viewModelScope.launch {
            _estadoPromocion.value = EstadoPromocion.Cargando

            try {
                val promo = repo.obtner_datos_promocion(
                    id_tienda = id_tienda,
                    localidad = localidad,
                    index = index
                )

                _estadoPromocion.value =
                    if (promo.url_img.isNotEmpty()) {
                        EstadoPromocion.Exito(promo)
                    } else {
                        EstadoPromocion.Vacio
                    }

            } catch (e: Exception) {
                _estadoPromocion.value =
                    EstadoPromocion.Error(
                        e.message ?: "Error desconocido"
                    )
            }
        }
    }

    fun obtener_datos_promocion_notificacion(
        id_tienda: String, localidad: String, id_promo: String
    ) {
        viewModelScope.launch {
            _estadoPromocion.value = EstadoPromocion.Cargando

            try {
                val promo = repo.obtner_datos_promocion_notificacion(
                    id_tienda = id_tienda,
                    localidad = localidad,
                    id_promo = id_promo
                )

                _estadoPromocion.value =
                    if (promo.url_img.isNotEmpty()) {
                        EstadoPromocion.Exito(promo)
                    } else {
                        EstadoPromocion.Vacio
                    }

            } catch (e: Exception) {
                _estadoPromocion.value =
                    EstadoPromocion.Error(
                        e.message ?: "Error desconocido"
                    )
            }
        }
    }

    fun obtener_datos_promociones_por_paramtros(
        localidad: String,
        id_promo: String
    ) {
        Log.d("parmaeonsotrnaioe","$localidad $id_promo")
        viewModelScope.launch {
            try {
                _datos_promocion_parametro.value =
                    repo.obtener_datos_promociones_scroll_infinito_compartido(localidad, id_promo)
            } catch (e: Exception) {
                Log.d("error_opbtner_prom", "error $e")
            }
        }
    }
}


sealed class EstadoPromocion {
    object Cargando : EstadoPromocion()
    data class Exito(
        val data: promociones_tiendas_negocios
    ) : EstadoPromocion()

    object Vacio : EstadoPromocion()

    data class Error(
        val mensaje: String
    ) : EstadoPromocion()
}

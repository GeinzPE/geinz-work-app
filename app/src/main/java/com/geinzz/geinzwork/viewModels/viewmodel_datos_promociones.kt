package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_promos.promociones_tiendas_negocios
import com.geinzz.geinzwork.model.repo_obtener_datos_promociones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewmodel_datos_promociones : ViewModel() {

    private val repo = repo_obtener_datos_promociones()

    private val _estadoPromocion =
        MutableStateFlow<EstadoPromocion>(EstadoPromocion.Vacio)

    val estadoPromocion: StateFlow<EstadoPromocion> =
        _estadoPromocion

    fun obtener_datos_promociones(
        id_tienda: String,
        localidad: String,
        index: Int
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
                    if (promo != null) {
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

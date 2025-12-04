package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz
import com.geinzz.geinzwork.model.repo_novedades_tiendas_geinz
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_novedades_tiendas : ViewModel() {

    private val repo_novedades_geinz = repo_novedades_tiendas_geinz()

    // 📌 Lista completa de tiendas
    private var _todasLasTiendas: List<dataclass_novedades_geinz> = emptyList()

    // 📌 Lista filtrada
    private var _tiendasFiltradas: List<dataclass_novedades_geinz> = emptyList()

    // 📌 STATEFLOW
    private val _obtener_datos_tienda =
        MutableStateFlow<carga_datos_tienda>(carga_datos_tienda.empty)
    val obtener_datos_tienda = _obtener_datos_tienda.asStateFlow()

    // ==========================
    //  📌 OBTENER TIENDAS
    // ==========================
    fun obtener_datos_nuevos_tiendas(localidad: String) {
        viewModelScope.launch {
            try {
                // Mostrar loading
                _obtener_datos_tienda.value = carga_datos_tienda.loading



                // Obtener datos
                val datos = repo_novedades_geinz.obtener_tiendas_real_time(localidad)

                // Si los datos no han cambiado → NO emitir nada
                if (datos == _todasLasTiendas) return@launch

                _todasLasTiendas = datos
                _tiendasFiltradas = datos

                val categorias_unicas = datos
                    .map { it.categoria }
                    .filter { it.isNotBlank() }
                    .distinct()

                // Pasar a succes
                _obtener_datos_tienda.value = carga_datos_tienda.succes(
                    datos = _tiendasFiltradas,
                    categorias = categorias_unicas
                )

            } catch (e: Exception) {
                _obtener_datos_tienda.value = carga_datos_tienda.error
            }
        }
    }


    // ==========================
    //  📌 FILTRO
    // ==========================
    fun filtrarPorCategoria(categoria: String) {
        viewModelScope.launch {

            val categorias_unicas =
                _todasLasTiendas.map { it.categoria }.distinct()

            // 📌 Filtrar por "Todos"
            if (categoria == "Todos") {

                // Si ya está mostrando todo → NO emitir nada
                if (_tiendasFiltradas == _todasLasTiendas) return@launch

                _tiendasFiltradas = _todasLasTiendas

                _obtener_datos_tienda.value = carga_datos_tienda.succes(
                    datos = _tiendasFiltradas,
                    categorias = categorias_unicas
                )
                return@launch
            }

            // 📌 Filtro real
            val filtradas = _todasLasTiendas.filter {
                it.categoria.equals(categoria, ignoreCase = true)
            }

            // Si la lista ya es igual → NO emitir nada
            if (filtradas == _tiendasFiltradas) return@launch

            _tiendasFiltradas = filtradas

            _obtener_datos_tienda.value = carga_datos_tienda.succes(
                datos = _tiendasFiltradas,
                categorias = categorias_unicas
            )
        }
    }

    // ==========================
    //  📌 ESTADOS
    // ==========================
    sealed class carga_datos_tienda {
        object empty : carga_datos_tienda()
        object loading : carga_datos_tienda()

        data class succes(
            val datos: List<dataclass_novedades_geinz>,
            val categorias: List<String>
        ) : carga_datos_tienda()

        object error : carga_datos_tienda()
    }
}

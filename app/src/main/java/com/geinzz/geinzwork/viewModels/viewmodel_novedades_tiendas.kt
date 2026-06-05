package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz
import com.geinzz.geinzwork.model.repo_novedades_tiendas_geinz
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_novedades_tiendas : ViewModel() {

    private val repo = repo_novedades_tiendas_geinz()

    private var _todasLasTiendas: List<dataclass_novedades_geinz> = emptyList()
    private var _tiendasFiltradas: List<dataclass_novedades_geinz> = emptyList()
    private var _ultimoDocumento: DocumentSnapshot? = null
    private var _categoriaActiva: String = "Todos"
    private var _localidadActual: String = ""

    private val _hayMasPaginas = MutableStateFlow(true)
    val hayMasPaginas = _hayMasPaginas.asStateFlow()

    private val _cargandoMas = MutableStateFlow(false)
    val cargandoMas = _cargandoMas.asStateFlow()

    private val _obtener_datos_tienda =
        MutableStateFlow<carga_datos_tienda>(carga_datos_tienda.empty)
    val obtener_datos_tienda = _obtener_datos_tienda.asStateFlow()

    // Primera carga (resetea todo)
    fun obtener_datos_nuevos_tiendas(localidad: String) {
        _localidadActual = localidad

        viewModelScope.launch {
            _obtener_datos_tienda.value = carga_datos_tienda.loading

            _ultimoDocumento = null
            _todasLasTiendas = emptyList()
            _tiendasFiltradas = emptyList()
            _hayMasPaginas.value = true
            _categoriaActiva = "Todos"

            try {
                val (datos, cursor) = repo.obtener_tiendas_paginadas(localidad)

                _ultimoDocumento = cursor
                _hayMasPaginas.value = cursor != null

                _todasLasTiendas = datos
                _tiendasFiltradas = datos

                emitirEstadoActual()
            } catch (e: Exception) {
                _obtener_datos_tienda.value = carga_datos_tienda.error
            }
        }
    }

    // Cargar más páginas
    fun cargarMasTiendas(localidad: String = _localidadActual) {
        if (_cargandoMas.value || !_hayMasPaginas.value) return

        viewModelScope.launch {
            _cargandoMas.value = true
            try {
                val (nuevas, cursor) = repo.obtener_tiendas_paginadas(
                    localidad = localidad,
                    ultimoDocumento = _ultimoDocumento
                )

                _ultimoDocumento = cursor
                _hayMasPaginas.value = cursor != null

                // Evitar duplicados por id_tienda
                val idsExistentes = _todasLasTiendas.map { it.id_tienda }.toSet()
                val sinDuplicados = nuevas.filter { it.id_tienda !in idsExistentes }

                if (sinDuplicados.isEmpty() && nuevas.isNotEmpty()) {
                    _hayMasPaginas.value = false
                    return@launch
                }

                _todasLasTiendas = _todasLasTiendas + sinDuplicados

                // Reaplicar filtro actual
                _tiendasFiltradas = if (_categoriaActiva == "Todos") {
                    _todasLasTiendas
                } else {
                    _todasLasTiendas.filter {
                        it.categoria.equals(_categoriaActiva, ignoreCase = true)
                    }
                }

                emitirEstadoActual()
            } catch (e: Exception) {
                // Error silencioso, no rompe la UI
            } finally {
                _cargandoMas.value = false
            }
        }
    }

    // Filtro por categoría (solo sobre datos ya cargados)
    fun filtrarPorCategoria(categoria: String) {
        _categoriaActiva = categoria
        _tiendasFiltradas = if (categoria == "Todos") {
            _todasLasTiendas
        } else {
            _todasLasTiendas.filter {
                it.categoria.equals(categoria, ignoreCase = true)
            }
        }
        _obtener_datos_tienda.value = carga_datos_tienda.succes(
            datos = _tiendasFiltradas,
            categorias = obtenerCategoriasUnicas()
        )
    }

    private fun obtenerCategoriasUnicas(): List<String> =
        _todasLasTiendas.map { it.categoria }.filter { it.isNotBlank() }.distinct()

    private fun emitirEstadoActual() {
        _obtener_datos_tienda.value = carga_datos_tienda.succes(
            datos = _tiendasFiltradas,
            categorias = obtenerCategoriasUnicas()
        )
    }

    sealed class carga_datos_tienda {
        object empty   : carga_datos_tienda()
        object loading : carga_datos_tienda()
        data class succes(
            val datos: List<dataclass_novedades_geinz>,
            val categorias: List<String>
        ) : carga_datos_tienda()
        object error : carga_datos_tienda()
    }
}
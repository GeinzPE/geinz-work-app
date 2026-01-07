package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.model.repo_promos_cercanas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_promos_cercanas : ViewModel() {

    private val repo = repo_promos_cercanas()


    private val _promosCargadas =
        MutableStateFlow<List<dataclass_promociones_cerca_de_ti>>(emptyList())
    val promosCargadas: StateFlow<List<dataclass_promociones_cerca_de_ti>> = _promosCargadas

    private var paginaActual = 0
    private val bloque = 5
    private var cargando = false


    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarSiguienteBloque(localidad: String) {
        if (cargando) return
        cargando = true

        viewModelScope.launch {
            try {
                val todasLasPromos = repo.obtener_promos(localidad)
                Log.d("ViewModelPromos", "Total promos obtenidas de DB: ${todasLasPromos.size}")

                // 🔹 eliminar duplicados globalmente por id_promocion
                val todasFiltradas = todasLasPromos
                    .map { it.dataclass_promociones_cerca_de_ti }
                    .distinctBy { it.informacion_publcacion.id_promocion }

                Log.d("ViewModelPromos", "Promos únicas tras distinctBy: ${todasFiltradas.size} -> IDs: ${todasFiltradas.map { it.informacion_publcacion.id_promocion }}")

                // 🔹 Filtrar las promos que ya se cargaron
                val existentesIds = _promosCargadas.value.map { it.informacion_publcacion.id_promocion }.toSet()
                val nuevasDisponibles = todasFiltradas.filter { it.informacion_publcacion.id_promocion !in existentesIds }
                    .shuffled() // 🔹 orden aleatorio real cada vez

                Log.d("ViewModelPromos", "Promos disponibles tras filtrar existentes: ${nuevasDisponibles.map { it.informacion_publcacion.id_promocion }}")

                // 🔹 Tomar solo hasta "bloque" elementos
                val nuevasFiltradas = nuevasDisponibles.take(bloque)

                if (nuevasFiltradas.isNotEmpty()) {
                    _promosCargadas.value = _promosCargadas.value + nuevasFiltradas
                    Log.d("ViewModelPromos", "Total promos cargadas en StateFlow: ${_promosCargadas.value.size} -> IDs: ${_promosCargadas.value.map { it.informacion_publcacion.id_promocion }}")
                } else {
                    Log.d("ViewModelPromos", "No hay más promos nuevas para cargar.")
                }

            } catch (e: Exception) {
                Log.e("ViewModelPromos", "Error cargando bloque: ${e.message}")
            } finally {
                cargando = false
            }
        }
    }


    fun resetPromos() {
        _promosCargadas.value = emptyList()
        paginaActual = 0
    }


    private val _estadoPromos =
        MutableStateFlow<estado_carga_promociones>(
            estado_carga_promociones.loading
        )

    private val listaCompleta =
        MutableStateFlow<List<obj_completo>>(emptyList())

    private val listaFiltrada =
        MutableStateFlow<List<obj_completo>>(emptyList())

    private val categoriasDisponibles =
        MutableStateFlow<List<String>>(emptyList())

    val _categoriasDisponibles: StateFlow<List<String>> =
        categoriasDisponibles.asStateFlow()

    val estadoPromos: StateFlow<estado_carga_promociones> =
        _estadoPromos.asStateFlow()


    fun agregar_estadisticas_publicacion(tipo: String, id_promo: String, localidad: String) {
        viewModelScope.launch {
            try {
                repo.agregar_contador_estadisticas_publicacion(tipo, id_promo, localidad)
            } catch (e: Exception) {
                Log.d("error", "$e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtener_promociones(localidad: String) {
        viewModelScope.launch {
            _estadoPromos.value = estado_carga_promociones.loading

            try {
                val resultado = repo.obtener_promos(localidad)

                if (resultado.isEmpty()) {
                    _estadoPromos.value =
                        estado_carga_promociones.empty("No hay promociones cerca de ti")
                    return@launch
                }

                // 🔥 LISTA BASE (NO SE TOCA)
                listaCompleta.value = resultado

                // 🔥 LISTA VISIBLE
                listaFiltrada.value = resultado

                // 🔥 CATEGORÍAS (DESDE LISTA COMPLETA)
                categoriasDisponibles.value =
                    resultado.flatMap {
                        it.dataclass_promociones_cerca_de_ti
                            .informacion_publcacion
                            .categoria
                            .split(",")
                    }
                        .map { it.trim() }
                        .distinct()

                _estadoPromos.value =
                    estado_carga_promociones.succes(listaFiltrada.value)

            } catch (e: Exception) {
                _estadoPromos.value =
                    estado_carga_promociones.error("Error al cargar promociones")
            }
        }
    }

    fun filtrar_promociones(categoria: String) {
        val base = listaCompleta.value

        listaFiltrada.value =
            if (categoria == "Todos") {
                base
            } else {
                base.filter { obj ->
                    obj.dataclass_promociones_cerca_de_ti
                        .informacion_publcacion
                        .categoria
                        .split(",")
                        .any { it.trim().equals(categoria, ignoreCase = true) }
                }
            }

        _estadoPromos.value =
            if (listaFiltrada.value.isEmpty()) {
                estado_carga_promociones.empty("No hay promociones para esta categoría")
            } else {
                estado_carga_promociones.succes(listaFiltrada.value)
            }
    }

    fun filtrar_promociones_por_id(id: String) {
        val base = listaCompleta.value

        listaFiltrada.value = base.filter { obj ->
            obj.dataclass_promociones_cerca_de_ti
                .informacion_publcacion
                .id_tienda == id
        }

        _estadoPromos.value =
            if (listaFiltrada.value.isEmpty()) {
                estado_carga_promociones.empty("Esta tienda no tiene promociones activas")
            } else {
                estado_carga_promociones.succes(listaFiltrada.value)
            }
    }


    sealed class estado_carga_promociones {
        object loading : estado_carga_promociones()
        data class empty(val txt: String) : estado_carga_promociones()
        data class succes(val items: List<obj_completo>) : estado_carga_promociones()
        data class error(val txt: String) : estado_carga_promociones()
    }
}

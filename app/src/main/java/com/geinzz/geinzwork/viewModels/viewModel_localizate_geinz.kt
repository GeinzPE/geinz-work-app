package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_horarios_atencion_tiendas
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.estadoTienda
import com.geinzz.geinzwork.model.modelo_agregar_cat_sub_localizate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class viewModel_localizate_geinz : ViewModel() {
    private val _subcategorias = MutableLiveData<List<dataclass_cat_sub>>()
    val subcategorias: LiveData<List<dataclass_cat_sub>> get() = _subcategorias
    private val _cantiada_tiendas = MutableLiveData<Int>()
    val cantidad_tiendas: LiveData<Int> get() = _cantiada_tiendas
    val modelo_agregar_cat_sub = modelo_agregar_cat_sub_localizate()

    val _encontrados_activos_tiendas = MutableLiveData<List<encontradas_por_categoria>>()
    val encontrados_activos_tiendas: LiveData<List<encontradas_por_categoria>> get() = _encontrados_activos_tiendas

    val _tiendas_activa = MutableLiveData<List<estadoTienda>>()
    val tiendas_activa: LiveData<List<estadoTienda>> get() = _tiendas_activa

    fun obtener_cantidad_tiendas_por_localidad(localidad_selecionada: String) {
        viewModelScope.launch {
            try {
                _cantiada_tiendas.value =
                    modelo_agregar_cat_sub.obtenerCantidadTiendasPorLocalidad(localidad_selecionada)
            } catch (e: Exception) {
                _cantiada_tiendas.value = 0
            }
        }
    }

    fun obtener_horario_tiendas(localidad_selecionada: String) {
        viewModelScope.launch {
            try {
                val modelo_horario_tienda =
                    modelo_agregar_cat_sub.obtenerTiendas_registradas_activas(localidad_selecionada,modelo_agregar_cat_sub.obtener_categorias_subcategorias())
                _encontrados_activos_tiendas.value = modelo_horario_tienda
//                _tiendas_activa.value = modelo_agregar_cat_sub.verificar_activos_desactivos(modelo_horario_tienda)
            } catch (e: Exception) {
                _encontrados_activos_tiendas.value = emptyList()
//                _tiendas_activa.value = emptyList()
            }
        }
    }

//    fun obtener_conincidencias() {
//        viewModelScope.launch {
//            delay(300)
//            try {
//                val subcategorias = modelo_agregar_cat_sub.obtener_categorias_subcategorias()
//                _subcategorias.value = subcategorias
//            } catch (e: Exception) {
//                _subcategorias.value = emptyList()
//            }
//        }
//    }
}
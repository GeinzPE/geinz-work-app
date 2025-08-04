package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.repo_filtrado_tiendas
import kotlinx.coroutines.launch

class viewModel_filtado_tiendas : ViewModel() {

    val repo_filtrado = repo_filtrado_tiendas()

    private val subcategorias = MutableLiveData<List<filtrado_tiendas_cat_sub>>()
    val _subcategoiraList: LiveData<List<filtrado_tiendas_cat_sub>> get() = subcategorias

    private val tiendas_filtradas = MutableLiveData<List<tiendas_filtradas>>()
    val _tiendas_filtradas: LiveData<List<tiendas_filtradas>> get() = tiendas_filtradas

    val datos_tienda = MutableLiveData<List<modelo_tienda>>()
    val _datos_tienda: LiveData<List<modelo_tienda>> get() = datos_tienda

    val horario_tienda = MutableLiveData<List<horario_tienda>>()
    val _horario_tienda: LiveData<List<horario_tienda>> get() = horario_tienda


    fun obtener_subcategorias(categoria_selecionada: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtener_subcategorias_tiendas(categoria_selecionada)
                subcategorias.value = data
                Log.d(
                    "obtenemos_datos",
                    " $categoria_selecionada ${subcategorias.value.toString()}"
                )

            } catch (e: Exception) {
                subcategorias.value = emptyList()
            }
        }
    }

    fun obtener_tiendas_filtradas(localida: String, categoria: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerTiendasFiltradas(localida, categoria)
                tiendas_filtradas.value = data

            } catch (e: Exception) {
                tiendas_filtradas.value = emptyList()
            }
        }

    }

    fun obtener_campos_tiendas_por_id(localida: String, id_tienda: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenner_campos_tiendas_espesifica(localida, id_tienda)
                datos_tienda.value = data
            } catch (e: Exception) {
                datos_tienda.value = emptyList()
            }
        }
    }

    fun obtener_horario_por_tienda(localida: String, id_tienda: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerHorarioPorTienda(id_tienda, localida)
                horario_tienda.value = data
            } catch (e: Exception) {
                horario_tienda.value = emptyList()
            }

        }

    }

}
package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.tienda_patrocinada
import com.geinzz.geinzwork.model.modelo_agregar_cat_sub_localizate
import androidx.compose.runtime.State
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class viewModel_localizate_geinz : ViewModel() {
    val modelo_agregar_cat_sub = modelo_agregar_cat_sub_localizate()

    val _T_patrocinadas_por_categoria = MutableLiveData<List<tienda_patrocinada>>()
    val T_patrocinadas_por_categoria: LiveData<List<tienda_patrocinada>> get() = _T_patrocinadas_por_categoria

    val _encontrados_activos_tiendas = MutableLiveData<List<encontradas_por_categoria>>()
    val encontrados_activos_tiendas: LiveData<List<encontradas_por_categoria>> get() = _encontrados_activos_tiendas

    fun T_obtener_registrados_activos(localidad_selecionada: String) {
        viewModelScope.launch {
            try {
                val modelo_horario_tienda =
                    modelo_agregar_cat_sub.obtener_tiendas_categorias_activas_registradas(
                        localidad_selecionada
                    )
                Log.d(
                    "btenoemos_lista_filtrado_por",
                    "$localidad_selecionada $modelo_horario_tienda"
                )
                _encontrados_activos_tiendas.value = modelo_horario_tienda
            } catch (e: Exception) {
                _encontrados_activos_tiendas.value = emptyList()
            }
        }
    }


    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    fun T_patrocinadas(localidad: String, categoria: String) {
        viewModelScope.launch {
            val tiempoInicio = System.currentTimeMillis()
            _loading.value = true
            try {
                val result = modelo_agregar_cat_sub.obtenerTiendasPatrocinadas(localidad, categoria)
                _T_patrocinadas_por_categoria.value = result
            } catch (e: Exception) {
                _T_patrocinadas_por_categoria.value = emptyList()
            } finally {
                val tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio
                val tiempoRestante = 1500 - tiempoTranscurrido
                delay(tiempoRestante)
                _loading.value = false
            }
        }
    }
}
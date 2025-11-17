package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.model.repo_agregar_cat_sub_localizate
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.tiendas_patrocinadas
import com.geinzz.geinzwork.data_store.data_store_localidad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class viewModel_localizate_geinz : ViewModel() {
    val modelo_agregar_cat_sub = repo_agregar_cat_sub_localizate()

//    private val _T_patrocinadas_por_categoria = MutableLiveData<List<tiendas_filtradas>>()
//    val T_patrocinadas_por_categoria: LiveData<List<tiendas_filtradas>> get() = _T_patrocinadas_por_categoria

   private val _encontrados_activos_tiendas = MutableLiveData<List<encontradas_por_categoria>>()
    val encontrados_activos_tiendas: LiveData<List<encontradas_por_categoria>> get() = _encontrados_activos_tiendas

//    private val _datos_tienas_patrocinadas = MutableLiveData<List<tiendas_patrocinadas>>()
//    val datos_tienas_patrocinadas: LiveData<List<tiendas_patrocinadas>> get() = _datos_tienas_patrocinadas

    val _subcategoria = MutableLiveData<List<String>>()
    val subcategorias: LiveData<List<String>> get() = _subcategoria


    fun obtenerFrasesCarga(localidadUser: String, nombreUser: String): List<String> {
        Log.d("obtenmoms_frace",localidadUser)
        return listOf(
            "Espere un momento...",
            "Cargando tiendas de $localidadUser...",
            "Buscamos lo mejor para ti $nombreUser ..."
        )
    }

    fun obtenerResultados(
        texto: String,
        lista: List<encontradas_por_categoria>
    ): List<encontradas_por_categoria> = lista.filter { catSub ->
        catSub.subcateogiras?.any {
            it.contains(texto, ignoreCase = true)
        } == true
    }

    init {
        T_obtener_registrados_activos()
    }


    fun T_obtener_registrados_activos() {
        viewModelScope.launch {
            try {
                val modelo_horario_tienda =
                    modelo_agregar_cat_sub.obtener_tiendas_categorias_activas_registradas(
                    )
                _encontrados_activos_tiendas.value = modelo_horario_tienda
            } catch (e: Exception) {
                _encontrados_activos_tiendas.value = emptyList()
            }
        }
    }


    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

//    fun T_patrocinadas(localidad: String, categoria: String) {
//        viewModelScope.launch {
//            val tiempoInicio = System.currentTimeMillis()
//            _loading.value = true
//            try {
//                val result = modelo_agregar_cat_sub.obtener_datos_tiendas_patrocindas(localidad, categoria)
//                _T_patrocinadas_por_categoria.value = result
//            } catch (e: Exception) {
//                _T_patrocinadas_por_categoria.value = emptyList()
//            } finally {
//                val tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio
//                val tiempoRestante = 1500 - tiempoTranscurrido
//                delay(tiempoRestante)
//                _loading.value = false
//            }
//        }
//    }


//    private val _loading_subcategorias = mutableStateOf(false)
//    val loading_subcateogiras: State<Boolean> = _loading_subcategorias
//
//    fun obtener_subcategorias(categoria: String) {
//        viewModelScope.launch {
//            val tiempoInicio = System.currentTimeMillis()
//            _loading_subcategorias.value=false
//            try {
//                val datos = modelo_agregar_cat_sub.obtener_subcategorias(categoria)
//                _subcategoria.value = datos
//            } catch (e: Exception) {
//                _subcategoria.value = emptyList()
//            }finally {
//                val tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio
//                val tiempoRestante = 1500 - tiempoTranscurrido
//                if (tiempoRestante > 0) {
//                    delay(tiempoRestante)
//                }
//                _loading_subcategorias.value = true
//            }
//        }
//    }
}
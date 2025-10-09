package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_servicios_tramites
import com.geinzz.geinzwork.ui.adapters.adapterCategorias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewmode_servicios_tramite : ViewModel(){
    private val isnta= repo_servicios_tramites()
     private val _lugares= MutableLiveData<List<dataclass_lugares_db>>()
    val lugares : LiveData<List<dataclass_lugares_db>> get() =_lugares
    private val _listaFiltrada = MutableStateFlow<List<dataclass_lugares_db>>(emptyList())
    val listaFiltrada: StateFlow<List<dataclass_lugares_db>> = _listaFiltrada

    var lugares_turisticos = mutableListOf<dataclass_lugares_db>()
        private set

    private var todo_lugares=emptyList<dataclass_lugares_db>()

    fun obtener_lugares(localida:String){
        viewModelScope.launch {
            try {
                _lugares.value=isnta.obtenerServiciosTramites(localida)
            }catch (e: Exception){
                _lugares.value=emptyList()
            }
        }
    }
    fun todos(lista: List<dataclass_lugares_db>){
        todo_lugares=lista
        _listaFiltrada.value=lista
        Log.d("todo_lugares",todo_lugares.toString())
    }

    fun filtrar_por_categoria(categorias: String){
        _listaFiltrada.value=if(categorias=="Todos"){
            todo_lugares
        }else{
            todo_lugares.filter { it.categoria.contains(categorias) }
        }
        Log.d("lista_value","${ todo_lugares.filter { it.categoria.contains(categorias) }}")
    }
    fun filtrar_nombre_categoria(nombre:String,categoria:String,lista: List<dataclass_lugares_db>): List<dataclass_lugares_db>{
        return lista.filter { item->
            val conicide_TXT=nombre.isBlank() || item.lugar_nombre.contains(nombre,ignoreCase = true)
            val coincidenciaExacta = categoria == "Todos" || item.categoria.any { i -> i.contains(categoria, ignoreCase = true) }
            conicide_TXT && coincidenciaExacta
        }

    }
}
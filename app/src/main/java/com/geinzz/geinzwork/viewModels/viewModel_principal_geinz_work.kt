package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_agregar_cat_sub_localizate
import com.geinzz.geinzwork.model.repo_principal_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.principal.carga_progres_categoria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewModel_principal_geinz_work : ViewModel() {

    val instacia = repo_principal_geinz_work()
    val instacia_repo_cat_sub = repo_agregar_cat_sub_localizate()


    private val sub_cat_tiendas =
        MutableLiveData<List<com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub>>()
    val _sub_cat_tiendas: LiveData<List<com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub>> get() = sub_cat_tiendas


    private val _userData = MutableLiveData<datos_principales_user>()
    val userData: LiveData<datos_principales_user> = _userData

    private val state_cat = MutableLiveData<carga_categorias>()
    val _state_cat: LiveData<carga_categorias> = state_cat

    private val lista_filtrado_localida = MutableLiveData<List<localidades_filtrado>>()
    val _lista_filtrado_localidades: LiveData<List<localidades_filtrado>> get() = lista_filtrado_localida


    private val _estado_version_PS = MutableStateFlow(Triple("", false, ""))
    val estado_version_PS = _estado_version_PS.asStateFlow()

    init {
        // Se carga una sola vez cuando el ViewModel se crea
        obtener_subcategorias()
        obtner_filtrado_localidades()
    }


//    fun obtener_datos_user_registrado(id_user: String) {
//        viewModelScope.launch {
//            try {
//                _userData.value = instacia.obtenerDatosUser(id_user)
//            } catch (e: Exception) {
//                _userData.value = datos_principales_user()
//            }
//        }
//    }

    fun obtener_subcategorias(solo5: Boolean = true) {
        viewModelScope.launch {
            try {
                state_cat.value = carga_categorias.Loading
                sub_cat_tiendas.value =
                    instacia_repo_cat_sub.obtener_categorias_subcategorias(solo5)
                state_cat.value = carga_categorias.succes
            } catch (e: Exception) {
                sub_cat_tiendas.value = emptyList()
                state_cat.value = carga_categorias.error
            }
        }
    }

    fun obtner_filtrado_localidades() {
        viewModelScope.launch {
            try {
                lista_filtrado_localida.value = instacia.obtenerLocalidadesFiltrados()
            } catch (e: java.lang.Exception) {
                lista_filtrado_localida.value = emptyList()
            }
        }
    }


    fun verificar_vesion_actulizacion(context: Context) {
        viewModelScope.launch {
            val txt_cambios = instacia.txt_cambios_realziados()
            try {
                instacia.verificarControlVersiones(context) { versionRemota, debeActualizar ->
                    _estado_version_PS.value = Triple(txt_cambios, debeActualizar, versionRemota)
                }
            } catch (e: Exception) {
                _estado_version_PS.value = Triple("sin txt", false, "")
                Log.d("error", "error al obtenr lso datos")
            }
        }
    }

    sealed class carga_categorias {
        object Loading : carga_categorias()
        object succes : carga_categorias()
        object error : carga_categorias()


    }

}
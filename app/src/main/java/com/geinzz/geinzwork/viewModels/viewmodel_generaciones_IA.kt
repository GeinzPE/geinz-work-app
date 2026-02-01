package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.datos_gen_IA_Tiendas
import com.geinzz.geinzwork.model.repo_generaciones_IA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class viewmodel_generaciones_IA: ViewModel() {
    val insta_repo= repo_generaciones_IA()

    private val _estado_generaciones_IA =
        MutableStateFlow<EstadoGeneracionesIA>(EstadoGeneracionesIA.Idle)

    val estado_generaciones_IA: StateFlow<EstadoGeneracionesIA> =
        _estado_generaciones_IA


    fun obtner_generaciones_IA(localida: String, id_tienda: String) {
        viewModelScope.launch {

            insta_repo
                .obtener_generaciones_IA_realtime(id_tienda, localida)
                .onStart {
                    _estado_generaciones_IA.value = EstadoGeneracionesIA.Loading
                }
                .catch {
                    _estado_generaciones_IA.value =
                        EstadoGeneracionesIA.Error("Error al obtener generaciones")
                }
                .collect { lista ->

                    _estado_generaciones_IA.value =
                        if (lista.isNotEmpty()) {
                            EstadoGeneracionesIA.Success(lista)
                        } else {
                            EstadoGeneracionesIA.Empty("No se encontraron generaciones")
                        }
                }
        }
    }


    sealed class EstadoGeneracionesIA {
        object Idle : EstadoGeneracionesIA()
        object Loading : EstadoGeneracionesIA()
        data class Success(
            val data: List<datos_gen_IA_Tiendas>
        ) : EstadoGeneracionesIA()
        data class Error(
            val message: String
        ) : EstadoGeneracionesIA()
        data class Empty(
            val message: String
        ) : EstadoGeneracionesIA()
    }


}
package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.widget_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.data_store.data_store_localidad.guardarUrlsCarga
import com.geinzz.geinzwork.data_store.data_store_localidad.guardarUrlsCarga_turismo
import com.geinzz.geinzwork.data_store.data_store_localidad.obtenerUrlsCarga
import com.geinzz.geinzwork.data_store.data_store_localidad.obtenerUrlsCarga_turismo
import com.geinzz.geinzwork.model.repo_carga_img_general
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_carga_img_general(
    private val context: Context,
) : ViewModel() {
    private val _urlsCarga = MutableStateFlow<List<String>>(emptyList())
    val urlsCarga = _urlsCarga.asStateFlow()

    private val _urlsCarga_turistico = MutableStateFlow<List<String>>(emptyList())
    val urlsCarga_turistico = _urlsCarga_turistico.asStateFlow()



    private val _estado_carga_widget_tienda = MutableStateFlow<widget_tienda>(widget_tienda())

    val estado_carga_widget_tienda: StateFlow<widget_tienda> =
        _estado_carga_widget_tienda.asStateFlow()

    private val _idTienda = MutableStateFlow("")
    val idTienda: StateFlow<String> = _idTienda.asStateFlow()
    val repo = repo_carga_img_general()

    init {
        cargarUrls()
        cargar_url_lugares_turitsticos()
        viewModelScope.launch {
            // Collect del DataStore para obtener el id cuando esté disponible
            data_store_localidad.get_id_socio(context).collect { id ->
                if (id.isNotEmpty()) {
                    _idTienda.value = id
                    // Ahora que el id ya existe, puedes cargar todo
                    obtner_datos_tienda(id, "barranca")
                }
            }
        }
    }
    // Flow que expone el id del socio

    private fun cargarUrls() {
        viewModelScope.launch {
            try {
                val locales = obtenerUrlsCarga(context)

                if (locales.isNotEmpty()) {
                    _urlsCarga.value = locales
                } else {
                    val desdeFirebase = repo.obtenerUrlsCarga()

                    if (desdeFirebase.isNotEmpty()) {
                        guardarUrlsCarga(context, desdeFirebase)
                        _urlsCarga.value = desdeFirebase
                    }
                }
            } catch (e: Exception) {
                Log.d("img_error", "error al obtenr la img")
            }
        }
    }

    private fun cargar_url_lugares_turitsticos() {
        viewModelScope.launch {
            try {
                val locales = obtenerUrlsCarga_turismo(context)

                if (locales.isNotEmpty()) {
                    _urlsCarga_turistico.value = locales
                } else {
                    val desdeFirebase = repo.obtenerUrlsCarga_lugares_turisticos()

                    if (desdeFirebase.isNotEmpty()) {
                        guardarUrlsCarga_turismo(context, desdeFirebase)
                        _urlsCarga_turistico.value = desdeFirebase
                    }
                }
            } catch (e: Exception) {
                Log.d("img_error", "error al obtenr la img")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtner_datos_tienda(id_tienda: String, localidad: String) {
        try {
        viewModelScope.launch {
            repo.obtener_datos_tienda(
                id_tienda,
                localidad,
                resultado = { res ->
                    Log.d("hoariao;ohafa",res.horario_tiendaMap.sábado.cerrado.toString())
            _estado_carga_widget_tienda.value =res
                },
                error = { error ->
                    _estado_carga_widget_tienda.value = widget_tienda()
                })
        }
        }catch (e: Exception){
         Log.d("Error_obtenr","error al obtenr los datos")
        }
    }
}
package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import com.geinzz.geinzwork.model.repo_principal_geinz_work
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class viewmodel_mapa_personalizado : ViewModel() {
    private val repo_principal_geinz = repo_principal_geinz_work()
    private val repo_lugares = repo_lugares_turisticos()

    private val _estadoLocation = MutableStateFlow(false) // mutable interno
    val estadoLocation: StateFlow<Boolean> = _estadoLocation   // público inmutable

    private val lugares_turisiticos = MutableLiveData<List<lugares_turisticos>>()
    val _lugares_turisticos: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos

    private var todosLosLugares = emptyList<lugares_turisticos>()
    private val _listaFiltrada = MutableStateFlow<List<lugares_turisticos>>(emptyList())
    val listaFiltrada: StateFlow<List<lugares_turisticos>> = _listaFiltrada


    private val _estaCercaTienda = MutableStateFlow(false)
    val estaCercaTienda = _estaCercaTienda.asStateFlow()

    private var ultima_lat_user=0.0
    private var ultima_lon_user=0.0

    private var lat_tienda: Double?=null
    private var lon_tienda: Double?=null


    fun setTienda_selecionada(lat:Double,lon: Double){
        lat_tienda=lat
        lon_tienda=lon
    }
    fun limpiarCoordenadas() {
        lat_tienda = 0.0
        lon_tienda = 0.0

    }


    fun actualizar_ubicacion(lat_user: Double,lon_user: Double){
        val distacia_cambio=calcularDistancia(lat_user,lon_user,ultima_lat_user,ultima_lon_user)
        if(distacia_cambio<2)return
        ultima_lat_user = lat_user
        ultima_lat_user = lon_user

        lat_tienda?.let { latT ->
            lon_tienda?.let { lonT ->
                val distanciaTienda = calcularDistancia(lat_user, lon_user, latT, lonT)
                viewModelScope.launch {
                    _estaCercaTienda.emit(distanciaTienda <= 10)
                }
            }
        }
    }


    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun actualziar_estado (valor: Boolean){
        _estadoLocation.value=valor
    }

    fun lugares_turisticos(localidad: String) {
        viewModelScope.launch {
            try {
                lugares_turisiticos.value =
                    repo_principal_geinz.obtener_lugares_turisticos(localidad)
            } catch (e: Exception) {
                lugares_turisiticos.value = emptyList()
            }
        }
    }

    fun todos_lugares(lista: List<lugares_turisticos>) {
        todosLosLugares = lista
        _listaFiltrada.value = lista
    }

    fun filtrar_por_subcategoria(subcategoria: String) {
        _listaFiltrada.value = if (subcategoria == "Todos") {
            todosLosLugares
        } else {
            todosLosLugares.filter { it.subcategoria_filtrado.contains(subcategoria) }
        }

    }

}
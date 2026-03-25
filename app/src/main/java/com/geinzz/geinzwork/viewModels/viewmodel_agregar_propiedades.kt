package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.geinzz.geinzwork.model.repo_agregar_inmubles
import com.geinzz.geinzwork.model.repo_inmobiliaria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class viewmodel_agregar_propiedades : ViewModel() {

    private val instacia_lures_cercanos = repo_inmobiliaria()

    private val instania_repo_agregar_inmubes = repo_agregar_inmubles()

    // StateFlows para exponer los datos
    private val _titulo = MutableStateFlow("")
    val titulo: StateFlow<String> = _titulo.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()




    suspend fun obtener_lugares_cercanos(
        lat: Double,
        lng: Double,
        localidad: String
    ): List<String> = coroutineScope {
        Log.d("dada", "$lat,$lng,$localidad")

        try {
            val f1 = async {
                instacia_lures_cercanos.obtener_cantidad_lugares_cercanos(lat, lng, localidad)
                    .take(2)
                    .map { it.nombre } // o el campo que tenga el nombre
            }
            val f2 = async {
                instacia_lures_cercanos.obtner_lugares_seguros_cerca(lat, lng, localidad)
                    .take(2)
                    .map { it.nombre }
            }
            val f3 = async {
                instacia_lures_cercanos.obtner_lugares_seguros_cerca_turismo(lat, lng, localidad)
                    .take(2)
                    .map { it.nombre }
            }
            val f4 = async {
                instacia_lures_cercanos.obtener_servicios_esenciales(lat, lng, localidad)
                    .take(2)
                    .map { it.nombre }
            }

            f1.await() + f2.await() + f3.await() + f4.await()

        } catch (e: Exception) {
            Log.d("Error_obtner", "error al obtener los lugares cercanos $e")
            emptyList()
        }
    }

    fun generar_titulo_para_Casa(
        tipo_realizado: String,
        tipo_operacion: String,
        nombre_Calle: String,
        localidad: String,
        lista_lugares: List<String>
    ) {
        viewModelScope.launch {
            try {
                val titulo = instania_repo_agregar_inmubes.generar_titulo_propiedad(
                    tipo_realizado,
                    tipo_operacion,
                    nombre_Calle,
                    localidad
                )


                if (!titulo.isNullOrEmpty()) {
                    _titulo.value=titulo
                   val texto= instania_repo_agregar_inmubes.generar_descripcion(titulo, lista_lugares)
                    if(!texto.isNullOrEmpty()){
                    _descripcion.value=texto
                    }

                }

            } catch (e: Exception) {
                Log.d("error_datos", "erroo al generarar $e")
            }
        }
    }

    private val _nombre_calle = MutableStateFlow("")
    val nombre_calle: StateFlow<String> = _nombre_calle.asStateFlow()

    suspend fun obtenerDireccion(lat: Double, lon: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val token = "pk.eyJ1IjoiYmVuamFtaW5sb3BleiIsImEiOiJjbWZrajJ2NHIxOXBkMmtvZW1kMTA5NWNoIn0.7s_234BN9y0pkTIgtF6ikw"
                val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" +
                        "$lon,$lat.json" +
                        "?access_token=$token" +
                        "&types=address" +
                        "&language=es"

                val response = URL(url).readText() // <-- directo, sin función extra
                val json = JSONObject(response)
                val features = json.getJSONArray("features")

                if (features.length() > 0) {
                    features.getJSONObject(0).getString("place_name")
                } else {
                    null
                }

            } catch (e: Exception) {
                Log.e("Mapbox", "Error: ${e.message}")
                null
            }
        }
    }
    fun buscar_nombre_calle(latitud: Double, longitud: Double) {
        viewModelScope.launch {
            try {
                val direccion =obtenerDireccion(latitud, longitud)
                direccion?.let {
                    _nombre_calle.value = it
                }
            } catch (e: Exception) {
                Log.e("error_calle", "Error: ${e.message}")
            }
        }
    }

}

package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioTienda
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria

import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.repo_filtrado_tiendas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import kotlinx.coroutines.launch

class viewModel_filtado_tiendas : ViewModel() {

    val repo_filtrado = repo_filtrado_tiendas()

    private val subcategorias = MutableLiveData<List<filtrado_tiendas_cat_sub>>()
    val _subcategoiraList: LiveData<List<filtrado_tiendas_cat_sub>> get() = subcategorias

    private val tiendas_filtradas_por_categoria = MutableLiveData<List<tiendas_por_categoria>>()
    val _tiendas_filtradas_por_categoria: LiveData<List<tiendas_por_categoria>> get() = tiendas_filtradas_por_categoria

    private val datos_tienda = MutableLiveData<List<modelo_tienda>>()
    val _datos_tienda: LiveData<List<modelo_tienda>> get() = datos_tienda


    private val _horarioTienda = MutableLiveData<HorarioTienda?>(null)

    val horarioTienda: LiveData<HorarioTienda?> get() = _horarioTienda

//    private val tiendas_por_subcategoria = MutableLiveData<List<tiendas_por_categoria>>()
//    val _tiendas_por_subcategoria: LiveData<List<tiendas_por_categoria>> get() = tiendas_por_subcategoria


    private val _estadoTiendas = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val estadoTiendas: LiveData<Map<String, Boolean>> get() = _estadoTiendas


    var todas_tiendas = mutableListOf<tiendas_por_categoria>()
        private set

    fun tiendas_iniciales(lista: List<tiendas_por_categoria>) {
        Log.d("otbenremos_lista", lista.toString())
        todas_tiendas.clear()
        todas_tiendas.addAll(lista)
    }

    fun filtrar_por_subcategoria(subcategoria: String): List<tiendas_por_categoria> {
        return todas_tiendas.filter { it.lista_subcategoiras.contains(subcategoria) }
    }

    fun filtrar_por_nombre_en_lista(
        nombre: String,
        lista: List<tiendas_por_categoria>
    ): List<tiendas_por_categoria> {
        return lista.filter { it.nombre_tienda.contains(nombre, ignoreCase = true) }
    }


    fun fraces_loadin(localida: String, nombre_user: String, categoria: String): List<String> {
        return listOf(
            "Qué bueno verte por aquí en $localida ...",
            "Buscando tiendas para ti, $nombre_user ...",
            "Buscando tiendas de $categoria ..."
        )
    }

    fun fraces_cargando_filtradas(subcategoria: String, nombre_user: String): List<String> {
        return listOf(
            "Cargandos todas los negocios de $subcategoria ...",
            "Espera un momento $nombre_user ..."
        )
    }

    fun fraces_cargando(nombre_user: String): List<String>{
        return listOf(
            "Estamos cargando todas las tiendas ...",
            "Espera un momento $nombre_user ...",
            "Gracias por la espera ..."
        )
    }

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
                tiendas_filtradas_por_categoria.value = data

            } catch (e: Exception) {
                tiendas_filtradas_por_categoria.value = emptyList()
            }
        }
    }


    fun obtener_campos_tiendas_por_id(localida: String, id_tienda: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenner_campos_tiendas_espesifica(localida, id_tienda)
                datos_tienda.value = data
                Log.d("obtemos_tienda_Selecionda", "${datos_tienda.value}")
            } catch (e: Exception) {
                datos_tienda.value = emptyList()
            }
        }
    }

    fun obtenerHorarioPorTienda_activa(localidad: String, idTienda: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerHorarioPorTienda(idTienda, localidad)
                data?.let { horarioTienda ->
                    val estaAbierto = constantes_lista_localidades.verificarSiEstaAbierto(horarioTienda.lista_Horario)
                    val nuevoMapa = _estadoTiendas.value.orEmpty().toMutableMap()
                    nuevoMapa[idTienda] = estaAbierto
                    _estadoTiendas.postValue(nuevoMapa)
                }
                Log.d("obtenos_dataios_teindas", _horarioTienda.value.toString())
            } catch (e: Exception) {
                Log.d("obtenos_dataios_teindas", "no se econtroa datos")
            }
        }
    }
    fun obtenerHorarioPorTienda(localidad: String, idTienda: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerHorarioPorTienda(idTienda, localidad)
                _horarioTienda.value = data
            } catch (e: Exception) {
                _horarioTienda.value = null
            }
        }
    }





//    fun obtener_tiendas_por_subcategoria(subcategoria: String, localida: String) {
//        viewModelScope.launch {
//            try {
//                val data = repo_filtrado.obtener_tiendas_por_subcateogira(subcategoria, localida)
//                tiendas_por_subcategoria.value = data
//            } catch (e: Exception) {
//                tiendas_por_subcategoria.value = emptyList()
//            }
//        }
//    }

}
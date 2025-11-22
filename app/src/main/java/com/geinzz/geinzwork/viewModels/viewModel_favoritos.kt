package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.model.repo_favoritos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewModel_favoritos( private val id_user: String,) : ViewModel() {
    private val repo_fv = repo_favoritos()
    private val lista_categoria_filtrad = MutableStateFlow<List<String>>(emptyList())
    private val lista_localidad_filtrado = MutableStateFlow<List<String>>(emptyList())
    private val _lista_fv = MutableStateFlow<state_fv>(state_fv.loading)
    val lista_fv: StateFlow<state_fv> get() = _lista_fv

    private val lista_original_items = MutableStateFlow<List<favoritos_guardados>>(emptyList())

    private var listenerRegistrado = false

    init {

        obtener_favoritos(id_user)

    }

    fun obtener_favoritos(id_user: String) {
        // Solo mostrar loading la primera vez
        if (!listenerRegistrado) {
            _lista_fv.value = state_fv.loading
        }

        try {
            repo_fv.obtener_favoritos_realtime(id_user) { pair ->
                listenerRegistrado = true

                val (favoritos, categorias, localidad) = pair

                val categoriasSinRepetir = categorias.distinct()
                val localidad_sin_rep = localidad.distinct()

                lista_categoria_filtrad.value = categoriasSinRepetir
                lista_localidad_filtrado.value = localidad_sin_rep
                val listaIdsLocalidad = favoritos.map { fav ->
                    fav.id_tienda_lugar to fav.localida_tienda
                }

                repo_fv.obtener_timestamps_tiendas(listaIdsLocalidad) { mapTiemposTiendas ->
                    
                }
                if (favoritos.isNotEmpty()) {
                    lista_original_items.value = favoritos
                    _lista_fv.value = state_fv.succes(
                        favoritos,
                        categoriasSinRepetir,
                        localidad_sin_rep
                    )
                } else {
                    _lista_fv.value = state_fv.empty
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _lista_fv.value = state_fv.error("Ocurrió un error, inténtalo nuevamente")
        }
    }


    fun filtrar_categoira(cat: String) {
        val lista_original_fv = lista_original_items.value
        val categoria_filtrado = lista_categoria_filtrad.value
        val localidad_filtrado = lista_localidad_filtrado.value

        viewModelScope.launch {
            try {
                if (cat == "Todos") {
                    _lista_fv.value = state_fv.succes(
                        lista_original_fv,
                        categoria_filtrado,
                        localidad_filtrado
                    )
                } else {
                    val filtrado = lista_original_fv.filter { item ->
                        item.categoria.lowercase() == cat.lowercase()
                    }

                    _lista_fv.value = state_fv.succes(
                        filtrado,
                        categoria_filtrado,
                        localidad_filtrado
                    )
                }
            } catch (_: Exception) {
            }
        }
    }


    fun limpiar_listas_favoritos() {
        lista_original_items.value = emptyList()
        lista_categoria_filtrad.value = emptyList()
        lista_localidad_filtrado.value = emptyList()
    }


    sealed class state_fv {
        object loading : state_fv()
        data class succes(
            val item: List<favoritos_guardados>,
            val lista_categoria: List<String>,
            val localidad_list: List<String>
        ) : state_fv()

        object empty : state_fv()
        data class error(val txt: String) : state_fv()
    }


}
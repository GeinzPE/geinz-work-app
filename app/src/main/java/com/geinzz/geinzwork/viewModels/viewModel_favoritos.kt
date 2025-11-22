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

class viewModel_favoritos(private val id_user: String) : ViewModel() {
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

        if (!listenerRegistrado) {
            _lista_fv.value = state_fv.loading
        }

        viewModelScope.launch {

            try {

                Log.d("favorios123123213123123", "🔵 Registrando listener de favoritos...")

                repo_fv.obtener_favoritos_realtime(id_user) { pair ->

                    listenerRegistrado = true
                    Log.d("favorios123123213123123", "🟢 Favoritos recibidos del realtime listener")

                    val (favoritos, categorias, localidad) = pair

                    val categoriasSinRepetir = categorias.distinct()
                    val localidadSinRep = localidad.distinct()

                    lista_categoria_filtrad.value = categoriasSinRepetir
                    lista_localidad_filtrado.value = localidadSinRep

                    // Preparamos lista (id, localidad)
                    val listaIdsLocalidad = favoritos.map { fav ->
                        fav.id_tienda_lugar to fav.localida_tienda
                    }

                    // Pedimos timestamps remotos de tiendas
                    repo_fv.obtener_timestamps_tiendas(listaIdsLocalidad) { mapTiemposTiendas ->

                        Log.d(
                            "favorios123123213123123",
                            "🟣 Timestamps remotos obtenidos: $mapTiemposTiendas"
                        )

                        favoritos.forEachIndexed { index, fav ->

                            val timestampRemoto =
                                mapTiemposTiendas[fav.id_tienda_lugar]?.toLongOrNull() ?: 0L
                            val timestampLocal = fav.timesLap.toLongOrNull() ?: 0L

                            Log.d(
                                "favorios123123213123123",
                                "⭐ Comparando tienda=${fav.id_tienda_lugar} local=$timestampLocal remoto=$timestampRemoto"
                            )

                            val necesitaActualizar = timestampRemoto > timestampLocal

                            if (necesitaActualizar) {

                                Log.d(
                                    "favorios123123213123123",
                                    "🟠 Necesita actualizar → ${fav.nombre_lugar_tienda}"
                                )

                                viewModelScope.launch {

                                    Log.d(
                                        "favorios123123213123123",
                                        "🔄 Descargando datos nuevos de ${fav.id_tienda_lugar}"
                                    )

                                    val nuevosDatos = repo_fv.obtener_nuevos_datos(
                                        fav.localida_tienda,
                                        fav.id_tienda_lugar
                                    )

                                    Log.d(
                                        "favorios123123213123123",
                                        "🟢 Datos nuevos descargados: $nuevosDatos"
                                    )


                                    val timestampActualizado = timestampRemoto.toString()
                                    val dato_nuevo = favoritos_guardados(
                                        img_tienda = nuevosDatos.img_tienda,
                                        id_tienda_lugar = nuevosDatos.id_tienda_lugar,
                                        nombre_lugar_tienda = nuevosDatos.nombre_lugar_tienda,
                                        categoria = nuevosDatos.categoria,
                                        timesLap = timestampActualizado,
                                        lat = nuevosDatos.lat,
                                        lng = nuevosDatos.lng,
                                        localida_tienda = nuevosDatos.localida_tienda,
                                        estaAbierto = false,
                                        horario_tienda_box = nuevosDatos.horario_tienda_box
                                    )
                                    repo_fv.actalizar_tienda(
                                        dato_nuevo,
                                        id_user,
                                        nuevosDatos.id_tienda_lugar
                                    )

                                }
                            } else {
                                Log.d(
                                    "favorios123123213123123",
                                    "🟢 No necesita actualizar → ${fav.nombre_lugar_tienda}"
                                )
                            }
                        }
                    }

                    // Emitimos éxito
                    if (favoritos.isNotEmpty()) {
                        lista_original_items.value = favoritos
                        _lista_fv.value = state_fv.succes(
                            favoritos,
                            categoriasSinRepetir,
                            localidadSinRep
                        )
                        Log.d("favorios123123213123123", "🟢 Favoritos cargados correctamente")
                    } else {
                        _lista_fv.value = state_fv.empty
                        Log.d("favorios123123213123123", "⚪ No hay favoritos")
                    }
                }

            } catch (e: Exception) {
                Log.e("favorios123123213123123", "🔴 Error cargando favoritos", e)
                _lista_fv.value = state_fv.error("Ocurrió un error, inténtalo nuevamente")
            }
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
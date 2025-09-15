package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_turismo_google_mpa
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_mapa(
    lat_user: Double,
    log_user: Double,
    cameraPositionState: CameraPositionState,
    tipo: String,
    lista_filtrada_turismo: List<lugares_turisticos>,
    lista: List<tiendas_por_categoria>,
    onclose: () -> Unit,
    selecionado_id: (String?) -> Unit,
    datos_selecionado_retornar:(dataclass_map)-> Unit
) {
    ModalBottomSheet(onDismissRequest = { onclose() },containerColor = MaterialTheme.colorScheme.background) {

        when(tipo){
            "turismo"->{
                listado_items(
                    cameraPositionState,
                    lista = lista_filtrada_turismo,
                    getId = { it.id_lugar_turistico },
                    getLat = { it.latitud },
                    getLng = { it.longitud },
                    getLogo = { it.img_ref },
                    getNombre = { it.titulo },
                    getDescripcion = { it.descripcion },
                    selecionado = { id ->
                        selecionado_id(id.id_lugar_turistico)
                        datos_selecionado_retornar(dataclass_map(
                            id.id_lugar_turistico,
                            id.titulo,
                            id.subcategoria_filtrado,
                            lat_user,
                            log_user,
                            id.latitud,
                            id.longitud,
                            id.id_lugar_turistico,
                            "",
                            id.direcccion,
                            id.referencia
                        ))
                    }

                )

            }
            "tiendas"->{
                listado_items(
                    cameraPositionState,
                    lista = lista,
                    getId = { it.id_tienda },
                    getLat = { it.latitud },
                    getLng = { it.longitud },
                    getLogo = { it.logo_tienda },
                    getNombre = { it.nombre_tienda },
                    getDescripcion = { it.descripcion },
                    selecionado = { id ->
                        selecionado_id(id.id_tienda)
                        datos_selecionado_retornar(dataclass_map(
                            id.logo_tienda,
                            id.nombre_tienda,
                            id.lista_subcategoiras,
                            lat_user,
                            log_user,
                            id.latitud,
                            id.longitud,
                            id.id_tienda,
                            "",
                            id.direccion,
                            id.referencia,
                        ))
                    }
                )
            }

            else->{}
        }
//        listado_items(cameraPositionState,lista){selecionado->
//            selecionado_id(selecionado)
//        }
    }

}

//@Composable
//fun listado_items(cameraPositionState: CameraPositionState,
//                  lista: List<tiendas_por_categoria>,selecionado:(String?)-> Unit) {
//    var latitud by remember { mutableStateOf(0.0) }
//    var longitud by remember { mutableStateOf(0.0) }
//    var seleccionadoId by remember { mutableStateOf<String?>(null) }
//    val scope = rememberCoroutineScope()
//
//    LazyColumn {
//        items(lista) { tiendas ->
//            carta_turismo_google_mpa(
//                tiendas.id_tienda,
//                tiendas.latitud,
//                tiendas.longitud,
//                tiendas.logo_tienda,
//                tiendas.nombre_tienda,
//                tiendas.descripcion,
//                seleccionado = (seleccionadoId == tiendas.id_tienda)
//            ) { id, lat, log ->
//                val nuevaUbicacion = LatLng(lat, log)
//                seleccionadoId = id
//                selecionado(seleccionadoId)
//                latitud = lat
//                longitud = log
//                scope.launch {
//                    cameraPositionState.animate(
//                        CameraUpdateFactory.newLatLngZoom(nuevaUbicacion, 16f),
//                        1000
//                    )
//                }
//            }
//        }
//    }
//
//}

@Composable
fun <T> listado_items(
    cameraPositionState: CameraPositionState,
    lista: List<T>,
    getId: (T) -> String,
    getLat: (T) -> Double,
    getLng: (T) -> Double,
    getLogo: (T) -> String,
    getNombre: (T) -> String,
    getDescripcion: (T) -> String,
    selecionado: (T) -> Unit // ← ahora retorna el objeto completo
) {
    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn {
        items(lista) { item ->
            carta_turismo_google_mpa(
                getId(item),
                getLat(item),
                getLng(item),
                getLogo(item),
                getNombre(item),
                getDescripcion(item),
                seleccionado = (seleccionadoId == getId(item))
            ) { id, lat, log ->
                val nuevaUbicacion = LatLng(lat, log)
                seleccionadoId = id

                // 🔹 Aquí en vez de mandar solo el id, mandamos todo el item
                selecionado(item)

                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(nuevaUbicacion, 16f),
                        1000
                    )
                }
            }
        }
    }
}


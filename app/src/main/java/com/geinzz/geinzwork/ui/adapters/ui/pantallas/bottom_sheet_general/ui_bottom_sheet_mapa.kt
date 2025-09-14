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
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.carta_turismo_google_mpa
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_mapa(cameraPositionState: CameraPositionState, lista: List<tiendas_por_categoria>, onclose: () -> Unit,selecionado_id:(String?)-> Unit) {
    ModalBottomSheet(onDismissRequest = { onclose() },containerColor = MaterialTheme.colorScheme.background) {
        listado_items(cameraPositionState,lista){selecionado->
            selecionado_id(selecionado)
        }
    }

}

@Composable
fun listado_items(cameraPositionState: CameraPositionState, lista: List<tiendas_por_categoria>,selecionado:(String?)-> Unit) {
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn {
        items(lista) { tiendas ->
            carta_turismo_google_mpa(
                tiendas.id_tienda,
                tiendas.latitud,
                tiendas.longitud,
                tiendas.logo_tienda,
                tiendas.nombre_tienda,
                tiendas.descripcion,
                seleccionado = (seleccionadoId == tiendas.id_tienda)
            ) { id, lat, log ->
                val nuevaUbicacion = LatLng(lat, log)
                seleccionadoId = id
                selecionado(seleccionadoId)
                latitud = lat
                longitud = log
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
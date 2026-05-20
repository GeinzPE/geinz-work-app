package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.HorarioSemanal123
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_left
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_right
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun eres_socio_geinz(
    isConnected: Boolean,
    _tick: Long,
    ondimis: () -> Unit,
    datos: datos_tienda
) {
    Log.d("hoaerirodehoy22",datos.horario_tiendaMap.sábado.cerrado.toString())
    val viewmodel: viewmodel_eres_socio = viewModel()
    val viewModelFiltros: viewModel_filtado_tiendas = viewModel()
    var id_tienda by remember { mutableStateOf("") }
    var horarioMap by remember { mutableStateOf(HorarioAtencion_box()) }
    LaunchedEffect(id_tienda, horarioMap) {
        viewModelFiltros.calcularHorarioParaTienda(id_tienda, horarioMap)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        FuenteControladaApp {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 10.dp)
            ) {
                LazyColumn() {
                    item {
                        Box(
                            Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                                Box(
                                    modifier = Modifier
                                        .animateContentSize()
                                ) {

                                    HorarioSemanal123(
                                        "hoy",
                                        id_tienda = datos.id_tienda,
                                        tick = _tick,
                                        viewModelFiltros = viewModelFiltros,
                                        isConnected = isConnected,
                                        horario = datos.horario_tiendaMap,
                                        cerrar_tienda = { nombre_dia, motivo_cierre, lista ->
                                            viewmodel.cambiar_cerrado(
                                                datos.id_tienda,
                                                nombre_dia,
                                                motivo_cierre,
                                                lista
                                            )
                                        },
                                        abrir_tienda = { dia, lista_horarios ->
                                            viewmodel.cambiar_abierto(
                                                datos.id_tienda,
                                                dia,
                                                lista_horarios
                                            )
                                        },
                                        error_sin_internet = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "No puedes realizar cambios sin conexion a internet",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        },
                                        onclick_expand = {},
                                        error_campos_incompletos = { scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Por favor, completa todos los campos antes de actualizar el horario.",
                                                duration = SnackbarDuration.Short
                                            )
                                        } },
                                        { msje ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = msje,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        },shadow_left,shadow_right)
                                }
                        }
                    }
                }
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}



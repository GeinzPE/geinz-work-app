package com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite

@Composable
fun dialog_salid_seguridad_inmobiliaria(
    id_select: String,
    localidad: String,
    ondismis: () -> Unit
) {

    val viewmodel_seguridad: viewmode_seguridad_salud = viewModel()
    val state_obtener_datos by viewmodel_seguridad.estado_carga_datos_contacto.collectAsState()

    LaunchedEffect(id_select, localidad) {
        viewmodel_seguridad.otener_contatos_numeros_emergencia(id_select, localidad)
    }




    AlertDialog(
        onDismissRequest = { ondismis() },
        confirmButton = {},
        dismissButton = {},
        title = {
            FuenteControladaApp {
                texto_generico_one_line("Números de emergencia")
            }
        },
        text = {
            FuenteControladaApp {
                when (state_obtener_datos) {
                    is viewmode_seguridad_salud.carga_contanto_emergencia.empty -> {}

                    is viewmode_seguridad_salud.carga_contanto_emergencia.error -> {}

                    is viewmode_seguridad_salud.carga_contanto_emergencia.idle -> {}

                    is viewmode_seguridad_salud.carga_contanto_emergencia.loading -> {
                        CircularProgressIndicator()
                    }

                    is viewmode_seguridad_salud.carga_contanto_emergencia.succes -> {
                        val datos =
                            (state_obtener_datos as viewmode_seguridad_salud.carga_contanto_emergencia.succes).data

                    }
                }
            }
        }
    )


}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import io.ktor.client.content.LocalFileContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun eres_socio_geinz(ondimis: () -> Unit) {
    val context = LocalContext.current
    var id_registrado by remember { mutableStateOf("") }
    val viewmode_eres_socio: viewmodel_eres_socio = viewModel()
    val state_socio = viewmode_eres_socio.state_eres_socio.collectAsState()
    val uid_respald_user by data_store_localidad.get_id_socio(context).collectAsState(initial = "")
    LaunchedEffect(state_socio) {
        Log.d("estadosocio", "${state_socio.value}")
    }

    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Box() {
                if (uid_respald_user.isEmpty()) {
                    Column(
                        Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        texto_generico_one_line(
                            "Eres socio de Geinz?",
                            style = MaterialTheme.typography.titleLarge
                        )

                        spacer_vertical(10.dp)

                        texto_generico_multilinea(
                            "Ingresa tu ID y descubre el impacto real de tu negocio. Mira cuántas personas visitaron tu tienda, cuántos la guardaron como favorita y actualiza tu horario en solo segundos. Todo diseñado para que tengas el control total de tu crecimiento y te sientas parte de la experiencia exclusiva de GEINZ.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        spacer_vertical(10.dp)

                        MyOutlinedTextField(
                            value = id_registrado,
                            onValueChange = { id_registrado = it },
                            labelText = "Pega tu ID",
                            placeholderText = "Pega tu ID",
                        )

                        spacer_vertical(10.dp)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    viewmode_eres_socio.verificar_seccion(context, id_registrado)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            texto_generico_one_line(
                                "Acceder",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        spacer_vertical(20.dp)
                    }

                } else {

                    LaunchedEffect(uid_respald_user) {
                        viewmode_eres_socio.verificar_seccion(context, uid_respald_user)
                    }

                    Column(
                        Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        texto_generico_one_line("Bienvenido a GEINZ PANEL")
                    }
                }

            }

        }
    }
}
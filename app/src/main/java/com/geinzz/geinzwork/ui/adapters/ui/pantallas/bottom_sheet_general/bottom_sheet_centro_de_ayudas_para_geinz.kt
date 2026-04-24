package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_centro_de_Ayudas_pra_geinz(
    onDismissRequest: () -> Unit,
    mostar_link: (url: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                texto_generico_one_line("Centro Legal y Ayuda",   style = MaterialTheme.typography.titleLarge)

                texto_generico_multilinea(
                    "En Geinz priorizamos tu seguridad. Aquí puedes revisar nuestros términos legales, políticas de protección y el libro de reclamaciones.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
                Button(
                    onClick = {
                        mostar_link("https://geinzwork.web.app/terminos_y_condiciones.html")
//                        mostrar_webview_terminos_condiciones =true
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    texto_generico_one_line(
                        "\uD83D\uDCDC\uD83D\uDC99 Términos y condiciones",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Button(
                    onClick = {
                        mostar_link("https://geinzwork.web.app/politicas_devoluciones.html")
//                        mostrar_webview_politicas_devoluciones=true
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    texto_generico_one_line(
                        "\uD83D\uDCC4 Politica de cambios y devoluciones",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }


                Button(
                    onClick = {
                        mostar_link("https://geinzwork.firebaseapp.com/libro_reclamaciones.html")
//                        mostrar_webview_libro_recalmaciones=true
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    texto_generico_one_line(
                        "\uD83D\uDCD8 Libro de Reclamaciones",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                spacer_vertical(20.dp)
            }
        }
    }
}
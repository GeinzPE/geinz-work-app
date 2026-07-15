package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_creadtior_quees_geinzz(
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp), // Espacio al final para que no pegue al borde
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centra la imagen y textos
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.icon_monedas_3d), // Tu icono 3D
                                contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(top = 10.dp),
                                contentScale = ContentScale.Fit
                            )

                        }

                        texto_generico_multilinea(
                            "¿Cómo funcionan los créditos Geinz?",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                    }

                    item {
                        texto_generico_multilinea(
                            texto = "Impulsa tu negocio con los créditos Geinz, la herramienta oficial de crecimiento para comercios en el Norte Chico. " +
                                    "Al adquirirlos y gestionar tu panel premium, tomas el control total de tu presencia digital:\n\n" +
                                    "• Gestión en Tiempo Real: Actualiza tus horarios, contactos, redes sociales y métodos de pago al instante.\n" +
                                    "• Escaparate Visual: Cambia tus fotos de perfil, ambiente y productos para mantener tu negocio siempre atractivo.\n" +
                                    "• Marketing Directo: Utiliza tus créditos para enviar notificaciones push a tus seguidores y publicar promociones exclusivas con mayor alcance.\n" +
                                    "• Inteligencia de Negocio: Accede a estadísticas detalladas sobre cuántas personas visualizan, guardan o interactúan con tu perfil.\n" +
                                    "• IA Geinz: Tu negocio será prioridad para nuestro asistente de WhatsApp, recomendándote activamente en conversaciones con usuarios locales.\n\n" +
                                    "Cada crédito es una inversión para que tu comercio no solo sea visible, sino que sea el preferido en el norte chico.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 22.sp
                            )
                        )
                    }
                    item {
                        texto_generico_multilinea(
                            "Nota: Los créditos son herramientas de visibilidad publicitaria y no poseen valor monetario fuera de Geinz.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
                spacer_vertical(10.dp)

            }
        }
    }
}
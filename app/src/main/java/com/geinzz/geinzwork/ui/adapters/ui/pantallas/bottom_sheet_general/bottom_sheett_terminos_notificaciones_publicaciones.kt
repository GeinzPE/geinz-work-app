package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosNotificacionesUI(
    ondimis: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        FuenteControladaApp {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                // Título
                Text(
                    text = "Términos de uso de notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Para mantener una experiencia segura y de calidad, el uso de notificaciones debe cumplir las siguientes condiciones:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                // Permitido
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "✅ Contenido permitido",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        ItemTermino("Promociones, descuentos y ofertas reales")
                        ItemTermino("Información de servicios o productos")
                        ItemTermino("Recordatorios relacionados a pedidos o reservas")
                        ItemTermino("Actualizaciones importantes de la tienda")
                    }
                }

                // No permitido
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "❌ Contenido no permitido",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        ItemTermino("Contenido político o propaganda ideológica")
                        ItemTermino("Contenido sexual o para adultos")
                        ItemTermino("Mensajes de odio, violencia o discriminación")
                        ItemTermino("Incitación fuerte a la ludopatía o apuestas")
                        ItemTermino("Spam, mensajes engañosos o repetitivos")
                    }
                }

                // Nota final
                Text(
                    text = "El incumplimiento de estas normas puede ocasionar la suspensión temporal o permanente del envío de notificaciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosPublicacionesPromocionesUI(
    ondimis: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = { ondimis() },

        containerColor = MaterialTheme.colorScheme.background,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        FuenteControladaApp {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {

                    // Título
                    Text(
                        text = "Políticas de publicaciones y promociones",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                }

                item {
                    Text(
                        text = "Al crear y publicar contenido dentro de la plataforma, el usuario acepta cumplir las siguientes normas:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
                item {
                    // Responsabilidades
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Responsabilidades del publicador",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            ItemTermino("Verificar que la información publicada sea clara, veraz y actualizada.")
                            ItemTermino("Revisar cuidadosamente el contenido antes de publicarlo.")
                            ItemTermino("Respetar los días, horarios y duración definidos para cada publicación.")
                            ItemTermino("Cumplir con las condiciones ofrecidas en promociones y servicios.")
                            ItemTermino("Mantener coherencia entre el contenido publicado y el servicio real brindado.")
                        }
                    }
                }
                item {
                    // Contenido prohibido
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Contenido no permitido",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )

                            ItemTermino("Información falsa, engañosa o que induzca a error.")
                            ItemTermino("Promociones que no puedan ser cumplidas.")
                            ItemTermino("Contenido fraudulento, confuso o manipulado.")
                            ItemTermino("Publicaciones repetitivas con fines de spam.")
                            ItemTermino("Contenido que infrinja leyes, normas o derechos de terceros.")
                        }
                    }
                }
                item {
                    // Consecuencias
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Medidas y sanciones",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            ItemTermino("La plataforma puede revisar, modificar o retirar publicaciones.")
                            ItemTermino("El incumplimiento puede derivar en suspensión temporal o permanente.")
                            ItemTermino("Reincidencias pueden limitar el acceso a futuras publicaciones.")
                        }
                    }
                }
                item {
                    Text(
                        text = "Estas políticas buscan garantizar transparencia, confianza y una experiencia segura para todos los usuarios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemTermino(texto: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

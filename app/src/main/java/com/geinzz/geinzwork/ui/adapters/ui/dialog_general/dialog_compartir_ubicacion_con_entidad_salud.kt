package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@Composable
fun dialog_compartir_ubicacion_con_entidad_salud(
    ondismis: () -> Unit,
    entidad: String,
    img_entidad: String
) {
    AlertDialog(
        onDismissRequest = { ondismis() },
        confirmButton = {

        },

        icon = {
            Box(
                modifier = Modifier.size(45.dp)
            ) {
                AsyncImage(
                    model = img_entidad,
                    contentDescription = null,
                    contentScale = ContentScale.Crop, // 🔥 IMPORTANTE
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                )
            }
        },
        text = {
            FuenteControladaApp {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    texto_generico_multilinea(
                        """
Al activar esta opción, se enviará tu ubicación actual a $entidad mediante un mensaje automático de WhatsApp.

La ubicación será compartida como un enlace de Google Maps con las coordenadas exactas del lugar donde te encuentras en ese momento.

Esto permitirá que la entidad pueda llegar de forma más rápida y precisa ante cualquier incidente.

""".trimIndent(), style = MaterialTheme.typography.bodyMedium
                    )


                }
            }
        }

    )
}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.content.Context

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_redes.openPlayStore
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun verificar_version(
    context: Context,
    nueva_version: String,
    cambiosrealizados: String,
    ondimis: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LazyColumn(Modifier.padding(10.dp)) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            texto_generico_multilinea(
                                "Nueva version disponible",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    item {
                        spacer_vertical(10.dp)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            texto_generico_one_line(cambiosrealizados)
                        }
                    }
                    item {
                        spacer_vertical(10.dp)
                        texto_generico_multilinea(
                            nueva_version,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        spacer_vertical(10.dp)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    openPlayStore(
                                        context = context,
                                        appPackage = "com.geinzz.geinzwork"
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line(
                                "Ir a actualizar",
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Image(
                    painter = painterResource(R.drawable.logo_google_play),
                    contentDescription = "", modifier = Modifier.clip(CircleShape)
                        .size(220.dp)
                        .graphicsLayer {
                            alpha = 0.5f
                        }
                )
            }


        }
    }
}
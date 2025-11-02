package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import io.ktor.client.content.LocalFileContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ui_bottom_sheet_errores(viewModel_login_user: viewModel_login_user, ondimis: () -> Unit) {
    val errores by viewModel_login_user._lista_errores.collectAsState()
val context= LocalContext.current
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Errores capturados", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            if (errores.isEmpty()) {
                Text("Sin errores registrados", color = Color.Gray)
            } else {
                errores.forEach { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val texto = errores.joinToString("\n\n")
                       constantestextos_general.copiarTexto_portapapeles_compouse(texto,context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copiar todos los errores")
                }
            }
        }
    }
}
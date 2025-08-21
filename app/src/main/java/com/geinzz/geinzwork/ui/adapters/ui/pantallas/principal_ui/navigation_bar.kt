package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.nav_item
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun bottom_navigation() {
    val context = LocalContext.current
    val items = listOf(
        nav_item("Inicio", Icons.Default.Home),
        nav_item("Buscar", Icons.Default.Search),
        nav_item("Favoritos", Icons.Default.Star),
        nav_item("Cuenta", Icons.Default.Person),
    )

    var selected_item by remember { mutableIntStateOf(0) }
    val startScanner = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result -> handleScanResult(context, result?.contents) }
    )
    Box {
        NavigationBar {
            // Primer ítem
            Geinz_bottom_var(items[0], selecionado = selected_item == 0) {
                selected_item = 0
            }
            Geinz_bottom_var(items[1], selecionado = selected_item == 1) {
                selected_item = 1
            }

            Spacer(modifier = Modifier.weight(1f)) // espacio para el botón central

            // Últimos ítems
            Geinz_bottom_var(items[2], selecionado = selected_item == 2) {
                selected_item = 2
            }
            Geinz_bottom_var(items[3], selecionado = selected_item == 3) {
                selected_item = 3
            }
        }

        val navigationBarHeight = 80.dp // o el alto real de tu bottom bar

        FloatingActionButton(
            onClick = {startScanner.launch(ScanOptions())},
            containerColor = Color.Red,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(navigationBarHeight / 2))
        ) {
            Icon(
               painter = painterResource(R.drawable.qr_scaner_icon), contentDescription = "Agregar", modifier = Modifier.size(35.dp)
            )
        }

    }
}
private fun handleScanResult(context: Context, contenidoEscaneado: String?) {
    if (contenidoEscaneado.isNullOrEmpty()) {
        Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        return
    }

    Log.d("obtenoemos_resultado", contenidoEscaneado)

    try {
        if (contenidoEscaneado.startsWith("Tienda|")) {
            val base64Coordenadas = contenidoEscaneado.removePrefix("Tienda|")
            val (lat, lng) = generar_qr_cordenadas_tienda.decodificarCoordenadas(base64Coordenadas)
            constantes_lista_localidades.abrir_google_maps(context, lat, lng) { dialogo ->
                if (dialogo) Toast.makeText(
                    context,
                    "Activa tu ubicación primero",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Aquí podrías llamar al ViewModel si fuera necesario
            Log.d("Scanner", "Otro tipo de QR: $contenidoEscaneado")
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al decodificar coordenadas", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}


@Composable
fun RowScope.Geinz_bottom_var(navItem: nav_item, selecionado: Boolean, clikeado: () -> Unit) {
    NavigationBarItem(
        selected = selecionado,
        onClick = { clikeado() },
        icon = { Icon(imageVector = navItem.icon, contentDescription = "") },
        label = { texto_generico_one_line(navItem.nombre_item) },
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,      // Ícono seleccionado
            unselectedIconColor = Color.Gray,  // Ícono no seleccionado
            selectedTextColor = Color.White,
            indicatorColor = Color.Red
        )
    )
}
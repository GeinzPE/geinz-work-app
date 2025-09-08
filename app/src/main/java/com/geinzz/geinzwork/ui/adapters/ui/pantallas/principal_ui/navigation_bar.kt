package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.Items_menu
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.nav_item
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun bottom_navigation(navController: NavController) {
    val context = LocalContext.current
    val items = listOf(
        Items_menu.pantalla1,
        Items_menu.pantalla2,
        Items_menu.pantalla3,
        Items_menu.pantalla4
    )


    var selected_item by remember { mutableIntStateOf(0) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startScanner = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result -> handleScanResult(context, result?.contents) }
    )
    Box {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
            containerColor = Color(0xFF744ACB)
        ) {

            items.forEachIndexed { index, item ->
                if (index == 2) {
                    Spacer(modifier = Modifier.width(50.dp))
                }

                Geinz_bottom_var(
                    navItem = nav_item(item.titulo, item.icono),
                    selecionado = currentRoute == item.ruta
                ) {
                    if (currentRoute != item.ruta) {
                        navController.navigate(item.ruta) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }

        val navigationBarHeight = 100.dp

        FloatingActionButton(
            onClick = { startScanner.launch(ScanOptions()) },
            containerColor = Color(0xFF4F378B),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(navigationBarHeight / 2))
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.qr_scaner_icon),
                contentDescription = "Agregar",
                modifier = Modifier.size(35.dp)
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
            Log.d("Scanner", "Otro tipo de QR: $contenidoEscaneado")
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al decodificar coordenadas", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

@Composable
fun HandleBackPress(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BackHandler {
        when (currentRoute) {
            "pantalla_principal" -> {
                // salir de la app
                (navController.context as? android.app.Activity)?.finish()
            }

            else -> {
                // volver a la pantalla anterior
                navController.popBackStack()
            }
        }
    }
}


@Composable
fun RowScope.Geinz_bottom_var(navItem: nav_item, selecionado: Boolean, clikeado: () -> Unit) {
    NavigationBarItem(
        selected = selecionado,
        onClick = { clikeado() },
        icon = { Icon(imageVector = navItem.icon, contentDescription = "") },
        label = {
            texto_generico_one_line(
                navItem.nombre_item,
                MaterialTheme.typography.bodyMedium
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            unselectedIconColor = Color.White,
            selectedTextColor = Color.White,
            indicatorColor = Color(0xFF4A4458)
        )
    )
}
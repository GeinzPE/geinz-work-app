package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.Items_menu
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.nav_item
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_review
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

    var bottom_sheet by remember { mutableStateOf(false) }
    var id_tienda_review by remember { mutableStateOf(data_class_review("", "")) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startScanner = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            handleScanResult(context, result?.contents) { id_Tienda ->
                val datos_tienda=data_class_review(id_Tienda,"barranca")
                id_tienda_review = datos_tienda
                bottom_sheet = true
            }
        }
    )
    Box {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
            containerColor = Color.Black
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
            onClick = {
                startScanner.launch(ScanOptions())
            },
            containerColor = Color(0xFF8700F3), // un púrpura más suave que el negro
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(navigationBarHeight / 2))
                .size(60.dp)
                .shadow(
                    elevation = 12.dp, // elevación
                    shape = CircleShape,
                    ambientColor = Color.White.copy(alpha = 0.6f), // neblina blanca
                    spotColor = Color.White.copy(alpha = 0.4f)
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.qr_scaner_icon),
                contentDescription = "Agregar",
                modifier = Modifier.size(35.dp)
            )
        }

    }

    if (bottom_sheet) {
        Log.d("id_tienda_reviewid_tienda_review",id_tienda_review.toString())
        bottom_sheet_review(id_tienda_review) {
            bottom_sheet = !bottom_sheet
        }
    }


}

private fun handleScanResult(
    context: Context,
    contenidoEscaneado: String?,
    open_review: (id_Tienda: String) -> Unit
) {
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
        } else if (contenidoEscaneado.startsWith("Review|")) {
            val base64Coordenadas = contenidoEscaneado.removePrefix("Review|")
            open_review(base64Coordenadas)
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
            indicatorColor = Color(0xFF8700F3)
        )
    )
}
package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.Items_menu
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.nav_item
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_Sheet_seguro
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_review
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.estaDentroDeTienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewmodel_review
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.remoteMessage

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private lateinit var firebaseAuth: FirebaseAuth

@SuppressLint("MissingPermission")
@Composable
fun bottom_navigation(navController: NavController) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val items = listOf(
        Items_menu.pantalla1,
        Items_menu.pantalla2,
        Items_menu.pantalla3,
        Items_menu.pantalla4
    )

    val viewmodel: viewmodel_review = viewModel()
    var selected_item by remember { mutableIntStateOf(0) }
    var dialog_estas_tienda by remember { mutableStateOf(false) }
    var validacion_tienda_cordenadas by remember { mutableStateOf(false) }
    var bottom_sheet by remember { mutableStateOf(false) }
    var bottom_sheet_review_privado by remember { mutableStateOf(false) }
    var id_tienda_review by remember { mutableStateOf(data_class_review("", "")) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var dialogo_ubi_activa by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var latitude_tienda by remember { mutableStateOf(0.0) }
    var longitude_tienda by remember { mutableStateOf(0.0) }

    var estado_presencial_tienda_lugar by remember { mutableStateOf(false) }
    var rango_estrellas by remember { mutableStateOf(0) }
    var descripcion by remember { mutableStateOf("") }
    var estado_form_review by remember { mutableStateOf("") }

    var segun_user_tienda by remember { mutableStateOf(false) }

    var datos_review by remember { mutableStateOf(datos_review()) }

    val startScanner = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            handleScanResult(
                context,
                result?.contents,
                crear_ruta = { lat, lng ->
                    constantes_lista_localidades.abrir_google_maps(context, lat, lng) { dialogo ->
                        if (dialogo) Toast.makeText(
                            context,
                            "Activa tu ubicación primero",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                open_review_p = { id_tienda, localidad, latitude, longitude ->
                    dialog_estas_tienda = true
                    latitude_tienda = latitude
                    longitude_tienda = longitude
                    id_tienda_review = data_class_review(id_tienda, localidad)


                },
                open_review_public = { id_tienda, localidad ->
                    id_tienda_review = data_class_review(id_tienda, localidad)
                    bottom_sheet = true
                    estado_form_review = "normal"
                })

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

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (verificarUbiActiva(context)) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    val (distancia, dentro) = estaDentroDeTienda(
                        userLatLng.latitude,
                        userLatLng.longitude,
                        latitude_tienda,
                        longitude_tienda
                    )

                    estado_presencial_tienda_lugar = dentro

                    Log.d(
                        "obtenoemos_la_tog",
                        "userprimario = $userLatLng:  $estado_presencial_tienda_lugar"
                    )

                    viewmodel.agregar_review(
                        crearReview(
                            rango_estrellas,
                            descripcion,
                            estado_presencial_tienda_lugar,
                            id_tienda_review.id_tienda_lugar,
                            id_tienda_review.localida_lugar
                        )
                    )
                }
            } else {
                dialogo_ubi_activa = true
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT)
                .show()
        }
    }
    if (dialog_estas_tienda) {
        dialog_verificar_si_esta_tienda(
            onClose = { dialog_estas_tienda = false },
            rpa_si = {
                dialog_estas_tienda = false
                bottom_sheet_review_privado = true
                segun_user_tienda = true
            },
            rpa_no = {
                dialog_estas_tienda = false
                bottom_sheet_review_privado = true
                segun_user_tienda = false

            })
    }

    if(bottom_sheet_review_privado){
        bottom_Sheet_seguro(viewmodel,id_tienda_review, ondimis = {
            bottom_sheet_review_privado=!bottom_sheet_review_privado
        }, clik_envio = { ratingValue, texto ->
                if (segun_user_tienda) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        rango_estrellas = ratingValue
                        descripcion = texto
                        if (verificarUbiActiva(context)) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                Log.d("ReviewUbicacion", "Entró al addOnSuccessListener...")
                                val userLatLng = LatLng(location.latitude, location.longitude)

                                val (distancia, dentro) = estaDentroDeTienda(
                                    userLatLng.latitude,
                                    userLatLng.longitude,
                                    latitude_tienda,
                                    longitude_tienda
                                )

                                estado_presencial_tienda_lugar = dentro

                                Log.d(
                                    "ReviewUbicacion",
                                    "Datos para review -> rango: $rango_estrellas, texto: $descripcion, tiendaId: ${id_tienda_review.id_tienda_lugar}, localidad: ${id_tienda_review.localida_lugar}"
                                )

                                viewmodel.agregar_review(
                                    crearReview(
                                        rango_estrellas,
                                        descripcion,
                                        estado_presencial_tienda_lugar,
                                        id_tienda_review.id_tienda_lugar,
                                        id_tienda_review.localida_lugar
                                    )
                                )

                                Log.d("ReviewUbicacion", "✅ Review enviada correctamente")
                            }
                        } else {
                            dialogo_ubi_activa = true
                        }

                    } else {
                        permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }


                } else {
                    viewmodel.agregar_review(
                        crearReview(
                            ratingValue,
                            texto,
                            false,
                            id_tienda_review.id_tienda_lugar,
                            id_tienda_review.localida_lugar
                        )
                    )
                }

        })
    }


    if (bottom_sheet) {
        bottom_sheet_review(tipo=estado_form_review, viewmodel=viewmodel, data_class_review=id_tienda_review,  ondimis= {
           bottom_sheet = !bottom_sheet
        },clik_envio= { ratingValue, texto ->
            viewmodel.agregar_review(
                crearReview(
                    ratingValue,
                    texto,
                    true,
                    id_tienda_review.id_tienda_lugar,
                    id_tienda_review.localida_lugar
                )
            )
        })
    }

    if (dialogo_ubi_activa) {
        dialog_sin_ubi__rutas(
            onDismis = { dialogo_ubi_activa = false },
            abrir_configuracion = {
                dialogo_ubi_activa = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

            }
        )
    }

}

private fun handleScanResult(
    context: Context,
    contenidoEscaneado: String?,
    crear_ruta: (lat: Double, long: Double) -> Unit,
    open_review_p: (id_Tienda: String, localidad: String, latitude: Double, longitude: Double) -> Unit,
    open_review_public: (id_tienda: String, localidad: String) -> Unit
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
            crear_ruta(lat, lng)
        } else if (contenidoEscaneado.startsWith("Review_C|")) {
            val partes = contenidoEscaneado.split("|")
            if (partes.size >= 4) {
                val idTienda = partes[1]
                val base64Coordenadas = partes[3]
                val (lat, lng) = generar_qr_cordenadas_tienda.decodificarCoordenadas(
                    base64Coordenadas
                )
                open_review_p(idTienda, "barranca", lat, lng)
            } else {
                Toast.makeText(context, "Formato QR inválido", Toast.LENGTH_SHORT).show()
            }

        } else if (contenidoEscaneado.startsWith("Review|")) {
            val base64Coordenadas = contenidoEscaneado.removePrefix("Review|")
            open_review_public(base64Coordenadas, "barranca")
        } else {
            Log.d("Scanner", "Otro tipo de QR: $contenidoEscaneado")
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al decodificar coordenadas", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dialog_verificar_si_esta_tienda(onClose: () -> Unit, rpa_si: () -> Unit, rpa_no: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onClose() }, confirmButton = {
            Button(onClick = { rpa_si() }) { texto_generico_one_line("Si") }
        },
        dismissButton = { TextButton(onClick = { rpa_no() }) { texto_generico_one_line("no") } },
        title = { texto_generico_one_line("verificacion de entrada") },
        text = { "Te ecuentras presencial mente en la tienda localizada?" },
        icon = {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Ubicación",
                modifier = Modifier.size(25.dp)
            )
        }, properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )

    )
}

fun crearReview(
    ratingValue: Int,
    texto: String,
    presencial: Boolean,
    id_tienda_lugar: String,
    localida_lugar: String
) = datos_review(
    id_usuario = firebaseAuth.uid.toString(),
    cantidad_Strar = ratingValue,
    descripcion_review = texto,
    verificado_presencial = presencial,
    id_tienda_lugar = id_tienda_lugar,
    localidad_tienda = localida_lugar,
    hora = "",
    fecha = ""
)


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
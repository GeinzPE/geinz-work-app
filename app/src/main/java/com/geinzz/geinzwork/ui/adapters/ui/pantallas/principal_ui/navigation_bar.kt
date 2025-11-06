package com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.nav_item
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_Sheet_seguro
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_review
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.componentes.SnackbarHost
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.estaDentroDeTienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewmodel_review
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

private lateinit var firebaseAuth: FirebaseAuth

@SuppressLint("MissingPermission", "SuspiciousIndentation")
@Composable
fun bottom_navigation(
    datos_principales_user: datos_principales_user,
    navController: NavController,  crear_cuenta:()-> Unit,iniciar_seccion:()-> Unit
) {
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

    var segun_user_tienda by remember { mutableStateOf(false) }

    var datos_review by remember { mutableStateOf(datos_review()) }
    var esta_o_no_lugar by remember { mutableStateOf(false) }

    var mostar_snackvar_reivew by remember { mutableStateOf(false) }
    var scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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
        SnackbarHost(snackbarHostState,Modifier.align(Alignment.BottomCenter))

    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GPS", "✅ El usuario activó el GPS")

        } else {
            Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")

        }
    }
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            dialog_estas_tienda = false
            bottom_sheet_review_privado = true
            segun_user_tienda = true
        } else {
            Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT)
                .show()
        }
    }

    if (dialog_estas_tienda) {
        dialog_verificar_si_esta_tienda(
            onClose = { dialog_estas_tienda = false },
            rpa_si = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (verificarUbiActiva(context)) {
                        dialog_estas_tienda = false
                        bottom_sheet_review_privado = true
                        esta_o_no_lugar=false
                        segun_user_tienda = true
                    } else {
                        dialogo_ubi_activa = true
                    }
                } else {
                    permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

            },
            rpa_no = {
                dialog_estas_tienda = false
                bottom_sheet_review_privado = true
                segun_user_tienda = false
                esta_o_no_lugar=true

            })
    }


    if (bottom_sheet_review_privado) {
        bottom_Sheet_seguro(esta_o_no_lugar,datos_principales_user,viewmodel, id_tienda_review, ondimis = {
            bottom_sheet_review_privado = !bottom_sheet_review_privado
        }, clik_envio = { ratingValue, texto, location ->

            if (segun_user_tienda) {
                rango_estrellas = ratingValue
                descripcion = texto

                Log.d("ReviewUbicacion", "Entró al addOnSuccessListener...")

                val (distancia, dentro) = if (location != null) {
                    estaDentroDeTienda(
                        location.latitude,
                        location.longitude,
                        latitude_tienda,
                        longitude_tienda
                    )
                } else {
                    0f to false
                }

                estado_presencial_tienda_lugar = dentro

                Log.d(
                    "ReviewUbicacion",
                    "Datos para review -> rango: $rango_estrellas, texto: $descripcion, tiendaId: ${id_tienda_review.id_tienda_lugar}, localidad: ${id_tienda_review.localida_lugar}"
                )

                viewmodel.agregar_review(
                    crearReview(
                        ratingValue = rango_estrellas,
                        texto = descripcion,
                        presencial = estado_presencial_tienda_lugar,
                        id_tienda_lugar = id_tienda_review.id_tienda_lugar,
                        localida_lugar = id_tienda_review.localida_lugar
                    )
                )
                Log.d("ReviewUbicacion", "✅ Review enviada correctamente")

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
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "¡Gracias por compartir tu experiencia con nosotros!",
                    duration = SnackbarDuration.Short
                )
            }
        }, crear_cuenta = {crear_cuenta()}, iniciar_seccion ={iniciar_seccion()})
    }


    if (bottom_sheet) {
        bottom_sheet_review(
            datos_principales_user = datos_principales_user,
            viewmodel = viewmodel,
            data_class_review = id_tienda_review,
            ondimis = {
                bottom_sheet = !bottom_sheet
            },
            clik_envio = { ratingValue, texto ->
                viewmodel.agregar_review(
                    crearReview(
                        ratingValue,
                        texto,
                        true,
                        id_tienda_review.id_tienda_lugar,
                        id_tienda_review.localida_lugar
                    )
                )
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "¡Gracias por compartir tu experiencia con nosotros!",
                        duration = SnackbarDuration.Short
                    )
                }
            }, crear_cuenta = crear_cuenta, iniciar_seccion = iniciar_seccion,{mostar_snackvar_reivew=true}
        )
    }


    if (dialogo_ubi_activa) {
        dialog_sin_ubi__rutas(
            "Para garantizar que tu reseña sea verificada, te solicitamos habilitar el acceso a tu ubicación. Esto permitirá que el sistema confirme automáticamente si te encuentras en el establecimiento y así validar tu reseña como auténtica.",
            onDismis = { dialogo_ubi_activa = false },
            abrir_configuracion = {
                dialogo_ubi_activa = false
                verificarGPS(context, launcher)

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
    Log.d("conteinod_escamedo", contenidoEscaneado)
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
            btn_aceptar_etc_dialog_general("Si") {
                rpa_si()
            }

        },
        dismissButton = {  btn_cerra_etc_dialog_general ("no"){
            rpa_no()
        } },
        title = {
            FuenteControladaApp {
                texto_generico_one_line("Verifica tu reseña")
            }

        },
        text = {
            FuenteControladaApp {
                Column {
                texto_generico_multilinea(
                    "Para dejar una reseña en este establecimiento, Geinz necesita verificar que te encuentras físicamente en el lugar. Si confirmas tu ubicación, tu reseña será verificada. De lo contrario, la reseña se registrará como no verificada.",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                texto_generico_one_line("Te encuentras en el lugar?")
                }
            }
        },
        icon = {
            Image(
                painter = painterResource(R.drawable.pin_3d_webp),
                contentDescription = "marker3d",
                modifier = Modifier.size(40.dp)
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
    hora = mostrarFechaDialog_horaDialog.obtenerHoraActual(),
    fecha = mostrarFechaDialog_horaDialog.obtenerFechaActual()
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
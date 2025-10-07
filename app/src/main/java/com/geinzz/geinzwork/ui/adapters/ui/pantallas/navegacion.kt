@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geinzz.geinzwork.Network_internet.ConnectivityViewModel
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos.pantalla_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda.ui_pantalla_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.cuenta_user
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos.iu_favoritos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Pantalla_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.IniciarSeccion
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.iniciar_seccion_normal
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.login_principal
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.HandleBackPress
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.PantallaExplorarTiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.bottom_navigation
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.pantalla_mapa_perzonalizado
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad.ui_salud_seguirdad
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.ui_servicio_tramite
import com.geinzz.geinzwork.ui.adapters.ui.principal.pantalla_principal
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.fondo_oscuro5_s
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.geinzz.geinzwork.viewModels.viewmodel_usuario_registrado
import com.google.firebase.auth.FirebaseAuth

private lateinit var firebaseAuth: FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun nativationWrapper(
    viewmodel: viewModel_localizate_geinz,
    connectivityViewModel: ConnectivityViewModel = viewModel()
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModelLugares: viewModel_lugares_turisticos = viewModel()
    val viewModelCordenadas: viewModel_principal_geinz_work = viewModel()
    val viewModel_login_user: viewModel_login_user = viewModel()
    val viewModel_filtrado_tiendas: viewModel_filtado_tiendas = viewModel()
    val viewmode_segurirdad_Salud: viewmode_seguridad_salud = viewModel()
    val mostrarCarga by viewModel_login_user.mostrarCarga.observeAsState(false)
    val systemUiController = rememberSystemUiController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val viewmodel_usuario_registrado: viewmodel_usuario_registrado = viewModel()
    val viewModel_localizate_geinz: viewModel_localizate_geinz = viewModel()
    val focusRequester = remember { FocusRequester() }
    var isvisble_buttomvar by rememberSaveable { mutableStateOf(true) }
    val datos_user by viewmodel_usuario_registrado.userData.observeAsState()
    var bottom_sheet_iniciar_seccion by remember { mutableStateOf(false) }

    var datos_principales_user by remember {
        mutableStateOf(datos_principales_user("Usuario", "", ""))
    }

    val localidad_shader_user by remember { mutableStateOf("") }

    LaunchedEffect(firebaseAuth.currentUser) {
        val current = firebaseAuth.currentUser
        if (current != null) {
            viewmodel_usuario_registrado.obtener_datos_user_registrado(current.uid)
        } else {
            datos_principales_user = datos_principales_user("Usuario", "", "")
        }
    }

    LaunchedEffect(datos_user) {
        datos_user?.let {
            datos_principales_user = datos_principales_user(it.nombre, it.img_perfil, it.localida)
        }
    }


    SideEffect {
        // Si la ruta actual es de las principales...
        val colorBarraInferior = if (
            currentRoute == "pantalla_principal" ||
            currentRoute == "buscar" ||
            currentRoute == "favoritos" ||
            currentRoute == "principal" ||
            currentRoute == "login_principal"
        ) {

            if (isvisble_buttomvar) {
                Color.Black   // visible → color normal
            } else {
                Color.Black       // oculta → negro
            }
        } else {
            // En otras pantallas → tu color oscuro por defecto
            fondo_oscuro5_s
        }

        // Barra de estado (arriba)
        systemUiController.setStatusBarColor(
            color = fondo_oscuro5_s,
            darkIcons = false
        )

        // Barra de navegación (abajo)
        systemUiController.setNavigationBarColor(
            color = colorBarraInferior,
            darkIcons = false
        )
    }


    val showBottomBar = when (currentRoute) {
        "pantalla_principal", "buscar", "favoritos", "principal", "login_principal" -> isvisble_buttomvar
        else -> false
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "buscar") {
            isvisble_buttomvar = false
        } else if (currentRoute != "buscar" && !isvisble_buttomvar) {
            isvisble_buttomvar = true
        }
    }
    val isConnected by connectivityViewModel.isConnected.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {

                    bottom_navigation(datos_principales_user, navController)

                }
            },
        ) { innerPadding ->
            HandleBackPress(navController)
            NavHost(
                navController = navController,
                startDestination = "pantalla_principal",
                modifier = Modifier.padding(innerPadding)
            ) {
                // Pantalla principal
                composable("pantalla_principal") {
                    pantalla_principal(
                        datos_principales_user,
                        categorias = { localidad, nombre ->
                            navController.navigate(
                                mostrar_tiendas(
                                    nombre,
                                    localidad
                                )
                            )
                        },
                        clikear_cartas = { categoria, nombre, localidad ->
                            if (categoria.equals("turismo")) {
                                Toast.makeText(
                                    context,
                                    "Geinz esta trabajando para darle mejor experiencia",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@pantalla_principal
                            }
                            navController.navigate(
                                screen_filtrado(
                                    categoria,
                                    nombre,
                                    localidad,
                                )
                            )

                        },
                        ver_lugares = { localidad ->
//                            Log.d("localidad_defautl_user", localidad)
                            navController.navigate(ui_agregar_lugares)

                        },
                        listner_busqueda = {
                            navController.navigate("buscar")
                        },
                        listener_seguridad = { localida ->
                            navController.navigate(ui_salud_seguridad(localida))
                        }, listner_sevicios_tramites = {localidad->
                            navController.navigate(ui_servicios_tramites(localidad))

                        },
                    )
                }
                // Login
                composable("login_principal") {
                    if (firebaseAuth.currentUser != null) {
                        cuenta_user(viewModel_login_user, navController)
                    } else {
                        IniciarSeccion(
                            viewModel_login_user, navController,
                            { tipo_cuenta ->
                                navController.navigate(crear_cuenta_geinz(tipo_cuenta))
                            },
                        )
                    }
                }

                composable("buscar") {
                    ui_pantalla_busqueda(
                        datos_principales_user,
                        focusRequester = focusRequester,
                        mostrar = {
                            isvisble_buttomvar = true

                        },
                        ocultar = {
                            isvisble_buttomvar = false
                        }, estado_mostar = isvisble_buttomvar, estado_ocultar = isvisble_buttomvar
                    )


                }

                composable("favoritos") {
                    iu_favoritos()
                }


                // Explorar tiendas
                composable<mostrar_tiendas> { navback ->
                    val datos_user = navback.toRoute<mostrar_tiendas>()
                    PantallaExplorarTiendas(
                        datos_user.localidad,
                        datos_user.nombre_user,
                        viewModel_localizate_geinz,
                        clik_img = { categoria, localidada, nombre_user ->
                            if (categoria.equals("turismo")) {
                                Toast.makeText(
                                    context,
                                    "Geinz esta trabajando para darle mejor experiencia",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@PantallaExplorarTiendas
                            }
                            Log.d("clikeamos_img", "$categoria")

                            navController.navigate(
                                screen_filtrado(
                                    categoria,
                                    localidada,
                                    nombre_user
                                )
                            )


                        }
                    )
                }

                composable<map_perzonalizado> { navback ->
                    val direcciones = navback.toRoute<map_perzonalizado>()
                    pantalla_mapa_perzonalizado(
                        viewmode_segurirdad_Salud = viewmode_segurirdad_Salud,
                        viewModel_filtrado_tiendas = viewModel_filtrado_tiendas,
                        viewmodel_lugares_turisticos = viewModelLugares,
                        tipo = direcciones.tipo,
                        localidad = direcciones.localidad
                    )
                }

                composable<screen_filtrado> { navback ->
                    val categoria_localidad = navback.toRoute<screen_filtrado>()
                    Pantalla_filtrado_tiendas(
                        viewModel_filtrado_tiendas,
                        categoria_localidad.categoria,
                        categoria_localidad.localidad,
                        categoria_localidad.nombre_user,
                        navigation_regresar = { navController.popBackStack() },
                        abrir_mapa = { tipo, localidad ->
                            navController.navigate(map_perzonalizado(tipo, localidad))
                        }, iniciar_normal = {
                            navController.navigate("login_principal")
                        }, con_google = {
                            navController.navigate("login_principal")
                        }, crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        }
                    )
                }

                composable<lugares_turisticos> { navback ->
                    val datos_lugares_turisticos = navback.toRoute<lugares_turisticos>()
                    pantalla_lugares_turisticos(
                        datos_lugares_turisticos.localidad,
                        viewModelLugares,
                        viewModelCordenadas
                    ) { tipo ->
                        navController.navigate(map_perzonalizado(tipo, ""))
                    }
                }

                composable<crear_cuenta_geinz> { navback ->
                    val tipo_crear_cuenta = navback.toRoute<crear_cuenta_geinz>()
                    login_principal(
                        viewModel_login_user,
                        tipo_crear_cuenta.tipo_completado,
                        navController
                    )
                }

                composable<ui_salud_seguridad> { navback ->
                    val salud_Seguridad = navback.toRoute<ui_salud_seguridad>()
                    ui_salud_seguirdad(
                        viewmode_segurirdad_Salud,
                        localida = salud_Seguridad.localidad,
                        abrir_mapa = { latitud, longitud ->
                            navController.navigate(map_perzonalizado("seguridad", ""))

                        })
                }

                composable <ui_agregar_lugares> {
                    datos_teindas()
                }

                composable <ui_servicios_tramites>{navback ->
                    val servicio=navback.toRoute<ui_servicios_tramites>()
                    ui_servicio_tramite(servicio.localidad)
                }

            }
        }
        AnimatedVisibility(
            visible = !isConnected, enter = slideInVertically { -it },
            exit = slideOutVertically { -it }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                texto_generico_one_line(
                    "Sin conexión a Internet",
                    color = Color.White,
                    modifier = Modifier.padding(5.dp)
                )
            }
        }


        AnimatedVisibility(
            visible = mostrarCarga,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                pantalla_carga_login()
            }
        }
        if (bottom_sheet_iniciar_seccion) {
            bottom_sheet_registrate(
                ondimis = { bottom_sheet_iniciar_seccion = false },
                iniciar_seccion_normal = {},
                continuar_con_google = { },
                crear_cuenta_geinz = { })
        }
    }
}
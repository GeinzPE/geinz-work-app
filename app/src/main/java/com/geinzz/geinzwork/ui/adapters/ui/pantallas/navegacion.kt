@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.geinzz.geinzwork.Network_internet.ConnectivityViewModel
import com.geinzz.geinzwork.data.model.FavoritosFactory
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.UiAction
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos.pantalla_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.busqueda.ui_pantalla_busqueda
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.cuenta_user
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.favoritos.iu_favoritos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Pantalla_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.mapa_inmobilia
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.pantalla_geinz_inmobiliaria
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.ui_info_imobiliara
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.IniciarSeccion
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.login_principal
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nuevos_negocios.nuevos_negocios
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.HandleBackPress
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.PantallaExplorarTiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.bottom_navigation
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.ui_promos_cerca_de_ti
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad.ui_salud_seguirdad
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.ui_servicio_tramite
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.login_socios
import com.geinzz.geinzwork.ui.adapters.ui.principal.pantalla_principal
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.fondo_oscuro5_s
import com.geinzz.geinzwork.viewModels.DeepLinkViewModel
import com.geinzz.geinzwork.viewModels.UiActionViewModel
import com.geinzz.geinzwork.viewModels.viewModel_favoritos
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import com.geinzz.geinzwork.viewModels.viewmodel_inmobiliaria
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_inmobiliara
import com.geinzz.geinzwork.viewModels.viewmodel_mapa_personalizado
import com.geinzz.geinzwork.viewModels.viewmodel_usuario_registrado
import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder

private lateinit var firebaseAuth: FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun nativationWrapper(
    uiActionVM: UiActionViewModel,
    navegacion: NavHostController,
    deepLinkVM: DeepLinkViewModel,
    connectivityViewModel: ConnectivityViewModel = viewModel()

) {
    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val navController = navegacion
    val viewModelLugares: viewModel_lugares_turisticos = viewModel()
    val viewModelCordenadas: viewModel_principal_geinz_work = viewModel()
    val viewModel_login_user: viewModel_login_user = viewModel()
    val viewModel_filtrado_tiendas: viewModel_filtado_tiendas = viewModel()
    val viewmode_segurirdad_Salud: viewmode_seguridad_salud = viewModel()
    val viewmodelMapa: viewmodel_mapa_personalizado = viewModel()
    val viewmodel_mapa_inmobilia: viewmodel_mapa_inmobiliara = viewModel()
    val viewmodel_inmobiliaria: viewmodel_inmobiliaria = viewModel()

    var id_promo_params by remember { mutableStateOf("") }
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
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")
    val email_respald_user by data_store_localidad.get_email_user(context)
        .collectAsState(initial = "")
    var id_respado_user by remember { mutableStateOf("") }

    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""

    var email_respaldo_user by remember { mutableStateOf("") }
    var datos_principales_user by remember {
        mutableStateOf(
            datos_principales_user(
                "",
                "",
                "barranca"
            )
        )
    }
    LaunchedEffect(uiActionVM) {
        Log.d("UiAction", "🚀 LaunchedEffect iniciado, escuchando acciones")

        uiActionVM.actions.collect { action ->

            Log.d("UiAction", "📩 Acción recibida: $action")

            when (action) {


                is UiAction.AbrirPerfil -> TODO()
                is UiAction.Abrir_pantalla_promos_cecanas -> {
                    id_promo_params = action.id_promocion
                    navController.navigate(
                        promociones_y_ofertas(
                            action.localida_tienda,
                            id_promo_params
                        )
                    )
                }

                is UiAction.abrir_pantalla_inmobiliara -> {
                    navController.navigate(
                        datos_completros_inmobiliaria(
                            action.id_propiedad,
                            action.localdiad_pripiedad,
                            datos_user?.nombre ?: "usuario"
                        )
                    )
                }

                is UiAction.ReviewPrivada -> TODO()
                is UiAction.ReviewPublica -> TODO()
                is UiAction.Ruta -> TODO()
                else -> {}
            }
        }
    }

    LaunchedEffect(firebaseAuth.currentUser, uid_respald_user) {
        val current = firebaseAuth.currentUser
        if (current != null) {
            Log.d("id_firebase", "firebase ${firebaseAuth.uid.toString()}")
            viewmodel_usuario_registrado.obtener_datos_user_registrado(current.uid)
        } else if (uid_respald_user.isNotEmpty()) {
            Log.d("id_firebase", "estatico ${uid_respald_user}")
            viewmodel_usuario_registrado.obtener_datos_user_registrado(uid_respald_user)
        } else {
            Log.d("id_firebase", "vacio ${uid_respald_user}")
            datos_principales_user = datos_principales_user("", "", "barranca")
        }
    }

    val viewmodelFavoritos: viewModel_favoritos = viewModel(
        key = "favoritos_${id_user ?: ""}",
        factory = FavoritosFactory(id_user)
    )

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
            color = Color.Black,
            darkIcons = false
        )
    }


    val showBottomBar = when (currentRoute) {
        "pantalla_principal", "buscar", "favoritos", "principal", "login_principal" -> isvisble_buttomvar
        else -> false
    }

//    fun enviar_notificacion_lista_dispo(id_user: String, titulo: String, txt: String) {
//        val notificacion = NotificacionRS()
//        FirebaseFirestore.getInstance()
//            .collection("Trabajadores_Usuarios_Drivers")
//            .document("users")
//            .collection("tokens")
//            .document(id_user)
//            .get()
//            .addOnSuccessListener { res ->
//
//                if (!res.exists()) {
//                    Log.d("TOKENS", "❌ No existe documento para este usuario")
//                    return@addOnSuccessListener
//                }
//
//                val mapaTokens = (res.data?.get("tokens") as? Map<String, String>) ?: emptyMap()
//                val tokensInvalidos = mutableListOf<String>()
//
//                mapaTokens.forEach { (dispositivo, token) ->
//                    Log.d("TOKENS", "📨 Enviando a $dispositivo → $token")
//                    val link =
//                        "https://geinzworkapp.web.app/share?" +
//                                "t=ti" +
//                                "&id=1KEciyNnTwkrELdFU7F4" +
//                                "&l=barranca" +
//                                "&c=${URLEncoder.encode("salud y farmacias", "UTF-8")}"
////                    notificacion.enviarNotificacionFCM_LINK(
////                        id_user= id_user,
////                        token = token,
////                        titulo = titulo,
////                        cuerpo = txt,
////                        link = link,
////                        tipoNotificacion="premium",
////                        urlLogo = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fmifarma.webp?alt=media&token=e5276f0d-0de3-49a6-ac1a-afdee7a7a529",
////                        urlImagen = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/walpaper_geinz%2Fturisticos%2Fimg11.webp?alt=media&token=1151dd65-8a6b-497d-a452-a8d948859422"
////                    ) {token, fallo ->
////                        Log.d("fallo_dado_enteroa", "$fallo")
////                        if (fallo) {
////                            tokensInvalidos.add(dispositivo)
////                            if (tokensInvalidos.isNotEmpty()) {
////                                notificacion.eliminar_tokens_usuario(id_user, tokensInvalidos)
////                            }
////                        }
////                    }
//                }
//
//                // Después de enviar a todos, eliminamos los tokens inválidos
//
//
//            }
//            .addOnFailureListener { e ->
//                Log.e("TOKENS", "🔥 Error al obtener tokens", e)
//            }
//    }


    LaunchedEffect(currentRoute) {
        if (currentRoute == "buscar") {
            isvisble_buttomvar = false
        } else if (currentRoute != "buscar" && !isvisble_buttomvar) {
            isvisble_buttomvar = true
        }
    }
    var correo_registrado by remember { mutableStateOf("") }
    var mostrar_btn_termianr_configurar by remember { mutableStateOf(false) }
    val user = FirebaseAuth.getInstance().currentUser


    LaunchedEffect(email_respald_user) {
        if (email_respald_user.isNotEmpty()) {
            email_respaldo_user = email_respald_user
            Log.d(
                "UID_DataStore",
                "✅ Recuperado email válido desde DataStore: $email_respaldo_user"
            )
        } else {
            email_respaldo_user = ""
            Log.d(
                "UID_DataStore",
                "vacio"
            )
        }
    }
    LaunchedEffect(uid_respald_user) {
        if (uid_respald_user.isNotEmpty()) {
            id_respado_user = uid_respald_user
            Log.d("UID_DataStore", "✅ Recuperado UID válido desde DataStore: $id_respado_user")
        } else {
            id_respado_user = ""
            Log.d(
                "UID_DataStore",
                "vacio"
            )
        }
    }
    LaunchedEffect(user, mostrar_btn_termianr_configurar, uid_respald_user) {
        if (user != null) {
            val email = user.email
            val uid = user.uid
            correo_registrado = email ?: ""
            mostrar_btn_termianr_configurar =
                viewModel_login_user.verificar_cuenta_google_provider(email ?: "")
            Log.d(
                "correo_registrado",
                "Correo actual: $email — UID: $uid falta_confurar =$mostrar_btn_termianr_configurar"
            )
            viewModel_login_user.setear_mostrar_btn_configurar(mostrar_btn_termianr_configurar)

            data_store_localidad.guardar_datos_user(context, uid, email ?: "")

        } else if (id_respado_user.isNotEmpty() && !email_respaldo_user.isNullOrEmpty()) {
            val email = email_respaldo_user
            mostrar_btn_termianr_configurar =
                viewModel_login_user.verificar_cuenta_google_provider(email ?: "")
            viewModel_login_user.setear_mostrar_btn_configurar(mostrar_btn_termianr_configurar)
            Log.d("correo_registrado", "de data store quedo el $id_respado_user")
        } else {
            Log.d("correo_registrado", "No hay usuario logueado ni respaldo local")

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
                    bottom_navigation(
                        uiActionVM,
                        isConnected,
                        datos_principales_user = datos_principales_user,
                        navController = navController,
                        crear_cuenta = { navController.navigate(crear_cuenta_geinz("crear")) },
                        iniciar_seccion = { navController.navigate("login_principal") })

                }
            },
        ) { innerPadding ->
            HandleBackPress(navController)
            NavHost(
                navController = navController,
                startDestination = "pantalla_principal",
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(900))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(900))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(900))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(900))
                }
            ) {
                // Pantalla principal
                composable("pantalla_principal") {
                    pantalla_principal(
                        deepLinkVM = deepLinkVM,
                        isConnected = isConnected,
                        datos_principales_user = datos_principales_user,
                        categorias = { localidad, nombre ->
                            navController.navigate(
                                mostrar_tiendas(
                                    nombre,
                                    localidad
                                )
                            )
                        },
                        clikear_cartas = { categoria, localidad, nombre_user ->
                            Log.d("categoriass", "$categoria $nombre_user $localidad")
                            if (categoria.equals("turismo")) {
                                navController.navigate(lugares_turisticos(localidad))
                            } else {
                                navController.navigate(
                                    screen_filtrado(
                                        categoria,
                                        localidad,
                                        nombre_user,
                                    )
                                )
                            }

                        },
                        ver_lugares = { localidad ->
                            navController.navigate(lugares_turisticos(localidad))
                        },
                        listner_busqueda = {
                            navController.navigate("buscar")
                        },
                        listener_seguridad = { localida ->
                            navController.navigate(ui_salud_seguridad(localida))
                        },
                        listner_sevicios_tramites = { localidad, id ->
                            navController.navigate(ui_servicios_tramites(localidad))
//                            navController.navigate(promociones_y_ofertas(localidad, id))

                        },
                        abrir_guardar_datos = {
//                                                    enviar_notificacion_lista_dispo(
//                                                        id_user,
//                                                        "Mira ese nuevo negocio en geinz notificacion de prueva ",
//                                                        "Encuentralo a unos pasos cerca de ti "
//                                                    )
//                            navController.navigate(ui_agregar_lugares)
                            navController.navigate(agregar_pripiedads)

//                            navController.navigate(map_box)
                            //                            pasar_teindas_nuevas()

                        },
                        mostrar_panel_geinz = { navController.navigate(login_scios) },
                        mostar_nuevos_lugares_geinz = { localidad ->
                            navController.navigate(nuevos_negocios_geinz(localidad))
                        },
                        iniciar_seccion = { navController.navigate("login_principal") },
                        crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        },
                        abir_butom_Var = { isvisble_buttomvar = true },
                        cerrar_buttom_var = { isvisble_buttomvar = false },
                        {
                            navController.navigate("pantalla_principal") {
                                popUpTo("pantalla_principal") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }, geinz_inmobiliaria = { localidad ->
                            navController.navigate(geinz_inmobiliaria(localidad_selec = localidad))
                        }
                    )
                }
                // Login
                composable("login_principal") {
                    if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {
                        cuenta_user(
                            isConnected = isConnected,
                            viewModel_login_user = viewModel_login_user,
                            correo_registrado = correo_registrado,
                            navController = navController,
                            terminar_configurar = { correo_google ->
                                navController.navigate(crear_cuenta_geinz(correo_google))
                            }, click_login_ver_socio = {
                                navController.navigate(login_scios)
                            })
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
                        isConnected,
                        viewmodelMapa,
                        viewModelLugares,
                        localida_defauld = datos_principales_user,
                        focusRequester = focusRequester,
                        ocultar = {
                            isvisble_buttomvar = false
                        }, estado_mostar = isvisble_buttomvar, iniciar_seccion_normal = {
                            navController.navigate("login_principal")
                        }, crear_cuenta_geinz = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        }, abrir_mapa = { tipo ->
                            navController.navigate(
                                map_perzonalizado(
                                    tipo,
                                    "barranca",
                                    null,
                                    null,
                                    null
                                )
                            )
                        }, crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        }, iniciar_seccion = {
                            navController.navigate("login_principal")
                        })


                }

                composable("favoritos") {
                    iu_favoritos(
                        isConnected,
                        viewModelFiltros = viewModel_filtrado_tiendas,
                        viewmodelFavoritos = viewmodelFavoritos,
                        datos_principales_user = datos_principales_user,
                        empty_select_chip = { nombre, categoria, localidad ->
                            Log.d("adsd13413rdwF", "$nombre $categoria $localidad")
                            navController.navigate(
                                screen_filtrado(
                                    categoria,
                                    localidad,
                                    nombre,
                                )
                            )
                        },
                        mostar_butom_var = {
                            isvisble_buttomvar = true
                        },
                        ocultar_buttom_var = {
                            isvisble_buttomvar = false
                        })
                }


                // Explorar tiendas
                composable<mostrar_tiendas> { navback ->
                    val datos_user = navback.toRoute<mostrar_tiendas>()
                    PantallaExplorarTiendas(
                        localidadUser = datos_user.localidad,
                        nombreUser = datos_user.nombre_user,
                        viewModel = viewModel_localizate_geinz,
                        clik_img = { categoria, localidada, nombre_user ->
                            if (categoria.equals("turismo")) {
                                navController.navigate(lugares_turisticos(localidada))
                                return@PantallaExplorarTiendas
                            } else {
                                navController.navigate(
                                    screen_filtrado(
                                        categoria,
                                        localidada,
                                        nombre_user
                                    )
                                )
                            }


                        }
                    )
                }
                composable<nuevos_negocios_geinz> { navback ->
                    val datos = navback.toRoute<nuevos_negocios_geinz>()
                    nuevos_negocios(
                        verificar_inter = isConnected,
                        localida_select = datos.localidad,
                        crear_cuenta = { navController.navigate(crear_cuenta_geinz("crear")) },
                        iniciar_normal = { navController.navigate("login_principal") })
                }
                composable<lugares_turisticos> { navback ->
                    val datos_lugares_turisticos = navback.toRoute<lugares_turisticos>()
                    pantalla_lugares_turisticos(
                        "",
                        isConnected,
                        viewmodelMapa = viewmodelMapa,
                        localidad_selecionada = datos_lugares_turisticos.localidad,
                        viewmodel_lugares_turisticos = viewModelLugares,
                        abrir_mapa = { tipo, nombre_lugar, lat, lng ->
                            navController.navigate(
                                map_perzonalizado(
                                    tipo,
                                    "barranca",
                                    nombre_lugar,
                                    lat,
                                    lng
                                )
                            )
                        }, crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        }, navigation_regresar = {
                            navController.popBackStack()
                        }, iniciar_seccion = {
                            navController.navigate("login_principal")
                        })
                }

                composable<promociones_y_ofertas> { navback ->
                    val datos = navback.toRoute<promociones_y_ofertas>()
                    ui_promos_cerca_de_ti(
                        "clik_directo",
                        activar_promo_params = datos.id_promo,
                        localidad = datos.localidad,
                        verificar_intener = isConnected,
                        iniciar_seccion = {
                            bottom_sheet_iniciar_seccion = true
                        },
                        crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        },
                        onBack = {
                            navController.navigate("pantalla_principal") {
                                popUpTo("pantalla_principal") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable<map_perzonalizado> { navback ->
                    val direcciones = navback.toRoute<map_perzonalizado>()
//                    pantalla_mapa_perzonalizado(
//                        id_respado_user,
//                        verificar_intener = isConnected,
//                        viewmodelMapa = viewmodelMapa,
//                        viewmode_segurirdad_Salud = viewmode_segurirdad_Salud,
//                        viewModel_filtrado_tiendas = viewModel_filtrado_tiendas,
//                        viewmodel_lugares_turisticos = viewModelLugares,
//                        tipo = direcciones.tipo,
//                        localidad = direcciones.localidad
//                    )
                    SimpleMapDark(
                        direcciones.nombre, direcciones.latitud, direcciones.lng,
                        viewmodelMapa,
                        direcciones.localidad,
                        id_respado_user,
                        direcciones.tipo,
                        isConnected,
                        viewModelLugares,
                        viewModel_filtrado_tiendas,
                        viewmode_segurirdad_Salud
                    )

                }

                composable<screen_filtrado> { navBackStackEntry ->
                    val categoria_localidad = navBackStackEntry.toRoute<screen_filtrado>()
                    Pantalla_filtrado_tiendas(
                        id_tienda = "",
                        verificar_intener = isConnected,
                        viewmodelFavoritos = viewmodelFavoritos,
                        viewModelFiltros = viewModel_filtrado_tiendas,
                        categoria = categoria_localidad.categoria,
                        localida = categoria_localidad.localidad,
                        nombre_user = categoria_localidad.nombre_user,
                        navigation_regresar = {
                            viewModel_filtrado_tiendas.limpiarFiltros()
                            navController.popBackStack()
                        },
                        abrir_mapa = { tipo, localidad ->
                            if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {
                                navController.navigate(
                                    map_perzonalizado(
                                        tipo,
                                        localidad,
                                        null,
                                        null,
                                        null
                                    )
                                )
                            } else {
                                bottom_sheet_iniciar_seccion = true
                            }
                        },
                        iniciar_normal = {
                            navController.navigate("login_principal")
                        },
                        con_google = {
                            navController.navigate("login_principal")
                        },
                        crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        }, navController = navController
                    )
                }

                composable<login_scios> {
                    login_socios(isConnected, "", navController)
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
                        isConnected,
                        datos_user?.nombre ?: "", id_respado_user,
                        viewmode_segurirdad_Salud,
                        localida = salud_Seguridad.localidad,
                        abrir_mapa = { latitud, longitud ->
                            navController.navigate(
                                map_perzonalizado(
                                    "seguridad",
                                    "",
                                    null,
                                    null,
                                    null
                                )
                            )

                        })
                }

                composable<ui_agregar_lugares> {
                    datos_teindas()
                }


                composable<agregar_pripiedads> {
                    agregar_propiedades()
                }
                composable<map_box> {
//                    SimpleMapDark(
//                        "barranca",
//                        id_respado_user,
//                        "",
//                        isConnected,
//                        viewModelLugares,
//                        viewModel_filtrado_tiendas,
//                        viewmode_segurirdad_Salud
//                    )
                }
                composable<ui_servicios_tramites> { navback ->
                    val servicio = navback.toRoute<ui_servicios_tramites>()
                    ui_servicio_tramite(isConnected, servicio.localidad, id_respado_user)
                }

                composable(
                    route = "mostrar_tiendas/{localidad}/{idLugar}/{categoria}",
                ) { backStackEntry ->

                    val localidad = Uri.decode(
                        backStackEntry.arguments?.getString("localidad") ?: ""
                    )

                    val idLugar = backStackEntry.arguments?.getString("idLugar") ?: ""


                    val categoria = URLDecoder.decode(
                        backStackEntry.arguments?.getString("categoria") ?: "",
                        "UTF-8"
                    )
                    Pantalla_filtrado_tiendas(
                        id_tienda = idLugar,
                        verificar_intener = isConnected,
                        viewmodelFavoritos = viewmodelFavoritos,
                        viewModelFiltros = viewModel_filtrado_tiendas,
                        categoria = categoria,           // 👈 YA NORMAL
                        localida = localidad,             // 👈 YA NORMAL
                        nombre_user = "",
                        navigation_regresar = {
                            viewModel_filtrado_tiendas.limpiarFiltros()
                            navController.popBackStack()
                        },
                        abrir_mapa = { tipo, loc ->
                            if (firebaseAuth.currentUser != null || id_respado_user.isNotEmpty()) {
                                navController.navigate(
                                    map_perzonalizado(
                                        tipo,
                                        loc,
                                        null,
                                        null,
                                        null
                                    )
                                )
                            } else {
                                bottom_sheet_iniciar_seccion = true
                            }
                        },
                        iniciar_normal = {
                            navController.navigate("login_principal")
                        },
                        con_google = {
                            navController.navigate("login_principal")
                        },
                        crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        },
                        navController = navController
                    )
                }

                composable("lugares_turisticos") {
                    pantalla_lugares_turisticos(
                        "",
                        isConnected,
                        viewmodelMapa,
                        "barranca",
                        viewModelLugares,
                        abrir_mapa = { tipo, nombre, lat, lng ->
                            navController.navigate(
                                map_perzonalizado(
                                    tipo,
                                    "barranca",
                                    nombre,
                                    lat,
                                    lng
                                )
                            )
                        },
                        crear_cuenta = { navController.navigate(crear_cuenta_geinz("crear")) },
                        navigation_regresar = { navController.popBackStack() },
                        iniciar_seccion = { navController.navigate("login_principal") }
                    )
                }

                composable("salud_y_seguridad") {
                    ui_salud_seguirdad(
                        isConnected,
                        datos_user?.nombre ?: "",
                        id_respado_user,
                        viewmode_segurirdad_Salud,
                        localida = "barranca",
                        abrir_mapa = { latitud, longitud ->
                            navController.navigate(
                                map_perzonalizado(
                                    "seguridad",
                                    "",
                                    null,
                                    null,
                                    null
                                )
                            )

                        })
                }


                composable("nuevos_negocios") {
                    nuevos_negocios(
                        verificar_inter = isConnected,
                        localida_select = "barranca",
                        crear_cuenta = { navController.navigate(crear_cuenta_geinz("crear")) },
                        iniciar_normal = { navController.navigate("login_principal") })
                }
                composable("servicios_y_tramites") {
                    ui_servicio_tramite(isConnected, "barranca", id_respado_user)
                }

                composable("promocionar_ads") {
                    login_socios(isConnected, "envio", navController)
                }
                composable("promocionar_rec") {
                    login_socios(isConnected, "recargas", navController)
                }

                composable("promocionar_rec") {
                    login_socios(isConnected, "envio", navController)
                }
                composable("promociones_nuevas") {
                    ui_promos_cerca_de_ti(
                        "promociones_nuevas",
                        id_promo_params,
                        localidad = "barranca",
                        verificar_intener = isConnected,
                        iniciar_seccion = {
                            bottom_sheet_iniciar_seccion = true
                        },
                        crear_cuenta = {
                            navController.navigate(crear_cuenta_geinz("crear"))
                        }, {
                            navController.navigate("pantalla_principal") {
                                popUpTo("pantalla_principal") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }

                        })
                }

                composable<geinz_inmobiliaria> { navback ->

                    val servicio = navback.toRoute<geinz_inmobiliaria>()

                    pantalla_geinz_inmobiliaria(
                        viewmodel_mapa_inmobilia,
                        viewmodel = viewmodel_inmobiliaria,
                        nombre_user = datos_user?.nombre ?: "",
                        coneccion = isConnected,
                        localidad_user = servicio.localidad_selec,
                        ver_detalles_completos = { id, localidad, nombre ->
                            navController.navigate(
                                datos_completros_inmobiliaria(
                                    id,
                                    localidad,
                                    nombre
                                )
                            )


                        },
                        ver_lugares_mapa = {
                            navController.navigate(abrir_mapa_inmobiliara)
                        }
                    )
                }


                composable<datos_completros_inmobiliaria> { navback ->
                    val datos = navback.toRoute<datos_completros_inmobiliaria>()
                    ui_info_imobiliara(
                        viewmodel_mapa_inmobilia,
                        viewmodelMapa = viewmodelMapa,
                        viewmodel_lugares_turisticos = viewModelLugares,
                        verificar_inter = isConnected,
                        viewModel = viewmodel_inmobiliaria,
                        id = datos.id,
                        localidad = datos.localidad,
                        nombre_user = datos.nombre_user,
                        iniciar_seccion = { navController.navigate(crear_cuenta_geinz("crear")) },
                        crear_cuenta = { navController.navigate("login_principal") },
                        abrir_mapa = { tipo, img, lat, lng ->
                            navController.navigate(
                                map_perzonalizado(
                                    tipo = tipo,
                                    localidad = "barranca",
                                    nombre = img,
                                    latitud = lat,
                                    lng = lng
                                )
                            )

                        },
                        {
                            navController.navigate(abrir_mapa_inmobiliara)
                        }
                    )
                }

                composable<abrir_mapa_inmobiliara> {
                    mapa_inmobilia(
                        id_respado_user,
                        viewModelLugares,
                        viewmodelMapa,
                        viewModel_filtrado_tiendas,
                        isConnected,
                        viewmodel_mapa_inmobilia,
                        iniciar_seccion = {},
                        crear_cuenta = {}
                    )
                }


                composable(
                    route = "lugares_turisticos/{localidad}/{idLugar}",
                ) { backStackEntry ->
                    val localidad = backStackEntry.arguments?.getString("localidad") ?: ""
                    val idLugar = backStackEntry.arguments?.getString("idLugar") ?: ""

                    pantalla_lugares_turisticos(
                        idLugar,
                        isConnected,
                        viewmodelMapa = viewmodelMapa,
                        localidad_selecionada = localidad,
                        viewmodel_lugares_turisticos = viewModelLugares,
                        abrir_mapa = { tipo, nombre, lat, lng ->
                            navController.navigate(
                                map_perzonalizado(
                                    tipo,
                                    "barranca",
                                    nombre,
                                    lat,
                                    lng
                                )
                            )
                        },
                        crear_cuenta = { navController.navigate(crear_cuenta_geinz("crear")) },
                        navigation_regresar = { navController.popBackStack() },
                        iniciar_seccion = { navController.navigate("login_principal") }
                    )
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
            Log.d("mostramos_carga", "$mostrarCarga")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                pantalla_carga_login(true)
            }
        }
        if (bottom_sheet_iniciar_seccion) {
            bottom_sheet_registrate(
                ondimis = { bottom_sheet_iniciar_seccion = false },
                iniciar_seccion_normal = {
                    navController.navigate("login_principal")
                    bottom_sheet_iniciar_seccion = false
                    viewModel_filtrado_tiendas.limpiarFiltros()
                },
                crear_cuenta_geinz = {
                    bottom_sheet_iniciar_seccion = false
                    navController.navigate(crear_cuenta_geinz("crear"))
                    viewModel_filtrado_tiendas.limpiarFiltros()
                }, "Desbloquea el mapa completo y explora lo que te rodea"

            )
        }
    }


}
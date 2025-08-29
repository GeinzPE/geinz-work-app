@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.loadings.pantalla_carga_login
import com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos.pantalla_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user.cuenta_user
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Pantalla_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.IniciarSeccion
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.login_principal
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.HandleBackPress
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.PantallaExplorarTiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.bottom_navigation
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.pantalla_mapa_perzonalizado
import com.geinzz.geinzwork.ui.adapters.ui.principal.pantalla_principal
import com.geinzz.geinzwork.viewModels.LoginState
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work
import com.google.firebase.auth.FirebaseAuth

private lateinit var firebaseAuth: FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun nativationWrapper(
    viewmodel: viewModel_localizate_geinz
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val navController = rememberNavController()
    val viewModelLugares: viewModel_lugares_turisticos = viewModel()
    val viewModelCordenadas: viewModel_principal_geinz_work = viewModel()
    val viewModel_login_user: viewModel_login_user =viewModel()
    val loginState by viewModel_login_user.loginState.observeAsState(LoginState.Idle)


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = when (currentRoute) {
        "pantalla_principal", "principal", "login_principal" -> true
        else -> false
    }

    Scaffold(
        bottomBar = { if (showBottomBar) bottom_navigation(navController) },
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
                    categorias = { localidad,nombre->navController.navigate(mostrar_tiendas(nombre,localidad)) },
                    ver_lugares = { navController.navigate(lugares_turisticos) },
                    navController = navController
                )
            }
            // Login
            composable("login_principal") {
                if (firebaseAuth.currentUser != null) {
                    cuenta_user(navController)
                } else {
                    IniciarSeccion(navController,{ tipo_cuenta ->
                        navController.navigate(crear_cuenta_geinz(tipo_cuenta))
                    })
                }
            }



            // Explorar tiendas
            composable<mostrar_tiendas> { navback->
                val datos_user=navback.toRoute<mostrar_tiendas>()
                PantallaExplorarTiendas(
                    datos_user.localidad,
                    datos_user.nombre_user,
                    viewmodel,
                    clik_img = { categoria, localidada, nombre_user ->
                        navController.navigate(screen_filtrado(categoria, localidada, nombre_user))
                    }
                )
            }

            composable<map_perzonalizado> { navback ->
                val direcciones = navback.toRoute<map_perzonalizado>()
                pantalla_mapa_perzonalizado(direcciones.tipo, viewModelCordenadas, viewModelLugares)
            }

            composable<screen_filtrado> { navback ->
                val categoria_localidad = navback.toRoute<screen_filtrado>()
                Pantalla_filtrado_tiendas(
                    categoria_localidad.categoria,
                    categoria_localidad.localidad,
                    categoria_localidad.nombre_user,
                    navigation_regresar = { navController.popBackStack() }
                )
            }

            composable<lugares_turisticos> { navback ->
                pantalla_lugares_turisticos(
                    "barranca",
                    viewModelLugares,
                    viewModelCordenadas
                ) { tipo ->
                    navController.navigate(map_perzonalizado(tipo))
                }
            }

            composable<crear_cuenta_geinz> { navback ->
                val tipo_crear_cuenta = navback.toRoute<crear_cuenta_geinz>()
                login_principal(tipo_crear_cuenta.tipo_completado,navController)
            }

            composable<carga_login> {
                pantalla_carga_login()
            }

        }


        if (loginState is LoginState.Loading) {
            Log.d("LoginState", "Mostrando carga")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                pantalla_carga_login()
            }
        }
    }
}








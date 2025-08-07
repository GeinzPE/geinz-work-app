@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Pantalla_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.PantallaExplorarTiendas
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz


@Composable
fun nativationWrapper(
    localidad_user: String,
    nombre_user: String,
    viewmodel: viewModel_localizate_geinz
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = principal) {
        composable<principal> {
            PantallaExplorarTiendas(
                localidad_user,
                nombre_user,
                viewmodel,
                clik_img = { categoria, localidada,nombre_user ->
                    navController.navigate(screen_filtrado(categoria, localidada,nombre_user)) {}
                })
        }



        composable<screen_filtrado> { navback ->
                val categoria_localidad = navback.toRoute<screen_filtrado>()
                Pantalla_filtrado_tiendas(
                    categoria_localidad.categoria,
                    categoria_localidad.localidad,
                    categoria_localidad.nombre_user,
                    navigation_regresar = { navController.popBackStack() })
        }

    }
}






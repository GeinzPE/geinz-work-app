@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geinzz.geinzwork.ui.adapters.ui.lugares_turisticos.pantalla_lugares_turisticos
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Pantalla_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.PantallaExplorarTiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.pantalla_mapa_perzonalizado
import com.geinzz.geinzwork.ui.adapters.ui.principal.pantalla_principal
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_lugares_turisticos
import com.geinzz.geinzwork.viewModels.viewModel_principal_geinz_work


@Composable
fun nativationWrapper(
    localidad_user: String,
    nombre_user: String,
    viewmodel: viewModel_localizate_geinz
) {
    val navController = rememberNavController()
    val viewModelLugares: viewModel_lugares_turisticos = viewModel()
    val viewModelCordenadas: viewModel_principal_geinz_work = viewModel()

    NavHost(navController = navController, startDestination = pantalla_principal) {
        composable<pantalla_principal> {
            pantalla_principal(
                { navController.navigate(principal) },
                { navController.navigate(lugares_turisticos) })
        }
        composable<principal> {
            PantallaExplorarTiendas(
                localidad_user,
                nombre_user,
                viewmodel,
                clik_img = { categoria, localidada, nombre_user ->
                    navController.navigate(screen_filtrado(categoria, localidada, nombre_user)) {}
                })
        }
        composable<map_perzonalizado> { navback ->
            val direcciones = navback.toRoute<map_perzonalizado>()
            pantalla_mapa_perzonalizado(
                direcciones.tipo,viewModelCordenadas,viewModelLugares
            )
        }
        composable<screen_filtrado> { navback ->
            val categoria_localidad = navback.toRoute<screen_filtrado>()
            Pantalla_filtrado_tiendas(
                categoria_localidad.categoria,
                categoria_localidad.localidad,
                categoria_localidad.nombre_user,
                navigation_regresar = { navController.popBackStack() })
        }
        composable<lugares_turisticos> { navback ->
            pantalla_lugares_turisticos("barranca", viewModelLugares, viewModelCordenadas) { tipo ->
                navController.navigate(map_perzonalizado(tipo)) {}
            }
        }

    }
}






@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.telecom.Call
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.Pantalla_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.chips_categorias
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.PantallaExplorarTiendas
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.navigation.NavHostController


const val FAB_EXPLODE_BOUNDS_KEY = "FAB_EXPLODE_BOUNDS_KEY"

@Composable
fun nativationWrapper(
    localidad_user: String,
    nombre_user: String,
    viewmodel: viewModel_localizate_geinz
) {
    val navController = rememberNavController()
    val fabColor = Color.Black.copy(alpha = 0.4f)

    NavHost(navController = navController, startDestination = principal) {
        composable<principal> {
            PantallaExplorarTiendas(
                localidad_user,
                nombre_user,
                viewmodel,
                clik_img = { categoria, localidada ->
                    navController.navigate(screen_filtrado(categoria, localidada)) {}
                })
        }



        composable<screen_filtrado> { navback ->
                val categoria_localidad = navback.toRoute<screen_filtrado>()
                Pantalla_filtrado_tiendas(
                    categoria_localidad.categoria,
                    categoria_localidad.localidad,
                    navigation_regresar = { navController.popBackStack() })


        }
//        composable<floating_action_button> {
//            SharedTransitionLayout {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(fabColor)
//                        .sharedBounds(
//                            sharedContentState = rememberSharedContentState(
//                                key = FAB_EXPLODE_BOUNDS_KEY
//                            ),
//                            animatedVisibilityScope = this@composable
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("Add item")
//                }
//            }
//
//        }

    }
}

//@Composable
//fun SharedTransitionNavContent(
//    context: AnimatedVisibilityScope,
//    categoria: String,
//    localidad: String,
//    fabColor: Color,
//    navController: NavHostController
//) {
//    SharedTransitionLayout {
//        // screen_filtrado
//        Pantalla_filtrado_tiendas(
//            categoria,
//            localidad, fabColor, context, onFabClick = {
//                navController.navigate(floating_action_button)
//            },
//            navigation_regresar = { navController.popBackStack() })
//
//    }
//}
//
//@Composable
//fun FloatingButtonScreen(
//    context: AnimatedVisibilityScope,
//    fabColor: Color
//) {
//    SharedTransitionLayout {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(fabColor)
//                .sharedBounds(
//                    sharedContentState = rememberSharedContentState(FAB_EXPLODE_BOUNDS_KEY),
//                    animatedVisibilityScope = context
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Add item")
//        }
//    }
//}





package com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.LoginState_inicio
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.firebase.auth.FirebaseAuth

val firebaseAuth = FirebaseAuth.getInstance()

@Composable
fun cuenta_user(viewModel_login_user: viewModel_login_user, navController: NavController) {
    val loginState_principal by viewModel_login_user.loginStateCamposInicial.observeAsState()

    LaunchedEffect(loginState_principal) {
        when (loginState_principal) {
            is LoginState_inicio.LoggedOut -> {
                navController.navigate("pantalla_principal") {
                    popUpTo("login_principal") { inclusive = true }
                    launchSingleTop = true
                }
            }

            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        texto_generico_one_line("datos user")
        Button(onClick = { viewModel_login_user.logout() }) {texto_generico_one_line("cerrar seccion")}
    }
}
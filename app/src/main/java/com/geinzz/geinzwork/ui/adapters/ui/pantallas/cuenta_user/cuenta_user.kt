package com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.firebase.auth.FirebaseAuth

val firebaseAuth = FirebaseAuth.getInstance()

@Composable
fun cuenta_user(viewModel_login_user: viewModel_login_user, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        texto_generico_one_line("datos user")
        Button(onClick = { firebaseAuth.signOut()
            viewModel_login_user.logout()
            navController.navigate("pantalla_principal") {
                popUpTo("login_principal") { inclusive = true }
                launchSingleTop = true
            }}) { texto_generico_one_line("cerra seccioon") }
    }

}
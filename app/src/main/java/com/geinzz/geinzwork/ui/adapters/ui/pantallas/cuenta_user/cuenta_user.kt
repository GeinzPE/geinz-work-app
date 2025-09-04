package com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.Cartas_expandibles
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.expandibles_wrapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
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
    Box() {
        img_fondo_user(R.drawable.f2)
        LazyColumn(modifier = Modifier.padding(horizontal = 10.dp)) {
            item {

                protada_perfil_user(
                    R.drawable.f2,
                    "https://r2.photoaistudio.com/photo_demo_flux/mardi_gras/9d2ff1ff-aa98-4aed-a70a-b9fda565be7c.jpeg"
                )
                spacer_vertical(10.dp)
            }
            item {
                expandible_datos_user()
                spacer_vertical(10.dp)
            }
            item {
                cartas_expandible_datos_cotacto()
                spacer_vertical(10.dp)
            }

            item {
                cartas_Expandible_notificaciones()
                spacer_vertical(10.dp)
            }



            item {
                Button(
                    onClick = { viewModel_login_user.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                ) { texto_generico_one_line("cerrar seccion") }
            }
        }

    }


}

@Composable
fun img_fondo_user(img_fondo: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img_fondo)
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 1f),
                            Color.Transparent,              // al medio se difumina
                            Color.Black.copy(alpha = 1f)  // abajo aún más oscuro
                        )
                    )
                )
        )
    }
}

@Composable
fun protada_perfil_user(img_portada: Int, img_perfil: String) {
    Box(
        modifier = Modifier
            .height(250.dp)
            .padding(vertical = 10.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img_portada)
                .crossfade(true)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(200.dp)
                .clip(RoundedCornerShape(10))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(10))
                .background(Color.Black.copy(alpha = 0.3f))
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.Bottom
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img_perfil)
                    .crossfade(true)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias)
                    .build(),
                contentDescription = "",
                modifier = Modifier
                    .border(3.dp, Color.White, CircleShape)
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            carta_nombre_user()

        }
    }
}

@Composable
fun carta_nombre_user() {
    Box(
        modifier = Modifier
            .wrapContentSize().clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 10.dp, horizontal = 5.dp)
    ) {
        texto_generico_one_line(
            "@Benja_la_",
            MaterialTheme.typography.titleMedium
        )
    }
}


@Composable
fun expandible_datos_user() {
    Cartas_expandibles {
        expandibles_wrapp("Datos del usuario", null, iconVector = Icons.Filled.Person, false) {}
    }
}

@Composable
fun cartas_expandible_datos_cotacto() {
    Cartas_expandibles {
        expandibles_wrapp("Datos de contacto", null, iconVector = Icons.Filled.Phone, false) {}
    }
}

@Composable
fun cartas_Expandible_notificaciones() {
    Cartas_expandibles {
        expandibles_wrapp("Notificaciones", null, iconVector = Icons.Filled.Notifications, false) {}
    }
}


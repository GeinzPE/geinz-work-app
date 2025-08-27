package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nativationWrapper
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.LoginState
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IniciarSeccion(
    navController : NavController,
    listener_Crear_cuenta: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewmodel_login: viewModel_login_user = viewModel()
    val loginState by viewmodel_login.loginState.observeAsState()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("LOGIN_GOOGLE", "Correo de Google: ${account.email}")
            viewmodel_login.loginWithGoogle(account.idToken)
        } catch (e: Exception) {
            Log.e("LOGIN_GOOGLE", "Excepción: ${e.message}", e)
        }
    }

    val listaImg = constantes_lista_localidades.lista_img_local
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentImageIndex = (currentImageIndex + 1) % listaImg.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1000)) with
                        fadeOut(animationSpec = tween(1000))
            }
        ) { index ->
            Image(
                painter = painterResource(id = listaImg[index]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(10.dp),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .blur(40.dp)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.55f)
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        )
        crear_cuenta(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp), viewmodel_login, navController,
            { listener_Crear_cuenta("crear") },
            {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        )
        when (loginState) {
            is LoginState.Success -> {
                val user = loginState as LoginState.Success
                Toast.makeText(context, "Bienvenido ${user.name}", Toast.LENGTH_SHORT).show()
                listener_Crear_cuenta("${user.email}")
            }

            is LoginState.Error -> {
                val error = loginState as LoginState.Error
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

            LoginState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            null -> {
                // estado inicial, nada
            }
        }
    }
}


@Composable
fun crear_cuenta(
    modifier: Modifier,
    viewmodelLoginUser: viewModel_login_user,
    navController: NavController,
    listener_Crear_cuenta: () -> Unit,
    listener_continuar_con_google: () -> Unit,

) {
    val context = LocalContext.current

    val loginState by viewmodelLoginUser.loginState.observeAsState()

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                Toast.makeText(context, "Bienvenido ${state.name}", Toast.LENGTH_SHORT).show()
                navController.navigate("pantalla_principal") {
                    popUpTo("login_principal") { inclusive = true }
                }
            }
            is LoginState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            LoginState.Loading -> {
                // puedes mostrar un loading si quieres
            }
            null -> Unit
        }
    }

    Column(modifier = modifier) {
        var password by remember { mutableStateOf("") }
        var correo by remember { mutableStateOf("") }
        var error_correo by remember { mutableStateOf(false) }
        var error_pass by remember { mutableStateOf(false) }

        MyOutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            labelText = "Correo electrónico",
            placeholderText = "Escribe tu correo electrónico",
            keyboardType = KeyboardType.Email,
            isError = error_correo,
        )

        MyOutlinedTextField(
            value = password,
            onValueChange = { password = it },
            labelText = "Escriba su contraseña",
            placeholderText = "Escriba su contraseña",
            texto_error = "El campo es obligatorio",
            isError = error_pass
        )
        Button(onClick = { viewmodelLoginUser.logear_user(correo, password) }) {
            texto_generico_one_line("Iniciar seccion")
        }

        spacer_vertical(10.dp)
        btn_secciones(
            "Continuar con Google",
            R.drawable.gmail_img
        ) { listener_continuar_con_google() }
        spacer_vertical(10.dp)
        crear_cuenta { listener_Crear_cuenta() }
    }
}

@Composable
fun btn_secciones(texto: String, icono: Int = 0, listener_btn: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = { listener_btn() },
        text = { texto_generico_one_line(texto) },
        shape = RoundedCornerShape(50),
        icon = {
            Icon(
                painter = painterResource(icono),
                contentDescription = "",
                modifier = Modifier.size(20.dp), tint = Color.Unspecified
            )
        }
    )
}

@Composable
fun crear_cuenta(listner_crear_cuenta: () -> Unit) {
    Text(
        text = "Crear cuenta",
        modifier = Modifier.clickable { listner_crear_cuenta() },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

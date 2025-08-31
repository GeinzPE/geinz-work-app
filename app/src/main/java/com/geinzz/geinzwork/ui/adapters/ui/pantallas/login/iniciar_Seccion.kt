package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.util.Log
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavController
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.input_password
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.LoginState_inicio
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import kotlinx.coroutines.delay


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IniciarSeccion(
    viewModel_login_user: viewModel_login_user,
    navController: NavController,
    listener_Crear_cuenta: (String) -> Unit,
) {
    val context = LocalContext.current

    val loginState_principal by viewModel_login_user.loginStateCamposInicial.observeAsState()
    val listaImg = constantes_lista_localidades.lista_img_local
    var currentImageIndex by remember { mutableStateOf(0) }

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
            viewModel_login_user.loginWithGoogle(navController,account.idToken){correo->
                listener_Crear_cuenta(correo)
            }
        } catch (e: Exception) {
            Log.e("LOGIN_GOOGLE", "Excepción: ${e.message}", e)
        }
    }


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
        login_principal_apartado(
            loginState_principal,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            { correo, contra -> viewModel_login_user.logear_user(navController,correo, contra) },
            { listener_Crear_cuenta("crear") },
            { val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent) }
        )
//        LaunchedEffect(loginState_principal) {
//            when (val state = loginState_principal) {
//                is LoginState_inicio.Succes -> {
//                    // 🔹 Si el login viene de Google
//                    if (state.proveedor == "google") {
//                        val existe = viewModel_login_user.verificar_cuenta_google_provider(state.email.toString())
//                        if (existe) {
//                            Log.d("obtenemos_exist", existe.toString())
//                            navController.navigate("pantalla_principal") {
//                                popUpTo("login_principal") { inclusive = true }
//                            }
//                        } else {
//                            Log.d("obtenemos_exist", existe.toString())
//                            listener_Crear_cuenta(state.email.toString())
//                        }
//                    } else {
//                        // 🔹 Si es login normal (correo + contraseña)
//                        navController.navigate("pantalla_principal") {
//                            popUpTo("login_principal") { inclusive = true }
//                        }
//                        delay(100)
//                        Log.d("pasamos_parametros", "Login normal completado")
//                    }
//                }
//
//                is LoginState_inicio.error -> {
////                    Log.d("pasamos_parametros", "Error de login: ${state.msje}")
////                    when (state.tipo) {
////                        "correo_no_existe" -> {
////                            error_correo = true
////                            texto_error_correo = state.msje
////                            viewmodelLoginUser.resetLoginState()
////                        }
////
////                        "pass_incorrecta" -> {
////                            error_pass = true
////                            texto_error_contra = state.msje
////                            viewmodelLoginUser.resetLoginState()
////                        }
////
////                        else -> {
////                            Toast.makeText(context, "Error: ${state.msje}", Toast.LENGTH_SHORT).show()
////                        }
////                    }
//                }
//
//                LoginState_inicio.Loading -> {
//                    Log.d("pasamos_parametros", "Cargando...")
//                }
//
//                LoginState_inicio.LoggedOut -> {
//                    Log.d("pasamos_parametros", "Sesión cerrada")
//                }
//
//                null -> Unit
//            }
//        }
    }
}


@Composable
fun login_principal_apartado(
    loginState_principal: LoginState_inicio?,
    modifier: Modifier,
    listener_iniciar_seccion_geinz: (correo: String, contra: String) -> Unit,
    listener_Crear_cuenta_geinz: () -> Unit,
    listener_continuar_con_google: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var error_correo by remember { mutableStateOf(false) }
    var error_pass by remember { mutableStateOf(false) }
    var texto_error_correo by remember { mutableStateOf("") }
    var texto_error_contra by remember { mutableStateOf("") }
    var contra_oculta by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(loginState_principal) {
        when (val state = loginState_principal) {
            is LoginState_inicio.error -> {
                when (state.tipo) {
                    "correo_no_existe" -> {
                        error_correo = true
                        texto_error_correo = state.msje
                        error_pass = false
                        texto_error_contra = ""
                    }

                    "pass_incorrecta" -> {
                        error_pass = true
                        texto_error_contra = state.msje
                        error_correo = false
                        texto_error_correo = ""
                    }

                    else -> {
                        error_correo = false
                        texto_error_correo = ""
                        error_pass = false
                        texto_error_contra = ""
                    }
                }
            }

            else -> {
                error_correo = false
                texto_error_correo = ""
                error_pass = false
                texto_error_contra = ""
            }
        }
    }

    Column(modifier = modifier.imePadding()) {
        MyOutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                if (error_correo) {
                    error_correo = false
                }
            },
            labelText = "Correo electrónico",
            placeholderText = "Escribe tu correo electrónico",
            texto_error = texto_error_correo,
            keyboardType = KeyboardType.Email,
            isError = error_correo,
        )

        input_password(
            contra_oculta,
            error_pass,
            texto_error_contra,
            password,
            { contra_oculta = !contra_oculta },
            {
                password = it
                if (error_pass) {
                    error_pass = false
                }
            }
        )

        iniciar_seccion_normal { listener_iniciar_seccion_geinz(correo, password) }
        spacer_vertical(10.dp)
        iniciar_seccion_google { listener_continuar_con_google() }
        spacer_vertical(10.dp)
        crear_cuenta_geinz { listener_Crear_cuenta_geinz() }
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

@Composable
fun iniciar_seccion_normal(listener_logear_user: () -> Unit) {
    Button(onClick = { listener_logear_user() }) {
        texto_generico_one_line("Iniciar seccion")
    }
}

@Composable
fun iniciar_seccion_google(listener_continuar_con_google: () -> Unit) {
    btn_secciones("Continuar con Google", R.drawable.gmail_img) { listener_continuar_con_google() }
}


@Composable
fun crear_cuenta_geinz(listener_Crear_cuenta: () -> Unit) {
    crear_cuenta { listener_Crear_cuenta() }

}
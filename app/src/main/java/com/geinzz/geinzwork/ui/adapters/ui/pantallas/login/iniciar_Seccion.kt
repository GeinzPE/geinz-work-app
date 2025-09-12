package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.input_password
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.LoginState_inicio
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import kotlinx.coroutines.delay


@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IniciarSeccion(
    viewModel_login_user: viewModel_login_user,
    navController: NavController,
    listener_Crear_cuenta: (String) -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
            viewModel_login_user.loginWithGoogle(navController, account.idToken) { correo ->
                listener_Crear_cuenta(correo)
            }
        } catch (e: Exception) {
            Log.e("LOGIN_GOOGLE", "Excepción: ${e.message}", e)
        }
    }
//    val targetAlpha = if (listState.canScrollForward) 1f else 0f
//
//    val alphaAnim by animateFloatAsState(
//        targetValue = targetAlpha,
//        animationSpec = tween(durationMillis = 500)
//    )
    val frases = constantes_lista_localidades.lista_frances_inicio_seccion
    var index by remember { mutableStateOf(0) }

    var overlayEnabled by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        while (true) {
            delay(6000L)
            index = (index + 1) % minOf(listaImg.size, frases.size)
        }
    }
    var blurEnabled by remember { mutableStateOf(true) }

    val blurFixed = 16.dp // blur fijo, elegante y liviano


    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) with fadeOut(animationSpec = tween(500))
            }
        ) { index ->
            Image(
                painter = painterResource(id = listaImg[index]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { blurEnabled = !blurEnabled }
                    .blur(if (blurEnabled) blurFixed else 0.dp),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (blurEnabled) 0.5f else 0f),
                            Color.Black.copy(alpha = if (blurEnabled) 0.55f else 0f)
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        )




        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (blurEnabled) 0.5f else 0f)
                        )
                    )
                )
        )



        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(
                visible = blurEnabled,
                enter = fadeIn(animationSpec = tween(500)),   // 👈 solo opacidad
                exit = fadeOut(animationSpec = tween(500))    // 👈 solo opacidad
            ) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_geinz_blanco),
                            contentDescription = "",
                            modifier = Modifier.size(70.dp)
                        )
                    }
                    spacer_vertical(10.dp)
                    fraces_filtrado(frases, index)
                    spacer_vertical(10.dp)
                    login_principal_apartado(
                        loginState_principal,
                        Modifier
//                            .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
////                            .background(Color.Black)
                            .padding(20.dp),
                        { correo, contra ->
                            viewModel_login_user.logear_user(navController, correo, contra)
                            keyboardController?.hide()
                        },
                        { listener_Crear_cuenta("crear") },
                        {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
            // aplicamos el fade
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
fun fraces_filtrado(fraces: List<String>, index1: Int) {
    Crossfade(targetState = index1, label = "fraces") { txt ->
        Text(
            text = fraces[txt],
            style = MaterialTheme.typography.busquedaGeinzWork,
            textAlign = TextAlign.Center,
            maxLines = 2,        // máximo 2
            minLines = 2,        // 👈 asegura que siempre reserve 2
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )


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
//        MyOutlinedTextField(
//            value = correo,
//            onValueChange = {
//                correo = it
//                if (error_correo) {
//                    error_correo = false
//                }
//            },
//            labelText = "Correo electrónico",
//            placeholderText = "Escribe tu correo electrónico",
//            texto_error = texto_error_correo,
//            keyboardType = KeyboardType.Email,
//            isError = error_correo,
//        )
//
//        input_password(
//            contra_oculta,
//            error_pass,
//            texto_error_contra,
//            password,
//            { contra_oculta = !contra_oculta },
//            {
//                password = it
//                if (error_pass) {
//                    error_pass = false
//                }
//            }
//        )
        spacer_vertical(10.dp)
        iniciar_seccion_normal { listener_iniciar_seccion_geinz(correo, password) }
        spacer_vertical(15.dp)
        btn_continuar_con_google { listener_continuar_con_google() }
        spacer_vertical(15.dp)
        crear_cuenta_geinz { listener_Crear_cuenta_geinz() }
        spacer_vertical(10.dp)
    }

}


@Composable
fun btn_secciones(texto: String, icono: Int = 0, listener_btn: () -> Unit) {
    ExtendedFloatingActionButton(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary,
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
fun btn_continuar_con_google(listener_continuar_con_google: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(vertical = 12.dp)
            .clickable { listener_continuar_con_google() }
    ) {
        Image(
            painter = painterResource(R.drawable.gmail_img),
            contentDescription = "",
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        Text(
            "Continuar con Google",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun crear_cuenta(listner_crear_cuenta: () -> Unit) {
    Text(
        text = "Crear cuenta",
        modifier = Modifier.clickable { listner_crear_cuenta() },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun iniciar_seccion_normal(listener_logear_user: () -> Unit) {
    Button(
        onClick = { listener_logear_user() }, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ), modifier = Modifier.fillMaxWidth()
    ) {
        texto_generico_one_line("Iniciar seccion", MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 17.dp))
    }
}

@Composable
fun iniciar_seccion_google(listener_continuar_con_google: () -> Unit) {
    btn_secciones("Continuar con Google", R.drawable.gmail_img) { listener_continuar_con_google() }
}


@Composable
fun crear_cuenta_geinz(listener_Crear_cuenta: () -> Unit) {
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            texto_generico_one_line("Aun no tienes cuenta ?", MaterialTheme.typography.bodyMedium)
            spacer_horizonta(7.dp)
            crear_cuenta { listener_Crear_cuenta() }
        }
    }


}
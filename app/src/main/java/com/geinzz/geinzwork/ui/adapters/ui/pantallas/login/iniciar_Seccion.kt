package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.graphics.drawable.Icon
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IniciarSeccion(
    listener_Crear_cuenta: (String) -> Unit,
    listener_iniciar_seccion: () -> Unit,
    listener_continuar_con_google: () -> Unit
) {
    val context = LocalContext.current
    val viewmodel_login: viewModel_login_user = viewModel()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // 👈 importante
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            // 🔥 Iniciar sesión en Firebase con la credencial de Google
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Toast.makeText(context, "✅ Sesión iniciada con Google", Toast.LENGTH_SHORT)
                            .show()
                        listener_Crear_cuenta("completar")
                        val user = FirebaseAuth.getInstance().currentUser
                        Log.d("LOGIN_GOOGLE", "Usuario: ${user?.displayName} - ${user?.email}")
                    } else {
                        Toast.makeText(
                            context,
                            "❌ Error al autenticar en Firebase",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("LOGIN_GOOGLE", "Error: ${authTask.exception?.message}")
                    }
                }

        } catch (e: Exception) {
            Toast.makeText(context, "❌ Falló el login con Google", Toast.LENGTH_SHORT).show()
            Log.e("LOGIN_GOOGLE", "Excepción: ${e.message}", e)
        }
    }

    var mostrar_dialog_crear_cuenta by remember { mutableStateOf(false) }
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
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,                // totalmente transparente
                            Color.Black.copy(alpha = 0.5f),   // un poquito oscuro
                            Color.Black.copy(alpha = 0.55f)   // un poco más oscuro pero no negro
                        ),
                        startY = 0f,                         // comienza desde arriba
                        endY = 400f                          // 👈 controla hasta dónde baja el difuminado
                    )
                )
        )


        crear_cuenta(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            { listener_Crear_cuenta("crear") },
            {
                listener_continuar_con_google()
            },
            {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        )
    }
}


@Composable
fun crear_cuenta(
    modifier: Modifier,
    listener_Crear_cuenta: () -> Unit,
    listener_iniciar_seccion: () -> Unit,
    listener_continuar_con_google: () -> Unit
) {
    Column(modifier = modifier) {
        btn_secciones("Iniciar seccion", R.drawable.sin_item_carrito) { listener_iniciar_seccion() }
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
                modifier = Modifier.size(20.dp)
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

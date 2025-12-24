package com.geinzz.geinzwork

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nativationWrapper
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.DeepLinkViewModel
import com.geinzz.geinzwork.viewModels.viewModel_usuarios_general
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var datosViewModel: viewModel_usuarios_general
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()
    private lateinit var navController: androidx.navigation.NavHostController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        crearCanalNotificaciones()

        firebaseAuth = FirebaseAuth.getInstance()
        datosViewModel = ViewModelProvider(this)[viewModel_usuarios_general::class.java]
        datosViewModel.obtener_localida_nombre_user(firebaseAuth.uid.toString())

        procesarIntent(intent)

        setContent {
            FuenteControladaApp {
                GeinzWorkTheme {
                    navController = rememberNavController()
                    nativationWrapper(navController)

                    LaunchedEffect(Unit) {
                        delay(150)
                        deepLinkViewModel.pendingLinks.collectLatest { links ->
                            links.forEach { link ->
                                manejarDeepLink(Uri.parse(link))
                                deepLinkViewModel.consumeLink(link)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntent(intent)
    }

    private fun procesarIntent(intent: Intent) {
        val link = intent.getStringExtra("link") ?: intent.data?.toString()
        if (!link.isNullOrEmpty()) {
            Log.d("DeepLinkDebug", "LINK RECIBIDO -> $link")
            deepLinkViewModel.addLink(link)
        }
    }

    // ===============================
    // 🔥 DEEPLINK NIVEL PRODUCCIÓN
    // ===============================
    private fun manejarDeepLink(uri: Uri) {

        val tipo = uri.getQueryParameter("t")
            ?: uri.getQueryParameter("tipo")
            ?: return

        val idRaw = uri.getQueryParameter("id") ?: return

        val localidadRaw = uri.getQueryParameter("l")
            ?: uri.getQueryParameter("localidad")
            ?: return

        val categoriaRaw = uri.getQueryParameter("c")
            ?: uri.getQueryParameter("categoria")
            ?: ""

        val index = uri.getQueryParameter("i")?.toIntOrNull() ?: 0

        // 🔹 normalización crítica
        val id = idRaw.removePrefix("/")

        val localidad = when (localidadRaw.lowercase()) {
            "ba" -> "barranca"
            "par" -> "paramonga"
            "pat" -> "pativilca"
            "su" -> "supe"
            "pue" -> "puerto supe"
            else -> localidadRaw
        }

        val categoria = categoriaRaw.replace("+", " ")

        fun enc(value: String) = URLEncoder.encode(value, "UTF-8")

        when (tipo.lowercase()) {

            // 🏪 TIENDA
            "tienda", "ti" -> {
                val ruta = "mostrar_tiendas/" +
                        "${enc(localidad)}/" +
                        "${enc(id)}/" +
                        "${enc(categoria)}"

                Log.d("DeepLinkDebug", "NAVEGANDO -> $ruta")

                navController.navigate(ruta) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                }
            }

            // 🌍 TURISMO
            "turismo", "tu" -> {
                val ruta = "lugares_turisticos/${enc(localidad)}/${enc(id)}"
                navController.navigate(ruta) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                }
            }

            // 🎁 PROMO
            "p" -> {
                deepLinkViewModel.setPromoData(
                    id = id,
                    lugar = localidad,
                    index = index
                )
            }
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_geinz",
                "Geinz Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notificaciones generales"
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

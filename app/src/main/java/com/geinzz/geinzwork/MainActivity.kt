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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nativationWrapper
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.DeepLinkViewModel
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_usuarios_general
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: com.geinzz.geinzwork.databinding.ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var datos_viewmodel: viewModel_usuarios_general
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()
    private lateinit var navController: androidx.navigation.NavHostController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        binding = com.geinzz.geinzwork.databinding.ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        crearCanalNotificaciones()

        Log.d("DeepLinkDebug", "onCreate: iniciado")

        firebaseAuth = FirebaseAuth.getInstance()
        datos_viewmodel = ViewModelProvider(this)[viewModel_usuarios_general::class.java]
        datos_viewmodel.obtener_localida_nombre_user(firebaseAuth.uid.toString())

        Log.d("DeepLinkDebug", "onCreate: procesando intent inicial")
        procesarIntent(intent)

        setContent {
            FuenteControladaApp {
                GeinzWorkTheme {
                    navController = rememberNavController()
                    nativationWrapper(navController)

                    LaunchedEffect(navController) {
                        delay(100)
                        Log.d("DeepLinkDebug", "LaunchedEffect: observando pendingLinks")
                        deepLinkViewModel.pendingLinks.collectLatest { links ->
                            Log.d("DeepLinkDebug", "Links recibidos: $links")
                            links.forEach { link ->
                                Log.d("DeepLinkDebug", "Manejando link: $link")
                                manejarDeepLink(Uri.parse(link))
                                deepLinkViewModel.consumeLink(link)
                                Log.d("DeepLinkDebug", "Link consumido: $link")
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
        Log.d("DeepLinkDebug", "onNewIntent: nuevo intent recibido")
        procesarIntent(intent)
    }

    private fun procesarIntent(intent: Intent) {
        val linkFromExtra = intent.getStringExtra("link")
        val dataUri = intent.data

        val link = linkFromExtra ?: dataUri?.toString()
        if (!link.isNullOrEmpty()) {
            Log.d("DeepLinkDebug", "Intent contiene link a procesar -> $link")
            deepLinkViewModel.addLink(link)
        } else {
            Log.d("DeepLinkDebug", "Intent no contiene link ni data URI")
        }
    }



    private fun manejarDeepLink(uri: Uri) {

        // 🔹 soporta corto y largo
        val tipo = uri.getQueryParameter("t")
            ?: uri.getQueryParameter("tipo")
            ?: ""

        val id = uri.getQueryParameter("id") ?: ""

        val localidadRaw = uri.getQueryParameter("l")
            ?: uri.getQueryParameter("localidad")
            ?: ""

        val categoria = uri.getQueryParameter("c")
            ?: uri.getQueryParameter("categoria")
            ?: ""

        val index =uri.getQueryParameter("i")

        if (tipo.isEmpty() || id.isEmpty() || localidadRaw.isEmpty()) return

        // 🔹 NORMALIZAR LOCALIDAD (abreviado → real)
        val localidad = when (localidadRaw.lowercase()) {
            "ba" -> "barranca"
            "par" -> "paramonga"
            "pat" -> "pativilca"
            "su" -> "supe"
            "pue" -> "puerto supe"
            else -> localidadRaw
        }

        when (tipo.lowercase()) {

            // 🌍 TURISMO
            "turismo", "tu" -> {
                val ruta = "lugares_turisticos/$localidad/$id"
                navController.navigate(ruta) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                }
            }

            // 🏪 TIENDA (incluye p por ahora)
            // 🏪 TIENDA (incluye p por ahora)
            "tienda", "ti" -> {

                val categoriaLimpia = categoria.replace("+", " ")

                val ruta = "mostrar_tiendas/$localidad/$id/$categoriaLimpia"
                navController.navigate(ruta) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                }
            }

            "p" -> {
                deepLinkViewModel.setPromoData(
                    id = id,
                    lugar = localidad,
                    index = index?.toIntOrNull() ?: 0
                )

//                // 👇 solo navega a pantalla principal si no estás ahí
//                navController.navigate("pantalla_principal") {
//                    launchSingleTop = true
//                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
//                }
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
            channel.description = "Notificaciones generales de Geinz"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d("DeepLinkDebug", "Canal de notificaciones creado")
        }
    }
}


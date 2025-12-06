package com.geinzz.geinzwork

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.geinzz.geinzwork.Network_internet.BaseActivity
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.vistaTrabajador.vista_ver_productos_trabajadores
import com.geinzz.geinzwork.vistaTrabajador.vista_ver_publicaciones_trabajadores
import com.geinzz.geinzwork.utils.constantes.constantes.constantesPublicidad
import com.geinzz.geinzwork.databinding.ActivityMainBinding
import com.geinzz.geinzwork.databinding.BottomShettCambiosRealizadosBinding
import com.geinzz.geinzwork.fragmentos.categoriasFracment
import com.geinzz.geinzwork.fragmentos.contactoFracment
import com.geinzz.geinzwork.fragmentos.cuentaFracment
import com.geinzz.geinzwork.fragmentos.inicioFracment

import com.geinzz.geinzwork.fragmentos.sinRegistroFracment
import com.geinzz.geinzwork.ui.adapters.ui.localizate_geinz_wokr_ui
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nativationWrapper
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewModel_localizate_geinz
import com.geinzz.geinzwork.viewModels.viewModel_usuarios_general
import com.geinzz.geinzwork.vistaTiendas.TiendasGenerales
import com.geinzz.geinzwork.vistaTiendas.VistaTienda
import com.geinzz.geinzwork.vistaTiendas.vistaProductosGeneralTiendas
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var datos_viewmodel: viewModel_usuarios_general
    private val viewModel by viewModels<viewModel_localizate_geinz>()

    private lateinit var navController: androidx.navigation.NavHostController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        datos_viewmodel = ViewModelProvider(this)[viewModel_usuarios_general::class.java]
        datos_viewmodel.obtener_localida_nombre_user(firebaseAuth.uid.toString())

        setContent {
            FuenteControladaApp {
                GeinzWorkTheme {
                    navController = rememberNavController()
                    nativationWrapper(navController)

                    intent?.data?.let { uri ->
                        LaunchedEffect(uri) {
                            manejarDeepLink(uri)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.data?.let { uri ->
            manejarDeepLink(uri)
        }
    }

    private fun manejarDeepLink(uri: Uri) {
        Log.d("objtenosuir", "${uri.toString()}")

        lifecycleScope.launch {
            val tipo = uri.getQueryParameter("tipo") ?: ""
            val id = uri.getQueryParameter("id") ?: ""
            val localidad = uri.getQueryParameter("localidad") ?: ""
            val categoria = uri.getQueryParameter("categoria") ?: ""

            if (tipo.isEmpty() || id.isEmpty() || localidad.isEmpty()) {
                Log.d("DeepLink", "Faltan parámetros necesarios")
                return@launch
            }

            when (tipo.lowercase()) {
                "lugar", "turismo" -> {
                    navController.currentBackStackEntryFlow.first()
                    navController.navigate("lugares_turisticos/$localidad/$id") {
                        popUpTo("pantalla_principal") { inclusive = false }
                    }
                }
                "tienda" -> { // tiendas
                    val categoriaEncoded = URLEncoder.encode(categoria, "UTF-8").replace("+", "%20")
                    val ruta = "mostrar_tiendas/$localidad/$id/$categoriaEncoded"

                    navController.currentBackStackEntryFlow.first()
                    navController.navigate(ruta) {
                        popUpTo("pantalla_principal") { inclusive = false }
                    }
                }
                else -> {
                    Log.d("DeepLink", "Tipo desconocido: $tipo")
                }
            }
        }
    }


}

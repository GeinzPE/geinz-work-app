package com.geinzz.geinzwork

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.geinzz.geinzwork.data.model.localizate_geinz.DeepLinkViewModelFactory
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.UiAction
import com.geinzz.geinzwork.herramientas_geinz.constantes.get_alias_tienda.resolverAlias
import com.geinzz.geinzwork.model.SessionRepository
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nativationWrapper
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.handleScanResult
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_carga_ucrop_img
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.generar_qr_cordenadas_tienda
import com.geinzz.geinzwork.viewModels.DeepLinkViewModel
import com.geinzz.geinzwork.viewModels.UiActionViewModel
import com.geinzz.geinzwork.viewModels.viewModel_usuarios_general
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder



class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var datosViewModel: viewModel_usuarios_general
    private lateinit var deepLinkViewModel: DeepLinkViewModel
    private lateinit var navController: androidx.navigation.NavHostController
    lateinit var cropLauncher: ActivityResultLauncher<Intent>
    private val uiActionVM: UiActionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        firebaseAuth = FirebaseAuth.getInstance()

        enableEdgeToEdge()
        cropLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultUri = UCrop.getOutput(result.data!!)
                    if (resultUri != null) {
                        onImageCropped(resultUri)
                    }
                }
            }

        crearCanalNotificaciones()


        datosViewModel = ViewModelProvider(this)[viewModel_usuarios_general::class.java]
        datosViewModel.obtener_localida_nombre_user(firebaseAuth.uid.toString())


        val sessionRepository = SessionRepository(applicationContext)

        deepLinkViewModel = ViewModelProvider(
            this,
            DeepLinkViewModelFactory(sessionRepository)
        )[DeepLinkViewModel::class.java]

        procesarIntent(intent)

        setContent {
            FuenteControladaApp {
                GeinzWorkTheme {
                    navController = rememberNavController()
                    nativationWrapper(uiActionVM,navController,deepLinkViewModel)

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

    private fun onImageCropped(uri: Uri) {
        // 👉 aquí mandamos la URI a Compose
        constantes_carga_ucrop_img.croppedUri = uri
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

    private fun manejarDeepLink(uri: Uri) {
        Log.d("navegacion_rq", uri.toString())
        val repo_eres_socio=repo_eres_socio()

        val tipo = uri.getQueryParameter("t")
            ?: uri.getQueryParameter("tipo")
            ?: ""

        val idRaw = uri.getQueryParameter("id") ?: ""

        val localidadRaw = uri.getQueryParameter("l")
            ?: uri.getQueryParameter("localidad") ?: uri.getQueryParameter("loc")
            ?: ""

        val categoriaRaw = uri.getQueryParameter("c")
            ?: uri.getQueryParameter("categoria")
            ?: ""

        val cordenadaRaw = uri.getQueryParameter("cor") ?:""


        val index = uri.getQueryParameter("i")?.toIntOrNull() ?: 0

        val id_promocion = uri.getQueryParameter("pi")?:""
        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2 && pathSegments[0] == "perfil") {
            val alias = pathSegments[1]
            val promoIndex = uri.getQueryParameter("p")?.toIntOrNull()

            Log.d("DeepLinkDebug", "ALIAS DETECTADO -> $alias, promo=$promoIndex")

            resolverAlias(alias, this) { id, localidad, categoria ->
                if (promoIndex != null) {
                    // 🎁 Es una promo
                    deepLinkViewModel.setPromoData(
                        id = id,
                        lugar = localidad,
                        index = promoIndex
                    )
                } else {
                    // 🏪 Es una tienda normal
                    navegarATienda(id, localidad, categoria)
                }
            }
            return
        }
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

        val pantallas_screen: String? = when (id.lowercase()) {
            "nvng" -> "nuevos_negocios"
            "seyt" -> "servicios_y_tramites"
            "lgtr" -> "lugares_turisticos"
            "nemg" -> "salud_y_seguridad"
            "ads"->"promocionar_ads"
            "rec"->"promocionar_rec"
            "in"->"inmobiliaria"

            else -> null
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

            "to"->{
                val ruta = "mostrar_tiendas/" +
                        "${enc(localidad)}/" +
                        "${enc(id)}/" +
                        "${enc(categoria)}"

                Log.d("DeepLinkDebug", "cddddNAVEGANDO -> $ruta")
                repo_eres_socio.agregar_contador_estadistica_noti("abierto",id,localidad,id_promocion,firebaseAuth.uid.toString())

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

            "prn"->{
                deepLinkViewModel.setPromo_notificacion(
                    "promo_notificacion",
                    id_tienda = id,
                    lugar = localidad,
                    id_promo = id_promocion,
                )

            }

            "prms"->{
                uiActionVM.emitir(
                    UiAction.Abrir_pantalla_promos_cecanas(id_promocion, localidad)
                )
            }
            "pmspls" -> {
                // El parámetro "p" trae los IDs separados por coma
                // Ej: p=koOZB9Ju0w2TOQX2PdsT,xhsBaAgoWpa0Mfm92405,XuFt7RIL45p8tdUwBNak
                val idsRaw = uri.getQueryParameter("p") ?: ""
                val idsList = if (idsRaw.isNotEmpty()) {
                    idsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else null

                uiActionVM.emitir(
                    UiAction.Abrir_pantalla_promos_cecanas(
                        id_promocion = id_promocion,
                        localida_tienda = localidad,
                        ids = idsList
                    )
                )
            }



            "prf" -> {
                uiActionVM.emitir(
                    UiAction.AbrirPerfil(idRaw, localidadRaw)
                )
            }

            "rew" -> {
                uiActionVM.emitir(
                    UiAction.ReviewPublica(idRaw, "barranca")
                )

            }

            "ru" -> {
                val (lat, lng) =
                    generar_qr_cordenadas_tienda.decodificarCoordenadas(cordenadaRaw!!)
                uiActionVM.emitir(
                    UiAction.Ruta(idRaw,lat, lng)
                )
            }

            "rewc" -> {
                val (lat, lng) =
                    generar_qr_cordenadas_tienda.decodificarCoordenadas(cordenadaRaw!!)
                uiActionVM.emitir(
                    UiAction.ReviewPrivada(idRaw, "barranca", lat, lng)
                )
            }

            "scr" -> {
                pantallas_screen?.let { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                    }
                }
            }
            "in"->{
                uiActionVM.emitir(
                UiAction.abrir_pantalla_inmobiliara(idRaw, localidad)
                )
            }
        }
    }

        private fun navegarATienda(id: String, localidad: String, categoria: String) {
        fun enc(value: String) = java.net.URLEncoder.encode(value, "UTF-8")

        val ruta = "mostrar_tiendas/${enc(localidad)}/${enc(id)}/${enc(categoria)}"

        Log.d("DeepLinkDebug", "NAVEGANDO -> $ruta")
        navController.navigate(ruta) {
            launchSingleTop = true
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
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
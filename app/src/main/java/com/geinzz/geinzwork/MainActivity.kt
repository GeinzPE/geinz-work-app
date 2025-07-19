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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
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

class MainActivity :  BaseActivity() , View.OnApplyWindowInsetsListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentFragmentTag: String? = null
    private var isUpdatingBottomNavigation = false
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var bindingbottomShet: BottomShettCambiosRealizadosBinding

    private lateinit var remoteConfig: FirebaseRemoteConfig

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPagerAdapter: ViewPagerAdapter



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        // ✅ Primero inflas el binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        // ✅ Luego seteas el layout
        setContentView(binding.root)

        // ✅ Ahora sí puedes usar binding.root sin error
        ViewCompat.setOnApplyWindowInsetsListener(binding.buttonNavigation) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars =
                insets.getInsets(WindowInsetsCompat.Type.statusBars()) // Obtener insets de la barra de estado

            view.setPadding(
                0,
                statusBars.top,
                0,
                navigationBars.bottom
            ) // Ajustar padding superior e inferior
            insets
        }

        viewPager = findViewById(R.id.viewPager)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        binding.VistaTiendas.setOnClickListener {
            startActivity(Intent(this, TiendasGenerales::class.java))
        }
        val storyId = intent.getStringExtra("story_id")
        if (storyId != null) {
            println("ID del story recibido en la actividad: $storyId")
        }
        hideNavigationBar()
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val idPublicidadPrimaria = deepLink.getQueryParameter("idDocumento")
                    val idAnuncio = deepLink.getQueryParameter("idAnuncio")
                    val storeId = deepLink.getQueryParameter("id")
                    val anunciosPrimarios = deepLink.getQueryParameter("idAnuncioPrimarioGeinz")
                    val userId = deepLink.getQueryParameter("idTienda")
                    val ArticiculoClikado = deepLink.getQueryParameter("idArticulo")
                    val idTiendaSeleccionada = deepLink.getQueryParameter("idTiendaSeleccionada")
                    val idTrabajadorGeinz = deepLink.getQueryParameter("idTrabajadorGeinz")

                    val idTrabajador = deepLink.getQueryParameter("idTrabajadorVeri")
                    val id_publicacion_clikeada = deepLink.getQueryParameter("idpublicacion")


                    val id_trabajadores_productos =
                        deepLink.getQueryParameter("idTrabajadorVeriProducto")
                    val id_trabajadores_productos_clikeados =
                        deepLink.getQueryParameter("idProducto")


                    val id_trabajador_publicacion =
                        deepLink.getQueryParameter("idTrabajadorRec")
                    val id_publicacion_trabajo_realizado =
                        deepLink.getQueryParameter("idpublicacionRec")

                    when {

                        id_trabajador_publicacion != null && id_publicacion_trabajo_realizado != null -> obtener_trabajo_realizado_info(
                            id_trabajador_publicacion,
                            id_publicacion_trabajo_realizado
                        )

                        idPublicidadPrimaria != null && idAnuncio != null -> openPublicidadPrimaria(
                            idPublicidadPrimaria,
                            idAnuncio
                        )

                        idTrabajador != null && id_publicacion_clikeada != null -> openDinamickLink_Publicaciones_recientes(
                            idTrabajador,
                            id_publicacion_clikeada
                        )

                        id_trabajadores_productos != null && id_trabajadores_productos_clikeados != null -> openDinamickLink_productos_publicados(
                            id_trabajadores_productos,
                            id_trabajadores_productos_clikeados
                        )

                        idTrabajadorGeinz != null -> openVistaTrabajador(idTrabajadorGeinz)
                        anunciosPrimarios != null -> openAnunciosPrimarios(anunciosPrimarios)
                        storeId != null -> openPublicidad(storeId)
                        userId != null -> openPerfilTienda(userId)
                        ArticiculoClikado != null && idTiendaSeleccionada != null -> openArticuloTienda(
                            ArticiculoClikado, idTiendaSeleccionada
                        )

                        else -> println("No se encontraron parámetros válidos en el enlace")
                    }
                }
            }
            .addOnFailureListener(this) {
                println("No se encontró el enlace dinámico: $it")
            }
        changeSystemBarsColor(Color.parseColor("#744ACB"))

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Fetch cada hora
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        // Fetch y activar los valores de Remote Config
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val versionMasReciente = remoteConfig.getString("latest_version")
                    val url_playStore = remoteConfig.getString("urlPlayStore")
                    println("obtenemos la version mas reciente $versionMasReciente")
                    verificarVersion(url_playStore, versionMasReciente)
                } else {
                    println("error al obtener la version")
                }
            }
        intent?.let {
            if (it.hasExtra("target")) {
                val target = it.getStringExtra("target")
                if (target == "new_version_section") {
                    openNewVersionSection()
                }
            }
        }

        viewPager = binding.viewPager
        bottomNav = binding.buttonNavigation

        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.inicio -> viewPager.currentItem = 0
                R.id.Contacto -> viewPager.currentItem = 1
                R.id.Categorias -> viewPager.currentItem = 2
                R.id.Cuenta -> viewPager.currentItem = 3
            }
            true
        }



        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val menuItemId = when (position) {
                    0 -> R.id.inicio
                    1 -> R.id.Contacto
                    2 -> R.id.Categorias // Ajustado para corresponder a la posición 2
                    3 -> R.id.Cuenta // Ajustado para corresponder a la posición 3
                    else -> R.id.inicio // Valor predeterminado
                }
                bottomNav.selectedItemId = menuItemId
            }
        })
    }


    override fun getRootView(): View = binding.root

    val getViewPager: ViewPager2
        get() = viewPager

    fun setViewPagerSwipeEnabled(enabled: Boolean) {
        viewPager.isUserInputEnabled = enabled
    }

    private inner class ViewPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            // Devuelve el número total de fragmentos
            return 4 // Cambia esto según la cantidad de tus fragmentos
        }

        override fun createFragment(position: Int): Fragment {
            // Crea y devuelve el fragmento para la posición dada
            return when (position) {
                0 -> inicioFracment() // Reemplaza con tus fragmentos
                1 -> contactoFracment()
                2 -> categoriasFracment()
                3 -> if (firebaseAuth.currentUser == null) sinRegistroFracment() else cuentaFracment()
                else -> inicioFracment() // Valor predeterminado
            }
        }
    }

    private fun openVistaTrabajador(idTrabajadorGeinz: String) {
        val userCollections =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores").document(idTrabajadorGeinz)
        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones").collection("activos").document(idTrabajadorGeinz)
        constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(db, "verificacion")

        userCollections.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imperfil = data?.get("imagenPerfil") as? String ?: ""
                val nombre = data?.get("nombre") as? String ?: ""
                val nacionalidad = data?.get("nacionalidad") as? String ?: ""
                val categoria = data?.get("categoriaTrabajo") as? String ?: ""
                val id = data?.get("id") as? String ?: ""

                val vista = Intent(this, vistaTrabajador::class.java).apply {
                    putExtra("id", id)
                    putExtra("nombreUSer", nombre)
                    putExtra("nacionalidad", nacionalidad)
                    putExtra("categoria", categoria)
                    putExtra("imagenPerfil", imperfil)
                }
                startActivity(vista)

            }
        }.addOnFailureListener { e ->
            Log.e("error", "error al obtener los datos")
        }


    }

    var nombreUsuario = ""
    var id = ""

    private fun openNewVersionSection() {
        val intent = Intent(this, vistas_anuncios_general::class.java)
        startActivity(intent)
    }

    private fun verificarVersion(urlPlayStore: String, versionMasReciente: String) {
        val versionActual = packageManager.getPackageInfo(packageName, 0).versionName
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val versionGuardada = sharedPreferences.getString("ultima_version", "")

        // Si la versión actual es menor que la más reciente, pedir actualizar
        if (versionActual != null) {
            if (versionActual < versionMasReciente) {
                AlertDialog.Builder(this)
                    .setTitle("Actualización disponible")
                    .setMessage("Hay una nueva versión de la aplicación disponible. Actualiza para disfrutar de las últimas mejoras.")
                    .setPositiveButton("Actualizar") { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(urlPlayStore)
                        }
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "No se pudo abrir el enlace de actualización.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Más tarde", null)
                    .show()
            }
            // Si se detecta una nueva versión nunca mostrada antes
            else if (versionActual != null) {
                if (versionGuardada.isNullOrBlank() || versionActual > versionGuardada) {
                    dialog = BottomSheetDialog(this)
                    dialogControlVersiones(versionMasReciente)
                    dialog.show()

                    // Guardar la nueva versión como mostrada
                    with(sharedPreferences.edit()) {
                        putString("ultima_version", versionActual)
                        apply()
                    }
                }
            }
        }
    }

    private fun dialogControlVersiones(versionMasReciente: String) {
        bindingbottomShet = BottomShettCambiosRealizadosBinding.inflate(LayoutInflater.from(this))
        val view = bindingbottomShet.root
        bottomSheet = view.findViewById(R.id.cerrar)
        val db = FirebaseFirestore.getInstance().collection("controVersiones").document("control")

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val texto = data?.get("texto") as? String ?: ""
                if (texto.isNotEmpty()) {
                    val formattedText = Html.fromHtml(texto, Html.FROM_HTML_MODE_COMPACT)
                    bindingbottomShet.versionActual.text = versionMasReciente
                    bindingbottomShet.textoCambiosRealiazdos.text = formattedText

                    // Mostrar el diálogo después de cargar y configurar el texto
                    dialog.setContentView(view)
                    dialog.show()
                }
            }
        }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "No se pudo encontrar los cambios de la nueva versión",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun hideNavigationBar() {
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }

    private fun openPublicidad(storeId: String?) {
        val db = FirebaseFirestore.getInstance().collection("noticias").document(storeId!!)
        storeId?.let {
            val intent = Intent(this, ver_detalles_Promociones::class.java).apply {
                constantesPublicidad.agregarCantidadClickAnuncios(db, "", "click")
                putExtra("idAnuncio", it)
                putExtra("entrada", "noticia")
            }
            startActivity(intent)
        }
    }


    private fun openAnunciosPrimarios(id: String?) {
        id?.let {
            val intent = Intent(this, oferta_principales_geinz::class.java).apply {
                putExtra("idPublicidad", it)
            }
            startActivity(intent)
        }
    }

    private fun openPerfilTienda(storeId: String?) {
        storeId?.let {
            val intent = Intent(this, VistaTienda::class.java).apply {
                putExtra("idTienda", it)
            }
            startActivity(intent)
        }
    }

    private fun openArticuloTienda(idProducto: String?, idtienda: String?) {
        val intent = Intent(this, vistaProductosGeneralTiendas::class.java).apply {
            putExtra("idProductoClikado", idProducto)
            putExtra("idTienda", idtienda)
        }
        startActivity(intent)
    }

    private fun openPublicidadPrimaria(idPublicidadPrimaria: String?, idAnuncio: String?) {
        val db =
            FirebaseFirestore.getInstance().collection("anuncios").document(idPublicidadPrimaria!!)
                .collection("anuncios").document(idAnuncio!!)
        constantesPublicidad.agregarCantidadClickAnuncios(
            db,
            idAnuncio.toString(),
            "Clicks"
        )
        val intent = Intent(this, vistas_anuncios_general::class.java).apply {
            putExtra("docuemnto", idPublicidadPrimaria)
            putExtra("anuncio", idAnuncio)
        }
        startActivity(intent)

    }

    private fun obtener_trabajo_realizado_info(idTrabajadorRec: String, idpublicacion: String) {
        val intent = Intent(this, vistaTrabajador::class.java).apply {
            putExtra(Variables.id, idTrabajadorRec)
            putExtra("id_publicacion", idpublicacion)
        }
        startActivity(intent)

    }

    private fun openDinamickLink_Publicaciones_recientes(
        idTrabajador: String?,
        id_publicacion_clikeada: String?
    ) {
        val vista =
            Intent(this, vista_ver_publicaciones_trabajadores::class.java).apply {
                putExtra("id_trabajador", idTrabajador)
                    .putExtra("id_publicacion", id_publicacion_clikeada)
            }
        startActivity(vista)
    }

    private fun openDinamickLink_productos_publicados(
        id_trabajador: String,
        id_publicacion: String,

        ) {
        val vista =
            Intent(this, vista_ver_productos_trabajadores::class.java).apply {
                putExtra("id_trabajador", id_trabajador)
                    .putExtra("id_publicacion", id_publicacion)
            }
        startActivity(vista)
    }

    private fun changeSystemBarsColor(color: Int) {
        window.navigationBarColor = color
        window.statusBarColor = color
    }




    override fun onBackPressed() {
        if (viewPager.currentItem == 0) { // Verifica si el fragmento actual es el primero (inicio)
            super.onBackPressed() // Si es el inicio, realiza la acción predeterminada (salir de la actividad)
        } else {
            viewPager.currentItem = 0 // Si no es el inicio, cambia al fragmento de inicio
        }
    }


    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


    val bundle = Bundle().apply {
        putString("clave", "$nombreUsuario")
        putString("idUSer", "$id")
    }


    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        TODO("Not yet implemented")
    }

}
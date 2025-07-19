package com.geinzz.geinzwork.vistaTiendas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.NotificacionRS
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.constantes.constantesNoticias.mostrarDatos
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaTiendaBinding
import com.geinzz.geinzwork.model.dataclassArticulos
import com.geinzz.geinzwork.model.dataclassGridNoticiasVistaTiendas
import com.geinzz.geinzwork.model.dataclassPromociones
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class VistaTienda : AppCompatActivity() {
    private lateinit var binding: ActivityVistaTiendaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var showBottomSheetButton: ImageButton
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private val listaimagenes = mutableListOf<dataclassGridNoticiasVistaTiendas>().toMutableList()
    private val listaPromociones = mutableListOf<dataclassPromociones>()
    private val listaArticulos = mutableListOf<dataclassArticulos>()
    private var isNotificationOn = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaTiendaBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        constantes.carga(
            5000, { mostrarDatos(binding.loading, binding.ContainerContenido, binding.Tiendas) })
        val idTienda = intent.getStringExtra("idTienda").toString()
        val ubicacion = intent.getStringExtra("ubicacion").toString()

        confirmacionTiendaReserva(idTienda, this)
        obtenerDatosTiendaFirestore(idTienda)
        verificarTienda(idTienda)

        showBottomSheetButton = binding.Tiendas
        dialog = BottomSheetDialog(this)
        showBottomSheetButton.setOnClickListener {
            showDialog(idTienda)
            dialog.show()
        }
        colocarNoti_on_off(idTienda)


        binding.notificaciones.setOnClickListener {
            isNotificationOn = !isNotificationOn

            if (isNotificationOn) {
                toggleNotificacion(idTienda, true) {
                    binding.notificaciones.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.notification_on
                        )
                    )
                }
            } else {
                toggleNotificacion(idTienda, false) {
                    binding.notificaciones.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.notification_off
                        )
                    )
                }
            }
        }


        binding.VerTodosProductos.setOnClickListener {
            val vista = Intent(this, vistaRecicleProductos::class.java)
            vista.putExtra("idTienda", idTienda)
            startActivity(vista)
        }

        binding.VerTodaLosServicios.setOnClickListener {
            var vista = Intent(this, ver_mas_servicios::class.java).apply {
                putExtra("idTienda", idTienda)
            }
            startActivity(vista)
        }

        verificarSiSigue(idTienda, binding.dejarDeSeguirOSeguir)
        binding.Categorias.setOnClickListener {
            retornarimgPerfilTienda(idTienda) { img ->
                if (img != null) {
                    popupMenu(img, idTienda)
                }
            }

        }

        binding.dejarDeSeguirOSeguir.setOnClickListener {
            // Verificar si el usuario está autenticado
            if (firebaseAuth.currentUser == null) {
                // Mostrar un diálogo para registrar al usuario
                val builder = AlertDialog.Builder(this)
                builder.setTitle("No estás registrado en Geinz Work")
                builder.setMessage("Regístrate en Geinz Work para que puedas seguir.")
                builder.setPositiveButton("Cuenta Simple") { dialog, _ ->
                    // Mostrar diálogo de carga y redirigir a la pantalla de registro
                    constantes.showLoadingDialog(
                        this,
                        2000,
                        "Cargando información",
                        "Espere un momento..."
                    )
                    val intent = Intent(this, CuentaFreelancer::class.java).apply {
                        putExtra("tipoCuenta", "cuentaSimple")
                        putExtra("Title", "Cuenta Simple")
                        putExtra("pasos", "Estás a 1/2 pasos")
                    }
                    startActivity(intent)
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cuenta Trabajador") { dialog, _ ->
                    val intent = Intent(this, CuentaFreelancer::class.java).apply {
                        putExtra("tipoCuenta", "cuentaTrabajador")
                        putExtra("Title", "Cuenta Freelancer")
                        putExtra("pasos", "Estás a 1/5 pasos")
                    }
                    startActivity(intent)
                    dialog.dismiss()
                }
                builder.create().show()
            } else {

                val buttonText = binding.dejarDeSeguirOSeguir.text.toString()
                when (buttonText) {
                    "Seguir" -> {
                        binding.notificaciones.isVisible = true
                        seguir(idTienda)
                        binding.dejarDeSeguirOSeguir.text = "Siguiendo"

                    }

                    "Siguiendo" -> {
                        showCustomUnfollowDialog(this, idTienda)
                    }
                }
            }
        }

        binding.VerTodaLasPublicaciones.setOnClickListener {
            verTodaLasPublicaiones(idTienda)
        }
        binding.VerTodaLasNoticias.setOnClickListener {
            vermasNoticiasOfertas(idTienda)
        }
        binding.mapa.setOnClickListener {
            constantes.abrirGoogleMaps(this, ubicacion)
            finish()
        }
        binding.carritoCompras.setOnClickListener {
            val vista = Intent(this, carrito_compras::class.java)
            startActivity(vista)
        }
    }

    fun retornarimgPerfilTienda(idTienda: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imgPerfil = data?.get("imgPerfil") as? String
                callback(imgPerfil)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun showCustomUnfollowDialog(context: Context, idTienda: String) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_unfollow, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            Toast.makeText(context, "Has dejado de seguir", Toast.LENGTH_SHORT).show()
            dejarDeSeguir(idTienda)
            binding.dejarDeSeguirOSeguir.text = "seguir"
            binding.notificaciones.isVisible = false
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun toggleNotificacion(idTienda: String, activar: Boolean, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
            .collection(Variables.collection_Tiendas)
            .document(idTienda)
            .collection(Variables.collection_seguidores_tiendas)
            .document(firebaseAuth.uid.toString())

        val hashMap = hashMapOf<String, Any>(
            "notificaciones" to activar
        )

        db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
            onSuccess()
            val mensaje = if (activar) "Notificaciones Activadas" else "Notificaciones Desactivadas"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.e("Error_noti", "Error al cambiar la configuración de notificaciones: $e")
        }
    }

    private fun colocarNoti_on_off(idTienda: String) {
        val db = FirebaseFirestore.getInstance()
            .collection(Variables.collection_Tiendas)
            .document(idTienda)
            .collection(Variables.collection_seguidores_tiendas)
            .document(firebaseAuth.uid.toString())

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val noti = res.getBoolean("notificaciones") ?: false
                isNotificationOn = noti
                val drawable = if (isNotificationOn) {

                    R.drawable.notification_on
                } else {

                    R.drawable.notification_off

                }
                binding.notificaciones.setImageDrawable(ContextCompat.getDrawable(this, drawable))
            }
        }
    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showDialog(idTienda: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet, null, false)
        bottomSheet = view.findViewById(R.id.cerrar)
        val lunes = view.findViewById<TextView>(R.id.horarioLunes)
        val martes = view.findViewById<TextView>(R.id.horarioMartes)
        val miercoles = view.findViewById<TextView>(R.id.horarioMiercoles)
        val jueves = view.findViewById<TextView>(R.id.horarioJueves)
        val viernes = view.findViewById<TextView>(R.id.horarioViernes)
        val sabado = view.findViewById<TextView>(R.id.horarioSabado)
        val domingo = view.findViewById<TextView>(R.id.horarioDomingo)

        val cargandoDatos = view.findViewById<LinearLayout>(R.id.linealCargandoHorario)
        val linalFechas = view.findViewById<LinearLayout>(R.id.linalFechas)

        // Asegúrate de que solo cargandoDatos esté visible al inicio
        cargandoDatos.visibility = View.VISIBLE
        linalFechas.visibility = View.GONE

        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }

        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("horario")
        db.get()
            .addOnSuccessListener { res ->
                val diasDeLaSemana = mutableMapOf<String, Map<String, Any>>()

                for (document in res) {
                    val dia = document.id
                    val datos = document.data
                    diasDeLaSemana[dia] = datos
                }

                for ((dia, datos) in diasDeLaSemana) {
                    val apertura = datos["h_apertura"] as? String ?: "00:00"
                    val cierre = datos["h_cierre"] as? String ?: "00:00"
                    val cerrado = datos["cerrado"] as? Boolean ?: false

                    when (dia) {
                        "lunes" -> lunes.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        "martes" -> martes.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        "miercoles" -> miercoles.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        "jueves" -> jueves.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        "viernes" -> viernes.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        "sabado" -> sabado.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        "domingo" -> domingo.text = if (!cerrado) "$apertura AM - $cierre PM" else "Cerrado"
                        else -> println("Día inválido")
                    }
                }

                // Oculta cargandoDatos y muestra linalFechas
                cargandoDatos.visibility = View.GONE
                linalFechas.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                cargandoDatos.visibility = View.GONE
                // Podrías mostrar un mensaje de error aquí si es necesario
            }

        dialog.setContentView(view)
    }


    private fun dejarDeSeguir(idTienda: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
            .document(idTienda)
        db.collection(Variables.collection_seguidores_tiendas).document(firebaseAuth.uid.toString())
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Dejaste de seguir correctamente desde Firestore",
                    Toast.LENGTH_SHORT
                ).show()
                db.get().addOnSuccessListener { res ->
                    if (res.exists()) {
                        val data = res.data
                        val seguidores = data?.get("seguidores") as? String
                        val seguidoresInt = seguidores?.toInt() ?: 0
                        val total = (seguidoresInt - 1).toString()
                        binding.segidores.text = "$total seguidores"
                        val hashMap = hashMapOf<String, Any>("seguidores" to total)
                        db.update(hashMap)
                            .addOnSuccessListener {
                                Log.e("dejar_seguir", "Descontado exitosamente")
                                binding.dejarDeSeguirOSeguir.text = "Seguir"
                            }
                            .addOnFailureListener {
                                Log.e("Error", "Error al dejar de seguir en Firestore")
                            }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("Error", "Error al dejar de seguir en Firestore")
            }
    }

    private fun seguir(idTienda: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
            .document(idTienda)
        val hashMap = hashMapOf<String, Any>(
            "id" to firebaseAuth.uid.toString(),
            "notificaciones" to false
        )
        db.collection(Variables.collection_seguidores_tiendas).document(firebaseAuth.uid.toString())
            .set(hashMap, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Seguido correctamente", Toast.LENGTH_SHORT).show()
                db.get().addOnSuccessListener { res ->
                    if (res.exists()) {
                        val data = res.data
                        val seguidores = data?.get("seguidores") as? String
                        val token = data?.get("token") as? String ?: ""
                        val seguidoresInt = seguidores?.toInt() ?: 0
                        val total = (seguidoresInt + 1).toString()
                        binding.segidores.text = "$total seguidores"
                        val hashMap = hashMapOf<String, Any>("seguidores" to total)
                        db.update(hashMap)
                            .addOnSuccessListener {
                                Log.e("seguir", "Siguiendo exitosamente")
                                binding.dejarDeSeguirOSeguir.text = "Siguiendo"
                                val enviar_notificaciones = NotificacionRS()
                                GlobalScope.launch {
                                    enviar_notificaciones.sendNotification_con_parametros(
                                        "IDTienda",
                                        idTienda,
                                        "INICIO",
                                        this@VistaTienda,
                                        token,
                                        "Nuevo seguidor TIENDAS GEINZ ",
                                        "Tienes un nuevo seguidor en tu pefil de tienda"
                                    )
                                    println("pasamos los datos $idTienda $token")
                                }

                            }
                            .addOnFailureListener {
                                Log.e("Error", "Error al seguir en Firestore")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", "Error al seguir a la tienda $e")
            }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun verTodaLasPublicaiones(idTienda: String) {
        val vistaPromocionesSugeridas =
            Intent(this, TodasPromocionesSugeridasFiltrado::class.java).apply {
                putExtra("idTienda", idTienda)
            }
        Log.d("prmociones_dadas", "el id de la tienda $idTienda")
        startActivity(vistaPromocionesSugeridas)
    }

    private fun obtenerDatosTiendaFirestore(id: String) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(id)
        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val categoria = data?.get("categoria") as? String
                    val estrellas = data?.get("estrellas") as? String
                    val nombre = data?.get("nombre") as? String
                    val seguidores = data?.get("seguidores") as? String
                    val descripcion = data?.get("descripcion") as? String

                    binding.segidores.text = "${seguidores} seguidores"
                    binding.nombreEmpresa.text = nombre
                    binding.categoriaEmpresa.text = categoria
                    binding.descripcion.text = descripcion
                    binding.estrellas.text = estrellas

                    constantestextos_general.extender_acortar_texto(
                        binding.descripcion,
                        binding.tvReadMore
                    )

                }
            }
    }

    private fun verificarSiSigue(id: String, btn: Button) {
        val db =
            FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas).document(id)
                .collection(Variables.collection_seguidores_tiendas)
                .document(firebaseAuth.uid.toString())

        db.get().addOnSuccessListener { res ->
            val isFollowing = res.exists() && (res.getString("id") == firebaseAuth.uid.toString())

            btn.text = if (isFollowing) {
                binding.notificaciones.isVisible = true
                "Siguiendo"
            } else {
                binding.notificaciones.isVisible = false
                "Seguir"

            }

        }.addOnFailureListener { e ->
            Log.e("Error", "Error al obtener los datos: ${e.message}")
        }
    }

    private fun popupMenu(imTienda: String, idTienda: String) {
        val popup = PopupMenu(this, binding.Categorias)
        popup.menu.add(Menu.NONE, 1, 1, "Agregar Reseñas")
        popup.menu.add(Menu.NONE, 2, 2, "Compartir Perfil")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                val vista = Intent(this, Agregar_Ver_Review::class.java)
                vista.putExtra("idTienda", idTienda)
                startActivity(vista)
            } else if (itemID == 2) {
                createAndShareDynamicLink(imTienda, idTienda, this)
            }


            return@setOnMenuItemClickListener true
        }
    }

    fun createAndShareDynamicLink(img: String, id: String, context: Context) {
        Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("https://geinzapp.page.link/?idTienda=${id}")
            domainUriPrefix = "https://geinzapp.page.link"
            androidParameters("com.geinzz.geinzwork") {
                minimumVersion = 125
            }
            iosParameters("com.geinzz.ios") {
                appStoreId = "123456789"
                minimumVersion = "1.0.1"
            }
            googleAnalyticsParameters {
                source = "orkut"
                medium = "social"
                campaign = "geinzz-promo"
            }
            itunesConnectAnalyticsParameters {
                providerToken = "123456"
                campaignToken = "geinzz-promo"
            }
            socialMetaTagParameters {
                title = "Visita Mi Perfil de Tienda"
                description = "Echa un Vistazo a Nuestros Productos y Ofertas"
                imageUrl = Uri.parse(img)
            }
        }.addOnSuccessListener { shortDynamicLink ->
            val shortLink = shortDynamicLink.shortLink
            val invitationLink = shortLink.toString()

            // Comparte el enlace usando una intención
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, invitationLink)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        }.addOnFailureListener {
            println("Hubo un error con los links dinámicos: $it")
        }
    }

    private fun vermasNoticiasOfertas(idTienda: String) {
        val vistaPromocionesSugeridas = Intent(this, NoticiasYofertasFiltrado::class.java)
        vistaPromocionesSugeridas.putExtra("idTienda", idTienda)
        startActivity(vistaPromocionesSugeridas)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmacionTiendaReserva(idTienda: String, context: Context) {
        val db = FirebaseFirestore.getInstance()
        val tiendaRef = db.collection("Tiendas").document(idTienda)

        tiendaRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val servicios = documentSnapshot.getBoolean("reserva") ?: false
                    val noticiasOfertas = documentSnapshot.getBoolean("NoticiasOfertas") ?: false
                    val promocionesSugeridas =
                        documentSnapshot.getBoolean("PomocionesSugeridas") ?: false
                    val productos = documentSnapshot.getBoolean("Productos") ?: false

                    constantesVistaTiendas.obtenerImgTiendaFirestore(
                        idTienda,
                        binding.imgPerfilTienda,
                        binding.imgPortada,
                        this
                    )

                    binding.ServiciosLineal.isVisible = false
                    binding.NoticiasOfertasLineal.isVisible = false
                    binding.promocionesLineal.isVisible = false
                    binding.productosLineal.isVisible = false


                    if (servicios) {
                        binding.contenidoTienda.isVisible = true
                        binding.conteinerImgTienda.isVisible = true
                        binding.ServiciosLineal.isVisible = true
                        constantesVistaTiendas.obtenerServicios(
                            idTienda,
                            binding.recicleServicios,
                            this
                        )
                    }

                    if (noticiasOfertas) {
                        binding.contenidoTienda.isVisible = true
                        binding.conteinerImgTienda.isVisible = true
                        binding.NoticiasOfertasLineal.isVisible = true
                        constantesVistaTiendas.obtenerimgNoticias(
                            idTienda,
                            listaimagenes,
                            binding.recicleNoticiasYOfertas,
                            this
                        )
                    }

                    if (promocionesSugeridas) {
                        binding.contenidoTienda.isVisible = true
                        binding.conteinerImgTienda.isVisible = true
                        binding.promocionesLineal.isVisible = true
                        constantesVistaTiendas.obtenerPromoSugeridas(
                            "mediano",
                            listaPromociones,
                            idTienda,
                            binding.recicelPromosSugeridas,
                            this,
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false),
                            false
                        )
                    }

                    if (productos) {
                        binding.contenidoTienda.isVisible = true
                        binding.conteinerImgTienda.isVisible = true
                        binding.productosLineal.isVisible = true
                        constantesVistaTiendas.ArticulosPrimarios(
                            idTienda,
                            listaArticulos,
                            binding.ProductosTienda,
                            this
                        )
                    }

                } else {
                    Toast.makeText(context, "La tienda no fue encontrada", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores de consulta
                println("Error al obtener datos de Firestore: $e")
                Toast.makeText(
                    context,
                    "Error al obtener datos de Firestore: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerHorarioDeHoy(
        storeId: String,
        callback: (LocalTime, LocalTime, cerrado: Boolean) -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance()
        val hoy = DayOfWeek.from(
            Calendar.getInstance().time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        ).name.lowercase(
            Locale.ROOT
        )

        val diasEnInglesAEs = when (hoy) {
            "monday" -> "lunes"
            "tuesday" -> "martes"
            "wednesday" -> "miercoles"
            "thursday" -> "jueves"
            "friday" -> "viernes"
            "saturday" -> "sabado"
            "sunday" -> "domingo"

            else -> {
                "lunes"
            }
        }
        Log.d("hoy", "el dia de hoy es $diasEnInglesAEs")


        val horarioRef =
            db.collection("Tiendas").document(storeId).collection("horario")
                .document(diasEnInglesAEs.toString())

        horarioRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val aperturaStr = document.getString("h_apertura") ?: "00:00"
                val cierreStr = document.getString("h_cierre") ?: "00:00"
                val cerrado = document.getBoolean("cerrado") ?: false
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val apertura = LocalTime.parse(aperturaStr, formatter)
                val cierre = LocalTime.parse(cierreStr, formatter)

                callback(apertura, cierre, cerrado)
                Log.d("hoy", "la apertura es $apertura y el cierre es $cierre")
                binding.amTienda.text = apertura.toString()
                binding.pmTienda.text = cierre.toString()
            } else {
                println("No se encontró el documento para hoy")
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun verificarTienda(storeId: String) {
        obtenerHorarioDeHoy(storeId) { apertura, cierre, cerrado ->

            val ahora = LocalTime.now()
            Log.d(
                "hoy",
                "comparamos la $ahora y la hora abierta $apertura y cierre $cierre y cerrado es $cerrado"
            )
            if (!cerrado) {
                binding.acvidad.setBackgroundResource(R.drawable.round_desactivado)
                val abierta = if (apertura.isBefore(cierre)) {
                    ahora.isAfter(apertura) && ahora.isBefore(cierre)
                } else {
                    ahora.isAfter(apertura) || ahora.isBefore(cierre)
                }

                if (abierta) {
                    binding.cerradoLayout.isVisible = false
                    binding.linealContenidoTienda.isVisible = true
                    Log.d("hoy", "La tienda está abierta")
                    binding.acvidad.setBackgroundResource(R.drawable.round_activo)

                } else if (!abierta) {
                    binding.cerradoLayout.isVisible = true
                    binding.linealContenidoTienda.isVisible = false
                    binding.acvidad.setBackgroundResource(R.drawable.round_desactivado)
                    Log.d("hoy", "La tienda está cerrada")
                }
            } else {
                binding.cerradoLayout.isVisible = true
                binding.linealContenidoTienda.isVisible = false
                Log.d("hoy", "La tienda está abierta pero la tienda la cerro la cerro")
            }

        }
    }


}


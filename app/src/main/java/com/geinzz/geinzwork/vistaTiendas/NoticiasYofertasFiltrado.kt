package com.geinzz.geinzwork.vistaTiendas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adaptadorAnuncios
import com.geinzz.geinzwork.databinding.ActivityNoticiasYofertasFiltradoBinding
import com.geinzz.geinzwork.model.dataClassAnuncios
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FirebaseFirestore

class NoticiasYofertasFiltrado : AppCompatActivity() {
    private lateinit var binding: ActivityNoticiasYofertasFiltradoBinding
    val listaprincipal = mutableListOf<dataClassAnuncios>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasYofertasFiltradoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var zoonIn = AnimationUtils.loadAnimation(this, com.geinzz.geinzwork.R.anim.zoom_in)
        val zoomout = AnimationUtils.loadAnimation(this, com.geinzz.geinzwork.R.anim.zoom_uot)
        val idTienda = intent.getStringExtra("idTienda").toString()
        obtenerNoticias(idTienda, zoomout, zoonIn)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerNoticias(
        idTienda: String,
        zoomout: Animation?,
        zoomouts: Animation?,
    ) {
        val Firestore = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("noticias")
        Firestore.get()
            .addOnSuccessListener { res ->
                for (datos in res) {
                    val data = datos.data
                    val id = data?.get("id") as? String ?: ""
                    val titulo = data?.get("titulo") as? String ?: ""
                    val contenido = data?.get("contenido") as? String ?: ""
                    val fecha = data?.get("fecha") as? String ?: ""
                    val imagen = data?.get("imagenUrl") as? String ?: ""
                    val estado = data?.get("estado") as? String ?: ""
                    val numeroTelf = data?.get("numero") as? String ?: ""
                    val mensaje = data?.get("whatsappmsj") as? String ?: ""
                    val fechaVencimiento = data?.get("vencimineto") as? String ?: ""
                    val estadoVencimineto = data?.get("estado") as? String ?: ""
                    val categoria = data?.get("Categoria") as? String ?: ""
                    val TipoPromo = data?.get("tipoPromo") as? String ?: ""
                    val idTiendaProp = data?.get("idTiendaProp") as? String ?: ""
                    val localidad = data?.get("localidad") as? String ?: ""
                    val propietario = data?.get("propietario") as? String ?: ""
                    val efectivo = data?.get("efectivo") as? Boolean ?: false
                    val yape = data?.get("yape") as? Boolean ?: false
                    val tipT = datos?.getBoolean("tipo_T") ?: false
                    val precio = data?.get("price") as? String ?: ""
                    val listener = data?.get("listener") as? Boolean ?: false
                    val descuento_boolean = data?.get("descuento_boolean") as? Boolean ?: false
                    val descuento = data?.get("descuento") as? String ?: ""
                    val porcentajeDescuento = data?.get("porcentajeDescuento") as? Number ?: 0
                    println("pasamos el contenido " + contenido)
                    val anuncio = dataClassAnuncios(
                        propietario,
                        contenido,
                        fecha,
                        titulo,
                        imagen,
                        estado,
                        id,
                        numeroTelf,
                        mensaje,
                        fechaVencimiento,
                        estadoVencimineto,
                        categoria,
                        TipoPromo,
                        idTiendaProp, localidad, efectivo, yape, tipT, listener,descuento_boolean,descuento,porcentajeDescuento
                    )
                    listaprincipal.add(anuncio)
                }
                inicializarReciles(
                    "tiendas",
                    listaprincipal,
                    binding.recicleNoticiasOfertas,
                    this,
                    zoomout,
                    zoomouts
                )
            }
    }

    fun inicializarReciles(
        tipo: String,
        listaAnuncios: MutableList<dataClassAnuncios>,
        recicleNoticias: RecyclerView,
        context: Context, zoomout: Animation?, zoomouts: Animation?,
    ) {
        val recicle = recicleNoticias
        recicle.layoutManager = LinearLayoutManager(context)
        recicle.adapter = adaptadorAnuncios(
            false,
            tipo,
            zoomout,
            zoomouts,
            listaAnuncios,
            context,
            { dataClassAnuncios -> createAndShareDynamicLink(dataClassAnuncios, context) }
        )
    }


    fun createAndShareDynamicLink(dataClassAnuncios: dataClassAnuncios, context: Context) {
        Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("https://geinzapp.page.link/?id=${dataClassAnuncios.id}")
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
                campaign = "example-promo"
            }
            itunesConnectAnalyticsParameters {
                providerToken = "123456"
                campaignToken = "example-promo"
            }
            socialMetaTagParameters {
                title = dataClassAnuncios.propietario.toString()
                description = dataClassAnuncios.contenido.toString()
                imageUrl = Uri.parse(dataClassAnuncios.img.toString())
            }
        }.addOnSuccessListener { shortDynamicLink ->
            val shortLink = shortDynamicLink.shortLink
            val invitationLink = shortLink.toString()


            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, invitationLink)
                type = "text/plain"
            }


            context.startActivity(Intent.createChooser(sendIntent, null))
        }.addOnFailureListener { exception ->
            Log.e("DynamicLinkError", "Hubo un error con los links dinámicos", exception)
        }
    }

}
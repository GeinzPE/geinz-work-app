package com.geinzz.geinzwork.vistaTrabajador

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapter_item_mas_promo_servicios_art
import com.geinzz.geinzwork.utils.constantes.constantes.constantesPublicidad
import com.geinzz.geinzwork.databinding.ActivityPromocionesvistaBinding
import com.geinzz.geinzwork.databinding.BottomSheetReservasBinding
import com.geinzz.geinzwork.model.dataclass_mas_art_promo_servicio
import com.geinzz.geinzwork.model.dataclassradiobtn
import com.geinzz.geinzwork.vistaTiendas.VistaTienda
import com.geinzz.geinzwork.vistaTiendas.constantesVistaTiendas
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class promocionesvista : AppCompatActivity() {
    private lateinit var binding: ActivityPromocionesvistaBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var bindingBottomShet: BottomSheetReservasBinding
    private val lista = mutableListOf<dataclassradiobtn>()
    private val listaServicios = mutableListOf<dataclass_mas_art_promo_servicio>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromocionesvistaBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val idTienda = intent.getStringExtra(Variables.idTienda).toString()
        val idProductoClikado = intent.getStringExtra(Variables.idProductoClikado).toString()
        binding.bakPresert.setOnClickListener {
            onBackPressed()
        }
        binding.descripcionPromos.comprar.text="Adquirir promo"
        constantesVistaTiendas.setearDatosTienda(
            idTienda,
            this,
            binding.layoutPerfilTienda.imgPerfilUser,
            binding.layoutPerfilTienda.nombreTienda,
            binding.layoutPerfilTienda.sloganTienda,
            binding.layoutPerfilTienda.horarioAtencion
        )
        constantesVistaTiendas.setearDatosTienda(
            idTienda,
            this,
            binding.layoutPerfilTienda.imgPerfilUser,
            binding.layoutPerfilTienda.nombreTienda,
            binding.layoutPerfilTienda.sloganTienda,
            binding.layoutPerfilTienda.horarioAtencion
        )
        binding.layoutPerfilTienda.InfoTienda.setOnClickListener {
            val vista = Intent(this, VistaTienda::class.java)
            vista.putExtra(Variables.idTienda, idTienda)
            startActivity(vista)
            finish()
        }
        setearcampos(idProductoClikado, idTienda)
        binding.descripcionPromos.reservar.setOnClickListener {
            manejarAccionPromocion("Reserva de promociones", "reservaPromociones")
        }

        binding.descripcionPromos.comprar.setOnClickListener {
            manejarAccionPromocion("Compra de promociones", "compraPromociones")
        }
        obtenerMasServicios(idTienda)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun manejarAccionPromocion(titulo: String, tipoAccion: String) {
        val idTienda = intent.getStringExtra(Variables.idTienda).toString()
        val idProductoClikado = intent.getStringExtra(Variables.idProductoClikado).toString()
        if (firebaseAuth.currentUser == null) {
            dialog = BottomSheetDialog(this)
            constantesPublicidad.CreacionCuentaBottom_shett(this, dialog)
            dialog.show()
            Toast.makeText(this, "Necesitas registrarte para $tipoAccion", Toast.LENGTH_SHORT).show()
        } else {
            val dialog = BottomSheetDialog(this)
            constantes_bottomShet_fourdItem.bottomSheetReservaProducto(
                titulo,
                tipoAccion,
                dialog,
                idTienda,
                idProductoClikado,
                this@promocionesvista,
                binding.layoutPerfilTienda.nombreTienda,
                firebaseAuth.uid.toString(),
                lista
            )
            dialog.show()
        }
    }

    private fun setearcampos(idProductoClikado: String, idTienda: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
            .document(idTienda).collection("promociones").document(idProductoClikado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val contenido = data?.get("Contenido") as? String ?: ""
                val adquirir = data?.get("adquirir") as? Boolean ?: false
                val efectivo = data?.get("efectivo") as? Boolean ?: false
                val id = data?.get("id") as? String ?: ""
                val imagenUrl = data?.get("imagenUrl") as? String ?: ""
                val plin = data?.get("plin") as? Boolean ?: false
                val reserva = data?.get("reserva") as? Boolean ?: false
                val titulo = data?.get("titulo") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                val stok = data?.get("stok") as? String ?: ""
                val vencimiento = data?.get("vencimiento") as? String ?: ""
                val yape = data?.get("yape") as? Boolean ?: false
                val lista = mutableListOf<CarouselItem>()
                lista.add(CarouselItem(imagenUrl))
                binding.imgPrincipal.addData(lista)
                binding.descripcionPromos.contenido.text = contenido
                binding.descripcionPromos.titulo.text = titulo
                binding.descripcionPromos.precio.text = precio
                binding.descripcionPromos.cantidadDisponible.text = stok
                if (reserva && adquirir) {
                    binding.descripcionPromos.reservar.isVisible = true
                    binding.descripcionPromos.comprar.isVisible = true
                } else if (reserva) {
                    binding.descripcionPromos.reservar.isVisible = true
                    binding.descripcionPromos.comprar.isVisible = false
                } else if (adquirir) {
                    binding.descripcionPromos.comprar.isVisible = true
                    binding.descripcionPromos.reservar.isVisible = false
                } else {
                    binding.descripcionPromos.comprar.isVisible = false
                    binding.descripcionPromos.reservar.isVisible = false
                }

                binding.descripcionPromos.contenido.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        binding.descripcionPromos.contenido.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        if ( binding.descripcionPromos.contenido.lineCount > 2) {
                            binding.descripcionPromos.tvReadMore.isVisible = true
                            println("el texo es lagor")
                        }
                    }
                })

                binding.descripcionPromos.tvReadMore.setOnClickListener {
                    if ( binding.descripcionPromos.tvReadMore.text == "Leer más") {
                        binding.descripcionPromos.contenido.maxLines = Integer.MAX_VALUE
                        binding.descripcionPromos.tvReadMore.text = "Leer menos"
                    } else {
                        binding.descripcionPromos.contenido.maxLines = 3
                        binding.descripcionPromos.tvReadMore.text = "Leer más"
                    }
                }
            }
        }
    }

    private fun obtenerMasServicios(idTienda: String) {
        listaServicios.clear()
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas).document(idTienda)
            .collection("promociones")

        db.get().addOnSuccessListener { res ->
            val tempList = mutableListOf<dataclass_mas_art_promo_servicio>()

            for (datos in res) {
                val data = datos.data
                val img = data?.get("imagenUrl") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val descripcion = data?.get("Contenido") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""

                val dataclass = dataclass_mas_art_promo_servicio(id, idTienda, img, titulo, descripcion, precio)
                tempList.add(dataclass)
            }

            tempList.shuffle()

            val cantidadAMostrar = 5
            listaServicios.addAll(tempList.take(cantidadAMostrar))

            activarRecicle(listaServicios, binding.recicleItemMas, this)
        }
            .addOnFailureListener { e ->
                android.util.Log.d("error", "error al obtener los datos: ${e.message}")
            }
    }


    private fun activarRecicle(
        listaServicios: MutableList<dataclass_mas_art_promo_servicio>,
        recyclerContainer: RecyclerView, context: Context,
    ) {
        val adaptador = adapter_item_mas_promo_servicios_art(
            listaServicios, "promocionesvista"
        )
        recyclerContainer.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = adaptador
        }
    }

}
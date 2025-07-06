package com.geinzz.geinzwork.adapterViewholder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.fragmentos.img_completa.FullscreenImageDialog
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes2
import com.geinzz.geinzwork.constantesGeneral.constantesNoticias
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.databinding.ItemAnunciosBinding
import com.geinzz.geinzwork.dataclass.dataClassAnuncios
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class adaptadorAnuncios(
    private val finihs: Boolean,
    private val tipo: String,
    private val zoomin: Animation?,
    private val zoomOut: Animation?,
    private val listaAnuncios: MutableList<dataClassAnuncios>,
    private val context: Context,
    private val compartir: (dataClassAnuncios) -> Unit,
) : RecyclerView.Adapter<adaptadorAnuncios.viewHolderAnuncios>() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderAnuncios {
        val binding =
            ItemAnunciosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolderAnuncios(binding)
    }

    override fun getItemCount(): Int {
        return listaAnuncios.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: viewHolderAnuncios, position: Int) {
        val item = listaAnuncios[position]
        holder.render(tipo, item, context, compartir)
    }

    inner class viewHolderAnuncios(private val binding: ItemAnunciosBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val contenido = binding.contenidotext
        val imgAnuncio = binding.imganeAnuncio
        val propietario = binding.propietario
        val vencetexto = binding.venceTexto
        val tvReadMore = binding.tvReadMore
        val Element = binding.lineaPadreListener
        val Guardar = binding.save
        val like_animation = binding.likeAnimation
        val like_corazon = binding.corazon
        val compartirbtn = binding.Compartir
        val contadorLike = binding.contadorLike
        val saveImageview = binding.saveImageview
        var likeAniation = false

        @SuppressLint("ClickableViewAccessibility")
        @RequiresApi(Build.VERSION_CODES.O)
        fun render(
            tipo: String,
            dataClassAnuncios: dataClassAnuncios, context: Context,
            compartir: (dataClassAnuncios) -> Unit,
        ) {
            if (dataClassAnuncios.descuentoBoolean == true) {
                binding.descuentoPorcentaje.isVisible = true
                binding.descuentoPorcentaje.text = "- ${dataClassAnuncios.porcentajeDescuento}% OFF"
            }

            firebaseAuth = FirebaseAuth.getInstance()
            constantesNoticias.verificarLike(
                dataClassAnuncios,
                like_corazon,
                context,
                like_animation,
                zoomin,
                contadorLike
            )

            imgAnuncio.setOnClickListener {
                    val dialog = FullscreenImageDialog( dataClassAnuncios.img.toString()) //
                    dialog.show((itemView.context as AppCompatActivity).supportFragmentManager, "fullscreenImage")
            }

            vencetexto.text = "Vence :${dataClassAnuncios.fechaVencimiento.toString()}"
            Element.setOnClickListener {
                AccederPromo(finihs, tipo, dataClassAnuncios, context)
            }
            contenido.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    contenido.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (contenido.lineCount >= 2) {
                        tvReadMore.isVisible = true
                        println("el texo es lagor")
                    }
                }
            })

            tvReadMore.setOnClickListener {
                if (tvReadMore.text == Variables.más) {
                    contenido.maxLines = Integer.MAX_VALUE
                    tvReadMore.text = Variables.menos
                } else {
                    contenido.maxLines = 2
                    tvReadMore.text = Variables.más
                }
            }
            Glide.with(itemView.context)
                .load(dataClassAnuncios.img)
                .placeholder(R.drawable.cargando_img_geinz_500)
                .error(R.drawable.cargando_img_geinz_500)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        val bitmap = (resource as BitmapDrawable).bitmap
                        val isVertical = bitmap.height > bitmap.width

                        val layoutParamsAnim =
                            like_animation.layoutParams as RelativeLayout.LayoutParams
                        val imgHeight = imgAnuncio.height

                        // Calcula el tamaño de like_animation en relación con la altura de imgAnuncio
                        val likeSize = (imgHeight * 0.5).toInt()

                        // Establece el tamaño máximo de like_animation
                        val maxSize = 300.dpToPx() // Reemplaza con tu tamaño máximo deseado

                        val finalLikeSize = if (likeSize > maxSize) maxSize else likeSize

                        layoutParamsAnim.width = finalLikeSize
                        layoutParamsAnim.height = finalLikeSize

                        like_animation.layoutParams = layoutParamsAnim
                        imgAnuncio.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
            contenido.text = dataClassAnuncios.contenido
            constantesNoticias.obtenerLikesNoticias(
                dataClassAnuncios,
                contadorLike,
                binding.linealMeGusta,
                binding.cargaLike,
                binding.contanierActividades
            )

            obtenerGuardados(firebaseAuth.uid.toString(), context, dataClassAnuncios)

            saveImageview.setOnClickListener {
                if (firebaseAuth.currentUser == null) {
                    dialog = BottomSheetDialog(itemView.context)
                    constantesPublicidad.CreacionCuentaBottom_shett(
                        itemView.context,
                        dialog
                    )
                    dialog.show()
                } else {
                    val idUser = firebaseAuth.uid.toString()
                    val db2 = FirebaseFirestore.getInstance()
                        .collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB)
                        .collection(Variables.trabajadoresDB)
                        .document(idUser)
                        .collection("guardados").document("guardados").collection("noticias")


                    db2.get().addOnSuccessListener { res ->
                        var isSaved = false
                        for (document in res) {
                            if (document != null && document.exists()) {
                                val fields = document.data
                                if (fields != null && fields.containsKey(dataClassAnuncios.id)) {
                                    isSaved = true
                                    break
                                }
                            }
                        }

                        likeAniation = constantesNoticias.guardar(
                            Guardar,
                            R.raw.save_animation,
                            isSaved,
                            idUser,
                            dataClassAnuncios,
                            context, saveImageview
                        )
                    }.addOnFailureListener { exception ->
                        println("Error al obtener datos: ${exception.message}")
                    }
                }
            }

            compartirbtn.setOnClickListener {
                compartir(dataClassAnuncios)
                val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB)
                    .document(dataClassAnuncios.id.toString())
                constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.compartido)

            }
            val spannableString =
                SpannableString("${dataClassAnuncios.propietario} ${dataClassAnuncios.titulo}")

            val boldSpan = StyleSpan(Typeface.BOLD)
            val startIndex = 0
            val endIndex = dataClassAnuncios.propietario?.length ?: 0
            spannableString.setSpan(
                boldSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            propietario.text = spannableString
        }


        fun Int.dpToPx(): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (this * scale + 0.5f).toInt()
        }

        private fun obtenerGuardados(
            idUser: String,
            context: Context,
            dataClassAnuncios: dataClassAnuncios,
        ) {

            val db2 = FirebaseFirestore.getInstance()
                .collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB)
                .collection(Variables.trabajadoresDB)
                .document(idUser)
                .collection("guardados").document("guardados").collection("noticias")


            db2.get().addOnSuccessListener { res ->
                var isSaved = false

                for (document in res) {
                    if (document != null && document.exists()) {
                        val fields = document.data
                        if (fields != null && fields.containsKey(dataClassAnuncios.id)) {
                            isSaved = true
                            break
                        }
                    }
                }


                constantesNoticias.actualizarAnimacion(
                    Guardar,
                    R.raw.save_animation,
                    isSaved,
                    saveImageview
                )
            }.addOnFailureListener { exception ->
                println("Error al obtener datos: ${exception.message}")
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun AccederPromo(
            finihs: Boolean,
            tipo: String,
            dataClassAnuncios: dataClassAnuncios,
            context: Context
        ) {
            val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB)
                .document(dataClassAnuncios.id.toString())
            if (dataClassAnuncios.listener == false) {
                return
            } else {
                constantesNoticias.firebaseAuth = FirebaseAuth.getInstance()
                Toast.makeText(
                    context,
                    context.getString(R.string.cargandoContenido),
                    Toast.LENGTH_SHORT
                ).show()

                if (constantes2.firebaseAuth.uid == null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.lasPromocionesDisponiblesUsuarios),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (dataClassAnuncios.numero.toString().isNotEmpty()) {

                        val intent = Intent(context, ver_detalles_Promociones::class.java)
                        constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.click)
                        intent.putExtra(Variables.idAnuncio, dataClassAnuncios.id)
                        intent.putExtra(Variables.TipoPromo, dataClassAnuncios.TipoPromo)
                        intent.putExtra(Variables.idTienda, dataClassAnuncios.idProp)
                        intent.putExtra(Variables.entrada, tipo)

                        context.startActivity(intent)

                        if (finihs && context is Activity) {
                            context.finish()
                        }
                    }
                }
            }
        }

    }
}
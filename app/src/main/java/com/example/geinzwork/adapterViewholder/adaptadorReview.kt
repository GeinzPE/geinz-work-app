package com.geinzz.geinzwork.adapterViewholder

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.EditarReview
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesImagenes
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.BottomSheetConfigCuentaBinding
import com.geinzz.geinzwork.databinding.BottomSheetReviewReportadoBinding
import com.geinzz.geinzwork.databinding.ItemReviewBinding
import com.geinzz.geinzwork.dataclass.daclassReview
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class adaptadorReview(
    private val listaReview: MutableList<daclassReview>,
    private val idtrabajadorClikeado: String? = null,

    ) : RecyclerView.Adapter<adaptadorReview.viewHolderReview>() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderReview {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolderReview(binding)
    }

    override fun getItemCount(): Int {
        return listaReview.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: viewHolderReview, position: Int) {
        val item = listaReview[position]
        holder.render(item)
    }

    inner class viewHolderReview(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val review = binding.review
        val nombrevw = binding.nombre
        val imgperfil = binding.imgPerfilUser
        val imgpeStart = binding.cantidadStart
        val hora = binding.hora
        val fecha = binding.fecha
        val reportar = binding.reportar

        fun render(daclassReview: daclassReview) {
            review.text = daclassReview.review
            fecha.text = daclassReview.fecha
            hora.text = daclassReview.hora
            val uidActual = FirebaseAuth.getInstance().uid
            firebaseAuth = FirebaseAuth.getInstance()
            if (daclassReview.idUsuarioReview == uidActual) {
                binding.editar.isVisible = true
            } else {
                binding.editar.isVisible = false

            }
            binding.editar.setOnClickListener {
                val intent = Intent(itemView.context, EditarReview::class.java).apply {
                    putExtra("TipoEditado", Variables.CuentaFreelancer)
                    putExtra("idReview", uidActual)
                    putExtra(Variables.iduser, uidActual)
                    putExtra(Variables.idTrabajdor, idtrabajadorClikeado)
                    putExtra(Variables.cantidad, daclassReview.cantidaStarts)
                    putExtra("id_review", daclassReview.id_review)
                    putExtra("review", review.text.toString())
                    putExtra("editado_adaptador", false)
                }
                itemView.context.startActivity(intent)
            }
            reportar.setOnClickListener {
                if (firebaseAuth.currentUser == null) {
                    dialog = BottomSheetDialog(itemView.context)
                    constantesPublicidad.CreacionCuentaBottom_shett(
                        itemView.context,
                        dialog
                    )
                    dialog.show()

                } else {
                    if (daclassReview.idUsuarioReview == uidActual) {
                        Toast.makeText(
                            itemView.context,
                            "No puedes denunciar tu propia review",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        verificar_denunciaExistente(daclassReview.id_review.toString()) { existe ->
                            if (existe) {
                                Toast.makeText(
                                    itemView.context,
                                    "Ya tienes una denuncia en esta reseña",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                dialog = BottomSheetDialog(itemView.context)
                                BottomShett_UsoIndebido(daclassReview)
                                dialog.show()
                            }
                        }
                    }
                }


            }
            setearStarts(daclassReview)
            constantesCarrito.setearDatosUsuarioImgNombre(
                idUSer = daclassReview.idUsuarioReview.toString()
            ) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajadorUSer ->

                val nombreCompleto = "$nombre $apellido"
                binding.nombre.text = nombreCompleto
                println("id del user ${daclassReview.idUsuarioReview.toString()}")
                val placeholder: Drawable? =
                    ContextCompat.getDrawable(itemView.context, R.drawable.img_perfil)

                constatnes_carga_imagenes_general.changer_img(
                    binding.cargarImg,
                    itemView.context,
                    img.toString(),
                    binding.imgPerfilUser,
                    null,
                    "perfil", placeholder
                ) { cargado ->

                }

                constantes_servicios.verificarEstado_vericiacion(
                    binding.iconoVerificado,
                    daclassReview.idUsuarioReview.toString()
                ) { v, plan ->
                    when (plan) {
                        Variables.plaA -> {
                            binding.iconoVerificado.setImageResource(R.drawable.verificado_a)

                        }

                        Variables.planB -> {
                            binding.iconoVerificado.setImageResource(R.drawable.icon_verificado)
                        }

                        Variables.PlanC -> {
                            binding.iconoVerificado.setImageResource(R.drawable.verificado_c)


                        }
                    }

                }


                // Verificación después de setear
                val nombreYaCargado = binding.nombre.text.toString() == nombreCompleto
                val nombreValido =
                    nombre.toString().isNotBlank() && apellido.toString().isNotBlank()
//                val imagenValida = !img.isNullOrBlank() // o validar que no sea un placeholder

                if (nombreYaCargado && nombreValido) {
                    binding.cargaIMGtexto.isVisible = false
                    binding.linealIMgTexto.isVisible = true
                } else {
                    binding.cargaIMGtexto.isVisible = false
                    binding.linealIMgTexto.isVisible = false

                }

                binding.general.setOnClickListener {
                    if (trabajadorUSer.equals("trabajador")) {
                        val vista = Intent(itemView.context, vistaTrabajador::class.java).apply {
                            putExtra(Variables.id, daclassReview.idUsuarioReview)
                            putExtra(Variables.nombreUSer, nombre)
                            putExtra(Variables.nacionalidad, nacionalidad)
                            putExtra(Variables.categoria, categoria)
                            putExtra(Variables.imagenPerfil, img)
                        }
                        itemView.context.startActivity(vista)
                    } else {
                        Toast.makeText(
                            itemView.context,
                            "La cuenta no es cuenta trabajador",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }


            constantestextos_general.textoPrimarioBold(daclassReview, binding.review)

            constantestextos_general.textoPrimarioBold2(daclassReview, binding.tipoTrabajo)


            if (daclassReview.editado == true) {
                binding.editado.isVisible = true
            } else {
                binding.editado.isVisible = false
            }

        }


        fun setearStarts(daclassReview: daclassReview) {
            try {
                val drawableId = when (daclassReview.cantidaStarts) {
                    "1" -> R.drawable.start_one
                    "2" -> R.drawable.start_two
                    "3" -> R.drawable.start_tree
                    "4" -> R.drawable.start_four
                    "5" -> R.drawable.start_five
                    else -> R.drawable.start_one // Puedes tener un recurso predeterminado para casos inválidos
                }

                Glide.with(itemView.context)
                    .load(drawableId)
                    .into(imgpeStart)
            } catch (e: Exception) {
                println(e)
            }
        }

        private fun BottomShett_UsoIndebido(
            item: daclassReview,
        ) {
            val bottomSheet =
                BottomSheetReviewReportadoBinding.inflate(LayoutInflater.from(itemView.context))
            val view = bottomSheet.root
            val tipoIncidencia = bottomSheet.tipoIncidencia

            val opciones = listOf(
                "Comportamiento inapropiado",
                "Contenido ofensivo o falso",
                "Incumplimiento de las políticas de Geinz",
                "Lenguaje abusivo o discriminatorio",
                "Spam o promoción no autorizada",
                "Otro"
            )

            val adapter = ArrayAdapter(
                itemView.context,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )

            tipoIncidencia.setAdapter(adapter)
            val enviar = bottomSheet.btnApply


            enviar.setOnClickListener {
                if (bottomSheet.tipoIncidencia.text.toString().isEmpty()) {
                    bottomSheet.tipoIncidencia.error = "Selecione un tipo de incidencia"
                } else if (bottomSheet.DescripcionDelProblemaED.text.toString().isEmpty()) {
                    bottomSheet.DescripcionDelProblemaED.error = "Ingrese una descripcion"
                } else {
                    val db =
                        FirebaseFirestore.getInstance()
                            .collection("politicas_problemas_verificaciones")
                            .document("denuncia_review").collection("denuncia_review")

                    val hasmap = hashMapOf<String, Any>(
                        "contenido" to item.review.toString(), // contenido de la reseña
                        "cantidad_estrellas" to item.cantidaStarts.toString(), // estrellas dadas
                        "idUsuario_review" to item.idUsuarioReview.toString(), // usuario que escribió la reseña
                        "id_review" to item.id_review.toString(), // ID de la reseña
                        "id_registrado" to firebaseAuth.uid.toString(), // UID del usuario que está reportando
                        "idTrabajador_campo" to (idtrabajadorClikeado ?: ""), // trabajador al que se hizo la reseña
                        "idReporte" to item.id_review.toString(), // este campo se repite, revisar si es necesario
                        "Tipo_reporte" to bottomSheet.tipoIncidencia.text.toString(), // tipo de problema (spam, abuso, etc.)
                        "problema" to bottomSheet.DescripcionDelProblemaED.text.toString(), // descripción del problema
                        "fecha_envio" to binding.fecha.text.toString(), // fecha del reporte
                        "hora_envio" to binding.hora.text.toString(), // hora del reporte
                        "estado" to "enviado" // estado inicial del reporte
                    )
                    db.add(hasmap).addOnSuccessListener {
                        Toast.makeText(
                            itemView.context,
                            "Denuncia enviada exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        constantes_vinculados.encotrar_user(firebaseAuth.uid.toString()) { tipo, colleccion ->
                            if (colleccion != null) {
                                val dbUsuario = colleccion.document(firebaseAuth.uid.toString())
                                    .collection("reporte")
                                    .document("enviados").collection("review")
                                    .document(item.id_review.toString())
                                Toast.makeText(
                                    itemView.context,
                                    "el tipo enviado fue de $tipo",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val hasmap = hashMapOf<String, Any>(
                                    "contenido" to item.review.toString(), // contenido de la reseña
                                    "cantidad_estrellas" to item.cantidaStarts.toString(), // estrellas dadas
                                    "idUsuario_review" to item.idUsuarioReview.toString(), // usuario que escribió la reseña
                                    "id_review" to item.id_review.toString(), // ID de la reseña
                                    "id_registrado" to firebaseAuth.uid.toString(), // UID del usuario que está reportando
                                    "idTrabajador_campo" to (idtrabajadorClikeado ?: ""), // trabajador al que se hizo la reseña
                                    "idReporte" to item.id_review.toString(), // este campo se repite, revisar si es necesario
                                    "Tipo_reporte" to bottomSheet.tipoIncidencia.text.toString(), // tipo de problema (spam, abuso, etc.)
                                    "problema" to bottomSheet.DescripcionDelProblemaED.text.toString(), // descripción del problema
                                    "fecha_envio" to binding.fecha.text.toString(), // fecha del reporte
                                    "hora_envio" to binding.hora.text.toString(), // hora del reporte
                                    "estado" to "enviado" // estado inicial del reporte
                                )
                                dbUsuario.set(
                                    hasmap,
                                    SetOptions.merge()
                                ).addOnSuccessListener { res ->
                                    Toast.makeText(
                                        itemView.context,
                                        "Se agrego al trabjador normal ref",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        itemView.context,
                                        "error al agrege $e",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }
                    }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                itemView.context,
                                "Error al enviar el formulario",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    dialog.dismiss()
                }
            }
            dialog.setContentView(view)

        }

        private fun verificar_denunciaExistente(idclikeado: String, existe: (Boolean) -> Unit) {
            Toast.makeText(itemView.context,"el valor pasasdo fue de $idclikeado",Toast.LENGTH_SHORT).show()
            val firebaseAuth: FirebaseAuth
            firebaseAuth = FirebaseAuth.getInstance()
            constantes_vinculados.encotrar_user(firebaseAuth.uid.toString(), { tipo, collecion ->
                if (collecion != null) {
                    collecion.document(firebaseAuth.uid.toString()).collection("reporte")
                        .document("enviados").collection("review").get()
                        .addOnSuccessListener { res ->
                            var encontrada = false

                            for (datos in res) {
                                val id_review = datos.getString("idReporte") ?: ""
                                val id_registrado = datos.getString("id_registrado") ?: ""

                                if (idclikeado == id_review && id_registrado == firebaseAuth.uid.toString()) {
                                    encontrada = true
                                    break
                                }
                            }

                            existe(encontrada)
                        }.addOnFailureListener { e ->
                            println("Error al obtener datos: ${e.message}")
                            existe(false)
                        }
                }

            })


        }

        private fun verificar_siEsSu_review(id_trabajador: String, existe: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores").document(id_trabajador)
                .collection("review")

            db.get().addOnSuccessListener { res ->
                for (datos in res) {
                    val date = datos.data
                    val iduserREview = date?.get("iduserReview") as? String ?: ""
                    if (iduserREview == firebaseAuth.uid.toString()) {
                        existe(true)
                    } else {
                        existe(false)
                    }


                }
            }.addOnFailureListener { e ->
                Log.e("FirestoreError", "Error obteniendo reviews: $e")
                existe(false)
            }
        }


    }
}
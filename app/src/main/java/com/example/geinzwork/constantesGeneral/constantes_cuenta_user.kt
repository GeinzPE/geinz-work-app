package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.fragmentos.cuenta_config.OnIncludeSeleccionadoListener
import com.example.geinzwork.fragmentos.cuenta_config.cuenta_config
import com.example.geinzwork.vistas_p.onboarding_como_usar_geinz
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.BottomSheetConfigCuentaBinding
import com.geinzz.geinzwork.problemas_soporte_politicas.verificacion_cuenta_trabajador
import com.geinzz.geinzwork.servicios_geinz.serviciosGeinz
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object constantes_cuenta_user {
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    fun bottom_shett_config(
        dialog: BottomSheetDialog,
        context: Context, includeSeleccionadoListener: OnIncludeSeleccionadoListener
    ) {
        val binding = BottomSheetConfigCuentaBinding.inflate(LayoutInflater.from(context))

        val view = binding.root
        bottomSheet = binding.cerrar
        firebaseAuth = FirebaseAuth.getInstance()
        setear_datos_include(
            binding.serviciosGeinz.lineaApartado,
            binding.serviciosGeinz.imgIcono,
            R.drawable.geinz_circular_new,
            "Servicios Geinz",
            binding.serviciosGeinz.textoContenido,
            binding.serviciosGeinz.linealPadre,
            binding.serviciosGeinz.ocultarP1,
            binding.serviciosGeinz.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",
            context, includeSeleccionadoListener
        )

        setear_datos_include(binding.funcionamientoGeinz.lineaApartado,
            binding.funcionamientoGeinz.imgIcono,
            R.drawable.como_funciona_geinz_webp,
            "Funcionamiento de Geinz",
            binding.funcionamientoGeinz.textoContenido,
            binding.funcionamientoGeinz.linealPadre,
            binding.funcionamientoGeinz.ocultarP1,
            binding.funcionamientoGeinz.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",
            context, includeSeleccionadoListener
        )

        setear_datos_include(binding.pagosGeinz.lineaApartado,
            binding.pagosGeinz.imgIcono,
            R.drawable.pagos_geinz_webp,
            "Pagos Geinz",
            binding.pagosGeinz.textoContenido,
            binding.pagosGeinz.linealPadre,
            binding.pagosGeinz.ocultarP1,
            binding.pagosGeinz.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",
            context, includeSeleccionadoListener
        )

        setear_datos_include(binding.cuentaVerifica.lineaApartado,
            binding.cuentaVerifica.imgIcono,
            R.drawable.icon_verificado,
            "Verificar cuenta",
            binding.cuentaVerifica.textoContenido,
            binding.cuentaVerifica.linealPadre,
            binding.cuentaVerifica.ocultarP1,
            binding.cuentaVerifica.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",
            context, includeSeleccionadoListener
        )

//        val cerrar_Seccion = binding.containerCerrarSeccion
//        val container_eliminar_cuenta = binding.containerEliminarCuenta
//        val container_guardados = binding.containerGuardados
//        val container_review = binding.containerReview
//        val container_preview = binding.containerPreview
//        val lineal_verificado = binding.linealVerificado
//        val lineal_direccionEnvios = binding.containerLocalizacion
//        val containerqr_agregar_img = binding.containerqrAgregarImg
//        val panel_publicacion = binding.panelPublicacion
//        val lineaReportes = binding.lineaReportes
//        val lineal_como_funcion_Geinz = binding.linealComoFuncionGeinz
//        val qr_trabajador = binding.qrTrabajador
//        val linealServicios = binding.linealServicios
//        val vinculados = binding.vinculados
//        val accesos_directo = binding.accesoDirecto
//        val historial_venta = binding.historialVenta
//        val historial_compra = binding.historialCompra


//        vinculados.setOnClickListener {
//            val vista = Intent(context, activity_dispositivos_vinculados::class.java)
//            context.startActivity(vista)
//            dialog.dismiss()
//        }
//        when (tipoCuenta.text.toString()) {
//            Variables.Cuenta_Simple -> {
//                container_review.isVisible = false
//                container_preview.isVisible = false
//                lineal_verificado.isVisible = false
////                containerqr_agregar_img.isVisible = false
//                lineaReportes.isVisible = false
//
//                qr_trabajador.isVisible = false
//                accesos_directo.isVisible = false
//            }
//
//            else -> ""
//        }


//        historial_venta.setOnClickListener {
//            context.startActivity(Intent(context, venta_trabajador::class.java))
//            dialog.dismiss()
//        }
//
//        historial_compra.setOnClickListener {
//            context.startActivity(Intent(context, compra_trabajador::class.java))
//            dialog.dismiss()
//        }
//
//        accesos_directo.setOnClickListener {
//            context.startActivity(Intent(context, accesos_directos_geinz_work::class.java))
//            dialog.dismiss()
//        }

        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }
//        lineal_direccionEnvios.setOnClickListener {
//            context.startActivity(Intent(context, direccion_entrega_lat_log::class.java))
//            dialog.dismiss()
//        }
//        lineal_como_funcion_Geinz.setOnClickListener {
//            context.startActivity(Intent(context, onboarding_como_usar_geinz::class.java))
//            dialog.dismiss()
//        }
//        lineaReportes.setOnClickListener {
////            context.startActivity(Intent(context, reportes_users::class.java))
////            dialog.dismiss()
//            var vista = Intent(context, vista_denuncia_reporte::class.java).apply {
//                putExtra(Variables.plan, plan)
//            }
//            context.startActivity(vista)
//            dialog.dismiss()
//        }
//        qr_trabajador.setOnClickListener {
//            context.startActivity(Intent(context, GenerarQR_trabajador::class.java))
//            dialog.dismiss()
//        }
//        lineal_verificado.setOnClickListener {
//            context.startActivity(Intent(context, verificacion_cuenta_trabajador::class.java))
//            dialog.dismiss()
//        }
//        linealServicios.setOnClickListener {
//            context.startActivity(Intent(context, serviciosGeinz::class.java))
//            dialog.dismiss()
//        }
//
//        cerrar_Seccion.setOnClickListener {
//            val alerta = AlertDialog.Builder(context)
//            alerta.setTitle("Cerrar Sesión")
//            alerta.setMessage("¿Está seguro de que desea cerrar sesión?")
//            alerta.setPositiveButton("Sí") { dialog, which ->
//                constantes_vinculados.cerrarSeccion(context, firebaseAuth.uid.toString()) {
//                    firebaseAuth.signOut()
//                    context.startActivity(Intent(context, MainActivity::class.java))
//                    activity?.finishAffinity()
//                }
//            }
//
//
//            alerta.setNegativeButton("No") { dialog, which ->
//                dialog.dismiss()
//            }
//            alerta.show()
//            dialog.dismiss()
//        }
//        container_eliminar_cuenta.setOnClickListener {
//            val builder = AlertDialog.Builder(context)
//            builder.setTitle("Eliminar cuenta")
//            builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
//            builder.setPositiveButton("Sí") { dialogs, which ->
////                eliminarCuenta(tipoCuenta, firebaseAuth.uid.toString(), context, activity)
//            }
//            builder.setNegativeButton("No") { dialogs, which ->
//                dialogs.dismiss()
//            }
//            val dialogs = builder.create()
//            dialogs.show()
//            dialog.dismiss()
//
//        }
//        panel_publicacion.setOnClickListener {
//            firebaseAuth = FirebaseAuth.getInstance()
//            var intent = Intent(context, panel_publicacion_trabajador::class.java).apply {
//                putExtra(Variables.plan, plan)
//            }
//            context.startActivity(intent)
//            dialog.dismiss()
//        }
//        container_preview.setOnClickListener {
//            constantes.showLoadingDialog(
//                context,
//                3000,
//                "Por favor, espere",
//                "Cargando datos..."
//            )
//            mandarDatos(firebaseAuth.uid.toString(), context)
//            dialog.dismiss()
//        }
//        container_guardados.setOnClickListener {
////            val intent = Intent(context, noticias_y_review::class.java)
//            val intent = Intent(context, noticias_trabajadores_guardados::class.java)
//            intent.putExtra(Variables.iduser, firebaseAuth.uid.toString())
//            context.startActivity(intent)
//            dialog.dismiss()
//        }
//        container_review.setOnClickListener {
//            val intent = Intent(context, noticias_y_review::class.java)
//            intent.putExtra(Variables.iduser, firebaseAuth.uid.toString())
//            intent.putExtra(Variables.title, "Tus Reseñas")
//            context.startActivity(intent)
//            dialog.dismiss()
//        }
        dialog.setContentView(view)
    }

    fun setear_datos_include(
        lineal_principal: LinearLayout,
        imageView: ImageView,
        img: Int,
        tituloTexto: String,
        textView: TextView,
        linealPadre: LinearLayout,
        icono_oculatar: ImageView,
        textView_ocultar: TextView,
        texto_setear: String,
        context: Context,
        includeSeleccionadoListener: OnIncludeSeleccionadoListener
    ) {
        var visible = false
        lineal_principal.isVisible=true
        icono_oculatar.setOnClickListener {
            visible = !visible
            textView_ocultar.visibility = if (visible) View.VISIBLE else View.GONE
            icono_oculatar.setImageResource(
                if (visible) R.drawable.ocultar_abajo else R.drawable.ocultar_arriva
            )
        }

        textView_ocultar.text = texto_setear
        Glide.with(context)
            .load(img)
            .into(imageView)

        textView.text = tituloTexto

        linealPadre.setOnClickListener {
            when (tituloTexto) {
                "Servicios Geinz" -> {
                    context.startActivity(Intent(context, serviciosGeinz::class.java))
                }

                "Funcionamiento de Geinz" -> {
                    context.startActivity(Intent(context, onboarding_como_usar_geinz::class.java))
                }

                "Pagos Geinz" -> {}
                "Verificar cuenta" -> {
                    context.startActivity(
                        Intent(
                            context,
                            verificacion_cuenta_trabajador::class.java
                        )
                    )
                }
            }
        }
        linealPadre.setOnLongClickListener {
            val id = when (tituloTexto) {
                "Servicios Geinz" -> "servicios_geinz"
                "Funcionamiento de Geinz" -> "funcionamiento_geinz"
                "Pagos Geinz" -> "pagos_geinz"
                "Verificar cuenta" -> "verificar_cuenta"
                else -> ""
            }

            if (id.isNotEmpty()) {
                includeSeleccionadoListener.onIncludeSeleccionado(id)

            }
            true
        }
    }


    fun eliminarCuenta(
        tipoCuenta: String,
        id: String,
        context: Context,
        activity: Activity
    ) {
        when (tipoCuenta) {
            Variables.Cuenta_Simple -> {
                val db =
                    FirebaseFirestore.getInstance()
                        .collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                        .document(id)
                db.delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Usuario eliminado Correctamente",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(
                            context,
                            "Error al eliminar el Usuario ",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }

            Variables.Cuenta_Trabajador -> {
                val db =
                    FirebaseFirestore.getInstance()
                        .collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.usuarios_db).collection(Variables.usuarios_db)
                        .document(id)
                db.delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Usuario eliminado Correctamente",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(
                            context,
                            "Error al eliminar el Usuario ",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }
        }
        val RealTime =
            FirebaseDatabase.getInstance().getReference(Variables.ReseñasUsuarios).child(id)
        RealTime.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Reseña eliminada ", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->

                Toast.makeText(context, "Error al eliminar la reseña ", Toast.LENGTH_SHORT)
                    .show()
            }

        val dbStorage = FirebaseStorage.getInstance().getReference(id)
        dbStorage.delete()
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Fotos de Trabajo elimiada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al eliminar las fotos ", Toast.LENGTH_SHORT)
                    .show()
            }
        firebaseAuth.signOut()
        context.startActivity(Intent(context, MainActivity::class.java))
        activity.finishAffinity()
    }

    fun mandarDatos(idRegistrado: String, context: Context) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(idRegistrado)
        db.get()
            .addOnSuccessListener { resultado ->
                if (resultado.exists()) {
                    val data = resultado.data
                    val id = data?.get("id") as? String
                    val nombre = data?.get("nombre") as? String
                    val apellido = data?.get("apellido") as? String
                    val c1 = data?.get("caracteristica1") as? String
                    val c2 = data?.get("caracteristica2") as? String
                    val c3 = data?.get("caracteristica3") as? String
                    val categoria = data?.get("categoriaTrabajo") as? String
                    val genero = data?.get("genero") as? String
                    val horaioam = data?.get("horario1") as? String
                    val horaiopm = data?.get("horario2") as? String
                    val tipoT = data?.get("tipoTrabajo") as? String
                    val imagenPerfil = data?.get("imagenPerfil") as? String
                    val nacionalidad = data?.get("nacionalidad") as? String
                    val localidad = data?.get("localidad") as? String
                    val codigo_pais = data?.get("codigo_pais") as? String
                    val numero = data?.get("numero") as? String
                    val estado = data?.get("activado") as? String
                    val EdadUser = data?.get("EdadActual") as? String
                    var vista = Intent(context, vistaTrabajador::class.java)
                    vista.putExtra("id", id)
                    vista.putExtra("nombre", nombre)
                    vista.putExtra("apellido", apellido)
                    vista.putExtra("c1", c1)
                    vista.putExtra("c2", c2)
                    vista.putExtra("c3", c3)
                    vista.putExtra("categoria", categoria)
                    vista.putExtra("genero", genero)
                    vista.putExtra("horaioam", horaioam)
                    vista.putExtra("horaiopm", horaiopm)
                    vista.putExtra("tipoT", tipoT)
                    vista.putExtra("imagenPerfil", imagenPerfil)
                    vista.putExtra("nacionalidad", nacionalidad)
                    vista.putExtra("localidad", localidad)
                    vista.putExtra("codigo_pais", codigo_pais)
                    vista.putExtra("numero", numero)
                    vista.putExtra("estado", estado)
                    vista.putExtra("EdadUser", EdadUser)
                    context.startActivity(vista)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
    }


    fun calcularEdadTrabajador(fechaTrabajador: String, edadUsuario: (String) -> Unit) {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        try {
            val fechaNacimiento = formato.parse(fechaTrabajador)
            val calendarioNacimiento = Calendar.getInstance().apply { time = fechaNacimiento }
            val calendarioActual = Calendar.getInstance()

            var edad =
                calendarioActual.get(Calendar.YEAR) - calendarioNacimiento.get(Calendar.YEAR)


            if (calendarioActual.get(Calendar.DAY_OF_YEAR) < calendarioNacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--
            }

            edadUsuario(edad.toString())
        } catch (e: Exception) {
            edadUsuario("Error en la fecha")
        }
    }


}
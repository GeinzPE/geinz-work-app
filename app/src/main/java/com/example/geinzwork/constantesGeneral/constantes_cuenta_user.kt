package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.fragmentos.panel_publicacion_trabajador
import com.geinzz.geinzwork.FuncionalidadGeinz.comoUsar
import com.geinzz.geinzwork.GenerarQR_trabajador
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.crear_trabajos_realizados
import com.geinzz.geinzwork.databinding.BottomSheetConfigCuentaBinding
import com.geinzz.geinzwork.noticias_y_review
import com.geinzz.geinzwork.problemas_soporte_politicas.reportes_users
import com.geinzz.geinzwork.problemas_soporte_politicas.verificacion_cuenta_trabajador
import com.geinzz.geinzwork.servicios_geinz.serviciosGeinz
import com.geinzz.geinzwork.vistaTiendas.direccion_entrega_lat_log
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
        context: Context,
        activity: Activity,
        tipoCuenta: TextView,
        plan: String
    ) {
        val binding = BottomSheetConfigCuentaBinding.inflate(LayoutInflater.from(context))

        val view = binding.root
        bottomSheet = binding.cerrar
        firebaseAuth = FirebaseAuth.getInstance()
        val cerrar_Seccion = binding.containerCerrarSeccion
        val container_eliminar_cuenta = binding.containerEliminarCuenta
        val container_guardados = binding.containerGuardados
        val container_review = binding.containerReview
        val container_preview = binding.containerPreview
        val lineal_verificado = binding.linealVerificado
        val lineal_Publicacion = binding.linealPublicacion
        val lineal_direccionEnvios = binding.containerLocalizacion
        val containerqr_agregar_img = binding.containerqrAgregarImg
        val panel_publicacion = binding.panelPublicacion
        val lineaReportes = binding.lineaReportes
        val lineal_como_funcion_Geinz = binding.linealComoFuncionGeinz
        val qr_trabajador =binding.qrTrabajador
        val linealServicios = binding.linealServicios
        when (tipoCuenta.text.toString()) {
            Variables.Cuenta_Simple -> {
                container_review.isVisible = false
                container_preview.isVisible = false
                lineal_verificado.isVisible = false
                containerqr_agregar_img.isVisible = false
                lineaReportes.isVisible = false
                lineal_Publicacion.isVisible = false
                qr_trabajador.isVisible = false
            }
            else -> ""
        }
        if (plan.isEmpty() || plan == Variables.plaA) {
            lineal_Publicacion.isVisible = false
        } else if (plan == Variables.planB || plan == Variables.PlanC){
            lineal_Publicacion.isVisible = true
        }
        lineal_Publicacion.setOnClickListener {
            var vista = Intent(context, crear_trabajos_realizados::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            context.startActivity(vista)
            dialog.dismiss()


        }
        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }
        lineal_direccionEnvios.setOnClickListener {
            context.startActivity(Intent(context, direccion_entrega_lat_log::class.java))
            dialog.dismiss()
        }
        lineal_como_funcion_Geinz.setOnClickListener {
            context.startActivity(Intent(context, comoUsar::class.java))
            dialog.dismiss()
        }
        lineaReportes.setOnClickListener {
            context.startActivity(Intent(context, reportes_users::class.java))
            dialog.dismiss()
        }
        qr_trabajador.setOnClickListener {
            context.startActivity(Intent(context, GenerarQR_trabajador::class.java))
            dialog.dismiss()
        }
        lineal_verificado.setOnClickListener {
            context.startActivity(Intent(context, verificacion_cuenta_trabajador::class.java))
            dialog.dismiss()
        }
        linealServicios.setOnClickListener {
            context.startActivity(Intent(context, serviciosGeinz::class.java))
            dialog.dismiss()
        }

        cerrar_Seccion.setOnClickListener {
            val alerta = AlertDialog.Builder(context)
            alerta.setTitle("Cerrar Sesión")
            alerta.setMessage("¿Está seguro de que desea cerrar sesión?")
            alerta.setPositiveButton("Sí") { dialog, which ->
                firebaseAuth.signOut()
                context.startActivity(Intent(context, MainActivity::class.java))
                activity?.finishAffinity()
            }
            alerta.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            alerta.show()
            dialog.dismiss()
        }
        container_eliminar_cuenta.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Eliminar cuenta")
            builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            builder.setPositiveButton("Sí") { dialogs, which ->
                eliminarCuenta(tipoCuenta, firebaseAuth.uid.toString(), context, activity)
            }
            builder.setNegativeButton("No") { dialogs, which ->
                dialogs.dismiss()
            }
            val dialogs = builder.create()
            dialogs.show()
            dialog.dismiss()

        }
        panel_publicacion.setOnClickListener {
            firebaseAuth = FirebaseAuth.getInstance()
            var intent = Intent(context, panel_publicacion_trabajador::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            context.startActivity(intent)
            dialog.dismiss()
        }
        container_preview.setOnClickListener {
            constantes.showLoadingDialog(
                context,
                3000,
                "Por favor, espere",
                "Cargando datos..."
            )
            mandarDatos(firebaseAuth.uid.toString(), context)
            dialog.dismiss()
        }
        container_guardados.setOnClickListener {
            val intent = Intent(context, noticias_y_review::class.java)
            intent.putExtra(Variables.iduser, firebaseAuth.uid.toString())
            intent.putExtra(Variables.title, "Noticias Guardadas")
            context.startActivity(intent)
            dialog.dismiss()
        }
        container_review.setOnClickListener {
            val intent = Intent(context, noticias_y_review::class.java)
            intent.putExtra(Variables.iduser, firebaseAuth.uid.toString())
            intent.putExtra(Variables.title, "Tus Reseñas")
            context.startActivity(intent)
            dialog.dismiss()
        }
        dialog.setContentView(view)
    }

    private fun eliminarCuenta(
        tipoCuenta: TextView,
        id: String,
        context: Context,
        activity: Activity
    ) {
        when (tipoCuenta.text.toString()) {
            Variables.Cuenta_Simple -> {
                val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                    .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
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

                        Toast.makeText(context, "Error al eliminar el Usuario ", Toast.LENGTH_SHORT)
                            .show()
                    }
            }

            Variables.Cuenta_Trabajador -> {
                val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                    .document(Variables.usuarios_db).collection(Variables.usuarios_db).document(id)
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

                        Toast.makeText(context, "Error al eliminar el Usuario ", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
        val RealTime = FirebaseDatabase.getInstance().getReference(Variables.ReseñasUsuarios).child(id)
        RealTime.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Reseña eliminada ", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->

                Toast.makeText(context, "Error al eliminar la reseña ", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Error al eliminar las fotos ", Toast.LENGTH_SHORT).show()
            }
        firebaseAuth.signOut()
        context.startActivity(Intent(context, MainActivity::class.java))
        activity?.finishAffinity()
    }

    private fun mandarDatos(idRegistrado: String, context: Context) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(idRegistrado)
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

            var edad = calendarioActual.get(Calendar.YEAR) - calendarioNacimiento.get(Calendar.YEAR)


            if (calendarioActual.get(Calendar.DAY_OF_YEAR) < calendarioNacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--
            }

            edadUsuario(edad.toString())
        } catch (e: Exception) {
            edadUsuario("Error en la fecha")
        }
    }

}
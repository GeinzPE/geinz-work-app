package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.BottomSheetContactoDirectoBinding
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

object constantes_bottom_shet_trabaja {
    val handler = Handler(Looper.getMainLooper())

    private lateinit var firebaseAuth: FirebaseAuth
    fun obntener_datos_trabajador(
        dialog: BottomSheetDialog,
        context: Context,
        id_trabajador: String,
        binding_bottomSheet: BottomSheetContactoDirectoBinding
    ) {
        val tiempoInicio = System.currentTimeMillis() // ⏱ INICIO

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id_trabajador)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val nombre = data?.get("nombre") as? String ?: ""
                val apellido = data?.get("apellido") as? String ?: ""
                val categoria = data?.get("categoriaTrabajo") as? String ?: ""
                val subcategoria = data?.get("tipoTrabajo") as? String ?: ""
                val estrella = data?.get("estrellas") as? String ?: ""
                val localidad = data?.get("localidad") as? String ?: ""
                val nacionalidad = data?.get("nacionalidad") as? String ?: ""
                val imagenPerfil = data?.get("imagenPerfil") as? String ?: ""

                binding_bottomSheet.nombreApellido.text = "$nombre $apellido"

                val localidad_nac = SpannableString("Localidad y nacionalidad : ${localidad} | ${nacionalidad}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Localidad y nacionalidad", localidad_nac, binding_bottomSheet.localidadNacionaliad
                )

                val categoria_tra = SpannableString("Categoria de trabajo : ${categoria}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Categoria de trabajo", categoria_tra, binding_bottomSheet.categoria
                )

                val sub_categoria_tra = SpannableString("Subcategoria de trabajo : ${subcategoria}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Subcategoria de trabajo", sub_categoria_tra, binding_bottomSheet.subcategoriaTrabajador
                )

                val estrellas_total = SpannableString("Total de estrellas : ${estrella}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Total de estrellas", estrellas_total, binding_bottomSheet.totalEstrellas
                )

                val drawable_img_perfil = ContextCompat.getDrawable(context, R.drawable.img_perfil)

                constantes_servicios.verificarEstado_vericiacion(
                    binding_bottomSheet.iconoVerificado,
                    id_trabajador
                ) { v, plan ->
                    when (plan) {
                        Variables.plaA -> binding_bottomSheet.iconoVerificado.setImageResource(R.drawable.verificado_a)
                        Variables.planB -> binding_bottomSheet.iconoVerificado.setImageResource(R.drawable.icon_verificado)
                        Variables.PlanC -> binding_bottomSheet.iconoVerificado.setImageResource(R.drawable.verificado_c)
                    }
                }

                binding_bottomSheet.verPerfil.setOnClickListener {
                    val vista = Intent(context, vistaTrabajador::class.java).apply {
                        putExtra(Variables.id, id_trabajador)
                        putExtra(Variables.imagenPerfil, imagenPerfil)
                        putExtra(Variables.nombreUSer, nombre)
                        putExtra(Variables.nacionalidad, nacionalidad)
                        putExtra(Variables.categoria, categoria)
                    }
                    context.startActivity(vista)
                    dialog.dismiss()
                }

                verificarSiSiueTrabajador(binding_bottomSheet, id_trabajador, context)

                constantes_trabajadores_info.contadorSeguidores(
                    binding_bottomSheet.seguidores, id_trabajador
                )

                constantes_trabajadores_info.contadorSiguiendo(
                    binding_bottomSheet.siguiendo, id_trabajador
                )

                constatnes_carga_imagenes_general.changer_img(
                    binding_bottomSheet.cargandoImg,
                    context,
                    imagenPerfil,
                    binding_bottomSheet.imgPerfil,
                    null,
                    "perfil", drawable_img_perfil
                ) {
                    // Imagen cargada → Continuar
                    val tiempoFin = System.currentTimeMillis() // ⏱ FIN
                    Log.d("TIEMPO_CARGA", "Tiempo total de carga: ${tiempoFin - tiempoInicio} ms")
                    handler.postDelayed({
                      binding_bottomSheet.vistraPrevia.isVisible=true
                        binding_bottomSheet.cargarLineal.isVisible=false
                    }, tiempoFin - tiempoInicio)
                }

                obtener_foto_portada(context, id_trabajador, binding_bottomSheet)

                binding_bottomSheet.seguir.setOnClickListener {
                    seguirTrabajador(binding_bottomSheet, id_trabajador)
                }

                binding_bottomSheet.siguiendoBtn.setOnClickListener {
                    dejar_seguir_trabajador(id_trabajador, binding_bottomSheet)
                }
            }
        }
    }

    private fun obtener_foto_portada(
        context: Context,
        id_trabajador: String,
        binding_bottomSheet: BottomSheetContactoDirectoBinding
    ) {
        val ref = FirebaseStorage.getInstance().reference
            .child("usuarios")
            .child(id_trabajador)
            .child("foto_portada")

        ref.downloadUrl.addOnSuccessListener { uri ->
            val url = uri.toString()
            val drawable_img_perfil =
                ContextCompat.getDrawable(context, R.drawable.img_perfil)

            constatnes_carga_imagenes_general.changer_img(
                binding_bottomSheet.cargandoImg,
                context,
                url,
                null,
                binding_bottomSheet.imgPortada,
                "portada", drawable_img_perfil
            ) {}
            Log.d("FirebaseStorage", "URL de la foto portada: $url")
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Error al obtener la URL", exception)
        }
    }

    fun verificarSiSiueTrabajador(
        binding_bottomSheet: BottomSheetContactoDirectoBinding,
        idTrabajadorActual: String,
        contexto: Context,

        ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajadorActual)
            .collection("seguidores")

        db.get().addOnSuccessListener { res ->
            val seguidorEncontrado = res.find { datos ->
                val id = datos.getString("id") ?: ""
                id == firebaseAuth.uid.toString()
            }

            if (seguidorEncontrado != null) {
                Toast.makeText(contexto, "El user sí lo sigue normal", Toast.LENGTH_SHORT).show()
                binding_bottomSheet.siguiendoBtn.isVisible = true
                binding_bottomSheet.seguir.isVisible = false

            } else {
                binding_bottomSheet.siguiendoBtn.isVisible = false
                binding_bottomSheet.seguir.isVisible = true
            }

        }.addOnFailureListener {
            binding_bottomSheet.siguiendoBtn.isVisible = false
            binding_bottomSheet.seguir.isVisible = true
        }
    }

    fun seguirTrabajador(
        binding_bottomSheet: BottomSheetContactoDirectoBinding,
        idTrabajadorActual: String,

        ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("tokens").collection("tokens").document(firebaseAuth.uid.toString())
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val token = data?.get("token") as? String ?: ""
                AgregarCollectionFollow(
                    token,
                    firebaseAuth.uid.toString(),
                    idTrabajadorActual, binding_bottomSheet
                )
            } else {
                println("no se encontro el token del usuario")
            }
        }
    }

    private fun AgregarCollectionFollow(
        token: String,
        iduserActual: String,
        idTrabajadorActual: String,
        binding_bottomSheet: BottomSheetContactoDirectoBinding

    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        // 1. Actualizar en trabajadores > seguidores
        val refSeguidores = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajadorActual)
            .collection("seguidores")
            .document(iduserActual)

        val hashMapSeguidores = hashMapOf<String, Any>(
            "id" to iduserActual,
            "token" to token
        )

        refSeguidores.set(hashMapSeguidores, SetOptions.merge())
            .addOnSuccessListener {
                binding_bottomSheet.seguir.isVisible=false
                binding_bottomSheet.siguiendoBtn.isVisible=true
                constantes_trabajadores_info.contadorSeguidores(
                    binding_bottomSheet.seguidores,
                    idTrabajadorActual
                )
            }
            .addOnFailureListener {
                println("Error al seguir trabajador")
                binding_bottomSheet.seguir.isVisible=true
                binding_bottomSheet.siguiendoBtn.isVisible=true
            }

        // 2. Actualizar en usuarios > seguidos
        val refSeguidos = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(iduserActual)
            .collection("seguidos")
            .document(idTrabajadorActual)

        val hashMapSeguidos = hashMapOf<String, Any>(
            "id" to idTrabajadorActual
        )

        refSeguidos.set(hashMapSeguidos, SetOptions.merge())
            .addOnSuccessListener {
                println("Seguido agregado correctamente al usuario")
            }
            .addOnFailureListener {
                println("Error al agregar seguido")
            }
    }

    fun dejar_seguir_trabajador(
        idTrabajadorActual: String,
        binding_bottomSheet: BottomSheetContactoDirectoBinding
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val refSeguidores = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajadorActual)
            .collection("seguidores")
            .document(firebaseAuth.uid.toString())

        val refSeguidos = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("seguidos")
            .document(idTrabajadorActual)

        refSeguidores.delete().addOnSuccessListener {
            binding_bottomSheet.seguir.isVisible=true
            binding_bottomSheet.siguiendoBtn.isVisible=false
            constantes_trabajadores_info.contadorSeguidores(
                binding_bottomSheet.seguidores,
                idTrabajadorActual
            )

        }.addOnFailureListener { e ->
            binding_bottomSheet.seguir.isVisible=false
            binding_bottomSheet.siguiendoBtn.isVisible=true
        }

        refSeguidos.delete().addOnSuccessListener {
            println("Trabajador eliminado de seguidos correctamente")
        }.addOnFailureListener { e ->
            println("Hubo un error al eliminar seguido: $e")
        }
    }


}
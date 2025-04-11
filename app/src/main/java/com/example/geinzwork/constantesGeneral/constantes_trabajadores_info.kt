package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_seguirTrabajadores_info
import com.example.geinzwork.dataclass.dataClasSeguirTrabajdores_info
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.BottomShettDialogMasInfoTrabajadorBinding
import com.geinzz.geinzwork.databinding.FragmentInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

object constantes_trabajadores_info {
    private lateinit var firebaseAuth: FirebaseAuth
    private var seguir_TXT = "Seguir Trabajador"
    private var SiguientoTXT = "Siguiendo"
    private var seguirSoloTXT = "Seguir"
    private lateinit var adapterTrabajadores: adapter_seguirTrabajadores_info


    fun obtenerMejoresTrabajadores(
        idTrabajadorActual: String,
        tipot_trabajador: String,
        listaTrabajo: MutableList<dataClasSeguirTrabajdores_info>,
        recicle: RecyclerView,
        contexto: Context,
        binding: FragmentInfoBinding,
        trabajdoresDisponibles: (Boolean) -> Unit
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val userCollections = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
        fun procesarLista() {
            // Loguear la lista antes de la mezcla
            Log.d("ListaTrabajoAntes", "Lista antes de mezclar: ${listaTrabajo}")

            // Mezclar la lista
            val listaMezclada = listaTrabajo.shuffled()

            // Limpiar la lista original
            listaTrabajo.clear()

            // Agregar los elementos mezclados a la lista original
            listaTrabajo.addAll(listaMezclada)

            // Loguear la lista después de la mezcla
            Log.d("ListaTrabajoDespues", "Lista después de mezclar: ${listaTrabajo}")
            Log.d("tamañoLista", "el tamaño de la lista es ${listaTrabajo.size}")

            // Notificar si la lista tiene trabajadores disponibles
            trabajdoresDisponibles(listaTrabajo.isNotEmpty())

            // Inicializar los trabajadores sugeridos
            incializarTrabajadoresSugeridos(
                idTrabajadorActual, binding,
                listaTrabajo,
                recicle,
                contexto
            )
        }
        userCollections.get()
            .addOnSuccessListener { querySnapshot ->
                listaTrabajo.clear()
                var trabajadoresProcesados = 0
                val totalTrabajadores = querySnapshot.documents.size

                for (document in querySnapshot.documents) {
                    val userData = document.data
                    val categoriaTrabajo = userData?.get("categoriaTrabajo") as? String

                    if (tipot_trabajador == categoriaTrabajo) {
                        val usuario = dataClasSeguirTrabajdores_info(
                            id = userData?.get("id") as? String,
                            categoria = userData?.get("categoriaTrabajo") as? String,
                            nacionalidad = userData?.get("nacionalidad") as? String,
                            nombreTrabajador = userData?.get("nombre") as? String,
                            subcategoria = userData?.get("tipoTrabajo") as? String,
                            localida = userData?.get("localidad") as? String,
                            img_perfi = userData?.get("imagenPerfil") as? String,
                            verificado = userData?.get("verificado") as? Boolean
                        )

                        if (idTrabajadorActual != usuario.id.toString() && firebaseAuth.uid.toString() != usuario.id.toString()) {
                            val coleccionSeguido = FirebaseFirestore.getInstance()
                                .collection(Variables.trabajadores_usuariosDB)
                                .document(Variables.trabajadoresDB)
                                .collection(Variables.trabajadoresDB).document(usuario.id.toString())
                                .collection("seguidores").document(firebaseAuth.uid.toString())

                            Log.d("Ruta_Firebase", "Ruta de la colección seguidores: ${coleccionSeguido.path}")

                            coleccionSeguido.get().addOnSuccessListener { res ->
                                if (!res.exists()) {
                                    Log.d("ListaTrabajo", "Se agrega el trabajador: ${usuario.nombreTrabajador}, ID: ${usuario.id}")
                                    listaTrabajo.add(usuario)
                                    Log.d("TamañoLista", "Tamaño actual de la lista: ${listaTrabajo.size}")
                                } else {
                                    Log.d("Firestore", "El seguidor ya existe en la colección.")
                                }
                                trabajadoresProcesados++
                                if (trabajadoresProcesados == totalTrabajadores) {
                                    procesarLista()
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("FirebaseError", "Error al obtener el documento: ${exception.message}")
                                trabajadoresProcesados++
                                if (trabajadoresProcesados == totalTrabajadores) {
                                    procesarLista()
                                }
                            }
                        } else {
                            trabajadoresProcesados++
                            if (trabajadoresProcesados == totalTrabajadores) {
                                procesarLista()
                            }
                        }
                    } else {
                        trabajadoresProcesados++
                        if (trabajadoresProcesados == totalTrabajadores) {
                            procesarLista()
                        }
                    }
                }
            }
            .addOnFailureListener {
                trabajdoresDisponibles(false) // Si falla la consulta, también se debe indicar que no hay trabajadores
                Log.e("error", "Error al obtener los trabajadores", it)
            }

        // Función para procesar la lista después de agregar todos los trabajadores

    }



    fun incializarTrabajadoresSugeridos(
        idTrabajadorActual: String, binding: FragmentInfoBinding,
        listaTrabajos: List<dataClasSeguirTrabajdores_info>,
        recicle: RecyclerView,
        contexto: Context
    ) {
        val listaAleatoria = listaTrabajos.shuffled().toMutableList()
        adapterTrabajadores = adapter_seguirTrabajadores_info(true, listaAleatoria,binding)
        recicle.layoutManager = LinearLayoutManager(contexto, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterTrabajadores


    }

    private fun verificarSeguidor(
        idTrabajadorActual: String,
        userCollections: CollectionReference,
        iduserActual: String,
        Seguido: (Boolean) -> Unit
    ) {
        println("🔍 Verificando si el usuario $iduserActual sigue al trabajador $idTrabajadorActual")

        userCollections.document(idTrabajadorActual).collection("seguidores").get()
            .addOnSuccessListener { res ->
                var esSeguidor = false

                for (datos in res) {
                    val data = datos.data
                    val idFollo = data?.get("id") as? String ?: ""
                    println("👤 Comparando seguidor: $idFollo con actual: $iduserActual")

                    if (iduserActual == idFollo) {
                        println("✅ Usuario $iduserActual *sí* sigue al trabajador $idTrabajadorActual")
                        esSeguidor = true
                        break
                    }
                }

                if (!esSeguidor) {
                    println("❌ Usuario $iduserActual *no* sigue al trabajador $idTrabajadorActual")
                }

                Seguido(esSeguidor)
            }.addOnFailureListener { e ->
                println("🚫 Error al encontrar los seguidores del trabajador $idTrabajadorActual: ${e.message}")
                Seguido(false)
            }
    }


    fun verificarFollow(
        idTrabajadorActual: String,
        binding: FragmentInfoBinding,
        contexto: Context
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            val builder = AlertDialog.Builder(contexto)
            builder.setTitle("No estás registrado en Geinz Work")
            builder.setMessage("Regístrate en Geinz Work para que puedas seguir.")
            builder.setPositiveButton("Cuenta Simple") { dialog, _ ->
                // Mostrar diálogo de carga y redirigir a la pantalla de registro
                constantes.showLoadingDialog(
                    contexto,
                    2000,
                    "Cargando información",
                    "Espere un momento..."
                )
                val intent = Intent(contexto, CuentaFreelancer::class.java).apply {
                    putExtra("tipoCuenta", "cuentaSimple")
                    putExtra("Title", "Cuenta Simple")
                    putExtra("pasos", "Estás a 1/2 pasos")
                }
                contexto.startActivity(intent)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cuenta Trabajador") { dialog, _ ->
                val intent = Intent(contexto, CuentaFreelancer::class.java).apply {
                    putExtra("tipoCuenta", "cuentaTrabajador")
                    putExtra("Title", "Cuenta Freelancer")
                    putExtra("pasos", "Estás a 1/5 pasos")
                }
                contexto.startActivity(intent)
                dialog.dismiss()
            }
            builder.create().show()
        } else {
            val textoBTN = binding.dejarDeSeguirOSeguir.text
            when (textoBTN.toString()) {
                seguir_TXT -> {
                    binding.notificaciones.isVisible = true
                    seguirTrabajador(idTrabajadorActual, binding)
                    binding.dejarDeSeguirOSeguir.text = SiguientoTXT
                    aplicarEstiloSigueindo(binding.dejarDeSeguirOSeguir,contexto)
                }

                SiguientoTXT -> {
                    showCustomUnfollowDialog(binding, contexto, idTrabajadorActual)
                }

                seguirSoloTXT -> {
                    binding.notificaciones.isVisible = true
                    verificarSiSiueTrabajador(binding, idTrabajadorActual,contexto) { siguiendo ->
                        if (siguiendo) {
                            seguirTrabajador(idTrabajadorActual, binding)
                        }
                    }

                    binding.dejarDeSeguirOSeguir.text = SiguientoTXT
                }
            }
        }

    }
    fun aplicarEstiloSigueindo(button: AppCompatButton, context: Context) {
        button.setBackgroundResource(R.drawable.bordes_btn_sigueindo_trabajador)
        button.setTextColor(ContextCompat.getColor(context, R.color.heartOutlineColor))
        button.textSize = 12f
        button.setPadding(0, button.paddingTop, 0, button.paddingBottom)
        button.isAllCaps = false
        button.backgroundTintList = null
    }

    fun aplicarEstiloPorDefecto(button: AppCompatButton, context: Context) {
        button.setBackgroundResource(R.drawable.bordes_dtn_seguir_trabajdores)
        button.setTextColor(ContextCompat.getColor(context, R.color.white))
        button.textSize = 12f
        button.setPadding(0, button.paddingTop, 0, button.paddingBottom)
        button.isAllCaps = false
        button.backgroundTintList = null
    }


    fun verificarSiSiueTrabajador(
        binding: FragmentInfoBinding,
        idTrabajadorActual: String,
        contexto: Context,
        SeguirTrabajado: (Boolean) -> Unit
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores")
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val id = data?.get("id") as? String ?: ""
                if (firebaseAuth.uid.toString() == id) {
                    binding.dejarDeSeguirOSeguir.text = SiguientoTXT
                    binding.notificaciones.isVisible = true
                    aplicarEstiloSigueindo(binding.dejarDeSeguirOSeguir,contexto)
                    SeguirTrabajado(true)
                } else {
                    binding.notificaciones.isVisible = false
                    aplicarEstiloPorDefecto(binding.dejarDeSeguirOSeguir,contexto)
                    binding.dejarDeSeguirOSeguir.text = seguir_TXT
                    SeguirTrabajado(false)
                }
            }
        }.addOnFailureListener { e ->
            binding.dejarDeSeguirOSeguir.text = seguir_TXT
            SeguirTrabajado(false)
        }
    }

    private fun seguirTrabajador(idTrabajadorActual: String, binding: FragmentInfoBinding) {
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
                    idTrabajadorActual,
                    binding
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
        binding: FragmentInfoBinding
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores").document(iduserActual)
        val hashMap = hashMapOf<String, Any>(
            "id" to iduserActual,
            "token" to token
        )
        db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
            println("Seguido correctamente")
            actualizarSeguidres(binding, idTrabajadorActual)
        }.addOnFailureListener {
            println("Error al seguir")
        }
    }

    private fun dejarSeguitrTrabajdor(binding: FragmentInfoBinding, idTrabajadorActual: String,contexto: Context) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores").document(firebaseAuth.uid.toString())
        aplicarEstiloPorDefecto(binding.dejarDeSeguirOSeguir,contexto)
        db.delete().addOnSuccessListener { res ->
            println("dejo seguir corecteamnte")
            actualizarSeguidres(binding, idTrabajadorActual)
            binding.dejarDeSeguirOSeguir.text = seguir_TXT

        }.addOnFailureListener { e ->
            println("ubo un error al dejar de seguir $e")
        }
    }

    fun showCustomUnfollowDialog(
        binding: FragmentInfoBinding,
        context: Context,
        idTrabajadorActual: String
    ) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_unfollow, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            dejarSeguitrTrabajdor(binding, idTrabajadorActual,context)
            binding.dejarDeSeguirOSeguir.text = seguir_TXT
            binding.notificaciones.isVisible = false
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)

    fun mostrarDialoDatosUSer(
        dialog: BottomSheetDialog,
        idTrabajadorActual: String,
        contexto: Context,
        img: String
    ) {
        val bindingBottomSheett =
            BottomShettDialogMasInfoTrabajadorBinding.inflate(LayoutInflater.from(contexto))
        dialog.setContentView(bindingBottomSheett.root)
        bindingBottomSheett.cerrar.setOnClickListener {
            dialog.dismiss()
        }

        var tareasCompletadas = 0
        val totalTareas = 3 // puedes aumentar si agregas más tareas asincrónicas

        Handler(Looper.getMainLooper()).postDelayed({
            bindingBottomSheett.progresCargaDatos.isVisible = false
            bindingBottomSheett.linealDatos.isVisible = true
        }, 3000) // 3000 milisegundos = 3 segundos

        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(idTrabajadorActual)

        userCollections.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val nombre = data?.get(Variables.nombre) as? String ?: ""
                val descripcion = data?.get(Variables.descripcion) as? String ?: ""
                val categoriaTrabajo = data?.get(Variables.categoriaTrabajo) as? String ?: ""
                val genero = data?.get(Variables.genero) as? String ?: ""
                val horario1 = data?.get(Variables.horario1) as? String ?: ""
                val horario2 = data?.get(Variables.horario2) as? String ?: ""
                val nacionalidad = data?.get(Variables.nacionalidad) as? String ?: ""
                val localidad = data?.get(Variables.localidad) as? String ?: ""
                val codigo_pais = data?.get(Variables.codigo_pais) as? String ?: ""
                val numero = data?.get(Variables.numero) as? String ?: ""
                val tipoTrabajo = data?.get(Variables.tipoTrabajo) as? String ?: ""
                val FechaNacimiento = data?.get(Variables.fechaNac) as? String ?: ""

                val placeholderPortada =
                    ContextCompat.getDrawable(contexto, R.drawable.sin_foto_portada_con_marca)
                val placeholder = ContextCompat.getDrawable(contexto, R.drawable.img_perfil)

                val refStorage =
                    FirebaseStorage.getInstance().getReference(Variables.usuarios_db)
                        .child(idTrabajadorActual).child(Variables.foto_portada)
                refStorage.downloadUrl.addOnSuccessListener { uri ->
                    val imgUrl = uri.toString()
                    constatnes_carga_imagenes_general.changer_img(
                        bindingBottomSheett.progressCargaImagenFondo,
                        contexto,
                        imgUrl,
                        null,
                        bindingBottomSheett.imgPortada,
                        "portada", placeholderPortada
                    ) {
                        tareasCompletadas++

                    }
                }

                constantes.obtenerEstado(bindingBottomSheett.estado, idTrabajadorActual)
                tareasCompletadas++

                constatnes_carga_imagenes_general.changer_img(
                    bindingBottomSheett.progressCargaImagen,
                    contexto,
                    img,
                    bindingBottomSheett.imgPerfilUser,
                    null,
                    "perfil", placeholder
                ) {
                    tareasCompletadas++

                }

                bindingBottomSheett.nombre.text = nombre.toUpperCase()
                bindingBottomSheett.categoriaTipoTrabajo.text = "$tipoTrabajo | $categoriaTrabajo"

                constantestextos_general.extender_acortar_texto(
                    bindingBottomSheett.caracteristica1,
                    bindingBottomSheett.tvReadMore
                )

                val spannableString =
                    SpannableString("${"Descripcion : "} ${descripcion}")
                val boldSpan = StyleSpan(Typeface.BOLD)
                val startIndex = 0
                val endIndex = "Descripcion : ".length
                spannableString.setSpan(
                    boldSpan,
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                bindingBottomSheett.caracteristica1.text = spannableString
                bindingBottomSheett.genero.text = genero
                bindingBottomSheett.nacionalida.text = nacionalidad
                bindingBottomSheett.localidad.text = localidad
                bindingBottomSheett.horario.text = "${horario1} am : ${horario2} pm"
                bindingBottomSheett.telefono.text = "+${codigo_pais} ${numero}"
                constantes_cuenta_user.calcularEdadTrabajador(FechaNacimiento) { edad ->
                    if (edad.isNullOrEmpty()) {
                        bindingBottomSheett.edadUser.text = "No se calculo los años"
                    } else {
                        bindingBottomSheett.edadUser.text = "${edad} años"
                    }
                }
            }
        }.addOnFailureListener { e ->
            println("error al obtner los datos $e")
        }
    }


    fun actualizarSeguidres(binding: FragmentInfoBinding, idTrabajadorActual: String) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores")
        db.get().addOnSuccessListener { res ->
            val totalSeguidores = res.size()
            binding.segidores.text = "${totalSeguidores} Seguidores"
        }.addOnFailureListener { e ->
            binding.dejarDeSeguirOSeguir.text = seguir_TXT
        }

    }


}
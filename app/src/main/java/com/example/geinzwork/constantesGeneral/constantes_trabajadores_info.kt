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
import android.widget.TextView
import android.widget.Toast
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
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.databinding.BottomShettDialogMasInfoTrabajadorBinding
import com.geinzz.geinzwork.databinding.FragmentCategoriasFracmentBinding
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
    private lateinit var dialog: BottomSheetDialog


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
                                .collection(Variables.trabajadoresDB)
                                .document(usuario.id.toString())
                                .collection("seguidores").document(firebaseAuth.uid.toString())

                            Log.d(
                                "Ruta_Firebase",
                                "Ruta de la colección seguidores: ${coleccionSeguido.path}"
                            )

                            coleccionSeguido.get().addOnSuccessListener { res ->
                                if (!res.exists()) {
                                    Log.d(
                                        "ListaTrabajo",
                                        "Se agrega el trabajador: ${usuario.nombreTrabajador}, ID: ${usuario.id}"
                                    )
                                    listaTrabajo.add(usuario)
                                    Log.d(
                                        "TamañoLista",
                                        "Tamaño actual de la lista: ${listaTrabajo.size}"
                                    )
                                } else {
                                    Log.d("Firestore", "El seguidor ya existe en la colección.")
                                }
                                trabajadoresProcesados++
                                if (trabajadoresProcesados == totalTrabajadores) {
                                    procesarLista()
                                }
                            }.addOnFailureListener { exception ->
                                Log.e(
                                    "FirebaseError",
                                    "Error al obtener el documento: ${exception.message}"
                                )
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
        adapterTrabajadores = adapter_seguirTrabajadores_info(true, listaAleatoria, binding)
        recicle.layoutManager = LinearLayoutManager(contexto, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterTrabajadores


    }


    fun seguir_trabajador(
        idTrabajadorActual: String,
        binding: FragmentInfoBinding,
        contexto: Context
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            dialog = BottomSheetDialog(contexto)
            constantesPublicidad.CreacionCuentaBottom_shett(
                contexto,
                dialog
            )
            dialog.show()
        } else {
            seguirTrabajador(idTrabajadorActual, binding, true)

            actualizarSeguidres(binding, idTrabajadorActual)
            binding.notificaciones.isVisible=true
            binding.siguiendoBtn.isVisible = true
            binding.dejarDeSeguirOSeguir.isVisible = false
        }

    }

    fun aplicarEstiloSigueindo(button: AppCompatButton, context: Context) {
        button.setBackgroundResource(R.drawable.bordes_btn_sigueindo_trabajador)
        button.setTextColor(ContextCompat.getColor(context, R.color.white))
        button.textSize = 12f
        button.setPadding(0, button.paddingTop, 0, button.paddingBottom)
        button.isAllCaps = false
        button.backgroundTintList = null
    }

    fun aplicarEstiloPorDefecto(button: AppCompatButton, context: Context) {
        button.setBackgroundResource(R.drawable.bordes_dtn_seguir_trabajdores)
        button.setTextColor(ContextCompat.getColor(context, R.color.heartOutlineColor))
        button.textSize = 12f
        button.setPadding(0, button.paddingTop, 0, button.paddingBottom)
        button.isAllCaps = false
        button.backgroundTintList = null
    }


    fun verificarSiSiueTrabajador(
        binding: FragmentInfoBinding,
        idTrabajadorActual: String,
        contexto: Context,
        SeguirTrabajado: (Boolean) -> Unit,
        notificacion: (Boolean) -> Unit
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
                val notificado = seguidorEncontrado.getBoolean("notificado") ?: false
                Toast.makeText(contexto, "El user sí lo sigue normal", Toast.LENGTH_SHORT).show()

                binding.notificaciones.isVisible = true
                binding.siguiendoBtn.isVisible = true
                binding.dejarDeSeguirOSeguir.isVisible = false
                SeguirTrabajado(true)
                notificacion(notificado)
            } else {
                binding.notificaciones.isVisible = false
                binding.siguiendoBtn.isVisible = false
                binding.dejarDeSeguirOSeguir.isVisible = true
                SeguirTrabajado(false)
            }

        }.addOnFailureListener {
            binding.siguiendoBtn.isVisible = false
            binding.dejarDeSeguirOSeguir.isVisible = true
            SeguirTrabajado(false)
        }
    }

    fun seguirTrabajador(
        idTrabajadorActual: String,
        binding: FragmentInfoBinding,
        funcion_aplicar: Boolean
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
                    idTrabajadorActual,
                    binding, funcion_aplicar, false
                )

            } else {
                println("no se encontro el token del usuario")
            }
        }.addOnFailureListener { e ->
            Log.e("erro_toke", "error al obtener los tokens $e")
        }
    }

    fun seguirTrabajadorcategoriasFR(
        binding: FragmentInfoBinding? = null,
        idTrabajadorActual: String,
        funcion_aplicar: Boolean,
        funcionaplicar_fragmet_seguidos: Boolean
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("tokens").collection("tokens").document(firebaseAuth.uid.toString())
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val token = data?.get("token") as? String ?: ""
                if (funcionaplicar_fragmet_seguidos || funcion_aplicar) {
                    AgregarCollectionFollow(
                        token,
                        firebaseAuth.uid.toString(),
                        idTrabajadorActual,
                        binding,
                        funcion_aplicar, funcionaplicar_fragmet_seguidos
                    )
                } else {
                    AgregarCollectionFollow(
                        token,
                        firebaseAuth.uid.toString(),
                        idTrabajadorActual,
                        null,
                        funcion_aplicar, funcionaplicar_fragmet_seguidos
                    )
                }

            } else {
                println("no se encontro el token del usuario")
            }
        }
    }

    private fun AgregarCollectionFollow(
        token: String,
        iduserActual: String,
        idTrabajadorActual: String,
        binding: FragmentInfoBinding? = null,
        funcion_aplicar: Boolean, funcionaplicar_fragmet_seguidos: Boolean
    ) {
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
                println("Seguidor agregado correctamente")
                println("el valo de actualizar fue $funcion_aplicar")
                if (funcionaplicar_fragmet_seguidos && binding != null) {
                    ver_cantidad_siguiendo(binding, firebaseAuth.uid.toString())
                }
            }
            .addOnFailureListener {
                if (binding != null) {
                    binding.notificaciones.isVisible=false
                    binding.siguiendoBtn.isVisible = false
                    binding.dejarDeSeguirOSeguir.isVisible = true
                }
                println("Error al seguir trabajador")
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
                if (binding != null) {
                    binding.notificaciones.isVisible=false
                    binding.siguiendoBtn.isVisible = false
                    binding.dejarDeSeguirOSeguir.isVisible = true
                }
            }
    }


    fun dejarSeguirTrabajador(
        binding: FragmentInfoBinding,
        idTrabajadorActual: String,
        contexto: Context,
        estilo: Boolean, acutalizarSeguidores: Boolean
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

        if (estilo) {
            aplicarEstiloPorDefecto(binding.dejarDeSeguirOSeguir, contexto)
        }

        refSeguidores.delete().addOnSuccessListener {
            println("Dejó de seguir correctamente al trabajador")
            if (estilo) {
                binding.dejarDeSeguirOSeguir.text = seguir_TXT
            }
        }.addOnFailureListener { e ->
            println("Hubo un error al dejar de seguir trabajador: $e")
        }

        refSeguidos.delete().addOnSuccessListener {
            println("Trabajador eliminado de seguidos correctamente")
        }.addOnFailureListener { e ->
            println("Hubo un error al eliminar seguido: $e")
        }
    }

    fun dejarSeguirTrabajadorcaregoriasFR(
        idTrabajadorActual: String,
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
            println("Dejó de seguir correctamente al trabajador")

        }.addOnFailureListener { e ->
            println("Hubo un error al dejar de seguir trabajador: $e")
        }

        refSeguidos.delete().addOnSuccessListener {
            println("Trabajador eliminado de seguidos correctamente")
        }.addOnFailureListener { e ->
            println("Hubo un error al eliminar seguido: $e")
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
            binding.siguiendoBtn.isVisible = false
            binding.dejarDeSeguirOSeguir.isVisible = true
            binding.notificaciones.isVisible=false
            actualizar_dejar_seguir(binding, idTrabajadorActual)
            dejarSeguirTrabajador(binding, idTrabajadorActual, context, true, true)
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
        actualizarSeguidores(true,binding.segidores,binding.segidores.text.toString().toInt())
        db.get().addOnSuccessListener { res ->
            Log.d("correcto_segudores","se actualizo correctaete los segudores")

        }.addOnFailureListener { e ->
            actualizarSeguidores(false,binding.segidores,binding.segidores.text.toString().toInt())
        }
    }


    fun actualizar_dejar_seguir(binding: FragmentInfoBinding, idTrabajadorActual: String){
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores")
        actualizarSeguidores(false,binding.segidores,binding.segidores.text.toString().toInt())
        db.get().addOnSuccessListener { res ->
            Log.d("correcto_segudores","se actualizo correctaete los segudores")

        }.addOnFailureListener { e ->
            actualizarSeguidores(false,binding.segidores,binding.segidores.text.toString().toInt())
        }
    }

    fun actualizarSeguidores(esSuma: Boolean, textView: TextView, cantidad: Int) {
        val nuevoValor = if (esSuma) cantidad + 1 else cantidad - 1

        // Logs para depuración
        Log.d("Seguidores", "Operación: ${if (esSuma) "Suma" else "Resta"}")
        Log.d("Seguidores", "Cantidad actual: $cantidad")
        Log.d("Seguidores", "Nuevo valor: $nuevoValor")

        textView.text = nuevoValor.toString()
    }


    fun obtener_Segudores(binding: FragmentInfoBinding, idTrabajadorActual: String){
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores")

        db.get().addOnSuccessListener { res ->
            Log.d("correcto_segudores","se actualizo correctaete los segudores")
            binding.segidores.text="${res.size()}"
        }.addOnFailureListener { e ->
            actualizarSeguidores(false,binding.segidores,binding.segidores.text.toString().toInt())
        }
    }

    fun ver_cantidad_siguiendo(binding: FragmentInfoBinding, idTrabajadorActual: String) {
        Log.d("llegaod", "llegao a la fun sigueiotnod")
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidos")
        db.get().addOnSuccessListener { res ->
            val totalSeguidos = res.size()
            binding.siguiendo.text = "${totalSeguidos}"
            Log.d("llegaod", "Total segudiores acutales son $totalSeguidos")

        }.addOnFailureListener { e ->
            binding.dejarDeSeguirOSeguir.text = seguir_TXT
        }
    }

    fun contadorSeguidores(texView: TextView, idTrabajadorActual: String) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidores")
        db.get().addOnSuccessListener { res ->
            val totalSeguidores = res.size()
            texView.text = "${totalSeguidores}"
        }.addOnFailureListener { e ->
            println("error al setear los seguidores $e")
        }
    }

    fun contadorSiguiendo(texView: TextView, idTrabajadorActual: String) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
            .collection("seguidos")
        db.get().addOnSuccessListener { res ->
            val totalSeguidores = res.size()
            texView.text = "${totalSeguidores}"
        }.addOnFailureListener { e ->
            println("error al setear los seguidores $e")
        }
    }


}
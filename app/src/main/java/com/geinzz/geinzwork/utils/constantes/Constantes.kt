package com.geinzz.geinzwork.utils.constantes.constantes

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.model.dataClassTrabajosd
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.net.URLEncoder
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.get

object constantes {

    val firebaseAuth = FirebaseAuth.getInstance()

    fun vistaTrabajador(contexto: Context, dataClassTrabajosd: dataClassTrabajosd) {
        var vista =
            Intent(contexto, vistaTrabajador::class.java)
        vista.putExtra(Variables.id, dataClassTrabajosd.id)
        vista.putExtra(Variables.imagenPerfil, dataClassTrabajosd.imgpriamria)
        vista.putExtra(Variables.nombreUSer, dataClassTrabajosd.nombre)
        vista.putExtra(Variables.nacionalidad, dataClassTrabajosd.nacionalidad)
        vista.putExtra(Variables.categoria, dataClassTrabajosd.categoria)
        contexto.startActivity(vista)

    }

    fun setCategoria(dataClassTrabajosd: dataClassTrabajosd, categoria: TextView) {
        val categoriasAbreviadas = mapOf(
            "Desarrollo y animacion" to "D.Animacion",
            "Transporte y carga" to "T.Carga",
            "Mecanica" to "Mecanico",
            "Trabajo Hogar" to "T.Hogar",
            "Tecnico" to "Tecnico",
            "Construcción y hogar" to "h y Construcción",
            "Servicios de Salud" to "S.Salud",
            "Educación" to "Educación",
            "Legal y Jurídico" to "L.Jurídico",
            "Redacción y Edición" to "R.Edición",
            "Diseño Gráfico y Multimedia" to "D.Gr.M",
            "Desarrollo Web y Programación" to "D.Web",
            "Marketing Digital y Publicidad" to "M.Digital",
            "Artes Visuales y Creativas" to "A.Visuales",
            "Desarrollo Personal y Bienestar" to "D.Personal",
            "Escritura Creativa y Periodismo" to "E.Periodismo",
            "Conductor/a de reparto" to "C.Reparto",
            "Chofer privado" to "Chofer",
            "Conductor/a de transporte escolar" to "C.Escolar",
            "Mecánico/a" to "Mecánico"
        )

        val categoriaOriginal = dataClassTrabajosd.categoria
        categoria.text = categoriasAbreviadas[categoriaOriginal] ?: categoriaOriginal

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun quitaram(dataClassTrabajosd: dataClassTrabajosd): String {
        val formatohora = DateTimeFormatter.ofPattern("HH:mm")
        val formatoentradaam = dataClassTrabajosd.horarioam?.format(formatohora)
        val horaSinAm = formatoentradaam?.replace(" a. m.", "")
        return horaSinAm.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun quitarpm(dataClassTrabajosd: dataClassTrabajosd): String {
        val formatohora = DateTimeFormatter.ofPattern("HH:mm")
        val formatoentradaam = dataClassTrabajosd.horariopm?.format(formatohora)
        val horaSinAm = formatoentradaam?.replace(" p. m.", "")
        return horaSinAm.toString()
    }


    fun carga(timpo: Long, mostrarDatos: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            mostrarDatos()
        }, timpo)

    }

    fun obtenerFotoPerfil(
        dataClassTrabajosd: dataClassTrabajosd,
        contexto: Context,
        img: ImageView,
        imgperfil: CircleImageView,
    ) {
        val refStorage = FirebaseStorage.getInstance().getReference(Variables.usuarios_db)
            .child(dataClassTrabajosd.id.toString()).child(Variables.foto_portada)

        refStorage.downloadUrl.addOnSuccessListener { uri ->
            val imgUrl = uri.toString()
            try {
                Glide.with(contexto)
                    .load(imgUrl)
                    .placeholder(R.drawable.cargando_img)
                    .error(R.drawable.sin_foto_portada_con_marca)
                    .into(img)
            } catch (e: Exception) {
                println(e)
            }
        }.addOnFailureListener { exception ->
            try {
                Glide.with(contexto)
                    .load(R.drawable.sin_foto_portada_con_marca)
                    .into(img)
            } catch (e: Exception) {
                println(e)
            }
        }

        try {
            Glide.with(contexto)
                .load(dataClassTrabajosd.imgpriamria)
                .placeholder(R.drawable.img_perfil)
                .into(imgperfil)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun setearBanderas(
        dataClassTrabajosd: dataClassTrabajosd,
        contexto: Context,
        imagen: CircleImageView
    ) {
        val banderaResId = when (dataClassTrabajosd.nacionalidad?.trim()?.lowercase()) {
            Variables.Peruano.lowercase() -> R.drawable.bandera_peru
            Variables.Venezolano.lowercase() -> R.drawable.bandera_venezolana
            else -> R.drawable.logo_geinz_circular
        }

        try {
            Glide.with(contexto)
                .load(banderaResId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imagen)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    fun contactarWhatsapp(numero: String, mensaje: String, contexto: Context) {
        val uri = Uri.parse(
            "https://api.whatsapp.com/send?phone=$numero&text=${
                URLEncoder.encode(
                    mensaje,
                    "UTF-8"
                )
            }"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            contexto.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(contexto, "no se pudo abrir Whatsapp", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerEstado(view: View, id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)

        db.get()
            .addOnSuccessListener { valor ->
                val data = valor.data
                val horario1 = data?.get(Variables.horario1) as? String ?: ""
                val horario2 = data?.get(Variables.horario2) as? String ?: ""

                if (horario1.isNotEmpty() && horario2.isNotEmpty()) {
                    val ahora = LocalTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")

                    try {
                        val apertura = LocalTime.parse(horario1, formatter)
                        val cierre = LocalTime.parse(horario2, formatter)
                        val activoAhora = ahora.isAfter(apertura) && ahora.isBefore(cierre)

                        view.setBackgroundResource(
                            if (activoAhora) R.drawable.round_activo else R.drawable.round_desactivado
                        )
                    } catch (e: Exception) {
                        println("Error procesando horas para ID: $id. ${e.message}")
                        view.setBackgroundResource(R.drawable.round_gris)
                    }
                } else {
                    println("Usuario con ID: $id no tiene horario definido.")
                    view.setBackgroundResource(R.drawable.round_gris)
                }
            }
            .addOnFailureListener { error ->
                println("Error obteniendo datos para ID: $id. ${error.message}")
                view.setBackgroundResource(R.drawable.round_gris)
            }
    }


    fun SolicitarPermisoNotificacion(
        contexto: Context,
        permisoNotificaion: ActivityResultLauncher<String>,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_DENIED
            ) {
                permisoNotificaion.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun showLoadingDialog(mContext: Context, time: Long, title: String, msj: String) {
        val dialogView =
            LayoutInflater.from(mContext).inflate(R.layout.alert_dialog_personalizado, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)

        dialogTitle.text = "$title"
        dialogMessage.text = "$msj"

        val builder = AlertDialog.Builder(mContext)
        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()


        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            dialog.dismiss()


        }, time)

    }
    


    fun abrirGoogleMaps(contex: Context, direccion: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(direccion)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        contex.startActivity(intent)
    }


    fun llamarCliente(context: Context, tel: String, REQUEST_CALL_PHONE: Int) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$tel")

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivity(intent)
            } else {
                showPermissionDialog(context, REQUEST_CALL_PHONE)
            }
        } catch (e: SecurityException) {

            e.printStackTrace()
            Toast.makeText(
                context,
                "No se pudo realizar la llamada. Permiso denegado.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showPermissionDialog(context: Context, REQUEST_CALL_PHONE: Int) {
        AlertDialog.Builder(context)
            .setTitle("Permiso necesario")
            .setMessage("Esta aplicación necesita permiso para realizar llamadas. Por favor, activa el permiso.")
            .setPositiveButton("Aceptar") { dialog, which ->
                requestCallPermission(context, REQUEST_CALL_PHONE)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                Toast.makeText(context, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun requestCallPermission(context: Context, REQUEST_CALL_PHONE: Int) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
        }
    }

    fun obtenerToken_trabajador(
        idTrabajador: String,
        tokenCallback: (String, String, String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        val tokenRef = db.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(idTrabajador)

        val tokenUserRef = db.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.usuarios_db).collection(Variables.usuarios_db)
            .document(idTrabajador)

        tokenRef.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val tokenUser = datos?.get(Variables.token) as? String ?: ""
                val nombre = datos?.get(Variables.nombre) as? String ?: ""
                val apellido = datos?.get(Variables.apellido) as? String ?: ""


                tokenCallback(tokenUser, nombre, apellido)
                Log.d("obtener_token", "Token: $tokenUser, Nombre: $nombre, Apellido: $apellido")

            } else {

                tokenUserRef.get().addOnSuccessListener { resUser ->
                    if (resUser.exists()) {
                        val datosUser = resUser.data
                        val tokenUser = datosUser?.get(Variables.token) as? String ?: ""
                        val nombre = datosUser?.get(Variables.nombre) as? String ?: ""
                        val apellido = datosUser?.get(Variables.apellido) as? String ?: ""
                        tokenCallback(tokenUser, nombre, apellido)
                        Log.d(
                            "obtener_token",
                            "Token (desde usuarios): $tokenUser, Nombre: $nombre, Apellido: $apellido"
                        )
                    } else {
                        tokenCallback("", "", "")
                        Log.d("obtener_token", "No se encontró el token en ninguna colección")
                    }
                }.addOnFailureListener { e ->
                    tokenCallback("", "", "")
                    Log.e("obtener_token", "Error al obtener el token del usuario", e)
                }
            }
        }.addOnFailureListener { e ->
            tokenCallback("", "", "")
            Log.e("obtener_token", "Error al obtener el token del trabajador", e)
        }
    }

    fun pertenecia_trabajador_user(
        idTrabajador: String,
        callback: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        val tokenRef = db.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(idTrabajador)

        val tokenUserRef = db.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.usuarios_db).collection(Variables.usuarios_db)
            .document(idTrabajador)

        tokenRef.get().addOnSuccessListener { res ->
            if (res.exists()) {
                callback(true) // Es trabajador
                Log.d("obtener_tipo", "Es trabajador")
            } else {
                tokenUserRef.get().addOnSuccessListener { resUser ->
                    if (resUser.exists()) {
                        callback(false) // Es usuario normal
                        Log.d("obtener_tipo", "Es usuario normal")
                    } else {
                        callback(false) // No existe en ninguna colección, igual mandamos false
                        Log.d("obtener_tipo", "No encontrado en trabajadores ni usuarios")
                    }
                }.addOnFailureListener { e ->
                    callback(false)
                    Log.e("obtener_tipo", "Error al buscar en usuarios", e)
                }
            }
        }.addOnFailureListener { e ->
            callback(false)
            Log.e("obtener_tipo", "Error al buscar en trabajadores", e)
        }
    }



    fun obtnerTokenTienda(idTienda: String, tokenTienda: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas).document(idTienda)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val token = datos?.get(Variables.token) as? String ?: ""
                tokenTienda(token)
            }
        }.addOnFailureListener {
            tokenTienda("")
        }

    }


}
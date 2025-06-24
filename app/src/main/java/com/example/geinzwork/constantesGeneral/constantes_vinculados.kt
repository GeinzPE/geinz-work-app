package com.example.geinzwork.constantesGeneral

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.SetOptions

object constantes_vinculados {
    private lateinit var firebaseAuth: FirebaseAuth
    fun encotrar_user(idRegistrado: String, callback: (Boolean, CollectionReference?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        Log.d("DEBUG", "Buscando usuario con ID: $idRegistrado")

        val trabajadorCollection = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")

        val usuarioCollection = db.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios")

        trabajadorCollection.whereEqualTo("id", idRegistrado).get()
            .addOnSuccessListener { trabajadorResult ->
                if (!trabajadorResult.isEmpty) {
                    Log.d("DEBUG", "Usuario encontrado en 'trabajadores'")
                    callback(true, trabajadorCollection)
                } else {
                    Log.d("DEBUG", "No se encontró en 'trabajadores', buscando en 'usuarios'")
                    usuarioCollection.whereEqualTo("id", idRegistrado).get()
                        .addOnSuccessListener { usuarioResult ->
                            if (!usuarioResult.isEmpty) {
                                Log.d("DEBUG", "Usuario encontrado en 'usuarios'")
                                callback(true, usuarioCollection)
                            } else {
                                Log.d("DEBUG", "Usuario no encontrado en ninguna colección")
                                callback(false, null)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ERROR", "Error buscando en 'usuarios'", e)
                            callback(false, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ERROR", "Error buscando en 'trabajadores'", e)
                callback(false, null)
            }
    }

    fun encontrarUser(
        idRegistrado: String,
        callback: (tipo: String?, coleccion: CollectionReference?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        Log.d("DEBUG", "Buscando usuario con ID: $idRegistrado")

        val trabajadorCollection = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")

        val usuarioCollection = db.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios")

        trabajadorCollection.whereEqualTo("id", idRegistrado).get()
            .addOnSuccessListener { trabajadorResult ->
                if (!trabajadorResult.isEmpty) {
                    Log.d("DEBUG", "Usuario encontrado en 'trabajadores'")
                    callback("trabajador", trabajadorCollection)
                } else {
                    Log.d("DEBUG", "No se encontró en 'trabajadores', buscando en 'usuarios'")
                    usuarioCollection.whereEqualTo("id", idRegistrado).get()
                        .addOnSuccessListener { usuarioResult ->
                            if (!usuarioResult.isEmpty) {
                                Log.d("DEBUG", "Usuario encontrado en 'usuarios'")
                                callback("usuario", usuarioCollection)
                            } else {
                                Log.d("DEBUG", "Usuario no encontrado en ninguna colección")
                                callback(null, null)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ERROR", "Error buscando en 'usuarios'", e)
                            callback(null, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ERROR", "Error buscando en 'trabajadores'", e)
                callback(null, null)
            }
    }


    fun agregar_vinculado(idRegistrado: String, context: Context,tipo_directo:String) {
        val dispositivo = "${Build.MANUFACTURER} ${Build.MODEL}"
        val androidId = obtenerAndroidID(context)

        Log.d("DEBUG", "Iniciando proceso de vinculación para ID: $idRegistrado")
        Log.d("DEBUG", "Dispositivo detectado: $dispositivo")
        Log.d("DEBUG", "Android ID: $androidId")

        encotrar_user(idRegistrado) { exi, coleccion ->
            if (exi && coleccion != null) {
                Log.d("DEBUG", "Usuario encontrado, preparando datos para guardar")

                val vinculadoRef = coleccion.document(idRegistrado).collection("vinculados")
                vinculadoRef.get().addOnSuccessListener { res ->
                    val total = res.size()
                    if (total >= 4) {
                        Log.d("DEBUG", "Ya existen $total dispositivos vinculados. No se puede agregar más.")
                        Toast.makeText(
                            context,
                            "Tiene 4 dispositivos vinculados con esta cuenta",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addOnSuccessListener
                    }

                    // Crear mapa de datos del nuevo dispositivo
                    val hashMap = hashMapOf<String, Any>(
                        "id_dispositivo" to androidId,
                        "dispositivo" to dispositivo,
                        "fecha_registro" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
                        "hora_registro" to mostrarFechaDialog_horaDialog.obtenerHoraActual()
                    )

                    Log.d("DEBUG", "Subiendo datos del dispositivo a Firestore con ID: $androidId")
                    vinculadoRef.document(androidId).set(hashMap)
                        .addOnSuccessListener {
                            Log.d("DEBUG", "Dispositivo vinculado correctamente con ID: $androidId")
                            Toast.makeText(
                                context,
                                "Dispositivo vinculado. Inicio de sesión exitoso",
                                Toast.LENGTH_SHORT
                            ).show()

                            when(tipo_directo){
                                "directo"->{
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                                "regreso"->{
                                    if (context is Activity) {
                                        context.onBackPressed()
                                    }
                                }

                            }

                        }
                        .addOnFailureListener { e ->
                            Log.e("ERROR", "Error al subir dispositivo vinculado", e)
                            Toast.makeText(context, "Error al vincular el dispositivo", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Log.e("ERROR", "No se encontró el usuario con ID: $idRegistrado")
                Toast.makeText(context, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun obtenerAndroidID(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Log.d("ANDROID_ID", "ID único del dispositivo: $androidId")
        return androidId
    }

    fun cerrarSeccion(context: Context, iduser: String, onFinish: () -> Unit) {
        val androidId = obtenerAndroidID(context)
        encontrarUser(iduser) { tipo, coleccion ->
            Log.d("tipo", tipo.toString())
            val docRef = when (tipo) {
                "trabajador" -> FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(iduser).collection("vinculados")
                    .document(androidId)

                "usuario" -> FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("usuarios").collection("usuarios")
                    .document(iduser).collection("vinculados")
                    .document(androidId)

                else -> {
                    Log.d("RESULT", "No se encontró el usuario")
                    onFinish()
                    return@encontrarUser
                }
            }

            Log.d("modeloEXaco", androidId)
            docRef.delete()
                .addOnSuccessListener {
                    Log.d("dispo_vinculado", "Dispositivo eliminado correctamente")
                    onFinish()
                }
                .addOnFailureListener { e ->
                    Log.d("error_eliminar", "Error al eliminar el dispositivo: ${e.message}")
                    onFinish()
                }
        }
    }

    fun verificaAcceso(
        idRegistrado: String,
        context: Context,
        onStart: () -> Unit,
        onFinish: (dispositivoValido: Boolean) -> Unit
    ) {
        firebaseAuth=FirebaseAuth.getInstance()
        val androidId = obtenerAndroidID(context)
        onStart() // indica que la verificación empieza

        encontrarUser(idRegistrado) { tipo, coleccion ->
            if (coleccion != null) {
                coleccion.document(idRegistrado).collection("vinculados").get()
                    .addOnSuccessListener { res ->
                        var dispositivoEncontrado = false
                        for (datos in res) {
                            val id = datos.getString("id_dispositivo")

                            if (androidId == id) {
                                setar_hora_fecha_ultimaConexion(firebaseAuth.uid.toString(),context)
                                dispositivoEncontrado = true
                                break
                            }
                        }
                        if (!dispositivoEncontrado) {
                            FirebaseAuth.getInstance().signOut()
                            if (context is Activity) {
                            }
                        }
                        onFinish(dispositivoEncontrado)
                    }
                    .addOnFailureListener { e ->
                        Log.e("verificaAcceso", "Error: ${e.message}")
                        onFinish(false)
                    }
            } else {
                onFinish(true) // si coleccion es null, dejamos continuar (válido)
            }
        }
    }

    fun setar_hora_fecha_ultimaConexion(idRegistrado: String, context: Context) {
        val androidId = obtenerAndroidID(context)
        encontrarUser(idRegistrado) { tipo, coleccion ->
            if (coleccion != null) {
                val hashMap = hashMapOf<String, Any>(
                    "ultima_con" to mostrarFechaDialog_horaDialog.obtenerHoraActual(),
                    "untima_fecha_con" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
                )
                coleccion.document(idRegistrado).collection("vinculados").document(androidId).set(
                    hashMap, SetOptions.merge()
                ).addOnSuccessListener { res ->
                    Log.d("campo_actualizado", "campos actualizados correctamente")
                }

                    .addOnFailureListener { e ->
                        Log.d(
                            "error_actualizado",
                            "error al actuizar los campos"
                        )
                    }
            }
        }
    }


}
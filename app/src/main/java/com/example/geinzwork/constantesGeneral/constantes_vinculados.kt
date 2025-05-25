package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.geinzwork.dataclass.dataclass_dispo_vinculados
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

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


    fun agregar_vinculado(idRegistrado: String, context: Context) {
        val dispositivo = "${Build.MANUFACTURER} ${Build.MODEL}"
        val androidId = obtenerAndroidID(context)

        Log.d("DEBUG", "Iniciando proceso de vinculación para ID: $idRegistrado")
        Log.d("DEBUG", "Dispositivo detectado: $dispositivo")
        Log.d("DEBUG", "Android ID: $androidId")

        encotrar_user(idRegistrado) { exi, coleccion ->
            if (exi && coleccion != null) {
                Log.d("DEBUG", "Usuario encontrado, preparando datos para guardar")

                val hashMap = hashMapOf<String, Any>(
                    "id_dispositivo" to androidId,
                    "dispositivo" to dispositivo,
                    "fecha_registro" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
                    "hora_registro" to mostrarFechaDialog_horaDialog.obtenerHoraActual()
                )

                Log.d("DEBUG", "Subiendo datos del dispositivo a Firestore con ID: $androidId")
                coleccion.document(idRegistrado)
                    .collection("vinculados")
                    .document(androidId) // 🔐 Este será el ID del documento
                    .set(hashMap)
                    .addOnSuccessListener {
                        Log.d("DEBUG", "Dispositivo vinculado correctamente con ID: $androidId")
                        Toast.makeText(context, "Dispositivo vinculado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ERROR", "Error al subir dispositivo vinculado", e)
                    }
            } else {
                Log.e("ERROR", "No se encontró el usuario con ID: $idRegistrado")
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
            Log.d("tipo",tipo.toString())
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



}
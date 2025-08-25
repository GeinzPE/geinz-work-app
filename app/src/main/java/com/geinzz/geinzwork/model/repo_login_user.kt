package com.geinzz.geinzwork.model

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_vinculados
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class repo_login_user {

    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    val collection_user =
        db.collection("Trabajadores_Usuarios_Drivers").document("users").collection("users")


    fun agregar_user(login_user: login_user, context: Context) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.createUserWithEmailAndPassword(login_user.correo, login_user.password)
            .addOnSuccessListener { authResult ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    val hasmp = hashMapOf<String, Any>(
                        "nombre" to login_user.nombre,
                        "apellido" to login_user.apellido,
                        "nombre_user" to login_user.nombre_user,
                        "correo" to login_user.correo,
                        "numero_user" to login_user.numero_celular,
                        "genero" to login_user.genero,
                        "cod_pais" to login_user.cod_pais,
                        "localida" to login_user.localidad,
                        "fecha_registrada" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
                        "fecha_nac" to login_user.fecha_nac,
                        "id_user" to user.uid // 🔑 Aquí el UID es el mismo que el de FirebaseAuth
                    )

                    // 🔥 Usamos set() para que el id del documento sea igual al UID
                    collection_user.document(user.uid).set(hasmp)
                        .addOnSuccessListener {
                            Log.d("REGISTRO_USER", "Usuario registrado correctamente con UID: ${user.uid}")

                            // agregar dispositivo vinculado usando el UID
                            agregar_dispo_viculado(user.uid, context)

                            Toast.makeText(
                                context,
                                "Usuario creado y logueado correctamente ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("REGISTRO_USER", "Error al ingresar el user en Firestore: ${e.message}")
                        }
                } else {
                    Log.e("REGISTRO_USER", "Error: currentUser es null incluso después de crear la cuenta")
                }
            }
            .addOnFailureListener { e ->
                Log.e("REGISTRO_USER", "Error al crear usuario en FirebaseAuth: ${e.message}")
            }
    }


    fun agregar_user_google(login_google: login_google, context: Context) {
        val hasmap = hashMapOf<String, Any>(
            "nombre" to login_google.nombre,
            "apellido" to login_google.apellido,
            "nombre_user" to login_google.nombre_user,
            "correo" to login_google.correo,
            "numero_user" to login_google.numero_celular,
            "genero" to login_google.genero,
            "cod_pais" to login_google.cod_pais,
            "localida" to login_google.localidad,
            "fecha_registrada" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
            "fecha_nac" to login_google.fecha_nac,
            "id_user" to login_google.id
        )
        collection_user.document(login_google.id).set(hasmap).addOnSuccessListener {
            Log.d(
                "REGISTRO_USER",
                "Campo id_user actualizado correctamente con: ${login_google.id}"
            )
            agregar_dispo_viculado(login_google.id, context)
        }.addOnFailureListener {e->
            Log.e("REGISTRO_USER", "Error al crear usuario en FirebaseAuth: ${e.message}")
        }

    }

    private fun agregar_dispo_viculado(id_user: String, context: Context) {
        val nombre_dispo = "${Build.MANUFACTURER} ${Build.MODEL}"
        val id_dispo = constantes_vinculados.obtenerAndroidID(context)
        val collection_vincualdos =
            collection_user.document(id_user).collection("vinculados").document(id_dispo)
        obtener_token_FCM { token ->
            val hasmap = hashMapOf<String, Any>(
                "id_dispositivo" to id_dispo,
                "nombre_dispo" to nombre_dispo,
                "fecha_registrado" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
                "hora_registrada" to mostrarFechaDialog_horaDialog.obtenerHoraActual(),
                "token_FCM" to token
            )
            collection_vincualdos.set(hasmap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(
                        "REGISTRO_DISPO",
                        "Dispositivo vinculado guardado correctamente en Firestore"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("REGISTRO_DISPO", "Error al registrar dispositivo: ${e.message}")
                }
        }
    }

    private fun obtener_token_FCM(token_user: (String) -> Unit) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                token_user(token)
            } else {
                Log.e("TOKEN_FCM", "Error al obtener token FCM: ${task.exception?.message}")
                token_user("")
            }
        }
    }

}
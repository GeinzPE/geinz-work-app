package com.geinzz.geinzwork.model

import android.R
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_vinculados
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import okio.Options

class repo_login_user {

    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    val collection_user =
        db.collection("Trabajadores_Usuarios_Drivers").document("users").collection("users")

    suspend fun logear_user(correoRaw: String, password: String): Pair<Boolean, String> {
        val auth = FirebaseAuth.getInstance()
        val correo = correoRaw.trim()

        return try {
            // 🔹 Primero validar en tu colección personalizada
            val existe = buscar_correo_suspense(correo)
            Log.d("existe_correo", existe.toString())

            if (!existe) {
                // 🚨 Si no existe en Firestore, ni intentamos logear
                return Pair(false, "correo_no_existe")
            }

            // 🔹 Recién aquí intentamos login en FirebaseAuth
            auth.signInWithEmailAndPassword(correo, password).await()

            Pair(true, "logeado")

        } catch (ex: FirebaseAuthInvalidCredentialsException) {
            Log.e("login_error", "Contraseña incorrecta: ${ex.message}")
            Pair(false, "pass_incorrecta")

        } catch (ex: Exception) {
            Log.e("login_error", "Error desconocido: ${ex.message}", ex)
            Pair(false, ex.message ?: "error_desconocido")
        }
    }



    fun agregar_correo_registrado(id_user: String, correo: String, tipo: String) {
        val ref =
            db.collection("Trabajadores_Usuarios_Drivers").document("users").collection("correos")
                .document(id_user)
        val hasmap = hashMapOf<String, Any>(
            "correo" to correo,
            "tipo" to tipo
        )
        ref.set(hasmap, SetOptions.merge())
            .addOnSuccessListener { Log.d("correo", "correo_guardado_correctamente") }
            .addOnFailureListener { e ->
                Log.d("correo", "error al guardar el correo")
            }
    }

    suspend fun buscar_correo_suspense(correo: String): Boolean {
        return try {
            val ref = db.collection("Trabajadores_Usuarios_Drivers")
                .document("users")
                .collection("correos")

            val querySnapshot = ref.whereEqualTo("correo", correo).get().await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }


    fun agregar_user(login_user: login_user, context: Context, terminado: (Boolean) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.createUserWithEmailAndPassword(login_user.correo, login_user.password)
            .addOnSuccessListener { authResult ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    val contacto = hashMapOf(
                        "cod_telefonico" to login_user.cod_telefeno,
                        "numero_user" to login_user.numero_celular,
                        "nombre_pais_numero" to login_user.nacionalidad_numero
                    )

                    val hasmp = hashMapOf<String, Any>(
                        "nombre" to login_user.nombre,
                        "apellido" to login_user.apellido,
                        "nombre_user" to login_user.nombre_user,
                        "correo" to login_user.correo,
                        "genero" to login_user.genero,
                        "localida" to login_user.localidad,
                        "fecha_registrada" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
                        "fecha_nac" to login_user.fecha_nac,
                        "id_user" to user.uid,
                        "cod_pais" to login_user.cod_pais,
                        "nacionalidad_nacimiento" to login_user.nacionalidad_nacimiento,
                        "contacto" to contacto
                    )


                    collection_user.document(user.uid).set(hasmp)
                        .addOnSuccessListener {
                            Log.d(
                                "REGISTRO_USER",
                                "Usuario registrado correctamente con UID: ${user.uid}"
                            )

                            agregar_correo_registrado(user.uid, login_user.correo, "normal")
                            agregar_nombre_user(login_user.nombre_user, user.uid)
                            agregar_dispo_viculado(user.uid, context)

                            Toast.makeText(
                                context,
                                "Bienvenido a Geinz",
                                Toast.LENGTH_SHORT
                            ).show()
                            terminado(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "REGISTRO_USER",
                                "Error al ingresar el user en Firestore: ${e.message}"
                            )
                            terminado(false)
                        }
                } else {
                    terminado(false)
                    Log.e(
                        "REGISTRO_USER",
                        "Error: currentUser es null incluso después de crear la cuenta"
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("REGISTRO_USER", "Error al crear usuario en FirebaseAuth: ${e.message}")
                terminado(false)
            }
    }

    fun agregar_user_google(
        login_google: login_google,
        context: Context,
        cuenta_creada: (Boolean) -> Unit
    ) {
        val contacto = hashMapOf(
            "cod_telefonico" to login_google.cod_pais, // código telefónico, ej: +51
            "numero_user" to login_google.numero_celular,
            "nombre_pais_numero" to login_google.nacionalidad_numero // ej: Perú +51
        )

        val hasmap = hashMapOf<String, Any>(
            "nombre" to login_google.nombre,
            "apellido" to login_google.apellido,
            "nombre_user" to login_google.nombre_user,
            "correo" to login_google.correo,
            "genero" to login_google.genero,
            "localida" to login_google.localidad,
            "fecha_registrada" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
            "fecha_nac" to login_google.fecha_nac,
            "id_user" to login_google.id,
            "cod_pais" to login_google.cod_pais,                // ISO del país
            "nacionalidad_nacimiento" to login_google.nacionalidad_nacimiento, // nombre del país
            "contacto" to contacto,
        )

        collection_user.document(login_google.id).set(hasmap).addOnSuccessListener {
            Log.d(
                "REGISTRO_USER",
                "Campo id_user actualizado correctamente con: ${login_google.id}"
            )

            cuenta_creada(true)
            agregar_dispo_viculado(login_google.id, context)
            agregar_nombre_user(login_google.nombre_user, login_google.id)
            agregar_correo_registrado(login_google.id, login_google.correo, "google")

        }.addOnFailureListener { e ->
            cuenta_creada(false)
            Log.e("REGISTRO_USER", "Error al crear usuario en FirebaseAuth: ${e.message}")
        }

    }

    fun buscar_nombre_user(nombre_user_escrito: String, existe_nombre: (Boolean) -> Unit) {
        val ref = db.collection("Trabajadores_Usuarios_Drivers").document("users")
            .collection("nombres_user").whereEqualTo("nombres_user", "@$nombre_user_escrito")
        ref.get()
            .addOnSuccessListener { querySnapshot ->
                val existe = !querySnapshot.isEmpty
                existe_nombre(existe)
            }
            .addOnFailureListener { e ->
                existe_nombre(false)
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

    fun agregar_nombre_user(nombre_user: String, id: String) {
        val ref = db.collection("Trabajadores_Usuarios_Drivers").document("users")
            .collection("nombres_user").document(id)
        val hasmap = hashMapOf<String, Any>(
            "id_registrado" to id,
            "nombres_user" to "@$nombre_user"
        )
        ref.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Log.d("agregar_nombre_user", "agregado correcamtner")
        }.addOnFailureListener { e ->
            Log.d("agregar_nombre_user", "error al agregar el user")

        }
    }

    suspend fun verificar_cuenta_google(correo: String): Boolean {
        return try {
            val task = db.collection("Trabajadores_Usuarios_Drivers")
                .document("users")
                .collection("users")
                .whereEqualTo("correo", correo)
                .get()
                .await()
            !task.isEmpty
        } catch (e: Exception) {
            false
        }
    }


}
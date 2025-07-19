package com.geinzz.geinzwork.utils.constantes.constantes

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.databinding.BottomSheetEditarCamposBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object constantes_nombre_usuarios {

    fun agregar_firestoreNombre_usuario(
        id_registrado: String,
        nombre_user: String,
        tipo_cuento: String
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user").document(id_registrado)

        val hashMap = hashMapOf<String, Any>(
            "nombres_user" to "@$nombre_user",
            "id_registrado" to id_registrado,
            "cuenta" to tipo_cuento
        )
        db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
            Log.d("user_agregado", "user agregado correctament")
        }.addOnFailureListener { e ->
            Log.d("error_user", "error al agregar el user")

        }
    }

    fun verificar_existencia_nombre_usuario(
        rawNombreUsuario: String,
        callback: (Boolean) -> Unit
    ) {

        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user")

        db.whereEqualTo("nombres_user", rawNombreUsuario)
            .get()
            .addOnSuccessListener { res ->
                callback(!res.isEmpty) // true si existe, false si no
            }
            .addOnFailureListener { e ->
                println("Error al verificar nombre de usuario: $e")
                callback(false)
            }
    }

    fun obtene_fechas_cambios(id_user: String, fecha: (String?, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user").document(id_user)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val fecha_edicion = data?.get("fecha_edicion") as? String ?: ""
                val fecha_nueva_edicion = data?.get("fecha_nueva_edicion") as? String ?: ""
                fecha(fecha_edicion, fecha_nueva_edicion)

            } else {
                fecha("", "")
            }
        }.addOnFailureListener { e ->
            fecha("", "")

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizar_nombre_usuario(
        bottomSheetDialog: BottomSheetEditarCamposBinding,
        dialog: Dialog,
        dbDocumento: DocumentReference,
        campo: String,
        valor: String,
        id_user: String,
        rawNombreUsuario: String, contex: Context
    ) {
        verificar_existencia_nombre_usuario(rawNombreUsuario) { existe ->
            if (existe) {
                bottomSheetDialog.nombreUser.campoReferidoED.error =
                    "El nombre de usuario ya existe"
            } else {
                verificarDentro_fecha_edidicion(id_user) { actualizar ->
                    if (actualizar) {
                        val db = FirebaseFirestore.getInstance()
                            .collection("Trabajadores_Usuarios_Drivers")
                            .document("nombres_user")
                            .collection("nombres_user").document(id_user)
                        val hashMap = hashMapOf<String, Any>(
                            "nombres_user" to rawNombreUsuario,
                            "editado" to true,
                            "fecha_edicion" to constantesCarrito.obtenerFechaActual(),
                            "fecha_nueva_edicion" to constantesCarrito.obtenerFechaDentroDeUnMes(),
                        )
                        db.set(hashMap, SetOptions.merge()).addOnSuccessListener { res ->
                            Log.d("nombre_user", "Nombre de usuario cambiado correctamente")
                        }.addOnFailureListener { e ->
                            Log.d("nombre_user", "Ocurio un errro al cambiar el nobreUSer")
                        }
                        actualizarCampo(dialog, contex, dbDocumento, campo, valor) {}
                    } else
                        Toast.makeText(
                            contex,
                            "Tiene que pasar un mes para que pueda actualizar su nombre",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verificarDentro_fecha_edidicion(id_user: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user").document(id_user)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val fechaNuevaEdicion = data?.get("fecha_nueva_edicion") as? String ?: ""

                if (fechaNuevaEdicion.isNotEmpty()) {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val fechaActual = LocalDate.now()
                        val fechaLimite = LocalDate.parse(fechaNuevaEdicion, formatter)

                        val puedeEditar =
                            !fechaActual.isBefore(fechaLimite) // true si actual >= limite
                        callback(puedeEditar)
                    } catch (e: Exception) {
                        callback(false) // error al parsear fechas
                    }
                } else {
                    callback(true) // no hay fecha guardada, se permite editar
                }
            } else {
                callback(true) // no hay datos, se permite editar
            }
        }.addOnFailureListener {
            callback(false) // error en la consulta, no permitir
        }
    }

    private fun actualizarCampo(
        dialog: Dialog,
        contex: Context,
        dbDocumento: DocumentReference,
        campo: String,
        valor: String,
        onSuccess: () -> Unit
    ) {
        val hashMap = hashMapOf<String, Any>(campo to valor)
        dbDocumento.update(hashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    contex,
                    "Campo actualizado correctamente Actualiza para ver tus cambios.",
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    contex,
                    "Error al actualizar el campo",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("erro_actualziad", "${e.message}")
            }
    }

}
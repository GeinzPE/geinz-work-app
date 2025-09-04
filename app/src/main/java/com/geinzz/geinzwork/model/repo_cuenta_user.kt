package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.localizate_geinz.cuenta_user.contacto_cuenta_user
import com.geinzz.geinzwork.data.model.localizate_geinz.cuenta_user.cuenta_user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_cuenta_user {
    private val db = FirebaseFirestore.getInstance()

    suspend fun get_datos_user(id_user: String): cuenta_user {
        return try {
            val ref = db.collection("Trabajadores_Usuarios_Drivers")
                .document("users")
                .collection("users")
                .document(id_user)
                .get()
                .await()

            val datos = ref.data
            val contacto_map = datos?.get("contacto") as? Map<*, *>
            val contacto = contacto_map?.let {
                contacto_cuenta_user(
                    cod_telefonico = it["cod_telefonico"] as? String ?: "",
                    pais_telefono = it["nombre_pais_numero"] as? String ?: "",
                    numero_telf = it["numero_user"] as? Number ?: 0
                )
            }
            cuenta_user(
                nombre = datos?.get("nombre") as? String ?: "",
                apellido = datos?.get("apellido") as? String ?: "",
                cod_pais = datos?.get("cod_pais") as? String ?: "",
                correo = datos?.get("correo") as? String ?: "",
                fecha_nac = datos?.get("fecha_nac") as? String ?: "",
                fecha_registrada = datos?.get("fecha_registrada") as? String ?: "",
                genero = datos?.get("genero") as? String ?: "",
                localidad = datos?.get("localida") as? String ?: "",
                nacionalidad_nac = datos?.get("nacionalidad_nacimiento") as? String ?: "",
                nombre_user = datos?.get("nombre_user") as? String ?: "",
                img_perfil = datos?.get("img_perfil") as? String ?: "",
                contacto = contacto
            )
        } catch (e: Exception) {
            cuenta_user()
        }
    }

}
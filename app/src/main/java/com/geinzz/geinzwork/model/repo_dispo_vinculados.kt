package com.geinzz.geinzwork.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class repo_dispo_vinculados {
    private val info_user = repo_info_user()
    private lateinit var firebaseAuth: FirebaseAuth


    fun obtener_dispo_vinculados(): Flow<List<dataclass_dispo_vinculados>> = callbackFlow {
        val firebaseAuth = FirebaseAuth.getInstance()
        val id_registrado = firebaseAuth.uid

        if (id_registrado == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val (encontrado, collection) = info_user.encontrar_user(id_registrado)
        if (!encontrado || collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref_dispo = collection.document(id_registrado)
            .collection("vinculados")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val lista_vinculados = mutableListOf<dataclass_dispo_vinculados>()

                snapshots?.forEach { dispo ->
                    val data = dispo.data
                    val dispositivo = data["dispositivo"] as? String ?: ""
                    val fecha_registro = data["fecha_registro"] as? String ?: ""
                    val hora_registro = data["hora_registro"] as? String ?: ""
                    val id_dispositivo = data["id_dispositivo"] as? String ?: ""
                    val primario = data["primario"] as? Boolean ?: false
                    val ultima_con = data["ultima_con"] as? String ?: ""
                    val untima_fecha_con = data["untima_fecha_con"] as? String ?: ""
                    val marca = obtenerMarcaDesdeModelo(dispositivo)

                    val datos = dataclass_dispo_vinculados(
                        id_dispositivo,
                        dispositivo,
                        hora_registro,
                        fecha_registro,
                        marca,
                        primario,
                        untima_fecha_con,
                        ultima_con
                    )
                    lista_vinculados.add(datos)
                }

                trySend(lista_vinculados).isSuccess
            }

        awaitClose {
            ref_dispo.remove()
        }
    }


    fun obtenerMarcaDesdeModelo(modelo: String): String {
        return when {
            modelo.startsWith("SM-", ignoreCase = true) -> "samsung"
            modelo.startsWith(
                "M2",
                ignoreCase = true
            ) || modelo.startsWith("220") || modelo.startsWith("230") || modelo.startsWith("Xiaomi") -> "Xiaomi"

            modelo.startsWith("RMX", ignoreCase = true) -> "Realme"
            modelo.startsWith("CPH", ignoreCase = true) -> "Oppo"
            modelo.startsWith("V") && modelo.length >= 5 && modelo[1].isDigit() -> "Vivo"
            modelo.startsWith("XT", ignoreCase = true) -> "Motorola"
            modelo.startsWith("G", ignoreCase = true) && modelo.length >= 5 -> "Google"
            modelo.startsWith("LM-", ignoreCase = true) || modelo.startsWith(
                "LG-",
                ignoreCase = true
            ) -> "LG"

            modelo.startsWith("ASUS_", ignoreCase = true) || modelo.startsWith("ZS") -> "Asus"
            modelo.startsWith("XQ-", ignoreCase = true) -> "Sony"
            modelo.startsWith("TA-", ignoreCase = true) -> "Nokia"
            modelo.startsWith("ZTE", ignoreCase = true) -> "ZTE"
            modelo.startsWith("X") && modelo.length >= 5 && modelo[1].isDigit() -> "Infinix"
            modelo.startsWith("KG") || modelo.startsWith("KE") || modelo.startsWith("BD") -> "Tecno"
            else -> "Desconocido"
        }
    }

    suspend fun buscar_primario(
        collection: CollectionReference
    ): String {
        firebaseAuth = FirebaseAuth.getInstance()
        var id_primario = ""

        val primarioexiste = verificar_primario(collection, firebaseAuth.uid.toString())
        val (existeprimario, id_primario_existente) = primarioexiste
        if (existeprimario) {
            id_primario = id_primario_existente
        } else {
            id_primario = ""
        }
        return id_primario
    }


    suspend fun verificar_primario(
        collection: CollectionReference,
        id_user: String
    ): Pair<Boolean, String> {
        val collection_verificados = collection
            .document(id_user)
            .collection("vinculados")
            .get()
            .await()

        for (snapshot in collection_verificados) {
            val esPrimario = snapshot.getBoolean("primario") ?: false
            val idDispositivo = snapshot.getString("id_dispositivo") ?: ""
            if (esPrimario) return Pair(true, idDispositivo)
        }

        return Pair(false, "")
    }


}
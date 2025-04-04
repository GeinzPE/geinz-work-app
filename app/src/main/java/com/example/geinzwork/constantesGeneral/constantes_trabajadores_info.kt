package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet.inicializarRecicleMejoresTrabajadores
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.google.firebase.firestore.FirebaseFirestore

object constantes_trabajadores_info {

    fun obtenerMejoresTrabajadores(
        idTrabajadorActual: String,
        tipot_trabajador: String,
        listaTrabajo: MutableList<dataClassTrabajosd>,
        recicle: RecyclerView,
        contexto: Context,
        OnBackPresser: Boolean,
        trabajdoresDisponibles: (Boolean) -> Unit
    ) {
        val userCollections = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)

        userCollections.get()
            .addOnSuccessListener { querySnapshot ->
                listaTrabajo.clear()
                for (document in querySnapshot.documents) {
                    val userData = document.data
                    val categoriaTrabajo = userData?.get("categoriaTrabajo") as? String

                    if (tipot_trabajador == categoriaTrabajo) {
                        val usuario = dataClassTrabajosd(
                            id = userData?.get("id") as? String,
                            apellido = userData?.get("apellido") as? String,
                            c1 = userData?.get("caracteristica1") as? String,
                            c2 = userData?.get("caracteristica2") as? String,
                            c3 = userData?.get("caracteristica3") as? String,
                            categoria = userData?.get("categoriaTrabajo") as? String,
                            fecha_N = userData?.get("fechaNac") as? String,
                            genero = userData?.get("genero") as? String,
                            horarioam = userData?.get("horario1") as? String,
                            horariopm = userData?.get("horario2") as? String,
                            nacionalidad = userData?.get("nacionalidad") as? String,
                            nombre = userData?.get("nombre") as? String,
                            start = userData?.get("estrellas")?.toString(),
                            tipoT = userData?.get("tipoTrabajo") as? String,
                            localidad = userData?.get("localidad") as? String,
                            codigo = userData?.get("codigo_pais") as? String,
                            numero = userData?.get("numero") as? String,
                            imgpriamria = userData?.get("imagenPerfil") as? String,
                            activado = userData?.get("activado") as? String,
                            edadActual = userData?.get("EdadActual") as? String,
                            verificados = userData?.get("verificado") as? Boolean
                        )
                        if (idTrabajadorActual != usuario.id.toString()) {
                            listaTrabajo.add(usuario)
                        }
                    }
                }

                // ✅ Solo llamar a trabajdoresDisponibles(true) si hay trabajadores en la lista
                if (listaTrabajo.isNotEmpty()) {
                    trabajdoresDisponibles(true)
                } else {
                    trabajdoresDisponibles(false)
                }

                inicializarRecicleMejoresTrabajadores(
                    OnBackPresser,
                    listaTrabajo,
                    recicle,
                    contexto
                )
            }
            .addOnFailureListener {
                trabajdoresDisponibles(false) // Si falla la consulta, también se debe indicar que no hay trabajadores
                Log.e("error", "Error al obtener los trabajadores", it)
            }
    }


}
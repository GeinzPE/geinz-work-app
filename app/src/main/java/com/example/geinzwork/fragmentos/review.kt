package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesReviewComplet
import com.geinzz.geinzwork.databinding.FragmentReviewBinding
import com.google.firebase.firestore.FirebaseFirestore


class review : Fragment() {

    private lateinit var binding: FragmentReviewBinding
    private lateinit var mContex: Context

    companion object {
        private const val ARG_ID_TRABAJADOR = "id_trabajador"

        fun newInstance(idTrabajador: String): review {
            val fragment = review()
            val args = Bundle()
            args.putString(ARG_ID_TRABAJADOR, idTrabajador)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        mContex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun confSwipe(idTrabajador: String) {
        binding.swipe.setOnRefreshListener {
            binding.loading.isVisible=true
            binding.relativeReview.isVisible=false
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipe.isRefreshing = false
                binding.loading.isVisible=false
                binding.relativeReview.isVisible=true
                verificarSihayReviews(idTrabajador.toString())
                obtenerPorcentajeEstrellasUser(idTrabajador.toString())
                promedioEstrellas(idTrabajador.toString())

            }, 2000)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idTrabajador = arguments?.getString(ARG_ID_TRABAJADOR).toString()
        verificarSihayReviews(idTrabajador.toString())
        obtenerPorcentajeEstrellasUser(idTrabajador.toString())
        promedioEstrellas(idTrabajador.toString())
        confSwipe(idTrabajador.toString())

        binding.todos.setOnClickListener {
            binding.reyclerviewReview.isVisible=false
            binding.cargaContenido.isVisible=true
            constantesReviewComplet.obtenerReview(
                binding.SinReview,
                binding.reyclerviewReview,
                binding.cargaContenido,
                idTrabajador,
                binding.reyclerviewReview,
                mContex, "todos"
            )

        }
        binding.filtradoVerificado.setOnClickListener {

            binding.reyclerviewReview.isVisible=false
            binding.cargaContenido.isVisible=true
            constantesReviewComplet.obtenerReview(
                binding.SinReview,
                binding.reyclerviewReview,
                binding.cargaContenido,
                idTrabajador,
                binding.reyclerviewReview,
                mContex, "verificado"
            )
        }

        binding.unoATres.setOnClickListener {
            binding.reyclerviewReview.isVisible=false
            binding.cargaContenido.isVisible=true
            constantesReviewComplet.obtenerReview(
                binding.SinReview,
                binding.reyclerviewReview,
                binding.cargaContenido,
                idTrabajador,
                binding.reyclerviewReview,
                mContex, "uno_tres"
            )
        }

        binding.cuatroACinco.setOnClickListener {
            binding.reyclerviewReview.isVisible=false
            binding.cargaContenido.isVisible=true
            constantesReviewComplet.obtenerReview(
                binding.SinReview,
                binding.reyclerviewReview,
                binding.cargaContenido,
                idTrabajador,
                binding.reyclerviewReview,
                mContex, "cuatro_cinco"
            )
        }
    }

    private fun mostrarDatos() {
        binding.loading.isVisible = false
        binding.relativeReview.isVisible = true
        binding.swipe.isVisible = true
    }

    private fun obtnerReview(idTrabajador: String) {
        constantesReviewComplet.obtenerReview(
            binding.SinReview,
            binding.reyclerviewReview,
            null,
            idTrabajador,
            binding.reyclerviewReview,
            mContex, ""
        )
        Handler(Looper.getMainLooper()).postDelayed({
            mostrarDatos()
        }, 2000)
    }

    private fun verificarSihayReviews(idTrabajador: String) {
        val dbReview = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajador)
            .collection("review")

        dbReview.get()
            .addOnSuccessListener { res ->
                if (res.isEmpty) {
                    // No hay reseñas
                    binding.frameSinReview.isVisible = true
                    binding.loading.isVisible = false
                } else {
                    // Hay reseñas
                    obtnerReview(idTrabajador)
                    binding.loading.isVisible = false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("verificarSihayReviews", "Error al obtener las reseñas", exception)
                binding.loading.isVisible = false
            }
    }


    private fun obtenerPorcentajeEstrellasUser(idUser: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idUser)
            .collection("review")

        db.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val starCounts = (1..5).associateWith { 0 }.toMutableMap()
                    var totalReviews = 0

                    // Iterar sobre las reseñas y sumar las cantidades
                    for (reviewDocument in snapshot.documents) {
                        val estrellas =
                            reviewDocument.getString(Variables.cantidad)?.toIntOrNull() ?: continue
                        if (estrellas in 1..5) {
                            starCounts[estrellas] = starCounts[estrellas]?.plus(1) ?: 1
                            totalReviews++
                        }
                    }

                    // Calcular porcentajes si hay reseñas
                    if (totalReviews > 0) {
                        val porcentaje5 = (starCounts[5] ?: 0) * 100 / totalReviews
                        val porcentaje4 = (starCounts[4] ?: 0) * 100 / totalReviews
                        val porcentaje3 = (starCounts[3] ?: 0) * 100 / totalReviews
                        val porcentaje2 = (starCounts[2] ?: 0) * 100 / totalReviews
                        val porcentaje1 = (starCounts[1] ?: 0) * 100 / totalReviews

                        // Llamar a la función para actualizar la UI
                        actualizarProgresoEstrellas(
                            porcentaje5, porcentaje4, porcentaje3, porcentaje2, porcentaje1
                        )
                    }
                } else {
                    Log.d("obtenerPorcentajeEstrellas", "No hay reseñas para el usuario.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("obtenerPorcentajeEstrellas", "Error al obtener las reseñas: ", exception)
            }
    }


    private fun actualizarProgresoEstrellas(
        porcentaje5: Int,
        porcentaje4: Int,
        porcentaje3: Int,
        porcentaje2: Int,
        porcentaje1: Int
    ) {
        // Conexión de vistas (usando ViewBinding como ejemplo)
        val progress5Stars = binding.IncudeProgrs.progress5Stars
        val porcentaje5Text = binding.IncudeProgrs.porcentaje5Estrellas

        val progress4Stars = binding.IncudeProgrs.progrsVar4estrellas
        val porcentaje4Text = binding.IncudeProgrs.porcentaje4Estrellas

        val progress3Stars = binding.IncudeProgrs.progresVar3Estrellas
        val porcentaje3Text = binding.IncudeProgrs.pocentaje3Estrellas

        val progress2Stars = binding.IncudeProgrs.progrsVar2Estrellas
        val porcentaje2Text = binding.IncudeProgrs.porcentaje2Estrellas

        val progress1Star = binding.IncudeProgrs.progresvar1Estrellas
        val porcentaje1Text = binding.IncudeProgrs.porcentaje1Estrella

        // Actualizar los valores
        progress5Stars.progress = porcentaje5
        porcentaje5Text.text = "$porcentaje5%"

        progress4Stars.progress = porcentaje4
        porcentaje4Text.text = "$porcentaje4.%"

        progress3Stars.progress = porcentaje3
        porcentaje3Text.text = "$porcentaje3%"

        progress2Stars.progress = porcentaje2
        porcentaje2Text.text = "$porcentaje2%"

        progress1Star.progress = porcentaje1
        porcentaje1Text.text = "$porcentaje1%"
    }

    fun promedioEstrellas(idUSer: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(idUSer)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val estellas = data?.get(Variables.estrellas) as? String ?: ""
                val puntaje = estellas.toInt()
                when {
                    puntaje == null -> "No se pudo calcular el promedio"
                    puntaje >= 70 -> {
                        binding.IncudeProgrs.valoraciontrabajdor.text = "5"
                    }

                    puntaje >= 60 -> {
                        binding.IncudeProgrs.valoraciontrabajdor.text = "4.5"
                    }

                    puntaje >= 50 -> {
                        binding.IncudeProgrs.valoraciontrabajdor.text = "4"
                    }

                    puntaje >= 40 -> {
                        binding.IncudeProgrs.valoraciontrabajdor.text = "3.5"
                    }

                    puntaje >= 30 -> {
                        binding.IncudeProgrs.valoraciontrabajdor.text = "3"
                    }

                    else -> "1.0"
                }
            }
        }

    }


}
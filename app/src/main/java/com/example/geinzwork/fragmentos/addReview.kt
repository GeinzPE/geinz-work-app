package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesReviewComplet
import com.geinzz.geinzwork.databinding.FragmentAddReviewBinding
import com.google.firebase.auth.FirebaseAuth


class addReview : Fragment() {
    private lateinit var binding: FragmentAddReviewBinding
    private lateinit var mContex: Context
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val ARG_ID_TRABAJADOR = "id_trabajador"

        fun newInstance(idTrabajador: String): addReview {
            val fragment = addReview()
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
        binding = FragmentAddReviewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val idTrabajador = arguments?.getString(ARG_ID_TRABAJADOR)

        firebaseAuth = FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)
        if (firebaseAuth.uid.toString() == idTrabajador) {
            binding.lottieAnimationView.isVisible = true
            binding.tvErrorMessage.isVisible = true
            binding.container.isVisible=false
        } else {
            binding.lottieAnimationView.isVisible = false
            binding.tvErrorMessage.isVisible = false

            constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)
            if (idTrabajador.toString() == firebaseAuth.uid.toString()) {
                Toast.makeText(mContex, "no puedes dejarte reseñas a ti mismo", Toast.LENGTH_SHORT).show()
                binding.contenidoReseview.isEnabled = false
                binding.cantidadStarts.isEnabled = false
                binding.piblicarReview.isEnabled = false
            } else {
                val tipo = Variables.CuentaFreelancer
                val iduser = firebaseAuth.uid.toString()
                binding.piblicarReview.setOnClickListener {
                    constantesReviewComplet.verificarSiExisteReview(
                        tipo,
                        idTrabajador.toString(),
                        iduser,
                        mContex,
                        binding.contenidoReseview,
                        binding.cantidadStarts,
                        binding.hora,
                        binding.fecha,
                        binding.tipoTrabajoED.text.toString()
                    )
                }
            }
        }
    }

}
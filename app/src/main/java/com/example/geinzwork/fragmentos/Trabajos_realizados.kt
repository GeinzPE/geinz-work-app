package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.adapterViewholder.adapterTrabajo_realizados
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas
import com.geinzz.geinzwork.databinding.FragmentTrabajosRealizadosBinding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.google.firebase.auth.FirebaseAuth


class Trabajos_realizados : Fragment() {
    private lateinit var binding: FragmentTrabajosRealizadosBinding
    private lateinit var mcontex: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private var listAdapter = mutableListOf<dataclas_trabajos_ralizados>()

    companion object {
        private const val ARG_ID_TRABAJADOR = "id_trabajador"

        fun newInstance(idTrabajador: String): Trabajos_realizados {
            val fragment = Trabajos_realizados()
            val args = Bundle()
            args.putString(ARG_ID_TRABAJADOR, idTrabajador)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrabajosRealizadosBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)
        val idTrabajador = arguments?.getString(ARG_ID_TRABAJADOR)
//        val adapter = adapterTrabajo_realizados(listAdapter)
//        constantes_publicaciones_general_user_tiendas.obtenerPublicaciones(
//            idTrabajador.toString(),
//            listAdapter,
//            binding.recicleTrabajosRealizados,
//            mcontex,
//            adapter,
//            binding.linealappLayout,
//            binding.linealNoCuenta,binding.loading
//        )

    }

}
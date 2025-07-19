package com.geinzz.geinzwork.vistaTiendas.hisrotiralfr

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.utils.constantes.constantes.animacionesCambio_color_btnFiltrado_historial
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.databinding.FragmentReservaServiciosfrBinding
import com.geinzz.geinzwork.model.dataclassServiciosHistorial
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class reserva_serviciosfr : Fragment() {
    private lateinit var binding:FragmentReservaServiciosfrBinding
    private lateinit var mcontext:Context
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista= mutableListOf<dataclassServiciosHistorial>()
    private lateinit var listaBtnFiltrado: List<MaterialButton>
    override fun onAttach(context: Context) {
        mcontext=context
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentReservaServiciosfrBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth=FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)
        constantesCarrito.obtenerReservaServicios("todos",binding.recicleReservaServicios,mcontext,lista)
        initializeButtons()
    }
    private fun initializeButtons() {
        listaBtnFiltrado = listOf(binding.todos,binding.reservaPromo, binding.reservaArticulo, binding.reservaServicios)

        // Aplica el estilo predeterminado al botón 'todos' al iniciar
        animacionesCambio_color_btnFiltrado_historial.styleBtn(mcontext, listaBtnFiltrado, binding.todos)

        // Establece los listeners para cada botón
        listaBtnFiltrado.forEach { btn ->
            btn.setOnClickListener {
                // Cambia el estilo para el botón clicado
                animacionesCambio_color_btnFiltrado_historial.styleBtn(mcontext, listaBtnFiltrado, btn)

                // Muestra el Toast correspondiente
                showToastForButton(btn)
            }
        }
    }
    private fun showToastForButton(button: MaterialButton) {
        when (button) {
            binding.reservaPromo -> {
                constantesCarrito.obtenerReservaServicios(
                    "promo",
                    binding.recicleReservaServicios,mcontext,lista)
            }

            binding.reservaArticulo -> {
                constantesCarrito.obtenerReservaServicios(
                    "articulo",
                    binding.recicleReservaServicios,mcontext,lista)
            }
            binding.reservaServicios -> {
                constantesCarrito.obtenerReservaServicios(
                    "servicios",
                    binding.recicleReservaServicios,mcontext,lista)
            }

            binding.todos -> {
                constantesCarrito.obtenerReservaServicios(
                    "todos",
                    binding.recicleReservaServicios,mcontext,lista)
            }
        }

    }
}
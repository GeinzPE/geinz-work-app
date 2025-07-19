package com.geinzz.geinzwork.vistaTiendas.hisrotiralfr

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.databinding.FragmentReservasfrBinding

import com.geinzz.geinzwork.model.dataclassreservas


class reservasfr : Fragment() {
private lateinit var binding:FragmentReservasfrBinding
private lateinit var mcontex:Context
    private val listaReservas = mutableListOf<dataclassreservas>()
    override fun onAttach(context: Context) {
        mcontex=context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentReservasfrBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
     constantesCarrito.obtenerReservas(listaReservas,binding.recicleRerserva,mcontex)
        super.onViewCreated(view, savedInstanceState)
    }



}
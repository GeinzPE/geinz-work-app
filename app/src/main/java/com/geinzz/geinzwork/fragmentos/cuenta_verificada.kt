package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.databinding.FragmentCuentaVerificadaBinding
import com.geinzz.geinzwork.problemas_soporte_politicas.veneficios_verificados

class cuenta_verificada : Fragment() {
    private lateinit var binding: FragmentCuentaVerificadaBinding
    private lateinit var mcontex: Context
    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCuentaVerificadaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ConocerVerificados.setOnClickListener {
            startActivity(
                Intent(
                    mcontex,
                    veneficios_verificados::class.java
                )
            )
        }
    }


}
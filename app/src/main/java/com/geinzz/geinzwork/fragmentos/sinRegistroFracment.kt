package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.Login
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.databinding.FragmentSinRegistroFracmentBinding

class sinRegistroFracment : Fragment() {

    private lateinit var binding:FragmentSinRegistroFracmentBinding
    private lateinit var mcontex:Context

    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSinRegistroFracmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.iniciarSeccion.setOnClickListener {
            startActivity(Intent( mcontex,Login::class.java))
        }
        binding.Registrarme.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(mcontex)
            builder.setTitle("Selecciona tu tipo de cuenta")
            builder.setMessage("Selecciona el tipo de cuenta y registrate gratis en Geinz Work <3")
            builder.setPositiveButton("Cuenta simple") { dialog, which ->
                constantes.showLoadingDialog(mcontex,3000,"Dirigiendo al sitio","espere un momento")
                val vista=Intent( mcontex,CuentaFreelancer::class.java)
                vista.putExtra("tipoCuenta","cuentaSimple")
                vista.putExtra("Title","Cuenta Simple")
                vista.putExtra("pasos","Estas a 1/2 pasos")
                startActivity(vista)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cuenta trabajador") { dialog, which ->
                constantes.showLoadingDialog(mcontex,3000,"Dirigiendo al sitio","espere un momento")
                val vista=Intent( mcontex,CuentaFreelancer::class.java)
                vista.putExtra("tipoCuenta","cuentaTrabajador")
                vista.putExtra("Title","Cuenta Freelancer")
                vista.putExtra("pasos","Estas a 1/5 pasos")
                startActivity(vista)

                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()


        }
    }



}